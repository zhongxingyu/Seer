 package com.lake.tahoe.activities;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 
 import com.facebook.widget.ProfilePictureView;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.maps.android.ui.IconGenerator;
 import com.lake.tahoe.R;
 import com.lake.tahoe.callbacks.ModelCallback;
 import com.lake.tahoe.models.Request;
 import com.lake.tahoe.models.User;
 import com.lake.tahoe.utils.ErrorUtil;
 import com.lake.tahoe.utils.HandlesErrors;
 import com.lake.tahoe.utils.MapUtil;
 import com.lake.tahoe.views.DynamicActionBar;
 import com.lake.tahoe.widgets.SpeechBubble;
 import com.parse.ParseException;
 import com.parse.SaveCallback;
 
 /**
  * Created by steffan on 11/3/13.
  */
 public class RequestDetailActivity extends GoogleLocationServiceActivity implements HandlesErrors {
 	ProfilePictureView profilePictureView;
 	GoogleMap map;
 	Request request;
 	IconGenerator iconGenerator = new IconGenerator(this);
 	DynamicActionBar actionBar;
 
 	public static String REQUEST_ID = "requestId";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_request_detail);
 
 		User user = User.getCurrentUser();
 
 		actionBar = new DynamicActionBar(this, getResources().getColor(R.color.black));
 		actionBar.setXMarkVisibility(View.VISIBLE, new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				finish();
 			}
 		});
 
 		actionBar.setCheckMarkVisibility(View.VISIBLE, new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Log.d("debug", "Setting Request to Active");
 				request.setState(Request.State.ACTIVE);
				request.setVendor(User.getCurrentUser());
 				request.saveEventually(new SaveCallback() {
 					@Override
 					public void done(ParseException e) {
 						if (e == null) {
							Intent i = new Intent(
									RequestDetailActivity.this,
									RequestActiveVendorActivity.class
							);
 							i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 							startActivity(i);
 						} else {
 							onError(e);
 						}
 					}
 				});
 			}
 		});
 	}
 
 	public void updateRequestDetail() {
		User client = request.getClient();
		profilePictureView = (ProfilePictureView)findViewById(R.id.pvProfile);
		profilePictureView.setProfileId(client.getFacebookId());

		actionBar.setTitle(client.getName());

 		TextView tvCost = (TextView) findViewById(R.id.tvCost);
 		tvCost.setText(request.getDisplayDollars());
 
 		TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
 		tvTitle.setText(request.getTitle());
 
 		TextView tvDescription = (TextView) findViewById(R.id.tvDescription);
 		tvDescription.setText(request.getDescription());
 
 		MarkerOptions markerOptions = MapUtil.getSpeechBubbleMarkerOptions(
 				request,
 				iconGenerator,
 				SpeechBubble.ColorType.BLUE
 		);
 		map.addMarker(markerOptions);
		MapUtil.panAndZoomToUser(map, client, MapUtil.DEFAULT_ZOOM_LEVEL);
 	}
 
 	@Override
 	protected void onGooglePlayServicesReady() {
 		SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
 		map = fragment.getMap();
 		map.setMyLocationEnabled(true);
 		map.getUiSettings().setZoomControlsEnabled(false);
 		map.getUiSettings().setMyLocationButtonEnabled(false);
 
 		Intent i = getIntent();
 		String requestId = i.getStringExtra(REQUEST_ID);
 		Request.getByObjectId(requestId, new ModelCallback<Request>() {
 
 			@Override
 			public void onModelFound(Request req) {
 				request = req;
 				RequestDetailActivity.this.updateRequestDetail();
 			}
 
 			@Override
 			public void onModelError(Throwable e) {
 				RequestDetailActivity.this.onError(e);
 			}
 		});
 	}
 
 	@Override
 	protected void onGooglePlayServicesError(Throwable t) {
 		onError(t);
 	}
 
 	@Override
 	protected void onLocationTrackingFailed(Throwable t) {
 		onError(t);
 	}
 
 	@Override
 	public void onError(Throwable t) {
 		ErrorUtil.log(this, t);
 	}
 }
