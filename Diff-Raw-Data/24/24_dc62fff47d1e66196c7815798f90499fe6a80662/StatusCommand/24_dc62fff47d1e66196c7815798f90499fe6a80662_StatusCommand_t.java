 package com.redhat.contentspec.client.commands;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.google.code.regexp.NamedMatcher;
 import com.google.code.regexp.NamedPattern;
 import com.redhat.contentspec.client.config.ContentSpecConfiguration;
 import com.redhat.contentspec.client.constants.Constants;
 import com.redhat.contentspec.rest.RESTManager;
 import com.redhat.contentspec.rest.RESTReader;
 import com.redhat.contentspec.utils.HashUtilities;
 import com.redhat.contentspec.utils.StringUtilities;
 import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
 import com.redhat.ecs.commonutils.CollectionUtilities;
 import com.redhat.ecs.commonutils.FileUtilities;
 import com.redhat.topicindex.rest.entities.TopicV1;
 import com.redhat.topicindex.rest.entities.UserV1;
 
 @Parameters(commandDescription = "Check the status of a local copy of a Content Specification compared to the server")
 public class StatusCommand extends BaseCommandImpl {
 	
 	@Parameter(metaVar = "[ID]")
 	private List<Integer> ids = new ArrayList<Integer>();
 
 	public StatusCommand(JCommander parser) {
 		super(parser);
 	}
 
 	public List<Integer> getIds() {
 		return ids;
 	}
 
 	public void setIds(List<Integer> ids) {
 		this.ids = ids;
 	}
 
 	@Override
 	public void printHelp() {
 		printHelp(Constants.STATUS_COMMAND_NAME);
 	}
 
 	@Override
 	public void printError(String errorMsg, boolean displayHelp) {
 		printError(errorMsg, displayHelp, Constants.STATUS_COMMAND_NAME);
 	}
 
 	@Override
 	public UserV1 authenticate(RESTReader reader) {
 		return null;
 	}
 
 	@Override
 	public void process(ContentSpecConfiguration cspConfig, RESTManager restManager, ErrorLoggerManager elm, UserV1 user) {
 		RESTReader reader = restManager.getReader();
 		
 		// Load the data from the config data if no ids were specified
 		if (ids.size() == 0 && cspConfig.getContentSpecId() != null) {
 			setIds(CollectionUtilities.toArrayList(cspConfig.getContentSpecId()));
 		}
 		
 		// Check that only one ID exists
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
 		
 		// Get the content specification from the server
 		TopicV1 contentSpec = reader.getContentSpecById(ids.get(0), null);
 		if (contentSpec == null) {
 			printError(Constants.ERROR_NO_ID_FOUND_MSG, false);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 		
 		// Create the local file
 		File file = new File(StringUtilities.escapeTitle(contentSpec.getTitle()) + "-post." + Constants.FILENAME_EXTENSION);
 		
 		// Check that the file exists
 		if (!file.exists()) {
 			// Backwards compatibility check for files ending with .txt
 			file = new File(StringUtilities.escapeTitle(contentSpec.getTitle()) + "-post.txt");
 			if (!file.exists()) {
 				printError(String.format(Constants.ERROR_NO_FILE_OUT_OF_DATE_MSG, file.getName()), false);
 				shutdown(Constants.EXIT_FAILURE);
 			}
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		// Read in the file contents
 		String contentSpecData = FileUtilities.readFileContents(file);
 		
 		if (contentSpecData == null  || contentSpecData.equals("")) {
 			printError(Constants.ERROR_EMPTY_FILE_MSG, false);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown()) {
 			shutdown.set(true);
 			return;
 		}
 		
 		// Calculate the server checksum value
		String serverContentSpecData = contentSpec.getXml().replaceFirst("CHECKSUM[ ]*=.*(\r)?\n", "");
 		String serverChecksum = HashUtilities.generateMD5(serverContentSpecData);
 		
 		// Get the local checksum value
 		NamedPattern pattern = NamedPattern.compile("CHECKSUM[ ]*=[ ]*(?<Checksum>[A-Za-z0-9]+)");
 		NamedMatcher matcher = pattern.matcher(contentSpecData);
 		String checksumValue = "";
 		while (matcher.find()) {
 			String temp = matcher.group();
 			checksumValue = temp.replaceAll("^CHECKSUM[ ]*=[ ]*", "");
 		}
 		
 		// Calculate the local checksum value
		contentSpecData = contentSpecData.replaceFirst("CHECKSUM[ ]*=.*(\r)?\n", "");
 		String checksum = HashUtilities.generateMD5(serverContentSpecData);
 		
 		// Check that the checksums match
 		if (!checksumValue.equals(serverChecksum)) {
 			printError(Constants.ERROR_OUT_OF_DATE_MSG, false);
 			shutdown(Constants.EXIT_OUT_OF_DATE);
 		} else if (!checksum.equals(serverChecksum)) {
 			printError(Constants.ERROR_LOCAL_COPY_UPDATED_MSG, false);
 			shutdown(Constants.EXIT_OUT_OF_DATE);
 		} else {
 			JCommander.getConsole().println(Constants.UP_TO_DATE_MSG);
 		}
 	}
 
 }
