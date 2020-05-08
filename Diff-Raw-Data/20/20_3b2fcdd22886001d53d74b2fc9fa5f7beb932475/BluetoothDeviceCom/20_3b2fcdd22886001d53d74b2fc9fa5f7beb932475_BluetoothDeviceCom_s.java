 package madsdf.shimmer.gui;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Properties;
 import java.util.logging.Logger;
 import javax.microedition.io.Connector;
 import javax.microedition.io.StreamConnection;
 
 /**
  * Receive the information from the device and transmit them to the chart drawer
  *
  * Java version : JDK 1.6.0_21 IDE : Netbeans 7.1.1
  *
  * @author Gregoire Aubert
  * @version 1.0
  */
 public class BluetoothDeviceCom extends Observable implements Runnable {
     // See /home/julien/work/shimmer/tinyos-2.x-contrib/shimmer/apps/BoilerPlate/README.txt
     public final byte START_STREAMING_COMMAND = 0x07;
     public final byte STOP_STREAMING_COMMAND = 0x20;
     public final byte TOGGLE_LED_COMMAND = 0x06;
     public final byte SET_SAMPLING_RATE_COMMAND = 0x05;
     public final byte SET_SENSORS_COMMAND = 0x08;
     public final byte SET_ACCEL_RANGE_COMMAND = 0x09;
     public final byte INQUIRY_COMMAND = 0x01;
     public final byte SENSOR_ACCEL = (byte) 0x80;
     public final byte SENSOR_GYRO = 0x40;
     public final byte SAMPLING_1000HZ = 0x01;
     public final byte SAMPLING_500HZ = 0x02;
     public final byte SAMPLING_250HZ = 0x04;
     public final byte SAMPLING_200HZ = 0x05;
     public final byte SAMPLING_166HZ = 0x06;
     public final byte SAMPLING_125HZ = 0x08;
     public final byte SAMPLING_100HZ = 0x0A;
     public final byte SAMPLING_50HZ = 0x14;
     public final byte SAMPLING_10HZ = 0x64;
     public final byte SAMPLING_0HZ_OFF = (byte) 0xFF;
     public final byte RANGE_1_5G = 0x0;
     public final byte RANGE_2_0G = 0x1;
     public final byte RANGE_4_0G = 0x2;
     public final byte RANGE_6_0G = 0x3;
     public final int SAMPLE_SIZE = 15;
     private final static Logger Log = Logger.getLogger(BluetoothDeviceCom.class.getName());
     private StreamConnection bluetoothConnection;
     private OutputStream os;
     private InputStream is;
     private Thread thread;
     byte[] packetReceive = new byte[240];
     private boolean continueRead = true;
     
     // Accel/gyro sample packet type
     public final byte DATA_TYPE = 0x00;
     
     private float[] accel_offset = new float[3];
     private float[] accel_gain = new float[3];
     private float[] gyro_offset = new float[3];
     private float[] gyro_gain = new float[3];
 
     /**
      * Constructor
      *
      * @param connectionURL is the service on the device
      * @param observers a list of observers to be notified when new data is available
      */
     public BluetoothDeviceCom(Observer[] observers, String btDeviceID) throws IOException {
         for (Observer o: observers) {
             this.addObserver(o);
         }
         
         // Load calibration
        final String accelFilename = "shimmer_calib/1_5_" + btDeviceID + ".accel.properties";
        final String gyroFilename = "shimmer_calib/1_5_" + btDeviceID + ".gyro.properties";
         loadCalibration(accelFilename, accel_offset, accel_gain);
         loadCalibration(gyroFilename, gyro_offset, gyro_gain);
     }
     
     private void loadCalibration(String filename, float[] outOffset, float[] outGain) throws IOException {
         Properties prop = new Properties();
        prop.load(new FileInputStream(filename));
         outOffset[0] = Float.parseFloat(prop.getProperty("offset_x"));
         outOffset[1] = Float.parseFloat(prop.getProperty("offset_y"));
         outOffset[2] = Float.parseFloat(prop.getProperty("offset_z"));
         outGain[0] = Float.parseFloat(prop.getProperty("gain_x"));
         outGain[1] = Float.parseFloat(prop.getProperty("gain_y"));
         outGain[2] = Float.parseFloat(prop.getProperty("gain_z"));
     }
 
     public void connect(String connectionURL) throws IOException {
         // Connection to the bluetooth device
         bluetoothConnection = (StreamConnection) Connector.open(connectionURL);
         os = bluetoothConnection.openOutputStream();
         is = bluetoothConnection.openInputStream();
 
         thread = new Thread(this);
         thread.start();
         Log.info("Connected to " + connectionURL);
     }
     
     private AccelGyroSample parseSample(long time, byte[] sample) throws IllegalArgumentException {
         float[] accel = new float[3];
         float[] gyro = new float[3];
         if (sample[0] == DATA_TYPE) {
             // Data is in little endian, we are too :)
             // We have to skip the first 3 bytes in the packet (first is datatype
             // , second and third are probably timestamp ?)
             // TODO: figure what second/third bytes are
             int offset = 3;
             for (int i = 0; i < 3; ++i) {
                 accel[i] = ByteUtils.uint16ToInt(sample[offset], sample[offset + 1]);
                 offset += 2;
             }
             for (int i = 0; i < 3; ++i) {
                 gyro[i] = ByteUtils.uint16ToInt(sample[offset], sample[offset + 1]);
                 offset += 2;
             }
         } else {
             throw new IllegalArgumentException("AccelGyroSample, bad data : "
                     + ByteUtils.getHexString(sample, sample.length));
         }
         
         // Calibrate
         for (int i = 0; i < 3; ++i) {
             accel[i] = ((accel[i] - accel_offset[i]) / accel_gain[i]) * 9.81f;
         }
         for (int i = 0; i < 3; ++i) {
             gyro[i] = (gyro[i] - gyro_offset[i]) / gyro_gain[i];
         }
         return new AccelGyroSample(time, accel, gyro);
     }
     
     private void sendCommand(byte... commands) throws IOException {
         os.write(commands);
         is.read(packetReceive);
     }
 
     @Override
     public void run() {
         try {
             // Activate the accelerometer and the gyroscope
             sendCommand(SET_SENSORS_COMMAND,
                         (byte)(SENSOR_GYRO | SENSOR_ACCEL),
                         (byte)0x00);
 
             // Define the frequency
             sendCommand(SET_SAMPLING_RATE_COMMAND, SAMPLING_50HZ);
             
             // Define accel range
             sendCommand(SET_ACCEL_RANGE_COMMAND, RANGE_1_5G);
 
             // Starts the data streaming
             sendCommand(START_STREAMING_COMMAND);
 
             // Starts reading data
             int bytesRead;
             byte[] sample = new byte[SAMPLE_SIZE];
             int current = 0;
             while (continueRead) {
 
                 // Read the received packet, can contain multiple sample, and 
                 // the last can be incomplete
                 bytesRead = is.read(packetReceive);
 
                 for (int i = 0; i < bytesRead; i++) {
 
                     // Complete the existing sample
                     sample[current++] = packetReceive[i];
 
                     // If the sample is complete it is passed to the display
                     if (current == SAMPLE_SIZE) {
                         try {
                             AccelGyroSample s = parseSample(
                                     System.currentTimeMillis(), sample);
                             this.notifyObservers(s);
                             this.setChanged();
                         } catch (IllegalArgumentException e) {
                             System.err.println("Packet error : " + e);
                         }
                         current = 0;
                     }
                 }
             }
         } catch (IOException ex) {
             System.err.print("BluetoothDeviceCom.run : " + ex);
         }
     }
 
     /**
      * Stop the current thread and close the communication
      */
     public void stop() {
         continueRead = false;
         try {
             os.write(STOP_STREAMING_COMMAND);
             os.close();
             bluetoothConnection.close();
         } catch (IOException ex) {
             System.err.print("BluetoothDeviceCom.stop : " + ex);
         }
     }
 }
