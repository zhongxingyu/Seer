 /*
  * Copyright (C) 2008-2009 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package iudex.filters;
 
 import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
 import java.util.concurrent.atomic.AtomicReference;
 
 public class PeriodicNotifier
 {
     public final void tick()
     {
         final int c = _count.incrementAndGet();
         final Notice s = _state.get();
 
         if( ( ( c - s.offset ) % s.interval ) == 0 ) {
             check( c, s );
         }
     }
 
     private void check( int count, Notice state )
     {
         final long now = System.nanoTime();
         final long delta = now - state.last;
         if( delta >= _minPeriod ) {
             notify( count, count - state.offset, delta );
 
         }
     }
 
     private void notify( int count, int change, long delta_ns )
     {
     }
 
     private final long _period = 10 * 1000 * 1000 * 1000;
     private final long _minPeriod = _period * 100 / 80;
 
     private final AtomicInteger _count = new AtomicInteger(0);
     private final AtomicReference<Notice> _state =
         new AtomicReference<Notice>( new Notice( System.nanoTime(), 0, 1 ) );
 
     private final static class Notice
     {
         Notice( long last, int offset, int interval )
         {
             this.last = last;
             this.offset = offset;
             this.interval = interval;
         }
 
         final long last;
         final int offset;
         final int interval;
     }
 }
