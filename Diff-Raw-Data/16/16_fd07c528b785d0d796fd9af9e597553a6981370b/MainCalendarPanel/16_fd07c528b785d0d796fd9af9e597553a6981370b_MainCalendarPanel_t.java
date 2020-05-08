 package gui;
 
import java.awt.Event;

 import javax.swing.JPanel;
 
import model.EventModel;
import model.Person;
 
public class MainCalendarPanel extends JPanel {
	
	public MainCalendarPanel(){
		EventModel model = new EventModel(null, null);
		EventPanel eventPanel = new EventPanel(model);
		add(eventPanel);
		
	}
	
 }
