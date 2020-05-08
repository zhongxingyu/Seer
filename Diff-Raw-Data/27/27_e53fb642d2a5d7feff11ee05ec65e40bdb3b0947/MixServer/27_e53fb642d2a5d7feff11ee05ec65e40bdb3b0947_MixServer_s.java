 package com.dustyneuron.bitprivacy.exchanger;
 
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ClientMessage;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ServerMessage;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.TransactionSigning;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ServerMessage.Type;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Trade;
 import com.dustyneuron.bitprivacy.bitcoin.WalletMgr;
 import com.dustyneuron.bitprivacy.schemas.SchemaUtils;
 import com.google.bitcoin.core.Sha256Hash;
 import com.google.protobuf.ByteString;
 
 import asg.cliche.Command;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executors;
 
 public class MixServer {
 	
 	public static final int portNumber = 9000;
 	
 	private ServerBootstrap bootstrap;
 	private ChannelFactory factory;
 	private Channel channel;
 	final private List<ServerHandler> serverHandlers;
 	
 	final ChannelGroup allChannels;
 	
 	final private WalletMgr walletMgr;
 	
 	final private Map<Sha256Hash, TradeInProgress> tradesInProgress;
 	final private Map<Sha256Hash, TradeInProgress> tradesInSigning;
 
 	
 	public MixServer(WalletMgr w) {
 		walletMgr = w;
 		allChannels = new DefaultChannelGroup("mix-server");
 		serverHandlers = new ArrayList<ServerHandler>();
 		tradesInProgress = new HashMap<Sha256Hash, TradeInProgress>();
 		tradesInSigning = new HashMap<Sha256Hash, TradeInProgress>();
 	}
 	
 	@Command
 	public synchronized void start() {
 		factory =
 			new NioServerSocketChannelFactory(
 				Executors.newCachedThreadPool(),
                 Executors.newCachedThreadPool());
  
         bootstrap = new ServerBootstrap(factory);
  
         final MixServer mixServer = this;
         bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
              public ChannelPipeline getPipeline() {
             	 System.out.println("Starting new handler");
             	 ServerHandler serverHandler = new ServerHandler(mixServer);
             	 serverHandlers.add(serverHandler);
             	 return Channels.pipeline(serverHandler);
              }
         });
  
         bootstrap.setOption("child.tcpNoDelay", true);
         bootstrap.setOption("child.keepAlive", true);
         channel = bootstrap.bind(new InetSocketAddress(portNumber));
         allChannels.add(channel);
         
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override public void run() {
             	mixServer.stop();
             }
         });
 	}
 	
 	public void sendBytes(byte[] data, Channel c) {
 		ChannelBuffer msgData = ChannelBuffers.buffer(data.length + 4);
 		msgData.writeInt(data.length);
 		msgData.writeBytes(data);
 		c.write(msgData);
 	}
 		
 	
 	void makeTrade(Sha256Hash tradeKey, Trade trade) throws Exception {
 		System.out.println("Making trade...");
 		System.out.flush();
 		
 		TradeInProgress inProgress = tradesInProgress.get(tradeKey);
 		tradesInProgress.remove(tradeKey);
 		tradesInSigning.put(tradeKey, inProgress);
 		
 		inProgress.createInitialTransaction(walletMgr);
 		
 		ContactInfo next = inProgress.getNextToSign();
 		
 		sendBytes(ServerMessage.newBuilder()
 				.setType(Type.SIGN_REQUEST)
 				.setSigning(TransactionSigning.newBuilder()
 					.setTransaction(ByteString.copyFrom(inProgress.getTransactionBytes()))
 					.setSchemaHash(ByteString.copyFrom(tradeKey.getBytes()))
 					.build())
 				.build().toByteArray(),
 				next.getChannel());
 	}
 	
 	void tradeRequest(ClientMessage msg, ContactInfo contact) throws Exception {
 		Trade newTrade = msg.getTrade();
 		System.out.println("Received trade request");
 		System.out.flush();
 		
 		Sha256Hash key = SchemaUtils.getSchemaKey(msg.getTrade().getSchema());
 		if (tradesInProgress.containsKey(key)) {						
 			SchemaUtils.TradeCombinationResult result = SchemaUtils.tryAddTrade(tradesInProgress.get(key).getTrade(), newTrade);
 			if (result == null) {
 				sendBytes(ServerMessage.newBuilder()
 						.setType(Type.ERROR)
 						.setError("Failed to add trade")
 						.setRequestId(msg.getRequestId())
 						.build().toByteArray(),
 						channel);
 			} else {
 				if (!result.passedConstraints) {
 					sendBytes(ServerMessage.newBuilder()
 							.setType(Type.ERROR_TRADE_CONSTRAINTS)
 							.setRequestId(msg.getRequestId())
 							.build().toByteArray(),
 							channel);
 				} else {
 					System.out.println("Added trade");
 					tradesInProgress.get(key).addParticipent(result.trade, contact);
 					sendBytes(ServerMessage.newBuilder()
 							.setType(Type.ADDED_TRADE)
 							.setRequestId(msg.getRequestId())
 							.build().toByteArray(),
 							channel);
 
 					if (result.complete) {
 						makeTrade(key, result.trade);
 					}
 				}
 			}
 		} else {
 			if (!SchemaUtils.isInitialListingValid(msg.getTrade().getSchema(), msg.getTrade().getAllPartiesDataList())) {
 				sendBytes(ServerMessage.newBuilder()
 						.setType(Type.ERROR)
 						.setError("initial listing invalid")
 						.setRequestId(msg.getRequestId())
 						.build().toByteArray(),
 						channel);
 			} else {
 				System.out.println("Added trade");
 				tradesInProgress.put(key, new TradeInProgress(newTrade, contact));
 				sendBytes(ServerMessage.newBuilder()
 						.setType(Type.ADDED_TRADE)
 						.setRequestId(msg.getRequestId())
 						.build().toByteArray(),
 						channel);
 			}
 		}
 	}
 	
 	void signResponse(ClientMessage msg, ContactInfo contact) throws Exception {
 		Sha256Hash key = new Sha256Hash(msg.getSigning().getSchemaHash().toByteArray());
 		if (!tradesInSigning.containsKey(key)) {
 			System.out.println("bad client sent us a sign response with a dud key");
 			return;
 		}
 		
 		TradeInProgress inProgress = tradesInSigning.get(key);
 		inProgress.markParticipentSigned(walletMgr, contact, msg.getSigning().getTransaction().toByteArray());
 		
 		System.out.println("Marked as signed, sending an ACK");
 		sendBytes(ServerMessage.newBuilder()
 				.setType(Type.SIGN_RESPONSE_OK)
 				.build().toByteArray(),
 				contact.getChannel());
 		
 		ContactInfo next = inProgress.getNextToSign();
 		if (next == null) {
 			System.out.println("Signing complete, can now broadcast! (unimplemented)");
 			System.out.println(inProgress.getTransactionToString(walletMgr));
 			tradesInSigning.remove(key);
 			
 			for (ContactInfo c : inProgress.getAllParticipents()) {
 				sendBytes(ServerMessage.newBuilder()
 						.setType(Type.TRADE_COMPLETE)
 						.setSigning(TransactionSigning.newBuilder()
 							.setTransaction(ByteString.copyFrom(inProgress.getTransactionBytes()))
 							.setSchemaHash(ByteString.copyFrom(key.getBytes()))
 							.build())
 						.build().toByteArray(),
 						c.getChannel());
 			}
 			
 		} else {
 			System.out.println("Sending sign request to next participent");
 			sendBytes(ServerMessage.newBuilder()
 					.setType(Type.SIGN_REQUEST)
 					.setSigning(TransactionSigning.newBuilder()
 						.setTransaction(ByteString.copyFrom(inProgress.getTransactionBytes()))
 						.setSchemaHash(ByteString.copyFrom(key.getBytes()))
 						.build())
 					.build().toByteArray(),
 					next.getChannel());
 		}
 
 	}
 
 	synchronized void handleMessage(ClientMessage msg, Channel channel) {
 		ContactInfo contact = new ContactInfo(channel); 
		
 		 try {
			
 			switch (msg.getType()) {
			case TRADE_REQUEST:
				tradeRequest(msg, contact);
				break;
			
			case SIGN_RESPONSE:
				signResponse(msg, contact);
				break;
 				
 				default:
 					throw new Exception("unknown data type");
 			}
 		} catch (Exception exception) {
 			exception.printStackTrace();
 			System.out.println("Server still operating A-OK");
 		}
 	}
 	
 	@Command
 	public synchronized void listTrades() {
 		for (Sha256Hash h : tradesInProgress.keySet()) {
 			System.out.print(tradesInProgress.get(h).toString());
 		}
 	}
 	
 	@Command
 	public synchronized void listTradesSigning() {
 		for (Sha256Hash h : tradesInSigning.keySet()) {
 			System.out.print(tradesInSigning.get(h).toString());
 		}
 	}
 	
 	@Command
 	public synchronized void stop() {
 		if (allChannels.size() > 0) {
 			System.out.println("Starting shutdown...");
 			for (ServerHandler s : serverHandlers) {
 				System.out.println("ServerHandler " + s.toString());
 			}
 			System.out.println("Closing channels...");
 			allChannels.close().awaitUninterruptibly();
 			allChannels.clear();
 			System.out.println("Releasing external resources...");
 			factory.releaseExternalResources();
 			System.out.println("calling bootstrap.shutdown()...");
 			bootstrap.shutdown();
 		}
 	}
 }
