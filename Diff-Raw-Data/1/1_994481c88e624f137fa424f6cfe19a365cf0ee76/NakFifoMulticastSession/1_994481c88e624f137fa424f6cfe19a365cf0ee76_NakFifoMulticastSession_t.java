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
  /*
  * NakFifoMulticastSession.java
  *
  * Created on 10 de Julho de 2003, 15:46
  */
 
 package org.continuent.appia.protocols.nakfifo.multicast;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.ListIterator;
 
 import org.continuent.appia.core.*;
 import org.continuent.appia.core.events.AppiaMulticast;
 import org.continuent.appia.core.events.SendableEvent;
 import org.continuent.appia.core.events.channel.ChannelClose;
 import org.continuent.appia.core.events.channel.ChannelInit;
 import org.continuent.appia.core.events.channel.Debug;
 import org.continuent.appia.protocols.common.FIFOUndeliveredEvent;
 import org.continuent.appia.protocols.common.InetWithPort;
 import org.continuent.appia.protocols.frag.MaxPDUSizeEvent;
 import org.continuent.appia.protocols.nakfifo.*;
 import org.continuent.appia.xml.interfaces.InitializableSession;
 import org.continuent.appia.xml.utils.SessionProperties;
 
 
 /** Session of a protocol that provides reliable point-to-point communication.
  * This protocol operates better when using network multicast support.
  * <b>It only operates if destination is a <i>AppiaMulticast</i></b>.
  * @author alexp
  * @see org.continuent.appia.core.events.AppiaMulticast
  */
 public class NakFifoMulticastSession extends Session implements InitializableSession {
   
   /** The default duration of a round in milliseconds. 
    */  
   public static final long DEFAULT_TIMER_PERIOD=700; // 0,7 secs
   /** Default time, in milliseconds, to resend a NAK.
    * <br>
    * Must be a multiple of the round duration (DEFAULT_TIMER_PERIOD). 
    */  
   public static final long DEFAULT_RESEND_TIME=5000;  // 5 secs
   /** Default maximum time without receiving an Application message.
    * When this time is reached the peer is discarded.
    * <br>
    * Must be a multiple of the round duration (DEFAULT_TIMER_PERIOD). 
    */  
   public static final long DEFAULT_MAX_APPL_TIME=180000;    // 3 mins
   /** Default maximum time, in milliseconds, to recieve a message from a peer.
    * If this time is reached the peer is considered failed.
    * <br>
    * Must be a multiple of the round duration (DEFAULT_TIMER_PERIOD). 
    */  
   public static final long DEFAULT_MAX_RECV_TIME=60000;    // 60 secs
   /** Default maximum time to send a message to a peer.
    * If this time is reached a Ping message is sent.
    * <br>
    * Must be a multiple of the round duration (DEFAULT_TIMER_PERIOD). 
    */  
   public static final long DEFAULT_MAX_SENT_TIME=45000;     // 45 secs
   /** Default number of rounds between confirms
    */
   public static final long DEFAULT_CONFIRM_ROUNDS=0; // every round
 
   private long param_TIMER_PERIOD=DEFAULT_TIMER_PERIOD;
   private long param_RESEND_NACK_ROUNDS=DEFAULT_RESEND_TIME/param_TIMER_PERIOD;
   private long param_MAX_APPL_ROUNDS=DEFAULT_MAX_APPL_TIME/param_TIMER_PERIOD;
   private long param_MAX_RECV_ROUNDS=DEFAULT_MAX_RECV_TIME/param_TIMER_PERIOD;
   private long param_MAX_SENT_ROUNDS=DEFAULT_MAX_SENT_TIME/param_TIMER_PERIOD;
   private long param_CONFIRM_ROUNDS=DEFAULT_CONFIRM_ROUNDS;
   
   /** Creates a new instance of NakFifoSession */
   public NakFifoMulticastSession(Layer layer) {
     super(layer);
     
     last_msg_sent=System.currentTimeMillis() & MessageUtils.INIT_MASK;
     if (last_msg_sent == MessageUtils.INIT_MASK)
       last_msg_sent--;
     first_msg_sent=last_msg_sent+1;
   }
   
   public void init(SessionProperties params) {
     if (params.containsKey("timer_period"))
       param_TIMER_PERIOD=params.getLong("timer_period");
     if (params.containsKey("resend_nack_time"))
       param_RESEND_NACK_ROUNDS=params.getLong("resend_nack_time")/param_TIMER_PERIOD;
     if (params.containsKey("max_appl_time"))
       param_MAX_APPL_ROUNDS=params.getLong("max_appl_time")/param_TIMER_PERIOD;
     if (params.containsKey("max_recv_time"))
       param_MAX_RECV_ROUNDS=params.getLong("max_recv_time")/param_TIMER_PERIOD;
     if (params.containsKey("max_sent_time"))
       param_MAX_SENT_ROUNDS=params.getLong("max_sent_time")/param_TIMER_PERIOD;
     if (params.containsKey("confirm_rounds"))
       param_CONFIRM_ROUNDS=params.getLong("confirm_rounds");
     if (params.containsKey("debug"))
       debugOn=params.getBoolean("debug");
   }
 
   /** Main Event handler. */  
   public void handle(Event event) {
     
     if (event instanceof NackEvent) {
       handleNack((NackEvent)event); return;
     } else if (event instanceof IgnoreEvent) {
       handleIgnore((IgnoreEvent)event); return;
     } else if (event instanceof PingEvent) {
       handlePing((PingEvent)event); return;
     } else if (event instanceof UpdateEvent) {
       handleUpdate((UpdateEvent)event); return;
     } else if (event instanceof ConfirmEvent) {
     	handleConfirm((ConfirmEvent)event); return;
     } else if (event instanceof NakFifoTimer) {
       handleNakFifoTimer((NakFifoTimer)event); return;
     } else if (event instanceof SendableEvent) {
       handleSendable((SendableEvent)event); return;
     } if (event instanceof ChannelInit) {
       handleChannelInit((ChannelInit)event); return;
     } if (event instanceof ChannelClose) {
       handleChannelClose((ChannelClose)event); return;
     } else if (event instanceof MaxPDUSizeEvent) {
       handleMaxPDUSize((MaxPDUSizeEvent)event); return;
     } else if (event instanceof Debug) {
       Debug ev=(Debug)event;
       
       if (ev.getQualifierMode() == EventQualifier.ON) {
         debugOn=true;
         if (ev.getOutput() instanceof java.io.PrintStream)
           debug=(java.io.PrintStream)ev.getOutput();
         else
           debug=new java.io.PrintStream(ev.getOutput());
       } else if (ev.getQualifierMode() == EventQualifier.OFF) {
         debugOn=false;
       }
       
       try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
       return;
     }
     
     debug("Unwanted event (\""+event.getClass().getName()+"\") received. Continued...");
     try { event.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
   }
   
   private long first_msg_sent;
   private long last_msg_sent;
   private long rounds_confirm=0;
   private HashMap peers=new HashMap();
   private Channel timerChannel=null;
   private MessageUtils utils=new MessageUtils();
   
   private void handleChannelInit(ChannelInit ev) {
     try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
     
     if (timerChannel == null)
       sendTimer(ev.getChannel());
     
     //appia.protocols.drop.DropSession.dropRate=0.3;
     debug("Params:\n\tTIMER_PERIOD="+param_TIMER_PERIOD+
         "\n\tMAX_APPL_ROUNDS="+param_MAX_APPL_ROUNDS+
         "\n\tMAX_RECV_ROUNDS="+param_MAX_RECV_ROUNDS+
         "\n\tMAX_SENT_ROUNDS="+param_MAX_SENT_ROUNDS+
         "\n\tRESEND_NACK_ROUNDS="+param_RESEND_NACK_ROUNDS+
         "\n\tCONFIRM_ROUNDS="+param_CONFIRM_ROUNDS);
   }
   
   private void handleChannelClose(ChannelClose ev) {
     try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
     
     if (ev.getChannel() == timerChannel) {
       timerChannel=null;
       Iterator iter=peers.values().iterator();
       while (iter.hasNext() && (timerChannel == null)) {
         Peer peer=(Peer)iter.next();
         if (peer.last_channel != null)
           sendTimer(peer.last_channel);
       }
       if (timerChannel != null)
         debug("Sent new timer in channel "+timerChannel.getChannelID());
       else
         debug("Unable to send timer. Corret operation is not garanteed");
     }
   }
   
   /*
   private void handleRegisterSocket(RegisterSocketEvent ev) {
     if ((ev.getDir() == Direction.UP) && !ev.error) {
       InetWithPort addr=new InetWithPort(ev.localHost, ev.port);
       registerAddr(addr);
     }
     
     try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
   }
   */
   
   private void handleMaxPDUSize(MaxPDUSizeEvent event) {
     if (event.getDir() == Direction.UP) {
       event.pduSize-=5;
     }
     try {
       event.go();
     } catch (AppiaEventException e) {
       e.printStackTrace();
     }
   }
   
   private void handleSendable(SendableEvent event) {
     if (event.getDir() == Direction.UP) {
       byte flags=event.getMessage().popByte();
       if ((flags & MessageUtils.IGNORE_FLAG) != 0) {
         debug("Received msg with ignore flag. Ignoring.");
         try { event.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
         return;
       }
 
       Peer peer=(Peer)peers.get(event.source);
       if (peer == null)
         peer=createPeer(event.source,last_msg_sent,event.getChannel());
 
       long seq;
       if ((seq=utils.popSeq(event.getMessage(),peer.last_msg_delivered,false)) < 0) {
         debug("Problems reading sequence number discarding event "+event+" from "+event.source.toString());
         return;
       }
       
       receive(peer,event,seq,seq);
       return;
     }
     
     if (event.getDir() == Direction.DOWN) {
       
       if ((event.dest instanceof InetWithPort) && (((InetWithPort)event.dest).host.isMulticastAddress())) {
         debug("Destination is a IP Multicast address. Ignored.");
         event.getMessage().pushByte(MessageUtils.IGNORE_FLAG);
         try { event.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
         return;
       }
       
       last_msg_sent++;
       
       try {
         SendableEvent clone=(SendableEvent)event.cloneEvent();
         // Puts peer counter
         clone.getMessage().pushInt(0);
         
         if (event.dest instanceof AppiaMulticast) {
           Object[] dests=((AppiaMulticast)event.dest).getDestinations();
           for (int i=0 ; i < dests.length ; i++)
             sending(clone,dests[i],last_msg_sent);
         } else {
           sending(clone,event.dest,last_msg_sent);
         }
         
         utils.pushSeq(event.getMessage(),last_msg_sent);
         event.getMessage().pushByte(MessageUtils.NOFLAGS);
         
         event.go();
         
       } catch (AppiaEventException ex) {
         ex.printStackTrace();
         debug("To mantain coerence, sending undelivered.");
         sendFIFOUndelivered(event,event.dest);
         return;
       } catch (CloneNotSupportedException ex) {
         ex.printStackTrace();
         debug("To mantain coerence, sending undelivered.");
         sendFIFOUndelivered(event,event.dest);
         return;
       }
       
       return;
     }
     
     debug("Direction is wrong. Discarding event "+event);
   }
   
   private void handlePing(PingEvent ev) {
     if (ev.getDir() != Direction.UP) {
       debug("Discarding Ping event due to wrong diretion.");
       return;
     }
     
     // Confirmed
     Peer peer=(Peer)peers.get(ev.source);
     if (peer == null) {
     	debug("Received Ping from unknown peer ("+ev.source+"). Ignoring confirm.");
     	ev.getMessage().discard(MessageUtils.SEQ_SIZE);
     } else {
     	long confirmed;
     	if ((confirmed=utils.popSeq(ev.getMessage(),peer.last_msg_confirmed,false)) < 0) {
     		debug("Problems reading confirm sequence number from "+ev.source);
     	} else {
     		confirmed(peer,confirmed,ev.getChannel());
     	}
     }
     
     handleSendable(ev);
   }
   
   private void handleNack(NackEvent ev) {
     Peer peer=(Peer)peers.get(ev.source);
     if (peer == null) {
       peer=createPeer(ev.source,last_msg_sent,ev.getChannel());
       return;
     }
     
     long first;
     long last;
     
     if ((first=ev.getMessage().popLong()) < 0) {
       debug("Ignoring Nack due to wrong first seq number.");
       return;
     }
     if ((last=ev.getMessage().popLong()) < 0) {
       debug("Ignoring Nack due to wrong last seq number.");
       return;
     }
     if (first > last) {
       debug("Ignoring Nack due to wrong seq numbers (first="+first+",last="+last+",confirmed="+peer.last_msg_confirmed+").");
       return;
     }
     
     if ((first < first_msg_sent) || (last > last_msg_sent)) {
       // Restart comunication
       debug("Received Nack("+first+","+last+") for message not sent. Restarting communication.");
       ignore(peer,ev.getChannel());
       return;
     }
     
     if (debugFull)
       debugPeer(peer,"handleNack("+first+","+last+")");
     
     if (first <= peer.last_msg_confirmed) {
       if (last <= peer.last_msg_confirmed) {
         debug("Received Nack for messages already confirmed. Discarding.");
         return;
       }
       first=peer.last_msg_confirmed+1;
       debug("Received Nack for message already confirmed. Changig first to "+first);
     }
     
     if (last > peer.last_msg_sent) {
       debug("Nack includes messages not sent to peer. Sending Update.");
       if (first <= peer.last_msg_sent) {
         debug("Nack partially includes messages sent to peer, resending.");
         resend(peer,first,peer.last_msg_sent);
       }
       update(peer,last,ev.getChannel());
     } else
       resend(peer,first,last);
   }
   
   private void handleNakFifoTimer(NakFifoTimer ev) {
     try { ev.go(); } catch (AppiaEventException ex) { ex.printStackTrace(); }
     
     if (ev.getQualifierMode() != EventQualifier.NOTIFY)
       return;
 
     boolean doConfirm=false;
     rounds_confirm++;
     if (rounds_confirm > param_CONFIRM_ROUNDS) {
     	rounds_confirm=0;
     	doConfirm=true;
     }
 
     boolean changedSeq=false;
     Iterator peers_iter=peers.values().iterator();
     while (peers_iter.hasNext()) {
       Peer peer=(Peer)peers_iter.next();
       
       peer.rounds_appl_msg++;
       peer.rounds_msg_recv++;
       peer.rounds_msg_sent++;
       
       if (debugFull)
         debugPeer(peer,"Timer");
       
       if (peer.nacked != null) {
         peer.nacked.rounds++;
         if (peer.nacked.rounds > param_RESEND_NACK_ROUNDS) {
           nack(peer,peer.last_msg_delivered >= peer.nacked.first_msg ? peer.last_msg_delivered+1 : peer.nacked.first_msg, peer.nacked.last_msg, ((SendableEvent)peer.undelivered_msgs.getFirst()).getChannel());
           peer.nacked.rounds=0;
         }
       } else {
         if (peer.rounds_appl_msg > param_MAX_APPL_ROUNDS) {
           peers_iter.remove();
           peer=null;
         }
       }
       
       if ((peer != null) && (peer.rounds_msg_recv > param_MAX_RECV_ROUNDS)) {
         Iterator msgs=peer.unconfirmed_msgs.iterator();
         while (msgs.hasNext()) {
           sendFIFOUndelivered((SendableEvent)msgs.next(),peer.addr);
         }
         peers_iter.remove();
         peer=null;
       }
       
       if ((peer != null) && (peer.rounds_msg_sent > param_MAX_SENT_ROUNDS)) {
         try {
           PingEvent e=new PingEvent(peer.last_channel,this);
          e.getMessage().pushInt(0);
           
           if (!changedSeq) {
             last_msg_sent++;
             changedSeq=true;
           }
           
           sending(e,peer.addr,last_msg_sent);
           utils.pushSeq(e.getMessage(), last_msg_sent);
           e.getMessage().pushByte(MessageUtils.NOFLAGS);
           
           // Confirmed
           utils.pushSeq(e.getMessage(), peer.last_msg_delivered);
           peer.last_confirm_sent=peer.last_msg_delivered;
           
           e.dest=peer.addr;
           e.go();
         } catch (AppiaEventException ex) {
           ex.printStackTrace();
           debug("Impossible to send ping.");
         } catch (CloneNotSupportedException ex) {
           ex.printStackTrace();
           debug("Impossible to send ping.");
         }
       }
       
       if (doConfirm && (peer != null) &&  
       		(peer.last_msg_delivered > peer.last_confirm_sent)) {
       	confirm(peer);
       }
     }
   }
   
   private void handleIgnore(IgnoreEvent ev) {
     Peer peer=(Peer)peers.get(ev.source);
     if (peer == null)
       peer=createPeer(ev.source,last_msg_sent,ev.getChannel());
     
     if (debugFull)
       debugPeer(peer,"handleIgnore");
     
     peer.last_msg_delivered=ev.getMessage().popLong();
     peer.undelivered_msgs.clear();
     peer.nacked=null;
     
     peer.rounds_msg_recv=0;
     peer.last_channel=ev.getChannel();
     
     //if (debugFull)
     debug("Received Ignore from "+peer.addr.toString()+" with value "+peer.last_msg_delivered);
   }
   
   private void handleUpdate(UpdateEvent ev) {
     Peer peer=(Peer)peers.get(ev.source);
     if (peer == null)
       peer=createPeer(ev.source,last_msg_sent,ev.getChannel());
     
     if ((ev.from=utils.popSeq(ev.getMessage(),peer.last_msg_delivered, false)) < 0) {
       debug("Received incorrect Update. Discarding.");
       return;
     }
     if ((ev.to=utils.popSeq(ev.getMessage(),peer.last_msg_delivered,false)) < 0){
       debug("Received incorrect Update. Discarding.");
       return;
     }
     
     receive(peer,ev,ev.from,ev.to);
   }
   
   private void handleConfirm(ConfirmEvent ev) {
     Peer peer=(Peer)peers.get(ev.source);
     if (peer == null) {
     	debug("Received Confirm from unknown peer ("+ev.source+"). Discarding it.");
     	return;
     }
     
     long confirmed;
     if ((confirmed=utils.popSeq(ev.getMessage(),peer.last_msg_confirmed,false)) < 0) {
       debug("Problems reading confirm sequence number from "+ev.source);
       return;
     }
 
     confirmed(peer,confirmed,ev.getChannel());
   }
   
   private void sending(SendableEvent ev, Object addr, long seq) throws AppiaEventException, CloneNotSupportedException {
     Peer peer=(Peer)peers.get(addr);
     if (peer == null)
       peer=createPeer(addr,seq-1,ev.getChannel());
     
     if (seq > peer.last_msg_sent+1)
       update(peer,seq-1,ev.getChannel());
     
     peer.last_msg_sent=seq;
     storeUnconfirmed(peer,ev);
     
     peer.rounds_msg_sent=0;
     if (!(ev instanceof PingEvent))
       peer.rounds_appl_msg=0;
   }
   
   private void receive(Peer peer, SendableEvent ev, long seqfrom, long seqto) {    
     // Rounds
     peer.rounds_msg_recv=0;
     if (!(ev instanceof PingEvent) && !(ev instanceof UpdateEvent))
       peer.rounds_appl_msg=0;
         
     if (debugFull)
     	debug("Received event "+ev+" from "+peer.addr+" with seq "+seqfrom+" -> "+seqto);
     
     // Deliver
     if ((seqfrom <= peer.last_msg_delivered+1) && (seqto >= peer.last_msg_delivered+1)) {
       try {
         if (!(ev instanceof PingEvent) && !(ev instanceof UpdateEvent))
           ev.go();
       } catch  (AppiaEventException ex) {
         ex.printStackTrace();
         return;
       }
       
       peer.last_msg_delivered=seqto;
       if (peer.undelivered_msgs.size() > 0) {
         long undelivered=deliverUndelivered(peer);
         
         if (debugFull)
           debugPeer(peer,"receive1("+seqfrom+","+undelivered+")");
         
         if (peer.nacked != null) {
           if (peer.last_msg_delivered >= peer.nacked.last_msg)
             peer.nacked=null;
         }
         
         if ((peer.nacked == null) && (undelivered >= 0))
           nack(peer,peer.last_msg_delivered+1,undelivered-1,ev.getChannel());
       }
     } else { // Wrong seq number
       if (seqto <= peer.last_msg_delivered) {
         debug("Received old message from "+peer.addr.toString()+". Discarding.");
         return;
       }
    
       if (debugFull)
     	  debug("Storing undelivered from "+peer.addr+" with seq "+seqfrom);
       
       storeUndelivered(peer,ev,seqfrom);
       
       if (peer.nacked == null)
         nack(peer,peer.last_msg_delivered+1,seqfrom-1,ev.getChannel());
     }
   }
   
   private void confirmed(Peer peer, long peer_confirmed, Channel channel) {
     // Confirmed
     if ((peer_confirmed >= peer.first_msg_sent) && (peer_confirmed <= peer.last_msg_sent)) {
       if (peer_confirmed > peer.last_msg_confirmed)
         removeUnconfirmed(peer,peer_confirmed);
     } else {
       if (peer_confirmed > last_msg_sent) {
         debug("Received wrong peer confirmed number (expected between "+peer.first_msg_sent+" and "+last_msg_sent+", received "+peer_confirmed+" from "+peer.addr+". Sending Ignore.");
         ignore(peer,channel);
       }
     }
   }
   
   private void nack(Peer peer, long first, long last, Channel channel) {
     //TODO: erase
     if (first > last) {
       debugPeer(peer,"nack error");
       throw new AppiaError("first("+first+") > last("+last+")");
       //return;
     }
     
     try {
       NackEvent nack=new NackEvent(channel,this);
       nack.getMessage().pushLong(last);
       nack.getMessage().pushLong(first);
       nack.dest=peer.addr;
       nack.go();      
     } catch (AppiaEventException ex) {
       ex.printStackTrace();
       debug("Impossible to send Nack. Maybe next time.");
       return;
     }
     peer.nacked=new Nacked(first,last);
     
     if (debugFull)
       debugPeer(peer,"nack");
   }
   
   private void ignore(Peer peer, Channel channel) {
     try {
       IgnoreEvent ev=new IgnoreEvent(channel,this);
       ev.getMessage().pushLong(peer.last_msg_confirmed);
       ev.dest=peer.addr;
       ev.go();
       if (debugFull)
         debug("Sent Ignore with "+peer.last_msg_confirmed+" to "+peer.addr);
     } catch (AppiaEventException ex) {
       ex.printStackTrace();
       debug("Unable to send Ignore later it will be retransmited.");
       return;
     }
     peer.rounds_msg_sent=0;
   }
 
   private void update(Peer peer, long to, Channel channel) {
     try {
       UpdateEvent update=new UpdateEvent(channel,this);
       update.from=peer.last_msg_sent+1;
       update.to=to;
       update.dest=peer.addr;
       
       UpdateEvent clone=(UpdateEvent)update.cloneEvent();
       storeUnconfirmed(peer,clone);
       
       utils.pushSeq(update.getMessage(),update.to);
       utils.pushSeq(update.getMessage(),update.from);
       update.go();      
     } catch (AppiaEventException ex) {
       ex.printStackTrace();
       debug("Unable to send or store update.");
       throw new AppiaError("Don't know how to solve this problem. Aborting.");
     } catch (CloneNotSupportedException ex) {
       ex.printStackTrace();
       debug("Unable to send or store update.");
       throw new AppiaError("Don't know how to solve this problem. Aborting.");
     }
     peer.last_msg_sent=to;
     peer.rounds_msg_sent=0;
   }
 
   private void confirm(Peer peer) {
     try {
         ConfirmEvent ev=new ConfirmEvent(peer.last_channel,this);
         utils.pushSeq(ev.getMessage(), peer.last_msg_delivered);
         ev.dest=peer.addr;
         ev.go();
     } catch (AppiaEventException ex) {
         ex.printStackTrace();
         debug("Unable to send ConfirmEvent. Continuing.");
         return;
     }
     peer.last_confirm_sent=peer.last_msg_delivered;
     if (debugFull)
         debug("Sent Confirm "+peer.last_confirm_sent+" to "+peer.addr);
   }
 
   private void storeUnconfirmed(Peer peer, SendableEvent ev) {
     if (!(ev instanceof UpdateEvent)) {
       // updates peer counter
       ev.getMessage().pushInt(ev.getMessage().popInt()+1);
     }
     
     peer.unconfirmed_msgs.addLast(ev);
   }
   
   private void removeUnconfirmed(Peer peer, long last) {
     while (peer.last_msg_confirmed < last) {
     	SendableEvent ev = (SendableEvent) peer.unconfirmed_msgs.removeFirst();
       if (ev instanceof UpdateEvent)
         peer.last_msg_confirmed=((UpdateEvent)ev).to;
       else {
         peer.last_msg_confirmed++;
 
         // handles peer counter
         int c=ev.getMessage().popInt()-1;
         if (c <= 0)
           ev.getMessage().discardAll();
         else
           ev.getMessage().pushInt(c);
       }
     }
   }
   
   private void resend(Peer peer, long first, long last) {
     ListIterator aux=peer.unconfirmed_msgs.listIterator();
     long seq=peer.last_msg_confirmed;
     while (aux.hasNext() && (seq <= last)) {
       SendableEvent evaux=(SendableEvent)aux.next();
       if (evaux instanceof UpdateEvent) {
         UpdateEvent update=(UpdateEvent)evaux;
         seq=update.to;
         if (((update.from >= first) && (update.from <= last)) || ((update.to >= first) && (update.to <= last))) {
           try {
             SendableEvent ev=(UpdateEvent)update.cloneEvent();
             ev.setSource(this);
             ev.init();
             
             utils.pushSeq(ev.getMessage(),update.to);
             utils.pushSeq(ev.getMessage(),update.from);
             ev.dest=peer.addr;
             ev.go();
             
             peer.rounds_msg_sent=0;
           } catch (AppiaEventException ex1) {
             ex1.printStackTrace();
           } catch (CloneNotSupportedException ex2) {
             ex2.printStackTrace();
           }
         }
       } else {
         seq++;
         if ((seq >= first) && (seq <= last)) {
           try {
             SendableEvent ev=(SendableEvent)evaux.cloneEvent();
             // Removes peer counter
             ev.getMessage().popInt();
             ev.setSource(this);
             ev.init();
             
             utils.pushSeq(ev.getMessage(),seq);
             ev.getMessage().pushByte(MessageUtils.NOFLAGS);
             ev.dest=peer.addr;
             ev.go();
             
             peer.rounds_msg_sent=0;
           } catch (AppiaEventException ex1) {
             ex1.printStackTrace();
           } catch (CloneNotSupportedException ex2) {
             ex2.printStackTrace();
           }
         }
       }
     }
   }
   
   private void storeUndelivered(Peer peer, SendableEvent ev, long seq) {
     if (!(ev instanceof UpdateEvent))
       utils.pushSeq(ev.getMessage(),seq);
     ListIterator aux=peer.undelivered_msgs.listIterator(peer.undelivered_msgs.size());
     while (aux.hasPrevious()) {
       SendableEvent evaux=(SendableEvent)aux.previous();
       long seqaux;
       if (evaux instanceof UpdateEvent) { 
     	  UpdateEvent update=(UpdateEvent)evaux;
     	  if ((seq >= update.from) && (seq <= update.to)) {
     		  debug("Received undelivered message already stored. Discarding new copy.");
     		  return;
     	  }
     	  seqaux=update.to;
       }  else {
     	  seqaux=utils.popSeq(evaux.getMessage(),peer.last_msg_delivered,true);
     	  if (seqaux == seq) {
     		  debug("Received undelivered message already stored. Discarding new copy.");
     		  return;
     	  }
       }
       if (seqaux < seq) {
         aux.next();
         aux.add(ev);
         return;
       }
     }
     peer.undelivered_msgs.addFirst(ev);
   }
   
   private long deliverUndelivered(Peer peer) {
     ListIterator aux=peer.undelivered_msgs.listIterator();
     while (aux.hasNext()) {
       SendableEvent evaux=(SendableEvent)aux.next();
       if (evaux instanceof UpdateEvent) {
         UpdateEvent update=(UpdateEvent)evaux;
         if (update.to <= peer.last_msg_delivered) {
         	debug("Discarded unwated event from "+peer.addr+" with seq "+update.from+" -> "+update.to);
         	aux.remove();
         } else if (update.from == peer.last_msg_delivered+1) {
           peer.last_msg_delivered=update.to;
           aux.remove();
         } else {
           return update.from;
         }
       } else { // Not UpdateEvent interface regular SendableEvent
         long seqaux=utils.popSeq(evaux.getMessage(),peer.last_msg_delivered,true);
         if (seqaux <= peer.last_msg_delivered) {
         	debug("Discarded unwated event from "+peer.addr+" with seq "+seqaux);
         	aux.remove();        	
         } else if (seqaux == peer.last_msg_delivered+1) {
           if (!(evaux instanceof PingEvent)) {
             try {
               evaux.getMessage().discard(MessageUtils.SEQ_SIZE);
               evaux.go();
             } catch (AppiaEventException ex) {
               ex.printStackTrace();
               debug("Discarding event "+evaux+". This may lead to incoherence.");
             }
           }
           peer.last_msg_delivered=seqaux;
           aux.remove();
         } else {
           return seqaux;
         }
       }
     }
     return -1;
   }
   
   private Peer createPeer(Object addr, long init, Channel channel) {
     Peer peer=new Peer(addr,init);
     peers.put(peer.addr,peer);
     ignore(peer,channel);
     return peer;
   }
   
   private void sendFIFOUndelivered(SendableEvent ev, Object addr) {
     if (ev instanceof PingEvent)
       return;
     try {
       SendableEvent clone=(SendableEvent)ev.cloneEvent();
       clone.dest=addr;
       FIFOUndeliveredEvent e=new FIFOUndeliveredEvent(ev.getChannel(),this,clone);
       e.go();
     } catch (AppiaEventException ex) {
       ex.printStackTrace();
       debug("Unable to send Undelivered notification. Continuing but problems may happen.");
     } catch (CloneNotSupportedException ex) {
       ex.printStackTrace();
       debug("Unable to send Undelivered notification. Continuing but problems may happen.");
     }
   }
   
   private void sendTimer(Channel channel) {
     try {
       NakFifoTimer timer=new NakFifoTimer(param_TIMER_PERIOD,channel,this,EventQualifier.ON);
       timer.go();
       timerChannel=channel;
     } catch (AppiaException ex) {
       //ex.printStackTrace();
       debug("Unable to send timer. Correct operation of session is not guaranteed.");
     }
   }
     
   /*
   private int[] myHashes=new int[5];
   private int myHashesLen=0;
   
   private void registerAddr(Object addr) {
     if (myHashes.length == myHashesLen) {
       int[] aux=new int[myHashes.length*2];
       System.arraycopy(myHashes, 0, aux, 0, myHashes.length);
       myHashes=aux;
     }
     myHashes[myHashesLen]=addr.hashCode();
     myHashesLen++;
     debug("Registered "+addr+" with hash "+myHashes[myHashesLen-1]);
   }
   
   private boolean forMe(int hash) {
     for (int i=0 ; i < myHashesLen ; i++) {
       if (myHashes[i] == hash)
         return true;
     }
     return false;
   }
   */
   
   
   // DEBUG
   /** Full debug information. */
   public static final boolean debugFull=false;
   public static final int debugListLimit=10;
   private boolean debugOn=false;
   private java.io.PrintStream debug = System.err;
   
   private void debug(String s) {
     if ((debugFull || debugOn) && (debug != null))
       debug.println("appia.protocols.NakFifoMulticastSession: "+s);
   }
   
   private void debugPeer(Peer peer, String s) {
     if ((debugFull || debugOn) && (debug != null)) {
       debug.println("@"+s+" Peer: "+peer.addr.toString());
       debug.println("\t First Msg Sent: "+peer.first_msg_sent);
       debug.println("\t Last Msg Sent/Confirmed: "+peer.last_msg_sent+"/"+peer.last_msg_confirmed);
       debug.println("\t Last Msg Delivered: "+peer.last_msg_delivered);
       debug.println("\t Rounds Appl/Sent/Recv: "+peer.rounds_appl_msg+"/"+peer.rounds_msg_sent+"/"+peer.rounds_msg_recv);
       
       int limit=debugListLimit;
       debug.println("\t Unconfirmed Msgs (size="+peer.unconfirmed_msgs.size()+"):");
       ListIterator iter=peer.unconfirmed_msgs.listIterator();
       long l=peer.last_msg_confirmed;
       while (iter.hasNext()) {
         SendableEvent ev=(SendableEvent)iter.next();
         l++;
         debug.println("\t\t "+l+": "+ev);
         if (--limit <= 0) {
             debug.println("\t\t  ...");
             break;
         }
       }
       
       limit=debugListLimit;
       debug.println("\t Undelivered Msgs (size="+peer.undelivered_msgs.size()+"):");
       iter=peer.undelivered_msgs.listIterator();
       while (iter.hasNext()) {
         SendableEvent ev=(SendableEvent)iter.next();
         if (ev instanceof UpdateEvent) {
         	UpdateEvent e=(UpdateEvent)ev;
         	debug.println("\t\t "+e.from+" -> "+e.to);
         } else {
         	l=utils.popSeq(ev.getMessage(),peer.last_msg_delivered,true);
         	debug.println("\t\t "+l+": "+ev);
         }
         if (--limit <= 0) {
             debug.println("\t\t  ...");
             break;
         }
       }
       
       debug.print("\t Nacked First/Last/Rounds: ");
       if (peer.nacked == null)
         debug.println("null");
       else
         debug.println(""+peer.nacked.first_msg+"/"+peer.nacked.last_msg+"/"+peer.nacked.rounds);
       
       debug.println("\t Channel: "+peer.last_channel);
     }
   }
   
 /*
   public static void main(String[] args) {
     NakFifoMulticastSession fs=new NakFifoMulticastSession(null);
     SendableEvent ev=new SendableEvent();
     Peer peer=new Peer(new InetWithPort());
     long l;
  
     System.out.println("SEQ_SIZE="+SEQ_SIZE+" SEQ_MASK="+Long.toHexString(SEQ_MASK)+" INIT_SEQ_SIZE="+INIT_SEQ_SIZE+" INIT_SEQ_MASK="+Long.toHexString(INIT_SEQ_MASK));
  
 //    if (SEQ_SIZE == 4) {
       ev=new SendableEvent();
       fs.storeUndelivered(peer,ev,10);
       ev=new SendableEvent();
       fs.storeUndelivered(peer,ev,15);
       ev=new SendableEvent();
       fs.storeUndelivered(peer,ev,16);
       ev=new SendableEvent();
       fs.storeUndelivered(peer,ev,12);
       ev=new SendableEvent();
       fs.storeUndelivered(peer,ev,9);
       ev=new SendableEvent();
       fs.storeUndelivered(peer,ev,11);
 //    }
     fs.debugPeer(peer,"");
   }
  */
 }
