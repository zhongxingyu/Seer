 package hudson.plugins.yammer;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.model.AbstractBuild;
 import hudson.model.Descriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Publisher;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 
 import net.sf.json.JSONObject;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.client.ClientProtocolException;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 /**
  * <p>
  * When the user configures the project and enables this publisher,
  * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new
  * {@link YammerPublisher} is created. The created instance is persisted to the
  * project configuration XML by using XStream, so this allows you to use
  * instance fields (like {@link #name}) to remember the configuration.
  * 
  * <p>
  * When a build is performed, the
  * {@link #perform(Build, Launcher, BuildListener)} method will be invoked.
  * 
  * @author Russell Hart
  */
 public class YammerPublisher extends Publisher {
 
 	protected static final Log LOGGER = LogFactory.getLog(YammerPublisher.class
 			.getName());
 
 	/**
 	 * The name of the Yammer group to post the build result too.
 	 */
 	private String yammerGroup;
 
 	/**
 	 * The id of the Yammer group to post the build result too.
 	 */
 	private String yammerGroupId;
 
 	/**
 	 * A flag to indicate which build results should be posted to Yammer.
 	 */
 	private BuildResultPostOption buildResultPostOption;
 
 	/**
 	 * People to notify
 	 */
 	private String peopleToNotify;
 
 	/**
 	 * Get's called on saving the project specific config.
 	 * 
 	 * @param yammerGroup
 	 */
 	@SuppressWarnings("deprecation")
 	@DataBoundConstructor
 	public YammerPublisher(String peopleToNotify,
 			String yammerGroup,
 			BuildResultPostOption buildResultPostOption) {
 		this.peopleToNotify = peopleToNotify;
 		this.yammerGroup = yammerGroup;
 		this.buildResultPostOption = buildResultPostOption;
 
 		if (this.yammerGroup != null & !this.yammerGroup.equals("")) {
 			try {
 				this.yammerGroupId = YammerUtils.getGroupId(
 						((DescriptorImpl) getDescriptor()).accessAuthToken,
 						((DescriptorImpl) getDescriptor()).accessAuthSecret,
 						this.yammerGroup,
 						((DescriptorImpl) getDescriptor()).applicationKey,
 						((DescriptorImpl) getDescriptor()).applicationSecret);
 			}
 			catch (Exception e) {
 				LOGGER.error(e.getLocalizedMessage());
 				// throw new RuntimeException(e);
 			}
 		}
 	}
 
 	public String getPeopleToNotify() {
 		return this.peopleToNotify;
 	}
 
 	public String getYammerGroup() {
 		return this.yammerGroup;
 	}
 
 	public BuildResultPostOption getBuildResultPostOption() {
 		return this.buildResultPostOption;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild
 	 * , hudson.Launcher, hudson.model.BuildListener)
 	 */
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
 			BuildListener listener) throws IOException {
 
 		DescriptorImpl descriptor = (DescriptorImpl) getDescriptor();
 		boolean sendMessage = false;
 
 		switch (this.buildResultPostOption) {
 			case ALL:
 				sendMessage = true;
 				break;
 			case SUCCESS:
 				if (build.getResult() == Result.SUCCESS) {
 					sendMessage = true;
 				}
 				break;
 			case FAILURES_ONLY:
 				if (build.getResult() != Result.SUCCESS) {
 					sendMessage = true;
 				}
 				break;
 			case ABORTED_ONLY:
 				if (build.getResult() == Result.ABORTED) {
 					sendMessage = true;
 				}
 				break;
 			case STATUS_CHANGE:
				if (build.getResult() != build.getPreviousBuild().getResult()) {
 					sendMessage = true;
 				}
 				break;
 		}
 
 		// if (!this.postOnlyFailures || (this.postOnlyFailures &&
 		// build.getResult() != Result.SUCCESS)) {
 		if (sendMessage) {
 			YammerUtils.sendMessage(descriptor.accessAuthToken,
 					descriptor.accessAuthSecret,
 					createBuildMessageFromResults(build), this.yammerGroupId,
 					descriptor.applicationKey, descriptor.applicationSecret);
 			listener.getLogger().println("YammerNofier: Send notification to " + this.yammerGroup);
 		}
 
 		return true;
 	}
 
 	/**
 	 * Create a message from the build results.
 	 * 
 	 * @param build
 	 * @return
 	 */
 	private String createBuildMessageFromResults(AbstractBuild<?, ?> build) {
 		String hudsonUrl = ((DescriptorImpl) getDescriptor()).hudsonUrl;
 		String absoluteBuildURL = hudsonUrl.endsWith("/")
 				? hudsonUrl + build.getUrl()
 				: hudsonUrl + "/" + build.getUrl();
 
 		StringBuilder messageBuilder = new StringBuilder();
 		messageBuilder.append("Hudson Build Results - ");
 		messageBuilder.append(build.getFullDisplayName());
 		messageBuilder.append(" ");
 		messageBuilder.append(build.getResult().toString());
 		messageBuilder.append(" ");
 		messageBuilder.append(absoluteBuildURL);
 		messageBuilder.append("\n");
 		messageBuilder.append(peopleToNotifyText());
 
 		return messageBuilder.toString();
 	}
 
 	private String peopleToNotifyText() {
 		if (StringUtils.isEmpty(getPeopleToNotify())) {
 			return null;
 		}
 		String[] ppl = getPeopleToNotify().replaceAll("\\s", "").split(",");
 		if (ppl.length == 0) {
 			return null;
 		}
 
 		StringBuilder sb = new StringBuilder();
 		for (String p : ppl) {
 			sb.append("@").append(p).append(", ");
 		}
 		sb.setLength(sb.length() - 2);
 		return sb.toString();
 	}
 
 	@Extension
 	public static final class DescriptorImpl extends Descriptor<Publisher> {
 
 		/**
 		 * The key of the application registered with Yammer. See
 		 * http://www.yammer.com/client_applications/new
 		 */
 		private String applicationKey;
 
 		/**
 		 * The secret of the application registered with Yammer. See
 		 * http://www.yammer.com/client_applications/new
 		 */
 		private String applicationSecret;
 
 		/**
 		 * The Yammer request auth token used in getting the access token and
 		 * access authentication. See http://www.yammer.com/api_oauth.html
 		 */
 		private String requestAuthToken;
 
 		/**
 		 * The Yammer request auth secret used in getting the access
 		 * authentication. See http://www.yammer.com/api_oauth.html
 		 */
 		private String requestAuthSecret;
 
 		/**
 		 * The Yammer access token used in getting the access authentication.
 		 * See http://www.yammer.com/api_oauth.html
 		 */
 		private String accessToken;
 
 		/**
 		 * The Yammer access auth token, needed for using the Yammer API
 		 */
 		private String accessAuthToken = "";
 
 		/**
 		 * The Yammer access auth secret, needed for using the Yammer API
 		 */
 		private String accessAuthSecret = "";
 
 		/**
 		 * The HTTP address of the Hudson installation, such as
 		 * http://yourhost.yourdomain/hudson/. This value is used to put links
 		 * into messages generated by Hudson.
 		 */
 		private String hudsonUrl;
 
 		@Override
 		public String getDisplayName() {
 			return "Publish results in Yammer";
 		}
 
 		public String accessToken() {
 			return accessToken;
 		}
 
 		public String hudsonUrl() {
 			return hudsonUrl;
 		}
 
 		public String applicationKey() {
 			return applicationKey;
 		}
 
 		public String applicationSecret() {
 			return applicationSecret;
 		}
 
 		public Boolean showAccessToken() {
 			return (this.applicationKey != null && this.applicationSecret != null);
 		}
 
 		public DescriptorImpl() {
 			super(YammerPublisher.class);
 			// Load the saved configuration
 			load();
 		}
 
 		/**
 		 * Gets new oauth request auth parameters from Yammer for this plugin.
 		 * 
 		 * @throws IOException
 		 * @throws ClientProtocolException
 		 */
 		private void initialseRequestAuthParameters()
 				throws ClientProtocolException, IOException {
 			Map<String, String> parametersMap;
 
 			parametersMap = YammerUtils.getRequestTokenParameters(
 					this.applicationKey, this.applicationSecret);
 			this.requestAuthToken = parametersMap.get(YammerUtils.OAUTH_TOKEN);
 			this.requestAuthSecret = parametersMap
 					.get(YammerUtils.OAUTH_SECRET);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * hudson.model.Descriptor#configure(org.kohsuke.stapler.StaplerRequest,
 		 * net.sf.json.JSONObject)
 		 */
 		@Override
 		public boolean configure(StaplerRequest req, JSONObject o)
 				throws FormException {
 			// to persist global configuration information, set that to
 			// properties and call save().
 			// String newAccessToken = o.getString("accessToken");
 			this.hudsonUrl = o.getString("hudsonUrl");
 			this.applicationKey = o.getString("applicationKey");
 			this.applicationSecret = o.getString("applicationSecret");
 			this.accessToken = o.getString("accessToken");
 
 			save();
 			return super.configure(req, o);
 		}
 
 		/**
 		 * Get the access auth parameters from Yammer. See
 		 * http://www.yammer.com/api_oauth.html
 		 * 
 		 * @throws FormException
 		 */
 		private void setAccessAuthParameters() throws FormException {
 
 			Map<String, String> parametersMap;
 			try {
 				parametersMap = YammerUtils.getAccessTokenParameters(
 						this.requestAuthToken, this.requestAuthSecret,
 						this.accessToken, this.applicationKey,
 						this.applicationSecret);
 				this.accessAuthToken = parametersMap
 						.get(YammerUtils.OAUTH_TOKEN);
 				this.accessAuthSecret = parametersMap
 						.get(YammerUtils.OAUTH_SECRET);
 			}
 			catch (Exception e) {
 				throw new FormException(e.getCause(), "accessToken");
 			}
 		}
 
 		public void doGenerateAccessTokenLink(
 				StaplerRequest req,
 				StaplerResponse rsp,
 				@QueryParameter("applicationKey") final String applicationKey,
 				@QueryParameter("applicationSecret") final String applicationSecret)
 				throws IOException, ServletException {
 
 			try {
 				this.applicationKey = applicationKey;
 				this.applicationSecret = applicationSecret;
 				initialseRequestAuthParameters();
 				save();
 				rsp.getWriter()
 						.write("<a href='https://www.yammer.com/oauth/authorize?oauth_token="
 								+ this.requestAuthToken
 								+ "' target='_blank'>Click here to get a new access token</a>");
 			}
 			catch (Exception e) {
 				LOGGER.error(e.getLocalizedMessage());
 				e.printStackTrace();
 			}
 		}
 
 		public void doGetAccessAuthParameters(StaplerRequest req,
 				StaplerResponse rsp,
 				@QueryParameter("accessToken") final String accessToken)
 				throws IOException, ServletException {
 
 			try {
 				this.accessToken = accessToken;
 				setAccessAuthParameters();
 				save();
 				rsp.getWriter().write("Success");
 			}
 			catch (Exception e) {
 				LOGGER.error(e.getLocalizedMessage());
 				e.printStackTrace();
 				rsp.getWriter().write("Failed: " + e.getLocalizedMessage());
 			}
 		}
 
 	}
 
 	public enum BuildResultPostOption {
 		ALL("Post all results"),
 		SUCCESS("Post successes only"),
 		FAILURES_ONLY("Post failures only"),
 		ABORTED_ONLY("Post aborts only"),
 		STATUS_CHANGE("Post on status change");
 
 		private final String description;
 
 		BuildResultPostOption(String description) {
 			this.description = description;
 		}
 
 		public String description() {
 			return this.description;
 		}
 	}
 
 	@Override
 	public BuildStepMonitor getRequiredMonitorService() {
 		return BuildStepMonitor.BUILD;
 	}
 }
