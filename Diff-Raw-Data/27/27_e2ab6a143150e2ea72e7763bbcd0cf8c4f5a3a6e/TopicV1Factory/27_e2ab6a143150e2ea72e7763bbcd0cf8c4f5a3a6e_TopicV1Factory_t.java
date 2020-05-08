 package org.jboss.pressgang.ccms.server.rest.v1;
 
 import javax.persistence.EntityManager;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.docbook.constants.DocbookBuilderConstants;
 import org.jboss.pressgang.ccms.model.BugzillaBug;
 import org.jboss.pressgang.ccms.model.PropertyTag;
 import org.jboss.pressgang.ccms.model.Tag;
 import org.jboss.pressgang.ccms.model.Topic;
 import org.jboss.pressgang.ccms.model.TopicSourceUrl;
 import org.jboss.pressgang.ccms.model.TopicToPropertyTag;
 import org.jboss.pressgang.ccms.model.TranslatedTopicData;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTBugzillaBugCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicSourceUrlCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTranslatedTopicCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTBugzillaBugCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTagCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTopicCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTopicSourceUrlCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTranslatedTopicCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTAssignedPropertyTagCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTBugzillaBugV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicSourceUrlV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTranslatedTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.enums.RESTXMLDoctype;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTAssignedPropertyTagV1;
 import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
 import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectCollectionFactory;
 import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectFactory;
 import org.jboss.pressgang.ccms.server.utils.EnversUtilities;
 import org.jboss.pressgang.ccms.server.utils.TopicUtilities;
 import org.jboss.resteasy.spi.BadRequestException;
 
 public class TopicV1Factory extends RESTDataObjectFactory<RESTTopicV1, Topic, RESTTopicCollectionV1, RESTTopicCollectionItemV1> {
     public TopicV1Factory() {
         super(Topic.class);
     }
 
     @Override
     public RESTTopicV1 createRESTEntityFromDBEntityInternal(final Topic entity, final String baseUrl, final String dataType,
             final ExpandDataTrunk expand, final Number revision, final boolean expandParentReferences, final EntityManager entityManager) {
         assert entity != null : "Parameter entity can not be null";
         assert baseUrl != null : "Parameter baseUrl can not be null";
 
         final RESTTopicV1 retValue = new RESTTopicV1();
 
         final List<String> expandOptions = new ArrayList<String>();
         expandOptions.add(RESTTopicV1.TAGS_NAME);
         expandOptions.add(RESTTopicV1.INCOMING_NAME);
         expandOptions.add(RESTTopicV1.OUTGOING_NAME);
         expandOptions.add(RESTTopicV1.SOURCE_URLS_NAME);
         expandOptions.add(RESTTopicV1.BUGZILLABUGS_NAME);
         expandOptions.add(RESTTopicV1.PROPERTIES_NAME);
         expandOptions.add(RESTTopicV1.LOG_DETAILS_NAME);
         if (revision == null) expandOptions.add(RESTBaseEntityV1.REVISIONS_NAME);
 
         retValue.setExpand(expandOptions);
 
         /* Set simple properties */
         retValue.setId(entity.getTopicId());
         retValue.setTitle(entity.getTopicTitle());
         retValue.setDescription(entity.getTopicText());
         retValue.setXml(entity.getTopicXML());
         retValue.setHtml(entity.getTopicRendered());
         retValue.setLastModified(EnversUtilities.getFixedLastModifiedDate(entityManager, entity));
         retValue.setCreated(entity.getTopicTimeStamp());
         retValue.setLocale(entity.getTopicLocale());
         retValue.setXmlErrors(entity.getTopicXMLErrors());
         retValue.setXmlDoctype(RESTXMLDoctype.getXMLDoctype(entity.getXmlDoctype()));
 
         // REVISIONS
         if (revision == null && expand != null && expand.contains(RESTTopicV1.REVISIONS_NAME)) {
             retValue.setRevisions(
                     new RESTDataObjectCollectionFactory<RESTTopicV1, Topic, RESTTopicCollectionV1, RESTTopicCollectionItemV1>().create(
                             RESTTopicCollectionV1.class, new TopicV1Factory(), entity, EnversUtilities.getRevisions(entityManager, entity),
                             RESTBaseEntityV1.REVISIONS_NAME, dataType, expand, baseUrl, entityManager));
         }
 
         // TAGS
         if (expand != null && expand.contains(RESTTopicV1.TAGS_NAME)) {
             retValue.setTags(new RESTDataObjectCollectionFactory<RESTTagV1, Tag, RESTTagCollectionV1, RESTTagCollectionItemV1>().create(
                     RESTTagCollectionV1.class, new TagV1Factory(), entity.getTags(), RESTv1Constants.TAGS_EXPANSION_NAME, dataType, expand,
                     baseUrl, entityManager));
         }
 
         // OUTGOING RELATIONSHIPS
         if (expand != null && expand.contains(RESTTopicV1.OUTGOING_NAME)) {
             retValue.setOutgoingRelationships(
                     new RESTDataObjectCollectionFactory<RESTTopicV1, Topic, RESTTopicCollectionV1, RESTTopicCollectionItemV1>().create(
                             RESTTopicCollectionV1.class, new TopicV1Factory(), entity.getOutgoingRelatedTopicsArray(),
                             RESTTopicV1.OUTGOING_NAME, dataType, expand, baseUrl, revision, entityManager));
         }
 
         // INCOMING RELATIONSHIPS
         if (expand != null && expand.contains(RESTTopicV1.INCOMING_NAME)) {
             retValue.setIncomingRelationships(
                     new RESTDataObjectCollectionFactory<RESTTopicV1, Topic, RESTTopicCollectionV1, RESTTopicCollectionItemV1>().create(
                             RESTTopicCollectionV1.class, new TopicV1Factory(), entity.getIncomingRelatedTopicsArray(),
                             RESTTopicV1.INCOMING_NAME, dataType, expand, baseUrl, revision, entityManager));
         }
 
         // PROPERTIES
         if (expand != null && expand.contains(RESTTopicV1.PROPERTIES_NAME)) {
             retValue.setProperties(
                     new RESTDataObjectCollectionFactory<RESTAssignedPropertyTagV1, TopicToPropertyTag,
                             RESTAssignedPropertyTagCollectionV1, RESTAssignedPropertyTagCollectionItemV1>().create(
                             RESTAssignedPropertyTagCollectionV1.class, new TopicPropertyTagV1Factory(),
                             entity.getTopicToPropertyTagsArray(), RESTTopicV1.PROPERTIES_NAME, dataType, expand, baseUrl, revision,
                             entityManager));
         }
 
         // SOURCE URLS
         if (expand != null && expand.contains(RESTTopicV1.SOURCE_URLS_NAME)) {
             retValue.setSourceUrls_OTM(
                     new RESTDataObjectCollectionFactory<RESTTopicSourceUrlV1, TopicSourceUrl, RESTTopicSourceUrlCollectionV1,
                             RESTTopicSourceUrlCollectionItemV1>().create(
                             RESTTopicSourceUrlCollectionV1.class, new TopicSourceUrlV1Factory(), entity.getTopicSourceUrls(),
                             RESTTopicV1.SOURCE_URLS_NAME, dataType, expand, baseUrl, revision, false, entityManager));
         }
 
         // BUGZILLA BUGS
         if (expand != null && expand.contains(RESTTopicV1.BUGZILLABUGS_NAME)) {
             retValue.setBugzillaBugs_OTM(
                     new RESTDataObjectCollectionFactory<RESTBugzillaBugV1, BugzillaBug, RESTBugzillaBugCollectionV1,
                             RESTBugzillaBugCollectionItemV1>().create(
                             RESTBugzillaBugCollectionV1.class, new BugzillaBugV1Factory(), entity.getBugzillaBugs(),
                             RESTTopicV1.BUGZILLABUGS_NAME, dataType, expand, baseUrl, revision, false, entityManager));
         }
 
         // TRANSLATED TOPICS
         if (expand != null && expand.contains(RESTTopicV1.TRANSLATEDTOPICS_NAME)) {
             retValue.setTranslatedTopics_OTM(
                     new RESTDataObjectCollectionFactory<RESTTranslatedTopicV1, TranslatedTopicData, RESTTranslatedTopicCollectionV1,
                             RESTTranslatedTopicCollectionItemV1>().create(
                             RESTTranslatedTopicCollectionV1.class, new TranslatedTopicV1Factory(),
                             entity.getTranslatedTopics(entityManager, revision), RESTTopicV1.TRANSLATEDTOPICS_NAME, dataType, expand,
                             baseUrl, revision, false, entityManager));
         }
 
         retValue.setLinks(baseUrl, RESTv1Constants.TOPIC_URL_NAME, dataType, retValue.getId());
 
         return retValue;
     }
 
     @Override
     public void syncDBEntityWithRESTEntity(final EntityManager entityManager, final Topic entity, final RESTTopicV1 dataObject) {
 
         /* sync the basic properties */
         if (dataObject.hasParameterSet(RESTTopicV1.TITLE_NAME)) entity.setTopicTitle(dataObject.getTitle());
         if (dataObject.hasParameterSet(RESTTopicV1.DESCRIPTION_NAME)) entity.setTopicText(dataObject.getDescription());
         if (dataObject.hasParameterSet(RESTTopicV1.XML_NAME)) entity.setTopicXML(dataObject.getXml());
         if (dataObject.hasParameterSet(RESTTopicV1.HTML_NAME)) entity.setTopicRendered(dataObject.getHtml());
         if (dataObject.hasParameterSet(RESTTopicV1.LOCALE_NAME)) entity.setTopicLocale(dataObject.getLocale());
         if (dataObject.hasParameterSet(RESTTopicV1.DOCTYPE_NAME))
             entity.setXmlDoctype(RESTXMLDoctype.getXMLDoctypeId(dataObject.getXmlDoctype()));
 
         if (dataObject.hasParameterSet(RESTTopicV1.TAGS_NAME) && dataObject.getTags() != null && dataObject.getTags().getItems() != null) {
             dataObject.getTags().removeInvalidChangeItemRequests();
 
             /* Remove Tags first to ensure mutual exclusion is done correctly */
             for (final RESTTagCollectionItemV1 restEntityItem : dataObject.getTags().getItems()) {
                 final RESTTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsRemoveItem()) {
                     final Tag tagEntity = entityManager.find(Tag.class, restEntity.getId());
                     if (tagEntity == null)
                         throw new BadRequestException("No Tag entity was found with the primary key " + restEntity.getId());
 
                     entity.removeTag(restEntity.getId());
                 }
             }
 
             for (final RESTTagCollectionItemV1 restEntityItem : dataObject.getTags().getItems()) {
                 final RESTTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsAddItem()) {
                     final Tag tagEntity = entityManager.find(Tag.class, restEntity.getId());
                     if (tagEntity == null)
                         throw new BadRequestException("No Tag entity was found with the primary key " + restEntity.getId());
 
                     entity.addTag(entityManager, restEntity.getId());
                 }
             }
         }
 
         if (dataObject.hasParameterSet(
                 RESTTopicV1.PROPERTIES_NAME) && dataObject.getProperties() != null && dataObject.getProperties().getItems() != null) {
             dataObject.getProperties().removeInvalidChangeItemRequests();
 
             /* remove children first */
             for (final RESTAssignedPropertyTagCollectionItemV1 restEntityItem : dataObject.getProperties().getItems()) {
                 final RESTAssignedPropertyTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsRemoveItem()) {
                    final TopicToPropertyTag dbEntity = entityManager.find(TopicToPropertyTag.class, restEntity.getRelationshipId());
                    if (dbEntity == null) throw new BadRequestException(
                            "No TopicToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId());
 
                    entity.removePropertyTag(dbEntity);
                 } else if (restEntityItem.returnIsAddItem()) {
                     final PropertyTag dbEntity = entityManager.find(PropertyTag.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No PropertyTag entity was found with the primary key " + restEntity.getId());
 
                     entity.addPropertyTag(dbEntity, restEntity.getValue());
                 } else if (restEntityItem.returnIsUpdateItem()) {
                     final TopicToPropertyTag dbEntity = entityManager.find(TopicToPropertyTag.class, restEntity.getRelationshipId());
                     if (dbEntity == null) throw new BadRequestException(
                             "No TopicToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId());
                     if (!entity.getTopicToPropertyTags().contains(dbEntity)) throw new BadRequestException(
                             "No TopicToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId() +
                                     " for Topic " + entity.getId());
 
                     new TopicPropertyTagV1Factory().syncDBEntityWithRESTEntity(entityManager, dbEntity, restEntity);
                 }
             }
         }
 
         /* This method will set the XML errors field */
         TopicUtilities.syncXML(entityManager, entity);
         TopicUtilities.validateXML(entityManager, entity, DocbookBuilderConstants.ROCBOOK_DTD_BLOB_ID);
 
         /*
          * Persist the entity before adding anything else as they require an id for the topic
          */
         entityManager.persist(entity);
 
         if (dataObject.hasParameterSet(
                 RESTTopicV1.OUTGOING_NAME) && dataObject.getOutgoingRelationships() != null && dataObject.getOutgoingRelationships()
                 .getItems() != null) {
             dataObject.getOutgoingRelationships().removeInvalidChangeItemRequests();
 
             for (final RESTTopicCollectionItemV1 restEntityItem : dataObject.getOutgoingRelationships().getItems()) {
                 final RESTTopicV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsRemoveItem()) {
                     final Topic otherTopic = entityManager.find(Topic.class, restEntity.getId());
                     if (otherTopic == null)
                         throw new BadRequestException("No Topic entity was found with the primary key " + restEntity.getId());
 
                     entity.removeRelationshipTo(restEntity.getId(), 1);
                 } else if (restEntityItem.returnIsAddItem()) {
                     final Topic otherTopic = entityManager.find(Topic.class, restEntity.getId());
                     if (otherTopic == null)
                         throw new BadRequestException("No Topic entity was found with the primary key " + restEntity.getId());
 
                     entity.addRelationshipTo(entityManager, restEntity.getId(), 1);
 
                 }
             }
         }
 
         if (dataObject.hasParameterSet(
                 RESTTopicV1.INCOMING_NAME) && dataObject.getIncomingRelationships() != null && dataObject.getIncomingRelationships()
                 .getItems() != null) {
             dataObject.getIncomingRelationships().removeInvalidChangeItemRequests();
 
             for (final RESTTopicCollectionItemV1 restEntityItem : dataObject.getIncomingRelationships().getItems()) {
                 final RESTTopicV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsRemoveItem()) {
                     final Topic dbEntity = entityManager.find(Topic.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No Topic entity was found with the primary key " + restEntity.getId());
 
                     dbEntity.removeRelationshipTo(entity.getTopicId(), 1);
 
                 } else if (restEntityItem.returnIsAddItem()) {
                     final Topic otherTopic = entityManager.find(Topic.class, restEntity.getId());
                     if (otherTopic == null)
                         throw new BadRequestException("No Topic entity was found with the primary key " + restEntity.getId());
 
                     entity.addRelationshipFrom(entityManager, otherTopic.getTopicId(), 1);
 
                 }
             }
         }
 
         /* One To Many - Add will create a child entity */
         if (dataObject.hasParameterSet(
                 RESTTopicV1.SOURCE_URLS_NAME) && dataObject.getSourceUrls_OTM() != null && dataObject.getSourceUrls_OTM().getItems() !=
                 null) {
             dataObject.getSourceUrls_OTM().removeInvalidChangeItemRequests();
 
             for (final RESTTopicSourceUrlCollectionItemV1 restEntityItem : dataObject.getSourceUrls_OTM().getItems()) {
                 final RESTTopicSourceUrlV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsRemoveItem()) {
                     final TopicSourceUrl dbEntity = entityManager.find(TopicSourceUrl.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No TopicSourceUrl entity was found with the primary key " + restEntity.getId());
 
                     entity.removeTopicSourceUrl(restEntity.getId());
                 } else if (restEntityItem.returnIsAddItem()) {
                     final TopicSourceUrl dbEntity = new TopicSourceUrlV1Factory().createDBEntityFromRESTEntity(entityManager, restEntity);
                     entityManager.persist(dbEntity);
                     entity.addTopicSourceUrl(dbEntity);
                 } else if (restEntityItem.returnIsUpdateItem()) {
                     final TopicSourceUrl dbEntity = entityManager.find(TopicSourceUrl.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No TopicSourceUrl entity was found with the primary key " + restEntity.getId());
                     if (!entity.getTopicSourceUrls().contains(dbEntity))
                         throw new BadRequestException("No TopicSourceUrl entity was found with the primary key " + restEntity.getId() +
                                 " for Topic " + entity.getId());
 
                     new TopicSourceUrlV1Factory().syncDBEntityWithRESTEntity(entityManager, dbEntity, restEntity);
                 }
             }
         }
 
         /* One To Many - Add will create a child entity */
         if (dataObject.hasParameterSet(
                 RESTTopicV1.BUGZILLABUGS_NAME) && dataObject.getBugzillaBugs_OTM() != null && dataObject.getBugzillaBugs_OTM().getItems()
                 != null) {
             dataObject.getBugzillaBugs_OTM().removeInvalidChangeItemRequests();
 
             for (final RESTBugzillaBugCollectionItemV1 restEntityItem : dataObject.getBugzillaBugs_OTM().getItems()) {
                 final RESTBugzillaBugV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsRemoveItem()) {
                     final BugzillaBug dbEntity = entityManager.find(BugzillaBug.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No BugzillaBug entity was found with the primary key " + restEntity.getId());
 
                     entity.removeBugzillaBug(restEntity.getId());
                 } else if (restEntityItem.returnIsAddItem()) {
                     final BugzillaBug dbEntity = new BugzillaBugV1Factory().createDBEntityFromRESTEntity(entityManager, restEntity);
                     entityManager.persist(dbEntity);
                     entity.addBugzillaBug(dbEntity);
                 } else if (restEntityItem.returnIsUpdateItem()) {
                     final BugzillaBug dbEntity = entityManager.find(BugzillaBug.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No BugzillaBug entity was found with the primary key " + restEntity.getId());
                     if (!entity.getBugzillaBugs().contains(dbEntity))
                         throw new BadRequestException("No BugzillaBug entity was found with the primary key " + restEntity.getId() +
                                 " for Topic " + entity.getId());
 
                     new BugzillaBugV1Factory().syncDBEntityWithRESTEntity(entityManager, dbEntity, restEntity);
                 }
             }
         }
     }
 }
