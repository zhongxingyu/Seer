 package no.ntnu.fp.common.handlers;
 
 import no.ntnu.fp.client.Connection;
 import no.ntnu.fp.common.Constants;
 import no.ntnu.fp.common.model.Employee;
 import no.ntnu.fp.common.model.Event;
 import no.ntnu.fp.common.model.Notification;
 import no.ntnu.fp.common.model.Employee.Gender;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class EmployeeHandler {
 
     private int id;
 
     public EmployeeHandler(int id){
         this.id = id;
     }
     public ArrayList<Event> getEvents(int weekNr){
         if(Constants.USE_SERVER) return getEventsFromServer();
         ArrayList<Event> events = new ArrayList<Event>();
         events.add(Event.getDummyEvent("Superevent"));
         return events;
     }
 
     public Event getEvent(int id){
         return Event.getDummyEvent("Superevent");
     }
     
     public static ArrayList<Employee> getAllEmployees(){
         if(Constants.USE_SERVER) return getAllEmployeesFromServer();
         ArrayList<Employee> list = new ArrayList<Employee>();
         //TODO fjerne dummies
         list.add(new Employee("David Storjord", "awsm@test.no", new Date(1990, 12, 31), Gender.MALE));
         list.add(new Employee("Anne Berit", "awsm@test.no", new Date(1990, 12, 31), Gender.MALE));
         list.add(new Employee("Anne Ulf", "awsm@test.no", new Date(1990, 12, 31), Gender.MALE));
         list.add(new Employee("David Myklebust", "awsm@test.no", new Date(1990, 12, 31), Gender.MALE));
         list.add(new Employee("Rolf Relekang", "awsm@test.no", new Date(1990, 12, 31), Gender.MALE));
         list.add(new Employee("Rolf Rolfemann", "awsm@test.no", new Date(1990, 12, 31), Gender.MALE));
         list.add(new Employee("Audun Skjervold", "awsm@test.no", new Date(1990, 12, 31), Gender.MALE));
         list.add(new Employee("Guttorm", "awsm@test.no", new Date(1990, 12, 31), Gender.MALE));
         list.add(new Employee("Jenteorm", "awsm@test.no", new Date(1990, 12, 31), Gender.MALE));
         
         
         list.add(Employee.getExampleEployee());
         return list;
     }
 
     private static ArrayList<Employee> getAllEmployeesFromServer() {
         ArrayList<Employee> employees = new ArrayList<Employee>();
         try {
             Connection conn = new Connection();
             try {
                 conn.send(new JSONObject().put("key", "employees").put("action", "all"));
                 String message = conn.receive();
                 JSONArray jsonArray = new JSONArray(message);
                 for (int i = 0; i < jsonArray.length(); i++) {
                     JSONObject object = jsonArray.getJSONObject(i);
                     Employee e = new Employee(object);
                     employees.add(e);
                 }
                 conn.close();
 
             } catch (JSONException e) {
                 conn.close();
                 e.printStackTrace();
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         return employees;
     }
 
     public ArrayList<Notification> getAllNotifications(){
         if(Constants.USE_SERVER) return getNotificationsFromServer();
         ArrayList<Notification> list = new ArrayList<Notification>();
 //        list.add(new Notification(1, Event.getDummyEvent("Event"), "2012-03-19 12:04:36", true, Notification.NotificationType.INVITATION/*, testGuy*/));
 //        list.add(new Notification(2, Event.getDummyEvent("Event"), "2012-03-20 13:05:37", false, Notification.NotificationType.ACCEPTED));
 //        list.add(new Notification(3, Event.getDummyEvent("Event"), "2012-03-20 13:05:38", false, Notification.NotificationType.DECLINED));
 //        list.add(new Notification(4, Event.getDummyEvent("Event"), "2012-03-20 13:05:40", false, Notification.NotificationType.DELETION));
 //        list.add(new Notification(8, Event.getDummyEvent("Event"), "2012-03-20 13:05:39", true, Notification.NotificationType.CHANGE));
 
         return list;
     }
 
     private ArrayList<Event> getEventsFromServer() {
         ArrayList<Event> events = new ArrayList<Event>();
         try {
             Connection conn = new Connection();
             try {
                 conn.send(new JSONObject().put("key", "event").put("action", "all_for_user").put("argument", Integer.toString(this.id)));
                 String message = conn.receive();
                 JSONArray jsonArray = new JSONArray(message);
                 for (int i = 0; i < jsonArray.length(); i++) {
                     JSONObject object = jsonArray.getJSONObject(i);
                     Event e = new Event(object, false);
                     events.add(e);
                 }
                conn.close();
 
             } catch (JSONException e) {
                 conn.close();
                 e.printStackTrace();
             } catch (SQLException e) {
                 conn.close();
                 e.printStackTrace();
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         return events;
     }
     private ArrayList<Notification> getNotificationsFromServer() {
         ArrayList<Notification> notifications = new ArrayList<Notification>();
         try {
             Connection conn = new Connection();
             try {
                 conn.send(new JSONObject().put("key", "notification").put("action", "all_for_user").put("argument", Integer.toString(this.id)));
                 String message = conn.receive();
                 JSONArray jsonArray = new JSONArray(message);
                 for (int i = 0; i < jsonArray.length(); i++) {
                     JSONObject object = jsonArray.optJSONObject(i);
                     try {
                     	Notification notification = new Notification(object);
                     	notifications.add(notification);
                     } catch(SQLException e ){
                     	e.printStackTrace();
                     }
                 }
                conn.close();
             } catch (JSONException e) {
                 conn.close();
                 e.printStackTrace();
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         return notifications;
     }
 
 }
