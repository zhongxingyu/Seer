 package net.jorgeherskovic.medrec.client;
 
 /*
  * Copyright 2009 Fred Sauer
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 /* MODIFIED BY JORGE R. HERSKOVIC */
 
 import net.jorgeherskovic.medrec.client.event.RowDroppedEvent;
 
 import com.google.gwt.event.shared.SimpleEventBus;
 import com.google.gwt.user.client.ui.InsertPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import com.allen_sauer.gwt.dnd.client.DragContext;
 import com.allen_sauer.gwt.dnd.client.drop.AbstractPositioningDropController;
 import com.allen_sauer.gwt.dnd.client.util.CoordinateLocation;
 import com.allen_sauer.gwt.dnd.client.util.DOMUtil;
 import com.allen_sauer.gwt.dnd.client.util.Location;
 import com.allen_sauer.gwt.dnd.client.util.LocationWidgetComparator;
 import com.allen_sauer.gwt.dnd.client.util.WidgetLocation;
 
 /**
  * Allows one or more table rows to be dropped into an existing table.
  */
 public final class FlexTableRowDropController extends
 		AbstractPositioningDropController {
 
 	private static final String CSS_DEMO_TABLE_POSITIONER = "med-table-positioner";
 
 	private DraggableFlexTable flexTable;
 	private SimpleEventBus bus;
 
 	private InsertPanel flexTableRowsAsIndexPanel = new InsertPanel() {
 
 		public void add(Widget w) {
 			throw new UnsupportedOperationException();
 		}
 
 		public Widget getWidget(int index) {
 			return flexTable.getWidget(index, 0);
 		}
 
 		public int getWidgetCount() {
 			return flexTable.getRowCount();
 		}
 
 		public int getWidgetIndex(Widget child) {
 			throw new UnsupportedOperationException();
 		}
 
 		public void insert(Widget w, int beforeIndex) {
 			throw new UnsupportedOperationException();
 		}
 
 		public boolean remove(int index) {
 			throw new UnsupportedOperationException();
 		}
 	};
 
 	private Widget positioner = null;
 
 	private int targetRow;
 
 	public FlexTableRowDropController(DraggableFlexTable flexTable,
 			SimpleEventBus bus) {
 		super(flexTable);
 		this.flexTable = flexTable;
 		this.bus = bus;
 	}
 
 	@Override
 	public void onDrop(DragContext context) {
 		FlexTableRowDragController trDragController = (FlexTableRowDragController) context.dragController;
 		bus.fireEvent(new RowDroppedEvent(trDragController.getDraggableTable(),
 				flexTable, trDragController.getDragRow(), targetRow + 1));
 		//FlexTableUtil.moveRow(trDragController.getDraggableTable(), flexTable,
 		//		trDragController.getDragRow(), targetRow + 1);
 		super.onDrop(context);
 	}
 
 	@Override
 	public void onEnter(DragContext context) {
 		super.onEnter(context);
 		positioner = newPositioner(context);
 	}
 
 	@Override
 	public void onLeave(DragContext context) {
 		positioner.removeFromParent();
 		positioner = null;
 		super.onLeave(context);
 	}
 
 	@Override
 	public void onMove(DragContext context) {
 		super.onMove(context);
 		targetRow = DOMUtil.findIntersect(flexTableRowsAsIndexPanel,
 				new CoordinateLocation(context.mouseX, context.mouseY),
 				LocationWidgetComparator.BOTTOM_HALF_COMPARATOR) - 1;
 
 		if (flexTable.getRowCount() > 0) {
 			//Widget w = flexTable.getWidget(targetRow == -1 ? 0 : targetRow, 0);
 			if (targetRow<0) {
 				// We don't want people dropping stuff on top of the headers.
 				return;
 			}
 			//Location widgetLocation = new WidgetLocation(w,
 			//		context.boundaryPanel);
 			Location tableLocation = new WidgetLocation(flexTable,
 					context.boundaryPanel);
			positioner.setHeight(flexTable.getCumulativeOffsetHeight(targetRow)+"px");
 			context.boundaryPanel.add(
 					positioner,
 					tableLocation.getLeft(),
 					tableLocation.getTop() + flexTable.getCumulativeOffsetHeight(targetRow) - 2
 							//+ w.getOffsetHeight());
 					);
 		}
 	}
 
 	Widget newPositioner(DragContext context) {
 		Widget p = new SimplePanel();
 		p.addStyleName(CSS_DEMO_TABLE_POSITIONER);
 		p.setPixelSize(flexTable.getOffsetWidth(), 1);
 		return p;
 	}
 }
