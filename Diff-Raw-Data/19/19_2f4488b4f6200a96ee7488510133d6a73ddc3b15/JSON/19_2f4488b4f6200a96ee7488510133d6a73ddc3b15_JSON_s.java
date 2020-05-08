 package org.nohope.typetools;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.datatype.joda.JodaModule;
 import org.nohope.logging.Logger;
 import org.nohope.logging.LoggerFactory;
 
 import java.io.IOException;
 
 import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
 import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
 import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
 
 /**
  * Date: 23.01.12
  * Time: 13:43
  */
 public final class JSON {
     private static final Logger LOG = LoggerFactory.getLogger(JSON.class);
 
     private JSON() {
     }
 
     public static String jsonifyPretty(final Object obj,
                                        final String onErrorMessage) {
         try {
             final ObjectMapper mapper = new ObjectMapper();
             mapper.registerModule(new JodaModule());
             mapper.configure(INDENT_OUTPUT, true);
             mapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
             mapper.setSerializationInclusion(NON_EMPTY);
 
             return mapper.writeValueAsString(obj);
         } catch (IOException e) {
            System.err.println(e);
            e.printStackTrace();
            LOG.debug(e, "Unable to jsonify object of class {}",
                     obj == null ? null : obj.getClass());
             return onErrorMessage;
         }
     }
 
     public static String jsonifyPretty(final Object obj) {
        return jsonifyPretty(obj, "<? " + obj.getClass().getName() + ">");
     }
 
     public static String jsonify(final Object obj,
                                  final String onErrorMessage) {
         try {
             final ObjectMapper mapper = new ObjectMapper();
             mapper.registerModule(new JodaModule());
             mapper.setSerializationInclusion(NON_EMPTY);
             return mapper.writeValueAsString(obj);
         } catch (IOException e) {
            LOG.debug(e, "Unable to jsonify object of class {}",
                     obj == null ? null : obj.getClass());
             return onErrorMessage;
         }
     }
 }
