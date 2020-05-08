 /*
  *
  *  * Licensed to the Apache Software Foundation (ASF) under one or more
  *  * contributor license agreements.  See the NOTICE file distributed with
  *  * this work for additional information regarding copyright ownership.
  *  * The ASF licenses this file to You under the Apache License, Version 2.0
  *  * (the "License"); you may not use this file except in compliance with
  *  * the License.  You may obtain a copy of the License at
  *  *
  *  *      http://www.apache.org/licenses/LICENSE-2.0
  *  *
  *  * Unless required by applicable law or agreed to in writing, software
  *  * distributed under the License is distributed on an "AS IS" BASIS,
  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  * See the License for the specific language governing permissions and
  *  * limitations under the License.
  *
  */
 
 package org.vaadin.addons.locationtextfield.client.ui;
 
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.FocusEvent;
 import com.google.gwt.event.dom.client.FocusHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.dom.client.LoadEvent;
 import com.google.gwt.event.dom.client.LoadHandler;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.SuggestOracle;
 import com.google.gwt.user.client.ui.TextBox;
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.BrowserInfo;
 import com.vaadin.terminal.gwt.client.EventId;
 import com.vaadin.terminal.gwt.client.Focusable;
 import com.vaadin.terminal.gwt.client.Paintable;
 import com.vaadin.terminal.gwt.client.UIDL;
 import com.vaadin.terminal.gwt.client.Util;
 import com.vaadin.terminal.gwt.client.VConsole;
 import com.vaadin.terminal.gwt.client.VTooltip;
 import com.vaadin.terminal.gwt.client.ui.Field;
 import com.vaadin.terminal.gwt.client.ui.SubPartAware;
 import com.vaadin.terminal.gwt.client.ui.VLazyExecutor;
 import com.vaadin.terminal.gwt.client.ui.VOverlay;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 public class VLocationTextField extends Composite implements Paintable, Field, KeyDownHandler, KeyUpHandler, ClickHandler,
   FocusHandler, BlurHandler, Focusable, ChangeHandler {
 
     /**
      * Represents a suggestion in the suggestion popup box
      */
     public class FilterSelectSuggestion implements SuggestOracle.Suggestion, Command {
 
         private final String key;
         private final String caption;
         private String iconUri;
 
         /**
          * Constructor
          *
          * @param uidl
          *            The UIDL recieved from the server
          */
         public FilterSelectSuggestion(UIDL uidl) {
             key = uidl.getStringAttribute("key");
             caption = uidl.getStringAttribute("caption");
             if (uidl.hasAttribute("icon")) {
                 iconUri = client.translateVaadinUri(uidl.getStringAttribute("icon"));
             }
         }
 
         /**
          * Gets the visible row in the popup as a HTML string. The string
          * contains an image tag with the rows icon (if an icon has been
          * specified) and the caption of the item
          */
         public String getDisplayString() {
             final StringBuilder sb = new StringBuilder();
             if (iconUri != null) {
                 sb.append("<img src=\"");
                 sb.append(Util.escapeAttribute(iconUri));
                 sb.append("\" alt=\"\" class=\"v-icon\" />");
             }
             sb.append("<span>").append(Util.escapeHTML(caption)).append("</span>");
             return sb.toString();
         }
 
         /**
          * Get a string that represents this item. This is used in the text box.
          */
         public String getReplacementString() {
             return caption;
         }
 
         /**
          * Get the option key which represents the item on the server side.
          *
          * @return The key of the item
          */
         public int getOptionKey() {
             return Integer.parseInt(key);
         }
 
         /**
          * Get the URI of the icon. Used when constructing the displayed option.
          *
          * @return
          */
         public String getIconUri() {
             return iconUri;
         }
 
         /**
          * Executes a selection of this item.
          */
         public void execute() {
             onSuggestionSelected(this);
         }
     }
 
     /**
      * Represents the popup box with the selection options. Wraps a suggestion
      * menu.
      */
     public class SuggestionPopup extends VOverlay implements PopupPanel.PositionCallback, CloseHandler<PopupPanel> {
 
         private static final String Z_INDEX = "30000";
 
         private final SuggestionMenu menu;
 
         private final Element up = DOM.createDiv();
         private final Element down = DOM.createDiv();
         private final Element status = DOM.createDiv();
 
         private boolean isPagingEnabled = true;
 
         private long lastAutoClosed;
 
         private int popupOuterPadding = -1;
 
         private int topPosition;
 
         /**
          * Default constructor
          */
         SuggestionPopup() {
             super(true, false, true);
             menu = new SuggestionMenu();
             setWidget(menu);
             setStyleName(CLASSNAME + "-suggestpopup");
             DOM.setStyleAttribute(getElement(), "zIndex", Z_INDEX);
 
             final Element root = getContainerElement();
 
             DOM.setInnerHTML(up, "<span>Prev</span>");
             DOM.sinkEvents(up, Event.ONCLICK);
             DOM.setInnerHTML(down, "<span>Next</span>");
             DOM.sinkEvents(down, Event.ONCLICK);
             DOM.insertChild(root, up, 0);
             DOM.appendChild(root, down);
             DOM.appendChild(root, status);
             DOM.setElementProperty(status, "className", CLASSNAME + "-status");
             DOM.sinkEvents(root, Event.ONMOUSEDOWN | Event.ONMOUSEWHEEL);
             addCloseHandler(this);
         }
 
         /**
          * Shows the popup where the user can see the filtered options
          *
          * @param currentSuggestions
          *            The filtered suggestions
          * @param currentPage
          *            The current page number
          * @param totalSuggestions
          *            The total amount of suggestions
          */
         public void showSuggestions(Collection<FilterSelectSuggestion> currentSuggestions, int currentPage, int totalSuggestions) {
 
             // Add TT anchor point
             DOM.setElementProperty(getElement(), "id", "VAADIN_COMBOBOX_OPTIONLIST");
 
             //for (FilterSelectSuggestion f : currentSuggestions)
                 //VConsole.log("sugg - " + f.getReplacementString());
             //VConsole.log("cur sugg - " + (currentSuggestion != null ? currentSuggestion.getReplacementString() : ""));
 
             menu.setSuggestions(currentSuggestions);
             final int x = VLocationTextField.this.getAbsoluteLeft();
             topPosition = tb.getAbsoluteTop();
             topPosition += tb.getOffsetHeight();
             //VConsole.log("popup position: " + x + ", " + topPosition);
             setPopupPosition(x, topPosition);
 
             int nullOffset = (nullSelectionAllowed && "".equals(lastFilter) ? 1 : 0);
             boolean firstPage = (currentPage == 0);
             final int first = currentPage * pageLength + 1 - (firstPage ? 0 : nullOffset);
             final int last = first + currentSuggestions.size() - 1 - (firstPage && "".equals(lastFilter) ? nullOffset : 0);
             final int matches = totalSuggestions - nullOffset;
             //VConsole.log("nullOffset - " + nullOffset);
             //VConsole.log("firstPage - " + firstPage);
             //VConsole.log("first - " + first);
             //VConsole.log("last - " + last);
             //VConsole.log("matches - " + matches);
             if (last > 0) {
                 // nullsel not counted, as requested by user
                 DOM.setInnerText(status, (matches == 0 ? 0 : first) + "-" + last + "/" + matches);
             } else {
                 DOM.setInnerText(status, "");
             }
             // We don't need to show arrows or statusbar if there is only one
             // page
             if (totalSuggestions <= pageLength || pageLength == 0) {
                 //VConsole.log("turning paging OFF");
                 setPagingEnabled(false);
             } else {
                 setPagingEnabled(true);
                 //VConsole.log("turning paging ON");
             }
             setPrevButtonActive(first > 1);
             setNextButtonActive(last < matches);
 
             // clear previously fixed width
             menu.setWidth("");
             DOM.setStyleAttribute(DOM.getFirstChild(menu.getElement()), "width", "");
 
             setPopupPositionAndShow(this);
             if (currentSuggestions.size() < 1 || (currentSuggestions.size() == 1 && nullSelectionAllowed))
                 hide();
         }
 
         @Override
         public void show() {
             int length = currentSuggestions.size();
             if (length > 0 && (!nullSelectionAllowed || length > 1))
                 super.show();
         }
 
         /**
          * Should the next page button be visible to the user?
          *
          * @param active
          */
         private void setNextButtonActive(boolean active) {
             if (active) {
                 DOM.sinkEvents(down, Event.ONCLICK);
                 DOM.setElementProperty(down, "className", CLASSNAME + "-nextpage");
             } else {
                 DOM.sinkEvents(down, 0);
                 DOM.setElementProperty(down, "className", CLASSNAME + "-nextpage-off");
             }
         }
 
         /**
          * Should the previous page button be visible to the user
          *
          * @param active
          */
         private void setPrevButtonActive(boolean active) {
             if (active) {
                 DOM.sinkEvents(up, Event.ONCLICK);
                 DOM.setElementProperty(up, "className", CLASSNAME + "-prevpage");
             } else {
                 DOM.sinkEvents(up, 0);
                 DOM.setElementProperty(up, "className", CLASSNAME + "-prevpage-off");
             }
 
         }
 
         /**
          * Selects the next item in the filtered selections
          */
         public void selectNextItem() {
             final MenuItem cur = menu.getSelectedItem();
             final int index = 1 + menu.getItems().indexOf(cur);
             if (menu.getItems().size() > index) {
                 final MenuItem newSelectedItem = menu.getItems().get(index);
                 menu.selectItem(newSelectedItem);
                 tb.setText(newSelectedItem.getText());
                 tb.setSelectionRange(lastFilter.length(), Math.abs(newSelectedItem.getText().length() - lastFilter.length()));
             } else if (hasNextPage()) {
                 lastIndex = index - 1; // save for paging
                 filterOptions(currentPage + 1, lastFilter);
             }
         }
 
         /**
          * Selects the previous item in the filtered selections
          */
         public void selectPrevItem() {
             final MenuItem cur = menu.getSelectedItem();
             final int index = -1 + menu.getItems().indexOf(cur);
             if (index > -1) {
                 final MenuItem newSelectedItem = menu.getItems().get(index);
                 menu.selectItem(newSelectedItem);
                 tb.setText(newSelectedItem.getText());
                 tb.setSelectionRange(lastFilter.length(), Math.abs(newSelectedItem.getText().length() - lastFilter.length()));
             } else if (index == -1) {
                 if (currentPage > 0) {
                     lastIndex = index + 1; // save for paging
                     filterOptions(currentPage - 1, lastFilter);
                 }
             } else {
                 final MenuItem newSelectedItem = menu.getItems().get(menu.getItems().size() - 1);
                 menu.selectItem(newSelectedItem);
                 tb.setText(newSelectedItem.getText());
                 tb.setSelectionRange(lastFilter.length(), Math.abs(newSelectedItem.getText().length() - lastFilter.length()));
             }
         }
 
         /*
          * Using a timer to scroll up or down the pages so when we receive lots
          * of consecutive mouse wheel events the pages does not flicker.
          */
         private LazyPageScroller lazyPageScroller = new LazyPageScroller();
 
         private class LazyPageScroller extends Timer {
             private int pagesToScroll = 0;
 
             @Override
             public void run() {
                 if (pagesToScroll != 0) {
                     filterOptions(currentPage + pagesToScroll, lastFilter);
                     pagesToScroll = 0;
                 }
             }
 
             public void scrollUp() {
                 if (currentPage + pagesToScroll > 0) {
                     pagesToScroll--;
                     cancel();
                     schedule(100);
                 }
             }
 
             public void scrollDown() {
                 if (totalMatches > (currentPage + pagesToScroll + 1) * pageLength) {
                     pagesToScroll++;
                     cancel();
                     schedule(100);
                 }
             }
         }
 
         /*
          * (non-Javadoc)
          *
          * @see
          * com.google.gwt.user.client.ui.Widget#onBrowserEvent(com.google.gwt
          * .user.client.Event)
          */
         @Override
         public void onBrowserEvent(Event event) {
             if (event.getTypeInt() == Event.ONCLICK) {
                 final Element target = DOM.eventGetTarget(event);
                 if (target == up || target == DOM.getChild(up, 0)) {
                     lazyPageScroller.scrollUp();
                 } else if (target == down || target == DOM.getChild(down, 0)) {
                     lazyPageScroller.scrollDown();
                 }
             } else if (event.getTypeInt() == Event.ONMOUSEWHEEL) {
                 int velocity = event.getMouseWheelVelocityY();
                 if (velocity > 0) {
                     lazyPageScroller.scrollDown();
                 } else {
                     lazyPageScroller.scrollUp();
                 }
             }
 
             /*
              * Prevent the keyboard focus from leaving the textfield by
              * preventing the default behaviour of the browser. Fixes #4285.
              */
             handleMouseDownEvent(event);
         }
 
         /**
          * Should paging be enabled. If paging is enabled then only a certain
          * amount of items are visible at a time and a scrollbar or buttons are
          * visible to change page. If paging is turned of then all options are
          * rendered into the popup menu.
          *
          * @param paging
          *            Should the paging be turned on?
          */
         public void setPagingEnabled(boolean paging) {
             if (isPagingEnabled == paging) {
                 return;
             }
             if (paging) {
                 DOM.setStyleAttribute(down, "display", "");
                 DOM.setStyleAttribute(up, "display", "");
                 DOM.setStyleAttribute(status, "display", "");
             } else {
                 DOM.setStyleAttribute(down, "display", "none");
                 DOM.setStyleAttribute(up, "display", "none");
                 DOM.setStyleAttribute(status, "display", "none");
             }
             isPagingEnabled = paging;
         }
 
         /*
          * (non-Javadoc)
          *
          * @see
          * com.google.gwt.user.client.ui.PopupPanel$PositionCallback#setPosition
          * (int, int)
          */
         public void setPosition(int offsetWidth, int offsetHeight) {
 
             int top;
             int left;
 
             // reset menu size and retrieve its "natural" size
             menu.setHeight("");
             if (currentPage > 0) {
                 // fix height to avoid height change when getting to last page
                 menu.fixHeightTo(pageLength);
             }
             offsetHeight = getOffsetHeight();
 
 //VConsole.log("offsetHeight - " + offsetHeight);
             final int desiredWidth = getMainWidth();
 //VConsole.log("desiredWidth - " + desiredWidth);
             int naturalMenuWidth = DOM.getElementPropertyInt(DOM.getFirstChild(menu.getElement()), "offsetWidth");
 //VConsole.log("naturalMenuWidth - " + naturalMenuWidth);
             if (popupOuterPadding == -1) {
                 popupOuterPadding = Util.measureHorizontalPaddingAndBorder(getElement(), 2);
             }
 //VConsole.log("popupOuterPadding - " + popupOuterPadding);
             if (naturalMenuWidth < desiredWidth) {
                 menu.setWidth((desiredWidth - popupOuterPadding) + "px");
                 DOM.setStyleAttribute(DOM.getFirstChild(menu.getElement()), "width", "100%");
                 naturalMenuWidth = desiredWidth;
             }
 
             if (BrowserInfo.get().isIE()) {
                 /*
                  * IE requires us to specify the width for the container
                  * element. Otherwise it will be 100% wide
                  */
                 int rootWidth = naturalMenuWidth - popupOuterPadding;
                 DOM.setStyleAttribute(getContainerElement(), "width", rootWidth + "px");
             }
 
             if (offsetHeight + getPopupTop() > Window.getClientHeight() + Window.getScrollTop()) {
                 // popup on top of input instead
                 top = getPopupTop() - offsetHeight - VLocationTextField.this.getOffsetHeight();
                 if (top < 0) {
                     top = 0;
                 }
             } else {
                 top = getPopupTop();
                 /*
                  * Take popup top margin into account. getPopupTop() returns the
                  * top value including the margin but the value we give must not
                  * include the margin.
                  */
                 int topMargin = (top - topPosition);
                 top -= topMargin;
             }
 //VConsole.log("top - " + top);
             // fetch real width (mac FF bugs here due GWT popups overflow:auto )
             offsetWidth = DOM.getElementPropertyInt(DOM.getFirstChild(menu.getElement()), "offsetWidth");
             if (offsetWidth + getPopupLeft() > Window.getClientWidth() + Window.getScrollLeft()) {
                 left = VLocationTextField.this.getAbsoluteLeft() + VLocationTextField.this.getOffsetWidth()
                   + Window.getScrollLeft() - offsetWidth;
                 if (left < 0) {
                     left = 0;
                 }
             } else {
                 left = getPopupLeft();
             }
 //VConsole.log("left - " + left);
             setPopupPosition(left, top);
 //VConsole.log("visible - " + isVisible());
         }
 
         /**
          * Was the popup just closed?
          *
          * @return true if popup was just closed
          */
         public boolean isJustClosed() {
             final long now = (new Date()).getTime();
             return (lastAutoClosed > 0 && (now - lastAutoClosed) < 200);
         }
 
         /*
          * (non-Javadoc)
          *
          * @see
          * com.google.gwt.event.logical.shared.CloseHandler#onClose(com.google
          * .gwt.event.logical.shared.CloseEvent)
          */
         @Override
         public void onClose(CloseEvent<PopupPanel> event) {
             if (event.isAutoClosed()) {
                 lastAutoClosed = (new Date()).getTime();
             }
         }
 
         /**
          * Updates style names in suggestion popup to help theme building.
          */
         public void updateStyleNames(UIDL uidl) {
             if (uidl.hasAttribute("style")) {
                 setStyleName(CLASSNAME + "-suggestpopup");
                 final String[] styles = uidl.getStringAttribute("style").split(" ");
                 for (String style : styles) {
                     addStyleDependentName(style);
                 }
             }
         }
 
     }
 
     /**
      * The menu where the suggestions are rendered
      */
     public class SuggestionMenu extends MenuBar implements SubPartAware, LoadHandler {
 
         private VLazyExecutor delayedImageLoadExecutioner = new VLazyExecutor(100, new Scheduler.ScheduledCommand() {
 
             public void execute() {
                 //VConsole.log("popup visible - " + suggestionPopup.isVisible());
                 //VConsole.log("popup attached - " + suggestionPopup.isAttached());
                 if (suggestionPopup.isVisible() && suggestionPopup.isAttached()) {
                     setWidth("");
                     DOM.setStyleAttribute(DOM.getFirstChild(getElement()), "width", "");
                     suggestionPopup.setPopupPositionAndShow(suggestionPopup);
                 }
             }
         });
 
         /**
          * Default constructor
          */
         SuggestionMenu() {
             super(true);
             setStyleName(CLASSNAME + "-suggestmenu");
             addDomHandler(this, LoadEvent.getType());
         }
 
         /**
          * Fixes menus height to use same space as full page would use. Needed
          * to avoid height changes when quickly "scrolling" to last page
          */
         public void fixHeightTo(int pagelenth) {
             if (currentSuggestions.size() > 0) {
                 final int pixels = pagelenth * (getOffsetHeight() - 2) / currentSuggestions.size();
                 setHeight((pixels + 2) + "px");
             }
         }
 
         /**
          * Sets the suggestions rendered in the menu
          *
          * @param suggestions
          *            The suggestions to be rendered in the menu
          */
         public void setSuggestions(Collection<FilterSelectSuggestion> suggestions) {
             clearItems();
             for (FilterSelectSuggestion s : suggestions) {
                 final MenuItem mi = new MenuItem(s.getDisplayString(), true, s);
 
                 Util.sinkOnloadForImages(mi.getElement());
 
                 this.addItem(mi);
                 if (s == currentSuggestion) {
                     selectItem(mi);
                 }
             }
         }
 
         /**
          * Send the current selection to the server. Triggered when a selection
          * is made or on a blur event.
          */
         public void doSelectedItemAction() {
             // do not send a value change event if null was and stays selected
             final String enteredItemValue = tb.getText();
             if (nullSelectionAllowed && "".equals(enteredItemValue) && selectedOptionKey != null && !"".equals(selectedOptionKey)) {
                 if (nullSelectItem) {
                     //VConsole.log("reset() called from doSelectedItemAction ");
                     reset();
                     return;
                 }
                 // null is not visible on pages != 0, and not visible when
                 // filtering: handle separately
                 client.updateVariable(paintableId, "filter", "", false);
                 client.updateVariable(paintableId, "page", 0, false);
                 client.updateVariable(paintableId, "selected", new String[] {}, immediate);
                 suggestionPopup.hide();
                 return;
             }
 
             doPostFilterSelectedItemAction();
         }
 
         /**
          * Triggered after a selection has been made
          */
         public void doPostFilterSelectedItemAction() {
             final MenuItem item = getSelectedItem();
             final String enteredItemValue = tb.getText();
 
             // check for exact match in menu
             int p = getItems().size();
             if (p > 0) {
                 for (int i = 0; i < p; i++) {
                     final MenuItem potentialExactMatch = getItems().get(i);
                     if (potentialExactMatch.getText().equals(enteredItemValue)) {
                         selectItem(potentialExactMatch);
                         // do not send a value change event if null was and
                         // stays selected
                         if (!"".equals(enteredItemValue) || (selectedOptionKey != null && !"" .equals(selectedOptionKey))) {
                             //VConsole.log("doItemAction with " + potentialExactMatch + " called from doPostFilterSelectedItemAction ");
                             doItemAction(potentialExactMatch, true);
                         }
                         //VConsole.log("hide popup called from doPostFilterSelectedItemAction ");
                         suggestionPopup.hide();
                         return;
                     }
                 }
             }
             if (item != null && !"".equals(lastFilter)) {
                 //VConsole.log("doItemAction with " + item + " called from doPostFilterSelectedItemAction ");
                 doItemAction(item, true);
             } else {
                 // currentSuggestion has key="" for nullselection
                 if (currentSuggestion != null && !currentSuggestion.key.equals("")) {
                     //VConsole.log("current suggestion is not null in doPostFilterSelectedItemAction ");
                     // An item (not null) selected
                     String text = currentSuggestion.getReplacementString();
                     tb.setText(text);
                     selectedOptionKey = currentSuggestion.key;
                     //VConsole.log("selected option key = " + selectedOptionKey + " in doPostFilterSelectedItemAction ");
                 } else {
                     //VConsole.log("current suggestion is not null in doPostFilterSelectedItemAction ");
                     // Null selected
                     //tb.setText("");
                     selectedOptionKey = null;
                 }
             }
             //VConsole.log("hide popup at end of called from doPostFilterSelectedItemAction ");
             suggestionPopup.hide();
         }
 
         private static final String SUBPART_PREFIX = "item";
 
         public Element getSubPartElement(String subPart) {
             int index = Integer.parseInt(subPart.substring(SUBPART_PREFIX.length()));
 
             MenuItem item = getItems().get(index);
 
             return item.getElement();
         }
 
         public String getSubPartName(Element subElement) {
             if (!getElement().isOrHasChild(subElement)) {
                 return null;
             }
 
             Element menuItemRoot = subElement;
             while (menuItemRoot != null && !menuItemRoot.getTagName().equalsIgnoreCase("td")) {
                 menuItemRoot = menuItemRoot.getParentElement().cast();
             }
             // "menuItemRoot" is now the root of the menu item
 
             final int itemCount = getItems().size();
             for (int i = 0; i < itemCount; i++) {
                 if (getItems().get(i).getElement() == menuItemRoot) {
                     return SUBPART_PREFIX + i;
                 }
             }
             return null;
         }
 
         public void onLoad(LoadEvent event) {
             if (BrowserInfo.get().isIE6()) {
                 // Ensure PNG transparency works in IE6
                 Util.doIE6PngFix((Element) Element.as(event.getNativeEvent().getEventTarget()));
             }
 
             // Handle icon onload events to ensure shadow is resized
             // correctly
             delayedImageLoadExecutioner.trigger();
         }
     }
 
     public static final String ATTR_TEXTCHANGE_TIMEOUT = "iet";
     public static final String ATTR_TEXT_CHANGED = "textChanged";
     public static final String ATTR_TEXTCHANGE_EVENTMODE = "iem";
     private static final String TEXTCHANGE_MODE_TIMEOUT = "TIMEOUT";
 
     private static final String CLASSNAME = "v-filterselect";
     private static final String STYLE_NO_INPUT = "no-input";
 
     protected int pageLength = 10;
 
     private final FlowPanel panel = new FlowPanel();
 
     /**
      * The text box where the filter is written
      */
     private final TextBox tb = new TextBox() {
         /*
          * (non-Javadoc)
          *
          * @see
          * com.google.gwt.user.client.ui.TextBoxBase#onBrowserEvent(com.google
          * .gwt.user.client.Event)
          */
         @Override
         public void onBrowserEvent(Event event) {
             super.onBrowserEvent(event);
             if (client != null) {
                 client.handleTooltipEvent(event, VLocationTextField.this);
             }
             if ((event.getTypeInt() & TEXTCHANGE_EVENTS) == event.getTypeInt()) {
                 deferTextChangeEvent();
             }
         }
 
         @Override
         // Overridden to avoid selecting text when text input is disabled
         public void setSelectionRange(int pos, int length) {
             if (textInputEnabled) {
                 super.setSelectionRange(pos, length);
             } else {
                 super.setSelectionRange(getValue().length(), 0);
             }
         }
 
         @Override
         protected void onDetach() {
             super.onDetach();
             detachCutEventListener(getElement());
         }
 
         @Override
         protected void onAttach() {
             super.onAttach();
             detachCutEventListener(tb.getElement());
         }
     };
 
     protected void onCut() {
         deferTextChangeEvent();
     }
 
     protected native void attachCutEventListener(Element el)
     /*-{
         var me = this;
         el.oncut = function() {
             me.@org.vaadin.addons.locationtextfield.client.ui.VLocationTextField::onCut()();
         };
     }-*/;
 
     protected native void detachCutEventListener(Element el)
     /*-{
         el.oncut = null;
     }-*/;
 
     private final SuggestionPopup suggestionPopup = new SuggestionPopup();
 
     /**
      * Used when measuring the width of the popup
      */
     private final HTML popupOpener = new HTML("") {
 
         /*
          * (non-Javadoc)
          *
          * @see
          * com.google.gwt.user.client.ui.Widget#onBrowserEvent(com.google.gwt
          * .user.client.Event)
          */
         @Override
         public void onBrowserEvent(Event event) {
             super.onBrowserEvent(event);
             if (client != null) {
                 client.handleTooltipEvent(event, VLocationTextField.this);
             }
 
             /*
              * Prevent the keyboard focus from leaving the textfield by
              * preventing the default behaviour of the browser. Fixes #4285.
              */
             handleMouseDownEvent(event);
         }
     };
 
     private final Image selectedItemIcon = new Image();
 
     private ApplicationConnection client;
 
     private String paintableId;
 
     private int currentPage;
 
     /**
      * A collection of available suggestions (options) as received from the
      * server.
      */
     private final Collection<FilterSelectSuggestion> currentSuggestions = new ArrayList<FilterSelectSuggestion>();
 
     private boolean immediate;
 
     private String selectedOptionKey;
 
     private boolean tabPressed = false;
     private boolean initDone = false;
 
     private String lastFilter = "";
     private int lastIndex = -1; // last selected index when using arrows
 
     /**
      * The current suggestion selected from the dropdown. This is one of the
      * values in currentSuggestions except when filtering, in this case
      * currentSuggestion might not be in currentSuggestions.
      */
     private FilterSelectSuggestion currentSuggestion;
 
     private int totalMatches;
     private boolean nullSelectionAllowed;
     private boolean nullSelectItem;
     private boolean enabled;
     private boolean readonly;
 
     // shown in unfocused empty field, disappears on focus (e.g "Search here")
     private static final String CLASSNAME_PROMPT = "prompt";
     private static final String ATTR_INPUTPROMPT = "prompt";
     public static final String ATTR_NO_TEXT_INPUT = "noInput";
     private boolean prompting = false;
 
     // Set true when popupopened has been clicked. Cleared on each UIDL-update.
     // This handles the special case where are not filtering yet and the
     // selected value has changed on the server-side. See #2119
     private boolean popupOpenerClicked;
     private String width = null;
     private int textboxPadding = -1;
     private int componentPadding = -1;
     private int suggestionPopupMinWidth = 0;
     private int popupWidth = -1;
     private boolean focused = false;
     private int horizPaddingAndBorder = 2;
 
     /**
      * If set to false, the component should not allow entering text to the
      * field even for filtering.
      */
     private boolean textInputEnabled = true;
 
     private boolean scheduled = false;
 
     private String valueBeforeEdit = null;
     private String textChangeEventMode;
     private int textChangeEventTimeout;
 
     /**
      * Default constructor
      */
     public VLocationTextField() {
         selectedItemIcon.setStyleName("v-icon");
         selectedItemIcon.addLoadHandler(new LoadHandler() {
             public void onLoad(LoadEvent event) {
                 updateRootWidth();
                 updateSelectedIconPosition();
                 /*
                  * Workaround for an IE bug where the text is positioned below
                  * the icon (#3991)
                  */
                 if (BrowserInfo.get().isIE()) {
                     Util.setStyleTemporarily(tb.getElement(), "paddingLeft", "0");
                 }
             }
         });
 
         tb.sinkEvents(VTooltip.TOOLTIP_EVENTS);
         popupOpener.sinkEvents(VTooltip.TOOLTIP_EVENTS | Event.ONMOUSEDOWN);
         panel.add(tb);
         panel.add(popupOpener);
         initWidget(panel);
         setStyleName(CLASSNAME);
         tb.addChangeHandler(this);
         tb.addKeyDownHandler(this);
         tb.addKeyUpHandler(this);
         tb.setStyleName(CLASSNAME + "-input");
         tb.addFocusHandler(this);
         tb.addBlurHandler(this);
         tb.addClickHandler(this);
         popupOpener.setStyleName(CLASSNAME + "-button");
         popupOpener.addClickHandler(this);
     }
 
     /*
      * TODO When GWT adds ONCUT, add it there and remove workaround. See
      * http://code.google.com/p/google-web-toolkit/issues/detail?id=4030
      *
      * Also note that the cut/paste are not totally crossbrowsers compatible.
      * E.g. in Opera mac works via context menu, but on via File->Paste/Cut.
      * Opera might need the polling method for 100% working textchanceevents.
      * Eager polling for a change is bit dum and heavy operation, so I guess we
      * should first try to survive without.
      */
     private static final int TEXTCHANGE_EVENTS = Event.ONPASTE | Event.KEYEVENTS | Event.ONMOUSEUP;
 
     private boolean communicateTextValueToServer() {
         String text = tb.getText();
         if (prompting) {
             // Input prompt visible, text is actually ""
             text = "";
         }
         if (!text.equals(lastFilter)) {
             lastFilter = text;
             client.updateVariable(paintableId, "filter", text, false);
             client.updateVariable(paintableId, "page", currentPage, false);
             return true;
         }
         return false;
     }
 
     private Timer textChangeEventTrigger = new Timer() {
 
         @Override
         public void run() {
             if (tb.isAttached()) {
                 boolean textChanged = communicateTextValueToServer();
                 if (textChanged) {
                     client.sendPendingVariableChanges();
                 }
                 scheduled = false;
             }
         }
     };
 
     @Override
     public void onChange(ChangeEvent event) {
         valueChange(false);
     }
 
     /**
      * Called when the field value might have changed and/or the field was
      * blurred. These are combined so the blur event is sent in the same batch
      * as a possible value change event (these are often connected).
      *
      * @param blurred
      *            true if the field was blurred
      */
     public void valueChange(boolean blurred) {
         if (client != null && paintableId != null) {
             boolean sendBlurEvent = false;
             boolean sendValueChange = false;
 
             if (blurred && client.hasEventListeners(this, EventId.BLUR)) {
                 sendBlurEvent = true;
                 client.updateVariable(paintableId, EventId.BLUR, "", false);
             }
 
             String newText = tb.getText();
             if (!prompting && newText != null && !newText.equals(valueBeforeEdit)) {
                 sendValueChange = immediate;
                 lastFilter = newText;
                 client.updateVariable(paintableId, "filter", lastFilter, false);
                 client.updateVariable(paintableId, "page", currentPage, false);
                 valueBeforeEdit = newText;
             }
 
             if (sendBlurEvent || sendValueChange) {
                 /*
                  * Avoid sending text change event as we will simulate it on the
                  * server side before value change events.
                  */
                 textChangeEventTrigger.cancel();
                 scheduled = false;
                 client.sendPendingVariableChanges();
             }
         }
     }
 
     private void deferTextChangeEvent() {
         if (textChangeEventMode.equals(TEXTCHANGE_MODE_TIMEOUT) && scheduled) {
             return;
         } else {
             textChangeEventTrigger.cancel();
         }
         textChangeEventTrigger.schedule(getTextChangeEventTimeout());
         scheduled = true;
     }
 
     private int getTextChangeEventTimeout() {
         return textChangeEventTimeout;
     }
 
     /**
      * Does the Select have more pages?
      *
      * @return true if a next page exists, else false if the current page is the
      *         last page
      */
     public boolean hasNextPage() {
         return totalMatches > (currentPage + 1) * pageLength;
     }
 
     /**
      * Filters the options at a certain page. Uses the text box input as a
      * filter
      *
      * @param page
      *            The page which items are to be filtered
      */
     public void filterOptions(int page) {
         filterOptions(page, tb.getText());
     }
 
     /**
      * Filters the options at certain page using the given filter
      *
      * @param page
      *            The page to filter
      * @param filter
      *            The filter to apply to the components
      */
     public void filterOptions(int page, String filter) {
         if (filter.equals(lastFilter) && currentPage == page) {
             if (!suggestionPopup.isAttached()) {
                 suggestionPopup.showSuggestions(currentSuggestions, currentPage, totalMatches);
             }
             return;
         }
         if (!filter.equals(lastFilter)) {
             page = 0;
         }
 
         if (currentPage != page) {
             client.updateVariable(paintableId, "filter", filter, false);
             client.updateVariable(paintableId, "page", page, true);
             lastFilter = filter;
             currentPage = page;
         }
     }
 
     /*
      * (non-Javadoc)
      *
      * @see
      * com.vaadin.terminal.gwt.client.Paintable#updateFromUIDL(com.vaadin.terminal
      * .gwt.client.UIDL, com.vaadin.terminal.gwt.client.ApplicationConnection)
      */
     @SuppressWarnings("deprecation")
     public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
         boolean firstPaint = (this.client == null);
         paintableId = uidl.getId();
         this.client = client;
 
         readonly = uidl.hasAttribute("readonly");
         enabled = !uidl.hasAttribute("disabled");
 
         tb.setEnabled(enabled);
         updateReadOnly();
 
         if (client.updateComponent(this, uidl, true)) {
             return;
         }
 
         // Inverse logic here to make the default case (text input enabled)
         // work without additional UIDL messages
         boolean noTextInput = uidl.hasAttribute(ATTR_NO_TEXT_INPUT)
                 && uidl.getBooleanAttribute(ATTR_NO_TEXT_INPUT);
         setTextInputEnabled(!noTextInput);
 
         // not a FocusWidget -> needs own tabindex handling
         if (uidl.hasAttribute("tabindex")) {
             tb.setTabIndex(uidl.getIntAttribute("tabindex"));
         }
 
         immediate = uidl.hasAttribute("immediate");
 
         nullSelectionAllowed = uidl.hasAttribute("nullselect");
 
         nullSelectItem = uidl.hasAttribute("nullselectitem")
                 && uidl.getBooleanAttribute("nullselectitem");
 
         currentPage = Math.max(uidl.getIntVariable("page"), 0);
 
         if (uidl.hasAttribute("pagelength")) {
             pageLength = uidl.getIntAttribute("pagelength");
         }
 
         String inputPrompt;
         if (uidl.hasAttribute(ATTR_INPUTPROMPT)) {
             // input prompt changed from server
             inputPrompt = uidl.getStringAttribute(ATTR_INPUTPROMPT);
         } else {
             inputPrompt = "";
         }
 
         textChangeEventMode = uidl.getStringAttribute(ATTR_TEXTCHANGE_EVENTMODE);
         textChangeEventTimeout = uidl.getIntAttribute(ATTR_TEXTCHANGE_TIMEOUT);
         if (textChangeEventTimeout < 1) {
             // Sanitize and allow lazy/timeout with timeout set to 0 to
             // work as eager
             textChangeEventTimeout = 1000;
         }
         sinkEvents(TEXTCHANGE_EVENTS);
         attachCutEventListener(tb.getElement());
 
         final String text;
         if (uidl.hasVariable("filter") && (firstPaint || (uidl.hasAttribute(ATTR_TEXT_CHANGED)
           && uidl.getBooleanAttribute(ATTR_TEXT_CHANGED)))) {
             // Use value from UIDL if this is the first time the component is
             // painted or if something has changed on the server
             text = uidl.getStringVariable("filter");
         } else {
             // Use what we already have if no change from the server
             text = prompting ? null : tb.getText();
         }
         this.prompting = inputPrompt != null && focusedTextField != this && (text == null || text.equals(""));
 
         if (BrowserInfo.get().isFF3()) {
             /*
              * Firefox 3 is really sluggish when updating input attached to dom.
              * Some optimizations seems to work much better in Firefox3 if we
              * update the actual content lazily when the rest of the DOM has
              * stabilized. In tests, about ten times better performance is
              * achieved with this optimization. See for eg. #2898
              */
             Scheduler.get().scheduleDeferred(new Command() {
                 public void execute() {
                     //String fieldValue;
                     if (prompting) {
                         //fieldValue = tb.isReadOnly() ? "" : inputPrompt;
                         addStyleDependentName(CLASSNAME_PROMPT);
                     } else {
                         //fieldValue = text;
                         removeStyleDependentName(CLASSNAME_PROMPT);
                     }
                     /*
                      * Avoid resetting the old value. Prevents cursor flickering
                      * which then again happens due to this Gecko hack.
                      */
                     if (!tb.getText().equals(text)) {
                         tb.setText(text);
                     }
                 }
             });
         } else {
             //String fieldValue;
             if (prompting) {
                 //fieldValue = tb.isReadOnly() ? "" : inputPrompt;
                 addStyleDependentName(CLASSNAME_PROMPT);
             } else {
                 //fieldValue = text;
                 removeStyleDependentName(CLASSNAME_PROMPT);
             }
             //tb.setText(fieldValue);
             tb.setText(text);
         }
 
         lastFilter = valueBeforeEdit = text;
 
         suggestionPopup.setPagingEnabled(true);
         suggestionPopup.updateStyleNames(uidl);
 
         currentSuggestions.clear();
         /*
          * Clear the current suggestions as the server response always
          * includes the new ones. Exception is when filtering, then we need
          * to retain the value if the user does not select any of the
          * options matching the filter.
          */
         this.currentSuggestion = null;
         /*
          * Also ensure no old items in menu. Unless cleared the old values
          * may cause odd effects on blur events. Suggestions in menu might
          * not necessary exist in select at all anymore.
          */
         suggestionPopup.menu.clearItems();
 
         final UIDL options = uidl.getChildUIDL(0);
         if (uidl.hasAttribute("totalMatches")) {
             totalMatches = uidl.getIntAttribute("totalMatches");
         }
 
         // used only to calculate minimum popup width
         String captions = Util.escapeHTML(inputPrompt);
 
         for (final Iterator<?> i = options.getChildIterator(); i.hasNext();) {
             final UIDL optionUidl = (UIDL) i.next();
             final FilterSelectSuggestion suggestion = new FilterSelectSuggestion(optionUidl);
             currentSuggestions.add(suggestion);
             if (optionUidl.hasAttribute("selected")) {
                 //VConsole.log("popupOpenerClicked " + popupOpenerClicked);
                 //if (popupOpenerClicked) {
                     String newSelectedOptionKey = Integer.toString(suggestion.getOptionKey());
                     if (!newSelectedOptionKey.equals(selectedOptionKey) || suggestion.getReplacementString().equals(tb.getText())) {
                         // Update text field if we've got a new selection
                         // Also update if we've got the same text to retain old
                         // text selection behavior
                         setPromptingOff(suggestion.getReplacementString());
                         selectedOptionKey = newSelectedOptionKey;
                     }
                 //}
                 currentSuggestion = suggestion;
                 setSelectedItemIcon(suggestion.getIconUri());
             }
 
             // Collect captions so we can calculate minimum width for textarea
             if (captions.length() > 0) {
                 captions += "|";
             }
             captions += Util.escapeHTML(suggestion.getReplacementString());
         }
 
         if ((popupOpenerClicked) && uidl.hasVariable("selected")
           && uidl.getStringArrayVariable("selected").length == 0) {
             //VConsole.log("select nulled");
             // select nulled
             if (!popupOpenerClicked) {
                 if (!focused) {
                     /*
                      * client.updateComponent overwrites all styles so we must
                      * ALWAYS set the prompting style at this point, even though
                      * we think it has been set already...
                      */
                     prompting = false;
                     setPromptingOn();
                 }
             }
             selectedOptionKey = null;
         }
 
         suggestionPopup.showSuggestions(currentSuggestions, currentPage, totalMatches);
         //VConsole.log("showing suggestions");
         if (!popupOpenerClicked && lastIndex != -1) {
             // we're paging w/ arrows
             //VConsole.log("pagin with arrows");
             MenuItem activeMenuItem;
             if (lastIndex == 0) {
                 // going up, select last item
                 int lastItem = pageLength - 1;
                 List<MenuItem> items = suggestionPopup.menu.getItems();
                 /*
                  * The first page can contain less than 10 items if the null
                  * selection item is filtered away
                  */
                 if (lastItem >= items.size()) {
                     lastItem = items.size() - 1;
                 }
                 activeMenuItem = items.get(lastItem);
                 suggestionPopup.menu.selectItem(activeMenuItem);
             } else {
                 // going down, select first item
                 //VConsole.log("select first item");
                 activeMenuItem = suggestionPopup.menu.getItems().get(0);
                 suggestionPopup.menu.selectItem(activeMenuItem);
             }
 
             setTextboxText(activeMenuItem.getText());
             tb.setSelectionRange(lastFilter.length(), Math.abs(activeMenuItem.getText().length() - lastFilter.length()));
 
             lastIndex = -1; // reset
         }
         //TODO: is this necessary?
         if (currentSuggestion != null)
             suggestionPopup.menu.doPostFilterSelectedItemAction();
 
         // Calculate minumum textarea width
         suggestionPopupMinWidth = minWidth(captions);
 
         popupOpenerClicked = false;
 
         if (!initDone) {
             updateRootWidth();
         }
 
         // Focus dependent style names are lost during the update, so we add
         // them here back again
         if (focused) {
             addStyleDependentName("focus");
         }
 
         initDone = true;
     }
 
     private static VLocationTextField focusedTextField;
 
     public static void flushChangesFromFocusedTextField() {
         if (focusedTextField != null) {
             focusedTextField.onChange(null);
         }
     }
 
     private void updateReadOnly() {
         tb.setReadOnly(readonly || !textInputEnabled);
     }
 
     private void setTextInputEnabled(boolean textInputEnabled) {
         // Always update styles as they might have been overwritten
         if (textInputEnabled) {
             removeStyleDependentName(STYLE_NO_INPUT);
         } else {
             addStyleDependentName(STYLE_NO_INPUT);
         }
 
         if (this.textInputEnabled == textInputEnabled) {
             return;
         }
 
         this.textInputEnabled = textInputEnabled;
         updateReadOnly();
     }
 
     /**
      * Sets the text in the text box using a deferred command if on Gecko. This
      * is required for performance reasons (see #3663).
      *
      * @param text
      *            the text to set in the text box
      */
     private void setTextboxText(final String text) {
         if (BrowserInfo.get().isFF3()) {
             Scheduler.get().scheduleDeferred(new Command() {
                 public void execute() {
                     tb.setText(text);
                 }
             });
         } else {
             tb.setText(text);
         }
     }
 
     @Override
     protected void onDetach() {
         super.onDetach();
         suggestionPopup.hide();
         if (focusedTextField == this) {
             focusedTextField = null;
         }
     }
 
     @Override
     protected void onAttach() {
         super.onAttach();
 
         /*
          * We need to recalculate the root width when the select is attached, so
          * #2974 won't happen.
          */
         updateRootWidth();
     }
 
     /**
      * Turns prompting on. When prompting is turned on a command prompt is shown
      * in the text box if nothing has been entered.
      */
     private void setPromptingOn() {
         if (!prompting) {
             prompting = true;
             addStyleDependentName(CLASSNAME_PROMPT);
         }
     }
 
     /**
      * Turns prompting off. When prompting is turned on a command prompt is
      * shown in the text box if nothing has been entered.
      *
      * @param text
      *            The text the text box should contain.
      */
     private void setPromptingOff(String text) {
         if (text != null && !"".equals(text.trim()))
             setTextboxText(text.trim());
         if (prompting) {
             prompting = false;
             removeStyleDependentName(CLASSNAME_PROMPT);
         }
     }
 
     /**
      * Triggered when a suggestion is selected
      *
      * @param suggestion
      *            The suggestion that just got selected.
      */
     public void onSuggestionSelected(FilterSelectSuggestion suggestion) {
         currentSuggestion = suggestion;
         String newKey;
         if (suggestion.key.equals("")) {
             // "nullselection"
             newKey = "";
         } else {
             // normal selection
             newKey = String.valueOf(suggestion.getOptionKey());
         }
 
         String text = suggestion.getReplacementString();
         if ("".equals(newKey) && !focused) {
             setPromptingOn();
         } else {
             //VConsole.log("location - line 1483 - " + text);
             setPromptingOff(text);
         }
         setSelectedItemIcon(suggestion.getIconUri());
         if (!(newKey.equals(selectedOptionKey) || ("".equals(newKey) && selectedOptionKey == null))) {
             selectedOptionKey = newKey;
             client.updateVariable(paintableId, "selected", new String[] { selectedOptionKey }, immediate);
             // currentPage = -1; // forget the page
         }
         suggestionPopup.hide();
     }
 
     /**
      * Sets the icon URI of the selected item. The icon is shown on the left
      * side of the item caption text. Set the URI to null to remove the icon.
      *
      * @param iconUri
      *            The URI of the icon
      */
     private void setSelectedItemIcon(String iconUri) {
         if (iconUri == null || iconUri.equals("")) {
             panel.remove(selectedItemIcon);
             updateRootWidth();
         } else {
             panel.insert(selectedItemIcon, 0);
             selectedItemIcon.setUrl(iconUri);
             updateRootWidth();
             updateSelectedIconPosition();
         }
     }
 
     /**
      * Positions the icon vertically in the middle. Should be called after the
      * icon has loaded
      */
     private void updateSelectedIconPosition() {
         // Position icon vertically to middle
         int availableHeight;
         if (BrowserInfo.get().isIE6()) {
             getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
             availableHeight = getOffsetHeight();
             getElement().getStyle().setProperty("overflow", "");
         } else {
             availableHeight = getOffsetHeight();
         }
 
         int iconHeight = Util.getRequiredHeight(selectedItemIcon);
         int marginTop = (availableHeight - iconHeight) / 2;
         DOM.setStyleAttribute(selectedItemIcon.getElement(), "marginTop", marginTop + "px");
     }
 
     /*
      * (non-Javadoc)
      *
      * @see
      * com.google.gwt.event.dom.client.KeyDownHandler#onKeyDown(com.google.gwt
      * .event.dom.client.KeyDownEvent)
      */
     public void onKeyDown(KeyDownEvent event) {
         if (enabled && !readonly) {
             if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                 // Same reaction to enter no matter on whether the popup is open
                 if (suggestionPopup.isAttached()) {
                     filterOptions(currentPage);
                 } else if (currentSuggestion != null && tb.getText().equals(currentSuggestion.getReplacementString())) {
                     // Retain behavior from #6686 by returning without stopping
                     // propagation if there's nothing to do
                     return;
                 }
                 if (currentSuggestions.size() == 1) {
                     // If there is only one suggestion, select that
                     suggestionPopup.menu.selectItem(suggestionPopup.menu.getItems().get(0));
                 }
                 suggestionPopup.menu.doSelectedItemAction();
 
                 event.stopPropagation();
             } else if (suggestionPopup.isAttached()) {
                 popupKeyDown(event);
             } else {
                 inputFieldKeyDown(event);
             }
         }
     }
 
     /**
      * Triggered when a key is pressed in the text box
      *
      * @param event
      *            The KeyDownEvent
      */
     private void inputFieldKeyDown(KeyDownEvent event) {
         switch (event.getNativeKeyCode()) {
         case KeyCodes.KEY_DOWN:
         case KeyCodes.KEY_UP:
         case KeyCodes.KEY_PAGEDOWN:
         case KeyCodes.KEY_PAGEUP:
             if (!suggestionPopup.isAttached()) {
                 // open popup as from gadget
                 filterOptions(-1, "");
                 lastFilter = "";
                 tb.selectAll();
             }
             break;
         case KeyCodes.KEY_TAB:
             if (suggestionPopup.isAttached()) {
                 filterOptions(currentPage, tb.getText());
             }
             break;
         }
 
     }
 
     /**
      * Triggered when a key was pressed in the suggestion popup.
      *
      * @param event
      *            The KeyDownEvent of the key
      */
     private void popupKeyDown(KeyDownEvent event) {
         // Propagation of handled events is stopped so other handlers such as
         // shortcut key handlers do not also handle the same events.
         switch (event.getNativeKeyCode()) {
         case KeyCodes.KEY_DOWN:
             suggestionPopup.selectNextItem();
             DOM.eventPreventDefault(DOM.eventGetCurrentEvent());
             event.stopPropagation();
             break;
         case KeyCodes.KEY_UP:
             suggestionPopup.selectPrevItem();
             DOM.eventPreventDefault(DOM.eventGetCurrentEvent());
             event.stopPropagation();
             break;
         case KeyCodes.KEY_PAGEDOWN:
             if (hasNextPage()) {
                 filterOptions(currentPage + 1, lastFilter);
             }
             event.stopPropagation();
             break;
         case KeyCodes.KEY_PAGEUP:
             if (currentPage > 0) {
                 filterOptions(currentPage - 1, lastFilter);
             }
             event.stopPropagation();
             break;
         case KeyCodes.KEY_TAB:
             if (suggestionPopup.isAttached()) {
                 tabPressed = true;
                 filterOptions(currentPage);
             }
             // onBlur() takes care of the rest
             break;
         }
 
     }
 
     /**
      * Triggered when a key was depressed
      *
      * @param event
      *            The KeyUpEvent of the key depressed
      */
     public void onKeyUp(KeyUpEvent event) {
         if (enabled && !readonly) {
             switch (event.getNativeKeyCode()) {
             case KeyCodes.KEY_ENTER:
             case KeyCodes.KEY_TAB:
             case KeyCodes.KEY_SHIFT:
             case KeyCodes.KEY_CTRL:
             case KeyCodes.KEY_ALT:
             case KeyCodes.KEY_DOWN:
             case KeyCodes.KEY_UP:
             case KeyCodes.KEY_PAGEDOWN:
             case KeyCodes.KEY_PAGEUP:
                 break;
             case KeyCodes.KEY_ESCAPE:
                 reset();
                 break;
             default:
                 if (textInputEnabled) {
                     filterOptions(currentPage);
                 }
                 break;
             }
         }
     }
 
     /**
      * Resets the Select to its initial state
      */
     private void reset() {
         //VConsole.log("location - reset called");
         if (currentSuggestion != null) {
             String text = currentSuggestion.getReplacementString();
             setPromptingOff(text);
             selectedOptionKey = currentSuggestion.key;
         } else {
             if (focused) {
                 //VConsole.log("location - line 1684");
                 setPromptingOff("");
             } else {
                 //VConsole.log("location - line 1687");
                 setPromptingOn();
             }
             selectedOptionKey = null;
         }
         lastFilter = "";
         suggestionPopup.hide();
     }
 
     /**
      * Listener for popupopener
      */
     public void onClick(ClickEvent event) {
         VConsole.log("click event: t in enabled? " + textInputEnabled);
         VConsole.log("on txt field? " + (event.getNativeEvent().getEventTarget().cast() == tb.getElement()));
         if (textInputEnabled && event.getNativeEvent().getEventTarget().cast() == tb.getElement()) {
             // Don't process clicks on the text field if text input is enabled
             return;
         }
         VConsole.log("enabled: " + enabled);
         if (enabled && !readonly) {
             // ask suggestionPopup if it was just closed, we are using GWT
             // Popup's auto close feature
             if (!suggestionPopup.isJustClosed()) {
                 filterOptions(-1, "");
                 popupOpenerClicked = true;
                 lastFilter = "";
             }
             //DOM.eventPreventDefault(DOM.eventGetCurrentEvent());
             focus();
             tb.selectAll();
             suggestionPopup.show();
         }
     }
 
     /**
      * Calculate minimum width for FilterSelect textarea
      */
     private native int minWidth(String captions)
     /*-{
         if(!captions || captions.length <= 0)
                 return 0;
         captions = captions.split("|");
         var d = $wnd.document.createElement("div");
         var html = "";
         for(var i=0; i < captions.length; i++) {
                 html += "<div>" + captions[i] + "</div>";
                 // TODO apply same CSS classname as in suggestionmenu
         }
         d.style.position = "absolute";
         d.style.top = "0";
         d.style.left = "0";
         d.style.visibility = "hidden";
         d.innerHTML = html;
         $wnd.document.body.appendChild(d);
         var w = d.offsetWidth;
         $wnd.document.body.removeChild(d);
         return w;
     }-*/;
 
     /**
      * A flag which prevents a focus event from taking place
      */
     boolean iePreventNextFocus = false;
 
     /*
      * (non-Javadoc)
      *
      * @see
      * com.google.gwt.event.dom.client.FocusHandler#onFocus(com.google.gwt.event
      * .dom.client.FocusEvent)
      */
     public void onFocus(FocusEvent event) {
 
         /*
          * When we disable a blur event in ie we need to refocus the textfield.
          * This will cause a focus event we do not want to process, so in that
          * case we just ignore it.
          */
         if (BrowserInfo.get().isIE() && iePreventNextFocus) {
             iePreventNextFocus = false;
             return;
         }
 
         focused = true;
         if (prompting && !readonly) {
             //VConsole.log("location - line 1768; prompting onFocus");
             setPromptingOff("");
         }
         addStyleDependentName("focus");
 
         focusedTextField = this;
 
         if (client.hasEventListeners(this, EventId.FOCUS)) {
             client.updateVariable(paintableId, EventId.FOCUS, "", true);
         }
     }
 
     /**
      * A flag which cancels the blur event and sets the focus back to the
      * textfield if the Browser is IE
      */
     boolean preventNextBlurEventInIE = false;
 
     /*
      * (non-Javadoc)
      *
      * @see
      * com.google.gwt.event.dom.client.BlurHandler#onBlur(com.google.gwt.event
      * .dom.client.BlurEvent)
      */
 
     public void onBlur(BlurEvent event) {
 
         if (BrowserInfo.get().isIE() && preventNextBlurEventInIE) {
             /*
              * Clicking in the suggestion popup or on the popup button in IE
              * causes a blur event to be sent for the field. In other browsers
              * this is prevented by canceling/preventing default behavior for
              * the focus event, in IE we handle it here by refocusing the text
              * field and ignoring the resulting focus event for the textfield
              * (in onFocus).
              */
             preventNextBlurEventInIE = false;
 
             Element focusedElement = Util.getIEFocusedElement();
             if (getElement().isOrHasChild(focusedElement) || suggestionPopup.getElement().isOrHasChild(focusedElement)) {
 
                 // IF the suggestion popup or another part of the VFilterSelect
                 // was focused, move the focus back to the textfield and prevent
                 // the triggered focus event (in onFocus).
                 iePreventNextFocus = true;
                 tb.setFocus(true);
                 return;
             }
         }
 
         focused = false;
         if (!readonly) {
             // much of the TAB handling takes place here
             if (tabPressed) {
                 tabPressed = false;
                 suggestionPopup.menu.doSelectedItemAction();
                 suggestionPopup.hide();
             }/* else if (!suggestionPopup.isAttached() || suggestionPopup.isJustClosed()) {
                 suggestionPopup.menu.doSelectedItemAction();
             }*/
             if (selectedOptionKey == null) {
                 setPromptingOn();
             } else if (currentSuggestion != null) {
                 //VConsole.log("location - line 1834 - " + currentSuggestion.caption);
                 setPromptingOff(currentSuggestion.caption);
             }
         }
         removeStyleDependentName("focus");
 
         focusedTextField = null;
 
         valueChange(true);
 
         if (client.hasEventListeners(this, EventId.BLUR)) {
             client.updateVariable(paintableId, EventId.BLUR, "", true);
         }
     }
 
     /*
      * (non-Javadoc)
      *
      * @see com.vaadin.terminal.gwt.client.Focusable#focus()
      */
     public void focus() {
         focused = true;
         if (prompting && !readonly) {
             //VConsole.log("location - line 1856 prompting focus()");
             setPromptingOff("");
         }
         tb.setFocus(true);
     }
 
     /*
      * (non-Javadoc)
      *
      * @see com.google.gwt.user.client.ui.UIObject#setWidth(java.lang.String)
      */
     @Override
     public void setWidth(String width) {
         if (width == null || width.equals("")) {
             this.width = null;
         } else {
             this.width = width;
         }
 
         if (BrowserInfo.get().isIE6()) {
             // Required in IE when textfield is wider than this.width
             getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
             horizPaddingAndBorder = Util.setWidthExcludingPaddingAndBorder(this, width, horizPaddingAndBorder);
             getElement().getStyle().setProperty("overflow", "");
         } else {
             horizPaddingAndBorder = Util.setWidthExcludingPaddingAndBorder(this, width, horizPaddingAndBorder);
         }
 
         if (initDone) {
             updateRootWidth();
         }
     }
 
     /*
      * (non-Javadoc)
      *
      * @see com.google.gwt.user.client.ui.UIObject#setHeight(java.lang.String)
      */
     @Override
     public void setHeight(String height) {
         super.setHeight(height);
         Util.setHeightExcludingPaddingAndBorder(tb, height, 3);
     }
 
     /**
      * Calculates the width of the select if the select has undefined width.
      * Should be called when the width changes or when the icon changes.
      */
     private void updateRootWidth() {
         if (width == null) {
             /*
              * When the width is not specified we must specify width for root
              * div so the popupopener won't wrap to the next line and also so
              * the size of the combobox won't change over time.
              */
             int tbWidth = Util.getRequiredWidth(tb);
 
             if (popupWidth < 0) {
                 /*
                  * Only use the first page popup width so the textbox will not
                  * get resized whenever the popup is resized.
                  */
                 popupWidth = Util.getRequiredWidth(popupOpener);
             }
 
             /*
              * Note: iconWidth is here calculated as a negative pixel value so
              * you should consider this in further calculations.
              */
             int iconWidth = selectedItemIcon.isAttached() ? Util.measureMarginLeft(tb.getElement())
               - Util.measureMarginLeft(selectedItemIcon.getElement()) : 0;
 
             int w = tbWidth + popupWidth + iconWidth;
 
             /*
              * When the select has a undefined with we need to check that we are
              * only setting the text box width relative to the first page width
              * of the items. If this is not done the text box width will change
              * when the popup is used to view longer items than the text box is
              * wide.
              */
             if ((!initDone || currentPage + 1 < 0) && suggestionPopupMinWidth > w) {
                 setTextboxWidth(suggestionPopupMinWidth);
                 w = suggestionPopupMinWidth;
             } else {
                 /*
                  * Firefox3 has its own way of doing rendering so we need to
                  * specify the width for the TextField to make sure it actually
                  * is rendered as wide as FF3 says it is
                  */
                 tb.setWidth((tbWidth - getTextboxPadding()) + "px");
             }
             super.setWidth((w) + "px");
             // Freeze the initial width, so that it won't change even if the
             // icon size changes
             width = w + "px";
 
         } else {
             /*
              * When the width is specified we also want to explicitly specify
              * widths for textbox and popupopener
              */
             setTextboxWidth(getMainWidth() - getComponentPadding());
         }
     }
 
     /**
      * Get the width of the select in pixels where the text area and icon has
      * been included.
      *
      * @return The width in pixels
      */
     private int getMainWidth() {
         int componentWidth;
         if (BrowserInfo.get().isIE6()) {
             // Required in IE when textfield is wider than this.width
             getElement().getStyle().setOverflow(Style.Overflow.HIDDEN);
             componentWidth = getOffsetWidth();
             getElement().getStyle().setProperty("overflow", "");
         } else {
             componentWidth = getOffsetWidth();
         }
         return componentWidth;
     }
 
     /**
      * Sets the text box width in pixels.
      *
      * @param componentWidth
      *            The width of the text box in pixels
      */
     private void setTextboxWidth(int componentWidth) {
         int padding = getTextboxPadding();
         int popupOpenerWidth = Util.getRequiredWidth(popupOpener);
         int iconWidth = selectedItemIcon.isAttached() ? Util.getRequiredWidth(selectedItemIcon) : 0;
         int textboxWidth = componentWidth - padding - popupOpenerWidth - iconWidth;
         if (textboxWidth < 0) {
             textboxWidth = 0;
         }
         tb.setWidth(textboxWidth + "px");
     }
 
     /**
      * Gets the horizontal padding of the text box in pixels. The measurement
      * includes the border width.
      *
      * @return The padding in pixels
      */
     private int getTextboxPadding() {
         if (textboxPadding < 0) {
             textboxPadding = Util.measureHorizontalPaddingAndBorder(tb.getElement(), 4);
         }
         return textboxPadding;
     }
 
     /**
      * Gets the horizontal padding of the select. The measurement includes the
      * border width.
      *
      * @return The padding in pixels
      */
     private int getComponentPadding() {
         if (componentPadding < 0) {
             componentPadding = Util.measureHorizontalPaddingAndBorder(getElement(), 3);
         }
         return componentPadding;
     }
 
     /**
      * Handles special behavior of the mouse down event
      *
      * @param event
      */
     private void handleMouseDownEvent(Event event) {
         /*
          * Prevent the keyboard focus from leaving the textfield by preventing
          * the default behaviour of the browser. Fixes #4285.
          */
         if (event.getTypeInt() == Event.ONMOUSEDOWN) {
             event.preventDefault();
             event.stopPropagation();
 
             /*
              * In IE the above wont work, the blur event will still trigger. So,
              * we set a flag here to prevent the next blur event from happening.
              * This is not needed if do not already have focus, in that case
              * there will not be any blur event and we should not cancel the
              * next blur.
              */
             if (BrowserInfo.get().isIE() && focused) {
                 preventNextBlurEventInIE = true;
             }
         }
     }
 }
