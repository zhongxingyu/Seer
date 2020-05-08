 package com.abudko.scheduled.csv;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 
 public class CsvPhotoDataLoggerTest {
     
     private PhotoDataLogger dataLogger;
     
     @Before
     public void setup() {
        dataLogger = new CsvPhotoDataLogger("classpath:/csv/photos.csv");
     }
 
     @Test
     public void testDumpRead() {
         final String key1 = "key1";
         final String key2 = "key2";
         final String value1 = "value1";
         final String value2 = "value2";
         Map<String, String> photoIdGroupIdMap = new HashMap<String, String>();
         photoIdGroupIdMap.put(key1, value1);
         photoIdGroupIdMap.put(key2, value2);
         
         dataLogger.dump(photoIdGroupIdMap);
         
         Map<String, String> map = dataLogger.read();
         assertEquals(value1, map.get(key1));
         assertEquals(value2, map.get(key2));
     }
 }
