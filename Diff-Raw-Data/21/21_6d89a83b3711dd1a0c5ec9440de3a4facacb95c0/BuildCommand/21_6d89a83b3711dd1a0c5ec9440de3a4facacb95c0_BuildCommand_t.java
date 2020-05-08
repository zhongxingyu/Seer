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
 import com.redhat.contentspec.client.config.ClientConfiguration;
 import com.redhat.contentspec.client.config.ContentSpecConfiguration;
 import com.redhat.contentspec.client.constants.Constants;
 import com.redhat.contentspec.client.utils.ClientUtilities;
 import com.redhat.contentspec.processor.ContentSpecParser;
 import com.redhat.contentspec.processor.ContentSpecProcessor;
 import com.redhat.contentspec.processor.structures.ProcessingOptions;
 import com.redhat.contentspec.rest.RESTManager;
 import com.redhat.contentspec.rest.RESTReader;
 import com.redhat.contentspec.structures.CSDocbookBuildingOptions;
 import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
 import com.redhat.ecs.commonutils.CollectionUtilities;
 import com.redhat.ecs.commonutils.DocBookUtilities;
 import com.redhat.ecs.commonutils.FileUtilities;
 import com.redhat.topicindex.rest.entities.interfaces.RESTBaseTopicV1;
 import com.redhat.topicindex.rest.entities.interfaces.RESTUserV1;
 
 @Parameters(commandDescription = "Build a Content Specification from the server")
 public class BuildCommand extends BaseCommandImpl
 {
 	@Parameter(metaVar = "[ID] or [FILE]")
 	private List<String> ids = new ArrayList<String>();
 	
 	@Parameter(names = Constants.HIDE_ERRORS_LONG_PARAM, description = "Hide the errors in the output.")
 	private Boolean hideErrors = false;
 	
 	@Parameter(names = Constants.SHOW_CONTENT_SPEC_LONG_PARAM, description = "Show the content spec page in the output.")
 	private Boolean hideContentSpec = true;
 	
 	@Parameter(names = Constants.INLINE_INJECTION_LONG_PARAM, description = "Stop injections from being processed when building.")
 	private Boolean inlineInjection = true;
 	
 	@Parameter(names = Constants.INJECTION_TYPES_LONG_PARAM, splitter = CommaParameterSplitter.class, metaVar = "[arg1[,arg2,...]]", description = "Specify certain topic types that injection should be processed on.")
 	private List<String> injectionTypes;
 
 	@Parameter(names = Constants.EXEC_TIME_LONG_PARAM, description = "Show the execution time of the command.", hidden = true)
 	private Boolean executionTime = false;
 	
 	@Parameter(names = {Constants.PERMISSIVE_LONG_PARAM, Constants.PERMISSIVE_SHORT_PARAM}, description = "Turn on permissive processing.")
	private Boolean permissive = null;
 	
 	@DynamicParameter(names = Constants.OVERRIDE_LONG_PARAM, metaVar = "<variable>=<value>")
 	private Map<String, String> overrides = Maps.newHashMap();
 	
 	@Parameter(names = Constants.BUG_REPORTING_LONG_PARM, description = "Hide the error reporting links in the output.")
 	private Boolean hideBugLinks = false;
 	
 	@Parameter(names = {Constants.OUTPUT_LONG_PARAM, Constants.OUTPUT_SHORT_PARAM}, description = "Save the output to the specified file/directory.", metaVar = "<FILE>")
 	private String outputPath;
 	
 	@Parameter(names = Constants.EMPTY_LEVELS_LONG_PARAM, description = "Allow building with empty levels.", hidden=true)
 	private Boolean allowEmptyLevels = false;
 	
 	@Parameter(names = Constants.EDITOR_LINKS_LONG_PARAM, description = "Insert Editor links for each topic.")
 	private Boolean insertEditorLinks = false;
 	
 	@Parameter(names = Constants.LOCALE_LONG_PARAM, description = "What locale to build the content spec for.", metaVar = "<LOCALE>")
 	private String locale = null;
 	
 	private File output;
 	
 	private ContentSpecProcessor csp = null;
 	private ContentSpecBuilder builder = null;
 	
 	public BuildCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig)
 	{
 		super(parser, cspConfig, clientConfig);
 	}
 	
 	public List<String> getInjectionTypes()
 	{
 		return injectionTypes;
 	}
 
 	public void setInjectionTypes(final List<String> injectionTypes)
 	{
 		this.injectionTypes = injectionTypes;
 	}
 
 	public Boolean getInlineInjection()
 	{
 		return inlineInjection;
 	}
 
 	public void setInlineInjection(final Boolean inlineInjection)
 	{
 		this.inlineInjection = inlineInjection;
 	}
 
 	public Boolean getHideErrors()
 	{
 		return hideErrors;
 	}
 
 	public void setHideErrors(final Boolean hideErrors)
 	{
 		this.hideErrors = hideErrors;
 	}
 	
 	public Boolean getHideContentSpecPage()
 	{
 		return hideErrors;
 	}
 
 	public void setHideContentSpecPage(final Boolean hideContentSpecPage)
 	{
 		this.hideContentSpec = hideContentSpecPage;
 	}
 
 	public List<String> getIds()
 	{
 		return ids;
 	}
 
 	public void setIds(final List<String> ids)
 	{
 		this.ids = ids;
 	}
 
 	public Boolean getExecutionTime()
 	{
 		return executionTime;
 	}
 
 	public void setExecutionTime(final Boolean executionTime)
 	{
 		this.executionTime = executionTime;
 	}
 
 	public Map<String, String> getOverrides()
 	{
 		return overrides;
 	}
 
 	public void setOverrides(final Map<String, String> overrides)
 	{
 		this.overrides = overrides;
 	}
 
 	public Boolean getPermissive()
 	{
 		return permissive;
 	}
 
 	public void setPermissive(final Boolean permissive)
 	{
 		this.permissive = permissive;
 	}
 	
 	public File getOutputFile()
 	{
 		return output;
 	}
 	
 	public void setOutputFile(final File outputFile)
 	{
 		this.output = outputFile;
 	}
 	
 	public String getOutputPath()
 	{
 		return outputPath;
 	}
 
 	public void setOutputPath(final String outputPath)
 	{
 		this.outputPath = outputPath;
 	}
 
 	public Boolean getAllowEmptyLevels()
 	{
 		return allowEmptyLevels;
 	}
 
 	public void setAllowEmptyLevels(final Boolean allowEmptyLevels) {
 		this.allowEmptyLevels = allowEmptyLevels;
 	}
 
 	public Boolean getInsertEditorLinks()
 	{
 		return insertEditorLinks;
 	}
 
 	public void setInsertEditorLinks(final Boolean insertEditorLinks)
 	{
 		this.insertEditorLinks = insertEditorLinks;
 	}
 
 	public String getLocale()
 	{
 		return locale;
 	}
 
 	public void setLocale(final String locale)
 	{
 		this.locale = locale;
 	}
 
 	public CSDocbookBuildingOptions getBuildOptions()
 	{
 		final CSDocbookBuildingOptions buildOptions = new CSDocbookBuildingOptions();
 		buildOptions.setInjection(inlineInjection);
 		buildOptions.setInjectionTypes(injectionTypes);
 		buildOptions.setIgnoreMissingCustomInjections(hideErrors);
 		buildOptions.setSuppressErrorsPage(hideErrors);
 		buildOptions.setInsertSurveyLink(true);
 		buildOptions.setInsertBugzillaLinks(!hideBugLinks);
 		buildOptions.setPermissive(permissive);
 		buildOptions.setOverrides(overrides);
 		buildOptions.setSuppressContentSpecPage(hideContentSpec);
 		buildOptions.setInsertEditorLinks(insertEditorLinks);
 		return buildOptions;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	protected String getContentSpecString(final RESTReader reader, final String id)
 	{
 		final String contentSpec;
 		if (id.matches("^\\d+$"))
 		{
 			final RESTBaseTopicV1 contentSpecTopic = reader.getContentSpecById(Integer.parseInt(id), null);
 			/*
 			// TODO Use this code once content specs can be pushed to zanata
 			if (locale == null)
 			{
 				// Get the Content Specification from the server.
 				contentSpecTopic = reader.getContentSpecById(Integer.parseInt(id), null);
 			}
 			else
 			{
 				// Get the Content Specification from the server.
 				contentSpecTopic = reader.getTranslatedContentSpecById(Integer.parseInt(id), null, locale);
 			}*/
 			
 			if (contentSpecTopic == null || contentSpecTopic.getXml() == null)
 			{
 				printError(Constants.ERROR_NO_ID_FOUND_MSG, false);
 				shutdown(Constants.EXIT_FAILURE);
 			}
 			
 			contentSpec = contentSpecTopic.getXml();
			
			if (permissive == null)
			{
				permissive = true;
			}
 		}
 		else
 		{
 			// Get the content spec from the file
 			contentSpec = FileUtilities.readFileContents(new File(ClientUtilities.validateFilePath(id)));
 			
 			if (contentSpec == null  || contentSpec.equals(""))
 			{
 				printError(Constants.ERROR_EMPTY_FILE_MSG, false);
 				shutdown(Constants.EXIT_FAILURE);
 			}
			
			if (permissive == null)
			{
				permissive = false;
			}
 		}
 		
 		return contentSpec;
 	}
 	
 	@Override
 	public void process(final RESTManager restManager, final ErrorLoggerManager elm, final RESTUserV1 user)
 	{
 		final long startTime = System.currentTimeMillis();
 		final RESTReader reader = restManager.getReader();
 		boolean buildingFromConfig = false;
 		
 		// Add the details for the csprocessor.cfg if no ids are specified
 		if (loadFromCSProcessorCfg())
 		{
 			buildingFromConfig = true;
 			setIds(CollectionUtilities.toArrayList(cspConfig.getContentSpecId().toString()));
 			if (cspConfig.getRootOutputDirectory() != null && !cspConfig.getRootOutputDirectory().equals(""))
 			{
 				setOutputPath(cspConfig.getRootOutputDirectory());
 			}
 		}
 		
 		// Check that an id was entered
 		if (ids.size() == 0)
 		{
 			printError(Constants.ERROR_NO_ID_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		}
 		else if (ids.size() > 1)
 		{
 			printError(Constants.ERROR_MULTIPLE_ID_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown())
 		{
 			shutdown.set(true);
 			return;
 		}
 		
 		final String contentSpec = getContentSpecString(reader, ids.get(0));
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown())
 		{
 			shutdown.set(true);
 			return;
 		}
 				
 		// Setup the processing options
 		final ProcessingOptions processingOptions = new ProcessingOptions();
 		processingOptions.setPermissiveMode(permissive);
 		processingOptions.setValidating(true);
 		processingOptions.setIgnoreChecksum(true);
 		processingOptions.setAllowNewTopics(false);
 		if (allowEmptyLevels)
 			processingOptions.setAllowEmptyLevels(true);
 		
 		// Validate and parse the Content Specification
 		csp = new ContentSpecProcessor(restManager, elm, processingOptions);
 		boolean success = false;
 		try
 		{
 			success = csp.processContentSpec(contentSpec, user, ContentSpecParser.ParsingMode.EITHER, locale);
 		}
 		catch (Exception e)
 		{
 			JCommander.getConsole().println(elm.generateLogs());
 			shutdown(Constants.EXIT_FAILURE);
 		}
 		
 		// Print the error/warning messages
 		JCommander.getConsole().println(elm.generateLogs());
 		
 		// Check that everything validated fine
 		if (!success)
 		{
 			shutdown(Constants.EXIT_TOPIC_INVALID);
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown())
 		{
 			shutdown.set(true);
 			return;
 		}
 		
 		JCommander.getConsole().println(Constants.STARTING_BUILD_MSG);
 		
 		// Build the Content Specification
 		byte[] builderOutput = null;
 		try
 		{
 			builder = new ContentSpecBuilder(restManager);
 			builderOutput = builder.buildBook(csp.getContentSpec(), user, getBuildOptions(), cspConfig.getZanataDetails());
 		}
 		catch (Exception e)
 		{
 			printError(Constants.ERROR_INTERNAL_ERROR, false);
 			shutdown(Constants.EXIT_INTERNAL_SERVER_ERROR);
 		}
 		
 		// Print the success messages
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		JCommander.getConsole().println(String.format(Constants.ZIP_SAVED_ERRORS_MSG, builder.getNumErrors(), builder.getNumWarnings()) + (builder.getNumErrors() == 0 && builder.getNumWarnings() == 0 ? " - Flawless Victory!" : ""));
 		if (executionTime)
 		{
 			JCommander.getConsole().println(String.format(Constants.EXEC_TIME_MSG, elapsedTime));
 		}
 		
 		// Create the output file
 		String fileName = DocBookUtilities.escapeTitle(csp.getContentSpec().getTitle());
 		String outputDir = "";
 		if (buildingFromConfig)
 		{
 			outputDir = (cspConfig.getRootOutputDirectory() == null || cspConfig.getRootOutputDirectory().equals("") ? "" : (fileName + File.separator)) + Constants.DEFAULT_CONFIG_ZIP_LOCATION;
 			fileName += "-publican.zip";
 		} 
 		else
 		{
 			fileName += ".zip";
 		}
 		
 		// Create the fully qualified output path
 		if (outputPath != null && outputPath.endsWith("/"))
 		{
 			output = new File(outputPath + outputDir + fileName);
 		}
 		else if (outputPath == null)
 		{
 			output = new File(outputDir + fileName);
 		}
 		else
 		{
 			output = new File(outputPath);
 		}
 		
 		// Make sure the directories exist
 		if (output.isDirectory())
 		{
 			output.mkdirs();
 		}
 		else
 		{
 			if (output.getParentFile() != null)
 				output.getParentFile().mkdirs();
 		}
 		
 		String answer = "y";
 		// Check if the file exists. If it does then check if the file should be overwritten
 		if (!buildingFromConfig && output.exists())
 		{
 			JCommander.getConsole().println(String.format(Constants.FILE_EXISTS_OVERWRITE_MSG, fileName));
 			answer = JCommander.getConsole().readLine();
 			while (!(answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("n") || answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("no")))
 			{
 				JCommander.getConsole().print(String.format(Constants.FILE_EXISTS_OVERWRITE_MSG, fileName));
 				answer = JCommander.getConsole().readLine();
 				
 				// Need to check if the app is shutting down in this loop
 				if (isAppShuttingDown())
 				{
 					shutdown.set(true);
 					return;
 				}
 			}
 		}
 		
 		// Save the book to file
 		try 
 		{
 			if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes"))
 			{
 				FileOutputStream fos = new FileOutputStream(output);
 				fos.write(builderOutput);
 				fos.flush();
 				fos.close();
 				JCommander.getConsole().println(String.format(Constants.OUTPUT_SAVED_MSG, output.getAbsolutePath()));
 			}
 			else
 			{
 				shutdown(Constants.EXIT_FAILURE);
 			}
 		}
 		catch (IOException e)
 		{
 			printError(Constants.ERROR_FAILED_SAVING, false);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 	}
 
 	@Override
 	public void printError(final String errorMsg, final boolean displayHelp)
 	{
 		printError(errorMsg, displayHelp, Constants.BUILD_COMMAND_NAME);
 	}
 
 	@Override
 	public void printHelp()
 	{
 		printHelp(Constants.BUILD_COMMAND_NAME);
 	}
 
 	@Override
 	public RESTUserV1 authenticate(final RESTReader reader)
 	{
 		return authenticate(getUsername(), reader);
 	}
 
 	@Override
 	public void shutdown()
 	{
 		super.shutdown();
 		
 		// No need to wait as the ShutdownInterceptor is waiting
 		// on the whole program.
 		if (csp != null)
 		{
 			csp.shutdown();
 		}
 		
 		if (builder != null)
 		{
 			builder.shutdown();
 		}
 	}
 
 	@Override
 	public boolean loadFromCSProcessorCfg()
 	{
 		return ids.size() == 0 && cspConfig != null && cspConfig.getContentSpecId() != null;
 	}
 }
