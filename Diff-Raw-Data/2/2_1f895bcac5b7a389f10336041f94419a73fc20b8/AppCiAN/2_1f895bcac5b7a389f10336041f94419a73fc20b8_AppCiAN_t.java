 package ext.jist.swans.app;
 
 import java.io.File;
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import jist.runtime.JistAPI;
 import jist.runtime.JistAPI.Continuation;
 import jist.swans.Constants;
 import jist.swans.app.AppInterface;
 import jist.swans.misc.Message;
 import jist.swans.misc.MessageBytes;
 import jist.swans.net.NetAddress;
 import jist.swans.net.NetInterface;
 import jist.swans.trans.TransInterface.SocketHandler;
 import jist.swans.trans.TransInterface.TransTcpInterface;
 import jist.swans.trans.TransInterface.TransUdpInterface;
 import jist.swans.trans.TransTcp;
 import jist.swans.trans.TransUdp;
 import jist.swans.trans.TransUdp.UdpMessage;
 import ext.util.stats.DucksCompositionStats;
 
 public class AppCiAN implements AppInterface, AppInterface.TcpApp, AppInterface.UdpApp, SocketHandler
 {
     // network entity.
     private NetInterface              netEntity;
 
     private TransTcp                  transTCP;
 
     private TransUdp                  transUDP;
 
     // self-referencing proxy entity.
     private Object                    self;
 
     private int                       nodeId;
 
     // composition stats accumulator
     private DucksCompositionStats     compositionStats;
 
     private String[]                  args;
 
     private BlockingQueue<UdpMessage> UDPMessageQueue;
     private int                       multicastPort;
 
     public AppCiAN(int nodeId, DucksCompositionStats compositionStats, String[] args) {
         this.nodeId = nodeId;
         this.compositionStats = compositionStats;
         this.args = args;
         // init self reference
         this.self = JistAPI.proxyMany(this, new Class[] { AppInterface.class, AppInterface.TcpApp.class,
                 AppInterface.UdpApp.class });
 
         this.transTCP = new TransTcp();
         this.transUDP = new TransUdp();
 
         this.UDPMessageQueue = new LinkedBlockingQueue<UdpMessage>();
     }
 
     public int getNodeId() {
         return nodeId;
     }
 
     public InetAddress getInetAddress() {
         return this.netEntity.getAddress().getIP();
     }
 
     public void addUDPHandler(int port) {
         transUDP.addSocketHandler(port, this);
         this.multicastPort = port;
     }
 
     public void removeUDPHandler(int port) {
         transUDP.delSocketHandler(port);
         this.multicastPort = 0;
     }
 
     public void sendMulticastPacket(byte[] packet) {
         transUDP.send(new MessageBytes(packet), NetAddress.ANY, multicastPort, multicastPort,
                 Constants.NET_PRIORITY_NORMAL);
     }
 
     /**
      * Handler for new UDP packets
      * 
      * @see receiveUDPPacket()
      */
     public void receive(Message msg, NetAddress src, int srcPort) throws Continuation {
         UdpMessage uMsg;
         if (msg instanceof UdpMessage) {
             uMsg = (UdpMessage) msg;
         } else {
             // We can safely say it is a UDP message coming from the correct
             // port since we only use UDP for beaconning (multicast)
             uMsg = new UdpMessage(srcPort, multicastPort, msg);
         }
         UDPMessageQueue.add(uMsg);
     }
 
     /**
      * Blocks until a new UDP packet arrives and retrieves it
      * 
      * @return the payload of the UDP packet
      */
     public byte[] receiveUDPPacket() {
         byte[] packet = new byte[1500];
         try {
             UdpMessage msg = UDPMessageQueue.take();
             msg.getPayload().getBytes(packet, 0);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
 
         return packet;
     }
 
     public void run() {
         run(this.args);
     }
 
     public void run(String[] args) {
         compositionStats.incrementNumReq();
 
         // Starting a new CiAN application isolated from the others
         new CiANThread(this, args).start();
     }
 
     /**
      * Set network entity.
      * 
      * @param netEntity
      *            network entity
      */
     public void setNetEntity(NetInterface netEntity) {
         this.netEntity = netEntity;
         this.transTCP.setNetEntity(netEntity);
         this.transUDP.setNetEntity(netEntity);
     }
 
     public AppInterface getAppProxy() {
         return (AppInterface) self;
     }
 
     public AppInterface.TcpApp getTcpAppProxy() {
         return (AppInterface.TcpApp) self;
     }
 
     public AppInterface.UdpApp getUdpAppProxy() {
         return (AppInterface.UdpApp) self;
     }
 
     public TransTcpInterface getTcpEntity() throws Continuation {
         return transTCP.getProxy();
     }
 
     public TransUdpInterface getUdpEntity() throws Continuation {
         return transUDP.getProxy();
     }
 
     class CiANThread extends Thread
     {
         private String[] args;
         private AppCiAN  parent;
 
         public CiANThread(AppCiAN parent, String[] args) {
             super("CiANThread for node " + parent.getNodeId());
             this.args = args;
             this.parent = parent;
         }
 
         @Override
         public void run() {
             // Unfortunately we have to do this little hack in order for every
             // node to be isolated from the others.
             // IMPORTANT: it is vital that CiAN.jar is NOT in the classpath!
             try {
                 // We need a new ClassLoader for each node we are creating
                 ClassLoader loader = new URLClassLoader(new URL[] { new File("CiAN/CiAN.jar").toURI().toURL() });
 
                 // Loading CiAN class this way ensures us that each node is
                 // isolated from each other
                Class<?> c = loader.loadClass("system.CiAN");
 
                 // Add a dependency to this node's application into CiAN
                 Method extToolSetter = c.getDeclaredMethod("setExternalTool", Object.class);
                 extToolSetter.invoke(null, parent);
 
                 // Finally we can start CiAN
                 Method main = c.getDeclaredMethod("main", this.args.getClass());
                 main.invoke(null, (Object[]) this.args);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
 }
