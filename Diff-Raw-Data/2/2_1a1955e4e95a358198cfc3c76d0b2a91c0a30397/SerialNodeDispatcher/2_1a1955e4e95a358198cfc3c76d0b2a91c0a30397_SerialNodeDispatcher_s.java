 package de.cased.utilities;
 
import gnu.io.SerialPort;

 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.SwingUtilities;
 
 import de.cased.serial.ReceiveStringFromSerial;
 
 public class SerialNodeDispatcher extends Thread{
 	
 	private BufferedReader reader;
 	private StringBuffer result_buffer;
 
 	private ReceiveStringFromSerial receiver;
 	private Logger logger = Logger.getLogger("KeyGenerator");
 	private boolean keep_running = true;
 	private String[] keywords;
 	
 	public SerialNodeDispatcher(BufferedReader reader, ReceiveStringFromSerial receiver) {
 		this.reader = reader;
 		this.result_buffer = new StringBuffer();
 		this.receiver = receiver;
 	}
 	
 	private boolean matchKeyword(String read){
 		
 		if(read == null)
 			return false;
 		
 		synchronized (keywords) {
 			for (String keyword : keywords) {
 				
 				
 				Pattern p = Pattern.compile(keyword, Pattern.DOTALL); // DOTALL makes the dot matching new lines too
 				Matcher m = p.matcher(read);
 				
 				if(m.matches()){
 //					System.out.println("found mathing for keyword:" + keyword);
 					return true;
 				}
 			}
 			
 			return false;
 		}
 		
 	}
 	
 	public void setKeywords(String[] keywords){
 		synchronized (keywords) {
 			this.keywords = keywords;
 		}
 		
 	}
 	
 	private void sendNotificationString(final String input){
 //		SwingUtilities.invokeLater(new Runnable() {
 //			
 //			@Override
 //			public void run() {
 //				receiver.receiveString(input);
 //			}
 //		});
 		
 		receiver.receiveString(input);
 	}
 
 	@Override
 	public void run() {
 		try{
 			while(keep_running){ // && expectedLineCount > 0){
 				
 //				if(Thread.interrupted()){
 //					keep_running = false;
 //					logger.log(Level.INFO, "interrupted, SerialNodeDispatcher is going to die normally");
 //				}
 				String read = reader.readLine();
 				System.out.println("READER:" + read);
 //				System.out.println("KEYWORD:" + keywords[0]);
 				if(matchKeyword(read)){
 //					System.out.println("READER_MATCH");
 					sendNotificationString(read);
 				}
 //				int available = reader.available();
 //				if(available > 0){
 //					read = (char) reader.read();
 //					
 //					result_buffer.append(read);
 //					
 //					if(matchKeyword()){
 //						keep_running = false;
 //						
 //						logger.log(Level.INFO, "SerialNodeDispatcher matches keyword, aborting:" + result_buffer.toString());
 //					}
 //						
 //					
 //				}
 				
 			}
 			
 //			sendTerminationString(result_buffer.toString());
 			
 			
 
 
 //			port.close();
 //			port = null;
 //			
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 
 	}
 	
 	public void stopMe(){
 		keep_running = false;
 	}
 
 }
