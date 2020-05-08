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
 
 package ch.sebastienzurfluh.client.view.navigation;
 
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.ui.FocusPanel;
 import com.google.gwt.user.client.ui.Image;
 
 import ch.sebastienzurfluh.client.model.structure.DataReference;
 import ch.sebastienzurfluh.client.model.structure.MenuData;
 import ch.sebastienzurfluh.client.view.menuinterface.MenuButton;
 import ch.sebastienzurfluh.client.view.menuinterface.PageRequestHandler;
 
 /**
  * This is an item of the {@link NavigationSlider}.
  * @author Sebastien Zurfluh
  *
  */
 public class NavigationItem extends FocusPanel implements MenuButton {
 	private Image tileImage;
 	private DataReference menuReference;
 	private String stylePrimaryName = "navigationItem";
 	private int priority;
 	
 	public NavigationItem(MenuData menuData) {
 		this.menuReference = menuData.getReference();
 		this.priority = menuData.getPriorityNumber();
 		
 		tileImage = new Image(menuData.getRectangleImgURL());
 		tileImage.setStyleName(stylePrimaryName + "-" + "image");
 		tileImage.setAltText(menuData.getTitle());
 		
 		setWidget(tileImage);
 	}
 
 	public DataReference getReference() {
 		return menuReference;
 	}
 	
 	public int getPriority() {
 		return priority;
 	}
 	
 	private PageRequestHandler pageRequestHandler;
 	@Override
 	public HandlerRegistration addClickHandler(ClickHandler handler) {
 		assert handler instanceof PageRequestHandler;
 		
 		this.pageRequestHandler = (PageRequestHandler) handler;
 	    return super.addClickHandler(handler);
 	}
 	
 	/**
 	 * Make the button fire it's associated event.
 	 * 
 	 * Usually this is used by the scroller to simulate a mouse notify
 	 * the item it has been selected.
 	 */
 	public void ignite() {
		pageRequestHandler.requestPage(menuReference);
 	}
 	
 	/**
 	 * Makes the widget focused. (Like when you hover over a text link.
 	 * 
 	 * Default is disabled.
 	 */
 	public void setFocus(boolean enable) {
 		//TODO implement the setFocus method of the NavigationItem
 	}
 }
