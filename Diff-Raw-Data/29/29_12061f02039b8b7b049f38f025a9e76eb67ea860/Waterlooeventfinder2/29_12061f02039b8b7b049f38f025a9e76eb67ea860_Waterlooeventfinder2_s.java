 package com.waterlooeventfinder2.client;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import com.google.gwt.cell.client.DateCell;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.user.client.ui.RootPanel;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.view.client.AsyncDataProvider;
 import com.google.gwt.view.client.HasData;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.waterlooeventfinder2.shared.Event;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 
 public class Waterlooeventfinder2 extends Composite implements EntryPoint {
 
 	
 	ButtonPressed infoButtonPressed = new ButtonPressed();
 	SimplePager pager = new SimplePager();
 
 	private EventRetrievalServiceAsync retrievalService = GWT
 			.create(EventRetrievalService.class);
 
 	AsyncDataProvider<Event> provider = new AsyncDataProvider<Event>() {
 		@Override
 		protected void onRangeChanged(final HasData<Event> display) {
 
 			if (retrievalService == null) {
 				retrievalService = GWT.create(EventRetrievalService.class);
 			}
 
 			final int start = display.getVisibleRange().getStart();
 			final int end = start + display.getVisibleRange().getLength();
 
 			// Set up the callback object.
 			AsyncCallback<ArrayList<Event>> callback = new AsyncCallback<ArrayList<Event>>() {
 
 				public void onFailure(Throwable caught) {
 					Window.alert(caught.getMessage());
 					// TODO: Do something with errors.
 				}
 
 				public void onSuccess(ArrayList<Event> result) {
 					// FOR MARTIN: result object for list of events
 					//result.add(new Event());
 					// String lol = Integer.toString(start);
 					updateRowData(start, result);
 					updateRowCount(result.size(), true);
 
 				}
 			};
 			
 			retrievalService.GetEventsByFilter(infoButtonPressed.getCategory(), infoButtonPressed.getTime(), start, end, callback);
 		}
 
 	};
 
 	// Associate an async data provider to the table
 	// XXX: Use AsyncCallback in the method onRangeChanged
 	// to actaully get the data from the server side
 
 	protected void selectEvents(final String category, final String time) {
 
 		if (retrievalService == null) {
 			retrievalService = GWT.create(EventRetrievalService.class);
 		}
 
 		// Set up the callback object.
 		AsyncCallback<ArrayList<Event>> callback = new AsyncCallback<ArrayList<Event>>() {
 
 			public void onFailure(Throwable caught) {
 				Window.alert(caught.getMessage());
 				// TODO: Do something with errors.
 			}
 
 			public void onSuccess(ArrayList<Event> result) {
 				// Inform the displays of the new data.
 				provider.updateRowData(0, result);
 				// Inform the displays of the total number of items that are
 				// available.
 				provider.updateRowCount(result.size(), true);
 				pager.firstPage();
 			}
 		};
 
 		retrievalService.GetEventsByFilter(infoButtonPressed.getCategory(), infoButtonPressed.getTime(), 0, 0, callback);
 	}
 
 	// Call this function to load description page
 	public void viewEvent(int ID) {
		loadDetailsPage(1);
 		//Window.Location.assign(GWT.getHostPageBaseURL()
 		//		+ "EventDescription2.html?" + ID);
 	}
 	
 	public void loadMainPage() {
 		final Button CategoryAll = new Button("All");
 		final Button CategorySport = new Button("Sport");
 		final Button CategoryDance = new Button("Dance");
 		final Button CategoryConcert = new Button("Concert");
 		final Button CategoryBars = new Button("Bars");
 		final Button TimeUpcoming = new Button("Today");
 		final Button TimeOneDay = new Button("1 day");
 		final Button TimeTwoDays = new Button("2 days");
 		final Button TimeThreeDays = new Button("3 days");
 		final Button TimeOneWeek = new Button("1 week");
 		
 		// We put in Green All + Upcoming
 		DOM.setElementAttribute(CategoryAll.getElement(), "id", "pressed-button");
 		DOM.setElementAttribute(TimeUpcoming.getElement(), "id", "pressed-button");
 		CategoryAll.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				infoButtonPressed.setCategory("All");
 				selectEvents(infoButtonPressed.getCategory(),
 						infoButtonPressed.getTime());	
 					DOM.setElementAttribute(CategoryAll.getElement(), "id", "pressed-button");
 					DOM.setElementAttribute(CategorySport.getElement(), "id", "non-pressed-button");
 					DOM.setElementAttribute(CategoryDance.getElement(), "id", "non-pressed-button");
 					DOM.setElementAttribute(CategoryConcert.getElement(), "id", "non-pressed-button");
 					DOM.setElementAttribute(CategoryBars.getElement(), "id", "non-pressed-button");
 			}
 		});
 
 		CategorySport.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				infoButtonPressed.setCategory("Sport");
 				selectEvents(infoButtonPressed.getCategory(),
 						infoButtonPressed.getTime());
 				DOM.setElementAttribute(CategoryAll.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(CategorySport.getElement(), "id", "pressed-button");
 				DOM.setElementAttribute(CategoryDance.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(CategoryConcert.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(CategoryBars.getElement(), "id", "non-pressed-button");
 			}
 		});
 
 		CategoryDance.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				infoButtonPressed.setCategory("Dance");
 				selectEvents(infoButtonPressed.getCategory(),
 						infoButtonPressed.getTime());
 				DOM.setElementAttribute(CategoryAll.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(CategorySport.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(CategoryDance.getElement(), "id", "pressed-button");
 				DOM.setElementAttribute(CategoryConcert.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(CategoryBars.getElement(), "id", "non-pressed-button");
 			}
 		});
 
 		CategoryConcert.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				infoButtonPressed.setCategory("Concert");
 				selectEvents(infoButtonPressed.getCategory(),
 						infoButtonPressed.getTime());
 				DOM.setElementAttribute(CategoryAll.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(CategorySport.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(CategoryDance.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(CategoryConcert.getElement(), "id", "pressed-button");
 				DOM.setElementAttribute(CategoryBars.getElement(), "id", "non-pressed-button");
 			}
 		});
 
 		CategoryBars.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				infoButtonPressed.setCategory("Bars");
 				selectEvents(infoButtonPressed.getCategory(),
 						infoButtonPressed.getTime());
 				DOM.setElementAttribute(CategoryAll.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(CategorySport.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(CategoryDance.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(CategoryConcert.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(CategoryBars.getElement(), "id", "pressed-button");
 			}
 		});
 
 		TimeUpcoming.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				infoButtonPressed.setTime("Upcoming");
 				selectEvents(infoButtonPressed.getCategory(),
 						infoButtonPressed.getTime());
 				DOM.setElementAttribute(TimeUpcoming.getElement(), "id", "pressed-button");
 				DOM.setElementAttribute(TimeOneDay.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeTwoDays.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeThreeDays.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeOneWeek.getElement(), "id", "non-pressed-button");
 			}
 		});
 
 		TimeOneDay.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				infoButtonPressed.setTime("One day");
 				selectEvents(infoButtonPressed.getCategory(),
 						infoButtonPressed.getTime());
 				DOM.setElementAttribute(TimeUpcoming.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeOneDay.getElement(), "id", "pressed-button");
 				DOM.setElementAttribute(TimeTwoDays.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeThreeDays.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeOneWeek.getElement(), "id", "non-pressed-button");
 			}
 		});
 		
 		TimeTwoDays.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				infoButtonPressed.setTime("Two days");
 				selectEvents(infoButtonPressed.getCategory(),
 						infoButtonPressed.getTime());
 				DOM.setElementAttribute(TimeUpcoming.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeOneDay.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeTwoDays.getElement(), "id", "pressed-button");
 				DOM.setElementAttribute(TimeThreeDays.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeOneWeek.getElement(), "id", "non-pressed-button");
 			}
 		});		
 		
 		TimeThreeDays.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				infoButtonPressed.setTime("Three days");
 				selectEvents(infoButtonPressed.getCategory(),
 						infoButtonPressed.getTime());
 				DOM.setElementAttribute(TimeUpcoming.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeOneDay.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeTwoDays.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeThreeDays.getElement(), "id", "pressed-button");
 				DOM.setElementAttribute(TimeOneWeek.getElement(), "id", "non-pressed-button");
 			}
 		});
 
 		TimeOneWeek.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				infoButtonPressed.setTime("One week");
 				selectEvents(infoButtonPressed.getCategory(),
 						infoButtonPressed.getTime());
 				DOM.setElementAttribute(TimeUpcoming.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeOneDay.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeTwoDays.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeThreeDays.getElement(), "id", "non-pressed-button");
 				DOM.setElementAttribute(TimeOneWeek.getElement(), "id", "pressed-button");
 			}
 		});
 
 		
 		// We can add style names to widgets
 
 		CategoryAll.addStyleName("categoryButton");
 		CategorySport.addStyleName("categoryButton");
 		CategoryDance.addStyleName("categoryButton");
 		CategoryConcert.addStyleName("categoryButton");
 		CategoryBars.addStyleName("categoryButton");
 
 		TimeUpcoming.addStyleName("timeButton");
 		TimeOneDay.addStyleName("timeButton");
 		TimeTwoDays.addStyleName("timeButton");
 		TimeThreeDays.addStyleName("timeButton");
 		TimeOneWeek.addStyleName("timeButton");
 		
 		
 		// Focus the cursor on the name field when the app loads
 		/*
 		CategoryAll.setEnabled(true);
 		CategoryAll.setFocus(true);
 		CategoryFootball.setEnabled(true);
 		CategoryDance.setEnabled(true);
 		CategoryConcert.setEnabled(true);
 		 */
 		
 		// List of Events
 		// Create a CellTable.
 		final CellTable<Event> table = new CellTable<Event>();
 		// Display 2 rows in one page
 		table.setPageSize(3);
 
 		// Add a text column to show the name.
 		TextColumn<Event> nameColumn = new TextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				return object.Name();
 			}
 		};
 		
 		
 		// Add a column to show the Start time
 		TextColumn<Event> startColumn = new TextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				String startDateTime = null;  
 				if (infoButtonPressed.getTime().equals("Upcoming")) {
 					startDateTime = DateTimeFormat.getFormat(
 							PredefinedFormat.TIME_SHORT).format(object.getStarHour());
 				} else {
 					startDateTime = DateTimeFormat.getFormat(
 							PredefinedFormat.DATE_TIME_SHORT).format(object.getStarHour());
 				}
 				return startDateTime;
 			}
 		};
 	
 		// Add a column to show end time
 		TextColumn<Event> endColumn = new TextColumn<Event>() {
 			@Override
 			public String getValue(Event object) {
 				String endDateTime = null;  
 				if (infoButtonPressed.getTime().equals("Upcoming")) {
 					endDateTime = DateTimeFormat.getFormat(
 							PredefinedFormat.TIME_SHORT).format(object.getEndHour());
 				} else {
 					endDateTime = DateTimeFormat.getFormat(
 							PredefinedFormat.DATE_TIME_SHORT).format(object.getEndHour());
 				}
 				return endDateTime;
 			}
 		};
 
 
 		  
 		// Add columns to the table
 		table.addColumn(nameColumn, "Name");
 		table.addColumn(startColumn, "Start");
 		table.addColumn(endColumn, "End");
 		
 		table.addColumnStyleName(0, "nameColumStyle");
 		table.addColumnStyleName(1, "StartColumStyle");
 		table.addColumnStyleName(2, "EndColumnStyle");
 
 		table.setWidth("100%", true);
 		table.setColumnWidth(nameColumn, 50.0, Unit.PCT);
 		table.setColumnWidth(startColumn, 15.0, Unit.PCT);
 		table.setColumnWidth(endColumn, 15.0, Unit.PCT);
 
 		final SingleSelectionModel<Event> selectionModel = new SingleSelectionModel<Event>();
 		table.setSelectionModel(selectionModel);
 		selectionModel
 				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 					public void onSelectionChange(SelectionChangeEvent event) {
 						Event selected = selectionModel.getSelectedObject();
 						if (selected != null) {
							viewEvent(1);
 							String search = "1";
 							String result = "";
 							int i;
 							i = selected.Name().indexOf(search);
 							
 							//result = selected.Name().substring(i);
 							//if(result != null){
 							//	viewEvent(1);
 							//} else {
 							//	viewEvent(2);
							//}

 						}
 					}
 				});
 
 		provider.addDataDisplay(table);
 
 		pager.setDisplay(table);
 
 		// VerticalPanel vp = new VerticalPanel();
 		// vp.add(table);
 
 		// Add the nameField and sendButton to the RootPanel
 		// Use RootPanel.get() to get the entire body element
 
 //		RootPanel.get("menuBar").clear();
 //		RootPanel.get("label").clear();
 //		RootPanel.get("frames").clear();
 //		RootPanel.get("divCategoryButton").add(CategoryAll);
 //		RootPanel.get("divCategoryButton").add(CategorySport);
 //		RootPanel.get("divCategoryButton").add(CategoryDance);
 //		RootPanel.get("divCategoryButton").add(CategoryConcert);
 //		RootPanel.get("divCategoryButton").add(CategoryBars);
 //
 //		RootPanel.get("divTimeButton").add(TimeUpcoming);
 //		RootPanel.get("divTimeButton").add(TimeOneDay);
 //		RootPanel.get("divTimeButton").add(TimeTwoDays);
 //		RootPanel.get("divTimeButton").add(TimeThreeDays);
 //		RootPanel.get("divTimeButton").add(TimeOneWeek);
 //		
 //		RootPanel.get("listOfEvents").add(table);
 //		RootPanel.get("elementPager").add(pager);
 
 		clearTable();
 		RootPanel.get("row1").add(CategoryAll);
 		RootPanel.get("row1").add(CategorySport);
 		RootPanel.get("row1").add(CategoryDance);
 		RootPanel.get("row1").add(CategoryConcert);
 		RootPanel.get("row1").add(CategoryBars);
 
 		RootPanel.get("row2").add(TimeUpcoming);
 		RootPanel.get("row2").add(TimeOneDay);
 		RootPanel.get("row2").add(TimeTwoDays);
 		RootPanel.get("row2").add(TimeThreeDays);
 		RootPanel.get("row2").add(TimeOneWeek);
 		
 		RootPanel.get("row3").add(table);
 		pager.addStyleName("elementPager");
 		RootPanel.get("row4").add(pager);
 
 		
 		
 	}
 
 	public void onModuleLoad() {
 
 		loadMainPage();
 	}
 
 	public void loadDetailsPage(int eventID) {
 		
 		final Button ChooseMain = new Button("Main");
 		final String label1 = "Football season finale!!! <br><br>" +
 				"Location: Warrior Field, University of Waterloo, Waterloo, ON, N2L3G1 <br><br>" +
 				"LIGHTS, CAMERA, ACTION: The Warrior field will this Saturday be the host to the ultimate showdown between the Wilfrid Laurier Hawks and your very own Waterloo Warriors. Come on out this weekend whether it be to have fun with your friends or to enjoy a nice game of football. After the crushing loss two weeks ago at the hands of Queen's, the Warriors need your support." +
 				"So, bring out your Warrior spirit and help Warriors win the final game of the season. Tickets are only $10 and go on sale 26th October, 2011. Pick up your ticket soon from the athletics office or the sales booth in Student Life Centre. <br><br>" +
				"Video: http://www.youtube.com/watch?v=zkbXTQ95kLc <br>" +
				"Event Website: http://www.varsity.uwaterloo.ca/schedule.aspx?path=football&schedule=38& <br><br>" +
 				"Contact: <br>" +
 				"Phone: 5198884567 <br>" +
 				"E-mail: campusrec@uwaterloo.ca";
 		
 		final String label2 = "Brilliance in Music!!! <br><br>" +
 				"Location: Waterloo Public Square, Waterloo, ON, N2J 1P2 <br><br>" +
 				"What could be better than spending a relaxing evening listening to the music of some local musicians? This is an evening to enjoy some reggae, rock, pop and country. The event will be a great deal of fun for everyone involved, youll be able to spend some time with your family and friends while assisting with raising funds for the Brain Injury Association of Waterloo-Wellington. On top of an evening filled with melodious soothing music, the night will also consist of 50/50 draws, buffet, dancing, fun activities and of course music.  The cost is only $25 and tickets currently on sale on our website. <br><br>"+
				"Video: http://www.youtube.com/watch?v=U-iorEGq8OI <br>" +
				"Event Website: http://www.kitchener-waterloo.worldweb.com/Events/ConcertsPerformances/ <br><br>" +
 				"Contact: <br>" +
 				"Phone: 5195795300 <br>" +
 				"E-mail: brillianceinmusic@kwcweb.com";
 		final String flink1 = "http://www.youtube.com/embed/zkbXTQ95kLc";
 		final String flink2 = "http://www.youtube.com/embed/U-iorEGq8OI";
 		
		eventID = 1; //getval();
 		//final Label desclabel1 = new Label();
 		//desclabel1.setText(label1);
 		//final Label desclabel2 = new Label();
 		//desclabel2.setText(label2);
 		HTML desclabel1 = new HTML(label1);
 		HTML desclabel2 = new HTML(label2);
 		final HTML ftitle = new HTML("Check out the Video here:<br><br>");
 		final Frame frame1 = new Frame(flink1);
 		final Frame frame2 = new Frame(flink2);
 //		frame1.setWidth("100%");
 //		frame1.setHeight("100%");
 //		frame2.setWidth("100%");
 //		frame2.setHeight("100%");
 		
 		final VerticalPanel vp1 = new VerticalPanel();
 		final VerticalPanel vp2 = new VerticalPanel();
 		final VerticalPanel vp3 = new VerticalPanel();
 		final ScrollPanel sp = new ScrollPanel();
 		
 		ChooseMain.addStyleName("menuBar");
 		vp1.add(ChooseMain); 
 		//vp1.setWidth("100%");
 		vp2.add(sp); 
 		//vp2.setWidth("100%");
 		vp3.add(ftitle);
		if (eventID == 1){
 			sp.add(desclabel1);
 			vp3.add(frame1);
		} else if (eventID == 2){
 			sp.add(desclabel2);
 			vp3.add(frame2);
 		}
 		//vp3.setWidth("100%");
 		vp3.setStyleName("frames");
 		
 		
 		// Associate Main Panel with host page.
 	
 		clearTable();
 		RootPanel.get("row1").add(vp1);
 		RootPanel.get("row2").add(vp2);
 		RootPanel.get("row3").add(vp3);
 		
 		ChooseMain.setEnabled(true);
 		ChooseMain.setFocus(true);
 		
 		
 		ChooseMain.addClickHandler(new ClickHandler() {
 		      public void onClick(ClickEvent event) {
 		    	  loadMainPage();
 		    	  //String baseurl = GWT.getHostPageBaseURL();
 		  		  //Window.Location.assign(baseurl+"Waterlooeventfinder2.html");
 		      }
 		});	
 	}
 	
 	private void clearTable() {
 		RootPanel.get("row1").clear();
 		RootPanel.get("row2").clear();
 		RootPanel.get("row3").clear();
 		RootPanel.get("row4").clear();
 	}
 	
 }
