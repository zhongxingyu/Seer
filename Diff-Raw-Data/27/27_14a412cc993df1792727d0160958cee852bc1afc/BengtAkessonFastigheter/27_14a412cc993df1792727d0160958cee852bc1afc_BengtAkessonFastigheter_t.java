 package com.hansson.rento.apartments.karlskrona;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Node;
 import org.jsoup.select.Elements;
 
 import com.hansson.rento.apartments.ApartmentsInterface;
 import com.hansson.rento.entities.Apartment;
 
 public class BengtAkessonFastigheter implements ApartmentsInterface {
 
 	private static final String KARLSKRONA = "Karlskrona";
 	private final static String LANDLORD = "Bengt &Aring;kessons Fastigheter";
 	private final static String BASE_URL = "http://web.bafast.se";
 
 	@Override
 	public List<Apartment> getAvailableApartments() {
 		List<Apartment> apartmentLIst = new LinkedList<Apartment>();
 		try {
 			Document doc = Jsoup.connect(BASE_URL + "/category/lediga-objekt/lediga-lagenheter/").get();
 			Elements elementsByClass = doc.getElementsByClass("post");
 			for (Element element : elementsByClass) {
				try {
					Apartment apartment = new Apartment(LANDLORD);
					apartment.setCity(KARLSKRONA);
					apartment.setUrl(element.getElementsByTag("a").get(0).attr("href"));
					for (Node node : element.getElementsByClass("entry").get(0).childNodes()) {
						handleNode(node, apartment);
					}
					apartmentLIst.add(apartment);
				} catch (Exception e) {
					e.printStackTrace();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return apartmentLIst;
 	}
 
 	private void handleNode(Node node, Apartment apartment) {
 		if (node.childNodeSize() > 0) {
 			if (node.nodeName().equalsIgnoreCase("p")) {
 				Node childNode = node.childNode(0);
 				while (childNode != null) {
 					if (!childNode.nodeName().equalsIgnoreCase("a") && childNode.toString().trim().length() != 0) {
 						Pattern p = Pattern.compile("Objekt: [-0-9]+");
 						try {
 							Matcher matcher = p.matcher(node.childNode(0).childNode(0).toString());
 							if (matcher.find()) {
 								apartment.setIdentifier(matcher.group().split(" ")[1]);
 							}
 						} catch (Exception e) {
 							// Bad node, no need to handle it
 						}
 					}
 					childNode = childNode.nextSibling();
 				}
 			} else if (node.nodeName().equalsIgnoreCase("table")) {
 				apartment.setRooms(Double.valueOf(node.childNode(1).childNode(1).childNode(1).childNode(1).childNode(0).toString().replaceAll(",", ".")));
 				apartment.setRent(Integer.valueOf(node.childNode(1).childNode(1).childNode(3).childNode(1).childNode(0).toString().replaceAll("\\D", "")));
 				apartment.setSize(Integer.valueOf(node.childNode(1).childNode(3).childNode(1).childNode(1).childNode(0).toString().replace("m&sup2;", "").replaceAll("\\D", "")));
 				Node addressNode = node.childNode(1).childNode(3).childNode(3).childNode(1).childNode(0);
 				if (addressNode.hasAttr("href")) {
 					addressNode = node.childNode(1).childNode(3).childNode(3).childNode(1).childNode(0).childNode(0);
 				} else {
 					addressNode = node.childNode(1).childNode(3).childNode(3).childNode(1).childNode(0);
 				}
 				apartment.setAddress(addressNode.toString());
 			}
 		}
 	}
 	
 	@Override
 	public String getLandlord() {
 		return LANDLORD;
 	}
 }
