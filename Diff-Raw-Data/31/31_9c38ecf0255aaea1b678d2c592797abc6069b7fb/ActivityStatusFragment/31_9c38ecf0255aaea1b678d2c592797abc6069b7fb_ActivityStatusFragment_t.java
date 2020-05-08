 package com.geek.exercise.fragments;
 
 import android.app.*;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentSender;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.android.volley.Request;
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.JsonObjectRequest;
 import com.geek.exercise.Constants;
import com.geek.exercise.Logger;
 import com.geek.exercise.R;
 import com.geek.exercise.managers.StateManager;
 import com.geek.exercise.network.requests.MessageRequest;
 import com.geek.exercise.services.ActivityIntentService;
 import com.geek.exercise.transfer.Account;
 import com.geek.exercise.transfer.ActivityStatus;
 import com.geek.exercise.transfer.IStatus;
 import com.geek.exercise.transfer.Message;
 import com.geek.exercise.utilities.ActivityStatusUtils;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.location.ActivityRecognitionClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.Locale;
 
 /**
  * Created by Pequots34 on 7/8/13.
  */
 public class ActivityStatusFragment extends ListFragment implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
 
     public static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
 
     private static final int DETECTION_INTERVAL_SECONDS = 20;
 
     private static final int MILLISECONDS_PER_SECOND = 1000;
 
     private static final int DETECTION_INTERVAL_MILLISECONDS = MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;
 
     private static final String MESSAGE_REQUEST_TAG = "message";
 
     private static final String TAG = ActivityStatusFragment.class.getSimpleName();
 
     private IActivityStatusListener mActivityStatusListener;
 
     private ActivityRecognitionClient mActivityRecognitionClient;
 
     private PendingIntent mActivityRecognitionPendingIntent;
 
     private View mBanner;
 
     private TextView mMe;
 
     private TextView mType;
 
     private RequestQueue mRequestQueue;
 
     private ActivityStatusAdapter mActivityStatusAdapter;
 
     public ActivityStatusFragment() {
         super();
 
         mActivityRecognitionPendingIntent = null;
 
         mActivityRecognitionClient = null;
     }
 
     @Override
     public void onCreate( Bundle savedInstanceState ) {
         super.onCreate( savedInstanceState );
 
         setRetainInstance( true );
 
         if ( !StateManager.ApplicationManager.INSTANCE.isInitialized() ) {
             StateManager.ApplicationManager.INSTANCE.initialize( getActivity() );
         }
 
         mRequestQueue = StateManager.ApplicationManager.INSTANCE.getRequestQueue();
 
         mActivityStatusAdapter = new ActivityStatusAdapter( getActivity() );
     }
 
     @Override
     public void onAttach( Activity activity ) {
         super.onAttach( activity );
 
         try {
             mActivityStatusListener = (IActivityStatusListener) activity;
         } catch( ClassCastException e ) {
             throw new ClassCastException( e.toString() );
         }
     }
 
     @Override
     public void onActivityCreated( Bundle savedInstanceState ) {
         super.onActivityCreated( savedInstanceState );
 
         setListAdapter( mActivityStatusAdapter );
 
         try {
             JSONArray collection = getSavedState();
 
             if ( collection.length() > 0 ) {
                 for ( int i = 0; i < collection.length(); i ++ ) {
                     JSONObject data = collection.getJSONObject( i );
 
                     mActivityStatusAdapter.add(Message.newBuilder()
                             .setActivity(data.optString("activity", null))
                             .setUsername(data.optString("username", null))
                             .setElapsedRealtime(data.optLong("elapsed"))
                             .setTime(data.optLong("time"))
                             .setType(data.optInt("type"))
                             .build());
                 }
 
                 mActivityStatusAdapter.notifyDataSetChanged();
             }
         } catch ( JSONException e ) {
             Context context = getActivity();
 
             if ( context != null ) {
                 Toast.makeText( context, e.toString(), Toast.LENGTH_SHORT ).show();
             }
         }
     }
 
     @Override
     public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
         View view = inflater != null ? inflater.inflate( R.layout.fragment_activity_status, null, false ) :
                 LayoutInflater.from( getActivity() ).inflate(R.layout.fragment_activity_status, null, false) ;
 
         if ( view != null ) {
             mBanner = view.findViewById( R.id.banner );
 
             mType = (TextView) view.findViewById( R.id.type );
 
             mMe = (TextView) view.findViewById( R.id.me );
         }
 
         return view;
     }
 
     @Override
     public void onDestroy() {
         mRequestQueue.cancelAll( MESSAGE_REQUEST_TAG );
 
         super.onDestroy();
     }
 
     @Override
     public void onConnected( Bundle bundle ) {
         try {
             getActivityRecognitionClient().requestActivityUpdates( DETECTION_INTERVAL_MILLISECONDS, createRequestPendingIntent() );
         } catch( IllegalStateException e ) {}
     }
 
     @Override
     public void onDisconnected() {
         mActivityRecognitionClient = null;
     }
 
     @Override
     public void onConnectionFailed( ConnectionResult connectionResult ) {
         if ( connectionResult.hasResolution() ) {
             try {
                 connectionResult.startResolutionForResult( getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST );
             } catch( IntentSender.SendIntentException e ) {
                 Log.e(TAG, e.toString());
             }
         } else {
             Dialog dialog = GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
 
             if ( dialog != null ) {
                 dialog.show();
             }
         }
     }
 
     public void removeUpdates() {
         if ( mActivityRecognitionPendingIntent != null && getActivityRecognitionClient().isConnected() ) {
             try {
                 getActivityRecognitionClient().removeActivityUpdates( mActivityRecognitionPendingIntent );
 
                 mActivityRecognitionPendingIntent.cancel();
 
                 getActivityRecognitionClient().disconnect();
             } catch( IllegalStateException e ) {
                 Context context = getActivity();
 
                 if ( context != null ) {
                     Toast.makeText( context, e.toString(), Toast.LENGTH_SHORT ).show();
                 }
             }
         }
     }
 
     public void setCurrentActivity( final ActivityStatus activity ) {
         if ( activity != null ) {
             IStatus status = activity.getStatusByType();
 
             int color = status.getColorStateResource();
 
             mBanner.setBackgroundResource( status.getBannerResource() );
 
             mType.setText( status.getTextResource() );
 
             mMe.setTextColor( getResources().getColor( color ) );
 
             mType.setTextColor( getResources().getColor( color ) );
 
             Account account = StateManager.ApplicationManager.INSTANCE.getAccount();
 
             MessageRequest message = MessageRequest.newBuilder()
                     .setStatus( activity )
                     .setUsername( account != null ? account.getUsername() : null )
                     .build();
 
             JSONObject payload = message.toPayload();
 
             final JsonObjectRequest request = new JsonObjectRequest( Request.Method.POST, message.toURL(),
                     message.toJSONObject(), new Response.Listener<JSONObject>() {
 
                 @Override
                 public void onResponse( JSONObject data ) {
                     mActivityStatusListener.onActivityStatusPosted( activity );
                 }
 
             }, new Response.ErrorListener() {
 
                 @Override
                 public void onErrorResponse( VolleyError error ) {
                     Context context = getActivity();
 
                     if ( context != null ) {
                         Toast.makeText( context, error.getMessage(), Toast.LENGTH_SHORT ).show();
                     }
                 }
             } );
 
             request.setTag( MESSAGE_REQUEST_TAG );
 
             if ( ActivityStatusUtils.isOnTheMove( activity.getType() ) ) {
                 mActivityStatusAdapter.add( message.toMessage() );
 
                 saveStateToClient( payload );
             }
 
            mActivityStatusAdapter.setColorState( color );

            mActivityStatusAdapter.notifyDataSetChanged();

             Context context = getActivity();
 
             if ( context != null ) {
                 Toast.makeText( context, getString( R.string.network_posting_status ), Toast.LENGTH_SHORT ).show();
             }
 
            mRequestQueue.add( request );
         }
     }
 
     public void setRequestPendingIntent( PendingIntent intent ) {
         mActivityRecognitionPendingIntent = intent;
     }
 
     public PendingIntent getRequestPendingIntent() {
         return mActivityRecognitionPendingIntent;
     }
 
     public void requestUpdates() {
         Context context = getActivity();
 
         if ( context != null ) {
             Toast.makeText( context, getString( R.string.activity_starting_updates ), Toast.LENGTH_SHORT).show();
         }
 
         getActivityRecognitionClient().connect();
     }
 
     private JSONArray getSavedState() throws JSONException {
         SharedPreferences preferences = getSharedPreferences();
 
         if ( preferences != null ) {
             return new JSONArray( preferences.getString(Constants.PAYLOAD_EXTRA, new JSONArray().toString()) );
         }
 
         return new JSONArray();
     }
 
     private void saveStateToClient( JSONObject payload ) {
         SharedPreferences preferences = getSharedPreferences();
 
         if ( preferences != null ) {
             try {
                 JSONArray collection = getSavedState();
 
                 collection.put( payload );
 
                 preferences.edit()
                         .putString( Constants.PAYLOAD_EXTRA, collection.toString() )
                         .commit();
             } catch ( JSONException e ) {
                 Context context = getActivity();
 
                 if ( context != null ) {
                     Toast.makeText( context, e.toString(), Toast.LENGTH_SHORT ).show();
                 }
             }
         }
     }
 
     private SharedPreferences getSharedPreferences() {
         Context context = getActivity();
 
         if ( context != null ) {
             return context.getSharedPreferences(Constants.HISTORY_SHARED_PREFERENCES, Context.MODE_PRIVATE);
         }
 
         return null;
     }
 
     private PendingIntent createRequestPendingIntent() {
         if ( getRequestPendingIntent() != null ) {
             return mActivityRecognitionPendingIntent;
         } else {
             Context context = getActivity();
 
             if ( context != null ) {
                 Intent intent = new Intent( getActivity(), ActivityIntentService.class );
 
                 PendingIntent pendingIntent = PendingIntent.getService( context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
 
                 setRequestPendingIntent( pendingIntent );
 
                 return pendingIntent;
             }
         }
 
         return null;
     }
 
     private ActivityRecognitionClient getActivityRecognitionClient() {
         if ( mActivityRecognitionClient == null ) {
             mActivityRecognitionClient = new ActivityRecognitionClient( getActivity(), this, this );
         }
 
         return mActivityRecognitionClient;
     }
 
     public static interface IActivityStatusListener {
 
         public void onActivityStatusPosted( ActivityStatus status );
     }
 
     public static class ActivityStatusAdapter extends ArrayAdapter<Message> {
 
         private int mColorState;
 
         public ActivityStatusAdapter( Context context ) {
             super( context, -1 );
 
             mColorState = R.color.searching;
         }
 
         public void setColorState( int color ) {
             mColorState = color;
         }
 
         @Override
         public View getView( int position, View convertView, ViewGroup parent ) {
             ViewHolder holder = null;
 
             if ( convertView == null ) {
                 convertView = LayoutInflater.from( getContext() ).inflate( R.layout.status_item, null );
 
                 if ( convertView != null ) {
                     holder = new ViewHolder();
 
                     holder.status = (TextView) convertView.findViewById( R.id.status );
 
                     holder.time = (TextView) convertView.findViewById( R.id.time );

                    convertView.setTag( holder );
                 }
             } else {
                 holder = (ViewHolder) convertView.getTag();
             }
 
             Message message = getItem( position );
 
             if ( holder != null && message != null ) {
                int color;

                if (  ( position % 2 ) == 0 ) {
                    color = getContext().getResources().getColor( R.color.grey );
                } else {
                    color = getContext().getResources().getColor( R.color.dark_grey );
                }
 
                 convertView.setBackgroundColor( color );
 
                 holder.status.setTextColor( getContext().getResources().getColor( mColorState ) );
 
                 holder.time.setTextColor( getContext().getResources().getColor( mColorState ) );
 
                 holder.status.setText( ActivityStatusUtils.getActivityFromType( message.getType() ).toUpperCase( Locale.US ) );
             }
 
             return convertView;
         }
     }
 
     public static class ViewHolder {
 
         public TextView status;
 
         public TextView time;
 
     }
 }
