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
 import com.alkacon.geranium.client.util.PositionBean;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.DomEvent;
 import com.google.gwt.event.logical.shared.HasResizeHandlers;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Timer;
 
 /**
  * This class is used to start TinyMCE for editing the content of an element.<p>
  * 
  * After constructing the instance, the actual editor is opened using the init() method, and destroyed with the close()
  * method. While the editor is opened, the edited contents can be accessed using the methods of the HasValue interface.  
  */
 public final class TinyMCEWidget extends A_EditWidget implements HasResizeHandlers {
 
     /** Use as option to disallow any HTML or formatting the content. */
     public static final String NO_HTML_EDIT = "no_html_edit";
 
     /** The minimum editor height. */
     private static final int MIN_EDITOR_HEIGHT = 70;
 
     /** The toolbar container css class name. */
     private static final String TOOLBAR_CONTAINER = I_LayoutBundle.INSTANCE.form().tinymceToolbarContainer();
 
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
 
     /** The maximal width of the widget. */
     protected int m_width;
 
     /** The editor height to set. */
     int m_editorHeight;
 
     /** The element to store the widget content in. */
     private Element m_contentElement;
 
     /** Indicates the value has been set from external, not from within the widget. */
     private boolean m_externalValueChange;
 
     /** Indicating if the widget has been attached yet. */
     private boolean m_hasBeenAttached;
 
     /** Flag indicating the editor has been initialized. */
     private boolean m_initialized;
 
     /** Flag indicating if in line editing is used. */
     private boolean m_inline;
 
     /** The editor options. */
     private Object m_options;
 
     /** The in line editing toolbar container. */
     private Element m_toolbarContainer;
 
     /**
      * Creates a new instance for the given element. Use this constructor for in line editing.<p>
      * 
      * @param element the DOM element
      * @param options the tinyMCE editor options to extend the default settings
      */
     public TinyMCEWidget(Element element, Object options) {
 
         this(element, options, true);
     }
 
     /**
      * Creates a new instance with the given options. Use this constructor for form based editing.<p>
      * 
      * @param options the tinyMCE editor options to extend the default settings
      */
     public TinyMCEWidget(Object options) {
 
         this(DOM.createDiv(), options, false);
     }
 
     /**
      * Constructor.<p>
      * 
      * @param element the DOM element
      * @param options the tinyMCE editor options to extend the default settings
      * @param inline flag indicating if in line editing is used
      */
     private TinyMCEWidget(Element element, Object options, boolean inline) {
 
         super(element);
         m_originalContent = "";
         m_options = options;
         m_active = true;
         m_inline = inline;
         if (m_inline) {
             m_contentElement = element;
         } else {
             // using a child DIV as content element
             m_contentElement = getElement().appendChild(DOM.createDiv());
         }
     }
 
     /**
      * @see com.google.gwt.event.logical.shared.HasResizeHandlers#addResizeHandler(com.google.gwt.event.logical.shared.ResizeHandler)
      */
     public HandlerRegistration addResizeHandler(ResizeHandler handler) {
 
         return addHandler(handler, ResizeEvent.getType());
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
     public Element getMainElement() {
 
         return m_contentElement;
     }
 
     /**
      * @see com.google.gwt.user.client.ui.HasValue#getValue()
      */
     @Override
     public String getValue() {
 
         if (m_editor != null) {
             return getContent().trim();
         }
         return m_originalContent.trim();
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
                 getElement().removeClassName(I_LayoutBundle.INSTANCE.form().inActive());
                 fireValueChange(true);
             } else {
                 getElement().addClassName(I_LayoutBundle.INSTANCE.form().inActive());
             }
         }
     }
 
     /**
      * @see com.alkacon.acacia.client.widgets.I_EditWidget#setName(java.lang.String)
      */
     public void setName(String name) {
 
         // no input field so nothing to do
 
     }
 
     /**
      * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
      */
     public void setValue(String value) {
 
         setValue(value, false);
     }
 
     /**
      * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
      */
     public void setValue(String value, boolean fireEvents) {
 
         if (value != null) {
             value = value.trim();
         }
         setPreviousValue(value);
         if (m_editor == null) {
             // editor has not been initialized yet
             m_originalContent = value;
         } else {
             m_externalValueChange = true;
             setContent(value);
         }
         if (fireEvents) {
             fireValueChange(true);
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
      * Returns the editor parent element.<p>
      * 
      * @return the editor parent element
      */
     protected Element getEditorParentElement() {
 
         String parentId = m_id + "_parent";
         Element result = getElementById(parentId);
         return result;
     }
 
     /**
      * Gets an element by its id.<p>
      * 
      * @param id the id 
      * @return the element with the given id 
      */
     protected native Element getElementById(String id) /*-{
                                                        return $doc.getElementById(id);
                                                        }-*/;
 
     /**
      * Gets the toolbar element.<p>
      * 
      * @return the toolbar element 
      */
     protected Element getToolbarElement() {
 
         String toolbarId = m_id + "_external";
         Element result = getElementById(toolbarId);
         return result;
     }
 
     /**
      * Checks if the main element contains the current text selection.<p>
      * 
      * @return <code>true</code> if the main element contains the current text selection
      */
     protected boolean hasCurrentSelectionRange() {
 
         Element rangeParent = getCurrentRangeParent();
         return (rangeParent != null) && getMainElement().isOrHasChild(rangeParent);
     }
 
     /**
      * @see com.google.gwt.user.client.ui.FocusWidget#onAttach()
      */
     @Override
     protected void onAttach() {
 
         super.onAttach();
         if (!m_hasBeenAttached) {
             m_hasBeenAttached = true;
             Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 
                 public void execute() {
 
                     if (isAttached()) {
                         m_editorHeight = calculateEditorHeight();
                         m_id = ensureId(getMainElement());
                         m_width = getElement().getOffsetWidth() - 2;
                         checkLibraries();
                         initNative();
                         if (!m_active) {
                             getElement().addClassName(I_LayoutBundle.INSTANCE.form().inActive());
                         }
                     } else {
                         resetAtachedFlag();
                     }
                 }
             });
         }
     }
 
     /**
      * @see com.google.gwt.user.client.ui.Widget#onDetach()
      */
     @Override
     protected void onDetach() {
 
         detachEditor();
         if (m_toolbarContainer != null) {
             m_toolbarContainer.removeFromParent();
             m_toolbarContainer = null;
         }
         super.onDetach();
     }
 
     /**
      * Propagates the a focus event.<p>
      */
     protected void propagateFocusEvent() {
 
         NativeEvent nativeEvent = Document.get().createFocusEvent();
         DomEvent.fireNativeEvent(nativeEvent, this, getElement());
     }
 
     /**
      * Propagates a native mouse event.<p>
      *
      * @param eventType the mouse event type 
      * @param eventSource the event source
      */
     protected native void propagateMouseEvent(String eventType, Element eventSource) /*-{
                                                                                      var doc = $wnd.document;
                                                                                      var event;
                                                                                      if (doc.createEvent) {
                                                                                      event = doc.createEvent("MouseEvents");
                                                                                      event.initEvent(eventType, true, true);
                                                                                      eventSource.dispatchEvent(event);
                                                                                      } else {
                                                                                      eventSource.fireEvent("on" + eventType);
                                                                                      }
                                                                                      }-*/;
 
     /**
      * Sets focus to the editor. Use only when in line editing.<p>
      */
     protected native void refocusInlineEditor() /*-{
                                                 var elem = $wnd.document
                                                 .getElementById(this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_id);
                                                 elem.blur();
                                                 elem.focus();
                                                 }-*/;
 
     /**
      * Removes the editor instance.<p>
      */
     protected native void removeEditor() /*-{
                                          var editor = this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_editor;
                                          editor.remove();
                                          }-*/;
 
     /**
      * Schedules to reset the focus to the main element.<p>
      */
     protected void scheduleRefocus() {
 
         // this needs to be delayed a bit, otherwise the toolbar is not rendered properly
         Timer focusTimer = new Timer() {
 
             @Override
             public void run() {
 
                 refocusInlineEditor();
             }
         };
         focusTimer.schedule(150);
     }
 
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
                              var needsRefocus = self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::hasCurrentSelectionRange()();
                              var elementId = self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_id;
                              var mainElement = $wnd.document.getElementById(elementId);
                              var editorHeight = self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_editorHeight
                              + "px";
 
                              var fireChange = function() {
                              self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::fireChangeFromNative()();
                              };
                              var options = this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_options;
                              if (options != null && options.editorHeight) {
                              editorHeight = options.editorHeight;
                              delete options.editorHeight;
                              }
                              // default options:
                              var defaults;
                              if (@com.alkacon.acacia.client.widgets.TinyMCEWidget::NO_HTML_EDIT == options) {
                              // disallow any formatting
                              defaults = {
                              selector : mainElement.tagName+"#"+ elementId,
                             entity_encoding : "raw",
                             mode : "exact",
                             plugins : "paste",
                              toolbar : "undo,redo",
                              menubar : false,
                              toolbar_items_size : 'small'
                              };
                              options = null;
                              } else {
                              defaults = {
                              elements : elementId,
                              relative_urls : false,
                              remove_script_host : false,
                              entity_encoding : "raw",
                              skin_variant : 'ocms',
                              mode : "exact",
                              theme : "modern",
                              plugins : "autolink,lists,pagebreak,layer,table,save,hr,image,link,emoticons,spellchecker,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,noneditable,visualchars,nonbreaking,template,wordcount,advlist",
                              menubar : false,
                              toolbar_items_size : 'small'
                              };
                              }
                              if (this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_inline) {
                              self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_currentContent = mainElement.innerHTML;
                              defaults.inline = true;
                              defaults.width = this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_width;
                              var toolbarContainer = $wnd.document.createElement("div");
                              toolbarContainer.className = @com.alkacon.acacia.client.widgets.TinyMCEWidget::TOOLBAR_CONTAINER;
                              toolbarContainer.innerHTML = "<div id=\"" + elementId
                              + "_toolbarContainer\" style=\"width: " + defaults.width
                              + "px;\"></div>";
                              $wnd.document.body.appendChild(toolbarContainer);
                              this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_toolbarContainer = toolbarContainer;
                              defaults.fixed_toolbar_container = "#" + elementId
                              + "_toolbarContainer";
                              } else {
                              self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_currentContent = self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_originalContent;
                              defaults.autoresize_min_height = 100;
                              defaults.autoresize_max_height = editorHeight;
                              defaults.width = '100%';
                              defaults.resize = 'both';
                              }
                              // extend the defaults with any given options
                              if (options != null) {
                              var vie = @com.alkacon.vie.client.Vie::getInstance()();
                              vie.jQuery.extend(defaults, options);
                              if (this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_inline) {
                              delete defaults.content_css;
                              } else {
                              // enable autoresize
                              defaults.plugins = "autoresize," + defaults.plugins;
                              }
                              }
 
                              // add the setup function
                              defaults.setup = function(ed) {
                              self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_editor = ed;
                              ed.on('SetContent', fireChange);
                              ed.on('change', fireChange);
                              ed.on('KeyDown', fireChange);
                              ed
                              .on(
                              'LoadContent',
                              function() {
                                 if (!self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_inline) {
                                     // firing resize event on resize of the editor iframe
                                     ed.dom
                                             .bind(
                                                     ed.getWin(),
                                                     'resize',
                                                     function() {
                                                         self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::fireResizeEvent()();
                                                     });
                                     var content = self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_originalContent;
                                     if (content != null) {
                                         ed.setContent(content);
                                     }
                                     // ensure the body height is set to 'auto', otherwise the autoresize plugin will not work
                                     ed.getDoc().body.style.height = 'auto';
                                 }
                                 self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_initialized = true;
                              });
 
                              if (!self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_inline) {
 
                              ed
                              .on(
                                 'Click',
                                 function(event) {
                                     self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::propagateFocusEvent()();
                                 });
                              ed
                              .on(
                                 'activate',
                                 function(event) {
                                     self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::propagateFocusEvent()();
                                 });
                              ed
                              .on(
                                 'focus',
                                 function(event) {
                                     self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::propagateFocusEvent()();
                                 });
                              } else {
                              if (needsRefocus) {
                              ed
                              .on(
                                     'init',
                                     function() {
                                         self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::scheduleRefocus()();
                                     });
                              }
                              ed
                              .on(
                                 'focus',
                                 function(event) {
                                     self.@com.alkacon.acacia.client.widgets.TinyMCEWidget::resetToolbarContainerPosition()();
                                 });
                              }
                              };
                              // set default z-index for overlay ui components
                              var cssConstants = @com.alkacon.acacia.client.css.I_LayoutBundle::INSTANCE.@com.alkacon.acacia.client.css.I_LayoutBundle::constants()().@com.alkacon.geranium.client.ui.css.I_ConstantsBundle::css()();
                              $wnd.tinymce.ui.FloatPanel.zIndex = cssConstants.@com.alkacon.geranium.client.ui.css.I_ConstantsBundle.I_ConstantsCss::zIndexPopup()();
                              // initialize tinyMCE
                              $wnd.tinymce.init(defaults);
                              }-*/;
 
     /**
      * Resets the attached flag.<p> 
      */
     void resetAtachedFlag() {
 
         m_hasBeenAttached = false;
     }
 
     /**
      * Removes the editor.<p>
      */
     private native void detachEditor() /*-{
                                        var ed = this.@com.alkacon.acacia.client.widgets.TinyMCEWidget::m_editor;
                                        if (ed != null) {
                                        ed.remove();
                                        }
                                        // in IE somehow the whole document will be selected, empty the selection to resolve that
                                        if ($wnd.document.selection != null) {
                                        $wnd.document.selection.empty();
                                        }
                                        }-*/;
 
     /**
      * Used to fire the value changed event from native code.<p>
      */
     private void fireChangeFromNative() {
 
         // skip firing the change event, if the external flag is set
         if (m_initialized && !m_externalValueChange) {
             Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 
                 public void execute() {
 
                     try {
                         fireValueChange(false);
                     } catch (Throwable t) {
                         // this may happen when returning from full screen mode, nothing to be done
                     }
                 }
             });
         }
         // reset the external flag
         m_externalValueChange = false;
     }
 
     /**
      * Fires the resize event.<p>
      */
     private void fireResizeEvent() {
 
         ResizeEvent.fire(this, getOffsetWidth(), getOffsetHeight());
     }
 
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
      * Returns the parent element to the current selection range.<p>
      * 
      * @return the parent element
      */
     private native Element getCurrentRangeParent()/*-{
                                                   if ($wnd.getSelection) {
                                                   var sel = $wnd.getSelection();
                                                   if (sel.getRangeAt && sel.rangeCount) {
                                                   var range = sel.getRangeAt(0);
                                                   return range.commonAncestorContainer;
                                                   }
                                                   } else if ($wnd.document.selection
                                                   && $wnd.document.selection.createRange) {
                                                   var range = $wnd.document.selection.createRange();
                                                   return range.parentElement();
                                                   }
                                                   return null;
                                                   }-*/;
 
     /**
      * Resets the in line editing toolbar position.<p>
      */
     private void resetToolbarContainerPosition() {
 
         if (m_toolbarContainer != null) {
             PositionBean position = PositionBean.generatePositionInfo(m_contentElement);
             m_toolbarContainer.getStyle().setTop(position.getTop() - 5, Unit.PX);
             m_toolbarContainer.getStyle().setLeft(position.getLeft(), Unit.PX);
         }
     }
 
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
