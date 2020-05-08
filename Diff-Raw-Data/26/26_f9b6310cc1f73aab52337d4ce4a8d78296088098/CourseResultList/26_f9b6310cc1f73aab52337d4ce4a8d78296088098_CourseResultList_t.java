 package edu.rpi.rocs.client.ui.coursesearch;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import edu.rpi.rocs.client.filters.course.reason.InvalidReason;
 import edu.rpi.rocs.client.objectmodel.Course;
 
public class CourseResultList extends ScrollPanel {
	
	private VerticalPanel contentPanel;
 
 	private static CourseResultList instance = null;
 	public static CourseResultList getInstance() {
 		if (instance == null) {
 			instance = new CourseResultList();
 		}
 		return instance;
 	}
 	
	private CourseResultList() {
		contentPanel = new VerticalPanel();
		this.add(contentPanel);
	}
	
 	
 	public void clearList() {
		contentPanel.clear();
 	}
 	
 	public void addCourse(Course course, InvalidReason reason) {
		contentPanel.add(new Label(course.getDept() + " " + 
				course.getNum() + " - " + course.getName()));
 	}
 }
