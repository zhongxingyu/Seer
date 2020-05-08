 package nl.sense_os.testing.client.widgets;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
import nl.sense_os.testing.client.common.grid.PaginationGridPanel;
 
 import com.extjs.gxt.ui.client.data.ModelType;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.fx.Draggable;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.user.client.Window;
 
 
 public class NavigationPanel extends ContentPanel {
 	
 	public NavigationPanel() {
 		setHeaderVisible(false);
 		
 		ContentPanel panel = new ContentPanel();  
 		panel.setHeaderVisible(false);
 		panel.setBodyBorder(false);	
 		panel.setLayout(new AccordionLayout());
 
 		List<ColumnConfig> colConf = new ArrayList<ColumnConfig>();
 		ColumnConfig column = new ColumnConfig();
 		column.setId("users");
 		//column.setHeader("users");
 		column.setDataIndex("name");
 		column.setWidth(198);
 		colConf.add(column);
 		ColumnModel cm = new ColumnModel(colConf);
 
 		ModelType dataModel = new ModelType();
 		dataModel.setTotalName("total");
 		dataModel.setRoot("users");
 		dataModel.addField("id");
 		dataModel.addField("name");
 		
 		PaginationGridPanel gridPanel = new PaginationGridPanel(
 				"http://dev2.almende.com/commonsense/testing/users_nav_test.php", 
 				dataModel, 
 				cm, 
 				5);
 
 		gridPanel.setAutoHeight(true);
 		gridPanel.setAutoWidth(true);
 		gridPanel.setHeaderVisible(false);
 		
 		gridPanel.addListener(Events.CellClick, new Listener<BaseEvent>() {
 			@Override
             public void handleEvent(BaseEvent be) {
 				GridEvent<?> gr = (GridEvent<?>) be;
 				String value = gr.getModel().get("name");
 				//System.out.println("name: " + value);
 				
 				HashMap<String, String> winDataParams = new HashMap<String, String>();
 				winDataParams.put("name", value);
 				
 				/*
 				Window w = new Window();
 				w.setHeading("user profile");
 				w.setModal(true);
 				w.setSize(650, 450);
 				w.setMaximizable(true);
 				w.setToolTip("The ExtGWT product page...");
 				w.setUrl("http://www.google.com");
 				w.show();
 				*/
 				UserProfileWin profile = new UserProfileWin(300, 150, winDataParams);
 				profile.addNotification();
 				profile.show();
 			}
 		});
 		
 		gridPanel.load();
 
 		ContentPanel cp = new ContentPanel();  
 		cp.setAnimCollapse(false);  
 		cp.setHeading("online users");  
 		cp.setLayout(new RowLayout());
 		cp.add(gridPanel);
 		panel.add(cp);
         
 		cp = new ContentPanel();  
 		cp.setAnimCollapse(false);  
 		cp.setBodyStyleName("pad-text");
 		cp.setHeading("settings");
 		cp.addText("settings ...");
 		panel.add(cp);
 		
 		cp = new ContentPanel();  
 		cp.setAnimCollapse(false);  
 		cp.setBodyStyleName("pad-text");  
 		cp.setHeading("devices");  
 		cp.addText("stuff ...");  
 		panel.add(cp);		
 		
 		cp = new ContentPanel();  
 		cp.setAnimCollapse(false);  
 		cp.setBodyStyleName("pad-text");  
 		cp.setHeading("More Stuff");  
 		cp.addText("more stuff...");  
 		panel.add(cp);
 		panel.setSize(200, 325);  
 		
 		add(panel); 
 	}
 	
 }
