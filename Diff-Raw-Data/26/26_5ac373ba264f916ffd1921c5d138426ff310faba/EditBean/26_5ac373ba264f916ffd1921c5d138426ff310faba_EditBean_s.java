 package no.uis.portal.employee;
 
 import java.util.List;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.html.HtmlDataTable;
 import javax.faces.event.ActionEvent;
 
 import no.uis.abam.dom.EditableSelectItem;
 
 import com.icesoft.faces.context.DisposableBean;
 
 public class EditBean implements DisposableBean {
 
 	private EmployeeService employeeService;
 	
 	public EditBean(){
 	}
 
 	public void actionSetEditable(ActionEvent event) {
 		EditableSelectItem editItem = (EditableSelectItem) getRowFromEvent(event);
 		editItem.setEditable(!editItem.isEditable());			
 	}
 	
 	private Object getRowFromEvent(ActionEvent event) {
 		UIComponent uic = event.getComponent();		
 		HtmlDataTable table = (HtmlDataTable)uic.getParent().getParent();
 		return table.getRowData();
 	}
 	
 	public void actionAddNewStudyProgram(ActionEvent event){
 		List<EditableSelectItem> studyProgramList = getSelectedStudyProgramList();
 		EditableSelectItem newItem = new EditableSelectItem(new Integer(studyProgramList.size()), "");
 		newItem.setEditable(true);
 		studyProgramList.add(newItem);
 	}
 	
 	public void actionAddNewDepartment(ActionEvent event){
 		employeeService.addNewDepartment();		
 	}
 	
 	public void actionRemoveStudyProgram(ActionEvent event) {		
 		getSelectedStudyProgramList().remove(getRowFromEvent(event));			
 	}
 	
 	public void actionRemoveDepartment(ActionEvent event) {		
 		employeeService.removeDepartment((EditableSelectItem) getRowFromEvent(event));	
 	}
 	
 	public void actionSaveDepartmentListToWebService(ActionEvent event) {
 		employeeService.saveDepartmentListToWebService();
 	}
 	
 	public void actionGetDepartmentListFromWebService(ActionEvent event) {
 		employeeService.getDepartmentListFromWebService();
 	}
 	
 	public void setEmployeeService(EmployeeService employeeService) {
 		this.employeeService = employeeService;
 	}
 	
 	public List<EditableSelectItem> getSelectedStudyProgramList() {
 		return employeeService.getSelectedStudyProgramList();
 	}
 
 	public void dispose() throws Exception {
 		
 	}
 
 	
 }
