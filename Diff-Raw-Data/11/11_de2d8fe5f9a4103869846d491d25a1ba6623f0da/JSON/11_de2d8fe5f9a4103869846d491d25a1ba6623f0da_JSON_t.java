 package org.nohope.typetools;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
 import com.fasterxml.jackson.datatype.joda.JodaModule;
 import org.nohope.logging.Logger;
 import org.nohope.logging.LoggerFactory;
 import org.nohope.typetools.json.ColorModule;
 
 import java.io.IOException;
 
 import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
 import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
 import static com.fasterxml.jackson.databind.SerializationFeature.*;
 
 /**
  * Date: 23.01.12
  * Time: 13:43
  */
 public final class JSON {
     private static final Logger LOG = LoggerFactory.getLogger(JSON.class);
 
     private JSON() {
     }
 
     private static final ObjectMapper PRETTY_MAPPER = new ObjectMapper();
     private static final ObjectMapper USUAL_MAPPER = new ObjectMapper();
 
     static {
         PRETTY_MAPPER.registerModule(new JodaModule());
         PRETTY_MAPPER.registerModule(new ColorModule());
         PRETTY_MAPPER.configure(INDENT_OUTPUT, true);
         PRETTY_MAPPER.configure(WRITE_DATES_AS_TIMESTAMPS, false);
         PRETTY_MAPPER.configure(FAIL_ON_EMPTY_BEANS, false);
         PRETTY_MAPPER.setSerializationInclusion(NON_EMPTY);
         PRETTY_MAPPER.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");
         PRETTY_MAPPER.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(ANY));
 
         USUAL_MAPPER.registerModule(new JodaModule());
         USUAL_MAPPER.registerModule(new ColorModule());
         USUAL_MAPPER.setSerializationInclusion(NON_EMPTY);
         USUAL_MAPPER.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");
         USUAL_MAPPER.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(ANY));
     }
 
     public static String pretty(final Object obj) {
         return pretty(obj, defaultErrorMessage(obj));
     }
 
     public static String jsonify(final Object obj) {
         return jsonify(obj, defaultErrorMessage(obj));
     }
 
     /**
      * Serializes given object with Jackson, then deserializes into class required.
      * Not so fast, so shouldn't be used for deep copying.
      *
      * @throws IOException
      */
     public static <T> T copyAs(final Object source, final Class<T> clazz) throws IOException {
         final byte [] marshalled = USUAL_MAPPER.writeValueAsBytes(source);
         return USUAL_MAPPER.readValue(marshalled, clazz);
     }
 
     private static String jsonify(final Object obj,
                                  final String onErrorMessage) {
         return jsonifyWith(USUAL_MAPPER, obj, onErrorMessage);
     }
 
     private static String pretty(final Object obj,
                          final String onErrorMessage) {
         return jsonifyWith(PRETTY_MAPPER, obj, onErrorMessage);
     }
 
     private static String jsonifyWith(final ObjectMapper mapper, final Object obj, final String onErrorMessage) {
         try {
             return mapper.writeValueAsString(obj);
         } catch (Throwable e) {
             LOG.error(e, "Unable to jsonify object of class {}",
                     obj == null ? null : obj.getClass());
             return onErrorMessage;
         }
     }
 
     private static String defaultErrorMessage(final Object obj) {
         if (null != obj) {
             return "<? " + obj.getClass().getCanonicalName() + "/>";
         }
         return "<?null />";
     }
 }
