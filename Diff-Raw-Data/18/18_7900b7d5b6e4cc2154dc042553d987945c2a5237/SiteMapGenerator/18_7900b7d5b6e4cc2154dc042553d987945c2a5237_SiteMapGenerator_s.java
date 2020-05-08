 package org.otherobjects.cms.seo;
 
 import java.util.Date;
 import java.util.List;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentFactory;
 import org.dom4j.Element;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.otherobjects.cms.model.DynaNode;
 import org.springframework.util.Assert;
 
 /**
  * Generates SiteMap protocol XML from a lit of nodes.
  * 
  * <p>See <a href="http://www.sitemaps.org/protocol.php">http://www.sitemaps.org/protocol.php</a>
  * for protocol details
  * 
  * @author rich
  *
  */
 public class SiteMapGenerator
 {
     private static final DateTimeFormatter w3cDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
 
     /**
      * Generates the XML. 
      * 
      * <p>FIXME Need a better interface than DynaNode
      * @param items
      * @return
      */
     public Document generateSiteMap(List<DynaNode> items)
     {
         Document doc = DocumentFactory.getInstance().createDocument();
         // Must be in UTF-8 according to spec
         doc.setXMLEncoding("UTF-8");
         Element urlset = doc.addElement("urlset");
         for (DynaNode item : items)
         {
             String linkPath = item.getLinkPath();
             Date modificationTimestamp = item.getModificationTimestamp();
 
             Assert.hasText(linkPath, "URL can not be null for item: " + item.getJcrPath());
             Assert.notNull(modificationTimestamp, "modificationTimestamp can not be null for item: " + item.getJcrPath());
 
             // Required elements
             Element entry = urlset.addElement("url");
             entry.addElement("loc").addText(linkPath);
 
             // Optional elements
             entry.addElement("lastmod").addText(formatW3CDateTime(modificationTimestamp));
             // recipient.addElement("changefreq").addText();
             // recipient.addElement("qriority").addText();
         }
         return doc;
     }
 
     /**
      * Formats a date into W3C DateTime format.
      * 
      * <p>See <a href="http://www.w3.org/TR/NOTE-datetime">http://www.w3.org/TR/NOTE-datetime</a> for the specification.
      * 
      * TODO Move to a utility package?
      * 
      * @param date
      * @return
      */
     public String formatW3CDateTime(Date date)
     {
        return w3cDateFormatter.print(date.getTime());
     }
 }
