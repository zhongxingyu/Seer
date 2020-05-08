 package zfs.java;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 import static org.junit.Assert.assertTrue;
 import org.junit.Test;
 import zfs.java.helper.DeviceDetector;
 import zfs.java.helper.ZPOOLDetector;
 import zfs.java.helper.common.CommonZPOOLDetector;
 import zfs.java.helper.freebsd.FreeBSDDeviceDetector;
 import zfs.java.models.Device;
 import zfs.java.models.Host;
 import zfs.java.models.Pool;
 import zfs.java.models.ZPOOL;
 
 /**
  * FreeBSDPoolTest 20.10.2012
  *
  * @author Philipp Haussleiter
  *
  */
 public class FreeBSDPoolTest extends TestParent {
 
     private static final Logger LOG = Logger.getLogger(FreeBSDPoolTest.class.getName());
     private static final Host HOST = new Host("root", "localhost");
     private static final String TANK = "tank";
     private static final String TANK_2 = "tank2";
     private static final String TANK_3 = "backup";
 
     private Map<String, Device> getDeviceMapping(String glabelFile) {
         DeviceDetector fd = new FreeBSDDeviceDetector(HOST);
         String file;
         file = "resources/freenas.dmesg.txt";
         parseFile(file, fd);
         file = "resources/" + glabelFile;
         parseFile(file, fd);
         return fd.getDevices();
     }
 
     private Map<String, Device> getDeviceMapping(String dmesg, String status) {
         DeviceDetector fd = new FreeBSDDeviceDetector(HOST);
         parseFile(dmesg, fd);
         parseFile(status, fd);
         return fd.getDevices();
     }
 
     private Set<Device> getDectectedDevices(String dmesg, String status) {
         Map<String, Device> devicesMap = getDeviceMapping(dmesg, status);
         Set<Device> devices = new TreeSet<Device>();
         devices.addAll(devicesMap.values());
         return devices;
     }
 
     @Test
     public void testZPoolStatus1() {
         HOST.setDevices(getDectectedDevices("resources/freenas.dmesg.txt", "resources/freenas.glabel.status.txt"));
         ZPOOLDetector fd = new CommonZPOOLDetector(HOST);
         String file = "resources/freenas.zpool.status1.txt";
         parseFile(file, fd);
         ZPOOL zPool = (ZPOOL) first(fd.getPools());
         debug(zPool, "zPool");
         assertTrue(zPool != null);
         assertTrue(TANK.equals(zPool.name));
         assertTrue(zPool.pools.size() == 1);
         Pool subPool = (Pool) first(zPool.pools);
         debug(zPool.pools, "zPool.pools");
         assertTrue(subPool != null);
         assertTrue(subPool.type.equals(Pool.Type.MIRROR));
         assertTrue(subPool.devices.size() == 2);
     }
 
     @Test
     public void testZPoolStatus2() {
         HOST.setDevices(getDectectedDevices("resources/freenas.dmesg.txt", "resources/freenas.glabel.status.txt"));
         ZPOOLDetector fd = new CommonZPOOLDetector(HOST);
         String file = "resources/freenas.zpool.status2.txt";
         parseFile(file, fd);
         ZPOOL zPool = (ZPOOL) first(fd.getPools());
         debug(zPool, "zPool");
         assertTrue(zPool != null);
         assertTrue(TANK_2.equals(zPool.name));
         assertTrue(zPool.pools.size() == 1);
         Pool subPool = (Pool) first(zPool.pools);
         debug(zPool.pools, "zPool.pools");
         assertTrue(subPool != null);
         assertTrue(subPool.type.equals(Pool.Type.RAIDZ1));
         assertTrue(subPool.devices.size() == 3);
     }
 
     @Test
     public void testZPoolStatus3() {
         HOST.setDevices(getDectectedDevices("resources/freenas.dmesg.txt", "resources/freenas.glabel.status.txt"));
         ZPOOLDetector fd = new CommonZPOOLDetector(HOST);
         String file = "resources/freenas.zpool.status3.txt";
         parseFile(file, fd);
        ZPOOL zPool = (ZPOOL) get(0, fd.getPools());
        checkPool(zPool, TANK_3, Pool.Type.STRIPED, 1, 1);
        zPool = (ZPOOL) get(1, fd.getPools());
         checkPool(zPool, TANK, Pool.Type.RAIDZ1, 1, 3);
     }
 
     @Test
     public void testZPoolStatus4() {
         HOST.setDevices(getDectectedDevices("resources/freenas.dmesg.txt", "resources/freenas.glabel.status.txt"));
         ZPOOLDetector fd = new CommonZPOOLDetector(HOST);
         String file = "resources/freenas.zpool.status4.txt";
         parseFile(file, fd);
         ZPOOL zPool = (ZPOOL) first(fd.getPools());
         debug(zPool, "zPool");
         assertTrue(zPool != null);
         assertTrue(TANK.equals(zPool.name));
         assertTrue(zPool.pools.size() == 1);
         Pool subPool = (Pool) first(zPool.pools);
         debug(zPool.pools, "freenas.zpool.status4.txt: zPool.pools");
         assertTrue(subPool != null);
         assertTrue(subPool.type.equals(Pool.Type.RAIDZ1));
         assertTrue("should be 3, but were " + subPool.devices.size(), subPool.devices.size() == 3);
     }
     /*
      * FIXME: This Test also fails:
      */
 //    @Test
     public void testZPoolStatusWithCache() {
         HOST.setDevices(getDectectedDevices("resources/freenas.dmesg2.txt", "resources/freenas.glabel.status2.txt"));
         ZPOOLDetector fd = new CommonZPOOLDetector(HOST);
         String file = "resources/freenas.zpool.status6.txt";
         parseFile(file, fd);
         ZPOOL zPool = (ZPOOL) first(fd.getPools());
         debug(zPool, "zPool");
         assertTrue(zPool != null);
         assertTrue(TANK.equals(zPool.name));
         assertTrue(zPool.pools.size() == 1);
         Pool subPool = (Pool) first(zPool.pools);
         debug(zPool.pools, "freenas.zpool.status6.txt: zPool.pools");
         assertTrue(subPool != null);
         assertTrue(subPool.type.equals(Pool.Type.RAIDZ1));
         assertTrue("Devices should be 4, but were " + subPool.devices.size(), subPool.devices.size() == 4);
         assertTrue("Spares should be 1, but were " + subPool.spares.size(), subPool.spares.size() == 1);
         assertTrue("Caches should be 3, but were " + subPool.caches.size(), subPool.caches.size() == 1);
     }
 
     /*
      * FIXME: This Status fails:
      * raidz1-0                                        ONLINE       0     0     0
      * spare-0                                       ONLINE       0     0     0
      */
 //    @Test
     public void testZPoolStatus5() {
         HOST.setDevices(getDectectedDevices("resources/freenas.dmesg.txt", "resources/freenas.glabel.status.txt"));
         ZPOOLDetector fd = new CommonZPOOLDetector(HOST);
         String file = "resources/freenas.zpool.status5.txt";
         parseFile(file, fd);
         ZPOOL zPool = (ZPOOL) first(fd.getPools());
         assertTrue(zPool != null);
         assertTrue(TANK.equals(zPool.name));
         assertTrue(zPool.pools.size() == 1);
         Pool subPool = (Pool) first(zPool.pools);
         debug(zPool.pools, "freenas.zpool.status5.txt: zPool.pools");
         assertTrue(subPool != null);
         assertTrue(subPool.type.equals(Pool.Type.RAIDZ1));
         assertTrue("should be 3, but were " + subPool.devices.size(), subPool.devices.size() == 3);
     }
 
     private void checkPool(ZPOOL zPool, String poolName, Pool.Type poolType, int poolSize, int devicesSize) {
         debug(zPool, poolName + ": zPool");
         assertTrue(zPool != null);
         assertTrue(poolName + ": Pool Name should be " + poolName + ", but was " + zPool.name, poolName.equals(zPool.name));
         assertTrue(poolName + ": Pool size should be " + poolSize + ", but was " + zPool.pools.size(), zPool.pools.size() == poolSize);
         debugJson(zPool, zPool.name);
         Pool subPool = (Pool) first(zPool.pools);
         debug(zPool.pools, poolName + ": zPool.pools");
 
         assertTrue(subPool != null);
         assertTrue(subPool.type.equals(poolType));
         assertTrue(poolName + ": subPool size should be " + devicesSize + ", but was " + subPool.devices.size(), subPool.devices.size() == devicesSize);
     }
 }
