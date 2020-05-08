 package com.zynick.comparison.sites;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.zynick.comparison.Constant;
 import com.zynick.comparison.Item;
 
 // parsing code works as of 2013-11-03
 public class YouBeli implements Website {
     
     @Override
     public List<Item> parse(String query, int size) throws IOException {
         
         // request for a page
         Document doc = Jsoup.connect("http://www.youbeli.com/advanced_search_result.php?keywords=" + query)
                             .userAgent(Constant.HTTP_USER_AGENT) 
                             .timeout(Constant.HTTP_TIMEOUT).get();
 
         ArrayList<Item> result = new ArrayList<Item>(size);
         
         Elements divS = doc.select("div.widthFull");
         if (divS.size() > 3) {
             divS.remove(0);
             divS.remove(0);
             divS.remove(0);
             
             int count = 0;
             loop: for (Element div : divS) {
                 if (count >= size)
                     break;
                 
                 for (Element productE : div.children()) { // "div.products_box"
                     if (count >= size)
                         break loop;
                     
                     Element aE = productE.child(0).child(0); // "div.products_image > a"
                     String url = aE.attr("href");
                     Element imgE = aE.child(0);
                     String img = imgE.attr("data-original");
                     String title = imgE.attr("title");
                     
                     Elements specialPriceE = productE.select("span.products_special_price");
                     String price = (specialPriceE.size() != 0) ? 
                            specialPriceE.first().text() :
                            productE.child(3).text();
                    price = price.substring(3).replaceAll(",", "");
                     double dPrice = Double.parseDouble(price);
                     
                     result.add(new Item("YouBeli", title, dPrice, img, url));
                     count++;
                 }
             }
         }
         
         return result;
     }
 }
