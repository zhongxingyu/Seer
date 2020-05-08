 /**
  * 
  */
 package org.omo.core;
 
 import java.util.Date;
 
 public abstract class AbstractDateRange extends AbstractRange<Date> implements DateRange {
 
 	public AbstractDateRange(Date min, Date max) {
 		super(min, max);
 	}
 	
 }
