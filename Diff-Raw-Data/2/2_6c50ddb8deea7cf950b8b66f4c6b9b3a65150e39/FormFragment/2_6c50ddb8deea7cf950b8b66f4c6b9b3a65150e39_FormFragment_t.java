 package com.nurun.activemtl.ui.fragment;
 
 import java.io.File;
 import java.lang.ref.WeakReference;
 import java.net.URL;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.location.Location;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.MapView;
 import com.google.android.gms.maps.model.BitmapDescriptor;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.nurun.activemtl.ActiveMtlApplication;
 import com.nurun.activemtl.PreferenceHelper;
 import com.nurun.activemtl.R;
 import com.nurun.activemtl.model.EventType;
 import com.nurun.activemtl.service.UploaderService;
 import com.nurun.activemtl.ui.view.StreamDrawable;
 import com.nurun.activemtl.util.BitmapUtil;
 import com.nurun.activemtl.util.NavigationUtil;
 
 public class FormFragment extends Fragment {
 
     private static final String EXTRA_EVENT_TYPE = "EXTRA_EVENT_TYPE";
 
     public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
 
     protected Uri fileUri;
 
     private LocationClient locationClient;
     private boolean pictureTaken = false;
 
     private EditText editTextTitle;
     private EditText editTextDescription;
 
     private ImageView imageView;
 
     private TextView textViewUserName;
 
     private ImageView imageViewUserProfile;
 
     private MapView mapView;
 
     private Location lastLocation;
 
     private Spinner spinnerCategory;
 
     public static Fragment newFragment(EventType eventType) {
         FormFragment fragment = new FormFragment();
         Bundle bundle = new Bundle();
         bundle.putSerializable(EXTRA_EVENT_TYPE, eventType);
         fragment.setArguments(bundle);
         return fragment;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.form_fragment, container, false);
         setHasOptionsMenu(true);
         editTextTitle = (EditText) view.findViewById(R.id.editTextTitle);
         editTextTitle.addTextChangedListener(watcher);
         editTextDescription = (EditText) view.findViewById(R.id.editTextDescription);
         imageView = (ImageView) view.findViewById(R.id.imageView1);
         imageView.setOnClickListener(onClickListener);
         textViewUserName = (TextView) view.findViewById(R.id.textViewUserName);
         textViewUserName.setText(PreferenceHelper.getUserName(getActivity()));
         imageViewUserProfile = (ImageView) view.findViewById(R.id.imageViewUserProfile);
         mapView = (MapView) view.findViewById(R.id.mapView);
         mapView.onCreate(savedInstanceState);
         spinnerCategory = (Spinner) view.findViewById(R.id.spinnerCategory);
         spinnerCategory.setAdapter(getListAdapter());
         getActivity().setTitle(getTitle());
         return view;
     }
 
     private int getTitle() {
         switch (getEventType()) {
         case Alert:
             return R.string.submit_alert;
         case Challenge:
             return R.string.submit_challenge;
         case Idea:
             return R.string.submit_idea;
         default:
             throw new IllegalStateException("Wrong event type : " + getEventType());
         }
     }
 
     @Override
     public void onResume() {
         super.onResume();
         mapView.onResume();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mapView.onPause();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         mapView.onDestroy();
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         mapView.onSaveInstanceState(outState);
     }
 
     @Override
     public void onLowMemory() {
         super.onLowMemory();
         mapView.onLowMemory();
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         locationClient = (LocationClient) getActivity().getApplicationContext().getSystemService(ActiveMtlApplication.LOCATION_CLIENT);
         if (locationClient.isConnected()) {
             lastLocation = locationClient.getLastLocation();
             if (lastLocation != null) {
                 double latitude = lastLocation.getLatitude();
                 double longitude = lastLocation.getLongitude();
                 MarkerOptions marker = new MarkerOptions();
                 marker.position(new LatLng(latitude, longitude));
                 marker.anchor(0.5f, 0.5f);
                 marker.draggable(false);
                 marker.icon(getIcon(getEventType()));
                 mapView.getMap().addMarker(marker);
                 mapView.getMap().getUiSettings().setMyLocationButtonEnabled(false);
                 mapView.getMap().getUiSettings().setZoomControlsEnabled(false);
                 mapView.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
             }
         }
         new ProfilePictureAsyncTask().execute();
     }
 
     private EventType getEventType() {
         return (EventType) getArguments().getSerializable(EXTRA_EVENT_TYPE);
     }
 
     private BitmapDescriptor getIcon(EventType eventType) {
         switch (eventType) {
         case Alert:
             return BitmapDescriptorFactory.fromResource(R.drawable.ic_issue);
         case Challenge:
             return BitmapDescriptorFactory.fromResource(R.drawable.ic_challenge);
         case Idea:
             return BitmapDescriptorFactory.fromResource(R.drawable.ic_idea);
         }
         throw new IllegalStateException("Mauvais Event type : " + eventType);
     }
 
     private boolean isFormCompleted(Location lastLocation) {
         if (lastLocation == null) {
             Toast.makeText(getActivity(), R.string.cannot_get_your_location, Toast.LENGTH_LONG).show();
             return false;
         }
         if (editTextTitle.getText().length() < 4) {
             Toast.makeText(getActivity(), R.string.please_enter_a_longer_title, Toast.LENGTH_LONG).show();
             return false;
         }
         if (!pictureTaken) {
             Toast.makeText(getActivity(), R.string.please_take_a_picture, Toast.LENGTH_LONG).show();
             return false;
         }
         if (editTextDescription.getText().length() < 4) {
             Toast.makeText(getActivity(), R.string.please_enter_a_longer_description, Toast.LENGTH_LONG).show();
             return false;
         }
         if (spinnerCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(getActivity(), R.string.please_select_a_category, Toast.LENGTH_LONG).show();
             return false;
         }
         return true;
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == FormFragment.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
             if (resultCode == FragmentActivity.RESULT_OK) {
                 new BitmapWorkerTask(imageView).execute(fileUri);
             }
         }
         super.onActivityResult(requestCode, resultCode, data);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.suggestion, menu);
         super.onCreateOptionsMenu(menu, inflater);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.action_send_suggestion:
             if (isFormCompleted(lastLocation)) {
                 getActivity().startService(
                         UploaderService.newIntent(getActivity(), fileUri.getPath(), editTextTitle.getText().toString(), editTextDescription.getText()
                                 .toString(), lastLocation, getEventType(), (String)spinnerCategory.getSelectedItem()));
                 NavigationUtil.goToHome(getActivity());
             }
             break;
         default:
             break;
         }
         return super.onOptionsItemSelected(item);
     }
 
     private OnClickListener onClickListener = new OnClickListener() {
 
         @Override
         public void onClick(View v) {
             switch (v.getId()) {
             case R.id.imageView1:
                 Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                 File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FindAPlayground");
                 fileUri = Uri.fromFile(f);
                 intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                 startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                 break;
             default:
                 break;
             }
         }
     };
 
     class BitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap> {
         private final WeakReference<ImageView> imageViewReference;
 
         public BitmapWorkerTask(ImageView imageView) {
             // Use a WeakReference to ensure the ImageView can be garbage
             // collected
             imageViewReference = new WeakReference<ImageView>(imageView);
         }
 
         // Decode image in background.
         @Override
         protected Bitmap doInBackground(Uri... params) {
             fileUri = params[0];
             return BitmapUtil.getResizedBitmap(fileUri);
         }
 
         // Once complete, see if ImageView is still around and set bitmap.
         @Override
         protected void onPostExecute(Bitmap bitmap) {
             if (imageViewReference != null && bitmap != null) {
                 final ImageView imageView = imageViewReference.get();
                 if (imageView != null) {
                     imageView.setImageBitmap(bitmap);
                     pictureTaken = true;
                 }
             }
         }
     }
 
     private TextWatcher watcher = new TextWatcher() {
 
         @Override
         public void afterTextChanged(Editable editable) {
         }
 
         @Override
         public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
         }
 
         @Override
         public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
         }
 
     };
 
     private class ProfilePictureAsyncTask extends AsyncTask<Void, Void, StreamDrawable> {
 
         @Override
         protected StreamDrawable doInBackground(Void... params) {
             try {
                 URL profPict = new URL(PreferenceHelper.getSmallProfilePictureUrl(getActivity()));
                 Bitmap mBitmap = BitmapFactory.decodeStream(profPict.openStream());
                 StreamDrawable streamDrawable = new StreamDrawable(mBitmap);
                 streamDrawable.setSmallSize();
                 return streamDrawable;
             } catch (Exception e) {
                 Log.e(getClass().getSimpleName(), "Url = " + PreferenceHelper.getSmallProfilePictureUrl(getActivity()));
                 Log.e(getClass().getSimpleName(), e.getMessage(), e);
                 Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ali_g);
                 StreamDrawable streamDrawable = new StreamDrawable(mBitmap);
                 streamDrawable.setSmallSize();
                 return streamDrawable;
             }
         }
 
         @Override
         protected void onPostExecute(StreamDrawable result) {
             super.onPostExecute(result);
             imageViewUserProfile.setImageDrawable(result);
         }
     }
 
     private SpinnerAdapter getListAdapter() {
         ArrayAdapter<CharSequence> aa = null;
 
         switch (getEventType()) {
         case Alert:
             aa = ArrayAdapter.createFromResource(getActivity(), R.array.alerts_categories, android.R.layout.simple_spinner_dropdown_item);
             break;
         case Idea:
             aa = ArrayAdapter.createFromResource(getActivity(), R.array.ideas_categories, android.R.layout.simple_spinner_dropdown_item);
             break;
         default:
             throw new IllegalStateException("Event type not implemented : " + getEventType());
         }
 
         return aa;
     }
 }
