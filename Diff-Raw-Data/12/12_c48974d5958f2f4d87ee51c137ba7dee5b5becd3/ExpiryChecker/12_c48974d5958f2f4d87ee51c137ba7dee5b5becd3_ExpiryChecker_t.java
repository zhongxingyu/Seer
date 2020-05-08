 /**
  * Copyright (C) 2011-2012 Tim Besard <tim.besard@gmail.com>
  *
  * All rights reserved.
  */
 package be.mira.codri.server.bo.network;
 
 import be.mira.codri.server.bo.network.entities.Kiosk;
 import be.mira.codri.server.bo.Network;
 import be.mira.codri.server.exceptions.NetworkException;
 import be.mira.codri.server.spring.Slf4jLogger;
 import java.util.Map;
 import java.util.TimerTask;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Required;
 
 /**
  *
  * @author tim
  */
 public class ExpiryChecker extends TimerTask {
     //
     // Data members
     //
     
     @Slf4jLogger
     private Logger mLogger;
     
     private Network mNetwork;
 
     private Long mThreshold;
     
     
     //
     // Construction and destruction
     //    
     
     @Required
     @Autowired
     public final void setNetwork(final Network iNetwork) {
         mNetwork = iNetwork;
     }
     
     @Required
     public final void setThreshold(final Long iThreshold) {
         mThreshold = iThreshold;
     }
     
     
     //
     // TimerTask implementation
     //
     
     @Override
     public final void run() {
         mLogger.trace("Checking for expired network entities");
         
         // Check all kiosks
         for (Map.Entry<String, Kiosk> tEntry : mNetwork.getKiosks().entrySet()) {
             if (tEntry.getValue().getHeartbeatDelta() > mThreshold) {
            mLogger.trace("Kiosk {}'s last heartbeat was at {} (delta {}), and thus exceeds the threshold value of {}",
                    new Object[] {
                        tEntry.getKey(),
                        tEntry.getValue().getHeartbeat(),
                        tEntry.getValue().getHeartbeatDelta(),
                        mThreshold });
                 try {
                     mLogger.debug("Trying to expire kiosk {}", tEntry.getKey());
                     mNetwork.expireKiosk(tEntry.getKey());
                 } catch (NetworkException tException) {
                     mLogger.error("Could not expire kiosk {}", tEntry.getKey(), tException);
                 }
             }
         }
     }    
 }
