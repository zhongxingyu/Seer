 package com.bookspicker.server.queries;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.bookspicker.Log4JInitServlet;
 import com.bookspicker.shared.Book;
 import com.bookspicker.shared.Bundle;
 import com.bookspicker.shared.Offer.StoreName;
 import com.bookspicker.shared.OnlineOffer;
 
 /**
  * API to query Amazon for book info.
  * TODO(jonathan) - different query for all book info, prices and for used/new/usedNew + MerchantID + operation
  *  + Id type.
  * @author Jonathan
  */
 public class AmazonQuery implements BookstoreQuery {
     private static XmlParsingTools xmlParsingTools = new XmlParsingTools();
     private static Logger logger = Log4JInitServlet.logger;
     private static final String AWS_ACCESS_KEY_ID = "AKIAJ5PGRAWWJB7DHAWQ";
     private static final String AWS_SECRET_KEY = "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";
     private static final String AWS_ASSOCIATE_TAG = "bookpicker07-20";
     private static final String ENDPOINT = "ecs.amazonaws.com";
     private static final int SHIPPING_COST = 399;
 
     private AmazonSignedRequestsHelper helper;
     private final HashMap<String, String> restRequest;
     private final HashMap<String, String> amazonNewOfferRequest = generateAmazonNewOfferRequest();
    private final HashMap<String, String> amazonMerhcnatNewOfferRequest = generateMerchantNewOfferRequest();
     private final HashMap<String, String> amazonUsedOfferRequest = generateMerchantUsedOfferRequest();
     private final HashMap<String, String> amazonOfferQuery = generateAmazonOfferQuery();
 
     public AmazonQuery() {
         logger.debug("Initilaizing Amazon Query");
         try {
             helper = AmazonSignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID,
                     AWS_SECRET_KEY);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         // General paramters they should never change.
         restRequest = new HashMap<String, String>();
         cleanRestRequest();
     }
 
     /**
      * Returns general book information based on the ISBN (image url + author +
      * title and etc). Should be used to query book info when we don't have it
      * in the cache, and if we already have the ISBN.
      */
     public List<Book> getBooksInfoByIsbn(List<String> isbnList) {
         cleanRestRequest();
         restRequest.put("Operation", "ItemLookup");
         restRequest.put("MerchantId", "Amazon");
         restRequest.put("ResponseGroup", "ItemAttributes,Images");
 
         List<Book> books = new ArrayList<Book>();
         for (String isbn : isbnList) {
             restRequest.put("IdType", isbnTypeFinder(isbn));
             restRequest.put("ItemId", isbn);
             Document document = xmlParsingTools.parseXmlToDoc(helper.sign(restRequest));
             List<Book> tempBooks = extractBooksInfo(document);
             if (tempBooks.size() > 0) {
                 books.add(tempBooks.get(0));
             } else {
                 books.add(null);
             }
 
         }
         return books;
     }
 
     /**
      * Returns 10 possible books based on the search query.
      */
     public List<Book> getBooksInfoByQuery(String query) {
         cleanRestRequest();
         restRequest.put("Operation", "ItemSearch");
         restRequest.put("MerchantId", "Amazon");
         restRequest.put("ResponseGroup", "ItemAttributes,Images");
         restRequest.put("Keywords", query);
         logger.debug("Before XML request");
         Document document = xmlParsingTools.parseXmlToDoc(helper.sign(restRequest));
         logger.debug("after XML request");
         return extractBooksInfo(document);
     }
 
     /**
      * Finds the book's ISBN based on the provided parameters.
      */
     public List<Book> getBookInfoByData(String title, String author, String publisher) {
         cleanRestRequest();
         restRequest.put("Operation", "ItemSearch");
         restRequest.put("MerchantId", "Amazon");
         restRequest.put("ResponseGroup", "ItemAttributes,Images");
         restRequest.put("Title", title);
         restRequest.put("Author", author);
         restRequest.put("Publisher", publisher);
 
         Document document = xmlParsingTools.parseXmlToDoc(helper.sign(restRequest));
         return extractBooksInfo(document);
     }
 
     /**
      * Returns a list of the available offers for all the books.
      */
 
     // TODO(Jonathan) - Get offers for mutliple ISBN at once.
     @Override
     public void getBooksOffers(Bundle bundle) {
         List<HashMap<String, String>> restCalls = new ArrayList<HashMap<String, String>>();
         switch (bundle.getCondition()) {
         case ALL:
            restCalls.add(amazonMerhcnatNewOfferRequest);
             restCalls.add(amazonNewOfferRequest);
             restCalls.add(amazonUsedOfferRequest);
             break;
         case USED:
             restCalls.add(amazonUsedOfferRequest);
             break;
         case NEW:
            restCalls.add(amazonMerhcnatNewOfferRequest);
             restCalls.add(amazonNewOfferRequest);
             break;
         }
 
         for (Book book : bundle.getBooksThatNeedUpdates()) {
             for (HashMap<String, String> request : restCalls) {
                 request.put("ItemId", book.getEan() != null ? book.getEan() : book.getIsbn());
                 extractBookOffers(bundle, book, helper.sign(request));
             }
         }
     }
 
     private void extractBookOffers(Bundle bundle, Book book, String request) {
         Document mainDocument = xmlParsingTools.parseXmlToDoc(request);
         if (mainDocument == null) {
             return;
         } else {
             NodeList offerNodes = mainDocument.getElementsByTagName("Offer");
             Element offer = (Element) offerNodes.item(0);
 
             String listingId = xmlParsingTools.getTextValue(offer, "OfferListingId");
             String condition = xmlParsingTools.getTextValue(offer, "Condition");
             if (listingId == null) {
                 // No offers!
                 return;
             }
             // TODO: Why are we only searching for 1 offer???
             amazonOfferQuery.put("Item.1.OfferListingId", listingId);
             Document offerDocument = xmlParsingTools.parseXmlToDoc(helper.sign(amazonOfferQuery));
             if (offerDocument == null) {
                 return;
             } else {
                 NodeList cartNodes = offerDocument.getElementsByTagName("Cart");
                 Element cart = (Element) cartNodes.item(0);
                 String url = xmlParsingTools.getTextValue(cart, "PurchaseURL");
                 double priceDbl = xmlParsingTools.getDoubleValue(cart, "Amount") / 100;
                 int price = (int) Math.round(priceDbl * 100);
                 String sellerName = xmlParsingTools.getTextValue(cart, "SellerNickname");
                 StoreName shopName = sellerName.equals("Amazon.com") ? StoreName.AMAZON
                         : StoreName.AMAZON_MARKETPLACE;
                 bundle.addOffer(book, new OnlineOffer(price, SHIPPING_COST, shopName, sellerName,
                         condition, url));
             }
         }
     }
 
     private HashMap<String, String> generateAmazonNewOfferRequest() {
         HashMap<String, String> request = new HashMap<String, String>();
         request.put("AssociateTag", AWS_ASSOCIATE_TAG);
         request.put("Version", "2009-03-31");
         request.put("SearchIndex", "Books");
         request.put("Operation", "ItemLookup");
         request.put("ResponseGroup", "Offers");
         request.put("IdType", "ISBN");
         request.put("MerchantId", "Amazon");
         return request;
     }
 
     private HashMap<String, String> generateMerchantUsedOfferRequest() {
         HashMap<String, String> request = generateMerchantNewOfferRequest();
         request.put("Condition", "Used");
         return request;
     }
 
 
     private HashMap<String, String> generateMerchantNewOfferRequest() {
         HashMap<String, String> request = generateAmazonNewOfferRequest();
        request.put("MerchantId", "ALL");
         request.put("Condition", "New");
         return request;
     }
 
     private HashMap<String, String> generateAmazonOfferQuery() {
         HashMap<String, String> request = new HashMap<String, String>();
         request.put("AssociateTag", AWS_ASSOCIATE_TAG);
         request.put("Version", "2009-03-31");
         request.put("Operation", "CartCreate");
         request.put("Item.1.Quantity", "1");
         request.put("MergeCart", "True");
         request.put("Service", "AWSECommerceService");
         return request;
     }
 
     /**
      * Determines whether the query should be for 10/13 ISBN
      * 
      * @param isbn
      */
     private String isbnTypeFinder(String isbn) {
         if (isbn.length() == 10) {
             return "ISBN";
         } else if (isbn.length() == 13) {
             return "EAN";
         }
         return "";
     }
 
     /**
      * Returns specific book info for ISBN queries.
      */
     private List<Book> extractBooksInfo(Document document) {
         List<Book> bookList = new ArrayList<Book>();
         if (document == null) {
             bookList.add(null);
             logger.debug("No books document is null");
             return bookList;
         }
         NodeList bookNodes;
         bookNodes = document.getElementsByTagName("Item");
         for (int i = 0; i < bookNodes.getLength(); i++) {
             // logger.debug("add Book: " + i);
             bookList.add(extractBookInfo((Element) bookNodes.item(i)));
         }
         // logger.debug("done adding books");
         return bookList;
     }
 
     private Book extractBookInfo(Element book) {
         // Element bookAttributes = (Element)
         // book.getElementsByTagName("ItemAttributes");
         String title = xmlParsingTools.getTextValue(book, "Title");
         String[] authorList = xmlParsingTools.getAllTextValue(book, "Author");
         String isbn10 = xmlParsingTools.getTextValue(book, "ISBN");
         String ean = xmlParsingTools.getTextValue(book, "EAN");
         double listPriceDbl = xmlParsingTools.getDoubleValue(book, "Amount") / 100;
         int listPrice = (int) Math.round(listPriceDbl * 100);
         int edition = xmlParsingTools.getIntValue(book, "Edition");
         String publisher = xmlParsingTools.getTextValue(book, "Publisher");
         NodeList mediumImageNode = book.getElementsByTagName("MediumImage");
         String imageUrl = null;
         if (mediumImageNode != null) {
             imageUrl = xmlParsingTools.getTextValue((Element) mediumImageNode.item(0), "URL");
         }
         return new Book(title, authorList, isbn10, ean, listPrice, imageUrl, edition, publisher);
     }
 
     private void cleanRestRequest() {
         this.restRequest.clear();
         restRequest.put("AssociateTag", AWS_ASSOCIATE_TAG);
         restRequest.put("Version", "2009-03-31");
         restRequest.put("SearchIndex", "Books");
         restRequest.put("Service", "AWSECommerceService");
     }
 
     @Override
     public String toString() {
         return "AmazonQuery";
     }
 }
