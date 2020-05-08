 package com.redhat.contentspec.processor;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import com.redhat.contentspec.ContentSpec;
 import com.redhat.contentspec.Level;
 import com.redhat.contentspec.Process;
 import com.redhat.contentspec.SpecTopic;
 import com.redhat.contentspec.entities.Relationship;
 import com.redhat.contentspec.enums.LevelType;
 import com.redhat.contentspec.enums.RelationshipType;
 import com.redhat.contentspec.interfaces.ShutdownAbleApp;
 import com.redhat.contentspec.constants.CSConstants;
 import com.redhat.contentspec.processor.constants.ProcessorConstants;
 import com.redhat.contentspec.processor.structures.ProcessingOptions;
 import com.redhat.contentspec.processor.utils.ProcessorUtilities;
 import com.redhat.contentspec.rest.RESTManager;
 import com.redhat.contentspec.rest.RESTReader;
 import com.redhat.contentspec.utils.logging.ErrorLogger;
 import com.redhat.contentspec.utils.logging.ErrorLoggerManager;
 import com.redhat.ecs.commonutils.DocBookUtilities;
 import com.redhat.ecs.commonutils.HashUtilities;
 import com.redhat.ecs.constants.CommonConstants;
 import com.redhat.topicindex.rest.entities.BaseTopicV1;
 import com.redhat.topicindex.rest.entities.CategoryV1;
 import com.redhat.topicindex.rest.entities.TagV1;
 import com.redhat.topicindex.rest.entities.TopicV1;
 import com.redhat.topicindex.rest.entities.TranslatedTopicV1;
 
 /**
  * A class that is used to validate a Content Specification and the objects within a Content Specification. It provides methods for 
  * validating, ContentSpecs, Levels, Topics and Relationships.
  * 
  * @author lnewson
  *
  */
 public class ContentSpecValidator<T extends BaseTopicV1<T>> implements ShutdownAbleApp {
 	
 	private final RESTReader reader;
 	private final ErrorLogger log;
 	private final boolean permissiveMode;
 	private final boolean ignoreSpecRevisions;
 	private final boolean allowEmptyLevels;
 	private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
 	private final AtomicBoolean shutdown = new AtomicBoolean(false);
 	private final Class<T> clazz;
 	
 	private String locale;
 	
 	@Override
 	public void shutdown() {
 		isShuttingDown.set(true);
 	}
 
 	@Override
 	public boolean isShutdown() {
 		return shutdown.get();
 	}
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param elm An Error Logger Manager that is used to capture log messages.
 	 * @param restManager The manager that hands the rest communication.
 	 * @param permissiveMode Whether validation should be done in permissive mode.
 	 * @param ignoreSpecRevisions Whether the Checksum/SpecRevision attribute should be ignored.
 	 */
 	public ContentSpecValidator(final Class<T> clazz, final ErrorLoggerManager elm, final RESTManager restManager, final ProcessingOptions processingOptions) {
 		this.clazz = clazz;
 		log = elm.getLogger(ContentSpecValidator.class);
 		reader = restManager.getReader();
 		this.permissiveMode = processingOptions.isPermissiveMode();
 		this.ignoreSpecRevisions = processingOptions.isIgnoreSpecRevision();
 		this.allowEmptyLevels = processingOptions.isAllowEmptyLevels();
 		this.locale = CommonConstants.DEFAULT_LOCALE;
 	}
 	
 	@SuppressWarnings("deprecation")
 	/**
 	 * Validates that a Content Specification is valid by checking the META data, child levels and topics.
 	 * 
 	 * @param contentSpec The content specification to be validated.
 	 * @param specTopics The list of topics that exist within the content specification.
 	 * @return True if the content specification is valid, otherwise false.
 	 */
 	public boolean validateContentSpec(ContentSpec contentSpec, Map<String, SpecTopic> specTopics)
 	{
 		locale = contentSpec.getLocale() == null ? locale : contentSpec.getLocale();
 		
 		// Check if the app should be shutdown
 		if (isShuttingDown.get()) {
 			shutdown.set(true);
 			return false;
 		}
 		
 		boolean valid = true;
 		if (contentSpec.getTitle() == null || contentSpec.getTitle().equals("")) {
 			log.error(ProcessorConstants.ERROR_CS_NO_TITLE_MSG);
 			valid = false;
 		}
 		if (contentSpec.getProduct() == null || contentSpec.getProduct().equals("")) {
 			log.error(ProcessorConstants.ERROR_CS_NO_PRODUCT_MSG);
 			valid = false;
 		}
 		if (contentSpec.getVersion() == null || contentSpec.getVersion().equals("")) {
 			log.error(ProcessorConstants.ERROR_CS_NO_VERSION_MSG);
 			valid = false;
 		}
 		if (contentSpec.getPreProcessedText().isEmpty()) {
 			log.error(ProcessorConstants.ERROR_PROCESSING_ERROR_MSG);
 			valid = false;
 		}
 		if (contentSpec.getDtd() == null || contentSpec.getDtd().equals("")) {
 			log.error(ProcessorConstants.ERROR_CS_NO_DTD_MSG);
 			valid = false;
 		// Check that the DTD specified is a valid DTD format
 		} else if (!contentSpec.getDtd().toLowerCase().equals("docbook 4.5")) {
 			log.error(ProcessorConstants.ERROR_CS_INVALID_DTD_MSG);
 			valid = false;
 		}
 		if (contentSpec.getCreatedBy() == null) {
 			log.error(ProcessorConstants.ERROR_PROCESSING_ERROR_MSG);
 			valid = false;
 		}
 		if (contentSpec.getCopyrightHolder() == null || contentSpec.getCopyrightHolder().equals("")) {
 			log.error(ProcessorConstants.ERROR_CS_NO_COPYRIGHT_MSG);
 			valid = false;
 		}
 		// Check that the content specification isn't empty
 		if (contentSpec.getBaseLevel() == null) {
 			log.error(ProcessorConstants.ERROR_CS_EMPTY_MSG);
 			valid = false;
 		}
 		// If editing then check that the ID exists & the SpecRevision match
 		if (contentSpec.getId() != 0) {
			final TopicV1 contentSpecTopic = reader.getPostContentSpecById(contentSpec.getId(), null);
 			if (contentSpecTopic == null) {
 				log.error(String.format(ProcessorConstants.ERROR_INVALID_CS_ID_MSG, "ID=" + contentSpec.getId()));
 				valid = false;
 			}
 			
 			// Check that the revision is valid
 			if (!ignoreSpecRevisions && contentSpecTopic != null) {
 				String currentChecksum = HashUtilities.generateMD5(contentSpecTopic.getXml().replaceFirst("CHECKSUM[ ]*=.*(\r)?\n", ""));
 				if (contentSpec.getChecksum() != null) {
 					if (!contentSpec.getChecksum().equals(currentChecksum)) {
 						log.error(String.format(ProcessorConstants.ERROR_CS_NONMATCH_CHECKSUM_MSG, contentSpec.getChecksum(), currentChecksum));
 						valid = false;
 					}
 				} else if (contentSpec.getRevision() != null) {
 					// Check that the revision matches
 					int latestRev = reader.getLatestCSRevById(contentSpec.getId());
 					if (contentSpec.getRevision() != latestRev) {
 						log.error(String.format(ProcessorConstants.ERROR_CS_NONMATCH_SPEC_REVISION_MSG, contentSpec.getRevision(), latestRev));
 						valid = false;
 					}
 				} else {
 					log.error(String.format(ProcessorConstants.ERROR_CS_NONMATCH_CHECKSUM_MSG, null, currentChecksum));
 					valid = false;
 				}
 			}
 			
 			// Check that the Content Spec isn't read only
 			if (contentSpecTopic != null && contentSpecTopic.getProperty(CSConstants.CSP_READ_ONLY_PROPERTY_TAG_ID) != null) {
 				if (!contentSpecTopic.getProperty(CSConstants.CSP_READ_ONLY_PROPERTY_TAG_ID).getValue().matches("(^|.*,)" + contentSpec.getCreatedBy() + "(,.*|$)")) {
 					log.error(ProcessorConstants.ERROR_CS_READ_ONLY_MSG);
 					valid = false;
 				}
 			}
 		}
 		
 		// Check that the injection options are valid
 		if (contentSpec.getInjectionOptions() != null) {
 			for (String injectionType: contentSpec.getInjectionOptions().getStrictTopicTypes()) {
 				List<TagV1> tags = reader.getTagsByName(injectionType);
 				if (tags.size() == 1) {
 					if (!tags.get(0).isInCategory(CSConstants.TYPE_CATEGORY_ID)) {
 						log.error(String.format(ProcessorConstants.ERROR_INVALID_INJECTION_TYPE_MSG, injectionType));
 						valid = false;
 					}
 				} else {
 					log.error(String.format(ProcessorConstants.ERROR_INVALID_INJECTION_TYPE_MSG, injectionType));
 					valid = false;
 				}
 			}
 		}
 		
 		// Check that each level is valid
 		if (!validateLevel(contentSpec.getBaseLevel(), specTopics)) valid = false;
 		
 		// reset the locale back to its default
 		this.locale = CommonConstants.DEFAULT_LOCALE;
 
 		return valid;
 	}
 	
 	/**
 	 * Validate a set of relationships created when parsing.
 	 * 
 	 * @param relationships A list of all the relationships in a content specification.
 	 * @param specTopics The list of topics that exist within the content specification.
 	 * @param targetLevels The list of target levels in a content specification.
 	 * @param targetTopics The list of target topics in a content specification.
 	 * @return True if the relationships are valid, otherwise false.
 	 */
 	public boolean validateRelationships(final HashMap<String, List<Relationship>> relationships, HashMap<String, SpecTopic> specTopics, final HashMap<String, Level> targetLevels, final HashMap<String, SpecTopic> targetTopics) {
 		boolean error = false;
 		for(String topicId: relationships.keySet()) {
 			
 			// Check if the app should be shutdown
 			if (isShuttingDown.get()) {
 				shutdown.set(true);
 				return false;
 			}
 			
 			for (Relationship relationship: relationships.get(topicId)) {
 				
 				// Check if the app should be shutdown
 				if (isShuttingDown.get()) {
 					shutdown.set(true);
 					return false;
 				}
 				
 				String relatedId = relationship.getSecondaryRelationshipTopicId();
 				// The relationship points to a target so it must be a level or topic
 				if (relatedId.toUpperCase().matches(ProcessorConstants.TARGET_REGEX)) {
 					if (targetTopics.containsKey(relatedId) && !targetLevels.containsKey(relatedId)) {
 						// Nothing to validate here so do nothing
 					} else if (!targetTopics.containsKey(relatedId) && targetLevels.containsKey(relatedId)) {
 						if (relationship.getType() == RelationshipType.NEXT) {
 							log.error(String.format(ProcessorConstants.ERROR_NEXT_RELATED_LEVEL_MSG, specTopics.get(topicId).getLineNumber(), specTopics.get(topicId).getText()));
 							error = true;
 						} else if (relationship.getType() == RelationshipType.PREVIOUS) {
 							log.error(String.format(ProcessorConstants.ERROR_PREV_RELATED_LEVEL_MSG, specTopics.get(topicId).getLineNumber(), specTopics.get(topicId).getText()));
 							error = true;
 						}
 					} else {
 						log.error(String.format(ProcessorConstants.ERROR_TARGET_NONEXIST_MSG, specTopics.get(topicId).getLineNumber(), specTopics.get(topicId).getText()));
 						error = true;
 					}
 				// The relationship isn't a target so it must point to a topic directly
 				} else {
 					if (!relatedId.matches(CSConstants.NEW_TOPIC_ID_REGEX)) {
 						// The relationship isn't a unique new topic so it will contain the line number in front of the topic ID
 						if (relatedId.startsWith("X")) {
 							// Duplicated topics are never unique so throw an error straight away.
 							log.error(String.format(ProcessorConstants.ERROR_INVALID_DUPLICATE_RELATIONSHIP_MSG, specTopics.get(topicId).getLineNumber(), specTopics.get(topicId).getText()));
 							error = true;
 						} else {
 							int count = 0;
 							SpecTopic relatedTopic = null;
 							// Get the related topic and count if more then one is found
 							for (String specTopicId: specTopics.keySet()) {
 								if (specTopicId.matches("^[0-9]+-" + relatedId + "$")) {
 									relatedTopic = specTopics.get(specTopicId);
 									count++;
 								}
 							}
 							// Check to make sure the topic doesn't relate to itself
 							if (relatedTopic != specTopics.get(topicId)) {
 								if (count > 1) {
 									log.error(String.format(ProcessorConstants.ERROR_INVALID_RELATIONSHIP_MSG, specTopics.get(topicId).getLineNumber(), specTopics.get(topicId).getText()));
 									error = true;
 								} else if (count == 0) {
 									log.error(String.format(ProcessorConstants.ERROR_RELATED_TOPIC_NONEXIST_MSG, specTopics.get(topicId).getLineNumber(), specTopics.get(topicId).getText()));
 									error = true;
 								}
 							} else {
 								log.error(String.format(ProcessorConstants.ERROR_TOPIC_RELATED_TO_ITSELF_MSG, specTopics.get(topicId).getLineNumber(), specTopics.get(topicId).getText()));
 							}
 						}
 					} else {
 						if (specTopics.containsKey(relatedId)) {
 							// Check to make sure the topic doesn't relate to itself
 							if (specTopics.get(relatedId) == specTopics.get(topicId)) {
 								log.error(String.format(ProcessorConstants.ERROR_TOPIC_RELATED_TO_ITSELF_MSG, specTopics.get(topicId).getLineNumber(), specTopics.get(topicId).getText()));
 							}
 						} else {
 							log.error(String.format(ProcessorConstants.ERROR_RELATED_TOPIC_NONEXIST_MSG, specTopics.get(topicId).getLineNumber(), specTopics.get(topicId).getText()));
 							error = true;
 						}
 					}
 				}
 			}
 		}
 		return !error;
 	}
 	
 	/**
 	 * Validates a level to ensure its format and child levels/topics are valid.
 	 * 
 	 * @param level The level to be validated.
 	 * @param specTopics The list of topics that exist within the content specification.
 	 * @return True if the level is valid otherwise false.
 	 */
 	public boolean validateLevel(Level level, Map<String, SpecTopic> specTopics) {
 		
 		// Check if the app should be shutdown
 		if (isShuttingDown.get()) {
 			shutdown.set(true);
 			return false;
 		}
 		
 		boolean valid = true;
 		
 		// Check that the level isn't empty
 		if (level.getNumberOfSpecTopics() <= 0 && level.getNumberOfChildLevels() <= 0 && !allowEmptyLevels)
 		{
 			log.error(String.format(ProcessorConstants.ERROR_LEVEL_NO_TOPICS_MSG, level.getLineNumber(), level.getType().getTitle(), level.getType().getTitle(), level.getText()));
 			valid = false;
 		}
 		
 		if (level.getType() == null)
 		{
 			log.error(ProcessorConstants.ERROR_PROCESSING_ERROR_MSG);
 			valid = false;
 		}
 		
 		if (level.getTitle() == null || level.getTitle().equals(""))
 		{
 			log.error(String.format(ProcessorConstants.ERROR_LEVEL_NO_TITLE_MSG, level.getLineNumber(), level.getType().getTitle(), level.getText()));
 			valid = false;
 		}
 		
 		// Validate the sub levels
 		for (Level l: level.getChildLevels())
 		{
 			if (!validateLevel(l, specTopics)) valid = false;;
 		}
 		
 		// Validate the topics in this level
 		for (SpecTopic t: level.getSpecTopics())
 		{
 			if (!validateTopic(t, specTopics)) valid = false;;
 		}
 		
 		// Validate certain requirements depending on the type of level
 		switch (level.getType())
 		{
 		case APPENDIX:
 			if (!(level.getParent().getType() == LevelType.BASE || level.getParent().getType() == LevelType.PART))
 			{
 				log.error(String.format(ProcessorConstants.ERROR_CS_NESTED_APPENDIX_MSG, level.getLineNumber(), level.getText()));
 				valid = false;
 			}
 			break;
 		case CHAPTER:
 			if (!(level.getParent().getType() == LevelType.BASE || level.getParent().getType() == LevelType.PART))
 			{
 				log.error(String.format(ProcessorConstants.ERROR_CS_NESTED_CHAPTER_MSG, level.getLineNumber(), level.getText()));
 				valid = false;
 			}
 			break;
 		case PROCESS:
 			// Check that the process has no children
 			Process process = (Process) level;
 			if (process.getNumberOfChildLevels() != 0)
 			{
 				log.error(String.format(ProcessorConstants.ERROR_PROCESS_HAS_LEVELS_MSG, process.getLineNumber(), process.getText()));
 				valid = false;
 			}
 			break;
 		case PART:
 			if (level.getParent().getType() != LevelType.BASE)
 			{
 				log.error(String.format(ProcessorConstants.ERROR_CS_NESTED_PART_MSG, level.getLineNumber(), level.getText()));
 				valid = false;
 			}
 			break;
 		case SECTION:
 			if (!(level.getParent().getType() == LevelType.APPENDIX || level.getParent().getType() == LevelType.CHAPTER || level.getParent().getType() == LevelType.SECTION))
 			{
 				log.error(String.format(ProcessorConstants.ERROR_CS_SECTION_NO_CHAPTER_MSG, level.getLineNumber(), level.getText()));
 				valid = false;
 			}
 			break;
 		}
 		
 		return valid;
 	}
 	
 	/**
 	 * Validates a topic against the database and for formatting issues.
 	 * 
 	 * @param specTopic The topic to be validated.
 	 * @param specTopics The list of topics that exist within the content specification.
 	 * @return True if the topic is valid otherwise false.
 	 */
 	@SuppressWarnings("unchecked")
 	public boolean validateTopic(SpecTopic specTopic, Map<String, SpecTopic> specTopics) {
 		
 		// Check if the app should be shutdown
 		if (isShuttingDown.get()) {
 			shutdown.set(true);
 			return false;
 		}
 		
 		boolean valid = true;
 		
 		// Check that the topic exists in the spec by checking it's step
 		if (specTopic.getStep() == 0)
 		{
 			log.error(ProcessorConstants.ERROR_PROCESSING_ERROR_MSG);
 			valid = false;
 		}
 		
 		// Checks that the id isn't null and is a valid topic ID
 		if (specTopic.getId() == null || !specTopic.getId().matches(CSConstants.ALL_TOPIC_ID_REGEX))
 		{
 			log.error(String.format(ProcessorConstants.ERROR_INVALID_TOPIC_ID_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 			valid = false;
 		}
 		
 		// Check that the topic is inside a chapter/section/process/appendix
 		if (!(specTopic.getParent().getType() == LevelType.CHAPTER || specTopic.getParent().getType() == LevelType.APPENDIX 
 				|| specTopic.getParent().getType() == LevelType.PROCESS || specTopic.getParent().getType() == LevelType.SECTION))
 		{
 			log.error(String.format(ProcessorConstants.ERROR_TOPIC_OUTSIDE_CHAPTER_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 			valid = false;
 		}
 		
 		// Check that the title exists
 		if (specTopic.getTitle() == null || specTopic.getTitle().equals(""))
 		{
 			log.error(String.format(ProcessorConstants.ERROR_TOPIC_NO_TITLE_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 			valid = false;
 		}
 		// Check that it is valid when escaped
 		else if (DocBookUtilities.escapeTitle(specTopic.getTitle()).isEmpty())
 		{
 			log.error(String.format(ProcessorConstants.ERROR_INVALID_TOPIC_TITLE_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 			valid = false;
 		}
 		
 		// Check that we aren't using translations for anything but existing topics
 		if (!specTopic.isTopicAnExistingTopic())
 		{
 			// Check that we aren't processing translations
 			if (clazz == TranslatedTopicV1.class)
 			{
 				// TODO log an error about no new topics for translations
 				valid = false;
 			}
 		}
 		
 		// New Topics
 		if (specTopic.isTopicANewTopic())
 		{	
 			if (specTopic.getType() == null || specTopic.getType().equals(""))
 			{
 				log.error(String.format(ProcessorConstants.ERROR_TOPIC_NO_TYPE_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 				valid = false;
 			}
 			
 			// Check that the type entered exists
 			TagV1 type = reader.getTypeByName(specTopic.getType());
 			if (type == null)
 			{
 				log.error(String.format(ProcessorConstants.ERROR_TYPE_NONEXIST_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 				valid = false;
 			}
 			
 			// Validate the tags
 			if (!validateTopicTags(specTopic, specTopic.getTags(true)))
 			{
 				valid = false;
 			}
 			
 			//Check Assigned Writer exists
 			if (!validateAssignedWriter(specTopic)) valid = false;
 		}
 		// Existing Topics
 		else if (specTopic.isTopicAnExistingTopic())
 		{
 			// Check that the id actually exists
 			final T topic;
 			if (clazz == TopicV1.class)
 			{
 				topic = (T) reader.getTopicById(Integer.parseInt(specTopic.getId()), null);
 			}
 			else
 			{
 				topic = (T) reader.getTranslatedTopicByTopicId(Integer.parseInt(specTopic.getId()), locale);
 			}
 			
 			if (topic == null)
 			{
 				log.error(String.format(ProcessorConstants.ERROR_TOPIC_ID_NONEXIST_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 				return false;
 			}
 			else
 			{
 				specTopic.setTopic(topic);
 			}
 			
 			// Check that the topic has a valid id
 			if (topic.getId() <= 0)
 			{
 				log.error(String.format(ProcessorConstants.ERROR_TOPIC_ID_NONEXIST_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 				valid = false;
 			}
 			
 			// Validate the title matches if we aren't using permissive mode
 			if (!permissiveMode && !specTopic.getTitle().equals(topic.getTitle()))
 			{
 				String topicTitleMsg = "Topic " + specTopic.getId() + ": " + topic.getTitle();
 				log.error(String.format(ProcessorConstants.ERROR_TOPIC_TITLES_NONMATCH_MSG, specTopic.getPreProcessedLineNumber(), "Specified: " + specTopic.getText(), topicTitleMsg));
 				valid = false;
 			}
 			// If we are using permissive mode then change the title to the correct title
 			else if (permissiveMode && !specTopic.getTitle().equals(topic.getTitle()))
 			{
 				specTopic.setTitle(topic.getTitle());
 			}
 			
 			// Check that tags aren't trying to be removed
 			if (!specTopic.getRemoveTags(false).isEmpty())
 			{
 				log.error(String.format(ProcessorConstants.ERROR_TOPIC_EXISTING_TOPIC_CANNOT_REMOVE_TAGS, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 				valid = false;
 			}
 			
 			// Check that the assigned writer, description and source URLS haven't been set
 			if (specTopic.getAssignedWriter(false) != null || specTopic.getDescription(false) != null || !specTopic.getSourceUrls().isEmpty())
 			{
 				log.error(String.format(ProcessorConstants.ERROR_TOPIC_EXISTING_BAD_OPTIONS, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 				valid = false;
 			}
 			
 			// Validate the tags
 			if (!validateTopicTags(specTopic, specTopic.getTags(true)))
 			{
 				valid = false;
 			}
 		}
 		// Duplicated Topics
 		else if (specTopic.isTopicADuplicateTopic())
 		{
 			String temp = "N" + specTopic.getId().substring(1);
 			
 			// Check that the topic exists in the content specification
 			if (!specTopics.containsKey(temp))
 			{
 				log.error(String.format(ProcessorConstants.ERROR_TOPIC_NONEXIST_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 				valid = false;
 			}
 			else
 			{
 				// Check that the topic titles match the original
 				if (!specTopic.getTitle().equals(specTopics.get(temp).getTitle()))
 				{
 					String topicTitleMsg = "Topic " + specTopic.getId() + ": " + specTopics.get(temp).getTitle();
 					log.error(String.format(ProcessorConstants.ERROR_TOPIC_TITLES_NONMATCH_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText(), topicTitleMsg));
 					valid = false;
 				}
 			}
 		}
 		// Cloned Topics
 		else if (specTopic.isTopicAClonedTopic())
 		{
 			
 			// Check if a description or type exists. If one does then generate a warning.
 			if ((specTopic.getType() != null && !specTopic.getType().equals("")) || (specTopic.getDescription(false) != null && !specTopic.getDescription(false).equals("")))
 			{
 				String format = "";
 				if (specTopic.getType() != null && !specTopic.getType().equals(""))
 				{
 					format += String.format(ProcessorConstants.WARN_TYPE_IGNORE_MSG, specTopic.getPreProcessedLineNumber(), "Cloned");
 				}
 				
 				if (specTopic.getDescription(false) != null && !specTopic.getDescription(false).equals(""))
 				{
 					if (!format.equals("")) format += "\n       ";
 					format += String.format(ProcessorConstants.WARN_DESCRIPTION_IGNORE_MSG, specTopic.getPreProcessedLineNumber(), "Cloned");
 				}
 				
 				log.warn(String.format("%s" + ProcessorConstants.CSLINE_MSG, format, specTopic.getText()));
 			}
 			
 			// Get the original topic from the database
 			int temp = Integer.parseInt(specTopic.getId().substring(1));
 			TopicV1 topic = reader.getTopicById(temp, null);
 			
 			// Check that the original topic was found
 			if (topic == null)
 			{ 
 				log.error(String.format(ProcessorConstants.ERROR_TOPIC_NONEXIST_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 				valid = false;
 			}
 			else
 			{
 				// Validate the title matches if we aren't using permissive mode
 				if (!permissiveMode && !specTopic.getTitle().equals(topic.getTitle()))
 				{
 					String topicTitleMsg = "Topic " + topic.getId() + ": " + topic.getTitle();
 					log.error(String.format(ProcessorConstants.ERROR_TOPIC_TITLES_NONMATCH_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText(), topicTitleMsg));
 					valid = false;
 				}
 				// If we are using permissive mode then change the title to the correct title
 				else if (permissiveMode && !specTopic.getTitle().equals(topic.getTitle()))
 				{
 					specTopic.setTitle(topic.getTitle());
 				}
 				
 				//Check Assigned Writer exists
 				if (!validateAssignedWriter(specTopic)) valid = false;
 			}
 			
 			// Validate the tags
 			if (!validateTopicTags(specTopic, specTopic.getTags(true)))
 			{
 				valid = false;
 			}
 		// Duplicated Cloned Topics
 		}
 		else if (specTopic.isTopicAClonedDuplicateTopic())
 		{
 			// Find the duplicate topic in the content spec
 			String temp = specTopic.getId().substring(1);
 			int count = 0;
 			SpecTopic clonedTopic = null;
 			for (String topicId: specTopics.keySet())
 			{
 				if (topicId.endsWith(temp) && !topicId.endsWith(specTopic.getId()))
 				{
 					clonedTopic = specTopics.get(topicId);
 					count++;
 				}
 			}
 			
 			// Check that the topic exists
 			if (count == 0)
 			{
 				log.error(String.format(ProcessorConstants.ERROR_TOPIC_NONEXIST_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 				valid = false;
 			}
 			// Check that the referenced topic is unique
 			else if (count > 1)
 			{
 				log.error(String.format(ProcessorConstants.ERROR_TOPIC_DUPLICATE_CLONES_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 				valid = false;
 			}
 			else
 			{
 				// Check that the title matches
 				if (!specTopic.getTitle().equals(clonedTopic.getTitle()))
 				{
 					String topicTitleMsg = "Topic " + specTopic.getId() + ": " + clonedTopic.getTitle();
 					log.error(String.format(ProcessorConstants.ERROR_TOPIC_TITLES_NONMATCH_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText(), topicTitleMsg));
 					valid = false;
 				}
 			}
 		}
 		return valid;
 	}
 	
 	/**
 	 * Checks to make sure that the assigned writer for the topic is valid.
 	 * 
 	 * @return True if the assigned writer exists in the database and is under the Assigned Writer category otherwise false.
 	 */
 	private boolean validateAssignedWriter(SpecTopic topic)
 	{
 		if (topic.getAssignedWriter(true) == null)
 		{
 			log.error(String.format(ProcessorConstants.ERROR_NO_WRITER_MSG, topic.getPreProcessedLineNumber(), topic.getText()));
 			return false;
 		}
 		
 		//Check Assigned Writer exists
 		List<TagV1> tagList = reader.getTagsByName(topic.getAssignedWriter(true));
 		if (tagList.size() != 1)
 		{
 			log.error(String.format(ProcessorConstants.ERROR_WRITER_NONEXIST_MSG, topic.getPreProcessedLineNumber(), topic.getText()));
 			return false;
 		}
 		
 		// Check that the writer tag is actually part of the Assigned Writer category
 		CategoryV1 cat = reader.getCategoryByTagId(tagList.get(0).getId());
 		if (cat == null)
 		{
 			log.error(String.format(ProcessorConstants.ERROR_INVALID_WRITER_MSG, topic.getPreProcessedLineNumber(), topic.getText()));
 			return false;
 		}
 		
 		// Check that tag is actually in the Assigned Writer category
 		if (cat.getId() != ProcessorConstants.ASSIGNED_WRITER_CATEGORY_ID)
 		{
 			log.error(String.format(ProcessorConstants.ERROR_INVALID_WRITER_MSG, topic.getPreProcessedLineNumber(), topic.getText()));
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Checks to see if the tags are valid for a particular topic.
 	 * 
 	 * @param specTopic The topic the tags below to.
 	 * @param tagNames A list of all the tags in their string form to be validate.
 	 * @return True if the tags are valid otherwise false.
 	 */
 	private boolean validateTopicTags(SpecTopic specTopic, List<String> tagNames) {
 		boolean valid = true;
 		if (!tagNames.isEmpty())
 		{
 			List<TagV1> tags = new ArrayList<TagV1>();
 			for (String tagName: tagNames)
 			{
 				
 				// Check if the app should be shutdown
 				if (isShuttingDown.get()) {
 					shutdown.set(true);
 					return false;
 				}
 				
 				// Get the tag from the database
 				List<TagV1> tagList = reader.getTagsByName(tagName);
 				
 				// Check that it exists
 				if (tagList.size() == 1)
 				{
 					tags.add(tagList.get(0));
 				}
 				else if (tagList.size() == 0)
 				{
 					log.error(String.format(ProcessorConstants.ERROR_TAG_NONEXIST_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 					valid = false;
 				}
 				else
 				{
 					log.error(String.format(ProcessorConstants.ERROR_TOPIC_TAG_DUPLICATED_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 					valid = false;
 				}
 			}
 			Map<CategoryV1, List<TagV1>> mapping = ProcessorUtilities.getCategoryMappingFromTagList(tags);
 			
 			// Check that the mutex value entered is correct
 			for (CategoryV1 cat: mapping.keySet())
 			{
 				
 				// Check if the app should be shutdown
 				if (isShuttingDown.get())
 				{
 					shutdown.set(true);
 					return false;
 				}
 				
 				// Check that only one tag has been set if the category is mutually exclusive
 				if (cat.isMutuallyExclusive() && mapping.get(cat).size() > 1)
 				{
 					log.error(String.format(ProcessorConstants.ERROR_TOPIC_TOO_MANY_CATS_MSG, specTopic.getPreProcessedLineNumber(), cat.getName(), specTopic.getText()));
 					valid = false;
 				}
 				
 				// Check that the tag isn't a type or writer
 				if (cat.getId() == CSConstants.WRITER_CATEGORY_ID)
 				{
 					log.error(String.format(ProcessorConstants.ERROR_TOPIC_WRITER_AS_TAG_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 					valid = false;
 				}
 				
 				// Check that the tag isn't a topic type
 				if (cat.getId() == CSConstants.TYPE_CATEGORY_ID)
 				{
 					log.error(String.format(ProcessorConstants.ERROR_TOPIC_TYPE_AS_TAG_MSG, specTopic.getPreProcessedLineNumber(), specTopic.getText()));
 					valid = false;
 				}
 			}
 		}
 		return valid;
 	}
 }
