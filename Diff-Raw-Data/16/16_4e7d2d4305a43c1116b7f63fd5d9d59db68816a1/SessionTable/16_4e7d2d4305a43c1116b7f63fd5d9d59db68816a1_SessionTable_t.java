 package es.alltogether.c3po.db;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.database.Cursor;
 import es.alltogether.c3po.models.Session;
 
 public class SessionTable {
 
 	public static final String TABLE_NAME = "sessions";
 
 	DBHelper dbHelper;
 
 	public SessionTable(Activity activity) {
 		dbHelper = new DBHelper(activity);
 	}
 
 	public List<Session> findByCriteria(String where) {
 		Cursor cursorSessions = dbHelper.getReadableDatabase().query(
				TABLE_NAME, null, where, null, null, null, null);
 		List<Session> sessions = new ArrayList<Session>();
 		while (cursorSessions.moveToNext()) {
 			sessions.add(new Session(cursorSessions));
 		}
 		return sessions;
 	}
 
 	public Session findById(Long id) throws NoResultException {
 		try {
 			Cursor cursorSessions = dbHelper.getReadableDatabase().query(
 					TABLE_NAME, null, Session._ID + " = ?",
 					new String[] { id.toString() }, null, null, null);
 			return new Session(cursorSessions);
 		} catch (Exception e) {
 			throw new NoResultException();
 		}
 	}
 
 	public void save(Session session) {
 		Long id = dbHelper.getWritableDatabase().insertOrThrow(TABLE_NAME,
 				null, session.getContentValues());
 		session.setId(id);
 	}
 
 	public void delete(Session session) {
 		String where = Session._ID + " = ?";
 		dbHelper.getWritableDatabase().delete(TABLE_NAME, where,
 				new String[] { session.getId().toString() });
 	}
 }
