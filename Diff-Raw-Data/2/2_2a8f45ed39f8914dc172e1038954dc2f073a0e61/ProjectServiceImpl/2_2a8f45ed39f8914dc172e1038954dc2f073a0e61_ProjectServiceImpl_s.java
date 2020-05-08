 package de.unihamburg.zbh.fishoracle.server;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import de.unihamburg.zbh.fishoracle.client.data.FoProject;
 import de.unihamburg.zbh.fishoracle.client.data.FoUser;
 import de.unihamburg.zbh.fishoracle.client.datasource.OperationId;
 import de.unihamburg.zbh.fishoracle.client.exceptions.UserException;
 import de.unihamburg.zbh.fishoracle.client.rpc.ProjectService;
 import de.unihamburg.zbh.fishoracle.server.data.DBInterface;
 import de.unihamburg.zbh.fishoracle.server.data.SessionData;
 
 public class ProjectServiceImpl extends RemoteServiceServlet implements ProjectService {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private SessionData getSessionData(){
 		return new SessionData(this.getThreadLocalRequest());
 	}
 	
 	@Override
 	public FoProject add(FoProject foProject) throws UserException {
 		
 		SessionData sessionData = getSessionData();
 		
 		sessionData.isAdmin();
 		
 		String servletContext = this.getServletContext().getRealPath("/");
 		
 		DBInterface db = new DBInterface(servletContext);
 		
 		return db.addFoProject(foProject);
 	}
 	
 	@Override
 	public FoProject[] fetch(String operationId) throws Exception {
 		
 		String servletContext = this.getServletContext().getRealPath("/");
 		
 		DBInterface db = new DBInterface(servletContext);
 		
 		SessionData sessionData = getSessionData();
 		
 		FoUser u = sessionData.getSessionUserObject();
 		
 		FoProject[] projects = null;
 		
		if(u.getIsAdmin() && operationId.equals(OperationId.PROJECT_FETCH_ALL)){
 			
 			projects = db.getAllProjects();
 			
 		} else if (!u.getIsAdmin() && operationId.equals(OperationId.PROJECT_FETCH_ALL)) {
 			
 			projects = db.getProjectsForUser(u, false, false);
 			
 		} else if(!u.getIsAdmin() && operationId.equals(OperationId.PROJECT_FETCH_READ_WRITE)){
 			
 			projects = db.getProjectsForUser(u, false, true);
 		}
 		
 		return projects;
 	}
 
 	@Override
 	public void update(FoProject project) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void delete(int projectId) throws UserException {
 		
 		SessionData sessionData = getSessionData();
 		
 		sessionData.isAdmin();
 		
 		String servletContext = this.getServletContext().getRealPath("/");
 		
 		DBInterface db = new DBInterface(servletContext);
 		
 		db.removeProject(projectId);	
 	}
 }
