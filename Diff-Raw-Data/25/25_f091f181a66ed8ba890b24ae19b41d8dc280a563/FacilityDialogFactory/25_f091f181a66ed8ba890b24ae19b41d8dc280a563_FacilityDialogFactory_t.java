 package put.medicallocator.ui.dialogs;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.text.method.LinkMovementMethod;
 import android.text.util.Linkify;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import put.medicallocator.R;
 import put.medicallocator.io.model.Facility;
 import put.medicallocator.io.route.DirectionApiRouteProvider;
 import put.medicallocator.io.route.model.RoutePoint;
 import put.medicallocator.ui.location.LastLocationStrategy;
 import put.medicallocator.ui.misc.RouteDownloadAsyncTask;
 import put.medicallocator.ui.misc.RouteOverlayManager;
 import put.medicallocator.utils.MyLog;
 import put.medicallocator.utils.StringUtils;
 
 /**
  * Builds the dialog showing information about {@link Facility} and allowing to call, send e-mail, show route to it.
  */
 public class FacilityDialogFactory implements DialogFactory {
     private static final String TAG = FacilityDialogFactory.class.getName();
 
     private final Context context;
     private final Facility facility;
     private final LayoutInflater inflater;
     private final LocationManager locationManager;
     private final LastLocationStrategy lastLocationStrategy;
     private final RouteOverlayManager routeOverlayManager;
 
     private Dialog dialog;
 
     public FacilityDialogFactory(Context context, Facility facility, RouteOverlayManager routeOverlayManager) {
         this.context = context;
         this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
         this.lastLocationStrategy = new LastLocationStrategy();
         this.facility = facility;
         this.routeOverlayManager = routeOverlayManager;
     }
 
     @Override
     public Dialog createDialog(Context context) {
         // Create, inflate and populate the dialog layout.
         View layout = inflater.inflate(R.layout.dialog_facility_bubble, null);
         initializeViews(layout);
 
        final Drawable facilityDrawable = context.getResources().getDrawable(
                facility.getFacilityType().getDrawableId());

        final TextView textView = (TextView) inflater.inflate(R.layout.dialog_facility_title, null);
        textView.setCompoundDrawablesWithIntrinsicBounds(facilityDrawable, null, null, null);
        textView.setText(context.getString(facility.getFacilityType().getStringId()));
 
        // Create the Dialog itself.
        return this.dialog = new AlertDialog.Builder(context)
                .setView(layout)
                .setCustomTitle(textView)
                .create();
     }
 
     /**
      * Initializes all the {@link View}s and sets appropriate listeners.
      */
     private void initializeViews(View layout) {
         final ViewGroup emailContainer = (ViewGroup) layout.findViewById(R.id.email_container);
         final ViewGroup webpageContainer = (ViewGroup) layout.findViewById(R.id.webpage_container);
 
         // Set the primary information
         final TextView nameTextView = (TextView) layout.findViewById(R.id.name_textview);
         final TextView addressTextView = (TextView) layout.findViewById(R.id.address_textview);
         final TextView phoneTextView = (TextView) layout.findViewById(R.id.phone_textview);
         final TextView emailTextView = (TextView) layout.findViewById(R.id.email_textview);
         final TextView homepageTextView = (TextView) layout.findViewById(R.id.homepage_textview);
 
         nameTextView.setText(facility.getName());
         addressTextView.setText(facility.getAddress());
         phoneTextView.setText(facility.getPhone());
         emailTextView.setText(facility.getEmail());
         homepageTextView.setText(facility.getHomepage());
 
         Linkify.addLinks(phoneTextView, Linkify.PHONE_NUMBERS);
         Linkify.addLinks(emailTextView, Linkify.EMAIL_ADDRESSES);
         Linkify.addLinks(homepageTextView, Linkify.WEB_URLS);
         emailTextView.setMovementMethod(LinkMovementMethod.getInstance());
         homepageTextView.setMovementMethod(LinkMovementMethod.getInstance());
 
         final ImageButton callButton = (ImageButton) layout.findViewById(R.id.call_button);
         final ImageButton emailButton = (ImageButton) layout.findViewById(R.id.email_button);
         final ImageButton webpageButton = (ImageButton) layout.findViewById(R.id.www_button);
         final ImageButton routeButton = (ImageButton) layout.findViewById(R.id.route_button);
 
         final OnClickListener onCallClickListener = new OnClickListener() {
             @Override
             public void onClick(View v) {
                 invokeDialIntent();
             }
         };
         callButton.setOnClickListener(onCallClickListener);
 
 		/* Do the appropriate UI actions if the e-mail exists */
         if (!StringUtils.isEmpty(facility.getEmail())) {
             emailButton.setOnClickListener(new OnClickListener() {
 
                 @Override
                 public void onClick(View v) {
                     invokeSendMailIntent();
                 }
             });
             emailContainer.setVisibility(View.VISIBLE);
         } else {
             emailContainer.setVisibility(View.GONE);
         }
 
 		/* Do the appropriate UI actions if the webpage exists */
         String webpage = facility.getHomepage();
         if (!StringUtils.isEmpty(webpage)) {
             // We have to ensure that proper protocol is specified to make Intent working.
             if (!webpage.startsWith("http")) {
                 // If http was omitted at all, assume that http and not https should be used.
                 webpage = "http://" + webpage;
             }
             final String webpageAddress = webpage;
             webpageButton.setOnClickListener(new OnClickListener() {
 
                 @Override
                 public void onClick(View v) {
                     invokeUrlIntent(webpageAddress);
                 }
             });
             webpageContainer.setVisibility(View.VISIBLE);
         } else {
             webpageContainer.setVisibility(View.GONE);
         }
 
         final Location currentLocation = getBestSuitableLastLocation();
         if (routeOverlayManager != null && currentLocation != null && isConnectionAvailable()) {
             routeButton.setEnabled(true);
             routeButton.setOnClickListener(new OnClickListener() {
 
                 @Override
                 public void onClick(View v) {
                     final RoutePoint startPoint = new RoutePoint();
                     final RoutePoint finishPoint = new RoutePoint();
 
                     startPoint.setLatitude(currentLocation.getLatitude());
                     startPoint.setLongitude(currentLocation.getLongitude());
 
                     finishPoint.setLatitude(facility.getLocation().getLatitudeE6() / 1E6);
                     finishPoint.setLongitude(facility.getLocation().getLongitudeE6() / 1E6);
 
                     final DirectionApiRouteProvider routeProvider = new DirectionApiRouteProvider(startPoint, finishPoint);
                     final RouteDownloadAsyncTask.DownloadListener downloadListener = new RouteDownloadAsyncTask.DownloadListener() {
                         @Override
                         public void finished(boolean success) {
                             dialog.dismiss();
                         }
                     };
                     new RouteDownloadAsyncTask(context, routeProvider, routeOverlayManager, downloadListener).execute();
                 }
             });
         } else {
             routeButton.setEnabled(false);
         }
     }
 
     /**
      * Starts the {@link Intent} to show the provided {@code url}.
      */
     private void invokeUrlIntent(String url) {
         Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
         context.startActivity(webIntent);
     }
 
     /**
      * Starts the {@link Intent} to show dial console allowing to call selected {@link Facility}.
      */
     private void invokeDialIntent() {
         try {
             Intent intent = new Intent(Intent.ACTION_DIAL);
             intent.setData(Uri.parse("tel:" + facility.getPhone()));
             context.startActivity(intent);
         } catch (Exception e) {
             MyLog.e(TAG, "Failed to invoke call", e);
         }
     }
 
     /**
      * Starts the {@link Intent} allowing to send the e-mail to the mailbox associated with this {@link Facility}.
      */
     private void invokeSendMailIntent() {
         final Intent emailIntent = new Intent(Intent.ACTION_SEND);
         final String chooserMessage = context.getResources()
                 .getString(R.string.dialogfacilitybubble_email_app_chooser);
         emailIntent.setType("plain/text");
         emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{facility.getEmail()});
         context.startActivity(Intent.createChooser(emailIntent, chooserMessage));
     }
 
     /**
      * Checks whether there is Internet connection available.
      */
     private boolean isConnectionAvailable() {
         final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         return conMgr.getActiveNetworkInfo() != null
                 && conMgr.getActiveNetworkInfo().isAvailable()
                 && conMgr.getActiveNetworkInfo().isConnected();
     }
 
     /**
      * Finds the best last location of the user, coming either from network or GPS.
      */
     protected Location getBestSuitableLastLocation() {
         final Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         final Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 
         if (networkLocation != null) {
             return lastLocationStrategy.isBetterLocation(networkLocation, gpsLocation) ? networkLocation : gpsLocation;
         }
         return null;
     }
 
 }
