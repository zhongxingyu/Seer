 package com.erman.football.client.gui;
 
 import java.util.Date;
 import java.util.TreeMap;
 
 import com.erman.football.client.cache.Cache;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class FilterPanel extends HorizontalPanel{
 	static final int PAGINATION_NUM = 6;
 	static final TreeMap<String,String> months = new TreeMap<String,String>();
 	static final TreeMap<String,String> years = new TreeMap<String,String>();
 	
 	private Cache cache;
 	final DialogBox monthDialog = new DialogBox();
 	final DialogBox yearDialog = new DialogBox();
 	private Date startDate;
 	private String month;
 	private String year;
 	private boolean attend = false;
	private int startIndex =PAGINATION_NUM-1;
 	private FilterHandler handler;
 	final private DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd MM yy");
 	final private Label monthButton;
 	final private Label yearButton;
 	final private Label attendButton;
 	
 	public FilterPanel(Cache cache, FilterHandler handler){
 		this.handler = handler;
 		this.cache = cache;
 		months.put("01", "Ocak");
 		months.put("02", "Subat");
 		months.put("03", "Mart");
 		months.put("04", "Nisan");
 		months.put("05", "Mayis");
 		months.put("06", "Haziran");
 		months.put("07", "Temmuz");
 		months.put("08", "Agustos");
 		months.put("09", "Eylul");
 		months.put("10", "Ekim");
 		months.put("11", "Kasim");
 		months.put("12", "Aralik");
 		years.put("10","2010");
 		years.put("11","2011");
 		years.put("12","2012");
 		years.put("13","2013");
 		startDate = new Date();
 		String monthYear[] = dateFormat.format(startDate).split("\\s+");
 		month = monthYear[1];
 		year = monthYear[2];
 		
 		attendButton = new Label("tum");
 		attendButton.setStyleName("filterButton");
 		attendButton.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent event) {
 				if(attend){
 					attend = false;
 					attendButton.setText("tum");
 				}else{
 					attend = true;
 					attendButton.setText("katildigim");
 				}
 				applyFilter(false);
 			}	
 		});
 		
 		
 		VerticalPanel monthPicker = new VerticalPanel();
 		for(String key:months.keySet()){
 			monthPicker.add(new DateCell(months.get(key),key,true));	
 		}
 		monthDialog.add(monthPicker);
 		monthDialog.setAutoHideEnabled(true);
 		monthDialog.setText("Ay");
 		
 		monthButton = new Label(months.get(monthYear[1]));
 		monthButton.setStyleName("filterButton");
 		monthButton.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent event) {
 				monthDialog.setPopupPosition(monthButton.getAbsoluteLeft()+3, monthButton.getAbsoluteTop()-35);
 				monthDialog.show();
 			}	
 		});
 		
 		VerticalPanel yearPicker = new VerticalPanel();
 		for(String key:years.keySet()){
 			yearPicker.add(new DateCell(years.get(key),key,false));	
 		}
 
 		yearDialog.add(yearPicker);
 		yearDialog.setAutoHideEnabled(true);
 		yearDialog.setText("Yil");
 		
 		yearButton = new Label(years.get(monthYear[2]));
 		yearButton.setStyleName("filterButton");
 		yearButton.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent event) {
 				yearDialog.setPopupPosition(yearButton.getAbsoluteLeft()+3, yearButton.getAbsoluteTop()-35);
 				yearDialog.show();
 			}	
 		});
 		
 		HorizontalPanel datePanel = new HorizontalPanel();	
 		datePanel.add(monthButton);
 		datePanel.add(yearButton);
 				
 		this.add(datePanel);
 		this.add(new Label("itibaren"));
 		SimplePanel white = new SimplePanel();
 		white.setWidth("5px");
 		this.add(white);
 		this.add(attendButton);
 		this.add(new Label("maclar"));
 	}
 	
 	public void applyFilter(boolean pagination){
 		if(!pagination){
 			startIndex = 0;
 		}
 		handler.filterApplied(pagination);
 		startDate = dateFormat.parse("01 "+month+" "+year);
 		cache.getMatches(startDate, startIndex, startIndex+PAGINATION_NUM ,attend);
 		startIndex = startIndex+PAGINATION_NUM-1;// -1 is required for extra data to display more button
 	}
 	
 	private class DateCell extends SimplePanel{
 		
 		private final String value;
 		private final String name;
 		private final boolean isMonth;
 		
 		public DateCell(String _name,String _value,boolean _isMonth){
 			Label label = new Label(_name);
 			this.add(label);
 			this.value = _value;
 			this.name = _name;
 			isMonth = _isMonth;
 			this.setStyleName("dateCell");
 			label.addClickHandler(new ClickHandler(){
 
 				public void onClick(ClickEvent event) {
 					if(isMonth){
 						month = value;
 						monthDialog.hide();
 						monthButton.setText(name);
 					}else{
 						year = value;
 						yearDialog.hide();
 						yearButton.setText(name);
 					}
 					applyFilter(false);
 				}
 				
 			});
 		}
 	}
 }
