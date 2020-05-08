 package org.aap.nms.driver.server.pingobject;
 
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TimeZone;
 
 import org.doomdark.uuid.UUID;
 import org.valabs.odisp.common.Message;
 import org.valabs.odisp.common.StandartODObject;
 import org.valabs.stdmsg.ODObjectLoadedMessage;
 
 import com.novel.nms.messages.DevPollMessage;
 import com.novel.nms.messages.DevPollReplyMessage;
 import com.novel.nms.messages.DeviceGetCurrentLogMessage;
 import com.novel.nms.messages.DeviceGetCurrentLogReplyMessage;
 import com.novel.nms.messages.DeviceGetCurrentStatusErrorMessage;
 import com.novel.nms.messages.DeviceGetCurrentStatusMessage;
 import com.novel.nms.messages.DeviceGetCurrentStatusReplyMessage;
 import com.novel.nms.messages.LogEventMessage;
 import com.novel.nms.messages.MapAddObjectMessage;
 import com.novel.nms.messages.MapAddObjectNotifyMessage;
 import com.novel.nms.messages.MapDeleteObjectNotifyMessage;
 import com.novel.nms.messages.MapObjectAddedErrorMessage;
 import com.novel.nms.messages.MapObjectAddedMessage;
 import com.novel.nms.server.devices.common.Device;
 import com.novel.nms.server.devices.common.DeviceList;
 import com.novel.nms.server.storage.Storage;
 import com.novel.nms.server.storage.helpers.GetObjectListHelper;
 
 /**
  * Dummy driver for dummy object that support only icmp ping. 
  * 
  * @author <a href="mailto:andrew.porokhin@gmail.com">Andrew Porokhin</a>
  * @version 0.2
  */
 public class Ping extends StandartODObject {
   /** Name of the object, also handler. */
   public static final String NAME = "ping";
 
   /** Version of this component. */
   public static final String VERSION = "0.2";
 
   /** Short module description. */
   public static final String FULLNAME = "NMS Ping Object support";
 
   /** Short copyright string. */
   public static final String COPYRIGHT = "(c) Andrew Porokhin";
 
   /** Global device list resource. */
   public DeviceList deviceList;
 
   /** New objects. */
   private List newObjects = new ArrayList();
   private Pinger pinger = new Pinger();
   
   /**
    * Just default constructor.
    */
   public Ping() {
       super(NAME, FULLNAME, VERSION, COPYRIGHT);
   }
 
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.StandartODObject#handleMessage(org.valabs.odisp.common.Message)
    */
   public final void handleMessage(final Message msg) {
       if (ODObjectLoadedMessage.equals(msg)) {
         deviceList = ((DeviceList) dispatcher.getResourceManager()
             .resourceAcquire(DeviceList.class.getName()));
         loadDeviceList();
         pinger.start();
       } else if (DevPollMessage.equals(msg)) {
         pinger.addRequest(msg);
       } else if (MapAddObjectMessage.equals(msg)) {
           logger.fine("Adding new object " + MapAddObjectMessage.getName(msg));
           Message toMap = dispatcher.getNewMessage();
           MapAddObjectMessage.setup(toMap, "map", getObjectName(), msg.getId());
           MapAddObjectMessage.copyFrom(toMap, msg);
           toMap.addField("security", msg.getField("security"));
           dispatcher.send(toMap);
           newObjects.addAll(msg.getEnvelope());
       } else if (MapObjectAddedMessage.equals(msg)) {
           List localMessages;
           synchronized (newObjects) {
               localMessages = new ArrayList(newObjects);
               newObjects.clear();
           }
           List objData = (List) msg.getField("0");
           Device newDevice = new Device((String) objData.get(0));
           newDevice.setDriver(getObjectName());
           Iterator it = ((List) objData.get(1)).iterator();
           while (it.hasNext()) {
               String urn = (String) it.next();
               newDevice.addURN(urn);
           }
           deviceList.addDevice(newDevice);
           
           dispatcher.send(localMessages);
           Message notifyMsg = dispatcher.getNewMessage();
           MapAddObjectNotifyMessage.setup(notifyMsg, Message.RECIPIENT_ALL,
                   getObjectName(), msg.getId());
           MapAddObjectNotifyMessage.setName(notifyMsg, (String) objData.get(0));
           MapAddObjectNotifyMessage.setURN(notifyMsg, (List) objData.get(1));
           MapAddObjectNotifyMessage.setHandler(notifyMsg, (String) objData.get(2));
           dispatcher.send(notifyMsg);
       } else if (MapObjectAddedErrorMessage.equals(msg)) {
           msg.setDestination(NAME + "-gui");
           msg.setRoutable(true);
           dispatcher.send(msg);
       } else if (DeviceGetCurrentLogMessage.equals(msg)) {
           final String name = DeviceGetCurrentLogMessage.getName(msg);
           final Device dev = deviceList.getDeviceByName(name);
           if (dev != null) {
               Message m = dispatcher.getNewMessage();
               DeviceGetCurrentLogReplyMessage.setup(m, msg.getOrigin(),
                       getObjectName(), msg.getId());
               DeviceGetCurrentLogReplyMessage.setName(m, name);
               DeviceGetCurrentLogReplyMessage.setCurrentLog(m, dev.getCurrentLog());
               dispatcher.send(m);
           }
       } else if (DeviceGetCurrentStatusMessage.equals(msg)) {
           Message m = dispatcher.getNewMessage();
           Device dev = deviceList.getDeviceByName(DeviceGetCurrentStatusMessage.getDeviceName(msg));
           if (dev != null) {
               DeviceGetCurrentStatusReplyMessage.setup(m, msg.getOrigin(),
                       getObjectName(), msg.getId());
               DeviceGetCurrentStatusReplyMessage.setDeviceName(m,
                       DeviceGetCurrentStatusMessage.getDeviceName(msg));
               DeviceGetCurrentStatusReplyMessage.setStatus(m, new Long(dev.getStatus()));
           } else {
               DeviceGetCurrentStatusErrorMessage.setup(m, msg.getOrigin(), getObjectName(), msg.getId());
               DeviceGetCurrentStatusErrorMessage.initAll(m,
                       DeviceGetCurrentStatusMessage.getDeviceName(msg),
               "error.nosuchdevice");
           }
           dispatcher.send(m);
       } else if (MapDeleteObjectNotifyMessage.equals(msg)) {
           deviceList.removeDeviceByName(MapDeleteObjectNotifyMessage.getName(msg));
       } else {
           logger.finest("PingObject: I don't understand this message =) ");
       }
   }
 
   /**
    * Preload deviceList to our local list.
    */
   private void loadDeviceList() {
       GetObjectListHelper gohl = new GetObjectListHelper();
       try {
           ((Storage) dispatcher.getResourceManager().resourceAcquire(
                   Storage.class.getName())).executeRequest(gohl);
           List list = gohl.getResult();
           deviceList.clear(getObjectName());
           Iterator it = list.iterator();
           while (it.hasNext()) {
               List objEntry = (List) it.next();
               logger.fine(objEntry.toString());
               List urn = (List) objEntry.get(1);
               String handler = (String) objEntry.get(2);
               Iterator urn_it = urn.iterator();
               if (getObjectName().startsWith(handler)) {
                   Device dev = new Device((String) objEntry.get(0));
                   dev.setDriver(getObjectName());
                   while (urn_it.hasNext()) {
                       String urnS = (String) urn_it.next();
                       try {
                           InetAddress.getByName(urnS).toString();
                           dev.addURN(urnS);
                       } catch (Exception e) {
                           dispatcher.getExceptionHandler().signalException(e);
                       }
                   }
                   deviceList.addDevice(dev);
               }
           }
       } catch (Exception e) {
           dispatcher.getExceptionHandler().signalException(e);
       }
   }
 
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.ODObject#getProviding()
    */
   public final String[] getProviding() {
       String[] result = { NAME };
       return result;
   }
 
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.ODObject#getDepends()
    */
   public final String[] getDepends() {
       String[] result = { "dispatcher",
               Storage.class.getName(),
               DeviceList.class.getName(),
       };
       return result;
   }
 
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.StandartODObject#cleanUp(int)
    */
   public final int cleanUp(final int type) {
     return 0;
   }
   
   /*
    * Worker thread for icmp ping requests. It is used to free ODISP
    * sender threads from hang up.
    */
   private class Pinger extends Thread {
     /** Ping queue. */
     LinkedList<Message> queue = new LinkedList<Message>();
     
     public Pinger() {
      setName("Pinger thread: waiting");
     }
     
     public void run() {
       while (!Thread.interrupted()) {
         Message request = null;
         
         synchronized (queue) {
           if (queue.size() > 0) {
             request = queue.removeFirst();
           }
         }
         
         if (request != null) {
           processMessage(request);
         }
         
         synchronized (queue) {
           if (queue.size() == 0) {
            setName("Pinger thread: waiting");
             try {
               queue.wait(10000);
             } catch (InterruptedException e) {
               e.printStackTrace();
             }
           }
         }
       }
     }
     
     private void processMessage(Message msg) {
       String deviceName = DevPollMessage.getDeviceName(msg);
       Device device = deviceList.getDeviceByName(deviceName);
       String urn = (String) device.getURNs().get(0);
       
      setName("Pinger thread: " + urn);
      
       Message mtogui = dispatcher.getNewMessage();
       DevPollReplyMessage.setup(mtogui, NAME + "-gui", getObjectName(), msg.getId());
       DevPollReplyMessage.setDeviceName(mtogui, deviceName);
       
       Message mtopoll = dispatcher.getNewMessage();
       DevPollReplyMessage.setup(mtopoll, "devpoll", getObjectName(), msg.getId());
       DevPollReplyMessage.setDeviceName(mtopoll, deviceName);
       
       switch (ReachableTest.checkReach(urn)) {
       case ReachableTest.NO_ERROR:
           DevPollReplyMessage.setStatus(mtogui, DevPollReplyMessage.ALARM_OK);
           DevPollReplyMessage.setStatus(mtopoll, DevPollReplyMessage.ALARM_OK);
           break;
       case ReachableTest.UNREACHABLE_ERROR:
       case ReachableTest.UNKNOWN_HOST_ERROR:
       default:
           Message m = dispatcher.getNewMessage();
           LogEventMessage.setup(m, "nmslog", getObjectName(), UUID.getNullUUID());
           LogEventMessage.setName(m, deviceName);
           LogEventMessage.setSource(m, deviceName);
           Calendar c = Calendar.getInstance(TimeZone.getDefault());
           LogEventMessage.setDate(m, new Long(c.getTimeInMillis() / 1000));
           LogEventMessage.setLDate(m, new Long(c.getTimeInMillis() / 1000));
           LogEventMessage.setIndex(m, Device.POLL_TIMEOUT_EVENT_IDX);
           LogEventMessage.setEvent(m, LogEventMessage.ALARM_POLL_TIMEOUT);
           LogEventMessage.setObject(m, "");
           LogEventMessage.setPC(m, "Poll timeout");
           LogEventMessage.setAI(m, "");
           LogEventMessage.setEquipStatus(m, LogEventMessage.ALARM_POLL_TIMEOUT);
           device.addToCurrentLog(m);
           m.setRoutable(false);
           dispatcher.send(m);
           
           DevPollReplyMessage.setStatus(mtogui, DevPollReplyMessage.ALARM_POLL_TIMEOUT);
           DevPollReplyMessage.setStatus(mtopoll, DevPollReplyMessage.ALARM_POLL_TIMEOUT);
       }
       
      // setting up current device status
      device.setStatus(DevPollReplyMessage.getStatus(mtopoll));
      
       // multiple sending to Server and UI
       dispatcher.send(mtogui);
       dispatcher.send(mtopoll);
     }
     
     public void addRequest(Message request) {
       synchronized (queue) {
         queue.add(request);
         queue.notifyAll();
       }
     }
   }
 
 }
