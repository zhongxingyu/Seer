 /*
  * Created on Sep 16, 2004
  */
 package uk.org.ponder.stringutil;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * A convenience wrapper for a type-safe list of String objects.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class StringList extends ArrayList {
 
   public StringList() {
   }
 
   public StringList(Collection tocopy) {
     append(tocopy);
   }
 
   public StringList(String string) {
     ensureCapacity(2);
     add(string);
   }
   
   public StringList(String[] strings) {
     ensureCapacity(strings.length);
     append(strings);
   }
 
   public String stringAt(int i) {
     return (String) get(i);
   }
 
   public boolean add(Object o) {
     if (o != null && !(o instanceof String)) {
       throw new UniversalRuntimeException("Object " + o + " of " + o.getClass()
           + " added to StringList");
     }
     return super.add(o);
   }
 
   /**
    * Appends the members of the supplied array to this list.
    * 
    * @param toappend The array of Strings to be appended, which may be
    *          <code>null</code>.
    */
   
   public void append(String[] toappend) {
     if (toappend != null) {
       for (int i = 0; i < toappend.length; ++i) {
         add(toappend[i]);
       }
     }
   }
   
   /**
    * Appends the members of the supplied list to this list.
    * 
    * @param toappend The list of Strings to be appended, which may be
    *          <code>null</code>.
    */
   public void append(StringList toappend) {
     if (toappend != null) {
       for (int i = 0; i < toappend.size(); ++i) {
         add(toappend.get(i));
       }
     }
   }
 
   public void append(Collection toappend) {
     for (Iterator it = toappend.iterator(); it.hasNext();) {
      String next = (String) it.next(); // ensure ClassCastException if wrong
      // type
       add(next);
     }
   }
 
   public StringList copy() {
     StringList togo = new StringList();
     togo.addAll(this);
     return togo;
   }
 
   public String pack() {
     CharWrap togo = new CharWrap();
     for (int i = 0; i < size(); ++i) {
       togo.append(stringAt(i));
       togo.append('\n');
     }
     return togo.toString();
   }
 
   public String[] toStringArray() {
     String[] togo = (String[]) toArray(new String[size()]);
     return togo;
   }
 
   public String toString() {
     CharWrap togo = new CharWrap();
     for (int i = 0; i < size(); ++i) {
       togo.append(stringAt(i));
       togo.append(", ");
     }
     return togo.toString();
   }
 
   /**
    * Construct a StringList from a comma separated list of Strings, trimming
    * whitespace.
    * 
    * @param commasep
    * @return
    */
   public static StringList fromString(String commasep) {
     String[] strings = commasep.split(",");
     StringList togo = new StringList();
 
     for (int i = 0; i < strings.length; ++i) {
       String toadd = strings[i].trim();
       // ensure that empty string produces empty list
       if (toadd.length() != 0 || strings.length > 1) {
         togo.add(toadd);
       }
     }
     return togo;
   }
 }
