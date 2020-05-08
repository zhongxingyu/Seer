 package com.skybot.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.NameValuePair;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.TranslateAnimation;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.skybot.activities.delegate.ActionDelegate;
 import com.skybot.adapters.JobsAdapter;
 import com.skybot.connection.connection.BaseNetworkManager;
 import com.skybot.connection.connection.helper.RequestCreator;
 import com.skybot.connection.connection.helper.RequestHelper;
 import com.skybot.serivce.parser.dataholder.DataHolder;
 import com.skybot.util.Constants;
 import com.skybot.util.Util;
 import com.skybot.util.ViewTracker;
 
 /**
  * Activity for representing Dashboard items for overall statistics. This class
  * will contain also ScrollView implementation control
  * 
  * @author gor, armenabrahamyan
  * 
  */
 public class JobsActivity extends SwipeListViewActivity implements
 		ActionDelegate {
 
 	private ListView listView;
 	private boolean directionRight = false;
 	private JobsAdapter adapter;
 	public static ArrayList<HashMap<String, String>> jobsList = new ArrayList<HashMap<String, String>>();
 
 	static final String KEY_TITLE = "title";
 	static final String KEY_DESCRIPTION = "description";
 	static final String KEY_AGENT = "agent";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.jobs_list);
 
 		listView = (ListView) findViewById(R.id.listView1);
 		adapter = new JobsAdapter(this, jobsList);
 		listView.setAdapter(adapter);
 
 	}
 
 	private void getJobsResponse() {
 		Util.showOrHideActivityIndicator(JobsActivity.this.getParent(), 0,
 				"Requesting Jobs List...");
 		String system_Time = Long.toString(System.currentTimeMillis());
 		RequestCreator creator = new RequestCreator();
 		BaseNetworkManager baseNetworkManager = new BaseNetworkManager();
 
 		Map<String, String> job_params = creator.createAppropriateMapRequest(
 				Constants.DATE, system_Time, Constants.RESULTS, "300",
 				Constants.SORT, "name", Constants.DIRECTION, "ASC",
 				Constants.TAG, "", Constants.TAG_MATCH_ANY, "false",
 				Constants.START, "0", Constants.LIMIT, "300");
 
 		final RequestHelper reqHelper = new RequestHelper();
 		String urlStringWithParams = reqHelper.constructGetRequestString(
 				job_params, Constants.SERVER_URL, Constants.JOB_SERVICE_URL);
 
 		baseNetworkManager.constructConnectionAndHitGET("Login Successful",
 				"Jobs Request Started", urlStringWithParams, this,
 				Constants.JOBS_VIEW, Constants.JOB_SERVICE_URL);
 	}
 
 	public void signOutRequest() {
 		BaseNetworkManager baseNetworkManager = new BaseNetworkManager();
 		String urlStringWithParams = Constants.SERVER_URL
 				+ Constants.RIGHT_SLASH + Constants.SIGN_OUT;
 
 		baseNetworkManager.constructConnectionAndHitGET("Log out Successful",
 				"Log out Request Started", urlStringWithParams, this,
 				Constants.LOGOUT_VIEW, Constants.SIGN_OUT);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		ViewTracker.getInstance().setCurrentContext(this);
 		if (DataHolder.getInstance().jobsList.isEmpty()) {
 			getJobsResponse();
 		}
 
 		listView = getListView();
 		if (jobsList != null) {
 			adapter.data = jobsList;
 			adapter.notifyDataSetChanged();
 		}
 	}
 
 	public void runJob(View v, String id) {
 
 		String system_Time = Long.toString(System.currentTimeMillis());
 		RequestCreator creator = new RequestCreator();
 		final RequestHelper reqHelper = new RequestHelper();
 		BaseNetworkManager baseNetworkManager = new BaseNetworkManager();
 
 		Map<String, String> command_params = creator
 				.createAppropriateMapRequest(Constants.CS_ID, "1006",
 						Constants.CS_TYPE, "1", Constants.JOB_ID, id,
 						Constants.DATE, system_Time, Constants.RESULTS, "300",
 						Constants.SORT, "id", Constants.DIRECTION, "ASC",
 						Constants.START, "0", Constants.LIMIT, "300");
 
 		String urlStringWithParams = reqHelper.constructGetRequestString(
 				command_params, Constants.SERVER_URL, Constants.COMMAND_URL);
 		baseNetworkManager.constructConnectionAndHitGET("Command Successful",
 				"Command Request Started", urlStringWithParams, this,
 				"Commands", Constants.COMMAND_URL);
 
 		Map<String, String> run_job_params = creator
 				.createAppropriateMapRequest("cmd_seq", "1", "skip_reactive",
 						"false", "clear_reactivity", "false",
 						"ignore_monitors", "false", "ignore_conditions",
 						"false", "run_priority", "50", "authenticity_token",
 						LoginActivity.authToken);
 
 		final List<NameValuePair> paramsList = reqHelper
 				.createPostDataWithKeyValuePair(run_job_params);
 		String service_url = Constants.JOB_SERVICE + "/" + id + "/"
 				+ Constants.DO_NOW;
 
 		baseNetworkManager.constructConnectionAndHitPOST("Run Successful",
 				"Run Job Request Started", paramsList, this,
 				Constants.JOBS_VIEW, service_url);
 	}
 
 	public void holdJob(View v, String id) {
 		String system_Time = Long.toString(System.currentTimeMillis());
 		RequestCreator creator = new RequestCreator();
 		BaseNetworkManager baseNetworkManager = new BaseNetworkManager();
 		final RequestHelper reqHelper = new RequestHelper();
 
 		Map<String, String> hold_job_params = creator
 				.createAppropriateMapRequest("refresh_grids", "true",
 						"authenticity_token", LoginActivity.authToken, "id",
 						id, "hold", "2", "hold_for", "", "hold_until", "");
 
 		final List<NameValuePair> paramsList = reqHelper
 				.createPostDataWithKeyValuePair(hold_job_params);
 		String service_url = Constants.JOB_SERVICE + Constants.RIGHT_SLASH + id
 				+ Constants.RIGHT_SLASH + Constants.HOLD_JOB;
 
 		baseNetworkManager.constructConnectionAndHitPOST("Hold Successful",
 				"Hold Job Request Started", paramsList, this,
 				Constants.JOBS_VIEW, service_url);
 
 		Map<String, String> command_params = creator
 				.createAppropriateMapRequest(Constants.DATE, system_Time,
 						Constants.SORT, "id", Constants.DIRECTION, "ASC",
 						Constants.TAG, "", Constants.TAG_MATCH_ANY, "false",
 						Constants.START, "0", Constants.LIMIT, "300");
 
 		String urlStringWithParams = reqHelper
 				.constructGetRequestString(command_params,
 						Constants.SERVER_URL, Constants.JOB_SERVICE_URL);
 
 		baseNetworkManager.constructConnectionAndHitGET("Hold Successful",
 				"Hold Job Request Started", urlStringWithParams, this,
 				Constants.JOBS_VIEW, Constants.JOB_SERVICE_URL);
 	}
 
 	public void releaseJob(View v, String id) {
 		String system_Time = Long.toString(System.currentTimeMillis());
 		RequestCreator creator = new RequestCreator();
 		BaseNetworkManager baseNetworkManager = new BaseNetworkManager();
 		final RequestHelper reqHelper = new RequestHelper();
 
 		Map<String, String> release_job_params = creator
 				.createAppropriateMapRequest("authenticity_token",
 						LoginActivity.authToken);
 
 		final List<NameValuePair> paramsList = reqHelper
 				.createPostDataWithKeyValuePair(release_job_params);
 		String service_url = Constants.JOB_SERVICE + Constants.RIGHT_SLASH + id
 				+ Constants.RIGHT_SLASH + Constants.RELEASE_JOB;
 
 		baseNetworkManager.constructConnectionAndHitPOST("Release Successful",
 				"Release Job Request Started", paramsList, this,
 				Constants.JOBS_VIEW, service_url);
 
 		Map<String, String> command_params = creator
 				.createAppropriateMapRequest(Constants.DATE, system_Time,
 						Constants.SORT, "id", Constants.DIRECTION, "ASC",
 						Constants.TAG, "", Constants.TAG_MATCH_ANY, "false",
 						Constants.START, "0", Constants.LIMIT, "300");
 
 		String urlStringWithParams = reqHelper
 				.constructGetRequestString(command_params,
 						Constants.SERVER_URL, Constants.JOB_SERVICE_URL);
 
 		baseNetworkManager.constructConnectionAndHitGET("Release Successful",
 				"Release Job Request Started", urlStringWithParams, this,
 				Constants.JOBS_VIEW, Constants.JOB_SERVICE_URL);
 	}
 
 	@Override
 	public void didFinishRequestProcessing() {
 
 	}
 
 	@Override
 	public void didFinishRequestProcessing(
 			ArrayList<HashMap<String, String>> list, String service) {
 		if (list != null) {
 			Util.showOrHideActivityIndicator(JobsActivity.this.getParent(), 1,
 					"Requesting Jobs List...");
 			jobsList = list;
 			adapter.data = jobsList;
 			adapter.notifyDataSetChanged();
 		}
 	}
 
 	@Override
 	public void didFailRequestProcessing() {
 		Util.showOrHideActivityIndicator(JobsActivity.this.getParent(), 1,
 				"Requesting Jobs List...");
 
 		Toast.makeText(getApplicationContext(), "Request Failed",
 				Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void didFinishRequestProcessing(
 			ArrayList<HashMap<String, String>> list) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public ListView getListView() {
 		return listView;
 	}
 
 	@Override
 	public void getSwipeItem(boolean isRight, int position) {
 
 		View rowView = listView.getChildAt(position);
 		if (isRight) {
 			if (!directionRight) {
 				rowView.startAnimation(getDeleteAnimation(0,
 						rowView.getWidth(), position));
 				directionRight = true;
 			} else {
 				rowView.startAnimation(getDeleteAnimation(0,
 						rowView.getWidth(), position));
 				directionRight = false;
 			}
 		} else {
 			if (directionRight) {
 				rowView.startAnimation(getDeleteAnimation(rowView.getWidth(),
 						0, position));
 				directionRight = false;
 			} else {
 				rowView.startAnimation(getDeleteAnimation(rowView.getWidth(),
 						0, position));
 				directionRight = true;
 			}
 		}
 
 	}
 
 	@Override
 	public void onItemClickListener(ListAdapter adapter, int position) {
 
 		if (directionRight)
 			getSwipeItem(false, position);
 	}
 
 	private Animation getDeleteAnimation(float fromX, float toX, int position) {
 		Animation animation = new TranslateAnimation(fromX, toX, 0, 0);
 		animation.setStartOffset(100);
 		animation.setDuration(250);
 		animation.setAnimationListener(new DeleteAnimationListenter(position));
 		animation.setInterpolator(AnimationUtils.loadInterpolator(this,
 				android.R.anim.linear_interpolator));
 		return animation;
 	}
 
 	public class DeleteAnimationListenter implements
 			Animation.AnimationListener {
 		private int position;
 
 		public DeleteAnimationListenter(int position) {
 			this.position = position;
 		}
 
 		@Override
 		public void onAnimationEnd(Animation arg0) {
 			View rowView = listView.getChildAt(position);
 			if (directionRight) {
 				rowView.setBackgroundColor(Color.GRAY);
 				rowView.findViewById(R.id.title).setVisibility(View.INVISIBLE);
 				rowView.findViewById(R.id.description).setVisibility(
 						View.INVISIBLE);
 				rowView.findViewById(R.id.agent).setVisibility(View.INVISIBLE);
				rowView.findViewById(R.id.btn1).setVisibility(View.VISIBLE);
				 rowView.findViewById(R.id.btn2).setVisibility(View.VISIBLE);
 				rowView.findViewById(R.id.btn3).setVisibility(View.VISIBLE);
 				rowView.findViewById(R.id.list_image).setVisibility(
 						View.INVISIBLE);
 
 				rowView.findViewById(R.id.details)
 						.setVisibility(View.INVISIBLE);
 				rowView.findViewById(R.id.thumbnail).setVisibility(
 						View.INVISIBLE);
 
 			} else {
 
 				rowView.setBackgroundColor(Color.parseColor("#e0f2f9"));
 				rowView.findViewById(R.id.title).setVisibility(View.VISIBLE);
 				rowView.findViewById(R.id.description).setVisibility(
 						View.VISIBLE);
 				rowView.findViewById(R.id.agent).setVisibility(View.VISIBLE);
				rowView.findViewById(R.id.btn1).setVisibility(View.INVISIBLE);
			 rowView.findViewById(R.id.btn2).setVisibility(View.INVISIBLE);
 				rowView.findViewById(R.id.btn3).setVisibility(View.INVISIBLE);
 				rowView.findViewById(R.id.list_image).setVisibility(
 						View.VISIBLE);
 				rowView.findViewById(R.id.details).setVisibility(View.VISIBLE);
 				rowView.findViewById(R.id.thumbnail)
 						.setVisibility(View.VISIBLE);
 
 			}
 		}
 
 		@Override
 		public void onAnimationRepeat(Animation animation) {
 
 		}
 
 		@Override
 		public void onAnimationStart(Animation animation) {
 			/*
 			 * View rowView = listView.getChildAt(position); if (directionRight)
 			 * {
 			 * 
 			 * } else {
 			 * rowView.findViewById(R.id.title).setVisibility(View.VISIBLE);
 			 * rowView.findViewById(R.id.description).setVisibility(
 			 * View.VISIBLE);
 			 * rowView.findViewById(R.id.agent).setVisibility(View.VISIBLE);
 			 * rowView.findViewById(R.id.btn1).setVisibility(View.INVISIBLE);
 			 * rowView.findViewById(R.id.btn2).setVisibility(View.INVISIBLE);
 			 * rowView.findViewById(R.id.btn3).setVisibility(View.INVISIBLE); }
 			 */
 		}
 	}
 
 	// ---------------------------------- Menu Callbacks
 	// -------------------------------//
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater menuInflater = getMenuInflater();
 		menuInflater.inflate(R.layout.more_refresh_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.menu_refresh:
 			getJobsResponse();
 			return true;
 		case R.id.menu_more:
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onBackPressed() {
 		showDialog(10);
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case 10:
 			// Create out AlterDialog
 			Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Do you want to log out?");
 			builder.setCancelable(true);
 			builder.setPositiveButton("Yes", new OkOnClickListener());
 			builder.setNegativeButton("No", new CancelOnClickListener());
 			AlertDialog dialog = builder.create();
 			dialog.show();
 		}
 		return super.onCreateDialog(id);
 	}
 
 	private final class CancelOnClickListener implements
 			DialogInterface.OnClickListener {
 		public void onClick(DialogInterface dialog, int which) {
 
 		}
 	}
 
 	private final class OkOnClickListener implements
 			DialogInterface.OnClickListener {
 		public void onClick(DialogInterface dialog, int which) {
 			signOutRequest();
 			JobsActivity.this.finish();
 			Toast.makeText(getApplicationContext(), "Log out",
 					Toast.LENGTH_LONG).show();
 		}
 	}
 }
