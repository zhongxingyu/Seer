 package notifier.parser;
 
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import notifier.SRM;
 
 public class SRMCalendarParser {
 	private static final Logger log = Logger.getLogger(SRMCalendarParser.class
 			.getName());
 	private static final SimpleDateFormat format;
 	static {
 		format = new SimpleDateFormat("yyyy年MM月dd日（E） HH時mm分", Locale.JAPAN);
 		format.setTimeZone(TimeZone.getTimeZone("GMT+09:00"));
 	}
 
 	private String url;
 
 	public SRMCalendarParser(String url) {
 		this.url = url;
 	}
 
 	public String getUrl() {
 		return this.url;
 	}
 
 	protected String getContent(String url) throws IOException{
 		BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));
 		StringBuilder sb = new StringBuilder();
 		for(String line; (line = br.readLine()) != null;) {
 			sb.append(line);
 			sb.append("\n");
 		}
 		return sb.toString();
 	}
 
 	public List<SRM> getSRMs() {
 		ArrayList<SRM> result = new ArrayList<SRM>();
 		try {
 			Document doc = Jsoup.parse(getContent(url));
 			log.info("カレンダー取得 from " + url);
 			for(Element elem : doc.getElementsByClass("srm")){
 				Element link = elem.getElementsByTag("a").get(0);
 				SRM srm = new SRM();
 				srm.setName(link.text());
 				srm.setUrl(link.attr("href"));
 				List<Date> dates = getTimes("http://community.topcoder.com" + srm.getUrl());
 				srm.setRegisterTime(dates.get(0));
 				srm.setCompetisionTime(dates.get(1));
 				srm.setCount(0);
 				result.add(srm);
 				log.info("SRMデータ解析 : " + srm);
 			}
 		} catch (Exception e) {
 			log.warning(e.getMessage());
 		}
 		return result;
 	}
 
 	private List<Date> getTimes(String url) {
		System.out.println(url);
 		ArrayList<Date> dates = new ArrayList<Date>();
 		try {
 			Document doc = Jsoup.parse(getContent(url));
 			String day = "";
 			for(Element elem: doc.getElementsByClass("statText")) {
 				String text = elem.text().replaceAll("\\s", "");
 				if (Pattern.matches("..\\..+", text)) {
 					day = text;
 				} else {
 					SimpleDateFormat format2 = new SimpleDateFormat(
 							"MM.dd.yyyyhh:mmaz", Locale.US);
 					Date parse = format2.parse(day + text);
 					dates.add(parse);
 					log.info("データ取得 from " + url + ",date=" + day + text
 							+ " to " + parse + " and " + format.format(parse));
 				}
 			}
 		} catch (Exception e) {
 		}
 		return dates;
 	}
 
 	public static SimpleDateFormat getDataFormat() {
 		return format;
 	}
 }
