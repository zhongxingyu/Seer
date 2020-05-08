 package no.ntnu.fp;
 import java.awt.Container;
 import java.awt.List;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.security.PublicKey;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Stack;
 
 import no.ntnu.fp.gui.CalendarView;
 import no.ntnu.fp.gui.LoginView;
 import no.ntnu.fp.gui.View;
 import no.ntnu.fp.model.*;
 import no.ntnu.fp.net.cl.ClSocket;
 import no.ntnu.fp.net.co.Connection;
 import no.ntnu.fp.net.co.SimpleConnection;
 
 import javax.swing.*;
 
 public class Client extends JFrame {
     
     /** Test data **/
     public static Appointment app1;
     public static Appointment app2;
     public static Invitation inv1;
     public static Invitation inv2;
     public static User sigve;
     public static Room f1;
     
     public static Connection connection;
     
     public static GregorianCalendar activeWeek;
     public static ArrayList<ActiveWeekListener> activeWeekListeners;
     public static HashMap<String,ArrayList<Appointment>> appointments;
     private static User user;
     public static Client calendar;
     /**A stack of views*/
     static Stack<View> viewStack;
     static{
     	viewStack = new Stack<View>();
     	appointments =  new HashMap<String, ArrayList<Appointment>>();
     	activeWeekListeners = new ArrayList<ActiveWeekListener>();
     	activeWeek = new GregorianCalendar();
 	}
     /**
      * Pushes view on {@link #viewStack}, displays and returns view afterwards.
      * @param view
      * @return topmost view
      * @see no.ntnu.fp.Client#viewStack
      */
     static View pushView(View view) {
     	view.initialize();
     	calendar.setContentPane((Container) view);
     	if(viewStack.size()>0){
 	    	viewStack.peek().pause();
     	}
     	viewStack.push(view);
     	calendar.pack();
     	calendar.centerOnScreen();
 		return view;
 	}
     /**
      * Pops view, resuming and returning the next view in stack
      * @return next view
      */
     public static View popView() {
     	View popped = viewStack.pop();
     	popped.deinitialize();
     	View view = viewStack.peek();
     	view.resume();
     	calendar.setContentPane((Container) view);
     	calendar.pack();
     	calendar.centerOnScreen();
 		return view;
 	}
     
     public static String dateToString(Date d){
     	return ""+(d.getYear()+1900)+"-"+(d.getMonth()+1)+"-"+d.getDate();
     }
     public static String dateToString(GregorianCalendar d){
     	return ""+d.get(GregorianCalendar.YEAR)+"-"+(d.get(GregorianCalendar.MONTH)+1)+"-"+d.get(GregorianCalendar.DATE);
     }
     
     public void addAppointment(Appointment appointment){
     	ArrayList<Appointment> appointments = new ArrayList<Appointment>();
     	appointments.add(appointment);
     	addAppointments(appointments);
     }
     
     public void addAppointments(Iterable<Appointment> appointments){
     	String key;
     	for(Appointment appointment : appointments){
     		key = Client.dateToString(appointment.getStartDate());
     		System.out.println(key);
     		if(Client.appointments.containsKey(key)){
     			Client.appointments.get(key).add(appointment);
     		}else{
     			Client.appointments.put(key,new ArrayList<Appointment>());
     			Client.appointments.get(key).add(appointment);
     		}
     		
     	}
     }
     
     public static void setActiveWeek(int year, int month, int date){
     	activeWeek.set(year, month, date);
     	while(activeWeek.get(GregorianCalendar.DAY_OF_MONTH) != 1 && activeWeek.get(GregorianCalendar.DAY_OF_WEEK)!= activeWeek.getFirstDayOfWeek()){
     		activeWeek.add(GregorianCalendar.DAY_OF_MONTH, -1);
     	}
     	System.out.println(Client.dateToString(activeWeek));
     	for(ActiveWeekListener listener : activeWeekListeners){
     		listener.setActiveWeek(activeWeek.get(GregorianCalendar.YEAR), activeWeek.get(GregorianCalendar.MONTH), activeWeek.get(GregorianCalendar.DATE));
     	}
     }
     
     public static void addActiveWeekListener(ActiveWeekListener listener){
     	activeWeekListeners.add(listener);
     }
     
     public static void removeActiveWeekListener(ActiveWeekListener listener){
     	activeWeekListeners.remove(listener);
     }
 
     public Client() throws ConnectException, IOException, ClassNotFoundException {
         setTitle("Calendar");
         
         sigve = User.getUser(1);//new User("sigveseb", "Sigve Sebastian", "Farstad", "sigve@arkt.is");
         inv1 = new Invitation(sigve, app1);
         inv2 = new Invitation(sigve, app2);
         app1 = new Appointment("Testappointment", "Utendørs", "Dette er en test for å teste testen", new GregorianCalendar(2012, 3, 23, 10, 30), new GregorianCalendar(2012,3,23,11,30));
         app2 = new Appointment("Testappointment nummer 2", f1, "Dette er en test for å teste testen", new GregorianCalendar(2012,3,26,12,0),new GregorianCalendar(2012,3,26,14,45));
         f1 = new Room("F1", 12,300, false);
     }
     
     
     
     /**
      * Logs in user, eventually
      * @param username
      * @param password
      * 
      */
     public static Boolean login(String username, String password){
         Boolean status;
     	if (username.equals("sigve") && password.equals("1337")) {
             setUser(User.getUser(1)); //get user 1 for now
             calendar.addAppointments(Appointment.loadAppointments(user, null, null)); //bogus date data for now
            System.out.println(calendar.appointments);
 	        GregorianCalendar now = new GregorianCalendar(2012,3,23); //arbitrary date for now
             System.out.println("User logged in");
 	        pushView(new CalendarView());
 	        Client.setActiveWeek(now.get(GregorianCalendar.YEAR), now.get(GregorianCalendar.MONTH), now.get(GregorianCalendar.DATE));
             status = true;
         }
         else {
             System.out.println("Wrong username and/or password");
             status = false;
         }
         return status;
     }
     /**
      * Sets this calendar user
      * @param user
      */
     public static void setUser(User user) {
         Client.user = user;
     }
     /**
      * Returns this Calendar's user
      * @return user
      * 
      */
     public static User getUser() {
         return Client.user;
     }
     /**
      * Centers the window
      */
     public void centerOnScreen(){
     	calendar.setLocationRelativeTo(null);
     }
     /**
      * makes a new Calendar, pushes to stack, and makes it visible
      * @throws IOException 
      * @throws ConnectException 
      * @throws ClassNotFoundException 
      * @see#viewStack 
      */
     public static void main (String[] args) throws ConnectException, IOException, ClassNotFoundException {
         calendar = new Client();
         pushView(new LoginView());
         calendar.setVisible(true);
     }
     
 }
