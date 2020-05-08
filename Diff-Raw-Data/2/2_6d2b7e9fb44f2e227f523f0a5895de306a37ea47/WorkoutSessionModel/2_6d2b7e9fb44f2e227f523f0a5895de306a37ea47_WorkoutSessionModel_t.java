 package com.vorsk.crossfitr.models;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 
 /**
  * DAO for "workout_session" table.
  * 
  * Create a new instance and use the methods to interact with the database.
  * Data is returned as instances of WorkoutSessionRow where each column
  * is a publicly accessible property.
  * 
  * @author Vivek
  * @since 1.0
  */
 public class WorkoutSessionModel extends SQLiteDAO
 {
 	//// Constants
 	
 	// Table-specific columns
 	public static final String COL_WORKOUT    = "workout_id";
 	public static final String COL_SCORE      = "score";
 	public static final String COL_SCORE_TYPE = "score_type_id";
 	public static final String COL_CMNT       = "comments";
 	
 	private Context context;
 	
 	
 	/*****   Constructors   *****/
 	
 	/**
 	 * Init SQLiteDAO with table "workout_session"
 	 * 
 	 * @param ctx In the example they passed "this" from the calling class..
 	 *            I'm not really sure what this is yet.
 	 */
 	public WorkoutSessionModel(Context ctx)
 	{
 		super("workout_session", ctx);
 		context = ctx;
 	}
 	
 	/*****   Private   *****/
 	
 	/**
 	 * Utility method to grab all the rows from a cursor
 	 * 
 	 * @param cr result of a query
 	 * @return Array of entries
 	 */
 	private WorkoutSessionRow[] fetchWorkoutSessionRows(Cursor cr)
 	{
 		if (cr == null) {
 			return null;
 		}
 		WorkoutSessionRow[] result = new WorkoutSessionRow[cr.getCount()];
 		if (result.length == 0) {
 			cr.close();
 			return result;
 		}
 		
 		boolean valid = cr.moveToFirst();
 		int ii = 0;
 		
 		// Grab the cursor's column indices
 		// An error here indicates the COL constants aren't synced with the DB
 		int ind_id    = cr.getColumnIndexOrThrow(COL_ID);
 		int ind_wid   = cr.getColumnIndexOrThrow(COL_WORKOUT);
 		int ind_score = cr.getColumnIndexOrThrow(COL_SCORE);
 		int ind_stid  = cr.getColumnIndexOrThrow(COL_SCORE_TYPE);
 		int ind_dm    = cr.getColumnIndexOrThrow(COL_MDATE);
 		int ind_dc    = cr.getColumnIndexOrThrow(COL_CDATE);
 		int ind_cmnt  = cr.getColumnIndexOrThrow(COL_CMNT);
 		
 		// Iterate over every row (move the cursor down the set)
 		while (valid) {
 			result[ii] = new WorkoutSessionRow();
 			fetchBaseData(cr, result[ii], ind_id, ind_dm, ind_dc);
 			result[ii].workout_id    = cr.getLong(ind_wid);
 			result[ii].score         = cr.getInt(ind_score);
 			result[ii].score_type_id = cr.getLong(ind_stid);
 			result[ii].comments      = cr.getString(ind_cmnt);
 		
 			valid = cr.moveToNext();
 			ii ++;
 		}
 		
 		cr.close();
 		return result;
 	}
 	
 	/**
 	 * Update the workout entry to reflect the new record
 	 * 
 	 * @param workout_id ID of the workout
 	 * @param score The new session's score
 	 */
 	private void checkUpdateRecord(long workout_id, int score)
 	{
 		WorkoutModel model = new WorkoutModel(context);
 		WorkoutRow workout = model.getByID(workout_id);
 		boolean update = false;
 
 		// Check if this session is the new best record
 		switch ((int)workout.record_type_id) {
 		case SCORE_TIME:
 			if (workout.record == 0 || score < workout.record) {
 				workout.record = score;
 				update = true;
 			}
 			break;
 		case SCORE_REPS:
 		case SCORE_WEIGHT:
 			if (workout.record == 0 || score > workout.record) {
 				workout.record = score;
 				update = true;
 			}
 			break;
 		}
 		
 		// If so, update the workout
 		if (update) {
 			model.edit(workout);
 		}
 	}
 	
 	/*****   Public   *****/
 	
 	/**
 	 * Inserts a new entry into the workout table
 	 * 
 	 * @param row Add this entry to the DB
 	 * @return ID of newly added entry, -1 on failure
 	 */
 	public long insert(WorkoutSessionRow row)
 	{
 		checkUpdateRecord(row.workout_id, row.score);
 		// Insert this entry
 		return super.insert(row.toContentValues());
 	}
 	
 	/**
 	 * Inserts a new entry into the workout table, defaults record to 0
 	 * 
 	 * @param workout ID of the workout performed this session
 	 * @param score The entry's score (time, reps, etc) or this.NOT_SCORED
 	 * @param score_type Type of the score (this.SCORE_TIME, etc) or
 	 *                   this.SCORE_NONE if no score is recorded
 	 * @return ID of newly added entry, -1 on failure
 	 */
 	public long insert(long workout, long score, long score_type)
 	{
 		Integer isc = (score == NOT_SCORED) ? null : (int)score;
 		Long ist = (score_type == SCORE_NONE) ? null : score_type;
 		
 		checkUpdateRecord(workout, isc);
 		ContentValues cv = new ContentValues();
 		cv.put(COL_WORKOUT, workout);
 		cv.put(COL_SCORE, isc);
 		cv.put(COL_SCORE_TYPE, ist);
 		return super.insert(cv);
 	}
 	
 	/**
 	 * Change the comment of a session
 	 * 
 	 * @param id ID of the comment to edit
 	 * @param comment New comment; overwrites existing comment
 	 * @return 1 on success, -1 on failure, 0 if invalid ID
 	 */
 	public int editComment(long id, String comment)
 	{
 		if (comment == null) comment = "";
 		
 		ContentValues cv = new ContentValues();
 		cv.put(COL_CMNT, comment);
 		return super.update(cv, COL_ID + " = " + id);
 	}
 	
 	/**
 	 * Removes all sessions of a particular workout
 	 * 
 	 * @param id ID of the workout whose history to remove
 	 * @return Number of sessions removed
 	 */
 	public int deleteWorkoutHistory(long id)
 	{
 		return super.delete(COL_WORKOUT + " = " + id);
 	}
 	
 	/**
 	 * Remove a previously created session
 	 * 
 	 * Currently used by ResultsActivity is you don't want to save. This
 	 * should be cleaned up so this method can be removed.
 	 * 
 	 * @param id session_id to delete
 	 * @return result of deletion, -1 on failure
 	 */
 	public int delete(long id)
 	{
 		WorkoutModel model = new WorkoutModel(context);
 		WorkoutSessionRow session = getByID(id);
 		if (session == null) {
 			return 0;
 		}
 		WorkoutRow workout = model.getByID(session.workout_id);
 		
 		int result = super.delete(COL_ID + " = " + id);
 		
 		if (workout.record == session.score) {
			model.calculateRecord(workout._id, workout.record_type_id);
 		}
 
 		return result;
 	}
 	
 	/**
 	 * Fetch an entry via the ID
 	 * 
 	 * @param id
 	 * @return Associated entry or NULL on failure
 	 */
 	public WorkoutSessionRow getByID(long id)
 	{
 		Cursor cr = selectByID(id);
 		
 		if (cr.getCount() > 1) {
 			return null; // TODO: Throw exception
 		}
 		
 		WorkoutSessionRow[] rows = fetchWorkoutSessionRows(cr);
 		return (rows.length == 0) ? null : rows[0];
 	}
 	
 	/**
 	 * Fetch all workouts within a given time period
 	 *
 	 * @param mintime Beginning time of interval (unix timestamp)
 	 * @param maxtime End time of interval (unix timestamp)
 	 * @return Sessions within the time period; NULL on failure
 	 */
 	public WorkoutSessionRow[] getByTime(int mintime, int maxtime)
 	{
 		String sql = "SELECT * FROM " + DB_TABLE + " WHERE "
 			+ COL_CDATE + "> ? AND " + COL_CDATE + "< ?";
 		String[] params = {
 			String.valueOf(mintime), String.valueOf(maxtime)
 		};
 		
 		Cursor cr = db.rawQuery(sql, params);
 		return fetchWorkoutSessionRows(cr);
 	}
 	
 	/**
 	 * Fetch all workouts within a given time period of a given type
 	 *
 	 * @param mintime Beginning time of interval (unix timestamp)
 	 * @param maxtime End time of interval (unix timestamp)
 	 * @param type Workout type; use constants (TYPE_GIRL, etc)
 	 */
 	public WorkoutSessionRow[] getByTime(int mintime, int maxtime, int type)
 	{
 		String sql = "SELECT * FROM " + DB_TABLE + " ws WHERE "
 			+ COL_CDATE + "> ? AND " + COL_CDATE + "< ? AND "
 			+ "(SELECT " + WorkoutModel.COL_WK_TYPE + " FROM workout WHERE "
 			+ COL_ID + "=ws." + COL_WORKOUT + ") = ?";
 		
 		Cursor cr = db.rawQuery(sql, new String[] {
 				String.valueOf(mintime),
 				String.valueOf(maxtime), 
 				String.valueOf(type)
 		});
 		return fetchWorkoutSessionRows(cr);
 	}
 	
 	/**
 	 * Fetch all workout sessions by type
 	 *
 	 * @param type Workout type; use constants (TYPE_GIRL, etc)
 	 * @return Sessions of that workout type; null on failure
 	 */
 	public WorkoutSessionRow[] getByType(int type)
 	{
 		String sql = "SELECT * FROM " + DB_TABLE + " ws WHERE "
 			+ "(SELECT " + WorkoutModel.COL_WK_TYPE + " FROM workout WHERE "
 			+ COL_ID + "=ws." + COL_WORKOUT + ") = ?";
 		
 		Cursor cr = db.rawQuery(sql, new String[] { String.valueOf(type) });
 		return fetchWorkoutSessionRows(cr);
 	}
 	
 	/**
 	 * Fetch sessions of a particular workout
 	 * 
 	 * @param id ID of the workout; obtain from WorkoutModel
 	 * @return Sessions of that workout; null on failure
 	 */
 	public WorkoutSessionRow[] getByWorkout(long id)
 	{
 		String col[] = { COL_WORKOUT };
 		String val[] = { String.valueOf(id) };
 		Cursor cr = select(col, val);
 		return fetchWorkoutSessionRows(cr);
 	}
 	
 	/**
 	 * Gets the total number of sessions performed
 	 * 
 	 * @return Total sessions
 	 */
 	public int getTotal()
 	{
 		return selectCount(null, null);
 	}
 	
 	/**
 	 * Get the most recent session
 	 * 
 	 * @type Workout type, or NULL to search all sessions
 	 * @return Most recently created session; NULL on failure
 	 */
 	public WorkoutSessionRow getMostRecent(Integer type)
 	{
 		String[] col = (type == null) ? null : new String[1];
 		String[] val = (type == null) ? null : new String[1];
 		
 		if (type != null) {
 			col[0] = WorkoutModel.COL_WK_TYPE;
 			val[0] = type.toString();
 		}
 		
 		String order = COL_CDATE + " DESC";
 		
 		Cursor cr = select(col, val, order, 1);
 		
 		WorkoutSessionRow[] rows = fetchWorkoutSessionRows(cr);
 		if (rows == null || rows.length < 1) {
 			return null;
 		}
 		return rows[0];
 	}
 
 }
