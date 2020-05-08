 package edu.nrao.dss.client;
 
 import java.util.Date;
 import java.util.HashMap;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.DateField;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.form.Validator;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 
 import edu.nrao.dss.client.data.Window;
 import edu.nrao.dss.client.data.WindowCalendarData;
 import edu.nrao.dss.client.util.JSONCallbackAdapter;
 import edu.nrao.dss.client.util.JSONRequest;
 import edu.nrao.dss.client.widget.form.IntegerValidator;
 
 public class WindowCalendar extends ContentPanel {
 	
 	private WindowCalTable calendar = new WindowCalTable();
     private DateField start = new DateField();
     private SimpleComboBox<String> numDays = new SimpleComboBox<String>();
     private Button update = new Button();
     
     
 	public WindowCalendar() {
 		initLayout();
 		initListeners();
 	}
 	
 	private void initLayout() {	
 	
 		setLayout(new RowLayout(Orientation.VERTICAL));
 	
 		setScrollMode(Scroll.AUTO);
 		setBorders(false);
 		setHeaderVisible(true);
 		setHeading("Window Calendar");
 		
 		FormPanel fp = new FormPanel();
 		fp.setHeaderVisible(false);
 
 		// when to start the schedule
 		start.setFieldLabel("Start Date");
 		fp.add(start);
 		
 		// for how long?
 		int maxDays = 30;
 		numDays.setFieldLabel("# Days");
 		numDays.setTriggerAction(TriggerAction.ALL);
 		for (int i = 2; i < maxDays; i++) {
 			numDays.add(Integer.toString(i));
 		}
 		numDays.setValidator(new IntegerValidator());
 		fp.add(numDays);
 		
 		update.setText("Update");
 		fp.add(update);
 		
 		add(fp);
 		
 	    add(calendar);
     
 	}
 	
 	private void initListeners() {
 		
 	    update.addListener(Events.OnClick, new Listener<BaseEvent>() {
 	    	public void handleEvent(BaseEvent be) {
 	    		// update the calendar using the controls
 	    		getWindows();
 	    		
 	    	}
 	    });
 	   
 	}	
 	
 	public void setupCalendar() {
 		
 		// set default values for controls
 		start.setValue(new Date()); // today
 		numDays.setSimpleValue("1");
 		
 		// update the calendar
 		getWindows();
 	}
 	
    // update the calendar using the controls;
 	public void getWindows() {
		// first, validate the input
		String startStr;
         // WTF: why doesn't this work?		
 		//if (!numDays.validate()) {
 		try {
 			int days = Integer.parseInt(numDays.getSimpleValue());
 		} catch (Exception e) {
 			MessageBox.alert("Error", "Invalid number of Days.", null);
 			return;
 		}
		try {
		    startStr = DateTimeFormat.getFormat("yyyy-MM-dd").format(start.getValue()); //+ " 00:00:00";
		} catch (Exception e) {
			MessageBox.alert("Error", "Invalid Start Date.", null);
			return;
		}
 		// /scheduler/windows?filterStartDate=2010-02-01&filterDuration=30&sortField=null&sortDir=NONE&offset=0&limit=50
 		HashMap<String, Object> keys = new HashMap<String, Object>();
 		//keys.put("startdate", DATE_FORMAT.format(day.getValue()));
 		keys.put("filterStartDate", startStr);
 		keys.put("filterDuration", numDays.getSimpleValue());
 		JSONRequest.get("/scheduler/windows", keys  
 			      , new JSONCallbackAdapter() {
 			public void onSuccess(JSONObject json) {
 				GWT.log(json.toString(), null);
 				jsonToCalendar(json, start.getValue(), Integer.parseInt(numDays.getSimpleValue()));
 				
 			}
 		});		
 	}
 	
 	private void jsonToCalendar(JSONObject json, Date start, int numDays) {
 		WindowCalendarData[] data = parseWindowsJSON(json, start, numDays);
 		renderCalendar(data, start, numDays);
 	}
 	
 	private WindowCalendarData[] parseWindowsJSON(JSONObject json, Date dt, int days) {
 	    JSONArray windowsJson = json.get("windows").isArray();
 	    int numWins = windowsJson.size();
 	    //int days = Integer.parseInt(numDays.getSimpleValue());
 	    WindowCalendarData[] data = new WindowCalendarData[numWins];
 	    for (int i=0; i<numWins; i++) {
 	    	data[i] = new WindowCalendarData(dt, days, windowsJson.get(i).isObject());
 	    }
 	    return data;
 	}
 	
 	private void renderCalendar(WindowCalendarData[] data, Date dt, int numDays) {
 		this.calendar.renderCalendar(data, dt, numDays);
 	}
 }
