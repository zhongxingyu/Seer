 package communication;
 
 import communication.message.Messages;
 import communication.message.MessagesFactory;
 import filesystem.FileState;
 import filesystem.SDFS;
 import maplejuice.JuiceForClient;
 import maplejuice.MapleForClient;
 import membership.Proc;
 import misc.MiscTool;
 import org.apache.log4j.Logger;
 
 import java.io.*;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.security.PrivateKey;
 import java.util.Arrays;
 import java.util.List;
 
 import static communication.message.Messages.*;
 
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
 //                logger.info("Received Message: " + message.toString() + ", size: " + message.toByteArray().length);
                 logger.info("Received Message: " + message.getType());
                socket.close();
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
 
     public String readID() {
         try {
             is = socket.getInputStream();
             byte[] bytes = new byte[36];    // 36 is the length of uuid string representation
             Integer numberOfBytes = is.read(bytes, 0, 36);
             if(numberOfBytes != 36) {
                 throw new IOException("input stream format error");
             }
 
             return new String(bytes);
         } catch (IOException e) {
             if(e.getMessage().equals("socket close")){
                 //
             } else {
                 logger.error("Read id from inputstream error ", e);
             }
             return "wrong id";
         }
     }
 
     public String readFileHeader() {
         try {
             is = socket.getInputStream();
             byte[] bytes = new byte[10];    // 10 is the length of file header
             Integer numberOfBytes = is.read(bytes, 0, 10);
             if(numberOfBytes != 10) {
                 throw new IOException("input stream format error");
             }
 
             return new String(bytes);
         } catch (IOException e) {
             if(e.getMessage().equals("socket close")){
                 //
             } else {
                 logger.error("Read header from inputstream error ", e);
             }
             return "wrong file header";
         }
     }
 
     public void readAndWriteToFile(String fileName) {
         SDFS sdfs = proc.getSDFS();
         FileOutputStream fos;
         BufferedOutputStream bos;
         BufferedInputStream bis;
 
         try {
             fos = new FileOutputStream(sdfs.openFile(fileName)) ;
             bos = new BufferedOutputStream(fos);
         } catch (FileNotFoundException e) {
             logger.error("writing to file error " + e);
             return;
         }
 
         try {
             is = socket.getInputStream();
             bis = new BufferedInputStream(is);
 //            byte [] buffer = new byte[8096 * 4];
 //            int c;
 //            while((c = bis.read(buffer))!= -1) {
 //                bos.write(buffer, 0, c);
 //            }
 //            bos.close();
 //            bis.close();
             MiscTool.readFromInputStreamToOutputStream(bis, bos);
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
         BufferedOutputStream bos;
         BufferedInputStream bis;
 
         try {
             fis = new FileInputStream(file);
             bis = new BufferedInputStream(fis);
 
         } catch (FileNotFoundException e) {
             logger.error("read from file error " + e);
             return;
         }
 
         try {
             os =socket.getOutputStream();
             bos = new BufferedOutputStream(os);
 
 //            byte buffer[] = new byte[8096 * 4];
 //            int c;
 //            while((c = bis.read(buffer))!=-1) {
 //                bos.write(buffer, 0, c);
 //            }
 //            bis.close();
 //            bos.close();
             MiscTool.readFromInputStreamToOutputStream(bis, bos);
 
 //            int nextByte;
 //            while((nextByte = bis.read())!= -1) {
 //                bos.write(nextByte);
 //            }
 //            bis.close();
 
         } catch (IOException e) {
             if(e.getMessage().equals("socket close")) {
                 //
             } else  {
                 logger.error("Write message error", e);
             }
         }
     }
     public void sendData(int b) {
         try{
             os.write(b);
         } catch(IOException e) {
             logger.error("Sending TCP packets error" + e);
             e.printStackTrace();
         }
     }
 
     public void flush() {
         try {
             os.flush();
         } catch(IOException e) {
             logger.error("flush error", e);
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
 
     public void sendData(InputStream is) {
         try {
             proc.increaseAndGetTimeStamp();
 
             MiscTool.readFromInputStreamToOutputStream(is, os);
 
 //            int nextByte;
 //            while((nextByte = is.read())!=-1) {
 //                os.write(nextByte);
 //            }
         } catch(IOException e) {
             logger.error("Error in writing from stream", e);
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
 //                GetFileMessage getFileMessage = m.getGetFileMessage();
 //                prepareToSend(proc.getIdentifier(), getFileMessage.getFilepath());
 //                sendReadyToGetMessage(getFileMessage.getFilepath(),
 //                        getFileMessage.getRequestingProcess().getIP(),
 //                        getFileMessage.getRequestingProcess().getPort());
                 break;
 
             case putFile:
                 PutFileMessage putFileMessage = m.getPutFileMessage();
                 prepareToGet(putFileMessage.getStoringProcess(), putFileMessage.getFid());
                 sendReadyToPutMessage(putFileMessage.getFid(),
                         putFileMessage.getStoringProcess().getIP(),
                         putFileMessage.getStoringProcess().getPort());
                 break;
 
 
             case deleteFile:
                 DeleteFileMessage deleteFileMessage = m.getDeleteFileMessage();
                 deleteFile(deleteFileMessage.getFilepath());
 //                if (deleteFile(deleteFileMessage.getFilepath())) {
 //                    System.out.println("File Successfully deleted.");
 //                } else {
 //                    System.out.println("File NOT deleted, please try again");
 //                }
                 break;
 
 
                 //TODO add code for handling get, put and delete messages
 
             case get:
                 GetMessage getMessage = m.getGetMessage();
                 ProcessIdentifier requestProcess = getMessage.getRequestingProcess();
                 String address = requestProcess.getIP()+":" + (requestProcess.getPort()+3);
                 String fileName = getMessage.getFileName();
 
                 TCPClient tcpClient = new TCPClient(address);
                 tcpClient.setProc(proc);
                 while(true) {
                     if(tcpClient.connect()) {
                         try {
                             BufferedInputStream bis =
                                     new BufferedInputStream(new FileInputStream(proc.getSDFS().getFile(fileName)));
                             tcpClient.sendData(bis);
                             bis.close();
                             tcpClient.close();
                             break;
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     }
                     try {
                         Thread.sleep(200);
                     } catch (InterruptedException e) {
                         ;
                     }
                 }
                 break;
 
             case Fail:
                 break;
 
             case readyToPut:
                 ReadyToPutFileMessage readyToPutFileMessage = m.getReadyToPutFileMessage();
                 putFile(readyToPutFileMessage);
                 break;
 
             case readyToGet:
                 ReadyToGetFileMessage readyToGetFileMessage = m.getReadyToGetFileMessage();
                 getFile(readyToGetFileMessage);
                 break;
 
             case ImMaster:
                 ImMasterMessage masterMessage = m.getMasterMessage();
                 setMaster(masterMessage.getMasterProcess());
                 break;
 
             case maple:
                 MapleMessage mapleMessage = m.getMapleMessage();
                 constructMapleJob(mapleMessage);
                 break;
 
             case receivedMaple:
                 ReceivedMapleMessage receivedMapleMessage = m.getReceivedMapleMessage();
                 respondMaple(receivedMapleMessage);
                 break;
 
             case doMaple:
                 doMapleJob();
                 break;
 
             case mapleResult:
                 MapleResultMessage mapleResultMessage = m.getMapleResultMessage();
                 aggregateMapleResult(mapleResultMessage);
                 break;
 
             case juice:
                 JuiceMessage juiceMessage = m.getJuiceMessage();
                 doJuiceJob(juiceMessage);
                 break;
 
             case juiceResult:
                 JuiceResultMessage juiceResultMessage = m.getJuiceResultMessage();
                 aggregateJuiceResult(juiceResultMessage);
             default:
                 break;
         }
     }
 
     private JuiceForClient juice;
 
     private void doJuiceJob(JuiceMessage juiceMessage) {
         juice = new JuiceForClient();
         juice.setProc(proc).setJuiceMessage(juiceMessage).init();
         juice.doJuice();
     }
     private void aggregateJuiceResult(JuiceResultMessage juiceResult) {
         String fileName = juiceResult.getFileName();
         Integer numJuice = juiceResult.getNumJuice();
 
         if(MiscTool.requireToCreateFile(proc.getMemberList().getList(), proc.getIdentifier(), fileName, numJuice)) {
             if(!proc.getSDFS().hasSDFSFile(fileName)) {
                 proc.getSDFS().createLocalSDFSFile(fileName);
             }
             String data = juiceResult.getKey() + "," + juiceResult.getValue();
             proc.getSDFS().appendDataToLocalFile(fileName, data);
         }
     }
 
     private void constructMapleJob(MapleMessage mapleMessage) {
         MapleForClient maple = new MapleForClient();
         maple.setProc(proc).setMapleMessage(mapleMessage).init();
         proc.setMapleClient(maple);
     }
 
     private void respondMaple(ReceivedMapleMessage receivedMapleMessage) {
         proc.getMapleMaster().onReceived();
     }
 
     private void doMapleJob() {
         proc.getMapleClient().doMaple();
     }
 
     private void aggregateMapleResult(MapleResultMessage mapleResult) {
         List<String> fileNames = mapleResult.getFileNameList();
         List<String> values = mapleResult.getValueList();
         MapleForClient maple = proc.getMapleClient();
 
         for(int i=0;i<fileNames.size();++i) {
             String fileName = fileNames.get(i);
             String value = values.get(i);
 
             if(MiscTool.requireToCreateFile(maple.getPidList(), proc.getIdentifier(), fileName)) {
                 if(!proc.getSDFS().hasSDFSFile(fileName)) {
                     proc.getSDFS().createLocalSDFSFile(fileName);
                 }
                 proc.getSDFS().appendDataToLocalFile(fileName, value);
             }
         }
 
     }
 
     private void setMaster(ProcessIdentifier masterProcess) {
         proc.setMaster(masterProcess);
     }
 
     private void putFile(ReadyToPutFileMessage readyToPutFileMessage){
         try {
             File file = proc.getSDFS().openFile(readyToPutFileMessage.getFilepath());
             FileInputStream in = new FileInputStream(file);
 
             String address = readyToPutFileMessage.getStoringProcess().getIP() + ":" +
                     Integer.toString(readyToPutFileMessage.getStoringProcess().getPort() + 2);
 
             TCPClient tcpClient = new TCPClient(address);
             tcpClient.setProc(proc);
             if(tcpClient.connect()){
                 tcpClient.sendData(proc.getId());
                 Integer hashCode = file.getName().hashCode();
                 hashCode = Math.abs(hashCode);
                 String fileHeader = String.format("%010d", hashCode);
                 tcpClient.sendData(fileHeader);
                 BufferedInputStream bis = new BufferedInputStream(in);
                 tcpClient.sendData(bis);
 
                 tcpClient.close();
             }
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
     }
 
     private void getFile(ReadyToGetFileMessage readyToGetFileMessage){
 
         long startTime = System.currentTimeMillis();
 
         String address = readyToGetFileMessage.getStoringProcess().getIP() + ":" +
                 Integer.toString(readyToGetFileMessage.getStoringProcess().getPort());
         TCPClient tcpClient = new TCPClient(address);
         tcpClient.setProc(proc);
         if(tcpClient.connect()){
             tcpClient.sendData(proc.getId());
             Integer hashCode = readyToGetFileMessage.getFid().getFileName().hashCode();
             hashCode = Math.abs(hashCode);
             String fileHeader = String.format("%010d", hashCode);
             tcpClient.sendData(fileHeader);
             tcpClient.receiveAndSaveData(readyToGetFileMessage.getFid().getFileName());
             tcpClient.close();
 
             FileIdentifier fileIdentifier = FileIdentifierFactory.generateFileIdentifier(
                     proc.getIdentifier(), readyToGetFileMessage.getFid().getFileName(), FileState.available);
 
             proc.getSDFS().addAvailableEntryToFileList(fileIdentifier, proc.getTimeStamp());
         }
 
         long useTime = System.currentTimeMillis() - startTime;
         logger.info("replicate " + readyToGetFileMessage.getFid().getFileName() + " uses " + useTime + " ms");
     }
 
     private void prepareToGet(ProcessIdentifier storingProcess, FileIdentifier fid){
         this.proc.getFileServer().prepareToGet(storingProcess, fid);
 
     }
 
     private void prepareToSend(ProcessIdentifier storingProcess, String SDFSfilepath){
         this.proc.getFileServer().prepareToSend(storingProcess, SDFSfilepath);
     }
 
 //    private void sendReadyToGetMessage(String SDFSFilepath, String processRequestingFile_IP, int processRequestingFile_port){
 //        String address = processRequestingFile_IP + ":" + Integer.toString(processRequestingFile_port);
 //        System.out.println(address);
 //        TCPClient tcpClient = new TCPClient(address);
 //        tcpClient.setProc(proc);
 //        if(tcpClient.connect()){
 //            Message m = MessagesFactory.generatePutFileMessage(SDFSFilepath, proc.getIdentifier());
 //            tcpClient.sendData(m);
 //            tcpClient.close();
 //        }
 //    }
 
     private void sendReadyToPutMessage(FileIdentifier fid, String processStoringFile_IP, int processStoringFile_port){
         String address = processStoringFile_IP + ":" + Integer.toString(processStoringFile_port);
         TCPClient tcpClient = new TCPClient(address);
         tcpClient.setProc(proc);
         if(tcpClient.connect()){
             Message m = MessagesFactory.generateReadyToPutFileMessage(fid.getFileName(), proc.getIdentifier());
             tcpClient.sendData(m);
             tcpClient.close();
         }
     }
 
     private void deleteFile(String SDFSfilepath){
         proc.getSDFS().deleteFile(SDFSfilepath, false);
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
 
     public String getRemoteIPAddress() {
         InetAddress inetAddress = socket.getInetAddress();
         return inetAddress.getHostAddress();
     }
 }
