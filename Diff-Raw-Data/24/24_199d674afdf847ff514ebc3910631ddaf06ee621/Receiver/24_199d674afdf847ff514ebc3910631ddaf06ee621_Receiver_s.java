 package sdjug.demo;
 
 import gnu.io.CommPortIdentifier;
 import gnu.io.SerialPort;
 import gnu.io.SerialPortEvent;
 import gnu.io.SerialPortEventListener;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.sql.Timestamp;
 import java.util.Enumeration;
 
 import sdjug.demo.json.JSONException;
 import sdjug.demo.json.JSONObject;
 
 public class Receiver implements SerialPortEventListener
 {
   SerialPort serialPort;
   /** The port we're normally going to use. */
 
   private static final String PORT_NAMES[] = {"/dev/tty.usbmodemfd131", // Mac OS X
       "/dev/ttyUSB0", // Linux
       "COM3", // Windows
   };
 
   /** Buffered input stream from the port */
   private InputStream input;
   /** The output stream to the port */
   private OutputStream output;
   /** Milliseconds to block while waiting for port open */
   private static final int TIME_OUT = 2000;
   /** Default bits per second for COM port. */
   private static final int DATA_RATE = 9600;
 
   public void initialize()
   {
     CommPortIdentifier portId = null;
     Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
 
     // iterate through, looking for the port
     while (portEnum.hasMoreElements())
     {
       CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
       for (String portName : PORT_NAMES)
       {
         if (currPortId.getName().equals(portName))
         {
           portId = currPortId;
           break;
         }
       }
     }
 
     if (portId == null)
     {
       System.out.println("Could not find COM port.");
       return;
     }
 
     try
     {
       // open serial port, and use class name for the appName.
       serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);
 
       // set port parameters
       serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
 
       // open the streams
       input = serialPort.getInputStream();
       output = serialPort.getOutputStream();
 
       // add event listeners
       serialPort.addEventListener(this);
       serialPort.notifyOnDataAvailable(true);
     }
     catch (Exception e)
     {
       System.err.println(e.toString());
     }
   }
 
   /**
    * This should be called when you stop using the port. This will prevent port locking on platforms like Linux.
    */
   public synchronized void close()
   {
     if (serialPort != null)
     {
       serialPort.removeEventListener();
       serialPort.close();
     }
   }
 
   /**
    * Handle an event on the serial port. Read the data and print it.
    */
   public synchronized void serialEvent(SerialPortEvent oEvent)
   {
     if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE)
     {
       try
       {
         int available = input.available();
         byte chunk[] = new byte[available];
         input.read(chunk, 0, available);
 
         // Displayed results are codepage dependent
 
         String inStr = (new String(chunk)).replaceAll("(?:\\n|\\r)", "");
 
         pushData(Integer.parseInt(inStr));
 
       }
       catch (Exception e)
       {
         System.err.println(e.toString());
       }
     }
     // Ignore all the other eventTypes, but you should consider the other ones.
   }
 
   void pushData(int data)
   {
 
     JSONObject jo = new JSONObject();
     try
     {
      jo.put("Timestamp", (new Timestamp(System.currentTimeMillis())).toString());
       jo.put("SensorData", data);
 
       System.out.println(jo.toString());
 
       URL url = new URL("http://127.0.0.1:8090/datastore/sensordata");
       HttpURLConnection conn = (HttpURLConnection) url.openConnection();
       conn.setDoOutput(true);
       conn.setRequestMethod("POST");
       conn.setRequestProperty("Content-Type", "application/json");
 
       String input = jo.toString();
 
       OutputStream os = conn.getOutputStream();
       os.write(input.getBytes());
       os.flush();
 
       if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) { throw new RuntimeException("Failed : HTTP error code : "
           + conn.getResponseCode()); }
 
       BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
 
       String output;
       System.out.println("Output from Server .... \n");
       while ((output = br.readLine()) != null)
       {
         System.out.println(output);
       }
 
       conn.disconnect();
 
     }
     catch (JSONException e)
     {
       e.printStackTrace();
     }
     catch (MalformedURLException e)
     {
       e.printStackTrace();
     }
     catch (IOException e)
     {
       e.printStackTrace();
     }
   }
 
   public static void main(String[] args) throws Exception
   {
     Receiver main = new Receiver();
     main.initialize();
     System.out.println("Started");
   }
 }
