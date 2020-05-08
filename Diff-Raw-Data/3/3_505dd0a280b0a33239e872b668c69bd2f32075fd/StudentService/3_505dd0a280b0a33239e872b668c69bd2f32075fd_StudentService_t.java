 package no.uis.portal.student;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 
 import no.uis.abam.commons.BaseTextUtil;
 import no.uis.abam.dom.Application;
 import no.uis.abam.dom.Assignment;
 import no.uis.abam.dom.AssignmentType;
 import no.uis.abam.dom.Employee;
 import no.uis.abam.dom.Student;
 import no.uis.abam.dom.Thesis;
 import no.uis.abam.ws_abam.AbamWebService;
 import no.uis.portal.util.LiferayUtil;
 import no.uis.service.model.Organization;
 import no.uis.service.model.StudyProgram;
 
 import org.apache.log4j.Logger;
 import org.apache.myfaces.shared_impl.util.MessageUtils;
 import org.springframework.beans.factory.InitializingBean;
 
 import com.icesoft.faces.component.ext.HtmlDataTable;
 import com.liferay.portal.PortalException;
 import com.liferay.portal.SystemException;
 import com.liferay.portal.model.User;
 import com.liferay.portal.theme.ThemeDisplay;
 import com.liferay.portlet.expando.model.ExpandoTableConstants;
 import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
 
 // TODO improve protection of resources in concurrent thread environment, it is a mess. 
 public class StudentService implements InitializingBean {
 
   private static final UnknownStudent UNKNOWN_STUDENT = new UnknownStudent();
 
   private static final int MAX_BACHELOR_APPLICATIONS = 3;
   
 	public static final String COLUMN_UIS_LOGIN_NAME = "UiS-login-name";
 	
 	private static Logger log = Logger.getLogger(StudentService.class);
 	
 	private List<Assignment> assignmentList; 
 	private Assignment selectedAssignment;
 	
 	private Student currentStudent;
 
 	private AbamWebService abamStudentClient;
 	
 	private List<Application> tempApplicationPriorityArray = new ArrayList<Application>();
 	private List<Application> applicationsToRemove = new ArrayList<Application>();
 	private List<SelectItem> departmentSelectItemList = new ArrayList<SelectItem>();
 	private List<SelectItem> studyProgramSelectItemList = new ArrayList<SelectItem>();
 	
 	private String selectedStudyProgramCode;
 	
 	private ThemeDisplay themeDisplay;
 
   private String studentDepartmentName;
 	
   public StudentService() {
 	}
 
 	@Override
   public void afterPropertiesSet() throws Exception {
     FacesContext context  = FacesContext.getCurrentInstance();
     themeDisplay = LiferayUtil.getThemeDisplay(context);
     Student stud = getStudentFromLogin();
     if (stud == null) {
       stud = UNKNOWN_STUDENT;
     }
     currentStudent = stud;
     this.studentDepartmentName = initDepartmentName();
   }
 
   private Student getStudentFromLogin() {
 		String loginName = null;
 		try {			
 			loginName = getUserCustomAttribute(getThemeDisplay().getUser(), COLUMN_UIS_LOGIN_NAME);
 		} catch (Exception e) {
 		  log.warn(loginName, e);
 		}
 		Student student = null;
 		if (loginName != null) {
 		  student = abamStudentClient.getStudentFromStudentNumber(loginName);
 		}
 		return student;
 	}
 
 	public ThemeDisplay getThemeDisplay() {
 		return themeDisplay;
 	}
 	
 	 // TODO this is the same function as in EmployeeService, put common code in a library
 	private static String getUserCustomAttribute(User user, String columnName) throws PortalException, SystemException {
 	  // we cannot use the user's expando bridge here because the permission checker is not initialized properly at this stage	    
 		String data = ExpandoValueLocalServiceUtil.getData(User.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME,
 	      columnName, user.getUserId(), (String)null);
 	   return data;
 	}
 	
 	public void saveAssignment(Assignment assignment) {
 		getCurrentStudent().setCustomAssignment(assignment);
 	}
 	
 	public void setApplicationToStudent(Application application) {
 	  Student stud = getCurrentStudent();
 	  if (applicationIsLegitimate(stud, application)) {
 	    stud.getApplications().add(application);
 	    stud = abamStudentClient.updateStudent(stud);
 	    setCurrentStudent(stud);
 	  }
 	}
 	
   private static boolean applicationIsLegitimate(Student student, Application application) {
     List<Application> applications = student.getApplications();
     if (applications.size() >= MAX_BACHELOR_APPLICATIONS) {
       MessageUtils.addMessage(FacesMessage.SEVERITY_WARN, "maximum_applications_reached", new Object[] {MAX_BACHELOR_APPLICATIONS});
       return false;
     }
     // TODO This doesn't work. since the oids don't necessary match
     if (applications.contains(application)) {
       MessageUtils.addMessage(FacesMessage.SEVERITY_WARN, "applications_already_applied_for", null);
       return false;
     }
     return true;
   }
   
 	public List<Assignment> getAssignmentList() {
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
 		HtmlDataTable table = (HtmlDataTable)uic.getParent().getParent().getParent();
 		return (Application)table.getRowData();
 	}
 	 
 	public void actionGetApplicationFromStudent(ActionEvent event) {
 		tempApplicationPriorityArray.clear(); 
 		tempApplicationPriorityArray.addAll(getCurrentStudent().getApplications());
 	}
 	
 	public void actionClearStudyProgramAndDepartmentNumber(ActionEvent event){
 		setSelectedStudyProgramCode(null);
 	}
 	
 	private void updateStudyProgramList(String departmentCode){
 		List<Assignment> assignmentList = getAssignmentList();
 		//Student stud = getCurrentStudent();
 		if (assignmentList != null) {
 			for (Assignment assignment : assignmentList) {
 				if (assignment.getDepartmentCode().equals(departmentCode)) { 
 					if(currentStudentIsEligibleForAssignment(assignment)) {
 						assignment.setDisplayAssignment(true);
 						//String depName = getDepartmentNameFromCode(assignment.getDepartmentCode());
 					} else {
 					  assignment.setDisplayAssignment(false);
 					}
 				} else {
 				  assignment.setDisplayAssignment(false);
 				}
 			}
 		}
 	}
 	
 	private boolean currentStudentIsEligibleForAssignment(Assignment assignment) {
 	  AssignmentType studentType = getCurrentStudent().getType();
 		return assignment.getType().equals(studentType);
 	}
 	
 	public void actionSetDisplayAssignment(ValueChangeEvent event){
 	  
 		selectedStudyProgramCode = event.getNewValue().toString();
     String selectedStudyProgram = getStudyProgramNameFromCode(selectedStudyProgramCode);
 		assignmentList = getAssignmentList();
 		if (assignmentList != null) {
 			for (Assignment assignment : assignmentList) {
 				if (checkIfAssignmentShouldBeDisplayed(assignment,
 						selectedStudyProgram)) {
 					if (currentStudentIsEligibleForAssignment(assignment))
 						assignment.setDisplayAssignment(true);
 				} else
 					assignment.setDisplayAssignment(false);
 			}
 		}		
 	}
 	
 	private boolean checkIfAssignmentShouldBeDisplayed(Assignment abIn, String selectedStudyProgram) {
 	  Student stud = getCurrentStudent();
 	  String assignStudProgName = getStudyProgramNameFromCode(abIn.getStudyProgramCode());
 	  
   return (selectedStudyProgram.equals("") && abIn.getDepartmentCode().equals(stud.getDepartmentCode())) 
   || assignStudProgName.equals(selectedStudyProgram);
 	  
 	}
 	
 	private String getStudyProgramNameFromCode(String progCode) {
 	  no.uis.service.model.StudyProgram prog = abamStudentClient.getStudyProgramFromCode(progCode);
 	  
 	  return BaseTextUtil.getText(prog.getName(), getThemeDisplay().getLocale().getLanguage());
 	}
 	
   public void updateSelectedAssignmentInformation(Assignment selectedAssignment){
 		setSelectedAssignment(selectedAssignment);
 		//setStudyProgramListFromDepartmentNumber(selectedAssignment.getDepartmentNumber());
 		
 		setSelectedStudyProgramCode(selectedAssignment.getStudyProgramCode());
 	}
 	
 	
 	public void actionPrepareAvailableAssignments(ActionEvent event) {		
 		assignmentList = abamStudentClient.getAssignmentsFromDepartmentCode(getCurrentStudent().getDepartmentCode());			
 		updateStudyProgramList(findDepartmentCodeForCurrentStudent());		
 		//getStudyProgramList();
 	}
 	
 	public String findDepartmentCodeForCurrentStudent() {
 		return getCurrentStudent().getDepartmentCode();
 	}
 	
 	public void actionSaveApplications(ActionEvent event) {
 		removeDeletedApplications();
 		getCurrentStudent().setApplications(tempApplicationPriorityArray);
 		abamStudentClient.updateApplications(tempApplicationPriorityArray);		
 		abamStudentClient.updateStudent(getCurrentStudent());
 	}
 	
 	private void removeDeletedApplications() {
 		for (Application application : applicationsToRemove) {
 			abamStudentClient.removeApplication(application);
 		}
 	}
 	
 	public void actionClearDeletedElements(ActionEvent event){
 		applicationsToRemove.clear();
 	}
 	
 	private String initDepartmentName() {
 	  
 	  Student stud = getCurrentStudent();
 	  String deptCode = stud.getDepartmentCode();
	  if (stud instanceof UnknownStudent) {
	    return deptCode;
	  }
 	  
     List<Organization> depts = abamStudentClient.getDepartmentList();
 	  String lang = getThemeDisplay().getLocale().getLanguage();
 		for (Organization dept : depts) {
       if (dept.getPlaceRef().equals(deptCode)) {
         String name = BaseTextUtil.getText(dept.getName(), lang);
         return name;
       }
     }
 		return deptCode;
 	}
 
   public void removeAssignment(Assignment assignment) {
 		abamStudentClient.removeAssignment(assignment);
 	}
 
 	public String getSelectedStudyProgramCode() {
 		return selectedStudyProgramCode;
 	}
 	
 	public void setSelectedStudyProgramCode(String selectedStudyProgramCode) {
 		this.selectedStudyProgramCode = selectedStudyProgramCode;
 	}
 
 	public synchronized Student getCurrentStudent() {
 		return currentStudent;
 	}
 
 	private synchronized void setCurrentStudent(Student stud) {
 	  currentStudent = stud;
 	}
 	
   public String getCurrentDepartmentName() {
     return studentDepartmentName;
   }
   
   public String getCurrentStudyProgramName() {
     String code = getCurrentStudent().getStudyProgramCode();
     return this.getStudyProgramNameFromCode(code);
   }
   
 	public void updateStudentInWebServiceFromCurrentStudent() {
 	  Student stud = getCurrentStudent();
 		abamStudentClient.updateStudent(stud);
 	}
 	
 	public List<Application> getApplicationList() {
 		return abamStudentClient.getApplicationList();
 	}
 
 	public void setAbamStudentClient(AbamWebService abamStudentClient) {
 		this.abamStudentClient = abamStudentClient;
 	}
 	
 	public void removeApplication(Application application) {
 	  tempApplicationPriorityArray.remove(application);
 	}
 
 	// TODO solve with sorting
 	private void moveApplicationHigher(Application selectedApplication) {
 //		int selectedApplicationIndex = findApplicationIndex(selectedApplication);
 //		if(selectedApplicationIndex > 0) {
 //			int higherApplicationIndex = selectedApplicationIndex - 1;
 //			Application higherApplication = tempApplicationPriorityArray[higherApplicationIndex];
 //			if(selectedApplication != null){
 //				selectedApplication.setPriority(higherApplicationIndex + 1);
 //				if(higherApplication != null){
 //					higherApplication.setPriority(selectedApplicationIndex + 1);
 //				}
 //				tempApplicationPriorityArray[higherApplicationIndex] = selectedApplication;
 //				tempApplicationPriorityArray[selectedApplicationIndex] = higherApplication;								
 //					
 //			}
 //		}
 	}
 	
 	// TODO solve with sorting
 	private void moveApplicationLower(Application selectedApplication) {
 //		int selectedApplicationIndex = findApplicationIndex(selectedApplication);
 //		if(selectedApplicationIndex != 2) {
 //			int lowerApplicationIndex = selectedApplicationIndex + 1;
 //			Application lowerApplication = tempApplicationPriorityArray[lowerApplicationIndex];
 //			if(lowerApplication != null) {
 //				lowerApplication.setPriority(selectedApplicationIndex + 1);
 //				tempApplicationPriorityArray[lowerApplicationIndex] = selectedApplication;
 //				tempApplicationPriorityArray[selectedApplicationIndex] = lowerApplication;
 //				if (selectedApplication != null) {
 //					selectedApplication.setPriority(lowerApplicationIndex + 1);
 //				}
 //			}
 //		}
 	}
 
 	public List<Application> getTempApplicationPriorityArray() {
 		return tempApplicationPriorityArray;
 	}
 
 	public Assignment getAssignmentFromId(long assignedAssignmentId) {
 		return abamStudentClient.getAssignmentFromId(assignedAssignmentId);
 	}
 
 	public List<SelectItem> getDepartmentSelectItemList() {
 		return departmentSelectItemList;
 	}
 
 	@Deprecated
 	public String getStudentInstitute() {
 	  return this.getCurrentDepartmentName();
 	}
 	
 	public void setDepartmentSelectItemList(
 			ArrayList<SelectItem> departmentSelectItemList) {
 		this.departmentSelectItemList = departmentSelectItemList;
 	}
 
 	// TODO only refresh on change of department code
 	public synchronized List<SelectItem> getStudyProgramSelectItemList() {
     List<StudyProgram> studyProgramList = null; 
     
     Student stud = getCurrentStudent();
     if (stud != null) {
       studyProgramList = abamStudentClient.getStudyProgramsFromDepartmentFSCode(stud.getDepartmentCode());
     }
     studyProgramSelectItemList.clear();
     if (studyProgramList != null) {
       String lang = getThemeDisplay().getLocale().getLanguage();
       for (no.uis.service.model.StudyProgram program : studyProgramList) {
         studyProgramSelectItemList.add(new SelectItem(program.getId(), BaseTextUtil.getText(program.getName(), lang)));
       }
     }
 		return studyProgramSelectItemList;
 	}
 
 	public Employee getEmployeeFromFullName(String name) {
 		return abamStudentClient.getEmployeeFromFullName(name);
 	}
 
 	public Student getStudentFromStudentNumber(String studentNumber) {
 		return abamStudentClient.getStudentFromStudentNumber(studentNumber);
 	}
 
 	public void updateThesis(Thesis thesis) {
 		abamStudentClient.updateThesis(thesis);
 		
 	}
 	
 	public void updateStudent(Student std) {
 	  if (!UNKNOWN_STUDENT.equals(std)) {
 	    setCurrentStudent(abamStudentClient.updateStudent(std));
 	  }
 	}
 	
 	public Assignment getCustomAssignmentFromStudentNumber(String studentNumber) {
 		return abamStudentClient.getCustomAssignmentFromStudentNumber(studentNumber);
 	}
 	
   private final static class UnknownStudent extends Student {
     private static final Long LONG_ZERO = Long.valueOf(0);
 
     private static final long serialVersionUID = 1L;
 
     private static final String EMPTY_STRING = "";
     public UnknownStudent() {
     }
     
     @Override
     public String getName() {
       return EMPTY_STRING;
     }
 
     @Override
     public String getEmail() {
       return EMPTY_STRING;
     }
 
 
     @Override
     public String getPhoneNumber() {
       return EMPTY_STRING;
     }
 
 
     @Override
     public Long getOid() {
       return LONG_ZERO;
     }
 
 
     @Override
     public List<Application> getApplications() {
       return Collections.emptyList();
     }
 
     @Override
     public String getStudentNumber() {
       return EMPTY_STRING;
     }
 
     @Override
     public String getDepartmentCode() {
       return EMPTY_STRING;
     }
 
     @Override
     public String getDepartmentName() {
       return EMPTY_STRING;
     }
 
     @Override
     public String getStudyProgramName() {
       return EMPTY_STRING;
     }
 
     @Override
     public synchronized String getStudyProgramCode() {
       return EMPTY_STRING;
     }
 
     @Override
     public Assignment getCustomAssignment() {
       return null;
     }
 
     @Override
     public AssignmentType getType() {
       return AssignmentType.BACHELOR;
     }
 
     @Override
     public Thesis getAssignedThesis() {
       return null;
     }
 
     @Override
     public boolean isAcceptedThesis() {
       return false;
     }
 
     @Override
     public Calendar getSubmissionDate() {
       return null;
     }
 
     @Override
     public void setStudentNumber(String studentNumber) {
     }
 
     @Override
     public void setDepartmentCode(String departmentCode) {
     }
 
     @Override
     public void setDepartmentName(String departmentName) {
     }
 
     @Override
     public void setStudyProgramName(String studyProgramName) {
     }
 
     @Override
     public synchronized void setStudyProgramCode(String studyProgramCode) {
     }
 
     @Override
     public void setCustomAssignment(Assignment customAssignment) {
     }
 
     @Override
     public void setApplications(List<Application> applications) {
     }
 
     @Override
     public void setType(AssignmentType type) {
     }
 
     @Override
     public void setAssignedThesis(Thesis assignedThesis) {
     }
 
     @Override
     public void setAcceptedThesis(boolean acceptedThesis) {
     }
 
     @Override
     public void setSubmissionDate(Calendar submissionDate) {
     }
 
     @Override
     public void setEmail(String email) {
     }
 
     @Override
     public void setPhoneNumber(String phoneNumber) {
     }
 
     @Override
     public void setName(String name) {
     }
 
     @Override
     public void setOid(Long oid) {
     }
 
     @Override
     public boolean equals(Object obj) {
       if (obj == null) {
         return false;
       }
       if (this == obj) {
         return true;
       }
       
       return false;
     }
   }
 }
 
 
