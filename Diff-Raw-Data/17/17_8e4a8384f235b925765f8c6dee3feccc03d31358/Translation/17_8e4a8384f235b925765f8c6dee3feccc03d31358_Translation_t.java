 package de.cubeisland.libMinecraft;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author CodeInfection
  */
 public class Translation
 {
     private String language;
     private final Map<String, String> translations;
     private static final String RESOURCE_PATH = "/language/";
     private static final String RESOURCE_EXT = ".ini";
 
     private Translation(Class clazz, final String language) throws IOException
     {
         if (clazz == null)
         {
            throw new IllegalArgumentException("The class must not be null!");
         }
         if (language == null)
         {
            throw new IllegalArgumentException("The language must not be null!");
         }
         
         InputStream is = clazz.getResourceAsStream(RESOURCE_PATH + language + RESOURCE_EXT);
         if (is == null)
         {
             throw new IllegalStateException("Requested language was not found!");
         }
         this.translations = new HashMap<String, String>();
         StringBuilder sb = new StringBuilder();
         byte[] buffer = new byte[512];
         int bytesRead;
 
         while ((bytesRead = is.read(buffer)) > 0)
         {
             sb.append(new String(buffer, 0, bytesRead, "UTF-8"));
         }
         is.close();
 
         translations.clear();
         int equalsOffset;
         char firstChar;
         for (String line : explode("\n", sb.toString().trim()))
         {
            if (line.length() == 0)
            {
                continue;
            }
             firstChar = line.charAt(0);
             if (firstChar == ';' || firstChar == '[')
             {
                 continue;
             }
             equalsOffset = line.indexOf("=");
             if (equalsOffset < 1)
             {
                 continue;
             }
 
             translations.put(line.substring(0, equalsOffset).trim(), ChatColor.translateAlternateColorCodes('&', line.substring(equalsOffset + 1).trim()));
         }
 
         this.language = language;
     }
 
     public String translate(String key, Object... params)
     {
         String translation = this.translations.get(key);
         if (translation == null)
         {
             return "null";
         }
         return String.format(translation, params);
     }
 
     public static Translation get(final Class clazz, final String language)
     {
         try
         {
             return new Translation(clazz, language);
         }
         catch (IOException e)
         {
             e.printStackTrace(System.err);
         }
         return null;
     }
 
     public String getLanguage()
     {
         return this.language;
     }
 
     private static List<String> explode(String delim, String string)
     {
         int pos, offset = 0, delimLen = delim.length();
         List<String> tokens = new ArrayList<String>();
 
         while ((pos = string.indexOf(delim, offset)) > -1)
         {
             tokens.add(string.substring(offset, pos));
             offset = pos + delimLen;
         }
         tokens.add(string.substring(offset));
 
         return tokens;
     }
 }
