 /*******************************************************************************
  * Copyright (c) 2011 The University of York.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Louis Rose - initial API and implementation
  ******************************************************************************/
 package simulator.execution.model;
 
 import java.util.Calendar;
 import java.util.Date;
 
 public class DateUtils {
 
 	public static Date add(Date augend, int unit, int amount) {
 		final Calendar calendar = Calendar.getInstance();
 		
 		calendar.setTime(augend);
 		calendar.add(unit, amount);
 		return calendar.getTime();
 	}
 	
 	public static Date todayAt(int hours, int minutes) {
 		final Calendar calendar = Calendar.getInstance();
 		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, hours);
 		calendar.set(Calendar.MINUTE, minutes);
 		return calendar.getTime();
 	}
 	
 	/**
 	 * Checks whether the two parameters represent the same
 	 * date (but not necessarily the same time).
 	 */
 	public static boolean sameDate(Date first, Date second) {
 		return getDayOfYearOf(first) == getDayOfYearOf(second) &&
 		       getYearOf(first) == getYearOf(second);
 	}
 
 	private static int getDayOfYearOf(Date date) {
 		return getFieldOf(date, Calendar.DAY_OF_YEAR);
 	}
 	
 	private static int getYearOf(Date date) {
 		return getFieldOf(date, Calendar.YEAR);
 	}
 	
 	private static int getFieldOf(Date date, int field) {
 		final Calendar calendar = Calendar.getInstance();
 		calendar.setTime(date);
 		return calendar.get(field);
 	}
 }
