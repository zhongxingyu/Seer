 package org.eluder.jetty.server;
 
 public final class ContextAttributes {
     
     public static final String CLASSPATH_PATTERN = "org.eluder.jetty.server.ClasspathPattern";
     public static final String JAR_APP_PATTERN = "org.eluder.jetty.server.JarAppPattern";
 
     private ContextAttributes() {
         // hide constructor
     }
     
    public final static class ClasspathPatterns {
         
         public static final String NON_SYSTEM = "(?!.*(/jre/lib/|/org/eclipse/jetty/)).*";
         public static final String ALL_JARS = ".*\\.jar$";
         public static final String NON_JARS = "(?!.*\\.jar$).*";
         public static final String CLASSES = ".*/test-classes/.*,.*/classes/.*";
         
         private ClasspathPatterns() {
             // hide constructor
         }
     }
 }
