 /**
  * FreeBSDDeviceDetector 20.10.2012
  *
  * @author Philipp Haussleiter
  *
  */
 package zfs.java.helper.freebsd;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import zfs.java.helper.DeviceDetector;
 import zfs.java.helper.common.CommonDeviceDetector;
 import zfs.java.models.Device;
 import zfs.java.models.Host;
 import zfs.java.models.Pair;
 import zfs.java.models.Partition;
 
 public class FreeBSDDeviceDetector extends CommonDeviceDetector implements DeviceDetector {
 
     private static final Logger LOG = Logger.getLogger(FreeBSDDeviceDetector.class.getName());
     private static final int GPT_ARRAY_SIZE = 3;
     private boolean gptidMode = false;
 
     public FreeBSDDeviceDetector(Host host) {
         super(host);
     }
 
     @Override
     public void parse(BufferedReader reader) {
         String line;
         try {
             while ((line = reader.readLine()) != null) {
                 line = line.trim();
                 if (line.startsWith(FreeBSDKeys.IDE_HDD)) {
                     handleIDEHDD(line);
                 } else if (line.startsWith(FreeBSDKeys.SCSI_HDD)) {
                     handleSCSIHDD(line);
                     //} else if (line.startsWith(IDE_CDROM)) {
                     // NOP
                     //} else if (line.startsWith(SCSI_CDROM)) {
                     // NOP
                 } else if (line.startsWith(FreeBSDKeys.GPTID_HEADER)) {
                     gptidMode = true;
                 } else if (gptidMode) {
                     handleGptId(line);
                 }
             }
         } catch (IOException ex) {
             LOG.log(Level.SEVERE, ex.getLocalizedMessage());
         }
     }
 
     private void handleIDEHDD(String line) {
         setParts(line);
         String name = parts[0].trim();
         //Logger.debug("adding IDE HDD " + name);
         if (devices.containsKey(name)) {
             addToDevice(devices.get(name), line);
         } else {
             createDevice(name, line, Device.IDE_HDD);
         }
     }
 
     private void handleSCSIHDD(String line) {
         setParts(line);
         String name = parts[0].trim();
         //Logger.debug("adding SCSI HDD " + name);
         if (devices.containsKey(name)) {
             addToDevice(devices.get(name), line);
         } else {
             createDevice(name, line, Device.SCSI_HDD);
         }
     }
 
     private void addToDevice(Device device, String line) {
         if (device != null) {
             infoPair = getInfo(device.name, line);
             device = addInfoToDevice(device, infoPair);
             devices.put(device.name, device);
         }
     }
 
     private void createDevice(String deviceName, String line, int type) {
         gptidMode = false;
         //Logger.debug("creating device " + deviceName);
         Device device = new Device(deviceName, type);
         device.host = host;
         infoPair = getInfo(deviceName, line);
         if (infoPair != null) {
             device = addInfoToDevice(device, infoPair);
         }
         devices.put(deviceName, device);
     }
 
     private Device addInfoToDevice(Device device, Pair<Integer, String> pair) {
         if (pair != null) {
             //Logger.debug("addInfo: " + pair.getKey() + " = " + pair.getValue() + "\n");
             switch (pair.getKey().intValue()) {
                 case FreeBSDKeys.DEVICE_TRANSFER:
                     device.transfer = pair.getValue();
                     return device;
                 case FreeBSDKeys.DEVICE_DESCRIPTION:
                     device.description = pair.getValue();
                     return device;
                 case FreeBSDKeys.DEVICE_SIZE:
                     long size = 0L;
                     if (pair.getValue() != null) {
                         size = Long.parseLong(pair.getValue());
                     }
                     device.size = size;
                     return device;
                 case FreeBSDKeys.DEVICE_BUS:
                     device.bus = pair.getValue();
                     return device;
                 default:
                 /* NOP */
             }
         }
         return device;
     }
 
     private Pair<Integer, String> getInfo(String deviceName, String line) {
         String data = line.replace(deviceName + ":", "");
         data = data.replace(deviceName, "").trim();
         if (data.indexOf(FreeBSDKeys.DEVICE_TRANSFER_MARKER) != -1) {
             parts = data.split(FreeBSDKeys.DEVICE_TRANSFER_MARKER);
             return new Pair(FreeBSDKeys.DEVICE_TRANSFER, parts[0]);
         }
         if (data.indexOf(FreeBSDKeys.DEVICE_SIZE_MARKER) != -1) {
             parts = data.split(FreeBSDKeys.DEVICE_SIZE_DEVIDER);
             return new Pair(FreeBSDKeys.DEVICE_SIZE, parts[0]);
         }
        if (data.startsWith(FreeBSDKeys.DEVICE_DESCRIPTION_MARKER)) {
             return new Pair(FreeBSDKeys.DEVICE_DESCRIPTION, data);
         }
         if (data.startsWith(FreeBSDKeys.DEVICE_BUS_MARKER)) {
             return new Pair(FreeBSDKeys.DEVICE_BUS, data);
         }
         return null;
     }
 
     private void setParts(String line) {
         parts = line.split(":");
         if (parts.length < 2) {
             parts = line.split(" ");
         }
     }
 
     private void handleGptId(String line) {
         Device device;
         parts = cleanSplit(line, " ");
         if (parts.length == GPT_ARRAY_SIZE) {
             for (String key : devices.keySet()) {
                 if (parts[2].startsWith(key)) {
                     device = devices.get(key);
                     device.addPartition(new Partition(parts[2], parts[0]));
                 }
             }
         }
     }
 }
