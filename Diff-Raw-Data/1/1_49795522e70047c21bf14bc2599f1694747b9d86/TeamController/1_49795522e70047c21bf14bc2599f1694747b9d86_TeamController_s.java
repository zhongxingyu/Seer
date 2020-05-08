 
 package org.alt60m.ministry.servlet.modules.team;
 
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.alt60m.ministry.Regions;
 import org.alt60m.ministry.model.dbio.Activity;
 import org.alt60m.ministry.model.dbio.Contact;
 import org.alt60m.ministry.model.dbio.LocalLevel;
 import org.alt60m.ministry.model.dbio.Person;
 import org.alt60m.ministry.model.dbio.TargetArea;
 import org.alt60m.ministry.servlet.InfoBaseTool;
 import org.alt60m.ministry.servlet.modules.InfoBaseModuleHelper;
 import org.alt60m.ministry.servlet.modules.InfoBaseModuleQueries;
 import org.alt60m.ministry.servlet.modules.campus.location.LocationHelper;
 import org.alt60m.ministry.servlet.modules.model.Section;
 import org.alt60m.ministry.servlet.modules.person.PersonHelper;
 import org.alt60m.security.dbio.model.User;
 import org.alt60m.servlet.ActionResults;
 import org.alt60m.servlet.Controller.ActionContext;
 import org.alt60m.staffSite.bean.dbio.Bookmarks;
 import org.alt60m.util.ObjectHashUtil;
 
 public class TeamController extends org.alt60m.ministry.servlet.modules.InfoBaseModuleController {
 
     /**
 	 *
 	 */
 	private static final long serialVersionUID = 1L;
 
 	
 
     public TeamController() {
         log.debug("TeamController constructor");
         
     }
     private final String module="team";
 	private final String title="Team";
     public void init() {
         log.debug("TeamController.init()");
         try {
             initState();
         } catch (Exception e) {
             log.fatal("Failed to init!", e);
         }
     }
     protected void initState() throws Exception {
         initViews(getServletContext().getRealPath("/WEB-INF/modules/views.xml"));
         setDefaultAction("index");
         _bookmarks = new Bookmarks();
     }
     /** @param ctx ActionContext object Request parameters: <none> */
   
 	public void makeNewPerson(ActionContext ctx){
 		try{
 			ActionResults results = (ActionResults)ctx.getSessionValue("team_response");
         	results.putValue("add_person","true" );
 			results.putValue("teamID",ctx.getInputString("teamID"));
 			results.putValue("activityid","");
 			results.putValue("purpose","team");
 			results.putValue("id",ctx.getInputString("id"));
 			ctx.setReturnValue(results);
 			ctx.goToView(results.getValue("view"));
 		
 		}
 		catch (Exception e) {
 		 ctx.setError();
 		 ctx.goToErrorView();
 		 log.error("Failed to perform makeNewPerson().", e);
 		}
 	}
     
     public void saveTeamMember(ActionContext ctx) {
         try {
         	if (! loginCheck(ctx)) {
         		return;
         	}
             String teamID = ctx.getInputString("id");
             String personID = ctx.getInputString("personID");
             TeamHelper ibt = new TeamHelper();
             ibt.saveTeamMember(personID, teamID);
           content(ctx);
             
         }
         catch (Exception e) {
             ctx.setError();
             ctx.goToErrorView();
             log.error("Failed to perform saveTeamMember().", e);
         }
     }
     public void removeTeamMember(ActionContext ctx) {
         try {
         	if (! loginCheck(ctx)) {
         		return;
         	}
         	ActionResults result=new ActionResults("removeTeamMember");
     		result.putValue("personID",ctx.getInputString("personID"));
         	String teamID = ctx.getInputString("id");
             String personID = ctx.getInputString("personID");
             TeamHelper ibt = new TeamHelper();
 			ibt.removeTeamMember( personID,teamID);
 			
 			content(ctx);
         }
         catch (Exception e) {
             ctx.setError();
             ctx.goToErrorView();
             log.error("Failed to perform removeTeamMember().", e);
         }
     }
     public void addMissionalTeamMember(ActionContext ctx) {
         try {
         	ActionResults results = new ActionResults("addMissionalTeamMember");
             InfoBaseModuleHelper ibt = new InfoBaseModuleHelper();
             String search = "";
             Vector<Contact> contacts=new Vector<Contact>();
             if((ctx.getInputString("lastName")!= "")) {
             	search = ctx.getInputString("lastName") + "%";
             	contacts = ibt.listContactsByLastName(search.toUpperCase().replace("'", "%27"));
             }
             else {
 	        	results.putValue("infoMessage", "You need to specify a last name.");
             }
             String teamID = ctx.getInputString("id", true);
             results.addCollection("contacts", contacts);
             results.putValue("lastName",ctx.getInputString("lastName"));
             results.addHashtable("search",TeamHelper.sessionSearch(ctx));
 			results.addCollection("content", TeamHelper.content(teamID));
 			results.addHashtable("info", TeamHelper.info(teamID));
 			Person person=getUserPerson(ctx);
             results.putValue("personID",person.getPersonID()+"");
             LocalLevel ll=new LocalLevel(teamID);
             results.putValue("isRD",isTeamLeader(person,ll));
 			results.putValue("module",this.module);
 			results.putValue("title",this.title);
 			results.putValue("mode","addMember");
 			ctx.setReturnValue(results);
 			ctx.goToView("index");
         }
         catch (Exception e) {
 			ctx.setError();
             ctx.goToErrorView();
             log.error("Failed to perform addMissionalTeamMember().", e);
         }
     }
     public void saveTeam(ActionContext ctx) {
         try {
         	if (! loginCheck(ctx)) {
         		return;
         	}
         	if(ctx.getInputString("new")==null){
         	String mode = "update";
             String localLevelId = null;
             localLevelId = ctx.getInputString("locallevelid", true); 
   			TeamHelper.saveTeam(ctx.getHashedRequest(), localLevelId, mode);
             content(ctx);
         	} else {
         		TeamHelper ibt = new TeamHelper();
         		List admins=Arrays.asList("todd.gross@uscm.org","mark.kohman@uscm.org","todd.gross@cojourners.com","ruth.rhea@uscm.org","justin.sabelko@uscm.org","robin.muscarella@studentventure.org");
         		boolean admin = admins.contains(((String)ctx.getSessionValue("userName")).toLowerCase());
     			Hashtable request = ctx.getHashedRequest();
     			if (!admin) {
     				String serverName = (ctx.getRequest().getServerName().endsWith("campuscrusadeforchrist.com") ? "https://": "http://")
 					+ ctx.getRequest().getServerName()+
 					(ctx.getRequest().getServerName().endsWith("campuscrusadeforchrist.com") ? "":":"+ctx.getRequest().getServerPort());
     				ibt.sendLocalLevelRequestEmail(request,"todd.gross@uscm.org", ctx.getProfileID(),serverName);
     				ctx.setSessionValue("confirm", "Your location proposal has been sent.");
     			} else {
     				ibt.saveTeam(request, null, "new");
     				ctx.setSessionValue("confirm", "The new team has been saved.");
     			}
         	
             index(ctx);	
             }
         }
         catch (Exception e) {
             ctx.setError();
             ctx.goToErrorView();
             log.error("Failed to perform saveTeam().", e);
         }
     }
     public void saveTeamLeader(ActionContext ctx) {
         try {
         	if (! loginCheck(ctx)) {
         		return;
         	}
             String teamID = ctx.getInputString("teamID", true);
             String personID = ctx.getInputString("personID", true);
             TeamHelper ibt = new TeamHelper();
             ibt.saveTeamLeader(personID, teamID);
             content(ctx);
             
         }
         catch (Exception e) {
             ctx.setError();
             ctx.goToErrorView();
             log.error("Failed to perform saveTeamLeader().", e);
         }
     }
     public void removeTeamLeader(ActionContext ctx) {
         try {
         	if (! loginCheck(ctx)) {
         		return;
         	}
         	String teamID = ctx.getInputString("teamID", true);
             String personID = ctx.getInputString("personID");
             TeamHelper ibt = new TeamHelper();
 			ibt.removeTeamLeader( personID,teamID);
 			content(ctx);
         }
         catch (Exception e) {
             ctx.setError();
             ctx.goToErrorView();
             log.error("Failed to perform removeTeamLeader().", e);
         }
     }
 }
