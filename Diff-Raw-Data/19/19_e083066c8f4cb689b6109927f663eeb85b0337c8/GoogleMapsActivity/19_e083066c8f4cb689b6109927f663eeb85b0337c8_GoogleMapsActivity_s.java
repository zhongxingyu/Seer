 package duke.cs.fall2012.catseverywhere;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.google.android.maps.MapActivity;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 
 import duke.cs.fall2012.catseverywhere.gallery.ImageGridActivity;
 import duke.cs.fall2012.catseverywhere.model.ImageAdapter;
 
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.graphics.drawable.Drawable;
 import android.location.LocationManager;
 import android.media.ExifInterface;
 import android.net.Uri;
 
 import android.os.Bundle;
 import android.provider.MediaStore.MediaColumns;
 
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 
 
 public class GoogleMapsActivity extends MapActivity implements OnClickListener{
 
 	private static final int PICK_FROM_FILE = 2;
 	private MapView myMapView;
 	private static MapController myMapController;
 	private LocationManager myLocManager;
 	private CustomLocationListener myLocListener;
 	private Uri myIMGUri;
 	private String myPickedPath="";
 	private ExifInterface myExifInterface;
 	private float[] myPicLoc = new float[2];
 	private GeoPoint myPicGeoPoint;
 	private CustomItemizedOverlay myItemizedOverlay;
 	private Resources myResources;
 	private Drawable myMarker;
 	private Bitmap myPickedBitmap;
 	private final int MAX_ICON_WIDTH = 60;
 	private final int MAX_ICON_HEIGHT = 60;
 	private ImageAdapter myImageAdapter;
 	private ArrayList<GeoPoint> myGeoPoints;
 	
 	//nav bar
 	private ImageButton uploadButtonNav, galleryButtonNav, mapsButtonNav, prefButtonNav;
 	
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         myResources = this.getResources();
         setContentView(R.layout.activity_google_maps);
         myMapView = (MapView) findViewById(R.id.mapview1);
         myMapController = myMapView.getController();
         myLocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
         myLocListener = new CustomLocationListener(getApplicationContext());
         myLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocListener);
         myMarker = myResources.getDrawable(R.raw.blue_circle_60);
         myItemizedOverlay = new CustomItemizedOverlay(myMarker, this);
         myImageAdapter = new ImageAdapter();
         loadOnlineGeoPoints();
         //myMapView.getOverlays().add(myItemizedOverlay);
         
         final Button button = (Button) findViewById(R.id.button1);
         button.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
                 // Perform action on click
             	System.out.println("HAI");
             	Intent myPhotoPicker = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
             	startActivityForResult(myPhotoPicker, PICK_FROM_FILE);
             	
             	
             }
            
         });
         
 //      
 //        final Button imgSelectorButton = (Button) findViewById(R.id.button_img_selector);
 //        imgSelectorButton.setOnClickListener(new View.OnClickListener() {
 //            public void onClick(View v) {
 //                // Perform action on click
 //            	Intent imageSelctorScreen = new Intent(getApplicationContext(), ImageSelector.class);
 //            	startActivity(imageSelctorScreen);
 //            	
 //            }
 //           
 //        });
         
 
         final Button findMeButton = (Button) findViewById(R.id.button_find_me);
         findMeButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
             	myMapController.animateTo(myLocListener.getCurrentGeoP());
             	myMapController.setZoom(17);
             	
             }
            
         });
         
 
         final Button uploadButton = (Button) findViewById(R.id.button_upload);
         uploadButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
                 // Perform action on click
             	Intent imageUploadScreen= new Intent(getApplicationContext(), ImageUpload.class);
             	startActivity(imageUploadScreen);
             	
             }
            
         });
       
       final Button testButton = (Button) findViewById(R.id.button_test_main);
       testButton.setOnClickListener(new View.OnClickListener() {
 		@Override
 		public void onClick(View v) {
               // Perform action on click
           	Intent testMain= new Intent(getApplicationContext(), Main.class);
           	startActivity(testMain);
           	
           }
          
       });
       
       initialize();
 
     }
     
     private void loadOnlineGeoPoints() {
         myGeoPoints = myImageAdapter.getRandomGeopoints(5);
 		
 		for(GeoPoint point: myGeoPoints)
 		{
 		String picSnippet = "Lat: " + point.getLatitudeE6() +"\nLong: " + point.getLongitudeE6();
 
		OverlayItem myPicThumb = new OverlayItem(point, "Picture", picSnippet);
		//myPickedBitmap 	= BitmapFactory.decodeFile(myPickedPath);
		//Bitmap myPickedIcon = iconize(myPickedBitmap);
		//Drawable myDrawImg = new BitmapDrawable(myResources, myPickedPath);
		myItemizedOverlay.addOverlayItem(myPicThumb);
 		myMapView.getOverlays().clear();
 		myMapView.getOverlays().add(myItemizedOverlay);
		}
 	}
 
 	public void initialize() {
     	//nav bar
     	uploadButtonNav = (ImageButton) findViewById(R.id.bMapsUploadNav);
     	uploadButtonNav.setOnClickListener(this);
     	galleryButtonNav = (ImageButton) findViewById(R.id.bMapsGalleryNav);
     	galleryButtonNav.setOnClickListener(this);
     	mapsButtonNav = (ImageButton) findViewById(R.id.bMapsMapsNav);
     	mapsButtonNav.setOnClickListener(this);
     	prefButtonNav = (ImageButton) findViewById(R.id.bMapsPrefNav);
     	prefButtonNav.setOnClickListener(this);
     }
     
     
 
     @Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
     	if (requestCode == PICK_FROM_FILE) {
 			myIMGUri = data.getData(); 
 			myPickedPath = getRealPathFromURI(myIMGUri); //from Gallery
 			System.out.println("Just picked image wit path: " + myPickedPath);
 			myPicGeoPoint = getGeoLocData(myPickedPath);
 			myMapController.animateTo(myPicGeoPoint);
 			myMapController.setZoom(17);
 			String picName = getFileNameFromPath(myPickedPath);
 			System.out.println("IMAGE NAME: " + picName);
 			String picSnippet = "Lat: " + myPicGeoPoint.getLatitudeE6() +"\nLong: " + myPicGeoPoint.getLongitudeE6();
 			OverlayItem myPicThumb = new OverlayItem(myPicGeoPoint, picName, picSnippet);
 			//myPickedBitmap 	= BitmapFactory.decodeFile(myPickedPath);
 			//Bitmap myPickedIcon = iconize(myPickedBitmap);
 			//Drawable myDrawImg = new BitmapDrawable(myResources, myPickedPath);
 			myItemizedOverlay.addOverlayItem(myPicThumb);
 			myMapView.getOverlays().clear();
 			myMapView.getOverlays().add(myItemizedOverlay);
 			//myMapView.postInvalidate();
     	}
 	}
     
 
     private Bitmap iconize(Bitmap pic)
     {
     	int picWidth = pic.getWidth();
     	int picHeight = pic.getHeight();
 
     	// Constrain to given size but keep aspect ratio
     	float scaleFactor = Math.min(((float) MAX_ICON_WIDTH) / picWidth, ((float) MAX_ICON_HEIGHT) / picHeight);
     	Matrix scale = new Matrix();
     	scale.postScale(scaleFactor, scaleFactor);
     	Bitmap icon = Bitmap.createBitmap(pic, 0, 0, picWidth, picHeight, scale, false);
     	return icon;
     	
     	
     }
     private GeoPoint getGeoLocData(String path) {
 		// TODO Auto-generated method stub
 		System.out.println("Image path: " + path);
 		try {
 			myExifInterface = new ExifInterface(path);
 			myExifInterface.getLatLong(myPicLoc);
 			System.out.println("Pic latitude: " + myPicLoc[0] + "long: " + myPicLoc[1]);
 			GeoPoint geoP = new GeoPoint((int) (myPicLoc[0] * 1E6), (int) (myPicLoc[1] * 1E6));
 			return geoP;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
     }
 
     	private String getFileNameFromPath(String path)
     	{
     		return path.substring(path.lastIndexOf("/")+1);
     	}
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_google_maps, menu);
         return true;
     }
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	public static void animateToLoc(GeoPoint geoPoint)
 	{
 		myMapController.animateTo(geoPoint);
 	}
 	
 	public String getRealPathFromURI(Uri contentUri) {
         String [] proj 		= {MediaColumns.DATA};
         Cursor cursor 		= managedQuery( contentUri, proj, null, null,null);
         
         if (cursor == null) return null;
         
         int column_index 	= cursor.getColumnIndexOrThrow(MediaColumns.DATA);
         
         cursor.moveToFirst();
 
         return cursor.getString(column_index);
 	}
 	
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()) {
 		
 		//navbar
 		case R.id.bMapsUploadNav:
 			startActivity(new Intent(this, ImageUpload.class));
 			break;
 		case R.id.bMapsGalleryNav:
 			startActivity(new Intent(this, ImageGridActivity.class));
 			break;
 		case R.id.bMapsMapsNav:
 			startActivity(new Intent(this, GoogleMapsActivity.class));
 			break;
 		case R.id.bMapsPrefNav:
 			startActivity(new Intent(this, Preferences.class));
 			break;
 		}
 	}
 }
