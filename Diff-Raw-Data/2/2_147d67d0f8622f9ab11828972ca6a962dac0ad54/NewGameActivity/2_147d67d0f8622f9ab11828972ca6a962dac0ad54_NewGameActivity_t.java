 package edu.selu.android.classygames;
 
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Build;
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
 import com.actionbarsherlock.view.MenuItem;
 import com.facebook.android.Util;
 import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
 
 import edu.selu.android.classygames.games.Person;
 
 
 public class NewGameActivity extends SherlockListActivity
 {
 
 
 	private PeopleAdapter peopleAdapter;
 
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.new_game_activity);
 		Utilities.styleActionBar(getResources(), getSupportActionBar());
 
 		new AsyncPopulateFacebookFriends().execute();
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId()) 
 		{		
 			case android.R.id.home:
 				finish();
 				return true;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 	private final class AsyncPopulateFacebookFriends extends AsyncTask<Void, Long, ArrayList<Person>>
 	{
 
 
 		private ProgressDialog progressDialog;
 
 
 		@Override
 		protected ArrayList<Person> doInBackground(final Void... v)
 		{
 			ArrayList<Person> people = new ArrayList<Person>();
 
 			try
 			{
 				final String request = Utilities.getFacebook().request("me/friends");
 				final JSONObject response = Util.parseJson(request);
 				final JSONArray friends = response.getJSONArray("data");
 
 				final int friendsLength = friends.length();
 				publishProgress((long) friendsLength);
 
 				for (int i = 0; i < friendsLength; ++i)
 				{
 					final JSONObject friend = friends.getJSONObject(i);
 					final long id = friend.getLong("id");
 					people.add(new Person(id, friend.getString("name")));
 
 					publishProgress((long) i, id);
 				}
 
 				people.trimToSize();
 
 				// TODO: sort the arraylist of facebook friends into alphabetical order. currently it's
 				// sorted by id, which is how facebook delivers the data to us.
 			}
 			catch (final Exception e)
 			{
 				Log.e(Utilities.LOG_TAG, e.getMessage());
 			}
 
 			return people;
 		}
 
 
 		@Override
 		protected void onPostExecute(final ArrayList<Person> people)
 		{
 			peopleAdapter = new PeopleAdapter(NewGameActivity.this, R.layout.new_game_activity_listview_item, people);
 			setListAdapter(peopleAdapter);
 			peopleAdapter.notifyDataSetChanged();
 
 			if (progressDialog.isShowing())
 			{
 				progressDialog.dismiss();
 			}
 		}
 
 
 		@Override
 		protected void onPreExecute()
 		{
 			progressDialog = new ProgressDialog(NewGameActivity.this);
 			progressDialog.setMessage("Retrieving all of your Facebook friends...");
 			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			progressDialog.setTitle(R.string.new_game_activity_progressdialog_title);
 			progressDialog.show();
 		}
 
 
 		@Override
 		protected void onProgressUpdate(final Long... i)
 		{
 			switch (i.length)
 			{
 				case 1:
 					progressDialog.setMax(i[0].intValue());
 					break;
 
 				case 2:
 					progressDialog.setProgress(i[0].intValue());
 
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
 					{
 						UrlImageViewHelper.loadUrlDrawable(NewGameActivity.this, "https://graph.facebook.com/" + i[1] + "/picture?return_ssl_resources=1");
 					}
 					break;
 			}
 		}
 
 
 	}
 
 
 	private class PeopleAdapter extends ArrayAdapter<Person>
 	{
 
 
 		private ArrayList<Person> people;
 
 
 		public PeopleAdapter(final Context context, final int textViewResourceId, final ArrayList<Person> people)
 		{
 			super(context, textViewResourceId, people);
 
 			this.people = people;
 		}
 
 
 		@Override
 		public View getView(final int position, View convertView, final ViewGroup parent)
 		{
 			if (convertView == null)
 			{
 				LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = layoutInflater.inflate(R.layout.new_game_activity_listview_item, null);
 			}
 
 			final Person person = people.get(position);
 			Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/blue_highway_d.ttf");
 
 			if (person != null)
 			{
 				ViewHolder viewHolder = new ViewHolder();
 				viewHolder.picture = (ImageView) convertView.findViewById(R.id.new_game_activity_listview_item_picture);
 				if (viewHolder.picture != null)
 				{
 					UrlImageViewHelper.setUrlDrawable(viewHolder.picture, "https://graph.facebook.com/" + person.getId() + "/picture?return_ssl_resources=1");
 				}
 
 				viewHolder.name = (TextView) convertView.findViewById(R.id.new_game_activity_listview_item_name);
 				if (viewHolder.name != null)
 				{
 					viewHolder.name.setText(person.getName());
 					viewHolder.name.setTypeface(typeface);
 				}
 
 				viewHolder.onClickListener = new OnClickListener()
 				{
 					@Override
 					public void onClick(final View v)
 					{
 						Utilities.easyToastAndLog(NewGameActivity.this, "\"" + person.getName() + "\" \"" + person.getId() + "\"");
 					}
 				};
 
 				convertView.setOnClickListener(viewHolder.onClickListener);
 				convertView.setTag(viewHolder);
 			}
 
 			return convertView;
 		}
 
 
 		private class ViewHolder
 		/**
 		 * made this li'l class while trying to optimize our listview. apparently using
 		 * something like this helps performance
 		 * https://developer.android.com/training/improving-layouts/smooth-scrolling.html
 		 *
 		 */
 		{
 
 
 			public ImageView picture;
 			public OnClickListener onClickListener;
 			public TextView name;
 
 
 		}
 
 
 	}
 
 
 }
