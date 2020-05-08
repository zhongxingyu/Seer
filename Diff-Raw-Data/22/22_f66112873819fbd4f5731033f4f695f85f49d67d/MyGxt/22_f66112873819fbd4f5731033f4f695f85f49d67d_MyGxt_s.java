 package nl.sense_os.testing.client;
 
 
 import com.extjs.gxt.ui.client.GXT;
 import com.extjs.gxt.ui.client.widget.Viewport;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.user.client.ui.RootPanel;
 
 /**
  * Entry point classes define <code/>onModuleLoad()</code>
  */
 public class MyGxt implements EntryPoint {
 	public void onModuleLoad() {
 		GXT.hideLoadingPanel("loading"); // hide loading ...
 		Viewport vp = new Viewport();
 		vp.setLayout(new FitLayout());
 		SensorDataGrid lp = new SensorDataGrid();
 		vp.add(lp);
 		RootPanel.get().add(vp);
 	}
 }
