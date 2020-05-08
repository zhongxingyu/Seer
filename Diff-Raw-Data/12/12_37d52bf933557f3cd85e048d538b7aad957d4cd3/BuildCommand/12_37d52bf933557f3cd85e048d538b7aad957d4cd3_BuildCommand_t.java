 package com.redhat.contentspec.client.commands;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.beust.jcommander.DynamicParameter;
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.beust.jcommander.converters.CommaParameterSplitter;
 import com.beust.jcommander.internal.Maps;
 import com.redhat.contentspec.builder.ContentSpecBuilder;
 import com.redhat.contentspec.builder.utils.BuilderOptions;
 import com.redhat.contentspec.client.config.ContentSpecConfiguration;
 import com.redhat.contentspec.client.constants.Constants;
 import com.redhat.contentspec.processor.ContentSpecParser;
 import com.redhat.contentspec.processor.ContentSpecProcessor;
 import com.redhat.contentspec.rest.RESTManager;
 import com.redhat.contentspec.rest.RESTReader;
 import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
 import com.redhat.ecs.commonutils.CollectionUtilities;
 import com.redhat.topicindex.rest.entities.TopicV1;
 import com.redhat.topicindex.rest.entities.UserV1;
 
 @Parameters(commandDescription = "Build a Content Specification from the server")
 public class BuildCommand extends BaseCommandImpl {
 
 	@Parameter(metaVar = "[ID]")
 	private List<Integer> ids = new ArrayList<Integer>();
 	
 	@Parameter(names = Constants.HIDE_ERRORS_LONG_PARAM, description = "Hide the errors in the output.")
 	private Boolean hideErrors = false;
 	
 	@Parameter(names = Constants.INLINE_INJECTION_LONG_PARAM, description = "Stop injections from being processed when building.")
 	private Boolean inlineInjection = true;
 	
 	@Parameter(names = Constants.INJECTION_TYPES_LONG_PARAM, splitter = CommaParameterSplitter.class, metaVar = "[arg1[,arg2,...]]", description = "Specify certain topic types that injection should be processed on.")
 	private List<String> injectionTypes;
 
 	@Parameter(names = Constants.EXEC_TIME_LONG_PARAM, description = "Show the execution time of the command.", hidden = true)
 	private Boolean executionTime = false;
 	
 	@Parameter(names = {Constants.PERMISSIVE_LONG_PARAM, Constants.PERMISSIVE_SHORT_PARAM}, description = "Turn on permissive processing.")
 	private Boolean permissive = false;
 	
 	@DynamicParameter(names = Constants.OVERRIDE_LONG_PARAM, metaVar = "<variable>=<value>")
 	private Map<String, String> overrides = Maps.newHashMap();
 	
 	@Parameter(names = Constants.BUG_REPORTING_LONG_PARM, description = "Hide the error reporting links in the output.")
 	private Boolean hideBugLinks = false;
 	
 	@Parameter(names = {Constants.OUTPUT_LONG_PARAM, Constants.OUTPUT_SHORT_PARAM}, description = "Save the output to the specified file/directory.", metaVar = "<FILE>")
 	private String outputPath;
 	
 	private File output;
 	
 	private ContentSpecProcessor csp = null;
 	private ContentSpecBuilder builder = null;
 	
 	public BuildCommand(JCommander parser) {
 		super(parser);
 	}
 	
 	public List<String> getInjectionTypes() {
 		return injectionTypes;
 	}
 
 	public void setInjectionTypes(List<String> injectionTypes) {
 		this.injectionTypes = injectionTypes;
 	}
 
 	public Boolean getInlineInjection() {
 		return inlineInjection;
 	}
 
 	public void setInlineInjection(Boolean inlineInjection) {
 		this.inlineInjection = inlineInjection;
 	}
 
 	public Boolean getHideErrors() {
 		return hideErrors;
 	}
 
 	public void setHideErrors(Boolean hideErrors) {
 		this.hideErrors = hideErrors;
 	}
 
 	public List<Integer> getIds() {
 		return ids;
 	}
 
 	public void setIds(List<Integer> ids) {
 		this.ids = ids;
 	}
 
 	public Boolean getExecutionTime() {
 		return executionTime;
 	}
 
 	public void setExecutionTime(Boolean executionTime) {
 		this.executionTime = executionTime;
 	}
 
 	public Map<String, String> getOverrides() {
 		return overrides;
 	}
 
 	public void setOverrides(Map<String, String> overrides) {
 		this.overrides = overrides;
 	}
 
 	public Boolean getPermissive() {
 		return permissive;
 	}
 
 	public void setPermissive(Boolean permissive) {
 		this.permissive = permissive;
 	}
 	
 	public File getOutputFile() {
 		return output;
 	}
 	
 	public void setOutputFile(File outputFile) {
 		this.output = outputFile;
 	}
 	
 	public String getOutputPath() {
 		return outputPath;
 	}
 
 	public void setOutputPath(String outputPath) {
 		this.outputPath = outputPath;
 	}
 
 	public BuilderOptions getBuildOptions() {
 		BuilderOptions buildOptions = new BuilderOptions();
 		buildOptions.setInjection(inlineInjection);
 		buildOptions.setInjectionTypes(injectionTypes);
 		buildOptions.setIgnoreErrors(hideErrors);
 		buildOptions.setInjectBugzillaLinks(!hideBugLinks);
 		buildOptions.setPermissive(permissive);
 		buildOptions.setOverrides(overrides);
 		return buildOptions;
 	}
 	
 	@Override
 	public void process(ContentSpecConfiguration cspConfig, RESTManager restManager, ErrorLoggerManager elm, UserV1 user) {
 		long startTime = System.currentTimeMillis();
 		RESTReader reader = restManager.getReader();
 		boolean buildingFromConfig = false;
 		
 		// Add the details for the csprocessor.cfg if no ids are specified
 		if (ids.size() == 0 && cspConfig.getContentSpecId() != null) {
 			buildingFromConfig = true;
 			setIds(CollectionUtilities.toArrayList(cspConfig.getContentSpecId()));
 			if (cspConfig.getRootOutputDirectory() != null && !cspConfig.getRootOutputDirectory().equals("")) {
 				setOutputPath(cspConfig.getRootOutputDirectory());
 			}
 		}
 		
 		// Check that an id was entered
 		if (ids.size() == 0) {
 			printError(Constants.ERROR_NO_ID_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		} else if (ids.size() > 1) {
 			printError(Constants.ERROR_MULTIPLE_ID_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		// Get the Content Specification from the server.
 		TopicV1 contentSpec = reader.getContentSpecById(ids.get(0), null);
 		if (contentSpec == null || contentSpec.getXml() == null) {
 			printError(Constants.ERROR_NO_ID_FOUND_MSG, false);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		JCommander.getConsole().println(Constants.STARTING_VALIDATE_MSG);
 		
 		// Validate and parse the Content Specification
 		csp = new ContentSpecProcessor(restManager, elm, permissive, true, true);
		boolean success = false;
 		try {
			success = csp.processContentSpec(contentSpec.getXml(), user, ContentSpecParser.ParsingMode.EDITED);
 		} catch (Exception e) {
 			JCommander.getConsole().println(elm.generateLogs());
 			shutdown(Constants.EXIT_FAILURE);
 		}
 		
		// Check that everything validated fine
		if (!success)
		{
			JCommander.getConsole().println(elm.generateLogs());
			shutdown(Constants.EXIT_TOPIC_INVALID);
		}
		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		JCommander.getConsole().println(Constants.STARTING_BUILD_MSG);
 		
 		// Build the Content Specification
 		byte[] builderOutput = null;
 		try {
 			builder = new ContentSpecBuilder(restManager);
 			builderOutput = builder.buildBook(csp.getContentSpec(), user, getBuildOptions());
 		} catch (Exception e) {
 			printError(Constants.ERROR_INTERNAL_ERROR, false);
 			e.printStackTrace();
 			shutdown(Constants.EXIT_INTERNAL_SERVER_ERROR);
 		}
 		
 		// Print the success messages
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		JCommander.getConsole().println(String.format(Constants.ZIP_SAVED_ERRORS_MSG, builder.getNumErrors(), builder.getNumWarnings()) + (builder.getNumErrors() == 0 && builder.getNumWarnings() == 0 ? " - Flawless Victory!" : ""));
 		if (executionTime) {
 			JCommander.getConsole().println(String.format(Constants.EXEC_TIME_MSG, elapsedTime));
 		}
 		
 		// Create the output file
 		String fileName = builder.getEscapedName();
 		String outputDir = "";
 		if (buildingFromConfig) {
 			outputDir = (cspConfig.getRootOutputDirectory() == null || cspConfig.getRootOutputDirectory().equals("") ? "" : (builder.getEscapedName() + File.separator)) + Constants.DEFAULT_CONFIG_ZIP_LOCATION;
 			fileName += "-publican.zip";
 		} else {
 			fileName += ".zip";
 		}
 		if (outputPath != null && outputPath.endsWith("/")) {
 			output = new File(outputPath + outputDir + fileName);
 		} else if (outputPath == null) {
 			output = new File(outputDir + fileName);
 		} else {
 			output = new File(outputPath);
 		}
 		
 		// Make sure the directories exist
 		if (output.isDirectory()) {
 			output.mkdirs();
 		} else {
 			if (output.getParentFile() != null)
 				output.getParentFile().mkdirs();
 		}
 		
 		String answer = "y";
 		// Check if the file exists. If it does then check if the file should be overwritten
 		if (!buildingFromConfig && output.exists()) {
 			JCommander.getConsole().println(String.format(Constants.FILE_EXISTS_OVERWRITE_MSG, fileName));
 			answer = JCommander.getConsole().readLine();
 			while (!(answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("n") || answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("no"))) {
 				JCommander.getConsole().print(String.format(Constants.FILE_EXISTS_OVERWRITE_MSG, fileName));
 				answer = JCommander.getConsole().readLine();
 				
 				// Need to check if the app is shutting down in this loop
 				if (isAppShuttingDown()) {
 					shutdown.set(true);
 					return;
 				}
 			}
 		}
 		
 		// Save the book to file
 		try {
 			if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
 				FileOutputStream fos = new FileOutputStream(output);
 				fos.write(builderOutput);
 				fos.flush();
 				fos.close();
 				JCommander.getConsole().println(String.format(Constants.OUTPUT_SAVED_MSG, output.getAbsolutePath()));
 			} else {
 				shutdown(Constants.EXIT_FAILURE);
 			}
 		} catch (IOException e) {
 			printError(Constants.ERROR_FAILED_SAVING, false);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 	}
 
 	@Override
 	public void printError(String errorMsg, boolean displayHelp) {
 		printError(errorMsg, displayHelp, Constants.BUILD_COMMAND_NAME);
 	}
 
 	@Override
 	public void printHelp() {
 		printHelp(Constants.BUILD_COMMAND_NAME);
 	}
 
 	@Override
 	public UserV1 authenticate(RESTReader reader) {
 		return authenticate(getUsername(), reader);
 	}
 
 	@Override
 	public void shutdown() {
 		super.shutdown();
 		
 		// No need to wait as the ShutdownInterceptor is waiting
 		// on the whole program.
 		if (csp != null) {
 			csp.shutdown();
 		}
 		if (builder != null) {
 			builder.shutdown();
 		}
 	}
 }
