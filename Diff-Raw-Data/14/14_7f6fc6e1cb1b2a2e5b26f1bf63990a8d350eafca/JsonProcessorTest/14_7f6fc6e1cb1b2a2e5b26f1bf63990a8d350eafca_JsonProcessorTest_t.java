 package com.psddev.dari.util;
 
 import java.util.*;
 import org.junit.*;
 import static org.junit.Assert.*;
 
 public class JsonProcessorTest {
 
     @Test
     public void test_generate_indent1() {
 
         JsonProcessor processor = new JsonProcessor();
 
         Class1 object = new Class1();
         object.string1 = "string1";
 
         assertEquals(
                "{\"string1\":\"string1\",\"string2\":null,\"string3\":null,\"date1\":null}",
                 processor.generate(object));
     }
 
     @Test
     public void test_generate_indent2() {
 
         JsonProcessor processor = new JsonProcessor();
         processor.setIndentOutput(true);
 
         Class1 object = new Class1();
         object.string1 = "string1";
 
         assertEquals(
                "{\n  \"string1\" : \"string1\",\n  \"string2\" : null,\n  \"string3\" : null,\n  \"date1\" : null\n}",
                 processor.generate(object));
     }
 
     @Test
     public void test_generate_filter_include1() {
 
         JsonProcessor processor = new JsonProcessor();
         processor.setTransformer(new TransformerBuilder()
                 .setMapType(TreeMap.class)
                 .includeFields(Class1.class, "string1")
                 .toTransformer());
 
         Class1 object = new Class1();
         object.string1 = "string1";
         object.string2 = "string2";
         object.string3 = "string3";
 
         assertEquals(
                 "{\"string1\":\"string1\"}",
                 processor.generate(object));
     }
 
     @Test
     public void test_generate_filter_include2() {
 
         JsonProcessor processor = new JsonProcessor();
         processor.setTransformer(new TransformerBuilder()
                 .setMapType(TreeMap.class)
                 .includeGetters(Class1.class, "method1", "method2")
                 .toTransformer());
 
         Class1 object = new Class1();
 
         assertEquals(
                 "{\"method1\":\"method1\",\"method2\":\"method2\"}",
                 processor.generate(object));
 
     }
 
     @Test
     public void test_generate_filter_exclude1() {
 
         JsonProcessor processor = new JsonProcessor();
         processor.setTransformer(new TransformerBuilder()
                 .setMapType(TreeMap.class)
                 .includeAllFields(Class1.class)
                 .exclude(Class1.class, "string2")
                 .toTransformer());
 
         Class1 object = new Class1();
         object.string1 = "string1";
         object.string2 = "string2";
         object.string3 = "string3";
 
         assertEquals(
                "{\"date1\":null,\"string1\":\"string1\",\"string3\":\"string3\"}",
                 processor.generate(object));
     }
 
     @Test
     public void test_generate_filter_rename1() {
 
         JsonProcessor processor = new JsonProcessor();
         processor.setTransformer(new TransformerBuilder()
                 .setMapType(TreeMap.class)
                 .includeAllFields(Class1.class)
                 .rename(Class1.class, "string1", "foo")
                 .toTransformer());
 
         Class1 object = new Class1();
         object.string1 = "string1";
 
         assertEquals(
                "{\"date1\":null,\"foo\":\"string1\",\"string2\":null,\"string3\":null}",
                 processor.generate(object));
     }
 
     @Test
     public void test_generate_filter_filter() {
 
         JsonProcessor processor = new JsonProcessor();
         processor.setTransformer(new TransformerBuilder()
                 .setMapType(TreeMap.class)
                 .includeAllFields(Class1.class)
                 .transform(Date.class, new TransformationFunction<Date>() {
 
                     @Override
                     public Object transform(Date date) {
                         return date.getTime();
                     }
                 })
                 .toTransformer());
 
         Class1 object = new Class1();
         object.date1 = new Date();
 
         assertEquals(
                "{\"date1\":" + object.date1.getTime() + ",\"string1\":null,\"string2\":null,\"string3\":null}",
                 processor.generate(object));
     }
 
     private static class Class1 {
 
         public String string1;
         public String string2;
         public String string3;
         public Date date1;
 
         public String getMethod1() {
             return "method1";
         }
 
         public String getMethod2() {
             return "method2";
         }
     }
 }
