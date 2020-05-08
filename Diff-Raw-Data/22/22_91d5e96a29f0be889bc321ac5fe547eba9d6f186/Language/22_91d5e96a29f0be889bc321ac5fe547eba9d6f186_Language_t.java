 package models.data;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import models.EMessages;
 import play.i18n.Lang;
 
 /**
  * Wrapper class around the Play Lang class. This wrapper adds exceptions to
  * limit the languages to the supported languages.
  * @author Felix Van der Jeugt
  */
 public class Language implements Comparable<Language> {
 
     private Lang lang;
 
     // The list of already created languages.
     private static Set<Language> languages = new TreeSet<Language>();
 
     /**
      * Creates a new Language with the given code.
      * @param code The code Play uses for this language.
      */
     private Language(String code) throws UnknownLanguageCodeException,
            UnavailableLanguageException {
         if(code == null) throw new UnknownLanguageCodeException("<null>");
 
         try {
             Lang.forCode(code);
         } catch(RuntimeException e) {
             if(e.getMessage().startsWith("Unrecognized language:")) {
                 throw new UnknownLanguageCodeException(code);
             } else {
                 throw e;
             }
         }
 
         if(!Lang.availables().contains(Lang.forCode(code))) {
             throw new UnavailableLanguageException(code);
         }
         this.lang = Lang.forCode(code);
     }
 
     /**
      * Creates a new Language from the given Lang.
      * @param lang The Play language.
      */
     private Language(Lang lang) throws UnavailableLanguageException {
         if(!Lang.availables().contains(lang)) {
             throw new UnavailableLanguageException(lang.code());
         }
         this.lang = lang;
     }
 
     /**
      * Creates a new language, or returns an existing language with the given
      * code.
      * @param code The code Play uses for this language.
      */
     public static Language getLanguage(String code) throws
             UnavailableLanguageException, UnknownLanguageCodeException {
         for(Language l : languages) {
             if(l.getCode().equals(code)) return l;
         }
         Language l = new Language(code);
         languages.add(l);
         return l;
     }
 
     /**
      * Creates a new language, or returns an existing language as wrapper for
      * the supplied Lang.
      * @param code The Play language.
      */
     public static Language getLanguage(Lang lang) throws
             UnavailableLanguageException {
         for(Language l : languages) {
             if(l.getLang().equals(lang)) return l;
         }
         Language l = new Language(lang);
         languages.add(l);
         return l;
     }
 
     /**
      * Returns the readable name of this Language.
      * @param language The language for the name.
      * @return The name of this language in the provided language.
      */
     public String getName(Language language) {
        return EMessages.get(language.getCode(), "languages." + language.getCode());
     }
 
     /**
      * Returns the readable name of this Language in the user's preferred
      * language.
      * @return The name of this language in the preferred language.
      */
     public String getName() {
        return EMessages.get("languages." + lang.code());
     }
 
     /**
      * Returns the ISO code for this language.
      * @return The ISO code for this language.
      */
     public String getCode() {
         return lang.code();
     }
 
     /**
      * Returns the Play Lang objects this language wraps.
      */
     public Lang getLang() {
         return lang;
     }
 
     /**
      * Lists the available Languages.
      * @return A List of the Available languages.
      */
     public static List<Language> listLanguages() {
         List<Language> langs = new ArrayList<Language>();
         try {
             for(Lang l : Lang.availables()) {
                 langs.add(Language.getLanguage(l));
             }
         } catch(UnavailableLanguageException e) {
             // When we loop over availables...
             throw new Error("Impossibru!");
         }
         return langs;
     }
 
     @Override
     public boolean equals(Object o) {
         if(o == null || !(o instanceof Language)) return false;
         Language that = (Language) o;
         return this.getLang() == that.getLang();
     }
 
     @Override
     public int compareTo(Language that) {
         if(this.equals(that)) return 0;
         return this.getCode().compareTo(that.getCode());
     }
 
 }
