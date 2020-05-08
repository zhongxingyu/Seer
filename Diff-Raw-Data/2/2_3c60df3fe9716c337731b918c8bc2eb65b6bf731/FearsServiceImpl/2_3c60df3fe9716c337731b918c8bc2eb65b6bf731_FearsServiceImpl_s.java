 package eu.ist.fears.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpSession;
 import javax.xml.parsers.ParserConfigurationException;
 
 import jvstm.Atomic;
 
 import org.xml.sax.SAXException;
 
 import com.google.gwt.user.client.rpc.SerializationException;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import edu.yale.its.tp.cas.client.ServiceTicketValidator;
 import eu.ist.fears.common.FearsConfigClient;
 import eu.ist.fears.common.State;
 import eu.ist.fears.common.communication.FearsService;
 import eu.ist.fears.common.exceptions.FearsException;
 import eu.ist.fears.common.exceptions.NoFeatureException;
 import eu.ist.fears.common.exceptions.NoProjectException;
 import eu.ist.fears.common.exceptions.NoUserException;
 import eu.ist.fears.common.exceptions.RequiredLogin;
 import eu.ist.fears.common.views.ViewAdmins;
 import eu.ist.fears.common.views.ViewFeatureDetailed;
 import eu.ist.fears.common.views.ViewFeatureResume;
 import eu.ist.fears.common.views.ViewProject;
 import eu.ist.fears.common.views.ViewVoterDetailed;
 import eu.ist.fears.common.views.ViewVoterResume;
 import eu.ist.fears.server.domain.FearsApp;
 import eu.ist.fears.server.domain.FeatureRequest;
 import eu.ist.fears.server.domain.Project;
 import eu.ist.fears.server.domain.User;
 import eu.ist.fears.server.domain.Voter;
 import eu.ist.fears.server.json.JSONException;
 import eu.ist.fears.server.json.JSONObject;
 
 public class FearsServiceImpl extends RemoteServiceServlet implements FearsService {
 
     private static final long serialVersionUID = -9186875057311859285L;
     private static Hashtable<String, String> table;
 
     @Override
     public void init() throws ServletException {
 	Init.init();
 	table = new Hashtable<String, String>();
     }
 
     public static String getNickName(String user) {
 	String nick = user;
 	String response = "";
 	if (FearsConfigServer.isRunningInProduction()) {
 	    if (table.get(user) == null) {
 		URL url;
 		try {
 		    url = new URL("https://fenix.ist.utl.pt//external/NameResolution.do?method=resolve&id=" + user
 			    + "&username=fenixRemoteRequests&password=" + FearsConfigServer.fenixAPIPassword());
 		    URLConnection conn = url.openConnection();
 		    // Get the response
 		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
 		    String line;
 		    while ((line = rd.readLine()) != null) {
 			response = response + line + "\n";
 		    }
 		    rd.close();
 
 		    nick = (String) new JSONObject(response).get("nickName");
 		    System.out.println("Nick:" + nick);
 		    table.put(user, nick);
 
 		} catch (MalformedURLException e) {
 		    e.printStackTrace();
 
 		} catch (IOException e) {
 		    e.printStackTrace();
 		} catch (JSONException e) {
 		    e.printStackTrace();
 		}
 
 		return nick;
 	    } else {
 		return table.get(user);
 	    }
 	}else{
 	    return SimpleNameGenerator.solveName(user);
 	}
     }
 
     @Override
     @Atomic
     public String processCall(final String payload) throws SerializationException {
 	return super.processCall(payload);
     }
 
     protected void isLoggedIn() throws FearsException {
 	if (getUserFromSession() == null) {
 	    throw new RequiredLogin();
 	}
 
     }
 
     protected void isAdmin() throws FearsException {
 	isLoggedIn();
 
 	if (!FearsApp.getFears().isAdmin(getUserFromSession()))
 	    throw new RequiredLogin();
 
     }
 
     public ViewFeatureDetailed vote(String projectID, String name, String sessionID) throws FearsException {
 	isLoggedIn();
 
 	Project p = FearsApp.getFears().getProject(projectID);
 
 	if (p == null)
 	    throw new NoProjectException(projectID);
 
 	FeatureRequest f = p.getFeature(name);
 
 	if (f == null)
 	    throw new NoFeatureException(projectID, name);
 
 	if (!f.getState().equals(State.Novo)) {
 	    throw new FearsException("So pode votar em sugestoes com o Estado Novo.");
 	}
 
 	f.vote(getUserFromSession().getVoter(p));
 	return f.getDetailedView(getUserFromSession().getVoter(p));
     }
 
     public void addFeature(String projectID, String name, String description, String sessionID) throws FearsException {
 	isLoggedIn();
 
 	Project p = FearsApp.getFears().getProject(projectID);
 
 	if (p == null)
 	    throw new NoProjectException(projectID);
 
 	p.addFeature(new FeatureRequest(name, description, getUserFromSession().getVoter(p), p.getInitialVotes()));
     }
 
     public ViewFeatureDetailed getFeature(String projectID, String name, String sessionID) throws FearsException {
 
 	Project p = FearsApp.getFears().getProject(projectID);
 
 	if (p == null)
 	    throw new NoProjectException(projectID);
 
 	if (p.getFeature(name) == null)
 	    throw new NoFeatureException(projectID, name);
 
 	if (getUserFromSession() == null)
 	    return p.getFeature(name).getDetailedView(null);
 
 	return p.getFeature(name).getDetailedView(getUserFromSession().getVoter(p));
 
     }
 
     public ViewFeatureDetailed addComment(String projectID, String featureName, String comment, State newState, String sessionID)
 	    throws FearsException {
 	isLoggedIn();
 
 	Project p = FearsApp.getFears().getProject(projectID);
 
 	if (p == null)
 	    throw new NoProjectException(projectID);
 
 	if (p.getFeature(featureName) == null)
 	    throw new NoFeatureException(projectID, featureName);
 
 	FeatureRequest feature = p.getFeature(featureName);
 
 	feature.addComment(comment, getUserFromSession().getVoter(p), newState);
 
 	if (newState != null) {
 	    if (!p.isProjectAdmin(getUserFromSession()))
 		isAdmin();
 
 	    // See if the old state is "New", and the new state any other.
 	    // Add 1 vote to all voters.
 	    if (feature.getState().equals(State.Novo) && !newState.equals(State.Novo)) {
 		for (Voter v : feature.getVoterSet()) {
 		    v.setVotesUsed(v.getVotesUsed() - 1);
 		}
 	    }
 
 	    // See if the old state is other than "New", and the new state is
 	    // "New".
 	    // Remove 1 vote to all voters that have votes left.
 	    if (!feature.getState().equals(State.Novo) && newState.equals(State.Novo)) {
 		for (Voter v : feature.getVoterSet()) {
 		    if (v.getVotesUsed() < p.getInitialVotes())
 			v.setVotesUsed(v.getVotesUsed() + 1);
 		}
 	    }
 
 	    feature.setState(newState);
 	}
 
 	return p.getFeature(featureName).getDetailedView(getUserFromSession().getVoter(p));
     }
 
     public void addProject(String name, String description, int nvotes, String sessionID) throws FearsException {
 	isAdmin();
 
 	FearsApp.getFears().addProject(new Project(name, description, nvotes, getUserFromSession()), getUserFromSession());
     }
 
     public void editProject(String projectID, String name, String description, int nvotes, String sessionID)
 	    throws FearsException {
 	isAdmin();
 
 	Project p = FearsApp.getFears().getProject(projectID);
 
 	if (p == null)
 	    throw new NoProjectException(projectID);
 
 	p.edit(name, description, nvotes);
     }
 
     public List<ViewProject> getProjects(String sessionID) {
 	return FearsApp.getFears().getProjectsViews();
     }
 
     public void deleteProject(String name, String sessionID) throws FearsException {
 	isAdmin();
 
 	if (FearsApp.getFears().getProject(name).getFeatureRequestCount() > 0)
 	    throw new FearsException("Projecto n&atilde;o pode ser remoido, porque tem sugest&otilde;oes");
 
 	FearsApp.getFears().deleteProject(name);
     }
 
     public ViewVoterResume validateSessionID(String sessionID) {
 	HttpSession session = this.getThreadLocalRequest().getSession();
 	ViewVoterResume temp = (ViewVoterResume) session.getAttribute("fears_voter");
 
 	return temp;
     }
 
     public User getUserFromSession() throws FearsException {
 	HttpSession session = this.getThreadLocalRequest().getSession();
 	ViewVoterResume temp = (ViewVoterResume) session.getAttribute("fears_voter");
 	if (temp == null)
 	    return null;
 	return FearsApp.getFears().getUser(temp.getName());
     }
 
     public ViewFeatureDetailed removeVote(String projectID, String feature, String sessionID) throws FearsException {
 	isLoggedIn();
 
 	Project p = FearsApp.getFears().getProject(projectID);
 
 	if (p == null)
 	    throw new NoProjectException(projectID);
 
 	FeatureRequest f = p.getFeature(feature);
 
 	if (f == null)
 	    throw new NoFeatureException(projectID, feature);
 
 	if (!f.getState().equals(State.Novo)) {
 	    throw new FearsException("So pode retirar o voto de sugestoes com o Estado Novo.");
 	}
 
 	f.removeVote(getUserFromSession().getVoter(p));
 	return f.getDetailedView(getUserFromSession().getVoter(p));
     }
 
     public ViewAdmins getAdmins(String sessionID) throws FearsException {
 	isAdmin();
 
 	return FearsApp.getFears().getViewAdmins();
     }
 
     public ViewAdmins addAdmin(String userName, String sessionID) throws FearsException {
 	isAdmin();
 
 	User u = null;
 	try {
 	    u = FearsApp.getFears().getUser(userName);
 	} catch (NoUserException e) {
 	    u = FearsApp.getFears().createUser(userName);
 	}
 
 	FearsApp.getFears().addAdmin(u);
 	return FearsApp.getFears().getViewAdmins();
     }
 
     public ViewAdmins removeAdmin(String userName, String sessionID) throws FearsException {
 	isAdmin();
 
 	if (userName.equals(getUserFromSession().getName()))
 	    throw new FearsException("Nao se pode remover a si proprio de Administrador.");
 
 	FearsApp.getFears().removeAdmin(FearsApp.getFears().getUser(userName));
 	return FearsApp.getFears().getViewAdmins();
     }
 
     public List<ViewFeatureResume> search(String projectID, String search, int sort, int page, String filter, String sessionID)
 	    throws FearsException {
 	Project p = FearsApp.getFears().getProject(projectID);
 
 	if (p == null)
 	    throw new NoProjectException(projectID);
 
 	if (getUserFromSession() == null)
 	    return p.search(search, sort, page, filter, null);
 
 	return p.search(search, sort, page, filter, getUserFromSession().getVoter(p));
 
     }
 
     public String getProjectName(String projectID) throws FearsException {
 	Project p = FearsApp.getFears().getProject(projectID);
 
 	if (p == null)
 	    throw new NoProjectException(projectID);
 
 	return p.getName();
     }
 
     public ViewProject getProject(String projectID) throws FearsException {
 	Project p = FearsApp.getFears().getProject(projectID);
 
 	if (p == null)
 	    throw new NoProjectException(projectID);
 
 	return p.getView();
     }
 
     public ViewAdmins getProjectAdmins(String projectID) throws FearsException {
 	Project p = FearsApp.getFears().getProject(projectID);
 
 	if (p == null)
 	    throw new NoProjectException(projectID);
 	return p.getViewAdmins();
 
     }
 
     public ViewAdmins addProjectAdmin(String newAdmin, String projectID) throws FearsException {
 	isAdmin();
 
 	Project p = FearsApp.getFears().getProject(projectID);
 
 	if (p == null)
 	    throw new NoProjectException(projectID);
 
 	User u = null;
 	try {
 	    u = FearsApp.getFears().getUser(newAdmin);
 	} catch (NoUserException e) {
 	    u = FearsApp.getFears().createUser(newAdmin);
 	}
 	p.addAdmin(u);
 
 	return p.getViewAdmins();
     }
 
     public ViewAdmins removeProjectAdmin(String oldAdmin, String projectID) throws FearsException {
 	isAdmin();
 
 	Project p = FearsApp.getFears().getProject(projectID);
 
 	if (p == null)
 	    throw new NoProjectException(projectID);
 
 	if (oldAdmin.equals(getUserFromSession().getName()))
 	    throw new FearsException("Nao se pode remover a si proprio de Administrador.");
 
 	p.removeAdmin(FearsApp.getFears().getUser(oldAdmin));
 
 	return p.getViewAdmins();
     }
 
     public List<ViewVoterDetailed> getVoter(String projectID, String userOID, String sessionID) throws FearsException {
 
 	Project actualP = null;
 	if (projectID != null)
 	    actualP = FearsApp.getFears().getProject(projectID);
 
 	ArrayList<ViewVoterDetailed> res = new ArrayList<ViewVoterDetailed>();
 	User u = FearsApp.getFears().getUserFromID(userOID);
 
 	try {
 	    u.getName();
 	} catch (Exception e) {
 	    throw new NoUserException();
 	}
 
 	if (actualP != null)
 	    res.add(u.getVoter(actualP).getView());
 
 	for (Project p : FearsApp.getFears().getProjectsSorted()) {
 	    if (p != actualP)
 		res.add(u.getVoter(p).getView());
 	}
 
 	return res;
     }
 
     public void logoff(String sessionID) throws FearsException {
 	isLoggedIn();
 
 	HttpSession session = this.getThreadLocalRequest().getSession();
 	session.invalidate();
     }
 
     public ViewVoterResume getCurrentVoter(String projectID, String sessionID) throws FearsException {
 
 	Project p = FearsApp.getFears().getProject(projectID);
 
 	if (p == null)
 	    throw new NoProjectException(projectID);
 
 	User u = getUserFromSession();
 
 	if (u == null)
 	    return null;
 
 	return u.getVoter(p).getCurrentVoterView(sessionID);
     }
 
     public ViewVoterResume CASlogin(String ticket, boolean admin, String sessionID) throws FearsException {
 	HttpSession session = this.getThreadLocalRequest().getSession();
 
 	String username = validateTicket(ticket, admin, sessionID);
 	if (username != null) {
 	    username = username.toLowerCase();
 	    User temp = null;
 	    try {
 		temp = FearsApp.getFears().getUser(username);
 	    } catch (NoUserException e) {
 		temp = FearsApp.getFears().createUser(username);
 	    }
 	    ViewVoterResume ret = new ViewVoterResume(temp.getName(), getNickName(temp.getName()), new Long(temp.getOid())
 		    .toString(), FearsApp.getFears().isAdmin(temp));
 	    session.setAttribute("fears_voter", ret);
 	    return ret;
 	} else
 	    System.out.println("ERRO NO CAS....");
 	return null;
 
     }
 
     public String validateTicket(String ticket, boolean admin, String sessionID) {
 	if (FearsConfigServer.isRunningInProduction()) {
 	    String user = null;
 	    String errorCode = null;
 	    String errorMessage = null;
 
 	    ServiceTicketValidator cas = new ServiceTicketValidator();
 	    /* instantiate a new ServiceTicketValidator */
 
 	    /* set its parameters */
	    cas.setCasValidateUrl(FearsConfigClient.getCasUrl() + "serviceValidate");
 	    if (admin)
 		cas.setService(PropertiesManager.getProperty("production.url") + "Admin.html");
 	    else
 		cas.setService(PropertiesManager.getProperty("production.url") + "Fears.html");
 	    
 	    cas.setServiceTicket(ticket);
 
 	    try {
 		cas.validate();
 	    } catch (IOException e) {
 		System.out.println("Validate error:" + e);
 		e.printStackTrace();
 	    } catch (SAXException e) {
 		System.out.println("SAXException:" + e);
 		e.printStackTrace();
 	    } catch (ParserConfigurationException e) {
 		System.out.println("ParserConfigurationException:" + e);
 		e.printStackTrace();
 	    }
 
 	    if (cas.isAuthenticationSuccesful()) {
 		user = cas.getUser();
 	    } else {
 		System.out.println("CAS ERROR\n");
 		errorCode = cas.getErrorCode();
 		errorMessage = cas.getErrorMessage();
 		System.out.println(errorCode + errorMessage);
 	    }
 	    return user;
 	} else {
 	    String user = ticket.substring(0, ticket.indexOf('/'));
 	    return user;
 	}
     }
 
     public List<ViewProject> projectUp(String projectId, String cookie) throws FearsException {
 	isAdmin();
 	FearsApp.getFears().projectUp(projectId);
 	return FearsApp.getFears().getProjectsViews();
 
     }
 
     public List<ViewProject> projectDown(String projectId, String cookie) throws FearsException {
 	isAdmin();
 	FearsApp.getFears().projectDown(projectId);
 	return FearsApp.getFears().getProjectsViews();
     }
 
     public Boolean userCreatedFeature(String cookie) throws FearsException {
 	isLoggedIn();
 	User actual = getUserFromSession();
 
 	for (Voter v : actual.getVoter()) {
 	    if (v.getFeaturesCreatedCount() > 0)
 		return true;
 	}
 
 	return false;
     }
 
 }
