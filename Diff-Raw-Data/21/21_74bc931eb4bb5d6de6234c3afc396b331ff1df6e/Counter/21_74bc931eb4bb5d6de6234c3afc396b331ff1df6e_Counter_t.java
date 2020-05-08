 package com.coffeeandpower.datatiming;
 
 import java.util.ArrayList;
 import java.util.Observable;
 
 import android.os.Handler;
 import android.util.Log;
 
 import com.coffeeandpower.AppCAP;
 import com.coffeeandpower.cont.DataHolder;
 import com.coffeeandpower.cont.VenueSmart;
 import com.google.android.maps.GeoPoint;
 
 public class Counter extends Observable {
 	
 	private Integer tick = 0;
 	private DataHolder venuesWithCheckinsResponse;
 	private DataHolder nearbyVenuesResponse;
 	
 	private boolean isRunning = false;
 	private boolean delayHttp = false;	// Flag used to delay HTTP call if there is cached data
 	private boolean isFirstRun = true;
 	
 	private int numberOfCalls = 0;
 	
 	// Scheduler
 	protected Handler taskHandler = new Handler();
 
 	//CounterWorkerThread workerThread = new CounterWorkerThread();
 	public void getLastResponseReset() {
 		if(!isFirstRun)
 		{
                     delayHttp = true;
 		}
 		//The user is moving around the activities lets keep the data fresh
 		this.numberOfCalls = 0;
 		//Restart the timer
 		stop();
 		start();
 		
 		
 	}
 	
 	public Counter(Integer tick, Integer trigger) {
 		super();
 		this.tick = tick;
 		this.isFirstRun = true;
 	}
 
 	public void stop() {
 		
 		if (isRunning == true) {
 			Log.d("Counter","Removing callbacks...");
 			this.isRunning = false;
 			taskHandler.removeCallbacks(runTimer);
 			
 		}
 		else
 		{
 			Log.d("Counter","Warning: Tried to stop Counter but it was not started...");
 		}
 	}
 
 	public void start() {
 		
 		if (isRunning == false) {
 			Log.d("Counter","Posting runnable...");
 			this.isRunning = true;
 			taskHandler.removeCallbacks(runTimer);
 			taskHandler.post(runTimer);
 			
 		}
 		else
 		{
 			Log.d("Counter","Warning: Tried to start Counter when it was already running...");
 		}
 		
 	}
 	
 	public void manualTrigger() {
 		stop();
 		start();
 	}
 
 	private Runnable runTimer = new Runnable()
         {
             @Override
             public void run()
             {
         	    // We are now on the main thread, so kick off the API call in a worker thread
         	    new Thread(new Runnable() {
         		    public void run() {
         			    
         			    
         			    
         			    if (AppCAP.getUserLatLon()[0] == 0 && AppCAP.getUserLatLon()[1] == 0) {
         				    Log.d("Counter","User position is currently 0-0, skipping API calls until a position is received.");
         			    } else {                                            
                                 	    if(delayHttp)
                                 	    {
                                 		    delayHttp = false;
                                                     
                                                     @SuppressWarnings("unchecked")
                             			    ArrayList<VenueSmart> arrayVenues = (ArrayList<VenueSmart>) nearbyVenuesResponse.getObject();
                                                     Log.d("Timer","Sending notifyObservers with cached data (" + arrayVenues.size() + " nearby venues)...");
                                                     
                                                     // Send notify for nearby venues
                                                     setChanged();
                                                     
                                                     // Response objects should already be set if execution reaches this point
                                                     // but I saw a crash once where they weren't, which I can't recreate.
                                                     if (venuesWithCheckinsResponse == null) 
                                                 	    Log.e("Counter","Error! venuesWithCheckinsResponse was null but delayHttp got set...");
                                                     else if (nearbyVenuesResponse == null) 
                                                 	    Log.e("Counter","Error! nearbyVenuesResponse was null but delayHttp got set...");
                                                     else 
                                                 	    notifyObservers(new CounterData(venuesWithCheckinsResponse,nearbyVenuesResponse));
                                 	    }
                                 	    else
                                 	    {
                                 		    Log.d("Timer","Calling functions with coordinates: " + AppCAP.getUserLatLon()[0] + ", " + AppCAP.getUserLatLon()[1]);
                                 		    
                                 		    if (isFirstRun)
                         				    isFirstRun = false;
                                 		    
                                 		    venuesWithCheckinsResponse = AppCAP.getConnection().getNearestVenuesWithCheckinsToCoordinate(AppCAP.getUserLatLon());
                                 		    Log.d("Timer","Received VenuesWithCheckins: " + venuesWithCheckinsResponse.toString());
                                 		    
                                 		    //If we aren't logged in we don't want to run this http request
                                 		    //if (AppCAP.isLoggedIn()==false)
                                 		    final GeoPoint gp = new GeoPoint((int)(AppCAP.getUserLatLon()[0]*1E6), (int)(AppCAP.getUserLatLon()[1]*1E6));
                                 		    nearbyVenuesResponse = AppCAP.getConnection().getVenuesCloseToLocation(gp,20);
                                 		    Log.d("Timer","Received VenuesWithCheckins: " + venuesWithCheckinsResponse.toString());
                                 		    
                                 		    Log.d("Counter","Received Response: " + venuesWithCheckinsResponse.getResponseMessage());
                                         	    Log.d("Counter","Received Response: " + nearbyVenuesResponse.getResponseMessage());
                                         	    
                                         	    if (!venuesWithCheckinsResponse.getResponseMessage().equals("HTTP 200 OK")) {
                                         		    Log.d("Counter","Skipping notifyObservers.");
                                         	    } else {
                                         		 
                                                             Log.d("Timer","Sending notifyObservers with received data...");
                                                             
                                                             // Send notify for nearby venues
                                                             setChanged();
                                                             notifyObservers(new CounterData(venuesWithCheckinsResponse,nearbyVenuesResponse));
                                                 	    
                                         	    }                                		    
                                 	    }                                	    
         			    }
                         	    
         			    //We are going stop the timer if the user hasn't moved views in a while
         			    numberOfCalls++;
         			    //Currently 10 second interval with 20 calls so 3.3bar minutes
         			    if(numberOfCalls < 20)
         			    {
         				    Log.d("Counter","Posting runnable delayed for 10 seconds...");
         				    taskHandler.postDelayed(runTimer, tick * 1000);
         			    }
         			    else
         			    {
                                 	    Log.d("Counter","Turning off counter until user activity");
                                	    //TODO We should also turn off the GPS at this point as well
         			    }
         		    }
         	    }).start();
             }
         };
 	
 }
