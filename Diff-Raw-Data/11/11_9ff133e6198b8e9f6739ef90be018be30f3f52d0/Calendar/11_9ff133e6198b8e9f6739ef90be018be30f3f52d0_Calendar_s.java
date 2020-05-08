 /*
  *  Copyright 2008 University of Prince Edward Island
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package ca.upei.ic.timetable.client;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.webinit.gwt.client.KeyValueCodingObserving;
 import org.webinit.gwt.client.Observable;
 
 /**
  * This is a managed Calendar class.
  * 
  * This class is managed by WIObjects that provides KeyValueCoding and general
  * observable/observer services.
  * 
  * @author felix
  *
  */
 public abstract class Calendar implements Observable, KeyValueCodingObserving {
 	
	public final static int RESOLUTION = 14;
 
 	public final static int SUNDAY = 0;
 	public final static int MONDAY = 1;
 	public final static int TUESDAY = 2;
 	public final static int WEDNESDAY = 3;
 	public final static int THURSDAY = 4;
 	public final static int FRIDAY = 5;
 	public final static int SATURDAY = 6;
 	
 	public final static int FIVE = 5;
 	public final static int SEVEN = 7;
 
 	private int type_;
 	private Set<CalendarItem> items_;
 	
 	/**
 	 * Create a Calendar
 	 * 
 	 * setType must be called after the Calendar is created.
 	 */
 	public Calendar() {
 		items_ = new HashSet<CalendarItem>();
 	}
 	
 	/**
 	 * Create a Calendar with a type
 	 * 
 	 * @param type
 	 */
 	public Calendar(int type) {
 		this();
 		setType(type);
 	}
 	
 	/**
 	 * Set the type of the Calendar
 	 * 
 	 * @param type
 	 */
 	public void setType(int type) {
 		type_ = type;
 		
 		switch(type_) {
 		case FIVE:
 		case SEVEN:
 			break;
 		default:
 			throw new IllegalArgumentException("Invalid Calendar type.");
 		}	
 	}
 	
 	public int getType() {
 		return type_;
 	}
 	
 	/**
 	 * Add an CalendarItem to the Calendar
 	 * 
 	 * @param item
 	 */
 	public void addItem(CalendarItem item) {
 		trigger("itemWillAdd", this, item);
 		if (trigger("itemShouldAdd", this, item)) {
 			items_.add(item);
 			trigger("itemDidAdd", this, item);
 		}
 	}
 	
 	/**
 	 * Remove an CalendarItem from the Calendar
 	 * 
 	 * @param item
 	 * @return true the item exists.
 	 */
 	public boolean removeItem(CalendarItem item) {
 		trigger("itemWillRemove", this, item);
 		boolean result = false;
 		if (trigger("itemShouldAdd", this, item))
 			result = items_.remove(item);
 		if (result)
 			trigger("itemDidRemove", this, item);
 		return result;
 	}
 
 	/**
 	 * Get all CalendarItems of a day
 	 * 
 	 * @param day
 	 * @return a Set of CalendarItems that occupy that day.
 	 */
 	public Set<CalendarItem> getItemsOfDay(int day) {
 		final Set<CalendarItem> items = new HashSet<CalendarItem>();
 		for (CalendarItem item: items_) {
 			if (item.hasDay(day)) {
 				items.add(item);
 			}
 		}
 		return items;
 	}
 	
 }
