 package carnero.netmap.database;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.util.Log;
 
 import carnero.netmap.common.Constants;
 import carnero.netmap.model.Bts;
 import com.google.android.gms.maps.model.LatLng;
 
 public class BtsDb {
 
 	public static boolean isSaved(OperatorDatabase od, Bts bts) {
 		boolean saved = false;
 
 		Cursor cursor = null;
 		try {
 			// select all BTS' with no particular order
 			final StringBuilder where = new StringBuilder();
 			where.append(DatabaseStructure.COLUMNS_BTS.LAC);
 			where.append(" = ");
 			where.append(bts.lac);
 			where.append(" and ");
 			where.append(DatabaseStructure.COLUMNS_BTS.CID);
 			where.append(" = ");
 			where.append(bts.cid);
 			where.append(" and ");
 			where.append(DatabaseStructure.COLUMNS_BTS.NETWORK);
 			where.append(" <= ");
 			where.append(bts.network);
 
 			cursor = od.database.query(
 				DatabaseHelper.getBTSTableName(od.operatorID),
 				new String[]{DatabaseStructure.COLUMNS_BTS.ID},
 				where.toString(),
 				null,
 				null,
 				null,
 				"1"
 			);
 
 			if (cursor != null && cursor.getCount() > 0) {
 				saved = true;
 			}
		} catch (Exception e) {
			// pokemon
 		} finally {
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 
 		return saved;
 	}
 
 	public static boolean save(OperatorDatabase od, Bts bts) {
 		if (BtsDb.isSaved(od, bts)) {
 			return true;
 		}
 
 		final StringBuilder where = new StringBuilder();
 		where.append(DatabaseStructure.COLUMNS_BTS.LAC);
 		where.append(" = ");
 		where.append(bts.lac);
 		where.append(" and ");
 		where.append(DatabaseStructure.COLUMNS_BTS.CID);
 		where.append(" = ");
 		where.append(bts.cid);
 
 		final ContentValues values = new ContentValues();
 		values.put(DatabaseStructure.COLUMNS_BTS.LAC, bts.lac);
 		values.put(DatabaseStructure.COLUMNS_BTS.CID, bts.cid);
 		values.put(DatabaseStructure.COLUMNS_BTS.NETWORK, bts.network);
 		if (bts.location != null) {
 			values.put(DatabaseStructure.COLUMNS_BTS.LATITUDE, bts.location.latitude);
 			values.put(DatabaseStructure.COLUMNS_BTS.LONGITUDE, bts.location.longitude);
 		}
 
 		// update
 		try {
 			int rows = od.database.update(DatabaseHelper.getBTSTableName(od.operatorID), values, where.toString(), null);
 			if (rows > 0) {
 				Log.i(Constants.TAG, "BTS " + bts + " was updated (save)");
 				return true;
 			}
 		} catch (Exception e) {
 			// pokemon
 		}
 
 		// insert new
 		try {
 			long id = od.database.insert(DatabaseHelper.getBTSTableName(od.operatorID), null, values);
 			if (id > 0) {
 				Log.i(Constants.TAG, "BTS " + bts + " was saved");
 				return true;
 			}
 		} catch (Exception e) {
 			// pokemon
 		}
 
 		return false;
 	}
 
 	public static boolean updateNetwork(OperatorDatabase od, Bts bts) {
 		final StringBuilder where = new StringBuilder();
 		where.append(DatabaseStructure.COLUMNS_BTS.LAC);
 		where.append(" = ");
 		where.append(bts.lac);
 		where.append(" and ");
 		where.append(DatabaseStructure.COLUMNS_BTS.CID);
 		where.append(" = ");
 		where.append(bts.cid);
 
 		final ContentValues values = new ContentValues();
 		values.put(DatabaseStructure.COLUMNS_BTS.NETWORK, bts.network);
 
 		// update
 		try {
 			int rows = od.database.update(DatabaseHelper.getBTSTableName(od.operatorID), values, where.toString(), null);
 			if (rows > 0) {
 				Log.i(Constants.TAG, "BTS " + bts + " was updated (network)");
 				return true;
 			}
 		} catch (Exception e) {
 			// pokemon
 		}
 
 		return false;
 	}
 
 	public static boolean updateLocation(OperatorDatabase od, Bts bts) {
 		if (bts.location == null) {
 			return false;
 		}
 
 		final StringBuilder where = new StringBuilder();
 		where.append(DatabaseStructure.COLUMNS_BTS.LAC);
 		where.append(" = ");
 		where.append(bts.lac);
 		where.append(" and ");
 		where.append(DatabaseStructure.COLUMNS_BTS.CID);
 		where.append(" = ");
 		where.append(bts.cid);
 
 		final ContentValues values = new ContentValues();
 		values.put(DatabaseStructure.COLUMNS_BTS.LATITUDE, bts.location.latitude);
 		values.put(DatabaseStructure.COLUMNS_BTS.LONGITUDE, bts.location.longitude);
 
 		// update
 		try {
 			int rows = od.database.update(DatabaseHelper.getBTSTableName(od.operatorID), values, where.toString(), null);
 			if (rows > 0) {
 				Log.i(Constants.TAG, "BTS " + bts + " was updated (location)");
 				return true;
 			}
 		} catch (Exception e) {
 			// pokemon
 		}
 
 		return false;
 	}
 
 	public static Bts load(OperatorDatabase od, long lac, long cid) {
 		Bts bts = null;
 
 		Cursor cursor = null;
 		try {
 			final StringBuilder where = new StringBuilder();
 			where.append(DatabaseStructure.COLUMNS_BTS.LAC);
 			where.append(" = ");
 			where.append(lac);
 			where.append(" and ");
 			where.append(DatabaseStructure.COLUMNS_BTS.CID);
 			where.append(" = ");
 			where.append(cid);
 
 			cursor = od.database.query(
 				DatabaseHelper.getBTSTableName(od.operatorID),
 				DatabaseStructure.PROJECTION.BTS,
 				where.toString(),
 				null,
 				null,
 				null,
 				"1"
 			);
 
 			if (cursor != null && cursor.getCount() > 0) {
 				cursor.moveToFirst();
 
 				final int idxLac = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LAC);
 				final int idxCid = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.CID);
 				final int idxType = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.NETWORK);
 				final int idxLatitude = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LATITUDE);
 				final int idxLongitude = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LONGITUDE);
 
 				bts = new Bts();
 				bts.lac = cursor.getLong(idxLac);
 				bts.cid = cursor.getLong(idxCid);
 				bts.network = cursor.getInt(idxType);
 				if (!cursor.isNull(idxLatitude) && !cursor.isNull(idxLongitude)) {
 					bts.location = new LatLng(cursor.getDouble(idxLatitude), cursor.getDouble(idxLongitude));
 				}
 			}
		} catch (Exception e) {
			// pokemon
 		} finally {
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 
 		return bts;
 	}
 
 	public static List<Bts> loadAll(OperatorDatabase od) {
 		final ArrayList<Bts> btsList = new ArrayList<>();
 
 		Cursor cursor = null;
 		try {
 			cursor = od.database.query(
 				DatabaseHelper.getBTSTableName(od.operatorID),
 				DatabaseStructure.PROJECTION.BTS,
 				null,
 				null,
 				null,
 				null,
 				null
 			);
 
 			if (cursor != null && cursor.getCount() > 0) {
 				cursor.moveToFirst();
 
 				final int idxLac = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LAC);
 				final int idxCid = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.CID);
 				final int idxType = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.NETWORK);
 				final int idxLatitude = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LATITUDE);
 				final int idxLongitude = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LONGITUDE);
 
 				Bts bts;
 				do {
 					bts = new Bts();
 					bts.lac = cursor.getLong(idxLac);
 					bts.cid = cursor.getLong(idxCid);
 					bts.network = cursor.getInt(idxType);
 					if (!cursor.isNull(idxLatitude) && !cursor.isNull(idxLongitude)) {
 						bts.location = new LatLng(cursor.getDouble(idxLatitude), cursor.getDouble(idxLongitude));
 					}
 
 					btsList.add(bts);
 				} while (cursor.moveToNext());
 			}
		} catch (Exception e) {
			// pokemon
 		} finally {
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 
 		return btsList;
 	}
 }
