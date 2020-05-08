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
 
 import org.glom.web.client.StringUtils;
 import org.glom.web.client.Utils;
 import org.glom.web.shared.layout.LayoutGroup;
 import org.glom.web.shared.layout.LayoutItem;
 import org.glom.web.shared.layout.LayoutItemNotebook;
 
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TabLayoutPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
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
 		// The height of the TabLayoutPanel needs to be set to make the child widgets visible. The height of the tab bar
 		// also needs to be explicitly set. To work around these constraints, we're setting the tab height based on the
 		// tab text and decorations as set in the CSS. The height of the TabLayoutPanel will be set to be the height of
 		// the tab panel plus the height of the decorated tab content panel plus the height of the tallest child widget.
 
 		int tabBarHeight = getDecoratedTabBarHeight();
 		int emptyTabContentHeight = getDecoratedEmptyTabContentHeight();
 
 		TabLayoutPanel tabPanel = new TabLayoutPanel(tabBarHeight, Unit.PX);
 
 		int maxChildHeight = 0;
 		for (LayoutItem layoutItem : layoutItemNotebook.getItems()) {
 			if (!(layoutItem instanceof LayoutGroup))
 				// Ignore non-LayoutGroup items. This is what Glom 1.18 does.
 				continue;
 
 			// child groups of Notebooks shouldn't show their titles
 			Widget child = createChildWidget(layoutItem, false);
 
 			// update the maximum value of the child height if required
 			int childHeight = Utils.getWidgetHeight(child);
 			if (childHeight > maxChildHeight)
 				maxChildHeight = childHeight;
 
 			// Use the name if the title is empty. This avoids having tabs with empty labels.
 			tabPanel.add(child,
 					StringUtils.isEmpty(layoutItem.getTitle()) ? layoutItem.getName() : layoutItem.getTitle());
 		}
 
 		// Set the first tab as the default tab.
 		tabPanel.selectTab(0);
 
 		// Use the max child height plus the height of the tab bar and the height of an empty content panel.
 		tabPanel.setHeight((tabBarHeight + emptyTabContentHeight + maxChildHeight) + "px");
 
 		initWidget(tabPanel);
 	}
 
 	/*
 	 * Gets the height of the tab bar with decorations. The height is determined by the decorations on the tab label
 	 * text and the padding around the tab labels.
 	 */
 	private int getDecoratedTabBarHeight() {
 		// There's no way to get the tab bar panel from the TabLayoutPanel widget. We have to manually build a widget
 		// with the same structure and CSS class names to get the decorated height.
 
 		// Create the widgets that make up the Tabs panel.
 		SimplePanel tabLayoutPanel = new SimplePanel();
 		tabLayoutPanel.setStyleName("gwt-TabLayoutPanel");
 		SimplePanel tabLayoutPanelTabs = new SimplePanel();
 		tabLayoutPanelTabs.setStyleName("gwt-TabLayoutPanelTabs");
 		SimplePanel tabLayoutPanelTab = new SimplePanel();
 		tabLayoutPanelTab.setStyleName("gwt-TabLayoutPanelTab");
 		SimplePanel tabLayoutPanelTabInner = new SimplePanel();
 		tabLayoutPanelTabInner.setStyleName("gwt-TabLayoutPanelTabInner");
 		Label tabHeightHackLabel = new Label("A");
 
 		// Build the widget structure.
 		tabLayoutPanelTabInner.add(tabHeightHackLabel);
 		tabLayoutPanelTab.add(tabLayoutPanelTabInner);
 		tabLayoutPanelTabs.add(tabLayoutPanelTab);
 		tabLayoutPanel.add(tabLayoutPanelTabs);
 
 		// Return the height
 		return Utils.getWidgetHeight(tabLayoutPanel);
 	}
 
 	/*
 	 * Gets the height of the tab content panel with decorations. The height is determined by the decorations tab
 	 * content panel.
 	 */
 	private int getDecoratedEmptyTabContentHeight() {
 		// There's no way to get the tab content panel from the TabLayoutPanel widget. We have to manually build a
 		// widget with the same structure and CSS class names to get the decorated height.
 
 		// Create the widgets that make up the tab content panel.
 		SimplePanel tabLayoutPanel = new SimplePanel();
 		tabLayoutPanel.setStyleName("gwt-TabLayoutPanel");
 		SimplePanel tabContentPanel = new SimplePanel();
 		tabContentPanel.setStyleName("subgroup");
 		tabContentPanel.addStyleName("gwt-TabLayoutPanelContent");
 
 		// Build the widget structure.
 		tabLayoutPanel.add(tabContentPanel);
 
 		// Return the height
 		return Utils.getWidgetHeight(tabLayoutPanel);
 	}
 }
