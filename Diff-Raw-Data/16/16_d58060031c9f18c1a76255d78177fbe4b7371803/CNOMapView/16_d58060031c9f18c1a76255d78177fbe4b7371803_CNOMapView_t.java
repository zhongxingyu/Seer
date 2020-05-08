 package com.NightOutApps.chiconightout;
 
 import java.io.IOException;
 import java.util.Calendar;
 
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Rect;
 import android.graphics.Canvas;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Toast;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 
 
 public class CNOMapView extends MapActivity implements OnClickListener{
 	private MapView map;
 	private MapController control;
 	Calendar calendar = Calendar.getInstance();
 	int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
 	public static final String DRINKNAME = "DrinkName";
 	
 	public static final String DRINKDESCRIPT = "DrinkDescription";
 	private Cursor c = null;
 	public static final String DATABASE_TABLE = "Drink";
 	private String[] colsfrom = {"_id", DRINKNAME, DRINKDESCRIPT};
 	String barStr = "Bar_id = 2";
 	GeoPoint centerP = new GeoPoint(39728478,  -121842176);
 	CallACab cacButton;
 	 Facebook facebook = new Facebook("281094275327241");
 	 AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.map);
 		
		View bacButton = findViewById(R.id.bacbutton);
		bacButton.setOnClickListener(this);
 		
 		View barListButton = findViewById(R.id.barlistbutton);
         barListButton.setOnClickListener(this);
         
         View callACabButton = findViewById(R.id.cacbutton);
         callACabButton.setOnClickListener(this);
         View facebookButton = findViewById(R.id.facebookbutton);
         facebookButton.setOnClickListener(this);
         
         
 		initMapView();
 
 		
 		initMyLocation(); 
 		initDatabase(); //TODO this will eventually be done in an Async Task
 
 
 
 		GeoPoint bansheePoint = new GeoPoint(39730006,-121841087); //39729912,-121841041
 		GeoPoint beachPoint = new GeoPoint(39730693,-121839199); //39730685,-121839423
 		GeoPoint bellasPoint = new GeoPoint(39729984,-121841755);
 		GeoPoint crazyPoint = new GeoPoint(39729596,-121839414); //39729621,-121839522
 		GeoPoint downPoint = new GeoPoint(39729491,-121839170);
 		GeoPoint duffysPoint = new GeoPoint(39729171,-121838808);
 		GeoPoint joesPoint = new GeoPoint(39723920,-121844260);
 		GeoPoint lasallesPoint = new GeoPoint(39729414,-121840720);
 		GeoPoint lostPoint = new GeoPoint(39729412,-121839089);
 		GeoPoint bearPoint = new GeoPoint(39729000,-121842430); //39729192, 121842705
 		GeoPoint maltesePoint = new GeoPoint(39721210,-121827139);
 		GeoPoint panamasPoint = new GeoPoint(39730627,-121839792); //39730517,-121839671
 		GeoPoint rileysPoint = new GeoPoint(39724563,-121843874);
 		GeoPoint gradPoint = new GeoPoint(39724585,-121838365);
 		GeoPoint townPoint = new GeoPoint(39729303,-121838974);
 		GeoPoint ubarPoint = new GeoPoint(39730858,-121839387); //39730685, -121839423
 		
 		
 		Drawable bansheeIcon = this.getResources().getDrawable(R.drawable.banshee_icon_trans);
 		Drawable beachIcon = this.getResources().getDrawable(R.drawable.beach_icon_trans);
 		Drawable bellasIcon = this.getResources().getDrawable(R.drawable.bellas_icon_trans);
 		Drawable crazyIcon = this.getResources().getDrawable(R.drawable.crazy_horse_icon_trans);
 		Drawable downIcon = this.getResources().getDrawable(R.drawable.down_lo_icon_trans);
 		Drawable duffysIcon = this.getResources().getDrawable(R.drawable.duffys_icon_trans);
 		Drawable joesIcon = this.getResources().getDrawable(R.drawable.joes_icon_trans);
 		Drawable lasallesIcon = this.getResources().getDrawable(R.drawable.lasalles_icon_trans);
 		Drawable lostIcon = this.getResources().getDrawable(R.drawable.lost_on_main_icon_trans);
 		Drawable bearIcon = this.getResources().getDrawable(R.drawable.madison_bear_icon_trans);
 		Drawable malteseIcon = this.getResources().getDrawable(R.drawable.maltese_icon_trans);
 		Drawable panamasIcon = this.getResources().getDrawable(R.drawable.panamas_icon_trans);
 		Drawable rileysIcon = this.getResources().getDrawable(R.drawable.rileys_icon_trans);
 		Drawable gradIcon = this.getResources().getDrawable(R.drawable.the_grad_icon_trans);
 		Drawable townIcon = this.getResources().getDrawable(R.drawable.town_lounge_icon_trans);
 		Drawable ubarIcon = this.getResources().getDrawable(R.drawable.u_bar_icon_trans);
 		Drawable cnoIco = this.getResources().getDrawable(R.drawable.ic_launcher);
 		
 		Rect bansheeR = new Rect(-bansheeIcon.getIntrinsicWidth()/2, -bansheeIcon.getIntrinsicHeight(), bansheeIcon.getIntrinsicWidth()/2, 0);
 		Rect beachR = new Rect(0, -beachIcon.getIntrinsicHeight(), beachIcon.getIntrinsicWidth(), 0);
 		Rect bellasR = new Rect(-bellasIcon.getIntrinsicWidth()/2, -bellasIcon.getIntrinsicHeight(), bellasIcon.getIntrinsicWidth()/2, 0);
 		Rect crazyR = new Rect(-crazyIcon.getIntrinsicWidth()/2, -crazyIcon.getIntrinsicHeight(), crazyIcon.getIntrinsicWidth()/2, 0);
 		Rect downR = new Rect(-downIcon.getIntrinsicWidth(), -downIcon.getIntrinsicHeight(), 0, 0);
 		Rect duffysR = new Rect(0, -duffysIcon.getIntrinsicHeight(), duffysIcon.getIntrinsicWidth(), 0);
 		Rect joesR = new Rect(-joesIcon.getIntrinsicWidth()/2, -joesIcon.getIntrinsicHeight(), joesIcon.getIntrinsicWidth()/2, 0);
 		Rect lasallesR = new Rect(-lasallesIcon.getIntrinsicWidth()/2, -lasallesIcon.getIntrinsicHeight(), lasallesIcon.getIntrinsicWidth()/2, 0);
 		Rect lostR = new Rect(0, -lostIcon.getIntrinsicHeight(), lostIcon.getIntrinsicWidth(), 0);
 		Rect bearR = new Rect(-bearIcon.getIntrinsicWidth()/2, -bearIcon.getIntrinsicHeight(), bearIcon.getIntrinsicWidth()/2, 0);
 		Rect malteseR = new Rect(-malteseIcon.getIntrinsicWidth()/2, -malteseIcon.getIntrinsicHeight(), malteseIcon.getIntrinsicWidth()/2, 0);
 		Rect panamasR = new Rect(-panamasIcon.getIntrinsicWidth()/2, -panamasIcon.getIntrinsicHeight(), panamasIcon.getIntrinsicWidth()/2, 0);
 		Rect rileysR = new Rect(-rileysIcon.getIntrinsicWidth()/2, -rileysIcon.getIntrinsicHeight(), rileysIcon.getIntrinsicWidth()/2, 0);
 		Rect gradR = new Rect(-gradIcon.getIntrinsicWidth()/2, -gradIcon.getIntrinsicHeight(), gradIcon.getIntrinsicWidth()/2, 0);
 		Rect townR = new Rect(-townIcon.getIntrinsicWidth(), -townIcon.getIntrinsicHeight(), 0, 0);
 		Rect ubarR = new Rect(-ubarIcon.getIntrinsicWidth()/2, -ubarIcon.getIntrinsicHeight(), ubarIcon.getIntrinsicWidth()/2, 0);
 		
 		BarItem overlaybanshee = new BarItem(bansheePoint, "Banshee", "I'm at Banshee!", bansheeIcon, bansheeR);
 		BarItem overlaybeach = new BarItem(beachPoint, "Beach", "I'm at The Beach!", beachIcon, beachR);
 		BarItem overlaybellas = new BarItem(bellasPoint, "Bellas", "I'm at Bellas", bellasIcon, bellasR);
 		BarItem overlaycrazy = new BarItem(crazyPoint, "Crazy Horse", "I'm at The Crazy Horse!", crazyIcon, crazyR);
 		BarItem overlaydown = new BarItem(downPoint, "Down Lo", "I'm at the DownLo!", downIcon, downR);
 		BarItem overlayduffys = new BarItem(duffysPoint, "Duffys", "I'm at Duffy's!", duffysIcon, duffysR);
 		BarItem overlayjoes = new BarItem(joesPoint, "Joes", "I'm at Joe's!", joesIcon, joesR);
 		BarItem overlaylasalles = new BarItem(lasallesPoint, "Lasalles", "I'm at Lasalles!", lasallesIcon, lasallesR);
 		BarItem overlaylost = new BarItem(lostPoint, "Lost On Main", "I'm at Lost On Main!", lostIcon, lostR);
 		BarItem overlaybear = new BarItem(bearPoint, "Madison Bear Garden", "I'm at The Bear", bearIcon, bearR);
 		BarItem overlaymaltese = new BarItem(maltesePoint, "Maltese", "I'm at the Maltese!", malteseIcon, malteseR);
 		BarItem overlaypanamas = new BarItem(panamasPoint, "Panamas", "I'm at Panamas!", panamasIcon, panamasR);
 		BarItem overlayrileys = new BarItem(rileysPoint, "Rileys", "I'm at Rileys!", rileysIcon, rileysR);
 		BarItem overlaygrad = new BarItem(gradPoint, "The Grad", "I'm at The Graduate!", gradIcon, gradR);
 		BarItem overlaytown = new BarItem(townPoint, "Town Lounge", "I'm at Town Lounge!", townIcon, townR);
 		BarItem overlayubar = new BarItem(ubarPoint, "University Bar", "I'm at the U-Bar!", ubarIcon, ubarR);
 		
 		//This creates a new bar overlay which is a list of overlay items. a BatItem extends overlayitem.
 		BarOverlay barsOverlay = new BarOverlay(cnoIco, this, dayOfWeek);
 		
 		//this is adding all of the custom bar overlayitems that i created previously
 		barsOverlay.addOverlay(overlaybanshee);
 		barsOverlay.addOverlay(overlaybeach);
 		barsOverlay.addOverlay(overlaybellas);
 		barsOverlay.addOverlay(overlaycrazy);
 		barsOverlay.addOverlay(overlaydown);
 		barsOverlay.addOverlay(overlayduffys);
 		barsOverlay.addOverlay(overlayjoes);
 		barsOverlay.addOverlay(overlaylasalles);
 		barsOverlay.addOverlay(overlaylost);
 		barsOverlay.addOverlay(overlaybear);
 		barsOverlay.addOverlay(overlaymaltese);
 		barsOverlay.addOverlay(overlaypanamas);
 		barsOverlay.addOverlay(overlayrileys);
 		barsOverlay.addOverlay(overlaygrad);
 		barsOverlay.addOverlay(overlaytown);
 		barsOverlay.addOverlay(overlayubar);
 		
 		map.getOverlays().add(barsOverlay);
 }
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	private void initMapView() {
 		map = (MapView) findViewById(R.id.map);
 		control = map.getController();
 		map.setBuiltInZoomControls(true);
 		map.setSatellite(false);
 		map.invalidate();
 	}
 	
 	private void initMyLocation() {
 		
 		final MyLocationOverlay overlay = new MyLocationOverlay(this, map);
 		overlay.enableMyLocation();
 		overlay.runOnFirstFix(new Runnable() {
 			public void run() {
 				control.setZoom(18);
 				control.setCenter(centerP);
 			}
 		});
 		map.getOverlays().add(overlay);
 	}
 	private void initDatabase() {
 		MyDBHelper myDbHelper = new MyDBHelper(CNOMapView.this);
         try {
         	myDbHelper.createDataBase();
             Toast.makeText(CNOMapView.this, "Success in creation", Toast.LENGTH_SHORT).show();
 
         }
         catch (IOException ioe) {
             throw new Error("Unable to create database");
         }
 	}
 	public void onClick(View v) {
 		Intent i;
 		Toast.makeText(CNOMapView.this, "Herp", Toast.LENGTH_SHORT).show();
 		switch(v.getId()) {
 		
 			case R.id.bacbutton:
 				Toast.makeText(CNOMapView.this, "Derp", Toast.LENGTH_SHORT).show();
 				i = new Intent(this, BAC.class);
 				startActivity(i);
 				break;
 			case R.id.barlistbutton:
 				i = new Intent(this, BarListView.class);
 				startActivity(i);
 				break;
 			case R.id.cacbutton:
 				cacButton = new CallACab(this);
 				break;
 			case R.id.facebookbutton:
 				authorizeFacebook();
 				break;
 		}
 	}
 	
 	public void authorizeFacebook() {
 		facebook.authorize(this, new DialogListener() {
            @Override
             public void onComplete(Bundle values) {}
 
            @Override
             public void onCancel() {}
 
			@Override
 			public void onFacebookError(FacebookError e) {
 				// TODO Auto-generated method stub
 				
 			}
 
			@Override
 			public void onError(DialogError e) {
 				// TODO Auto-generated method stub
 				
 			}
         });
 	}
 	
 	@Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         facebook.authorizeCallback(requestCode, resultCode, data);
     }
 }
