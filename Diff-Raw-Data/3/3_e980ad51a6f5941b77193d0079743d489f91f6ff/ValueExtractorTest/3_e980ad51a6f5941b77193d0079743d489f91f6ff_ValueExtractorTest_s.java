 package com.zenoss.zenpacks.zenjmx;
 
 import java.lang.management.ManagementFactory;
 import java.lang.management.MemoryManagerMXBean;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import javax.management.openmbean.CompositeData;
 import javax.management.openmbean.CompositeDataSupport;
 import javax.management.openmbean.TabularData;
 import javax.management.openmbean.TabularDataSupport;
 
 import junit.framework.TestCase;
 
 import com.zenoss.jmx.JmxException;
 import com.zenoss.jmx.ValueExtractor;
 import com.zenoss.zenpacks.zenjmx.call.ZenJMXTest;
 
 public class ValueExtractorTest extends TestCase {
 
     private MBeanServer mbs = null;
     private CompositeData testComposite = null;
     private TabularData testTabular = null;
     private TabularData testSimpleTabular = null;
 
     Object gcObject = null;
     ObjectName testObjectName = null;
     ObjectName gcObjectName = null;
     boolean isOneSix = false;
 
     @Override
     protected void setUp() throws Exception
         {
 
         String version = System.getProperty("java.version").substring(0, 3);
 
         if ( Float.parseFloat(version) > 1.5 ) isOneSix = true;
 
         mbs = ManagementFactory.getPlatformMBeanServer();
         // objectName = new
 
         testObjectName = new ObjectName(ZenJMXTest.mbeanObjectNameStr);
         if ( isOneSix )
             {
             // some of the tests can not be run unless using java 1.6
             // because ZenJMXTest is an MXBean.
             // MXBeans are 1.6 only and are used for testing because they
             // automatically convert return values to composite or tabular data
             System.out.println("Running 1.6 compatible tests");
             if ( !mbs.isRegistered(testObjectName) )
                 {
                 ZenJMXTest.registerMbean(mbs);
                 }
 
             Object o = mbs.getAttribute(testObjectName, "CompositeTestData");
             testComposite = (CompositeData) o;
 
             o = mbs.getAttribute(testObjectName, "TabularTestData");
             testTabular = (TabularData) o;
 
             o = mbs.getAttribute(testObjectName, "SimpleTabularTestData");
             testSimpleTabular = (TabularData) o;
             }
 
         // lets find a registered gc mbean
         for (MemoryManagerMXBean gc : ManagementFactory
                 .getMemoryManagerMXBeans())
             {
             String name = gc.getName();
             gcObjectName = new ObjectName(
                     "java.lang:type=GarbageCollector,name=" + name);
             if ( mbs.isRegistered(gcObjectName) )
                 {
                 gcObject = mbs.getAttribute(gcObjectName, "LastGcInfo");
                 break;
                 }
             }
 
         }
 
     public void testSimpleTabularPath() throws Exception
         {
         if ( !isOneSix )
             {
             return;
             }
         Object o = ValueExtractor.getDataValue(testSimpleTabular, "rowOne");
         assertEquals(5, o);
         }
 
     public void testSimpleTabularPathWithIndex() throws Exception
         {
         if ( !isOneSix )
             {
             return;
             }
         Object o = ValueExtractor.getDataValue(testSimpleTabular, "[rowOne]");
         assertEquals(5, o);
         }
 
     public void testSimpleTabularBadPath() throws Exception
         {
         if ( !isOneSix )
             {
             return;
             }
         try
             {
             ValueExtractor.getDataValue(testSimpleTabular, "[rowOnse]");
             }
         catch (JmxException e)
             {
             return;
             }
         fail("expected an exception");
         }
 
     public void testSimpleCompositePath() throws Exception
         {
         if ( !isOneSix )
             {
             return;
             }
         Object o = ValueExtractor.getDataValue(testComposite, "stringValue");
         assertEquals("123", o);
         }
 
     public void testCompositePathAndIndex() throws Exception
         {
         if ( !isOneSix )
             {
             return;
             }
         Object o = ValueExtractor.getDataValue(testComposite,
                 "nested.rows.[rowTwo].rowValue");
         assertEquals(3, o);
         }
 
     public void testCompositePathAndDotIndex() throws Exception
         {
         if ( !isOneSix )
             {
             return;
             }
         Object o = ValueExtractor.getDataValue(testComposite,
                 "nested.rows.[row.Three].rowValue");
         assertEquals(1, o);
         }
 
     public void testSimpleCompositeBadPath() throws Exception
         {
         if ( !isOneSix )
             {
             return;
             }
         try
             {
             ValueExtractor.getDataValue(testComposite, "[rowOnse]");
             }
         catch (JmxException e)
             {
             return;
             }
         fail("expected an exception");
         }
 
     public void testTabularPath() throws Exception
         {
         if ( !isOneSix )
             {
             return;
             }
         Object o = ValueExtractor.getDataValue(testTabular,
                 "rowOne.anotherRowValue");
         assertEquals(654, o);
         }
 
     public void testTabularPathWithIndex() throws Exception
         {
         if ( !isOneSix )
             {
             return;
             }
         Object o = ValueExtractor.getDataValue(testTabular,
                 "[rowOne].differentRowValue");
         assertEquals(384, o);
         }
 
     public void testTabularPathWithDotIndex() throws Exception
         {
         if ( !isOneSix )
             {
             return;
             }
         Object o = ValueExtractor.getDataValue(testTabular,
                 "[row.Three].differentRowValue");
         assertEquals(2, o);
         }
 
     public void testTabularBadPath() throws Exception
         {
         if ( !isOneSix )
             {
             return;
             }
         try
             {
             ValueExtractor.getDataValue(testTabular, "rowOne.blam");
             }
         catch (JmxException e)
             {
             return;
             }
         fail("expected an exception");
         }
 
     public void testGetGcThreadCount() throws Exception
         {
 
         Object result = ValueExtractor.getDataValue(gcObject, "GcThreadCount");
         assertEquals(Integer.class, result.getClass());
 
         }
 
     public void testGetMemoryUsageAfterGc() throws Exception
         {
 
         Object result = ValueExtractor.getDataValue(gcObject,
                 "memoryUsageAfterGc");
         assertEquals(TabularDataSupport.class, result.getClass());
 
         }
 
     public void testGetMemoryUsageAfterGcEdenSpace() throws Exception
         {
 
         Object result = ValueExtractor.getDataValue(gcObject,
                 "memoryUsageAfterGc.Code Cache");
         assertEquals(CompositeDataSupport.class, result.getClass());
 
         }
 
     public void testGetMemoryUsageAfterGcEdenSpaceWithIndex() throws Exception
         {
 
         Object result = ValueExtractor.getDataValue(gcObject,
                 "memoryUsageAfterGc.[Code Cache]");
         assertEquals(CompositeDataSupport.class, result.getClass());
 
         }
 
     public void testGetMemoryUsageAfterGcEdenSpaceWithBadIndex()
             throws Exception
         {
         try
             {
 
             ValueExtractor.getDataValue(gcObject,
                     "memoryUsageAfterGc.[Code Cache, Committed]");
             }
         catch (JmxException e)
             {
             return;
             }
         fail("expected an exception");
 
         }
 
     public void testGetMemoryUsageAfterGcEdenSpaceCommitted() throws Exception
         {
 
         Object result = ValueExtractor.getDataValue(gcObject,
                 "memoryUsageAfterGc.Code Cache.committed");
         assertEquals(Long.class, result.getClass());
 
         }
 
     public void testGetMemoryUsageAfterGcEdenSpaceCommittedWithIndex()
             throws Exception
         {
 
         Object result = ValueExtractor.getDataValue(gcObject,
                 "memoryUsageAfterGc.[Code Cache].committed");
         assertEquals(Long.class, result.getClass());
 
         }
 
     public void testGetMemoryUsageAfterGcEdenSpaceCommittedWithIndexAndColumn()
             throws Exception
         {
 
         Object result = ValueExtractor.getDataValue(gcObject,
                 "memoryUsageAfterGc.[Code Cache].{value}.committed");
         assertEquals(Long.class, result.getClass());
 
         }
 
     public void testGetMemoryUsageAfterGcEdenSpaceCommittedWithExtraPath()
             throws Exception
         {
         try
             {
             ValueExtractor.getDataValue(gcObject,
                     "endTime.[Code Cache].committed.pop");
             }
         catch (JmxException e)
             {
             return;
             }
         fail("expected an exception");
 
         }
 
     public void testSplit() throws Exception
         {
         doSplitTest("[blam.foo.more.last]");
         doSplitTest("[blam]", "other", "[things]", "last");
         doSplitTest("[blam.first]", "other", "[things]", "last");
         doSplitTest("blam", "other", "[things]", "last");
         doSplitTest("blam", "other", "[things]","{foo}", "last");
         doSplitTest("blam", "other", "[things.more.mas]", "last");
         doSplitTest("blam", "other", "[a.b.c]", "last");
         doSplitTest("blam", "other", "[things]","{x.y.z}", "last");
         doSplitTest("blam", "other", "[things.more]","{x.y.z}", "last");
         doSplitTest("blam");
         doSplitTest("[]");
         doSplitTest("[x.]");
         doSplitTest("[xz.]");
         doSplitTest("[.x]");
         doSplitTest("[.z]");
         doSplitTest("[.]");
         doSplitTest("");
 
         
         
         }
 
     private static void doSplitTest(String... values )
         {
         
         String strPath = join(values, ".");
 
         List<String> path = ValueExtractor.split(strPath);
         String[] resultArray = path.toArray(new String[0]);
         System.out.println(Arrays.toString(resultArray));
         assertTrue(Arrays.equals(values, resultArray));
         
         }
     private static String join(String[] values, String seq)
         {
         StringBuilder bldr = new StringBuilder();
         for (int i = 0; i < values.length; i++)
             {
             bldr.append(values[i]);
             if ( i < values.length - 1 )
                 {
                 bldr.append(".");
                 }
             }
         return bldr.toString();
         }
 
 }
