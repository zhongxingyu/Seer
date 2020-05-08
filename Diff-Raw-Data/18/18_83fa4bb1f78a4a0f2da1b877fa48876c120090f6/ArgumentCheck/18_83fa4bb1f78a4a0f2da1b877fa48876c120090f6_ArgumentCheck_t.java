 /*
  * Author: Dabo Ross
  * Website: www.daboross.net
  * Email: daboross@daboross.net
  */
 package net.daboross.bukkitdev.playerdata.libraries.dargumentchecker;
 
 import java.util.Collection;
 
 /**
  *
  * @author daboross
  */
 public class ArgumentCheck {
 
     public static void notNull(Object... objects) {
         if (objects == null) {
             throw new IllegalArgumentException("Null arguments not permitted");
         }
         for (Object o : objects) {
             if (o == null) {
                 throw new IllegalArgumentException("Null arguments not permitted");
             }
         }
     }
 
     public static void notNull(Collection<Object> objects) {
         if (objects == null) {
             throw new IllegalArgumentException("Null arguments not permitted");
         }
         for (Object o : objects) {
             if (o == null) {
                 throw new IllegalArgumentException("Null arguments not permitted");
             }
         }
     }
 }
