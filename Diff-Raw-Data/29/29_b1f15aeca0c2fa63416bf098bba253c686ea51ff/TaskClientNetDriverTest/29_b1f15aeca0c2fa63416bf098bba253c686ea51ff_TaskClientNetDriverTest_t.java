 import task_network.NetworkInteraction;
 import task_network.TaskClientNetDriver;
 import task_tree.Data;
 import task_tree.IDGenerator;
 import task_tree.TaskTree;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: TheRusskiy
  * Date: 10.11.12
  * Time: 19:53
  * To change this template use File | Settings | File Templates.
  */
 public class TaskClientNetDriverTest {
 
     public static final int TEST_PORT = 7970;
     public static final int TEST_PORT2 = 7971;
 
     public static boolean testAll()
     {
         boolean result=true;
         if (!testingSocketTest(TEST_PORT))
         {
             System.out.println("testing socket test fails");
             result=false;
         }
         if (!testingSocketTest(TEST_PORT2))
         {
             System.out.println("testing socket test fails");
             result=false;
         }
         if (!saveTreeNetworkTest())
         {
             System.out.println("save tree via network test fails");
             result=false;
         }
         if (!loadTreeNetworkTest())
         {
             System.out.println("load tree via network test fails");
             result=false;
         }
         return result;
     }
 
     public static boolean testingSocketTest(int port){
         Socket socket=null;
         ServerSocket server=null;
         try {
             server = new ServerSocket(port);
             InetAddress address=InetAddress.getLocalHost();
             socket = new Socket(address, port);
 
         } catch (IOException e) {
             return false;
         } finally {
             try {
                 if (socket!=null) socket.close();
                 if (server!=null) server.close();
             } catch (IOException e) {
                 e.printStackTrace();
                 System.err.println("Cant' close testing socket socket!");
             }
         }
         return true;
     }
 
     public static boolean loadTreeNetworkTest(){
         //!!! TEST WILL FAIL IF EQUALS COMPARES TREE IDs!!!
         int portTemp=TaskClientNetDriver.SERVER_PORT;
         InetAddress tempAddress=TaskClientNetDriver.SERVER_ADDRESS;
         TaskClientNetDriver.SERVER_PORT=TEST_PORT;
         try {TaskClientNetDriver.SERVER_ADDRESS=InetAddress.getLocalHost();} catch (UnknownHostException e) {}
 
         try {
             final NetworkInteraction interaction = new NetworkInteraction();
             IDGenerator idGenerator= new IDGenerator();
             Data data = new Data("test string");
             final TaskTree tree = new TaskTree(idGenerator, data);
             interaction.setTree(tree);
 
             //Faking transfer:
             Thread fakeThread = new Thread(){
                 public void run()
                 {
                     ServerSocket server=null;
                     Socket fakeServer=null;
                     ObjectInputStream fakeServerInput=null;
                     ObjectOutputStream fakeServerOutput=null;
                     try {
                         server = new ServerSocket(TEST_PORT);
                         fakeServer=server.accept();
                         fakeServerInput=new ObjectInputStream(fakeServer.getInputStream());
                         fakeServerOutput=new ObjectOutputStream(fakeServer.getOutputStream());
                         NetworkInteraction fakeInteraction=(NetworkInteraction) fakeServerInput.readObject();
 
                         //faking transmission from server:
                         fakeInteraction.setTree(tree);
                        fakeInteraction.setReplyCode(NetworkInteraction.ReplyCode.SUCCESS);
                         fakeServerOutput.writeObject(fakeInteraction);
                         fakeServerOutput.flush();
                     } catch (Exception e) {
                         e.printStackTrace();
                     } finally {
                         try {
                             if (server!=null) server.close();
                             if (fakeServer!=null) fakeServer.close();
                             if (fakeServerInput!=null) fakeServerInput.close();
                             if (fakeServerOutput!=null) fakeServerOutput.close();
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     }
                 }
             };
             fakeThread.start();
             TaskTree loadedTree=TaskClientNetDriver.loadTree();
             if (!loadedTree.equals(tree)) {
                 throw new Exception("Tree that arrived from fake server differs");
             }
             fakeThread.join();
             return true;
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
         finally {
             TaskClientNetDriver.SERVER_PORT=portTemp;
             TaskClientNetDriver.SERVER_ADDRESS=tempAddress;
         }
     }
 
 
 
     public static boolean saveTreeNetworkTest(){
         //!!! TEST WILL FAIL IF EQUALS COMPARES TREE IDs!!!
         int portTemp=TaskClientNetDriver.SERVER_PORT;
         InetAddress tempAddress=TaskClientNetDriver.SERVER_ADDRESS;
         TaskClientNetDriver.SERVER_PORT=TEST_PORT;
         try {TaskClientNetDriver.SERVER_ADDRESS=InetAddress.getLocalHost();} catch (UnknownHostException e) {}
 
         try {
             final NetworkInteraction interaction = new NetworkInteraction();
             IDGenerator idGenerator= new IDGenerator();
             Data data = new Data("test string");
             final TaskTree tree = new TaskTree(idGenerator, data);
             interaction.setTree(tree);
 
             //Faking transfer:
             Thread fakeThread = new Thread(){
                 public void run()
                 {
                     ServerSocket server=null;
                     Socket fakeServer=null;
                     ObjectInputStream fakeServerInput=null;
                     ObjectOutputStream fakeServerOutput=null;
                     try {
                         server = new ServerSocket(TEST_PORT);
                         fakeServer=server.accept();
                         fakeServerInput=new ObjectInputStream(fakeServer.getInputStream());
                         fakeServerOutput=new ObjectOutputStream(fakeServer.getOutputStream());
                         NetworkInteraction fakeInteraction=(NetworkInteraction) fakeServerInput.readObject();
                         if (!fakeInteraction.getTree().equals(interaction.getTree())){
                             throw new TestException("Tree that arrived at fake server differs");
                         }
                         //faking transmission from server:
                         fakeInteraction.setTree(tree);
                        fakeInteraction.setReplyCode(NetworkInteraction.ReplyCode.SUCCESS);
                         fakeServerOutput.writeObject(fakeInteraction);
                     } catch (IOException|ClassNotFoundException e) {
                         e.printStackTrace();
                     } finally {
                         try {
                             if (server!=null) server.close();
                             if (fakeServer!=null) fakeServer.close();
                             if (fakeServerInput!=null) fakeServerInput.close();
                             if (fakeServerOutput!=null) fakeServerOutput.close();
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     }
                 }
             };
             fakeThread.start();
             TaskClientNetDriver.saveTree(tree);
             fakeThread.join();
             return true;
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
         finally {
             TaskClientNetDriver.SERVER_PORT=portTemp;
             TaskClientNetDriver.SERVER_ADDRESS=tempAddress;
         }
     }
 
     public static void startTests(){
         System.out.println("NetDriver test: " + TaskClientNetDriverTest.testAll());
     }
 
     public static void main(String[] args)
     {
         startTests();
     }
 }
