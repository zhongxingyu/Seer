 package name.vysoky.epub;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Entity mapper.
  * @author Jiri Vysoky
  */
 public class Entity {
 
     private String name;
     private char character;
 
     private static Entity[] entities;
     private static Map<String, Entity> nameHash;
     private static Map<Character, Entity> charHash;
 
     static {
         entities = new Entity[] {
             new Entity("&nbsp;", '\u00A0'),
             new Entity("&iexcl;", '\u00A1'),
             new Entity("&cent;", '\u00A2'),
             new Entity("&pound;", '\u00A3'),
             new Entity("&curren;", '\u00A4'),
             new Entity("&yen;", '\u00A5'),
             new Entity("&brvbar;", '\u00A6'),
             new Entity("&sect;", '\u00A7'),
             new Entity("&uml;", '\u00A8'),
             new Entity("&copy;", '\u00A9'),
             new Entity("&ordf;", '\u00AA'),
             new Entity("&laquo;", '\u00AB'),
             new Entity("&not;", '\u00AC'),
             new Entity("&shy;", '\u00AD'),
             new Entity("&reg;", '\u00AE'),
             new Entity("&macr;", '\u00AF'),
             new Entity("&deg;", '\u00B0'),
             new Entity("&plusmn;", '\u00B1'),
             new Entity("&sup2;", '\u00B2'),
             new Entity("&sup3;", '\u00B3'),
             new Entity("&acute;", '\u00B4'),
             new Entity("&micro;", '\u00B5'),
             new Entity("&para;", '\u00B6'),
             new Entity("&middot;", '\u00B7'),
             new Entity("&cedil;", '\u00B8'),
             new Entity("&sup1;", '\u00B9'),
             new Entity("&ordm;", '\u00BA'),
             new Entity("&raquo;", '\u00BB'),
             new Entity("&frac14;", '\u00BC'),
             new Entity("&frac12;", '\u00BD'),
             new Entity("&frac34;", '\u00BE'),
             new Entity("&iquest;", '\u00BF'),
             new Entity("&Agrave;", '\u00C0'),
             new Entity("&Aacute;", '\u00C1'),
             new Entity("&Acirc;", '\u00C2'),
             new Entity("&Atilde;", '\u00C3'),
             new Entity("&Auml;", '\u00C4'),
             new Entity("&Aring;", '\u00C5'),
             new Entity("&AElig;", '\u00C6'),
             new Entity("&Ccedil;", '\u00C7'),
             new Entity("&Egrave;", '\u00C8'),
             new Entity("&Eacute;", '\u00C9'),
             new Entity("&Ecirc;", '\u00CA'),
             new Entity("&Euml;", '\u00CB'),
             new Entity("&Igrave;", '\u00CC'),
             new Entity("&Iacute;", '\u00CD'),
             new Entity("&Icirc;", '\u00CE'),
             new Entity("&Iuml;", '\u00CF'),
             new Entity("&ETH;", '\u00D0'),
             new Entity("&Ntilde;", '\u00D1'),
             new Entity("&Ograve;", '\u00D2'),
             new Entity("&Oacute;", '\u00D3'),
             new Entity("&Ocirc;", '\u00D4'),
             new Entity("&Otilde;", '\u00D5'),
             new Entity("&Ouml;", '\u00D6'),
             new Entity("&times;", '\u00D7'),
             new Entity("&Oslash;", '\u00D8'),
             new Entity("&Ugrave;", '\u00D9'),
             new Entity("&Uacute;", '\u00DA'),
             new Entity("&Ucirc;", '\u00DB'),
             new Entity("&Uuml;", '\u00DC'),
             new Entity("&Yacute;", '\u00DD'),
             new Entity("&THORN;", '\u00DE'),
             new Entity("&szlig;", '\u00DF'),
             new Entity("&agrave;", '\u00E0'),
             new Entity("&aacute;", '\u00E1'),
             new Entity("&acirc;", '\u00E2'),
             new Entity("&atilde;", '\u00E3'),
             new Entity("&auml;", '\u00E4'),
             new Entity("&aring;", '\u00E5'),
             new Entity("&aelig;", '\u00E6'),
             new Entity("&ccedil;", '\u00E7'),
             new Entity("&egrave;", '\u00E8'),
             new Entity("&eacute;", '\u00E9'),
             new Entity("&ecirc;", '\u00EA'),
             new Entity("&euml;", '\u00EB'),
             new Entity("&igrave;", '\u00EC'),
             new Entity("&iacute;", '\u00ED'),
             new Entity("&icirc;", '\u00EE'),
             new Entity("&iuml;", '\u00EF'),
             new Entity("&eth;", '\u00F0'),
             new Entity("&ntilde;", '\u00F1'),
             new Entity("&ograve;", '\u00F2'),
             new Entity("&oacute;", '\u00F3'),
             new Entity("&ocirc;", '\u00F4'),
             new Entity("&otilde;", '\u00F5'),
             new Entity("&ouml;", '\u00F6'),
             new Entity("&divide;", '\u00F7'),
             new Entity("&oslash;", '\u00F8'),
             new Entity("&ugrave;", '\u00F9'),
             new Entity("&uacute;", '\u00FA'),
             new Entity("&ucirc;", '\u00FB'),
             new Entity("&uuml;", '\u00FC'),
             new Entity("&yacute;", '\u00FD'),
             new Entity("&thorn;", '\u00FE'),
             new Entity("&yuml;", '\u00FF'),
             new Entity("&quot;", '\u0022'),
             new Entity("&amp;", '\u0026'),
             new Entity("&lt;", '\u003C'),
             new Entity("&gt;", '\u003E'),
             //new Entity("&apos;", '\u0027'),
             new Entity("&OElig;", '\u0152'),
             new Entity("&oelig;", '\u0153'),
             new Entity("&Scaron;", '\u0160'),
             new Entity("&scaron;", '\u0161'),
             new Entity("&Yuml;", '\u0178'),
             new Entity("&circ;", '\u02C6'),
             new Entity("&tilde;", '\u02DC'),
             new Entity("&ensp;", '\u2002'),
             new Entity("&emsp;", '\u2003'),
             new Entity("&thinsp;", '\u2009'),
             new Entity("&zwnj;", '\u200C'),
             new Entity("&zwj;", '\u200D'),
             new Entity("&lrm;", '\u200E'),
             new Entity("&rlm;", '\u200F'),
             new Entity("&ndash;", '\u2013'),
             new Entity("&mdash;", '\u2014'),
             new Entity("&lsquo;", '\u2018'),
             new Entity("&rsquo;", '\u2019'),
             new Entity("&sbquo;", '\u201A'),
             new Entity("&ldquo;", '\u201C'),
             new Entity("&rdquo;", '\u201D'),
             new Entity("&bdquo;", '\u201E'),
             new Entity("&dagger;", '\u2020'),
             new Entity("&Dagger;", '\u2021'),
             new Entity("&permil;", '\u2030'),
             new Entity("&lsaquo;", '\u2039'),
             new Entity("&rsaquo;", '\u203A'),
             new Entity("&euro;", '\u20AC'),
             new Entity("&fnof;", '\u0192'),
             new Entity("&Alpha;", '\u0391'),
             new Entity("&Beta;", '\u0392'),
             new Entity("&Gamma;", '\u0393'),
             new Entity("&Delta;", '\u0394'),
             new Entity("&Epsilon;", '\u0395'),
             new Entity("&Zeta;", '\u0396'),
             new Entity("&Eta;", '\u0397'),
             new Entity("&Theta;", '\u0398'),
             new Entity("&Iota;", '\u0399'),
             new Entity("&Kappa;", '\u039A'),
             new Entity("&Lambda;", '\u039B'),
             new Entity("&Mu;", '\u039C'),
             new Entity("&Nu;", '\u039D'),
             new Entity("&Xi;", '\u039E'),
             new Entity("&Omicron;", '\u039F'),
             new Entity("&Pi;", '\u03A0'),
             new Entity("&Rho;", '\u03A1'),
             new Entity("&Sigma;", '\u03A3'),
             new Entity("&Tau;", '\u03A4'),
             new Entity("&Upsilon;", '\u03A5'),
             new Entity("&Phi;", '\u03A6'),
             new Entity("&Chi;", '\u03A7'),
             new Entity("&Psi;", '\u03A8'),
             new Entity("&Omega;", '\u03A9'),
             new Entity("&alpha;", '\u03B1'),
             new Entity("&beta;", '\u03B2'),
             new Entity("&gamma;", '\u03B3'),
             new Entity("&delta;", '\u03B4'),
             new Entity("&epsilon;", '\u03B5'),
             new Entity("&zeta;", '\u03B6'),
             new Entity("&eta;", '\u03B7'),
             new Entity("&theta;", '\u03B8'),
             new Entity("&iota;", '\u03B9'),
             new Entity("&kappa;", '\u03BA'),
             new Entity("&lambda;", '\u03BB'),
             new Entity("&mu;", '\u03BC'),
             new Entity("&nu;", '\u03BD'),
             new Entity("&xi;", '\u03BE'),
             new Entity("&omicron;", '\u03BF'),
             new Entity("&pi;", '\u03C0'),
             new Entity("&rho;", '\u03C1'),
             new Entity("&sigmaf;", '\u03C2'),
             new Entity("&sigma;", '\u03C3'),
             new Entity("&tau;", '\u03C4'),
             new Entity("&upsilon;", '\u03C5'),
             new Entity("&phi;", '\u03C6'),
             new Entity("&chi;", '\u03C7'),
             new Entity("&psi;", '\u03C8'),
             new Entity("&omega;", '\u03C9'),
             new Entity("&thetasym;", '\u03D1'),
             new Entity("&upsih;", '\u03D2'),
             new Entity("&piv;", '\u03D6'),
             new Entity("&bull;", '\u2022'),
             new Entity("&hellip;", '\u2026'),
             new Entity("&prime;", '\u2032'),
             new Entity("&Prime;", '\u2033'),
             new Entity("&oline;", '\u203E'),
             new Entity("&frasl;", '\u2044'),
             new Entity("&weierp;", '\u2118'),
             new Entity("&image;", '\u2111'),
             new Entity("&real;", '\u211C'),
             new Entity("&trade;", '\u2122'),
             new Entity("&alefsym;", '\u2135'),
             new Entity("&larr;", '\u2190'),
             new Entity("&uarr;", '\u2191'),
             new Entity("&rarr;", '\u2192'),
             new Entity("&darr;", '\u2193'),
             new Entity("&harr;", '\u2194'),
             new Entity("&crarr;", '\u21B5'),
             new Entity("&lArr;", '\u21D0'),
             new Entity("&uArr;", '\u21D1'),
             new Entity("&rArr;", '\u21D2'),
             new Entity("&dArr;", '\u21D3'),
             new Entity("&hArr;", '\u21D4'),
             new Entity("&forall;", '\u2200'),
             new Entity("&part;", '\u2202'),
             new Entity("&exist;", '\u2203'),
             new Entity("&empty;", '\u2205'),
             new Entity("&nabla;", '\u2207'),
             new Entity("&isin;", '\u2208'),
             new Entity("&notin;", '\u2209'),
             new Entity("&ni;", '\u220B'),
             new Entity("&prod;", '\u220F'),
             new Entity("&sum;", '\u2211'),
             new Entity("&minus;", '\u2212'),
             new Entity("&lowast;", '\u2217'),
             new Entity("&radic;", '\u221A'),
             new Entity("&prop;", '\u221D'),
             new Entity("&infin;", '\u221E'),
             new Entity("&ang;", '\u2220'),
             new Entity("&and;", '\u2227'),
             new Entity("&or;", '\u2228'),
             new Entity("&cap;", '\u2229'),
             new Entity("&cup;", '\u222A'),
             new Entity("&int;", '\u222B'),
             new Entity("&there4;", '\u2234'),
             new Entity("&sim;", '\u223C'),
             new Entity("&cong;", '\u2245'),
             new Entity("&asymp;", '\u2248'),
             new Entity("&ne;", '\u2260'),
             new Entity("&equiv;", '\u2261'),
             new Entity("&le;", '\u2264'),
             new Entity("&ge;", '\u2265'),
             new Entity("&sub;", '\u2282'),
             new Entity("&sup;", '\u2283'),
             new Entity("&nsub;", '\u2284'),
             new Entity("&sube;", '\u2286'),
             new Entity("&supe;", '\u2287'),
             new Entity("&oplus;", '\u2295'),
             new Entity("&otimes;", '\u2297'),
             new Entity("&perp;", '\u22A5'),
             new Entity("&sdot;", '\u22C5'),
             new Entity("&lceil;", '\u2308'),
             new Entity("&rceil;", '\u2309'),
             new Entity("&lfloor;", '\u230A'),
             new Entity("&rfloor;", '\u230B'),
             new Entity("&lang;", '\u2329'),
             new Entity("&rang;", '\u232A'),
             new Entity("&loz;", '\u25CA'),
             new Entity("&spades;", '\u2660'),
             new Entity("&clubs;", '\u2663'),
             new Entity("&hearts;", '\u2665'),
             new Entity("&diams;", '\u2666')
         };
 
         nameHash = new HashMap<String, Entity>(entities.length);
         charHash = new HashMap<Character, Entity>(entities.length);
         for (Entity entity : entities) {
             nameHash.put(entity.getName(), entity);
             charHash.put(entity.getCharacter(), entity);
         }
 
     }
 
     /**
      * Returns entity character by entity name.
      * @param entityName entity name
      * @return character
      * @throws IllegalArgumentException exception thrown when entity name not e
      */
     public static char toCharacter(String entityName) throws IllegalArgumentException {
         if (nameHash.containsKey(entityName)) return nameHash.get(entityName).getCharacter();
         else throw new IllegalArgumentException("No such name: \"" + entityName + "\"");
     }
 
     /**
      * Returns entity character as string.
      * @param entityName entity name
      * @return one letter string
      * @throws IllegalArgumentException exception thrown when entity name not exist
      */
     public static String toCharacterAsString(String entityName) throws IllegalArgumentException {
         if (nameHash.containsKey(entityName)) return nameHash.get(entityName).getCharacterAsString();
         else throw new IllegalArgumentException("No such name: \"" + entityName + "\"");
     }
 
     public static String toEntityName(char character) throws IllegalArgumentException {
         if (charHash.containsKey(character)) return charHash.get(character).getName();
         else throw new IllegalArgumentException("No such char of name: \"" + character + "\"");
     }
 
     @Deprecated //TODO: not working
     public static String convertEntitiesToChars(String string) {
         for (Entity entity : entities)
             string = string.replace(entity.getName(), entity.getCharacterAsString());
         return string;
     }
 
     public static String convertCharsToEntities(String string) {
         for (Entity entity : entities)
             string = string.replace(entity.getCharacterAsString(), entity.getName());
         return string;
     }
 
     public Entity(String entity, char character) {
         this.name = entity;
         this.character = character;
     }
 
     public String getName() {
         return name;
     }
 
     public char getCharacter() {
         return character;
     }
 
     public String getCharacterAsString() {
         char[] chars = new char[1];
         chars[0] = character;
         return new String(chars);
     }
 }
