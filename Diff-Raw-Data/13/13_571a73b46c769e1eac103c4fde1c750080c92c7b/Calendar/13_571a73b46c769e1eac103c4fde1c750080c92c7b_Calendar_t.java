 import gui.CalendarBar;
 import gui.LoginView;
 import gui.WeekView;
 import model.User;
 
 public class Calendar {
     
     private User user;
     static Calendar calendar;
 
     public Calendar() {
         CalendarView view = new CalendarView();
 
        if (this.getUser() == null) {
            System.out.println("No user logged in");
             LoginView loginView = new LoginView();
             view.setContentView(loginView);
         }
         else {
            System.out.println("Bruker logget inn");
             WeekView week = new WeekView();
             CalendarBar bar = new CalendarBar(2012, 3);
             view.setContentView(week);
             view.setSideBar(bar);
         }
 
     }
     
     public void setUser(User user) {
         this.user = user;
     }
     
     public User getUser() {
         return this.user;
     }
 
     public static void main (String[] args) {
         calendar = new Calendar();
     }
 }
