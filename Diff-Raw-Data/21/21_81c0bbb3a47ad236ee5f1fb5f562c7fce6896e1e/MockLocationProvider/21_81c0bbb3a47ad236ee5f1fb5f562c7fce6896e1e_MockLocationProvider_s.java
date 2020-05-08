 package it.unipd.testbase.location;
 
 import it.unipd.testbase.eventdispatcher.EventDispatcher;
 import it.unipd.testbase.eventdispatcher.event.location.PositionsTerminatedEvent;
 import it.unipd.testbase.helper.DebugLogger;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationManager;
 
 public class MockLocationProvider {
 	protected static final String TAG = "it.unipd.testbase";
 	private DebugLogger logger = new DebugLogger(MockLocationProvider.class);
 	private boolean isSetup = false;
 	private int counter;
 	private int peersNumber;
 	private LocationManager manager;
 	private boolean firstExec = true;
 	public String name = "MockProvider";
 	public boolean requiresNetwork = false;
 	public boolean requiresSatellite = false;
 	public boolean requiresCell = false;
 	public boolean hasMonetaryCost = false;
 	public boolean supportsAltitude = false;
 	public boolean supportsSpeed = false;
 	public boolean supportsBearing = true;
 	public int powerRequirement = 0;
 	public int accuracy = 2;
 	private float bearing;
 	
 	private List<String> lines;
 	int currentIndex = 1;
 
 	/**
 	 * Positions file
 	 */
 	private BufferedReader file;
 
 	public MockLocationProvider(LocationManager manager, Context context) {
 		logger.d("Registering provider: "+name);
 		this.manager = manager;
 		// Horrible Workaround: When shutting app from application manager (long press home+swipe) TestBaseActivity.onDestroy and, 
 		// consequently, doUnbindService get called, but LocationService.onDestroy doesn't for some reason, leaving 
 		// mockup-provider registered within the system and causing crash on next startup (Runtime Exception).
 		if(manager.getProvider(name)!=null) {
 			manager.removeTestProvider(name);
 		}
 		manager.addTestProvider(name, requiresNetwork, requiresSatellite, requiresCell, hasMonetaryCost, 
 				supportsAltitude, supportsSpeed, supportsBearing, powerRequirement, accuracy);
 		manager.setTestProviderEnabled(name, true);
 		
 		
 		try {
 			file = new BufferedReader(new InputStreamReader(context.getAssets().open("mock_positions.txt")));
 			lines = new ArrayList<String>();
 			String strLine;
 			//Read File Line By Line
 			while ((strLine = file.readLine()) != null)   {
 			// Print the content on the console
 				lines.add(strLine);
 			}
 			
 			bearing = Float.parseFloat(lines.get(currentIndex));
 			currentIndex++;
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void remove() {
 		manager.removeTestProvider(name);
 	}
 
 	public void updateLocation() {
 			if(firstExec){
 				currentIndex = currentIndex + counter;
 				firstExec = false;
 			} else {
 				currentIndex = currentIndex + peersNumber;
 			}
 			
 			if(lines.size() <= currentIndex){
 				logger.d("End of file reached");
 				// Trigger an event to communicate the end of positions
				firstExec = true;
				// Restart 
				currentIndex = 1;
 				EventDispatcher.getInstance().triggerEvent(new PositionsTerminatedEvent());
 				return;
 			}
 			logger.d("Current position found in line "+(currentIndex+1)+": "+lines.get(currentIndex));
 			String[] positions = lines.get(currentIndex).split(",");
 			Location location = new Location(name);
 			location.setLatitude(Double.valueOf(positions[0]));
 			location.setLongitude(Double.valueOf(positions[1]));
 			location.setTime(System.currentTimeMillis());
 			location.setBearing(bearing);
 			logger.d("NEW LOCATION: "+location.getBearing()+"; "+location.getLatitude()+"; "+location.getLongitude());
 			logger.d("Current position found in car queue is "+(currentIndex+1));
 			manager.setTestProviderLocation(name, location);
 	}
 
 	public void setup(int counter, int peersNumber) {
 		isSetup = true;
 		logger.d("File counter: "+counter+"; peers number: "+peersNumber);
 		this.counter = counter;
 		this.peersNumber = peersNumber;
 	}
 	
 	public boolean isSetup() {
 		return isSetup;
 	}
 }
