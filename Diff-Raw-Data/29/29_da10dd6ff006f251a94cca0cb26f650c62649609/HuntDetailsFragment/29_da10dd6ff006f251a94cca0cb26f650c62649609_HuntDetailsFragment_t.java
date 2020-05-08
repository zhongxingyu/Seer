 package com.chaseit.fragments;
 
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.List;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 
 import com.chaseit.ParseHelper;
 import com.chaseit.R;
 import com.chaseit.fragments.interfaces.HuntStartInterface;
 import com.chaseit.models.CIUser;
 import com.chaseit.models.Hunt;
 import com.chaseit.models.UserHunt;
 import com.chaseit.models.UserHunt.HuntStatus;
 import com.chaseit.models.wrappers.HuntWrapper;
 import com.chaseit.models.wrappers.ParseObjectWrapper;
 import com.chaseit.models.wrappers.UserHuntWrapper;
 import com.chaseit.util.Constants;
 import com.chaseit.util.Helper;
 import com.google.android.gms.maps.model.LatLng;
 import com.parse.FindCallback;
 import com.parse.GetCallback;
 import com.parse.ParseException;
 import com.parse.SaveCallback;
 
 public class HuntDetailsFragment extends Fragment {
 
 	private static final String tag = "Debug - com.chaseit.fragments.HuntDetailsFragment";
 	private HuntWrapper hWrapper;
 	private TextView tvHuntDetailsTitle;
 	private TextView tvHuntDetailsCreatorHandle;
 	private RatingBar rbHuntDetailsRating;
 	private TextView tvHuntDetailsLocationName;
 	private TextView tvHuntDetailsDescription;
 	private ImageView ivHuntsDetailsMap;
 	private Button btnHuntDetailsLaunch;
 	private List<UserHunt> huntsInProgress;
 
 	private boolean isHuntInProgress = false;
 	protected UserHuntWrapper uHuntWrapper;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_huntdetails, container,
 				false);
 		return view;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		Bundle extras = getArguments();
 		hWrapper = (HuntWrapper) extras
 				.getSerializable(Constants.HUNT_WRAPPER_DATA_NAME);
 
 		setupViews(getView(), hWrapper);
 
 		ParseHelper.getHuntByObjectId(hWrapper.getObjectId(),
 				new GetCallback<Hunt>() {
 
 					@Override
 					public void done(final Hunt hunt, ParseException e) {
 
 						// ParseHelper.getHuntInProgressGivenHuntAndUser(hunt,
 						// CIUser.getCurrentUser(),
 						// new FindCallback<UserHunt>() {
 						//
 						// @Override
 						// public void done(List<UserHunt> hunts,
 						// ParseException e) {
 						// huntsInProgress = hunts;
 						// if (e == null) {
 						// if (hunts != null
 						// && hunts.size() > 0) {
 						// Log.d(tag,
 						// "This hunt is already in progress !");
 						// isHuntInProgress = true;
 						// btnHuntDetailsLaunch
 						// .setText("Continue");
 						// }
 						// } else
 						// Log.d(tag, e.getMessage());
 						// }
 						// });
 						//
						Log.d(tag, "");
 
 						ParseHelper
 								.getMyHuntsInProgress(new FindCallback<UserHunt>() {
 
 									@Override
 									public void done(
 											List<UserHunt> myHuntsInProgress,
 											ParseException e) {
 										huntsInProgress = myHuntsInProgress;
 										if (e == null) {
 
 											for (UserHunt uHunt : myHuntsInProgress) {
 												// if (uHunt
 												// .getHunt()
 												// .getObjectId()
 												// .equals(hunt
 												// .getObjectId())) {
 												// Log.d(tag,
 												// "This hunt is already in progress !");
 												// isHuntInProgress = true;
 												// btnHuntDetailsLaunch
 												// .setText("Continue");
 												// }
 											}
 
 											// if (huntsInProgress != null
 											// && huntsInProgress.size() > 0) {
 											// Log.d(tag,
 											// "This hunt is already in progress !");
 											// isHuntInProgress = true;
 											// btnHuntDetailsLaunch
 											// .setText("Continue");
 											// }
 										} else
 											Log.d(tag, e.getMessage());
 
 									}
 								});
 
 					}
 				});
 
 	}
 
 	private void setupViews(View view, HuntWrapper hWrapper) {
 
 		tvHuntDetailsTitle = (TextView) view
 				.findViewById(R.id.tvHuntDetailsTitle);
 		tvHuntDetailsCreatorHandle = (TextView) view
 				.findViewById(R.id.tvHuntDetailsCreatorHandle);
 		rbHuntDetailsRating = (RatingBar) view
 				.findViewById(R.id.rbHuntDetailsRating);
 		tvHuntDetailsLocationName = (TextView) view
 				.findViewById(R.id.tvHuntDetailsLocationName);
 		tvHuntDetailsDescription = (TextView) view
 				.findViewById(R.id.tvHuntDetailsDescription);
 		ivHuntsDetailsMap = (ImageView) view
 				.findViewById(R.id.ivHuntsDetailsMap);
 		btnHuntDetailsLaunch = (Button) view
 				.findViewById(R.id.btnHuntDetailsLaunch);
 
 		if (Helper.isNotEmpty(hWrapper.getName()))
 			tvHuntDetailsTitle.setText(hWrapper.getName());
 
 		// rbHuntDetailsRating.setRating(Math.round(.getAvgRating()));
 
 		if (Helper.isNotEmpty(hWrapper.getDetails()))
 			tvHuntDetailsDescription.setText(hWrapper.getDetails());
 
 		LatLng startLocation = hWrapper.getStartLocation();
 		if (startLocation != null) {
 			getMap(startLocation, view);
 		}
 
 		btnHuntDetailsLaunch.setOnClickListener(getHuntStartClickListener());
 	}
 
 	private OnClickListener getHuntStartClickListener() {
 		return new OnClickListener() {
 			private UserHunt userHunt;
 
 			@Override
 			public void onClick(View v) {
 				Log.d(tag, "start or contiue clicked");
 
 				/*
 				 * hunt started, create a new user hunt and push to parse
 				 * 
 				 * first get the hunt from parse
 				 */
 
 				if (!isHuntInProgress) {
 					userHunt = new UserHunt();
 					ParseHelper.getHuntByObjectId(hWrapper.getObjectId(),
 							new GetCallback<Hunt>() {
 
 								@Override
 								public void done(Hunt object, ParseException e) {
 									userHunt.setHuntObjectId(object
 											.getObjectId());
 									userHunt.setUserObjectId(CIUser
 											.getCurrentUser().getObjectId());
 									userHunt.setHuntStatus(HuntStatus.IN_PROGRESS);
 									userHunt.setLastLocationLat(object
 											.getStartLocation().getLatitude());
 									userHunt.setLastLocationLong(object
 											.getStartLocation().getLongitude());
 									userHunt.setLocationIndex(0);
 									userHunt.saveInBackground(new SaveCallback() {
 
 										@Override
 										public void done(ParseException e) {
 											if (e != null)
 												Log.d(tag, e.getMessage());
 
 											Log.d(tag, "leaving done");
 										}
 									});
 
 									startHuntOrContinue(userHunt);
 								}
 
 							});
 				} else {
 					// get the user hunt
 
 					if (huntsInProgress != null && huntsInProgress.size() > 0)
 						userHunt = huntsInProgress.get(0);
 					startHuntOrContinue(userHunt);
 				}
 			}
 		};
 	}
 
 	private void startHuntOrContinue(UserHunt userHunt) {
 		uHuntWrapper = new UserHuntWrapper(new ParseObjectWrapper(userHunt));
 
 		FragmentActivity activity = getActivity();
 		if (activity instanceof HuntStartInterface)
 			((HuntStartInterface) activity).startHunt(uHuntWrapper);
 	}
 
 	private void getMap(LatLng point, View view) {
 		StringBuilder stringBuilder = new StringBuilder();
 		stringBuilder
 				.append("http://maps.google.com/maps/api/staticmap?markers=");
 		stringBuilder.append(point.latitude);
 		stringBuilder.append(",");
 		stringBuilder.append(point.longitude);
 		stringBuilder.append("&zoom=16&size=520x520&sensor=false");
 		String[] stringURL = new String[1];
 		stringURL[0] = stringBuilder.toString();
 		GetGoogleImage task = new GetGoogleImage(view);
 		task.execute(stringURL);
 	}
 }
 
 class GetGoogleImage extends AsyncTask<String, Void, Bitmap> {
 
 	private View view;
 	private static final String tag = "Debug com.chaseit.fragments.GetGoogleImage";
 
 	public GetGoogleImage(View view) {
 		super();
 		this.view = view;
 	}
 
 	@Override
 	protected Bitmap doInBackground(String... stringURL) {
 		Bitmap bmp = null;
 		try {
 			URL url = new URL(stringURL[0]);
 			Log.d(tag, "url: " + url);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setDoInput(true);
 			conn.connect();
 			InputStream is = conn.getInputStream();
 			BitmapFactory.Options options = new BitmapFactory.Options();
 
 			bmp = BitmapFactory.decodeStream(is, null, options);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return bmp;
 	}
 
 	@Override
 	protected void onPostExecute(Bitmap result) {
 		ImageView iHuntsDetailsMap = (ImageView) view
 				.findViewById(R.id.ivHuntsDetailsMap);
 		iHuntsDetailsMap.setImageBitmap(result);
 		super.onPostExecute(result);
 	}
 
 }
