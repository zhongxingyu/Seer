 package Helpdesk.java.helpdesk.mvc.Model;
 /******************
  * Imports
  ******************/
 import Helpdesk.java.helpdesk.lib.ComboBox;
 import Helpdesk.java.helpdesk.mvc.View.Error_Frame;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 public class HtmlModel {
     
      /**************************
      *  set string to fullticket 
      *  editor pane with html codes 
      **************************/
     public static StringBuilder Htmlfullticket (Integer integer) {
         // Display the selected item
                 String [] Array = FullTicket.searchFullTicket(integer);
                 String product = "";
                 try {
                     product = ComboBox.getProductName(ComboBox.getPID(integer,null));
                 } catch (Exception e) {}
                 
                 //Set color for ticket status
                 String color = "red";
                 if (Array[1].equals("Open")) color = "green";
                 else if (Array[1].equals("In process")) color = "blue";
                 
                 for (int i = 0; i < Array.length ; i++) {
                     if (Array[i] == null || "null".equals(Array [i])) {
                         Array[i] = "";
                     }
                 }
 
                 //html formatted text for Fullticket Editor pane
                 StringBuilder builder = new StringBuilder("<h2>Ticket ID: " + integer +"</h2> "
                 +"<table border='0'>"
                 +"<tr>"
                 +"<td width='140'>"
                         + "<font color='#708090'><b>"
                         + "Created on:"
                         + "</b></font></td><td>"+ StringtoDate(Array[14]) +"</td>"
                 +"</tr><tr>"
                 +"<td>"
                         + "<font color='#708090'><b>"
                         + "Last update:"
                         + "</b></font></td><td>"+ StringtoDate(Array[15]) +"</td>"
                 +"</tr><tr>"      
                 +"<td>"
                         + "<font color='#708090'><b>"
                         + "Problem Category:"
                         + "</b></font></td><td>"+ Array[0] +"</td>" 
                 +"</tr><tr>"
                 +"<td>"
                         + "<font color='#708090'><b>"
                         + "Ticket Status:"
                         + "</b></font></td><td><u><font color="+color+">"+ Array[1] +"</font></u></td>" 
                 +"</tr><tr>"
                 +"<td>"
                         + "<font color='#708090'><b>"
                         + "Employee ID"
                         + "</b></font></td><td>"+ Array[2] +"</td>"
                 +"</tr><tr>"
                 +"<td>"
                         + "<font color='#708090'><b>"
                         + "Employee Firstname: "
                         + "</b></font></td><td>"+ Array[3] +"</td>"
                 +"</tr><tr>"
                 +"<td>"
                         + "<font color='#708090'><b>"
                         + "Employee Lastname: "
                         + "</b></font></td><td>"+ Array[4] +"</td>"
                 +"</tr><tr>"
                 +"<td>"
                         + "<font color='#708090'><b>"
                         + "Customer ID: "
                         + "</b></font></td><td>"+ Array[5] +"</td>"
                 +"</tr><tr>"
                 +"<td>"
                         + "<font color='#708090'><b>"
                         + "Customer Firstname:"
                         + "</b></font></td><td>"+ Array[6] +"</td>"
                 +"</tr><tr>"
                 +"<td>"
                         + "<font color='#708090'><b>"
                         + "Customer Lastname:"
                         + "</b></font></td><td>"+ Array[7] +"</td>" 
                 +"</tr><tr>"
                 +"<td>"
                         + "<font color='#708090'><b>"
                         + "Telephone:"
                         + "</b></font></td><td>"+ Array[8] +"</td>"
                 +"</tr><tr>"
                 +"<td>"
                         + "<font color='#708090'><b>"
                         + "Email:"
                         + "</b></font></td><td>"+ Array[9] +"</td>"
                 +"</tr><tr>"
                 +"<td>"
                         + "<font color='#708090'><b>"
                         + "Topic:"
                         + "</b></font></td><td>"+ Array[10] +"</td>"
                 +"</tr><tr>"
                 +"<td>"
                         + "<font color='#708090'><b>"
                         + "Problem:"
                         + "</b></font></td><td>"+ Array[11] +"</td>"
                 +"</tr><tr>"
                 +"<td>"
                         + "<font color='#708090'><b>"
                         + "Note:"
                         + "</b></font></td><td>"+ Array[12] +"</td>"
                 +"</tr><tr>"
                 +"<td>"
                         + "<font color='#708090'><b>"
                         + "Involved Product:"
                         + "</b></font></td><td>"+ product +"</td>"
                 +"</tr><tr>"
                 +"<td align='left'   valign='top'>"
                         + "<font color='#708090'><b>"
                         + "Solution:"
                         + "</b></font></td><td>"+ Array[13] +"</td>"
                 +"</tr>"
                 );
                 
     return builder;
 }       
     
      /**************************
      *  set string to history 
      *  editor pane with html codes 
      **************************/
         public static StringBuilder Htmlhistory (Integer ID, String changed, String name) {
         // Display the selected item
                 String [] Array = History.searchHistory(ID,changed,name);
                 for (int i = 0; i < Array.length ; i++) {
                     if (Array[i] == null || "null".equals(Array [i])) {
                         Array[i] = "";
                     }
                    
                     if (Array[2].equals("1") && Array[1].equals("CategoryID")) Array[2] = "Hardware Problem";
                     if (Array[2].equals("2") && Array[1].equals("CategoryID")) Array[2] = "Software Problem";
                     if (Array[2].equals("3") && Array[1].equals("CategoryID")) Array[2] = "Activation Problems";
                     if (Array[2].equals("4") && Array[1].equals("CategoryID")) Array[2] = "Other";
                     if (Array[2].equals("1") && Array[1].equals("StatusID")) Array[2] = "Open";
                     if (Array[2].equals("2") && Array[1].equals("StatusID")) Array[2] = "In process";
                     if (Array[2].equals("3") && Array[1].equals("StatusID")) Array[2] = "Closed";
                    
                 }
                 //html formatted text for history Editor pane
                 StringBuilder builder = new StringBuilder("<h2>Ticket ID: " + ID +"</h2> "
                 +"<table border='0'>"
                 +"<tr>"
                 +"<td width='100'>"
                         + "<font color='#708090'><b>"
                         + "Created on:"
                         + "</b></font></td><td>"+ StringtoDate(Array[0]) +"</td>"
                 +"</tr><tr>"
                 +"<td><font color='#708090'><b>"
                         + "Name:"
                         + "</b></font></td><td>"+ Array[1] +"</td>"
                 +"</tr><tr>"      
                 +"<td align='left'valign='top'>"
                         + "<font color='#708090'><b>"
                         + "Value:"
                         + "</b></font></td><td>"+ Array[2] +"</td>" 
                 +"</tr>"
                 );
     return builder;
 }
     
      /**********************************************
      *  Change dateformat for better reading 
      *  e.g. 2012-03-01 21:22:01.123 to Saturday 14. Jan 2012 10:12:21
      *****************************************************/
    public static String StringtoDate(String timestamp){
         try {
             SimpleDateFormat sdfToDate = new SimpleDateFormat(
                              "yyyy-MM-dd HH:mm:ss.SSS");
             Date date = sdfToDate.parse(timestamp);
             SimpleDateFormat sdfTranslated = new SimpleDateFormat(
                              "EEEE dd.MMM yyyy HH:mm:ss", Locale.getDefault());
             timestamp = sdfTranslated.format(date).toString();
         } catch (ParseException e) {
             Error_Frame.Error(e.toString());
         }
         return timestamp;
     }
 }
