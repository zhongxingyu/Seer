 package com.redhat.contentspec.client.commands;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.w3c.dom.Document;
 import org.zanata.common.ContentType;
 import org.zanata.common.LocaleId;
 import org.zanata.common.ResourceType;
 import org.zanata.rest.dto.resource.Resource;
 import org.zanata.rest.dto.resource.TextFlow;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.redhat.contentspec.ContentSpec;
 import com.redhat.contentspec.builder.utils.XMLUtilities;
 import com.redhat.contentspec.client.config.ClientConfiguration;
 import com.redhat.contentspec.client.config.ContentSpecConfiguration;
 import com.redhat.contentspec.client.constants.Constants;
 import com.redhat.contentspec.client.utils.ClientUtilities;
 import com.redhat.contentspec.processor.ContentSpecParser.ParsingMode;
 import com.redhat.contentspec.processor.ContentSpecProcessor;
 import com.redhat.contentspec.processor.structures.ProcessingOptions;
 import com.redhat.contentspec.rest.RESTManager;
 import com.redhat.contentspec.rest.RESTReader;
 import com.redhat.contentspec.utils.ContentSpecUtilities;
 import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
 import com.redhat.ecs.commonstructures.Pair;
 import com.redhat.ecs.commonstructures.StringToCSNodeCollection;
 import com.redhat.ecs.commonstructures.StringToNodeCollection;
 import com.redhat.ecs.commonutils.CollectionUtilities;
 import com.redhat.ecs.commonutils.DocBookUtilities;
 import com.redhat.ecs.commonutils.HashUtilities;
 import com.redhat.ecs.constants.CommonConstants;
 import com.redhat.topicindex.rest.collections.RESTTopicCollectionV1;
 import com.redhat.topicindex.rest.entities.interfaces.RESTTopicV1;
 import com.redhat.topicindex.rest.entities.interfaces.RESTUserV1;
 import com.redhat.topicindex.zanata.ZanataDetails;
 import com.redhat.topicindex.zanata.ZanataInterface;
 
 @Parameters(commandDescription = "Push a Content Specification and it's topics to Zanata for translation.")
 public class PushTranslationCommand extends BaseCommandImpl
 {
 	@Parameter(metaVar = "[ID]")
 	private List<Integer> ids = new ArrayList<Integer>();
 	
 	private ContentSpecProcessor csp;
 	
 	public PushTranslationCommand(final JCommander parser, final ContentSpecConfiguration cspConfig, final ClientConfiguration clientConfig)
 	{
 		super(parser, cspConfig, clientConfig);
 	}
 
 	public List<Integer> getIds()
 	{
 		return ids;
 	}
 
 	public void setIds(final List<Integer> ids)
 	{
 		this.ids = ids;
 	}
 
 	@Override
 	public void printHelp()
 	{
 		printHelp(Constants.PUSH_TRANSLATION_COMMAND_NAME);
 	}
 
 	@Override
 	public void printError(final String errorMsg, final boolean displayHelp)
 	{
 		printError(errorMsg, displayHelp, Constants.PUSH_TRANSLATION_COMMAND_NAME);
 	}
 
 	@Override
 	public RESTUserV1 authenticate(final RESTReader reader)
 	{
 		return authenticate(getUsername(), reader);
 	}
 	
 	@Override
 	public void validateServerUrl()
 	{
 		// Print the server url
 		JCommander.getConsole().println(String.format(Constants.WEBSERVICE_MSG, getServerUrl()));
 		
 		// Test that the server address is valid
 		if (!ClientUtilities.validateServerExists(getServerUrl()))
 		{
 			// Print a line to separate content
 			JCommander.getConsole().println("");
 			
 			printError(Constants.UNABLE_TO_FIND_SERVER_MSG, false);
 			shutdown(Constants.EXIT_NO_SERVER);
 		}
 		
 		final ZanataDetails zanataDetails = cspConfig.getZanataDetails();
 		
 		// Print the zanata server url
 		JCommander.getConsole().println(String.format(Constants.ZANATA_WEBSERVICE_MSG, zanataDetails.getServer()));
 		
 		// Test that the server address is valid
 		if (!ClientUtilities.validateServerExists(zanataDetails.getServer()))
 		{
 			// Print a line to separate content
 			JCommander.getConsole().println("");
 			
 			printError(Constants.UNABLE_TO_FIND_SERVER_MSG, false);
 			shutdown(Constants.EXIT_NO_SERVER);
 		}
 	}
 	
 	protected boolean isValid()
 	{
 		final ZanataDetails zanataDetails = cspConfig.getZanataDetails();
 		
 		// Check that we even have some zanata details.
 		if (zanataDetails == null) return false;
 		
 		// Check that none of the fields are invalid.
 		if (zanataDetails.getServer() == null || zanataDetails.getServer().isEmpty()
 				|| zanataDetails.getProject() == null || zanataDetails.getProject().isEmpty()
 				|| zanataDetails.getVersion() == null || zanataDetails.getVersion().isEmpty()
 				|| zanataDetails.getToken() == null || zanataDetails.getToken().isEmpty()
 				|| zanataDetails.getUsername() == null || zanataDetails.getUsername().isEmpty())
 		{
 			return false;
 		}
 		
 		// At this point the zanata details are valid, so save the details.
 		System.setProperty(CommonConstants.ZANATA_SERVER_PROPERTY, zanataDetails.getServer());
 		System.setProperty(CommonConstants.ZANATA_PROJECT_PROPERTY, zanataDetails.getProject());
 		System.setProperty(CommonConstants.ZANATA_PROJECT_VERSION_PROPERTY, zanataDetails.getVersion());
 		System.setProperty(CommonConstants.ZANATA_USERNAME_PROPERTY, zanataDetails.getUsername());
 		System.setProperty(CommonConstants.ZANATA_TOKEN_PROPERTY, zanataDetails.getToken());
 		
 		return true;
 	}
 
 	@Override
 	public void process(final RESTManager restManager, final ErrorLoggerManager elm, final RESTUserV1 user)
 	{
 		final RESTReader reader = restManager.getReader();
 		
 		// Add the details for the csprocessor.cfg if no ids are specified
 		if (loadFromCSProcessorCfg())
 		{
 			setIds(CollectionUtilities.toArrayList(cspConfig.getContentSpecId()));
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
 		
 		// Check that the zanata details are valid
 		if (!isValid())
 		{
			// TODO Error Message
 			shutdown(Constants.EXIT_CONFIG_ERROR);
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown())
 		{
 			shutdown.set(true);
 			return;
 		}
 		
 		final RESTTopicV1 contentSpecTopic = reader.getContentSpecById(ids.get(0), null);
 		
 		if (contentSpecTopic == null || contentSpecTopic.getXml() == null)
 		{
 			printError(Constants.ERROR_NO_ID_FOUND_MSG, false);
 			shutdown(Constants.EXIT_FAILURE);
 		}
 				
 		// Setup the processing options
 		final ProcessingOptions processingOptions = new ProcessingOptions();
 		processingOptions.setPermissiveMode(true);
 		processingOptions.setValidating(true);
 		processingOptions.setIgnoreChecksum(true);
 		processingOptions.setAllowNewTopics(false);
 		
 		// Validate and parse the Content Specification
 		csp = new ContentSpecProcessor(restManager, elm, processingOptions);
 		boolean success = false;
 		try
 		{
 			success = csp.processContentSpec(contentSpecTopic.getXml(), user, ParsingMode.EITHER);
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
 		
 		// Get the topics that were processed by the ContentSpecProcessor. (They should be stored in cache)
 		RESTTopicCollectionV1 topics = reader.getTopicsByIds(csp.getParser().getReferencedLatestTopicIds(), false);
 		
 		if (topics == null)
 			topics = new RESTTopicCollectionV1();
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown())
 		{
 			shutdown.set(true);
 			return;
 		}
 		
 		// Get the revision topics that were processed by the ContentSpecProcessor. (They should be stored in cache)
 		final List<Pair<Integer, Integer>> referencedRevisionTopicIds = csp.getParser().getReferencedRevisionTopicIds();
 		for (final Pair<Integer, Integer> referencedRevisionTopic : referencedRevisionTopicIds)
 		{
 			final RESTTopicV1 revisionTopic = reader.getTopicById(referencedRevisionTopic.getFirst(), referencedRevisionTopic.getSecond());
 			topics.addItem(revisionTopic);
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown())
 		{
 			shutdown.set(true);
 			return;
 		}
 		
 		if (!pushCSTopicsToZanata(topics, contentSpecTopic, csp.getContentSpec()))
 		{
			// TODO Error Message
			printError("", false);
 			System.exit(Constants.EXIT_FAILURE);
 		}
 		else
 		{
			// TODO success message
 		}
 	}
 
 	@Override
 	public boolean loadFromCSProcessorCfg()
 	{
 		return ids.size() == 0 && cspConfig != null && cspConfig.getContentSpecId() != null;
 	}
 	
 	protected boolean pushCSTopicsToZanata(final RESTTopicCollectionV1 topics, final RESTTopicV1 contentSpecTopic, final ContentSpec contentSpec)
 	{
 		final Map<RESTTopicV1, Document> topicToDoc = new HashMap<RESTTopicV1, Document>();
 		boolean error = false;
 		final ZanataInterface zanataInterface = new ZanataInterface();
 		
 		// Convert all the topics to DOM Documents first so we know if any are invalid
 		for (final RESTTopicV1 topic : topics.getItems())
 		{
 			/*
 			 * make sure the section title is the same as the
 			 * topic title
 			 */
 			Document doc = null;
 			try
 			{
 				doc = XMLUtilities.convertStringToDocument(topic.getXml());
 			}
 			catch (Exception e)
 			{
 				// Do Nothing as we handle the error below.
 			}
 			
 			if (doc == null)
 			{
 				JCommander.getConsole().println("ERROR: Topic ID " + topic.getId() + ", Revison " + topic.getRevision() + " does not have valid XML");
 				error = true;
 			}
 			else
 			{
 				DocBookUtilities.setSectionTitle(topic.getTitle(), doc);
 				topicToDoc.put(topic, doc);
 			}
 			
 			// Good point to check for a shutdown
 			if (isAppShuttingDown())
 			{
 				shutdown.set(true);
 				return false;
 			}
 		}
 		
 		// Good point to check for a shutdown
 		if (isAppShuttingDown())
 		{
 			shutdown.set(true);
 			return false;
 		}
 		
 		final int total = topics.getItems().size() + 1;
 		int current = 0;
 		final int showPercent = 5;
 		int lastPercent = 0;
 		
 		JCommander.getConsole().println("You are about to push " + total + " topics to zanata. Continue? (Yes/No)");
 		String answer = JCommander.getConsole().readLine();
 		
 		if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y"))
 		{
 			// Loop through each topic and upload it to zanata
 			for (final RESTTopicV1 topic : topicToDoc.keySet())
 			{
 				++current;
 				final int percent = Math.round(current / total * 100);
 				if (percent - lastPercent >= showPercent)
 				{
 					lastPercent = percent;
 					JCommander.getConsole().println("\tPushing topics to zanata " + percent + "% Done");
 				}
 				
 				final Document doc = topicToDoc.get(topic);
 				final String zanataId = topic.getId() + "-" + topic.getRevision();
 				
 				/*
 				 * deleting existing resources is useful for debugging,
 				 * but not for production
 				 */
 				final boolean zanataFileExists = zanataInterface.getZanataResourceExists(zanataId);
 
 				if (!zanataFileExists)
 				{
 					final Resource resource = new Resource();
 	
 					resource.setContentType(ContentType.TextPlain);
 					resource.setLang(LocaleId.fromJavaName(topic.getLocale()));
 					resource.setName(zanataId);
 					resource.setRevision(1);
 					resource.setType(ResourceType.FILE);
 	
 					final List<StringToNodeCollection> translatableStrings = XMLUtilities.getTranslatableStrings(doc, false);
 
 					for (final StringToNodeCollection translatableStringData : translatableStrings)
 					{
 						final String translatableString = translatableStringData.getTranslationString();
 						if (!translatableString.trim().isEmpty())
 						{										
 							final TextFlow textFlow = new TextFlow();
 							textFlow.setContent(translatableString);
 							textFlow.setLang(LocaleId.fromJavaName(topic.getLocale()));
 							textFlow.setId(createId(topic, translatableString));
 							textFlow.setRevision(1);
 
 							resource.getTextFlows().add(textFlow);
 						}
 					}
 
					zanataInterface.createFile(resource);
 				}
 				else
 				{
 					JCommander.getConsole().println("Topic ID " + topic.getId() + ", Revision " + topic.getRevision() + " already exists - Skipping.");
 				}
 			}
 			// Upload the content specification to zanata
 			final String zanataId = contentSpecTopic.getId() + "-" + contentSpecTopic.getRevision();
 			final boolean zanataFileExists = zanataInterface.getZanataResourceExists(zanataId);
 
 			if (!zanataFileExists)
 			{
 				final Resource resource = new Resource();
 	
 				resource.setContentType(ContentType.TextPlain);
 				resource.setLang(LocaleId.fromJavaName(contentSpecTopic.getLocale()));
 				resource.setName(contentSpecTopic.getId() + "-" + contentSpecTopic.getRevision());
 				resource.setRevision(1);
 				resource.setType(ResourceType.FILE);
 	
 				final List<StringToCSNodeCollection> translatableStrings = ContentSpecUtilities.getTranslatableStrings(contentSpec, false);
 	
 				for (final StringToCSNodeCollection translatableStringData : translatableStrings)
 				{
 					final String translatableString = translatableStringData.getTranslationString();
 					if (!translatableString.trim().isEmpty())
 					{										
 						final TextFlow textFlow = new TextFlow();
 						textFlow.setContent(translatableString);
 						textFlow.setLang(LocaleId.fromJavaName(contentSpecTopic.getLocale()));
 						textFlow.setId(createId(contentSpecTopic, translatableString));
 						textFlow.setRevision(1);
 
 						resource.getTextFlows().add(textFlow);
 					}
 				}
 
 				zanataInterface.createFile(resource);
 			}
 			else
 			{
 				JCommander.getConsole().println("Content Spec ID " + contentSpecTopic.getId() + ", Revision " + contentSpecTopic.getRevision() + " already exists - Skipping.");
 			}
 		}
 
 		return !error;
 	}
 	
 	private static String createId(final RESTTopicV1 topic, final String text)
 	{
 		final String sep = "\u0000";
 		final String hashBase = text + sep + topic.getId() + sep + topic.getRevision();
 		return HashUtilities.generateMD5(hashBase);
 	}
 }
