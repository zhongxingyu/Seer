 package SocketHandelers;
 
 import java.io.IOException;
 import java.net.Socket;
 
 import Events.CompletePayloadTXEvent;
 import Events.ICompletePayloadTXEventListener;
 import IOStream.GetStreamIn;
 import IOStream.SendStreamOut;
 import Main.Controller;
 
 public class PayloadDataController extends Thread
 {
 	private Socket socket;
 	private int available = 0;
 	private long pingLastReadTime = System.currentTimeMillis();
 	private long lastPingTime = System.currentTimeMillis();
 	private SendStreamOut streamOut;
 	private String streamInString;
 	private GetStreamIn getStreamIn;
	private long timeout = 10000;
 	private Controller controller;
 	private String deviceName;
 	private boolean payloadConnected = true;
 	
 	public PayloadDataController(Socket socket, Controller controller, String deviceName)
 	{
 		this.socket = socket;
 		this.deviceName = deviceName;
 		this.controller = controller;
 		getStreamIn = new GetStreamIn();
 		streamOut = new SendStreamOut();
 		streamOut.attachSocket(socket);
 		
 		this.start();
		//streamOut.streamOut("@"); // complete handshake
 	}
 	
 	public void run() 
 	{
 		while(payloadConnected)
 		{
 			try 
 			{
 				available = socket.getInputStream().available();
 			} 
 			catch (IOException e) 
 			{
 				e.printStackTrace();
 			}
 			
 			if(available > 0)
 			{
 				  streamInString = getStreamIn.StreamIn(socket);
				  System.out.println(streamInString);
				  if(!streamInString.equals("Pong"))  // Pong incoming
 				  {
 					  pingLastReadTime = System.currentTimeMillis();
 					  
 					  if(controller.terminalDataList != null)
 					  {
 						  for(int j = 0; j < controller.terminalDataList.size(); j++)
 						  {
 							  if(controller.terminalDataList.get(j).payloadDeviceName.equals(deviceName))
 							  {
 								  CompletePayloadTXEvent complete = new CompletePayloadTXEvent(this,j,streamInString); // 0 is the first terminal need to assign this!!! create list and loop through?
 									Object[] listeners = Controller.listenerList.getListenerList(); 
 							   		for (int i=0; i<listeners.length; i+=2) 
 							   		{
 							             if (listeners[i]==ICompletePayloadTXEventListener.class)
 							             {
 							                 ((ICompletePayloadTXEventListener)listeners[i+1]).CompleteTXEventHandler(complete);
 							             }
 							        } 
 							  }
 				   		
						  }
 					  }
 				  }
				  else
				  {
					  pingLastReadTime = System.currentTimeMillis();
				  }	   
 			}
 			Ping();
 			if(Disconnected())
 			{
 				System.out.print(deviceName);
			    System.out.println(" Disconnected!!!");
		    	payloadConnected = false;
 			}
 			
 			try { Thread.sleep(10); } catch(InterruptedException e) { /* we tried */}
 		}
 		
 		
 	}
 	
 	public void StreamOut(String sendText)
 	{
 		streamOut.streamOut(sendText);
 		//streamOut.streamOut("!"); /// find why this needs to be here
 	}
 	
 	public boolean Disconnected()
 	{
 		return (System.currentTimeMillis() - pingLastReadTime) > timeout;
 	}
 		
 	public void Ping()
 	{
 		if((System.currentTimeMillis() - pingLastReadTime) > 3000 && (System.currentTimeMillis() - lastPingTime) > 3000) //// Fix this
 		{
 			streamOut.streamOut("#"); // Ping outgoing
 			lastPingTime = System.currentTimeMillis();
 		}
 	
 	}
 	
 
 }
