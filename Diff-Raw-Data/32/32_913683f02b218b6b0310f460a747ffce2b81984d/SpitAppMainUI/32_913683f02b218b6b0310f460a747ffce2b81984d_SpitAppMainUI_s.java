 package spitapp.gui;
 
 import java.util.Locale;
 
 import spitapp.controller.AppointmentController;
 
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 
 /**
  * The Application's "main" class
  */
 @SuppressWarnings("serial")
 public class SpitAppMainUI extends UI
 {
 	AppointmentController controller = null;
 	
     @Override
     protected void init(VaadinRequest request) {
     	
     	controller = new AppointmentController();
     	
     	// Set the root layout for the UI
     	VerticalLayout content = new VerticalLayout();
     	setContent(content);
     	content.setLocale(Locale.GERMAN);
     	 
     	// Add the topmost component.
     	Label title = new Label("SpitApp Webfrontend made with Vaadin by Swen, Pascal[1], Pascal[2] and Roger");
     	title.setWidth(800, Unit.PIXELS);
     	title.setHeight(50, Unit.PIXELS);
     	content.addComponent(title);
     	content.setComponentAlignment(title, Alignment.MIDDLE_LEFT);
     	 
     	// Add a horizontal layout for the bottom part.
     	HorizontalLayout bottom = new HorizontalLayout();
     	content.addComponent(bottom);
         
         // Add a tabsheet with the other GUI components
         TabSheet tabsheet = new TabSheet();
         tabsheet.setHeight(600, Unit.PIXELS);
         tabsheet.setWidth(600, Unit.PIXELS);
         tabsheet.addTab(new DocumentGuiHandler(controller), "Dokumente");
         tabsheet.addTab(new ExpensesGuiHandler(controller) , "Spesen");
         tabsheet.addTab(new TaskListGuiHandler(controller), "ToDo's");
         tabsheet.addTab(new TaskTimeGuiHandler(controller), "Zeitrapporte");
  
     	// Add the appointments
         AppointmentGuiHandler appointments = new AppointmentGuiHandler(controller, tabsheet);
         appointments.setWidth(300, Unit.PIXELS);
         appointments.setHeight(600, Unit.PIXELS);
         
         // Add the components to the buttom layout
     	bottom.addComponent(appointments);
     	bottom.addComponent(tabsheet);
     }
 }
