 package cl.own.usi.dao.impl.mongo;
 
 import static cl.own.usi.dao.impl.mongo.DaoHelper.scoreField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.userIdField;
 import static cl.own.usi.dao.impl.mongo.DaoHelper.usersCollection;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import cl.own.usi.dao.ScoreDAO;
 import cl.own.usi.model.User;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 @Repository
 public class ScoreDAOMongoImpl implements ScoreDAO {
 
 	@Autowired
 	DB db;
 
 	private Logger logger = LoggerFactory.getLogger(this.getClass());
 
 	// TODO is there more field to order by ? what if they have the same score ?
 	private static DBObject orderBy = new BasicDBObject()
			.append(scoreField, -1);
 
 	public void updateScore(User user, int newScore) {
 
 		DBCollection dbUsers = db.getCollection(usersCollection);
 
 		DBObject dbUserId = new BasicDBObject();
 		dbUserId.put(userIdField, user.getUserId());
 
 		DBObject dbScore = new BasicDBObject();
 		dbScore.put(scoreField, newScore);
 		DBObject dbSetScore = new BasicDBObject();
 		dbSetScore.put("$set", dbScore);
 
 		DBObject dbUser = dbUsers.findAndModify(dbUserId, dbSetScore);
 
 		logger.debug("update score of user:" + userIdField + "to :" + newScore
 				+ " actual score set in DB:" + dbUser.get(scoreField));
 	}
 
 	private List<User> getUsers(DBObject query, DBObject querySubset,
 			int expectedSize) {
 		DBCollection dbUsers = db.getCollection(usersCollection);
 
 		DBCursor dbCursor = dbUsers.find(query, querySubset).sort(orderBy);
 
 		List<User> users = new ArrayList<User>();
 		while (dbCursor.hasNext()) {
 			DBObject dbUser = dbCursor.next();
 			User user = DaoHelper.fromDBObject(dbUser);
 			users.add(user);
 		}
 		return users;
 	}
 
 	@Override
 	public List<User> getTop(int limit) {
 		// TODO set the fields we want, take everything for now
 		DBObject query = new BasicDBObject();
 
 		DBObject subset = new BasicDBObject();
 		subset.put("$slice", limit);
 		DBObject querySubset = new BasicDBObject();
 		querySubset.put("comment", subset);
 
 		List<User> users = getUsers(query, querySubset, limit);
 
 		logger.debug("get the " + limit + " top scores : " + users);
 
 		return users;
 	}
 
 	@Override
 	public List<User> getBefore(User user, int limit) {

		// TODO do we mean > or >=

 		DBObject criteria = new BasicDBObject();
 		criteria.put("$gt", user.getScore());
 		DBObject query = new BasicDBObject();
 		query.put(scoreField, criteria);
 
 		DBObject subset = new BasicDBObject();
 		subset.put("$slice", -limit);
 		DBObject querySubset = new BasicDBObject();
 		querySubset.put("comment", subset);
 
 		List<User> users = getUsers(query, querySubset, limit);
 
 		logger.debug("get the " + limit + " users before " + user.getUserId()
 				+ " :  " + users);
 
 		return users;
 	}
 
 	@Override
 	public List<User> getAfter(User user, int limit) {

		// TODO do we mean < or <=

 		DBObject criteria = new BasicDBObject();
		criteria.put("$lte", user.getScore());
 		DBObject query = new BasicDBObject();
 		query.put(scoreField, criteria);
 
 		DBObject subset = new BasicDBObject();
 		subset.put("$slice", limit);
 		DBObject querySubset = new BasicDBObject();
 		querySubset.put("comment", subset);
 
 		List<User> users = getUsers(query, querySubset, limit);
 
 		logger.debug("get the " + limit + " users after " + user.getUserId()
 				+ " :  " + users);
 
 		return users;
 	}
 
 	@Override
 	public int getUserBonus(User user) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void setUserBonus(User user, int newBonus) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void flushUsers() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
