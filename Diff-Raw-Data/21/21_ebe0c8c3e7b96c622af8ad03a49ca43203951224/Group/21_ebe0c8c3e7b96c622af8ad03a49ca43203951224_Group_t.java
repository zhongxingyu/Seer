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
 
 import java.util.ArrayList;
 
 import org.glom.web.shared.layout.LayoutGroup;
 import org.glom.web.shared.layout.LayoutItem;
 import org.glom.web.shared.layout.LayoutItemField;
 import org.glom.web.shared.layout.LayoutItemNotebook;
 import org.glom.web.shared.layout.LayoutItemPortal;
 
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author Ben Konrath <ben@bagu.org>
  */
 public class Group extends Composite {
 	private FlowPanel mainPanel = new FlowPanel();
 	private final ArrayList<DetailsCell> cells = new ArrayList<DetailsCell>();
 	private final ArrayList<Portal> portals = new ArrayList<Portal>();
 
 	protected Group() {
 		// can used by sub-classes
 	}
 
 	/**
 	 * Creates a new widget for a main Group in the Details View.
 	 * 
 	 * @param layoutGroup
 	 *            The DTO that holds the Group or sub-Group layout information
 	 */
 	public Group(LayoutGroup layoutGroup) {
 		// is default is a group that is not a sub
 		this(layoutGroup, false, true);
 	}
 
 	/**
 	 * Creates a new widget for a Group or sub-Group.
 	 * 
 	 * @param layoutGroup
 	 *            The DTO that holds the Group or sub-Group layout information
 	 * @param subGroup
 	 *            true if the layoutGroup is a sub-Group, false if it's a Group
 	 * @param setGroupTitle
 	 *            whether or not to add a title label for the Group
 	 */
 	private Group(LayoutGroup layoutGroup, boolean subGroup, boolean setGroupTitle) {
 
 		mainPanel.setStyleName(subGroup ? "subgroup" : "group");
 
 		FlowPanel groupContents;
 		String groupTitle = layoutGroup.getTitle();
 
 		if (setGroupTitle && !groupTitle.isEmpty()) {
 			Label label = new Label(groupTitle);
 			mainPanel.add(label);
 
 			label.setStyleName(subGroup ? "subgroup-title" : "group-title");
 
 			groupContents = new FlowPanel();
 			groupContents.setStyleName("group-contents");
 			mainPanel.add(groupContents);
 		} else {
 			// Don't make a separate contents panel when the group title is not being set.
 			groupContents = mainPanel;
 		}
 
 		FlowTable flowtable = new FlowTable(layoutGroup.getColumnCount());
 		for (LayoutItem layoutItem : layoutGroup.getItems()) {
 			Widget child = createChildWidget(layoutItem, true);
 			flowtable.add(child);
 		}
 		groupContents.add(flowtable);
 		initWidget(mainPanel);
 	}
 
 	public ArrayList<DetailsCell> getCells() {
 		return cells;
 	}
 
 	public ArrayList<Portal> getPortals() {
 		return portals;
 	}
 
 	/**
 	 * Creates a child widget for the specified LayoutItem and updates the cells and portals field appropriately. This
 	 * can be used by subclasses like {@link Notebook}.
 	 * 
 	 * @param layoutItem
 	 *            The DTO that holds the layout information
 	 * @param addGroupTitle
 	 *            whether or not to add a title label for the Group
 	 */
 	protected Widget createChildWidget(LayoutItem layoutItem, boolean addGroupTitle) {
 
 		if (layoutItem instanceof LayoutItemField) {
 
 			// create a DetailsCell
 			DetailsCell detailsCell = new DetailsCell((LayoutItemField) layoutItem);
 			cells.add(detailsCell);
 			return detailsCell;
 
 		} else if (layoutItem instanceof LayoutGroup) {
 
 			if (layoutItem instanceof LayoutItemPortal) {
 
 				// create a Portal
				Portal portal = new Portal((LayoutItemPortal) layoutItem, addGroupTitle);
 				portals.add(portal);
 				return portal;
 
 			} else if (layoutItem instanceof LayoutItemNotebook) {
 
 				// create a Notebook
 				Notebook notebook = new Notebook((LayoutItemNotebook) layoutItem);
 				cells.addAll(notebook.getCells());
 				portals.addAll(notebook.getPortals());
 				return notebook;
 
 			} else {
 
 				// create a subgroup Group
 				Group subGroup = new Group((LayoutGroup) layoutItem, true, addGroupTitle);
 				cells.addAll(subGroup.getCells());
 				portals.addAll(subGroup.getPortals());
 				return subGroup;
 			}
 		}
 
 		return null; // This should never happen.
 	}
 
 	/**
 	 * Allows subclasses to access and use the main panel widget.
 	 * 
 	 * @return the main FlowPanel
 	 */
 	protected FlowPanel getMainPanel() {
 		return mainPanel;
 	}
 
 }
