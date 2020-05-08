 package com.ecyrd.jspwiki.xmlrpc;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.xmlrpc.XmlRpcException;
 
 import com.ecyrd.jspwiki.TestEngine;
 import com.ecyrd.jspwiki.WikiPage;
 import com.ecyrd.jspwiki.attachment.Attachment;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author $author$
  * @version $Revision$
  */
 public class RPCHandlerTest
         extends TestCase
 {
     /** DOCUMENT ME! */
     static final String NAME1 = "Test";
 
     /** DOCUMENT ME! */
     TestEngine m_engine;
 
     /** DOCUMENT ME! */
     RPCHandler m_handler;
 
     /**
      * Creates a new RPCHandlerTest object.
      *
      * @param s DOCUMENT ME!
      */
     public RPCHandlerTest(String s)
     {
         super(s);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws Exception DOCUMENT ME!
      */
     public void setUp()
             throws Exception
     {
        Configuration conf = TestEngine.getConfiguration("/jspwiki_auth.properties");
 
         m_engine = new TestEngine(conf);
 
         m_handler = new RPCHandler();
         m_handler.initialize(m_engine);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws Exception DOCUMENT ME!
      */
     public void tearDown()
             throws Exception
     {
         m_engine.cleanup();
     }
 
     /**
      * DOCUMENT ME!
      */
     public void testNonexistantPage()
     {
         try
         {
             byte [] res = m_handler.getPage("NoSuchPage");
             fail("No exception for missing page.");
         }
         catch (XmlRpcException e)
         {
             assertEquals("Wrong error code.", RPCHandler.ERR_NOPAGE, e.code);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws Exception DOCUMENT ME!
      */
     public void testRecentChanges()
             throws Exception
     {
         String text = "Foo";
         String pageName = NAME1;
 
         m_engine.saveText(pageName, text);
 
         WikiPage directInfo = m_engine.getPage(NAME1);
 
         Date modDate = directInfo.getLastModified();
 
         Calendar cal = Calendar.getInstance();
         cal.setTime(modDate);
         cal.add(Calendar.HOUR, -1);
 
         // Go to UTC
         cal.add(
             Calendar.MILLISECOND,
             -(cal.get(Calendar.ZONE_OFFSET)
             + (cal.getTimeZone().inDaylightTime(modDate)
             ? cal.get(Calendar.DST_OFFSET)
             : 0)));
 
         Vector v = m_handler.getRecentChanges(cal.getTime());
 
         assertEquals("wrong number of changes", 1, v.size());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws Exception DOCUMENT ME!
      */
     public void testRecentChangesWithAttachments()
             throws Exception
     {
         String text = "Foo";
         String pageName = NAME1;
 
         m_engine.saveText(pageName, text);
 
         Attachment att = new Attachment(NAME1, "TestAtt.txt");
         att.setAuthor("FirstPost");
         m_engine.getAttachmentManager().storeAttachment(att, m_engine.makeAttachmentFile());
 
         WikiPage directInfo = m_engine.getPage(NAME1);
 
         Date modDate = directInfo.getLastModified();
 
         Calendar cal = Calendar.getInstance();
         cal.setTime(modDate);
         cal.add(Calendar.HOUR, -1);
 
         // Go to UTC
         cal.add(
             Calendar.MILLISECOND,
             -(cal.get(Calendar.ZONE_OFFSET)
             + (cal.getTimeZone().inDaylightTime(modDate)
             ? cal.get(Calendar.DST_OFFSET)
             : 0)));
 
         Vector v = m_handler.getRecentChanges(cal.getTime());
 
         assertEquals("wrong number of changes", 1, v.size());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws Exception DOCUMENT ME!
      */
     public void testPageInfo()
             throws Exception
     {
         String text = "Foobar.";
         String pageName = NAME1;
 
         m_engine.saveText(pageName, text);
 
         WikiPage directInfo = m_engine.getPage(NAME1);
 
         Hashtable ht = m_handler.getPageInfo(NAME1);
 
         assertEquals("name", (String) ht.get("name"), NAME1);
 
         Date d = (Date) ht.get("lastModified");
 
         Calendar cal = Calendar.getInstance();
         cal.setTime(d);
 
         System.out.println("Real: " + directInfo.getLastModified());
         System.out.println("RPC:  " + d);
 
         // Offset the ZONE offset and DST offset away.  DST only
         // if we're actually in DST.
         cal.add(
             Calendar.MILLISECOND,
             (cal.get(Calendar.ZONE_OFFSET)
             + (cal.getTimeZone().inDaylightTime(d)
             ? cal.get(Calendar.DST_OFFSET)
             : 0)));
         System.out.println("RPC2: " + cal.getTime());
 
         assertEquals("date", cal.getTime().getTime(), directInfo.getLastModified().getTime());
     }
 
     /**
      * Tests if listLinks() works with a single, non-existant local page.
      *
      * @throws Exception DOCUMENT ME!
      */
     public void testListLinks()
             throws Exception
     {
         String text = "[Foobar]";
         String pageName = NAME1;
 
         m_engine.saveText(pageName, text);
 
         Vector links = m_handler.listLinks(pageName);
 
         assertEquals("link count", 1, links.size());
 
         Hashtable linkinfo = (Hashtable) links.elementAt(0);
 
         assertEquals("name", "Foobar", linkinfo.get("page"));
         assertEquals("type", "local", linkinfo.get("type"));
         assertEquals("href", "Edit.jsp?page=Foobar", linkinfo.get("href"));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws Exception DOCUMENT ME!
      */
     public void testListLinksWithAttachments()
             throws Exception
     {
         String text = "[Foobar] [Test/TestAtt.txt]";
         String pageName = NAME1;
 
         m_engine.saveText(pageName, text);
 
         Attachment att = new Attachment(NAME1, "TestAtt.txt");
         att.setAuthor("FirstPost");
         m_engine.getAttachmentManager().storeAttachment(att, m_engine.makeAttachmentFile());
 
         // Test.
         Vector links = m_handler.listLinks(pageName);
 
         assertEquals("link count", 2, links.size());
 
         Hashtable linkinfo = (Hashtable) links.elementAt(0);
 
         assertEquals("edit name", "Foobar", linkinfo.get("page"));
         assertEquals("edit type", "local", linkinfo.get("type"));
         assertEquals("edit href", "Edit.jsp?page=Foobar", linkinfo.get("href"));
 
         linkinfo = (Hashtable) links.elementAt(1);
 
         assertEquals("att name", NAME1 + "/TestAtt.txt", linkinfo.get("page"));
         assertEquals("att type", "local", linkinfo.get("type"));
         assertEquals("att href", "attach/" + NAME1 + "/TestAtt.txt", linkinfo.get("href"));
     }
 
     /*
      * TODO: ENABLE
     public void testPermissions()
         throws Exception
     {
         String text ="Blaa. [{DENY view Guest}] [{ALLOW view NamedGuest}]";
 
         m_engine.saveText( NAME1, text );
 
         try
         {
             Vector links = m_handler.listLinks( NAME1 );
             fail("Didn't get an exception in listLinks()");
         }
         catch( XmlRpcException e ) {}
 
         try
         {
             Hashtable ht = m_handler.getPageInfo( NAME1 );
             fail("Didn't get an exception in getPageInfo()");
         }
         catch( XmlRpcException e ) {}
     }
     */
     public static Test suite()
     {
         return new TestSuite(RPCHandlerTest.class);
     }
 }
