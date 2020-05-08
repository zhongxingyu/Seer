 package com.aavu.server.service.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.dao.IncorrectResultSizeDataAccessException;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.aavu.client.domain.Meta;
 import com.aavu.client.domain.MetaSeeAlso;
 import com.aavu.client.domain.MetaValue;
 import com.aavu.client.domain.MindTreeOcc;
 import com.aavu.client.domain.Occurrence;
 import com.aavu.client.domain.RealTopic;
 import com.aavu.client.domain.Root;
 import com.aavu.client.domain.Topic;
 import com.aavu.client.domain.TopicTypeConnector;
 import com.aavu.client.domain.URI;
 import com.aavu.client.domain.User;
 import com.aavu.client.domain.WebLink;
 import com.aavu.client.domain.commands.AbstractCommand;
 import com.aavu.client.domain.commands.SaveSeeAlsoCommand;
 import com.aavu.client.domain.dto.DatedTopicIdentifier;
 import com.aavu.client.domain.dto.FullTopicIdentifier;
 import com.aavu.client.domain.dto.LinkAndUser;
 import com.aavu.client.domain.dto.LocationDTO;
 import com.aavu.client.domain.dto.TagStat;
 import com.aavu.client.domain.dto.TimeLineObj;
 import com.aavu.client.domain.dto.TopicIdentifier;
 import com.aavu.client.domain.mapper.MindTree;
 import com.aavu.client.exception.HippoBusinessException;
 import com.aavu.client.exception.HippoException;
 import com.aavu.client.exception.HippoPermissionException;
 import com.aavu.server.dao.EditDAO;
 import com.aavu.server.dao.SelectDAO;
 import com.aavu.server.service.TopicService;
 import com.aavu.server.service.UserService;
 import com.aavu.server.web.domain.UserPageBean;
 
 /**
  * TODO remove ApplicationContextAware. This was introduced when UserService began needing a
  * reference to TopicService
  * 
  * @author Jeff Dwyer
  * 
  */
 @Transactional
 public class TopicServiceImpl implements TopicService, ApplicationContextAware {
 	private static final Logger log = Logger.getLogger(TopicServiceImpl.class);
 
 	private static MetaSeeAlso seealsoSingleton;
 	private EditDAO editDAO;
 	private SelectDAO selectDAO;
 
 
 	private UserService userService;
 
 
 
 	// private SearchService searchService;
 
 
 
 	public void addLinkToTags(WebLink link, String[] tags) throws HippoBusinessException {
 		addLinkToTags(link, tags, null, new HashMap<String, Topic>());
 	}
 
 	/**
 	 * TODO this save() calls mustHaveUniqueName() and then getUnique...() which is totally
 	 * redundant and gives us n selects, which we know are redundant since we just did
 	 * createNewIfNonExistent(). this is probably why the del.icio.us add is so darn slow.
 	 * 
 	 * @throws HippoBusinessException
 	 */
 	// public void addLinkToTags(WebLink link, String[] tags, Topic parent)
 	// throws HippoBusinessException {
 	// addLinkToTags(link, tags, parent, new HashMap<String, Topic>());
 	// }
 	public void addLinkToTags(WebLink link, String[] tags, Topic parent,
 			Map<String, Topic> cachedTopics) throws HippoBusinessException {
 
 		User user = userService.getCurrentUser();
 
 		WebLink linkM = (WebLink) editDAO.merge(link);
 
 
 		for (String string : tags) {
 			log.debug("str: " + string);
 
 			String clipped = string.trim();
 
 			if (clipped.length() != 0) {
 
 				Topic t = cachedTopics.get(clipped);
 
 				if (t == null) {
 					if (parent != null) {
 						t = createNewIfNonExistent(clipped, parent);
 					} else {
 						t = createNewIfNonExistent(clipped);
 					}
 
 
 					t.addOccurence(linkM);
 					t = save(t, true, user);
 
 					cachedTopics.put(clipped, t);
 
 					// System.out.println("Cache Miss " + t + " " + t.getId() + " " + linkM);
 				} else {
 					Topic merged = editDAO.merge(t);
 					merged.addOccurence(linkM);
 
 					merged = save(merged, true, user);
 
 					cachedTopics.put(clipped, merged);
 
 					// System.out.println("Cache Hit " + t + " " + t.getId() + " " + linkM);
 				}
 			}
 
 		}
 	}
 
 	/**
 	 * 
 	 * @throws HippoPermissionException
 	 */
 	public void changeState(long topicID, boolean toIsland) throws HippoPermissionException {
 		Topic t = selectDAO.get(topicID);
 		if (!t.getUser().equals(userService.getCurrentUser())) {
 			throw new HippoPermissionException();
 		}
 		editDAO.changeState(t, toIsland);
 	}
 
 
 	private List<FullTopicIdentifier> connectorsToTIs(List<TopicTypeConnector> conns) {
 		List<FullTopicIdentifier> rtn = new ArrayList<FullTopicIdentifier>(conns.size());
 		for (TopicTypeConnector conn : conns) {
 			rtn.add(new FullTopicIdentifier(conn));
 
 
 			// log.debug("Topic on island Found " + conn.getId() + " " + conn.getLatitude() + " "
 			// + conn.getLongitude());
 
 		}
 		return rtn;
 	}
 
 
 
 	public <T> T createNew(String title, Class<T> type, Topic parent) throws HippoBusinessException {
 		return createNew(title, type, parent, null, null);
 	}
 
 	/**
 	 * If parent is null, that's ok, we're saving a Meta or something. Pass new Root(), to do a
 	 * lookup and get the actual root element. Pass parent to get tagged.
 	 * 
 	 * NOTE: must do the 'loadedParent' thing or some variation of this, to make sure that we don't
 	 * save a parent passed in directly from GWT. That led to some awful foreign key shenanagins as
 	 * we tried to duplicate the Collections.
 	 * 
 	 * If prototype is an occurrence, use addOccurrence()
 	 */
 
 	public <T> T createNew(String title, Class<T> type, Topic parent, int[] lnglat, Date dateCreated)
 			throws HippoBusinessException {
 
 		Topic prototype;
 		try {
 			prototype = (Topic) type.newInstance();
 		} catch (Exception e) {
 			throw new HippoBusinessException(e);
 		}
 
 		// if ((prototype instanceof RealTopic) && userIsOverSubscriptionLimit()) {
 		// log.info("User over Subscription Limit " + userService.getCurrentUser().getUsername());
 		// throw new HippoSubscriptionException("Too many topics for your subscription.");
 		// }
 
 		log.debug("Prototype " + prototype.getId() + " " + prototype.getClass());
 
 		log.info("create New: " + title + " " + prototype.getClass() + " "
 				+ userService.getCurrentUser().getUsername() + " parent " + parent);
 
 		prototype.setTitle(title);
 		if (dateCreated != null) {
 			prototype.setCreated(dateCreated);
 		}
 
 		prototype = save(prototype);
 
 		log.debug("Prototype " + prototype.getId() + " " + prototype.getClass());
 
 		if (parent != null) {
 			prototype = ensureTopicHasTag(prototype, parent, lnglat);
 		}
 
 
 
 		return (T) prototype;
 
 	}
 
 	/**
 	 * Split out this functionality so that it could be re-used aside from Topic creation, this is
 	 * because a createIfNotExistant() call will carry with it a parent. That relationship needs to
 	 * be created if it hasn't been already
 	 * 
 	 * @param topic
 	 * @param parent
 	 * @param lnglat
 	 * @return
 	 * @throws HippoBusinessException
 	 */
 	private Topic ensureTopicHasTag(Topic topic, Topic parent, int[] lnglat)
 			throws HippoBusinessException {
 		Topic loadedParent = selectDAO.getForID(userService.getCurrentUser(), parent.getId());
 
 		if (topic instanceof Occurrence) {
 			log.debug("adding occurrence");
 			loadedParent.addOccurence((Occurrence) topic, lnglat);
 			save(loadedParent);
 		} else if (topic instanceof MetaValue) {
 			log.info("adding MetaValue");
 			loadedParent.addMetaValue(null, topic);
 			save(loadedParent);
 		} else {
 
 			if (parent instanceof Root) {
 
 				topic.tagTopic(selectDAO.getRoot(userService.getCurrentUser(), userService
 						.getCurrentUser()), lnglat);
 			} else {
 				topic.tagTopic(loadedParent, lnglat);
 			}
 		}
 		return save(topic);
 	}
 
 	public Topic createNewIfNonExistent(String title) throws HippoBusinessException {
 		return createNewIfNonExistent(title, new Root());
 	}
 
 	/**
 	 * @param parent
 	 *            Also guarantee that this parent topic will be a type of created (or existing)
 	 *            topic
 	 */
 	public Topic createNewIfNonExistent(String title, Topic parent) throws HippoBusinessException {
 		return createNewIfNonExistent(title, RealTopic.class, parent);
 	}
 
 	/**
 	 * NOTE! this cast "(T)" is necessary to compile on the linux java 1.5.0_07 javac, but not here
 	 * in eclipse. How worrisome is that? Bug fixed for Dolphin b03, so no help there.
 	 * 
 	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6302954
 	 * http://forum.java.sun.com/thread.jspa?messageID=4196171
 	 * 
 	 * 
 	 * @param parent
 	 *            Also guarantee that this parent topic will be a type of created (or existing)
 	 *            topic
 	 */
 	public <T extends Topic> T createNewIfNonExistent(String title, Class<? extends Topic> type,
 			Topic parent) throws HippoBusinessException {
 		return (T) createNewIfNonExistent(title, type, parent, null, null);
 	}
 
 	/**
 	 * @param parent
 	 *            Also guarantee that this parent topic will be a type of created (or existing)
 	 *            topic
 	 */
 	public <T extends Topic> T createNewIfNonExistent(String title, Class<? extends Topic> type,
 			Topic parent, int[] lnglat, Date dateCreated) throws HippoBusinessException {
 		Topic cur = getForNameCaseInsensitive(title);
 
 		if (cur == null) {
 
 			try {
 				cur = createNew(title, type, parent, lnglat, dateCreated);
 			} catch (Exception e) {
 				throw new HippoBusinessException(e);
 			}
 		}
 		// enforce our guarantee that the parent tag exists
 		else {
 			ensureTopicHasTag(cur, parent, lnglat);
 		}
 		return (T) cur;
 	}
 
 	public <T extends URI> T createNewIfURINonexistant(Class<? extends URI> clazz, String uri,
 			String title, Date date, String data) throws HippoBusinessException {
 		User u = userService.getCurrentUser();
 
 		URI cur = selectDAO.getForURI(uri, u, u);
 
 		if (cur == null) {
 
 			try {
 				cur = (URI) clazz.newInstance();
 			} catch (Exception e) {
 				throw new HippoBusinessException(e);
 			}
 
 			log.debug("Prototype " + cur.getId() + " " + cur.getClass());
 
 			cur.setTitle(title);
 			cur.setData(data);
 			cur.setUri(uri);
 			cur.setCreated(date);
 
 			cur = (URI) save(cur);
 		}
 
 		return (T) cur;
 	}
 
 
 
 	public void delete(long id) throws HippoBusinessException {
 		Topic t = selectDAO.get(id);
 		if (!t.getUser().equals(userService.getCurrentUser())) {
 			throw new HippoPermissionException(userService.getCurrentUser() + " " + t.getUser());
 		}
 		delete(t);
 	}
 
 	/*
 	 * TODO maybe AOP secure this?
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see com.aavu.server.service.TopicService#delete(com.aavu.client.domain.Topic)
 	 */
 	public void delete(Topic topic) throws HippoBusinessException {
 		if (userService.getCurrentUser().equals(topic.getUser())) {
 
 			editDAO.deleteWithChildren(topic);
 
 			// TODO delete S3Files
 			// TODO delete Weblinks that were only referenced by us
 			// TODO_ delete our Entries - done
 			// TODO_ delete our MindTrees - done
 
 		} else {
 			throw new HippoBusinessException("User " + userService.getCurrentUser().getUsername()
 					+ " can't delete this topic");
 		}
 	}
 
 
 	/**
 	 * currently used by SaveMetaLocationCommand
 	 * 
 	 * @param command
 	 * @throws HippoBusinessException
 	 */
 	private void deleteCommand(AbstractCommand command) throws HippoBusinessException {
 		Set topics = command.getDeleteSet();
 		for (Iterator iter = topics.iterator(); iter.hasNext();) {
 			Topic topic = (Topic) iter.next();
 			log.info("DeleteCommand Delete: " + topic);
 			delete(topic);
 		}
 	}
 
 	/**
 	 * PEND MED efficiency
 	 * 
 	 * @param topics
 	 * @param visible
 	 * @throws HippoBusinessException
 	 */
 	public void editVisibility(List<TopicIdentifier> topics, boolean visible)
 			throws HippoBusinessException {
 		for (TopicIdentifier topicIdentifier : topics) {
 			Topic t = getForID(topicIdentifier.getTopicID());
 			if (t != null) {
 				t.setPublicVisible(visible);
 				save(t);
 			} else {
 				log.error("Edit Vis null " + topicIdentifier.getTopicID());
 			}
 		}
 	}
 
 	/**
 	 * 1) Hydrate. prepar the command. change the long id's into loaded hibernate objects. 2)
 	 * Execute. use the domain classes logic & the command to enact the change 3) Save.
 	 * 
 	 * TODO secure, BE carefull of messageService's use of this
 	 */
 	public void executeAndSaveCommand(AbstractCommand command) throws HippoException {
 
 		log.info(command + " " + userService.getCurrentUser().getUsername());
 
 		hydrateCommand(command);
 		command.executeCommand();
 		saveCommand(command);
 		deleteCommand(command);
 	}
 
 	public List<LocationDTO> getAllLocations() {
 		return selectDAO.getLocations(userService.getCurrentUser());
 	}
 
 
 
 	public List<Meta> getAllMetas() {
 		return selectDAO.getAllMetas(userService.getCurrentUser());
 	}
 
 	public List<DatedTopicIdentifier> getAllPublicTopicIdentifiers(String username, int start,
 			int max, String startStr) {
 		return selectDAO.getAllTopicIdentifiers(userService.getUserWithNormalization(username),
 				userService.getUserWithNormalization(username), start, max, startStr);
 	}
 
 	public List<DatedTopicIdentifier> getAllTopicIdentifiers() {
 		return selectDAO.getAllTopicIdentifiers(userService.getCurrentUser(), userService
 				.getCurrentUser(), false);
 	}
 
 	public List<DatedTopicIdentifier> getAllTopicIdentifiers(boolean all) {
 		return selectDAO.getAllTopicIdentifiers(userService.getCurrentUser(), userService
 				.getCurrentUser(), all);
 	}
 
 	public List<DatedTopicIdentifier> getAllTopicIdentifiers(int start, int max, String startStr) {
 		return selectDAO.getAllTopicIdentifiers(userService.getCurrentUser(), userService
 				.getCurrentUser(), start, max, startStr);
 	}
 
 
 	public List<Topic> getDeleteList(long topicID) {
 		return editDAO.getDeleteList(topicID);
 	}
 
 	public List<Topic> getMakePublicList(long topicID) {
 		return editDAO.getMakePublicList(topicID);
 	}
 
 	public Topic getForID(long topicID) {
 
 		return selectDAO.getForID(userService.getCurrentUser(), topicID);
 	}
 
 	public Topic getForNameCaseInsensitive(String string) {
 		return selectDAO.getForNameCaseInsensitive(userService.getCurrentUser(), string);
 	}
 
 
 	public List<TopicIdentifier> getLinksTo(Topic topic) {
 		return selectDAO.getLinksTo(topic, userService.getCurrentUser());
 	}
 
 	public List<List<LocationDTO>> getLocationsForTags(List<TopicIdentifier> shoppingList) {
 		List<List<LocationDTO>> rtn = new ArrayList<List<LocationDTO>>(shoppingList.size());
 
 		for (TopicIdentifier tag : shoppingList) {
 			rtn.add(selectDAO.getLocations(tag.getTopicID(), userService.getCurrentUser()));
 		}
 		return rtn;
 	}
 
 	public Topic getPublicTopic(String userString, String topicString)
 			throws HippoBusinessException {
 		Topic t = selectDAO.getPublicForName(userString, topicString);
 		if (t != null) {
 			return t;
 		} else {
 			if (userService.exists(userString)) {
 				throw new HippoBusinessException("No Topic Found");
 			} else {
 				throw new HippoBusinessException("No User " + userString + " Found");
 			}
 		}
 	}
 
 	public List<FullTopicIdentifier> getPublicTopicIdsWithTag(long id) {
 		return connectorsToTIs(selectDAO.getTopicIdsWithTag(id));
 	}
 
 	public Root getRootTopic(User forUser) {
 		return selectDAO.getRoot(forUser, userService.getCurrentUser());
 	}
 
 	public MetaSeeAlso getSeeAlsoMetaSingleton() throws HippoBusinessException {
 		if (seealsoSingleton == null) {
 			log.info("seealso single == null. Finding... ");
 
 			seealsoSingleton = selectDAO.getSeeAlsoSingleton();
 
 			if (seealsoSingleton == null) {
 				log.info("seealso single == null. Creating. First time DB?");
 
 				seealsoSingleton = new MetaSeeAlso();
 				seealsoSingleton = (MetaSeeAlso) editDAO.save(seealsoSingleton);
 
 			}
 		}
 
 		return seealsoSingleton;
 	}
 
 	public List<TopicIdentifier> getTagsStarting(String match) {
 		return selectDAO.getTagsStarting(userService.getCurrentUser(), match);
 	}
 
 	public List<TagStat> getTagStats() {
 		log.info("getting tag stats " + userService.getCurrentUser().getUsername());
 		return selectDAO.getTagStats(userService.getCurrentUser());
 	}
 
 	public List<TimeLineObj> getTimeline() {
 		return selectDAO.getTimeline(userService.getCurrentUser());
 	}
 
 
 
 	public List<List<TimeLineObj>> getTimelineWithTags(List<TopicIdentifier> shoppingList) {
 		List<List<TimeLineObj>> rtn = new ArrayList<List<TimeLineObj>>(shoppingList.size());
 
 		for (TopicIdentifier tag : shoppingList) {
 			rtn.add(selectDAO.getTimeline(tag.getTopicID(), userService.getCurrentUser()));
 		}
 		return rtn;
 	}
 
 	public List<FullTopicIdentifier> getTopicIdsWithTag(long id) {
 
 		List<TopicTypeConnector> conns = selectDAO.getTopicIdsWithTag(id, userService
 				.getCurrentUser());
 
 		return connectorsToTIs(conns);
 
 	}
 
 	public List<List<FullTopicIdentifier>> getTopicIdsWithTags(List<TopicIdentifier> shoppingList) {
 
 		List<List<FullTopicIdentifier>> rtn = new ArrayList<List<FullTopicIdentifier>>(shoppingList
 				.size());
 
 
 		for (TopicIdentifier tag : shoppingList) {
 			rtn.add(getTopicIdsWithTag(tag.getTopicID()));
 		}
 
 		return rtn;
 	}
 
 	public List<TopicIdentifier> getTopicsStarting(String match) {
 
 		return selectDAO.getTopicsStarting(userService.getCurrentUser(), match);
 	}
 
 	public MindTree getTree(MindTreeOcc occ) {
 		return selectDAO.getTree(occ);
 	}
 
 	public UserPageBean getUserPageBean(User su) {
 		return selectDAO.getUsageStats(su);
 	}
 
 	/**
 	 * TODO merge with getForURI
 	 */
 	public LinkAndUser getWebLinkForURLAndUser(String url) {
 		User u = userService.getCurrentUser();
 		// selectDAO.getForURI(url, u, u)
 		return new LinkAndUser(selectDAO.getWebLinkForURI(url, u), u);
 	}
 
 	/**
 	 * PEND Would prefer to make these loads instead of gets, but Tag's won't instanceof Tag.class
 	 * when we do that.
 	 * 
 	 * @param command
 	 * @throws HippoBusinessException
 	 */
 	private void hydrateCommand(AbstractCommand command) throws HippoBusinessException {
 
 		log.debug("Hydrate: " + command);
 
 		List ids = command.getTopicIDs();
 		int i = 0;
 		for (Iterator iter = ids.iterator(); iter.hasNext();) {
 			Long id = (Long) iter.next();
 			command.setTopic(i, selectDAO.get(id));
 			i++;
 		}
 
 		log.debug("Hydrated. " + command.getTopic(0) + " " + command.getTopic(1) + " "
 				+ command.getTopic(2));
 
 		// a bit messy, but it's tough to inject this otherwise, since we don't want the
 		// domain knowing about this service.
 		//
 		if (command instanceof SaveSeeAlsoCommand) {
 			command.setTopic(2, getSeeAlsoMetaSingleton());
 		}
 	}
 
 
 
 	public Occurrence save(Occurrence link) {
 		link.setUser(userService.getCurrentUser());
 		link.setLastUpdated(new Date());
 		return editDAO.save(link);
 	}
 
 	/**
 	 * TODO I don't really like setting the user here, but it's tricky to give the Topic & Entry
 	 * objects something that knows about users.
 	 * 
 	 */
 	public Topic save(Topic topic) throws HippoBusinessException {
 		return save(topic, false, userService.getCurrentUser());
 	}
 
 
 	private Topic save(Topic topic, boolean guaranteedNotDupe, User user)
 			throws HippoBusinessException {
 
 		if (topic == null) {
 			return null;
 		}
 
 		if (!topic.usesLastUpdated()) {
 			topic.setLastUpdated(new Date());
 		}
 		topic.setUser(user);
 
 
 		if (topic.mustHaveUniqueName() && topic.getTitle().equals("")) {
 			log.info("Throw HBE exception for Empty Title");
 			throw new HippoBusinessException("Empty Title");
 		}
 		if (topic.mustHaveUniqueName() && !guaranteedNotDupe) {
 			log.debug("Getting same named");
 
 			try {
 				Topic sameNamed = (Topic) selectDAO.getForNameCaseInsensitive(topic.getUser(),
 						topic.getTitle());
 				log.debug("Rec " + sameNamed);
 
 				if (sameNamed != null && sameNamed.getId() != topic.getId()) {
 					log.info("Throw HBE exception for Duplicate Title. ID: " + topic.getId()
 							+ " ID2:" + sameNamed.getId());
 					throw new HippoBusinessException("Duplicate Name");
 				}
 				log.debug("evict " + sameNamed);
 				// need to evict or we'll get a NonUniqueException
 				editDAO.evict(sameNamed);
 
 			} catch (IncorrectResultSizeDataAccessException e) {
 				log.info(e.getMessage() + " Throw HBE exception for Duplicate Title. ID: "
 						+ topic.getId() + " " + topic.getTitle());
 				throw new HippoBusinessException("Duplicate Name");
 			}
 
 
 		}
 
 		// log.debug("save "+topic.toPrettyString());
 
 
 		// TODO Necessary????
 		Set<Occurrence> occs = topic.getOccurenceObjs();
 		for (Occurrence o : occs) {
 			o.setUser(user);
 		}
 
 
 
 		return editDAO.save(topic);
 	}
 
 	private void saveCommand(AbstractCommand command) throws HippoBusinessException {
 
 		List topics = command.getTopics();
 		for (Iterator iter = topics.iterator(); iter.hasNext();) {
 			Topic topic = (Topic) iter.next();
 			log.debug("saveComand save " + topic);
 			if (topic instanceof Occurrence) {
 				Occurrence oc = (Occurrence) topic;
 				log.debug("was occ Data " + oc.getData());
 			} else {
 				log.debug("not oc");
 			}
 			save(topic);
 		}
 	}
 
 	public void saveOccurrenceLocation(long topicID, long occurrenceID, int lat, int lng)
 			throws HippoException {
 		editDAO.saveOccurrenceLocation(topicID, occurrenceID, lat, lng, userService
 				.getCurrentUser());
 	}
 
 	public void saveTopicLocation(long tagId, long topicId, int lat, int lng) throws HippoException {
 		editDAO.saveTopicsLocation(tagId, topicId, lat, lng, userService.getCurrentUser());
 	}
 
 	public MindTree saveTree(MindTree tree) {
 		return editDAO.save(tree);
 	}
 
 	@Required
 	public void setEditDAO(EditDAO editDAO) {
 		this.editDAO = editDAO;
 	}
 
 	@Required
 	public void setSelectDAO(SelectDAO selectDAO) {
 		this.selectDAO = selectDAO;
 	}
 
 	public void setUserService(UserService userService) {
 		this.userService = userService;
 	}
 
 	private boolean userIsOverSubscriptionLimit() {
 		User u = userService.getCurrentUser();
 		int curTopics = selectDAO.getTopicCount(u);
 		return u.getSubscription().getMaxTopics() < curTopics;
 	}
 
 
 	/**
 	 * avoid circular reference problems by loading this way
 	 */
 	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
 		setUserService((UserService) applicationContext.getBean("userService"));
 	}
 
 
 
 }
