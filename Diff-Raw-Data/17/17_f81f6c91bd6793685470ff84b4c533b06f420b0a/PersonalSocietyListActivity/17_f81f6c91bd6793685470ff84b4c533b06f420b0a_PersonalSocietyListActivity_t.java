 package uk.ac.dur.duchess.activity;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import uk.ac.dur.duchess.R;
 import uk.ac.dur.duchess.data.CalendarFunctions;
 import uk.ac.dur.duchess.data.SessionFunctions;
 import uk.ac.dur.duchess.entity.Event;
 import uk.ac.dur.duchess.entity.User;
 import uk.ac.dur.duchess.webservice.EventAPI;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class PersonalSocietyListActivity extends CustomTitleBarActivity
 {
 	private ExpandableListView exListView;
 	private ExpandableListAdapter exAdapter;
 	private Map<String, List<Event>> eventMap;
 	private Activity activity;
 	private List<String> societyList;
 	private AlertDialog alertDialog;
 	private ProgressDialog progressDialog;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.personal_society_events_list);
 
 		activity = this;
 
 		exListView = (ExpandableListView) findViewById(R.id.expandableSocietyList);
 
 		User user = SessionFunctions.getCurrentUser(this);
 		societyList = user.getSocieties();
 
 		eventMap = new HashMap<String, List<Event>>();
 
 		for (String s : societyList)
 		{
 			eventMap.put(s, new ArrayList<Event>());
 		}
 		
 		exAdapter = new GroupedEventListAdapter(societyList, eventMap);
 		exListView.setAdapter(exAdapter);
 
 		String[] societyArray = new String[societyList.size()];
 		societyList.toArray(societyArray);
 		
 		(new DownloadEventsBackgroundTask()).execute(societyArray);
 	}
 
 	public class GroupedEventListAdapter extends BaseExpandableListAdapter
 	{
 		private List<String> groupList;
 		private Map<String, List<Event>> groupedEvents;
 
 		public GroupedEventListAdapter(List<String> groupList,
 				Map<String, List<Event>> groupedEvents)
 		{
 			this.groupList = groupList;
 			this.groupedEvents = groupedEvents;
 		}
 
 		public Object getGroup(int groupPosition)
 		{
 			return groupList.get(groupPosition);
 		}
 
 		public int getGroupCount()
 		{
 			return groupList.size();
 		}
 
 		public long getGroupId(int groupPosition)
 		{
 			return groupPosition;
 		}
 
 		public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
 				ViewGroup parent)
 		{
 			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
 					ViewGroup.LayoutParams.FILL_PARENT, 64);
 			TextView textView = new TextView(PersonalSocietyListActivity.this);
 			textView.setLayoutParams(lp);
 			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
 			textView.setPadding(60, 0, 0, 0);
 			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
 			textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
 			textView.setText(getGroup(groupPosition).toString());
 			if (getChildrenCount(groupPosition) == 0)
 			{
 				textView.setTextColor(Color.parseColor("#FF0000"));
 			}
 			return textView;
 		}
 
 		public Object getChild(int groupPosition, int childPosition)
 		{
 			return groupedEvents.get(groupList.get(groupPosition)).get(childPosition);
 		}
 
 		public long getChildId(int groupPosition, int childPosition)
 		{
 			return childPosition;
 		}
 
 		public int getChildrenCount(int groupPosition)
 		{
 			return groupedEvents.get(groupList.get(groupPosition)).size();
 		}
 
 		public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
 				View convertView, ViewGroup parent)
 		{
 			View v = convertView;
 			ViewHolder holder;
 			if (v == null)
 			{
 				LayoutInflater inflater = activity.getLayoutInflater();
 				v = inflater.inflate(R.layout.custom_event_list_row, parent, false);
 
 				holder = new ViewHolder();
 
 				holder.txtEventName = (TextView) v.findViewById(R.id.txtEventName);
 				holder.txtEventDescription = (TextView) v.findViewById(R.id.txtEventDescription);
 				holder.txtEventDate = (TextView) v.findViewById(R.id.txtEventDate);
 				holder.star1 = (ImageView) v.findViewById(R.id.newStar1);
 				holder.star2 = (ImageView) v.findViewById(R.id.newStar2);
 				holder.star3 = (ImageView) v.findViewById(R.id.newStar3);
 				holder.star4 = (ImageView) v.findViewById(R.id.newStar4);
 				holder.star5 = (ImageView) v.findViewById(R.id.newStar5);
 				holder.numberOfReviewsDisplay = (TextView) v
 						.findViewById(R.id.numberOfReviewsOnList);
 
 				v.setTag(holder);
 			}
 			else
 			{
 				holder = (ViewHolder) v.getTag();
 			}
 
 			Event e = (Event) getChild(groupPosition, childPosition);
 
 			if (e != null)
 			{
 				if (holder.txtEventName != null)
 				{
 					holder.txtEventName.setText(e.getName());
 				}
 				if (holder.txtEventDescription != null)
 				{
 					holder.txtEventDescription.setText(e.getDescriptionHeader());
 				}
 				if (holder.txtEventDate != null)
 				{
 					holder.txtEventDate.setText(e.getAddress1() + "\n"
 							+ CalendarFunctions.getEventDate(e));
 				}
 
 				int numberOfReviews = e.getNumberOfReviews();
 
 				if (numberOfReviews > 0)
 				{
 					holder.numberOfReviewsDisplay.setText("based on " + numberOfReviews + " review"
 							+ ((numberOfReviews != 1) ? "s" : ""));
 					int rating = e.getReviewScore();
 					Bitmap emptyStar = BitmapFactory.decodeResource(activity.getResources(),
 							R.drawable.empty_star);
 					Bitmap halfStar = BitmapFactory.decodeResource(activity.getResources(),
 							R.drawable.half_star);
 					Bitmap fullStar = BitmapFactory.decodeResource(activity.getResources(),
 							R.drawable.full_star);
 
 					holder.star1.setVisibility(View.VISIBLE);
 					holder.star2.setVisibility(View.VISIBLE);
 					holder.star3.setVisibility(View.VISIBLE);
 					holder.star4.setVisibility(View.VISIBLE);
 					holder.star5.setVisibility(View.VISIBLE);
 					holder.numberOfReviewsDisplay.setVisibility(View.VISIBLE);
 					
 					Bitmap[] stars = {emptyStar, emptyStar, emptyStar, emptyStar, emptyStar};
 					
 					int r = rating;
 					int i = 0;
 					
 					while(r > 0)
 					{
 						if(r > 1)
 						{
 							stars[i] = fullStar;
 							r -= 2;
 							i++;
 						}
						else
						{
							stars[i] = halfStar;
							r--;
						}
 					}
 					
 					holder.star5.setImageBitmap(stars[4]);
 					holder.star4.setImageBitmap(stars[3]);
 					holder.star3.setImageBitmap(stars[2]);
 					holder.star2.setImageBitmap(stars[1]);
 					holder.star1.setImageBitmap(stars[0]);
 				}
 				else
 				{
 					holder.star1.setVisibility(View.GONE);
 					holder.star2.setVisibility(View.GONE);
 					holder.star3.setVisibility(View.GONE);
 					holder.star4.setVisibility(View.GONE);
 					holder.star5.setVisibility(View.GONE);
 					holder.numberOfReviewsDisplay.setVisibility(View.GONE);
 				}
 			}
 			return v;
 		}
 
 		public boolean isChildSelectable(int groupPosition, int childPosition)
 		{
 			return true;
 		}
 
 		public boolean hasStableIds()
 		{
 			return true;
 		}
 	}
 
 	private class DownloadEventsBackgroundTask extends
 			AsyncTask<String, Void, Map<String, List<Event>>>
 	{
 		@Override
 		protected void onPreExecute()
 		{
 			progressDialog = ProgressDialog.show(PersonalSocietyListActivity.this,
 					"Please wait...", "Downloading Events ...", true);
 		}
 
 		@Override
 		protected Map<String, List<Event>> doInBackground(String... societyArray)
 		{
 			try
 			{
 				List<Event> eventList = new ArrayList<Event>();
 				for (String s : societyArray)
 				{
 					eventList = EventAPI.downloadEventsBySociety(s);
 					eventMap.put(s, eventList);
 				}
 				return eventMap;
 			}
 			catch (IOException ex)
 			{
 
 			}
 			return eventMap;
 		}
 
 		@Override
 		protected void onPostExecute(Map<String, List<Event>> eventMap)
 		{
 			exListView.setAdapter(new GroupedEventListAdapter(societyList, eventMap));
 			progressDialog.dismiss();
 		}
 	}
 
 	private static class ViewHolder
 	{
 		public TextView txtEventName;
 		public TextView txtEventDescription;
 		public TextView txtEventDate;
 
 		public ImageView star1;
 		public ImageView star2;
 		public ImageView star3;
 		public ImageView star4;
 		public ImageView star5;
 		public TextView numberOfReviewsDisplay;
 	}
 
 }
