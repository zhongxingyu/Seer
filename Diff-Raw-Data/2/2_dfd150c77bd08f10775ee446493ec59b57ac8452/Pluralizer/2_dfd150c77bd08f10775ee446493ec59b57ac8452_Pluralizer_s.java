 /*
  * Created on 12-Feb-2006
  */
 package uk.org.ponder.stringutil;
 
 /** A standalone Pluralizer that deals with most names that might arise
  * as entity names.
  * @author Antranig Basman (amb26@ponder.org.uk)
  *
  */
 
 public class Pluralizer {
   // table from
   // http://www.tiscali.co.uk/reference/dictionaries/english/data/d0082600.html
   // and http://en.wikipedia.org/wiki/English_plural
   public static final String[][] rules = { { "", "s" }, { "s", "ses" },
       { "sh", "shes" }, { "ss", "sses" }, { "ch", "ches" }, { "dg", "dges" },
       { "x", "xes" }, { "o", "oes" }, { "f", "ves" }, { "fe", "ves" },
       { "y", "ies" }, { "ies", "ies" }, { "ay", "ays" }, { "ey", "eys" },
       { "iy", "iys" }, { "oy", "oys" }, { "uy", "uys" }, { "kilo", "kilos" },
       { "photo", "photos" }, { "piano", "pianos" }, { "ox", "oxen" },
       { "child", "children" }, { "louse", "lice" }, { "mouse", "mice" },
       { "goose", "geese" }, { "tooth", "teeth" }, { "aircraft", "aircraft" },
       { "sheep", "sheep" }, { "species", "species" }, { "foot", "feet" },
       { "man", "men" }, { "ex", "ices" }, { "ix", "ices" }, { "um", "a" },
      { "us", "i" }, { "eau", "eaux" }, { "is", "es" } };
 
   public static String singularize(String plural) {
     for (int i = rules.length - 1; i >= 0; -- i) {
       if (plural.endsWith(rules[i][1])) {
         return plural.substring(0, plural.length() - rules[i][1].length()) + 
         rules[i][0];
       }
     }
     return plural;
   }
   
   public static String pluralize(String singular) {
     for (int i = rules.length - 1; i >= 0; -- i) {
       if (singular.endsWith(rules[i][0])) {
         return singular.substring(0, singular.length() - rules[i][0].length()) + 
         rules[i][1];
       }
     }
     return singular;
   }
 
 }
