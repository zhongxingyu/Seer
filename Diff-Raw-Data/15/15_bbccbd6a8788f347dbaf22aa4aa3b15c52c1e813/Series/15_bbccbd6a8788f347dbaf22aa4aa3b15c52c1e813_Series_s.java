 package com.yoharnu.newontv.android.shows;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.commons.io.FileUtils;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Intent;
 import android.util.Xml;
 
 import com.yoharnu.newontv.android.App;
 import com.yoharnu.newontv.android.DownloadFilesTask;
 
 @SuppressWarnings("unused")
 public class Series {
 	public static final int NAME = 0;
 	public static final int ID = 1;
 	private String seriesid;
 	private String language;
 	private String seriesName;
 	private String overview;
 	private String firstAired;
 	private String imdb_id;
 	private String airTime = null;
 	private String network = null;
 	private String banner;
 	public LinkedList<Episode> episodes;
 	File cache = null;
 	public static LinkedList<Series> options = null;
 	public DownloadFilesTask task;
 	public String url;
 	public String file;
 
 	public Series(final String text, int mode) {
 		episodes = new LinkedList<Episode>();
 		if (mode == NAME) {
 			setupSeriesByName(text);
 		} else if (mode == ID) {
 			setupSeriesById(text);
 		} else {
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	public Series() {
 	}
 
 	private void setupSeriesByName(final String search) {
 		url = App.MIRRORPATH + "/api/GetSeries.php?seriesname="
 				+ search.replaceAll(" ", "%20") + "&language=" + App.LANGUAGE;
 		file = App.getContext().getCacheDir().getAbsolutePath() + "search";
 	}
 
 	public static void parseSearch(File search) {
 		options = new LinkedList<Series>();
 		try {
 			Scanner s = new Scanner(search);
 			while (s.hasNextLine()) {
 				String line = s.nextLine();
 				if (XMLParser.getTag(line).equals("Series") && s.hasNextLine()) {
 					Series temp = new Series();
 					while (!XMLParser.getTag(line).equals("/Series")
 							&& s.hasNextLine()) {
 						line = s.nextLine();
 						String tag = XMLParser.getTag(line);
 						String data = XMLParser.getData(line);
 						if (tag.equals("seriesid")) {
 							temp.seriesid = data;
 						} else if (tag.equals("language")) {
 							temp.language = data;
 						} else if (tag.equals("SeriesName")) {
 							temp.seriesName = data.replaceAll("&amp;", "&");
 						} else if (tag.equals("Overview")) {
 							temp.overview = data;
 						} else if (tag.equals("FirstAired")) {
 							temp.firstAired = data;
 						} else if (tag.equals("IMDB_ID")) {
 							temp.imdb_id = data;
 						} else if (tag.equals("Airs_Time")) {
 							temp.airTime = data.replaceAll("PST", "")
 									.replaceAll("EST", "")
 									.replaceAll("ET/PT", "").trim();
 						} else if (tag.equals("banner")) {
 							temp.banner = data;
 						}
 					}
 					options.add(temp);
 				}
 			}
 			if (s != null) {
 				s.close();
 			}
 		} catch (FileNotFoundException e) {
 		}
 
 	}
 
 	private void setupSeriesById(final String id) {
 		this.seriesid = id;
		file = App.getContext().getCacheDir().getAbsolutePath() + id;
 		cache = new File(file);
 		if (!cache.exists()) {
 			try {
 				cache.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			url = App.MIRRORPATH + "/api/" + App.API_KEY + "/series/" + id
 					+ "/" + App.LANGUAGE + ".xml";
 			task = new DownloadFilesTask();
 			task.execute(url, file);
 		}
 		try {
 			if (task != null) {
 				task.get();
 			}
 		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		parse();
 		App.shows.add(this);
 		App.save();
 
 	}
 
 	public String getSeriesName() {
 		return this.seriesName;
 	}
 
 	public String getOverview() {
 		return this.overview;
 	}
 
 	public String getSeriesId() {
 		return this.seriesid;
 	}
 
 	public void parse() {
 		try {
 			Scanner s = new Scanner(cache);
 			while (s.hasNextLine()) {
 				String line = s.nextLine();
 				String tag = XMLParser.getTag(line);
 				String data = XMLParser.getData(line);
 				if (data == null) {
 					data = "Not Available";
 				}
 				if (tag.equals("language")) {
 					language = data;
 				} else if (tag.equals("SeriesName")) {
 					seriesName = data;
 				} else if (tag.equals("Overview")) {
 					overview = data;
 				} else if (tag.equals("FirstAired")) {
 					firstAired = data;
 				} else if (tag.equals("IMDB_ID")) {
 					imdb_id = data;
 				} else if (tag.equals("banner")) {
 					banner = data;
 				} else if (tag.equals("Airs_Time")) {
 					airTime = data;
 				} else if (tag.equals("Network")) {
 					network = data;
 				}
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getTime() {
 		return airTime;
 	}
 
 	public String getNetwork() {
 		return network;
 	}
 }
