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
 
 package ch.sebastienzurfluh.client.view.TileMenu;
 
 import java.util.HashMap;
 
 import ch.sebastienzurfluh.client.model.structure.MenuData;
 import ch.sebastienzurfluh.client.view.MenuInterface.MenuList;
 import ch.sebastienzurfluh.client.view.MenuInterface.PageRequestHandler;
 import ch.sebastienzurfluh.client.view.TileMenu.Tile.TileMode;
 
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 
 /**
  * This widget uses several {@link Tile}s to create a menu.
  * @author Sebastien Zurfluh
  *
  */
 public class TileMenu extends VerticalPanel implements MenuList {
 	private String stylePrimaryName = "tileMenu";
 	private FlowPanel tilePanel;
 	private HashMap<Integer, Tile> tileOrderList;
 	private ModeSwapButton button;
 	private PageRequestHandler pageRequestHandler;
 	
 	public TileMenu(String title, PageRequestHandler pageRequestHandler) {
 		this.pageRequestHandler = pageRequestHandler;
 		
 		setStyleName(stylePrimaryName);
 		
 		HorizontalPanel firstLine = new HorizontalPanel();
 		Label titleLabel = new Label(title);
 		titleLabel.setStyleName(stylePrimaryName + "-" + "title");
 		firstLine.add(titleLabel);
 		
 		button = new ModeSwapButton(stylePrimaryName, this);

		setStyleName(stylePrimaryName + "-" + "detailButton");
 		
 		firstLine.add(button);
 		add(firstLine);
 		
 		tilePanel = new FlowPanel();
 		tileOrderList = new HashMap<Integer, Tile>();
 		tilePanel.setStyleName(stylePrimaryName + "-" + "tileList");
 		add(tilePanel);
 	}
 	
 	public void addTile(MenuData menuData) {
 		Tile tile = new Tile(menuData);
 		tile.addClickHandler(pageRequestHandler);
 		tile.setMode(currentMode);
 		tileOrderList.put(menuData.getPriorityNumber(), tile);
 		// TODO order the tiles in the menu according to their priority number
 		tilePanel.add(tile);
 	}
 	
 	public void clearTiles() {
 		//TODO test that. It didn't work before... maybe use a new tilePanel.
 		tilePanel.clear();
 		tileOrderList.clear();
 	}
 	
 	private TileMode currentMode = TileMode.ICON_ONLY;
 	public void setMode(TileMode mode) {
 		if (mode.equals(currentMode))
 			return;
 		System.out.println("TileMenu: mode change.");
 		currentMode = mode;
 		System.out.println("TileMenu: current mode: "+mode);
 		if (mode.equals(TileMode.DETAILED)) {
 			button.setState(TwoStatesImageButton.State.ONE);
 		} else {
 			button.setState(TwoStatesImageButton.State.TWO);
 		}
 		for (Tile tile : tileOrderList.values()) {
 			tile.setMode(mode);
 		}
 	}
 }
