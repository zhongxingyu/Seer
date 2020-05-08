 package au.com.dius.resilience.ui.activity;
 
 import android.content.Intent;
 import android.location.Location;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.*;
 import au.com.dius.resilience.R;
 import au.com.dius.resilience.actionbar.ActionBarHandler;
 import au.com.dius.resilience.factory.MediaFileFactory;
 import au.com.dius.resilience.intent.Extras;
 import au.com.dius.resilience.loader.ImageLoader;
 import au.com.dius.resilience.location.LocationBroadcaster;
 import au.com.dius.resilience.location.event.LocationUpdatedEvent;
 import au.com.dius.resilience.model.MediaType;
 import au.com.dius.resilience.model.ServiceListDefaults;
 import au.com.dius.resilience.persistence.repository.impl.ServiceRequestRepository;
 import au.com.dius.resilience.service.CreateIncidentService;
 import au.com.dius.resilience.ui.adapter.ServiceListSpinnerAdapter;
 import au.com.dius.resilience.ui.fragment.LocationResolverFragment;
 import au.com.dius.resilience.util.Logger;
 import au.com.justinb.open311.model.ServiceList;
 import au.com.justinb.open311.model.ServiceRequest;
 import com.google.inject.Inject;
 import com.squareup.otto.Subscribe;
 import org.apache.commons.lang.StringUtils;
 import roboguice.activity.RoboFragmentActivity;
 import roboguice.inject.ContentView;
 import roboguice.inject.InjectFragment;
 import roboguice.inject.InjectView;
 
 import java.io.File;
 
 @ContentView(R.layout.activity_create_service_request)
 public class CreateServiceRequestActivity extends RoboFragmentActivity {
 
   public static final int CAPTURE_PHOTO_REQUEST_CODE = 100;
   public static final String SERVICE_REQUEST_URI = "ServiceRequestUri";
 
   @Inject
   private ActionBarHandler actionBarHandler;
 
   @InjectView(R.id.service_spinner)
   private Spinner serviceSpinner;
 
   @InjectView(R.id.photo_preview_image)
   private ImageView photoPreview;
 
   @InjectView(R.id.description_field)
   private EditText descriptionField;
 
   @InjectFragment(R.id.location_resolver_fragment)
   private LocationResolverFragment locationResolverFragment;
 
   @InjectView(R.id.submit_button)
   private Button submitButton;
 
   @Inject
   private LocationBroadcaster locationBroadcaster;
 
   @Inject
   private MediaFileFactory mediaFileFactory;
 
   @Inject
   private ImageLoader imageLoader;
 
   private ServiceListSpinnerAdapter serviceListSpinnerAdapter;
 
  private Uri cachedPhotoUri;
 
   private Location lastKnownLocation;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setupAdapter();
 
     locationBroadcaster.subscribe(this);
     locationBroadcaster.subscribe(locationResolverFragment);
 
     if (savedInstanceState != null) {
      cachedPhotoUri = Uri.parse(savedInstanceState.getString(SERVICE_REQUEST_URI));
     }
 
     descriptionField.setOnTouchListener(new View.OnTouchListener() {
       @Override
       public boolean onTouch(View v, MotionEvent event) {
         descriptionField.setError(null);
         return false;
       }
     });
   }
 
   @Subscribe
   public void onServiceRequestErrorEvent(ServiceRequestRepository.ErrorEvent errorEvent) {
     setStateEnabled();
   }
 
   @Subscribe
   public void onLocationUpdatedEvent(LocationUpdatedEvent event) {
     lastKnownLocation = event.getLocation();
   }
 
   @Override
   public void onResume() {
     super.onResume();
     if (cachedPhotoUri != null) {
      imageLoader.loadFromUrl(photoPreview, cachedPhotoUri.toString());
     }
     setStateEnabled();
   }
 
   @Override
   public void onSaveInstanceState(Bundle state) {
    state.putString(SERVICE_REQUEST_URI, cachedPhotoUri == null ? null : cachedPhotoUri.toString());
   }
 
   private void setupAdapter() {
     serviceListSpinnerAdapter = new ServiceListSpinnerAdapter(this);
     serviceListSpinnerAdapter.setData(ServiceListDefaults.DEFAULT_SERVICES);
     serviceSpinner.setAdapter(serviceListSpinnerAdapter);
   }
 
   public void onCameraButtonClick(View button) {
     File mediaFile = mediaFileFactory.createMediaFile(MediaType.PHOTO);
 
     if (mediaFile == null) {
       Toast.makeText(this, "Couldn't save photo to storage!", Toast.LENGTH_LONG).show();
       return;
     }
 
     Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 
    cachedPhotoUri = Uri.fromFile(mediaFile);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, cachedPhotoUri);
     startActivityForResult(intent, CAPTURE_PHOTO_REQUEST_CODE);
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     getMenuInflater().inflate(R.menu.action_bar_minimal, menu);
 
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     return actionBarHandler.handleMenuItemSelected(item);
   }
 
   @Override
   protected void onActivityResult(int requestCode, int resultCode,
                                   Intent data) {
   }
 
   public void onSubmitClick(final View view) {
 
     if (StringUtils.isEmpty(descriptionField.getText().toString())) {
       descriptionField.setError("Can't be blank");
       return;
     }
 
     if (cachedPhotoUri == null) {
       Toast.makeText(this, "You need to attach a photo to the issue.", Toast.LENGTH_SHORT).show();
       return;
     }
 
     if (lastKnownLocation == null) {
       Toast.makeText(this, "Can't resolve your location, please check your location services.", Toast.LENGTH_SHORT).show();
       setStateEnabled();
       return;
     }
 
     setStateUploading();
 
     Intent service = new Intent(this, CreateIncidentService.class);
    service.putExtra(Extras.PHOTO_LOCAL_URI, cachedPhotoUri.getPath());
     service.putExtra(Extras.SERVICE_REQUEST_BUILDER, buildServiceRequest());
     startService(service);
 
     finish();
   }
 
   private void setStateUploading() {
     submitButton.setText(R.string.uploading);
     submitButton.setEnabled(false);
     descriptionField.setEnabled(false);
     photoPreview.setEnabled(false);
     serviceSpinner.setEnabled(false);
 
   }
 
   private void setStateEnabled() {
     submitButton.setText(R.string.submit);
     serviceSpinner.setEnabled(true);
     submitButton.setEnabled(true);
     descriptionField.setEnabled(true);
     photoPreview.setEnabled(true);
   }
 
   private ServiceRequest.Builder buildServiceRequest() {
     final ServiceRequest.Builder builder = new ServiceRequest.Builder();
 
     ServiceList serviceList = (ServiceList) serviceSpinner.getSelectedItem();
     builder.serviceCode(serviceList.getServiceCode())
       .serviceName(serviceList.getServiceName())
       .description(descriptionField.getText().toString())
       .latitude(lastKnownLocation.getLatitude())
       .longtitude(lastKnownLocation.getLongitude());
 
     return builder;
   }
 }
