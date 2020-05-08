 package de.hswt.hrm.contact.ui.event;
 
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Text;
 
 public class ContactEventHandler {
 
     public void clearText(Event event) {
 
         Text text = (Text) event.widget;
         text.setText("");
 
     }

    public void onFocusOut(Event event) {

        Text text = (Text) event.widget;
        text.setText("Suche");
    }
 }
