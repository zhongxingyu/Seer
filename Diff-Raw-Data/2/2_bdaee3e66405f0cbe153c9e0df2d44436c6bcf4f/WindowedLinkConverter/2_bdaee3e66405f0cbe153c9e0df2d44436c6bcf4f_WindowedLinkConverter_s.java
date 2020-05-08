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
 package ditl.plausible;
 
 import java.io.*;
 import java.util.*;
 
 import ditl.*;
 import ditl.graphs.*;
 
 public class WindowedLinkConverter implements Converter, Generator, LinkTrace.Handler {
 	
 	private long _window;
 	private Bus<Link> expire_bus = new Bus<Link>();
 	private Bus<Link> update_bus = new Bus<Link>();
 	private Bus<Link> pop_bus = new Bus<Link>();
 	private Bus<Link> state_bus = new Bus<Link>();
 	private Bus<LinkEvent> bus = new Bus<LinkEvent>();
 	private Map<Link,LinkTimeline> link_timelines = new HashMap<Link,LinkTimeline>();
 	
 	private StatefulWriter<WindowedLinkEvent,WindowedLink> windowed_writer;
 	private StatefulReader<LinkEvent,Link> link_reader;
 	private WindowedLinkTrace windowed_links;
 	private LinkTrace _links;
 	
 	private long cur_time;
 	private Random rng = new Random();
 	
 	public WindowedLinkConverter(WindowedLinkTrace windowedLinks, LinkTrace links, 
 			long window ){
 		_links = links;
 		windowed_links = windowedLinks; 
 		_window = window;
 	}
 
 	@Override
 	public void convert() throws IOException {
 		windowed_writer = windowed_links.getWriter(_links.snapshotInterval());
 		link_reader = _links.getReader(0,_window);
 		
 		link_reader.setBus(bus);
 		link_reader.setStateBus(state_bus);
 		bus.addListener(linkEventListener());
 		state_bus.addListener(linkListener());
 		pop_bus.addListener(popListener());
 		expire_bus.addListener(expireListener());
 		update_bus.addListener(updateListener());
 		
 		long minTime = _links.minTime() - _window;
 		long maxTime = _links.maxTime() + _window;
 		
 		Runner runner = new Runner(_links.ticsPerSecond(), minTime, maxTime);
 		runner.addGenerator(link_reader);
 		runner.addGenerator(this);
 		
 		runner.run();
 		
 		windowed_writer.flush();
 		windowed_writer.setProperty(WindowedLinkTrace.windowLengthKey, _window);
 		windowed_writer.setProperty(Trace.ticsPerSecondKey, _links.ticsPerSecond());
 		windowed_writer.close();
 		link_reader.close();
 	}
 	
 	@Override
 	public Bus<?>[] busses() {
 		return new Bus<?>[]{ expire_bus, update_bus, pop_bus };
 	}
 
 	@Override
 	public int priority() {
 		return Trace.defaultPriority;
 	}
 
 	@Override
 	public void incr(long time) throws IOException {
		windowed_writer.flush(time - _window);
 		cur_time += time;
 	}
 
 	@Override
 	public void seek(long time) throws IOException {
 		cur_time = time;
 	}
 
 	@Override
 	public Listener<LinkEvent> linkEventListener() {
 		return new Listener<LinkEvent>(){
 			@Override
 			public void handle(long time, Collection<LinkEvent> events)
 					throws IOException {
 				// here time is the time in the window-shifted link event trace
 				for ( LinkEvent lev : events ){
 					final Link l = lev.link();
 					if ( ! link_timelines.containsKey(l) ){
 						link_timelines.put(l, new LinkTimeline(l, windowed_writer));
 						windowed_writer.queue(time, new WindowedLinkEvent(l, WindowedLinkEvent.UP));
 					}
 					LinkTimeline timeline = link_timelines.get(l);
 					timeline.append(time+_window, _window, lev);
 					
 					pop_bus.queue(time+2*_window, l);
 					update_bus.queue(time+_window, l);
 					if ( lev.isUp() ){
 						expire_bus.removeFromQueueAfterTime(time, new Matcher<Link>(){
 							@Override
 							public boolean matches(Link link) {
 								return link.equals(l);
 							}
 						});
 					} else {
 						expire_bus.queue(time+2*_window, l);
 					}
 				}
 			}
 		};
 	}
 
 	@Override
 	public Listener<Link> linkListener() {
 		return new StatefulListener<Link>(){
 
 			@Override
 			public void reset() {
 				link_timelines.clear();
 			}
 
 			@Override
 			public void handle(long time, Collection<Link> links)
 					throws IOException {
 				Set<WindowedLink> init_state = new HashSet<WindowedLink>();
 				for ( Link l : links ){
 					LinkTimeline timeline = new LinkTimeline(l, windowed_writer);
 					long delta = (long)(rng.nextDouble()*_window);
 					timeline.next_up = time + delta; // random start date for links that are already up
 					timeline.queue(time + delta, new LinkEvent(l, LinkEvent.UP));
 					pop_bus.queue(time+_window + delta, l);
 					update_bus.queue(time+delta, l);
 					link_timelines.put(l, timeline);
 					init_state.add(timeline.windowedLink());
 				}
 				windowed_writer.setInitState(time, init_state);
 			}
 		};
 	}
 	
 	public Listener<Link> updateListener(){
 		return new Listener<Link>(){
 			@Override
 			public void handle(long time, Collection<Link> links) throws IOException {
 				for ( Link l : links ){
 					link_timelines.get(l).update(time);
 				}
 			}
 		};
 	}
 	
 	public Listener<Link> popListener(){
 		return new Listener<Link>(){
 			@Override
 			public void handle(long time, Collection<Link> links) throws IOException {
 				for ( Link l : links ){
 					LinkTimeline timeline = link_timelines.get(l);
 					if ( timeline != null ){ // it could have been previously expired
 						timeline.pop(time, _window);
 					}
 				}
 			}
 		};
 	}
 	
 	public Listener<Link> expireListener(){
 		return new Listener<Link>(){
 			@Override
 			public void handle(long time, Collection<Link> links) throws IOException {
 				for ( Link l : links ){
 					link_timelines.remove(l).expire(time);
 				}
 			}
 		};
 	}
 }
