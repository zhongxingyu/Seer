 package com.ning.metrics.goodwill.access;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.SerializationConfig;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import java.util.List;
 import java.util.concurrent.Future;
 
 public class GoodwillAccessorTest
 {
     @Test(enabled = false)
     public void testGetSchema() throws Exception
     {
         GoodwillAccessor accessor = new GoodwillAccessor("localhost", 8080);
 
         ObjectMapper mapper = new ObjectMapper();
         mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
 
         Future<List<GoodwillSchema>> thrifts = accessor.getSchemata();
        System.out.println(String.format("All thrifts:\n%s", mapper.writeValueAsString(thrifts.get())));
 
         Future<GoodwillSchema> thrift = accessor.getSchema("Awesomeness");
        System.out.println(String.format("Awesomeness thrift:\n%s", mapper.writeValueAsString(thrift.get())));
     }
 
     @Test(enabled = true)
     public void testNullSchema() throws Exception
     {
         GoodwillAccessor accessor = new GoodwillAccessor("unexistent", 8080);
 
         try {
             accessor.getSchemata().get();
             Assert.assertTrue(false);
         }
         catch (Exception e) {
             Assert.assertTrue(true);
         }
     }
 }
