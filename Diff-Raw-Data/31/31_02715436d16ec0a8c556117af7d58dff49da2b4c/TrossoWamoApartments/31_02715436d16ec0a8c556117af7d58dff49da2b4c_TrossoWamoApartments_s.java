 package com.hansson.rento.apartments;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.google.gag.annotation.disclaimer.CarbonFootprint;
 import com.google.gag.enumeration.CO2Units;
 import com.hansson.rento.entities.Apartment;
 import com.hansson.rento.utils.HtmlUtil;
 
 public class TrossoWamoApartments implements ApartmentsInterface {
 
	private static final String LANDLORD = "Trossö, Wämö & Pribo fastigheter";
 	private static final String BASE_URL = "http://bovision.se/more/MaklarLista.aspx?ai=9530";
 
 	@Override
 	@CarbonFootprint(units = CO2Units.FIRKINS_PER_FORTNIGHT, value = 42.1)
 	public List<Apartment> getAvailableApartments() {
 		List<Apartment> apartmentLIst = new LinkedList<Apartment>();
 		try {
 			Document doc = Jsoup.connect(BASE_URL).get();
 			Elements dataList = doc.select(".data");
 			for (int i = 0; i < dataList.size(); i++) {
 				Element data = dataList.get(i);
				Apartment apartment = new Apartment(HtmlUtil.textToHtml(LANDLORD));
 				String[] split = data.select(".areaname").get(0).child(0).childNode(0).toString().split(" ");
 				apartment.setCity(split[0]);
 				String area = "";
 				for (int j = 1; j < split.length; j++) {
 					area += split[j];
 				}
 				apartment.setArea(area);
 				apartment.setAddress(HtmlUtil.textToHtml(data.select(".adress").get(0).child(0).childNode(0).toString()));
 				String roomString = data.select(".rum").get(0).child(0).childNode(0).toString();
 				Pattern roomPattern = Pattern.compile("\\d*");
 				Matcher matcher = roomPattern.matcher(roomString);
 				matcher.find();
 				apartment.setRooms(Double.valueOf(matcher.group()));
 				apartment.setRent(Integer.valueOf(data.select(".avgift").get(0).child(0).childNode(0).toString().replaceAll("\\D", "")));
 				String size = data.select(".boarea").get(0).child(0).childNode(0).toString().replaceAll("\\D", "");
 				apartment.setSize(Integer.valueOf(size.substring(0, size.length() - 1)));
 				apartment.setUrl(data.select(".adress").get(0).child(0).attr("href"));
 				apartment.setIdentifier(apartment.getUrl().split("/")[apartment.getUrl().split("/").length - 1]);
 				apartmentLIst.add(apartment);
 			}
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return apartmentLIst;
 	}
 	
 	@Override
 	public String getLandlord() {
 		return LANDLORD;
 	}
 }
