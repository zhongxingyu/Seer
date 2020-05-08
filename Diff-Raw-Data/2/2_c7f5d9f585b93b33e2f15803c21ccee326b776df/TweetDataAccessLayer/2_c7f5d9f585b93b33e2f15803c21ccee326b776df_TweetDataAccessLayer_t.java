 package pt.isel.pdm.yamba.dataAccessLayer;
 
 import java.sql.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import pt.isel.pdm.yamba.model.YambaPost;
 import pt.isel.pdm.yamba.model.YambaUser;
 import pt.isel.pdm.yamba.provider.contract.TweetContract;
 import winterwell.jtwitter.Status;
 import android.content.ContentProvider;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.database.Cursor;
 
 public class TweetDataAccessLayer {
     
     //Get Mehtods
     public static YambaPost getTweet(ContentProvider provider, long id){
         Cursor cursor = provider.query(
                 TweetContract.CONTENT_URI,
                 getProjection(),
                 String.format("%s = ?", TweetContract._ID),
                 new String[]{id+""},null);
 
         if(cursor != null && cursor.getCount() == 1){
             cursor.moveToNext();
             return getTwitterStatusFromCursor(provider,cursor);
         }
         return null;
     }
     public static List<YambaPost> getTweetsFrom(ContentProvider provider, long timestamp, int count){
         Cursor cursor = provider.query(
                 TweetContract.CONTENT_URI,
                 getProjection(),
                 String.format("%s > ?", TweetContract.TIMESTAMP),
                 new String[]{timestamp+""},null);
         
         if(cursor != null && cursor.getCount() > 0){
             List<YambaPost> toReturn = new LinkedList<YambaPost>();
             while (cursor.moveToNext()) {
                 toReturn.add(getTwitterStatusFromCursor(provider,cursor));
             }
             return toReturn;
         }
         return null;
     }
     public static List<YambaPost> getAllTweets(ContentProvider provider){
         Cursor cursor = provider.query(
                 TweetContract.CONTENT_URI,
                 getProjection(),
                 null,
                 null,
                 null);
         
         if(cursor != null && cursor.getCount() > 0){
             List<YambaPost> toReturn = new LinkedList<YambaPost>();
             while (cursor.moveToNext()) {
                 toReturn.add(getTwitterStatusFromCursor(provider,cursor));
             }
             return toReturn;
         }
         return null;
     }
     //Insert Methods
     public static void insertTweet(ContentResolver resolver, Status tweet){
         resolver.insert(TweetContract.CONTENT_URI, getContentValuesFromTwitterStatus(tweet));
     }
     //TwitterStatus aux methods
     private static String[] getProjection(){
         return new String[]{
                 TweetContract._ID,
                 TweetContract.USER,
                 TweetContract.TWEET,
                 TweetContract.DATE
         };
     }
     private static YambaPost getTwitterStatusFromCursor(ContentProvider provider,Cursor cursor){
         long id = cursor.getLong(cursor.getColumnIndex(TweetContract._ID));
         String username = cursor.getString(cursor.getColumnIndex(TweetContract.USER));
         String tweet = cursor.getString(cursor.getColumnIndex(TweetContract.TWEET));
         Date date = Date.valueOf(cursor.getString(cursor.getColumnIndex(TweetContract.DATE)));
         YambaUser user = UserDataAccessLayer.getUser(provider, username);
        return new YambaPost(id, user.getUsername(), date, tweet);
     }
     private static ContentValues getContentValuesFromTwitterStatus(Status tweet){
         ContentValues cv = new ContentValues();
         cv.put(TweetContract._ID,   tweet.getId().longValue() );
         cv.put(TweetContract.USER,  tweet.getUser().getName());
         cv.put(TweetContract.TWEET, tweet.getText());
         cv.put(TweetContract.DATE,  tweet.createdAt.toLocaleString());
         cv.put(TweetContract.TIMESTAMP, tweet.createdAt.getTime() );
         return cv;
     }
 }
