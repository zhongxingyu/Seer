 package edu.selu.android.classygames;
 
 
 import java.util.ArrayList;
 
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.facebook.android.Util;
 import com.google.android.gcm.GCMRegistrar;
 
 import edu.selu.android.classygames.data.Person;
 import edu.selu.android.classygames.games.GenericGame;
 import edu.selu.android.classygames.games.checkers.Checkers;
 
 
 public class GamesListActivity extends SherlockListActivity
 {
 
 
 	private AsyncTask<Void, Void, Void> registerTask;
 	private GamesListAdapter gamesAdapter;
 	private Person person;
 
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.games_list_activity);
 		Utilities.styleActionBar(getResources(), getSupportActionBar(), false);
 
 		new AsyncGCMRegister().execute();
 		new AsyncPopulateGamesList().execute();
 	}
 
 
 	@Override
 	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (resultCode == LogoutActivity.LOGGED_OUT)
 		{
 			finish();
 		}
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu menu)
 	{
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.games_list_activity, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 
 	@Override
 	public void onDestroy()
 	{
 		if (registerTask != null)
 		{
 			registerTask.cancel(true);
 		}
 
 		unregisterReceiver(messageReceiver);
 		GCMRegistrar.onDestroy(GamesListActivity.this);
 		super.onDestroy();
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId()) 
 		{
 			case R.id.games_list_activity_actionbar_about:
 				startActivity(new Intent(GamesListActivity.this, AboutActivity.class));
 				return true;
 
 			case R.id.games_list_activity_actionbar_logout:
 				startActivityForResult(new Intent(GamesListActivity.this, LogoutActivity.class), 0);
 				return true;
 
 			case R.id.games_list_activity_actionbar_new_game:
 				Intent intent = new Intent(GamesListActivity.this, NewGameActivity.class);
 				intent.putExtra(CheckersGameActivity.INTENT_DATA_PERSON_CREATOR_ID, person.getId());
 				intent.putExtra(CheckersGameActivity.INTENT_DATA_PERSON_CREATOR_NAME, person.getName());
 				startActivity(intent);
 				return true;
 
 			case R.id.games_list_activity_actionbar_refresh:
 				new AsyncPopulateGamesList().execute();
 				return true;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 	/**
 	 * set up Google Cloud Messaging (GCM) Stuff
 	 */
 	private final class AsyncGCMRegister extends AsyncTask<Void, Void, Void>
 	{
 
 
 		private ProgressDialog progressDialog;
 
 
 		@Override
 		protected Void doInBackground(final Void... v)
 		{
			// make sure that the device has the proper dependencies
 			GCMRegistrar.checkDevice(GamesListActivity.this);
 
 			// make sure that the manifest was properly set
 			GCMRegistrar.checkManifest(GamesListActivity.this);
 
 			registerReceiver(messageReceiver, new IntentFilter(Utilities.DISPLAY_MESSAGE_ACTION));
 
 			try
 			{
 				final String request = Utilities.getFacebook().request("me");
 				final JSONObject me = Util.parseJson(request);
 				final long id = me.getLong("id");
 				final String name = me.getString("name");
 
 				if (id < 0 || name == null || name.equals(""))
 				{
 					person = new Person();
 				}
 				else
 				{
 					person = new Person(id, name);
 				}
 			}
 			catch (final Exception e)
 			{
 				Log.e(Utilities.LOG_TAG, "Exception during Facebook request or parse.", e);
 			}
 
 			// grab current registration ID. we may not already have one
 			final String reg_id = GCMRegistrar.getRegistrationId(GamesListActivity.this);
 
 			if (reg_id == null || reg_id.equals(""))
 			// if we do not have a registration ID then we'll need to register with the server with
 			// the below code
 			{
 				// automatically registers application with GCM on startup
 				GCMRegistrar.register(GamesListActivity.this, SecretConstants.GOOGLE_PROJECT_ID);
 			}
 			else
 			// if we're here then that means that this device has already been registered with the GCM
 			// server
 			{
 				if (GCMRegistrar.isRegisteredOnServer(GamesListActivity.this))
 				// check to see if this device is registed on the classy games server
 				{
 
 				}
 				else
 				{
 					new AsyncTask<Void, Void, Void>()
 					{
 						@Override
 						protected Void doInBackground(final Void... v)
 						{
 							if (!Utilities.GCMRegister(GamesListActivity.this, person.getId(), person.getName(), reg_id))
 							{
 								GCMRegistrar.unregister(GamesListActivity.this);
 							}
 
 							return null;
 						}
 
 
 						@Override
 						protected void onPostExecute(final Void result)
 						{
 							registerTask = null;
 						}
 					}.execute();
 				}
 			}
 
 			return null;
 		}
 
 
 		@Override
 		protected void onPostExecute(final Void result)
 		{
 			if (progressDialog.isShowing())
 			{
 				progressDialog.dismiss();
 			}
 		}
 
 
 		@Override
 		protected void onPreExecute()
 		{
 			progressDialog = new ProgressDialog(GamesListActivity.this);
 			progressDialog.setMessage(GamesListActivity.this.getString(R.string.games_list_activity_init_progressdialog_message));
 			progressDialog.setTitle(R.string.games_list_activity_init_progressdialog_title);
 			progressDialog.setCancelable(false);
 			progressDialog.setCanceledOnTouchOutside(false);
 
 			progressDialog.show();
 		}
 
 
 	}
 
 
 	private final class AsyncPopulateGamesList extends AsyncTask<Void, Void, ArrayList<GenericGame>>
 	{
 
 
 		private ProgressDialog progressDialog;
 
 
 		@Override
 		protected ArrayList<GenericGame> doInBackground(final Void... v)
 		{
 			ArrayList<GenericGame> games = new ArrayList<GenericGame>();
 
 			try
 			{
 				// TODO: this code will eventually be replaced by an actual call to our
 				// server. This call will ask the server for a games list
 
 				games.add(new Checkers(new Person("Your Turn")));
 				games.add(new Checkers(new Person("Charles Madere")));
 				games.add(new Checkers(new Person("Bart Simpson")));
 				games.add(new Checkers(new Person("Tristan Kidder")));
 				games.add(new Checkers(new Person("Geonathon Sena")));
 				games.add(new Checkers());
 				games.add(new Checkers());
 				games.add(new Checkers());
 				games.add(new Checkers());
 				games.add(new Checkers());
 				games.add(new Checkers(new Person("Their Turn")));
 				games.add(new Checkers());
 				games.add(new Checkers());
 				games.add(new Checkers());
 				games.add(new Checkers());
 				games.add(new Checkers());
 				games.add(new Checkers());
 
 				games.trimToSize();
 			}
 			catch (final Exception e)
 			{
 				Log.e(Utilities.LOG_TAG, e.getMessage());
 			}
 
 			return games;
 		}
 
 
 		@Override
 		protected void onPostExecute(final ArrayList<GenericGame> games)
 		{
 			gamesAdapter = new GamesListAdapter(GamesListActivity.this, R.layout.new_game_activity_listview_item, games);
 			setListAdapter(gamesAdapter);
 			gamesAdapter.notifyDataSetChanged();
 			
 			if (progressDialog.isShowing())
 			{
 				progressDialog.dismiss();
 			}
 		}
 
 
 		@Override
 		protected void onPreExecute()
 		{
 			progressDialog = new ProgressDialog(GamesListActivity.this);
 			progressDialog.setMessage(GamesListActivity.this.getString(R.string.games_list_activity_games_progressdialog_message));
 			progressDialog.setTitle(R.string.games_list_activity_games_progressdialog_title);
 			progressDialog.setCancelable(false);
 			progressDialog.setCanceledOnTouchOutside(false);
 
 			progressDialog.show();
 		}
 
 
 	}
 
 
 	private final BroadcastReceiver messageReceiver = new BroadcastReceiver()
 	{
 		@Override
 		public void onReceive(final Context context, final Intent intent)
 		{
 
 		}
 	};
 
 
 	private class GamesListAdapter extends ArrayAdapter<GenericGame>
 	{
 
 
 		private ArrayList<GenericGame> games;
 
 
 		public GamesListAdapter(final Context context, final int textViewResourceId, final ArrayList<GenericGame> games)
 		{
 			super(context, textViewResourceId, games);
 			this.games = games;
 		}
 
 
 		@Override
 		public View getView(final int position, View convertView, final ViewGroup parent)
 		{
 			if (convertView == null)
 			{
 				LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = layoutInflater.inflate(R.layout.games_list_activity_listview_item, null);
 			}
 
 			final GenericGame game = games.get(position);
 
 			if (game != null)
 			{
 				LayoutInflater layoutInflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE);
 				ViewHolder viewHolder = new ViewHolder();
 
 				if (game.getPerson().getName() == "Their Turn")
 				{
 					convertView = layoutInflater.inflate(R.layout.games_list_activity_listview_turn_theirs, null);
 					viewHolder.picture = (ImageView) convertView.findViewById(R.drawable.turn_theirs);
 				}
 				else if (game.getPerson().getName() == "Your Turn")
 				{
 					convertView = layoutInflater.inflate(R.layout.games_list_activity_listview_turn_yours, null);
 					viewHolder.picture = (ImageView) convertView.findViewById(R.drawable.turn_yours);
 				}
 				else
 				{
 					layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 					convertView = layoutInflater.inflate(R.layout.games_list_activity_listview_item, null);
 					viewHolder = new ViewHolder();
 					viewHolder.picture = (ImageView) convertView.findViewById(R.id.games_list_activity_listview_item_picture);
 
 					if (viewHolder.picture != null)
 					{
 
 					}
 
 					viewHolder.name = (TextView) convertView.findViewById(R.id.games_list_activity_listview_item_name);
 					if (viewHolder.name != null)
 					{
 						viewHolder.name.setText(game.getPerson().getName());
 						viewHolder.name.setTypeface(Utilities.getTypeface(getAssets(), Utilities.TYPEFACE_BLUE_HIGHWAY_D));
 					}
 
 					viewHolder.time = (TextView) convertView.findViewById(R.id.games_list_activity_listview_item_time);
 					if (viewHolder.time != null)
 					{
 						viewHolder.time.setText(game.getLastMoveTime());
 					}	
 
 					viewHolder.onClickListener = new OnClickListener()
 					{
 						@Override
 						public void onClick(final View v)
 						{
 							Intent intent = new Intent(GamesListActivity.this, CheckersGameActivity.class);
 							intent.putExtra(CheckersGameActivity.INTENT_DATA_GAME_ID, game.getId());
 							intent.putExtra(CheckersGameActivity.INTENT_DATA_PERSON_CHALLENGED_ID, game.getPerson().getId());
 							intent.putExtra(CheckersGameActivity.INTENT_DATA_PERSON_CHALLENGED_NAME, game.getPerson().getName());
 
 							// start the ConfirmGameActivity with a bit of extra data. We're passing it both
 							// the id and the name of the facebook person that the user clicked on
 							startActivity(intent);
 						}
 					};
 
 					convertView.setOnClickListener(viewHolder.onClickListener);
 				}
 
 				convertView.setTag(viewHolder);
 			}
 
 			return convertView;
 		}
 
 
 		/**
 		 * made this li'l class while trying to optimize our listview. apparently it
 		 * helps performance
 		 * https://developer.android.com/training/improving-layouts/smooth-scrolling.html
 		 */
 		private class ViewHolder
 		{
 
 
 			ImageView picture;
 			OnClickListener onClickListener;
 			TextView name;
 			TextView time;
 
 
 		}
 
 
 	}
 
 
 }
