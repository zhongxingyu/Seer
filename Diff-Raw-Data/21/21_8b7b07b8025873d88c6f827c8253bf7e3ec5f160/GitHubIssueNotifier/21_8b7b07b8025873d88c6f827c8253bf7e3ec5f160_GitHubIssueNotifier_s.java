 package net.guto.jenkins.plugin;
 
 import static java.util.logging.Level.FINE;
 
 import static java.util.logging.Level.INFO;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Notifier;
 import hudson.tasks.Publisher;
 import hudson.tasks.junit.CaseResult;
 import hudson.tasks.test.AbstractTestResultAction;
 import hudson.util.FormValidation;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 public class GitHubIssueNotifier extends Notifier implements Serializable {
 
 	private static final long serialVersionUID = 8429400310427930527L;
 
 	private static final Logger LOGGER = Logger.getLogger(GitHubIssueNotifier.class.getName());
 
 	String projectUrl;
 	String projectOwner;
 	String projectName;
 
 	@DataBoundConstructor
 	public GitHubIssueNotifier(String projectUrl) {
 		if (LOGGER.isLoggable(FINE)) {
 			LOGGER.entering(this.getClass().getName(), "constructor", new Object[] { projectUrl });
 		}
 		this.projectUrl = projectUrl;
 		projectOwner = parseGithubProjectName(projectUrl);
 		projectName = parseGithubUsername(projectUrl);
 	}
 
 	public String getProjectUrl() {
 		return projectUrl;
 	}
 
 	public void setProjectUrl(String projectUrl) {
 		this.projectUrl = projectUrl;
 	}
 
 	public BuildStepMonitor getRequiredMonitorService() {
 		return BuildStepMonitor.NONE;
 	}
 
 	@Override
 	public boolean needsToRunAfterFinalized() {
 		return true;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
 		if (LOGGER.isLoggable(FINE)) {
 			LOGGER.entering(this.getClass().getName(), "perform", new Object[] { build, launcher, listener });
 		}
 		Result result = build.getResult();
 
 		Result prevResult = null;
 		AbstractBuild<?, ?> prevBuild = build.getPreviousBuild();
 		if (prevBuild != null)
 			prevResult = prevBuild.getResult();
 
 		if (result == prevResult) {
 			// LOGGER.
 		}
 
 		AbstractTestResultAction<AbstractTestResultAction> testResultAction = build.getTestResultAction();
 		if (testResultAction != null && testResultAction.getFailCount() > 0) {
 			DescriptorImpl descriptor = (DescriptorImpl) getDescriptor();
 			final String username = descriptor.getUsername();
 			final String password = descriptor.getPassword();
			Issue issue = getIssue(testResultAction.getFailedTests(), username, password, build.getNumber());
 			GitHubIssueHelper.openIssue(issue, username, password);
 		}
 		if (LOGGER.isLoggable(FINE)) {
 			LOGGER.exiting(this.getClass().getName(), "perform");
 		}
 		return true;
 	}
 
 	@Extension
 	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
 	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
 
 		String token;
 		String username;
 		String password;
 
 		DescriptorImpl() {
 			super(GitHubIssueNotifier.class);
 		}
 
 		@Override
 		public String getDisplayName() {
 			return "GitHub Issue";
 		}
 
 		@SuppressWarnings("rawtypes")
 		@Override
 		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
 			return true;
 		}
 
 		@Override
 		public boolean configure(StaplerRequest req, JSONObject json) throws hudson.model.Descriptor.FormException {
 			if (LOGGER.isLoggable(INFO)) {
 				LOGGER.entering(this.getClass().getName(), "configure", new Object[] { req, json });
 			}
 			username = json.getString("username");
 			password = json.getString("password");
 			token = json.getString("token");
 			LOGGER.info("Saving Github token: " + token);
 			save();
 			return super.configure(req, json);
 		}
 
 		FormValidation doProjectUrlCheck(@QueryParameter final String projectUrl) throws IOException {
 			if (isProjectUrlValid(projectUrl)) {
 				return FormValidation.ok();
 			}
 			return FormValidation.error("GitHub project url is invalid");
 
 		}
 
 		public String getUsername() {
 			return username;
 		}
 
 		public String getPassword() {
 			return password;
 		}
 
 		public String getToken() {
 			return token;
 		}
 
 		public FormValidation doTestGithubAuthentication(@QueryParameter String username, @QueryParameter String password) {
 			if (LOGGER.isLoggable(INFO)) {
 				LOGGER.entering(this.getClass().getName(), "doTestGithubAuthentication", new Object[] { username, password });
 			}
 			return FormValidation.ok();
 		}
 	}
 
 	private static final String pathRegex = "([A-Za-z_\\-]+)/([A-Za-z_\\-]+)";
 	private static final String regexHttp = "^https?://github\\.com/" + pathRegex + "$";
 	private static final String regexSsh = "^git@github\\.com:" + pathRegex + "(\\.git)?$";
 	private static final String regexGit = "^git://github\\.com/" + pathRegex + "\\.git$";
 
 	private static Matcher getMatcher(final String projectUrl) {
 		Pattern pattern;
 		if (Pattern.matches(regexHttp, projectUrl)) {
 			pattern = Pattern.compile(regexHttp);
 		} else if (Pattern.matches(regexSsh, projectUrl)) {
 			pattern = Pattern.compile(regexSsh);
 		} else if (Pattern.matches(regexGit, projectUrl)) {
 			pattern = Pattern.compile(regexGit);
 		} else {
 			throw new IllegalArgumentException();
 		}
 		return pattern.matcher(projectUrl);
 	}
 
 	protected static boolean isProjectUrlValid(final String projectUrl) {
 		return Pattern.matches(regexHttp, projectUrl) || Pattern.matches(regexSsh, projectUrl) || Pattern.matches(regexGit, projectUrl);
 	}
 
 	protected static String parseGithubUsername(final String projectUrl) {
 		Matcher matcher = getMatcher(projectUrl);
 		matcher.find();
 		return matcher.group(1);
 
 	}
 
 	protected static String parseGithubProjectName(final String projectUrl) {
 		Matcher matcher = getMatcher(projectUrl);
 		matcher.find();
 		return matcher.group(2);
 	}
 
 	protected static Issue getIssue(final List<CaseResult> caseResults, String projectOwner, String projectName, int buildNumber) {
 		Issue issue = new Issue();
 		issue.owner = projectOwner;
 		issue.project = projectName;
 		int failNum = caseResults.size();
 		StringBuilder title = new StringBuilder("[JENKINS] There was ");
 		title.append(failNum);
 		title.append((failNum == 1) ? "failure" : "failures");
 		title.append(" on build ");
 		title.append(buildNumber);
 		if (failNum == 1 && !caseResults.get(0).getErrorDetails().trim().equals("")) {
 			title.append(": ");
 			title.append(caseResults.get(0).getErrorDetails());
 		}
 		StringBuilder body = new StringBuilder();
 		for (CaseResult fail : caseResults) {
 			body.append("automatic message\n");
 			body.append(fail.getId() + "\n");
 			body.append(fail.getFullName() + "\n");
 			body.append(fail.getDurationString() + "\n");
 			body.append(fail.getErrorDetails() + "\n");
 			body.append(fail.getErrorStackTrace() + "\n");
 		}
 		issue.title = title.toString();
 		issue.body = body.toString();
 		return issue;
 	}
 }
