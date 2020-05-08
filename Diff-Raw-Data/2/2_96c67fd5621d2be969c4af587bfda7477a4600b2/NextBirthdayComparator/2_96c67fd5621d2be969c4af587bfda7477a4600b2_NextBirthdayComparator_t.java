 /**
  * File: NextBirthdayComparator.java
  * Created: 04.06.2011
  *
  * Copyright (c) 2011 Yves Bussard
  * Wettingen, Switzerland
  */
 
 package ch.ybus.birthdaybook.utils;
 
 import java.util.Comparator;
 
 import ch.ybus.birthdaybook.db.RawContact;
 
 /**
 * Comparator which compares the number of days to the next birthday of two {@link RawContact}s.
  */
 public class NextBirthdayComparator implements Comparator<RawContact> {
 	
 	//---- Fields
 	
 	private final long referenceTime;
 	//---- Constructor
 	
 	/**
 	 * Constructs a <code>NextBirthdayComparator</code>.
 	 * @param referenceTime the time stamp against which to calculate the next birthday delta.
 	 */
 	public NextBirthdayComparator (long referenceTime) {
 		this.referenceTime = referenceTime;
 	}
 
 	//---- Methods
 
 	@Override
 	public int compare (RawContact object1, RawContact object2) {
 		return getDaysUntilNextBirthday(object1) - getDaysUntilNextBirthday(object2);
 	}
 
 	private int getDaysUntilNextBirthday (RawContact contact) {
 		return DateUtils.getEventInfo(contact.getBirthDate(), this.referenceTime)
 			.getDaysUntilNextBirthday();
 	}
 }
