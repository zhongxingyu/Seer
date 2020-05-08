 package com.attask.jenkins;
 
 import hudson.*;
 import hudson.model.*;
 import hudson.tasks.*;
 import hudson.tasks.Messages;
 import hudson.util.ListBoxModel;
 import jenkins.model.Jenkins;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.export.Exported;
 import org.kohsuke.stapler.export.ExportedBean;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.*;
 
 /**
  * User: Joel Johnson
  * Date: 8/29/12
  * Time: 5:23 PM
  */
 @ExportedBean
 public class ScriptBuilder extends Builder {
 	public static final boolean CONTINUE = true;
 	public static final boolean ABORT = false;
 
 	private final String scriptName; //will be an absolute path
 	private final List<Parameter> parameters;
 	private final boolean abortOnFailure;
 	private final ErrorMode errorMode;
 	private final String errorRange;
 	private final ErrorMode unstableMode;
 	private final String unstableRange;
 	private final String injectProperties;
 	private final boolean runOnMaster;
 
 	@DataBoundConstructor
 	public ScriptBuilder(String scriptName, List<Parameter> parameters, boolean abortOnFailure, ErrorMode errorMode, String errorRange, ErrorMode unstableMode, String unstableRange, String injectProperties, boolean runOnMaster) {
 		this.scriptName = scriptName;
 		if (parameters == null) {
 			this.parameters = Collections.emptyList();
 		} else {
 			this.parameters = Collections.unmodifiableList(new ArrayList<Parameter>(parameters));
 		}
 
 		this.abortOnFailure = abortOnFailure;
 		this.errorMode = errorMode;
 		this.errorRange = errorRange;
 		this.unstableMode = unstableMode;
 		this.unstableRange = unstableRange;
 
 		this.injectProperties = injectProperties;
 
 		this.runOnMaster = runOnMaster;
 	}
 
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
 		return runScript(build, launcher, listener);
 	}
 
 	private boolean runScript(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
 		Result result;
 
 		final Map<String, Script> runnableScripts = findRunnableScripts();
 		Script script = runnableScripts.get(scriptName);
 		if (script != null) {
 			//If we want to run it on master, do so. But if the job is already running on master, just run it as if the run on master flag isn't set.
 			if (this.runOnMaster && !(launcher instanceof Launcher.LocalLauncher)) {
 				FilePath workspace = Jenkins.getInstance().getRootPath().createTempDir("Workspace", "Temp");
 				try {
 					Launcher masterLauncher = new Launcher.RemoteLauncher(listener, workspace.getChannel(), true);
 					result = execute(build, masterLauncher, listener, script);
 				} finally {
 					workspace.deleteRecursive();
 				}
 			} else {
 				result = execute(build, launcher, listener, script);
 			}
 		} else {
 			listener.error("'" + scriptName + "' doesn't exist anymore. Failing.");
 			result = Result.FAILURE;
 		}
 
 		injectProperties(build, listener);
 
 		build.setResult(result);
 
 		boolean failed = result.isWorseOrEqualTo(Result.FAILURE);
 		if(failed) {
 			if(abortOnFailure) {
 				listener.getLogger().println("Abort on Failure is enabled: Aborting.");
 				return ABORT;
 			} else {
 				listener.getLogger().println("Abort on Failure is disabled: Continuing.");
 				return CONTINUE;
 			}
 		} else {
 			return CONTINUE;
 		}
 	}
 
 	private Map<String, String> injectParameters(List<Parameter> parameters, EnvVars envVars) {
 		Map<String, String> result = new HashMap<String, String>();
 		for (Parameter parameter : parameters) {
 			String key = parameter.getParameterKey();
 			String value = envVars.expand(parameter.getParameterValue());
 			result.put(key, value);
 		}
 		return result;
 	}
 
 	private Result execute(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, Script script) throws IOException, InterruptedException {
 		String scriptContents = script.findScriptContents();
 		int exitCode;
 		CommandInterpreter commandInterpreter;
 		if (launcher.isUnix()) {
 			commandInterpreter = new Shell(scriptContents);
 		} else {
 			commandInterpreter = new BatchFile(scriptContents);
 		}
 
 		PrintStream logger = listener.getLogger();
 		logger.println("========================================");
 		logger.println("Executing: " + script.getFile().getName());
 		logger.println("----------------------------------------");
 
 		long startTime = System.currentTimeMillis();
 		exitCode = executeScript(build, launcher, listener, commandInterpreter);
 		long runTime = System.currentTimeMillis() - startTime;
 		Result result = ExitCodeParser.findResult(exitCode, errorMode, errorRange, unstableMode, unstableRange);
 
 		logger.println("----------------------------------------");
 		logger.println(script.getFile().getName() + " finished in " + runTime + "ms.");
 		logger.println("Exit code was " + exitCode + ". " + result + ".");
 
 		return result;
 	}
 
 	/**
 	 * <p>
 	 *	This method is simply an inline of
 	 *		{@link CommandInterpreter#perform(hudson.model.AbstractBuild, hudson.Launcher, hudson.model.TaskListener)},
 	 *	but returning the exit code instead of a boolean.
 	 *	Also, I've cleaned up the code a bit.
 	 *	Tried to remove any inspection warnings, renamed variables so they would be useful, and added curly braces.
 	 * </p>
 	 * <p>
 	 *  If that method ever gets updated, this one should be too.
 	 *  Obviously the better solution is to change the CommandInterpreter to have two public methods,
 	 *  one that returns the integer value.
 	 * </p>
 	 * <p>
 	 *  The reason I do this is because the exit code provides useful user customization.
 	 *  So now the user can define if the script fails or goes unstable or even remains successful for certain exit codes.
 	 * </p>
 	 */
 	private int executeScript(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, CommandInterpreter command) throws InterruptedException, IOException {
 		FilePath ws = build.getWorkspace();
 		FilePath script = null;
 		try {
 			try {
 				script = command.createScriptFile(ws);
 			} catch (IOException e) {
 				Util.displayIOException(e, listener);
 				e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_UnableToProduceScript()));
 				return -2;
 			}
 
 			int exitCode;
 			try {
 				EnvVars envVars = build.getEnvironment(listener);
 
 				Map<String, String> varsToInject = injectParameters(parameters, envVars);
 				envVars.putAll(varsToInject);
				envVars.put("BUILD_RESULT", String.valueOf(build.getResult()));
 
 				// on Windows environment variables are converted to all upper case,
 				// but no such conversions are done on Unix, so to make this cross-platform,
 				// convert variables to all upper cases.
 				for (Map.Entry<String, String> e : build.getBuildVariables().entrySet()) {
 					envVars.put(e.getKey(), e.getValue());
 				}
 
 				exitCode = launcher.launch().cmds(command.buildCommandLine(script)).envs(envVars).stdout(listener).pwd(ws).join();
 			} catch (IOException e) {
 				Util.displayIOException(e, listener);
 				e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_CommandFailed()));
 				throw e;
 			}
 			return exitCode;
 		} finally {
 			try {
 				if (script != null) {
 					script.delete();
 				}
 			} catch (IOException e) {
 				Util.displayIOException(e, listener);
 				e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_UnableToDelete(script)));
 			}
 		}
 	}
 
 	private void injectProperties(AbstractBuild<?, ?> build, BuildListener listener) throws IOException {
 		PrintStream logger = listener.getLogger();
 
 		if (getInjectProperties() != null && !getInjectProperties().isEmpty()) {
 			logger.println("injecting properties from " + getInjectProperties());
 
 			FilePath filePath = new FilePath(build.getWorkspace(), getInjectProperties());
 			Properties injectedProperties = new Properties();
 			InputStream read = filePath.read();
 			try {
 				injectedProperties.load(read);
 			} finally {
 				read.close();
 			}
 
 			Map<String, String> result = new HashMap<String, String>(injectedProperties.size());
 			for (Map.Entry<Object, Object> entry : injectedProperties.entrySet()) {
 				logger.println("\t" + entry.getKey() + " => " + entry.getValue());
 				result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
 			}
 			build.addAction(new InjectPropertiesAction(result));
 		}
 		logger.println("========================================");
 		logger.println();
 	}
 
 	@Exported
 	public String getScriptName() {
 		return scriptName;
 	}
 
 	@Exported
 	public List<Parameter> getParameters() {
 		return parameters;
 	}
 
 	@Exported
 	public boolean getAbortOnFailure() {
 		return abortOnFailure;
 	}
 
 	@Exported
 	public ErrorMode getErrorMode() {
 		return errorMode;
 	}
 
 	@Exported
 	public String getErrorRange() {
 		return errorRange;
 	}
 
 	@Exported
 	public ErrorMode getUnstableMode() {
 		return unstableMode;
 	}
 
 	@Exported
 	public String getUnstableRange() {
 		return unstableRange;
 	}
 
 	@Exported
 	public String getInjectProperties() {
 		return injectProperties;
 	}
 
 	/**
 	 * If true the script runs on the master node in a temporary directory rather than on the machine the build is running on.
 	 */
 	@Exported
 	public boolean getRunOnMaster() {
 		return runOnMaster;
 	}
 
 	@Override
 	public DescriptorImpl getDescriptor() {
 		return (DescriptorImpl) super.getDescriptor();
 	}
 
 	public Map<String, Script> findRunnableScripts() throws IOException, InterruptedException {
 		FilePath rootPath = Jenkins.getInstance().getRootPath();
 		FilePath userContent = new FilePath(rootPath, "userContent");
 
 		DescriptorImpl descriptor = getDescriptor();
 		return descriptor.findRunnableScripts(userContent);
 	}
 
 	@Extension
 	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
 		public String fileTypes;
 
 		@Override
 		public boolean configure(StaplerRequest request, JSONObject formData) throws FormException {
 			fileTypes = formData.getString("fileTypes");
 			save();
 			return super.configure(request, formData);
 		}
 
 		public String getFileTypes() {
 			load();
 			if (fileTypes == null || fileTypes.isEmpty()) {
 				return ".*";
 			}
 			return fileTypes;
 		}
 
 		@Exported
 		public ListBoxModel doFillScriptNameItems() {
 			FilePath rootPath = Jenkins.getInstance().getRootPath();
 			FilePath userContent = new FilePath(rootPath, "userContent");
 
 			ListBoxModel items = new ListBoxModel();
 			for (Script script : findRunnableScripts(userContent).values()) {
 				//Pretty up the name
 				String path = script.getFile().getRemote();
 				path = path.substring(userContent.getRemote().length() + 1);
 
 				items.add(path, script.getFile().getRemote());
 			}
 
 			return items;
 		}
 
 		@Exported
 		public String getGuid() {
 			return UUID.randomUUID().toString().replaceAll("-", "");
 		}
 
 		private Map<String, Script> findRunnableScripts(FilePath userContent) {
 			final List<String> fileTypes = Arrays.asList(this.getFileTypes().split("\\s+"));
 			try {
 				return userContent.act(new FindScriptsOnMaster(userContent, fileTypes));
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			} catch (InterruptedException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		public ListBoxModel doFillErrorModeItems() {
 			ListBoxModel items = new ListBoxModel();
 			for (ErrorMode errorMode : ErrorMode.values()) {
 				items.add(errorMode.getHumanReadable(), errorMode.toString());
 			}
 			return items;
 		}
 
 		@Exported
 		public ListBoxModel doFillUnstableModeItems() {
 			return doFillErrorModeItems();
 		}
 
 		@Override
 		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
 			return true;
 		}
 
 		@Override
 		public String getDisplayName() {
 			return "Execute UserContent Script";
 		}
 	}
 }
