 package fr.insalyon.pyp.gui.events;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 import fr.insalyon.pyp.R;
 import fr.insalyon.pyp.gui.common.BaseActivity;
 import fr.insalyon.pyp.gui.common.IntentHelper;
 import fr.insalyon.pyp.gui.common.popup.Popups;
 import fr.insalyon.pyp.network.ServerConnection;
 import fr.insalyon.pyp.tools.AppTools;
 import fr.insalyon.pyp.tools.Constants;
 import fr.insalyon.pyp.tools.PYPContext;
 
 public class EventActivity extends BaseActivity {
 	private LinearLayout abstractView;
 	private LinearLayout mainView;
 	private ScrollView scrollView;
 	private TextView windowTitle;
 	private ListView list;
 	private ChatAdapter chatAdapter;
 	private Location lastLocation;
 	private Long lastRefresh = System.currentTimeMillis() / 1000;
 	private ViewFlipper vf;
 	private float lastX;
 
 	// Get event details
 	private TextView eventNameField;
 	private TextView eventTypeField;
 	private TextView eventHoursField;
 
 	private TextView eventPriceField;
 	private TextView eventDescriptionField;
 	private TextView eventAgeAverageField;
 	private TextView eventFemaleRatioField;
 	private TextView eventSingleRatioField;
 	private TextView eventHeadcountField;
 	private TextView eventGradeField;
 	private ImageView smiley;
 
 	private TextView eventPlaceNameField;
 	private TextView eventPlaceDescriptionField;
 	private TextView eventPlaceAddressField;
 
 	private LinearLayout gradeZone;
 	private ImageView star1;
 	private ImageView star2;
 	private ImageView star3;
 	private ImageView star4;
 	private ImageView star5;
 	private Button checkInButton;
 
 	private String event_grade;
 	private String event_id;
 
 	// Chat conversation
 	private EditText MessageChat;
 	private Button SendButtonChat;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// TODO: change constant
 		super.onCreate(savedInstanceState, Constants.GET_EVENT_DETAIL_CONST);
 		AppTools.info("on create EventActivity");
 		initGraphicalInterface();
 	}
 
 	private void initGraphicalInterface() {
 		// set layouts
 		LayoutInflater mInflater = LayoutInflater.from(this);
 		abstractView = (LinearLayout) findViewById(R.id.abstractLinearLayout);
 		abstractView.setVisibility(LinearLayout.GONE);
 		abstractView = (LinearLayout) findViewById(R.id.abstractLinearLayoutTop);
 		abstractView.setVisibility(LinearLayout.VISIBLE);
 		mainView = (LinearLayout) mInflater
 				.inflate(R.layout.event_layout, null);
 		abstractView.addView(mainView);
 		vf = (ViewFlipper) findViewById(R.id.view_flipper);
 		windowTitle = (TextView) findViewById(R.id.pageTitle);
 		windowTitle.setText(R.string.NoTitle);
 		scrollView = (ScrollView) findViewById(R.id.scroll_view_event_activity);
 		scrollView.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 
 				return EventActivity.this.onTouchEvent(event);
 			}
 		});
 
 		// Get event details
 		eventNameField = (TextView) findViewById(R.id.event_name);
 		eventHoursField = (TextView) findViewById(R.id.event_hours);
 		eventTypeField = (TextView) findViewById(R.id.event_type);
 
 		eventPriceField = (TextView) findViewById(R.id.event_price);
 		eventDescriptionField = (TextView) findViewById(R.id.event_description);
 		eventAgeAverageField = (TextView) findViewById(R.id.event_age);
 		eventFemaleRatioField = (TextView) findViewById(R.id.event_female);
 		eventSingleRatioField = (TextView) findViewById(R.id.event_single);
 		eventHeadcountField = (TextView) findViewById(R.id.event_headcount);
 		eventGradeField = (TextView) findViewById(R.id.event_grade);
 		smiley = (ImageView) findViewById(R.id.smiley);
 
 		eventPlaceNameField = (TextView) findViewById(R.id.event_place_name);
 		eventPlaceDescriptionField = (TextView) findViewById(R.id.event_place_description);
 		eventPlaceAddressField = (TextView) findViewById(R.id.event_place_address);
 
 		checkInButton = (Button) findViewById(R.id.checkin);
 		checkInButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				new GradeRateEvent().execute(event_id);
 			}
 
 		});
 		
 		gradeZone = (LinearLayout) findViewById(R.id.grade_zone);
 
 		star1 = (ImageView) findViewById(R.id.star1);
 		star2 = (ImageView) findViewById(R.id.star2);
 		star3 = (ImageView) findViewById(R.id.star3);
 		star4 = (ImageView) findViewById(R.id.star4);
 		star5 = (ImageView) findViewById(R.id.star5);
 
 		star1.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				event_grade = "1";
 				SetStars(Integer.decode(event_grade));
 			}
 		});
 
 		star2.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				event_grade = "2";
 				SetStars(Integer.decode(event_grade));
 			}
 		});
 
 		star3.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				event_grade = "3";
 				SetStars(Integer.decode(event_grade));
 			}
 		});
 
 		star4.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				event_grade = "4";
 				SetStars(Integer.decode(event_grade));
 			}
 		});
 
 		star5.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				event_grade = "5";
 				SetStars(Integer.decode(event_grade));
 			}
 		});
 
 		final String[] data = IntentHelper.getActiveIntentParam(String[].class);
 		event_id = data[0];
 
 		new GetEventDetails().execute(event_id);
 		hideHeader(false);
 
 		MessageChat = (EditText) findViewById(R.id.MessageChat);
 		SendButtonChat = (Button) findViewById(R.id.sendButtonChat);
 
 		SendButtonChat.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				if ("".equals(MessageChat.getText().toString())) {
 					Popups.showPopup(Constants.IncompleatData);
 					return;
 				}
 				new SendMessageTask().execute();
 			}
 		});
 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		// check if logged in
 		checkLoggedIn();
 		new GetEventDetails().execute(event_id);
 		new GetConversation().execute(event_id);
 		final Handler handler = new Handler();
 		 Timer timer = new Timer();
 		 TimerTask doAsynchronousTask = new TimerTask() {
 		 @Override
 		 public void run() {
 		 handler.post(new Runnable() {
 		 public void run() {
 		 try {
 		 GetConversation ev = new GetConversation();
 		 ev.execute(event_id);
 		 } catch (Exception e) {
 		 AppTools.error(e.getMessage());
 		 }
 		 }
 		 });
 		 }
 		 };
 		 timer.schedule(doAsynchronousTask, 0, 20000);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent touchevent) {
 		switch (touchevent.getAction()) {
 		case MotionEvent.ACTION_DOWN: {
 			lastX = touchevent.getX();
 			break;
 		}
 
 		case MotionEvent.ACTION_UP: {
 
 			float currentX = touchevent.getX();
 			AppTools.debug("Old:" + lastX + "New:" + currentX);
 
 			if (lastX < currentX && lastX != 0 && currentX - lastX > 100) {
 				if (vf.getDisplayedChild() == 0)
 					break;
 				vf.setInAnimation(this, R.anim.in_from_left);
 				vf.setOutAnimation(this, R.anim.out_to_right);
 				lastLocation = null;
 				new GetEventDetails().execute(event_id);
 				vf.showNext();
 			}
 
 			if (lastX > currentX && lastX != 0 && lastX - currentX > 100) {
 				if (vf.getDisplayedChild() == 1)
 					break;
 				vf.setInAnimation(this, R.anim.in_from_right);
 				vf.setOutAnimation(this, R.anim.out_to_left);
 				new GetConversation().execute(event_id);
 				vf.showPrevious();
 			}
 			lastX = 0;
 			break;
 		}
 		}
 		return false;
 	}
 
 	public void networkError(String error) {
 		if (error.equals("Incomplete data")) {
 			Popups.showPopup(Constants.IncompleatData);
 		}
 		if (error.equals("Not checked in")) {
 			gradeZone.setVisibility(View.GONE);
 		}
 	}
 
 	// Chat conversation
 
 	private void buildList(ArrayList<String[]> data) {
 
 		list = (ListView) findViewById(R.id.get_chat_list);
 
 		// Getting adapter by passing xml data ArrayList
 		chatAdapter = new ChatAdapter(this, data);
 		list.setAdapter(chatAdapter);
 		list.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 
 				return EventActivity.this.onTouchEvent(event);
 			}
 		});
 	}
 
 	private class SendMessageTask extends AsyncTask<Void, Void, Void> {
 
 		// ProgressDialog mProgressDialog;
 		JSONObject res;
 
 		@Override
 		protected void onPostExecute(Void result) {
 			if (res != null) {
 				try {
 					if (res.has("error")) {
 						// Error
 						String error;
 						error = res.getString("error");
 						EventActivity.this.networkError(error);
 					} else {
 						MessageChat.setText("");
 						new GetConversation().execute(event_id);
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		@Override
 		protected void onPreExecute() {
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			// Send request to server for login
 
 			ServerConnection srvCon = ServerConnection.GetServerConnection();
 			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 			parameters.add(new BasicNameValuePair("message", Html.fromHtml(MessageChat
 					.getText().toString()).toString()));
 			if (event_id != null)
 				parameters.add(new BasicNameValuePair("event_id", event_id));
 			parameters.add(new BasicNameValuePair("auth_token", PYPContext
 					.getContext().getSharedPreferences(AppTools.PREFS_NAME, 0)
 					.getString("auth_token", "")));
 			try {
 				res = srvCon.connect(ServerConnection.ADD_MESSAGE, parameters);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 	}
 
 	private class GetConversation extends AsyncTask<String, Void, Void> {
 
 		JSONObject res;
 
 		@Override
 		protected void onPostExecute(Void result) {
 			if (res != null) {
 				try {
 					JSONArray array = res.getJSONArray("list");
 					ArrayList<String[]> data = new ArrayList<String[]>();
 					for (int i = 0; i < array.length(); i++) {
 						JSONObject obj = array.getJSONObject(i);
 						data.add(new String[] { obj.getString("date"),
 								obj.getString("message"), obj.getString("user") });
 					}
 					EventActivity.this.buildList(data);
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		@Override
 		protected void onPreExecute() {
 		}
 
 		@Override
 		protected Void doInBackground(String... params) {
 			// Send request to server for login
 			ServerConnection srvCon = ServerConnection.GetServerConnection();
 			try {
 				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 				SharedPreferences settings = PYPContext.getContext()
 						.getSharedPreferences(AppTools.PREFS_NAME, 0);
 				parameters.add(new BasicNameValuePair("auth_token", settings
 						.getString("auth_token", "")));
 				parameters.add(new BasicNameValuePair("event_id", params[0]));
 				AppTools.debug("ID of the event: " + params[0]);
 				res = srvCon.connect(ServerConnection.GET_CONVERSATION,
 						parameters);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 	
 	private void SetStars(int stars){
 		switch(stars){
 			case 1: star1.setImageDrawable(PYPContext.getContext().getResources()
 						.getDrawable(android.R.drawable.btn_star_big_on));
 					star2.setImageDrawable(PYPContext.getContext().getResources()
 						.getDrawable(android.R.drawable.btn_star_big_off));
 					star3.setImageDrawable(PYPContext.getContext().getResources()
 							.getDrawable(android.R.drawable.btn_star_big_off));
 					star4.setImageDrawable(PYPContext.getContext().getResources()
 							.getDrawable(android.R.drawable.btn_star_big_off));
 					star5.setImageDrawable(PYPContext.getContext().getResources()
 							.getDrawable(android.R.drawable.btn_star_big_off));
 					break;
 			case 2: star1.setImageDrawable(PYPContext.getContext().getResources()
 					.getDrawable(android.R.drawable.btn_star_big_on));
 				star2.setImageDrawable(PYPContext.getContext().getResources()
 					.getDrawable(android.R.drawable.btn_star_big_on));
 				star3.setImageDrawable(PYPContext.getContext().getResources()
 						.getDrawable(android.R.drawable.btn_star_big_off));
 				star4.setImageDrawable(PYPContext.getContext().getResources()
 						.getDrawable(android.R.drawable.btn_star_big_off));
 				star5.setImageDrawable(PYPContext.getContext().getResources()
 						.getDrawable(android.R.drawable.btn_star_big_off));
 				break;
 			case 3: star1.setImageDrawable(PYPContext.getContext().getResources()
 					.getDrawable(android.R.drawable.btn_star_big_on));
 				star2.setImageDrawable(PYPContext.getContext().getResources()
 					.getDrawable(android.R.drawable.btn_star_big_on));
 				star3.setImageDrawable(PYPContext.getContext().getResources()
 						.getDrawable(android.R.drawable.btn_star_big_on));
 				star4.setImageDrawable(PYPContext.getContext().getResources()
 						.getDrawable(android.R.drawable.btn_star_big_off));
 				star5.setImageDrawable(PYPContext.getContext().getResources()
 						.getDrawable(android.R.drawable.btn_star_big_off));
 				break;
 			case 4: star1.setImageDrawable(PYPContext.getContext().getResources()
 					.getDrawable(android.R.drawable.btn_star_big_on));
 				star2.setImageDrawable(PYPContext.getContext().getResources()
 					.getDrawable(android.R.drawable.btn_star_big_on));
 				star3.setImageDrawable(PYPContext.getContext().getResources()
 						.getDrawable(android.R.drawable.btn_star_big_on));
 				star4.setImageDrawable(PYPContext.getContext().getResources()
 						.getDrawable(android.R.drawable.btn_star_big_on));
 				star5.setImageDrawable(PYPContext.getContext().getResources()
 						.getDrawable(android.R.drawable.btn_star_big_off));
 				break;
 			case 5: star1.setImageDrawable(PYPContext.getContext().getResources()
 					.getDrawable(android.R.drawable.btn_star_big_on));
 				star2.setImageDrawable(PYPContext.getContext().getResources()
 					.getDrawable(android.R.drawable.btn_star_big_on));
 				star3.setImageDrawable(PYPContext.getContext().getResources()
 						.getDrawable(android.R.drawable.btn_star_big_on));
 				star4.setImageDrawable(PYPContext.getContext().getResources()
 						.getDrawable(android.R.drawable.btn_star_big_on));
 				star5.setImageDrawable(PYPContext.getContext().getResources()
 						.getDrawable(android.R.drawable.btn_star_big_on));
 				break;
 		}
 	}
 	
 	// Event Details
 
 	private class GetEventDetails extends AsyncTask<String, Void, Void> {
 
 		JSONObject res;
 		ProgressDialog mProgressDialog;
 
 		@Override
 		protected void onPostExecute(Void result) {
 			mProgressDialog.dismiss();
 			if (res != null) {
 				try {
 					if (res.has("error")) {
 						// Error
 						String error;
 						error = res.getString("error");
 						EventActivity.this.networkError(error);
 					} else {
 						// Put the data in form
 						eventNameField.setText(res.getString("name"));
 						eventTypeField.setText(res.getString("type"));
 						eventHoursField.setText(res.getString("start_time")
 								+ " - " + res.getString("end_time"));
 
 						eventPriceField.setText("Price : "
 								+ res.getString("price") + " €");
 						String description = res.getString("description");
 						TextView DescriptionLabel = (TextView) findViewById(R.id.event_description_label);
						if( "".equals(description) ){
 							DescriptionLabel.setVisibility(View.GONE);
 						}else{
 							DescriptionLabel.setVisibility(View.VISIBLE);
 						}
 						eventDescriptionField.setText(description);
 						
 						
 						eventAgeAverageField.setText(res
 								.getString("age_average"));
 						eventFemaleRatioField.setText(res
 								.getString("female_ratio"));
 						eventSingleRatioField.setText(res
 								.getString("single_ratio"));
 						eventHeadcountField.setText(res.getString("headcount"));
 						
 						
 						eventGradeField.setText(res.getString("stars"));
 						
 						int stars = Integer.valueOf(res.getString("stars"));
 						if( !"".equals(stars))
 							stars = 1;
 						// Set the smiley indicator
 						if( stars >= 1 && stars <= 2)
 							smiley.setImageDrawable((PYPContext.getContext().getResources()
 						.getDrawable(R.drawable.smiley_bad)));
 						else if( stars > 2 && stars <= 4 )
 							smiley.setImageDrawable((PYPContext.getContext().getResources()
 									.getDrawable(R.drawable.smiley_normal)));
 						else
 							smiley.setImageDrawable((PYPContext.getContext().getResources()
 									.getDrawable(R.drawable.smiley_happy)));
 						
 						eventPlaceNameField
 								.setText(res.getString("place_name"));
 						
 						String placeDescription = res.getString("place_description");
 						TextView EventDescriptionLabel = (TextView) findViewById(R.id.event_place_description_label);
						if( "".equals(placeDescription) ){
 							EventDescriptionLabel.setVisibility(View.GONE);
 						}else{
 							EventDescriptionLabel.setVisibility(View.VISIBLE);
 						}
 						eventPlaceDescriptionField.setText(placeDescription);
 						
 						String placeAddress = res.getString("place_address");
 						TextView AddressLabel = (TextView) findViewById(R.id.event_address_label);
						if( "".equals(placeAddress) ){
 							AddressLabel.setVisibility(View.GONE);
 						}else{
 							AddressLabel.setVisibility(View.VISIBLE);
 						}
 						eventPlaceAddressField.setText(description);
 						
 						// Set the seperators and smiley to visible
 						smiley.setVisibility(View.VISIBLE);
 						LinearLayout separator1 = (LinearLayout) findViewById(R.id.separator1);
 						separator1.setVisibility(View.VISIBLE);
 						LinearLayout separator2 = (LinearLayout) findViewById(R.id.separator2);
 						separator2.setVisibility(View.VISIBLE);
 						LinearLayout separator3 = (LinearLayout) findViewById(R.id.separator3);
 						separator3.setVisibility(View.VISIBLE);
 						
 						new GetPersonalRateEvent().execute();
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		@Override
 		protected void onPreExecute() {
 			mProgressDialog = ProgressDialog.show(EventActivity.this,
 					getString(R.string.app_name), getString(R.string.loading));
 			AppTools.debug("Loading event details");
 		}
 
 		@Override
 		protected Void doInBackground(String... params) {
 			// Send request to server for login
 			ServerConnection srvCon = ServerConnection.GetServerConnection();
 			try {
 				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 				SharedPreferences settings = PYPContext.getContext()
 						.getSharedPreferences(AppTools.PREFS_NAME, 0);
 				parameters.add(new BasicNameValuePair("auth_token", settings
 						.getString("auth_token", "")));
 				parameters.add(new BasicNameValuePair("id", params[0]));
 				AppTools.debug("ID of the event: " + params[0]);
 				res = srvCon.connect(ServerConnection.GET_EVENT_FULL_INFO,
 						parameters);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 
 	private class GradeRateEvent extends AsyncTask<String, Void, Void> {
 
 		JSONObject res;
 		ProgressDialog mProgressDialog;
 
 		@Override
 		protected void onPostExecute(Void result) {
 			mProgressDialog.dismiss();
 			if (res != null) {
 				try {
 					if (res.has("error")) {
 						// Error
 						String error;
 						error = res.getString("error");
 						EventActivity.this.networkError(error);
 					} else {
 						// Disable button
 						// TODO: already grade it
 						// TODO: pop up
 						Popups.showPopup(Constants.ThankGradeEvent);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		@Override
 		protected void onPreExecute() {
 			mProgressDialog = ProgressDialog.show(EventActivity.this,
 					getString(R.string.app_name), getString(R.string.loading));
 			AppTools.debug("Loading events");
 		}
 
 		@Override
 		protected Void doInBackground(String... params) {
 			// Send request to server for login
 			ServerConnection srvCon = ServerConnection.GetServerConnection();
 			try {
 				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 				SharedPreferences settings = PYPContext.getContext()
 						.getSharedPreferences(AppTools.PREFS_NAME, 0);
 				parameters.add(new BasicNameValuePair("auth_token", settings
 						.getString("auth_token", "")));
 				parameters.add(new BasicNameValuePair("event_id", params[0]));
 				parameters.add(new BasicNameValuePair("stars", event_grade));
 				AppTools.debug("ID of the event: " + params[0]);
 				res = srvCon.connect(ServerConnection.STAR, parameters);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 	
 	
 	private class GetPersonalRateEvent extends AsyncTask<String, Void, Void> {
 
 		JSONObject res;
 		ProgressDialog mProgressDialog;
 
 		@Override
 		protected void onPostExecute(Void result) {
 			mProgressDialog.dismiss();
 			if (res != null) {
 				try {
 					if (res.has("error")) {
 						// Error
 						String error;
 						error = res.getString("error");
 						EventActivity.this.networkError(error);
 					} else {
 						event_grade = res.getString("stars");
 						gradeZone.setVisibility(View.VISIBLE);
 						if( "".equals(event_grade) )
 							//TODO: show pop up or something
 							SetStars(Integer.decode(event_grade));
 						else
 							SetStars(Integer.decode(event_grade));
 						// TODO: able disable button
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		@Override
 		protected void onPreExecute() {
 			mProgressDialog = ProgressDialog.show(EventActivity.this,
 					getString(R.string.app_name), getString(R.string.loading));
 			AppTools.debug("Loading events");
 		}
 
 		@Override
 		protected Void doInBackground(String... params) {
 			// Send request to server for login
 			ServerConnection srvCon = ServerConnection.GetServerConnection();
 			try {
 				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 				SharedPreferences settings = PYPContext.getContext()
 						.getSharedPreferences(AppTools.PREFS_NAME, 0);
 				parameters.add(new BasicNameValuePair("auth_token", settings
 						.getString("auth_token", "")));
 				parameters.add(new BasicNameValuePair("event_id", event_id));
 				AppTools.debug("ID of the event: " + event_id);
 				res = srvCon.connect(ServerConnection.GET_USER_STARS, parameters);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 	
 
 }
