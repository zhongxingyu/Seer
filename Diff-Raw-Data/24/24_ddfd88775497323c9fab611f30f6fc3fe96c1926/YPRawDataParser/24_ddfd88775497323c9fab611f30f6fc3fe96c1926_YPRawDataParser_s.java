 package com.where.atlas.feed.yellowpages;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.StringTokenizer;
 
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import com.where.atlas.feed.FeedParser;
 import com.where.atlas.feed.PlaceCollector;
 import com.where.place.Address;
 import com.where.place.YPPlace;
 
 
 public class YPRawDataParser implements FeedParser { 
     
     
     protected YPParserUtils parser;
     private static BufferedWriter bufferedWriter;
     int parseingType;
 
     
     public YPRawDataParser(YPParserUtils Yparser)
     {
         parser=Yparser;
         try{
         	
         	File file = new File(parser.getTargetPath());
         	file.createNewFile();
             bufferedWriter = new BufferedWriter(new FileWriter(parser.getTargetPath()));
             
            //Start the JSON Array
            bufferedWriter.write("[");
         }
         catch(Exception e){
             System.err.print("Error Loading!"+e.getMessage());
         }
     }
     
     protected static BufferedWriter bufferedWriter()
     {
         return bufferedWriter;
     }
     
     public static String cleanReview(String review)
     {
         review.replace("\\n","");
         review.replace("\\t", "");
         return review;
     }
     
     public void closeWriter() throws IOException
     {
    	//close the array and close the writer
    	bufferedWriter.write("]");
         bufferedWriter.close();
     }
     
     public static String stripPhone(String phone) {
         if(phone == null || phone.trim().length() == 0){return phone;}
         StringBuffer buffer = new StringBuffer();
         char[] chrs = phone.toCharArray();
         for(int i = 0, n = chrs.length; i < n; i++) {
             if(Character.isDigit(chrs[i])) {
                 buffer.append(chrs[i]);
             }
         }
         String tel = buffer.toString();
         if(tel.length() > 10) tel = tel.substring(0, 9);
         
         return tel;
     }
     
     private void parseListings(PlaceCollector collector,NodeList listings)
     {
     	Node listingNode = null;
         YPPlace poi = null;
         Address location = null;
         //// <LISTING>
         for(int i = 0; i < listings.getLength();i++)
         {   
         	
             listingNode = listings.item(i);
             if(listingNode.getNodeType() == Node.ELEMENT_NODE){
                 Element listingElement = (Element)listingNode;
                 
                 
                 poi = new YPPlace();
                 location = new Address();
                 
                 poi.setName(listingElement.getAttribute("name"));//NAME
                 location.setZip(listingElement.getAttribute("postal_code"));//addy.ZIP
                 poi.setPhone(stripPhone(listingElement.getAttribute("phone")));//PHONE
                 poi.setHours(listingElement.getAttribute("hours"));
                 poi.setBizURL(listingElement.getAttribute("biz_url"));
                 location.setCity(listingElement.getAttribute("locality"));//addy.CITY
                 location.setState(listingElement.getAttribute("region"));
                 location.setAddress1(listingElement.getAttribute("address"));
                 
                 
                 if(listingElement.getAttribute("lat").length() > 1 && listingElement.getAttribute("lon").length() > 1){
                     location.setLat(Double.parseDouble(listingElement.getAttribute("lat")));	//LATITUDE
                     location.setLng(Double.parseDouble(listingElement.getAttribute("lon")));	//LONGITUDE
                 }
                 
                 poi.setAddress(location);
                 
                 //sub-nodes
                 NodeList categories = listingElement.getElementsByTagName("category");
                 fillCategories(categories,poi);
                 NodeList reviews = listingElement.getElementsByTagName("review");
                 fillReviews(reviews,poi);
                 
                 NodeList URLlist = listingElement.getElementsByTagName("URL");
                 Element URL = (Element) URLlist.item(0);
                 
                 try{
                 	poi.setYPurl(URL.getTextContent());
                 }
                 catch(NullPointerException np)
                 {
                 	
                 }
                 	
                 if(poi.getName().length() <= 0)
             		collector.collectBadInput(poi, new Exception("Nullname"));
                 else
                 	collector.collect(poi);
             }
         }
     	
     }
     
     public String readNode(Element element,String nodename)
     {
     	return element.getElementsByTagName(nodename).item(0) != null ? 
 				element.getElementsByTagName(nodename).item(0).getTextContent().trim() : null;
     }
     
     private void parseDetails(PlaceCollector collector,NodeList details)
     {
     	Node listingNode = null;
         YPPlace poi = null;
         
         //// <BUSINESS_DETAILS>
         for(int i = 0; i < details.getLength();i++)
         {	
         	listingNode = details.item(i);
         	
             if(listingNode.getNodeType() == Node.ELEMENT_NODE){
             	
             	Element element = ((Element)listingNode);
             	poi = new YPPlace();
             	
             	//<URL>
 	            String url = element.getElementsByTagName("URL").item(0).getTextContent();
 	            poi.setYPurl(url);
 	            
 	            String name = readNode(element,"name");
 	            if(name != null && name.length() > 0)
 	            	poi.setName(name);
 	            
 	            //<address>
                 String address = readNode(element, "address");
                 if(address != null && address.length() > 0)
                 	poi.setAddress(parseAddress(address));
 	            
 	            //<accreditations>
                 String accreditations = readNode(element, "accreditations");
                 if(accreditations != null && accreditations.length() > 0)
                 	poi.setAccreditations(accreditations);
             	//<brands>
                 String brands = readNode(element, "brands");
                 if(brands != null && brands.length() > 0)
                 	poi.setBrands(brands);
 	            //<payment_types_accepted>
                 String payment_types_accepted = readNode(element, "payment_types_accepted");
                 if(payment_types_accepted != null && payment_types_accepted.length() > 0)
                 	poi.setPayment_types_accepted(payment_types_accepted);
                 //<in_business_since>
                 String in_business_since = readNode(element, "in_business_since");
                 if(in_business_since != null && in_business_since.length() > 0)
                 	poi.setBiz_since(in_business_since);
                 
                 //<aka>
                 String aka = readNode(element, "aka");
                 if(aka != null && aka.length() > 0)
                 	poi.setAka(aka);
                 //<average_rating>
                 String average_rating = readNode(element, "average_rating");
                 if(average_rating != null && average_rating.length() > 0)
                 	poi.setAvg_rating(average_rating);
                 //<languages_spoken>
                 String languages_spoken = readNode(element, "languages_spoken");
                 if(languages_spoken != null && languages_spoken.length() > 0)
                 	poi.setLanguages(languages_spoken);
                 
                 //<categories>
                 String categories = readNode(element,"categories");
                 if(categories != null && categories.length() > 0)
                 	parseDetailCategories(categories, poi);
                 
                 //aaannnddddd   collect.
                 if(poi.getName() != null && poi.getName().length() <= 0)
             		collector.collectBadInput(poi, new Exception("Nullname"));
                 else
                 	collector.collect(poi);
             }
         }
     }
     
     private void parseDetailCategories(String categories, YPPlace poi)
     {
     	
     	if(categories.indexOf(",") > -1){
 	    	StringTokenizer st = new StringTokenizer(categories,",");
 	    	while(st.hasMoreTokens())
 	    		poi.addCategory(st.nextToken().trim());
     	}
     	else{
     		StringTokenizer st = new StringTokenizer(categories);
     		
     		if(st.countTokens() == 2){
     			if(categories.charAt(categories.indexOf(" ")+1) == ' ')
     				while(st.hasMoreTokens())
     		    		poi.addCategory(st.nextToken());
     			else
     				poi.addCategory(categories);
     		}
     		else{
 		    	while(st.hasMoreTokens())
 		    		poi.addCategory(st.nextToken());
     		}
     	}
     		
     }
     
     
     private void parseReviews(PlaceCollector collector,NodeList reviews)
     {
     	Node listingNode = null;
         YPPlace poi = null;
         
         //// <BUSINESS_REVIEWS>
         for(int i = 0; i < reviews.getLength();i++)
         {	
         	listingNode = reviews.item(i);
         	
             if(listingNode.getNodeType() == Node.ELEMENT_NODE){
             	
             	Element element = ((Element)listingNode);
             	
                 poi = new YPPlace();
                 
                 //<name>
                 String name = element.getElementsByTagName("name").item(0).getTextContent();
 	            poi.setName(name);
 	            
                 //<address>
                 String address = element.getElementsByTagName("address").item(0).getTextContent().trim();
                 if(address != null && address.length() > 0)
                 	poi.setAddress(parseAddress(address));
                 
                 //<URL>
 	            String url = element.getElementsByTagName("URL").item(0).getTextContent();
 	            poi.setYPurl(url);
                 
                 //<reviews>
 	            NodeList reviewnodes = element.getElementsByTagName("review");
 	            if(reviewnodes.getLength() > 0)
 	            	fillDetailReviews(reviewnodes, poi);
 	            
 	            if(poi.getName().length() <= 0)
             		collector.collectBadInput(poi, new Exception("Nullname"));
                 else
                 	collector.collect(poi);
             }
         }
     }
     
     private Address parseAddress(String addy)
     {
     	Address address = new Address();
     	String state = null , city = null;
     	
     	String zip = addy.substring(addy.lastIndexOf(" ")+1);
     	try{
 	    	addy = addy.substring(0,addy.lastIndexOf(" "));
 	    	
 	    	
 	    	state = addy.substring(addy.lastIndexOf(" ")+1);
 	    	
 	    	addy = addy.substring(0,addy.lastIndexOf(" "));
 	    	
     	
 	    	city = addy.substring(addy.lastIndexOf(" ")+1);
 	    	
 	    	addy = addy.substring(0,addy.lastIndexOf(" "));
 	    	address.setAddress1(addy.trim());
 	    	address.setState(state);
 	    	address.setCity(city);
 	    	address.setZip(zip);
     	}
     	catch(Exception e)
     	{
         	address.setState(state);
         	address.setCity(addy);
         	address.setZip(zip);
     	}
     	
     	return address;
     }
     
     private void fillDetailReviews(NodeList reviews, YPPlace poi)
     {
     	Node listingNode = null;
     	JSONObject json;
     	
     	for(int i = 0; i < reviews.getLength();i++)
         {	
     		json = new JSONObject();
         	listingNode = reviews.item(i);
         	
             if(listingNode.getNodeType() == Node.ELEMENT_NODE){
             	
             	Element element = ((Element)listingNode);
             	
             	try {
 			    	//<content>
 	            	String content = element.getElementsByTagName("content").item(0).getTextContent();
 					json.put("text", content);
 			    	//<author>
 	                String author = element.getElementsByTagName("author").item(0).getTextContent();
 		            json.put("user", author);
 			    	//<subject>
 		            String subject = element.getElementsByTagName("subject").item(0).getTextContent();
 		            json.put("subject", subject);
 			    	//<rating>
 		            String rating = element.getElementsByTagName("rating").item(0).getTextContent();
 		            json.put("rating", rating);
 			    	//<date>
 		            String date = element.getElementsByTagName("date").item(0).getTextContent();
 		            json.put("date", date);
 		            
 		            poi.addReview(json);
             	}
             	catch(Exception e)
             	{
             		System.err.println("error putting together review json "+e.getMessage());
             	}
             }
         }
     }
     
     public void parse(PlaceCollector collector, InputStream ins) throws IOException {
         try{
                 DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                 DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                 Document doc = docBuilder.parse(ins);
                 
                 doc.getDocumentElement().normalize();
                 
                 
                 if(parser.getParserType() == 1){
                 	NodeList listOfListings = doc.getElementsByTagName("listing");
                 	parseListings(collector, listOfListings);
                 }
                 else if(parser.getParserType() == 2)
                 {
 					String nodename = doc.getChildNodes().item(0).getChildNodes().item(0).getNodeName();
 					Element reviews = (Element)doc.getChildNodes().item(0);
 					NodeList reviewLst = reviews.getElementsByTagName("business_reviews");
 					
                     if(!nodename.equals("listing"))
                     	parseReviews(collector, reviewLst);
                 }
                 else if(parser.getParserType() == 3)
                 {
                 	String nodename = doc.getChildNodes().item(0).getChildNodes().item(0).getNodeName();
 					Element details = (Element)doc.getChildNodes().item(0);
 					NodeList detailLst = details.getElementsByTagName("business_details");
 					
                     if(!nodename.equals("listing"))
                     	parseDetails(collector, detailLst);
                 }
                 else
                 {
                 	System.err.println("INVALID PARSEING TYPE\n restart and choose: 1) <listing>   2) <reviews>");
                 	return;
                 }
                 
     	}
         catch(Exception e){
             System.err.println(e.getMessage());
             }
     }
     
     private void fillReviews(NodeList reviewList,YPPlace poi)
     {
     	for(int i = 0;i < reviewList.getLength();i++)
         {
     		Node reviewNode = reviewList.item(i);
         	if(reviewNode.getNodeType() == Node.ELEMENT_NODE){
                 Element review = (Element)reviewNode;
                 JSONObject json = new JSONObject();
                 
                 try{
 	                json.put("user", review.getAttribute("user_name"));
 	                json.put("rating", review.getAttribute("rating"));
 	                json.put("date", review.getAttribute("date"));
 	                json.put("text", review.getAttribute("text"));
 	                
 	                poi.addReview(json);
                 }
                 catch(Exception e)
                 {
                 	System.out.println("Error constructing review json: "+e.getMessage());
                 }
                 
                 
                 
         	}
         }
     }
     
     private void fillCategories(NodeList categoryList,YPPlace poi)
     {
     	for(int i = 0;i < categoryList.getLength();i++)
         {
     		Node categoryNode = categoryList.item(i);
         	if(categoryNode.getNodeType() == Node.ELEMENT_NODE){
                 Element category = (Element)categoryNode;
                 
                 if(category.getAttribute("name") != null)
                 	poi.addCategory(category.getAttribute("name"));
         	}
         }
     }
     
     
     
     
 }
