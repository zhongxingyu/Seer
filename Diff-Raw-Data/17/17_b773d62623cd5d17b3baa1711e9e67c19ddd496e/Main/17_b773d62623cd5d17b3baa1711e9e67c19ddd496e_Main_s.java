 /*
  * JBoss, Home of Professional Open Source.
  * Copyright (c) 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.beach.jar.digest;
 
 import sun.misc.BASE64Encoder;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.jar.JarEntry;
 import java.util.jar.JarInputStream;
 
 /**
  * Create a digest for a jar ignoring any timestamped bits.
  *
  * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
  */
 public class Main {
     private static BASE64Encoder encoder = new BASE64Encoder();
 
     private static byte[] digest(final String fileName, final boolean showEntries) throws NoSuchAlgorithmException, IOException {
         // TODO: make the algorithm choice configurable
         final MessageDigest jarDigest = MessageDigest.getInstance("SHA1");
         final MessageDigest digest = MessageDigest.getInstance("SHA1");
         final JarInputStream in = new JarInputStream(new BufferedInputStream(new FileInputStream(fileName)));
         try {
             JarEntry entry;
             while ((entry = in.getNextJarEntry()) != null) {
                 // do not hash directories
                 if (entry.isDirectory())
                     continue;
 
                 final String name = entry.getName();
                 // do not hash information added by jarsigner
                 if (name.startsWith("META-INF/")) {
                     if (name.endsWith(".SF") || name.endsWith(".DSA"))
                         continue;
                 }
                 if (name.equals("META-INF/INDEX.LIST"))
                     continue;
 
                 // do not hash timestamped maven artifacts
                 // TODO: make this optional, enabled by default
                 if (name.startsWith("META-INF/maven/") && name.endsWith("/pom.properties"))
                     continue;
 
                 if (showEntries) System.out.println("Name: " + name);
                 digest.reset();
                 final byte[] buf = new byte[4096];
                 int l;
                 while ((l = in.read(buf)) > 0)
                     digest.update(buf, 0, l);
                 final byte[] d = digest.digest();
                 if (showEntries) {
                     System.out.println("SHA1-Digest: " + encoder.encode(d));
                     System.out.println();
                 }
                 jarDigest.update(d);
             }
         } finally {
             in.close();
         }
         return jarDigest.digest();
     }
 
     public static void main(String[] args) throws Exception {
         if (args.length == 0) {
             System.err.println("Usage: Main [-s] <file>...");
             System.exit(1);
         }
         boolean showEntries = false;
 
         for (String fileName : args) {
             // TODO: make sensible options and process them properly
             if (fileName.equals("-s"))
                 showEntries = true;

            // TODO: make output format configurable
            if (showEntries)
                System.out.print("Jar-SHA1-Digest: ");
            else
                System.out.print(fileName + ": ");
            final byte[] digest = digest(fileName, showEntries);
            System.out.println(encoder.encode(digest));
         }
     }
 }
