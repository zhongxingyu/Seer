 /*
  * Copyright 2012-2013 Sebastien Zurfluh
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
 
 package ch.sebastienzurfluh.swissmuseum.parcours.client.view.bookletnavigator;
 
 import ch.sebastienzurfluh.swissmuseum.core.client.control.eventbus.EventBus;
 import ch.sebastienzurfluh.swissmuseum.core.client.control.eventbus.events.PageChangeRequest;
 import ch.sebastienzurfluh.swissmuseum.core.client.model.Model;
 import ch.sebastienzurfluh.swissmuseum.core.client.model.structure.DataReference;
 
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.user.client.Window;
 import com.googlecode.mgwt.ui.client.widget.Carousel;
 
 /** This extended {@link Carousel} sends notifications through {@link EventBus} when a page is 
  * turned.
  *
  * @author Sebastien Zurfluh
  *
  */
 public class InteractiveCarousel extends Carousel {
 	public InteractiveCarousel(
 			final EventBus eventBus, final Model model) {
 		super();
 		
 		addSelectionHandler(new SelectionHandler<Integer>() {
 			int previousSelection = -1;
 			
 			@Override
 			public void onSelection(SelectionEvent<Integer> event) {
 				Window.alert("Previous selection:" + previousSelection
 						+ "\n and the current selection is " + event.getSelectedItem());
 				if (previousSelection == -1) {
 					previousSelection = event.getSelectedItem();
 				} else {
 					if (previousSelection == event.getSelectedItem()) {
 						previousSelection = -1;
 						eventBus.fireEvent(new PageChangeRequest(DataReference.SUPER));
 					} else if (previousSelection < event.getSelectedItem()) {
 						previousSelection = event.getSelectedItem();
 						Window.alert("model.getNextPage " + model.getNextPageMenu().getTitle());
						eventBus.fireEvent(new PageChangeRequest(model.getNextPageMenu().getReference()));
 					} else if (previousSelection > event.getSelectedItem()) {
 						previousSelection = event.getSelectedItem();
						eventBus.fireEvent(new PageChangeRequest(model.getPreviousPageMenu().getReference()));
 					}
 				}
 				Window.alert("New selection is " + previousSelection);
 			}
 		});
 	}
 }
