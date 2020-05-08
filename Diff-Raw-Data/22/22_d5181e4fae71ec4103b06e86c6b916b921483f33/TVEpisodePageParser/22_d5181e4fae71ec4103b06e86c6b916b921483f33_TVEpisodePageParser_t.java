 package com.twentysix20.media.webreaders.tv;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.twentysix20.media.webreaders.movie.ShowData;
 import com.twentysix20.util.StringUtil;
 import com.twentysix20.util.html.FancyInternetHtmlLoader;
 import com.twentysix20.util.html.HtmlLoader;
 import com.twentysix20.util.html.HtmlUtil;
 
 public class TVEpisodePageParser {
 //	static private DateFormat formatter1 = new SimpleDateFormat("EEEE MMMM dd, yyyy");
 	static private DateFormat formatter1 = new SimpleDateFormat("MM/dd/yy");
 	static private DateFormat formatter2 = new SimpleDateFormat("MMMM dd, yyyy");
 	//1977-09-10T07:00:00Z
 //	static private DateFormat outputFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
 	static private DateFormat outputFormatter = new SimpleDateFormat("yyyy-MM-dd");
 	private HtmlLoader loader;
 	private CastPageParser castParser;
 
 	public TVEpisodePageParser(HtmlLoader loader) {
 		this.loader = loader;
 		this.castParser = new CastPageParser(loader);
 	}
 
 	public ShowData parse(String url) throws Exception {
 		System.out.println("  Processing Episode: "+url);
 		String page = loader.getHtmlPage(url);
 		ShowData showData = new ShowData();
 	
 		int titlePos = page.indexOf("ep_title");
 		String title = StringUtil.grab(page,">","<",titlePos);
 		showData.setEpisodeTitle(cleanSummary(title));
 
 		int showNamePos = page.indexOf("show_title");
 		String showName = StringUtil.grab(page,">","<",showNamePos);
 		showData.setShowName(showName);
 
 		int numbersPos = page.indexOf("<title>");
 		String numbersStr = StringUtil.grab(page," - "," - ",numbersPos).trim();
 		numbersStr = numbersStr.replaceAll("[^0-9,]", "");
 		String[] numbers = numbersStr.split(",");
 		if (!StringUtil.isEmpty(numbers[0]))
 			showData.setSeason(Integer.valueOf(numbers[0]));
 		if (numbers.length > 1 && !StringUtil.isEmpty(numbers[1]))
 			showData.setEpisode(Integer.valueOf(numbers[1]));
 
 		String dateStr = StringUtil.grab(page, "Aired","</div>");
 		showData.setYear("'"+parseDate(dateStr));
 
 		int descPos = page.indexOf("<h2>Episode Summary</h2>");
 		String summary = StringUtil.grab(page, "description\">","<form", descPos);
 		summary = cleanSummary(summary);
 		showData.setDescription(summary);
 
 		List<String> actors = new ArrayList<String>();
 
 //		castParser.parse(url + "/cast/", showData);
 		CastPageParser castPageParser = new CastPageParser(new FancyInternetHtmlLoader());
 		actors.addAll(castPageParser.parse(url, "stars"));
 		actors.addAll(castPageParser.parse(url, "recurring-roles"));
 		actors.addAll(castPageParser.parse(url, "guest-stars"));
 //		actors.addAll(castPageParser.parse(url, "cameos"));
 //		actors.addAll(castPageParser.parse(url, "special-guest-stars"));
 
 		showData.setActors(actors);
 
 		showData.setDirectors(castPageParser.parse(url, "directors"));
 		showData.setWriters(castPageParser.parse(url, "writers"));
 
 		return showData;
 	}
 
 	private String parseDate(String dateStr) throws ParseException {
 		dateStr = dateStr.trim();
 		if (!dateStr.matches(".*[0-9]+.*"))
 			return dateStr;
 		try {
 			return outputFormatter.format(formatter1.parse(dateStr));
 		} catch (ParseException p) {}
 		return outputFormatter.format(formatter2.parse(dateStr));
 	}
 
 	private String cleanSummary(String summary) {
		summary = summary.replaceAll("<a.*?</a>", "");
		summary = summary.replaceAll("</?span.*?>\\.*", "");
 		summary = HtmlUtil.cleanHtml(summary);
 		return summary;
 	}
 }
