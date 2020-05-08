 package ru.finam.bustard.java;
 
 import ru.finam.bustard.Bustard;
 import ru.finam.bustard.codegen.Consts;
 
 public class BustardFactory implements Consts {
     public static Bustard createBustard() {
         try {
            Class bustardType = Class.forName(BUSTARD_PACKAGE_NAME + "." + BUSTARD_IMPL_NAME);
             return (Bustard) bustardType.newInstance();
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 }
