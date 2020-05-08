 package org.jenkinsci.plugins.appio;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.FilePath.FileCallable;
 import hudson.Launcher;
 import hudson.remoting.VirtualChannel;
 import hudson.model.Action;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Publisher;
 import hudson.tasks.Recorder;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Base64;
 import org.jenkinsci.plugins.appio.model.AppioAppObject;
 import org.jenkinsci.plugins.appio.model.AppioVersionObject;
 import org.jenkinsci.plugins.appio.service.AppioService;
 import org.jenkinsci.plugins.appio.service.S3Service;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import com.cloudbees.plugins.credentials.CredentialsProvider;
 import org.kohsuke.stapler.framework.io.IOException2;
 
 /**
  * @author Kohsuke Kawaguchi
  * @author markprichard
  */
 public class AppioRecorder extends Recorder {
 	private String appFile;
 	private String appName;
 
 	public String getAppName() {
 		return appName;
 	}
 
 	@Override
 	public Action getProjectAction(AbstractProject<?, ?> project) {
         AbstractBuild<?,?> b = project.getLastBuild();
        for (int i=0; i<10; i++) {
             AppioProjectAction a = b.getAction(AppioProjectAction.class);
             if (a!=null)    return a;
             b = b.getPreviousBuild();
         }
         return null;
 	}
 
 	@DataBoundConstructor
 	public AppioRecorder(String appFile, String appName) {
 		this.appFile = appFile;
 		this.appName = appName;
 	}
 
 	public String getAppFile() {
 		return appFile;
 	}
 
 	public BuildStepMonitor getRequiredMonitorService() {
 		return BuildStepMonitor.NONE;
 	}
 
 	@SuppressWarnings("serial")
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener)
 			throws InterruptedException, IOException {
 
 		if (build.getResult().isWorseOrEqualTo(Result.FAILURE))
 			return false;
 
 		final FilePath appPath = build.getWorkspace().child(appFile);
 		listener.getLogger().println("Deploying to App.io: " + appPath);
 
 		List<AppioCredentials> credentialsList = CredentialsProvider.lookupCredentials(AppioCredentials.class,
 				build.getProject());
 		AppioCredentials appioCredentials = credentialsList.get(0);
 
 		byte[] encodedBytes = Base64.encodeBase64(appioCredentials.getApiKey().getPlainText().getBytes());
 		String appioApiKeyBase64 = new String(encodedBytes);
 
 		// Zip <build>.app package for upload to S3
         File zip = File.createTempFile("appio","zip");
         // = appPath.getParent().child(appPath.getName() + ".zip");
 		listener.getLogger().println("Creating zipped package");
 
         try {
             try {
                 appPath.zip(new FilePath(zip));
             } catch (IOException e) {
                 throw new IOException2("Exception creating "+zip,e);
             }
 
 
             // Upload <build>.app.zip to S3 bucket
             String s3Url = null;
             try {
                 S3Service s3service = new S3Service(appioCredentials.getS3AccessKey(), appioCredentials.getS3SecretKey()
                         .getPlainText());
                 listener.getLogger().println("Uploading to S3 bucket: " + appioCredentials.getS3Bucket());
                 //s3Url = s3service.getUploadUrl(appioCredentials.getS3Bucket(), appName, zippedPath);
                 s3Url = s3service.getUploadUrl(appioCredentials.getS3Bucket(), appName + build.getNumber(), zip);
                 listener.getLogger().println("S3 Public URL: " + s3Url);
             } catch (Exception e) {
                 throw new IOException2("Exception while uploading to S3"+zip,e);
             }
 
             // Create new app/version on App.io
             try {
                 // Check if app already exists on App.io
                 AppioAppObject appObject = null;
                 AppioService appioService = new AppioService(appioApiKeyBase64);
 
                 listener.getLogger().println("Checking for App.io app: " + appName);
                 appObject = appioService.findApp(appName);
 
                 // Create new App.io app if necessary
                 if (appObject.getId() == null) {
                     listener.getLogger().println("Creating new App.io application");
                     appObject = appioService.createApp(appName);
                 }
                 listener.getLogger().println("App.io application id: " + appObject.getId());
 
                 // Add new version pointing to S3 URL
                 listener.getLogger().println("Adding new version");
                 AppioVersionObject versionObject = appioService.addVersion(appObject.getId(), s3Url);
                 listener.getLogger().println("App.io version id: " + versionObject.getId());
 
                 // Get the public App.io link for the app
                 listener.getLogger().println("App.io URL: " + "https://app.io/" + appObject.getPublic_key());
                 build.getProject().getAction(AppioProjectAction.class)
                         .setAppURL("https://app.io/" + appObject.getPublic_key());
             } catch (Exception e) {
                 throw new IOException2("Error uploading app/version to App.io",e);
             }
 
             return true;
         } finally {
             zip.delete();
         }
     }
 
 	@Extension
 	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
 
 		// Validation check
 		// public FormValidation doCheckAppFile(@QueryParameter String value)
 		// {�}
 
 		@Override
 		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
 			return true;
 		}
 
 		@Override
 		public String getDisplayName() {
 			return "Upload to App.io";
 		}
 	}
 }
