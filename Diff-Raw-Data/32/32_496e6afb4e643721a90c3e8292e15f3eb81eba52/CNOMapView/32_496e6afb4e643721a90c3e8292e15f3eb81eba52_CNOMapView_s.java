 package com.NightOutApps.chiconightout;
 
 import java.io.IOException;
 import java.util.Calendar;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Toast;
 
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
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.map);
 		
 		View barListButton = findViewById(R.id.barlistbutton);
         barListButton.setOnClickListener(this);
         
 		initMapView();
 		initMyLocation();
 		initDatabase();
 	
 
 		
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
 		
 		
		Drawable bansheeIcon = this.getResources().getDrawable(R.drawable.banshee_icontest);
		Drawable beachIcon = this.getResources().getDrawable(R.drawable.beach_icontest);
		Drawable bellasIcon = this.getResources().getDrawable(R.drawable.bellas_icontest);
		Drawable crazyIcon = this.getResources().getDrawable(R.drawable.crazy_horse_icontest);
		Drawable downIcon = this.getResources().getDrawable(R.drawable.down_lo_icontest);
		Drawable duffysIcon = this.getResources().getDrawable(R.drawable.duffys_icontest);
		Drawable joesIcon = this.getResources().getDrawable(R.drawable.joes_icontest);
		Drawable lasallesIcon = this.getResources().getDrawable(R.drawable.lasalles_icontest);
		Drawable lostIcon = this.getResources().getDrawable(R.drawable.lost_on_main_icontest);
		Drawable bearIcon = this.getResources().getDrawable(R.drawable.madison_bear_icontest);
		Drawable malteseIcon = this.getResources().getDrawable(R.drawable.maltese_icontest);
		Drawable panamasIcon = this.getResources().getDrawable(R.drawable.panamas_icontest);
		Drawable rileysIcon = this.getResources().getDrawable(R.drawable.rileys_icontest);
		Drawable gradIcon = this.getResources().getDrawable(R.drawable.the_grad_icontest);
		Drawable townIcon = this.getResources().getDrawable(R.drawable.town_lounge_icontest);
		Drawable ubarIcon = this.getResources().getDrawable(R.drawable.u_bar_icontest);
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
 		BarItem overlaybeach = new BarItem(beachPoint, "Beach", "I'm in Mexico City!", beachIcon, beachR);
 		BarItem overlaybellas = new BarItem(bellasPoint, "Bellas", "I'm in Mexico City!", bellasIcon, bellasR);
 		BarItem overlaycrazy = new BarItem(crazyPoint, "Crazy Horse", "I'm in Mexico City!", crazyIcon, crazyR);
 		BarItem overlaydown = new BarItem(downPoint, "Down Lo", "I'm in Mexico City!", downIcon, downR);
 		BarItem overlayduffys = new BarItem(duffysPoint, "Duffys", "I'm in Mexico City!", duffysIcon, duffysR);
 		BarItem overlayjoes = new BarItem(joesPoint, "Joes", "I'm in Mexico City!", joesIcon, joesR);
 		BarItem overlaylasalles = new BarItem(lasallesPoint, "Lasalles", "I'm in Mexico City!", lasallesIcon, lasallesR);
 		BarItem overlaylost = new BarItem(lostPoint, "Lost On Main", "I'm in Mexico City!", lostIcon, lostR);
 		BarItem overlaybear = new BarItem(bearPoint, "Madison Bear Garden", "I'm in Mexico City!", bearIcon, bearR);
 		BarItem overlaymaltese = new BarItem(maltesePoint, "Maltese", "I'm in Mexico City!", malteseIcon, malteseR);
 		BarItem overlaypanamas = new BarItem(panamasPoint, "Panamas", "I'm in Mexico City!", panamasIcon, panamasR);
 		BarItem overlayrileys = new BarItem(rileysPoint, "Rileys", "I'm in Mexico City!", rileysIcon, rileysR);
 		BarItem overlaygrad = new BarItem(gradPoint, "The Grad", "I'm in Mexico City!", gradIcon, gradR);
 		BarItem overlaytown = new BarItem(townPoint, "Town Lounge", "I'm in Mexico City!", townIcon, townR);
 		BarItem overlayubar = new BarItem(ubarPoint, "University Bar", "I'm in Mexico City!", ubarIcon, ubarR);
 		
 		BarOverlay barsOverlay = new BarOverlay(cnoIco, this);
 		
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
 				control.setZoom(17);
 				control.animateTo(centerP);
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
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()) {
 		
 		case R.id.barlistbutton:
 			Intent i = new Intent(this, BarListView.class);
 			startActivity(i);
 			break;
 		}
 	}
 	
 
 }
