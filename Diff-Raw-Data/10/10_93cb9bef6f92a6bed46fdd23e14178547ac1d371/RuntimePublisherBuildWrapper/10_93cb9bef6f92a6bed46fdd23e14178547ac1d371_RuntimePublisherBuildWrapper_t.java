 package com.krux.jenkins.runtimepublisher;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildWrapper;
 import hudson.tasks.BuildWrapperDescriptor;
 import hudson.util.FormValidation;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.servlet.ServletException;
 
 import org.kohsuke.stapler.AncestorInPath;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 
 /**
  * Build Wrapper. Add possibility to configure HTML to be published
  * 
  * @author Andrei Varabyeu
  * 
  */
 @SuppressWarnings("rawtypes")
 public class RuntimePublisherBuildWrapper extends BuildWrapper {
 
 	/** HTML Reports to be published */
 	private final List<HtmlReport> reportTargets;
 
 	/**
 	 * @return the reportTargets
 	 */
 	public List<HtmlReport> getReportTargets() {
 		return reportTargets;
 	}
 
 	@DataBoundConstructor
 	public RuntimePublisherBuildWrapper(List<HtmlReport> reportTargets) {
 		this.reportTargets = reportTargets != null ? new ArrayList<HtmlReport>(
 				reportTargets) : new ArrayList<HtmlReport>();
 
 	}
 
 	@Override
 	public Environment setUp(AbstractBuild build, Launcher launcher,
 			BuildListener listener) throws IOException, InterruptedException {
 
 		final ExecutorService threadPool = Executors
 				.newFixedThreadPool(reportTargets.size());
 		for (HtmlReport target : reportTargets) {
 			HtmlReportAction action = new HtmlReportAction(
 					build.getWorkspace(), target);
 			threadPool.execute(new ReportWaiterThread(action, build, listener));
 		}
 		return new Environment() {
 
 			@Override
 			public boolean tearDown(AbstractBuild build, BuildListener listener)
 					throws IOException, InterruptedException {
 				threadPool.shutdownNow();
 				return true;
 			}
 		};
 
 	}
 
 	@Extension
 	public static class DescriptorImpl extends BuildWrapperDescriptor {
 		@Override
 		public String getDisplayName() {
 			return "Publish HTML reports in Runtime";
 		}
 
 		@Override
 		public boolean isApplicable(AbstractProject<?, ?> item) {
 			return true;
 		}
 
 		/**
 		 * Performs on-the-fly validation on the file mask wildcard.
 		 */
 		public FormValidation doCheck(@AncestorInPath AbstractProject project,
 				@QueryParameter String value) throws IOException,
 				ServletException {
 			FilePath ws = project.getSomeWorkspace();
 			return ws != null ? ws.validateRelativeDirectory(value)
 					: FormValidation.ok();
 		}
 
 	}
 
 	public class ReportWaiterThread implements Runnable {
 
 		private HtmlReportAction action;
 		private AbstractBuild build;
 		private BuildListener listener;
 		private boolean actionSubmitted = false;
 
 		public ReportWaiterThread(HtmlReportAction action, AbstractBuild build,
 				BuildListener listener) {
 			this.action = action;
 			this.build = build;
 			this.listener = listener;
 		}
 
 		public void run() {
 			try {
 				listener.getLogger().println(
 						"[runtimereportpublisher] Waiter started for report with name '"
 								+ action.getDisplayName() + "'");
 				waitForPresent();
 				listener.getLogger().println(
 						"[runtimereportpublisher] Waiter finished for report with name '"
 								+ action.getDisplayName() + "'");
 			} catch (IOException e) {
 				build.getActions().remove(action);
 				listener.getLogger()
 						.println(
 								"[runtimereportpublisher] exception occured during runtime report publisher execution "
 										+ action.getDisplayName() + "'");
 			} catch (InterruptedException e) {
 				build.getActions().remove(action);
 				listener.getLogger().println(
						"[runtimereportpublisher] Waiter interrupted for report with name '"
 								+ action.getDisplayName() + "'");
 
 			}
 		}
 
 		public void waitForPresent() throws IOException, InterruptedException {
 			while (true) {
 				Thread.sleep(3000l);
 				boolean present = action.isPresent();
 				if (present) {
 					if (!actionSubmitted) {
 						build.addAction(action);
 						actionSubmitted = true;
 					}
 				} else {
 					if (actionSubmitted) {
 						build.getActions().remove(action);
 						actionSubmitted = false;
 					}
 				}
 			}
 		}
 	}
 
 }
