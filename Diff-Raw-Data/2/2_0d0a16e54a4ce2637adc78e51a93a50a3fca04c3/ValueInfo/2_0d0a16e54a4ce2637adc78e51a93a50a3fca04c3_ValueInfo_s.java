 package org.melati.poem;
 
 import org.melati.poem.generated.*;
 import java.util.*;
 import java.sql.Date;
 import java.sql.Timestamp;
 import org.melati.util.*;
 
 public class ValueInfo extends ValueInfoBase {
   public ValueInfo() {}
 
   public PoemTypeFactory.Parameter toTypeParameter() {
     final Boolean nullable = getNullable_unsafe();
     final Integer size = getSize_unsafe();
     return
         new PoemTypeFactory.Parameter() {
           public boolean getNullable() {
             return nullable == null || nullable.booleanValue();
           }
 
           public int getSize() {
             return size == null ? -1 : size.intValue();
           }
         };
   }
 
   private SQLPoemType poemType = null;
 
   public SQLPoemType getType() {
     if (poemType == null) {
       PoemTypeFactory f = getTypefactory();
 
       if (f == null) {
         // this can happen before we have been fully initialised
         // it's convenient to return the "most general" type available ...
         return StringPoemType.nullable;
       }
 
       poemType = f.typeOf(getDatabase(), toTypeParameter());
     }
 
     return poemType;
   }
 
   private SQLPoemType getRangeEndType() {
     // FIXME a little inefficient, but rarely used; also relies on BasePoemType
     // should have interface for ranged type ...
 
     SQLPoemType t = getType();
 
     if (t instanceof BasePoemType) {
       BasePoemType unrangedType = (BasePoemType)((BasePoemType)t).clone();
       unrangedType.setRawRange(null, null);
       return unrangedType;
     }
     else
       return null;
   }
 
   private FieldAttributes fieldAttributesRenamedAs(FieldAttributes c,
                                                    PoemType type) {
     return new BaseFieldAttributes(
         c.getName(), c.getDisplayName(), c.getDescription(), getType(),
         width == null ? 12 : width.intValue(),
         height == null ? 1 : height.intValue(),
         renderinfo,
         false,
         usereditable == null ? true : usereditable.booleanValue(),
         true);
   }
 
   public FieldAttributes fieldAttributesRenamedAs(FieldAttributes c) {
     return fieldAttributesRenamedAs(c, getType());
   }
 
   private Field rangeEndField(Column c) {
     SQLPoemType unrangedType = getRangeEndType();
 
     if (unrangedType == null)
       return null;
     else {
       Object raw;
       try {
         raw = unrangedType.rawOfString((String)c.getRaw_unsafe(this));
       }
       catch (Exception e) {
         System.err.println("Found a bad entry for " + c + " in " +
                            getTable().getName() + "/" + troid() + ": " +
                            "solution is to null it out ...");
         e.printStackTrace();
         c.setRaw_unsafe(this, null);
         raw = null;
       }
 
       return new Field(
           raw,
           fieldAttributesRenamedAs(c, unrangedType));
     }
   }
 
   public Field getRangelow_stringField() {
     Field it = rangeEndField(getValueInfoTable().getRangelow_stringColumn());
     return it != null ? it : super.getRangelow_stringField();
   }
 
   public Field getRangelimit_stringField() {
    Field it = rangeEndField(getValueInfoTable().getRangelow_stringColumn());
     return it != null ? it : super.getRangelimit_stringField();
   }
 
   public void setRangelow_string(String value) {
     PoemType unrangedType = getRangeEndType();
     if (unrangedType != null)
       value = unrangedType.stringOfRaw(unrangedType.rawOfString(value));
     super.setRangelow_string(value);
   }
 
   public void setRangelimit_string(String value) {
     PoemType unrangedType = getRangeEndType();
     if (unrangedType != null)
       value = unrangedType.stringOfRaw(unrangedType.rawOfString(value));
     super.setRangelimit_string(value);
   }
 }
