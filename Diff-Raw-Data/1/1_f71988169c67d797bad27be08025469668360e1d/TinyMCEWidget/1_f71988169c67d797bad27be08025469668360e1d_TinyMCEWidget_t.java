 /*
  * This library is part of the Acacia Editor -
  * an open source inline and form based content editor for GWT.
  *
  * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * For further information about Alkacon Software, please see the
  * company website: http://www.alkacon.com
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package com.alkacon.acacia.client.widgets;
 
 import com.alkacon.acacia.client.css.I_LayoutBundle;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.RepeatingCommand;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.dom.client.Style.Display;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.DomEvent;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 
 /**
  * This class is used to start TinyMCE for editing the content of an element.<p>
  * 
  * After constructing the instance, the actual editor is opened using the init() method, and destroyed with the close()
  * method. While the editor is opened, the edited contents can be accessed using the methods of the HasValue interface.  
  */
 public final class TinyMCEWidget extends A_EditWidget {
 
     /** The minimum editor height. */
     private static final int MIN_EDITOR_HEIGHT = 70;
 
     /** A flag which indicates whether the editor is currently active. */
     protected boolean m_active;
 
     /** The current content. */
     protected String m_currentContent;
 
     /** The TinyMCE editor instance. */
     protected JavaScriptObject m_editor;
 
     /** The DOM ID of the editable element. */
     protected String m_id;
 
     /** The original HTML content of the editable element. */
     protected String m_originalContent;
 
     /** The editor height to set. */
     int m_editorHeight;
 
     /** The editor options. */
     private JavaScriptObject m_options;
 
     /** The maximal width of the widget. */
     protected int m_width;
 
     /**
      * Creates a new instance for the given element.<p>
      * 
      * @param element the DOM element
      * @param options the tinyMCE editor options to extend the default settings
      */
     public TinyMCEWidget(Element element, JavaScriptObject options) {
 
         super(element);
         m_options = options;
         m_active = true;
     }
 
     /**
      * Constructor.<p>
      * 
      * @param options the tinyMCE editor options to extend the default settings
      */
     public TinyMCEWidget(JavaScriptObject options) {
 
         this(DOM.createDiv(), options);
 
     }
 
     /**
      * @see com.alkacon.acacia.client.widgets.A_EditWidget#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
      */
     @Override
     public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
 
         return addHandler(handler, ValueChangeEvent.getType());
     }
 
     /** 
      * Gets the main editable element.<p>
      * 
      * @return the editable element 
      */
     public native Element getMainElement() /*-{
         var elementId = this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_id;
         var mainElement = $wnd.document.getElementById(elementId);
         return mainElement;
     }-*/;
 
     /**
      * @see com.google.gwt.user.client.ui.HasValue#getValue()
      */
     @Override
     public String getValue() {
 
         if (m_editor != null) {
             return getContent();
         }
         return getElement().getInnerHTML();
     }
 
     /**
      * @see com.alkacon.acacia.client.widgets.I_EditWidget#isActive()
      */
     public boolean isActive() {
 
         return m_active;
     }
 
     /**
      * @see com.alkacon.acacia.client.widgets.I_EditWidget#setActive(boolean)
      */
     public void setActive(boolean active) {
 
         if (m_active == active) {
             return;
         }
         m_active = active;
         if (m_editor != null) {
             if (m_active) {
                 getEditorParentElement().removeClassName(I_LayoutBundle.INSTANCE.form().inActive());
                 // getElement().focus();
                 fireValueChange(true);
             } else {
                 getEditorParentElement().addClassName(I_LayoutBundle.INSTANCE.form().inActive());
             }
         }
     }
 
     /**
      * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
      */
     public void setValue(String value) {
 
         setValue(value, true);
     }
 
     /**
      * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
      */
     public void setValue(String value, boolean fireEvents) {
 
         if (m_editor == null) {
             // editor has not been initialized yet
             getElement().setInnerHTML(value);
         } else {
             setContent(value);
         }
         if (fireEvents) {
             fireValueChange(false);
         }
     }
 
     /**
      * Checks whether the necessary Javascript libraries are available by accessing them. 
      */
     protected native void checkLibraries() /*-{
         // fail early if tinymce is not available
         var w = $wnd;
         var init = w.tinyMCE.init;
     }-*/;
 
     /**
      * Gives an element an id if it doesn't already have an id, and then returns the element's id.<p>
      * 
      * @param element the element for which we want to add the id
      *  
      * @return the id 
      */
     protected String ensureId(Element element) {
 
         String id = element.getId();
         if ((id == null) || "".equals(id)) {
             id = Document.get().createUniqueId();
             element.setId(id);
         }
         return id;
     }
 
     /**
      * Fixes the styling of the editor widget.<p>
      */
     protected void fixStyles() {
 
         // it may take some time until the editor has been initialized, repeat until layout fix can be applied
         Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
 
             /**
              * @see com.google.gwt.core.client.Scheduler.RepeatingCommand#execute()
              */
             public boolean execute() {
 
                 Element parent = getEditorParentElement();
                 if (parent != null) {
                     String cssClass = getMainElement().getClassName();
                     if ((cssClass != null) && (cssClass.trim().length() > 0)) {
                         parent.addClassName(cssClass);
                     }
                     parent.getStyle().setDisplay(Display.BLOCK);
                     getEditorTableElement().getStyle().setWidth(100, Unit.PCT);
                     return false;
                 }
                 return true;
             }
         }, 100);
     }
 
     /**
      * Fixes the layout when the toolbar's top is above the body's top.<p>
      */
     protected void fixToolbar() {
 
         Element element = getToolbarElement();
         int top = element.getAbsoluteTop();
         if (top < 0) {
             Element parent = (Element)(element.getParentNode());
             parent.getStyle().setMarginTop(-top, Unit.PX);
         }
     }
 
     /**
      * Returns the editor parent element.<p>
      * 
      * @return the editor parent element
      */
     protected native Element getEditorParentElement() /*-{
         var elementId = this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_id;
         var parentId = elementId + "_parent";
         return $doc.getElementById(parentId);
     }-*/;
 
     /**
      * Returns the editor table element.<p>
      * 
      * @return the editor table element
      */
     protected native Element getEditorTableElement() /*-{
         var elementId = this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_id;
         var tableId = elementId + "_tbl";
         return $doc.getElementById(tableId);
     }-*/;
 
     /**
      * Returns the editor parent element.<p>
      * 
      * @return the editor parent element
      */
     protected native int getFrameContentHeight() /*-{
         var elementId = this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_id;
         var parentId = elementId + "_parent";
         return $doc.getElementById(parentId);
     }-*/;
 
     /**
      * Gets the toolbar element.<p>
      * 
      * @return the toolbar element 
      */
     protected native Element getToolbarElement() /*-{
         var elementId = this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_id;
         var toolbarId = elementId + "_external";
         return $doc.getElementById(toolbarId);
     }-*/;
 
     /**
      * @see com.google.gwt.user.client.ui.FocusWidget#onAttach()
      */
     @Override
     protected void onAttach() {
 
         super.onAttach();
         Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 
             public void execute() {
 
                 m_editorHeight = calculateEditorHeight();
                 m_id = ensureId(getElement());
                 m_width = getElement().getOffsetWidth() - 2;
                 checkLibraries();
                 initNative();
                 if (!m_active) {
 
                     Element parent = getEditorParentElement();
                     if (parent != null) {
                         parent.addClassName(I_LayoutBundle.INSTANCE.form().inActive());
                     }
                 }
 
             }
         });
     }
 
     /**
      * Propagates the a focus event.<p>
      */
     protected void propagateFocusEvent() {
 
         NativeEvent nativeEvent = Document.get().createFocusEvent();
         DomEvent.fireNativeEvent(nativeEvent, this, this.getElement());
     }
 
     /**
      * Propagates a native mouse event.<p>
      *
      * @param nativeEvent the native mouse event 
      */
     protected void propagateMouseEvent(NativeEvent nativeEvent) {
 
         DomEvent.fireNativeEvent(nativeEvent, this, this.getElement());
     }
 
     /**
      * Removes the editor instance.<p>
      */
     protected native void removeEditor() /*-{
         var editor = this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_editor;
         editor.remove();
     }-*/;
 
     /**
      * Sets the main content of the element which is inline editable.<p>
      * 
      * @param html the new content html 
      */
     protected native void setMainElementContent(String html) /*-{
         var instance = this;
         var elementId = instance.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_id;
         var mainElement = $wnd.document.getElementById(elementId);
         mainElement.innerHTML = html;
     }-*/;
 
     /**
      * Calculates the needed editor height.<p>
      * 
      * @return the calculated editor height
      */
     int calculateEditorHeight() {
 
         int result = getElement().getOffsetHeight() + 30;
         return result > MIN_EDITOR_HEIGHT ? result : MIN_EDITOR_HEIGHT;
     }
 
     /**
      * Initializes the TinyMCE instance.
      */
     native void initNative() /*-{
 
         var self = this;
         var elementId = self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_id;
         var iframeId = elementId + "_ifr";
         var mainElement = $wnd.document.getElementById(elementId);
         var editorHeight = self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_editorHeight
                 + "px";
         self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_originalContent = mainElement.innerHTML;
         self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_currentContent = mainElement.innerHTML;
 
         var fireChange = function() {
             self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::fireValueChange(Z)(false);
         };
 
         var fireChangeDelayed = function() {
             // delay because we want TinyMCE to process the event first 
             $wnd
                     .setTimeout(
                             function() {
                                 try {
                                     self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::fireValueChange(Z)(false);
                                 } catch (e) {
                                     var handler = @com.google.gwt.core.client.GWT::getUncaughtExceptionHandler()();
                                     handler.@com.google.gwt.core.client.GWT.UncaughtExceptionHandler::onUncaughtException(Ljava/lang/Throwable;)(e);
                                     throw e;
                                 }
                             }, 1);
         };
 
         // default options:
         var defaults = {
             theme_advanced_resize_horizontal : true,
             theme_advanced_resizing_max_width : self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_width,
             relative_urls : false,
             remove_script_host : false,
             skin_variant : 'ocms',
             mode : "exact",
             theme : "advanced",
             plugins : "autolink,lists,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,noneditable,visualchars,nonbreaking,xhtmlxtras,template,wordcount,advlist",
             theme_advanced_toolbar_location : "external",
             theme_advanced_toolbar_align : "right",
             theme_advanced_statusbar_location : "bottom",
             width : '100%',
             theme_advanced_resizing : true,
             theme_advanced_resizing_use_cookie : false
         };
         var options = this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_options;
         // extend the defaults with any given options
         if (options != null) {
             var vie = @com.alkacon.vie.client.Vie::getInstance()();
             vie.jQuery.extend(defaults, options);
         }
 
         // add the setup function
         defaults.setup = function(ed) {
             self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_editor = ed;
 
             ed.onChange.add(fireChange);
             ed.onKeyDown.add(fireChangeDelayed);
             ed.onLoadContent
                     .add(function() {
                         $wnd.document.getElementById(iframeId).style.minHeight = editorHeight;
                     });
             ed.onClick
                     .add(function(event) {
                         self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::fixToolbar()();
                         self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::propagateMouseEvent(Lcom/google/gwt/dom/client/NativeEvent;)(event);
                     });
         };
 
         // add event handlers
         defaults.init_instance_callback = function(ed) {
             $wnd.tinyMCE.dom.Event
                     .add(
                             ed.getWin(),
                             'focus',
                             function(event) {
                                 self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::propagateFocusEvent()();
                             });
             $wnd.tinyMCE.dom.Event
                     .add(
                             ed.getWin(),
                             'mousedown',
                             function(event) {
                                 self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::propagateMouseEvent(Lcom/google/gwt/dom/client/NativeEvent;)(event);
                             });
             $wnd.tinyMCE.dom.Event
                     .add(
                             ed.getWin(),
                             'mouseup',
                             function(event) {
                                 self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::propagateMouseEvent(Lcom/google/gwt/dom/client/NativeEvent;)(event);
                             });
             $wnd.tinyMCE.dom.Event
                     .add(
                             ed.getWin(),
                             'mousemove',
                             function(event) {
                                 self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::propagateMouseEvent(Lcom/google/gwt/dom/client/NativeEvent;)(event);
                             });
         };
 
         // set the edited element id
         defaults.elements = self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_id;
 
         // initialize tinyMCE
         $wnd.tinyMCE.init(defaults);
         self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::fixStyles()();
     }-*/;
 
     /**
      * Returns the editor content.<p>
      * 
      * @return the editor content
      */
     private native String getContent() /*-{
         var editor = this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_editor;
         return editor.getContent();
     }-*/;
 
     /**
      * Sets the content of the TinyMCE editor.<p>
      * 
      * @param newContent the new content 
      */
     private native void setContent(String newContent) /*-{
         var editor = this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_editor;
         editor.setContent(newContent);
     }-*/;
 
 }
