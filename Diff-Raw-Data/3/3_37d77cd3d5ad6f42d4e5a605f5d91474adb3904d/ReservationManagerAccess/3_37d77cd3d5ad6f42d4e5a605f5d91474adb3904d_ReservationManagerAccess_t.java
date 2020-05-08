 /*
  * Master-Thesis work: see https://sites.google.com/site/sifthesis/
  */
 package managers;
 
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import tangible.devices.TangibleDevice;
 
 /**
  *
  * @author leo
  */
 public enum ReservationManagerAccess {
 
     INSTANCE;
     private ReservationManager _singleton;
 
     private ReservationManagerAccess() {
         _singleton = new ReservationManagerImpl();
     }
 
     public static ReservationManager getInstance() {
         return INSTANCE._singleton;
     }
 
     private static class ReservationManagerImpl
             implements ReservationManager {
 
         private final Map<UUID, Set<TangibleDevice>> _reservations;
         private final Map<String, TangibleDevice> _busyDevices;
         private DeviceFinder _devFinder = DeviceFinderAccess.getInstance();
 
         private ReservationManagerImpl() {
             //private constructor
             _reservations = new HashMap<UUID, Set<TangibleDevice>>();
             _busyDevices = new HashMap<String, TangibleDevice>();
         }
 
         @Override
         public synchronized String reserveDeviceById(String device_id, UUID app_id)
                 throws UnsuccessfulReservationException {
             Logger.getLogger(ReservationManagerImpl.class.getName()).log(Level.INFO, "trying to reserve the device: {0}", device_id);
             if (!isDeviceAvailable(device_id) || app_id == null) {
                 throw new UnsuccessfulReservationException();
             }//else the device is available
             TangibleDevice dev = _devFinder.getDevice(device_id);
             addNewReservation(dev, app_id);
             _busyDevices.put(device_id, dev);
             //<FOR DEBUG>
 //      System.out.println("Reservation made!!!");
             dev.getTalk().showColor(0x00DA55);
             //</FOR DEBUG>
             return device_id;
         }
 
         private void addNewReservation(TangibleDevice dev, UUID app_id) {
             synchronized (_reservations) {
                 if (!_reservations.containsKey(app_id)) {
                     //create the set
                     _reservations.put(app_id, new HashSet<TangibleDevice>());
                 }
                 //get the good set and add the new reserved device
                 _reservations.get(app_id).add(dev);
             }
         }
 
         @Override
         public synchronized String reserveDeviceByType(String type, UUID app_id)
                 throws UnsuccessfulReservationException {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public Set<String> reservedByAnApp(UUID app_id)
                 throws UnsuccessfulReservationException {
             Set<String> devIds = new HashSet<String>();
             if (app_id == null) {
                 throw new UnsuccessfulReservationException();
             }
             for (TangibleDevice dev : _reservations.get(app_id)) {
                 devIds.add(dev.getId());
             }
             return devIds;
         }
 
         private boolean isDeviceAvailable(String id) {
             return _devFinder.existsDevice(id) && !_busyDevices.containsKey(id);
         }
 
         @Override
         public synchronized void endReservation(String device_id, UUID app_id) {
             if (!_busyDevices.containsKey(device_id) || //if the device is not busy
                     _reservations.get(app_id) == null || //or if this application has no reservation
                     !_reservations.get(app_id).contains(_busyDevices.get(device_id))) { //or if this device is not reserved by this application
                 throw new NoSuchReservationException();
             }
             //otherwise let's remove that from both the busy list and the reservation map!
             _reservations.get(app_id).remove(_busyDevices.remove(device_id));
            
             _devFinder.getDevice(device_id).getTalk().showColor(0xDA0000);
         }
 
         @Override
         public boolean isAReservation(String devID, UUID appUUID) {
             return _reservations.containsKey(appUUID)
                     && _reservations.get(appUUID).contains(_devFinder.getDevice(devID));
         }
     }
 }
