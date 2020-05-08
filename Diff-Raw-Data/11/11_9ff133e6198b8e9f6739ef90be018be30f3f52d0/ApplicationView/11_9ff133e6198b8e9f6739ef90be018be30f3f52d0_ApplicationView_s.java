 /*
  *  Copyright 2008 University of Prince Edward Island
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package ca.upei.ic.timetable.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * The Main View for the Application
  * 
  * This is the main view of the application. This view organizes all other
  * views of various controllers. All other views should be positioned and
  * resized by this view.
  * 
  * @author felix
  *
  */
 public class ApplicationView implements View {
 	
 	private ApplicationController app_;
 	private static String[] headerStrings = {
 		"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
 	};
 	
 	private HorizontalPanel headerPanel_;
 	
 	public ApplicationView(ApplicationController app) {
 		app_ = app;
 		
 		// hide the root panel first
 		hide();
 
 		// create the application view elements
 		// we are using a horizontal split panel to host the left side (course view)
 		// and the right side (calendar view)
 		DockPanel panel = GWT.create(DockPanel.class);
 		panel.setSize("980px", "580px");
 
 		final View findCourseView = app_.getFindCourseController().getView();
 		
 		// find course button
 		final Button findCourseButton = GWT.create(Button.class);
 		findCourseButton.setText("Find Course...");
 		findCourseButton.setPixelSize(250, 28);
 		findCourseButton.addClickListener(new ClickListener() {
 
 			public void onClick(Widget sender) {
 				findCourseView.show();
 			}
 			
 		});
 		// message view
 		final View messageView = app_.getMessageController().getView();
 		
 		// course view
 		final View courseView = app_.getCourseController().getView();
 		courseView.getWidget().setWidth("340px");
 		
 		// calendar view
 		final View calendarView = app_.getCourseCalendarController().getView();
 		
 		// top panel
 		HorizontalPanel topPanel = GWT.create(HorizontalPanel.class);
 		topPanel.add(findCourseButton);
 		headerPanel_ = GWT.create(HorizontalPanel.class);
 		SimplePanel topleft = PanelUtils.simplePanel(new HTML(""), 61, 28);
 		headerPanel_.add(topleft);
 		
 		// add headers
 		for (int i=1; i<6; i++) {
 			SimplePanel header = GWT.create(SimplePanel.class);
 			header.addStyleName("gridHeaderCell");
 			header.setPixelSize(128, 28);
 			header.add(new HTML(headerStrings[i]));
 			headerPanel_.add(header);
 		}
 		
 		topPanel.add(headerPanel_);
 		
 		// add elements to the dock panel
 		// to north (top bar)
 		panel.add(PanelUtils.horizontalPanel(findCourseButton,
 				headerPanel_
 				)
 				, DockPanel.NORTH);
 		// to west (left side bar)
 		panel.add(PanelUtils.verticalPanel(
 				PanelUtils.decoratorPanel(PanelUtils.scrollPanel(courseView.getWidget(), 240, 555)),
 				PanelUtils.horizontalPanel(
 						ButtonUtils.button("Clear", 125, 25, new ClickListener() {
 							public void onClick(Widget sender) {
 								app_.getCourseController().clear();
 							}
 						}, null),
 						ButtonUtils.button("Print...", 125, 25, new ClickListener() {
 							public void onClick(Widget sender) {
 								Element wrapper = DOM.createDiv();
 								Element header = (Element) headerPanel_.getElement().cloneNode(true);
 								wrapper.appendChild(header);
 								CalendarPanel calendar = (CalendarPanel) app_.getCourseCalendarController().getView().getWidget();
 								int height = calendar.getRealHeight();
 								Element calendarElement = (Element) calendar.getElement().cloneNode(true);
 								DOM.setStyleAttribute(calendarElement, "page-break-inside", "avoid");
 								DOM.setStyleAttribute(calendarElement, "height", (height+50)+"px");
 								wrapper.appendChild(calendarElement);
 								app_.print("Main.css", wrapper.getInnerHTML());
 							}
 						}, null)
 						)
 				),
 				DockPanel.WEST);
 		// to center (content)
 		panel.add(calendarView.getWidget(),
 				DockPanel.CENTER);
 		
 		// add the horizontal panel
 		RootPanel.get().add(panel);
 	}
 
 	public ViewController getController() {
 		return app_;
 	}
 
 	public void hide() {
 		RootPanel.get().setVisible(false);
 	}
 
 	public void show() {
 		RootPanel.get().setVisible(true);
 	}
 	
 	public void addSubView(View subView) {
 		RootPanel.get().add(subView.getWidget());
 	}
 	
 	public Widget getWidget() {
 		return RootPanel.get();
 	}
 
 }
