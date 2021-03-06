 package eu.europeana.database.domain;
 
 /**
  * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
  * @since Mar 17, 2009: 12:18:13 AM
  */
 public enum StaticPageType {
     ABOUT_US("aboutus"),
     ACCESSIBILITY("accessibility"),
     LANGUAGE_POLICY("languagepolicy"),
     NEW_CONTENT("newcontent"),
     PRIVACY("privacy"),
     TERMS_OF_SERVICE("termsofservice"),
     USING_EUROPEANA("usingeuropeana"),
     THINK_VIDEO("thinkvideo"),
     ERROR("error"),
    SITEMAP("sitemap"),
     ADVANCED_SEARCH("advancedsearch"),
     NEWS("news"),
     COMMUNITIES("communities"),
     THOUGHT_LAB("thought-lab"),
     CONTRIBUTORS("partners"),
     TAG_GRID("tag-grid"),
     YEAR_GRID("yeargrid");
 
     private String viewName;
 
     StaticPageType(String viewName) {
         this.viewName = viewName;
     }
 
     public String getViewName() {
         return viewName;
     }
 
     public static StaticPageType get(String string) {
         for (StaticPageType t : values()) {
             if (t.getViewName().equalsIgnoreCase(string)) {
                 return t;
             }
         }
         throw new IllegalArgumentException("Did not recognize StaticPageType: ["+string+"]");
     }
 
 }
