 package org.jboss.pressgang.ccms.contentspec.processor;
 
 import static com.google.common.base.Strings.isNullOrEmpty;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.jboss.pressgang.ccms.contentspec.Comment;
 import org.jboss.pressgang.ccms.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.contentspec.File;
 import org.jboss.pressgang.ccms.contentspec.FileList;
 import org.jboss.pressgang.ccms.contentspec.KeyValueNode;
 import org.jboss.pressgang.ccms.contentspec.Level;
 import org.jboss.pressgang.ccms.contentspec.Node;
 import org.jboss.pressgang.ccms.contentspec.SpecNode;
 import org.jboss.pressgang.ccms.contentspec.SpecTopic;
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.entities.BaseBugLinkOptions;
 import org.jboss.pressgang.ccms.contentspec.entities.ProcessRelationship;
 import org.jboss.pressgang.ccms.contentspec.entities.Relationship;
 import org.jboss.pressgang.ccms.contentspec.entities.TargetRelationship;
 import org.jboss.pressgang.ccms.contentspec.entities.TopicRelationship;
 import org.jboss.pressgang.ccms.contentspec.enums.BookType;
 import org.jboss.pressgang.ccms.contentspec.enums.BugLinkType;
 import org.jboss.pressgang.ccms.contentspec.enums.RelationshipType;
 import org.jboss.pressgang.ccms.contentspec.enums.TopicType;
 import org.jboss.pressgang.ccms.contentspec.interfaces.ShutdownAbleApp;
 import org.jboss.pressgang.ccms.contentspec.processor.constants.ProcessorConstants;
 import org.jboss.pressgang.ccms.contentspec.processor.exceptions.ProcessingException;
 import org.jboss.pressgang.ccms.contentspec.processor.structures.ProcessingOptions;
 import org.jboss.pressgang.ccms.contentspec.processor.utils.ProcessorUtilities;
 import org.jboss.pressgang.ccms.contentspec.utils.EntityUtilities;
 import org.jboss.pressgang.ccms.contentspec.utils.TopicPool;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLogger;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.docbook.compiling.BugLinkStrategy;
 import org.jboss.pressgang.ccms.docbook.processing.BugzillaBugLinkStrategy;
 import org.jboss.pressgang.ccms.docbook.processing.JIRABugLinkStrategy;
 import org.jboss.pressgang.ccms.provider.CSNodeProvider;
 import org.jboss.pressgang.ccms.provider.ContentSpecProvider;
 import org.jboss.pressgang.ccms.provider.DataProviderFactory;
 import org.jboss.pressgang.ccms.provider.PropertyTagProvider;
 import org.jboss.pressgang.ccms.provider.TagProvider;
 import org.jboss.pressgang.ccms.provider.TopicProvider;
 import org.jboss.pressgang.ccms.provider.TopicSourceURLProvider;
 import org.jboss.pressgang.ccms.provider.exception.ProviderException;
 import org.jboss.pressgang.ccms.utils.common.StringUtilities;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 import org.jboss.pressgang.ccms.wrapper.CSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.CSRelatedNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.LogMessageWrapper;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagInContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagInTopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagWrapper;
 import org.jboss.pressgang.ccms.wrapper.TagWrapper;
 import org.jboss.pressgang.ccms.wrapper.TopicSourceURLWrapper;
 import org.jboss.pressgang.ccms.wrapper.TopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A class to fully process a Content Specification. It first parses the data using a ContentSpecParser,
  * then validates the Content Specification using a ContentSpecValidator and lastly saves the data to the database.
  * It can also be configured to only validate the data and not save it.
  *
  * @author lnewson
  */
 @SuppressWarnings("rawtypes")
 public class ContentSpecProcessor implements ShutdownAbleApp {
     private final Logger LOG = LoggerFactory.getLogger(ContentSpecProcessor.class.getPackage().getName() + ".CustomContentSpecProcessor");
     private static final long WEEK_MILLI_SECS = 7 * 24 * 60 * 60 * 1000;
     private static final List<String> IGNORE_META_DATA = Arrays.asList(CommonConstants.CS_CHECKSUM_TITLE, CommonConstants.CS_ID_TITLE);
 
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
      * @param username    The user who requested the process operation.
      * @param mode        The mode to parse the content specification in.
      * @return True if everything was processed successfully otherwise false.
      */
     public boolean processContentSpec(final ContentSpec contentSpec, final String username, final ContentSpecParser.ParsingMode mode) {
         return processContentSpec(contentSpec, username, mode, (String) null);
     }
 
     /**
      * Process a content specification so that it is parsed, validated and saved.
      *
      * @param contentSpec The Content Specification that is to be processed.
      * @param username    The user who requested the process operation.
      * @param mode        The mode to parse the content specification in.
      * @param logMessage
      * @return True if everything was processed successfully otherwise false.
      */
     public boolean processContentSpec(final ContentSpec contentSpec, final String username, final ContentSpecParser.ParsingMode mode,
             final LogMessageWrapper logMessage) {
         return processContentSpec(contentSpec, username, mode, null, logMessage);
     }
 
     /**
      * Process a content specification so that it is parsed, validated and saved.
      *
      * @param contentSpec    The Content Specification that is to be processed.
      * @param username       The user who requested the process operation.
      * @param mode           The mode to parse the content specification in.
      * @param overrideLocale Override the default locale using this parameter.
      * @return True if everything was processed successfully otherwise false.
      */
     public boolean processContentSpec(final ContentSpec contentSpec, final String username, final ContentSpecParser.ParsingMode mode,
             final String overrideLocale) {
         return processContentSpec(contentSpec, username, mode, overrideLocale, null);
     }
 
     /**
      * Process a content specification so that it is parsed, validated and saved.
      *
      * @param contentSpec    The Content Specification that is to be processed.
      * @param username       The user who requested the process operation.
      * @param mode           The mode to parse the content specification in.
      * @param overrideLocale Override the default locale using this parameter.
      * @param logMessage
      * @return True if everything was processed successfully otherwise false.
      */
     public boolean processContentSpec(final ContentSpec contentSpec, final String username, final ContentSpecParser.ParsingMode mode,
             final String overrideLocale, final LogMessageWrapper logMessage) {
         boolean editing = false;
         if (mode == ContentSpecParser.ParsingMode.EDITED) {
             editing = true;
         }
 
         final ProcessorData processorData = new ProcessorData();
         processorData.setContentSpec(contentSpec);
         processorData.setUsername(username);
         processorData.setLogMessage(logMessage);
 
         // Set the username as the assigned writer if one doesn't exist
         if (isNullOrEmpty(contentSpec.getAssignedWriter())) {
             contentSpec.setAssignedWriter(username);
         }
 
         // Change the locale if the overrideLocale isn't null
         if (overrideLocale != null) {
             contentSpec.setLocale(overrideLocale);
         }
 
         // Set the log details user if one isn't set
         if (logMessage != null && username != null && logMessage.getUser() == null) {
             logMessage.setUser(CSConstants.UNKNOWN_USER_ID.toString());
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
 
         if (!postValidateContentSpec(processorData)) {
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
                 if (saveContentSpec(providerFactory, processorData, editing)) {
                     log.info(ProcessorConstants.INFO_SUCCESSFUL_SAVE_MSG);
                 } else {
                     log.error(ProcessorConstants.ERROR_PROCESSING_ERROR_MSG);
                     return false;
                 }
             }
         }
         return true;
     }
 
     /**
      * Does the post Validation step on a Content Spec and also re-validates the bug links if required.
      *
      * @param contentSpec The Content Specification to be saved.
      * @param username    The User who requested the Content Spec be validated.
      * @return True if the content spec is
      */
     protected boolean postValidateContentSpec(final ProcessorData processorData) {
         final ContentSpec contentSpec = processorData.getContentSpec();
         boolean valid = validator.postValidateContentSpec(contentSpec, processorData.getUsername());
 
         if (processingOptions.isValidateBugLinks()) {
             // Find out if we should re-validate bug links
             boolean reValidateBugLinks = true;
             if (contentSpec.getId() != null) {
                 ContentSpecWrapper contentSpecEntity = null;
                 try {
                     contentSpecEntity = providerFactory.getProvider(ContentSpecProvider.class).getContentSpec(contentSpec.getId(),
                             contentSpec.getRevision());
                 } catch (ProviderException e) {
 
                 }
 
                 // This shouldn't happen but if it does, then it'll be picked up in the postValidationContentSpec method
                 if (contentSpecEntity != null) {
                     final PropertyTagInContentSpecWrapper lastValidated = contentSpecEntity.getProperty(
                             ProcessorConstants.BUG_LINKS_LAST_VALIDATED_PROPERTY_TAG);
                     final Date now = new Date();
                     final long then;
                     if (lastValidated != null && lastValidated.getValue() != null && lastValidated.getValue().matches("^[0-9]+$")) {
                         then = Long.parseLong(lastValidated.getValue());
                     } else {
                         then = 0L;
                     }
 
                     // Check that a day has passed
                     if ((then + WEEK_MILLI_SECS) > now.getTime()) {
                         // A day hasn't passed so check to see if anything has changed
                         boolean changed = false;
                         if (contentSpec.isInjectBugLinks()) {
                             final String bugLinksValue = contentSpec.getBugLinksActualValue() == null ? null : contentSpec
                                     .getBugLinksActualValue().toString();
                             if (EntityUtilities.hasContentSpecMetaDataChanged(CommonConstants.CS_BUG_LINKS_TITLE, bugLinksValue,
                                     contentSpecEntity)) {
                                 changed = true;
                             } else {
                                 BugLinkStrategy bugLinkStrategy = null;
                                 BaseBugLinkOptions bugOptions = null;
                                 if (contentSpec.getBugLinks() == BugLinkType.JIRA) {
                                     bugLinkStrategy = new JIRABugLinkStrategy(contentSpec.getJIRAServer());
                                     bugOptions = contentSpec.getJIRABugLinkOptions();
                                 } else if (contentSpec.getBugLinks() == BugLinkType.BUGZILLA) {
                                     bugLinkStrategy = new BugzillaBugLinkStrategy(contentSpec.getBugzillaServer());
                                     bugOptions = contentSpec.getBugzillaBugLinkOptions();
                                 }
 
                                 if (bugLinkStrategy != null && bugLinkStrategy.hasValuesChanged(contentSpecEntity, bugOptions)) {
                                     changed = true;
                                 }
                             }
                         }
 
                         // If the content hasn't changed then don't re-validate the bug links
                         if (!changed) {
                             reValidateBugLinks = false;
                         }
                     }
                 }
             }
 
             // Validate the bug links
             if (reValidateBugLinks) {
                 processorData.setBugLinksReValidated(reValidateBugLinks);
                 LOG.info("Starting bug link validation pass...");
                 if (!validator.postValidateBugLinks(contentSpec, processingOptions.isStrictBugLinks())) {
                     valid = false;
                 }
             }
         }
 
         return valid;
     }
 
     /**
      * Saves the Content Specification and all of the topics in the content specification
      *
      * @param providerFactory
      * @param processorData   The data to be processed.
      * @param edit            Whether the content specification is being edited or created.
      * @return True if the topic saved successfully otherwise false.
      */
     protected boolean saveContentSpec(final DataProviderFactory providerFactory, final ProcessorData processorData, final boolean edit) {
         final ContentSpecProvider contentSpecProvider = providerFactory.getProvider(ContentSpecProvider.class);
 
         try {
             final List<SpecTopic> specTopics = processorData.getContentSpec().getSpecTopics();
 
             // Create the duplicate topic map
             final Map<SpecTopic, SpecTopic> duplicatedTopicMap = createDuplicatedTopicMap(specTopics);
 
             // Create the new topic entities
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
                         throw new ProcessingException("Failed to create topic: " + specTopic.getId(), e);
                     }
                 } else if (specTopic.isTopicAnExistingTopic() && !specTopic.getTags(true).isEmpty() && specTopic.getRevision() == null) {
                     try {
                         final TopicWrapper topic = createTopicEntity(providerFactory, specTopic);
                         if (topic != null) {
                             topics.addUpdatedTopic(topic);
                         }
                     } catch (Exception e) {
                         throw new ProcessingException("Failed to create topic: " + specTopic.getId(), e);
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
                 cleanSpecTopicWhenCreatedOrUpdated(specTopic);
             }
 
             // Sync the Duplicated Topics (ID = X<Number>)
             syncDuplicatedTopics(duplicatedTopicMap);
 
             // Save the content spec
             mergeAndSaveContentSpec(providerFactory, processorData, !edit);
         } catch (ProcessingException e) {
             LOG.debug("", e);
             if (providerFactory.isRollbackSupported()) {
                 providerFactory.rollback();
             } else {
                 // Clean up the data that was created
                 if (processorData.getContentSpec().getId() != null && !edit) {
                     try {
                         contentSpecProvider.deleteContentSpec(processorData.getContentSpec().getId());
                     } catch (Exception e1) {
                         log.error("Unable to clean up the Content Specification from the database.", e);
                     }
                 }
                 if (topics.isInitialised()) topics.rollbackPool();
             }
             return false;
         } catch (Exception e) {
             LOG.error("", e);
             if (providerFactory.isRollbackSupported()) {
                 providerFactory.rollback();
             } else {
                 // Clean up the data that was created
                 if (processorData.getContentSpec().getId() != null && !edit) {
                     try {
                         contentSpecProvider.deleteContentSpec(processorData.getContentSpec().getId());
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
      * Cleans a SpecTopic to reset any content that should be removed in a post processed content spec.
      *
      * @param specTopic
      */
     protected void cleanSpecTopicWhenCreatedOrUpdated(final SpecTopic specTopic) {
         specTopic.setSourceUrls(new ArrayList<String>());
         specTopic.setDescription(null);
         specTopic.setTags(new ArrayList<String>());
         specTopic.setRemoveTags(new ArrayList<String>());
         specTopic.setAssignedWriter(null);
         specTopic.setType(null);
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
         LOG.debug("Processing topic: {}", specTopic.getText());
 
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
             // Set the CSP Property Tag ID for new/updated so that we can track the newly created topic
             if (specTopic.isTopicAClonedTopic() || specTopic.isTopicANewTopic()) {
                 setCSPPropertyForTopic(topic, specTopic, providerFactory.getProvider(PropertyTagProvider.class));
             }
 
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
         LOG.debug("Creating a new topic");
 
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
         final TagWrapper tag = tagProvider.getTagByName(specTopic.getType());
         if (tag == null) {
             log.error(String.format(ProcessorConstants.ERROR_TYPE_NONEXIST_MSG, specTopic.getLineNumber(), specTopic.getText()));
             return null;
         }
 
         // Add the type to the topic
         topic.setTags(tagProvider.newTagCollection());
         topic.getTags().addNewItem(tag);
 
         // Add the added by property tag
         topic.setProperties(propertyTagProvider.newPropertyTagInTopicCollection(topic));
         final String assignedWriter = specTopic.getAssignedWriter(true);
         if (assignedWriter != null) {
             final PropertyTagWrapper addedByPropertyTag = propertyTagProvider.getPropertyTag(CSConstants.ADDED_BY_PROPERTY_TAG_ID);
             final PropertyTagInTopicWrapper addedByProperty = propertyTagProvider.newPropertyTagInTopic(addedByPropertyTag, topic);
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
 
         // Get the current existing topic
         final TopicWrapper topic = topicProvider.getTopic(specTopic.getDBId(), null);
         LOG.debug("Updating existing topic {}", topic.getId());
 
         return topic;
     }
 
     protected void setCSPPropertyForTopic(final TopicWrapper topic, final SpecTopic specTopic,
             final PropertyTagProvider propertyTagProvider) {
         LOG.debug("Setting the CSP Property Tag for {}", specTopic.getId());
 
         // Update the CSP Property tag
         final UpdateableCollectionWrapper<PropertyTagInTopicWrapper> properties;
         if (topic.getProperties() == null) {
             properties = propertyTagProvider.newPropertyTagInTopicCollection(topic);
         } else {
             properties = topic.getProperties();
         }
         final List<PropertyTagInTopicWrapper> propertyItems = topic.getProperties().getItems();
         boolean cspPropertyFound = false;
         for (final PropertyTagInTopicWrapper property : propertyItems) {
             // Update the CSP Property Tag if it exists, otherwise add a new one
             if (property.getId().equals(CSConstants.CSP_PROPERTY_ID)) {
                 cspPropertyFound = true;
 
                 // Remove the current property
                 properties.remove(property);
 
                 // Add the updated one
                 property.setValue(specTopic.getUniqueId());
                 properties.addUpdateItem(property);
             }
         }
 
         if (!cspPropertyFound) {
             final PropertyTagWrapper cspPropertyTag = propertyTagProvider.getPropertyTag(CSConstants.CSP_PROPERTY_ID);
             final PropertyTagInTopicWrapper cspProperty = propertyTagProvider.newPropertyTagInTopic(cspPropertyTag, topic);
             cspProperty.setValue(specTopic.getUniqueId());
             cspProperty.setId(CSConstants.CSP_PROPERTY_ID);
             properties.addNewItem(cspProperty);
         }
 
         topic.setProperties(properties);
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
         LOG.debug("Processing topic tags");
         boolean changed = false;
 
         // Get the tags for the topic
         final List<String> addTagNames = specTopic.getTags(true);
         final List<TagWrapper> addTags = new ArrayList<TagWrapper>();
         for (final String addTagName : addTagNames) {
             final TagWrapper tag = tagProvider.getTagByName(addTagName);
             if (tag != null) {
                 addTags.add(tag);
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
             final TagWrapper removeTag = tagProvider.getTagByName(removeTagName);
             if (removeTag != null) {
                 removeTags.add(removeTag);
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
         // Check to make sure we have tags to add
         if (addTags.isEmpty()) {
             return false;
         }
 
         boolean changed = false;
         // Finds tags that aren't already in the database and adds them
         final List<TagWrapper> topicTagList = topic.getTags() == null ? new ArrayList<TagWrapper>() : topic.getTags().getItems();
         final CollectionWrapper<TagWrapper> updatedTagCollection = topic.getTags() == null ? tagProvider.newTagCollection() : topic
                 .getTags();
 
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
         LOG.debug("Processing assigned writer");
 
         // See if a new tag collection needs to be created
         if (topic.getTags() == null) {
             topic.setTags(tagProvider.newTagCollection());
         }
 
         // Set the assigned writer (Tag Table)
         final TagWrapper writerTag = tagProvider.getTagByName(specTopic.getAssignedWriter(true));
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
         LOG.debug("Processing topic source urls");
 
         boolean changed = false;
         // Save the new Source Urls
         final List<String> urls = specTopic.getSourceUrls(true);
 
         if (urls != null && !urls.isEmpty()) {
             final UpdateableCollectionWrapper<TopicSourceURLWrapper> sourceUrls = topic.getSourceURLs() == null ? topicSourceURLProvider
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
 
     protected Map<SpecTopic, SpecTopic> createDuplicatedTopicMap(final List<SpecTopic> specTopics) {
         final Map<SpecTopic, SpecTopic> mapping = new HashMap<SpecTopic, SpecTopic>();
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
                 mapping.put(topic, cloneTopic);
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
                 mapping.put(topic, cloneTopic);
             }
         }
 
         return mapping;
     }
 
     /**
      * Syncs all duplicated topics with their real topic counterpart in the content specification.
      *
      * @param duplicatedTopics A Map of the all the duplicated topics in the Content Specification mapped to there bae topic.
      */
     protected void syncDuplicatedTopics(final Map<SpecTopic, SpecTopic> duplicatedTopics) {
         for (final Map.Entry<SpecTopic, SpecTopic> topicEntry : duplicatedTopics.entrySet()) {
             final SpecTopic topic = topicEntry.getKey();
             final SpecTopic cloneTopic = topicEntry.getValue();
 
             // Set the id
             topic.setId(cloneTopic.getDBId() == null ? null : cloneTopic.getDBId().toString());
         }
     }
 
     /**
      * Merges a Content Spec object with it's Database counterpart if one exists, otherwise it will create a new Database Entity if
      * create is set to true.
      *
      * @param providerFactory
      * @param processorData   The data used for this processing action.
      * @param create          If a new Content Spec Entity should be created if one doesn't exist.
      * @throws Exception Thrown if a problem occurs saving to the database.
      */
     protected void mergeAndSaveContentSpec(final DataProviderFactory providerFactory, final ProcessorData processorData,
             boolean create) throws Exception {
         // Get the providers
         final ContentSpecProvider contentSpecProvider = providerFactory.getProvider(ContentSpecProvider.class);
         final PropertyTagProvider propertyTagProvider = providerFactory.getProvider(PropertyTagProvider.class);
         final TagProvider tagProvider = providerFactory.getProvider(TagProvider.class);
 
         // Create the temporary entity to store changes in and load the real entity if it exists.
         final ContentSpec contentSpec = processorData.getContentSpec();
         ContentSpecWrapper contentSpecEntity = null;
         if (contentSpec.getId() != null) {
             contentSpecEntity = contentSpecProvider.getContentSpec(contentSpec.getId());
         } else if (create) {
             contentSpecEntity = contentSpecProvider.newContentSpec();
 
             // setup the basic values
             contentSpecEntity.setLocale(CommonConstants.DEFAULT_LOCALE);
 
             if (processorData.getUsername() != null) {
                 // Add the added by property tag
                 final UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper> propertyTagCollection = propertyTagProvider
                         .newPropertyTagInContentSpecCollection(
                         contentSpecEntity);
 
                 // Create the new property tag
                 final PropertyTagWrapper addedByProperty = propertyTagProvider.getPropertyTag(CSConstants.ADDED_BY_PROPERTY_TAG_ID);
                 final PropertyTagInContentSpecWrapper propertyTag = propertyTagProvider.newPropertyTagInContentSpec(addedByProperty,
                         contentSpecEntity);
                 propertyTag.setValue(processorData.getUsername());
                 propertyTagCollection.addNewItem(propertyTag);
 
                 // Set the updated properties for the content spec
                 contentSpecEntity.setProperties(propertyTagCollection);
             }
         } else {
             throw new ProcessingException("Unable to find the existing Content Specification");
         }
 
         // Check that the type still matches
         final int typeId = BookType.getBookTypeId(contentSpec.getBookType());
         if (contentSpecEntity.getType() == null || !contentSpecEntity.getType().equals(typeId)) {
             contentSpecEntity.setType(typeId);
         }
 
         final ArrayList<CSNodeWrapper> contentSpecNodes = new ArrayList<CSNodeWrapper>();
         if (contentSpecEntity.getChildren() != null) {
             contentSpecNodes.addAll(contentSpecEntity.getChildren().getItems());
         }
 
         // Create the content spec entity so that we have a valid reference to add nodes to
         if (create) {
             contentSpecEntity = contentSpecProvider.createContentSpec(contentSpecEntity);
         }
 
         // Check that the content spec was updated/created successfully.
         if (contentSpecEntity == null) {
             throw new ProcessingException("Saving the updated Content Specification failed.");
         }
         contentSpec.setId(contentSpecEntity.getId());
 
         // Set the bug links last validated property
         if (processorData.isBugLinksReValidated()) {
             // Get the collection to use
             final UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper> propertyTagCollection = contentSpecEntity.getProperties()
                     == null ? propertyTagProvider.newPropertyTagInContentSpecCollection(
                     contentSpecEntity) : contentSpecEntity.getProperties();
 
             // Check if the property already exists and if so remove it, and then create a new one to ensure it a revision is created
             for (final PropertyTagInContentSpecWrapper propertyTag : propertyTagCollection.getItems()) {
                 if (propertyTag.getId().equals(ProcessorConstants.BUG_LINKS_LAST_VALIDATED_PROPERTY_TAG)) {
                     propertyTag.setValue(Long.toString(new Date().getTime()));
                     propertyTagCollection.remove(propertyTag);
                     propertyTagCollection.addRemoveItem(propertyTag);
                 }
             }
 
             // Add the new tag
             final PropertyTagWrapper lastUpdatedProperty = propertyTagProvider.getPropertyTag(
                     ProcessorConstants.BUG_LINKS_LAST_VALIDATED_PROPERTY_TAG);
             final PropertyTagInContentSpecWrapper propertyTag = propertyTagProvider.newPropertyTagInContentSpec(lastUpdatedProperty,
                     contentSpecEntity);
             propertyTag.setValue(Long.toString(new Date().getTime()));
             propertyTagCollection.addNewItem(propertyTag);
 
             // Set the updated properties for the content spec
             contentSpecEntity.setProperties(propertyTagCollection);
         }
 
         // Add any global book tags
         mergeGlobalOptions(contentSpecEntity, contentSpec, tagProvider);
 
         // Get the list of transformable child nodes for processing
         final List<Node> nodes = getTransformableNodes(contentSpec.getNodes());
         nodes.addAll(getTransformableNodes(contentSpec.getBaseLevel().getChildNodes()));
 
         // Merge the base level and comments
         final Map<SpecNode, CSNodeWrapper> nodeMapping = new HashMap<SpecNode, CSNodeWrapper>();
         mergeChildren(nodes, contentSpecNodes, providerFactory, null, contentSpecEntity, nodeMapping);
 
         contentSpecProvider.updateContentSpec(contentSpecEntity);
 
         // Merge the relationships now all spec topics have a mapping to a node
         mergeTopicRelationships(nodeMapping, providerFactory);
 
         contentSpecProvider.updateContentSpec(contentSpecEntity, processorData.getLogMessage());
     }
 
     /**
      * Merge a Content Spec entities global options (book tags & condition) with the options defined in the processed content spec.
      *
      * @param contentSpecEntity The content spec entity to merge with.
      * @param contentSpec       The generic content spec object to merge from.
      * @param tagProvider
      */
     protected void mergeGlobalOptions(final ContentSpecWrapper contentSpecEntity, final ContentSpec contentSpec,
             final TagProvider tagProvider) {
         // make sure the condition matches
         contentSpecEntity.setCondition(contentSpec.getBaseLevel().getConditionStatement());
 
         // Merge the global tags
         final List<String> tags = contentSpec.getTags();
         final CollectionWrapper<TagWrapper> tagsCollection = contentSpecEntity.getBookTags() == null ? tagProvider.newTagCollection() :
                 contentSpecEntity.getBookTags();
 
         // Check to make sure we have something to process
         if (!(tags.isEmpty() && tagsCollection.isEmpty())) {
             final List<TagWrapper> existingTags = new ArrayList<TagWrapper>(tagsCollection.getItems());
 
             // Add any new book tags
             for (final String tagName : tags) {
                 final TagWrapper existingTag = findExistingBookTag(tagName, existingTags);
                 if (existingTag == null) {
                     LOG.debug("Adding global book tag {}", tagName);
                     final TagWrapper tag = tagProvider.getTagByName(tagName);
                     tagsCollection.addNewItem(tag);
                 } else {
                     existingTags.remove(existingTag);
                 }
             }
 
             // Remove any existing book tags that are no longer valid
             if (!existingTags.isEmpty()) {
                 for (final TagWrapper existingTag : existingTags) {
                     LOG.debug("Removing global book tag {}", existingTag.getName());
                     tagsCollection.remove(existingTag);
                     tagsCollection.addRemoveItem(existingTag);
                 }
             }
 
             contentSpecEntity.setBookTags(tagsCollection);
         }
     }
 
     protected TagWrapper findExistingBookTag(final String tagName, final List<TagWrapper> existingTags) {
         if (existingTags != null) {
             // Loop over the current nodes and see if any match
             for (final TagWrapper tag : existingTags) {
                 if (tagName.equals(tag.getName())) {
                     return tag;
                 }
             }
         }
 
         return null;
     }
 
     /**
      * Merges the children nodes of a Content Spec level into the Content Spec Entity level. If any new nodes have to be created,
      * the base of the node will be created on the server and then the rest after that.
      *
      * @param childrenNodes    The child nodes to be merged.
      * @param contentSpecNodes The list of content spec nodes that can be matched to.
      * @param providerFactory
      * @param parentNode       The parent entity node that the nodes should be assigned to.
      * @param contentSpec      The content spec entity that the nodes belong to.
      * @param nodeMapping      TODO
      * @throws Exception Thrown if an error occurs during saving new nodes.
      */
     protected void mergeChildren(final List<Node> childrenNodes, final List<CSNodeWrapper> contentSpecNodes,
             final DataProviderFactory providerFactory, final CSNodeWrapper parentNode, final ContentSpecWrapper contentSpec,
             final Map<SpecNode, CSNodeWrapper> nodeMapping) throws Exception {
         final CSNodeProvider nodeProvider = providerFactory.getProvider(CSNodeProvider.class);
 
         UpdateableCollectionWrapper<CSNodeWrapper> levelChildren;
         if (parentNode == null) {
             levelChildren = contentSpec.getChildren();
 
             // Check that the level container isn't null
             if (levelChildren == null) {
                 levelChildren = nodeProvider.newCSNodeCollection();
             }
         } else {
             levelChildren = parentNode.getChildren();
 
             // Check that the level container isn't null
             if (levelChildren == null) {
                 levelChildren = nodeProvider.newCSNodeCollection();
             }
         }
 
         // Update or create all of the children nodes that exist in the content spec
         CSNodeWrapper prevNode = null;
         for (final Node childNode : childrenNodes) {
             if (!isTransformableNode(childNode)) {
                 continue;
             }
 
             LOG.debug("Processing: {}", childNode.getText());
 
             // Find the Entity Node that matches the Content Spec node, if one exists
             CSNodeWrapper foundNodeEntity = findExistingNode(parentNode, childNode, contentSpecNodes);
 
             // If the node was not found create a new one
             boolean changed = false;
             boolean newNode = false;
             if (foundNodeEntity == null) {
                 LOG.debug("Creating a new node");
                 final CSNodeWrapper newCSNodeEntity = nodeProvider.newCSNode();
                 if (childNode instanceof SpecTopic) {
                     final SpecTopic specTopic = (SpecTopic) childNode;
                     mergeTopic(specTopic, newCSNodeEntity);
                     if (specTopic.getTopicType().equals(TopicType.LEVEL)) {
                         newCSNodeEntity.setNodeType(CommonConstants.CS_NODE_INNER_TOPIC);
                     } else {
                         newCSNodeEntity.setNodeType(CommonConstants.CS_NODE_TOPIC);
                     }
                 } else if (childNode instanceof Level) {
                     mergeLevel((Level) childNode, newCSNodeEntity);
                     newCSNodeEntity.setNodeType(((Level) childNode).getLevelType().getId());
                 } else if (childNode instanceof Comment) {
                     mergeComment((Comment) childNode, newCSNodeEntity);
                     newCSNodeEntity.setNodeType(CommonConstants.CS_NODE_COMMENT);
                 } else if (childNode instanceof File) {
                     mergeFile((File) childNode, newCSNodeEntity);
                     newCSNodeEntity.setNodeType(CommonConstants.CS_NODE_FILE);
                 } else {
                     mergeMetaData((KeyValueNode<?>) childNode, newCSNodeEntity);
                     if (((KeyValueNode<?>) childNode).getValue() instanceof SpecTopic) {
                         newCSNodeEntity.setNodeType(CommonConstants.CS_NODE_META_DATA_TOPIC);
                     } else {
                         newCSNodeEntity.setNodeType(CommonConstants.CS_NODE_META_DATA);
                     }
                 }
 
                 // set the content spec for the new entity
                 foundNodeEntity = newCSNodeEntity;
                 newNode = true;
             } else {
                 LOG.debug("Found existing node {}", foundNodeEntity.getId());
 
                 // If the node was found remove it from the list of content spec nodes, so it can no longer be matched
                 contentSpecNodes.remove(foundNodeEntity);
 
                 if (childNode instanceof SpecTopic) {
                     final SpecTopic specTopic = (SpecTopic) childNode;
                     if (mergeTopic((SpecTopic) childNode, foundNodeEntity)) {
                         changed = true;
                     }
                     if (specTopic.getTopicType().equals(
                             TopicType.LEVEL) && (foundNodeEntity.getNodeType() == null || !foundNodeEntity.getNodeType().equals(
                             CommonConstants.CS_NODE_INNER_TOPIC))) {
                         foundNodeEntity.setNodeType(CommonConstants.CS_NODE_INNER_TOPIC);
                         foundNodeEntity.setNextNode(null);
                         changed = true;
                     } else if (!specTopic.getTopicType().equals(
                             TopicType.LEVEL) && (foundNodeEntity.getNodeType() == null || !foundNodeEntity.getNodeType().equals(
                             CommonConstants.CS_NODE_TOPIC))) {
                         foundNodeEntity.setNodeType(CommonConstants.CS_NODE_TOPIC);
                         changed = true;
                     }
                 } else if (childNode instanceof Level) {
                     if (mergeLevel((Level) childNode, foundNodeEntity)) {
                         changed = true;
                     }
                 } else if (childNode instanceof Comment) {
                     if (mergeComment((Comment) childNode, foundNodeEntity)) {
                         changed = true;
                     }
                 } else if (childNode instanceof File) {
                     if (mergeFile((File) childNode, foundNodeEntity)) {
                         changed = true;
                     }
                 } else {
                     if (mergeMetaData((KeyValueNode<?>) childNode, foundNodeEntity)) {
                         changed = true;
                     }
                 }
 
                 // Check if the parent node is different, if so then set it as moved
                 if (!doesParentMatch(parentNode, foundNodeEntity.getParent())) {
                     LOG.debug("Setting entity {} as moved", foundNodeEntity.getId());
                     changed = true;
                 }
             }
 
             // If the node is a level than merge the children nodes as well
             if (childNode instanceof Level) {
                 final Level level = (Level) childNode;
 
                 // Add the levels inner topic to the list of children if one exists
                 final LinkedList<Node> children = level.getChildNodes();
                 if (level.getInnerTopic() != null) {
                     children.addFirst(level.getInnerTopic());
                 }
 
                 final ArrayList<CSNodeWrapper> currentChildren = new ArrayList<CSNodeWrapper>();
                 if (foundNodeEntity.getChildren() != null) {
                     currentChildren.addAll(foundNodeEntity.getChildren().getItems());
                 }
 
                 mergeChildren(getTransformableNodes(children), currentChildren, providerFactory, foundNodeEntity, contentSpec, nodeMapping);
             } else if (childNode instanceof FileList) {
                 final FileList level = (FileList) childNode;
 
                 // Add the levels inner topic to the list of children if one exists
                 final LinkedList<Node> children = new LinkedList<Node>();
                 if (level.getValue() != null) {
                     children.addAll(level.getValue());
                 }
 
                 final ArrayList<CSNodeWrapper> currentChildren = new ArrayList<CSNodeWrapper>();
                 if (foundNodeEntity.getChildren() != null) {
                     currentChildren.addAll(foundNodeEntity.getChildren().getItems());
                 }
 
                 mergeChildren(getTransformableNodes(children), currentChildren, providerFactory, foundNodeEntity, contentSpec, nodeMapping);
             }
 
             // Set up the next node relationship for the previous node
             if (!foundNodeEntity.getNodeType().equals(CommonConstants.CS_NODE_INNER_TOPIC) && prevNode != null &&
                     (prevNode.getNextNode() == null || (prevNode.getNextNode() != null && foundNodeEntity.getId() == null) || (prevNode
                             .getNextNode() != null && !prevNode.getNextNode().getId().equals(
                             foundNodeEntity.getId())))) {
                 prevNode.setNextNode(foundNodeEntity);
                 changed = true;
 
                 // Add the previous node to the updated collection if it's not already there
                 if (parentNode == null) {
                     addContentSpecChild(contentSpec, levelChildren, prevNode);
                 } else {
                     addChild(parentNode, levelChildren, prevNode);
                 }
             }
 
             // The node has been updated or created so update it's state
             if (changed || newNode) {
                 if (parentNode == null) {
                     addContentSpecChild(contentSpec, levelChildren, foundNodeEntity);
                 } else {
                     addChild(parentNode, levelChildren, foundNodeEntity);
                 }
             }
 
             // Add the node to the mapping of nodes to entity nodes
             if (childNode instanceof SpecNode) {
                 nodeMapping.put((SpecNode) childNode, foundNodeEntity);
             }
 
             // Set the previous node to the current node since processing is done
             if (!foundNodeEntity.getNodeType().equals(CommonConstants.CS_NODE_INNER_TOPIC)) {
                 prevNode = foundNodeEntity;
             }
         }
 
         // If there is a previous node then make sure it's next node id is null as it's the last in the linked list
         if (prevNode != null && prevNode.getNextNode() != null) {
             prevNode.setNextNode(null);
 
             // Add the previous node to the updated collection if it's not already there
             if (parentNode == null) {
                 addContentSpecChild(contentSpec, levelChildren, prevNode);
             } else {
                 addChild(parentNode, levelChildren, prevNode);
             }
         }
 
         // Set the nodes that are no longer used for removal
         if (!contentSpecNodes.isEmpty()) {
             for (final CSNodeWrapper childNode : contentSpecNodes) {
                 LOG.debug("Removing entity {} - {}", childNode.getId(), childNode.getTitle());
                 levelChildren.remove(childNode);
                 levelChildren.addRemoveItem(childNode);
 
                 // Remove any incoming relationships as well, since this node has been removed
                 if (childNode.getRelatedFromNodes() != null) {
                     final UpdateableCollectionWrapper<CSRelatedNodeWrapper> relatedNodes = childNode.getRelatedFromNodes();
                     for (final CSRelatedNodeWrapper relatedNode : relatedNodes.getItems()) {
                         relatedNodes.remove(relatedNode);
                         relatedNodes.addRemoveItem(relatedNode);
                     }
 
                     childNode.setRelatedFromNodes(relatedNodes);
                 }
             }
         }
 
         // Set the children so it'll be updated
         if (parentNode == null) {
             contentSpec.setChildren(levelChildren);
         } else {
             parentNode.setChildren(levelChildren);
         }
     }
 
     protected void addChild(final CSNodeWrapper parent, final UpdateableCollectionWrapper<CSNodeWrapper> collection,
             final CSNodeWrapper item) {
         collection.remove(item);
         if (item.getId() == null) {
             collection.addNewItem(item);
         } else {
             if (item.getParent() != null && item.getParent().equals(parent)) {
                 collection.addUpdateItem(item);
             } else if (item.getParent() != null) {
                 item.getParent().getChildren().addRemoveItem(item);
                 collection.addNewItem(item);
             } else {
                 collection.addNewItem(item);
             }
         }
     }
 
     protected void addContentSpecChild(final ContentSpecWrapper parent, final UpdateableCollectionWrapper<CSNodeWrapper> collection,
             final CSNodeWrapper item) {
         collection.remove(item);
         if (item.getId() == null) {
             collection.addNewItem(item);
         } else {
             if (item.getContentSpec() != null && item.getContentSpec().equals(parent)) {
                 collection.addUpdateItem(item);
             } else if (item.getContentSpec() != null) {
                 item.getContentSpec().getChildren().addRemoveItem(item);
                 collection.addNewItem(item);
             } else {
                 collection.addNewItem(item);
             }
         }
     }
 
     /**
      * Finds the existing Entity Node that matches a ContentSpec node.
      *
      * @param childNode           The ContentSpec node to find a matching Entity node for.
      * @param entityChildrenNodes The entity child nodes to match from.
      * @return The matching entity if one exists, otherwise null.
      */
     protected CSNodeWrapper findExistingNode(final CSNodeWrapper parent, final Node childNode,
             final List<CSNodeWrapper> entityChildrenNodes) {
         CSNodeWrapper foundNodeEntity = null;
         if (entityChildrenNodes != null && !entityChildrenNodes.isEmpty()) {
             for (final CSNodeWrapper nodeEntity : entityChildrenNodes) {
                 // ignore it if the parent doesn't match otherwise keep looking to see if we can find a better match
                 if (!doesParentMatch(parent, nodeEntity.getParent())) {
                     continue;
                 }
 
                 if (childNode instanceof Comment) {
                     // Comments are handled differently to try and get the exact comment better, since its common for multiple comments
                     // to be on the same level
                     if (doesCommentMatch((Comment) childNode, nodeEntity, foundNodeEntity != null)) {
                         foundNodeEntity = nodeEntity;
                     }
                 } else if (childNode instanceof Level) {
                     // Since levels might have other possible titles that are in the title threshold,
                     // we need to keep looping to see if we can find something better
                     if (doesLevelMatch((Level) childNode, nodeEntity, foundNodeEntity != null)) {
                         foundNodeEntity = nodeEntity;
                     }
 
                     // stop looking if the title matches otherwise keep looking to see if we can find a better match
                     if (parent != null && foundNodeEntity != null && foundNodeEntity.getTitle().equals(((Level) childNode).getTitle())) {
                         break;
                     }
                 } else {
                     if (childNode instanceof SpecTopic && doesTopicMatch((SpecTopic) childNode, nodeEntity, foundNodeEntity != null)) {
                         foundNodeEntity = nodeEntity;
                     } else if (childNode instanceof KeyValueNode && doesMetaDataMatch((KeyValueNode<?>) childNode, nodeEntity)) {
                         foundNodeEntity = nodeEntity;
                     } else if (childNode instanceof File && doesFileMatch((File) childNode, nodeEntity)) {
                         foundNodeEntity = nodeEntity;
                     }
                 }
             }
         }
 
         return foundNodeEntity;
     }
 
     /**
      * Checks to see if two parent nodes match
      *
      * @param parent       The current parent being used for processing.
      * @param entityParent The processed entities parent.
      * @return True if the two match, otherwise false.
      */
     protected boolean doesParentMatch(final CSNodeWrapper parent, final CSNodeWrapper entityParent) {
         if (parent != null && entityParent != null) {
             if (parent.getId() != null && parent.getId().equals(entityParent.getId())) {
                 return true;
             } else if (parent.getId() == null && entityParent.getId() == null && parent == entityParent) {
                 return true;
             } else {
                 return false;
             }
         } else if (parent == null && entityParent == null) {
             return true;
         } else {
             return false;
         }
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
 
         // Meta Data Value
         final Object value = metaData.getValue();
         if (metaDataEntity.getAdditionalText() == null || !metaDataEntity.getAdditionalText().equals(value.toString())) {
             metaDataEntity.setAdditionalText(value.toString());
             changed = true;
         }
 
         // Set the Topic details if the Meta Data is a Spec Topic
         if (value instanceof SpecTopic) {
             if (mergeTopic((SpecTopic) value, metaDataEntity)) {
                 changed = true;
             }
         }
 
         // Meta Data Title
         if (metaDataEntity.getTitle() == null || !metaDataEntity.getTitle().equals(metaData.getKey())) {
             metaDataEntity.setTitle(metaData.getKey());
             changed = true;
         }
 
         return changed;
     }
 
     /**
      * @param level
      * @param levelEntity
      * @return True if some value was changed, otherwise false.
      * @throws Exception
      */
     protected boolean mergeLevel(final Level level, final CSNodeWrapper levelEntity) throws Exception {
         boolean changed = false;
 
         // TYPE
         if (level.getLevelType() != null && level.getLevelType().getId() != levelEntity.getNodeType()) {
             levelEntity.setNodeType(level.getLevelType().getId());
             changed = true;
         }
 
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
      * @param file
      * @param fileEntity
      * @return True if a some value was changed, otherwise false.
      */
     protected boolean mergeFile(final File file, final CSNodeWrapper fileEntity) {
         boolean changed = false;
 
         // TITLE
         if (file.getTitle() != null && !file.getTitle().equals(fileEntity.getTitle())) {
             fileEntity.setTitle(file.getTitle());
             changed = true;
         } else if (file.getTitle() == null && fileEntity.getTitle() != null) {
             fileEntity.setTitle(null);
             changed = true;
         }
 
         // FILE ID
         // No need to check if the file id should be set to null, as it is a mandatory field
         if (file.getId() != null && !file.getId().equals(fileEntity.getEntityId())) {
             fileEntity.setEntityId(file.getId());
             changed = true;
         }
 
         // FILE REVISION
         if (file.getRevision() != null && !file.getRevision().equals(fileEntity.getEntityRevision())) {
             fileEntity.setEntityRevision(file.getRevision());
             changed = true;
         } else if (file.getRevision() == null && fileEntity.getEntityRevision() != null) {
             fileEntity.setEntityRevision(null);
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
         if (commentEntity.getTitle() == null || !commentEntity.getTitle().equals(comment.getText())) {
             commentEntity.setTitle(comment.getText());
             return true;
         }
 
         return false;
     }
 
     /**
      * Merges the relationships for all Spec Topics into their counterpart Entity nodes.
      *
      * @param nodeMapping     The mapping of Spec Nodes to Entity nodes.
      * @param providerFactory
      */
     protected void mergeTopicRelationships(final Map<SpecNode, CSNodeWrapper> nodeMapping, final DataProviderFactory providerFactory) {
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
                 mergeTopicRelationship(nodeMapping, specTopic, topicEntity, nodeProvider);
             }
         }
     }
 
     /**
      * Merges the relationships from a Spec Topic into a Topic Node Entity.
      *
      * @param nodeMapping  The mapping of Spec Nodes to Entity nodes.
      * @param specTopic    The Spec Topic to get the relationships from.
      * @param topicEntity  The Entity to merge the relationships into.
      * @param nodeProvider The provider factory for getting new or existing nodes.
      */
     protected void mergeTopicRelationship(final Map<SpecNode, CSNodeWrapper> nodeMapping, final SpecTopic specTopic,
             final CSNodeWrapper topicEntity, final CSNodeProvider nodeProvider) {
         final UpdateableCollectionWrapper<CSRelatedNodeWrapper> relatedToNodes = topicEntity.getRelatedToNodes() == null ? nodeProvider
                 .newCSRelatedNodeCollection() : topicEntity.getRelatedToNodes();
         final List<CSRelatedNodeWrapper> existingRelationships = new ArrayList<CSRelatedNodeWrapper>(relatedToNodes.getItems());
 
         LOG.debug("Processing relationships for topic: {}", topicEntity.getEntityId());
 
         // Check to make sure that the spec topic has any relationships
         if (!specTopic.getRelationships().isEmpty()) {
             int relationshipSortCount = 1;
 
             for (final Relationship relationship : specTopic.getRelationships()) {
                 // All process relationships should not be stored
                 if (relationship instanceof ProcessRelationship) continue;
 
                 LOG.debug("Processing Relationship: {}", relationship.getSecondaryRelationshipTopicId());
 
                 // See if the related node already exists
                 CSRelatedNodeWrapper foundRelatedNode = findExistingRelatedNode(relationship, existingRelationships);
 
                 if (foundRelatedNode != null) {
                     LOG.debug("Found existing related node {}", foundRelatedNode.getRelationshipId());
                     // Remove the related node from the list of existing nodes
                     existingRelationships.remove(foundRelatedNode);
 
                     // Found a node so update anything that might have changed
                     boolean updated = false;
                     if (foundRelatedNode.getRelationshipSort() != relationshipSortCount) {
                         foundRelatedNode.setRelationshipSort(relationshipSortCount);
                         updated = true;
                     }
 
                     // If the node was updated, set it's state in the collection to updated, otherwise just put it back in normally.
                     if (updated) {
                         relatedToNodes.remove(foundRelatedNode);
                         relatedToNodes.addUpdateItem(foundRelatedNode);
                     }
                 } else {
                     LOG.debug("Creating new relationship");
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
 
                     relatedToNodes.addNewItem(foundRelatedNode);
                 }
 
                 // increment the sort counter
                 relationshipSortCount++;
             }
         }
 
         // Remove any existing relationships that are no longer valid
         if (!existingRelationships.isEmpty()) {
             for (final CSRelatedNodeWrapper relatedNode : existingRelationships) {
                 LOG.debug("Removing relationship {}", relatedNode.getRelationshipId());
                 relatedToNodes.remove(relatedNode);
                 relatedToNodes.addRemoveItem(relatedNode);
             }
         }
 
         // Check if anything was changed, if so then set the changes
         if (!relatedToNodes.getAddItems().isEmpty() || !relatedToNodes.getUpdateItems().isEmpty() || !relatedToNodes.getRemoveItems()
                 .isEmpty()) {
             topicEntity.setRelatedToNodes(relatedToNodes);
 
             // Make sure the node is in the updated state
             if (topicEntity.getParent() == null) {
                 topicEntity.getContentSpec().getChildren().remove(topicEntity);
                 if (topicEntity.getId() == null) {
                     topicEntity.getContentSpec().getChildren().addNewItem(topicEntity);
                 } else {
                     topicEntity.getContentSpec().getChildren().addUpdateItem(topicEntity);
                 }
             } else {
                 final CSNodeWrapper parent = topicEntity.getParent();
                 parent.getChildren().remove(topicEntity);
                 if (topicEntity.getId() == null) {
                     parent.getChildren().addNewItem(topicEntity);
                 } else {
                     parent.getChildren().addUpdateItem(topicEntity);
                 }
             }
         }
     }
 
     /**
      * Finds an existing relationship for a topic.
      *
      * @param relationship      The relationship to be found.
      * @param topicRelatedNodes The topic related nodes to find the relationship from.
      * @return The related Entity, otherwise null if one can't be found.
      */
     protected CSRelatedNodeWrapper findExistingRelatedNode(final Relationship relationship,
             final List<CSRelatedNodeWrapper> topicRelatedNodes) {
         if (topicRelatedNodes != null) {
             // Loop over the current nodes and see if any match
             for (final CSRelatedNodeWrapper relatedNode : topicRelatedNodes) {
                 if (relationship instanceof TargetRelationship) {
                     if (doesRelationshipMatch((TargetRelationship) relationship, relatedNode)) {
                         return relatedNode;
                     }
                 } else {
                     if (doesRelationshipMatch((TopicRelationship) relationship, relatedNode)) {
                         return relatedNode;
                     }
                 }
             }
         }
 
         return null;
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
         if (childNode instanceof KeyValueNode) {
             return !IGNORE_META_DATA.contains(((KeyValueNode) childNode).getKey());
         } else {
             return childNode instanceof SpecNode || childNode instanceof Comment || childNode instanceof Level || childNode instanceof File;
         }
     }
 
     /**
      * Checks to see if a ContentSpec meta data matches a Content Spec Entity meta data.
      *
      * @param metaData The ContentSpec meta data object.
      * @param node     The Content Spec Entity topic.
      * @return True if the meta data is determined to match otherwise false.
      */
     protected boolean doesMetaDataMatch(final KeyValueNode<?> metaData, final CSNodeWrapper node) {
         if (!(node.getNodeType().equals(CommonConstants.CS_NODE_META_DATA) || node.getNodeType().equals(
                 CommonConstants.CS_NODE_META_DATA_TOPIC))) return false;
 
         // If the unique id is not from the parser, in which case it will start with a number than use the unique id to compare
         if (metaData.getUniqueId() != null && metaData.getUniqueId().matches("^\\d.*")) {
             return metaData.getUniqueId().equals(Integer.toString(node.getId()));
         } else {
             // Allow for old abstract references.
             if (metaData.getKey().equals(CommonConstants.CS_ABSTRACT_TITLE) && node.getTitle().equals(
                     CommonConstants.CS_ABSTRACT_ALTERNATE_TITLE)) {
                 return true;
             }
 
             // Allow for alternate file references.
             if (metaData.getKey().equals(CommonConstants.CS_FILE_TITLE) && node.getTitle().equals(CommonConstants.CS_FILE_SHORT_TITLE)) {
                 return true;
             }
 
             // Check if the key matches, if it does than the nodes match.
             return metaData.getKey().equals(node.getTitle());
         }
     }
 
     /**
      * Checks to see if a ContentSpec level matches a Content Spec Entity level.
      *
      * @param level        The ContentSpec level object.
      * @param node         The Content Spec Entity level.
      * @param matchContent If the level title has to match exactly, otherwise it should match to a reasonable extent.
      * @return True if the level is determined to match otherwise false.
      */
     protected boolean doesLevelMatch(final Level level, final CSNodeWrapper node, boolean matchContent) {
        if (!EntityUtilities.isNodeALevel(node)) return false;
 
         // If the unique id is not from the parser, than use the unique id to compare
         if (level.getUniqueId() != null && level.getUniqueId().matches("^\\d.*")) {
             return level.getUniqueId().equals(Integer.toString(node.getId()));
         } else {
             // If the target ids match then the level should be the same
             if (level.getTargetId() != null && level.getTargetId() == node.getTargetId()) {
                 return true;
             }
 
             if (matchContent) {
                 // Make sure the level type matches
                 if (node.getNodeType() != level.getLevelType().getId()) return false;
 
                 return level.getTitle().equals(node.getTitle());
             } else {
                 return StringUtilities.similarDamerauLevenshtein(level.getTitle(),
                         node.getTitle()) >= ProcessorConstants.MIN_MATCH_SIMILARITY;
             }
         }
     }
 
     /**
      * Checks to see if a ContentSpec topic matches a Content Spec Entity topic.
      *
      * @param specTopic  The ContentSpec topic object.
      * @param node       The Content Spec Entity topic.
      * @param matchTypes If the node type has to match.
      * @return True if the topic is determined to match otherwise false.
      */
     protected boolean doesTopicMatch(final SpecTopic specTopic, final CSNodeWrapper node, boolean matchTypes) {
         if (!EntityUtilities.isNodeATopic(node)) return false;
 
         // Check if the node/topic type matches
         if (matchTypes) {
             boolean matches = true;
             switch (specTopic.getTopicType()) {
                 case LEVEL:
                     matches = node.getNodeType().equals(CommonConstants.CS_NODE_INNER_TOPIC);
                     break;
                 case NORMAL:
                 case FEEDBACK:
                     matches = node.getNodeType().equals(CommonConstants.CS_NODE_TOPIC);
                     break;
                 case LEGAL_NOTICE:
                 case REVISION_HISTORY:
                     matches = node.getNodeType().equals(CommonConstants.CS_NODE_META_DATA_TOPIC);
                     break;
             }
 
             if (!matches) {
                 return false;
             }
         }
 
         // If the unique id is not from the parser, in which case it will start with a number than use the unique id to compare
         if (specTopic.getUniqueId() != null && specTopic.getUniqueId().matches("^\\d.*")) {
             return specTopic.getUniqueId().equals(Integer.toString(node.getId()));
         } else {
             // Check the parent has the same name
             if (specTopic.getParent() != null && node.getParent() != null && node.getParent() instanceof Level) {
                 if (!((Level) specTopic.getParent()).getTitle().equals(node.getParent().getTitle())) {
                     return false;
                 }
             }
 
             // Since a content spec doesn't contain the database ids for the nodes use what is available to see if the topics match
             return specTopic.getDBId().equals(node.getEntityId());
         }
     }
 
     /**
      * Checks to see if a ContentSpec topic matches a Content Spec Entity file.
      *
      * @param file The ContentSpec file object.
      * @param node The Content Spec Entity file.
      * @return True if the file is determined to match otherwise false.
      */
     protected boolean doesFileMatch(final File file, final CSNodeWrapper node) {
         if (!node.getNodeType().equals(CommonConstants.CS_NODE_FILE)) return false;
 
         // If the unique id is not from the parser, in which case it will start with a number than use the unique id to compare
         if (file.getUniqueId() != null && file.getUniqueId().matches("^\\d.*")) {
             return file.getUniqueId().equals(Integer.toString(node.getId()));
         } else {
             // Since a content spec doesn't contain the database ids for the nodes use what is available to see if the files match
             return file.getId().equals(node.getEntityId());
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
         // If the relationship is a TopicRelationship, then the related node must be a topic or its not a match
         if (!EntityUtilities.isNodeATopic(relatedNode)) return false;
 
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
      * @param comment      The ContentSpec comment object.
      * @param node         The Content Spec Entity comment.
      * @param matchContent If the contents of the comment have to match to a reasonable extent.
      * @return True if the comment is determined to match otherwise false.
      */
     protected boolean doesCommentMatch(final Comment comment, final CSNodeWrapper node, boolean matchContent) {
         if (!node.getNodeType().equals(CommonConstants.CS_NODE_COMMENT)) return false;
 
         // If the unique id is not from the parser, in which case it will start with a number than use the unique id to compare
         if (comment.getUniqueId() != null && comment.getUniqueId().matches("^\\d.*")) {
             return comment.getUniqueId().equals(Integer.toString(node.getId()));
         } else if (matchContent) {
             return StringUtilities.similarDamerauLevenshtein(comment.getText(), node.getTitle()) >= ProcessorConstants.MIN_MATCH_SIMILARITY;
         } else {
             // Check the parent has the same name
             if (comment.getParent() != null) {
                 if (comment.getParent() instanceof ContentSpec) {
                     return node.getParent() == null;
                 } else if (comment.getParent() instanceof Level && node.getParent() != null) {
                     final Level parent = ((Level) comment.getParent());
                     return parent.getTitle().equals(node.getParent().getTitle());
                 } else {
                     return false;
                 }
             }
 
             return true;
         }
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
 
 class ProcessorData {
     private ContentSpec contentSpec;
     private String username;
     private boolean bugLinksReValidated = false;
     private LogMessageWrapper logMessage;
 
     String getUsername() {
         return username;
     }
 
     void setUsername(String username) {
         this.username = username;
     }
 
     boolean isBugLinksReValidated() {
         return bugLinksReValidated;
     }
 
     void setBugLinksReValidated(boolean bugLinksReValidated) {
         this.bugLinksReValidated = bugLinksReValidated;
     }
 
     LogMessageWrapper getLogMessage() {
         return logMessage;
     }
 
     void setLogMessage(LogMessageWrapper logMessage) {
         this.logMessage = logMessage;
     }
 
     ContentSpec getContentSpec() {
         return contentSpec;
     }
 
     void setContentSpec(ContentSpec contentSpec) {
         this.contentSpec = contentSpec;
     }
 }
