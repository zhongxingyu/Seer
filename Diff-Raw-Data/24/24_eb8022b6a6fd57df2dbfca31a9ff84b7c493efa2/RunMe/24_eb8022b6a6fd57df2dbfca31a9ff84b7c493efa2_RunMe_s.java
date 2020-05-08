 package com.seeker.anagramkata;
 
import java.io.File;
 import java.io.IOException;
import java.net.URI;
 import java.net.URISyntaxException;
import java.net.URL;
 import java.util.Arrays;
 import java.util.List;
 
import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.StringUtils;
 
 
 public class RunMe {
     public static List<String> openWordList() throws IOException, URISyntaxException {
        URL url = RunMe.class.getResource("/wordlist.txt");
        URI uri = url.toURI();
        File file = new File(uri);
        return FileUtils.readLines(file);
     }
     
     public static List<String> theAnswer() {
         return Arrays.asList(StringUtils.split("This is the answer -- 42"));
     }
     
     public static void main(final String [] args) throws IOException, URISyntaxException {
         System.out.println(theAnswer());
         List<String> lines = openWordList();
         
        for (String line : lines) {
            System.out.println(line);
         }
     }
 }
 
