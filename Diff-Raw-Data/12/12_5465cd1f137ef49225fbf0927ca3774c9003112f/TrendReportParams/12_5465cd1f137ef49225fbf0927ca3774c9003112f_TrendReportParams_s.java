 /*
  * TrendReportParams.java
  *
  * Created on April 8, 2005, 3:59 PM
  */
 
 package com.sun.japex.report;
 import java.util.Date;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.ArrayList;
 
 /*
  *TrendReport title reportPath outputPath date offset [driver] [test]
     where parameters are the same as the first format except:
     title -- name of the chart to be generated
     reportPath -- path to where the report directory is
     outputPath -- path where the html report will be saved. If
     date -- a specific date in format "yyyy-MM-dd", including "Today", 
             from or to which a trend report shall be made
     offset -- days, weeks, months or years from/to the above date a trend report will be created. 
             Supports format: xD where x is a positive or negative integer, and D indicates Days
             Similarily, xW, xM and xY are also support where W=Week, M=Month, and Y=Year
     driver -- name of a driver for which a trend report is to be generated. All drivers if not specified.
     test(s) -- specific test(s) in a driver for which a trend report will be created.  
             Return means if no tests specified.
   
 */
 
 public class TrendReportParams {
     static final int ARGS_TITLE = 0;
     static final int ARGS_PATH = 1;
     static final int ARGS_OUTPUTPATH = 2;
     static final int ARGS_DATE = 3;
     static final int ARGS_OFFSET = 4;
     static final int ARGS_DRIVER = 5;
     static final int ARGS_TESTCASE = 6;
     
     String _title;
     String _reportPath;
     String _outputPath;
     Date _from;
     Date _to;
     String _driver;
     boolean _isDriverSpecified = false;
     String[] _test;
     boolean _isTestSpecified = false;
     
     /** Creates a new instance of TrendReportParams */
     public TrendReportParams(String[] args) {
         _title = args[ARGS_TITLE];
         _reportPath = args[ARGS_PATH];
         _outputPath = args[ARGS_OUTPUTPATH];
         parseDates(args[ARGS_DATE], args[ARGS_OFFSET]);
                 
         try {
             if (args.length > ARGS_DRIVER) {
                 _driver = args[ARGS_DRIVER];
                 _isDriverSpecified = true;
             }
             if (args.length > ARGS_TESTCASE) {
                ArrayList<String> testcases = new ArrayList<String>();
                 for (int i=ARGS_TESTCASE; i<args.length; i++) {
                     testcases.add((String)args[i]);
                 }
                 _test = new String[(testcases.size())];
                 testcases.toArray(_test);
                 //_test = (String[])testcases.toArray();
                 _isTestSpecified = true;
             }
         } catch (Exception e) {
             e.printStackTrace();
             
         }        
     }
     
     public String title() {
         return _title;
     }
     public String reportPath() {
         return _reportPath;
     }
     public String outputPath() {
         return _outputPath;
     }
     public Date dateFrom() {
         return _from;
     }
     public Date dateTo() {
         return _to;
     }
     public String driver() {
         return _driver;
     }
     public boolean isDriverSpecified() {
         return _isDriverSpecified;
     }
     public String[] test() {
         return _test;
     }
     public boolean isTestSpecified() {
         return _isTestSpecified;
     }
     
     void parseDates(String date, String offset) {
         try {            
             Date date1 = null;
             
             if (date.toUpperCase().equals("TODAY")) {
                 date1 = new Date();
             } else {
                 DateFormat df= new SimpleDateFormat ("yyyy-MM-dd");
                 date1 = df.parse(date);
             }
             
             int n = Integer.parseInt(offset.substring(0, offset.length()-1));
             char c = offset.toUpperCase().charAt(offset.length()-1);
             Calendar cal = Calendar.getInstance();
             cal.setTime(date1);
             
             //GregorianCalendar cal = new GregorianCalendar();
             //cal.setTime(new Date());
             
             switch (c) {
                 case 68:
                     cal.add(Calendar.DATE, n);
                     break;
                 case 87:
                     cal.add(Calendar.WEEK_OF_YEAR, n);
                     break;
                 case 77:
                     cal.add(Calendar.MONTH, n);
                     break;
                 case 89:
                     cal.add(Calendar.YEAR, n);                    
             }
             
             if (n > 0) {
                 _from = date1;
                 _to = cal.getTime();
             } else {
                 _from = cal.getTime();
                 _to = date1;                
                 cal.setTime(_to);
                 cal.add(Calendar.DATE, 1);
                 _to = cal.getTime();
             }
         } catch (Exception e) {
             e.printStackTrace();
         }        
     }
 }
