 package edu.nrao.dss.client;
 
 import java.util.HashMap;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;  
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;  
 import com.extjs.gxt.ui.client.widget.VerticalPanel;  
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;  
 import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;  
 import com.extjs.gxt.ui.client.widget.form.FormPanel;  
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.form.TextField;  
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.layout.FormData;  
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.Element;  
 
 public class InvestigatorForm extends LayoutContainer {  
 
     private VerticalPanel vp;
     private SimpleComboBox<String> users      = new SimpleComboBox<String>();
     private HashMap<String, Integer> user_ids = new HashMap<String, Integer>();
     private Window window;  
     private FormData formData;
 	private InvestigatorExplorer investigatorExplorer;
 	private Button submit = new Button("Submit");
 	
	
     public InvestigatorForm(Window w, InvestigatorExplorer investExp) {
     	setWindow(w);
    	w.add(this);
    	w.setSize(375, 175);
     	investigatorExplorer = investExp;
     }
     
 	@Override  
     protected void onRender(Element parent, int index) {  
       super.onRender(parent, index);  
       formData = new FormData("-20");  
       vp = new VerticalPanel();  
       vp.setSpacing(10);  
       createForm();
       updateUserOptions();
       add(vp);  
     }  
    
     private void createForm() {  
       FormPanel form = new FormPanel();  
       form.setHeading("Add Investigator");  
       form.setFrame(true);  
       form.setWidth(350);  
    
       users.setFieldLabel("User");
       users.setTriggerAction(TriggerAction.ALL);
       form.add(users);
      
       getSubmit().addSelectionListener(new SelectionListener<ButtonEvent>() {
 
 		@Override
 		public void componentSelected(ButtonEvent ce) {
 			HashMap<String, Object> fields = new HashMap<String, Object>();
         	fields.put("project_id", investigatorExplorer.getProject_id());
         	fields.put("user_id", user_ids.get(users.getSimpleValue()));
         	investigatorExplorer.addRecord(fields);
        	getWindow().hide();
        	
 			
 		}
     	  
       });
       form.addButton(getSubmit());
       
       Button c = new Button("Cancel");
       c.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
 		@Override
 		public void componentSelected(ButtonEvent ce) {
			getWindow().hide();
 		} 
       });
       form.addButton(c); 
     
       form.setButtonAlign(HorizontalAlignment.CENTER);  
     
       FormButtonBinding binding = new FormButtonBinding(form);  
       binding.addButton(getSubmit());  
     
       vp.add(form);  
     }
     
  // gets all project codes form the server and populates the project combo
 	public void updateUserOptions() {
 		JSONRequest.get("/sessions/options"
 			      , new HashMap<String, Object>() {{
 			    	  put("mode", "users");
 			        }}
 			      , new JSONCallbackAdapter() {
 			public void onSuccess(JSONObject json) {
 				// get ready to populate the project codes list
 				users.removeAll();
 				user_ids.clear();
 				JSONArray invests = json.get("users").isArray();
 				JSONArray ids     = json.get("ids").isArray();
 				for (int i = 0; i < invests.size(); ++i){
 					String invest = invests.get(i).toString().replace('"', ' ').trim();
 					int id = (int) ids.get(i).isNumber().doubleValue();
 					user_ids.put(invest, id);
 					users.add(invest);
 				}
 			}
 		});
 	}
 
 	public void setWindow(Window window) {
 		this.window = window;
 	}
 
 	public Window getWindow() {
 		return window;
 	}
 
 	public void setSubmit(Button submit) {
 		this.submit = submit;
 	}
 
 	public Button getSubmit() {
 		return submit;
 	}
 }
