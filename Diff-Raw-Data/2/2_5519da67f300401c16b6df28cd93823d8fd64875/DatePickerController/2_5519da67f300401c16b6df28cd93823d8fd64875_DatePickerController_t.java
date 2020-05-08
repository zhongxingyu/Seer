 package controllers;
 
 import java.awt.event.ActionEvent;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import views.gui.DatePicker;
 
 import com.toedter.calendar.JCalendar;
 
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
 	public static String dateDisplay(Calendar calendar) {
 		return ""+calendar.get(GregorianCalendar.DAY_OF_MONTH)+"."+
				   (calendar.get(GregorianCalendar.MONTH)+1)+"."+
 				   calendar.get(GregorianCalendar.YEAR);
 	}
 }
