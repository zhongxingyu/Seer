 package hudson.plugins.svn_release_mgr;
 
 import hudson.Util;
 import hudson.model.AbstractProject;
 import hudson.model.Cause;
 import hudson.model.CauseAction;
 import hudson.model.Hudson;
 import hudson.model.ParameterValue;
 import hudson.model.ParametersAction;
 import hudson.model.ProminentProjectAction;
 import hudson.model.Run;
 import hudson.model.StringParameterValue;
 import hudson.plugins.svn_release_mgr.model.Revision;
 import hudson.scm.SubversionReleaseSCM;
 import hudson.scm.SubversionReleaseSCM.ModuleLocation;
 import hudson.util.FormFieldValidator;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNLogEntry;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.auth.ISVNAuthenticationProvider;
 import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
 import org.tmatesoft.svn.core.io.SVNRepository;
 import org.tmatesoft.svn.core.wc.SVNClientManager;
 
 public class ProjectReleaseAction implements ProminentProjectAction {
 
 	private AbstractProject<?, ?> owner;
 	private JobPropertyImpl property;
 	private String[] compare;
 	private String revision;
 
 	private static final Logger LOGGER = Logger.getLogger(ProjectReleaseAction.class.getName());
 
 	public ProjectReleaseAction(AbstractProject<?, ?> owner, JobPropertyImpl property) {
 		this.owner = owner;
 		this.property = property;
	}

	private ISVNAuthenticationProvider getAuthProvider() {
		return getSubversion().getDescriptor().createAuthenticationProvider();
	}

	private ModuleLocation[] getLocations() {
		return getSubversion().getLocations();
 	}
 
 	public Collection<Revision> getRevisions() {
 		Collection revisions = new ArrayList();
 		int i = 1;
 		int maxRevisions = Integer.parseInt(property.maxRevisions);
 		for (Revision r:getRevisions(0, -1)) {
 			revisions.add(r);
 			i++;
 			if (i > maxRevisions) break;
 		}
 		
 		return revisions;
 	}
 	
 	public Collection<Revision> getCompareRevisions() {
 		return getRevisions(Long.parseLong(compare[0]), Long.parseLong(compare[1]));
 	}
 
 	public Collection<Revision> getRevisions(long start, long end) {
 		DAVRepositoryFactory.setup();
 		SortedMap<Long, Revision> revisions = new TreeMap<Long, Revision>(Collections.reverseOrder());
		for (ModuleLocation l : getLocations()) {
 			SVNURL svnUrl;
 			SVNRepository repository;
 			try {
 				svnUrl = l.getSVNURL();
 				repository = createSvnClientManager().createRepository(svnUrl, true);
 
 				Collection logEntries = null;
 
 				logEntries = repository.log(new String[] { "" }, null, start, end, true, false);
 				for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
 					SVNLogEntry logEntry = (SVNLogEntry) entries.next();
 					revisions.put(logEntry.getRevision(), new Revision(logEntry));
 					// -------------DEBUG OUTPUT---------------
 					LOGGER.fine("---------------------------------------------");
 					LOGGER.fine("revision: " + logEntry.getRevision());
 					LOGGER.fine("author: " + logEntry.getAuthor());
 					LOGGER.fine("date: " + logEntry.getDate());
 					LOGGER.fine("log message: " + logEntry.getMessage());
 					LOGGER.fine("---------------------------------------------");
 				}
 			} catch (SVNException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		for (Run r : owner.getBuilds().toArray(new Run[0])) {
 			Map<String, String> env = r.getEnvVars();
 			LOGGER.fine("---------ENV VARS FOR RUN:" + r.number);
 			for (String key : env.keySet()) {
 				LOGGER.fine(key + " = " + env.get(key));
 			}
 			Long rev = Revision.getRevisionNumber(r);
 			if (rev == null) continue;
 			Revision revision = revisions.get(rev);
 			if (revision != null ) {
 				//Add the link to the build as well
 				if (r.getActions(ProjectReleaseAction.class).size() < 1) r.addAction(this);
 				revision.addBuild(r);
 			}
 		}
 		
 		return revisions.values();
 	}
 
 	public String getUrlName() {
 		return "releases";
 	}
 
 	public String getDisplayName() {
 		return "Releases";
 	}
 
 	public String getIconFileName() {
 		return "clipboard.gif";
 	}
 
 	public AbstractProject<?, ?> getOwner() {
 		return owner;
 	}
 
 	public void setOwner(AbstractProject<?, ?> owner) {
 		this.owner = owner;
 	}
 
 	public JobPropertyImpl getProperty() {
 		return property;
 	}
 
 	public void setProperty(JobPropertyImpl property) {
 		this.property = property;
 	}
 
 	public SubversionReleaseSCM getSubversion() {
 		return (SubversionReleaseSCM)owner.getScm();
 	}
 
 	public SVNClientManager createSvnClientManager() {
		return getSubversion().createSvnClientManager(getAuthProvider());
 	}
 
 	public void doCompare(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException {
 		//TODO Validator needed for two values
 		setCompare(req.getParameterValues("compare"));
 		req.getView(this, "compare").forward(req, rsp);
 	}
 	
 	public void doBuild(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException {
 		req.bindParameters(this);
         List<ParameterValue> values = new ArrayList<ParameterValue>();
         values.add(new StringParameterValue("REVISION", revision));
     	Hudson.getInstance().getQueue().add(
     			owner, 0, new ParametersAction(values), new CauseAction(new Cause.UserCause()));
         rsp.forwardToPreviousPage(req);
 	}
 
 	public String[] getCompare() {
 		return compare;
 	}
 
 	public void setCompare(String[] compare) {
 		this.compare = compare;
 	}
 
 	public String getRevision() {
 		return revision;
 	}
 
 	public void setRevision(String revision) {
 		this.revision = revision;
 	}
 
 }
