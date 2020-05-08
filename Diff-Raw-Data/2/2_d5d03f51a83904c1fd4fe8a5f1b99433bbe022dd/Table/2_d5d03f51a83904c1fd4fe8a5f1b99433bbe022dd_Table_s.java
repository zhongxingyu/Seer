 package org.narwhal.annotation;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 
 /**
 * The <code>Table</code> annotation marks particular
  * class which maps on the database table.
  *
  * @author Miron Aseev
  */
 @Target(value = ElementType.TYPE)
 @Retention(value = RetentionPolicy.RUNTIME)
 public @interface Table {
 
     /**
      * Returns a table name that maps
      * to the particular entity.
      *
      * @return Table name.
      * */
     String value();
 }
