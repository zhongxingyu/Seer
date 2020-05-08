 package testenough.counter;
 
 import org.apache.commons.io.FileUtils;
 import testenough.Configuration;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 public class Track {
 
     static {
         Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
             public void run() {
                 File file = new File(configuration.trackingInfoFilePath());
                 try {
                     String contents = "";
                     if (file.exists()) {
                         contents = FileUtils.readFileToString(file);
                     }
                     contents = contents + "\n" + trackingInfoToPersist();
                     FileUtils.writeStringToFile(file, contents);
                 } catch (IOException e) {
                     throw new RuntimeException("Could not persist tracking information to file: " + file);
                 }
             }
         }));
     }
 
     private static Map<String, Set<String>> methodToTests = new HashMap<String, Set<String>>();
     private static final int INDEX_OF_CALLER = 2;
     private static Configuration configuration;
 
     public static void trackCurrentThread() {
         StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
         String actualFrameName = methodAsString(stackTrace[INDEX_OF_CALLER]);
         for (StackTraceElement stackTraceElement : stackTrace) {
             if (stackTraceElement.getClassName().matches(testClassNamePattern())) {
                 trackTest(actualFrameName, stackTraceElement);
             }
         }
     }
 
     private static String testClassNamePattern() {
         return configuration.testClassNamePattern();
     }
 
     private static void trackTest(String actualFrameName, StackTraceElement stackTraceElement) {
         Set<String> tests = methodToTests.get(actualFrameName);
         if (tests == null) {
             tests = new TreeSet<String>();
             methodToTests.put(actualFrameName, tests);
         }
         tests.add(stackTraceElement.getClassName());
     }
 
     public static Set<String> testsFor(String methodAsString) {
         return methodToTests.get(methodAsString);
     }
 
     public static String methodAsString(String fqn, String method) {
         return fqn + ":" + method;
     }
 
     private static String methodAsString(StackTraceElement frame) {
         return methodAsString(frame.getClassName(), frame.getMethodName());
     }
 
     public static void setConfiguration(Configuration configuration) {
         Track.configuration = configuration;
     }
 
     public static void reset() {
         methodToTests.clear();
     }
 
     public static String trackingInfoToPersist() {
         StringBuilder builder = new StringBuilder();
         for (Map.Entry<String, Set<String>> codeToTests : methodToTests.entrySet()) {
             builder.append(codeToTests.getKey()).append("=>").append("[");
             for (String test : codeToTests.getValue()) {
                 builder.append(test).append(",");
             }
             builder.deleteCharAt(builder.length() - 1);
            builder.append("]");
         }
         return builder.toString();
     }
 }
