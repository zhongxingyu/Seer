 package com.develogical.crypto;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class Encrypt {
 
     public static void main(String[] args) {
         System.out.println(new Encrypt().run(args));
     }
 
     public String run(String... args) {
         List<String> argList = Arrays.asList(args);
 
         if (argList.isEmpty()) {
             return usageMessage();
         }
 
        Cipher transform = null;
         Mode mode = Mode.ENCODE;
         String text = "";
         for (String arg : argList) {
             if (arg.startsWith("-")) {
                 if ("-reverse".equals(arg)) {
                     transform = new ReverseCipher();
                 } else if ("-substitute".equals(arg)) {
                     transform = new SubstitutionCipher();
                 } else if ("-decode".equals(arg)) {
                     mode = Mode.DECODE;
                 }
             } else {
                 text = arg;
             }
         }
 
         return mode.apply(transform, text);
     }
 
     private String usageMessage() {
         return "Encrypt (usage): try adding arguments e.g. '-reverse abcd'";
     }
 
     enum Mode {
         ENCODE {
             @Override
             String apply(Cipher cipher, String text) {
                 return cipher.encode(text);
             }
         },
         DECODE {
             @Override
             String apply(Cipher cipher, String text) {
                 return cipher.decode(text);
             }
         };
 
         abstract String apply(Cipher cipher, String text);
     }
 
 }
