 package com.connectsy.events;
 
 import android.content.Context;
 import android.content.Intent;
 import android.text.Html;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.connectsy.LocManager;
 import com.connectsy.R;
 import com.connectsy.data.AvatarFetcher;
 import com.connectsy.data.DataManager.DataUpdateListener;
 import com.connectsy.events.EventManager.Event;
 import com.connectsy.utils.DateUtils;
 import com.connectsy.utils.Utils;
 
 public class EventRenderer implements DataUpdateListener {
 	@SuppressWarnings("unused")
 	private static final String TAG = "EventRenderer";
 	private EventManager evMan;
 	Context context;
 	View view;
 	String rev;
 	boolean truncate;
 	Event event;
 	
 	public EventRenderer(Context context, View view, String rev, boolean truncate){
 		evMan = new EventManager(context, this, null, null);
 		this.context = context;
 		this.view = view;
 		this.rev = rev;
 		this.truncate = truncate;
 		event = evMan.getEvent(rev);
 		if (event == null)
 			evMan.refreshEvent(rev, 0);
 		else
 			render();
 	}
 	
 	private void render(){
 
 		final OnClickListener userClick = new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent i = new Intent(Intent.ACTION_VIEW);
 				i.setType("vnd.android.cursor.item/vnd.connectsy.user");
 				i.putExtra("com.connectsy.user.username", event.creator);
 				context.startActivity(i);
 			}
 		};
 
 		view.findViewById(R.id.event_loading).setVisibility(View.GONE);
 		
 		ImageView avatar = (ImageView) view.findViewById(R.id.event_avatar);
 		avatar.setOnClickListener(userClick);
 		new AvatarFetcher(context, event.creator, avatar).fetch();
 
 		TextView username = (TextView) view.findViewById(R.id.event_username);
 		username.setText(event.creator);
 		username.setOnClickListener(userClick);
 
 		if (event.category != null && !event.category.equals("")) {
 			view.findViewById(R.id.event_pipe).setVisibility(View.VISIBLE);
 			TextView category = (TextView) view
 					.findViewById(R.id.event_category);
 			category.setVisibility(View.VISIBLE);
 			category.setText(event.category);
 			category.setOnClickListener(new TextView.OnClickListener() {
 				public void onClick(View v) {
 					Intent i = new Intent(Intent.ACTION_VIEW);
 					i.setType("vnd.android.cursor.dir/vnd.connectsy.event");
 					i.putExtra("filter", EventManager.Filter.CATEGORY);
 					i.putExtra("category", event.category);
 					context.startActivity(i);
 				}
 			});
 		}
 
 		TextView where = (TextView) view.findViewById(R.id.event_where);
 		where.setText(Html.fromHtml("<b>where:</b> "
 				+ Utils.maybeTruncate(event.where, 25, truncate)));
 		TextView what = (TextView) view.findViewById(R.id.event_what);
 		what.setText(Html.fromHtml("<b>what:</b> "
 				+ Utils.maybeTruncate(event.description, 25, truncate)));
 		TextView when = (TextView) view.findViewById(R.id.event_when);
 		when.setText(Html.fromHtml("<b>when:</b> "
 				+ DateUtils.formatTimestamp(event.when)));
 
 		TextView distance = (TextView) view.findViewById(R.id.event_distance);
 		String distanceText = new LocManager(context).distanceFrom(
 				event.posted_from[0], event.posted_from[1]);
 		if (distanceText != null)
 			distance.setText(distanceText);
 		else
 			distance.setVisibility(View.VISIBLE);
 		TextView created = (TextView) view.findViewById(R.id.event_created);
 		created.setText("created " + DateUtils.formatTimestamp(event.created));
 	}
 
 	public void onDataUpdate(int code, String response) {
 		event = evMan.getEvent(rev);
 		render();
 	}
 
 	public void onRemoteError(int httpStatus, int code) {}
 }
