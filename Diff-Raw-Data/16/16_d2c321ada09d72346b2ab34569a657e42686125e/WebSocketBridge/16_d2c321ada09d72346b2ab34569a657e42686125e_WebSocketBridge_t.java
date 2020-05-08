 package com.napthats.websocket;
 
 import java.io.IOException;
 import org.eclipse.jetty.websocket.WebSocket;
 
 import com.napthats.websocket.SpecialCommandSet.Func;
 import com.napthats.websocket.SpecialCommandSet.SpecialCommand;
 import com.napthats.websocket.SpecialCommandSet.Status;
 
 /**
  * WebSocket bridge for each client
  * @author napthats
  *
  */
 final class WebSocketBridge implements WebSocket.OnTextMessage {
     private Connection itsJsConnection = null;
     private ForwardingSocket itsForwardingSocket = null;
     private final SpecialCommandSet itsSpecialCommandSet;
     
     public WebSocketBridge(SpecialCommandSet scSet) {
     	super();
     	itsSpecialCommandSet = scSet;
     }
     
     @Override
     public void onOpen(Connection con) {
         itsJsConnection = con;
     }
 
     @Override
     public void onClose(int arg0, String arg1) {
     	disconnectServer();
     	disconnectClient();
     }
     
     //handling messages from a client
     @Override
     public void onMessage(String msg) {
     	//connected to server allready
     	if (itsForwardingSocket != null && !itsForwardingSocket.isClosed()) {
     		SpecialCommand sc = itsSpecialCommandSet.findSpecialCommand(Status.ACTIVE, msg);
        		if (sc == null) itsForwardingSocket.sendToServer(msg);
        		else if (sc.getInvokeMethod() == Func.DISCONNECT) disconnectServer();
     	}
     	//not connected
     	else {
     		itsForwardingSocket = null;
     		SpecialCommand sc = itsSpecialCommandSet.findSpecialCommand(Status.NONACTIVE, msg);
     		if (sc == null) return;
     		else if (sc.getInvokeMethod() == Func.CONNECT) connectServer(sc.getOptionList().get(0), Integer.parseInt(sc.getOptionList().get(1)));
     	}
     }
     
     private void disconnectServer() {
    	if (itsForwardingSocket != null) itsForwardingSocket.close();
     	itsForwardingSocket = null;    	
     }
 
     private void disconnectClient() {
    	if (itsJsConnection != null) itsJsConnection.disconnect();
     	itsJsConnection = null;  	
     }    
     
 	private void connectServer(String host, int port) {
 		itsForwardingSocket = new ForwardingSocket(host, port, new ForwardingSocket.RecvFromServer() {
 			@Override
 			public void recvFromServer(String msg) throws IOException {
 				send(msg);
 			}
 		});
 	}
 
     private void send(String msg) throws IOException {
    	itsJsConnection.sendMessage(msg);
     }
 }
