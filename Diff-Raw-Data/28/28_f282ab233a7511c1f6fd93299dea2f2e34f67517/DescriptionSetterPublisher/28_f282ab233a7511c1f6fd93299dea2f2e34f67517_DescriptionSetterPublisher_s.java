 package hudson.plugins.descriptionsetter;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Publisher;
 import hudson.tasks.Recorder;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.ObjectStreamException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 public class DescriptionSetterPublisher extends Recorder {
 
 	private final String regexp;
 	private final String regexpForFailed;
 	private final String description;
 	
 	private final String descriptionForFailed;
 
 	@Deprecated
 	private transient boolean setForFailed = false;
 
 	@Deprecated
 	private transient boolean explicitNotRegexp= false;
 	
 	@DataBoundConstructor
 	public DescriptionSetterPublisher(String regexp, String regexpForFailed, String description, String descriptionForFailed) {
 		this.regexp = regexp;
 		this.regexpForFailed = regexpForFailed;
 		this.description = Util.fixEmptyAndTrim(description);
 		this.descriptionForFailed = Util.fixEmptyAndTrim(descriptionForFailed);
 	}
 
 	public BuildStepMonitor getRequiredMonitorService() {
 		return BuildStepMonitor.NONE;
 	}
 
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
 			BuildListener listener) throws InterruptedException {
 
 		try {
 			Matcher matcher;
 			String result = null;
 			
 			boolean useUnstable = (regexpForFailed != null || descriptionForFailed != null) && build.getResult().isWorseThan(Result.UNSTABLE);
 			
 			matcher = parseLog(build.getLogFile(), useUnstable ? regexpForFailed : regexp);
 			if (matcher != null) {
 				result = getExpandedDescription(matcher, useUnstable ? descriptionForFailed : description);
 				result = build.getEnvironment(listener).expand(result);
 			} else {
 				if (useUnstable) {
 					if (result == null && regexpForFailed == null && descriptionForFailed != null) {
 						result = descriptionForFailed;
 					}
 				} else {
 					if (result == null && regexp == null && description != null) {
 						result = description;
 					}
 				}
 			}
 			
 			if (result == null) {
 				listener.getLogger().println("[description-setter] Could not determine description.");
 				return true;
 			}
 
 			build.addAction(new DescriptionSetterAction(result));
 			listener.getLogger().println("Description set: " + result);
 			build.setDescription(result);
 		} catch (IOException e) {
 			e.printStackTrace(listener.error("error while parsing logs for description-setter"));
 		}
 
 		return true;
 	}
 
 	private Matcher parseLog(File logFile, String regexp) throws IOException,
 			InterruptedException {
 		
 		if (regexp == null) {
 			return null;
 		}
 		
 		// Assume default encoding and text files
 		String line;
 		Pattern pattern = Pattern.compile(regexp);
 		BufferedReader reader = new BufferedReader(new FileReader(logFile));
 		while ((line = reader.readLine()) != null) {
 			Matcher matcher = pattern.matcher(line);
 			if (matcher.find()) {
 				return matcher;
 			}
 		}
 		return null;
 	}
 	
 	private Object readResolve() throws ObjectStreamException {
 		if (explicitNotRegexp) {
 			return new DescriptionSetterPublisher(null, null, regexp, setForFailed ? regexpForFailed : null);
 		} else {
 			return this;
 		}
 	}
 
 	private String getExpandedDescription(Matcher matcher, String description) {
 		String result = description;
 		if (result == null) {
 			if (matcher.groupCount() == 0) {
 				result = "\\0";
 			} else {
 				result = "\\1";
 			}
 		}
 
 		for (int i = matcher.groupCount(); i >= matcher.groupCount(); i--) {
 			result = result.replace("\\" + i, matcher.group(i));
 		}
 		return result;
 	}
 
 	@Extension
 	public static final class DescriptorImpl extends
 			BuildStepDescriptor<Publisher> {
 
 		public DescriptorImpl() {
 			super(DescriptionSetterPublisher.class);
 		}
 
 		@Override
 		public String getDisplayName() {
 			return "Set build description";
 		}
 
 		@Override
 		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
 			return true;
 		}
 
 		@Override
 		public Publisher newInstance(StaplerRequest req, JSONObject formData)
 				throws FormException {
 			return req.bindJSON(DescriptionSetterPublisher.class, formData);
 		}
 	}
 
 	@Override
 	public DescriptorImpl getDescriptor() {
 		return (DescriptorImpl) super.getDescriptor();
 	}
 
 	@Deprecated
 	public boolean isExplicitNotRegexp() {
 		return explicitNotRegexp;
 	}
 
 	public String getRegexp() {
 		return regexp;
 	}
 
 	@Deprecated
 	public boolean isSetForFailed() {
 		return setForFailed;
 	}
 
 	public String getRegexpForFailed() {
 		return regexpForFailed;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public String getDescriptionForFailed() {
 		return descriptionForFailed;
 	}
 
 }
