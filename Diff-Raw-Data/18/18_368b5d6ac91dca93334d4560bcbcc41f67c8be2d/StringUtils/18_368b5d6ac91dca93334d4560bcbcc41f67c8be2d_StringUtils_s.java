 package com.grooveshark.util;
 
import java.util.Properties;
import java.util.Enumeration;
import java.io.InputStream;
 import java.io.IOException;
 
 
 public class StringUtils
 {
     public static void logToStdOut(String threadName, String msg) {
         System.out.println(DateUtils.getNow() + ": [" + threadName + "] " + msg);
     }
 
     public static LinkedList<String> splitTrim(String str, String sep) {
         LinkedList<String> splitted = new LinkedList<String>();
        String[] str = str.split(sep);
        for(String st : str) {
             splitted.add(st.trim());
         }
         return splitted;
     }
 
    public static Properties getProperties(HashSet<String> properties, String filename) throws IOException {
        Properties props = new Properties();
        InputStream is = FileUtils.getInputStream(filename);
        if (is != null) {
            props.load(is);
        }
        return props;
    }
 }
