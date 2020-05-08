 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.StringReader;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import edu.washington.cs.cse490h.lib.Callback;
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 
 public class SocialNetworkNode extends DFSNode {
 
   public static final String kFollowersFilename = "followers.txt";
   public static final String kWallFilename = "wall.txt";
   public static final String kRequestFilename = "requests.txt";
 
   // Name of the user currently logged in to this node. Null if no such user.
   String activeUser;
 
   // NOTE: This list currently only ever contains the user logged into the
   //       current node...
   List<String> loggedInUsers;
 
   // Queue of blocked wall posts.
   List<WallPost> queuedWallPosts;
 
 
   @Override
   public void start() {
     super.start();
     this.activeUser = null;
     this.loggedInUsers = new LinkedList<String>();
     this.queuedWallPosts = new LinkedList<WallPost>();
   }
 
 
   /**
    * True if a user is currently logged in.
    */
   private boolean hasActiveUser() {
     return activeUser != null;
   }
 
 
   /**
    * Generates a List<WallPost> from a newline-separated list of serialized
    * WallPost blobs.
    */
   private List<WallPost> getWall(String blob) {
     List<WallPost> wall = new ArrayList<WallPost>();
     // NOTE: Format of each WallPost in blob:
     //         <length of serialized WallPost in bytes>
     //         <serialized WallPost>
     //
     //       Thus, the split list comes in pairs of lengths and serialized
     //       WallPosts.
     StringReader strReader = new StringReader(blob);
     try {
       BufferedReader reader = new BufferedReader(strReader);
 
       if (!reader.ready())
         return wall;
 
       while (reader.ready()) {
         String line = reader.readLine();
         if (line == null)
           break;
 
         int binaryLength = Integer.parseInt(line);
         if (binaryLength <= 0)
           continue;
 
         if (!reader.ready())
           break;
 
         char[] binaryData = new char[binaryLength];
         reader.read(binaryData, 0, binaryLength);
         ByteArrayInputStream baIn = null;
         ObjectInputStream objIn = null;
         try {
           baIn = new ByteArrayInputStream(new String(binaryData).getBytes());
           objIn = new ObjectInputStream(baIn);
           WallPost wp = (WallPost) objIn.readObject();
           wall.add(wp);
         } catch (IOException e) {
           e.printStackTrace();
           return wall;
         } catch (ClassNotFoundException e) {
           e.printStackTrace();
           return wall;
         } finally {
           try {
             if (baIn != null)
               baIn.close();
             if (objIn != null)
               objIn.close();
           } catch (IOException e) {
             e.printStackTrace();
           }
         }
       }
     } catch (IOException e) {
       e.printStackTrace();
       return wall;
     }
 
     return wall;
   }
 
 
   /**
    * Load user's wall posts from file into a List.
    *
    * NOTE: Using ObjectInputStreams, etc even though followers are ints, in case
    *       we use a more concrete "follower object" later on.
    */
   private List<String> getFollowers(String username) {
     List<String> followers = new ArrayList<String>();
     try {
       PersistentStorageReader reader = getReader(username + "-" + kFollowersFilename);
       if (!reader.ready())
         return followers;
 
       // Gobble the first line of metadata.
       String line = reader.readLine();
       while (reader.ready()) {
         followers.add(reader.readLine());
         if (!reader.ready())
           break;
       }
     } catch (FileNotFoundException e) {
       return followers;
     } catch (IOException e) {
       e.printStackTrace();
       throw new IllegalStateException("Should not get here", e);
     }
 
     return followers;
   }
 
 
   /**
    * Start a transaction.
    */
   private void startTransaction() {
     clientAPI.startTransaction(createTransactionCb());
   }
 
 
   /**
    * Commit a transaction.
    */
   private void commitTransaction() {
     clientAPI.commitTransaction(createTransactionCb());
   }
 
 
   /**
    * Creates a user.
    * @param username
    *            Name of the user to create.
    */
   private void createUser(String username) {
     clientAPI.create(new DFSFilename("/dfs/0/" + username + "-" + kWallFilename),
                      createNonDataCb());
     clientAPI.create(new DFSFilename("/dfs/0/" + username + "-" +
                                      kFollowersFilename),
                      createNonDataCb());
     // Users follow themselves.
     // HACK: Temporarily set activeUser so followUser works properly
     activeUser = username;
     followUser(username);
     activeUser = null;
   }
 
 
   /**
    * Log in as the specified user.
    */
   private void login(String username) {
     if (loggedInUsers.indexOf(username) == -1) {
       activeUser = username;
       loggedInUsers.add(username);
     } else {
       System.err.println("Username " + username + " already logged in");
     }
   }
 
 
   /**
    * Log out the specified user.
    */
   private void logout(String username) {
     if (loggedInUsers.indexOf(username) != -1) {
       activeUser = null;
       loggedInUsers.remove(username);
     } else {
       System.err.println("Username " + username + " already logged out");
     }
   }
 
 
   /**
    * Adds a follower to the user's follower list.
    *
    * @param username
    *            The name of the user who is to follow someone.
    *
    * @param other
    *            The name of the user gaining a follower.
    */
   private void followUser(String other) {
     clientAPI.append(new DFSFilename("/dfs/0/" + activeUser + "-" + kFollowersFilename),
                      other + "\n", createFollowUserCb());
   }
 
 
   /**
    * DEPRECATED (probably)
    *
    * Facebook style friending is a bit more complex
 
   private void requestFriend(String username, String friendname) {
     clientAPI.put(new DFSFilename("/dfs/0/" + friendname + "-" + kRequestFilename),
                   username, createNonDataCb());
   }
 
   private void acceptFriend(String username) {
     try {
       PersistentStorageReader r = this.getReader(username + "-" + kRequestFilename);
       String friendname = r.readLine();
 
       //Facebook style friending is basically mutual follows.
       followUser(username, friendname);
       followUser(friendname, username);
 
       clientAPI.startTransaction(createTransactionCb());
       clientAPI.put(new DFSFilename("/dfs/0/" + username + "-" + kRequestFilename), "", createNonDataCb());
       clientAPI.commitTransaction(createTransactionCb());
 
     } catch (FileNotFoundException e) {
       System.err.println("file not found!");
     } catch (IOException ioe) {
       System.err.println("ioException!");
     }
   }
 
   /**
    * DEPRECATED (probably)
    *
    * Request to become a friend of the argument node.
 
   private void requestFriend(int target) {
     clientAPI.startTransaction(createTransactionCb());
     System.err.println("Requesting to friend" + target);
     clientAPI.commitTransaction(createTransactionCb());
   }
   */
 
 
   /**
    * Post a message to all followers.
    */
   private void postMsg(String msg) {
     queuedWallPosts.add(new WallPost(activeUser, new Date(), msg));
     clientAPI.get(new DFSFilename("/dfs/0/" + activeUser + "-" + kFollowersFilename),
                   createPostMsgCb());
   }
 
 
   /**
    * Print the contents of the user's wall.
    */
   private void readWall(String username) {
     clientAPI.get(new DFSFilename("/dfs/0/" + username + "-" + kWallFilename),
                   createReadWallCb());
   }
 
 
   /**
    * Serialize a WallPost using ObjectOutputStream.
    */
   private String serializeWallPost(WallPost wp) {
     ByteArrayOutputStream baOut = null;
     ObjectOutputStream objOut = null;
     try {
       baOut = new ByteArrayOutputStream();
       objOut = new ObjectOutputStream(baOut);
       objOut.writeObject(wp);
       baOut.close();
       objOut.close();
       return baOut.toString();
     } catch (IOException e) {
       e.printStackTrace();
       throw new IllegalStateException("Error writing wall");
     }
   }
 
 
   /**
    * Print the user's friends list.
    */
   private void listFollowers(String username) {
     List<String> followers = getFollowers(username);
     System.err.println("Node " + addr + "'s followers:");
     for (String f : followers) {
       System.err.println("Node " + f);
     }
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
 
     // Commands not requiring user to be logged in.
     if (action.equals("paxosinit")) {
       super.onCommand(command);
     } else if (action.equals("txstart")) {
       startTransaction();
     } else if (action.equals("txcommit")) {
       commitTransaction();
     } else if (action.equals("createuser")) {
       // TODO: DRY.
       String username = pieces[1];
       createUser(username);
     } else if (action.equals("login")) {
       String username = pieces[1];
       login(username);
     } else if (!hasActiveUser()) {
       // Subsequent commands require a logged-in user, so ensure that there
       // is one.
       //
       // TODO: Refactor a bit. It's possible for an invalid command to trigger
       //       this warning instead of an "Invalid command" message.
       System.err.println("Node " + addr + " attempting a " + action +
                          " command without a user logged in.");
     } else if (action.equals("logout")) {
       String username = pieces[1];
       logout(username);
     } else if (action.equals("follow")) {
       followUser(pieces[1]);
     } else if (action.equals("postmsg")) {
       // Join the tail of pieces to build the msg.
       String msg = "";
       for (String s : Arrays.copyOfRange(pieces, 1, pieces.length)) {
         msg += s + " ";
       }
       postMsg(msg.trim());
     } else if (action.equals("readwall")) {
       String username = pieces[1];
       readWall(username);
     } else if (action.equals("listfriends")) {
       String username = pieces[1];
       listFollowers(username);
     } else {
       System.err.println("Invalid command");
     }
   }
 
 
   /**** Callbacks ****/
 
 
   /**
    * Boilerplate for wall-reading callback.
    */
   protected Callback createFollowUserCb() {
     try {
       Method method;
       String[] paramTypes = { "java.lang.Exception",
                               "TransactionId",
                               "DFSFilename" };
       method = Callback.getMethod("followUserCb", this, paramTypes);
       return new Callback(method, this, null);
     } catch (NoSuchMethodException nsme) {
       assert(false): "Should never get here.";
       nsme.printStackTrace();
       System.exit(10);
       return null;
     } catch (ClassNotFoundException cnfe) {
       assert(false): "Should never get here.";
       cnfe.printStackTrace();
       System.exit(10);
       return null;
     }
   }
 
 
   /**
    * Message-posting callback.
    *
    * @param txId
    *            Transaction Id associated with the command.
    *
    * @param file
    *            The name of the file targeted by the command.
    *
    * @param e
    *            Exception that occurred, or null if none did.
    *
    * @param data
    *            Data/textual portion of command response.
    */
   public void followUserCb(Exception e, TransactionId txId,
                            DFSFilename file) {
    if (e != null || ((DFSExceptions.FileSystemException) e).code != ErrorCode.Success) {
       System.err.println("Node " + addr + " encountered an error while trying " +
                          "to add follower to " + file.getPath() + ":");
       e.printStackTrace();
     } else {
       System.err.println("Successfully added follower to " + file.getPath());
     }
   }
 
 
   /**
    * Boilerplate for wall-reading callback.
    */
   protected Callback createReadWallCb() {
     try {
       Method method;
       String[] paramTypes = { "java.lang.Exception",
                               "TransactionId",
                               "DFSFilename",
                               "java.lang.String" };
       method = Callback.getMethod("readWallCb", this, paramTypes);
       return new Callback(method, this, null);
     } catch (NoSuchMethodException nsme) {
       assert(false): "Should never get here.";
       nsme.printStackTrace();
       System.exit(10);
       return null;
     } catch (ClassNotFoundException cnfe) {
       assert(false): "Should never get here.";
       cnfe.printStackTrace();
       System.exit(10);
       return null;
     }
   }
 
 
   /**
    * Returns the username encoded in the argument filename.
    */
   private String getUsernameFromFilename(String filename) {
     return filename.split("-")[0];
   }
 
 
   /**
    * Wall-reading callback.
    *
    * @param txId
    *            Transaction Id associated with the command.
    *
    * @param file
    *            The name of the file targeted by the command.
    *
    * @param e
    *            Exception that occurred, or null if none did.
    *
    * @param data
    *            Data/textual portion of command response.
    */
   public void readWallCb(Exception e, TransactionId txId,
                          DFSFilename file, String data) {
    if (e != null || ((DFSExceptions.FileSystemException) e).code != ErrorCode.Success) {
       System.err.println("Node " + addr + " encountered an error while fetching " +
                          file.getPath() + ":");
       e.printStackTrace();
       return;
     }
 
     String username = getUsernameFromFilename(file.getPath());
     List<WallPost> wall = getWall(data);
     System.err.println(username + "'s wall:");
     for (WallPost wp : wall) {
       System.err.println("At " + wp.timestamp + ", " + wp.author + " wrote: " +
                          wp.text + "\n\n");
     }
   }
 
 
   /**
    * Boilerplate for wall-reading callback.
    */
   protected Callback createPostMsgCb() {
     try {
       Method method;
       String[] paramTypes = { "java.lang.Exception",
                               "TransactionId",
                               "DFSFilename",
                               "java.lang.String" };
       method = Callback.getMethod("postMsgCb", this, paramTypes);
       return new Callback(method, this, null);
     } catch (NoSuchMethodException nsme) {
       assert(false): "Should never get here.";
       nsme.printStackTrace();
       System.exit(10);
       return null;
     } catch (ClassNotFoundException cnfe) {
       assert(false): "Should never get here.";
       cnfe.printStackTrace();
       System.exit(10);
       return null;
     }
   }
 
 
   /**
    * Message-posting callback.
    *
    * @param txId
    *            Transaction Id associated with the command.
    *
    * @param file
    *            The name of the file targeted by the command.
    *
    * @param e
    *            Exception that occurred, or null if none did.
    *
    * @param data
    *            Data/textual portion of command response.
    */
   public void postMsgCb(Exception e, TransactionId txId,
                         DFSFilename file, String data) {
    if (e != null || ((DFSExceptions.FileSystemException) e).code != ErrorCode.Success) {
       System.err.println("Node " + addr + " encountered an error while trying " +
                          "to post a message to " + file.getPath() + ":");
       e.printStackTrace();
       return;
     }
 
     for (String follower : data.split("\n")) {
       Iterator<WallPost> it = queuedWallPosts.iterator();
       while (it.hasNext()) {
         WallPost wp = it.next();
         String serialized = serializeWallPost(wp);
         clientAPI.append(new DFSFilename("/dfs/0/" + follower + "-" + kWallFilename),
                          new String(serialized.length() + "\n" + serialized),
                          createNonDataCb());
       }
     }
   }
 
 }
