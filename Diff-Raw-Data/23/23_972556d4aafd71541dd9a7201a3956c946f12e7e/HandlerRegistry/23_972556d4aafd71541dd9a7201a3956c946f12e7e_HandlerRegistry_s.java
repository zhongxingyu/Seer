 package jk_5.nailed.crashreporter;
 
 import com.google.common.collect.Lists;
 
 import java.util.*;
 
 /**
  * Registry for API provider objects.
  *
  * @author jk-5
  */
 public class HandlerRegistry {
 
    private static final Map<String, PasteProvider> PasteProviders = new HashMap<String, PasteProvider>();
     private static final Map<String, NotificationHandler> notificationHandlers = new HashMap<String, NotificationHandler>();
 
     public static void registerPasteProvider(String id, PasteProvider provider){
        if(PasteProviders.containsKey(id))
            throw new IllegalArgumentException("Pastebin provider " + id + " already registered by " + PasteProviders.get(id) + " when registering " + provider);
 
        PasteProviders.put(id, provider);
     }
 
     public static PasteProvider getPasteProvider(String id){
        return PasteProviders.get(id);
     }
 
     public static List<PasteProvider> getPasteProviders(){
        List<PasteProvider> providers = new ArrayList<PasteProvider>(PasteProviders.size());
        providers.addAll(PasteProviders.values());
         return providers;
     }
 
     public static Map<String, PasteProvider> getAllPasteProviders(){
        return Collections.unmodifiableMap(PasteProviders);
     }
 
     public static void registerNotificationHandler(String id, NotificationHandler provider){
         if(notificationHandlers.containsKey(id))
             throw new IllegalArgumentException("Notification handler " + id + " already registered by " + notificationHandlers.get(id) + " when registering " + provider);
 
         notificationHandlers.put(id, provider);
     }
 
     public static NotificationHandler getNotificationHandler(String id){
         return notificationHandlers.get(id);
     }
 
     public static List<NotificationHandler> getNotificationHandlers(){
         List<NotificationHandler> handlers = Lists.newArrayListWithCapacity(notificationHandlers.size());
         handlers.addAll(notificationHandlers.values());
         return handlers;
     }
 
     public static Map<String, NotificationHandler> getAllNotificationHandlers(){
         return Collections.unmodifiableMap(notificationHandlers);
     }
 }
