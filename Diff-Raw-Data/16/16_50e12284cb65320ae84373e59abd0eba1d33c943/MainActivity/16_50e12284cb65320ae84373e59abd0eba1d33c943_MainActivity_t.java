 package me.courbiere.chromecast.experiment;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v7.app.MediaRouteButton;
 import android.support.v7.media.MediaRouteSelector;
 import android.support.v7.media.MediaRouter;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.google.cast.ApplicationChannel;
 import com.google.cast.ApplicationMetadata;
 import com.google.cast.ApplicationSession;
 import com.google.cast.CastContext;
 import com.google.cast.CastDevice;
 import com.google.cast.MediaRouteAdapter;
 import com.google.cast.MediaRouteHelper;
 import com.google.cast.MediaRouteStateChangeListener;
 import com.google.cast.MessageStream;
 import com.google.cast.SessionError;
 
 import org.json.JSONObject;
 
 import java.io.IOException;
 
 public class MainActivity extends FragmentActivity implements MediaRouteAdapter {
     private static final String TAG = "MainActivity";
     private static final String APP_ID = "430570b4-300e-470d-8f56-bdfabc6420c7";
     private static final String APP_NAMESPACE = "me.courbiere.chromecast.experiment";
 
     private ApplicationSession mApplicationSession;
     private SessionListener mSessionListener;
     private MyMessageStream mMessageStream;
 
     private CastContext mCastContext;
     private CastDevice mSelectedDevice;
 
     private MediaRouter mMediaRouter;
     private MediaRouteSelector mMediaRouteSelector;
     private MediaRouter.Callback mMediaRouterCallback;
     private MediaRouteStateChangeListener mRouteStateListener;
     private MediaRouteButton mMediaRouteButton;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
         setContentView(R.layout.activity_main);
 
         mSessionListener = new SessionListener();
         mMessageStream = new MyMessageStream();
 
         mCastContext = new CastContext(this.getApplicationContext());
         MediaRouteHelper.registerMinimalMediaRouteProvider(mCastContext, this);
         mMediaRouter = MediaRouter.getInstance(this.getApplicationContext());
         mMediaRouteSelector = MediaRouteHelper.buildMediaRouteSelector(
                 MediaRouteHelper.CATEGORY_CAST, APP_ID, null);
         mMediaRouterCallback = new MyMediaRouterCallback();
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                 MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
     }
 
     @Override
     protected void onStop() {
         mMediaRouter.removeCallback(mMediaRouterCallback);
         super.onStop();
     }
 
     @Override
     protected void onDestroy() {
         MediaRouteHelper.unregisterMediaRouteProvider(mCastContext);
         mCastContext.dispose();
         super.onDestroy();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
         getMenuInflater().inflate(R.menu.main, menu);
         MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
         mMediaRouteButton = (MediaRouteButton) mediaRouteMenuItem.getActionView();
         mMediaRouteButton.setRouteSelector(mMediaRouteSelector);
 
         return true;
     }
 
     @Override
     public void onDeviceAvailable(CastDevice castDevice, String routeId,
             MediaRouteStateChangeListener listener) {
 
         Log.d(TAG, "onDeviceAvailable()");
 
         mSelectedDevice = castDevice;
         mRouteStateListener = listener;
         mApplicationSession = new ApplicationSession(mCastContext, mSelectedDevice);
         mApplicationSession.setListener(mSessionListener);
 
         try {
             mApplicationSession.startSession(APP_ID);
         } catch (IOException e) {
             Log.e(TAG, "Failed to open a session", e);
         }
     }
 
     @Override
     public void onSetVolume(double v) {
 
     }
 
     @Override
     public void onUpdateVolume(double v) {
 
     }
 
     private class MyMediaRouterCallback extends MediaRouter.Callback {
 
         @Override
         public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
             Log.d(TAG, "onRouteSelected()");
 
             MediaRouteHelper.requestCastDeviceForRoute(route);
         }
 
         @Override
         public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
             Log.d(TAG, "onRouteUnselected()");
 
             mSelectedDevice = null;
             mRouteStateListener = null;
 
             if (mApplicationSession != null && mApplicationSession.hasStarted()) {
                 try {
                     if (mApplicationSession.hasChannel()) {
                         //mMessageStream.leave() ?
                     }
 
                     mApplicationSession.endSession();
                 } catch (IOException e) {
                     Log.e(TAG, "Failed to end the session", e);
                 } catch (IllegalStateException e) {
                     Log.e(TAG, "Unable to end the session", e);
                 } finally {
                     mApplicationSession = null;
                 }
             }
         }
     }
 
     private class SessionListener implements ApplicationSession.Listener {
 
         @Override
         public void onSessionStarted(ApplicationMetadata applicationMetadata) {
             Log.d(TAG, "SessionListener.onSessionStarted()");
 
             if (!mApplicationSession.hasChannel()) {
 
                 // Application does not support channel.
                 Log.w(TAG, "Application does not support channel");
                 return;
             }
 
             ApplicationChannel channel = mApplicationSession.getChannel();
 
             if (channel == null) {
                 Log.w(TAG, "onSessionStarted: channel is null");
                 return;
             }
 
             channel.attachMessageStream(mMessageStream);
         }
 
         @Override
         public void onSessionStartFailed(SessionError sessionError) {
            if (sessionError != null) {
                Log.d(TAG, "SessionListener.onStartFailed: " + sessionError.toString());
            } else {
                Log.d(TAG, "SessionListener.onStartFailed");
            }
         }
 
         @Override
         public void onSessionEnded(SessionError sessionError) {
             if (sessionError != null) {
                 Log.d(TAG, "SessionListener.onEnded: " + sessionError.toString());
             } else {
                Log.d(TAG, "SessionListener.onEnded");
             }
         }
     }
 
     private class MyMessageStream extends MessageStream {
 
         protected MyMessageStream() {
             super(APP_NAMESPACE);
         }
 
         @Override
         public void onMessageReceived(JSONObject jsonObject) {
            Log.d(TAG, "onMessageReceived()");
         }
     }
 }
