 
 package server;
 
 import java.sql.Timestamp;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import org.primefaces.event.TabChangeEvent;
 
 import data.DeadlineNotification;
 import data.Editable;
 import data.ExRules;
 import data.Field;
 import data.ModManual;
 import data.ModificationNotification;
 import data.Module;
 import data.Notification;
 import data.Subject;
 import db.DBField;
 import db.DBNotification;
 import db.DBSubject;
 
 import java.util.List;
 
 import javax.faces.event.ActionEvent;
 import javax.servlet.http.HttpSession;
 
 import org.primefaces.event.SelectEvent;
 
 
 @ManagedBean(name = "RedDezNotificationBean")
 @SessionScoped
 public class RedDezNotificationBean {
 
 	public static final String TO_FOR = "message-log-edit-dez";
 
 	private String recipientEmail;
 	private String senderEmail;
 	private Timestamp timeStamp;
 	private String message;
 	private String action;
 	private String status;
 	private List<Notification> notificationList;
 	private Notification selectedNotification;
 	Editable selectedEditableAfter, selectedEditableBefore;
 	private String strTimeStamp;
 
 	// variables for editable before & after
 	private String title, description, ects, aim;
 	private boolean mainVisible, ectsAimVisible, addInfoVisible;
 	private String title2, description2, ects2, aim2;
 	private boolean mainVisible2, ectsAimVisible2, addInfoVisible2;
 	List<Field> fieldList, fieldList2;
 
 	// variables for DeadlineNotification
 	private String deadlineModMan;
 	private Timestamp deadline;
 	private boolean isDeadline;
 
 	private String currentUser;
 
 	public RedDezNotificationBean() {
 		// get User Session
 		HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
 				.getExternalContext().getSession(false);
 		currentUser = (String) session.getAttribute("email");
 		loadNotifications();
 	}
 
 	/**
 	 * Clicking on the tablerow sets isReadRecipient to true
 	 */
 	public void selectedNotificationIsReadRecipient(SelectEvent e) {
 		DBNotification.updateNotificationIsReadRecipient(getSelectedNotification());
 	}
 
 	/**
 	 * Clicking on the tablerow sets isReadRecipient to true
 	 */
 	public void selectedNotificationIsReadSender(SelectEvent e) {
 		DBNotification.updateNotificationIsReadSender(getSelectedNotification());
 	}
 	/**
 	 * Deletes a specific notification from DB
 	 */
 	public void cancelSelectedNotification(ActionEvent e) {
 		if (selectedNotification != null
 				&& selectedNotification instanceof ModificationNotification) {
 			DBNotification.deleteNotification(getSelectedNotification());
 			Subject sub = (Subject) selectedEditableAfter;
 			if (!sub.isAck()) {
 				DBSubject.deleteSubject(sub);
 				DBField.deleteFields(sub.getVersion(), sub.getSubTitle(),
 						sub.getModTitle());
 				if (selectedNotification.getStatus().equals("declined")) {
 					addMessage(TO_FOR,
 							"Benachrichtigung erfolgreich entfernt: ",
 							"Die Benachrichtigung und das entsprechende Fach wurden erfolgreich gelöscht.");
 				} else {
 					addMessage(TO_FOR, "Änderung erfolgreich widerrufen: ",
 							"Das dazu gehörige Fach wurde gelöscht.");
 				}
 			} else {
 				addMessage(TO_FOR, "Benachrichtigung erfolgreich entfernt: ",
 						"Die Benachrichtigung wurde erfolgreich aus ihrer Liste entfernt.");
 			}
 			loadNotifications();
 		} else if (selectedNotification != null
 				&& selectedNotification instanceof DeadlineNotification) {
 			DBNotification
 					.deleteDeadlineNotification(getSelectedNotification());
 			addMessage(TO_FOR, "Benachrichtigung erfolgreich entfernt: ",
 					"Die Benachrichtigung wurde erfolgreich aus ihrer Liste entfernt.");
 			loadNotifications();
 		}
 	}
 
 	/**
 	 * Creates the dialog of unread notifications and shows them by changing a
 	 * tab
 	 */
 	public void onTabChange(TabChangeEvent event) {
 		loadNotifications();
 		boolean glbIsNotRead = false;
 		String glbSender = new String();
 		int cntr = 0;
 		for (int i = 0; i < getNotificationList().size(); i++) {
			if (!getNotificationList().get(i).isIsReadSender()) {
 				glbIsNotRead = true;
 				cntr += 1;
 				glbSender += cntr + ". ("
 						+ getNotificationList().get(i).getTimeStamp()
						+ ") &nbsp";
 			}
 		}
 		if (glbIsNotRead) {
 			if (cntr == 1) {
 				FacesMessage msg = new FacesMessage(
 						"Sie haben eine ungelesene Nachricht ("
 								+ getNotificationList().get(0).getTimeStamp()
 								+ ")");
 				FacesContext.getCurrentInstance().addMessage(null, msg);
			} else {
 				FacesMessage msg = new FacesMessage(
 						"Sie haben "+ cntr +" ungelesene Nachrichten "
 								+ glbSender
 								+ ")");
 				FacesContext.getCurrentInstance().addMessage(null, msg);
 			}
 		}
 	}
 	/**
 	 * Loads all Notifications from the database
 	 * 
 	 */
 	public void loadNotifications() {
 		setNotificationList(DBNotification
 				.loadModificationNotificationModEx(currentUser));
 	}
 
 	/**
 	 * if anything goes wrong - display an Error
 	 * 
 	 * @param title
 	 * @param msg
 	 */
 	public void addErrorMessage(String toFor, String title, String msg) {
 		FacesContext.getCurrentInstance().addMessage(toFor,
 				new FacesMessage(FacesMessage.SEVERITY_FATAL, title, msg));
 	}
 
 	/**
 	 * Informs the User via message.
 	 * 
 	 * @param title
 	 * @param msg
 	 */
 	public void addMessage(String toFor, String title, String msg) {
 		FacesContext.getCurrentInstance().addMessage(toFor,
 				new FacesMessage(FacesMessage.SEVERITY_INFO, title, msg));
 	}
 
 	/**
 	 * @return the recipientEmail
 	 */
 	public String getRecipientEmail() {
 		return recipientEmail;
 	}
 
 	/**
 	 * @param recipientEmail
 	 *            the recipientEmail to set
 	 */
 	public void setRecipientEmail(String recipientEmail) {
 		this.recipientEmail = recipientEmail;
 	}
 
 	/**
 	 * @return the senderEmail
 	 */
 	public String getSenderEmail() {
 		return senderEmail;
 	}
 
 	/**
 	 * @param senderEmail
 	 *            the senderEmail to set
 	 */
 	public void setSenderEmail(String senderEmail) {
 		this.senderEmail = senderEmail;
 	}
 
 	/**
 	 * @return the timeStamp
 	 */
 	public Timestamp getTimeStamp() {
 		return timeStamp;
 	}
 
 	/**
 	 * @param timeStamp
 	 *            the timeStamp to set
 	 */
 	public void setTimeStamp(Timestamp timeStamp) {
 		this.timeStamp = timeStamp;
 	}
 
 	/**
 	 * @return the message
 	 */
 	public String getMessage() {
 		return message;
 	}
 
 	/**
 	 * @param message
 	 *            the message to set
 	 */
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	/**
 	 * @return the action
 	 */
 	public String getAction() {
 		return action;
 	}
 
 	/**
 	 * @param action
 	 *            the action to set
 	 */
 	public void setAction(String action) {
 		this.action = action;
 	}
 
 	/**
 	 * @return the status
 	 */
 	public String getStatus() {
 		return status;
 	}
 
 	/**
 	 * @param status
 	 *            the status to set
 	 */
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
 	/**
 	 * @return the notificationList
 	 */
 	public List<Notification> getNotificationList() {
 		return notificationList;
 	}
 
 	/**
 	 * @param notificationList
 	 *            the notificationList to set
 	 */
 	public void setNotificationList(List<Notification> notificationList) {
 		this.notificationList = notificationList;
 	}
 
 	/**
 	 * @return the selectedNotification
 	 */
 	public Notification getSelectedNotification() {
 		return selectedNotification;
 	}
 
 	/**
 	 * @param selectedNotification
 	 *            the selectedNotification to set
 	 */
 	public void setSelectedNotification(Notification selectedNotification) {
 		this.selectedNotification = selectedNotification;
 		if (selectedNotification != null) {
 			if (selectedNotification instanceof ModificationNotification) {
 
 				// set boolean for visibillity
 				isDeadline = false;
 
 				ModificationNotification selectedModNotification = (ModificationNotification) selectedNotification;
 				selectedEditableAfter = selectedModNotification
 						.getModification().getAfter();
 				selectedEditableBefore = selectedModNotification
 						.getModification().getBefore();
 				if (selectedEditableAfter instanceof Subject
 						&& selectedEditableBefore instanceof Subject) {
 					// After
 					Subject sub = (Subject) selectedEditableAfter;
 					fieldList = DBField.loadFieldList(sub.getModTitle(),
 							sub.getVersion(), sub.getSubTitle());
 					title = sub.getSubTitle();
 					description = sub.getDescription();
 					ects = String.valueOf(sub.getEcts());
 					aim = sub.getAim();
 					mainVisible = true;
 					ectsAimVisible = true;
 					addInfoVisible = true;
 					System.out.println(description);
 					// Before
 					Subject sub2 = (Subject) selectedEditableBefore;
 					fieldList2 = DBField.loadFieldList(sub2.getModTitle(),
 							sub2.getVersion(), sub2.getSubTitle());
 					title2 = sub2.getSubTitle();
 					description2 = sub2.getDescription();
 					ects2 = String.valueOf(sub2.getEcts());
 					aim2 = sub2.getAim();
 					mainVisible2 = true;
 					ectsAimVisible2 = !sub2.getSubTitle().equals("Vorgängerversion nicht verfügbar.");
 					addInfoVisible2 = !sub2.getSubTitle().equals("Vorgängerversion nicht verfügbar.");
 				} else if (selectedEditableAfter instanceof Module
 						&& selectedEditableBefore instanceof Module) {
 					// After
 					Module mod = (Module) selectedEditableAfter;
 					title = mod.getModTitle();
 					description = mod.getDescription();
 					mainVisible = true;
 					ectsAimVisible = false;
 					addInfoVisible = false;
 					// Before
 					Module mod2 = (Module) selectedEditableBefore;
 					title2 = mod2.getModTitle();
 					description2 = mod2.getDescription();
 					mainVisible2 = true;
 					ectsAimVisible2 = false;
 					addInfoVisible2 = false;
 				} else if (selectedEditableAfter instanceof ModManual
 						&& selectedEditableBefore instanceof ModManual) {
 					// After
 					ModManual modMan = (ModManual) selectedEditableAfter;
 					title = modMan.getModManTitle();
 					description = modMan.getDescription();
 					mainVisible = true;
 					ectsAimVisible = false;
 					addInfoVisible = false;
 					// Before
 					ModManual modMan2 = (ModManual) selectedEditableBefore;
 					title2 = modMan2.getModManTitle();
 					description2 = modMan2.getDescription();
 					mainVisible2 = true;
 					ectsAimVisible2 = false;
 					addInfoVisible2 = false;
 				} else if (selectedEditableAfter instanceof ExRules
 						&& selectedEditableBefore instanceof ExRules) {
 					// After
 					ExRules rule = (ExRules) selectedEditableAfter;
 					title = rule.getExRulesTitle();
 					description = "";
 					mainVisible = true;
 					ectsAimVisible = false;
 					addInfoVisible = false;
 					// Before
 					ExRules rule2 = (ExRules) selectedEditableBefore;
 					title2 = rule2.getExRulesTitle();
 					description2 = "";
 					mainVisible2 = true;
 					ectsAimVisible2 = false;
 					addInfoVisible2 = false;
 				}
 			} else if (selectedNotification instanceof DeadlineNotification) {
 
 				// set boolean for visibillity
 				isDeadline = true;
 
 				DeadlineNotification selectedDeadNotification = (DeadlineNotification) selectedNotification;
 				deadline = selectedDeadNotification.getDeadline();
 				deadlineModMan = selectedDeadNotification.getModManTitle();
 			}
 		}
 	}
 
 	/**
 	 * @return the strTimeStamp
 	 */
 	public String getStrTimeStamp() {
 		return strTimeStamp;
 	}
 
 	/**
 	 * @param strTimeStamp
 	 *            the strTimeStamp to set
 	 */
 	public void setStrTimeStamp(String strTimeStamp) {
 		this.strTimeStamp = strTimeStamp;
 	}
 
 	/**
 	 * @return the selectedEditableAfter
 	 */
 	public Editable getSelectedEditableAfter() {
 		return selectedEditableAfter;
 	}
 
 	/**
 	 * @param selectedEditableAfter
 	 *            the selectedEditableAfter to set
 	 */
 	public void setSelectedEditableAfter(Editable selectedEditableAfter) {
 		this.selectedEditableAfter = selectedEditableAfter;
 	}
 
 	/**
 	 * @return the selectedEditableBefore
 	 */
 	public Editable getSelectedEditableBefore() {
 		return selectedEditableBefore;
 	}
 
 	/**
 	 * @param selectedEditableBefore
 	 *            the selectedEditableBefore to set
 	 */
 	public void setSelectedEditableBefore(Editable selectedEditableBefore) {
 		this.selectedEditableBefore = selectedEditableBefore;
 	}
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * @param title
 	 *            the title to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param description
 	 *            the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * @return the ects
 	 */
 	public String getEcts() {
 		return ects;
 	}
 
 	/**
 	 * @param ects
 	 *            the ects to set
 	 */
 	public void setEcts(String ects) {
 		this.ects = ects;
 	}
 
 	/**
 	 * @return the aim
 	 */
 	public String getAim() {
 		return aim;
 	}
 
 	/**
 	 * @param aim
 	 *            the aim to set
 	 */
 	public void setAim(String aim) {
 		this.aim = aim;
 	}
 
 	/**
 	 * @return the mainVisible
 	 */
 	public boolean isMainVisible() {
 		return mainVisible;
 	}
 
 	/**
 	 * @param mainVisible
 	 *            the mainVisible to set
 	 */
 	public void setMainVisible(boolean mainVisible) {
 		this.mainVisible = mainVisible;
 	}
 
 	/**
 	 * @return the ectsAimVisible
 	 */
 	public boolean isEctsAimVisible() {
 		return ectsAimVisible;
 	}
 
 	/**
 	 * @param ectsAimVisible
 	 *            the ectsAimVisible to set
 	 */
 	public void setEctsAimVisible(boolean ectsAimVisible) {
 		this.ectsAimVisible = ectsAimVisible;
 	}
 
 	/**
 	 * @return the addInfoVisible
 	 */
 	public boolean isAddInfoVisible() {
 		return addInfoVisible;
 	}
 
 	/**
 	 * @param addInfoVisible
 	 *            the addInfoVisible to set
 	 */
 	public void setAddInfoVisible(boolean addInfoVisible) {
 		this.addInfoVisible = addInfoVisible;
 	}
 
 	/**
 	 * @return the title2
 	 */
 	public String getTitle2() {
 		return title2;
 	}
 
 	/**
 	 * @param title2
 	 *            the title2 to set
 	 */
 	public void setTitle2(String title2) {
 		this.title2 = title2;
 	}
 
 	/**
 	 * @return the description2
 	 */
 	public String getDescription2() {
 		return description2;
 	}
 
 	/**
 	 * @param description2
 	 *            the description2 to set
 	 */
 	public void setDescription2(String description2) {
 		this.description2 = description2;
 	}
 
 	/**
 	 * @return the ects2
 	 */
 	public String getEcts2() {
 		return ects2;
 	}
 
 	/**
 	 * @param ects2
 	 *            the ects2 to set
 	 */
 	public void setEcts2(String ects2) {
 		this.ects2 = ects2;
 	}
 
 	/**
 	 * @return the aim2
 	 */
 	public String getAim2() {
 		return aim2;
 	}
 
 	/**
 	 * @param aim2
 	 *            the aim2 to set
 	 */
 	public void setAim2(String aim2) {
 		this.aim2 = aim2;
 	}
 
 	/**
 	 * @return the mainVisible2
 	 */
 	public boolean isMainVisible2() {
 		return mainVisible2;
 	}
 
 	/**
 	 * @param mainVisible2
 	 *            the mainVisible2 to set
 	 */
 	public void setMainVisible2(boolean mainVisible2) {
 		this.mainVisible2 = mainVisible2;
 	}
 
 	/**
 	 * @return the ectsAimVisible2
 	 */
 	public boolean isEctsAimVisible2() {
 		return ectsAimVisible2;
 	}
 
 	/**
 	 * @param ectsAimVisible2
 	 *            the ectsAimVisible2 to set
 	 */
 	public void setEctsAimVisible2(boolean ectsAimVisible2) {
 		this.ectsAimVisible2 = ectsAimVisible2;
 	}
 
 	/**
 	 * @return the addInfoVisible2
 	 */
 	public boolean isAddInfoVisible2() {
 		return addInfoVisible2;
 	}
 
 	/**
 	 * @param addInfoVisible2
 	 *            the addInfoVisible2 to set
 	 */
 	public void setAddInfoVisible2(boolean addInfoVisible2) {
 		this.addInfoVisible2 = addInfoVisible2;
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
 	 * @return the fieldList2
 	 */
 	public List<Field> getFieldList2() {
 		return fieldList2;
 	}
 
 	/**
 	 * @param fieldList2
 	 *            the fieldList2 to set
 	 */
 	public void setFieldList2(List<Field> fieldList2) {
 		this.fieldList2 = fieldList2;
 	}
 
 	/**
 	 * @return the deadlineModMan
 	 */
 	public String getDeadlineModMan() {
 		return deadlineModMan;
 	}
 
 	/**
 	 * @param deadlineModMan
 	 *            the deadlineModMan to set
 	 */
 	public void setDeadlineModMan(String deadlineModMan) {
 		this.deadlineModMan = deadlineModMan;
 	}
 
 	/**
 	 * @return the deadline
 	 */
 	public Timestamp getDeadline() {
 		return deadline;
 	}
 
 	/**
 	 * @param deadline
 	 *            the deadline to set
 	 */
 	public void setDeadline(Timestamp deadline) {
 		this.deadline = deadline;
 	}
 
 	/**
 	 * @return the isDeadline
 	 */
 	public boolean isIsDeadline() {
 		return isDeadline;
 	}
 
 	/**
 	 * @param isDeadline
 	 *            the isDeadline to set
 	 */
 	public void setIsDeadline(boolean isDeadline) {
 		this.isDeadline = isDeadline;
 	}
 
 }
