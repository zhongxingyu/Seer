 package org.jabox.utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.Map;
 import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import org.apache.wicket.util.io.IOUtils;
 
 public class SettingsModifier {
    private static final Logger LOGGER = LoggerFactory
        .getLogger(SettingsModifier.class);
 
     public static String parseInputStream(final InputStream is,
             Map<String, String> values) throws IOException {
         StringWriter writer = new StringWriter();
         IOUtils.copy(is, writer);
         String theString = writer.toString();
         String replace = theString;
         for (Entry<String, String> pair : values.entrySet()) {
            if (pair.getValue()==null) {
                pair.setValue("");
            }
             replace = replace.replace(pair.getKey(), pair.getValue());
            LOGGER.debug("Settings.xml variable: {}={}", pair.getKey(), pair.getValue());
         }
         return replace;
     }
 
 }
