 package com.wealdtech.utils;
 
 import java.util.Collection;
 
 public class GuavaUtils
 {
  public static <T extends Collection<?>> T emptyToNull(final T cl)
   {
     if (cl == null || cl.isEmpty())
     {
       return null;
     }
     return cl;
   }
 }
