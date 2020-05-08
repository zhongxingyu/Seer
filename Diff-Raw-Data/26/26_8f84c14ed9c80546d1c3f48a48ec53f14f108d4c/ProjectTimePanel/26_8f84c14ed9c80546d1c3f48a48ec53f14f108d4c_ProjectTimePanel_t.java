 package edu.nrao.dss.client.widget;
 
 import java.util.ArrayList;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.Style.VerticalAlignment;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.extjs.gxt.ui.client.widget.layout.TableData;
 import com.extjs.gxt.ui.client.widget.layout.TableLayout;
 import java.util.HashMap;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import edu.nrao.dss.client.TimeAccounting;
 
 import edu.nrao.dss.client.util.JSONCallbackAdapter;
 import edu.nrao.dss.client.util.JSONRequest;
 import edu.nrao.dss.client.util.JSONRequestCache;
 
 import edu.nrao.dss.client.widget.form.ProjAllotmentFieldSet;
 
 public class ProjectTimePanel extends ContentPanel {
 	// project level
 	private ArrayList<String> project_codes = new ArrayList<String>();
 	private SimpleComboBox<String> projects = new SimpleComboBox<String>();
 	private ProjAllotmentFieldSet projGrade1 = new ProjAllotmentFieldSet();
 	private ProjAllotmentFieldSet projGrade2 = new ProjAllotmentFieldSet();
 	private ProjectTimeAccountPanel projectTimeAccounting = new ProjectTimeAccountPanel();
 	private Button saveProj = new Button("Save Project Changes");
 	
 	private ContentPanel parent;
 
 	private String pcode = "";
 	
 	public ProjectTimePanel() {
 		initLayout();
 		initListeners();
 		updatePCodeOptions();
 	}
 
 	public void setParent(ContentPanel parent) {
 		this.parent = parent;
 	}
 
 	private void initLayout() {
 
 		// final ContentPanel project = new ContentPanel();
 		setLayout(new RowLayout(Orientation.VERTICAL));
 		setBorders(false);
 		setHeaderVisible(false);
 
 		// first the project table!
 		LayoutContainer projectTable = new LayoutContainer();
 		TableLayout tb = new TableLayout(2);
 		tb.setWidth("100%");
 		tb.setBorder(1);
 		projectTable.setLayout(tb);
 		projectTable.setBorders(true);
 
 		TableData td = new TableData();
 		td.setVerticalAlign(VerticalAlignment.TOP);
 		// Question: why must I do this, just to get the two forms to share
 		// space?
 		td.setColspan(1);
 		td.setWidth("400px");
 
 		// left side of project table lets you pick what project & session you
 		// want
 		final FormPanel projectForm = new FormPanel();
 		projectForm.setHeading("Project");
 		projectForm.setBorders(false);
 		projectForm.setBodyBorder(false);
 
 		// the project picker goes in this left-most form panel
 		projects.setFieldLabel("Project");
 		projects.setTriggerAction(TriggerAction.ALL);
 		projectForm.add(projects);
 
 		// then the save changes button
 		projectForm.add(saveProj);
 
 		projectTable.add(projectForm, td);
 
 		// The right side of the project table includes allotments by grade.
 		// a FieldSet for each grade allotment - the first always gets shown,
 		// not the second
 		FormPanel projectForm2 = new FormPanel();
 		projectForm2.setHeading("Allotments");
 		projectForm2.setBorders(false);
 		projectForm2.add(projGrade1);
 		projGrade2.setVisible(false);
 		projectForm2.add(projGrade2);
 
 		projectTable.add(projectForm2, td);
 
 		// so now we can add the top half of this panel
 		add(projectTable, new RowData(1, -1, new Margins(4)));
 
 		// the project time accounting panel is the second half
 		projectTimeAccounting.setHeading("Project Time Accounting");
 		projectTimeAccounting.collapse();
 		add(projectTimeAccounting);
 	}
 
 	private void initListeners() {
 
		projects.addListener(Events.SelectionChange, new Listener<BaseEvent>() {
 			public void handleEvent(BaseEvent be) {
				if (project_codes.contains(projects.getSimpleValue())) { 
 					pcode = projects.getSimpleValue();
 				    getProjectTimeAccounting();
 				}
 			}
 		});
 
 		saveProj.addListener(Events.OnClick, new Listener<BaseEvent>() {
 			public void handleEvent(BaseEvent be) {
 				// save changes to project times, and display new time accounting
 				sendProjectAllotments();
 			}
 		});
 
 	}
 
 	// gets all project codes form the server and populates the project combo
 	public void updatePCodeOptions() {
 		JSONRequestCache.get("/scheduler/sessions/options",
 				new HashMap<String, Object>() {
 					{
 						put("mode", "project_codes");
 					}
 				}, new JSONCallbackAdapter() {
 					public void onSuccess(JSONObject json) {
 						// get ready to populate the project codes list
 						projects.removeAll();
 						project_codes.clear();
 						JSONArray pcodes = json.get("project codes").isArray();
 						for (int i = 0; i < pcodes.size(); ++i) {
 							String pcode = pcodes.get(i).toString()
 									.replace('"', ' ').trim();
 							project_codes.add(pcode);
 							projects.add(pcode);
 
 						}
 					}
 				});
 	}
 
 	protected void getProjectTimeAccounting() {
 
 		JSONRequest.get(
 				"/scheduler/projects/time_accounting/"
 						+ projects.getSimpleValue(), new JSONCallbackAdapter() {
 					public void onSuccess(JSONObject json) {
 						// populate this panel
 						populateProjTimeAccounting(json);
 						// how does this choice affect other panels?
 						if (parent != null) {
 							((TimeAccounting) parent).projectSelected(projects
 									.getSimpleValue());
 						}
 					}
 				});
 	}
 
 	// given the JSON which has all the time accounting info in it, update the
 	// current project
 	public void populateProjTimeAccounting(JSONObject json) {
 
 		projGrade2.setVisible(false);
 		JSONArray times = json.get("times").isArray();
 		for (int i = 0; i < times.size(); ++i) {
 			// Note: we only can deal with up to two grades right now! this code
 			// sucks!
 			JSONObject time = times.get(i).isObject();
 			if (i == 0) {
 				// first grade field list
 				projGrade1.setValues(time);
 			}
 			if (i == 1) {
 				// second grade field list - make it visible!
 				projGrade2.setVisible(true);
 				projGrade2.setValues(time);
 
 			}
 		}
 		projectTimeAccounting.setValues(json);
 	}
 
 	// saves off changes to project allotments, then redisplays latest time
 	// accounting info
 	private void sendProjectAllotments() {
 		// send a JSON request for each grade being updated.
 		// First grade is always visible:
 		sendProjectAllotment(projGrade1, projectTimeAccounting.getDescription());
 		if (projGrade2.isVisible()) {
 			sendProjectAllotment(projGrade2,
 					projectTimeAccounting.getDescription());
 		}
 	}
 
 	// handles one grade's allotment numbers at a time
 	private void sendProjectAllotment(ProjAllotmentFieldSet fs, String desc) {
 
 		String url = "/scheduler/projects/time_accounting/"
 				+ projects.getSimpleValue();
 		HashMap<String, Object> keys = new HashMap<String, Object>();
 
 		keys.put("grade", fs.getGrade());
 		keys.put("total_time", fs.getAllotment());
 		keys.put("description", desc);
 
 		JSONRequest.post(url, keys, new JSONCallbackAdapter() {
 			// this url returns all the time accounting for the whole proj.,
 			// so use it to update the whole UI
 			public void onSuccess(JSONObject json) {
 				// populate the panel with these new results
 				populateProjTimeAccounting(json);
 			}
 		});
 	}
 	
 	public String getSelectedProject() {
 		return projects.getSimpleValue();
 	}
 }
