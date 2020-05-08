 package no.uis.portal.employee;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.InitializingBean;
 
 import com.icesoft.faces.component.ext.HtmlSelectOneMenu;
 import com.liferay.portal.PortalException;
 import com.liferay.portal.SystemException;
 import com.liferay.portal.model.Permission;
 import com.liferay.portal.model.Role;
 import com.liferay.portal.model.User;
 import com.liferay.portal.service.PermissionLocalServiceUtil;
 import com.liferay.portal.theme.ThemeDisplay;
 import com.liferay.portlet.expando.model.ExpandoTableConstants;
 import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
 
 import no.uis.abam.commons.BaseTextUtil;
 import no.uis.abam.dom.AbamGroup;
 import no.uis.abam.dom.Application;
 import no.uis.abam.dom.Assignment;
 import no.uis.abam.dom.Employee;
 import no.uis.abam.dom.Student;
 import no.uis.abam.dom.Thesis;
 import no.uis.abam.ws_abam.AbamWebService;
 import no.uis.portal.util.LiferayUtil;
 import no.uis.service.model.Organization;
 
 public class EmployeeService implements InitializingBean {
 	
 	public static class BooleanHashMap extends HashMap<String, Boolean> {
 
     private static final long serialVersionUID = 1L;
 
     @Override
     public Boolean get(Object key) {
       Boolean result = super.get(key);
       return result == null ? Boolean.FALSE : result;
     }
   }
 
   private static final UnknownEmployee UNKNOWN_EMPLOYEE = new UnknownEmployee();
 
   public static final String COLUMN_UIS_LOGIN_NAME = "UiS-login-name";
 	private Logger log = Logger.getLogger(EmployeeService.class);
 	
 	private String selectedDepartmentCode;
 	private String selectedStudyProgramCode;
 	
 	private AbamWebService abamClient;
 
 	private List<Organization> departmentList;
 	private List<no.uis.service.model.StudyProgram> selectedStudyProgramList = new ArrayList<no.uis.service.model.StudyProgram>();
 
 	private List<SelectItem> departmentSelectItemList = new ArrayList<SelectItem>();
 	private List<SelectItem> studyProgramSelectItemList = new ArrayList<SelectItem>();
 	
 	private HtmlSelectOneMenu studyProgramMenu;
 	
 	private List<Assignment> assignmentSet;
 	private Set<Assignment> displayAssignmentSet;
 	
 	private Employee loggedInEmployee;
 	private ThemeDisplay themeDisplay;
 
   private Map<String, Boolean> permissions = new BooleanHashMap();
 
   private Map<String, Boolean> userRoles = new BooleanHashMap();
 	
 	public EmployeeService() {	
 	}
 	
   @Override
   public void afterPropertiesSet() throws Exception {
     themeDisplay = LiferayUtil.getThemeDisplay(FacesContext.getCurrentInstance());
     loggedInEmployee = initLoggedInEmployee(themeDisplay);
     initRolesAndPermissions(themeDisplay.getUser());
     initDepartmentListFromWebService(themeDisplay.getLocale().getDisplayLanguage());
   }
 
 	public void saveAssignment(Assignment assignment) {
 		abamClient.saveAssignment(assignment);
 	}
 
 	/**
 	 * ActionListener that prepares displayAssignments.jspx
 	 * 
 	 * @param event
 	 */
 	public void actionPrepareDisplayAssignments(ActionEvent event) {		
 		if (StringUtils.isBlank(loggedInEmployee.getEmployeeId())) {
 	     throw new IllegalArgumentException("employeeId");
 		} else  {
       Organization org = abamClient.getEmployeeDeptarment(loggedInEmployee.getEmployeeId());
       setSelectedDepartmentCode(org.getPlaceRef());
       getStudyProgramListFromSelectedDepartment();
       if (!selectedStudyProgramList.isEmpty()) {
         setSelectedStudyProgramCode(this.selectedStudyProgramList.get(0).getId());
       }
   		
       getActiveAssignmentsSet();
   		checkIfLoggedInUserIsAuthor();
 		}
 	}
 	
 	public void setSelectedStudyProgramCode(String selectedStudyProgramCode) {
 	  this.selectedStudyProgramCode = selectedStudyProgramCode;
   }
 
   public String getSelectedStudyProgramCode() {
     return selectedStudyProgramCode;
   }
 
   private void checkIfLoggedInUserIsAuthor() {
 		if(assignmentSet != null) {
 			for (Assignment assignment : assignmentSet) {
			  String authorName = assignment.getAuthor() == null ? "" : (assignment.getAuthor().getName() == null ? "" : assignment.getAuthor().getName());
				if (authorName.equals(loggedInEmployee.getName())) {
 					assignment.setLoggedInUserIsAuthor(true);
 				} else {
 					assignment.setLoggedInUserIsAuthor(false);
 				}
 			}
 		}
 	}
 
 	/**
 	 * ValueChangeListener that updates the StudyProgram List and Assignment Set based on the selected Department
 	 * @param event
 	 */
 	public void actionUpdateStudyProgramList(ValueChangeEvent event) {
 		String newDeptCode = event.getNewValue().toString();
 		setSelectedDepartmentCode(newDeptCode);
 		setSelectedStudyProgramListFromDepartmentCode(newDeptCode);
 		if(studyProgramMenu != null) {
 		  studyProgramMenu.setValue(getSelectedStudyProgramCode());
 		}
 		displayAssignmentSet.clear();
 		if(assignmentSet != null) {
 			for (Assignment assignment : assignmentSet) {
 				if (assignment.getDepartmentCode().equals(selectedDepartmentCode)
 					|| selectedDepartmentCode.equals("")) {
 					displayAssignmentSet.add(assignment);
 					assignment.setDepartmentCode(assignment.getDepartmentCode());
 				}
 			}
 		}
 	}
 
 	public String getDepartmentNameFromCode(String code) {
 	  if (code != null) {
   		for (Organization dep : departmentList) {
   		  if(dep.getPlaceRef().equals(code)) {
   		    return BaseTextUtil.getText(dep.getName(), themeDisplay.getLocale().getLanguage());
   		  }
   		}
 	  }
 		return "";
 	}
 	
 	
 	/**
 	 * ValueChangeListener that updates the StudyProgram List from createAssignment.jspx
 	 * @param event
 	 */
 	public void actionUpdateStudyProgramListFromCreateAssignment(ValueChangeEvent event) {
 		selectedDepartmentCode = event.getNewValue().toString();
 		getStudyProgramListFromSelectedDepartment();		
 
 	}
 
 	private void getStudyProgramListFromSelectedDepartment() {
 	  List<no.uis.service.model.StudyProgram> progs = abamClient.getStudyProgramsFromDepartmentFSCode(this.selectedDepartmentCode);
 	  if (progs == null) {
 	    progs = Collections.emptyList();
 	  }
 		selectedStudyProgramList = progs;
 		studyProgramSelectItemList.clear();
 		updateStudyProgramSelectItemList();
 	}
 
 	
 	/**
 	 * ValueChangeListener that updates the Set with Assignments based on the selected StudyProgram
 	 * 
 	 * @param event
 	 */
 	public void actionSetDisplayAssignment(ValueChangeEvent event) {
 
 		if (event.getNewValue() == null) {
 			selectedStudyProgramCode = null;
 		} else {
 			selectedStudyProgramCode = event.getNewValue().toString();
 		}
 		setDisplayAssignments();
 	}
 	
 	
 	/**
 	 * Updates the Set with DisplayAssignments based on selected StudyProgram 
 	 */
 	public void setDisplayAssignments() {
 		String selectedStudyProgram = getStudyProgramNameFromCode(selectedStudyProgramCode);
 		
 		displayAssignmentSet.clear();
 		
 		if (selectedDepartmentCode == null)
 			
 			setSelectedDepartmentCode("");
 		if (selectedStudyProgram == null)
 			selectedStudyProgram = "";
 		if (assignmentSet != null) {
 			for (Assignment assignment : assignmentSet) {
 				if (assignmentShouldBeDisplayed(assignment,
 						selectedStudyProgram)) {
 					displayAssignmentSet.add(assignment);
 				}
 			}
 		}		
 	}
 
 	public String getStudyProgramNameFromCode(String programCode) {
     no.uis.service.model.StudyProgram studProg = abamClient.getStudyProgramFromCode(programCode);
     return BaseTextUtil.getText(studProg.getName(), themeDisplay.getLocale().getLanguage());
 	}
 	
 	private boolean assignmentShouldBeDisplayed(Assignment assignmentIn, String selectedStudyProgram) {
 	  
 	  String assignmentStudProgName = getStudyProgramNameFromCode(assignmentIn.getStudyProgramCode());
 	  
 		return (selectedStudyProgram.equals("") && assignmentIn
 				.getDepartmentCode().equals(selectedDepartmentCode))
 				|| assignmentStudProgName.equals(
 						selectedStudyProgram)
 				|| getSelectedDepartmentCode().equals("");
 	}
 
 	public void addThesesFromList(List<Thesis> thesesToAdd) {
 		abamClient.addThesesFromList(thesesToAdd);
 	}
 
 	public void updateThesis(Thesis thesisToUpdate) {
 		abamClient.updateThesis(thesisToUpdate);
 	}
 
 	public Student getStudentFromStudentNumber(String studentNumber) {
 		return abamClient.getStudentFromStudentNumber(studentNumber);
 	}
 
 	public void removeAssignment(Assignment assignment) {
 		abamClient.removeAssignment(assignment);
 		getActiveAssignmentsSet();
 	}
 
 	public void removeApplication(Application application) {
 		abamClient.removeApplication(application);
 	}
 
 	public List<Application> getMasterApplicationList() {
 		return abamClient.getMasterApplicationListFromDepartmentCode(selectedDepartmentCode);
 	}
 
 	public List<Application> getBachelorApplicationListFromSelectedDepartmentNumber() {
 		return abamClient.getBachelorApplicationListFromDepartmentCode(selectedDepartmentCode);
 	}
 
 	public Assignment getAssignmentFromId(int id) {
 		return abamClient.getAssignmentFromId(id);
 	}
 
 	
 	/**
 	 * Gets the Departments from the webservice, and sets the name based on selected language 
 	 */
 	private void initDepartmentListFromWebService(String lang) {
 	  List<Organization> deps = abamClient.getDepartmentList();
 		departmentSelectItemList.clear();
 		for (Organization dep : deps) {
       String placeRef = dep.getPlaceRef();
       SelectItem item = null;
       if (placeRef == null || placeRef.length() == 0) {
         item = new SelectItem("", "");
       } else {
         item = new SelectItem(placeRef, BaseTextUtil.getText(dep.getName(), lang));
       }
       departmentSelectItemList.add(item);
     }
     departmentList = deps;
 	}
 
 	public int getNextId() {
 		return abamClient.getNextId();
 	}
 
 	public String getSelectedDepartmentCode() {
 		return selectedDepartmentCode;
 	}
 
 	public void setSelectedDepartmentCode(String selectedDepartmentCode) {
 		this.selectedDepartmentCode = selectedDepartmentCode;
 	}
 
 	public List<no.uis.service.model.StudyProgram> getSelectedStudyProgramList() {
 		return selectedStudyProgramList;
 	}
 
 	public void setSelectedStudyProgramList(List<no.uis.service.model.StudyProgram> list) {
 		this.selectedStudyProgramList = list;
 		updateStudyProgramSelectItemList();
 	}
 	
 	private void updateStudyProgramSelectItemList() {
 		studyProgramSelectItemList.clear();
 		for (no.uis.service.model.StudyProgram prog : selectedStudyProgramList) {
 		  studyProgramSelectItemList.add(new SelectItem(prog.getId(), BaseTextUtil.getText(prog.getName(), themeDisplay.getLocale().getLanguage())));
     }
 	}
 
 	public List<Thesis> getThesisList() {
 		return abamClient.getThesisList();
 	}
 
 	public List<Assignment> getAssignmentSet() {
 		return assignmentSet;
 	}
 
 	public void setAssignmentSet(List<Assignment> assignmentSet) {
 		this.assignmentSet = assignmentSet;
 	}
 
 	public List<Assignment> getAllAssignmentsSet() {
 		assignmentSet = abamClient.getAllAssignments();
 		return assignmentSet;
 	}
 	
 	public HtmlSelectOneMenu getStudyProgramMenu() {
 		return studyProgramMenu;
 	}
 
 	public void setStudyProgramMenu(HtmlSelectOneMenu studyProgramMenu) {
 		this.studyProgramMenu = studyProgramMenu;
 	}
 
 	
 	/**
 	 * @return a Set containing all active Assignments
 	 */
 	public List<Assignment> getActiveAssignmentsSet() {
 		assignmentSet = abamClient.getActiveAssignments();
 		displayAssignmentSet = new TreeSet<Assignment>();
 		if(assignmentSet != null) {
 			displayAssignmentSet.addAll(assignmentSet);
 		}
 		return assignmentSet;
 	}
 
 	public void setAbamClient(AbamWebService abamClient) {
 		this.abamClient = abamClient;
 	}
 	
 	AbamWebService getAbamClient() {
 	  return this.abamClient;
 	}
 	
 	private void initRolesAndPermissions(User user) {
 
     for (Role role : user.getRoles()) {
       userRoles.put(role.getName(), Boolean.TRUE);
       try {
         List<Permission> perms = PermissionLocalServiceUtil.getRolePermissions(role.getRoleId());
         for (Permission permission : perms) {
           permissions.put(permission.getActionId(), Boolean.TRUE);
         }
       } catch (SystemException e) {       
         log.warn("initialize permissions", e);
       }     
     }
 	}
 	
 	public Map<String, Boolean> getRoles() {
 	  return this.userRoles;
 	}
 	
 	public Map<String, Boolean> getPermissions() {
 	  return this.permissions;
 	}
 	
 	public List<SelectItem> getDepartmentSelectItemList() {
 		return departmentSelectItemList;
 	}
 
 	public void setDepartmentSelectItemList(
 			List<SelectItem> departmentSelectItemList) {
 		this.departmentSelectItemList = departmentSelectItemList;
 	}
 
 	public List<SelectItem> getStudyProgramSelectItemList() {
 		return studyProgramSelectItemList;
 	}
 
 	public void setStudyProgramSelectItemList(
 			List<SelectItem> studyProgramSelectItemList) {
 		this.studyProgramSelectItemList = studyProgramSelectItemList;
 	}
 
 	public ThemeDisplay getThemeDisplay() {
 		return themeDisplay;
 	}
 	
 	/**
 	 * Finds the logged in Employee based on employee id 
 	 * @return Employee object if found, UNKNOWN_EMPLOYEE if not found
 	 */
 	public Employee getLoggedInEmployee() {
 		return loggedInEmployee;
 	}
 
   private Employee initLoggedInEmployee(ThemeDisplay td) {
 		String loginName = null;
 		User user = td.getUser();
 		try {			
       loginName = getUserCustomAttribute(user, COLUMN_UIS_LOGIN_NAME);
 		} catch (Exception e) {
 		  log.warn(user.getFullName(), e);
 		}
 		Employee employee = null;
 		if (loginName != null) {
 		  try {
 		    employee = abamClient.getEmployeeFromUisLoginName(loginName);
 		  } catch(Exception e) {
 		    log.warn(loginName, e);
 		  }
 		}
 
 		return (employee == null ? UNKNOWN_EMPLOYEE : employee);
   }		
 	
 	// TODO this is the same function as in StudentService, put common code in a library
 	private static String getUserCustomAttribute(User user, String columnName) throws PortalException, SystemException {
     // we cannot use the user's expando bridge here because the permission checker is not initialized properly at this stage      
     String data = ExpandoValueLocalServiceUtil.getData(User.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME,
         columnName, user.getUserId(), (String)null);
      return data;
 	}
 
 	public Set<Assignment> getDisplayAssignmentSet() {
 		return displayAssignmentSet;
 	}
 
 	public Employee getEmployeeFromName(String facultySupervisorName) {		
 		return abamClient.getEmployeeFromFullName(facultySupervisorName);
 	}
 	
 	public List<Thesis> getThesisListFromDepartmentCode(String depCode) {
 		return abamClient.getThesisListFromDepartmentCode(depCode);
 	}
 	
 	public List<Thesis> getArchivedThesisListFromDepartmentCode(String depCode) {
 		return abamClient.getArchivedThesisListFromDepartmentCode(depCode);
 	}
 		
 	/**
 	 * @return a List containing the archived Theses for logged in Employee
 	 */
 	public List<Thesis> getArchivedThesisListFromUisLoginName() {
 		Employee employee = getLoggedInEmployee();		
 		return abamClient.getArchivedThesisListFromUisLoginName(employee.getEmployeeId());		
 	}
 
   public void setSelectedStudyProgramListFromDepartmentCode(String departmentCode) {
     List<no.uis.service.model.StudyProgram> progs = abamClient.getStudyProgramsFromDepartmentFSCode(departmentCode);
     this.selectedStudyProgramList = progs;
     updateStudyProgramSelectItemList();
   }
   
   private static class UnknownEmployee extends Employee {
 
     private static final Long LONG_ZERO = Long.valueOf(0);
     private static final String EMPTY_STRING = "";
     private static final long serialVersionUID = 1L;
     
     @Override
     public String getEmployeeId() {
       return EMPTY_STRING;
     }
 
     @Override
     public List<AbamGroup> getGroups() {
       return Collections.emptyList();
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
     public boolean equals(Object obj) {
       if (obj == null) {
         return false;
       }
       if (this == obj) {
         return true;
       }
       return false;
     }
 
     @Override
     public void setEmployeeId(String employeeId) {
     }
 
     @Override
     public void setGroups(List<AbamGroup> groups) {
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
   }
 }
