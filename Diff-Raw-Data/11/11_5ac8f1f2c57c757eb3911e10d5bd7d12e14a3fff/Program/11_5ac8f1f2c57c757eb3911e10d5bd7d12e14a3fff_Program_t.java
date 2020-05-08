 /**
  * cal+ – like cal, but with events
  * 
  * Copyright © 2013  Mattias Andrée (maandree@member.fsf.org)
  * 
  * This library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this library.  If not, see <http://www.gnu.org/licenses/>.
  */
 import java.util.*;
 
 
 /**
  * Mane class
  * 
  * @author  Mattias Andrée, <a href="mailto:maandree@member.fsf.org">maandree@member.fsf.org</a>
  */
 public class Program
 {
     /**
      * Mane method
      * 
      * @param  args  Command line, excluding executable
      */
     public static void main(final String... args)
     {
 	Locale locale = Locale.getDefault();
 	Calendar cal = Calendar.getInstance(locale);
 	cal.setLenient(true);
 	
 	int year = cal.get(Calendar.YEAR);
 	int month = cal.get(Calendar.MONTH) + 1;
 	String monthName = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale);
 	int day = cal.get(Calendar.DAY_OF_MONTH);
 	
 	int hour = cal.get(Calendar.HOUR_OF_DAY);
 	int minute = cal.get(Calendar.MINUTE);
 	int second = cal.get(Calendar.SECOND);
 	int milli = cal.get(Calendar.MILLISECOND);
 	
 	int weekday = (cal.get(Calendar.DAY_OF_WEEK) % 7 + 5) % 7 + 1;
 	String weekdayName = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale);
 	int week = cal.get(Calendar.WEEK_OF_YEAR);
 	
 	TimeZone tz = cal.getTimeZone();
 	int offset = tz.getOffset(cal.getTime().getTime());
 	boolean negOffset = offset < 0;
 	offset = (offset < 0 ? -offset : offset) / (60 * 1000);
 	offset = offset / 60 * 100 + offset % 60;
 	String timeZoneName = tz.getDisplayName(tz.inDaylightTime(cal.getTime()), TimeZone.SHORT, locale);
 	
 	System.out.printf("Current time:  %03d-(%02d)%s-%02d %02d:%02d:%02d.%03d, %s(%d) w%d, %s%04d %s\n\n",
 			  year, month, monthName, day, hour, minute, second, milli,
 			  weekdayName, weekday, week,
 			  negOffset ? "-" : "+", offset, timeZoneName);
 	
	String head = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
	head += " " + year;
	head = "             ".substring(0, (26 - head.length()) >> 1) + head + "             ";
	System.out.println(head.substring(0, 25));
	
 	cal.roll(Calendar.DATE, 1 - day);
 	int epochWeekday = (cal.getFirstDayOfWeek() % 7 + 5) % 7;
 	int firstWeekday = (cal.get(Calendar.DAY_OF_WEEK) % 7 + 5) % 7;
 	int firstWeek = cal.get(Calendar.WEEK_OF_YEAR);
 	int weekOffset = (7 - epochWeekday + firstWeekday) % 7;
 	
 	cal.add(Calendar.DATE, -weekOffset);
 	System.out.print("Week");
 	for (int i = 0; i < 7; i++)
 	{
 	    System.out.print(" " + cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale).substring(0, 2));
 	    cal.add(Calendar.DATE, 1);
 	}
 	cal.add(Calendar.DATE, weekOffset - 7);
 	System.out.println();
 	
 	System.out.printf("(%2d)", firstWeek);
 	for (int i = 0; i < weekOffset; i++)
 	    System.out.print("   ");
 	month--;
 	int d = 1, wd = firstWeekday, w = firstWeek;
 	while (cal.get(Calendar.MONTH) == month)
 	{
 	    System.out.printf(" %2d", d++);
 	    wd++;
 	    cal.add(Calendar.DATE, 1);
 	    if ((wd == 7) && (cal.get(Calendar.MONTH) == month))
 	    {   wd = 0;
		System.out.printf("\n(%2d)", ++w);
 	    }
 	}
 	System.out.println();
	
	System.out.println();
     }
     
 }
 
