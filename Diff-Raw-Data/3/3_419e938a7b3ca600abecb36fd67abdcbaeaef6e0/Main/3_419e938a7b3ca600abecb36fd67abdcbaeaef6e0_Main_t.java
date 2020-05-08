 package org.anhonesteffort.commandler;
 
 import jssc.SerialPort;
 import org.anhonesteffort.tnc.ax25.AX25Operator;
 import org.anhonesteffort.tnc.ax25.AX25Port;
 import org.anhonesteffort.tnc.ax25.AX25Protocol;
 import org.anhonesteffort.tnc.ax25.AX25UIFrame;
 import org.anhonesteffort.tnc.kiss.KISSPort;
 
 public class Main {
 
     public static void main(String[] args) {
         try {
             // Open a serial port connected to a TNC in KISS mode.
             SerialPort serialPort = new SerialPort("COM9");
            //serialPort.openPort();
            //serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
 
 
             /*
                 KISS Examples
              */
 
             // Creating a KISSPort for High-Level Data Link Control port 0.
             KISSPort kissTNC0 = new KISSPort(0, serialPort);
 
             // Adding a KSSDataListener to process data received by the KISSPort.
             SimpleKISSDataListener kissDataListener = new SimpleKISSDataListener();
             kissTNC0.addKISSDataListener(kissDataListener);
 
             // Transmitting arbitrary data through the TNC.
             kissTNC0.transmitData("ChubSat, truth prevails.".getBytes("ASCII"));
 
 
 
             /*
                 AX.25 Examples
              */
 
             // Creating an AX25Port for sending and receiving AX.25 frames through a KISSPort.
             AX25Port ax25Port = new AX25Port(kissTNC0);
 
             // Adding an AX25FrameListener to process frames received by the AX25Port.
             SimpleAX25FrameListener ax25FrameListener = new SimpleAX25FrameListener();
             ax25Port.addFrameListener(ax25FrameListener);
 
             // Creating and transmitting Unnumbered Information Frames manually.
             AX25UIFrame uiFrame = new AX25UIFrame("CHBSAT".getBytes("ASCII"), "EARTH".getBytes("ASCII"));
             uiFrame.setInfoField("ChubSat, have it your way.".getBytes("ASCII"));
             ax25Port.sendFrame(uiFrame);
 
             // Creating an AX25Operator to transmit and receive frames on behalf of a call sign.
             AX25Operator chubSatOperator = new AX25Operator(ax25Port, "CHBSAT".getBytes("ASCII"));
             chubSatOperator.sendUnnumberedInformation("EARTH".getBytes("ASCII"), "ChubSat, like tears in the rain.".getBytes("ASCII"));
             chubSatOperator.sendUnnumberedInformation("CHBSAT".getBytes("ASCII"), "Hello ChubSat, this is ChubSat.".getBytes("ASCII"));
 
             // Creating an AX25SecondaryStation to transmit and receive frames on behalf of Operator: CHBSAT, SSID: 4.
             SimpleAX25SecondaryStation secondaryStation = new SimpleAX25SecondaryStation(4);
             chubSatOperator.addSecondaryStation(secondaryStation);
             secondaryStation.sendUnnumberedInformation("CHBSAT".getBytes("ASCII"), "Hello ChubSat SSID default destination, this is ChubSat SSID 4.".getBytes("ASCII"));
             secondaryStation.sendUnnumberedInformation("CHBSAT".getBytes("ASCII"), 4, "Hello ChubSat SSID 4, this is ChubSat SSID 4.".getBytes("ASCII"));
 
             /*
                 Cleaning up
              */
 
             //serialPort.closePort();
         }
         catch(Exception e) {
             System.out.println(e.toString());
         }
     }
 }
