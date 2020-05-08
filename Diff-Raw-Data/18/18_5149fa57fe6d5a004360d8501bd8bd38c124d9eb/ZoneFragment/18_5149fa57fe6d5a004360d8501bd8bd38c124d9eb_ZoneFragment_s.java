 package com.ecn.urbapp.fragments;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 import android.app.Fragment;
 import android.content.res.Resources;
 import android.graphics.Matrix;
 import android.graphics.Point;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.LayerDrawable;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.ecn.urbapp.R;
 import com.ecn.urbapp.activities.MainActivity;
 import com.ecn.urbapp.db.Element;
 import com.ecn.urbapp.db.PixelGeom;
 import com.ecn.urbapp.dialogs.TopologyExceptionDialogFragment;
 import com.ecn.urbapp.dialogs.UnionDialogFragment;
 import com.ecn.urbapp.utils.ConvertGeom;
 import com.ecn.urbapp.utils.GetId;
 import com.ecn.urbapp.zones.BitmapLoader;
 import com.ecn.urbapp.zones.DrawZoneView;
 import com.ecn.urbapp.zones.UtilCharacteristicsZone;
 import com.ecn.urbapp.zones.Zone;
 import com.vividsolutions.jts.geom.MultiPolygon;
 import com.vividsolutions.jts.geom.TopologyException;
 import com.vividsolutions.jts.io.ParseException;
 
 /**
  * @author	COHENDET Sébastien
  * 			DAVID Nicolas
  * 			GUILBART Gabriel
  * 			PALOMINOS Sylvain
  * 			PARTY Jules
  * 			RAMBEAU Merwan
  * 
  * ZoneFragment class
  * 
  * This is the fragment used to define the different zones.
  * 			
  */
 
 public class ZoneFragment extends Fragment implements OnClickListener, OnTouchListener{
 	/**
 	 * Field defining the radius tolerance on touch
 	 */
 	private int TOUCH_RADIUS_TOLERANCE = 30;//only for catching points in edit mode
 	
 	/**
 	 * Constant field defining the reference height to correct the size of point for the zone creation
 	 */
 	private final int REFERENCE_HEIGHT = 600;
 	
 	/**
 	 * Constant field defining the reference width to correct the size of point for the zone creation
 	 */
 	private final int REFERENCE_WIDTH = 1200;
 	
 	/**
 	 * Constant field defining the reference time length to force selection
 	 */
 	private final int REFERENCE_TIME = 150;
 	
 	/**
 	 * Field defining the actual sate of definition of zones
 	 */
 	public static int state;
 
 	/**
 	 * Button cancel
 	 */
 	private Button cancel;
 	/**
 	 * Button fusion
 	 */
 	//private Button fusion;
 	/**
 	 * Button back
 	 */
 	private Button back;
 	/**
 	 * Button validate
 	 */
 	private Button validate;
 	/**
 	 * Button delete
 	 */
 	private Button delete;
 
 	/**
 	 * Image displayed
 	 */
 	private static ImageView myImage;
 	
 	/**
 	 * Matrix for displaying
 	 */
 	private Matrix matrix;
 	
 	/**
 	 * Temporary zone for edition
 	 */
 	private Zone zoneCache ; 
 	
 
 	/**
 	 * Temporary pixelGeom for edition
 	 */
 	public static PixelGeom geomCache;
 	
 	/**
 	 * Zone selected
 	 */
 	private Zone zone;
 	
 	/**
 	 * Point selected
 	 */
 	private Point selected;
 	public static Element elementTemp;
 	private DrawZoneView drawzoneview;
 	private int imageHeight; private int imageWidth;
 	
 	/**
 	 * Constant value
 	 */
 	public static final int IMAGE_CREATION = 2;
 	/**
 	 * Constant value
 	 */
 	public static final int IMAGE_EDITION = 3;
 	/**
 	 * Constant value
 	 */
 	public static final int IMAGE_SELECTION= 1;
 	
 	/**
 	 * Point selection indicator, works in both creation and edition modes
 	 */
 	private boolean POINT_SELECTED = false;
 	
 	private int moving;
 
 	//private SetCharactFragment scf;
 	//private RecapCharactFragment rcf;
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 	
 
 	@Override
 	public void onClick(View v) {
 		Log.d("Move","Etat:"+state);
 		switch(state){
 		
 		case IMAGE_SELECTION:
 			break;
 		case IMAGE_CREATION:
 			switch(v.getId()){
 			case R.id.zone_button_back:
 				if(!zone.back()){
 					Toast.makeText(getActivity(), R.string.no_back, Toast.LENGTH_SHORT).show();
 				}
 				refreshDisplay();
 				break;
 			case R.id.zone_button_cancel:
 				//scf.resetAffichage();
 				state = IMAGE_SELECTION;
 				exitAction();
 				break;
 			case R.id.zone_button_delete:
 				if(POINT_SELECTED){
 					if (!zone.deletePoint(selected)){
 						Toast.makeText(getActivity(), R.string.point_deleting_impossible, Toast.LENGTH_SHORT).show();
 					}
 					selected.set(0,0);//no selected point anymore
 					refreshDisplay();
 					delete.setEnabled(false);
 					POINT_SELECTED = false;
 				}
 				break;
 			/*case R.id.zone_button_fusion:
 				UnionDialogFragment summarydialog = new UnionDialogFragment();
 				summarydialog.show(getFragmentManager(), "UnionDialogFragment");
 				break;*/
 			case R.id.zone_button_validate:
 				validateCreation();//it's also possible to create a zone by looping polygon, use this method !
 				break;
 			}
 			break;
 		case IMAGE_EDITION:
 			switch(v.getId()){
 			case R.id.zone_button_delete:
 				if(POINT_SELECTED){
 					if (!zone.deletePoint(selected)){
 						Toast.makeText(getActivity(), "Impossible de supprimer ce point", Toast.LENGTH_SHORT).show();
 					}
 					selected.set(0,0);//no selected point anymore
 					refreshDisplay();
 					delete.setEnabled(false);
 					POINT_SELECTED = false;
 				}
 				else{
 					int pos;
 					for(pos=0; pos<MainActivity.pixelGeom.size(); pos++){
 						if(MainActivity.pixelGeom.get(pos).getPixelGeomId()==geomCache.getPixelGeomId()){
 							for(int i=0; i<MainActivity.element.size(); i++){
 								if(MainActivity.element.get(i).getPixelGeom_id()==MainActivity.pixelGeom.get(pos).getPixelGeomId()){
 									MainActivity.element.remove(i);
 									//scf.resetAffichage();
 									break;
 								}
 							}
 							MainActivity.pixelGeom.remove(pos);
 							break;
 						}
 					}
 					state = IMAGE_SELECTION;
 					exitAction();
 				}
 				break;
 			case R.id.zone_button_back:
 				if(!zone.back()){
 					Toast.makeText(getActivity(), R.string.no_back, Toast.LENGTH_SHORT).show();
 				}
 	            refreshDisplay();
 				break;
 			case R.id.zone_button_cancel:
 				//scf.resetAffichage();
 	            exitAction();
 				state = IMAGE_SELECTION;
 				break;
 			/*case R.id.zone_button_fusion:
 				UnionDialogFragment summarydialog = new UnionDialogFragment();
 				summarydialog.show(getFragmentManager(), "UnionDialogFragment");
 				break;*/
 			case R.id.zone_button_validate:
 				if(!zone.getPoints().isEmpty()){
 					//scf.validation();
 					ArrayList<PixelGeom> lpg = new ArrayList<PixelGeom>();
 					for (PixelGeom pg : MainActivity.pixelGeom) {
 						lpg.add(pg);
 					}
 					ArrayList<Element> le = new ArrayList<Element>();
 					for (Element elt : MainActivity.element) {
 						le.add(elt);
 					}
 					try {
 						//MainActivity.zones.remove(zoneCache); //delete original
 						MainActivity.pixelGeom.remove(geomCache);
 						PixelGeom pg = new PixelGeom();
 						pg.setPixelGeom_the_geom(zone.getPolygon().toText());
 						UtilCharacteristicsZone.addInMainActivityZones(pg, null);
 						exitAction();
 						zone.clearBacks();//remove list of actions backs
 					} catch(TopologyException e) {
 						MainActivity.pixelGeom = lpg;
 						MainActivity.element = le;
 						TopologyExceptionDialogFragment diag = new TopologyExceptionDialogFragment();
 						diag.show(getFragmentManager(), "TopologyExceptionDialogFragment");
 					} catch (ParseException e) {
 						e.printStackTrace();
 					}
 				}
 				for(PixelGeom pg : MainActivity.pixelGeom){
 					pg.selected=false;
 				}
 				state = IMAGE_SELECTION;
 				exitAction();
 				break;
 			}
 			break;
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		View v = inflater.inflate(R.layout.layout_zone, null);
 
 		back = (Button) v.findViewById(R.id.zone_button_back);
 		//fusion = (Button) v.findViewById(R.id.zone_button_fusion);
 		cancel = (Button) v.findViewById(R.id.zone_button_cancel);
 		validate = (Button) v.findViewById(R.id.zone_button_validate);
 		delete = (Button) v.findViewById(R.id.zone_button_delete);
 
 		back.setOnClickListener(this);
 		cancel.setOnClickListener(this);
 		validate.setOnClickListener(this);
 		delete.setOnClickListener(this);
 		//fusion.setOnClickListener(this);
 		
 		validate.setEnabled(false);
 		back.setEnabled(false);
 		cancel.setEnabled(false);
 		//fusion.setEnabled(false);
 		delete.setEnabled(false);
 
 		zone = new Zone(); zoneCache = new Zone(); selected = new Point(0,0); 
 		
 		zone = new Zone(); selected = new Point(0,0); 
 		myImage = (ImageView) v.findViewById(R.id.image_zone);
 		
 		drawzoneview = new DrawZoneView(zone, selected) ;
 
 		DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
 
 		Drawable[] drawables = {
 			new BitmapDrawable(
 				getResources(),
 				BitmapLoader.decodeSampledBitmapFromFile(
 						Environment.getExternalStorageDirectory()+"/featureapp/"+MainActivity.photo.getPhoto_url(), metrics.widthPixels, metrics.heightPixels - 174)), drawzoneview
 		};//TODO 174 corresponds to menu bar + buttons bar. Calculate this value ! Maybe by charging a small picture before to know ImageView size
 
 				
 		myImage.setImageDrawable(new LayerDrawable(drawables));
 		myImage.setOnTouchListener(this);
 
 		//scf = (SetCharactFragment)getFragmentManager().findFragmentById(R.id.fragmentCaract);
 		//rcf = (RecapCharactFragment)getFragmentManager().findFragmentById(R.id.fragmentRecap);
 		//rcf.setZoneFragment(this);
 		
 		
 		return v;
 	}
 	
 	@Override
 	public void onStart(){
 		super.onStart();
 		
 		state=1;
 		
 		imageHeight = myImage.getDrawable().getIntrinsicHeight(); 
 		imageWidth = myImage.getDrawable().getIntrinsicWidth();
 		
 		float ratioW =((float)REFERENCE_WIDTH/imageWidth);
 		float ratioH =((float)REFERENCE_HEIGHT/imageHeight);
 		float ratio = ratioW < ratioH ? ratioW : ratioH ;
 			
 		drawzoneview.setRatio(ratio);
 		TOUCH_RADIUS_TOLERANCE/=ratio;
 	}
 	
 	/**
 	 * Common action to do on exit (cancel or validation)
 	 */
 	private void exitAction(){
         drawzoneview.onZonePage();
 		validate.setEnabled(false);
 		back.setEnabled(false);
 		cancel.setEnabled(false);
 		//fusion.setEnabled(false);
 		delete.setEnabled(false);
 		
 		zone.setZone(new Zone());
 		selected.set(0,0);
 		drawzoneview.setIntersections(new Vector<Point>());
 		myImage.invalidate();
 		//rcf.refresh();
 	}
 	
 	private void validateCreation(){
 		//scf.validation();
 		ArrayList<PixelGeom> lpg = new ArrayList<PixelGeom>();
 		for (PixelGeom pg : MainActivity.pixelGeom) {
 			lpg.add(pg);
 		}
 		ArrayList<Element> le = new ArrayList<Element>();
 		for (Element elt : MainActivity.element) {
 			le.add(elt);
 		}
 		try {
 			PixelGeom pg = new PixelGeom();
 			pg.setPixelGeom_the_geom((new Zone(zone)).getPolygon().toText());
 			if(elementTemp.getElementType_id()==0 && elementTemp.getMaterial_id()==0){
 				UtilCharacteristicsZone.addInMainActivityZones(pg, null);
 			}
 			else{
 				UtilCharacteristicsZone.addInMainActivityZones(pg, elementTemp);
 				//MainActivity.element.add(elementTemp);
 			}
 			exitAction();
 			zone.clearBacks();
 		} catch(TopologyException e) {
 			MainActivity.pixelGeom = lpg;
 			MainActivity.element = le;
 			TopologyExceptionDialogFragment diag = new TopologyExceptionDialogFragment();
 			diag.show(getFragmentManager(), "TopologyExceptionDialogFragment");
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		state = IMAGE_SELECTION;
 		exitAction();
 	}
 	public Point getTouchedPoint(MotionEvent event){
 		float[] coord = {event.getX(),event.getY()};//get touched point coord
 
 		getMatrix();
 		matrix.mapPoints(coord);//apply matrix transformation on points coord
 		int pointX = (int)coord[0]; int pointY = (int)coord[1];
 		Log.d("Touch","x:"+pointX+" y:"+pointY);
 		if(pointX<0){
 			pointX=0;
 		}else{
 			if(pointX>imageWidth){
 				pointX=imageWidth;
 			}
 		}
 		if(pointY<0){
 			pointY=0;
 		}else{
 			if(pointY>imageHeight){
 				pointY=imageHeight;
 			}
 		}
 		return(new Point(pointX,pointY));
 	}
 	
 	public void getMatrix(){
 		matrix = new Matrix();
 		myImage.getImageMatrix().invert(matrix);
 	}
 	
 	public void refreshDisplay(){
 		Vector<Point> points = zone.getPoints();
 		if(! points.isEmpty()){
 			back.setEnabled(false);							
 			validate.setEnabled(false);
 			if(points.size()>1+1){
 				back.setEnabled(true);
 				if(points.size()>2+1){
 					validate.setEnabled(true);
 					if(points.size()>2+1){//cannot be intersections with less than 3 points but needed for refreshing displaying
 						zone.actualizePolygon();
 						MultiPolygon poly = zone.getPolygon();
 						for (int i=0; i<poly.getNumGeometries(); i++) {
 							PixelGeom pgeom = new PixelGeom();
 							pgeom.setPixelGeom_the_geom(poly.getGeometryN(i).toText());
 							Zone zone = ConvertGeom.pixelGeomToZone(pgeom);
 							Vector<Point> intersections = new Vector<Point>(zone.isSelfIntersecting(zone.getPoints()));
 							if(!intersections.isEmpty()){
 								validate.setEnabled(false);
 							}
 							drawzoneview.setIntersections(intersections);
 						}
 					}
 				}
 			}
 		}
 		myImage.invalidate();
 	}
     /*public void refreshDisplay(){
 		Vector<Point> points = zone.getPoints();
 		if(points.size()<3+1){//+1 corresponds to the ending point closing polygon 
 			validate.setEnabled(false);
 		}
 		else{
 			if(points.size()>2+1){
 				validate.setEnabled(true);
 				if(points.size()>3+1){
 					MultiPolygon poly = zone.getPolygon();
 					for (int i=0; i<poly.getNumGeometries(); i++) {
 						PixelGeom pgeom = new PixelGeom();
 						pgeom.setPixelGeom_the_geom(poly.getGeometryN(i).toText());
 						Zone zone = ConvertGeom.pixelGeomToZone(pgeom);
 						Vector<Point> intersections = new Vector<Point>(zone.isSelfIntersecting(zone.getPoints()));
 						if(!intersections.isEmpty()){
 							validate.setEnabled(false);
 						}
 						drawzoneview.setIntersections(intersections);
 					}
 				}
 			}
 		}
 		myImage.invalidate();
 	}*/
 
     /**
      * All touches handling method. Override Android API method.
      */
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 			switch(state){
 			case IMAGE_EDITION:	case IMAGE_CREATION: //app behavior in these two cases are quite the same, except some ifs
 				Log.d("Move","Moving:"+moving);
 				if(event.getAction() == MotionEvent.ACTION_DOWN && !POINT_SELECTED){
 					moving = 0;//ACTION_MOVE occurrences
 					Log.d("Move","Action Down");
 					selected.set(0, 0);
 					Point touch = getTouchedPoint(event);
 					for(Point p : zone.getPoints()){//is the touched point a normal point ?
 						float dx=Math.abs(p.x-touch.x);
 						float dy=Math.abs(p.y-touch.y);
 						if((dx*dx+dy*dy)<TOUCH_RADIUS_TOLERANCE*TOUCH_RADIUS_TOLERANCE){//10 radius tolerance
 							selected.set(p.x,p.y);
 						}
 					}
 					if(selected.x == 0 && selected.y == 0){//is the touched point a middle point ?
 						for(Point p : zone.getMiddles()){
 							float dx=Math.abs(p.x-touch.x);
 							float dy=Math.abs(p.y-touch.y);
 							if((dx*dx+dy*dy)<TOUCH_RADIUS_TOLERANCE*TOUCH_RADIUS_TOLERANCE){
 								selected.set(p.x,p.y);
 							}
 						}
 					}
 				}
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					Log.d("Move","Action Up, down time:"+(event.getEventTime()-event.getDownTime()));
 					if(POINT_SELECTED){
 						selected.set(0, 0);
 						POINT_SELECTED = false; delete.setEnabled(false);
 					}
 					else{
 						if(selected.x==0 && selected.y==0){
 							if(state == IMAGE_CREATION){
 								zone.addPoint2(getTouchedPoint(event));				
 							}else{
 								//if the user is touching a long time, and not by moving, a zone, switch zone selected 
 								if(moving < 2){
 									hasZoneSelected(event);
 								}
 							}
 						}
 						else{
 							if(state == IMAGE_CREATION && event.getEventTime()-event.getDownTime()<REFERENCE_TIME){
 								if(zone.getPoints().size()>2+1){
 									float dx=Math.abs(zone.getPoints().get(0).x-selected.x);
 									float dy=Math.abs(zone.getPoints().get(0).y-selected.y);
 									if((dx*dx+dy*dy)<TOUCH_RADIUS_TOLERANCE*TOUCH_RADIUS_TOLERANCE){//10 radius tolerance
 										validateCreation();
 										break;
 									}
 								}
 							}
 							Point touch = getTouchedPoint(event);
 							if(moving > 2){//if there is a real movement
 								zone.updatePoint(selected, touch);
 								zone.endMove(touch);
 								selected.set(0, 0);//No selected point anymore
 							}
 							else{
 								POINT_SELECTED = true; delete.setEnabled(true);
 								moving=0;
 							}
 						}
 					}
 				}
 				if (event.getAction() == MotionEvent.ACTION_MOVE && !POINT_SELECTED) {
 					moving ++;
 					Log.d("Move","Action Move");
 					if(selected.x!=0 || selected.y!=0){
 						Point touch = getTouchedPoint(event);
 						if (moving==3){
 							if (! zone.updatePoint(selected, touch)){//Is it a normal point ?
 								zone.updateMiddle(selected, touch);//If not it's a "middle" point, and it's upgraded to normal
 								zone.startMove(null);	
 							}else{
 								zone.startMove(selected);
 							}
 							selected.set(touch.x,touch.y);
 						}
 						else{
 							if(moving>3){
 								zone.updatePoint(selected, touch);
 								selected.set(touch.x,touch.y);
 							}
 						}
 					}
 				}
 				refreshDisplay();//display new point, refresh buttons' availabilities	
 				break;
 
 			case IMAGE_SELECTION:
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					if(!hasZoneSelected(event)){
 			    		getMatrix();
 						zone.addPoint2(getTouchedPoint(event));
 						refreshDisplay();
 						state = IMAGE_CREATION; drawzoneview.onCreateMode();
 						validate.setEnabled(false);
 						back.setEnabled(false);
 						cancel.setEnabled(true);
 						//fusion.setEnabled(true);
 						
 						elementTemp = new Element();
 						elementTemp.setElement_id(GetId.Element());
 						elementTemp.setPhoto_id(MainActivity.photo.getPhoto_id());
 						elementTemp.setPixelGeom_id(GetId.PixelGeom());
 						elementTemp.setGpsGeom_id(MainActivity.photo.getGpsGeom_id());
 					}
 				}
 				break;
 			}
 		return true;
 	}
 
 	@Override
 	public void onStop(){
 		super.onStop();
 	}
 	
 	public void selectGeom(long i){
 		if(state==IMAGE_CREATION){
 			//scf.resetAffichage();
 			state = IMAGE_SELECTION;
 			exitAction();
 		}
 		else if(state==IMAGE_EDITION){
 			//scf.resetAffichage();
             exitAction();
 		}
 			Zone z=null;
 			for(PixelGeom pg : MainActivity.pixelGeom){
 				z=ConvertGeom.pixelGeomToZone(pg);
 			}
 			zoneCache = z;
 			zone.setZone(z);
 
 			for(int j=0; j<MainActivity.pixelGeom.size(); j++){
 				if(MainActivity.pixelGeom.get(j).getPixelGeom_the_geom().equals(ConvertGeom.ZoneToPixelGeom(zoneCache))){
 					geomCache = MainActivity.pixelGeom.get(j);
 					MainActivity.pixelGeom.get(j).selected=true;
 				}
 			}
 			state = IMAGE_EDITION;	drawzoneview.onEditMode();
 			validate.setEnabled(true);
 			back.setEnabled(false);
 			cancel.setEnabled(true);
 			//fusion.setEnabled(true);
 			delete.setEnabled(true);
 			refreshDisplay();
 			//scf.setAffichage(geomCache);
 	}
 
 	public static ImageView getMyImage(){
 		return myImage;
 	}
 
 	/**
 	 * Add the equivalent of the attribute zone in MainActivity.pixelGeom.
 	 * Intersect this new PixelGeom wih older if the boolean in parameter is true. 
 	 * @param tryIntersect intersect the zone to add with existing zones if true
 	 */
 	public void addZone(boolean tryIntersect) {
 		PixelGeom pgeom = new PixelGeom();
		zone.closePolygon();
 		zone.actualizePolygon();
 		pgeom.setPixelGeom_the_geom(zone.getPolygon().toText());
 		try {
 			if (tryIntersect) {
 				ArrayList<PixelGeom> lpg = new ArrayList<PixelGeom>();
 				for (PixelGeom pg : MainActivity.pixelGeom) {
 					lpg.add(pg);
 				}
 				ArrayList<Element> le = new ArrayList<Element>();
 				for (Element elt : MainActivity.element) {
 					le.add(elt);
 				}
 				try {
 					UtilCharacteristicsZone.addInMainActivityZones(pgeom, null);
 					exitAction();
 				} catch(TopologyException e) {
 					MainActivity.pixelGeom = lpg;
 					MainActivity.element = le;
 					TopologyExceptionDialogFragment diag = new TopologyExceptionDialogFragment();
 					diag.show(getFragmentManager(), "TopologyExceptionDialogFragment");
 				}
 			} else {
 				UtilCharacteristicsZone.addPixelGeom(pgeom, null);
 				exitAction();
 			}
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Is user selecting a zone ?
 	 * @param event : the user action object
 	 * @return yes if zone selected, no otherwise. The selected zone is saved in a fragment's attribute.
 	 */
 	private boolean hasZoneSelected(MotionEvent event){
 		getMatrix();
 		Point touch = getTouchedPoint(event);
 		
 		boolean flag=false;
 		Zone z=null;
 		if(event.getEventTime()-event.getDownTime()>REFERENCE_TIME){
 			for(PixelGeom pg: MainActivity.pixelGeom){
 				if(ConvertGeom.pixelGeomToZone(pg).containPoint(touch)){
 					flag=true;
 					geomCache = pg;
 					z=ConvertGeom.pixelGeomToZone(pg);
 					//scf.setAffichage(pg);
 					break;
 				}
 			}
 		}
 		if(flag){
 			zoneCache = z;
 			zone.setZone(z);
 			state = IMAGE_EDITION;	drawzoneview.onEditMode();
 			validate.setEnabled(true);
 			back.setEnabled(false);
 			cancel.setEnabled(true);
 			//fusion.setEnabled(true);
 			delete.setEnabled(true);
 			refreshDisplay();
 			return true;
 			}
 		else{
 			return false;
 		}
 	}
 }
