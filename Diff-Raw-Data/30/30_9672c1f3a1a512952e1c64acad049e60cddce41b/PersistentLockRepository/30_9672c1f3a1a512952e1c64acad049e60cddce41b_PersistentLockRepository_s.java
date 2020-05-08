 package org.iucn.sis.server.api.locking;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.User;
 
 import com.solertium.db.CDateTime;
 import com.solertium.db.CInteger;
 import com.solertium.db.CString;
 import com.solertium.db.CanonicalColumnName;
 import com.solertium.db.DBException;
 import com.solertium.db.ExecutionContext;
 import com.solertium.db.Row;
 import com.solertium.db.RowID;
 import com.solertium.db.RowProcessor;
 import com.solertium.db.query.DeleteQuery;
 import com.solertium.db.query.InsertQuery;
 import com.solertium.db.query.QConstraint;
 import com.solertium.db.query.SelectQuery;
 import com.solertium.util.TrivialExceptionHandler;
 
 public class PersistentLockRepository extends LockRepository {
 	
	public static final String LOCK_TABLE = "lock";
 
 	@Override
 	public LockInfo getLockedAssessment(Integer id) {
 		final Integer identifier = id;
 		final SelectQuery query = new SelectQuery();
 		query.select(LOCK_TABLE, "userid");
 		query.select(LOCK_TABLE, "type");
 		query.constrain(new CanonicalColumnName(LOCK_TABLE, "lockid"), 
 			QConstraint.CT_EQUALS, identifier	
 		);
 		
 		final Row.Loader rl = new Row.Loader();
 		
 		try {
 			SIS.get().getExecutionContext().doQuery(query, rl);
 		} catch (DBException e) {
 			return null;
 		}
 		
 		if (rl.getRow() == null)
 			return null;
 		else {
 			final Row row = rl.getRow();
 			return new LockRepository.LockInfo(
 				identifier, row.get("userid").getInteger(), 
 				LockType.fromString(row.get("type").toString()), 
				row.get("group").toString(), 
 				row.get("date").getDate(), this
 			);
 		}
 	}
 
 	public boolean isAssessmentPersistentLocked(Integer id) {
 		final String identifier = id.toString();
 		final SelectQuery query = new SelectQuery();
 		query.select(LOCK_TABLE, "id");
 		query.constrain(new CanonicalColumnName(LOCK_TABLE, "lockid"), 
 			QConstraint.CT_EQUALS, identifier	
 		);
 		
 		final Row.Loader rl = new Row.Loader();
 		
 		try {
 			SIS.get().getExecutionContext().doQuery(query, rl);
 		} catch (DBException e) {
 			return false;
 		}
 		
 		return rl.getRow() != null;
 	}
 	
 	@Override
 	public Map<String, List<Integer>> listGroups() {
 		final SelectQuery query = new SelectQuery();
 		query.select(LOCK_TABLE, "*");
 		
 		final Map<String, List<Integer>> map = 
 			new ConcurrentHashMap<String, List<Integer>>();
 		
 		synchronized (this) {
 			try {
 				SIS.get().getExecutionContext().doQuery(query, new RowProcessor() {
 					public void process(Row row) {
						final String groupID = row.get("group").toString();
 						
 						List<Integer> list = map.get(groupID); 
 						if (list == null)
 							list = new ArrayList<Integer>();
 						
 						list.add(row.get("id").getInteger());
 						
 						map.put(groupID, list);
 					}
 				});
 			} catch (DBException e) {
 				TrivialExceptionHandler.ignore(this, e);
 			}
 		};
 		
 		return map;
 	}
 	
 	public List<LockInfo> listLocks() {
 		final SelectQuery query = new SelectQuery();
 		query.select(LOCK_TABLE, "*");
 		
 		final List<LockInfo> list = new ArrayList<LockInfo>();
 		
 		synchronized (this) {
 			try {
 				SIS.get().getExecutionContext().doQuery(query, new RowProcessor() {
 					public void process(Row row) {
 						list.add(new LockRepository.LockInfo(
 							row.get("lockid").getInteger(), row.get("userid").getInteger(), 
 							LockType.fromString(row.get("type").toString()), 
							row.get("group").toString(), 
 							row.get("date").getDate(), 
 							PersistentLockRepository.this
 						));
 					}
 				});
 			} catch (DBException e) {
 				Debug.println(e);
 				TrivialExceptionHandler.ignore(this, e);
 			}
 		};
 		
 		return list;
 	}
 
 	@Override
 	public LockInfo lockAssessment(Integer id, User owner, LockType lockType) {
 		return lockAssessment(id, owner, lockType, null);
 	}
 	
 	@Override
 	public LockInfo lockAssessment(Integer id, User owner, LockType lockType, String group) {
 		final ExecutionContext ec = SIS.get().getExecutionContext();
 		if (isAssessmentPersistentLocked(id)) {
 			return getLockedAssessment(id);
 		}
 		
 		final Integer rowID;
 		try {
 			rowID = Integer.valueOf((int)RowID.get(ec, LOCK_TABLE, "id"));
 		} catch (DBException e) {
 			return null;
 		}
 		
 		final Date date = Calendar.getInstance().getTime();
 		
 		final Row row = new Row();
 		row.add(new CInteger("id", rowID));
 		row.add(new CInteger("lockid", id));
 		row.add(new CInteger("userid", owner.getId()));
 		row.add(new CDateTime("date", date));
 		row.add(new CString("type", lockType.toString()));
		row.add(new CString("group", group));
 
 		final InsertQuery query = new InsertQuery();
 		query.setRow(row);
 		query.setTable(LOCK_TABLE);
 		
 		try {
 			ec.doUpdate(query);
 		} catch (DBException e) {
 			return null;
 		}
 		
 		return new LockRepository.LockInfo(id, owner.getId(), lockType, group, date, this);
 	}
 
 	public void removeLockByID(Integer id) {
 		final ExecutionContext ec = SIS.get().getExecutionContext();
 		
 		final Row.Loader rl = new Row.Loader(); {
 			final SelectQuery query = new SelectQuery();
 			query.select(LOCK_TABLE, "id");
 			query.constrain(new CanonicalColumnName(LOCK_TABLE, "lockid"), 
 				QConstraint.CT_EQUALS, id
 			);
 			
 			try {
 				ec.doQuery(query, rl);
 			} catch (DBException e) {
 				Debug.println(e);
 				return;
 			}
 		}
 		
 		final Integer rowID;
 		if (rl.getRow() == null) {
 			return;
 		}
 		else
 			rowID = rl.getRow().get("id").getInteger();
 		
 		{
 			final DeleteQuery query = new DeleteQuery();
 			query.setTable(LOCK_TABLE);
 			query.constrain(new CanonicalColumnName(LOCK_TABLE, "id"), 
 				QConstraint.CT_EQUALS, rowID
 			);
 			
 			try {
 				ec.doUpdate(query); 
 			} catch (DBException e) {
 				TrivialExceptionHandler.ignore(this, e);
 			}
 		}
 	}
 	
 	public void clearGroup(String group) {
 		final ExecutionContext ec = SIS.get().getExecutionContext();
 		
 		final DeleteQuery query = new DeleteQuery();
 		query.setTable(LOCK_TABLE);
		query.constrain(new CanonicalColumnName(LOCK_TABLE, "group"), QConstraint.CT_EQUALS, group);
 		
 		try {
 			ec.doUpdate(query);
 		} catch (DBException e) {
 			TrivialExceptionHandler.ignore(this, e);
 		}
 	}
 
 }
