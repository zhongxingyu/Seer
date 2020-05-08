 package com.dev.campus.util;
 
 import android.annotation.SuppressLint;
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.dev.campus.CampusUB1App;
 import com.dev.campus.event.Event;
 
 @SuppressLint("SimpleDateFormat")
 public class TimeExtractor {
 
 	public static List<String[]> parseTime(String text){
 
 		ArrayList<String[]> dates = new ArrayList<String[]>();
 		String hours = "[0-2]?[0-9]";
 		String minutes = "[0-5][0-9]";
 		String intervalle = "de " + hours + "[hH:](" + minutes + ")?" 
 				+ " [àa] " + hours + "[hH:](" + minutes + ")?";
 		String time = hours + "[hH:](" + minutes + ")?";
 
 		Pattern p = Pattern.compile(intervalle);
 		Pattern p2 = Pattern.compile(time);
 		Matcher m = p.matcher(text);
 		Matcher m2 = p2.matcher(text);
 
 		if (m.find()) {
 			String s = m.group();
 			if(s != null) {
 				s = s.toLowerCase().replace("de ", "");
 				String[] tabDate = s.split(" à ");
 				for (int i = 0; i < tabDate.length; i++) {
 					if (tabDate[i].contains("h")) {
 						String s2[] = tabDate[i].split("h");
 						dates.add(s2);
 						
 					} else {
 						dates.add(tabDate[0].split(":"));
 					}
 				}
 			}
 		} else if (m2.find()) {
 			String s = m2.group();
 			if (s != null) {
 				s.toLowerCase();
 				// Split the time into a table to help with the management
 				if(s.contains("h")) {
 					String[] ss = s.split("h");
 					dates.add(ss);
 				} else {
 					dates.add(s.split(":"));
 				}
 			}
 		}
 		return dates;
 	}
 
 	public static Date createDate(String date, String format) throws ParseException {
 		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
 		return sdf.parse(date);
 	}
 
 	public static void getCorrectDate(String date, Event event) throws ParseException {
 		List<String[]> time = parseTime(event.getDetails());
 		String format = "EEE, d MMM yyyy HH:mm:ss Z";
 		Date start = createDate(date, format);
 		Date end = createDate(date, format);
 
 		if(time.size() > 0) {
 			if(time.get(0).length > 1) { // Means we have parsed minutes
 				start.setMinutes(Integer.parseInt(time.get(0)[1]));
 			} else {
 				// No minutes parsed, assume the time is at the start of the specified hour
 				start.setMinutes(0);
 			}
 			start.setHours(Integer.parseInt(time.get(0)[0]));
 			if (time.size() > 1){
 				if(time.get(1).length > 1) { // Means we have parsed minutes
 					end.setMinutes(Integer.parseInt(time.get(1)[1]));
 				} else {
 					// No minutes parsed, assume the time is at the start of the specified hour
 					end.setMinutes(0);
 				}
 				end.setHours(Integer.parseInt(time.get(1)[0]));
 			} else {
 				end.setMinutes(start.getMinutes());
 				end.setHours(start.getHours());
 			}
 		} else {
 			/* no time parsed in text, set time to 00:00 to avoid displaying
 			 * the time at which the article was posted
 			 */
 			start.setMinutes(0);
 			start.setHours(0);
 			end.setMinutes(0);
 			end.setHours(0);
 		}
 		event.setStartDate(start);
 		event.setEndDate(end);
 	}
 
 	public static void getDateLabri(String date, Date start, Date end) {
 		String[] tabDate = date.split("-");
 		String[] tabDate2 = tabDate[0].split(":");
 		String[] tabDate3 = tabDate[1].split(":");
 
 		start.setHours(Integer.parseInt(tabDate2[0]));
 		start.setMinutes(Integer.parseInt(tabDate2[1]));
 		end.setHours(Integer.parseInt(tabDate3[0]));
 		end.setMinutes(Integer.parseInt(tabDate3[1]));
 	}
 
 
 }
