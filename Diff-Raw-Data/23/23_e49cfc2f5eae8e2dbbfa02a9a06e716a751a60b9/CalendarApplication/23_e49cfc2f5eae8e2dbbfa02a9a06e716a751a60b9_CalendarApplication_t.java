 package com.example.calendar;
 
 import java.util.Date;
 
import com.example.calendar.ui.MenuBarNavigation;
import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Window;
 
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
		getMainWindow().addComponent(popupDateField);
		getMainWindow().addComponent(Today);
		getMainWindow().addComponent(Clear);
 	}
 
 	private void buildMainButtons() {
 		Today = new Button("Today");
 		Today.addListener(this);
 		Clear = new Button("Clear");
 		Clear.addListener(this);
 	}
 
 	private void buildMainLayout() {
 		Window mainWindow = new Window("Calendar");
 		popupDateField = new PopupDateField();
 		popupDateField.setDateFormat("yyyy-MM-dd");
 		setMainWindow(mainWindow);		
 	}
 
 	public void buttonClick(Button.ClickEvent event) {
 		if (event.getButton() == Today)
 		popupDateField.setValue(new Date());
 		else if (event.getButton() == Clear)
 			popupDateField.setValue(null);		
 	}
 }
