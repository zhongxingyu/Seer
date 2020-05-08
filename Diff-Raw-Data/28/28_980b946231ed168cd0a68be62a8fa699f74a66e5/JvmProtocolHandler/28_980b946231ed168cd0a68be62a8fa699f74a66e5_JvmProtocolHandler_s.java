 package com.github.signed.protocols.jvm;
 
 import java.net.URL;
 
 public class JvmProtocolHandler {
 
     public static void registerJvmProtocolHandler(MemoryDictionary dictionary) {
        URL.setURLStreamHandlerFactory(new JcmStreamHandlerFactory(dictionary));
     }
 }
