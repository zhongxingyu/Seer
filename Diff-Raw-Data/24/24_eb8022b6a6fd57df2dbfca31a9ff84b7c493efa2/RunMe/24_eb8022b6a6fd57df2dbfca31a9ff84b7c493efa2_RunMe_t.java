 package com.seeker.anagramkata;
 
 import java.io.IOException;
import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.List;
import java.util.Scanner;
 
 import org.apache.commons.lang3.StringUtils;
 
 
 public class RunMe {
     public static List<String> openWordList() throws IOException, URISyntaxException {
        InputStream istream = RunMe.class.getResourceAsStream("/wordlist.txt");
        String result = new Scanner(istream).useDelimiter("\\A").next();
        return Arrays.asList(result.split("\\n"));
     }
     
     public static List<String> theAnswer() {
         return Arrays.asList(StringUtils.split("This is the answer -- 42"));
     }
     
     public static void main(final String [] args) throws IOException, URISyntaxException {
         System.out.println(theAnswer());
         List<String> lines = openWordList();
         
        // print out 1st 10 lines
        for (int x = 0; x < 10; x++) {
            System.out.println(lines.get(x));
         }
     }
 }
 
