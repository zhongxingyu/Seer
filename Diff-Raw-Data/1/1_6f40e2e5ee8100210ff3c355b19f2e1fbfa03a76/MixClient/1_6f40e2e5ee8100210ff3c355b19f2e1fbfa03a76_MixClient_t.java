 package com.dustyneuron.bitprivacy.exchanger;
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ListedTrade;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ServerMessage;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Trade;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.TradeInfo;
 
 import java.math.BigInteger;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 
 import org.apache.commons.codec.binary.Hex;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ClientMessage;
 import com.dustyneuron.bitprivacy.bitcoin.WalletUtils;
 import com.dustyneuron.bitprivacy.exchanger.ClientHandler.IncomingMessage;
 import com.dustyneuron.bitprivacy.schemas.BlockDownloader;
 import com.dustyneuron.bitprivacy.schemas.PartialSigner;
 import com.dustyneuron.bitprivacy.schemas.SchemaUtils;
 import com.dustyneuron.bitprivacy.schemas.SimpleMix;
 import com.dustyneuron.bitprivacy.schemas.TransactionVerifier;
 import com.google.bitcoin.core.Address;
 import com.google.bitcoin.core.Sha256Hash;
 import com.google.bitcoin.core.Transaction;
 import com.google.bitcoin.core.Wallet;
 import com.google.protobuf.ByteString;
 
 import asg.cliche.Command;
 import asg.cliche.Param;
 
 public class MixClient implements Runnable {
 	ChannelFactory factory;
 	ClientBootstrap bootstrap;
 	ChannelFuture future;
 	ClientHandler client;
 	
 	private Wallet wallet;
 	private BlockDownloader blockDownloader;
 	
 	private List<ClientTradeInfo> trades;
 	private List<ClientTradeInfo> completedTrades;
 	
 	private boolean shutdown;
 	
 	public MixClient(Wallet w, BlockDownloader bl) {
 		blockDownloader = bl;
 		wallet = w;
 		trades = new ArrayList<ClientTradeInfo>();
 		completedTrades = new ArrayList<ClientTradeInfo>();
 		shutdown = false;
 	}
 	
 	
 	@Command
 	public void connect(
 			@Param(name="server", description="Server hostname or IP address")
 			String host) throws Exception {
		connect(host, true);
 	}
 	
 	void connect(String host, boolean startWorker) throws Exception {
 
 		if (future != null) {
 			throw new Exception("already connected");
 		}
 		
 		InetSocketAddress server = new InetSocketAddress(InetAddress.getByName(host), MixServer.portNumber);
 		
 		System.out.println("Connecting to " + server);
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
         future = bootstrap.connect(server);
         future.awaitUninterruptibly().getChannel();
         if (!future.isSuccess()) {
         	throw new Exception(future.getCause());
         }
         
         final MixClient mixClient = this;
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override public void run() {
             	mixClient.disconnect();
             }
         });
         if (startWorker) {
 	        Thread t = new Thread(this);
 	        t.start();
         }
 	}
 
 	@Override
 	public void run() {
 		while (!shutdown) {
 			try {
 				IncomingMessage i = client.getMessage();
 				handleMessage(i.msg, i.channel);
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.out.println("Client still processing messages");
 			}
 		}
 	}
 	
 	IncomingMessage testGetMessage(long timeout, TimeUnit unit) throws Exception {
 		while (client == null) {
 			Thread.sleep(100);
 		}
 		return client.getMessage(timeout, unit);
 	}
 	
 	void handleSignRequest(ServerMessage serverMsg, Channel channel) throws Exception {
 		System.out.println("Request to sign transaction");
 		
 		ClientTradeInfo info = findTrade(new Sha256Hash(serverMsg.getTradeInfo().getSchemaHash().toByteArray()), serverMsg.getTradeInfo().getTradeId());
 		if (info == null) {
 			throw new Exception("Server is misbehaving - asked us to sign a tx we know nothing about");
 		}
 		if (info.unsigned != null) {
 			throw new Exception("Server is misbehaving - we have already signed this transaction!");
 		}
 		if (serverMsg.getTransaction().toByteArray().length == 0) {
 			throw new Exception("Server is misbehaving - transaction is 0 bytes");
 		}
 		
 		System.out.println("Raw tx data:");
 		System.out.println(new String(Hex.encodeHex(serverMsg.getTransaction().toByteArray())));
 		
 		Transaction newTx = new Transaction(wallet.getNetworkParameters(), serverMsg.getTransaction().toByteArray());
 		if (!TransactionVerifier.isSignRequestValid(newTx, serverMsg.getTrade(), info.myTradeRequest, wallet, blockDownloader)) {
 			throw new Exception("Server is misbehaving - sign request is invalid");
 		}
 		info.unsigned = newTx;
 		System.out.println(WalletUtils.transactionToString(info.unsigned, wallet));
 		System.out.println("Auto-signing:");
 		info.signed = PartialSigner.doSignRequest(info.unsigned, info.myTradeRequest, wallet);
 		System.out.println(WalletUtils.transactionToString(info.signed, wallet));
 		
     	ClientMessage msg = ClientMessage.newBuilder()
         		.setType(ClientMessage.Type.SIGN_RESPONSE)
         		.setPublicKey(ByteString.copyFrom(info.getPublicKey()))
         		.setTradeInfo(serverMsg.getTradeInfo())
         		.setTransaction(ByteString.copyFrom(info.signed.bitcoinSerialize()))
         		.build();
 
     	client.sendMessage(msg);
 	}
 	
 	@Command(description="Join in an existing mix on the server")
 	public void joinMix(
 			@Param(name="inputAddress", description="Address from your wallet. Will use largest unspent output.")
 			String inputAddress,
 			@Param(name="outputAddress", description="Destination address for mixed coins to end up")
 			String outputAddress,
 			@Param(name="numParties", description="How many people you'd like to mix with (inc. yourself)")
 			int numParties,
 			@Param(name="existingTradeId", description="Server tradeId as shown by 'list-remote-trades'/'lrt'")
 			int existingTradeId) throws Exception {
 		_mix(inputAddress, outputAddress, numParties, existingTradeId);
 	}
 	
 	@Command(description="Register a new mix on the server")
 	public void newMix(
 			@Param(name="inputAddress", description="Address from your wallet. Will use largest unspent output.")
 			String inputAddress,
 			@Param(name="outputAddress", description="Destination address for mixed coins to end up")
 			String outputAddress,
 			@Param(name="numParties", description="How many people you'd like to mix with (inc. yourself)")
 			int numParties) throws Exception {
 		_mix(inputAddress, outputAddress, numParties, -1);
 	}
 
 	synchronized void _mix(String inputAddress, String outputAddress, int numParties, int existingTradeId) throws Exception {
 		
 		Address input = new Address(wallet.getNetworkParameters(), inputAddress);
 		Transaction t = WalletUtils.getLargestSpendableOutput(input, wallet);
     	if (t == null) {
     		throw new Exception("couldn't find transaction");
     	}
 		int unspentOutputIdx = WalletUtils.getLargestSpendableOutput(t, input);
     	if (unspentOutputIdx < 0) {
     		throw new Exception("couldn't find spendable output");
     	}
     	
     	BigInteger value = t.getOutput(unspentOutputIdx).getValue();
 		
     	Trade trade = SimpleMix.createTrade(t, unspentOutputIdx, value, new Address(wallet.getNetworkParameters(), outputAddress), numParties);
     	tradeRequest(trade, existingTradeId);
 	}
 	
 	synchronized void tradeRequest(Trade trade, int existingTradeId) throws Exception {
     	
 		Sha256Hash tradeHash = SchemaUtils.getSchemaKey(trade.getSchema());
     	ClientTradeInfo info = new ClientTradeInfo(tradeHash, trade);
     	if (existingTradeId != -1) {
     		
     	}
     	trades.add(info);
     	System.out.print(info.toString(wallet, false));
 
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
     	client.sendMessage(msg);
 	}
 		
 	@Command
 	public void disconnect() {
 		if (future != null) {
 			shutdown = true;
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
 	
 	@Command(description="List all mixes on the server waiting for new people")
 	synchronized public void listRemoteTrades() throws Exception {
 		ClientMessage msg = ClientMessage.newBuilder()
         		.setType(ClientMessage.Type.LIST_TRADES)
         		.build();
     	client.sendMessage(msg);
 	}
 	
 	@Command
 	public void showTrade(
 			@Param(name="tradeId", description="tradeId as shown by 'list-trades'/'lt'")
 			int tradeId) throws Exception {
 		for (ClientTradeInfo c : trades) {
 			if (c.tradeId == tradeId) {
 				System.out.println(c.toString(wallet, true));
 			}
 		}
 		for (ClientTradeInfo c : completedTrades) {
 			if (c.tradeId == tradeId) {
 				System.out.println(c.toString(wallet, true));
 			}
 		}
 	}
 	
 	@Command
 	public void listTrades() throws Exception {
 		for (ClientTradeInfo c : trades) {
 			System.out.println(c.toString(wallet, false));
 		}
 	}
 	
 	@Command
 	public void listCompletedTrades() throws Exception {
 		for (ClientTradeInfo c : completedTrades) {
 			System.out.println(c.toString(wallet, false));
 		}
 	}
 
 	@Command
 	synchronized public boolean deleteTrade(
 			@Param(name="tradeId", description="tradeId as shown by 'list-trades'/'lt'")
 			int tradeId) throws Exception {
 		ClientTradeInfo found = null;
 		for (ClientTradeInfo c : trades) {
 			if (c.tradeId == tradeId) {
 				found = c;
 				break;
 			}
 		}
 		if (found != null) {
 			trades.remove(found);
 			return true;
 		}
 		return false;
 	}
 	
 	void handleNewTradeListed (ServerMessage msg) throws Exception {
 		System.out.println("New trade listed OK, registering tradeId #" + msg.getTradeInfo().getTradeId());
 		List<ClientTradeInfo> matches = findTrades(new Sha256Hash(msg.getTradeInfo().getSchemaHash().toByteArray()));
 		if (matches.size() > 1) {
 			throw new Exception("oops, we have several new trades with this hash. TODO: please code better");
 		} else if (matches.size() == 0) {
 			throw new Exception("got a NEW_TRADE_LISTED about an unknown trade!");
 		}
 		matches.get(0).tradeId = msg.getTradeInfo().getTradeId();
 	}
 
 	synchronized public void handleMessage(ServerMessage msg, Channel channel) throws Exception {
 		System.out.println("Client handling msg of type " + msg.getType());		 
 		System.out.flush();
 
 		switch (msg.getType()) {
 			case ERROR:
 				System.err.println(msg.getError());
 				break;
 				
 			case NEW_TRADE_LISTED:
 				handleNewTradeListed(msg);
 				break;
 				
 			case SIGN_REQUEST:
 				handleSignRequest(msg, channel);
 				break;
 				
 			case TRADE_COMPLETE:
 				System.out.println("Trade complete, moving to completed list");
 				ClientTradeInfo info = findTrade(new Sha256Hash(msg.getTradeInfo().getSchemaHash().toByteArray()), msg.getTradeInfo().getTradeId());
 				info.completed = new Transaction(wallet.getNetworkParameters(), msg.getTransaction().toByteArray());
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
