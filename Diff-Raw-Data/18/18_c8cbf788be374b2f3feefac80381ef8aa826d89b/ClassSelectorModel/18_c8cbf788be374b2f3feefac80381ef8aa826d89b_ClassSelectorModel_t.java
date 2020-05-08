 package schedutron;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.Scrollbar;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 public class ClassSelectorModel implements ListSelectionModel{
   
 	//list on the left
 	static JList<Course> list_left;
     //list on the right
     static JList<Course> list_right;
 
     GridBagConstraints c;
     
     //scroll pane for list on the left
     static JScrollPane scroll_left; 
     //scroll pan for list on the right
     static JScrollPane scroll_right; 
     //the pane that holds the two lists
     JSplitPane splitPane;
     //the list models that are used for adding and removing elements from the lists
     static DefaultListModel<Course> listmodel_right;
     static DefaultListModel<Course> listmodel_left;
     
     //the list of courses passed to the constructor
     //not used yet
     ArrayList<Course> mcourses = new ArrayList<Course>();
     /** List of courses that are being taken */
     public ArrayList<Course> takencourses;
     
 
       
 	public ClassSelectorModel(ArrayList<Course> courses) {
 
 		    c = new GridBagConstraints();
 	        c.fill = GridBagConstraints.BOTH;
 	        c.weighty = 1;
 
 		    mcourses = courses;
 		    ArrayList<Course> mcourses_left = courses;
 		    
 
 		    listmodel_left = new DefaultListModel<Course>();
 			Course[] courseArray = new Course[mcourses_left.size()];
 		    addToListModel(mcourses_left.toArray(courseArray));
 		    
 		    list_left = new JList<Course>(listmodel_left);
 		    list_left.setLayoutOrientation(JList.VERTICAL);
 		    list_left.setVisibleRowCount(-1);
 		    list_left.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		    
 		    scroll_left = new JScrollPane(list_left);
 		    scroll_left.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		    
 		    //set up list model
 		    listmodel_right = new DefaultListModel<Course>();
 		    //make it the model for the selected_list
 		    list_right = new JList<Course>(listmodel_right); 
 		    list_right.setLayoutOrientation(JList.VERTICAL);
 		    list_right.setVisibleRowCount(-1);
 		    list_right.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		    //make it the right scroll pane
 		    scroll_right = new JScrollPane(list_right);
 	 
 	        //Provide minimum sizes for the two components in the split pane.
 	        Dimension minimumSize = new Dimension(100, 500);
 	        scroll_left.setMinimumSize(minimumSize);
 	        scroll_right.setMinimumSize(minimumSize);
 	        scroll_left.setPreferredSize(minimumSize);
 	        scroll_right.setPreferredSize(minimumSize);
 	        
 	        Dimension maximumSize = new Dimension(200, 500);
 	        scroll_left.setMaximumSize(maximumSize);
 	        scroll_right.setMaximumSize(maximumSize);
 	 
 		    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                     scroll_left, scroll_right);
 	        splitPane.setOneTouchExpandable(true);
 	        splitPane.setDividerLocation(150);
	        
	        //Provide a preferred size for the split pane.
 	        splitPane.setPreferredSize(new Dimension(400, 500));
 		    takencourses = new ArrayList<Course>();
 	}
 	
 	
 	private void addToListModel(Course[] arr_courses){
 		for(Course c : arr_courses){
 			listmodel_left.addElement(c);
 		}
 	}
 	
 	private void addToSelectedListModel(Course[] arr_courses){
 		for(Course c : arr_courses){
 			listmodel_right.addElement(c);
 		}
 	}
 	
 	public Course[] removeCourse( Course deleteMe, Course[] input) {
 	    List<Course> result = new LinkedList<Course>();
 	    if (input.length > 0){
 	    	for(Course item : input)
 	    		if(!deleteMe.equals(item))
 	    			result.add(item);
 	    } 
 	    return (schedutron.Course[]) result.toArray(input);
 	}
 	  
 	public void setPanel(JPanel panel){
 		 panel.add(splitPane, c);
 	}
 	  
 	public static <Course> Course[] concatCourses(Course[] first, Course[] second) {
 		  Course[] result = Arrays.copyOf(first, first.length + second.length);
 		  System.arraycopy(second, 0, result, first.length, second.length);
 		  return result;
 	}
 	
 	public class CourseSelectorListener implements ListSelectionListener {
 
 		public CourseSelectorListener(){
 		}
 		
 		@Override
 		public void valueChanged(ListSelectionEvent e) {
 		
 			if (e.getValueIsAdjusting() == false) {
 				if (list_left.getSelectedIndex() == -1) {
 				} else {
 					JList<Course> ret_list = (JList<Course>)e.getSource();
 					Course newcourse = ret_list.getSelectedValue();
 					if (!scheduleConflicts(listmodel_right,newcourse)){
 						listmodel_right.addElement(newcourse);
 						listmodel_left.removeElement(newcourse);  
 						takencourses.add(newcourse);
 					}
 
 				}
 			}
 		}
 		
 	}
 
 	public static boolean scheduleConflicts(DefaultListModel<Course> model, Course course){
 		for(int i=0; i < model.getSize(); i++){
 			if (course.ConflictsWith(model.getElementAt(i))){
 				System.out.println("This course conflicts<><><><><>");
 				return true;
 			}
 		}
 		return false;
 		
 	}
 	
 	
 	public class CourseDeselectorListener implements ListSelectionListener {
 
 		public CourseDeselectorListener(){			
 
 		}
 		
 		@Override
 		public void valueChanged(ListSelectionEvent e) {
 			if (e.getValueIsAdjusting() == false) {
 				if (list_right.getSelectedIndex() == -1) {
 				} else {
 					JList<Course> ret_list = (JList<Course>)e.getSource();
 					Course newcourse = ret_list.getSelectedValue();;
 					listmodel_left.addElement(newcourse);
 					listmodel_right.removeElement(newcourse);
 					takencourses.remove(newcourse);
 				}
 			}
 		}
 	}
 
 
 	@Override
 	public void addListSelectionListener(ListSelectionListener x) {
 		list_left.addListSelectionListener(x);
 	}
 	
 	public void addListDeselectionListener(ListSelectionListener x){
 		list_right.addListSelectionListener(x);
 	}
 
 	public void addListeners(){
 	    this.addListSelectionListener(new CourseSelectorListener());
 	    this.addListDeselectionListener(new CourseDeselectorListener());
 
 	}
 	@Override
 	public void addSelectionInterval(int index0, int index1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void clearSelection() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public int getAnchorSelectionIndex() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getLeadSelectionIndex() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getMaxSelectionIndex() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getMinSelectionIndex() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getSelectionMode() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public boolean getValueIsAdjusting() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void insertIndexInterval(int index, int length, boolean before) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean isSelectedIndex(int index) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean isSelectionEmpty() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void removeIndexInterval(int index0, int index1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void removeListSelectionListener(ListSelectionListener x) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void removeSelectionInterval(int index0, int index1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setAnchorSelectionIndex(int index) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setLeadSelectionIndex(int index) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setSelectionInterval(int index0, int index1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setSelectionMode(int selectionMode) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setValueIsAdjusting(boolean valueIsAdjusting) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 
 
 }
