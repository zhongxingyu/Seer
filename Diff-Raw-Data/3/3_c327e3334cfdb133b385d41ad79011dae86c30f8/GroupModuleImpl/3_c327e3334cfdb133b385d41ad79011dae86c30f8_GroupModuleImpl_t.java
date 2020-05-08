 package se.umu.cs.jsgajn.gcom.groupmanagement;
 
 import java.io.IOException;
 import java.rmi.AlreadyBoundException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.util.Properties;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import se.umu.cs.jsgajn.gcom.Client;
 import se.umu.cs.jsgajn.gcom.debug.Debugger;
 import se.umu.cs.jsgajn.gcom.groupcommunication.CommunicationModule;
 import se.umu.cs.jsgajn.gcom.groupcommunication.CommunicationsModuleImpl;
 import se.umu.cs.jsgajn.gcom.groupcommunication.Message;
 import se.umu.cs.jsgajn.gcom.groupcommunication.MessageImpl;
 import se.umu.cs.jsgajn.gcom.groupcommunication.MessageType;
 import se.umu.cs.jsgajn.gcom.groupcommunication.Multicast;
 import se.umu.cs.jsgajn.gcom.groupcommunication.MulticastType;
 import se.umu.cs.jsgajn.gcom.groupcommunication.Multicasts;
 import se.umu.cs.jsgajn.gcom.messageordering.Ordering;
 import se.umu.cs.jsgajn.gcom.messageordering.OrderingModule;
 import se.umu.cs.jsgajn.gcom.messageordering.OrderingModuleImpl;
 import se.umu.cs.jsgajn.gcom.messageordering.OrderingType;
 import se.umu.cs.jsgajn.gcom.messageordering.Orderings;
 
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * author dit06ajn, dit06jsg
  */
 public class GroupModuleImpl implements GroupModule {
     private static final Logger logger = LoggerFactory.getLogger(GroupModuleImpl.class);
     private static final Debugger debugger = Debugger.getDebugger();
     
     private Client client;
     private String groupName;
     private GNS gns;
     private GroupView groupView;
     private OrderingModule orderingModule;
     private CommunicationModule communicationModule;
     private GroupLeader gl;
     //private Receiver receiver;
 
     // Own group member
     private GroupMember groupMember;
 
     // Queue to contain newly delivered messages
     private LinkedBlockingQueue<Message> receiveQueue;
 
     private Thread messageReceiverThread;
     private boolean running;
     
     public GroupModuleImpl(Client client, String gnsHost, int gnsPort, String groupName)
         throws RemoteException, AlreadyBoundException, NotBoundException {
         this(client, gnsHost, gnsPort, groupName, Registry.REGISTRY_PORT);
     }
     
     /**
      * Implementations responsible for management of group, communication with
      * group and ordering of messages. Clients should use this class to send
      * messages and receive messages with a group.
      *
      * @param gnsHost GNS host
      * @param gnsPort GNS port
      * @param groupName Group name to connect to
      * @throws RemoteException If GNS throws exception
      * @throws AlreadyBoundException If it's not possible to bind this to own register.
      * @throws NotBoundException If GNS stub is not found in GNS register.
      */
     public GroupModuleImpl(Client client, String gnsHost, int gnsPort,
                            String groupName, int clientPort)
         throws RemoteException, AlreadyBoundException, NotBoundException {
         this.client = client;
         this.receiveQueue = new LinkedBlockingQueue<Message>();
 
         this.orderingModule = new OrderingModuleImpl(this);
         this.communicationModule = new CommunicationsModuleImpl(this, clientPort);
         this.communicationModule.setOrderingModule(this.orderingModule);
         this.orderingModule.setCommunicationsModule(this.communicationModule);
 
         this.groupMember = new GroupMember(communicationModule.getReceiver());
        // Temp groupview
         this.groupView =  new GroupViewImpl(groupName, this.groupMember);
         this.groupName = groupName;
         GroupSettings gs = initGroupSettings(groupMember, groupName);
 
         this.gns = getGNS(gnsHost, gnsPort);
         // TODO: what happens if another client connects directly after this
         //       client. Not all threads are started.
         gs = gns.connect(gs);
         Ordering o = Orderings.newInstance(gs.getOrderingType());
         this.orderingModule.setOrdering(o);
         Multicast m = Multicasts.newInstance(gs.getMulticastType());
         this.communicationModule.setMulticastMethod(m);
 
         // Start modules
         this.orderingModule.start();
         this.communicationModule.start();
 
         if (gs.isNew()) { // Group is empty I am leader
             logger.debug("Got new group from GNS, this member is leader");
             this.gl = new GroupLeaderImpl();
            debugger.groupChange(this.groupView);
         } else {
             logger.debug("Got existing group from DNS, sending join message");
             MessageImpl joinMessage =
                 new MessageImpl(this.groupMember,
                                 MessageType.JOIN, PID, groupView.getID());
             gs.getLeader().getReceiver().receive(joinMessage);
         }
 
         this.messageReceiverThread = new Thread(new MessageReceiver(), "GroupModule Thread");
        
         start();
     }
 
     public void start() {
         logger.debug("Starting GroupModuleImpl");
         //Module is started in constructor
         this.running = true;
         this.messageReceiverThread.start();
     }
 
     public void stop() {
         logger.debug("Stopping GroupModuleImpl");
         this.running = false;
         this.communicationModule.stop();
         this.orderingModule.stop();
     }
 
     private GroupSettings initGroupSettings(GroupMember leader, String groupName) {
         Properties prop = new Properties();
         Properties sys = System.getProperties();
         String ordering = "FIFO";
         String multicastMethod = "BASIC_MULTICAST";
         try {
             prop.load(this.getClass().getResourceAsStream("/application.properties"));
 
             // Get ordering
             if (sys.containsKey("gcom.ordering")) {
                 ordering = sys.getProperty("gcom.ordering");
             } else if (prop.containsKey("gcom.ordering")) {
                 ordering = prop.getProperty("gcom.ordering");
             }
 
             // Get multicastMethod
             if (sys.containsKey("gcom.multicast")) {
                 multicastMethod = sys.getProperty("gcom.multicast");
             } else if (prop.containsKey("gcom.multicast")) {
                 multicastMethod = prop.getProperty("gcom.multicast");
             }
         } catch (IOException e) {
             logger.warn("application.properties not found");
         }
 
         OrderingType otype = OrderingType.valueOf(ordering);
         MulticastType mtype = MulticastType.valueOf(multicastMethod);
         if (otype == null || mtype == null) {
             throw new Error("Coudn't initialize groupsettings");
         }
         logger.debug("Init groupsettings: " + mtype + ", " + otype);
         return new GroupSettings(groupName, leader, mtype, otype);
     }
 
 
 
     /**
      * Will send package Object in appropriate Message and send it to every
      * group member.
      *
      * @param clientMessage The Object to send.
      */
     public void send(Object clientMessage) {
         Message m = new MessageImpl(clientMessage,
                                     MessageType.CLIENTMESSAGE, PID, groupView.getID());
         send(m, this.groupView);
     }
 
     /**
      * Will send message to {@link OrderingModule} -> {@link CommunicationModule} ->
      * every member of the {@link GroupView}.
      *
      * @param m The Message to send.
      * @param g The GroupView Message should be sent to.
      */
     public void send(Message m, GroupView g) {
         orderingModule.send(m, this.groupView);
     }
 
     public GroupView getGroupView() {
         return groupView;
     }
 
     /** GroupLeader ************/
     private class GroupLeaderImpl implements GroupLeader {
         public void removeFromGroup(GroupMember member) {
             throw new Error("TODO: not implemented");
         }
 
         public void addMemberToGroup(GroupMember member) {
             // TODO: fix this
             groupView.add(member);
 
             // Multicast new groupView
             orderingModule.send(new MessageImpl(groupView,
                                                 MessageType.GROUPCHANGE, PID, groupView.getID()), groupView);
         }
     }
 
     private class MessageReceiver implements Runnable {
         public void run() {
             while (running) {
                 try {
                     handleDelivered(receiveQueue.take());
                 } catch (InterruptedException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
         }
 
         private void handleDelivered(Message m) {
             System.out.println("Got message!");
             MessageType type = m.getMessageType();
             switch (type) {
             case GROUPCHANGE:
                 System.out.println("We have a new member!!");
                 groupView = (GroupView) m.getMessage();
                 debugger.groupChange(groupView);
                 break;
             case CLIENTMESSAGE:
                 client.deliver(m.getMessage());
                 break;
             case MEMBERCRASH:
                 handelCrash((GroupMember)(m.getMessage()));
                 break;
             case JOIN:
                 if (gl == null) {
                     System.err.println("Got join message but I'm not leader");
                 } else {
                     gl.addMemberToGroup((GroupMember) m.getMessage());
                 }
                 break;
             default:
                 System.out.println("error i header");
             }
         }
 
         private void handelCrash(GroupMember m) {
             // TODO: Election om de e ledaren annars groupchange
         }
     }
 
     public void deliver(Message m) {
         try {
             debugger.messageDelivered(m);
             receiveQueue.put(m);
         } catch (InterruptedException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     /**
      * @param host Host to GNS
      * @param port Port to GNS
      * @return GNS stub
      * @throws RemoteException If GNS throws exception
      * @throws NotBoundException If GNS stub can't be found.
      */
     private GNS getGNS(String host, int port) throws RemoteException,
                                                      NotBoundException {
         Registry gnsReg = LocateRegistry.getRegistry(host, port);
         return (GNS) gnsReg.lookup(GNS.STUB_NAME);
     }
 }
