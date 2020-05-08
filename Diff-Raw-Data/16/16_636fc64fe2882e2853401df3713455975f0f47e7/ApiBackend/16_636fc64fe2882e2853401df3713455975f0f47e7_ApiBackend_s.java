 package com.sgzmd.examples.cloudendpoints;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.annotation.Nullable;
 import javax.inject.Named;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 
 import com.google.api.server.spi.config.Api;
 import com.google.api.server.spi.config.ApiMethod;
 import com.google.api.server.spi.response.NotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.labs.repackaged.com.google.common.annotations.VisibleForTesting;
 import com.google.appengine.labs.repackaged.com.google.common.base.Predicate;
 import com.google.appengine.labs.repackaged.com.google.common.base.Throwables;
 import com.google.appengine.labs.repackaged.com.google.common.collect.Iterables;
 import com.sgzmd.examples.utils.Clock;
 import com.sgzmd.examples.utils.SystemClock;
 
 /**
  * API entry point.
  * 
  * Local API explorer: {@link http://localhost:8888/_ah/api/explorer}
  * 
  * @author me@romankirillov.info [Roman "sgzmd" Kirillov"]
  */
 @Api(name = "monitoring", version = "v1")
 public class ApiBackend {
   private static final Logger logger = Logger.getLogger(ApiBackend.class.getSimpleName());
   private final Clock clock;
 
   public ApiBackend() {
     this.clock = new SystemClock();
   }
   
   public ApiBackend(Clock clock) {
     this.clock = clock;
   }
   
   /**
    * Adds a {@link Room} to collection.
    * 
    * @param room New {@link Room} to be added. Must not exist.
    * 
    * @return an instance of {@link Room} added, with Key assigned.
    */
   @ApiMethod(name = "addRoom", httpMethod = "POST", path = "rooms")
   public Room addRoom(Room room) {
     log("addRoom({0})", room);
     Room created = getPM().makePersistent(room);
     log("created: {0}", created);
     return created;
   }
 
   /**
    * Updates a room with specified ID.
    * 
    * @param roomId {@link Room} ID to update
    * @param updatedRoom Updated {@link Room} contents.
    * 
    * @return updated {@link Room}.
    */
   @ApiMethod(name = "updateRoom", httpMethod = "PUT", path = "rooms/{room}")
   public Room updateRoom(@Named("room") Long roomId, Room updatedRoom) {
     log("updateRoom({0}, {1})", roomId, updatedRoom);
     PersistenceManager pm = getPM();
     try {
       Room room = (Room) pm.getObjectById(Room.class, roomId);
       room.updateFrom(updatedRoom);
       return room;
     } finally {
       pm.close();
     }
   }
 
   /**
    * Deletes {@link Room} from collection.
    * 
    * @param roomId {@link Room} ID to be deleted.
    */
   @ApiMethod(name = "deleteRoom", httpMethod = "DELETE", path = "rooms/{room}")
   public void deleteRoom(@Named("room") Long roomId) {
     log("deleteRoom({0})", roomId);
     PersistenceManager pm = getPM();
     Room room = (Room) pm.getObjectById(Room.class, roomId);
     pm.deletePersistent(room);
   }
 
   /**
    * Lists all {@link Room} in collection, along with their linked {@link Sensor}.
    * @return {@link List} of {@link Room}.
    */
   @SuppressWarnings("unchecked")
   @ApiMethod(name = "listRooms", httpMethod = "GET", path = "rooms")
   public List<Room> listRooms() {
     log("list()");
     PersistenceManager pm = getPM();
     Query query = pm.newQuery(Room.class);
     return (List<Room>) pm.newQuery(query).execute();
   }
   
   @ApiMethod(name = "getRoom", httpMethod = "GET", path="rooms/{room}")
   public Room getRoom(@Named("room") Long roomId) {
     log("getRoom({0})", roomId);
     PersistenceManager pm = getPM();
     Room room = null;
 
     try {
       room = (Room) pm.getObjectById(
           Room.class,
           KeyFactory.createKey(Room.class.getSimpleName(), roomId));
     } finally {
       pm.close();
     }
     
     return room;
   }
 
   /**
    * Adds a new {@link Sensor} to an existing {@link Room}.
    * 
    * @param roomId An identifier of a {@link Room} sensor should be added to.
    * @param sensor New {@link Sensor} to be added.
    * 
    * @return Updated {@link Room} with added {@link Sensor}.
    */
   @ApiMethod(name = "addSensor", httpMethod = "POST", path = "rooms/{room}")
   public Room addSensorToRoom(@Named("room") Long roomId, Sensor sensor) {
     PersistenceManager pm = getPM(); 
     
     log("addSensorToRoom({0}, {1})", roomId, sensor);
     
     if (findSensorByNetworkId(sensor.getNetworkId(), pm) != null) {
       throw new IllegalArgumentException("Duplicate sensor network id: " + sensor.getNetworkId());
     }
     
     Room room;
     try {
       room = (Room) pm.getObjectById(
           Room.class,
           KeyFactory.createKey(Room.class.getSimpleName(), roomId));
       room.addSensor(sensor);
     } finally {
       pm.close();
     }
     
     return room;
   }
 
   @ApiMethod(name = "deleteSensor", httpMethod = "DELETE", path = "sensors/{sensor}")
  public void deleteSensor(@Named("room") Long roomId, @Named("sensor") Long sensorId) {
     PersistenceManager pm = getPM();
     log("deleteSensor(roomId={0}, sensorId={1}", roomId, sensorId);
     try {
       Room room = pm.getObjectById(Room.class, roomId);
       room.deleteSensor(sensorId);
     } finally {
       pm.close();
     }
   }
   
   /**
    * Will be called whenever {@link Sensor} fired and needs to be updated. 
    * Sensor's last active will be set to {@link #clock.now()}. 
    * 
    * @param sensorNetworkId {@link Sensor} to update.
    * @throws NotFoundException
    */
   @ApiMethod(name = "logSensorUpdate", httpMethod = "GET", path = "sensors/{network_id}")
   public void sensorUpdated(@Named("network_id") String sensorNetworkId) throws NotFoundException {
     log("sensorUpdated{0}", sensorNetworkId);
     PersistenceManager pm = getPM();
     try {
       Sensor sensor = findSensorByNetworkId(sensorNetworkId, pm);
       if (sensor != null) {
         sensor.setLastActive(clock.now().getMillis());
         log("Sensor updated: {0}", sensor);
       } else {
         log("Sensor with NetworkId = {0} not found", sensorNetworkId);
         throw new NotFoundException("Sensor not found: network_id" + sensorNetworkId);
       }
     } finally {
       pm.close();
     }
   }
   
   /**
    * Disables all sensors in all rooms of the household.
    */
   @ApiMethod(name = "disarm", httpMethod = "GET", path = "disarm")
   public void disarm() {
     log("Disarming all sensors");
     
     resetAllSensors(false);
   }
 
   /**
    * Disables all sensors in all rooms of the household.
    */
   @ApiMethod(name = "arm", httpMethod = "GET", path = "reset")
  public void reset(
       @Nullable @Named("room") Long roomId,
       @Nullable @Named("sensor") Long sensorId,
       @Named("state") Boolean state) {
     if (sensorId != null) {
       resetSensor(sensorId, roomId, state);
     } else if (roomId != null) {
       resetRoom(roomId, state);
     } else {
       resetAllSensors(state);
     }
   }
   
   @VisibleForTesting void resetSensor(final Long sensorId, final Long roomId, final Boolean state) {
     log("resetSensor({0}, {1})", sensorId, state);
     PersistenceManager pm = getPM();
     try {
       Room room = pm.getObjectById(Room.class, roomId);
       List<Sensor> sensors = room.getSensors();
 
       for (Sensor sensor : sensors) {
         if (sensor.getId() == sensorId) {
           log("Setting sensor {0} to active={1}", sensor, state);
           sensor.setActive(state);
           break;
         }
       }
     } finally {
       pm.close();
     }
   }
 
   @VisibleForTesting void resetRoom(Long roomId, Boolean state) {
     log("resetRoom({0}, {1}", roomId, state);
     PersistenceManager pm = getPM();
     Room room;
     try {
       room = (Room) pm.getObjectById(
           Room.class,
           KeyFactory.createKey(Room.class.getSimpleName(), roomId));
       if (room != null) {
         for (Sensor sensor : room.getSensors()) {
           log("Setting sensor {0} to {1}", sensor, state);
           sensor.setActive(state);
         }
       }
     } finally {
       pm.close();
     }
   }
   
   @VisibleForTesting void resetAllSensors(boolean state) {
     PersistenceManager pm = getPM();
     Query query = pm.newQuery(Sensor.class);
     try {
       @SuppressWarnings({"unchecked"})
       List<Sensor> sensors = (List<Sensor>)pm.newQuery(query).execute();
       for (Sensor sensor : sensors) {
         log("Updating sensor {0}", sensor.toString());
         sensor.setActive(state);
       }
     } finally {
       query.closeAll();
       pm.close();
     }
   }
   
   @SuppressWarnings("unchecked")
   private Sensor findSensorByNetworkId(String networkId, PersistenceManager pm) {
     Query query = pm.newQuery(Sensor.class);
     query.setFilter("networkId == networkIdParam");
     query.declareParameters("String networkIdParam");
     
     try {
       return Iterables.getOnlyElement((List<Sensor>)query.execute(networkId), null);
     } finally {
       query.closeAll();
     }
   }
   
   private void log(String format, Object... objects) {
     logger.log(Level.INFO, format, objects);
   }
 
   private static PersistenceManager getPM() {
     return PMF.get().getPersistenceManager();
   }
 }
