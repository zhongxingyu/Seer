 package org.qrone.sample;
 
 import java.io.IOException;
 import java.util.LinkedList;
 
 import org.qrone.xmlsocket.XMLSocket;
 import org.qrone.xmlsocket.XMLSocketServer;
 import org.qrone.xmlsocket.event.XMLSocketListener;
 import org.qrone.xmlsocket.event.XMLSocketServerListener;
 import org.w3c.dom.Document;
 
 /**
  * Ă XML ڑ̑Sɂ̂܂ܑT[o[iport:9601j̃TvB
  * 
  * @author J.Tabuchi
  * @since 2005/8/6
  * @version 1.0
  * @link QrONE Technology : http://www.qrone.org/
  */
 public class QrXMLSocketServer {
 	// T[o[̑҂|[gԍ
 	public static final int SERVER_PORT = 9601;
 	
 	public static void main(String[] args){
 		// ڑ̃NCAg̃Xg
		final LinkedList<XMLSocket> clientList = new LinkedList();
 		
 		// XMLSocketServer ̍쐬
 		XMLSocketServer socketServer = null;
 		try {
 			socketServer = new XMLSocketServer();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		socketServer.setEncoding("UTF-8");
 		
 		// T[o[̃Cxgnh̓o^
 		socketServer.addXMLSocketServerListener(new XMLSocketServerListener(){
 
 			// T[o[Jn
 			public void onOpen(boolean success) {
 				System.out.println("open:" + success);
 			}
 
 			// T[o[I
 			public void onClose() {
 				System.out.println("close:");
 			}
 
 			// G[
 			public void onError(Exception e) {
 				//e.printStackTrace();
 			}
 
 			// VNCAg̐ڑ
 			public void onNewClient(final XMLSocket socket) {
 				// NCAgXgɓo^
 				clientList.add(socket);
 				// NCAg̒ʂԍ
 				final int clientnumber = clientList.size();
 				
 				System.out.println("newclient:" + clientnumber);
 				
 				// NCAg̃Cxgnh̓o^
 				socket.addXMLSocketListener(new XMLSocketListener(){
 					//@ڑJn
 					public void onConnect(boolean success) {
 						System.out.println("flash:"+clientnumber+":connect:");
 					}
 					
 					// ڑI
 					public void onClose() {
 						System.out.println("flash:"+clientnumber+":close:");
 					}
 
 					// G[
 					public void onError(Exception e) {
 						//e.printStackTrace();
 					}
 
 					//@^CAEg
 					public void onTimeout() {
 						System.out.println("flash:"+clientnumber+":timeout");
 					}
 
 					// Flash ̃f[^M
 					public void onData(String data) {
 						System.out.println("flash:"+clientnumber+":data:"+data);
 						socket.send("<?xml version=\"1.0\" encoding=\"Shift_JIS\"?>"+
 									"<Message date=\"eXg\"/>");
 					}
 
 					// Flash Mf[^ XML DOM
 					public void onXML(Document doc) {
 					}
 				});
 			}
 		});
 		
 		// T[o[Jn
 		socketServer.open(SERVER_PORT);
 	}
 }
