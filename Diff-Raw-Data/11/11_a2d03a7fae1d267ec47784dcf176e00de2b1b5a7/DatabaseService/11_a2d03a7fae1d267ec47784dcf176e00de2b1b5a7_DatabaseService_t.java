 package net.thumbtack.updateNotifierBackend.database;
 
 import static net.thumbtack.updateNotifierBackend.updateChecker.CheckForUpdate.getNewHashCode;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import net.thumbtack.updateNotifierBackend.database.daosimpl.ResourceDAOImpl;
 import net.thumbtack.updateNotifierBackend.database.daosimpl.ResourceTagDAOImpl;
 import net.thumbtack.updateNotifierBackend.database.daosimpl.TagDAOImpl;
 import net.thumbtack.updateNotifierBackend.database.daosimpl.UserDAOImpl;
 import net.thumbtack.updateNotifierBackend.database.entities.Resource;
 import net.thumbtack.updateNotifierBackend.database.entities.Tag;
 import net.thumbtack.updateNotifierBackend.database.entities.User;
 import net.thumbtack.updateNotifierBackend.database.exceptions.DatabaseSeriousException;
 import net.thumbtack.updateNotifierBackend.database.exceptions.DatabaseTinyException;
 import net.thumbtack.updateNotifierBackend.database.mappers.UserMapper;
 
 import org.apache.ibatis.io.Resources;
 import org.apache.ibatis.session.SqlSession;
 import org.apache.ibatis.session.SqlSessionFactory;
 import org.apache.ibatis.session.SqlSessionFactoryBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 //REVU: You should use interfaces.
 // Create dao interfaces and make current Dao's classes as implementation of them.
 // All daos without implementation of base dao interface.
 // BaseDao should be with sqlSessionFactory and All daos should implement BaseDao interface.
 // And you don't have to send mapper to dao class, they will use sqlSessionFactory.
 public class DatabaseService {
 
 	private static final Logger log = LoggerFactory
 			.getLogger(DatabaseService.class);
 	private static DatabaseService db = null;
 
 	private SqlSession session;
 	private String RESOURCE = "mybatis-cfg.xml";
 
 	private TagDAOImpl tagDao;
 	private UserDAOImpl userDao;
 	private ResourceDAOImpl resourceDao;
 	private ResourceTagDAOImpl resourceTagDao;
 
 	public static DatabaseService getInstance() {
 		if (db == null) {
 			db = new DatabaseService();
 		}
 		return db;
 	}
 
 	private DatabaseService() {
 		InputStream inputStream = null;
 		try {
 			inputStream = Resources.getResourceAsStream(RESOURCE);
 		} catch (IOException e) {
 			log.error("Great crash: exception on initialize database");
 			// TODO Great crash should be here!
 			e.printStackTrace();
 		}
 		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
 				.build(inputStream);
 		session = sqlSessionFactory.openSession();
 		tagDao = new TagDAOImpl(session);
 		userDao = new UserDAOImpl(session);
 		resourceDao = new ResourceDAOImpl(session);
 		resourceTagDao = new ResourceTagDAOImpl(session);
 	}
 
 	public long getUserIdByEmailOrAdd(User user)
 			throws DatabaseSeriousException {
 		log.trace("Get user email by id; email: {}", user.getEmail());
 		User savedUser = userDao.get(user.getEmail());
 		if (savedUser == null) {
 			if (!userDao.add(user)) {
 				throw new DatabaseSeriousException("User can't to login");
 //			} else {
 //				session.commit();
 			}
 		} else {
 			user = savedUser;
 		}
 		return user.getId();
 	}
 
 	public User getUserEmailById(long id) throws DatabaseSeriousException {
 		log.trace("get user email by id; id: {}", id);
 		User user = userDao.get2(id);
 		if (user == null) {
 			// TODO user deletes his account when his resource was updationg?
 			// Serious, because i know, where this method is called
 			throw new DatabaseSeriousException("Can't get user by id");
 		}
 		return user;
 	}
 
 	public void addResource(Resource resource) throws DatabaseTinyException,
 			DatabaseSeriousException {
 		if (log.isTraceEnabled()) {
 			log.trace("Add resource; resource: {}", resource.toString());
 		}
 		if (!userDao.exists(resource.getUserId())) {
 			log.debug("Database exception: can't add resource to nonexist user");
 			throw new DatabaseTinyException(
 					"Database exception: can't add resource to nonexist user");
 		}
 		resource.setHash(getNewHashCode(resource));
 
 		if (!resourceDao.add(resource)) {
 			throw new DatabaseSeriousException("Resource addition failed");
 		}
 		session.commit();
 		if (resource.getTags() != null) {
 			for (Long tagId : resource.getTags()) {
 				if (!resourceTagDao.add(resource.getId(), tagId)) {
 					throw new DatabaseSeriousException(
 							"Relation addition failed");
 				}
 			}
 		}
 		session.commit();
 	}
 
 	public List<Resource> getResourcesByIdAndTags(long userId, List<Long> tagIds)
 			throws DatabaseTinyException {
 		if (log.isTraceEnabled()) {
 			log.trace("Get resources by id and tags; user id: {}, tag ids: {}",
 					userId, tagIds == null ? null : tagIds.toString());
 		}
 		if (!userDao.exists(userId)) {
 			log.debug("Database exception: can't get resources for nonexistent user");
 			throw new DatabaseTinyException(
 					"Database exception: can't get resources for nonexistent user");
 		}
 
 		List<Resource> resources = null;
 		if (tagIds == null) {
 			resources = resourceDao.getAllForUser(userId);
 		} else {
 			if (!tagDao.exists(userId, tagIds)) {
 				log.debug("Database exception: can't get resources for nonexistent tags");
 				throw new DatabaseTinyException(
 						"Database exception: can't get resources for nonexistent tags");
 			}
 			resources = resourceDao.getByUserIdAndTags(userId, tagIds);
 		}
 		for (Resource resource : resources) {
 			resource.setTags(resourceTagDao.get(resource.getId()));
 		}
 		return resources;
 	}
 
 	public Resource getResource(long userId, long resourceId)
 			throws DatabaseTinyException {
 		log.trace("Get resource; user id = {}, resource id = {}", userId,
 				resourceId);
 		if (!userDao.exists(userId)) {
 			throw new DatabaseTinyException(
 					"Can't get resource for nonexistent user");
 		}
 		Resource resource = null;
 		resource = resourceDao.get(userId, resourceId);
 		if (resource != null) {
 			resource.setTags(resourceTagDao.get(resource.getId()));
 		}
 		return resource;
 	}
 
 	/**
 	 * Get from database resources with specified <code>scheduleCode</code>
 	 * 
 	 * @param scheduleCode
 	 * @return
 	 */
 	public Set<Resource> getResourcesByScheduleCode(byte scheduleCode) {
 		log.trace("Get resources by shedule code; code: {}", scheduleCode);
 		// TODO check schedule code
 		Set<Resource> resources = resourceDao.getByscheduleCode(scheduleCode);
 		if (resources == null) {
 			return Collections.emptySet();
 		}
 		return resources;
 	}
 
 	public void deleteResource(Resource resource)
 			throws DatabaseSeriousException, DatabaseTinyException {
 		if (log.isTraceEnabled()) {
 			log.trace("Delete resource; user id: {}, resource id: {}",
 					resource.getUserId(), resource.getId());
 		}
 		if (!userDao.exists(resource.getUserId())) {
 			log.debug("Database exception: can't delete resource for nonexistant user");
 			throw new DatabaseTinyException(
 					"Database exception: can't delete resource for nonexistant user");
 		}
 		if (!resourceDao.delete(resource)) {
 			throw new DatabaseSeriousException("Resource deletion failed");
 		}
 		session.commit();
 	}
 
 	public void deleteResourcesByIdAndTags(long userId, List<Long> tagIds)
 			throws DatabaseTinyException, DatabaseSeriousException {
 		if (log.isTraceEnabled()) {
 			log.trace("Delete resource; user id: {}, tag ids: {}", userId,
 					tagIds == null ? "null" : tagIds.toString());
 		}
 		if (!userDao.exists(userId)) {
 			log.debug("Database exception: can't delete resource for nonexistant user");
 			throw new DatabaseTinyException(
 					"Database exception: can't delete resource for nonexistant user");
 		}
 		boolean result = false;
 		if (tagIds == null) {
 			result = resourceDao.deleteAll(userId);
 		} else {
 			if (!tagDao.exists(userId, tagIds)) {
 				log.debug("Database exception: can't get resources for nonexistent tags");
 				throw new DatabaseTinyException(
 						"Database exception: can't get resources for nonexistent tags");
 			}
 			result = resourceDao.deleteByTags(userId, tagIds);
 		}
 		if (!result) {
 			throw new DatabaseSeriousException("Resource delition failed");
 		}
 		session.commit();
 	}
 
 	public void editResource(Resource resource) throws DatabaseTinyException,
 			DatabaseSeriousException {
 		if (log.isTraceEnabled()) {
 			log.trace("Edit resource; resource = {}", resource.toString());
 		}
		if (!userDao.exists(resource.getUserId())) {
 			throw new DatabaseTinyException(
 					"Can't edit resource for nonexistent user");
 		}
 		Resource savedResource = resourceDao.get(resource.getUserId(),
 				resource.getId());
 		if (savedResource == null) {
 			log.debug("Database exception: not found resource for edit");
 			throw (new DatabaseTinyException(
 					"Database exception: not found resource for edit"));
 		}
 		if (!savedResource.getUrl().equals(resource.getUrl())) {
 			resourceDao.updateAfterCheck(resource.getId(),
 					getNewHashCode(resource));
 		}
		if (!tagDao.exists(resource.getUserId(), resource.getTags())) {
 			log.debug("Database exception: can't assign nonexistant tags to resource");
 			throw new DatabaseTinyException(
 					"Database exception: can't assign nonexistant tags to resource");
 		}
 		if (!resourceDao.edit(resource)) {
 			throw new DatabaseSeriousException("Resource edition failed");
 		}
 		if (!resourceTagDao.delete(resource.getId())) {
 			throw new DatabaseSeriousException("Relation delition failed");
 		}
 		if (resource.getTags() != null) {
 			for (Long tagId : resource.getTags()) {
 				if (!resourceTagDao.add(resource.getId(), tagId)) {
 					throw new DatabaseSeriousException(
 							"Relation addition failed");
 				}
 			}
 		}
 		session.commit();
 	}
 
 	/**
 	 * Return all tags for specified user.
 	 * 
 	 * @param userId
 	 * @return all tags for user with <code>userId</code>
 	 * @throws DatabaseTinyException
 	 */
 	public Set<Tag> getTags(long userId) throws DatabaseTinyException {
 		log.trace("Get tags for user with user id: {}", userId);
 		if (!userDao.exists(userId)) {
 			throw new DatabaseTinyException(
 					"Can't get tags for nonexistent user");
 		}
 		return tagDao.get(userId);
 	}
 
 	/**
 	 * Update (in database) hash for resource with <code>resourceId</code>.
 	 * 
 	 * @param resourceId
 	 *            resource, which hash will be overridden
 	 * @param newHash
 	 *            new hash value
 	 * @throws DatabaseSeriousException
 	 */
 	public void updateResourceHash(long resourceId, int newHash)
 			throws DatabaseSeriousException {
 		log.trace("Update resource hash; resource id: {}, new hash: {}",
 				resourceId, newHash);
 		if (!resourceDao.exists(resourceId)) {
 			// TODO What can i dooo?
 			throw new DatabaseSeriousException(
 					"Can't update hash for nonexistent resource");
 		}
 		// Don't forget - hash will be update only with 'true' result
 		if (!resourceDao.updateAfterCheck(resourceId, newHash)) {
 			throw new DatabaseSeriousException("Hash updating failed");
 		}
 		session.commit();
 	}
 
 	/**
 	 * Add tag with specified name and user id to database
 	 * 
 	 * @param userId
 	 * @param tagName
 	 * @return true, if success, false otherwise
 	 * @throws DatabaseTinyException
 	 * @throws DatabaseSeriousException
 	 */
 	public void addTag(Tag tag) throws DatabaseTinyException,
 			DatabaseSeriousException {
 		if (!userDao.exists(tag.getUserId())) {
 			throw new DatabaseTinyException("Can't add tag to nonexistent user");
 		}
 		if (!tagDao.add(tag)) {
 			throw new DatabaseSeriousException("Tag addition failed");
 		}
 		session.commit();
 	}
 
 	public void editTag(Tag tag) throws DatabaseTinyException,
 			DatabaseSeriousException {
 		if (!userDao.exists(tag.getUserId())) {
 			throw new DatabaseTinyException(
 					"Can't edit tag of nonexistent user");
 		}
 		if (!tagDao.edit(tag)) {
 			throw new DatabaseSeriousException("Tag edition failed");
 		}
 		session.commit();
 	}
 
 	public void deleteTag(Tag tag) throws DatabaseTinyException,
 			DatabaseSeriousException {
 		if (!userDao.exists(tag.getUserId())) {
 			throw new DatabaseTinyException(
 					"Can't delete tag of nonexistent user");
 		}
 		if (!tagDao.delete(tag)) {
 			throw new DatabaseSeriousException("Tag delition failed");
 		}
 		session.commit();
 	}
 
 	public void deleteAllData() {
 		session.getMapper(UserMapper.class).deleteAll();
 		session.commit();
 	}
 
 }
