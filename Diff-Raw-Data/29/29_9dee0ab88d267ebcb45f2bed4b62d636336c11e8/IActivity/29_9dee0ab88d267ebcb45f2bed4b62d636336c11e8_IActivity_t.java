 package karyon.android.activities;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.view.View;
 import android.view.Window;
 
 /**
  * The IActivity is the interface for activities
  * of any type
  * @param <T> the inner type of the activity
  */
 public interface IActivity<T extends IActivity>
 {
     /**
      * Determines if this Activity class will show the title area
      * or not
      * @return true to show the title area, false to hide it
      */
     boolean canShowTitle();
 
     /**
      * Sets the flag for the specified window feature
      * @param tnFeature the feature to turn on
      * @return true if the feature is supported and enabled after this call
      */
     boolean setWindowFeature(int tnFeature);
 
     /**
      * Gets the id for the drawable resource that should
      * be used as the custom drawable title
      * @return the custom drawable title id, 0 if none
      */
     int getCustomTitleDrawable();
 
     /**
      * Gets the window for the activity
      * @return the window for this activity
      */
     Window getWindow();
 
     /**
      * Initialises the activity.  This is called after onCreate but before
      * the next lifescycle stage
      * @param toSavedState the state to restore from, or null if there is no restore state
      * @return true if this activity was correctly initialised
      */
     boolean onInit(Bundle toSavedState);
 
     /**
      * Hook method to allow direct interaction with the activity lifecycle.
      * Occurs after onRestart is called
      */
     void onRestarted();
 
     /**
      * Hook method to allow direct interaction with the activity lifecycle.
      * Occurs just before onStart is called
      */
     void onStarted();
 
     /**
      * Hook method to allow direct interaction with the activity lifecycle.
      * Occurs just before onResume is called
      */
     void onResumed();
 
     /**
      * Hook method to allow direct interaction with the activity lifecycle.
      * Occurs just before onPause is called
      */
     void onPaused();
 
     /**
      * Hook method to allow direct interaction with the activity lifecycle.
      * Occurs just before onStop is called
      */
     void onStopped();
 
     /**
      * Hook method to allow direct interaction with the activity lifecycle.
      * Occurs just before onDestroy is called
      */
     void onDestroyed();
 
     /**
      * Hook method to allow direct interaction with the activity lifecycle.
      * Occurs just before onLowMemory is called
      */
     void onMemoryLow();
 
     /**
      * Sets teh activity content from a layout resource.  The layout will be inflated adding all top=level views
      * to the activity
      * @param tnLayoutID the resource ID to inflate
      */
     void setContentView(int tnLayoutID);
 
     /**
      * Gets the resources instance for the applications package
      * @return the instance of the applications resources
      */
     Resources getResources();
 
     /**
      * Gets the content view for this activity based on the current orientation
      * @return the layout id to fill the content with
      */
     int getContentViewID();
 
     /**
      * Gets the content view if this activity is in landscape orientation
      * @return the landscape resource id
      */
     int getLandscapeViewResourceID();
 
     /**
      * Gets the content view if this activity is in portrait or square orientation
      * @return the portrait resource id
      */
     int getPortraitViewResourceID();
 
     /**
     * Sets the orientation of the device to the specified orientation
     * @param tnOrientation the new orientation
     */
    void setRequestedOrientation(int tnOrientation);

    /**
      * This should be called when the activity is complete
       */
     void finish();
 
     /**
      * This is called to allow interaction with the activity lifecycle.  This will be called
      * after any content changes have been made to the activity
      */
     void onContentReady();
 
     /**
      * Adjustments to the UI can be made here, this will be called only when the UI needs to be
      * refreshed
      */
     void onUpdateUI();
 
     /**
      * Ensures the action specified is executed on the UI thread
      * @param toAction the action to execute
      */
     void runOnUiThread(Runnable toAction);
 
     /**
      * Helper method for those who forget what the context is
      * @return gets this activity as a context reference
      */
     Context getContext();
 
     /**
      * Checks if this activity is currently paused
      * @return true if the activity is paused
      */
     boolean isPaused();
 
     /**
      * Checks if this activity is currently finishing
      * @return true if the activity is finishing
      */
     boolean isFinishing();
 
     /**
      * Finds a view that was identified by the id attribute from the XML that was processed in onCreate
      * @param tnID the id of the view to find
      * @return the view, or null if no view could be found
      */
     View findViewById(int tnID);
 
     /**
      * Gets the Fragment Manager for interacting with fragments associated with this activity
      * @return the fragment manger
      */
     FragmentManager getSupportFragmentManager();
 
     /**
      * Invalidates the entire activity.
      */
     void invalidate();
 }
