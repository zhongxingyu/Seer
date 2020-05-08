 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package managers;
 
 import java.io.File;
 import java.sql.Time;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import models.Adjustment;
 import models.Adjustments;
 import models.Dates;
 import models.CompiledEmployeeData;
 import models.Constants;
 import models.DateUtil;
 import models.Employee;
 import models.EmployeeNameList;
 import models.Holiday;
 import models.Holidays;
 import models.TimeInOutData;
 import models.TimeRecord;
 import models.WeeklyTimeData;
 import utils.fileaccess.FileReader;
 
 /**
  *
  * @author jaspertomas
  */
 public class EmployeeDataManager {
     //---------------SINGLETON-------------------
 
     static EmployeeDataManager instance;
 
     private EmployeeDataManager() {
     }
 
     public static EmployeeDataManager getInstance() {
         return instance;
     }
 
     public static void initialize(JTextField txtStartDate, JTextField txtEndDate, JTextArea jTextArea) {
         if (instance == null) {
             instance = new EmployeeDataManager();
             instance.txtStartDate = txtStartDate;
             instance.txtEndDate = txtEndDate;
             instance.jTextArea = jTextArea;
         }
     }
     //---------------VARIABLES-------------------
     JTextField txtStartDate, txtEndDate;
     JTextArea jTextArea;
     ArrayList<TimeRecord> timerecords;
     //if a line contains this, it's the header - ignore it
     public static final String headerfingerprint = "APB\tJobCode\tDateTime";
     public static final Time twelve = new Time(12, 0, 0);
     public static final Time one = new Time(13, 0, 0);
     public static final Time eightthirty = new Time(8, 30, 0);
     
     //this is a string array of all employee names included in the parsed file
     EmployeeNameList employeenamelist;
     //this is a string array of all dates included in the parsed file
     //key = employee
     //value = all timerecords by a specific employee
     TreeMap<String, ArrayList<TimeRecord>> timerecordsbyemployee;
     WeeklyTimeData weeklydata;
 
     //----------------------------------
     public CompiledEmployeeData genEmployeeDataMap(ArrayList<TimeRecord> employeetimerecordlist) {
         //employeetimerecordlist contains many login time records per day, spanning many days
 
         CompiledEmployeeData compiledemployeedata = new CompiledEmployeeData();
 
         //process all records, sort by date
         //for each time record:
         TimeInOutData inoutdata;
         for (TimeRecord tr : employeetimerecordlist) {
             //if the daily record for the time record doesn't exist, create it, 
             //save the time record as a new daily record, using the recorded time as both in and out
             if (!compiledemployeedata.containsKey(tr.getDate())) {
                 inoutdata = new TimeInOutData();
                 inoutdata.setIn(tr);
                 inoutdata.setOut(tr.copy());
                 compiledemployeedata.put(tr.getDate(), inoutdata);
             } //else if the daily record exists
             //set the time as time in if it's earlier than the existing time out
             //or set the time as time out if it's later than the existing time in
             //* this works because time in is always earlier than time out
             else {
                 inoutdata = compiledemployeedata.get(tr.getDate());
                 if (inoutdata.getIn().islaterthan(tr)) {
                     inoutdata.setIn(tr);
                 } else if (inoutdata.getOut().isearlierthan(tr)) {
                     inoutdata.setOut(tr);
                 }
             }
         }
         compiledemployeedata = enforceTimeInCeiling(compiledemployeedata);
         return compiledemployeedata;
     }
 
     public CompiledEmployeeData enforceTimeInCeiling(CompiledEmployeeData compiledemployeedata) {
         Time time;
         for (TimeInOutData edata2 : compiledemployeedata.values()) {
             //adjust in time
             time = edata2.getIn().getTime();
             //adjust in time to 8:30 if earlier than 8:30
             if (time.before(eightthirty)) {
                 edata2.getIn().setTime(eightthirty);
             } else //adjust in time to 1:00 if between 12 and 1
             if (time.after(twelve)
                     && time.before(one)) {
                 edata2.getIn().setTime(one);
             }
             //adjust out time to 12:00 if between 12 and 1
             time = edata2.getOut().getTime();
             if (time.after(twelve)
                     && time.before(one)) {
                 edata2.getOut().setTime(twelve);
             }
         }
         return compiledemployeedata;
     }
 
     public void createNameListAndCalendar() {
         //create employee name list
         // and date list
         employeenamelist = new EmployeeNameList();
         ArrayList<String> dates=Dates.getInstance().getItems();
         for (TimeRecord timerecord : timerecords) {
             if (!employeenamelist.contains(timerecord.getName())) {
                 employeenamelist.add(timerecord.getName());
             }
             if (!dates.contains(timerecord.getDate())) {
                 dates.add(timerecord.getDate());
             }
         }
         Collections.sort(employeenamelist);
         
         //set date textboxes according to the date range in the calendar
         txtStartDate.setText(dates.get(0));
         txtEndDate.setText(dates.get(dates.size() - 1));
     }
 
 //    public void printSampleOutput() {
 //        
 //        ArrayList<String> dates=Dates.getInstance().getItems();
 //
 //        //clear the textarea
 //        jTextArea.setText("");
 //
 //        //sample output
 //        CompiledEmployeeData edatamap;
 //        TimeInOutData data;
 //        for (String name : employeenamelist) {
 //            for (String date : dates) {
 //                edatamap = weeklydata.get(name);
 //                if (edatamap == null) {
 //                    continue;
 //                }
 //                data = edatamap.get(date);
 //                if (data == null) {
 //                    continue;
 //                }
 //
 //                jTextArea.append(name + "\t");
 //                jTextArea.append(date + "\t");
 //                if (data.getIn().getTime().equals(one)) {
 //                    jTextArea.append(
 //                            ""
 //                            + "\t"
 //                            + ""
 //                            + "\t"
 //                            + data.getInTimeString()
 //                            + "\t"
 //                            + data.getOutTimeString()
 //                            + "\r\n");
 //                } else if (data.getOut().getTime().equals(twelve)) {
 //                    jTextArea.append(
 //                            data.getInTimeString()
 //                            + "\t"
 //                            + data.getOutTimeString()
 //                            + ""
 //                            + "\t"
 //                            + ""
 //                            + "\t"
 //                            + "\r\n");
 //                } else {
 //                    jTextArea.append(
 //                            data.getInTimeString()
 //                            + "\t"
 //                            + "1200"
 //                            + "\t"
 //                            + "1300"
 //                            + "\t"
 //                            + data.getOutTimeString()
 //                            + "\r\n");
 //                }
 //
 //            }
 //        }
 //    }
 
     public void printPayrollOutput() {
         Double regularholidayratenowork=100d;//(percent)
         Double regularholidayratewithwork=200d-100;//(percent)
         Double specialholidayratenowork=0d;//(percent)
         Double specialholidayratewithwork=130d-100;//(percent)
         
         
         
         DecimalFormat format = new DecimalFormat(",##0.00");
         
         //clear the textarea
         jTextArea.setText("");
         String tempstring="";//,prefix1="",prefix2="",problemdatestring="";
         ArrayList<String> problemdates=new ArrayList<String>();
         ArrayList<String> dates=Dates.getInstance().getItems();
 //        Employee e;
         
         //sample output
         
         CompiledEmployeeData edatamap=null;
         TimeInOutData data;
         Double regularrate,overtimerate,cola,regularpay,overtimepay,grosspay,totalcola,deductions,netpay;
         Integer days,regularminutes=0,overtimeminutes=0,totalregularminutes=0,totalovertimeminutes=0,diffminutes;
         Holiday holiday=null;
         Double holidaybonus=0d;
 
 //        for (String name : employeenamelist) 
         for(Employee e:EmployeeFileManager.getInstance().getEmployees())
         {
             String name=e.getNickname();
 //            prefix1=prefix2=problemdatestring=
             problemdates.clear();
 
             totalregularminutes=0;
             totalovertimeminutes=0;
 
             holidaybonus=0d;
                         
 //            e=EmployeeFileManager.getInstance().getEmployees().getByNickname(name);
             if(e==null)
             {
                 tempstring="Employee with nickname \""+name+"\" removed from Employee Data"+"\r\n";
                 tempstring+="Skipping..."+"\r\n";
                 tempstring+="==============================\r\n";
                 jTextArea.append(tempstring);
                 continue;
             }
             
             String ename=e.getLname()+", "+e.getFname()+" "+e.getMname();
             if(ename.trim().contentEquals(","))
                 tempstring="(Employee name not set)"+"\r\n";
             else
                 tempstring=ename+"\r\n";
             tempstring+="- - - - - - - - - - - - - - - \r\n";
             
             //calculate total regular minutes and total overtime minutes
             edatamap = weeklydata.get(name);
             if (edatamap == null) {
                 continue;
             }
             for (String date : dates) 
             {
                 //fetch employee attendance data
                 data = edatamap.get(date);
 
                 //process holiday bonus first
                 try {
                     holiday=Holidays.getInstance().getByDateString(date);
                 } catch (ParseException ex) {
                     ex.printStackTrace();
                     JOptionPane.showMessageDialog(null, "Error","Error processing holiday data",JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 //if holiday, 
                 if(holiday!=null)
                 {
 //                    Double regularholidayratenowork=100d;//(percent)
 //                    Double regularholidayratewithwork=200d;//(percent)
 //                    Double specialholidayratenowork=0d;//(percent)
 //                    Double specialholidayratewithwork=130d;//(percent)
                     
                     //add holiday bonus
                     if(holiday.getType()==Holidays.REGULAR)
                     {
                         tempstring+="Regular Holiday: "+holiday.getName()+"\n";
                         //if absent
                         if (data == null) {
 //                            regularholidayratenowork
                             Double holidaybonusrate=regularholidayratenowork;
                             
                             Double holidayregularrate=e.getMonthlySalary();
 //                            overtimerate=e.getMonthlySalary()*Constants.overtimemultiplier;
                             Double holidayregularpay=holidayregularrate*holidaybonusrate/100;
 //                            overtimepay=overtimerate*totalovertimeminutes/60/8;
                             holidaybonus+=holidayregularpay;
                             tempstring+="Holiday Additional (Absent): P "+format.format(holidayregularpay)+"\r\n";
                             
                         }
                         else
                         {
 //                              regularholidayratewithwork      
                             Double holidaybonusrate=regularholidayratewithwork;
                             
                             Double holidayregularrate=e.getMonthlySalary();
 //                            overtimerate=e.getMonthlySalary()*Constants.overtimemultiplier;
                             Double holidayregularpay=holidayregularrate*holidaybonusrate/100;
 //                            overtimepay=overtimerate*totalovertimeminutes/60/8;
                             holidaybonus+=holidayregularpay;
                             tempstring+="Holiday Additional: P "+format.format(holidayregularpay)+"\r\n";
                             
                         }
                     }
                     else if(holiday.getType()==Holidays.SPECIAL)
                     {
                         tempstring+="Special Non-working Holiday: "+holiday.getName()+"\n";
                         //if absent
                         if (data == null) {
 //                            specialholidayratenowork
                             Double holidaybonusrate=specialholidayratenowork;
                             
                             Double holidayregularrate=e.getMonthlySalary();
 //                            overtimerate=e.getMonthlySalary()*Constants.overtimemultiplier;
                             Double holidayregularpay=holidayregularrate*holidaybonusrate/100;
 //                            overtimepay=overtimerate*totalovertimeminutes/60/8;
                             holidaybonus+=holidayregularpay;
                             tempstring+="Holiday Additional (Absent): P "+format.format(holidayregularpay)+"\r\n";
                         }
                         else
                         {
 //                            specialholidayratewithwork
                             Double holidaybonusrate=specialholidayratewithwork;
                             
                             Double holidayregularrate=e.getMonthlySalary();
 //                            overtimerate=e.getMonthlySalary()*Constants.overtimemultiplier;
                             Double holidayregularpay=holidayregularrate*holidaybonusrate/100;
 //                            overtimepay=overtimerate*totalovertimeminutes/60/8;
                             holidaybonus+=holidayregularpay;
                             tempstring+="Holiday Additional: P "+format.format(holidayregularpay)+"\r\n";
 
                             
                         }
                     }
                     
                     
                 }
                 
                 //if absent, do nothing
                 if (data == null) {
                     continue;
                 }
 
                 if(name.trim().isEmpty())
                     tempstring+="(nickname not set)" + "\t";
                 else
                     tempstring+=name + "\t";
                 tempstring+=date + "\t";
                 
                 
                 //if missing time in or missing time out
                 if (data.getIn().getTime().equals(data.getOut().getTime())) {
 //                    prefix1="Error: Missing time in or time out: ";
 //                    prefix2="\nPlease go to the Manage Employee Data page to make corrections\n";
                     problemdates.add(date);
                     
 //                    if(data.getIn().getTime().after(one))
                         tempstring+=
                                 "Missing time in or time out. Fingerprint login at "+data.getPrettyInTimeString()
                                 + "\t"
                                 + ""
                                 + "\t"
                                 + ""
                                 + "\t"
                                 + ""
                                 + "\r\n";
 //                        tempstring+=
 //                                ""
 //                                + "\t"
 //                                + ""
 //                                + "\t"
 //                                + data.getOutTimeString()
 //                                + "\t"
 //                                + data.getOutTimeString()
 //                                + "\r\n";
 //                    else
 //                        tempstring+=
 //                                data.getOutTimeString()
 //                                + "\t"
 //                                + data.getOutTimeString()
 //                                + "\t"
 //                                + ""
 //                                + "\t"
 //                                + ""
 //                                + "\r\n";
                 } else                       
                 //came to work at lunch break or after
                 if (data.getIn().getTime().equals(one) || data.getIn().getTime().after(one)) {
                     tempstring+=
                             ""
                             + "\t"
                             + ""
                             + "\t"
                             + data.getPrettyInTimeString()
                             + "\t"
                             + data.getPrettyOutTimeString()
                             + "\r\n";
                 //left work at lunch break
                 } else if (data.getOut().getTime().equals(twelve) || data.getOut().getTime().before(twelve)) {
                     tempstring+=
                             data.getPrettyInTimeString()
                             + "\t"
                             + data.getPrettyOutTimeString()
                             + ""
                             + "\t"
                             + ""
                             + "\t"
                             + "\r\n";
                 } else {
                     tempstring+=
                             data.getPrettyInTimeString()
                             + "\t"
 //                            + "1200"
                             + "12:00 PM"
                             + "\t"
 //                            + "1300"
                             + "01:00 PM"
                             + "\t"
                             + data.getPrettyOutTimeString()
                             + "\r\n";
                 }
                 
                 //calculate regular and overtime minutes
                 diffminutes=data.getTimeDiffMinutes();
                 if(diffminutes>480)//8 hours*60 min
                 {
                     regularminutes=480;
                     overtimeminutes=diffminutes-480;
                 }
                 else
                 {
                     regularminutes=diffminutes;
                     overtimeminutes=0;
                 }
                 totalregularminutes+=regularminutes;
                 totalovertimeminutes+=overtimeminutes;
             }
 
             //get regular rate and overtime rate
             regularrate=e.getMonthlySalary();
             overtimerate=e.getMonthlySalary()*Constants.overtimemultiplier;
             regularpay=regularrate*totalregularminutes/60/8;
             overtimepay=overtimerate*totalovertimeminutes/60/8;
             grosspay=regularpay+overtimepay;
             cola=e.getCola();
             totalcola=cola*edatamap.size();
             deductions=e.getDeduction();
             netpay=grosspay+totalcola-deductions+holidaybonus;
             
             tempstring+="- - - - - - - - - - - - - - - \r\n";
             tempstring+="Regular Minutes: "+totalregularminutes+" minutes ("+totalregularminutes/60+" hours "+totalregularminutes%60+" minutes)\r\n";
             tempstring+="Regular Rate: P "+format.format(regularrate)+"/day\n";
             tempstring+="Regular Pay: P "+format.format(regularpay)+"\r\n";
             tempstring+="Overtime Minutes: "+totalovertimeminutes+" minutes ("+totalovertimeminutes/60+" hours "+totalovertimeminutes%60+" minutes)\r\n";
             tempstring+="Overtime Rate: P "+format.format(overtimerate)+"/day\n";
             tempstring+="Overtime Pay: P "+format.format(overtimepay)+"\r\n";
             tempstring+="Gross Salary: P "+format.format(grosspay)+"\r\n";
             tempstring+="Total COLA: P "+format.format(totalcola)+"\r\n";
 //            if(holidaybonus!=0)
                 tempstring+="Holiday Additional: P "+format.format(holidaybonus)+"\r\n";
             tempstring+="Deductions: P "+format.format(deductions)+"\r\n";
             tempstring+="- - - - - - - - - - - - - - - \r\n";
             tempstring+="NET SALARY: PHP "+format.format(netpay) +"\r\n";
             tempstring+="==============================\n\n";
 
 //            for(int i=0;i<problemdates.size();i++)
 //            {
 //                if(i!=0)problemdatestring+=", ";
 //                problemdatestring+=problemdates.get(i);
 //            }
 //            problemdatestring=prefix1+problemdatestring+prefix2;
 //            jTextArea.append(problemdatestring+tempstring);
             jTextArea.append(tempstring);
             jTextArea.moveCaretPosition(0);
         }
     }
     public void calculate(File file) {
 
         //parse file and create records list
         parseFile(file);
 
         //arrange time records into arraylists by employee
         groupTimeRecordsByEmployee();
 
         createNameListAndCalendar();
         
         EmployeeFileManager.getInstance().generateFromStringArray(employeenamelist);
 
         //start to process data into array structure
         weeklydata = new WeeklyTimeData();
         for (ArrayList<TimeRecord> timerecordlist : timerecordsbyemployee.values()) {
             CompiledEmployeeData edatamap = genEmployeeDataMap(timerecordlist);
             weeklydata.put(timerecordlist.get(0).getName(), edatamap);
         }
         
         applyAdjustments();
 
         printPayrollOutput();
     }
 
     public void recalculate(File file) {
 
         //parse file and create records list
         parseFile(file);
 
         //remove calendar entries that do not fit between startdate and enddate
         ArrayList<TimeRecord> temp = new ArrayList<TimeRecord>();
         for (TimeRecord timerecord : timerecords) {
             if (DateUtil.isEarlierThan(timerecord.getDate(), txtStartDate.getText())
                     || DateUtil.isLaterThan(timerecord.getDate(), txtEndDate.getText())) {
                 temp.add(timerecord);
             }
         }
         for (TimeRecord timerecord : temp) {
             timerecords.remove(timerecord);
         }
 
         //arrange time records into arraylists by employee
         groupTimeRecordsByEmployee();
 
         createNameListAndCalendar();
 
 
         //start to process data into array structure
         weeklydata = new WeeklyTimeData();
         for (ArrayList<TimeRecord> timerecordlist : timerecordsbyemployee.values()) 
         {
             CompiledEmployeeData edatamap = genEmployeeDataMap(timerecordlist);
             if(edatamap!=null)
                 weeklydata.put(timerecordlist.get(0).getName(), edatamap);
         }
         
         applyAdjustments();
         
         printPayrollOutput();
     }
     
     private void applyAdjustments()
     {
         for(Adjustment a:Adjustments.getInstance().getItems())
         {
             CompiledEmployeeData edatamap = weeklydata.get(a.getEmployeeNickname());
            if(edatamap==null)continue;
             TimeInOutData data = edatamap.get(Holidays.dateFormat.format(a.getDate()));
             if(data==null)continue;
             if(a.getType()==Adjustment.IN)
                 data.getIn().setTime(a.getTime());
             else if(a.getType()==Adjustment.OUT)
                 data.getOut().setTime(a.getTime());
         }    
     }
 
     private void parseFile(File file) {
         //extract file contents
         String filecontents = FileReader.read(file.getPath());
 
         //remove double spaces and null characters
         filecontents = filecontents.replace("\0", "");
         filecontents = filecontents.replace("\n\n", "\r\n");
 
         //split into lines
         String[] lines = filecontents.split("\r\n");
 
         //convert lines into objects
         timerecords = new ArrayList<TimeRecord>();
         for (String line : lines) {
             //ignore the header
             if (line.contains(headerfingerprint)) {
                 continue;
             } //ignore empty lines
             else if (line.trim().length() == 0) {
                 continue;
             } else {
                 timerecords.add(new TimeRecord(line));
             }
         }
     }
 
     private void groupTimeRecordsByEmployee() {
         //group by date
         ArrayList<TimeRecord> dailyrecordlist;
         timerecordsbyemployee = new TreeMap<String, ArrayList<TimeRecord>>();
         for (TimeRecord record : timerecords) {
             if (!timerecordsbyemployee.containsKey(record.getName())) {
                 dailyrecordlist = new ArrayList<TimeRecord>();
                 dailyrecordlist.add(record);
                 timerecordsbyemployee.put(record.getName(), dailyrecordlist);
             } else {
                 dailyrecordlist = timerecordsbyemployee.get(record.getName());
                 dailyrecordlist.add(record);
             }
 
         }
     }
 //    private void groupTimeRecordsByDate()
 //    {
 //        //group by date
 //        ArrayList<TimeRecord> dailyrecordlist; 
 //        parseresult=new TreeMap<String,ArrayList<TimeRecord>>();
 //        for(TimeRecord record:timerecords)
 //        {
 //            if(!parseresult.containsKey(record.getDate()))
 //            {
 //                dailyrecordlist=new ArrayList<TimeRecord>();
 //                dailyrecordlist.add(record);
 //                parseresult.put(record.getDate(), dailyrecordlist);
 //            }
 //            else
 //            {
 //                dailyrecordlist=parseresult.get(record.getDate());
 //                dailyrecordlist.add(record);
 ////                    datesmap.put(record.getDate(), dailyrecords);
 //            }
 //
 //        }
 //    }
 
     public boolean validateDates() {
         if (txtStartDate.getText().trim().isEmpty()) {
             JOptionPane.showMessageDialog(null, "Start Date cannot be empty", "Error", JOptionPane.PLAIN_MESSAGE);
             return false;
         }
         if (txtEndDate.getText().trim().isEmpty()) {
             JOptionPane.showMessageDialog(null, "End Date cannot be empty", "Error", JOptionPane.PLAIN_MESSAGE);
             return false;
         }
 
         String[] segments = txtStartDate.getText().trim().replace("-", "/").split("/");
         if (segments.length != 3) {
             JOptionPane.showMessageDialog(null, "Start Date is invalid", "Error", JOptionPane.PLAIN_MESSAGE);
             return false;
         }
         for (int i = 0; i < 3; i++) {
             segments[i] = String.format("%02d", Integer.valueOf(segments[i]));
         }
         txtStartDate.setText(segments[0] + "/" + segments[1] + "/" + segments[2]);
 
         segments = txtEndDate.getText().trim().replace("-", "/").split("/");
         if (segments.length != 3) {
             JOptionPane.showMessageDialog(null, "End Date is invalid", "Error", JOptionPane.PLAIN_MESSAGE);
             return false;
         }
         for (int i = 0; i < 3; i++) {
             segments[i] = String.format("%02d", Integer.valueOf(segments[i]));
         }
         txtEndDate.setText(segments[0] + "/" + segments[1] + "/" + segments[2]);
 
         if (DateUtil.isEarlierThan(txtEndDate.getText(), txtStartDate.getText())) {
             String temp = txtStartDate.getText();
             txtStartDate.setText(txtEndDate.getText());
             txtEndDate.setText(temp);
         }
         return true;
     }
     public String getPayrollText() 
     {
         String string="";
         for(Employee e:EmployeeFileManager.getInstance().getEmployees())
         {
             string+=e.getFullName()+"\r\n";
 //            string+="reg hours rh"++"\r\n";
 //            string+="reg rate"++"\r\n";
 //            string+="reg pay "++"\r\n";
 //            string+="overtime hours oh"++"\r\n";
 //            string+="overtime rate"++"\r\n";
 //            string+="overtime pay"++"\r\n";
 //            string+="holiday pay"++"\r\n";
 //            string+="gross pay"++"\r\n";
 //            string+="cola"++"\r\n";
 //            string+="deductions"++"\r\n";
 //            string+="net salary"++"\r\n";
             /*
 reg hours rh
 reg rate
 reg pay 
 overtime hours oh
 overtime rate
 overtime pay
 holiday pay
 gross pay
 cola
 deductions
 net salary             
              */
             
             
             string+="\n\n----------------------------------";
         }
         return string;
     }
 
     public WeeklyTimeData getWeeklydata() {
         return weeklydata;
     }
 
     
 
 }
