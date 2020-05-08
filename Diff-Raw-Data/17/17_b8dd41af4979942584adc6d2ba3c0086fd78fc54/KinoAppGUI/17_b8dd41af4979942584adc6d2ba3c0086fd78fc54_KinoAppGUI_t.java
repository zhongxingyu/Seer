 package ws.softlabs.kino.client;
 
 import java.util.Date;
 import java.util.List;
 
 import ws.softlabs.kino.client.gui.ListDialogBox;
 import ws.softlabs.lib.kino.model.client.*;
 
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HTMLTable.Cell;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
 import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 @SuppressWarnings("unused")
 public class KinoAppGUI {
 
 	/* ==== PROTECTED FIELDS ==== */ 
     /* Data model */
     protected KinoServiceDelegate kinoService; 
     private   boolean			  processing = false;
     
     /* data holders */
     private List<Theater> 		theaters;
     private List<String>		days;
     private List<Object>		shows;
     
     public  Theater 			currentTheater;
     public  String				currentDay;
     private   Show				currentShow;
     
     /* === GUI elements === */
     /*  grids */
     protected Grid 				theatersGrid;
     protected Grid				daysGrid;    
     protected Grid				showGrid;
 
     /* panels */
     protected ScrollPanel		scrollPanel;
     protected DockLayoutPanel   containerPanel;
     protected HorizontalPanel 	headerPanel;
     protected HorizontalPanel 	statusPanel;
     protected VerticalPanel		theatersPanel;
     protected VerticalPanel   	toolsPanel;
     protected HorizontalPanel 	footerPanel;
     protected HorizontalPanel	contentsPanel;
     protected VerticalPanel		showsPanel;
     
     /* widgets */
     protected Hyperlink			loadDataButton;
     protected Label				status;    
     protected Label				title;    
     
     /* dialogs */
     protected Image				loadingImage;
     
     /* === PUBLIC METHODS === */
 	public void init() {
 	    createHeader();
 	    createStatus();
 	    createTheaters();
 	    createToolbar();
 	    createFooter();
 	    createMainContent(); 
 	    
 		placeWidgets();	  		
 	}		
 
 	/* ==== GUI EVENTS ==== */
 	public void gui_eventTheaterGridClicked(Cell cellClicked) {
 		if ( processing ) return;		
 		if ((this.theaters.get(cellClicked.getRowIndex())).equals(currentTheater))
 			return;
 		status.setText("");
 		currentTheater = this.theaters.get(cellClicked.getRowIndex());
 		title.setText(currentTheater.getName());
 		hide(scrollPanel);
 		hide(showsPanel);
 		showGrid.clear();
 		show(loadingImage);
 		show(statusPanel);
 		kinoService.listDays(currentTheater);
 		processing = true;
 	}	
 	public void gui_eventDaysGridClicked(Cell cellClicked) {
 		if ( processing ) return;		
 		status.setText("");
 		selectDay(cellClicked.getRowIndex());
 		currentDay = this.days.get(cellClicked.getRowIndex());
 		hide(showsPanel);
 		show(loadingImage);
 		kinoService.listShows(currentTheater, currentDay);
 		processing = true;
 	}
 
 	/* ==== SERVICE CALLBACKS ==== */
 	/* theater */
 	public void service_eventListTheatersFailed(Throwable caught) {
 		processing = false;
 		hide(loadingImage);
 		status.setText("cant load theaters list");
 	}
 	public void service_eventListTheatersSuccess(List<Theater> result) {
 		status.setText("");
 		processing = false;		
 		theaters = result;
 
 		if (theaters != null) {
 			int row = 0;
 			theatersGrid.resizeRows(theaters.size());
 			for(Theater t : theaters) {
 				theatersGrid.setWidget(row, 0, new Hyperlink(t.getName(), ""));
 				row++;
 			}
 			show(theatersPanel);
 		}
 		else {
 			status.setText( "no data in response to 'listTheaters'");
 		}
 		hide(loadingImage);
 	}
 	/* days */
 	public void service_eventListDaysFailed(Throwable caught) {
 		status.setText("failure in list days");
 		processing = false;		
 		hide(loadingImage);
 	}
 	public void service_eventListDaysSuccess(List<String> result) {
 		status.setText("");
 		processing = false;		
 		days = result;
 		if (days != null) {
 			int row = 0;
 			daysGrid.resizeRows(days.size());
 			for(String d : days) {
 				daysGrid.setWidget(row, 0, new Hyperlink(d, ""));
 				row++;
 			}
 			currentDay = this.days.get(0);
 			selectDay(0);
			hide(showsPanel);
			kinoService.listShows(currentTheater, currentDay);
			show(scrollPanel);
 		}
 		else {
 			status.setText( "no data in response to 'listDays'");
 			currentDay = null;
			hide(loadingImage);
			hide(scrollPanel);
 		}	
 	}
 	/* shows */
 	public void service_eventListShowsFailed(Throwable caught) {
 		processing = false;		
 		hide(loadingImage);
 		status.setText("failure loading shows for " + currentTheater.getName());
 	}
 	public void service_eventListShowsSuccess(List<Object> result) {
 		status.setText("");
 		processing = false;		
 		shows = result;
 		if(shows != null) {
 			int row = 0;
 			showGrid.resize(shows.size(), 7);
 			for(Object o : shows) {
 				if (o instanceof Hall) {
 					Label label = new Label(((Hall)o).getName());
 					label.setStylePrimaryName("shows00");
 					label.setWidth("100%");
 					label.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
 					showGrid.setWidget(row, 0, new Label(""));
 					showGrid.setWidget(row, 1, label);
 					showGrid.setWidget(row, 2, new Label(""));
 					showGrid.setWidget(row, 3, new Label(""));
 					showGrid.setWidget(row, 4, new Label(""));
 					showGrid.setWidget(row, 5, new Label(""));
 					showGrid.setWidget(row, 6, new Label(""));
 					row++;
 				} else if (o instanceof Show) {
 					Label     label = null;
 					Hyperlink link  = null;
 					Show show = (Show)o;
 					label = new Label(show.getTimeString());
 					label.setStylePrimaryName("shows01");
 					showGrid.setWidget(row, 0, label);
 					/*link = new Hyperlink(show.getMovie().getName(), show.getMovie().getUrl());
 					link.setStylePrimaryName("shows02");
 					showGrid.setWidget(row, 1, link);*/
 					label = new Label(show.getMovie().getName());
 					label.setStylePrimaryName("shows021");
 					showGrid.setWidget(row, 1, label);
 					
 					int col = 2;
 					for(Integer i : show.getPrice()) {
 						if (i != null) {
 							label = new Label((new Integer(i)).toString());
 							label.setStylePrimaryName("shows03");
 							showGrid.setWidget(row, col, label);
 						}
 						col++;
 					}							
 					row++;
 				}
  			}
 			showsPanel.setVisible(true);
 		}
		else {
 			status.setText( "no data in response to 'listShows'");
			
		}
 		hide(loadingImage);
 	}
 	/* dataLoad */
 	public void service_eventLoadDataFailed(Throwable caught) {
 		processing = false;
 		status.setText("data load failed...");
 		kinoService.listTheaters();
 	}
 	public void service_eventLoadDataSuccess() {
 		processing = false;
 		status.setText("");
 		kinoService.listTheaters();
 	}
 	/* list from db */
 	void service_eventListDBFailed(Throwable caught) {
 		status.setText("can't load data list from DB");
 	}
 	void service_eventListDBSuccess(List<String> strings) {
 		if (strings == null || strings.isEmpty()) {
 			status.setText("no data");
 		} else {
 			ListDialogBox dialog = new ListDialogBox(strings);
 			dialog.center();
 			status.setText("");
 		}
 	}
 	public void service_eventClearDBFailed(Throwable caught) {
 		status.setText("can't clear database");
 	}
 	public void service_eventClearDBSuccess() {
 		status.setText("cleared database");
 	}
 	
 	/* ==== PRIVATE METHODS ==== */
     private void placeWidgets() {
 		containerPanel = new DockLayoutPanel(Unit.PX);
 		containerPanel.addStyleName("main");
 		containerPanel.addNorth(headerPanel, 70);
 		containerPanel.addSouth(footerPanel, 30);
 		containerPanel.addEast(toolsPanel, 250);
 		containerPanel.addWest(theatersPanel, 200);
 		containerPanel.addNorth(statusPanel, 40);
 		containerPanel.add(scrollPanel);
 		show(containerPanel);
 		RootLayoutPanel.get().add(containerPanel);
 		//RootPanel.get(KINO_APP_HEADER_ROOT_PANEL).add(headerPanel);
         //RootPanel.get(KINO_APP_STATUS_ROOT_PANEL).add(statusPanel);
         //RootPanel.get(KINO_APP_THEATERS_ROOT_PANEL).add(theatersPanel);
         //RootPanel.get(KINO_APP_SHOWS_ROOT_PANEL).add(contentsPanel);
         //RootPanel.get(KINO_APP_TOOLS_ROOT_PANEL).add(toolsPanel);
         //RootPanel.get(KINO_APP_FOOTER_ROOT_PANEL).add(footerPanel);
 	}
     private void createHeader() {
 		headerPanel	= new HorizontalPanel();
 		show(headerPanel);
 		headerPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
 		
 		headerPanel.setWidth("100%");
 		headerPanel.addStyleName("header");
 		
 		loadingImage = new Image("images/rotation.gif");
 
 		headerPanel.add(loadingImage);
 		headerPanel.add(new HTML(
 				"<span class=\"font01\">KinoSite</span>" +
 				"<span class=\"font04\">" +
 				"<a href=\"http://softlabs.ws\" " +
 				"target=\"_blank\">softlabs.ws</a></span>"));
 		
 		headerPanel.setCellWidth(headerPanel.getWidget(1), "1");
 		headerPanel.setCellVerticalAlignment(headerPanel.getWidget(0), VerticalPanel.ALIGN_MIDDLE);
 		hide(loadingImage);
     }
     private void createStatus() {
 	    status		 = new Label("");
 	    status.setStyleName("status");	    
 	    status.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
 
 	    title		 = new Label("");
 	    title.setStylePrimaryName("title");	    
 	    title.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
 
 	    statusPanel	 = new HorizontalPanel();
 	    statusPanel.add(status);
 	    statusPanel.add(title);
  	    statusPanel.setWidth("100%");
  	    statusPanel.setHeight("100%");
  	    statusPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
 	    statusPanel.addStyleName("status_panel");
 	    
 	    hide(statusPanel);
     }
     private void createTheaters() {
 	    theatersPanel = new VerticalPanel();
 	    theatersGrid  = new Grid(1,1);
 
 	    theatersPanel.add(new HTML("<span class=font02>Кинотеатры<br></span>"));
 	    
 	    theatersPanel.add(theatersGrid);   
 	    
 	    theatersPanel.addStyleName("theaters");
     
 	    hide(theatersPanel);
     }
     private void createToolbar() {
 	    final String[] links = {"", "theaters", "halls" , "days", 
 	    						"movies", "shows", "", "clear all!" , ""
 	    						};
 
     	toolsPanel	   = new VerticalPanel();
 /*
 	    toolsPanel.add(new HTML("<span class=\"font02\">Инструменты</span>"));
 	    
 	    final FlexTable toolLinks = new FlexTable();
 	    toolLinks.addStyleName("toolsGrid");
 
 	    
 	    for(int i = 0; i < links.length; i++ ) {
 	    	
 	    	if ("".equalsIgnoreCase(links[i])) {
 	    		toolLinks.setWidget(i, 0, new HTML("<hr>"));
 	    		toolLinks.getFlexCellFormatter().setColSpan(i, 0, 3);
 	    	}
 	    	else {
 	    		 String s = new String(links[i]).replaceAll("!", "");
 	    		 toolLinks.setWidget(i, 0, new Hyperlink(s, ""));
 	    		 if (!links[i].contains("!")) {
 			    	 Image reload = new Image("/images/reload.png");
 			    	 reload.addStyleName("cursor");
 			    	 Image remove = new Image("/images/remove.png");
 			    	 remove.addStyleName("cursor");
 			    	 toolLinks.setWidget(i, 1, reload);
 			    	 toolLinks.setWidget(i, 2, remove);
 	    		 }
 	    	}
 	    }
 		toolLinks.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				Cell cellForEvent = toolLinks.getCellForEvent(event);
 				
 				int row = cellForEvent.getRowIndex();
 				
 				String type   = toolLinks.getText(row, 0);
 				String action = "";
 				switch (cellForEvent.getCellIndex()) {
 					case 0: action = "show";
 							break;
 					case 1: action = "reload";
 							break;
 					case 2: action = "clear";
 							break;
 				}
 				show(statusPanel);
 				
 				status.setText(type + " - " + action);
 				
 				if (!"clear all".equalsIgnoreCase(type)) {
 					if ("show".equalsIgnoreCase(action))
 						kinoService.loadDataFromDB(type);
 					else if ("reload".equalsIgnoreCase(action)) {
 						kinoService.loadFromParser(type);
 					}
 					else if ("clear".equalsIgnoreCase(action)) {
 						kinoService.clearDatabase(type);
 					}
 				}
 				else 
 					kinoService.clearDatabase();
 		}});
 			
 				
 		toolsPanel.add(toolLinks);
 
 		toolsPanel.add(new Hyperlink("run cron job", "/cron/load"));
 		/**/
 		toolsPanel.addStyleName("tools");
 	    show(toolsPanel);
     }
     private void createFooter() {
 	    footerPanel	= new HorizontalPanel();
 	    
 	    footerPanel.add(new HTML("<img align=right src='http://code.google.com/appengine/images/appengine-noborder-120x30.gif'" + 
 	    						 " alt='Google App Engine'/>"));
 	    
 	    footerPanel.addStyleName("footer");
 	    footerPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
 	    footerPanel.setWidth("100%");
 	    show(footerPanel);
     }
     private void createMainContent() {
     	contentsPanel = new HorizontalPanel();
     	showsPanel	  = new VerticalPanel();
     	daysGrid	  = new Grid(1, 1);  
     	showGrid	  = new Grid(1, 6);
     	
     	daysGrid.setStyleName("daysGrid");
     	showGrid.setStyleName("showsGrid");
     	
      	daysGrid.setWidth("250px");
     	contentsPanel.setWidth("100%");
     	showsPanel.setWidth("100%");
 
     	contentsPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
     	showsPanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
 
     	contentsPanel.add(showsPanel);
     	contentsPanel.add(daysGrid);    	
     	showsPanel.add(showGrid);
 
     	scrollPanel = new ScrollPanel(contentsPanel);
     	scrollPanel.setAlwaysShowScrollBars(false);
     	scrollPanel.addStyleName("contents");
     	
     	hide(scrollPanel);
     }    
 
     private void hide(Widget widget) {
     	widget.setVisible(false);
     }
     private void show(Widget widget) {
     	widget.setVisible(true);
     }
     private void selectDay(int idx) {
 		for(int i=0; i < days.size(); i++)
 			daysGrid.getWidget(i, 0).removeStyleName("selected");
 		daysGrid.getWidget(idx, 0).addStyleName("selected");    	
     }
     
     /* ==== PRIVATE STATIC FIELDS ==== */    
 	private static final String KINO_APP_HEADER_ROOT_PANEL   = "header";
 	private static final String KINO_APP_STATUS_ROOT_PANEL   = "status";
     private static final String KINO_APP_THEATERS_ROOT_PANEL = "theaters_list";
 	private static final String KINO_APP_SHOWS_ROOT_PANEL    = "shows_list";
     private static final String KINO_APP_TOOLS_ROOT_PANEL    = "toolbar";
 	private static final String KINO_APP_FOOTER_ROOT_PANEL   = "footer";
 	private static final String LOADING_PANEL   			 = "loading";
 }
