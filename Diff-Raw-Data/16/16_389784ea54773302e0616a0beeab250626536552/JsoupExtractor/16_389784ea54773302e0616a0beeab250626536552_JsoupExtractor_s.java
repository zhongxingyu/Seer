 package org.apache.camel.processor.mashup.extractor;
 
 import org.apache.camel.processor.mashup.api.IExtractor;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 /**
  * Extractor which use jsoup to extract data.
  */
 public class JsoupExtractor implements IExtractor {
     
     private String query;
     
     public JsoupExtractor() { }
     
     public String extract(String html) throws Exception {
         Document document = Jsoup.parse(html);
         Element element = document.select(query).first();
         if (element == null) {
             return null;
         }
        return element.toString();
     }
 
     public String getQuery() {
         return query;
     }
 
     public void setQuery(String query) {
         this.query = query;
     }
 
 }
