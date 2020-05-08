 package com.redhat.contentspec.client.commands;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.jboss.pressgang.ccms.contentspec.rest.RESTManager;
 import org.jboss.pressgang.ccms.contentspec.rest.RESTReader;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTUserV1;
 import org.jboss.pressgang.ccms.utils.common.DocBookUtilities;
 import org.jboss.pressgang.ccms.utils.common.ZipUtilities;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.redhat.contentspec.client.config.ClientConfiguration;
 import com.redhat.contentspec.client.config.ContentSpecConfiguration;
 import com.redhat.contentspec.client.constants.Constants;
 import com.redhat.contentspec.client.utils.ClientUtilities;
 import com.redhat.contentspec.processor.ContentSpecParser;
 
 @Parameters(commandDescription = "Build, Assemble and then Publish the Content Specification")
 public class PublishCommand extends BuildCommand
 {
 	@Parameter(names = Constants.NO_BUILD_LONG_PARAM, description = "Don't build the Content Specification.")
 	private Boolean noBuild = false;
 	
 	@Parameter(names = Constants.NO_ASSEMBLE_LONG_PARAM, description = "Don't assemble the Content Specification.")
 	private Boolean noAssemble = false;
 	
 	@Parameter(names = Constants.PUBLICAN_BUILD_LONG_PARAM, description = "Build the Content Specification with publican before publishing.")
 	private Boolean publicanBuild = false;
 	
 	@Parameter(names = Constants.HIDE_OUTPUT_LONG_PARAM, description = "Hide the output from assembling & publishing the Content Specification.")
 	private Boolean hideOutput = false;
 	
 	@Parameter(names = Constants.PUBLISH_MESSAGE_LONG_PARAM, description = "Add a message to be used with the publish command.", metaVar = "<MESSAGE>")
 	private String message = null;
 	
 	public PublishCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig)
 	{
 		super(parser, cspConfig, clientConfig);
 	}
 	
 	public Boolean getNoBuild()
 	{
 		return noBuild;
 	}
 
 	public void setNoBuild(final Boolean noBuild)
 	{
 		this.noBuild = noBuild;
 	}
 	
 	public Boolean getNoAssemble()
 	{
 		return noAssemble;
 	}
 
 	public void setNoAssemble(final Boolean noAssemble)
 	{
 		this.noAssemble = noAssemble;
 	}
 
 	public Boolean getPublicanBuild()
 	{
 		return publicanBuild;
 	}
 
 	public void setPublicanBuild(final Boolean publicanBuild)
 	{
 		this.publicanBuild = publicanBuild;
 	}
 	
 	public Boolean getHideOutput()
 	{
 		return hideOutput;
 	}
 
 	public void setHideOutput(final Boolean hideOutput)
 	{
 		this.hideOutput = hideOutput;
 	}
 
 	public String getPublishMessage()
 	{
 		return message;
 	}
 
 	public void setPublishMessage(final String message)
 	{
 		this.message = message;
 	}
 
 	@Override
 	public void printHelp()
 	{
 		printHelp(Constants.PUBLISH_COMMAND_NAME);
 	}
 
 	@Override
 	public void printError(final String errorMsg, final boolean displayHelp)
 	{
 		printError(errorMsg, displayHelp, Constants.PUBLISH_COMMAND_NAME);
 	}
 
 	@Override
 	public RESTUserV1 authenticate(final RESTReader reader)
 	{
 		return noAssemble || noBuild ? null : authenticate(getUsername(), reader);
 	}
 	
 	private boolean isValid()
 	{
 		if (cspConfig.getPublishCommand() == null || cspConfig.getPublishCommand().isEmpty())
 			return false;
 		
 		return true;
 	}
 
 	@Override
 	public void process(final RESTManager restManager, final ErrorLoggerManager elm, final RESTUserV1 user)
 	{
 		final RESTReader reader = restManager.getReader();
 		final boolean publishFromConfig = loadFromCSProcessorCfg();
 		
 		if (!isValid())
 		{
 			printError(Constants.ERROR_NO_PUBLISH_COMMAND, false);
 			shutdown(Constants.EXIT_CONFIG_ERROR);
 		}
 		
 		if (!noAssemble)
 		{
 			if (!noBuild)
 			{
 				super.process(restManager, elm, user);
 				if (isShutdown()) return;
 			}
 			
 			JCommander.getConsole().println(Constants.STARTING_ASSEMBLE_MSG);
 		}
 		
 		String fileDirectory = "";
 		String outputDirectory = "";
 		String fileName = null;
 		if (publishFromConfig)
 		{
 			final RESTTopicV1 contentSpec = restManager.getReader().getContentSpecById(cspConfig.getContentSpecId(), null);
 			
 			// Check that that content specification was found
 			if (contentSpec == null || contentSpec.getXml() == null) 
 			{
 				printError(Constants.ERROR_NO_ID_FOUND_MSG, false);
 				shutdown(Constants.EXIT_FAILURE);
 			}
 			
 			final String rootDir = (cspConfig.getRootOutputDirectory() == null || cspConfig.getRootOutputDirectory().equals("") ? "" : (cspConfig.getRootOutputDirectory() + DocBookUtilities.escapeTitle(contentSpec.getTitle()) + File.separator));
 			
 			fileDirectory = rootDir + Constants.DEFAULT_CONFIG_ZIP_LOCATION;
 			outputDirectory = rootDir + Constants.DEFAULT_CONFIG_PUBLICAN_LOCATION;
 			fileName = DocBookUtilities.escapeTitle(contentSpec.getTitle()) + "-publican.zip";
 		}
 		else if (getIds() != null && getIds().size() == 1)
 		{
 			final String contentSpec = getContentSpecString(reader, getIds().get(0));
 			
 			/* parse the spec to get the main details */
 			final ContentSpecParser csp = new ContentSpecParser(elm, restManager);
 			try
 			{
 				csp.parse(contentSpec);
 			}
 			catch (Exception e)
 			{
 				printError(Constants.ERROR_INTERNAL_ERROR, false);
 				shutdown(Constants.EXIT_INTERNAL_SERVER_ERROR);
 			}
 			
 			outputDirectory = DocBookUtilities.escapeTitle(csp.getContentSpec().getTitle());
 			fileName = DocBookUtilities.escapeTitle(csp.getContentSpec().getTitle()) + ".zip";
 		}
 		else if (getIds().size() == 0)
 		{
 			printError(Constants.ERROR_NO_ID_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		}
 		else
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
 		
 		// Make sure the output directories exist
 		final File outputDir = new File(outputDirectory);
 		
 		if (!noAssemble)
 		{	
 			final File file = new File(fileDirectory + fileName);
 			if (!file.exists())
 			{
 				printError(String.format(Constants.ERROR_UNABLE_TO_FIND_ZIP_MSG, fileName), false);
 				shutdown(Constants.EXIT_FAILURE);
 			}
 			
 			outputDir.mkdirs();
 			
 			// Ensure that the directory is empty
 			ClientUtilities.deleteDirContents(outputDir);
 			
 			// Unzip the file
 			if (!ZipUtilities.unzipFileIntoDirectory(file, outputDirectory))
 			{
 				printError(Constants.ERROR_FAILED_TO_ASSEMBLE_MSG, false);
 				shutdown(Constants.EXIT_FAILURE);
 			}
 			else
 			{
 				JCommander.getConsole().println(String.format(Constants.SUCCESSFUL_UNZIP_MSG, outputDir.getAbsolutePath()));
 			}
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown())
 		{
 			shutdown.set(true);
 			return;
 		}
 		
 		if (publicanBuild)
 		{
 			String publicanOptions = clientConfig.getPublicanBuildOptions();
 			
 			// Replace the locale in the build options if the locale has been set
 			if (getLocale() != null)
 				publicanOptions = publicanOptions.replaceAll("--lang(s)?=[A-Za-z\\-,]+", "--langs=" + getLocale());
 			
 			try
 			{
 				JCommander.getConsole().println(Constants.STARTING_PUBLICAN_BUILD_MSG);
 				final Integer exitValue = ClientUtilities.runCommand("publican build " + publicanOptions, outputDir, JCommander.getConsole(), !hideOutput, false);
 				if (exitValue == null || exitValue != 0)
 				{
 					shutdown(Constants.EXIT_FAILURE);
 				}
 			}
 			catch (IOException e)
 			{
 				printError(Constants.ERROR_RUNNING_PUBLICAN_MSG, false);
 				shutdown(Constants.EXIT_FAILURE);
 			}
 		}
 		
 		String publishCommand = cspConfig.getPublishCommand();
 		
 		// Replace the locale in the build options if the locale has been set
 		if (getLocale() != null)
 			publishCommand = publishCommand.replaceAll("--lang(s)?=[A-Za-z\\-,]+", "--langs=" + getLocale());
 		
 		// Add the message to the script
 		if (message != null)
 			publishCommand += " -m \"" + message + "\"";
 		
 		try
 		{
 			JCommander.getConsole().println(Constants.PUBLISH_BUILD_MSG);
 			Integer exitValue = ClientUtilities.runCommand(publishCommand, outputDir, JCommander.getConsole(), !hideOutput, true);
 			if (exitValue == null || exitValue != 0)
 			{
			    printError(Constants.ERROR_RUNNING_PUBLISH_MSG, false);
 				shutdown(Constants.EXIT_FAILURE);
 			}
 		}
 		catch (IOException e)
 		{
 			printError(Constants.ERROR_RUNNING_PUBLISH_MSG, false);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 		JCommander.getConsole().println(Constants.SUCCESSFUL_PUBLISH_MSG);
 	}
 }
