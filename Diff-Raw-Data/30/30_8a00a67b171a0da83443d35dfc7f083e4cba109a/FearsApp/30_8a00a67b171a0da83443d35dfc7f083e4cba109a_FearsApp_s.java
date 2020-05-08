 package eu.ist.fears.server.domain;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.joda.time.format.DateTimeFormat;
 
 import pt.ist.fenixframework.pstm.Transaction;
 import eu.ist.fears.client.common.DateFormat;
 import eu.ist.fears.client.common.views.ViewAdmins;
 import eu.ist.fears.client.common.views.ViewFeatureResume;
 import eu.ist.fears.client.common.views.ViewProject;
 
 public class FearsApp extends FearsApp_Base {
 
 
 	public static FearsApp getFears(){
 		return (FearsApp)Transaction.getDomainObject(FearsApp.class.getName(), 1);
 	}
 	
 
 	public Project getProject(String projectID){
 		int projID;
 		try{
 			projID= new Integer(projectID).intValue();
 		}catch(Throwable t){
 			System.out.println("ID:" + projectID);
 			throw new RuntimeException("Nao existe esse projecto: " + projectID);
 		}
 		
 		for(Project p :  getProjectSet()){
 			if(p.getIdInternal()==(projID))
 				return p;
 		}
 
 		return null;
 	}
 
 
 	public void addProject(Project p, User u){
 		addProject(p);
 	}
 
 
	public void deleteProject(String name){
 		for(Project p : getProjectSet()){
			if(p.getName().equals(name))
 				removeProject(p);			
 		}				
 	}		
 
 
 	public User getUser(String name){
 		for(User u : getUserSet()){
 			if(u.getName().equals(name))
 				return u;					
 		}
 		
 		if(name!=null){
 		User temp = new User(name);
 		addUser(temp);
 		return temp;
 		} else return null;
 	
 	}
 
 	public Collection<User> getAdmins(){
 		return super.getAdminSet();
 	}
 	
 	public void addAdmin(User u){
 		super.addAdmin(u);
 	}
 	
 	public void removeAdmin(User u){
 		super.removeAdmin(u);
 	} 
 	
 	public boolean isAdmin(User u){
 		return getAdmins().contains(u);
 	}
 
 	public ViewProject[] getProjects() {
 		ViewProject[] res = new ViewProject[getProjectCount()];
 
 		
 		int i=0;
 		for(Project p : getProjectSet()){
 			res[i]=new ViewProject(p.getName(), p.getIdInternal(),  p.getDescription(), p.getNFeatures(), p.getAuthor().getUsername(), 5);
 			i++;
 		}
 
 		return res;
 	}
 
 	public static List<ViewFeatureResume> getViewFeaturesResumes(Collection<FeatureRequest> features, Voter user){
 		if( features.size()==0)
 			return null;
 
 		ArrayList<ViewFeatureResume> res = new ArrayList<ViewFeatureResume>();
 
 		boolean userHasvoted;
 		
 		for(FeatureRequest f : features ){
 			userHasvoted=false;
 			for(Voter v : f.getVoterSet()){
 				if(v.equals(user))
 					userHasvoted=true;
 			}
 			res.add(new ViewFeatureResume(f.getProject().getName(), f.getProject().getIdInternal(), f.getName(), f.getWebID() , f.getState(), userHasvoted , f.getDescription(),
 					f.getVotes(), f.getNComments(), f.getAuthor().getUser().getName(), f.getCreatedTime().toString(DateTimeFormat.forPattern(DateFormat.DEFAULT_FORMAT)) ));
 		}
 
 		return res;
 	}
 
 	public ViewAdmins getViewAdmins(){
 		List<String >admins = new ArrayList<String>();
 		
 		for(User u : getFears().getAdmins()){
 			admins.add(u.getUsername());
 		}
 		
 		return new ViewAdmins(admins);	
 	}
 
 }
