 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011-2012 John Whitbeck <john@whitbeck.fr>                    *
  *                                                                             *
  * DITL is free software: you can redistribute it and/or modify                *
  * it under the terms of the GNU General Public License as published by        *
  * the Free Software Foundation, either version 3 of the License, or           *
  * (at your option) any later version.                                         *
  *                                                                             *
  * DITL is distributed in the hope that it will be useful,                     *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  * GNU General Public License for more details.                                *
  *                                                                             *
  * You should have received a copy of the GNU General Public License           *
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
  *******************************************************************************/
 package ditl;
 
 import java.io.IOException;
 import java.util.Set;
 
 public class StatefulReader<E extends Item, S extends Item> extends Reader<E> {
 
     private final StateUpdater<E, S> _updater;
     private Bus<S> state_bus = new Bus<S>();
     private final Item.Factory<S> state_factory;
 
     StatefulReader(StatefulTrace<E, S> trace, int priority, long offset) throws IOException {
         super(trace, priority, offset);
         _updater = trace.getNewUpdaterFactory();
         state_factory = trace.stateFactory();
     }
 
     public Set<S> referenceState() {
         return _updater.states();
     }
 
     @Override
     public void seek(long time) throws IOException {
         if (seek_map.getOffset(time + _offset) == Long.MIN_VALUE) {
             throw new IOException("Cannot seek before initial state");
         }
         fastSeek(time + _offset);
         // we always hit a state item block after this step
         _updater.setState(readItemBlock(state_factory));
         prev_time = next_time;
         readHeader();
        while (hasNext() && next_time < time + _offset) {
             for (final E event : next()) {
                 // cur_time is updated by call to next
                 _updater.handleEvent(cur_time, event);
             }
         }
         state_bus.queue(time, _updater.states());
         cur_time = time + _offset;
     }
 
     public void setStateBus(Bus<S> bus) {
         state_bus = bus;
     }
 
     public Bus<S> stateBus() {
         return state_bus;
     }
 
     @Override
     public void step() throws IOException {
         if (next_flag == StatefulWriter.STATE) {
             skipBlock();
             readHeader();
         }
         super.step();
     }
 
     @Override
     public Bus<?>[] busses() {
         return new Bus<?>[] { _bus, state_bus };
     }
 
 }
