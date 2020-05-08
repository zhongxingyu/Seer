 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.StringReader;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import edu.washington.cs.cse490h.lib.Callback;
 
 public class SocialNetworkNode extends DFSNode {
 
   public static final String kFollowersFilename = "followers.txt";
   public static final String kWallFilename = "wall.txt";
 
   // Name of the user currently logged in to this node. Null if no such user.
   String activeUser;
 
   // Queue of blocked wall posts.
   List<WallPost> queuedWallPosts;
 
 
   @Override
   public void start() {
     super.start();
     this.activeUser = null;
     this.queuedWallPosts = new LinkedList<WallPost>();
   }
 
 
   /**
    * True if a user is currently logged in.
    */
   private boolean hasActiveUser() {
     return activeUser != null;
   }
 
 
   /**
    * Returns the username encoded in the argument filename.
    */
   private String getUsernameFromFilename(String filename) {
     return filename.split("-")[0];
   }
 
 
   /**
    * Generates a List<WallPost> from a newline-separated list of serialized
    * WallPost blobs.
    */
   private List<WallPost> getWall(String blob) {
     List<WallPost> wall = new ArrayList<WallPost>();
     // NOTE: Format of each WallPost in blob:
     //         <length of serialized WallPost in bytes><newline>
     //         <serialized WallPost>
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
    */
   private List<String> getFollowers(String data) {
     return new ArrayList<String>(Arrays.asList(data.split("\n")));
   }
 
 
 
   /******** Commands ********/
 
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
     if (activeUser != null) {
       System.err.println("Node " + addr + " attempting to login when there " +
                          "is already a logged-in user. (username=" + username +
                          ", activeUser=" + activeUser);
       return;
     }
     activeUser = username;
   }
 
 
   /**
    * Log out the specified user.
    */
   private void logout() {
     activeUser = null;
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
     System.err.println("\n\n\n\n\n\n\n" + activeUser + " is attempting to follow " + other + "\n\n\n\n\n\n\n\n");
     clientAPI.append(new DFSFilename("/dfs/0/" + other + "-" + kFollowersFilename),
                      activeUser + "\n", createFollowUserCb());
   }
 
 
   /**
    * Post a message to all followers.
    */
   private void postMsg(String msg) {
     queuedWallPosts.add(new WallPost(activeUser, new Date(), msg));
     clientAPI.get(new DFSFilename("/dfs/0/" + activeUser + "-" + kFollowersFilename),
                   createPostMsgCb());
   }
 
 
   /**
    * Print the contents of a user's wall.
    */
   private void readWall(String username) {
     clientAPI.get(new DFSFilename("/dfs/0/" + username + "-" + kWallFilename),
                   createReadWallCb());
   }
 
 
   /**
    * Print a user's friends list.
    */
   private void listFollowers(String username) {
     clientAPI.get(new DFSFilename("/dfs/0/" + username + "-" + kFollowersFilename),
                   createListFollowersCb());
   }
 
 
   /**
    * Routes commands from the user or a file to the proper client method.
    *
    * @param command
    *            String-representation of a SocialNetworkNode command.
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
       createUser(pieces[1]);
     } else if (action.equals("login")) {
       login(pieces[1]);
     } else if (!hasActiveUser()) {
       // Subsequent commands require a logged-in user, so ensure that there
       // is one.
       //
       // TODO: Refactor a bit. It's possible for an invalid command to trigger
       //       this warning instead of an "Invalid command" message.
       System.err.println("Node " + addr + " attempting a " + action +
                          " command without a user logged in.");
     } else if (action.equals("logout")) {
       logout();
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
       readWall(pieces[1]);
    } else if (action.equals("listfriends")) {
       listFollowers(pieces[1]);
     } else {
       System.err.println("Invalid command: " + command);
     }
   }
 
 
 
   /******** Callbacks ********/
 
   /**
    * Boilerplate for user-following callback.
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
    * Callback for following a user.
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
     if (e != null ||
         (e instanceof DFSExceptions.FileSystemException &&
          ((DFSExceptions.FileSystemException) e).code != ErrorCode.Success)) {
       System.err.println("Node " + addr + " encountered an error while trying " +
                          "to add follower to " + file.getPath() + ":");
       e.printStackTrace();
     } else {
       System.err.println("\n\n\n\n\nSuccessfully added follower to " + file.getPath() + "\n\n\n\n\n");
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
    * Callback for reading a user's wall.
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
     if (e != null ||
         (e instanceof DFSExceptions.FileSystemException &&
          ((DFSExceptions.FileSystemException) e).code != ErrorCode.Success)) {
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
    * Boilerplate for follower-listing callback.
    */
   protected Callback createListFollowersCb() {
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
    * Callback for listing a user's followers.
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
   public void listFollowersCb(Exception e, TransactionId txId,
                               DFSFilename file, String data) {
     if (e != null ||
         (e instanceof DFSExceptions.FileSystemException &&
          ((DFSExceptions.FileSystemException) e).code != ErrorCode.Success)) {
       System.err.println("Node " + addr + " encountered an error while fetching " +
                          file.getPath() + ":");
       e.printStackTrace();
       return;
     }
 
     String username = getUsernameFromFilename(file.getPath());
     List<String> followers = getFollowers(data);
     System.err.println(username + "'s followers:");
     for (String f : followers) {
       System.err.println("    " + f);
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
    * Callback for posting a message on a user's wall.
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
     if (e != null ||
         (e instanceof DFSExceptions.FileSystemException &&
          ((DFSExceptions.FileSystemException) e).code != ErrorCode.Success)) {
       System.err.println("Node " + addr + " encountered an error while trying " +
                          "to post a message to " + file.getPath() + ":");
       e.printStackTrace();
       return;
     }
 
     for (String follower : data.split("\n")) {
       Iterator<WallPost> it = queuedWallPosts.iterator();
       while (it.hasNext()) {
         WallPost wp = it.next();
         String serialized = wp.pack();
         clientAPI.append(new DFSFilename("/dfs/0/" + follower + "-" + kWallFilename),
                          new String(serialized.length() + "\n" + serialized),
                          createNonDataCb());
       }
     }
   }
 
 }
