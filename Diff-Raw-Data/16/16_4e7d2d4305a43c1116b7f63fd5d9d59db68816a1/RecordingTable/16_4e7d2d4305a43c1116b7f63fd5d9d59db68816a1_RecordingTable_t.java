 package es.alltogether.c3po.db;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.database.Cursor;
 import es.alltogether.c3po.models.Recording;
 
 public class RecordingTable {
 
 	public static final String TABLE_NAME = "recordings";
 
 	DBHelper dbHelper;
 
 	public RecordingTable(Activity activity) {
 		dbHelper = new DBHelper(activity);
 	}
 
 	public List<Recording> findByCriteria(String where) {
 		Cursor cursorResources = dbHelper.getReadableDatabase().query(
				TABLE_NAME, null, where, null, null, null, null);
 		List<Recording> resources = new ArrayList<Recording>();
 		while (cursorResources.moveToNext()) {
 			resources.add(new Recording(cursorResources));
 		}
 		return resources;
 	}
 
 	public Recording findById(Long id) throws NoResultException {
 		try {
 			Cursor cursorResources = dbHelper.getReadableDatabase().query(
 					TABLE_NAME, null, Recording._ID + " = ?",
 					new String[] { id.toString() }, null, null, null);
 			return new Recording(cursorResources);
 		} catch (Exception e) {
 			throw new NoResultException();
 		}
 	}
 
 	public void save(Recording resource) {
 		Long id = dbHelper.getWritableDatabase().insertOrThrow(TABLE_NAME,
 				null, resource.getContentValues());
 		resource.setId(id);
 	}
 
 	public void delete(Recording resource) {
 		String where = Recording._ID + " = ?";
 		dbHelper.getWritableDatabase().delete(TABLE_NAME, where,
 				new String[] { resource.getId().toString() });
 	}
 }
