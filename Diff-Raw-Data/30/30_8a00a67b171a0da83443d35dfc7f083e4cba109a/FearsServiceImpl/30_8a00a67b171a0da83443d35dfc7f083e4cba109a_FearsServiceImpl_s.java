 package eu.ist.fears.server;
 
 
 import java.io.IOException;
 import java.security.acl.NotOwnerException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpSession;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import pt.ist.dmapl.enforcement.AccessControlSession;
 import pt.ist.fenixframework.Config;
 import pt.ist.fenixframework.FenixFramework;
 import pt.ist.fenixframework.pstm.Transaction;
 
 import com.google.gwt.user.client.rpc.SerializationException;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.google.gwt.user.server.rpc.UnexpectedException;
 
 import edu.yale.its.tp.cas.client.ServiceTicketValidator;
 import eu.ist.fears.client.common.State;
 import eu.ist.fears.client.common.communication.FearsService;
 import eu.ist.fears.client.common.exceptions.FearsException;
 import eu.ist.fears.client.common.exceptions.NoFeatureException;
 import eu.ist.fears.client.common.exceptions.NoProjectException;
 import eu.ist.fears.client.common.exceptions.RequiredLogin;
 import eu.ist.fears.client.common.views.ViewAdmins;
 import eu.ist.fears.client.common.views.ViewFeatureDetailed;
 import eu.ist.fears.client.common.views.ViewFeatureResume;
 import eu.ist.fears.client.common.views.ViewProject;
 import eu.ist.fears.client.common.views.ViewVoterDetailed;
 import eu.ist.fears.client.common.views.ViewVoterResume;
 import eu.ist.fears.server.domain.FearsApp;
 import eu.ist.fears.server.domain.FeatureRequest;
 import eu.ist.fears.server.domain.Project;
 import eu.ist.fears.server.domain.User;
 
 public class FearsServiceImpl extends RemoteServiceServlet implements FearsService {
 
 	/**
 	 * 
 	 */	
 	private static final long serialVersionUID = -9186875057311859285L;
 
 	@Override
 	public void init() throws ServletException {
 		Config config = new Config() {{ 
 			domainModelPath = "/fears.dml";
 			dbAlias = "//localhost:3306/fears"; 
 			dbUsername = "root";
 			dbPassword = "fears";
 		}};
 		FenixFramework.initialize(config);
 	}
 
 	@Override
 	public String processCall(final String payload) throws SerializationException {
 		// process the RPC call within a transaction
 		while (true) {
 			Transaction.begin();
 			//AccessControlSession.beginAccessControl(getUserFromSession("..."));
 			boolean txFinished = false;
 			try {
 				String result = super.processCall(payload);
 				Transaction.commit();
 				txFinished = true;
 				return result;
 			} catch (jvstm.CommitException ce) {
 				Transaction.abort();
 				txFinished = true;
 			} finally {
 				AccessControlSession.endAccessControl();
 				if (! txFinished) {
 					Transaction.abort();
 				}
 			}
 		}
 	}
 
 	protected void isLoggedIn(String sessionID) throws FearsException{
 		if(getUserFromSession(sessionID)==null){
 			throw new RequiredLogin();
 		}
 
 	}
 
 	protected void isAdmin(String sessionID) throws FearsException{
 		isLoggedIn(sessionID);
 
 		if(!FearsApp.getFears().isAdmin(getUserFromSession(sessionID)))
 			throw new RequiredLogin();
 
 	}
 
 	public ViewFeatureDetailed vote(String projectID, String name, String sessionID) throws FearsException{
 		isLoggedIn(sessionID);
 
 		Project p =FearsApp.getFears().getProject(projectID);
 
 		if(p==null)
 			throw  new NoProjectException(projectID);
 
 		FeatureRequest f= p.getFeature(name);
 
 		if(f==null)
 			throw new NoFeatureException(projectID, name);
 
 		f.vote(getUserFromSession(sessionID).getVoter(p));
 		return  f.getDetailedView(getUserFromSession(sessionID).getVoter(p));
 	}
 
 	public void addFeature(String projectID, String name,
 			String description, String sessionID) throws FearsException{
 		isLoggedIn(sessionID);
 
 		Project p = FearsApp.getFears().getProject(projectID);
 
 		if(p==null)
 			throw new NoProjectException(projectID);
 
 		p.addFeature(new FeatureRequest(name, description, getUserFromSession(sessionID).getVoter(p) ));
 	}
 
 
 
 	public ViewFeatureDetailed getFeature(String projectID,
 			String name, String sessionID) throws FearsException{
 
 
 		Project p =FearsApp.getFears().getProject(projectID);
 
 		if(p==null)
 			throw new NoProjectException(projectID);
 
 		if(p.getFeature(name)==null)
 			throw new NoFeatureException(projectID, name);
 
 		if(getUserFromSession(sessionID)==null)
 			return p.getFeature(name).getDetailedView(null);
 
 		return p.getFeature(name).getDetailedView(getUserFromSession(sessionID).getVoter(p));
 
 	}
 
 	public ViewFeatureDetailed addComment(String projectID,
 			String featureName, String comment, State newState, String sessionID) throws FearsException {
 		isLoggedIn(sessionID);
 
 		Project p =FearsApp.getFears().getProject(projectID);
 
 		if(p==null)
 			throw new NoProjectException(projectID);
 
 
 		if(p.getFeature(featureName)==null)
 			throw new NoFeatureException(projectID, featureName);
 
 		p.getFeature(featureName).addComment(comment, getUserFromSession(sessionID).getVoter(p), newState);
 
 		if(newState!=null){
 			isAdmin(sessionID);
 			p.getFeature(featureName).setState(newState);
 		}
 
 		return p.getFeature(featureName).getDetailedView(getUserFromSession(sessionID).getVoter(p));
 	}
 
 	public void addProject(String name, String description, String sessionID) throws FearsException{
 		isAdmin(sessionID);
 
 		FearsApp.getFears().addProject(new Project(name, description, getUserFromSession(sessionID)), getUserFromSession(sessionID));
 	}
 
 	public ViewProject[] getProjects(String sessionID) {
 		return  FearsApp.getFears().getProjects();
 	}
 
 	public void deleteProject(String name, String sessionID) throws FearsException{
 		isAdmin(sessionID);
 
		if(FearsApp.getFears().getProject(name).getFeatureRequestCount()>=0)
 			throw new FearsException("Projecto n&atilde;o pode ser remoido, porque tem sugest&otilde;oes");
 
 		FearsApp.getFears().deleteProject(name);
 	}
 
 	/*public ViewVoterResume login(String username, String password) throws FearsException{
 		HttpSession session = this.getThreadLocalRequest().getSession();
 
 		//Fingir que esta tudo bem.
 
 		User temp = FearsApp.getFears().getUser(username);
 		ViewVoterResume ret =  new ViewVoterResume(temp.getName(),  session.getId(), FearsApp.getFears().isAdmin(temp));
 		session.setAttribute("fears_voter", ret);
 		return ret;
 
 	} */
 
 	public ViewVoterResume validateSessionID(String sessionID) {
 		HttpSession session = this.getThreadLocalRequest().getSession();
 		ViewVoterResume temp = (ViewVoterResume)session.getAttribute("fears_voter");
 		
 		return temp;
 	}
 
 	public User getUserFromSession(String sessionID){
 		HttpSession session = this.getThreadLocalRequest().getSession();
 		ViewVoterResume temp = (ViewVoterResume)session.getAttribute("fears_voter");
 		if(temp==null)
 			return null;
 		return FearsApp.getFears().getUser(temp.getName());
 	}
 
 	public ViewFeatureDetailed removeVote(String projectID, String feature,
 			String sessionID) throws FearsException {
 		isLoggedIn(sessionID);
 
 		Project p =FearsApp.getFears().getProject(projectID);
 
 		if(p==null)
 			throw new NoProjectException(projectID);
 
 		FeatureRequest f= p.getFeature(feature);
 
 		if(f==null)
 			throw new NoFeatureException(projectID, feature);
 
 		f.removeVote(getUserFromSession(sessionID).getVoter(p));
 		return  f.getDetailedView(getUserFromSession(sessionID).getVoter(p));
 	}
 
 	public ViewAdmins getAdmins(String sessionID) throws FearsException {
 		isAdmin(sessionID);
 
 		return FearsApp.getFears().getViewAdmins();
 	}
 
 
 	public ViewAdmins addAdmin(String userName, String sessionID) throws FearsException {
 		isAdmin(sessionID);
 
 		FearsApp.getFears().addAdmin(FearsApp.getFears().getUser(userName));
 		return FearsApp.getFears().getViewAdmins();
 	}
 
 
 	public ViewAdmins removeAdmin(String userName, String sessionID) throws FearsException {
 		isAdmin(sessionID);
 
 		FearsApp.getFears().removeAdmin(FearsApp.getFears().getUser(userName));
 		return FearsApp.getFears().getViewAdmins();
 	}
 
 
 	public List<ViewFeatureResume> search(String projectID, String search,
 			String sort, int page, String filter, String sessionID) throws FearsException{
 		Project p =FearsApp.getFears().getProject(projectID);
 
 		if(p==null)
 			throw new NoProjectException(projectID);
 
 		if(getUserFromSession(sessionID)==null)
 			return p.search(search, sort, page, filter, null);		
 
 
 		return p.search(search, sort, page, filter, getUserFromSession(sessionID).getVoter(p));
 
 	}
 
 	public String getProjectName(String projectID) throws FearsException {
 		Project p =FearsApp.getFears().getProject(projectID);
 
 		if(p==null)
 			throw new NoProjectException(projectID);
 
 		return p.getName();
 	}
 
 
 	public ViewVoterDetailed getVoter(String projectID, String voterName,
 			String sessionID) throws FearsException {
 		Project p =FearsApp.getFears().getProject(projectID);
 
 		if(p==null)
 			throw new NoProjectException(projectID);
 
 
 		return FearsApp.getFears().getUser(voterName).getVoter(p).getView(sessionID);
 	}
 
 	public void logoff(String sessionID) throws FearsException{
 		isLoggedIn(sessionID);
 
 		HttpSession session = this.getThreadLocalRequest().getSession();
 		session.invalidate();
 	}
 
 
 	public ViewVoterResume getCurrentVoter(String projectID, String sessionID) throws FearsException {
 
 		Project p =FearsApp.getFears().getProject(projectID);
 
 		if(p==null)
 			throw new NoProjectException(projectID);
 
 		User u = getUserFromSession(sessionID);
 
 		if(u==null)
 			return null;
 
 		return u.getVoter(p).getCurrentVoterView(sessionID);
 	}
 
 
 	public ViewVoterResume CASlogin(String ticket, String sessionID)
 	throws FearsException {
 		HttpSession session = this.getThreadLocalRequest().getSession();
 
 		String username=validateTicket(ticket, sessionID);
 		if(username!=null){
 			username = username.toLowerCase();
 			User temp = FearsApp.getFears().getUser(username);
 			ViewVoterResume ret =  new ViewVoterResume(temp.getName(),  session.getId(), FearsApp.getFears().isAdmin(temp));
 			session.setAttribute("fears_voter", ret);
 			System.out.println("CAS Login: User:" + ret.getName());
 			return ret;
 		}else System.out.println("ERRO NO CAS....");
 		return null;
 
 	}
 
 	public String validateTicket(String ticket, String sessionID){
 		HttpSession session = this.getThreadLocalRequest().getSession();
 
 		String user = null;
 		String errorCode = null;
 		String errorMessage = null;
 
 		ServiceTicketValidator cas = (ServiceTicketValidator)session.getAttribute("fears_CAS");
 		/* instantiate a new ServiceTicketValidator */
 		if(cas==null){
 			cas = new ServiceTicketValidator();
 			/* set its parameters */
 			cas.setCasValidateUrl("https://localhost:8443/cas/serviceValidate");
 			cas.setService("https://localhost:8443/webapp/Fears.html");
 
 
 			System.out.println("Deubg ticket:"+ticket);
 			cas.setServiceTicket(ticket);
 
 			try {
 				cas.validate();
 			} catch (IOException e) {
 				System.out.println("Validate error:"+ e);
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SAXException e) {
 				System.out.println("Validate error:"+ e);
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ParserConfigurationException e) {
 				System.out.println("Validate error:"+ e);
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			if(cas.isAuthenticationSuccesful()) {
 				user = cas.getUser();
 				session.setAttribute("fears_CAS", cas);
 			} else {
 				errorCode = cas.getErrorCode();
 				errorMessage = cas.getErrorMessage();
 				System.out.println(errorCode +errorMessage );
 				/* handle the error */
 			}
 
 			return user;
 		}
 		
 		//We have already authenticaed
 		else{
 			System.out.println("We have already authenticad.");
 			if(cas.isAuthenticationSuccesful()) {
 				user = cas.getUser();
 				System.out.println("User is valid.");
 			} else {
 				errorCode = cas.getErrorCode();
 				errorMessage = cas.getErrorMessage();
 				System.out.println(errorCode +errorMessage );
 				/* handle the error */
 			}
 			return user;
 
 		}
 
 	}
 
 }
