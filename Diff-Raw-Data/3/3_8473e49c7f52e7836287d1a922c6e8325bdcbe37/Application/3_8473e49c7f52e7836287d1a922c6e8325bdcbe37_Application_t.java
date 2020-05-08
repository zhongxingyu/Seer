 /*
  * Copyright (C) 2013 Project-Phoenix
  * 
  * This file is part of Website.
  * 
  * Website is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * Website is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Website.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package controllers;
 
 
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.core.MediaType;
 
 import org.joda.time.DateTimeConstants;
 import org.joda.time.LocalTime;
 
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import play.mvc.Result;
 import views.html.*;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 import de.phoenix.rs.EntityUtil;
 import de.phoenix.rs.PhoenixClient;
 import de.phoenix.rs.entity.PhoenixAttachment;
 import de.phoenix.rs.entity.PhoenixDetails;
 import de.phoenix.rs.entity.PhoenixLecture;
 import de.phoenix.rs.entity.PhoenixLectureGroup;
 import de.phoenix.rs.entity.PhoenixLectureGroupTaskSheet;
 import de.phoenix.rs.entity.PhoenixSubmission;
 import de.phoenix.rs.entity.PhoenixTask;
 import de.phoenix.rs.entity.PhoenixTaskSheet;
 import de.phoenix.rs.entity.PhoenixText;
 import de.phoenix.rs.key.ConnectionEntity;
 import de.phoenix.rs.key.KeyReader;
 import de.phoenix.rs.key.SelectAllEntity;
 import de.phoenix.rs.key.SelectEntity;
 
 
 
 /**
  *  This class handles all the functionality used by the frontend.
  * @author Markus W.<br>Matthias E.
  * 
  */
 public class Application extends Controller {
     
     private final static String BASE_URI = "http://meldanor.dyndns.org:8080/PhoenixWebService/rest";
     private final static Client CLIENT = PhoenixClient.create();
     /**
      * Displays the home page
      * @return play.mvc.Results.Status
      */
     
     public static Result home() {
         return ok(home.render("Home"));
     }
     
     public static Result createTask() {
         return ok(createTask.render("Create Task"));
     }
     
     public static Result sendTask() {
         MultipartFormData form = request().body().asMultipartFormData();
         
         ArrayList<PhoenixAttachment> attachmentLst = new ArrayList<PhoenixAttachment>();
         ArrayList<PhoenixText> patternLst = new ArrayList<PhoenixText>();
         System.out.println(form.getFiles());
         for (FilePart fp : form.getFiles()) {
             try {
                 if (!fp.getFilename().trim().equals(""))
                     if (fp.getKey().equals("binary")) 
                         attachmentLst.add(new PhoenixAttachment(fp.getFile(), fp.getFilename())); 
                     else if (fp.getKey().equals("pattern")) 
                         patternLst.add(new PhoenixText(fp.getFile(), fp.getFilename()));   
             } catch (IOException e) {
                 return ok(stringShower.render("ERROR", e.toString()));
             }
         }  
 
         WebResource wr = CLIENT.resource(BASE_URI).path(PhoenixTask.WEB_RESOURCE_ROOT).path(PhoenixTask.WEB_RESOURCE_CREATE);
                 
         PhoenixTask task = new PhoenixTask(attachmentLst,patternLst, Form.form().bindFromRequest().get("description"), Form.form().bindFromRequest().get("title"));
         ClientResponse post = wr.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, task);
         System.out.println("CreateTask Status: "+post.getStatus());
         if(post.getStatus() == 200){
             return ok(stringShower.render("Task created", "Task has been created successfully"));
         }else{
             return ok(stringShower.render("send Lecture", "Ups, da ist ein Fehler aufgetreten!(" + post.getStatus() + ")"));
         }
     }
       
     public static Result showTasks() {
         if (request().queryString().get("option") != null)
             if (request().queryString().get("option")[0].equals("all"))
                 return ok(showTasks.render("showTasks", getAllTasks()));
             else
                 return ok(showTasks.render("showTasks", Arrays.asList(getTaskByTitle(request().queryString().get("option")[0]))));
         return ok();
     }
     
     public static Result showSubmissions() {
         Map<String, String> dataMap = Form.form().bindFromRequest().data();
         String taskTitle = dataMap.get("task");
         
         SelectEntity<PhoenixTask> taskSelector = new SelectEntity<PhoenixTask>();
         taskSelector.addKey("title", taskTitle);
         
         WebResource wrGetSubmissions = PhoenixSubmission.getResource(CLIENT, BASE_URI);
         
         SelectEntity<PhoenixSubmission> submissionSelector = new SelectEntity<PhoenixSubmission>();
         submissionSelector.addKey("task", taskSelector);
         
         ClientResponse post = wrGetSubmissions.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, submissionSelector);
         
         if (post.getStatus() == 200){
            List<PhoenixSubmission> submissions = EntityUtil.extractEntityList(post);
             return ok(showSubmissions.render("showSubmissions", submissions));
         }else{
             return ok(stringShower.render("show Submissions", "Ups, da ist ein Fehler aufgetreten!(" + post.getStatus() + ")"));
         }
     }
 
     public static List<PhoenixTask> getAllTasks(){        
         WebResource wr = PhoenixTask.getResource(CLIENT, BASE_URI);
         ClientResponse resp = wr.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, new SelectAllEntity<PhoenixTask>());
         List<PhoenixTask> taskList = EntityUtil.extractEntityList(resp);
         return taskList;     
     }
     
     public static PhoenixTask getTaskByTitle(String title) {
         WebResource wr = PhoenixTask.getResource(CLIENT, BASE_URI);
         SelectEntity<PhoenixTask> selectByTitle = new SelectEntity<PhoenixTask>().addKey("title", title);
         ClientResponse post = wr.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, selectByTitle);
         PhoenixTask task = EntityUtil.extractEntity(post);
         return task;
     }
     
 
     public static Result createLecture() {
         return ok(createLecture.render("Create Lecture"));
     }
     
     
     public static Result sendLecture() {        
         //Arrays to get the inputs form CreateLecture
         String[] keyStrings = new String[] {"title", "room", "day", "startHours","startMinutes", "endHours", 
                                             "endMinutes", "period", "startYear", "startMonth", "startDay",
                                             "endYear", "endMonth", "endDay"};
         String[] keyStrings2 = new String[] {"room2", "day2", "startHours2","startMinutes2", "endHours2", 
                                             "endMinutes2", "period2", "startYear2", "startMonth2", "startDay2",
                                             "endYear2", "endMonth2", "endDay2"};
         String[] keyStrings3 = new String[] {"room3", "day3", "startHours3","startMinutes3", "endHours3", 
                                             "endMinutes3", "period3", "startYear3", "startMonth3", "startDay3",
                                             "endYear3", "endMonth3", "endDay3"};
         String[][] details = new String[][] {keyStrings, keyStrings2, keyStrings3};
         //Arrays, which will be filled with the requeststrings
         String[][] requests = new String[3][13];
         WebResource ws = CLIENT.resource(BASE_URI).path(PhoenixLecture.WEB_RESOURCE_ROOT).path(PhoenixLecture.WEB_RESOURCE_CREATE);
         
         String title = "";
         //if input is missing don't create a detail later
         boolean[] wrongInput = new boolean[] {false,false,false};
         int itemCount = 0;
         int arrayCount = 0;
         for(String[] stringArray: details){   
             // TODO: exception handling
             // Get the requests and if something missing, set wronginput[arrayCount] to true and test the next detail
             for(String item: stringArray){
                 String temp = Form.form().bindFromRequest().get(item);
                 if ((Form.form().bindFromRequest().get("box1") == null) && (arrayCount == 1) ||(Form.form().bindFromRequest().get("box2") == null) && (arrayCount == 2)){
                     wrongInput[arrayCount] = true;         //TODO: if (wrongInput == true) throw IOException;
                     break;                
                 }
                 //if everything's filled in, set title and put the rest in the requestarrays
                 else {
                     if (itemCount == 0) {
                         title = temp;
                         itemCount++;
                     }
                     else{
                         requests[arrayCount][itemCount-1] = temp;
                         itemCount++;               
                     }
                 }
             }
             //just one title so start next loop at itemcount 1
             itemCount = 1;
             arrayCount++;
         } 
         //PhoenixDetaillist
         List<PhoenixDetails> allDetails = new ArrayList<PhoenixDetails>();
         int boolIndex = 0;
         for(String[] item: requests){
             //only create LectureCheck if detail is complete
             if(!wrongInput[boolIndex]){
                 LectureCheck lectureCheck = new LectureCheck(item);
                 //add it to allDetails
                 allDetails.add(lectureCheck.getPhoenixDetails());
             }
             boolIndex++;
         }
         //send it to server
         PhoenixLecture lecture = new PhoenixLecture(title, allDetails);
         ClientResponse response = ws.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, lecture);
         if(response.getStatus() == 200){
             return ok(stringShower.render("strings to show", "Good News!"));
         }else{
             return ok(stringShower.render("send Lecture", "Ups, da ist ein Fehler aufgetreten!(" + response.getStatus() + ")"));
         }
     }
     
     public static Result addGroup() {
         return ok(addGroup.render("add Group"));
     }
 
     public static Result sendGroup() {        
         //Array to get the inputs form addGroup
         String[] keyStrings = new String[] {"title","lecture", "size", "room", "day", "startHours","startMinutes", "endHours", 
                                             "endMinutes", "period", "startYear", "startMonth", "startDay",
                                             "endYear", "endMonth", "endDay", "submitDay", "submitHours", "submitMinutes"};
 
         //Array, which will be filled with the requeststrings
         String[] requests = new String[13];
         String title = "";
         String lecture = "";
         int itemCount = 0;
         int size = 0;
         int submitDay = 0;
         int submitHours = 0;
         int submitMinutes = 0;
 
         // TODO: exception handling
         // Get the requests and if something missing, set wronginput[arrayCount] to true and test the next detail
         for(String item: keyStrings){
             String temp = Form.form().bindFromRequest().get(item);
             //if everything's filled in, set title and put the rest in the requestarrays
             switch(itemCount){
             case 0:    
                 title = temp;
                 itemCount++;
                 break;
             case 1:    
                 lecture = temp;
                 itemCount++;
                 break;
             case 2:
                 size = Integer.parseInt(temp);
                 itemCount++;
                 break;
             case 16:
                 submitDay = checkDay(temp);
                 itemCount++;
                 break;
             case 17:
                 submitHours = Integer.parseInt(temp);
                 itemCount++;
                 break;
             case 18:
                 submitMinutes = Integer.parseInt(temp);
                 break;
             default:
                 requests[itemCount-3] = temp;
                 itemCount++;
                 break;
             }
         }
         
         WebResource ws = PhoenixLecture.getResource(CLIENT, BASE_URI);
         // Get single lecture
         SelectEntity<PhoenixLecture> selectLecture = new SelectEntity<PhoenixLecture>().addKey("title", lecture);
         ClientResponse response = ws.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, selectLecture);
         
         //Status BadRequest abfangen   
         if ((response.getStatus() != 200)){
             //alert("Veranstaltung nicht vorhanden!");
             return ok(stringShower.render("show Groups", "Ups, da ist ein Fehler aufgetreten!(" + response.getStatus() + ")"));
         }
         else{
             List<PhoenixLecture> lectures = EntityUtil.extractEntityList(response);
             PhoenixLecture lec = lectures.get(0);
             
             // unique title (maybe not necessary)
             //title = lecture + " - " + title;
             
             LocalTime submitTime = new LocalTime(submitHours, submitMinutes);
             //PhoenixDetaillist
             List<PhoenixDetails> allDetails = new ArrayList<PhoenixDetails>();
             LectureCheck lectureCheck = new LectureCheck(requests);
             //add it to allDetails
             allDetails.add(lectureCheck.getPhoenixDetails());
             //send it to server
             WebResource ws2 = PhoenixLecture.addGroupResource(CLIENT, BASE_URI);
             PhoenixLectureGroup group = new PhoenixLectureGroup(title, size, submitDay, submitTime, allDetails, lec);
             response = ws2.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, KeyReader.createAddTo(lec, group));
             return ok(stringShower.render("strings to show", "Good News!"));
         }
     }
     
     private static int checkDay(String day){
         switch(day){
             case "monday":
                 return DateTimeConstants.MONDAY;
             case "tuesday":
                 return DateTimeConstants.TUESDAY;
             case "wednesday":
                 return DateTimeConstants.WEDNESDAY;
             case "thursday":
                 return DateTimeConstants.THURSDAY;
             case "friday":
                 return DateTimeConstants.FRIDAY;
             case "saturday":
                 return DateTimeConstants.SATURDAY;
             case "sunday":
                 return DateTimeConstants.SUNDAY;
             default:
                 //throw invalid input exception
                 System.out.println("null, dafuq?!");
                 return -1;
         }
     }
     
     public static Result download(String title, String filename, String type){
             PhoenixTask task = getTaskByTitle(title);
 
             try {
                 response().setContentType("application/x-download");
                 if (type.equals("attachment")) { 
                     for(PhoenixAttachment a : task.getAttachments()) 
                         if ((a.getName()+"."+a.getType()).equals(filename)) { 
                                 response().setHeader("Content-disposition","attachment; filename="+URI.create(a.getName().replace(" ", "_")+"."+a.getType())); 
                                 response().setHeader("Content-Lenght", String.valueOf(a.getFile().length()));
                                 return ok(a.getFile());
                         }
                 }
                 else if (type.equals("pattern")) {
                     for(PhoenixText t : task.getPattern()) 
                         if ((t.getName()+"."+t.getType()).equals(filename)) { 
                                 response().setHeader("Content-disposition","attachment; filename="+t.getName()+"."+t.getType()); 
                                 response().setHeader("Content-Lenght", String.valueOf(t.getFile().length()));
                                 return ok(t.getFile());
                             }
                 }
             } catch (IOException e) { return internalServerError("File not found!"); }
 
         return internalServerError();
     }
     
     public static Result createTaskSheet() {
         ArrayList<String> titles = new ArrayList<String>();
         for(PhoenixTask t : getAllTasks())
             titles.add(t.getTitle());
         return ok(createTaskSheet.render("Create Task Sheet", titles));
     }
     
     public static Result sendTaskSheet() {
         Map<String, String> raw = Form.form().bindFromRequest().data();
         raw.remove("submit");
         String sheetname = raw.get("sheetname");
         raw.remove("sheetname");
         System.out.println(sheetname);
         
         ConnectionEntity entity = new ConnectionEntity();
         List<SelectEntity<PhoenixTask>> list = new ArrayList<SelectEntity<PhoenixTask>>();
         for(String key : raw.keySet()) 
             list.add(new SelectEntity<PhoenixTask>().addKey("title", key));
         
         entity.addSelectEntities(PhoenixTask.class, list);
         entity.addAttribute("title", sheetname);
         
         WebResource wr = PhoenixTaskSheet.connectTaskSheetWithTaskResource(CLIENT, BASE_URI);
         ClientResponse response = wr.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, entity);
          
         System.out.println("CreateTaskSheet Status: "+response.getStatus());
         if(response.getStatus() == 200){
             return ok(stringShower.render("TaskSheet created", "Your Tasksheet has been created successfully!"));
         }
         else{
             return ok(stringShower.render("ERROR", "ERROR: "+response.getStatus()));
         }
     }
     
     public static Result showGroups() {
         //show lectures
         WebResource wsLec = PhoenixLecture.getResource(CLIENT, BASE_URI);
         ClientResponse responseLec = wsLec.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, new SelectAllEntity<PhoenixLecture>());
 
         List<PhoenixLecture> lectures = EntityUtil.extractEntityList(responseLec);   
         //empty group
         List<PhoenixLectureGroup> empty = new ArrayList<PhoenixLectureGroup>();
         if (responseLec.getStatus() == 200){
             return ok(showGroups.render("show Groups", empty, lectures));
         }else{
             return ok(stringShower.render("show Groups", "Ups, da ist ein Fehler aufgetreten!(" + responseLec.getStatus() + ")"));
         }
     } 
     
     private static List<PhoenixLectureGroup> getAllGroups(String lecture) {
         WebResource wsGroups = PhoenixLectureGroup.getResource(CLIENT, BASE_URI);
         // Get single lecture
         SelectEntity<PhoenixLectureGroup> groupSelector = new SelectEntity<PhoenixLectureGroup>();
         SelectEntity<PhoenixLecture> lectureSelector = new SelectEntity<PhoenixLecture>().addKey("title", lecture);
 
         groupSelector.addKey("lecture", lectureSelector);
         ClientResponse response = wsGroups.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, groupSelector);
         List<PhoenixLectureGroup> groups = new ArrayList<PhoenixLectureGroup>();
         
         if (response.getStatus() == 200)
             groups = EntityUtil.extractEntityList(response);
         
         System.out.println("Get-all-Groups Status: "+response.getStatus());
         
         return groups;
     }
     
     private static PhoenixLectureGroup getGroup(String lecture, String groupName) {
         WebResource wsGroups = PhoenixLectureGroup.getResource(CLIENT, BASE_URI);
         // Get single lecture
         SelectEntity<PhoenixLectureGroup> groupSelector = new SelectEntity<PhoenixLectureGroup>().addKey("name", groupName);
         SelectEntity<PhoenixLecture> lectureSelector = new SelectEntity<PhoenixLecture>().addKey("title", lecture);
 
         groupSelector.addKey("lecture", lectureSelector);
         ClientResponse response = wsGroups.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, groupSelector);
         PhoenixLectureGroup group = null;
         
         if (response.getStatus() == 200)
             group = EntityUtil.extractEntity(response);
         
         System.out.println("Get-all-Groups Status: "+response.getStatus());
         
         return group;
     }
     
     public static Result showLectureGroups(){
         //show lectures
         WebResource wsLec = PhoenixLecture.getResource(CLIENT, BASE_URI);
         ClientResponse responseLec = wsLec.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, new SelectAllEntity<PhoenixLecture>());
 
         List<PhoenixLecture> lectures = EntityUtil.extractEntityList(responseLec);   
         //show groups
         String lecture = Form.form().bindFromRequest().get("lecture");
         WebResource ws = PhoenixLectureGroup.getResource(CLIENT, BASE_URI);
         // Get single lecture
         SelectEntity<PhoenixLectureGroup> groupSelector = new SelectEntity<PhoenixLectureGroup>();
         SelectEntity<PhoenixLecture> lectureSelector = new SelectEntity<PhoenixLecture>().addKey("title", lecture);
 
         groupSelector.addKey("lecture", lectureSelector);
         ClientResponse response = ws.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, groupSelector);
         List<PhoenixLectureGroup> groups = new ArrayList<PhoenixLectureGroup>();
         if (response.getStatus() == 200){
             groups = EntityUtil.extractEntityList(response);
             System.out.println("Gruppen gefunden");
             return ok(showGroups.render("show Groups", groups, lectures));
         }else{
             return ok(stringShower.render("show Groups", "Ups, da ist ein Fehler aufgetreten!(" + response.getStatus() + ")"));
         }
   }
     
     private static List<PhoenixTaskSheet> getAllTaskSheets() {
         WebResource wr = PhoenixTaskSheet.getResource(CLIENT, BASE_URI);
         ClientResponse response = wr.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, new SelectAllEntity<PhoenixTaskSheet>());
         List<PhoenixTaskSheet> result = EntityUtil.extractEntityList(response);
         return result;
     }
     
     public static Result showTaskSheets() {
         return ok(showTaskSheet.render("Show Task Sheets", getAllTaskSheets()));
     }
 
     //TODO:
     //- Exceptions(?)
     //- Statushandling
     public static Result deleteGroups(){
         //deleteResource
         Map<String, String> boxes = Form.form().bindFromRequest().data();
         boxes.remove("Delete");
         WebResource ws = PhoenixLectureGroup.deleteResource(CLIENT, BASE_URI);
         String lecture = boxes.get("lecture").replaceAll("_", " ");
         for(String key : boxes.keySet()) {
             key = key.replaceAll("_", " ");                
             System.out.println(key);
             // Create Lecture Group Selector
             SelectEntity<PhoenixLectureGroup> groupSelector = new SelectEntity<PhoenixLectureGroup>();
             // Add Lecture Group Key - the name
             groupSelector.addKey("name", key);
 
             // Create Lecture Selector
             SelectEntity<PhoenixLecture> lectureSelector = new SelectEntity<PhoenixLecture>();
             // Add Lecture Key - the title
             lectureSelector.addKey("title", lecture);
             // Add lecture selector to group selector (only groups from this lecture are selected)
             groupSelector.addKey("lecture", lectureSelector);
 
             // Send delete response
             ClientResponse response = ws.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, groupSelector);
             if (response.getStatus() != 200){
                 return ok(stringShower.render("Groups deleted", "Ups, da ist ein Fehler aufgetreten!(" + response.getStatus() + ")"));
             }
         }
         return ok(stringShower.render("Groups deleted", "Groups deleted!"));
     }
     
     public static Result deleteLecture(){
         Map<String, String> dataMap = Form.form().bindFromRequest().data();
         WebResource ws = PhoenixLecture.deleteResource(CLIENT, BASE_URI);
         // Create Lecture Selector
         SelectEntity<PhoenixLecture> lectureSelector = new SelectEntity<PhoenixLecture>();
         System.out.println(dataMap.get("lecture"));
         // Add Lecture Key - the title
         lectureSelector.addKey("title", dataMap.get("lecture"));
         // Send delete response
         ClientResponse response = ws.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, lectureSelector);
         if (response.getStatus() != 200){
             return ok(stringShower.render("lecture delete", "Ups, da ist ein Fehler aufgetreten!(" + response.getStatus() + ")"));
         }
     return ok(stringShower.render("Lecture deleted", "Lecture deleted!"));
     }
     
     public static Result showLectures(){
         WebResource ws = PhoenixLecture.getResource(CLIENT, BASE_URI);
         ClientResponse response = ws.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, new SelectAllEntity<PhoenixLecture>());
 
         List<PhoenixLecture> lectures = EntityUtil.extractEntityList(response);   
         if (response.getStatus() == 200){
             return ok(showLectures.render("show Lectures", lectures));
         }else{
             return ok(stringShower.render("send Lecture", "Ups, da ist ein Fehler aufgetreten!(" + response.getStatus() + ")"));
         }
     }
     public static Result stringShower(){
         return ok();
     }
     
     private static PhoenixLecture getLecture(String lectureTitle) {
         SelectEntity<PhoenixLecture> lectureEntity = new SelectAllEntity<PhoenixLecture>();
         lectureEntity.addKey("title", lectureTitle);
         WebResource wsLec = PhoenixLecture.getResource(CLIENT, BASE_URI);
         ClientResponse responseLec = wsLec.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, lectureEntity);
         PhoenixLecture lecture = EntityUtil.extractEntity(responseLec);
         return lecture;
     }
     
     public static Result addTaskSheetToGroupProcess() {
         //SEND Lecture-titel via hidden input !!
         List<PhoenixLectureGroup> groups = new ArrayList<PhoenixLectureGroup>();
         Map<String, String> data = Form.form().bindFromRequest().data();
         String lectureTitle = data.get("lecture");
         data.remove("lecture");
         String tasksheet = data.get("tasksheet");
         if (tasksheet != null) {
             data.remove("tasksheet");
             if (!data.isEmpty()) {
                 PhoenixLectureGroup group = null;
                 for(String g : data.keySet()) {
                      group = getGroup(lectureTitle, g);
                      if (group == null)
                          return ok(stringShower.render("FEHLER!","Gruppe "+g+" existiert nicht!!"));
                      groups.add(group);
                 }   
             }
             for (PhoenixLectureGroup plg : groups) {
                 //HOLE TASKSHEET UND HÄNGE AN JEDE GRUPPE TASKSHEET AN => SELECTENTITY
             }
         }
         
         return ok(stringShower.render("Erfolg!","Test läuft!"));
     }
     
     public static Result addTaskSheetToGroup() {
         if (request().queryString().get("lecture") != null) {
             String lecture = request().queryString().get("lecture")[0];
             if (lecture != null) {
                 List<PhoenixLectureGroup> groups = getAllGroups(lecture);
                 List<PhoenixTaskSheet> tasksheets = getAllTaskSheets();
                 return ok(addTaskSheetToGroup.render("Add Sheet to Group",lecture,tasksheets,groups));
             } else {
                 return ok(stringShower.render("ERROR", "This Lecture does not exist!"));
             }
         } else {
             return ok(stringShower.render("ERROR", "No Lecture selected!"));
         }    
     }
     
 }
