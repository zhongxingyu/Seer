 package edu.nrao.dss.client;
 
 import java.util.HashMap;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
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
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.extjs.gxt.ui.client.widget.menu.MenuItem;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.user.client.Element;
 
 public class ColumnConfigForm extends LayoutContainer{
 	private VerticalPanel vp;
     private Window window = new Window();  
     private FormData formData;
 	private Button submit = new Button("Submit");
 	private Explorer explorer;
 	
     public ColumnConfigForm(Explorer e) {
     	explorer = e;
     	window.add(this);
         window.setSize(375, 175);
     }
     
 	@Override  
     protected void onRender(Element parent, int index) {  
       super.onRender(parent, index);  
       formData = new FormData("-20");  
       vp = new VerticalPanel();  
       vp.setSpacing(10);  
       createForm();
       add(vp);
     }  
    
     private void createForm() {  
     	FormPanel form = new FormPanel();  
     	form.setHeading("Save Column Configuration");  
     	form.setFrame(true);  
     	form.setWidth(350);  
    
     	final TextField<String> name = new TextField<String>();
     	name.setFieldLabel("Name");
     	name.setAllowBlank(false);
     	form.add(name);
     
     	submit.addSelectionListener(new SelectionListener<ButtonEvent>() {
 
 			@SuppressWarnings("deprecation")
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				// Get the hidden status on each column in the explorer.
 				final HashMap<String, Object> fields = new HashMap<String, Object>();				
 				for (ColumnConfig cc : explorer.grid.getColumnModel().getColumns()){
 					fields.put(cc.getId(), cc.isHidden());
 				}
 				fields.put("name", name.getValue().toString());
 				// Used to identify the specific explorer on the server.
 				fields.put("explorer", explorer.rootURL);
 				
 				JSONRequest.post("/configurations/explorer/columnConfigs", fields, new JSONCallbackAdapter() {
 					@Override
 					public void onSuccess(JSONObject json) {
 						String new_id = json.get("id").isNumber().toString();
 						// Update save column combo list
						if (! explorer.columnConfigIds.contains(new_id)) {
							Button columns = explorer.getColumnsItem();
							columns.getMenu().add(new ColumnConfigMenuItem(explorer.grid
									, fields.get("name").toString()
									, new_id));
							explorer.columnConfigIds.add(new_id);
						}
 					}
 				});
 				getWindow().close();
 				
 			}
     	  
     	});
     	form.addButton(submit);
       
     	Button c = new Button("Cancel");
     	c.addSelectionListener(new SelectionListener<ButtonEvent>() {
 	
 			@SuppressWarnings("deprecation")
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				getWindow().close();
 			} 
     	});
     	form.addButton(c); 
     
     	form.setButtonAlign(HorizontalAlignment.CENTER);  
     
     	FormButtonBinding binding = new FormButtonBinding(form);  
     	binding.addButton(submit);  
     
     	vp.add(form);
     }
 
 	public void setWindow(Window window) {
 		this.window = window;
 	}
 
 	public Window getWindow() {
 		return window;
 	}
 	
 }
