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
 
 import org.glom.web.shared.layout.LayoutItemPortal;
 
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author Ben Konrath <ben@bagu.org>
  * 
  */
 public class Portal extends Composite {
 
 	private FlowPanel contents = new FlowPanel();
 	private LayoutItemPortal layoutItem;
 
 	@SuppressWarnings("unused")
 	private Portal() {
 		// disable default constructor
 	}
 
 	/**
 	 * Creates a new widget for a Portal.
 	 * 
 	 * @param layoutItem
 	 *            The DTO that holds the Portal layout information
	 * @param setTitle
	 *            true if the title should be set, false otherwise
 	 */
	public Portal(LayoutItemPortal layoutItemPortal, boolean setTitle) {
 		layoutItem = layoutItemPortal;
 
 		FlowPanel mainPanel = new FlowPanel();
 		mainPanel.setStyleName("subgroup");
 
		if (setTitle) {
			Label title = new Label(layoutItem.getTitle());
			title.setStyleName("subgroup-title");
			mainPanel.add(title);
		}
 
 		contents.setStyleName("group-contents");
 
 		mainPanel.add(contents);
 		initWidget(mainPanel);
 	}
 
 	public void setContents(Widget widget) {
 		contents.clear();
 		contents.add(widget);
 	}
 
 	/**
 	 * @return
 	 */
 	public LayoutItemPortal getLayoutItem() {
 		return layoutItem;
 	}
 
 }
