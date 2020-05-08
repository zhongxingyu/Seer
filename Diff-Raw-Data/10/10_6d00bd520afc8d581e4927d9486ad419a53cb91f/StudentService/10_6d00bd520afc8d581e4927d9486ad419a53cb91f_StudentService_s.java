 package no.uis.portal.student;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.faces.component.UIComponent;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 
 import com.icesoft.faces.component.ext.HtmlDataTable;
 
 
 import no.uis.abam.dom.*;
 import no.uis.abam.ws_abam.AbamWebService;
 
 public class StudentService {
 
 	private TreeSet<Assignment> assignmentList; 
 	private Assignment selectedAssignment;
 	
 	private Student currentStudent;
 
 	private AbamWebService abamStudentClient;
 	
 	private Application[] tempApplicationPriorityArray = new Application[3];
 	private ArrayList<Application> applicationsToRemove = new ArrayList<Application>();
 	
 	private ArrayList<Application> applicationList = new ArrayList<Application>();
 	
 	private String selectedDepartmentName;
 	
 	private int selectedDepartmentNumber;
 	private int selectedStudyProgramNumber;
 	
 	private long testStudentNumber = 123456;
 	
 	public StudentService() {
 	}
 
 	public void setCurrentStudentFromLoggedInUser(){
 		currentStudent = new Student();
 		currentStudent.setName("Bachelor Studenten");
 		currentStudent.setBachelor(true);
 		currentStudent.setStudentNumber(123456);
 		currentStudent.setDepartmentName("Data- og elektroteknikk");
 		currentStudent.setStudyProgramName("Elektro");
 	}
 
 //	public void setCurrentStudentFromLoggedInUser(){
 //		currentStudent = new MasterStudent();
 //		currentStudent.setName("Studenten");
 //		currentStudent.setDepartment("Petroleumsteknologi");
 //		currentStudent.setStudyProgram("Boreteknologi");
 //	}
 	
 	
 	public int getNextId(){
 		return abamStudentClient.getNextId();
 	}
 	
 	
 	public void saveAssignment(Assignment assignment) {
 		getCurrentStudent().setCustomAssignment(assignment);
 	}
 	
 	public void setApplicationToStudent(Application application){
 		currentStudent.addApplication(application);
 		abamStudentClient.updateStudent(currentStudent);
 	}
 	
 	public TreeSet<Assignment> getAssignmentList() {
 		if(assignmentList == null) 
 			assignmentList = abamStudentClient.getAssignmentsFromDepartmentName(getCurrentStudent().getDepartmentName());
 		return assignmentList;		
 	}
 
 	
 	public Assignment getSelectedAssignment() {
 		return selectedAssignment;
 	}
 
 	
 	public void setSelectedAssignment(Assignment selectedAssignment) {
 		this.selectedAssignment = selectedAssignment;
 	}
 	
 	public void actionRemoveApplication(ActionEvent event) {
 		Application application = getApplicationFromEvent(event);
 		applicationsToRemove.add(application);
 		removeApplication(application);
 	}
 	
 	public void actionSetApplicationPriorityHigher(ActionEvent event) {
 		Application application = getApplicationFromEvent(event);
 		moveApplicationHigher(application);
 	}
 	
 	public void actionSetApplicationPriorityLower(ActionEvent event) {
 		Application application = getApplicationFromEvent(event);
 		moveApplicationLower(application);
 	}
 	
 	private Application getApplicationFromEvent(ActionEvent event) {
 		UIComponent uic = event.getComponent();
 		HtmlDataTable table = (HtmlDataTable)uic.getParent().getParent();
 		return (Application)table.getRowData();
 	}
 	 
 	public void actionGetApplicationFromStudent(ActionEvent event) {
 		tempApplicationPriorityArray = getCurrentStudent().getApplicationPriorityArray().clone();
 	}
 	
 	public void actionClearStudyProgramAndDepartmentNumber(ActionEvent event){
 		setSelectedStudyProgramNumber(0);
 		setSelectedDepartmentNumber(0);
 	}
 	
 	
 	public void updateStudyProgramList(int index){
 		selectedDepartmentName = (String) getDepartmentName(index);
 		selectedDepartmentNumber = index;
 		Set<Assignment> assignmentList = getAssignmentList();
 		for (Assignment assignment : assignmentList) {
 			if (assignment.getDepartmentName().equals(selectedDepartmentName)
 				|| selectedDepartmentName.equals("")) { 
 				if(currentStudentIsEligibleForAssignment(assignment))
 					assignment.setDisplayAssignment(true);
 				else assignment.setDisplayAssignment(false);
 			} else assignment.setDisplayAssignment(false);
 		}
 		setAllEditExternalExaminerToFalse();
 	}
 	
 	private boolean currentStudentIsEligibleForAssignment(Assignment assignment){
 		return assignment.getType().equalsIgnoreCase(getCurrentStudent().getType());
 	}
 	
 	
 //	public void actionUpdateStudyProgramListFromCreateAssignment(ValueChangeEvent event){
 //		studyProgramList = allStudyProgramsByDepartmentList.get(Integer.parseInt(event.getNewValue().toString()));
 //		selectedDepartmentNumber = Integer.parseInt(event.getNewValue().toString());
 //	}
 	
 	
 	public void actionSetDisplayAssignment(ValueChangeEvent event){
 		String selectedStudyProgram = (String) getStudyProgramName(Integer.parseInt(event.getNewValue().toString()));
 		assignmentList = getAssignmentList();
 		selectedStudyProgramNumber = Integer.parseInt(event.getNewValue().toString());
 		if (selectedDepartmentName == null) setSelectedDepartment("");
 		for (Assignment assignment : assignmentList) {
 			if (checkIfAssignmentShouldBeDisplayed(assignment, selectedStudyProgram)){ 
 				if(currentStudentIsEligibleForAssignment(assignment))
 					assignment.setDisplayAssignment(true);
 			} else assignment.setDisplayAssignment(false);
 		}
 		setAllEditExternalExaminerToFalse();
 	}
 	
 	private boolean checkIfAssignmentShouldBeDisplayed(Assignment abIn, String selectedStudyProgram) {
 		return (selectedStudyProgram.equals("") && abIn.getDepartmentName().equals(selectedDepartmentName)) 
 		|| abIn.getStudyProgramName().equals(selectedStudyProgram);
 	}
 	
 	
 	public void updateSelectedAssignmentInformation(Assignment selectedAssignment){
 		setSelectedAssignment(selectedAssignment);
 		//setStudyProgramListFromDepartmentNumber(selectedAssignment.getDepartmentNumber());
 		
 		setSelectedDepartmentNumber(selectedAssignment.getDepartmentNumber());
 		setSelectedStudyProgramNumber(selectedAssignment.getStudyProgramNumber());
 	}
 	
 	
 	public void actionPrepareAvailableAssignments(ActionEvent event) {		
 		assignmentList = abamStudentClient.getAssignmentsFromDepartmentName(getCurrentStudent().getDepartmentName());
		updateStudyProgramList(findDepartmentNumberForCurrentStudent());
 	}
 	
 	private int findDepartmentNumberForCurrentStudent() {
 		String name = getCurrentStudent().getDepartmentName();
 		for (SelectItem department : getDepartmentList()) {
 			if(department.getLabel().equalsIgnoreCase(name)) return Integer.parseInt(department.getValue().toString());
 		}
 		return 0;
 	}
 	
 	public void actionSaveApplications(ActionEvent event) {
 		removeDeletedApplications();
 		getCurrentStudent().setApplicationPriorityArray(tempApplicationPriorityArray);
 		abamStudentClient.updateApplicationsFromCurrentStudent(tempApplicationPriorityArray);		
 	}
 	
 	private void removeDeletedApplications() {
 		for (Application application : applicationsToRemove) {
 			abamStudentClient.removeApplication(application);
 		}
 	}
 	
 	public void actionClearDeletedElements(ActionEvent event){
 		applicationsToRemove.clear();
 	}
 	
 	public void setAllEditExternalExaminerToFalse() {
 		for (Assignment assignment : getAssignmentList()) {
 			assignment.setEditExternalExaminer(false);
 		}
 	}
 	
 	
 	public LinkedList<Department> getDepartmentList() {
 		return abamStudentClient.getDepartmentList();
 	}
 
 	public List<EditableSelectItem> getStudyProgramList() {
 		return abamStudentClient.getStudyProgramList(selectedDepartmentNumber);
 	}
 	
 	public String getStudyProgramName(int index) {
 		return abamStudentClient.getStudyProgram(selectedDepartmentNumber,index);
 	}
 	
 	public String getDepartmentName(int index) {
 		return  abamStudentClient.getDepartmentList().get(index).getLabel();
 	}
 
 	
 	public String getSelectedDepartment() {
 		return selectedDepartmentName;
 	}
 
 	
 	public void setSelectedDepartment(String selectedDepartment) {
 		this.selectedDepartmentName = selectedDepartment;
 	}
 
 	
 	public void removeAssignment(Assignment assignment) {
 		abamStudentClient.removeAssignment(assignment);
 	}
 
 	
 //	public void setStudyProgramListFromDepartmentNumber(int departmentNumber) {
 //		setStudyProgramList(getAllStudyProgramsByDepartmentsList().
 //				get(departmentNumber));
 //	}
 //	
 	
 	public int getSelectedDepartmentNumber() {
 		return selectedDepartmentNumber;
 	}
 
 	
 	public void setSelectedDepartmentNumber(int selectedDepartmentNumber) {
 		this.selectedDepartmentNumber = selectedDepartmentNumber;
 	}
 
 	
 	public int getSelectedStudyProgramNumber() {
 		return selectedStudyProgramNumber;
 	}
 
 	
 	public void setSelectedStudyProgramNumber(int selectedStudyProgramNumber) {
 		this.selectedStudyProgramNumber = selectedStudyProgramNumber;
 	}
 
 	public Student getCurrentStudent() {
 		if (currentStudent == null) {
 			currentStudent = abamStudentClient.getStudentFromStudentNumber(testStudentNumber);
 		}
 		return currentStudent;
 	}
 
 	public void setCurrentStudent(Student currentStudent) {
 		this.currentStudent = currentStudent;
 	}
 	
 	public void updateStudentInWebServiceFromCurrentStudent() {
 		abamStudentClient.updateStudent(currentStudent);
 	}
 	
 	public void updateCurrentStudentFromWebService() {
 		currentStudent = abamStudentClient.getStudentFromStudentNumber(testStudentNumber);
 	}
 
 	public List<Application> getApplicationList() {
 		return abamStudentClient.getApplicationList();
 	}
 
 	public void setApplicationList(ArrayList<Application> applicationList) {
 		abamStudentClient.setApplicationList(applicationList);
 	}
 
 	public void saveApplication(Application application) {
 		abamStudentClient.saveApplication(application);
 	}
 
 	public void setAbamStudentClient(AbamWebService abamStudentClient) {
 		this.abamStudentClient = abamStudentClient;
 	}
 	
 	public void removeApplication(Application application) {
 		for (int index = 0; index < tempApplicationPriorityArray.length; index++) {
 			if(tempApplicationPriorityArray[index] == application){ 				
 				tempApplicationPriorityArray[index] = null;
 				for (int j = index + 1; j < tempApplicationPriorityArray.length; j++) {
 					if(tempApplicationPriorityArray[j] != null){						
 						moveApplicationHigher(tempApplicationPriorityArray[j]);
 					}
 				}
 			}
 		}
 	}
 	
 	public void moveApplicationHigher(Application selectedApplication) {
 		int selectedApplicationIndex = findApplicationIndex(selectedApplication);
 		if(selectedApplicationIndex > 0) {
 			int higherApplicationIndex = selectedApplicationIndex - 1;
 			Application higherApplication = tempApplicationPriorityArray[higherApplicationIndex];
 			if(selectedApplication != null){
 				selectedApplication.setPriority(higherApplicationIndex + 1);
 				if(higherApplication != null){
 					higherApplication.setPriority(selectedApplicationIndex + 1);
 				}
 				tempApplicationPriorityArray[higherApplicationIndex] = selectedApplication;
 				tempApplicationPriorityArray[selectedApplicationIndex] = higherApplication;								
 					
 			}
 		}
 	}
 	public void moveApplicationLower(Application selectedApplication) {
 		int selectedApplicationIndex = findApplicationIndex(selectedApplication);
 		if(selectedApplicationIndex != 2) {
 			int lowerApplicationIndex = selectedApplicationIndex + 1;
 			Application lowerApplication = tempApplicationPriorityArray[lowerApplicationIndex];
 			if(lowerApplication != null) {
 				lowerApplication.setPriority(selectedApplicationIndex + 1);
 				tempApplicationPriorityArray[lowerApplicationIndex] = selectedApplication;
 				tempApplicationPriorityArray[selectedApplicationIndex] = lowerApplication;
 				if (selectedApplication != null) {
 					selectedApplication.setPriority(lowerApplicationIndex + 1);
 				}
 			}
 		}
 	}
 	private int findApplicationIndex(Application application) {
 		for (int index = 0; index < tempApplicationPriorityArray.length; index++) {
 			if(tempApplicationPriorityArray[index] == application) return index;
 		}
 		return -1;
 	}
 
 	public Application[] getTempApplicationPriorityArray() {
 		return tempApplicationPriorityArray;
 	}
 
 	public Assignment getAssignmentFromId(int assignedAssignmentId) {
 		return abamStudentClient.getAssignmentFromId(assignedAssignmentId);
 	}
 	
 	
 }
 
 
