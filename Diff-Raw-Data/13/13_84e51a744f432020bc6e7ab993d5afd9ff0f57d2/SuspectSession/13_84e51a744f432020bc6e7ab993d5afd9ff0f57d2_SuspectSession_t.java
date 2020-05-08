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
  * Initial developer(s): Alexandre Pinto and Hugo Miranda and Nuno Carvalho.
  * Contributor(s): See Appia web page for a list of contributors.
  */
  
 package org.continuent.appia.protocols.group.suspect;
 
 
 
 import java.io.PrintStream;
 import java.net.InetSocketAddress;
 import java.util.Arrays;
 
 import javax.management.AttributeChangeNotification;
 import javax.management.Notification;
 
 import org.continuent.appia.core.*;
import org.continuent.appia.core.events.AppiaMulticast;
 import org.continuent.appia.core.events.channel.Debug;
 import org.continuent.appia.core.events.channel.EchoEvent;
 import org.continuent.appia.management.ManagedSession;
 import org.continuent.appia.management.AbstractSensorSession;
 import org.continuent.appia.management.ManagedSessionEvent;
 import org.continuent.appia.protocols.common.FIFOUndeliveredEvent;
 import org.continuent.appia.protocols.group.ArrayOptimized;
 import org.continuent.appia.protocols.group.LocalState;
 import org.continuent.appia.protocols.group.ViewState;
 import org.continuent.appia.protocols.group.events.GroupSendableEvent;
 import org.continuent.appia.protocols.group.events.Send;
 import org.continuent.appia.protocols.group.intra.View;
 import org.continuent.appia.protocols.tcpcomplete.TcpUndeliveredEvent;
 import org.continuent.appia.xml.interfaces.InitializableSession;
 import org.continuent.appia.xml.utils.SessionProperties;
 
 
 
 /** The <I>Appia</I> failure detector.
  * @see org.continuent.appia.protocols.group.suspect.SuspectLayer
  * @author Alexandre Pinto
  */
 public class SuspectSession extends AbstractSensorSession implements InitializableSession, ManagedSession {
     
   /** Default duration of a round.
    */
   public static final long DEFAULT_SUSPECT_SWEEP=3000; //in milliseconds
   /** Default time to suspect a member.
    * <br>
    * This value is converted in number of rounds and added 2 for transmission safety.
    */
   public static final long DEFAULT_SUSPECT_TIME=5000; //in milliseconds
 
   /** Creates a new Suspect session.
    */  
   public SuspectSession(Layer layer) {
     super(layer);
   }
   
   /**
    * Initializes the session using the parameters given in the XML configuration.
    * Possible parameters:
    * <ul>
    * <li><b>suspect_sweep</b> duration of a round in milliseconds.
    * <li><b>suspect_time</b> time to suspect a member, in milliseconds. 
    * This value is converted in number of rounds and added 2 for transmission safety.
    * </ul>
    * 
    * @param params The parameters given in the XML configuration.
    * @see org.continuent.appia.xml.interfaces.InitializableSession#init(SessionProperties)
    */
   public void init(SessionProperties params) {
     if (params.containsKey("suspect_sweep"))
       suspect_sweep=params.getLong("suspect_sweep");
     if (params.containsKey("suspect_time"))
       rounds_idle=(params.getLong("suspect_time")/suspect_sweep)+2;
   }
 
   public void setParameter(SessionProperties params, Channel channel) {
 	  Notification notif = null;
 	  if (params.containsKey("suspect_sweep")){
 		  Long old_value = new Long(suspect_sweep);
 		  suspect_sweep=params.getLong("suspect_sweep");
 		  notif = new AttributeChangeNotification(this,1,channel.getTimeProvider().currentTimeMillis(),"Suspect sweep Changed",
 				  "suspect_sweep",Long.class.getName(),old_value,new Long(suspect_sweep));
 		  System.out.println("SWEEP changed to "+suspect_sweep);
 	  }
 	  if (params.containsKey("suspect_time")){
 		  Long old_value = new Long(rounds_idle);
 		  rounds_idle=(params.getLong("suspect_time")/suspect_sweep)+2;
 		  notif = new AttributeChangeNotification(this,1,channel.getTimeProvider().currentTimeMillis(),"Rounds idle Changed",
 				  "rounds_idle",Long.class.getName(),old_value,new Long(rounds_idle));
 	  }
 	  if(notif != null)
 		  notifySensorListeners(notif);
   }
   
   /** Event handler.
    */  
   public void handle(Event event) {
     
     // Suspect
     if (event instanceof Suspect) {
       handleSuspect((Suspect)event); return;
       // GroupSendableEvent
     } else if (event instanceof GroupSendableEvent) {
       handleGroupSendableEvent((GroupSendableEvent)event); return;
     // SuspectTimer
     } else if (event instanceof SuspectTimer) {
       handleSuspectTimer((SuspectTimer)event); return;
     // View
     } else if (event instanceof View) {
       handleView((View)event); return;
     // FIFOUndeliveredEvent
     } else if (event instanceof FIFOUndeliveredEvent) {
       handleFIFOUndeliveredEvent((FIFOUndeliveredEvent)event); return;
     // TcpUndeliveredEvent
     } else if (event instanceof TcpUndeliveredEvent) {
       handleTcpUndeliveredEvent((TcpUndeliveredEvent)event); return;
     } else if (event instanceof ManagedSessionEvent) {
     		handleManagedSessionEvent((ManagedSessionEvent)event); return;
     // Debug
     } else if (event instanceof Debug) {
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
   
   private void handleManagedSessionEvent(ManagedSessionEvent event) {
       setParameter(event.getProperties(), event.getChannel());
       try {
 		event.go();
 	} catch (AppiaEventException e) {
 		e.printStackTrace();
 	}
 	
 }
 
 private ViewState vs;
   private LocalState ls;
   
   private long suspect_sweep=DEFAULT_SUSPECT_SWEEP;
   private long rounds_idle=(DEFAULT_SUSPECT_TIME/DEFAULT_SUSPECT_SWEEP)+2;
   private long round=0;
   private long[] last_recv=new long[0];
   
   private void handleView(View ev) {
     vs=ev.vs;
     ls=ev.ls;
     
     try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
     
     if (round == 0) {
       try {
         SuspectTimer periodic=new SuspectTimer("Suspect Timer",suspect_sweep,ev.getChannel(),this);
         periodic.go();
       } catch (AppiaException ex) {
         ex.printStackTrace();
         System.err.println("appia:group:SuspectSession: impossible to set SuspectTimer, SuspectSession will be idle");
       }
     }
     
     if (vs.view.length != last_recv.length) {
       last_recv=new long[vs.view.length];
     }
     round=1;
     Arrays.fill(last_recv,round);
   }
   
   private void handleGroupSendableEvent(GroupSendableEvent ev) {
     if (ev instanceof Send) {
       try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
       return;
     }
     
     if (ev.getDir() == Direction.DOWN) {
       last_recv[ls.my_rank]=round;
       if (debugFull)
         debug("Sent msg ("+ev+") in round "+round);
     } else {
       last_recv[ev.orig]=round;
       if (debugFull)
         debug("Recv msg from "+ev.orig+" in round "+round);
     }
     
     if (ev instanceof Alive)
       return;
     
     try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
   }
   
   private void handleSuspect(Suspect ev) {
     
     if (ev.getDir() == Direction.UP) {
       if ( ls.failed[ev.orig] ) {
         debug("Invalid (failed) message source");
         return;
       }
       
       ev.failed=ArrayOptimized.popArrayBoolean(ev.getMessage());
     }
     
     if (ev.failed[ls.my_rank]) {
       debug("i am not failed !!");
       return;
     }
     
     int i;
     boolean[] new_failed=null;
     
     for (i=0 ; i < ev.failed.length ; i++) {
       if (ev.failed[i] && !ls.failed[i]) {
         ls.fail(i);
         if (new_failed == null) {
           new_failed=new boolean[ls.failed.length];
           Arrays.fill(new_failed,false);
         }
         new_failed[i]=true;
       }
     }
     
     if (new_failed != null) {
       if (ev.getDir() == Direction.DOWN) {
         ArrayOptimized.pushArrayBoolean(ls.failed,ev.getMessage());
         //ev.getObjectsMessage().push(ls.failed);
         try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
       }
       
       sendFail(new_failed,ev.getChannel());
     }
   }
   
   private void handleSuspectTimer(SuspectTimer ev) {
 	  if(ev.getPeriod() != suspect_sweep){
 		  ev.setDir(Direction.invert(ev.getDir()));
 		  ev.setQualifierMode(EventQualifier.OFF);
 		  ev.setSource(this);
 		  try {
 			  ev.init();
 			  ev.go();
 			  SuspectTimer periodic=new SuspectTimer("Suspect Timer",suspect_sweep,ev.getChannel(),this);
 			  periodic.go();
 		  } catch (AppiaEventException e) {
 			  e.printStackTrace();
 		  } catch (AppiaException ex) {
 			  ex.printStackTrace();
 			  System.err.println("appia:group:SuspectSession: impossible to set SuspectTimer, SuspectSession will be idle");
 		  }
 	  }
 	  else
 		  try { 
 			  ev.go(); 
 		  } catch (AppiaEventException ex) {
 			  ex.printStackTrace(); 
 		  }    
     int i;
     boolean[] new_failed=null;
         
     for (i=0 ; i < last_recv.length ; i++) {
       if (i != ls.my_rank) {
         if ( (round-last_recv[i] > rounds_idle) && !ls.failed[i] ) {
           ls.fail(i);
           if (new_failed == null) {
             new_failed=new boolean[ls.failed.length];
             Arrays.fill(new_failed,false);
           }
           new_failed[i]=true;
 
           if (debugFull)
             debug("Suspected "+i+" because it passed "+(round-last_recv[i])+" rounds of "+suspect_sweep+" milliseconds since last reception");
         }
       }
     }
     
     if (new_failed != null) {
       sendSuspect(new_failed,ev.getChannel());
       sendFail(new_failed,ev.getChannel());
       
       if (debugFull) {
         String s="New failed members: ";
         for (int j=0 ; j < new_failed.length ; j++)
           if (new_failed[j])
             s=s+j+",";
         debug(s);
       }    
     }
     
     if (round > last_recv[ls.my_rank]) {
       sendAlive(ev.getChannel());
       last_recv[ls.my_rank]=round;
       if(debugFull)
         debug("Sent Alive in round "+round);
     }
 
     if (debugFull)
       debug("Ended round "+round+" at "+ev.getChannel().getTimeProvider().currentTimeMillis()+" milliseconds");
     
     round++;
     
     if (round < 0) {
       round=1;
       for (i=0 ; i < last_recv.length ; i++)
         last_recv[i]=0;
     }    
   }
   
   private void handleFIFOUndeliveredEvent(FIFOUndeliveredEvent ev) {
     try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
 
     if (vs == null)
       return;
     
     if (!(ev.what instanceof GroupSendableEvent))
       return;
     
     GroupSendableEvent event=(GroupSendableEvent)ev.what;
     
     if (!vs.group.equals(event.group)) {
       debug("Ignored FIFOUndelivered due to wrong group");
       return;
     }
     
     if (!vs.id.equals(event.view_id)) {
       debug("Ignored FIFOUndelivered due to wrong view id");
       return;
     }
     
    if (event.dest instanceof InetSocketAddress)
        undelivered((InetSocketAddress)event.dest,ev.getChannel());
    else if (event.dest instanceof AppiaMulticast) {
        Object[] dests=((AppiaMulticast)event.dest).getDestinations();
        for (int i=0 ; i < dests.length ; i++) {
            if (dests[i] instanceof InetSocketAddress)
                undelivered((InetSocketAddress)dests[i],ev.getChannel());
        }
    } else
        debug("Received FIFOUndelivered with unknown destination address. Ignoring it.");
   }
   
   private void handleTcpUndeliveredEvent(TcpUndeliveredEvent ev) {
     try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
 
     if (vs == null)
       return;
     
     undelivered((InetSocketAddress)ev.who,ev.getChannel());
   }
   
   private void undelivered(InetSocketAddress addr, Channel channel) {
     int rank,i;
     
     if ((rank=vs.getRankByAddress(addr)) >= 0) {
       if (!ls.failed[rank]) {
         ls.fail(rank);
         
         boolean[] new_failed=new boolean[vs.view.length];
         for (i=0 ; i < new_failed.length ; i++) new_failed[i]=(i==rank);
         
         sendSuspect(ls.failed,channel);
         sendFail(new_failed,channel);
         
         if (debugFull)
           debug("Suspected member "+rank+" due to Undelivered");
       }
     } else
       debug("Undelivered didn't contain a current view member");
   }
   
   private void sendSuspect(boolean[] failed, Channel channel) {
     try {
       Suspect ev=new Suspect(failed,channel,Direction.DOWN,this,vs.group,vs.id);
       ArrayOptimized.pushArrayBoolean(ls.failed,ev.getMessage());
       //ev.getObjectsMessage().push(ls.failed);
       ev.go();
     } catch (AppiaEventException ex) {
       ex.printStackTrace();
       debug("Impossible to send Suspect");
     }
   }
   
   private void sendFail(boolean[] failed, Channel channel) {
     try {
       Fail ev=new Fail(failed,vs.group,vs.id);
       EchoEvent echo=new EchoEvent(ev,channel,Direction.DOWN,this);
       echo.go();
     } catch (AppiaEventException ex) {
       ex.printStackTrace();
       System.err.println("appia:group:SuspectSession: impossible to inform locally of failure");
     }
   }
   
   private void sendAlive(Channel channel) {
     if (vs.view.length < 2)
       return;
     
     try {
       Alive alive=new Alive(channel,Direction.DOWN,this,vs.group,vs.id);
       alive.go();
     } catch (AppiaEventException ex) {
       ex.printStackTrace();
       debug("Impossible to send alive");
     }
   }
   
   
   // DEBUG
   /** Major debug mode.
    */
   public static final boolean debugFull=false;
   
   private boolean debugOn=false;
   private PrintStream debug=System.out;
   
   private void debug(String s) {
     if ((debug != null) && (debugFull || debugOn))
       debug.println("appia:group:SuspectSession: "+s);
   }
 
 }
