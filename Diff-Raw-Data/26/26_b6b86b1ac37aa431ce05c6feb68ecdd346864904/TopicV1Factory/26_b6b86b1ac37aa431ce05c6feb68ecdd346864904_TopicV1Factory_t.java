 package org.jboss.pressgangccms.restserver;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.jboss.pressgangccms.rest.v1.collections.RESTBugzillaBugCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTPropertyTagCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTTopicCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTTopicSourceUrlCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTTranslatedTopicCollectionV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTBugzillaBugV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTPropertyTagV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTTopicSourceUrlV1;
 import org.jboss.pressgangccms.rest.v1.entities.RESTTopicV1;
import org.jboss.pressgangccms.rest.v1.entities.RESTTranslatedTopicV1;
 import org.jboss.pressgangccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgangccms.rest.v1.exceptions.InvalidParameterException;
 import org.jboss.pressgangccms.rest.v1.expansion.ExpandDataTrunk;
 import org.jboss.pressgangccms.restserver.entities.BugzillaBug;
 import org.jboss.pressgangccms.restserver.entities.PropertyTag;
 import org.jboss.pressgangccms.restserver.entities.Tag;
 import org.jboss.pressgangccms.restserver.entities.Topic;
 import org.jboss.pressgangccms.restserver.entities.TopicSourceUrl;
import org.jboss.pressgangccms.restserver.entities.TranslatedTopicData;
import org.jboss.pressgangccms.restserver.entities.TopicToPropertyTag;
 
 class TopicV1Factory extends RESTDataObjectFactory<RESTTopicV1, Topic, RESTTopicCollectionV1>
 {
 	TopicV1Factory()
 	{
 		super(Topic.class);
 	}
 
 	@Override
 	RESTTopicV1 createRESTEntityFromDBEntity(final Topic entity, final String baseUrl, final String dataType, final ExpandDataTrunk expand, final Number revision, final boolean expandParentReferences, final EntityManager entityManager)
 	{
 		assert entity != null : "Parameter topic can not be null";
 		assert baseUrl != null : "Parameter baseUrl can not be null";
 		assert expand != null : "Parameter expand can not be null";
 
 		final RESTTopicV1 retValue = new RESTTopicV1();
 
 		final List<String> expandOptions = new ArrayList<String>();
 		expandOptions.add(BaseRESTv1.TAGS_EXPANSION_NAME);
 		expandOptions.add(BaseRESTv1.TOPIC_INCOMING_RELATIONSHIPS_EXPANSION_NAME);
 		expandOptions.add(BaseRESTv1.TOPIC_OUTGOING_RELATIONSHIPS_EXPANSION_NAME);
 		expandOptions.add(BaseRESTv1.SOURCE_URLS_EXPANSION_NAME);
 		expandOptions.add(RESTTopicV1.BUGZILLABUGS_NAME);
 		expandOptions.add(RESTTopicV1.PROPERTIES_NAME);
 		if (revision == null)
 			expandOptions.add(RESTBaseEntityV1.REVISIONS_NAME);
 
 		retValue.setExpand(expandOptions);
 
 		/* Set simple properties */
 		retValue.setId(entity.getTopicId());
 		retValue.setTitle(entity.getTopicTitle());
 		retValue.setDescription(entity.getTopicText());
 		retValue.setXml(entity.getTopicXML());
 		retValue.setHtml(entity.getTopicRendered());
 		retValue.setLastModified(entity.getFixedLastModifiedDate());
 		retValue.setRevision(entity.getLatestRevision().intValue());
 		retValue.setCreated(entity.getTopicTimeStamp());
 		retValue.setLocale(entity.getTopicLocale());
 		retValue.setXmlErrors(entity.getTopicXMLErrors());
 
 		/* Set collections */
 		if (revision == null)
 		{
 			retValue.setRevisions(new RESTDataObjectCollectionFactory<RESTTopicV1, Topic, RESTTopicCollectionV1>().create(RESTTopicCollectionV1.class, new TopicV1Factory(), entity, entity.getRevisions(), RESTBaseEntityV1.REVISIONS_NAME, dataType, expand, baseUrl, entityManager));
 		}
 		retValue.setTags(new RESTDataObjectCollectionFactory<RESTTagV1, Tag, RESTTagCollectionV1>().create(RESTTagCollectionV1.class, new TagV1Factory(), entity.getTags(), BaseRESTv1.TAGS_EXPANSION_NAME, dataType, expand, baseUrl, entityManager));
 		retValue.setOutgoingRelationships(new RESTDataObjectCollectionFactory<RESTTopicV1, Topic, RESTTopicCollectionV1>().create(RESTTopicCollectionV1.class, new TopicV1Factory(), entity.getOutgoingRelatedTopicsArray(), BaseRESTv1.TOPIC_OUTGOING_RELATIONSHIPS_EXPANSION_NAME, dataType, expand, baseUrl, revision, entityManager));
 		retValue.setIncomingRelationships(new RESTDataObjectCollectionFactory<RESTTopicV1, Topic, RESTTopicCollectionV1>().create(RESTTopicCollectionV1.class, new TopicV1Factory(), entity.getIncomingRelatedTopicsArray(), BaseRESTv1.TOPIC_INCOMING_RELATIONSHIPS_EXPANSION_NAME, dataType, expand, baseUrl, revision, entityManager));
 		retValue.setProperties(new RESTDataObjectCollectionFactory<RESTPropertyTagV1, TopicToPropertyTag, RESTPropertyTagCollectionV1>().create(RESTPropertyTagCollectionV1.class, new TopicPropertyTagV1Factory(), entity.getTopicToPropertyTagsArray(), BaseRESTv1.PROPERTIES_EXPANSION_NAME, dataType, expand, baseUrl, revision, entityManager));
 		retValue.setSourceUrls_OTM(new RESTDataObjectCollectionFactory<RESTTopicSourceUrlV1, TopicSourceUrl, RESTTopicSourceUrlCollectionV1>().create(RESTTopicSourceUrlCollectionV1.class, new TopicSourceUrlV1Factory(), entity.getTopicSourceUrls(), BaseRESTv1.SOURCE_URLS_EXPANSION_NAME, dataType, expand, baseUrl, revision, false, entityManager));
 		retValue.setBugzillaBugs_OTM(new RESTDataObjectCollectionFactory<RESTBugzillaBugV1, BugzillaBug, RESTBugzillaBugCollectionV1>().create(RESTBugzillaBugCollectionV1.class, new BugzillaBugV1Factory(), entity.getBugzillaBugs(), RESTTopicV1.BUGZILLABUGS_NAME, dataType, expand, baseUrl, revision, false, entityManager));
 		retValue.setTranslatedTopics_OTM(new RESTDataObjectCollectionFactory<RESTTranslatedTopicV1, TranslatedTopicData, RESTTranslatedTopicCollectionV1>().create(RESTTranslatedTopicCollectionV1.class, new TranslatedTopicV1Factory(), entity.getTranslatedTopics(entityManager), RESTTopicV1.TRANSLATEDTOPICS_NAME, dataType, expand, baseUrl, revision, false, entityManager));
 		
 		retValue.setLinks(baseUrl, BaseRESTv1.TOPIC_URL_NAME, dataType, retValue.getId());
 
 		return retValue;
 	}
 
 	@Override
 	void syncDBEntityWithRESTEntity(final EntityManager entityManager, final Topic entity, final RESTTopicV1 dataObject) throws InvalidParameterException
 	{
 		/* sync the basic properties */
 		if (dataObject.hasParameterSet(RESTTopicV1.TITLE_NAME))
 			entity.setTopicTitle(dataObject.getTitle());
 		if (dataObject.hasParameterSet(RESTTopicV1.DESCRIPTION_NAME))
 			entity.setTopicText(dataObject.getDescription());
 		if (dataObject.hasParameterSet(RESTTopicV1.XML_NAME))
 			entity.setTopicXML(dataObject.getXml());
 		if (dataObject.hasParameterSet(RESTTopicV1.HTML_NAME))
 			entity.setTopicRendered(dataObject.getHtml());
 		if (dataObject.hasParameterSet(RESTTopicV1.LOCALE_NAME))
 			entity.setTopicLocale(dataObject.getLocale());
 		if (dataObject.hasParameterSet(RESTTopicV1.XML_ERRORS_NAME))
 			entity.setTopicXMLErrors(dataObject.getXmlErrors());
 
 		if (dataObject.hasParameterSet(RESTTopicV1.TAGS_NAME) && dataObject.getTags() != null && dataObject.getTags().getItems() != null)
 		{
 			dataObject.getTags().removeInvalidAddRemoveItemRequests();
 			
 			for (final RESTTagV1 tag : dataObject.getTags().getItems())
 			{
 				if (tag.getRemoveItem())
 				{
 					final Tag tagEntity = entityManager.find(Tag.class, tag.getId());
 					if (tagEntity == null)
 						throw new InvalidParameterException("No Tag entity was found with the primary key " + tag.getId());
 
 					entity.removeTag(tag.getId());
 				}
 			}
 
 			for (final RESTTagV1 tag : dataObject.getTags().getItems())
 			{
 				if (tag.getAddItem())
 				{
 					final Tag tagEntity = entityManager.find(Tag.class, tag.getId());
 					if (tagEntity == null)
 						throw new InvalidParameterException("No Tag entity was found with the primary key " + tag.getId());
 
 					entity.addTag(entityManager, tag.getId());
 				}
 			}
 		}
 
 		if (dataObject.hasParameterSet(RESTTopicV1.PROPERTIES_NAME) && dataObject.getProperties() != null && dataObject.getProperties().getItems() != null)
 		{
 			dataObject.getProperties().removeInvalidAddRemoveItemRequests();
 			
 			/* remove children first */
 			for (final RESTPropertyTagV1 restEntity : dataObject.getProperties().getItems())
 			{
 				if (restEntity.getRemoveItem())
 				{
 					final PropertyTag dbEntity = entityManager.find(PropertyTag.class, restEntity.getId());
 					if (dbEntity == null)
 						throw new InvalidParameterException("No PropertyTag entity was found with the primary key " + restEntity.getId());
 					entity.removePropertyTag(dbEntity, restEntity.getValue());
 				}
 			}
 
 			/* add children second */
 			for (final RESTPropertyTagV1 restEntity : dataObject.getProperties().getItems())
 			{
 				if (restEntity.getAddItem())
 				{
 					final PropertyTag dbEntity = entityManager.find(PropertyTag.class, restEntity.getId());
 					if (dbEntity == null)
 						throw new InvalidParameterException("No PropertyTag entity was found with the primary key " + restEntity.getId());
 
 					entity.addPropertyTag(dbEntity, restEntity.getValue());
 				}
 			}
 		}
 
 		/*
 		 * Persist the entity before adding anything else as they require an id
 		 * for the topic
 		 */
 		entityManager.persist(entity);
 
 		if (dataObject.hasParameterSet(RESTTopicV1.OUTGOING_NAME) && dataObject.getOutgoingRelationships() != null && dataObject.getOutgoingRelationships().getItems() != null)
 		{
 			dataObject.getOutgoingRelationships().removeInvalidAddRemoveItemRequests();
 			
 			for (final RESTTopicV1 topic : dataObject.getOutgoingRelationships().getItems())
 			{
 				if (topic.getRemoveItem())
 				{
 					final Topic otherTopic = entityManager.find(Topic.class, topic.getId());
 					if (otherTopic == null)
 						throw new InvalidParameterException("No Topic entity was found with the primary key " + topic.getId());
 
 					entity.removeRelationshipTo(topic.getId(), 1);
 				}
 			}
 
 			for (final RESTTopicV1 topic : dataObject.getOutgoingRelationships().getItems())
 			{
 				if (topic.getAddItem())
 				{
 					final Topic otherTopic = entityManager.find(Topic.class, topic.getId());
 					if (otherTopic == null)
 						throw new InvalidParameterException("No Topic entity was found with the primary key " + topic.getId());
 
 					entity.addRelationshipTo(entityManager, topic.getId(), 1);
 
 				}
 			}
 		}
 
 		if (dataObject.hasParameterSet(RESTTopicV1.INCOMING_NAME) && dataObject.getIncomingRelationships() != null && dataObject.getIncomingRelationships().getItems() != null)
 		{
 			dataObject.getIncomingRelationships().removeInvalidAddRemoveItemRequests();
 			
 			for (final RESTTopicV1 topic : dataObject.getIncomingRelationships().getItems())
 			{
 				if (topic.getRemoveItem())
 				{
 					final Topic otherTopic = entityManager.find(Topic.class, topic.getId());
 					if (otherTopic == null)
 						throw new InvalidParameterException("No Topic entity was found with the primary key " + topic.getId());
 
 					otherTopic.removeRelationshipTo(entity.getTopicId(), 1);
 
 				}
 			}
 
 			for (final RESTTopicV1 topic : dataObject.getIncomingRelationships().getItems())
 			{
 				if (topic.getAddItem())
 				{
 					final Topic otherTopic = entityManager.find(Topic.class, topic.getId());
 					if (otherTopic == null)
 						throw new InvalidParameterException("No Topic entity was found with the primary key " + topic.getId());
 
 					entity.addRelationshipFrom(entityManager, otherTopic.getTopicId(), 1);
 
 				}
 			}
 		}
 
 		/* One To Many - Add will create a child entity */
 		if (dataObject.hasParameterSet(RESTTopicV1.SOURCE_URLS_NAME) && dataObject.getSourceUrls_OTM() != null && dataObject.getSourceUrls_OTM().getItems() != null)
 		{
 			dataObject.getSourceUrls_OTM().removeInvalidAddRemoveItemRequests();
 			
 			for (final RESTTopicSourceUrlV1 url : dataObject.getSourceUrls_OTM().getItems())
 			{
 				if (url.getRemoveItem())
 				{
 
 					final TopicSourceUrl topicSourceUrl = entityManager.find(TopicSourceUrl.class, url.getId());
 					if (topicSourceUrl == null)
 						throw new InvalidParameterException("No TopicSourceUrl entity was found with the primary key " + url.getId());
 
 					entity.removeTopicSourceUrl(url.getId());
 
 				}
 			}
 
 			for (final RESTTopicSourceUrlV1 url : dataObject.getSourceUrls_OTM().getItems())
 			{
 				if (url.getAddItem())
 				{
 					final TopicSourceUrl dbEntity = new TopicSourceUrlV1Factory().createDBEntityFromRESTEntity(entityManager, url);
 					entityManager.persist(dbEntity);
 					entity.addTopicSourceUrl(dbEntity);
 				}
 			}
 		}
 
 		/* One To Many - Add will create a child entity */
 		if (dataObject.hasParameterSet(RESTTopicV1.BUGZILLABUGS_NAME) && dataObject.getBugzillaBugs_OTM() != null && dataObject.getBugzillaBugs_OTM().getItems() != null)
 		{
 			dataObject.getBugzillaBugs_OTM().removeInvalidAddRemoveItemRequests();
 			
 			for (final RESTBugzillaBugV1 restEntity : dataObject.getBugzillaBugs_OTM().getItems())
 			{
 				if (restEntity.getRemoveItem())
 				{
 					final BugzillaBug dbEntity = entityManager.find(BugzillaBug.class, restEntity.getId());
 					if (dbEntity == null)
 						throw new InvalidParameterException("No BugzillaBug entity was found with the primary key " + restEntity.getId());
 
 					entity.removeBugzillaBug(restEntity.getId());
 				}
 			}
 
 			for (final RESTBugzillaBugV1 restEntity : dataObject.getBugzillaBugs_OTM().getItems())
 			{
 				if (restEntity.getAddItem())
 				{
 					final BugzillaBug dbEntity = new BugzillaBugV1Factory().createDBEntityFromRESTEntity(entityManager, restEntity);
 					entityManager.persist(dbEntity);
 					entity.addBugzillaBug(dbEntity);
 				}
 			}
 		}
 	}
 }
