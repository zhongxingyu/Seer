 /**
 	This file is part of Personal Trainer.
 
     Personal Trainer is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     Personal Trainer is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
 
     (C) Copyright 2012: Daniel Kvist, Henrik Hugo, Gustaf Werlinder, Patrik Thitusson, Markus Schutzer
  */
 package se.team05.content;
 
 import java.util.ArrayList;
 
 import se.team05.overlay.CheckPoint;
 import android.content.Context;
 
 /**
 * This class contains all data related to a single route plus all the geopoints
 * and checkpoints associated with the route. It also provides several getters
 * and setters for the various instance variables. It simply acts as a model in
 * the mvc structure.
  * 
 * @author Henrik Hugo, Daniel Kvist, Gustaf Werlinder
  * 
  */
 public class Route
 {
 	public static String EXTRA_ID = "rid";
 
 	private long _id;
 	private String name;
 	private String description;
 	private int type;
 	private int timecoach;
 	private int lengthcoach;
 	private int timePassed;
 	private int calories;
 	private float totalDistance;
 	private boolean started;
 	private Context context;
 
 	private ArrayList<ParcelableGeoPoint> geoPoints;
 	private ArrayList<CheckPoint> checkPoints;
 	private CalorieCounter calorieCounter;
 
 	/**
 	 * Constructor for a route
 	 * 
 	 * @param name
 	 * @param description
	 * @param context
 	 */
 	public Route(String name, String description, Context context)
 	{
 		this(name, description, 0, -1, -1, context);
 	}
 
 	/**
 	 * Constructor for a route
 	 * 
 	 * @param name
 	 * @param description
 	 * @param type
 	 * @param timecoach
 	 * @param lengthcoach
 	 */
 	public Route(String name, String description, int type, int timecoach, int lengthcoach, Context context)
 	{
 		this(-1, name, description, type, timecoach, lengthcoach, context);
 	}
 
 	/**
 	 * Constructor for a route
 	 * 
 	 * @param _id
 	 * @param name
 	 * @param description
 	 * @param type
 	 * @param timecoach
 	 * @param lengthcoach
 	 */
 	public Route(long _id, String name, String description, int type, int timecoach, int lengthcoach, Context context)
 	{
 		this._id = _id;
 		this.name = name;
 		this.description = description;
 		this.type = type;
 		this.timecoach = timecoach;
 		this.lengthcoach = lengthcoach;
 		this.totalDistance = 0;
 		this.timePassed = 0;
 		this.started = false;
 		this.geoPoints = new ArrayList<ParcelableGeoPoint>();
 		this.checkPoints = new ArrayList<CheckPoint>();
 		this.calorieCounter = new CalorieCounter(context);
 	}
 
 	public String toString()
 	{
 		return name;
 	}
 
 	public long getId()
 	{
 		return _id;
 	}
 
 	public void setId(long id)
 	{
 		this._id = id;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	public String getDescription()
 	{
 		return description;
 	}
 
 	public void setDescription(String description)
 	{
 		this.description = description;
 	}
 
 	public int getType()
 	{
 		return type;
 	}
 
 	public void setType(int type)
 	{
 		this.type = type;
 	}
 
 	public int isTimecoach()
 	{
 		return timecoach;
 	}
 
 	public void setTimecoach(int timecoach)
 	{
 		this.timecoach = timecoach;
 	}
 
 	public int isLengthcoach()
 	{
 		return lengthcoach;
 	}
 
 	public void setLengthcoach(int lengthcoach)
 	{
 		this.lengthcoach = lengthcoach;
 	}
 
 	/**
 	 * Sets the geo points that are related to this route.
 	 * 
 	 * @param geoPoints2
 	 */
 	public void setGeoPoints(ArrayList<ParcelableGeoPoint> geoPoints)
 	{
 		this.geoPoints = geoPoints;
 	}
 
 	/**
 	 * Gets the geo points related to this route.
 	 * 
 	 * @return a list of geo points
 	 */
 	public ArrayList<ParcelableGeoPoint> getGeoPoints()
 	{
 		return geoPoints;
 	}
 
 	/**
 	 * Sets the checkpoint list of a route
 	 * 
 	 * @param checkPoints
 	 *            the checkpoints to relate to the route
 	 */
 	public void setCheckPoints(ArrayList<CheckPoint> checkPoints)
 	{
 		this.checkPoints = checkPoints;
 	}
 
 	/**
 	 * Gets the checkpoints of this route
 	 * 
 	 * @return a list of checkpoints
 	 */
 	public ArrayList<CheckPoint> getCheckPoints()
 	{
 		return checkPoints;
 	}
 
 	/**
 	 * Sets the total distance of the route. This is updated continiously when a
 	 * new route is being recorded.
 	 * 
 	 * @param totalDistance
 	 *            the total distance to set
 	 */
 	public void setTotalDistance(float totalDistance)
 	{
 		this.totalDistance = totalDistance;
 		calories = calorieCounter.updateCalories(totalDistance);
 	}
 
 	/**
 	 * Get the total distance
 	 * 
 	 * @return the total distance
 	 */
 	public float getTotalDistance()
 	{
 		return totalDistance;
 	}
 
 	/**
 	 * Sets the time passed of the route. This is updated continiously when a
 	 * new route is being recorded.
 	 * 
 	 * @param timePassed
 	 *            the time passed since starting the route
 	 */
 	public void setTimePassed(int timePassed)
 	{
 		this.timePassed = timePassed;
 	}
 
 	/**
 	 * Get the time passed.
 	 * 
 	 * @return the time passed since starting the route
 	 */
 	public int getTimePassed()
 	{
 		return timePassed;
 	}
 
 	/**
 	 * Convert the minutes and seconds to a string
 	 * 
 	 * @return a string of the time passed formatted as MM:SS
 	 */
 	public String getTimePassedAsString()
 	{
 		int timePassed = getTimePassed();
 		int seconds = timePassed % 60;
 		int minutes = timePassed / 60;
 		return String.format("%02d:%02d", minutes, seconds);
 	}
 
 	/**
 	 * Sets the started flag in the route so that we can keep track of when the
 	 * route is started
 	 * 
 	 * @param true if started
 	 */
 	public void setStarted(boolean started)
 	{
 		this.started = started;
 	}
 
 	/**
 	 * Checks to see if the route has been started or not
 	 * 
 	 * @return true if started
 	 */
 	public boolean isStarted()
 	{
 		return started;
 	}
 
 	/**
 	 * Checks if this is a new route that has not been saved yet.
 	 * 
 	 * @return true if it is a new route
 	 */
 	public boolean isNewRoute()
 	{
 		return getId() == -1;
 	}
 
 	public int getCalories()
 	{
 		return calories;
 	}
 
 }
