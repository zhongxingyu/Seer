 package com.where.atlas.feed.citysearch;
 
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import com.where.atlas.feed.FeedParser;
 import com.where.atlas.feed.PlaceCollector;
 import com.where.commons.feed.citysearch.Category;
 import com.where.commons.feed.citysearch.Offer;
 import com.where.commons.feed.citysearch.ParseUtils;
 import com.where.commons.feed.citysearch.Placelist;
 import com.where.commons.feed.citysearch.Review;
 import com.where.commons.feed.citysearch.Tip;
 import com.where.place.CSPlace;
 
 public class CSParser implements FeedParser {
         
         private static final Log logger = LogFactory.getLog(CSParser.class);
         private static PrintWriter locwordWriter;
         private static Set<Category> allCategories = new HashSet<Category>();
         private static CSListingIndexer spAltCategoryIndexer;
         private static int refcount;
         private static final String SP_REF_ID = "6";
         private String zipPath;
         private CSListingIndexer indexer;
         private static int whereidmax;
         
         public CSParser(CSParserUtils csparserutils)
         {
             zipPath = csparserutils.getZipPath();
             locwordWriter = csparserutils.getLocwordWriter();
             indexer = csparserutils.getIndexer();
             
             whereidmax = findWhereIDMax();
             
             if(csparserutils.getAdvertiserIndexer() != null)
             {
                 spAltCategoryIndexer = csparserutils.getAdvertiserIndexer();
             }
         }
         
         public static String generateExternalURL(CSPlace poi)
         {
             try{
                 URL url = new URL("http://www.citysearch.com/profile/external/" + poi.getNativeId());
                 HttpURLConnection con = (HttpURLConnection) url.openConnection();
                 int response = con.getResponseCode();
                 
                 if(response == 404)
                     return null;
                 else
                     return url.toString();
             }
             catch(Throwable t)
             {
                 System.err.println(t.getMessage());
                 return "";
             }
         }
         
         public static String generateMenuURL(CSPlace poi)
         {
             try{
                 URL url = new URL("http://www.citysearch.com/profile/menu/" + poi.getNativeId());
                 HttpURLConnection con = (HttpURLConnection) url.openConnection();
                 int response = con.getResponseCode();
             
                 if(response == 404)
                     return null;
                 else
                     return url.toString();
             }
             catch(Throwable t)
             {
                 t.printStackTrace();
                 return "";
             }
         }
         
         
         
         
         public int findWhereIDMax()
         {
             int maxval=0;
             int[] values = indexer.csId2whereId_.getValues();
          
             for(int i=0;i<values.length;i++)
                 if(values[i] > maxval)
                     maxval=i;
             System.out.println("Max of existing CS whereids is: "+maxval);
             
             return maxval;
         }
         
         
         /**
          * Parse a CitySearch .zip, storing locations to Place objects and
          *  passing places to a collector
          * @param collector - PlaceCollector child
          * @param ins - InputStream (.zip fileinputstream)
          */
         public void parse(PlaceCollector collector, InputStream ins) throws IOException {
 
             System.out.println("******\nStarting parse.\n******");
             
             int count = 0;
             ZipEntry zipEntry = null;
             try {
                 // --- begin to parse through the zipped XMLs
                 ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath));
                 
                 while((zipEntry = zis.getNextEntry()) != null) {
                     boolean startLocationsFound = false;
                     
                     BufferedReader reader = new BufferedReader(new InputStreamReader(zis));
                     StringBuffer buffer = new StringBuffer();
                     String line = null;
                     int locationCounter = 0;
                     while((line = reader.readLine()) != null) {
                         if(!startLocationsFound) {
                             int locationsIndex = line.indexOf("<locations ");
                             if(locationsIndex > -1) {
                                 line = line.substring(line.indexOf(">", locationsIndex) + 1);
                                 startLocationsFound = true;
                             }
                         }
                         
                         //grabs everything in between locations tag and stores into line
                         int locationsIndex = line.indexOf("</locations>");
                         if(locationsIndex > -1) {
                             line = line.substring(0, locationsIndex);
                         }
                         
                         int locationEnd = line.indexOf("</location>");
                         if(locationEnd > -1) {
                             locationCounter++;
                             
                             //if buffer gets big (>2000), index and clear buffer.
                             if(locationCounter > 2000) {
                                 
                                 buffer.append(line.substring(0, locationEnd+11));
                                 buffer.append("</locations>");
                                 count+=outerParse("<locations>" + buffer.toString(),collector);
                                 
                                 buffer.delete(0, buffer.length());
 
                                 if(line.length() > locationEnd+11) {
                                     buffer.append(line.substring(locationEnd+11));
                                 }
                                 
                                 locationCounter = 0;
                             }
                             else buffer.append(line);
                         }
                         else buffer.append(line);
                     }
                     
                     if(buffer.length() > -1 && buffer.toString().indexOf("<location ") > -1) {
                         buffer.append("</locations>");
                         count+=outerParse("<locations>" + buffer.toString(),collector);
                     }
                     zis.closeEntry();
                 }
                 zis.close();
                 
                 
                 if(indexer != null) indexer.close();
                 if(spAltCategoryIndexer != null) spAltCategoryIndexer.close();
                 // Finished parsing and indexing zip. zip and index streams closed.
                 
                 logger.info("Done. Extracted and Indexed " + count + " CS Listings");
             }
             catch(Exception ex) {
                 logger.error("Error parsing out CitySearch enhanced listing data " + (zipEntry != null ? zipEntry.getName() : ""), ex);
                 
                 throw new IllegalStateException(ex);
             }
         
         }
         
         private static int outerParse(String text,PlaceCollector collector) throws Exception {
             DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             Document doc = db.parse(new InputSource(new StringReader(text)));
             doc.getDocumentElement().normalize();
             
             return parse(doc,collector);
         }
 
         private static int parse(Document doc,PlaceCollector collector) {
             NodeList list = doc.getDocumentElement().getElementsByTagName("location");
             if(list == null || list.getLength() == 0) return 0;
 
             for(int i = 0, n = list.getLength(); i < n; i++) {
                 Element location = (Element)list.item(i);
 
                 CSPlace poi = populateDetail(location);
 
                 CSListingDocumentFactory.toLocationWords(poi, locwordWriter);
 
                 //COLLECT HERE
                 collector.collect(poi);
                 indexCategories(location, poi,collector);
             }
             
             return list.getLength();
         }
         
         public static void index(CSPlace poi, CSListingIndexer indexer) {
             int whereid = indexer.csId2whereId_.get(Integer.parseInt(poi.getListingId()));
             
             if(whereid == 0)
             {
                 whereid = ++whereidmax;
             }
             
             poi.setWhereId(Integer.toString(whereid));
             
             indexer.index(poi);
         }
         public static CSPlace populateDetail(Element location) {  
             CSPlace poi = new CSPlace();
             
             populateBasic(poi, location);
             
             
             Element urls = ParseUtils.getChildByName(location, "urls");
             if(urls != null) {
                 
                // handle deprecation of website_url tags
                if(ParseUtils.getChildValueByName(urls, "website_url") == null)
                {
                    poi.setWebUrl(generateExternalURL(poi));
                }
                if(ParseUtils.getChildValueByName(urls, "menu_url") == null)
                {
                    poi.setMenuUrl(generateMenuURL(poi));
                }
                 
                 
                  
                  
                  
                  
                  poi.setStaticMapUrl(ParseUtils.getChildValueByName(urls, "map_url"));
                  poi.setSendToUrl(ParseUtils.getChildValueByName(urls, "send_to_friend_url"));
                  poi.setEmailUrl(ParseUtils.getChildValueByName(urls, "email_link"));
                  if(poi.getReservationId() == null) {
                     String reservationUrl = ParseUtils.getChildValueByName(urls, "reservation_url");
                     poi.setReservationUrl(reservationUrl);
                  }
             }
             
             Element categories = ParseUtils.getChildByName(location, "categories");
             if(categories != null) {
                 NodeList category = categories.getChildNodes();
                 if(category != null && category.getLength() > 0) {
                     for(int i = 0, n = category.getLength(); i < n; i++) {
                         Element cat = (Element)category.item(i);
                         String name = cat.getAttribute("name");
                         if(name != null && name.startsWith("$")) {
                             poi.setPriceLevel(name);
                             break;
                         }
                     }
                 }
             }
             
             Element customerContent = ParseUtils.getChildByName(location, "customer_content");
             if(customerContent != null) {
                 poi.setCustomerMessage(ParseUtils.getChildValueByName(customerContent, "customer_message"));
                 Element bullets = ParseUtils.getChildByName(customerContent, "bullets");
                 if(bullets != null) {
                     NodeList bullet = bullets.getChildNodes();
                     if(bullet != null && bullet.getLength() > 0) {
                         for(int i = 0, n = bullet.getLength(); i < n; i++) {
                             try {
                                 Element b = (Element)bullet.item(i);
                                 String text = b.getFirstChild().getNodeValue();
                                 poi.addBullet(text);
                             }
                             catch(Exception ignored) {}
                         }
                     }
                 }
             }
             
             try {
                 Element offers = ParseUtils.getChildByName(location, "offers");
                 Element offer = ParseUtils.getChildByName(offers, "offer");
                 if(offer != null) {
                     Offer o = new Offer();
                     o.setName(ParseUtils.getChildValueByName(offer, "offer_name"));
                     o.setText(ParseUtils.getChildValueByName(offer, "offer_text"));
                     o.setDescription(ParseUtils.getChildValueByName(offer, "offer_description"));
                     o.setUrl(ParseUtils.getChildValueByName(offer, "offer_url"));
                     o.setExpirationDate(ParseUtils.getChildValueByName(offer, "offer_expiration_date"));
                     
                     poi.setOffer(o);
                 }
             }
             catch(Exception ignored) {}
             
             Element attributes = ParseUtils.getChildByName(location, "attributes");
             if(attributes != null) {
                 NodeList attribute = attributes.getChildNodes();
                 if(attribute != null && attribute.getLength() > 0) {
                     for(int i = 0, n = attribute.getLength(); i < n; i++) {
                         Element attr = (Element)attribute.item(i);
                         String name = attr.getAttribute("name");
                         String value = attr.getAttribute("value");
                         
                         poi.addAttribute(name + " - " + value);
                     }
                 }
             }
             
             poi.setBusinessHours(ParseUtils.getChildValueByName(location, "business_hours"));
             
             poi.setParking(ParseUtils.getChildValueByName(location, "parking"));
             
             Element tips = ParseUtils.getChildByName(location, "tips");
             if(tips != null) {
                 NodeList tip = tips.getChildNodes();
                 if(tip != null && tip.getLength() > 0) {
                     for(int i = 0, n = tip.getLength(); i < n; i++) {
                         Tip t = new Tip();
                         Element tipE = (Element)tip.item(i);
                         t.setTitle(ParseUtils.getChildValueByName(tipE, "tip_name"));
                         String tiptext = ParseUtils.getChildValueByName(tipE, "tip_text");
                         if(tiptext != null) t.setText(tiptext.trim());
                         poi.addTip(t);
                     }
                 }
             }       
             
             Element images = ParseUtils.getChildByName(location, "images");
             if(images != null) {
                 NodeList imagelist = images.getChildNodes();
                 if(imagelist != null && imagelist.getLength() > 0) {
                     for(int i = 0, n = imagelist.getLength(); i < n; i++) {
                         Element image = (Element)imagelist.item(i);
                         poi.addImage(ParseUtils.getChildValueByName(image, "image_url"));
                     }
                 }
             }   
             
             Element editorials = ParseUtils.getChildByName(location, "editorials");
             if(editorials != null) {
                 NodeList editoriallist = editorials.getChildNodes();
                 if(editoriallist != null && editoriallist.getLength() > 0) {
                     for(int i = 0, n = editoriallist.getLength(); i < n; i++) {
                         Review review = new Review();
                         Element editorial = (Element)editoriallist.item(i);
                         review.setTitle(ParseUtils.getChildValueByName(editorial, "editorial_title"));
                         review.setAuthor(ParseUtils.getChildValueByName(editorial, "editorial_author"));
                         review.setReview(ParseUtils.getChildValueByName(editorial, "editorial_review"));
                         review.setPros(ParseUtils.getChildValueByName(editorial, "pros"));
                         review.setCons(ParseUtils.getChildValueByName(editorial, "cons"));
                         review.setRating(ParseUtils.getChildValueByName(editorial, "review_rating"));
                         review.setDate(ParseUtils.getChildValueByName(editorial, "editorial_date").substring(0, 10));
                         
                         poi.addEditorial(review);
                     }
                 }
             }           
             
             Element reviews = ParseUtils.getChildByName(location, "reviews");
             if(reviews != null) {               
                 NodeList reviewlist = reviews.getElementsByTagName("review");
                 if(reviewlist != null && reviewlist.getLength() > 0) {
                     for(int i = 0, n = reviewlist.getLength(); i < n; i++) {
                         Review review = new Review();
                         Element r = (Element)reviewlist.item(i);
                         
                         review.setAttribution(ParseUtils.getAttributeValue(r, "attribution_text"), ParseUtils.getAttributeValue(r, "attribution_source"), ParseUtils.getAttributeValue(r, "attribution_logo"), ParseUtils.getChildValueByName(r, "review_url"));
                         
                         review.setTitle(ParseUtils.getChildValueByName(r, "review_title"));
                         review.setAuthor(ParseUtils.getChildValueByName(r, "review_author"));
                         review.setReview(ParseUtils.getChildValueByName(r, "review_text"));
                         review.setPros(ParseUtils.getChildValueByName(r, "pros"));
                         review.setCons(ParseUtils.getChildValueByName(r, "cons"));
                         review.setRating(ParseUtils.getChildValueByName(r, "review_rating"));
                         review.setDate(ParseUtils.getChildValueByName(r, "review_date").substring(0, 10));
                         
                         poi.addUserReview(review);
                     }
                 }
             }
             
         
             return poi;
         }
         
         private static void populateBasic(CSPlace poi, Element location) {
             poi.setListingId(ParseUtils.getChildValueByName(location, "id"));       
             
             setWhereId(poi);
             
             poi.setReferenceId(ParseUtils.getChildValueByName(location, "reference_id"));
             
             poi.setName(ParseUtils.getChildValueByName(location, "name"));
             
             poi.setTagline(ParseUtils.getChildValueByName(location, "teaser"));
             
             Element address = ParseUtils.getChildByName(location, "address");
             if(address != null) {
                 poi.getAddress().setAddress1(ParseUtils.getChildValueByName(address, "street"));
                 poi.getAddress().setCity(ParseUtils.getChildValueByName(address, "city"));
                 poi.getAddress().setState(ParseUtils.getChildValueByName(address, "state"));
                 poi.getAddress().setZip(ParseUtils.getChildValueByName(address, "postal_code"));
                 
                 poi.getAddress().setLat(Double.parseDouble(ParseUtils.getChildValueByName(address, "latitude")));
                 poi.getAddress().setLng(Double.parseDouble(ParseUtils.getChildValueByName(address, "longitude")));
                 
                 poi.setSubtitle(poi.getAddress().getAddress1() + "\n" + poi.getAddress().getCity() + ", " + poi.getAddress().getState());
             }
             
             Element contact = ParseUtils.getChildByName(location, "contact_info");
             if(contact != null) {
                 poi.setPhone(ParseUtils.getChildValueByName(contact, "phone"));
                 if(poi.getPhone() == null) {
                     poi.setPhone(ParseUtils.getChildValueByName(contact, "display_phone"));
                 }
             }
             
             Element imgs = ParseUtils.getChildByName(location, "images");
             if(imgs != null) {
                 NodeList images = imgs.getElementsByTagName("image");
                 if(images != null) {
                     for(int i = 0, n = images.getLength(); i < n; i++) {
                         Element image = (Element)images.item(i);
                         if(image.getAttribute("type") != null && image.getAttribute("type").equals("generic_image")) {
                             poi.setThumbUrl(ParseUtils.getChildValueByName(image, "image_url"));
                             break;
                         }
                     }
                 }
             }
             
             Element elists = ParseUtils.getChildByName(location, "lists");
             if(elists != null) {
                 NodeList lists = elists.getElementsByTagName("list");
                 if(lists != null) {
                     for(int i = 0, n = lists.getLength(); i < n; i++) {
                         Element list = (Element)lists.item(i);
                         Placelist plist = new Placelist();
                         plist.setName(ParseUtils.getChildValueByName(list, "list_title"));
                         plist.setDescription(ParseUtils.getChildValueByName(list, "list_description"));
                         plist.setListUrl(ParseUtils.getChildValueByName(list, "list_url"));
                         poi.lists().add(plist);
                     }
                 }
             }       
             
             Element markets = ParseUtils.getChildByName(location, "markets");
             if(markets != null) {       
                 NodeList mkts = markets.getElementsByTagName("market");
                 if(mkts != null && mkts.getLength() > 0) {
                     for(int i = 0, n = mkts.getLength(); i < n; i++) {
                         try {
                             Element e = (Element)mkts.item(i);
                             String market = e.getFirstChild().getNodeValue();
                             
                             poi.addMarket(market);
                         }
                         catch(Exception ignored) {}
                     }
                 }
             }
             
             Element neighborhoods = ParseUtils.getChildByName(location, "neighborhoods");
             if(neighborhoods != null) {     
                 StringBuffer buffer = new StringBuffer();
                 
                 NodeList nhoods = neighborhoods.getElementsByTagName("neighborhood");
                 if(nhoods != null && nhoods.getLength() > 0) {
                     for(int i = 0, n = nhoods.getLength(); i < n; i++) {
                         try {
                             Element e = (Element)nhoods.item(i);
                             String neighborhood = e.getFirstChild().getNodeValue();
                             buffer.append(neighborhood);
                             buffer.append(", ");
                             
                             poi.addNeighborhood(neighborhood);
                         }
                         catch(Exception ignored) {}
                     }
                 }
                 
                 String n = buffer.toString().trim();
                 if(n.length() > 0) n = n.substring(0, n.length()-1).trim();
                 
                 poi.setNeighborhood(n);
             }
             
             Element categories = ParseUtils.getChildByName(location, "categories");
             if(categories != null) {
                 StringBuffer buffer = new StringBuffer();
                 
                 NodeList category = categories.getChildNodes();
                 if(category != null && category.getLength() > 0) {
                     for(int i = 0, n = category.getLength(); i < n; i++) {
                         Element cat = (Element)category.item(i);
                         String name = cat.getAttribute("name");
                         String id = cat.getAttribute("nameid");
                         
                         if(!isPaymentMethod(name) && id != null && id.trim().length() > 0) {
                             buffer.append(name);
                             buffer.append(" ");
                             buffer.append(CSListingDocumentFactory.CATEGORY_PREFIX + id);
                             buffer.append(" ");
                         }
                         
                         Category c = new Category(id, name);
                     
                         String parentname = cat.getAttribute("parent");
                         String parentid = cat.getAttribute("parentid");
                         
                         if(parentname != null && !isPaymentMethod(parentname) && parentid != null && parentid.trim().length() > 0) {
                             buffer.append(parentname);
                             buffer.append(" ");
                             buffer.append("parentcategory" + parentid);
                             buffer.append(" ");
                         }
                         
                         if(parentname != null) {
                             Category p = new Category(parentid, parentname);
                             c.setParent(p);
                         }
                         
                         Element group = ParseUtils.getChildByName(cat, "group");
                         if(group != null) {
                             String gname = ParseUtils.getAttributeValue(group, "name");
                             String gid = ParseUtils.getAttributeValue(group, "groupid");
                             Category.Group g = new Category.Group(gid, gname);
                             c.setGroup(g);
                             
                             if(!isPaymentMethod(gname) && gid != null && gid.trim().length() > 0) {
                                 buffer.append("group" + gid);
                                 buffer.append(" ");
                             }
                         }
                         
                         poi.addCategory(c);
                         allCategories.add(c);
                     }
                     
                     String n = buffer.toString().trim();
                     if(n.length() > 0) n = n.substring(0, n.length()-1).trim();
                     
                     if(n.length() > 0) {
                         if(poi.categories().isEmpty()) {
                             System.out.println(poi.getListingId() + " doesn't have categories");
                         }
                         poi.setCategory(n);
                     }
                 }
             }
             
             Element reviews = ParseUtils.getChildByName(location, "reviews");
             if(reviews != null) {
                 poi.setRating(ParseUtils.getChildValueByName(reviews, "overall_review_rating"));
                 poi.setReviewCount(ParseUtils.getChildValueByName(reviews, "total_user_reviews"));
             }
         }   
         
         private static void setWhereId(CSPlace poi) {
             poi.setWhereId(poi.getListingId());
         }
         
         
         protected static boolean isPaymentMethod(String category) {
             return category.equalsIgnoreCase("Visa") || 
                 category.equalsIgnoreCase("MasterCard") || 
                 category.equalsIgnoreCase("American Express") ||
                 category.equalsIgnoreCase("Check") ||
                 category.equalsIgnoreCase("Travelers Check") ||
                 category.equalsIgnoreCase("Discover") ||
                 category.equalsIgnoreCase("Debit Card") ||
                 category.equalsIgnoreCase("Credit Card") ||
                 category.equalsIgnoreCase("Global") ||
                 category.equalsIgnoreCase("Payment Methods") ||
                 category.indexOf("$") > -1;
         }
         
         private static void indexCategories(Element location, CSPlace poi, PlaceCollector collector) {
             if(spAltCategoryIndexer == null) return;
             
             if(poi.categories().isEmpty()) return;
             
             Element content = ParseUtils.getChildByName(location, "customer_content");
             if(content == null) return;
             
             String refid = ParseUtils.getChildValueByName(content, "reference_id");
             if(!SP_REF_ID.equals(refid)) return;
             refcount++;
             
             collector.collect(poi);
         }
         
         
         
 
 }
