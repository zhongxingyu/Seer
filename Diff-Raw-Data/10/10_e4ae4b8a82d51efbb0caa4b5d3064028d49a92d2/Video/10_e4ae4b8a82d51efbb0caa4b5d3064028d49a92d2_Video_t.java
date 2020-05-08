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
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.UnsupportedEncodingException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import ru.rutube.RutubeAPI.BuildConfig;
 import ru.rutube.RutubeAPI.R;
 import ru.rutube.RutubeAPI.RutubeApp;
 import ru.rutube.RutubeAPI.requests.AuthJsonObjectRequest;
 import ru.rutube.RutubeAPI.requests.RequestListener;
 import ru.rutube.RutubeAPI.requests.Requests;
 
 /**
  * Created by tumbler on 16.06.13.
  */
 public class Video implements Parcelable {
     public final static SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 
     private static final String LOG_TAG = Video.class.getName();
     private static final boolean D = BuildConfig.DEBUG;
     private static final String JSON_CREATED = "created_ts";
     private static final String JSON_AUTHOR = "author";
     private static final String JSON_TITLE = "title";
     private static final String JSON_DESCIPTION = "description";
     private static final String JSON_THUMBNAIL_URL = "thumbnail_url";
     private static final String JSON_VIDEO_ID = "id";
     private static final String JSON_VIDEO_URL = "video_url";
     private static final String URI_SIGNATURE = "p";
     private static final String JSON_ACL_ACCESS = "acl_access";
     private static final String JSON_ALLOWED = "allowed";
     private static final String JSON_ACL_ERRCODE = "err_code";
     private static final String JSON_TRACKINFO_DETAIL = "detail";
     private static final String JSON_TRACKINFO_ID = "id";
     private static final String JSON_DURATION = "duration";
     private static final String JSON_HITS = "hits";
     private String mVideoId;
     private String mTitle;
     private String mDescription;
     private Date mCreated;
     private Uri mThumbnailUri;
     private Author mAuthor;
     private String mSignature;
     private TrackInfo mTrackInfo;
     private int mDuration;
     private int mHits;
 
 
     public Video(String videoId) {
         this(videoId, null);
     }
 
     public Video(String videoId, String signature) {
         this(videoId, signature, null, null, null, null, null, 0, 0);
     }
 
     protected Video(String videoId, String signature, String title, String description,
                     Date created, Uri thumbnailUri, Author author, int duration, int hits) {
         mVideoId = videoId;
         mTitle = title;
         mDescription = description;
         mCreated = created;
         mThumbnailUri = thumbnailUri;
         mAuthor = author;
         mSignature = signature;
         mDuration = duration;
         mHits = hits;
     }
 
     private static Date parseDate(String data) {
         try {
             return dtf.parse(data);
         } catch (ParseException e) {
             return new Date(0);
         }
     }
 
     public static Video fromJSON(JSONObject data) throws JSONException {
         Date created = parseDate(data.getString(JSON_CREATED));
         JSONObject author_json = data.optJSONObject(JSON_AUTHOR);
         Author author = null;
         if (author_json != null)
             author = Author.fromJSON(author_json);
         String title = data.getString(JSON_TITLE);
         String description = data.getString(JSON_DESCIPTION);
         Uri thumbnailUri = Uri.parse(data.getString(JSON_THUMBNAIL_URL));
         String videoId = data.getString(JSON_VIDEO_ID);
         if (D) Log.d(LOG_TAG, "Created item: " + videoId + " " + String.valueOf(created));
         Uri videoUri = Uri.parse(data.getString(JSON_VIDEO_URL));
         int duration = data.getInt(JSON_DURATION);
         int hits = data.getInt(JSON_HITS);
         return new Video(videoId, videoUri.getQueryParameter(URI_SIGNATURE), title, description,
                 created, thumbnailUri, author, duration, hits);
     }
 
     protected Response.Listener<JSONObject> getTrackInfoListener(final RequestListener requestListener) {
         return new Response.Listener<JSONObject>() {
             @Override
             public void onResponse(JSONObject response) {
                 try {
                     mTrackInfo = parseTrackInfo(response);
                     Bundle bundle = new Bundle();
                     bundle.putParcelable(Constants.Result.TRACKINFO, mTrackInfo);
                     requestListener.onResult(Requests.TRACK_INFO, bundle);
                 } catch (JSONException e) {
                     RequestListener.RequestError error = new RequestListener.RequestError(e.getMessage());
                     requestListener.onRequestError(Requests.TRACK_INFO, error);
                 }
             }
         };
     }
 
     protected Response.Listener<JSONObject> getPlayOptionsListener(final RequestListener requestListener) {
         return new Response.Listener<JSONObject>() {
             @Override
             public void onResponse(JSONObject response) {
                 try {
                     Boolean allowed = parseAllowed(response);
                     Integer errCode = parseErrCode(response);
                     Uri thumbnailUri = parsePlayThumbnailUri(response);
                     Bundle bundle = new Bundle();
                     bundle.putBoolean(Constants.Result.ACL_ALLOWED, allowed);
                     bundle.putInt(Constants.Result.ACL_ERRCODE, errCode);
                     bundle.putParcelable(Constants.Result.PLAY_THUMBNAIL, thumbnailUri);
                     requestListener.onResult(Requests.PLAY_OPTIONS, bundle);
                 } catch (JSONException e) {
                     RequestListener.RequestError error = new RequestListener.RequestError(e.getMessage());
                     requestListener.onRequestError(Requests.PLAY_OPTIONS, error);
                 }
             }
         };
     }
 
     protected Response.Listener<JSONObject> getYastListener() {
         return new Response.Listener<JSONObject>() {
             @Override
             public void onResponse(JSONObject response) {
 
             }
         };
     }
 
     private Integer parseTrackInfoError(JSONObject response) {
         try {
             JSONObject details = response.getJSONObject(JSON_TRACKINFO_DETAIL);
             return details.optInt(JSON_TRACKINFO_ID, 0);
         } catch (JSONException ignored) {
             return 0;
         }
     }
 
     private Uri parsePlayThumbnailUri(JSONObject response) throws JSONException {
         return Uri.parse(response.getString(JSON_THUMBNAIL_URL));
     }
 
     private Boolean parseAllowed(JSONObject response) throws JSONException {
         JSONObject acl = response.getJSONObject(JSON_ACL_ACCESS);
         return acl.optBoolean(JSON_ALLOWED, false);
     }
 
     private Integer parseErrCode(JSONObject response) throws JSONException {
         JSONObject acl = response.getJSONObject(JSON_ACL_ACCESS);
         return acl.optInt(JSON_ACL_ERRCODE, 0);
     }
 
     protected TrackInfo parseTrackInfo(JSONObject data) throws JSONException {
         if (D) Log.d(LOG_TAG, "Result: " + data.toString());
         return TrackInfo.fromJSON(data);
     }
 
     public JsonObjectRequest getTrackInfoRequest(Context context, RequestListener listener) {
         String trackInfoPath = String.format(context.getString(R.string.trackinfo_uri), mVideoId);
         String trackInfoUri = RutubeApp.getUrl(trackInfoPath);
         if (mSignature != null)
             trackInfoUri += String.format("?p=%s", mSignature);
         JsonObjectRequest request = new JsonObjectRequest(trackInfoUri,
                 null, getTrackInfoListener(listener), getErrorListener(Requests.TRACK_INFO, listener));
         request.setShouldCache(true);
         request.setTag(Requests.TRACK_INFO);
         if (D) Log.d(LOG_TAG, "Trackinfo URL: " + trackInfoUri);
         return request;
     }
 
     public JsonObjectRequest getVideoRequest(Context context, RequestListener listener) {
         String videoPath = String.format(context.getString(R.string.video_uri), mVideoId);
         String videoUri = RutubeApp.getUrl(videoPath);
         JsonObjectRequest request = new JsonObjectRequest(videoUri,
                 null, getVideoListener(listener), getErrorListener(Requests.VIDEO, listener));
         request.setShouldCache(true);
         request.setTag(Requests.VIDEO);
         return request;
     }
 
     protected Response.Listener<JSONObject> getVideoListener(final RequestListener listener) {
         return new Response.Listener<JSONObject>() {
             @Override
             public void onResponse(JSONObject response) {
                 try {
                     Video result = Video.fromJSON(response);
                     mTitle = result.getTitle();
                     mDescription = result.getDescription();
                     mThumbnailUri = result.getThumbnailUri();
                     mCreated = result.getCreated();
                     mAuthor = result.getAuthor();
                     Bundle bundle = new Bundle();
                     bundle.putParcelable(Constants.Result.VIDEO, result);
                     listener.onResult(Requests.VIDEO, bundle);
                 } catch (JSONException ignored) {}
 
             }
         };
 
     }
 
     private Response.ErrorListener getErrorListener(final int tag, final RequestListener requestListener) {
         return new Response.ErrorListener() {
             @Override
             public void onErrorResponse(VolleyError error) {
                 if (D) Log.d(LOG_TAG, "onErrorResponse");
                 try {
                     NetworkResponse networkResponse = error.networkResponse;
                     Bundle bundle = new Bundle();
                     if (networkResponse != null) {
                         String responseBody = new String(networkResponse.data, "utf-8");
                         JSONObject response = new JSONObject(responseBody);
                         Integer errCode = parseTrackInfoError(response);
                         bundle.putInt(Constants.Result.TRACKINFO_ERROR, errCode);
                     } else {
                         requestListener.onVolleyError(error);
                     }
 
                     requestListener.onResult(tag, bundle);
                 } catch (JSONException ignored) {
                     requestListener.onVolleyError(error);
                 } catch (UnsupportedEncodingException ignored) {
                     requestListener.onVolleyError(error);
                 }
             }
         };
     }
 
     public JsonObjectRequest getPlayOptionsRequest(Context context, RequestListener listener) {
         String playOptionsPath = String.format(context.getString(R.string.playoptions_uri), mVideoId);
         String playOptionsUrl = RutubeApp.getUrl(playOptionsPath);
         Uri uri = Uri.parse(playOptionsUrl).buildUpon()
                 .appendQueryParameter("referer", context.getString(R.string.referer))
                 .build();
         assert uri != null;
         JsonObjectRequest request = new AuthJsonObjectRequest(uri.toString(),
                 null, getPlayOptionsListener(listener), getErrorListener(Requests.PLAY_OPTIONS, listener),
                 User.loadToken(context));
         request.setShouldCache(true);
         request.setTag(Requests.PLAY_OPTIONS);
         if (D) Log.d(LOG_TAG, "Play Options URL: " + uri.toString());
         return request;
     }
 
     public JsonObjectRequest getYastRequest(Context context) {
         assert mTrackInfo != null;
         assert context != null;
         if (D)
             Log.d(LOG_TAG, "Context: " + String.valueOf(context) + " TI: " + String.valueOf(mTrackInfo));
         String yastUrl = String.format(context.getString(R.string.yastUri), mTrackInfo.getTrackId());
         Uri uri = Uri.parse(yastUrl).buildUpon()
                 .appendQueryParameter("referer", context.getString(R.string.referer))
                 .build();
         assert uri != null;
         if (D) Log.d(LOG_TAG, "Yast request: " + uri.toString());
         JsonObjectRequest request = new JsonObjectRequest(uri.toString(), null, getYastListener(), null);
         request.setTag(Requests.YAST_VIEWED);
         return request;
     }
 
     public String getVideoId() {
         return mVideoId;
     }
 
     public String getTitle() {
         return mTitle;
     }
 
     public String getDescription() {
         return mDescription;
     }
 
     public Date getCreated() {
         return mCreated;
     }
 
     public Uri getThumbnailUri() {
         return mThumbnailUri;
     }
 
     public Author getAuthor() {
         return mAuthor;
     }
 
     public int getDuration() {
         return mDuration;
     }
 
     public int getHits() {
         return mHits;
     }
 
 
     // Реализация Parcelable
 
     public static Video fromParcel(Parcel in) {
         String videoId = in.readString();
         String title = in.readString();
         String description = in.readString();
         Uri thumbnailUri = in.readParcelable(Uri.class.getClassLoader());
         String dateStr = in.readString();
        Date created = null;
        if (dateStr != null && !dateStr.isEmpty())
            created = parseDate(dateStr);
         Author author = in.readParcelable(Author.class.getClassLoader());
         int duration = in.readInt();
         int hits = in.readInt();
         return new Video(videoId, null, title, description, created, thumbnailUri, author, duration,
                 hits);
     }
 
     @Override
     public int describeContents() {
         return 0;
     }
 
     @Override
     public void writeToParcel(Parcel parcel, int flags) {
         parcel.writeString(mVideoId);
         parcel.writeString(mTitle);
         parcel.writeString(mDescription);
         parcel.writeParcelable(mThumbnailUri, flags);
        if (mCreated != null)
            parcel.writeString(dtf.format(mCreated));
        else
            parcel.writeString("");
         parcel.writeParcelable(mAuthor, flags);
         parcel.writeInt(mDuration);
         parcel.writeInt(mHits);
     }
 
     @SuppressWarnings("UnusedDeclaration")
     public static final Parcelable.Creator<Video> CREATOR
             = new Parcelable.Creator<Video>() {
         public Video createFromParcel(Parcel in) {
             return Video.fromParcel(in);
         }
 
         public Video[] newArray(int size) {
             return new Video[size];
         }
     };
 
     public String getHitsText(Context context) {
         if (mHits == 1)
             return formatHits(context, R.string.view1);
         int hits = mHits % 100;
         if (hits > 10 && hits < 20)
             return formatHits(context, R.string.views5);
         hits = hits % 10;
         if (hits == 1)
             return formatHits(context, R.string.views21);
         if (hits > 1 && hits < 5)
             return formatHits(context, R.string.views4);
         return formatHits(context, R.string.views5);
     }
 
     private String formatHits(Context context, int resourceId) {
         return String.format(context.getResources().getString(resourceId), mHits);
     }
 }
