 package fi.helsinki.biblex.domain;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * This class stores a BibTex-reference.
  *
  * @author rodde
  */
 public class BibTexEntry implements Iterable<Map.Entry<String,String>> {
     private String name;
     private BibTexStyle style;
     private Map<String, String> m;
 
     public BibTexEntry(String name, BibTexStyle style) {
         if (name == null) {
             throw new IllegalArgumentException(
                     "Entry name may not be null.");
         }
 
         if (style == null) {
             throw new IllegalArgumentException(
                     "bibtex style may not be null.");
         }
 
         this.name = name;
         this.style = style;
         // TreeMap guarantees lexicographic order of fields while iterating
         // over the fields.
         m = new TreeMap<String, String>();
     }
 
     public BibTexEntry(String name, String style) {
         this(name, BibTexStyle.valueOf(style.toUpperCase()));
     }
 
     public Iterator<Map.Entry<String,String>> iterator() {
         return m.entrySet().iterator();
     }
 
     public String put(String fieldName, String value) {
         return m.put(fieldName, value);
     }
 
     public String get(String fieldName) {
         return m.get(fieldName);
     }
 
     public boolean containsField(String fieldName) {
         return m.containsKey(fieldName);
     }
 
     public String remove(String fieldName) {
         return m.remove(fieldName);
     }
 
     public String getName() {
         return name;
     }
 
     public BibTexStyle getStyle() {
         return style;
     }
 
     public String toString() {
         StringBuilder sb = new StringBuilder();
 
         sb.append('@');
         sb.append(style);
         sb.append("{ ");
 
         final int indent = sb.length();
         final char[] intro = new char[indent];
 
         for (int i = 0; i < indent; ++i) {
             intro[i] = ' ';
         }
 
         sb.append(name);
 
         for (Map.Entry<String, String> e : this) {
             sb.append(",\n");
             sb.append(intro);
             sb.append(e.getKey());
            sb.append(" = {");
             sb.append(e.getValue());
            sb.append('}');
         }
 
        sb.append(",\n}");
 
         return sb.toString();
     }
 }
