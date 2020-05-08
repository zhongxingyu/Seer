 /**
  * Copyright 2012 Rob Mayhew
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.robmayhew.qds;
 
 import java.io.File;
 
 /**
  * Convenience methods for quickly loading and saving data to the store
  */
 public class QDS
 {
     private static QuickDataStoreInterface instance;
 
     /**
      * Set the path to save data
      *
      * @param filePath Path of the file to save data in
      */
     public static void usePath(String filePath)
     {
         if (instance == null)
         {
             instance = new QuickDataStore(filePath);
         } else
         {
             throw new QDSException("QuickDataStore has already been " +
                     "setup to use the" + instance.getFilePath() + " path");
         }
     }
 
     /**
      * Set the path to userhome and a given file name
      * @param fileName
      */
     public static void useFileAtUserHome(String fileName)
     {
         if (instance == null)
         {
             instance = new QuickDataStore(getUserHome() + fileName);
         } else
         {
             throw new QDSException("QuickDataStore has already been " +
                     "setup to use the" + instance.getFilePath() + " path");
         }
     }
 
     /**
      * <p>Save an object to the store.</p>
      * <p>Note: the object must be made up of java primitives</p>
      * Can be a:
      * <ul>
      * <li>String,boolean,int,long,double</li>
      * <li>A simple java object containing only String, boolean, int,
      * long, and double primitives</li>
      * <li>A <code>java.util.List</code> containing one of the above</li>
      * </ul>
      * <p/>
      * <p></p> <b>Note:</b>If the store has not already been setup by
      * <code>usePath</code> or <code>useInstance</code> a file location
      * will be chosen for you based on the name of the calling class.
      * File will be stored in <code>System.getProperty("user.home")</code></p>
      *
      * @param key   Key to save the object
      * @param value The value to be saved, will overwrite existing if present
      */
     public static void save(String key, Object value)
     {
         if (instance == null)
         {
             instance = new QuickDataStore(choosePath());
         }
         instance.save(key, value);
     }
 
     /**
      * Load an object from the store
      *
      * @param key key the object was saved under
      * @return The object stored or null
      */
     public static Object load(String key)
     {
         if (instance == null)
        {
            instance = new QuickDataStore(choosePath());
        }
         return instance.load(key);
     }
 
     /**
      * Force these convenience methods to use a custom store.
      * Useful for testing or replacing QuickDataStore
      *
      * @param qds
      */
     public static void useInstance(QuickDataStoreInterface qds)
     {
         instance = qds;
     }
 
 
     /**
      * Force the store to use a specific path
      *
      * @param filePath
      */
     public static void forceUsePath(String filePath)
     {
         instance = new QuickDataStore(filePath);
     }
 
 
     private static String choosePath()
     {
         StackTraceElement[] stackTraceElements = Thread.currentThread()
                 .getStackTrace();
        String className = null;
        for(StackTraceElement stackTraceElement : stackTraceElements)
        {
            String s = stackTraceElement.getClassName();
            if(s.startsWith("java.lang") || s.startsWith("com.robmayhew.qds"))
            {
                continue;
            }
            className = s;
            break;
        }
 
         StringBuilder sb = new StringBuilder();
         for (char c : className.toCharArray())
         {
             if (Character.isLetter(c))
             {
                 sb.append(c);
             }
         }
 
         String userHome = getUserHome();
         String path = userHome + sb.toString() + ".qds";
         return path;
     }
 
     private static String getUserHome()
     {
         String userHome = System.getProperty("user.home");
         if (!userHome.endsWith(File.separator))
         {
             userHome += File.separator;
         }
         return userHome;
     }
 
     public static String filePath()
     {
         if (instance == null)
             return null;
         return instance.getFilePath();
 
     }
 }
