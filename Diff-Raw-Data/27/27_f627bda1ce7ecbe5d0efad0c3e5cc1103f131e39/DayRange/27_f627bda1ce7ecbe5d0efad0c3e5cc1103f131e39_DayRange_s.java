 /**
  * 
  */
 package org.omo.core;
 
 import java.util.Collection;
 import java.util.Date;
 
 public class DayRange extends AbstractDateRange {
 
 	public DayRange(Date min, Date max) {
 		super(min, max);
 	}
 
 	@Override
 	public boolean contains(Date value) {
 		long time = value.getTime();
		return time >= min.getTime() || time <= max.getTime();
 	}
 
 	@Override
 	public Collection<Date> getValues() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("NYI");
 	}
 
 	@Override
 	public Date random() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("NYI");
 	}
 
 	@Override
 	public int size() {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("NYI");
 	}
 }
