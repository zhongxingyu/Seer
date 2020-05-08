 import java.io.IOException;
 import java.io.InputStream;
import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.security.GuardedObject;
 
 import javax.bluetooth.BluetoothStateException;
 import javax.bluetooth.DiscoveryAgent;
 
 import javax.bluetooth.LocalDevice;
 import javax.microedition.io.Connector;
 import javax.microedition.io.StreamConnection;
 import javax.microedition.io.StreamConnectionNotifier;
 
 
 public class BTServer  implements Runnable{
 	private static final String SERVICEUUID = "6ABC1C60693D11E1B86C0800200C9A66";
 	private boolean listening=true;
 	private boolean active=true;
 	private StreamConnection con;
	
 	private InputStream is;
 	public BTServer(){
 		
 	}
 	
 	public void init() throws IOException {
 		Thread t = new Thread(this);
 		
 		LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
 		
 		StreamConnectionNotifier service =  (StreamConnectionNotifier) Connector.open( "btspp://localhost:" + SERVICEUUID + ";name=RemoteHCIServer" );
 		
 		while (active){
             con = (StreamConnection) service.acceptAndOpen();
            if (t.isAlive()){
            	this.notifyAll();
            }else{
            	t.start();            	
            }
            
             is = con.openInputStream();
             System.out.println("Connection Received");
             this.send("hello from Server".getBytes());
             try {
 				t.join();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		
 		}
 		
 		
 		
 	}
 
 	@Override
 	public void run() {
 		System.out.println("now listening in new thread");
		
 		try {
             while (listening){
             	byte[] recv= new byte[1024];
            	if (is.read(recv)==-1){
            		break;
            	}
             	
             	String s = new String(recv, 0, recv.length);
             	if (s.equals("")){
             		System.out.println("disconnected");
             		listening=false;
             		break;
             	}
             	System.out.println("rec: "+s);
             }
 			
 
 		} catch (BluetoothStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}	
 
	
 	public void send(byte[] b) throws IOException{
         OutputStream os = con.openOutputStream();
         os.write(b);
         os.flush();
         
 	}
 }
 
 
 
 
 
 
 
