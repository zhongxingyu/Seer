 package org.ybiquitous.messages;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 
 public final class Utils {
 
     public static @Nonnull <T> T notNull(
            @Nullable T obj,
             @Nullable String name) throws NullPointerException {
 
         if (obj == null) {
             if (name == null) {
                 throw new NullPointerException();
             } else {
                 throw new NullPointerException(name + " is required");
             }
         }
         return obj;
     }
 
     public static @Nonnull <T> T notNullOrElse(
             @Nullable T obj,
             @Nonnull T defaultValue) throws NullPointerException {
 
         return (obj != null) ? obj : notNull(defaultValue, "defaultValue");
     }
 
     public static @Nonnull <T> T notNullOrElse(
             @Nullable T obj,
             @Nonnull Factory<T> defaultValueFactory) throws NullPointerException {
 
         return (obj != null) ? obj : notNull(defaultValueFactory, "defaultValueFactory").get();
     }
 
     public static boolean isEmpty(@Nullable String str) {
         return (str == null || str.isEmpty() || str.trim().isEmpty());
     }
 
     private Utils() {
     }
 }
