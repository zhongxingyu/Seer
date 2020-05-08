 package pt.isel.pdm.yamba.dataAccessLayer;
 
 import java.sql.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.database.Cursor;
 
 import pt.isel.pdm.yamba.model.TweetToPost;
 import pt.isel.pdm.yamba.provider.contract.TweetPostContract;
 
 public class TweetToPostDataAccessLayer {
 
     //GetMethods
     public static List<TweetToPost> getTweetsToPost(ContentResolver resolver){
         Cursor cursor = resolver.query(
                 TweetPostContract.CONTENT_URI,
                 getProjection(),
                 null,
                 null,
                String.format("%s DESC", TweetPostContract.TIMESTAMP ) );
         List<TweetToPost> toReturn = new LinkedList<TweetToPost>();
         if(cursor != null && cursor.getCount() > 0){
             while (cursor.moveToNext()) {
                 toReturn.add(getTweetToPostFromCursor(cursor));
             }
         }
         return toReturn;
     }
     
     //Insert Methods
     public static void insertTweetToPost(ContentResolver resolver, TweetToPost tweet){
         resolver.insert(TweetPostContract.CONTENT_URI, getContentValuesFromTweetToPost(tweet));
     }
     
     //Delete Method
     public static boolean deleteTweetToPost(ContentResolver resolver, TweetToPost tweet){
         int deletedLines = resolver.delete( TweetPostContract.CONTENT_URI, TweetPostContract.TIMESTAMP + "=?", new String[] { String.valueOf( tweet.getDate().getTime() ) } );
         return deletedLines != 0;
     }
     
     //Aux methods
     private static String[] getProjection() {
         return new String[]{ TweetPostContract.TIMESTAMP, TweetPostContract.DATE, TweetPostContract.TWEET };
     }
     
     public static TweetToPost getTweetToPostFromCursor(Cursor cursor){
         Date date = new Date(cursor.getLong(cursor.getColumnIndex(TweetPostContract.TIMESTAMP) ) );
         String text = cursor.getString(cursor.getColumnIndex(TweetPostContract.TWEET));
         return new TweetToPost(date,text);
     }
     public static ContentValues getContentValuesFromTweetToPost(TweetToPost tweet){
         ContentValues cv = new ContentValues();
         cv.put(TweetPostContract.TIMESTAMP, tweet.getDate().getTime() );
         cv.put(TweetPostContract.DATE     , tweet.getDate().toLocaleString() );
         cv.put(TweetPostContract.TWEET    , tweet.getText());
         return cv;
     }
 }
