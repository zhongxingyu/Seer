 /*
  * Copyright (c) 2013 Mark D. Horton
  *
  * This is free software; you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option)
  * any later version.
  *
  * This software is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABIL-
  * ITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
  * Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.nostromo.qbuffer;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
 public abstract class QParticipant<E> {
 
     public enum CommitMode {
         SET, LAZY_SET, LAZY_SET_MIX
     }
 
     private static final AtomicInteger idCounter = new AtomicInteger();
 
     private final int id;
 
     // these 4 vars are used by both the producer and consumer threads
     protected final E[] data;
     protected final AtomicLong head;
     protected final AtomicLong tail;
     protected final AtomicBoolean active;
 
     // the remaining vars are used only by either a producer or a consumer thread
     protected final int batchSize;
     protected final int mask;
 
     protected long ops;
     protected long opsCapacity;
 
     public QParticipant(final E[] data, final AtomicLong head, final AtomicLong tail, final AtomicBoolean active,
             final int batchSize) {
         this.data = data;
         this.head = head;
         this.tail = tail;
         this.active = active;
         this.batchSize = batchSize;
         mask = data.length - 1;
         id = idCounter.getAndIncrement();
     }
 
     abstract long availableOperations();
 
     public abstract long size();
 
     public E peek() {
         return data[(int) (ops & mask)];
     }
 
     public boolean isEmpty() {
         return size() == 0;
     }
 
     public boolean isActive() {
         return active.get();
     }
 
     public boolean activate() {
         return active.compareAndSet(false, true);
     }
 
     public boolean deactivate() {
         setCommit();
         if (!active.compareAndSet(true, false)) return false;
         return true;
     }
 
     public int capacity() {
         return data.length;
     }
 
     public int batchSize() {
         return batchSize;
     }
 
     public long begin() {
         // do we need to calculate a new opsCapacity?
         if (opsCapacity == 0) {
             final boolean active = isActive();
             opsCapacity = availableOperations();
             // return -1 when empty and inactive
             if (opsCapacity == 0 && !active) return -1;
         }
 
         // return opsCapacity, but ensure it's not greater than batchSize
         return (batchSize < opsCapacity) ? batchSize : opsCapacity;
     }
 
     public long setCommit() {
         return commit(CommitMode.SET);
     }
 
     public long lazySetCommit() {
         return commit(CommitMode.LAZY_SET);
     }
 
     public long lazySetMixCommit() {
         return commit(CommitMode.LAZY_SET_MIX);
     }
 
     private long commit(final CommitMode mode) {
         final long opCount = ops - tail.get();
         opsCapacity -= opCount;
 
         switch (mode) {
             case LAZY_SET_MIX:
                 // if we've used up the current opsCapacity then set(), otherwise lazySet(),
                 // this logic seemed to perform better that just lazySet()
                 if (opsCapacity == 0) tail.set(ops);
                 else tail.lazySet(ops);
                 break;
             case SET:
                 // just set()
                 tail.set(ops);
                 break;
             case LAZY_SET:
                 // just lazySet()
                 tail.lazySet(ops);
                 break;
         }
 
         return opCount;
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (this == obj) return true;
         if (obj == null || getClass() != obj.getClass()) return false;
 
         final QParticipant that = (QParticipant) obj;
        return id != that.id;
     }
 
     @Override
     public int hashCode() {
         return id;
     }
 }
