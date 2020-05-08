 /*
  * This file is part of d3.
  * 
  * d3 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * d3 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with d3.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Copyright 2010 Guilhelm Savin
  */
 package org.d3.feature;
 
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.TimeUnit;
 
 import org.d3.Console;
 import org.d3.actor.Agency;
 import org.d3.actor.CallException;
 import org.d3.actor.Feature;
 import org.d3.actor.Future;
 import org.d3.actor.StepActor;
 import org.d3.events.Bindable;
 import org.d3.remote.RemoteAgency;
 import org.d3.remote.RemoteEvent;
 
 public class RemoteActorLister extends Feature implements Bindable, StepActor {
 
 	private ConcurrentLinkedQueue<RemoteAgency> agencies;
 
 	public RemoteActorLister() {
 		super("remote_actor_lister");
 		agencies = new ConcurrentLinkedQueue<RemoteAgency>();
 	}
 
 	public long getStepDelay(TimeUnit unit) {
 		return unit.convert(10, TimeUnit.SECONDS);
 	}
 
 	public void step() {
 		for (RemoteAgency remote : agencies) {
 			Future f = (Future) remote.asRemoteActor().call("actors_list");
 
 			try {
 				f.waitForValue();
 			} catch (InterruptedException e) {
 			}
 
 			if (!Thread.interrupted()) {
 				try {
 					String[] list = f.get();
 
 					if (list != null) {
 						StringBuilder builder = new StringBuilder();
 						builder.append("actors on ").append(remote.getId());
 						for (int i = 0; i < list.length; i++)
 							builder.append("\n- ").append(list[i]);
 						Console.info(builder.toString());
 					}
 
 				} catch (CallException e) {
 					Console.exception(e);
 				}
 			}
 		}
 	}
 
 	public void initFeature() {
 		Agency.getLocalAgency().getRemoteHosts().getEventDispatcher()
 				.bind(this);
 	}
 
 	public <K extends Enum<K>> void trigger(K event, Object... data) {
 		if (RemoteEvent.class.isAssignableFrom(event.getClass())) {
			RemoteEvent revent = RemoteEvent.class.cast(event);
 			switch (revent) {
 			case REMOTE_AGENCY_REGISTERED:
 				agencies.add((RemoteAgency) data[0]);
 				break;
 			case REMOTE_AGENCY_UNREGISTERED:
 				agencies.remove((RemoteAgency) data[0]);
 				break;
 			}
 		}
 	}
 }
