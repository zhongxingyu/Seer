 package org.jenkinsci.plugins.nopmdcheck.verifytrac;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Descriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Publisher;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.sf.json.JSONObject;
 
 import org.jenkinsci.plugins.nopmdcheck.NopmdCheckResultAction;
 import org.jenkinsci.plugins.nopmdcheck.model.CheckResult;
 import org.jenkinsci.plugins.nopmdcheck.model.LineHolder;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 public class VerifyTracPublisher extends Publisher {
 
 	private String ticketPattern;
 
 	private String tracUrl;
 
 	private String user;
 
 	private String password;
 
 	public String getTicketPattern() {
 		return ticketPattern;
 	}
 
 	public void setTicketPattern(String ticketPattern) {
 		this.ticketPattern = ticketPattern;
 	}
 
 	public String getTracUrl() {
 		return tracUrl;
 	}
 
 	public void setTracUrl(String tracUrl) {
 		this.tracUrl = tracUrl;
 	}
 
 	public String getUser() {
 		return user;
 	}
 
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	@DataBoundConstructor
 	public VerifyTracPublisher(String ticketPattern, String tracUrl, String user, String password) {
 		this.ticketPattern = ticketPattern;
 		this.tracUrl = tracUrl.replaceAll("/\\s*$", "");
 		this.user = user;
 		this.password = password;
 	}
 
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
 			throws InterruptedException {
 		//
 		NopmdCheckResultAction action = build.getAction(NopmdCheckResultAction.class);
 
 		if (action == null) {
 			listener.getLogger().println("cannot find nopmd-check-plugin result.");
 			return false;
 		}
 		List<CheckResult> resultList = action.getResultList();
 
 		Set<Integer> idSet = new TracClient(this.tracUrl, this.user, this.password).getClosedSet();
 
 		Map<String, Integer> typeMap = new HashMap<String, Integer>();
 		Pattern p = Pattern.compile(this.ticketPattern);
 
 		for (CheckResult result : resultList) {
 			for (LineHolder line : result.getLineHolders()) {
 				Matcher m = p.matcher(line.getComment());
 				if (m.find() && m.groupCount() > 0) {
 					Integer ticketId = Integer.parseInt(m.group(1));
 					if (idSet.contains(ticketId)) {
 						// TODO replace OK code
 						// listener.getLogger().println(ticketId + ", OK!");
 						typeMap.put(line.getHashcode(), VerifyTracAction.TYPE_OK);
 					} else {
 						typeMap.put(line.getHashcode(), VerifyTracAction.TYPE_NOT_CLOSE);
 					}
 				} else {
 					typeMap.put(line.getHashcode(), VerifyTracAction.TYPE_NO_TICKET);
 				}
 			}
 		}
 
 		VerifyTracAction verifyTracAction;
 		verifyTracAction = new VerifyTracAction(build);
 		verifyTracAction.setTypeMap(typeMap);
 		
 		SvnInfo svnInfo = fetchSvnInfo(build);
 		
 		verifyTracAction.setRevision(svnInfo.revision);
 		verifyTracAction.setSvnPath(svnInfo.rootPath);
 		verifyTracAction.setTracUrl(tracUrl);
 		listener.getLogger().println(verifyTracAction.getRevision());
 		build.addAction(verifyTracAction);
 
 		return true;
 	}
 
	/**
	 * fetch SVN url and revision from revision.txt.
	 * @param build
	 * @return
	 */
 	private SvnInfo fetchSvnInfo(AbstractBuild<?, ?> build) {
 
 		SvnInfo svnInfo = new SvnInfo();
 		String rev = "";
 		String svnUrl = "";
 		String prjName = "";
 
 		// "http://com.example/svn/trunk/fooo/100" in revision.txt.
 		// The last path fragment is revision.
 		File f = new File(build.getRootDir(), "revision.txt");
 		if (f.exists()) {
 			try {
 				BufferedReader br = new BufferedReader(new FileReader(f));
 				svnUrl = br.readLine();
 				// System.out.println(svnUrl);
 				Pattern pRev = Pattern.compile("\\d+\\s*$");
 				Matcher m = pRev.matcher(svnUrl);
 
 				if (m.find()) {
 					// System.out.println("match!");
 					rev = m.group().trim();
 					svnInfo.revision = rev;
 				}
 
 				// If tracUrl is "http://com.example/trac/hogehoge", prjName is "hogehoge".
 				Pattern pPrj = Pattern.compile("[^/]+$");
 				Matcher mPrj = pPrj.matcher(tracUrl);
 				if (mPrj.find()) {
 					prjName = mPrj.group();
 //					System.out.println(prjName);
 				}
 
 				// "http://com.example/svn/hogehoge/trunk/fooo/100" -> "trunk/fooo"
 				svnInfo.rootPath = svnUrl.replaceAll("/" + rev, "").replaceFirst(".*" + prjName + "/", "");
				br.close();
 //				System.out.println(svnInfo.rootPath + ',' + svnInfo.revision);
 
 			} catch (IOException e) {
			}finally{
 			}
 		}
 		return svnInfo;
 
 	}
 
 	public BuildStepMonitor getRequiredMonitorService() {
 		return BuildStepMonitor.NONE;
 	}
 
 	@Override
 	public DescriptorImpl getDescriptor() {
 		return (DescriptorImpl) super.getDescriptor();
 	}
 
 	@Extension
 	public static final class DescriptorImpl extends Descriptor<Publisher> {
 
 		public boolean isApplicable(Class<? extends AbstractProject<?, ?>> aClass) {
 			return true;
 		}
 
 		public DescriptorImpl() {
 			super(VerifyTracPublisher.class);
 		}
 
 		@Override
 		public String getDisplayName() {
 			return "verify NOPMD correspoing ticket";
 		}
 
 		@Override
 		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
 			// To persist global configuration information,
 			// set that to properties and call save().
 			// ^Can also use req.bindJSON(this, formData);
 			// (easier when there are many fields; need set* methods for this,
 			// like setUseFrench)
 			save();
 			return super.configure(req, formData);
 		}
 
 	}
 
 	class SvnInfo {
 		String revision = "";
 		String rootPath = "";
 	}
 }
