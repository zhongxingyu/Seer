 package emotodrome.data;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.*;
 
 import emotodrome.mesh.Vec3;
 import emotodrome.user.User;
 import android.app.Activity;
 import android.os.Environment;
 import android.provider.Settings.Secure;
 
 public class Backend{
 	
 	public static final int MOVE_FORWARD = 0;
 	public static final int MOVE_BACKWARD = 1;
 	public static final int MOVE_LEFT = 2;
 	public static final int MOVE_RIGHT = 3;
 	private final String ICE_DATA_FILE = "iceData.dat";
 	
 	private Server server;
 	private MyLocationManager locationManager;
 	private ImageManager imageManager[];
 	private Activity activity;
 	private File storage;
 	private final String IMAGE_NAME = "map.png";
 	private final String IMAGE_NAME_SWAP = "swap.png";
 	private String imageNames[] = new String[9];
 	private boolean isSwap[] = new boolean[9];
 	private boolean isDownloaded[];
 	private int zoom;
 	private final int INIT_ZOOM = 10;
 	private final int IMAGE_WIDTH = 256;
 	private final int IMAGE_HEIGHT = 256;
 	public double longitude;
 	public double latitude;
 	public double top;
 	public double bottom;
 	public double left;
 	public double right;
 	private Thread serverListener = null;
 
 	public Backend(Activity activity, MyLocationManager locationManager) {
 		this.activity = activity;
 		this.locationManager = locationManager;
 		System.out.println("Initial Lat: " + locationManager.getLatitude());
 		System.out.println("Initial Long: " + locationManager.getLongitude());
 
 		//if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
 		storage = Environment.getExternalStorageDirectory();
 //		else
 //			storage = Environment.getDataDirectory();
 
 
 		zoom = INIT_ZOOM;
 		//download map to sdcard or data directory
 		imageManager = new ImageManager[9];
 		isDownloaded = new boolean[9];
 		
 		for(int i=0; i<9; i++){
 			imageManager[i] = new ImageManager(storage);
 			isSwap[i] = false;
 			isDownloaded[i] = false;
 			imageNames[i] = IMAGE_NAME;
 		}
 		
 		longitude = locationManager.getLongitude();
 		latitude = locationManager.getLatitude();
 		findLatitudeBounds();
 		findLongitudeBounds();
 		
 		downloadImage(top, left, zoom, "0"+IMAGE_NAME, 0);
 		downloadImage(top, longitude, zoom, "1"+IMAGE_NAME, 1);
 		downloadImage(top, right, zoom, "2"+IMAGE_NAME, 2);
 
 		downloadImage(latitude, left, zoom, "3"+IMAGE_NAME, 3);
 		downloadImage(latitude, longitude, zoom, "4"+IMAGE_NAME, 4);
 		downloadImage(latitude, right, zoom, "5"+IMAGE_NAME, 5);
 
 		downloadImage(bottom, left, zoom, "6"+IMAGE_NAME, 6);
 		downloadImage(bottom, longitude, zoom, "7"+IMAGE_NAME, 7);
 		downloadImage(bottom, right, zoom, "8"+IMAGE_NAME, 8);
 
 		System.out.println("Return from image manager download.");
 
 		server = new Server(getAndroidID(), locationManager.getLongitude(),
 				locationManager.getLatitude(), zoom);
 		File iceData = activity.getFileStreamPath(ICE_DATA_FILE);
 //		if (!iceData.exists()){
 			server.sendToServer("d,0,0,0\n");
 			try {
 				if (server.receiveIceDataFile(activity.openFileOutput(ICE_DATA_FILE, 0)) < 0){
 					iceData.delete();
 				}
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 		sendUserLatLon(latitude, longitude + 180);
 //		}
 	}
 	
 	public HashMap<Vec3, Float> processIceData(){
 		FileInputStream data;
 		try {
 			data = activity.openFileInput(ICE_DATA_FILE);
 			Scanner s = new Scanner(data);
 			HashMap<Vec3, Float> icePercents = new HashMap<Vec3, Float>();
 			while (s.hasNextLine()){
 				String line = s.nextLine();
 				String[] values = line.split(",");
 				int lat = Integer.valueOf(values[0]);
 				int lon = Integer.valueOf(values[1]);
 				float icePercentage = Float.valueOf(values[2]);
 				if (icePercentage > 0){
 					icePercents.put(new Vec3(lon, 0, lat), icePercentage);
 				}
 				System.out.println(lon + ", " + lat + " processed");
 			}
 			return icePercents;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private void findLatitudeBounds() {
 		top = Mercator.getTopFromLat(latitude, zoom);
 		bottom = Mercator.getBottomFromLat(latitude, zoom);
 	}
 	private void findLongitudeBounds(){
 		right = Mercator.getRightFromLng(longitude, zoom);
 		left = Mercator.getLeftFromLng(longitude, zoom);
 	}
 
 	//
 	//	public void getSurroundingPlots(boolean isFirstCall) {
 	//		String fileName = IMAGE_NAME_SWAP;
 	//		if(isSwap || isFirstCall)
 	//			fileName = IMAGE_NAME;
 	//		
 	//		//launch threads for all of them
 	//		
 	//		
 	//	}
 
 
 
 	//	public void getSurroundingPlots(boolean isFirstCall) {
 	//		double longitude = locationManager.getLongitude();
 	//		double latitude = locationManager.getLatitude();
 	//		double top = Mercator.getTopFromLat(latitude, zoom);
 	//		double bottom = Mercator.getBottomFromLat(latitude, zoom);
 	//		double right = Mercator.getRightFromLng(longitude, zoom);
 	//		double left = Mercator.getLeftFromLng(longitude, zoom);
 	//		
 	//		String fileName = IMAGE_NAME_SWAP;
 	//		if(isSwap || isFirstCall)
 	//			fileName = IMAGE_NAME;
 	//		
 	//		downloadImage(top, left, zoom, "0"+fileName);
 	//		downloadImage(top, longitude, zoom, "1"+fileName);
 	//		downloadImage(top, right, zoom, "2"+fileName);
 	//		
 	//		downloadImage(latitude, left, zoom, "3"+fileName);
 	//		downloadImage(latitude, longitude, zoom, "4"+fileName);
 	//		downloadImage(latitude, right, zoom, "5"+fileName);
 	//		
 	//		downloadImage(bottom, left, zoom, "6"+fileName);
 	//		downloadImage(bottom, longitude, zoom, "7"+fileName);
 	//		downloadImage(bottom, right, zoom, "8"+fileName);
 	//	}
 
 /*
 	public void downloadImagesNonThreaded() {
 		
 		String fileName = IMAGE_NAME_SWAP;
 		if(isSwap)
 			fileName = IMAGE_NAME;
 
 		downloadImage(top, left, zoom, "0"+fileName, 0);
 		downloadImage(top, longitude, zoom, "1"+fileName, 1);
 		downloadImage(top, right, zoom, "2"+fileName, 2);
 
 		downloadImage(latitude, left, zoom, "3"+fileName, 3);
 		downloadImage(latitude, longitude, zoom, "4"+fileName, 4);
 		downloadImage(latitude, right, zoom, "5"+fileName, 5);
 
 		downloadImage(bottom, left, zoom, "6"+fileName, 6);
 		downloadImage(bottom, longitude, zoom, "7"+fileName, 7);
 		downloadImage(bottom, right, zoom, "8"+fileName, 8);
 		switchBuffer();
 	}
 */
 	public void changeZoom() {
 		zoom++;
 		if(zoom == 18)
 			zoom = 2;
 		System.out.println("Zoom is now " + zoom);
 	}
 
 	private void getImageAtIndex(int index) {
 		//		String fileName = IMAGE_NAME_SWAP;
 		//		if(isSwap)
 		//			fileName = IMAGE_NAME;
 		String fileName = getAlternateImageName(index);
 
 		switch(index) {
 		case 0:
 			downloadImage(top, left, zoom, "0"+fileName, 0); break;
 		case 1:
 			downloadImage(top, longitude, zoom, "1"+fileName, 1); break;
 		case 2:
 			downloadImage(top, right, zoom, "2"+fileName, 2); break;
 		case 3:
 			downloadImage(latitude, left, zoom, "3"+fileName, 3); break;
 		case 4:
 			downloadImage(latitude, longitude, zoom, "4"+fileName, 4); break;
 		case 5:
 			downloadImage(latitude, right, zoom, "5"+fileName, 5); break;
 		case 6:
 			downloadImage(bottom, left, zoom, "6"+fileName, 6); break;
 		case 7:
 			downloadImage(bottom, longitude, zoom, "7"+fileName, 7); break;
 		case 8:
 			downloadImage(bottom, right, zoom, "8"+fileName, 8); break;
 		}
 		switchBuffer(index);
 	}
 
 	//http://maps.google.com/maps/api/staticmap?center=
 	//"&size="+IMAGE_WIDTH+"x"+IMAGE_HEIGHT+"&maptype=satellite&key=MyGoogleMapsAPIKey&sensor=true"
 	private void downloadImage(double latitude, double longitude, int zoom, String fileName, int index) {
 		imageManager[index].DownloadFromUrl("http://darrenlincheng.com/showmap.php?lat="+latitude+"&long="+longitude+"&zoom="+zoom, fileName);
 	}
 
 	public void switchBuffer(int index) {
 		if (isSwap[index])
 			imageNames[index] = IMAGE_NAME;
 		else
 			imageNames[index] = IMAGE_NAME_SWAP;
 		isSwap[index] = !isSwap[index];
 	}
 /*
 	protected class ImageDownload extends AsyncTask<Integer, Void, Void> {
 		private int index;
 		@Override
 		protected Void doInBackground(Integer... params) {
 			index = params[0]; // 0 if first call, 1 otherwise
 
 			System.out.println("Start Backend Image " + index + " Download");
 			
 			getImageAtIndex(index);
 
 			return null;
 		}
 		@Override
 		protected void onProgressUpdate(Void... params) { }
 		@Override
 		protected void onPostExecute(Void params) {
 			isDownloaded[index] = true;
 			System.out.println("Finish Backend Image " + index + " Download");
 		}
 	}
 
 	public void spawnImageDownload(int index) {
 		isDownloaded[index] = false;
 		new ImageDownload().execute(index);
 		while(!isDownloaded[index]) {
 			try { Thread.sleep(100); }
 			catch (InterruptedException e) { e.printStackTrace(); }
 		}
 		System.out.println("Returning From Backend Image " + index + " Download");
 	}
 */
 
 
 
 
 
 
 	//
 	//	public boolean getIsSwap() {
 	//		return isSwap;
 	//	}
 
 
 	public String getAlternateImageName(int index) {
 		if(imageNames[index].equals(IMAGE_NAME))
 			return IMAGE_NAME_SWAP;
 		return IMAGE_NAME;
 	}
 
 	public String getImageName(int index) {
 		return imageNames[index];
 	}
 
 	public String getImagePath() {
 		return storage.getAbsolutePath();
 	}
 
 	public double getLongitude() {
 		return locationManager.getLongitude();
 	}
 
 	public double getLatitude() {
 		return locationManager.getLatitude();
 	}
 	
 	public void queryServer() {
 		server.runServerLoop(locationManager.getLongitude(), locationManager.getLatitude());
 	}
 
 	public String getServerResponse() {
 		return server.getServerResponse();
 	}
 
 
 	private String getAndroidID() {
 		//Retrieve Android Unique ID
 		String android_id = Secure.getString(activity.getContentResolver(), Secure.ANDROID_ID); 
 		if (android_id == null) {
 			android_id = UUID.randomUUID().toString();
 			System.out.println("Running In Emulator. Spoofed ID: " + android_id);
 		}
 		else {
 			System.out.println("Android Device ID: " + android_id);
 		}
 		return android_id;
 	}
 
 	public void updateAvatarLocation(int moveForward) {
 		
 		switch (moveForward){
 		
 		case MOVE_FORWARD: 
 			latitude = Mercator.getTopFromLat(latitude, zoom);
 			findLatitudeBounds();
 			getImageAtIndex(0);
 			getImageAtIndex(1);
 			getImageAtIndex(2);
 			break;
 		case MOVE_BACKWARD:
 			latitude = Mercator.getBottomFromLat(latitude, zoom);
 			findLatitudeBounds();
 			getImageAtIndex(6);
 			getImageAtIndex(7);
 			getImageAtIndex(8);
 			break;
 		case MOVE_LEFT:
 			longitude = Mercator.getLeftFromLng(longitude, zoom);
 			findLongitudeBounds();
 			getImageAtIndex(0);
 			getImageAtIndex(3);
 			getImageAtIndex(6);
 			break;
 		case MOVE_RIGHT: 
 			longitude = Mercator.getRightFromLng(longitude, zoom);
 			findLongitudeBounds();
 			getImageAtIndex(2);
 			getImageAtIndex(5);
 			getImageAtIndex(8);
 			break;
 		
 		}
 	}
 
 	public void updateUserLocation(Vec3 v){
 		server.sendToServer("m," + v.x + "," + v.y + "," + v.z + "\n");
 //		System.out.println("sent " + v.x + "," + v.y + "," + v.z);
 	}
 	
 	public void sendUserLatLon(double latitude, double longitude){
 		server.sendToServer("l," + latitude + "," + longitude + ",0\n");
 		System.out.println("sent lat,lon: " + latitude + "," + longitude);
 	}
 	
 	public void listenUserUpdates(HashMap<Integer, User> users){
 		serverListener = new Thread(server.new ServerListener(users));
 		serverListener.start();
 	}
 	
 	public Queue<LocationValuePair> getIceToAdd(){
 		return server.getAddQueue();
 	}
 	
 	public Queue<Vec3> getIceToRemove(){
 		return server.getRemQueue();
 	}
 
 	public void closeConnections() throws IOException, InterruptedException {
		server.sendToServer("q,0,0,0\n");
		server.close();
 		System.out.println("server closed");
 	}
 	
 	public boolean checkConnected(){
 		return server.connected;
 	}
 
 }
