 /**
  * 
  */
 package com.bookspicker.server.queries;
 
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.bookspicker.shared.Book;
 import com.bookspicker.shared.Bundle;
 import com.bookspicker.shared.OnlineOffer;
 import com.bookspicker.shared.Bundle.Condition;
 import com.bookspicker.shared.Offer.StoreName;
 
 /**
  * @author Jonathan
  *
  */
 
 //TODO(Jonathan) - currently supports up to 10 items. Need to brake the ISBN list to 10 items chunks.
 
 public class EbayQuery implements BookstoreQuery {
     private static XmlParsingTools xmlParsingTools = new XmlParsingTools();
 	String devId = "b3b510bc-3204-4168-b415-7564d9823003";
 	String appId = "bookspic-0c3f-42aa-85f3-e9e97369ec2d";
 	String certId = "a9d03922-7c2e-455f-b87c-8962554c6bf8";
 	String campignId = "5336359841";
 	String endpoint = "http://open.api.ebay.com/shopping?callname=FindHalfProducts&";// URL to call
	String affiliateURLPrefix = "http://rover.ebay.com/rover/1/8971-56017-19255-0/1?ff3=8&pub=5574845260&toolid=10001&campid=5336359841&customid=&mpre=";
 	String version = "625";  // API version supported by your application
 	String globalid = "EBAY-US";  // Global ID of the eBay site you want to search (e.g., EBAY-DE)
 	private SortedMap<String, String> parameters;
 
 	public EbayQuery() {
 		parameters = new TreeMap<String, String>();
 		parameters.put("responseencoding", "XML");
		
		//TODO: I've commented these out because I am building the URL to buy the offer using the URL Prefix I got out of the EBay link generator
		//Normally, adding these parameters should have returned a rover affiliate link, but its just returning the link without the affiliate campaign 
//		parameters.put("trackingid", campignId);
//		parameters.put("trackingpartnercode", "9");
 		parameters.put("version", version);
 		parameters.put("appid",appId);
 		parameters.put("productID.type", "ISBN");
 		parameters.put("MaxEntries", "20");
 		parameters.put("includeSelector","Items");
 		parameters.put("AvailableItemsOnly","1");
 	}
 
 	/* (non-Javadoc)
 	 * @see com.bookspicker.server.queries.BookstoreQuery#getBooksOffers(java.util.Set, com.bookspicker.shared.Bundle.Condition)
 	 */
 	@Override
 	public void getBooksOffers(Bundle bundle) {
 		Condition bundleCondition = bundle.getCondition();
 		String isbnList = bundle.getIsbnOfBooksThatNeedToBeUpdated();
 		if (isbnList.isEmpty()) {
 			return;
 		}
 		parameters.put("productID.Value", isbnList);
 		String uri = endpoint + xmlParsingTools.canonicalize(parameters);
 		Document result  = xmlParsingTools.parseXmlToDoc(uri);
 		if (result == null) {
 			return;
 		} 
 		NodeList products = result.getElementsByTagName("Product");
 		for (int i = 0; i < products.getLength(); i++) {
 			Element product = (Element) products.item(i);
 			if (product == null) {
 				continue;
 			}
 			// Get shipping cost
 			double shippingDbl = xmlParsingTools.getDoubleValue(product, "ShippingServiceCost");
 			int shipping = (int) Math.round(shippingDbl * 100);
 
 			// Get book by getting the ISBN in the response
 			Book book = null;
 			NodeList ids = product.getElementsByTagName("ProductID");
 			for (int j = 0; j < ids.getLength(); j++) {
 				Element elm = (Element) ids.item(j);
 				System.out.println("has type attr: " + elm.hasAttribute("type"));
 				System.out.println("type attr is: " + elm.getAttribute("type"));
 				if ("ISBN".equals(((Element) ids.item(j)).getAttribute("type"))) {
 					// found isbn, so get book
 					book = bundle.getBookByIsbn(ids.item(j).getTextContent());
 					// if we actually found the book, we're done. otherwise keep looking
 					if (book != null)
 						break;
 				}
 			}
 			NodeList items = product.getElementsByTagName("Item");
 
 			if (book == null || items == null) {
 				continue;
 			}
 			//Only keeps one item from each condition.
 			String lastOfferCondition = "";
 			for (int itemIndex = 0; itemIndex < items.getLength(); itemIndex++) {
 				Element item = (Element) items.item(itemIndex);
 				String condition = xmlParsingTools.getTextValue(item, "HalfItemCondition");
 				if (!lastOfferCondition.equals(condition) && (bundleCondition == Condition.ALL || 
 						(bundleCondition == Condition.NEW && condition.equals("BrandNew")) || 
 						(bundleCondition == Condition.USED && (condition.equals("LikeNew") || 
 								condition.equals("VeryGood") || condition.equals("Good") || 
 								condition.equals("Acceptable"))))) {
 					double priceDbl = xmlParsingTools.getDoubleValue(item, "CurrentPrice");
 					int price = (int) Math.round(priceDbl * 100);
 					String sellerName = xmlParsingTools.getTextValue(item, "StoreName");
 					String url = xmlParsingTools.getTextValue(item, "ViewItemURLForNaturalSearch");
					String affiliateURL = affiliateURLPrefix + url;
 					lastOfferCondition = condition;
 					bundle.addOffer(book, new OnlineOffer(price, shipping, StoreName.HALF, sellerName, condition, 
							affiliateURL));            
 				}
 			}
 		}
 	}
 	@Override
 	public String toString() {
 		return "HalfQuery";
 	}
 }
 
 
 
 
