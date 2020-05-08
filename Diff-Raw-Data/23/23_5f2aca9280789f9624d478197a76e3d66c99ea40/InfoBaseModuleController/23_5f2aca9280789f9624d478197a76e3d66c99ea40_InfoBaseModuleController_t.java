 
 package org.alt60m.ministry.servlet.modules;
 
 import java.util.Date;
 
 import java.util.Hashtable;
 
 import java.util.Iterator;
 
 import java.util.Vector;
 
 import org.alt60m.ministry.model.dbio.Person;
 
 import org.alt60m.ministry.model.dbio.LocalLevel;
 
 import org.alt60m.ministry.model.dbio.Staff;
 
 import org.alt60m.ministry.servlet.modules.model.Section;
 
 import org.alt60m.security.dbio.model.User;
 import org.alt60m.servlet.ActionResults;
 import org.alt60m.servlet.Controller;
 
 
 import org.alt60m.staffSite.bean.dbio.Bookmarks;
 import org.alt60m.util.Toolbox;
 
 
 /**
  * Web controller for InfoBase History: 4/10/01	MDP	Initial Coding Completeness (0 - 5): 3 Known Issues:<p>
  * refactoring and repackaging 5/23/03 MAB
  * @author  Mark Petrotta
  * @version 1.0
  */
 public class InfoBaseModuleController extends Controller {
 	
 	private final String module="home";
 	private final String title="Home";
     /**
 	 *
 	 */
 	private static final long serialVersionUID = 1L;
 
 	class StaffByRegionCache {
         Date lastUpdated;
 		Hashtable staffByRegion = new Hashtable();
     }
     protected final String[] _abbrevRegion = new String[] { "NE", "MA", "MS", "SE", "GL", "UM", "GP", "RR", "NW", "SW" };
 
     protected Bookmarks _bookmarks;
    
     protected final String[] _reportTypes = new String[] { "locallevel", "targetarea", "regional", "national" };
     protected final String LOCAL_LEVEL_ID_TOKEN = "locallevelid";
     protected final String TARGET_AREA_ID_TOKEN = "targetareaid";
    
     
     public InfoBaseModuleController() {
         log.debug("InfoBaseModuleController constructor");
        
        
         
     }
     public Person getUserPerson(ActionContext ctx){
     	 String username = (String) ctx.getSessionValue("userName");
          User user=new User();
          user.setUsername(username);
          user.select();
          Person person=new Person();
          person.setFk_ssmUserID(user.getUserID());
          person.select();
          return person;
     }
     public void home(ActionContext ctx) {
         try {
         	ActionResults results = new ActionResults("home");
         	ctx.setSessionValue("quicksearch",ctx.getInputString("quicksearch")==null?null:"true");
         	String lastClass=InfoBaseModuleHelper.lastClass(ctx);
         	results.addHashtable("search",InfoBaseModuleHelper.sessionSearch(ctx));
         	results.addHashtable("info",InfoBaseModuleHelper.infotize(new LocalLevel()));
         	results.putValue("module", "home");
 			results.putValue("title", "Home");
 			results.putValue("mode", "home");
 			ctx.setReturnValue(results);
             ctx.goToView("home");
         }
         catch (Exception e) {
 			e.printStackTrace();
             ctx.setError();
             ctx.goToErrorView();
             log.error("Failed to perform home().", e);
         }
     }
     
     /** @param ctx ActionContext object */
     public void search(ActionContext ctx) {
         try {
         	ActionResults results = new ActionResults("search");
         	String granularity="name";
         	String view="index";
         	String lastClass=InfoBaseModuleHelper.lastClass(ctx);
     		ctx.setSessionValue("lastClass", lastClass);
     		ctx.setSessionValue(lastClass, "search");
         	ctx.setSessionValue("home", "search");
            
             Hashtable terms=InfoBaseModuleHelper.storeSearch(ctx);
             Section list;
             Vector<Section> content=new Vector<Section>();
             if(ctx.getInputString("breadcrumb")==null||(!((String)terms.get("city")).equals(""))||(!((String)terms.get("name")).equals(""))){
 					list=InfoBaseModuleHelper.getSearchResults((String)terms.get("type"),
 					(String)terms.get("name"),(String)terms.get("city"),(String)terms.get("state"),
 					(String)terms.get("region"),(String)terms.get("country"),(String)terms.get("strategy"),ctx.getInputString("singleField")!=null);
 					content.add(list);
             } else {
             	granularity=(String)ctx.getInputString("breadcrumb");
             	content=InfoBaseModuleHelper.getBreadcrumbSearchResults((String)terms.get("type"),
     					(String)terms.get("name"),(String)terms.get("city"),(String)terms.get("state"),
     					(String)terms.get("region"),(String)terms.get("country"),granularity);
             	results.putValue("breadcrumb","true");
             	view=lastClass+"_home";
             }
 			results.addHashtable("search",InfoBaseModuleHelper.sessionSearch(ctx));
 			results.addCollection("content", content);
 			results.addHashtable("newInfo",InfoBaseModuleHelper.newInfo(ctx, lastClass) );
 			results.addHashtable("info",InfoBaseModuleHelper.searchInfo(ctx));
 			results.putValue("module", lastClass);
 			results.putValue("title", "Search Results");
 			results.putValue("mode", "list");
 			results.putValue("view", view);
 			results.putValue("granularity", granularity);
 			ctx.setSessionValue(lastClass+"_response", results);//put this before switch tests
 			ctx.setReturnValue(results);
             ctx.setSessionValue("breadcrumb",null);
             ctx.goToView(view);
         }
         catch (Exception e) {
 			e.printStackTrace();
             ctx.setError();
             ctx.goToErrorView();
             log.error("Failed to perform search().", e);
         }
     }
     public void content(ActionContext ctx){
     	try {
     		ActionResults results = new ActionResults("content()");
     		String lastClass=InfoBaseModuleHelper.lastClass(ctx);
     		String id = ctx.getInputString("id"); 
             if (id!=null){
             	ctx.setSessionValue(lastClass, ctx.getInputString("id"));
             } else {
             	id=(String)ctx.getSessionValue(lastClass)+"";
             }
             results.addHashtable("search", InfoBaseModuleHelper.lastSearch(ctx));
             results.addCollection("content", InfoBaseModuleHelper.content(id, lastClass)); //write this; classes vary though-- put forks in lower layer
             results.addHashtable("info",InfoBaseModuleHelper.info(id, lastClass) );
             results.addHashtable("newInfo",InfoBaseModuleHelper.newInfo(ctx, lastClass) );
             Person person=getUserPerson(ctx);
             results.putValue("personID",person.getPersonID()+"");
             results.putValue("isRD",isLeader(person,lastClass,id));
 			results.putValue("module",lastClass);
 			results.putValue("title",lastClass);
 			results.putValue("mode","content");
 			results.putValue("view","index");
 			ctx.setSessionValue(lastClass+"_response", results);//put this before switch tests
 			 ctx.setReturnValue(results);
             ctx.goToView("index");
     	 }
         catch (Exception e) {
 			e.printStackTrace();
             ctx.setError();
             ctx.goToErrorView();
             log.error("Failed to perform content().", e);
         }
     }
     /** @param ctx ActionContext object Request parameters: <none> */
     public void index(ActionContext ctx) {
         try {
         	String lastClass=InfoBaseModuleHelper.lastClass(ctx);
         	ctx.setSessionValue("quicksearch",ctx.getInputString("quicksearch")==null?null:"true");
         	String id=ctx.getInputString("id");
         	if(id==null){
         		id=(String)ctx.getSessionValue(lastClass);
         		}
         	if(id==null||id.equals("0")||id.equals("")){
         		ActionResults results = new ActionResults(lastClass+"_home");
         		if(!lastClass.equals("person")){
         		results.addCollection("content", InfoBaseModuleHelper.getBreadcrumbSearchResults(lastClass,"","","","('nonnull')","","region"));
         		}
     			results.addHashtable("search",InfoBaseModuleHelper.sessionSearch(ctx));
     			
             	results.putValue("breadcrumb","true");
             	results.addHashtable("info",InfoBaseModuleHelper.searchInfo(ctx));
             	results.addHashtable("newInfo",InfoBaseModuleHelper.newInfo(ctx, lastClass) );
                 
             	results.putValue("module", lastClass);
     			results.putValue("title", lastClass+" home");
     			results.putValue("mode", "home");
 				results.putValue("granularity", "region");
 				if(ctx.getInputString("edit")!=null){results.putValue("edit_"+lastClass,"true" );}else{results.removeValue("edit_"+lastClass);}
     			if(ctx.getInputString("new")!=null) {results.putValue("new_"+lastClass,"true" ); }else {results.removeValue("new_"+lastClass);}
     			
 				ctx.setReturnValue(results);    	
                 ctx.goToView(lastClass+"_home");
                 id="";
         	}else{
         	if (ctx.getSessionValue(lastClass+"_response")==null){
 	        	if(id.equals("search")){
 	    			search(ctx);
 	    		}else if(!id.equals("0")&&!id.equals("")){
 	    			content(ctx);
 	        	}
         	} else
         	{
         		ActionResults results=(ActionResults)ctx.getSessionValue(lastClass+"_response");
     			if(ctx.getInputString("edit")!=null){results.putValue("edit_"+lastClass,"true" );}else{results.removeValue("edit_"+lastClass);}
     			if(ctx.getInputString("new")!=null) {results.putValue("new_"+lastClass,"true" ); }else {results.removeValue("new_"+lastClass);}
    			if(ctx.getInputString("new")!=null) results.addHashtable("newInfo",InfoBaseModuleHelper.newInfo(ctx, lastClass) );
     			ctx.setReturnValue(results);
         		ctx.goToView(results.getValue("view"));
         	}
         	}
         }
         catch (Exception e) {
             ctx.goToErrorView();
             log.error("Failed to perform index().", e);
         }
     }
 
     public void init() {
         log.debug("InfoBaseController.init()");
         try {
             initState();
         } catch (Exception e) {
             log.fatal("Failed to init!", e);
         }
     }
 
     protected void initState() throws Exception {
         initViews(getServletContext().getRealPath("/WEB-INF/modules/views.xml"));
         setDefaultAction("home");
         _bookmarks = new Bookmarks();
     }
 
     protected boolean inList(String[] list, String value) {
         for (int i = 0; i < list.length; i++) {
             if (list[i].equalsIgnoreCase(value)) return true;
         }
         return false;
     }
 
     protected boolean isNullOrEmpty(String string) {
         return !(string != null && string.length() > 0);
     }
 
 	public Boolean isBlank(String string){
 		return (
 				(string).equals("")
 				||
 				(string).equals("null")
 				||
 				(string)==null
 				)
 				
 				;
 	}
 	public Boolean isBlankOrNonNumeric(String string){
 		return (
 				(string).replaceAll("[^0123456789]","error").contains("error")
 				||
 				isBlank(string)
 				)
 				;
 	}
 	public Boolean blankEqualsZeroCompare(String string,String compareString){
 		if (isBlank(string)){string="0";}
 		if (isBlank(compareString)){string="0";}
 		return (
 				string.equals(compareString)
 				)
 				;
 	}
 	
     protected boolean loginCheck(ActionContext ctx) {
 		String username = (String) ctx.getSessionValue("userName");
 		if (username == null) {
 			ctx.goToView("login");
 			return false;
 		}
 		return true;
 	}
 	public Hashtable<String,Hashtable<String,String>> convertBracketedParamsToHashtable(ActionContext ctx) {
 		Hashtable input=(ctx.getHashedRequest());
 		
 		Hashtable<String,Hashtable<String,String>> output=new Hashtable<String,Hashtable<String,String>>();
 
 		String content;
 		
 		Iterator getTheKeys=input.keySet().iterator();
 		String theKey;
 		String contentKey;
 		String objectCandidate;
 		Hashtable<String,String> contentCandidate;
 		while(getTheKeys.hasNext())
 		{
 			
 			objectCandidate="";
 			contentCandidate=new Hashtable<String,String>();
 			contentKey="";
 			content="";
 			theKey=(String)(getTheKeys.next());
 			if (theKey.contains("[")&&theKey.contains("]")&&(theKey.indexOf("[")!=0)&&(theKey.indexOf("[")<theKey.indexOf("]")))
 			{
 				
 				
 				objectCandidate=theKey.substring(0,theKey.indexOf("["));
 				
 					content=((String)(ctx.getInputString(theKey)));
 					
 				
 				contentKey=(theKey.substring(theKey.indexOf("[")+1,theKey.indexOf("]")));
 				if(output.keySet().contains(objectCandidate))
 	  			{
 	  			contentCandidate=new Hashtable<String,String>(output.get(objectCandidate));
 	  			contentCandidate.put(contentKey,content);
 					output.put(objectCandidate, contentCandidate);
 	  			}
 	  			else
 	  			{
 	  				contentCandidate=new Hashtable<String,String>();
 		    			contentCandidate.put(contentKey,content);
 	  				output.put(objectCandidate,contentCandidate);
 	  			}
 				
 			}
 		}
 		return  output;
 	}
     public static String getUsersPersonId(ActionContext ctx){
     	org.alt60m.security.dbio.model.User user=new org.alt60m.security.dbio.model.User();
 		user.setUsername((String)ctx.getSessionValue("userName"));
 		user.select();
 		org.alt60m.ministry.model.dbio.Person person=new org.alt60m.ministry.model.dbio.Person();
 		person.setFk_ssmUserID(user.getUserID());
 		person.select();
 		return person.getPersonID()+"";
     }
     protected String isRD(Person person)
     {
     	
         String personID = person.getPersonID()+"";
         String isRD="false";
         LocalLevel lab=new LocalLevel(); //LAB members are honorary RDees!!
         lab.setName("The LAB");
         lab.setRegion("NC");
         lab.select();
         Section labMembers=InfoBaseModuleQueries.getTeamMembers(lab.getLocalLevelId());
         for (Hashtable c:labMembers.getRows()){
         	if((c.get("id")+"").equals(personID)){
         		log.debug("The LAB!");
         		isRD="true";
         	}
         	
         }
        
        Staff staff=new Staff(person.getAccountNo());
        if (staff.getJobTitle().contains("Regional Director")){
     	   log.debug("Real RDee!");
    			isRD="true";
    			
        }
        return isRD;
     }
     protected String isTeamLeader(Person person,LocalLevel ll) throws Exception
     {
     	
         if(isRD(person).equals("true")){
         	return "true";
         }
     	String personID = person.getPersonID()+"";
        Boolean isLeader=InfoBaseModuleQueries.isTeamLeader(person,ll);
       return isLeader?"true":"false";
     }
     protected String isLeader(Person person,String type,String id) throws Exception
     {
     	
         if(isRD(person).equals("true")){
         	return "true";
         }
     	String personID = person.getPersonID()+"";
     	Boolean isLeader=false;
     	if(type.equals("team")){
     		LocalLevel ll=new LocalLevel(id);
        isLeader=InfoBaseModuleQueries.isTeamLeader(person,ll);
     	} 
       return isLeader?"true":"false";
     } 
 }
