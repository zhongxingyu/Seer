 package no.uis.portal.employee;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.TreeSet;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 
 
 import no.uis.portal.employee.domain.AssigmentIdComparator;
 import no.uis.portal.employee.domain.Assignment;
 import no.uis.portal.employee.domain.ExternalExaminer;
 
 public class EmployeeServiceImpl implements EmployeeService {
 
 	private TreeSet<Assignment> assignmentList = new TreeSet<Assignment>(new AssigmentIdComparator()); 
 	private EmployeeService selectedAssignment;
 	
 
 	private LinkedList<SelectItem> departmentList;
 	private LinkedList<SelectItem> studyProgramList = new LinkedList<SelectItem>();
 	private LinkedList<LinkedList<SelectItem>> allStudyProgramsByDepartmentList;
 	
 	private String selectedDepartment;
 	
 	private int selectedDepartmentNumber;
 	private int selectedStudyProgramNumber;
 	
 	public EmployeeServiceImpl() {
 		if(departmentList == null) {
 			initializeDepartmentAndStudyProgramLists();
 			createTestData();
 		}
 	}
 	
 	@Override
 	public int getSelectedDepartmentNumber() {
 		return selectedDepartmentNumber;
 	}
 
 	@Override
 	public void setSelectedDepartmentNumber(int selectedDepartmentNumber) {
 		this.selectedDepartmentNumber = selectedDepartmentNumber;
 	}
 
 	@Override
 	public int getSelectedStudyProgramNumber() {
 		return selectedStudyProgramNumber;
 	}
 
 	@Override
 	public void setSelectedStudyProgramNumber(int selectedStudyProgramNumber) {
 		this.selectedStudyProgramNumber = selectedStudyProgramNumber;
 	}
 
 	private void initializeDepartmentAndStudyProgramLists(){
 		departmentList = new LinkedList<SelectItem>();
 		allStudyProgramsByDepartmentList = new LinkedList<LinkedList<SelectItem>>();
 		
 		departmentList.add(new EditableSelectItem(new Integer(0), ""));
 		departmentList.add(new EditableSelectItem(new Integer(1), "Institutt for industriell konomi, risikostyring og planlegging"));
 		departmentList.add(new EditableSelectItem(new Integer(2), "Petroleumsteknologi"));
 		departmentList.add(new EditableSelectItem(new Integer(3), "Data- og elektroteknikk"));
 		departmentList.add(new EditableSelectItem(new Integer(4), "Institutt for konstruksjonsteknikk og materialteknologi"));
 		departmentList.add(new EditableSelectItem(new Integer(5), "Matematikk og naturvitskap"));
 		
 		LinkedList<SelectItem> listToAdd = new LinkedList<SelectItem>();
 		listToAdd.add(new EditableSelectItem(new Integer(0), ""));
 		allStudyProgramsByDepartmentList.add(listToAdd);
 		
 		listToAdd = new LinkedList<SelectItem>();
 		listToAdd.add(new EditableSelectItem(new Integer(0), ""));
 		listToAdd.add(new EditableSelectItem(new Integer(1), "Industriell konomi"));
 		allStudyProgramsByDepartmentList.add(listToAdd);
 		
 		listToAdd = new LinkedList<SelectItem>();
 		listToAdd.add(new EditableSelectItem(new Integer(0), ""));
 		listToAdd.add(new EditableSelectItem(new Integer(1), "Boreteknologi"));
 		listToAdd.add(new EditableSelectItem(new Integer(2), "Petroleumsgeologi"));
 		allStudyProgramsByDepartmentList.add(listToAdd);
 		
 		listToAdd = new LinkedList<SelectItem>();
 		listToAdd.add(new EditableSelectItem(new Integer(0), ""));
 		listToAdd.add(new EditableSelectItem(new Integer(1), "Data"));
 		listToAdd.add(new EditableSelectItem(new Integer(2), "Elektro"));
 		listToAdd.add(new EditableSelectItem(new Integer(3), "Informasjonsteknologi"));
 		allStudyProgramsByDepartmentList.add(listToAdd);
 		
 		listToAdd = new LinkedList<SelectItem>();
 		listToAdd.add(new EditableSelectItem(new Integer(0), ""));
 		listToAdd.add(new EditableSelectItem(new Integer(1), "Byggeteknikk"));
 		listToAdd.add(new EditableSelectItem(new Integer(2), "Maskinteknikk"));
 		listToAdd.add(new EditableSelectItem(new Integer(3), "Offshoreteknologi"));
 		allStudyProgramsByDepartmentList.add(listToAdd);
 		
 		listToAdd = new LinkedList<SelectItem>();
 		listToAdd.add(new EditableSelectItem(new Integer(0), ""));
 		listToAdd.add(new EditableSelectItem(new Integer(1), "Matematikk"));
 		listToAdd.add(new EditableSelectItem(new Integer(2), "Fysikk"));
 		allStudyProgramsByDepartmentList.add(listToAdd);
 		studyProgramList = allStudyProgramsByDepartmentList.get(0);
 	}
 	
 	public void createTestData(){
 		Assignment test1 = new Assignment();
 		test1.setTitle("Pet Bor oppgave");
 		test1.setBachelor(true);
 		test1.setDescription("Beskrivelse av test1");
 		test1.setNumberOfStudents("2-3");
 		test1.setId(1);
 		test1.setDepartment("Petroleumsteknologi");
 		test1.setDepartmentNumber(2);
 		test1.setStudyProgram("Boreteknologi");
 		test1.setStudyProgramNumber(1);
 		test1.setFacultySupervisor("Louis Lane");
 		test1.getSupervisorList().get(0).setName("Superman");
 		test1.setExternalExaminer(new ExternalExaminer("tester"));
 		test1.setAddedDate(new GregorianCalendar(10, 11, 10));
 		GregorianCalendar dato = test1.getAddedDate();
 		dato.add(Calendar.MONTH, 6);
 		test1.setExpireDate(dato);
 		
 		Assignment test2 = new Assignment();
 		test2.setTitle("IDE El oppgave");
 		test2.setBachelor(false);
 		test2.setMaster(true);
 		test2.setDescription("Beskrivelse av test2");
 		test2.setNumberOfStudents("1");
 		test2.setDepartment("Data- og elektroteknikk");
 		test2.setDepartmentNumber(3);
 		test2.setStudyProgram("Elektro");
 		test2.setStudyProgramNumber(2);
 		test2.setId(2);
 		test2.setFacultySupervisor("Robin");
 		test2.getSupervisorList().get(0).setName("Batman");
 		test2.setAddedDate(new GregorianCalendar(2010, 10, 10));
 		dato = test2.getAddedDate();
 		dato.add(Calendar.MONTH, 6);
 		test2.setExpireDate(dato);
 		assignmentList.add(test1);
 		assignmentList.add(test2);
 	}
 
 	@Override
 	public int getNextId(){
 		return assignmentList.size()+1;
 	}
 	
 	@Override
 	public void saveAssignment(Assignment assignment) {
 		assignmentList.add(assignment);
 	}
 	
 	@Override
 	public TreeSet<Assignment> getAssignmentList() {
 		return assignmentList;
 	}
 
 	@Override
 	public EmployeeService getSelectedAssignment() {
 		return selectedAssignment;
 	}
 
 	@Override
 	public void setSelectedAssignment(EmployeeService selectedAssignment) {
 		this.selectedAssignment = selectedAssignment;
 	}
 	
 	@Override
 	public void actionClearStudyProgramAndDepartmentNumber(ActionEvent event){
 		setSelectedStudyProgramNumber(0);
 		setSelectedDepartmentNumber(0);
 	}
 	
 	@Override
 	public void actionUpdateStudyProgramList(ValueChangeEvent event){
 		studyProgramList = allStudyProgramsByDepartmentList.get(Integer.parseInt(event.getNewValue().toString()));
 		selectedDepartment = (String) departmentList.get(Integer.parseInt(event.getNewValue().toString())).getLabel();
 		selectedDepartmentNumber = Integer.parseInt(event.getNewValue().toString());
 		TreeSet<Assignment> assignmentList = getAssignmentList();
 		for (Assignment assignment : assignmentList) {
 			if (assignment.getDepartment().equals(selectedDepartment)
 				|| selectedDepartment.equals("")) 
 				assignment.setDisplayAssignment(true);
 			else assignment.setDisplayAssignment(false);
 		}
		setAllEditExternalExaminerToFalse();
 	}
 	
 	@Override
 	public void actionUpdateStudyProgramListFromCreateAssignment(ValueChangeEvent event){
 		studyProgramList = allStudyProgramsByDepartmentList.get(Integer.parseInt(event.getNewValue().toString()));
 		selectedDepartmentNumber = Integer.parseInt(event.getNewValue().toString());
 	}
 	
 	@Override
 	public void actionSetDisplayAssignment(ValueChangeEvent event){
 		String selectedStudyProgram = (String) studyProgramList.get(Integer.parseInt(event.getNewValue().toString())).getLabel();
 		TreeSet<Assignment> assignmentList = getAssignmentList();
 		selectedStudyProgramNumber = Integer.parseInt(event.getNewValue().toString());
 		if (selectedDepartment == null) setSelectedDepartment("");
 		for (Assignment assignment : assignmentList) {
 			if (checkIfAssignmentShouldBeDisplayed(assignment, selectedStudyProgram)) 
 				assignment.setDisplayAssignment(true);
 			else assignment.setDisplayAssignment(false);
 		}
		setAllEditExternalExaminerToFalse();
 	}
 	
 	private boolean checkIfAssignmentShouldBeDisplayed(Assignment abIn, String selectedStudyProgram) {
 		return (selectedStudyProgram.equals("") && abIn.getDepartment().equals(selectedDepartment)) 
 		|| abIn.getStudyProgram().equals(selectedStudyProgram);
 	}
 	
 	public void setAllEditExternalExaminerToFalse() {
 		for (Assignment assignment : assignmentList) {
 			assignment.setEditExternalExaminer(false);
 		}
 	}
 	
 	@Override
 	public LinkedList<SelectItem> getDepartmentList() {
 		return departmentList;
 	}
 
 	@Override
 	public void setDepartmentList(LinkedList<SelectItem> departmentList) {
 		this.departmentList = departmentList;
 	}
 	@Override
 	public LinkedList<SelectItem> getStudyProgramList() {
 		return studyProgramList;
 	}
 
 	@Override
 	public void setStudyProgramList(LinkedList<SelectItem> studyProgramList) {
 		this.studyProgramList = studyProgramList;
 	}
 	
 	@Override
 	public LinkedList<LinkedList<SelectItem>> getAllStudyProgramsByDepartmentsList() {
 		return allStudyProgramsByDepartmentList;
 	}
 
 	@Override
 	public void setAllStudyProgramsByDepartmentList(
 			LinkedList<LinkedList<SelectItem>> allStudyProgramsByDepartmentListIn) {
 		  allStudyProgramsByDepartmentList = allStudyProgramsByDepartmentListIn;
 	}
 
 	@Override
 	public String getStudyProgram(int index) {
 		return studyProgramList.get(index).getLabel();
 	}
 	@Override
 	public String getDepartment(int index) {
 		return  departmentList.get(index).getLabel();
 	}
 
 	@Override
 	public String getSelectedDepartment() {
 		return selectedDepartment;
 	}
 
 	@Override
 	public void setSelectedDepartment(String selectedDepartment) {
 		this.selectedDepartment = selectedDepartment;
 	}
 
 	@Override
 	public void removeAssignment(Assignment assignment) {
 		assignmentList.remove(assignment);
 	}
 
 	@Override
 	public void setStudyProgramListFromDepartmentNumber(int departmentNumber) {
 		setStudyProgramList(getAllStudyProgramsByDepartmentsList().
 				get(departmentNumber));
 	}
 }
