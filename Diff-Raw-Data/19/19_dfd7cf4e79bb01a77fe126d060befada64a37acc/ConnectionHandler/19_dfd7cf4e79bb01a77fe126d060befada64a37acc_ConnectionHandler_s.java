 import java.io.IOException;
 import java.io.StringReader;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.util.ArrayList;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 
 public class ConnectionHandler implements Runnable {
 
     private boolean shouldRun = true;
     private Config conf;
     private int bufferSize = 2048;
     private MembershipList list;
 
     public ConnectionHandler(MembershipList list, Config conf) {
         this.list = list;
         this.conf = conf;
     }
     
     public ConnectionHandler(MembershipList list, Config conf, int bufferSize) {
         this.list = list;
     	this.conf = conf;
     	this.bufferSize = bufferSize;
     }
 
     public void kill() {
         this.shouldRun = false;
     }
 
     public void run() {
         int port = conf.intFor("contactPort");
 
     	DatagramSocket rcvSocket = null;
         try {
             rcvSocket = new DatagramSocket(port);
         } catch (SocketException e1) {
             System.out.println("Can't listen on port "+port);
         }
     	byte[] buffer = new byte[bufferSize];
     	DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
         System.out.println("Waiting for UDP packets: Started");
 
         while(shouldRun) {
             try {
                 rcvSocket.receive(packet);
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
         	
             String msg = new String(buffer, 0, packet.getLength());
             System.out.println("\nMessage from: " + packet.getAddress().getHostAddress());
             
             InputSource source = new InputSource(new StringReader(msg));
 
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder db;
             Element a = null;
             
             try {
                 db = dbf.newDocumentBuilder();
                 Document doc = db.parse(source);
                 a = doc.getDocumentElement();
             } catch (ParserConfigurationException e) {
                 e.printStackTrace();
             } catch (SAXException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
                         
 			
             // Go this way, when the node receives a join-request from another node (maybe not necessary)
             if(a.getNodeName() == "join") {
 
                 String newMember = packet.getAddress().getHostAddress();
                 try {
					Logger.log("Join", newMember);
				} catch (IOException e) {
					e.printStackTrace();
				}
                 System.out.println(newMember + " is joining the cluster.");
                 list.add(newMember);
              
            // Go this way, when the node receives a leave-request from another node  
             } else if(a.getNodeName() == "leave") {
            
     		// Go this way, when the node gets a membershiplist from another node
             } else if(a.getNodeName() == "membershipList") {
             	
             	ArrayList<MembershipEntry> receivedMemList = new ArrayList<MembershipEntry>();
            	
             	try {
                     receivedMemList = DstrMarshaller.unmarshallXML(msg);
                 } catch (JAXBException e) {
                     e.printStackTrace();
                 }
             	
             	MembershipController.updateMembershipList(list, receivedMemList);
                 
             }
             
         }
 
         System.out.println("[" + this.getClass().toString() + "] is dying.");
     }
 
 }
