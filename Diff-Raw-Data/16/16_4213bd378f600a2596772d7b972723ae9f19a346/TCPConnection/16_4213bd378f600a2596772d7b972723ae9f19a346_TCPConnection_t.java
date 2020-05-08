 package communication;
 
 import filesystem.SDFS;
 import membership.Proc;
 import misc.MiscTool;
 import org.apache.log4j.Logger;
 
 import java.io.*;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.Arrays;
 
 import static communication.Messages.*;
 import communication.MessagesFactory;
 
 public class TCPConnection {
     private Logger logger = Logger.getLogger(TCPConnection.class);
     private Socket socket;
     private InputStream is;
     private OutputStream os;
     private Proc proc;
 
     public TCPConnection() {
 
     }
 
     public TCPConnection setSocket(Socket socket) {
         this.socket = socket;
         tryUpdateInputAndOutputStream();
         return this;
     }
 
     public TCPConnection setProc(Proc proc) {
         if(proc == null) {
             throw new NullPointerException("null argument");
         }
         this.proc = proc;
         return this;
     }
 
     public void tryUpdateInputAndOutputStream() {
         try {
             updateInputAndOutputStream();
         } catch(IOException e) {
             //
         }
     }
 
 
     public TCPConnection updateInputAndOutputStream() throws IOException {
         is = socket.getInputStream();
         os = socket.getOutputStream();
         return this;
     }
 
     public void startReceiving() {
         while(true) {
             try{
                 byte[] tmpBytes = new byte[MiscTool.BUFFER_SIZE];
                 int num;
                 is = socket.getInputStream();
                 num = is.read(tmpBytes);
                 byte[] bytes = new byte[num];
                 System.arraycopy(tmpBytes, 0, bytes, 0, num);
                 Message message = Message.parseFrom(bytes);
                 logger.debug("Received Message: " + message.toString());
                 handle(message);
             } catch(IOException e) {
                 if(e.getMessage().equals("socket close")) {
                     break;
                 } else {
                     logger.error("Receiving message error", e);
                 }
             } catch(NegativeArraySizeException e) {
                 //ignore it
             }
         }
     }
 
     public void readAndWriteToFile(String fileName) {
         File file;
         SDFS sdfs = proc.getSDFS();
         file = sdfs.openFile(fileName);
         FileOutputStream fos;
 
         try {
             fos = new FileOutputStream(fileName);
         } catch (FileNotFoundException e) {
             logger.error("writing to file error " + e);
             return;
         }
 
         try {
             is = socket.getInputStream();
             int nextByte;
             while((nextByte = is.read())!= -1) {
                 fos.write(nextByte);
             }
             fos.close();
         } catch (IOException e) {
             if(e.getMessage().equals("socket close")) {
                 //
             } else {
                 logger.error("Read messages error", e);
             }
         }
     }
 
     public void readFileAndSend(String fileName) {
         SDFS sdfs = proc.getSDFS();
         File file = sdfs.openFile(fileName);
 
         FileInputStream fis;
 
         try {
             fis = new FileInputStream(file);
         } catch (FileNotFoundException e) {
             logger.error("read from file error " + e);
             return;
         }
 
         try {
             os =socket.getOutputStream();
             int nextByte;
             while((nextByte = fis.read()) != -1) {
                 os.write(nextByte);
             }
             fis.close();
 
         } catch (IOException e) {
             if(e.getMessage().equals("socket close")) {
                 //
             } else  {
                 logger.error("Write message error", e);
             }
         }
     }
 
     public void sendData(byte[] bytes) {
         try {
             proc.increaseAndGetTimeStamp();
             os.write(bytes);
         } catch (IOException e) {
             logger.error("Sending TCP packets error" + e);
             e.printStackTrace();
         }
     }
 
     public void receiveAndSaveData(String filename){
         proc.increaseAndGetTimeStamp();
         readAndWriteToFile(filename);
     }
 
     private void handle(Message m) {
         proc.increaseAndGetTimeStamp();
 
         switch (m.getType()) {
             case Join:
                 JoinMessage joinMessage = m.getJoinMessage();
                 ProcessIdentifier joinedMachine = joinMessage.getJoinedMachine();
 //                ProcessIdentifier remoteProcessIdentifier = generateRemoteProcessIdentifier(joinedMachine);
 //                proc.addProcToMemberList(remoteProcessIdentifier);
 //                if(!reconstructHeartBeatRing(joinedMachine)) {
 //                    logger.error("Fail to reconstruct ring, drop the join message");
 //                    break;
 //                }
 
                 proc.addProcToMemberList(joinedMachine);
                 break;
 
             case SendTo:
 //                SendToMessage sendToMessage = m.getSendToMessage();
 //                ProcessIdentifier sendToMachine = sendToMessage.getSendToMachine();
 //                proc.getFailureDetector().setSendToMachine(sendToMachine);
                 break;
 
             case ListenFrom:
 //                ListenFromMessage listenFromMessage = m.getListenFrom();
 //                ProcessIdentifier listenFromMachine = listenFromMessage.getListenFromMachine();
 //                proc.getFailureDetector().setListenFromMachine(listenFromMachine);
                 break;
 
             case getFile:
                 GetFileMessage getFileMessage = m.getGetFileMessage();
                 prepareToSend(proc.getIdentifier(), getFileMessage.getFilepath());
                 sendReadyToGetMessage(getFileMessage.getFilepath(),
                         getFileMessage.getRequestingProcess().getIP(),
                         getFileMessage.getRequestingProcess().getPort());
                 break;
 
             case putFile:
                 PutFileMessage putFileMessage = m.getPutFileMessage();
                 prepareToGet(putFileMessage.getStoringProcess(), putFileMessage.getFilepath());
                 sendReadytoPutMessage(putFileMessage.getFilepath(),
                         putFileMessage.getStoringProcess().getIP(),
                         putFileMessage.getStoringProcess().getPort());
                 break;
 
 
             case deleteFile:
                 DeleteFileMessage deleteFileMessage = m.getDeleteFileMessage();
                 if (deleteFile(deleteFileMessage.getFilepath())) {
                     System.out.println("File Successfully deleted.");
                 } else {
                     System.out.println("File NOT deleted, please try again");
                 }
                 break;
 
 
                 //TODO add code for handling get, put and delete messages
 
             default:
                 break;
             case Heartbeat:
                 break;
             case Fail:
                 break;
             case SyncProcesses:
                 break;
             case SyncFiles:
                 break;
             case readyToPut:
                 ReadyToPutFileMessage readyToPutFileMessage = m.getReadyToPutFileMessage();
                 putFile(readyToPutFileMessage);
                 break;
             case readyToGet:
                 ReadyToGetFileMessage readyToGetFileMessage = m.getReadyToGetFileMessage();
                 getFile(readyToGetFileMessage);
                 break;
         }
     }
 
     private void putFile(ReadyToPutFileMessage readyToPutFileMessage){
         try {
             File file = new File(readyToPutFileMessage.getFilepath());
             FileInputStream in = new FileInputStream(file);
 
             String address = readyToPutFileMessage.getStoringProcess().getIP() + ":" +
                     Integer.toString(readyToPutFileMessage.getStoringProcess().getPort() + 2);
 
             TCPClient tcpClient = new TCPClient(address);
             tcpClient.setProc(proc);
             if(tcpClient.connect()){
                int nextByte;
                byte buffer[] = new byte[1024];
                while((nextByte = in.read(buffer)) != -1){
                    tcpClient.sendData(buffer);
                }

                 tcpClient.close();
             }
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
     }
 
     private void getFile(ReadyToGetFileMessage readyToGetFileMessage){
         String address = readyToGetFileMessage.getStoringProcess().getIP() + ":" +
                 Integer.toString(readyToGetFileMessage.getStoringProcess().getPort());
         TCPClient tcpClient = new TCPClient(address);
         tcpClient.setProc(proc);
         if(tcpClient.connect()){
             tcpClient.receiveAndSaveData(readyToGetFileMessage.getFilepath());
             tcpClient.close();
         }
     }
 
     private void prepareToGet(ProcessIdentifier storingProcess, String SDFSfilepath){
         this.proc.getFileServer().prepareToGet(storingProcess, SDFSfilepath);
 
     }
 
     private void prepareToSend(ProcessIdentifier storingProcess, String SDFSfilepath){
         this.proc.getFileServer().prepareToSend(storingProcess, SDFSfilepath);
     }
 
     private void sendReadyToGetMessage(String SDFSFilepath, String processRequestingFile_IP, int processRequestingFile_port){
         String address = processRequestingFile_IP + ":" + Integer.toString(processRequestingFile_port);
         TCPClient tcpClient = new TCPClient(address);
         tcpClient.setProc(proc);
         if(tcpClient.connect()){
             Message m = MessagesFactory.generatePutFileMessage(SDFSFilepath, proc.getIdentifier());
             tcpClient.sendData(m);
             tcpClient.close();
         }
     }
 
     private void sendReadytoPutMessage(String SDFSFilepath, String processStoringFile_IP, int processStoringFile_port){
         String address = processStoringFile_IP + ":" + Integer.toString(processStoringFile_port);
         TCPClient tcpClient = new TCPClient(address);
         tcpClient.setProc(proc);
         if(tcpClient.connect()){
             Message m = MessagesFactory.generateReadyToPutFileMessage(SDFSFilepath, proc.getIdentifier());
             tcpClient.sendData(m);
             tcpClient.close();
         }
     }
 
     private boolean deleteFile(String SDFSfilepath){
         File file = new File(SDFSfilepath);
         //TODO delete file from the filelist, when filelist is implemented
         return file.delete();
     }
 
 
 
     private boolean reconstructHeartBeatRing(ProcessIdentifier joinedMachine) {
         Messages.ProcessIdentifier first = proc.getMemberList().getFirst();
         Messages.ProcessIdentifier last = proc.getMemberList().getLast();
         Messages.Message message;
 
         //before: last -> first -> second ...
         //after: last -> joinMachine -> first -> second
         TCPClient clientConnectToLast = new TCPClient(last.getIP()+":"+last.getPort());
         clientConnectToLast.setProc(proc);
         if(clientConnectToLast.connect()) {
             message = MessagesFactory.generateSendToMessage(joinedMachine);
             clientConnectToLast.sendData(message);
             clientConnectToLast.close();
         } else {
             return false;
         }
 
         TCPClient clientConnectToJoinedMachine = new TCPClient(joinedMachine.getIP()+":"+joinedMachine.getPort());
         clientConnectToJoinedMachine.setProc(proc);
         if(clientConnectToJoinedMachine.connect()) {
             message = MessagesFactory.generateSendToMessage(first);
             clientConnectToJoinedMachine.sendData(message);
             clientConnectToJoinedMachine.close();
         } else {
             return false;
         }
 
         if(clientConnectToJoinedMachine.connect()) {
             message = MessagesFactory.generateListenFromMessage(last);
             clientConnectToJoinedMachine.sendData(message);
             clientConnectToJoinedMachine.close();
         } else {
             return false;
         }
 
         TCPClient clientConnectToFirst = new TCPClient(first.getIP()+":"+first.getPort());
         clientConnectToFirst.setProc(proc);
         if(clientConnectToFirst.connect()) {
             message = MessagesFactory.generateListenFromMessage(joinedMachine);
             clientConnectToFirst.sendData(message);
             clientConnectToFirst.close();
         } else {
             return false;
         }
 
         return true;
     }
 
     public void close() throws IOException {
         socket.close();
     }
 
     /**
      * Generate remote process identifier from joinedMachine. We cannot directly use ip address in joinedMachine
      * and port in socket.
      * @param joinedMachine
      * @return
      */
     private ProcessIdentifier generateRemoteProcessIdentifier(ProcessIdentifier joinedMachine) {
         String ip = socket.getInetAddress().getHostAddress();
         return ProcessIdentifierFactory.generateProcessIdentifier(
                 joinedMachine.getId(), ip, joinedMachine.getPort(), joinedMachine.getTimestamp());
     }
 
     public String getRemoteAddress() {
         InetAddress inetAddress = socket.getInetAddress();
         Integer port = socket.getPort();
         return inetAddress.getHostAddress() + ":" + port;
     }
 }
