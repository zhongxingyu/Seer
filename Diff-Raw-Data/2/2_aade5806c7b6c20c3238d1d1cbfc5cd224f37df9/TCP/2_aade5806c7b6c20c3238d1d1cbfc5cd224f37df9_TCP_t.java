 package Communications;
 
 import java.io.*;
 import java.net.*;
 import java.util.Enumeration;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import Messages.Message;
 import Utilities.Parser;
 
 
 public class TCP implements Runnable {
 	Socket socket=null;
     PushbackInputStream clientInputStream;
     TCPReceiverThread tr=null;
     Thread t=null;
     ConcurrentLinkedQueue<Byte> queue=null;
     protected boolean active;
     int size=132;
     int moreNeeded=0;
     boolean needsMore=false;
     byte[] current=null;
     byte[] body=null;
     Parser parser=new Parser();
 	public String pendingIP="";
 	public ServerSocket serverSocket=null;
 	public InetAddress ip=null;
     
     public TCP(){
 		try {
 			InetAddress address = null;
 			Enumeration<NetworkInterface> interfaces = NetworkInterface
 					.getNetworkInterfaces();
 			while (interfaces.hasMoreElements()) {
 				NetworkInterface networkInterface = interfaces.nextElement();
 				if (networkInterface.isLoopback())
 					continue; // Don't want to broadcast to the loopback
 								// interface
 				Enumeration<InetAddress> addresses = networkInterface
 						.getInetAddresses();
 				while (addresses.hasMoreElements()) {
 					address = addresses.nextElement();
 					if (!address.getHostAddress().contains("192.168.224"))
 						continue;
 					break;
 				}
				if (address != null && address.toString().contains("192.168.224")) {
 					break;
 				}
 			}
 			ip=address;
 			queue = new ConcurrentLinkedQueue<Byte>();
 			tr = new TCPReceiverThread(queue);
 		}
     	catch(Exception e){
     		System.out.println("Could not set up TCP");
     		System.exit(-1);
     	}
     }
     public boolean getActive(){
     	return active && !socket.isClosed();
     }
 	public void run(){
 		try{
 		 serverSocket = new ServerSocket(12345,0,ip);
          Socket socket2 = serverSocket.accept();
          if(!getActive()){
         	 socket=socket2;
              clientInputStream = new PushbackInputStream(socket.getInputStream());
              active=true;
              tr.setSocket(socket);
              t=new Thread(tr);
              t.start();
          }
          else{
         	 socket2.close();
          }
 		}
 		catch(Exception e){
 			active=false;
 		}
 	}
 	
 	public int connect(String target){
 		try {
 			if(getActive()){
 				return -1;
 			}
 			socket = new Socket(target, 12345);
 			tr.setSocket(socket);
             t=new Thread(tr);
             t.start();
 		} catch (UnknownHostException e) {
 			return -1;
 		} catch (IOException e) {
 			return -2;
 		}
 		return 0;
 	}
 	 public int send(byte[] message) {
          try {
                  OutputStream socketOutputStream = socket.getOutputStream();
                  socketOutputStream.write(message, 0, message.length);
          } catch (Exception e) {
         	 return -1;
          }
          return 0;
 	 }
 	 public int send(Message message) {
          try {
                  OutputStream socketOutputStream = socket.getOutputStream();
                  byte[] buffer=message.convert();
                  socketOutputStream.write(buffer, 0, buffer.length);
          } catch (Exception e) {
         	 return -1;
          }
          return 0;
 	 }
 	
 	public Message read(){
 		if (!needsMore) {
 			if (queue.size() >= size) {
 				current = new byte[size];
 				for (int i = 0; i < size; i++) {
 					current[i] = queue.poll();
 				}
 				moreNeeded=parser.parse(current);
 				if(queue.size()>=moreNeeded){
 					for (int i = 0; i < moreNeeded; i++) {
 						body[i] = queue.poll();
 					}
 					needsMore=false;
 					return parser.addBody(body);
 				}
 				needsMore=true;
 				return null;
 			} else {
 				needsMore=true;
 				return null;
 			}
 		}
 		else if(queue.size()>=moreNeeded){
 			for (int i = 0; i < moreNeeded; i++) {
 				body[i] = queue.poll();
 			}
 			needsMore=false;
 			return parser.addBody(body);
 		}
 		needsMore=true;
 		return null;
 	}
 	
 	public String getIP(){
 		return serverSocket.getInetAddress().getHostAddress();
 	}
 	
 	public void close() throws IOException{
 		active=false;
 		queue.clear();
 		tr.stop();
 		clientInputStream.close();
 		socket.close();
 	}
 }
