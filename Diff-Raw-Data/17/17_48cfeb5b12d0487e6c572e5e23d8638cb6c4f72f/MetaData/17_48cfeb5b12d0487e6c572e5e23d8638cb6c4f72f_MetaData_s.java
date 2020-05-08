 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package de.weltraumschaf.juberblog;
 
 import com.google.common.collect.Maps;
 import java.util.Map;
import java.util.Objects;
 import org.apache.commons.lang3.Validate;
 
 /**
  * Contains meta data found by {@link Preprocessor} in a data file.
  *
  * This object is immutable.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class MetaData {
 
     /**
      * Returned by default if a property is not set.
      */
     private static final String DEFAULT_VALUE = "";
     /**
      * Navigation property key.
      */
     private static final String KEY_NAVI = "Navi";
     /**
      * Description property key.
      */
     private static final String KEY_DESCRIPTION = "Description";
     /**
      * Keywords property key.
      */
     private static final String KEY_KEYWORDS = "Keywords";
     /**
      * Data map.
      */
     private final Map<String, String> data;
 
     /**
      * Initializes {@link #data} with an empty map.
      */
     public MetaData() {
         this(Maps.<String, String>newHashMap());
     }
 
     /**
      * Dedicated constructor.
      *
      * @param data must not be {@code null}
      */
     public MetaData(final Map<String, String> data) {
         super();
         Validate.notNull(data, "Data must not be null!");
         this.data = data;
     }
 
     /**
      * Get a key from {@link #data} which never returns {@code null}.
      *
      * @param key must not be {@code null} or empty
      * @return never {@code null}, maybe empty
      */
     private String get(final String key) {
         Validate.notEmpty(key, "Key must not be null or empty!");
 
         if (data.containsKey(key)) {
             return data.get(key);
         }
 
         return DEFAULT_VALUE;
     }
 
     /**
      * Get navigation data.
      *
      * @return never {@code null}, maybe empty
      */
     public String getNavi() {
         return get(KEY_NAVI);
     }
 
     /**
      * Get description data.
      *
      * @return never {@code null}, maybe empty
      */
     public String getDescription() {
         return get(KEY_DESCRIPTION);
     }
 
     /**
      * Get keywords data.
      *
      * @return never {@code null}, maybe empty
      */
     public String getKeywords() {
         return get(KEY_KEYWORDS);
     }
 
     @Override
     public int hashCode() {
         return data.hashCode();
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (!(obj instanceof MetaData)) {
             return false;
         }
 
         final MetaData other = (MetaData) obj;
         return data.equals(other.data);
     }
 
 }
