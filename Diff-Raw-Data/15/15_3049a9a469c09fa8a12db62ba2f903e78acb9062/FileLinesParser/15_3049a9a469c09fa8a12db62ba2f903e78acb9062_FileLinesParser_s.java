 package ru.finam.bustard.codegen;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.Iterators;
 import com.google.common.io.Resources;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.*;
 
 public class FileLinesParser {
 
     public static List<String> retrieveResource(ClassLoader loader, String filePath) throws IOException {
        Iterator<URL> urls = Iterators.forEnumeration(loader.getResources(filePath));
         List<String> result = new ArrayList<String>();
        while (urls.hasNext()) {
            URL url = urls.next();
            result.addAll(Resources.readLines(url, Charsets.UTF_8));
         }
         return result;
     }
 
 }
