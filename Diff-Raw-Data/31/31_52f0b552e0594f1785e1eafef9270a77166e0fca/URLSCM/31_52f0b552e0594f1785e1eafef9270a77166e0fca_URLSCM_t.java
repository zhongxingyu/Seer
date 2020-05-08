 package hudson.plugins.URLSCM;
 
 import static hudson.Util.fixEmpty;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Run;
 import hudson.model.TaskListener;
 import hudson.scm.ChangeLogParser;
 import hudson.scm.SCM;
 import hudson.scm.SCMDescriptor;
 import hudson.util.FormFieldValidator;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.servlet.ServletException;
 
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 public class URLSCM extends hudson.scm.SCM {
 	private final ArrayList<URLTuple> urls = new ArrayList<URLTuple>();
 	private final boolean clearWorkspace;
 	
 	public URLSCM(String[] u, boolean clear) {
 		for(int i = 0; i < u.length; i++) {
 			urls.add(new URLTuple(u[i]));
 		}
 		this.clearWorkspace = clear;
 	}
 	
 	public URLTuple[] getUrls() {
 		return urls.toArray(new URLTuple[urls.size()]);
 	}
 	
 	public boolean isClearWorkspace() {
 		return clearWorkspace;
 	}
 	
 	@Override
 	public boolean checkout(AbstractBuild build, Launcher launcher,
 			FilePath workspace, BuildListener listener, File changelogFile)
 			throws IOException, InterruptedException {
 		if(clearWorkspace) {
 			workspace.deleteContents();
 		}
 		
 		URLDateAction dates = new URLDateAction();
 		
 		for(URLTuple tuple : urls) {
 			String urlString = tuple.getUrl();
 			InputStream is = null;
 			OutputStream os = null;
 			try {
 				URL url = new URL(urlString);
 				URLConnection conn = url.openConnection();
 				dates.setLastModified(urlString, conn.getLastModified());
 				is = conn.getInputStream();
 				String path = new File(url.getPath()).getName();
 				listener.getLogger().append("Copying " + urlString + " to " + path + "\n");
 				os = workspace.child(path).write();
 				byte[] buf = new byte[8192];
 				int i = 0;
 				while ((i = is.read(buf)) != -1) {
 					os.write(buf, 0, i);
 				}
 			} 
 			catch (Exception e) {
 				listener.error("Unable to copy " + urlString + "\n" + e.getMessage());
 				return false;
 			}
 			finally {
 				if (is != null) is.close();
 				if (os != null) os.close();
 			}
 			build.addAction(dates);
 		}
 		return true;
 	}
 
 	@Override
 	public ChangeLogParser createChangeLogParser() {
 		return null;
 	}
 
 	@Override
 	public SCMDescriptor<?> getDescriptor() {
 		return DescriptorImpl.DESCRIPTOR;
 	}
 
 	@Override
 	public boolean pollChanges(AbstractProject project, Launcher launcher,
 			FilePath workspace, TaskListener listener) throws IOException,
 			InterruptedException {
 		boolean change = false;
 		Run lastBuild = project.getLastBuild();
 		if(lastBuild == null) return true;
 		URLDateAction dates = lastBuild.getAction(URLDateAction.class);
 		if(dates == null) return true;
 		
 		for(URLTuple tuple : urls) {
 			String urlString = tuple.getUrl();
 			try {
 				URL url = new URL(urlString);
 				URLConnection conn = url.openConnection();
 				
 				long lastMod = conn.getLastModified();
 				if(dates.getLastModified(urlString) != lastMod) {
 					listener.getLogger().println("Found change: " + urlString + " last modified " + new Date(conn.getLastModified()));
 					change = true;
 				}
 			} 
 			catch (Exception e) {
 				listener.error("Unable to check " + urlString + "\n" + e.getMessage());
 			} 
 		}
 		return change;
 	}
 
 	public static final class URLTuple {
 		private String urlString;
 		public URLTuple(String s) {
 			urlString = s;
 		}
 		
 		public String getUrl() {
 			return urlString;
 		}
 	}
 	
 	public static final class DescriptorImpl extends SCMDescriptor<URLSCM> {
         public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
         private  DescriptorImpl() {
             super(URLSCM.class, null);
             load();
         }
 
         public String getDisplayName() {
             return "URL Copy";
         }
 
         public SCM newInstance(StaplerRequest req) throws FormException {
             return new URLSCM(req.getParameterValues("URL.url"), req.getParameter("URL.clear") != null);
         }
 
         public boolean configure(StaplerRequest req) throws FormException {
             return true;
         }
         
         public void doUrlCheck(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
             new FormFieldValidator.URLCheck(req,rsp) {
     			@Override
     			protected void check() throws IOException, ServletException {
     				String url = fixEmpty(request.getParameter("value"));
     				try {
     					open(new URL(url));
     				} catch (Exception e) {
     					error("Cannot open " + url);
     					return;
     				}
     				ok();
     			}
             }.process();
         }
     }
 
 }
