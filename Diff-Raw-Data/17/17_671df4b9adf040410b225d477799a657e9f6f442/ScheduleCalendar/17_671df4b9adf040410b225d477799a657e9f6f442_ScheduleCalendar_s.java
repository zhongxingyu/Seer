 package edu.nrao.dss.client;
 
 
 
 import java.util.Date;
 import java.util.HashMap;
 
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.layout.FitData;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 
 
 public class ScheduleCalendar extends ContentPanel { 
 
 	public PeriodExplorer  pe;  
 	
 	public ScheduleCalendar(Date start, int days) {
 		super();
 		// TBF: use this date range to filter the explorer
 		initLayout(start, days);
 	}	
 	
 	protected void initLayout(Date start, int days) {
 		
 		setHeading("Period Explorer");
 		setBorders(true);
 
 		// put the period explorer inside
 		FitLayout fl = new FitLayout();
 		setLayout(fl);
 	    pe = new PeriodExplorer();
 	    add(pe, new FitData(10));   	
 	}
 	
 	public void addButtonsListener(final Schedule schedule) {
 		pe.addButtonsListener(schedule);
 	}
 	
 	public void addRecord(HashMap<String, Object> fields) {
 		pe.addRecord(fields);
 	}
 	
 	public void setDefaultDate(Date date) {
 		pe.setDefaultDate(date);
 	}
 }	
 	
