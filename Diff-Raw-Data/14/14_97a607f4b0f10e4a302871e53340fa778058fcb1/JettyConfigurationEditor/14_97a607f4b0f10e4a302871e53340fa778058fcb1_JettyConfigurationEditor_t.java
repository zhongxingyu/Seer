 package com.jclarity.had_one_dismissal.jmx;
 
 import static java.lang.Integer.parseInt;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.collect.Lists;
 
 @Component
 public class JettyConfigurationEditor {
     
     private static final Logger logger = LoggerFactory.getLogger(JettyConfigurationEditor.class);
     
     private static final String maxThreads = "maxThreads";
     private static final String minThreads = "minThreads";
     
     // This deliberately references src/main/resources, rather than
     // using getResource() since that's what the maven-jetty-plugin references
     private static final String relativeJettyXml = System.getProperty("jetty.xml", "src/main/resources/jetty.xml");
     
     private final Pattern minThreadsPattern;
     private final Pattern maxThreadsPattern;
     
     public JettyConfigurationEditor() {
         minThreadsPattern = Pattern.compile(jettySetFinder(minThreads));
         maxThreadsPattern = Pattern.compile(jettySetFinder(maxThreads));
     }
     
     private String jettySetFinder(String name) {
         return jettySetPattern(name, "(\\d+)");
     }
 
     private String jettySetPattern(String name, String body) {
         return "<Set name=\"" + name + "\" type=\"int\">" + body + "</Set>";
     }
 
     public int readMinThreadPoolSize() {
         return readFromConfig(minThreadsPattern);
     }
     
     public int readMaxThreadPoolSize() {
         return readFromConfig(maxThreadsPattern);
     }
     
     public void writeMinThreadPoolSize(int size) {
         replaceConfig(minThreadsPattern, jettySetPattern(minThreads, String.valueOf(size)));
     }
 
     public void writeMaxThreadPoolSize(int size) {
         replaceConfig(maxThreadsPattern, jettySetPattern(maxThreads, String.valueOf(size)));
     }
     
     private <T> Optional<T> withLines(Function<String, Optional<T>> handler) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(relativeJettyXml));
             String line;
             while ((line=reader.readLine()) != null) {
                 Optional<T> result = handler.apply(line);
                 if (result.isPresent())
                     return result;
             }
             return Optional.absent();
         } catch (IOException e) {
             logger.error(e.getMessage(), e);
             throw new RuntimeException(e);
         } 
     }
 
     private void replaceConfig(final Pattern pattern, final String replacement) {
         final List<String> result = Lists.newArrayList();
         withLines(new Function<String, Optional<Void>>() {
             @Override
             public Optional<Void> apply(String line) {
                 Matcher matcher = pattern.matcher(line);
                 if (matcher.find()) {
                     line = matcher.replaceFirst(replacement);
                 }
                 result.add(line);
                 
                 return Optional.absent();
             }
         });
         
        try {
            PrintWriter writer = new PrintWriter(relativeJettyXml);
             for (String line : result) {
                 writer.write(line);
                 writer.write('\n');
             }
             writer.flush();
         } catch (FileNotFoundException e) {
             throw new RuntimeException(e);
         }
     }
 
     private int readFromConfig(final Pattern pattern) {
         Optional<Integer> result = withLines(new Function<String, Optional<Integer>>() {
             @Override
             public Optional<Integer> apply(String line) {
                 Matcher matcher = pattern.matcher(line);
                 if (!matcher.find()) {
                     return Optional.absent();
                 }
                 
                 return Optional.of(parseInt(matcher.group(1)));
             }
         });
         
         if (!result.isPresent()) {
             throw new RuntimeException("Unable to find : " + pattern);
         }
         
         return result.get();
     }
 
 }
