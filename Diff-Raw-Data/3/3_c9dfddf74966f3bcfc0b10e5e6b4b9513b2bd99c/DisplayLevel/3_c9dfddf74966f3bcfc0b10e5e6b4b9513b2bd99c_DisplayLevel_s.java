 package org.melati.poem;
 
 import java.util.*;
 import org.melati.util.*;
 
 public class DisplayLevel {
 
   public final Integer index;
   public final String name;
 
   private DisplayLevel(int index, String name) {
     this.index = new Integer(index);
     this.name = name;
   }
 
   public static final DisplayLevel
       primary, summary, record, detail, never;
 
   private static int n = 0;
 
   private static final DisplayLevel[] displayLevels =
     { primary = new DisplayLevel(n++, "primary"),
       summary = new DisplayLevel(n++, "summary"),
       record = new DisplayLevel(n++, "record"),
       detail = new DisplayLevel(n++, "detail"),
       never = new DisplayLevel(n++, "never") };
 
   private static final Hashtable levelOfName = new Hashtable();
 
   static {
     for (int i = 0; i < displayLevels.length; ++i)
       levelOfName.put(displayLevels[i].name, displayLevels[i]);
   }
 
   public static DisplayLevel forIndex(int index) {
     return displayLevels[index];
   }
 
   public static int count() {
     return displayLevels.length;
   }
 
  public static class NameUnrecognisedException
      extends MelatiRuntimeException {
     public String name;
 
     public NameUnrecognisedException(String name) {
       this.name = name;
     }
 
     public String getMessage() {
       return "No display level found which goes by the name `" + name + "'";
     }
   }
 
   public static DisplayLevel named(String name) {
     DisplayLevel it = (DisplayLevel)levelOfName.get(name);
     if (it == null)
       throw new NameUnrecognisedException(name);
     return it;
   }
 }
