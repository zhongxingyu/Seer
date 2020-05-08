 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Reader;
 import java.lang.reflect.Method;
 import java.nio.CharBuffer;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import edu.washington.cs.cse490h.lib.Callback;
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 import edu.washington.cs.cse490h.lib.Utility;
 
 public class DFSNode extends RIONode {
 
   private HashMap<Integer, RequestWrapper> issuedCommands;
   private LinkedList<Integer> requestQueue;
 
   // Bundles messages with recipient's ID for use in requestQueue.
   public static class RequestWrapper {
     DFSMessage msg;
     int recipient;
 
     public RequestWrapper(DFSMessage msg, int recipient) {
       this.msg = msg;
       this.recipient = recipient;
     }
   }
 
 
   @Override
   public void start() {
     requestQueue = new LinkedList<Integer>();
     System.out.println("Starting the DFSNode...");
 
     // Check for a temp file.
     if (Utility.fileExists(this, ".temp")) {
       try {
         PersistentStorageReader tempReader = this.getReader(".temp");
         PersistentStorageWriter tempDeleter = this.getWriter(".temp", false);
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
 
 
   /**
    * Callback for network exceptions.
    *
    * @param seqno
    *            Sequence number of finished packet.
    *
    * @param e
    *           Exception thrown by possible failure of packet.
    */
   public void networkFailureCb(int seqno, Exception e) {
     RequestWrapper request = issuedCommands.remove(seqno);
     DFSMessage message = request.msg;
     if (e != null) {
       e.printStackTrace();
     } else {
       System.err.print("Node " + this.addr + ": Error: ");
 
       // Print command.
       switch (message.getMessageType()) {
       case Create:
         System.err.print("create");
         break;
       case Get:
         System.err.print("get");
         break;
       case Put:
         System.err.print("put");
         break;
       case Append:
         System.err.print("append");
         break;
       case Delete:
         System.err.print("delete");
         break;
       default:
         System.err.print("invalid command");
       }
 
       System.err.println(" on server " + request.recipient + " and file " +
                          ((FileNameMessage) message).getFileName());
     }
   }
 
 
   @Override
   public void onCommand(String command) {
     DFSMessage msg;
     String[] pieces = command.split("\\s");
     String action = pieces[0];
     // TODO: Validate server int.
     int server = Integer.parseInt(pieces[1]);
     String filename = pieces[2];
     String contents = "";
     for (int i = 3; i < pieces.length; i++) {
       contents += pieces[i] + " ";
     }
     contents = contents.trim();
 
     // Construct the appropriate message type.
     if (action.equals("create")) {
       msg = new CreateMessage(filename);
     } else if (action.equals("get")) {
       msg = new GetMessage(filename);
     } else if (action.equals("put")) {
       msg = new PutMessage(filename, contents);
     } else if (action.equals("append")) {
       msg = new AppendMessage(filename, contents);
     } else if (action.equals("delete")) {
       msg = new DeleteMessage(filename);
     } else {
       System.err.println("Invalid command");
       return;
     }
 
     // Construct a Callback for the request.
    // TODO URGENT: Why doesn't this work..?
    String[] paramTypes = {"java.lang.Integer", "java.lang.Exception"};
     Method method;
     Callback cb;
     try {
       method = Callback.getMethod("networkFailureCb", this, paramTypes);
       cb = new Callback(method, this, new Object[]{msg, server});
     } catch (NoSuchMethodException nsme) {
       assert(false): "Should never get here.";
       nsme.printStackTrace();
       return;
     } catch (ClassNotFoundException cnfe) {
       assert(false): "Should never get here.";
       cnfe.printStackTrace();
       return;
     }
 
     // Pack and send the message.
     byte[] packedMsg = msg.pack();
     System.out.println("Client (addr " + this.addr + ") sending: " +
                        msg.toString());
     int seqno = RIOSend(server, Protocol.RIOTEST_PKT, packedMsg, cb);
     requestQueue.add(seqno);
     issuedCommands.put(seqno, new RequestWrapper(msg, server));
   }
 
 
   @Override
   public void onRIOReceive(Integer from, int protocol, byte[] msg) {
     DFSMessage unpacked = DFSMessage.unpack(msg);
     DFSMessage.MessageWireType msgType = unpacked.getMessageType();
     System.err.println("Got msg: " + unpacked.toString());
 
     if (msgType.equals(DFSMessage.MessageWireType.Response) ||
         msgType.equals(DFSMessage.MessageWireType.DataResponse)) {
       // We're a client receiving a response.
       ResponseMessage response = (ResponseMessage) unpacked;
       int seqno = requestQueue.remove();
       RequestWrapper issuedReq = issuedCommands.remove(seqno);
       DFSMessage issuedMsg = issuedReq.msg;
       if (response.getCode() != 0) {
         System.err.print("Node " + this.addr + ": Error: ");
 
         // Print command.
         switch (issuedMsg.getMessageType()) {
         case Create:
           System.err.print("create");
           break;
         case Get:
           System.err.print("get");
           break;
         case Put:
           System.err.print("put");
           break;
         case Append:
           System.err.print("append");
           break;
         case Delete:
           System.err.print("delete");
           break;
         default:
           System.err.print("invalid command");
         }
 
         System.err.println(" on server " + from + " and file " +
                            ((FileNameMessage) issuedMsg).getFileName() +
                            " returned error code " + response.getCode());
       }
     } else {
       // We're a server receiving a command.
       String filename = ((FileNameMessage) unpacked).getFileName();
       ResponseMessage response;
 
       // Demux on the message type and dispatch accordingly.
       switch (msgType) {
       case Create:
         response = handleCreateRequest(filename);
         break;
       case Get:
         response =  handleGetRequest(filename);
         break;
       case Put:
         response = handlePutRequest(filename, (PutMessage) unpacked);
         break;
       case Append:
         response = handleAppendRequest(filename, (AppendMessage) unpacked);
     	break;
       case Delete:
     	response = handleDeleteRequest(filename);
         break;
       default:
         // TODO: Exception fixit. Giving error code 1 so the bitch'll compile.
         response = new ResponseMessage(5);
       }
 
       System.out.println("Server (addr " + this.addr + ") sending: " + response.toString());
       RIOSend(from, Protocol.RIOTEST_PKT, response.pack());
     }
   }
 
   public String readFileToString(String filename) throws IOException {
     PersistentStorageReader reader = this.getReader(filename);
     return readRemainingContentsToString(reader);
   }
 
   public String readRemainingContentsToString(Reader reader) throws IOException {
     StringBuilder sb = new StringBuilder();
 
     char[] cbuf = new char[1024];
     int bytesRead = reader.read(cbuf, 0, 1024);
     while(bytesRead != -1) {
       sb.append(cbuf, 0, bytesRead);
       bytesRead = reader.read(cbuf, 0, 1024);
     }
 
     return sb.toString();
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
   private ResponseMessage handleCreateRequest(String filename) {
     ResponseMessage response;
     PersistentStorageWriter writer;
 
     // Test existence of filename.
     if (Utility.fileExists(this, filename)) {
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
 
     if (!Utility.fileExists(this, filename)) {
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
   private ResponseMessage handlePutRequest(String filename, PutMessage putmessage) {
     ResponseMessage response;
     PersistentStorageWriter tempWriter;
     PersistentStorageWriter writer;
 
     if (Utility.fileExists(this, filename)) {
       try {
         tempWriter = this.getWriter(".temp", false);
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
         PersistentStorageWriter tempDeleter = this.getWriter(".temp", false);
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
 
     if (Utility.fileExists(this, filename)) {
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
 
     if (!Utility.fileExists(this, filename)) {
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
 
 
   /**
    * Builds a properly-formatted response byte[] to be sent from a server
    * to a client.
    *
    * @param wire
    *            The wireID of the response to be sent.
    * @param str
    *            The response string to be sent.
    * @return A properly-formatted byte[] to be sent as a reponse to a client.
    */
   public byte[] prepareResponse(int wire, String str) {
     byte[] wireId = new byte[] {
       (byte) (wire >>> 24),
       (byte) (wire >>> 16),
       (byte) (wire >>> 8),
       (byte) wire };
 
     byte[] payload = Utility.stringToByteArray(str);
 
     byte[] response = new byte[wireId.length+payload.length];
     System.arraycopy(wireId, 0, response, 0, wireId.length);
     System.arraycopy(payload, 0, response, wireId.length, payload.length);
 
     return response;
   }
 
 }
