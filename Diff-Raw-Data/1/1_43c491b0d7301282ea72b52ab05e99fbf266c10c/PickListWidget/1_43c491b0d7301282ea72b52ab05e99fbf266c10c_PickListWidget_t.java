 /**
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  **/
 package org.jboss.demo.widgets.client.local;
 
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.LIElement;
 import com.google.gwt.dom.client.Node;
 import com.google.gwt.dom.client.NodeList;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.Widget;
 import org.jboss.demo.widgets.client.shared.Capital;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * @author <a href="http://community.jboss.org/people/bleathem">Brian Leathem</a>
  */
 public class PickListWidget extends Widget {
     private List<Capital> capitals;
     private final Element sourceList;
     private final Element targetList;
 
     public PickListWidget() {
         super();
         Element panel = DOM.createDiv();
         String uniqueId = Document.get().createUniqueId();
         panel.setId(uniqueId);
         setElement(panel);
 
         sourceList = DOM.createElement("ol");
         sourceList.setClassName("source");
         targetList = DOM.createElement("ol");
         targetList.setClassName("target");
 
         panel.appendChild(sourceList);
         panel.appendChild(targetList);
     }
 
     public void initCapitals(List<Capital> capitals, List<Capital> selectedCapitals) {
         this.capitals = capitals;
         clearChildren(sourceList);
         clearChildren(targetList);
 
         Document document = Document.get();
         for (Capital capital : capitals) {
             LIElement li = document.createLIElement();
             li.setInnerText(capital.getName());
             // "data-key" is used by the jQuery plugin to uniquely identify the list elements
             li.setAttribute("data-key", capital.getName());
             // use JSNI to store the item object in the "data-object" attribute of the list element
             setCapital(li, capital);
             if (selectedCapitals.contains(capital)) {
                 targetList.appendChild(li);
             } else {
                 sourceList.appendChild(li);
             }
         }
         // We don't initialize the jQuery plugin until the list elements are in place
         initPlugin();
     }
 
     public void updateSelectedCapitals(List<Capital> selectedCapitals) {
         List<LIElement> liElements = new ArrayList<LIElement>();
         // retrieve the list elements from the targetList
         for (int i = 0; i < targetList.getChildCount(); i++ ) {
             Node node = targetList.getChild(i);
             if (node instanceof LIElement) {
                 liElements.add((LIElement) node);
             }
         }
         // retrieve the list elements from the sourceList
         for (int i = 0; i < sourceList.getChildCount(); i++ ) {
             Node node = sourceList.getChild(i);
             if (node instanceof LIElement) {
                 liElements.add((LIElement) node);
             }
         }
         clearChildren(sourceList);
         clearChildren(targetList);
         // put the selected list elements back in the targetList in the selected order
         Iterator<LIElement> iterator = liElements.iterator();
         for (Capital capital : selectedCapitals) {
             while (iterator.hasNext()) {
                 LIElement li = iterator.next();
                 if (capital.equals(getCapital(li))) {
                     targetList.appendChild(li);
                     iterator.remove();
                     break;
                 }
             }
         }
         // put the non-selected list elements back in the sourceList in the original order
        iterator = liElements.iterator();
         for (Capital capital : capitals) {
             if (! selectedCapitals.contains(capital)) {
                 while (iterator.hasNext()) {
                     LIElement li = iterator.next();
                     if (capital.equals(getCapital(li))) {
                         sourceList.appendChild(li);
                         iterator.remove();
                         break;
                     }
                 }
             }
         }
     }
 
     public List<Capital> getSelectedCapitals() {
         List<Capital> selectedCapitals = new ArrayList<Capital>();
         for (int i = 0; i < targetList.getChildCount(); i++ ) {
             Node node = targetList.getChild(i);
             if (node instanceof LIElement) {
                 LIElement li = (LIElement) node;
                 Capital capital = getCapital(li);
                 if (capital != null) {
                     selectedCapitals.add(capital);
                 }
             }
         }
         return selectedCapitals;
     }
 
     private Element clearChildren(Element element) {
         if (element.hasChildNodes()) {
             while ( element.hasChildNodes()) {
                 element.removeChild(element.getLastChild());
             }
         }
         return element;
     }
 
     private void initPlugin() {
         String id = this.getElement().getId();
         initPickList(id);
     }
 
     private static native void initPickList(String id) /*-{
         $wnd.jQuery("#" + id).pickList();
     }-*/;
 
     private native void setCapital(LIElement li, Capital capital) /*-{
         $wnd.jQuery(li).data('object', capital);
     }-*/;
 
     private native Capital getCapital(LIElement li) /*-{
         return $wnd.jQuery(li).data('object');
     }-*/;
 
 
 }
