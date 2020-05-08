 package com.km2team.syriush.service;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import static com.km2team.syriush.util.Algoritm.*;
 
 import android.content.Context;
 import android.location.Location;
 import android.widget.Toast;
 
 import com.km2team.syriush.database.*;
 public class LoggingLocationListener extends DebugLocationListener {
 
 	
 	Location savedLocation;
 	
 	Location checkpoint;
 	
 	Database db;
 	Context context;
 	
 	int routeNumber;
 	boolean routeJustStarted = false;
 	boolean recording = false;
 	
 	List<Location> list = new LinkedList<Location>();
 	
 	public LoggingLocationListener(Context context) {
 		this.context = context;
 		db = DatabaseFactory.getDatabase(context);
 	}
 	
 	@Override
 	public void onLocationChanged(Location arg0) {
 	
 		
 		super.onLocationChanged(arg0);
 		
 		/* TODO it's wrong - getTime() is unpredictable */
 		//if (Math.abs(checkpoint.getTime() - lastLocation.getTime()) > 1000) {
 			/* TODO save point, with lower priority */
 			/* XXX XXX XXX XXX */
 		//	db.newPoint(lastLocation);
 		//	checkpoint = lastLocation;
 			
 		//}
 		
 		list.add(arg0);
 		
 	}
 	
 	private synchronized void checkRoute(Location location) throws DatabaseException {
 		if (routeJustStarted) {
 			routeNumber = db.startNewRoute(new Point(location.getLatitude(), location.getLongitude()));
 			routeJustStarted = false;
 		}
 	}
 	
 	private void saveList(String note) throws DatabaseException {
 		RamerDouglasPeucker(list, 0.1);
 		Point[] plist = new Point[list.size()];
 		int i = 0;
 		for (Location l : list) {
 			//TODO should be lower priority
 			//TODO also .....
 			plist[i++] = new Point(l);
 		}
 		plist[i-1] = new Point(note,plist[i-1]);
 		db.appendToRoute(routeNumber, plist);
 		list.clear();
 	}
 	
 	public void savePoint(String note) throws DatabaseException {
 		if (recording) {
 			checkRoute(lastLocation);
 			list.add(lastLocation);
 			saveList(note);
 			list.add(lastLocation);
 			GPSService.showText("saved checkpoint");
 		}
 	}
 	
 	public void savePoint() throws DatabaseException {
 		/* save point and wait for a note */
 		checkRoute(lastLocation);
 		if (recording) {
 			savedLocation = lastLocation;
 		}
 	}
 	
 	public void addNote(String note) throws DatabaseException {
 		/* TODO add note to saved location */
 		saveList(note);
 		list.add(lastLocation);
 		GPSService.showText("saved checkpoint");
 	}
 	
 	public synchronized void beginRoute() {
 		routeJustStarted = true;
 	}
 	
 	public synchronized void endRoute(String name) {
 		recording = false;
 		
 	}
 
 }
