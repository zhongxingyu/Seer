 /*
  * Copyright 2012 Sebastien Zurfluh
  * 
  * This file is part of "Parcours".
  * 
  * "Parcours" is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * "Parcours" is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with "Parcours".  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ch.sebastienzurfluh.client.view.eventbushooks;
 
 import com.google.gwt.animation.client.Animation;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Panel;
 
 import ch.sebastienzurfluh.client.control.eventbus.Event;
 import ch.sebastienzurfluh.client.control.eventbus.Event.EventType;
 import ch.sebastienzurfluh.client.control.eventbus.EventBus;
 import ch.sebastienzurfluh.client.control.eventbus.EventBusListener;
 
 /**
  * This class listens to the page changes in order to scroll
  * automatically to the right position on load.
  *
  *
  * @author Sebastien Zurfluh
  *
  */
 public class ScrollToPanelOnEvent extends Animation implements EventBusListener {
 	private Panel panel;
 	private EventType eventType;
 	
 	private ScrollToPanelOnEvent(EventBus eventBus, Panel panel, EventType eventType) {
 		this.panel = panel;
 		this.eventType = eventType;
 		
 		eventBus.addListener(this);
 	}
 	
 	@Override
 	public EventType getEventType() {
 		return eventType;
 	}
 
 	@Override
 	public void notify(Event e) {
		origin = Window.getScrollTop();
 		destination = panel.getElement().getAbsoluteTop();
 		
 		run(400);
 	}
 
 	public static ScrollToPanelOnEvent addRule(EventBus eventBus, Panel panel, EventType eventType) {
 		return new ScrollToPanelOnEvent(eventBus, panel, eventType);
 	}
 
 	private int destination = 0;
 	private int origin = 0;
 	@Override
 	protected void onUpdate(double progress) {
 		Window.scrollTo(0 , (int) (origin + (destination - origin) * progress));
 	}
 
 }
