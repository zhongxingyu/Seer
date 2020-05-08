 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.hedwig.server.delivery;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.apache.bookkeeper.util.MathUtils;
 import org.apache.hedwig.protocol.PubSubProtocol.PubSubResponse;
 
 public class ClusterDeliveryEndPoint implements DeliveryEndPoint, ThrottlingPolicy {
 
     volatile boolean closed = false;
     final ReentrantReadWriteLock closeLock = new ReentrantReadWriteLock();
     final LinkedHashMap<DeliveryEndPoint, DeliveryState> endpoints =
             new LinkedHashMap<DeliveryEndPoint, DeliveryState>();
     final HashMap<Long, DeliveredMessage> pendings = new HashMap<Long, DeliveredMessage>();
     final String label;
     final int messageWindowSize;
     final ScheduledExecutorService scheduler;
 
     static class DeliveryState {
         SortedSet<Long> msgs = new TreeSet<Long>();
     }
 
     class DeliveredMessage {
         final PubSubResponse msg;
         volatile long lastDeliveredTime;
         volatile DeliveryEndPoint lastDeliveredEP = null;
 
         DeliveredMessage(PubSubResponse msg) {
             this.msg = msg;
             this.lastDeliveredTime = MathUtils.now();
         }
 
         void resetDeliveredTime(DeliveryEndPoint ep) {
             DeliveryEndPoint oldEP = this.lastDeliveredEP;
             if (null != oldEP) {
                 DeliveryState state = endpoints.get(oldEP);
                 if (null != state) {
                     state.msgs.remove(msg.getMessage().getMsgId().getLocalComponent());
                 }
             }
             this.lastDeliveredTime = MathUtils.now();
             this.lastDeliveredEP = ep;
         }
     }
 
     class ClusterDeliveryCallback implements DeliveryCallback {
 
         final DeliveryEndPoint ep;
         final DeliveryState state;
         final DeliveredMessage msg;
         final long deliveredTime;
 
         ClusterDeliveryCallback(DeliveryEndPoint ep, DeliveryState state, DeliveredMessage msg) {
             this.ep = ep;
             this.state = state;
             this.msg = msg;
             this.deliveredTime = msg.lastDeliveredTime;
             // add this msgs to current delivery endpoint state
             this.state.msgs.add(msg.msg.getMessage().getMsgId().getLocalComponent());
         }
 
         @Override
         public void sendingFinished() {
             // nop
         }
 
         @Override
         public void transientErrorOnSend() {
             closeAndRedeliver(ep, state);
         }
 
         @Override
         public void permanentErrorOnSend() {
             closeAndRedeliver(ep, state);
         }
 
     };
 
     class RedeliveryTask implements Runnable {
 
         final DeliveryState state;
 
         RedeliveryTask(DeliveryState state) {
             this.state = state;
         }
 
         @Override
         public void run() {
             closeLock.readLock().lock();
             try {
                 if (closed) {
                     return;
                 }
             } finally {
                 closeLock.readLock().unlock();
             }
             Set<DeliveredMessage> msgs = new HashSet<DeliveredMessage>();
             synchronized (endpoints) {
                 for (long seqid : state.msgs) {
                     DeliveredMessage msg = pendings.get(seqid);
                     if (null != msg) {
                         msgs.add(msg);
                     }
                 }
             }
             for (DeliveredMessage msg : msgs) {
                 DeliveryEndPoint ep = send(msg);
                 if (null == ep) {
                     // no delivery channel found
                     ClusterDeliveryEndPoint.this.close();
                     return;
                 }
             }
         }
 
     }
 
     public ClusterDeliveryEndPoint(String label, int messageWindowSize, ScheduledExecutorService scheduler) {
         this.label = label;
         this.messageWindowSize = messageWindowSize;
         this.scheduler = scheduler;
     }
 
     public void addDeliveryEndPoint(DeliveryEndPoint endPoint) {
         addDeliveryEndPoint(endPoint, new DeliveryState());
     }
 
     private void addDeliveryEndPoint(DeliveryEndPoint endPoint, DeliveryState state) {
         closeLock.readLock().lock();
         try {
             if (closed) {
                 return;
             }
             synchronized (endpoints) {
                 endpoints.put(endPoint, state);
             }
         } finally {
             closeLock.readLock().unlock();
         }
     }
 
     private void closeAndRedeliver(DeliveryEndPoint ep, DeliveryState state) {
         closeLock.readLock().lock();
         try {
             if (closed) {
                 return;
             }
             // redeliver the state
             scheduler.submit(new RedeliveryTask(state));
         } finally {
             closeLock.readLock().unlock();
         }
     }
 
     public void removeDeliveryEndPoint(DeliveryEndPoint endPoint) {
         DeliveryState state;
         synchronized (endpoints) {
             state = endpoints.remove(endPoint);
         }
         if (null == state) {
             return;
         }
         closeAndRedeliver(endPoint, state);
     }
 
     // the caller should synchronize
     private Entry<DeliveryEndPoint, DeliveryState> pollDeliveryEndPoint() {
         if (endpoints.isEmpty()) {
             return null;
         } else {
             Iterator<Entry<DeliveryEndPoint, DeliveryState>> iter = endpoints.entrySet().iterator();
             Entry<DeliveryEndPoint, DeliveryState> entry = iter.next();
             iter.remove();
             return entry;
         }
     }
 
     public boolean hasAvailableDeliveryEndPoints() {
         synchronized (endpoints) {
             return !endpoints.isEmpty();
         }
     }
 
     @Override
     public boolean messageConsumed(long newSeqIdConsumed) {
         DeliveredMessage msg;
         synchronized (endpoints) {
             msg = pendings.remove(newSeqIdConsumed);
             if (null != msg && null != msg.lastDeliveredEP) {
                 DeliveryState state = endpoints.get(msg.lastDeliveredEP);
                 if (null != state) {
                     state.msgs.remove(newSeqIdConsumed);
                 }
             }
         }
         return null != msg;
     }
 
     @Override
     public boolean shouldThrottle(long lastSeqIdDelivered) {
         synchronized (endpoints) {
             return pendings.size() >= messageWindowSize;
         }
     }
 
     @Override
     public void send(final PubSubResponse response, final DeliveryCallback callback) {
         closeLock.readLock().lock();
         try {
             if (closed) {
                 callback.permanentErrorOnSend();
                 return;
             }
             DeliveryEndPoint ep = send(new DeliveredMessage(response));
             if (null == ep) {
                 // no delivery endpoint
                 callback.permanentErrorOnSend();
             } else {
                 // callback after sending the message
                 callback.sendingFinished();
             }
         } finally {
             closeLock.readLock().unlock();
         }
     }
 
     private DeliveryEndPoint send(final DeliveredMessage msg) {
         Entry<DeliveryEndPoint, DeliveryState> entry = pollDeliveryEndPoint();
         DeliveryCallback dcb;
         synchronized (endpoints) {
             if (null == entry) {
                 // no delivery endpoint found
                 return null;
             }
             dcb = new ClusterDeliveryCallback(entry.getKey(), entry.getValue(), msg);
             long seqid = msg.msg.getMessage().getMsgId().getLocalComponent();
            pendings.put(seqid, msg);
             msg.resetDeliveredTime(entry.getKey());
             addDeliveryEndPoint(entry.getKey(), entry.getValue());
         }
         entry.getKey().send(msg.msg, dcb);
         return entry.getKey();
     }
 
     public void sendSubscriptionEvent(PubSubResponse resp) {
         List<Entry<DeliveryEndPoint, DeliveryState>> eps;
         synchronized (endpoints) {
             eps = new ArrayList<Entry<DeliveryEndPoint, DeliveryState>>(endpoints.entrySet());
         }
         for (final Entry<DeliveryEndPoint, DeliveryState> entry : eps) {
             entry.getKey().send(resp, new DeliveryCallback() {
 
                 @Override
                 public void sendingFinished() {
                     // do nothing
                 }
 
                 @Override
                 public void transientErrorOnSend() {
                     closeAndRedeliver(entry.getKey(), entry.getValue());
                 }
 
                 @Override
                 public void permanentErrorOnSend() {
                     closeAndRedeliver(entry.getKey(), entry.getValue());
                 }
 
             });
         }
     }
 
     @Override
     public void close() {
         closeLock.writeLock().lock();
         try {
             if (closed) {
                 return;
             }
             closed = true;
         } finally {
             closeLock.writeLock().unlock();
         }
     }
 
 }
