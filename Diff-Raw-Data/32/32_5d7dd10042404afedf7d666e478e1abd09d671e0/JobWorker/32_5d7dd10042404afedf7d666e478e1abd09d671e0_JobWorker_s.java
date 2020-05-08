 package org.aldeon.dht.crawler;
 
 import org.aldeon.core.CoreModule;
 import org.aldeon.dht.Dht;
 import org.aldeon.events.Callback;
 import org.aldeon.networking.common.PeerAddress;
 import org.aldeon.networking.common.Sender;
import org.aldeon.protocol.request.IndicateInterestRequest;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JobWorker {
 
     private static final Logger log = LoggerFactory.getLogger(JobWorker.class);
 
     private final Dht dht;
     private final Sender sender;
     private final TargetPicker picker;
     private static boolean dhtEmpty = false;
 
     public JobWorker(Dht dht, Sender sender, TargetPicker picker) {
         this.dht = dht;
         this.sender = sender;
         this.picker = picker;
     }
 
     public void process(final Job job, final Callback<Boolean> workDone) {
         final PeerAddress peer = picker.findTarget(job);
         if(peer == null) {
             if(!dhtEmpty) {
                 log.info("No target found. Is the DHT empty?");
                 dhtEmpty = true;
             }
             workDone.call(false);
         } else {
             dhtEmpty = false;
             sender.addTask(new GetRelevantPeersTask(peer, job.topic(), dht, new Callback<Boolean>() {
                 @Override
                 public void call(Boolean responseReceived) {
                     if(responseReceived) {
 
                         // The peer is alive. Let's give him an interest entry
                         // 1. Figure out who should we advertise ourselves as
                         final PeerAddress myself  = CoreModule.getInstance().reachableLocalAddress(peer);
                         if(myself != null) {
                             sender.addTask(new IndicateInterestTask(job.topic(), peer, myself, new Callback<Boolean>() {
                                 @Override
                                 public void call(Boolean success) {
                                     if(success) {
                                         log.info("Indicated interest in topic " + job.topic() + " to peer " + peer + " (as " + myself + ")");
                                     }
                                 }
                             }));
                         }
 
                     } else {
                         // remove address from our dht
                         log.info("Failed to receive response from peer (" + peer + "). Removing from DHT.");
                         dht.interestTracker().delAddress(peer);
                         dht.closenessTracker().delAddress(peer);
                     }
                     int demand = dht.interestTracker().getDemand(job.addressType(), job.topic());
                     workDone.call(demand == 0);
                 }
             }));
         }
     }
 }
