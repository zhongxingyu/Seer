 package com.mojojungle.webosstorm.run;
 
 import com.intellij.execution.ExecutionException;
 import com.intellij.execution.ExecutionManager;
 import com.intellij.execution.Executor;
 import com.intellij.execution.configurations.CommandLineState;
 import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
 import com.intellij.execution.process.OSProcessHandler;
 import com.intellij.execution.process.ProcessAdapter;
 import com.intellij.execution.process.ProcessEvent;
 import com.intellij.execution.process.ProcessHandler;
 import com.intellij.execution.runners.ExecutionEnvironment;
 import com.intellij.execution.ui.RunContentDescriptor;
 import com.intellij.ide.plugins.IdeaPluginDescriptor;
 import com.intellij.ide.plugins.PluginManager;
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.openapi.extensions.PluginId;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.Key;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.ui.content.Content;
 import com.intellij.ui.content.ContentManager;
 import com.mojojungle.webosstorm.WebOSStorm;
 
 import java.io.File;
 
 public class WebOSRunningState extends CommandLineState {
 
 	public static final Key<WebOSRunConfiguration> WEB_OS_RUN_CONFIGURATION_KEY = Key.<WebOSRunConfiguration>create("WebOSRunConfiguration");
 	private ExecutionEnvironment executionEnvironment;
 	private Executor executor;
 
 	public WebOSRunningState(Executor executor, ExecutionEnvironment executionEnvironment) {
 		super(executionEnvironment);
 		this.executor = executor;
 		this.executionEnvironment = executionEnvironment;
 		Project project = executionEnvironment.getProject();
 		TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
 		builder.setViewer(false);
 		this.setConsoleBuilder(builder);
 	}
 
 	@Override
 	protected OSProcessHandler startProcess() throws ExecutionException {
 		WebOSRunConfiguration config = (WebOSRunConfiguration) getRunnerSettings().getRunProfile();
 		VirtualFile appFolder = config.getAppFolder();
 		RunTarget target = config.getTarget();
 		boolean loggingEnabled = config.LOGGING;
 		boolean appAlreadyRunning = false;
 
 		if (appFolder == null) {
 			throw new ExecutionException("Please select a valid webOS application directory");
 		}
 
 		Project project = executionEnvironment.getProject();
 //		RunContentManagerImpl runContentManager = (RunContentManagerImpl) ExecutionManager.getInstance(project).getContentManager();
 //		RunContentDescriptor[] allDescriptors = runContentManager.getAllDescriptors();
 //		RunContentDescriptor d = allDescriptors[0];
 		ProcessHandler[] runningProcesses = ExecutionManager.getInstance(project).getRunningProcesses();
 		for (ProcessHandler process : runningProcesses) {
 			WebOSRunConfiguration data = process.getUserData(WEB_OS_RUN_CONFIGURATION_KEY);
 			if (data != null) {
 				if (data.equals(config)) {
 					appAlreadyRunning = true;
 					if (data.LOGGING) {
 						loggingEnabled = false;
 					}
 				}
 			}
 		}
 
 		IdeaPluginDescriptor descriptor = PluginManager.getPlugin(PluginId.getId(WebOSStorm.PLUGIN_ID));
 		File palmRunJar = new File(descriptor.getPath(), "lib/palm-run.jar");
 
 		GeneralCommandLine command = new GeneralCommandLine();
 		command.setExePath("java");
 		command.addParameter("-cp");
 		command.addParameter(WebOSStorm.getWebOSToolsJarPath() + File.pathSeparatorChar + palmRunJar.getAbsolutePath());
 		command.addParameter("com.mojojungle.palmrun.Main");
 		command.addParameter(target.getId());
 		command.addParameter(appFolder.getPath());
 		command.addParameter("log=" + loggingEnabled);
 
 		String configName = this.getRunnerSettings().getRunProfile().getName();
 		String title = "Run '" + configName + "' on " + target;// + " (" + command.getCommandLineString() + ")";
 		OSProcessHandler processHandler = new OSProcessHandler(command.createProcess(), title);
 		processHandler.putUserData(WEB_OS_RUN_CONFIGURATION_KEY, config);
 		final boolean finalAppAlreadyRunning = appAlreadyRunning;
 		processHandler.addProcessListener(new ProcessAdapter() {
 			@Override
 			public void processTerminated(ProcessEvent event) {
 				if (finalAppAlreadyRunning) {
 					RunContentDescriptor contentDescriptor = ExecutionManager.getInstance(executionEnvironment.getProject()).getContentManager().findContentDescriptor(executor, event.getProcessHandler());
 					if (contentDescriptor != null) {
 						final Content content = contentDescriptor.getAttachedContent();
 						ApplicationManager.getApplication().invokeLater(new Runnable() {
 							public void run() {
 								String oldName = content.getDisplayName();
 								ContentManager manager = content.getManager();
 
 								manager.removeContent(content, true);
 								Content oldContent = manager.findContent(oldName);
 								if (oldContent != null)
 									manager.setSelectedContent(oldContent);
 							}
 						});
 					}
 				}
 			}
 		});
 		return processHandler;
 	}
 
 }
