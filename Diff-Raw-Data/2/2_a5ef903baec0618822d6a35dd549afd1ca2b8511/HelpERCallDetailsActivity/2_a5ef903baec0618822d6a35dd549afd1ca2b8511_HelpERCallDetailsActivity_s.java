 package com.indago.helpme.gui.dashboard;
 
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.LayerDrawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.Html;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.android.helpme.demo.interfaces.DrawManagerInterface;
 import com.android.helpme.demo.interfaces.UserInterface;
 import com.android.helpme.demo.manager.HistoryManager;
 import com.android.helpme.demo.manager.MessageOrchestrator;
 import com.android.helpme.demo.manager.UserManager;
 import com.android.helpme.demo.overlay.MapItemnizedOverlay;
 import com.android.helpme.demo.overlay.MapOverlayItem;
 import com.android.helpme.demo.utils.Task;
 import com.android.helpme.demo.utils.User;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.indago.helpme.R;
 import com.indago.helpme.gui.util.ImageUtility;
 
 public class HelpERCallDetailsActivity extends MapActivity implements DrawManagerInterface {
 	private static final String LOGTAG = HelpERCallDetailsActivity.class.getSimpleName();
 	protected static DisplayMetrics metrics = new DisplayMetrics();
 	private Handler mHandler;
 
 	private List<Overlay> mapOverlays;
 	private MapItemnizedOverlay overlay;
 	private MapController mapController;
 	private HashMap<String, MapOverlayItem> hashMapOverlayItem;
 	private Drawable mMapsPinGreen;
 	private Drawable mMapsPinOrange;
 	private boolean show = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
 
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_help_er_call_details);
 
 		Bundle extras = getIntent().getExtras();
 		String userID = extras.getString("USER_ID");
 		UserInterface mUser = UserManager.getInstance().getUserById(userID);
 
 		if(mUser == null) {
 			throw new NullPointerException(LOGTAG + ": User with ID " + userID + " could not be retrieved from Extras-Bundle at onCreate()");
 		}
 
 		TextView name = (TextView) findViewById(R.id.tv_help_ee_name);
 		name.setText(Html.fromHtml(name.getText() + " " + mUser.getName()));
 		TextView age = (TextView) findViewById(R.id.tv_help_ee_age);
 		age.setText(Html.fromHtml(age.getText() + " " + mUser.getAge()));
 		TextView gender = (TextView) findViewById(R.id.tv_help_ee_gender);
 		gender.setText(Html.fromHtml(gender.getText() + " " + mUser.getGender()));
 
 		ImageView picture = (ImageView) findViewById(R.id.iv_help_ee_picture);
 		picture.setImageDrawable(new LayerDrawable(ImageUtility.retrieveDrawables(getApplicationContext(), mUser.getPicture())));
 		picture.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				setZoomLevel();
 			}
 		});
 
 		mHandler = new Handler();
 		initMaps(mUser);
 		MessageOrchestrator.getInstance().addDrawManager(DRAWMANAGER_TYPE.MAP, this);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		setDefaulAppearance();
 	}
 
 	protected void setDefaulAppearance() {
 		getWindow().getDecorView().getRootView()
 		.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
 	}
 
 	private void initMaps(UserInterface userInterface) {
 		MapView mapView = (MapView) findViewById(R.id.mapview);
 		mapView.setBuiltInZoomControls(true);
 
 		mapOverlays = mapView.getOverlays();
 		hashMapOverlayItem = new HashMap<String, MapOverlayItem>();
 
 		mMapsPinOrange = this.getResources().getDrawable(R.drawable.maps_pin_orange);
 		int w = mMapsPinOrange.getIntrinsicWidth();
 		int h = mMapsPinOrange.getIntrinsicHeight();
 		mMapsPinOrange.setBounds(-w / 2, -h, w / 2, 0);
 
 		mMapsPinGreen = this.getResources().getDrawable(R.drawable.maps_pin_green);
 		mMapsPinGreen.setBounds(0, 0, mMapsPinGreen.getIntrinsicWidth(), mMapsPinGreen.getIntrinsicHeight());
 
 		overlay = new MapItemnizedOverlay(mMapsPinGreen, this);
 
 		mapController = mapView.getController();
 		mapOverlays.add(overlay);
 
 		mHandler.postDelayed(addMarker(userInterface), 200);
 		if(UserManager.getInstance().thisUser().getGeoPoint() != null) {
 			mHandler.postDelayed(addMarker(UserManager.getInstance().getThisUser()), 200);
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (show) {
 			return;
 		}
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
 		// set title
 		alertDialogBuilder.setTitle("Warning!\nCanceling Support Offer!");
 
 		// set dialog message
 		alertDialogBuilder.setMessage("Do you really want to cancel your current support offer?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				MessageOrchestrator.getInstance().removeDrawManager(DRAWMANAGER_TYPE.MAP);
 				HistoryManager.getInstance().getTask().setFailed();
 				HistoryManager.getInstance().stopTask();
 
 				startActivity(new Intent(getApplicationContext(), com.indago.helpme.gui.dashboard.HelpERControlcenterActivity.class));
 
 				finish();
 			}
 		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				// if this button is clicked, just close
 				// the dialog box and do nothing
 				dialog.cancel();
 			}
 		});
 
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show it
 
 		alertDialog.show();
 
 		//startActivity(new Intent(getApplicationContext(), com.indago.helpme.gui.HelpERControlcenterActivity.class), null);
 
 		//super.onBackPressed();
 
 		//finish();
 	}
 
 	private Runnable addMarker(final UserInterface userInterface) {
 		return new Runnable() {
 
 			@Override
 			public void run() {
 				MapOverlayItem overlayitem = hashMapOverlayItem.get(userInterface.getId());
 				boolean zoom = false;
 				if(overlayitem != null) {
 					overlay.removeItem(overlayitem);
 				}
 				if (hashMapOverlayItem.size() <= 1 && overlayitem == null)
 					zoom = true;
 
 				if(userInterface.getId().equalsIgnoreCase(UserManager.getInstance().thisUser().getId())) {
 					overlayitem = new MapOverlayItem(userInterface.getGeoPoint(), userInterface.getId(), null, userInterface.getJsonObject() , ImageUtility.retrieveDrawables(getApplicationContext(), userInterface.getPicture()));
 					overlayitem.setMarker(mMapsPinGreen);
 
 				} else {// a help seeker
 					overlayitem = new MapOverlayItem(userInterface.getGeoPoint(), userInterface.getId(), null,userInterface.getJsonObject(), ImageUtility.retrieveDrawables(getApplicationContext(), userInterface.getPicture()));
 					overlayitem.setMarker(mMapsPinOrange);
 
 				}
 
 
 
 
 				hashMapOverlayItem.put(userInterface.getId(), overlayitem);
 				overlay.addOverlay(overlayitem);
 
 				if (zoom) {
 					setZoomLevel();
 				}
 				//				setZoomLevel();
 
 			}
 		};
 	}
 
 	private Runnable showInRangeMessageBox(final Context context, final Task task) {
 		show = true;
 		return new Runnable() {
 
 			@Override
 			public void run() {
 				MessageOrchestrator.getInstance().removeDrawManager(DRAWMANAGER_TYPE.MAP);
 				HistoryManager.getInstance().stopTask();
 				mHandler.post(HistoryManager.getInstance().saveHistory(getApplicationContext()));
 
 				Dialog dialog = buildDialog(context, task);
 				try {
 					dialog.show();
 				} catch(Exception exception) {
 					Log.e(LOGTAG, exception.toString());
 				}
 			}
 		};
 	}
 
 	@Override
 	public void drawThis(Object object) {
 		if(object instanceof User) {
 			User user = (User) object;
 			mHandler.post(addMarker(user));
 		} else if(object instanceof Task) {
 			Task task = (Task) object;
 			if(!show) {
 				mHandler.post(showInRangeMessageBox(this, task));
 			}
 		}
 
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/*
 	 * 
 	 */
 	private void setZoomLevel() {
 		Object[] keys = hashMapOverlayItem.keySet().toArray();
 		OverlayItem item;
 		if(keys.length > 1) {
 			int minLatitude = Integer.MAX_VALUE;
 			int maxLatitude = Integer.MIN_VALUE;
 			int minLongitude = Integer.MAX_VALUE;
 			int maxLongitude = Integer.MIN_VALUE;
 
 			for(Object key : keys) {
 				item = hashMapOverlayItem.get((String) key);
 				GeoPoint p = item.getPoint();
 				int lati = p.getLatitudeE6();
 				int lon = p.getLongitudeE6();
 
 				maxLatitude = Math.max(lati, maxLatitude);
 				minLatitude = Math.min(lati, minLatitude);
 				maxLongitude = Math.max(lon, maxLongitude);
 				minLongitude = Math.min(lon, minLongitude);
 			}
 			mapController.zoomToSpan(Math.abs(maxLatitude - minLatitude),
 					Math.abs(maxLongitude - minLongitude));
 			mapController.animateTo(new GeoPoint((maxLatitude + minLatitude) / 2,
 					(maxLongitude + minLongitude) / 2));
 
 		} else {
 			String key = (String) keys[0];
 			item = hashMapOverlayItem.get(key);
 			mapController.animateTo(item.getPoint());
 			while(mapController.zoomIn()) {
 
 			}
 			mapController.zoomOut();
 		}
 	}
 
 	private Dialog buildDialog(Context context, Task task) {
 		UserInterface userInterface = task.getUser();
 		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
 
 		Dialog dialog = dialogBuilder.show();
 		dialog.setContentView(R.layout.dialog_help_er_in_range);
//		dialog.setCanceledOnTouchOutside(false);
 
 		ImageView imageView; TextView text; String string;Button button;
 		
 		Drawable[] drawables = new Drawable[4];
 		
 		imageView = (ImageView) dialog.findViewById(R.id.dialog_helper_in_range_picture);
 		imageView.setImageDrawable(new LayerDrawable(ImageUtility.retrieveDrawables(context, userInterface.getPicture())));
 
 		text = (TextView) dialog.findViewById(R.id.dialog_helper_in_range_title);
 		string = context.getString(R.string.helper_in_range_title);
 		string = string.replace("[Name]", userInterface.getName());
 		text.setText(Html.fromHtml(string));
 
 		text = (TextView) dialog.findViewById(R.id.dialog_helper_in_range_text);
 		string = context.getString(R.string.helper_in_range_text);
 		
 		if (userInterface.getGender().equalsIgnoreCase("female")) {
 			string = string.replace("[gender]", "her");
 		}else {
 			string = string.replace("[gender]", "him");
 		}
 		text.setText(Html.fromHtml(string));
 
 
 		button = (Button) dialog.findViewById(R.id.dialog_helper_in_range_button);
 		button.setText("OK");
 		button.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(getApplicationContext(), HelpERControlcenterActivity.class);
 				startActivity(intent);
 				finish();
 
 			}
 		});
 
 		return dialog;
 	}
 }
