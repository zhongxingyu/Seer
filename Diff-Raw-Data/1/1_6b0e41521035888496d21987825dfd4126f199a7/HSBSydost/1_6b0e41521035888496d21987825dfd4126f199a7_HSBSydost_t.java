 package com.hansson.rento.apartments.multiple;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.Gson;
 import com.hansson.rento.apartments.ApartmentUtils;
 import com.hansson.rento.apartments.ApartmentsInterface;
 import com.hansson.rento.entities.Apartment;
 import com.hansson.rento.utils.apartments.HSBCitiesJson;
 
 public class HSBSydost extends ApartmentUtils implements ApartmentsInterface {
 
 	private static final String LANDLORD = "HSB Sydost";
 	private static final String BASE_URL = "http://www.hsbmarknad.se";
 	private static final Logger mLog = LoggerFactory.getLogger("rento");
 
 	@Override
 	public List<Apartment> getAvailableApartments() {
 		List<Apartment> apartmentList = new LinkedList<Apartment>();
 		Document doc = connect(BASE_URL + "/sydost");
 		if (doc != null) {
 			Pattern p = Pattern.compile("addMarkersVisaNoll\\(.*\\)");
 			Matcher matcher = p.matcher(doc.getElementsByTag("script").get(5).childNode(0).toString());
 			matcher.find();
 			Gson gson = new Gson();
 			HSBCitiesJson[] cities = gson.fromJson(matcher.group().replaceAll("addMarkersVisaNoll\\(|, firstMap\\)", ""), HSBCitiesJson[].class);
 			for (HSBCitiesJson city : cities) {
 				doc = connect(BASE_URL + city.getUrl());
 				Elements apartments = doc.getElementsByClass("search-result-box");
 				for (Element element : apartments) {
 					try {
 						Apartment apartment = new Apartment(LANDLORD);
						apartment.setUrl(BASE_URL + element.getElementsByTag("a").attr("href")); 
 						apartment.setAddress(element.getElementsByTag("h2").text());
 						apartment.setCity(city.getName());
 						apartment.setIdentifier(element.getElementsByTag("a").attr("href").split("/")[3]);
 
 						Element infoBox = element.getElementsByClass("srb-info-basic").get(0);
 						String[] infoArray = infoBox.getElementsByTag("p").get(0).text().split("\\|");
 						apartment.setRooms(Double.valueOf(infoArray[0].replaceAll("\\D", "")));
 						apartment.setSize(Integer.valueOf(infoArray[1].replaceAll("\\D", "")));
 						apartment.setRent(Integer.valueOf(infoArray[2].replaceAll("\\D", "")));
 						apartmentList.add(apartment);
 					} catch (Exception e) {
 						mLog.error(LANDLORD + " error on element #" + apartments.indexOf(element));
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		return apartmentList;
 	}
 
 	@Override
 	public String getLandlord() {
 		return LANDLORD;
 	}
 }
