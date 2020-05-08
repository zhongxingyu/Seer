 package com.syfen.zookeeper;
 
 import com.netflix.config.ConfigurationManager;
 import junit.framework.Assert;
 import org.apache.commons.configuration.AbstractConfiguration;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 
 /**
  * User: ToneD
  * Created: 21/09/13 1:09 PM
  */
 public class ExhibitorListProviderTest {
 
     private static final Logger log = LoggerFactory.getLogger(ExhibitorListProvider.class);
     private static final AbstractConfiguration config = ConfigurationManager.getConfigInstance();
 
    private static Collection<String> exhibitorList;
     private static ArrayList<String> expectedList;
 
     @BeforeClass
     public static void setup() {
 
         expectedList = new ArrayList<String>();
         exhibitorList = ExhibitorListProvider.getExhibitorList();
 
         for(Object item : config.getList("test.exhibitor.list")) {
 
             expectedList.add(item.toString());
         }
     }
 
     @Test
     public void testGetExhibitorList() {
 
         // Ensure that an array list is returned
         Assert.assertEquals(ArrayList.class, exhibitorList.getClass());
 
         // Ensure that the list is the right length
         Assert.assertEquals(expectedList.size(), exhibitorList.size());
 
         // Ensure array list contains correct values
         Assert.assertEquals(expectedList, exhibitorList);
     }
 }
