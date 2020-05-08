 package eu.ist.fears.common.views;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 import eu.ist.fears.common.State;
 
 public class ViewFeatureResume implements IsSerializable {
 
     private static final long serialVersionUID = -4452593206230245849L;
     protected String projectName;
     protected int projectID;
     protected String name;
     protected int featureID;
     protected State state;
     protected boolean userHasVoted;
     protected String _description;
     protected int votes;
     protected int nComments;
     protected String authorNick;
     protected String authorOID;
     protected String date;
 
     public ViewFeatureResume() {
 
     }
    
     public ViewFeatureResume(String projectName, int projectID, String name, int featureID, State state, boolean userHasvoted,
 	    String description, int votes, int nComments, String authorNick, String authorOID, String date) {
	this(projectName, projectID, name, featureID, state, userHasvoted, description, votes, nComments, authorNick, authorOID, date, true);
    }

    public ViewFeatureResume(String projectName, int projectID, String name, int featureID, State state, boolean userHasvoted,
	    String description, int votes, int nComments, String authorNick, String authorOID, String date, boolean shorten) {
 	this.projectName = projectName;
 	this.projectID = projectID;
 	this.name = name;
 	this.featureID = featureID;
 	this.state = state;
 	this.userHasVoted = userHasvoted;
 	int length = description.length();
	if (length > 194 && shorten) {
 	    _description = description.substring(0, 194) + " (...)";
 	} else
 	    _description = description;
 	// Remove new lines
 	_description = _description.replaceAll("<br>", " ");
 	this.votes = votes;
 	this.nComments = nComments;
 	this.authorNick = authorNick;
 	this.date = date;
 	this.authorOID = authorOID;
     }
 
     public String getProjectName() {
 	return projectName;
 
     }
 
     public int getProjectID() {
 	return projectID;
 
     }
 
     public String getName() {
 	return name;
     }
 
     public int getFeatureID() {
 	return featureID;
     }
 
     public String getDescription() {
 	return _description;
     }
 
     public int getVotes() {
 	return votes;
     }
 
     public int getNComments() {
 	return nComments;
     }
 
     public boolean userHasVoted() {
 	return userHasVoted;
     }
 
     public State getState() {
 	return state;
 
     }
 
     public String getCreatedDate() {
 	return date;
     }
 
     public String getAuthorNick() {
 	return authorNick;
     }
 
     public String getAuthorOID() {
 	return authorOID;
     }
 
 }
