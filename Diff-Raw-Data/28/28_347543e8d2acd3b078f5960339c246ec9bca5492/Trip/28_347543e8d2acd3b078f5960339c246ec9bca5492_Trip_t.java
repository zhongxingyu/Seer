 /**
  * @contributor(s): Jaroslav Rakhmatoullin (NTNU), Jacqueline Floch (SINTEF), Rune Sætre (NTNU)
  * @version: 		0.1
  * @date:			23 May 2011
  * @revised:
  *
  * Copyright (C) 2011 UbiCompForAll Consortium (SINTEF, NTNU)
  * for the UbiCompForAll project
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  * 
  */
 
 /**
  * @description:
  *
  * 
  */
 
 package org.ubicompforall.CityExplorer.data;
 
 import java.util.HashMap;
 import java.util.ArrayList;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 final public class Trip extends IntentPassable {
 
 	/** The id of a public trip. */
 	private final int idGlobal;
 
 	/** The id of a private trip. */
 	private final int idPrivate;
 
 	/** The name of a trip. */
 	private final String label;
 
 	/** The description of a trip. */
 	private final String description;
 
 	/** A boolean deciding if this is a free trip or not. */
 	private final boolean freeTrip;
 
 	/** The list of all pois. */
 	private ArrayList<Poi> pois;
 
 	/** The fixed times of each poi in a trip. */
 	private HashMap<Poi, Time> fixedTimes;
 
 	/**
 	 * A Builder for the Trip class. 
 	 */
 	public static class Builder {
 		// required parameters
 		/** The name of a trip. */
 		private final String	label;
 
 		// default values for optional fields
 		/** The id of a public trip. */
 		private int		idGlobal	= -1;
 
 		/** The id of a private trip. */
 		private int		idPrivate	= -1;
 
 		/** The description of a trip. */
 		private String	description	= "";
 
 		/** A boolean deciding if this is a free trip or not. */
 		private boolean freeTrip = true;
 
 		/** The list of all pois. */
 		private ArrayList<Poi> pois	= new ArrayList<Poi>();
 
 		/** The fixed times of each poi in a trip. */
 		private HashMap<Poi, Time> fixedTimes = new HashMap<Poi, Time>();
 
 		/**
 		 * The required fields of the class are given as parameters to the constructor of the Builder.
 		 * @param label	A recognizable name for a trip
 		 */
 		public Builder(String label){
 			this.label = label;
 			if (this.pois != null && this.pois.isEmpty()) { pois = new ArrayList<Poi>();}
 			if (this.fixedTimes != null && this.fixedTimes.isEmpty()) { fixedTimes = new HashMap<Poi, Time>();}
 		}
 
 		/**
 		 * Populates the optional field description.
 		 *
 		 * @param d A description ofa trip.
 		 * @return The instance of the calling Builder class (returns itself).
 		 */
 		public Builder description(String d){
 			this.description = d;
 			return this;
 		}
 
 		/**
 		 * Sets wether or not the trip is a free trip.
 		 *
 		 * @param free True if the trip is free, false otherwise.
 		 * @return The builder
 		 */
 		public Builder freeTrip(boolean free){
 			this.freeTrip = free;
 			return this; 
 		}
 
 		/**
 		 * Id global.
 		 *
 		 * @param gid The global id.
 		 * @return The builder.
 		 */
 		public Builder idGlobal(int gid){
 			this.idGlobal = gid;
 			return this; 
 		}
 
 		/**
 		 * Id private.
 		 *
 		 * @param pid the pid
 		 * @return the builder
 		 */
 		public Builder idPrivate(int pid){
 			this.idPrivate = pid;
 			return this;
 		}
 
 		/**
 		 * Pois.
 		 *
 		 * @param pois The list of pois.
 		 * @return The builder
 		 */
 		public Builder pois(ArrayList<Poi> pois){
 			this.pois = pois;			
 			return this; }
 
 		/**
 		 * Fixed times.
 		 *
 		 * @param fixedTimes The fixed times.
 		 * @return The builder.
 		 */
 		public Builder fixedTimes(HashMap<Poi, Time> fixedTimes){
 			this.fixedTimes	= fixedTimes;
 			return this; 
 		}
 
 
 		/**
 		 * Calls the Trip(Builder b) constructor that returns an immutable Trip.
 		 * @return Trip A new empty Trip Object to be used anywhere in the system.
 		 */
 		public Trip build() {
 			return new Trip(this); 
 		}
 	}
 
 	/**
 	 * Construcor that takes the nested Builder as an argument.
 	 * @param b The real constructor (Builder).
 	 */
 	private Trip(Builder b){
 		this.idGlobal		= b.idGlobal;
 		this.idPrivate		= b.idPrivate;
 		this.description	= b.description;
 		this.label			= b.label;
 		this.pois			= b.pois;
 		this.freeTrip		= b.freeTrip;
 		this.fixedTimes		= b.fixedTimes;
 	}
 
 	/**
 	 * A method that allows an existing Trip to be modified.
 	 *
 	 * @return The builder
 	 */
 	public Builder modify() {
 
 		Builder temp = new Builder(this.getLabel());
 		temp.description(this.getDescription());
 		temp.freeTrip(this.isFreeTrip());
 		temp.idGlobal(this.getIdGlobal());
 		temp.idPrivate(this.getIdPrivate());
 		temp.pois(this.getPois());
 		temp.fixedTimes(this.getFixedTimes());
 		return temp;
 	}
 
 	/**
 	 * Gets the name of the trip.
 	 * @return The name of the trip.
 	 */
 	public String getLabel(){
 		return label;
 	}
 
 	/**
 	 * Gets the description of the trip.
 	 * @return The description of the trip
 	 */
 	public String getDescription(){
 		return description;
 	}
 
 	/**
 	 * Checks if it is a free trip.
 	 *
 	 * @return True if it is a free trip, false otherwise.
 	 */
 	public boolean isFreeTrip(){
 		return freeTrip;
 	}
 
 	/**
 	 * Gets the global id of the trip.
 	 *
 	 * @return The global id of the trip
 	 */
 	public int getIdGlobal(){
 		return idGlobal;
 	}
 
 	/**
 	 * Gets the private id of the trip.
 	 *
 	 * @return The private id of the trip.
 	 */
 	public int getIdPrivate(){
 		return idPrivate;
 	}
 
 	/**
 	 * Gets the pois in the trip.
 	 *
 	 * @return The pois in the trip-
 	 */
 	public ArrayList<Poi> getPois(){
 		return pois;
 	}
 
 	/**
 	 * Gets the fixed times.
 	 *
 	 * @return The fixed times.
 	 */
 	public HashMap<Poi, Time> getFixedTimes(){
 		return fixedTimes;
 	}
 
 	/**
 	 * Gets the poi at specified index.
 	 *
 	 * @param idx The index that you will get the poi at.
 	 * @return The poi at the specified index.
 	 */
 	public Poi getPoiAt(int idx){
		if (pois.size()>0){
			return pois.get(idx);
		}else{
			return null;
		}//if idx is valid (e.g. 0>0?)
	}//getPoiAt(idx)
 
 	/**
 	 * Adds a poi to the trip. Remember to add it to the database.
 	 *
 	 * @param newPoi The poi you want to add to the trip
 	 */
 	public void addPoi(Poi newPoi){
 		pois.add(newPoi);
 	}
 
 	/**
 	 * Insert poi in trip in specified index.
 	 *
 	 * @param idx The index where you want to add the poi to the trip.
 	 * @param newPoi The poi you want to add to the trip.
 	 */
 	public void insertPoi(int idx, Poi newPoi){
 		pois.add(idx, newPoi);
 	}
 
 	/**
 	 * Removes the poi from the trip. Remember to remove it from the database.
 	 *
 	 * @param idx The index where you want to remove the poi.
 	 */
 	public void removePoi(int idx){
 		if(fixedTimes.containsKey(pois.get(idx))){
 			fixedTimes.remove(pois.get(idx));
 		}
 		pois.remove(idx);
 	}
 
 	/**
 	 * Sets the time of a pois in the trip.
 	 *
 	 * @param Hashmap of the time and the corresponding times
 	 */
 	public void setTime(HashMap<Poi, Time> times){
 		fixedTimes = times;
 	}
 	
 	/**
 	 * Sets the times of the poi in the trip.
 	 *
 	 * @param poi The poi you want to add times to.
 	 * @param time The time you want to add to the poi.
 	 */
 	public void setTime(Poi poi, Time time){
 		fixedTimes.put(poi, time);
 	}
 
 	/**
 	 * Clear times previously created.
 	 */
 	public void clearTimes(){
 		fixedTimes.clear();
 	}
 
 	/**
 	 * Checks if the poi list is empty.
 	 *
 	 * @return True if the poi list is empty, false otherwise
 	 */
 	public boolean isEmpty(){
 		return pois.isEmpty();
 	}
 
 
 	@Override
 	public boolean equals(Object obj){
 		Trip cmprnd = null;
 
 		if (obj instanceof Trip){
 			cmprnd = (Trip) obj;
 		}
 
 		if (obj == null && this == null) {
 			return true;
 		}
 
 		boolean same = 
 			cmprnd		!= null							&&
 			idGlobal 	== 		cmprnd.getIdGlobal()	&&
 			idPrivate	==		cmprnd.getIdPrivate()	&&
 			label.		equals(cmprnd.getLabel())		&&
 			description.equals(cmprnd.getDescription())	&&
 			freeTrip 	== cmprnd.isFreeTrip()			&&
 			pois.		equals(cmprnd.getPois())
 			;
 		return same;
 	}
 
 	@Override
 	public String toString(){
 
 		StringBuilder sb = new StringBuilder();
 		sb.append("global("		+idGlobal+				")");
 		sb.append("private("	+idPrivate+				")");
 		sb.append("label("		+label+					")");
 		sb.append("desription("	+description+			")");
 		sb.append("freeTrip("	+freeTrip+				")");
 		sb.append("pois("		+pois.toString()+		")");
 		return sb.toString();
 	}
 
 	/**
 	 * Instantiates a new parcelable trip.
 	 *
 	 * @param in The parcelable you want to instantiate.
 	 */
 	public Trip(Parcel in) {
 
 		idGlobal 	= in.readInt();
 		idPrivate 	= in.readInt();
 
 		label		= in.readString();
 		description	= in.readString();
 		freeTrip	= (1==in.readInt());
 
 		int numOfFixedTimes = in.readInt();
 		fixedTimes = new HashMap<Poi, Time>();
 		for (int i = 0; i < numOfFixedTimes; i++)
 		{
 			fixedTimes.put(
 					new Poi(in), 
 					new Time(in.readInt(), 
 							in.readInt()));
 		}
 
 		pois = new ArrayList<Poi>();
 		while(in.dataAvail() > 0)
 			pois.add(new Poi(in));
 	}
 
 	@Override
 	public void writeToParcel(Parcel out, int flags) {
 
 		out.writeInt(idGlobal);
 		out.writeInt(idPrivate);
 		out.writeString(label);
 		out.writeString(description);
 		out.writeInt(freeTrip? 1 : 0);
 
 		out.writeInt(fixedTimes.size());
 		for (Poi poi : fixedTimes.keySet())
 		{
 			poi.writeToParcel(out, flags);
 			out.writeInt(fixedTimes.get(poi).hour);
 			out.writeInt(fixedTimes.get(poi).minute);
 		}
 
 		for (Poi poi : pois)
 		{
 			poi.writeToParcel(out, flags);
 		}
 	}
 
 	/*
 	 * Parcelable.Creator Interface
 	 *
 	 */
 	/** The Constant CREATOR. Parcelable.Creator Interface. */
 	public static final	Parcelable.Creator<Trip> CREATOR = new Parcelable.Creator<Trip>() {
 		public Trip createFromParcel(Parcel in) {
 			return new Trip(in);
 		}
 
 		public Trip[] newArray(int size) {
 			return new Trip[size];
 		}
 	};
 }
