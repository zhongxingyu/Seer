 import java.lang.reflect.Method;
 import java.util.HashMap;
 import edu.washington.cs.cse490h.lib.Callback;
 
 public class DFSNode extends RIONode {
 
   DFSServer server;
   HashMap<Integer, DFSClient> clients;
 
 
   @Override
   public void start() {
     server = new DFSServer(this);
     clients = new HashMap<Integer, DFSClient>();
   }
 
 
   /**
    * Creates a DFSClient for the specified server and inserts it into the
    * clients hash.
    *
    * @param id
    *            ID of the server of which to create a client.
   * @return The create DFSClient.
    */
   private DFSClient createClient(int id) {
     DFSClient cl = new DFSClient(this, id);
     clients.put(id, cl);
     return cl;
   }
 
 
   /**
    * Test user callback.
    *
    * @param file
    *            The name of the file targeted by the command.
    *
    * @param e
    *            Arbitrary exception passed to callback.
    *
    * @param data
    *            Data/textual portion of command response.
    */
   public void getCommandComplete(DFSFilename file, String data, Exception e) {
     if (e != null) {
       System.err.println("Error:");
       e.printStackTrace();
     } else {
       System.err.println("Contents of " + file + ":");
       System.err.println(data);
     }
   }
 
 
   public void commandComplete(DFSFilename file, Exception e) {
     System.err.println("Command on " + file + " complete!");
   }
 
 
   /**
    * Routes commands from the user or a file to the proper client method.
    *
    * @param command
    *            String-representation of a DFS command.
    */
   @Override
   public void onCommand(String command) {
     String[] pieces = command.split("\\s");
     String action = pieces[0];
     // TODO: Validate server ID?
     String filename = pieces[1];
     String contents = "";
     for (int i = 2; i < pieces.length; i++) {
       contents += pieces[i] + " ";
     }
     contents = contents.trim();
 
     // Construct the appropriate message type.
     int serverId;
     try {
       serverId = new DFSFilename(filename).getOwningServer();
     } catch (IllegalArgumentException iae) {
       System.err.println(iae.getMessage());
       return;
     }
 
     DFSClient cl = clients.get(serverId);
     if (cl == null) {
       // If no client for specified server, then create one.
       cl = createClient(serverId);
     }
 
     // TODO: We should only construct one cb obj, depending on command type.
     // Construct a Callback for the requests that don't return data.
     Callback nonDataCb;
     Method method;
     try {
       String[] paramTypes = { "DFSFilename", "java.lang.String",
                               "java.lang.Exception" };
       method = Callback.getMethod("getCommandComplete", this, paramTypes);
       nonDataCb = new Callback(method, this, null);
     } catch (NoSuchMethodException nsme) {
       assert(false): "Should never get here.";
       nsme.printStackTrace();
       return;
     } catch (ClassNotFoundException cnfe) {
       assert(false): "Should never get here.";
       cnfe.printStackTrace();
       return;
     }
 
     // Construct a Callback for the requests that return data.
     Callback dataCb;
     try {
       String[] paramTypes = { "DFSFilename", "java.lang.Exception" };
       method = Callback.getMethod("commandComplete", this, paramTypes);
       dataCb = new Callback(method, this, null);
     } catch (NoSuchMethodException nsme) {
       assert(false): "Should never get here.";
       nsme.printStackTrace();
       return;
     } catch (ClassNotFoundException cnfe) {
       assert(false): "Should never get here.";
       cnfe.printStackTrace();
       return;
     }
 
     // Route the command to the appropriate client method.
     if (action.equals("create")) {
       cl.create(filename, nonDataCb);
     } else if (action.equals("get")) {
       cl.get(filename, dataCb);
     } else if (action.equals("put")) {
       cl.put(filename, contents, nonDataCb);
     } else if (action.equals("append")) {
       cl.append(filename, contents, nonDataCb);
     } else if (action.equals("delete")) {
       cl.delete(filename, nonDataCb);
     } else {
       System.err.println("Invalid command");
     }
   }
 
 
   /**
    * DFSNode receive method.
    *
    * Called whenever a message is received from the RIO layer. Routes the
    * message to either our server logic or one of our client sessions based
    * on the file name given in the message.
    */
   @Override
   public void onRIOReceive(Integer from, int protocol, byte[] msg) {
     DFSMessage unpacked = DFSMessage.unpack(msg);
     if (!(unpacked instanceof FileNameMessage)) {
       System.err.println("Bad message received: " + unpacked.toString());
       return;
     }
 
     // Route the message to the client or server, depending on owning server
     // of target file.
     FileNameMessage filemsg = (FileNameMessage) unpacked;
     System.err.print("DFSNode:" + addr + ": got pkt for ");
     if (filemsg.getDFSFileName().getOwningServer() == addr) {
       System.err.println("server");
       server.onReceive(from, filemsg);
     } else {
       DFSClient client = clients.get(from);
       if (client != null) {
         System.err.println("client");
         client.onReceive(from, filemsg);
       } else {
         System.err.println("nonexistent client");
       }
     }
   }
 
 }
