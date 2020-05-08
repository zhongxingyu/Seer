 package com.angry_glass_studios.activity;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 
 import com.angry_glass_studios.R;
 import com.angry_glass_studios.models.NoteModel;
 import com.angry_glass_studios.views.NoteView;
 
 public class NoteActivity extends Activity {
 
   private static final String TAG = NoteActivity.class.getSimpleName();
 
   private NoteView view;
   private Handler handler;
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     model = NoteModel.getInstance();
 
     view = (NoteView) View.inflate(this, R.layout.activity_note, null);
     view.setViewListener(viewListener);
     setContentView(view);
 
    messageState = new UnlockedState(this);
 
     handler = new Handler();
   }
 
   @Override
   protected void onResume() {
     super.onResume();
   }
 
   @Override
   protected void onPause() {
     super.onPause();
     // Remove any handler callbacks if you use a handler
     // handler.removeCallbacks();
   }
 
   @Override
   protected void onDestroy() {
     super.onDestroy();
     view.destroy();
    messageState.dispose();
   }
 
   /**
    * TODO: Runnable to invoke model if necessary
    */
   private Runnable actionRun = new Runnable() {
     @Override
     public void run() {
       // TODO: action stuff can go here
       // update models and views if necessary as well or just not use a
       // runnable if a handler is not needed. This type of code is not
       // view event handling
       //
       // Alternatively, if you do not need a handler to schedule the
       // code invocation, just use regular methods
     }
   };
 
   /**
    * Receive events from the view.
    * The view takes user actions
    * The controller/activity responds to user actions
    */
   private NoteView.ViewListener viewListener = new NoteView.ViewListener() {
     // TODO: controller can call methods directly on the view here
     // just implement the interface defined in the view and for each
     // view event, update views and models as necessary
   };
 
   /**
    * Set the current message state.
    */
  private ControllerState messageState;
   protected void setMessageState(ControllerState messageState) {
     if (this.messageState != null) {
       this.messageState.dispose();
     }
     this.messageState = messageState;
   }
 
   private NoteModel model;
   public NoteModel getModel() {
     return model;
   }
 }
