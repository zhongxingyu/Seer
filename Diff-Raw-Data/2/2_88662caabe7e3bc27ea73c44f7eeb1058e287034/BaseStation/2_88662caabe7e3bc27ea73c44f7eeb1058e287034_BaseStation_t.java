 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import lejos.pc.comm.NXTComm;
 import lejos.pc.comm.NXTCommException;
 import lejos.pc.comm.NXTCommFactory;
 import lejos.pc.comm.NXTInfo;
 
 
 public class BaseStation {
     public NXTComm connection;
     public NXTInfo info;
     public OutputStream os; 
     public boolean readFlag = true;
     public InputStream is; 
     public DataOutputStream oHandle;
     public DataInputStream iHandle;
     public String command; 
 	
     public BaseStation() throws NXTCommException, IOException
     {
     }
 	
     public void establishConnection() throws NXTCommException, IOException {
 	connection = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
 	info = new NXTInfo(NXTCommFactory.BLUETOOTH, "TROGDOR", "00:16:53:13:E6:74"); // your robot's name must be NXT and the code is 123
 
 	// open connections and data streams
 	connection.open(info);
 	os = connection.getOutputStream();
 	is = connection.getInputStream();
 	iHandle = new DataInputStream(is);
 	oHandle = new DataOutputStream(os);
 		
 	Thread PCreceiver = new Thread() { 
 		public void run() {
 		    while (readFlag) {
 			try {
 			    byte[] buffer = new byte[256];
 			    int count = iHandle.read(buffer); // might want to check ack later
 			    if (count > 0) {
 				String ret = (new String(buffer)).trim();
 				if(verifyChecksum(ret)){
 				    System.out.printf("NXJ: %s [%dms]\n", ret);
 				}
 			    }
 			    Thread.sleep(10);
 			} catch (IOException e) {
 			    System.out.println("Fail to read from iHandle bc "
 					       + e.toString());
 			    return;
 			} catch (InterruptedException e) {
 
 
 			}
 		    }
 		}
 	    };
 		
 		
 	PCreceiver.start();
 		
 	/*oHandle.close();
 	  iHandle.close();
 	  os.close();
 	  is.close();
 	  connection.close(); 
 	  readFlag = false;*/ 
     }
 	
     public void sendMessage(String message) throws IOException
     {
 	oHandle.write(message.getBytes());
 	oHandle.flush();
     }
 	
     public void moveForward() throws IOException
     {
 	//Encrypt a move forward command and send using bluetooth
 	command = "MSF0000000";
 	command = command + getChecksum(command);
 	sendMessage(command); 
     }
 	
     public void moveBackward() throws IOException
     {
 	//Encrypt a move backward command and send using bluetooth
 	command = "MSB0000000";
 	command = command + getChecksum(command);
 	sendMessage(command);
     }
 	
     public void turnLeft(int degrees) throws IOException
     {
 	//encrypt a turn left command and send using bluetooth
 	if(degrees == 0)
 	    {
 		command = "TNL0000000";
 	    }
 	else
 	    {
 		command = "TNL0000090";
 	    }
 	command = command + getChecksum(command);
 	sendMessage(command); 
     }
 	
     public void turnRight(int degrees) throws IOException
     {
 	//encrypt a turn right command and send using bluetooth
 	if(degrees == 0)
 	    {
 		command = "TNR0000000";
 	    }
 	else 
 	    {
 		command = "TNR0000090";
 	    }
 	command = command + getChecksum(command);
 	sendMessage(command); 
     }
 	
     public void stop() throws IOException
     {
 	//encrypt a stop command and send using bluetooth
 	command = "ST00000000";
 	command = command + getChecksum(command);
 	sendMessage(command); 
     }
 
     public void getTouchSensor() throws IOException
     {
 	command ="RST0000000";
	command = command + getChecksum(command);
 	sendMessage(command);
     }
     
     //verify the checksum that is held at the 11th byte.
     public boolean verifyChecksum(String message) {
 	if(message.length() == 11) {
             if(getChecksum(message.substring(0, 10)).equals(message.substring(10))){
                 return true;
             }
         }
         return false;
     }
 
     //Gets the checksum to be sent as the byte.
     private String getChecksum(String message) {
 	int sum = 0;
         String ret;
         byte[] buffer = message.getBytes();
         for (int i = 0; i < buffer.length; i++) {
             sum += (int) buffer[i];
         }
         sum = sum % 256;
         byte[] checksum = new byte[1];
         checksum[0] = (byte) sum;
         ret = new String(checksum);
         return ret;
     }
 }
