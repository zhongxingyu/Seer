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
 package ditl.plausible;
 
 import java.io.IOException;
 import java.util.*;
 
 import ditl.*;
 import ditl.graphs.*;
 
 public class WindowedLinkTrace extends StatefulTrace<WindowedLinkEvent,WindowedLink>{
 	
 	public static final String type = "windowed links";
 	public static final String defaultName = "windowed_links";
 	
 	public final static String windowLengthKey = "window length";
 
 	public final static class Updater implements StateUpdater<WindowedLinkEvent,WindowedLink>{
 
 		private Set<WindowedLink> state = new WindowedLinkSet();
 		private Map<Link,WindowedLink> map = new AdjacencyMap.Links<WindowedLink>();
 		
 		@Override
 		public void handleEvent(long time, WindowedLinkEvent event) {
 			Link link = event.link();
 			WindowedLink wl;
 			switch ( event.type() ){
 			case WindowedLinkEvent.UP:
 				wl = new WindowedLink(link);
 				state.add(wl);
 				map.put(link,wl);
 				break;
 			case WindowedLinkEvent.DOWN:
 				wl = map.get(link);
 				state.remove(wl);
 				map.remove(link);
 				break;
 			default:
 				wl = map.get(link);
 				wl.handleEvent(event);
 			}
 		}
 
 		@Override
 		public void setState(Collection<WindowedLink> states) {
 			state.clear();
 			map.clear();
 			state.addAll(states);
 			for ( WindowedLink wl : states ){
 				map.put(wl.link(), wl);
 			}
 		}
 
 		@Override
 		public Set<WindowedLink> states() {
 			return state;
 		}
 	}
 	
	public final static class WindowedLinkMap extends AdjacencyMap<WindowedLink,WindowedLink> {
 		@Override
 		protected WindowedLink newCouple(Integer id1, Integer id2) {
 			throw new UnsupportedOperationException();
 		}
 	}
 	
	public final static class WindowedLinkSet extends AdjacencySet<WindowedLink> {
 		WindowedLinkSet(){
 			map = new WindowedLinkMap();
 		}
 	}
 	
 	public interface Handler {
 		public Listener<WindowedLink> windowedLinkListener();
 		public Listener<WindowedLinkEvent> windowedLinkEventListener();
 	}
 	
 
 	public WindowedLinkTrace(Store store, String name, PersistentMap info) throws IOException {
 		super(store, name, info, new WindowedLinkEvent.Factory(), new WindowedLink.Factory(), 
 				new StateUpdaterFactory<WindowedLinkEvent,WindowedLink>(){
 					@Override
 					public StateUpdater<WindowedLinkEvent, WindowedLink> getNew() {
 						return new WindowedLinkTrace.Updater();
 					}
 		});
 	}
 
 	public long windowLength(){
 		return Long.parseLong(getValue(windowLengthKey));
 	}
 }
