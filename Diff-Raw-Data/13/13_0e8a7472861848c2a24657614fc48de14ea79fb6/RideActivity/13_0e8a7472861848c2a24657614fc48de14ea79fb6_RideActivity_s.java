 package com.itay.mandewho;
 
 import com.parse.*;
 
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.parse.Parse;
 import com.parse.ParseFacebookUtils;
 import com.parse.ParseObject;
 import com.parse.ParseUser;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class RideActivity extends Activity {
 
 	ParseUser _currentUser;
 	String _rideId;
 	ParseObject _ride;
 	View _screen;
 	String _request;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		//getWindow().setWindowAnimations(android.R.anim.fade_in);
 		setContentView(R.layout.activity_ride);
 		Bundle b = getIntent().getExtras();
 		_rideId = b.getString("rideId");
 		Parse.initialize(this, "4PgddgAXZjvMhpVJeILJ54dNH21NaRTpR4kTCVqQ", "1ur95H52EAwP6UVPNEHoHWEUYXGVmu5K1QwqZpyB");
 		ParseFacebookUtils.initialize("537020433036560");
 		_currentUser = ParseUser.getCurrentUser();
 		_screen = findViewById(R.id.inner_view);
 		_screen.setVisibility(View.GONE);
 		buttonsListener listener = new buttonsListener();
 		findViewById(R.id.call_button).setOnClickListener(listener);
 		findViewById(R.id.mail_button).setOnClickListener(listener);
 		findViewById(R.id.sms_button).setOnClickListener(listener);
 		findViewById(R.id.delete_button).setOnClickListener(listener);
 		getRideData();
 
 	}
 	private void getRideData() {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery(Ride.RIDE);
 		query.getInBackground(_rideId, new GetCallback<ParseObject>() {
 			public void done(ParseObject ride, ParseException e) {
 				if (e == null) {
 					objectWasRetrievedSuccessfully(ride);
 				} else {
 					System.out.println("error with getting object");
 				}
 			}
 		});
 
 	}
 
 	protected void objectWasRetrievedSuccessfully(ParseObject ride) {
 		_ride=ride;
 		if(_ride==null)
 		{
 			return;
 		}
 		hideOrShowFewButton();
 		
 		_screen.setVisibility(View.VISIBLE);
 		ImageView pageIcon = (ImageView) findViewById(R.id.page_icon);
 		TextView pageKindView = (TextView) findViewById(R.id.page_kind);
 		String pageKindString;
 		if(_ride.getBoolean(Ride.RIDEKIND))
 		{
 			pageIcon.setImageResource(R.drawable.car_reg);
 			pageKindString = getString(R.string.ride);
 			pageKindView.setText(pageKindString);
 			_request = getResources().getString(R.string.IwantText);
 		}
 		else
 		{
 			pageIcon.setImageResource(R.drawable.rider_reg);
 			pageKindString = getString(R.string.rider);
 			pageKindView.setText(pageKindString);
 			_request = getResources().getString(R.string.IcanText);
 		}
 		addGroupName();
 
 	}
 	private void hideOrShowFewButton() {
 		if(_ride.get(Ride.PUBLISHERID).equals(_currentUser.getObjectId()))
 		{
 			((View)findViewById(R.id.delete_layer)).setVisibility(View.VISIBLE);
 		}
 		if(_ride.get(Ride.COMMENTS)==null || _ride.get(Ride.COMMENTS).equals(""))
 		{
 			((TextView)findViewById(R.id.rideDetails)).setVisibility(View.GONE);
 		}
 		if(_ride.get(Ride.BYMAIL)==null || _ride.get(Ride.BYMAIL).equals(""))
 		{
 			((Button)findViewById(R.id.mail_button)).setVisibility(View.GONE);
 		}
 		if(_ride.get(Ride.BYPHONE)==null || _ride.get(Ride.BYPHONE).equals(""))
 		{
 			((Button)findViewById(R.id.call_button)).setVisibility(View.GONE);
 		}
 		if(_ride.get(Ride.BYSMS)==null || _ride.get(Ride.BYSMS).equals(""))
 		{
 			((Button)findViewById(R.id.sms_button)).setVisibility(View.GONE);
 		}
 		
 	}
 	private void addGroupName() {
 		TextView groupNameView = (TextView) findViewById(R.id.groupNameRideView);
 		String groupName="";
 		JSONArray groupsData = _currentUser.getJSONArray(UserFields.GROUPS);
 		List<String> publishedGroupsIDs = _ride.getList(Ride.GROUPS);
 		try {
 			for (int i = 0; i < groupsData.length(); i++) {
 				JSONObject groupData = (JSONObject) groupsData.get(i);
 				if(publishedGroupsIDs.contains(groupData.getString("id")))
 				{
 					groupName=groupData.getString("name");
 					break;
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.toString());
 		}
 		groupNameView.setText(groupName);
 
 	}
 	@Override
 	public void onBackPressed()
 	{
 		this.finish();
 		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
 		return ;
 	}
 
 	/*
 	 * A button listener class
 	 */
 	private class buttonsListener implements View.OnClickListener 
 	{
 		public void onClick(View v) {
 			switch(v.getId()){
 			case R.id.call_button:
 				phoneClicked();
 				break;
 			case R.id.mail_button:
 				mailClicked();
 				break;
 			case R.id.sms_button:
 				smsClicked();
 				break;
 			case R.id.delete_button:
 				deleteClicked();
 				break;
 			}
 		}
 	}
 
 	public void smsClicked() {
 		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
 		smsIntent.setData(Uri.parse("sms:"+_ride.get(Ride.BYSMS)));
 		smsIntent.putExtra("sms_body",_request);
 		startIntnet(smsIntent);
 
 	}
 
 	public void deleteClicked() {
 		if(_ride!=null)
 		{
 			_ride.deleteInBackground();
 			onBackPressed();
 		}
 		
 	}
 	public void mailClicked() {
 		
 		Intent mailIntent = new Intent(Intent.ACTION_SEND,Uri.parse("mailto:"+_ride.get(Ride.BYMAIL)));
 		mailIntent.setType("plain/text");
 		mailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.ride));
 		mailIntent.putExtra(Intent.EXTRA_TEXT, _request);
 		startIntnet(mailIntent);
 		
 	}
 	public void phoneClicked() {
 		Intent phoneIntent = new Intent(Intent.ACTION_VIEW);
 		phoneIntent.setData(Uri.parse("tel:"+_ride.get(Ride.BYPHONE)));
 		startIntnet(phoneIntent);
 	}
 
 	public void startIntnet(Intent intent)
 	{
 		try {
 			startActivity(intent);
 		} catch (Exception e) {
 			System.out.println(e.toString());
 		}
 	}
 }
