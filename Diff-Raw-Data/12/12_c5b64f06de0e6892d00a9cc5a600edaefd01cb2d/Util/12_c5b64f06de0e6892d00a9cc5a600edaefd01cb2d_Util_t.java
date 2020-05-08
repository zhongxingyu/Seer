 /*
  * Copyright 2007-2009 Alexander Fabisch
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.ev.util;
 
 import java.io.*;
 import java.util.Properties;
 
 /**
  * This utility class is used to avoid duplicate code.
  *
  * @author <a href="mailto:afabisch@tzi.de">Alexander Fabisch</a>
  * @since 0.8.2
  */
 public final class Util {
 
     private Util() { throw new AssertionError("Constructur invokation not allowed!"); }
 
     /**
      * Reads data from a properties file to a Properties object.
      *
      * @param path
      *          Path to the file.
      * @param store
      *          Properties object to store the data.
      * @throws IOException
      *          If the method failes to load the properties, an IOException will be thrown.
      * @throws NullPointerException
      *          If path or store are null, a NullPointerException will be thrown.
      */
     public static void readFile(String path, Properties store) throws IOException {
         checkNullReference(path, store);
         store.load(new FileInputStream(path));
     }
 
     /**
      * Writes data of a Properties object to a properties file. If necessary, new directories will be created.
      *
      * @param path
      *          Path to the file.
      * @param store
      *          Contains the data.
      * @param comment
      *          Comment that should be written in the file. May be null.
      * @throws IOException
      *          If the method failes to save the properties, an IOException will be thrown.
      * @throws NullPointerException
      *          If path or store are null, a NullPointerException will be thrown.
      */
     public static void writeFile(String path, Properties store, String comment) throws IOException {
         checkNullReference(path, store);
         File file = new File(path);
         File topDir = file.getParentFile();
        if (topDir != null && !topDir.exists()) {
             topDir.mkdirs();
         }
         FileOutputStream fos = new FileOutputStream(file);
         store.store(fos, comment);
     }
 
     private static void checkNullReference(Object... objects) {
         for(Object o : objects) {
             if (o == null) {
                 throw new NullPointerException("objects must not be null");
             }
         }
     }
 
     /**
      * Checks whether a directory is accessible. This means: it exists, it is a directory, it is readable and
      * writable.
      *
      * @param directory
      *          The directory to check.
      * @return  Is the directory accessible?
      */
     public static boolean isDirectoryAccessible(File directory) {
         return directory.exists() && directory.isDirectory() && directory.canRead() && directory.canWrite();
     }
 
 }
