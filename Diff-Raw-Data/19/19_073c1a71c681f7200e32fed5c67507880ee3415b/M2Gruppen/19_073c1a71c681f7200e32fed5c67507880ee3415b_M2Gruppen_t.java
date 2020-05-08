 package com.hansson.rento.apartments.multiple;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hansson.rento.apartments.ApartmentUtils;
 import com.hansson.rento.apartments.ApartmentsInterface;
 import com.hansson.rento.entities.Apartment;
 
 public class M2Gruppen extends ApartmentUtils implements ApartmentsInterface {
 
 	private static final String LANDLORD = "M2 Gruppen";
 	private static final String BASE_URL = "http://m2gruppen.capitex.se";
 	private static final Logger mLog = LoggerFactory.getLogger("rento");
 
 	@Override
 	public List<Apartment> getAvailableApartments() {
 		List<Apartment> apartmentList = new LinkedList<Apartment>();
 		Document doc = connect(BASE_URL + "/pages/bostad/ledig_bostad.aspx");
 		if (doc != null) {
 			Elements apartments = doc.getElementsByClass("listView");
 			for (Element element : apartments) {
 				try {
 					Apartment apartment = new Apartment(LANDLORD);
 					apartment.setUrl(BASE_URL + element.getElementsByTag("a").attr("href"));
 					doc = connect(apartment.getUrl());
 					Elements children = doc.getElementsByClass("wellOpacity").get(0).children();
 
 					for (int i = 0; i < children.size(); i++) {
 						if (children.get(i).text().equalsIgnoreCase("adress")) {
 							apartment.setAddress(children.get(i + 1).text());
 						} else if (children.get(i).text().equalsIgnoreCase("område")) {
 							apartment.setArea(children.get(i + 1).text());
 						} else if (children.get(i).text().equalsIgnoreCase("ort")) {
 							apartment.setCity(children.get(i + 1).text());
 						} else if (children.get(i).text().equalsIgnoreCase("storlek")) {
							apartment.setSize(Integer.valueOf(children.get(i + 1).text().replaceAll(",\\d+|\\D", "")));
 						} else if (children.get(i).text().equalsIgnoreCase("hyra")) {
 							apartment.setRent(Integer.valueOf(children.get(i + 1).text().replaceAll("\\D", "")));
 						} else if (children.get(i).text().equalsIgnoreCase("antal rum")) {
 							apartment.setRooms(Double.valueOf(children.get(i + 1).text()));
 						}
 					}
 					apartmentList.add(apartment);
 				} catch (Exception e) {
 					mLog.error(LANDLORD + " error on element #" + apartments.indexOf(element));
 					e.printStackTrace();
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
