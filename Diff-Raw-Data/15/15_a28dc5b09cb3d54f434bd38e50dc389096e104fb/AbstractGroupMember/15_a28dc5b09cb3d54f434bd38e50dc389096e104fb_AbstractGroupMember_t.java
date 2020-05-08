 package se.umu.cs.jsgajn.gcom.groupmanagement;
 
 import java.rmi.AlreadyBoundException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 
 import se.umu.cs.jsgajn.gcom.groupcommunication.Message;
 
 public abstract class AbstractGroupMember implements GroupMember {
    private String groupName;
     private GroupMember stub;
     private GNS gns;
     private Group group;
     private GroupLeader groupLeader;
 
     /**
      * @param gnsHost GNS host
      * @param gnsPort GNS port
      * @param groupName Group name to connect to
      * @throws RemoteException If GNS throws exception
      * @throws AlreadyBoundException If it's not possible to bind this to own register.
      * @throws NotBoundException If GNS stub is not found in GNS register.
      */
     public AbstractGroupMember(String gnsHost, int gnsPort, String groupName)
             throws RemoteException, AlreadyBoundException, NotBoundException {
        this.groupName = groupName;
         this.stub = (GroupMember) UnicastRemoteObject.exportObject(this, 0);
         Registry registry = LocateRegistry.createRegistry(1099); // TODO: change 1099
         registry.bind(GroupMember.STUB_NAME, stub);
         this.gns = connectToGns(gnsHost, gnsPort);
         this.groupLeader = gns.connect(stub, groupName);
        this.group = groupLeader.joinGroup(stub);
     }
 
     /**
      * @param host Host to GNS
      * @param port Port to GNS
      * @return GNS stub
      * @throws RemoteException If GNS throws exception
      * @throws NotBoundException If GNS stub can't be found.
      */
     private GNS connectToGns(String host, int port) throws RemoteException,
             NotBoundException {
         Registry gnsReg = LocateRegistry.getRegistry(host, port);
         return (GNS) gnsReg.lookup(GNS.STUB_NAME);
     }
 
     /**
      * Send message to all members of the group.
      * TODO: move method to interface or Ordering-layer.
      * @param m The message to send.
      */
     public void multicast(Message<?> m) {
         for (GroupMember member : this.group) {
             try {
                 member.receive(m);
             } catch (RemoteException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 
     public abstract void received(Message<?> m);
     
     /*
      * (non-Javadoc)
      * 
      * @see se.umu.cs.jsgajn.gcom.groupmanagement.GroupMember#receive(se.umu.cs.jsgajn.gcom.groupcommunication.Message)
      */
     public void receive(Message<?> m) {
         // TODO: implement ordering and delivering and stuff.
         System.out.println(m.getMessage());
     }
 
     /** GroupLeader ************/
     public boolean removeFromGroup(GroupMember member) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public Group joinGroup(GroupMember member) {
        if (member == stub) {
            System.out.println("I am my on master?");
            return new GroupImpl(this.groupName, stub);
        }
        System.out.println("Group exists you got it");
        return this.group;
     }
 }
