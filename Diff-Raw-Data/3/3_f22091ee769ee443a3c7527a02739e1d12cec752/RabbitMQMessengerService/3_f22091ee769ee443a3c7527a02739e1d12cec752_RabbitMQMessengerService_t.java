 package edu.stanford.mobisocial.bumblebee;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.security.PublicKey;
 import java.security.interfaces.RSAPublicKey;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import org.jivesoftware.smack.packet.Message;
 import org.xbill.DNS.CNAMERecord;
 
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.Connection;
 import com.rabbitmq.client.ConnectionFactory;
 import com.rabbitmq.client.MessageProperties;
 import com.rabbitmq.client.QueueingConsumer;
 import com.rabbitmq.client.ReturnListener;
 import com.rabbitmq.client.AMQP.BasicProperties;
 import com.rabbitmq.client.ShutdownSignalException;
 
 import edu.stanford.mobisocial.bumblebee.util.Base64;
 
 public class RabbitMQMessengerService extends MessengerService {
 
 	ConnectionFactory factory;
 	Connection conn;
 	Channel inChannel;
 	Channel outChannel;
 	String exchangeKey;
 	String queueName;
 	Thread outThread;
 	Thread inThread;
 	Thread connectThread;
 	
 	private LinkedBlockingQueue<OutgoingMessage> mSendQ = 
 	        new LinkedBlockingQueue<OutgoingMessage>();
 	private MessageFormat mFormat = null;
 	
 	static String encodeRSAPublicKey(RSAPublicKey key) {
 		try {
 			byte[] mod = key.getModulus().toByteArray();
 			byte[] exp = key.getPublicExponent().toByteArray();
 			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
 			DataOutputStream data = new DataOutputStream(bytes);
 			data.write(255);
 			data.write(mod.length);
 			data.write(mod);
 			data.write(exp.length);
 			data.write(exp);
 			data.flush();
 			byte[] raw = bytes.toByteArray();
 			return Base64.encodeToString(raw, false);
 		} catch(IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	synchronized void teardown() {
 		inChannel = null;
 		outChannel = null;
 		conn = null;
 	}
 	
 	public RabbitMQMessengerService(TransportIdentityProvider ident,
 			ConnectionStatus status) {
 		super(ident, status);
         mFormat = new MessageFormat(ident);
         
 		exchangeKey = new String(encodeRSAPublicKey(ident.userPublicKey()));
 		queueName = exchangeKey;
 		factory = new ConnectionFactory();
 	    factory.setHost("pepperjack.stanford.edu");
 	    //may want this higher for battery
 	    factory.setRequestedHeartbeat(30);
 	    connectThread = new Thread(new Runnable() {
 			
 			public void run() {
 				//open the connection
 				while(true) {
 					try {
 						conn = factory.newConnection();
 					} catch(IOException e) {
 						signalConnectionStatus("Failed initial AMQP connection", e);
 						try {
 							Thread.sleep(30000);
 						} catch (InterruptedException e1) {
 						}
 						continue;
 					}
 					signalConnectionStatus("AMQP connected", null);
 					//once its opened the rabbitmq library handles reconnect
 			        outThread = new Thread() {
 			            @Override
 			            public void run() {
 					        for(;;) {
 					        	try {
 									outChannel = conn.createChannel();
 				                	outChannel.setReturnListener(new ReturnListener() {
 										public void handleReturn(int reply_code, String arg1, String arg2, String arg3,
 												BasicProperties arg4, byte[] body) throws IOException {
 											if(reply_code != 200)
 												signalConnectionStatus("Message delivery failure: " + Base64.encodeToString(body, false), null);
 											
 										}
 									});
 					                while (true) {
 					                	OutgoingMessage m = null;
 					                    try {
 											try {
 												m = mSendQ.poll(15, TimeUnit.SECONDS);
 											} catch (InterruptedException e) {
 											}
 											if(!conn.isOpen())
 												return;
 											if(m == null)
 												continue;
 					                    	String plain = m.contents();
 					                    	byte[] cyphered = mFormat.encodeOutgoingMessage(
 					                    			plain, m.toPublicKeys());
 					                    	for(RSAPublicKey pubKey : m.toPublicKeys()){
 					                    		String dest = encodeRSAPublicKey(pubKey);
 						                        outChannel.basicPublish("", dest, true, false, null, cyphered);
 					                    	}
 					                    } catch(CryptoException e) {
 											signalConnectionStatus("Failed to handle message crypto", e);
 											try {
 												conn.close();
 											} catch(IOException e1) {}
 					                    } catch (IOException e) {
 											signalConnectionStatus("Failed to send message over AMQP connection", e);
 					                    	mSendQ.add(m);
 											try {
 												Thread.sleep(30000);
 											} catch (InterruptedException e1) {
 											}
 								        } catch(ShutdownSignalException e) {				        	
 											signalConnectionStatus("Forced shutdown in send AMQP", e);
 								        	return;
 								        }
 					                }
 					        	} catch(IOException e) {
 					        		
 					        	}
 					        }
 			            }
 			        };	 
 			        outThread.start();
 			        
 			        inThread = new Thread(new Runnable() {
 						
 						public void run() {
 					        boolean autoAck = false;
 					        for(;;) {
 						        try {
 									inChannel = conn.createChannel();
							        QueueingConsumer consumer = new QueueingConsumer(inChannel);
 									inChannel.queueDeclare(queueName, true, false, false, null);						
 							        inChannel.basicConsume(queueName, autoAck, consumer);
 							        for(;;) {
 							            QueueingConsumer.Delivery delivery;
 							            try {
 							                delivery = consumer.nextDelivery(15000);
 							            } catch (InterruptedException ie) {
 							                continue;
 							            }
 										if(!conn.isOpen())
 											return;
 							            if(delivery == null)
 							            	continue;
 							            final byte[] body = delivery.getBody();
 							            if(body == null) throw new RuntimeException("Could not decode message.");
 					
 					                    final String id = mFormat.getMessagePersonId(body);
 					                    if (id == null) {
 					                        System.err.println("WTF! person id in message does not match sender!.");
 					                        return;
 					                    }
 					                    RSAPublicKey pubKey = identity().publicKeyForPersonId(id);
 					                    if (pubKey == null) {
 					                        System.err.println("WTF! message from unrecognized sender! " + id);
 					                        return;
 					                    }
 							            
 					                    final String contents = mFormat.decodeIncomingMessage(body, pubKey);
 					                    signalMessageReceived(
 					                        new IncomingMessage() {
 					                            public String from() { return id; }
 					                            public String contents() { return contents; }
 					                            public String toString() { return contents(); }
 					                        });
 							            inChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
 							        }
 			                    } catch(CryptoException e) {
 									signalConnectionStatus("Failed to handle message crypto", e);
 									try {
 										conn.close();
 									} catch(IOException e1) {}
 									return;
 						        } catch(IOException e) {
 									signalConnectionStatus("Failed to receive message over AMQP connection", e);
 									try {
 										Thread.sleep(30000);
 									} catch (InterruptedException e1) {
 									}
 						        } catch(ShutdownSignalException e) {				        	
 									signalConnectionStatus("Forced shutdown in receive AMQP", e);
 						        	return;
 						        }
 					        }
 						}
 					});
 					inThread.start();
 					for(;;) {
 						try {
 							inThread.join();
 							break;
 						} catch(InterruptedException e) {
 							continue;
 						}
 					}
 					for(;;) {
 						try {
 							outThread.join();
 							break;
 						} catch(InterruptedException e) {
 							continue;
 						}
 					}
 					inThread = null;
 					outThread = null;
 					conn = null;
 					inChannel = null;
 					outChannel = null;
 					
 				}
 			}
 		}); 
 	    connectThread.start();
 	}
 
 	@Override
 	public void init() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void sendMessage(OutgoingMessage m) {
 		try {
 			mSendQ.put(m);
 		} catch(InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 }
