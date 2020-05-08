 package karyon.android.activities;
 
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.ViewGroup;
 import android.view.Window;
 import karyon.applications.Application;
 import karyon.exceptions.InvalidOperationException;
 
 /**
  * The actual implementation behind any activity classes.
  * All activity types should contain an impl class
  *
  */
 public class ActivityImpl<T extends IActivity>
 {
     private boolean m_lPaused;
     private T m_oThis;
     private boolean m_lIsFinishing;
     private boolean m_lVisible;
     private Runnable m_oOutstandingUpdate;
 
     /**
      * Creates a new instance of the activity class.
      * Generally this should only be created by the constructor of
      * an activity
      * @param toActivity the activity that this implementation is for
      */
     public ActivityImpl(T toActivity)
     {
         setActivity(toActivity);
     }
 
     /**
      * Creates a new instance of the activity implementation.
      * setActivity must be called before the activities are used
      */
     public ActivityImpl()
     {
     }
 
     /**
      * Gets the activity associated with this implementation
      * @return the activity for this implementation
      */
     protected T getActivity()
     {
         return m_oThis;
     }
 
     /**
      * This is always called when the activity is first created.
      * This is the "entry point" to the activity life cycle
      * @param toSavedState if restoring from a previous state this will be populated,
      *                        otherwise null
      */
     public void onCreate(Bundle toSavedState)
     {
         // Custom title and window features must come before the user does anything
         boolean llCustomTitle = false;
         if (!m_oThis.canShowTitle())
         {
             // Hide the title area
             m_oThis.setWindowFeature(Window.FEATURE_NO_TITLE);
         }
         else
         {
             // We are showing a title
             if (m_oThis.getCustomTitleDrawable() != 0)
             {
                 llCustomTitle = m_oThis.setWindowFeature(Window.FEATURE_CUSTOM_TITLE);
             }
         }
 
         if (initialise(toSavedState))
         {
             if (llCustomTitle)
             {
                 m_oThis.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, m_oThis.getCustomTitleDrawable());
 
                 // Fix for removing additional padding from the title area
                 try
                 {
                     int lnTitleContainer = (Integer)Class.forName("com.android.internal.R$id").getField("title_container").get(null);
                     ViewGroup loGroup = ((ViewGroup)m_oThis.getWindow().findViewById(lnTitleContainer));
                     if (loGroup != null)
                     {
                         loGroup.setPadding(0,0,0,0);
                     }
                 }
                 catch (ClassNotFoundException ex)
                 {
                     Application.log(ex);
                 }
                 catch (NoSuchFieldException ex)
                 {
                     Application.log(ex);
                 }
                 catch (IllegalAccessException ex)
                 {
                     Application.log(ex);
                 }
             }
         }
     }
 
     /**
      * This is called after the activity has been stopped, before it is being started again.
      * Called before onStart
      */
     public void onRestart()
     {
         ActivityManager.getInstance().notify(NotificationType.RESTART, m_oThis);
         m_oThis.onRestarted();
     }
 
     /**
      * Called when the activity is becoming visible to the user, after either onCreate or onRestart.
      */
     public void onStart()
     {
         m_lVisible = true;
         /*
         // Start up Flurry
         if (Application.getInstance().usesFlurry())
         {
             FlurryAgent.onStartSession(m_oThis.getContext(), Application.getInstance().flurryAPIKey());
         }
         */
 
         onContentChanged();
         ActivityManager.getInstance().notify(NotificationType.START, m_oThis);
         m_oThis.onContentReady();
         m_oThis.onStarted();
     }
 
     /**
      * Called when the activity will start interacting with the user.  At this point your activity
      * is at the top of the activity stack, with the user input going to your activity
      */
     public void onResume()
     {
         m_lPaused = false;
         ActivityManager.getInstance().notify(NotificationType.RESUME, m_oThis);
         m_oThis.onResumed();
     }
 
     /**
      * Called when the system is about to start resuming a previous activity.  This is typically
      * used to commit unsaved changes to persistent data, stop animations and other things that may be consuming CPU.
      * Implementations of this method should be very quick as the next activity will not be resumed until this
      * method returns
      */
     public void onPause()
     {
         m_lPaused = true;
         ActivityManager.getInstance().notify(NotificationType.PAUSE, m_oThis);
         m_oThis.onPaused();
     }
 
     /**
      * Called when the activity is no longer visible to the user, because another activity has been resumed and is
      * covering this one.  This may happen either because a new activity is being started, an existing activity is
      * being brought in front of this one, or this one is being destroyed
      */
     public void onStop()
     {
         m_lVisible = false;
         /*
         // Stop the Flurry session
         if (Application.getInstance().usesFlurry())
         {
             FlurryAgent.onEndSession(m_oThis.getContext());
         }
         */
         ActivityManager.getInstance().notify(NotificationType.STOP, m_oThis);
         m_oThis.onStopped();
     }
 
     public void onLowMemory()
     {
         ActivityManager.getInstance().notify(NotificationType.LOW_MEMORY, m_oThis);
         m_oThis.onMemoryLow();
     }
 
     /**
      * The final call you receive before your activity is destroyed.  this can happen either because the activity
      * is finishing or because the system is temporarily destroying this instance of the activity to save
      * space.  You can distinguish between these two scenarios with the isFinishing method
      */
     public void onDestroy()
     {
         ActivityManager.getInstance().notify(NotificationType.DESTROY, m_oThis);
         m_oThis.onDestroyed();
     }
 
     /**
      * Initialises the activity, template method to ensure flexibility in inheritance
      * @param toSavedState the state that is being used to restore the activity
      */
     private boolean initialise(Bundle toSavedState)
     {
         if (ActivityManager.getInstance().add(m_oThis) &&
                 m_oThis.onInit(toSavedState))
         {
             ActivityManager.getInstance().notify(NotificationType.INIT, m_oThis);
             updateContentView();
             return true;
         }
         else
         {
             finish();
             return false;
         }
     }
 
     /**
      * Called to update the content view, sets the view in the
      * activity to the specified resource
      */
     public void updateContentView()
     {
         int lnContentID = getContentViewID();
         if (lnContentID != 0)
         {
             m_oThis.setContentView(lnContentID);
         }
     }
 
     /**
      * Hook to allow overriding of the default view resources
      * @return the integer ID of the layout resource to initialise the view with
      */
     public int getContentViewID()
     {
         return m_oThis.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
                 m_oThis.getLandscapeViewResourceID() : m_oThis.getPortraitViewResourceID();
     }
 
     /**
      * Notifies the activity that it should close.  The activity will check with the activity manager before
      * closing
      */
     public void finish()
     {
         if (!m_lIsFinishing)
         {
             m_lIsFinishing = true;
             if (ActivityManager.getInstance().notify(NotificationType.FINISH, m_oThis))
             {
                 m_oThis.finish();
             }
         }
     }
 
     /**
      * Checks if the Implementation has started the process of finishing the activity
      * @return true if we are already in the process of finishing
      */
     public boolean isFinishing()
     {
         return m_lIsFinishing;
     }
 
     /**
      * Checks if this view is paused
      * @return true if paused
      */
     public boolean isPaused()
     {
         return m_lPaused;
     }
 
     /**
      * Determines if this activity has been made visible
      * by a call to start
      * @return true if this activity is visible
      */
     public boolean isVisible()
     {
         return m_lVisible;
     }
 
     /**
      * Occurs when the activity has been notified its content has changed.
      * This happens directly after the system call to onContentChanged
      */
     public void onContentChanged()
     {
         if (ActivityManager.getInstance().notify(NotificationType.CONTENT_READY, m_oThis) && isVisible())
         {
            m_oThis.onContentReady();
         }
     }
 
     /**
      * Forces a UI update by calling onUpdateUI on the correct thread.  If this is called multiple times and there
      * is still an outstanding update the additional calls will be considered a no op.
      */
     public void updateUI()
     {
         if (!m_lPaused && m_oOutstandingUpdate == null)
         {
             final ActivityImpl loSelf = this;
             m_oOutstandingUpdate = new Runnable()
             {
                 public void run()
                 {
                     try
                     {
                         m_oThis.onUpdateUI();
                     }
                     catch (Throwable ex)
                     {}
                     finally
                     {
                         loSelf.m_oOutstandingUpdate = null;
                     }
                 }
             };
             m_oThis.runOnUiThread(m_oOutstandingUpdate);
         }
     }
 
     /**
      * Sets the activity for this implementation.  This can only
      * be set once.  This is a helper method for custom implementations
      * @param toActivity the activity
      */
     protected void setActivity(T toActivity)
     {
         if (m_oThis != null)
         {
             throw new InvalidOperationException("The Activity has already been set");
         }
         m_oThis = toActivity;
     }
 
 }
