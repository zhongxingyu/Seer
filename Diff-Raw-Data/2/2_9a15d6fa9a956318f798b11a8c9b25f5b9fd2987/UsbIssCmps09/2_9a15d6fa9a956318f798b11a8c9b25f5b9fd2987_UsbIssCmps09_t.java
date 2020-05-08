 /*
 /* Copyright (c) 2012-2013 Csaba Kiraly http://csabakiraly.com
  *
 * This is a java driver for the CMPS09/CMPS10 tilt compensated compass.
  *
  * UsbIssCmps09 is free software: you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * UsbIssCmps09 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero
  * General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with UsbIssCmps09.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 import java.io.*;
 import net.tinyos.comm.*;
 
 public class UsbIssCmps09 implements SerialPortListener {
 
   public enum Axis {YAW, PITCH, ROLL};
 
   private SerialPort port;
   private String portName;
   private InputStream in;
   private OutputStream out;
 
   Object readBlock = new Object();
 
   private QueryThread queryThread;
 
   private int yaw;
   private int roll;
   private int pitch;
 
   private class QueryThread extends Thread {
     public void run() {
       while (true) {
         try {
           int[] ret = queryCMPS10();
           yaw = ret[Axis.YAW.ordinal()];
           pitch = ret[Axis.PITCH.ordinal()];
           roll = ret[Axis.ROLL.ordinal()];
         } catch (IOException e) {
           // TODO: handle error in the serial comm
         }
       }
     }
   }
 
   public UsbIssCmps09(String portName) throws IOException, UnsupportedCommOperationException {
     this.portName = portName;
     open();
     setI2CMode();
 
     queryThread = new QueryThread();
     queryThread.start();
   }
 
   private void open() throws IOException, UnsupportedCommOperationException {
     System.out.println("Opening port " + portName);
     port = new TOSSerial(portName);
 
     try {
       //port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
       port.setSerialPortParams(19200, 8, SerialPort.STOPBITS_2, false);
       port.addListener(this);
       port.notifyOn(SerialPortEvent.DATA_AVAILABLE, true);
       port.notifyOn(SerialPortEvent.OUTPUT_EMPTY, true);
     } catch (Exception e) {
       port.close();
       throw new IOException("Could not configure " + portName + ": "+ e.getMessage());
     }
 
     in = port.getInputStream();
     out = port.getOutputStream();
 
     port.open();
   }
 
   private void setI2CMode() throws IOException {
     byte[] cmd = {
             0x5A,	// USB_ISS command
             0x02,	// Set mode
             0x40,	// Set mode to 100KHz I2C
             0x00    // Spare pins set to output low
     };
     byte[] resp = new byte[2];
 
     System.out.println("Writing " + cmd[0] + " " + cmd[1] + " " + cmd[2] + " " + cmd[3]);
     out.write(cmd);
     out.flush();
     read(resp);
     System.out.println("Response " + resp[0] + " " + resp[1]);
     if (resp[0] != (byte)0xFF) {
       throw new IOException("**set_i2c_mode: Error setting I2C mode!**");
     }
   }
 
   // TOSSerial's InputStream is not a real InputStream, it does not respect the semantics in that it is non-blocking.
   // This is a helper function to implement the blocking variant, already for n bytes
   private int read(byte[] b) throws IOException  {
     int r = 0;
 
     while (r < b.length) {
       synchronized (readBlock) {
         if (in.available() == 0) {
           try {
             readBlock.wait();
           } catch (InterruptedException e) {}	//continue the cycle
         } else {
           int ret = in.read(b);
           if (ret == -1) {
             return -1;
           } else {
             r += ret;
           }
         }
       }
     }
     return r;
   }
 
   public void serialEvent(SerialPortEvent ev) {
     if (ev.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
       synchronized (readBlock) {
         readBlock.notify();
       }
     }
   }
 
   private int[] queryCMPS10() throws IOException {
     int[] ret = new int[3];
 
     byte[] cmd = {
     		0x55,	// USBI2C command for single byte address device
     		(byte)0xC1,	// CMPS09 address with R/W bit set high
     		0x00,	// Register we want to read from (0 is software version)
     		0x06
     };
     byte[] resp = new byte[6];
 
     out.write(cmd);
     out.flush();
     read(resp);
 
     ret[Axis.YAW.ordinal()] = ((resp[2] << 8) + (resp[3] & 0xFF)) /10;
     ret[Axis.PITCH.ordinal()] = resp[4];
     ret[Axis.ROLL.ordinal()] = resp[5];
     return ret;
   }
 
   public int getDirection() {
     return yaw;
   }
 
   public int getYaw() {
     return yaw;
   }
 
   public int getRoll() {
     return roll;
   }
 
   public int getPitch() {
     return pitch;
   }
 
   public static void main(String[] args) throws IOException, UnsupportedCommOperationException {
     UsbIssCmps09 compass = new UsbIssCmps09(args[0]);
     while(true) {
       System.out.println(compass.getDirection());
     }
   }
 }
