 package com.github.wolfie.meteorcursor;
 
 import com.vaadin.Application;
import com.vaadin.ui.Button;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
 
 @SuppressWarnings("serial")
 public class MeteorcursorApplication extends Application {
   @Override
   public void init() {
     final Window mainWindow = new Window("Meteorcursor Application");
     final Label label = new Label("Hello Vaadin user");
     mainWindow.addComponent(label);
     setMainWindow(mainWindow);
     
     final MeteorCursor meteorCursor = new MeteorCursor();
     mainWindow.addComponent(meteorCursor);
    mainWindow.addComponent(new Button("Enable", new ClickListener() {
      private static final long serialVersionUID = 7162981081807888955L;
      private boolean meteorEnabled = false;
      
      public void buttonClick(final ClickEvent event) {
        meteorEnabled = !meteorEnabled;
        event.getButton().setCaption(meteorEnabled ? "Disable" : "Enable");
        meteorCursor.setEnabled(meteorEnabled);
      }
    }));
   }
   
 }
