 package hudson.plugins.bugzilla;
 
 import hudson.Util;
 import hudson.model.AbstractProject;
 import hudson.model.Job;
 import hudson.model.JobProperty;
 import hudson.model.JobPropertyDescriptor;
 import hudson.util.FormFieldValidator;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import javax.servlet.ServletException;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 public class BugzillaProjectProperty extends JobProperty<AbstractProject<?,?>> {
 
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     public static final class DescriptorImpl extends JobPropertyDescriptor {
     	private BugzillaSession bugzillaSession;
     	private String regex;
     	private boolean useTooltips;
     	
         public DescriptorImpl() {
             super(BugzillaProjectProperty.class);
             load();
         }
 
         public boolean isApplicable(Class<? extends Job> jobType) {
         	return false;
         }
 
         public String getDisplayName() {
         	return "Bugzilla";
         }
         
         public JobProperty<?> newInstance(StaplerRequest req) throws FormException {
         	return new BugzillaProjectProperty();
         }
 
         public boolean configure(StaplerRequest req) {
             try {
 				regex = req.getParameter("bugzilla.regex");
             	if(req.getParameter("bugzilla.usetooltips")==null) {
             		useTooltips = false;
             		bugzillaSession = new BugzillaSession(req.getParameter("bugzilla.base"));
             	} else {
             		useTooltips = true;
 					bugzillaSession = new BugzillaSession(
 							req.getParameter("bugzilla.base"),
 							req.getParameter("bugzilla.username"),
 							req.getParameter("bugzilla.password")
 					);
             	}
 			} catch (MalformedURLException e) {
 			} catch (XmlRpcException e) {
 			}
             save();
             return true;
         }
 
         public String getBaseUrl() {
             if(bugzillaSession==null) return "http://bugzilla";
             return bugzillaSession.getUrl();
         }
 
         public String getUsername() {
         	if(bugzillaSession == null) return "";
         	return bugzillaSession.getUsername();
         }
         
         public String getPassword() {
         	if(bugzillaSession == null) return "";
         	return bugzillaSession.getPassword();
         }
         
         public boolean getUseToolTips() {
         	return useTooltips;
         }
         
         public String getRegex() {
         	if(regex == null) return "\\b[0-9.]*[0-9]\\b";
         	return regex;
         }
         
         public BugzillaSession getBugzillaSession() {
         	return bugzillaSession;
         }
         
         /**
          * Checks if the Bugzilla URL is accessible and exists.
          */
         public void doRegexCheck(final StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
             // this can be used to check existence of any file in any URL, so admin only
         	new FormFieldValidator(req,rsp,false) {
                 protected void check() throws IOException, ServletException {
                     String regex = Util.fixEmpty(request.getParameter("value"));
                     if(regex==null) {
                         error("No Bug ID regex");
                         return;
                     }
                     try {
                     	Pattern.compile(regex);
                 		ok();
             	        return;
             		} catch (PatternSyntaxException e) {
             			error("Pattern cannot be compiled");
             			return;
             		}
                 }
             }.process();
         }
 
         /**
          * Checks if the Bugzilla URL is accessible and exists.
          */
         public void doUrlCheck(final StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
             // this can be used to check existence of any file in any URL, so admin only
             new FormFieldValidator.URLCheck(req,rsp) {
                 protected void check() throws IOException, ServletException {
                     String url = Util.fixEmpty(request.getParameter("value"));
                     if(url==null) {
                         error("No bugzilla base URL");
                         return;
                     }
                     try {
                    	new BugzillaSession(url);
                 		ok();
             	        return;
             		} catch (MalformedURLException e) {
             			error("Not a valid URL");
             			return;
             		} catch (XmlRpcException e) {
             			error("Error contacting bugzilla XMLRPC at this URL - tooltips may not work");
             			return;
             		} 
                 }
             }.process();
         }
 
         /**
          * Checks if the user name and password are valid.
          */
         public void doLoginCheck(final StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
             new FormFieldValidator(req,rsp,false) {
                 protected void check() throws IOException, ServletException {
                     String url = Util.fixEmpty(request.getParameter("url"));
                     if(url==null) {// URL not entered yet
                         ok();
                         return;
                     }
                     BugzillaSession bsess = null;
 					try {
 						bsess = new BugzillaSession(url,
 								request.getParameter("user"),
 								request.getParameter("pass")
 						);
 					} catch (XmlRpcException e) {
 						// no error report needed, since it would duplicate the error from checkUrl
 						ok();
 						return;
 					}
                     if(bsess.login()) ok();
                     else error("Invalid username/password");
                 }
             }.process();
         }
 
         public void save() {
             super.save();
         }
     }
 
 	@Override
 	public JobPropertyDescriptor getDescriptor() {
 		return DESCRIPTOR;
 	}
 
 }
