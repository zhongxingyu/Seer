 /**
  * Bristlecone Test Tools for Databases
  * Copyright (C) 2006-2007 Continuent Inc.
  * Contact: bristlecone@lists.forge.continuent.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of version 2 of the GNU General Public License as
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
  *
  * Initial developer(s): Robert Hodges and Ralph Hannus.
  * Contributor(s):
  */
 
 package com.continuent.bristlecone.evaluator.test;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.dom4j.Attribute;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.io.SAXReader;
 
 import com.continuent.bristlecone.evaluator.Configuration;
 import com.continuent.bristlecone.evaluator.Evaluator;
 import com.continuent.bristlecone.evaluator.EvaluatorThread;
 import com.continuent.bristlecone.evaluator.Statistics;
 import com.continuent.bristlecone.evaluator.TableGroup;
 import com.continuent.bristlecone.evaluator.ThreadConfiguration;
 
 import junit.framework.TestCase;
 
 public class EvaluatorTest extends TestCase
 {
   private Connection conn = null;
   protected void setUp() throws Exception
   {
     super.setUp();
   }
 
   protected void tearDown() throws Exception
   {
     if (conn != null)
     {
       try 
       {
         conn.close();
         conn = null;
       }
       catch (SQLException e)
       {
         // ignore it
       }
     }
     super.tearDown();
   }
   
   private Document readFile(File file) throws DocumentException
   {
     SAXReader reader = new SAXReader();
     Document document = reader.read(file);
     return document;
   }
   
   private void checkCount(String tbl, int count) throws Exception
   {
     Statement s = conn.createStatement();
     ResultSet rs = s.executeQuery("select count(*) from " + tbl);
     
     assertTrue(rs.next());
     int rows = rs.getInt(1);
     assertEquals(count, rows);
     assertFalse(rs.next());
   }
 
   /** Tests HSQL with default heavyweight queries. */
   public void testHsqlSample_Heavy() throws Exception
   {
     execHsqlTest(new Configuration("config/evaluator/hsql_sample.xml"));
   }
   
   /** Tests HSQL with medium-weight queries. */
   public void testHsqlSample_Medium() throws Exception
   {
     execHsqlTest(new Configuration("config/evaluator/hsql_sample_medium.xml"));
   }
   
   /** Tests HSQL with default lightweight queries. */
   public void testHsqlSample_Light() throws Exception
   {
     execHsqlTest(new Configuration("config/evaluator/hsql_sample_light.xml"));
   }
   
   /** Runs HSQL test with a particular configuration file. */
   private void execHsqlTest(Configuration config) throws Exception
   {
     Evaluator eval = new Evaluator(config);
     long startTime = System.currentTimeMillis();
     eval.run();
     long runTime = System.currentTimeMillis() - startTime;
     assertTrue(runTime > 10000);
     conn = eval.getConnection();
     checkCount("tbl1", 100);
     checkCount("tbl2", 100);
     checkCount("tbl3", 10000);
     int count = 0;
     for (Iterator i = eval.getThreads().iterator(); i.hasNext();)
     {
       count++;
       EvaluatorThread t = (EvaluatorThread)i.next();
       assertFalse(t.isAlive());
     }
     assertEquals(15, count);
     count = 0;
     for (Iterator i = eval.getStats(); i.hasNext(); )
     {
       Statistics s = (Statistics)i.next();
       count++;
       assertEquals(0, s.getDeletes());
       assertEquals(0, s.getUpdates());
       assertEquals(0, s.getInserts());
       if (count < 6)
       {
        assertEquals(1, (int)s.getInterval());
       }
     }
     assertEquals(6, count);
   }
 
   /**
    * Assertion: errors encountered by the EvaluatorThreads will
    * be returned in the failures list.
    * 
    * Plan: Start evaluator in a thread. Let the threads run for 
    * a while drop one of the tables they depend on and let 
    * the threads run for a while and collect the failures.
    * 
    * @throws Exception
    */
   public void testFailures() throws Exception
   {
     Configuration  config = new Configuration("config/evaluator/hsql_sample.xml");
     config.setTestDuration(3);
     final Evaluator eval = new Evaluator(config);
 
     eval.initialize();
     Thread t = new Thread(eval);
     t.start();
     conn = eval.getConnection();
     Statement s = conn.createStatement();
     TableGroup tg = (TableGroup)config.getTableGroups().get(0);
     s.execute("drop table " + tg.getBase1TableName());
     t.join(10000);
     assertFalse(t.isAlive());
     List failures = eval.getFailures();
     assertTrue(failures.size() > 0);
   }
   
   public void testNoInitializeDDL() throws Exception
   {
     Configuration config = new Configuration("config/evaluator/hsql_sample.xml");
     TableGroup tg = (TableGroup)config.getTableGroups().get(0);
     tg.setInitializeDDL(false);
     tg.setTruncateTable("delete from ");
     Evaluator eval = new Evaluator(config);
     eval.run();
   }
   
   public void testStop() throws Exception
   {
     Configuration  config = new Configuration("config/evaluator/hsql_sample.xml");
     config.setTestDuration(300);
 
     TableGroup tg = (TableGroup)config.getTableGroups().get(0);
     ThreadConfiguration tc = (ThreadConfiguration)tg.getThreads().get(0);
     // make sure all the threads start immediately
     tc.setRampUpIncrement(0);
     
     final Evaluator eval = new Evaluator(config);
 
     eval.initialize();
     Thread t = new Thread(eval);
     t.start();
     Thread.sleep(1000);
     eval.stop();
     t.join(10000);
     assertFalse(t.isAlive());
     assertTrue(eval.getFailures().isEmpty());
   }
   
   private int getIntValue(List headers, List cells, String label)
   {
     Element cell = (Element)cells.get(headers.indexOf(label));
     return Integer.parseInt(cell.getStringValue().trim());
   }
   private void checkValue(List headers, List cells, String label, int value)
   {
     assertEquals(value, getIntValue(headers, cells, label));
   }
   
   private void checkValue(Element element, String attr, int value)
   {
 
     Attribute attribute = element.attribute(attr);
     assertEquals(value ,Integer.parseInt(attribute.getValue()));
   }
   public void testOutput() throws Exception
   {
     File xml = File.createTempFile("test", ".xml");
     File html = File.createTempFile("test", ".xml");
     try
     {
       Configuration config = new Configuration("config/evaluator/hsql_sample.xml");
       
       config.setXmlFile(xml.getPath());
       TableGroup tg = (TableGroup)config.getTableGroups().get(0);
       ThreadConfiguration tc = (ThreadConfiguration)tg.getThreads().get(0);
      
       tc.setUpdatePercentage(9);
       tc.setDeletePercentage(7);
       tc.setInsertPercentage(5);
       config.setHtmlFile(html.getPath());
       Evaluator eval = new Evaluator(config);
       eval.run();
       assertTrue(xml.exists());
       assertTrue(html.exists());
       Document xmlDoc = readFile(xml);
       Element root = xmlDoc.getRootElement();
       assertEquals("EvaluatorResults", root.getName());
       Iterator statsIter = eval.getStats();
       for (Iterator iter = root.elementIterator(); iter.hasNext();)
       {
         Element stats = (Element)iter.next();
         Statistics stat = (Statistics)statsIter.next();
         assertEquals("Stats", stats.getName());
         checkValue(stats, "avgResponseTime", stat.getAverageResponseTime());
         checkValue(stats, "queries", stat.getQueries());
         checkValue(stats, "users", stat.getThreads());
         checkValue(stats, "updates", stat.getUpdates());
         checkValue(stats, "deletes", stat.getDeletes());
         checkValue(stats, "inserts", stat.getInserts());
       }
       Document htmlDoc = readFile(html);
       root = htmlDoc.getRootElement();
       assertEquals("HTML", root.getName());
       Iterator e = root.elementIterator("BODY");
       Element body = (Element)e.next();
       assertFalse(e.hasNext());
       e = body.elementIterator();
       Element table = (Element)e.next();
       assertFalse(e.hasNext());
       Iterator rows = table.elementIterator();
       Element header = (Element)rows.next();
       List headers = new ArrayList();
       for (Iterator h = header.elementIterator(); h.hasNext();)
       {
         Element label = (Element)h.next();
         headers.add(label.getStringValue().trim());
         
       }
       statsIter = eval.getStats();
       int time = 0;
       while (rows.hasNext())
       {
         Statistics stats = (Statistics)statsIter.next();
         Element row = (Element)rows.next();
         List cells = row.elements();
         time += getIntValue(headers, cells, "interval");
         checkValue(headers, cells, "queries", stats.getQueries());
         checkValue(headers, cells, "average response time", 
             stats.getAverageResponseTime());
         checkValue(headers, cells, "users", stats.getThreads());
         checkValue(headers, cells, "updates", stats.getUpdates());
         checkValue(headers, cells, "inserts", stats.getInserts());
         checkValue(headers, cells, "deletes", stats.getDeletes());
         checkValue(headers, cells, "time", time);
       }
     }
     finally
     {
       xml.delete();
       html.delete();
     }
   }
 }
