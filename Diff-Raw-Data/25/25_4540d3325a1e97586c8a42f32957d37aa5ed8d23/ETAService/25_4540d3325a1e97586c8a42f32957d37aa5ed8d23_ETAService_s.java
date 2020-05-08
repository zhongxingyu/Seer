 package com.cheetah.controller;
 
 import roboguice.RoboGuice;
 import roboguice.event.EventManager;
 import roboguice.inject.ContextScope;
 import roboguice.service.RoboIntentService;
 import roboguice.service.event.OnCreateEvent;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.ToneGenerator;
 import android.os.IBinder;
 
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 
 import de.akquinet.android.androlog.Log;
 
 public class ETAService extends RoboIntentService {
 
   @Inject
   RetreivesRouteResult retreivesRouteResult;
 
   public ETAService() {
     super("hello");
   }
 
   @Override
   public IBinder onBind(final Intent arg0) {
     return null;
   }
 
   @Override
   public void onDestroy() {
     super.onDestroy();
   }
 
   @Override
   public int onStartCommand(final Intent intent, final int flags, final int startId) {
     handleCommand(intent);
     return START_STICKY;
   }
 
   private void handleCommand(final Intent intent) {
     Log.i("@ handleCommand retreivesRouteResult=" + retreivesRouteResult);
 
     final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
     tg.startTone(ToneGenerator.TONE_PROP_BEEP);
    retreivesRouteResult.
   }
 
   @Override
   public void onCreate() {
     final Injector injector = getInjector();
     eventManager = injector.getInstance(EventManager.class);
     final ContextScope scope = injector.getInstance(ContextScope.class);
     scope.enter(this);
     injector.injectMembers(this);
     super.onCreate();
     Log.i("@ MyUpdateService.onCreate");
 
     super.onCreate();
     eventManager.fire(new OnCreateEvent());
 
   }
 
   private Injector getInjector() {
     return RoboGuice.getInjector(this);
   }
 
   @Override
   protected void onHandleIntent(final Intent intent) {
   }
 
 }
