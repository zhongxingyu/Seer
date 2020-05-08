 /*
  * Routing Server accepts updates and passes them to Routing Updater
  */
 package routerprogram;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 /**
  *
  * @author bmulvihill
  */
 public class RoutingServer extends Thread {
     private int serverPort = Config.getInstance().serverPort;
     
     public RoutingServer(){
         Config config = Config.getInstance();
         serverPort = config.serverPort;
     }
     
     @Override
     public void run(){
         try{  
             ServerSocket welcomeSocket = new ServerSocket(serverPort);   
             System.out.println("IP Server Receiver listening on port " + serverPort + "... ... ...");
             while(true) { 
                     Socket clientSocket = welcomeSocket.accept(); 
                     Connection c = new Connection(clientSocket); 
             } 
 	} 
 	catch(IOException e) {
                 Logger.log(e.getMessage());
 		System.out.println("Listen :"+e.getMessage());
         } 
     }
 }
 
 class Connection extends Thread { 
 	DataInputStream input; 
 	DataOutputStream output; 
 	Socket clientSocket; 
 	Config config = Config.getInstance();
         PacketList pl = PacketList.getInstance();
         
         
 	public Connection (Socket aClientSocket) { 
 		try { 
                     clientSocket = aClientSocket; 
                     input = new DataInputStream( clientSocket.getInputStream()); 
                     output =new DataOutputStream( clientSocket.getOutputStream()); 
                     this.start(); 
                 } 
                     catch(IOException e) {
                         Logger.log(e.getMessage());
                         System.out.println("Connection: "+e.getMessage());
 		} 
 	  } 
 
 	  public void run() { 
 		try { 		                                
                         ObjectInputStream ois = new ObjectInputStream(input);  
                         String originIP = clientSocket.getRemoteSocketAddress().toString().split(":")[0].substring(1);
                         LinkStatePacket lsp = (LinkStatePacket)ois.readObject();
                         if (lsp!=null && !pl.exists(lsp)){
                             pl.add(lsp);
                             Logger.log("Packet received: " + lsp.ownerIP + " | Seq Num :" + lsp.seqNum);
                             System.out.println("Packet received: " + lsp.ownerIP + " | Seq Num :" + lsp.seqNum);      
                             RoutingUpdater ru = new RoutingUpdater(lsp, originIP);      
                         }  
                         input.close();    
 			} 
                         catch(ClassNotFoundException e){
                             Logger.log(e.getMessage());
                             System.out.println("Class Not Found:"+e.getMessage());  
                         }
 			catch(EOFException e) {
                             Logger.log(e.getMessage());
                             System.out.println("EOF:"+e.getMessage()); 
                         } 
 			catch(IOException e) {
                             Logger.log(e.getMessage());
                             System.out.println("IO:"+e.getMessage());
                         }  
 			finally { 
 			  try { 
                             clientSocket.close();
 			  }
 			  catch (IOException e){
                               Logger.log(e.getMessage());
                           }
 			}
 		}
 }
 
 /**
  * Data Structure to hold all received packets
 * Move to Router Program? More to do with RouterUpdater
  * @author bmulvihill
  */
 class PacketList{
     private static PacketList instance = null;
     private ArrayList<LinkStatePacket> received = new ArrayList();
     protected PacketList() {}
     
     public static PacketList getInstance() {
       if(instance == null) {
          instance = new PacketList();
       }
       return instance;
     }
     
    protected ArrayList<LinkStatePacket> getList(){
      return received;  
    }
    
     protected synchronized void add(LinkStatePacket lsp){
         received.add(lsp);
     }
     
     protected void remove(LinkStatePacket lsp){
         received.remove(lsp);
     }
     
     protected Boolean exists(LinkStatePacket lsp){
         for (Iterator<LinkStatePacket> it = received.iterator(); it.hasNext(); ) {
             LinkStatePacket l = it.next();
             if(l.ownerIP.equals(lsp.ownerIP) && l.seqNum == lsp.seqNum){
                //System.out.println("Duplicate Packet , not forwarding: "+ lsp.ownerIP + " | " + lsp.seqNum);
                 return true;
             }
         }
         return false;
     }
     
     //decrement the TTL on all Packets
     protected void decrement(){
         for (Iterator<LinkStatePacket> it = received.iterator(); it.hasNext(); ) {
             LinkStatePacket lsp = it.next();
             lsp.TTL = lsp.TTL - 1;
             Logger.log("Time to Live for Packet: " + lsp.seqNum + " from " + lsp.ownerIP + " | " + lsp.TTL);
             if (lsp.TTL == 0) {
                 Logger.log("TTL Expired for Packet: " + lsp.seqNum + " from " + lsp.ownerIP);
                 it.remove();
             }
         }
         
     }
    
 }
