 package ua.edu.tntu.schedule;
 
 import android.content.Context;
 import android.content.res.XmlResourceParser;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import ua.edu.tntu.R;
 
 public class ScheduleXMLResourceParser {
 
     //  variables that replate
     private Context context;
     private String groupName;
     private String subGroup;
     private int event;
     private boolean switchSubGroup;
 
     //  variables that relate to field ScheduleBlock class
     private String nameLecture;
     private String nameDay;
     private String nameLocation;
     private String nameStsrtTime;
     private String nameTimeEnd;
 
     //  variables that relate to Tag XML
     private String nameTextTag;
     private String nameWeek;
     private String nameStartTag;
     private ArrayList<ScheduleBlock> scheduleBlockContainer;
     private ScheduleBlock item;
 
     private boolean[] flagSwitchWeek = new boolean[2];
 
     boolean flagTempGroup;
     boolean flagTempSubGroup;
     boolean flagTempWeek;
     boolean flagCancelWeek_1;
     boolean flagCancelWeek_2;
     boolean flagSwitchGroup;
     boolean flagSwitchSubGroup;
     boolean flagCloseSwitchSubGroup;
 
     private XmlResourceParser parser;
 
     // constructor for  to get the context object from where you are using this plist parsing
     public ScheduleXMLResourceParser(Context context, String groupName, boolean switchSubGroup) {
         this.context = context;
         this.groupName = groupName;
         this.switchSubGroup = switchSubGroup;
 
         if (switchSubGroup) {
             subGroup = " 1\n";
         } else {
             subGroup = " 2\n";
         }
 
         this.groupName = " " + this.groupName + "\n";
         flagSwitchWeek[0] = false;
         flagSwitchWeek[1] = false;
 
         flagTempGroup = false;
         flagTempSubGroup = false;
         flagTempWeek = false;
         flagCancelWeek_1 = false;
         flagCancelWeek_2 = false;
         flagSwitchGroup = false;
         flagSwitchSubGroup = false;
         flagCloseSwitchSubGroup = false;
 
         scheduleBlockContainer = new ArrayList<ScheduleBlock>();
 
         parser = this.context.getResources()
                 .getXml(R.xml.schedule);
     }
 
     public ArrayList<ScheduleBlock> getSchedule() {
 
         // specifying the  your plist file.And Xml ResourceParser is an event type parser for more details Read android source
 
         try {
             event = parser.getEventType();
 
             while (event != parser.END_DOCUMENT) {
 
                 if (event == XmlResourceParser.START_TAG) {
                     if (parser.getName().equals("group")) {
                         flagTempGroup = true;
                     }
                     if (parser.getName().equals("subgroup") && flagSwitchGroup) {
                         flagTempSubGroup = true;
                     }
                     if (parser.getName().equals("week") && flagSwitchSubGroup) {
                         flagTempWeek = true;
                     }
                     if (parser.getName().equals("day") && flagSwitchSubGroup) {
                         nameStartTag = parser.getName();
                     }
                     if (parser.getName().equals("lecture") && flagSwitchSubGroup) {
                         nameStartTag = parser.getName();
                     }
                     if (parser.getName().equals("startTime") && flagSwitchSubGroup) {
                         nameStartTag = parser.getName();
                     }
                     if (parser.getName().equals("endTime") && flagSwitchSubGroup) {
                         nameStartTag = parser.getName();
                     }
                     if (parser.getName().equals("location") && flagSwitchSubGroup) {
                         nameStartTag = parser.getName();
                     }
                 }
 
                 if (event == XmlResourceParser.TEXT) {
                     nameTextTag = parser.getText();
                     if (flagTempGroup && nameTextTag.equals(groupName)) {
                         flagSwitchGroup = true;
                     }
                     if (flagTempSubGroup && nameTextTag.equals(subGroup)) {
                         flagSwitchSubGroup = true;
                         flagCloseSwitchSubGroup = true;
                     }
 
                     if (flagTempWeek && nameTextTag.equals(" first\n")) {
                         nameWeek = nameTextTag;
                         flagSwitchWeek[0] = true;
                         flagTempWeek = false;
                     }
                     if (flagTempWeek && nameTextTag.equals(" second\n")) {
                         nameWeek = nameTextTag;
                         flagSwitchWeek[1] = true;
                         scheduleBlockContainer.add(new ScheduleBlock());
                         flagTempWeek = false;
                     }
                     if (flagSwitchGroup && flagSwitchSubGroup) {
                         if (flagSwitchWeek[0]) {
                             if (flagCancelWeek_1 == true) {
                                 item = new ScheduleBlock();
                                 if (nameStartTag.equals("day") && flagSwitchSubGroup) {
                                     item.setNameOfDay(parser.getText());
                                     scheduleBlockContainer.add(item);
                                 } else {
                                     if (nameStartTag.equals("lecture") && flagSwitchSubGroup) {
                                         nameLecture = parser.getText();
                                     }
                                     if (nameStartTag.equals("startTime") && flagSwitchSubGroup) {
                                         nameStsrtTime = parser.getText();
                                     }
                                     if (nameStartTag.equals("endTime") && flagSwitchSubGroup) {
                                         nameTimeEnd = parser.getText();
                                     }
                                     if (nameStartTag.equals("location") && flagSwitchSubGroup) {
                                         nameLocation = parser.getText();
                                         item.setLocation(nameLocation);
                                         item.setLecture(nameLecture);
                                         item.setTimeBegin(nameStsrtTime);
                                         item.setTimeEnd(nameTimeEnd);
                                         scheduleBlockContainer.add(item);
                                     }
                                 }
                             }
                             flagCancelWeek_1 = true;
                         }
                         if (flagSwitchWeek[1]) {
                             if (flagCancelWeek_2 == true) {
                                 item = new ScheduleBlock();
                                 if (nameStartTag.equals("day") && flagSwitchSubGroup) {
                                     item.setNameOfDay(parser.getText());
                                     scheduleBlockContainer.add(item);
                                 } else {
                                     if (nameStartTag.equals("lecture") && flagSwitchSubGroup) {
                                         nameLecture = parser.getText();
                                     }
                                     if (nameStartTag.equals("startTime") && flagSwitchSubGroup) {
                                         nameStsrtTime = parser.getText();
                                     }
                                     if (nameStartTag.equals("endTime") && flagSwitchSubGroup) {
                                         nameTimeEnd = parser.getText();
                                     }
                                     if (nameStartTag.equals("location") && flagSwitchSubGroup) {
                                         nameLocation = parser.getText();
                                         item.setLocation(nameLocation);
                                         item.setLecture(nameLecture);
                                         item.setTimeBegin(nameStsrtTime);
                                         item.setTimeEnd(nameTimeEnd);
                                         scheduleBlockContainer.add(item);
                                     }
                                 }
                             }
                             flagCancelWeek_2 = true;
                         }
                     }
                 }
                 if (event == XmlResourceParser.END_TAG && flagCloseSwitchSubGroup) {
                     if (parser.getName().equals("subgroup")) {
                         return scheduleBlockContainer;
                     }
                     if (parser.getName().equals("week")) {
                         if (nameWeek.equals(" first\n")) {
                             flagSwitchWeek[0] = false;
                         }
                         if (nameWeek.equals(" second\n")) {
                             flagSwitchWeek[1] = false;
                         }
                     }
                 }
                 event = parser.next();
 
             }
 
         } catch (XmlPullParserException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return scheduleBlockContainer;
     }
 }
