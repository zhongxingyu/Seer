 package ase;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.maven.AbstractMavenProject;
 import hudson.maven.ModuleName;
 import hudson.maven.MavenModule;
 import hudson.maven.MavenModuleSet;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Result;
 import hudson.model.User;
 import hudson.scm.ChangeLogSet;
 import hudson.scm.ChangeLogSet.Entry;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Notifier;
 import hudson.tasks.Publisher;
 
 import java.io.IOException;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 public class BuildGamePlugin extends Notifier
 {
 	private String sonarUrl = "";
 	private String sonarUsername = "";
 	private String sonarPassword = "";
 
 	@DataBoundConstructor
 	public BuildGamePlugin(String sonarUrl, String sonarUsername, String sonarPassword)
 	{
 		this.sonarUrl = sonarUrl;
 		this.sonarUsername = sonarUsername;
 		this.sonarPassword = sonarPassword;
 	}
 
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException
 	{
		boolean buildSuccess = build.getResult().isBetterOrEqualTo(Result.SUCCESS);
		
 		if (buildSuccess)
 		{
 			listener.getLogger().println("Executing BuildGame plugin...");
 
 			String project = getProjectId(build.getProject());
 			listener.getLogger().println("Project Id: " + project);
 			listener.getLogger().println("Sonar URL: " + sonarUrl);
 			double pointValue = ComputePoints.getPointValue(project, sonarUrl, sonarUsername, sonarPassword);
 			listener.getLogger().println("That build was worth " + pointValue + " points.");
 			listener.getLogger().println("Distributing points among players involved with this build...");
 
 			Set<User> players = new TreeSet<User>();
 			ChangeLogSet<? extends Entry> changeSet = build.getChangeSet();
 
 			if (changeSet != null)
 			{
 				int count = 1;
 				for (Entry entry : changeSet)
 				{
 					players.add(entry.getAuthor());
 					listener.getLogger().println("Contributor " + count + ": " + entry.getAuthor());
 					count++;
 				}
 			}
 
 			updateBuildersScore(players, pointValue);
 
 			listener.getLogger().println("Finished BuildGame plugin execution.");
 		}
 		else
 		{
 			listener.getLogger().println("Build did not complete successfully. Skpping BuildGame plugin.");
 		}
 
 		return true;
 	}
 
 	private void updateBuildersScore(Set<User> players, double pointValue) throws IOException
 	{
 		if (pointValue != 0)
 		{
 			for (User user : players)
 			{
 				ScoreProperty property = user.getProperty(ScoreProperty.class);
 				if (property == null)
 				{
 					property = new ScoreProperty();
 					user.addProperty(property);
 				}
 				property.setScore(property.getScore() + pointValue);
 
 				user.save();
 			}
 		}
 	}
 
 	private String getProjectId(AbstractProject<?, ?> project)
 	{
 		// SonarInstallation sonarInstallation = getInstallation();
 		String url = "";
 		if (project instanceof AbstractMavenProject)
 		{
 			AbstractMavenProject mavenProject = (AbstractMavenProject) project;
 			if (mavenProject.getRootProject() instanceof MavenModuleSet)
 			{
 				MavenModuleSet mms = (MavenModuleSet) mavenProject.getRootProject();
 				MavenModule rootModule = mms.getRootModule();
 				if (rootModule != null)
 				{
 					ModuleName moduleName = rootModule.getModuleName();
 					url += moduleName.groupId + ":" + moduleName.artifactId;
 				}
 			}
 		}
 		return url;
 	}
 
 	@Extension(ordinal = 999)
 	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher>
 	{
 		public boolean isApplicable(Class<? extends AbstractProject> aClass)
 		{
 			return true;
 		}
 
 		// This human readable name is used in the configuration screen.
 		public String getDisplayName()
 		{
 			return "Run BuildGame";
 		}
 	}
 
 	public BuildStepMonitor getRequiredMonitorService()
 	{
 		return BuildStepMonitor.BUILD;
 	}
 
 	public String getSonarUrl()
 	{
 		return sonarUrl;
 	}
 
 	public String getSonarUsername()
 	{
 		return sonarUsername;
 	}
 
 	public String getSonarPassword()
 	{
 		return sonarPassword;
 	}
 }
