 package org.jboss.pressgang.ccms.contentspec.processor;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.interfaces.ShutdownAbleApp;
 import org.jboss.pressgang.ccms.contentspec.processor.constants.ProcessorConstants;
 import org.jboss.pressgang.ccms.contentspec.processor.exceptions.ProcessingException;
 import org.jboss.pressgang.ccms.contentspec.processor.structures.ProcessingOptions;
 import org.jboss.pressgang.ccms.contentspec.processor.utils.ProcessorUtilities;
 import org.jboss.pressgang.ccms.contentspec.provider.DataProviderFactory;
 import org.jboss.pressgang.ccms.contentspec.provider.PropertyTagProvider;
 import org.jboss.pressgang.ccms.contentspec.provider.TagProvider;
 import org.jboss.pressgang.ccms.contentspec.provider.TopicProvider;
 import org.jboss.pressgang.ccms.contentspec.provider.TopicSourceURLProvider;
 import org.jboss.pressgang.ccms.contentspec.utils.EntityUtilities;
 import org.jboss.pressgang.ccms.contentspec.utils.TopicPool;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLogger;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.contentspec.wrapper.CategoryInTagWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.PropertyTagInTopicWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.TagWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.TopicSourceURLWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.TopicWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.UserWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.collection.UpdateableCollectionWrapper;
 
 /**
  * A class to fully process a Content Specification. It first parses the data using a ContentSpecParser,
  * then validates the Content Specification using a ContentSpecValidator and lastly saves the data to the database.
  * It can also be configured to only validate the data and not save it.
  *
  * @author lnewson
  */
 @SuppressWarnings("rawtypes")
 public class ContentSpecProcessor implements ShutdownAbleApp {
     private final Logger LOG = Logger.getLogger(ContentSpecProcessor.class.getPackage().getName() + ".CustomContentSpecProcessor");
 
     private final ErrorLogger log;
     private final TopicProvider topicProvider;
     private final TagProvider tagProvider;
     private final PropertyTagProvider propertyTagProvider;
     private final TopicSourceURLProvider topicSourceUrlProvider;
 
     private final ProcessingOptions processingOptions;
     private ContentSpecValidator validator;
     private final TopicPool topics;
     private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
     private final AtomicBoolean shutdown = new AtomicBoolean(false);
 
     /**
      * Constructor
      *
      * @param factory           A DBManager object that manages the REST connection and the functions to read/write to the REST Interface.
      * @param loggerManager
      * @param processingOptions The set of options to use when processing.
      */
     public ContentSpecProcessor(final DataProviderFactory factory, final ErrorLoggerManager loggerManager,
             final ProcessingOptions processingOptions) {
 
         topicProvider = factory.getProvider(TopicProvider.class);
         tagProvider = factory.getProvider(TagProvider.class);
         propertyTagProvider = factory.getProvider(PropertyTagProvider.class);
         topicSourceUrlProvider = factory.getProvider(TopicSourceURLProvider.class);
 
         log = loggerManager.getLogger(ContentSpecProcessor.class);
         topics = new TopicPool(factory);
         this.processingOptions = processingOptions;
         validator = new ContentSpecValidator(factory, loggerManager, processingOptions);
     }
 
     /**
      * Process a content specification so that it is parsed, validated and saved.
      *
      * @param contentSpec The Content Specification that is to be processed.
      * @param user        The user who requested the process operation.
      * @param mode        The mode to parse the content specification in.
      * @return True if everything was processed successfully otherwise false.
      */
    public boolean processContentSpec(final ContentSpec contentSpec, final UserWrapper user, final ContentSpecParser.ParsingMode mode) {
         return processContentSpec(contentSpec, user, mode, null);
     }
 
 
     /**
      * Process a content specification so that it is parsed, validated and saved.
      *
      * @param contentSpec    The Content Specification that is to be processed.
      * @param user           The user who requested the process operation.
      * @param mode           The mode to parse the content specification in.
      * @param overrideLocale Override the default locale using this parameter.
      * @return True if everything was processed successfully otherwise false.
      */
     @SuppressWarnings({"unchecked"})
     public boolean processContentSpec(final ContentSpec contentSpec, final UserWrapper user, final ContentSpecParser.ParsingMode mode,
            final String overrideLocale) {
         boolean editing = false;
         if (mode == ContentSpecParser.ParsingMode.EDITED) {
             editing = true;
         }
 
         // Set the user as the assigned writer
         contentSpec.setAssignedWriter(user == null ? null : user.getUsername());
 
         // Change the locale if the overrideLocale isn't null
         if (overrideLocale != null) {
             contentSpec.setLocale(overrideLocale);
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             shutdown.set(true);
             return false;
         }
 
         // Validate the content specification before doing any rest calls
         LOG.info("Starting first validation pass...");
 
         // Validate the relationships
         if (!validator.preValidateRelationships(contentSpec) || !validator.preValidateContentSpec(contentSpec)) {
             log.error(ProcessorConstants.ERROR_INVALID_CS_MSG);
             return false;
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             shutdown.set(true);
             return false;
         }
 
         // Validate the content specification now that we have most of the data from the REST API
         LOG.info("Starting second validation pass...");
 
         if (!validator.postValidateContentSpec(contentSpec, user)) {
             log.error(ProcessorConstants.ERROR_INVALID_CS_MSG);
             return false;
         } else {
             log.info(ProcessorConstants.INFO_VALID_CS_MSG);
 
             // If we aren't validating then save the content specification
             if (!processingOptions.isValidating()) {
                 // Check if the app should be shutdown
                 if (isShuttingDown.get()) {
                     shutdown.set(true);
                     return false;
                 }
 
                 LOG.info("Saving the Content Specification to the server...");
                 if (saveContentSpec(contentSpec, editing, user)) {
                     log.info(ProcessorConstants.INFO_SUCCESSFUL_SAVE_MSG);
                 } else {
                     return false;
                 }
             }
         }
         return true;
     }
 
     /**
      * Creates an entity to be sent through the REST interface to create or update a DB entry.
      *
      * @param specTopic The Content Specification Topic to create the topic entity from.
      * @return The new topic object if any changes where made otherwise null.
      * @throws Exception Any error that occurs when trying to build the new topic Entity.
      */
     protected TopicWrapper createTopicEntity(final SpecTopic specTopic) throws Exception {
         if (isShuttingDown.get()) {
             return null;
         }
 
         boolean changed = false;
 
         try {
             final UpdateableCollectionWrapper<PropertyTagInTopicWrapper> properties = propertyTagProvider.newPropertyTagInTopicCollection();
 
             TopicWrapper topic = null;
 
             // Create a Tag collection that will hold the tags for this topic entity
             CollectionWrapper<TagWrapper> topicTags = tagProvider.newTagCollection();
 
             if (specTopic.isTopicANewTopic()) {
                 // Create the topic entity.
                 topic = topicProvider.newTopic();
 
                 // Set the basics
                 topic.setTitle(specTopic.getTitle());
                 topic.setDescription(specTopic.getDescription(true));
                 topic.setXml("");
 
                 // Write the type
                 final CollectionWrapper<TagWrapper> tags = tagProvider.getTagsByName(specTopic.getType());
                 if (tags == null && tags.size() != 1) {
                     log.error(String.format(ProcessorConstants.ERROR_TYPE_NONEXIST_MSG, specTopic.getLineNumber(), specTopic.getText()));
                     return null;
                 }
 
                 // Add the type to the topic
                 topicTags.addNewItem(tags.getItems().get(0));
 
                 // Create the unique ID for the property
                 final PropertyTagInTopicWrapper cspProperty = propertyTagProvider.newPropertyTagInTopic();
                 cspProperty.setValue(Integer.toString(specTopic.getLineNumber()));
                 cspProperty.setId(CSConstants.CSP_PROPERTY_ID);
                 properties.addNewItem(cspProperty);
 
                 // Since this is a new topic the data has already changed
                 changed = true;
             } else if (specTopic.isTopicAClonedTopic()) {
                 // Get the existing topic from the database
                 int clonedId = Integer.parseInt(specTopic.getId().substring(1));
                 final TopicWrapper originalTopic = topicProvider.getTopic(clonedId, null);
                 topic = originalTopic.clone(true);
 
                 // Set the ID to null so a new ID will be created
                 topic.setId(null);
                 // Set other items to null that should be recreated
                 topic.setCreated(null);
                 topic.setLastModified(null);
                 // Set-up the configured parameters so that everything gets saved
                 topic.setTitle(topic.getTitle());
                 topic.setDescription(topic.getDescription());
                 topic.setHtml(topic.getHtml());
                 topic.setXml(topic.getXml());
 
                 // Go through each collection and set the "addItem" attribute to true
                 final CollectionWrapper<TopicWrapper> incomingTopics = topicProvider.newTopicCollection();
                 for (final TopicWrapper incomingRelationship : topic.getIncomingRelationships().getItems()) {
                     incomingTopics.addNewItem(incomingRelationship);
                 }
                 topic.setIncomingRelationships(incomingTopics);
 
                 final CollectionWrapper<TopicWrapper> outgoingTopics = topicProvider.newTopicCollection();
                 for (final TopicWrapper outgoingRelationship : topic.getOutgoingRelationships().getItems()) {
                     outgoingTopics.addNewItem(outgoingRelationship);
                 }
                 topic.setOutgoingRelationships(outgoingTopics);
 
                 final CollectionWrapper<TopicSourceURLWrapper> sourceUrls = topicSourceUrlProvider.newTopicSourceURLCollection(topic);
                 for (final TopicSourceURLWrapper sourceUrl : topic.getSourceURLs().getItems()) {
                     sourceUrl.setTitle(sourceUrl.getTitle());
                     sourceUrl.setDescription(sourceUrl.getTitle());
                     sourceUrl.setUrl(sourceUrl.getUrl());
                     sourceUrls.addNewItem(sourceUrl);
                 }
                 topic.setSourceURLs(sourceUrls);
 
                 final List<PropertyTagInTopicWrapper> propertyItems = topic.getProperties().getItems();
                 boolean cspPropertyFound = false;
                 for (final PropertyTagInTopicWrapper property : propertyItems) {
                     // Ignore the CSP Property ID as we will add a new one
                     if (!property.getId().equals(CSConstants.CSP_PROPERTY_ID)) {
                         properties.addNewItem(property);
                     } else {
                         cspPropertyFound = true;
 
                         property.setValue(Integer.toString(specTopic.getLineNumber()));
                         properties.addNewItem(property);
                     }
                 }
 
                 if (!cspPropertyFound) {
                     final PropertyTagInTopicWrapper cspProperty = propertyTagProvider.newPropertyTagInTopic();
                     cspProperty.setValue(Integer.toString(specTopic.getLineNumber()));
                     cspProperty.setId(CSConstants.CSP_PROPERTY_ID);
                     properties.addNewItem(cspProperty);
                 }
 
                 final List<TagWrapper> tags = topic.getTags().getItems();
                 for (final TagWrapper tag : tags) {
                     topicTags.addNewItem(tag);
                 }
 
                 // Since this is a new topic the data has already changed
                 changed = true;
             } else if (specTopic.isTopicAnExistingTopic()) {
                 final TopicWrapper originalTopic = topicProvider.getTopic(specTopic.getDBId(), null);
                 topic = originalTopic.clone(true);
 
                 // Remove any existing property tags
                 final List<PropertyTagInTopicWrapper> propertyItems = topic.getProperties().getItems();
                 boolean cspPropertyFound = false;
                 for (final PropertyTagInTopicWrapper property : propertyItems) {
                     // Remove the CSP Property ID as we will add a new one
                     if (property.getId().equals(CSConstants.CSP_PROPERTY_ID)) {
                         cspPropertyFound = true;
 
                         property.setValue(Integer.toString(specTopic.getLineNumber()));
                         properties.addUpdateItem(property);
                     }
                 }
 
                 if (!cspPropertyFound) {
                     final PropertyTagInTopicWrapper cspProperty = propertyTagProvider.newPropertyTagInTopic();
                     cspProperty.setValue(Integer.toString(specTopic.getLineNumber()));
                     cspProperty.setId(CSConstants.CSP_PROPERTY_ID);
                     properties.addNewItem(cspProperty);
                 }
             }
             topic.setProperties(properties);
 
             // Check if the app should be shutdown
             if (isShuttingDown.get()) {
                 shutdown.set(true);
                 return null;
             }
 
             if (!specTopic.isTopicAnExistingTopic()) {
                 // Set the assigned writer (Tag Table)
                 final List<TagWrapper> assignedWriterTags = tagProvider.getTagsByName(specTopic.getAssignedWriter(true)).getItems();
                 if (assignedWriterTags.size() != 1) {
                     log.error(String.format(ProcessorConstants.ERROR_WRITER_NONEXIST_MSG, specTopic.getLineNumber(), specTopic.getText()));
                     return null;
                 }
                 final TagWrapper writerTag = assignedWriterTags.get(0);
                 // Save a new assigned writer
                 topicTags.addNewItem(writerTag);
             }
 
             // Check if the app should be shutdown
             if (isShuttingDown.get()) {
                 shutdown.set(true);
                 return null;
             }
 
             // Get the tags for the topic
             List<String> tagNames = specTopic.getTags(true);
             final List<TagWrapper> tags = new ArrayList<TagWrapper>();
             for (final String tagName : tagNames) {
                 final List<TagWrapper> tagList = tagProvider.getTagsByName(tagName).getItems();
                 if (tagList.size() == 1) {
                     tags.add(tagList.get(0));
                 }
             }
             final Map<CategoryInTagWrapper, List<TagWrapper>> mapping = EntityUtilities.getCategoryMappingFromTagList(tags);
 
             // Check if the app should be shutdown
             if (isShuttingDown.get()) {
                 shutdown.set(true);
                 return null;
             }
 
             // If the topic is a cloned topic then only save new tags/urls
             if (specTopic.isTopicAClonedTopic()) {
                 // Save the new tags
                 // Find tags that aren't already in the database and adds them
                 final List<TagWrapper> tttList = topic.getTags().getItems();
                 for (final Entry<CategoryInTagWrapper, List<TagWrapper>> catEntry : mapping.entrySet()) {
                     for (final TagWrapper tag : catEntry.getValue()) {
                         boolean found = false;
                         for (final TagWrapper ttt : tttList) {
                             if (ttt.getId().equals(tag.getId())) {
                                 found = true;
                                 break;
                             }
                         }
 
                         if (!found) {
                             topicTags.addNewItem(tag);
                         }
                     }
                 }
 
                 // Check if the app should be shutdown
                 if (isShuttingDown.get()) {
                     shutdown.set(true);
                     return null;
                 }
 
                 // Remove the database tags for - tags
                 tagNames = specTopic.getRemoveTags(true);
                 final List<TagWrapper> removeTags = new ArrayList<TagWrapper>();
                 for (final String tagName : tagNames) {
                     final List<TagWrapper> tagList = tagProvider.getTagsByName(tagName).getItems();
                     if (tagList.size() == 1) {
                         tags.add(tagList.get(0));
                     }
                 }
 
                 for (final TagWrapper ttt : tttList) {
                     boolean found = false;
                     for (final TagWrapper tag : removeTags) {
                         if (ttt.getId().equals(tag.getId())) {
                             found = true;
                         }
                     }
 
                     if (found) {
                         // Set the tag to be removed from the database
                         topicTags.addRemoveItem(ttt);
                     }
 
                     // Remove the old writer tag as it will get replaced
                     if (ttt.containedInCategory(CSConstants.WRITER_CATEGORY_ID)) {
                         topicTags.addRemoveItem(ttt);
                     }
                 }
             } else if (specTopic.isTopicAnExistingTopic() && specTopic.getRevision() == null) {
                 // Finds tags that aren't already in the database and adds them
                 final List<TagWrapper> tttList = topic.getTags().getItems();
                 for (final Entry<CategoryInTagWrapper, List<TagWrapper>> cat : mapping.entrySet()) {
                     for (final TagWrapper tag : cat.getValue()) {
                         boolean found = false;
                         for (final TagWrapper ttt : tttList) {
                             if (ttt.getId().equals(tag.getId())) {
                                 found = true;
                                 break;
                             }
                         }
                         if (!found) {
                             topicTags.addNewItem(tag);
                         }
                     }
                 }
             } else {
                 // Save the tags
                 for (final Entry<CategoryInTagWrapper, List<TagWrapper>> cat : mapping.entrySet()) {
                     for (final TagWrapper tag : cat.getValue()) {
                         topicTags.addNewItem(tag);
                     }
                 }
             }
 
             // Check if the app should be shutdown
             if (isShuttingDown.get()) {
                 shutdown.set(true);
                 return null;
             }
 
             if (!specTopic.isTopicAnExistingTopic()) {
                 // Save the new Source Urls
                 final List<String> urls = specTopic.getSourceUrls();
 
                 final CollectionWrapper<TopicSourceURLWrapper> sourceUrls = topic.getSourceURLs() == null ? topicSourceUrlProvider
                         .newTopicSourceURLCollection(
                         topic) : topic.getSourceURLs();
 
                 for (final String url : urls) {
                     final TopicSourceURLWrapper sourceUrl = topicSourceUrlProvider.newTopicSourceURL(topic);
                     sourceUrl.setUrl(url);
                     sourceUrls.addNewItem(sourceUrl);
                 }
 
                 if (sourceUrls.getItems() != null && !sourceUrls.getItems().isEmpty()) {
                     topic.setSourceURLs(sourceUrls);
                     changed = true;
                 }
             }
 
             // Check if the app should be shutdown
             if (isShuttingDown.get()) {
                 shutdown.set(true);
                 return null;
             }
 
             if (topicTags.getItems() != null && !topicTags.getItems().isEmpty()) {
                 topic.setTags(topicTags);
                 changed = true;
             }
 
             if (changed) {
                 return topic;
             } else {
                 return null;
             }
         } catch (Exception e) {
             log.debug("", e);
             throw e;
         }
     }
 
     /**
      * Syncs all duplicated topics with their real topic counterpart in the content specification.
      *
      * @param specTopics A HashMap of the all the topics in the Content Specification. The key is the Topics ID.
      */
     protected void syncDuplicatedTopics(final List<SpecTopic> specTopics) {
         for (final SpecTopic topic : specTopics) {
             // Sync the normal duplicates first
             if (topic.isTopicADuplicateTopic()) {
                 final String id = topic.getId();
                 final String temp = "N" + id.substring(1);
                 SpecTopic cloneTopic = null;
                 for (final SpecTopic specTopic : specTopics) {
                     final String key = specTopic.getId();
                     if (key.equals(temp)) {
                         cloneTopic = specTopic;
                         break;
                     }
                 }
                 topic.setDBId(cloneTopic.getDBId());
             }
             // Sync the duplicate cloned topics
             else if (topic.isTopicAClonedDuplicateTopic()) {
                 final String id = topic.getId();
                 final String idType = id.substring(1);
                 SpecTopic cloneTopic = null;
                 for (final SpecTopic specTopic : specTopics) {
                     final String key = specTopic.getId();
                     if (key.endsWith(idType) && !key.endsWith(id)) {
                         cloneTopic = specTopic;
                         break;
                     }
                 }
                 topic.setDBId(cloneTopic.getDBId());
 
             }
         }
     }
 
     /**
      * Saves the Content Specification and all of the topics in the content specification
      *
      * @param contentSpec The Content Specification to be saved.
      * @param edit        Whether the content specification is being edited or created.
      * @param user        The User who requested the Content Spec be saved.
      * @return True if the topic saved successfully otherwise false.
      */
     public boolean saveContentSpec(final ContentSpec contentSpec, final boolean edit, final UserWrapper user) {
         try {
             // Get the full text representation of the processed content spec
             final StringBuilder fullText = new StringBuilder("");
             for (final String line : contentSpec.getPreProcessedText()) {
                 fullText.append(line + "\n");
             }
 
             // A new content specification
             if (contentSpec.getId() == 0) {
                 contentSpec.setId(createContentSpec(contentSpec.getTitle(), fullText.toString(), contentSpec.getDtd(), user.getUsername()));
                 if (contentSpec.getId() == 0) {
                     log.error(ProcessorConstants.ERROR_DATABASE_ERROR_MSG);
                     throw new Exception("Failed to create the pre content specification.");
                 }
             }
             // An existing content specification
             else {
                 if (!updateContentSpec(contentSpec.getId(), contentSpec.getTitle(), fullText.toString(), contentSpec.getDtd())) {
                     log.error(ProcessorConstants.ERROR_DATABASE_ERROR_MSG);
                     throw new Exception("Failed to create the pre content specification.");
                 }
             }
 
             // Create the new topic entities
             final List<SpecTopic> specTopics = contentSpec.getSpecTopics();
             for (final SpecTopic specTopic : specTopics) {
 
                 // Check if the app should be shutdown
                 if (isShuttingDown.get()) {
                     shutdown.set(true);
                     throw new Exception("Shutdown Requested");
                 }
 
                 // Add topics to the TopicPool that need to be added or updated
                 if (specTopic.getId().matches("(" + CSConstants.NEW_TOPIC_ID_REGEX + "|" + CSConstants.CLONED_TOPIC_ID_REGEX + ")")) {
                     try {
                         final TopicWrapper topic = createTopicEntity(specTopic);
                         if (topic != null) {
                             topics.addNewTopic(topic);
                         }
                     } catch (Exception e) {
                         throw new ProcessingException("Failed to create topic: " + specTopic.getId());
                     }
                 } else if (specTopic.isTopicAnExistingTopic() && !specTopic.getTags(true).isEmpty() && specTopic.getRevision() == null) {
                     try {
                         final TopicWrapper topic = createTopicEntity(specTopic);
                         if (topic != null) {
                             topics.addUpdatedTopic(topic);
                         }
                     } catch (Exception e) {
                         throw new ProcessingException("Failed to create topic: " + specTopic.getId());
                     }
                 }
             }
 
             // Check if the app should be shutdown
             if (isShuttingDown.get()) {
                 shutdown.set(true);
                 throw new Exception("Shutdown Requested");
             }
 
             // From here on the main saving happens so this shouldn't be interrupted
 
             // Save the new topic entities
             if (!topics.savePool()) {
                 log.error(ProcessorConstants.ERROR_DATABASE_ERROR_MSG);
                 throw new Exception("Failed to save the pool of topics.");
             }
 
             // Initialise the new and cloned topics using the populated topic pool
             for (final SpecTopic specTopic : specTopics) {
                 topics.initialiseFromPool(specTopic);
             }
 
             // Sync the Duplicated Topics (ID = X<Number>)
             syncDuplicatedTopics(specTopics);
 
             // Create the post processed content spec
             final Map<String, SpecTopic> specTopicMap = new HashMap<String, SpecTopic>();
             for (final SpecTopic specTopic : specTopics) {
                 specTopicMap.put(specTopic.getId(), specTopic);
             }
             final String postCS = ProcessorUtilities.generatePostContentSpec(contentSpec, specTopicMap);
             if (postCS == null) {
                 throw new ProcessingException("Failed to create the Post Content Specification.");
             }
 
             // Validate that the content specification was processed correctly
             if (!validatePostProcessedSpec(postCS)) {
                 throw new ProcessingException("Failed to create the Post Content Specification.");
             }
 
             if (!updatePostContentSpec(contentSpec.getId(), postCS)) {
                 log.error(ProcessorConstants.ERROR_DATABASE_ERROR_MSG);
                 throw new Exception("Failed to save the post content Specification");
             }
         } catch (ProcessingException e) {
             // Clean up the data that was created
             if (contentSpec.getId() != 0 && !edit) {
                 try {
                     topicProvider.deleteTopic(contentSpec.getId());
                 } catch (Exception e1) {
                     log.error("", e);
                 }
             }
             if (topics.isInitialised()) topics.rollbackPool();
             log.error(String.format("%s\n%7s%s", ProcessorConstants.ERROR_PROCESSING_ERROR_MSG, "", e.getMessage()));
         } catch (Exception e) {
             // Clean up the data that was created
             if (contentSpec.getId() != 0 && !edit) {
                 try {
                     topicProvider.deleteTopic(contentSpec.getId());
                 } catch (Exception e1) {
                     log.error("", e);
                 }
             }
             if (topics.isInitialised()) topics.rollbackPool();
             log.debug("", e);
             return false;
         }
         return true;
     }
 
     /**
      * Checks a post processed content specification to ensure that no new, cloned or duplicated
      * topics exist in the content specification as they should have been resolved to
      * existing topics.
      *
      * @param postProcessedSpec The post processed content specification.
      * @return True if no invalid topics were found, otherwise false
      */
     private boolean validatePostProcessedSpec(final String postProcessedSpec) {
         Pattern newTopicPattern = Pattern.compile("(#.*)?\\[[ ]*N[0-9]*[ ]*,.*?\\]");
         Matcher matcher = newTopicPattern.matcher(postProcessedSpec);
 
         while (matcher.find()) {
             final String match = matcher.group();
             if (!match.contains("#")) return false;
         }
 
         Pattern clonedTopicPattern = Pattern.compile("(#.*)?\\[[ ]*C[0-9]+.*?\\]");
         matcher = clonedTopicPattern.matcher(postProcessedSpec);
 
         while (matcher.find()) {
             final String match = matcher.group();
             if (!match.contains("#")) return false;
         }
 
         Pattern duplicateTopicPattern = Pattern.compile("(#.*)?\\[[ ]*X[0-9]+.*?\\]");
         matcher = duplicateTopicPattern.matcher(postProcessedSpec);
 
         while (matcher.find()) {
             final String match = matcher.group();
             if (!match.contains("#")) return false;
         }
 
         Pattern duplicateClonedTopicPattern = Pattern.compile("(#.*)?\\[[ ]*XC[0-9]+.*?\\]");
         matcher = duplicateClonedTopicPattern.matcher(postProcessedSpec);
 
         while (matcher.find()) {
             final String match = matcher.group();
             if (!match.contains("#")) return false;
         }
 
         return true;
     }
 
     /**
      * Writes a ContentSpecs tuple to the database using the data provided.
      *
      * @param title
      * @param preContentSpec
      * @param dtd
      * @param createdBy
      * @return
      */
     protected Integer createContentSpec(final String title, final String preContentSpec, final String dtd, final String createdBy) {
         try {
             TopicWrapper contentSpec = topicProvider.newTopic();
             contentSpec.setTitle(title);
             contentSpec.setXml(preContentSpec);
 
             // Create the Added By, Content Spec Type and DTD property tags
             final UpdateableCollectionWrapper<PropertyTagInTopicWrapper> properties = propertyTagProvider.newPropertyTagInTopicCollection();
             final PropertyTagInTopicWrapper addedBy = propertyTagProvider.newPropertyTagInTopic(
                     propertyTagProvider.getPropertyTag(CSConstants.ADDED_BY_PROPERTY_TAG_ID));
             addedBy.setValue(createdBy);
 
             final PropertyTagInTopicWrapper typePropertyTag = propertyTagProvider.newPropertyTagInTopic(
                     propertyTagProvider.getPropertyTag(CSConstants.CSP_TYPE_PROPERTY_TAG_ID));
             typePropertyTag.setValue(CSConstants.CSP_PRE_PROCESSED_STRING);
 
             final PropertyTagInTopicWrapper dtdPropertyTag = propertyTagProvider.newPropertyTagInTopic(
                     propertyTagProvider.getPropertyTag(CSConstants.DTD_PROPERTY_TAG_ID, null));
             dtdPropertyTag.setValue(dtd);
 
             properties.addNewItem(addedBy);
             properties.addNewItem(dtdPropertyTag);
             properties.addNewItem(typePropertyTag);
 
             contentSpec.setProperties(properties);
 
             // Add the Content Specification Type Tag
             final CollectionWrapper<TagWrapper> tags = tagProvider.newTagCollection();
             final TagWrapper typeTag = tagProvider.getTag(CSConstants.CONTENT_SPEC_TAG_ID);
             tags.addNewItem(typeTag);
 
             contentSpec.setTags(tags);
 
             contentSpec = topicProvider.createTopic(contentSpec);
             if (contentSpec != null) return contentSpec.getId();
         } catch (Exception e) {
             log.error("", e);
         }
         return null;
     }
 
     /**
      * Updates a ContentSpecs tuple from the database using the data provided.
      */
     public boolean updateContentSpec(final Integer id, final String title, final String preContentSpec, final String dtd) {
         try {
             TopicWrapper contentSpec = topicProvider.getTopic(id);
 
             if (contentSpec == null) return false;
 
             // Change the title if it's different
             if (!contentSpec.getTitle().equals(title)) {
                 contentSpec.setTitle(title);
             }
 
             contentSpec.setXml(preContentSpec);
 
             // Update the Content Spec Type and DTD property tags
             final UpdateableCollectionWrapper<PropertyTagInTopicWrapper> properties = contentSpec.getProperties();
             UpdateableCollectionWrapper<PropertyTagInTopicWrapper> fixedProperties = propertyTagProvider.newPropertyTagInTopicCollection();
             if (properties.getItems() != null && !properties.getItems().isEmpty()) {
                 boolean foundCSPType = false;
 
                 // Loop through and remove any Type or DTD tags if they don't
                 // match
                 for (final PropertyTagInTopicWrapper property : properties.getItems()) {
                     if (property.getId().equals(CSConstants.CSP_TYPE_PROPERTY_TAG_ID)) {
                         property.setValue(CSConstants.CSP_PRE_PROCESSED_STRING);
                         fixedProperties.addUpdateItem(property);
                         foundCSPType = true;
                     } else if (property.getId().equals(CSConstants.DTD_PROPERTY_TAG_ID)) {
                         if (!property.getValue().equals(dtd)) {
                             property.setValue(dtd);
                             fixedProperties.addUpdateItem(property);
                         } else {
                             fixedProperties.addItem(property);
                         }
                     } else {
                         fixedProperties.addItem(property);
                     }
                 }
 
                 if (!foundCSPType) {
                     // The property tag should never match a pre tag
                     final PropertyTagInTopicWrapper typePropertyTag = propertyTagProvider.newPropertyTagInTopic(
                             propertyTagProvider.getPropertyTag(CSConstants.CSP_TYPE_PROPERTY_TAG_ID));
                     typePropertyTag.setValue(CSConstants.CSP_PRE_PROCESSED_STRING);
 
                     fixedProperties.addNewItem(typePropertyTag);
                 }
             }
 
             contentSpec.setProperties(fixedProperties);
 
             contentSpec = topicProvider.updateTopic(contentSpec);
             if (contentSpec != null) {
                 return true;
             }
         } catch (Exception e) {
             log.error("", e);
         }
         return false;
     }
 
     /**
      * Writes a ContentSpecs tuple to the database using the data provided.
      */
     public boolean updatePostContentSpec(final Integer id, final String postContentSpec) {
         try {
             TopicWrapper contentSpec = topicProvider.getTopic(id);
             if (contentSpec == null) return false;
 
             contentSpec.setXml(postContentSpec);
 
             // Update Content Spec Type
             final UpdateableCollectionWrapper<PropertyTagInTopicWrapper> properties = contentSpec.getProperties();
             UpdateableCollectionWrapper<PropertyTagInTopicWrapper> fixedProperties = propertyTagProvider.newPropertyTagInTopicCollection();
             if (properties.getItems() != null && !properties.getItems().isEmpty()) {
                 // Loop through and remove the type
                 for (final PropertyTagInTopicWrapper property : properties.getItems()) {
                     if (property.getId().equals(CSConstants.CSP_TYPE_PROPERTY_TAG_ID)) {
                         property.setValue(CSConstants.CSP_POST_PROCESSED_STRING);
                         fixedProperties.addUpdateItem(property);
                     } else {
                         fixedProperties.addItem(property);
                     }
                 }
 
                 contentSpec.setProperties(fixedProperties);
             }
 
             contentSpec = topicProvider.updateTopic(contentSpec);
             if (contentSpec != null) {
                 return true;
             }
         } catch (Exception e) {
             log.error("", e);
         }
         return false;
     }
 
     @Override
     public void shutdown() {
         isShuttingDown.set(true);
         if (validator != null) {
             validator.shutdown();
         }
     }
 
     @Override
     public boolean isShutdown() {
         return shutdown.get();
     }
 }
