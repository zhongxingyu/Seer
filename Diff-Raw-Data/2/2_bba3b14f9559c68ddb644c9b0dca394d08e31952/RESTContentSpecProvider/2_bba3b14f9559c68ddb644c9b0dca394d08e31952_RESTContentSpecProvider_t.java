 package org.jboss.pressgang.ccms.provider;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.jboss.pressgang.ccms.rest.RESTManager;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.base.RESTBaseCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTAssignedPropertyTagV1;
 import org.jboss.pressgang.ccms.wrapper.CSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.ContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.LogMessageWrapper;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagInContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.RESTContentSpecV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.RESTWrapperFactory;
 import org.jboss.pressgang.ccms.wrapper.TagWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 import org.jboss.resteasy.specimpl.PathSegmentImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RESTContentSpecProvider extends RESTDataProvider implements ContentSpecProvider {
     private static Logger log = LoggerFactory.getLogger(RESTContentSpecProvider.class);
     private int count = -1000;
 
     protected RESTContentSpecProvider(final RESTManager restManager, final RESTWrapperFactory wrapperFactory) {
         super(restManager, wrapperFactory);
     }
 
     protected RESTContentSpecV1 loadContentSpec(Integer id, Integer revision, String expandString) {
         if (revision == null) {
             return getRESTClient().getJSONContentSpec(id, expandString);
         } else {
             return getRESTClient().getJSONContentSpecRevision(id, revision, expandString);
         }
     }
 
     public RESTContentSpecV1 getRESTContentSpec(int id) {
         return getRESTContentSpec(id, null);
     }
 
     @Override
     public ContentSpecWrapper getContentSpec(int id) {
         return getContentSpec(id, null);
     }
 
     public RESTContentSpecV1 getRESTContentSpec(int id, Integer revision) {
         try {
             final RESTContentSpecV1 contentSpec;
             if (getRESTEntityCache().containsKeyValue(RESTContentSpecV1.class, id, revision)) {
                 contentSpec = getRESTEntityCache().get(RESTContentSpecV1.class, id, revision);
             } else {
                 contentSpec = loadContentSpec(id, revision, "");
                 getRESTEntityCache().add(contentSpec, revision);
             }
             return contentSpec;
         } catch (Exception e) {
             log.debug("Failed to retrieve Content Spec " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public ContentSpecWrapper getContentSpec(int id, Integer revision) {
         return getWrapperFactory().create(getRESTContentSpec(id, revision), revision != null);
     }
 
     public RESTContentSpecCollectionV1 getRESTContentSpecsWithQuery(final String query) {
         if (query == null || query.isEmpty()) return null;
 
         try {
             // We need to expand the all the content specs in the collection
             final String expandString = getExpansionString(RESTv1Constants.CONTENT_SPEC_EXPANSION_NAME);
 
             final RESTContentSpecCollectionV1 contentSpecs = getRESTClient().getJSONContentSpecsWithQuery(new PathSegmentImpl(query, false),
                     expandString);
             getRESTEntityCache().add(contentSpecs);
 
             return contentSpecs;
         } catch (Exception e) {
             log.debug("Failed to retrieve ContentSpecs with Query: " + query, e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<ContentSpecWrapper> getContentSpecsWithQuery(final String query) {
         if (query == null || query.isEmpty()) return null;
 
         return getWrapperFactory().createCollection(getRESTContentSpecsWithQuery(query), RESTContentSpecV1.class, false);
     }
 
     public RESTTagCollectionV1 getRESTContentSpecTags(int id, Integer revision) {
         try {
             RESTContentSpecV1 contentSpec = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTContentSpecV1.class, id, revision)) {
                 contentSpec = getRESTEntityCache().get(RESTContentSpecV1.class, id, revision);
 
                 if (contentSpec.getTags() != null) {
                     return contentSpec.getTags();
                 }
             }
 
             // We need to expand the tags in the content spec
             final String expandString = getExpansionString(RESTContentSpecV1.TAGS_NAME);
 
             // Load the content spec from the REST Interface
             final RESTContentSpecV1 tempContentSpec = loadContentSpec(id, revision, expandString);
 
             if (contentSpec == null) {
                 contentSpec = tempContentSpec;
                 getRESTEntityCache().add(contentSpec, revision);
             } else {
                 contentSpec.setTags(tempContentSpec.getTags());
             }
 
             return contentSpec.getTags();
         } catch (Exception e) {
             log.debug("Failed to retrieve the Tags for Content Spec " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TagWrapper> getContentSpecTags(int id, Integer revision) {
         return getWrapperFactory().createCollection(getRESTContentSpecTags(id, revision), RESTTagV1.class, revision != null);
     }
 
     public RESTTagCollectionV1 getRESTContentSpecBookTags(int id, Integer revision) {
         try {
             RESTContentSpecV1 contentSpec = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTContentSpecV1.class, id, revision)) {
                 contentSpec = getRESTEntityCache().get(RESTContentSpecV1.class, id, revision);
 
                 if (contentSpec.getBookTags() != null) {
                     return contentSpec.getBookTags();
                 }
             }
 
             // We need to expand the tags in the content spec
             final String expandString = getExpansionString(RESTContentSpecV1.BOOK_TAGS_NAME);
 
             // Load the content spec from the REST Interface
             final RESTContentSpecV1 tempContentSpec = loadContentSpec(id, revision, expandString);
 
             if (contentSpec == null) {
                 contentSpec = tempContentSpec;
                 getRESTEntityCache().add(contentSpec, revision);
             } else {
                 contentSpec.setBookTags(tempContentSpec.getBookTags());
             }
 
            return contentSpec.getBookTags();
         } catch (Exception e) {
             log.debug("Failed to retrieve the Book Tags for Content Spec " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     public RESTCSNodeCollectionV1 getRESTContentSpecNodes(int id, Integer revision) {
         try {
             RESTContentSpecV1 contentSpec = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTContentSpecV1.class, id, revision)) {
                 contentSpec = (RESTContentSpecV1) getRESTEntityCache().get(RESTContentSpecV1.class, id, revision);
 
                 if (contentSpec.getChildren_OTM() != null) {
                     return contentSpec.getChildren_OTM();
                 }
             }
 
             // We need to expand the tags in the content spec
             final String expandString = getExpansionString(RESTContentSpecV1.CHILDREN_NAME);
 
             // Load the content spec from the REST Interface
             final RESTContentSpecV1 tempContentSpec = loadContentSpec(id, revision, expandString);
 
             if (contentSpec == null) {
                 contentSpec = tempContentSpec;
                 getRESTEntityCache().add(contentSpec, revision);
             } else {
                 contentSpec.setChildren_OTM(tempContentSpec.getChildren_OTM());
             }
 
             return contentSpec.getChildren_OTM();
         } catch (Exception e) {
             log.debug("Failed to retrieve the Children Nodes for Content Spec " + id + (revision == null ? "" : (", " +
                     "Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public UpdateableCollectionWrapper<CSNodeWrapper> getContentSpecNodes(int id, Integer revision) {
         final CollectionWrapper<CSNodeWrapper> collection = getWrapperFactory().createCollection(getRESTContentSpecNodes(id, revision),
                 RESTCSNodeV1.class, revision != null);
         return (UpdateableCollectionWrapper<CSNodeWrapper>) collection;
     }
 
     public RESTTranslatedContentSpecCollectionV1 getRESTContentSpecTranslations(int id, Integer revision) {
         try {
             RESTContentSpecV1 contentSpec = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTContentSpecV1.class, id, revision)) {
                 contentSpec = (RESTContentSpecV1) getRESTEntityCache().get(RESTContentSpecV1.class, id, revision);
 
                 if (contentSpec.getTranslatedContentSpecs() != null) {
                     return contentSpec.getTranslatedContentSpecs();
                 }
             }
 
             // We need to expand the tags in the content spec
             final String expandString = getExpansionString(RESTContentSpecV1.TRANSLATED_CONTENT_SPECS_NAME);
 
             // Load the content spec from the REST Interface
             final RESTContentSpecV1 tempContentSpec = loadContentSpec(id, revision, expandString);
 
             if (contentSpec == null) {
                 contentSpec = tempContentSpec;
                 getRESTEntityCache().add(contentSpec, revision);
             } else {
                 contentSpec.setTranslatedContentSpecs(tempContentSpec.getTranslatedContentSpecs());
             }
 
             return contentSpec.getTranslatedContentSpecs();
         } catch (Exception e) {
             log.debug("Failed to retrieve the Translations for Content Spec " + id + (revision == null ? "" : (", " +
                     "Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedContentSpecWrapper> getContentSpecTranslations(int id, Integer revision) {
         return getWrapperFactory().createCollection(getRESTContentSpecTranslations(id, revision), RESTTranslatedContentSpecV1.class,
                 revision != null);
     }
 
     public RESTContentSpecCollectionV1 getRESTContentSpecRevisions(int id, Integer revision) {
         try {
             RESTContentSpecV1 contentSpec = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTContentSpecV1.class, id, revision)) {
                 contentSpec = getRESTEntityCache().get(RESTContentSpecV1.class, id, revision);
 
                 if (contentSpec.getRevisions() != null) {
                     return contentSpec.getRevisions();
                 }
             }
 
             // We need to expand the revisions in the content spec
             final String expandString = getExpansionString(RESTContentSpecV1.REVISIONS_NAME);
 
             // Load the content spec from the REST Interface
             final RESTContentSpecV1 tempContentSpec = loadContentSpec(id, revision, expandString);
 
             if (contentSpec == null) {
                 contentSpec = tempContentSpec;
                 getRESTEntityCache().add(contentSpec, revision);
             } else {
                 contentSpec.setRevisions(tempContentSpec.getRevisions());
             }
 
             return contentSpec.getRevisions();
         } catch (Exception e) {
             log.debug("Failed to retrieve the Revisions for Content Spec " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<ContentSpecWrapper> getContentSpecRevisions(int id, Integer revision) {
         return getWrapperFactory().createCollection(getRESTContentSpecRevisions(id, revision), RESTContentSpecV1.class, true);
     }
 
     public RESTAssignedPropertyTagCollectionV1 getRESTContentSpecProperties(int id, final Integer revision) {
         try {
             RESTContentSpecV1 topic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTContentSpecV1.class, id, revision)) {
                 topic = getRESTEntityCache().get(RESTContentSpecV1.class, id, revision);
 
                 if (topic.getProperties() != null) {
                     return topic.getProperties();
                 }
             }
 
             // We need to expand the all the properties in the content spec
             final String expandString = getExpansionString(RESTContentSpecV1.PROPERTIES_NAME);
 
             // Load the content spec from the REST Interface
             final RESTContentSpecV1 tempContentSpec = loadContentSpec(id, revision, expandString);
 
             if (topic == null) {
                 topic = tempContentSpec;
                 getRESTEntityCache().add(topic, revision);
             } else {
                 topic.setProperties(tempContentSpec.getProperties());
             }
 
             return topic.getProperties();
         } catch (Exception e) {
             log.debug("Failed to retrieve the Properties for Content Spec " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper> getContentSpecProperties(int id, final Integer revision) {
         final CollectionWrapper<PropertyTagInContentSpecWrapper> collection = getWrapperFactory().createCollection(
                 getRESTContentSpecProperties(id, revision), RESTAssignedPropertyTagV1.class, revision != null,
                 PropertyTagInContentSpecWrapper.class);
         return (UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper>) collection;
     }
 
     @Override
     public String getContentSpecAsString(int id) {
         return getContentSpecAsString(id, null);
     }
 
     @Override
     public String getContentSpecAsString(int id, Integer revision) {
         try {
             final String contentSpec;
             if (revision == null) {
                 contentSpec = getRESTClient().getTEXTContentSpec(id);
             } else {
                 contentSpec = getRESTClient().getTEXTContentSpecRevision(id, revision);
             }
             return contentSpec;
         } catch (Exception e) {
             log.debug("Failed to retrieve Content Spec String " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public ContentSpecWrapper createContentSpec(final ContentSpecWrapper contentSpecEntity) {
         return createContentSpec(contentSpecEntity, null);
     }
 
     @Override
     public ContentSpecWrapper createContentSpec(ContentSpecWrapper contentSpecEntity, LogMessageWrapper logMessage) {
         try {
             final RESTContentSpecV1 contentSpec = ((RESTContentSpecV1Wrapper) contentSpecEntity).unwrap();
 
             // Clean the entity to remove anything that doesn't need to be sent to the server
             cleanEntityForSave(contentSpec);
 
             final RESTContentSpecV1 createdContentSpec;
             if (logMessage != null) {
                 createdContentSpec = getRESTClient().createJSONContentSpec("", contentSpec, logMessage.getMessage(), logMessage.getFlags(),
                         logMessage.getUser());
             } else {
                 createdContentSpec = getRESTClient().createJSONContentSpec("", contentSpec);
             }
             if (createdContentSpec != null) {
                 getRESTEntityCache().add(createdContentSpec);
                 return getWrapperFactory().create(createdContentSpec, false);
             } else {
                 return null;
             }
         } catch (Exception e) {
             throw handleException(e);
         }
     }
 
     @Override
     public ContentSpecWrapper updateContentSpec(ContentSpecWrapper contentSpecEntity) {
         return updateContentSpec(contentSpecEntity, null);
     }
 
     @Override
     public ContentSpecWrapper updateContentSpec(ContentSpecWrapper contentSpecEntity, LogMessageWrapper logMessage) {
         try {
             final RESTContentSpecV1 contentSpec = ((RESTContentSpecV1Wrapper) contentSpecEntity).unwrap();
 
             // Clean the entity to remove anything that doesn't need to be sent to the server
             cleanEntityForSave(contentSpec);
 
             final RESTContentSpecV1 updatedContentSpec;
             if (logMessage != null) {
                 updatedContentSpec = getRESTClient().updateJSONContentSpec("", contentSpec, logMessage.getMessage(), logMessage.getFlags(),
                         logMessage.getUser());
             } else {
                 updatedContentSpec = getRESTClient().updateJSONContentSpec("", contentSpec);
             }
             if (updatedContentSpec != null) {
                 getRESTEntityCache().expire(RESTContentSpecV1.class, contentSpecEntity.getId());
                 getRESTEntityCache().add(updatedContentSpec);
                 return getWrapperFactory().create(updatedContentSpec, false);
             } else {
                 return null;
             }
         } catch (Exception e) {
             throw handleException(e);
         }
     }
 
     @Override
     public boolean deleteContentSpec(Integer id) {
         return deleteContentSpec(id, null);
     }
 
     @Override
     public boolean deleteContentSpec(Integer id, LogMessageWrapper logMessage) {
         try {
             if (logMessage != null) {
                 return getRESTClient().deleteJSONContentSpec(id, logMessage.getMessage(), logMessage.getFlags(), logMessage.getUser(),
                         "") != null;
             } else {
                 return getRESTClient().deleteJSONContentSpec(id, "") != null;
             }
         } catch (Exception e) {
             throw handleException(e);
         }
     }
 
     @Override
     public ContentSpecWrapper newContentSpec() {
         return getWrapperFactory().create(new RESTContentSpecV1(), false);
     }
 
     @Override
     public CollectionWrapper<ContentSpecWrapper> newContentSpecCollection() {
         return getWrapperFactory().createCollection(new RESTContentSpecCollectionV1(), RESTContentSpecV1.class, false);
     }
 
     @Override
     public void cleanEntityForSave(final RESTBaseEntityV1<?, ?, ?> entity) throws InvocationTargetException, IllegalAccessException {
         if (entity instanceof RESTBaseCSNodeV1) {
             final RESTCSNodeV1 node = (RESTCSNodeV1) entity;
 
             // Give new nodes a negative id so that serialization can track the linked list
             if (node.getId() == null) {
                 node.setId(count);
                 count--;
             }
 
             // Remove a CSNode parent element to eliminate recursive serialization issues, since it can't be explicitly set.
             if (node.getParent() != null) {
                 node.setParent(null);
             }
 
             // If the entity is a CSNode then replace entities, that could cause recursive serialization issues, with a dummy entity.
             if (node.getContentSpec() != null && node.getContentSpec().getId() != null) {
                 final RESTContentSpecV1 dummyContentSpec = new RESTContentSpecV1();
                 dummyContentSpec.setId(node.getContentSpec().getId());
 
                 node.setContentSpec(dummyContentSpec);
             }
         }
 
         super.cleanEntityForSave(entity);
     }
 }
