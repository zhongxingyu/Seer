 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
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
 package ditl.graphs;
 
 import java.io.IOException;
 import java.util.*;
 
 import ditl.*;
 
 
 
 public class MovementToLinksConverter implements Incrementable, MovementTrace.Handler, Converter {
 
 	private double r2;
 	private long max_interval;
 	private Map<Integer,Movement> invalid_movements = new HashMap<Integer,Movement>();
 	private Map<Integer,Movement> valid_movements = new HashMap<Integer,Movement>();
 	private StatefulWriter<LinkEvent,Link> links_writer;
 	private StatefulReader<MovementEvent,Movement> movement_reader;
 	private MovementTrace _movement;
 	private LinkTrace _links;
 	private long cur_time;
 	
 	public MovementToLinksConverter(LinkTrace links, MovementTrace movement,
 			double range, long maxInterval) {
 		_links = links;
 		_movement = movement;
 		r2 = range*range;
 		max_interval = maxInterval;
 	}
 	
 	@Override
 	public Listener<Movement> movementListener(){
 		return new StatefulListener<Movement>(){
 			@Override
 			public void handle(long time, Collection<Movement> events) throws IOException {
 				for ( Movement m : events )
 					invalid_movements.put(m.id(),m);
 				setInitialState(time);
 			}
 
 			@Override
 			public void reset() {
 				valid_movements.clear();
 				invalid_movements.clear();
 			}
 		};
 	}
 	
 	private void setInitialState(long time) throws IOException{
 		Set<Link> initLinks = new HashSet<Link>();
 		Iterator<Movement> i = invalid_movements.values().iterator();
 		while ( i.hasNext() ){
 			Movement im = i.next();
 			for ( Movement vm : valid_movements.values() ) {
 				long[] meetings = im.meetingTimes(vm, r2);
 				if ( meetings != null ){
 					long begin = meetings[0], end = meetings[1];
 					Link l = new Link(im.id(), vm.id());
 					if ( begin < time ){
 						if ( time <= end ){
 							initLinks.add(l); // link is already up
 							if ( end-time < max_interval ) // link goes down before max_interval
 								links_writer.queue(end, new LinkEvent(l, LinkEvent.DOWN) );
 						}
 					} else { // begin >= time
 						if ( begin-time < max_interval ){
 							links_writer.queue(begin, new LinkEvent(l, LinkEvent.UP));
 							if ( end-time < max_interval ){
								links_writer.queue(begin, new LinkEvent(l, LinkEvent.DOWN));
 							}
 						}
 					}
 				}
 			}
 			i.remove();
 			valid_movements.put(im.id(), im);
 		}
 		links_writer.setInitState(time, initLinks);
 	}
 	
 	@Override
 	public Listener<MovementEvent> movementEventListener(){
 		return new Listener<MovementEvent>(){
 			@Override
 			public void handle(long time, Collection<MovementEvent> events) throws IOException {
 				for ( MovementEvent event : events ){
 					Integer id = event.id();
 					switch(event.type){
 					case MovementEvent.IN: 
 						invalid_movements.put(id,event.origMovement());
 						break;
 						
 					case MovementEvent.OUT:
 						valid_movements.remove(id);
 						invalidNodeMeetings(time, id);
 						break;
 						
 					default:
 						Movement m;
 						if ( invalid_movements.containsKey(id) ){
 							m = invalid_movements.get(id);
 							m.handleEvent(time, event);
 						} else {
 							m = valid_movements.remove(id);
 							invalidNodeMeetings(time, id);
 							m.handleEvent(time, event);
 							invalid_movements.put(id,m);
 						}
 					}
 				}
 				links_writer.flush(time);
 				updateNextMeetings(time);
 			}
 		};
 	}
 	
 	private void updateNextMeetings(long time){
 		Iterator<Movement> i = invalid_movements.values().iterator();
 		while ( i.hasNext() ){
 			Movement m = i.next();
 			for ( Movement vm : valid_movements.values() ) {
 				long[] meetings = m.meetingTimes(vm, r2);
 				if ( meetings != null ){
 					long begin = meetings[0], end = meetings[1];
 					Link l = new Link(m.id(), vm.id());
 					if ( begin >= time && begin-time < max_interval )
 						links_writer.queue(begin, new LinkEvent(l, LinkEvent.UP));
 					if ( end >= time && end-time < max_interval ) // link goes down before max_interval
 						links_writer.queue(end, new LinkEvent(l, LinkEvent.DOWN) );
 				}
 			}
 			valid_movements.put(m.id(), m);
 			i.remove();
 		}
 	}
 	
 	private void invalidNodeMeetings(final long time, final Integer i){
 		links_writer.removeFromQueueAfterTime(time, new Matcher<LinkEvent>(){
 			@Override
 			public boolean matches(LinkEvent item) {
 				return item.link().hasVertex(i);
 			}
 		});
 	}
 
 	@Override
 	public void incr(long dt) throws IOException {
 		links_writer.flush(cur_time);
 		cur_time += dt;
 	}
 
 	@Override
 	public void seek(long time) throws IOException {
 		cur_time = time;
 	}
 	
 	@Override
 	public void convert() throws IOException {
 		links_writer = _links.getWriter(_movement.snapshotInterval());
 		movement_reader = _movement.getReader();
 		
 		movement_reader.stateBus().addListener(movementListener());
 		movement_reader.bus().addListener(movementEventListener());
 		
 		links_writer.setProperty(Trace.ticsPerSecondKey, _movement.ticsPerSecond());
 		links_writer.setProperty(Trace.maxTimeKey, _movement.maxTime());
 		Runner runner = new Runner(_movement.maxUpdateInterval(), _movement.minTime(), _movement.maxTime());
 		runner.addGenerator(movement_reader);
 		runner.add(this);
 		runner.run();
 		
 		links_writer.flush(_movement.maxTime());
 		links_writer.close();
 		movement_reader.close();
 	}
 }
