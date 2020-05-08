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
 
 package ch.sebastienzurfluh.client.view.tilemenu;
 
 import java.util.Collection;
 
 import ch.sebastienzurfluh.client.control.ModelAsyncPlug;
 import ch.sebastienzurfluh.client.control.eventbus.Event;
 import ch.sebastienzurfluh.client.control.eventbus.EventBus;
 import ch.sebastienzurfluh.client.control.eventbus.Event.EventType;
 import ch.sebastienzurfluh.client.control.eventbus.events.DataType;
 import ch.sebastienzurfluh.client.control.eventbus.events.PageChangeEvent;
 import ch.sebastienzurfluh.client.model.Model;
 import ch.sebastienzurfluh.client.model.structure.Data;
 import ch.sebastienzurfluh.client.model.structure.DataReference;
 import ch.sebastienzurfluh.client.model.structure.MenuData;
 import ch.sebastienzurfluh.client.view.menuinterface.MenuWidget;
 import ch.sebastienzurfluh.client.view.menuinterface.PageRequestHandler;
 import ch.sebastienzurfluh.client.view.tilemenu.Tile.TileMode;
 
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 
 /**
  * This widget uses several {@link TileMenu}s to create a menu.
  * @author Sebastien Zurfluh
  *
  */
 public class TileWidget extends VerticalPanel implements MenuWidget {
 	private String stylePrimaryName = "tileWidget";
 	private TileMenu bookletMenu;
 	private TileMenu chapterMenu;
 	private TileMenu pageMenu;
 	private Model model;
 	
 	public TileWidget(EventBus pageChangeEventBus, PageRequestHandler pageRequestHandler, Model model) {
 		this.model = model;
 		
 		setStyleName(stylePrimaryName);
 		
 		initialise(pageRequestHandler);
 		
 		setDefaults();
 		
 		pageChangeEventBus.addListener(this);
 	}
 	
 	private void initialise(PageRequestHandler pageRequestHandler) {
 		pageMenu = new TileMenu("Pages", pageRequestHandler);
 		add(pageMenu);
 		chapterMenu = new TileMenu("Chapters", pageRequestHandler);
 		add(chapterMenu);
 		bookletMenu = new TileMenu("Booklets", pageRequestHandler);
 		add(bookletMenu);
 	}
 	
 	private void setDefaults() {
 		chapterMenu.setVisible(false);
 		pageMenu.setVisible(false);
 	}
 
 	@Override
 	public EventType getEventType() {
 		return EventType.PAGE_CHANGE_EVENT;
 	}
 
 	@Override
 	public void notify(Event e) {
 		if(e instanceof PageChangeEvent) {
 			PageChangeEvent pageChangeEvent = (PageChangeEvent) e;
 			
 			// Change layout according to the new page type.
 			switch (pageChangeEvent.getPageType()) {
 			case PAGE:
 				pageMenu.setVisible(true);
 				pageMenu.setMode(TileMode.ICON_ONLY);
 				chapterMenu.setVisible(true);
 				chapterMenu.setMode(TileMode.ICON_ONLY);
 				bookletMenu.setMode(TileMode.ICON_ONLY);
 				break;
 			case CHAPTER:
 				pageMenu.setVisible(true);
 				chapterMenu.setVisible(true);
 				bookletMenu.setMode(TileMode.ICON_ONLY);
 				break;
 			case BOOKLET:
 				pageMenu.setVisible(false);
 				chapterMenu.setVisible(true);
 				bookletMenu.setMode(TileMode.ICON_ONLY);
 				break;
 			case SUPER:
 				pageMenu.setVisible(false);
 				chapterMenu.setVisible(false);
 				bookletMenu.setMode(TileMode.DETAILED);
 				break;
 			default:
 				break;
 			}
 			
 			// Reload the tiles as necessary
 			Data data = pageChangeEvent.getData();
 			switch (pageChangeEvent.getPageType()) {
 			case SUPER:
 				model.getMenus(new ModelAsyncPlug<Collection<MenuData>>() {
					public void update(Collection<MenuData> menus) {
						reloadTiles(bookletMenu, menus);
 					};
 				}, DataType.BOOKLET);
 				break;
 			case BOOKLET:
 				// list the booklet's chapters
 				reloadTiles(chapterMenu, data.getReference());
 				break;
 			case CHAPTER:
 				// list the chapter's pages
 				reloadTiles(pageMenu, data.getReference());
 				break;
 			default:
 				break;
 			}
 		}
 	}
 	
 	private void reloadTiles (final TileMenu menu, DataReference parentReference) {
 		menu.clearTiles();
 		model.getSubMenus(new ModelAsyncPlug<Collection<MenuData>>() {
 			@Override
 			public void update(Collection<MenuData> data) {
				if (data.isEmpty()) {
					menu.setVisible(false);
					return;
				}	
 				for (MenuData menuData : data) {
 					menu.addTile(menuData);
 				}
				
 			}
 		}, parentReference);
 	}
 	
 	private void reloadTiles (TileMenu menu, Collection<MenuData> menus) {
 		menu.clearTiles();
 		for (MenuData menuData : menus) {
 			menu.addTile(menuData);
 		}
 	}
 }
