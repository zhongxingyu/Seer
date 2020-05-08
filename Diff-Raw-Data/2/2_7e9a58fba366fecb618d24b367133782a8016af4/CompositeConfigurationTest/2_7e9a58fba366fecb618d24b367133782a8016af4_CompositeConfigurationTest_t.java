 package org.ethelred.mymailtool2;
 
 import java.util.Collections;
 import java.util.Map;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import org.ethelred.util.TestUtil;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.junit.Test;
 
 import static org.ethelred.util.TestUtil.assertEmpty;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 /**
  *
  */
 public class CompositeConfigurationTest
 {
     @Test
     public void testEmptyConfiguration()
     {
         MailToolConfiguration empty = new CompositeConfiguration();
        assertEquals(-1, empty.getOperationLimit());
         assertNull(empty.getUser());
         assertNull(empty.getMinAge());
         assertNull(empty.getPassword());
         assertEmpty(empty.getFileLocations());
         assertEmpty(empty.getFileHandlers());
     }
 
     @Test
     public void testSingleConfiguration()
     {
 
         MailToolConfiguration mock = new MailToolConfiguration()
         {
             @Override
             public String getPassword()
             {
                 return "password";
             }
 
             @Override
             public Map<String, String> getMailProperties()
             {
                 return Collections.singletonMap("test", "mail");
             }
 
             @Override
             public String getUser()
             {
                 return "user";
             }
 
             @Override
             public Iterable<String> getFileLocations()
             {
                 return Lists.newArrayList("file1");
             }
 
             @Override
             public Task getTask() throws Exception
             {
                 return null;
             }
 
             @Override
             public int getOperationLimit()
             {
                 return 1000;
             }
 
             @Override
             public String getMinAge()
             {
                 return "3 months";
             }
 
             @Override
             public Iterable<FileConfigurationHandler> getFileHandlers()
             {
                 return Collections.emptyList();
             }
 
             @Override
             public String getTimeLimit()
             {
                 return null;  //To change body of implemented methods use File | Settings | File Templates.
             }
         };
 
         MailToolConfiguration comp = new CompositeConfiguration(mock);
         assertEquals(mock.getOperationLimit(), comp.getOperationLimit());
         assertEquals(mock.getUser(), comp.getUser());
         assertEquals(mock.getMinAge(), comp.getMinAge());
         assertEquals(mock.getPassword(), comp.getPassword());
         TestUtil.assertEquals(mock.getFileLocations(), comp.getFileLocations());
         TestUtil.assertEquals(mock.getFileHandlers(), comp.getFileHandlers());
     }
 
     @Test
     public void testInsertion()
     {
         Mockery my = new Mockery();
 
         final Iterable<String> testFileLocs = ImmutableList.of("loc1", "loc2");
         final Iterable<String> testFileLocs2 = ImmutableList.of("ins1");
         final MailToolConfiguration mockDefault = my.mock(MailToolConfiguration.class, "MTCdefault");
         final MailToolConfiguration mockFile1 = my.mock(MailToolConfiguration.class, "MTCfile1");
         final MailToolConfiguration mockFile2 = my.mock(MailToolConfiguration.class, "MTCfile2");
         Map<String, MailToolConfiguration> mockFiles = ImmutableMap.of(
                 "loc1", mockFile1,
                 "ins1", mockFile2
         );
         CompositeConfiguration cmp = new CompositeConfiguration(mockDefault);
 
         my.checking(new Expectations(){{
             oneOf(mockDefault).getFileLocations(); will(returnValue(testFileLocs));
             oneOf(mockFile1).getFileLocations(); will(returnValue(testFileLocs2));
             oneOf(mockFile2).getFileLocations(); will(returnValue(Collections.emptyList()));
         }});
         int counter = 0;
         for(String filename: cmp.getFileLocations())
         {
             MailToolConfiguration fileConf = mockFiles.get(filename);
             if(fileConf != null)
             {
                 cmp.insert(fileConf);
             }
             counter++;
             assertTrue("excessive loop count", counter < 5);
         }
         my.assertIsSatisfied();
     }
 }
