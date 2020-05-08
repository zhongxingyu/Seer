 package edu.rpi.rocs.client.ui.scheduler;
 
 import java.util.List;
 
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import edu.rpi.rocs.client.objectmodel.Period;
 import edu.rpi.rocs.client.objectmodel.Section;
 import edu.rpi.rocs.client.objectmodel.Time;
 
 public class SectionInfoPanel extends VerticalPanel {
 	protected Label nocourse = new Label("No course selected.");
 	protected Label time;
 	protected Label seats;
 	protected Label name;
 	protected Label num;
 	protected HTML periods;
 	protected HTML notes;
 	protected boolean showing=false;
 	
 	public SectionInfoPanel() {
 		super();
 		setStyleName("section-info");
 		add(nocourse);
 	}
 	
 	public void displayInfoForSection(Section s) {
 		if(s == null) {
 			clear();
 			return;
 		}
 		name = new Label(s.getParent().getName());
 		num = new Label("Section "+s.getNumber()+" ("+s.getCrn()+")");
 		int hr=0,min=0,total=0;
 		for(Period p : s.getPeriods()) {
 			total += (p.getEndInt() - p.getStartInt())*p.getDays().size();
 		}
 		hr = total/60;
 		min = total%60;
 		String minStr;
 		if(min < 10) minStr = "0"+min;
 		else minStr = ""+min;
 		time = new Label("Class time: "+hr+":"+minStr+" per week");
 		seats = new Label("Seats: "+s.getSeats()+" ("+(s.getSeats()-s.getStudents())+" remaining)");
		String text = "<p>Class times:<p>";
 		text += "<ul>";
 		for(Period p : s.getPeriods()) {
 			String type = p.getType();
 			Time start = p.getStart();
 			Time end = p.getEnd();
 			List<Integer> days = p.getDays();
 			String prof = p.getInstructor();
 			text += "<li>"+type+"<br/>";
 			if(start.getMeridiem().equals(end.getMeridiem())) {
 				text += start.get12HRString(false);
 				text += " - ";
 				text += end.get12HRString();
 			}
 			else {
 				text += start.get12HRString();
 				text += " - ";
 				text += end.get12HRString();
 			}
 			text += " on ";
 			for(int i=0;i<days.size();i++) {
 				text += dayOfWeek(days.get(i));
 				if(i!=days.size()-1) {
 					text+=",";
 				}
 			}
 			text += "<br/>";
 			text += "Professor: "+prof;
 			text += "</li>";
 		}
 		text += "</ul>";
 		periods = new HTML(text);
 	}
 	
 	private String dayOfWeek(int day) {
 		switch(day) {
 		case 0:
 			return "Mon";
 		case 1:
 			return "Tue";
 		case 2:
 			return "Wed";
 		case 3:
 			return "Thu";
 		case 4:
 			return "Fri";
 		case 5:
 			return "Sat";
 		case 6:
 			return "Sun";
 		default:
 			return "???";
 		}
 	}
 
 	public void clear() {
 		showing = false;
 		for(int i=this.getWidgetCount();i>0;i--)
 			this.remove(0);
 		this.add(nocourse);
 	}
 }
