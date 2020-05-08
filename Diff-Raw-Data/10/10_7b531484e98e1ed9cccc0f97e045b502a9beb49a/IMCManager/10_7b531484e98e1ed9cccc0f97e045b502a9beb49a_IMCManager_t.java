 package pt.lsts.accu.msg;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import pt.lsts.accu.state.Accu;
 import pt.lsts.accu.types.Sys;
 import pt.lsts.accu.util.MUtil;
 import pt.lsts.imc.IMCDefinition;
 import pt.lsts.imc.IMCMessage;
 import pt.lsts.imc.net.UDPTransport;
 import pt.lsts.neptus.messages.listener.MessageInfo;
 import pt.lsts.neptus.messages.listener.MessageListener;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 /**
  * Class that aggregates IMC messaging functions and delivers message to the subscribers
  * @author jqcorreia
  *
  */
 public class IMCManager implements MessageListener<MessageInfo, IMCMessage>
 {
 	public static final String TAG = "IMCManager";
 	
 	LinkedHashMap<Integer,List<IMCSubscriber>> subscribers = new LinkedHashMap<Integer,List<IMCSubscriber>>();
 	ArrayList<IMCSubscriber> subscribersToAll = new ArrayList<IMCSubscriber>();
 	
 	UDPTransport announceListener, comm;
 	
 	final static boolean LOG_DEBUG=false; // Message Logging flag
 	public boolean commActive = false;
 	int localId;
 	
 	public IMCManager()
 	{
 		// startComms(); // Commented out means you have to explicitly call startComms to initialize sockets
 		String localIp = MUtil.getLocalIpAddress();
 		int lastIpNumber;
 		if(localIp == null) 
 			lastIpNumber = 0;
 		else
 			lastIpNumber = Integer.valueOf(localIp.split("\\.")[3]);
 			
 		localId = 0x4100 | lastIpNumber;
 		Log.d(TAG,"System ID: "+localId);
 	}
 	
 	
     /**
      * Function that adds a subscriber class to the message system, registering for a given id
      * @param sub
      * @param mgid
      */
     public boolean addSubscriber(IMCSubscriber sub, String abbrevName)
     {
     	int mgid;
 		try {
 			mgid = IMCDefinition.getInstance().getMessageId(abbrevName);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 
     	if(!subscribers.containsKey(mgid))
     	{
     		List<IMCSubscriber> l = new LinkedList<IMCSubscriber>();
     		l.add(sub);
     		subscribers.put(mgid,l);
     	}
     	else
     	{
     		if(!subscribers.get(mgid).contains(sub)) // FIXME doesnt really work...
     		{
     			subscribers.get(mgid).add(sub);
     		}
     	}
     	return true;
     }
     /**
      * Batch method to add subscribers
      * @param sub IMCSubscriber
      * @param abbrevNameList Array of strings containing the names of messages to subscribe
      */
     public void addSubscriber(IMCSubscriber sub, String[] abbrevNameList)
     {
     	for(String abbrevName : abbrevNameList)
     	{
     		addSubscriber(sub,abbrevName);
     	}
     }
     public void addSubscriberToAllMessages(IMCSubscriber sub)
     {
     	if(!subscribersToAll.contains(sub))
     	{
     		subscribersToAll.add(sub);
     	}
     }
     
     /**
      * Function that unsubscribes every message sent to a component 
      * @param sub Subscriber component to remove
      */
     public void removeSubscriberToAll(IMCSubscriber sub)
     {
     	for(List<IMCSubscriber> l: subscribers.values())
     	{
     		if(l.contains(sub))
     			l.remove(sub);
     	}
     	if(subscribersToAll.contains(sub))
     		subscribersToAll.remove(sub);
     }
     
     /**
      * Method that delivers the message to the various subscribers.
      * Leaving the task of seeing which is the Active Sys to the various components for low level
      * flexibility.
      * @param message Message to be delivered
      */
 	public void processMessage(IMCMessage message)
 	{
 		try
 		{
 			if (LOG_DEBUG)
 				Log.v(TAG, message.toString()); 
 
 			int id = (Integer) message.getHeaderValue("mgid");
 
 			// First and foremost send the message to components registered to
 			// every message
 			for (IMCSubscriber s : subscribersToAll) {
 				s.onReceive(message);
 			}
 
 			// Then check for components listening to this specific message
 			if (subscribers.containsKey(id)) {
 				for (IMCSubscriber s : subscribers.get(id)) {
 					s.onReceive(message);
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			Log.e("ERROR","ERROR IN MESSAGE PROCESSING, IGNORING MESSAGE: "+e.getMessage());
 //			Log.e("ERROR",message.toString());
 			return;
 		}
 	}
 	
 	Handler handle = new Handler(){
 		@Override
 		public void handleMessage(Message msg) {
 		switch(msg.what){
 		     case 1:
 		            /*Refresh UI*/
 		            processMessage((IMCMessage)msg.obj);
 		            break;
 		   }
 		}
 	};
 	@Override
 	public void onMessage(MessageInfo info, IMCMessage message) {
 		handle.sendMessage(Message.obtain(handle, 1, message));
 	}
 	
 	public void killComms()
 	{
 		if(commActive)
 		{
 			Log.i("IMCManager", "Killing Comms");
 			announceListener.removeMessageListener(this);
 			comm.removeMessageListener(this);
 			announceListener.stop();
 			comm.stop();
 			announceListener = null;
 			comm = null;
 
 //			subscribers.clear();
 
 			commActive = false;
 		}
 	}
 	public void startComms()
 	{
 		if (!commActive) {
 			Log.i("IMCManager", "Starting Comms");
 			announceListener = new UDPTransport("224.0.75.69", 30100);
			announceListener.setImcId(localId);
 			comm = new UDPTransport(6001, 1);
			comm.setImcId(localId);
 			announceListener.addMessageListener(this);
 			comm.addMessageListener(this);
 
 			announceListener.setIsMessageInfoNeeded(false);
 			comm.setIsMessageInfoNeeded(false);
 			commActive = true;
 		}
 	}
 	
 	public UDPTransport getComm()
 	{
 		return comm;
 	}
 	public UDPTransport getListener()
 	{
 		return announceListener;
 	}
 	public int getLocalId()
 	{
 		return localId;
 	}
 	// Message helper functions
 	public void sendToActiveSys(String name, Object ... values)
 	{
 		sendToSys(Accu.getInstance().getActiveSys(), name, values);
 	}
 	public void sendToActiveSys(IMCMessage msg)
 	{
 		sendToSys(Accu.getInstance().getActiveSys(), msg);
 	}
 	public void sendToSys(Sys sys, String name, Object ... values)
 	{
 		try {
 			send(sys.getAddress(),sys.getPort(),
 					name, values);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	public void sendToSys(Sys sys, IMCMessage msg)
 	{
 		try {
 			send(sys.getAddress(),sys.getPort(),
 					msg);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void send(String address, int port, String name, Object ... values)
 	{
 		try {
 			IMCMessage msg = IMCDefinition.getInstance().create(name, values);
 			send(address, port, msg);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	/**
 	 * Method responsible for effectively giving the order to the UDPTransport to send the message
 	 */
 	public void send(String address, int port, IMCMessage msg)
 	{
 		try {
 			//FIXME Fill the header of the messages here
 			fillHeader(msg);
 			comm.sendMessage(address,port,msg);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	private void fillHeader(IMCMessage msg)
 	{
 		msg.getHeader().setValue("src", localId);
 		msg.getHeader().setValue("timestamp", System.currentTimeMillis() / 1000);
 	}
 	
 	public void printUsedTypes()
 	{
 		for(int i: subscribers.keySet())
 		{
 			Log.i(TAG,i+"");
 		}
 	}
 }
