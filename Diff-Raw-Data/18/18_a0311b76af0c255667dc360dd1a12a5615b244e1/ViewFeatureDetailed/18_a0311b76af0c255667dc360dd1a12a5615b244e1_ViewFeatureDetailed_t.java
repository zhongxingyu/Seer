 package eu.ist.fears.common.views;
 
 import java.io.Serializable;
 import java.util.List;
 
 import eu.ist.fears.common.State;
 
 public class ViewFeatureDetailed extends ViewFeatureResume implements Serializable {
 
     private static final long serialVersionUID = -9130167518653434831L;
     private List<ViewVoterResume> voters;
     private List<ViewComment> comments;
     private boolean isProjectAdmin;
 
     public ViewFeatureDetailed() {
 
     }
 
     public ViewFeatureDetailed(String projectName, int projectID, String name, int featureID, State state, boolean userHasvoted,
 	    String description, String authorNick, String authorOID, String date, boolean isProjectAdmin,
 	    List<ViewVoterResume> voters, List<ViewComment> comments) {
 	super(projectName, projectID, name, featureID, state, userHasvoted, description, voters.size(), comments.size(),
		authorNick, authorOID, date,false);
 	this.comments = comments;
 	this.isProjectAdmin = isProjectAdmin;
     }
 
     public List<ViewComment> getComments() {
 	return comments;
     }
 
     public List<ViewVoterResume> getVoters() {
 	return voters;
     }
 
     public boolean isProjectAdmin() {
 	return isProjectAdmin;
     }
 
 }
