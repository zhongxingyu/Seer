 package com.zen.droidparts.ui.activity;
 
 import android.os.Bundle;
 import android.support.v7.app.ActionBarActivity;
 
 import butterknife.Views;
 import de.greenrobot.event.EventBus;
 
 public abstract class BaseActivity extends ActionBarActivity {
     public interface Events {
         class ReloadEvent {
 
         }
     }
 
     private EventBus eventBus;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setEventBus(EventBus.getDefault());
 
         int contentResourse = getContentViewResource();
         if (contentResourse > 0) {
             setContentView(contentResourse);
         }
 
         Views.inject(this);
 
         afterCreateView(savedInstanceState);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         getEventBus().register(this);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         getEventBus().unregister(this);
     }
 
     public void onEvent(Events.ReloadEvent reloadEvent) {
        
     }
 
     protected abstract int getContentViewResource();
 
     protected abstract void afterCreateView(Bundle savedInstanceState);
 
     public EventBus getEventBus() {
         return eventBus;
     }
 
     public void setEventBus(EventBus eventBus) {
         this.eventBus = eventBus;
     }
 }
