 package ru.finam.bustard.codegen;
 
 
 import ru.finam.bustard.BustardImpl;
 
 import java.io.*;
 import java.net.URISyntaxException;
 import java.net.URL;
import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 public class SubscribersFinder {
 
     public static final Pattern LINE_PATTERN =
             Pattern.compile("^([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]* " +
                     "[a-zA-Z_$][a-zA-Z\\d_$]* ([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*$");
 
     public static Iterable<SubscriberInfo> retrieveSubscribersInfo() throws IOException {
         ClassLoader bustardClassLoader = BustardImpl.class.getClassLoader();
        final Enumeration<URL> urls = bustardClassLoader.getResources("ru/finam/bustard/subscribers.bustard");
         return new Iterable<SubscriberInfo>() {
             @Override
             public Iterator<SubscriberInfo> iterator() {
                 return new LineIterator(urls);
             }
         };
     }
 
     private static String readEntireFile(URL fileUrl) throws URISyntaxException, IOException {
         File file = new File(fileUrl.toURI());
         FileReader reader = new FileReader(file);
         StringBuilder contents = new StringBuilder();
         char[] buffer = new char[4096];
         int read = 0;
         do {
             contents.append(buffer, 0, read);
             read = reader.read(buffer);
         } while (read >= 0);
         return contents.toString();
     }
 
     public static class LineIterator implements Iterator<SubscriberInfo> {
 
        private Enumeration<URL> urls;
 
         private StringTokenizer tokenizer = null;
         private String nextLine = null;
 
        public LineIterator(Enumeration<URL> urls) {
            this.urls = urls;
         }
 
         @Override
         public boolean hasNext() {
             try {
                 while (nextLine == null || !LINE_PATTERN.matcher(nextLine).matches()) {
                     while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                        if (!urls.hasMoreElements()) {
                             return false;
                         }
 
                        tokenizer = new StringTokenizer(readEntireFile(urls.nextElement()), "\n\r");
                     }
                     nextLine = tokenizer.nextToken();
                 }
 
                 return true;
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
 
         @Override
         public SubscriberInfo next() {
             if (!hasNext()) {
                 return null;
             }
             String[] tokens = nextLine.split(" ");
             nextLine = null;
             return new SubscriberInfo(tokens[0], tokens[1], tokens[2]);
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException();
         }
     }
 }
