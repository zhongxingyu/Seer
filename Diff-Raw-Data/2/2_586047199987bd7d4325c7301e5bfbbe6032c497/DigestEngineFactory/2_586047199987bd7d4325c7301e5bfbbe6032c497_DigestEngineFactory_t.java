 /*
     Copyright (c) 2000-2013 Alessandro Coppo
     All rights reserved.
 
     Redistribution and use in source and binary forms, with or without
     modification, are permitted provided that the following conditions
     are met:
     1. Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
     2. Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
     3. The name of the author may not be used to endorse or promote products
        derived from this software without specific prior written permission.
 
     THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
     IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
     OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
     IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
     INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
     NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
     DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
     THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
     THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package net.sf.jautl.md;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  */
 public class DigestEngineFactory {
     private static final String packagePrefix = DigestEngineFactory.class.getPackage().getName() + ".";
     
     public static DigestEngine create(String name) {
         Class<?> clazz;
         
         try {
             clazz = Class.forName(packagePrefix + name);
         } catch (ClassNotFoundException ex) {
             return null;
         }
 
         try {
             return (DigestEngine)clazz.newInstance();
         } catch (InstantiationException ex) {
             return null;
         } catch (IllegalAccessException ex) {
             return null;
         }
     }
     
     public static List<String> enumerate() {
         ArrayList<String> names = new ArrayList<String>();
         
         //Enumerating all the classes of package is not an easy task in Java.
         //Adding the required code or adding a dependency to an appropriate
         //library is unjustifiable overkill for what we need here.
         names.add("Adler32");
         names.add("Goulburn");
        names.add("HMAC2104");
         names.add("MD2");
         names.add("MD5");
         names.add("Murmur2A");
         names.add("Murmur3_32");
         names.add("RIPEMD128");
         names.add("RIPEMD160");
         names.add("SHA1");
         names.add("SHA2_256");
         names.add("SHA2_384");
         names.add("SHA2_512");
         names.add("SipHash_2_4");
 
         return names;
     }
 }
