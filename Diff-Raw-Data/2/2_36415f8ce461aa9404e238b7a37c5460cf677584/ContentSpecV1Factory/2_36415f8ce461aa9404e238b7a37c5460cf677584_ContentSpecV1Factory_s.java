 package org.jboss.pressgang.ccms.server.rest.v1;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.model.PropertyTag;
 import org.jboss.pressgang.ccms.model.Tag;
 import org.jboss.pressgang.ccms.model.contentspec.CSNode;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpecToPropertyTag;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.RESTCSNodeCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.RESTContentSpecCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTagCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTAssignedPropertyTagCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.enums.RESTContentSpecTypeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTAssignedPropertyTagV1;
 import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
 import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectCollectionFactory;
 import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectFactory;
 import org.jboss.pressgang.ccms.server.rest.v1.utils.RESTv1Utilities;
 import org.jboss.pressgang.ccms.server.utils.ContentSpecUtilities;
 import org.jboss.pressgang.ccms.server.utils.EnversUtilities;
 import org.jboss.resteasy.spi.BadRequestException;
 
 @ApplicationScoped
 public class ContentSpecV1Factory extends RESTDataObjectFactory<RESTContentSpecV1, ContentSpec, RESTContentSpecCollectionV1,
         RESTContentSpecCollectionItemV1> {
     @Inject
     protected CSNodeV1Factory csNodeFactory;
     @Inject
     protected ContentSpecPropertyTagV1Factory contentSpecPropertyTagFactory;
     @Inject
     protected TranslatedContentSpecV1Factory translatedContentSpecFactory;
     @Inject
     protected TagV1Factory tagFactory;
 
     @Override
     public RESTContentSpecV1 createRESTEntityFromDBEntityInternal(final ContentSpec entity, final String baseUrl, final String dataType,
             final ExpandDataTrunk expand, final Number revision, final boolean expandParentReferences) {
         assert entity != null : "Parameter entity can not be null";
         assert baseUrl != null : "Parameter baseUrl can not be null";
 
         final RESTContentSpecV1 retValue = new RESTContentSpecV1();
 
         final List<String> expandOptions = new ArrayList<String>();
         expandOptions.add(RESTBaseEntityV1.LOG_DETAILS_NAME);
         expandOptions.add(RESTContentSpecV1.CHILDREN_NAME);
         expandOptions.add(RESTContentSpecV1.PROPERTIES_NAME);
         expandOptions.add(RESTContentSpecV1.BOOK_TAGS_NAME);
         expandOptions.add(RESTContentSpecV1.TAGS_NAME);
         if (revision == null) expandOptions.add(RESTBaseEntityV1.REVISIONS_NAME);
         retValue.setExpand(expandOptions);
 
         retValue.setId(entity.getId());
         retValue.setLocale(entity.getLocale());
         retValue.setCondition(entity.getCondition());
         retValue.setType(RESTContentSpecTypeV1.getContentSpecType(entity.getContentSpecType()));
         retValue.setLastPublished(entity.getLastPublished());
         retValue.setLastModified(entity.getLastModified());
         retValue.setErrors(entity.getErrors());
         retValue.setFailedContentSpec(ContentSpecUtilities.fixFailedContentSpec(entity));
 
         // REVISIONS
         if (revision == null && expand != null && expand.contains(RESTBaseEntityV1.REVISIONS_NAME)) {
             retValue.setRevisions(RESTDataObjectCollectionFactory.create(RESTContentSpecCollectionV1.class, this, entity,
                     EnversUtilities.getRevisions(entityManager, entity), RESTBaseEntityV1.REVISIONS_NAME, dataType, expand, baseUrl,
                     entityManager));
         }
 
         // CHILDREN NODES
         if (expand != null && expand.contains(RESTContentSpecV1.CHILDREN_NAME)) {
             retValue.setChildren_OTM(
                     RESTDataObjectCollectionFactory.create(RESTCSNodeCollectionV1.class, csNodeFactory, entity.getChildrenList(),
                             RESTContentSpecV1.CHILDREN_NAME, dataType, expand, baseUrl, expandParentReferences, entityManager));
         }
 
         // PROPERTY TAGS
         if (expand != null && expand.contains(RESTContentSpecV1.PROPERTIES_NAME)) {
             retValue.setProperties(
                     RESTDataObjectCollectionFactory.create(RESTAssignedPropertyTagCollectionV1.class, contentSpecPropertyTagFactory,
                             entity.getPropertyTagsList(), RESTContentSpecV1.PROPERTIES_NAME, dataType, expand, baseUrl,
                             revision, entityManager));
         }
 
         // BOOK TAGS
         if (expand != null && expand.contains(RESTContentSpecV1.BOOK_TAGS_NAME)) {
            retValue.setTags(RESTDataObjectCollectionFactory.create(RESTTagCollectionV1.class, tagFactory, entity.getBookTags(),
                     RESTContentSpecV1.BOOK_TAGS_NAME, dataType, expand, baseUrl, entityManager));
         }
 
         // TAGS
         if (expand != null && expand.contains(RESTContentSpecV1.TAGS_NAME)) {
             retValue.setTags(RESTDataObjectCollectionFactory.create(RESTTagCollectionV1.class, tagFactory, entity.getTags(),
                     RESTContentSpecV1.TAGS_NAME, dataType, expand, baseUrl, entityManager));
         }
 
         // TRANSLATIONS
         if (expand != null && expand.contains(RESTContentSpecV1.TRANSLATED_CONTENT_SPECS_NAME)) {
             retValue.setTranslatedContentSpecs(
                     RESTDataObjectCollectionFactory.create(RESTTranslatedContentSpecCollectionV1.class, translatedContentSpecFactory,
                             entity.getTranslatedContentSpecs(entityManager, revision), RESTContentSpecV1.TRANSLATED_CONTENT_SPECS_NAME,
                             dataType, expand, baseUrl, entityManager));
         }
 
         retValue.setLinks(baseUrl, RESTv1Constants.CONTENT_SPEC_URL_NAME, dataType, retValue.getId());
 
         return retValue;
     }
 
     @Override
     public void syncDBEntityWithRESTEntityFirstPass(final ContentSpec entity, final RESTContentSpecV1 dataObject) {
         if (dataObject.hasParameterSet(RESTContentSpecV1.LOCALE_NAME)) entity.setLocale(dataObject.getLocale());
 
         if (dataObject.hasParameterSet(RESTContentSpecV1.CONDITION_NAME)) entity.setCondition(dataObject.getCondition());
 
         if (dataObject.hasParameterSet(RESTContentSpecV1.LAST_PUBLISHED_NAME)) entity.setLastPublished(dataObject.getLastPublished());
 
         if (dataObject.hasParameterSet(RESTContentSpecV1.TYPE_NAME))
             entity.setContentSpecType(RESTContentSpecTypeV1.getContentSpecTypeId(dataObject.getType()));
 
         // Remove any error content
         entity.setErrors(null);
         entity.setFailedContentSpec(null);
 
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
                     final CSNode dbEntity = csNodeFactory.createDBEntityFromRESTEntity(restEntity);
                     dbEntity.setParent(null);
                     entity.addChild(dbEntity);
                 } else if (restEntityItem.returnIsUpdateItem()) {
                     final CSNode dbEntity = entityManager.find(CSNode.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No CSNode entity was found with the primary key " + restEntity.getId());
                     if (!entity.getChildrenList().contains(dbEntity))
                         throw new BadRequestException("No CSNode entity was found with the primary key " + restEntity.getId() + " for " +
                                 "ContentSpec " + entity.getId());
 
                     csNodeFactory.updateDBEntityFromRESTEntity(dbEntity, restEntity);
                 }
             }
         }
 
         // Many to Many
         if (dataObject.hasParameterSet(
                 RESTContentSpecV1.PROPERTIES_NAME) && dataObject.getProperties() != null && dataObject.getProperties().getItems() != null) {
             dataObject.getProperties().removeInvalidChangeItemRequests();
 
             for (final RESTAssignedPropertyTagCollectionItemV1 restEntityItem : dataObject.getProperties().getItems()) {
                 final RESTAssignedPropertyTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsRemoveItem()) {
                     final ContentSpecToPropertyTag dbEntity = entityManager.find(ContentSpecToPropertyTag.class,
                             restEntity.getRelationshipId());
                     if (dbEntity == null) throw new BadRequestException(
                             "No ContentSpecToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId());
 
                     entity.removePropertyTag(dbEntity);
                 } else if (restEntityItem.returnIsUpdateItem()) {
                     final ContentSpecToPropertyTag dbEntity = entityManager.find(ContentSpecToPropertyTag.class,
                             restEntity.getRelationshipId());
                     if (dbEntity == null) throw new BadRequestException(
                             "No ContentSpecToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId());
                     if (!entity.getContentSpecToPropertyTags().contains(dbEntity)) throw new BadRequestException(
                             "No ContentSpecToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId() + " for" +
                                     " ContentSpec " + entity.getId());
 
                     contentSpecPropertyTagFactory.updateDBEntityFromRESTEntity(dbEntity, restEntity);
                 }
             }
         }
     }
 
     @Override
     public void syncDBEntityWithRESTEntitySecondPass(ContentSpec entity, RESTContentSpecV1 dataObject) {
         // One To Many - Add will create a new mapping
         if (dataObject.hasParameterSet(
                 RESTContentSpecV1.CHILDREN_NAME) && dataObject.getChildren_OTM() != null && dataObject.getChildren_OTM().getItems() !=
                 null) {
             dataObject.getChildren_OTM().removeInvalidChangeItemRequests();
 
             for (final RESTCSNodeCollectionItemV1 restEntityItem : dataObject.getChildren_OTM().getItems()) {
                 final RESTCSNodeV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsAddItem() || restEntityItem.returnIsUpdateItem()) {
                     final CSNode dbEntity = RESTv1Utilities.findEntity(entityManager, entityCache, restEntity, CSNode.class);
                     if (dbEntity == null)
                         throw new BadRequestException("No CSNode entity was found with the primary key " + restEntity.getId());
 
                     csNodeFactory.syncDBEntityWithRESTEntitySecondPass(dbEntity, restEntity);
                 }
             }
         }
 
         // Many to Many
         if (dataObject.hasParameterSet(
                 RESTContentSpecV1.PROPERTIES_NAME) && dataObject.getProperties() != null && dataObject.getProperties().getItems() != null) {
             dataObject.getProperties().removeInvalidChangeItemRequests();
 
             for (final RESTAssignedPropertyTagCollectionItemV1 restEntityItem : dataObject.getProperties().getItems()) {
                 final RESTAssignedPropertyTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsAddItem()) {
                     final PropertyTag dbEntity = RESTv1Utilities.findEntity(entityManager, entityCache, restEntity, PropertyTag.class);
                     if (dbEntity == null)
                         throw new BadRequestException("No PropertyTag entity was found with the primary key " + restEntity.getId());
 
                     entity.addPropertyTag(dbEntity, restEntity.getValue());
                 } else if (restEntityItem.returnIsUpdateItem()) {
                     final ContentSpecToPropertyTag dbEntity = entityManager.find(ContentSpecToPropertyTag.class,
                             restEntity.getRelationshipId());
                     if (dbEntity == null) throw new BadRequestException(
                             "No ContentSpecToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId());
                     if (!entity.getContentSpecToPropertyTags().contains(dbEntity)) throw new BadRequestException(
                             "No ContentSpecToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId() + " for" +
                                     " ContentSpec " + entity.getId());
 
                     contentSpecPropertyTagFactory.syncDBEntityWithRESTEntitySecondPass(dbEntity, restEntity);
                 }
             }
         }
 
         // Many to Many
         if (dataObject.hasParameterSet(
                 RESTContentSpecV1.BOOK_TAGS_NAME) && dataObject.getBookTags() != null && dataObject.getBookTags().getItems() != null) {
             dataObject.getBookTags().removeInvalidChangeItemRequests();
 
             // Remove Tags first to ensure mutual exclusion is done correctly
             for (final RESTTagCollectionItemV1 restEntityItem : dataObject.getBookTags().getItems()) {
                 final RESTTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsRemoveItem()) {
                     final Tag dbEntity = entityManager.find(Tag.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No Tag entity was found with the primary key " + restEntity.getId());
 
                     entity.removeBookTag(dbEntity);
                 }
             }
 
             for (final RESTTagCollectionItemV1 restEntityItem : dataObject.getBookTags().getItems()) {
                 final RESTTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsAddItem()) {
                     final Tag dbEntity = RESTv1Utilities.findEntity(entityManager, entityCache, restEntity, Tag.class);
                     if (dbEntity == null)
                         throw new BadRequestException("No Tag entity was found with the primary key " + restEntity.getId());
 
                     entity.addBookTag(dbEntity);
                 }
             }
         }
 
         // Many to Many
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
                     final Tag dbEntity = RESTv1Utilities.findEntity(entityManager, entityCache, restEntity, Tag.class);
                     if (dbEntity == null)
                         throw new BadRequestException("No Tag entity was found with the primary key " + restEntity.getId());
 
                     entity.addTag(dbEntity);
                 }
             }
         }
     }
 
     @Override
     protected Class<ContentSpec> getDatabaseClass() {
         return ContentSpec.class;
     }
 }
