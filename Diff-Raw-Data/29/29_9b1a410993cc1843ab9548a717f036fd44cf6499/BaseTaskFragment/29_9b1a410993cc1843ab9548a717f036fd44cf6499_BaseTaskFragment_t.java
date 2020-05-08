 package fr.utc.assos.uvweb.io.base;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 /**
  * A base UI-less fragment to easily manage async queries while being tight
  * to the corresponding Activity or Fragment lifecycle.
  * If the attached Activity or Fragment gets destroyed by a configuration
  * change (like a rotation), the {@link Callbacks} reference needs to be updated
  * using the {@code setCallCallbacks()} method.
  */
 public abstract class BaseTaskFragment extends SherlockFragment {
 	public static final int THREAD_DEFAULT_POLICY = 0;
 	public static final int THREAD_POOL_EXECUTOR_POLICY = 1;
 	protected FragmentTask mTask;
 	private Callbacks mCallbacks;
 	private boolean mIsRunning;
 
 	public BaseTaskFragment() {
 	}
 
 	// Public API
 	public static <T extends BaseTaskFragment> T get(FragmentManager fm, Class<T> clazz) {
 		final String tag = clazz.getSimpleName();
 		T instance = clazz.cast(fm.findFragmentByTag(tag));
 		if (instance == null) {
 			try {
 				instance = clazz.newInstance();
 				fm.beginTransaction().add(instance, tag).commit();
 			} catch (java.lang.InstantiationException e) {
 				throw new IllegalArgumentException("Class must be instantiable");
 			} catch (IllegalAccessException e) {
 				throw new IllegalArgumentException("Class must be accessible");
 			}
 		}
 		return instance;
 	}
 
 	public void startNewTask() {
 		startNewTask(THREAD_DEFAULT_POLICY);
 	}
 
 	public void startNewTask(final int threadMode) {
 		if (mTask != null) {
 			mTask.cancel(true);
			mTask = null;
 		}
		final Fragment target = getTargetFragment();
		if (!(target instanceof Callbacks)) {
			throw new IllegalStateException("Target fragment must implement Callbacks");
		}
		setCallbacks((Callbacks) target);
 		if (threadMode == THREAD_DEFAULT_POLICY) {
 			start();
 		} else if (threadMode == THREAD_POOL_EXECUTOR_POLICY) {
 			startOnThreadPoolExecutor();
 		} else {
 			throw new IllegalArgumentException("threadMode must be either THREAD_DEFAULT_POLICY" +
 					"or THREAD_POOL_EXECUTOR_POLICY");
 		}
 	}
 
 	public boolean isRunning() {
 		return mIsRunning;
 	}
 
 	// Lifecycle
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 
 		if (activity instanceof Callbacks) {
 			mCallbacks = (Callbacks) activity;
 		}
 	}
 
 	/**
 	 * This method will only be called once when the retained
 	 * Fragment is first created.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Retain this fragment across configuration changes.
 		setRetainInstance(true);
 		setUserVisibleHint(false);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return null;
 	}
 
 	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
 
		final Fragment target = getTargetFragment();
		if (target instanceof Callbacks) {
			setCallbacks((Callbacks) target);
 		}
 	}
 
 	/**
 	 * Set the callback to null so we don't accidentally leak the
 	 * Fragment instance.
 	 */
 	@Override
 	public void onDetach() {
 		mCallbacks = null;
 
 		super.onDetach();
 	}
 
 	protected abstract void start();
 
 	protected abstract void startOnThreadPoolExecutor();
 
 	protected void setCallbacks(Callbacks callbacks) {
		if (mTask != null && callbacks == null) {
			mTask.cancel(true);
			mTask = null;
 		}
 		mCallbacks = callbacks;
 	}
 
 	public interface Callbacks<Result> {
 		void onPreExecute();
 
 		void onPostExecute(Result result);
 
 		void onError();
 	}
 
 	public abstract class FragmentTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
 		protected static final String API_ENDPOINT = "http://192.168.0.15/Uvweb/web/app_dev.php/uv/app/";
 
 		@Override
 		protected void onPreExecute() {
 			if (mCallbacks != null) {
 				mCallbacks.onPreExecute();
 			}
 			mIsRunning = true;
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		protected void onPostExecute(Result result) {
 			mIsRunning = false;
 			if (mCallbacks != null) {
 				if (result == null) {
 					mCallbacks.onError();
 				} else {
 					mCallbacks.onPostExecute(result);
 				}
 			}
 		}
 
 		@Override
 		protected void onCancelled() {
 			mCallbacks = null;
 			mIsRunning = false;
 		}
 	}
 }
