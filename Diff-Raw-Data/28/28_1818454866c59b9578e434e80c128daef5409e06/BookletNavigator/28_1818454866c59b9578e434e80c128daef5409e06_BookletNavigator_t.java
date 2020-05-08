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
 
 import com.google.gwt.user.client.Window;
 import com.googlecode.mgwt.ui.client.widget.Carousel;
 import com.googlecode.mgwt.ui.client.widget.LayoutPanel;
 
 import ch.sebastienzurfluh.swissmuseum.core.client.control.eventbus.EventBus;
 import ch.sebastienzurfluh.swissmuseum.core.client.model.Model;
 import ch.sebastienzurfluh.swissmuseum.core.client.model.structure.MenuData;
 import ch.sebastienzurfluh.swissmuseum.core.client.patterns.Observable;
 import ch.sebastienzurfluh.swissmuseum.core.client.patterns.Observer;
 
 /**
  * The BookletNavigator listens to the changes in the set of pages in the current booklet and
  * display those in a magasine-like manner.
  *
  *
  * @author Sebastien Zurfluh
  *
  */
 public class BookletNavigator extends LayoutPanel implements Observer {
 	private Model model;
 	private EventBus eventBus;
 
 	private Carousel carousel;
 
 	public BookletNavigator(
 			EventBus eventBus,
 			Model model) {
 		this.model = model;
 		this.eventBus = eventBus;
 		
 		carousel = new InteractiveCarousel(eventBus, model);
 		add(carousel);
 		
		
		// Needed for the panel to show properly!
 		this.setHeight(Window.getClientHeight() + "px");
		carousel.setHeight(Window.getClientHeight() + "px");
 		
 		model.allPagesMenusInCurrentGroupObservable.subscribeObserver(this);
 	}
 
 	@Override
 	public void notifyObserver(Observable source) {
 		carousel.clear();
 		for(MenuData menuData : model.getAllPageMenusInCurrentGroup()) {
 			LoadOnDemandPageWidget page = 
 					new LoadOnDemandPageWidget(menuData, eventBus, model);
 			carousel.add(page);
 		}
 	}
 }
