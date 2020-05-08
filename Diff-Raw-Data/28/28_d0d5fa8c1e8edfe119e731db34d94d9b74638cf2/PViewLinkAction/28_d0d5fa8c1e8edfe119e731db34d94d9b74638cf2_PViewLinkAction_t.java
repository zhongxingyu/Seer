 package de.fspengler.hudson.pview;
 
 import hudson.model.AbstractProject;
 import hudson.model.Action;
 import hudson.model.Hudson;
 import hudson.model.Job;
 import hudson.model.RSS;
 import hudson.model.Run;
 import hudson.model.TopLevelItem;
 import hudson.model.User;
 import hudson.security.ACL;
 import hudson.security.AccessControlled;
 import hudson.security.Permission;
 import hudson.util.RunList;
 import hudson.views.BuildButtonColumn;
 import hudson.views.JobColumn;
 import hudson.views.LastDurationColumn;
 import hudson.views.LastFailureColumn;
 import hudson.views.LastStableColumn;
 import hudson.views.LastSuccessColumn;
 import hudson.views.ListViewColumn;
 import hudson.views.StatusColumn;
 import hudson.views.WeatherColumn;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletException;
 
 import org.kohsuke.stapler.Stapler;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.kohsuke.stapler.export.ExportedBean;
 
 /**
  * Link for call the personal view
  * 
  * @author Tom Spengler
  */
 @ExportedBean
 public class PViewLinkAction implements Action, AccessControlled {
 
 	private static final String URL_PART_SI_VIEW = "siView";
 	private static final String URL_PART_ROOT_SI_VIEW = "rootSiView";
 	private static final String URL_PVIEW_ROOT_SI_VIEW = "/pview/" + URL_PART_ROOT_SI_VIEW ;
 	private static final String URL_PVIEW_SI_VIEW = "/pview/" + URL_PART_SI_VIEW;
 	private static final long serialVersionUID = 1L;
 	
 
 	
 	public String getDisplayName() {
 		StaplerRequest req = Stapler.getCurrentRequest();
         if(req!=null && req.getOriginalRequestURI().contains("/view")){
         	return null;
         }
         if (req.getAttribute("rootisSet") != null){
         	if (req.getAttribute("rootProject") != null){
         		 return "Tree View: " + req.getAttribute("rootProject") ;	
         	}
            return "Tree View";	
         }
 
 		if (User.current() != null) {
 			req.setAttribute("rootisSet",Boolean.TRUE);
 			return "Personal View";
 		} else {
 			return "Anonymous View";
 		}
 	}
 
 	public String getUrlName() {
 		StaplerRequest req = Stapler.getCurrentRequest();
         if(req!=null && req.getOriginalRequestURI().contains("/view")){
         	return null;
         }
         return "pview";
 	}
 
 	public String getIconFileName() {
 		StaplerRequest req = Stapler.getCurrentRequest();
         if(req!=null && req.getOriginalRequestURI().contains("/view")){
         	return null;
         }
 		return "up.gif";
 	}
 
 	private boolean isIsTreePosition (int position, boolean pDefault){
		if (!isIsTree()){
			return false;
		}
		if (getUser() && getUserProp() != null )
			return ((getUserProp().getTreePosition() == position)); 
 		else
 			return pDefault;
 	}
 	public boolean isIsTreeNE() {
 		return isIsTreePosition(0, true);
 	}
 	
 	public boolean isIsTreeSE() {
 		return isIsTreePosition(1, false);
 	}
 	
 	public boolean isIsTreeNW() {
 		return isIsTreePosition(2, false);
 	}
 	
 	public boolean isIsTreeSW() {
 		return isIsTreePosition(3, false);
 	}	
 	public boolean isIsTree() {
 		StaplerRequest req = Stapler.getCurrentRequest();
         if(req!=null && 
         		(req.getOriginalRequestURI().startsWith(URL_PVIEW_SI_VIEW) || req.getOriginalRequestURI().startsWith(URL_PVIEW_ROOT_SI_VIEW) )){
         	return true;
         }
 		return false;
 	}
 	public boolean isIsList() {
 		return !isIsTree();
 	}
 	
 	public String getUserLogin(){
 		if (getUser() )
 			return User.current().getUrl();
 		else
 			return "";
 	} 
 	
 	public boolean isHasUp () {
 		if (isIsTree()){
 			return getProjectMatcher().length() > 0;
 		}
 		return false;
 	}
 	public String getTreeParent(){
 		String result;
 		if (isIsTree()){
 			String matcher = getProjectMatcher();
			String splitChar = "-";
			if (getUserProp() != null ) {		
				splitChar = getUserProp().getTreeSplitChar();
			} 
			if (matcher.indexOf(splitChar) > -1){
				result = URL_PART_SI_VIEW + "/" + matcher.substring(0, matcher.lastIndexOf(splitChar)) + "/";
 			} else {
 				result = URL_PART_ROOT_SI_VIEW;
 			}
 		} else {
 			result = "";
 		}
 		return result;
 	}
 	
 	public Collection<DirEntry> getSubDirs(){
		String splitChar = "-";
		if (getUserProp() != null ) {		
			splitChar = getUserProp().getTreeSplitChar();
		} 
 		List<AbstractProject> iList = Hudson.getInstance().getItems(AbstractProject.class);
 		SortedMap<String,DirEntry> dirSet = new TreeMap<String,DirEntry>();
 		String startMatcher = getProjectMatcher();
 		int lenMatcher = startMatcher.length();
 		if (lenMatcher > 0) {
 			for (AbstractProject abstractProject : iList) {
 				if  (abstractProject.getName().startsWith(startMatcher)) {
 					int posOfMatcher = abstractProject.getName().indexOf(splitChar, lenMatcher + 1);
 					if (posOfMatcher > -1) {
 						String abString = abstractProject.getName().substring(lenMatcher+1, posOfMatcher);
 						if (dirSet.containsKey(abString)){
 							dirSet.get(abString).addOne();
 						} else {
 							DirEntry de =new DirEntry(abString, startMatcher + splitChar + abString );
 							dirSet.put(abString, de);
 						}
 					}
 
 				}
 			}
 		} else {
 			for (AbstractProject abstractProject : iList) {
 				int posOfMatcher = abstractProject.getName().indexOf(splitChar);
 				if (posOfMatcher > -1) {
 					String abString = abstractProject.getName().substring(0, posOfMatcher);
 					if (dirSet.containsKey(abString)){
 						dirSet.get(abString).addOne();
 					} else {
 						DirEntry de =new DirEntry(abString, abString );
 						dirSet.put(abString, de);
 					}
 				}
 			}
 		}
 		return dirSet.values();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<AbstractProject> getJobs() {
 		List<AbstractProject> jobList = new ArrayList<AbstractProject>();
 		List<AbstractProject> iList = Hudson.getInstance().getItems(AbstractProject.class);
	   String splitChar = "-";
		if (getUserProp() != null) {
			splitChar = getUserProp().getTreeSplitChar();
		} 
 		
 		if (isIsTree()){
 			StaplerRequest req = Stapler.getCurrentRequest();
 			if (req.getOriginalRequestURI().startsWith(URL_PVIEW_ROOT_SI_VIEW)){
 				for (AbstractProject abstractProject : iList) {
 					if  (!abstractProject.getName().contains(splitChar)){
 						jobList.add(abstractProject);
 					}
 				}	
 			} else {
 				String startMatcher = getProjectMatcher();
 				int lenMatcher = startMatcher.length();
 				for (AbstractProject abstractProject : iList) {
 					if  (abstractProject.getName().startsWith(startMatcher) && abstractProject.getName().indexOf(splitChar, lenMatcher + 1)  == -1){
 						jobList.add(abstractProject);
 					}
 				}	
 			}
 		} else {
 			Pattern pat = getUserPattern();
 		
 			for (AbstractProject abstractProject : iList) {
 				if  (pat.matcher(abstractProject.getName()).matches()){
 					jobList.add(abstractProject);
 				}
 			}
 		}
 		return jobList;
 	}
 
 	public String getProjectMatcher() {
 		StaplerRequest req = Stapler.getCurrentRequest();
 		if ( ! req.getOriginalRequestURI().startsWith(URL_PVIEW_SI_VIEW + "/")){
 			return "";
 		}
 		String matchPoint = req.getOriginalRequestURI().substring((URL_PVIEW_SI_VIEW + "/").length(),req.getOriginalRequestURI().length() -1);
 		return matchPoint;
 	}
 
 	private Pattern getUserPattern() {
 		UserPersonalViewProperty up = getUserProp();
 		Pattern pat;
 		if (up != null){
 		 pat = Pattern.compile(getUserProp().getPViewExpression());
 		} else {
 			pat = Pattern.compile(".*");
 		}
 		return pat;
 	}
 	
 	public boolean getShowQueue(){
 		if (getUserProp() == null){
 			return true;
 		} else {
 		return !getUserProp().isPViewBuildNoQueue();
 		}
 	}
 	public boolean getShowExecutor(){
 		if (getUserProp() == null){
 			return true;
 		} else {
 				return !getUserProp().isPViewBuildNoExecutor();
 		}
 	}
 	private UserPersonalViewProperty getUserProp(){
 		if (User.current() != null)
 			return User.current().getProperty(UserPersonalViewProperty.class);
 		else
 			return null;
 		
 	}
 	public boolean getUser(){
 		return (User.current() != null);
 	}
 
 	public ACL getACL() {
 		return Hudson.getInstance().getACL();
 	}
 
 	public void checkPermission(Permission p) {
 		getACL().checkPermission(p);
 	}
 
 	public boolean hasPermission(Permission p) {
 		return (User.current() != null);
 
 	}
 
 
     public void doRssAll( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
         rss(req, rsp, " all builds", getBuilds());
     }
 
     public void doRssFailed( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
         rss(req, rsp, " failed builds", getBuilds().failureOnly());
     }
     
     public RunList getBuilds() {
         return new RunList(getJobs());
     }
 
     private void rss(StaplerRequest req, StaplerResponse rsp, String suffix, RunList runs) throws IOException, ServletException {
         RSS.forwardToRss(getDisplayName()+ suffix, getUrlName(),
             runs.newBuilds(), Run.FEED_ADAPTER, req, rsp );
     }
 
     public void doRssLatest( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
         List<Run<?,?>> lastBuilds = new ArrayList<Run<?,?>>();
         Pattern pat = getUserPattern();
         for (TopLevelItem item : Hudson.getInstance().getItems()) {
             if (item instanceof Job) {
                 Job<?,?> job = (Job<?,?>) item;
                 if (pat.matcher(job.getName()).matches()){
                 	Run<?,?> lb = job.getLastBuild();
                 	if(lb!=null)    lastBuilds.add(lb);
                 }
             }
         }
         RSS.forwardToRss(getDisplayName()+" last builds only", getUrlName(),
             lastBuilds, Run.FEED_ADAPTER_LATEST, req, rsp );
     }
     
     /**
      * Alias for {@link #getItem(String)}. This is the one used in the URL binding.
      */
     public final TopLevelItem getJob(String name) {
     	return Hudson.getInstance().getItem(name);
     }
     
     public List<ListViewColumn> getColumns (){
     	ArrayList<ListViewColumn> rlist = new ArrayList<ListViewColumn>();
     	UserPersonalViewProperty up = getUserProp() ;
     	if (up == null) {
     	rlist.add(new StatusColumn());
     	rlist.add(new WeatherColumn());
     	rlist.add(new JobColumn());
     	rlist.add(new LastSuccessColumn());
     	rlist.add(new LastFailureColumn());
     	rlist.add(new LastStableColumn());
     	rlist.add(new LastDurationColumn());
     	rlist.add(new ConsoleViewColumn());
     	rlist.add(new BuildButtonColumn());
     	} else {
     		if (up.isPcStatus())
     	    	rlist.add(new StatusColumn());
     		if (up.isPcWeather())
     			rlist.add(new WeatherColumn());
     		if (up.isPcJob())
     			rlist.add(new JobColumn());
     		if (up.isPcLastSuccess())
     			rlist.add(new LastSuccessColumn());
     		if (up.isPcLastFailure())
     			rlist.add(new LastFailureColumn());
     		if (up.isPcLastStable())
     			rlist.add(new LastStableColumn());
     		if (up.isPcLastDuration())
     			rlist.add(new LastDurationColumn());
     		if (up.isPcConsoleView())
     			rlist.add(new ConsoleViewColumn());
     		if (up.isPcBuildButton())
     			rlist.add(new BuildButtonColumn());
     	}
     	return rlist;
     }
     
     /**
      * Alias for {@link #getItem(String)}. This is the one used in the URL binding.
      */
     public final PViewLinkAction getSiView(String rootProject) throws CloneNotSupportedException {
     	StaplerRequest req = Stapler.getCurrentRequest();
     	req.setAttribute("rootProject",rootProject);
     	return  this;
     }
     
     /**
      * Alias for {@link #getItem(String)}. This is the one used in the URL binding.
      * @throws CloneNotSupportedException 
      */
     public final PViewLinkAction getRootSiView() throws CloneNotSupportedException {
     	StaplerRequest req = Stapler.getCurrentRequest();
     	req.removeAttribute("rootProject");
     	return  this;
     }
 
 	
 }
