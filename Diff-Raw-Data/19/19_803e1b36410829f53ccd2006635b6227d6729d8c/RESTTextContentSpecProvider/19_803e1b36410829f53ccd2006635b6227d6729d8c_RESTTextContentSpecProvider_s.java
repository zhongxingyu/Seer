 package org.jboss.pressgang.ccms.provider;
 
 import org.jboss.pressgang.ccms.rest.RESTManager;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTextContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTextCSProcessingOptionsV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTextContentSpecV1;
 import org.jboss.pressgang.ccms.wrapper.LogMessageWrapper;
 import org.jboss.pressgang.ccms.wrapper.RESTTextCSProcessingOptionsV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.RESTTextContentSpecV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.RESTWrapperFactory;
 import org.jboss.pressgang.ccms.wrapper.TextCSProcessingOptionsWrapper;
 import org.jboss.pressgang.ccms.wrapper.TextContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.resteasy.specimpl.PathSegmentImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RESTTextContentSpecProvider extends RESTDataProvider implements TextContentSpecProvider {
     private static Logger log = LoggerFactory.getLogger(RESTTextContentSpecProvider.class);
 
     protected RESTTextContentSpecProvider(final RESTManager restManager, final RESTWrapperFactory wrapperFactory) {
         super(restManager, wrapperFactory);
     }
 
     protected RESTTextContentSpecV1 loadContentSpec(Integer id, Integer revision, String expandString) {
         if (revision == null) {
             return getRESTClient().getJSONTextContentSpec(id, expandString);
         } else {
             return getRESTClient().getJSONTextContentSpecRevision(id, revision, expandString);
         }
     }
 
     public RESTTextContentSpecV1 getRESTTextContentSpec(int id) {
         return getRESTTextContentSpec(id, null);
     }
 
     @Override
     public TextContentSpecWrapper getTextContentSpec(int id) {
         return getTextContentSpec(id, null);
     }
 
     public RESTTextContentSpecV1 getRESTTextContentSpec(int id, Integer revision) {
         try {
             final RESTTextContentSpecV1 contentSpec;
             if (getRESTEntityCache().containsKeyValue(RESTTextContentSpecV1.class, id, revision)) {
                 contentSpec = getRESTEntityCache().get(RESTTextContentSpecV1.class, id, revision);
             } else {
                 final String expandString = getExpansionString(RESTTextContentSpecV1.TEXT_NAME);
                 contentSpec = loadContentSpec(id, revision, expandString);
                 getRESTEntityCache().add(contentSpec, revision);
             }
             return contentSpec;
         } catch (Exception e) {
             log.debug("Failed to retrieve Content Spec " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public TextContentSpecWrapper getTextContentSpec(int id, Integer revision) {
         return getWrapperFactory().create(getRESTTextContentSpec(id, revision), revision != null);
     }
 
     public RESTTextContentSpecCollectionV1 getRESTTextContentSpecsWithQuery(final String query) {
         if (query == null || query.isEmpty()) return null;
 
         try {
             // We need to expand the all the content specs in the collection
             final String expandString = getExpansionString(RESTv1Constants.CONTENT_SPEC_EXPANSION_NAME);
 
             final RESTTextContentSpecCollectionV1 contentSpecs = getRESTClient().getJSONTextContentSpecsWithQuery(
                     new PathSegmentImpl(query, false), expandString);
             getRESTEntityCache().add(contentSpecs);
 
             return contentSpecs;
         } catch (Exception e) {
             log.debug("Failed to retrieve ContentSpecs with Query: " + query, e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TextContentSpecWrapper> getTextContentSpecsWithQuery(final String query) {
         if (query == null || query.isEmpty()) return null;
 
         return getWrapperFactory().createCollection(getRESTTextContentSpecsWithQuery(query), RESTTextContentSpecV1.class, false);
     }
 
     public RESTTagCollectionV1 getRESTTextContentSpecTags(int id, Integer revision) {
         try {
             RESTTextContentSpecV1 contentSpec = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTextContentSpecV1.class, id, revision)) {
                 contentSpec = getRESTEntityCache().get(RESTTextContentSpecV1.class, id, revision);
 
                 if (contentSpec.getTags() != null) {
                     return contentSpec.getTags();
                 }
             }
 
             // We need to expand the tags in the content spec
             final String expandString = getExpansionString(RESTTextContentSpecV1.TAGS_NAME);
 
             // Load the content spec from the REST Interface
             final RESTTextContentSpecV1 tempContentSpec = loadContentSpec(id, revision, expandString);
 
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
 
     public RESTTextContentSpecCollectionV1 getRESTTextContentSpecRevisions(int id, Integer revision) {
         try {
             RESTTextContentSpecV1 contentSpec = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTextContentSpecV1.class, id, revision)) {
                 contentSpec = getRESTEntityCache().get(RESTTextContentSpecV1.class, id, revision);
 
                 if (contentSpec.getRevisions() != null) {
                     return contentSpec.getRevisions();
                 }
             }
 
             // We need to expand the revisions in the content spec
             final String expandString = getExpansionString(RESTTextContentSpecV1.REVISIONS_NAME);
 
             // Load the content spec from the REST Interface
             final RESTTextContentSpecV1 tempContentSpec = loadContentSpec(id, revision, expandString);
 
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
     public CollectionWrapper<TextContentSpecWrapper> getTextContentSpecRevisions(int id, Integer revision) {
         return getWrapperFactory().createCollection(getRESTTextContentSpecRevisions(id, revision), RESTTextContentSpecV1.class, true);
     }
 
     public RESTAssignedPropertyTagCollectionV1 getRESTContentSpecProperties(int id, final Integer revision) {
         try {
             RESTTextContentSpecV1 topic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTextContentSpecV1.class, id, revision)) {
                 topic = getRESTEntityCache().get(RESTTextContentSpecV1.class, id, revision);
 
                 if (topic.getProperties() != null) {
                     return topic.getProperties();
                 }
             }
 
             // We need to expand the all the properties in the content spec
             final String expandString = getExpansionString(RESTTextContentSpecV1.PROPERTIES_NAME);
 
             // Load the content spec from the REST Interface
             final RESTTextContentSpecV1 tempContentSpec = loadContentSpec(id, revision, expandString);
 
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
     public TextContentSpecWrapper createTextContentSpec(final TextContentSpecWrapper contentSpecEntity) {
         return createTextContentSpec(contentSpecEntity, null, null);
     }
 
     @Override
     public TextContentSpecWrapper createTextContentSpec(TextContentSpecWrapper contentSpecEntity,
             TextCSProcessingOptionsWrapper processingOptions) {
         return createTextContentSpec(contentSpecEntity, processingOptions, null);
     }
 
     @Override
     public TextContentSpecWrapper createTextContentSpec(TextContentSpecWrapper contentSpecEntity,
             TextCSProcessingOptionsWrapper processingOptions, LogMessageWrapper logMessage) {
         try {
             final RESTTextContentSpecV1 contentSpec = ((RESTTextContentSpecV1Wrapper) contentSpecEntity).unwrap();
             if (processingOptions != null) {
                 contentSpec.setProcessingOptions(((RESTTextCSProcessingOptionsV1Wrapper) processingOptions).unwrap());
             }
 
             // Clean the entity to remove anything that doesn't need to be sent to the server
             cleanEntityForSave(contentSpec);
 
             final RESTTextContentSpecV1 createdContentSpec;
             if (logMessage != null) {
                createdContentSpec = getRESTClient().createJSONTextContentSpec("", contentSpec, logMessage.getMessage(),
                         logMessage.getFlags(), logMessage.getUser());
             } else {
                createdContentSpec = getRESTClient().createJSONTextContentSpec("", contentSpec);
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
     public TextContentSpecWrapper updateTextContentSpec(TextContentSpecWrapper contentSpecEntity) {
         return updateTextContentSpec(contentSpecEntity, null, null);
     }
 
     @Override
     public TextContentSpecWrapper updateTextContentSpec(TextContentSpecWrapper contentSpecEntity,
             TextCSProcessingOptionsWrapper processingOptions) {
         return updateTextContentSpec(contentSpecEntity, processingOptions, null);
     }
 
     @Override
     public TextContentSpecWrapper updateTextContentSpec(TextContentSpecWrapper contentSpecEntity,
             TextCSProcessingOptionsWrapper processingOptions, LogMessageWrapper logMessage) {
         try {
             final RESTTextContentSpecV1 contentSpec = ((RESTTextContentSpecV1Wrapper) contentSpecEntity).unwrap();
             if (processingOptions != null) {
                 contentSpec.setProcessingOptions(((RESTTextCSProcessingOptionsV1Wrapper) processingOptions).unwrap());
             }
 
             // Clean the entity to remove anything that doesn't need to be sent to the server
             cleanEntityForSave(contentSpec);
 
             final RESTTextContentSpecV1 updatedContentSpec;
             if (logMessage != null) {
                updatedContentSpec = getRESTClient().updateJSONTextContentSpec("", contentSpec, logMessage.getMessage(),
                         logMessage.getFlags(), logMessage.getUser());
             } else {
                updatedContentSpec = getRESTClient().updateJSONTextContentSpec("", contentSpec);
             }
             if (updatedContentSpec != null) {
                 getRESTEntityCache().expire(RESTTextContentSpecV1.class, contentSpecEntity.getId());
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
     public TextContentSpecWrapper newTextContentSpec() {
         return getWrapperFactory().create(new RESTTextContentSpecV1(), false);
     }
 
     @Override
     public TextCSProcessingOptionsWrapper newTextProcessingOptions() {
         return new RESTTextCSProcessingOptionsV1Wrapper(new RESTTextCSProcessingOptionsV1());
     }
 }
