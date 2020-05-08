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
 
 import org.d3.Console;
 import org.d3.actor.Agency;
 import org.d3.actor.Feature;
 import org.d3.agency.AgencyEvents;
 import org.d3.events.Bindable;
 import org.d3.events.NonBindableActorException;
 
 public class BindTest extends Feature implements Bindable {
 
 	public BindTest() {
 		super("test");
 	}
 
 	public <K extends Enum<K>> void trigger(K event, Object... data) {
 		if (AgencyEvents.class.isAssignableFrom(event.getClass())) {
			AgencyEvents aEvent = (AgencyEvents) event;
 			Console.info("agency event: %s, %s", aEvent, data[0]);
 		}
 	}
 
 	public void initFeature() {
 		try {
 			Agency.getLocalAgency().getEventDispatcher().bind();
 		} catch (NonBindableActorException e) {
 		}
 	}
 }
