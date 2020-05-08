 package fr.insalyon.pyp.gui.events;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import fr.insalyon.pyp.R;
 import fr.insalyon.pyp.gui.common.BaseActivity;
 import fr.insalyon.pyp.gui.common.IntentHelper;
 import fr.insalyon.pyp.network.ServerConnection;
 import fr.insalyon.pyp.tools.AppTools;
 import fr.insalyon.pyp.tools.Constants;
 import fr.insalyon.pyp.tools.PYPContext;
 
 public class GetEventDetailActivity extends BaseActivity {
 	private LinearLayout abstractView;
	private LinearLayout mainView;
 	private TextView windowTitle;
 	private TextView eventNameField;
 	private TextView eventTypeField;
 	private TextView eventHoursField;
 	private TextView checkInTxt;
 	private String id;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState,
 				Constants.GET_EVENT_DETAIL_CONST);
 		AppTools.info("on create GetEventDetailActivity");
 		initGraphicalInterface();
 	}
 
 	private void initGraphicalInterface() {
 		// set layouts
 		LayoutInflater mInflater = LayoutInflater.from(this);
 		abstractView = (LinearLayout) findViewById(R.id.abstractLinearLayout);
		mainView = (LinearLayout) mInflater.inflate(
				R.layout.manage_personal_event_activity, null);
 		abstractView.addView(mainView);
 		windowTitle = (TextView) findViewById(R.id.pageTitle);
 		windowTitle.setText(R.string.GetEventDetail);
 		
 		eventNameField = (TextView) findViewById(R.id.event_name);
 		eventHoursField = (TextView) findViewById(R.id.event_hours);
 		eventTypeField = (TextView) findViewById(R.id.event_type);
 		
 		
 		
 		checkInTxt = (TextView) findViewById(R.id.check_in);
 		
 		
 		final String[] data = IntentHelper.getActiveIntentParam(String[].class);
 //		Drawable background = ((LinearLayout) findViewById(R.id.event_detail_backgorund))
 //				.getBackground();
 //		background.setAlpha(95);
 		id = data[0];
		AppTools.debug(data[1]);
 //		if (data[1].equals("false")) {
 //			checkInTxt.setText("Your in!!!");
 //		} else {
 //			checkInTxt.setText("Your out!!!");
 //		}
 		new GetEventDetails().execute(id);
 		hideHeader(false);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 	}
 
 	private class GetEventDetails extends AsyncTask<String, Void, Void> {
 
 		JSONObject res;
 
 		@Override
 		protected void onPostExecute(Void result) {
 			if (res != null) {
 				try {
 					// Put the data in form
 					eventNameField.setText(res.getString("name"));
 					eventTypeField.setText(res.getString("type"));
 					eventHoursField.setText(res.getString("start_time")+" - "+res.getString("end_time"));
 					
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
 				res = srvCon.connect(ServerConnection.GET_EVENT_FULL_INFO,
 						parameters);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 
 }
