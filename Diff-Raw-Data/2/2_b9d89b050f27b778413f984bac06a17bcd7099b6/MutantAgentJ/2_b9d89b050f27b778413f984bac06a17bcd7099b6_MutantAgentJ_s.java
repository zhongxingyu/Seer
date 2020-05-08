 package com.razie.mutant.agent;
 
 
 import scala.razie.FullAgent;
 
 import com.razie.agent.network.Devices;
 import com.razie.agent.pres.AgentNetworkService;
 import com.razie.agent.upnp.AgentUpnpService;
 import com.razie.dist.db.AgentDbService;
 import com.razie.pub.agent.Agent;
 import com.razie.pub.base.NoStaticSafe;
 import com.razie.pub.comms.AgentCloud;
 import com.razie.pub.comms.AgentHandle;
 import com.razie.pub.webui.MutantPresentation;
 
 /**
  * this is the full-blown mutant agent, with its specific services etc
  * 
  * @author razvanc
  */
 @NoStaticSafe
 public class MutantAgentJ extends FullAgent {
 
 	public MutantAgentJ(AgentHandle myHandle, AgentCloud homeGroup) {
 		super(myHandle, homeGroup, null);
 	}
 
 	/**
 	 * get the current instance (can be many per JVM)
 	 */
 	public static MutantAgentJ getInstance() {
 	    Agent x = Agent.instance();
 	    
 		if (x == null) {
 			throw new IllegalStateException("agent NOT intiialized");
 		}
 		
 		return (MutantAgentJ)x;
 	}
 
 	/**
 	 * called when main() starts up but before onStartup(). Initialize all
 	 * services from the configuration file
 	 */
 	public synchronized MutantAgentJ onInit() {
 	   super.onInit();
       getThreadContext().enter();
 
 		// initialize rest in separate thread to speed up startup response time
 		new Thread(new Runnable() {
 			public void run() {
       getThreadContext().enter();
 	   register(new AgentDbService());
 			}
 		}).start();
 		
 		return this;
 	}
 }
