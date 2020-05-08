 package edu.brown.cs32.scheddar.gui;
 
 
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import edu.brown.cs32.scheddar.Meeting;
 import edu.brown.cs32.scheddar.ScheddarTime;
 import edu.brown.cs32.scheddar.UsefulMethods;
 
 /**
  * @author sdemane
  * 
  * An abstract class encompassing the week and month views 
  * of the Scheddar gui.
  *
  */
 public class CalendarPane extends ScheddarSubPane {
 	private static final long serialVersionUID = 1L;
 	private ScheddarTime _time;
 	ScheddarSubPane activePane = null;
 	
 	public CalendarPane(ScheddarPane s) {
 		super(s);
 		ScheddarTime today = UsefulMethods.getCurrentTime();
 		switchToMonth(today);
 	}
 	
 	public void switchToMonth(ScheddarTime time) {
 		removeAll();
 		revalidate();
 		activePane = new MonthPane(_scheddarPane,time); 
 		add(activePane);
 		_time = time;
 		revalidate();
 	}
 	
 	public void switchToWeek(ScheddarTime time) {
 		removeAll();
 		revalidate();
 		activePane = new WeekPane(_scheddarPane,time);
 		add(activePane);
 		_time = time;
 		revalidate();
 	}
 	
 	public void switchToMeeting(ScheddarTime time, Meeting m) {
 		removeAll();
 		revalidate();
 		if (time == null)
 			time = UsefulMethods.getCurrentTime();
 
 		_time = time;
 		add(new DayPane(_scheddarPane, time));
 		add(new MeetingPane(_scheddarPane, m));
 		revalidate();
 	}
 
 	public ScheddarTime getTime() {
 		return _time;
 	}
 	
 	public Dimension getPreferredSize() {
 		return new Dimension(_scheddarPane.getPreferredSize().width * 7/8 - 10, _scheddarPane.getPreferredSize().height);
 	}
 	
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
//		if(activePane!=null) {
//			activePane.repaint();
//		}
 		Graphics2D g2 = (Graphics2D) g;
 		if (isFun()) {
 			BufferedImage img = null;
 			try {
 			    img = ImageIO.read(new File("data/swiss-background.jpg"));
 			    g2.drawImage(img,0,0,null);
 			} catch (IOException e) {
 			}
 		}
 	}
 
 }
