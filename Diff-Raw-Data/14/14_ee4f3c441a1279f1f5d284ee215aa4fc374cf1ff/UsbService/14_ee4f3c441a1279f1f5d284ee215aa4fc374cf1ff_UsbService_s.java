 package pro.kornev.kcar.cop.services.usb;
 
 import android.content.Context;
 import android.hardware.usb.UsbDevice;
 import android.hardware.usb.UsbManager;
 
 import com.hoho.android.usbserial.driver.UsbSerialDriver;
 import com.hoho.android.usbserial.driver.UsbSerialProber;
 import com.hoho.android.usbserial.util.SerialInputOutputManager;
 
 import java.util.Arrays;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import pro.kornev.kcar.cop.Utils;
 import pro.kornev.kcar.cop.providers.LogsDB;
 import pro.kornev.kcar.cop.services.CopService;
 import pro.kornev.kcar.cop.services.network.NetworkListener;
 import pro.kornev.kcar.protocol.Data;
 import pro.kornev.kcar.protocol.Protocol;
 
 /**
  * @author vkornev
  * @since 14.10.13
  */
 public final class UsbService implements NetworkListener, SerialInputOutputManager.Listener {
     private static final int DRIVER_SCAN_TIMEOUT = 1000;
     private static final int WRITE_TIMEOUT = 1000;
     private static final int USB_VENDOR_ID = 0x1d50;
     private static final int USB_PRODUCT_ID = 0x602f;
     private final LogsDB log;
     private volatile SerialInputOutputManager mSerialIoManager;
     private volatile UsbSerialDriver driver;
     private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
     private CopService copService;
     private UsbPermissionReceiver usbPermissionReceiver;
 
     public UsbService(CopService copService) {
         this.copService = copService;
         log = new LogsDB(this.copService);
         usbPermissionReceiver = new UsbPermissionReceiver(this.copService);
     }
 
     public void start() {
         driver = getDriver();
         if (driver == null) {
             log.putLog("US Failed get USB driver");
             return;
         }
        startIoManager(driver);
         log.putLog("US Is running");
     }
 
     public void stop() {
         stopIoManager();
     }
 
     @Override
     public void onRunError(Exception e) {
         log.putLog("US Run error: " + e.getMessage());
         Utils.sleep(DRIVER_SCAN_TIMEOUT);
         start();
     }
 
     @Override
     public void onNewData(final byte[] buf) {
         Data data = Protocol.fromByteArray(buf, buf.length);
         log.putLog("US Receive data: cmd=" + data.cmd);
         sendToNetwork(data);
     }
 
     private void stopIoManager() {
         try {
             if (mSerialIoManager != null) {
                 log.putLog("US Stopping io manager ..");
                 mSerialIoManager.stop();
                 mSerialIoManager = null;
             }
             if (driver != null) {
                 driver.close();
             }
         } catch (Exception ignored) {}
     }
 
     private void startIoManager(UsbSerialDriver driver) {
         try {
             if (driver != null) {
                 driver.open();
                 log.putLog("US Starting io manager ..");
                 mSerialIoManager = new SerialInputOutputManager(driver, this);
                 mExecutor.submit(mSerialIoManager);
             }
         } catch (Exception e) {
             log.putLog("US Start IO manager is failed: " + e.getMessage());
         }
     }
 
     @Override
     public void onDataReceived(Data data) {
         if ((data.cmd == Protocol.Cmd.ping() && data.bData == 0)
                 || (data.cmd >= Protocol.Cmd.autoFirst() && data.cmd <= Protocol.Cmd.autoLast())) {
             write(data);
         }
     }
 
     private UsbSerialDriver getDriver() {
         try {
             UsbManager usbManager = (UsbManager) copService.getSystemService(Context.USB_SERVICE);
             if (usbManager.getDeviceList() == null || usbManager.getDeviceList().isEmpty()) {
                 return null;
             }
             UsbDevice usbDevice = null;
             for (UsbDevice device: usbManager.getDeviceList().values()) {
                 if (device.getVendorId() == USB_VENDOR_ID && device.getProductId() == USB_PRODUCT_ID) {
                     usbDevice = device;
                     break;
                 }
             }
             if (usbDevice == null) {
                 return null;
             }
             if (!usbManager.hasPermission(usbDevice)) {
                 usbManager.requestPermission(usbDevice, usbPermissionReceiver.getPermissionIntent());
                 if (!usbManager.hasPermission(usbDevice)) {
                     return null;
                 }
             }
            return UsbSerialProber.probeSingleDevice(usbManager, usbDevice).get(0);
         } catch (Exception e) {
             log.putLog("US Failed get driver: " + e.getMessage());
             return null;
         }
     }
 
     private void sendToNetwork(Data data) {
         copService.getNetworkService().write(data);
     }
 
     private void write(Data data) {
         try {
            if (driver == null) return;
             byte[] buf = new byte[Protocol.getMaxLength()];
             log.putLog("UW Write data cmd: " + data.cmd);
             int bLen = Protocol.toByteArray(data, buf);
             driver.write(Arrays.copyOf(buf, bLen), WRITE_TIMEOUT);
         } catch (Exception e) {
             log.putLog("UW Failed write data: " + e.getMessage());
         }
 
     }
 }
