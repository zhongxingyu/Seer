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
 
 package org.vaadin.addons.tuningdatefield.widgetset.client;
 
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.TuningDateFieldWidget;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.CalendarClosedEvent;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.CalendarClosedHandler;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.CalendarItemClickEvent;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.CalendarItemClickHandler;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.CalendarOpenEvent;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.CalendarOpenHandler;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.DateTextChangeEvent;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.DateTextChangeHandler;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.NextControlClickEvent;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.NextControlClickHandler;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.PreviousControlClickEvent;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.PreviousControlClickHandler;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.ResolutionControlClickEvent;
 import org.vaadin.addons.tuningdatefield.widgetset.client.ui.events.ResolutionControlClickHandler;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.ui.Widget;
 import com.vaadin.client.MouseEventDetailsBuilder;
 import com.vaadin.client.communication.StateChangeEvent;
 import com.vaadin.client.ui.AbstractFieldConnector;
 import com.vaadin.shared.MouseEventDetails;
 import com.vaadin.shared.ui.Connect;
 
 @Connect(org.vaadin.addons.tuningdatefield.TuningDateField.class)
 public class TuningDateFieldConnector extends AbstractFieldConnector {
 
     private static final long serialVersionUID = 5137839525617663928L;
 
     @Override
     protected void init() {
         super.init();
 
         final TuningDateFieldRpc rpc = getRpcProxy(TuningDateFieldRpc.class);
 
         getWidget().addDateTextChangedHandler(new DateTextChangeHandler() {
 
             @Override
             public void onDateTextChange(DateTextChangeEvent event) {
                 rpc.dateTextChanged(event.getDateText());
             }
         });
 
         getWidget().addCalendarOpenHandler(new CalendarOpenHandler() {
 
             @Override
             public void onCalendarOpen(CalendarOpenEvent event) {
                 rpc.onCalendarOpen();
 
             }
         });
 
         getWidget().addCalendarClosedHandler(new CalendarClosedHandler() {
 
             @Override
             public void onCalendarClosed(CalendarClosedEvent event) {
                 rpc.onCalendarClosed();
             }
         });
 
         getWidget().getCalendar().addCalendarItemClickHandler(new CalendarItemClickHandler() {
 
             @Override
             public void onCalendarItemClick(CalendarItemClickEvent event) {
                 MouseEventDetails mouseDetails = MouseEventDetailsBuilder.buildMouseEventDetails(event.getClickEvent()
                         .getNativeEvent(), getWidget().getElement());
                 rpc.calendarItemClicked(event.getItemIndex(), event.getRelativeDateIndex(), mouseDetails);
             }
         });
 
         getWidget().getCalendar().addPreviousControlClickHandler(new PreviousControlClickHandler() {
 
             @Override
             public void onPreviousControlClick(PreviousControlClickEvent event) {
                 rpc.previousControlClicked();
             }
         });
 
         getWidget().getCalendar().addNextControlClickHandler(new NextControlClickHandler() {
 
             @Override
             public void onNextControlClick(NextControlClickEvent event) {
                 rpc.nextControlClicked();
             }
         });
 
         getWidget().getCalendar().addResolutionControlClickHandler(new ResolutionControlClickHandler() {
 
             @Override
             public void onResolutionControlClick(ResolutionControlClickEvent event) {
                 rpc.resolutionControlClicked();
             }
         });
 
     }
 
     @Override
     public void onStateChanged(StateChangeEvent stateChangeEvent) {
 
         getWidget().getDateTextBox().setEnabled(getState().enabled);
         getWidget().getCalendarToggle().setEnabled(getState().enabled);
        getWidget().setDateTextReadOnly(getState().isDateTextReadOnly());
        getWidget().getDateTextBox().setReadOnly(getState().readOnly);
         getWidget().getCalendarToggle().setVisible(!getState().readOnly);
 
         getWidget().setDisplayedDateText(getState().getDisplayedDateText());
         getWidget().setCalendarOpen(getState().isCalendarOpen());
         if (getState().isCalendarOpen()) {
             getWidget().getCalendar().setCalendarResolutionText(getState().getCalendarResolutionText());
             getWidget().getCalendar().setWeekHeaderNames(getState().getWeekHeaderNames());
 
             getWidget().getCalendar().setControlsEnabled(getState().isControlsEnabled());
 
             getWidget().getCalendar().setCalendarResolution(getState().getCalendarResolution());
 
             getWidget().getCalendar().setCalendarItems(getState().getCalendarItems());
 
             getWidget().redrawCalendar();
         }
 
         super.onStateChanged(stateChangeEvent);
     }
 
     @Override
     protected Widget createWidget() {
         return GWT.create(TuningDateFieldWidget.class);
     }
 
     @Override
     public TuningDateFieldState getState() {
         return (TuningDateFieldState) super.getState();
     }
 
     @Override
     public TuningDateFieldWidget getWidget() {
         return (TuningDateFieldWidget) super.getWidget();
     }
 
 }
