 package controllers;
 
 import com.toedter.calendar.JCalendar;
 import views.gui.DatePicker;
 
 import java.awt.event.ActionEvent;
import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 public abstract class DatePickerController extends Controller {
     protected JCalendar jCalendar;
     protected DatePicker picker;
     public DatePickerController(){
     	
     }
     public DatePickerController(JCalendar jCalendar, DatePicker picker) {
         this.jCalendar = jCalendar;
         this.picker = picker;
     }
     public void setUp(JCalendar jCalendar, DatePicker picker){
     	this.jCalendar = jCalendar;
         this.picker = picker;
     }
     public GregorianCalendar getGregorianCalendar(){
     	GregorianCalendar calendar = new GregorianCalendar();
     	calendar.setTime(jCalendar.getDate());
     	return calendar;
     }
     @Override
     public void actionPerformed(ActionEvent arg0) {
     	picker.dispose();
     }
 }
