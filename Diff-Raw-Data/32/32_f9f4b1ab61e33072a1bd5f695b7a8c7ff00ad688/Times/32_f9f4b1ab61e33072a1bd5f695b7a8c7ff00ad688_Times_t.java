 /*
  * This file is part of AmbientSeasons (https://github.com/Olloth/AmbientSeasons).
  * 
  * AmbientSeasons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package me.ambientseasons.util;
 
 import org.bukkit.World;
 
 import me.ambientseasons.AmbientSeasons;
 
 /**
  * 
  * @author Olloth
  */
 public class Times {
 
 	AmbientSeasons plugin;
 	Config config;
 	World world;
 
 	public Times(AmbientSeasons plugin, World world) {
 		this.plugin = plugin;
 		this.config = plugin.getConfig();
 		this.world = world;
 	}
 
 	public int getDayOfWeek() {
 
 		int day = getDays() % config.getNumberOfWeekdays(world) + 1;
 
 		return day;
 	}
 
 	public int getDayOfMonth() {
 
 		int days = getDays() % getDaysInYear() + 1;
 		
 		for (String string : config.getMonths(world)) {
			int length = config.getMonthLength(string, world);
			if(days > length) {
				days -= length;
 			}
 		}
 		return days;
 	}
 
 	public int getMonth() {
 
 		int month = 1;
 		int days = getDays() % getDaysInYear() + 1;
 		
 		for (String string : config.getMonths(world)) {
			int length = config.getMonthLength(string, world);
			if(days > length) {
				days -= length;
 				month++;
 			}
 		}
 
 		return month;
 	}
 
 	public int getYear() {
 
 		int year = getDays() / getDaysInYear() + 1;
 
 		return year;
 	}
 	
 	public int getDaysInYear() {

 		int days = 0;
 		for (String month : config.getMonths(world)) {
 			days += config.getMonthLength(month,world);
 		}
 		return days;
 	}
 
 	public int getDays() {
 		long time = world.getFullTime();
 
 		return (int) (time / 24000) + 1;
 	}
 
 	public String getDayString() {
 		int day = getDayOfWeek();
 		String string = "";
 		for (int i = 0; i < day; i++) {
 			if (i == (day - 1)) {
 				string = (String) config.getWeekdays(world).get(i);
 			}
 		}
 
 		return string;
 	}
 
 	public String getMonthString() {
 		int month = getMonth();
 		String string = "";
 		for (int i = 0; i < month; i++) {
 			if (i == (month - 1)) {
 				string = (String) config.getMonths(world).get(i);
 			}
 		}
 
 		return string;
 	}
 
 	public String getSeasonUrl() {
 		String string = "";
 		string = config.getSeasonURL(config.getSeason(getMonthString(), world));
 
 		return string;
 	}
 
 	public String getModString(int day) {
 		String string = "";
 		if ((day == 11) || (day == 12) || (day == 13)) {
 			string = "th";
 		} else {
 			switch (day % 10) {
 			case 1:
 				string = "st";
 				break;
 			case 2:
 				string = "nd";
 				break;
 			case 3:
 				string = "rd";
 				break;
 			default:
 				string = "th";
 				break;
 			}
 			;
 		}
 
 		return string;
 	}
 
 	public String getDate() {
 
 		String date = config.getDateMessage();
 		date = date.replace("{WEEKDAY}", getDayString());
 		date = date.replace("{DATE}", Integer.toString(getDayOfMonth()));
 		date = date.replace("{MOD}", getModString(getDayOfMonth()));
 		date = date.replace("{MONTH}", getMonthString());
 		date = date.replace("{YEAR}", Integer.toString(getYear()));
 		return date;
 	}
 }
