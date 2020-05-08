 package hudson.plugins.buckminster.command;
 
 import hudson.FilePath;
 import hudson.model.AbstractBuild;
 import hudson.model.BuildListener;
 import hudson.model.Computer;
 import hudson.model.Hudson;
 import hudson.model.JDK;
 import hudson.model.Node;
 import hudson.model.TaskListener;
 import hudson.plugins.buckminster.BuckminsterInstallation;
 import hudson.plugins.buckminster.EclipseBuckminsterBuilder;
 import hudson.plugins.buckminster.install.BuckminsterInstallable;
 import hudson.plugins.buckminster.install.BuckminsterInstallable.Feature;
 import hudson.plugins.buckminster.install.BuckminsterInstallable.Repository;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Creates the commandline to invoke to process and writes a textfile containing the 
  * buckminster commands to a file commands.txt in the workspace
  * @author Johannes Utzig
  *
  */
 public class CommandLineBuilder {
 	private BuckminsterInstallation installation;
 	private String commands;
 	private String logLevel;
 	private String additionalParams;
 	private File hudsonWorkspaceRoot;
 	private String userWorkspace;
 	private String userTemp;
 	private String userOutput;
 	private String userCommandFile;
 	
 
 	public CommandLineBuilder(BuckminsterInstallation installation,
 			String commands, String logLevel, String additionalParams,
 			String userWorkspace, String userTemp, String userOutput,
 			String userCommandFile) {
 		super();
 		this.installation = installation;
 		this.commands = commands;
 		this.logLevel = logLevel;
 		this.additionalParams = additionalParams;
 		this.userWorkspace = userWorkspace;
 		this.userTemp = userTemp;
 		this.userOutput = userOutput;
 		this.userCommandFile = userCommandFile;
 	}
 
 	/**
 	 * fills an arraylist with all program arguments for buckminster
 	 * 
 	 * @param build
 	 * @return
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public List<String> buildCommands(AbstractBuild<?,?> build, BuildListener listener)
 			throws MalformedURLException, IOException, InterruptedException{
 
 
 		List<String> commandList = new ArrayList<String>();
 		hudsonWorkspaceRoot = new File(build.getWorkspace().absolutize().toURI().getPath());
 		// VM Options
 		JDK jdk = build.getProject().getJDK();
 		if(jdk!=null)
 		{
 	        jdk= jdk.forNode(Computer.currentComputer().getNode(), listener);
 	        jdk = jdk.forEnvironment(build.getEnvironment(listener));
 		}
 
 		//if none is configured, hope it is in the PATH
 		if(jdk==null)
 			commandList.add("java");
 		//otherwise use the configured one
 		else
 		{
 			File javaBinDir = jdk.getBinDir();
 			File javaExecutable = new File(javaBinDir,"java");
			commandList.add(javaExecutable.getCanonicalPath());
 		}
 		
 		Map<String, String> properties = new HashMap<String, String>(build.getEnvironment(listener));
 		properties.putAll(build.getBuildVariables());
 		
 		addJVMProperties(commandList, properties);
 		String commandsPath = getCommandFilePath(build, properties);
 		
 		addStarterParameters(build, commandList, properties);
 
 
 		 commandList.add("--loglevel");
 		 commandList.add(getLogLevel());
 
 		// Tell Buckminster about the command file
 		commandList.add("-S");
 		commandList.add(commandsPath);
 
 		//only write out commands if the user did not specify a custom command file
 		if(userCommandFile==null || userCommandFile.length()==0)
 		{
 			writeCommandFile(commandsPath, properties);
 		}
 		return commandList;
 	}
 
 	private String getCommandFilePath(AbstractBuild<?, ?> build,
 			Map<String, String> properties) {
 		// the file listing all the commands since buckminster doesn't accept
 		// several commands as programm arguments
 		if(userCommandFile==null || userCommandFile.length()==0)
 		{
 			return new File(build.getRootDir(),"commands.txt").getAbsolutePath();
 		}
 		return expandProperties(userCommandFile, properties);
 
 	}
 
 	public BuckminsterInstallation getInstallation() {
 		return installation;
 	}
 
 	public String getCommands() {
 		return commands;
 	}
 
 	public String getLogLevel() {
 		return logLevel;
 	}
 
 	public String getAdditionalParams() {
 		return additionalParams;
 	}
 
 	private void writeCommandFile(String commandsPath, Map<String, String> properties)
 			throws IOException {
 		PrintWriter writer = null;
 		try {
 			writer = new PrintWriter(new FileWriter(commandsPath));
 
 			String[] commands = getCommands().split("[\n\r]+");
 			for (int i = 0; i < commands.length; i++) {
 
 					writer.println(expandProperties(commands[i],properties));
 			}
 		} finally {
 			if (writer != null)
 				writer.close();
 		}
 	}
 
 	private void addStarterParameters(AbstractBuild<?,?> build, List<String> commandList, Map<String, String> properties)
 			throws IOException, InterruptedException {
 		commandList.add("-jar");
 		commandList.add(findEquinoxLauncher());
 
 		// Specify Eclipse Product
 		commandList.add("-application");
 		commandList.add("org.eclipse.buckminster.cmdline.headless");
 
 		// set the workspace to the hudson workspace
 		commandList.add("-data");
 	
 		String workspace = getDataPath(build, properties);
 
 		commandList.add(workspace);
 	}
 
 	private String getDataPath(AbstractBuild<?, ?> build,
 			Map<String, String> properties) throws IOException, InterruptedException {
 		if(userWorkspace==null || userWorkspace.length()==0)
 		{
 			return hudsonWorkspaceRoot.getAbsolutePath();
 		}
 		return expandProperties(userWorkspace, properties);
 	}
 
 	private void addJVMProperties(List<String> commandList, Map<String, String> properties) throws IOException, InterruptedException {
 		//temp and output root
 		commandList.add(MessageFormat.format("-Dbuckminster.output.root={0}",getOutputDir(properties)));
 		commandList.add(MessageFormat.format("-Dbuckminster.temp.root={0}",getTempDir(properties)));
 		String params = getInstallation().getParams();
 		String[] globalVMParams = params.split("[\n\r]+");
 
 		for (int i = 0; i < globalVMParams.length; i++) {
 			if(globalVMParams[i].trim().length()>0)
 				commandList.add(expandProperties(globalVMParams[i],properties));
 		}
 		if(globalVMParams.length==0)
 		{
 			commandList.add("-Xmx512m");
 			commandList.add("-XX:PermSize=128m");
 		}
 		//job vm setting (properties)
 		String jobParams = getAdditionalParams();
 		if(jobParams!=null){
 			String[] additionalJobParams = jobParams.split("[\n\r]+");
 			for (int i = 0; i < additionalJobParams.length; i++) {
 				if(additionalJobParams[i].trim().length()>0){
 					String parameter = expandProperties(additionalJobParams[i],properties);
 					commandList.add(parameter);
 				}
 			}
 		}
 	}
 
 	private Object getTempDir(Map<String, String> properties) {
 		if(userTemp==null || userTemp.length()==0)
 		{
 			return new File(hudsonWorkspaceRoot,"buckminster.temp").getAbsolutePath();
 		}
 		return new File(hudsonWorkspaceRoot,expandProperties(userTemp, properties)).getAbsolutePath();
 	}
 
 	private String getOutputDir(Map<String, String> properties) {
 		if(userOutput==null || userOutput.length()==0)
 		{
 			return new File(hudsonWorkspaceRoot,"buckminster.output").getAbsolutePath();
 		}
 		return new File(hudsonWorkspaceRoot,expandProperties(userOutput, properties)).getAbsolutePath();
 	}
 
 	private String expandProperties(String string, Map<String, String> properties) {
 		Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
 		Matcher matcher = pattern.matcher(string);
 		while(matcher.find()){
 			if(matcher.group(1)!=null){
 				if(properties.containsKey(matcher.group(1))){
 					String replacement = null;
 					if(matcher.group(1).equalsIgnoreCase("WORKSPACE")){
 						//special treatment for the workspace variable because the path has to be transformed into a unix path
 						//see: https://hudson.dev.java.net/issues/show_bug.cgi?id=4947
 						replacement = new File(properties.get("WORKSPACE")).toURI().getPath();
 					}
 					else{
 						replacement = properties.get(matcher.group(1));
 					}
 					string = string.replace(matcher.group(0), replacement);
 					
 				}
 			}
 		}
 		
 		return string;
 	}
 	
 	/**
 	 * searches for the eclipse starter jar
 	 * <p>
 	 * The content of the folder $ECLIPSE_HOME/plugins is listed and the first
 	 * file that starts with <code>org.eclipse.equinox.launcher_</code> is
 	 * returned.
 	 * 
 	 * @return the guess for the startup jar, or <code>null</code> if none was
 	 *         found
 	 * @throws IOException 
 	 * @see EclipseBuckminsterBuilder#getEclipseHome()
 	 */
 	private String findEquinoxLauncher() throws IOException {
 		//TODO: make this behave in master/slave scenario
 		File pluginDir = new File(getInstallation().getHome() + "/plugins");
 		if(!pluginDir.exists())
 			throw new FileNotFoundException("No 'plugins' directory has been found in "+installation.getHome());
 		File[] plugins = pluginDir.listFiles();
 		for (int i = 0; i < plugins.length; i++) {
 			if (plugins[i].getName()
 					.startsWith("org.eclipse.equinox.launcher_")) {
 				return plugins[i].getAbsolutePath();
 
 			}
 		}
 		throw new FileNotFoundException("No equinox launcher jar has been found in "+pluginDir.getCanonicalPath());
 	}
 	
 	public static String createInstallScript(BuckminsterInstallable installable, FilePath toolDir, Node node, TaskListener log) throws MalformedURLException, IOException, InterruptedException
 	{
 		FilePath directorDir = toolDir.child("director");
 		String directorDirPath = directorDir.absolutize().toURI().getPath();
 		FilePath buckyDir = toolDir.child("buckminster");
 		String buckyDirPath = buckyDir.absolutize().toURI().getPath();
 		List<JDK> jdks = Hudson.getInstance().getJDKs();
 		String vmArgument = "";
 		if(jdks!=null && jdks.size()>0)
 		{
 			JDK jdk = Hudson.getInstance().getJDKs().get(0);
 			jdk = jdk.forNode(node, log);
 			jdk = jdk.forEnvironment(Computer.currentComputer().getEnvironment());
 			File javaBinDir = jdk.getBinDir(); 
 			File javaExecutable = new File(javaBinDir,"java");
 			vmArgument = "-vm "+"\""+javaExecutable.getCanonicalPath()+"\"";
 		}
 
 		//TODO: put IU to JSON
 		String command = "{0}/director {1} -r \"{2}\" -d \"{3}\" -p Buckminster -i \"{4}\"";
 		command = MessageFormat.format(command, directorDirPath,vmArgument ,installable.repositoryURL, buckyDirPath, installable.iu);
 		StringBuilder builder = new StringBuilder(command);
 		for (Repository repo : installable.repositories) {
 
 			for (Feature feature : repo.features) {
 				builder.append("\n");
 				command = "{0}/buckminster {1} install {2} {3}";
 				command = MessageFormat.format(command, buckyDirPath,vmArgument, repo.url,feature.id);
 				builder.append(command);
 				builder.append("\n");
 				command = "echo \"Installed Feature {0} from {1}\"";
 				command = MessageFormat.format(command, feature.id, repo.url);
 				builder.append(command);
 			}
 		}
 		return builder.toString();
 	}
 }
