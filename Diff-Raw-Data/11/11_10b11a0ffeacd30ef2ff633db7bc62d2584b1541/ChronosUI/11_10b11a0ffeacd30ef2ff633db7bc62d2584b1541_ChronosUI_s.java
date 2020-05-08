 package edu.ycp.cs320.chronos.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.PasswordTextBox;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.TextBoxBase;
 
 import edu.ycp.cs320.chronos.database.Database;
 import edu.ycp.cs320.chronos.database.IDatabase;
 
 
 public class ChronosUI implements EntryPoint{
 
 	public static ChronosUI instance;
 	private static IsWidget currentView;
 	
 	@SuppressWarnings("deprecation")
 	@Override
 	
 	public void onModuleLoad() {
 		instance = this;
 		
 		setCurrentView(new LoginView());
 		
 		
 	}
 	
 	public static void setCurrentView(IsWidget cv) {
		if (cv != null) {
			RootLayoutPanel.get().remove(cv);
 		}
 		currentView = cv;
 		RootLayoutPanel.get().add(cv);
 		RootLayoutPanel.get().setWidgetTopBottom(cv, 10.0, Unit.PX, 10.0, Unit.PX);
 		RootLayoutPanel.get().setWidgetLeftRight(cv, 10.0, Unit.PX, 10.0, Unit.PX);
 	}
 	
 }
 
 
