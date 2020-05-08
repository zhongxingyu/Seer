 package com.alexshabanov.mm4j;
 
 import com.alexshabanov.mm4j.av.AVNativeBridge;
 
 import java.util.Arrays;
 
 /**
  * Entry point.
  */
 public final class App {
     public static void main(String[] args) {
        System.out.println("Native API invoke sample #2");

        AVNativeBridge.loadDependencies();
 
         final byte[] context = AVNativeBridge.initInputFileContext("deus");
         for (final byte b : context) {
             System.out.println("b = " + Integer.toHexString(b & 0x000000ff));
         }
 
        // this line crushes the application
        //AVNativeBridge.disposeFileContext(context);
 
         System.out.println("EOF native api");
     }
 }
