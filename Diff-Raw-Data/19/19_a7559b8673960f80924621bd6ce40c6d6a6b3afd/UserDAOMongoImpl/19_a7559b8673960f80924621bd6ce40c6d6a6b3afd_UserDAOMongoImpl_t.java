 package cl.own.usi.dao.impl.mongo;
 
 import static cl.own.usi.dao.impl.mongo.DaoHelper.bonusField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.isLoggedField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.namesEmailField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.orderByNames;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.orderByScore;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.orderByScoreNames;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.passwordField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.scoreField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.userIdField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.usersCollection;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.questionFieldPrefix;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.util.StringUtils;
 
 import cl.own.usi.dao.UserDAO;
 import cl.own.usi.exception.UserAlreadyLoggedException;
 import cl.own.usi.model.Answer;
 import cl.own.usi.model.User;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.CommandResult;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.MongoException;
 import com.mongodb.WriteResult;
 
 @Repository
 public class UserDAOMongoImpl implements UserDAO, InitializingBean {
 
 	@Autowired
 	private DB db;
 
 	private static final int INSERT_USER_RETRY = 3;
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(UserDAOMongoImpl.class);
 
 	private static final DBObject userIdIndex = new BasicDBObject(userIdField,
 			1);
 
 	private static final DBObject loginIdIndex = new BasicDBObject().append(
 			userIdField, 1).append(passwordField, 1);
 
 	private static final DBObject isLoggedIndex = new BasicDBObject().append(
 			isLoggedField, 1);
 
 	private static final DBObject userFieldsToFetch = new BasicDBObject()
 			.append(userIdField, 1).append(namesEmailField, 1)
 			.append(scoreField, 1).append(isLoggedField, 1)
 			.append(passwordField, 1).append(isLoggedField, 1)
 			.append(bonusField, 1);
 
 	private static final DBObject loginFieldsToFetch = new BasicDBObject()
 			.append(isLoggedField, 1);
 
 	@Override
 	public boolean insertUser(final User user) {
 		DBCollection dbUsers = db.getCollection(usersCollection);
 		// Ensure that it is strictly impossible to have twice the same user
 		dbUsers.ensureIndex(userIdIndex, "userIdIndex", true);
 
 		DBObject dbUser = DaoHelper.toDBObject(user);
 
 		// TODO FIXME please
 		final int numberOfQuestions = 20;
 
 		// Insert the questions directly at the beginning
 		for (int i = 1; i <= numberOfQuestions; ++i) {
 			dbUser.put(questionFieldPrefix + i, Integer.valueOf(-1));
 		}
 
 		for (int i = 0; i < INSERT_USER_RETRY; i++) {
 
 			WriteResult wr = dbUsers.insert(dbUser);
 			String error = wr.getError();
 
 			// E11000 -> duplicate key
 			if (StringUtils.hasText(error)) {
 				if (error.indexOf("E11000") == 0) {
 					LOGGER.info(
 							"user {} was already in the collection, insertion aborted",
 							user.getEmail());
 					return false;
 
 				} else if (isRetryableError(error)) {
 					LOGGER.info(
 							"retryable error when inserting user {}, error code={}, try={}, will retry",
 							new Object[] { user.getEmail(), error, i });
 
 				} else {
 					LOGGER.info(
 							"error when inserting user {}, error code={}, try={}",
 							new Object[] { user.getEmail(), error, i });
 					return false;
 				}
 			} else {
 				LOGGER.debug("user {} was successfully inserted",
 						user.getEmail());
 				return true;
 			}
 		}
 
 		LOGGER.warn("Failure to insert user {} after {} retry!",
 				user.getEmail(), INSERT_USER_RETRY);
 		return false;
 	}
 
 	@Override
 	public User getUserById(final String userId) {
 		DBCollection dbUsers = db.getCollection(usersCollection);
 
 		DBObject dbId = new BasicDBObject();
 		dbId.put(userIdField, userId);
 
 		DBObject dbUser = dbUsers.findOne(dbId, userFieldsToFetch);
 		if (dbUser != null) {
 			LOGGER.debug("fetching userId={} and isLogged={}", userId,
 					dbUser.get(isLoggedField));
 
 			return DaoHelper.fromDBObject(dbUser);
 		} else {
 			LOGGER.debug("fetching userId={} is impossible, not in db", userId);
 			return null;
 		}
 	}
 
 	@Override
 	public User getUserByEmail(String email) {
 		final String userId = DaoHelper.generateUserId(email);
 		return getUserById(userId);
 	}
 
 	@Override
 	public String login(final String email, final String password)
 			throws UserAlreadyLoggedException {
 
 		final DBCollection dbUsers = db.getCollection(usersCollection);
 
 		final String userId = DaoHelper.generateUserId(email);
 
 		// Get the current score and bonus
 		DBObject dbId = new BasicDBObject();
 		dbId.put(userIdField, userId);
 		dbId.put(passwordField, password);
 
 		// The user has been correction authenticated
 		DBObject dblogin = new BasicDBObject();
 		dblogin.put(isLoggedField, true);
 		DBObject dbSetlogin = new BasicDBObject();
 		dbSetlogin.put("$set", dblogin);
 
 		DBObject dbUser = dbUsers.findAndModify(dbId, loginFieldsToFetch, null,
 				false, dbSetlogin, false, false);
 
 		if (dbUser != null) {
 
 			Boolean isLogged = (Boolean) dbUser.get(isLoggedField);
 			if (isLogged != null && !isLogged.booleanValue()) {
 				LOGGER.debug("login sucessful for {}/{}->userId={}",
 						new Object[] { email, password, userId });
 				return userId;
 			} else {
 				LOGGER.debug("user already logged in {}/{}", email, password);
 				throw new UserAlreadyLoggedException();
 			}
 		} else {
 
 			LOGGER.debug("login failed for {}/{}", email, password);
 			return null;
 		}
 	}
 
 	@Override
 	public void logout(final String userId) {
 		DBCollection dbUsers = db.getCollection(usersCollection);
 
 		DBObject dbUser = new BasicDBObject();
 		dbUser.put(userIdField, userId);
 
 		DBObject dblogout = new BasicDBObject();
 		dblogout.put(isLoggedField, false);
 		DBObject dbSetlogout = new BasicDBObject();
 		dblogout.put("$set", dblogout);
 
 		dbUsers.update(dbUser, dbSetlogout);
 	}
 
 	@Override
 	public void insertRequest(final String userId, final int questionNumber) {
 		// not needed anymore
 	}
 
 	@Override
 	public void insertAnswer(final Answer answer) {
 		// Nothing to do, all the answers have been pre-inserted
 	}
 
 	@Override
 	public List<Answer> getAnswersByEmail(final String email) {
 
 		final String userId = DaoHelper.generateUserId(email);
 		return getAnswers(userId);
 	}
 
 	@Override
 	public List<Answer> getAnswers(final String userId) {
 
 		DBCollection dbUsers = db.getCollection(usersCollection);
 
 		DBObject dbId = new BasicDBObject();
 		dbId.put(userIdField, userId);
 
 		DBObject dbUser = dbUsers.findOne(dbId);
 		if (dbUser != null) {
 			List<Answer> answers = new ArrayList<Answer>(100);
 			for (String key : dbUser.keySet()) {
 				if (key != null && key.startsWith(questionFieldPrefix)) {
 
 					String keyQuestion = key.substring(questionFieldPrefix
 							.length());
 					Integer questionNumber = Integer.valueOf(keyQuestion);
 
 					final int answerNumber = (Integer) dbUser.get(key);
 
 					Answer answer = new Answer();
 					answer.setAnswerNumber(answerNumber > 0 ? answerNumber : 0);
 					answer.setQuestionNumber(questionNumber.intValue());
 					answer.setUserId(userId);
 					answers.add(answer);
 				}
 			}
 
 			Collections.sort(answers, new Comparator<Answer>() {
 				@Override
 				public int compare(Answer lhs, Answer rhs) {
 					return Integer.valueOf(lhs.getAnswerNumber()).compareTo(
 							rhs.getAnswerNumber());
 				}
 			});
 
 			LOGGER.debug("fetching answers for userId={} {}", userId, answers);
 			return answers;
 		} else {
 			return Collections.emptyList();
 		}
 	}
 
 	private static final DBObject dbFindAll = new BasicDBObject();
 	private static final DBObject dbFlushUpdate = new BasicDBObject();
 
 	static {
 		final DBObject dbUpdate = new BasicDBObject();
 		// Reset the login status
 		dbUpdate.put(isLoggedField, Boolean.FALSE);
 		// Reset the scores
 		dbUpdate.put(scoreField, Integer.valueOf(0));
 		dbUpdate.put(bonusField, Integer.valueOf(0));
 		// TODO
 		final int numberOfQuestions = 20;
 		// Purge the answers
 		for (int i = 1; i <= numberOfQuestions; ++i) {
 			dbUpdate.put(questionFieldPrefix + i, Integer.valueOf(-1));
 		}
 
 		dbFlushUpdate.put("$set", dbUpdate);
 	}
 
 	@Override
 	public void flushUsers() {
 		final DBCollection dbUsers = db.getCollection(usersCollection);
 
 		final boolean upsert = false;
 		final boolean multi = true;
 
 		// Flush all users
 		WriteResult wr = dbUsers
 				.update(dbFindAll, dbFlushUpdate, upsert, multi);
 
 		String error = wr.getError();
 		if (StringUtils.hasText(error)) {
 			LOGGER.info("the users collection has been flushed but encountered an error : "
 					+ error);
 		} else {
 			LOGGER.info("the users collection has been successfully flushed");
 		}
 	}
 
 	private boolean isRetryableError(final String mongoError) {
 		return (mongoError.indexOf("10429") == 0 || mongoError
 				.indexOf("setShardVersion") == 0);
 	}
 
 	@Override
 	public void gameCreated() {
 		initializeSharding();
 		ensureIndexes();
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		initializeSharding();
 		ensureIndexes();
 	}
 
 	private void ensureIndexes() {
 		final DBCollection dbUsers = db.getCollection(usersCollection);
 
 		// Setup all the appropriate indexes
 		dbUsers.ensureIndex(userIdIndex, "userIdIndex", true);
 		dbUsers.ensureIndex(loginIdIndex, "loginIdIndex", false);
 		dbUsers.ensureIndex(isLoggedIndex, "isLoggedIndex", false);
 		dbUsers.ensureIndex(orderByScore, "orderByScore", false);
 		dbUsers.ensureIndex(orderByNames, "orderByNames", false);
 		dbUsers.ensureIndex(orderByScoreNames, "orderByScoreNames", false);
 	}
 
 	private void initializeSharding() {
 
 		LOGGER.info("MongoDB : enable sharding settings");
 
 		// Enable sharding for the newly created collection
 		final DB adminDb = db.getSisterDB("admin");
 		try {
 			final BasicDBObject enableSharding = new BasicDBObject(
 					"enablesharding", db.getName());
 			final CommandResult command = adminDb.command(enableSharding);
 			LOGGER.info("Enable sharding: {}, ok:{}, error: {}", new Object[] {
 					ToStringBuilder.reflectionToString(command), command.ok(),
 					command.getErrorMessage() });
 
 		} catch (MongoException e) {
 			LOGGER.warn(
 					"Exception while enabling sharding, probably already on", e);
 		}
 
 		try {
 			final BasicDBObject shard = new BasicDBObject("shardcollection",
 					db.getName() + "." + usersCollection);
 			final BasicDBObject shardKey = new BasicDBObject(userIdField, 1);
 			shard.put("key", shardKey);
 
 			final CommandResult command = adminDb.command(shard);
 			LOGGER.debug("Shard users: {}, ok:{}, error: {}", new Object[] {
 					ToStringBuilder.reflectionToString(command), command.ok(),
 					command.getErrorMessage() });
 		} catch (MongoException e) {
 			LOGGER.warn("Exception while trying to shard 'users' collection", e);
 		}
 	}
 
 }
