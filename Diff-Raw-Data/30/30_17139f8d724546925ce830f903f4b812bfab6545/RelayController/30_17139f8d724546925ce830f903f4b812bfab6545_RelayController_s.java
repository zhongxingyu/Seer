 package controlv2;
 
 import java.io.IOException;
 import gnu.io.RXTXCommDriver;
 import java.io.*;
 import gnu.io.*;
 import java.util.Properties;
 
 /**
  *
  * @author Tyrone
  */
 public class RelayController extends Thread {
 
     /**
      * Class Variables
      */
     Controlv2 ctrl;
     SerialPort relaySerialPort;
     InputStream relayInputStream;
     OutputStream relayOutputStream;
     int sleepTime = 10000;
     // Triangle / Relay
     static Relay[][] relayTable = new Relay[19][8];
     static Flower[][] flowers = new Flower[3][12];
    byte[][] sensors = new byte[2][12];
 
     public RelayController(Controlv2 ctrl, String port, int baud, String programName) {
         this.ctrl = ctrl;
         try {
             relaySerialPort = initializeSerial(port, baud, programName);
             relayInputStream = relaySerialPort.getInputStream();
             relayOutputStream = relaySerialPort.getOutputStream();
             initializeFlowers();
         } catch (IOException ex) {
             System.out.println("Could not initialize relay components");
         }
 
     }
 
     public void run() {
         turnOff();
         turnOn();
 
         while (true) {
 //            runDiagonalChase();
 //            runFlowerChase();
 //            runAllOnOff();
 //            runInputTest();
             
            updateSensors();
         }
     }
 
     /**
      * Open a serial connection note: assumes databits=8, stopbits=1, no parity
      * and no flow control.
      */
     public SerialPort initializeSerial(String port, int baud, String programName) throws IOException {
         SerialPort serialPort;
         try {
             CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(port);
             serialPort = (SerialPort) portId.open(programName, 5000);
             serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
             serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
         } catch (NoSuchPortException ex) {
             throw new IOException("No such port");
         } catch (PortInUseException ex) {
             throw new IOException("Port in use");
         } catch (UnsupportedCommOperationException ex) {
             throw new IOException("Unsupported serial port parametes");
         }
         return serialPort;
     }
 
     /**
      * This method should create all triangles big and small and populate the
      * triangle / relay related tables.
      */
     public static void initializeFlowers() {
         for (int level = 0; level < 3; level++) {
             for (int flowerNumber = 0; flowerNumber < 12; flowerNumber++) {
                 flowers[level][flowerNumber] = new Flower(level, flowerNumber, relayTable);
             }
         }
 
         // initialise the rest of the relayTable
         // PSU's and unsed 7th relay
         for (int i = 0; i < 19; i++) {
             relayTable[i][0] = new Relay();
             relayTable[i][7] = new Relay();
         }
         // bank 1
         for (int i = 1; i < 7; i++) {
             relayTable[0][i] = new Relay();
         }
     }
 
     public void send(int i) {
         try {
             relayOutputStream.write(i);
         } catch (IOException ex) {
             ex.getMessage();
         }
     }
 
     public int readLine(byte[] bytes) {
         int numberBytesRead = 0;
         try {
             int a = relayInputStream.available();
             numberBytesRead = relayInputStream.read(bytes);
 
         } catch (IOException ex) {
             ex.getMessage();
         }
         return numberBytesRead;
     }
 
     public void sleep(int time) {
         try {
             Thread.currentThread().sleep(time);
         } catch (InterruptedException ex) {
             ex.printStackTrace();
         }
     }
 
     // Should activate a coloumn of flowers corresponding to the PIR sesor below it.
     // Should also test rain wind and light sensors.
     public void runInputTest() {
         while (true) {
             boolean triggered = false;
             try {
                 relayInputStream.skip(relayInputStream.available());
             } catch (IOException ex) {
                 ex.getMessage();
             }
             send(254);
             send(192);
             sleep(500);
             byte[] bytes = new byte[12];
             int numberBytesRead = readLine(bytes);
             for (int i = 0; i < 6; i++) {
                 System.out.println("sensor " + i + " is " + bytes[i]);
                 if (bytes[i] < 0) {
                     if (!flowers[0][i * 2 + 1].isInBloom()) {
                         triggered = true;
                         flowers[0][i * 2 + 1].allOn();
                         flowers[1][i * 2 + 1].allOn();
                         flowers[2][i * 2 + 1].allOn();
                     }
                 } else {
                     if (flowers[0][i * 2 + 1].isInBloom()) {
                         triggered = true;
                         flowers[0][i * 2 + 1].allOff();
                         flowers[1][i * 2 + 1].allOff();
                         flowers[2][i * 2 + 1].allOff();
                     }
                 }
             }
             updateRelays();
             if (triggered) {
                 System.out.println("Sleeping sensors for 7 seconds");
                 sleep(7000);
             } else {
                 sleep(100);
             }
         }
     }
 
     public void runDiagonalChase() {
         System.out.println("Starting diagonal chase");
         for (int level = 0; level < 3; level++) {
             turnOnBank(6 * level + 1);
             turnOnBank(6 * level + 6);
         }
         updateRelays();
         sleep(sleepTime);
         for (int level = 0; level < 3; level++) {
             turnOffBank(6 * level + 1);
             turnOffBank(6 * level + 6);
         }
 //        updateRelays();
 //        sleep(sleepTime);
         for (int level = 0; level < 3; level++) {
             turnOnBank(6 * level + 5);
             turnOnBank(6 * level + 2);
         }
         updateRelays();
         sleep(sleepTime);
         for (int level = 0; level < 3; level++) {
             turnOffBank(6 * level + 5);
             turnOffBank(6 * level + 2);
         }
 //        updateRelays();
 //        sleep(sleepTime);
         // horizontals
         for (int level = 0; level < 3; level++) {
             turnOnBank(6 * level + 4);
             turnOnBank(6 * level + 3);
         }
         updateRelays();
         sleep(sleepTime);
         for (int level = 0; level < 3; level++) {
             turnOffBank(6 * level + 4);
             turnOffBank(6 * level + 3);
         }
         updateRelays();
         sleep(sleepTime);
     }
 
     public void turnOnBank(int bank) {
         for (int relayNumber = 1; relayNumber < 8; relayNumber++) {
             relayTable[bank][relayNumber].setState(true);
         }
     }
 
     public void turnOffBank(int bank) {
         for (int relayNumber = 1; relayNumber < 8; relayNumber++) {
             relayTable[bank][relayNumber].setState(false);
         }
     }
 
     public void runFlowerChase() {
         System.out.println("Starting chase bloom");
         for (int level = 0; level < 3; level++) {
             for (int flowerNumber = 0; flowerNumber < 12; flowerNumber++) {
                 flowers[level][flowerNumber].allOn();
                 updateRelays();
                 sleep(1000);
 //                flowers[level][flowerNumber].allOff();
 //                updateRelays();
 //                sleep(sleepTime);
             }
         }
     }
 
     public void runAllOnOff() {
         for (int level = 0; level < 3; level++) {
             for (int flowerNumber = 0; flowerNumber < 12; flowerNumber++) {
                 flowers[level][flowerNumber].allOff();
             }
         }
         updateRelays();
         sleep(sleepTime);
         System.out.println("Starting all on off");
         for (int level = 0; level < 3; level++) {
             for (int flowerNumber = 0; flowerNumber < 12; flowerNumber++) {
                 flowers[level][flowerNumber].allOn();
             }
         }
         updateRelays();
         sleep(sleepTime);
         for (int level = 0; level < 3; level++) {
             for (int flowerNumber = 0; flowerNumber < 12; flowerNumber++) {
                 flowers[level][flowerNumber].allOff();
             }
         }
         updateRelays();
         sleep(sleepTime);
     }
 
     public void clear() {
         for (int bank = 1; bank < 19; bank++) {
             turnOffBank(bank);
         }
     }
 
     public void turnOff() {
         //pull in all actuators
         for (int bank = 1; bank < 19; bank++) {
             for (int relay = 1; relay < 8; relay++) {
                 relayTable[bank][relay].setState(false);
             }
         }
         updateRelays();
 
         // TODO: put in wait command, be weary of this pausing other aspects of totem.
 
         // turn off PSU's
         for (int bank = 0; bank < 19; bank++) {
             relayTable[bank][0].setState(false);
         }
         // relay coil 12V supply
         relayTable[0][1].setState(false);
         updateRelays();
     }
 
     public void turnOn() {
         // turn on all PSU's
         for (int bank = 0; bank < 19; bank++) {
             relayTable[bank][0].setState(true);
         }
         // relay coil 12V supply
         relayTable[0][1].setState(true);
         updateRelays();
     }
 
     public void updateRelays() {
         for (int bank = 0; bank < 19; bank++) {
             int command = 0;
             for (int relay = 0; relay < 8; relay++) {
                 if (relayTable[bank][relay].getState()) {
                     command += Math.pow(2, relay);
                 }
             }
             sleep(10);
             send(254);
             //sleep(2);
             send(140);
             //sleep(2);
             send(command);
             //sleep(2);
             send(bank + 1); // +1 because ProXR starts at 1
             sleep(8);
         }
     }
     
     public void updateSensors() {
         try {
             relayInputStream.skip(relayInputStream.available());
         } catch (IOException ex) {
             ex.getMessage();
         }
         send(254);
         send(192);
        sleep(500);
        byte[] bytes = new byte[12];
         int numberBytesRead = readLine(bytes);
        for(int i=0; i<12; i++) {
             if(Math.abs(sensors[0][i]-bytes[i])<6) {
                 sensors[1][i]=0;
                 sensors[0][i]=bytes[i];
             } else {
                 sensors[1][i]=1;
                 sensors[0][i]=bytes[i];
             }
         }
        sleep(500);
     }
     
     /**
      * When override is called totem should stop any sequence and run the override command.
      * 
      * @param input 
      */
     public void override(String input) {
         clear();
         String[] overrideCommands = input.split(",");
         for(int i=0; i<overrideCommands.length; i++) {
             String[] command = overrideCommands[i].split("-");
             int bank = Integer.parseInt(command[0]);
             String binaryCommand = Integer.toBinaryString(Integer.parseInt(command[0]));
         }
     }
     
 }
