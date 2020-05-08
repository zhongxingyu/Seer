 package hudson.plugins.buckminster.targetPlatform;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.AncestorInPath;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Util;
import hudson.matrix.MatrixConfiguration;
 import hudson.matrix.MatrixProject;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.DependecyDeclarer;
 import hudson.model.DependencyGraph;
 import hudson.model.FreeStyleProject;
 import hudson.model.Hudson;
 import hudson.model.Project;
 import hudson.model.Result;
 import hudson.plugins.buckminster.EclipseBuckminsterBuilder;
 import hudson.tasks.ArtifactArchiver;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Messages;
 import hudson.tasks.Publisher;
 import hudson.util.FormValidation;
 
 /**
  * A publisher that archives a workspace directory and makes its content available under a certain name to other jobs that have a buckminster build step. 
  * 
  * 
  * @author Johannes Utzig
  *
  */
 public class TargetPlatformPublisher extends ArtifactArchiver implements
 		DependecyDeclarer {
 
 	private static final String TARGET_PLATFORM_DIRECTORY = "/targetPlatform";
 
 	@DataBoundConstructor
 	public TargetPlatformPublisher(String artifacts, String excludes,
 			boolean latestOnly, String targetPlatformName) {
 		super(artifacts, excludes, latestOnly);
 		this.targetPlatformName = targetPlatformName;
 	}
 
 	/**
 	 * overrides {@link ArtifactArchiver#perform(AbstractBuild, Launcher, BuildListener)} so that
 	 * <ul>
 	 * <li>the contents of the {@link TargetPlatformPublisher#getArtifacts()} is copied flat instead of preserving the directory structure</li>
 	 * <li>file patterns are not allowed</li>
 	 * </ul>
 	 */
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
 			BuildListener listener) throws InterruptedException {
 
 		AbstractProject<?, ?> p = build.getProject();
 
 		if (getArtifacts().length() == 0) {
 			listener.error(Messages.ArtifactArchiver_NoIncludes());
 			build.setResult(Result.FAILURE);
 			return true;
 		}
 
 		File dir = build.getArtifactsDir();
 		dir = new File(dir, "targetPlatform");
 		dir.mkdirs();
 
 		listener.getLogger().println(
 				Messages.ArtifactArchiver_ARCHIVING_ARTIFACTS());
 		try {
 			FilePath ws = p.getWorkspace();
 			if (ws == null) { // #3330: slave down?
 				return true;
 			}
 			FilePath result = ws.child(getArtifacts());
 			if (result.copyRecursiveTo(new FilePath(dir)) == 0) {
 				if (build.getResult().isBetterOrEqualTo(Result.UNSTABLE)) {
 					// If the build failed, don't complain that there was no
 					// matching artifact.
 					// The build probably didn't even get to the point where it
 					// produces artifacts.
 					listener.error(Messages
 							.ArtifactArchiver_NoMatchFound(getArtifacts()));
 					String msg = ws.validateAntFileMask(getArtifacts());
 					if (msg != null)
 						listener.error(msg);
 				}
 				build.setResult(Result.FAILURE);
 				return true;
 			}
 
 		} catch (IOException e) {
 			Util.displayIOException(e, listener);
 			e.printStackTrace(listener.error(Messages
 					.ArtifactArchiver_FailedToArchive(getArtifacts())));
 			return true;
 		}
 
 		return true;
 
 	}
 
 	private String targetPlatformName;
 
 	public TargetPlatformReference getTargetPlatformReference(
 			AbstractProject project) {
 		TargetPlatformReference reference = new TargetPlatformReference();
 		reference.setName(targetPlatformName);
 		reference.setFullName(project.getFullName() + "/" + targetPlatformName);
 		if (project.getLastSuccessfulBuild() != null) {
 			String fullPath = project.getLastSuccessfulBuild()
 					.getArtifactsDir().getAbsolutePath()
 					+ TARGET_PLATFORM_DIRECTORY;
 			reference.setPath(fullPath);
 		}
 		return reference;
 	}
 
 	public String getTargetPlatformName() {
 		return targetPlatformName;
 	}
 
 	public void buildDependencyGraph(AbstractProject owner,
 			DependencyGraph graph) {
 
 		TargetPlatformReference reference = getTargetPlatformReference(owner);
 		if (reference == null || reference.getFullName() == null)
 			return;
 		List<Project> projects = Hudson.getInstance()
 				.getAllItems(Project.class);
 		for (Project project : projects) {
 			List builds = project.getBuilders();
 			for (Object object : builds) {
 				if (object instanceof EclipseBuckminsterBuilder) {
 					EclipseBuckminsterBuilder builder = (EclipseBuckminsterBuilder) object;
 					if (builder.getTargetPlatform() != null
 							&& reference.getFullName().equals(
 									builder.getTargetPlatform().getFullName())) {
						if (project instanceof MatrixConfiguration) {
							//in case of a matrix configuration, the MatrixProject is the actual dependency, not the individual configurations
							MatrixConfiguration matrixConfiguration = (MatrixConfiguration) project;
							MatrixProject matrix = (MatrixProject) project.getParent();
							graph.addDependency(owner, matrix);
						}
						else{
							graph.addDependency(owner, project);	
						}
						
 					}
 
 				}
 			}
 		}
 	}
 
 	/**
 	 * The descriptor of this publisher. It is annotated as an Extension so
 	 * Hudson can automatically register this instance
 	 */
 	@Extension
 	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
 	/**
 	 * The descriptor implementation of this trigger
 	 */
 	public static final class DescriptorImpl extends
 			BuildStepDescriptor<Publisher> {
 
 		@Override
 		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
 			return FreeStyleProject.class.isAssignableFrom(jobType)
 					|| MatrixProject.class.isAssignableFrom(jobType);
 		}
 
 		@Override
 		public String getDisplayName() {
 			return "Archive and publish an Eclipse Target Platform";
 		}
 
 		@Override
 		public Publisher newInstance(StaplerRequest req, JSONObject formData)
 				throws hudson.model.Descriptor.FormException {
 			return req.bindJSON(TargetPlatformPublisher.class, formData);
 		}
 
 		/**
 		 * Performs on-the-fly validation on the relative directory.
 		 */
 		public FormValidation doCheckArtifacts(
 				@AncestorInPath AbstractProject project,
 				@QueryParameter String value) throws IOException {
 			return project.getWorkspace().validateRelativeDirectory(value);
 		}
 		@Override
 		public String getHelpFile() {
 			return "/plugin/buckminster/help-targetPlatformPublisher.html";
 		}
 	}
 }
