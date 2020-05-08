 package com.redhat.contentspec.processor;
 
 import com.redhat.contentspec.processor.constants.ProcessorConstants;
 import com.redhat.contentspec.processor.structures.ProcessingOptions;
 import com.redhat.contentspec.processor.utils.ProcessorUtilities;
 import org.jboss.pressgang.ccms.contentspec.*;
 import org.jboss.pressgang.ccms.contentspec.Process;
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.contentspec.entities.Relationship;
 import org.jboss.pressgang.ccms.contentspec.enums.BookType;
 import org.jboss.pressgang.ccms.contentspec.enums.LevelType;
 import org.jboss.pressgang.ccms.contentspec.enums.RelationshipType;
 import org.jboss.pressgang.ccms.contentspec.interfaces.ShutdownAbleApp;
 import org.jboss.pressgang.ccms.contentspec.rest.RESTManager;
 import org.jboss.pressgang.ccms.contentspec.rest.RESTReader;
 import org.jboss.pressgang.ccms.contentspec.sort.NullNumberSort;
 import org.jboss.pressgang.ccms.contentspec.sort.SpecTopicLineNumberComparator;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLogger;
 import org.jboss.pressgang.ccms.contentspec.utils.logging.ErrorLoggerManager;
 import org.jboss.pressgang.ccms.rest.v1.components.ComponentBaseTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.components.ComponentTagV1;
 import org.jboss.pressgang.ccms.rest.v1.components.ComponentTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTranslatedTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTCategoryInTagV1;
 import org.jboss.pressgang.ccms.utils.common.DocBookUtilities;
 import org.jboss.pressgang.ccms.utils.common.HashUtilities;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import static java.lang.String.format;
 
 /**
  * A class that is used to validate a Content Specification and the objects within a Content Specification. It
  * provides methods
  * for validating, ContentSpecs, Levels, Topics and Relationships. The Validator contains "Pre" and "Post" validation
  * methods
  * that will provide validation before doing any rest calls (pre) and after doing rest calls (post).
  *
  * @param <T> The REST Topic class that the Validator will be validating against.
  * @author lnewson
  */
 public class ContentSpecValidator<T extends RESTBaseTopicV1<T, ?, ?>> implements ShutdownAbleApp {
     private final RESTReader reader;
     private final ErrorLogger log;
     private final ProcessingOptions processingOptions;
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
      * Constructor.
      *
      * @param clazz             The Topic class that the validator should validate against.
      * @param elm               An Error Logger Manager that is used to capture log messages.
      * @param restManager       The manager that hands the rest communication.
      * @param processingOptions The set of processing options to be used when validating.
      */
     public ContentSpecValidator(final Class<T> clazz, final ErrorLoggerManager elm, final RESTManager restManager,
                                 final ProcessingOptions processingOptions) {
         this.clazz = clazz;
         log = elm.getLogger(ContentSpecValidator.class);
         reader = restManager.getReader();
         this.processingOptions = processingOptions;
         this.locale = CommonConstants.DEFAULT_LOCALE;
     }
 
     /**
      * Validates that a Content Specification is valid by checking the META data,
      * child levels and topics. This method is a
      * wrapper to first call PreValidate and then PostValidate.
      *
      * @param contentSpec The content specification to be validated.
      * @param specTopics  The list of topics that exist within the content specification.
      * @return True if the content specification is valid, otherwise false.
      */
     public boolean validateContentSpec(final ContentSpec contentSpec, final Map<String, SpecTopic> specTopics) {
         boolean valid = preValidateContentSpec(contentSpec, specTopics);
 
         if (!postValidateContentSpec(contentSpec, specTopics)) {
             valid = false;
         }
 
         return valid;
     }
 
     /**
      * Validates that a Content Specification is valid by checking the META data, child levels and topics.
      *
      * @param contentSpec The content specification to be validated.
      * @param specTopics  The list of topics that exist within the content specification.
      * @return True if the content specification is valid, otherwise false.
      */
     public boolean preValidateContentSpec(final ContentSpec contentSpec, final Map<String, SpecTopic> specTopics) {
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
 
         if (contentSpec.getCopyrightYear() != null && !contentSpec.getCopyrightYear().equals("")) {
             if (!contentSpec.getCopyrightYear().matches(ProcessorConstants.COPYRIGHT_YEAR_VALIDATE_REGEX)) {
                 log.error(ProcessorConstants.ERROR_INVALID_CS_COPYRIGHT_YEAR_MSG);
                 valid = false;
             }
         }
 
         // Check that the content specification isn't empty
         if (contentSpec.getBaseLevel() == null) {
             log.error(ProcessorConstants.ERROR_CS_EMPTY_MSG);
             valid = false;
         }
 
         // Check that each level is valid
         if (!preValidateLevel(contentSpec.getBaseLevel(), specTopics, contentSpec.getAllowEmptyLevels(),
                 contentSpec.getBookType())) {
             valid = false;
         }
 
         /*
          * Ensure that no topics exist that have the same ID but different revisions. This needs to be done at the
          * Content Spec
          * level rather than the Topic level as it isn't the topic that would be invalid but rather the set of topics
           * in the
          * content specification. If we are updating revisions however, we can ignore this check.
          */
         if (!processingOptions.isUpdateRevisions() && !checkTopicsForInvalidDuplicates(contentSpec)) {
             valid = false;
         }
 
         // reset the locale back to its default
         this.locale = CommonConstants.DEFAULT_LOCALE;
 
         return valid;
     }
 
     /**
      * Checks a Content Specification to see if it contains existing topics that have the same ID but different
      * revisions.
      *
      * @param contentSpec The content specification to be validated.
      * @return True if no duplicates were found, otherwise false.
      */
     private boolean checkTopicsForInvalidDuplicates(final ContentSpec contentSpec) {
         boolean valid = true;
 
         /* Find all Topics that have two or more different revisions */
         final List<SpecTopic> allSpecTopics = contentSpec.getSpecTopics();
         final Map<Integer, Map<Integer, Set<SpecTopic>>> invalidSpecTopics = new HashMap<Integer, Map<Integer,
                 Set<SpecTopic>>>();
         for (final SpecTopic specTopic1 : allSpecTopics) {
             if (!specTopic1.isTopicAnExistingTopic())
                 continue;
 
             for (final SpecTopic specTopic2 : allSpecTopics) {
                 // If the Topic isn't an existing topic and doesn't match the first spec topic's id, then continue
                 if (specTopic1 == specTopic2 || !specTopic2.isTopicAnExistingTopic()
                         || !specTopic1.getDBId().equals(specTopic2.getDBId()))
                     continue;
 
                 // Check if the revisions between the two topics are the same
                 if (specTopic1.getRevision() == null && specTopic2.getRevision() != null || specTopic1.getRevision()
                         != null
                         && specTopic2.getRevision() == null || specTopic1.getRevision() != null
                         && !specTopic1.getRevision().equals(specTopic2.getRevision())) {
                     if (!invalidSpecTopics.containsKey(specTopic1.getDBId())) {
                         invalidSpecTopics.put(specTopic1.getDBId(), new HashMap<Integer, Set<SpecTopic>>());
                     }
 
                     final Map<Integer, Set<SpecTopic>> revisionsToSpecTopic = invalidSpecTopics.get(specTopic1
                             .getDBId());
                     if (!revisionsToSpecTopic.containsKey(specTopic1.getRevision())) {
                         revisionsToSpecTopic.put(specTopic1.getRevision(), new HashSet<SpecTopic>());
                     }
 
                     revisionsToSpecTopic.get(specTopic1.getRevision()).add(specTopic1);
 
                     valid = false;
                 }
             }
         }
 
         /* Loop through and generate an error message for each invalid topic */
         for (final Entry<Integer, Map<Integer, Set<SpecTopic>>> entry : invalidSpecTopics.entrySet()) {
             final Integer topicId = entry.getKey();
             final Map<Integer, Set<SpecTopic>> revisionsToSpecTopic = entry.getValue();
 
             final List<String> revNumbers = new ArrayList<String>();
             final List<Integer> revisions = new ArrayList<Integer>(revisionsToSpecTopic.keySet());
             Collections.sort(revisions, new NullNumberSort<Integer>());
 
             for (final Integer revision : revisions) {
                 final List<SpecTopic> specTopics = new ArrayList<SpecTopic>(revisionsToSpecTopic.get(revision));
 
                 // Build up the line numbers message
                 final StringBuilder lineNumbers = new StringBuilder();
                 if (specTopics.size() > 1) {
                     /* Sort the Topics by line numbers */
                     Collections.sort(specTopics, new SpecTopicLineNumberComparator());
 
                     for (int i = 0; i < specTopics.size(); i++) {
                         if (i == specTopics.size() - 1) {
                             lineNumbers.append(" and ");
                         } else if (lineNumbers.length() != 0) {
                             lineNumbers.append(", ");
                         }
 
                         lineNumbers.append(specTopics.get(i).getLineNumber());
                     }
                 } else if (specTopics.size() == 1) {
                     lineNumbers.append(specTopics.get(0).getLineNumber());
                 }
 
                 // Build the revision message
                 revNumbers.add(format(ProcessorConstants.ERROR_TOPIC_WITH_DIFFERENT_REVS_REV_MSG,
                         (revision == null ? "Latest" : revision), lineNumbers));
             }
 
             final StringBuilder message = new StringBuilder(format(ProcessorConstants
                     .ERROR_TOPIC_WITH_DIFFERENT_REVS_MSG, topicId));
             for (final String revNumber : revNumbers) {
                 message.append(format(ProcessorConstants.CSLINE_MSG, revNumber));
             }
 
             log.error(message.toString());
         }
 
         return valid;
     }
 
     /**
      * Validates that a Content Specification is valid by checking the META data, child levels and topics.
      *
      * @param contentSpec The content specification to be validated.
      * @param specTopics  The list of topics that exist within the content specification.
      * @return True if the content specification is valid, otherwise false.
      */
     @SuppressWarnings("deprecation")
     public boolean postValidateContentSpec(final ContentSpec contentSpec, final Map<String, SpecTopic> specTopics) {
         locale = contentSpec.getLocale() == null ? locale : contentSpec.getLocale();
 
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             shutdown.set(true);
             return false;
         }
 
         boolean valid = true;
 
         // If editing then check that the ID exists & the CHECKSUM/SpecRevision match
        if (contentSpec.getId() != null) {
             final RESTTopicV1 contentSpecTopic = reader.getPostContentSpecById(contentSpec.getId(),
                     processingOptions.getRevision());
             if (contentSpecTopic == null) {
                 log.error(format(ProcessorConstants.ERROR_INVALID_CS_ID_MSG, "ID=" + contentSpec.getId()));
                 valid = false;
             } else {
                 /* Set the revision the content spec is being validated for */
                 contentSpec.setRevision(contentSpecTopic.getRevision());
 
                 // Check that the checksum is valid
                 if (!processingOptions.isIgnoreChecksum()) {
                     final String currentChecksum = HashUtilities.generateMD5(contentSpecTopic.getXml().replaceFirst(
                             "CHECKSUM[ ]*=.*(\r)?\n", ""));
                     if (contentSpec.getChecksum() != null) {
                         if (!contentSpec.getChecksum().equals(currentChecksum)) {
                             log.error(format(ProcessorConstants.ERROR_CS_NONMATCH_CHECKSUM_MSG,
                                     contentSpec.getChecksum(), currentChecksum));
                             valid = false;
                         }
                     } else if (contentSpec.getSpecRevision() != null) {
                         // Check that the revision matches
                         int latestRev = reader.getLatestCSRevById(contentSpec.getId());
                         if (contentSpec.getSpecRevision() != latestRev) {
                             log.error(format(ProcessorConstants.ERROR_CS_NONMATCH_SPEC_REVISION_MSG,
                                     contentSpec.getSpecRevision(), latestRev));
                             valid = false;
                         }
                     } else {
                         log.error(format(ProcessorConstants.ERROR_CS_NONMATCH_CHECKSUM_MSG, null, currentChecksum));
                         valid = false;
                     }
                 }
 
                 // Check that the Content Spec isn't read only
                 if (ComponentTopicV1.returnProperty(contentSpecTopic, CSConstants.CSP_READ_ONLY_PROPERTY_TAG_ID) !=
                         null) {
                     if (!ComponentTopicV1.returnProperty(contentSpecTopic, CSConstants.CSP_READ_ONLY_PROPERTY_TAG_ID)
                             .getValue().matches("(^|.*,)" + contentSpec.getCreatedBy() + "(,.*|$)")) {
                         log.error(ProcessorConstants.ERROR_CS_READ_ONLY_MSG);
                         valid = false;
                     }
                 }
             }
         }
 
         // Check that the injection options are valid
         if (contentSpec.getInjectionOptions() != null) {
             for (final String injectionType : contentSpec.getInjectionOptions().getStrictTopicTypes()) {
                 final List<RESTTagV1> tags = reader.getTagsByName(injectionType);
                 if (tags.size() == 1) {
                     if (!ComponentTagV1.containedInCategory(tags.get(0), CSConstants.TYPE_CATEGORY_ID)) {
                         log.error(format(ProcessorConstants.ERROR_INVALID_INJECTION_TYPE_MSG, injectionType));
                         valid = false;
                     }
                 } else {
                     log.error(format(ProcessorConstants.ERROR_INVALID_INJECTION_TYPE_MSG, injectionType));
                     valid = false;
                 }
             }
         }
 
         // Check that each level is valid
         if (!postValidateLevel(contentSpec.getBaseLevel(), specTopics, contentSpec.getAllowEmptyLevels(),
                 contentSpec.getBookType())) {
             valid = false;
         }
 
         // reset the locale back to its default
         this.locale = CommonConstants.DEFAULT_LOCALE;
 
         return valid;
     }
 
     /**
      * Validate a set of relationships created when parsing.
      *
      * @param relationships A list of all the relationships in a content specification.
      * @param specTopics    The list of topics that exist within the content specification.
      * @param targetLevels  The list of target levels in a content specification.
      * @param targetTopics  The list of target topics in a content specification.
      * @return True if the relationships are valid, otherwise false.
      */
     public boolean preValidateRelationships(final HashMap<String, List<Relationship>> relationships,
                                             final HashMap<String, SpecTopic> specTopics, final HashMap<String,
             Level> targetLevels,
                                             final HashMap<String, SpecTopic> targetTopics) {
         boolean error = false;
         for (final Entry<String, List<Relationship>> relationshipEntry : relationships.entrySet()) {
             final String topicId = relationshipEntry.getKey();
             final SpecTopic specTopic = specTopics.get(topicId);
 
             // Check if the app should be shutdown
             if (isShuttingDown.get()) {
                 shutdown.set(true);
                 return false;
             }
 
             for (final Relationship relationship : relationshipEntry.getValue()) {
                 // Check if the app should be shutdown
                 if (isShuttingDown.get()) {
                     shutdown.set(true);
                     return false;
                 }
 
                 final String relatedId = relationship.getSecondaryRelationshipTopicId();
                 // The relationship points to a target so it must be a level or topic
                 if (relatedId.toUpperCase().matches(ProcessorConstants.TARGET_REGEX)) {
                     if (targetTopics.containsKey(relatedId) && !targetLevels.containsKey(relatedId)) {
                         /*
                          * final SpecTopic targetTopic = targetTopics.get(relatedId); if (relationship
                          * .getRelationshipTitle() !=
                          * null && !relationship.getRelationshipTitle().equals(targetTopic.getTitle())) { if
                          * (!processingOptions.isPermissiveMode()) {
                          * log.error(String.format(ProcessorConstants.ERROR_RELATED_TITLE_NO_MATCH_MSG,
                          * specTopics.get(topicId).getLineNumber(), relationship.getRelationshipTitle(),
                          * targetTopic.getTitle())); error = true; } }
                          */
                     } else if (!targetTopics.containsKey(relatedId) && targetLevels.containsKey(relatedId)) {
                         // final Level targetLevel = targetLevels.get(relatedId);
                         if (relationship.getType() == RelationshipType.NEXT) {
                             log.error(format(ProcessorConstants.ERROR_NEXT_RELATED_LEVEL_MSG, specTopic.getLineNumber(),
                                     specTopic.getText()));
                             error = true;
                         } else if (relationship.getType() == RelationshipType.PREVIOUS) {
                             log.error(format(ProcessorConstants.ERROR_PREV_RELATED_LEVEL_MSG, specTopic.getLineNumber(),
                                     specTopic.getText()));
                             error = true;
                         }
                         /*
                          * else if (relationship.getRelationshipTitle() != null &&
                          * !relationship.getRelationshipTitle().equals(targetLevel.getTitle())) { if
                          * (!processingOptions.isPermissiveMode()) {
                          * log.error(String.format(ProcessorConstants.ERROR_RELATED_TITLE_NO_MATCH_MSG,
                          * specTopics.get(topicId).getLineNumber(), relationship.getRelationshipTitle(),
                          * targetLevel.getTitle())); error = true; } }
                          */
                     } else {
                         log.error(format(ProcessorConstants.ERROR_TARGET_NONEXIST_MSG, specTopic.getLineNumber(),
                                 relatedId, specTopic.getText()));
                         error = true;
                     }
                     // The relationship isn't a target so it must point to a topic directly
                 } else {
                     if (!relatedId.matches(CSConstants.NEW_TOPIC_ID_REGEX)) {
                         // The relationship isn't a unique new topic so it will contain the line number in front of
                         // the topic ID
                         if (relatedId.startsWith("X")) {
                             // Duplicated topics are never unique so throw an error straight away.
                             log.error(format(ProcessorConstants.ERROR_INVALID_DUPLICATE_RELATIONSHIP_MSG,
                                     specTopic.getLineNumber(), specTopic.getText()));
                             error = true;
                         } else {
                             int count = 0;
                             final List<SpecTopic> relatedTopics = new ArrayList<SpecTopic>();
                             // Get the related topic and count if more then one is found
                             for (final Entry<String, SpecTopic> entry : specTopics.entrySet()) {
                                 final String specTopicId = entry.getKey();
 
                                 if (specTopicId.matches("^[0-9]+-" + relatedId + "$")) {
                                     relatedTopics.add(entry.getValue());
                                     count++;
                                 }
                             }
 
                             if (count > 1) {
                                 // Build up the line numbers message
                                 final StringBuilder lineNumbers = new StringBuilder();
                                 for (int i = 0; i < relatedTopics.size(); i++) {
                                     if (i == relatedTopics.size() - 1) {
                                         lineNumbers.append(" and ");
                                     } else if (lineNumbers.length() != 0) {
                                         lineNumbers.append(", ");
                                     }
 
                                     lineNumbers.append(relatedTopics.get(i).getLineNumber());
                                 }
 
                                 log.error(format(ProcessorConstants.ERROR_INVALID_RELATIONSHIP_MSG,
                                         specTopic.getLineNumber(), relatedId, lineNumbers.toString(),
                                         specTopic.getText()));
                                 error = true;
                             } else if (count == 0) {
                                 log.error(format(ProcessorConstants.ERROR_RELATED_TOPIC_NONEXIST_MSG,
                                         specTopic.getLineNumber(), relatedId, specTopic.getText()));
                                 error = true;
                             } else {
                                 // Check to make sure the topic doesn't relate to itself
                                 final SpecTopic relatedTopic = relatedTopics.get(0);
                                 if (relatedTopic == specTopic) {
                                     log.error(format(ProcessorConstants.ERROR_TOPIC_RELATED_TO_ITSELF_MSG,
                                             specTopic.getLineNumber(), specTopic.getText()));
                                 }
 
                                 // Check to ensure the title matches
                                 /*
                                  * if (relationship.getRelationshipTitle() != null &&
                                  * !relationship.getRelationshipTitle().equals(relatedTopic.getTitle())) { if
                                  * (!processingOptions.isPermissiveMode()) {
                                  * log.error(String.format(ProcessorConstants.ERROR_RELATED_TITLE_NO_MATCH_MSG,
                                  * specTopic.getLineNumber(), relationship.getRelationshipTitle(),
                                  * relatedTopic.getTitle()));
                                  * error = true; } }
                                  */
                             }
                         }
                     } else {
                         if (specTopics.containsKey(relatedId)) {
                             final SpecTopic relatedTopic = specTopics.get(relatedId);
 
                             // Check to make sure the topic doesn't relate to itself
                             if (relatedTopic == specTopic) {
                                 log.error(format(ProcessorConstants.ERROR_TOPIC_RELATED_TO_ITSELF_MSG,
                                         specTopic.getLineNumber(), specTopic.getText()));
                             }
                             // Check to ensure the title matches
                             /*
                              * else if (relationship.getRelationshipTitle() != null &&
                              * relationship.getRelationshipTitle().equals(relatedTopic.getTitle())) { if
                              * (!processingOptions.isPermissiveMode()) {
                              * log.error(String.format(ProcessorConstants.ERROR_RELATED_TITLE_NO_MATCH_MSG,
                              * specTopic.getLineNumber(), relationship.getRelationshipTitle(),
                              * relatedTopic.getTitle())); error
                              * = true; } }
                              */
                         } else {
                             log.error(format(ProcessorConstants.ERROR_RELATED_TOPIC_NONEXIST_MSG,
                                     specTopic.getLineNumber(), specTopic.getText()));
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
      * @param level              The level to be validated.
      * @param specTopics         The list of topics that exist within the content specification.
      * @param csAllowEmptyLevels If the "Allow Empty Levels" bit is set in a content specification.
      * @param bookType           The type of book the level should be validated for.
      * @return True if the level is valid otherwise false.
      */
     public boolean preValidateLevel(final Level level, final Map<String, SpecTopic> specTopics,
                                     final boolean csAllowEmptyLevels, final BookType bookType) {
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             shutdown.set(true);
             return false;
         }
 
         boolean valid = true;
 
         // Check that the level isn't empty
         if (level.getNumberOfSpecTopics() <= 0 && level.getNumberOfChildLevels() <= 0 /*
                                                                                        * && !allowEmptyLevels &&
                                                                                        * (allowEmptyLevels &&
                                                                                        * !csAllowEmptyLevels)
                                                                                        */) {
             log.error(format(ProcessorConstants.ERROR_LEVEL_NO_TOPICS_MSG, level.getLineNumber(), level.getType()
                     .getTitle(), level.getType().getTitle(), level.getText()));
             valid = false;
         }
 
         if (level.getType() == null) {
             log.error(ProcessorConstants.ERROR_PROCESSING_ERROR_MSG);
             valid = false;
         }
 
         if (level.getTitle() == null || level.getTitle().equals("")) {
             log.error(format(ProcessorConstants.ERROR_LEVEL_NO_TITLE_MSG, level.getLineNumber(), level.getType()
                     .getTitle(), level.getText()));
             valid = false;
         }
 
         // Validate the sub levels and topics
         for (final Node childNode : level.getChildNodes()) {
             if (childNode instanceof Level) {
                 if (!preValidateLevel((Level) childNode, specTopics, csAllowEmptyLevels, bookType)) {
                     valid = false;
                 }
             } else if (childNode instanceof SpecTopic) {
                 if (!preValidateTopic((SpecTopic) childNode, specTopics, bookType)) {
                     valid = false;
                 }
             }
         }
 
         // Validate certain requirements depending on the type of level
         if (bookType == BookType.ARTICLE || bookType == BookType.ARTICLE_DRAFT) {
             switch (level.getType()) {
                 case APPENDIX:
                     if (!(level.getParent().getType() == LevelType.BASE)) {
                         log.error(format(ProcessorConstants.ERROR_ARTICLE_NESTED_APPENDIX_MSG, level.getLineNumber(),
                                 level.getText()));
                         valid = false;
                     }
 
                     /* Check that the appendix is at the end of the article */
                     final Integer nodeListId = level.getParent().getChildNodes().indexOf(level);
                     final ListIterator<Node> parentNodes = level.getParent().getChildNodes().listIterator(nodeListId);
 
                     while (parentNodes.hasNext()) {
                         final Node node = parentNodes.next();
                         if (node instanceof Level && !(node instanceof Appendix)) {
                             log.error(format(ProcessorConstants.ERROR_CS_APPENDIX_STRUCTURE_MSG, level.getLineNumber(),
                                     level.getText()));
                             valid = false;
                         }
                     }
                     break;
                 case CHAPTER:
                     log.error(format(ProcessorConstants.ERROR_ARTICLE_CHAPTER_MSG, level.getLineNumber(),
                             level.getText()));
                     valid = false;
                     break;
                 case PROCESS:
                     log.error(format(ProcessorConstants.ERROR_ARTICLE_PROCESS_MSG, level.getLineNumber(),
                             level.getText()));
                     valid = false;
                     break;
                 case PART:
                     log.error(format(ProcessorConstants.ERROR_ARTICLE_PART_MSG, level.getLineNumber(),
                             level.getText()));
                     valid = false;
                     break;
                 case SECTION:
                     if (!(level.getParent().getType() == LevelType.BASE || level.getParent().getType() == LevelType
                             .SECTION)) {
                         log.error(format(ProcessorConstants.ERROR_ARTICLE_SECTION_MSG, level.getLineNumber(),
                                 level.getText()));
                         valid = false;
                     }
                     break;
                 default:
                     break;
             }
         }
         // Generic book based validation
         else {
             switch (level.getType()) {
                 case APPENDIX:
                     if (!(level.getParent().getType() == LevelType.BASE || level.getParent().getType() == LevelType
                             .PART)) {
                         log.error(format(ProcessorConstants.ERROR_CS_NESTED_APPENDIX_MSG, level.getLineNumber(),
                                 level.getText()));
                         valid = false;
                     }
 
                     /* Check that the appendix is at the end of the book */
                     final Integer nodeListId = level.getParent().getChildNodes().indexOf(level);
                     final ListIterator<Node> parentNodes = level.getParent().getChildNodes().listIterator(nodeListId);
 
                     while (parentNodes.hasNext()) {
                         final Node node = parentNodes.next();
                         if (node instanceof Level && !(node instanceof Appendix)) {
                             log.error(format(ProcessorConstants.ERROR_CS_APPENDIX_STRUCTURE_MSG, level.getLineNumber(),
                                     level.getText()));
                             valid = false;
                         }
                     }
 
                     break;
                 case CHAPTER:
                     if (!(level.getParent().getType() == LevelType.BASE || level.getParent().getType() == LevelType
                             .PART)) {
                         log.error(format(ProcessorConstants.ERROR_CS_NESTED_CHAPTER_MSG, level.getLineNumber(),
                                 level.getText()));
                         valid = false;
                     }
                     break;
                 case PROCESS:
                     // Check that the process has no children
                     Process process = (Process) level;
                     if (process.getNumberOfChildLevels() != 0) {
                         log.error(format(ProcessorConstants.ERROR_PROCESS_HAS_LEVELS_MSG, process.getLineNumber(),
                                 process.getText()));
                         valid = false;
                     }
                     break;
                 case PART:
                     if (level.getParent().getType() != LevelType.BASE) {
                         log.error(format(ProcessorConstants.ERROR_CS_NESTED_PART_MSG, level.getLineNumber(),
                                 level.getText()));
                         valid = false;
                     }
                     break;
                 case SECTION:
                     if (!(level.getParent().getType() == LevelType.APPENDIX || level.getParent().getType() ==
                             LevelType.CHAPTER || level
                             .getParent().getType() == LevelType.SECTION)) {
                         log.error(format(ProcessorConstants.ERROR_CS_SECTION_NO_CHAPTER_MSG, level.getLineNumber(),
                                 level.getText()));
                         valid = false;
                     }
                     break;
                 default:
                     break;
             }
         }
 
         return valid;
     }
 
     /**
      * Validates a level to ensure its format and child levels/topics are valid.
      *
      * @param level              The level to be validated.
      * @param specTopics         The list of topics that exist within the content specification.
      * @param csAllowEmptyLevels If the "Allow Empty Levels" bit is set in a content specification.
      * @param bookType           The type of book the level should be validated for.
      * @return True if the level is valid otherwise false.
      */
     public boolean postValidateLevel(final Level level, final Map<String, SpecTopic> specTopics,
                                      final boolean csAllowEmptyLevels, final BookType bookType) {
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             shutdown.set(true);
             return false;
         }
 
         boolean valid = true;
 
         // Validate the tags
         if (!validateTopicTags(level, level.getTags(false))) {
             valid = false;
         }
 
         // Validate the sub levels and topics
         for (final Node childNode : level.getChildNodes()) {
             if (childNode instanceof Level) {
                 if (!postValidateLevel((Level) childNode, specTopics, csAllowEmptyLevels, bookType)) {
                     valid = false;
                 }
             } else if (childNode instanceof SpecTopic) {
                 if (!postValidateTopic((SpecTopic) childNode, specTopics, bookType)) {
                     valid = false;
                 }
             }
         }
 
         return valid;
     }
 
     /**
      * Validates a topic against the database and for formatting issues.
      *
      * @param specTopic  The topic to be validated.
      * @param specTopics The list of topics that exist within the content specification.
      * @param bookType   The type of book the topic is to be validated against.
      * @return True if the topic is valid otherwise false.
      */
     public boolean preValidateTopic(final SpecTopic specTopic, final Map<String, SpecTopic> specTopics,
                                     final BookType bookType) {
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             shutdown.set(true);
             return false;
         }
 
         boolean valid = true;
 
         // Check that the topic exists in the spec by checking it's step
         if (specTopic.getStep() == 0) {
             log.error(ProcessorConstants.ERROR_PROCESSING_ERROR_MSG);
             valid = false;
         }
 
         // Checks that the id isn't null and is a valid topic ID
         if (specTopic.getId() == null || !specTopic.getId().matches(CSConstants.ALL_TOPIC_ID_REGEX)) {
             log.error(format(ProcessorConstants.ERROR_INVALID_TOPIC_ID_MSG, specTopic.getLineNumber(),
                     specTopic.getText()));
             valid = false;
         }
 
         if (bookType != BookType.ARTICLE && bookType != BookType.ARTICLE_DRAFT) {
             // Check that the topic is inside a chapter/section/process/appendix/part
             if (specTopic.getParent() == null
                     || !(specTopic.getParent().getType() == LevelType.CHAPTER
                     || specTopic.getParent().getType() == LevelType.APPENDIX
                     || specTopic.getParent().getType() == LevelType.PROCESS
                     || specTopic.getParent().getType() == LevelType.SECTION || specTopic.getParent().getType() ==
                     LevelType.PART)) {
                 log.error(format(ProcessorConstants.ERROR_TOPIC_OUTSIDE_CHAPTER_MSG, specTopic.getLineNumber(),
                         specTopic.getText()));
                 valid = false;
             }
 
             // Check that there are no levels in the parent part (ie the topic is in the intro)
             if (specTopic.getParent() != null && specTopic.getParent().getType() == LevelType.PART) {
                 final LinkedList<Node> parentChildren = specTopic.getParent().getChildNodes();
                 final int index = parentChildren.indexOf(specTopic);
 
                 for (int i = 0; i < index; i++) {
                     final Node node = parentChildren.get(i);
                     if (node instanceof Level) {
                         log.error(format(ProcessorConstants.ERROR_TOPIC_NOT_IN_PART_INTRO_MSG,
                                 specTopic.getLineNumber(), specTopic.getText()));
                         valid = false;
                         break;
                     }
                 }
             }
         }
 
         // Check that the title exists
         if (specTopic.getTitle() == null || specTopic.getTitle().equals("")) {
             log.error(format(ProcessorConstants.ERROR_TOPIC_NO_TITLE_MSG, specTopic.getLineNumber(),
                     specTopic.getText()));
             valid = false;
         }
         // Check that it is valid when escaped
         else if (DocBookUtilities.escapeTitle(specTopic.getTitle()).isEmpty()) {
             log.error(format(ProcessorConstants.ERROR_INVALID_TOPIC_TITLE_MSG, specTopic.getLineNumber(),
                     specTopic.getText()));
             valid = false;
         }
 
         // Check that we aren't using translations for anything but existing topics
         if (!specTopic.isTopicAnExistingTopic()) {
             // Check that we aren't processing translations
             if (clazz == RESTTranslatedTopicV1.class) {
                 log.error(format(ProcessorConstants.ERROR_TOPIC_NO_NEW_TRANSLATION_TOPIC, specTopic.getLineNumber(),
                         specTopic.getText()));
                 valid = false;
             }
         }
 
         // Check that we are allowed to create new topics
         if (!specTopic.isTopicAnExistingTopic() && !processingOptions.isAllowNewTopics()) {
             log.error(format(ProcessorConstants.ERROR_TOPIC_NO_NEW_TOPIC_BUILD, specTopic.getLineNumber(),
                     specTopic.getText()));
             valid = false;
         }
 
         // New Topics
         if (specTopic.isTopicANewTopic()) {
             if (specTopic.getType() == null || specTopic.getType().equals("")) {
                 log.error(format(ProcessorConstants.ERROR_TOPIC_NO_TYPE_MSG, specTopic.getLineNumber(),
                         specTopic.getText()));
                 valid = false;
             }
 
             // Check Assigned Writer exists
             if (!preValidateAssignedWriter(specTopic)) {
                 valid = false;
             }
             // Existing Topics
         } else if (specTopic.isTopicAnExistingTopic()) {
             // Check that tags aren't trying to be removed
             if (!specTopic.getRemoveTags(false).isEmpty()) {
                 log.error(format(ProcessorConstants.ERROR_TOPIC_EXISTING_TOPIC_CANNOT_REMOVE_TAGS,
                         specTopic.getLineNumber(), specTopic.getText()));
                 valid = false;
             }
 
             // Check that the assigned writer, description and source URLS haven't been set
             if (specTopic.getAssignedWriter(false) != null || specTopic.getDescription(false) != null
                     || !specTopic.getSourceUrls(false).isEmpty()) {
                 log.error(format(ProcessorConstants.ERROR_TOPIC_EXISTING_BAD_OPTIONS, specTopic.getLineNumber(),
                         specTopic.getText()));
                 valid = false;
             }
 
             // Check that we aren't processing translations
             if (!specTopic.getTags(true).isEmpty() && clazz == RESTTranslatedTopicV1.class) {
                 log.error(format(ProcessorConstants.ERROR_TOPIC_NO_TAGS_TRANSLATION_TOPIC, specTopic.getLineNumber(),
                         specTopic.getText()));
                 valid = false;
             }
         }
         // Duplicated Topics
         else if (specTopic.isTopicADuplicateTopic()) {
             String temp = "N" + specTopic.getId().substring(1);
 
             // Check that the topic exists in the content specification
             if (!specTopics.containsKey(temp)) {
                 log.error(format(ProcessorConstants.ERROR_TOPIC_NONEXIST_MSG, specTopic.getLineNumber(),
                         specTopic.getText()));
                 valid = false;
             } else {
                 // Check that the topic titles match the original
                 if (!specTopic.getTitle().equals(specTopics.get(temp).getTitle())) {
                     String topicTitleMsg = "Topic " + specTopic.getId() + ": " + specTopics.get(temp).getTitle();
                     log.error(format(ProcessorConstants.ERROR_TOPIC_TITLES_NONMATCH_MSG, specTopic.getLineNumber(),
                             specTopic.getText(), topicTitleMsg));
                     valid = false;
                 }
             }
             // Cloned Topics
         } else if (specTopic.isTopicAClonedTopic()) {
             // Check if a description or type exists. If one does then generate a warning.
             if ((specTopic.getType() != null && !specTopic.getType().equals(""))
                     || (specTopic.getDescription(false) != null && !specTopic.getDescription(false).equals(""))) {
                 String format = "";
                 if (specTopic.getType() != null && !specTopic.getType().equals("")) {
                     format += format(ProcessorConstants.WARN_TYPE_IGNORE_MSG, specTopic.getLineNumber(), "Cloned");
                 }
 
                 if (specTopic.getDescription(false) != null && !specTopic.getDescription(false).equals("")) {
                     if (!format.equals("")) {
                         format += "\n       ";
                     }
                     format +=
                             format(ProcessorConstants.WARN_DESCRIPTION_IGNORE_MSG, specTopic.getLineNumber(), "Cloned");
                 }
 
                 log.warn(format("%s" + ProcessorConstants.CSLINE_MSG, format, specTopic.getText()));
             }
             // Duplicated Cloned Topics
         } else if (specTopic.isTopicAClonedDuplicateTopic()) {
             // Find the duplicate topic in the content spec
             final String temp = specTopic.getId().substring(1);
             int count = 0;
             SpecTopic clonedTopic = null;
             for (final Entry<String, SpecTopic> entry : specTopics.entrySet()) {
                 final String topicId = entry.getKey();
 
                 if (topicId.endsWith(temp) && !topicId.endsWith(specTopic.getId())) {
                     clonedTopic = entry.getValue();
                     count++;
                 }
             }
 
             // Check that the topic exists
             if (count == 0) {
                 log.error(format(ProcessorConstants.ERROR_TOPIC_NONEXIST_MSG, specTopic.getLineNumber(),
                         specTopic.getText()));
                 valid = false;
             }
             // Check that the referenced topic is unique
             else if (count > 1) {
                 log.error(format(ProcessorConstants.ERROR_TOPIC_DUPLICATE_CLONES_MSG, specTopic.getLineNumber(),
                         specTopic.getText()));
                 valid = false;
             } else {
                 // Check that the title matches
                 if (!specTopic.getTitle().equals(clonedTopic.getTitle())) {
                     String topicTitleMsg = "Topic " + specTopic.getId() + ": " + clonedTopic.getTitle();
                     log.error(format(ProcessorConstants.ERROR_TOPIC_TITLES_NONMATCH_MSG, specTopic.getLineNumber(),
                             specTopic.getText(), topicTitleMsg));
                     valid = false;
                 }
             }
         }
         return valid;
     }
 
     /**
      * Validates a topic against the database and for formatting issues.
      *
      * @param specTopic  The topic to be validated.
      * @param specTopics The list of topics that exist within the content specification.
      * @param bookType   The type of book the topic is to be validated against.
      * @return True if the topic is valid otherwise false.
      */
     @SuppressWarnings("unchecked")
     public boolean postValidateTopic(final SpecTopic specTopic, final Map<String, SpecTopic> specTopics,
                                      final BookType bookType) {
         // Check if the app should be shutdown
         if (isShuttingDown.get()) {
             shutdown.set(true);
             return false;
         }
 
         boolean valid = true;
 
         // New Topics
         if (specTopic.isTopicANewTopic()) {
 
             // Check that the type entered exists
             final RESTTagV1 type = reader.getTypeByName(specTopic.getType());
             if (type == null) {
                 log.error(format(ProcessorConstants.ERROR_TYPE_NONEXIST_MSG, specTopic.getLineNumber(),
                         specTopic.getText()));
                 valid = false;
             }
 
             // Validate the tags
             if (!validateTopicTags(specTopic, specTopic.getTags(false))) {
                 valid = false;
             }
 
             // Check Assigned Writer exists
             if (!postValidateAssignedWriter(specTopic)) {
                 valid = false;
             }
         }
         // Existing Topics
         else if (specTopic.isTopicAnExistingTopic()) {
             // Calculate the revision for the topic
             final Integer revision;
             if (specTopic.getRevision() == null || processingOptions.isUpdateRevisions()) {
                 revision = processingOptions.getRevision();
             } else {
                 revision = specTopic.getRevision();
             }
 
             // Check that the id actually exists
             final T topic;
             if (clazz == RESTTranslatedTopicV1.class) {
                 topic = (T) reader.getTranslatedTopicByTopicId(Integer.parseInt(specTopic.getId()), revision, locale);
                 if (processingOptions.isAddRevisions()
                         && (specTopic.getRevision() == null || processingOptions.isUpdateRevisions())) {
                     specTopic.setRevision(((RESTTranslatedTopicV1) topic).getTopicRevision());
                 }
             } else {
                 topic = (T) reader.getTopicById(Integer.parseInt(specTopic.getId()), revision);
                 if (processingOptions.isAddRevisions()
                         && (specTopic.getRevision() == null || processingOptions.isUpdateRevisions())) {
                     specTopic.setRevision(topic.getRevision());
                 }
             }
 
             // Check that the topic actually exists
             if (topic == null) {
                 log.error(format(ProcessorConstants.ERROR_TOPIC_ID_NONEXIST_MSG, specTopic.getLineNumber(),
                         specTopic.getText()));
                 return false;
             } else {
                 specTopic.setTopic(topic);
 
                 // Check to see if the topic contains the "Internal-Only" tag
                 if (ComponentBaseTopicV1.hasTag(topic, CSConstants.RH_INTERNAL_TAG_ID)) {
                     log.warn(format(ProcessorConstants.WARN_INTERNAL_TOPIC_MSG, specTopic.getLineNumber(),
                             specTopic.getText()));
                 }
             }
 
             // Check that the topic has a valid id
             if (topic.getId() <= 0) {
                 log.error(format(ProcessorConstants.ERROR_TOPIC_ID_NONEXIST_MSG, specTopic.getLineNumber(),
                         specTopic.getText()));
                 valid = false;
             }
 
             // Validate the title matches if we aren't using permissive mode
             if (!processingOptions.isPermissiveMode() && !specTopic.getTitle().equals(topic.getTitle())) {
                 String topicTitleMsg = "Topic " + specTopic.getId() + ": " + topic.getTitle();
                 log.error(format(ProcessorConstants.ERROR_TOPIC_TITLES_NONMATCH_MSG, specTopic.getLineNumber(),
                         "Specified: " + specTopic.getText(), topicTitleMsg));
                 valid = false;
             }
             // If we are using permissive mode then change the title to the correct title
             else if (processingOptions.isPermissiveMode() && !specTopic.getTitle().equals(topic.getTitle())) {
                 specTopic.setTitle(topic.getTitle());
             }
 
             // Validate the tags
             if (!validateTopicTags(specTopic, specTopic.getTags(false))) {
                 valid = false;
             }
             // Cloned Topics
         } else if (specTopic.isTopicAClonedTopic()) {
             // Get the original topic from the database
             int temp = Integer.parseInt(specTopic.getId().substring(1));
             final RESTTopicV1 topic = reader.getTopicById(temp, null);
 
             // Check that the original topic was found
             if (topic == null) {
                 log.error(format(ProcessorConstants.ERROR_TOPIC_NONEXIST_MSG, specTopic.getLineNumber(),
                         specTopic.getText()));
                 valid = false;
             } else {
                 // Validate the title matches if we aren't using permissive mode
                 if (!processingOptions.isPermissiveMode() && !specTopic.getTitle().equals(topic.getTitle())) {
                     String topicTitleMsg = "Topic " + topic.getId() + ": " + topic.getTitle();
                     log.error(format(ProcessorConstants.ERROR_TOPIC_TITLES_NONMATCH_MSG, specTopic.getLineNumber(),
                             specTopic.getText(), topicTitleMsg));
                     valid = false;
                 }
                 // If we are using permissive mode then change the title to the correct title
                 else if (processingOptions.isPermissiveMode() && !specTopic.getTitle().equals(topic.getTitle())) {
                     specTopic.setTitle(topic.getTitle());
                 }
 
                 // Check Assigned Writer exists
                 if (!postValidateAssignedWriter(specTopic)) {
                     valid = false;
                 }
             }
 
             // Validate the tags
             if (!validateTopicTags(specTopic, specTopic.getTags(false))) {
                 valid = false;
             }
         }
 
         return valid;
     }
 
     /**
      * Checks to make sure that the assigned writer for the topic is valid.
      *
      * @param topic The topic to check the assigned writer for.
      * @return True if the assigned writer exists in the database and is under the Assigned Writer category otherwise
      *         false.
      */
     private boolean preValidateAssignedWriter(final SpecTopic topic) {
         if (topic.getAssignedWriter(true) == null) {
             log.error(format(ProcessorConstants.ERROR_NO_WRITER_MSG, topic.getLineNumber(), topic.getText()));
             return false;
         }
 
         return true;
     }
 
     /**
      * Checks to make sure that the assigned writer for the topic is valid.
      *
      * @param topic The topic to check the assigned writer for.
      * @return True if the assigned writer exists in the database and is under the Assigned Writer category otherwise
      *         false.
      */
     private boolean postValidateAssignedWriter(final SpecTopic topic) {
 
         // Check Assigned Writer exists
         final List<RESTTagV1> tagList = reader.getTagsByName(topic.getAssignedWriter(true));
         if (tagList.size() != 1) {
             log.error(format(ProcessorConstants.ERROR_WRITER_NONEXIST_MSG, topic.getLineNumber(), topic.getText()));
             return false;
         }
 
         // Check that the writer tag is actually part of the Assigned Writer category
         final RESTCategoryInTagV1 cat = reader.getCategoryByTagId(tagList.get(0).getId());
         if (cat == null) {
             log.error(format(ProcessorConstants.ERROR_INVALID_WRITER_MSG, topic.getLineNumber(), topic.getText()));
             return false;
         }
 
         // Check that tag is actually in the Assigned Writer category
         if (cat.getId() != ProcessorConstants.ASSIGNED_WRITER_CATEGORY_ID) {
             log.error(format(ProcessorConstants.ERROR_INVALID_WRITER_MSG, topic.getLineNumber(), topic.getText()));
             return false;
         }
 
         return true;
     }
 
     /**
      * Checks to see if the tags are valid for a particular topic.
      *
      * @param specNode The topic or level the tags below to.
      * @param tagNames A list of all the tags in their string form to be validate.
      * @return True if the tags are valid otherwise false.
      */
     private boolean validateTopicTags(final SpecNode specNode, final List<String> tagNames) {
         boolean valid = true;
         if (!tagNames.isEmpty()) {
             final List<RESTTagV1> tags = new ArrayList<RESTTagV1>();
             for (final String tagName : tagNames) {
                 // Check if the app should be shutdown
                 if (isShuttingDown.get()) {
                     shutdown.set(true);
                     return false;
                 }
 
                 // Get the tag from the database
                 final List<RESTTagV1> tagList = reader.getTagsByName(tagName);
 
                 // Check that it exists
                 if (tagList.size() == 1) {
                     tags.add(tagList.get(0));
                 } else if (tagList.size() == 0) {
                     log.error(format(ProcessorConstants.ERROR_TAG_NONEXIST_MSG, specNode.getLineNumber(), tagName,
                             specNode.getText()));
                     valid = false;
                 } else {
                     log.error(format(ProcessorConstants.ERROR_TOPIC_TAG_DUPLICATED_MSG, specNode.getLineNumber(),
                             specNode.getText()));
                     valid = false;
                 }
             }
 
             // Check that the mutex value entered is correct
             final Map<RESTCategoryInTagV1, List<RESTTagV1>> mapping = ProcessorUtilities
                     .getCategoryMappingFromTagList(tags);
             for (final Entry<RESTCategoryInTagV1, List<RESTTagV1>> catEntry : mapping.entrySet()) {
                 final RESTCategoryInTagV1 cat = catEntry.getKey();
                 final List<RESTTagV1> catTags = catEntry.getValue();
 
                 // Check if the app should be shutdown
                 if (isShuttingDown.get()) {
                     shutdown.set(true);
                     return false;
                 }
 
                 // Check that only one tag has been set if the category is mutually exclusive
                 if (cat.getMutuallyExclusive() && catTags.size() > 1) {
                     log.error(format(ProcessorConstants.ERROR_TOPIC_TOO_MANY_CATS_MSG, specNode.getLineNumber(),
                             cat.getName(), specNode.getText()));
                     valid = false;
                 }
 
                 // Check that the tag isn't a type or writer
                 if (cat.getId().equals(CSConstants.WRITER_CATEGORY_ID)) {
                     log.error(format(ProcessorConstants.ERROR_TOPIC_WRITER_AS_TAG_MSG, specNode.getLineNumber(),
                             specNode.getText()));
                     valid = false;
                 }
 
                 // Check that the tag isn't a topic type
                 if (cat.getId().equals(CSConstants.TYPE_CATEGORY_ID)) {
                     log.error(format(ProcessorConstants.ERROR_TOPIC_TYPE_AS_TAG_MSG, specNode.getLineNumber(),
                             specNode.getText()));
                     valid = false;
                 }
             }
         }
         return valid;
     }
 }
