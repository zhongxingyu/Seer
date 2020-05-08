 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import vindsiden.Measurement;
 
 import common.WeatherFetcherCommon;
 
 public class WeatherFetcher extends WeatherFetcherCommon {
 
 	private Document document;
 	private Map<String, List<String>> data;
 
 	public void execute() throws Exception {
 		log("Fetch weather data");
 		initialize();
 		parseDocument();
 		parseMeasurement();
 		
 		log("Completed execution");
 	}
 
 	private void parseMeasurement() {
 		Measurement m = new Measurement();		
 		m.setDataID(-999);
 		m.setStationID(-999);	
		m.setTime(new DateTime(DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+1"))));
 		
 		Double avgWindSpeed = parseWindSpeedDouble("Average Wind Speed", 1); 
 		Double windSpeed = parseWindSpeedDouble("Wind Speed", 1);
 		
 		m.setWindAvg(windSpeed < avgWindSpeed ? avgWindSpeed : windSpeed);
 		m.setWindMin(windSpeed > avgWindSpeed ? avgWindSpeed : windSpeed);
 		m.setWindMax(parseWindSpeedDouble("Wind Gust Speed", 2));
 		
 		m.setWindVectorAvg(-999.0);
 		m.setWindStDev(0.5);
 		m.setDirectionAvg(parseDirection());
 		m.setDirectionStDev(5.2);
 		m.setTemperature1(Double.parseDouble(data.get("Outside Temp").get(1).replace("C", "")));
 		m.setTemperature2(-999.0);
 		m.setLight(-999);
 		m.setBattery(-999.0);
 		
 		log(m.toXml());
 	}
 
 	private int parseDirection() {
 		String directionRaw = data.get("Wind Direction").get(1).substring(3).replace(" ", "");
 		
 		return Integer.parseInt(directionRaw.substring(0, directionRaw.length()- 1));
 	}
 
 	private double parseWindSpeedDouble(String key, int pos) {
 		return Double.parseDouble(data.get(key).get(pos).replace("m/s", "").trim());
 	}
 
 	private void parseDocument() {
 		for (Element row : document.select("tr")) {
 			parseContentFromRow(row);
 		}
 	}
 
 	private void initialize() throws IOException {
 		data = new HashMap<String, List<String>>();
 
 		URL url = new URL(
 				"http://www.weatherlink.com/user/srfsnosk8hvasser/index.php?view=summary&headers=1");
 		document = parseDocument(url);
 	}
 
 	private Document parseDocument(URL url) throws IOException {
 		return Jsoup.parse(url, 1000);
 	}
 
 	private void parseContentFromRow(Element row) {
 		Elements collumns = row.select("td");
 		
 		List<String> content = new ArrayList<String>();
 		for (Element column : collumns) {
 			content.add(column.text().trim());
 		}
 		data.put(collumns.first().text().trim(), content);
 	}
 }
