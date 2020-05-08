 package prj.pingback.devicemanager;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import prj.pingback.utils.ConcurrencyUtils;
 
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 
 public class DeviceManager implements IDeviceManager
 {
     private Map<String, Map<String, Device>> versionMap;
     private ConcurrencyUtils _concurrencyUtils;
 
     public DeviceManager(ConcurrencyUtils concurrencyUtils)
     {
         _concurrencyUtils = concurrencyUtils;
         versionMap = new HashMap<>();
     }
 
     public void registerDevice(Device device)
     {
         final String relevantVersionNumber = device.getVersionNumber();
         Map<String, Device> relevantMap = versionMap.get(relevantVersionNumber);
         if (relevantMap == null)
         {
             versionMap.put(relevantVersionNumber, (relevantMap = new HashMap<>()));
         }
         if (relevantMap.containsKey(device.getDeviceId()))
         {
             device = relevantMap.get(device.getDeviceId());
             device.resetCancellingFuture();
         }
         else
         {
             relevantMap.put(device.getDeviceId(), device);
         }
         final Device finalDevice = device;
         device.setRegistryTime(new Date(), _concurrencyUtils.scheduleOnSTP(
                 new Runnable()
                 {
                     @Override
                     public void run()
                     {
                         System.out.println("device " + finalDevice + " de-registered at " + new Date());
                         Map<String, Device> set = versionMap.get(relevantVersionNumber);
                         set.remove(finalDevice.getDeviceId());
                         if (set.size() == 0)
                         {
                            versionMap.remove(set);
                         }
                     }
                }, 30, TimeUnit.SECONDS));
     }
 
     @Override
     public String toString()
     {
         JSONObject jsonObject = new JSONObject();
         for (String key : versionMap.keySet())
         {
             try
             {
                 jsonObject.put(key, versionMap.get(key).size());
             }
             catch (JSONException e)
             {
             }
         }
         return jsonObject.toString();
     }
 }
