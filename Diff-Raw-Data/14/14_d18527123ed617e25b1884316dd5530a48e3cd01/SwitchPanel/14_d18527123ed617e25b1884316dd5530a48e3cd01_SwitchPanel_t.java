 /* 
  * Copyright 2012 Matthias Huebner
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.gwtao.ui.widgets.client;
 
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.dom.client.Style.Display;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.ComplexPanel;
 import com.google.gwt.user.client.ui.ProvidesResize;
 import com.google.gwt.user.client.ui.RequiresResize;
 import com.google.gwt.user.client.ui.Widget;
 
 public class SwitchPanel extends ComplexPanel implements RequiresResize, ProvidesResize {
   public SwitchPanel() {
     setElement(DOM.createDiv());
   }
 
   @Override
   public void add(Widget w) {
     prepareWidget(w);
     super.add(w, getElement());
   }
 
   public void insert(Widget w, int beforeIndex) {
     prepareWidget(w);
     super.insert(w, getElement(), beforeIndex, true);
   }
 
   private void prepareWidget(Widget w) {
     Style style = w.getElement().getStyle();
     style.setPosition(Position.RELATIVE);
     style.clearTop();
     style.clearLeft();
     if (getWidgetCount() > 0)
       doHideWidget(w);
     // if (w instanceof ILayoutContainer)
     // ((ILayoutContainer) w).measure();
   }
 
   private void doHideWidget(Widget w) {
     w.setVisible(false);
     w.getElement().getStyle().setDisplay(Display.NONE);
   }
 
   private void doShowWidget(final Widget w) {
    delayedShow(w);
    // new Timer() {
 
    // @Override
    // public void run() {
    // delayedShow(w);
    // }
    // }.schedule(20);
     // Scheduler.get().scheduleDeferred(new ScheduledCommand() {
     // @Override
     // public void execute() {
     // }
     // });
   }
 
   protected void delayedShow(Widget w) {
     w.getElement().getStyle().clearDisplay();
     w.setVisible(true);
 
     // if (w instanceof ILayoutContainer)
     // ((ILayoutContainer) w).measure();
 
     // Element e = getParent().getElement();
     // final String width = e.getClientWidth() + "px";
     // final String height = e.getClientHeight() + "px";
     String width = getOffsetWidth() + "px";
     String height = getOffsetHeight() + "px";
     w.setSize(width, height);
 
     if (w instanceof RequiresResize)
       ((RequiresResize) w).onResize();
   }
 
   @Override
   public void onResize() {
     Element e = getElement();
     final String width = e.getClientWidth() + "px";
     final String height = e.getClientHeight() + "px";
 
     // String width = getOffsetWidth() + "px";
     // String height = getOffsetHeight() + "px";
     for (Widget child : getChildren()) {
       if (child.isVisible()) {
         child.setSize(width, height);
         if (child instanceof RequiresResize) {
           ((RequiresResize) child).onResize();
         }
       }
     }
   }
 
   public void showWidget(int index) {
     if (index < 0 || index >= getWidgetCount())
       throw new IndexOutOfBoundsException("index=" + index + " size=" + getWidgetCount());
     if (index != getVisibleIndex()) {
       Widget current = getVisibleWidget();
       if (current != null)
         doHideWidget(current);
     }
     doShowWidget(getWidget(index));
   }
 
   public void showWidget(Widget w) {
     showWidget(getWidgetIndex(w));
   }
 
   public int getVisibleIndex() {
     int idx = 0;
     for (Widget child : getChildren()) {
       if (child.isVisible())
         return idx;
       ++idx;
     }
     return -1;
   }
 
   public Widget getVisibleWidget() {
     for (Widget child : getChildren()) {
       if (child.isVisible())
         return child;
     }
     return null;
   }
 }
