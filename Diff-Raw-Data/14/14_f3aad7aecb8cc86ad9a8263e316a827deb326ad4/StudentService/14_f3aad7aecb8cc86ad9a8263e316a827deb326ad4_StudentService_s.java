 package no.uis.portal.student;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 
 import com.icesoft.faces.component.ext.HtmlDataTable;
 
 
 import no.uis.abam.dom.*;
 import no.uis.abam.ws_abam.AbamWebService;
 
 public class StudentService {
 
 	private static final String LANGUAGE = "language";
 	private static final String NORWEGIAN_LANGUAGE = "Norsk";
 	
 	private TreeSet<Assignment> assignmentList; 
 	private Assignment selectedAssignment;
 	
 	private Student currentStudent;
 
 	private AbamWebService abamStudentClient;
 	
 	private Application[] tempApplicationPriorityArray = new Application[3];
 	private ArrayList<Application> applicationsToRemove = new ArrayList<Application>();
 	private ArrayList<SelectItem> departmentSelectItemList = new ArrayList<SelectItem>();
 	private ArrayList<SelectItem> studyProgramSelectItemList = new ArrayList<SelectItem>();
 	private List<Department> departmentList;
 	
 	private int selectedStudyProgramNumber;
 	
 	private long testStudentNumber = 123456;
 	
 	FacesContext context;
 	Locale locale;
     ResourceBundle res;
 	
 	public StudentService() {
 		context  = FacesContext.getCurrentInstance();
 		locale = context.getViewRoot().getLocale();
 		res = ResourceBundle.getBundle("Language", locale);
 	}
 
 	public void setCurrentStudentFromLoggedInUser(){
 		currentStudent = new Student();
 		currentStudent.setName("Bachelor Studenten");
 		currentStudent.setBachelor(true);
 		currentStudent.setStudentNumber(123456);
 		currentStudent.setDepartmentCode("TN-IDE");
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
 			assignmentList = abamStudentClient.getAssignmentsFromDepartmentCode(getCurrentStudent().getDepartmentCode());
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
 	}
 	
 	public void updateStudyProgramList(int index){
 		Set<Assignment> assignmentList = getAssignmentList();
 		if (assignmentList != null) {
 			for (Assignment assignment : assignmentList) {
 				if (assignment.getDepartmentCode().equals(currentStudent.getDepartmentCode())) { 
 					if(currentStudentIsEligibleForAssignment(assignment)) {
 						assignment.setDisplayAssignment(true);
 						String depName = getDepartmentNameFromCode(assignment.getDepartmentCode());
 						assignment.setDepartmentName(depName);
 					}
 					else assignment.setDisplayAssignment(false);
 				} else assignment.setDisplayAssignment(false);
 			}
 		}
 	}
 	
 	private String getDepartmentNameFromCode(String code) {
 		String language = res.getString(LANGUAGE);
 		for (Department dep : departmentList) {
 			if (dep.getOeKode() != null && dep.getOeKode().equals(code)) {
 				if (language.equals(NORWEGIAN_LANGUAGE)) {
 					return dep.getOeNavn_Bokmaal();
 				} else {
 					return dep.getOeNavn_Engelsk();
 				}
 			}
 		}
 		return "";
 	}
 	
 	private boolean currentStudentIsEligibleForAssignment(Assignment assignment){
 		return assignment.getType().equalsIgnoreCase(getCurrentStudent().getType());
 	}
 	
 	public void actionSetDisplayAssignment(ValueChangeEvent event){
 		String selectedStudyProgram = (String) getStudyProgramName(Integer.parseInt(event.getNewValue().toString()));
 		assignmentList = getAssignmentList();
 		selectedStudyProgramNumber = Integer.parseInt(event.getNewValue().toString());
		for (Assignment assignment : assignmentList) {
			if (checkIfAssignmentShouldBeDisplayed(assignment, selectedStudyProgram)){ 
				if(currentStudentIsEligibleForAssignment(assignment))
					assignment.setDisplayAssignment(true);
			} else assignment.setDisplayAssignment(false);
 		}		
 	}
 	
 	private boolean checkIfAssignmentShouldBeDisplayed(Assignment abIn, String selectedStudyProgram) {
 		return (selectedStudyProgram.equals("") && abIn.getDepartmentCode().equals(currentStudent.getDepartmentCode())) 
 		|| abIn.getStudyProgramName().equals(selectedStudyProgram);
 	}
 	
 	
 	public void updateSelectedAssignmentInformation(Assignment selectedAssignment){
 		setSelectedAssignment(selectedAssignment);
 		//setStudyProgramListFromDepartmentNumber(selectedAssignment.getDepartmentNumber());
 		
 		setSelectedStudyProgramNumber(selectedAssignment.getStudyProgramNumber());
 	}
 	
 	
 	public void actionPrepareAvailableAssignments(ActionEvent event) {		
 		updateCurrentStudentFromWebService();
 		assignmentList = abamStudentClient.getAssignmentsFromDepartmentCode(getCurrentStudent().getDepartmentCode());			
 		updateStudyProgramList(findDepartmentNumberForCurrentStudent());		
 		getStudyProgramList();
 	}
 	
 	public int findDepartmentNumberForCurrentStudent() {
 		String code = getCurrentStudent().getDepartmentCode();
 		List<Department> tempList = getDepartmentList();
 		for (Department department : tempList) {
 			if(department.getOeKode().equalsIgnoreCase(code)) 
 				return tempList.indexOf(department);
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
 	
 	public List<Department> getDepartmentList() {
 		if(departmentList == null || departmentList.isEmpty()) {
 			departmentList = abamStudentClient.getDepartmentList();
 			for (int i = 0; i < departmentList.size(); i++) {
 				if(departmentList.get(i).getOeKode().equals(currentStudent.getDepartmentCode())) {
 					if(res.getString(LANGUAGE).equals(NORWEGIAN_LANGUAGE)) {
 						departmentSelectItemList.add(new SelectItem(i,departmentList.get(i).getOeNavn_Bokmaal()));
 					} else departmentSelectItemList.add(new SelectItem(i,departmentList.get(i).getOeNavn_Engelsk()));
 				}	
 			}
 		}
 		return departmentList;
 	}
 
 	public List<StudyProgram> getStudyProgramList() {
 		List<StudyProgram> studyProgramList = abamStudentClient.getStudyProgramList(findDepartmentNumberForCurrentStudent());
 		studyProgramSelectItemList.clear();
 		for (int i = 0; i < studyProgramList.size(); i++) {
 			studyProgramSelectItemList.add(new SelectItem(i,studyProgramList.get(i).getName()));
 		}
 		return studyProgramList;
 	}
 	
 	public String getStudyProgramName(int index) {
 		return abamStudentClient.getStudyProgram(findDepartmentNumberForCurrentStudent(),index);
 	}
 	
 	public String getDepartmentNameFromIndex(int index) {
 		if(res.getString(LANGUAGE).equals(NORWEGIAN_LANGUAGE)) {
 			return departmentList.get(index).getOeNavn_Bokmaal();
 		}
 		return departmentList.get(index).getOeNavn_Engelsk();
 	}
 	
 	public String getDepartmentCodeFromIndex(int index) {		
 		return departmentList.get(index).getOeKode();
 
 	}
 
 	
 	public void removeAssignment(Assignment assignment) {
 		abamStudentClient.removeAssignment(assignment);
 	}
 
 	
 //	public void setStudyProgramListFromDepartmentNumber(int departmentNumber) {
 //		setStudyProgramList(getAllStudyProgramsByDepartmentsList().
 //				get(departmentNumber));
 //	}
 //	
 	
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
 
 	public ArrayList<SelectItem> getDepartmentSelectItemList() {
 		return departmentSelectItemList;
 	}
 
 	public void setDepartmentSelectItemList(
 			ArrayList<SelectItem> departmentSelectItemList) {
 		this.departmentSelectItemList = departmentSelectItemList;
 	}
 
 	public ArrayList<SelectItem> getStudyProgramSelectItemList() {
 		return studyProgramSelectItemList;
 	}
 
 	public void setStudyProgramSelectItemList(
 			ArrayList<SelectItem> studyProgramSelectItemList) {
 		this.studyProgramSelectItemList = studyProgramSelectItemList;
 	}
 	
 	
 }
 
 
