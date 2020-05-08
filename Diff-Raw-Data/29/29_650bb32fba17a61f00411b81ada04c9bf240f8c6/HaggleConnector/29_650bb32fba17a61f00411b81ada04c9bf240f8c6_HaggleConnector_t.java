 package com.datakom.POIObjects;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.haggle.Attribute;
 import org.haggle.DataObject;
 import org.haggle.EventHandler;
 import org.haggle.Handle;
 import org.haggle.Node;
 import org.haggle.DataObject.DataObjectException;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Bitmap.CompressFormat;
 import android.util.Log;
 
 import com.datakom.GpsLocation;
 import com.google.android.maps.GeoPoint;
 
 
 /**
  * @author aa a
  * 
  * Haggle controller implementing interface
  */
 public class HaggleConnector implements EventHandler {
 	private static final String HAGGLE_TAG = "HagglePOI"; 
 	public static final String STORAGE_PATH = "/sdcard/HagglePOI";
 	public static final String SEARCH_TITLE = "SEARCH_TITLE_KEY";
 	public static final String COORDINATES_REF = "CORDINATES_REF";
 	public static final String SHOW_TRACE = "true";
 	
 	/* used for fetching from DataObject in haggle*/
 	public static final String PIC = "My_Picture";
 	public static final String MD5 = "md5";
 	public static final String TYPE = "Type";
 	public static final String NAME = "Name";
 	public static final String DESC = "Desc";
 	public static final String RATING = "Rating";
 	public static final String C_LAT = "Create_Latitude";
 	public static final String C_LON = "Create_Longitude";
 	public static final String EX_COORD = "Exchange_Coordinates";
 	/* Format for exchange coordinates are lat#Lon*/
 
 	
 	private Handle hh;
 	
 	private static final int STATUS_OK = 0;
 	private static final int STATUS_REG_FAILED = -2;
 	private static final int STATUS_SPAWN_DAEMON_FAILED = -3;
 	
 	private static  final int NUM_RETRIES = 2;
 	
 
 	/* keeping track of what neighbors exist*/
 	private Node[] neighbors = null;
 
 	/* collection for keeping  track of POIObjects recieved from haggle
 	 * this could be avoided if we traverse the haggle.db and generate XML-objects from
 	 * xmlhdr and parse values into a POIObject, though due to time limimations this design was choosen.*/
 	private HaggleContainer haggleContainer =  HaggleContainer.getInstance(); 
 	
 	//Singleton instance
 	private static HaggleConnector uniqueInstance;
 		
 	//Context instance from Activity
 	private Context context = null;
 	
 	public void setContext(Context context) {
 		this.context = context;
 	}
 	/* Such that several Activities in Front end can share the same HaggleConnector object*/
 	public static synchronized HaggleConnector getInstance() {
 		if (uniqueInstance == null) {
 			uniqueInstance = new HaggleConnector();
 			uniqueInstance.initHaggle();
 		}
 		return uniqueInstance;
 	}
 	
 	/* this could be private later on */
 	public Handle getHaggleHandle() {
 		if (hh == null) {
 			Log.e(getClass().getSimpleName(), "Haggle Handle is null!");
 		}
 		
 		return hh;
 	}
 	
 	private int initHaggle() {
 		if (hh != null)
 			return STATUS_OK; 
 
 		Log.d(getClass().getSimpleName(), "Trying to Spawn haggle daemon");
 		
 		if (!Handle.spawnDaemon()) {
 			Log.d(getClass().getSimpleName(), "Spawning failed");
 			return STATUS_SPAWN_DAEMON_FAILED;
 		}
 		
 		long pid = Handle.getDaemonPid(); 
 		Log.d(getClass().getSimpleName(), "Haggle daemon pid is: " + pid);
 
 		
 		for (int tries = 0; tries < NUM_RETRIES; tries++) {
 			try {
 				hh = new Handle(HAGGLE_TAG);
 				
 				hh.registerEventInterest(EVENT_NEIGHBOR_UPDATE, this);
 				hh.registerEventInterest(EVENT_NEW_DATAOBJECT, this);
 				hh.registerEventInterest(EVENT_INTEREST_LIST_UPDATE, this);
 				hh.registerEventInterest(EVENT_HAGGLE_SHUTDOWN, this);
 				
 				hh.eventLoopRunAsync();
 				hh.getApplicationInterestsAsync();
 				
 				Log.d(getClass().getSimpleName(), "Haggle event loop started");
 				registerInterests();
 				
 				return STATUS_OK;
 				 
 			} catch (Handle.RegistrationFailedException e) {
 				Log.e(getClass().getSimpleName(), "Registration failed: " + e.getMessage());
 				
 				Handle.unregister(HAGGLE_TAG);
 			}
 		}
 
 		Log.e(getClass().getSimpleName(), "Registration failed, after retries");
 		return STATUS_REG_FAILED;
 	}
 	
 	/* drop current haggle connection? */
 	public synchronized void finiHaggle() {
 		if (hh != null) {
 			hh.eventLoopStop();
 			hh.dispose();
 			hh = null;
 		}
 	}
 	
 	public int shutdownHaggle() {
 		if (hh != null) {
 			unregisterInterests();
 			return hh.shutdown();
 		}
 		return 1;
 	}
 	
 	@Override
 	public void onInterestListUpdate(Attribute[] arr) {
 		Log.d(getClass().getSimpleName() + ":onInterestListUpdate", "got new interests, size: " +arr.length);
 		for (Attribute a : arr) {
 			Log.d(getClass().getSimpleName(), "Attr: " + a.getName() + ", value:" + a.getValue());
 		}
 	}
 
 	@Override
 	public void onNeighborUpdate(Node[] newNeighbors) {
 		Log.d(getClass().getSimpleName(), "onNewNeighbours");
 		
 		if (newNeighbors == null) {
 			Log.e(getClass().getSimpleName() + ":NeighborUpdate", "Neighbors null");
 		}
 		
 		/* updating neighborArray */
 		if (newNeighbors.length == 0) {
 			neighbors = null;
 			return;
 		}
 		neighbors = newNeighbors;
 		
 		for (Node n : newNeighbors) {
 			Log.d(getClass().getSimpleName() + ":NeighborUpdate", n.getName());
 		}
 	}
 	
 	@Override
 	public void onNewDataObject(DataObject dObj) { 
 		Log.d(getClass().getSimpleName() + ":onNewDataObject", "callback occured");
 		
 		String filepath = dObj.getFilePath();
 		/* if there is a random Haggle callback, we disregard :-) */
 		if (filepath == null || filepath.length() == 0) {
 			Log.d(getClass().getSimpleName() + ":onNewDataObject", "Filepath is empty, disregarding dObj");
 			return;
 		}
 		
 		Attribute[] all = dObj.getAttributes();
 		
 		//case 2 - newly published dObj is re-presented again
 		/* also used to prevent cycles as we re-publish Dataobjects with additional coordinates where exchange occured
 		 * as we keep track of what we have published on application level this hinderes haggle from bouncing messages between
 		 * nodes */
 		//don't modify this data		
 		for (POIObject poi : haggleContainer.getAllPOIObjects()) {
 			for (Attribute a : all) {
 				if (a.getName().compareTo("md5") == 0 && a.getValue().compareTo(poi.getMd5()) == 0) {
 					if (neighbors != null && neighbors.length > 0) {
 						Log.d(getClass().getSimpleName() + ":onNewDataObject", "got my own published message back, preventing cycle");
 						return;
 					}
 					Log.d(getClass().getSimpleName() + ":onNewDataObject", "got newly pushed object");
 					return; 
 				}
 			}
 		}
 		
 		if (neighbors != null && neighbors.length > 0) {
 			Log.d(getClass().getSimpleName() + ":onNewDataObject", "Case 3 - Got new data from neighbor!");
 			//case 3 - neighbors published data
 			 
 			/* transfering picture from haggle to android sdcard */
 			String base64_pic = dObj.getAttribute(PIC, 0).getValue();
 			Bitmap bitmap = convertBase64StringToBitmap(base64_pic);
 //			byte[] arr = new byte[102400];			
 //			dObj.getThumbnail(arr);
 //			int size = (int) dObj.getThumbnailSize();
 //			Bitmap bitmap = BitmapFactory.decodeByteArray(arr, 0, size);
 			try {
 				File dir = new File(STORAGE_PATH);
 				dir.mkdirs(); //creating directory if missing
 				
 				FileOutputStream fos = new FileOutputStream(STORAGE_PATH + "/" + dObj.getFileName());
 				bitmap.compress(CompressFormat.JPEG, 100, fos);
 				fos.flush();
 				fos.close();
 			} catch (FileNotFoundException e) {
 				Log.e(getClass().getSimpleName() + ":onNewData", "FileNotFoundException -  while trying to write down picture to disk");
 			} catch (IOException e) {
 				Log.e(getClass().getSimpleName() + ":onNewData", "IOException on outputstream");				
 			}
 			
 			/* adding tracing data */
 			GeoPoint point = GpsLocation.getInstance(null).getCurrentPoint();
 			POIObject poi = null;
 			/* if GPS hasn't been initilized */
 			if (point == null) {
 				//cannot add trace data if we don't have any GPS.
 				poi = parseDataObject(dObj);
 			} else {
 				poi = parseDataObject(dObj, point);
 			}
 			/* after parsing is done, we remove current dataObject and re-publish it
 			 * this also includes adding poi to haggleContainer */
 			getHaggleHandle().deleteDataObject(dObj);
 			pushPOIObject(poi);
 		} else {
 			//case 1 - published material from previous run is re-presented again
 			//don't modify any of this data 
 			Log.d(getClass().getSimpleName() + ":onNewDataObject", "case 1 - re-loading data from haggle to container");
 			haggleContainer.add(parseDataObject(dObj));	
 		}
 	}
 
 	/* the same as ParseDataObject with 1 argument, with the difference
 	 * that it adds GeoPoint p to the coordsExchange arrayList
 	 * used when we tag the dObjs */
 	private POIObject parseDataObject(DataObject dObj, GeoPoint p) {
 		POIObject poi = parseDataObject(dObj);
 		poi.addExchangeCoords(p);
 		return poi;
 	}
 	/* parses out all attributes for POIObject from DataObject */
 	private POIObject parseDataObject(DataObject dObj) {
 		Log.d(getClass().getSimpleName() + ":parseDataObject", "Parsing DataObject");
 		if (dObj == null || dObj.getFileName() == null || dObj.getFileName().length() == 0) {
 			Log.e(getClass().getSimpleName() + ":parseDataObject", "dObj is null or fileName is not right");
 			return null;
 		}
 		
 		if (dObj.getAttributes() == null || dObj.getAttributes().length == 0) {
 			Log.e(getClass().getSimpleName() + ":parseDataObject", "dObj lacks attributes");
 			return null;
 		}
 		
 		int type = Integer.parseInt(dObj.getAttribute(TYPE, 0).getValue());
 		String picPath = dObj.getFileName();
 		double rating = Double.parseDouble(dObj.getAttribute(RATING, 0).getValue());
 		String name = dObj.getAttribute(NAME, 0).getValue();
 		String desc = dObj.getAttribute(DESC, 0).getValue();
 		int create_latitude = Integer.parseInt(dObj.getAttribute(C_LAT, 0).getValue());
 		int create_longitude = Integer.parseInt(dObj.getAttribute(C_LON, 0).getValue());
 		String md5 = dObj.getAttribute(MD5, 0).getValue();
 		ArrayList<GeoPoint> coordsExchange = new ArrayList<GeoPoint>();
 		
 		Attribute[] attributes = dObj.getAttributes();
 		/* due to the reason haggle doesn't put attributes in order we have to 
 		 * combine for exchange coordinates due to the fact that they can be arbitrary many */
 		for (Attribute a : attributes) {
 			if (a.getName().compareTo(EX_COORD) == 0) {
 				String both = a.getValue();
 				int separator = both.indexOf("#");
 				int latitude = Integer.parseInt(both.substring(0, separator));
 				int longitude = Integer.parseInt(both.substring(separator + 1, both.length()));
 				GeoPoint p = new GeoPoint(latitude, longitude);
 				coordsExchange.add(p);
 			}
 		}
 		return new POIObject(type, picPath, rating, name, desc, create_latitude, create_longitude, md5, coordsExchange);
 	}
 	
 	@Override
 	public void onShutdown(int reason) {
 		Log.e(getClass().getSimpleName(), "Haggle Shutdown, reason: " + reason);
 	}
 
 	//@Override
 	public ArrayList<POIObject> getAllObjects() {
 		return haggleContainer.getAllPOIObjects();
 	}
 	
 	public ArrayList<String> getAllObjectNames(){
 		return haggleContainer.getAllPOIObjectNames();
 	}
 	
 	public ArrayList<GeoPoint> getAllObjectPoints(){
         return haggleContainer.getAllPOIObjectGeoPoints();
 	}
 	public ArrayList<GeoPoint> getAllPOITraces(String name){
         return haggleContainer.getAllPOITraces(name);
 	}
 	public int countObjects(){
 		return haggleContainer.countObject();
 	}
 	public GeoPoint getPoint(String name){
 		return haggleContainer.getPoint(name);
 	}
 	public ArrayList<POIObject> getPOIObjectsByName(String name) {
 		return haggleContainer.search(name);
 	}
 
 	private void registerInterests() {
 		List<String> col = ObjectTypes.getAsList();
 		for (String c : col) {
 			Attribute a = new Attribute("Type", c, 1);
 			int status = getHaggleHandle().registerInterest(a);
 			if (status != 0)
 				Log.d(getClass().getSimpleName(), "Registered Interest!: " + c);
 			else
 				Log.e(getClass().getSimpleName(), "Failed to register Interest! status: " + status);
 		}
 	}
 	
 	private void unregisterInterests() {
 		List<String> col = ObjectTypes.getAsList();
 		for (String c : col) {
 			Attribute a = new Attribute("Type", c, 1);
 			int status = getHaggleHandle().unregisterInterest(a);
 			if (status != 0)
 				Log.d(getClass().getSimpleName(), "Unregistered Interest!: " + c);
 			else
 				Log.e(getClass().getSimpleName(), "Failed to unregister Interest! status: " + status);
 		}
 	}
 	
 	public Bitmap convertBase64StringToBitmap(String  base64_imagedata) {
 		byte[] imagedata = null;
 		try {
 			imagedata = Base64.decode(base64_imagedata);
 		} catch (IOException e) {
 			Log.e(getClass().getSimpleName(), "Cannot decode base64");
 		}
 		Bitmap result = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
 		return result;
 	 }
 	
 	public static Bitmap scaleImage(String filepath, int width) {
 		BitmapFactory.Options opts = new BitmapFactory.Options();
 
     	opts.inJustDecodeBounds = true;
     	BitmapFactory.decodeFile(filepath, opts); 
     	double ratio = opts.outWidth / width;
     	opts.inSampleSize = (int)ratio;
     	opts.inJustDecodeBounds = false;
     	return BitmapFactory.decodeFile(filepath, opts);
 	}
 	//@Override
 	public int pushPOIObject(POIObject o) {
 
 		try {
 			DataObject dObj;
 			dObj = new DataObject(STORAGE_PATH + "/" + o.getPicPath());
 			
 			dObj.addAttribute(MD5, o.getMd5(), 1); 
 			dObj.addAttribute(TYPE, Integer.toString(o.getType()), 1);
 			dObj.addAttribute(NAME, o.getName(), 1);
 			dObj.addAttribute(DESC, o.getDescription(), 1);
 			dObj.addAttribute(RATING, Double.toString(o.getRating()), 1);
 			dObj.addAttribute(C_LAT, Integer.toString(o.getPoint().getLatitudeE6()), 1);
 			dObj.addAttribute(C_LON, Integer.toString(o.getPoint().getLongitudeE6()), 1);
 
			for(GeoPoint p : o.getCoordsExchange()) {
				dObj.addAttribute(EX_COORD, p.getLatitudeE6() + "#" + p.getLongitudeE6(), 1);
			}
			
 			/* scaling is done due to the fact that we push our pictures as an attribute, 
 			 * probable limitations in our usage of haggle cannot manage arbitrary long attributes, 
 			 * atleast it seems so */
 			Bitmap bmp = scaleImage(dObj.getFilePath(), 100); 
 			ByteArrayOutputStream os = new ByteArrayOutputStream();
 			bmp.compress(CompressFormat.JPEG, 75, os);
 			byte[] arr = os.toByteArray();
 			//hacking around haggle
 			String base64_pic = Base64.encodeBytes(arr);
 			dObj.addAttribute(PIC, base64_pic, 1); 
			
 			getHaggleHandle().publishDataObject(dObj);
 		} catch (DataObjectException e) {
 			Log.e(getClass().getSimpleName(), "Could not create object for: " + o.getName());
 			Log.e(getClass().getSimpleName(), e.getMessage());
 			
 			return -1;
 		}
 		//on successful publish we store the POIObject
 		haggleContainer.add(o);
 		return 0;
 	}
 	
 }
