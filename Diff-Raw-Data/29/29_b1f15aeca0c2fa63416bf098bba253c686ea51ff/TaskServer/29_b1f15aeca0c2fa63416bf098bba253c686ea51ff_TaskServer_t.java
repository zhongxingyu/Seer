 package server_network;
 
 import exceptions.FileManagerException;
 import exceptions.WrongInteractionDataException;
 import server_entities.TaskUser;
 import server_persistence.FileManager;
 import task_network.NetworkInteraction;
 import task_tree.Data;
 import task_tree.IDGenerator;
 import task_tree.TaskTree;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.SocketTimeoutException;
 import java.util.List;
 import java.util.concurrent.ConcurrentLinkedDeque;
 //Feature multiuser SET check
 //Feature check that user name != key file name
 //FIXME check all directories, if absent => create
 //FIXME alter network communication to contain single integer constant
 /**
  * Created with IntelliJ IDEA.
  * User: TheRusskiy
  * Date: 10.11.12
  * Time: 19:57
  * To change this template use File | Settings | File Templates.
  */
 public class TaskServer {
     public static final int PORT = 6969;
     public static final String firstTreeName="Rest";
     private static ConcurrentLinkedDeque<TaskUser> users;
     private boolean working=true;
 
 
     public static void main(String[] args)
     {
         TaskServer taskServer= new TaskServer();
         taskServer.startServer();
     }
 
     public void stopServer(){
         working=false;
         FileManager.saveUsersToFile(users);
     }
 
 
     public void startServer(){
         ServerSocket server=null;
         try {
             try{
                 users =FileManager.getUsersFromFile();
             }
             catch (FileManagerException e){
                 FileManager.placeToLog(e.getMessage());
                 users=new ConcurrentLinkedDeque<>();
             }
             server = new ServerSocket(PORT);
             server.setSoTimeout(2000);
             while(working){
                 try{
                 InteractionThread interactionThread = new InteractionThread(server.accept());
                 interactionThread.start();
                 }
                 catch (SocketTimeoutException e) {
                     System.out.println("Timeout...");
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         finally {
             try {
                 if (server!=null) server.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             FileManager.saveUsersToFile(users);
         }
     }
 
     private static void insertNewUser(TaskUser user){
         users.add(user);
     }
 
     public static NetworkInteraction processInput(NetworkInteraction interaction) {
         //task_network.NetworkInteraction result = new task_network.NetworkInteraction();
         try {
            if (interaction.getRequestCode()!= NetworkInteraction.RequestCode.CREATE_NEW_USER){
                if(!correctCredentials(interaction.getLogin(), interaction.getPassword())){
                    throw new WrongInteractionDataException(NetworkInteraction.ReplyCode.WRONG_CREDENTIALS);
                }
            }
             switch (interaction.getRequestCode()){
                 case CREATE_NEW_USER:{
                     if (userExists(interaction.getLogin())) {
                         interaction.setReplyCode(NetworkInteraction.ReplyCode.USER_ALREADY_EXISTS);
                         break;
                     }
                     TaskTree newTree = createNewTree();
                     TaskUser newUser = createNewUser(interaction.getLogin(), interaction.getPassword(), newTree);
                     insertNewUser(newUser);
                     FileManager.saveUsersToFile(users);
                     interaction.setTree(newTree);
                     interaction.setReplyCode(NetworkInteraction.ReplyCode.SUCCESS);
                     break;
                 }
                 case GET_AVAILABLE_TREES:{
                     TaskUser user =getUserByLogin(interaction.getLogin());
                     interaction.setTreeNames(user.getTreeNames());
                     interaction.setReplyCode(NetworkInteraction.ReplyCode.SUCCESS);
                     break;
                 }
                 case LOAD_FROM_SERVER:{
                     TaskTree tree = openTree(interaction.getLogin(), interaction.getTreeName());
                     interaction.setTree(tree);
                     interaction.setReplyCode(NetworkInteraction.ReplyCode.SUCCESS);
                     break;
                 }
                 case SAVE_TO_SERVER:{
                     saveTree(interaction.getLogin(), interaction.getTree(), interaction.getTreeName());
                     interaction.setReplyCode(NetworkInteraction.ReplyCode.SUCCESS);
                     break;
                 }
                 case DELETE_USER:{
                     TaskUser user =getUserByLogin(interaction.getLogin());
                     users.removeFirstOccurrence(user);
                     List<String> treesToDelete=user.getTreeNames();
                     for(String treeToDelete:treesToDelete){
                         FileManager.deleteFile(user.getLogin(),treeToDelete);
                     }
                     FileManager.deleteFile(user.getLogin());
                     assert (!users.contains(user));
                     interaction.setReplyCode(NetworkInteraction.ReplyCode.SUCCESS);
                     break;
                 }
                 default:{
                     interaction.setReplyCode(NetworkInteraction.ReplyCode.UNKNOWN_REQUEST_CODE);
                     break;
                 }
             }
         } catch (WrongInteractionDataException e) {
             interaction.setReplyCode(NetworkInteraction.ReplyCode.ERROR);
             interaction.setText(e.getMessage());
             FileManager.placeToLog(e.getMessage());
         }
         catch (IOException e) {
             interaction.setReplyCode(NetworkInteraction.ReplyCode.ERROR);
             interaction.setText("Server File System Error...");
             FileManager.placeToLog(e.getMessage());
         }
         return interaction;
     }
 
     private static void saveTree(String login, TaskTree tree, String treeName) throws IOException {
         FileManager.saveToFile(login, treeName, tree);
     }
 
     private static TaskTree openTree(String login, String treeName) throws IOException {
         return FileManager.loadFromFile(login, treeName);
     }
 
     private static boolean correctCredentials(String login, String password) {
         Boolean validLogin=false;
         TaskUser user=null;
         for (TaskUser user_iter : users){
             if (user_iter.getLogin().equals(login)){
                 validLogin=true;
                 user=user_iter;
                 break;
             }
         }
         if (validLogin&&user.getPassword().equals(password)) return true;
         return false;
     }
 
     private static boolean userExists(String login) {
         for (TaskUser o : users){
             if (o.getLogin().equals(login)) return true;
         }
         return false;
     }
 
     private static TaskTree createNewTree() {
         IDGenerator idGenerator = new IDGenerator();
         Data data = new Data("New tree");
         data.setActivityName(firstTreeName);
         TaskTree tree = new TaskTree(idGenerator, data);
         return tree;
     }
 
     private static TaskUser createNewUser(String login, String password, TaskTree tree) throws WrongInteractionDataException, IOException {
 //        if (server_persistence.FileManager.fileExists(login)){
 //            server_persistence.FileManager.placeToLog("User with this name already exists: "+login);
 //            throw new WrongInteractionDataException("User with this name already exists: "+login);
 //        }
         FileManager.saveToFile(login, firstTreeName, tree);
         TaskUser user=new TaskUser();
         user.setLogin(login);
         user.setPassword(password);
         user.addTreeName(tree.getData().getActivityName());
         return user;
     }
 
     private static TaskUser getUserByLogin(String login) throws WrongInteractionDataException {
         for (TaskUser o : users){
             if (o.getLogin().equals(login)) return o;
         }
        throw new WrongInteractionDataException(NetworkInteraction.ReplyCode.USER_DOES_NOT_EXIST);
     }
 }
