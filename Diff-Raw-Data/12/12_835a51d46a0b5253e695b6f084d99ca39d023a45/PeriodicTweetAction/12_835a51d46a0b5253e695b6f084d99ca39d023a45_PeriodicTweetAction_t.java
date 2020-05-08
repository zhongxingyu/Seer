 /*
  * Copyright (C) 2011-2013 Kuropen.
  * 
  * This file is part of the Electricity Warning Crawler, Version 3.
  * 
  * The Electricity Warning Crawler is free software:
  * you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * The Electricity Warning Crawler is distributed in the hope that
  * it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with The Electricity Warning Crawler.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.kuropen.elecwarnv3;
 
 import java.util.Calendar;
 
 import org.kuropen.elecwarnv3.util.TwitterUtilv3;
 
 public class PeriodicTweetAction implements Action {
 
 	private Calendar calendar;
 	private TwitterUtilv3 tu;
 	
 	public PeriodicTweetAction (TwitterUtilv3 twUtil, Calendar cal) {
 		calendar = cal;
 		tu = twUtil;
 	}
 	
 	@Override
 	public void doAction() {
		int min = calendar.get(Calendar.MINUTE);
		tu.sendTweet("各地の最新の電力使用率の情報は、 http://elecwarn.kuropen.org/ をご覧ください。 (" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", min) + ")");
 	}
 
 }
