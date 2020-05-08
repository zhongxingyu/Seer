 package edu.rpi.rocs.client.filters.course;
 
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import edu.rpi.rocs.client.filters.course.reason.InvalidReason;
 import edu.rpi.rocs.client.objectmodel.Course;
 import edu.rpi.rocs.client.objectmodel.Section;
 
 public class DepartmentCourseFilter extends CourseFilter {
 	
 	private HorizontalPanel filterPanel = new HorizontalPanel();
 	private Label filterLabel = new Label("Department:");
 	private TextBox departmentBox = new TextBox();
 
 	protected DepartmentCourseFilter() {
 		filterPanel.add(filterLabel);
 		filterPanel.add(departmentBox);
 	}
 
 	@Override
 	protected boolean filter(Course course) {
		if ((course== null) || (departmentBox.getText().isEmpty()))
 				return true;
 		String dept = course.getDept();
 		return departmentBox.getText().equalsIgnoreCase(dept);
 	}
 	@Override
 	protected boolean filter(Section section) {
 		return (section == null) ? true : filter(section.getParent());
 	}
 	
 	@Override
 	public Widget getFilterWidget() {
 		return filterPanel;
 	}
 
 	@Override
 	public InvalidReason getReason() {
 		return InvalidReason.WRONG_DEPARTMENT;	
 	}
 
 	
 	
 
 }
