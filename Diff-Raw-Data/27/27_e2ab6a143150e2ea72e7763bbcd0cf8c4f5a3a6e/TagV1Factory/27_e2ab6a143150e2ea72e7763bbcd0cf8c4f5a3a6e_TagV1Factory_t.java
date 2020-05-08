 package org.jboss.pressgang.ccms.server.rest.v1;
 
 import javax.persistence.EntityManager;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.model.Category;
 import org.jboss.pressgang.ccms.model.Project;
 import org.jboss.pressgang.ccms.model.PropertyTag;
 import org.jboss.pressgang.ccms.model.Tag;
 import org.jboss.pressgang.ccms.model.TagToCategory;
 import org.jboss.pressgang.ccms.model.TagToPropertyTag;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTProjectCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTProjectCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTagCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTAssignedPropertyTagCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTCategoryInTagCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTCategoryInTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTProjectV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTAssignedPropertyTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTCategoryInTagV1;
 import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
 import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectCollectionFactory;
 import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectFactory;
 import org.jboss.pressgang.ccms.server.utils.EnversUtilities;
 import org.jboss.resteasy.spi.BadRequestException;
 
 public class TagV1Factory extends RESTDataObjectFactory<RESTTagV1, Tag, RESTTagCollectionV1, RESTTagCollectionItemV1> {
     TagV1Factory() {
         super(Tag.class);
     }
 
     @Override
     public RESTTagV1 createRESTEntityFromDBEntityInternal(final Tag entity, final String baseUrl, final String dataType,
             final ExpandDataTrunk expand, final Number revision, final boolean expandParentReferences, final EntityManager entityManager) {
         assert entity != null : "Parameter entity can not be null";
         assert baseUrl != null : "Parameter baseUrl can not be null";
 
         final RESTTagV1 retValue = new RESTTagV1();
 
         final List<String> expandOptions = new ArrayList<String>();
         expandOptions.add(RESTTagV1.CATEGORIES_NAME);
         expandOptions.add(RESTTagV1.PARENT_TAGS_NAME);
         expandOptions.add(RESTTagV1.CHILD_TAGS_NAME);
         expandOptions.add(RESTTagV1.PROJECTS_NAME);
         expandOptions.add(RESTTagV1.PROPERTIES_NAME);
         expandOptions.add(RESTBaseEntityV1.LOG_DETAILS_NAME);
 
         if (revision == null) expandOptions.add(RESTBaseEntityV1.REVISIONS_NAME);
 
         retValue.setExpand(expandOptions);
 
         /* Set simple properties */
         retValue.setId(entity.getTagId());
         retValue.setName(entity.getTagName());
         retValue.setDescription(entity.getTagDescription());
 
         // REVISIONS
         if (revision == null && expand != null && expand.contains(RESTTopicV1.REVISIONS_NAME)) {
             retValue.setRevisions(
                     new RESTDataObjectCollectionFactory<RESTTagV1, Tag, RESTTagCollectionV1, RESTTagCollectionItemV1>().create(
                             RESTTagCollectionV1.class, new TagV1Factory(), entity, EnversUtilities.getRevisions(entityManager, entity),
                             RESTBaseEntityV1.REVISIONS_NAME, dataType, expand, baseUrl, entityManager));
         }
 
         // CATEGORIES
         if (expand != null && expand.contains(RESTTagV1.CATEGORIES_NAME)) {
             retValue.setCategories(
                     new RESTDataObjectCollectionFactory<RESTCategoryInTagV1, TagToCategory, RESTCategoryInTagCollectionV1,
                             RESTCategoryInTagCollectionItemV1>().create(
                             RESTCategoryInTagCollectionV1.class, new CategoryInTagV1Factory(), entity.getTagToCategoriesArray(),
                             RESTTagV1.CATEGORIES_NAME, dataType, expand, baseUrl, entityManager));
         }
 
         // PARENT TAGS
         if (expand != null && expand.contains(RESTTagV1.PARENT_TAGS_NAME)) {
             retValue.setParentTags(
                     new RESTDataObjectCollectionFactory<RESTTagV1, Tag, RESTTagCollectionV1, RESTTagCollectionItemV1>().create(
                             RESTTagCollectionV1.class, new TagV1Factory(), entity.getParentTags(), RESTTagV1.PARENT_TAGS_NAME, dataType,
                             expand, baseUrl, entityManager));
         }
 
         // CHILD TAGS
         if (expand != null && expand.contains(RESTTagV1.CHILD_TAGS_NAME)) {
             retValue.setChildTags(
                     new RESTDataObjectCollectionFactory<RESTTagV1, Tag, RESTTagCollectionV1, RESTTagCollectionItemV1>().create(
                             RESTTagCollectionV1.class, new TagV1Factory(), entity.getChildTags(), RESTTagV1.CHILD_TAGS_NAME, dataType,
                             expand, baseUrl, entityManager));
         }
 
         // PROPERTY TAGS
         if (expand != null && expand.contains(RESTTagV1.PROPERTIES_NAME)) {
             retValue.setProperties(
                     new RESTDataObjectCollectionFactory<RESTAssignedPropertyTagV1, TagToPropertyTag, RESTAssignedPropertyTagCollectionV1,
                             RESTAssignedPropertyTagCollectionItemV1>().create(
                             RESTAssignedPropertyTagCollectionV1.class, new TagPropertyTagV1Factory(), entity.getTagToPropertyTagsArray(),
                             RESTTagV1.PROPERTIES_NAME, dataType, expand, baseUrl, entityManager));
         }
 
         // PROJECTS
         if (expand != null && expand.contains(RESTTagV1.PROJECTS_NAME)) {
             retValue.setProjects(
                     new RESTDataObjectCollectionFactory<RESTProjectV1, Project, RESTProjectCollectionV1,
                             RESTProjectCollectionItemV1>().create(
                             RESTProjectCollectionV1.class, new ProjectV1Factory(), entity.getProjects(), RESTTagV1.PROJECTS_NAME, dataType,
                             expand, baseUrl, entityManager));
         }
 
         retValue.setLinks(baseUrl, RESTv1Constants.TAG_URL_NAME, dataType, retValue.getId());
 
         return retValue;
     }
 
     @Override
     public void syncDBEntityWithRESTEntity(final EntityManager entityManager, final Tag entity, final RESTTagV1 dataObject) {
         if (dataObject.hasParameterSet(RESTTagV1.DESCRIPTION_NAME)) entity.setTagDescription(dataObject.getDescription());
         if (dataObject.hasParameterSet(RESTTagV1.NAME_NAME)) entity.setTagName(dataObject.getName());
 
         entityManager.persist(entity);
 
         if (dataObject.hasParameterSet(
                 RESTTagV1.CATEGORIES_NAME) && dataObject.getCategories() != null && dataObject.getCategories().getItems() != null) {
             dataObject.getCategories().removeInvalidChangeItemRequests();
 
             for (final RESTCategoryInTagCollectionItemV1 restEntityItem : dataObject.getCategories().getItems()) {
                 final RESTCategoryInTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsAddItem() || restEntityItem.returnIsRemoveItem()) {
                     final Category dbEntity = entityManager.find(Category.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No Category entity was found with the primary key " + restEntity.getId());
 
                     if (restEntityItem.returnIsAddItem()) {
                         if (restEntity.hasParameterSet(RESTCategoryInTagV1.RELATIONSHIP_SORT_NAME)) {
                             dbEntity.addTagRelationship(entity, restEntity.getRelationshipSort());
                         } else {
                             dbEntity.addTagRelationship(entity);
                         }
                     } else if (restEntityItem.returnIsRemoveItem()) {
                         dbEntity.removeTagRelationship(entity);
                     }
                 } else if (restEntityItem.returnIsUpdateItem()) {
                     final TagToCategory dbEntity = entityManager.find(TagToCategory.class, restEntity.getRelationshipId());
                     if (dbEntity == null) throw new BadRequestException(
                             "No TagToCategory entity was found with the primary key " + restEntity.getRelationshipId());
                     if (!entity.getTagToCategories().contains(dbEntity)) throw new BadRequestException(
                             "No TagToCategory entity was found with the primary key " + restEntity.getRelationshipId() + " for Tag " +
                                     entity.getId());
 
                     new CategoryInTagV1Factory().syncDBEntityWithRESTEntity(entityManager, dbEntity, restEntity);
                 }
             }
         }
 
         if (dataObject.hasParameterSet(
                 RESTTagV1.PROJECTS_NAME) && dataObject.getProjects() != null && dataObject.getProjects().getItems() != null) {
             dataObject.getProjects().removeInvalidChangeItemRequests();
 
             for (final RESTProjectCollectionItemV1 restEntityItem : dataObject.getProjects().getItems()) {
                 final RESTProjectV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsAddItem() || restEntityItem.returnIsRemoveItem()) {
                     final Project dbEntity = entityManager.find(Project.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No Project entity was found with the primary key " + restEntity.getId());
 
                     if (restEntityItem.returnIsAddItem()) {
                         dbEntity.addRelationshipTo(entity);
                     } else if (restEntityItem.returnIsRemoveItem()) {
                         dbEntity.removeRelationshipTo(entity.getId());
                     }
                 }
             }
         }
 
         if (dataObject.hasParameterSet(
                 RESTTagV1.CHILD_TAGS_NAME) && dataObject.getChildTags() != null && dataObject.getChildTags().getItems() != null) {
             dataObject.getChildTags().removeInvalidChangeItemRequests();
 
             for (final RESTTagCollectionItemV1 restEntityItem : dataObject.getChildTags().getItems()) {
                 final RESTTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsAddItem() || restEntityItem.returnIsRemoveItem()) {
                     final Tag dbEntity = entityManager.find(Tag.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No Tag entity was found with the primary key " + restEntity.getId());
 
                     if (restEntityItem.returnIsAddItem()) {
                         entity.addTagRelationship(dbEntity);
                     } else if (restEntityItem.returnIsRemoveItem()) {
                         entity.removeTagRelationship(dbEntity);
                     }
                 }
             }
         }
 
         if (dataObject.hasParameterSet(
                 RESTTagV1.PARENT_TAGS_NAME) && dataObject.getParentTags() != null && dataObject.getParentTags().getItems() != null) {
             dataObject.getParentTags().removeInvalidChangeItemRequests();
 
             for (final RESTTagCollectionItemV1 restEntityItem : dataObject.getParentTags().getItems()) {
                 final RESTTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsAddItem() || restEntityItem.returnIsRemoveItem()) {
                     final Tag dbEntity = entityManager.find(Tag.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No Tag entity was found with the primary key " + restEntity.getId());
 
                     if (restEntityItem.returnIsAddItem()) {
                         dbEntity.addTagRelationship(entity);
                     } else if (restEntityItem.returnIsRemoveItem()) {
                         dbEntity.removeTagRelationship(entity);
                     }
                 }
             }
         }
 
         if (dataObject.hasParameterSet(
                 RESTTagV1.PROPERTIES_NAME) && dataObject.getProperties() != null && dataObject.getProperties().getItems() != null) {
             dataObject.getProperties().removeInvalidChangeItemRequests();
 
             for (final RESTAssignedPropertyTagCollectionItemV1 restEntityItem : dataObject.getProperties().getItems()) {
                 final RESTAssignedPropertyTagV1 restEntity = restEntityItem.getItem();
 
                 if (restEntityItem.returnIsAddItem() || restEntityItem.returnIsRemoveItem()) {
                    final TagToPropertyTag dbEntity = entityManager.find(TagToPropertyTag.class, restEntity.getRelationshipId());
                    if (dbEntity == null) throw new BadRequestException(
                            "No TagToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId());

                    entity.removePropertyTag(dbEntity);
                } else if (restEntityItem.returnIsAddItem()) {
                     final PropertyTag dbEntity = entityManager.find(PropertyTag.class, restEntity.getId());
                     if (dbEntity == null)
                         throw new BadRequestException("No PropertyTag entity was found with the primary key " + restEntity.getId());
 
                    entity.addPropertyTag(dbEntity, restEntity.getValue());
                 } else if (restEntityItem.returnIsUpdateItem()) {
                     final TagToPropertyTag dbEntity = entityManager.find(TagToPropertyTag.class, restEntity.getRelationshipId());
                     if (dbEntity == null) throw new BadRequestException(
                             "No TagToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId());
                     if (!entity.getTagToPropertyTags().contains(dbEntity)) throw new BadRequestException(
                             "No TagToPropertyTag entity was found with the primary key " + restEntity.getRelationshipId() + " for Tag " +
                                     entity.getId());
 
                     new TagPropertyTagV1Factory().syncDBEntityWithRESTEntity(entityManager, dbEntity, restEntity);
                 }
             }
         }
     }
 
 }
