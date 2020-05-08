 package org.jvnet.hudson.tools.majorminor;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildWrapper;
 import hudson.tasks.BuildWrapperDescriptor;
 import hudson.util.FormValidation;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * 
  * Changes the build name so that it follows a major.minor.revision format. For
  * the first build, it uses the value in the firstBuildName config parameter.
  * For subsequent builds, it replaces the first matching group in the previous
  * build name with the latest revision number from subversion. If no subversion
  * changes were detected, then the previous build number is used causing you to
  * have two builds with the same name.
  * 
  * @author ERICSSON - Nithin Thomas
  * 
  */
 public class MajorMinorBuilder extends BuildWrapper
 {
 
 	private static final String REGEX_NO_MATCH_ERROR = "[MajorMinor Plugin] Previous build name doesn't match Build Name Regex.";
 	private static final String LOG_NOTE = "[MajorMinor Plugin] Build Name Changed to %1s.";
 	public static final String DEFAULT_BUILD_NAME_REGEX = "\\d+\\.\\d+\\.(\\d+)";
 	public static final String DEFAULT_FIRST_BUILD_NAME = "0.0.00";
 	private static final String SVN_REVISION_FIELD = "SVN_REVISION";
 	private static final String BUILD_NAME_FIELD = "BUILD_NAME";
 
 	/**
 	 * Regex to match the previous build name against to look for revision #
 	 */
 	private final String buildNameRegex;
 	/**
 	 * Name to be given to the first build. This will be matched against
 	 * {@link MajorMinorBuilder#buildNameRegex} to replace the revision number
 	 * with most recent
 	 */
 	private final String firstBuildName;
 
 	@DataBoundConstructor
 	public MajorMinorBuilder(String buildNameRegex, String firstBuildName)
 	{
 		if (buildNameRegex == null || buildNameRegex.isEmpty())
 			this.buildNameRegex = DEFAULT_BUILD_NAME_REGEX;
 		else
 		{
 			this.buildNameRegex = buildNameRegex;
 		}
 		if (firstBuildName == null || firstBuildName.isEmpty())
 			this.firstBuildName = DEFAULT_FIRST_BUILD_NAME;
 		else
 			this.firstBuildName = firstBuildName;
 	}
 
 	public String getBuildNameRegex()
 	{
 		return buildNameRegex;
 	}
 
 	public String getFirstBuildName()
 	{
 		return firstBuildName;
 	}
 
 	/**
 	 * This method is called during the build process by hudson for this plugin
 	 * to do its part in setting up the build environment.
 	 * 
 	 * @param build
 	 *            - current build in progress
 	 * @param launcher
 	 * @param listener
 	 * @return
 	 */
 	public Environment setUp(AbstractBuild build, Launcher launcher,
 			BuildListener listener) throws IOException, InterruptedException
 	{
 		/**
 		 * get latest svn revision # included in this build
 		 */
 		String revision;
		revision = build.getEnvironment(listener).get(SVN_REVISION_FIELD);
 
 		// if revision is not set, then this is not an scm based project. so use
 		// the build # instead of
 		// revision #
 		if (revision == null || revision.isEmpty())
 			revision = String.valueOf(build.getNumber());
 
 		String prevBuildName = "";
 
 		// check if this is the first build of this project
 		if (build.getPreviousBuild() == null)
 		{
 			prevBuildName = this.getFirstBuildName();
 		} else
 		{
 			// not the first build
 			// find the previous build name
 			prevBuildName = build.getPreviousBuild().getDisplayName();
 		}
 
 		String nextBuildName = "";
 		Matcher m = Pattern.compile(this.getBuildNameRegex()).matcher(
 				prevBuildName);
 		if (m.matches() && m.group(1) != null)
 		{
 			nextBuildName = prevBuildName.substring(0, m.start(1)) + revision;
 
 			// add any suffix if it exists
 			if (m.end(1) + 1 < prevBuildName.length())
 				nextBuildName += prevBuildName.substring(m.end(1) + 1);
 		} else
 		{
 			// build name doesn't match pattern. error
 			listener.error(this.REGEX_NO_MATCH_ERROR); // also outputs
 														// this message
 														// to output
 			build.setResult(Result.FAILURE);
 		}
 
 		// declare a new final variable newBuildName so that it can be accessed
 		// from inner class
 		final String newBuildName = nextBuildName;
 
 		// set build name (only available since Hudson v1.390)
 		build.setDisplayName(newBuildName);
 
 		// add a note to the log that name was changed
 		listener.getLogger().println(String.format(LOG_NOTE, newBuildName));
 		return new Environment()
 		{
 			@Override
 			public void buildEnvVars(Map<String, String> env)
 			{
 				env.put(BUILD_NAME_FIELD, newBuildName);
 			}
 		};
 	}
 
 	@Override
 	public BuildWrapperDescriptor getDescriptor()
 	{
 		return DESCRIPTOR;
 	}
 
 	@Extension
 	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
 	/**
 	 * Descriptor for {@link MajorMinorBuilder}. Used as a singleton. The class
 	 * is marked as public so that it can be accessed from views.
 	 */
 	public static final class DescriptorImpl extends BuildWrapperDescriptor
 	{
 		public DescriptorImpl()
 		{
 			super(MajorMinorBuilder.class);
 			load();
 		}
 
 		/**
 		 * This human readable name is used in the configuration screen.
 		 */
 		public String getDisplayName()
 		{
 			return "Create a major.minor.revision formatted version number";
 		}
 
 		@Override
 		public boolean configure(StaplerRequest req, JSONObject json)
 				throws FormException
 		{
 			return super.configure(req, json);
 		}
 
 		@Override
 		public boolean isApplicable(AbstractProject<?, ?> proj)
 		{
 			return true;
 		}
 
 		/**
 		 * Performs on-the-fly validation of the form field 'buildNameRegex'.
 		 * 
 		 * @param value
 		 *            This receives the current value of the field.
 		 */
 		public FormValidation doCheckBuildNameRegex(
 				@QueryParameter final String value)
 		{
 			try
 			{
 				Pattern p = Pattern.compile(value);
 				return FormValidation.ok();
 			} catch (PatternSyntaxException e)
 			{
 				return FormValidation
 						.error("The given expression is not a valid pattern. Syntax Error: "
 								+ e.getMessage()
 								+ "Description: "
 								+ e.getDescription()
 								+ "Error index: "
 								+ e.getIndex());
 			}
 		}
 	}
 }
