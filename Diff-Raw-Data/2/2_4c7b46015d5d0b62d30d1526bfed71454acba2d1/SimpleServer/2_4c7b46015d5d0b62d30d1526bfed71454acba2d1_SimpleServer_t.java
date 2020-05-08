 import java.io.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Никита
  * Date: 15.12.12
  * Time: 23:29
  * To change this template use File | Settings | File Templates.
  */
 public class SimpleServer {
     public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
         Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connect = DriverManager.getConnection("jdbc:mysql://localhost/geofriend?user=root&password=password");
         Statement stmt = connect.createStatement();
         ServerSocket server = new ServerSocket(7777);
         ExecutorService pool = Executors.newCachedThreadPool();
         while(true) {
             Socket sock = server.accept();
             pool.submit(new SocketHandler(sock, stmt));
         }
     }
 }
