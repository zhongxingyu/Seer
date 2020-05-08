 package cl.own.usi.dao.impl.mongo;
 
 import static cl.own.usi.dao.impl.mongo.DaoHelper.answerNumberField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.answersField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.isLoggedField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.orderByNames;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.orderByScoreAndNames;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.questionNumberField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.userIdField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.usersCollection;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.util.StringUtils;
 
 import cl.own.usi.dao.UserDAO;
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
 public class UserDAOMongoImpl implements UserDAO {
 
 	@Autowired
 	private DB db;
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(UserDAOMongoImpl.class);
 
 	private static DBObject userIdIndex = new BasicDBObject(userIdField, 1);
 
 	@Override
 	public boolean insertUser(final User user) {
 		DBCollection dbUsers = db.getCollection(usersCollection);
 
 		DBObject dbUser = DaoHelper.toDBObject(user);
 
 		WriteResult wr = dbUsers.insert(dbUser);
 		String error = wr.getError();
 
 		// E11000 -> duplicate key
		if (StringUtils.hasText(error)) {
			if (error.indexOf("E11000") == 0) {
				LOGGER.info(
						"user {} was already in the collection, insertion aborted",
						user.getEmail());
				return false;
			} else {
				LOGGER.info("error when inserting user {}, error code={}",
						user.getEmail(), error);
				return false;
			}
 		} else {
 			LOGGER.debug("user {} was successfully inserted", user.getEmail());
 			return true;
 		}
 	}
 
 	@Override
 	public User getUserById(final String userId) {
 		DBCollection dbUsers = db.getCollection(usersCollection);
 
 		DBObject dbId = new BasicDBObject();
 		dbId.put(userIdField, userId);
 
 		DBObject dbUser = dbUsers.findOne(dbId);
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
 	public String login(final String email, final String password) {
 
 		final String userId = DaoHelper.generateUserId(email);
 		final User user = getUserById(userId);
 
 		if (user != null && user.getPassword().equals(password)) {
 			// The user has been correction authenticated
 			DBCollection dbUsers = db.getCollection(usersCollection);
 
 			DBObject dbId = new BasicDBObject();
 			dbId.put(userIdField, userId);
 
 			DBObject dblogin = new BasicDBObject();
 			dblogin.put(isLoggedField, true);
 			DBObject dbSetlogin = new BasicDBObject();
 			dbSetlogin.put("$set", dblogin);
 
 			dbUsers.update(dbId, dbSetlogin);
 
 			LOGGER.debug("login sucessful for {}/{}->userId={}", new Object[] {
 					email, password, userId });
 
 			return userId;
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
 
 		DBCollection dbUsers = db.getCollection(usersCollection);
 
 		DBObject dbUserId = new BasicDBObject();
 		dbUserId.put(userIdField, answer.getUserId());
 
 		DBObject dbAnswer = new BasicDBObject();
 		dbAnswer.put(answerNumberField, answer.getAnswerNumber());
 		dbAnswer.put(questionNumberField, answer.getQuestionNumber());
 
 		DBObject dbAnswers = new BasicDBObject();
 		dbAnswers.put(answersField, dbAnswer);
 
 		DBObject dbPushAnswers = new BasicDBObject();
 		dbPushAnswers.put("$push", dbAnswers);
 
 		dbUsers.update(dbUserId, dbPushAnswers);
 
 		LOGGER.debug("answer inserted, {}", answer);
 
 	}
 
 	@Override
 	public List<Answer> getAnswers(final String userId) {
 
 		DBCollection dbUsers = db.getCollection(usersCollection);
 
 		DBObject dbId = new BasicDBObject();
 		dbId.put(userIdField, userId);
 
 		DBObject dbUser = dbUsers.findOne(dbId);
 		if (dbUser != null) {
 
 			@SuppressWarnings("unchecked")
 			List<DBObject> dbAnswers = (List<DBObject>) dbUser
 					.get(answersField);
 
 			if (dbAnswers != null) {
 
 				List<Answer> answers = new ArrayList<Answer>(dbAnswers.size());
 				for (DBObject dbAnswer : dbAnswers) {
 					Answer answer = new Answer();
 					answer.setAnswerNumber((Integer) dbAnswer
 							.get(answerNumberField));
 					answer.setQuestionNumber((Integer) dbAnswer
 							.get(questionNumberField));
 					answer.setUserId(userId);
 					answers.add(answer);
 				}
 
 				LOGGER.debug("fetching answers for userId={} {}", userId,
 						answers);
 				return answers;
 			} else {
 				return Collections.emptyList();
 			}
 		} else {
 			return Collections.emptyList();
 		}
 	}
 
 	@Override
 	public void flushUsers() {
 		final DBCollection dbUsers = db.getCollection(usersCollection);
 		dbUsers.drop();
 
 		// TODO NIRE : all this code must be moved to a "setupWhatever"
 		// function. It is possible to create a game *without* flushing the
 		// users
 
 		final DBCollection newUsers = db.getCollection(usersCollection);
 
 		// the driver keeps a cache of the added index
 		newUsers.ensureIndex(userIdIndex, "userIdIndex", true);
 		newUsers.ensureIndex(orderByScoreAndNames, "orderByScoreAndNames",
 				false);
 		newUsers.ensureIndex(orderByNames, "orderByScoreAndNames", false);
 
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
 
 	@Override
 	public List<Answer> getAnswersByEmail(final String email) {
 
 		final String userId = DaoHelper.generateUserId(email);
 		return getAnswers(userId);
 	}
 }
