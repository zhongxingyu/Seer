 package no.uis.portal.employee;
 
 import java.util.Map;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import com.icesoft.faces.context.DisposableBean;
 import com.liferay.portal.service.ServiceContext;
 
 public class AssignmentBean implements DisposableBean {
 
 	private int id;
 	private int numberOfStudents;
 	
 	private String title;
 	private String instructor; //Change this to a seperate class later?
 	private String technicalResponsible;
 	private String description;
 	private String studyProgram;
 	private String institute;
 	
 	private boolean master;
 	private boolean bachelor;
 	
 	private Logger log = Logger.getLogger(AssignmentBean.class); 
 	
 	public AssignmentBean(){
 	}
 	
 	public void listen(ActionEvent event) {
 		UIComponent comp = event.getComponent();
 		FacesContext context = FacesContext.getCurrentInstance();
 		
 		String clientId = event.getComponent().getClientId(context);
 		clientId = clientId.replaceAll("CreateButton", "");
 		
 		Map<?,?> parameterMap = context.getExternalContext().getRequestParameterMap();
 		
 		log.setLevel(Level.DEBUG);
 		if (log.isDebugEnabled()) {
			log.debug("Title2: "+parameterMap.get(clientId+"title"));
 			log.debug("Des: "+parameterMap.get(clientId+"description"));
 			log.debug("Instructor: "+parameterMap.get(clientId+"instructor"));
 			log.debug("TechRes: "+parameterMap.get(clientId+"technicalResponsible"));
 			log.debug("Institute: "+parameterMap.get(clientId+"institute"));
 			log.debug("StudyProgram: "+parameterMap.get(clientId+"studyProgram"));
 			log.debug("NumberOfStudents: "+parameterMap.get(clientId+"numberOfStudents"));
 			log.debug("type: "+parameterMap.get(clientId+"type"));
 		}
 	}
 	
 	public void dispose() throws Exception {
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getInstructor() {
 		return instructor;
 	}
 
 	public void setInstructor(String instructor) {
 		this.instructor = instructor;
 	}
 
 	public String getTechnicalResponsible() {
 		return technicalResponsible;
 	}
 
 	public void setTechnicalResponsible(String technicalResponsible) {
 		this.technicalResponsible = technicalResponsible;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public int getNumberOfStudents() {
 		return numberOfStudents;
 	}
 
 	public void setNumberOfStudents(int numberOfStudents) {
 		this.numberOfStudents = numberOfStudents;
 	}
 
 	public String getStudyProgram() {
 		return studyProgram;
 	}
 
 	public void setStudyProgram(String studyProgram) {
 		this.studyProgram = studyProgram;
 	}
 
 	public String getInstitute() {
 		return institute;
 	}
 
 	public void setInstitute(String institute) {
 		this.institute = institute;
 	}
 
 	public boolean isMaster() {
 		return master;
 	}
 
 	public void setMaster(boolean master) {
 		this.master = master;
 	}
 
 	public boolean isBachelor() {
 		return bachelor;
 	}
 
 	public void setBachelor(boolean bachelor) {
 		this.bachelor = bachelor;
 	}
 
 }
