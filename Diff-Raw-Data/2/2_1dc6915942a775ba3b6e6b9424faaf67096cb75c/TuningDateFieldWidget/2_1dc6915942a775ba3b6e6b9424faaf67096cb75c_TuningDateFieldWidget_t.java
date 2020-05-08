 /*
  * Copyright (C) 2013 Frederic Dreyfus
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.vaadin.addons.tuningdatefield.widgetset.client.ui;
 
 import java.util.logging.Logger;
 
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.calendar.TuningDateFieldCalendarWidget;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.CalendarAttachedEvent;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.CalendarAttachedHandler;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.CalendarClosedEvent;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.CalendarClosedHandler;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.CalendarOpenEvent;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.CalendarOpenHandler;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.DateTextChangeEvent;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.DateTextChangeHandler;
 
 import com.google.gwt.aria.client.Id;
 import com.google.gwt.aria.client.Roles;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.DomEvent;
 import com.google.gwt.event.dom.client.FocusEvent;
 import com.google.gwt.event.dom.client.FocusHandler;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Focusable;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
 import com.google.gwt.user.client.ui.TextBox;
 import com.vaadin.client.ui.Field;
 import com.vaadin.client.ui.VCalendarPanel.FocusOutListener;
 import com.vaadin.client.ui.VOverlay;
 import com.vaadin.client.ui.VTextField;
 
 /**
  * A text box with a toggle button that displays a {@link TuningDateFieldCalendarWidget}.
  * 
  * @author Frederic.Dreyfus
  * 
  */
 public class TuningDateFieldWidget extends FlowPanel implements Field, CloseHandler<PopupPanel>, ClickHandler,
         ChangeHandler, Focusable {
 
     /**
      * We reuse Vaadin class so that the textfield/toogle button look the same as other Vaadin fields
      */
     public static final String CLASSNAME = "v-datefield";
 
     private final TextBox dateTextBox;
 
     private final Button calendarToggle = new Button();
 
     private TuningDateFieldCalendarWidget calendar;
 
     private final VOverlay popup;
 
     private boolean enabled = true;
     private boolean readOnly;
 
     /**
      * True when the popup calendar is open, else false
      */
     private boolean calendarOpen = false;
 
     public TuningDateFieldWidget() {
         setStyleName(CLASSNAME);
         addStyleName("tuning-datefield");
 
         dateTextBox = new TextBox();
         dateTextBox.addStyleName("v-textfield");
         dateTextBox.addStyleName(CLASSNAME + "-textfield");
         dateTextBox.addChangeHandler(this);
         dateTextBox.addFocusHandler(new FocusHandler() {
             @Override
             public void onFocus(FocusEvent event) {
                 dateTextBox.addStyleName(VTextField.CLASSNAME + "-" + VTextField.CLASSNAME_FOCUS);
                 // Show calendar
             }
         });
         dateTextBox.addBlurHandler(new BlurHandler() {
 
             @Override
             public void onBlur(BlurEvent event) {
                 dateTextBox.removeStyleName(VTextField.CLASSNAME + "-" + VTextField.CLASSNAME_FOCUS);
 
             }
         });
         add(dateTextBox);
 
         calendarToggle.addStyleName("v-datefield-button");
         calendarToggle.setText("");
         calendarToggle.addClickHandler(this);
         calendarToggle.getElement().setTabIndex(-2);
 
         Roles.getButtonRole().set(calendarToggle.getElement());
         Roles.getButtonRole().setAriaHiddenState(calendarToggle.getElement(), true);
         add(calendarToggle);
 
         calendar = GWT.create(TuningDateFieldCalendarWidget.class);
         calendar.setFocusOutListener(new FocusOutListener() {
             @Override
             public boolean onFocusOut(DomEvent<?> event) {
                 event.preventDefault();
                 closeCalendar();
                 return true;
             }
         });
 
         Roles.getTextboxRole().setAriaControlsProperty(dateTextBox.getElement(), Id.of(calendar.getElement()));
         Roles.getButtonRole().setAriaControlsProperty(calendarToggle.getElement(), Id.of(calendar.getElement()));
 
        popup = new VOverlay(true, false, true);
         popup.setOwner(this);
 
         popup.setWidget(calendar);
         // When the calendar widget is set we need to update
         // popup position
         calendar.addCalendarAttachedHandler(new CalendarAttachedHandler() {
             @Override
             public void onCalendarAttached(CalendarAttachedEvent event) {
                 updatePopupPosition();
             }
         });
 
         popup.addCloseHandler(this);
 
         sinkEvents(Event.ONKEYDOWN);
 
         updateStyleNames();
     }
 
     // When the user clicks on the calendar toggle button
     @Override
     public void onClick(ClickEvent event) {
         if (event.getSource() == calendarToggle && isEnabled()) {
             openCalendar();
         }
     }
 
     public void redrawCalendar() {
         updateStyleNames();
         calendar.redraw(calendarOpen);
         // HACK : We need to hide/show so that the popup overlay is repainted
         if (popup.isShowing()) {
             popup.hide();
             popup.show();
         }
     }
 
     public void openCalendar() {
 
         if (!calendarOpen && !readOnly) {
             fireEvent(new CalendarOpenEvent());
             calendar.redraw(calendarOpen);
             // clear previous values
             popup.setWidth("");
             popup.setHeight("");
             updatePopupPosition();
         } else {
             Logger.getLogger(TuningDateFieldWidget.class.getName()).warning("Cannot reopen popup, it is already open!");
         }
     }
 
     public void updatePopupPosition() {
 
         // This has been copied from Vaadin VPopupCalendar (shame on me...)
         popup.setPopupPositionAndShow(new PositionCallback() {
             @Override
             public void setPosition(int offsetWidth, int offsetHeight) {
                 final int w = offsetWidth;
                 final int h = offsetHeight;
                 final int browserWindowWidth = Window.getClientWidth() + Window.getScrollLeft();
                 final int browserWindowHeight = Window.getClientHeight() + Window.getScrollTop();
                 int t = calendarToggle.getAbsoluteTop();
                 int l = calendarToggle.getAbsoluteLeft();
 
                 // Add a little extra space to the right to avoid
                 // problems with IE7 scrollbars and to make it look
                 // nicer.
                 int extraSpace = 30;
 
                 boolean overflowRight = false;
                 if (l + +w + extraSpace > browserWindowWidth) {
                     overflowRight = true;
                     // Part of the popup is outside the browser window
                     // (to the right)
                     l = browserWindowWidth - w - extraSpace;
                 }
 
                 if (t + h + calendarToggle.getOffsetHeight() + 30 > browserWindowHeight) {
                     // Part of the popup is outside the browser window
                     // (below)
                     t = browserWindowHeight - h - calendarToggle.getOffsetHeight() - 30;
                     if (!overflowRight) {
                         // Show to the right of the popup button unless we
                         // are in the lower right corner of the screen
                         l += calendarToggle.getOffsetWidth();
                     }
                 }
 
                 popup.setPopupPosition(l, t + calendarToggle.getOffsetHeight() + 2);
             }
         });
     }
 
     @Override
     public void onChange(ChangeEvent event) {
         if (!dateTextBox.getText().equals("")) {
             fireEvent(new DateTextChangeEvent(dateTextBox.getText()));
         } else {
             fireEvent(new DateTextChangeEvent(null));
         }
     }
 
     /**
      * Closes the open popup panel
      */
     public void closeCalendar() {
         if (calendarOpen) {
             popup.hide(true);
         }
     }
 
     @Override
     public void onClose(CloseEvent<PopupPanel> event) {
         if (event.getSource() == popup) {
 
             if (event.isAutoClosed()) { // In this case the user clicks somewhere else than the calendar
                 // Why ??????
                 Timer t = new Timer() {
                     @Override
                     public void run() {
                         // calendarOpen = false;
                         fireEvent(new CalendarClosedEvent());
 
                     }
                 };
                 t.schedule(100);
             }
 
         }
     }
 
     public void setDisplayedDateText(String text) {
         dateTextBox.setText(text);
     }
 
     protected void updateStyleNames() {
         if (getStylePrimaryName() != null && calendarToggle != null) {
             addStyleName(getStylePrimaryName() + "-popupcalendar");
             calendarToggle.setStyleName(getStylePrimaryName() + "-button");
             popup.setStyleName("tuning-datefield-popup");
             calendar.setStyleName("tuning-datefield-calendar");
 
             // We update the popup and calendar stylenames
             String styleName = getStyleName();
             if (styleName != null) {
                 String[] styles = styleName.split(" ");
                 for (String style : styles) {
                     if (!style.startsWith("v-datefield") && !style.startsWith("v-widget")) {
                         popup.addStyleName(style + "-popup");
                         calendar.addStyleName(style + "-calendar");
                     }
                 }
             }
         }
     }
 
     public HandlerRegistration addDateTextChangedHandler(DateTextChangeHandler dateTextChangeHandler) {
         return addHandler(dateTextChangeHandler, DateTextChangeEvent.getType());
     }
 
     public HandlerRegistration addCalendarOpenHandler(CalendarOpenHandler calendarOpenHandler) {
         return addHandler(calendarOpenHandler, CalendarOpenEvent.getType());
     }
 
     public HandlerRegistration addCalendarClosedHandler(CalendarClosedHandler calendarClosedHandler) {
         return addHandler(calendarClosedHandler, CalendarClosedEvent.getType());
     }
 
     /**
      * @return the enabled
      */
     public boolean isEnabled() {
         return enabled;
     }
 
     /**
      * @param enabled
      *            the enabled to set
      */
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
 
     /**
      * @return the readOnly
      */
     public boolean isReadOnly() {
         return readOnly;
     }
 
     /**
      * @param readOnly
      *            the readOnly to set
      */
     public void setReadOnly(boolean readOnly) {
         this.readOnly = readOnly;
     }
 
     /**
      * @return the calendarOpen
      */
     public boolean isCalendarOpen() {
         return calendarOpen;
     }
 
     /**
      * @param calendarOpen
      *            the calendarOpen to set
      */
     public void setCalendarOpen(boolean calendarOpen) {
         if (this.calendarOpen == true && calendarOpen == false) {
             closeCalendar();
         }
         this.calendarOpen = calendarOpen;
 
     }
 
     /**
      * @return the calendarToggle
      */
     public Button getCalendarToggle() {
         return calendarToggle;
     }
 
     /**
      * @return the dateTextBox
      */
     public TextBox getDateTextBox() {
         return dateTextBox;
     }
 
     /**
      * @return the dateTextReadOnly
      */
     public boolean isDateTextReadOnly() {
         return dateTextBox.isReadOnly();
     }
 
     /**
      * @param dateTextReadOnly
      *            the dateTextReadOnly to set
      */
     public void setDateTextReadOnly(boolean dateTextReadOnly) {
         dateTextBox.setReadOnly(dateTextReadOnly);
     }
 
     /**
      * @return the calendar
      */
     public TuningDateFieldCalendarWidget getCalendar() {
         return calendar;
     }
 
     @Override
     public int getTabIndex() {
         return dateTextBox.getTabIndex();
     }
 
     @Override
     public void setAccessKey(char key) {
         dateTextBox.setAccessKey(key);
     }
 
     @Override
     public void setFocus(boolean focused) {
         dateTextBox.setFocus(focused);
     }
 
     @Override
     public void setTabIndex(int index) {
         dateTextBox.setTabIndex(index);
     }
 
 }
