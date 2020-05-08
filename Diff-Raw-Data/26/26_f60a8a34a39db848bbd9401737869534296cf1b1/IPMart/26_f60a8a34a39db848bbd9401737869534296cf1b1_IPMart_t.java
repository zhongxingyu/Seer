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
 public class IPMart implements Website {
     
     @Override
     public List<Item> parse(String query, int size) throws IOException {
         
         // request for a page
         Document doc = Jsoup.connect("http://www.ipmart.com.my/main/search.php?find=" + query)
                             .userAgent(Constant.HTTP_USER_AGENT) 
                             .timeout(Constant.HTTP_TIMEOUT).get();
         
         ArrayList<Item> result = new ArrayList<Item>(size);
         int count = 0;
         
         Elements rowS = doc
                 .select("table#productsBrowseTable > tbody")
                 .first().children();
         
         loop: for (int i = 0; i < rowS.size() - 1; i += 8) { // every 8 rows
             if (count >= size)
                 break;
             
             int colSize = rowS.get(i+1).children().size();
             for (int j = 1; j <= colSize; j += 2) {
                 
                if (count >= size)
                     break loop;
                 
                Element divE = rowS.get(i+3).child(j-1).child(0);  // "tr > td > div"
                if (divE.children().size() < 2)
                    continue;  // item out of stock
                
                 Element aE = rowS.get(i+1).child(j-1).child(0) // "tr > td > div"
                                  .child(1).child(0).child(0); // "> center > div > a"
                 String url = aE.attr("href");
                 String img = aE.child(0).attr("src");
                 String title = rowS.get(i+2).child(j-1).child(1).child(0).text(); // "tr > td > div > a"
                String price = divE.child(2).child(0).text(); // "div > b"
                 price = price.substring(3).replaceAll(",", "");
                 double dPrice = Double.parseDouble(price);
                 
                 result.add(new Item("IPMart", title, dPrice, img, url));
                 count++;
             }
         }
         
         return result;
     }
 }
