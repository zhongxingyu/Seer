 package org.jboss.pressgang.ccms.provider;
 
 import org.jboss.pressgang.ccms.rest.RESTManager;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedContentSpecCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedContentSpecV1;
 import org.jboss.pressgang.ccms.wrapper.RESTTranslatedContentSpecV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.RESTWrapperFactory;
 import org.jboss.pressgang.ccms.wrapper.TranslatedCSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTTranslatedContentSpecCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 import org.jboss.resteasy.specimpl.PathSegmentImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RESTTranslatedContentSpecProvider extends RESTDataProvider implements TranslatedContentSpecProvider {
     private static Logger log = LoggerFactory.getLogger(RESTTranslatedContentSpecProvider.class);
 
     protected RESTTranslatedContentSpecProvider(RESTManager restManager, RESTWrapperFactory wrapperFactory) {
         super(restManager, wrapperFactory);
     }
 
     public RESTTranslatedContentSpecV1 getRESTTranslatedContentSpec(int id) {
         return getRESTTranslatedContentSpec(id, null);
     }
 
     protected RESTTranslatedContentSpecV1 loadTranslatedContentSpec(int id, Integer revision, String expandString) {
         if (revision == null) {
             return getRESTClient().getJSONTranslatedContentSpec(id, expandString);
         } else {
             return getRESTClient().getJSONTranslatedContentSpecRevision(id, revision, expandString);
         }
     }
 
     @Override
     public TranslatedContentSpecWrapper getTranslatedContentSpec(int id) {
         return getTranslatedContentSpec(id, null);
     }
 
     public RESTTranslatedContentSpecV1 getRESTTranslatedContentSpec(int id, Integer revision) {
         try {
             final RESTTranslatedContentSpecV1 node;
             if (getRESTEntityCache().containsKeyValue(RESTTranslatedContentSpecV1.class, id, revision)) {
                 node = getRESTEntityCache().get(RESTTranslatedContentSpecV1.class, id, revision);
             } else {
                 final String expandString = super.getExpansionString(RESTTranslatedContentSpecV1.CONTENT_SPEC_NAME);
                 node = loadTranslatedContentSpec(id, revision, expandString);
                 getRESTEntityCache().add(node, revision);
             }
             return node;
         } catch (Exception e) {
             log.debug("Failed to retrieve Translated Content Spec " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public TranslatedContentSpecWrapper getTranslatedContentSpec(int id, Integer revision) {
         return getWrapperFactory().create(getRESTTranslatedContentSpec(id, revision), revision != null);
     }
 
     public RESTTranslatedCSNodeCollectionV1 getRESTTranslatedNodes(int id, Integer revision) {
         try {
             RESTTranslatedContentSpecV1 translatedContentSpec = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTranslatedContentSpecV1.class, id, revision)) {
                 translatedContentSpec = getRESTEntityCache().get(RESTTranslatedContentSpecV1.class, id, revision);
 
                 if (translatedContentSpec.getTranslatedNodes_OTM() != null) {
                     return translatedContentSpec.getTranslatedNodes_OTM();
                 }
             }
 
             // We need to expand the all the translated nodes in the translated content collection
             final String expandString = getExpansionString(RESTTranslatedContentSpecV1.TRANSLATED_NODES_NAME);
 
             // Load the translated content spec node from the REST Interface
             final RESTTranslatedContentSpecV1 tempTranslatedContentSpec = loadTranslatedContentSpec(id, revision, expandString);
 
             if (translatedContentSpec == null) {
                 translatedContentSpec = tempTranslatedContentSpec;
                 getRESTEntityCache().add(translatedContentSpec, revision);
             } else {
                 translatedContentSpec.setTranslatedNodes_OTM(tempTranslatedContentSpec.getTranslatedNodes_OTM());
             }
 
             return translatedContentSpec.getTranslatedNodes_OTM();
         } catch (Exception e) {
             log.error(
                     "Unable to retrieve the Translated ContentSpec Nodes for Translated ContentSpec " + id + (revision == null ? "" : ("," +
                             " Revision" + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public UpdateableCollectionWrapper<TranslatedCSNodeWrapper> getTranslatedNodes(int id, Integer revision) {
         final CollectionWrapper<TranslatedCSNodeWrapper> collection = getWrapperFactory().createCollection(
                 getRESTTranslatedNodes(id, revision), RESTTranslatedCSNodeV1.class, revision != null);
         return (UpdateableCollectionWrapper<TranslatedCSNodeWrapper>) collection;
     }
 
     public RESTTranslatedContentSpecCollectionV1 getRESTTranslatedContentSpecRevisions(int id, Integer revision) {
         try {
             RESTTranslatedContentSpecV1 translatedContentSpec = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTranslatedContentSpecV1.class, id, revision)) {
                 translatedContentSpec = getRESTEntityCache().get(RESTTranslatedContentSpecV1.class, id, revision);
 
                 if (translatedContentSpec.getRevisions() != null) {
                     return translatedContentSpec.getRevisions();
                 }
             }
 
             // We need to expand the revisions in the content spec translated node collection
             final String expandString = getExpansionString(RESTTranslatedContentSpecV1.REVISIONS_NAME);
 
             // Load the content spec translated node from the REST Interface
             final RESTTranslatedContentSpecV1 tempTranslatedContentSpec = loadTranslatedContentSpec(id, revision, expandString);
 
             if (translatedContentSpec == null) {
                 translatedContentSpec = tempTranslatedContentSpec;
                 getRESTEntityCache().add(translatedContentSpec, revision);
             } else {
                 translatedContentSpec.setRevisions(tempTranslatedContentSpec.getRevisions());
             }
 
             return translatedContentSpec.getRevisions();
         } catch (Exception e) {
             log.debug(
                     "Failed to retrieve the Revisions for Translated Content Spec " + id + (revision == null ? "" : (", " +
                             "Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedContentSpecWrapper> getTranslatedContentSpecRevisions(int id, Integer revision) {
         return getWrapperFactory().createCollection(getRESTTranslatedContentSpecRevisions(id, revision), RESTTranslatedContentSpecV1.class,
                 true);
     }
 
     public RESTTranslatedContentSpecCollectionV1 getRESTTranslatedContentSpecsWithQuery(String query) {
         if (query == null || query.isEmpty()) return null;
 
         try {
             // We need to expand the all the Translated Content Specs in the collection
             final String expandString = getExpansionString(RESTv1Constants.TRANSLATED_CONTENT_SPEC_EXPANSION_NAME);
 
             final RESTTranslatedContentSpecCollectionV1 contentSpecs = getRESTClient().getJSONTranslatedContentSpecsWithQuery(
                     new PathSegmentImpl(query, false), expandString);
             getRESTEntityCache().add(contentSpecs);
 
             return contentSpecs;
         } catch (Exception e) {
             log.debug("Failed to retrieve Translated Content Specs with a query", e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedContentSpecWrapper> getTranslatedContentSpecsWithQuery(String query) {
         if (query == null || query.isEmpty()) return null;
 
        return getWrapperFactory().createCollection(getTranslatedContentSpecsWithQuery(query), RESTTranslatedContentSpecV1.class, false);
     }
 
     @Override
     public TranslatedContentSpecWrapper createTranslatedContentSpec(
             TranslatedContentSpecWrapper translatedContentSpecEntity) {
         try {
             final RESTTranslatedContentSpecV1 translatedContentSpec = ((RESTTranslatedContentSpecV1Wrapper) translatedContentSpecEntity)
                     .unwrap();
 
             // Clean the entity to remove anything that doesn't need to be sent to the server
             cleanEntityForSave(translatedContentSpec);
 
             final RESTTranslatedContentSpecV1 updatedTranslatedContentSpec = getRESTClient().createJSONTranslatedContentSpec("",
                     translatedContentSpec);
             if (updatedTranslatedContentSpec != null) {
                 getRESTEntityCache().add(updatedTranslatedContentSpec);
                 return getWrapperFactory().create(updatedTranslatedContentSpec, false);
             } else {
                 return null;
             }
         } catch (Exception e) {
             throw handleException(e);
         }
     }
 
     @Override
     public TranslatedContentSpecWrapper updateTranslatedContentSpec(
             TranslatedContentSpecWrapper translatedContentSpecEntity) {
         try {
             final RESTTranslatedContentSpecV1 translatedContentSpec = ((RESTTranslatedContentSpecV1Wrapper) translatedContentSpecEntity)
                     .unwrap();
 
             // Clean the entity to remove anything that doesn't need to be sent to the server
             cleanEntityForSave(translatedContentSpec);
 
             final RESTTranslatedContentSpecV1 updatedTranslatedContentSpec = getRESTClient().updateJSONTranslatedContentSpec("",
                     translatedContentSpec);
             if (updatedTranslatedContentSpec != null) {
                 getRESTEntityCache().expire(RESTTranslatedContentSpecV1.class, updatedTranslatedContentSpec.getId());
                 getRESTEntityCache().add(updatedTranslatedContentSpec);
                 return getWrapperFactory().create(updatedTranslatedContentSpec, false);
             } else {
                 return null;
             }
         } catch (Exception e) {
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedContentSpecWrapper> createTranslatedContentSpecs(
             CollectionWrapper<TranslatedContentSpecWrapper> translatedNodes) {
         try {
             final RESTTranslatedContentSpecCollectionV1 unwrappedNodes = ((RESTTranslatedContentSpecCollectionV1Wrapper) translatedNodes)
                     .unwrap();
 
             final String expandString = getExpansionString(RESTv1Constants.TRANSLATED_CONTENT_SPEC_EXPANSION_NAME);
             final RESTTranslatedContentSpecCollectionV1 createdNodes = getRESTClient().createJSONTranslatedContentSpecs(expandString,
                     unwrappedNodes);
             if (createdNodes != null) {
                 getRESTEntityCache().add(createdNodes, false);
                 return getWrapperFactory().createCollection(createdNodes, RESTTranslatedContentSpecV1.class, false);
             } else {
                 return null;
             }
         } catch (Exception e) {
             throw handleException(e);
         }
     }
 
     @Override
     public TranslatedContentSpecWrapper newTranslatedContentSpec() {
         return getWrapperFactory().create(new RESTTranslatedContentSpecV1(), false, TranslatedContentSpecWrapper.class, true);
     }
 
     @Override
     public UpdateableCollectionWrapper<TranslatedContentSpecWrapper> newTranslatedContentSpecCollection() {
         final CollectionWrapper<TranslatedContentSpecWrapper> collection = getWrapperFactory().createCollection(
                 new RESTTranslatedContentSpecCollectionV1(), RESTTranslatedContentSpecV1.class, false);
         return (UpdateableCollectionWrapper<TranslatedContentSpecWrapper>) collection;
     }
 }
