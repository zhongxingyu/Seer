 package org.jboss.pressgang.ccms.provider;
 
 import javax.ws.rs.core.PathSegment;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.rest.RESTManager;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicSourceUrlCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTranslatedTopicCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTranslatedTopicStringCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicSourceUrlV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTranslatedTopicStringV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTranslatedTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTAssignedPropertyTagV1;
 import org.jboss.pressgang.ccms.utils.common.CollectionUtilities;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagInTopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.RESTTranslatedTopicV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.RESTWrapperFactory;
 import org.jboss.pressgang.ccms.wrapper.TagWrapper;
 import org.jboss.pressgang.ccms.wrapper.TopicSourceURLWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedTopicStringWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedTopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTTranslatedTopicCollectionV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 import org.jboss.resteasy.specimpl.PathSegmentImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RESTTranslatedTopicProvider extends RESTDataProvider implements TranslatedTopicProvider {
     private static Logger log = LoggerFactory.getLogger(RESTTranslatedTopicProvider.class);
 
     protected RESTTranslatedTopicProvider(final RESTManager restManager, final RESTWrapperFactory wrapperFactory) {
         super(restManager, wrapperFactory);
     }
 
     protected RESTTranslatedTopicV1 loadTranslatedTopic(int id, Integer revision, String expandString) {
         if (revision == null) {
             return getRESTClient().getJSONTranslatedTopic(id, expandString);
         } else {
             return getRESTClient().getJSONTranslatedTopicRevision(id, revision, expandString);
         }
     }
 
     public RESTTranslatedTopicV1 getRESTTranslatedTopic(int id) {
         return getRESTTranslatedTopic(id, null);
     }
 
     @Override
     public TranslatedTopicWrapper getTranslatedTopic(int id) {
         return getTranslatedTopic(id, null);
     }
 
     public RESTTranslatedTopicV1 getRESTTranslatedTopic(int id, final Integer revision) {
         try {
             final RESTTranslatedTopicV1 translatedTopic;
             if (getRESTEntityCache().containsKeyValue(RESTTranslatedTopicV1.class, id, revision)) {
                 translatedTopic = getRESTEntityCache().get(RESTTranslatedTopicV1.class, id, revision);
             } else {
                 final String expandString = super.getExpansionString(RESTTranslatedTopicV1.TOPIC_NAME);
                 translatedTopic = loadTranslatedTopic(id, revision, expandString);
                 getRESTEntityCache().add(translatedTopic, revision);
             }
             return translatedTopic;
         } catch (Exception e) {
             log.debug("Failed to retrieve Translated Topic " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public TranslatedTopicWrapper getTranslatedTopic(int id, final Integer revision) {
         return getWrapperFactory().create(getRESTTranslatedTopic(id, revision), revision != null);
     }
 
     public RESTTagCollectionV1 getRESTTranslatedTopicTags(int id, Integer revision) {
         try {
             RESTTranslatedTopicV1 translatedTopic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTranslatedTopicV1.class, id, revision)) {
                 translatedTopic = getRESTEntityCache().get(RESTTranslatedTopicV1.class, id, revision);
 
                 if (translatedTopic.getTags() != null) {
                     return translatedTopic.getTags();
                 }
             }
 
             // We need to expand the all the tags in the translated topic
             final String expandString = getExpansionString(RESTTranslatedTopicV1.TAGS_NAME);
 
             // Load the translated topic from the REST Interface
             final RESTTranslatedTopicV1 tempTranslatedTopic = loadTranslatedTopic(id, revision, expandString);
 
             if (translatedTopic == null) {
                 translatedTopic = tempTranslatedTopic;
                 getRESTEntityCache().add(translatedTopic, revision);
             } else {
                 translatedTopic.setTags(tempTranslatedTopic.getTags());
             }
 
             return translatedTopic.getTags();
         } catch (Exception e) {
             log.debug("Failed to retrieve the Tags for Translated Topic " + id + (revision == null ? "" : (", Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TagWrapper> getTranslatedTopicTags(int id, Integer revision) {
         return getWrapperFactory().createCollection(getRESTTranslatedTopicTags(id, revision), RESTTagV1.class, revision != null);
     }
 
     public RESTAssignedPropertyTagCollectionV1 getRESTTranslatedTopicProperties(int id, Integer revision) {
         try {
             RESTTranslatedTopicV1 topic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTranslatedTopicV1.class, id, revision)) {
                 topic = getRESTEntityCache().get(RESTTranslatedTopicV1.class, id, revision);
 
                 if (topic.getProperties() != null) {
                     return topic.getProperties();
                 }
             }
 
             // We need to expand the all the properties in the translated topic
             final String expandString = getExpansionString(RESTTranslatedTopicV1.PROPERTIES_NAME);
 
             // Load the translated topic from the REST Interface
             final RESTTranslatedTopicV1 tempTopic = loadTranslatedTopic(id, revision, expandString);
 
             if (topic == null) {
                 topic = tempTopic;
                 getRESTEntityCache().add(topic, revision);
             } else {
                 topic.setProperties(tempTopic.getProperties());
             }
 
             return topic.getProperties();
         } catch (Exception e) {
             log.debug("Failed to retrieve the Properties for Translated Topic " + id + (revision == null ? "" : (", " +
                     "Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<PropertyTagInTopicWrapper> getTranslatedTopicProperties(int id, Integer revision) {
         return getWrapperFactory().createCollection(getRESTTranslatedTopicProperties(id, revision), RESTAssignedPropertyTagV1.class,
                 revision != null, PropertyTagInTopicWrapper.class);
     }
 
     public RESTTranslatedTopicCollectionV1 getRESTTranslatedTopicOutgoingRelationships(int id, Integer revision) {
         try {
             RESTTranslatedTopicV1 translatedTopic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTranslatedTopicV1.class, id, revision)) {
                 translatedTopic = getRESTEntityCache().get(RESTTranslatedTopicV1.class, id, revision);
 
                 if (translatedTopic.getOutgoingRelationships() != null) {
                     return translatedTopic.getOutgoingRelationships();
                 }
             }
 
             // We need to expand the all the outgoing relationships in the topic
             final String expandString = getExpansionString(RESTTranslatedTopicV1.OUTGOING_NAME);
 
             // Load the translated topic from the REST Interface
             final RESTTranslatedTopicV1 tempTranslatedTopic = loadTranslatedTopic(id, revision, expandString);
 
             if (translatedTopic == null) {
                 translatedTopic = tempTranslatedTopic;
                 getRESTEntityCache().add(translatedTopic, revision);
             } else {
                 translatedTopic.setOutgoingRelationships(tempTranslatedTopic.getOutgoingRelationships());
             }
 
             return translatedTopic.getOutgoingRelationships();
         } catch (Exception e) {
             log.debug("Failed to retrieve the Outgoing Topics for Translated Topic " + id + (revision == null ? "" : (", " +
                     "Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedTopicWrapper> getTranslatedTopicOutgoingRelationships(int id, Integer revision) {
         return getWrapperFactory().createCollection(getRESTTranslatedTopicOutgoingRelationships(id, revision), RESTTranslatedTopicV1.class,
                 revision != null);
     }
 
     public RESTTranslatedTopicCollectionV1 getRESTTranslatedTopicIncomingRelationships(int id, Integer revision) {
         try {
             RESTTranslatedTopicV1 translatedTopic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTranslatedTopicV1.class, id, revision)) {
                 translatedTopic = getRESTEntityCache().get(RESTTranslatedTopicV1.class, id, revision);
 
                 if (translatedTopic.getIncomingRelationships() != null) {
                     return translatedTopic.getIncomingRelationships();
                 }
             }
 
             // We need to expand the all the incoming relationships in the topic
             final String expandString = getExpansionString(RESTTranslatedTopicV1.INCOMING_NAME);
 
             // Load the translated topic from the REST Interface
             final RESTTranslatedTopicV1 tempTranslatedTopic = loadTranslatedTopic(id, revision, expandString);
 
             if (translatedTopic == null) {
                 translatedTopic = tempTranslatedTopic;
                 getRESTEntityCache().add(translatedTopic, revision);
             } else {
                 translatedTopic.setIncomingRelationships(tempTranslatedTopic.getIncomingRelationships());
             }
 
             return translatedTopic.getIncomingRelationships();
         } catch (Exception e) {
             log.debug("Failed to retrieve the Incoming Topics for Translated Topic " + id + (revision == null ? "" : (", " +
                     "Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedTopicWrapper> getTranslatedTopicIncomingRelationships(int id, Integer revision) {
         return getWrapperFactory().createCollection(getRESTTranslatedTopicIncomingRelationships(id, revision), RESTTranslatedTopicV1.class,
                 revision != null);
     }
 
     @Override
     public CollectionWrapper<TopicSourceURLWrapper> getTranslatedTopicSourceUrls(int id, Integer revision) {
         throw new UnsupportedOperationException("A parent is needed to get Topic Source URLs using V1 of the REST Interface.");
     }
 
     public RESTTopicSourceUrlCollectionV1 getRESTTranslatedTopicSourceUrls(int id, Integer revision, RESTBaseTopicV1<?, ?, ?> parent) {
         try {
             RESTTranslatedTopicV1 translatedTopic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTranslatedTopicV1.class, id, revision)) {
                 translatedTopic = getRESTEntityCache().get(RESTTranslatedTopicV1.class, id, revision);
 
                 if (translatedTopic.getSourceUrls_OTM() != null) {
                     return translatedTopic.getSourceUrls_OTM();
                 }
             }
 
             // We need to expand the all the source urls in the topic
             final String expandString = getExpansionString(RESTTranslatedTopicV1.SOURCE_URLS_NAME);
 
             // Load the translated topic from the REST Interface
             final RESTTranslatedTopicV1 tempTranslatedTopic = loadTranslatedTopic(id, revision, expandString);
 
             if (translatedTopic == null) {
                 translatedTopic = tempTranslatedTopic;
                 getRESTEntityCache().add(translatedTopic, revision);
             } else {
                 translatedTopic.setSourceUrls_OTM(tempTranslatedTopic.getSourceUrls_OTM());
             }
 
             return translatedTopic.getSourceUrls_OTM();
         } catch (Exception e) {
             log.debug("Failed to retrieve the Source URLs for Translated Topic " + id + (revision == null ? "" : (", " +
                     "Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     public CollectionWrapper<TopicSourceURLWrapper> getTranslatedTopicSourceUrls(int id, Integer revision,
             RESTBaseTopicV1<?, ?, ?> parent) {
         return getWrapperFactory().createCollection(getRESTTranslatedTopicSourceUrls(id, revision, parent), RESTTopicSourceUrlV1.class,
                 revision != null, parent);
     }
 
     @Override
     public UpdateableCollectionWrapper<TranslatedTopicStringWrapper> getTranslatedTopicStrings(int id, Integer revision) {
         throw new UnsupportedOperationException("A parent is needed to get Translated Topic Strings using V1 of the REST Interface.");
     }
 
     public RESTTranslatedTopicStringCollectionV1 getRESTTranslatedTopicStrings(int id, Integer revision) {
         try {
             RESTTranslatedTopicV1 translatedTopic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTranslatedTopicV1.class, id, revision)) {
                 translatedTopic = getRESTEntityCache().get(RESTTranslatedTopicV1.class, id, revision);
 
                 if (translatedTopic.getTranslatedTopicStrings_OTM() != null) {
                     return translatedTopic.getTranslatedTopicStrings_OTM();
                 }
             }
 
             // We need to expand the all the translated strings in the topic
             final String expandString = getExpansionString(RESTTranslatedTopicV1.TRANSLATEDTOPICSTRING_NAME);
 
             // Load the translated topic from the REST Interface
             final RESTTranslatedTopicV1 tempTranslatedTopic = loadTranslatedTopic(id, revision, expandString);
 
             if (translatedTopic == null) {
                 translatedTopic = tempTranslatedTopic;
                 getRESTEntityCache().add(translatedTopic, revision);
             } else {
                 translatedTopic.setTranslatedTopicStrings_OTM(tempTranslatedTopic.getTranslatedTopicStrings_OTM());
             }
 
             return translatedTopic.getTranslatedTopicStrings_OTM();
         } catch (Exception e) {
             log.debug("Unable to retrieve the Translated Topic Strings for Translated Topic " + id + (revision == null ? "" : (", " +
                     "Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     public UpdateableCollectionWrapper<TranslatedTopicStringWrapper> getTranslatedTopicStrings(int id, Integer revision,
             RESTTranslatedTopicV1 parent) {
         final CollectionWrapper<TranslatedTopicStringWrapper> collection = getWrapperFactory().createCollection(
                 getRESTTranslatedTopicStrings(id, revision), RESTTranslatedTopicStringV1.class, revision != null, parent);
         return (UpdateableCollectionWrapper<TranslatedTopicStringWrapper>) collection;
     }
 
     public RESTTranslatedTopicCollectionV1 getRESTTranslatedTopicRevisions(int id, final Integer revision) {
         try {
             RESTTranslatedTopicV1 translatedTopic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTranslatedTopicV1.class, id, revision)) {
                 translatedTopic = getRESTEntityCache().get(RESTTranslatedTopicV1.class, id, revision);
 
                 if (translatedTopic.getRevisions() != null) {
                     return translatedTopic.getRevisions();
                 }
             }
 
             // We need to expand the all the revisions in the topic
             final String expandString = getExpansionString(RESTTranslatedTopicV1.REVISIONS_NAME);
 
             // Load the translated topic from the REST Interface
             final RESTTranslatedTopicV1 tempTranslatedTopic = loadTranslatedTopic(id, revision, expandString);
 
             if (translatedTopic == null) {
                 translatedTopic = tempTranslatedTopic;
                 getRESTEntityCache().add(translatedTopic, revision);
             } else {
                 translatedTopic.setRevisions(tempTranslatedTopic.getRevisions());
             }
 
             return translatedTopic.getRevisions();
         } catch (Exception e) {
             log.debug("Failed to retrieve the Revisions for Translated Topic " + id + (revision == null ? "" : (", Revision " + revision)),
                     e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedTopicWrapper> getTranslatedTopicRevisions(int id, final Integer revision) {
         return getWrapperFactory().createCollection(getRESTTranslatedTopicRevisions(id, revision), RESTTranslatedTopicV1.class, true);
     }
 
     public RESTTranslatedCSNodeV1 getRESTTranslatedTopicTranslatedCSNode(int id, Integer revision) {
         try {
             RESTTranslatedTopicV1 translatedTopic = null;
             // Check the cache first
             if (getRESTEntityCache().containsKeyValue(RESTTranslatedTopicV1.class, id, revision)) {
                 translatedTopic = getRESTEntityCache().get(RESTTranslatedTopicV1.class, id, revision);
 
                 if (translatedTopic.getTranslatedCSNode() != null) {
                     return translatedTopic.getTranslatedCSNode();
                 }
             }
 
             // We need to expand the translated cs node in the topic
             final String expandString = getExpansionString(RESTTranslatedTopicV1.TRANSLATED_CSNODE_NAME);
 
             // Load the translated topic from the REST Interface
             final RESTTranslatedTopicV1 tempTranslatedTopic = loadTranslatedTopic(id, revision, expandString);
 
             if (translatedTopic == null) {
                 translatedTopic = tempTranslatedTopic;
                 getRESTEntityCache().add(translatedTopic, revision);
             } else {
                 translatedTopic.setTranslatedCSNode(tempTranslatedTopic.getTranslatedCSNode());
             }
 
             return translatedTopic.getTranslatedCSNode();
         } catch (Exception e) {
             log.debug("Failed to retrieve the Translated Content Spec Node for Translated Topic " + id + (revision == null ? "" : (", " +
                     "Revision " + revision)), e);
             throw handleException(e);
         }
     }
 
     public RESTTranslatedTopicCollectionV1 getRESTTranslatedTopicsWithQuery(String query) {
         if (query == null || query.isEmpty()) return null;
 
         try {
             // We need to expand the all the translated topics in the collection
            final String expandString = getExpansionString(RESTv1Constants.TRANSLATEDTOPICS_EXPANSION_NAME, RESTTranslatedTopicV1.TOPIC_NAME);
             final RESTTranslatedTopicCollectionV1 topics = getRESTClient().getJSONTranslatedTopicsWithQuery(
                     new PathSegmentImpl(query, false), expandString);
             getRESTEntityCache().add(topics);
 
             return topics;
         } catch (Exception e) {
             log.debug("Failed to retrieve Translated Topics with Query: " + query, e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedTopicWrapper> getTranslatedTopicsWithQuery(String query) {
         if (query == null || query.isEmpty()) return null;
 
         return getWrapperFactory().createCollection(getRESTTranslatedTopicsWithQuery(query), RESTTranslatedTopicV1.class, false);
     }
 
     @Override
     public TranslatedTopicWrapper createTranslatedTopic(final TranslatedTopicWrapper topic) {
         try {
             final RESTTranslatedTopicV1 updatedTopic = getRESTClient().createJSONTranslatedTopic("",
                     ((RESTTranslatedTopicV1Wrapper) topic).unwrap());
             if (updatedTopic != null) {
                 getRESTEntityCache().add(updatedTopic);
                 return getWrapperFactory().create(updatedTopic, false);
             } else {
                 return null;
             }
         } catch (Exception e) {
             log.debug("", e);
             throw handleException(e);
         }
     }
 
     @Override
     public TranslatedTopicWrapper updateTranslatedTopic(TranslatedTopicWrapper topic) {
         try {
             final RESTTranslatedTopicV1 updatedTopic = getRESTClient().updateJSONTranslatedTopic("",
                     ((RESTTranslatedTopicV1Wrapper) topic).unwrap());
             if (updatedTopic != null) {
                 getRESTEntityCache().expire(RESTTranslatedTopicV1.class, topic.getId());
                 getRESTEntityCache().add(updatedTopic);
                 return getWrapperFactory().create(updatedTopic, false);
             } else {
                 return null;
             }
         } catch (Exception e) {
             log.debug("", e);
             throw handleException(e);
         }
     }
 
     @Override
     public boolean deleteTranslatedTopic(Integer id) {
         try {
             final RESTTranslatedTopicV1 topic = getRESTClient().deleteJSONTranslatedTopic(id, "");
             return topic != null;
         } catch (Exception e) {
             log.debug("", e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedTopicWrapper> createTranslatedTopics(
             CollectionWrapper<TranslatedTopicWrapper> topics) {
         try {
             final RESTTranslatedTopicCollectionV1 unwrappedTopics = ((RESTTranslatedTopicCollectionV1Wrapper) topics).unwrap();
 
             final String expandString = getExpansionString(RESTv1Constants.TRANSLATEDTOPICS_EXPANSION_NAME);
             final RESTTranslatedTopicCollectionV1 updatedTopics = getRESTClient().createJSONTranslatedTopics(expandString, unwrappedTopics);
             if (updatedTopics != null) {
                 getRESTEntityCache().add(updatedTopics, false);
                 return getWrapperFactory().createCollection(updatedTopics, RESTTranslatedTopicV1.class, false);
             } else {
                 return null;
             }
         } catch (Exception e) {
             log.debug("", e);
             throw handleException(e);
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedTopicWrapper> updateTranslatedTopics(
             CollectionWrapper<TranslatedTopicWrapper> topics) {
         try {
             final RESTTranslatedTopicCollectionV1 unwrappedTopics = ((RESTTranslatedTopicCollectionV1Wrapper) topics).unwrap();
 
             final String expandString = getExpansionString(RESTv1Constants.TRANSLATEDTOPICS_EXPANSION_NAME);
             final RESTTranslatedTopicCollectionV1 updatedTopics = getRESTClient().updateJSONTranslatedTopics(expandString, unwrappedTopics);
             if (updatedTopics != null) {
                 // Expire the old cached data
                 for (final RESTTranslatedTopicV1 topic : unwrappedTopics.returnItems()) {
                     getRESTEntityCache().expire(RESTTranslatedTopicV1.class, topic.getId());
                 }
                 // Add the new data to the cache
                 getRESTEntityCache().add(updatedTopics, false);
                 return getWrapperFactory().createCollection(updatedTopics, RESTTranslatedTopicV1.class, false);
             } else {
                 return null;
             }
         } catch (Exception e) {
             log.debug("", e);
             throw handleException(e);
         }
     }
 
     @Override
     public boolean deleteTranslatedTopics(final List<Integer> topicIds) {
         try {
             final String pathString = "ids;" + CollectionUtilities.toSeperatedString(topicIds, ";");
             final PathSegment path = new PathSegmentImpl(pathString, false);
             final RESTTranslatedTopicCollectionV1 topics = getRESTClient().deleteJSONTranslatedTopics(path, "");
             return topics != null;
         } catch (Exception e) {
             log.debug("", e);
             throw handleException(e);
         }
     }
 
     @Override
     public TranslatedTopicWrapper newTranslatedTopic() {
         return getWrapperFactory().create(new RESTTopicV1(), false);
     }
 
     @Override
     public CollectionWrapper<TranslatedTopicWrapper> newTranslatedTopicCollection() {
         return getWrapperFactory().createCollection(new RESTTranslatedTopicCollectionV1(), RESTTranslatedTopicV1.class, false);
     }
 }
