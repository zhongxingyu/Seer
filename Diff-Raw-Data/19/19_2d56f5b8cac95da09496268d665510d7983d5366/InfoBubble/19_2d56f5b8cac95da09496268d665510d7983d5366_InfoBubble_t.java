 package fi.cie.chiru.servicefusionar.serviceApi;
 
 import java.util.List;
 
 import android.location.Location;
 import android.util.Log;
 
 import util.Vec;
 
 public class InfoBubble extends ServiceApplication
 {
 	private final static String LOG_TAG = "InfoBubble"; 
 	private DraggableText items[] = null;
 	private ServiceManager serviceManager;
	private final float RADIUS = 35f;
 	
 	public InfoBubble(ServiceManager serviceManager, String name)
 	{
 		super (serviceManager, name);
 		this.serviceManager = serviceManager;
 	}
 	
 	@Override
 	public void setvisible(boolean visible)
 	{
		if (this.isVisible() == visible)
			return;
		
 	    super.setvisible(visible);
 	    if (items != null)
 	    {
 		    for(int i=0; i<items.length; i++)
 		    {
 		        items[i].visible();
 		    }
 	    }
 	    
 	}
 	
 	public boolean isVisible()
 	{
 		return visible;
 	}
 	
 	public void populateItems(List<String> content, String contentManager)
 	{
 		int contentLen = content.size();
 
 		// Limit maximum visible items to 11
 		if (contentLen > 10)
 			contentLen = 10;
 		
 		items = new DraggableText[contentLen];
 		
 		Vec infoBubblePosition = new Vec(this.getPosition());
     	infoBubblePosition.add(0.0f, 7.0f, 0.0f);
     	for(int i=0; i<contentLen; i++)
     	{    		
         	DraggableText infoItem = new DraggableText(this.serviceManager);
 	        infoItem.setDragText(content.get(i));
 	        infoItem.setDragTextManager(contentManager);
 	        infoBubblePosition.add(0.0f, -1.1f, 0.0f);
     		infoItem.setPosition(infoBubblePosition);
     		infoItem.createTextComponent();
    		
     		items[i] = infoItem;
     	}
     	if (this.isAttached)
     	{
     		this.attachToCamera(true);
     	}
 	}
 	
 	@Override
 	public void servicePlaceFromLocation(Location location)
 	{
 		if (geoLocation == null)
 		{
 			geoLocation = new Location("LocationInfo");
 			if (this.getName().equals("MovieInfobubble"))
 			{
 				Log.d(LOG_TAG, "Getting geolocation from Movie service");
 				geoLocation.set(serviceManager.getApplication("MovieIcon").getGeoLocation());
 			}
 			else if (this.getName().equals("MusicInfobubble"))
 			{
 				Log.d(LOG_TAG, "Getting geolocation from Music service");
 				geoLocation.set(serviceManager.getApplication("MusicIcon").getGeoLocation());
 			}
 		}
 		
 		float[] results= new float[3];
 		// The computed distance in meters is stored in results[0].
 		// If results has length 2 or greater, the initial bearing is stored in results[1].
 		// If results has length 3 or greater, the final bearing is stored in results[2].
 		Location.distanceBetween(location.getLatitude(), location.getLongitude(), this.geoLocation.getLatitude(), this.geoLocation.getLongitude(), results);
 		
 		// Set static positions if service is within predefined range from device.
 		if (results[0] < DISTANCE_LIMIT)
 		{
 			// Recalculate positions only if InfoBubble is not already attached
 			if (this.isAttached)
 				return;
 
 			float angle = serviceManager.getSetup().getCamera().getRotation().y;
 			
 			if (this.getName().equals("MovieInfobubble"))
 				angle += 4.0f;			
 			else if (this.getName().equals("MusicInfobubble"))
 				angle += 8.0f;				
 
 			
			Vec position = this.positionFromAngle(angle, RADIUS);
 			Log.i(LOG_TAG, "position = " + position.x + ", " + position.y);
 
 			this.setPosition(position.x, 4, -position.y);
 
 			if (!this.initialized)
 			{
 				if (this.getName().equals("MovieInfobubble"))
 					serviceManager.getMovieManager().positionInitialized();
 				else if (this.getName().equals("MusicInfobubble"))
 					serviceManager.getMusicManager().positionInitialized();
 				
 				this.initialized = true;
 			}
 
 			this.attachToCamera(true);
 			
 			// Don't calculate dynamic positions.
 			return;
 		}
 		
 		// Dynamic positions for services which are too far away to be attached to the camera.
 		float bearing = (float)((360 + results[2]) % 360f);		
 		
 		Vec position = new Vec();
 		if (this.getName().equals("MovieInfobubble"))
 			bearing += 4.0f;
 		else if (this.getName().equals("MusicInfobubble"))
 			bearing += 6.0f;
 
 		Log.i(LOG_TAG, this.getName() + " position = " + position.x + ", " + position.y + " Bearing was: " + bearing);
		position.setToVec(this.positionFromAngle(bearing, RADIUS));
 		this.setPosition(position.x, 3, -position.y);
 		
 		if (!this.initialized)
 		{
 			if (this.getName().equals("MovieInfobubble"))
 				serviceManager.getMovieManager().positionInitialized();
 			else if (this.getName().equals("MusicInfobubble"))
 				serviceManager.getMusicManager().positionInitialized();
 			
 			this.initialized = true;
 		}
 
 		this.attachToCamera(false);
 	}
 	
 	@Override
 	public void attachToCamera(boolean attach)
 	{
 		super.attachToCamera(attach);
 		if (items != null)
 		{
 			for (int i = 0; i < items.length; i++)
 			{
 				items[i].attachToCamera(attach);
 			}
 		}
 	}
 
 }
