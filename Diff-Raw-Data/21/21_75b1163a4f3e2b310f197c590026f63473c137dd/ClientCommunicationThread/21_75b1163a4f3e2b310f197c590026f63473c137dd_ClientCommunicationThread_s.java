 package elka.clouddir.server.communication;
 
 import elka.clouddir.server.model.AbstractFileInfo;
 import elka.clouddir.server.serverevents.*;
 import elka.clouddir.shared.LoginInfo;
 import elka.clouddir.shared.Message;
 import elka.clouddir.shared.RenameInfo;
 import elka.clouddir.shared.TransmissionEnd;
 
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 
 /**
  * Wątek odpowiadający za komunikację z klientem
  * @author Michał Toporowski
  */
 public class ClientCommunicationThread extends Thread
 {
     private final   ObjectOutputStream  out;
     private final   ObjectInputStream   in;
 
     private final BlockingQueue<ServerEvent> serverEventQueue;
 
     private         boolean             running;
 
 
 //    static Map<Message, MessageProcesser> processerMap;
 //    static {
 //        initMap();
 //    }
 
     public ClientCommunicationThread(Socket clientSocket, BlockingQueue<ServerEvent> serverEventQueue) throws IOException {
         this.serverEventQueue = serverEventQueue;
         out = new ObjectOutputStream(clientSocket.getOutputStream());
         in = new ObjectInputStream(clientSocket.getInputStream());
     }
 
     @Override
     public void run()
     {
         running = true;
         while (running) {
             try {
                 Message message = (Message)in.readObject();
 
                 //pobranie dodatkowych danych
                 ServerEvent event = processMessage(message);
 
                 //to po to, aby sprawdzić, że poprawnie zakończono transmisję
 //                TransmissionEnd transmissionEnd = (TransmissionEnd)in.readObject();
 
                 //ok - wyślij do serwera
                 serverEventQueue.put(event);
            } catch (Exception e) {
            	running = false;
//                e.printStackTrace();
            	System.out.println("Connection was closed");
             }
 
         }
     }
 
 
     /**
      * Wysyła obiekt do klienta
      * @param object
      * @throws IOException
      */
     public void sendObject(final Serializable object) throws IOException {
         out.writeObject(object);
     }
 
 
     private ServerEvent processMessage(final Message message) throws IOException, ClassNotFoundException, InterruptedException {
         System.out.println("Received message \"" + message.toString() + "\"");
         switch (message) {
             case LOGIN_REQUEST:
                 LoginInfo loginInfo = (LoginInfo) in.readObject();
                 return new LoginRequestEvent(this, loginInfo);
             case FILE_CHANGED: {
                 AbstractFileInfo metadata = (AbstractFileInfo) in.readObject();
                 return new FileChangedEvent(this, metadata);
             }
             case FILE_DELETED: {
                 AbstractFileInfo metadata = (AbstractFileInfo) in.readObject();
                 return new FileDeletedEvent(this, metadata);
             }
             case FILEPATH_CHANGED: {
                 RenameInfo renameInfo = (RenameInfo) in.readObject();
                 return new FilePathChangedEvent(this, renameInfo);
             }
             case FULL_METADATA_TRANSFER: {
                 AbstractFileInfo[] metadata = (AbstractFileInfo[]) in.readObject();
                 return new FullMetadataTransferEvent(this, metadata);
             }
             case FILE_TRANSFER:
                 AbstractFileInfo metadata = (AbstractFileInfo) in.readObject();
                 byte[] data = (byte[]) in.readObject();
                 return new FileTransferEvent(this, metadata, data);
             default:
                 throw new UnsupportedOperationException("Operation not implemented");
         }
     }
 
 
 }
