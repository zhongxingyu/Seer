 package de.cased.serial;
 
import gnu.io.CommPortIdentifier;

 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import de.cased.utilities.SerialNodeDispatcher;
 
 public class SerialCommunicator  {
 	
 	private Logger logger = Logger.getLogger("KeyGenerator");
 	private SerialNodeDispatcher dispatcher;
 	Process serialLink;
 	BufferedReader reader;
 	BufferedWriter writer;
 	
 	
 	
 //	private static SerialCommunicator instance;
 	
 	
 //	static ArrayList<CommPortIdentifier> resultList;
 	
 	
 //	static CommPortIdentifier persistentIdentifier;
 	
 //	static void setPersistentIdentifier(String portName){
 //		for (CommPortIdentifier element : resultList) {
 //			if(element.getName().equals(portName)){
 //				persistentIdentifier = element;
 ////				System.out.println("set persistent interface name to " + portName);
 //				break;
 //			}
 //		}
 //	}
 	
 	public SerialCommunicator(String portName, ReceiveStringFromSerial receiver) throws Exception{
 		connect(portName);
 		buildListener(receiver);
 	}
 	
 //	public static SerialCommunicator getInstance(String portName) throws Exception{
 //		if(instance == null)
 //			instance = new SerialCommunicator(portName);
 //		return instance;
 //	}
 	
 	private void buildListener(ReceiveStringFromSerial receiver) {
 		
 		dispatcher = new SerialNodeDispatcher(reader, receiver);
 	    dispatcher.start();	
 	}
 
 	/*
 	 * sends a 'message' to a serial connected TelosB on 'portName' and returns a 
 	 * String which holds the 'expectedReturnLineCount' Lines that are read from the 
 	 * device after sending the 'message'.
 	 */
 	public void send(String message[], String[] keywords) throws Exception{
 		try{
 			
 			
 //		    final Thread t = new Thread(new SerialNodeDispatcher(port, receiver, keywords));
 //		    t.start();
 //	    
 //		    new Timer().schedule(new TimerTask(){
 //		    	public void run(){
 //		    		t.interrupt();
 //		    	}
 //		    }, 6000);
 			
 			dispatcher.setKeywords(keywords);
 			
 		    for(int i = 0; i < message.length; i++){
 		    	
 		    	System.out.println("sending:" + message[i]);
 //		    	
 //		    	byte[] bytes = message[i].getBytes();
 //		    	for (byte b : bytes) {
 //			    	writer.write(b);
 //			    }
 //		    	writer.write(0x0a); //0A - end of line
 //		    	writer.flush();
 		    	
 		    	writer.write(message[i]);
 		    	writer.write(0x0a); //0A - end of line
 		    	writer.flush();
 		    	
 		    }
 		}
 		catch(Exception e){
 			logger.log(Level.SEVERE, "Serial Port is busy, abort connection");
 			e.printStackTrace();
 		}
 	    
 		
 	}
 	
 	private boolean connect (String portName) {
 		URL serialDumpPrg = ClassLoader.getSystemResource("serialdump-linux");
 		if(serialDumpPrg != null){
 			try {
 				
 				//kill previous instances
 //				destroySerialProcess();
 				
 				System.out.println("trying to connect to:");
 				String connectTo = serialDumpPrg.toString() + " -b115200 " + portName;
 				connectTo = connectTo.substring(5);
 				serialLink = Runtime.getRuntime().exec(connectTo);
 				reader = new BufferedReader(new InputStreamReader(serialLink.getInputStream()));
 				writer = new BufferedWriter(new OutputStreamWriter(serialLink.getOutputStream()));
 				return true;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				System.out.println("failed connecting to the USB device");
 				e.printStackTrace();
 				return false;
 			}
 		}else{
 			System.out.println("couldnt find the binary to load");
 			return false;
 		}
     }
 	
 
 	private void destroySerialProcess(){
 		Process killer;
 		try {
 			killer = Runtime.getRuntime().exec("killall serialdump-linux");
 			killer.waitFor();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public static String[] retrieveHostSerialPortList() {
 		
 		try{
 			ArrayList<String> list = new ArrayList<String>();
 			Pattern pattern = Pattern.compile("ttyUSB[0-9]");
 			Process p = Runtime.getRuntime().exec("/bin/ls -l /dev");
 			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
 			
 			String s;
 			while((s = reader.readLine()) != null){
 				Matcher m = pattern.matcher(s);
 				if(m.find()){
 					list.add("/dev/" + s.substring(m.start(),m.end()));
 					
 				}
 				
 			}
 			
 			return listToArray(list);
 			
 		}catch(Exception e){
 			System.out.println("Couldnt list attached devices");
 		}
 		
 		return null;
 	}
 
 	public static String[] listToArray(ArrayList<String> resultList) {
 		String[] resultArray = new String[resultList.size()];
 		for(int i = 0; i < resultList.size(); i++){
 			resultArray[i] = resultList.get(i);
 		}
 		return resultArray;
 	}
 	
 	public void stop(){
 		try {
 			reader.close();
 			writer.close();
 		} catch (IOException e) {}
 		dispatcher.stopMe();
 		serialLink.destroy();
 //		destroySerialProcess();
 		
 	}
 
 }
