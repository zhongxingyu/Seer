 package com.example.calendar;
 
import com.example.calendar.ui.*;
import com.vaadin.Application;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
 import java.util.Date;
 
 
 public class CalendarApplication extends Application implements ClickListener {
 	private PopupDateField popupDateField;
 	private Button Today;
 	private Button Clear;
 	
 	private MenuBarNavigation menubar = new MenuBarNavigation();
 
 	@Override
 	public void init() {
 		buildMainLayout();	
 		buildMainButtons();
 		buildMainMenu();
}
 
 	private void buildMainMenu() {
         getMainWindow().addComponent(menubar);
 	}
 
 	private void buildMainButtons() {
 		Today = new Button("Today");
 		Today.addListener(this);
 		Clear = new Button("Clear");
 		Clear.addListener(this);
		getMainWindow().addComponent(Today);
		getMainWindow().addComponent(Clear);
 	}
 
 	private void buildMainLayout() {
 		Window mainWindow = new Window("Calendar");
 		popupDateField = new PopupDateField();
 		popupDateField.setDateFormat("yyyy-MM-dd");
		mainWindow.addComponent(popupDateField);
 		setMainWindow(mainWindow);		
 	}
 
 	public void buttonClick(Button.ClickEvent event) {
 		if (event.getButton() == Today)
 		popupDateField.setValue(new Date());
 		else if (event.getButton() == Clear)
 			popupDateField.setValue(null);		
 	}
 }
