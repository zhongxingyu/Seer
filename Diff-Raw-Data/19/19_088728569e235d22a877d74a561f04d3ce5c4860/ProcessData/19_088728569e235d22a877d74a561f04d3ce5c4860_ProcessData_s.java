 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package nl.biemond.smartmeter;
 
 import gnu.io.CommPort;
 import gnu.io.CommPortIdentifier;
 import gnu.io.SerialPort;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Scanner;
 
 /**
  *
  * @author edwin
  */
 public class ProcessData {
 
     MeasurementsDbStore store = MeasurementsDbStore.getInstance();
  
     
     public ProcessData(){
       System.out.println("ProcessData");  
     }
 
     
     public void connect(String portName) throws Exception {
         System.out.println("connect on usb");
         
         CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
 
         if (portIdentifier.isCurrentlyOwned()) {
             System.out.println("Error: Port is currently in use");
         } else {
 
             int timeout = 2000;
             while ( 1 != 0 ) {
 
                CommPort commPort = portIdentifier.open(this.getClass().getName(), timeout);
                if (commPort instanceof SerialPort) {
                     SerialPort serialPort = (SerialPort) commPort;
 
                     serialPort.setSerialPortParams(9600,
                             SerialPort.DATABITS_7,
                             SerialPort.STOPBITS_1,
                             SerialPort.PARITY_EVEN);
 
                     InputStream in = serialPort.getInputStream();
                     Thread myThread = new Thread(new SerialReader(in,commPort));
                     myThread.start();


                } else {
                     System.out.println("Error: Only serial ports are handled by this example.");
                 }
                 Thread.sleep(1800000);
             }
         }
     }
 
     private class SerialReader implements Runnable {
 
         InputStream in;
         CommPort commPort;
         public SerialReader(InputStream in ,CommPort commPort) {
             this.in = in;
             this.commPort = commPort;
         }
 
         public void run() {
             System.out.println("start new thread on "+new Date());
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             String line;
             StringBuffer buf = new StringBuffer();
             // continue processing
             try {
                 while ((line = br.readLine()) != null) {
                     buf.append(line).append(System.lineSeparator());
                     if ("!".equals(line)){
                       processSmartMeter(buf.toString());
                       in.close();
                       commPort.close();
                       System.out.println("return thread");
                       return;
                     }
                 }
             } catch (IOException ex) {
                 Thread.currentThread().interrupt();
             }
 
         }
     }
 
     private void processSmartMeter(String result) {
         Scanner scanner = new Scanner(result);
         String elecSerialId = null;
         Float meter181  = new Float(0);
         Float meter182  = new Float(0);
         Float meter281  = new Float(0);
         Float meter282  = new Float(0);
         Float consumption  = new Float(0);
         Float production   = new Float(0);
         Integer elecSwitch = 0;
         String  tarif = new String();
         
         String  gasSerialId = null;
         Float   measurement = new Float(0);
         Integer gasSwitch   = 0;
         Date    gasDate     = new Date();
         
         while (scanner.hasNextLine()) {
             String line = scanner.nextLine().trim();
             if (!"".equals(line)) {
                 // elect
                 if (line.startsWith("1-0:1.8.1")) {
                     meter181 = Float.valueOf(line.substring(line.indexOf("(") + 1, line.indexOf("*kWh")));
 
                 } else if (line.startsWith("1-0:1.8.2")) {
                     meter182 = Float.valueOf(line.substring(line.indexOf("(") + 1, line.indexOf("*kWh")));
 
                 } else if (line.startsWith("1-0:2.8.1")) {
                     meter281 = Float.valueOf(line.substring(line.indexOf("(") + 1, line.indexOf("*kWh")));
 
                 } else if (line.startsWith("1-0:2.8.2")) {
                     meter282 = Float.valueOf(line.substring(line.indexOf("(") + 1, line.indexOf("*kWh")));
 
                 } else if (line.startsWith("1-0:1.7.")) {
                     consumption = (Float.valueOf(line.substring(line.indexOf("(") + 1, line.indexOf("*kW"))) * 1000);
 
                 } else if (line.startsWith("1-0:2.7.0")) {
                     production = (Float.valueOf(line.substring(line.indexOf("(") + 1, line.indexOf("*kW"))) * 1000);
 
                 } else if (line.startsWith("0-0:96.14.0")) {
                    tarif = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
 
                 } else if (line.startsWith("0-0:96.3.10")) {
                     elecSwitch = Integer.valueOf(line.substring(line.indexOf("(") + 1, line.indexOf(")")));
 
                 } else if (line.startsWith("0-0:96.1.1")) {
                     elecSerialId = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                 }
 
                 // gas 
                 if (line.startsWith("0-1:24.3.0")) {
                     try {
                         gasDate = new SimpleDateFormat("yyMMddhhmmss", Locale.ENGLISH).parse(line.substring(11, 23));
                     } catch (ParseException e) {
                         e.printStackTrace();
                     }
                     line = scanner.nextLine();
                     measurement = Float.valueOf(line.substring(line.indexOf("(") + 1, line.indexOf(")")));
 
                 } else if (line.startsWith("0-1:24.4.0")) {
                     gasSwitch = Integer.valueOf(line.substring(line.indexOf("(") + 1, line.indexOf(")")));
 
                 } else if (line.startsWith("0-1:96.1.0")) {
                     gasSerialId = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                 }
 
             }
         }
         if ( elecSerialId != null) {
            int elecDeviceId = store.addDevice("ELEC", elecSerialId);
            if ( elecDeviceId != 0 ) {
                store.addEnergyMeasurement(elecDeviceId, meter181, meter182, meter281, meter282, tarif, consumption, production, elecSwitch);
  
            }
         }
         if ( gasSerialId != null) {
            int gasDeviceId = store.addDevice("GAS", gasSerialId);
            if ( gasDeviceId != 0 ) {
                store.addGasMeasurement( gasDeviceId, gasDate , measurement,gasSwitch);
            }    
         }
      }
 
     public static void main(String[] args) {
         try {
             (new ProcessData()).connect("/dev/ttyUSB0");
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
 }
