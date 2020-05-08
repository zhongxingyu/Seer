 package nl.sense_os.testing.client.widgets;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
import nl.sense_os.testing.client.widgets.grid.PaginationGridPanel;
 
 import com.extjs.gxt.ui.client.data.ModelType;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.util.Padding;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
 import com.extjs.gxt.ui.client.widget.layout.FormLayout;
 import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
 import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
 import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
 
 public class GroupSearchForm extends ContentPanel {
 
 	public GroupSearchForm() {
 		setHeaderVisible(false);
 
 		// Form
 		FormPanel form = new FormPanel();		
 		form.setFrame(true);
 		//form.setLabelSeparator("");
 		form.setHeading("Group search");
 		form.setWidth(350);
 
 		FormLayout formLayout = new FormLayout();
 		formLayout.setLabelSeparator("");		
 		form.setLayout(formLayout);
 
 		// Group search
 		TextField<String> groupSearch = new TextField<String>();
 		groupSearch.setFieldLabel("search");
 		
 		// Buttons
 		Button searchBtn = new Button();
 		searchBtn.setText("search");
 		searchBtn.setWidth(100);
 		searchBtn.addListener(Events.OnClick, new Listener<ButtonEvent>() {
 			public void handleEvent(ButtonEvent be) {
 				displayResults();
 			}
 		});
 		
 		LayoutContainer btnContainer= new LayoutContainer();
 		HBoxLayout layout = new HBoxLayout();
 		layout.setPadding(new Padding(5));  
 		layout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
 		layout.setPack(BoxLayoutPack.CENTER);  
 		btnContainer.setLayout(layout);  
 
 		HBoxLayoutData layoutData = new HBoxLayoutData(new Margins(0, 5, 0, 0));  
 		btnContainer.add(searchBtn, layoutData);
 		
 		// Adds widgets to the form.
 		form.add(groupSearch);
 		form.add(btnContainer);
 
 		// Adds the form to the content panel.
 		add(form);		
 	}
 
 	private void displayResults() {
 		List<ColumnConfig> colConf = new ArrayList<ColumnConfig>();
 		ColumnConfig column = new ColumnConfig();
 		column.setId("groups");
 		column.setDataIndex("name");
 		column.setWidth(348);
 		colConf.add(column);
 		ColumnModel cm = new ColumnModel(colConf);
 
 		ModelType dataModel = new ModelType();
 		dataModel.setTotalName("total");
 		dataModel.setRoot("groups");
 		dataModel.addField("id");
 		dataModel.addField("name");
 		
 		PaginationGridPanel gridPanel = new PaginationGridPanel(
 				"http://dev2.almende.com/commonsense/testing/groups_list_test.php", 
 				dataModel,
 				cm, 
 				3);
 
 		gridPanel.setAutoHeight(true);
 		gridPanel.setWidth(350);
 		gridPanel.setHeaderVisible(false);
 		
 		gridPanel.addListener(Events.CellClick, new Listener<BaseEvent>() {
 			@Override
             public void handleEvent(BaseEvent be) {
 				// Gets the group name from the data store through the event.
 				GridEvent<?> gr = (GridEvent<?>) be;
 				String value = gr.getModel().get("name");
 				
 				HashMap<String, String> winDataParams = new HashMap<String, String>();
 				winDataParams.put("name", value);
 				
 				GroupInfoWin group = new GroupInfoWin(310, 150, winDataParams);
 				group.addNotification();
 				group.show();
 			}
 		});
 		
 		gridPanel.load();
 		
 		add(gridPanel);
 		doLayout();
 	}
 }
