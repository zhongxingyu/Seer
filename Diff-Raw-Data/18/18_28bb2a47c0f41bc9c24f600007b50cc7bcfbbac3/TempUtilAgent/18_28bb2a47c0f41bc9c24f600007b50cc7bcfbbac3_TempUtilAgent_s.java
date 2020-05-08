/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
  */
 package com.razie.pub.agent;
 
 import com.razie.pub.base.ThreadContext;
 import com.razie.pub.comms.AgentCloud;
 import com.razie.pub.comms.AgentHandle;
 import com.razie.pub.comms.LightAuth;
 import com.razie.pub.http.LightCmdGET;
 import com.razie.pub.http.LightServer;
 
 /** netbeans can't share unit classes forcing me to move testing code here - this is temp until i figure a workaround
  *
  * @author razvanc
  */
 public class TempUtilAgent {
 	public static AgentHandle me = new AgentHandle("testagent1", "localhost",
 			"127.0.0.1", "4446", "http://localhost:4446");
 	public static AgentHandle other = new AgentHandle("testagent2", "localhost",
 			"127.0.0.1", "4447", "http://localhost:4447");
 
 
    /** start an agent and mount basic services */
    public static Agent startAgent(AgentHandle h, AgentCloud g) {
       Agent agent = new Agent(h, g);
       agent.getMainContext().enter();
       agent.onInit();
 
       LightAuth.init(new LightAuth("lightsoa"));
 
       // we need a server with cmdget to accept bindings for services. You
       // should do this for any
       // agent
       LightServer server = new LightServer(Integer.parseInt(h.port), agent.getMainContext());
       server.registerCmdListener(new LightCmdGET());
       agent.register(new AgentHttpService(agent, server));
 
       agent.onStartup();
       ThreadContext.exit();
       return agent;
    }
 }
