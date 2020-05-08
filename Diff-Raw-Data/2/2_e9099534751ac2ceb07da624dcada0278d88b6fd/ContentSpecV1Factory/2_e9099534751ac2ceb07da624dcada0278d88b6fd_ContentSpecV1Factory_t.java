 package org.jboss.pressgang.ccms.restserver.rest.v1;
 
 import javax.persistence.EntityManager;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.model.PropertyTag;
 import org.jboss.pressgang.ccms.model.Tag;
 import org.jboss.pressgang.ccms.model.contentspec.CSNode;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpecToPropertyTag;
 import org.jboss.pressgang.ccms.model.contentspec.TranslatedContentSpec;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.RESTCSNodeCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.RESTContentSpecCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.RESTTranslatedContentSpecCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTagCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTAssignedPropertyTagCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.enums.RESTContentSpecTypeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTAssignedPropertyTagV1;
 import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
 import org.jboss.pressgang.ccms.restserver.rest.v1.base.RESTDataObjectCollectionFactory;
 import org.jboss.pressgang.ccms.restserver.rest.v1.base.RESTDataObjectFactory;
 import org.jboss.pressgang.ccms.restserver.utils.ContentSpecUtilities;
 import org.jboss.pressgang.ccms.restserver.utils.EnversUtilities;
 import org.jboss.resteasy.spi.BadRequestException;
 
 public class ContentSpecV1Factory extends RESTDataObjectFactory<RESTContentSpecV1, ContentSpec, RESTContentSpecCollectionV1,
         RESTContentSpecCollectionItemV1> {
 
     public ContentSpecV1Factory() {
         super(ContentSpec.class);
     }
 
     @Override
     public RESTContentSpecV1 createRESTEntityFromDBEntityInternal(final ContentSpec entity, final String baseUrl, final String dataType,
             final ExpandDataTrunk expand, final Number revision, final boolean expandParentReferences, final EntityManager entityManager) {
         assert entity != null : "Parameter entity can not be null";
         assert baseUrl != null : "Parameter baseUrl can not be null";
 
         final RESTContentSpecV1 retValue = new RESTContentSpecV1();
 
         final List<String> expandOptions = new ArrayList<String>();
         expandOptions.add(RESTBaseEntityV1.LOG_DETAILS_NAME);
         expandOptions.add(RESTContentSpecV1.CHILDREN_NAME);
         expandOptions.add(RESTContentSpecV1.PROPERTIES_NAME);
         expandOptions.add(RESTContentSpecV1.TAGS_NAME);
         expandOptions.add(RESTContentSpecV1.TEXT_NAME);
         if (revision == null) expandOptions.add(RESTBaseEntityV1.REVISIONS_NAME);
         retValue.setExpand(expandOptions);
 
         retValue.setId(entity.getId());
         retValue.setLocale(entity.getLocale());
         retValue.setCondition(entity.getCondition());
         retValue.setType(RESTContentSpecTypeV1.getContentSpecType(entity.getContentSpecType()));
         retValue.setLastPublished(entity.getLastPublished());
         retValue.setLastModified(entity.getLastModified());
         retValue.setErrors(entity.getErrors());
         retValue.setFailedContentSpec(entity.getFailedContentSpec());
 
         // REVISIONS
         if (revision == null && expand != null && expand.contains(RESTBaseEntityV1.REVISIONS_NAME)) {
             retValue.setRevisions(
                     new RESTDataObjectCollectionFactory<RESTContentSpecV1, ContentSpec, RESTContentSpecCollectionV1,
                             RESTContentSpecCollectionItemV1>().create(
                             RESTContentSpecCollectionV1.class, new ContentSpecV1Factory(), entity,
                             EnversUtilities.getRevisions(entityManager, entity), RESTBaseEntityV1.REVISIONS_NAME, dataType, expand, baseUrl,
                             entityManager));
         }
 
         // CHILDREN NODES
         if (expand != null && expand.contains(RESTContentSpecV1.CHILDREN_NAME)) {
             retValue.setChildren_OTM(
                     new RESTDataObjectCollectionFactory<RESTCSNodeV1, CSNode, RESTCSNodeCollectionV1, RESTCSNodeCollectionItemV1>().create(
                             RESTCSNodeCollectionV1.class, new CSNodeV1Factory(), entity.getTopCSNodes(), RESTContentSpecV1.CHILDREN_NAME,
                             dataType, expand, baseUrl, expandParentReferences, entityManager));
         }
 
         // PROPERTY TAGS
         if (expand != null && expand.contains(RESTContentSpecV1.PROPERTIES_NAME)) {
             retValue.setProperties(
                     new RESTDataObjectCollectionFactory<RESTAssignedPropertyTagV1, ContentSpecToPropertyTag,
                             RESTAssignedPropertyTagCollectionV1, RESTAssignedPropertyTagCollectionItemV1>().create(
                             RESTAssignedPropertyTagCollectionV1.class, new ContentSpecPropertyTagV1Factory(),
                             entity.getContentSpecToPropertyTagsList(), RESTContentSpecV1.PROPERTIES_NAME, dataType, expand, baseUrl,
                             revision, entityManager));
         }
 
         // TAGS
         if (expand != null && expand.contains(RESTContentSpecV1.TAGS_NAME)) {
             retValue.setTags(new RESTDataObjectCollectionFactory<RESTTagV1, Tag, RESTTagCollectionV1, RESTTagCollectionItemV1>().create(
                     RESTTagCollectionV1.class, new TagV1Factory(), entity.getTags(), RESTContentSpecV1.TAGS_NAME, dataType, expand, baseUrl,
                     entityManager));
         }
 
         // TRANSLATIONS
         if (expand != null && expand.contains(RESTContentSpecV1.TRANSLATED_CONTENT_SPECS_NAME)) {
             retValue.setTranslatedContentSpecs(
                     new RESTDataObjectCollectionFactory<RESTTranslatedContentSpecV1, TranslatedContentSpec,
                             RESTTranslatedContentSpecCollectionV1, RESTTranslatedContentSpecCollectionItemV1>().create(
                             RESTTranslatedContentSpecCollectionV1.class, new TranslatedContentSpecV1Factory(),
                             entity.getTranslatedContentSpecs(entityManager, revision), RESTContentSpecV1.TRANSLATED_CONTENT_SPECS_NAME,
                             dataType, expand, baseUrl, entityManager));
         }
 
         // TEXT
         if (expand != null && expand.contains(RESTContentSpecV1.TEXT_NAME)) {
            final String text = ContentSpecUtilities.getContentSpecText(entity.getId(), (Integer) revision, entityManager);
             retValue.setText(text);
         }
 
         retValue.setLinks(baseUrl, RESTv1Constants.CONTENT_SPEC_URL_NAME, dataType, retValue.getId());
 
         return retValue;
     }
 
     @Override
     public void syncDBEntityWithRESTEntity(final EntityManager entityManager, final ContentSpec entity,
             final RESTContentSpecV1 dataObject) {
         // If the Text is being set then don't do anything else.
         if (dataObject.hasParameterSet(RESTContentSpecV1.TEXT_NAME)) return;
 
         if (dataObject.hasParameterSet(RESTContentSpecV1.LOCALE_NAME)) entity.setLocale(dataObject.getLocale());
 
         if (dataObject.hasParameterSet(RESTContentSpecV1.CONDITION_NAME)) entity.setCondition(dataObject.getCondition());
 
         if (dataObject.hasParameterSet(RESTContentSpecV1.LAST_PUBLISHED_NAME)) entity.setLastPublished(dataObject.getLastPublished());
 
         if (dataObject.hasParameterSet(RESTContentSpecV1.TYPE_NAME))
             entity.setContentSpecType(RESTContentSpecTypeV1.getContentSpecTypeId(dataObject.getType()));
 
         entityManager.persist(entity);
 
         /* Many To Many */
         if (dataObject.hasParameterSet(
                 RESTContentSpecV1.PROPERTIES_NAME) && dataObject.getProperties() != null && dataObject.getProperties().getItems() != null) {
             dataObject.getProperties().removeInvalidChangeItemRequests();
 
             /* remove children first */
             for (final RESTAssignedPropertyTagCollectionItemV1 restEntityItem : dataObject.getProperties().getItems()) {
                 final RESTAssignedPropertyTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsRemoveItem()) {
                     final PropertyTag dbEntity = entityManager.find(PropertyTag.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No PropertyTag entity was found with the primary key " + restEntity.getId());
 
                     entity.removePropertyTag(dbEntity, restEntity.getValue());
                 } else if (restEntityItem.returnIsAddItem()) {
                     final PropertyTag dbEntity = entityManager.find(PropertyTag.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No PropertyTag entity was found with the primary key " + restEntity.getId());
 
                     entity.addPropertyTag(dbEntity, restEntity.getValue());
                 } else if (restEntityItem.returnIsUpdateItem()) {
                     final ContentSpecToPropertyTag dbEntity = entityManager.find(ContentSpecToPropertyTag.class,
                             restEntity.getRelationshipId());
                     if (dbEntity == null) throw new BadRequestException(
                             "No ContentSpecToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId());
                     if (!entity.getContentSpecToPropertyTags().contains(dbEntity)) throw new BadRequestException(
                             "No ContentSpecToPropertyTag entity was found with the primary key " + restEntity.getId() + " for " +
                                     "ContentSpec " + entity.getId());
 
                     new ContentSpecPropertyTagV1Factory().syncDBEntityWithRESTEntity(entityManager, dbEntity, restEntity);
                 }
             }
         }
 
         if (dataObject.hasParameterSet(
                 RESTContentSpecV1.TAGS_NAME) && dataObject.getTags() != null && dataObject.getTags().getItems() != null) {
             dataObject.getTags().removeInvalidChangeItemRequests();
 
             // Remove Tags first to ensure mutual exclusion is done correctly
             for (final RESTTagCollectionItemV1 restEntityItem : dataObject.getTags().getItems()) {
                 final RESTTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsRemoveItem()) {
                     final Tag dbEntity = entityManager.find(Tag.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No Tag entity was found with the primary key " + restEntity.getId());
 
                     entity.removeTag(dbEntity);
                 }
             }
 
             for (final RESTTagCollectionItemV1 restEntityItem : dataObject.getTags().getItems()) {
                 final RESTTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsAddItem()) {
                     final Tag dbEntity = entityManager.find(Tag.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No Tag entity was found with the primary key " + restEntity.getId());
 
                     entity.addTag(dbEntity);
                 }
             }
         }
 
         // One To Many - Add will create a new mapping
         if (dataObject.hasParameterSet(
                 RESTContentSpecV1.CHILDREN_NAME) && dataObject.getChildren_OTM() != null && dataObject.getChildren_OTM().getItems() !=
                 null) {
             dataObject.getChildren_OTM().removeInvalidChangeItemRequests();
 
             for (final RESTCSNodeCollectionItemV1 restEntityItem : dataObject.getChildren_OTM().getItems()) {
                 final RESTCSNodeV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsRemoveItem()) {
                     final CSNode dbEntity = entityManager.find(CSNode.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No CSNode entity was found with the primary key " + restEntity.getId());
 
                     entity.removeChild(dbEntity);
                     entityManager.remove(dbEntity);
                 } else if (restEntityItem.returnIsAddItem()) {
                     final CSNode dbEntity = new CSNodeV1Factory().createDBEntityFromRESTEntity(entityManager, restEntity);
                     dbEntity.setParent(null);
                     entityManager.persist(dbEntity);
                     entity.addChild(dbEntity);
                 } else if (restEntityItem.returnIsUpdateItem()) {
                     final CSNode dbEntity = entityManager.find(CSNode.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No CSNode entity was found with the primary key " + restEntity.getId());
                     if (!entity.getTopCSNodes().contains(dbEntity))
                         throw new BadRequestException("No CSNode entity was found with the primary key " + restEntity.getId() + " for " +
                                 "ContentSpec " + entity.getId());
 
                     new CSNodeV1Factory().syncDBEntityWithRESTEntity(entityManager, dbEntity, restEntity);
                 }
             }
         }
     }
 }
