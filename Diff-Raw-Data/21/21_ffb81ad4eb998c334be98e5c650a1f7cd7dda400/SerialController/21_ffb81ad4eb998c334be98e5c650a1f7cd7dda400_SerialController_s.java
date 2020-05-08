 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package littlesmarttool2.comm;
 
 import gnu.io.CommPortIdentifier;
 import gnu.io.NoSuchPortException;
 import gnu.io.PortInUseException;
 import gnu.io.SerialPort;
 import gnu.io.UnsupportedCommOperationException;
 import java.util.ArrayList;
 import java.io.*;
 import java.util.Enumeration;
 import java.util.concurrent.TimeoutException;
 
 /**
  * Intended use order:
  *  create new
  *  set up listeners
  *  connect (via data from static getPortNames)
  * @author Rasmus
  */
 public class SerialController {
     private ArrayList<ConnectionListener> connectionListeners = new ArrayList<ConnectionListener>();
     private boolean connected = false;
     private SerialPort port;
     private OutputStream outStream;
     private BufferedReader inReader;
     private static final String INIT_STRING = "StratoSnapper 2 Init.";
    private final boolean DEBUG = false;
 
     /**
      * Add a connection listener which will get invoked when a 
      * a connection is either started or stopped.
      */
     
     public void addConnectionListener(ConnectionListener listener){
         if (!connectionListeners.contains(listener))
             connectionListeners.add(listener);
     }
     public void removeConnectionListener(ConnectionListener listener)
     {
         connectionListeners.remove(listener);
     }
     
     /**
      * Get a list of existing ports in the systems
      * @return 
      */
     public static ArrayList<String> getPortNames()
     {
         Enumeration ports = CommPortIdentifier.getPortIdentifiers();
         ArrayList<String> portNames = new ArrayList<>();
         while(ports.hasMoreElements()) {
             String portName = ((CommPortIdentifier)ports.nextElement()).getName();
             portNames.add(portName);
         }     
         return portNames;
     }
 
     /**
      * Connect to a StratoSnapper on a given Serial Port
      * @param portName The portname to connect to
      * @param timeOut The timout in ms to wait for a connection
      * @return The Firmware version of the connected StratoSnapper
      * @throws NoSuchPortException
      * @throws PortInUseException
      * @throws UnsupportedCommOperationException
      * @throws IOException
      * @throws TimeoutException 
      */
     public synchronized SerialCommand connect(String portName, int timeOut) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException, TimeoutException
     {
         CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
         port = (SerialPort) portIdentifier.open("LittleSmartTool 2", 1000);
         port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
         outStream = port.getOutputStream();
         inReader = new BufferedReader(new InputStreamReader(port.getInputStream()));
         
         String answer = send("V",timeOut);
         SerialCommand cmd = SerialCommand.fromMessage(answer);
         
         if (cmd == null || cmd.getCommand() != 'V')
             throw new IOException("Connected device gave unknown answer");
 
         connected = true;
         invokeConnectionChangedListeners(true);
         
         return cmd;
     }
     
     private void invokeConnectionChangedListeners(boolean value)
     {
         for (ConnectionListener l : connectionListeners)
             if (l != null)
                 l.connectionStateChanged(value);
     }
     
     public String send(String msg, int timeOut, int maxRepetitions) throws IOException, TimeoutException
     {
         while (maxRepetitions > 0)
         {
             maxRepetitions--;
             try {
                 return send(msg, timeOut);
             } catch (IOException | TimeoutException ex) {
                 if (maxRepetitions == 0)
                     throw ex;
             }
         }
         throw new TimeoutException("Timed out");
     }
     
     public SerialCommand send(SerialCommand command, int timeOut) throws IOException, TimeoutException
     {
         return SerialCommand.fromMessage(send(command.getCommand(),command.getArgs(), timeOut));
     }
     
     public String send(char command, String[] args, int timeOut, int maxRepetitions) throws IOException, TimeoutException
     {
         String msg = command + "";
         for (String s : args) msg += ";" + s;
         return send(msg,timeOut, maxRepetitions);
     }
     
     public String send(char command, String[] args, int timeOut) throws IOException, TimeoutException
     {
         String msg = command + "";
         for (String s : args) msg += ";" + s;
         return send(msg,timeOut);
     }
     
     /**
      * Send a message to the connected StratoSnapper
      * @param message The message to send (e.g O;1)
      * @param timeOut The timout in ms to wait for a response
      * @return The answer from the StratoSnapper
      * @throws IOException On error
      * @throws TimeoutException On timeout
      */
     public synchronized String send(String message, int timeOut) throws IOException, TimeoutException
     {
         long endTime = System.currentTimeMillis() + timeOut;
         if (DEBUG) System.err.println("Sending >>>" + message + "<<<");
         
         try {
             port.enableReceiveTimeout(timeOut);
         } catch (UnsupportedCommOperationException ex) {
         }
         
         String read = INIT_STRING;
         while (INIT_STRING.equals(read))
         {           
             //Actually send message
             outStream.write((message + ">").getBytes());
             outStream.flush();
             
             while(true)
             {
                 if (System.currentTimeMillis() > endTime)
                     throw new TimeoutException("Connection timeout exceeded");
                 try {
                     read = inReader.readLine();
                     break;
                 }
                 catch (IOException ex)
                 {
                     continue; //Nothing to read at this time
                 }
             }
         }
         if (DEBUG) System.err.println("Received >>>" + read + "<<<");
         return read;
     }
     
     public synchronized SerialCommand[] sendMultiResponse(String message, String term, int timeOut) throws IOException, TimeoutException
     {
         ArrayList<SerialCommand> response = new ArrayList<>();
         long endTime = System.currentTimeMillis() + timeOut;
         if (DEBUG) System.err.println("Sending (multi) >>>" + message + "<<<");
         try {
             port.enableReceiveTimeout(timeOut);
         } catch (UnsupportedCommOperationException ex) {
         }
         String read = INIT_STRING;
         while (INIT_STRING.equals(read))
         {
             //Actually send message
             outStream.write((message+">").getBytes());
             outStream.flush();
             while(true)
             {
                 if (System.currentTimeMillis() > endTime)
                     throw new TimeoutException("Timeout exceeded");
                 try {
                     read = inReader.readLine();
                     if (term.equals(read))
                         break; //All done
                     else
                     {
                         response.add(SerialCommand.fromMessage(read));
                         if (DEBUG) System.err.println("Received (multi) >>>" + read + "<<<");
                     }
                 }
                 catch (IOException ex)
                 {
                     continue; //Nothing to read at this time
                 }
             }
         }
         if (DEBUG) System.err.println("Multireceive complete. " + response.size() +  " parts");
         return response.toArray(new SerialCommand[response.size()]);
     }
     
     public void disconnect()
     {
        System.out.println("Disconnect");
         if (port == null || !connected) return;
         connected = false;
         invokeConnectionChangedListeners(false);
         port.close();
     }
     
     /**
      * Get a value indicating if this controller is connected
      */
     public boolean connected()
     {
         return connected;
     }
     
 }
