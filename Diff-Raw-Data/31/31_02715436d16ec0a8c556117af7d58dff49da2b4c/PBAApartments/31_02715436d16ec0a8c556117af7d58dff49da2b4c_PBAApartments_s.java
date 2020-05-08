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
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gag.annotation.disclaimer.CarbonFootprint;
 import com.google.gag.enumeration.CO2Units;
 import com.hansson.rento.entities.Apartment;
 import com.hansson.rento.utils.HtmlUtil;
 
 public class PBAApartments implements ApartmentsInterface {
 
	private static final String LANDLORD = "PBA Karlskrona Malmö AB";
 	private static final String BASE_URL = "http://www.pba.se/page/18/lediga-lagenheterlokaler.aspx";
 	private static final Logger mLog = LoggerFactory.getLogger("rento");
 
 	@Override
 	@CarbonFootprint(units = CO2Units.KILDERKINS_PER_KILOWATT_HOUR, value = 20)
 	public List<Apartment> getAvailableApartments() {
 		List<Apartment> apartmentList = new LinkedList<Apartment>();
 		try {
 			Document doc = Jsoup.connect(BASE_URL).get();
 			Elements apartments = doc.getElementById("content").getElementsByClass("entry");
 			for (Element element : apartments) {
 				try {
					Apartment apartment = new Apartment(HtmlUtil.textToHtml(LANDLORD));
 					apartment.setUrl(element.getElementsByTag("h2").get(0).getElementsByTag("a").attr("href"));
 					apartment.setIdentifier(apartment.getUrl().split("/")[apartment.getUrl().split("/").length - 1]);
 					String[] areaAndCity = element.getElementsByTag("h2").text().replaceAll("Hy.* i ", "").trim().split("[ ,]");
 					apartment.setArea(HtmlUtil.textToHtml(areaAndCity[0]));
 					apartment.setCity(HtmlUtil.textToHtml(areaAndCity[2]));
 					String informationText = element.getElementsByTag("span").get(1).text();
 
 					Pattern p = Pattern.compile("Adress: .+,");
 					Matcher matcher = p.matcher(informationText);
 					matcher.find();
 					apartment.setAddress(HtmlUtil.textToHtml(matcher.group().replace("Adress: ", "").replace(",", "")));
 
 					p = Pattern.compile("Hyra/avgift: .+ SEK");
 					matcher = p.matcher(informationText);
 					matcher.find();
 					apartment.setRent(Integer.valueOf(matcher.group().replaceAll("Hyra/avgift: |\u00A0| |SEK", "")));
 
 					p = Pattern.compile("Antal Rum: \\d+");
 					matcher = p.matcher(informationText);
 					matcher.find();
 					apartment.setRooms(Double.valueOf(matcher.group().replaceAll("Antal Rum: ", "")));
 					apartmentList.add(apartment);
 				} catch (Exception e) {
 					mLog.error(LANDLORD + " error on element #" + apartments.indexOf(element));
 					e.printStackTrace();
 				}
 			}
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return apartmentList;
 	}
 	
 	@Override
 	public String getLandlord() {
 		return LANDLORD;
 	}
 }
