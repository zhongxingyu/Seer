 package nl.bhit.mtor.server.webapp.action;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import nl.bhit.mtor.Constants;
 import nl.bhit.mtor.dao.SearchException;
 import nl.bhit.mtor.model.MTorMessage;
 import nl.bhit.mtor.model.Project;
 import nl.bhit.mtor.model.Status;
 import nl.bhit.mtor.model.User;
 import nl.bhit.mtor.server.webapp.util.UserManagementUtils;
 import nl.bhit.mtor.service.GenericManager;
 import nl.bhit.mtor.service.MessageManager;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.opensymphony.xwork2.Preparable;
 
 @Component
 public class MessageAction extends BaseAction implements Preparable {
 	@Autowired
 	private MessageManager messageManager;
 	@Autowired
 	private GenericManager<Project, Long> projectManager;
 	private List mTorMessages;
 	private List projects;
 	private List status;
 	private MTorMessage message;
 	private Long id;
 	private String query;
 
 	public void setMessageManager(MessageManager messageManager) {
 		this.messageManager = messageManager;
 	}
 
 	public void setProjectManager(GenericManager<Project, Long> projectManager) {
 		this.projectManager = projectManager;
 	}
 
 	public List getMTorMessages() {
 		return mTorMessages;
 	}
 
 	/**
 	 * Grab the entity from the database before populating with request parameters
 	 */
 	@Override
 	public void prepare() {
 		if (getRequest().getMethod().equalsIgnoreCase("post")) {
 			// prevent failures on new
 			String messageId = getRequest().getParameter("message.id");
 			if (messageId != null && !messageId.equals("")) {
 				message = messageManager.get(new Long(messageId));
 			}
 		}
 	}
 
 	public void setQ(String q) {
 		this.query = q;
 	}
 
 	public String list() {
 		try {
 			if (!getRequest().isUserInRole(Constants.ADMIN_ROLE)) {
 				// messages = messageManager.search(query, Message.class);
 				mTorMessages = new ArrayList();
 				List<MTorMessage> tempMessages = messageManager.search(query, MTorMessage.class);
 				List<Project> tempProjects = getProjectCompanyList();
 				for (MTorMessage tempMessage : tempMessages) {
 					String messageProjectName = tempMessage.getProject().getName();
 					for (Project tempProject : tempProjects) {
 						if (tempProject.getName().equalsIgnoreCase(messageProjectName)) {
 							mTorMessages.add(tempMessage);
 						}
 					}
 				}
 				Collection messagesNew = new LinkedHashSet(mTorMessages);
 				mTorMessages = new ArrayList(messagesNew);
 			} else {
 				mTorMessages = messageManager.getAllDistinct();
 			}
 		} catch (SearchException se) {
 			addActionError(se.getMessage());
 			mTorMessages = messageManager.getAllDistinct();
 		}
 		return SUCCESS;
 	}
 
 	public List getProjectCompanyList() {
 		if (!getRequest().isUserInRole(Constants.ADMIN_ROLE)) {
 			List<Project> tempProjects = projectManager.getAllDistinct();
 			String loggedInUser = UserManagementUtils.getAuthenticatedUser().getFullName();
 			projects = new ArrayList();
 			for (Project tempProject : tempProjects) {
 				Set<User> projectUsers = tempProject.getUsers();
 				for (User projectUser : projectUsers) {
 					if (projectUser.getFullName().equalsIgnoreCase(loggedInUser)) {
 						projects.add(tempProject);
 					}
 				}
 			}
 		} else {
 			projects = projectManager.getAllDistinct();
 		}
 		return projects;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public MTorMessage getMessage() {
 		return message;
 	}
 
 	public void setMessage(MTorMessage message) {
 		this.message = message;
 	}
 
 	public String delete() {
 		messageManager.remove(message.getId());
 		saveMessage(getText("message.deleted"));
 
 		return SUCCESS;
 	}
 
 	public String edit() {
 		if (id != null) {
 			message = messageManager.get(id);
 		} else {
 			message = new MTorMessage();
 		}
 
 		return SUCCESS;
 	}
 
 	public String save() throws Exception {
 		if (cancel != null) {
 			return "cancel";
 		}
 
 		if (delete != null) {
 			return delete();
 		}
 
 		boolean isNew = (message.getId() == null);
 
 		messageManager.save(message);
 
 		String key = (isNew) ? "message.added" : "message.updated";
 		saveMessage(getText(key));
 
 		if (!isNew) {
 			return INPUT;
 		}
 		return SUCCESS;
 	}
 
 	public String resolve() {
 		List<MTorMessage> mTorMessagesList = new ArrayList();
 		mTorMessagesList = messageManager.getMessagesWithTimestamp(message);
 
 		for (MTorMessage tempMessage : mTorMessagesList) {
 			tempMessage.setResolved(true);
 			messageManager.save(tempMessage);
 		}
 		return SUCCESS;
 	}
 }
