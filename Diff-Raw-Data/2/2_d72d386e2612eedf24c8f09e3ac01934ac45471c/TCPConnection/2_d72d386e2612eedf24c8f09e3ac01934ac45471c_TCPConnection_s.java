 package communication;
 
 import membership.Proc;
 import misc.MiscTool;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.util.Arrays;
 
 import static communication.Messages.JoinMessage;
 import static communication.Messages.Message;
 import static communication.Messages.ProcessIdentifier;
 
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
             }
         }
     }
 
     public void sendData(byte[] bytes) {
         try {
             os.write(bytes);
         } catch (IOException e) {
             logger.error("Sending TCP packets error" + e);
             e.printStackTrace();
         }
     }
 
     private void handle(Message m) {
         proc.increaseAndGetTimeStamp();
 
         switch (m.getType()) {
             case Join:
                 JoinMessage joinMessage = m.getJoinMessage();
                 ProcessIdentifier joinedMachine = joinMessage.getJoinedMachine();
 //                ProcessIdentifier remoteProcessIdentifier = generateRemoteProcessIdentifier(joinedMachine);
 //                proc.addProcToMemberList(remoteProcessIdentifier);
                 proc.addProcToMemberList(joinedMachine);
             default:
                 break;
         }
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
 
 }
