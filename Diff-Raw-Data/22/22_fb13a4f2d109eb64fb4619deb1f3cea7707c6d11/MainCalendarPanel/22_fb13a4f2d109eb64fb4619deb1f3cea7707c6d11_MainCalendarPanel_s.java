 package gui;
 
 import java.io.IOException;
 import java.sql.Timestamp;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import datapackage.CalendarRequestPackage;
 
 import net.PackageSender;
 
 import model.Event;
 import model.EventModel;
 import model.Person;
 
 public class MainCalendarPanel extends JPanel {
 
 	PackageSender sender;
 
 	public MainCalendarPanel(String username){
 //		CalendarRequestPackage calReq = new CalendarRequestPackage(username,null);
 //		try {
 //			sender = new PackageSender();
 //		} catch (IOException e) {
 //			JOptionPane.showMessageDialog(null, "Could not connect to server. Critical");
 //			e.printStackTrace();
 //		}
 //		sender.sendPackage(calReq);
 		Person p = new Person();
 		p.setName("Torstein");
 		EventModel model = new EventModel(
 				new Event(99, new Person(), new Timestamp(System.currentTimeMillis()), 
 						new Timestamp(System.currentTimeMillis()), 
						"Name", "Decription", "Place", null, null), p);
 		EventPanel eventPanel = new EventPanel(model);
 		add(eventPanel);
 		this.updateUI();
 
 		
 	}
 }
