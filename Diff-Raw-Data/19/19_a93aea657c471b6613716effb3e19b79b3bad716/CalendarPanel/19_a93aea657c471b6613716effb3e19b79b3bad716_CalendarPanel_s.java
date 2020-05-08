 package gui;
 
 import java.awt.GridBagConstraints;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import model.CalendarModel;
 import model.Event;
 
 public class CalendarPanel extends JPanel implements PropertyChangeListener{
 
 	CalendarModel model; 
 	GridBagConstraints grid; 
 	public static final long MILLISECOND_IN_DAY = 24*60*60*1000;
 	
 	public CalendarPanel(CalendarModel model){
 		this.model = model; 
 		model.addPropertyChangeListener(this);
 		this.setLayout(null);
 		
 		update();
 //		this.setBorder(BorderFactory.createLineBorder(Color.black));
 		this.validate(); 
 		this.repaint();
 	}
 	
 	public void update(){
 		
 		ArrayList<Event> events = model.getEventList();
 		
 		Timestamp dayStart = model.getWeekStart();
 		Timestamp dayEnd = new Timestamp(dayStart.getTime() + MILLISECOND_IN_DAY);
 		
 		int height = this.getHeight() - 30;
 		int width = (this.getWidth()-50)/7;
 		
 		addTimeLabel(height);
 		
 		for (int i = 0; i < 7; i++){
 			
 			ArrayList<Event> eventsForDay = new ArrayList<Event>();
 			
 			for (Event event : events){
 				if (event.getStartTime().after(dayStart) && event.getEndTime().before(dayEnd)){
 					eventsForDay.add(event);
 				}
 			}
 			
 			JPanel dayPanel = new CalendarDayPanel(eventsForDay,width,height,i);
 			this.add(dayPanel);
 			dayPanel.setBounds((int)width*(i)+50, 0, (int)width, (int)height);
 			
 			dayStart = dayEnd; 
 			dayEnd = new Timestamp(dayStart.getTime() + MILLISECOND_IN_DAY);
 			
 		}
 	}
 	private void addTimeLabel(int height){
 		JPanel panel = new JPanel(); 
 		panel.setLayout(null); 
 		
 		for (int i = 0; i < 24; i ++){
 			JLabel timeLabel = getTimeLabel(i); 
 			panel.add(timeLabel); 
 			timeLabel.setBounds(0, i*(height/24) + 30, 50, height/24);
 		}
 		this.add(panel);
 		panel.setBounds(0, 0, 50, height);
 	}
 	
 	private JLabel getTimeLabel(int time){
 		return new JLabel("  " + Integer.toString(time) + ":00");
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent arg0) {
 		update(); 
 		this.validate(); 
 		this.repaint();
 	}
 }
