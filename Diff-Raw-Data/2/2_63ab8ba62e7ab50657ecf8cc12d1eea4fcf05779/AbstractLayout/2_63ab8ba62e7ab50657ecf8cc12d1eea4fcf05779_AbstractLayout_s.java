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
 package com.gwtao.ui.layout.client;
 
 import java.util.Iterator;
 
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.Widget;
 import com.gwtao.ui.util.client.CSSUtils;
 import com.gwtao.ui.util.client.Size;
 
 public abstract class AbstractLayout<T extends LayoutData> implements ILayout {
   private static final String POSITION = "position";
   private static final String TOP = "top";
   private static final String LEFT = "left";
   private static final String PX = "px";
   private LayoutPanel layoutPanel;
 
   @Override
   public final void init(LayoutPanel layoutPanel) {
     this.layoutPanel = layoutPanel;
     init();
   }
 
   protected void init() {
   }
 
   @Override
   public void exit() {
   }
 
   @Override
   public void onAddChild(Widget child) {
   }
 
   @Override
   public void onRemoveChild(Widget child) {
   }
 
   public LayoutPanel getLayoutPanel() {
     return layoutPanel;
   }
 
   protected Iterator<Widget> iterateWidgets() {
     return layoutPanel.iterator();
   }
 
   protected Size getClientSize() {
     Size size;
      size = new Size(layoutPanel.getOffsetWidth(), layoutPanel.getOffsetHeight());
     //size = new Size(layoutPanel.getOffsetWidth() + CSSUtils.calcMarginWidth(layoutPanel.getElement()), layoutPanel.getOffsetHeight() + CSSUtils.calcMarginHeight(layoutPanel.getElement()));
 
     // Element e = layoutPanel.getElement();
     // size = new Size(e.getClientWidth(), e.getClientHeight());
     return size;
   }
 
   protected Size getWidgetMinSize(Widget widget) {
     // if (widget instanceof ILayoutContainer)
     // return ((ILayoutContainer) widget).getMinSize();
     // else if (widget instanceof Panel)
     // return Size.ZERO;
     LayoutData fld = getWidgetData(widget);
     return fld.getMinSize(); // new Size(0, 0);// widget.getOffsetWidth(), widget.getOffsetHeight());
   }
 
   // protected Size updateEffectiveMinSize(Widget widget) {
   // Size minSize = getWidgetMinSize(widget);
   // LayoutData fld = getWidgetData(widget);
   // fld.effectiveMinHeight = minSize.getHeight() > 0 ? Math.min(fld.getMinHeight(), minSize.getHeight()) :
   // fld.getMinHeight();
   // fld.effectiveMinWidth = minSize.getWidth() > 0 ? Math.min(fld.getMinWidth(), minSize.getWidth()) :
   // fld.getMinWidth();
   // return fld.getEffectiveMinSize();
   // }
 
   @SuppressWarnings("unchecked")
   protected T getWidgetData(Widget widget) {
     T ld = (T) widget.getLayoutData();
     if (ld == null) {
       // ld = createDefaultLayoutData(widget.getOffsetWidth() + CSSUtils.calcMarginWidth(widget.getElement()),
       // widget.getOffsetHeight() + CSSUtils.calcMarginHeight(widget.getElement()));
       ld = createDefaultLayoutData(32, 32); // widget.getOffsetWidth(), widget.getOffsetHeight());
       widget.setLayoutData(ld);
     }
     return ld;
   }
 
   protected abstract T createDefaultLayoutData(int minWidth, int minHeight);
 
   protected static void placeWidget(Widget widget, int left, int top, int width, int height) {
     Element elem = widget.getElement();
 
     int cx = CSSUtils.calcMarginLeft(elem);
     int cy = CSSUtils.calcMarginTop(elem);
 
     int mw = CSSUtils.calcMarginWidth(elem);
     int mh = CSSUtils.calcMarginHeight(elem);
 
     mw += CSSUtils.calcPaddingWidth(elem);
     mh += CSSUtils.calcPaddingHeight(elem);
 
    DOM.setStyleAttribute(elem, "overflow", "auto");
     DOM.setStyleAttribute(elem, POSITION, "absolute");
     DOM.setStyleAttribute(elem, LEFT, (left + cx) + PX);
     DOM.setStyleAttribute(elem, TOP, (top + cy) + PX);
     widget.setSize((width - mw) + PX, (height - mh) + PX);
 
   }
 
   protected static void sizeWidget(Widget widget, int width, int height) {
     widget.setSize(width + PX, height + PX);
   }
 
   // @Override
   // public void measure() {
   // Iterator<Widget> it = iterateWidgets();
   // while (it.hasNext()) {
   // measure(it.next());
   // }
   // }
 
   // private void measure(Widget widget) {
   // if (widget instanceof ILayoutContainer) {
   // ((ILayoutContainer) widget).measure();
   // }
   // // else if (widget instanceof Composite) {
   // // measure(((Composite) widget).);
   // // }
   // else if (widget instanceof HasWidgets) {
   // Iterator<Widget> it = ((HasWidgets) widget).iterator();
   // while (it.hasNext())
   // measure(it.next());
   // }
   // }
 }
