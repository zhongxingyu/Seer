 /*
  * Copyright (C) 2011 Openismus GmbH
  *
  * This file is part of GWT-Glom.
  *
  * GWT-Glom is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.glom.web.client.ui.details;
 
 import org.glom.web.client.Utils;
 import org.glom.web.shared.layout.LayoutGroup;
 import org.glom.web.shared.layout.LayoutItem;
 import org.glom.web.shared.layout.LayoutItemNotebook;
 
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.TabLayoutPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author Ben Konrath <ben@bagu.org>
  * 
  */
 public class Notebook extends Group {
 
 	@SuppressWarnings("unused")
 	private Notebook() {
 		// disable default constructor
 	}
 
 	/**
 	 * Create a new Notebook widget based on the specified LayoutItemNotebook DTO.
 	 * 
 	 * @param layoutItemNotebook
 	 */
 	public Notebook(LayoutItemNotebook layoutItemNotebook) {
 		TabLayoutPanel tabPanel = new TabLayoutPanel(1.6, Unit.EM);
 
 		int maxChildHeight = 0;
 		for (LayoutItem layoutItem : layoutItemNotebook.getItems()) {
 			if (!(layoutItem instanceof LayoutGroup))
 				// Ignore non-LayoutGroup items. This is what Glom 1.18 does.
 				continue;
 
 			Widget child = createChildWidget(layoutItem, false, false);
 
 			// update the maximum value of the child height if required
 			int childHeight = Utils.getWidgetHeight(child);
 			if (childHeight > maxChildHeight)
 				maxChildHeight = childHeight;
 
			tabPanel.add(child, layoutItem.getTitle().isEmpty() ? layoutItem.getName() : layoutItem.getTitle());
 		}
 
 		// Set the first tab as the default tab.
 		tabPanel.selectTab(0);
 
 		// The height needs to be set of the TabLayoutPanel to work.
 		// Use the max child height plus a few extra pixels for padding.
 		tabPanel.setHeight((maxChildHeight + 6) + "px");
 
 		FlowPanel mainPanel = getMainPanel();
 		mainPanel.add(tabPanel);
 		initWidget(mainPanel);
 	}
 }
