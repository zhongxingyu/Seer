 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
import com.sun.tools.javac.code.Flags;
 
 import DFSClient.RequestWrapper;
 
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 import edu.washington.cs.cse490h.lib.Utility;
 
 public class DFSServer extends DFSComponent {
 
   public static final String tempFileName = ".temp";
   
   private LinkedList<Integer> requestQueue;
   private HashMap<Integer, RequestWrapper> issuedCommands;
 
   public DFSServer(DFSNode parent) {
     super(parent);
   }
 
   public void start() {
     requestQueue = new LinkedList<Integer>();
     issuedCommands = new HashMap<Integer, RequestWrapper>();
     System.out.println("Starting the DFSNode...");
 
     // Check for a temp file.
     if (Utility.fileExists(parent, tempFileName)) {
       try {
         PersistentStorageReader tempReader = this.getReader(tempFileName);
         PersistentStorageWriter tempDeleter = this.getWriter(tempFileName, false);
         if (tempReader.ready()) {
           String filename = tempReader.readLine();
           String oldContent = readRemainingContentsToString(tempReader);
 
           PersistentStorageWriter writer = this.getWriter(filename, false);
           writer.write(oldContent);
           writer.close();
         }
         tempDeleter.delete();
       } catch(FileNotFoundException fnfe) {
     	// File must have been deleted since the check.
       } catch(IOException ioe) {
         // AUDIT(andrew): Shouldn't we at least error here?
       }
     }
   }
 
   @Override
   public void onRIOReceive(Integer from, DFSMessage msg) {
     String filename = ((FileNameMessage) msg).getFileName();
     DFSMessage response;
 
     // Demux on the message type and dispatch accordingly.
     switch (msg.getMessageType()) {
     case Create:
       response = handleCreateRequest(filename);
       break;
     case Get:
       response = handleGetRequest(filename);
       break;
     case Put:
       response = handlePutRequest(filename, (PutMessage) msg);
       break;
     case Append:
       response = handleAppendRequest(filename, (AppendMessage) msg);
       break;
     case Delete:
       response = handleDeleteRequest(filename);
       break;
     case SyncRequest:
       response = handleSyncRequest(filename, (SyncRequestMessage) msg);
     	break;
     case SyncData:
       response = handleSyncData(filename, (SyncDataMessage) msg);
       break;
     default:
       // TODO: Exception fixit. Giving error code 1 so the bitch'll compile.
       response = new ResponseMessage(5);
     }
 
     System.out.println("Server (addr " + this.addr + ") sending: " + response.toString());
     RIOSend(from, Protocol.DATA, response.pack());
 
   }
   
   private DFSMessage handleSyncRequest(String filename, SyncRequestMessage msg) {
     DFSMessage response;
     
     if(!Utility.fileExists(parent, filename)) {
       response = new ResponseMessage(10);
     } else {
       if (msg.version < getVersion(filename)) { //server has most recent version
         try {
     	  response = new SyncDataMessage(filename, msg.version, msg.flags, readFileToString(filename));
         } catch (FileNotFoundException fnfe) {
           response = new ResponseMessage(10);
         } catch (IOException ioe) {
           response = new ResponseMessage(5);
         }
       } else { //client had the most recent version
         response = new SyncRequestMessage(filename, getVersion(filename), msg.flags);
       }
     }
     return response;
   }
   
   private ResponseMessage handleSyncData(String filename, SyncDataMessage msg) {
 	DFSMessage response;
 	
 	handleDeleteRequest(filename);
 	handleCreateRequest(filename);
 	handlePutRequest(filename, (FileMessage) msg);
 	
 	response = new SyncRequestMessage(filename, getVersion(filename), msg.flags);
   }
 
   /**
    * Creates the target file.
    *
    * Generates and returns a response containing the appropriate error code.
    *
    * @param filename
    *            The name of the file to create.
    * @return A ResponseMessage containing the appropriate error code.
    */
   private DFSMessage handleCreateRequest(String filename) {
     ResponseMessage response;
     PersistentStorageWriter writer;
 
     // Test existence of filename.
     if (Utility.fileExists(parent, filename)) {
       response = new ResponseMessage(11);
     } else {
       try {
         writer = this.getWriter(filename, false);
         writer.close();
 
         // File created properly, so we respond with happy error code.
         response = new ResponseMessage(0);
       } catch (IOException ioe) {
         // TODO: Exception fixit.
         response = new ResponseMessage(5);
       }
     }
 
     return response;
   }
 
 
   /**
    * Gets the contents of the target file.
    *
    * @param filename
    *            The name of the file of which to get contents.
    * @return A ResponseMessage containing error code and (hopefully)
    *         contents of the target file.
    */
   private ResponseMessage handleGetRequest(String filename) {
     ResponseMessage response;
 
     if (!Utility.fileExists(parent, filename)) {
       response = new ResponseMessage(10);
     } else {
       try {
         response = new DataResponseMessage(0, readFileToString(filename));
       } catch (FileNotFoundException fnfe) {
         response = new ResponseMessage(10);
       } catch (IOException ioe) {
         // TODO: Exception fixit.
         response = new ResponseMessage(5);
       }
     }
 
     return response;
   }
 
 
   /**
    * Writes data to a file.
    *
    * @param filename The name of the file to which to write.
    * @param
    * @return A ResponseMessage containing the appropriate error code.
    */
   private ResponseMessage handlePutRequest(String filename, FileMessage putmessage) {
     ResponseMessage response;
     PersistentStorageWriter tempWriter;
     PersistentStorageWriter writer;
 
     if (Utility.fileExists(parent, filename)) {
       try {
         tempWriter = this.getWriter(tempFileName, false);
         String existingContent = readFileToString(filename);
         tempWriter.write(filename + "\n");
         tempWriter.write(existingContent);
         tempWriter.close();
 
         // Write to  target file.
         writer = this.getWriter(filename, false);
         System.out.println("String to write to " + filename +
                            " in put request: " + putmessage.getData());
         writer.write(putmessage.getData());
 
         // Delete temp file.
         // TODO: Should probably use File.delete() here. See HandleDeleteRequest.
         PersistentStorageWriter tempDeleter = this.getWriter(tempFileName, false);
         if (!tempDeleter.delete())
           System.out.println(filename + " failed to delete");
 
         response = new ResponseMessage(0);
       } catch(IOException ioe) {
         ioe.printStackTrace();
         response = new ResponseMessage(5);
       }
     } else {
       response = new ResponseMessage(10);
     }
 
     return response;
   }
 
 
   /**
    * Appends data to the end of afile.
    *
    * @param filename The name of the file to which to append.
    * @param
    * @return A ResponseMessage containing the appropriate error code.
    */
   private ResponseMessage handleAppendRequest(String filename, AppendMessage appendmessage) {
     ResponseMessage response;
     PersistentStorageWriter writer;
 
     if (Utility.fileExists(parent, filename)) {
       try {
         // Working on the write file.
         writer = this.getWriter(filename, true);
         writer.write(appendmessage.getData());
         writer.close();
         response = new ResponseMessage(0);
       } catch(IOException ioe) {
         ioe.printStackTrace();
         response = new ResponseMessage(5);
       }
     } else {
       response = new ResponseMessage(10);
     }
 
     return response;
   }
 
 
   /**
    * Attempts to delete the target file.
    *
    * @param filename
    *            The name of the file to delete
    * @return A ResponseMessage containing the appropriate error code.
    */
   private ResponseMessage handleDeleteRequest(String filename) {
     ResponseMessage response;
 
     if (!Utility.fileExists(parent, filename)) {
       // File doesn't exist, so we return appropriate error code.
       response = new ResponseMessage(10);
     } else {
       File f = new File("storage/" + this.addr + "/" + filename);
       try {
         if (f.delete()) {
           response = new ResponseMessage(0);
         } else {
           // Delete failed, so return a generic error.
           // TODO: Exception fixit.
           response = new ResponseMessage(1);
         }
       } catch (SecurityException se) {
         response = new ResponseMessage(5);
       }
     }
 
     return response;
   }
 
 }
