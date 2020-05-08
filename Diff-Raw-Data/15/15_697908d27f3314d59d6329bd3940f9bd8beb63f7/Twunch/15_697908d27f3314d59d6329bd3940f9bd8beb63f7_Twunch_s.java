 /**
  *	Copyright 2010 Norio bvba
  *
  *	This program is free software: you can redistribute it and/or modify
  *	it under the terms of the GNU General Public License as published by
  *	the Free Software Foundation, either version 3 of the License, or
  *	(at your option) any later version.
  *	
  *	This program is distributed in the hope that it will be useful,
  *	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *	GNU General Public License for more details.
  *	
  *	You should have received a copy of the GNU General Public License
  *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package be.norio.twunch.android.core;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TimeZone;
 
 /**
  * 
  */
 public class Twunch implements Comparable<Twunch> {
 
 	String id;
 	String title;
 	Date date;
 	String latitude;
 	String longitude;
 	String address;
 	String map;
 	String link;
 	List<String> participants = new ArrayList<String>();;
 
 	/**
 	 * @return the id
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * @return the date
 	 */
 	public Date getDate() {
 		return date;
 	}
 
 	/**
 	 * @return the latitude
 	 */
 	public String getLatitude() {
 		return latitude;
 	}
 
 	/**
 	 * @return the longitude
 	 */
 	public String getLongitude() {
 		return longitude;
 	}
 
 	/**
 	 * @return the participants
 	 */
 	public String getParticipants() {
 		StringBuilder sb = new StringBuilder();
 		for (Iterator<String> it = participants.iterator(); it.hasNext();) {
 			if (sb.length() > 0) {
 				sb.append(" ");
 			}
 			sb.append("@");
 			sb.append(it.next());
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * @param id
 	 *          the id to set
 	 */
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	/**
 	 * @param title
 	 *          the title to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	/**
 	 * @param date
 	 *          the date to set
 	 */
 	public void setDate(String date) {
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
 		sdf.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
 		try {
 			this.date = sdf.parse(date);
 		} catch (ParseException e) {
 			// Do nothing
 		}
 	}
 
 	/**
 	 * @param latitude
 	 *          the latitude to set
 	 */
 	public void setLatitude(String latitude) {
 		this.latitude = latitude;
 	}
 
 	/**
 	 * @param participants
 	 *          the articipants to set
 	 */
 	public void addParticipant(String participant) {
 		this.participants.add(participant);
 	}
 
 	/**
 	 * @return the address
 	 */
 	public String getAddress() {
 		return address;
 	}
 
 	/**
 	 * @param address
 	 *          the address to set
 	 */
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	/**
 	 * @param longitude
 	 *          the longitude to set
 	 */
 	public void setLongitude(String longitude) {
 		this.longitude = longitude;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	@Override
 	public int compareTo(Twunch another) {
 		return this.date.compareTo(another.getDate());
 	}
 
 	/**
 	 * @return the map
 	 */
 	public String getMap() {
 		return map;
 	}
 
 	/**
 	 * @param map
 	 *          the map to set
 	 */
 	public void setMap(String map) {
 		this.map = map;
 	}
 
 	/**
 	 * @return the link
 	 */
 	public String getLink() {
 		return link;
 	}
 
 	/**
 	 * @param link
 	 *          the link to set
 	 */
 	public void setLink(String link) {
 		this.link = link;
 	}
 
 }
