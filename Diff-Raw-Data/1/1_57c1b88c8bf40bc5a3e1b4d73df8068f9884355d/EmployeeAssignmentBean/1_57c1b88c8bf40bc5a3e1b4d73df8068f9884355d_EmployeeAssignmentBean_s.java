 package no.uis.portal.employee;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.EventObject;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.application.NavigationHandler;
 import javax.faces.component.ActionSource;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIInput;
 import javax.faces.context.FacesContext;
 import javax.faces.el.MethodBinding;
 import javax.faces.el.ValueBinding;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 
 import no.uis.abam.commons.AttachmentResource;
 import no.uis.abam.commons.ThesisInformation;
 import no.uis.abam.dom.Assignment;
 import no.uis.abam.dom.AssignmentType;
 import no.uis.abam.dom.Attachment;
 import no.uis.abam.dom.Employee;
 import no.uis.abam.dom.Supervisor;
 import no.uis.abam.dom.Thesis;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.myfaces.shared_impl.util.MessageUtils;
 
 import com.icesoft.faces.component.ext.HtmlDataTable;
 import com.icesoft.faces.component.inputfile.FileInfo;
 import com.icesoft.faces.component.inputfile.InputFile;
 import com.icesoft.faces.context.DisposableBean;
 import com.icesoft.faces.context.Resource;
 
 public class EmployeeAssignmentBean implements DisposableBean {
 		
 	private EmployeeService employeeService;
 	private Assignment currentAssignment;
 	private Thesis currentThesis;
 	
 	private boolean backToAssignAssignment;
 	private boolean backToDisplayAssignments;
 	private boolean backToAssignmentAttachment;
 	private boolean backToMyStudentThesis;
 	
 	private boolean showExpired;
   private boolean autoUpload = true;
   private int uploadProgress;
 	
 	public EmployeeAssignmentBean(){
 	}
 	
 	public void setEmployeeService(EmployeeService employeeService) {
 		this.employeeService = employeeService;
 	}
 	
 	/**
 	 * ActionListener that prepares the assignment summary from assignAssignment.jspx
 	 * @param event
 	 */
 	public void actionSetSelectedAssignmentFromAssignAssignment(ActionEvent event) {
 		UIComponent uic = event.getComponent();
 		HtmlDataTable table = (HtmlDataTable)uic.getParent().getParent();
 		
 		ApplicationInformation applicationInformation = (ApplicationInformation)table.getRowData();
 		Assignment selectedAssignment = applicationInformation.getApplication().getAssignment();
 		setCurrentAssignment(selectedAssignment);
 		employeeService.setSelectedStudyProgramListFromDepartmentCode(selectedAssignment.getDepartmentCode());
 		//employeeService.setSelectedStudyProgramListFromDepartmentIndex(selectedAssignment.getDepartmentCode());
 		
 		employeeService.setSelectedDepartmentCode(selectedAssignment.getDepartmentCode());
 		employeeService.setSelectedStudyProgramCode(selectedAssignment.getStudyProgramCode());
 		actionPrepareBackButtonFromAssignAssignemnt(event);
 	}
 	
 	/**
 	 * ActionListener that crates a new Assignment object and fills in id and employee name.
 	 * @param event
 	 */
 	public void actionCreateNewAssignment(ActionEvent event) {	
 		Assignment ass = new Assignment();
 		ass.setFacultySupervisor(employeeService.getLoggedInEmployee());
 		ass.setDepartmentCode(employeeService.getSelectedDepartmentCode());
 		ass.setStudyProgramCode(employeeService.getSelectedStudyProgramCode());
 		ass.setNumberOfStudents(1);
 		setCurrentAssignment(ass);
 	}
 	
 	/**
 	 * ActionListener that saves currentAssignemnt
 	 * @param event
 	 */
 	public void actionSaveAssignment(ActionEvent event) {
 		employeeService.saveAssignment(currentAssignment);
 	}
 	
 	/**
 	 * ActionListener that gets the Assignment and sets to currentAssignment
 	 * @param event
 	 */
 	public void actionSetSelectedAssignment(ActionEvent event){		
 		Assignment selectedAssignment = (Assignment) getRowFromEvent(event);
 		setCurrentAssignment(selectedAssignment);
 		//employeeService.setSelectedStudyProgramListFromDepartmentIndex(selectedAssignment.getDepartmentNumber());
 		employeeService.setSelectedStudyProgramListFromDepartmentCode(selectedAssignment.getDepartmentCode());
 		employeeService.setSelectedDepartmentCode(selectedAssignment.getDepartmentCode());
 		employeeService.setSelectedStudyProgramCode(selectedAssignment.getStudyProgramCode());
 		//employeeService.setSelectedStudyProgramNumber(selectedAssignment.getStudyProgramNumber());
 	}
 	
 	private Object getRowFromEvent(ActionEvent event) {
 		UIComponent uic = event.getComponent();		
 		HtmlDataTable table = (HtmlDataTable)uic.getParent().getParent();
 		return table.getRowData();
 	}
 	
 	/**
 	 * ActionListener that prepares the assignment summary from displayAssignments.jspx
 	 * @param event
 	 */
 	public void actionSetSelectedAssignmentFromDisplayAssignments(ActionEvent event){
 		actionSetSelectedAssignment(event);
 		actionPrepareBackButtonFromDisplayAssignments(event);
 	}
 	
 	/**
 	 * ActionListener that prepares the assignment summary from myStudentTheses.jspx
 	 * @param event
 	 */
 	public void actionSetSelectedAssignmentFromMyStudentTheses(ActionEvent event){
 		ThesisInformation selectedThesis = (ThesisInformation) getRowFromEvent(event);
 		setCurrentThesis(selectedThesis.getThesis());
 		Assignment selectedAssignment = selectedThesis.getThesis().getAssignment();
 		setCurrentAssignment(selectedAssignment);
 		employeeService.setSelectedStudyProgramListFromDepartmentCode(selectedAssignment.getDepartmentCode());
 		
 		employeeService.setSelectedDepartmentCode(selectedAssignment.getDepartmentCode());
 		employeeService.setSelectedStudyProgramCode(selectedAssignment.getStudyProgramCode());
 		actionPrepareBackButtonFromMyStudentTheses(event);
 	}
 	
 	/**
 	 * ActionListener that removes selected assignment
 	 * @param event
 	 */
 	public void actionRemoveAssignment(ActionEvent event) {
 		Assignment assignment = (Assignment) getRowFromEvent(event);
 		
 		employeeService.removeAssignment(assignment);
 		
 	}
 	
 	/**
 	 * ActionListener that adds a new Supervisor object to currentAssignment
 	 * @param event
 	 */
 	public void actionAddSupervisor(ActionEvent event) {
 		currentAssignment.getSupervisorList().add(new Supervisor());
 	}
 	/**
 	 * ActionListener that removes a Supervisor object from currentAssignment
 	 * @param event
 	 */	
 	public void actionRemoveSupervisor(ActionEvent event) {
 		currentAssignment.getSupervisorList().remove(getRowFromEvent(event));		
 	}
 	
 	/**
 	 * ActionListener that prepares back button from assignmentAttachment.jspx
 	 * @param event
 	 */
 	public void actionPrepareBackButtonFromAssignmentAttachment(ActionEvent event) {
 		setBackToAssignmentAttachment(true);
 		setBackToAssignAssignment(false);
 		setBackToDisplayAssignments(false);
 		setBackToMyStudentThesis(false);
 	}
 	
 	/**
 	 * ActionListener that prepares back button from assignAssignment.jspx
 	 * @param event
 	 */	
 	public void actionPrepareBackButtonFromAssignAssignemnt(ActionEvent event) {
 		setBackToAssignAssignment(true);
 		setBackToAssignmentAttachment(false);
 		setBackToDisplayAssignments(false);
 		setBackToMyStudentThesis(false);
 	}
 	
 	/**
 	 * ActionListener that prepares back button from displayAssignments.jspx
 	 * @param event
 	 */
 	public void actionPrepareBackButtonFromDisplayAssignments(ActionEvent event) {
 		setBackToDisplayAssignments(true);
 		setBackToAssignmentAttachment(false);
 		setBackToAssignAssignment(false);
 		setBackToMyStudentThesis(false);
 	}
 	
 	/**
 	 * ActionListener that prepares back button from myStudentTheses.jspx
 	 * @param event
 	 */
 	public void actionPrepareBackButtonFromMyStudentTheses(ActionEvent event) {
 		setBackToDisplayAssignments(false);
 		setBackToAssignmentAttachment(false);
 		setBackToAssignAssignment(false);
 		setBackToMyStudentThesis(true);
 	}
 
 	public void valueChanged(ValueChangeEvent event) {
 	  UIInput source = (UIInput)event.getSource();
 	  Object newValue = event.getNewValue();
 	  
 	  ValueBinding valueBinding = source.getValueBinding("value");
 	  valueBinding.setValue(FacesContext.getCurrentInstance(), newValue);
 	}
 	
 	/**
 	 * ActionListener that sets all fields on currentAssignment
 	 * @param event
 	 */
 	public void actionUpdateCurrentAssignment(ActionEvent event) {				
 
 	  // TODO what is this?
 		//currentAssignment.setDepartmentName(employeeService.getDepartmentNameFromIndex(currentAssignment.getDepartmentNumber()));
 		//currentAssignment.setDepartmentCode(employeeService.getDepartmentCodeFromIndex(currentAssignment.getDepartmentNumber()));
 	  //employeeService.getStudyProgramNameFromCode(currentAssignment.getStudyProgramCode());
 		//currentAssignment.setStudyProgramName(employeeService.getSelectedStudyProgramNameFromIndex(currentAssignment.getStudyProgramCode()));
 		Calendar calendar = Calendar.getInstance();
 		currentAssignment.setAddedDate(calendar);
 		calendar = Calendar.getInstance();
 		calendar.add(Calendar.MONTH, Assignment.ACTIVE_MONTHS);
 		currentAssignment.setExpireDate(calendar);
 		//currentAssignment.setType(AssignmentType.BACHELOR);
 		FacesContext fc = FacesContext.getCurrentInstance();
 		String outcome = "assignmentAttachment";
 		
 		int numberOfStudentsInput = currentAssignment.getNumberOfStudents(); 
 		if (numberOfStudentsInput <= 0) {
 		  numberOfStudentsInput = 1;
 		}
 		if(currentAssignment.getType().equals(AssignmentType.MASTER) && numberOfStudentsInput > 1) {
 			currentAssignment.setNumberOfStudents(1);
 
 			// add an error message  to the context
 			UIComponent component = (UIComponent)event.getSource();
 			String forClientId = component.getClientId(fc);
 			MessageUtils.addMessage(FacesMessage.SEVERITY_ERROR, "max_master_assignment", new Object[] {1}, forClientId);
 			outcome = "error";
 		}
 		
 		for (Supervisor supervisor : currentAssignment.getSupervisorList()) {
 			if(!supervisor.isExternal()) {
 				Employee employee = employeeService.getEmployeeFromName(supervisor.getName());
 				supervisor.setName(employee.getName());
 			}
 		}
 		currentAssignment.setFacultySupervisor(employeeService.getEmployeeFromName(currentAssignment.getFacultySupervisor().getName()));
 		
 		currentAssignment.setAuthor(employeeService.getLoggedInEmployee());		
 		
     ActionSource actionSource = (ActionSource)event.getComponent();
     MethodBinding methodBinding = actionSource.getAction();
 
     String fromAction = methodBinding != null ? methodBinding.getExpressionString() : "";
     NavigationHandler navigationHandler = fc.getApplication().getNavigationHandler();
     navigationHandler.handleNavigation(fc,
                                        fromAction,
                                        outcome);
     //Render Response if needed
     fc.renderResponse();
 		
 	}
 	
 	/**
 	 * ValueChangeListener updates list of Assignments on user action
 	 * @param event
 	 */
 	public void actionShowExpired(ValueChangeEvent event) {
 		if(event.getNewValue().equals(true)) {
 			employeeService.getAllAssignmentsSet();			
 		} else {
 			employeeService.getActiveAssignmentsSet();
 		}
 		employeeService.setDisplayAssignments();
 	}
 	
 	public boolean isAutoUpload() {
 	  return this.autoUpload;
 	}
 	
 	public void setAutoUpload(boolean autoUpload) {
 	  this.autoUpload = autoUpload;
 	}
 	
 	public void fileUploadProgress(EventObject event) {
 	  InputFile inputFile = (InputFile)event.getSource();
 	  uploadProgress = inputFile.getFileInfo().getPercent();
 	}
 	
 	public int getUploadProgress() {
 	  return this.uploadProgress;
 	}
 	
 	/**
 	 * ActionListener that handles file uploading
 	 * @param event
 	 */
 	public void actionFileUpload(ActionEvent event){
 		InputFile inputFile =(InputFile) event.getSource();
     FileInfo fileInfo = inputFile.getFileInfo();
 
     switch(fileInfo.getStatus()) {
       case FileInfo.SAVED:
         try {
           addAttachment(fileInfo);
         } catch(Exception e) {
           MessageUtils.addMessage(FacesMessage.SEVERITY_ERROR, "msg_could_not_upload", new Object[] {e.getLocalizedMessage()});
         }
         break;
       case FileInfo.INVALID:
         MessageUtils.addMessage(FacesMessage.SEVERITY_ERROR, "msg_could_not_upload", null);
         break;
       case FileInfo.SIZE_LIMIT_EXCEEDED:
         MessageUtils.addMessage(FacesMessage.SEVERITY_ERROR, "msg_exceeded_size_limit", null);
         break;
       case FileInfo.INVALID_CONTENT_TYPE:
         MessageUtils.addMessage(FacesMessage.SEVERITY_ERROR, "msg_could_not_upload", null);
         break;
       case FileInfo.INVALID_NAME_PATTERN:
         MessageUtils.addMessage(FacesMessage.SEVERITY_ERROR, "msg_attachment_type_restrictions", null);
         break;
     }        
 	}
 
   private void addAttachment(FileInfo fileInfo) throws IOException, FileNotFoundException {
     Attachment attachment = new Attachment();
     attachment.setContentType(fileInfo.getContentType());
     attachment.setFileName(fileInfo.getFileName());
     ByteArrayOutputStream bout = new ByteArrayOutputStream();
     IOUtils.copyLarge(new FileInputStream(fileInfo.getFile()), bout);
     attachment.setData(bout.toByteArray());
     synchronized(currentAssignment) {
       currentAssignment.getAttachments().add(attachment);
     }
   }
 	
 	/**
 	 * ValueChangeListener to set assignment type
 	 * @param event
 	 */
 	public void actionChangeAssignmentTypeRadioListener(ValueChangeEvent event){
 		String newValue = event.getNewValue().toString();
 		AssignmentType type = AssignmentType.valueOf(newValue);
     if (type.equals(AssignmentType.MASTER)){
 			currentAssignment.setType(AssignmentType.MASTER);
 		} else {
 			currentAssignment.setType(AssignmentType.BACHELOR);
 		}
 	}
 	
 	/**
 	 * ActionListener that removes an uploaded attachment
 	 * @param event
 	 */
 	public void actionRemoveAttachment(ActionEvent event) {
 	  FacesContext fc = FacesContext.getCurrentInstance();
 	  String fileToRemove = (String)fc.getExternalContext().getRequestParameterMap().get("fileName");
 	  synchronized (currentAssignment) {
   	  Iterator<Attachment> iter = currentAssignment.getAttachments().iterator();
   	  while(iter.hasNext()) {
   	    Attachment att = iter.next();
   	    if (att.getFileName().equals(fileToRemove)) {
   	      iter.remove();
   	      break;
   	    }
   	  }
 	  }
 	}
 
 	public List<Resource> getCurrentAttachmentResources() {
 	  List<Attachment> attachments = currentAssignment.getAttachments();
 	  List<Resource> resources = new ArrayList<Resource>(attachments.size());
 	  for (Attachment attachment : attachments) {
       Resource res = new AttachmentResource(attachment.getData(), attachment.getFileName(), attachment.getContentType());
       resources.add(res);
     }
 	  return resources;
 	}
 	
 	public List<SelectItem> getAssignmentTypes() {
 	  AssignmentType[] values = AssignmentType.values();
 	  List<SelectItem> items = new ArrayList<SelectItem>(values.length);
     for (AssignmentType type : values) {
       items.add(new SelectItem(type, type.toString()));
     }
     return items;
 	}
 	
 	public boolean isBackToAssignAssignment() {
 		return backToAssignAssignment;
 	}
 
 	public void setBackToAssignAssignment(boolean backToAssignAssignment) {
 		this.backToAssignAssignment = backToAssignAssignment;
 	}
 
 	public boolean isBackToAssignmentAttachment() {
 		return backToAssignmentAttachment;
 	}
 
 	public void setBackToAssignmentAttachment(boolean backToAssignmentAttachment) {
 		this.backToAssignmentAttachment = backToAssignmentAttachment;
 	}
 
 	public boolean isBackToDisplayAssignments() {
 		return backToDisplayAssignments;
 	}
 
 	public void setBackToDisplayAssignments(boolean backToDisplayAssignments) {
 		this.backToDisplayAssignments = backToDisplayAssignments;
 	}
 
 	public boolean isShowExpired() {
 		return showExpired;
 	}
 
 	public void setShowExpired(boolean showExpired) {
 		this.showExpired = showExpired;
 	}
 
 	public Assignment getCurrentAssignment() {
 		return currentAssignment;
 	}
 
 	public String getCurrentAssignmentDepartmentCode() {
 	  String code = currentAssignment.getDepartmentCode();
     return employeeService.getDepartmentNameFromCode(code);  
 	}
 	
 	public String getCurrentAssignmentStudyProgramCode() {
 	  String code = currentAssignment.getStudyProgramCode();
     return employeeService.getStudyProgramNameFromCode(code);
 	}
 	
 	public void setCurrentAssignment(Assignment currentAssignment) {
 		this.currentAssignment = currentAssignment;
 	}
 
 	public boolean isBackToMyStudentThesis() {
 		return backToMyStudentThesis;
 	}
 
 	public void setBackToMyStudentThesis(boolean backToMyStudentThesis) {
 		this.backToMyStudentThesis = backToMyStudentThesis;
 	}
 
 	public Thesis getCurrentThesis() {
 		return currentThesis;
 	}
 
 	public void setCurrentThesis(Thesis currentThesis) {
 		this.currentThesis = currentThesis;
 	}
 
 	public void dispose() throws Exception {
 	}	
 }
