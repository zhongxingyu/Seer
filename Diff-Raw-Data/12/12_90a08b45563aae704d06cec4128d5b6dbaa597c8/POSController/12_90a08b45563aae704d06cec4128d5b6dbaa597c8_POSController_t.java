 /* RPI Planner - Customized plans of study for RPI students.
  *
  * Copyright (C) 2008 Eric Allen allene2@rpi.edu
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package rpiplanner;
 
 import java.awt.CardLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 
 import javax.swing.AbstractListModel;
 import javax.swing.ComboBoxModel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListModel;
 import javax.swing.border.TitledBorder;
 
 import rpiplanner.model.Course;
 import rpiplanner.model.CourseDatabase;
 import rpiplanner.model.Degree;
 import rpiplanner.model.Term;
 import rpiplanner.model.PlanOfStudy;
 import rpiplanner.model.ValidationError;
 import rpiplanner.model.YearPart;
 import rpiplanner.view.CourseDatabaseFilter;
 import rpiplanner.view.CourseDisplay;
 import rpiplanner.view.CourseTransferHandler;
 import rpiplanner.view.DegreeListModel;
 import rpiplanner.view.PlanOfStudyEditor;
 
 public class POSController {
 	protected PlanOfStudy plan;
 	protected rpiplanner.view.PlanOfStudyEditor view;
 	private ArrayList<JPanel> semesterPanels;
 	private CourseDatabase courseDatabase;
 	private CourseDatabaseFilter courseDatabaseModel;
 	private JPanel detailsPanel;
 	private DegreeListModel degreeListModel;
 	private DegreeListModel planDegreeListModel;
 	private JList degreeList;
 	protected final PropertyChangeSupport support = new PropertyChangeSupport(this);
 	
 	public POSController(){
 		plan = new PlanOfStudy();
 	}
 
 	public void setSemesterPanels(ArrayList<JPanel> semesterPanels) {
 		this.semesterPanels = semesterPanels;
 		for(int i = 0; i < semesterPanels.size(); i++){
 			JPanel p = semesterPanels.get(i);
 			p.setTransferHandler(new CourseTransferHandler(this));
 			updateSemesterPanel(i, plan.getTerm(i));
 		}
 	}
 
 	public void setCourseDatabase(CourseDatabase courseDatabase) {
 		this.courseDatabase = courseDatabase;
 		this.courseDatabaseModel = new CourseDatabaseFilter(courseDatabase);
 		courseDatabase.addPropertyChangeListener(courseDatabaseModel);
 	}
 	
 	public CourseDatabase getCourseDatabase(){
 		return courseDatabase;
 	}
 
 	public PlanOfStudy getPlan() {
 		return plan;
 	}
 	
 	public void setPlan(PlanOfStudy plan){
 		this.plan = plan;
 		if(semesterPanels != null)
 			setSemesterPanels(semesterPanels);
 		if(planDegreeListModel != null)
 			planDegreeListModel.newPlan(plan);
 	}
 
 	public ListModel getCourseListModel() {
 		return courseDatabaseModel;
 	}
 
 	public void setView(PlanOfStudyEditor planCard) {
 		this.view = planCard;
 		view.setController(this);
 		totalCredits();
 		validatePlan();
 	}
 
 	public void addCourse(int term, Course toAdd) {
 		Term toModify = plan.getTerm(term);
 		toModify.add(toAdd);
 		updateSemesterPanel(term, toModify);
 		totalCredits();
		planDegreeListModel.planChanged();
 		validatePlan();
 	}
 
 	private void updateSemesterPanel(int term, Term model) {
 		ArrayList<Course> courses = model.getCourses();
 
 		JPanel semesterPanel = semesterPanels.get(term);
 		semesterPanel.removeAll();
 		int creditTotal = 0;
 		
 		for(Course c : courses){
 			semesterPanel.add(new CourseDisplay(this, c));
 			creditTotal += c.getCredits();
 		}
 		for(int i = 0; i < (SchoolInformation.getDefaultCoursesPerSemester()-courses.size()); i++){
 			semesterPanel.add(new CourseDisplay(this));
 		}
 		if(model.getYear() == 0){ // AP/transfer credit
 			semesterPanel.setBorder(new TitledBorder("AP/transfer credit ("
 					+ String.valueOf(creditTotal) + ")"));
 		}
 		else{
 			semesterPanel.setBorder(new TitledBorder(model.getTerm().toString()
 					+ " " + String.valueOf(model.getYear()) + " ("
 					+ String.valueOf(creditTotal) + ")"));
 		}
 		
 		semesterPanel.revalidate();
 	}
 
 	public void initializeTerms(int startingYear) {
 		plan.setStartingYear(startingYear);
 		plan.rebuildTerms();
 		for(int i = 0; i < plan.numTerms(); i++)
 		updateSemesterPanel(i, plan.getTerm(i));
 	}
 
 	public void removeCourse(Container semesterPanel, CourseDisplay course) {
 		int semester = 0;
 		int courseIndex = 0;
 		// find the semester
 		while(semesterPanels.get(semester) != semesterPanel)
 			semester++;
 		
 		// find course
 		while(semesterPanel.getComponent(courseIndex) != course)
 			courseIndex++;
 
 		ArrayList<Course> courses = plan.getTerm(semester).getCourses();
 		courses.remove(courseIndex);
 		updateSemesterPanel(semester, plan.getTerm(semester));
 		totalCredits();
		planDegreeListModel.planChanged();
 		validatePlan();
 	}
 
 	private void totalCredits() {
 		int credits = 0;
 		for(Term t : plan.getTerms()){
 			for(Course c : t.getCourses())
 				credits += c.getCredits();
 		}
 		support.firePropertyChange("creditTotal", -1, credits);
 	}
 
 	/**
 	 * Show the details for the specified course in the course details panel
 	 * @param course
 	 */
 	public void setDetailDisplay(Course course) {
 		if(course == null)
 			course = new Course();
 		JPanel courseDetailsPanel = (JPanel) detailsPanel.getComponent(1); 
 		for(Component c : courseDetailsPanel.getComponents()){
 			if(c.getName() == "department"){
 				((JTextField)c).setText(course.getDepartment());
 			}
 			if(c.getName() == "title"){
 				((JTextField)c).setText(course.getTitle());
 			}
 			if(c.getName() == "catalogNumber"){
 				((JTextField)c).setText(course.getCatalogNumber());
 			}
 			if(c.getName() == "description"){
 				((JTextArea)c).setText(course.getDescription());
 			}
 		}
 		((CardLayout)detailsPanel.getLayout()).last(detailsPanel);
 	}
 	
 	/**
 	 * Show the details for the specified degree in the course details panel
 	 * @param course
 	 */
 	public void setDetailDisplay(final Degree degree) {
 		JPanel degreeDetailsPanel = (JPanel) detailsPanel.getComponent(0); 
 		for(Component c : degreeDetailsPanel.getComponents()){
 			if(c.getName() == "name"){
 				((JTextField)c).setText(degree.getName());
 			}
 			if(c instanceof JScrollPane){
 				JList problemsList = (JList) ((JScrollPane)c).getViewport().getView();
 				problemsList.setModel(new AbstractListModel(){
 
 					public Object getElementAt(int index) {
 						return degree.getErrors()[index];
 					}
 
 					public int getSize() {
 						return degree.getErrors().length;
 					}
 				});
 			}
 		}
 		((CardLayout)detailsPanel.getLayout()).first(detailsPanel);
 	}
 
 	public void setDetailsPanel(JPanel detailsPanel) {
 		this.detailsPanel = detailsPanel;
 	}
 
 	public void searchTextChanged(String text) {
 		courseDatabaseModel.setSearchText(text);
 	}
 
 	public void updatePlanFromDatabase() {
 		for(Term t : plan.getTerms()){
 			ArrayList<Course> courses = t.getCourses();
 			for(int j = 0; j < courses.size(); j++){
 				courses.set(j, courseDatabase.getCourse(courses.get(j).getCatalogNumber()));
 			}
 		}
 	}
 
 	public ComboBoxModel getDegreeListModel() {
 		if(degreeListModel == null){
 			degreeListModel = new DegreeListModel(courseDatabase);
 		}
 		return degreeListModel;
 	}
 
 	public void addDegree(Degree toAdd) {
 		plan.getDegrees().add(toAdd);
 		planDegreeListModel.degreeAdded();
 		validatePlan();
 	}
 
 	public void removeDegree(Degree selectedValue) {
 		plan.getDegrees().remove(selectedValue);
 		planDegreeListModel.degreeRemoved();
 		validatePlan();
 	}
 	
 	public void validatePlan(){
 		for(Degree degree : plan.getDegrees()){
 			if(degree.getID() != 0){ // legacy check
 				Degree validationDegree = courseDatabase.getDegree(degree.getID());
 				ValidationError[] errors = PlanValidator.getInstance().validate(plan, validationDegree);
 				degree.setErrors(errors);
 			}
 			else{
 				ValidationError[] errors = PlanValidator.getInstance().validate(plan, degree);
 				degree.setErrors(errors);
 			}
 		}
 		degreeList.revalidate();
 
 		// validate prerequisites, corequisites, and term restructions
 		for(int i = 1; i < plan.numTerms(); i++){
 			Term currentTerm = plan.getTerm(i);
 			int courseIdx = 0;
 			for(Course course : currentTerm.getCourses()){
 				StringBuilder tooltip = new StringBuilder();
 
 				boolean availableInTerm = false;
 				for(YearPart y : course.getAvailableTerms()){
 					if(y == currentTerm.getTerm())
 						availableInTerm = true;
 				}
 				CourseDisplay cd = (CourseDisplay) semesterPanels.get(i).getComponent(courseIdx);
 				cd.setCorequisitesSatisfied(true);
 				cd.setPrerequisitesSatisfied(true);
 				cd.setIsAppropriateTerm(availableInTerm);
 				if(!availableInTerm)
 					tooltip.append("Not offered during "+currentTerm.getTerm().toString()+" term\n");
 				
 				for(Course coreq : course.getCorequisites()){
 					// search within this term and previous terms
 					boolean found = false;
 					for(int j = 0; j <= i; j++){
 						Term testTerm = plan.getTerm(j);
 						for(Course c : testTerm.getCourses()){
 							if(coreq.equals(c))
 								found = true;
 						}
 					}
 					if(!found){
 						tooltip.append("Corequisite not satisfied: "+coreq.toString()+"\n");
 						cd.setCorequisitesSatisfied(false);
 					}
 				}
 				for(Course prereq : course.getPrerequisites()){
 					// search within previous terms
 					boolean found = false;
 					for(int j = 0; j < i; j++){
 						Term testTerm = plan.getTerm(j);
 						for(Course c : testTerm.getCourses()){
 							if(prereq.equals(c))
 								found = true;
 						}
 					}
 					if (!found){
 						tooltip.append("Prerequisite not satisfied: "+prereq.toString()+"\n");
 						cd.setPrerequisitesSatisfied(false);
 					}
 				}
 				cd.setToolTipText(tooltip.toString());
 				courseIdx++;
 			}
 		}
 	}
 
 	public DegreeListModel getPlanDegreeListModel() {
 		if(planDegreeListModel == null)
 			planDegreeListModel = new DegreeListModel(plan);
 		return planDegreeListModel;
 	}
 
 	public void setDegreeList(JList degreeList) {
 		this.degreeList = degreeList;
 	}
 
 	public void addPropertyChangeListener(String propertyName,
 			PropertyChangeListener propertyChangeListener) {
 		support.addPropertyChangeListener(propertyName, propertyChangeListener);
 	}
 }
