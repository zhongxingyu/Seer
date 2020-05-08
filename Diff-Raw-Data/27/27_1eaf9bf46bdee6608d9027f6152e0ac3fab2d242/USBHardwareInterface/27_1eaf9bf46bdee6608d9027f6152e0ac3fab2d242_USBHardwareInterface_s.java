 package de.newsarea.homecockpit.connector.usb.hardware;
 
 import com.codeminders.hidapi.ClassPathLibraryLoader;
 import com.codeminders.hidapi.HIDDevice;
 import com.codeminders.hidapi.HIDDeviceInfo;
 import com.codeminders.hidapi.HIDManager;
 import de.newsarea.homecockpit.connector.event.ValueEventListener;
 import org.apache.commons.codec.DecoderException;
 import org.apache.commons.codec.binary.Hex;
 import org.apache.commons.lang3.event.EventListenerSupport;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.util.Arrays;
 
 public class USBHardwareInterface {
 
     private static Logger log = LoggerFactory.getLogger(USBHardwareInterface.class);
 
     private HIDManager hidManager;
     private HIDDevice hidDevice;
     private int vendorId;
     private int productId;
     private HIDDeviceReaderThread hidDeviceReaderThread;
     private EventListenerSupport<ValueEventListener> valueEventListeners;
 
     public USBHardwareInterface(int vendorId, int productId) {
         this.vendorId = vendorId;
         this.productId = productId;
         //
         ClassPathLibraryLoader.loadNativeHIDLibrary();
     }
 
     public void open() throws ConnectException {
         if(hidManager != null) {
             throw new ConnectException("connection is open");
         }
         initHIDManager();
         initHIDDevice(vendorId, productId);
         initHIDDeviceReaderThread(hidDevice);
     }
 
     private void initHIDManager() throws ConnectException {
         try {
             hidManager = HIDManager.getInstance();
         } catch (IOException e) {
             log.error(e.getMessage(), e);
             throw new ConnectException(e.getMessage());
         }
     }
 
     private void initHIDDevice(int vendorId, int productId) throws ConnectException {
         try {
             hidDevice = HIDManager.getInstance().openById(vendorId, productId, null);
             log.info("Manufacturer: " + hidDevice.getManufacturerString());
             log.info("Product: " + hidDevice.getProductString());
         } catch (IOException e) {
             log.error(e.getMessage(), e);
             throw new ConnectException(e.getMessage());
         }
     }
 
     private void initHIDDeviceReaderThread(HIDDevice hidDevice) {
         hidDeviceReaderThread = new HIDDeviceReaderThread(hidDevice);
         hidDeviceReaderThread.addEventListener(new ValueEventListener() {
             @Override
             public void valueReceived(String value) {
                 valueEventListeners.fire().valueReceived(value);
             }
         });
         hidDeviceReaderThread.start();
     }
 
     public void send(String value) throws IOException {
         try {
             byte[] data = Hex.decodeHex(value.toCharArray());
             hidDevice.write(data);
         } catch (DecoderException e) {
             throw new IllegalArgumentException(e.getMessage());
         }
     }
 
     public void close() {
         if(hidDeviceReaderThread != null) {
             hidDeviceReaderThread.exit();
         }
         if(hidDevice != null) {
             try {
                 hidDevice.close();
                 log.debug("hidDevice closed");
             } catch (IOException e) {
                 log.error(e.getMessage(), e);
             }
         }
         if(hidDeviceReaderThread != null) {
             try {
                 hidDeviceReaderThread.join();
                 log.debug("hidDeviceReaderThread joined");
             } catch (InterruptedException e) {
                 log.error(e.getMessage(), e);
             }
         }
         if(hidManager != null) {
             hidManager.release();
             log.debug("hidManager released");
         }
     }
 
     public void addEventListener(ValueEventListener valueEventListener) {
        if(valueEventListeners == null) {
            valueEventListeners = EventListenerSupport.create(ValueEventListener.class);
        }
         valueEventListeners.addListener(valueEventListener);
     }
 
     /* HELPER */
 
     private static final class HIDDeviceReaderThread extends Thread {
 
         private static final int BUFFER_SIZE = 2048;
 
         private HIDDevice hidDevice;
         private EventListenerSupport<ValueEventListener> valueEventListeners;
         private boolean exit = false;
 
         private HIDDeviceReaderThread(HIDDevice hidDevice) {
             this.hidDevice = hidDevice;
         }
 
         public void run() {
             log.debug("starting usb reader thread");
             byte[] buffer = new byte[BUFFER_SIZE];
             while(!exit) {
                 int n;
                 try {
                     n = hidDevice.read(buffer);
                     String hexString = toHexString(buffer, n);
                     valueEventListeners.fire().valueReceived(hexString);
                 } catch (IOException e) {
                     if(exit) { return; } // io exception on exit is a normal behavior - no logging
                     log.error(e.getMessage(), e);
                 } catch (Exception e) {
                     log.error(e.getMessage(), e);
                 }
 
             }
         }
 
         public void exit() {
             log.debug("stopping usb reader thread");
             exit = true;
         }
 
         public void addEventListener(ValueEventListener valueEventListener) {
            if(valueEventListeners == null) {
                valueEventListeners = EventListenerSupport.create(ValueEventListener.class);
            }
             valueEventListeners.addListener(valueEventListener);
         }
 
         /* HELPER */
 
         private static String toHexString(byte[] buffer, int n) {
             byte[] data = Arrays.copyOf(buffer, n);
             return new String(Hex.encodeHex(data));
         }
 
     }
 
     public static void printDeviceList() throws IOException {
         ClassPathLibraryLoader.loadNativeHIDLibrary();
         HIDManager hidManager = HIDManager.getInstance();
         try {
             HIDDeviceInfo[] devs = hidManager.listDevices();
             log.info("Devices:");
             for (int i = 0; i < devs.length; i++) {
                 log.info("{}.\t{}", i, devs[i]);
                 log.info("---------------------------------------------");
             }
             hidManager.release();
         } catch (IOException e) {
             log.error(e.getMessage(), e);
         }
     }
 
 }
