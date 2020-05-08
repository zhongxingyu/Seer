 package org.araqne.logdb.query.command;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.logdb.LogMap;
 import org.araqne.logdb.LogQueryCommand;
 import org.araqne.logdb.LogResultSet;
 import org.araqne.logdb.query.command.Sort.SortField;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Join extends LogQueryCommand {
 	private final Logger logger = LoggerFactory.getLogger(Join.class);
 
 	private Result subQueryResult;
 	private LogResultSet subQueryResultSet;
 
 	// for later sort-merge join
 	private Object[] sortJoinKeys1;
 	private Object[] sortJoinKeys2;
 
 	// for hash join
 	private HashMap<JoinKeys, List<Object>> hashJoinMap;
 	private JoinKeys joinKeys;
 
 	private int joinKeyCount;
 	private SortField[] sortFields;
 	private List<LogQueryCommand> subQuery;
 
 	private SubQueryRunner subQueryRunner = new SubQueryRunner();
 
 	public Join(SortField[] sortFields, List<LogQueryCommand> subQuery) {
 		try {
 			this.joinKeyCount = sortFields.length;
 			this.joinKeys = new JoinKeys(new Object[joinKeyCount]);
 			this.sortJoinKeys1 = new Object[sortFields.length];
 			this.sortJoinKeys2 = new Object[sortFields.length];
 
 			this.sortFields = sortFields;
 			this.subQuery = subQuery;
 			this.subQueryResult = new Result("sub");
 
 			Sort sort = new Sort(sortFields);
 			sort.init();
 
 			LogQueryCommand lastCmd = subQuery.get(subQuery.size() - 1);
 
 			lastCmd.setNextCommand(sort);
 			sort.setNextCommand(subQueryResult);
 
 			this.subQuery.add(sort);
 			this.subQuery.add(subQueryResult);
 		} catch (IOException e) {
 			throw new IllegalStateException("cannot create join query", e);
 		}
 	}
 
 	public SortField[] getSortFields() {
 		return sortFields;
 	}
 
 	public List<LogQueryCommand> getSubQuery() {
 		return subQuery;
 	}
 
 	@Override
 	public void start() {
 		Thread t = new Thread(subQueryRunner);
 		t.start();
 	}
 
 	@Override
 	public void push(LogMap m) {
 		LogQueryCommand cmd = subQuery.get(subQuery.size() - 1);
 
 		// wait until subquery end
 		synchronized (cmd) {
 			while (subQueryResultSet == null && cmd.getStatus() != Status.End) {
 				try {
 					cmd.wait(1000);
 				} catch (InterruptedException e) {
 				}
 			}
 		}
 
 		if (subQueryResultSet == null) {
 			eof(true);
 			return;
 		}
 
 		subQueryResultSet.reset();
 
 		if (hashJoinMap != null) {
 			int i = 0;
 			for (SortField f : sortFields)
 				joinKeys.keys[i++] = m.get(f.getName());
 
 			List<Object> l = hashJoinMap.get(joinKeys);
 			if (l == null)
 				return;
 
 			for (Object o : l) {
 				@SuppressWarnings("unchecked")
 				Map<String, Object> sm = (Map<String, Object>) o;
 				Map<String, Object> joinMap = new HashMap<String, Object>(m.map());
 				joinMap.putAll(sm);
 				write(new LogMap(joinMap));
 			}
 			return;
 		}
 
 		int i = 0;
 		for (SortField f : sortFields)
 			sortJoinKeys1[i++] = m.get(f.getName());
 
 		while (subQueryResultSet.hasNext()) {
 			Map<String, Object> sm = subQueryResultSet.next();
 
 			i = 0;
 			for (SortField f : sortFields)
 				sortJoinKeys2[i++] = sm.get(f.getName());
 
 			if (Arrays.equals(sortJoinKeys1, sortJoinKeys2)) {
 				Map<String, Object> joinMap = new HashMap<String, Object>(m.map());
 				joinMap.putAll(sm);
 				write(new LogMap(joinMap));
 			}
 		}
 	}
 
 	@Override
 	public boolean isReducer() {
 		return false;
 	}
 
 	@Override
 	public void eof(boolean cancelled) {
 		subQuery.get(0).eof(cancelled);
 
 		if (subQueryResultSet != null) {
 			try {
 				subQueryResultSet.close();
 			} catch (Throwable t) {
 				logger.error("araqne logdb: subquery result set close failed, query " + logQuery.getId(), t);
 			}
 		}
 
 		if (subQueryResult != null) {
 			try {
 				subQueryResult.purge();
 			} catch (Throwable t) {
 				logger.error("araqne logdb: subquery result purge failed, query " + logQuery.getId(), t);
 			}
 		}
 
 		super.eof(cancelled);
 	}
 
 	private class SubQueryRunner implements Runnable {
 		private static final int HASH_JOIN_THRESHOLD = 50000;
 
 		@Override
 		public void run() {
 			LogQueryCommand cmd = null;
 			try {
				cmd = subQuery.get(subQuery.size() - 1);
 				for (int i = subQuery.size() - 1; i >= 0; i--)
 					subQuery.get(i).start();
 
 				subQuery.get(0).eof(false);
 
 				try {
 					subQueryResultSet = subQueryResult.getResult();
 					logger.debug("araqne logdb: fetch subquery result of query [{}:{}]", logQuery.getId(), logQuery.getQueryString());
 
 					if (subQueryResultSet.size() <= HASH_JOIN_THRESHOLD)
 						buildHashJoinTable();
 
 				} catch (IOException e) {
 					logger.error("araqne logdb: cannot get subquery result of query " + logQuery.getId(), e);
 				}
 
 			} catch (Throwable t) {
 				logger.error("araqne logdb: subquery failed, query " + logQuery.getId(), t);
 			} finally {
 				if (cmd != null) {
 					synchronized (cmd) {
 						cmd.notifyAll();
 					}
 				}
 
 				logger.debug("araqne logdb: subquery end, query " + logQuery.getId());
 			}
 		}
 
 		private void buildHashJoinTable() {
 			hashJoinMap = new HashMap<JoinKeys, List<Object>>(50000);
 
 			while (subQueryResultSet.hasNext()) {
 				Map<String, Object> sm = subQueryResultSet.next();
 
 				Object[] keys = new Object[joinKeyCount];
 				for (int i = 0; i < joinKeyCount; i++)
 					keys[i] = sm.get(sortFields[i].getName());
 
 				JoinKeys joinKeys = new JoinKeys(keys);
 				List<Object> l = hashJoinMap.get(joinKeys);
 				if (l == null) {
 					l = new ArrayList<Object>(2);
 					hashJoinMap.put(joinKeys, l);
 				}
 
 				l.add(sm);
 			}
 		}
 	}
 
 	private static class JoinKeys {
 		private Object[] keys;
 
 		public JoinKeys(Object[] keys) {
 			this.keys = keys;
 		}
 
 		@Override
 		public int hashCode() {
 			return Arrays.hashCode(keys);
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			JoinKeys other = (JoinKeys) obj;
 			if (!Arrays.equals(keys, other.keys))
 				return false;
 			return true;
 		}
 	}
 }
