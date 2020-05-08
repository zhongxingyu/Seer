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
  package org.continuent.appia.protocols.udpsimple;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.MulticastSocket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Random;
 
 import org.continuent.appia.core.AppiaEventException;
 import org.continuent.appia.core.Channel;
 import org.continuent.appia.core.Direction;
 import org.continuent.appia.core.Event;
 import org.continuent.appia.core.EventQualifier;
 import org.continuent.appia.core.Layer;
 import org.continuent.appia.core.Session;
 import org.continuent.appia.core.events.AppiaMulticast;
 import org.continuent.appia.core.events.SendableEvent;
 import org.continuent.appia.core.events.channel.ChannelClose;
 import org.continuent.appia.core.events.channel.ChannelInit;
 import org.continuent.appia.core.events.channel.Debug;
 import org.continuent.appia.core.message.Message;
 import org.continuent.appia.core.message.MsgBuffer;
 import org.continuent.appia.protocols.common.AppiaThreadFactory;
 import org.continuent.appia.protocols.common.RegisterSocketEvent;
 import org.continuent.appia.protocols.common.SendableNotDeliveredEvent;
 import org.continuent.appia.protocols.common.ThreadFactory;
 import org.continuent.appia.protocols.frag.MaxPDUSizeEvent;
 import org.continuent.appia.protocols.utils.HostUtils;
 import org.continuent.appia.xml.interfaces.InitializableSession;
 import org.continuent.appia.xml.utils.SessionProperties;
 
 
 /**
  * Class UdpSimpleSession is the Session subclassing for UdpSimple
  * protocol. Concurrently to this session, a reader class, running on
  * another thread listens the sockets for datagrams. Synchronization with
  * Appia is made using async events.
  * <br>
  * <b>The UDP socket is bound to a local address</b>.
  * If {@link org.continuent.appia.protocols.common.RegisterSocketEvent#localHost} is null, 
  * {@link org.continuent.appia.protocols.utils.HostUtils} is used to select one. 
  *
  * @see org.continuent.appia.core.Session
  * @see org.continuent.appia.core.events.SendableEvent
  * @see UdpSimpleLayer
  * @see org.continuent.appia.protocols.common.RegisterSocketEvent
  * @see SendableNotDeliveredEvent
  * @see MulticastInitEvent
  * @author Hugo Miranda, M.Joao Monteiro
  */
 
 public class UdpSimpleSession extends Session implements InitializableSession {
   
   private DatagramSocket sock = null; //point-to-point socket
   private UdpSimpleReader sockReader = null; //point-to-point reader
   private HashMap multicastReaders = new HashMap(); //multicast readers
   protected HashMap channels = new HashMap(); // known channels
   
   private InetAddress param_LOCAL_ADDRESS=null;
   private int param_MAX_UDPMSG_SIZE=DEFAULT_MAX_UDPMSG_SIZE;
   private static final int MAX_UdpSimple_HEADERS = 80+8;
   public static final int DEFAULT_MAX_UDPMSG_SIZE=8192;
   public static final int DEFAULT_SOTIMEOUT=5000;
   private int param_SOTIMEOUT=DEFAULT_SOTIMEOUT;
   
   private InetSocketAddress myAddress = null;
   private InetSocketAddress ipMulticast = null;
   
   protected PrintStream debugOutput = System.out;
 
   private ThreadFactory threadFactory = null;
   
   /**
    * Session standard constructor.
    *
    * @param l The UdpSimpleLayer creating the session.
    */
   
   public UdpSimpleSession(Layer l) {
     super(l);
     
     if (UdpSimpleConfig.debugOn)
       debug("UDP: New udpSimple session");
     threadFactory = AppiaThreadFactory.getThreadFactory();
   }
   
   /**
    * Initializes the session using the parameters given in the XML configuration.
    * Possible parameters:
    * <ul>
    * <li><b>local_address</b> the address to which the UDP socket is bound.
    * <li><b>max_udp_message_size</b> the maximum size of an underlying UDP message payload.
    * <li><b>reader_sotimeout</b> the timeout of the threads that listen UDP sockets. Default is 5 seconds.
    * </ul>
    * 
    * @param params The parameters given in the XML configuration.
    */
   public void init(SessionProperties params) {
     if (params.containsKey("local_address")) {
       try {
         param_LOCAL_ADDRESS=InetAddress.getByName(params.getString("local_address"));
       } catch (UnknownHostException e) {
         System.err.println("UDP: Unknown host \""+params.getString("local_address")+"\". Using default.");
         param_LOCAL_ADDRESS=null;
       }
     }
     if (params.containsKey("max_udp_message_size"))
       param_MAX_UDPMSG_SIZE=params.getInt("max_udp_message_size");
     if (params.containsKey("reader_sotimeout"))
         param_SOTIMEOUT=params.getInt("reader_sotimeout");
   }
 
   /**
    * The event handler function. Tests event types and dispatches
    * them to the appropriate handler.
    * @param e The event
    * @see Session#handle
    */
   
   public void handle(Event e) {
     
     if (e instanceof RegisterSocketEvent)
       handleRegisterSocket((RegisterSocketEvent) e);
     else if (e instanceof SendableEvent)
       handleSendable((SendableEvent) e);
     else if (e instanceof ChannelInit)
       handleChannelInit((ChannelInit) e);
     else if (e instanceof ChannelClose)
       handleChannelClose((ChannelClose) e);
     else if (e instanceof Debug)
       handleDebug((Debug) e);
     else if (e instanceof MaxPDUSizeEvent)
       handlePDUSize((MaxPDUSizeEvent) e);
     else if (e instanceof MulticastInitEvent)
       handleMulticastInit((MulticastInitEvent) e);
     else {
       /*Unexpected event received in UdpSimpleSession */
       try {
         if (UdpSimpleConfig.debugOn)
           debug(":handle: Unexpected event. Forwarding it...");
         e.go();
       } catch (AppiaEventException ex) {}
     }
   }
   
   private void handlePDUSize(MaxPDUSizeEvent e) {
     
     if (UdpSimpleConfig.debugOn)
       debug(":handlePDUSize ");
     
     try {
       e.pduSize = param_MAX_UDPMSG_SIZE-MAX_UdpSimple_HEADERS;
       
       e.setDir(Direction.invert(e.getDir()));
       e.setSource(this);
       e.init();
       e.go();
     } catch (AppiaEventException ex) {
       ex.printStackTrace();
       System.err.println("Unexpected exception when forwarding " + "MaxPDUSize event in UDPSimple");
     }
   }
   
   private void handleDebug(Debug e) {
     
     if (UdpSimpleConfig.debugOn)
       debug(":handleDebug");
     
     int q = e.getQualifierMode();
     
     if (q == EventQualifier.ON) {
       debugOutput = new PrintStream(e.getOutput());
       debug("UDP: Debugging started");
     } else if (q == EventQualifier.OFF) {
       debugOutput = null;
     } else if (q == EventQualifier.NOTIFY) {
       printState(new PrintStream(e.getOutput()));
     }
     
     try {
       e.go();
     } catch (AppiaEventException ex) {}
   }
   
   private void printState(PrintStream out) {
     
     out.println("UdpSimpleSession state dumping:");
     if (sock != null)
       out.println("Local UDP port: " + sock.getLocalPort());
     Iterator iter=multicastReaders.keySet().iterator();
     while (iter.hasNext())
       out.println("Local Multicast address: " + ((InetSocketAddress)iter.next()));
     
     int nChannels = channels.size();
     out.println("Currently connected channels: " + nChannels);
     
     iter = channels.values().iterator();
     while (iter.hasNext()) {
       Channel c = (Channel) iter.next();
       out.println("Channel name: " + c.getChannelID() + " QoS: " + c.getQoS().getQoSID());
     }
     
     out.println("Debug output is currently " + (debugOutput == null ? "off" : "on"));
   }
   
         /*
          * Receives a register socket request. Only one of this events is
          * expected to be received by channel.
          *
          * @param e The socket description and the ID which will be used
          * to identify it.
          */
   
   private void handleRegisterSocket(RegisterSocketEvent e) {
     
     if (UdpSimpleConfig.debugOn)
       debug(":handleRegisterSocket");
     
                 /* if the socket is already binded then something is
                  * wrong. Keep existing information.
                  */
     if (sock != null) {
       reverseRegister(e, myAddress.getPort(), myAddress.getAddress(), true);
       return;
     }
     
     // Checks if address given is a local address
     if ((e.localHost != null) && !HostUtils.isLocalAddress(e.localHost)) {
     	reverseRegister(e, e.port, null, true);
     	return;
     }
     	
     if (newSock(e.port,e.localHost)) {
       reverseRegister(e, myAddress.getPort(), myAddress.getAddress(), false);
     } else {
       reverseRegister(e, e.port, null, true);
     }
   }
   
         /*
          * Receives a AppiaMulticastInit event.
          *
          * @param e The ipMulticast to make joinGroup.
          */
   
   private void handleMulticastInit(MulticastInitEvent e) {
     
     if (UdpSimpleConfig.debugOn)
       debug(":handleAppiaMulticastInit");
 
     if (!multicastReaders.containsKey(e.ipMulticast)) {
       /*creates a multicast socket and binds it to a specific port on the local host machine*/
       try {
         MulticastSocket multicastSock = new MulticastSocket(e.ipMulticast);
         
         if (UdpSimpleConfig.debugOn)
           debug(":handleAppiaMulticastInit: Socket Multicast created. Address: "  + e.ipMulticast);
         
         /*joins a multicast group*/
         multicastSock.joinGroup(((InetSocketAddress)e.ipMulticast).getAddress());
         
         //keeping the multicast address...
         ipMulticast = new InetSocketAddress(((InetSocketAddress)e.ipMulticast).getAddress(),((InetSocketAddress)e.ipMulticast).getPort());
         
         if (UdpSimpleConfig.debugOn)
           debug(":handleAppiaMulticastInit: Socket Multicast joined.");
         
         try {
         	multicastSock.setSoTimeout(param_SOTIMEOUT);
         } catch(SocketException se){
         	System.err.println("Unable to set SoTimeout value on UdpSimpleSession. Using default OS value.");
         	se.printStackTrace();
         }
 
         /* The socket is binded. Launch reader and return the event.*/
         UdpSimpleReader multicastReader = 
           new UdpSimpleReader(this, multicastSock, ipMulticast, e.fullDuplex ? null : myAddress);
         Thread thread = threadFactory.newThread(multicastReader,"MulticastReaderThread ["+ipMulticast+"]");
         multicastReader.setParentThread(thread);
         thread.start();
         
         multicastReaders.put(ipMulticast, multicastReader);
         
         /*forwarding the event*/
         e.error=false;
       } catch (IOException ex) {
         ex.printStackTrace();
         System.err.println("Error creating/joining the multicast socket");
         e.error=true;
       }
     } else {
       if (UdpSimpleConfig.debugOn)
         debug(":handleAppiaMulticastInit: Requested multicast socket already existed.");
     }
     
     try {
       e.setDir(Direction.invert(e.getDir()));
       e.setSource(this);
       e.init();
       e.go();
       if (UdpSimpleConfig.debugOn){
           debug(":handleAppiaMulticastInit: Returning multicastInit with error code: "+e.error);
           debug(":handleAppiaMulticastInit: Direction is "+(e.getDir() == Direction.DOWN? "DOWN":"UP"));
       }
     } catch (AppiaEventException ex) {
       ex.printStackTrace();
     }
   }
   
         /*
          * Receives sendable events from sessions above and puts them on
          * a socket.
          */
   
   private void handleSendable(SendableEvent e) {
     
     if (UdpSimpleConfig.debugOn)
       debug(":handleSendable: "+e);
     
     if (e.getDir() == Direction.DOWN) {
       formatAndSend(e);
     }
     
     /* Now that the packet is sent, event follows his way */
     try {
       e.go();
     } catch (AppiaEventException ex) {
       System.err.println("Event not initialized but tried to be " + "sent in UdpSimpleSession");
     }
   }
   
         /*
          * Handle of ChannelInit event. Keep tracks of which channels this
          * session is currently serving to learn where to route the
          * incoming messages. Notifies his layer.
          */
   private void handleChannelInit(ChannelInit e) {
     
     if (UdpSimpleConfig.debugOn)
       debug(":handleChannelInit from channel: "+e.getChannel().getChannelID());
     
     channels.put(new Integer(e.getChannel().getChannelID().hashCode()), e.getChannel());
     
     try {
       e.go();
     } catch (AppiaEventException ex) {
       System.err.println("Event not initialized exception in " + "UdpSimpleSession");
     }
   }
   
         /*
          * Remove the channel from the vector.
          */
   private void handleChannelClose(ChannelClose e) {
     
     if (UdpSimpleConfig.debugOn)
       debug(":handleChannelClose: Channel " + e.getChannel().getChannelID() + " closed");    
     
     // Access to vectors is synchronized
     channels.remove(new Integer(e.getChannel().getChannelID().hashCode()));
     try {
       e.go();
     } catch (AppiaEventException ex) {
       System.err.println("Unexpected exception when " + "forwarding ChannelClose event");
     }
     
     if (channels.isEmpty()) {
       // Terminating 
       sockReader.terminate();
       
       Iterator iter=multicastReaders.values().iterator();
       while (iter.hasNext()) {
         UdpSimpleReader reader=(UdpSimpleReader)iter.next();
         reader.terminate();
       }
     }
   }
   
   /**
    * int serialization.
    */
   byte[] intToByteArray(int i) {
     byte[] ret = new byte[4];
     
     ret[0] = (byte) ((i & 0xff000000) >> 24);
     ret[1] = (byte) ((i & 0x00ff0000) >> 16);
     ret[2] = (byte) ((i & 0x0000ff00) >> 8);
     ret[3] = (byte) (i & 0x000000ff);
     
     return ret;
   }
   
   void intToByteArray(int i, byte[] a, int o) {
     a[o + 0] = (byte) ((i & 0xff000000) >> 24);
     a[o + 1] = (byte) ((i & 0x00ff0000) >> 16);
     a[o + 2] = (byte) ((i & 0x0000ff00) >> 8);
     a[o + 3] = (byte) (i & 0x000000ff);
   }
   
   /**
    * int deserialization.
    */
   int byteArrayToInt(byte[] b, int off) {
     int ret = 0;
     
     ret |= b[off] << 24;
     ret |= (b[off + 1] << 24) >>> 8; // must be done this way because of
     ret |= (b[off + 2] << 24) >>> 16; // java's sign extension of <<
     ret |= (b[off + 3] << 24) >>> 24;
     
     return ret;
   }
   
   private boolean newSock(int port, InetAddress addr) {
     if (addr == null) {
       if (param_LOCAL_ADDRESS == null)
         addr=HostUtils.getLocalAddress();
       else
         addr=param_LOCAL_ADDRESS;
     }
   		
     if (port == RegisterSocketEvent.FIRST_AVAILABLE) {
       /*first available port*/
       try {
         sock = new DatagramSocket(0,addr);
       } catch (SocketException ex) {
         ex.printStackTrace();
         return false;
       }
     } else if (port == RegisterSocketEvent.RANDOMLY_AVAILABLE) {
       /*chooses a random port*/
       Random random = new Random();
       
       boolean sucess = false;
       
       /*verifies if the random port is a valid one*/
       while (!sucess) {
         port = Math.abs(random.nextInt() % Short.MAX_VALUE);
         /* Open Socket with any port*/
         try {
           sock = new DatagramSocket(port,addr);
           sucess = true;
         } catch (IllegalArgumentException ex) {} catch (SocketException se) {}
       }
       
     } else { /*Regular RegisterSocketEvent */
       
       /* Open the specified socket (if possible) */
       try {
         sock = new DatagramSocket(port,addr);
       }
       /* Socket exception. Possibly the socket is already bound.
        Return the event up to notify that the command could not
        be issued.
        */
       catch (SocketException se) {
         return false;
       } catch (IllegalArgumentException ex) {
         return false;
       }
     }
     
     // Determine local address
     myAddress = new InetSocketAddress(sock.getLocalAddress(),sock.getLocalPort());
     
     try {
 		sock.setSoTimeout(param_SOTIMEOUT);
 	} catch (SocketException e) {
     	System.err.println("Unable to set SoTimeout value on UdpSimpleSession. Using default OS value.");
 		e.printStackTrace();
 	}
 
     /* The socket is binded. Launch reader*/
 	//FIXME
     sockReader = new UdpSimpleReader(this, sock, myAddress);
     Thread t = threadFactory.newThread(sockReader,"UdpSimpleReader ["+myAddress+"]");
     sockReader.setParentThread(t);
     t.start();
     
     return true;
   }
   
   /*
    * Event serialization and sending it to socket (int+className+int+channelName+message)
    */
   
   private void formatAndSend(SendableEvent e) {
     
     /* Event Class name */
     try {
       if (sock == null) {
         if (!newSock(RegisterSocketEvent.FIRST_AVAILABLE,null))
           throw new IOException("Impossible to create new socket.");
       }
       
       Message msg = e.getMessage();
       MsgBuffer mbuf = new MsgBuffer();
       
       byte[] eventType = e.getClass().getName().getBytes("ISO-8859-1");
       int channelHash = e.getChannel().getChannelID().hashCode();
       
       mbuf.len = 4;
       msg.push(mbuf);
       intToByteArray(channelHash, mbuf.data, mbuf.off);
       
       mbuf.len = eventType.length;
       msg.push(mbuf);
       System.arraycopy(eventType, 0, mbuf.data, mbuf.off, mbuf.len);
       
       mbuf.len = 4;
       msg.push(mbuf);
       intToByteArray(eventType.length, mbuf.data, mbuf.off);
       
       if (msg.length() > param_MAX_UDPMSG_SIZE)
         throw new IOException("Message length to great, may be truncated");
       
       /* Create the packet and send it */
       byte[] bytes = msg.toByteArray();
       
       if ((e.dest instanceof AppiaMulticast)
           && (((AppiaMulticast) e.dest).getMulticastAddress() == null)) {
         
         Object[] dests = ((AppiaMulticast) e.dest).getDestinations();
         
         if (dests == null) {
           System.err.println(
           "UdpSimpleSession: Destinations field of AppiaMulticast empty. Not sending event " + e);
           return;
         }
         
         DatagramPacket dp = new DatagramPacket(bytes, bytes.length);
         
         for (int i = 0; i < dests.length; i++) {
           if (dests[i] instanceof InetSocketAddress) {
             
             dp.setAddress(((InetSocketAddress) dests[i]).getAddress());
             dp.setPort(((InetSocketAddress) dests[i]).getPort());
             sock.send(dp);
             
             if (UdpSimpleConfig.debugOn)
               debug(":formatAndSend: Multicast emulation: " + dp.getLength() 
                   + " bytes datagram sent to " + dp.getAddress().getHostAddress() 
                   + " (port " + dp.getPort() + ")");
           } else
             System.err.println("UdpSimpleSession: Wrong destination address type in event " + e);
         }
       } else {
         InetSocketAddress dest = null;
         if (e.dest instanceof InetSocketAddress) {
           dest = (InetSocketAddress) e.dest;
         } else if (e.dest instanceof AppiaMulticast) {
           Object aux=((AppiaMulticast) e.dest).getMulticastAddress();
           if (aux instanceof InetSocketAddress) {
             dest = (InetSocketAddress)aux;
             if (!dest.getAddress().isMulticastAddress()) {
               System.err.println("UdpSimpleSession: Not a multicast address in AppiaMulticast of event " + e);
               return;
             }
           } else {
             System.err.println("UdpSimpleSession: Wrong multicast address type in event " + e);
             return;
           }
         } else {
           System.err.println("UdpSimpleSession: Wrong destination address type in event " + e);
           return;
         }
         
         DatagramPacket dp = new DatagramPacket(bytes, bytes.length, dest.getAddress(), dest.getPort());
         
         sock.send(dp);
         
         if (UdpSimpleConfig.debugOn)
           debug(":formatAndSend: "+dp.getLength()+" bytes datagram sent to "
               + dp.getAddress().getHostAddress() + " (port " + dp.getPort() + ")");
       }
     } catch (IOException ex) {
      if (UdpSimpleConfig.debugOn)
          ex.printStackTrace();
       /* Couldn't send message to socket. */
       try {
         SendableNotDeliveredEvent snd = new SendableNotDeliveredEvent(e.getChannel(), this, e);
         snd.go();
         if (UdpSimpleConfig.debugOn)
           debug(":formatAndSend: IOException when sending Datagram to socket. "
               + "Inserting SendableNotDeliveredEvent in the channel.");
         
       } catch (AppiaEventException ex1) { /* Possible exception: Unwanted Event */
         ex.printStackTrace();
       }
     }
   }
         /* Auxiliary class.
          *
          * This is the class responsible for blocking on a socket waiting for
          * datagrams to come. Incoming datagrams are transformed in Events and
          * wait that the main thread pools them. The main thread is notified by
          * an AsyncEvent.
          *
          */
   class UdpSimpleReader implements Runnable {
     
       private static final int MAX_BUFFER_SIZE =65536; 
       
     private DatagramSocket sock = null;
     private InetSocketAddress dest = null;
     private InetSocketAddress ignoreSource = null;
     private UdpSimpleSession parentSession = null;
     private Thread parentThread = null;
     
     private byte[] b = new byte[MAX_BUFFER_SIZE];
     private MsgBuffer mbuf = new MsgBuffer();
     
     private boolean terminate=false;
     
     /**
      * Waits on a DatagramSocket - point-to-point communication
      */
     public UdpSimpleReader(UdpSimpleSession parentSession, DatagramSocket s, InetSocketAddress dest) {
       this.parentSession = parentSession;
       this.sock = s;
       this.dest = dest;
     }
     
     void setParentThread(Thread t){
     		this.parentThread = t;
     }
     
     /**
      * Waits on a MulticastSocket - multicast communication
      */
     public UdpSimpleReader(UdpSimpleSession parentSession, DatagramSocket s, InetSocketAddress dest, InetSocketAddress ignore) {
       this.parentSession = parentSession;
       this.sock = s;
       this.dest = dest;
       this.ignoreSource = ignore;
     }
     
     public InetSocketAddress getDest() {
       return dest;
     }
     
     public void terminate() {
       synchronized (this) {
         terminate=true;
       }
       parentThread.interrupt();
     }
     
     public void run() {
       boolean running=true;
       
       DatagramPacket msg = new DatagramPacket(b, b.length);
       
       if (UdpSimpleConfig.debugReaderOn)
         debug("Reader running (Multicast="+(sock instanceof MulticastSocket)+").");
       
       while (running) {
         try {
           sock.receive(msg);
           
           if (UdpSimpleConfig.debugReaderOn)
             debug(":run: PtP datagram received. Size = " + msg.getLength());
           
           if ((ignoreSource != null)
           && (ignoreSource.getPort() == msg.getPort())
           && (ignoreSource.getAddress().equals(msg.getAddress()))) {
             if (UdpSimpleConfig.debugReaderOn)
               debug(":run: Ignored Last received message");
           } else
             receiveFormatSend(msg);
           
         } catch(SocketTimeoutException ste){
 //        	ste.printStackTrace();
         } catch (IOException e) {
           System.err.println("[UdpSimpleSession:reader:run] IOException: " + e.getMessage());
         }
         
         synchronized (this) {
           if (terminate)
             running=false;
         }
       }
     }
     
                 /* Event deserialization. Returns the event or null if something wrong
                  * happened.
                  */
     private void receiveFormatSend(DatagramPacket p) {
       
       byte[] data = new byte[p.getLength()];
       System.arraycopy(p.getData(), p.getOffset(), data, 0, p.getLength());
       SendableEvent e = null;
       Message msg = null;
       
       try {
         /* Extract event class name */
         //size of class name
         int sLength = byteArrayToInt(data, 0);
         //the class name
         String className = new String(data, 4, sLength, "ISO-8859-1");
         
         /* Create event */
         Class c = Class.forName(className);
         if (UdpSimpleConfig.debugReaderOn) {
           if (sock != null)
             debug(":receiveAndFormat: Reader, creating "+className+" event.");
         }
         e = (SendableEvent) c.newInstance();
         msg = e.getMessage();
         msg.setByteArray(data, 4 + sLength, data.length - (4 + sLength));
         
         /* Extract channel hash and put event in it*/
         mbuf.len = 4;
         msg.pop(mbuf);
         int channelHash = byteArrayToInt(mbuf.data, mbuf.off);
         Channel msgChannel = (Channel) parentSession.channels.get(new Integer(channelHash));
         
         /* If channel does not exist, discard message */
         if (msgChannel == null) {
         	if (UdpSimpleConfig.debugReaderOn)
         		System.err.println(this.getClass().getName()+
         				": channel does not exist. message will be discarded. "
         				+ "hash="+channelHash
         				+ " "
         				+ e);
         	return;
         }
         
         if (UdpSimpleConfig.debugReaderOn)
           debug(":receiveAndFormat: "+msgChannel.getChannelID()+" ("+channelHash+")");
 
         
         /* Extract the addresses and put them on the event */
         
         //msg's source, futurely change udpsimple to common
         InetSocketAddress addr = new InetSocketAddress(p.getAddress(),p.getPort());
         e.source = addr;
         
         //msg's destination
         e.dest = dest;
         
         // send event
         e.asyncGo(msgChannel, Direction.UP);
         
       } catch (Exception ex) {
         if (UdpSimpleConfig.debugReaderOn) {
           ex.printStackTrace(debugOutput);
           debug("Exception catched while processing message from "+p.getAddress().getHostName()+":"+p.getPort()+". Continued operation.");
         }
       }
     }
     private void debug(String s) {
       if (debugOutput != null)
         debugOutput.println("["+parentSession.getClass().getName()+":Reader]"+s);
     }
   }
   
   private void reverseRegister(RegisterSocketEvent e, int port, InetAddress localHost, boolean error) {
     try {
       e.setSource(this);
       e.setDir(Direction.invert(e.getDir()));
       e.port=port;
       e.localHost=localHost;
       e.error = error;
       e.init();
       e.go();
     } catch (AppiaEventException ex) {
       ex.printStackTrace();
     }
   }
   
   private void debug(String s) {
     if (debugOutput != null)
       debugOutput.println("["+this.getClass().getName()+"]"+s);
   }
 }
