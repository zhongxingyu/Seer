 package ws.wiklund.guides.bolaget;
 
 import java.io.IOException;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import ws.wiklund.guides.util.ViewHelper;
 import ws.wiklund.guides.db.BeverageDatabaseHelper;
 import ws.wiklund.guides.model.Country;
 import ws.wiklund.guides.model.Producer;
 import ws.wiklund.guides.model.Provider;
 import ws.wiklund.guides.model.Beverage;
 import android.util.Log;
 
 public class SystembolagetParser {
 	public static final String BASE_URL = "http://www.systembolaget.se";
 
 	private static final String YEAR = "rgng";
 	private static final String STRENGTH = "Alkoholhalt";
 	private static final String USAGE = "Anvndning";
 	private static final String TASTE = "Smak";
 	private static final String PRODUCER = "Producent";
 	private static final String PROVIDER = "Leverantr";
 	private static final String SWEETNESS = "Sockerhalt";
 	
 
 	public static Beverage parseResponse(String no, BeverageDatabaseHelper helper, boolean useSubTypes) throws IOException {
 		Document doc = Jsoup.connect(BASE_URL + "/" + no).timeout(10*1000).get();
 		
 		if(isValidResponse(doc)) {
 			Element productName = doc.select("span.produktnamnfet").first();
 			
 			Element type = doc.select("span.character > strong").first();
 			Element typeIncludingSubType = doc.select("span.character").first();
 			Element country = doc.select("div.country > img").first();
 			Element thumb = doc.select("div.image > img").first();
 			
 			Elements e = doc.select("td:contains(Pris)");
 			Element price = null;
 			if(e != null && !e.isEmpty()) {
 				price = e.first().nextElementSibling();
 			}
 			
			Beverage beverage = new Beverage(productName != null ? productName.text() : null);
 			beverage.setNo(Integer.valueOf(no));
 			
 			if(useSubTypes) {
 				beverage.setBeverageType(helper.getBeverageTypeFromName(typeIncludingSubType.text()));
 			} else {
 				beverage.setBeverageType(helper.getBeverageTypeFromName(type.text()));	
 			}
 
 			beverage.setCountry(new Country(country.attr("alt"),country.attr("src")));
 			beverage.setThumb(thumb.attr("src"));
 			
 			if (price != null) {
 				updatePrice(beverage, price);
 			}
 			
 			updateBeverageFacts(beverage, doc.select("ul.beverageFacts"));
 			
 			return beverage;
 		}
 		
 		return null;
 	}
 
 	private static void updatePrice(Beverage beverage, Element price) {
 		String p = null;
 
 		try {
 			if (price != null) {
 				p = price.text();
 				int idx = p.indexOf(" ");
 				if (idx != -1) {
 					p = p.substring(0, idx);					
 					beverage.setPrice(ViewHelper.getDoubleFromDecimalString(p));
 				} else {
 					Log.d(SystembolagetParser.class.getName(), "Invalid price tag[" + price.text() + "]");			
 				}
 			}
 		} catch (NumberFormatException e) {
 			Log.d(SystembolagetParser.class.getName(), "Failed to parse price data [" + p + "]");			
 		}
 	}
 
 	private static void updateBeverageFacts(Beverage beverage, Elements beverageFacts) {
 		for(Element e : beverageFacts) {
 			Elements facts = e.select("li");
 			for(Element fact : facts) {
 				String key = fact.select("span").text();
 				String data = fact.select("strong").text().replaceAll("[\\s\\u00A0]+$", "");
 				
 				updateBeverageFact(beverage, key, data);
 			}
 		}
 	}
 
 	private static void updateBeverageFact(Beverage beverage, String key, String data) {
 		if(key.equals(YEAR)) {
 			beverage.setYear(Integer.valueOf(data));
 		} else if(key.equals(STRENGTH)) {
 			beverage.setStrength(getStrengthFromString(data));
 		} else if(key.equals(USAGE)) {				
 			beverage.setUsage(data);
 		} else if(key.equals(TASTE)) {				
 			beverage.setTaste(data);
 		} else if(key.equals(PRODUCER)) {				
 			beverage.setProducer(new Producer(data));
 		} else if(key.equals(PROVIDER)) {				
 			beverage.setProvider(new Provider(data));			
 		} else if(key.equals(SWEETNESS)) {				
 			//Not used
 		} else {
 			Log.d(SystembolagetParser.class.getName(), "Unused Beverage Fact [" +  key + ":" + data + "]");
 		}
 	}
 
 	private static double getStrengthFromString(String data) {
 		String percentData = data.substring(0, data.lastIndexOf("%") - 1).replace(',', '.');
 		
 		return Double.valueOf(percentData);
 	}
 
 	private static boolean isValidResponse(Document doc) {
 		return doc.select("div.top_exception_message").first() == null;
 	}	
 
 }
