 package net.yeputons.cscenter.dbfall2013.scaling;
 
 import net.yeputons.cscenter.dbfall2013.engines.hashtrie.HashTrieEngine;
 
 import java.io.*;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Egor Suvorov
  * Date: 12.10.13
  * Time: 19:10
  * To change this template use File | Settings | File Templates.
  */
 public class ShardingNode {
     private static byte[] readBytes(DataInputStream in) throws IOException {
         int len = in.readInt();
         if (len == -1) return null;
         byte[] res = new byte[len];
         in.readFully(res);
         return res;
     }
     private static void writeBytes(DataOutputStream out, byte[] data) throws IOException {
         if (data == null) {
             out.writeInt(-1);
         } else {
             out.writeInt(data.length);
             out.write(data);
         }
     }
 
     HashTrieEngine engine;
     protected void processClient(Socket clientSocket) throws Exception {
         DataInputStream in = new DataInputStream(clientSocket.getInputStream());
         DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
 
         while (true) {
             byte[] cmd = new byte[3];
             in.readFully(cmd);
             if (Arrays.equals(cmd, "clr".getBytes())) {
                 synchronized (engine) {
                     engine.clear();
                 }
                 out.write("ok".getBytes());
             } else if (Arrays.equals(cmd, "siz".getBytes())) {
                 out.write("ok".getBytes());
                 int siz;
                 synchronized (engine) {
                     siz = engine.size();
                 }
                 out.writeInt(siz);
             } else if (Arrays.equals(cmd, "del".getBytes())) {
                 byte[] key = readBytes(in);
                 synchronized (engine) {
                     engine.remove(ByteBuffer.wrap(key));
                 }
                 out.write("ok".getBytes());
             } else if (Arrays.equals(cmd, "put".getBytes())) {
                 byte[] value = readBytes(in);
                 byte[] key = readBytes(in);
                 synchronized (engine) {
                     engine.put(ByteBuffer.wrap(key), ByteBuffer.wrap(value));
                 }
                 out.write("ok".getBytes());
             } else if (Arrays.equals(cmd, "get".getBytes())) {
                 byte[] key = readBytes(in);
                 ByteBuffer res;
                 synchronized (engine) {
                     res = engine.get(ByteBuffer.wrap(key));
                 }
                 out.write("ok".getBytes());
                 writeBytes(out, res == null ? null : res.array());
             } else {
                 out.write("no".getBytes());
                 writeBytes(out, "Invalid command".getBytes());
             }
         }
     }
 
     Set<Socket> clients;
     protected ServerSocket serverSocket;
     protected volatile boolean isRunning;
 
     public void run(File storage, InetAddress bindTo, int port) throws Exception {
         isRunning = true;
         engine = new HashTrieEngine(storage);
         serverSocket = new ServerSocket(port, 0, bindTo);
 
         clients = new HashSet<Socket>();
 
         while (true) {
             try {
                 final Socket clientSocket = serverSocket.accept();
                if (!isRunning) break;
                 new Thread(new Runnable() {
                     @Override
                     public void run() {
                         synchronized (clients) {
                             clients.add(clientSocket);
                         }
 
                         try {
                             processClient(clientSocket);
                         } catch (EOFException e) {
                         } catch (SocketException e) {
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                         try {
                             clientSocket.close();
                         } catch (SocketException e) {
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
 
                         synchronized (clients) {
                             clients.remove(clientSocket);
                         }
                     }
                }).run();
             } catch (SocketException e) {
             }
         }
         for (Socket s : clients)
             s.close();
         serverSocket.close();
         engine.close();
     }
     public void stop() {
         isRunning = false;
         try {
             serverSocket.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static void main(String[] args) throws Exception {
         if (args.length != 3) {
             System.err.println("Arguments: <storage file> <ip to listen on> <port>\n");
             System.exit(1);
         }
         String host = args[1];
         int port = Integer.parseInt(args[2]);
         new ShardingNode().run(new File(args[0]), InetAddress.getByName(host), port);
     }
 }
