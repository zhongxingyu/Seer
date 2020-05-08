 package server;
 
 import java.util.HashMap;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 import ctrl.DBField;
 import ctrl.DBSubject;
 
 import data.Field;
 import data.Subject;
 
 @ManagedBean(name = "DezernatBean")
 @SessionScoped
 public class DezernatBean {
 	private List<Subject> subjectList;
 	private List<Field> fieldList;
 
 	private HashMap<Subject, List<Field>> Map;
 	private Subject selected;
 
 	public DezernatBean() {
 
 		subjectList = DBSubject.loadSubjectsforDezernat();
 		Map = new HashMap<Subject, List<Field>>();
 
 		for (Subject s : subjectList) {
 			s.getSubTitle();
 			if(!DBField.loadFieldforDezernat(s.getSubTitle()).isEmpty()){
 				fieldList = DBField.loadFieldforDezernat(s.getSubTitle());
 			}
 			
 			Map.put(s, fieldList);
 			if(!(fieldList==null))System.out.println(fieldList.toString());
 		}
 		System.out.println(subjectList.toString());
 	}
 
 	public void loadFields(ActionEvent e) {
 		System.out.println("selected");
 		
 		if(selected!=null){
 			System.out.println(selected.getSubTitle());
 			fieldList=DBField.loadFieldforDezernat(selected.getSubTitle());
 		}
 	}
 	
 	public void acceptChanges(ActionEvent e){
 		System.out.println(selected.getSubTitle()+" is accepted!");
 		if(DBSubject.updateSubjectAck(true,selected.getVersion(), selected.getSubTitle(), selected.getModTitle())){
 			subjectList.remove(selected);
 			addMessage("approve","Success:" ,"Change published!");
 		}
		else addErrorMessage("approve", "Failed", "An SQL Error Occured in 'updateSubjectAck'@DBSubject, call the Administrator for further help and note this message.");
 	}
 	
 	/**
 	 * if anything goes wrong - display an Error
 	 * 
 	 * @param title
 	 * @param msg
 	 */
 	public void addErrorMessage(String toFor,String title, String msg) {
 		FacesContext.getCurrentInstance().addMessage(toFor,
 				new FacesMessage(FacesMessage.SEVERITY_FATAL, title, msg));
 	}
 
 	/**
 	 * Informs the User via message.
 	 * 
 	 * @param title
 	 * @param msg
 	 */
 	public void addMessage(String toFor,String title, String msg) {
 		FacesContext.getCurrentInstance().addMessage(toFor,
 				new FacesMessage(FacesMessage.SEVERITY_INFO, title, msg));
 	}
 	/**
 	 * @return the fieldList
 	 */
 	public List<Field> getFieldList() {
 		return fieldList;
 	}
 
 	/**
 	 * @param fieldList
 	 *            the fieldList to set
 	 */
 	public void setFieldList(List<Field> fieldList) {
 		this.fieldList = fieldList;
 	}
 
 	/**
 	 * @return the subjectList
 	 */
 	public List<Subject> getSubjectList() {
 		return subjectList;
 	}
 
 	/**
 	 * @param subjectList the subjectList to set
 	 */
 	public void setSubjectList(List<Subject> subjectList) {
 		this.subjectList = subjectList;
 	}
 
 	/**
 	 * @return the selected
 	 */
 	public Subject getSelected() {
 		return selected;
 	}
 
 	/**
 	 * @param selected the selected to set
 	 */
 	public void setSelected(Subject selected) {
 		this.selected = selected;
 	}
 
 }
