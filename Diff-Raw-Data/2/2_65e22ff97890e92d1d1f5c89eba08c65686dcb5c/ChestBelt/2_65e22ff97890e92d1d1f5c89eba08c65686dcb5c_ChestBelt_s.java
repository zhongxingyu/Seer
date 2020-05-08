 /**
  * Copyright (C) 2012 SINTEF <franck.fleurey@sintef.no>
  *
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.gnu.org/licenses/lgpl-3.0.txt
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.thingml.chestbelt.driver;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
 
 public class ChestBelt implements Runnable {
 
     protected InputStream in;
     protected OutputStream out;
     
     private Thread rxthread = null;
 
     public ChestBelt(InputStream in, OutputStream out) {
         this.in = in;
         this.out = out;
         rxthread = new Thread(this);
         rxthread.start();
     }
 
     private int msg_size(byte code) {
         if (code == 101) {
             return 3;
         } else if (code == 106 || code == 107 || code == 110 || code == 112 || code == 114 || code == 117 || code == 121) {
             return 6;
         } else if (code == 120) {
             return 18;
         } else {
             return 5; // default value for other messages
         }
     }
     private long receivedBytes = 0;
 
     public long getReceivedBytes() {
         return receivedBytes;
     }
     
     public boolean isConnected() {
         if (rxthread != null) return rxthread.isAlive();
         else return false;
     }
     
     public void close() {
         
         try {
             terminate = true;
             // Wait up to 3 seconds for the rx thread to die before closing the streams
             rxthread.join(3000); 
             in.close();
             out.close();
         }
         catch(Exception e) {
             e.printStackTrace();
         }
     }
     
     private boolean terminate = false;
 
     public void run() {
 
         byte[] buffer = new byte[1024];
         int len = -1;
 
         int code = 0;
         int target_length = 0;
         int msg_index = 0;
         byte[] message = new byte[32];
 
         try {
             while (!terminate && ((len = this.in.read(buffer)) > -1)) {
                 receivedBytes += len;
                 //System.out.println("len = " + len);
                 for (int i = 0; i < len; i++) {
                     byte c = buffer[i];
                     // Check if this is the code for the start of a message
                     if (c > 95) {
                         // check if it was something in the buffer
                         if (msg_index != 0) {
                             System.err.println("ChestBelt: Received incomplete message (code = " + code + ").");
                         }
 
                         code = c;
                         msg_index = 0;
                         target_length = msg_size(c);
                         message[msg_index++] = c;
                     } else {
                         if (msg_index > 0 && msg_index < target_length) {
                             message[msg_index++] = c;
                             if (msg_index == target_length) {
                                 // We got a complete message: Forward to listeners
                                 switch (code) {
                                     case 110:
                                         cUSerialNumber(message);
                                         break;
                                     case 117:
                                         cUFWRevision(message);
                                         break;
                                     case 98:
                                         batteryStatus(message);
                                         break;
                                     case 105:
                                         indication(message);
                                         break;
                                     case 109:
                                         status(message);
                                         break;
                                     case 100:
                                         messageOverrun(message);
                                         break;
                                     case 106:
                                         referenceClockTime(message);
                                         break;
                                     case 107:
                                         fullClockTimeSync(message);
                                         break;
                                     case 104:
                                         heartRate(message);
                                         break;
                                     case 99:
                                         heartRateConfidence(message);
                                         break;
                                     case 101:
                                         eCGData(message);
                                         break;
                                     case 102:
                                         eCGSignalQuality(message);
                                         break;
                                     case 103:
                                         eCGRaw(message);
                                         break;
                                     case 112:
                                         gyroPitch(message);
                                         break;
                                     case 114:
                                         gyroRoll(message);
                                         break;
                                     case 121:
                                         gyroYaw(message);
                                         break;
                                     case 115:
                                         accLateral(message);
                                         break;
                                     case 119:
                                         accLongitudinal(message);
                                         break;
                                     case 118:
                                         accVertical(message);
                                         break;
                                     case 97:
                                         rawActivityLevel(message);
                                         break;
                                     case 116:
                                         skinTemperature(message);
                                         break;
                                     case 120:
                                         combinedIMU(message);
                                         break;
                                     default:
                                         System.err.println("ChestBelt: Received unknown message (code = " + code + ").");
                                 }
                                 // Re-initialize for the next message
                                 code = 0;
                                 msg_index = 0;
                                 target_length = 0;
                             }
                         } else {
                             System.err.println("ChestBelt: Received Corrupted Data.");
                         }
                     }
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         finally {
             System.err.println("ChestBelt: Receiver thread stopped.");
             if (!terminate) {
                 for (ChestBeltListener l : listeners) {
                     l.connectionLost();
                 }
             }
         }
     }
 
     long decodeLong(byte d1, byte d2, byte d3) {
         long result = 0;
         result += ((d1 - 32) & 0x3F) << 12;
         result += ((d2 - 32) & 0x3F) << 6;
         result += (d3 - 32) & 0x3F;
         return result;
     }
 
     synchronized void cUSerialNumber(byte[] message) {
         long value = decodeLong(message[1], message[2], message[3]);
         int timestamp = ((message[4] - 32) * 64 + (message[5] - 32));
         for (ChestBeltListener l : listeners) {
             l.cUSerialNumber(value, timestamp);
         }
     }
 
     synchronized void cUFWRevision(byte[] message) {
         long value = decodeLong(message[1], message[2], message[3]);
         int timestamp = ((message[4] - 32) * 64 + (message[5] - 32));
         for (ChestBeltListener l : listeners) {
             l.cUFWRevision(""+(message[1]-32) +"."+ (message[2]-32) +"."+ (message[3]-32), timestamp);
         }
     }
 
     synchronized void batteryStatus(byte[] message) {
         int value = ((message[1] - 32) * 64 + (message[2] - 32));
         int timestamp = ((message[3] - 32) * 64 + (message[4] - 32));
         for (ChestBeltListener l : listeners) {
             l.batteryStatus(value, timestamp);
         }
     }
 
     synchronized void indication(byte[] message) {
         int value = ((message[1] - 32) * 64 + (message[2] - 32));
         int timestamp = ((message[3] - 32) * 64 + (message[4] - 32));
         for (ChestBeltListener l : listeners) {
             l.indication(value, timestamp);
         }
     }
 
     synchronized void status(byte[] message) {
         int value = ((message[1] - 32) * 64 + (message[2] - 32));
         int timestamp = ((message[3] - 32) * 64 + (message[4] - 32));
         for (ChestBeltListener l : listeners) {
             l.status(value, timestamp);
         }
     }
 
     synchronized void messageOverrun(byte[] message) {
         int value = ((message[1] - 32) * 64 + (message[2] - 32));
         int timestamp = ((message[3] - 32) * 64 + (message[4] - 32));
         for (ChestBeltListener l : listeners) {
             l.messageOverrun(value, timestamp);
         }
     }
     
     long decode5byteLong(byte d1, byte d2, byte d3, byte d4, byte d5) {
         long result = 0;
         result += ((d1 - 32) & 0x1F) << 24; // dicard bit 29 which is used for something else
         result += ((d2 - 32) & 0x3F) << 18;
         result += ((d3 - 32) & 0x3F) << 12;
         result += ((d4 - 32) & 0x3F) << 6;
         result += (d5 - 32) & 0x3F;
         return result;
     }
 
     // Time and clock synchronization messages
     synchronized void referenceClockTime(byte[] message) {
         long value = decode5byteLong(message[1], message[2], message[3], message[4], message[5]);
         //System.out.println("message[1]-32=" + (message[1]-32) + "    ((message[1]-32) & 0x20) = "  + ((message[1]-32) & 0x20));
         boolean seconds = ((message[1]-32) & 0x20) == 0;
         for (ChestBeltListener l : listeners) {
             l.referenceClockTime(value, seconds);
         }
     }
 
     synchronized void fullClockTimeSync(byte[] message) {
         long value = decode5byteLong(message[1], message[2], message[3], message[4], message[5]);
         boolean seconds = ((message[1]-32) & 0x20) == 0;
         for (ChestBeltListener l : listeners) {
             l.fullClockTimeSync(value, seconds);
         }
     }
 
     // ECG and Heart rate messages
     synchronized void heartRate(byte[] message) {
         int value = ((message[1] - 32) * 64 + (message[2] - 32));
         int timestamp = ((message[3] - 32) * 64 + (message[4] - 32));
         for (ChestBeltListener l : listeners) {
             l.heartRate(value, timestamp);
         }
     }
 
     synchronized void heartRateConfidence(byte[] message) {
         int value = ((message[1] - 32) * 64 + (message[2] - 32));
         int timestamp = ((message[3] - 32) * 64 + (message[4] - 32));
         for (ChestBeltListener l : listeners) {
             l.heartRateConfidence(value, timestamp);
         }
     }
 
     synchronized void eCGData(byte[] message) {
         int value = ((message[1] - 32) * 64 + (message[2] - 32));
         for (ChestBeltListener l : listeners) {
             l.eCGData(value);
         }
     }
 
     synchronized void eCGSignalQuality(byte[] message) {
         int value = ((message[1] - 32) * 64 + (message[2] - 32));
         int timestamp = ((message[3] - 32) * 64 + (message[4] - 32));
         for (ChestBeltListener l : listeners) {
             l.eCGSignalQuality(value, timestamp);
         }
     }
 
     synchronized void eCGRaw(byte[] message) {
         int value = ((message[1] - 32) * 64 + (message[2] - 32));
         int timestamp = ((message[3] - 32) * 64 + (message[4] - 32));
         for (ChestBeltListener l : listeners) {
             l.eCGRaw(value, timestamp);
         }
     }
 
     // Gyroscope messages
     synchronized void gyroPitch(byte[] message) {
         int value = decodeGyro(message[1], message[2], message[3]);
         int timestamp = ((message[4] - 32) * 64 + (message[5] - 32));
         for (ChestBeltListener l : listeners) {
             l.gyroPitch(value, timestamp);
         }
     }
 
     synchronized void gyroRoll(byte[] message) {
         int value = decodeGyro(message[1], message[2], message[3]);
         int timestamp = ((message[4] - 32) * 64 + (message[5] - 32));
         for (ChestBeltListener l : listeners) {
             l.gyroRoll(value, timestamp);
         }
     }
 
     synchronized void gyroYaw(byte[] message) {
         int value = decodeGyro(message[1], message[2], message[3]);
         int timestamp = ((message[4] - 32) * 64 + (message[5] - 32));
         for (ChestBeltListener l : listeners) {
             l.gyroYaw(value, timestamp);
         }
     }
     // Accelerometer and activity messages
     synchronized void accLateral(byte[] message) {
         int value = decodeAcc(message[1], message[2]);
         int timestamp = ((message[3] - 32) * 64 + (message[4] - 32));
         for (ChestBeltListener l : listeners) {
             l.accLateral(value, timestamp);
         }
     }
 
     synchronized void accLongitudinal(byte[] message) {
         int value = decodeAcc(message[1], message[2]);
         int timestamp = ((message[3] - 32) * 64 + (message[4] - 32));
         for (ChestBeltListener l : listeners) {
             l.accLongitudinal(value, timestamp);
         }
     }
 
     synchronized void accVertical(byte[] message) {
         int value = decodeAcc(message[1], message[2]);
         int timestamp = ((message[3] - 32) * 64 + (message[4] - 32));
         for (ChestBeltListener l : listeners) {
             l.accVertical(value, timestamp);
         }
     }
 
     synchronized void rawActivityLevel(byte[] message) {
         int value = ((message[1] - 32) * 64 + (message[2] - 32));
         int timestamp = ((message[3] - 32) * 64 + (message[4] - 32));
         for (ChestBeltListener l : listeners) {
             l.rawActivityLevel(value, timestamp);
         }
     }
 
     // IR Temperature sensor messages
     synchronized void skinTemperature(byte[] message) {
         int value = ((message[1] - 32) * 64 + (message[2] - 32));
         int timestamp = ((message[3] - 32) * 64 + (message[4] - 32));
         for (ChestBeltListener l : listeners) {
             l.skinTemperature(value, timestamp);
         }
     }
 
     synchronized int decodeGyro(byte d1, byte d2, byte d3) {
         int result = 0;
         result += ((d1 - 32) & 0x3F) << 12;
         result += ((d2 - 32) & 0x3F) << 6;
         result += (d3 - 32) & 0x3F;
         if (result > (1 << 17)) {
             result = result - (1 << 18);
         }
         return result;
     }
 
     synchronized int decodeAcc(byte d1, byte d2) {
         int result = 0;
         result += ((d1 - 32) & 0x3F) << 6;
         result += (d2 - 32) & 0x3F;
         if (result > (1 << 11)) {
             result = result - (1 << 12);
         }
         return result;
     }
 
     synchronized void combinedIMU(byte[] message) {
         int ax = decodeAcc(message[1], message[2]);
         int ay = decodeAcc(message[3], message[4]);
         int az = decodeAcc(message[5], message[6]);
         int gx = decodeGyro(message[7], message[8], message[9]);
         int gy = decodeGyro(message[10], message[11], message[12]);
         int gz = decodeGyro(message[13], message[14], message[15]);
         int timestamp = ((message[16] - 32) * 64 + (message[17] - 32));
         for (ChestBeltListener l : listeners) {
             l.combinedIMU(ax, ay, az, gx, gy, gz, timestamp);
         }
     }
     private ArrayList<ChestBeltListener> listeners = new ArrayList<ChestBeltListener>();
 
     public synchronized void addChestBeltListener(ChestBeltListener l) {
         listeners.add(l);
     }
 
     public synchronized void removeChestBeltListener(ChestBeltListener l) {
         listeners.remove(l);
     }
 
     public void connectionRestored() {
         sendData(120, 0);
     }
 
     public void getSerialNumber() {
         sendData(110, 0);
     }
 
     public void getModelInfo() {
         sendData(114, 0);
     }
 
     public void setBTUpdateInterval(int value) {
         sendData(122, 32 + value);
     }
 
     public void requestCUTime(int value) {
         sendData(107, 32 + value);
     }
 
     public void setBatchDataMode() {
         sendData(98, 0);
     }
 
     public void setLiveDataMode() {
         sendData(108, 0);
     }
 
     public void setDataMode(ChestBeltMode mode) {
         sendData(109, 32 + mode.getCode());
     }
 
     public void setHRUpdateInterval(int value) {
         sendData(103, 32 + value);
     }
 
     public void setHRAverageInterval(int value) {
         sendData(104, 32 + value);
     }
 
     public void getCUFWRevision(int value) {
         sendData(117, 32 + value);
     }
 
     public void setTempAverageInterval(int value) {
         sendData(116, 32 + value);
     }
 
     protected void sendData(int code, int value) {
         try {
             // send the code
             out.write((int) code);
             // send the value
             out.write((int) value);
 
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 }
