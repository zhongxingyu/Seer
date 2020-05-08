 package no.uis.portal.employee;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.event.ActionEvent;
 
 import no.uis.abam.dom.ExternalExaminer;
 import no.uis.abam.dom.Thesis;
 import no.uis.abam.dom.ThesisInformation;
 
 import com.icesoft.faces.context.DisposableBean;
 
 public class ExternalExaminerBean implements DisposableBean{
 
 	private List<ThesisInformation> thesisInformationList = new ArrayList<ThesisInformation>();
 	
 	private EmployeeService employeeService;
 	
 	private ExternalExaminer externalExaminer;
 	
 	public ExternalExaminerBean() {
 		
 	}
 	
 	public void actionPrepareAddExternalExaminer(ActionEvent event) {
 		thesisInformationList.clear();
 		setExternalExaminer(new ExternalExaminer());
 		//TODO: don't get all theses.
 		List<Thesis> thesisList = employeeService.getThesisList();
 		ThesisInformation thesisInformation;
 		for (Thesis thesis : thesisList) {
 			thesisInformation = new ThesisInformation();
 			thesisInformation.setAssignmentTitle(employeeService
 					.getAssignmentFromId(thesis.getAssignedAssignmentId())
 					.getTitle());
 			thesisInformation.setCoStudent1Name(thesis.getCoStudent1());
 			thesisInformation.setCoStudent2Name(thesis.getCoStudent2());
 			ExternalExaminer examiner = thesis.getExternalExaminer();
 			if (examiner == null) {
 				thesisInformation.setExternalExaminerName("");
 			} else {
 				thesisInformation.setExternalExaminerName(thesis.getExternalExaminer().getName());
 			}
 			thesisInformation.setStudentName(employeeService
 					.getStudentFromStudentNumber(thesis.getStudentNumber())
 					.getName());
 			thesisInformation.setThesis(thesis);	
 			thesisInformationList.add(thesisInformation);
 		}
 	}
 
 	public List<ThesisInformation> getThesisInformationList() {
 		return thesisInformationList;
 	}
 
 	public void setThesisInformationList(
 			List<ThesisInformation> thesisInformationList) {
 		this.thesisInformationList = thesisInformationList;
 	}
 
 	public EmployeeService getEmployeeService() {
 		return employeeService;
 	}
 
 	public void setEmployeeService(EmployeeService employeeService) {
 		this.employeeService = employeeService;
 	}
 
 	public ExternalExaminer getExternalExaminer() {
 		return externalExaminer;
 	}
 
 	public void setExternalExaminer(ExternalExaminer externalExaminer) {
 		this.externalExaminer = externalExaminer;
 	}
 
 	@Override
 	public void dispose() throws Exception {
 	}
 
 }
