 package edu.uiuc.whosinline.database;
 
 import edu.uiuc.whosinline.data.Review;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 public class DatabaseAccessObjReview {
 	
 	private SQLiteDatabase database;
 	private SQLiteHelperReview dbHelper;
 	
 	private String[] columnNames = {SQLiteHelperReview.COLUMN_ID,
 			SQLiteHelperReview.COLUMN_REVIEW_VENUE_NAME,
 			SQLiteHelperReview.COLUMN_REVIEW_TITLE,
 			SQLiteHelperReview.COLUMN_REVIEW_CONTENT,
 			SQLiteHelperReview.COLUMN_REVIEW_RATING,
 			SQLiteHelperReview.COLUMN_REVIEW_USER_NAME};
 	
 	public DatabaseAccessObjReview(Context context) {
 		dbHelper = new SQLiteHelperReview(context);
 	}
 
 	public boolean isOpen() {
 		return database.isOpen();
 	}
 	
 	public void open() {
 		database = dbHelper.getWritableDatabase();
 	}
 
 	public void close() {
 		dbHelper.close();
 	}
 
 	public int getTableRowCount() {
 		String statement = "SELECT Count(*) FROM " +
 				SQLiteHelperReview.TABLE_NAME_REVIEW;
 		Cursor cursor = database.rawQuery(statement, null);
 		cursor.moveToFirst();
 		return cursor.getInt(0);
 	}
 	
 	public Cursor getReviewsForVenue(String venueName) {
 		String statement = "SELECT * FROM " + SQLiteHelperReview.TABLE_NAME_REVIEW
 				+ " WHERE " + SQLiteHelperReview.COLUMN_REVIEW_VENUE_NAME + "=\""
 				+ venueName + "\"";
 		Cursor cursor = database.rawQuery(statement, null);
 		if (cursor.moveToFirst()) {
 			return cursor;
 		}
 		return null;
 	}
 	
 	/**
 	 * Insert a new review into the SQLite database.
 	 * 
 	 * @param review a {@link Review} object.
 	 */
 	public void insertReview(Review review) {
 		ContentValues values = new ContentValues();
 
 		values.put(SQLiteHelperReview.COLUMN_REVIEW_VENUE_NAME, review.getVenueName());
 		values.put(SQLiteHelperReview.COLUMN_REVIEW_TITLE, review.getTitle());
 		values.put(SQLiteHelperReview.COLUMN_REVIEW_CONTENT, review.getContent());
 		values.put(SQLiteHelperReview.COLUMN_REVIEW_RATING, review.getRating());
 		values.put(SQLiteHelperReview.COLUMN_REVIEW_USER_NAME, review.getUserName());
 
 		database.insert(SQLiteHelperReview.TABLE_NAME_REVIEW, null, values);
 	}
 
 	/**
 	 * Delete a review from the SQLite database.
 	 * 
 	 * @param review a {@link Review} object.
 	 */
 	public void deleteReview(Review review) {
 		int id = review.getId();
 
 		database.delete(SQLiteHelperReview.TABLE_NAME_REVIEW,
 				SQLiteHelperReview.COLUMN_ID + "=" + id, null);
 	}
 	
 	public Cursor getCursor(boolean reverse) {
 		String orderBy = SQLiteHelperReview.COLUMN_ID;
 		if (reverse) {
 			orderBy += " DESC";
 		}
 		
 		Cursor cursor = database.query(SQLiteHelperReview.TABLE_NAME_REVIEW,
 				columnNames, null, null, null, null, orderBy);
 
 		cursor.moveToFirst();
 
 		return cursor;
 	}
 	
 	/**
 	 * Helper method to convert a {@link Cursor} object to a {@link Review} object.
 	 * 
 	 * @param cursor a {@link Cursor} object.
 	 * @return a {@link Review} object.
 	 */
 	public static Review cursorToReview(Cursor cursor) {
 		Review review = new Review(cursor.getInt(0), cursor.getString(1),
 				cursor.getString(1), cursor.getString(1), cursor.getFloat(5),
 				cursor.getString(1));
 		return review;
 	}
 }
