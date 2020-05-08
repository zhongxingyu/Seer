 package com.redhat.contentspec.client.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.redhat.contentspec.client.config.ContentSpecConfiguration;
 import com.redhat.contentspec.client.constants.Constants;
 import com.redhat.contentspec.rest.RESTManager;
 import com.redhat.contentspec.rest.RESTReader;
 import com.redhat.contentspec.utils.HashUtilities;
 import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
 import com.redhat.ecs.commonutils.CollectionUtilities;
 import com.redhat.topicindex.rest.entities.TopicV1;
 import com.redhat.topicindex.rest.entities.UserV1;
 
 @Parameters(commandDescription = "Get the checksum value for a Content Specification")
 public class ChecksumCommand extends BaseCommandImpl {
 
 	@Parameter(metaVar = "[ID]")
 	private List<Integer> ids = new ArrayList<Integer>();
 	
 	@Parameter(names = {Constants.CONTENT_SPEC_LONG_PARAM, Constants.CONTENT_SPEC_SHORT_PARAM})
 	private Boolean contentSpec = true;
 	
 	public ChecksumCommand(JCommander parser) {
 		super(parser);
 	}
 
 	public Boolean useContentSpec() {
 		return contentSpec;
 	}
 
 	public void setContentSpec(Boolean contentSpec) {
 		this.contentSpec = contentSpec;
 	}
 
 	public List<Integer> getIds() {
 		return ids;
 	}
 
 	public void setIds(List<Integer> ids) {
 		this.ids = ids;
 	}
 
 	@Override
 	public void printError(String errorMsg, boolean displayHelp) {
 		printError(errorMsg, displayHelp, Constants.CHECKSUM_COMMAND_NAME);
 		
 	}
 
 	@Override
 	public void printHelp() {
 		printHelp(Constants.CHECKSUM_COMMAND_NAME);
 	}
 	
 	@Override
 	public UserV1 authenticate(RESTReader reader) {
 		return null;
 	}
 
 	@Override
 	public void process(ContentSpecConfiguration cspConfig, RESTManager restManager, ErrorLoggerManager elm, UserV1 user) {
 		// If there are no ids then use the csprocessor.cfg file
 		if (ids.size() == 0 && cspConfig.getContentSpecId() != null) {
 			setIds(CollectionUtilities.toArrayList(cspConfig.getContentSpecId()));
 		}
 		
 		// Check that one and only one ID exists
 		if (ids.size() == 0) {
 			printError(Constants.ERROR_NO_ID_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		} else if (ids.size() > 1) {
 			printError(Constants.ERROR_MULTIPLE_ID_MSG, false);
 			shutdown(Constants.EXIT_ARGUMENT_ERROR);
 		}
 		
 		TopicV1 cs = restManager.getReader().getPostContentSpecById(ids.get(0), null);
 		
 		// Check that that content specification was found
 		if (cs == null || cs.getXml() == null) {
 			printError(Constants.ERROR_NO_ID_FOUND_MSG, false);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 		
 		// Calculate and print the checksum value
		String contentSpec = cs.getXml().replaceFirst("CHECKSUM[ ]*=.*(\r)?\n", "");
 		String checksum = HashUtilities.generateMD5(contentSpec);
 		JCommander.getConsole().println("CHECKSUM=" + checksum);
 	}
 }
