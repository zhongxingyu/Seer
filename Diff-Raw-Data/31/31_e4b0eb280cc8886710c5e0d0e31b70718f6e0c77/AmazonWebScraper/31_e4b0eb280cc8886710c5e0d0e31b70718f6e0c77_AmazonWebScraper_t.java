 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.michael.webScraper;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlDivision;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
 import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
 import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
 import com.gargoylesoftware.htmlunit.html.DomElement;
 import com.gargoylesoftware.htmlunit.html.HtmlElement;
 import com.gargoylesoftware.htmlunit.html.HtmlImage;
 import com.michael.webScraper.VO.BookVO;
 import com.michael.webScraper.VO.SellerVO;
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import org.apache.http.client.utils.URIBuilder;
 
 /**
  *
  * @author 
  */
 public class AmazonWebScraper implements IWebScraper {
 
     private static final String amazonBookSearchURL = "http://www.amazon.com/s/ref=nb_sb_ss_i_0_21?url=search-alias%3Dstripbooks&field-keywords=harry%20potter%20and%20the%20sorcerer's%20stone&sprefix=harry+potter+and+the+%2Cstripbooks%2C542&rh=i%3Astripbooks%2Ck%3Aharry%20potter%20and%20the%20sorcerer's%20stone";
     private static final String amazonBookSearchURL1 = "http://www.amazon.com/s/ref=nb_sb_ss_i_0_21?url=search-alias%3Dstripbooks&field-keywords=#&sprefix=#+%2Cstripbooks%2C542&rh=i%3Astripbooks%2Ck%3A#";
     private static final String amazonBookSearchURL2 = "http://www.amazon.com/s/?url=search-alias%3Dstripbooks&field-keywords=&";
     private static final String amazonAllLink = "http://www.amazon.com/gp/offer-listing/059035342X/";
     // this is the url which we would append the ISBN no
     private static final String amazonBookFinalURL = "www.amazon.com/gp/offer-listing/";
     private static final String altImageStr = "Return to product information";
     //private static final String imagePath = "C:\\Users\\Shweta\\Work\\myspace\\projects\\OtherProjects\\scraper";
     private static final String imagePath = ".";
 
     @Override
     public void fetchDetails(String searchString) {
         try {
             String searchURL = resolveURL(searchString);
             // find the ISBN no
             findISBNNo(searchString);
         } catch (Exception e) {
             System.out.println("Exception...");
         }
     }
 
     public String resolveURL(String searchString) throws Exception {
         URIBuilder builder = new URIBuilder();
         builder.setScheme("http").setHost("www.amazon.com/s").setPath("/ref=nb_sb_ss_i_0_21")
                 .setParameter("url", "search-alias%3Dstripbooks")
                 .setParameter("field-keywords", searchString)
                 .setParameter("sprefix", searchString)
                 .setParameter("rh", searchString);
         URI uri = builder.build();
         System.out.println("Found URL : " + uri.toString());
         String newStr = uri.toString().replaceFirst("%25", "%");
         System.out.println("Modified URL : " + newStr);
         return newStr;
     }
 
     /**
      * Finds the ISBN no of the book if we provide the URL of the Amazon web
      * page to it.
      *
      * @param url
      */
     public String findISBNNo(String url) throws IOException {
         final WebClient webClient = new WebClient();
         final HtmlPage page = webClient.getPage(url);
         final String pageAsXml = page.asXml();
         final String pageAsText = page.asText();
         final HtmlDivision div = page.getHtmlElementById("result_0");
         System.out.println("Div found : " + div.getAttribute("name"));
 
         String isbnNo = div.getAttribute("name");
         webClient.closeAllWindows();
         return isbnNo;
     }
     
     public void getImage(String isbnNo) throws IOException, URISyntaxException {
         String finalURL = amazonBookFinalURL + isbnNo;
         //String finalURL = "www.amazon.com/gp/offer-listing/059035342X/";
         // now create the final URL
         URIBuilder builder = new URIBuilder();
         builder.setScheme("http").setHost(finalURL);
         URI uri = builder.build();
         System.out.println(">>>>>>>>>>>>" + uri.toString());
 
         // fetch details
         final WebClient webClient = new WebClient();
         final HtmlPage page = webClient.getPage(uri.toString());
 
         // now get the image
         HtmlImage image = page.<HtmlImage>getFirstByXPath("//img[@alt='Return to product information']");
         String imageLocation = imagePath + "image-"+ isbnNo +".jpg";
         File imageFile = new File(imageLocation);
         image.saveAs(imageFile);
     }
     
     public String getImageLocation(String isbnNo) {     
         return imagePath + "image-"+ isbnNo +".jpg"; 
     }
 
     public void showDetails(String isbnNo) throws URISyntaxException, IOException {
         // create the final URL
         String finalURL = amazonBookFinalURL + isbnNo;
         // now create the final URL
         URIBuilder builder = new URIBuilder();
         builder.setScheme("http").setHost(finalURL);
         URI uri = builder.build();
         System.out.println("URL found :" + uri.toString());
 
         // fetch details
         final WebClient webClient = new WebClient();
         final HtmlPage page = webClient.getPage(uri.toString());
 
         //get list of all tbody elements which has class as "result"
         final List<?> tbodyResultList = page.getByXPath("//tbody[@class='result']");
 
         //get div which has a 'name' attribute of 'John'
         //final HtmlDivision div = (HtmlDivision) page.getByXPath("//div[@name='John']").get(0);
         System.out.println("List has elements >> ");
 
         Iterator it = tbodyResultList.iterator();
 
         try{
           DatabaseUtil.establishDatabaseConnection();
           DatabaseUtil.initialize();
           
           List<SellerVO> sellerList = new ArrayList<SellerVO>();
           while (it.hasNext()) {
                   HtmlTableBody tableBodyElement = (HtmlTableBody) it.next();
                   System.out.println("\n " + tableBodyElement);
                   // get the table rows
                   List<HtmlTableRow> tableRows = tableBodyElement.getRows();
                   // now get the iterator on table row          
 
                   String className ;         
                   SellerVO seller;
                   for (HtmlTableRow row : tableBodyElement.getRows()) {
                       System.out.println("------------------ Next Row -----------------");
                       seller = new SellerVO();
                       for (HtmlTableCell cell : row.getCells()) {
                           for(DomElement element : cell.getChildElements()){
                               className = element.getAttribute("class");
                               if(className.equalsIgnoreCase("price")){
                                   System.out.println("Found Price : "+element.asText());
                                   seller.setPrice(element.asText().trim());
                               } else if(className.equalsIgnoreCase("condition")){
                                   System.out.println("Found Condition : "+element.asText());
                                   seller.setCondition(element.asText().trim());
                               } else if(className.equalsIgnoreCase("sellerInformation")){
                                   for(DomElement child1 : element.getChildElements()){
                                      for(DomElement child2 : child1.getChildElements()){                                   
                                          if(child2.getAttribute("class").equalsIgnoreCase("seller")){
                                              for(DomElement child3 : child2.getChildElements()){
                                                   if(!child3.getAttribute("class").equalsIgnoreCase("sellerHeader")){
                                                       System.out.println("Found Seller : "+child3.asText());
                                                       seller.setName(child3.asText().trim());
                                                   }   
                                              }
                                          } else if(child2.getAttribute("class").equalsIgnoreCase("rating")){
                                              for(DomElement child3 : child2.getChildElements()){
                                                   if(!child3.getAttribute("class").equalsIgnoreCase("ratingHeader") || !child3.getAttribute("class").equalsIgnoreCase("olpSellerStars")){
                                                       if(child3.getId().startsWith("rating")){
                                                           System.out.println("Found Rating : "+child3.asText());
                                                           seller.setRating(child3.asText().trim());                                               
                                                       }                                                
                                                    }   
                                              }
                                          }                        
                                       }
                                    } 
                               }   
                           }
                       }
                       
                       // Scrape the amazon zipcode as well
                 /*     String value= null;
                       //get list of all tbody elements which has class as "availability"
                       final List<?> resultList = page.getByXPath("//div[@class='availability']");
                       Iterator ait = resultList.iterator();
                       HtmlDivision divElement;
                       while (ait.hasNext()) {
                           divElement = (HtmlDivision) ait.next();
                           // now pass the text and get the state if there is any otherwise NULL
                           value = divElement.getElementsByAttribute("div", "class", "availability").get(0).asText();
                           value = stripData(value);
                      } */
                       System.out.println("SellerVO : "+seller);
                       sellerList.add(seller);                                        
                   }
               }
                 
               HtmlElement elm = page.getHtmlElementById("olpProductByLine");
               String bookName  = elm.getPreviousElementSibling().asText().trim();
               String author  = elm.asText().trim();
               System.out.println("Book name :"+bookName);
               System.out.println("Author :"+author);
               BookVO book = BookVO.create(bookName, author, sellerList);          
               System.out.println("AmazonBookVO : "+book);       
 
               if(DatabaseUtil.create(book, Source.AMAZON)){
                   LogUtil.log(book);
               }  
                            
         } catch(Exception e){
             System.out.println ("Exception :"+e);
         } finally {
             webClient.closeAllWindows(); 
             
             DatabaseUtil.finish();
         }      
     }
     
     public static String stripData(String value) {
         //String value = "(Red Lion, PA, U.S.A.)";
         try {
             
         
         String values [] = value.split(",");
 	System.out.println("Values 0 "+values[0]);
 	System.out.println("Values 1 "+values[1].trim());
         System.out.println("Values 2 "+values[2]);
         return values[1].trim();
         } catch(Exception e) {
             System.out.println("Does Not Have Zip .. ");
                    
         return null;
         }
         
     }
 }
