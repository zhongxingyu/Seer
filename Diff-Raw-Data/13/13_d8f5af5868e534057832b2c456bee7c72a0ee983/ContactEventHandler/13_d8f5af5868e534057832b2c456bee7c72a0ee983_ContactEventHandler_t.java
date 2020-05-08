 package de.hswt.hrm.contact.ui.event;
 
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Text;
 
 public class ContactEventHandler {
 
    public void onFocusOut(Event event) {
 
         Text text = (Text) event.widget;
        text.setText("Suche");
     }
 
    public void onFocusIn(Event event) {
         Text text = (Text) event.widget;
        text.setText("");
     }
 }
