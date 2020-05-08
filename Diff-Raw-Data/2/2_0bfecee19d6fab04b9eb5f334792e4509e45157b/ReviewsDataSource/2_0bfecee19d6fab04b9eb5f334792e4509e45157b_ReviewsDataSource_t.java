 package com.cloudbees.gasp.model;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by markprichard on 7/14/13.
  */
 public class ReviewsDataSource {
     private static final String TAG = ReviewsDataSource.class.getName();
 
     // Database fields
     private SQLiteDatabase database;
     private ReviewsSQLiteHelper dbHelper;
     private String[] allColumns = { ReviewsSQLiteHelper.COLUMN_ID,
                                     ReviewsSQLiteHelper.COLUMN_RESTAURANT_ID,
                                     ReviewsSQLiteHelper.COLUMN_USER_ID,
                                     ReviewsSQLiteHelper.COLUMN_COMMENT,
                                     ReviewsSQLiteHelper.COLUMN_STAR };
 
     public ReviewsDataSource(Context context) {
         dbHelper = new ReviewsSQLiteHelper(context);
     }
 
     public void open() throws SQLException {
         database = dbHelper.getWritableDatabase();
     }
 
     public void close() {
         dbHelper.close();
     }
 
     public void insertReview(Review review) {
         ContentValues values = new ContentValues();
         values.put(ReviewsSQLiteHelper.COLUMN_ID, review.getId());
         values.put(ReviewsSQLiteHelper.COLUMN_RESTAURANT_ID, review.getRestaurant_id());
         values.put(ReviewsSQLiteHelper.COLUMN_USER_ID, review.getUser_id());
         values.put(ReviewsSQLiteHelper.COLUMN_COMMENT, review.getComment());
         values.put(ReviewsSQLiteHelper.COLUMN_STAR, review.getStar());
         long insertId = database.insert(ReviewsSQLiteHelper.REVIEWS_TABLE, null,
                 values);
         if (insertId != -1) {
             Log.d(TAG, "Inserted review with id: " + insertId);
         } else {
             Log.e(TAG, "Error inserting review with id: " + review.getId());
         }
     }
 
     public void deleteReview(Review Review) {
         long id = Review.getId();
         Log.d(TAG, "Deleting review with id: " + id);
         database.delete(ReviewsSQLiteHelper.REVIEWS_TABLE, ReviewsSQLiteHelper.COLUMN_ID
                 + " = " + id, null);
     }
 
     public List<Review> getAllReviews() {
         List<Review> Reviews = new ArrayList<Review>();
 
         Cursor cursor = database.query(ReviewsSQLiteHelper.REVIEWS_TABLE,
                 allColumns, null, null, null, null, null);
 
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             Review review = cursorToReview(cursor);
             Reviews.add(review);
             cursor.moveToNext();
         }
         cursor.close();
         return Reviews;
     }
 
     public List<String> getAllReviewsAsStrings() {
         List<String> reviewStrings = new ArrayList<String>();
 
         Cursor cursor = database.query(ReviewsSQLiteHelper.REVIEWS_TABLE,
                 allColumns, null, null, null, null, null);
 
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             Review review = cursorToReview(cursor);
             reviewStrings.add(review.toString());
             cursor.moveToNext();
         }
         cursor.close();
         return reviewStrings;
     }
 
     private Review cursorToReview(Cursor cursor) {
         Review Review = new Review();
         Review.setId(cursor.getInt(0));
         Review.setRestaurant_id(cursor.getInt(1));
         Review.setUser_id(cursor.getInt(2));
         Review.setComment(cursor.getString(3));
        Review.setStar(cursor.getInt(4));
         return Review;
     }
 }
