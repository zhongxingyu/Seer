 package Communications;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 
 public class TCPReceiverThread implements Runnable {
 	ConcurrentLinkedQueue<Byte> queue=null;
 	Socket socket=null;
 	InputStream is=null;
 	boolean running=true;
 	public TCPReceiverThread(ConcurrentLinkedQueue<Byte> _queue){
 		queue=_queue;
 	}
 	public int setSocket(Socket _socket){
 		socket=_socket;
 		try {
 			socket.setSoTimeout(500);
 			is=socket.getInputStream();
 		} catch (IOException e) {
 			return -1;
 		}
 		return 0;
 	}
 	public String getIP(){
 		return socket.getInetAddress().getHostAddress();
 	}
 	public void run() {
 		byte[] packet=null;
 		while(running){
 			packet=new byte[5000];
 			try{
 				int size=is.read(packet);
 				for(int i=0;i<size;i++){
 					queue.add(packet[i]);
 				}
 				System.out.println("Got it");
 			}
 			catch(SocketTimeoutException e){
 			} catch (IOException e) {
 				System.out.println("\rTCP socket receive error");
 				running=false;
 			}
 		}
 		try {
 			is.close();
 			socket.close();
 		} catch (IOException e) {
 		}
 		try {
 			socket.close();
 		} catch (IOException e) {}
 	}
 	
 	public void stop(){
 		running=false;
 	}
 
 }
