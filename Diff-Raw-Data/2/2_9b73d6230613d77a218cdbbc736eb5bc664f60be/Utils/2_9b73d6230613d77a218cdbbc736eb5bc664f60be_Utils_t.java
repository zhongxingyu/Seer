 /*
  * Project:     MyRobots.com integration for ARISE Human Hamster Wheel 
  * Authors:     Jeffrey Arcand <jeffrey.arcand@ariselab.ca>
  * File:        ca/ariselab/myhhw/Utils.java
  * Date:        Sat 2013-04-13
  * Copyright:   Copyright (c) 2013 by Jeffrey Arcand.  All rights reserved.
  * License:     GNU GPL v3
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package ca.ariselab.myhhw;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Calendar;
 
 public final class Utils {
 	
 	/** Cannot instantiate this class. */
 	private Utils() {
 	}
 	
 	/**
 	 * Determine if the HHW is usually open at this time.
 	 * @return Whether or not the HHW is expected to be open.
 	 */
 	public static boolean getHHWOpen() {
 		Calendar now = Calendar.getInstance();
 		
 		// Opens at 09:00 and closes 17:00
 		int hour = now.get(Calendar.HOUR);
 		if (hour < 9 || hour >= 17) {
 			return false;
 		}
 		
 		// Closed on Mondays from Sept to April (inclusive)
		int dow = now.get(Calendar.DAY_OF_WEEK);
 		int month = now.get(Calendar.MONTH);
 		if (dow == Calendar.MONDAY
 		  && (month <= Calendar.APRIL || month >= Calendar.SEPTEMBER)) {
 			return false;
 		}
 		
 		// Otherwise open
 		return true;
 	}
 	
 	/**
 	 * Get the system uptime.
 	 * @return The uptime in minutes.
 	 */
 	public static int getSystemUptime() {
 		int uptime = -1;
 		
 		try {
 			FileReader fr = new FileReader("/proc/uptime");
 			BufferedReader br = new BufferedReader(fr);
 			
 			String[] parts = br.readLine().split(" ");
 			
 			br.close();
 			fr.close();
 			
 			uptime = (int) Float.parseFloat(parts[0]) / 60;
 		
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return uptime;
 	}
 }
 
