 package edu.nrao.dss.client;
 
 import java.util.HashMap;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.CheckBox;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.ui.HTML;
 
 import edu.nrao.dss.client.util.JSONCallbackAdapter;
 import edu.nrao.dss.client.util.JSONRequest;
 import edu.nrao.dss.client.util.JSONRequestCache;
 import edu.nrao.dss.client.widget.ElectivesInfoPanel;
 import edu.nrao.dss.client.widget.WindowsInfoPanel;
 
 public class SessionPage extends ContentPanel {
 	
     private final WindowsInfoPanel  wp = new WindowsInfoPanel("windows", "Window");
     private final ElectivesInfoPanel  ep = new ElectivesInfoPanel("electives", "Elective");
     
 	private FormPanel sessionForm = new FormPanel();
 	private SimpleComboBox<String> sessions = new SimpleComboBox<String>();
 	private TextField<String> type = new TextField<String>();
 	private CheckBox guaranteed = new CheckBox();
 	private Button save = new Button();
 	private Button reset = new Button();
     private FormData fd = new FormData(200, 25);    
 	
 	private HashMap<String, Integer> session_ids = new HashMap<String, Integer>();
 	private JSONObject sessionJson;
 	private String sessionHandle;
     
 	public SessionPage() {
 		initLayout();
 		initListeners();
 		updateSessionOptions();		
 	}
 
 	private void initLayout() {
 		setBorders(false);
 		setHeaderVisible(false);
 		setLayout(new RowLayout(Orientation.VERTICAL));
 		
 		// so we can always see everything 
 		setScrollMode(Scroll.AUTO); 
 
         sessionForm.setHeaderVisible(false);
         sessionForm.add(new HTML("<h2>Session Information</h2>"));
 		
 		sessions.setFieldLabel("Session");
 		sessionForm.add(sessions);
 		
 		type.setFieldLabel("Type");
 		type.setReadOnly(true);
 		type.setStyleAttribute("color", "grey");
 		sessionForm.add(type, fd);
 		
 		guaranteed = new CheckBox();
 		guaranteed.setFieldLabel("Guaranteed?");
 		guaranteed.setReadOnly(true);
 		guaranteed.setStyleAttribute("color", "grey");
 		guaranteed.setVisible(false);
 		sessionForm.add(guaranteed);
 		
 		add(sessionForm);
 		
 		add(wp);
 		add(ep);
 		
 	}
 	
 	private void initListeners() {
 		// when a project gets picked, populate the sessions combo
		sessions.addListener(Events.SelectionChange, new Listener<BaseEvent>() {
 		  	public void handleEvent(BaseEvent be) {
	  			sessionHandle = sessions.getSimpleValue();
		  		// go git it
		  		getSession(sessions.getSimpleValue());
 		   	}
 		});				
 	}
 	
 	// gets all project codes form the server and populates the project combo
 	public void updateSessionOptions() {
 		JSONRequestCache.get("/scheduler/sessions/options"
 				, new HashMap<String, Object>() {{
 			    	  put("mode", "session_handles");
 			        }}
 				, new JSONCallbackAdapter() {
 			public void onSuccess(JSONObject json) {
 				// get ready to populate the project codes list
 				sessions.removeAll();
 				session_ids.clear();
 				JSONArray handles = json.get("session handles").isArray();
 				JSONArray ids    = json.get("ids").isArray();
 				for (int i = 0; i < handles.size(); ++i){
 					String handle = handles.get(i).toString().replace('"', ' ').trim();
 					int id = (int) ids.get(i).isNumber().doubleValue();
 					session_ids.put(handle, id);
 					sessions.add(handle);
 					
 				}
 			}
 		});
 	}	
 	
 	// retrieves the project's JSON
 	private void getSession(String handle) {
 		
 		// don't bother if it doesn't even look like a valid pcode
 		if ((handle == null) || (handle.equals(new String("")))) {
 			//Window.alert("You must select a valid project code.");
 			return;
 		}
 		
 		String url = "/scheduler/sessions/" + session_ids.get(handle);
 		HashMap <String, Object> keys = new HashMap<String, Object>();
 		
 		JSONRequest.get(url, keys, new JSONCallbackAdapter() {
 			// this url returns all the time accounting for the whole proj., 
 			// so use it to update the whole UI
 			public void onSuccess(JSONObject json) {
 				sessionJson = json;
 				populateSessionPage(json);
 			}
 		});
 
 	}	
 	
 	// populates this page's widgets with the project's JSON values
 	protected void populateSessionPage(JSONObject json) {
 		JSONObject s = json.get("session").isObject();
 		
 		String sessionType = s.get("type").isString().stringValue();
 		type.setValue(sessionType);
 		
 		Boolean g = s.get("guaranteed").isBoolean().booleanValue();
 		guaranteed.setValue(g);
 		
         String sessionHandle = s.get("handle").isString().stringValue(); 
 		int sessionId = (int) s.get("id").isNumber().doubleValue();
 		
 		if (sessionType.equals("windowed") == true) {
 			wp.setVisible(true);
 			wp.getWindows(sessionId, sessionHandle);
 			ep.setVisible(false);
 			guaranteed.setVisible(true);
 		} else if (sessionType.equals("elective") == true) {
 			ep.setVisible(true);
 			ep.getElectives(sessionId, sessionHandle);
 			wp.setVisible(false);
 			guaranteed.setVisible(true);
 		} else {
 			wp.setVisible(false);
 			ep.setVisible(false);
 			guaranteed.setVisible(false);
 		}
 			
 		
 	}
 
 }
