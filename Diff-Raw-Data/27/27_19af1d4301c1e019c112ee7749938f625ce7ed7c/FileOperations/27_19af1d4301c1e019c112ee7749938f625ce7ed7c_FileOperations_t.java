 package filesystem;
 
 import communication.Messages;
 
 import java.io.File;
 import java.security.PrivateKey;
 
 import communication.Messages.FileIdentifier;
 import communication.MessagesFactory;
 import communication.TCPClient;
 import main.MainEntry;
 import membership.Proc;
 import communication.Messages.Message;
 import communication.Messages.ProcessIdentifier;
 
 import javax.xml.soap.MessageFactory;
 
 /**
  * Created with IntelliJ IDEA.
  * User: naveed
  * Date: 11/8/12
  * Time: 11:55 PM
  * To change this template use File | Settings | File Templates.
  */
 public class FileOperations {
 
     private static Proc proc;
 
     private static int localClientPort;
 
     public void get(String localFilename, String SDFSFilename, FileList fileList){
         FileIdentifier requiredFileID =  fileList.getFileLocation(SDFSFilename);
         String processHavingFile_IP = requiredFileID.getFileStoringProcess().getIP();
         int processHavingFile_port = requiredFileID.getFileStoringProcess().getPort();
         sendGetMessage(SDFSFilename, processHavingFile_IP, processHavingFile_port);
     }
 
     public void put(String SDFSFilename, String localFilename, ProcessIdentifier storingProcessID){
         sendPutMessage(SDFSFilename, storingProcessID.getIP(), storingProcessID.getPort());
         //TODO code for sending file
     }
 
     public void delete(String SDFSFilename, ProcessIdentifier storingProcessID){
         sendDeleteMessage(SDFSFilename, storingProcessID.getIP(), storingProcessID.getPort());
     }
 
     public void sendPutMessage(String SDFSFilename, String processStroingFile_IP, int processStoringFile_port){
         String address = processStroingFile_IP + ":" + Integer.toString(processStoringFile_port);
         TCPClient tcpClient = new TCPClient(address);
         tcpClient.setProc(proc);
         if(tcpClient.connect()){
             Message m = MessagesFactory.generatePutFileMessage(SDFSFilename, proc.getIdentifier());
             tcpClient.sendData(m);
             tcpClient.close();
         }
     }
 
     public void sendDeleteMessage(String SDFSFilename, String processStroingFile_IP, int processStoringFile_port){
         String address = processStroingFile_IP + ":" + Integer.toString(processStoringFile_port);
         TCPClient tcpClient = new TCPClient(address);
         tcpClient.setProc(proc);
         if(tcpClient.connect()){
             Message m = MessagesFactory.generateDeletedFileMessage(SDFSFilename, proc.getIdentifier());
             tcpClient.sendData(m);
             tcpClient.close();
         }
 
     }
 
     public void sendGetMessage(String SDFSFilename, String processHavingFile_IP, int processHavingFile_port){
         String address = processHavingFile_IP + ":" + Integer.toString(processHavingFile_port);
         TCPClient tcpClient = new TCPClient(address);
         tcpClient.setProc(proc);
         if(tcpClient.connect()){
             Message m = MessagesFactory.generateGetFileMessage(SDFSFilename);
             tcpClient.sendData(m);
             tcpClient.close();
         }
     }
 
    public FileOperations setProc(Proc proc) {
         FileOperations.proc = proc;
        return this;
     }
 
 
     private void startFileServer(){
 
     }
 
 
 
     //TODO One thread to receive the requests
 
 
 }
