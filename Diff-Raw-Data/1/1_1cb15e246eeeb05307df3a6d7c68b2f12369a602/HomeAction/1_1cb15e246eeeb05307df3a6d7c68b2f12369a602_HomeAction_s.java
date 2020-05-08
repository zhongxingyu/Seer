 package edu.uoc.pelp.actions;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.struts2.ServletActionContext;
 import org.apache.struts2.convention.annotation.InterceptorRef;
 import org.apache.struts2.convention.annotation.InterceptorRefs;
 import org.apache.struts2.convention.annotation.Namespace;
 import org.apache.struts2.convention.annotation.ParentPackage;
 import org.apache.struts2.convention.annotation.Result;
 import org.apache.struts2.convention.annotation.ResultPath;
 import org.apache.struts2.convention.annotation.Results;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 
 import edu.uoc.pelp.bussines.UOC.UOCPelpBussines;
 import edu.uoc.pelp.bussines.UOC.vo.UOCClassroom;
 import edu.uoc.pelp.bussines.UOC.vo.UOCSubject;
 import edu.uoc.pelp.bussines.vo.Activity;
 import edu.uoc.pelp.bussines.vo.DeliverDetail;
 import edu.uoc.pelp.bussines.vo.DeliverSummary;
 import edu.uoc.pelp.bussines.vo.UserInformation;
 import edu.uoc.pelp.engine.campus.UOC.CampusConnection;
 import edu.uoc.pelp.exception.PelpException;
 import edu.uoc.pelp.test.tempClasses.LocalCampusConnection;
 
 
 /**
  * 
  * Main action class for PeLP. Merged code from StudentAction and TeacherAction.
  * 
  * @author oripolles
  * 
  */
 @ParentPackage(value = "default")
 @InterceptorRefs(value = { @InterceptorRef(value = "langInterceptor"), @InterceptorRef(value = "defaultStack") })
 
 @Namespace("/")
 @ResultPath(value = "/")
 @Results({
 		@Result(name = "success", location = "jsp/home.jsp"),
 		@Result(name = "programming-environment", location = "jsp/deliveries.jsp"),
 		@Result(name="rsuccess", type="redirectAction", params = {"actionName" , "home"}),
 		@Result(name="dinamic", type="json"),
 		@Result(name = "filedown", type = "stream", params =
 		{
 		    "contentType",
 		    "application/octet-stream",
 		    "inputName",
 		    "fileInputStream",
 		    "contentDisposition",
 		    "filename=\"${filename}\""
 		}),
 	    @Result(name="rprogramming-environment", type="redirectAction", params = {"actionName" , "home.html?activeTab=programming-environment"})
 		
 })
 
 public class HomeAction extends ActionSupport {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3165462908903079864L;
 	public static final String TAB_DELIVERIES = "deliveries";
 	public static final String TAB_PROGRAMMING_ENVIROMENT = "programming-environment";
 
 	private UOCPelpBussines bUOC;
 
 	private UOCSubject[] listSubjects;
 	private UOCClassroom[] listClassroms;
 	private Activity[] listActivity;
 	private DeliverSummary[] listDeliverSummaries;
 	private DeliverDetail[] listDeliverDetails;
 	
 	private int fileDim;
 
 	private String s_assign;
 	private String s_aula;
 	private String s_activ;
 
 	private String username;
 	private String password;
 	private String imageURL;
 	private String fullName;
 
 	private String activeTab;
 	private boolean ajaxCall = true;
 	private String selectorToLoad;
 	private String idDelivers;
 	private String idFile;
 	private String filename;
 	private InputStream fileInputStream;
 	private String timeFile;
 	private String maxDelivers;
 
 	private boolean teacher;
 
 	@Override
 	public String execute() throws Exception {
 
 		//UOC API
 		HttpServletRequest request = ServletActionContext.getRequest();
     	
     	String token = (String) request.getSession().getAttribute("access_token");
     	
     	if( token != null) {
             WebApplicationContext context =
         			WebApplicationContextUtils.getRequiredWebApplicationContext(
                                             ServletActionContext.getServletContext()
                                 );
             CampusConnection campusConnection = (CampusConnection) context.getBean("lcctj");
             campusConnection.setCampusSession(token);
             bUOC.setCampusConnection(campusConnection);
     	}
 		
 		UserInformation userInfo = bUOC.getUserInformation();
 		if (userInfo != null) {
 			listSubjects = bUOC.getUserSubjects();
 			if (s_assign != null) {
 				String[] infoAssing = s_assign.split("_");
 				teacher = bUOC.isTeacher(new UOCSubject(infoAssing[0],
 						infoAssing[2]));
 				listClassroms = bUOC.getUserClassrooms(new UOCSubject(
 						infoAssing[0], infoAssing[2]));
 			}
 			if (s_aula != null && s_aula.length() > 0 && s_assign != null) {
 				String[] infoAssing = s_assign.split("_");
 				listActivity = bUOC.getSubjectActivities(new UOCSubject(
 						infoAssing[0], infoAssing[2]));
 			}
 			if (s_aula != null && s_aula.length() > 0 && s_assign != null
 					&& s_activ != null && s_activ.length() > 0) {
 				Activity objActivity = new Activity();
 				for (int j = 0; j < listActivity.length; j++) {
 					if (listActivity[j].getIndex() == Integer.parseInt(s_activ)) {
 						objActivity = listActivity[j];
 					}
 				}
 				String[] infoAssing = s_assign.split("_");
 				if(teacher){
 					UOCClassroom objClassroom = null;
 					for(int i = 0;i<listClassroms.length;i++){
 						if(listClassroms[i].getIndex() == Integer.parseInt(s_aula)){
 							objClassroom = listClassroms[i];
 						}
 					}
 					//listDeliverDetails = bUOC.getAllClassroomDeliverDetails(objActivity, objClassroom);
 					listDeliverDetails = bUOC.getLastClassroomDeliverDetails(objActivity, objClassroom);
 					if(listDeliverDetails.length>0&& listDeliverDetails!= null)maxDelivers =  String.valueOf(listDeliverDetails[0].getMaxDelivers());
 					//this.listStudents(listDeliverDetails);
 				}else{
 					listDeliverDetails = bUOC.getUserDeliverDetails(new UOCSubject(
 							infoAssing[0], infoAssing[2]), objActivity.getIndex());	
 					if(listDeliverDetails.length>0&& listDeliverDetails!= null)maxDelivers =  String.valueOf(listDeliverDetails[0].getMaxDelivers());
 				}
 			}
 			imageURL = userInfo.getUserPhoto();
 			if(imageURL== null)imageURL = "img/user.png";
 			fullName = userInfo.getUserFullName();
 		} else {
 			imageURL = null;
 			fullName = null;
 		}
 
 		String toReturn = SUCCESS;
 
 		if (TAB_PROGRAMMING_ENVIROMENT.equals(activeTab)) {
 			toReturn = TAB_PROGRAMMING_ENVIROMENT;
 		}
 
 		return toReturn;
 	}
 	
 	public String combo() throws PelpException{
 		
 		if (s_assign != null) {
 			String[] infoAssing = s_assign.split("_");
 			teacher = bUOC.isTeacher(new UOCSubject(infoAssing[0],
 					infoAssing[2]));
 			listClassroms = bUOC.getUserClassrooms(new UOCSubject(
 					infoAssing[0], infoAssing[2]));
 		}
 		if (s_aula != null && s_aula.length() > 0 && s_assign != null) {
 			String[] infoAssing = s_assign.split("_");
 			listActivity = bUOC.getSubjectActivities(new UOCSubject(
 					infoAssing[0], infoAssing[2]));
 		}
 		
 		return "dinamic";
 	}
 
 	public String down() throws Exception {
 		
 		if (s_aula != null && s_aula.length() > 0 && s_assign != null
 				&& s_activ != null && s_activ.length() > 0) {
 			
 			String[] infoAssing = s_assign.split("_");
 			listActivity = bUOC.getSubjectActivities(new UOCSubject(
 					infoAssing[0], infoAssing[2]));
 			
 			Activity objActivity = new Activity();
 			for (int j = 0; j < listActivity.length; j++) {
 				if (listActivity[j].getIndex() == Integer.parseInt(s_activ)) {
 					objActivity = listActivity[j];
 				}
 			}
 			
 			listDeliverDetails = bUOC.getUserDeliverDetails(new UOCSubject(
 					infoAssing[0], infoAssing[2]), objActivity.getIndex());
 			
 			String urlpath = listDeliverDetails[Integer.parseInt(idDelivers)].getDeliverFiles()[Integer.parseInt(idFile)].getAbsolutePath();
 			File file = new File(urlpath);
 			fileInputStream = new FileInputStream(file);
 			filename = listDeliverDetails[Integer.parseInt(idDelivers)].getDeliverFiles()[Integer.parseInt(idFile)].getRelativePath();
 		}
 		
 		return "filedown";
 	}
 	
 	public String logout() throws PelpException {
 		/*HttpServletRequest request = ServletActionContext.getRequest();
     	request.getSession().setAttribute("authUOC", "close");
     	bUOC.setCampusConnection(new CampusConnection());
     	bUOC.logout();
         
 		String toReturn = 'r'+SUCCESS;
 
 		if (TAB_PROGRAMMING_ENVIROMENT.equals(activeTab)) {
 			toReturn = 'r'+TAB_PROGRAMMING_ENVIROMENT;
 		}
 
 		return toReturn;
 		*/
 		//bUOC.logout();
 		HttpServletRequest request = ServletActionContext.getRequest();
     	request.getSession().setAttribute("authUOC", "close");
 		LocalCampusConnection _campusConnection = new LocalCampusConnection();
 		        // Add the register to the admin database to give administration rights
 		        _campusConnection.setProfile("none");
 		        
 		        bUOC.setCampusConnection(_campusConnection);
 		        
 		String toReturn = 'r'+SUCCESS;
 
 		if (TAB_PROGRAMMING_ENVIROMENT.equals(activeTab)) {
 		toReturn = 'r'+TAB_PROGRAMMING_ENVIROMENT;
 		}
 
 		return toReturn;
 	}
 
 	public String authLocal() throws Exception {
 		//FIXME Miramos Si es estudiante , professor i dependiendo usaremos uno o otro
 		LocalCampusConnection _campusConnection = new LocalCampusConnection();
 		_campusConnection.setProfile(username);			  	
 		bUOC.setCampusConnection(_campusConnection);
 
 		String toReturn = 'r'+SUCCESS;
 
 		if (TAB_PROGRAMMING_ENVIROMENT.equals(activeTab)) {
 			toReturn = 'r'+TAB_PROGRAMMING_ENVIROMENT;
 		}
 
 		return toReturn;
 	}
 
 	public String auth() throws Exception {
 
 		HttpServletRequest request = ServletActionContext.getRequest();
 		request.getSession().setAttribute("authUOC", "request");
 		
 		if(bUOC != null && bUOC.getUserInformation()!=null){
 			String lang = bUOC.getUserInformation().getLanguage();
 			System.out.println("IDIOMA USUARIO: "+lang);
 			Map session = ActionContext.getContext().getSession();	
 		  	if(lang.equals("ca")){
 		  		session.put("WW_TRANS_I18N_LOCALE",new java.util.Locale("ca"));
 		  		Locale locale = new Locale("ca", "ES");
 		  		 	session.put("org.apache.tiles.LOCALE", locale);
 		  	}else if(lang.equals("es")){
 		  		session.put("WW_TRANS_I18N_LOCALE",new java.util.Locale("es"));
 		  		Locale locale = new Locale("es", "ES");
 		  		 	session.put("org.apache.tiles.LOCALE", locale);
 		  	}else if(lang.equals("en")){
 		  		session.put("WW_TRANS_I18N_LOCALE",new java.util.Locale("en"));
 		  		Locale locale = new Locale("en", "UK");
 		  		 	session.put("org.apache.tiles.LOCALE", locale);
 		  	}   	
 		}
 
 		String toReturn = 'r'+SUCCESS;
 
 
 		if (TAB_PROGRAMMING_ENVIROMENT.equals(activeTab)) {
 		toReturn = 'r'+TAB_PROGRAMMING_ENVIROMENT;
 		}
 
 		return toReturn;
 	}
 	
 	public UOCPelpBussines getbUOC() {
 		return bUOC;
 	}
 
 	public void setbUOC(UOCPelpBussines bUOC) {
 		this.bUOC = bUOC;
 	}
 
 	public UOCSubject[] getListSubjects() {
 		return listSubjects;
 	}
 
 	public void setListSubjects(UOCSubject[] listSubjects) {
 		this.listSubjects = listSubjects;
 	}
 
 	public UOCClassroom[] getListClassroms() {
 		return listClassroms;
 	}
 
 	public void setListClassroms(UOCClassroom[] listClassroms) {
 		this.listClassroms = listClassroms;
 	}
 
 	public Activity[] getListActivity() {
 		return listActivity;
 	}
 
 	public void setListActivity(Activity[] listActivity) {
 		this.listActivity = listActivity;
 	}
 
 	public DeliverSummary[] getListDeliverSummaries() {
 		return listDeliverSummaries;
 	}
 
 	public void setListDeliverSummaries(DeliverSummary[] listDeliverSummaries) {
 		this.listDeliverSummaries = listDeliverSummaries;
 	}
 
 	public DeliverDetail[] getListDeliverDetails() {
 		return listDeliverDetails;
 	}
 
 	public void setListDeliverDetails(DeliverDetail[] listDeliverDetails) {
 		this.listDeliverDetails = listDeliverDetails;
 	}
 
 	public String getS_assign() {
 		return s_assign;
 	}
 
 	public void setS_assign(String s_assign) {
 		this.s_assign = s_assign;
 	}
 
 	public String getS_aula() {
 		return s_aula;
 	}
 
 	public void setS_aula(String s_aula) {
 		this.s_aula = s_aula;
 	}
 
 	public String getS_activ() {
 		return s_activ;
 	}
 
 	public void setS_activ(String s_activ) {
 		this.s_activ = s_activ;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getImageURL() {
 		return imageURL;
 	}
 
 	public void setImageURL(String imageURL) {
 		this.imageURL = imageURL;
 	}
 
 	public String getFullName() {
 		return fullName;
 	}
 
 	public void setFullName(String fullName) {
 		this.fullName = fullName;
 	}
 
 	public String getActiveTab() {
 		return activeTab;
 	}
 
 	public void setActiveTab(String activeTab) {
 		this.activeTab = activeTab;
 	}
 
 	public boolean isAjaxCall() {
 		return ajaxCall;
 	}
 
 	public void setAjaxCall(boolean ajaxCall) {
 		this.ajaxCall = ajaxCall;
 	}
 
 	public String getSelectorToLoad() {
 		return selectorToLoad;
 	}
 
 	public void setSelectorToLoad(String selectorToLoad) {
 		this.selectorToLoad = selectorToLoad;
 	}
 
 	public boolean isTeacher() {
 		return teacher;
 	}
 
 	public void setTeacher(boolean teacher) {
 		this.teacher = teacher;
 	}
 
 	public String getIdDelivers() {
 		return idDelivers;
 	}
 
 	public void setIdDelivers(String idDelivers) {
 		this.idDelivers = idDelivers;
 	}
 
 	public String getIdFile() {
 		return idFile;
 	}
 
 	public void setIdFile(String idFile) {
 		this.idFile = idFile;
 	}
 
 	public InputStream getFileInputStream() {
 		return fileInputStream;
 	}
 
 	public void setFileInputStream(InputStream fileInputStream) {
 		this.fileInputStream = fileInputStream;
 	}
 
 	public String getFilename() {
 		return filename;
 	}
 
 	public void setFilename(String filename) {
 		this.filename = filename;
 	}
 
 	public int getFileDim() {
 		return fileDim;
 	}
 
 	public void setFileDim(int fileDim) {
 		this.fileDim = fileDim;
 	}
 
 	public String getTimeFile() {
 		return timeFile;
 	}
 
 	public void setTimeFile(String timeFile) {
 		this.timeFile = timeFile;
 	}
 
 	public String getMaxDelivers() {
 		return maxDelivers;
 	}
 
 	public void setMaxDelivers(String maxDelivers) {
 		this.maxDelivers = maxDelivers;
 	}
 
 }
