 package cl.own.usi.dao.impl.mongo;
 
 import static cl.own.usi.dao.impl.mongo.DaoHelper.answerNumberField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.answersField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.emailField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.isLoggedField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.passwordField;
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
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.WriteResult;
 
 @Repository
 public class UserDAOMongoImpl implements UserDAO {
 
 	@Autowired
 	DB db;
 
 	private Logger logger = LoggerFactory.getLogger(this.getClass());
 
	private static DBObject userIdIndex = new BasicDBObject("userId", 1);
	private static DBObject credentialsIndex = new BasicDBObject("email", 1)
 			.append("password", 1);
 
	
	
 
 	@Override
 	public boolean insertUser(final User user) {
 		DBCollection dbUsers = db.getCollection(usersCollection);
 
 		// the driver keeps a cache of the added index
 		dbUsers.ensureIndex(userIdIndex, "userIdIndex", true);
 		dbUsers.ensureIndex(credentialsIndex, "credentialsIndex", false);
 
 		DBObject dbUser = DaoHelper.toDBObject(user);
 
 		WriteResult wr = dbUsers.insert(dbUser);
 		String error = wr.getError();
 
 		// E11000 -> duplicate key
 		if (StringUtils.hasText(error) && error.indexOf("E11000") == 0) {
 			logger.debug("user " + user.getEmail()
 					+ " was already in the collection, insertion aborted");
 			return false;
 		} else {
 			logger.debug("user " + user.getEmail()
 					+ " was successfully inserted");
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
 			logger.debug("fetching userId=" + userId + " and isLogged="
 					+ dbUser.get(isLoggedField));
 
 			// The index is only on the id, isLogged can't be part of the query
 			return (Boolean) dbUser.get(isLoggedField) ? DaoHelper.fromDBObject(dbUser)
 					: null;
 		} else {
 			logger.debug("fetching userId=" + userId
 					+ " is impossible, not in db");
 			return null;
 		}
 	}
 
 	@Override
 	public String login(final String email, final String password) {
 		DBCollection dbUsers = db.getCollection(usersCollection);
 
 		DBObject dbCredentials = new BasicDBObject();
 		dbCredentials.put(emailField, email);
 		dbCredentials.put(passwordField, password);
 
 		DBObject dblogin = new BasicDBObject();
 		dblogin.put(isLoggedField, true);
 		DBObject dbSetlogin = new BasicDBObject();
 		dbSetlogin.put("$set", dblogin);
 
 		DBObject dbUser = dbUsers.findAndModify(dbCredentials, dbSetlogin);
 
 		if (dbUser != null) {
 			logger.debug("login sucessful for " + email + "/" + password
 					+ "->userId=" + dbUser.get("userId"));
 
 			return (String) dbUser.get("userId");
 		} else {
 			logger.debug("login failed for " + email + "/" + password);
 
 			return null;
 		}
 	}
 
 	@Override
 	public void logout(final String userId) {
 		DBCollection dbUsers = db.getCollection(usersCollection);
 
 		DBObject dbUser = new BasicDBObject();
 		dbUser.put(userIdField, userId);
 
 		DBObject dblogout = new BasicDBObject();
 		dblogout.put(isLoggedField, Boolean.FALSE);
 
 		dbUsers.findAndModify(dbUser, dblogout);
 	}
 
 	@Override
 	public void insertRequest(final String userId, final int questionNumber) {
 
 		// TODO finish
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
 
 		dbUsers.findAndModify(dbUserId, dbPushAnswers);
 
 		logger.debug("answer inserted, "
 				+ ToStringBuilder.reflectionToString(answer));
 
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
 
 				logger.debug("fetching answers for userId=" + userId + "  "
 						+ answers.toString());
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
 		// TODO Auto-generated method stub
 
 	}
 
 }
