 package pl.michalorman.kvmapper.core.mapper;
 
 import pl.michalorman.kvmapper.core.config.Config;
 import pl.michalorman.kvmapper.core.exception.KVMapperException;
 import pl.michalorman.kvmapper.core.introspect.DefaultTypeIntrospector;
 import pl.michalorman.kvmapper.core.introspect.TypeIntrospector;
 import pl.michalorman.kvmapper.core.serializer.DefaultKeyValuePairsSerializer;
 import pl.michalorman.kvmapper.core.serializer.KeyValuePairsSerializer;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.Iterator;
 
 /**
  * Core component that performs the serialization and deserialization Java objects
  * to/from key-value pairs.
  *
  * @author Michal Orman
  * @version 1.0
  */
 public class KVMapper {
 
     /**
      * Serialization/deserialization config. Create getter and setter methods to alter
      * the configuration, or create instance providing custom configuration.
      */
     private Config config;
 
     /** Component performing type introspection. */
     private TypeIntrospector typeIntrospector = new DefaultTypeIntrospector();
 
     /** Component performing object serialization. */
     private KeyValuePairsSerializer keyValuePairsSerializer = new DefaultKeyValuePairsSerializer();
 
     /** Creates the <tt>KVMapper</tt> instance with default {@link Config} */
     public KVMapper() {
         config = new Config();
     }
 
     /** Creates the <tt>KVMapper</tt> instance with provided {@link Config} */
     public KVMapper(Config config) {
         this.config = config;
     }
 
     /**
      * Reads the key-value pairs from the provided <tt>input</tt> and creates new instance
      * of the specified <tt>type</tt> applying values to matching properties.
      *
      * @param input Input to be read.
      * @param type  Class to be instantiated.
      * @param <T>   Type of object to be instantiated.
      *
      * @return New instance of specified <tt>type</tt>.
      */
     public <T> T readObject(CharSequence input, Class<T> type) {
         return readObject(input.toString(), type);
     }
 
     /**
      * Reads the key-value pairs from the provided <tt>input</tt> and creates new instance
      * of the specified <tt>type</tt> applying values to matching properties.
      *
      * @param input Input to be read.
      * @param type  Class to be instantiated.
      * @param <T>   Type of object to be instantiated.
      *
      * @return New instance of specified <tt>type</tt>.
      */
     public <T> T readObject(String input, Class<T> type) {
         // TODO: implement body
         throw new UnsupportedOperationException("Not implemented yet!");
     }
 
     /**
      * Reads the key-value pairs from the provided <tt>input</tt> and creates new instance
      * of the specified <tt>type</tt> applying values to matching properties.
      *
      * @param input Input to be read.
      * @param type  Class to be instantiated.
      * @param <T>   Type of object to be instantiated.
      *
      * @return New instance of specified <tt>type</tt>.
      */
     public <T> T readObject(InputStream input, Class<T> type) {
         // TODO: implement body
         throw new UnsupportedOperationException("Not implemented yet!");
     }
 
     /**
      * Serializes provided <tt>object</tt> and appends the serialization result to provided <tt>output</tt>.
      *
      * @param output Object to which append the serialization result.
      * @param object Object to serialize.
      *
      * @throws java.io.IOException Thrown if appending to output fails.
      */
     public void writeObject(Appendable output, Object object) throws IOException {
         writeObjects(output, Arrays.asList(object));
     }
 
     /**
      * Serializes provided <tt>object</tt> and writes the serialization result to provided <tt>output</tt>.
      *
      * @param output Object to which write the output.
      * @param object Object to serialize.
      */
     public void writeObject(OutputStream output, Object object) {
         writeObjects(output, Arrays.asList(object));
     }
 
     /**
     * Serializes provided collection of <tt>objects<tt> and appends the serialization result to provided
      * <tt>output</tt>.
      *
      * @param output  Object to which append the serialization result.
      * @param objects Collection of objects to serialize.
      *
      * @throws java.io.IOException Thrown if appending to output fails.
      */
     public void writeObjects(Appendable output, Object[] objects) throws IOException {
         writeObjects(output, Arrays.asList(objects));
     }
 
     /**
      * Serializes provided collection of <tt>objects<tt> and appends the serialization result to provided
      * <tt>output</tt>.
      *
      * @param output  Object to which append the serialization result.
      * @param objects Collection of objects to serialize.
      *
      * @throws java.io.IOException Thrown if appending to output fails.
      */
     public void writeObjects(Appendable output, Iterable<?> objects) throws IOException {
         writeObjects(output, objects.iterator());
     }
 
     /**
      * Iterates and serialize each element using provided <tt>iterator</tt> and appends serialization
      * result to provided <tt>output</tt>.
      *
      * @param output   Object to which append the serialization result.
      * @param iterator Iterator to use to iterate over each object to serialize.
      *
      * @throws java.io.IOException Thrown if appending to output fails.
      */
     public void writeObjects(Appendable output, Iterator<?> iterator) throws IOException {
         keyValuePairsSerializer.serialize(output, iterator, config, typeIntrospector);
     }
 
     /**
      * Serializes provided collection of <tt>objects<tt> and writes serialization result to provided
      * <tt>output</tt>.
      *
      * @param output  Object to which write the serialization result.
      * @param objects Collection of objects to serialize.
      */
     public void writeObjects(OutputStream output, Object[] objects) {
         writeObjects(output, Arrays.asList(objects));
     }
 
     /**
      * Serializes provided collection of <tt>objects<tt> and writes the serialization result to provided
      * <tt>output</tt>.
      *
      * @param output  Object to which write the serialization result.
      * @param objects Collection of objects to serialize.
      */
     public void writeObjects(OutputStream output, Iterable<?> objects) {
         writeObjects(output, objects.iterator());
     }
 
     /**
      * Iterates and serializes each element using provided <tt>iterator</tt> and writes the serialization
      * result in the provided <tt>output</tt>.
      *
      * @param output   Object to which write the serialization result.
      * @param iterator Object to use to iterate over each object to serialize.
      */
     public void writeObjects(OutputStream output, Iterator<?> iterator) {
         // TODO: implement body
         throw new UnsupportedOperationException("Not implemented yet!");
     }
 
     /**
      * Serializes provided <tt>object</tt> and returns result as string.
      *
      * @param object Object to serialize.
      *
      * @return Serialization result as string.
      */
     public String dump(Object object) {
         return dumpAll(Arrays.asList(object));
     }
 
     /**
      * Serializes provided collection of <tt>objects</tt> and returns result
      * as string.
      *
      * @param objects Collection of objects to serialize.
      *
      * @return Serialization result as string.
      */
     public String dumpAll(Object[] objects) {
         return dumpAll(Arrays.asList(objects));
     }
 
     /**
      * Serializes provided collection of <tt>objects</tt> and returns result
      * as string.
      *
      * @param objects Collection of objects to serialize.
      *
      * @return Serialization result as string.
      */
     public String dumpAll(Iterable<?> objects) {
         return dumpAll(objects.iterator());
     }
 
     /**
      * Iterates over all objects using provided <tt>iterator</tt> and serializes
      * each of them. Serialization result is returned as string.
      *
      * @param iterator Iterator to use to iterate over each object to serialize.
      *
      * @return Serialization result as string.
      */
     public String dumpAll(Iterator<?> iterator) {
         try {
             StringBuilder builder = new StringBuilder();
             writeObjects(builder, iterator);
             return builder.toString();
         } catch (IOException ex) {
             throw new KVMapperException("Failed to dump objects.", ex);
         }
     }
 
     public void setConfig(Config config) {
         this.config = config;
     }
 
     public Config getConfig() {
         return config;
     }
 }
