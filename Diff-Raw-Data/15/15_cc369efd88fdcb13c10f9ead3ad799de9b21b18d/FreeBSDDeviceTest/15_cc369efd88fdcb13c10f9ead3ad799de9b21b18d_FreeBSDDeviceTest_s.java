 package zfs.java;
 
 import java.util.Map;
 import static org.junit.Assert.assertTrue;
 import zfs.java.helper.DeviceDetector;
 import zfs.java.helper.freebsd.FreeBSDDeviceDetector;
 import zfs.java.models.Device;
 import zfs.java.models.Host;
 import org.junit.Test;
 
 /**
  * FreeBSDDeviceTest 19.11.2012
  *
  * @author Philipp Haussleiter
  *
  */
 public class FreeBSDDeviceTest extends TestParent {
 
     private static final String DEVICE_TRANSFER_ADA0 = "300.000";
     private static final String BUS_TRANSFER_ADA1 = "at ahcich1 bus 0 scbus1 target 0 lun 0";
     private static final String DESCRIPTION_TRANSFER_ADA2 = "<WDC WD20EARS-00MVWB0 51.0AB51> ATA-8 SATA 2.x device";
     private static Host HOST = new Host("root", "localhost");
 
     private Map<String, Device> getDeviceMapping() {
         DeviceDetector fd = new FreeBSDDeviceDetector(HOST);
         String file;
         file = "resources/freenas.dmesg.txt";
         parseFile(file, fd);
         file = "resources/freenas.glabel.status.txt";
         parseFile(file, fd);
         return fd.getDevices();
     }
 
     @Test
     public void testDeviceListing() {
         Map<String, Device> devices = getDeviceMapping();
        assertTrue(devices.size() == 4);
        assertTrue(DEVICE_TRANSFER_ADA0.equals(devices.get("ada0").transfer));
        assertTrue(BUS_TRANSFER_ADA1.equals(devices.get("ada1").bus));
        assertTrue(DESCRIPTION_TRANSFER_ADA2.equals(devices.get("ada2").description));
         debug(devices);
     }
 }
