 package cl.own.usi.dao.impl.cassandra;
 
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.emailColumn;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.dbKeyspace;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.answersColumnFamily;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.firstnameColumn;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.isLoggedColumn;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.lastnameColumn;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.passwordColumn;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.usersColumnFamily;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.scoreColumn;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.bonusesColumnFamily;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.ranksColumnFamily;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.locks.ReentrantLock;
 
 import me.prettyprint.cassandra.model.AllOneConsistencyLevelPolicy;
 import me.prettyprint.cassandra.model.QuorumAllConsistencyLevelPolicy;
 import me.prettyprint.cassandra.serializers.BooleanSerializer;
 import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
 import me.prettyprint.cassandra.serializers.IntegerSerializer;
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.hector.api.Cluster;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.beans.ColumnSlice;
 import me.prettyprint.hector.api.beans.HColumn;
 import me.prettyprint.hector.api.beans.OrderedRows;
 import me.prettyprint.hector.api.beans.Row;
 import me.prettyprint.hector.api.beans.Rows;
 import me.prettyprint.hector.api.exceptions.HectorException;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.mutation.MutationResult;
 import me.prettyprint.hector.api.mutation.Mutator;
 import me.prettyprint.hector.api.query.MultigetSliceQuery;
 import me.prettyprint.hector.api.query.QueryResult;
 import me.prettyprint.hector.api.query.RangeSlicesQuery;
 import me.prettyprint.hector.api.query.SliceQuery;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import cl.own.usi.dao.ScoreDAO;
 import cl.own.usi.dao.UserDAO;
 import cl.own.usi.model.Answer;
 import cl.own.usi.model.User;
 
 @Repository
 public class AllDAOCassandraImpl implements ScoreDAO, UserDAO, InitializingBean {
 
 	private List<Integer> orderedScores = Collections.<Integer> emptyList();
 	private List<Integer> reverseOrderedScores = Collections
 			.<Integer> emptyList();
 	private List<User> top100 = Collections.<User> emptyList();
 
 	private final ReentrantLock scoresComputationLock = new ReentrantLock();
 
 	@Autowired
 	private Cluster cluster;
 	
 	private Keyspace consistencyOneKeyspace;
 	private Keyspace consistencyQuorumKeyspace;
 
 	private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
 	private final StringSerializer ss = StringSerializer.get();
 	private final ByteBufferSerializer bbs = ByteBufferSerializer.get();
 	private final IntegerSerializer is = IntegerSerializer.get();
 	private final BooleanSerializer bs = BooleanSerializer.get();
 
 	@Override
 	public List<User> getTop(int limit) {
 
 		if (limit == 100) {
 			return top100;
 		} else {
 			return computeTop(limit);
 		}
 
 	}
 
 	@Override
 	public List<User> getBefore(User user, int limit) {
 
 		ensureOrderedScoresLoaded();
 
 		String userKey = generateRankedUserKey(user);
 
 		List<User> users = findRankedUsers(limit, user.getScore(),
 				reverseOrderedScores, false, userKey);
 
 		return users;
 	}
 
 	private List<User> loadUsers(List<String> userIds) {
 
 		if (userIds == null || userIds.isEmpty()) {
 			return Collections.<User> emptyList();
 		}
 
 		MultigetSliceQuery<String, String, ByteBuffer> multigetSliceQuery = HFactory
 				.createMultigetSliceQuery(consistencyOneKeyspace, ss, ss, bbs);
 
 		multigetSliceQuery.setColumnFamily(usersColumnFamily);
 		multigetSliceQuery.setColumnNames(emailColumn, firstnameColumn,
 				lastnameColumn, passwordColumn, scoreColumn);
 		multigetSliceQuery.setKeys(userIds);
 
 		QueryResult<Rows<String, String, ByteBuffer>> queryResult = multigetSliceQuery
 				.execute();
 
 		List<User> users = new ArrayList<User>(userIds.size());
 		for (String userId : userIds) {
 			Row<String, String, ByteBuffer> row = queryResult.get().getByKey(
 					userId);
 			if (row != null) {
 				users.add(toUser(userId, row.getColumnSlice()));
 			}
 		}
 		return users;
 	}
 
 	@Override
 	public List<User> getAfter(User user, int limit) {
 
 		ensureOrderedScoresLoaded();
 
 		String userKey = generateRankedUserKey(user);
 
 		List<User> users = findRankedUsers(limit, user.getScore(),
 				orderedScores, true, userKey);
 
 		return users;
 	}
 
 	@Override
 	public int setBadAnswer(String userId, int questionNumber) {
 
 		User user = getUserById(userId);
 		if (user != null) {
 			Mutator<String> mutator = HFactory.createMutator(consistencyOneKeyspace,
 					StringSerializer.get());
 			mutator.addInsertion(userId, bonusesColumnFamily, HFactory
 					.createColumn(questionNumber, Boolean.FALSE, is, bs));
 			mutator.execute();
 			return user.getScore();
 		} else {
 			return 0;
 		}
 	}
 
 	@Override
 	public int setGoodAnswer(String userId, int questionNumber,
 			int questionValue) {
 
 		User user = getUserById(userId);
 
 		if (user != null) {
 
 			// int oldScore = user.getScore();
 
 			SliceQuery<String, Integer, Boolean> q = HFactory.createSliceQuery(
 					consistencyOneKeyspace, ss, is, bs);
 			q.setKey(userId);
 			q.setColumnFamily(bonusesColumnFamily);
 			q.setRange(questionNumber - 1, 0, true, 20);
 
 			QueryResult<ColumnSlice<Integer, Boolean>> result = q.execute();
 
 			ColumnSlice<Integer, Boolean> columnSlice = result.get();
 
 			int newBonus = 0;
 			if (columnSlice.getColumns() != null
 					&& !columnSlice.getColumns().isEmpty()) {
 				List<HColumn<Integer, Boolean>> previousAnswers = columnSlice
 						.getColumns();
 				int searchedQuestion = questionNumber - 1;
 				for (HColumn<Integer, Boolean> previousAnswer : previousAnswers) {
 					if (previousAnswer.getName().compareTo(searchedQuestion) < 0) {
 						break;
 					} else if (previousAnswer.getName().compareTo(
 							searchedQuestion) == 0) {
 						if (previousAnswer.getValue()) {
 							newBonus++;
 						} else {
 							break;
 						}
 					}
 					searchedQuestion--;
 				}
 			}
 
 			int newScore = user.getScore() + questionValue + newBonus;
 
 			Mutator<String> mutator = HFactory.createMutator(consistencyOneKeyspace,
 					StringSerializer.get());
 			mutator.addInsertion(userId, bonusesColumnFamily,
 					HFactory.createColumn(questionNumber, Boolean.TRUE, is, bs));
 
 			mutator.addInsertion(userId, usersColumnFamily,
 					HFactory.createColumn(scoreColumn, newScore, ss, is));
 
 			mutator.execute();
 
 			// String userKey = generateRankedUserKey(user);
 			// Mutator<Integer> rankMutator = HFactory.createMutator(keyspace,
 			// is);
 			// rankMutator.addDeletion(oldScore, ranksColumnFamily, userKey,
 			// ss);
 			// rankMutator.addInsertion(newScore, ranksColumnFamily,
 			// HFactory.createColumn(userKey, user.getUserId(), ss, ss));
 			// rankMutator.execute();
 			return newScore;
 
 		} else {
 			return 0;
 		}
 
 	}
 
 	private String generateRankedUserKey(User user) {
 		return user.getLastname() + user.getFirstname() + user.getEmail();
 	}
 
 	@Override
 	public boolean insertUser(User user) {
 
 		String userID = CassandraHelper.generateUserId(user);
 
 		try {
 			if (isEmailInDatabase(userID)) {
 				logger.debug(
 						"user {} was already in the database, insertion aborted",
 						user.getEmail());
 				return false;
 			}
 
 			Mutator<String> mutator = HFactory.createMutator(consistencyQuorumKeyspace,
 					StringSerializer.get());
 
 			// Add the user in the CF Users
 			mutator.addInsertion(userID, usersColumnFamily,
 					HFactory.createColumn(emailColumn, user.getEmail(), ss, ss));
 			mutator.addInsertion(userID, usersColumnFamily, HFactory
 					.createColumn(firstnameColumn, user.getFirstname(), ss, ss));
 			mutator.addInsertion(userID, usersColumnFamily, HFactory
 					.createColumn(lastnameColumn, user.getLastname(), ss, ss));
 			mutator.addInsertion(userID, usersColumnFamily, HFactory
 					.createColumn(passwordColumn, user.getPassword(), ss, ss));
 			mutator.addInsertion(userID, usersColumnFamily,
 					HFactory.createColumn(scoreColumn, user.getScore(), ss, is));
 			mutator.addInsertion(userID, usersColumnFamily, HFactory
 					.createColumn(isLoggedColumn, Boolean.FALSE, ss, bs));
 
 			mutator.execute();
 			logger.debug("user {} was successfully inserted", user.getEmail());
 		} catch (HectorException e) {
 			logger.error("An error occured while inserting user", e);
 			return false;
 		}
 		return true;
 	}
 
 	private boolean isEmailInDatabase(String userId) {
 		SliceQuery<String, String, ByteBuffer> q = HFactory.createSliceQuery(
 				consistencyQuorumKeyspace, ss, ss, bbs);
 		q.setKey(userId);
 		q.setColumnFamily(usersColumnFamily);
 		q.setColumnNames(emailColumn);
 
 		QueryResult<ColumnSlice<String, ByteBuffer>> result = q.execute();
 		ColumnSlice<String, ByteBuffer> cs = result.get();
 
 		return cs.getColumns().size() != 0;
 	}
 
 	@Override
 	public User getUserById(String userId) {
 
 		if (userId == null) {
 			return null;
 		}
 
 		SliceQuery<String, String, ByteBuffer> q = HFactory.createSliceQuery(
 				consistencyOneKeyspace, ss, ss, bbs);
 		q.setKey(userId);
 		q.setColumnFamily(usersColumnFamily);
 		q.setColumnNames(emailColumn, firstnameColumn, lastnameColumn,
 				passwordColumn, scoreColumn);
 
 		QueryResult<ColumnSlice<String, ByteBuffer>> result = q.execute();
 		ColumnSlice<String, ByteBuffer> cs = result.get();
 
 		if (cs.getColumns().size() != 0) {
 			User user = toUser(userId, cs);
 			return user;
 		} else {
 			logger.debug("fetching userId={} is impossible, not in db", userId);
 			return null;
 		}
 	}
 
 	private User toUser(String userId, ColumnSlice<String, ByteBuffer> cs) {
 		User user = new User();
 		user.setEmail(ss.fromByteBuffer(cs.getColumnByName(emailColumn)
 				.getValue()));
 		user.setFirstname(ss.fromByteBuffer(cs.getColumnByName(firstnameColumn)
 				.getValue()));
 		user.setLastname(ss.fromByteBuffer(cs.getColumnByName(lastnameColumn)
 				.getValue()));
 		user.setPassword(ss.fromByteBuffer(cs.getColumnByName(passwordColumn)
 				.getValue()));
 		user.setScore(is.fromByteBuffer(cs.getColumnByName(scoreColumn)
 				.getValue()));
 		user.setUserId(userId);
 		return user;
 	}
 
 	@Override
 	public void insertRequest(String userId, int questionNumber) {
 		// not needed anymore.
 	}
 
 	@Override
 	public void insertAnswer(Answer answer) {
 
 		logger.debug(
 				"insertAnswer ({}, {}, {})",
 				new Object[] { answer.getAnswerNumber(),
 						answer.getQuestionNumber(), answer.getUserId() });
 		try {
 			Mutator<String> mutator = HFactory.createMutator(consistencyOneKeyspace, ss);
 
 			mutator.addInsertion(
 					answer.getUserId(),
 					answersColumnFamily,
 					HFactory.createColumn(answer.getQuestionNumber(),
 							answer.getAnswerNumber(), is, is));
 
 			mutator.execute();
 			logger.debug("answer inserted, {}", answer);
 
 		} catch (HectorException e) {
 			logger.error("An error occured while inserting answer", e);
 		}
 	}
 
 	@Override
 	public List<Answer> getAnswers(String userId) {
 
 		SliceQuery<String, Integer, Integer> query = HFactory.createSliceQuery(
 				consistencyOneKeyspace, ss, is, is);
 		query.setColumnFamily(answersColumnFamily);
 		query.setKey(userId);
 		query.setRange(0, Integer.MAX_VALUE, false, 20); // max 20 questions.
 
 		QueryResult<ColumnSlice<Integer, Integer>> result = query.execute();
 		List<HColumn<Integer, Integer>> columns = result.get().getColumns();
 
 		if (columns.size() == 0) {
 			return Collections.emptyList();
 		}
 
 		List<Answer> answers = new ArrayList<Answer>(columns.size());
 		for (HColumn<Integer, Integer> column : columns) {
 			Answer answer = new Answer();
 			answer.setAnswerNumber(column.getValue());
 			answer.setQuestionNumber(column.getName());
 			answer.setUserId(userId);
 			answers.add(answer);
 		}
 
 		return answers;
 	}
 
 	@Override
 	public String login(String email, String password) {
 
 		String userId = CassandraHelper.generateUserId(email);
 
 		SliceQuery<String, String, ByteBuffer> q = HFactory.createSliceQuery(
 				consistencyOneKeyspace, ss, ss, bbs);
 		q.setKey(userId);
 		q.setColumnFamily(usersColumnFamily);
 		q.setColumnNames(passwordColumn, isLoggedColumn);
 
 		QueryResult<ColumnSlice<String, ByteBuffer>> result = q.execute();
 		ColumnSlice<String, ByteBuffer> cs = result.get();
 
 		if (cs.getColumns().size() != 0) {
 
 			String passwordFromDB = ss.fromByteBuffer(cs.getColumnByName(
 					passwordColumn).getValue());
 			Boolean isLogged = bs.fromByteBuffer(cs.getColumnByName(
 					isLoggedColumn).getValue());
 
 			if (password.equals(passwordFromDB) && !isLogged) {
 
 				Mutator<String> mutator = HFactory.createMutator(consistencyQuorumKeyspace,
 						StringSerializer.get());
 				mutator.addInsertion(
 						userId,
 						usersColumnFamily,
 						HFactory.createColumn(isLoggedColumn,
 								bs.toByteBuffer(Boolean.TRUE), ss, bbs));
 				mutator.execute();
 				logger.debug("login sucessful for {}, userId={}", email, userId);
 				return userId;
 			}
 		}
 		logger.debug("login failed for {}", email);
 		return null;
 	}
 
 	@Override
 	public void logout(String userId) {
 		SliceQuery<String, String, ByteBuffer> q = HFactory.createSliceQuery(
 				consistencyOneKeyspace, ss, ss, bbs);
 
 		q.setColumnFamily(usersColumnFamily);
 		q.setKey(userId);
 		q.setColumnNames(isLoggedColumn);
 
 		QueryResult<ColumnSlice<String, ByteBuffer>> result = q.execute();
 		ColumnSlice<String, ByteBuffer> cs = result.get();
 
 		if (cs.getColumns().size() != 0) {
 			Mutator<String> mutator = HFactory.createMutator(consistencyQuorumKeyspace, ss);
 			mutator.addInsertion(
 					userId,
 					usersColumnFamily,
 					HFactory.createColumn(isLoggedColumn,
 							bs.toByteBuffer(Boolean.FALSE), ss, bbs));
 			mutator.execute();
 			logger.debug("User {} successfully logout", userId);
 		}
 	}
 
 	@Override
 	public void flushUsers() {
 
 		String from = "";
 		int limit = 2;
 		boolean oneMoreIteration = true;
 		do {
 			RangeSlicesQuery<String, String, ByteBuffer> rangeSlicesQuery = HFactory
 					.createRangeSlicesQuery(consistencyOneKeyspace, ss, ss, bbs);
 
 			rangeSlicesQuery.setColumnFamily(usersColumnFamily);
 			rangeSlicesQuery.setKeys(from, "");
 			rangeSlicesQuery.setReturnKeysOnly();
 			rangeSlicesQuery.setRowCount(limit);
 
 			QueryResult<OrderedRows<String, String, ByteBuffer>> result = rangeSlicesQuery
 					.execute();
 			if (result.get().getCount() < limit) {
 				oneMoreIteration = false;
 			}
 
 			Iterator<Row<String, String, ByteBuffer>> iterator = result.get()
 					.iterator();
 			Mutator<String> mutator = HFactory.createMutator(consistencyOneKeyspace, ss);
 
 			while (iterator.hasNext()) {
 				Row<String, String, ByteBuffer> row = iterator.next();
 				String key = row.getKey();
 				mutator.addDeletion(key, usersColumnFamily);
 				mutator.addDeletion(key, answersColumnFamily);
 				mutator.addDeletion(key, bonusesColumnFamily);
 				from = key;
 			}
 			MutationResult mutationResult = mutator.execute();
 			logger.debug("Flush batch executed in {} ms",
 					mutationResult.getExecutionTimeMicro());
 
 		} while (oneMoreIteration);
 
 		// Remove all entries in ranks.
 		RangeSlicesQuery<Integer, String, String> rangeSliceQuery = HFactory
 				.createRangeSlicesQuery(consistencyOneKeyspace, is, ss, ss);
 		rangeSliceQuery.setColumnFamily(ranksColumnFamily);
 		rangeSliceQuery.setReturnKeysOnly();
 		QueryResult<OrderedRows<Integer, String, String>> result = rangeSliceQuery
 				.execute();
 		Iterator<Row<Integer, String, String>> iterator = result.get()
 				.iterator();
 		Mutator<Integer> mutator = HFactory.createMutator(consistencyOneKeyspace, is);
 		while (iterator.hasNext()) {
 			Row<Integer, String, String> row = iterator.next();
 			int key = row.getKey();
 			mutator.addDeletion(key, ranksColumnFamily);
 		}
 		mutator.execute();
 
 		orderedScores = Collections.<Integer> emptyList();
 		reverseOrderedScores = Collections.<Integer> emptyList();
 	}
 
 	@Override
 	public void computeRankings() {
 
 		insertRankings();
 
 		top100 = new ArrayList<User>(100);
 		top100 = computeTop(100);
 
 	}
 
 	private void insertRankings() {
 
 		int limit = 1000;
 		String start = "";
 
 		boolean oneMoreIteration = true;
 		do {
 			
 			RangeSlicesQuery<String, String, ByteBuffer> rangeSliceQuery = HFactory.createRangeSlicesQuery(consistencyOneKeyspace, ss, ss, bbs);
 			
 			rangeSliceQuery.setColumnFamily(usersColumnFamily);
 			rangeSliceQuery.setRange(start, "", false, limit);
 
 			QueryResult<OrderedRows<String, String, ByteBuffer>> result = rangeSliceQuery
 					.execute();
 
 			OrderedRows<String, String, ByteBuffer> rows = result.get();
 
 			if (rows.getCount() < limit) {
 				oneMoreIteration = false;
 			}
 			Iterator<Row<String, String, ByteBuffer>> iterator = rows.iterator();
 			
 			Mutator<Integer> mutator = HFactory.createMutator(consistencyOneKeyspace, is);
 			
 			while (iterator.hasNext()) {
 				Row<String, String, ByteBuffer> row = iterator.next();
 				if (row.getColumnSlice().getColumnByName(emailColumn) != null) {
 					User user = toUser(row.getKey(), row.getColumnSlice());
 					String userKey = generateRankedUserKey(user);
 					mutator.addInsertion(user.getScore(), ranksColumnFamily, HFactory.createColumn(userKey, user.getUserId(), ss, ss));
 				}
 			}
 
 			MutationResult mutationResult = mutator.execute();
 
 			logger.debug("Mutator executed in {} ms",
 					mutationResult.getExecutionTimeMicro());
 
 		} while (oneMoreIteration);
 
 	}
 
 	private void ensureOrderedScoresLoaded() {
 
 		if (orderedScores.isEmpty()) {
 			scoresComputationLock.lock();
 			if (orderedScores.isEmpty()) {
 				computeOrderedScores();
 			}
 			scoresComputationLock.unlock();
 		}
 	}
 
 	private void computeOrderedScores() {
 
 		RangeSlicesQuery<Integer, String, String> rangeSliceQuery = HFactory
				.createRangeSlicesQuery(consistencyOneKeyspace, is, ss, ss);
 
 		rangeSliceQuery.setColumnFamily(ranksColumnFamily);
 		rangeSliceQuery.setReturnKeysOnly();
 
 		QueryResult<OrderedRows<Integer, String, String>> result = rangeSliceQuery
 				.execute();
 
 		orderedScores = new ArrayList<Integer>(result.get().getCount());
 
 		Iterator<Row<Integer, String, String>> iterator = result.get()
 				.iterator();
 
 		while (iterator.hasNext()) {
 			Row<Integer, String, String> row = iterator.next();
 			int key = row.getKey();
 			orderedScores.add(key);
 		}
 
 		Collections.sort(orderedScores);
 		reverseOrderedScores = new ArrayList<Integer>(orderedScores);
 		Collections.sort(reverseOrderedScores, Collections.reverseOrder());
 
 	}
 
 	private List<User> computeTop(int limit) {
 
 		ensureOrderedScoresLoaded();
 
 		List<User> users = findRankedUsers(limit, null, reverseOrderedScores,
 				false, "");
 
 		return users;
 	}
 
 	List<User> findRankedUsers(int limit, Integer startScore,
 			List<Integer> orderedScores, boolean reverseOrder, String startKey) {
 
 		List<String> userIds = new ArrayList<String>(limit);
 
 		boolean scoreFound = false;
 		boolean first = true;
 		String start = startKey;
 		EXTERNALLOOP: for (Integer score : orderedScores) {
 			if (!scoreFound) {
 				if (startScore == null || score.equals(startScore)) {
 					scoreFound = true;
 				} else {
 					continue;
 				}
 			}
 
 			if (first) {
 				first = false;
 			} else {
 				start = "";
 			}
 
 			SliceQuery<Integer, String, String> sliceQuery = HFactory
 					.createSliceQuery(consistencyOneKeyspace, is, ss, ss);
 
 			sliceQuery.setColumnFamily(ranksColumnFamily);
 			sliceQuery.setKey(score);
 			sliceQuery.setRange(start, "", reverseOrder, limit);
 
 			QueryResult<ColumnSlice<String, String>> sliceResult = sliceQuery
 					.execute();
 			ColumnSlice<String, String> columnSlice = sliceResult.get();
 			for (HColumn<String, String> column : columnSlice.getColumns()) {
 				userIds.add(column.getValue());
 				if (userIds.size() >= limit) {
 					break EXTERNALLOOP;
 				}
 			}
 		}
 
 		return loadUsers(userIds);
 
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		
 		consistencyOneKeyspace = HFactory.createKeyspace(dbKeyspace, cluster, new AllOneConsistencyLevelPolicy());
 		consistencyQuorumKeyspace = HFactory.createKeyspace(dbKeyspace, cluster, new QuorumAllConsistencyLevelPolicy());
 		
 	}
 }
