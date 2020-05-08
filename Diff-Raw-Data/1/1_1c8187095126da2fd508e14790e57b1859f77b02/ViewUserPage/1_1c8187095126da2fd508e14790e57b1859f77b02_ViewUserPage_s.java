 import javax.swing.*;
 import java.awt.*;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 
 
 @SuppressWarnings("serial")
 public class ViewUserPage extends PagePanel {
     
     private static final int UPCOMING_EVENTS_SIZE = 5;
     
     private JLabel usernameLabel = new JLabel();
     private JPanel availabilityPanel = new JPanel();
     
     public ViewUserPage(){
         setLayout(new BorderLayout());
         setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
         JPanel infoPanel = new JPanel();
         
         JPanel[] panels = {infoPanel, upcomingEventsPanel, availabilityPanel};
         add(Utils.stackPanels(panels));
         
         infoPanel.add(usernameLabel);
         
         Font font = new Font(getFont().getFontName(), Font.BOLD, 20);
         usernameLabel.setFont(font);
     }
     
     @Override
     public void activate() {
         usernameLabel.setText("Name: " + SchedulerMain.application.currentUser.getName());
         
         Calendar cal = new GregorianCalendar();
         Date start = cal.getTime();
         cal.add(Calendar.MONTH, 1);
         Date end = cal.getTime();
         Iterator<OneTimeEvent> eventsIt = SchedulerMain.application.currentUser.getEvents(start, end).iterator();
         for (int i=0; i<UPCOMING_EVENTS_SIZE && eventsIt.hasNext(); i++){
             OneTimeEvent ev = eventsIt.next();
             upcomingEventsPanel.add(new JLabel(ev.getName()));
         }
     }
 
 }
