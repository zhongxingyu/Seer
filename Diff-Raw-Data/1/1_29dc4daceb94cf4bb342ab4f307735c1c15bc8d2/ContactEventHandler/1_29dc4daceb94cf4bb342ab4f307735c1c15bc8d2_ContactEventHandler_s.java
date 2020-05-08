 package de.hswt.hrm.contact.ui.event;
 
 import org.eclipse.e4.xwt.XWT;
import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Text;
 
 public class ContactEventHandler {
 
     protected void button(Event event) {
         Object o = XWT.findElementByName(event.widget, "t");
         // System.out.println("entering event....");
         Text b = (Text) o;
         b.setText("BAM!");
     }
 
     protected void text(Event event) {
         Text t = (Text) event.widget;
         System.out.println(t.getText());
     }
 
     public void onDefaultSelection(Event event) {
 
     }
 }
