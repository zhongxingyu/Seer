 package org.jboss.pressgang.ccms.provider;
 
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jboss.pressgang.ccms.filter.TopicFieldFilter;
 import org.jboss.pressgang.ccms.filter.builder.TopicFilterQueryBuilder;
 import org.jboss.pressgang.ccms.filter.utils.EntityUtilities;
 import org.jboss.pressgang.ccms.filter.utils.FilterUtilities;
 import org.jboss.pressgang.ccms.model.Filter;
 import org.jboss.pressgang.ccms.model.Tag;
 import org.jboss.pressgang.ccms.model.Topic;
 import org.jboss.pressgang.ccms.model.TopicSourceUrl;
 import org.jboss.pressgang.ccms.model.TopicToPropertyTag;
 import org.jboss.pressgang.ccms.model.TranslatedTopicData;
import org.jboss.pressgang.ccms.model.utils.EnversUtilities;
 import org.jboss.pressgang.ccms.provider.listener.ProviderListener;
 import org.jboss.pressgang.ccms.utils.constants.CommonFilterConstants;
 import org.jboss.pressgang.ccms.wrapper.DBTopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.DBWrapperFactory;
 import org.jboss.pressgang.ccms.wrapper.LogMessageWrapper;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagInTopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.TagWrapper;
 import org.jboss.pressgang.ccms.wrapper.TopicSourceURLWrapper;
 import org.jboss.pressgang.ccms.wrapper.TopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.TranslatedTopicWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 
 public class DBTopicProvider extends DBDataProvider implements TopicProvider {
     protected DBTopicProvider(EntityManager entityManager, DBWrapperFactory wrapperFactory, List<ProviderListener> listeners) {
         super(entityManager, wrapperFactory, listeners);
     }
 
     @Override
     public TopicWrapper getTopic(int id) {
        final Topic topic = getEntityManager().find(Topic.class, id);
        return getWrapperFactory().create(topic, false);
     }
 
     @Override
     public TopicWrapper getTopic(int id, Integer revision) {
         if (revision == null) {
             return getTopic(id);
         } else {
            final Topic dummyTopic = new Topic();
            dummyTopic.setTopicId(id);

            return getWrapperFactory().create(EnversUtilities.getRevision(getEntityManager(), dummyTopic, revision), true);
         }
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> getTopics(final List<Integer> ids) {
         final CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
         final CriteriaQuery<Topic> topics = criteriaBuilder.createQuery(Topic.class);
         final Root<Topic> from = topics.from(Topic.class);
         topics.select(from);
         topics.where(from.get("topicId").in(ids));
 
         return getWrapperFactory().createCollection(executeQuery(topics), Topic.class, false);
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> getTopicsWithQuery(final String query) {
         final String fixedQuery = query.replace("query;", "");
         final String[] queryValues = fixedQuery.split(";");
         final Map<String, String> queryParameters = new HashMap<String, String>();
         for (final String value : queryValues) {
             if (value.trim().isEmpty()) continue;
             String[] keyValue = value.split("=", 2);
             try {
                 queryParameters.put(keyValue[0], URLDecoder.decode(keyValue[1], "UTF-8"));
             } catch (UnsupportedEncodingException e) {
                 // Should support UTF-8, if not throw a runtime error.
                 throw new RuntimeException(e);
             }
         }
 
         final Filter filter = EntityUtilities.populateFilter(getEntityManager(), queryParameters, CommonFilterConstants.FILTER_ID,
                 CommonFilterConstants.MATCH_TAG, CommonFilterConstants.GROUP_TAG, CommonFilterConstants.CATEORY_INTERNAL_LOGIC,
                 CommonFilterConstants.CATEORY_EXTERNAL_LOGIC, CommonFilterConstants.MATCH_LOCALE, new TopicFieldFilter());
 
         final TopicFilterQueryBuilder queryBuilder = new TopicFilterQueryBuilder(getEntityManager());
         final CriteriaQuery<Topic> criteriaQuery = FilterUtilities.buildQuery(filter, queryBuilder);
 
         return getWrapperFactory().createCollection(executeQuery(criteriaQuery), Topic.class, false);
     }
 
     @Override
     public CollectionWrapper<TagWrapper> getTopicTags(int id, final Integer revision) {
         final DBTopicWrapper topic = (DBTopicWrapper) getTopic(id, revision);
         if (topic == null) {
             return null;
         } else {
             return getWrapperFactory().createCollection(topic.unwrap().getTags(), Tag.class, revision != null);
         }
     }
 
     @Override
     public UpdateableCollectionWrapper<PropertyTagInTopicWrapper> getTopicProperties(int id, Integer revision) {
         final DBTopicWrapper topic = (DBTopicWrapper) getTopic(id, revision);
         if (topic == null) {
             return null;
         } else {
             final CollectionWrapper<PropertyTagInTopicWrapper> collection = getWrapperFactory().createCollection(
                     topic.unwrap().getTopicToPropertyTags(), TopicToPropertyTag.class, revision != null);
             return (UpdateableCollectionWrapper<PropertyTagInTopicWrapper>) collection;
         }
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> getTopicOutgoingRelationships(int id, Integer revision) {
         final DBTopicWrapper topic = (DBTopicWrapper) getTopic(id, revision);
         if (topic == null) {
             return null;
         } else {
             return getWrapperFactory().createCollection(topic.unwrap().getOutgoingRelatedTopicsArray(), Topic.class, revision != null);
         }
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> getTopicIncomingRelationships(int id, Integer revision) {
         final DBTopicWrapper topic = (DBTopicWrapper) getTopic(id, revision);
         if (topic == null) {
             return null;
         } else {
             return getWrapperFactory().createCollection(topic.unwrap().getIncomingRelatedTopicsArray(), Topic.class, revision != null);
         }
     }
 
     @Override
     public CollectionWrapper<TopicSourceURLWrapper> getTopicSourceUrls(int id, Integer revision) {
         final DBTopicWrapper topic = (DBTopicWrapper) getTopic(id, revision);
         if (topic == null) {
             return null;
         } else {
             return getWrapperFactory().createCollection(topic.unwrap().getTopicSourceUrls(), TopicSourceUrl.class, revision != null);
         }
     }
 
     @Override
     public CollectionWrapper<TranslatedTopicWrapper> getTopicTranslations(int id, final Integer revision) {
         final Topic topic = new Topic();
         topic.setTopicId(id);
         return getWrapperFactory().createCollection(topic.getTranslatedTopics(getEntityManager(), revision), TranslatedTopicData.class,
                 revision != null);
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> getTopicRevisions(int id, Integer revision) {
         final List<Topic> revisions = getRevisionList(Topic.class, id);
         return getWrapperFactory().createCollection(revisions, Topic.class, revision != null);
     }
 
     @Override
     public TopicWrapper createTopic(TopicWrapper topic) {
         return createTopic(topic, null);
     }
 
     @Override
     public TopicWrapper createTopic(TopicWrapper topic, LogMessageWrapper logMessage) {
         // Send notification events
         notifyCreateEntity(topic);
         notifyLogMessage(logMessage);
 
         // Persist the new entity
         getEntityManager().persist(topic.unwrap());
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return topic;
     }
 
     @Override
     public TopicWrapper updateTopic(TopicWrapper topic) {
         return updateTopic(topic, null);
     }
 
     @Override
     public TopicWrapper updateTopic(TopicWrapper topic, LogMessageWrapper logMessage) {
         // Send notification events
         notifyUpdateEntity(topic);
         notifyLogMessage(logMessage);
 
         // Persist the changes
         getEntityManager().persist(topic.unwrap());
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return topic;
     }
 
     @Override
     public boolean deleteTopic(Integer id) {
         return deleteTopic(id, null);
     }
 
     @Override
     public boolean deleteTopic(Integer id, LogMessageWrapper logMessage) {
         // Send notification events
         notifyDeleteEntity(Topic.class, id);
         notifyLogMessage(logMessage);
 
         final Topic topic = getEntityManager().find(Topic.class, id);
         getEntityManager().remove(topic);
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return true;
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> createTopics(CollectionWrapper<TopicWrapper> topics) {
         return createTopics(topics, null);
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> createTopics(CollectionWrapper<TopicWrapper> topics, LogMessageWrapper logMessage) {
         // Send notification events
         notifyCreateEntityCollection(topics);
         notifyLogMessage(logMessage);
 
         // Persist the new entities
         for (final TopicWrapper topic : topics.getItems()) {
             getEntityManager().persist(topic.unwrap());
         }
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return topics;
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> updateTopics(CollectionWrapper<TopicWrapper> topics) {
         return updateTopics(topics, null);
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> updateTopics(CollectionWrapper<TopicWrapper> topics, LogMessageWrapper logMessage) {
         // Send notification events
         notifyUpdateEntityCollection(topics);
         notifyLogMessage(logMessage);
 
         // Persist the changes for each entity
         for (final TopicWrapper topic : topics.getItems()) {
             getEntityManager().persist(topic.unwrap());
         }
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return topics;
     }
 
     @Override
     public boolean deleteTopics(List<Integer> topicIds) {
         return deleteTopics(topicIds, null);
     }
 
     @Override
     public boolean deleteTopics(List<Integer> topicIds, LogMessageWrapper logMessage) {
         // Send notification events
         notifyDeleteEntityCollection(Topic.class, topicIds);
         notifyLogMessage(logMessage);
 
         // Delete the topics
         for (final Integer id : topicIds) {
             final Topic topic = getEntityManager().find(Topic.class, id);
             getEntityManager().remove(topic);
         }
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return true;
     }
 
     @Override
     public TopicWrapper newTopic() {
         return getWrapperFactory().create(new Topic(), false);
     }
 
     @Override
     public CollectionWrapper<TopicWrapper> newTopicCollection() {
         return getWrapperFactory().createCollection(new ArrayList<Topic>(), Topic.class, false);
     }
 }
