 /**
  *     Copyright SocialSite (C) 2009
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.socialsite.util;
 
 import java.util.Date;
 
 public class DateUtils
 {
 	public static String relativeTime(final Date date)
 	{
 		final int SECOND = 1;
 		final int MINUTE = 60 * SECOND;
 		final int HOUR = 60 * MINUTE;
 		final int DAY = 24 * HOUR;
 		final int MONTH = 30 * DAY;
 
		final long delta = Math.abs(System.currentTimeMillis() - date.getTime()) / 1000;
 
 		if (delta < 1 * MINUTE)
 		{
			return delta == 1 ? "one second ago" : delta + " seconds ago";
 		}
 		if (delta < 2 * MINUTE)
 		{
 			return "a minute ago";
 		}
 		if (delta < 45 * MINUTE)
 		{
			return delta / MINUTE + " minutes ago";
 		}
 		if (delta < 90 * MINUTE)
 		{
 			return "an hour ago";
 		}
 		if (delta < 24 * HOUR)
 		{
			return delta / HOUR + " hours ago";
 		}
 		if (delta < 48 * HOUR)
 		{
 			return "yesterday";
 		}
 		if (delta < 30 * DAY)
 		{
			return delta / DAY + " days ago";
 		}
 		if (delta < 12 * MONTH)
 		{
			final long months = delta / MONTH;
 			return months <= 1 ? "one month ago" : months + " months ago";
 		}
 		else
 		{
			final long years = delta / MONTH * 12;
 			return years <= 1 ? "one year ago" : years + " years ago";
 		}
 	}
 }
