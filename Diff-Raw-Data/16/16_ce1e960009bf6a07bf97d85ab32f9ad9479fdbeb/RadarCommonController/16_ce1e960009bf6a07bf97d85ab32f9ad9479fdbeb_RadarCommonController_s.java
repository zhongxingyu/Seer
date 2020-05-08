 package com.tabbie.android.radar;
 
 /*
  *  RadarCommonController.java
  *
  *  Created on: July 22, 2012
  *      Author: Valeri Karpov
  *      
  *  Data structure for maintaining a collection of events with the radar feature. Events
  *  are accessible by id.
  */
 
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class RadarCommonController implements Parcelable {
 	public static final int MAX_RADAR_SELECTIONS = 3;
 	
 	private final LinkedHashMap<String, Event> events = new LinkedHashMap<String, Event>();
 	public final List<Event> eventsList = new ArrayList<Event>();
 	
 	private final LinkedHashSet<String> radarIds = new LinkedHashSet<String>();
 	public final List<Event> radar = new ArrayList<Event>();
 	
 	// Sort by # of people with event in radar, reversed
 	private static final Comparator<Event> defaultOrdering = new Comparator<Event>() {
 		public int compare(Event e1, Event e2) {
 			if (e1.radarCount > e2.radarCount) {
 				return -1;
 			} else if (e1.radarCount < e2.radarCount) {
 				return 1;
 			}
 			return 0;
 		}
 	};
 	
 	public RadarCommonController() {
 		try {
 			Event e1 = new Event(	"1",
									"Rager",
									"Get smashed, yeah!",
 									"Skyroom NYC",
									new URL("http://thechive.files.wordpress.com/2009/04/demotivated-funny-karate.jpg"),
 									40.709208,
 									-74.005864,
 									4,
 									false,
 									"11:00pm");
 			Event e2 = new Event(	"2",
									"Ginyu Force Party",
									"Dress up as Ginyu Force and show off your Recoome Boom!",
									"230 Fifth",
									new URL("http://3.bp.blogspot.com/_NQz93lR8zIY/TSlxGIsV3vI/AAAAAAAAAOI/s0AoNlr18v8/s640/230_Fifth_v2_460x285.jpg"),
 									40.744253,
 									-73.987991,
 									6,
 									true,
 									"8:00pm");
 			addEvent(e1);
 			addEvent(e2);
 			order();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public void addEvent(Event e) {
 		events.put(e.id, e);
 		eventsList.add(e);
 	}
 	
 	public void order() {
 		Collections.sort(eventsList, defaultOrdering);
 	}
 	
 	public Event getEvent(String id) {
 		return events.get(id);
 	}
 	
 	public boolean isOnRadar(Event e) {
 		return radarIds.contains(e.id);
 	}
 	
 	public boolean addToRadar(Event e) {
 		if (radarIds.contains(e.id) || radar.size() >= MAX_RADAR_SELECTIONS) {
 			return false;
 		}
 		radarIds.add(e.id);
 		radar.add(e);
 		++e.radarCount;
 		e.setOnRadar(true);
 		return true;
 	}
 	
 	public boolean removeFromRadar(Event e) {
 	  if (!radarIds.contains(e.id)) {
 	    return false;
 	  }
 	  radarIds.remove(e.id);
 	  // TODO: this is slow, improve
 	  radar.remove(e);
 	  --e.radarCount;
 	  e.setOnRadar(false);
 	  return true;
 	}
 
   @Override
   public int describeContents() {
     return 0;
   }
 
   @Override
   public void writeToParcel(Parcel dest, int flags) {
     // Technically all we need to do is write eventsList, and then reconstruct on the other side
     dest.writeTypedList(eventsList);
   }
   
   public static final Parcelable.Creator<RadarCommonController> CREATOR = new Parcelable.Creator<RadarCommonController>() {
     public RadarCommonController createFromParcel(Parcel in) {
       List<Event> events = new ArrayList<Event>();
       in.readTypedList(events, Event.CREATOR);
       RadarCommonController c = new RadarCommonController();
       c.eventsList.clear();
       c.events.clear();
       c.radar.clear();
       c.radarIds.clear();
       c.eventsList.addAll(events);
       for (Event e : events) {
         c.events.put(e.id, e);
         if (e.isOnRadar()) {
           c.radarIds.add(e.id);
           c.radar.add(e);
         }
       }
       return c;
     }
 
     public RadarCommonController[] newArray(int size) {
       return new RadarCommonController[size];
     }
 };
 }
