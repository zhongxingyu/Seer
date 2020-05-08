 package com.dev.campus.schedule;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 
 public class ScheduleParser {
 
 	public ArrayList<ScheduleGroup> parseFeed(String url) throws MalformedURLException, IOException {
 		// Cannot open iso-8859-1 encoding directly with Jsoup
 		InputStream input = new URL(url).openStream();
 		Document xmlDoc = Jsoup.parse(input, "CP1252", url);
 
 		ArrayList<ScheduleGroup> allGroups = new ArrayList<ScheduleGroup>();
 		for (Element resource : xmlDoc.select("resource")) {
 			ScheduleGroup group = new ScheduleGroup(); 
 			Elements name = resource.select("name");
 			Elements link = resource.select("link[class=xml]");
 			String xmlLink = link.attr("href");
 			group.setGroup(name.text());
			group.setUrl(url.substring(0, 67)+xmlLink);
 			allGroups.add(group);
 		}
 
 		return allGroups;
 	}
 
 	public void parseSchedule(Context context, String url) throws MalformedURLException, IOException {
 		// Cannot open iso-8859-1 encoding directly with Jsoup
 		InputStream input = new URL(url).openStream();
 		Document xmlDoc = Jsoup.parse(input, "CP1252", url);
 
 		for (Element span : xmlDoc.select("event[date]")) {
 			String calDate = span.attr("date");
 			String calStartTime = span.select("starttime").text();
 			String calEndTime = span.select("endtime").text();
 			String calType = span.select("category").text();
 			String calModule = span.select("module").text();
 			String calRoom = span.select("room").text();
 			String calStaff = span.select("staff").text();
 			String calGroup = "";
 			Elements group = span.select("group");
 			for (Element groupItem : group.select("item")) {
 				calGroup += groupItem.text()+"\n";
 			}
 
			String calTitle = (calType == "") ? calModule : calType+" "+calModule ;
			String calDesc  = calGroup+"Prof: "+calStaff+"\n" ;
 
 			if (!calDate.equals("") && !calStartTime.equals("") && !calEndTime.equals("")) {
 				Log.d("LogTag", "date: "+calDate);
 				Log.d("LogTag", "startTime: "+calStartTime);
 				Log.d("LogTag", "endTime: "+calEndTime);
 				Log.d("LogTag", "type: "+calType);
 				Log.d("LogTag", "module: "+calModule);
 				Log.d("LogTag", "room: "+calRoom);
 				Log.d("LogTag", "staff: "+calStaff);
 				Log.d("LogTag", "group: "+calGroup);
 				Log.d("LogTag", " ");
 				Log.d("LogTag", "calTitle: "+calTitle);
 				Log.d("LogTag", "calDesc: "+calDesc);
 				Log.d("LogTag", " ");
 
 				/*
 				// Temporary Code
 				Intent intent = new Intent(Intent.ACTION_EDIT);
 				intent.setType("vnd.android.cursor.item/event");
 				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				intent.putExtra("beginTime", calStartTime);
 				intent.putExtra("endTime", calEndTime);
 				intent.putExtra("title", calTitle);
 				intent.putExtra("description", calDesc);
 				intent.putExtra("eventLocation", calRoom);
 				intent.putExtra("allDay", true);
 				intent.putExtra("rrule", "FREQ=YEARLY");
 				context.startActivity(intent);
 
 				break; // To treat only one event
 				 */
 			}
 		}
 
 	}
 }
