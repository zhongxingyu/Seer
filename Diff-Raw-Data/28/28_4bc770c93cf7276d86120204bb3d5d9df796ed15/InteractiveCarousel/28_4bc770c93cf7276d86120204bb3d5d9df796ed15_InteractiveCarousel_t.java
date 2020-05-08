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
 import ch.sebastienzurfluh.swissmuseum.core.client.model.structure.MenuData;
 
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.user.client.ui.Widget;
 import com.googlecode.mgwt.ui.client.widget.Carousel;
 
 /** This extended {@link Carousel} sends notifications through {@link EventBus} when a page is 
  * turned.
  *
  * @author Sebastien Zurfluh
  *
  */
 public class InteractiveCarousel extends Carousel {
 	private int widgetCount = 0;
 	
 	public InteractiveCarousel(
 			final EventBus eventBus, final Model model) {
 		super();
 		widgetCount = 0;
 		
 		addSelectionHandler(new SelectionHandler<Integer>() {
 			int previousSelection = -1;
 			
 			@Override
 			public void onSelection(SelectionEvent<Integer> event) {
 				if (previousSelection == -1) {
 					previousSelection = event.getSelectedItem();
					deferedSetSelectedPage(1);
 				} else {
 					if ((event.getSelectedItem() == 0 || event.getSelectedItem() == widgetCount-1)) {
 						previousSelection = -1;
 						eventBus.fireEvent(new PageChangeRequest(DataReference.SUPER));
 					} else if (previousSelection < event.getSelectedItem()) {
 						previousSelection = event.getSelectedItem();
 						
 						// WORKAROUND
 						// get the next page reference (without using the model's dedicated method
 						int i = 0;
 						for (MenuData menuData : model.getAllPageMenusInCurrentGroup()) {
 							if (i == event.getSelectedItem()) {
 								eventBus.fireEvent(new PageChangeRequest(menuData.getReference()));
 								break;
 							}
 							i++;
 						}
 						// END OF WORKAROUND
 					} else if (previousSelection > event.getSelectedItem()) {
 						previousSelection = event.getSelectedItem();
 						eventBus.fireEvent(new PageChangeRequest(
 								model.getPreviousPageMenu().getReference()));
 					}
 				}
 			}
 		});
 	}
 	
	private boolean isDeferedSetSelectedPage = false;
	private int deferedSelectedPage = 1;
	
	/**
	 * We may need to defer the selection of the page until the carousel is properly loaded.
	 * @param index the index of the page we want to load.
	 */
	private void deferedSetSelectedPage(int index) {
		if (widgetCount > index) {
			isDeferedSetSelectedPage = false;
			setSelectedPage(index);
		} else {
			isDeferedSetSelectedPage = true;
			deferedSelectedPage = index;
		}
	}
	
	
 	@Override
 	public void add(Widget w) {
 		super.add(w);
 		widgetCount++;
		if(isDeferedSetSelectedPage) {
			isDeferedSetSelectedPage = false;
			setSelectedPage(deferedSelectedPage);
		}
 	}
 	
 	@Override
 	public void clear() {
 		super.clear();
 		widgetCount = 0;
		isDeferedSetSelectedPage = false;
 	}
 }
