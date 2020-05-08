 package Communications;
 
 import java.io.*;
 import java.net.SocketException;
 import java.util.concurrent.*;
 
 import Messages.Message;
 import Utilities.Parser;
 
 
 public class UDPReceiver  {
 	protected PipedInputStream data=null;
 	protected UDPReceiverThread thread=null;
 	protected Thread t;
 	protected ConcurrentLinkedQueue<Byte> queue=new ConcurrentLinkedQueue<Byte>();
 	protected boolean needsMore=false;
 	int size=132;
     int moreNeeded=0;
     byte[] current=null;
     byte[] body=null;
     Parser parser=new Parser();
 	public UDPReceiver() throws SocketException{
 		data=new PipedInputStream();
 		thread=new UDPReceiverThread(queue);
 		t=(new Thread(thread));
 		t.start();
 	}
 	
 //	public byte[] getPacket(){
 //		if(queue.size()<size){
 //			return new byte[0];
 //		}
 //		else{
 //			byte[] packet = new byte[size];
 //			for(int i=0;i<size;i++){
 //				packet[i]=queue.poll();
 //			}
 //		}
 //	}
 	
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
 	
 	public void stop() throws InterruptedException{
 		thread.setRunning(false);
 		t.join();
 	}
 }
