 package com.redhat.contentspec.rest;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.ws.rs.core.PathSegment;
 
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.jboss.resteasy.specimpl.PathSegmentImpl;
 
 import com.redhat.contentspec.constants.CSConstants;
 import com.redhat.contentspec.entities.*;
 import com.redhat.contentspec.rest.utils.RESTCollectionCache;
 import com.redhat.contentspec.rest.utils.RESTEntityCache;
 import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;
 import com.redhat.topicindex.rest.entities.*;
 import com.redhat.topicindex.rest.expand.ExpandDataDetails;
 import com.redhat.topicindex.rest.expand.ExpandDataTrunk;
 import com.redhat.topicindex.rest.sharedinterface.RESTInterfaceV1;
 import com.redhat.ecs.commonutils.CollectionUtilities;
 import com.redhat.ecs.commonutils.ExceptionUtilities;
 
 public class RESTReader {
 	
 	private final Logger log = Logger.getLogger(RESTReader.class);
 	
 	private final RESTInterfaceV1 client;
 	private final ObjectMapper mapper = new ObjectMapper();
 	private final RESTEntityCache entityCache;
 	private final RESTCollectionCache collectionsCache;
 	
 	public RESTReader(RESTInterfaceV1 client, RESTEntityCache entityCache, RESTCollectionCache collectionsCache) {
 	    this.client = client;
 	    this.entityCache = entityCache;
 	    this.collectionsCache = collectionsCache;
 	}
 	
 	// CATEGORY QUERIES
 	
 	/*
 	 * Gets a specific category tuple from the database as specified by the categories ID.
 	 */
 	public CategoryV1 getCategoryById(int id) {
 		try {
 			if (entityCache.containsKeyValue(CategoryV1.class, id)) {
 				return (CategoryV1)entityCache.get(CategoryV1.class, id);
 			} else {
 				CategoryV1 category = client.getJSONCategory(id, null);
 				entityCache.add(category);
 				return category;
 			}
 		} catch (Exception e) {
 			log.debug(e.getMessage());
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/*
 	 * Gets a List of all categories tuples for a specified name.
 	 */
 	public List<CategoryV1> getCategoriesByName(String name) {
 		final List<CategoryV1> output = new ArrayList<CategoryV1>();
 		
 		try {
 			
 			BaseRestCollectionV1<CategoryV1> categories = collectionsCache.get(CategoryV1.class);
 			if (categories.getItems() == null) {
 				/* We need to expand the Categories collection */
 				final ExpandDataTrunk expand = new ExpandDataTrunk();
 				expand.setBranches(CollectionUtilities.toArrayList(new ExpandDataTrunk(new ExpandDataDetails("categories"))));
 				
 				final String expandString = mapper.writeValueAsString(expand);
 				final String expandEncodedString = URLEncoder.encode(expandString, "UTF-8");
 				
 				categories = client.getJSONCategories(expandEncodedString);
 				collectionsCache.add(CategoryV1.class, categories);
 			}
 		
 			if (categories != null) {
 				for (CategoryV1 cat: categories.getItems()) {
 					if (cat.getName().equals(name)) {
 						output.add(cat);
 					}
 				}
 			}
 
 			return output;
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return null;
 	}
 	
 	/*
 	 * Gets a Category item assuming that tags can only have one category
 	 */
 	public CategoryV1 getCategoryByTagId(int tagId) {		
 		TagV1 tag = getTagById(tagId);
 		if (tag == null) return null;
 		
 		return tag.getCategories().getItems().size() > 0 ? tag.getCategories().getItems().get(0) : null;
 	}
 	
 	// TAG QUERIES
 	
 	/*
 	 * Gets a specific tag tuple from the database as specified by the tags ID.
 	 */
 	public TagV1 getTagById(int id) {
 		try {
 			if (entityCache.containsKeyValue(TagV1.class, id)) {
 				return entityCache.get(TagV1.class, id);
 			} else {
 				/* We need to expand the Categories collection in most cases so expand it anyway*/
 				final ExpandDataTrunk expand = new ExpandDataTrunk();
 				expand.setBranches(CollectionUtilities.toArrayList(new ExpandDataTrunk(new ExpandDataDetails("categories")), new ExpandDataTrunk(new ExpandDataDetails("properties"))));
 				
 				final String expandString = mapper.writeValueAsString(expand);
 				final String expandEncodedString = URLEncoder.encode(expandString, "UTF-8");
 				TagV1 tag = client.getJSONTag(id, expandEncodedString);
 				entityCache.add(tag);
 				return tag;
 			}
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return null;
 	}
 	
 	/*
 	 * Gets a List of all tag tuples for a specified name.
 	 */
 	public List<TagV1> getTagsByName(String name) {
 		List<TagV1> output = new ArrayList<TagV1>();
 		
 		try {
 			
 			BaseRestCollectionV1<TagV1> tags = collectionsCache.get(TagV1.class);
 			if (tags.getItems() == null) {
 				/* We need to expand the Tags & Categories collection */
 				final ExpandDataTrunk expand = new ExpandDataTrunk();
 				final ExpandDataTrunk expandTags = new ExpandDataTrunk(new ExpandDataDetails("tags"));
 				expandTags.setBranches(CollectionUtilities.toArrayList(new ExpandDataTrunk(new ExpandDataDetails("categories"))));
 				expand.setBranches(CollectionUtilities.toArrayList(expandTags));
 				
 				final String expandString = mapper.writeValueAsString(expand);
 				final String expandEncodedString = URLEncoder.encode(expandString, "UTF-8");
 				
 				tags = client.getJSONTags(expandEncodedString);
 				collectionsCache.add(TagV1.class, tags);
 			}
 			
 			// Iterate through the list of tags and check if the tag is a Type and matches the name.
 			if (tags != null) {
 				for (TagV1 tag: tags.getItems()) {
 					if (tag.getName().equals(name)) {
 						output.add(tag);
 					}
 				}
 			}
 			
 			return output;
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return null;
 	}
 	
 	/*
 	 * Gets a List of Tag tuples for a specified its TopicID relationship through TopicToTag.
 	 */
 	public List<TagV1> getTagsByTopicId(int topicId) {
 		final TopicV1 topic;
 		if (entityCache.containsKeyValue(TopicV1.class, topicId)) {
 			topic = entityCache.get(TopicV1.class, topicId);
 		} else {
 			topic = getTopicById(topicId, null);
 		}
 		
 		return topic == null ? null : topic.getTags().getItems();
 	}
 	
 	// TOPIC QUERIES
 	
 	/*
 	 * Gets a specific tag tuple from the database as specified by the tags ID.
 	 */
 	public TopicV1 getTopicById(int id, Integer rev) {
 		try {
 			TopicV1 topic = null;
 			if (entityCache.containsKeyValue(TopicV1.class, id, rev)) {
 				topic = entityCache.get(TopicV1.class, id, rev);
 			} else {
 				/* We need to expand the all the items in the topic collection */
 				final ExpandDataTrunk expand = new ExpandDataTrunk();
 				final ExpandDataTrunk expandTags = new ExpandDataTrunk(new ExpandDataDetails("tags"));
 				expandTags.setBranches(CollectionUtilities.toArrayList(new ExpandDataTrunk(new ExpandDataDetails("categories")), new ExpandDataTrunk(new ExpandDataDetails("properties"))));
 				expand.setBranches(CollectionUtilities.toArrayList(expandTags, new ExpandDataTrunk(new ExpandDataDetails("sourceUrls")), new ExpandDataTrunk(new ExpandDataDetails("properties")),
 						new ExpandDataTrunk(new ExpandDataDetails("outgoingRelationships")), new ExpandDataTrunk(new ExpandDataDetails("incomingRelationships"))));
 
 				final String expandString = mapper.writeValueAsString(expand);
 				final String expandEncodedString = URLEncoder.encode(expandString, "UTF-8");
 				if (rev == null)
 				{
 					topic = client.getJSONTopic(id, expandEncodedString);
 					entityCache.add(topic);
 				}
 				else
 				{
 					topic = client.getJSONTopicRevision(id, rev, expandEncodedString);
 					entityCache.add(topic, true);
 				}
 			}
 			return topic;
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return null;
 	}
 	
 	/*
 	 * Gets a collection of topics based on the list of ids passed.
 	 */
 	public BaseRestCollectionV1<TopicV1> getTopicsByIds(final List<Integer> ids, final boolean expandTranslations) {
 		if (ids.isEmpty()) return null;
 		
 		try {
 			final BaseRestCollectionV1<TopicV1> topics = new BaseRestCollectionV1<TopicV1>();
 			final StringBuffer urlVars = new StringBuffer("query;topicIds=");
 			final String encodedComma = URLEncoder.encode(",", "UTF-8");
 			
 			for (Integer id: ids) {
 				if (!entityCache.containsKeyValue(TopicV1.class, id)) {
 					urlVars.append(id + encodedComma);
 				} else {
 					topics.addItem(entityCache.get(TopicV1.class, id));
 				}
 			}
 			
 			String query = urlVars.toString();
 			
 			/* Get the missing topics from the REST interface */
 			if (query.length() != "query;topicIds=".length())
 			{
 				query = query.substring(0, query.length() - encodedComma.length());
 				
 				PathSegment path = new PathSegmentImpl(query, false);
 				
 				/* We need to expand the all the items in the topic collection */
 				final ExpandDataTrunk expand = new ExpandDataTrunk();
 
 				final ExpandDataTrunk topicsExpand = new ExpandDataTrunk(new ExpandDataDetails("topics"));
 				final ExpandDataTrunk tags = new ExpandDataTrunk(new ExpandDataDetails("tags"));
 				final ExpandDataTrunk properties = new ExpandDataTrunk(new ExpandDataDetails(TopicV1.PROPERTIES_NAME));
 				final ExpandDataTrunk categories = new ExpandDataTrunk(new ExpandDataDetails("categories"));
 				final ExpandDataTrunk parentTags = new ExpandDataTrunk(new ExpandDataDetails("parenttags"));
 				final ExpandDataTrunk outgoingRelationships = new ExpandDataTrunk(new ExpandDataDetails("outgoingRelationships"));
 				final ExpandDataTrunk expandTranslatedTopics = new ExpandDataTrunk(new ExpandDataDetails(TopicV1.TRANSLATEDTOPICS_NAME));
 				
 				/* We need to expand the categories collection on the topic tags */
 				tags.setBranches(CollectionUtilities.toArrayList(categories, parentTags, properties));
				outgoingRelationships.setBranches(CollectionUtilities.toArrayList(tags, properties, expandTranslatedTopics));
 				if (expandTranslations)
 					topicsExpand.setBranches(CollectionUtilities.toArrayList(tags, outgoingRelationships, properties, new ExpandDataTrunk(new ExpandDataDetails("sourceUrls")), expandTranslatedTopics));
 				else
 					topicsExpand.setBranches(CollectionUtilities.toArrayList(tags, outgoingRelationships, properties, new ExpandDataTrunk(new ExpandDataDetails("sourceUrls"))));
 
 				expand.setBranches(CollectionUtilities.toArrayList(topicsExpand));
 				
 				final String expandString = mapper.writeValueAsString(expand);
 				final String expandEncodedString = URLEncoder.encode(expandString, "UTF-8");
 				BaseRestCollectionV1<TopicV1> downloadedTopics = client.getJSONTopicsWithQuery(path, expandEncodedString);
 				entityCache.add(downloadedTopics);
 				
 				/* Transfer the downloaded data to the current topic list */
 				if (downloadedTopics != null && downloadedTopics.getItems() != null) {
 					for (TopicV1 item: downloadedTopics.getItems()) {
 						topics.addItem(item);
 					}
 				}
 			}
 			
 			return topics;
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return null;
 	}
 	
 	/*
 	 * Gets a list of Revision's from the TopicIndex database for a specific topic
 	 */
 	public List<Object[]> getTopicRevisionsById(Integer topicId) {
 		List<Object[]> results = new ArrayList<Object[]>();
 		try {
 			final List<String> additionalKeys = CollectionUtilities.toArrayList("revisions", "topic" + topicId);
 			final BaseRestCollectionV1<TopicV1> topicRevisions;
 			if (collectionsCache.containsKey(TopicV1.class, additionalKeys)) {
 				topicRevisions = collectionsCache.get(TopicV1.class, additionalKeys);
 			} else {
 				/* We need to expand the Revisions collection */
 				final ExpandDataTrunk expand = new ExpandDataTrunk();
 				final ExpandDataTrunk expandTags = new ExpandDataTrunk(new ExpandDataDetails("tags"));
 				final ExpandDataTrunk expandRevs = new ExpandDataTrunk(new ExpandDataDetails("revisions"));
 				expandTags.setBranches(CollectionUtilities.toArrayList(new ExpandDataTrunk(new ExpandDataDetails("categories"))));
 				expandRevs.setBranches(CollectionUtilities.toArrayList(expandTags, new ExpandDataTrunk(new ExpandDataDetails("sourceUrls")), 
 						new ExpandDataTrunk(new ExpandDataDetails("properties")), new ExpandDataTrunk(new ExpandDataDetails("outgoingRelationships")),
 						new ExpandDataTrunk(new ExpandDataDetails("incomingRelationships"))));
 				expand.setBranches(CollectionUtilities.toArrayList(expandRevs));
 
 				final String expandString = mapper.writeValueAsString(expand);
 				final String expandEncodedString = URLEncoder.encode(expandString, "UTF-8");
 
 				final TopicV1 topic = client.getJSONTopic(topicId, expandEncodedString);
 				collectionsCache.add(TopicV1.class, topic.getRevisions(), additionalKeys, true);
 				topicRevisions = topic.getRevisions();
 			}
 
 			// Create the custom revisions list
 			if (topicRevisions != null && topicRevisions.getItems() != null) {
 				for (TopicV1 topicRev: topicRevisions.getItems()) {
 					Object[] revision =  new Object[2];
 					revision[0] = topicRev.getRevision();
 					revision[1] = topicRev.getLastModified();
 					results.add(revision);
 				}
 			}
 			return results;
 		} catch (Exception e) {
 			log.debug(e.getMessage());
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/*
 	 * Gets a List of TopicSourceUrl tuples for a specified its TopicID relationship through TopicToTopicSourceUrl.
 	 */
 	public List<TopicSourceUrlV1> getSourceUrlsByTopicId(int topicId) {
 		final TopicV1 topic;
 		if (entityCache.containsKeyValue(TopicV1.class, topicId)) {
 			topic = entityCache.get(TopicV1.class, topicId);
 		} else {
 			topic = getTopicById(topicId, null);
 		}
 		return topic == null ? null : topic.getSourceUrls_OTM().getItems();
 	}
 	
 	// TRANSLATED TOPICS QUERIES
 	
 	/*
 	 * Gets a collection of translated topics based on the list of topic ids passed.
 	 */
 	public BaseRestCollectionV1<TranslatedTopicV1> getTranslatedTopicsByTopicIds(List<Integer> ids, String locale) {
 		if (ids.isEmpty()) return null;
 		
 		try {
 			final BaseRestCollectionV1<TranslatedTopicV1> topics = new BaseRestCollectionV1<TranslatedTopicV1>();
 			final StringBuffer urlVars = new StringBuffer("query;latestTranslations=true;topicIds=");
 			final String encodedComma = URLEncoder.encode(",", "UTF-8");
 			
 			for (Integer id: ids) {
 				if (!entityCache.containsKeyValue(TranslatedTopicV1.class, id) && !entityCache.containsKeyValue(TranslatedTopicV1.class, (id * -1)))
 				{
 					urlVars.append(id + encodedComma);
 				}
 				else if (entityCache.containsKeyValue(TranslatedTopicV1.class, (id * -1)))
 				{
 					topics.addItem(entityCache.get(TranslatedTopicV1.class, (id * -1)));
 				}
 				else
 				{
 					topics.addItem(entityCache.get(TranslatedTopicV1.class, id));
 				}
 			}
 			
 			String query = urlVars.toString();
 			
 			if (query.length() != "query;latestTranslations=true;topicIds=".length())
 			{
 				query = query.substring(0, query.length() - encodedComma.length());
 				
 				/* Add the locale to the query if one was passed */
 				if (locale != null && !locale.isEmpty())
 					query += ";locale1=" + locale + "1";
 				
 				PathSegment path = new PathSegmentImpl(query, false);
 				
 				/* We need to expand the all the items in the translatedtopic collection */
 				final ExpandDataTrunk expand = new ExpandDataTrunk();
 
 				final ExpandDataTrunk translatedTopicsExpand = new ExpandDataTrunk(new ExpandDataDetails("translatedtopics"));
 				final ExpandDataTrunk topicExpandTranslatedTopics = new ExpandDataTrunk(new ExpandDataDetails(TopicV1.TRANSLATEDTOPICS_NAME));
 				final ExpandDataTrunk tags = new ExpandDataTrunk(new ExpandDataDetails("tags"));
 				final ExpandDataTrunk properties = new ExpandDataTrunk(new ExpandDataDetails(TopicV1.PROPERTIES_NAME));
 				final ExpandDataTrunk categories = new ExpandDataTrunk(new ExpandDataDetails("categories"));
 				final ExpandDataTrunk parentTags = new ExpandDataTrunk(new ExpandDataDetails("parenttags"));
 				final ExpandDataTrunk outgoingRelationships = new ExpandDataTrunk(new ExpandDataDetails(TranslatedTopicV1.ALL_LATEST_OUTGOING_NAME));
 				final ExpandDataTrunk topicsExpand = new ExpandDataTrunk(new ExpandDataDetails(TranslatedTopicV1.TOPIC_NAME));
 				
 				/* We need to expand the categories collection on the topic tags */
 				tags.setBranches(CollectionUtilities.toArrayList(categories, parentTags, properties));
 				outgoingRelationships.setBranches(CollectionUtilities.toArrayList(tags, properties, topicsExpand));
 				
 				topicsExpand.setBranches(CollectionUtilities.toArrayList(topicExpandTranslatedTopics));
 				
 				translatedTopicsExpand.setBranches(CollectionUtilities.toArrayList(tags, outgoingRelationships, properties, topicsExpand));
 
 				expand.setBranches(CollectionUtilities.toArrayList(translatedTopicsExpand));
 				
 				final String expandString = mapper.writeValueAsString(expand);
 				final String expandEncodedString = URLEncoder.encode(expandString, "UTF-8");
 				BaseRestCollectionV1<TranslatedTopicV1> downloadedTopics = client.getJSONTranslatedTopicsWithQuery(path, expandEncodedString);
 				entityCache.add(downloadedTopics);
 				
 				/* Transfer the downloaded data to the current topic list */
 				if (downloadedTopics != null && downloadedTopics.getItems() != null) {
 					for (TranslatedTopicV1 item: downloadedTopics.getItems()) {
 						topics.addItem(item);
 					}
 				}
 			}
 			
 			return topics;
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return null;
 	}
 	
 	/*
 	 * Gets a collection of translated topics based on the list of topic ids
 	 * passed.
 	 */
 	public BaseRestCollectionV1<TranslatedTopicV1> getTranslatedTopicsByZanataIds(final List<Integer> ids, final String locale)
 	{
 		if (ids.isEmpty())
 			return null;
 
 		try
 		{
 			final BaseRestCollectionV1<TranslatedTopicV1> topics = new BaseRestCollectionV1<TranslatedTopicV1>();
 			final StringBuffer urlVars = new StringBuffer("query;latestTranslations=true;zanataIds=");
 			final String encodedComma = URLEncoder.encode(",", "UTF-8");
 
 			for (Integer id : ids)
 			{
 				if (!entityCache.containsKeyValue(TranslatedTopicV1.class, id))
 				{
 					urlVars.append(id + encodedComma);
 				}
 				else
 				{
 					topics.addItem(entityCache.get(TranslatedTopicV1.class, id));
 				}
 			}
 
 			String query = urlVars.toString();
 
 			if (query.length() != "query;latestTranslations=true;zanataIds=".length())
 			{
 				query = query.substring(0, query.length() - encodedComma.length());
 
 				/* Add the locale to the query if one was passed */
 				if (locale != null && !locale.isEmpty())
 					query += ";locale1=" + locale + "1";
 
 				PathSegment path = new PathSegmentImpl(query, false);
 
 				/*
 				 * We need to expand the all the items in the translatedtopic
 				 * collection
 				 */
 				final ExpandDataTrunk expand = new ExpandDataTrunk();
 
 				final ExpandDataTrunk translatedTopicsExpand = new ExpandDataTrunk(new ExpandDataDetails("translatedtopics"));
 				final ExpandDataTrunk topicExpandTranslatedTopics = new ExpandDataTrunk(new ExpandDataDetails(TopicV1.TRANSLATEDTOPICS_NAME));
 				final ExpandDataTrunk tags = new ExpandDataTrunk(new ExpandDataDetails("tags"));
 				final ExpandDataTrunk properties = new ExpandDataTrunk(new ExpandDataDetails(BaseTopicV1.PROPERTIES_NAME));
 				final ExpandDataTrunk categories = new ExpandDataTrunk(new ExpandDataDetails("categories"));
 				final ExpandDataTrunk parentTags = new ExpandDataTrunk(new ExpandDataDetails("parenttags"));
 				final ExpandDataTrunk outgoingRelationships = new ExpandDataTrunk(new ExpandDataDetails(TranslatedTopicV1.ALL_LATEST_OUTGOING_NAME));
 				final ExpandDataTrunk topicsExpand = new ExpandDataTrunk(new ExpandDataDetails(TranslatedTopicV1.TOPIC_NAME));
 
 				/* We need to expand the categories collection on the topic tags */
 				tags.setBranches(CollectionUtilities.toArrayList(categories, parentTags, properties));
 				outgoingRelationships.setBranches(CollectionUtilities.toArrayList(tags, properties, topicsExpand));
 
 				topicsExpand.setBranches(CollectionUtilities.toArrayList(topicExpandTranslatedTopics));
 
 				translatedTopicsExpand.setBranches(CollectionUtilities.toArrayList(tags, outgoingRelationships, properties, topicsExpand));
 
 				expand.setBranches(CollectionUtilities.toArrayList(translatedTopicsExpand));
 
 				final String expandString = mapper.writeValueAsString(expand);
 				final String expandEncodedString = URLEncoder.encode(expandString, "UTF-8");
 				BaseRestCollectionV1<TranslatedTopicV1> downloadedTopics = client.getJSONTranslatedTopicsWithQuery(path, expandEncodedString);
 				entityCache.add(downloadedTopics);
 
 				/* Transfer the downloaded data to the current topic list */
 				if (downloadedTopics != null && downloadedTopics.getItems() != null)
 				{
 					for (final TranslatedTopicV1 item : downloadedTopics.getItems())
 					{
 						entityCache.add(item, item.getZanataId(), false);
 						topics.addItem(item);
 					}
 				}
 			}
 
 			return topics;
 		}
 		catch (Exception e)
 		{
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return null;
 	}
 
 	
 	/*
 	 * Gets a translated topic based on a topic id and locale
 	 */
 	public TranslatedTopicV1 getTranslatedTopicByTopicId(final Integer id, final String locale) {
 		try {
 			final BaseRestCollectionV1<TranslatedTopicV1> topics = getTranslatedTopicsByTopicIds(CollectionUtilities.toArrayList(id), locale);
 			
 			return topics != null && topics.getItems() != null && topics.getItems().size() == 1 ? topics.getItems().get(0) : null;
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return null;
 	}
 	
 	// USER QUERIES
 	
 	/*
 	 * Gets a List of all User tuples for a specified name.
 	 */
 	public List<UserV1> getUsersByName(String userName) {
 		List<UserV1> output = new ArrayList<UserV1>();
 		
 		try {
 			
 			final BaseRestCollectionV1<UserV1> users;
 			if (collectionsCache.containsKey(UserV1.class)) {
 				users = collectionsCache.get(UserV1.class);
 			} else {
 				/* We need to expand the Users collection */
 				final ExpandDataTrunk expand = new ExpandDataTrunk();
 				expand.setBranches(CollectionUtilities.toArrayList(new ExpandDataTrunk(new ExpandDataDetails("users"))));
 				
 				final String expandString = mapper.writeValueAsString(expand);
 				final String expandEncodedString = URLEncoder.encode(expandString, "UTF-8");
 				users = client.getJSONUsers(expandEncodedString);
 				collectionsCache.add(UserV1.class, users);
 			}
 			
 			if (users != null) { 
 				for (UserV1 user: users.getItems()) {
 					if (user.getName().equals(userName)) {
 						output.add(user);
 					}
 				}
 			}
 			
 			return output;
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return null;
 	}
 	
 	/*
 	 * Gets a specific User tuple from the database as specified by the tags ID.
 	 */
 	public UserV1 getUserById(int id) {
 		try {
 			if (entityCache.containsKeyValue(UserV1.class, id)) {
 				return (UserV1)entityCache.get(UserV1.class, id);
 			} else {
 				UserV1 user = client.getJSONUser(id, null);
 				entityCache.add(user);
 				return user;
 			}
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return null;
 	}
 	
 	// CONTENT SPEC QUERIES
 	
 	/*
 	 * Gets a ContentSpec tuple for a specified id.
 	 */
 	public TopicV1 getContentSpecById(int id, Integer rev) {
 		TopicV1 cs = getTopicById(id, rev);
 		if (cs == null) return null;
 		List<TagV1> topicTypes = cs.getTagsInCategoriesByID(CollectionUtilities.toArrayList(CSConstants.TYPE_CATEGORY_ID));
 		for (TagV1 type: topicTypes) {
 			if (type.getId().equals(CSConstants.CONTENT_SPEC_TAG_ID)) return cs;
 		}
 		return null;
 	}
 	
 	/*
 	 * Gets a list of Revision's from the CSProcessor database for a specific content spec
 	 */
 	public List<Object[]> getContentSpecRevisionsById(Integer csId) {
 		List<Object[]> results = new ArrayList<Object[]>();
 		try {
 			final List<String> additionalKeys = CollectionUtilities.toArrayList("revision", "topic" + csId);
 			final BaseRestCollectionV1<TopicV1> topicRevisions;
 			if (collectionsCache.containsKey(TopicV1.class, additionalKeys)) {
 				topicRevisions = collectionsCache.get(TopicV1.class, additionalKeys);
 			} else {
 				/* We need to expand the Revisions collection */
 				final ExpandDataTrunk expand = new ExpandDataTrunk();
 				final ExpandDataTrunk expandTags = new ExpandDataTrunk(new ExpandDataDetails("tags"));
 				final ExpandDataTrunk expandRevs = new ExpandDataTrunk(new ExpandDataDetails("revisions"));
 				expandTags.setBranches(CollectionUtilities.toArrayList(new ExpandDataTrunk(new ExpandDataDetails("categories"))));
 				expandRevs.setBranches(CollectionUtilities.toArrayList(expandTags, new ExpandDataTrunk(new ExpandDataDetails("sourceUrls")), 
 						new ExpandDataTrunk(new ExpandDataDetails("properties")), new ExpandDataTrunk(new ExpandDataDetails("outgoingRelationships")),
 						new ExpandDataTrunk(new ExpandDataDetails("incomingRelationships"))));
 				expand.setBranches(CollectionUtilities.toArrayList(expandTags, expandRevs));
 				
 				final String expandString = mapper.writeValueAsString(expand);
 				final String expandEncodedString = URLEncoder.encode(expandString, "UTF-8");
 				
 				final TopicV1 topic = client.getJSONTopic(csId, expandEncodedString);
 				// Check that the topic is a content spec
 				if (!topic.isTaggedWith(CSConstants.CONTENT_SPEC_TAG_ID)) return null;
 				
 				// Add the content spec revisions to the cache
 				collectionsCache.add(TopicV1.class, topic.getRevisions(), additionalKeys, true);
 				topicRevisions = topic.getRevisions();
 			}
 			
 			// Create the unique array from the revisions
 			if (topicRevisions != null && topicRevisions.getItems() != null) {
 				for (TopicV1 topicRev: topicRevisions.getItems()) {
 					Object[] revision =  new Object[2];
 					revision[0] = topicRev.getRevision();
 					revision[1] = topicRev.getLastModified();
 					results.add(revision);
 				}
 			}
 			return results;
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return null;
 	}
 	
 	/*
 	 * Gets a list of all content specifications in the database or the first 50 if limit is set
 	 */
 	public List<TopicV1> getContentSpecs(Integer startPos, Integer limit) {
 		List<TopicV1> results = new ArrayList<TopicV1>();
 		
 		try {
 			BaseRestCollectionV1<TopicV1> topics;
 			
 			// Set the startPos and limit to zero if they are null
 			startPos = startPos == null ? 0 : startPos;
 			limit = limit == null ? 0 : limit;
 			
 			final List<String> additionalKeys = CollectionUtilities.toArrayList("start-" + startPos, "end-" + (startPos + limit));
 			if (collectionsCache.containsKey(TopicV1.class, additionalKeys)) {
 				topics = collectionsCache.get(TopicV1.class, additionalKeys);
 			} else {
 				/* We need to expand the topics collection */
 				final ExpandDataTrunk expand = new ExpandDataTrunk();
 				ExpandDataDetails expandDataDetails = new ExpandDataDetails("topics");
 				if (startPos != 0 && startPos != null) {
 					expandDataDetails.setStart(startPos);
 				}
 				if (limit != 0 && limit != null) {
 					expandDataDetails.setEnd(startPos + limit);
 				}
 				
 				final ExpandDataTrunk expandTopics = new ExpandDataTrunk(expandDataDetails);
 				final ExpandDataTrunk expandTags = new ExpandDataTrunk(new ExpandDataDetails("tags"));
 				expandTags.setBranches(CollectionUtilities.toArrayList(new ExpandDataTrunk(new ExpandDataDetails("categories"))));
 				expandTopics.setBranches(CollectionUtilities.toArrayList(expandTags, new ExpandDataTrunk(new ExpandDataDetails("properties"))));
 				
 				expand.setBranches(CollectionUtilities.toArrayList(expandTopics));
 				
 				final String expandString = mapper.writeValueAsString(expand);
 				final String expandEncodedString = URLEncoder.encode(expandString, "UTF-8");
 	
 				PathSegment path = new PathSegmentImpl("query;tag" + CSConstants.CONTENT_SPEC_TAG_ID + "=1;", false);
 				topics = client.getJSONTopicsWithQuery(path, expandEncodedString);
 				collectionsCache.add(TopicV1.class, topics, additionalKeys);
 			}
 			
 			return topics.getItems();
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return results;
 	}
 	
 	/*
 	 * Gets a list of Revision's from the CSProcessor database for a specific content spec
 	 */
 	public Integer getLatestCSRevById(Integer csId) {
 		TopicV1 cs = getTopicById(csId, null);
 		if (cs != null) {
 			return (Integer) cs.getRevision();
 		}
 		return null;
 	}
 	
 	/*
 	 * Get the Pre Processed Content Specification for a ID and Revision
 	 */
 	public TopicV1 getPreContentSpecById(Integer id, Integer revision) {
 		TopicV1 cs = getContentSpecById(id, revision);
 		List<Object[]> specRevisions = getContentSpecRevisionsById(id);
 		
 		if (specRevisions == null) return null;
 		
 		// Create a sorted set of revision ids that are less the the current revision
 		SortedSet<Integer> sortedSpecRevisions = new TreeSet<Integer>();
 		for (Object[] specRev: specRevisions) {
 			if ((Integer)specRev[0] <= (Integer)cs.getRevision()) {
 				sortedSpecRevisions.add((Integer) specRev[0]);
 			}
 		}
 		
 		if (sortedSpecRevisions.size() == 0) return null;
 		
 		// Find the Pre Content Spec from the revisions
 		TopicV1 preContentSpec = null;
 		Integer specRev = sortedSpecRevisions.last();
 		while (specRev != null) {
 			TopicV1 contentSpecRev = getContentSpecById(id, specRev);
 			if (contentSpecRev.getProperty(CSConstants.CSP_TYPE_PROPERTY_TAG_ID) != null
 					&& contentSpecRev.getProperty(CSConstants.CSP_TYPE_PROPERTY_TAG_ID).getValue().equals(CSConstants.CSP_PRE_PROCESSED_STRING)) {
 				preContentSpec = contentSpecRev;
 				break;
 			}
 			specRev = sortedSpecRevisions.headSet(specRev).isEmpty() ? null : sortedSpecRevisions.headSet(specRev).last();
 		}
 		return preContentSpec;
 	}
 	
 	/*
 	 * Get the Pre Processed Content Specification for a ID and Revision
 	 */
 	public TopicV1 getPostContentSpecById(Integer id, Integer revision) {
 		TopicV1 cs = getContentSpecById(id, revision);
 		List<Object[]> specRevisions = getContentSpecRevisionsById(id);
 		
 		if (specRevisions == null) return null;
 		
 		// Create a sorted set of revision ids that are less the the current revision
 		SortedSet<Integer> sortedSpecRevisions = new TreeSet<Integer>();
 		for (Object[] specRev: specRevisions) {
 			if ((Integer)specRev[0] <= (Integer)cs.getRevision()) {
 				sortedSpecRevisions.add((Integer) specRev[0]);
 			}
 		}
 		
 		if (sortedSpecRevisions.size() == 0) return null;
 		
 		// Find the Pre Content Spec from the revisions
 		TopicV1 postContentSpec = null;
 		Integer specRev = sortedSpecRevisions.last();
 		while (specRev != null) {
 			TopicV1 contentSpecRev = getContentSpecById(id, specRev);
 			if (contentSpecRev.getProperty(CSConstants.CSP_TYPE_PROPERTY_TAG_ID) != null
 					&& contentSpecRev.getProperty(CSConstants.CSP_TYPE_PROPERTY_TAG_ID).getValue().equals(CSConstants.CSP_POST_PROCESSED_STRING)) {
 				postContentSpec = contentSpecRev;
 				break;
 			}
 			specRev = sortedSpecRevisions.headSet(specRev).isEmpty() ? null : sortedSpecRevisions.headSet(specRev).last();
 		}
 		return postContentSpec;
 	}
 	
 	// MISC QUERIES
 	
 	/*
 	 * Gets a List of all type tuples for a specified name.
 	 */
 	public TagV1 getTypeByName(String name) {
 		List<TagV1> tags = getTagsByName(name);
 		
 		// Iterate through the list of tags and check if the tag is a Type and matches the name.
 		if (tags != null) {
 			for (TagV1 tag: tags) {
 				if (tag.isInCategory(CSConstants.TYPE_CATEGORY_ID) && tag.getName().equals(name)) {
 					return tag;
 				}
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * Gets an Image File for a specific ID
 	 */
 	public ImageV1 getImageById(int id) {
 		try {
 			return client.getJSONImage(id, null);
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return null;
 	}
 	
 	// AUTHOR INFORMATION QUERIES
 	
 	/*
 	 * Gets the Author Tag for a specific topic 
 	 */
 	public TagV1 getAuthorForTopic(int topicId, Integer rev) {
 		if (rev == null) {
 			
 			final List<TagV1> tags = this.getTagsByTopicId(topicId);
 			
 			if (tags != null) {
 				for (TagV1 tag: tags) {
 					if (tag.isInCategory(CSConstants.WRITER_CATEGORY_ID)) return tag;
 				}
 			}
 		} else {
 			final TopicV1 topic = this.getTopicById(topicId, rev);
 			if (topic != null) {
 				for (TopicV1 topicRevision: topic.getRevisions().getItems()) {
 					if (topicRevision.getRevision().equals(rev)) {
 						List<TagV1> writerTags = topicRevision.getTagsInCategoriesByID(CollectionUtilities.toArrayList(CSConstants.WRITER_CATEGORY_ID));
 						if (writerTags.size() == 1) return writerTags.get(0);
 						break;
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	/*
 	 * Gets the Author Information for a specific author
 	 */
 	public AuthorInformation getAuthorInformation(Integer authorId) {
 		AuthorInformation authInfo = new AuthorInformation();
 		authInfo.setAuthorId(authorId);
 		TagV1 tag = getTagById(authorId);
 		if (tag != null && tag.getProperty(CSConstants.FIRST_NAME_PROPERTY_TAG_ID) != null
 				&& tag.getProperty(CSConstants.LAST_NAME_PROPERTY_TAG_ID) != null) {
 			authInfo.setFirstName(tag.getProperty(CSConstants.FIRST_NAME_PROPERTY_TAG_ID).getValue());
 			authInfo.setLastName(tag.getProperty(CSConstants.LAST_NAME_PROPERTY_TAG_ID).getValue());
 			if (tag.getProperty(CSConstants.EMAIL_PROPERTY_TAG_ID) != null) {
 				authInfo.setEmail(tag.getProperty(CSConstants.EMAIL_PROPERTY_TAG_ID).getValue());
 			}
 			if (tag.getProperty(CSConstants.ORGANIZATION_PROPERTY_TAG_ID) != null) {
 				authInfo.setOrganization(tag.getProperty(CSConstants.ORGANIZATION_PROPERTY_TAG_ID).getValue());
 			}
 			if (tag.getProperty(CSConstants.ORG_DIVISION_PROPERTY_TAG_ID) != null) {
 				authInfo.setOrgDivision(tag.getProperty(CSConstants.ORG_DIVISION_PROPERTY_TAG_ID).getValue());
 			}
 			return authInfo;
 		}
 		return null;
 	}
 	
 	/*
 	 * Gets a list of all content specifications in the database
 	 */
 	public int getNumberOfContentSpecs() {
 		List<TopicV1> contentSpecs = getContentSpecs(0, 0);
 		return contentSpecs.size();
 	}
 	
 	/*
 	 * Gets a list of snapshots in the database for the the content spec id specified or all if the id is null.
 	 */
 	/*public List<CSSnapshot> getSnapshots(Integer csId, int startPos, int limit) {
 		Session sess = sm.getCSSession();
 		try {
 			Query query;
 			if (csId != null) {
 				query = sess.createQuery("from CSSnapshot where scopeId = " + csId);
 			} else {
 				query = sess.createQuery("from CSSnapshot");
 			}
 			if (startPos != 0) {
 				query.setFirstResult(startPos);
 			}
 			if (limit != 0) {
 				query.setMaxResults(limit);
 			}
 			return query.list();
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return new ArrayList<CSSnapshot>();
 	}*/
 	
 	/*
 	 * Gets the number of snapshots in the database for the the content spec id specified or all if the id is null.
 	 */
 	/*public long getNumberOfSnapshots(Integer csId) {
 		Session sess = sm.getCSSession();
 		try {
 			Query query;
 			if (csId != null) {
 				query = sess.createQuery("select count(*) from CSSnapshot where scopeId = " + csId);
 			} else {
 				query = sess.createQuery("select count(*) from CSSnapshot");
 			}
 			return (Long)query.uniqueResult();
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return 0;
 	}*/
 	
 	/*
 	 * Gets snapshot for the specified id or null if one isn't found
 	 */
 	/*public CSSnapshot getSnapshotById(int id) {
 		Session sess = sm.getCSSession();
 		try {
 			return (CSSnapshot) sess.get(CSSnapshot.class, id);
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return null;
 	}*/
 	
 	/*
 	 * Gets a list of all content specifications in the database that match the search string
 	 */
 	/*public List<CSSnapshot> searchSnapshots(String searchText, Integer startPos, Integer limit) {
 		Session sess = sm.getCSSession();
 		try {
 			Query query = sess.createQuery("from CSSnapshot where snapshotName like '%" + searchText + "%'");
 			if (startPos != null) {
 				query.setFirstResult(startPos);
 			}
 			if (limit != null) {
 				query.setMaxResults(limit);
 			}
 			return query.list();
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return new ArrayList<CSSnapshot>();
 	}*/
 	
 	/*public SnapshotTopicV1 getSnapshotTopicByTopicAndRevId(Integer topicId, Integer rev) {
 		try {
 			final BaseRestCollectionV1<SnapshotTopicV1> snapshotTopics;
 			if (entityCache.containsKey("SnapshotTopics")) {
 				snapshotTopics = (BaseRestCollectionV1<SnapshotTopicV1>)entityCache.get("SnapshotTopics");
 			} else {
 				/* We need to expand the Snapshot Topics collection */
 				/*final ExpandDataTrunk expand = new ExpandDataTrunk();
 				expand.setBranches(CollectionUtilities.toArrayList(new ExpandDataTrunk(new ExpandDataDetails("snapshottopics"))));
 				
 				final String expandString = mapper.writeValueAsString(expand);
 				final String expandEncodedString = URLEncoder.encode(expandString, "UTF-8");
 				
 				snapshotTopics = client.getJSONSnapshotTopics(expandEncodedString);
 				entityCache.add("SnapshotTopics", snapshotTopics);
 			}
 		
 			// List through the snapshotTopics and see if a topic exists for the Topic Id and Revision
 			if (snapshotTopics != null) {
 				for (SnapshotTopicV1 snapshotTopic: snapshotTopics.getItems()) {
 					if (snapshotTopic.getTopicId().equals(topicId) && snapshotTopic.getTopicRevision().equals(rev)) {
 						return snapshotTopic;
 					}
 				}
 			}
 		} catch (Exception e) {
 			log.error(ExceptionUtilities.getStackTrace(e));
 		}
 		return null;
 	}*/
 }
