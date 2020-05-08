 package ru.rutube.RutubeAPI.models;
 
 import android.content.Context;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 import com.android.volley.NetworkResponse;
 import com.android.volley.Response;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.JsonObjectRequest;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ru.rutube.RutubeAPI.BuildConfig;
 import ru.rutube.RutubeAPI.R;
 import ru.rutube.RutubeAPI.requests.RequestListener;
 import ru.rutube.RutubeAPI.requests.Requests;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Сергей
  * Date: 03.05.13
  * Time: 21:07
  * To change this template use File | Settings | File Templates.
  */
 public class TrackInfo implements Parcelable {
     public static final String VIDEO_BALANCER = "video_balancer";
     public static final String STREAM_TYPE_M3U8 = "m3u8";
     public static final String STREAM_TYPE_JSON = "json";
     public static final String JSON_TRACK_ID = "track_id";
     public static final String JSON_RESULTS = "results";
     public static final String JSON_AUTHOR_ID = "id";
     public static final String JSON_TAGS = "tags";
     public static final String JSON_AUTHOR_NAME = "name";
     public static final String JSON_AUTHOR = "author";
     public static final String JSON_DURATION = "duration";
     private static final String LOG_TAG = TrackInfo.class.getName();
     private static final boolean D = BuildConfig.DEBUG;
     private List<VideoTag> mTags;
     private Author mAuthor;
 
     private Uri mBalancerUrl;
     private int mTrackId;
     private String mTitle;
     private int mDuration;
 
     public TrackInfo(Uri balancerUrl, int trackId, String title, List<VideoTag> tags, Author author,
                      int duration) {
         mBalancerUrl = balancerUrl;
         mTrackId = trackId;
         mTags = tags;
         mTitle = title;
         mAuthor = author;
         mDuration = duration;
     }
 
     public static TrackInfo fromParcel(Parcel parcel) {
         Uri balancerUrl = parcel.readParcelable(Uri.class.getClassLoader());
         int trackId = parcel.readInt();
         String title = parcel.readString();
         ArrayList tags_list = parcel.readArrayList(VideoTag.class.getClassLoader());
         List<VideoTag> tags = (ArrayList<VideoTag>) tags_list;
         Author author = parcel.readParcelable(Author.class.getClassLoader());
         int duration = parcel.readInt();
         return new TrackInfo(balancerUrl, trackId, title, tags, author, duration);
     }
 
     public static TrackInfo fromJSON(JSONObject data) throws JSONException {
         JSONObject balancer = data.getJSONObject(VIDEO_BALANCER);
         int trackId = data.getInt(JSON_TRACK_ID);
         String title = data.getString("title");
         List<VideoTag> tags = parseTags(data);
         Author author = parseAuthor(data);
         Uri streamUri = Uri.parse(balancer.getString(STREAM_TYPE_M3U8));
         int duration = data.getInt(JSON_DURATION);
         return new TrackInfo(streamUri, trackId, title, tags, author, duration);
     }
 
     protected static Author parseAuthor(JSONObject data) throws JSONException {
         data = data.optJSONObject(JSON_AUTHOR);
         if( data == null)
             return null;
         int id = data.getInt(JSON_AUTHOR_ID);
         String name = data.getString(JSON_AUTHOR_NAME);
         return new Author(null, id, name);
     }
 
     protected static List<VideoTag> parseTags(JSONObject data) throws JSONException {
         JSONArray tags_json = data.getJSONArray(JSON_TAGS);
         ArrayList<VideoTag> result = new ArrayList<VideoTag>(tags_json.length());
         for (int i=0; i<tags_json.length(); i++) {
             JSONObject item = tags_json.getJSONObject(i);
             result.add(i, VideoTag.fromJSON(item));
         }
         return result;
     }
 
     // Parcelable implementation
 
     @SuppressWarnings("UnusedDeclaration")
     public static final Parcelable.Creator<TrackInfo> CREATOR
             = new Parcelable.Creator<TrackInfo>() {
         public TrackInfo createFromParcel(Parcel in) {
             return TrackInfo.fromParcel(in);
         }
 
         public TrackInfo[] newArray(int size) {
             return new TrackInfo[size];
         }
     };
     @Override
     public int describeContents() {
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel parcel, int flags) {
         parcel.writeParcelable(mBalancerUrl, flags);
         parcel.writeInt(mTrackId);
         parcel.writeArray(mTags.toArray());
         parcel.writeParcelable(mAuthor, flags);
     }
 
     public String getTitle() {
         return mTitle;
     }
 
     public Uri getBalancerUrl() {
         return mBalancerUrl;
     }
 
     public int getTrackId(){
         return mTrackId;
     }
 
     public Author getAuthor() { return mAuthor; }
 
     public JsonObjectRequest getMP4UrlRequest(Context context, RequestListener listener) {
         String balancerUrl = mBalancerUrl.toString().replace(STREAM_TYPE_M3U8, STREAM_TYPE_JSON);
         Uri uri = Uri.parse(balancerUrl).buildUpon()
                 .appendQueryParameter("referer", context.getString(R.string.referer))
                 .build();
         if (D) Log.d(LOG_TAG, "Balancer Url:" + uri.toString());
         assert uri != null;
         JsonObjectRequest request = new JsonObjectRequest(uri.toString(),
                 null, getMP4UrlListener(listener), getErrorListener(Requests.BALANCER_JSON, listener));
         request.setShouldCache(false);
         request.setTag(Requests.PLAY_OPTIONS);
         return request;
     }
 
     private Response.Listener<JSONObject> getMP4UrlListener(final RequestListener listener){
         return new Response.Listener<JSONObject>() {
             @Override
             public void onResponse(JSONObject response) {
                 if (D) Log.d(LOG_TAG, "mp4Url response: " + String.valueOf(response));
                 try {
                     JSONArray results = response.getJSONArray(JSON_RESULTS);
                     Bundle bundle = new Bundle();
                     if (results.length() > 0) {
                         bundle.putString(Constants.Result.MP4_URL, results.get(0).toString());
                     }
                     listener.onResult(Requests.BALANCER_JSON, bundle);
                 } catch (JSONException e) {
                     RequestListener.RequestError error = new RequestListener.RequestError(e.getMessage());
                     listener.onRequestError(Requests.BALANCER_JSON, error);
                 }
             }
         };
     }
 
     private Response.ErrorListener getErrorListener(final int tag, final RequestListener requestListener) {
         return new Response.ErrorListener() {
             @Override
             public void onErrorResponse(VolleyError error) {
                 if (D) Log.d(LOG_TAG, "onErrorResponse");
                 NetworkResponse networkResponse = error.networkResponse;
                 Bundle bundle = new Bundle();
                 if (networkResponse != null) {
                     bundle.putInt(Constants.Result.BALANCER_ERROR, networkResponse.statusCode);
                 } else {
                     requestListener.onVolleyError(error);
                 }
                 requestListener.onResult(tag, bundle);
             }
         };
     }
 
     public int getDuration() {
         return mDuration;
     }
 
     public List<VideoTag> getTags() {
         return mTags;
     }
 }
