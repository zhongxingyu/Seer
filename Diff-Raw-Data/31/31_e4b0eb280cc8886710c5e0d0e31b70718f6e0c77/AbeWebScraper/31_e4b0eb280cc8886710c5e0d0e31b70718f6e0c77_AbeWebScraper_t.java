 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.michael.webScraper;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlDivision;
 import com.michael.webScraper.VO.BookVO;
 import com.michael.webScraper.VO.SellerVO;
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
 public class AbeWebScraper implements IWebScraper {
 
     public static final String abeURL = "www.abebooks.com/servlet/SearchResults?isbn=";
     
     @Override
     public void fetchDetails(String searchString) {
         
     }
 
     public void showDetails(String isbnNo) throws URISyntaxException, IOException {
         // create the final URL
         String finalURL = abeURL + isbnNo;
         // now create the final URL
         URIBuilder builder = new URIBuilder();
         builder.setScheme("http").setHost(finalURL);
         URI uri = builder.build();
         System.out.println("Abe URL found :" + uri.toString());
 
         // fetch details
         final WebClient webClient = new WebClient();
         final HtmlPage page = webClient.getPage(uri.toString());
 
         //get list of all tbody elements which has class as "result"
         final List<?> resultList = page.getByXPath("//div[@class='result']");
         Iterator it = resultList.iterator();
 
         try{
           DatabaseUtil.establishDatabaseConnection();
           DatabaseUtil.initialize();
           
           List<SellerVO> sellerList = new ArrayList<SellerVO>();
           SellerVO seller;
           String value, bookName="", author="";
           HtmlDivision divElement;
           while (it.hasNext()) {
               seller = new SellerVO();
               divElement = (HtmlDivision) it.next();
               
               if(bookName.isEmpty()){
                  bookName = divElement.getElementsByAttribute("h2", "class", "title").get(0).getFirstElementChild().getFirstElementChild().asText();
                  System.out.println("Book Name >> " +bookName); 
               }
               
               if(author.isEmpty()){
                  author = divElement.getElementsByAttribute("div", "class", "author").get(0).asText();
                  System.out.println("Author >> " +author); 
               }              
                             
               value = divElement.getElementsByAttribute("div", "class", "bookseller").get(0).getFirstElementChild().asText();
               System.out.println("seller : " +value);
               seller.setName(value);
               
               value = divElement.getElementsByAttribute("div", "class", "item-price").get(0).asText();
               System.out.println("price : " +value);
               seller.setPrice(value);                      
 
               value = divElement.getElementsByAttribute("div", "class", "bookseller-rating").get(0).getFirstElementChild().getNextElementSibling().getFirstElementChild().getAttribute("alt");
               System.out.println("rating : " +value);
               seller.setRating(value);
               
               value = divElement.getElementsByAttribute("div", "class", "bookseller-location").get(0).asText();
               System.out.println("Full location : " +value);
               value = stripData(value);
               System.out.println("Striped location : " +value);
               seller.setZip_location(value);
               
               System.out.println("SellerVO : "+seller);
               sellerList.add(seller);                                        
               }
                                        
               BookVO book = BookVO.create(bookName, author, sellerList);          
               System.out.println("BookVO : "+book);       
 
               if(DatabaseUtil.create(book, Source.ABE)){
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
        /*if(value != null || value.isEmpty()) {
             return null;
         }
         /*String value = "
         In Stock. 
 
 
             Ships from AL, United States.
 
             Expedited shipping available.
 
 
         ";*/
        /*try {
             
         value = value.trim();
         String values [] = value.split(",");
 	System.out.println("Values 0 "+values[0]);
 	System.out.println("Values 1 "+values[1].trim());
         System.out.println("Values 2 "+values[2]);
         return values[1].trim();
         } catch(Exception e) {
             System.out.println("Does Not Have Zip .. ");
                    
         return null;
        }*/
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
