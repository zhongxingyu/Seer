 package mdiss.umappin.fragments;
 
 import java.util.Date;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.mapsforge.android.maps.MapView;
 import org.mapsforge.android.maps.mapgenerator.tiledownloader.MapnikTileDownloader;
 import org.mapsforge.android.maps.overlay.ArrayItemizedOverlay;
 import org.mapsforge.android.maps.overlay.OverlayItem;
 import org.mapsforge.core.GeoPoint;
 
 import mdiss.umappin.R;
 import mdiss.umappin.asynctasks.UploadImageAsyncTask;
 import mdiss.umappin.ui.MainActivity;
 import mdiss.umappin.utils.Constants;
 import mdiss.umappin.utils.GeoMethods;
 import mdiss.umappin.utils.ImageUtils;
 import android.app.Dialog;
 import android.app.Fragment;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 
 public class PictureFragment extends Fragment {
 
 	private MapView mapView;
 	private ImageView mImageView;
 	private FrameLayout mFrameLayout;
 	private GestureDetector gestureDetector;
 	private View.OnTouchListener gestureListener;
 	private GeoPoint picturePoint;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		setHasOptionsMenu(true);
 		getActivity().setTitle("Pictures");
 		
 		View view = inflater.inflate(R.layout.locate_picture, container, false);
 		mFrameLayout = (FrameLayout) view.findViewById(R.id.map_container);
 		
 		mapView = new MapView(getActivity(), new MapnikTileDownloader());
 		mapView.getController().setZoom(16);
 		mapView.setBuiltInZoomControls(true);
 		mapView.setClickable(true);
 		mapView.setBuiltInZoomControls(true);
 		picturePoint = GeoMethods.getCurrentLocation(getActivity());
 		mapView.setCenter(picturePoint);
 
 		gestureDetector = new GestureDetector(getActivity(), new MyGestureDetector());
 		gestureListener = new View.OnTouchListener() {
 			public boolean onTouch(View v, MotionEvent event) {
 				return gestureDetector.onTouchEvent(event);
 			}
 		};
 		mapView.setOnTouchListener(gestureListener);
 		mFrameLayout.addView(mapView);
 		return view;
 	}
 
 	/**
 	 * Using global variable picturePoint, draws a OverlayItem in map in that
 	 * location
 	 */
 	private void putMarkerInMap() {
 		mapView.getOverlays().clear();
 		Drawable defaultMarker = getResources().getDrawable(R.drawable.marker);
 		ArrayItemizedOverlay itemizedOverlay = new ArrayItemizedOverlay(defaultMarker);
 		OverlayItem item = new OverlayItem(picturePoint, Constants.picturePointName, Constants.picturePointDesc);
 		itemizedOverlay.addItem(item);
 		mapView.getOverlays().add(itemizedOverlay);
 		mapView.setCenter(picturePoint);
 	}
 
 	class MyGestureDetector extends SimpleOnGestureListener {
 
 		@Override
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			Log.i(Constants.logMap, "On single tap");
 			picturePoint = mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY());
 			putMarkerInMap();
 			return super.onSingleTapConfirmed(e);
 		}
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 		inflater.inflate(R.menu.picture, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		MainActivity main = (MainActivity) getActivity();
 		switch (item.getItemId()) {
 		case R.id.action_new_photo:
 			main.dispatchTakePictureIntent(MainActivity.ACTION_TAKE_PHOTO_B);
 			return true;
 		case R.id.action_upload_photo:
 			final Dialog dialog = new Dialog(getActivity());
 			dialog.setContentView(R.layout.dialog_photo_upload);
 			dialog.setTitle(getString(R.string.photo_upload));
 			Button upload = (Button) dialog.findViewById(R.id.upload);
 			upload.setOnClickListener(new OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					mImageView = (ImageView) getActivity().findViewById(R.id.current_picture);
 					String imageBase64 = ImageUtils.getBase64(mImageView);
 					EditText et = (EditText) dialog.findViewById(R.id.title);
 					String title = et.getText().toString();
 					et = (EditText) dialog.findViewById(R.id.description);
 					String description = et.getText().toString();
 					Date date = new Date();
 					JSONObject json = new JSONObject();
 					try {
 						json.put("content", "data:image/jpeg;base64;" + imageBase64);
 						json.put("title",title);
 						json.put("description",description);
 						json.put("latitude",picturePoint.getLatitude());
 						json.put("longitude",picturePoint.getLongitude());
 						json.put("is_searchable",true);
 						json.put("date_created",date.getTime());
 						new UploadImageAsyncTask(getActivity()).execute(json);
 					} catch (JSONException e) {
 						e.printStackTrace();
 					} finally {
 						dialog.dismiss();
 					}
 				}
 			});
 			dialog.show();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		mapView.getOverlays().clear();
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		putMarkerInMap();
 	}
 }
