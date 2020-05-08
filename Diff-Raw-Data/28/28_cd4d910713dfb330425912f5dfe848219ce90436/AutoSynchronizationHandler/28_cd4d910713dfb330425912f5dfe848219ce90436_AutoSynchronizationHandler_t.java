 package org.geogit.geotools.data;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.geogit.api.GeoGIT;
import org.geogit.api.Remote;
import org.geogit.api.porcelain.RemoteResolve;

import com.google.common.base.Optional;
 
 public class AutoSynchronizationHandler {
 
     private Queue<GeoGIT> repositories;
 
     private ScheduledExecutorService executor = null;
 
     private static AutoSynchronizationHandler instance = new AutoSynchronizationHandler();
 
     private AutoSynchronizationHandler() {
         repositories = new ConcurrentLinkedQueue<GeoGIT>();
 
         executor = Executors.newSingleThreadScheduledExecutor();
         executor.scheduleAtFixedRate(new AutoSynchronizer(), 0, 10, TimeUnit.SECONDS);
     }
 
     public void addRepo(GeoGIT geogit) {
         checkNotNull(geogit);
        Optional<Remote> originRemote = geogit.command(RemoteResolve.class).setName("origin")
                .call();
        if (originRemote.isPresent()) {
            repositories.add(geogit);
        }
     }
 
     public static AutoSynchronizationHandler get() {
         return instance;
     }
 
     private class AutoSynchronizer implements Runnable {
 
         @Override
         public void run() {
             for (GeoGIT geogit : repositories) {
                 GeogitSynchronizationHandler.get().setDirty(geogit, null);
             }
         }
 
     }
 }
