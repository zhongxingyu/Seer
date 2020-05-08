 package com.abudko.reseller.huuto.query.service.list.html.huuto;
 
 import java.util.Collection;
 import java.util.LinkedHashSet;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Attributes;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.springframework.stereotype.Component;
 
 import com.abudko.reseller.huuto.query.enumeration.Brand;
 import com.abudko.reseller.huuto.query.html.HtmlParserConstants;
 import com.abudko.reseller.huuto.query.service.list.ListResponse;
 import com.abudko.reseller.huuto.query.service.list.SizeParser;
 import com.abudko.reseller.huuto.query.service.list.html.HtmlListParser;
 
 @Component
 public class HuutoHtmlListParser implements HtmlListParser {
 
     private static final String IMG_SUFFIX = "-s.jpg";
 
     private static final String HTML_RESULT_LIST_CLASS = "row-fluid item ";
     private static final String HTML_ELEMENT_PRICE_WRAPPER = "pull-right";
     private static final String HTML_ELEMENT_PRICE = "row-fluid";
     private static final String HTML_ELEMENT_DESCRIPTION = "spandesc";
     private static final String HTML_ELEMENT_IMAGE = "spanimage";
     private static final String HTML_ELEMENT_CLOSING_TIME = "closingtime";
 
     @Override
     public Collection<ListResponse> parse(String htmlResponse) {
         String html = truncateUnused(htmlResponse);
 
         Collection<ListResponse> responses = new LinkedHashSet<ListResponse>();
         Document document = Jsoup.parse(html);
 
         Elements elements = document.getElementsByAttributeValueContaining("class", HTML_RESULT_LIST_CLASS);
         for (Element element : elements) {
             ListResponse queryResponse = new ListResponse();
 
             String description = parseDescription(element);
             queryResponse.setDescription(description);
 
             String currentPrice = parseCurrentPrice(element);
             queryResponse.setCurrentPrice(currentPrice);
 
             String fullPrice = parseFullPrice(element);
             queryResponse.setFullPrice(fullPrice);
 
             String itemUrl = parseItemUrl(element);
             queryResponse.setItemUrl(itemUrl);
 
             String imgBaseSrc = parseImgSrc(element);
             queryResponse.setImgBaseSrc(imgBaseSrc);
 
             String bids = parseBids(element);
             queryResponse.setBids(bids);
 
             String last = parseLast(element);
             queryResponse.setLast(last);
 
             String size = parseSize(description);
             queryResponse.setSize(size);
 
             String brand = parseBrand(description);
             queryResponse.setBrand(brand);
 
             responses.add(queryResponse);
         }
 
         return responses;
     }
 
     private String truncateUnused(String htmlResponse) {
         String str = String.format("<div class=\"%s\"", HTML_RESULT_LIST_CLASS);
         int indexOfResultList = htmlResponse.indexOf(str);
         if (indexOfResultList < 0) {
             return htmlResponse;
         }
         String truncatedHtml = htmlResponse.substring(indexOfResultList);
         return truncatedHtml;
     }
 
     private String parseCurrentPrice(Element element) {
         Element priceWrapper = element.getElementsByClass(HTML_ELEMENT_PRICE_WRAPPER).get(0);
         Element pricesElement = priceWrapper.getElementsByClass(HTML_ELEMENT_PRICE).get(0);
         Element currentPriceElement = pricesElement.child(0).child(1);
         if (currentPriceElement.children().isEmpty() == false) {
             return formatPrice(currentPriceElement.child(0).ownText());
         } else {
             return formatPrice(currentPriceElement.ownText());
         }
     }
 
     private String parseFullPrice(Element element) {
         Element priceWrapper = element.getElementsByClass(HTML_ELEMENT_PRICE_WRAPPER).get(0);
         Element pricesElement = null;
         if (priceWrapper.getElementsByClass(HTML_ELEMENT_PRICE).size() > 1) {
             pricesElement = priceWrapper.getElementsByClass(HTML_ELEMENT_PRICE).get(1);
         } else {
             pricesElement = priceWrapper.getElementsByClass(HTML_ELEMENT_PRICE).get(0);
         }
         Element currentPriceElement = pricesElement.child(0).child(1);
         if (currentPriceElement.children().isEmpty() == false) {
             return formatPrice(currentPriceElement.child(0).ownText());
         } else {
             return formatPrice(currentPriceElement.ownText());
         }
     }
 
     private String formatPrice(String price) {
         String euro = String.format(" %s", HtmlParserConstants.EURO_CHAR);
         String priceDouble = price.substring(0, price.indexOf(euro));
         return priceDouble.replace(",", ".");
     }
 
     private String parseDescription(Element element) {
         Element descriptionElement = element.getElementsByClass(HTML_ELEMENT_DESCRIPTION).get(0);
         Element child = descriptionElement.child(0);
         String description = child.ownText();
         return description;
     }
 
     private String parseItemUrl(Element element) {
         Element descriptionElement = element.getElementsByClass(HTML_ELEMENT_DESCRIPTION).get(0);
         Attributes attributes = descriptionElement.child(0).attributes();
         String itemUrl = attributes.get("href");
         return formatItemUrl(itemUrl);
     }
 
     private String formatItemUrl(String itemUrl) {
         String formattedItemUrl = itemUrl.replace(HtmlParserConstants.ITEM_URL_CONTEXT, "");
         return formattedItemUrl;
     }
 
     private String parseBids(Element element) {
         Element priceWrapper = element.getElementsByClass(HTML_ELEMENT_PRICE_WRAPPER).get(0);
         Element pricesElement = priceWrapper.getElementsByClass(HTML_ELEMENT_PRICE).get(0);
         Element currentPriceElement = pricesElement.child(0).child(0);
         return currentPriceElement.ownText();
     }
 
     private String parseLast(Element element) {
         Element lastElement = element.getElementsByClass(HTML_ELEMENT_CLOSING_TIME).get(0);
         String last = lastElement.child(0).ownText();
         return last;
     }
 
     private String parseImgSrc(Element element) {
         Element descriptionElement = element.getElementsByClass(HTML_ELEMENT_IMAGE).get(0);
         Element child = descriptionElement.child(0);
         String imgSrc = child.child(0).attr("src");
         return formatImgSrc(imgSrc);
     }
 
     private String formatImgSrc(String imgSrc) {
         String formattedImgSrc = imgSrc.replace(IMG_SUFFIX, "");
         return formattedImgSrc;
     }
 
     private String parseSize(String description) {
         return SizeParser.getSize(description);
     }
 
     private String parseBrand(String description) {
         Brand brand = Brand.getBrandFrom(description);
         if (brand != null) {
             return brand.getFullName();
         }
         return null;
     }
 }
