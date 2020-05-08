 package no.uis.portal.student;
 
 import java.util.GregorianCalendar;
 
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 
 import no.uis.abam.dom.Assignment;
 import no.uis.abam.dom.Thesis;
 
 import com.icesoft.faces.context.DisposableBean;
 
 public class ThesisBean implements DisposableBean {
 
 	private StudentService studentService;
 	
 	private Thesis currentStudentsThesis;
 	private Assignment currentAssignment;
 	
 	private boolean renderAcceptButton = false;
 	private boolean renderNotAssigned;
 	private boolean renderMyThesis;
 	private boolean readRules1;
 	private boolean readRules2;
 	private boolean readRules3;
 	
 	
 	public ThesisBean() {
 	}
 	
 	public void actionGetInformationForStudent(ActionEvent event) {
 		setRenderAcceptButtonFalse();
 
 		studentService.updateCurrentStudentFromWebService();
 		currentStudentsThesis = studentService.getCurrentStudent().getAssignedThesis();
 		if (currentStudentsThesis != null) {
 			if (currentStudentsThesis.getAssignedAssignmentId() == 0) {
 				currentAssignment = studentService.getCurrentStudent()
 						.getCustomAssignment();
 			} else {
 				currentAssignment = studentService
 						.getAssignmentFromId(currentStudentsThesis
 								.getAssignedAssignmentId());
 			}
 			if (currentStudentsThesis.isAccepted()) {
 				readRules1 = true;
 				readRules2 = true;
 				readRules3 = true;
 			}
 			renderNotAssigned = false;
 		} else {
 			renderNotAssigned = true;
 		}
 	}
 	
 	private void setRenderAcceptButtonFalse() {
 		renderAcceptButton = false;
 	}
 	
 	public void actionCheckBoxChanges(ValueChangeEvent event) {
 		String id = event.getComponent().getId();
 		if (id.equals("readRules1")) {
 			readRules1 = (Boolean)event.getNewValue();
 		} else if (id.equals("readRules2")) {
 			readRules2 = (Boolean)event.getNewValue();
 		} else if (id.equals("readRules3")) {
 			readRules3 = (Boolean)event.getNewValue();
 		}
 		if(readRules1 && readRules2 && readRules3) renderAcceptButton = true;
 		else renderAcceptButton = false;
 	}
 	
 	public void actionSetThesisIsAccepted(ActionEvent event) {
 		currentStudentsThesis.setAccepted(true);
 		currentStudentsThesis.setActualSubmissionOfTopic(GregorianCalendar.getInstance().getTime());
 		studentService.getCurrentStudent().setAssignedThesis(currentStudentsThesis);
 		studentService.updateStudentInWebServiceFromCurrentStudent();
 		actionInitMyThesisPage(event);
 	}
 	
 	public void actionInitMyThesisPage(ActionEvent event) {
 		if (currentStudentsThesis == null) {
 			renderMyThesis = false;
 		} else {
 			renderMyThesis = true;
 		}
 	}
 	
 	public void setStudentService(StudentService studentService) {
 		this.studentService = studentService;
 	}
 	
 	public Thesis getCurrentStudentsThesis() {
 		return currentStudentsThesis;
 	}
 
 	public void setCurrentStudentsThesis(Thesis currentStudentsThesis) {
 		this.currentStudentsThesis = currentStudentsThesis;
 	}
 
 	public Assignment getCurrentAssignment() {
 		return currentAssignment;
 	}
 
 	public void setCurrentAssignment(Assignment currentAssignment) {
 		this.currentAssignment = currentAssignment;
 	}
 
 	public boolean isRenderAcceptButton() {
 		return renderAcceptButton;
 	}
 
 	public void setRenderAcceptButton(boolean enableAcceptButton) {
 		this.renderAcceptButton = enableAcceptButton;
 	}
 
 	
 	
 	public boolean isReadRules1() {
 		return readRules1;
 	}
 
 	public void setReadRules1(boolean readRules1) {
 		this.readRules1 = readRules1;
 	}
 
 	public boolean isReadRules2() {
 		return readRules2;
 	}
 
 	public void setReadRules2(boolean readRules2) {
 		this.readRules2 = readRules2;
 	}
 
 	public boolean isReadRules3() {
 		return readRules3;
 	}
 
 	public void setReadRules3(boolean readRules3) {
 		this.readRules3 = readRules3;
 	}
 
 	public boolean isRenderNotAssigned() {
 		return renderNotAssigned;
 	}
 
 	public void setRenderNotAssigned(boolean renderNotAssigned) {
 		this.renderNotAssigned = renderNotAssigned;
 	}
 
 	public boolean isRenderMyThesis() {
 		return renderMyThesis;
 	}
 
 	public void setRenderMyThesis(boolean renderMyThesis) {
 		this.renderMyThesis = renderMyThesis;
 	}
 
 	public void dispose() throws Exception {}
 
 }
