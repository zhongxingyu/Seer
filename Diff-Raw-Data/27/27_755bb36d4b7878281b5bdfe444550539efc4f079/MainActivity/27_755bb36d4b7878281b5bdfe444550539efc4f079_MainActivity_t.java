 package com.charlesmadere.android.classygames;
 
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.ViewGroup;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Window;
 import com.charlesmadere.android.classygames.models.Person;
 import com.charlesmadere.android.classygames.server.Server;
 import com.charlesmadere.android.classygames.utilities.FacebookUtilities;
 import com.charlesmadere.android.classygames.utilities.Utilities;
 import com.facebook.*;
 import com.facebook.Request.GraphUserCallback;
 import com.facebook.model.GraphUser;
 
 
 /**
  * This class is the app's entry point.
  */
 public final class MainActivity extends SherlockActivity
 {
 
 
 	private UiLifecycleHelper uiHelper;
 
 	private boolean isResumed = false;
 
 
 
 
 	/**
 	 * Used to obtain the current user's Facebook identity.
 	 */
 	private AsyncGetFacebookIdentity asyncGetFacebookIdentity;
 
 
 
 
 	@Override
 	protected void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.main_activity);
 
 		final Session.StatusCallback sessionStatusCallback = new Session.StatusCallback()
 		{
 			@Override
 			public void call(final Session session, final SessionState state, final Exception exception)
 			{
 				onSessionStateChange(session, state);
 			}
 		};
 
 		uiHelper = new UiLifecycleHelper(this, sessionStatusCallback);
 		uiHelper.onCreate(savedInstanceState);
 	}
 
 
 	@Override
 	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		uiHelper.onActivityResult(requestCode, resultCode, data);
 	}
 
 
 	@Override
 	public void onBackPressed()
 	{
 		if (isAnAsyncTaskRunning())
 		{
 			cancelRunningAnyAsyncTask();
 		}
 		else
 		{
 			super.onBackPressed();
 		}
 	}
 
 
 	@Override
 	protected void onDestroy()
 	{
 		cancelRunningAnyAsyncTask();
 
 		isResumed = false;
 		uiHelper.onDestroy();
 		super.onDestroy();
 	}
 
 
 	@Override
 	protected void onPause()
 	{
 		isResumed = false;
 		uiHelper.onPause();
 		super.onPause();
 	}
 
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		uiHelper.onResume();
 		isResumed = true;
 
 		final Person whoAmI = Utilities.getWhoAmI(this);
 
 		if (whoAmI != null && whoAmI.isValid())
 		{
 			startCentralFragmentActivity();
 		}
 	}
 
 
 	@Override
 	protected void onSaveInstanceState(final Bundle outState)
 	{
 		uiHelper.onSaveInstanceState(outState);
 		super.onSaveInstanceState(outState);
 	}
 
 
 
 
 	/**
 	 * Cancels the AsyncRefreshGamesList AsyncTask if it is currently
 	 * running.
 	 */
 	private void cancelRunningAnyAsyncTask()
 	{
 		if (isAnAsyncTaskRunning())
 		{
 			asyncGetFacebookIdentity.cancel(true);
 		}
 	}
 
 
 	/**
 	 * @return
 	 * Returns true if an AsyncTask is running.
 	 */
 	private boolean isAnAsyncTaskRunning()
 	{
 		return asyncGetFacebookIdentity != null;
 	}
 
 
 	private void onSessionStateChange(final Session session, final SessionState state)
 	{
 		if (isResumed)
 		// only make changes if this activity is visible
 		{
 			if (state.equals(SessionState.OPENED))
 			// if the session state is open, show the authenticated activity
 			{
 				// store the user's Facebook Access Token for retrieval later
 				FacebookUtilities.setAccessToken(this, session.getAccessToken());
 
 				asyncGetFacebookIdentity = new AsyncGetFacebookIdentity(this,
 					(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), session, (ViewGroup) findViewById(R.id.main_activity_listview));
 				asyncGetFacebookIdentity.execute();
 			}
 		}
 	}
 
 
 	private void startCentralFragmentActivity()
 	{
		startActivity(new Intent(this, GameFragmentActivity.class));
 	}
 
 
 
 
 	private final class AsyncGetFacebookIdentity extends AsyncTask<Void, Void, Person>
 	{
 
 
 		private Context context;
 		private LayoutInflater inflater;
 		private Session session;
 		private ViewGroup viewGroup;
 
 
 		private AsyncGetFacebookIdentity(final Context context, final LayoutInflater inflater, final Session session,
 			final ViewGroup viewGroup)
 		{
 			this.context = context;
 			this.inflater = inflater;
 			this.session = session;
 			this.viewGroup = viewGroup;
 		}
 
 
 		@Override
 		protected Person doInBackground(final Void... params)
 		{
 			final Person facebookIdentity = new Person();
 
 			if (!isCancelled())
 			{
 				Request.newMeRequest(session, new GraphUserCallback()
 				{
 					@Override
 					public void onCompleted(final GraphUser user, final Response response)
 					{
 						facebookIdentity.setId(user.getId());
 						facebookIdentity.setName(user.getName());
 					}
 				}).executeAndWait();
 
 				Server.gcmPerformRegister(context);
 			}
 
 			return facebookIdentity;
 		}
 
 
 		private void cancelled()
 		{
 			session.closeAndClearTokenInformation();
 			viewGroup.removeAllViews();
 			asyncGetFacebookIdentity = null;
 
 			finish();
 		}
 
 
 		@Override
 		protected void onCancelled()
 		{
 			cancelled();
 		}
 
 
 		@Override
 		protected void onCancelled(final Person facebookIdentity)
 		{
 			cancelled();
 		}
 
 
 		@Override
 		protected void onPostExecute(final Person facebookIdentity)
 		{
 			if (facebookIdentity.isValid())
 			{
 				Utilities.setWhoAmI(context, facebookIdentity);
 				viewGroup.removeAllViews();
 				asyncGetFacebookIdentity = null;
 
 				startCentralFragmentActivity();
 			}
 			else
 			{
 				cancelled();
 			}
 		}
 
 
 		@Override
 		protected void onPreExecute()
 		{
 			viewGroup.removeAllViews();
 			inflater.inflate(R.layout.main_activity_loading, viewGroup);
 		}
 
 
 	}
 
 
 
 
 }
