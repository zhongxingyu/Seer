 package de.uulm.presenter.bluetooth;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.bluetooth.ServiceRecord;
 import javax.microedition.io.StreamConnection;
 
 import com.intel.bluetooth.NotImplementedError;
 
 import de.uulm.presenter.control.Main;
 import de.uulm.presenter.io.IODevice;
 import de.uulm.presenter.io.IODeviceImpl;
 import de.uulm.presenter.io.IORemote;
 import de.uulm.presenter.io.IORemoteImpl;
 
 public class BTHandler implements Runnable{
 	protected StreamConnection con;
 	protected boolean listening=true;
 	private InputStream is;
 	private OutputStream os;
 	private static int instances=0;
 	
 	public BTHandler(){
 		
 		
 	}
 	public void init(StreamConnection con) throws IOException {
 		this.con=con;
 		this.is=con.openInputStream();
 		this.os=con.openOutputStream();
 		
  		
 		
 	}
 	
 	@Override
 	public void run() {
 		init();
 		while (listening){
 			byte[] recv= new byte[1024];
         	try {
         		int cnt = is.read(recv); 
 				if (cnt==-1){
					listening=false;
 					break;
 				}
 			this.recv(recv,cnt);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 		
 		shutdown();
 		
 	}
 	
 	public synchronized void init(){
 		instances++;
 		
 		
 	}
 	
 	public synchronized void shutdown(){
 		instances--;
 		
 		if (instances<1){
 			Main.control.stateServerListening();
 		}
 	}
 	
 	public void recv(byte[] b,int len){
 		throw new NotImplementedError();
 	}
 
 	public void send(byte[] b){
         try {
 			os.write(b);
 			os.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
         
 	}
 	
 	public synchronized void stop(){
 		listening=false;
 		try {
 			
 			is.close();
 			con.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 }
