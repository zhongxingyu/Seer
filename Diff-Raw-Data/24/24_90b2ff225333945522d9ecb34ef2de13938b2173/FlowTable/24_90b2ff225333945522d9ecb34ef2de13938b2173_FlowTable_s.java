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
 
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Style.Float;
 import com.google.gwt.dom.client.Style.Overflow;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.user.client.ui.ComplexPanel;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * A container widget that implements the Glom details view flow table behaviour. Child widgets are arranged using the
  * least vertical space in the specified number of columns.
  * 
  * This class is currently implemented as a {@link Composite} widget. It would be more efficient to subclass
  * {@link Panel} or {@link ComplexPanel} and implement this class at a lower level but we'd loose the ability to easily
  * debug the code. This is something to consider when looking for optimisations.
  * 
  * @author Ben Konrath <ben@bagu.org>
  */
 public class FlowTable extends Composite {
 
 	// Represents an item to be inserted into the FlowTable. The primary reason for this class is to cache the vertical
 	// height of the widget being added to the FlowTable.
 	class FlowTableItem implements IsWidget {
 
 		Widget widget;
 		int height;
 
 		@SuppressWarnings("unused")
 		private FlowTableItem() {
 			// disable default constructor
 		}
 
 		FlowTableItem(Widget widget) {
 			// Get the vertical height with decorations by temporarily adding the widget to the body element of the
 			// document in a transparent container. This is required because the size information is only available when
 			// the widget is attached to the DOM. The size information must be obtained before the widget is added to
 			// column because adding a widget to a container automatically removes it from the previous container.
 			Document doc = Document.get();
 			com.google.gwt.dom.client.Element div = doc.createDivElement();
 			div.getStyle().setOpacity(0.0);
 			div.appendChild(widget.getElement().<com.google.gwt.user.client.Element> cast());
 			doc.getBody().appendChild(div);
 			height = widget.getOffsetHeight();
 			doc.getBody().removeChild(div);
 			this.widget = widget;
 		}
 
 		int getHeight() {
 			return height;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see com.google.gwt.user.client.ui.IsWidget#asWidget()
 		 */
 		@Override
 		public Widget asWidget() {
 			return widget;
 		}
 	}
 
 	private FlowPanel mainPanel = new FlowPanel();
 	private ArrayList<FlowPanel> columns = new ArrayList<FlowPanel>();
 	private ArrayList<FlowTableItem> items = new ArrayList<FlowTableItem>();
 
 	@SuppressWarnings("unused")
 	private FlowTable() {
 		// disable default constructor
 	}
 
 	public FlowTable(int columnCount) {
 		// set the overflow properly so that the columns can be arranged properly
 		mainPanel.getElement().getStyle().setOverflow(Overflow.HIDDEN);
 		mainPanel.getElement().getStyle().setWidth(100, Unit.PCT);
 
		// create the columns
 		for (int i = 0; i < columnCount; i++) {
 			FlowPanel column = new FlowPanel();
			column.getElement().getStyle().setFloat(Float.LEFT);
			// The columns widths are evenly distributed amongst the number of columns
			column.getElement().getStyle().setWidth(100 / columnCount, Unit.PCT);
 			columns.add(column);
 			mainPanel.add(column);
 		}
 
 		initWidget(mainPanel);
 	}
 
 	/**
 	 * Adds a Widget to the FlowTable. The layout of the child widgets is adjusted to minimize the vertical height of
 	 * the entire FlowTable.
 	 * 
 	 * @param widget
 	 *            widget to add to the FlowTable
 	 */
 	public void add(Widget widget) {
 
 		// keep track for the child items
 		items.add(new FlowTableItem(widget));
 
 		// Discover the total amount of minimum space needed by this container widget, by examining its child widgets,
 		// by examining every possible sequential arrangement of the widgets in this fixed number of columns:
 		int minColumnHeight = getMinimumColumnHeight(0, columns.size()); // This calls itself recursively.
 
 		// Rearrange the widgets taking the newly added widget into account.
 		int currentColumnIndex = 0;
 		int currentColumnHeight = 0;
 		FlowPanel currentColumn = columns.get(currentColumnIndex);
 		for (FlowTableItem item : items) {
 			if (currentColumnHeight + item.getHeight() > minColumnHeight) {
 				// Ensure that we never try to add widgets to an existing column. This shouldn't happen so it's just a
 				// precaution. TODO: log a message if columnNumber is greater than columns.size()
 				if (currentColumnIndex < columns.size() - 1) {
 					currentColumn = columns.get(++currentColumnIndex);
 					currentColumnHeight = 0;
 				}
 			}
 			currentColumn.add(item.asWidget()); // adding the widget to the column removes it from its current container
 			currentColumnHeight += item.getHeight();
 		}
 	}
 
 	/*
 	 * Discover how best (least column height) to arrange these widgets in these columns, keeping them in sequence, and
 	 * then say how high the columns must be.
 	 * 
 	 * This method was ported from the FlowTable class of Glom.
 	 */
 	private int getMinimumColumnHeight(int startWidget, int columnCount) {
 
 		if (columnCount == 1) {
 			// Just add the heights together:
 			int widgetsCount = items.size() - startWidget;
 			return getColumnHeight(startWidget, widgetsCount);
 
 		} else {
 			// Try each combination of widgets in the first column, combined with the the other combinations in the
 			// following columns:
 			int minimumColumnHeight = 0;
 			boolean atLeastOneCombinationChecked = false;
 
 			int countItemsRemaining = items.size() - startWidget;
 
 			for (int firstColumnWidgetsCount = 1; firstColumnWidgetsCount <= countItemsRemaining; firstColumnWidgetsCount++) {
 				int firstColumnHeight = getColumnHeight(startWidget, firstColumnWidgetsCount);
 				int minimumColumnHeightSoFar = firstColumnHeight;
 				int othersColumnStartWidget = startWidget + firstColumnWidgetsCount;
 
 				// Call this function recursively to get the minimum column height in the other columns, when these
 				// widgets are in the first column:
 				int minimumColumnHeightNextColumns = 0;
 				if (othersColumnStartWidget < items.size()) {
 					minimumColumnHeightNextColumns = getMinimumColumnHeight(othersColumnStartWidget, columnCount - 1);
 					minimumColumnHeightSoFar = Math.max(firstColumnHeight, minimumColumnHeightNextColumns);
 				}
 
 				// See whether this is better than the last one:
 				if (atLeastOneCombinationChecked) {
 					if (minimumColumnHeightSoFar < minimumColumnHeight) {
 						minimumColumnHeight = minimumColumnHeightSoFar;
 					}
 				} else {
 					minimumColumnHeight = minimumColumnHeightSoFar;
 					atLeastOneCombinationChecked = true;
 				}
 			}
 
 			return minimumColumnHeight;
 		}
 	}
 
 	private int getColumnHeight(int startWidget, int widgetCount) {
 		// Just add the heights together:
 		int columnHeight = 0;
 		for (int i = startWidget; i < (startWidget + widgetCount); i++) {
 			FlowTableItem item = items.get(i);
 			int itemHeight = item.getHeight();
 			columnHeight += itemHeight;
 		}
 		return columnHeight;
 	}
 
 }
