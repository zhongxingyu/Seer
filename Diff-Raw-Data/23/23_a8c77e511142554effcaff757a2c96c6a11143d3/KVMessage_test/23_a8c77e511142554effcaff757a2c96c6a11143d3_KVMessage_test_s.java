 package edu.berkeley.cs162;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ByteArrayInputStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 public class KVMessage_test {
 	@Test
 	public void constructor1() {
 		//Test #1 for constructor1
 		try {
 			KVMessage kv = new KVMessage("getreq");
 			assertTrue(kv.getMsgType() == "getreq");
 		}
 		catch (KVException e) {
 			assertTrue(false);
 		}
 		
 		//Test #2 for constructor1
 		try {
 			KVMessage kv = new KVMessage("wrongType");
 			assertTrue(false);
 		}
 		catch (KVException e) {
 			KVMessage kv = e.getMsg();
 			assertTrue(kv.getMsgType()=="resp" && kv.getMessage()=="Message format incorrect");
 		}
 	}
 	
 	@Test
 	public void constructor2() {
 		//Test #1 for constructor2
 		try {
 			KVMessage kv = new KVMessage("resp", "This is a sample message");
 			assertTrue(kv.getMsgType()=="resp" && kv.getMessage()=="This is a sample message");
 		}
 		catch (KVException e) {
 			assertTrue(false);
 		}
 		
 		//Test #2 for constructor2
 		try {
 			KVMessage kv = new KVMessage("wrongType");
 			assertTrue(false);
 		}
 		catch (KVException e) {
 			KVMessage kv = e.getMsg();
 			assertTrue(kv.getMsgType()=="resp" && kv.getMessage()=="Message format incorrect");
 		}
 	}
 	
 	@Test
 	public void constructor3() {
 		//Test #1 for constructor3
 		try {
 			String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"getreq\"><Key>sampleKey</Key></KVMessage>";
 			InputStream stream = new ByteArrayInputStream(input.getBytes());
             KVMessage kv = new KVMessage(stream);
             assertTrue(kv.getMsgType().equals("getreq") && kv.getKey().equals("sampleKey"));
 		}
 		catch (KVException e) {
 			assertTrue(false);
 		}
 		
 		//Test #2 for constructor3
 		try {
 			String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"putreq\"><Key>sampleKey</Key><Value>sampleValue</Value></KVMessage>";
 			InputStream stream = new ByteArrayInputStream(input.getBytes());
             KVMessage kv = new KVMessage(stream);
             assertTrue(kv.getMsgType().equals("putreq") && kv.getKey().equals("sampleKey") && kv.getValue().equals("sampleValue"));
 		}
 		catch (KVException e) {
 			assertTrue(false);
 		}
 		
 		//Test #3 for constructor3
 		try {
 			String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"delreq\"><Key>sampleKey</Key></KVMessage>";
 			InputStream stream = new ByteArrayInputStream(input.getBytes());
             KVMessage kv = new KVMessage(stream);
             assertTrue(kv.getMsgType().equals("delreq") && kv.getKey().equals("sampleKey"));
 		}
 		catch (KVException e) {
 			assertTrue(false);
 		}
 		
 		//Test #4 for constructor3
 		try {
 			String wrongInput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"WRONG_TYPE\"><Key>sampleKey</Key></KVMessage>";
 			InputStream stream = new ByteArrayInputStream(wrongInput.getBytes());
             KVMessage kv = new KVMessage(stream);
             assertTrue(false);
 		}
 		catch (KVException e) {
 			KVMessage kv = e.getMsg();
 			assertTrue(kv.getMsgType()=="resp" && kv.getMessage()=="Message format incorrect");
 		}
 		
 		//Test #5 for constructor3
 		try {
 			KVMessage kvm = new KVMessage("resp");
 			kvm.setKey("sampleKey");
 			kvm.setValue("sampleValue");
 			String xml = kvm.toXML();
 			InputStream stream = new ByteArrayInputStream(xml.getBytes());
 			
 			KVMessage kvm2 = new KVMessage(stream);
 			assertTrue(kvm2.getMsgType().equals("resp"));
 			assertTrue(kvm2.getKey().equals("sampleKey"));
 			assertTrue(kvm2.getValue().equals("sampleValue"));
 		}
 		catch (KVException e) {
 			KVMessage kv = e.getMsg();
 			assertTrue(false);
 		}
 	}
 	
 	@Test
 	public void toXML() {
 		//Test #1 for toXML
 		try {
 			KVMessage kv = new KVMessage("getreq");
 			kv.setKey("sampleKey");
 			String answer = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"getreq\"><Key>sampleKey</Key></KVMessage>";
 			assertTrue(kv.toXML().equals(answer));
 		}
 		catch (KVException e) {
 			assertTrue(false);
 		}
 		
 		//Test #2 for toXML
 		try {
 			KVMessage kv = new KVMessage("putreq");
 			kv.setKey("sampleKey");
 			kv.setValue("sampleValue");
 			String answer = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"putreq\"><Key>sampleKey</Key><Value>sampleValue</Value></KVMessage>";
 			assertTrue(kv.toXML().equals(answer));
 		}
 		catch (KVException e) {
 			assertTrue(false);
 		}
 		
 		//Test #3 for toXML
 		try {
 			KVMessage kv = new KVMessage("delreq");
 			kv.setKey("sampleKey");
 			String answer = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"delreq\"><Key>sampleKey</Key></KVMessage>";
 			assertTrue(kv.toXML().equals(answer));
 		}
 		catch (KVException e) {
 			assertTrue(false);
 		}
 		
 		//Test #4 for toXML
 		try {
 			KVMessage kv = new KVMessage("resp");
 			kv.setMessage("Success");
 			String answer = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><KVMessage type=\"resp\"><Message>Success</Message></KVMessage>";
 			assertTrue(kv.toXML().equals(answer));
 		}
 		catch (KVException e) {
 			assertTrue(false);
 		}
 	}
 	
 	@Test
 	public void sendMessage() {
 		Thread client = new Thread() {
 			public void run() {
 				InetAddress server = null;
 				int port = 2222;
 				Socket socket = null;
 				            
 				try {
 					server = InetAddress.getLocalHost();
 					socket = new Socket(server, port);
 					                
 					KVMessage request = null;
 					KVMessage response = null;
 					InputStream is = null;
 					                
 					request = new KVMessage("putreq");
 					request.setKey("sampleKey");
 					request.setValue("sampleValue");
 					
 					System.out.println("BB");
 					request.sendMessage(socket);
 					//<--------------------------------------- OK
 					is = socket.getInputStream();
 					System.out.println("CC1");
 					response = new KVMessage(is);
 					System.out.println("CC2");
					System.out.println(response.toXML());
 				}
 				catch (KVException e) {
 					System.out.println(e.getMsg().getMessage());
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 				finally {
 					try {
 						socket.close();
 					}
 					catch (IOException e) {
 					            	
 					}
 				}
 			}
 		};
 		
 		Thread server = new Thread() {
 			public void run() {
 				int port = 2222;
 				ServerSocket serverSocket = null;
 				Socket socket = null;
 				        
 				try {
 					serverSocket = new ServerSocket(port);
 					socket = serverSocket.accept();
 								
 					KVMessage request = null;
 					KVMessage response = null;
 					InputStream is = null;
 								
 					System.out.println("AA");
 					is = socket.getInputStream();
 					request = new KVMessage(is);
 					
 					System.out.println("DD");
 					response = new KVMessage("resp","Success");
 					response.sendMessage(socket);
 				}
 				catch (KVException e) {
 					System.out.println(e.getMsg().getMessage());
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 				finally {
 					try {
 						socket.close();
 					}
 					catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		};
 		
 		server.start();
 		client.start();
 		
 		try {
 			server.join();
 			client.join();
 		}
 		catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 }
