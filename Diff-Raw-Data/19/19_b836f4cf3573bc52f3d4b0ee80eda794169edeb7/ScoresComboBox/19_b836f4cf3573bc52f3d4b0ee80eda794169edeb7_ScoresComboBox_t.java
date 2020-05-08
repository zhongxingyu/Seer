 package edu.nrao.dss.client;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Set;
 
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.FormEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.event.WidgetListener;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.gargoylesoftware.htmlunit.javascript.host.Event;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 
 import edu.nrao.dss.client.util.JSONCallbackAdapter;
 import edu.nrao.dss.client.util.JSONRequest;
 
 public class ScoresComboBox extends SimpleComboBox implements ScoresControl {
 	
 	private ScoresDisplay display;
 	private ScoresAccess access;
     private Schedule schedulePanel;
     private CheckBox notcomplete;
     private CheckBox enabled;
 	private final HashMap<String, Integer> sessionsMap = new HashMap<String, Integer>();
  
 	public ScoresComboBox(Schedule schedulePanel, CheckBox notcomplete, CheckBox enabled) {
 		super();
 		this.schedulePanel = schedulePanel;
 		this.notcomplete   = notcomplete;
 		this.enabled       = enabled;
 		initLayout();
 		initListeners();
 		setTriggerAction(TriggerAction.ALL);
 	}
 	
 	private void initListeners() {
 		
 		notcomplete.addListener(Events.Change, new Listener<BaseEvent>(){
 
 			@Override
 			public void handleEvent(BaseEvent be) {
 				getOptions();
 			}
 
 		});
 		enabled.addListener(Events.Change, new Listener<BaseEvent>(){
 
 			@Override
 			public void handleEvent(BaseEvent be) {
 				getOptions();
 			}
 
 		});
 		
 	}
 	
 	private void initLayout() {
 		
 	    // get the options
 		setForceSelection(true);
 		getOptions();
 	}
 	
 	private void getOptions() {
 		this.removeAll();
 		JSONRequest.get("/scheduler/sessions/options"
 			      , new HashMap<String, Object>() {{
 			    	  put("mode", "session_handles");
 			    	  put("notcomplete", notcomplete.getValue().toString());
 			    	  put("enabled",   enabled.getValue().toString());
 			        }}
 			      , new JSONCallbackAdapter() {
 			@SuppressWarnings("unchecked")
 			public void onSuccess(JSONObject json) {
 				JSONArray results = json.get("session handles").isArray();
 				JSONArray ids = json.get("ids").isArray();
 				for (int i = 0; i< ids.size(); i += 1) {
 					String key = results.get(i).toString().replace('"', ' ').trim();
 					sessionsMap.put(key, (int)(ids.get(i).isNumber().doubleValue()));
 					add(key);
 				}
 			}
   	});		
 		
 	}
 
 	public void getSessionScores(String session) {
 	    int id = sessionsMap.get(session);
 	    getScores(id, session);
 	}
 	
 	private void getScores(int id, String name) {
 		Date start = schedulePanel.getStartCalendarDay();
 		int numDays = schedulePanel.getNumCalendarDays();
 		String tz = schedulePanel.getTimeZone();
 		
 		// go get the scores
 	    access.request(display, id, name, start, (numDays * 24 * 60), tz);
 
 	}
 	
 	@Override
 	public void setAccess(ScoresAccess access) {
         this.access = access;		
 	}
 
 	@Override
 	public void setDisplay(ScoresDisplay display) {
         this.display = display;		
 	}
 
 }
