 /*
  * Created by Lolay, Inc.
  * Copyright 2011 Lolay, Inc. All rights reserved.
  */
 package com.lolay.android.tracker;
 
 import java.util.Collection;
 import java.util.Map;
 
 import android.content.Context;
 import android.util.Log;
 
 public class LolayMultipleTracker extends LolayBaseTracker {
 	private static final String TAG = LolayMultipleTracker.class.getSimpleName();
 	private Collection<LolayTracker> trackers;
 	
 	public LolayMultipleTracker(Collection<LolayTracker> trackers) {
 		this.trackers = trackers;
 		Log.i(TAG, String.format("Intialized with %s trackers", trackers.size()));
 	}
 	
     @Override
 	public void startSession() {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.startSession();
     	}
 	}
 	
     @Override
 	public void endSession() {
     	for (LolayTracker tracker : this.trackers) {
    		tracker.startSession();
     	}
 	}
 
     @Override
     public void startSession(Context context) {
         for (LolayTracker tracker : this.trackers) {
             tracker.startSession(context);
         }
     }
 
     @Override
     public void endSession(Context context) {
         for (LolayTracker tracker : this.trackers) {
             tracker.endSession(context);
         }
     }
 
     @Override
     public void setIdentifier(String identifier) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.setIdentifier(identifier);
     	}
     }
 
     @Override
     public void setVersion(String version) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.setVersion(version);
     	}
     }
 
     @Override
     public void setAge(int age) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.setAge(age);
     	}
     }
 
     @Override
     public void setGender(LolayTrackerGender gender) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.setGender(gender);
     	}
     }
 
     @Override
     public void setState(String state) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.setState(state);
     	}
     }
 
     @Override
     public void setZip(String zip) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.setZip(zip);
     	}
     }
 
     @Override
     public void setCampaign(String campaign) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.setCampaign(campaign);
     	}
     }
 
     @Override
     public String getTrackerId() {
     	for (LolayTracker tracker : this.trackers) {
     		String id = tracker.getTrackerId();
     		if (id != null) {
     			return id;
     		}
     	}
     	
     	return null;
     }
 
     @Override
     public String getTrackerId(Class<?> clazz) {
     	for (LolayTracker tracker : this.trackers) {
     		String id = tracker.getTrackerId(clazz);
     		if (id != null) {
     			return id;
     		}
     	}
     	
     	return null;
     }
     
     @Override
     public void setGlobalParameters(Map<Object, Object> globalParameters) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.setGlobalParameters(globalParameters);
     	}
     }
 
     @Override
     public void setGlobalParameter(Object key, Object object) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.setGlobalParameter(key, object);
     	}
     }
     
     @Override
     public void removeGlobalParameter(Object key) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.removeGlobalParameter(key);
     	}
     }
     
     @Override
     public void logEvent(String name) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.logEvent(name);
     	}
     }
 
     @Override
     public void logEventWithParams(String name, Map<Object, Object> parameters) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.logEventWithParams(name, parameters);
     	}
     }
 
     @Override
     public void logPage(String name) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.logPage(name);
     	}
     }
 
     @Override
     public void logPageWithParams(String name, Map<Object, Object> parameters) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.logPageWithParams(name, parameters);
     	}
     }
 
     @Override
     public void logException(Throwable throwable) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.logException(throwable);
     	}
     }
 
     @Override
     public void logException(String errorId, String message, Throwable throwable) {
     	for (LolayTracker tracker : this.trackers) {
     		tracker.logException(errorId, message, throwable);
     	}
     }
 }
