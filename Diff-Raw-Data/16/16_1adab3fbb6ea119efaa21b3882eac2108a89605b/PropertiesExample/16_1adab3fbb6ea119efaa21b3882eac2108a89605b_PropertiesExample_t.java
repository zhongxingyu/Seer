 package com.github.tachesimazzoca.java.example;
 
 import java.io.IOException;
 
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.Set;
 
 public final class PropertiesExample {
     private PropertiesExample() {
     }
 
     public static void main(String[] args) throws IOException {
         Properties prop = new Properties();
         prop.setProperty("foo", "bar");
         prop.setProperty("fuga", "baz");
 
         System.out.println("Properties#propertyNames: java.util.Enumeration");
        Enumeration<?> names = prop.propertyNames();
         while (names.hasMoreElements()) {
             String key = (String) names.nextElement();
             System.out.println(key + "=" + prop.getProperty(key));
         }
         System.out.println();
 
         System.out.println("Properties#stringPropertyNames: java.util.Set<String>");
         Set<String> nameSet = prop.stringPropertyNames();
         for (String name : nameSet) {
             System.out.println(name + "=" + prop.getProperty(name));
         }
         System.out.println();
 
         try {
             final String path = "properties-example/a.properties";
 
             System.out.println("Load(Merge) " + path);
             prop.load(PropertiesExample.class.getClassLoader().getResourceAsStream(path));
             prop.list(System.out);
             System.out.println();
 
             System.out.println("Update the value of \"description\"");
             prop.setProperty("description", "updated");
             prop.list(System.out);
             System.out.println();
 
             System.out.println("Reload(Merge) " + path);
             prop.load(PropertiesExample.class.getClassLoader().getResourceAsStream(path));
             prop.list(System.out);
             System.out.println();
 
         } catch (IOException e) {
             throw e;
         }
     }
 }
