 package com.zettsett.timetracker.database;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.util.Log;
 
 import com.zettsett.timetracker.Global;
 import com.zettsett.timetracker.Settings;
 import com.zettsett.timetracker.activity.TimeSliceFilterParameter;
 import com.zettsett.timetracker.model.ITimeSliceFilter;
 import com.zettsett.timetracker.model.TimeSlice;
 import com.zettsett.timetracker.model.TimeSliceCategory;
 
 import de.k3b.database.SqlFilter;
 
 public class TimeSliceRepository {
 	private static final DatabaseInstance CURRENT_DB_INSTANCE = DatabaseInstance
 			.getCurrentInstance();
 	private static TimeSliceRepository timeSliceRepositorySingleton;
 	private final TimeSliceCategoryRepsitory categoryRepository;
 
 	public TimeSliceRepository(final Context context) {
 		TimeSliceRepository.CURRENT_DB_INSTANCE.initialize(context);
 		this.categoryRepository = new TimeSliceCategoryRepsitory(context);
 	}
 
 	public static TimeSliceRepository getDBAdapter(final Context context) {
 		if (TimeSliceRepository.timeSliceRepositorySingleton == null) {
 			TimeSliceRepository.timeSliceRepositorySingleton = new TimeSliceRepository(
 					context);
 		}
 		return TimeSliceRepository.timeSliceRepositorySingleton;
 	}
 
 	/**
 	 * creates a new timeSlice, <br/>
	 * if distance to last timeslice > Settings.minPunchInTrashhold.<br/>
	 * Else append to previous.
 	 * 
 	 * @param timeSlice
 	 * @return rowid if successfull or -1 if error.
 	 */
 	public long create(final TimeSlice timeSlice) {
		final long newStartTime = timeSlice.getStartTime();
 		final TimeSlice oldTimeSlice = this
 				.fetchOldestByCategoryAndEndTimeInterval(
 						"create-Find with same category to append to",
 						timeSlice.getCategory(),
						newStartTime
 								- Settings.getMinPunchInTreshholdInMilliSecs(),
						newStartTime);
 
 		if (oldTimeSlice != null) {
			timeSlice.setRowId(oldTimeSlice.getRowId()).setNotes(
					oldTimeSlice.getNotes() + " " + timeSlice.getNotes());
			final long oldStartTime = oldTimeSlice.getStartTime();
			if (oldStartTime < newStartTime) {
				timeSlice.setStartTime(oldStartTime);
			}
 			if (Global.isDebugEnabled()) {
 				Log.d(Global.LOG_CONTEXT, "create(): merging old timeslice '"
 						+ oldTimeSlice + "' to '" + timeSlice + "'.");
 			}
 			return (this.update(timeSlice) > 0) ? timeSlice.getRowId() : -1;
 		} else {
 			if (Global.isDebugEnabled()) {
				Log.d(Global.LOG_CONTEXT,
						"create(): db-inserting new timeslice '" + timeSlice
								+ "'.");
 			}
 			return TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().insert(
 					DatabaseHelper.TIME_SLICE_TABLE, null,
 					this.timeSliceContentValuesList(timeSlice));
 		}
 	}
 
 	/**
 	 * update existing timeslice.
 	 * 
 	 * @return number of affected rows.
 	 */
 	public long update(final TimeSlice timeSlice) {
 		final int result = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb()
 				.update(DatabaseHelper.TIME_SLICE_TABLE,
 						this.timeSliceContentValuesList(timeSlice),
 						"_id = " + timeSlice.getRowId(), null);
 		if (Global.isDebugEnabled()) {
 			Log.d(Global.LOG_CONTEXT, "updateTimeSlice(" + timeSlice + ") "
 					+ result + " rows affected");
 		}
 
 		return result;
 	}
 
 	public static int delete(final ITimeSliceFilter timeSliceFilter) {
 		final String context = "deleteForDateRange(" + timeSliceFilter + ") ";
 
 		final SqlFilter sqlFilter = TimeSliceRepository.createFilter(context,
 				timeSliceFilter);
 
 		final int result = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb()
 				.delete(DatabaseHelper.TIME_SLICE_TABLE, sqlFilter.sql,
 						sqlFilter.args);
 		if (Global.isDebugEnabled()) {
 			Log.d(Global.LOG_CONTEXT, context + result + " rows affected");
 		}
 		return result;
 	}
 
 	public TimeSlice fetchByRowID(final long rowId) throws SQLException {
 		Cursor cur = null;
 		try {
 			cur = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().query(true,
 					DatabaseHelper.TIME_SLICE_TABLE, this.columnList(),
 					"_id=?", new String[] { Long.toString(rowId) }, null, null,
 					null, null);
 			if ((cur != null) && (cur.moveToFirst())) {
 				return this.fillTimeSliceFromCursor(cur);
 			}
 		} catch (final Exception ex) {
 			throw new TimeTrackerDBException(
 					"TimeSliceRepository.fetchByRowID(rowId = " + rowId + ")",
 					null, ex);
 		} finally {
 			if (cur != null) {
 				cur.close();
 			}
 		}
 		Log.e(Global.LOG_CONTEXT,
 				"Not found : TimeSliceRepository.fetchByRowID(rowId = " + rowId
 						+ ")");
 		return null;
 	}
 
 	public List<TimeSlice> fetchList(final ITimeSliceFilter timeSliceFilter) {
 		final String debugMessage = "TimeSliceRepository.fetchTimeSlices("
 				+ timeSliceFilter + ")";
 		final SqlFilter sqlFilter = TimeSliceRepository.createFilter(
 				debugMessage, timeSliceFilter);
 
 		return this.fetchListInternal(debugMessage, sqlFilter);
 	}
 
 	private List<TimeSlice> fetchListInternal(final String debugMessage,
 			final SqlFilter sqlFilter) {
 
 		final List<TimeSlice> result = new ArrayList<TimeSlice>();
 		Cursor cur = null;
 		try {
 			cur = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().query(
 					DatabaseHelper.TIME_SLICE_TABLE, this.columnList(),
 					sqlFilter.sql, sqlFilter.args, null, null,
 					TimeSliceSql.COL_START_TIME);
 			while (cur.moveToNext()) {
 				final TimeSlice ts = this.fillTimeSliceFromCursor(cur);
 				result.add(ts);
 			}
 			if (Global.isDebugEnabled()) {
 				Log.d(Global.LOG_CONTEXT, debugMessage + ": " + result.size()
 						+ " rows affected");
 			}
 			return result;
 		} catch (final Exception ex) {
 			throw new TimeTrackerDBException(debugMessage, sqlFilter, ex);
 		} finally {
 			if (cur != null) {
 				cur.close();
 			}
 		}
 	}
 
 	private TimeSlice fetchOldest(final String debugMessage,
 			final SqlFilter sqlFilter) {
 		final List<TimeSlice> result = this.fetchListInternal(debugMessage,
 				sqlFilter);
 		if (result.size() > 0) {
 			return result.get(0);
 		}
 		return null;
 	}
 
 	private TimeSlice fetchOldestByCategoryAndEndTimeInterval(
 			final String debugMessage, final TimeSliceCategory category,
 			final long minimumEndDate, final long maximumEndDate) {
 		final TimeSliceFilterParameter timeSliceFilter = new TimeSliceFilterParameter()
 				.setParameter(minimumEndDate, maximumEndDate,
 						category.getRowId());
 		final de.k3b.database.SqlFilter sqlFilter = TimeSliceRepository
 				.createFilter(debugMessage, timeSliceFilter);
 		final SqlFilter sqlEndFilter = new SqlFilter(sqlFilter.sql.replace(
 				TimeSliceSql.COL_START_TIME, TimeSliceSql.COL_END_TIME),
 				sqlFilter.args);
 		return this.fetchOldest(debugMessage, sqlEndFilter);
 	}
 
 	public int getCount(final TimeSliceCategory category) throws SQLException {
 		final ITimeSliceFilter timeSliceFilter = new TimeSliceFilterParameter()
 				.setCategoryId(category.getRowId());
 		return TimeSliceRepository.getCount(timeSliceFilter);
 	}
 
 	/**
 	 * Counts how many TimeSlice items exist that matches the timeSliceFilter
 	 * 
 	 * @return
 	 */
 	public static int getCount(final ITimeSliceFilter timeSliceFilter) {
 		final String context = "TimeSliceRepository.getCount("
 				+ timeSliceFilter + ")";
 
 		final SqlFilter sqlFilter = TimeSliceRepository.createFilter(context,
 				timeSliceFilter);
 		Cursor cur = null;
 		int result = -1;
 		try {
 			cur = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().query(
 					DatabaseHelper.TIME_SLICE_TABLE,
 					new String[] { "COUNT(*)" }, sqlFilter.sql, sqlFilter.args,
 					null, null, null);
 			if ((cur != null) && (cur.moveToFirst())) {
 				result = cur.getInt(0);
 			}
 		} catch (final Exception ex) {
 			throw new TimeTrackerDBException(context, sqlFilter, ex);
 		} finally {
 			if (cur != null) {
 				cur.close();
 			}
 			if (Global.isDebugEnabled()) {
 				Log.d(Global.LOG_CONTEXT,
 						sqlFilter.getDebugMessage(context + " = " + result));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Totals those TimeSlice-Durations that matches the timeSliceFilter
 	 * 
 	 * @return
 	 */
 	public static double getTotalDurationInHours(
 			final ITimeSliceFilter timeSliceFilter) {
 		final String context = "TimeSliceRepository.getTotalDurationInHours("
 				+ timeSliceFilter + ")";
 
 		final SqlFilter sqlFilter = TimeSliceRepository.createFilter(context,
 				timeSliceFilter);
 		Cursor cur = null;
 		double result = -1.0;
 		try {
 			cur = TimeSliceRepository.CURRENT_DB_INSTANCE.getDb().query(
 					DatabaseHelper.TIME_SLICE_TABLE,
 					new String[] { "SUM(" + TimeSliceSql.COL_END_TIME + "-"
 							+ TimeSliceSql.COL_START_TIME + ")" },
 					sqlFilter.sql, sqlFilter.args, null, null, null);
 			if ((cur != null) && (cur.moveToFirst())) {
 				result = cur.getLong(0) / (1000.0 * 60 * 60);
 			}
 		} catch (final Exception ex) {
 			throw new TimeTrackerDBException(context, sqlFilter, ex);
 		} finally {
 			if (cur != null) {
 				cur.close();
 			}
 			if (Global.isDebugEnabled()) {
 				Log.d(Global.LOG_CONTEXT,
 						sqlFilter
 								.getDebugMessage("TimeSliceRepository.getTotalDurationInHours("
 										+ timeSliceFilter + ") = " + result));
 			}
 		}
 		return result;
 
 	}
 
 	private TimeSlice fillTimeSliceFromCursor(final Cursor cur) {
 		final int categoryID = cur.getInt(cur
 				.getColumnIndexOrThrow(TimeSliceSql.COL_CATEGORY_ID));
 
 		if (categoryID != TimeSliceCategory.NOT_SAVED) {
 			final TimeSlice ts = new TimeSlice();
 			ts.setRowId(cur.getInt(cur.getColumnIndexOrThrow("_id")));
 			ts.setStartTime(cur.getLong(cur
 					.getColumnIndexOrThrow(TimeSliceSql.COL_START_TIME)));
 			ts.setEndTime(cur.getLong(cur
 					.getColumnIndexOrThrow(TimeSliceSql.COL_END_TIME)));
 			ts.setCategory(this.categoryRepository.fetchByRowID(categoryID));
 			ts.setNotes(cur.getString(cur
 					.getColumnIndexOrThrow(TimeSliceSql.COL_NOTES)));
 			return ts;
 		}
 
 		Log.w(Global.LOG_CONTEXT, "Ignoring timeslice with categoryID="
 				+ TimeSliceCategory.NOT_SAVED);
 		return null;
 	}
 
 	private String[] columnList() {
 		final List<String> columns = new ArrayList<String>();
 		columns.add("_id");
 		columns.add(TimeSliceSql.COL_CATEGORY_ID);
 		columns.add(TimeSliceSql.COL_START_TIME);
 		columns.add(TimeSliceSql.COL_END_TIME);
 		columns.add(TimeSliceSql.COL_NOTES);
 		return columns.toArray(new String[0]);
 	}
 
 	private ContentValues timeSliceContentValuesList(final TimeSlice timeSlice) {
 		final ContentValues values = new ContentValues();
 		values.put(TimeSliceSql.COL_CATEGORY_ID, timeSlice.getCategoryId());
 		values.put(TimeSliceSql.COL_START_TIME, timeSlice.getStartTime());
 		values.put(TimeSliceSql.COL_END_TIME, timeSlice.getEndTime());
 		values.put(TimeSliceSql.COL_NOTES, timeSlice.getNotes());
 		return values;
 	}
 
 	private static SqlFilter createFilter(final String debugContext,
 			final ITimeSliceFilter timeSliceFilter) {
 		final SqlFilter sqlFilter = TimeSliceSql.createFilter(timeSliceFilter);
 
 		final String context = "TimeSliceRepository.createFilter("
 				+ debugContext + "," + timeSliceFilter + ")";
 		Log.d(Global.LOG_CONTEXT,
 				(sqlFilter != null) ? sqlFilter.getDebugMessage(context)
 						: context);
 
 		return sqlFilter;
 	}
 }
