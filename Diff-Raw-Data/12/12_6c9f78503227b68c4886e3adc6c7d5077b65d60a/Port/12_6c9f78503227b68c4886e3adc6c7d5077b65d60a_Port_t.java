 package SerialPort;
 
 import gnu.io.*;
 import java.util.*;
 import Log.Logger;
 import Game.Score;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.FileDescriptor;
 import java.io.IOException;
 
 public class Port {
 
     private SerialPort serialPort;
     private String port;
     private int baudrate;
     private InputStream inputStream;
     private OutputStream outputStream;
     private Score score;
 
     public static String[] listPorts() throws Exception {
 
         CommPortIdentifier portId;
         Enumeration portList;
         String[] ports = new String[10];
 
         portList = CommPortIdentifier.getPortIdentifiers();
         int i = 0;
         while (portList.hasMoreElements()) {
             portId = (CommPortIdentifier) portList.nextElement();
 
             /* We willen alleen serieële poorten */
             if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                 ports[i] = portId.getName();
                 i++;
             }
         }
 
         if (i == 0) {
             ports = null;
         }
         return ports;
     }
 
     public Port(String port) {
         this.port = port;
     }
 
     public Port(String port, int baudrate) {
         this.port = port;
         this.baudrate = baudrate;
     }
    
     public void setScoreObject(Score score) {
         this.score = score;
     }
 
     public void open() {
 
         CommPortIdentifier portId = null;
 
         /* We openen de poort */
         try {
             portId = CommPortIdentifier.getPortIdentifier(this.port);
             this.serialPort = (SerialPort) portId.open(this.getClass().getName(), 2000);
             this.serialPort.setSerialPortParams(this.baudrate,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
 
             this.inputStream = serialPort.getInputStream();
             this.outputStream = serialPort.getOutputStream();
 
             this.serialPort.addEventListener(new ScoreReader(this.inputStream, this));
             this.serialPort.notifyOnDataAvailable(true);
 
             Logger.msg("Info", "Opened serial port: " + this.port + " with baudrate: " + this.baudrate);
 
         } catch (Exception e) {
             Logger.msg("Error", "Could not open " + this.port + " the message was: " + e.getMessage());
         }
     }
 
     private void sendPacket(byte[] packetLoad) {
 
         try {
         this.outputStream.write(packetLoad);
         this.outputStream.flush();
 
         Logger.msg("Info", "Sending: " + this.hexToString(packetLoad));
         } catch (Exception e) {
         Logger.msg("Error", "Could not send packet, the message was: " + e.getMessage());
         }
 
     }
 
     /* Stuur start commando naar alle vlaggen */
     public void roundStart() {
         Logger.msg("Info", "Sending start signal to all flags");
         byte[] startGame = { (byte)238, (byte)255, (byte)255, 0, 0, 17, 1, 1, 0, (byte)238, (byte)204 };
         /* Uit veiligheid sturen we een start en stop 3 keer */
         for (int i = 0; i < 3; i++) {
             this.sendPacket(startGame);
         }
     }
 
     /* Stuur het stop commando naar alle vlaggen */
     public void roundEnd() {
         Logger.msg("Info", "Sending stop signal to all flags");
         byte[] stopGame = { (byte)238, (byte)255, (byte)255, 0, 0, 17, 1, 2, 0, (byte)238, (byte)204 };
         /* Uit veiligheid sturen we een start en stop 3 keer */
         for (int i = 0; i < 3; i++) {
             this.sendPacket(stopGame);
             try {
                 Thread.sleep(100);
             } catch (Exception e) {}
         }
     }
 
     /* Reset de vlaggen, alles op 0 */
     public void reset() {
         Logger.msg("Info", "Resetting all flags");
         byte[] resetGame = { (byte)238, (byte)255, (byte)255, 0, 0, 17, 1, 3, 0, (byte)238, (byte)204 };
         this.sendPacket(resetGame);
     }
 
     public void reverseFlag(int destination) {
         Logger.msg("Info", "Sending timereverse to flag: " + destination);
         byte[] reverseFlag = { (byte)238, (byte)255, (byte)destination, 0, 0, 64, 1, 3, 0, (byte)238, (byte)204 };
         this.sendPacket(reverseFlag);
     }
 
     public void freezeFlag(int destination) {
         Logger.msg("Info", "Sending freeze to flag: " + destination);
         byte[] freezeFlag = { (byte)238, (byte)255, (byte)destination, 0, 0, 64, 1, 0, 0, (byte)238, (byte)204 };
         this.sendPacket(freezeFlag);
     }
 
     public void setGameSettings(int roundtime, int respawntime, int countdowntime) {
         Logger.msg("Info", "Sending round data to all flags. Roundtime: " + roundtime + ", Respawntime: " + respawntime);
 
         // De vlaggen verwachten hun data in 16 bits, daarom een cast naar een 16-bits short
         short sRoundTime = new Integer(roundtime).shortValue();
         short sRespawnTime = new Integer(respawntime).shortValue();
         short sCountDownTime = new Integer(countdowntime).shortValue();
 
         byte[] roundTimePacket = { (byte)238, (byte)255, (byte)255, 0, 2, 16, 4, 0, 1, (byte)(sRoundTime >> 8), (byte)sRoundTime, 0, (byte)238, (byte)204 };
         byte[] respawnTimePacket = { (byte)238, (byte)255, (byte)255, 0, 2, 16, 4, 1, 1, (byte)(sRespawnTime >> 8), (byte)sRespawnTime, 0, (byte)238, (byte)204 };
         byte[] countdownTimePacket = { (byte)238, (byte)255, (byte)255, 0, 2, 16, 4, 2, 0, (byte)(sCountDownTime >> 8), (byte)sCountDownTime, 0, (byte)238, (byte)204 };
 
         this.sendPacket(roundTimePacket);
         try {
             Thread.sleep(300);
         } catch (Exception e) {}
 
         this.sendPacket(respawnTimePacket);
         try {
             Thread.sleep(300);
         } catch (Exception e) {}
 
         this.sendPacket(countdownTimePacket);
     }
 
     public void poll() {
       byte[] pollPacket = { (byte)238, (byte)255, (byte)255, 0, 1, 32, 0, 0, (byte)238, (byte)204 };
       this.sendPacket(pollPacket);
     }
 
     public void close() {
     Logger.msg("Info", "Closing serial port.");
     this.serialPort.close();
     }
 
     public void finalize() {
     this.close();
     }
 
     protected synchronized void handleReceivedScores(byte[] packet) {
 
             byte sender;
             byte destination;
             byte messagetype;
             byte command;
             byte length;
             byte crc;
 
             destination = packet[2];
             sender      = packet[3];
             messagetype = packet[4];
             command     = packet[5];
             length      = packet[6];
         /* Op dit moment wordt er GEEN gebruik gemaakt van enige vorm van CRC */
         crc        = packet[length + 7];
 
         byte[] data =  new byte[length];
 
             for (int i = 0; i < length; i++) {
                 data[i] = packet[7 + i];
             }
             
         Logger.msg("Info", "Destination: " + destination + ", Sender: " + sender + ", Command: " + command);
 
             if (destination == 0) {
                 switch (command) {
                     case 32:
                         switch (sender) {
                             case 1:
                                this.score.setBlueBaseScore(data[6], (int)(data[2] << 8 | data[3] & 0xFF), (int)(data[4] << 8 | data[5] & 0xFF));
                                break;
                             case 2:
                                this.score.setRedBaseScore(data[6], (int)(data[2] << 8 | data[3] & 0xFF), (int)(data[4] << 8 | data[5] & 0xFF));
                                break;
                             case 3:
                                this.score.setSwingBaseScore(data[6], (int)(data[2] << 8 | data[3] & 0xFF), (int)(data[4] << 8 | data[5] & 0xFF));
                                 break;
 
                             default:
                                 Logger.msg("Warn", "Received a packet with wrong sender (" + sender + "), please check the dipswitches!");
                                 break;
               }
                         break;
 
                     default:
                         break;
                 }
 
             }
 
         Logger.msg("Info", "Finished processing packet");
 
     }
 
     public static class ScoreReader implements SerialPortEventListener {
 
     private InputStream in;
     private byte[] buffer = new byte[32];
     private Port port;
         
     public ScoreReader (InputStream in, Port port) {
         this.in = in;
         this.port = port;
     }
         
     public synchronized void serialEvent(SerialPortEvent arg0) {
       int data;
 
       try {
         int len = 0;
         boolean validPacket = true;
 
         while ((data = in.read()) > -1) {
 
           /* Als het eerste pakket geen 0xFF (255) bevat, dan is het geen volledig pakket */
           if (len == 0 && data != 238) {
         Logger.msg("Warn", "Corrupt packet received, did not start with 0xFF!");
         validPacket = false;
           } else {
         buffer[len++] = (byte) data;
 
         /* End of packet, verwerk het ingekomen pakket */
         if (data == 204) {
           break;
         }
 
           }
 
         }
 
         if (validPacket) {
           Logger.msg("Info", "Received packet: " + port.hexToString(buffer));
           port.handleReceivedScores(buffer);
         }
 
       } catch ( IOException e ) {
         e.printStackTrace();
       }             
     }
     }
 
     protected String hexToString(byte[] packet) {
     String hexString = "";
     for (int i = 0; i < packet.length; i++) {
         if (packet[i] == 204) {
         break;
         }
         String hex = "0x";
         hex += Integer.toHexString(packet[i]).toUpperCase();
         hexString += hex + " ";
     }
     return hexString;
     }
 
 
 }
