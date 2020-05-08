 package nl.bhit.mtor.server.webapp.action;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import nl.bhit.mtor.Constants;
 import nl.bhit.mtor.dao.SearchException;
 import nl.bhit.mtor.model.MTorMessage;
 import nl.bhit.mtor.model.Project;
 import nl.bhit.mtor.model.User;
 import nl.bhit.mtor.server.webapp.util.UserManagementUtils;
 import nl.bhit.mtor.service.MessageManager;
 import nl.bhit.mtor.service.ProjectManager;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.opensymphony.xwork2.Preparable;
 
 @Component
 public class MessageAction extends BaseAction implements Preparable {
 
     /**
 	 * 
 	 */
     private static final long serialVersionUID = 963285910852618368L;
 
     @Autowired
     private transient MessageManager messageManager;
     @Autowired
     private transient ProjectManager projectManager;
 
     private Long id;
     @SuppressWarnings("unused")
     private String query;
     private MTorMessage message;
     private List<MTorMessage> mTorMessages;
     private List<Project> projects;
 
     public void setMessageManager(MessageManager messageManager) {
         this.messageManager = messageManager;
     }
 
     public void setProjectManager(ProjectManager projectManager) {
         this.projectManager = projectManager;
     }
 
     public List<MTorMessage> getMTorMessages() {
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
                 getMessagesForUser();
             } else {
                 getMessagesForAdmin();
             }
         } catch (SearchException se) {
             addActionError(se.getMessage());
             getMessagesForAdmin();
         }
         return SUCCESS;
     }
 
     private void getMessagesForUser() {
         mTorMessages = messageManager.getAllByUser(UserManagementUtils.getAuthenticatedUser());
     }
 
     private void getMessagesForAdmin() {
         mTorMessages = messageManager.getAllDistinct();
     }
 
     public List<Project> getProjectCompanyList() {
         if (!getRequest().isUserInRole(Constants.ADMIN_ROLE)) {
             List<Project> tempProjects = projectManager.getAllDistinct();
             String loggedInUser = UserManagementUtils.getAuthenticatedUser().getFullName();
             projects = new ArrayList<Project>();
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
 
     public String save() {
         if (cancel != null) {
             return "cancel";
         }
 
         if (delete != null) {
             return delete();
         }
 
         boolean isNew = message.getId() == null;
 
         messageManager.save(message);
 
         String key = isNew ? "message.added" : "message.updated";
         saveMessage(getText(key));
 
         if (!isNew) {
             return INPUT;
         }
         return SUCCESS;
     }
 
     public String resolve() {
         log.trace("start resolving...");
         List<MTorMessage> mTorMessagesList = messageManager.getMessagesWithTimestamp(message);
 
         for (MTorMessage tempMessage : mTorMessagesList) {
             log.trace("resolving issue id: " + tempMessage.getId());
             tempMessage.setResolved(true);
             messageManager.save(tempMessage);
         }
         return SUCCESS;
     }
 }
