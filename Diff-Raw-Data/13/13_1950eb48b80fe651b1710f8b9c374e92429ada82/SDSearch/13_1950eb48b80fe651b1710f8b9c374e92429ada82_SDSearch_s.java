 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package protoSearchDevice;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.jxta.endpoint.ByteArrayMessageElement;
 import net.jxta.endpoint.EndpointAddress;
 import net.jxta.endpoint.Message.ElementIterator;
 import net.jxta.endpoint.MessageElement;
 import net.jxta.endpoint.StringMessageElement;
 import org.bouncycastle.jce.exception.ExtIOException;
import org.json.JSONObject;
 import protoLowareInterfaceDevice.LIDRequestHandler;
 import protoStandardAbstractData.LOP2PMetadata;
 import protoTranslatorDevice.*;
 import protonetcommunicationdevice.*;
 
 
 /**
  *
  * @author Rafael de Santiago
  */
 public class SDSearch {
     
     //configuration
     private SDConfiguration sdcfg;
     
     //translator
     TrDTranslator translator;
     
     private LIDRequestHandler clientSocket;
     
     /**
      * Constructor of the class SDSearch. This class have the purpose of
      * execute search for metadatas in LOP2P net and retrieve information about
      * requested searchs. 
      * 
      * @param clientSocket socket of loware connection
      * @param sdcfg configuration object of Search Device
      * @param trdcfg configuration object of the Translator Device
      */                    
     public SDSearch (LIDRequestHandler clientSocket, SDConfiguration sdcfg, TrDConfiguration trdcfg){
         this.sdcfg = sdcfg;    
         this.translator = new TrDTranslator(trdcfg);
         this.clientSocket = clientSocket;
     }
 
     /**
      * Search for Learning Objects. Request received by loware
      * 
      * @param message request message of search containing metadata for search
      */      
     public void search(String message) throws FileNotFoundException, IOException, ClassNotFoundException, Exception{
 
         //send the responses for client
         DataOutputStream toCliente = null;
         ArrayList metadatas = translator.translate(message);
         LOP2PMetadata metadata = null;
         if (metadatas.size() > 0) {
             metadata = (LOP2PMetadata) metadatas.get(0);
         } else {
             //generate exception
            throw new Exception("A metadata couldn't be created with the search arguments.");
         }
         ArrayList compatibilities = new ArrayList();
         compatibilities.addAll(this.sdcfg.getGDData().searchInMyMetadatas(metadata, this.sdcfg.getSimForRecover()));
         compatibilities.addAll(this.sdcfg.getGDData().searchInOthersMetadatas(metadata, this.sdcfg.getSimForRecover()));        
         
 
         //request search outside: others peers
         ArrayList msgPool = this.sdcfg.getNCDCfg().getNCDData().getMessageBufferList();
         
         Integer nrequests = 0;
         
         Date date = new Date();
         String msgID = "msgSent" + date.getTime();
         ArrayList otherPeers = sdcfg.getNCDCfg().getNCDData().getKnownPeersList();
         for (int i=0; i<otherPeers.size(); i++){
             ObjectOutputStream objstream = null;
             try {
                 String peerTarget = ((EndpointAddress) otherPeers.get(i)).getProtocolAddress();
                 String thisPeer = this.sdcfg.getNCDCfg().getNetworkManager().getPeerID().toURI().toString();
                 thisPeer = thisPeer.substring(thisPeer.indexOf("uuid"));
                 LOP2PMessage msgToSend = new LOP2PMessage(thisPeer, peerTarget, this.sdcfg.getNCDCfg().getMessageNamespace());
                 msgToSend.addItem("IDMSG", msgID);
                 msgToSend.addItem("TYPE", LOP2PMessage.MESSAGE_TYPE.SEARCH.toString());
                 String fileName = msgID;
                 String metadataPath = this.sdcfg.getNCDCfg().getMessagesTempPath() + '/' + fileName + ".ser";
                 objstream = new ObjectOutputStream(new FileOutputStream(metadataPath));
                 objstream.writeObject(metadata);
                 objstream.close();
 
                 //putting that in a message
                 File file = new File(metadataPath);
                 msgToSend.addAttachment(file);
                 nrequests++;
                 
                 msgPool.add(msgToSend);
             } catch (IOException ex) {
                 otherPeers.remove(i);
                 i--;
                 System.err.println(ex.toString());
             } finally {
                 try {
                     objstream.close();
                 } catch (IOException ex) {
                     Logger.getLogger(SDSearch.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             
         }
         
         
         //send the local messages to loware
         toCliente = clientSocket.getOut();//new DataOutputStream(this.clientSocket.getOutputStream());
         for (int i = 0; i < compatibilities.size(); i++) {
             LOP2PMetadata mtdt = (LOP2PMetadata)compatibilities.get(i);
             //create message
             String msg = (String) translator.translateBack(mtdt, this.sdcfg.getNCDCfg().getNetworkManager().getPeerID().toURI().toString());
 
             //send message to search requester
             synchronized(toCliente){
                 toCliente.write(msg.getBytes());
             }
         }
         
         
         //send the responses for client: use time as stop criteria
         Calendar targetCal = new GregorianCalendar();
         targetCal.add(Calendar.SECOND, this.getSDCfg().getExpirationTime2());
         Date target = targetCal.getTime();
         Date now = new Date();
         while ((target.getTime() > now.getTime())){
             LOP2PMessage msg = (LOP2PMessage)this.sdcfg.getNCDCfg().getNCDData().searchForResponseById(msgID);
             if (msg != null){
                 //getting metadatas
                 ElementIterator itMsg =  msg.getMessageElements();
                 
                 while(itMsg.hasNext()){
                     MessageElement el = itMsg.next();
                     if (el.getElementName().equals("fileRefence")){
                         //recover the element
                         StringMessageElement sme = 
                                                         (StringMessageElement) el;                    
                         String filePath = new String(sme.getBytes(true));
                         
                         ObjectInputStream objstream = new ObjectInputStream(new FileInputStream(filePath));
                         LOP2PMetadata metadataFromFile = (LOP2PMetadata) objstream.readObject();
                         objstream.close(); 
                         
                         //send message with metadata to search requester
                         String translatedMessage = (String) translator.translateBack(metadataFromFile, msg.getItem("peerFrom"));
                         synchronized(toCliente){
                             toCliente.write(translatedMessage.getBytes());                        
                         }
                     }                    
                 }
                 
                 //decrising number of responses
                 //nrequests--; 
             }            
             now = new Date();
         }              
         
         //ending message for requester
         String bye = "GOODBYE";
         synchronized(toCliente){
             toCliente.write(bye.getBytes()); 
             toCliente.close();
         }
    //     clientSocket.close();
     }
     
     /**
      * Return the configuration object of Seach Device. 
      * 
      * @return configuration object of Seach Device
      */      
     public SDConfiguration getSDCfg() {
         return sdcfg;
     }
 
     /**
      * Search for Learning Objects. Request received by LOP2P network 
      * 
      * @param msg request message of search containing metadata for search
      */     
     public void searchExpose(LOP2PMessage msg) {
         //getting metadatas
         try{
             ElementIterator itMsg =  msg.getMessageElements();
             MessageElement el = null;
             LOP2PMetadata metadataFromFile = null;
             String filePath = "";
             while(itMsg.hasNext()){
                 el = itMsg.next();
                 if (el.getElementName().equals("fileRefence")){
 
                 ObjectInputStream objistream = null;
                 try {
                     StringMessageElement sme = (StringMessageElement) el;
                     filePath = new String(sme.getBytes(true));
                     objistream = new ObjectInputStream(new FileInputStream(filePath));
                     metadataFromFile = (LOP2PMetadata) objistream.readObject();
                     objistream.close();
                     break;
                 } catch (IOException ex) {
                     Logger.getLogger(NCDMessagesReceiverVerifier.class.getName()).log(Level.SEVERE, null, ex);
                 } catch (ClassNotFoundException ex) {
                     Logger.getLogger(NCDMessagesReceiverVerifier.class.getName()).log(Level.SEVERE, null, ex);
 
                 } finally {
 
                 }
              }
             }
              if(metadataFromFile != null){
                 ArrayList compatibilities = new ArrayList();
                 compatibilities.addAll(this.sdcfg.getGDData().searchInMyMetadatas(metadataFromFile, this.sdcfg.getSimForRecover()));
                 compatibilities.addAll(this.sdcfg.getGDData().searchInOthersMetadatas(metadataFromFile, this.sdcfg.getSimForRecover()));
 
 
                 //request search outside: others peers
                 ArrayList msgPool = this.sdcfg.getNCDCfg().getNCDData().getMessageBufferList();
 
                 Integer nrequests = 0;
 
                 //send message other
                 String msgID = msg.getItem("IDMSG");
 
                 //verifies if this message was received another day
                 if (this.sdcfg.getNCDCfg().getNCDData().addIfIsNewMessage(msgID) == false){
                     //preempt the proccess
                     return;
                 }
 
                 ArrayList otherPeers = sdcfg.getNCDCfg().getNCDData().getKnownPeersList();
                 for (int i=0; i<otherPeers.size(); i++){
                     String peerTarget = ((EndpointAddress) otherPeers.get(i)).getProtocolAddress();
                     if (msg.getItem("peerFrom").equals(peerTarget) != true){
                         ObjectOutputStream objstream = null;
     //                    try {
 
 
                         LOP2PMessage msgToSend = new LOP2PMessage(msg.getItem("peerFrom"), peerTarget, this.sdcfg.getNCDCfg().getMessageNamespace());
                         msgToSend.addItem("IDMSG", msgID);
                         msgToSend.addItem("TYPE", LOP2PMessage.MESSAGE_TYPE.SEARCH.toString());
                         String fileName = msgID + i;
                         /*String metadataPath = this.sdcfg.getNCDCfg().getMessagesTempPath() + '/' + fileName + ".ser";
                         objstream = new ObjectOutputStream(new FileOutputStream(metadataPath));
                         objstream.writeObject(metadataFromFile);
                         objstream.close();*/
 
                         //putting that in a message
                         File file = new File(filePath);
                         msgToSend.addAttachment(file);
                         nrequests++;
 
                         msgPool.add(msgToSend);
 
                     }
 
                 }
 
 
                 //send the local messages to requester peer
                 ObjectOutputStream objstream = null;
                 for (int i = 0; i < compatibilities.size(); i++) {
                         try {
                             LOP2PMetadata mtdt = (LOP2PMetadata) compatibilities.get(i);
                             //create message
                             LOP2PMessage msgToSend = new LOP2PMessage(msg.getItem("peerDestination"), msg.getItem("peerFrom"), this.sdcfg.getNCDCfg().getMessageNamespace());
                             msgToSend.addItem("IDMSG", msgID);
                             msgToSend.addItem("TYPE", LOP2PMessage.MESSAGE_TYPE.SEARCH_EXPOSE.toString());
                             String fileName = msgID + i + "EXPOSE";
 
                             String metadataPath = this.sdcfg.getNCDCfg().getMessagesTempPath() + '/' + fileName + ".ser";
                             objstream = new ObjectOutputStream(new FileOutputStream(metadataPath));
                             objstream.writeObject(mtdt);
                             objstream.close();
 
                             //putting that in a message
                             File file = new File(metadataPath);
                             msgToSend.addAttachment(file);
                             nrequests++;
 
                             //send message to search requester
                             msgPool.add(msgToSend);
                             System.err.println("Exposing requested search!");
                         } catch (IOException ex) {
                             Logger.getLogger(SDSearch.class.getName()).log(Level.SEVERE, null, ex);
 
                         }
 
                 }
 
     /*
                 //send the responses for client: use time as parade criteria
                 Calendar targetCal = new GregorianCalendar();
                 targetCal.add(Calendar.SECOND, this.getSDCfg().getExpirationTime());
                 Date target = targetCal.getTime();
                 Date now = new Date();
                 while ((target.getTime() > now.getTime())){
                     LOP2PMessage msg2 = (LOP2PMessage)this.sdcfg.getNCDCfg().getNCDData().searchForResponseById(msgID);
                     if (msg != null){
                         //getting metadatas
                         ElementIterator itMsg2 =  msg2.getMessageElements();
 
                         while(itMsg.hasNext()){
                             MessageElement el2 = itMsg2.next();
                             if (el2.getElementName().equals("fileRefence")){
                                 //recover the element
                                 ByteArrayMessageElement myByteArrayMessageElement2 = 
                                                                 (ByteArrayMessageElement) el2;                    
                                 String filePath2 = new String(myByteArrayMessageElement2.getBytes());
 
                                 ObjectInputStream objstream2 = new ObjectInputStream(new FileInputStream(filePath2));
                                 LOP2PMetadata metadataFromFile2 = (LOP2PMetadata) objstream2.readObject();
                                 objstream.close(); 
 
                                 //send message with metadata to search requester
                                 LOP2PMessage msgToSend = new LOP2PMessage(  
                                                             msg.getItem("peerDestination"),
                                                             msg.getItem("peerFrom"),
                                                             this.sdcfg.getNCDCfg().getMessageNamespace()
                                                             );
                                 msgToSend.addItem("IDMSG", msgID);
                                 msgToSend.addItem("TYPE", LOP2PMessage.MESSAGE_TYPE.SEARCH_EXPOSE.toString());                    
                                 String fileName = msgID + "asdfEXPOSE"+itMsg2.getNamespace();                    
                                 String metadataPath = this.sdcfg.getNCDCfg().getMessagesTempPath() + '/' + fileName + ".ser";
                                 objstream = new ObjectOutputStream(new FileOutputStream(metadataPath));
                                 objstream.writeObject(metadataFromFile2);
                                 objstream.close();
 
                                 //putting that in a message
                                 File file = new File(metadataPath);
                                 msgToSend.addAttachment(file);
                                 nrequests++;
 
                                 //send message to search requester
                                 msgPool.add(msgToSend);  
 
 
                             }                    
                         }
 
                         //decrising number of responses
                         //nrequests--; 
                     }            
                     now = new Date();
                 }                         */     
 
 
 
              }
       
         }catch(Exception ex){
             System.err.println("Search Expose: "+ex.toString());
         }                   
     }
     
     
     /**
      * Set the configuration object of Seach Device. 
      * 
      * @param sdcfg  configuration object of Seach Device
      */          
 
     public void setSDCfg(SDConfiguration sdcfg) {
         this.sdcfg = sdcfg;     
     }
     
     
     
     
 }
