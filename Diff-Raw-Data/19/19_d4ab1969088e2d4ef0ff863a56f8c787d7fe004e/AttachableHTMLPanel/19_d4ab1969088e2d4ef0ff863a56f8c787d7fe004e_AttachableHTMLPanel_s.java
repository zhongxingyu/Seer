 /*
  * Copyright 2011 Google Inc.
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
 package com.google.gwt.user.client.ui;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.safehtml.client.SafeHtmlTemplates;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.client.Command;
 
 /**
  * EXPERIMENTAL and subject to change. Do not use this in production code.
  * <p>
  * An {@link Attachable} version of {@link HTMLPanel}. This class is a stepping
  * in our transition to the Attachable strategy. Eventually this functionality
  * should be merged into {@link HTMLPanel}.
  * The only reason this class doesn't extend {@link HTMLPanel} is because it
  * doesn't provide any way to build the panel lazily (which is needed here).
  */
 public class AttachableHTMLPanel extends ComplexPanel implements Attachable {
 
   private static Element hiddenDiv;
 
   interface HTMLTemplates extends SafeHtmlTemplates {
     @Template("<div id=\"{0}\">{1}</div>")
     SafeHtml renderWithId(String id, SafeHtml innerHtml);
 
     @Template("<div id=\"{0}\" class=\"{1}\">{2}</div>")
     SafeHtml renderWithIdAndClass(String id, String styleName, SafeHtml innerHtml);
   }
   private static final HTMLTemplates TEMPLATE =
       GWT.create(HTMLTemplates.class);
 
   private static void ensureHiddenDiv() {
     // If it's already been created, don't do anything.
     if (hiddenDiv != null) {
       return;
     }
 
     hiddenDiv = Document.get().createDivElement();
     UIObject.setVisible(hiddenDiv, false);
     RootPanel.getBodyElement().appendChild(hiddenDiv);
   }
 
   // TODO(rdcastro): Add setters for these, or maybe have a list instead of a
   // single callback.
   public Command wrapInitializationCallback = null;
   public Command detachedInitializationCallback = null;
 
   protected SafeHtml html = null;
   private String styleName = null;
 
   /**
    * Creates an HTML panel with the specified HTML contents inside a DIV
    * element. Any element within this HTML that has a specified id can contain a
    * child widget.
    * The actual element that will hold this HTML isn't initialized until it is
    * needed.
    *
    * @param html the panel's HTML
    */
   public AttachableHTMLPanel(String html) {
     this(new SafeHtmlBuilder().appendHtmlConstant(html).toSafeHtml());
   }
 
   /**
    * Initializes the panel's HTML from a given {@link SafeHtml} object.
    *
    * Similar to {@link #HTMLPanel(String)}
    *
    * @param safeHtml the html to set.
    */
   public AttachableHTMLPanel(SafeHtml safeHtml) {
     this.html = safeHtml;
   }
 
   /**
    * Adds a child widget to the panel.
    *
    * @param widget the widget to be added
    */
   @Override
   public void add(Widget widget) {
     add(widget, getElement());
   }
 
 
   /**
    * Adds a child widget to the panel, replacing the HTML element.
    *
    * @param widget the widget to be added
    * @param toReplace the element to be replaced by the widget
    */
   public final void addAndReplaceElement(Widget widget, Element toReplace) {
     com.google.gwt.user.client.Element clientElem = toReplace.cast();
     addAndReplaceElement(widget, clientElem);
   }
 
   /**
    * Adds a child widget to the panel, replacing the HTML element.
    *
    * @param widget the widget to be added
    * @param toReplace the element to be replaced by the widget
    * @deprecated use {@link #addAndReplaceElement(Widget, Element)}
    */
   @Deprecated
   public void addAndReplaceElement(Widget widget,
       com.google.gwt.user.client.Element toReplace) {
     // Logic pulled from super.add(), replacing the element rather than adding.
     widget.removeFromParent();
     getChildren().add(widget);
     toReplace.getParentNode().replaceChild(widget.getElement(), toReplace);
     adopt(widget);
   }
 
   /**
    * Overloaded version for IsWidget.
    *
    * @see #addAndReplaceElement(Widget,Element)
    */
   public void addAndReplaceElement(IsWidget widget,
       com.google.gwt.user.client.Element toReplace) {
     this.addAndReplaceElement(widget.asWidget(), toReplace);
   }
 
   @Override
   public com.google.gwt.user.client.Element getElement() {
     if (!isFullyInitialized()) {
       // In case we haven't finished initialization yet, finish it now.
       buildAndInitDivContainer();
       html = null;
 
       // We might have to add a style that has been previously set.
       if (styleName != null) {
         super.setStyleName(styleName);
         styleName = null;
       }
     }
     return super.getElement();
   }
 
   /**
    * Adopts the given, but doesn't change anything about its DOM element.
    * Should only be used for widgets with elements that are children of this
    * panel's element.
    */
   public void logicalAdd(Widget widget) {
     getChildren().add(widget);
     adopt(widget);
   }
 
   @Override
   public void performDetachedInitialization() {
     if (detachedInitializationCallback != null) {
       detachedInitializationCallback.execute();
       detachedInitializationCallback = null;
     }
   }
 
   @Override
   public SafeHtml render(String id) {
     SafeHtmlBuilder builder = new SafeHtmlBuilder();
     render(id, builder);
     return builder.toSafeHtml();
   }
 
   @Override
   public void render(String id, SafeHtmlBuilder builder) {
     if (styleName != null) {
       builder.append(TEMPLATE.renderWithIdAndClass(id, styleName, getInnerHtml()));
       styleName = null;
     } else {
       builder.append(TEMPLATE.renderWithId(id, getInnerHtml()));
     }
   }
 
   @Override
   public void setStyleName(String styleName) {
     if (!isFullyInitialized()) {
       // If we haven't built the actual HTML element yet, we save the style
       // to apply later on.
       this.styleName = styleName;
     } else {
       super.setStyleName(styleName);
     }
   }
 
   @Override
   public void wrapElement(Element element) {
    if (!isFullyInitialized()) {
      // NOTE(rdcastro): This code is only run when Attachable is in active use.
      element.getParentNode().replaceChild(getElement(), element);
    } else {
      setElement(element);
      html = null;
     }
 
     if (wrapInitializationCallback != null) {
       wrapInitializationCallback.execute();
       wrapInitializationCallback = null;
     }
   }
 
   /**
    * Returns the HTML to be set as the innerHTML of the container.
    */
   protected SafeHtml getInnerHtml() {
     return html;
   }
 
   /**
    * Whether the initilization of the panel is finished (i.e., the corresponding
    * DOM element has been built).
    */
   protected boolean isFullyInitialized() {
     return html == null;
   }
 
   /**
    * Method that finishes the initialization of HTMLPanel instances built from
    * HTML. This will create a div to wrap the given HTML and call any callbacks
    * that may have been added to the panel.
    */
   private void buildAndInitDivContainer() {
     // Build the div that'll container the panel's HTML.
     Element element = Document.get().createDivElement();
     element.setInnerHTML(getInnerHtml().asString());
     setElement(element);
 
     // If there's any wrap callback to call, we have to attach the div before
     // calling it, and then detach again.
     if (wrapInitializationCallback != null) {
       ensureHiddenDiv();
       hiddenDiv.appendChild(element);
       wrapInitializationCallback.execute();
       element.getParentNode().removeChild(element);
     }
 
     // Call any detached init callbacks we might have.
     if (detachedInitializationCallback != null) {
       detachedInitializationCallback.execute();
     }
   }
 }
