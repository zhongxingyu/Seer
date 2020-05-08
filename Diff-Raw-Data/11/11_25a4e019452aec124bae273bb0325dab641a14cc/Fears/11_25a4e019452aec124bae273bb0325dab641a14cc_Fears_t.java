 package eu.ist.fears.client;
 
 
 import java.util.Date;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.IFrameElement;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.HistoryListener;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import eu.ist.fears.client.admin.Admin;
 import eu.ist.fears.client.common.communication.Communication;
 import eu.ist.fears.client.common.exceptions.ExceptionsTreatment;
 import eu.ist.fears.client.common.views.ViewVoterResume;
 import eu.ist.fears.client.interfaceweb.Header;
 import eu.ist.fears.client.interfaceweb.Path;
 
 
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Fears extends Widget implements EntryPoint, HistoryListener   {
 
 
 	protected static Communication _com;
 	protected VerticalPanel frameBox;
 	protected VerticalPanel frame;
 	protected static VerticalPanel content;
 	protected static Header header; 
 	protected static DialogBox  popup;
 	protected static Path path;
 	protected static ViewVoterResume _curretUser;
 	public static boolean validCookie;
 	protected static String lastURL;
 
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 
 		if (RootPanel.get("Admin") != null){
 			return;			
 		}
 
 		init();
 
 		History.addHistoryListener(this);
 		History.fireCurrentHistoryState();
 	}	
 
 	public void init(){
 		_com = new Communication("service");
 
 		frameBox =  new VerticalPanel();
 		frame = new VerticalPanel();
 		content = new VerticalPanel();
 		content.setStyleName("width100");
 		path = new Path();
 		_curretUser=new ViewVoterResume("guest","",false);
 		header = new Header("guest",validCookie, false);
 		RootPanel.get().setStyleName("centered");
 		RootPanel.get().add(header);
 		frameBox.setStyleName("frameBox");
 		frame.setStyleName("frame");
 		RootPanel.get().add(frameBox); 
 		frameBox.add(frame);
 		frame.add(path);
 		frame.add(content);
 
 
 		String ticket=getTicket();
 		if(ticket!=null && !ticket.isEmpty()){
 			_com.CASlogin(ticket, null, new WaitForLogin());
 			return;
 		}
 
 		verifyLogin(false);
 		Fears.getHeader().update(false, isAdminPage());
 
 	}
 
 	public static void setContet(Widget w){
 		content.clear();
 		content.add(w);
 	}
 
 	public static void setError(Widget w){
 		w.setStyleName("error");
 		setContet(w);
 	}
 
 	public static boolean isAdminPage(){
 		return RootPanel.get("Admin")!= null;
 	}
 
 
 	public static Path getPath(){
 		return path;
 	}
 
 	public static Header getHeader(){
 		return header;
 	}
 
 	public static void setCurrentUser(ViewVoterResume v){
 		_curretUser=v;
 	}
 
 	public static boolean isLogedIn(){
 		return validCookie;
 	}
 
 	public static boolean isAdminUser(){
 		return _curretUser.isAdmin();
 	}
 
 	public static String getUsername(){
 		return _curretUser.getName();
 	}
 
 	public static int getVotesLeft(){
 		return _curretUser.getVotesLeft();
 	}
 
 	public void listFeatures(String projectName,String filter){
 		content.clear();
 
 		verifyLogin(false);
 
 		ListFeatures features = new ListFeatures(projectName, filter);
 
 		features.update();
 		content.add(features);
 	}
 
 	public void addFeature(String projectName){
 		content.clear();
 
 		if(!verifyLogin(true)){
 			content.add(new HTML("Por favor fa&ccedil;a login para continuar"));
 			return;
 		}
 
 		content.add(new CreateFeature(projectName));
 
 	}
 
 	public void viewFeature(String projectName, String featureID){
 		content.clear();
 
 		verifyLogin(false);
 		DisplayFeatureDetailed d =new DisplayFeatureDetailed(projectName, featureID);
 		content.add(d);
 	}
 
 	public void viewVoter(String projectID, String voterName){
 		content.clear();
 		verifyLogin(false);
 		content.add(new DisplayVoter(projectID, voterName));
 	}
 
 	public void viewListProjects(){
 		content.clear();
 
 		verifyLogin(false);
 
 		ListProjects projects = new ListProjects();
 
 		projects.update();	
 		content.add(projects);
 	}
 
 	public void viewLogin(){
 		//content.clear();
 
 		verifyLogin(false);
 
 		if(popup!=null)
 			popup.setVisible(false);
 		
 		popup =  new DialogBox(false,false);
 		VerticalPanel dialogContents = new VerticalPanel();
 		dialogContents.setSpacing(0);
 		popup.setWidget(dialogContents);
 
 		Login login = new Login(this);
 		dialogContents.add(login);
 		//dialogContents.add(iframe);
 
 		login.setStyleName("loginWindow");
 		popup.setSize("400px", "400px");
 		popup.setPopupPosition(500, 50);
 		saveFears(this);
 		popup.show();
 		//popup.getElement().setId("FEARSPOPUP");
 
 	}
 
 	public void viewLogout(){
 
 		validCookie=false;
 		header.update(false, isAdminPage());
 		_com.logoff(Cookies.getCookie("fears"), new ExceptionsTreatment(){
 			public void onSuccess(Object result) {}});
 		_curretUser.setName("guest");
 		Cookies.removeCookie("fears");
		RootPanel.get().add(new HTML(Cookies.getCookie("fears")));
 		History.back();
		
 	}
 
 
 
 
 	public void setCookie(String value, ViewVoterResume user){
 		final long DURATION = 1000 * 60 * 60 * 1; //duration remembering login, 1 hour
 		Date expires = new Date(System.currentTimeMillis() + DURATION);
 		Cookies.setCookie("fears", value, expires);
 		setCurrentUser(user);	
 		validCookie=true;
 	}
 
 
 	protected boolean verifyLogin(boolean tryToLogin){
 
 		if(validCookie){
 			return true;
 		}	
 
 		String sessionID = Cookies.getCookie("fears");
 
 		_com.validateSessionID(sessionID, new ValidateSession(this, tryToLogin));
 		return false;
 
 	}
 
 
 
 	public void onHistoryChanged(String historyToken) {
 		if (RootPanel.get("Admin") != null){
 			return;			
 		}
 
 		if(!"login".equals(historyToken) && !"logoff".equals(historyToken))
 			lastURL=historyToken;
 
 		header.update(false,false);
 		parseURL(historyToken, this);
 	}
 
 
 	public static void parseURL(String url, Fears f){
 		// This method is called whenever the application's history changes. Set
 		// the label to reflect the current history token.
 
 		if(url.length()==0){
 			f.viewListProjects();
 		}
 
 		if(url.startsWith("listProjects")){
 			f.viewListProjects();
 		}else if(url.startsWith("login")){
 			f.viewLogin();
 		}else if(url.startsWith("Project")){
 			projectParse(url.substring("Project".length()), f);	
 		}if(url.startsWith("admins")){
 			if(f instanceof Admin)
 				((Admin)f).viewChangeAdmins();
 		}else if(url.startsWith("logout")){
 			f.viewLogout();
 		}
 
 
 	}
 
 	private static void projectParse(String string, Fears f){
 		int parseAt =  string.indexOf('&');
 		int parseB =  string.indexOf("%26");
 		String projectID;
 		String parse;
 
 		//Estamos no Caso: #ProjectXPTO  
 		if(parseAt==-1 && parseB==-1 ){
 			projectID=string;
 			/* getCurrentUser, to update Votes*/ 
 			header.update(projectID);
 			f.listFeatures(projectID,"");
 			return;	
 		}
 
 
 		if(parseAt!=-1){
 			projectID = string.substring(0,parseAt);
 			parse = string.substring(parseAt+1);
 
 		}else{
 			projectID = string.substring(0,parseB);
 			parse = string.substring(parseB+3);
 		}
 
 		/* getCurrentUser, to update Votes*/ 
 		header.update(projectID);
 
 		if("listFeatures".equals(parse)){
 			f.listFeatures(projectID,"");	
 		}else if("addFeature".equals(parse)){
 			f.addFeature(projectID);
 		}else if(parse.startsWith("viewFeature")){
 			f.viewFeature(projectID, parse.substring("viewFeature".length()));
 		}else if(parse.startsWith("viewUser")){
 			f.viewVoter(projectID, parse.substring("viewUser".length()));
 		}
 		else if(parse.startsWith("filter")){
 			f.listFeatures(projectID, parse.substring("filter".length()));
 		}
 
 	}
 
 	public void loggedIn(){
		popup.hide();
 		if(History.getToken().equals("login")){
 			History.newItem(lastURL);
 		}else{
 			History.fireCurrentHistoryState();
 		}
 	}
 
 	public native void saveFears(Fears f)/*-{
 		$wnd.myfears=f;
 	}-*/;
	
 
 	public static native void callLoggedIn()/*-{
 	var temp=$wnd.parent.myfears;
 		temp.@eu.ist.fears.client.Fears::loggedIn()();
 	}-*/;
 
 	public static native String getParamString() /*-{
     return $wnd.location.search;
 }-*/;
 
 	public static String getTicket(){
 		String string=getParamString();
 		if(!string.startsWith("?ticket="))
 			return null;
 
 		int index=string.indexOf('=');
 		if(string.length()>index+1){
 			String ticket = string.substring(index+1);
 			return ticket;	
 		}
 		return null;
 
 	}
 
 
 	protected class ValidateSession extends ExceptionsTreatment{
 		Fears _f;
 		boolean _trytoLogin;
 
 		public ValidateSession(Fears f, boolean trytoLogin){
 			_f=f;
 			_trytoLogin=trytoLogin;
 		}
 
 		public void onSuccess(Object result){
 			ViewVoterResume voter = (ViewVoterResume) result;
 			if(voter!=null){
 				validCookie= true;
 				setCurrentUser(voter);
 				_f.onHistoryChanged(History.getToken());
 			}else{
 				if(_trytoLogin)
 					viewLogin();
 			}
 		}
 
 	};
 
 	protected class WaitForLogin extends ExceptionsTreatment{
 
 		public WaitForLogin(){}
 
 		public void onSuccess(Object result){
 			ViewVoterResume voter = (ViewVoterResume) result;
 			if(voter==null){
 			}
 			else {
 				Fears.setCurrentUser(voter);	
 				content.clear();
 				content.add(new HTML("Utilizador logado, a fechar janela de login..."));
				frameBox.setVisible(false);
 				Fears.callLoggedIn();
 				return;
 			}
 		}
 
 
 	}
 
 
 
 }
