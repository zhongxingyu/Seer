 
 package org.alt60m.ministry.servlet.modules.campus.location;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Vector;
 
 import org.alt60m.ministry.model.dbio.Activity;
 import org.alt60m.ministry.model.dbio.Contact;
 import org.alt60m.ministry.model.dbio.Person;
 import org.alt60m.ministry.model.dbio.TargetArea;
 import org.alt60m.ministry.servlet.InfoBaseTool;
 import org.alt60m.ministry.servlet.modules.InfoBaseModuleHelper;
 import org.alt60m.ministry.servlet.modules.InfoBaseModuleQueries;
 import org.alt60m.ministry.servlet.modules.model.Section;
 import org.alt60m.ministry.servlet.modules.person.PersonHelper;
 import org.alt60m.ministry.servlet.modules.team.TeamHelper;
 import org.alt60m.security.dbio.model.User;
 import org.alt60m.servlet.ActionResults;
 import org.alt60m.servlet.Controller.ActionContext;
 import org.alt60m.staffSite.bean.dbio.Bookmarks;
 import org.alt60m.util.ObjectHashUtil;
 
 public class LocationController extends org.alt60m.ministry.servlet.modules.InfoBaseModuleController {
 
     /**
 	 *
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	
 
     public LocationController() {
         log.debug("LocationController constructor");
     }
     private final String module="campus_location";
 	private final String title="Campus";
     public void init() {
         log.debug("LocationController.init()");
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
    
     public void saveTargetAreaInfo(ActionContext ctx) {
         try {
             if(ctx.getInputString("new")==null){
         	String targetAreaId = ctx.getInputString(TARGET_AREA_ID_TOKEN, true);
 			LocationHelper ibt = new LocationHelper();
             ibt.saveTargetAreaInfo(ctx.getHashedRequest(), targetAreaId);
         	content(ctx);
             } else {
             	LocationHelper ibt = new LocationHelper();
             	List admins=Arrays.asList("trent.murray@uscm.org","todd.gross@uscm.org");
             	log.debug((String)ctx.getSessionValue("userName"));
     			boolean admin = admins.contains(((String)ctx.getSessionValue("userName")).toLowerCase());
     			if (isNullOrEmpty(ctx.getInputString("Name")) || isNullOrEmpty(ctx.getInputString("City")) ||
     				isNullOrEmpty(ctx.getInputString("Country")) || isNullOrEmpty(ctx.getInputString("isSecure"))) 
     			{
     						ctx.setSessionValue("confirm", "One or more required fields was empty. Please try again.");
     			} else { // required fields OK
     				Hashtable request = ctx.getHashedRequest();
     				if (!admin) {
    					String serverName = (ctx.getRequest().getServerName().endsWith("campuscrusadeforchrist.com") ? "https://": "http://")
    							+ ctx.getRequest().getServerName()+
    							(ctx.getRequest().getServerName().endsWith("campuscrusadeforchrist.com") ? "":":"+ctx.getRequest().getServerPort());
     					ibt.sendTargetAreaRequestEmail(request, "isaac.kimball@uscm.org", ctx.getProfileID(),serverName);
     					ctx.setSessionValue("confirm", "Your location proposal has been sent.");
     				} else {
     					ibt.createNewTargetArea(request);
     					ctx.setSessionValue("confirm", "The location has been saved.");
     				}
             	
             	
             }
     			ctx.getResponse().sendRedirect("Campus");
         }
         }
         catch (Exception e) {
             ctx.setError();
             ctx.goToErrorView();
             log.error("Failed to perform saveTargetAreaInfo().", e);
         }
     }
 }
