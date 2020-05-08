 package DistGrep;
 
 import java.io.*;
 import java.net.Socket;
 
 
 public class Connection {
 
     private int port;
     private String[] peers;
 
     public Connection(Config config) {
         this.port = Integer.parseInt(config.valueFor("port"));
         String peersList = config.valueFor("peers");
         this.peers = peersList.split(",");
     }
 
     public String sendRawBroadcast(String msg) throws IOException {
         Socket [] resultConnections = new Socket[peers.length];
 
         for(int i = 0; i < peers.length; i++)
         {
             String ip = peers[i];
             try {
                 Socket socket = new Socket(ip, port);
                 PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                 printWriter.print(msg);
                 printWriter.flush();
             }
             catch (IOException e) {
                 System.err.print("Could not send data to " + ip);
                 continue;
             }
         }
         return gatherResults(resultConnections);
     }
 
 	public String sendBroadcast(String msg, String type) throws IOException {
 
         Socket [] resultConnections = new Socket[peers.length];
 
         for(int i=0; i<peers.length;i++) {
 			String ip = peers[i];
 			try {
                 resultConnections[i] = sendRequestTo(msg,ip,type);
             } catch(IOException e) {
                 System.err.println("Could not send data to " + ip);
                 continue;
             }
 		}
 
         return gatherResults(resultConnections);
 	}
 	
 	
 	public String sendMessage(String msg, String type, String[] receiver) throws IOException {
 
         Socket [] resultConnections = new Socket[receiver.length];
 
 		for(int i=0; i<receiver.length;i++) {
 			String ip = receiver[i];
 			try {
                 resultConnections[i] =  sendRequestTo(msg,ip,type);
 			} catch(IOException e) {
 				System.err.println("Could not send data to " + ip);
                 resultConnections[i] = null;
 				continue;
 			}
 		}
 
         return gatherResults(resultConnections);
 	}
 
     private Socket sendRequestTo(String msg, String ip, String type) throws IOException {
 
         Socket socket = new Socket(ip, port);
         PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
         printWriter.print("<header>"+type+"</header><body>"+msg+"</body>");
         printWriter.flush();
         return socket;
     }
 
     private String gatherResults(Socket[] connections) throws IOException {
 
         ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
 
         for(int i = 0; i < connections.length; i++) {
             if(connections[i] == null)
                 continue;
 
             InputStream inputStream = connections[i].getInputStream();
             String header = "\nResults from " + connections[i].getInetAddress().toString() + ":\n";
             byteArrayOutputStream.write(header.getBytes());
 
             byte[] buffer = new byte[1024];
             while(true) {
                 int n =  inputStream.read(buffer);
                 if(n < 0)
                     break;
                 byteArrayOutputStream.write(buffer);
             }
             connections[i].close();
         }
         return byteArrayOutputStream.toString();
     }
 	
 }
