 package com.vorsk.crossfitr.models;
 
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.horrabin.horrorss.*;
 
 public class WODModel {
 	public static final String feed_url = "http://feeds.feedburner.com/crossfit/eRTq?format=xml";
 
 	RssParser rss = new RssParser();
 
 	private String title;
 	private ArrayList<WorkoutRow> list = new ArrayList<WorkoutRow>();

 	public WODModel() {
 	}
 	
 	/**
 	 * Run this to get results
 	 */
 	public void fetch(){
 		try{
 			RssFeed feed = rss.load(feed_url);
 			
 			RssChannelBean channel = feed.getChannel();
 			this.title = channel.getTitle();
 			
 			// Gets and iterate the items of the feed 
 			List<RssItemBean> items = feed.getItems();
 			Iterator<RssItemBean> it = items.iterator();
 			
 			RssItemBean item;
 			WorkoutRow row;
 			while (it.hasNext()){
 				item = it.next();
 				row = new WorkoutRow();
 				
 				row.name = parseTitle(item.getTitle(), item.getPubDate());
 				row.description = "Test";//android.text.Html.fromHtml(item.getDescription()).toString();
 				row.workout_type_id = SQLiteDAO.TYPE_WOD;
 				row.record = WorkoutModel.NOT_SCORED;
 				row.record_type_id = WorkoutModel.SCORE_NONE; 
 				list.add(row);
 			}
 			
 		}catch(Exception e){
 			//TODO  Something to do if an exception occurs
 			WorkoutRow row = new WorkoutRow();
 			row.name = "Error Loading RSS";
 			list.add(row);
 		}
 	}
 	
	private static String parseTitle(String rssTitle, Date pubDate){
 		// currently I don't use the actual title...
		Format formatter = new SimpleDateFormat("EEEE, dd MMM yyyy");
 		return "WOD "+formatter.format(pubDate);
 	}
 	
 	public String getTitle(){
 		return this.title;
 	}
 	
 	public ArrayList<WorkoutRow> getWodRows(){
 		return this.list;
 	}
 
 }
