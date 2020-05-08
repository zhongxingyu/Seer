 package org.okiju.pir.cli;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.okiju.pir.generator.InstapaperGenerator;
 import org.okiju.pir.generator.TwitterGenerator;
 import org.okiju.pir.model.TemplateInfo;
 import org.okiju.pir.util.PropertyHelper;
 
 /**
  * CLI of favorites extractor.
  * 
  */
 public class FavoritesExtractor extends BaseExtractor {
 
     public static void main(String[] args) {
         checkArgs("FavoritesExtractor", args);
 
         String path = args[0];
         Properties props = PropertyHelper.loadProperties(path);
 
         Map<String, Set<String>> context = new HashMap<String, Set<String>>();
 
         Set<String> entries = generateEntries(new TwitterGenerator(props), "ficheroTwits");
         context.put("data", entries);
 
         Set<String> entriesQuotes = generateEntries(new InstapaperGenerator(props, "Citas", true),
                 "ficheroInstapaperQuotes");
         context.put("dataQuote", entriesQuotes);
        sendEmail(props, context, "Favoritos del dia ", TemplateInfo.favoritesTwitter);
     }
 }
