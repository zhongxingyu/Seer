 package com.nmt.nmj.editor.dialog;
 
 import java.text.DecimalFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Shell;
 
 public class CalendarDialog extends Dialog {
 
     private String selectedDate;
     private Date releaseDate;
 
     public CalendarDialog(Shell parentShell, Date releaseDate) {
         super(parentShell);
         this.releaseDate = releaseDate;
     }
 
     protected void configureShell(Shell newShell) {
         newShell.setText("Calendar");
         super.configureShell(newShell);
     }
 
     protected Control createDialogArea(Composite parent) {
         final DateTime dateTime = new DateTime(parent, SWT.CALENDAR | SWT.BORDER);
         if (releaseDate != null) {
             Calendar calendar = Calendar.getInstance();
             calendar.setTime(releaseDate);
             dateTime.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) - 1,
                     calendar.get(Calendar.DAY_OF_MONTH));
         }
         dateTime.addDisposeListener(new DisposeListener() {
             public void widgetDisposed(DisposeEvent e) {
                 selectedDate = "" + dateTime.getYear() + "-" + new DecimalFormat("00").format(dateTime.getMonth())
                         + "-" + new DecimalFormat("00").format(dateTime.getDay());
             }
         });
         Button todayButton = new Button(parent, SWT.PUSH);
         todayButton.setText("Today");
         todayButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
                 Calendar calendar = Calendar.getInstance();
                 dateTime.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                         calendar.get(Calendar.DAY_OF_MONTH));
             }
         });
         return parent;
     }
 
     public String getSelectedDate() {
         return selectedDate;
     }
 
     public static Date convertDate(String fullDate) {
         String[] splitedDate = fullDate.split("-");
         if (splitedDate.length != 3) {
             return null;
         }
         int year = 0;
         int month = 0;
         int day = 0;
         for (int i = 0; i < splitedDate.length; i++) {
             switch (i) {
             case 0:
                 year = Integer.valueOf(splitedDate[i]);
                 break;
             case 1:
                 month = Integer.valueOf(splitedDate[i]);
                 break;
             case 2:
                 day = Integer.valueOf(splitedDate[i]);
                 break;
             default:
                 break;
             }
         }
         Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.YEAR, year);
         calendar.set(Calendar.MONTH, month);
         calendar.set(Calendar.DAY_OF_MONTH, day);
         return calendar.getTime();
     }
 
 }
