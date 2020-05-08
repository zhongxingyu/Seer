 package Main;
 
 import java.awt.Color;
 import java.io.IOException;
 import java.net.Socket;
 import javax.swing.SwingUtilities;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import Events.CompleteSendEventListener;
 import Events.StartStop.ICompleteStartEventListener;
 import Events.StartStop.ICompleteStopEventListener;
 import Events.StartStop.CompleteStartEventListener;
 import Events.StartStop.CompleteStopEventListener;
 import Events.ICompleteSendEventListener;
 import FileWriters.WiFiWriter;
 import GUI.Terminal;
 import IOStream.GetStreamIn;
 import IOStream.SendStreamOut;
 import Socket.Reconnect;
 
 public class DataController extends Thread 
 {
 	private Terminal terminal;
 	private Socket socket;
 	private SendStreamOut streamOut;
 	private String streamInString;
 	private GetStreamIn getStreamIn;
 	private int available = 0;
 	private SimpleAttributeSet blue = new SimpleAttributeSet();
 	private SimpleAttributeSet green = new SimpleAttributeSet();
 	private int timeout = 5000;
 	public boolean boolStream = true;
 	public long lastReadTime = System.currentTimeMillis();
 	public String ip;
 	public int port;
 	public WiFiWriter wiFiWriter;
 	public static javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
 	public String pongString = "Pong"; // need to dynamically change if connected to server or payload!!!
 	public String deviceName;
 	public void Initilize(Socket socket, String ip, int port, String deviceName)
 	{
 		this.socket = socket;
 		this.ip = ip;
 		this.port = port;
 		this.deviceName = deviceName;
 		
 		StyleConstants.setForeground(blue, Color.BLUE);
 		StyleConstants.setForeground(green, new Color(0,64,0));
 		
 		terminal = new Terminal(deviceName);
 		wiFiWriter = new WiFiWriter();
 		getStreamIn = new GetStreamIn();
 		streamOut = new SendStreamOut();
 		streamOut.attachSocket(socket);
 		
 		TextSendController();
 		Start();
 		Stop();
 		this.start();
 	}
 	
 	public void run() 
 	{
 		while(true)
 		{
 			if(boolStream)
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
 					  lastReadTime = System.currentTimeMillis();
 					  streamInString = getStreamIn.StreamIn(socket);
 					  
 					  if(!streamInString.equals("Ping"))
 					  {
 						  updateText(streamInString , blue);
 					  }
 					  else
 					  {
 						  Pong();
 					  }
 				}
 				
 				if(!isConnectionAlive())
 				{
 					updateText("Lost Connection...\n" , green);
 					terminal.btnSend.setEnabled(false);
 					
 					Reconnect reconnect = new Reconnect(this,ip,port);
 					socket = reconnect.socketLoop();
 					
 					if(socket.isBound())
 					{
 						
 						streamOut.attachSocket(socket);
 						terminal.btnSend.setEnabled(true);
 						lastReadTime = System.currentTimeMillis();			
 					}
 				}
 				
 			}
 			else
 			{
 				 getStreamIn.StreamIn(socket);
 				 lastReadTime = System.currentTimeMillis();
 			}
			try { Thread.sleep(10); } catch(InterruptedException e) { /* we tried */}
 		}
 	}
 	
 	public void Pong()
 	{
 		streamOut.streamOut(pongString); // Ping outgoing need to change if connected directly or separately!!!!
 	
 	}
 	public void updateText(final String _streamInString, final SimpleAttributeSet type) {
 		  SwingUtilities.invokeLater(new Runnable() {
 		    public void run() {
 		    	 terminal.updateText(_streamInString,type);
 		    	 wiFiWriter.recieveText(_streamInString);
 		    }
 		  });
 		}
 	
 	public  void TextSendController()
 	{
 		addCompleteSendEventListener(new CompleteSendEventListener(this, streamOut));
 	}
 	
 	public void Start()
 	{
 		addCompleteStartEventListener(new CompleteStartEventListener(this));
 	}
 	
 	public void Stop()
 	{
 		addCompleteStopEventListener(new CompleteStopEventListener(this));
 	}
 	public void SendButtonDisable()
 	{
 		terminal.btnSend.setEnabled(false);
 	}
 	
 	public void SendButtonEnable()
 	{
 		terminal.btnSend.setEnabled(true);
 	}
 	
 	public boolean isConnectionAlive()
 	{
 		return (System.currentTimeMillis() - lastReadTime) < timeout;
 	}
 
 	public static void addCompleteSendEventListener (ICompleteSendEventListener completeSendEventListener)
 	{
 		listenerList.add(ICompleteSendEventListener.class, completeSendEventListener);
 	}
 	public static void removeCompleteSendEventListener (ICompleteSendEventListener completeSendEventListener)
 	{
 		listenerList.remove(ICompleteSendEventListener.class, completeSendEventListener);
 	}
 	public static void addCompleteStartEventListener (ICompleteStartEventListener completeStartEventListener)
 	{
 		listenerList.add(ICompleteStartEventListener.class, completeStartEventListener);
 	}
 	public static void addCompleteStopEventListener (ICompleteStopEventListener completeStopEventListener)
 	{
 		listenerList.add(ICompleteStopEventListener.class, completeStopEventListener);
 	}
 
 }
