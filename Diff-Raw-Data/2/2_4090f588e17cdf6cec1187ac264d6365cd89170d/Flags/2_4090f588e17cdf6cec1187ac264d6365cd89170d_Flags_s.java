 package com.ajitgeorge.flags;
 
 import com.ajitgeorge.flags.logger.DeferredLogger;
 import com.ajitgeorge.flags.logger.ImmediateLogger;
 import com.ajitgeorge.flags.logger.Logger;
 import com.google.common.base.Predicate;
 import org.reflections.Reflections;
 import org.reflections.scanners.FieldAnnotationsScanner;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.nio.charset.Charset;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import static com.google.common.collect.Iterables.filter;
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.io.Files.newReader;
 
 /**
  * {@link Flags} scans the classpath for static fields annotated with {@link Flag}, setting
  * each field to a value determined from an  array of argument strings (typically
  * from the command line).
  *
  * Argument strings either
  * <ul>
  * <li> begin with "<code>--</code>" and provide a name for a value, i.e.  <code>--name=value</code>,
  * <li> are a path to a properties file readable by {@link java.util.Properties}, or
  * <li> are collected as non-Flags arguments and returned by {@link #parse(String...)}.
  * </ul>
  *
  * Argument strings are applied in order and can be overridden.
  *
  * {@link Flags} logs information about fields and the values to which they are set using slf4j.
  * Logging can be done immediately or deferred until {@link #undeferLogging()} is invoked.  Deferral is
  * useful when the logging configuration depends on values set by {@link Flags}.  Clients can customize
  * logging by providing an implementation of {@link Logger}.
  */
 public class Flags {
     private final Map<Class, Parser> parsers = Parsers.all();
     private final Set<Field> flaggedFields;
     private final Map<String, String> properties = new HashMap<String, String>();
     private final Logger logger;
 
     /**
      * Create a Flags scanner that defers its logging until {@link #undeferLogging()} is called.
      *
      * @param packagePrefix common prefix of packages to scan
      * @return a scanner
      */
     public static Flags withDeferredLogging(String packagePrefix) {
         return new Flags(packagePrefix, new DeferredLogger());
     }
 
     /**
      * Create a Flags scanner that logs immediately.
      *
      * @param packagePrefix common prefix of packages to scan
      * @return a scanner
      */
     public static Flags withImmediateLogging(String packagePrefix) {
         return new Flags(packagePrefix, new ImmediateLogger());
     }
 
     /**
      * Create a scanner with custom logging behavior.
      *
      * @param packagePrefix common prefix of packages to scan
     * @param logger
      */
     public Flags(String packagePrefix, Logger logger) {
         Reflections reflections = new Reflections(packagePrefix, new FieldAnnotationsScanner());
         flaggedFields = reflections.getFieldsAnnotatedWith(Flag.class);
         initializePropertiesMapWithDefaults();
         this.logger = logger;
     }
 
     /**
      * Parse properties objects, followed by arguments.
      *
      * @param propertiesInstances properties objects
      * @param argv arguments
      * @return elements of argv that don't define a Flag value or name a property file
      */
     public List<String> parse(Properties[] propertiesInstances, String[] argv) {
         for (Properties properties : propertiesInstances) {
             parse(properties, "Properties[]");
         }
         return parse(argv);
     }
 
     /**
      * Parse an array of arguments, typically from the command line.
      * @param argv arguments
      * @return elements of argv that don't define a Flag value or name a property file
      */
     public List<String> parse(String... argv) {
         List<String> nonFlagArguments = newArrayList();
 
         for (String s : argv) {
             if (s.startsWith("--")) {
                 String[] parts = s.split("=", 2);
                 final String name = parts[0].substring(2);
                 String value = parts[1];
 
                 set(name, value, "command line");
             } else if (new File(s).isFile()) {
                 parse(loadProperties(s), s);
             } else {
                 nonFlagArguments.add(s);
             }
         }
         return nonFlagArguments;
     }
 
     void parse(Properties properties) {
         parse(properties, "object");
     }
     private void parse(Properties properties, String source) {
         for (String name : properties.stringPropertyNames()) {
             set(name, properties.getProperty(name), source);
         }
     }
 
     /**
      * Provide all flags and the values they have as of the last parse.
      *
      * @return a map of flag names to their values
      */
     @SuppressWarnings({"UnusedDeclaration"})
     public Map<String, String> getAllProperties() {
         return Collections.unmodifiableMap(properties);
     }
 
     /**
      * Provide the value for a given flag name.
      *
      * @param key the name of the flag
      * @return the value of the flag
      */
     public String getProperty(String key) {
         return properties.get(key);
     }
 
     /**
      * Do any logging that was previously deferred.
      */
     public void undeferLogging() {
         logger.undeferLogging();
     }
 
     private Object fieldGet(Field field) throws IllegalAccessException {
         try {
             return field.get(null);
         } catch (NullPointerException e) {
             throw new IllegalAccessException();
         }
     }
 
     private void initializePropertiesMapWithDefaults() {
         for (Field field : flaggedFields) {
             try {
                 Object value = fieldGet(field);
 
                 if (value != null) {
                     properties.put(field.getName(), field.get(null).toString());
                 }
             } catch (IllegalAccessException e) {
                 throw new RuntimeException("Field " + field.getName() + " cannot be accessed statically", e);
             }
         }
     }
 
 
     private void set(final String name, String value, String source) {
         properties.put(name, value);
 
         Iterable<Field> fields = filter(flaggedFields, new Predicate<Field>() {
             @Override
             public boolean apply(Field elem) {
                 return elem.getAnnotation(Flag.class).value().equals(name);
             }
         });
         for (Field field : fields) {
             try {
                 Parser parser = parsers.get(field.getType());
 
                 if (parser == null) {
                     throw new IllegalArgumentException("flagged field is of unknown type " + field.getType());
                 }
 
                 field.set(null, parser.parse(value));
                 logger.info("set {} to {} (value from {})", field, value, source);
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             }
         }
     }
 
     private Properties loadProperties(String filename) {
         try {
             Properties properties = new Properties();
             properties.load(newReader(new File(filename), Charset.defaultCharset()));
             return properties;
         } catch (IOException e) {
             throw new RuntimeException("couldn't load properties from file " + filename, e);
         }
     }
 
 }
