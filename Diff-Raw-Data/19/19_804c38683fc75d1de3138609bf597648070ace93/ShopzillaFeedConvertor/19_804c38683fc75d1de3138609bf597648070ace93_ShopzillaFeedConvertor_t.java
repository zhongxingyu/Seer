 package com.shopzilla.feeds;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import java.io.*;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.logging.Logger;
 
 /**
  * Converts Shopzilla XML feed to CSV format
  */
 public class ShopzillaFeedConvertor extends DefaultHandler {
     private static final Logger log = Logger.getLogger(ShopzillaFeedConvertor.class.getName());
 
     // getting SAXParserFactory instance
     private static SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
     private HashMap<String, String> offer = null;
     private StringBuffer curValue = null;
     private String xsize = null, ysize = null;
     private PrintWriter writer = null;
     private int index = 0,
             curFetchCount = 0, maxFetchCount = 100000,
             includedResultsIteration = 0,
             fetchCountIteration = 0, totalResultsIteration = 1;
 
     enum OfferType {PRODUCT, OFFER}
 
     ;
     private OfferType offerType = OfferType.OFFER;
 
 
     public void convert(String urlString, OutputStream out) throws Exception {
         URL url = null;
         InputStream in = null;
 
         try {
             this.writer = new PrintWriter(new BufferedOutputStream(out));
 
             emitHeader();
 
             if (urlString.startsWith("file://")) {
                 in = Utils.getInputStream(urlString, true);
             } else {
                 convertURL(urlString, out);
                 return;
             }
             convertXMLToCSV(in, out);
 
         } catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             // TODO some error needs to go back to client
             throw e;
         } finally {
             try {
                 if (in != null) in.close();
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
     }
 
     public void convertURL(String urlString, OutputStream out) throws Exception {
         HTTPParams params = new HTTPParams(urlString);
 
         int requestedMaxFetchCount = params.getAttributeValueAsInt("results");
         if (requestedMaxFetchCount != 0) {
             maxFetchCount = requestedMaxFetchCount;
         }
         System.err.println("Will download " + maxFetchCount + " offers");
 
         String categoryId = params.getAttributeValue("categoryId");
         if (categoryId == null || categoryId == "") {
             iterate(params, out);
             return;
         }
         log.info("Need to fetch child categories");
         //List<Category> list = new Taxonomy().getStaticChildCategories(categoryId);
         List<Category> list = new Taxonomy().getChildCategories(categoryId);
 
         if (list.size() == 0) {
             log.info(categoryId + " does not have child categories");
             list.add(new Category(categoryId));
         }
         log.info("Will fetch offers for " + list.size() + " categories");
         for (Category category : list) {
             if (curFetchCount >= maxFetchCount) break;
             params.setAttributeValue("categoryId", category.id);
             //log.info(params.getURL());
             iterate(params, out);
         }
 
     }
 
     private void iterate(HTTPParams params, OutputStream out) throws Exception {
         //log.info("Query args:\n" + Utils.printQueryMap(queryMap));
         int iter = 0;
         fetchCountIteration = 0;
         totalResultsIteration = 1;
         int retryCount = 0;
         while (fetchCountIteration < totalResultsIteration && curFetchCount < maxFetchCount && retryCount < 100) {
             try {
                 index = fetchCountIteration;
                 params.setAttributeValue("start", index + "");
                 String newURLString = params.getURL();
                 log.info("new URL string=\n" + newURLString);
                 InputStream in = Utils.getInputStream(newURLString, true);
 
                 convertXMLToCSV(in, out);
                 if (in != null) in.close();
 
                 fetchCountIteration += includedResultsIteration;
                 curFetchCount += includedResultsIteration;
                 log.info("Iter = " + ++iter + " includedResultsIteration=" + includedResultsIteration
                         + " iterarion fetch count=" + fetchCountIteration + " current fetch count = " + curFetchCount);
 
                 if (includedResultsIteration == 0) {
                     // there are no more records to fetch
                     log.info("No more items to fetch");
                     break;
                 }
                 includedResultsIteration = 0; // reset for the next iteration
             } catch (Exception ex) {
                 log.warning(ex.getMessage() + "\n" + ex.getStackTrace());
                 retryCount++;
             }
 
 
         }
 
     }
 
     public void convertXMLToCSVMock(InputStream in, OutputStream out) {
         includedResultsIteration = 250;
     }
 
     public void convertXMLToCSV(InputStream in, OutputStream out) throws SAXException, ParserConfigurationException, IOException {
 
 
         // Getting SAXParser object from AXParserFactory instance
         SAXParser saxParser = saxParserFactory.newSAXParser();
 
         // Parsing XML Document by calling parse method of SAXParser class
         saxParser.parse(new InputSource(in), this);
         writer.flush();
     }
 
 
     @Override
     public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         //log.info("Start element =" + qName);
         if (qName.equalsIgnoreCase("Offer") || qName.equalsIgnoreCase("product")) {
             offer = new HashMap<String, String>();
         }
 
         if (qName.equalsIgnoreCase("Offer")) {
             //System.out.println("Parsing Offer element");
             this.offerType = ShopzillaFeedConvertor.OfferType.OFFER;
             for (int i = 0; i < attributes.getLength(); i++) {
                 offer.put(attributes.getQName(i).toLowerCase(), attributes.getValue(i));
             }
         } else if (qName.equalsIgnoreCase("product")) {
             //System.out.println("Parsing product element");
             this.offerType = OfferType.PRODUCT;
         } else if (qName.equalsIgnoreCase("merchantProduct")) {
             //System.out.println("Parsing merchantProduct element");
             for (int i = 0; i < attributes.getLength(); i++) {
 //                System.out.println("Putting merchantProduct attribute: " +
 //                        attributes.getQName(i) + "=" + attributes.getValue(i) + " in offer");
                 offer.put(attributes.getQName(i).toLowerCase(), attributes.getValue(i));
             }
         } else if (qName.equalsIgnoreCase("Image")) {
             xsize = attributes.getValue("xsize");
             ysize = attributes.getValue("ysize");
         } else if (qName.equalsIgnoreCase("Offers")) {
             totalResultsIteration = Utils.parseInt(attributes.getValue("totalResults"));
             includedResultsIteration = Utils.parseInt(attributes.getValue("includedResults"));
             log.info("totalResultsIteration=" + totalResultsIteration + " includedResultsIteration=" + includedResultsIteration);
         }
 
         curValue = new StringBuffer(300);
     }
 
 
     @Override
     public void endElement(String uri, String localName, String qName) throws SAXException {
         //log.info("End element = "+ qName);
         if (offer == null) return;  // Unless we are inside offer element we are not interested
         if (qName.equalsIgnoreCase("Images")) return;   // we don't have to process outer Images container
 
         // override landing page url as this is to be constructed using an offline contract
         if (qName.equalsIgnoreCase("Offer") || qName.equalsIgnoreCase("product")) {
             String url = "http://www.bizrate.com/oid" +
                     offer.get(OfferField.ProductId.getFieldName(offerType).toLowerCase()) +"/search/retarget/";
             offer.put(OfferField.URL.getFieldName(offerType).toLowerCase(), url);
         }
 
         if (qName.equalsIgnoreCase("Offer")) {
             //System.out.println("End element = "+ qName);
             emitOffer(offer);
             offer = null;
             curValue = null;
             return;
         }
         if (qName.equalsIgnoreCase("product")) {
             //System.out.println("End element = "+ qName);
             emitOffer(offer);
             offer = null;
             curValue = null;
             return;
         }
 
         if (curValue == null) return;
 
         if (qName.equalsIgnoreCase("Image")) {
             // We need to check whether sizes are one of the 60x60 or 100x100 or 160x160 or 400x400
             if (!xsize.equals(ysize)) return;
             // check whether its a supported size
             if (!(xsize.equals("60") || xsize.equals("100") || xsize.equals("160") || xsize.equals("400")))
                 return;
             String elemName = "image" + xsize + "x" + ysize;
             //log.info("Putting " + elemName + "=" + curValue + " in offer");
 
             offer.put(elemName, curValue.toString());
             xsize = null;
             ysize = null;
             return;
         }
 
         // we have a new element (other than image) value for offer
         if (OfferField.isMandatory(offerType, qName)) {
             log.info("Putting " + qName.toLowerCase() + "=" + curValue + " in offer. offertype = " + offerType);
             offer.put(qName.toLowerCase(), curValue.toString());
         }
 
     }
 
     private void emitValue(String key, boolean emitSeparator) {
         String value = offer.get(key.toLowerCase());
         // remove all new lines as they screw up csv format
         if (value != null) {
             value = value.replaceAll("\n||\r", "");
             if (value.contains("\"")) {
                 value = value.replaceAll("\"", "\"\"");
                 value = "\"" + value + "\"";
             } else if (value.contains(",")) {
                 value = "\"" + value + "\"";
             }
             value = value.trim();   // remove leading and trailing spaces as well
         }
         value = (value == null ? "" : value);
         //System.out.println("Emitting value=" + value);
         writer.write(value);
         if (emitSeparator) writer.write(",");
     }
 
     private void emitValue(String key) {
         emitValue(key, true);
     }
 
     /**
      * Order of the attributes below are deeply tied with order of headers in emitHeader()
      * Any change below needs to be co-ordinated with that method.
      */
 
     private void emitOffer(HashMap<String, String> offer) {
        int fieldCount = OfferField.getMandatoryFields().size();
        int index = 1;
         for (OfferField field : OfferField.getMandatoryFields()) {
             //System.out.println("Emitting value for " + field.getFieldName(offerType));
            // don't emit separator for last column
            emitValue(field.getFieldName(offerType), (index++ != fieldCount));
         }
 
         writer.println();
 
     }
 
     /**
      * Order of the attributes below are deeply tied with order of emitting in emitOffer()
      * Any change below needs to be co-ordinated with that method.
      */
 
     private void emitHeader() {
         for (OfferField field : OfferField.getMandatoryFields()) {
             //System.out.println("Emitting header: " + field.getFieldName(offerType));
             writer.print(field.getFieldName(offerType));
             writer.print(",");
         }
         /*
         for(OfferField field : otherFields) {
             writer.print(field.getFieldName());
         }
         */
 
         writer.println();
 
     }
 
     @Override
     public void characters(char[] chars, int start, int length) throws SAXException {
         if (offer == null) return;  // Unless we are inside offer element we are not interested
         curValue.append(chars, start, length);
         //log.info("Start=" + start + " length = "+ length + " value=" + curValue);
     }
 
     public static void main(String[] args) throws Exception {
         //FileReader reader = new FileReader("/Users/shitalm/Documents/work/test/feeds/test/shopzilla.xml");
         ShopzillaFeedConvertor convertor = new ShopzillaFeedConvertor();
         //convertor.convert("file://Users/shitalm/Documents/work/test/feeds/resources/shopzilla.xml", log.info);
         //String url = "http://catalog.bizrate.com/services/catalog/v1/us/product?apiKey=bfc9253adedf4ad6880d24ee17eb59d6&publisherId=6866&placementId=1&categoryId=&keyword=acer+aspire+laptops&productId=&productIdType=&offersOnly=true&merchantId=&brandId=&biddedOnly=true&minPrice=&maxPrice=&minMarkdown=&zipCode=&freeShipping=&start=0&results=3&backfillResults=0&startOffers=0&resultsOffers=0&sort=relevancy_desc&attFilter=&attWeights=&attributeId=&resultsAttribute=1&resultsAttributeValues=1&showAttributes=&showProductAttributes=&minRelevancyScore=100&maxAge=&showRawUrl=&imageOnly=true&format=xml&callback=callback";
         //String url = "http://catalog.bizrate.com/services/catalog/v1/us/product?apiKey=bfc9253adedf4ad6880d24ee17eb59d6&publisherId=6866&&categoryId=&keyword=laptop&productId=&productIdType=&offersOnly=true&biddedOnly=true&start=0&results=300&sort=relevancy_desc&minRelevancyScore=100&imageOnly=true&format=xml";
         convertor.convert("file:///Users/shitalm/Documents/work/test/shopzilla/test/publisher_feed_100001846.xml", System.out);
 
         //convertor.convert(url, System.out);
 
     }
 
 }
