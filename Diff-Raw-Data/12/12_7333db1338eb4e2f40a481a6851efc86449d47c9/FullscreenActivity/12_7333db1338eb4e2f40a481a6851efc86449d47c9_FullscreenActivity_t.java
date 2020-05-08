 package com.teamgrau.altourism;
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.*;
 import android.location.Location;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.renderscript.Allocation;
 import android.renderscript.RenderScript;
 import android.text.SpannableString;
 import android.text.style.ForegroundColorSpan;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.*;
 import android.widget.*;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.Projection;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.*;
 import com.teamgrau.altourism.rs.ScriptC_invertAlpha;
 import com.teamgrau.altourism.util.AltourismLocationSource;
 import com.teamgrau.altourism.util.AltourismNewStoryView;
 import com.teamgrau.altourism.util.SystemUiHider;
 import com.teamgrau.altourism.util.data.*;
 import com.teamgrau.altourism.util.data.model.POI;
 import com.teamgrau.altourism.util.data.model.Story;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 /**
  * An example full-screen activity that shows and hides the system UI (i.e.
  * status bar and navigation/system bar) with user interaction.
  *
  * @see SystemUiHider
  */
 public class FullscreenActivity extends android.support.v4.app.FragmentActivity
         implements GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraChangeListener,
                    GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerDragListener,
                    GoogleMap.OnMapClickListener, OnStoryProviderFinishedListener, View.OnClickListener {
 
     /**
      * Reference to GoogleMap instance
      */
     private GoogleMap mMap;
 
     /**
      * Reference to the AltourismInfoWindow Adapter
      */
     private GoogleMap.InfoWindowAdapter mIwa;
 
     /**
      * List of currently loaded Markers
      */
     private List<Marker> currentMarkers;
 
     /**
      * holds the current overlay a.k.a. war dust
      */
     private GroundOverlay mGroundOverlay;
     private GPSTracker mTracker;
     private RenderScript mRS;
     private ScriptC_invertAlpha mScript;
     private LocationService mBoundService;
     private Marker mMarker;
 
     private LinkedList<StoryProvider> mStoryProviders;
     private boolean mIsBound;
     private ServiceConnection mConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service) {
             // This is called when the connection with the service has been
             // established, giving us the service object we can use to
             // interact with the service.  Because we have bound to a explicit
             // service that we know is running in our own process, we can
             // cast its IBinder to a concrete class and directly access it.
             mBoundService = ((LocationService.LocalBinder)service).getService();
             mMap.setLocationSource ( mBoundService.getLocationSource () );
             mBoundService.getLocationSource ().activate ( mTracker );
         }
 
         public void onServiceDisconnected(ComponentName className) {
             // This is called when the connection with the service has been
             // unexpectedly disconnected -- that is, its process crashed.
             // Because it is running in our same process, we should never
             // see this happen.
             mBoundService = null;
         }
     };
 
     void doBindService() {
         // Establish a connection with the service.  We use an explicit
         // class name because we want a specific service implementation that
         // we know will be running in our own process (and thus won't be
         // supporting component replacement by other applications).
         bindService(new Intent (FullscreenActivity.this,
                 LocationService.class), mConnection, Context.BIND_AUTO_CREATE);
         mIsBound = true;
     }
 
     void doUnbindService() {
         if (mIsBound) {
             // Detach our existing connection.
             unbindService(mConnection);
             mIsBound = false;
         }
     }
 
 
     @Override
     protected void onCreate( Bundle savedInstanceState )
     {
         super.onCreate( savedInstanceState );
 
         mTracker = new GPSTrackerLocalDB ( this );
 
         mRS = RenderScript.create(this);
 
         setContentView(R.layout.main_view);
 
         TextView welcomeBubble = (TextView) findViewById( R.id.welcome_bubble_title );
         welcomeBubble.setTypeface( Typeface.createFromAsset( getAssets(), "fonts/miso-bold.otf" ));
         welcomeBubble.setPaintFlags( welcomeBubble.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG );
         welcomeBubble = (TextView) findViewById( R.id.welcome_bubble_text);
         welcomeBubble.setTypeface( Typeface.createFromAsset( getAssets(), "fonts/miso-light.otf" ));
         ImageView wbcb = (ImageView) findViewById( R.id.close_welcome_bubble_button );
         wbcb.setOnClickListener( new View.OnClickListener() {
             @Override
             public void onClick( View v ) {
                 findViewById( R.id.welcome_bubble ).setVisibility( View.GONE );
                 findViewById( R.id.menu_contaier ).setVisibility( View.VISIBLE );
             }
         });
 
 //        View v = findViewById ( R.id.menu_contaier );
 //        v.setVisibility ( View.VISIBLE );
 
         ActionBar actionBar = getActionBar();
         actionBar.hide();
 
         ImageButton b = (ImageButton) findViewById ( R.id.tell_story_button );
         b.setOnClickListener ( this );
 
         findViewById ( R.id.close_button ).setOnClickListener ( this );
 
         doBindService ();
         insertDummyStories();
 
         setUpMapIfNeeded ();
 
         setUpStoryProviders();
 
         mMap.setOnCameraChangeListener(this);
 
         findViewById ( R.id.menuButton ).setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 ImageButton b = (ImageButton) v;
                 b.setRotation(b.getRotation() + 90);
                 View menu = findViewById(R.id.menu_contaier);
                 menu.setVisibility(menu.getVisibility()==View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
             }
         });
         findViewById ( R.id.cancelButton ).setOnClickListener ( this );
     }
 
     private void insertDummyStories () {
         StoryArchivist sa = new StoryArchivistLocalDB ( getBaseContext () );
        Location l = new AltourismLocation ( "DummyLocation", 52.52455991534191, 13.401819951832294 );
         String head = "Gallerie Neurotitan";
         String body = "War hier zum ersten mal während des Pictoplasma Festivals 2012 und habe dort die großartigen Illustrationen von Jean Milch (unbedingt googeln!) gesehen. Aber fast noch besser ist der Buchshop der direkt mit der Galerie verbunden ist. Nach einem kurzen Gespräch mit der freundlichen Kassiererin habe ich dann noch erfahren das hier während der Zeit des Nationalsozialismus jüdischen Familien ein versteck geboten wurde.";
         sa.storeGeschichte ( l, new Story ( body, head ) );
 
         head = "Kino Central";
         body = "Wohl das gemütlichste Kino in dem ich je saß! Auf schwarzen Ledersesseln können hier zu einem fairen Preis jede menge Underground und Arthouse Filme angeschaut werden. Meistens in Original Fassung mit Untertiteln.";
         sa.storeGeschichte ( l, new Story ( body, head ) );
 
         head = "Café Cinema";
         body = "In meinem ersten Monat in Berlin wurde ich hier von meinem Date versetzt – während sich mein kleines Luftschloss in Rauch auflöste konnte ich von hier aus wunderbar das bunte treiben auf der Straße beobachten. Das obligatorische Eis gab es gleich im Anschluss (war lecker)!";
         sa.storeGeschichte ( l, new Story ( body, head ) );
 
         head = "Hof";
         body = "Bei schlechten Wetter wurde ich vom Shuttlebus, wie alle anderen Touristen direkt vorm Alex ausgespuckt... Hmm, irgendwie hab ich mir Berlin doch anders vorgestellt – nicht so langweilig und trostlos.\n" +
                 "Also folgte ich via Altourism dem nächstbesten Punk. Direkt am Hackeschenmarkt, etwas versteckt zwischen Starbucks und noblen Mode Boutiquen befindet sich das Kino Central.\n" +
                 "An sich ist das schon einen Besuch Wert – aber fast noch interessanter ist der Hinterhof! Voll mit Street Art und Skulpturen lässt sich der letzte Rest vom alternativen Berlin in Mitte genießen! Am besten mit nem leckeren Kaffee vom benachbarten Café Cinema.";
         sa.storeGeschichte ( l, new Story ( body, head ) );
     }
 
     private void setUpStoryProviders () {
         mStoryProviders = new LinkedList<StoryProvider> (  );
         //mStoryProviders.add ( new StoryProviderFoursquare ( this ) );
         mStoryProviders.add ( new StoryProviderLocalDB ( this ) );
     }
 
     @Override
     protected void onRestart() {
         super.onRestart ();
         refreshOverlay ();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy ();
         doUnbindService ();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     protected void onPostCreate( Bundle savedInstanceState )
     {
         super.onPostCreate( savedInstanceState );
     }
 
     private void setUpMapIfNeeded() {
         // Do a null check to confirm that we have not already instantiated the map.
         if (mMap == null) {
             // Try to obtain the map from the SupportMapFragment.
             mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                     .getMap();
             // Check if we were successful in obtaining the map.
             if (mMap != null) {
                 setUpMap();
             }
         }
     }
 
     private void refreshOverlay() {
         if (mGroundOverlay!=null)
             mGroundOverlay.remove();
 
         mGroundOverlay = mMap.addGroundOverlay(renderOverlay());
     }
 
     private GroundOverlayOptions renderOverlay() {
         Projection p = mMap.getProjection();
         LatLngBounds b = p.getVisibleRegion().latLngBounds;
 
         Log.d("Altourism beta", "Rendering with " + b.toString());
 
         List<Location> lList;
         lList = mTracker.getLocations(b);
 
         List<Point> pList = new LinkedList<Point>();
         for (Location l : lList)
             pList.add(p.toScreenLocation(new LatLng(l.getLatitude(), l.getLongitude())));
 
         DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
 
         /**
          * TODO: change to Bitmap.Config.Alpha_8
          * Would reduce memory usage but inverting of alpha channel only worked with this.
          * we should examine the structure of a alpha 8 pixel array
          */
         Bitmap bitmap = Bitmap.createBitmap(metrics.widthPixels+1, metrics.heightPixels+1, Bitmap.Config.ARGB_8888);
         Canvas c = new Canvas(bitmap);
 
         GroundOverlayOptions gOO = new GroundOverlayOptions()
                 .positionFromBounds ( b );
 
         /**
          * shortcut for unexplored areas
          */
         if (lList.size()==0) {
             c.drawARGB(0xcc, 0, 0, 0);
             return gOO.image(BitmapDescriptorFactory.fromBitmap(bitmap));
         }
 
         Paint pathPaint = new Paint();
         pathPaint.setColor(0xff000000); // <- transparent white
         /**
          * TODO: make width dependent on zoom level
          */
         pathPaint.setStrokeWidth(100); // <- width in pixels
 
         ListIterator<Point> pIter = pList.listIterator();
         Point prev = pIter.next();
         Point current = null;
 
         while (pIter.hasNext()) {
             current = pIter.next();
             c.drawLine(prev.x, prev.y, current.x, current.y, pathPaint);
             c.drawCircle(prev.x, prev.y, 50, pathPaint);
             prev = current;
         }
         // also the last location need rounded corners :-)
         c.drawCircle(prev.x, prev.y, 25, pathPaint);
 
         Bitmap overlay = invertAlphaRS(bitmap);
 
         return gOO.image(BitmapDescriptorFactory.fromBitmap(overlay));
     }
 
     /**
      * This function manipulates the alpha values of a given bitmap.
      * It is not generic in any way, its purpose is to process a bitmap
      * generated from renderOverlay(). The drawn path gets its alpha values
      * set to 0x00 for full transparency and the remaining pixels a value
      * with some less.
      * @param original Reference to the bitmap to manipulate
      * @return Reference to the manipulated bitmap
      */
     /*private Bitmap invertAlpha(Bitmap original) {
         int[] alphas = new int[original.getWidth() * original.getHeight()];
         original.getPixels(alphas, 0, original.getWidth(), 0, 0, original.getWidth(), original.getHeight());
 
         for (int i = 0; i < alphas.length; i++) {
             alphas[i] = (alphas[i]==0xff000000) ? 0x00000000 : 0xaa000000;
         }
 
         original.setPixels(alphas, 0, original.getWidth(), 0, 0, original.getWidth(), original.getHeight());
         return original;
     }*/
 
     private Bitmap invertAlphaRS(Bitmap original) {
         Allocation alloc = Allocation.createFromBitmap(mRS, original,
                 Allocation.MipmapControl.MIPMAP_NONE,
                 Allocation.USAGE_SCRIPT);
         mScript = new ScriptC_invertAlpha(mRS, getResources(), R.raw.invertalpha);
         mScript.forEach_root(alloc);
         alloc.copyTo(original);
         return original;
     }
 
     private void setUpMap() {
 
         mMap.setMyLocationEnabled(true);
         mMap.setMapType ( GoogleMap.MAP_TYPE_SATELLITE );
         mMap.getUiSettings().setMyLocationButtonEnabled(true);
         // Hide the zoom controls as the button panel will cover it.
         mMap.getUiSettings().setZoomControlsEnabled(true);
 
         // Add lots of markers to the map.
         // TODO: There comes a time when we won't need static markers anymore
         addMarkersToMap();
 
         // Request POIs from story providers
         requestPOIs( mMap.getMyLocation () );
 
         // Set listeners for marker events.  See the bottom of this class for their behavior.
         mMap.setOnMarkerClickListener( this );
         mMap.setOnInfoWindowClickListener( this );
         mMap.setOnMarkerDragListener( this );
         mMap.setOnMapClickListener ( this );
 
         // Pan to see all markers in view.
         // Cannot zoom to bounds until the map has a size.
         final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
         if (mapView.getViewTreeObserver().isAlive()) {
             mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                 @SuppressLint("NewApi") // We check which build version we are using.
                 @Override
                 public void onGlobalLayout() {
                     LatLngBounds.Builder b = new LatLngBounds.Builder();
                     //for (Location l : mTracker.getLocations ( 10 )) {
                     //    b.include ( new LatLng ( l.getLatitude (), l.getLongitude () ) );
                     //}
 
                     // TODO:replace by last known locations
                     for (Marker m : currentMarkers) {
                         b.include(m.getPosition());
                     }
                     LatLngBounds bounds = b.build();
 
                     if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                         //noinspection deprecation
                         mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                     } else {
                         mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                     }
                     mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
 
                     // not sure if this is the best position but overlay rendering also needs sizes
                     refreshOverlay();
                 }
             });
         }
     }
 
     private void addMarkersToMap() {
         StoryProvider sp = new StoryProviderHardcoded();
         List<POI> poiList = sp.listPOIs(mMap.getMyLocation(), 200);
         currentMarkers = new LinkedList<Marker>();
 
         for (POI p : poiList) {
             Log.d ( "Altourism beta", "Hardcoded: " + p.getStories ().get(0).getTitle() + " " + p.getPosition ().getLatitude () + " " +p.getPosition ().getLongitude () );
 
             currentMarkers.add(mMap.addMarker(new MarkerOptions()
                     .position(new LatLng(p.getPosition().getLatitude(), p.getPosition().getLongitude()))
                     .title(p.getTitle())
                     .icon(BitmapDescriptorFactory.fromResource(R.drawable.altourism_pov))));
         }
     }
 
     private boolean checkReady() {
         if (mMap == null) {
             Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
             return false;
         }
         return true;
     }
 
     //
     // Marker related listeners.
     //
 
     @Override
     public boolean onMarkerClick(final Marker marker) {
         // Move camera so whole bubble is visible
         Projection proj = mMap.getProjection();
         Point startPoint = proj.toScreenLocation( marker.getPosition() );
         startPoint.offset( 0, -365 );
         final LatLng startLatLng = proj.fromScreenLocation( startPoint );
         CameraPosition camPos = new CameraPosition.Builder()
                 .target(startLatLng)
                 .zoom(mMap.getCameraPosition().zoom)
                 .build();
 
         mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
 
 
         View v = findViewById ( R.id.menu_contaier );
         v.setVisibility ( View.GONE );
 
         LinearLayout infoWindow = (LinearLayout) findViewById( R.id.info_window );
         render( marker, infoWindow );
         infoWindow.setVisibility( View.VISIBLE );
 
         return true;
     }
 
     @Override
     public void onInfoWindowClick(Marker marker) {
         Toast.makeText(getBaseContext(), "Click Info Window", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public void onMarkerDragStart(Marker marker) {
         //mTopText.setText("onMarkerDragStart");
         Toast.makeText(getBaseContext(), "onMarkerDragStart", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public void onMarkerDragEnd(Marker marker) {
         //mTopText.setText("onMarkerDragEnd");
         Toast.makeText(getBaseContext(), "onMarkerDragEnd", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public void onMarkerDrag(Marker marker) {
         //mTopText.setText("onMarkerDrag.  Current Position: " + marker.getPosition());
         Toast.makeText(getBaseContext(), "onMarkerDrag.  Current Position: " + marker.getPosition(), Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public void onCameraChange(CameraPosition cameraPosition) {
         Log.d ( "Altourism beta", "camera changed" );
         mMap.clear ();
         refreshOverlay ();
         Location l = new Location ( "Cam Location" );
         l.setLatitude ( cameraPosition.target.latitude );
         l.setLongitude ( cameraPosition.target.longitude );
         requestPOIs ( l );
     }
 
 
     private void requestPOIs ( Location location ) {
         if ( mStoryProviders == null ) {
             Log.d ( this.getClass ().getName (), "StoryProviderList not initialized yet." );
             return;
         }
         if ( location == null ) {
             Log.d ( this.getClass ().getName (), "requestPOIs() location is null." );
             return;
         }
         for ( StoryProvider sp : mStoryProviders ) {
             sp.listPOIs ( location, 100, this );
         }
     }
 
     @Override
     public void onStoryProviderFinished ( List<POI> poiList ) {
         currentMarkers = new LinkedList<Marker>();
 
         if ( poiList == null ) {
             Log.d ( "Altourism beta", "poiList came by null" );
             return;
         }
         Log.i ( "Altourism beta", "list of pois length: " + poiList.size () );
         for (POI p : poiList) {
             Log.d ( "Altourism beta", "wichtig: "+p.getStories ().get(0).getTitle() + " " + p.getPosition ().getLatitude () + " " +p.getPosition ().getLongitude () );
 
             currentMarkers.add ( mMap.addMarker ( new MarkerOptions ()
                     .position ( new LatLng ( p.getPosition ().getLatitude (), p.getPosition ().getLongitude () ) )
                     .title ( p.getTitle () )
                     .icon ( BitmapDescriptorFactory.fromResource ( R.drawable.altourism_pov ) ) ) );
         }
     }
 
     @Override
     public void onMapClick ( LatLng latLng ) {
         View v = findViewById ( R.id.menu_contaier );
         if ( v.getVisibility () != View.VISIBLE ) {
             v.setVisibility ( View.VISIBLE );
         }
         findViewById( R.id.info_window ).setVisibility ( View.GONE );
     }
 
     private void render( final Marker marker, View view ) {
         String title = marker.getTitle ();
         TextView titleUi = ((TextView) view.findViewById( R.id.title ));
         if ( title != null ) {
             // Spannable string allows us to edit the formatting of the text.
             SpannableString titleText = new SpannableString( title );
             titleText.setSpan( new ForegroundColorSpan( Color.DKGRAY ), 0, titleText.length(), 0 );
             titleUi.setText(titleText.toString().toUpperCase());
             titleUi.setPaintFlags(titleUi.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
             titleUi.setTypeface( Typeface.createFromAsset( getAssets(), "fonts/miso-bold.otf" ));
         } else {
             titleUi.setText( "" );
         }
         ImageView ib = (ImageView) view.findViewById( R.id.close_button );
         ib.setOnClickListener( new View.OnClickListener() {
             @Override
             public void onClick( View v ) {
                 findViewById( R.id.info_window ).setVisibility( View.GONE );
                 findViewById( R.id.menu_contaier ).setVisibility( View.VISIBLE );
             }
         });
         ((TextView) view.findViewById( R.id.share_on )).
                 setTypeface(Typeface.createFromAsset(getAssets(), "fonts/miso-light.otf"));
 
 
         mMarker = marker;
         final View.OnClickListener clickListener = this;
         // Now we setup the Story list and show it in the InfoWindow
         ExpandableListView ev = (ExpandableListView) view.findViewById(R.id.expandableListView);
         ev.setAdapter( new BaseExpandableListAdapter() {
 
             final StoryProvider sp = new StoryProviderLocalDB( getBaseContext() );
 
             // insert test stories first
             StoryArchivist sa = new StoryArchivistLocalDB( getBaseContext() );
             Location p = new Location( "Simon" );
             {
                 p.setLongitude(marker.getPosition().longitude);
                 p.setLatitude(marker.getPosition().latitude);
                 sa.storeGeschichte(p,new Story("Die große Granitwanne vor dem Alten Museum stammt aus einem größeren Findling in der Nähe " +
                         " von Angermünde. Eine interessante Geschichte steckt dahinter", "Findlings-Wanne"));
                 sa.storeGeschichte(p,new Story("Der Dom ist grundsätzlich für Besucher geoeffnet (kostenlos). Mindestens einen Guck wert!", "Berliner Dom ansehen!"));
                 sa.storeGeschichte(p,new Story("Innerhalb einer Stunde treffen sich an der Weltzeituhr ca 100 Gruppen. Zählt bei " +
                         "einem Kaffee mal mit!", "Gruppenzählen"));
                 sa.storeGeschichte(p,new Story("Am westlichen Durchgang unter den Schienen am Hackeschen Markt kann man die lustigsten Poster sehen.", "Poster Tour"));
 
             }
 
             final POI poi = sp.getPOI( p );
             final List<Story> geschichten = poi.getStories();
 
             @Override
             public int getGroupCount() {
                 return geschichten.size()+1;
             }
 
             @Override
             public int getChildrenCount(int groupPosition) {
                 if ( groupPosition != 0 ) {
                     return 1;
                 }
                 else {
                     return 0;
                 }
             }
 
             @Override
             public Object getGroup(int groupPosition) {
                 return geschichten.get( groupPosition-1 ).getTitle();
             }
 
             @Override
             public Object getChild(int groupPosition, int childPosition) {
                 return geschichten.get( groupPosition-1 ).getStoryText();
             }
 
             @Override
             public long getGroupId(int groupPosition) {
                 return groupPosition;
             }
 
             @Override
             public long getChildId(int groupPosition, int childPosition) {
                 return childPosition;
             }
 
             @Override
             public boolean hasStableIds() {
                 return true;
             }
 
             @Override
             public View getGroupView( int groupPosition, boolean isExpanded, View convertView, ViewGroup parent ) {
                 if ( convertView != null && groupPosition != 0 ) {
                     if ( isExpanded ){
                         ((ImageView) ((LinearLayout) convertView).getChildAt( 1 )).setImageResource(
                                 R.drawable.altourism_hcc_story_close );
                     }
                     else {
                         ((ImageView) ((LinearLayout) convertView).getChildAt( 1 )).setImageResource(
                                 R.drawable.altourism_hcc_story_open );
                     }
                     if (((LinearLayout) convertView).getChildAt( 0 ) instanceof LinearLayout ) {
                         ((TextView) ((LinearLayout)((LinearLayout) convertView).getChildAt( 0 )).getChildAt(0)).
                                 setText( getGroup( groupPosition ).toString());
                         ((TextView) ((LinearLayout)((LinearLayout) convertView).getChildAt( 0 )).getChildAt(1)).
                                 setText( "Tags: Grafiti, Urban, Secret" );
                         convertView.setPadding( 0, 0, 0, 3 );
                         return convertView;
                     }
                     else { // convertView is the add new story row and cannot be reused so we continue after this if
                     }
                 }
                 else if ( convertView != null && ((LinearLayout) convertView).getChildAt( 0 ) instanceof TextView){
                     // and: groupPosition == 0
                     ((TextView) ((LinearLayout) convertView).getChildAt( 0 )).setText( R.string.tell_a_new_story_to_this_point );
                     ((ImageView) ((LinearLayout) convertView).getChildAt( 1 )).setImageResource(
                             R.drawable.altourism_hcc_story_new );
 
                     /** pretty dirty but it should do ... */
                     ((LinearLayout) convertView).getChildAt( 0 ).setOnClickListener ( clickListener );
                     ((LinearLayout) convertView).getChildAt( 0 ).setId ( R.id.add_story_to_poi );
                     ((LinearLayout) convertView).getChildAt( 1 ).setOnClickListener ( clickListener );
                     ((LinearLayout) convertView).getChildAt( 1 ).setId ( R.id.add_story_to_poi );
                     convertView.setPadding( 0, 0, 0, 12 );
                     return convertView;
                 }
                 else { // convertView is a story row but we need the add new story view here so continue after this if
                 }
                 LinearLayout l = new LinearLayout( getBaseContext() );
                 l.setOrientation(LinearLayout.HORIZONTAL);
                 l.setLayoutParams( new AbsListView.LayoutParams(
                         ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ));
                 l.setGravity(Gravity.LEFT);
                 ImageView iv = new ImageView( getBaseContext() );
                 iv.setLayoutParams(new AbsListView.LayoutParams(
                         ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0));
                 iv.setPadding( 3, 0, 0, 0 );
                 //iv.setScaleX(0.75f);
                 //iv.setScaleY(0.75f);
 
                 TextView tv = new TextView( getBaseContext() );
                 tv.setTypeface( Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/miso-bold.otf" ));
                 tv.setTextColor( 0xffffffff );
                 tv.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 24 );
                 tv.setLayoutParams( new LinearLayout.LayoutParams(
                         ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1 )); // same heigth as the expand-arrow
                 tv.setSingleLine(true);                                                               // w=1 so spare space is given to tv
                 tv.setBackgroundResource(R.color.black);
 
                 if ( groupPosition == 0 ){
                     tv.setText( R.string.tell_a_new_story_to_this_point );
                     tv.setPaddingRelative( 6, 2, 6, 6 );
                     iv.setImageResource( R.drawable.altourism_hcc_story_new );
                     l.setPadding( 0, 0, 0, 12 );
                     l.addView( tv );
                     l.addView( iv );
                 }
                 else {
                     LinearLayout tl = new LinearLayout( getBaseContext() );
                     tl.setBackgroundResource( R.color.black );
                     tl.setLayoutParams( new LinearLayout.LayoutParams(
                             ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
                     tl.setOrientation( LinearLayout.VERTICAL );
                     tl.setPadding( 6, 2, 6, 6 );
                     TextView tt = new TextView( getBaseContext() );
                     tt.setLayoutParams( new LinearLayout.LayoutParams(
                             ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0 ));
                     tt.setBackgroundResource( R.color.black );
                     tt.setTextColor( 0xffffffff );
                     tt.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 24 );
                     tt.setTypeface( Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/miso-light.otf" ));
                     tt.setSingleLine( true );
                     tt.setText( "Tags: Grafiti, Urban, Secret" );
                     tv.setText( getGroup( groupPosition ).toString());
                     tl.addView( tv );
                     tl.addView( tt );
                     l.addView( tl );
                     l.addView( iv );
                     if ( ! isExpanded ) {
                         iv.setImageResource( R.drawable.altourism_hcc_story_open );
                     }
                     else {
                         iv.setImageResource( R.drawable.altourism_hcc_story_close );
                     }
                     l.setPadding( 0, 0, 0, 3 );
                 }
                 return l;
             }
 
             @Override
             public View getChildView( int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent ) {
                 if ( convertView != null ){
                     ((TextView) convertView).setText( getChild( groupPosition, childPosition ).toString() );
                     return convertView;
                 }
                 TextView tv = new TextView( getBaseContext() );
                 tv.setTypeface( Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/miso-light.otf" ));
                 tv.setTextColor( 0xff000000 );
                 tv.setLayoutParams( new AbsListView.LayoutParams(
                         ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ));
                 tv.setText( getChild( groupPosition, childPosition ).toString() );
                 tv.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 15 );
                 //parent.setPadding( 0, 3, 0, 6 );
                 //tv.setPadding( 0, 3, 0, 50 );
                 tv.setPaddingRelative( 6, 3, 6, 6 );
                 return tv;
             }
 
             @Override
             public boolean isChildSelectable(int groupPosition, int childPosition) {
                 return false;
             }
         });
     }
 
     @Override
     public void onClick ( View view ) {
         if ( view.getId () == R.id.close_button )  {
             findViewById ( R.id.info_window ).setVisibility ( View.GONE );
         } else if ( view.getId () == R.id.close_welcome_bubble_button ) {
             findViewById ( R.id.welcome_bubble ).setVisibility ( View.GONE );
         } else if ( view.getId () == R.id.cancelButton ) {
             finish ();
         } else if ( view.getId () == R.id.add_story_to_poi ) {
             Intent newStoryView = new Intent( this, AltourismNewStoryView.class );
             if ( mMarker==null || mMarker.getPosition ()==null ) {
                 Toast.makeText ( this, "That's embarrassing but something went terribly wrong.", Toast.LENGTH_SHORT );
                 return;
             }
             newStoryView.putExtra ( "lat", mMarker.getPosition ().latitude );
             newStoryView.putExtra ( "lng", mMarker.getPosition ().longitude );
             startActivity( newStoryView );
         } else if ( view.getId () == R.id.tell_story_button ) {
             Intent newStoryView = new Intent( this, AltourismNewStoryView.class );
             if ( mMap==null || mMap.getMyLocation ()==null ) {
                 Toast.makeText ( this, "We need to wait for a location, please try again soon.", Toast.LENGTH_SHORT );
                 return;
             }
             Marker newMarker = mMap.addMarker ( new MarkerOptions ()
                     .position ( new LatLng ( mMap.getMyLocation ().getLatitude (), mMap.getMyLocation ().getLongitude () ) )
                     .title ( "Test Title" ) // TODO: muss noch in der Activity dann auf den echten Titel gesetzt werden!
                     .icon ( BitmapDescriptorFactory.fromResource ( R.drawable.altourism_pov ) ) );
             newStoryView.putExtra ( "lat", newMarker.getPosition().latitude );
             newStoryView.putExtra ( "lng", newMarker.getPosition().longitude );
             startActivity( newStoryView );
         }
     }
 }
