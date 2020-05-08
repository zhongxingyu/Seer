 package pt.com.broker.monitorization.collectors;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import pt.com.broker.client.BrokerClient;
 import pt.com.broker.monitorization.Utils;
 import pt.com.broker.monitorization.configuration.ConfigurationInfo;
 import pt.com.broker.monitorization.configuration.ConfigurationInfo.AgentInfo;
 import pt.com.broker.types.NetPong;
 
 public class AgentStatusCollector
 {
 	private Runnable statusVerifier;
 
 	private List<AgentStatusListener> listeners = new ArrayList<AgentStatusListener>();
 	
 	public AgentStatusCollector() throws Throwable
 	{
 		
 	}
 
 	public void start() throws Throwable
 	{
 		statusVerifier = new Runnable()
 		{
 			public void run()
 			{
 				if( listeners.size() == 0)
 					return;
 				
 				List<AgentInfo> cloudAgents = ConfigurationInfo.getCloudAgents();
 
 				for (AgentInfo agent : cloudAgents)
 				{
 					BrokerClient bk = null;
 					NetPong checkStatus = null;
 					try
 					{
 						bk = new BrokerClient(agent.tcpInfo.getHostname(), agent.tcpInfo.getPort(), 0);
 						checkStatus = bk.checkStatus();
 					}
 					catch (Throwable t)
 					{
 					}
 					notifyListeners( agent.hostname, (checkStatus != null) ? AgentStatus.Ok : AgentStatus.Down);						
 					if(bk != null)
 						bk.close();
 				}
 			}
 		};
 
		Utils.schedule(statusVerifier, 30, 60, TimeUnit.SECONDS);
 	}
 	
 	private void notifyListeners(String agentName, AgentStatus status)
 	{
 		synchronized (listeners)
 		{
 			for(AgentStatusListener listener : listeners)
 				listener.onUpdate(agentName, status);
 		}
 	}
 	
 	public void addListener(AgentStatusListener listener)
 	{
 		synchronized (listeners)
 		{
 			listeners.add(listener);
 		}
 	}
 }
