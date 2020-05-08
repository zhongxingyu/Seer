 package no.uis.portal.employee;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 
 import com.icesoft.faces.component.ext.HtmlSelectOneMenu;
 
 import no.uis.abam.dom.Application;
 import no.uis.abam.dom.Assignment;
 import no.uis.abam.dom.Department;
 import no.uis.abam.dom.EditableSelectItem;
 import no.uis.abam.dom.Student;
 import no.uis.abam.dom.Thesis;
 import no.uis.abam.ws_abam.AbamWebService;
 
 public class EmployeeService {
 
 	private int selectedDepartmentNumber;
 	private int selectedStudyProgramNumber;
 
 	private String selectedDepartmentName;
 
 	private AbamWebService abamClient;
 
 	private LinkedList<Department> departmentList;
 	private List<EditableSelectItem> selectedStudyProgramList = new LinkedList<EditableSelectItem>();
 
 	private HtmlSelectOneMenu studyProgramMenu;
 
 	
 	private Set<Assignment> assignmentSet;
 	
 	public EmployeeService() {
 	}
 
 	public void saveAssignment(Assignment assignment) {
 		abamClient.saveAssignment(assignment);
 	}
 
 	public void actionPrepareDisplayAssignments(ActionEvent event) {
 		getActiveAssignmentsSet();
 		setSelectedStudyProgramNumber(0);
 		setSelectedDepartmentNumber(0);
 		getDepartmentListFromWebService();
 	}
 
 	public void actionUpdateStudyProgramList(ValueChangeEvent event) {
 		setSelectedDepartmentAndStudyProgramFromValue(Integer.parseInt(event.getNewValue().toString()));
		if(studyProgramMenu != null) studyProgramMenu.setValue(getSelectedStudyProgramNumber());
 		for (Assignment assignment : assignmentSet) {
 			if (assignment.getDepartmentName().equals(selectedDepartmentName)
 					|| selectedDepartmentName.equals(""))
 				assignment.setDisplayAssignment(true);
 			else
 				assignment.setDisplayAssignment(false);
 		}
 		setAllEditExternalExaminerToFalse();
 	}
 
	public void setSelectedDepartmentAndStudyProgramFromValue(int value) {
 		setSelectedDepartmentNumber(value);
 		Department selectedDepartment = getDepartmentFromValue(selectedDepartmentNumber);
 		setSelectedDepartmentName(selectedDepartment.getLabel());
 		setSelectedStudyProgramNumber(0);
 		setSelectedStudyProgramList(selectedDepartment.getStudyPrograms());		
 
 	}
 	
 	public void actionUpdateStudyProgramListFromCreateAssignment(
 			ValueChangeEvent event) {
 		selectedDepartmentNumber = Integer.parseInt(event.getNewValue()
 				.toString());
 		getStudyProgramListFromSelectedDepartment();
 
 	}
 
 	private void getStudyProgramListFromSelectedDepartment() {
 		selectedStudyProgramList = getDepartmentFromValue(
 				selectedDepartmentNumber).getStudyPrograms();
 	}
 
 	public void actionSetDisplayAssignment(ValueChangeEvent event) {
 
 		if (event.getNewValue() == null) {
 			selectedStudyProgramNumber = 0;
 		} else {
 			selectedStudyProgramNumber = Integer.parseInt(event.getNewValue()
 					.toString());
 		}
 		setDisplayAssignments();
 		setAllEditExternalExaminerToFalse();
 	}
 	
 	public void setDisplayAssignments() {
 		String selectedStudyProgram = getStudyProgramNameFromValue(selectedStudyProgramNumber);
 		
 		
 		//TreeSet<Assignment> assignmentList = abamClient.getAllAssignments();
 		
 		if (selectedDepartmentName == null)
 			setSelectedDepartmentName("");
 		if (selectedStudyProgram == null)
 			selectedStudyProgram = "";
 		for (Assignment assignment : assignmentSet) {
 			if (assignmentShouldBeDisplayed(assignment,
 					selectedStudyProgram))
 				assignment.setDisplayAssignment(true);
 			else
 				assignment.setDisplayAssignment(false);
 		}
 		//abamClient.setAssignmentList(assignmentSet);
 	}
 		
 	private boolean assignmentShouldBeDisplayed(Assignment assignmentIn,
 			String selectedStudyProgram) {
 		return (selectedStudyProgram.equals("") && assignmentIn
 				.getDepartmentName().equals(selectedDepartmentName))
 				|| assignmentIn.getStudyProgramName().equals(
 						selectedStudyProgram)
 				|| getSelectedDepartmentName().equals("");
 	}
 
 	public void setAllEditExternalExaminerToFalse() {
 		// TODO Only get logged in teacher's students theses
 		for (Thesis thesis : abamClient.getThesisList()) {
 			thesis.setEditExternalExaminer(false);
 		}
 	}
 
 	public void addNewDepartment() {
 		Department newDepartment = new Department(new Integer(
 				departmentList.size()), "");
 		newDepartment.setEditable(true);
 		departmentList.add(newDepartment);
 	}
 
 	public void addThesesFromList(List<Thesis> thesesToAdd) {
 		abamClient.addThesesFromList(thesesToAdd);
 	}
 
 	public void updateThesis(Thesis thesisToUpdate) {
 		abamClient.updateThesis(thesisToUpdate);
 	}
 
 	public void removeDepartment(EditableSelectItem department) {
 		departmentList.remove(department);
 	}
 
 	public void saveDepartmentListToWebService() {
 		abamClient.setDepartmentList(departmentList);
 	}
 
 	public String getDepartmentNameFromIndex(int index) {
 		return departmentList.get(index).getLabel();
 	}
 
 	public Department getDepartmentFromValue(int value) {
 		for (Department department : departmentList) {
 			if (Integer.parseInt(department.getValue().toString()) == value) {
 				return department;
 			}
 		}
 		return null;
 	}
 
 	public String getStudyProgramNameFromValue(int value) {
 		for (EditableSelectItem studyProgram : selectedStudyProgramList) {
 			if (Integer.parseInt(studyProgram.getValue().toString()) == value) {
 				return studyProgram.getLabel();
 			}
 		}
 		return null;
 	}
 
 	public Student getStudentFromStudentNumber(long studentNumber) {
 		return abamClient.getStudentFromStudentNumber(studentNumber);
 	}
 
 	public void removeAssignment(Assignment assignment) {
 		abamClient.removeAssignment(assignment);
 	}
 
 	public void removeApplication(Application application) {
 		abamClient.removeApplication(application);
 	}
 
 	public String getSelectedStudyProgramNameFromIndex(int index) {
 		return selectedStudyProgramList.get(index).getLabel();
 	}
 
 	public void setSelectedStudyProgramListFromDepartmentNumber(
 			int departmentNumber) {
 		setSelectedStudyProgramList(getDepartmentFromValue(departmentNumber)
 				.getStudyPrograms());
 	}
 
 	public List<Application> getMasterApplicationList() {
 		return abamClient.getMasterApplicationList();
 	}
 
 	public List<Application> getBachelorApplicationList() {
 		return abamClient.getBachelorApplicationList();
 	}
 
 	public Assignment getAssignmentFromId(int id) {
 		return abamClient.getAssignmentFromId(id);
 	}
 
 	public void getDepartmentListFromWebService() {
 		departmentList = abamClient.getDepartmentList();
 	}
 
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
 
 	public int getNextId() {
 		return abamClient.getNextId();
 	}
 
 	public String getSelectedDepartmentName() {
 		return selectedDepartmentName;
 	}
 
 	public void setSelectedDepartmentName(String selectedDepartment) {
 		this.selectedDepartmentName = selectedDepartment;
 	}
 
 	public List<EditableSelectItem> getSelectedStudyProgramList() {
 		return selectedStudyProgramList;
 	}
 
 	public void setSelectedStudyProgramList(List<EditableSelectItem> list) {
 		this.selectedStudyProgramList = list;
 	}
 
 	public List<Thesis> getThesisList() {
 		return abamClient.getThesisList();
 	}
 
 	public List<Department> getDepartmentList() {
 		return departmentList;
 	}
 
 	public Set<Assignment> getAssignmentSet() {
 		return assignmentSet;
 	}
 
 	public void setAssignmentSet(Set<Assignment> assignmentSet) {
 		this.assignmentSet = assignmentSet;
 	}
 
 	public Set<Assignment> getAllAssignmentsSet() {
 		assignmentSet = abamClient.getAllAssignments();
 		return assignmentSet;
 	}
 	
 	public HtmlSelectOneMenu getStudyProgramMenu() {
 		return studyProgramMenu;
 	}
 
 	public void setStudyProgramMenu(HtmlSelectOneMenu studyProgramMenu) {
 		this.studyProgramMenu = studyProgramMenu;
 	}
 
 	public Set<Assignment> getActiveAssignmentsSet() {
 		assignmentSet = abamClient.getActiveAssignments();
 		return assignmentSet;
 	}
 
 	public void setAbamClient(AbamWebService abamClient) {
 		this.abamClient = abamClient;
 	}
 
 }
