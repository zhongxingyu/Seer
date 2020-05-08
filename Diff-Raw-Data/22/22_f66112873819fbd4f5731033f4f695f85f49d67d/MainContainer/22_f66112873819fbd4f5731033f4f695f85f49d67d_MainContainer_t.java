 package nl.sense_os.testing.client;
 
import nl.sense_os.testing.client.widgets.SensorDataGrid;

 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.fx.Draggable;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.RootPanel;
 
 public class MainContainer implements EntryPoint {
 
 	public void onModuleLoad() {
 		/*
         Viewport vp = new Viewport();
         vp.setLayout(new FitLayout());
         vp.setLayout(new RowLayout());
 
         NavigationPanel nav = new NavigationPanel();
         vp.add(nav, new MarginData(0));
 
         SensorDataGrid sg = new SensorDataGrid();
         vp.add(sg, new MarginData(10));
         
         RootPanel.get().add(vp);
         */        
         
         LayoutContainer main = new LayoutContainer();
         main.setLayout(new BorderLayout());
 
         LayoutContainer north = new LayoutContainer();
         
         BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 80);
         northData.setSplit(true);
         northData.setMargins(new Margins(3));
         
         final Image logo = new Image("/img/logo_sense-150.png");
         logo.setPixelSize(131, 68);        
         north.add(logo);
 
         LayoutContainer west = new LayoutContainer();
         west.setBorders(true);
         //west.setLayout(new AccordionLayout());        
         NavigationPanel nav = new NavigationPanel();
         //nav.setZIndex(10);
         west.add(nav);        
         new Draggable(west);
 
         BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 200, 100, 300);  
         westData.setMargins(new Margins(5, 0, 5, 5));  
         westData.setCollapsible(true);
         
         SensorDataGrid sg = new SensorDataGrid();
         
         LayoutContainer portal = new LayoutContainer();
         portal.setHeight(500);
         //portal.setLayout(new FlowLayout());
         portal.setBorders(true);
         portal.add(sg);
         
         BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);  
         centerData.setMargins(new Margins(5));      
         
         main.add(north, northData);
         main.add(west, westData);
         main.add(portal, centerData);
         	
         RootPanel.get().add(main);
 	}
 	
 	public void onModuleLoad_bkp() {
 		/*
         Viewport vp = new Viewport();
         vp.setLayout(new FitLayout());
         vp.setLayout(new RowLayout());
 
         NavigationPanel nav = new NavigationPanel();
         vp.add(nav, new MarginData(0));
 
         SensorDataGrid sg = new SensorDataGrid();
         vp.add(sg, new MarginData(10));
         
         RootPanel.get().add(vp);
         */        
         
         /*
         ContentPanel main = new ContentPanel();
         main.setLayout(new BorderLayout());
         */
 
         LayoutContainer main = new LayoutContainer();
         main.setLayout(new BorderLayout());
 
         ContentPanel north = new ContentPanel();
         north.setHeaderVisible(false);
         
         BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 80);
         northData.setSplit(true);
         northData.setMargins(new Margins(5));
         
         final Image logo = new Image("/img/logo_sense-150.png");
         logo.setPixelSize(131, 68);        
         north.add(logo);
 
         ContentPanel west = new ContentPanel();
         //west.setBodyBorder(false);
         west.setHeaderVisible(false);
         west.setBorders(true);
         //west.setHeading("West");
         //west.setLayout(new AccordionLayout());         
         
         NavigationPanel nav = new NavigationPanel();
         west.add(nav);
         new Draggable(west);
 
         BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 200, 100, 300);  
         westData.setMargins(new Margins(5, 0, 5, 5));  
         westData.setCollapsible(true);
         
         SensorDataGrid sg = new SensorDataGrid();
 
         /*
         Portal portal = new Portal(3);  
         portal.setBorders(true);
         portal.setStyleAttribute("backgroundColor", "white");  
         portal.setColumnWidth(0, .33);
         portal.setHeight(500);
         
         Portlet portlet = new Portlet();
         portlet.add(sg);        
         portal.add(portlet, 0);
 		*/
         
         ContentPanel portal = new ContentPanel();
         portal.setHeight(500);
         portal.setHeaderVisible(false);
         portal.setBorders(true);
         portal.add(sg);
         
         BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);  
         centerData.setMargins(new Margins(5));         
         
         main.add(north, northData);
         main.add(west, westData);
         main.add(portal, centerData);
         
         RootPanel.get().add(main);
 	}
 	
 }
