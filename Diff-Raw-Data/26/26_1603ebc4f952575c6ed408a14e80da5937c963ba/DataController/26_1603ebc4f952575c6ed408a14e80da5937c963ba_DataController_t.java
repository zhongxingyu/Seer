 package Main;
 
 import java.awt.Color;
 import java.net.Socket;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import Events.CompleteSendEventListener;
 import Events.ICompleteSendEventListener;
 import IOStream.GetStreamIn;
 import IOStream.HandShake;
 import IOStream.RXTXhandeler;
 import IOStream.SendStreamOut;
 
 
 public class DataController extends Thread 
 {
 	private Socket socket;
 	private SendStreamOut streamOut;
 	private GetStreamIn getStreamIn;
 	private SimpleAttributeSet blue = new SimpleAttributeSet();
 	private SimpleAttributeSet green = new SimpleAttributeSet();
 	private int timeout = 5000;
 	private boolean isHandShakeComplete = false;
 	private long lastReadTime = System.currentTimeMillis();
 	public static javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
 	private String deviceName;
 	private String type;
 	
 	public void Initilize(Socket socket, String deviceName, String type)
 	{
 		this.socket = socket;
 		this.deviceName = deviceName;
 		this.type = type;
 		
 		StyleConstants.setForeground(blue, Color.BLUE);
 		StyleConstants.setForeground(green, new Color(0,64,0));
 
 		getStreamIn = new GetStreamIn();
 		streamOut = new SendStreamOut();
 		streamOut.attachSocket(socket);
 		
 		TextSendController();
 		this.start();
 	}
 	
 	public void run() 
 	{
 		HandShake handShake = new HandShake(streamOut, getStreamIn, socket, deviceName);
 		
 		while(!isHandShakeComplete)
 		{
 			isHandShakeComplete = handShake.isHandShakeComplete();
 		}
 		
 		RXTXhandeler rxtxhandeler = new RXTXhandeler(streamOut, getStreamIn, socket, deviceName);
 		
 		if(type.equals("Name and Time"))
 		{
 			while(isHandShakeComplete)
 			{
 				rxtxhandeler.NameTimeRXTX();
				try { Thread.sleep(10); } catch(InterruptedException e) { /* we tried */}
 			}
 		}
 		else if (type.equals("RockSim"))
 		{
 			rxtxhandeler.getRockSimData();
 			while(isHandShakeComplete)
 			{
 				rxtxhandeler.RockSimRXTX();
				try { Thread.sleep(10); } catch(InterruptedException e) { /* we tried */}
 			}
 		}
 		else
 		{
 			System.out.println("Error"); // get better internal error handling
 		}
 	}
 	
 	public  void TextSendController()
 	{
 		addCompleteSendEventListener(new CompleteSendEventListener(streamOut));
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
 	
 
 }
