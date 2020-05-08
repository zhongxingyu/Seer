 /*
  * Maven Packaging Plugin,
  * Maven plugin to package a Project (deb and izpack)
  * Copyright (C) 2000-2007 tarent GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License,version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  *
  * tarent GmbH., hereby disclaims all copyright
  * interest in the program 'Maven Packaging Plugin'
  * Signature of Elmar Geese, 14 June 2007
  * Elmar Geese, CEO tarent GmbH.
  */
 
 package de.tarent.maven.plugins.pkg;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import org.apache.maven.artifact.Artifact;
 
 /**
  * This class provides means to translate between Maven artifactIds and
  * Debian package names and the jar file names in that system.
  * 
  * @author Robert Schuster (robert.schuster@tarent.de)
  *
  */
 class DebianPackageMap
 {
   /**
    * Mandatory package dependencies for every Debian Java package.
    */
   private static String defaults = "java-gcj-compat | java2-runtime";
   
  private static String ipkDefaults = "java2-runtime";
   
   private static Map mapping;
   
   private static final String IGNORE_MARKER = "[IGNORE]";
   private static final String BUNDLE_MARKER = "[BUNDLE]";
   
   private static final Entry IGNORE_ENTRY = new Entry(null, null, null, null);
   private static final Entry BUNDLE_ENTRY = new Entry(null, null, null, null);
 
   /** Iterates over all the dependencies' artifacts and calls
    * <code>visit()</code> for each entry.
    * 
    * <p>Dependencies which have been marked to be ignored won't
    * cause a <code>visit</code> call!</p>
    * 
    * <p>Dependencies which have been marked to be bundled with the
    * project will be called with a <code>null</code> Entry instance!</p>
    * 
    * @param deps
    * @param v
    */
   static void iterateDependencyArtifacts(Collection deps, Visitor v)
   {
     initMapping();
 
    for (Iterator ite = deps.iterator(); ite.hasNext(); )
      {
        Artifact a = (Artifact) ite.next();
        
        // HACK: Dependencies are by definition libraries. As such
        // dependency artifacts which have no special naming configuration
        // will be named "lib" + <artifactId> + "-java" (which is a convention
        // followed by Debian.
        Entry e = getEntry(a.getArtifactId(), "libs");
        if (e == BUNDLE_ENTRY)
     	   v.bundle(a);
        else if (e != IGNORE_ENTRY)
          v.visit(a, e);
      }
      
   }
   
   static Entry getEntry(String artifactId, String debianSection)
   {
     initMapping();
 	   
     Entry e = (Entry) mapping.get(artifactId);
     
     // If an entry does not exist create one based on the artifact id.
     if (e == null)
       {
     	e = new Entry(artifactId, debianise(artifactId, debianSection));
       }
     
     return e;
   }
   
   private static void initMapping()
   {
 	  if (mapping != null)
 		  return;
 
 	  mapping = new HashMap();
       
       readMappingFromProperties(mapping, "tarent.properties");
       readMappingFromProperties(mapping, "3rdparty.properties");
   }
   
   /**
    * Convert the artifactId into a Debian package name. Currently this only
    * applies to libraries which get a "lib" prefix and a "-java" suffix.
    * 
    * @param artifactId
    * @return
    */
   static String debianise(String artifactId, String debianSection)
   {
     return debianSection.equals("libs") ? "lib" + artifactId + "-java"
                                        : artifactId;
   }
   
   static String getDefaults()
   {
     return defaults;
   }
   
   static String getIpkDefaults()
   {
     return ipkDefaults;
   }
   
   static class Entry
   {
     final static String DEFAULT_LOCATION = "/usr/share/java";
     
     String location;
     
     String artifactId;
     
     String packageName;
     
     String[] jarNames;
     
     /** A pattern which can split the line of jar (= whitespace separated
      * list, eg. "  foo.jar baz.jar " ).
      */ 
     static Pattern p = Pattern.compile("\\s+");
     
     /**
      * Constructs an entry by evaluating the String array. The more it contains
      * the more information is explicitly given.
      * 
      * @param artifactId
      * @param values
      */
     Entry(String artifactId, String[] values)
     {
       this.artifactId = artifactId;
       
       switch(values.length)
       {
         default:
           // Superfluous arguments are silently discarded
         case 3:
           packageName = values[0];
           jarNames = p.split(values[1].trim());
           location = values[2];
           break;
         case 2:
           packageName = values[0];
           jarNames = p.split(values[1].trim());
           location = DEFAULT_LOCATION;
           break;
         case 1:
           // values[0] is empty the corresponding entry in the properties file was empty, too.
           packageName = (values[0].length() > 0) ? values[0] : artifactId;
           jarNames = new String[] { artifactId + ".jar" };
           location = DEFAULT_LOCATION;
           break;
       }
         
     }
 
     /** An entry living in the default location /usr/share/java
      * and where the artifactId is equal to the Debian package name
      * and the jar name can be created by adding ".jar" to the artifactId.
      * 
      * @param artifactId
      */
     Entry(String artifactId)
     {
      this(artifactId, artifactId); 
     }
 
     /** An entry living in the default location /usr/share/java
      * and where the artifactId may be different to the Debian package name
      * and the jar name can be created by adding ".jar" to the artifactId.
      * 
      * @param artifactId
      */
     Entry(String artifactId, String packageName)
     {
      this(artifactId, packageName, artifactId + ".jar"); 
     }
     
     /** An entry living in the default location /usr/share/java
      * and where the artifactId, the Debian package name and the
      * jar name may be different.
      */
     Entry(String artifactId, String packageName, String jarName)
     {
      this(artifactId, packageName, jarName, DEFAULT_LOCATION); 
     }
     
     Entry(String artifactId, String packageName, String jarName, String location)
     {
       this.artifactId = artifactId;
       this.packageName = packageName;
       this.jarNames = new String[] { jarName };
       this.location = location;
     }
     
   }
   
   /**
    * Reads the mapping from a properties file which is part of the classpath
    * and stores it in the first argument.
    * 
    * @param mapping
    * @param properties
    */
   private static void readMappingFromProperties(Map mapping, String properties)
   {
     Properties props = new Properties();
     try
     {
       props.load(DebianPackageMap.class.getResourceAsStream(properties));
     }
     catch (IOException ioe)
     {
       // Very unlikely ...
       throw new IllegalStateException("IOException while fetching properties.", ioe);
     }
     // A pattern which splits at commas (and can cope with whitespace).
     Pattern p = Pattern.compile("\\s*,\\s*");
     
     for (Iterator ite = props.keySet().iterator(); ite.hasNext(); )
       {
         String key = (String) ite.next();
         String value = (String) props.get(key);
         
         if (value.equals(IGNORE_MARKER))
           // Put a special Entry instance in which can be recognized when the
           // mapping is iterated over.
           mapping.put(key, IGNORE_ENTRY);
         else if (value.equals(BUNDLE_MARKER))
           // Put a special Entry instance in which can be recognized when the
           // mapping is iterated over and treated in a special way.
           mapping.put(key, BUNDLE_ENTRY);
         else
           mapping.put(key, new Entry(key, p.split(value)));
       }
     
   }
   
 }
