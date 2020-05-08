 package br.unb.tr2.zeroconf;
 
 import java.net.InetAddress;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Logger;
 
 /**
  * Copyright (C) 2013 Loop EC - All Rights Reserved
  * Created by sandoval for harmonic-server
  */
 public class DiscoveryService {
 
     private Logger logger = Logger.getLogger("DiscoveryService");
 
     private Set<ServiceAnnouncement> sentAnnouncements = Collections.synchronizedSet(new HashSet<ServiceAnnouncement>());
 
     private Set<DiscoveryListener> discoveryListeners = Collections.synchronizedSet(new HashSet<DiscoveryListener>());
 
     private DiscoveryService() {
         new Thread(new DiscoveryThread(this)).start();
     }
 
     public void broadcastServiceAnnouncement(ServiceAnnouncement serviceAnnouncement) {
         new Thread(new AnnouncementThread(this, serviceAnnouncement)).start();
     }
 
     public void sendServiceAnnouncement(ServiceAnnouncement serviceAnnouncement, InetAddress destination) {
         new Thread(new AnnouncementThread(this, serviceAnnouncement, destination)).start();
     }
 
     public void sendServiceAnnouncement(ServiceAnnouncement serviceAnnouncement, InetAddress destination, Integer port) {
        new Thread(new AnnouncementThread(this, serviceAnnouncement, destination, port));
     }
 
     public void notifyReceivedServiceAnnouncement(ServiceAnnouncement serviceAnnouncement) {
         if (sentAnnouncements.contains(serviceAnnouncement))
             return;
         logger.info("New service: " + serviceAnnouncement.getService() + " on address " + serviceAnnouncement.getAddress().getHostAddress() + ":" + serviceAnnouncement.getPort());
         synchronized (discoveryListeners) {
             Iterator<DiscoveryListener> i = discoveryListeners.iterator();
             while (i.hasNext()) {
                 i.next().DSHasReceivedAnnouncement(serviceAnnouncement);
             }
         }
     }
 
     public void notifySentServiceAnnouncement(ServiceAnnouncement serviceAnnouncement) {
         sentAnnouncements.add(serviceAnnouncement);
     }
 
     public Boolean addListener(DiscoveryListener discoveryListener) {
         return discoveryListeners.add(discoveryListener);
     }
 
     public Boolean removeListener(DiscoveryListener discoveryListener) {
         return discoveryListeners.remove(discoveryListener);
     }
 
     private static DiscoveryService instance = null;
 
     public static DiscoveryService getInstance() {
         if (instance == null)
             instance = new DiscoveryService();
         return instance;
     }
 
 }
