 package edu.nrao.dss.client.forms;
 
 import java.util.ArrayList;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.Field;
 import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.RadioGroup;
 import com.extjs.gxt.ui.client.widget.layout.TableData;
 import com.google.gwt.core.client.GWT;
 
 import edu.nrao.dss.client.data.InputData;
 import edu.nrao.dss.client.data.ResultsData;
 
 public abstract class BasicForm extends FormPanel {
 	public ArrayList<BasicForm> observers = new ArrayList<BasicForm>();
 	FormButtonBinding saveBinding = new FormButtonBinding(this);
 
 	public BasicForm(String title) {
 		setFrame(true);
 		setCollapsible(true);
 		setAction("/calculator/set_terms/");
 		setMethod(FormPanel.Method.POST);
 
 		// setting layout
 		setLabelWidth(200);
 		setLabelAlign(LabelAlign.RIGHT);
 
 		setHeading(title);
 		initLayout();
 
 		HorizontalPanel buttons = new HorizontalPanel();
 		buttons.setTableWidth("100%");
 		buttons.setSpacing(10);
 		
 		
 		add(buttons);
 
 		layout(true);
 	}
 
 	public abstract void validate();
 
 	public abstract void initLayout();
 
 	public abstract void notify(String name, String value);
 
 	public void addObserver(BasicForm form) {
 		observers.add(form);
 	}
 
 	public void notifyAllForms() {
 
 		for (BasicForm form : observers) {
 			for (Field fe : this.getFields()) {
 				String name = fe.getName();
 				
 				if (name != null) {
 					if (name.equals("frame")) {
 						form.notify(name, ((RadioGroup) fe).getValue().getValueAttribute());
 					} else {
 						form.notify(name, fe.getRawValue());
 					}
 				}
 			}
 		}
 	}
 	
 	SelectionListener<ButtonEvent> updateTerms = new SelectionListener<ButtonEvent>() {
 		public void componentSelected(ButtonEvent ce) {
 			submit();
 			ResultsData.loadResults();
 			InputData.loadInput();
 		}
 	};
 	
 	public void submit() {
 		super.submit();
 		for(Field<?> field : getFields()){
 			field.getElement().getParentElement().removeClassName("x-grid3-dirty-cell");
 		}
 	}
 
 }
