 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.telefonica.moncollector.test;
 
 import com.telefonica.tcloud.collectorinterfaces.KeyValueCache;
 import com.telefonica.tcloud.collectorinterfaces.KeyValueCacheFactory;
 import java.util.Properties;
 import junit.framework.TestCase;
 
 /**
  *
  * @author jomar
  */
public class KeyValueCacheDefaultImplTest extends TestCase {
     
     KeyValueCache keyValue;
    public KeyValueCacheDefaultImplTest(String testName) {
         super(testName);
         
     }
     
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         Properties config=new Properties();
         config.setProperty("keyvaluecache.capacity","100");
         keyValue=KeyValueCacheFactory.getKeyValueCache(
                 "com.telefonica.moncollector.KeyValueCacheDefaultImplFactory",
                  config);
         
     }
     
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
     // TODO add test methods here. The name must begin with 'test'. For example:
     // public void testHello() {}
     
     public void testBasic() {
       
         keyValue.putValue("key1","value1");
         keyValue.putValue("key2","value2");
         keyValue.putValue("key3", "value3");
     }
     
    
    
     public void testFullCapacity() {
         
         for(int i=0;i<100;i++)
               keyValue.putValue("key"+i, "value"+i);
         assert(keyValue.getValue("key0")!=null);
         keyValue.putValue("key100","value100");
         assert(keyValue.getValue("key100").equals("value100"));
         assert(keyValue.getValue("key0")==null);
         
     }
 }
