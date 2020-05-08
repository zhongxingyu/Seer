 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  *
  */
 
 package de.weltraumschaf.commons;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
// CHECKSTYLE:OFF
 /**
  * Reads the current version from a property file.
  *
  * The property files only defines one property:
  * <pre>
  *  version=1.0.0
  * </pre>
  *
  * This property file may be processed by a Maven filter to provide the verison from
  * the pom.xml:
  * <pre>
  *  version=${pom.version}
  * </pre>
  *
  * If you save the file in <kbd>src/main/resources/foo/bar/version.properties</kbd>
  * you can use the version this way:
  *
  * <code>
  * Version version = new Version("/foo/bar/version.properties");
  * version.load()
  * System.out.println(version.getVersion);
  * </code>
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
// CHECKSTYLE:ON
 public final class Version {
 
     /**
      * Available properties in the file.
      */
     enum PropertyNames {
 
         /**
          * Version property.
          */
         VERSION("version", "n/a");
 
         /**
          * Property name.
          */
         private final String name;
 
         /**
          * Default value.
          */
         private final String defaultValue;
 
         /**
          * Initializes the enum with the property name.
          *
          * @param name The property name.
          * @param defaultValue Fall back value.
          */
         PropertyNames(final String name, final String defaultValue) {
             this.name = name;
             this.defaultValue = defaultValue;
         }
 
         /**
          * Returns the property name.
          *
          * @return String containing the property name.
          */
         @Override
         public String toString() {
             return name;
         }
 
         /**
          * Get the fallback value.
          *
          * @return Return default value string.
          */
         public String getDefaultValue() {
             return defaultValue;
         }
 
     }
 
     /**
      * Location of property file.
      */
     private final String propertyFileName;
 
     /**
      * Indicates whether the properties are already loaded or not.
      */
     private boolean propertiesLoaded;
 
     /**
      * Properties.
      */
     private final Properties properties = new Properties();
 
     /**
      * Private constructor for singleton.
      *
      * @param propertyFileName Location of file, e.g. /de/weltraumschaf/commons/version.properties.
      */
     public Version(final String propertyFileName) {
         super();
         this.propertyFileName = propertyFileName;
     }
 
     /**
      * Opens the properties file and loads it.
      *
      * Only loads th property file once.
      *
      * @throws IOException On IO errors of the property file.
      */
     public void load() throws IOException {
         if (!propertiesLoaded) {
             InputStream in = null;
 
             try {
                 in = getClass().getResourceAsStream(propertyFileName);
                 properties.load(in);
                 in.close();
                 propertiesLoaded = true;
             } finally {
                 if (null != in) {
                     in.close();
                 }
             }
         }
     }
 
     /**
      * Get the version string.
      *
      * @return The version string.
      */
     public String getVersion() {
         return properties.getProperty(PropertyNames.VERSION.toString(),
                                       PropertyNames.VERSION.getDefaultValue());
     }
 
     /**
      * Returns the version string.
      *
      * @return Same as {@link #getVersion()}.
      */
     @Override
     public String toString() {
         return getVersion();
     }
 
 }
