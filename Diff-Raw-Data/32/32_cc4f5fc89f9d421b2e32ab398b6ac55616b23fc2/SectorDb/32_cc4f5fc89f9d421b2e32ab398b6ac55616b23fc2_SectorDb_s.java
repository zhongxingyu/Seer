 package carnero.netmap.database;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.util.Log;
 
 import carnero.netmap.common.Constants;
 import carnero.netmap.model.Sector;
 import carnero.netmap.model.XY;
 
 public class SectorDb {
 
 	public static boolean isSaved(OperatorDatabase od, Sector sector) {
 		boolean saved = false;
 
 		Cursor cursor = null;
 		try {
 			// select all BTS' with no particular order
 			final StringBuilder where = new StringBuilder();
 			where.append(DatabaseStructure.COLUMNS_SECTORS.X);
 			where.append(" = ");
 			where.append(sector.index.x);
 			where.append(" and ");
 			where.append(DatabaseStructure.COLUMNS_SECTORS.Y);
 			where.append(" = ");
 			where.append(sector.index.y);
 			where.append(" and ");
 			where.append(DatabaseStructure.COLUMNS_SECTORS.NETWORK);
 			where.append(" <= ");
 			where.append(sector.network);
 
 			cursor = od.database.query(
 				DatabaseHelper.getSectorsTableName(od.operatorID),
 				new String[]{DatabaseStructure.COLUMNS_SECTORS.ID},
 				where.toString(),
 				null,
 				null,
 				null,
 				"1"
 			);
 
 			if (cursor != null && cursor.getCount() > 0) {
 				saved = true;
 			}
 		} finally {
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 
 		return saved;
 	}
 
 	public static boolean save(OperatorDatabase od, Sector sector) {
 		if (SectorDb.isSaved(od, sector)) {
 			return true;
 		}
 
 		final StringBuilder where = new StringBuilder();
 		where.append(DatabaseStructure.COLUMNS_SECTORS.X);
 		where.append(" = ");
 		where.append(sector.index.x);
 		where.append(" and ");
 		where.append(DatabaseStructure.COLUMNS_SECTORS.Y);
 		where.append(" = ");
 		where.append(sector.index.y);
 
 		final ContentValues values = new ContentValues();
 		values.put(DatabaseStructure.COLUMNS_SECTORS.X, sector.index.x);
 		values.put(DatabaseStructure.COLUMNS_SECTORS.Y, sector.index.y);
 		values.put(DatabaseStructure.COLUMNS_SECTORS.NETWORK, sector.network);
 		values.put(DatabaseStructure.COLUMNS_SECTORS.SIGNAL_AVERAGE, sector.signalAverage);
 		values.put(DatabaseStructure.COLUMNS_SECTORS.SIGNAL_COUNT, sector.signalCount);
 
 		// update
 		try {
 			int rows = od.database.update(DatabaseHelper.getSectorsTableName(od.operatorID), values, where.toString(), null);
 			if (rows > 0) {
 				Log.i(Constants.TAG, "Sector " + sector.index + " was updated");
 				return true;
 			}
 		} catch (Exception e) {
 			// pokemon
 		}
 
 		// insert new
 		try {
 			long id = od.database.insert(DatabaseHelper.getSectorsTableName(od.operatorID), null, values);
 			if (id > 0) {
 				Log.i(Constants.TAG, "Sector " + sector.index + " was saved");
 				return true;
 			}
 		} catch (Exception e) {
 			// pokemon
 		}
 
 		return false;
 	}
 
 	public static boolean updateNetwork(OperatorDatabase od, Sector sector) {
 		final StringBuilder where = new StringBuilder();
 		where.append(DatabaseStructure.COLUMNS_SECTORS.X);
 		where.append(" = ");
 		where.append(sector.index.x);
 		where.append(" and ");
 		where.append(DatabaseStructure.COLUMNS_SECTORS.Y);
 		where.append(" = ");
 		where.append(sector.index.y);
 
 		final ContentValues values = new ContentValues();
 		values.put(DatabaseStructure.COLUMNS_SECTORS.NETWORK, sector.network);
 
 		// update
 		try {
 			int rows = od.database.update(DatabaseHelper.getSectorsTableName(od.operatorID), values, where.toString(), null);
 			if (rows > 0) {
 				Log.i(Constants.TAG, "Sector " + sector.index + " was updated");
 				return true;
 			}
 		} catch (Exception e) {
 			// pokemon
 		}
 
 		return false;
 	}
 
 	public static Sector load(OperatorDatabase od, XY xy) {
 		Sector sector = null;
 
 		Cursor cursor = null;
 		try {
 			final StringBuilder where = new StringBuilder();
 			where.append(DatabaseStructure.COLUMNS_SECTORS.X);
 			where.append(" = ");
 			where.append(xy.x);
 			where.append(" and ");
 			where.append(DatabaseStructure.COLUMNS_SECTORS.Y);
 			where.append(" = ");
 			where.append(xy.y);
 
 			cursor = od.database.query(
 				DatabaseHelper.getSectorsTableName(od.operatorID),
 				DatabaseStructure.PROJECTION.SECTOR,
 				where.toString(),
 				null,
 				null,
 				null,
 				"1"
 			);
 
 			if (cursor != null && cursor.getCount() > 0) {
 				cursor.moveToFirst();
 
 				final int idxX = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.X);
 				final int idxY = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.Y);
 				final int idxType = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.NETWORK);
 				final int idxAverage = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.SIGNAL_AVERAGE);
 				final int idxCount = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.SIGNAL_COUNT);
 
 				sector = new Sector();
 				sector.index = new XY(cursor.getInt(idxX), cursor.getInt(idxY));
 				sector.network = cursor.getInt(idxType);
 				sector.signalAverage = cursor.getDouble(idxAverage);
 				sector.signalCount = cursor.getInt(idxCount);
 			}
 		} finally {
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 
 		return sector;
 	}
 
 	public static List<Sector> loadAll(OperatorDatabase od) {
 		final ArrayList<Sector> sectorList = new ArrayList<Sector>();
 
 		Cursor cursor = null;
 		try {
 			cursor = od.database.query(
 				DatabaseHelper.getSectorsTableName(od.operatorID),
 				DatabaseStructure.PROJECTION.SECTOR,
 				null,
 				null,
 				null,
 				null,
 				null
 			);
 
 			if (cursor != null && cursor.getCount() > 0) {
 				cursor.moveToFirst();
 
 				final int idxX = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.X);
 				final int idxY = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.Y);
 				final int idxType = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.NETWORK);
 				final int idxAverage = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.SIGNAL_AVERAGE);
 				final int idxCount = cursor.getColumnIndex(DatabaseStructure.COLUMNS_SECTORS.SIGNAL_COUNT);
 
 				Sector sector;
 				do {
 					sector = new Sector();
 					sector.index = new XY(cursor.getInt(idxX), cursor.getInt(idxY));
 					sector.network = cursor.getInt(idxType);
 					sector.signalAverage = cursor.getDouble(idxAverage);
 					sector.signalCount = cursor.getInt(idxCount);
 
 					sectorList.add(sector);
 				} while (cursor.moveToNext());
 			}
 		} finally {
 			if (cursor != null) {
 				cursor.close();
 			}
 		}
 
 		return sectorList;
 	}
 }
