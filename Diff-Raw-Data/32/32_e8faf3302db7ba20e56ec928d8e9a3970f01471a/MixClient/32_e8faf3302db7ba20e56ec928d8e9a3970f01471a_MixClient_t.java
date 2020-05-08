 package com.dustyneuron.bitprivacy.exchanger;
 
import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ListedTrade;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ServerMessage;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Trade;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.TradeInfo;
 
 import java.math.BigInteger;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executors;
 
 
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ClientMessage;
 import com.dustyneuron.bitprivacy.bitcoin.WalletMgr;
 import com.dustyneuron.bitprivacy.schemas.SchemaUtils;
 import com.dustyneuron.bitprivacy.schemas.SimpleMix;
 import com.google.bitcoin.core.Address;
 import com.google.bitcoin.core.Sha256Hash;
 import com.google.bitcoin.core.Transaction;
 import com.google.protobuf.ByteString;
 
 import asg.cliche.Command;
 
 public class MixClient {
 	ChannelFactory factory;
 	ClientBootstrap bootstrap;
 	ChannelFuture future;
 	ClientHandler client;
 	
 	WalletMgr walletMgr;
 	
 	private List<ClientTradeInfo> trades;
 	private List<ClientTradeInfo> completedTrades;
 	
 	public MixClient(WalletMgr w) {
 		walletMgr = w;
 		trades = new ArrayList<ClientTradeInfo>();
 		completedTrades = new ArrayList<ClientTradeInfo>();
 	}
 	
 	
 	@Command
 	public void connect() {
 		factory =
 			new NioClientSocketChannelFactory(
 				Executors.newCachedThreadPool(),
                 Executors.newCachedThreadPool());
  
         bootstrap = new ClientBootstrap(factory);
  
         final MixClient mx = this;
         bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
              public ChannelPipeline getPipeline() {
             	 client = new ClientHandler(mx);
             	 return Channels.pipeline(client);
              }
         });
  
         bootstrap.setOption("child.tcpNoDelay", true);
         bootstrap.setOption("child.keepAlive", true);
         future = bootstrap.connect(new InetSocketAddress("localhost", MixServer.portNumber));
         
         future.awaitUninterruptibly();
         if (!future.isSuccess()) {
         	future.getCause().printStackTrace();
         }
         
         final MixClient mixClient = this;
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override public void run() {
             	mixClient.disconnect();
             }
         });
         
 	} 
 	
 	void handleSignRequest(ServerMessage serverMsg, Channel channel) throws Exception {
 		
 		System.out.println("Request to sign transaction:");
 		
 		ClientTradeInfo info = findTrade(new Sha256Hash(serverMsg.getTradeInfo().getSchemaHash().toByteArray()), serverMsg.getTradeInfo().getTradeId());
 		if (info == null) {
 			System.err.println("Server is misbehaving - asked us to sign a tx we know nothing about");
 			return;
 		}
 		if (info.unsigned != null) {
 			System.err.println("Server is misbehaving - we have already signed this transaction!");
 			return;
 		}
 		
 		info.unsigned = new Transaction(walletMgr.params, serverMsg.getTransaction().toByteArray());
 		System.out.println(walletMgr.transactionToString(info.unsigned));
 		System.out.println("Auto-signing:");
 		info.signed = walletMgr.newSignedInputsIfPossible(info.unsigned);
 		System.out.println(walletMgr.transactionToString(info.signed));
 		System.out.println("Sending back...");
 		
     	ClientMessage msg = ClientMessage.newBuilder()
         		.setType(ClientMessage.Type.SIGN_RESPONSE)
         		.setPublicKey(ByteString.copyFrom(info.getPublicKey()))
         		.setTradeInfo(serverMsg.getTradeInfo())
         		.setTransaction(ByteString.copyFrom(info.signed.bitcoinSerialize()))
         		.build();
 
     	client.sendBytes(msg.toByteArray());
 	}
 	
 	@Command
 	public void mix(String inputAddress, String outputAddress, int numParties, int existingTradeId) throws Exception {
 		_mix(inputAddress, outputAddress, numParties, existingTradeId);
 	}
 	
 	@Command
 	public void mix(String inputAddress, String outputAddress, int numParties) throws Exception {
 		_mix(inputAddress, outputAddress, numParties, -1);
 	}
 
 	public void _mix(String inputAddress, String outputAddress, int numParties, int existingTradeId) throws Exception {
 		
 		Address input = new Address(walletMgr.params, inputAddress);
 		Transaction t = walletMgr.getLargestSpendableOutput(input);
     	if (t == null) {
     		throw new Exception("couldn't find transaction");
     	}
 		int unspentOutputIdx = walletMgr.getLargestSpendableOutput(t, input);
     	if (unspentOutputIdx < 0) {
     		throw new Exception("couldn't find spendable output");
     	}
     	
     	BigInteger value = t.getOutput(unspentOutputIdx).getValue();
 		
     	Trade trade = SimpleMix.createTrade(t, unspentOutputIdx, value, new Address(walletMgr.params, outputAddress), numParties);
     	
     	Sha256Hash tradeHash = SchemaUtils.getSchemaKey(trade.getSchema());
     	ClientTradeInfo info = new ClientTradeInfo(tradeHash, trade);
     	trades.add(info);
     	
     	
     	ClientMessage msg;
     	if (existingTradeId == -1) {
         	msg = ClientMessage.newBuilder()
             		.setType(ClientMessage.Type.NEW_TRADE_REQUEST)
             		.setPublicKey(ByteString.copyFrom(info.getPublicKey()))
             		.setTrade(trade)
             		.build();
     	} else {
     		info.tradeId = existingTradeId;
         	msg = ClientMessage.newBuilder()
             		.setType(ClientMessage.Type.ADD_TRADE_REQUEST)
             		.setPublicKey(ByteString.copyFrom(info.getPublicKey()))
             		.setTrade(trade)
             		.setTradeInfo(TradeInfo.newBuilder()
             				.setSchemaHash(ByteString.copyFrom(tradeHash.getBytes()))
             				.setTradeId(existingTradeId)
             				.build())
             		.build();
     	}
     	
     	System.out.println(msg.toString());
     	client.sendBytes(msg.toByteArray());
 	}
 		
 	@Command
 	public void disconnect() {
 		if (future != null) {
 			future.getChannel().close().awaitUninterruptibly();
 			future = null;
 			factory.releaseExternalResources();
 			bootstrap.shutdown();
 		}
 	}
 	
 	ClientTradeInfo findTrade(Sha256Hash schemaHash, int tradeId) {
 		for (ClientTradeInfo c : trades) {
 			if (c.schemaHash.equals(schemaHash) && (c.tradeId == tradeId)) {
 				return c;
 			}
 		}
 		return null;
 	}
 
 	List<ClientTradeInfo> findTrades(Sha256Hash schemaHash) {
 		List<ClientTradeInfo> results = new ArrayList<ClientTradeInfo>();
 		for (ClientTradeInfo c : trades) {
 			if (c.schemaHash.equals(schemaHash)) {
 				results.add(c);
 			}
 		}
 		return results;
 	}
 	
 	@Command
 	public void listRemoteTrades() throws Exception {
 		ClientMessage msg = ClientMessage.newBuilder()
         		.setType(ClientMessage.Type.LIST_TRADES)
         		.build();
     	client.sendBytes(msg.toByteArray());
 	}
 	
 	@Command
 	public void showTrade(int tradeId) throws Exception {
 		for (ClientTradeInfo c : trades) {
 			if (c.tradeId == tradeId) {
 				System.out.println(c.toString(walletMgr, true));
 			}
 		}
 		for (ClientTradeInfo c : completedTrades) {
 			if (c.tradeId == tradeId) {
 				System.out.println(c.toString(walletMgr, true));
 			}
 		}
 	}
 	
 	@Command
 	public void listTrades() throws Exception {
 		for (ClientTradeInfo c : trades) {
 			System.out.println(c.toString(walletMgr, false));
 		}
 	}
 	
 	@Command
 	public void listCompletedTrades() throws Exception {
 		for (ClientTradeInfo c : completedTrades) {
 			System.out.println(c.toString(walletMgr, false));
 		}
 	}
 
 
 	public void handleMessage(ServerMessage msg, Channel channel) throws Exception {
		System.out.println("Client received msg of type " + msg.getType());		 
		System.out.flush();
 
 		switch (msg.getType()) {
 			case ERROR:
 				System.err.println(msg.getError());
 				break;
 				
 			case NEW_TRADE_LISTED:
 				System.out.println("New trade listed OK, registering tradeId #" + msg.getTradeInfo().getTradeId());
 				List<ClientTradeInfo> matches = findTrades(new Sha256Hash(msg.getTradeInfo().getSchemaHash().toByteArray()));
 				if (matches.size() < 1) {
 					throw new Exception("oops, we have several new trades with this hash. TODO: please code better");
 				}
 				matches.get(0).tradeId = msg.getTradeInfo().getTradeId();
 				break;
 				
 			case SIGN_REQUEST:
 				handleSignRequest(msg, channel);
 				break;
 				
 			case TRADE_COMPLETE:
 				System.out.println("Trade complete, moving to completed list");
 				ClientTradeInfo info = findTrade(new Sha256Hash(msg.getTradeInfo().getSchemaHash().toByteArray()), msg.getTradeInfo().getTradeId());
 				info.completed = new Transaction(walletMgr.params, msg.getTransaction().toByteArray());
 				completedTrades.add(info);
 				trades.remove(info);
 				break;
 				
			case LIST_TRADES_RESPONSE:
				for (ListedTrade t : msg.getTradesList()) {
					System.out.println(t.getTradeInfo());
					System.out.println(t.getPartyTypesList() + "\n");
				}
				break;
				
 			default:
 				break;
 		}
 	}
 }
