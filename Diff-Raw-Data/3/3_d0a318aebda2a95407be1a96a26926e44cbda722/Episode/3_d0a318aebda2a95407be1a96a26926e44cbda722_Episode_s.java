 package com.yoharnu.newontv.android.shows;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.GregorianCalendar;
 import java.util.Scanner;
 
 import org.apache.commons.io.FileUtils;
 
 import android.widget.TextView;
 
 import com.yoharnu.newontv.android.App;
 
 public class Episode {
 	private String season;
 	private String episode;
 	private String overview;
 	private String epName;
 	public Series parent;
 	private File cache = null;
 
 	public Episode(Series series) {
 		parent = series;
 		String seriesId = series.getSeriesId();
 		String today = Integer.toString(App.today.get(GregorianCalendar.YEAR));
 		if (App.today.get(GregorianCalendar.MONTH) + 1 < 10)
 			today += "0";
 		today += Integer.toString(App.today.get(GregorianCalendar.MONTH) + 1);
 		if (App.today.get(GregorianCalendar.DATE) < 10)
 			today += "0";
 		today += Integer.toString(App.today.get(GregorianCalendar.DATE));
 		File dir = new File(App.getContext().getCacheDir(), "episodes");
 		if (!dir.exists()) {
 			dir.mkdir();
 		}
 		final String file = dir.getAbsolutePath() + "/" + today + "/"
 				+ seriesId;
 		cache = new File(file);
 		if (!cache.exists()) {
 			try {
 				String url = App.MIRRORPATH
 						+ "/api/GetEpisodeByAirDate.php?apikey=" + App.API_KEY
 						+ "&seriesid=" + seriesId + "&airdate=" + today
 						+ "&language=" + App.LANGUAGE + ".xml";
 				FileUtils.copyURLToFile(new URL(url), cache);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			Scanner s = new Scanner(cache);
 			parent.episodes.clear();
 			while (s.hasNextLine()) {
 				String line = s.nextLine();
 				String tag = XMLParser.getTag(line);
 				if (tag.equals("Episode")) {
 					parse(s);
 					print();
 					parent.episodes.add(this);
 				}
 			}
 			if (s != null)
 				s.close();
 		} catch (FileNotFoundException e) {
 			// e.printStackTrace();
 		}
 	}
 
 	public TextView print() {
 		TextView returnVal = new TextView(App.getContext());
 		String text = parent.getSeriesName() + "          "
 				+ parent.getNetwork() + "\n          " + epName + "          s";
 		if (Integer.parseInt(season) < 10) {
 			text += "0";
 		}
 		text += season + "e";
 		if (Integer.parseInt(episode) < 10) {
 			text += "0";
 		}
 		text += episode + "\n          Overview: " + overview;
 		returnVal.setText(text);
 		returnVal.setPadding(returnVal.getPaddingLeft() + 50,
 				returnVal.getPaddingTop(), returnVal.getPaddingRight(),
 				returnVal.getPaddingBottom());
 		return returnVal;
 	}
 
 	public String getSeason() {
 		return season;
 	}
 
 	public String getEpisode() {
 		return episode;
 	}
 
 	public String getOverview() {
 		return overview;
 	}
 
 	public String getEpName() {
 		return epName;
 	}
 
 	public void parse(Scanner s) {
 		while (s.hasNextLine()) {
 			String line = s.nextLine();
 			String tag = XMLParser.getTag(line);
 			String data = XMLParser.getData(line);
 			if (data == null || data.equals(""))
 				data = "Not Available";
 			if (tag.equals("Overview"))
 				overview = data.replaceAll("&quot;", "\"").replaceAll("&amp;",
 						"&");
 			else if (tag.equals("SeasonNumber"))
 				season = data;
 			else if (tag.equals("EpisodeNumber"))
 				episode = data;
 			else if (tag.equals("EpisodeName"))
				epName = data;
 		}
 	}
 
 	public File getCache() {
 		return cache;
 	}
 
 	public void setCache(File cache) {
 		this.cache = cache;
 	}
 }
