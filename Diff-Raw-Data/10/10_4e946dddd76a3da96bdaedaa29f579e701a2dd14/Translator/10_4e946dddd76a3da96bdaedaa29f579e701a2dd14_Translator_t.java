 package com.novel.stdobj.translator;
 
 import java.io.FileInputStream;
 import java.util.Map;
 import java.util.Properties;
 
 import com.novel.odisp.common.Resource;
 
 /** - 
  *
  * @author <a href="mailto:dron@novel-il.ru"> . </a>
  * @author (C) 2004  "-"
 * @version $Id: Translator.java,v 1.8 2004/04/24 19:49:07 valeks Exp $
  */
 public class Translator extends Properties implements Resource {
   /**  ,       */
   public final static String LanguageFile = "LanguageFile";
   /**  ,     
    *   Odisp
    */
   private final String DefaultLanguageFile = "resources/language/Default.lng";
   
   /**  
    */
   public Translator() {
     /* empty constructor */
   }
 
   /**     ̣ .  
    *    .    -
    *     ̣  
    * .
    *
    * @param key   .
    * @param defaultValue  -,     
    * .
    * @return ,   ,   
    * .
    */
   public synchronized String translate(String key, String defaultValue) {
     String tmp = getProperty(key, defaultValue);
     return tmp;
   }
   
   /**  
    *
    * @param type  
    */
   public int cleanUp(int type) {
     return 0;
   }
   
   /**   
    */
   public int getMaxReferenceCount() {
     return 0;
   }
   
   /**   .
    * 
    * @param cfg 
    */
   public void setConfiguration(final Map cfg) {
     try {
       String fileName = DefaultLanguageFile;
      if (cfg != null && cfg.containsKey(LanguageFile)) {
        fileName = (String) cfg.get(LanguageFile);
       }
       FileInputStream inputStream = new FileInputStream(fileName);
       load(inputStream);
     } catch (Exception e) {
//      logger.warning("Exception: " + e);
     }
   }
 }
