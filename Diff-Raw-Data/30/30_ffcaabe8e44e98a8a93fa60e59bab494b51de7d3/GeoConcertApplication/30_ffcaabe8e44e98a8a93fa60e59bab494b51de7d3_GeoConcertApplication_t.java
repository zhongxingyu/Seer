 package no.uib.info331.geomusic;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.StreamCorruptedException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import android.app.Application;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationManager;
 import android.util.Log;
 import de.umass.lastfm.Event;
 import de.umass.lastfm.PaginatedResult;
 
 public class GeoConcertApplication extends Application {
 	// private static GeoConcertApplication singleton;
 
 	/* list of the events */
 	private ArrayList<Event> newevents;
 	private LocationManager locationManager;
 	private Location location;
 
 	/**
 	 * Private constructor. Is not to be called.
 	 */
 	public GeoConcertApplication() {
 		super();
 		this.newevents = new ArrayList<Event>();
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		locationManager = (LocationManager) this
 				.getSystemService(Context.LOCATION_SERVICE);
 	}
 
 	/**
 	 * 
 	 * @param result
 	 */
 	public void setEvents(ArrayList<Event> result) {
 		this.newevents = result;
 	}
 
 
 	/**
 	 * 
 	 * @param result
 	 */
	public void createEvents(PaginatedResult<Event> result) {
 		if (result != null) {
 			/* Get iterator for search inside the list of the events */
 			Iterator i = result.iterator();
 			while (i.hasNext()) {
 				this.newevents.add((Event) i.next());
 			}
 		}
 	}
 	
 	public ArrayList<Event> getEvents()
 	{
 		return this.newevents;
 	}
 	
 	/**
 	 * @return the locationManager
 	 */
 	public LocationManager getLocationManager() {
 		return locationManager;
 	}
 
 	/**
 	 * @param locationManager
 	 *            the locationManager to set
 	 */
 	public void setLocationManager(LocationManager locationManager) {
 		this.locationManager = locationManager;
 	}
 
 	/**
 	 * Function for get a specific event from the list
 	 * 
 	 * @param id
 	 * @return the searched event
 	 */
 	public Event findEvent(int id) {
 		if (this.newevents != null) {
 			/* Get iterator for search inside the list of the events */
 			
 			for(int i=0; i<this.newevents.size(); i++)
 			{
 				if (this.newevents.get(i).getId() == id)
 					return this.newevents.get(i);
 			}
 		}
 		return null;
 	}
 
 	public Location getLocation() {
 		return location;
 	}
 
 	public void setLocation(Location location) {
 		this.location = location;
 	}
 
 	public void writeObject() {
 		String FILENAME = "events";
 
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		try {
 
 			ObjectOutput out = new ObjectOutputStream(bos);
 			
 			out.writeObject(this.newevents);
 			byte[] buf = bos.toByteArray();
 
 			FileOutputStream fos = this.openFileOutput(FILENAME,
 					Context.MODE_PRIVATE);
 			fos.write(buf);
 			fos.close();
 		} catch (IOException ioe) {
 			Log.e("serializeObject", "error", ioe);
 
 		}
 		File f = this.getDir(FILENAME, 0);
 		Log.v("FILE", f.getName());
 	}
 
 	public void readObject() {
 		String FILENAME = "events";
 
 		FileInputStream fis;
 		try {
 			fis = openFileInput(FILENAME);
 			ObjectInputStream ois = new ObjectInputStream(fis);
 			this.newevents = (ArrayList<Event>) ois.readObject();
 		} catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (StreamCorruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		;
 
 	}
 
 
 }
