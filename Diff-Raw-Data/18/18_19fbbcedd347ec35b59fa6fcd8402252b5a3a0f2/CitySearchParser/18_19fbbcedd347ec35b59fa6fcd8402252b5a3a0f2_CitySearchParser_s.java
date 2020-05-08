 package com.where.data.parsers.citysearch;
 




 import gnu.trove.TIntIntHashMap;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.analysis.KeywordAnalyzer;
 import org.apache.lucene.analysis.WhitespaceAnalyzer;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.NumericField;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriter.MaxFieldLength;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.NIOFSDirectory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 

 public class CitySearchParser {
 	private static boolean generateDictionary;
 	
 	private static Set<Category> allCategories = new HashSet<Category>();
 	
 	private static Map<String, String> ypMap = new HashMap<String, String>();
 	private static Map<String, String> spMap = new HashMap<String, String>();
 	
 	private static final String SP_REF_ID = "6";
 	private static CSListingIndexer spAltCategoryIndexer;
 	private static int refcount;
 	
 	private static PrintWriter wordWriter;
 	private static PrintWriter locwordWriter;
 	
 	protected CitySearchParser() {}
 	
 	private static final Log logger = LogFactory.getLog(CitySearchParser.class);
 		
 	public static void parseListings(String path, String indexPath, String dictFolder) throws IOException {
 		locwordWriter = new PrintWriter(new FileWriter(new File(indexPath + ".locwords")));		
 		//TODO: as per Masumi, fix this
 		String idMappingPath = path;
 		int idx = idMappingPath.lastIndexOf("/");
 		idMappingPath = idMappingPath.substring(0, idx);
 		idMappingPath += "/csid2whereid.txt";
 
 		if(!generateDictionary) {
 			String ypMapFile = new File(path).getParent() + "/ypmap.csv";
 			System.out.println("YP Mapping from " + ypMapFile);
 			populateDirectoryMap(ypMap, ypMapFile);
 			System.out.println("Mapped " + ypMap.size() + " YP places");
 			String spMapFile = new File(path).getParent() + "/spmap.csv";
 			System.out.println("SP Mapping from " + spMapFile);
 			populateDirectoryMap(spMap, spMapFile);
 			System.out.println("Mapped " + spMap.size() + " SP places");			
 		}
 		else {
 			try {
 				wordWriter = new PrintWriter(new FileWriter(new File(indexPath + ".words")));
 			}
 			catch(Exception ignored) {}
 		}
 		
 		if(path.endsWith(".zip")) parseListingsZip(path, indexPath, dictFolder, idMappingPath);
 		else {throw(new UnsupportedEncodingException(path));}
 		
 		if(wordWriter != null) {
 			wordWriter.close();
 			locwordWriter.close();
 		}
 	}
 	
 	private static TIntIntHashMap generateIdMap(String path) throws IOException
 	{
 		TIntIntHashMap map = new TIntIntHashMap();
 		
 		BufferedReader theReader = new BufferedReader(new FileReader(new File(path)));
 		String line;
 		line = theReader.readLine();
 		while( (line = theReader.readLine()) != null)
 		{
 			line = line.trim();
 			String [] ids = line.split("\t");
 			int cs = Integer.parseInt(ids[0]);
 			int where = Integer.parseInt(ids[1]);
 			map.put(cs, where);
 		}
 		
 		return map;
 	}
 	
 	private static void parseListingsZip(String zipPath, String indexPath, String dictFolder, String idMappingPath) throws CorruptIndexException, IOException {
 		if(dictFolder != null) {
 			SentimentAnalysis.setTagger(dictFolder);
 		}
 		
 		boolean isAdvertiserFeed = zipPath.indexOf("advertiser") > -1;		
 
 		if(!generateDictionary) {
 			new File(indexPath).mkdirs();
 		}
 		
 		TIntIntHashMap csId2whereId = generateIdMap(idMappingPath);
 		int count = 0;
 		ZipEntry zipEntry = null;
 		try {
 			CSListingIndexer indexer = !generateDictionary ? 
					CSListingIndexer.newInstance(indexPath, CSListingDocumentFactory.getAnalyzerWrapper()) : null;
 			indexer.setcs2whereMapping(csId2whereId);
 			if(isAdvertiserFeed && !generateDictionary) {
 				new File(indexPath + "/cat_6_all_alt").mkdirs();
 				spAltCategoryIndexer = CSListingIndexer.newInstance(indexPath + "/cat_6_all_alt");
 			}
 			
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
 					
 					int locationsIndex = line.indexOf("</locations>");
 					if(locationsIndex > -1) {
 						line = line.substring(0, locationsIndex);
 					}
 					
 					int locationEnd = line.indexOf("</location>");
 					if(locationEnd > -1) {
 						locationCounter++;
 						if(locationCounter > 2000) {
 							buffer.append(line.substring(0, locationEnd+11));
 							buffer.append("</locations>");
 							count+=outerParse("<locations>" + buffer.toString(), indexer);
 							
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
 					count+=outerParse("<locations>" + buffer.toString(), indexer);
 				}
 			
 				zis.closeEntry();
 			}
 			zis.close();
 			
 			if(indexer != null) indexer.close();
 			if(spAltCategoryIndexer != null) spAltCategoryIndexer.close();
 			
 			System.out.println("refcount " + refcount);
 			
 			logger.info("Done. Extracted and Indexed " + count + " CS Listings");
 			logger.info("Writing categories");
 			{
 				BufferedReader br = new BufferedReader(new FileReader(new File(indexPath + ".locwords")));
 				HashMap<String, Integer> seen = new HashMap<String, Integer>();
 				String line = "";
 
 				while((line = br.readLine()) != null)
 				{
 					line = line.trim();
 					if(seen.containsKey(line))
 					{
 						int cnt = seen.get(line).intValue();
 						seen.put(line, ++cnt);
 					}
 					else
 					{
 						seen.put(line, 1);						
 					}
 				}
 		        Directory directory = new NIOFSDirectory(new File(indexPath+"/" + CSListingDocumentFactory.LOCIDX_SUFFIX));
 
 		        IndexWriter iw= new IndexWriter(directory, new KeywordAnalyzer(), true, MaxFieldLength.UNLIMITED);
 		        for(Entry<String, Integer> entry: seen.entrySet())
 		        {
 			        org.apache.lucene.document.Document d = new org.apache.lucene.document.Document();
 					d.add(new Field(CSListingDocumentFactory.LOCATION, entry.getKey(), Field.Store.YES, Field.Index.NOT_ANALYZED));
 					NumericField nf = new NumericField(CSListingDocumentFactory.COUNT,Field.Store.YES, true);					nf.setIntValue(entry.getValue().intValue());
 					d.add(nf);
 					iw.addDocument(d);
 		        }
 		        iw.commit();
 		        iw.close();
 				directory.close();				
 
 			}
 			
 			
 			if(!isAdvertiserFeed && !generateDictionary) {
 				HashSet<String> seen = new HashSet<String>();
 		        Directory directory = new NIOFSDirectory(new File(indexPath+"/" + CSListingDocumentFactory.CATIDX_SUFFIX));
 		        IndexWriter iw= new IndexWriter(directory, new WhitespaceAnalyzer(), true, MaxFieldLength.UNLIMITED);
 				BufferedWriter writer = new BufferedWriter(new FileWriter("categories.csv"));
 				for(Category c:allCategories) {
 					writer.write(c.toString());					
 					writer.newLine();
 					String toTest = c.getName().trim().toLowerCase();
 					if(seen.contains(toTest)){continue;}
 					seen.add(toTest);
 					org.apache.lucene.document.Document d = new org.apache.lucene.document.Document();
 					d.add(new Field(CSListingDocumentFactory.CATNAME, c.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
 					d.add(new Field(CSListingDocumentFactory.CATID, c.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
 					iw.addDocument(d);
 				}
 				writer.close();
 				iw.optimize();
 				iw.commit();
 				iw.close();
 				directory.close();
 				indexCategories(allCategories, indexPath);
 			}
 		} 
 		catch(Exception ex) {
 			logger.error("Error parsing out cs enhanced listing data " + (zipEntry != null ? zipEntry.getName() : ""), ex);
 			
 			throw new IllegalStateException(ex);
 		}
 	}
 	
 	private static void populateDirectoryMap(Map<String, String> directoryMap, String mapFilePath) {
 		try {
 			if(!new File(mapFilePath).exists()) return;
 			
 			String line = null;
 			BufferedReader reader = new BufferedReader(new FileReader(mapFilePath));
 			//skip header
 			reader.readLine();
 			while((line = reader.readLine()) != null) {
 				String[] tokens = line.split(",");
 				if(tokens != null && tokens.length > 1) {
 					String csid = tokens[0];
 					String directoryid = tokens[1].trim();
 					directoryMap.put(csid, directoryid);
 				}
 			}
 			
 			reader.close();
 		}
 		catch(Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	private static void indexCategories(Set<Category> allCategories, String indexPath) {
 		try {
 			Set<Category> cats = new HashSet<Category>();
 			Set<Category> parents = new HashSet<Category>();
 			
 			for(Category c:allCategories) {
 				cats.add(c);
 				if(c.getParent() != null) {
 					parents.add(c.getParent());
 				}
 			}
 			
 			File f = new File(indexPath);
 			String catIndexPath = f.getAbsolutePath() + "/cat_ba";
 			new File(catIndexPath).mkdirs();
 			CSListingIndexer catIndexer = CSListingIndexer.newInstance(catIndexPath);
 			
 			for(Category c:cats) {
 				catIndexer.index(CSListingDocumentFactory.createCategoryDocument(c));
 			}
 			
 			for(Category c:parents) {
 				catIndexer.index(CSListingDocumentFactory.createCategoryDocument(c));
 			}
 			
 			catIndexer.close();
 		}
 		catch(Exception ex) {
 			logger.error("Error parsing out cs enhanced listing data for categories", ex);
 			
 			throw new IllegalStateException(ex);
 		}
 	}
 	
 	private static int outerParse(String text, CSListingIndexer indexer) throws Exception {
 		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 		Document doc = db.parse(new InputSource(new StringReader(text)));
 		doc.getDocumentElement().normalize();
 		
 		return parse(doc, indexer);
 	}
 		
 	private static int parse(Document doc, CSListingIndexer indexer) {
 		NodeList list = doc.getDocumentElement().getElementsByTagName("location");
 		if(list == null || list.getLength() == 0) return 0;
 		
 		for(int i = 0, n = list.getLength(); i < n; i++) {
 			Element location = (Element)list.item(i);
 			
 			CSListing poi = populateDetail(location);
 			CSListingDocumentFactory.toLocationWords(poi, locwordWriter);			
 			if(!generateDictionary) {
 				index(poi, indexer);
 				indexCategories(location, poi);
 			}
 			else {
 				CSListingDocumentFactory.toWords(poi, wordWriter);
 			}
 		}
 		
 		return list.getLength();
 	}
 	
 	private static void index(CSListing poi, CSListingIndexer indexer) {
 		poi.setWhereId(Integer.toString(indexer.csId2whereId_.get(Integer.parseInt(poi.getListingId()))));
 		indexer.index(poi);
 	}
 	
 	public static CSListing populateDetail(Element location) {	
 		CSListing poi = new CSListing();
 		
 		populateBasic(poi, location);
 		
 		if(!generateDictionary) {
 			Element urls = ParseUtils.getChildByName(location, "urls");
 			if(urls != null) {
 				 poi.setMenuUrl(ParseUtils.getChildValueByName(urls, "menu_url"));
 				 poi.setWebUrl(ParseUtils.getChildValueByName(urls, "website_url"));
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
 			
 			SentimentAnalysis.extractTermFreqs(poi);
 		}
 		
 		return poi;
 	}
 	
 	private static void populateBasic(CSListing poi, Element location) {
 		poi.setListingId(ParseUtils.getChildValueByName(location, "id"));		
 		poi.setYpId(ypMap.get(poi.getListingId()));
 		poi.setSpId(spMap.get(poi.getListingId()));
 		
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
 	
 	private static void setWhereId(CSListing poi) {
 		if(!generateDictionary) {
 			poi.setWhereId(poi.getListingId());
 		}
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
 	
 	private static void indexCategories(Element location, CSListing poi) {
 		if(spAltCategoryIndexer == null) return;
 		
 		if(poi.categories().isEmpty()) return;
 		
 		Element content = ParseUtils.getChildByName(location, "customer_content");
 		if(content == null) return;
 		
 		String refid = ParseUtils.getChildValueByName(content, "reference_id");
 		if(!SP_REF_ID.equals(refid)) return;
 		refcount++;
 		
 		org.apache.lucene.document.Document doc = CSListingDocumentFactory.createCategoryDocument(poi);
 		if(doc == null) return;
 		
 		spAltCategoryIndexer.index(doc);
 	}
 	
 	public static void main(String[] args) throws Exception {
 		if(args.length == 4) {
 			CitySearchParser.generateDictionary = true;
 			CitySearchParser.parseListings(args[0], args[1], args[2]);
 		}
 		else if(args.length == 3) CitySearchParser.parseListings(args[0], args[1], args[2]);
 		else {throw(new Exception("need at least 3 args"));}
 	}	
 }
