 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Vector;
 
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 import edu.washington.cs.cse490h.lib.Utility;
 
 
 public class DFSServer extends DFSComponent {
 
   public static final String tempFileName = ".temp";
   private HashMap<Integer, DFSClient.RequestWrapper> issuedCommands;
   private HashMap<String, Vector<Integer>> readCheckOuts;
   private HashMap<String, Integer> whoOwns;
  private PersistentStorageCache cache;
 
   public DFSServer(DFSNode parent) {
     super(parent);
   }
 
 
   public void start() {
     issuedCommands = new HashMap<Integer, DFSClient.RequestWrapper>();
     readCheckOuts = new HashMap<String, Vector<Integer>>();
    cache = new PersistentStorageCache(parent, this, true);
 
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
       response = handleSyncRequest(from, filename, (SyncRequestMessage) msg);
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
 
 
   private DFSMessage handleSyncRequest(Integer from, String filename, SyncRequestMessage msg) {
     DFSMessage response;
 
     Flags f = msg.getFlags();
     if (f.isSet(SyncFlags.TransferOwnership)) {
       response = handleTransferOwnershipRequest(from, filename, msg);
     }
     if (f.isSet(SyncFlags.Create)) {
       response = handleCreateSyncRequest(from, filename, msg);
     }
     if (f.isSet(SyncFlags.ReadOnly)) {
       response = handleReadOnlySyncRequest(from, filename, msg);
     }
 
 
     return response;
   }
 
   private DFSMessage handleTransferOwnershipRequest(Integer from, String filename, SyncRequestMessage msg) {
     DFSMessage response;
 
   }
 
   private DFSMessage handleCreateSyncRequest(Integer from, String filename, SyncRequestMessage mgs) {
 	return null;
   }
 
   private DFSMessage handleReadOnlySyncRequest(Integer from, String filename, SyncRequestMessage msg) {
     DFSMessage response;
    if(!Utility.fileExists(parent, filename)) {
       response = new ResponseMessage(10);
     } else {
      if (msg.version < cache.get(new DFSFilename(filename)).getVersion()) { //server has most recent version
         try {
           Flags f = new Flags(0);
     	  response = new SyncDataMessage(filename, msg.version, null, readFileToString(filename)); //
     	  checkOutForRead(from, filename);
 
         } catch (FileNotFoundException fnfe) {
           response = new ResponseMessage(10);
         } catch (IOException ioe) {
           response = new ResponseMessage(5);
         }
       } else { //client had the most recent version
        response = new SyncRequestMessage(filename,
                                          cache.get(new DFSFilename(filename))
                                          .getVersion(), msg.flags);
       }
     }
 	return response;
   }
 
   private void checkOutForRead(Integer from, String filename) {
 	  //add to readCheckOuts
 	  Vector<Integer> v = readCheckOuts.get(filename);
 	  if(v == null) {
         v = new Vector<Integer>();
         v.add(from);
         readCheckOuts.put(filename, v);
 	  } else {
         v.add(from);
       }
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
