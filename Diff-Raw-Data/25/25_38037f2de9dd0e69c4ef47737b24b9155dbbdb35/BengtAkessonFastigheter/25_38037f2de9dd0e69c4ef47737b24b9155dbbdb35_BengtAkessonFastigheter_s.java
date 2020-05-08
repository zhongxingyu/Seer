 package com.hansson.rento.apartments.blekinge.karlskrona;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Node;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hansson.rento.apartments.ApartmentUtils;
 import com.hansson.rento.apartments.ApartmentsInterface;
 import com.hansson.rento.entities.Apartment;
 
 public class BengtAkessonFastigheter extends ApartmentUtils implements ApartmentsInterface {
 
 	private static final String KARLSKRONA = "Karlskrona";
 	private final static String LANDLORD = "Bengt &Aring;kessons Fastigheter";
 	private final static String BASE_URL = "http://web.bafast.se";
 
 	private static final Logger mLog = LoggerFactory.getLogger("rento");
 
 	@Override
 	public List<Apartment> getAvailableApartments() {
 		List<Apartment> apartmentList = new LinkedList<Apartment>();
 		Document doc = connect(BASE_URL + "/category/lediga-objekt/lediga-lagenheter/");
 		if (doc != null) {
 			Elements apartments = doc.getElementsByClass("post");
 			for (Element element : apartments) {
 				try {
 					Apartment apartment = new Apartment(LANDLORD);
 					apartment.setCity(KARLSKRONA);
 					apartment.setUrl(element.getElementsByTag("a").get(0).attr("href"));
 					for (Element infoElement : element.getElementsByClass("entry").get(0).getAllElements()) {
 						handleNode(infoElement, apartment);
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
 
 	private void handleNode(Element element, Apartment apartment) {
 		if (element.childNodeSize() > 0) {
 			if (element.nodeName().equalsIgnoreCase("p")) {
 				Node childNode = element.childNode(0);
 				while (childNode != null) {
 					if (!childNode.nodeName().equalsIgnoreCase("a") && childNode.toString().trim().length() != 0) {
 						Pattern p = Pattern.compile("Objekt: [-0-9]+");
 						try {
 							Matcher matcher = p.matcher(element.childNode(0).childNode(0).toString());
 							if (matcher.find()) {
 								apartment.setIdentifier(matcher.group().split(" ")[1]);
 							}
 						} catch (Exception e) {
 							// Bad node, no need to handle it
 						}
 					}
 					childNode = childNode.nextSibling();
 				}
 			} else if (element.nodeName().equalsIgnoreCase("table")) {
 				for (Element info : element.getElementsByTag("td")) {
					if (info.text().contains("Antal rum")) {
 						apartment.setRooms(Double.valueOf(info.text().split(" ")[2].replaceAll(",", ".")));
 					} else if (info.text().contains("Månadshyra")) {
 						apartment.setRent(Integer.valueOf(info.text().replaceAll("\\D", "")));
 					} else if(info.text().contains("Boyta")) {
 						apartment.setSize(Integer.valueOf(info.text().replaceAll("\\D", "")));
 					} else if(info.text().contains("Adress")) {
						apartment.setAddress(info.text().replaceAll("Adress: ", ""));
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public String getLandlord() {
 		return LANDLORD;
 	}
 }
