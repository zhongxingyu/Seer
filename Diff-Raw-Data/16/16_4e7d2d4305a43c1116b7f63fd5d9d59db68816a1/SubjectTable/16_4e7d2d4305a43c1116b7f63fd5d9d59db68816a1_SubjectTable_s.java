 package es.alltogether.c3po.db;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.database.Cursor;
 import es.alltogether.c3po.models.Subject;
 
 public class SubjectTable {
 
 	public static final String TABLE_NAME = "subjects";
 
 	DBHelper dbHelper;
 
 	public SubjectTable(Activity activity) {
 		dbHelper = new DBHelper(activity);
 	}
 
 	public List<Subject> findByCriteria(String where) {
 		Cursor cursorSubjects = dbHelper.getReadableDatabase().query(
				TABLE_NAME, null, null, null, null, null, null);
 		List<Subject> Subjects = new ArrayList<Subject>();
 		while (cursorSubjects.moveToNext()) {
 			Subjects.add(new Subject(cursorSubjects));
 		}
 		return Subjects;
 	}
 
 	public Subject findById(Long id) throws NoResultException {
 		try {
 			Cursor cursorSubjects = dbHelper.getReadableDatabase().query(
 					TABLE_NAME, null, Subject._ID + " = ?",
 					new String[] { id.toString() }, null, null, null);
 			return new Subject(cursorSubjects);
 		} catch (Exception e) {
 			throw new NoResultException();
 		}
 	}
 
 	public void save(Subject subject) {
 		Long id = dbHelper.getWritableDatabase().insertOrThrow(TABLE_NAME,
 				null, subject.getContentValues());
 		subject.setId(id);
 	}
 
 	public void delete(Subject subject) {
 		String where = Subject._ID + " = ?";
 		dbHelper.getWritableDatabase().delete(TABLE_NAME, where,
 				new String[] { subject.getId().toString() });
 	}
 }
