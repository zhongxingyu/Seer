 package serialclient;
 
 import gnu.io.CommPort;
 import gnu.io.CommPortIdentifier;
 import gnu.io.SerialPort;
 
 import java.io.FileDescriptor;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringWriter;
 
 import java.util.*;
 import java.lang.ref.WeakReference;
 
 public class SerialControl{
 	private static int BAUD = 115200;
 	private static int DATABITS = SerialPort.DATABITS_8;
 	private static int STOPBITS = SerialPort.STOPBITS_1;
 	private static int PARITY = SerialPort.PARITY_EVEN;
 	private static int FLOW_CTRL = SerialPort.FLOWCONTROL_NONE;
 
 
 	private LinkedList<WeakReference<SerialListener>> listeners;
 	private byte[] bytes;
 	private SerialPort port;
 
 	public SerialControl(){
 		listeners = new LinkedList<WeakReference<SerialListener>>();
 		bytes = null;
 		port = null;
 	}
 
 	public void addListener(SerialListener l){
 		listeners.add(new WeakReference<SerialListener>(l));
 	}
 
 	public void removeListener(SerialListener l){
		listeners.remove(l);
 	}
 
 	public void connect(String portName)
 	throws Exception {
 		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
 		if ( portIdentifier.isCurrentlyOwned() ){
 			System.out.println("Error: Port is currently in use");
 		}
 		else{
 			CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
 
 			if ( commPort instanceof SerialPort ){
 				port = (SerialPort) commPort;
 				port.setSerialPortParams(BAUD, DATABITS, STOPBITS, PARITY);
 				port.setFlowControlMode(FLOW_CTRL);
 
 				new Thread(new SerialReader(port.getInputStream())).start();
 
 			}
 			else{
 				System.out.println("Error: Only serial ports are handled by this example.");
 			}
 		}
 	}
 
 	public synchronized void send(byte[] bytes)
 	throws SerialException {
 		if(this.bytes != null)
 			throw new SerialException();
 
 		this.bytes = bytes;
 
 		new Thread(new Runnable(){
 			public void run(){
 				try{
 					port.getOutputStream().write(new PacketInfo(SerialControl.this.bytes).packageBytes());
 					SerialControl.this.bytes = null;
 				}
 				catch(IOException e){
 					e.printStackTrace();
 				}
 			}
 		}).start();
 	}
 
	/** */
 	public class SerialReader implements Runnable {
 		InputStream in;
 
 		public SerialReader ( InputStream in ){
 			this.in = in;
 		}
 
 		public void run (){
			byte[] buffer = new byte[1024];
 			int len = -1;
 			try{
 				while ( ( len = this.in.read(buffer)) > -1 ){
					if(len > 0)
						notifyListeners(buffer, len);
 				}
 			}
 			catch ( IOException e ){
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void notifyListeners(byte[] bytes, int length){
 		Iterator<WeakReference<SerialListener>> i = listeners.iterator();
 		while(i.hasNext()){
 			SerialListener l = i.next().get();
 			if(l != null){
 				l.bytesReceived(bytes, length);
 			}
 			else{
 				i.remove();
 			}
 		}
 	}
 }
