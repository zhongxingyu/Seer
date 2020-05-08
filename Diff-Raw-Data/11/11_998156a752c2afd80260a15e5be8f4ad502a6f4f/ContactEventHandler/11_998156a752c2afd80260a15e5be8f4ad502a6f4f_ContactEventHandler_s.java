 package de.hswt.hrm.contact.ui.event;
 
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Text;
 
 public class ContactEventHandler {
 
     protected void button(Event event) {
        Button b = (Button) event.widget;
        b.setText("Hello World");
        System.out.println(b.getText());
     }
 
     protected void text(Event event) {
         Text t = (Text) event.widget;
         System.out.println(t.getText());
     }
 
     public void onDefaultSelection(Event event) {
 
        System.out.println("entering event....");
     }
 }
