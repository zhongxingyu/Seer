 package nl.sense_os.Sample.client;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import nl.sense_os.Sample.client.widgets.PaginationGridPanel;
 import nl.sense_os.Sample.client.widgets.UserProfileWin;
 
 import com.extjs.gxt.ui.client.data.BaseListLoader;
 import com.extjs.gxt.ui.client.data.HttpProxy;
 import com.extjs.gxt.ui.client.data.JsonLoadResultReader;
 import com.extjs.gxt.ui.client.data.ListLoadResult;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.ModelType;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.GridEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.fx.Draggable;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.custom.Portlet;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
 import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.extjs.gxt.ui.client.widget.layout.TableLayout;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.Image;
 
 
 //public class NavigationPanel extends LayoutContainer {
 public class NavigationPanel extends ContentPanel {
 	
 	public NavigationPanel() {
 		/*
         final Image logo = new Image("/img/logo_sense-150.png");
         logo.setPixelSize(131, 68);
 		 */
         
         /*
         logo.addMouseOverHandler(new MouseOverHandler() {
 			
 			@Override
 			public void onMouseOver(MouseOverEvent event) {
 				// TODO Auto-generated method stub
                 System.out.println("relative x: " + event.getRelativeX(logo.getElement()));
                 System.out.println("relative y: " + event.getRelativeY(logo.getElement()));				
 			}
 		});
 		*/
 		
         /*
         final LayoutContainer logoContainer = new LayoutContainer(new CenterLayout());
         logoContainer.setHeight(68);
         logoContainer.add(logo);
 		*/
 		
         //setScrollMode(Scroll.AUTOY);
         //add(logoContainer, new RowData(-1, -1, new Margins(10, 0, 0, 0)));
         //add(timeRangePanel, new RowData(1, -1, new Margins(10, 0, 0, 0)));
         //setStyleAttribute("backgroundColor", "transparent");
         //setBorders(false);
 
         //setStyleAttribute("backgroundColor", "transparent");
         //setBorders(false);
         //setHeaderVisible(false);
 
 		setHeaderVisible(false);
 		
 		ContentPanel panel = new ContentPanel();  
 		panel.setHeaderVisible(false);
 		panel.setBodyBorder(false);  
 		 
 		panel.setLayout(new AccordionLayout());  
 		//panel.setIcon(Resources.ICONS.accordion());  
 
 		List<ColumnConfig> colConf = new ArrayList<ColumnConfig>();
 		ColumnConfig column = new ColumnConfig();
 		column.setId("users");
 		column.setHeader("users");
 		column.setDataIndex("name");
 		column.setWidth(198);
 		colConf.add(column);
 		/*
 		column = new ColumnConfig();
 		column.setId("id");
 		column.setHeader("id");
 		column.setDataIndex("id");
 		column.setWidth(15);			
 		colConf.add(column);
 		*/
 		ModelType model = new ModelType();
 		model.setTotalName("total");
 		model.setRoot("users");
 		model.addField("id");
 		model.addField("name");
 		
 		PaginationGridPanel gridPanel = new PaginationGridPanel(
				"http://dev.almende.com/commonsense/users_nav_test.php", 
 				model, 
 				colConf, 
 				5);
 		//grid.setWidth(195);
 		//grid.setHeight(200);
 		gridPanel.setAutoHeight(true);
 		gridPanel.setAutoWidth(true);
 		
 		gridPanel.getGrid().addListener(Events.CellClick, new Listener<BaseEvent>() {
 			public void handleEvent(BaseEvent be) {
 				GridEvent<?> gr = (GridEvent<?>) be;
 				String value = gr.getModel().get("name");
 				System.out.println("name: " + value);
 				
 				HashMap<String, String> params = new HashMap<String, String>();
 				params.put("name", value);
 				
 				/*
 				Window w = new Window();
 				w.setHeading("user profile");
 				w.setModal(true);
 				w.setSize(650, 450);
 				w.setMaximizable(true);
 				w.setToolTip("The ExtGWT product page...");
 				//w.setUrl("http://www.google.com");
 				w.show();
 				*/
 				UserProfileWin profile = new UserProfileWin(300, 150, params);
 				//profile.setMaximizable(true);
 				profile.show();
 
 			}
 		});
 		
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
 		cp.addText("Settings ...");  
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
