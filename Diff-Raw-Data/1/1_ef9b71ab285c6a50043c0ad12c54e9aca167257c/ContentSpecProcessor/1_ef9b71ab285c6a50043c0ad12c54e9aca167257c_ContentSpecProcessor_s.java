 package org.jboss.pressgang.ccms.contentspec.processor;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.log4j.Logger;
 import org.jboss.pressgang.ccms.contentspec.Comment;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.KeyValueNode;
 import org.jboss.pressgang.ccms.contentspec.Level;
 import org.jboss.pressgang.ccms.contentspec.Node;
 import org.jboss.pressgang.ccms.contentspec.SpecNode;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.entities.ProcessRelationship;
 import org.jboss.pressgang.ccms.contentspec.entities.Relationship;
 import org.jboss.pressgang.ccms.contentspec.entities.TargetRelationship;
 import org.jboss.pressgang.ccms.contentspec.entities.TopicRelationship;
 import org.jboss.pressgang.ccms.contentspec.enums.BookType;
 import org.jboss.pressgang.ccms.contentspec.enums.RelationshipType;
 import org.jboss.pressgang.ccms.contentspec.interfaces.ShutdownAbleApp;
 import org.jboss.pressgang.ccms.contentspec.processor.constants.ProcessorConstants;
 import org.jboss.pressgang.ccms.contentspec.processor.exceptions.ProcessingException;
 import org.jboss.pressgang.ccms.contentspec.processor.structures.ProcessingOptions;
 import org.jboss.pressgang.ccms.contentspec.processor.utils.ProcessorUtilities;
 import org.jboss.pressgang.ccms.contentspec.provider.CSNodeProvider;
 import org.jboss.pressgang.ccms.contentspec.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.contentspec.provider.DataProviderFactory;
 import org.jboss.pressgang.ccms.contentspec.provider.PropertyTagProvider;
 import org.jboss.pressgang.ccms.contentspec.provider.TagProvider;
 import org.jboss.pressgang.ccms.contentspec.provider.TopicProvider;
 import org.jboss.pressgang.ccms.contentspec.provider.TopicSourceURLProvider;
 import org.jboss.pressgang.ccms.contentspec.utils.TopicPool;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLogger;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.contentspec.wrapper.CSNodeWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.CSRelatedNodeWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.PropertyTagInContentSpecWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.PropertyTagInTopicWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.PropertyTagWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.TagWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.TopicSourceURLWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.TopicWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.UserWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.contentspec.wrapper.collection.UpdateableCollectionWrapper;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 
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
     private final DataProviderFactory providerFactory;
 
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
 
         providerFactory = factory;
 
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
                 if (saveContentSpec(providerFactory, contentSpec, editing, user)) {
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
      * @param providerFactory
      * @param specTopic       The Content Specification Topic to create the topic entity from.
      * @return The new topic object if any changes where made otherwise null.
      * @throws ProcessingException
      */
     protected TopicWrapper createTopicEntity(final DataProviderFactory providerFactory,
             final SpecTopic specTopic) throws ProcessingException {
         // Duplicates reference another new or cloned topic and should not have a different new/updated underlying topic
         if (specTopic.isTopicAClonedDuplicateTopic() || specTopic.isTopicADuplicateTopic()) return null;
 
         final TagProvider tagProvider = providerFactory.getProvider(TagProvider.class);
         final TopicSourceURLProvider topicSourceURLProvider = providerFactory.getProvider(TopicSourceURLProvider.class);
 
         if (isShuttingDown.get()) {
             return null;
         }
 
         // If the spec topic is a clone or new topic then it will have changed no mater what else is done, since it's a new topic
         boolean changed = specTopic.isTopicAClonedTopic() || specTopic.isTopicANewTopic();
         final TopicWrapper topic = getTopicForSpecTopic(providerFactory, specTopic);
 
         // Check if the topic is null, if so throw an exception as it shouldn't be at this stage
         if (topic == null) {
             throw new ProcessingException("Creating a topic failed.");
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             return null;
         }
 
         // Process the topic and add or remove any tags
         if (processTopicTags(tagProvider, specTopic, topic)) {
             changed = true;
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             return null;
         }
 
         // Process and set the assigned writer for new and cloned topics
         if (!specTopic.isTopicAnExistingTopic()) {
             processAssignedWriter(tagProvider, specTopic, topic);
             changed = true;
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             return null;
         }
 
         // Process and set the source urls for new and cloned topics
         if (!specTopic.isTopicAnExistingTopic()) {
             if (processTopicSourceUrls(topicSourceURLProvider, specTopic, topic)) changed = true;
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             return null;
         }
 
         if (changed) {
             return topic;
         } else {
             return null;
         }
     }
 
     /**
      * Gets or creates the underlying Topic Entity for a spec topic.
      *
      * @param providerFactory
      * @param specTopic       The spec topic to get the topic entity for.
      * @return The topic entity if one could be found, otherwise null.
      */
     protected TopicWrapper getTopicForSpecTopic(final DataProviderFactory providerFactory, final SpecTopic specTopic) {
         TopicWrapper topic = null;
 
         if (specTopic.isTopicANewTopic()) {
             topic = getTopicForNewSpecTopic(providerFactory, specTopic);
         } else if (specTopic.isTopicAClonedTopic()) {
             topic = ProcessorUtilities.cloneTopic(providerFactory, specTopic);
         } else if (specTopic.isTopicAnExistingTopic()) {
             topic = getTopicForExistingSpecTopic(providerFactory, specTopic);
         }
 
         return topic;
     }
 
     /**
      * C
      *
      * @param providerFactory
      * @param specTopic
      * @return
      */
     private TopicWrapper getTopicForNewSpecTopic(final DataProviderFactory providerFactory, final SpecTopic specTopic) {
         final TopicProvider topicProvider = providerFactory.getProvider(TopicProvider.class);
         final TagProvider tagProvider = providerFactory.getProvider(TagProvider.class);
         final PropertyTagProvider propertyTagProvider = providerFactory.getProvider(PropertyTagProvider.class);
 
         // Create the topic entity.
         final TopicWrapper topic = topicProvider.newTopic();
 
         // Set the basics
         topic.setTitle(specTopic.getTitle());
         topic.setDescription(specTopic.getDescription(true));
         topic.setXml("");
         topic.setXmlDoctype(CommonConstants.DOCBOOK_45);
         topic.setLocale(CommonConstants.DEFAULT_LOCALE);
 
         // Write the type
         final CollectionWrapper<TagWrapper> tags = tagProvider.getTagsByName(specTopic.getType());
         if (tags == null || tags.size() != 1) {
             log.error(String.format(ProcessorConstants.ERROR_TYPE_NONEXIST_MSG, specTopic.getLineNumber(), specTopic.getText()));
             return null;
         }
 
         // Add the type to the topic
         topic.setTags(tagProvider.newTagCollection());
         topic.getTags().addNewItem(tags.getItems().get(0));
 
         // Create the unique ID for the property
         topic.setProperties(propertyTagProvider.newPropertyTagInTopicCollection());
         final PropertyTagWrapper propertyTag = propertyTagProvider.getPropertyTag(CSConstants.CSP_PROPERTY_ID);
         final PropertyTagInTopicWrapper cspProperty = propertyTagProvider.newPropertyTagInTopic(propertyTag);
         cspProperty.setValue(specTopic.getUniqueId());
         topic.getProperties().addNewItem(cspProperty);
 
         // Add the added by property tag
         final String assignedWriter = specTopic.getAssignedWriter(true);
         if (assignedWriter != null) {
             final PropertyTagWrapper addedByPropertyTag = propertyTagProvider.getPropertyTag(CSConstants.ADDED_BY_PROPERTY_TAG_ID);
             final PropertyTagInTopicWrapper addedByProperty = propertyTagProvider.newPropertyTagInTopic(addedByPropertyTag);
             addedByProperty.setValue(assignedWriter);
             topic.getProperties().addNewItem(addedByProperty);
         }
 
         return topic;
     }
 
     /**
      * @param providerFactory
      * @param specTopic
      * @return
      */
     private TopicWrapper getTopicForExistingSpecTopic(final DataProviderFactory providerFactory, final SpecTopic specTopic) {
         final TopicProvider topicProvider = providerFactory.getProvider(TopicProvider.class);
         final PropertyTagProvider propertyTagProvider = providerFactory.getProvider(PropertyTagProvider.class);
 
         // Get the current existing topic
         final TopicWrapper topic = topicProvider.getTopic(specTopic.getDBId(), null);
 
         // Update the CSP Property tag
         final UpdateableCollectionWrapper<PropertyTagInTopicWrapper> properties = propertyTagProvider.newPropertyTagInTopicCollection();
         final List<PropertyTagInTopicWrapper> propertyItems = topic.getProperties().getItems();
         boolean cspPropertyFound = false;
         for (final PropertyTagInTopicWrapper property : propertyItems) {
             // Update the CSP Property Tag if it exists, otherwise add a new one
             if (property.getId().equals(CSConstants.CSP_PROPERTY_ID)) {
                 cspPropertyFound = true;
 
                 property.setValue(specTopic.getUniqueId());
                 properties.addUpdateItem(property);
             } else {
                 properties.addItem(property);
             }
         }
 
         if (!cspPropertyFound) {
             final PropertyTagWrapper cspPropertyTag = propertyTagProvider.getPropertyTag(CSConstants.CSP_PROPERTY_ID);
             final PropertyTagInTopicWrapper cspProperty = propertyTagProvider.newPropertyTagInTopic(cspPropertyTag);
             cspProperty.setValue(specTopic.getUniqueId());
             cspProperty.setId(CSConstants.CSP_PROPERTY_ID);
             properties.addNewItem(cspProperty);
         }
 
         topic.setProperties(properties);
 
         return topic;
     }
 
     /**
      * Process a Spec Topic and add or remove tags defined by the spec topic.
      *
      * @param tagProvider
      * @param specTopic   The spec topic that represents the changes to the topic.
      * @param topic       The topic entity to be updated.
      * @return True if anything in the topic entity was changed, otherwise false.
      */
     protected boolean processTopicTags(final TagProvider tagProvider, final SpecTopic specTopic, final TopicWrapper topic) {
         boolean changed = false;
 
         // Get the tags for the topic
         final List<String> addTagNames = specTopic.getTags(true);
         final List<TagWrapper> addTags = new ArrayList<TagWrapper>();
         for (final String addTagName : addTagNames) {
             final List<TagWrapper> tagList = tagProvider.getTagsByName(addTagName).getItems();
             if (tagList.size() == 1) {
                 addTags.add(tagList.get(0));
             }
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             return changed;
         }
 
         // Process the tags depending on the topic type
         if (specTopic.isTopicAClonedTopic()) {
             if (processClonedTopicTags(tagProvider, specTopic, topic, addTags)) changed = true;
         } else if (specTopic.isTopicAnExistingTopic() && specTopic.getRevision() == null) {
             if (processExistingTopicTags(tagProvider, topic, addTags)) changed = true;
         } else if (specTopic.isTopicANewTopic()) {
             if (processNewTopicTags(tagProvider, topic, addTags)) changed = true;
         }
 
         return changed;
     }
 
     /**
      * @param tagProvider
      * @param specTopic
      * @param topic
      * @param addTags
      * @return
      */
     private boolean processClonedTopicTags(final TagProvider tagProvider, final SpecTopic specTopic, final TopicWrapper topic,
             final List<TagWrapper> addTags) {
         // See if a new tag collection needs to be created
         if (addTags.size() > 0 && topic.getTags() == null) {
             topic.setTags(tagProvider.newTagCollection());
         }
 
         // Finds tags that aren't already in the database and adds them
         final List<TagWrapper> topicTagList = topic.getTags() == null ? new ArrayList<TagWrapper>() : topic.getTags().getItems();
 
         boolean changed = false;
         // Find tags that aren't already in the database and adds them
         for (final TagWrapper addTag : addTags) {
             boolean found = false;
             for (final TagWrapper topicTag : topicTagList) {
                 if (topicTag.getId().equals(addTag.getId())) {
                     found = true;
                     break;
                 }
             }
 
             if (!found) {
                 topic.getTags().addNewItem(addTag);
                 changed = true;
             }
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             return changed;
         }
 
         // Remove the database tags for - tags
         final List<String> removeTagNames = specTopic.getRemoveTags(true);
         final List<TagWrapper> removeTags = new ArrayList<TagWrapper>();
         for (final String removeTagName : removeTagNames) {
             final List<TagWrapper> tagList = tagProvider.getTagsByName(removeTagName).getItems();
             if (tagList.size() == 1) {
                 removeTags.add(tagList.get(0));
             }
         }
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             return changed;
         }
 
         // Iterate over the current tags and set any that should be removed. If they shouldn't be removed then add them as a normal list
         for (final TagWrapper topicTag : topicTagList) {
             boolean found = false;
             for (final TagWrapper removeTag : removeTags) {
                 if (topicTag.getId().equals(removeTag.getId())) {
                     found = true;
                 }
             }
 
             if (found) {
                 // Remove any current settings for the tag
                 topic.getTags().remove(topicTag);
                 // Set the tag to be removed from the database
                 topic.getTags().addRemoveItem(topicTag);
                 changed = true;
             }
         }
 
         return changed;
     }
 
     /**
      * @param tagProvider
      * @param topic
      * @param addTags
      * @return
      */
     private boolean processExistingTopicTags(final TagProvider tagProvider, final TopicWrapper topic, final List<TagWrapper> addTags) {
         boolean changed = false;
         // Finds tags that aren't already in the database and adds them
         final List<TagWrapper> topicTagList = topic.getTags() == null ? new ArrayList<TagWrapper>() : topic.getTags().getItems();
 
         // Copy the current tags into the new tag list
         final CollectionWrapper<TagWrapper> updatedTagCollection = tagProvider.newTagCollection();
         for (final TagWrapper topicTag : topicTagList) {
             updatedTagCollection.addItem(topicTag);
         }
 
         // Add the new tags to the updated tag list if they don't already exist
         for (final TagWrapper addTag : addTags) {
             boolean found = false;
             for (final TagWrapper topicTag : topicTagList) {
                 if (topicTag.getId().equals(addTag.getId())) {
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 updatedTagCollection.addNewItem(addTag);
                 changed = true;
             }
         }
 
         // If something has changed then set the new updated list for the topic
         if (changed) {
             topic.setTags(updatedTagCollection);
         }
 
         return changed;
     }
 
     /**
      * @param tagProvider
      * @param topic
      * @param addTags
      * @return
      */
     private boolean processNewTopicTags(final TagProvider tagProvider, final TopicWrapper topic, final List<TagWrapper> addTags) {
         boolean changed = false;
 
         // See if a new tag collection needs to be created
         if (addTags.size() > 0 && topic.getTags() == null) {
             topic.setTags(tagProvider.newTagCollection());
         }
 
         // Save the tags
         for (final TagWrapper addTag : addTags) {
             topic.getTags().addNewItem(addTag);
             changed = true;
         }
 
         return changed;
     }
 
     /**
      * Processes a Spec Topic and adds the assigned writer for the topic it represents.
      *
      * @param tagProvider
      * @param specTopic   The spec topic object that contains the assigned writer.
      * @param topic       The topic entity to be updated.
      * @return True if anything in the topic entity was changed, otherwise false.
      */
     protected void processAssignedWriter(final TagProvider tagProvider, final SpecTopic specTopic, final TopicWrapper topic) {
         // See if a new tag collection needs to be created
         if (topic.getTags() == null) {
             topic.setTags(tagProvider.newTagCollection());
         }
 
         // Set the assigned writer (Tag Table)
         final List<TagWrapper> assignedWriterTags = tagProvider.getTagsByName(specTopic.getAssignedWriter(true)).getItems();
         final TagWrapper writerTag = assignedWriterTags.get(0);
         // Save a new assigned writer
         topic.getTags().addNewItem(writerTag);
         // Some providers need the collection to be set to set flags for saving
         topic.setTags(topic.getTags());
     }
 
     /**
      * Processes a Spec Topic and adds any new Source Urls to the topic it represents.
      *
      * @param topicSourceURLProvider
      * @param specTopic              The spec topic object that contains the urls to add.
      * @param topic                  The topic entity to be updated.
      * @return True if anything in the topic entity was changed, otherwise false.
      */
     protected boolean processTopicSourceUrls(final TopicSourceURLProvider topicSourceURLProvider, final SpecTopic specTopic,
             final TopicWrapper topic) {
         boolean changed = false;
         // Save the new Source Urls
         final List<String> urls = specTopic.getSourceUrls(true);
 
         if (urls != null && !urls.isEmpty()) {
             final CollectionWrapper<TopicSourceURLWrapper> sourceUrls = topic.getSourceURLs() == null ? topicSourceURLProvider
                     .newTopicSourceURLCollection(
                     topic) : topic.getSourceURLs();
 
             // Iterate over the spec topic urls and add them
             for (final String url : urls) {
                 final TopicSourceURLWrapper sourceUrl = topicSourceURLProvider.newTopicSourceURL(topic);
                 sourceUrl.setUrl(url);
                 sourceUrls.addNewItem(sourceUrl);
             }
 
             topic.setSourceURLs(sourceUrls);
             changed = true;
         }
 
         return changed;
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
      * @param providerFactory
      * @param contentSpec     The Content Specification to be saved.
      * @param edit            Whether the content specification is being edited or created.
      * @param user            The User who requested the Content Spec be saved.
      * @return True if the topic saved successfully otherwise false.
      */
     protected boolean saveContentSpec(final DataProviderFactory providerFactory, final ContentSpec contentSpec, final boolean edit,
             final UserWrapper user) {
         final ContentSpecProvider contentSpecProvider = providerFactory.getProvider(ContentSpecProvider.class);
 
         try {
             // Create the new topic entities
             final List<SpecTopic> specTopics = contentSpec.getSpecTopics();
             for (final SpecTopic specTopic : specTopics) {
 
                 // Check if the app should be shutdown
                 if (isShuttingDown.get()) {
                     shutdown.set(true);
                     throw new ProcessingException("Shutdown Requested");
                 }
 
                 // Add topics to the TopicPool that need to be added or updated
                 if (specTopic.isTopicAClonedTopic() || specTopic.isTopicANewTopic()) {
                     try {
                         final TopicWrapper topic = createTopicEntity(providerFactory, specTopic);
                         if (topic != null) {
                             topics.addNewTopic(topic);
                         }
                     } catch (Exception e) {
                         throw new ProcessingException("Failed to create topic: " + specTopic.getId());
                     }
                 } else if (specTopic.isTopicAnExistingTopic() && !specTopic.getTags(true).isEmpty() && specTopic.getRevision() == null) {
                     try {
                         final TopicWrapper topic = createTopicEntity(providerFactory, specTopic);
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
                 throw new ProcessingException("Shutdown Requested");
             }
 
             // From here on the main saving happens so this shouldn't be interrupted
 
             // Save the new topic entities
             if (!topics.savePool()) {
                 log.error(ProcessorConstants.ERROR_DATABASE_ERROR_MSG);
                 throw new ProcessingException("Failed to save the pool of topics.");
             }
 
             // Initialise the new and cloned topics using the populated topic pool
             for (final SpecTopic specTopic : specTopics) {
                 topics.initialiseFromPool(specTopic);
             }
 
             // Sync the Duplicated Topics (ID = X<Number>)
             syncDuplicatedTopics(specTopics);
 
             // Save the content spec
             mergeAndSaveContentSpec(contentSpec, providerFactory, !edit, user);
         } catch (ProcessingException e) {
             if (providerFactory.isRollbackSupported()) {
                 providerFactory.rollback();
             } else {
                 // Clean up the data that was created
                 if (contentSpec.getId() != null && !edit) {
                     try {
                         contentSpecProvider.deleteContentSpec(contentSpec.getId());
                     } catch (Exception e1) {
                         log.error("Unable to clean up the Content Specification from the database.", e);
                     }
                 }
                 if (topics.isInitialised()) topics.rollbackPool();
             }
             log.error(ProcessorConstants.ERROR_PROCESSING_ERROR_MSG);
             return false;
         } catch (Exception e) {
             if (providerFactory.isRollbackSupported()) {
                 providerFactory.rollback();
             } else {
                 // Clean up the data that was created
                 if (contentSpec.getId() != null && !edit) {
                     try {
                         contentSpecProvider.deleteContentSpec(contentSpec.getId());
                     } catch (Exception e1) {
                         log.error("Unable to clean up the Content Specification from the database.", e);
                     }
                 }
                 if (topics.isInitialised()) topics.rollbackPool();
             }
             log.debug("", e);
             return false;
         }
         return true;
     }
 
     /**
      * Merges a Content Spec object with it's Database counterpart if one exists, otherwise it will create a new Database Entity if
      * create is set to true.
      *
      * @param contentSpec     The ContentSpec object to merge with the database entity.
      * @param providerFactory
      * @param create          If a new Content Spec Entity should be created if one doesn't exist.
      * @param user            The user who requested the save.
      * @throws Exception Thrown if a problem occurs saving to the database.
      */
     protected void mergeAndSaveContentSpec(final ContentSpec contentSpec, final DataProviderFactory providerFactory, boolean create,
             final UserWrapper user) throws Exception {
         // Get the providers
         final ContentSpecProvider contentSpecProvider = providerFactory.getProvider(ContentSpecProvider.class);
         final PropertyTagProvider propertyTagProvider = providerFactory.getProvider(PropertyTagProvider.class);
         final CSNodeProvider nodeProvider = providerFactory.getProvider(CSNodeProvider.class);
 
         // Create the temporary entity to store changes in and load the real entity if it exists.
         ContentSpecWrapper contentSpecEntity = null;
         if (contentSpec.getId() != null) {
             contentSpecEntity = contentSpecProvider.getContentSpec(contentSpec.getId());
         } else if (create) {
             contentSpecEntity = contentSpecProvider.newContentSpec();
 
             // setup the basic values
             contentSpecEntity.setLocale(CommonConstants.DEFAULT_LOCALE);
             contentSpecEntity.setType(BookType.getBookTypeId(contentSpec.getBookType()));
 
             // Add the added by property tag
             final UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper> propertyTagCollection = propertyTagProvider
                     .newPropertyTagInContentSpecCollection();
 
             // Create the new property tag
             final PropertyTagInContentSpecWrapper propertyTag = propertyTagProvider.newPropertyTagInContentSpec();
             propertyTag.setId(CSConstants.ADDED_BY_PROPERTY_TAG_ID);
             propertyTag.setValue(user.getUsername());
             propertyTagCollection.addNewItem(propertyTag);
 
             // Set the updated properties for the content spec
             contentSpecEntity.setProperties(propertyTagCollection);
         } else {
             throw new ProcessingException("Unable to find the existing Content Specification");
         }
 
         // Apply any changes to the content spec
         if (contentSpecEntity.getLocale() == null || !contentSpecEntity.getLocale().equals(contentSpec.getLocale())) {
             contentSpecEntity.setLocale(contentSpec.getLocale());
         }
 
         // Save the content spec entity so that we have a valid reference to add nodes to
         if (create) {
             contentSpecEntity = contentSpecProvider.createContentSpec(contentSpecEntity);
         } else {
             contentSpecEntity = contentSpecProvider.updateContentSpec(contentSpecEntity);
         }
 
         // Check that the content spec was updated/created successfully.
         if (contentSpecEntity == null) {
             throw new ProcessingException("Saving the updated Content Specification failed.");
         }
 
         // Get the list of transformable child nodes for processing
         final List<Node> nodes = getTransformableNodes(contentSpec.getNodes());
         nodes.addAll(getTransformableNodes(contentSpec.getBaseLevel().getChildNodes()));
 
         // Create the container to hold all the changed nodes
         final UpdateableCollectionWrapper<CSNodeWrapper> updatedCSNodes = nodeProvider.newCSNodeCollection();
 
         // Merge the base level and comments
         final Map<SpecNode, CSNodeWrapper> nodeMapping = new HashMap<SpecNode, CSNodeWrapper>();
         mergeChildren(nodes, contentSpecEntity.getChildren(), providerFactory, null, contentSpecEntity, updatedCSNodes, nodeMapping);
 
         // Merge the relationships now all spec topics have a mapping to a node
         mergeTopicRelationships(nodeMapping, providerFactory, updatedCSNodes);
 
         // Save the updated content spec nodes
         if (nodeProvider.updateCSNodes(updatedCSNodes) == null) {
             throw new ProcessingException("Saving the Content Specification contents failed.");
         }
     }
 
     /**
      * Merges the children nodes of a Content Spec level into the Content Spec Entity level. If any new nodes have to be created,
      * the base of the node will be created on the server and then the rest after that.
      *
      * @param childrenNodes       The child nodes to be merged.
      * @param entityChildrenNodes The current entity child nodes.
      * @param providerFactory
      * @param parentNode          The parent entity node that the nodes should be assigned to.
      * @param contentSpec         The content spec entity that the nodes belong to.
      * @param updatedCSNodes      A collection to store nodes that have been removed or updated. These node will be processed
      *                            later in one call.
      * @param nodeMapping         TODO
      * @throws Exception Thrown if an error occurs during saving new nodes.
      */
     /*
      * The reason that new nodes need to be saved is to set the next/previous entities, since they need to have an ID. This has to be
      * done this way due to the nature of serialising the object to JSON, otherwise you get a recursive loop until the JVM runs out of
      * memory.
      */
     protected void mergeChildren(final List<Node> childrenNodes, final CollectionWrapper<CSNodeWrapper> entityChildrenNodes,
             final DataProviderFactory providerFactory, final CSNodeWrapper parentNode, final ContentSpecWrapper contentSpec,
             final UpdateableCollectionWrapper<CSNodeWrapper> updatedCSNodes,
             final Map<SpecNode, CSNodeWrapper> nodeMapping) throws Exception {
         final CSNodeProvider nodeProvider = providerFactory.getProvider(CSNodeProvider.class);
 
         final List<CSNodeWrapper> processedNodes = new ArrayList<CSNodeWrapper>();
         final List<CSNodeWrapper> newNodes = new ArrayList<CSNodeWrapper>();
 
         // Update or create all of the children nodes that exist in the content spec
         CSNodeWrapper prevNode = null;
         for (final Node childNode : childrenNodes) {
             if (!isTransformableNode(childNode)) {
                 continue;
             }
 
             // Find the Entity Node that matches the Content Spec node, if one exists
             CSNodeWrapper foundNodeEntity = findExistingNode(childNode, entityChildrenNodes, processedNodes);
 
             // If the node was not found create a new one
             boolean changed = false;
             if (foundNodeEntity == null) {
                 final CSNodeWrapper newCSNodeEntity = nodeProvider.newCSNode();
                 if (childNode instanceof SpecTopic) {
                     mergeTopic((SpecTopic) childNode, newCSNodeEntity);
                     newCSNodeEntity.setNodeType(CommonConstants.CS_NODE_TOPIC);
                 } else if (childNode instanceof Level) {
                     mergeLevel((Level) childNode, newCSNodeEntity, providerFactory, contentSpec, updatedCSNodes, nodeMapping);
                     newCSNodeEntity.setNodeType(((Level) childNode).getType().getId());
                 } else if (childNode instanceof Comment) {
                     mergeComment((Comment) childNode, newCSNodeEntity);
                     newCSNodeEntity.setNodeType(CommonConstants.CS_NODE_COMMENT);
                 } else {
                     mergeMetaData((KeyValueNode<?>) childNode, newCSNodeEntity);
                     newCSNodeEntity.setNodeType(CommonConstants.CS_NODE_META_DATA);
                 }
 
                 // set the content spec for the new entity
                 newCSNodeEntity.setContentSpec(contentSpec);
 
                 // Save the basics of the node to get an id
                 foundNodeEntity = nodeProvider.createCSNode(newCSNodeEntity);
                 newNodes.add(foundNodeEntity);
             } else {
                 if (childNode instanceof SpecTopic) {
                     if (mergeTopic((SpecTopic) childNode, foundNodeEntity)) {
                         changed = true;
                     }
                 } else if (childNode instanceof Level) {
                     if (mergeLevel((Level) childNode, foundNodeEntity, providerFactory, contentSpec, updatedCSNodes, nodeMapping)) {
                         changed = true;
                     }
                 } else if (childNode instanceof Comment) {
                     if (mergeComment((Comment) childNode, foundNodeEntity)) {
                         changed = true;
                     }
                 } else {
                     if (mergeMetaData((KeyValueNode<?>) childNode, foundNodeEntity)) {
                         changed = true;
                     }
                 }
 
                 processedNodes.add(foundNodeEntity);
 
                 // setup the contentSpec for the entity
                 if (foundNodeEntity.getContentSpec() == null || !foundNodeEntity.getContentSpec().getId().equals(contentSpec.getId())) {
                     foundNodeEntity.setContentSpec(contentSpec);
                     changed = true;
                 }
             }
 
             // Set up the previous node relationship
             final Integer previousNodeId = prevNode == null ? null : prevNode.getId();
             if (foundNodeEntity.getPreviousNodeId() != null && !foundNodeEntity.getPreviousNodeId().equals(previousNodeId)) {
                 foundNodeEntity.setPreviousNodeId(previousNodeId);
                 changed = true;
             } else if (foundNodeEntity.getPreviousNodeId() == null && previousNodeId != null) {
                 foundNodeEntity.setPreviousNodeId(previousNodeId);
                 changed = true;
             }
 
             // Set up the next node relationship for the previous node
             if (prevNode != null && (prevNode.getNextNodeId() == null || (prevNode.getNextNodeId() != null && !prevNode.getNextNodeId()
                     .equals(
                     foundNodeEntity.getId())))) {
                 prevNode.setNextNodeId(foundNodeEntity.getId());
                 changed = true;
 
                 // Add the previous node to the updated collection if it's not already there
                 if (!updatedCSNodes.getItems().contains(prevNode)) {
                     updatedCSNodes.addUpdateItem(prevNode);
                 }
             }
 
             // setup the parent for the entity
             if (parentNode == null && foundNodeEntity.getParent() != null) {
                 foundNodeEntity.setParent(null);
                 changed = true;
             } else if (parentNode != null && (foundNodeEntity.getParent() == null || !foundNodeEntity.getParent().getId().equals(
                     parentNode.getId()))) {
                 foundNodeEntity.setParent(parentNode);
                 changed = true;
             }
 
             // The node has been updated so add it to the list of nodes to be saved
             if (changed) {
                 updatedCSNodes.addUpdateItem(foundNodeEntity);
             }
 
             // Add the node to the mapping of nodes to entity nodes
             if (childNode instanceof SpecNode) {
                 nodeMapping.put((SpecNode) childNode, foundNodeEntity);
             }
 
             // Set the previous node to the current node since processing is done
             prevNode = foundNodeEntity;
         }
 
         // If there is a previous node then make sure it's next node id is null as it's the last in the linked list
         if (prevNode != null && prevNode.getNextNodeId() != null) {
             prevNode.setNextNodeId(null);
         }
 
         // Loop over the entities current nodes and remove any that no longer exist
         if (entityChildrenNodes != null && !entityChildrenNodes.isEmpty()) {
             for (final CSNodeWrapper csNode : entityChildrenNodes.getItems()) {
                 // if the node wasn't processed then it no longer exists, so set it for removal
                 if (!processedNodes.contains(csNode)) {
                     updatedCSNodes.addRemoveItem(csNode);
                 }
             }
         }
     }
 
     /**
      * Finds the existing Entity Node that matches a ContentSpec node.
      *
      * @param childNode           The ContentSpec node to find a matching Entity node for.
      * @param entityChildrenNodes The entity child nodes to match from.
      * @param processedNodes      Any nodes that have already been processed, and should be excluded.
      * @return The matching entity if one exists, otherwise null.
      */
     protected CSNodeWrapper findExistingNode(final Node childNode, final CollectionWrapper<CSNodeWrapper> entityChildrenNodes,
             final List<CSNodeWrapper> processedNodes) {
         CSNodeWrapper foundNodeEntity = null;
         if (entityChildrenNodes != null && !entityChildrenNodes.isEmpty()) {
             for (final CSNodeWrapper nodeEntity : entityChildrenNodes.getItems()) {
                 // Check if the node has already been processed, if so then ignore it
                 if (processedNodes.contains(nodeEntity)) continue;
 
                 if (childNode instanceof SpecTopic && doesTopicMatch((SpecTopic) childNode, nodeEntity)) {
                     foundNodeEntity = nodeEntity;
                     break;
                 } else if (childNode instanceof Level && doesLevelMatch((Level) childNode, nodeEntity)) {
                     foundNodeEntity = nodeEntity;
                     break;
                 } else if (childNode instanceof Comment && doesCommentMatch((Comment) childNode, nodeEntity)) {
                     foundNodeEntity = nodeEntity;
                     break;
                 } else if (childNode instanceof KeyValueNode && doesMetaDataMatch((KeyValueNode<?>) childNode, nodeEntity)) {
                     foundNodeEntity = nodeEntity;
                     break;
                 }
             }
         }
 
         return foundNodeEntity;
     }
 
     /**
      * Merges a Content Specs meta data with a Content Spec Entities meta data
      *
      * @param metaData       The meta data object to be merged into a entity meta data object
      * @param metaDataEntity The meta data entity to merge with.
      * @return True if some value was changed, otherwise false.
      */
     protected boolean mergeMetaData(final KeyValueNode<?> metaData, final CSNodeWrapper metaDataEntity) {
         boolean changed = false;
 
         // Meta Data Title
         if (metaDataEntity.getTitle() == null || !metaDataEntity.getTitle().equals(metaData.getKey())) {
             metaDataEntity.setTitle(metaData.getKey());
             changed = true;
         }
 
         // Meta Data Value
         if (metaDataEntity.getAdditionalText() == null || !metaDataEntity.getAdditionalText().equals(metaData.getValue().toString())) {
             metaDataEntity.setAdditionalText(metaData.getValue().toString());
             changed = true;
         }
 
         return changed;
     }
 
     /**
      * @param level
      * @param levelEntity
      * @param providerFactory
      * @param contentSpec
      * @param updatedCSNodes
      * @param nodeMapping
      * @return True if some value was changed, otherwise false.
      * @throws Exception
      */
     protected boolean mergeLevel(final Level level, final CSNodeWrapper levelEntity, final DataProviderFactory providerFactory,
             final ContentSpecWrapper contentSpec, final UpdateableCollectionWrapper<CSNodeWrapper> updatedCSNodes,
             final Map<SpecNode, CSNodeWrapper> nodeMapping) throws Exception {
         boolean changed = false;
 
         // TITLE
         if (level.getTitle() != null && !level.getTitle().equals(levelEntity.getTitle())) {
             levelEntity.setTitle(level.getTitle());
             changed = true;
         }
 
         // TARGET ID
         if (level.getTargetId() != null && !level.getTargetId().equals(levelEntity.getTargetId())) {
             levelEntity.setTargetId(level.getTargetId());
             changed = true;
         } else if (level.getTargetId() == null && levelEntity.getTargetId() != null) {
             levelEntity.setTargetId(null);
             changed = true;
         }
 
         // CONDITION
         if (level.getConditionStatement() != null && !level.getConditionStatement().equals(levelEntity.getCondition())) {
             levelEntity.setCondition(level.getConditionStatement());
             changed = true;
         } else if (level.getConditionStatement() == null && levelEntity.getCondition() != null) {
             levelEntity.setCondition(null);
             changed = true;
         }
 
         // Merge the child nodes
         mergeChildren(getTransformableNodes(level.getChildNodes()), levelEntity.getChildren(), providerFactory, levelEntity, contentSpec,
                 updatedCSNodes, nodeMapping);
 
         return changed;
     }
 
     /**
      * @param specTopic
      * @param topicEntity
      * @return True if a some value was changed, otherwise false.
      */
     protected boolean mergeTopic(final SpecTopic specTopic, final CSNodeWrapper topicEntity) {
         boolean changed = false;
 
         // TITLE
         // No need to check if the topic title should be set to null, as it is a mandatory field
         if (specTopic.getTitle() != null && !specTopic.getTitle().equals(topicEntity.getTitle())) {
             topicEntity.setTitle(specTopic.getTitle());
             changed = true;
         }
 
         // TARGET ID
         if (!specTopic.isTargetIdAnInternalId()) {
             if (specTopic.getTargetId() != null && !specTopic.getTargetId().equals(topicEntity.getTargetId())) {
                 topicEntity.setTargetId(specTopic.getTargetId());
                 changed = true;
             } else if (specTopic.getTargetId() == null && topicEntity.getTargetId() != null) {
                 topicEntity.setTargetId(null);
                 changed = true;
             }
         }
 
         // CONDITION
         if (specTopic.getConditionStatement() != null && !specTopic.getConditionStatement().equals(topicEntity.getCondition())) {
             topicEntity.setCondition(specTopic.getConditionStatement());
             changed = true;
         } else if (specTopic.getConditionStatement() == null && topicEntity.getCondition() != null) {
             topicEntity.setCondition(null);
             changed = true;
         }
 
         // TOPIC ID
         // No need to check if the topic id should be set to null, as it is a mandatory field
         if (specTopic.getDBId() != null && !specTopic.getDBId().equals(topicEntity.getEntityId())) {
             topicEntity.setEntityId(specTopic.getDBId());
             changed = true;
         }
 
         // TOPIC REVISION
         if (specTopic.getRevision() != null && !specTopic.getRevision().equals(topicEntity.getEntityRevision())) {
             topicEntity.setEntityRevision(specTopic.getRevision());
             changed = true;
         } else if (specTopic.getRevision() == null && topicEntity.getEntityRevision() != null) {
             topicEntity.setEntityRevision(null);
             changed = true;
         }
 
         return changed;
     }
 
     /**
      * @param comment
      * @param commentEntity
      * @return
      */
     protected boolean mergeComment(final Comment comment, final CSNodeWrapper commentEntity) {
         if (commentEntity.getAdditionalText() == null || !commentEntity.getAdditionalText().equals(comment.getText())) {
             commentEntity.setAdditionalText(comment.getText());
             return true;
         }
 
         return false;
     }
 
     /**
      * Merges the relationships for all Spec Topics into their counterpart Entity nodes.
      *
      * @param nodeMapping     The mapping of Spec Nodes to Entity nodes.
      * @param providerFactory
      * @param updatedCSNodes  The list of nodes that have been flagged as needing updated, so any that need relationships updated can be
      *                        added.
      */
     protected void mergeTopicRelationships(final Map<SpecNode, CSNodeWrapper> nodeMapping, final DataProviderFactory providerFactory,
             final UpdateableCollectionWrapper<CSNodeWrapper> updatedCSNodes) {
         final CSNodeProvider nodeProvider = providerFactory.getProvider(CSNodeProvider.class);
 
         for (final Map.Entry<SpecNode, CSNodeWrapper> nodes : nodeMapping.entrySet()) {
             // Only process spec topics
             if (!(nodes.getKey() instanceof SpecTopic)) continue;
 
             // Get the matching spec topic and entity
             final SpecTopic specTopic = (SpecTopic) nodes.getKey();
             final CSNodeWrapper topicEntity = nodes.getValue();
 
             // Check if the node or entity have any relationships, if not then the node doesn't need to be merged
             if (!specTopic.getRelationships().isEmpty() || topicEntity.getRelatedToNodes() != null && !topicEntity.getRelatedToNodes()
                     .isEmpty()) {
                 // merge the relationships from the spec topic to the entity
                 mergeTopicRelationship(nodeMapping, specTopic, topicEntity, nodeProvider, updatedCSNodes);
             }
         }
     }
 
     /**
      * Merges the relationships from a Spec Topic into a Topic Node Entity.
      *
      * @param nodeMapping    The mapping of Spec Nodes to Entity nodes.
      * @param specTopic      The Spec Topic to get the relationships from.
      * @param topicEntity    The Entity to merge the relationships into.
      * @param nodeProvider   The provider factory for getting new or existing nodes.
      * @param updatedCSNodes The list of nodes that have been flagged as needing updated, so any that need relationships updated can be
      *                       added.
      */
     protected void mergeTopicRelationship(final Map<SpecNode, CSNodeWrapper> nodeMapping, final SpecTopic specTopic,
             final CSNodeWrapper topicEntity, final CSNodeProvider nodeProvider,
             final UpdateableCollectionWrapper<CSNodeWrapper> updatedCSNodes) {
         final UpdateableCollectionWrapper<CSRelatedNodeWrapper> updatedRelatedToNodes = nodeProvider.newCSRelatedNodeCollection();
         final List<CSRelatedNodeWrapper> processedRelationships = new ArrayList<CSRelatedNodeWrapper>();
 
         // Check to make sure that the spec topic has any relationships
         if (!specTopic.getRelationships().isEmpty()) {
             int relationshipSortCount = 1;
 
             for (final Relationship relationship : specTopic.getRelationships()) {
                 // All process relationships should not be stored
                 if (relationship instanceof ProcessRelationship) continue;
 
                 // See if the related node already exists
                 CSRelatedNodeWrapper foundRelatedNode = findExistingRelatedNode(relationship, topicEntity);
 
                 if (foundRelatedNode != null) {
                     // Found a node so update anything that might have changed
                     boolean updated = false;
                     if (foundRelatedNode.getRelationshipSort() != relationshipSortCount) {
                         foundRelatedNode.setRelationshipSort(relationshipSortCount);
                         updated = true;
                     }
 
                     // If the node was updated, set it's state in the collection to updated, otherwise just put it back in normally.
                     if (updated) {
                         updatedRelatedToNodes.addUpdateItem(foundRelatedNode);
                     } else {
                         updatedRelatedToNodes.addItem(foundRelatedNode);
                     }
 
                     // Add the related node to the list of processed nodes
                     processedRelationships.add(foundRelatedNode);
                 } else {
                     // No related node was found for the relationship so make a new one.
                     final CSNodeWrapper relatedNode;
                     if (relationship instanceof TargetRelationship) {
                         relatedNode = nodeMapping.get(((TargetRelationship) relationship).getSecondaryRelationship());
                     } else {
                         relatedNode = nodeMapping.get(((TopicRelationship) relationship).getSecondaryRelationship());
                     }
 
                     foundRelatedNode = nodeProvider.newCSRelatedNode(relatedNode);
                     foundRelatedNode.setRelationshipType(RelationshipType.getRelationshipTypeId(relationship.getType()));
                     foundRelatedNode.setRelationshipSort(relationshipSortCount);
 
                     updatedRelatedToNodes.addNewItem(foundRelatedNode);
                 }
 
                 // increment the sort counter
                 relationshipSortCount++;
             }
         }
 
         // Remove any existing relationships that are no longer valid
         final CollectionWrapper<CSRelatedNodeWrapper> topicRelatedNodes = topicEntity.getRelatedToNodes();
         if (topicRelatedNodes != null) {
             for (final CSRelatedNodeWrapper relatedNode : topicRelatedNodes.getItems()) {
                 // If the node was processed then ignore it, otherwise remove it
                 if (processedRelationships.contains(relatedNode)) {
                     continue;
                 } else {
                     updatedRelatedToNodes.addRemoveItem(relatedNode);
                 }
             }
         }
 
         // Check if anything was changed, if so then set the changes
         if (!updatedRelatedToNodes.getAddItems().isEmpty() || !updatedRelatedToNodes.getUpdateItems().isEmpty() || !updatedRelatedToNodes
                 .getRemoveItems().isEmpty()) {
             topicEntity.setRelatedToNodes(updatedRelatedToNodes);
 
             // Make sure the node is in the updated nodes collection
             if (!updatedCSNodes.getItems().contains(topicEntity)) {
                 updatedCSNodes.addUpdateItem(topicEntity);
             }
         }
     }
 
     /**
      * Finds an existing relationship for a topic.
      *
      * @param relationship The relationship to be found.
      * @param topicEntity  The topic to find the relationship from.
      * @return The related Entity, otherwise null if one can't be found.
      */
     protected CSRelatedNodeWrapper findExistingRelatedNode(final Relationship relationship, final CSNodeWrapper topicEntity) {
         final CollectionWrapper<CSRelatedNodeWrapper> topicRelatedNodes = topicEntity.getRelatedToNodes();
 
         CSRelatedNodeWrapper foundRelatedNode = null;
         if (topicRelatedNodes != null) {
             // Loop over the current nodes and see if any match
             for (final CSRelatedNodeWrapper relatedNode : topicRelatedNodes.getItems()) {
                 if (relationship instanceof TargetRelationship) {
                     if (doesRelationshipMatch((TargetRelationship) relationship, relatedNode)) {
                         foundRelatedNode = relatedNode;
                         break;
                     }
                 } else {
                     if (doesRelationshipMatch((TopicRelationship) relationship, relatedNode)) {
                         foundRelatedNode = relatedNode;
                         break;
                     }
                 }
             }
         }
 
         return foundRelatedNode;
     }
 
     /**
      * Gets a list of child nodes that can be transformed.
      *
      * @param childNodes The list of nodes to filter for translatable nodes.
      * @return A list of transformable nodes.
      */
     protected List<Node> getTransformableNodes(final List<Node> childNodes) {
         final List<Node> nodes = new LinkedList<Node>();
         for (final Node childNode : childNodes) {
             if (isTransformableNode(childNode)) {
                 nodes.add(childNode);
             }
         }
 
         return nodes;
     }
 
     /**
      * Checks to see if a node is a node that can be transformed and saved,
      *
      * @param childNode The node to be checked.
      * @return True if the node can be transformed, otherwise false.
      */
     protected boolean isTransformableNode(final Node childNode) {
         return childNode instanceof SpecNode || childNode instanceof Comment || childNode instanceof KeyValueNode || childNode instanceof
                 Level;
     }
 
     /**
      * Checks to see if a ContentSpec meta data matches a Content Spec Entity meta data.
      *
      * @param metaData The ContentSpec meta data object.
      * @param node     The Content Spec Entity topic.
      * @return True if the meta data is determined to match otherwise false.
      */
     protected boolean doesMetaDataMatch(final KeyValueNode<?> metaData, final CSNodeWrapper node) {
         if (!node.getNodeType().equals(CommonConstants.CS_NODE_META_DATA)) return false;
 
         // Check if the key matches, if it does than the nodes match.
         return metaData.getKey().equals(node.getTitle());
     }
 
     /**
      * Checks to see if a ContentSpec level matches a Content Spec Entity level.
      *
      * @param level The ContentSpec level object.
      * @param node  The Content Spec Entity level.
      * @return True if the level is determined to match otherwise false.
      */
     protected boolean doesLevelMatch(final Level level, final CSNodeWrapper node) {
         if (node.getNodeType().equals(CommonConstants.CS_NODE_COMMENT) || node.getNodeType().equals(CommonConstants.CS_NODE_TOPIC))
             return false;
 
         // If the unique id is not from the parser, than use the unique id to compare
         if (level.getUniqueId() != null && level.getUniqueId().matches("^\\d.*")) {
             return level.getUniqueId().equals(Integer.toString(node.getId()));
         } else {
             // Since a content spec doesn't contain the database ids for the nodes use what is available to see if the level matches
             if (node.getNodeType() != level.getType().getId()) return false;
 
             // If the target ids match then the level should be the same
             if (level.getTargetId() != null && level.getTargetId() == node.getTargetId()) {
                 return true;
             }
 
             return level.getTitle().equals(node.getTitle());
         }
     }
 
     /**
      * Checks to see if a ContentSpec topic matches a Content Spec Entity topic.
      *
      * @param specTopic The ContentSpec topic object.
      * @param node      The Content Spec Entity topic.
      * @return True if the topic is determined to match otherwise false.
      */
     protected boolean doesTopicMatch(final SpecTopic specTopic, final CSNodeWrapper node) {
         if (!node.getNodeType().equals(CommonConstants.CS_NODE_TOPIC)) return false;
 
         // If the unique id is not from the parser, in which case it will start with a number than use the unique id to compare
         if (specTopic.getUniqueId() != null && specTopic.getUniqueId().matches("^\\d.*")) {
             return specTopic.getUniqueId().equals(Integer.toString(node.getId()));
         } else {
             // Since a content spec doesn't contain the database ids for the nodes use what is available to see if the topics match
             return specTopic.getDBId().equals(node.getEntityId());
         }
     }
 
     /**
      * Checks to see if a ContentSpec topic relationship matches a Content Spec Entity topic.
      *
      * @param relationship The ContentSpec topic relationship object.
      * @param relatedNode  The related Content Spec Entity topic.
      * @return True if the topic is determined to match otherwise false.
      */
     protected boolean doesRelationshipMatch(final TopicRelationship relationship, final CSRelatedNodeWrapper relatedNode) {
         // If the relations ship is a TopicRelationship, then the related node must be a topic or its not a match
         if (!relatedNode.getNodeType().equals(CommonConstants.CS_NODE_TOPIC)) return false;
 
         // Check if the type matches first
         if (!RelationshipType.getRelationshipTypeId(relationship.getType()).equals(relatedNode.getRelationshipType())) return false;
 
         // If the unique id is not from the parser, in which case it will start with a number than use the unique id to compare
         if (relationship.getSecondaryRelationship().getUniqueId() != null && relationship.getSecondaryRelationship().getUniqueId().matches(
                 "^\\d.*")) {
             return relationship.getSecondaryRelationship().getUniqueId().equals(Integer.toString(relatedNode.getId()));
         } else {
             return relationship.getSecondaryRelationship().getDBId().equals(relatedNode.getEntityId());
         }
     }
 
     /**
      * Checks to see if a ContentSpec topic relationship matches a Content Spec Entity topic.
      *
      * @param relationship The ContentSpec topic relationship object.
      * @param relatedNode  The related Content Spec Entity topic.
      * @return True if the topic is determined to match otherwise false.
      */
     protected boolean doesRelationshipMatch(final TargetRelationship relationship, final CSRelatedNodeWrapper relatedNode) {
         // If the relationship node is a node that can't be related to (ie a comment or meta data), than the relationship cannot match
         if (relatedNode.getNodeType().equals(CommonConstants.CS_NODE_COMMENT) || relatedNode.getNodeType().equals(
                 CommonConstants.CS_NODE_META_DATA)) {
             return false;
         }
 
         // Check if the type matches first
         if (!RelationshipType.getRelationshipTypeId(relationship.getType()).equals(relatedNode.getRelationshipType())) return false;
 
         // If the unique id is not from the parser, in which case it will start with a number than use the unique id to compare
         if (relationship.getSecondaryRelationship().getUniqueId() != null && relationship.getSecondaryRelationship().getUniqueId().matches(
                 "^\\d.*")) {
             return relationship.getSecondaryRelationship().getUniqueId().equals(Integer.toString(relatedNode.getId()));
         } else if (relationship.getSecondaryRelationship() instanceof Level) {
             return ((Level) relationship.getSecondaryRelationship()).getTargetId().equals(relatedNode.getTargetId());
         } else if (relationship.getSecondaryRelationship() instanceof SpecTopic) {
             return ((SpecTopic) relationship.getSecondaryRelationship()).getTargetId().equals(relatedNode.getTargetId());
         } else {
             return false;
         }
     }
 
     /**
      * Checks to see if a ContentSpec comment matches a Content Spec Entity comment.
      *
      * @param comment The ContentSpec comment object.
      * @param node    The Content Spec Entity comment.
      * @return True if the comment is determined to match otherwise false.
      */
     protected boolean doesCommentMatch(final Comment comment, final CSNodeWrapper node) {
         return node.getNodeType() == CommonConstants.CS_NODE_COMMENT;
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
