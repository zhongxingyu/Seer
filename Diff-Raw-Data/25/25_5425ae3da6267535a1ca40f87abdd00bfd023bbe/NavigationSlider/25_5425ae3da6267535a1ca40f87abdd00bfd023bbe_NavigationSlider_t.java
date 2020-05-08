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
 	
 package ch.sebastienzurfluh.swissmuseum.parcours.client.view.navigation;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import ch.sebastienzurfluh.swissmuseum.core.client.control.eventbus.EventBus;
 import ch.sebastienzurfluh.swissmuseum.core.client.model.Model;
 import ch.sebastienzurfluh.swissmuseum.core.client.model.structure.DataReference;
 import ch.sebastienzurfluh.swissmuseum.core.client.model.structure.MenuData;
 import ch.sebastienzurfluh.swissmuseum.core.client.patterns.Observable;
 import ch.sebastienzurfluh.swissmuseum.core.client.patterns.Observer;
 import ch.sebastienzurfluh.swissmuseum.core.client.view.menuinterface.MenuList;
 import ch.sebastienzurfluh.swissmuseum.parcours.client.view.navigation.animation.AnimatorFactory;
 import ch.sebastienzurfluh.swissmuseum.parcours.client.view.navigation.animation.NavigationAnimator;
 import ch.sebastienzurfluh.swissmuseum.parcours.client.view.navigation.animation.AnimatorFactory.AnimatorType;
 
 import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.TouchEndEvent;
 import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
 import com.google.gwt.event.dom.client.TouchStartEvent;
 import com.google.gwt.event.dom.client.TouchStartHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Event.NativePreviewEvent;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.FocusPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 
 /**
  * This widget gives a way to navigate (next/previous) between pages. 
  * @author Sebastien Zurfluh
  */
 public class NavigationSlider extends FocusPanel implements MenuList, Observer {
 	private Model model;
 	private AbsolutePanel animationPanel;
 	private HorizontalPanel tilePanel;
 	private LinkedList<NavigationItem> tileList;
 	
 	private NavigationAnimator animatedScroller;
 	
 	/**
 	 * The width of an item element. This should be set whenever the size of the element changes.
 	 */
 	private int itemWidth;
 	/**
 	 * The width of the widget. This should be set whenever the size of the element changes.
 	 */
 	private int widgetWidth;
 	
 	
 	
 	public NavigationSlider(String string, EventBus pageRequestBus, Model model) {
 		this.model = model;
 		
 		setStyleName("navigationSlider");
 		
 		tilePanel = new HorizontalPanel();
 		tilePanel.setStyleName("navigationSlider-movingContainer");
 		tileList = new LinkedList<NavigationItem>();
 		
 		animationPanel = new AbsolutePanel();
 		tilePanel.setStyleName("navigationSlider-fixedContainer");
 		animationPanel.add(tilePanel);
 		
 		add(animationPanel);
 		animationPanel.setSize("100%", "100%");
 		
 //		animatedScroller = AnimatorFactory.createAnimator(
 //				AnimatorType.STATIC,
 //				animationPanel, tilePanel, this);
 		
 		animatedScroller = AnimatorFactory.createAnimator(
 				AnimatorType.SWIPE,
 				animationPanel, tilePanel, this, pageRequestBus);
 		
 //		addTouchStartHandler(animatedScroller);	
 //		addTouchEndHandler(animatedScroller);
 //		addMouseDownHandler(animatedScroller);
 //		addMouseUpHandler(animatedScroller);
 		
 		RootPanel.get().addDomHandler(animatedScroller, TouchStartEvent.getType());
		RootPanel.get().addDomHandler(animatedScroller, TouchMoveEvent.getType());
 		RootPanel.get().addDomHandler(animatedScroller, TouchEndEvent.getType());
 		RootPanel.get().addDomHandler(animatedScroller, MouseDownEvent.getType());
 		RootPanel.get().addDomHandler(animatedScroller, MouseUpEvent.getType());
 		
//		preventBrowserInterference();
 		
 		model.currentPageDataObservable.subscribeObserver(this);
 	}
 
 	private void addTile(MenuData menuData) {
 		NavigationItem tile = new NavigationItem(menuData);
 		tileList.add(tile);
 		tilePanel.add(tile);
 	}
 	
 	private void clearTiles() {
 		tilePanel.clear();
 		tileList.clear();
 	}
 	
 	/**
 	 * We never change the size of the items or this widget after it is loaded. Thus we may
 	 * compute it once on start then never again.
 	 */
 	private boolean updated = false;
 	public void reloadTiles(Collection<MenuData> menus) {
 		clearTiles();
 		for (MenuData menuData : menus) {
 			addTile(menuData);
 		}
 		if (!updated && !tileList.isEmpty())
 			update();
 	}
 
 	/**
 	 * Updates the never changing values of item and widget width.
 	 */
 	private void update() {
 		if(!tileList.isEmpty())
 			this.itemWidth = getItem(1).getOffsetWidth();
 		this.widgetWidth = getOffsetWidth();
 		this.updated = true;
 	}
 
 	/*******************************************************************************/	
 	/************* Centralised widget selection ************************************/
 	/*******************************************************************************/
 	
 	private int currentItemRank = 1;
 	/**
 	 * @return the number of menu items in the widget
 	 */
 	public int getItemCount() {
 		return tileList.size();
 	}
 	
 	public void setCurrentItem(int rank) {
 		currentItemRank = rank;
 	}
 	public int getCurrentItemRank() {
 		return currentItemRank;
 	}
 	
 	/**
 	 * @return Pixel size of a menu item.
 	 */
 	public int getItemWidth() {
 		return itemWidth;
 	}
 	
 	/**
 	 * @return Width in pixel of the complete widget
 	 */
 	public int getWidgetWidth() {
 		return widgetWidth;
 	}
 	
 	/**
 	 * Focus the navigation slider on the item with given reference. Provided it exists.
 	 * @param menuReference
 	 */
 	public void setFocusWidget(DataReference menuReference) {
 		// retrieve the menu in the list.
 		int menuRank = 1;
 		for (NavigationItem menu : tileList) {
 			if (menu.getReference().equals(menuReference)) {
 				setCurrentItem(menuRank);
 				animatedScroller.setFocusWidget(menuRank);
 				break;
 			}
 			menuRank++;
 		}
 	}
 	
 
 	@Override
 	public void notifyObserver(Observable source) {
 		setFocusWidget(model.getCurrentPageData().getReference());
 	}
 	
 	/**
 	 * @param rank of the widget to retrieve. 1 is the first element.
 	 * @return the {@code number}th widget or null if there's none at this position.
 	 */
 	public NavigationItem getItem(int rank) {
 		int i = 1;
 		for (Iterator<NavigationItem> iterator = tileList.iterator();
 				iterator.hasNext();
 				i++, iterator.next()) {
 			if (i == rank)
 				return  iterator.next();
 		}
 		return null;
 	}
 	
 	/*******************************************************************************/	
 	/******** END - Centralised widget selection ***********************************/
 	/*******************************************************************************/
 	
 	
 	/*******************************************************************************/	
 	/************* Prevent browser's mouse/touch events ****************************/
 	/*******************************************************************************/
 	
 	private HandlerRegistration handlerRegistration = null;
 	
 	private Event.NativePreviewHandler preventDefaultMouseEvents = 
 			new Event.NativePreviewHandler() {
 		@Override
 		public void onPreviewNativeEvent(NativePreviewEvent event) {
 			if (event.getTypeInt() == Event.ONTOUCHSTART 
 					|| event.getTypeInt() == Event.ONTOUCHEND
 					|| event.getTypeInt() == Event.ONTOUCHMOVE 
 					|| event.getTypeInt() == Event.ONMOUSEDOWN
 					|| event.getTypeInt() == Event.ONMOUSEMOVE
 					) {
 				event.getNativeEvent().preventDefault(); 
 			}
 		}
 	};
 	
 	/**
 	 * Variable to avoid the use of multiple preventDefault handlers when both mouse and touch are
 	 * used.
 	 */
 	private boolean defaultsPrevented = false;
 	/**
 	 * The browser steals the mouse events from GWT. It tries to select text
 	 * or copy/paste by drag and drop.
 	 * If you don't want the widget to interfere on this widget call this.
 	 */
 	private void preventBrowserInterference() {
 		addDomHandler(new MouseOverHandler() {
 			public void onMouseOver(MouseOverEvent event) {
 				if (!defaultsPrevented) {
 					handlerRegistration = Event.addNativePreviewHandler(preventDefaultMouseEvents);
 					defaultsPrevented = true;
 				}
 			}
 		}, MouseOverEvent.getType());
 		
 		addDomHandler(new TouchStartHandler() {
 			@Override
 			public void onTouchStart(TouchStartEvent event) {
 				if (!defaultsPrevented) {
 					handlerRegistration = Event.addNativePreviewHandler(preventDefaultMouseEvents);
 					defaultsPrevented = true;
 				}
 			}
 		}, TouchStartEvent.getType());
 
 		addDomHandler(new MouseOutHandler() {
 			public void onMouseOut(MouseOutEvent event) {
 				if (handlerRegistration != null) {
 					handlerRegistration.removeHandler();
 					defaultsPrevented = false;
 				}
 			}
 		}, MouseOutEvent.getType());
 		
 		addDomHandler(new TouchEndHandler() {
 			@Override
 			public void onTouchEnd(TouchEndEvent event) {
 				if (handlerRegistration != null) {
 					handlerRegistration.removeHandler();
 					defaultsPrevented = false;
 				}
 			}
 		}, TouchEndEvent.getType());
 	}
 
 	/*******************************************************************************/
 	/***** END *** Prevent browser's mouse/touch events ****************************/
 	/*******************************************************************************/
 }
 
 
