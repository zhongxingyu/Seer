 package com.teamsierra.csc191.api.util;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 /**
  * A class that is a collection of {@link DateRange}s with some
  * convenience methods for this collection. 
  * 
  * This collection works a bit differently than a normal collection, 
  * see method descriptions for more details.
  * 
  * @author Kyle
  *
  */
 public class Availability implements Collection<DateRange>
 {	
 	private Collection<DateRange> availability;
 	
 	public Availability()
 	{
 		availability = new ArrayList<DateRange>();
 	}
 	
 	public Availability(Collection<DateRange> availability)
 	{
 		this.availability = availability;
 	}
 	
 	public Collection<DateRange> getAvailability() 
 	{
 		return availability;
 	}
 
 	public void setAvailability(Collection<DateRange> availability) 
 	{
 		this.availability.clear();
 		
 		availability.addAll(availability);
 	}
 
 	/**
 	 * Convenience method which creates a {@link DateRange} from the two
 	 * params and then calls {@link #add(DateRange)}. 
 	 * 
 	 * @param startDate start date for DateRange, see {@link DateRange}.
 	 * @param endDate end date for DateRange, see {@link DateRange}.
 	 * @return see {@link #add(DateRange)}.
 	 */
 	public boolean addRange(Date startDate, Date endDate)
 	{
 		return add(new DateRange(startDate, endDate));
 	}
 	
 	/**
 	 * Convenience method which creates a {@link DateRange} from the two
 	 * params and then calls {@link #remove(Object)}.
 	 * 
 	 * @param startDate start date for DateRange, see {@link DateRange}.
 	 * @param endDate end date for DateRange, see {@link DateRange}.
 	 * @return see {@link #remove(Object)}.
 	 */
 	public boolean removeRange(Date startDate, Date endDate)
 	{
 		return remove(new DateRange(startDate, endDate));
 	}
 	
 	/**
 	 * Clears all availability prior to the date param.
 	 * 
 	 * @param date
 	 */
 	public void removePriorAvailability(Date date)
 	{
 		remove(new DateRange(new Date(Long.MIN_VALUE), date));
 	}
 	
 	/**
 	 * Clears all availability prior to the time at which this method
 	 * is called.
 	 * 
 	 */
 	public void removeOldAvailability()
 	{
 		Calendar cal = new GregorianCalendar();
 		removePriorAvailability(cal.getTime());
 	}
 	
 	/**
 	 * Returns true if available for the entire {@link DateRange} specified.
 	 * 
 	 * @param dateRange
 	 * @return
 	 */
 	public boolean isAvailable(DateRange dateRange)
 	{
 		for(DateRange dr : availability)
 		{
			if(dr.getStartDate().compareTo(dateRange.getStartDate()) <= 0 &&
					dr.getEndDate().compareTo(dateRange.getEndDate()) >= 0)
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean isEmpty()
 	{
 		return availability.isEmpty();
 	}
 	
 	@Override
 	public String toString()
 	{		
 		String string = "{";
 		Iterator<DateRange> iterator = availability.iterator();
 		
 		if(iterator.hasNext())
 		{
 			string += "availability[" + 0 + "]='" + iterator.next() + '\'';
 			
 			int i = 1;
 			while(iterator.hasNext())
 			{
 				string += ", availability[" + i + "]='" + iterator.next() + '\'';
 				i++;
 			}
 		}
 		
 		string += "}";
 		
 		return string;
 	}
 
 	/**
 	 * Adds a {@link DateRange} to the collection. does not work in the
 	 * traditional manor that a collection add method does. This method
 	 * will take into account the DateRanges already in the collection
 	 * and merge the new addition with any DateRanges that it overlaps.
 	 * 
 	 * Should always return true due to the way this collection works.
 	 */
 	@Override
 	public boolean add(DateRange dateRange)
 	{
 		if(availability.isEmpty())
 		{
 			availability.add(dateRange);
 		}
 		else
 		{
 			boolean overlap = false;
 			DateRange overlapDate = dateRange;
 			ArrayList<DateRange> removeDate = new ArrayList<DateRange>();
 			
 			for(DateRange dr : availability)
 			{
 				if(overlapDate.isOverlapping(dr))
 				{
 					removeDate.add(dr);
 					overlapDate = overlapDate.mergeDateRange(dr);
 					overlap = true;
 				}
 			}
 			for(DateRange dr : removeDate)
 			{
 				availability.remove(dr);
 			}
 			
 			if(!overlap)
 			{
 				availability.add(dateRange);
 			}
 			else
 			{				
 				availability.add(overlapDate);
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Iterates through the collection and calls {@link #add(DateRange)}.
 	 * 
 	 * Should always return true due to the way this collection works.
 	 */
 	@Override
 	public boolean addAll(Collection<? extends DateRange> c) 
 	{
 		for(DateRange dr : c)
 		{
 			this.add(dr);
 		}
 		
 		return true;
 	}
 
 	@Override
 	public void clear() 
 	{
 		availability = new ArrayList<DateRange>();
 	}
 
 	@Override
 	public boolean contains(Object o) 
 	{		
 		return availability.contains(o);
 	}
 
 	@Override
 	public boolean containsAll(Collection<?> c) 
 	{
 		return availability.containsAll(c);
 	}
 
 	@Override
 	public Iterator<DateRange> iterator()
 	{
 		return availability.iterator();
 	}
 	
 	/**
 	 * ONLY works if param is a {@link DateRange}. Will return false
 	 * if any other object is passed in.
 	 * 
 	 * This method does not work in the traditional collection remove sense.
 	 * It will remove the DateRange from the collection in such a way that
 	 * the remaining collection is the original DateRanges less the DateRange
 	 * passed in.
 	 * 
 	 * e.g. given that there is a DateRange with startDate at 1am and an endDate
 	 * on the same day at 10am, removing a DateRange from 4am to 7am on that
 	 * same day would result in the collection containing a DateRange from 
 	 * 1am to 4am, and another DateRange from 7am to 10am.
 	 * 
 	 * @return returns false if the object passes in is not an instance of
 	 * {@link DateRange}, true otherwise. 
 	 * 
 	 */
 	@Override
 	public boolean remove(Object o) 
 	{
 		DateRange dateRange;
 
 		if(o instanceof DateRange)
 		{
 			dateRange = (DateRange) o; 
 		}
 		else
 		{
 			return false;
 		}
 		
 		if(!availability.isEmpty())
 		{
 			ArrayList<DateRange> removeList = new ArrayList<DateRange>();
 			Date dateRangeStart = dateRange.getStartDate();
 			Date dateRangeEnd = dateRange.getEndDate();
 			
 			Date drStart, drEnd;
 			for(DateRange dr : availability)
 			{
 				drStart = dr.getStartDate();
 				drEnd = dr.getEndDate();
 				
 				if(dateRangeStart.compareTo(drEnd) < 0)
 				{
 					if(dateRangeStart.compareTo(drStart) > 0)
 					{
 						dr.setEndDate(dateRangeStart);
 						
 						if(dateRangeEnd.compareTo(drEnd) < 0)
 						{
 							availability.add(new DateRange(dateRangeEnd, drEnd));
 							break;
 						}
 					}
 					else
 					{
 						if(dateRangeEnd.compareTo(drStart) > 0)
 						{
 							if(dateRangeEnd.compareTo(drEnd) < 0)
 							{
 								dr.setStartDate(dateRangeEnd);
 							}
 							else
 							{
 								removeList.add(dr);
 							}
 						}
 					}
 				}
 			}
 			
 			for(DateRange dr : removeList)
 			{
 				availability.remove(dr);
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Iterates through the collection calling {@link #remove(Object)}.
 	 * 
 	 * Should always return true due to the way remove works in this
 	 * collection.
 	 */
 	@Override
 	public boolean removeAll(Collection<?> c) 
 	{
 		for(Object o : c)
 		{
 			remove(o);
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * NOT IMPLEMENTED. Always returns false.
 	 * 
 	 */
 	@Override
 	public boolean retainAll(Collection<?> c)
 	{
 		return false;
 	}
 
 	@Override
 	public int size() 
 	{
 		return availability.size();
 	}
 
 	@Override
 	public Object[] toArray() 
 	{
 		return availability.toArray();
 	}
 
 	@Override
 	public <T> T[] toArray(T[] a) 
 	{
 		return availability.toArray(a);
 	}
 }
