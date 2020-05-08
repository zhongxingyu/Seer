 /**
  * Appia: Group communication and protocol composition framework library
  * Copyright 2006 University of Lisbon
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  *
  * Initial developer(s): Alexandre Pinto and Hugo Miranda.
  * Contributor(s): See Appia web page for a list of contributors.
  */
  
 package org.continuent.appia.protocols.group.heal;
 
 
 import java.io.PrintStream;
 import java.net.InetSocketAddress;
 import java.util.HashSet;
 
 import org.continuent.appia.core.*;
 import org.continuent.appia.core.events.SendableEvent;
 import org.continuent.appia.core.events.channel.Debug;
 import org.continuent.appia.core.events.channel.PeriodicTimer;
 import org.continuent.appia.core.message.Message;
 import org.continuent.appia.protocols.group.Group;
 import org.continuent.appia.protocols.group.LocalState;
 import org.continuent.appia.protocols.group.ViewID;
 import org.continuent.appia.protocols.group.ViewState;
 import org.continuent.appia.protocols.group.bottom.OtherViews;
 import org.continuent.appia.protocols.group.events.GroupInit;
 import org.continuent.appia.protocols.group.intra.View;
 import org.continuent.appia.xml.interfaces.InitializableSession;
 import org.continuent.appia.xml.utils.SessionProperties;
 
 
 /**
  * @author Alexandre Pinto
  * @version 1.0
  */
 public class HealSession extends Session implements InitializableSession {
   
   private long gossip_time;
   private long hello_min_time;
   
   public HealSession(Layer layer, long gossip_time, long hello_min_time) {
     super(layer);
     this.gossip_time=gossip_time;
     this.hello_min_time=hello_min_time;
   }
    
   /**
    * Initializes the session using the parameters given in the XML configuration.
    * Possible parameters:
    * <ul>
    * <li><b>GOSSIP_TIME</b> time between gossip messages. (in milliseconds)
    * <li><b>HELLO_MIN_TIME</b> minimum time between Hello messages. (in milliseconds)
    * <li><b>DEBUGON</b> boolean indicating whether debug messages should be printed or not.
    * </ul>
    * 
    * @param params The parameters given in the XML configuration.
    */
   public void init(SessionProperties params) {
     if (params.containsKey("GOSSIP_TIME"))
       gossip_time=params.getLong("GOSSIP_TIME");
     if (params.containsKey("HELLO_MIN_TIME"))
       hello_min_time=params.getLong("HELLO_MIN_TIME");
     if (params.containsKey("DEBUGON"))
       debugOn=true;
   }
   
   public void handle(Event event) {
     
     // GossipOutEvent
     if (event instanceof GossipOutEvent) {
       handleGossipOrHello((GossipOutEvent)event); return;
     }
     // HelloEvent
     if (event instanceof HelloEvent) {
     	handleGossipOrHello((HelloEvent)event); return;
     }
     // ConcurrentViewEvent
     if (event instanceof ConcurrentViewEvent) {
     	handleConcurrentView((ConcurrentViewEvent)event); return;
     }
     // View
     if (event instanceof View) {
       handleView((View)event); return;
     }
     // PeriodicTimer
     if (event instanceof PeriodicTimer) {
       handleTimer((PeriodicTimer)event); return;
     }
     // OtherViews
     if (event instanceof OtherViews) {
       handleOtherViews((OtherViews)event); return;
     }
     // GroupInit
     if (event instanceof GroupInit) {
       handleGroupInit((GroupInit)event); return;
     }
     // Debug
     if (event instanceof Debug) {
       Debug ev=(Debug)event;
       
       if (ev.getQualifierMode() == EventQualifier.ON) {
         if (ev.getOutput() instanceof PrintStream)
           debug=(PrintStream)ev.getOutput();
         else
           debug=new PrintStream(ev.getOutput());
         debugOn=true;
       } else {
         if (ev.getQualifierMode() == EventQualifier.OFF)
           debugOn=false;
       }
       
       try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
       return;
     }
     
     debug("Unwanted event (\""+event.getClass().getName()+"\") received. Continued...");
     try { event.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
   }
   
 	private ViewState vs;
   private LocalState ls;
   private long last_gossip=0;
   private long last_hello=0;
   private Object multicast_addr=null;
   private Object[] other_addrs=null;
   private HashSet detectedViews=new HashSet();
   
   private void handleGossipOrHello(SendableEvent ev) {
     
 	  if(vs == null){
 		  debug("Received gossip or hello but didn't received first view.");
 		  return;
 	  }
     
     Message omsg=ev.getMessage();
     Group remote_group=Group.pop(omsg);
     ViewID remote_id=ViewID.pop(omsg);
     
     if (!vs.group.equals(remote_group)) {
       debug("Received gossip or hello of other group. Ignoring it.");
       return;
     }
     
     if (vs.id.equals(remote_id)) {
       debug("Received gossip or hello from my current view. Ignoring it.");
       return;
     }
     
     int i;
     for (i=0 ; i < vs.previous.length ; i++) {
       if (vs.previous[i].equals(remote_id)) {
         debug("Received gossip or hello from my old view. Ignoring it.");
         return;
       }
     }
 
     if (vs.getRankByAddress((InetSocketAddress)ev.source) >= 0) {
       debug("Received gossip of other alive member of my group (possibly an old one). Ignoring it.");
       return;
     }
 
     if (detectedViews.contains(remote_id)) {
     	debug("Received gossip or hello from an already detected concurrent view. Ignoring it.");
     	return;
     }
 
     if (debugFull)
       debug("Detected valid concurrent view (id="+remote_id.toString()+" source="+((InetSocketAddress)ev.source).toString()+"). Sending warning.");
     
     try {
     	ConcurrentViewEvent cve=new ConcurrentViewEvent(ev.getChannel(),Direction.DOWN,this,vs.group,vs.id);
     	if (ls.am_coord) {
     		cve.id=remote_id;
     		cve.addr=ev.source;
     	} else {
     		cve.getMessage().pushObject(ev.source);
     		ViewID.push(remote_id, cve.getMessage());
     		cve.dest=new int [] {ls.coord};
     	}
     	cve.go();
     	
     	detectedViews.add(remote_id);
     } catch (AppiaEventException ex) {
     	if (debugFull)
     		ex.printStackTrace();
     	debug("Unable to send ConcurrentViewEvent");
     }
   }
   
   private void handleConcurrentView(ConcurrentViewEvent event) {
   	event.id=ViewID.pop(event.getMessage());
   	event.addr=event.getMessage().popObject();
   	
   	if (detectedViews.contains(event.id)) {
   		debug("Received ConcurrentViewEvent of an already detected view. Ignoring it.");
   		return;
   	}
   	
   	if (!ls.am_coord) {
       debug("Received ConcurrentViewEvent but i am not the coordinator.");
       detectedViews.add(event.id);
       return;
     }
   	
     if (debugFull)
       debug("Received valid concurrent view detection (id="+event.id+" source="+event.addr+"). Resending it.");
     
   	try {
   		event.setDir(Direction.invert(event.getDir()));
   		event.setSource(this);
   		event.init();
   		event.go();
   		
   		detectedViews.add(event.id);
   	} catch (AppiaEventException ex) {
   		ex.printStackTrace();
   		debug("Impossible to reverse and resend a received ConcurrentViewEvent");
   	}
   }
   
   private void handleOtherViews(OtherViews ev) {
     if (ev.state != OtherViews.NOTIFY)
       return;
     
     if (!ls.am_coord) {
       debug("Received other view warning but i am not the coordinator");
       return;
     }
     
     long now=ev.getChannel().getTimeProvider().currentTimeMillis();
     if (now-last_hello > hello_min_time) {
       last_hello=now;
       
       sendHello(ev.getChannel(),null);
       if (debugFull)
         debug("Sent Hello due to OtherViews ("+ev.other_addr.toString()+")");
     }
   }
   
   private void handleGroupInit(GroupInit ev) {
     multicast_addr=ev.getIPmulticast();
     if (ev.getBaseVS() != null)
    	other_addrs=ev.getBaseVS().addresses.clone();
     try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
   }
   
   private void handleView(View ev) {
     vs=ev.vs;
     ls=ev.ls;
     
     try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
     
     if (ls.am_coord) {
       do_gossip(ev.getChannel());
       
       if (do_gossip)
         sendGossip(ev.getChannel());
       
       sendHello(ev.getChannel(),null);      
     } else {
       do_gossip=false;
     }
   }
   
   private void handleTimer(PeriodicTimer ev) {
     try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
         
     if (ev.getQualifierMode() != EventQualifier.NOTIFY)
       return;
     
     if (vs == null)
       return;
     
     if (ls.am_coord) {
     	long now=ev.getChannel().getTimeProvider().currentTimeMillis();
 
     	if (now-last_gossip > gossip_time) {
     		last_gossip=now;
 
     		if (do_gossip)
     			sendGossip(ev.getChannel());
 
     		sendHello(ev.getChannel(),null);
     	}
     }
   }
 
   private void sendHello(Channel channel, Object addr) {
   	if (addr == null) {
   		if (multicast_addr != null){
   			if(vs.view.length > 1)
   				return; // Hello unecessary because normal group messages will be detected by concurrent views
   			addr=multicast_addr;
   		} else {
   			if(other_addrs != null){
   				for(int i=0 ; i < other_addrs.length ; i++){
   					if(vs.getRankByAddress((InetSocketAddress)other_addrs[i]) == -1){
   						if(debugFull)
   							debug("Sending hello to "+other_addrs[i]);
   						sendHello(channel, other_addrs[i]);
   					}
   				}
   			} 
   			return;
   		}
   	}
     
     try {
       HelloEvent ev=new HelloEvent(channel,Direction.DOWN,this);
       
       Message omsg=ev.getMessage();
       ViewID.push(vs.id,omsg);
       Group.push(vs.group,omsg);
       
       ev.dest=addr;
       ev.go();
     } catch (AppiaEventException ex) {
       ex.printStackTrace();
       debug("Impossible to send HelloEvent");
     }
   }
 
   private void sendGossip(Channel channel) {
     if (debugFull)
       debug("Sending Gossip to Server");
 
     try {
       GossipOutEvent ev=new GossipOutEvent(channel,Direction.DOWN,this);
       
       Message omsg=ev.getMessage();
       ViewID.push(vs.id,omsg);
       Group.push(vs.group,omsg);
       
       ev.go();
     } catch (AppiaEventException ex) {
       ex.printStackTrace();
       debug("Impossible to send GossipOutEvent");
     }
   }
   
   private boolean do_gossip=false;
   
   private void do_gossip(Channel channel) {
     try {
       ChannelCursor cursor=channel.getCursor();
       
       for (cursor.bottom() ; cursor.isPositioned() ; cursor.up()) {
         if (cursor.getLayer() instanceof GossipOutLayer) {
           do_gossip=true;
           return;
         }
       }
       
       do_gossip=false;
     } catch (AppiaCursorException ex) {
       ex.printStackTrace();
       do_gossip=true;
     }
   }
   
   // DEBUG
   public static final boolean debugFull=false;
   
   private boolean debugOn=false;
   private PrintStream debug=System.out;
   
   private void debug(String s) {
     if ((debug != null) && (debugFull || debugOn))
       debug.println("appia:group:HealSession: "+s);
   }
 }
