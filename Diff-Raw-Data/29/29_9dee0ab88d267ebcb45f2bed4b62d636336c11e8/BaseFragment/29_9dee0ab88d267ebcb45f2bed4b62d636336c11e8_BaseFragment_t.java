 package karyon.android.fragments;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import karyon.android.activities.ActivityImpl;
 import karyon.android.activities.IActivity;
 
 /**
  * A Fragment is a view that has individual logic and can
  * be contained by an activity
  */
 public abstract class BaseFragment<T extends IActivity>
     extends Fragment
         implements IActivity<T>
 {
     /**
      * Helper method for creating an activity
      * @param toClass the class to load
      * @param <K> the return type of the activity
      * @return the Activity that was created
      */
     public static <K extends BaseFragment> K instantiate(Class<K> toClass)
             throws java.lang.InstantiationException, IllegalAccessException
     {
         return toClass.newInstance();
     }
 
     private ActivityImpl<T> m_oImpl;
     private ViewGroup m_oRootContainer;
 
     /**
      * Creates a new instance of the base activity
      */
     protected BaseFragment()
     {
         m_oImpl = new ActivityImpl<T>((T)this);
     }
 
     /**
      * Allows for customisation of the implementation
      * @param toImplementation the implementation to use with this instance
      */
     protected BaseFragment(ActivityImpl<T> toImplementation)
     {
         m_oImpl = toImplementation;
     }
 
     /**
      * Gets the implementation that is being used for this activity
      * @return the activity that is being used for the activity
      */
     protected ActivityImpl<T> getImpl()
     {
         return m_oImpl;
     }
 
     @Override
     public final int getContentViewID()
     {
         return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
                 getLandscapeViewResourceID() : getPortraitViewResourceID();
     }
 
     @Override
     public int getLandscapeViewResourceID()
     {
         return getPortraitViewResourceID();
     }
 
     @Override
     public abstract int getPortraitViewResourceID();
 
     @Override
     public boolean canShowTitle()
     {
         return true;
     }
 
     @Override
     public int getCustomTitleDrawable()
     {
         return 0;
     }
 
     @Override
     public final boolean setWindowFeature(int tnFeature)
     {
         return getActivity().requestWindowFeature(tnFeature);
     }
 
     /**
      * This is always called when the activity is first created.
      * This is the "entry point" to the activity life cycle
      */
     @Override
     public final void onCreate(Bundle toPreviousState)
     {
         super.onCreate(toPreviousState);
         m_oImpl.onCreate(toPreviousState);
     }
 
     @Override
     public View onCreateView(LayoutInflater toInflater, ViewGroup toContainer, Bundle toSavedInstanceState)
     {
         m_oRootContainer = toContainer;
         return toInflater.inflate(getContentViewID(), m_oRootContainer, false);
     }
 
     @Override
     public boolean onInit(Bundle toSavedState)
     {
         return true;
     }
 
     /**
      * Gets the root container for this fragment
      * @return the root container
      */
     protected final ViewGroup getRootContainer()
     {
         return m_oRootContainer;
     }
 
     /**
      * This is called when the fragment is attached to an
      * activity
      * @param toActivity the activity that the fragment was attached to
      */
     @Override
     public final void onAttach(Activity toActivity)
     {
         m_oImpl.onAttach(toActivity);
         super.onAttach(toActivity);
     }
 
     @Override
     public void onRestarted()
     {
     }
 
     /**
      * Called when the activity is becoming visible to the user, after either onCreate or onRestart.
      */
     @Override
     public final void onStart()
     {
         m_oImpl.onStart();
         super.onStart();
     }
 
     @Override
     public void onStarted()
     {
     }
 
     /**
      * Called when the activity will start interacting with the user.  At this point your activity
      * is at the top of the activity stack, with the user input going to your activity
      */
     @Override
     public final void onResume()
     {
         m_oImpl.onResume();
         super.onResume();
     }
 
     @Override
     public void onResumed()
     {
     }
 
     /**
      * Called when the system is about to start resuming a previous activity.  This is typically
      * used to commit unsaved changes to persistent data, stop animations and other things that may be consuming CPU.
      * Implementations of this method should be very quick as the next activity will not be resumed until this
      * method returns
      */
     @Override
     public final void onPause()
     {
         m_oImpl.onPause();
         super.onPause();
     }
 
     @Override
     public void onPaused()
     {
     }
 
     /**
      * Called when the activity is no longer visible to the user, because another activity has been resumed and is
      * covering this one.  This may happen either because a new activity is being started, an existing activity is
      * being brought in front of this one, or this one is being destroyed
      */
     @Override
     public final void onStop()
     {
         m_oImpl.onStop();
         super.onStop();
     }
 
     @Override
     public void onStopped()
     {
     }
 
     /**
      * The final call you receive before your activity is destroyed.  this can happen either because the activity
      * is finishing or because the system is temporarily destroying this instance of the activity to save
      * space.  You can distinguish between these two scenarios with the isFinishing method
      */
     @Override
     public final void onDestroy()
     {
         m_oImpl.onDestroy();
         super.onDestroy();
     }
 
     @Override
     public void onDestroyed()
     {
     }
 
     /**
      * This is called when the overall system is running low on memory and would like actively running processes to try
      * to clear up memory.  The system will perform a gc after returning from the method
      */
     @Override
     public final void onLowMemory()
     {
         m_oImpl.onLowMemory();
         super.onLowMemory();
     }
 
     @Override
     public void onMemoryLow()
     {
     }
 
     @Override
     public void onContentReady()
     {
         // By default, force an update
         invalidate();
     }
 
     /**
      * Notifies the UI that a refresh is needed
      */
     public final void invalidate()
     {
         m_oImpl.updateUI();
     }
 
     @Override
     public void onUpdateUI()
     {
     }
 
     @Override
     public Context getContext()
     {
         return this.getActivity();
     }
 
     /**
      * Called by the system when the device configuration changes while the activity is running.  This will only
      * be called if you have selected configurations you would like to handle with the configChanges attribute in your
      * manifest.  If any configuration change occurs that is not selected to be reported, then the system will instead
      * stop the activity and restart it with the new configuration
      * @param toConfig the new configuration
      */
     @Override
     public void onConfigurationChanged(Configuration toConfig)
     {
         super.onConfigurationChanged(toConfig);
         m_oImpl.updateContentView();
     }
 
     /**
      * Checks if this activity is currently paused
      * @return true if the activity is paused.  False otherwise
      */
     public boolean isPaused()
     {
         return m_oImpl.isPaused();
     }
 
     @Override
     public Window getWindow()
     {
         Activity loActivity = getActivity();
         return loActivity != null ? loActivity.getWindow() : null;
     }
 
     @Override
     public void setContentView(int tnLayoutID)
     {
 
     }
 
     @Override
     public void finish()
     {
         // TODO: Look at implementing this to "finish" the fragment
     }
 
     @Override
     public void runOnUiThread(Runnable toAction)
     {
         Activity loActivity = getActivity();
         if (loActivity != null)
         {
             loActivity.runOnUiThread(toAction);
         }
     }
 
     @Override
     public boolean isFinishing()
     {
         Activity loActivity = getActivity();
         return loActivity != null ? loActivity.isFinishing() : false;
     }
 
     @Override
     public View findViewById(int tnID)
     {
         View loView = getView();
         return loView != null ? loView.findViewById(tnID) : getActivity().findViewById(tnID);
     }
 
     @Override
     public FragmentManager getSupportFragmentManager()
     {
         return getFragmentManager();
     }

    @Override
    public void setRequestedOrientation(int tnOrientation)
    {
        if (getActivity() != null)
        {
            getActivity().setRequestedOrientation(tnOrientation);
        }
    }
 }
