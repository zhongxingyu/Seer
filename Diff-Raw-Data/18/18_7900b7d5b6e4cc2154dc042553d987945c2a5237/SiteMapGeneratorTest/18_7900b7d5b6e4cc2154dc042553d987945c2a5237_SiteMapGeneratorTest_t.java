 package org.otherobjects.cms.seo;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.dom4j.Document;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.otherobjects.cms.model.DynaNode;
 
 public class SiteMapGeneratorTest extends TestCase
 {
 
     public void testFormatW3CDateTime()
     {
        DateTime dt = new DateTime(2007, 9, 22, 12, 23, 34, 0, DateTimeZone.forID("UTC"));
         SiteMapGenerator smg = new SiteMapGenerator();
         String s = smg.formatW3CDateTime(dt.toDate());
        assertEquals("2007-09-22T12:23:34+00:00", s);
     }
 
     public void testGenerateSiteMap() throws IOException
     {
 
         DateTime dt1 = new DateTime(2007, 9, 22, 12, 23, 34, 0, DateTimeZone.forID("+0200"));
         DateTime dt2 = new DateTime(2007, 8, 22, 12, 23, 34, 0, DateTimeZone.forID("+0200"));
         DateTime dt3 = new DateTime(2007, 10, 22, 12, 23, 34, 0, DateTimeZone.forID("+0200"));
         List<DynaNode> items = new ArrayList<DynaNode>();
         DynaNode node1 = new DynaNode();
         node1.setJcrPath("/articles/article-1.html");
         node1.setModificationTimestamp(dt1.toDate());
         DynaNode node2 = new DynaNode();
         node2.setJcrPath("/articles/article-2.html");
         node2.setModificationTimestamp(dt2.toDate());
         DynaNode node3 = new DynaNode();
         node3.setJcrPath("/articles/article-3.html");
         node3.setModificationTimestamp(dt3.toDate());
         items.add(node1);
         items.add(node2);
         items.add(node3);
 
         SiteMapGenerator smg = new SiteMapGenerator();
         Document siteMap = smg.generateSiteMap(items);
 
         // Output XML for info
         StringWriter sw = new StringWriter();
         OutputFormat outformat = OutputFormat.createPrettyPrint();
         outformat.setEncoding("UTF-8");
         XMLWriter writer = new XMLWriter(sw, outformat);
         writer.write(siteMap);
         writer.flush();
         System.out.print(sw.toString());
 
         assertEquals(3, siteMap.selectNodes("/urlset/url").size());
         assertEquals("/articles/article-3.html", siteMap.valueOf("/urlset/url[3]/loc"));
     }
 
 }
