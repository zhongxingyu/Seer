 /*
  * Copyright 2010 kk-electronic a/s. 
  * 
  * This file is part of KKPortal.
  *
  * KKPortal is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * KKPortal is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with KKPortal.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.kk_electronic.kkportal.core.ui;
 
import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.layout.client.Layout;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.inject.Inject;
 import com.kk_electronic.kkportal.core.dnd.DND;
 import com.kk_electronic.kkportal.core.dnd.DragSource;
 import com.kk_electronic.kkportal.core.dnd.DND.DropSink;
import com.kk_electronic.kkportal.core.util.Pair;
 
 public class GroupDisplay<T extends IsWidget & KnownHeight & DragSource> implements DropSink<T>,IsWidget {
 	LayoutPanel canvas = new LayoutPanel();
 	private final DND<T> dnd;
 	private double margin = 1;
 	private Unit marginunit = Unit.EM;
 	
 	public static interface Handler<T>{
 		public void onElementDrop(double x, int y, T element);
 	}
 	
 	private Handler<T> handler;
 	
 	public void setHandler(Handler<T> handler) {
 		this.handler = handler;
 	}
 
 	@Inject
 	public GroupDisplay(DND<T> dnd) {
 		this.dnd = dnd;
 		dnd.registerDropSink(this);
 	}
 	
 	private native Layout iHateJavaProtection(LayoutPanel layoutPanel) /*-{
 		return layoutPanel.@com.google.gwt.user.client.ui.LayoutPanel::layout;
 	}-*/;
 	
 	HashSet<Widget> displayed = new HashSet<Widget>();
 	
 	public void setWidgets(List<List<T>> widgets){
 		if(canvas.isAttached() == false){
 			return;
 		}
 		if(widgets == null){
 			canvas.clear();
 			displayed.clear();
 			return;
 		}
 		HashSet<Widget> needRemoval = new HashSet<Widget>(displayed);
 	
 		Layout l = iHateJavaProtection(canvas);
 		
 		double x = margin * l.getUnitSize(marginunit, false)/l.getUnitSize(Unit.PCT, false);
 		double y = margin * l.getUnitSize(marginunit, true);
 		
 		double left = x;
 		for(List<T> column:widgets){
 			double top = y;
 			//TODO: Support weighted column sizes
 			double width = (100.0-x) / widgets.size()-x;
 			for(final T face:column){
 				final Widget widget = face.asWidget();
 				int height = face.getDesiredHeight();
 				if(!displayed.contains(widget)){
 					canvas.add(widget);
 					canvas.getWidgetContainerElement(widget).getStyle().clearOverflow();
 					displayed.add(widget);
 					face.getDragHandle().addMouseDownHandler(new MouseDownHandler() {
 						
 						@Override
 						public void onMouseDown(MouseDownEvent event) {
 							dnd.startDrag(event, face);
 						}
 					});
 				}
 				canvas.setWidgetLeftWidth(widget, left, Unit.PCT, width, Unit.PCT);
 				canvas.setWidgetTopHeight(widget, top, Unit.PX, height, Unit.PX);
 				top += height+y;
 				needRemoval.remove(face);
 			}
 			left += width + x;
 		}
 		for(Widget toRemove : needRemoval){
 			canvas.remove(toRemove);
 			displayed.remove(toRemove);
 			//TODO:remove handler
 		}
 		canvas.animate(150);
 	}
 	
 	@Override
 	public boolean Drop(MouseUpEvent event, final T element) {
 		Widget widget = element.asWidget();
 		int height = widget.getOffsetHeight();
 		event.setRelativeElement(canvas.getElement());
 		final int x = event.getX();
 		final int y = event.getY();
 		/*
 		 * Note must be 100.0 and not 100 since the later would result in
 		 * integer division which most likely would be 0
 		 */
 		double width = (100.0 * widget.getOffsetWidth()) / canvas.getOffsetWidth();
 		final int top = widget.getAbsoluteTop() - canvas.getAbsoluteTop();
 		final double left = (100.0 * (widget.getAbsoluteLeft() - canvas.getAbsoluteLeft()))
 				/ canvas.getOffsetWidth();
 		final double xpct = 100.0 * x / canvas.getOffsetWidth();
 		canvas.add(widget);
 		canvas.getWidgetContainerElement(widget).getStyle().clearOverflow();
 		canvas.setWidgetTopHeight(widget, top, Unit.PX, height, Unit.PX);
 		canvas.setWidgetLeftWidth(widget, left, Unit.PCT, width, Unit.PCT);
 		if (handler != null) {
 			Scheduler.get().scheduleDeferred(new Command() {
 
 				@Override
 				public void execute() {
 					handler.onElementDrop(xpct, y, element);
 				}
 			});
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public Widget asWidget() {
 		return canvas;
 	}
 	
 	public boolean checkForResizes() {
 		if (canvas.getWidgetCount() == 0)
 			return false;
 		//ArrayList<Pair<Integer, Integer>> worklist = new ArrayList<Pair<Integer,Integer>>();
 		
 		boolean newSizes = false;
 		for (int i = 0, l = canvas.getWidgetCount(); i < l; i++) {
 			Widget widget = canvas.getWidget(i);
 
 			int desiredHeight = 0;
 			if (widget instanceof KnownHeight) {
 				KnownHeight heightWidget = (KnownHeight) widget;
 				desiredHeight = heightWidget.getDesiredHeight();
 				if (desiredHeight != heightWidget.getLastHeight()) {
 					//worklist.add(new Pair(heightWidget.getId(), desiredHeight));
 					heightWidget.saveHeight(desiredHeight);
 					newSizes = true;
 				}
 			}
 		}
 		return newSizes;
 	}
 }
