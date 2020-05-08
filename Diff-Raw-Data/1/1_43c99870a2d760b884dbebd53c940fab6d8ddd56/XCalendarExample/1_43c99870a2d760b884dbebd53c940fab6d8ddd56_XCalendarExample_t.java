 package wingset;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.swing.*;
 
 import org.wings.*;
 import org.wings.text.SDateFormatter;
 import org.wingx.XCalendar;
 
 public class XCalendarExample extends WingSetPane {
 
     protected SComponent createControls() {
         return null;
     }
 
     public SComponent createExample() {
         SGridLayout layout = new SGridLayout(3);
         layout.setHgap(15);
         SPanel panel = new SPanel(layout);
 
         //
         // Regular nullable calendar
         final DateFormat nullableDateFormat = new NullableDateFormatter();
         panel.add(new SLabel("Calendar: ", SConstants.RIGHT_ALIGN));
         final XCalendar nullablXCalendar = new XCalendar(new SDateFormatter(nullableDateFormat));
         nullablXCalendar.setNullable(true);
         nullablXCalendar.setDate(null);
         panel.add(nullablXCalendar);
 
         final SLabel valueLabel1 = new SLabel("< press submit >");
         panel.add(valueLabel1);
 
         //
         // Date spinner example
         Calendar calendar = new GregorianCalendar();
         Date initDate = calendar.getTime();
 
         calendar.add(Calendar.YEAR, -50);
         Date earliestDate = calendar.getTime();
 
         calendar.add(Calendar.YEAR, 100);
         Date latestDate = calendar.getTime();
 
         final SSpinner spinner = new SSpinner(new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.MONTH));
         final CalendarEditor calendarEditor = new CalendarEditor(spinner, new SDateFormatter(DateFormat.getDateInstance()));
         spinner.setEditor(calendarEditor);
 
         panel.add(new SLabel("Spinner: ", SConstants.RIGHT_ALIGN));
         panel.add(spinner);
 
         final SLabel valueLabel2 = new SLabel("< press submit >");
         panel.add(valueLabel2);
 
         // For debugging purposes. Maybe ommit those labels
         final SButton button = new SButton("submit input");
         button.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 valueLabel1.setText(""+nullablXCalendar.getDate());
                 valueLabel2.setText(""+calendarEditor.getCalendar().getDate());
             }
         });
         panel.add(new SLabel());
         panel.add(button);
 
 
         return panel;
 
     }
 
 
     public static class CalendarEditor extends SSpinner.DefaultEditor {
         private XCalendar calendar = null;
 
         public CalendarEditor(SSpinner spinner, SDateFormatter formatter) {
             super(spinner);
 
             removeAll();
 
             calendar = new XCalendar(formatter);
             calendar.getFormattedTextField().setColumns(15);
 
             add(calendar);
 
         }
 
         public XCalendar getCalendar() {
             return calendar;
         }
 
         public SFormattedTextField getTextField() {
             return calendar.getFormattedTextField();
         }
     }
 
     /**
      * A simple date formatter that parses and allows empty strings as <code>null</code>.
      */
     private static class NullableDateFormatter extends SimpleDateFormat {
         public NullableDateFormatter() {
             super("dd.MM.yyyy");
         }
 
         public Object parseObject(String source) throws ParseException {
             if (source == null || source.trim().length() == 0) {
                 return null;
             }
             return super.parseObject(source);
         }
 
 
         public Date parse(String text, ParsePosition pos) {
             if (text == null || text.trim().length() == 0) {
                 return null;
             }
             return super.parse(text, pos);
         }
     }
 }
