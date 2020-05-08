 package lib;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import core.CiteProc;
 
 public class CslName extends CslFormat {
 
   private String attrInit = "";
   private String delimiter = "";
   String and = "";
   String dpl;
   String sort_separator;
   String form = "";
   String given = "";
   String initials = "";
   String family = "";
   Node pdomNode;
   CiteProc pciteProc;
   String alnum = "";
   String alpha = "";
   String cntrl = "";
   String dash = "";
   String digit = "";
   String graph = "";
   String lower = "";
   String print = "";
   String punct = "";
   String space = "";
   String upper = "";
   String word = "";
   String patternModifiers = "";
 
   public CslName(Node domNode, CiteProc citeProc) {
     super(domNode, citeProc);
 
     this.pdomNode = domNode;
     this.pciteProc = citeProc;
   }
 
   public String render(JSONArray names, String mode) {
 
     String text = "";
     HashMap authors = new HashMap();
     int count = 0;
     int authCount = 0;
     String givenValue = "";
     boolean etAlTriggered = false;
     String initializeWith = "";
     Integer key = 0;
     System.out.println("*****************************");
     System.out.println("*********CSLNAME************");
     System.out.println("*****************************" + this.elements);
     try {
       if (this.attributes.get("initialize-with") != null)
         initializeWith = (String) this.attributes.get("initialize-with").toString();
 
       if (this.attrInit != null || !(this.attrInit.equalsIgnoreCase(mode))) {
 
         this.initAttrs(mode);
       }
       if (names != null) {
         for (int i = 0; i < names.size(); i++) {
           count++;
           JSONObject name = (JSONObject) names.get(i);
           if (name.containsKey("given") && initializeWith != null && !initializeWith.equalsIgnoreCase("")) {
 
             givenValue = name.get("given").toString()
                 .replaceAll("([" + this.upper + "])[" + this.lower + "]+" + this.patternModifiers, "$1");
 
             givenValue = givenValue.toString().replaceAll("(?<=[-" + this.upper + "]) +(?=[-" + this.upper + "])" + this.patternModifiers,
                 "");
 
             name.put("given", givenValue);
             if (name.get("initials") != null) {
               name.put("initials", givenValue);
             }
             name.put("initials", givenValue);
             initials = givenValue;
 
           }
           if (initials != null && !initials.equalsIgnoreCase("")) {
 
             // within initials, remove any dots:
             initials = initials.toString().replaceAll("([" + this.upper + "])\\.+" + this.patternModifiers, "$1");
             // within initials, remove any spaces *between* initials:
             initials = initials.toString().replaceAll("(?<=[-" + this.upper + "]) +(?=[-" + this.upper + "])" + this.patternModifiers, "");
             if (this.citeProc.style.attributes.get("initialize-with-hyphen") != null
                 && this.citeProc.style.attributes.get("initialize-with-hyphen").equals("false")) {
               initials = name.get("initials").toString().replaceAll("-", "");
             }
             String pattern = "/ $/";
             // within initials, add a space after a hyphen, but only if ...
             if (initializeWith.matches(pattern)) {// ... the delimiter that separates initials ends with a space
               initials = initials.toString().replaceAll("-(?=[" + this.upper + "])" + this.patternModifiers, "- ");
             }
             // then, separate initials with the specified delimiter:
             initials = initials.toString().replaceAll("([" + this.upper + "])(?=[^" + this.lower + "]+|$)" + this.patternModifiers,
                 "$1" + initializeWith);
 
             if (initializeWith != null) {
               givenValue = initials;
             } else if (!given.isEmpty()) {
               givenValue = given + " " + initials;
             } else if (given.isEmpty()) {
               givenValue = initials;
             }
             name.put("given", givenValue.trim());
           }
 
           String ndp = name.containsKey("non-dropping-particle") ? name.get("non-dropping-particle") + " " : "";
           String suffix = name.containsKey("suffix") ? " " + name.get("suffix") : "";
 
           if (name.get("given") != null) {
             given = this.format(name.get("given").toString(), "given");
           }
           if (name.containsKey("family")) {
             family = this.format(name.get("family").toString(), "family");
             if (this.form.equalsIgnoreCase("short")) {
               text = ndp + family;
             } else {
               String val = (String) this.attributes.get("name-as-sort-order");
               if (val != null && val.equalsIgnoreCase("first")) {
 
               } else if (val != null && val.equalsIgnoreCase("all")) {
                 text = ndp + name.get("family") + this.sort_separator + given;
               } else {
                 text = given + " " + ndp + family + suffix;
               }
             }
            authors.put(key, this.format(text));
             key++;
           }
           if ((this.attributes.get("et-al-min") != null) && (count >= Integer.parseInt(this.attributes.get("et-al-min").toString())))
             break;
         }
       }
       if ((this.attributes.get("et-al-min") != null) && (count >= Integer.parseInt(this.attributes.get("et-al-min").toString()))
           && this.attributes.get("et-al-use-first") != null) {
         if (Integer.parseInt(this.attributes.get("et-al-use-first").toString()) < Integer.parseInt(this.attributes.get("et-al-min")
             .toString())) {
           for (Integer i = Integer.parseInt(this.attributes.get("et-al-use-first").toString()); i < count; i++) {
             authors.remove(i);
           }
         }
         if (this.attributes.get("etal") != null) {
           // authors = $this->etal->render();
 
         } else {
           authors.put(key, this.citeProc.getLocale("term", "et-al", "", ""));
           key++;
         }
         etAlTriggered = true;
       }
 
       if ((!authors.isEmpty()) && !etAlTriggered) {
         authCount = authors.size();
         if (this.and != null && authCount > 1) {
           Integer pos = (authCount - 1);
 
           String value = this.and + " " + authors.get(authCount - 1);
           authors.remove(pos);
           authors.put(key, value);// stick an "and" in front of the last author if "and" is defined
           key++;
         }
       }
       // for (int i = 0; i < authors.size(); i++) {
       Set s = authors.entrySet();
       int j = 0;
       Iterator i = s.iterator();
       while (i.hasNext()) {
         Map.Entry me = (Map.Entry) i.next();
         String auth = me.getValue().toString();
         if (j == 0)
          text = auth;
         else
          text = text + this.delimiter + auth;
         j++;
       }
 
       if (this.form.equalsIgnoreCase("count")) {
         if (!etAlTriggered) {
           return Integer.toString(authors.size());
         } else {
           return Integer.toString(authors.size() - 1);
         }
       }
       // strip out the last delimiter if not required
       if (this.and != null && authCount > 1) {
         int lastDelim = text.indexOf(this.delimiter + this.and);
         if (this.dpl.equalsIgnoreCase("always")) {
           return text;
         } else if (this.dpl.equalsIgnoreCase("never")) {
           String subStr = text.substring(lastDelim);
           text = text.replaceAll(subStr, " ");
           return text;
         } else if (this.dpl.equalsIgnoreCase("contextual")) {
 
         } else {
           if (authCount < 3) {
             String subStr = text.substring(lastDelim);
             text = text.replaceAll(subStr, " ");
             return text;
           }
         }
 
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     return text;
   }
 
   public String format(String text, String part) {
 
     if (text.isEmpty() || ((Boolean) this.citeProc.noOp.get(part)).booleanValue() == true) {
 
       return text;
     }
     if (this.citeProc.format.get(part) != null) {
       text = "<span style=\"" + this.citeProc.format.get(part) + "\">" + text + "</span>";
     }
     return text;
 
     /*
      * if(text.isEmpty() || this.noOp.get(part) != null)
      * return text;
      * if (isset($this->{$part}['text-case'])) {
      * switch ($this->{$part}['text-case']) {
      * case 'uppercase':
      * $text = mb_strtoupper($text);
      * break;
      * case 'lowercase':
      * $text = mb_strtolower($text);
      * break;
      * case 'capitalize-all':
      * $text = mb_convert_case($text, MB_CASE_TITLE);
      * break;
      * case 'capitalize-first':
      * $chr1 = mb_strtoupper(mb_substr($text, 0, 1));
      * $text = $chr1 . mb_substr($text, 1);
      * break;
      * }
      * }
      */
     /*
      * $open_quote = isset($this->{$part}['open-quote']) ? $this->{$part}['open-quote'] : '';
      * $close_quote = isset($this->{$part}['close-quote']) ? $this->{$part}['close-quote'] : '';
      * $prefix = isset($this->{$part}['prefix']) ? $this->{$part}['prefix'] : '';
      * $suffix = isset($this->{$part}['suffix']) ? $this->{$part}['suffix'] : '';
      * if ($text[(strlen($text) -1)] == $suffix) unset($suffix);
      * if (!empty($this->format[$part])) {
      * $text = '<span style="' . $this->format[$part] . '">' . $text . '</span>';
      * }
      * return $prefix . $open_quote . $text . $close_quote . $suffix;
      */
     // return "";
   }
 
   public void initAttrs(String mode) {
     HashMap styleAttrs = null;
     HashMap modeAttrs = null;
     this.and = (String) this.attributes.get("and");
     if (this.and != null && !this.and.isEmpty()) {
       if (this.and.equalsIgnoreCase("text")) {
         this.and = this.citeProc.getLocale("term", "and", "", "");
       } else if (this.and.equalsIgnoreCase("symbol")) {
         this.and = "&";
       }
     }
     if (this.citeProc != null) {
       styleAttrs = this.citeProc.style.get_hier_attributes();
       if (mode.equalsIgnoreCase("citation"))
         modeAttrs = this.citeProc.citation.get_hier_attributes();
       if (styleAttrs != null)
         this.attributes.putAll(styleAttrs);
       if (modeAttrs != null)
         this.attributes.putAll(modeAttrs);
     }
     if (this.attributes.get("delimiter") != null)
       this.delimiter = this.attributes.get("delimiter").toString();
     if (this.delimiter == null && this.delimiter.equalsIgnoreCase("")) {
       this.delimiter = (String) this.attributes.get("name-delimiter");
     }
     HashMap regMap = new HashMap();
 
     if (this.alnum == null || alnum.equalsIgnoreCase("")) {
       regMap = getRegexPatterns();
       this.alnum = regMap.get("alnum").toString();
       this.alpha = regMap.get("alpha").toString();
       this.cntrl = regMap.get("cntrl").toString();
       this.dash = regMap.get("dash").toString();
       this.digit = regMap.get("digit").toString();
       this.graph = regMap.get("graph").toString();
       this.lower = regMap.get("lower").toString();
       this.print = regMap.get("print").toString();
       this.punct = regMap.get("punct").toString();
       this.space = regMap.get("space").toString();
       this.upper = regMap.get("upper").toString();
       this.word = regMap.get("word").toString();
       this.patternModifiers = regMap.get("patternModifiers").toString();
 
     }
     System.out.println("this.attributes--........--" + this.attributes);
     this.dpl = (String) this.attributes.get("delimiter-precedes-last");
     this.sort_separator = (String) ((this.attributes.get("sort-separator") != null) ? this.attributes.get("sort-separator") : ",");
     this.form = (this.attributes.get("form") != null) ? this.attributes.get("form").toString() : "long";
     this.attrInit = mode;
   }
 
   public void setNamePairs(Node domNode) {
     Element el = (Element) domNode;
     NodeList tags = el.getElementsByTagName("name-part");
 
     try {
       if (this.citeProc.nameParts == null)
         this.citeProc.nameParts = new HashMap();
       if (tags != null) {
         String namePart = null;
         for (int i = 0; i < tags.getLength(); i++) {
 
           if (tags.item(i).getAttributes().getNamedItem("name") != null) {
 
             namePart = tags.item(i).getAttributes().getNamedItem("name").getNodeValue();
 
             tags.item(i).getAttributes().removeNamedItem("name");
 
             String value = "";
             String name = "";
             HashMap tempName = new HashMap();
 
             for (int j = 0; j < tags.item(i).getAttributes().getLength(); j++) {
               value = tags.item(i).getAttributes().item(j).getNodeValue();
               name = tags.item(i).getAttributes().item(j).getNodeName().replaceAll(" ", "_");
               tempName.put(name, value);
               this.citeProc.nameParts.put(namePart, tempName);
             }
           }
         }
       }
       // }
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   public void initFormatting(Node domNode) {
     setNamePairs(domNode);
 
     HashMap base = this.get_attributes();
 
     if (this.citeProc.format == null) {
       this.citeProc.format = new HashMap();
 
     }
 
     this.citeProc.format.put("base", "");
     this.citeProc.format.put("family", "");
     this.citeProc.format.put("given", "");
     this.citeProc.noOp.put("base", new Boolean(true));
     this.citeProc.noOp.put("family", new Boolean(true));
     this.citeProc.noOp.put("given", new Boolean(true));
 
     this.initFormat(base, "");
     HashMap temp = null;
     if (this.citeProc.nameParts != null) {
       Set s = this.citeProc.nameParts.entrySet();
       Iterator i = s.iterator();
       while (i.hasNext()) {
         Map.Entry me = (Map.Entry) i.next();
         Object ob = me.getValue();
         if (ob instanceof HashMap) {
           temp = (HashMap) ob;
           this.initFormat((HashMap) me.getValue(), me.getKey());
         }
       }
 
     }
   }
 
   public void initFormat(HashMap attribs, Object part) {
 
     String value = "";
     if (attribs.get("font-weight") != null) {
       value += "font-weight: " + attribs.get("font-weight");
     }
     if (attribs.get("font-style") != null) {
       value += "font-style: " + attribs.get("font-style");
     }
     if (attribs.get("font-family") != null) {
       value += "font-family: " + attribs.get("font-family");
     }
     if (attribs.get("font-variant") != null) {
       value += "font-variant: " + attribs.get("font-variant");
     }
     if (attribs.get("text-decoration") != null) {
       value += "text-decoration: " + attribs.get("text-decoration");
     }
     if (attribs.get("vertical-align") != null) {
       value += "vertical-align: " + attribs.get("vertical-align");
     }
     this.citeProc.format.put(part, value);
     if (attribs.containsKey("text-case")) {
       this.citeProc.noOp.put(part, new Boolean(false));
     }
     if (this.citeProc.format.get(part) != null)
       this.citeProc.noOp.put(part, new Boolean(false));
   }
 
   public HashMap getRegexPatterns() {
     // Checks if PCRE is compiled with UTF-8 and Unicode support
 
     String pattern = "[\\pL]";
 
     // within initials, add a space after a hyphen, but only if ...
     if (!("a".matches(pattern))) {
       // probably a broken PCRE library
       return getLatin1Regex();
     } else {
       // Unicode safe filter for the value
       return getUtf8Regex();
     }
 
   }
 
   public HashMap getLatin1Regex() {
     System.out.println("**********getLatin1Regex***************");
     HashMap retMap = new HashMap();
     retMap.put("alnum", "[:alnum:]");
     // Matches ISO-8859-1 letters:
     retMap.put("alpha", "[:alpha:]");
     // Matches ISO-8859-1 control characters:
     retMap.put("cntrl", "[:cntrl:]");
     // Matches ISO-8859-1 dashes & hyphens:
     retMap.put("dash", "-");
     // Matches ISO-8859-1 digits:
     retMap.put("digit", "[\\d]");
     // Matches ISO-8859-1 printing characters (excluding space):
     retMap.put("graph", "[:graph:]");
     // Matches ISO-8859-1 lower case letters:
     retMap.put("lower", "[:lower:]");
     // Matches ISO-8859-1 printing characters (including space):
     retMap.put("print", "[:print:]");
     // Matches ISO-8859-1 punctuation:
     retMap.put("punct", "[:punct:]");
     // Matches ISO-8859-1 whitespace (separating characters with no visual representation):
     retMap.put("space", "[\\s]");
     // Matches ISO-8859-1 upper case letters:
     retMap.put("upper", "[:upper:]");
     // Matches ISO-8859-1 "word" characters:
     retMap.put("word", "_[:alnum:]");
     // Defines the PCRE pattern modifier(s) to be used in conjunction with the above variables:
     // More info: <http://www.php.net/manual/en/reference.pcre.pattern.modifiers.php>
     retMap.put("patternModifiers", "");
     return retMap;
 
   }
 
   public HashMap getUtf8Regex() {
     System.out.println("**********getUtf8Regex***************");
     HashMap retMap = new HashMap();
     // Matches Unicode letters & digits:
     retMap.put("alnum", "\\pL\\pN");// Unicode-aware equivalent of "[:alnum:]"
 
     // Matches Unicode letters:
     retMap.put("alpha", "\\pL");// Unicode-aware equivalent of "[:alpha:]"
     // Matches Unicode control codes & characters not in other categories:
     retMap.put("cntrl", "\\pC");// Unicode-aware equivalent of "[:cntrl:]"
     // Matches Unicode dashes & hyphens:
     retMap.put("dash", "\\Pd");
     // Matches Unicode digits:
     retMap.put("digit", "\\Nd");// Unicode-aware equivalent of "[:digit:]"
     // Matches Unicode printing characters (excluding space):
     retMap.put("graph", "\\p{Zs}\\p{gc=Cc}\\p{gc=Cs}\\p{gc=Cn}");// Unicode-aware equivalent of "[:graph:]"
     // Matches Unicode lower case letters:
     retMap.put("lower", "\\p{Ll}\\p{M}");// Unicode-aware equivalent of "[:lower:]"
     // Matches Unicode printing characters (including space):
     retMap.put("print", "\\P{C}");// same as "^\p{C}", Unicode-aware equivalent of "[:print:]"
     // Matches Unicode punctuation (printing characters excluding letters & digits):
     retMap.put("punct", "\\p{P}");// Unicode-aware equivalent of "[:punct:]"
     // Matches Unicode whitespace (separating characters with no visual representation):
     retMap.put("space", "\\t\\n\\f\\r\\p{Z}");// Unicode-aware equivalent of "[:space:]"
     // Matches Unicode upper case letters:
     retMap.put("upper", "\\p{Lu}\\p{Lt}");// Unicode-aware equivalent of "[:upper:]"
     // Matches Unicode "word" characters:
     retMap.put("word", "_\\p{Ll}\\p{Lu}\\p{Lt}\\p{Lo}\\p{Nd}");// Unicode-aware equivalent of "[:word:]" (or "[:alnum:]" plus "_")
     // Defines the PCRE pattern modifier(s) to be used in conjunction with the above variables:
     // More info: <http://www.php.net/manual/en/reference.pcre.pattern.modifiers.php>
     retMap.put("patternModifiers", "");// the "u" (PCRE_UTF8) pattern modifier causes PHP/PCRE to treat pattern strings as UTF-8
     return retMap;
   }
 
 }
