 package ch.ethz.inf.dbproject.logic;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 
 import ch.ethz.inf.dbproject.model.Comment;
 import ch.ethz.inf.dbproject.model.DatastoreInterface;
 import ch.ethz.inf.dbproject.model.FundingAmount;
 import ch.ethz.inf.dbproject.model.Project;
 import ch.ethz.inf.utils.StringUtils;
 
 /**
  * This is a hack: Note that a persistent scope such as ViewScope/SessionScoped is required in order
  * to invoke the commandLink inside the datatable. The proper way would be to construct a
  * ProjectModel which contains all the domain objects. But since this is only a school project we
  * will stick to this solution...
  */
 @ManagedBean
 @ViewScoped
 public class ProjectController implements Serializable {
 
 	private static final long serialVersionUID = -6272753030255122907L;
 
 	private String comment;
 	private int projectId;
 	private Project selectedProject;
 	private List<FundingAmount> fundingAmounts;
 
 	@ManagedProperty(value = "#{sessionData}")
 	private SessionData sessionData;
 
 	@PostConstruct
 	public void init() {
 		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
 		String id = context.getRequestParameterMap().get("id");
		if (StringUtils.isNotNullOrEmpty(id)) {
 			projectId = Integer.valueOf(id);
 		}
 
 		DatastoreInterface dbInterface = new DatastoreInterface();
 
 		selectedProject = dbInterface.getProjectById(getProjectId());
 		fundingAmounts = dbInterface.getAmountsOfProject(projectId);
 	}
 
 	public Project getSelectedProject() {
 		return selectedProject;
 	}
 
 	public List<FundingAmount> getFundingAmounts() {
 		return fundingAmounts;
 	}
 
 	public String support() {
 		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
 		String id = context.getRequestParameterMap().get("fundingAmountId");
		if (StringUtils.isNotNullOrEmpty(id)) {
 			int fundingAmountId = Integer.valueOf(id);
 			DatastoreInterface dbInterface = new DatastoreInterface();
 			dbInterface.fundProject(sessionData.getUser().getId(), fundingAmountId);
 		}
 		return "User.jsf?faces-redirect=true";
 	}
 
 	public String addComment() {
 
 		// TODO implement this
 		// String username = UserManagement.getCurrentlyLoggedInUser(session).getUsername();
 		// Comment commentObj = new Comment(username, comment);
 
 		// TODO implement this
 		// this.dbInterface.addCommentForProject(id, commentObj);
 
 		return "Project.jsf?id=" + getProjectId();
 	}
 
 	public List<Comment> getComments() {
 		// TODO implement this
 		// List<Comment> comments = this.dbInterface.getCommentsOfProject(id);
 		// Create a table to display the comments the same way as above
 		// session.setAttribute("commentTable", table);
 
 		return new ArrayList<Comment>();
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	public int getProjectId() {
 		return projectId;
 	}
 
 	public void setProjectId(int projectId) {
 		this.projectId = projectId;
 	}
 
 	public SessionData getSessionData() {
 		return sessionData;
 	}
 
 	public void setSessionData(SessionData sessionData) {
 		this.sessionData = sessionData;
 	}
 }
