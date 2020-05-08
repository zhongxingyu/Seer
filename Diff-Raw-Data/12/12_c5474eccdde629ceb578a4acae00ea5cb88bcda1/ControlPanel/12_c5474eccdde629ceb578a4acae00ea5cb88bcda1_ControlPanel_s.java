 package com.fcl.uklug;
 
 import javax.faces.context.FacesContext;
 import java.io.Serializable;
 import lotus.domino.Database;
 import lotus.domino.DateTime;
 import lotus.domino.Document;
 import lotus.domino.NotesException;
 import lotus.domino.View;
 
 import com.ibm.domino.xsp.module.nsf.NotesContext;
 
 import java.util.Date;
 import java.util.Vector;
 
 
 public class ControlPanel implements Serializable {
 	private static final long serialVersionUID = 1L;
 	public static final String BEAN_NAME = "controlpanelbean"; // name of the bean
 	private String enableRegistration;
 	private String enableAgenda;
 	private String enableForum;
 	private String feedbackSendTo;
 	private String eventName;
 	private Date startDate;
 	private int agendaDays;
 	private int agendaRooms;
 	private String[] tracks;
 	private String registrationNAB;
 	private String registrationUsersGroup;
 	private String registrationEmailSubject;
 	private String registrationEmailBody;
 	private String registrationEmailFromEmail;
 	private String registrationEmailFromName;
 	private String forgottenEmailSubject;
 	private String forgottenEmailBody;
 	private String forgottenEmailFromEmail;
 	private String forgottenEmailFromName;
 	private String sessionEmailFromEmail;
 	private String sessionEmailFromName;
 	private String sessionEmailAcceptedSubject;
 	private String sessionEmailAcceptedBody;
 	private String sessionEmailRejectedSubject;
 	private String sessionEmailRejectedBody;
 	private String unid;
 	private String sessionVoting;
 
 	public static ControlPanel get() {
 		FacesContext context = FacesContext.getCurrentInstance();
 		ControlPanel bean = (ControlPanel) context.getApplication().getVariableResolver()
 				.resolveVariable(context, BEAN_NAME);
 		return bean;
 	}
 	
 	public ControlPanel() throws NotesException{
 		init();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void init() throws NotesException{
 		Database database = getCurrentDatabase();
 		View controlpanels = database.getView("Control Panels");
 		Document controlpaneldoc = controlpanels.getFirstDocument();
 		if (controlpaneldoc == null){
 			controlpaneldoc = database.createDocument();
 			controlpaneldoc.replaceItemValue("Form", "ControlPanel");
 			controlpaneldoc.computeWithForm(false, false);
 			controlpaneldoc.save();
 		}
 		this.setUnid(controlpaneldoc.getUniversalID());
 		this.setEnableRegistration(controlpaneldoc.getItemValueString("EnableRegistration"));
 		this.setAgendaDays(controlpaneldoc.getItemValueInteger("AgendaDays"));
 		this.setAgendaRooms(controlpaneldoc.getItemValueInteger("AgendaRooms"));
 		this.setEnableAgenda(controlpaneldoc.getItemValueString("EnableAgenda"));
 		this.setEnableForum(controlpaneldoc.getItemValueString("EnableForum"));
 		this.setEventName(controlpaneldoc.getItemValueString("EventName"));
 		this.setFeedbackSendTo(controlpaneldoc.getItemValueString("FeedbackSendTo"));
 		this.setRegistrationNAB(controlpaneldoc.getItemValueString("RegistrationNAB"));
 		this.setRegistrationUsersGroup(controlpaneldoc.getItemValueString("RegistrationUsersGroup"));
 		Vector<DateTime> dates = controlpaneldoc.getItemValueDateTimeArray("StartDate");
 		this.setStartDate(dates.elementAt(0).toJavaDate());
 		Vector<String> v = controlpaneldoc.getItemValue("Tracks");
 		this.setTracks(v.toArray(new String[v.size()]));
 		this.setRegistrationEmailSubject(controlpaneldoc.getItemValueString("RegistrationEmailSubject"));
 		this.setRegistrationEmailBody(controlpaneldoc.getItemValueString("RegistrationEmailBody"));
 		this.setRegistrationEmailFromEmail(controlpaneldoc.getItemValueString("RegistrationEmailFromEmail"));
 		this.setRegistrationEmailFromName(controlpaneldoc.getItemValueString("RegistrationEmailFromName"));
 		this.setForgottenEmailSubject(controlpaneldoc.getItemValueString("ForgottenEmailSubject"));
 		this.setForgottenEmailBody(controlpaneldoc.getItemValueString("ForgottenEmailBody"));
 		this.setForgottenEmailFromEmail(controlpaneldoc.getItemValueString("ForgottenEmailFromEmail"));
 		this.setForgottenEmailFromName(controlpaneldoc.getItemValueString("ForgottenEmailFromname"));
 		this.setSessionEmailFromEmail(controlpaneldoc.getItemValueString("SessionEmailFromEmail"));
 		this.setSessionEmailFromName(controlpaneldoc.getItemValueString("SessionEmailFromName"));
 		this.setSessionEmailAcceptedSubject(controlpaneldoc.getItemValueString("SessionEmailAcceptedSubject"));
 		this.setSessionEmailAcceptedBody(controlpaneldoc.getItemValueString("SessionEmailAcceptedBody"));
 		this.setSessionEmailRejectedSubject(controlpaneldoc.getItemValueString("SessionEmailRejectedSubject"));
 		this.setSessionEmailRejectedBody(controlpaneldoc.getItemValueString("SessionEmailRejectedBody"));
 		this.setSessionVoting(controlpaneldoc.getItemValueString("SessionVoting"));
 		controlpaneldoc.recycle();
 		controlpanels.recycle();
 	}
 	
 	private Database getCurrentDatabase() {
 		NotesContext nc = NotesContext.getCurrentUnchecked();
 		return (null != nc) ? nc.getCurrentDatabase() : null;
 	}
 
 	public String getEnableRegistration() {
 		return enableRegistration;
 	}
 
 	public void setEnableRegistration(String enableRegistration) {
 		this.enableRegistration = enableRegistration;
 	}
 
 	public String getEnableAgenda() {
 		return enableAgenda;
 	}
 
 	public void setEnableAgenda(String enableAgenda) {
 		this.enableAgenda = enableAgenda;
 	}
 
 	public String getEnableForum() {
 		return enableForum;
 	}
 
 	public void setEnableForum(String enableForum) {
 		this.enableForum = enableForum;
 	}
 
 	public String getFeedbackSendTo() {
 		return feedbackSendTo;
 	}
 
 	public void setFeedbackSendTo(String feedbackSendTo) {
 		this.feedbackSendTo = feedbackSendTo;
 	}
 
 	public String getEventName() {
 		return eventName;
 	}
 
 	public void setEventName(String eventName) {
 		this.eventName = eventName;
 	}
 
 	public Date getStartDate() {
 		return startDate;
 	}
 
 	public void setStartDate(Date startDate) {
 		this.startDate = startDate;
 	}
 
 	public int getAgendaDays() {
 		return agendaDays;
 	}
 
 	public void setAgendaDays(int agendaDays) {
 		this.agendaDays = agendaDays;
 	}
 
 	public int getAgendaRooms() {
 		return agendaRooms;
 	}
 
 	public void setAgendaRooms(int agendaRooms) {
 		this.agendaRooms = agendaRooms;
 	}
 
 	public String[] getTracks() {
 		return tracks;
 	}
 
 	public void setTracks(String[] tracks) {
 		this.tracks = tracks;
 	}
 
 	public String getRegistrationNAB() {
 		return registrationNAB;
 	}
 
 	public void setRegistrationNAB(String registrationNAB) {
 		this.registrationNAB = registrationNAB;
 	}
 
 	public String getRegistrationUsersGroup() {
 		return registrationUsersGroup;
 	}
 
 	public void setRegistrationUsersGroup(String registrationUsersGroup) {
 		this.registrationUsersGroup = registrationUsersGroup;
 	}
 
 	public void setUnid(String unid) {
 		this.unid = unid;
 	}
 
 	public String getUnid() {
 		return unid;
 	}
 	
 	public String getRegistrationEmailSubject() {
 		return registrationEmailSubject;
 	}
 
 	public void setRegistrationEmailSubject(String registrationEmailSubject) {
 		this.registrationEmailSubject = registrationEmailSubject;
 	}
 
 	public String getRegistrationEmailBody() {
 		return registrationEmailBody;
 	}
 
 	public void setRegistrationEmailBody(String registrationEmailBody) {
 		this.registrationEmailBody = registrationEmailBody;
 	}
 
 	public String getRegistrationEmailFromEmail() {
 		return registrationEmailFromEmail;
 	}
 
 	public void setRegistrationEmailFromEmail(String registrationEmailFromEmail) {
 		this.registrationEmailFromEmail = registrationEmailFromEmail;
 	}
 
 	public String getRegistrationEmailFromName() {
 		return registrationEmailFromName;
 	}
 
 	public void setRegistrationEmailFromName(String registrationEmailFromName) {
 		this.registrationEmailFromName = registrationEmailFromName;
 	}
 
 	public String getForgottenEmailSubject() {
 		return forgottenEmailSubject;
 	}
 
 	public void setForgottenEmailSubject(String forgottenEmailSubject) {
 		this.forgottenEmailSubject = forgottenEmailSubject;
 	}
 
 	public String getForgottenEmailBody() {
 		return forgottenEmailBody;
 	}
 
 	public void setForgottenEmailBody(String forgottenEmailBody) {
 		this.forgottenEmailBody = forgottenEmailBody;
 	}
 
 	public String getForgottenEmailFromEmail() {
 		return forgottenEmailFromEmail;
 	}
 
 	public void setForgottenEmailFromEmail(String forgottenEmailFromEmail) {
 		this.forgottenEmailFromEmail = forgottenEmailFromEmail;
 	}
 
 	public String getForgottenEmailFromName() {
 		return forgottenEmailFromName;
 	}
 
 	public void setForgottenEmailFromName(String forgottenEmailFromName) {
 		this.forgottenEmailFromName = forgottenEmailFromName;
 	}
 
 	public String getSessionEmailFromEmail() {
 		return sessionEmailFromEmail;
 	}
 
 	public void setSessionEmailFromEmail(String sessionEmailFromEmail) {
 		this.sessionEmailFromEmail = sessionEmailFromEmail;
 	}
 
 	public String getSessionEmailFromName() {
 		return sessionEmailFromName;
 	}
 
 	public void setSessionEmailFromName(String sessionEmailFromName) {
 		this.sessionEmailFromName = sessionEmailFromName;
 	}
 
 	public String getSessionEmailAcceptedSubject() {
 		return sessionEmailAcceptedSubject;
 	}
 
 	public void setSessionEmailAcceptedSubject(String sessionEmailAcceptedSubject) {
 		this.sessionEmailAcceptedSubject = sessionEmailAcceptedSubject;
 	}
 
 	public String getSessionEmailAcceptedBody() {
 		return sessionEmailAcceptedBody;
 	}
 
 	public void setSessionEmailAcceptedBody(String sessionEmailAcceptedBody) {
 		this.sessionEmailAcceptedBody = sessionEmailAcceptedBody;
 	}
 
 	public String getSessionEmailRejectedSubject() {
 		return sessionEmailRejectedSubject;
 	}
 
 	public void setSessionEmailRejectedSubject(String sessionEmailRejectedSubject) {
 		this.sessionEmailRejectedSubject = sessionEmailRejectedSubject;
 	}
 
 	public String getSessionEmailRejectedBody() {
 		return sessionEmailRejectedBody;
 	}
 
 	public void setSessionEmailRejectedBody(String sessionEmailRejectedBody) {
 		this.sessionEmailRejectedBody = sessionEmailRejectedBody;
 	}
 
 	public void setSessionVoting(String sessionVoting) {
 		this.sessionVoting = sessionVoting;
 	}
 
 	public String getSessionVoting() {
 		return sessionVoting;
 	}
 }
