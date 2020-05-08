 package pt.com.broker.client.messaging;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.caudexorigo.concurrent.CustomExecutors;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.client.AcceptRequest;
 import pt.com.broker.types.NetFault;
 
 /**
  * PendingAcceptRequestsManager deals with Accept requests made but still unanswered holding them and setting them as timeout if necessary. The timeout may be 500 milliseconds overdue.
  * 
  */
 
 public class PendingAcceptRequestsManager
 {
 	private static final Logger log = LoggerFactory.getLogger(PendingAcceptRequestsManager.class);
 	private static final ScheduledExecutorService shed_exec = CustomExecutors.newScheduledThreadPool(1, "Pendig Accept Janitor");
 
 	private static Map<String, AcceptRequest> requests = new HashMap<String, AcceptRequest>();
 
 	private static Map<Long, String> timeouts = new TreeMap<Long, String>(); // access protected by requests sync object
 
 	static
 	{
 		Runnable command = new Runnable()
 		{
 
 			@Override
 			public void run()
 			{
 				try
 				{
 					long currentTime = System.currentTimeMillis();
 					boolean deletedSomeEntry;
 					do
 					{
 						deletedSomeEntry = false;
 						AcceptRequest request = null;
 						synchronized (requests)
 						{
 							for (Map.Entry<Long, String> entry : timeouts.entrySet())
 							{
 								if (entry.getKey().longValue() <= currentTime)
 								{
 									request = requests.get(entry.getValue());
 									requests.remove(entry.getValue());
 									timeouts.remove(entry.getKey());
 
 									deletedSomeEntry = true;
 
 									log.warn("Accept Request with action id " + request.getActionId() + " timedout.");
 
 									break;
 								}
 							}
 						}
 						if (request != null)
 							onTimeout(request);
 					}
 					while (deletedSomeEntry);
 				}
 				catch (Throwable t)
 				{
 					log.error("Jannitor timeout thread", t);
 				}
 			}
 
 		};
 
 		shed_exec.scheduleWithFixedDelay(command, 2000, 500, TimeUnit.MILLISECONDS);
 	}
 
 	public static void addAcceptRequest(AcceptRequest request)
 	{
 		if (request.getActionId() == null)
 			return;
 
 		synchronized (requests)
 		{
 			if (requests.containsKey(request.getActionId()))
 				throw new IllegalArgumentException("Accept request wiht the same ActionId already exists");
 
 			requests.put(request.getActionId(), request);
 
 			timeouts.put(new Long(System.currentTimeMillis() + request.getTimeoutDelta()), request.getActionId());
 		}
 	}
 
 	public static boolean messageFailed(NetFault fault)
 	{
 		AcceptRequest request = null;
 		synchronized (requests)
 		{
 			request = requests.get(fault.getActionId());
 			if (request == null)
 				return false;
 
 			removeRequest(fault.getActionId());
 		}
 		request.getListner().messageFailed(fault);
 		return true;
 	}
 
	public static void removeRequest(String actionId)
 	{
 		synchronized (requests)
 		{
 			requests.remove(actionId);
 			for (Map.Entry<Long, String> entry : timeouts.entrySet())
 			{
 				if (entry.getValue().equals(actionId))
 				{
 					timeouts.remove(entry.getKey());
 					return;
 				}
 			}
 		}
 	}
 
 	public static void acceptedMessageReceived(String actionId)
 	{
 		AcceptRequest request = null;
 		synchronized (requests)
 		{
 			request = requests.get(actionId);
 			if (request == null)
 			{
 				log.error("Received unexpected action id: " + actionId);
 				return;
 			}
 			removeRequest(actionId);
 		}
 		request.getListner().messageAccepted(actionId);
 	}
 
 	private static void onTimeout(AcceptRequest request)
 	{
 		request.getListner().messageTimedout(request.getActionId());
 	}
 
 }
