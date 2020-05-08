 package task_network;
 
 import exceptions.NetworkInteractionException;
 import task_tree.TaskTree;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: TheRusskiy
  * Date: 18.11.12
  * Time: 0:24
  * To change this template use File | Settings | File Templates.
  */
 public class TaskClientNetDriver {
     public static int SERVER_PORT = 6969;
     public static InetAddress SERVER_ADDRESS;
     //TODO ^^^values^^^ from file!!
 
 
 
     public static void saveTree(TaskTree tree, String login, String password, String treeName) throws IOException, NetworkInteractionException {
         NetworkInteraction outgoing = new NetworkInteraction();
         outgoing.setLogin(login);
         outgoing.setPassword(password);
         outgoing.setTree(tree);
         outgoing.setRequestCode(NetworkInteraction.RequestCode.SAVE_TO_SERVER);
         outgoing.setTreeName(treeName);
         NetworkInteraction incoming=sendReceive(outgoing);
         if (incoming.getReplyCode()!= NetworkInteraction.ReplyCode.SUCCESS) {
             throw new NetworkInteractionException(incoming.getReplyCode());
         }
     }
 
     public static TaskTree loadTree(String login, String password, String treeName) throws IOException, NetworkInteractionException {
        //FIXME specific tree
         NetworkInteraction outgoing = new NetworkInteraction();
         outgoing.setLogin(login);
         outgoing.setPassword(password);
         outgoing.setRequestCode(NetworkInteraction.RequestCode.LOAD_FROM_SERVER);
         //interaction.setTreeName(treeName);
         NetworkInteraction incoming=sendReceive(outgoing);
         if (incoming.getReplyCode()!= NetworkInteraction.ReplyCode.SUCCESS) {
             throw new NetworkInteractionException(incoming.getReplyCode());
         }
         TaskTree tree=incoming.getTree();
         return tree;
     }
 
     public static TaskTree createUser(String login, String password) throws IOException, NetworkInteractionException {
         NetworkInteraction outgoing = new NetworkInteraction();
         outgoing.setLogin(login);
         outgoing.setPassword(password);
         outgoing.setRequestCode(NetworkInteraction.RequestCode.CREATE_NEW_USER);
         NetworkInteraction incoming=sendReceive(outgoing);
         if (incoming.getReplyCode()!= NetworkInteraction.ReplyCode.SUCCESS) {
             throw new NetworkInteractionException(incoming.getReplyCode());
         }
         TaskTree tree=incoming.getTree();
         return tree;
     }
 
     public static void deleteUser(String login, String password) throws IOException, NetworkInteractionException {
         NetworkInteraction outgoing = new NetworkInteraction();
         outgoing.setLogin(login);
         outgoing.setPassword(password);
         outgoing.setRequestCode(NetworkInteraction.RequestCode.DELETE_USER);
         NetworkInteraction incoming=sendReceive(outgoing);
         if (incoming.getReplyCode()!= NetworkInteraction.ReplyCode.SUCCESS) {
             throw new NetworkInteractionException(incoming.getReplyCode());
         }
     }
 
     public static List<String> getAvailableTrees(String login, String password) throws IOException, NetworkInteractionException {
         NetworkInteraction outgoing = new NetworkInteraction();
         outgoing.setLogin(login);
         outgoing.setPassword(password);
         outgoing.setRequestCode(NetworkInteraction.RequestCode.GET_AVAILABLE_TREES);
         NetworkInteraction incoming=sendReceive(outgoing);
         if (incoming.getReplyCode()!= NetworkInteraction.ReplyCode.SUCCESS) {
             throw new NetworkInteractionException(incoming.getReplyCode());
         }
         return incoming.getTreeNames();
     }
 
 
     private static NetworkInteraction sendReceive(NetworkInteraction interaction) throws IOException {
         TaskTree tree=null;
         Socket socket=null;
         ObjectInputStream in = null;
         ObjectOutputStream out = null;
         NetworkInteraction readInteraction=null;
         try {
             SERVER_ADDRESS=InetAddress.getLocalHost();
             socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             out = new ObjectOutputStream(socket.getOutputStream());
             out.writeObject(interaction);
             out.flush();
             in = new ObjectInputStream(socket.getInputStream());
             readInteraction = (NetworkInteraction) in.readObject();
         }  catch (ClassNotFoundException e) {
             e.printStackTrace();
         } finally {
             try {
                 if (socket!=null) socket.close();
                 if (in!=null) in.close();
                 if (out!=null) out.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return readInteraction;
     }
 
     //for test
     public static TaskTree loadTree() throws IOException, NetworkInteractionException {
         return loadTree("foo", "bar", "");
     }
     public static void saveTree(TaskTree tree) throws IOException, NetworkInteractionException {
         saveTree(tree, "foo", "bar", "");
     }
 }
