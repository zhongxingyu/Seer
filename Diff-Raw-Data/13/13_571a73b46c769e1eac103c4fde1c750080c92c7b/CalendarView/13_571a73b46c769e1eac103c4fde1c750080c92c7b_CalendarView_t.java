 import gui.ContentView;
 import gui.GUI_CONSTANTS;
 import gui.StatusBar;
 import gui.SideBar;
 
 import javax.swing.*;
 import java.awt.*;
 
 public class CalendarView extends JFrame {
     
     private StatusBar statusBar;
     private SideBar sideBar;
     private ContentView contentView;
 
     public CalendarView() {
         this.setTitle("Calendar");
         this.setSize(GUI_CONSTANTS.calendarViewDimension);
         this.setLayout(new BorderLayout());
 
         this.setUp();
 
         this.setVisible(true);
     }
 
 
     public void setUp() {
         statusBar = new StatusBar();
         this.add(statusBar, BorderLayout.NORTH);
         
         sideBar = new SideBar();
         this.add(sideBar, BorderLayout.WEST);
         
         contentView = new ContentView();
         this.add(contentView, BorderLayout.CENTER);
     }
 
     public void setSideBar(JPanel sideBar) {
        System.out.println("Added sidebar");
         this.sideBar.removeAll();
         this.sideBar.add(sideBar);
     }
 
     public void setContentView(JPanel contentView) {
        System.out.println("Added contentview");
         this.contentView.removeAll();
         this.contentView.add(contentView);
     }
 }
