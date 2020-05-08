 package com.atlassian.plugin.test;
 
 import org.apache.commons.io.IOUtils;
 import org.codehaus.janino.ByteArrayClassLoader;
 import org.codehaus.janino.SimpleCompiler;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.zip.ZipOutputStream;
 import java.util.zip.ZipEntry;
 import java.io.*;
 import java.lang.reflect.Field;
 
 /**
  * Builds a plugin jar, including optionally compiling simple Java code
  */
 public class PluginBuilder {
     private final Map/*<String,byte[]>*/ jarContents;
     private final String name;
     private ClassLoader classLoader;
 
     /**
      * Creates the builder
      * @param name The plugin name
      */
     public PluginBuilder(String name) {
         jarContents = new HashMap();
         this.name = name;
         classLoader = getClass().getClassLoader();
     }
 
     /**
      * Adds a Java class in source form.  Will compile the source code.
      * @param className The class name
      * @param code The code to compile
      * @return The builder
      * @throws Exception
      */
     public PluginBuilder addJava(String className, String code) throws Exception
     {
         SimpleCompiler compiler = new SimpleCompiler();
         compiler.setParentClassLoader(classLoader);
         compiler.cook(new StringReader(code));
         classLoader = compiler.getClassLoader();
 
         // Silly hack because I'm too lazy to do it the "proper" janino way
         ByteArrayClassLoader cl = (ByteArrayClassLoader) compiler.getClassLoader();
         Map classes = null;
         final Field fields[] = cl.getClass().getDeclaredFields();
         for (int i = 0; i < fields.length; ++i) {
             if ("classes".equals(fields[i].getName())) {
                 fields[i].setAccessible(true);
                 classes = (Map) fields[i].get(cl);
             }
         }
 
         jarContents.put(className.replace('.',File.separatorChar)+".class", classes.get(className));
         return this;
     }
 
     /**
      * Adds a resource in the jar from a string
      * @param path The path for the jar entry
      * @param contents The contents of the file to create
      * @return
      */
     public PluginBuilder addResource(String path, String contents)
     {
         jarContents.put(path, contents.getBytes());
         return this;
     }
 
     public PluginBuilder addPluginInformation(String key, String name, String version)
     {
        StringBuffer sb = new StringBuffer();
         sb.append("<atlassian-plugin name=\"").append(name).append("\" key=\"").append(key).append("\" pluginsVersion=\"2\">\n");
         sb.append("    <plugin-info>\n");
         sb.append("        <description>This plugin descriptor is used for testing plugins!</description>\n");
         sb.append("        <version>").append(version).append("</version>\n");
         sb.append("        <vendor name=\"Atlassian Software Systems Pty Ltd\" url=\"http://www.atlassian.com\" />\n");
         sb.append("    </plugin-info>");
         sb.append("</atlassian-plugin>");
         jarContents.put("atlassian-plugin.xml", sb.toString().getBytes());
         return this;
     }
 
     /**
      * Adds a file to the jar
      * @param path The path for the entry
      * @param file The file to add
      * @return
      * @throws IOException
      */
     public PluginBuilder addFile(String path, File file) throws IOException {
         jarContents.put(path, IOUtils.toByteArray(new FileInputStream(file)));
         return this;
     }
 
     /**
      * Builds a jar file from the provided information.  The file name is not guarenteed to match the jar name, as it is
      * created as a temporary file.
      * @return The created jar plugin
      * @throws IOException
      */
     public File build() throws IOException {
 
         // Ensure there is a manifest
         if (!jarContents.containsKey("META-INF/MANIFEST.MF"))
         {
             jarContents.put("META-INF/MANIFEST.MF", "Manifest-Version: 1.0".getBytes());
         }
 
         File jarFile = File.createTempFile(name, ".jar");
         ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(jarFile));
         for (Iterator i = jarContents.entrySet().iterator(); i.hasNext(); )
         {
             Map.Entry entry = (Map.Entry) i.next();
             zout.putNextEntry(new ZipEntry((String) entry.getKey()));
             zout.write((byte[]) entry.getValue());
         }
         zout.close();
         return jarFile;
     }
 
 }
