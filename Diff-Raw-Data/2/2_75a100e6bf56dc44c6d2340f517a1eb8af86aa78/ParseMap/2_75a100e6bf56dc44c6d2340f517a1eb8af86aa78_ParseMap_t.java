 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tools;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 /**
  *
  * @author xbmc
  */
 public class ParseMap {
         
     public static String process(String result){
         StringBuilder map = new StringBuilder();
         Document doc = Jsoup.parse(result);
         Elements arealinks = doc.select("area[href]");
         Elements alinks = doc.select("a[href]");
         
         map.append(processMapLinks(arealinks,true));
         map.append(processMapLinks(alinks,false));
         map.append(processBreadCrumbLinks(alinks));
         
        return "<center><div style='width:350px;'>"+map.toString()+"</div></center>";
     }
     
     private static String processMapLinks(Elements links, Boolean force){
         StringBuilder map = new StringBuilder();
         for(int i=0; i<links.size(); i++){
             Element link = links.get(i);
             String href = link.attr("href");
             String kolImageBase="http://images.kingdomofloathing.com/";
             String kolMobilImageBase="http://gcarlson.github.com/kolmobil/images/";
             if(link.html().contains("<img")){
                 Elements images = link.children();
                 for(int j=0; j<images.size(); j++){
                     Element image = images.get(j);
                     String src=image.attr("src");
                     if(src!=null && !src.isEmpty()){
                         String relativeSrc = src.replace(kolImageBase, "");
                         String html = 
                                 "<a href='"+href+"'>"
                                 + "<img src='"+kolMobilImageBase+relativeSrc+"' onerror=\"this.src='"+kolImageBase+relativeSrc+"';\"'>"
                                 + "</a>";
                         map.append(html);
                     }
                 }
             } else if(force){
                 String png = kolMobilImageBase+"generated/"+href.replace(".php", ".png");
                 String html = "<a href='"+href+"'><img src='"+png+"' width=100 height=100/></a>";
                 map.append(html);
             }
         }
         return map.toString();
     }
     
     private static String processBreadCrumbLinks(Elements links){
         StringBuilder map = new StringBuilder();
         for(int i=0; i<links.size(); i++){
             Element link = links.get(i);
             if(!link.html().contains("<img")){
                 link=link.attr("data-role", "button");
                 map.append(link);
             }
         }
         return map.toString();
     }
 }
