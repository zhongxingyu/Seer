 /**
  * The SidePanel is the panel that is on the left-hand side of the scroll pane, with the day's time information on it
  */
 
 package gui.main;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JPanel;
 
 import gui.TimeSlot;
 
 public class SidePanel extends JPanel {
 
 	DayPanel dp;
 	TimeHeading heading;
 	TimesPanel timesPanel;
 	
 	public SidePanel(DayPanel dp) {
 		this.dp = dp;
 		setLayout(new BorderLayout());
 		heading = new TimeHeading(dp.getDay().getDate());
 		add(heading, BorderLayout.NORTH);
 		
 		timesPanel = new TimesPanel(new TimeSlot(dp.getDay().getStart(),dp.getDay().getEnd()));
 		add(timesPanel, BorderLayout.CENTER);
 	}
 	
 	public void refreshTimeSlot(TimeSlot timeSlot) {
 		this.remove(timesPanel);
 		timesPanel = new TimesPanel(timeSlot);
 		add(timesPanel, BorderLayout.CENTER);
		this.revalidate();
 	}
 }
