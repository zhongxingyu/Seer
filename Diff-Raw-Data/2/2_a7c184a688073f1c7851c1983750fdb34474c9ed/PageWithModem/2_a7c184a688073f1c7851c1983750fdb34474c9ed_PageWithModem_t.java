 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package modem;
 
 import java.io.*;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 import log.LoggingSystem;
 
 /**
  *
  * @author Shawn
  */
 public class PageWithModem implements Runnable, ReadListener {
     
     private static final String PP_PORT = "pagingPlugPort", MC_IP = "voiceModemIP", MC_PORT = "voiceModemPort";
     private static final File configFile = new File("modemProps.cfg");
     
     private ModemConnector mc;
     private PagingPlug plug;
     
     private Properties props = new Properties();
     
     private NumberFormat format = new DecimalFormat("0000");
     
     public PageWithModem() {
         super();
         
         try {
             loadProps();
         } catch (IOException ex) {}
         
         initModem();
         
         //used to declare plug
         new Thread(this).start();
     }
 
     
     
     private void loadProps() throws IOException {
         if(props == null)
             props = new Properties();
         
         if(!configFile.exists()) {
            String makePath = configFile.getPath().replace("modemProps.cfg", "");
             new File(makePath).mkdirs();
             configFile.createNewFile();
             loadProps(); // if the config file doesnt exist, then create the config file and try to load the properties again
             savePropsAndClose();
         } else {
             props.load(new FileInputStream(configFile)); 
         }
     }
     
     private void savePropsAndClose() {
         try {
             props.store(new FileOutputStream(configFile), "Modem Properties, used by SCADA server");
             JOptionPane.showMessageDialog(null, "Please configure modem properties in " + configFile.getPath());
             System.exit(7);
         } catch (IOException ex) {
             Logger.getLogger(PageWithModem.class.getName()).log(Level.SEVERE, null, ex);
             System.exit(8);
         }
     }
     
     private void initModem() {
         try {
             String ip = props.getProperty(MC_IP).trim();
             String port = props.getProperty(MC_PORT).trim();
             
             mc = new ModemConnector(ip, port);
             mc.addReadListener(this);
             mc.start();
         } catch (IOException ex) {
             Logger.getLogger(PageWithModem.class.getName()).log(Level.SEVERE, null, ex);
             JOptionPane.showMessageDialog(null, "Voice Modem ip and port incorrect");
         }
     }
     
     public void startPage(int jobID, String message) {
         try {
             plug.startPage(jobID, message);
         } catch (IOException ex) {
             fix();
             startPage(jobID, message);
         }
     }
             
     public void stopPage(int jobID) {
         try {
             plug.stopPage(jobID);
         } catch (IOException ex) {
             fix();
             stopPage(jobID);
         }
     }
     
     public void acknowledgePage(int jobID) {
         try {
             plug.acknowledgePage(jobID);
         } catch (IOException ex) {
             fix();
             acknowledgePage(jobID);
         }
     }
     
     public int getStatus(int jobID) {
         try {
             return plug.getStatus(jobID);
         } catch (IOException ex) {
             fix();
             return getStatus(jobID);
         }
     }
     
     public String getAllActivePages() {
         try {
             return plug.getAllActivePages();
         } catch (IOException ex) {
             fix();
             return getAllActivePages();
         }
     }
     
     public void stopAllRunningPages() {
         try {
             plug.stopAllRunningPages();
         } catch (IOException ex) {
             fix();
             stopAllRunningPages();
         }
 ;
     }
     
     @Override
     public void run() {
         try {
             
             
             int port = Integer.parseInt(props.getProperty(PP_PORT));
             ServerSocket ss = new ServerSocket(port);
             while(true) {
                 Socket socket = ss.accept();
                 plug = new PagingPlug(socket);
             }
             
         } catch (IOException ex) {
             JOptionPane.showMessageDialog(null, "Server already on desginated port. Please reconfigure " + PP_PORT + " in " + configFile.getPath());
         }
         
     }
     
     private void fix() {
         try {
             JOptionPane.showMessageDialog(null, "The paging module needs to connect to: " + InetAddress.getLocalHost().getHostAddress() + ":" + props.getProperty(PP_PORT));
         } catch(Exception ex) {}
     }
     
     
     public PagingPlug getPagingPlug() {
         return plug;
     }
 
     @Override
     public void onRead(final String pinText) {
         Runnable r = new Runnable() {
             @Override
             public void run() {
                 try {
                     if(plug == null) {
                         JOptionPane.showMessageDialog(null, "Paging system not connected. Please connect and click OK");
                         onRead(pinText);
                         return;
                     }
                     int pin = Integer.parseInt(pinText);
                     int jobID = plug.jacg.getJobID(pin);
                     plug.acknowledgePage(jobID);
                     plug.jacg.ackJobID(jobID);
                 } catch (IOException ex) {
                     fix();
                     onRead(pinText);
                 }
             }
         };
         new Thread(r).start();
     }
     
     public class PagingPlug  {
         
         private InputStream is;
         private OutputStream os;
         private JobAckCodeGenerator jacg;
         
         public PagingPlug(Socket socket) throws IOException {
             super();
             is = socket.getInputStream();
             os = socket.getOutputStream();
             jacg = new JobAckCodeGenerator();
         }
         
         protected void startPage(int jobID, String message) throws IOException {
             int ackCode = jacg.generateAckCode(jobID);
             String compose = "ST " + jobID + " " + message + " ACKCODE:" + format.format(ackCode);
             os.write(compose.getBytes());
             os.flush();
             LoggingSystem.getLoggingSystem().alertAllLogListeners("Page sent: jobID-" + jobID + " message-" + message);
         }
         
         protected void acknowledgePage(int jobID) throws IOException {
             String compose = "ACK " + jobID;
             os.write(compose.getBytes());
             os.flush();
             LoggingSystem.getLoggingSystem().alertAllLogListeners("Acknowledgement received: jobID-" + jobID);
         }
         
         protected void stopPage(int jobID) throws IOException {
             String compose = "SP " + jobID;
             os.write(compose.getBytes());
             os.flush();
         }
         
         protected int getStatus(int jobID) throws IOException {
             String compose = "S " + jobID;
             os.write(compose.getBytes());
             os.flush();
             return Integer.parseInt(readBuffer());
         }
         
         protected String getAllActivePages() throws IOException {
             String compose = "AAP";
             os.write(compose.getBytes());
             os.flush();
             return readBuffer();
         }
         
         protected void stopAllRunningPages() throws IOException {
             String compose = "SPA";
             os.write(compose.getBytes());
             os.flush();
         }
         
         private String readBuffer() throws IOException {
             String buffer = "";
             
             while(is.available() > 0) {
                 int read = is.read();
                 if(read == -1) {
                     throw new IOException("The connection was broken.");
                 }
                 buffer += (char) read;
             }
             
             return buffer;
         }
     }
     
     private class JobAckCodeGenerator {
         
         private Random random;
         private ArrayList<AckCode> activeCodes;
         
         public JobAckCodeGenerator() {
             super();
             random = new Random();
             activeCodes = new ArrayList();
         }
         
         public int generateAckCode(int jobID) {
             String codeText = "";
             int ran = -1;
             do {
                 int randomInt = random.nextInt(9); //0-8
                 randomInt++; //1-9
                 codeText += randomInt;
                 ran = Integer.parseInt(codeText);
             } while(!randomUsed(ran));
             //now we have a random int that isn't used yet
             AckCode code = new AckCode(jobID, ran);
             activeCodes.add(code);
             return code.getAckCode();
         }
         
         
         private boolean randomUsed(int randomInt) {
             for(AckCode code: activeCodes) {
                 if(code.getAckCode() == randomInt)
                     return true;
             }
             return false;
         }
         
         public int getAckCode(int jobID) {
             for(AckCode code: activeCodes) {
                 if(code.jobID == jobID)
                     return code.getAckCode();
             }
             
             return -1;
         }
         
         public int getJobID(int ackCode) {
             for(AckCode code: activeCodes) {
                 if(code.ackCode == ackCode)
                     return code.jobID;
             }
             
             return -1;
         }
         
         /*
          * Acknoledge the jobID
          */
         public void ackJobID(int jobID) {
             for(AckCode code: activeCodes) {
                 if(code.getJobID() == jobID) {
                     activeCodes.remove(code);
                     return;
                 }
             }
         }
         
         private class AckCode {
             private final int jobID;
             private final int ackCode;
             
             public AckCode(int jobID, int ackCode) {
                 super();
                 this.jobID = jobID;
                 this.ackCode = ackCode;
             }
 
             public int getAckCode() {
                 return ackCode;
             }
 
             public int getJobID() {
                 return jobID;
             }
         }
     }
 }
