 package com.strollimo.android.view;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.location.Location;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.*;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.TextView;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapView;
 import com.google.android.gms.maps.MapsInitializer;
 import com.google.android.gms.maps.model.*;
 import com.strollimo.android.AppGlobals;
 import com.strollimo.android.R;
 import com.strollimo.android.StrollimoApplication;
 import com.strollimo.android.StrollimoPreferences;
 import com.strollimo.android.controller.AccomplishableController;
 import com.strollimo.android.controller.UserService;
 import com.strollimo.android.model.MapPlacesModel;
 import com.strollimo.android.model.Mystery;
 import com.strollimo.android.network.AmazonS3Controller;
 import com.strollimo.android.util.Analytics;
 
 public class MapFragment extends Fragment {
     public static final int DEFAULT_RADIUS = 25;
    private static final String TAG = MapFragment.class.getSimpleName();
     private View mView;
     private GoogleMap mMap;
     private LocationClient mLocationClient;
     private AccomplishableController mAccomplishableController;
     private StrollimoPreferences mPrefs;
     private UserService mUserService;
     private boolean firstStart = true;
     private ProgressNetworkImageView mPlaceImage;
     private View mPlaceImgProgress;
     private TextView mPlaceTitle;
     private View mRibbonPanel;
     private Circle mCircleRadius;
     private MapPlacesModel mMapPlacesModel;
     private MapView mMapView;
     private GoogleMap.OnInfoWindowClickListener onInfoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
         @Override
         public void onInfoWindowClick(Marker marker) {
             launchDetailsActivity();
         }
     };
     private GoogleMap.OnMarkerClickListener mOnMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
         @Override
         public boolean onMarkerClick(Marker marker) {
 
             //reset previous selection if any
             Marker selectedMarker = mMapPlacesModel.getSelectedMarker();
             if (selectedMarker != null) {
                 resetMarkerIcon(selectedMarker);
             }
 
             mMapPlacesModel.onMarkerClick(marker);
 
             // TODO: replace this with assets
             if (mAccomplishableController.isMysteryFinished(mMapPlacesModel.getSelectedPlace().getId())) {
                 marker.setIcon(BitmapDescriptorFactory.fromBitmap(getSelectedDiscoveredMarker()));
             } else {
                 marker.setIcon(BitmapDescriptorFactory.fromBitmap(getSelectedNotDiscoveredMarker()));
             }
 
             displayRibbon(mMapPlacesModel.getSelectedPlace(), true);
             marker.hideInfoWindow();
             mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 250, null);
 
             Analytics.track(Analytics.Event.SELECT_MYSTERY_ON_MAP);
 
             return true;
         }
     };
     private GoogleMap.OnMapClickListener mOnMapClickListener = new GoogleMap.OnMapClickListener() {
         @Override
         public void onMapClick(LatLng latLng) {
             Marker selectedMarker = mMapPlacesModel.getSelectedMarker();
             if (selectedMarker != null) {
                 resetMarkerIcon(selectedMarker);
             }
             hideRibbon();
             mMapPlacesModel.clearSelectedPlace();
         }
     };
 
     private void resetMarkerIcon(Marker marker) {
         if (mAccomplishableController.isMysteryFinished(mMapPlacesModel.getSelectedPlace().getId())) {
             marker.setIcon(BitmapDescriptorFactory.fromBitmap(getNormalDiscoveredMarker()));
         } else {
             marker.setIcon(BitmapDescriptorFactory.fromBitmap(getNormalNotDiscoveredMarker()));
         }
     }
 
     private Bitmap getNormalNotDiscoveredMarker() {
         return BitmapFactory.decodeResource(getResources(), R.drawable.light_mark);
     }
 
     private Bitmap getSelectedNotDiscoveredMarker() {
         return BitmapFactory.decodeResource(getResources(), R.drawable.dark_mark);
     }
 
     private Bitmap getNormalDiscoveredMarker() {
         return BitmapFactory.decodeResource(getResources(), R.drawable.light_mark_accepted);
     }
 
     private Bitmap getSelectedDiscoveredMarker() {
         return BitmapFactory.decodeResource(getResources(), R.drawable.dark_mark_accepted);
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mPrefs = StrollimoApplication.getService(StrollimoPreferences.class);
         setHasOptionsMenu(true);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         try {
             MapsInitializer.initialize(getActivity());
         } catch (GooglePlayServicesNotAvailableException ex) {
             Log.e(TAG, "Google play services not available.", ex);
         }
         if (mView == null) {
             firstStart = true;
             mView = inflater.inflate(R.layout.stroll_map_layout, container, false);
             mMapView = (MapView) mView.findViewById(R.id.map);
             mAccomplishableController = ((StrollimoApplication) getActivity().getApplication()).getService(AccomplishableController.class);
             mUserService = ((StrollimoApplication) getActivity().getApplication()).getService(UserService.class);
             mPlaceImage = (ProgressNetworkImageView) mView.findViewById(R.id.place_image);
             mPlaceImgProgress = mView.findViewById(R.id.place_progress);
             mPlaceTitle = (TextView) mView.findViewById(R.id.place_title);
             mRibbonPanel = mView.findViewById(R.id.ribbon_panel);
 
             mRibbonPanel.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     Log.i("BB", "on click");
                     launchDetailsActivity();
                 }
             });
 
             setSwipeToChangePlace(mRibbonPanel);
             mMapView.onCreate(savedInstanceState);
         } else {
             ViewGroup parentViewGroup = (ViewGroup) mView.getParent();
             if (parentViewGroup != null) {
                 parentViewGroup.removeAllViews();
             }
         }
         return mView;
     }
 
     private void setSwipeToChangePlace(View dismissableRibbon) {
         dismissableRibbon.setOnTouchListener(new SwipeDismissTouchListener(
                 dismissableRibbon,
                 null,
                 new SwipeDismissTouchListener.OnDirectionalDismissCallback() {
                     @Override
                     public void onDismiss(View view, Object token, DismissDirectionType dismissDirectionType) {
                         Mystery toMystery;
                         if (dismissDirectionType == DismissDirectionType.RIGHT) {
                             toMystery = mAccomplishableController.getPrevMisteryOf(mMapPlacesModel.getSelectedPlace());
                         } else {
                             toMystery = mAccomplishableController.getNextMisteryOf(mMapPlacesModel.getSelectedPlace());
                         }
                         if (toMystery != null) {
                             //reset previous selection if any
                             Marker oldMarker = mMapPlacesModel.getSelectedMarker();
                             if (oldMarker != null) {
                                 resetMarkerIcon(oldMarker);
                             }
                             Marker newMarker = mMapPlacesModel.getMarkerForPlace(toMystery);
                             mMapPlacesModel.onMarkerClick(newMarker);
                             // TODO: replace this with assets
                             newMarker.setIcon(BitmapDescriptorFactory.fromBitmap(getNormalNotDiscoveredMarker()));
                             mMap.animateCamera(CameraUpdateFactory.newLatLng(newMarker.getPosition()), 250, null);
                             mMapPlacesModel.selectMapPlaceByPlace(toMystery);
                             displayRibbon(mMapPlacesModel.getSelectedPlace(), dismissDirectionType != DismissDirectionType.RIGHT);
 
                             Analytics.track(Analytics.Event.SELECT_MYSTERY_ON_MAP);
 
                         } else {
                             Marker selectedMarker = mMapPlacesModel.getSelectedMarker();
                             if (selectedMarker != null) {
                                 resetMarkerIcon(selectedMarker);
                             }
                             removeCircleRadius();
                             mRibbonPanel.setVisibility(View.GONE);
                             mMapPlacesModel.clearSelectedPlace();
                         }
                     }
                 }));
     }
 
     @Override
     public void onResume() {
         super.onResume();
         mMapView.onResume();
         mMapPlacesModel.refreshSelectedMarker();
         // TODO BB: do we need this dialog?
 //        if (mUserService.getFoundPlacesNum() == mAccomplishableController.getMysteriesCount() && mAccomplishableController.getMysteriesCount() != 0) {
 //            new DemoFinishedDialog().show(getActivity().getSupportFragmentManager(), "dialog");
 //        }
     }
 
     @Override
     public void onStart() {
         super.onStart();
         Log.i("BB", "onStart");
         setUpMapIfNecessary();
 
         setUpLocationClientIfNecessary();
         setUpMapMarkersIfNecessary();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mMapView.onPause();
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.main, menu);
     }
 
     @Override
     public void onPrepareOptionsMenu(Menu menu) {
         if (menu == null) {
             return;
         }
         MenuItem addMysteryItem = menu.findItem(R.id.add_mystery);
         if (addMysteryItem != null) {
             addMysteryItem.setVisible(mPrefs.isDebugModeOn());
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.add_mystery) {
             startActivity(new Intent(getActivity(), AddMysteryActivity.class));
             return true;
         } else {
             return super.onOptionsItemSelected(item);
         }
     }
 
     private void removeCircleRadius() {
         if (mCircleRadius != null) {
             mCircleRadius.remove();
             mCircleRadius = null;
         }
     }
 
     private void setUpMapIfNecessary() {
         if (mMap == null) {
             mMap = mMapView.getMap();
             mMap.setMyLocationEnabled(true);
             mMap.setOnInfoWindowClickListener(onInfoWindowClickListener);
             mMap.setOnMarkerClickListener(mOnMarkerClickListener);
             mMap.setOnMapClickListener(mOnMapClickListener);
             mMap.getUiSettings().setZoomControlsEnabled(false);
             mMap.getUiSettings().setCompassEnabled(false);
         }
     }
 
     public void setUpMapMarkersIfNecessary() {
         if (mMapPlacesModel != null) {
             return;
         }
 
         mMapPlacesModel = new MapPlacesModel(mUserService, mAccomplishableController);
         mMap.clear();
         for (Mystery mystery : mAccomplishableController.getAllMysteries()) {
             addPlaceToMap(mystery);
         }
     }
 
     private void addPlaceToMap(Mystery mystery) {
         BitmapDescriptor bitmapDescriptor;
         if (mAccomplishableController.isMysteryFinished(mystery.getId())) {
             bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(getNormalDiscoveredMarker());
         } else {
             bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(getNormalNotDiscoveredMarker());
         }
 
         Marker marker = mMap.addMarker(new MarkerOptions()
                 .position(new LatLng(mystery.getLocation().getLat(), mystery.getLocation().getLng()))
                 .title(mystery.getName()).icon(bitmapDescriptor));
         mMapPlacesModel.add(mystery, marker);
     }
 
     @Override
     public void onStop() {
         mLocationClient.disconnect();
         super.onStop();
     }
 
     private void launchDetailsActivity() {
         Mystery selectedMystery = mMapPlacesModel.getSelectedPlace();
 
         Analytics.track(Analytics.Event.OPEN_MYSTERY_MAIN);
 
         if (selectedMystery != null) {
             this.startActivity(MysteryOpenActivity.createDetailsIntent(getActivity(), selectedMystery.getId()));
         }
     }
 
     private void setUpLocationClientIfNecessary() {
         if (mLocationClient == null) {
             mLocationClient = new LocationClient(getActivity(), new GooglePlayServicesClient.ConnectionCallbacks() {
                 @Override
                 public void onConnected(Bundle bundle) {
                     if (firstStart) {
                         Location loc = mLocationClient.getLastLocation();
                         Mystery mystery = mAccomplishableController.getFirstMystery();
                         LatLng latLng;
                         if (mystery != null) {
                             latLng = new LatLng(mystery.getLocation().getLat(), mystery.getLocation().getLng());
                         } else if (loc != null) {
                             latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                         } else {
                             latLng = AppGlobals.LONDON_SOMERSET_HOUSE;
                         }
                         CameraPosition pos = CameraPosition.builder().target(latLng).zoom(16f).tilt(45).build();
                         mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
                         firstStart = false;
                     }
                 }
 
                 @Override
                 public void onDisconnected() {
 
                 }
             }, new GooglePlayServicesClient.OnConnectionFailedListener() {
                 @Override
                 public void onConnectionFailed(ConnectionResult connectionResult) {
 
                 }
             }
             );
         }
         if (!mLocationClient.isConnected()) {
             mLocationClient.connect();
         }
     }
 
     private void displayRibbon(Mystery mystery, boolean fromRight) {
         Animation anim = AnimationUtils.loadAnimation(getActivity(), fromRight ? R.anim.slide_in_from_right : R.anim.slide_in_from_left);
         mRibbonPanel.setVisibility(View.VISIBLE);
 
         String imageUrl = StrollimoApplication.getService(AmazonS3Controller.class).getUrl(mystery.getImgUrl());
         mPlaceImage.setImageUrl(imageUrl, mPlaceImgProgress);
 
         mPlaceTitle.setText(mystery.getName().toUpperCase());
         mRibbonPanel.startAnimation(anim);
     }
 
     private void hideRibbon() {
         if (mMapPlacesModel.getSelectedPlace() != null) {
             Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_to_left);
             anim.setAnimationListener(new Animation.AnimationListener() {
                 @Override
                 public void onAnimationStart(Animation animation) {
 
                 }
 
                 @Override
                 public void onAnimationEnd(Animation animation) {
                     mRibbonPanel.setVisibility(View.GONE);
                 }
 
                 @Override
                 public void onAnimationRepeat(Animation animation) {
 
                 }
             });
             mRibbonPanel.startAnimation(anim);
         }
     }
 
 }
