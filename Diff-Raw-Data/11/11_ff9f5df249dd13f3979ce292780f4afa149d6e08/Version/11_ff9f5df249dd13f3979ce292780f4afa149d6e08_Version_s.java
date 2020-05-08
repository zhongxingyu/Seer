 package edu.uw.zookeeper.jackson.databind;
 
 import java.util.Properties;
 
 public abstract class Version {
 
    public static final String GROUP = "edu.uw.zookeeper.apache";
    public static final String ARTIFACT = "zookeeper-jackson";
     public static final Properties MAVEN_PROPS = new Properties();
     public static final String DEFAULT_VERSION = "0.0.0-SNAPSHOT";
     static {
         try {
             MAVEN_PROPS.load(Version.class.getClassLoader().getResourceAsStream(String.format("META-INF/maven/%s/%s/pom.properties", GROUP, ARTIFACT)));
         } catch (Exception e) {
         }
     }
     public static final String MAVEN_VERSION = MAVEN_PROPS.getProperty("version", DEFAULT_VERSION);
     public static final String PROJECT_NAME = "ZooKeeper Jackson";
     public static final String[] VERSION_FIELDS = MAVEN_VERSION.split("[.-]");
     
     private Version() {}
 }
