 package com.atlassian.plugin.test;
 
 import org.apache.commons.io.IOUtils;
 import org.codehaus.janino.ByteArrayClassLoader;
 import org.codehaus.janino.SimpleCompiler;
 import org.codehaus.janino.Parser;
 import org.codehaus.janino.CompileException;
 
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
 public class PluginJarBuilder
 {
     private final Map<String, byte[]> jarContents;
     private final String name;
     private ClassLoader classLoader;
 
     /**
      * Creates the builder
      */
     public PluginJarBuilder()
     {
         this("test");
     }
 
     /**
      * Creates the builder
      *
      * @param name The plugin name
      */
     public PluginJarBuilder(String name)
     {
         this(name, PluginJarBuilder.class.getClassLoader());
     }
 
     /**
      * Creates the builder
      *
      * @param name The plugin name
      */
     public PluginJarBuilder(String name, ClassLoader classLoader)
     {
         jarContents = new HashMap<String, byte[]>();
         this.name = name;
         this.classLoader = classLoader;
     }
 
     public PluginJarBuilder addFormattedJava(String className, String... lines) throws Exception
     {
         StringBuilder sb = new StringBuilder();
         for (String line : lines)
         {
             sb.append(line.replace('\'', '"')).append('\n');
         }
         return addJava(className, sb.toString());
     }
 
     /**
      * Adds a Java class in source form.  Will compile the source code.
      *
      * @param className The class name
      * @param code      The code to compile
      * @return The builder
      * @throws Exception
      */
     public PluginJarBuilder addJava(String className, String code) throws Exception
     {
         SimpleCompiler compiler = new SimpleCompiler();
         compiler.setParentClassLoader(classLoader);
         try
         {
             compiler.cook(new StringReader(code));
         }
         catch (Parser.ParseException ex)
         {
             throw new IllegalArgumentException("Unable to compile " + className, ex);
         }
         catch (CompileException ex)
         {
             throw new IllegalArgumentException("Unable to compile " + className, ex);
         }
         classLoader = compiler.getClassLoader();
 
         // Silly hack because I'm too lazy to do it the "proper" janino way
         ByteArrayClassLoader cl = (ByteArrayClassLoader) compiler.getClassLoader();
         Field field = cl.getClass().getDeclaredField("classes");
         field.setAccessible(true);
         Map classes = (Map) field.get(cl);
 
        // jar files use '/' as a directory separator, regardless of platform
        jarContents.put(className.replace('.', '/') + ".class", (byte[]) classes.get(className));
         return this;
     }
 
     /**
      * Adds a resource in the jar from a string
      *
      * @param path     The path for the jar entry
      * @param contents The contents of the file to create
      * @return
      */
     public PluginJarBuilder addResource(String path, String contents)
     {
         jarContents.put(path, contents.getBytes());
         return this;
     }
 
     /**
      * Adds a resource in the jar as lines.  Single quotes are converted to double quotes.
      *
      * @param path  The path for the jar entry
      * @param lines The contents of the file to create
      * @return
      */
     public PluginJarBuilder addFormattedResource(String path, String... lines)
     {
         StringBuilder sb = new StringBuilder();
         for (String line : lines)
             sb.append(line.replace('\'', '"')).append('\n');
         jarContents.put(path, sb.toString().getBytes());
         return this;
     }
 
     public PluginJarBuilder addPluginInformation(String key, String name, String version)
     {
         return addPluginInformation(key, name, version, 2);
     }
 
     public PluginJarBuilder addPluginInformation(String key, String name, String version, int pluginsVersion)
     {
         return addPluginInformation(key, name, version, pluginsVersion, null);
     }
 
     public PluginJarBuilder addPluginInformation(String key, String name, String version, int pluginsVersion, Map<String, String> params)
     {
         StringBuffer sb = new StringBuffer();
         sb.append("<atlassian-plugin name=\"").append(name).append("\" key=\"").append(key).append("\" pluginsVersion=\"" + pluginsVersion + "\">\n");
         sb.append("    <plugin-info>\n");
         sb.append("        <description>This plugin descriptor is used for testing plugins!</description>\n");
         sb.append("        <version>").append(version).append("</version>\n");
         sb.append("        <vendor name=\"Atlassian Software Systems Pty Ltd\" url=\"http://www.atlassian.com\" />\n");
         if (params != null)
             for (Map.Entry<String, String> param : params.entrySet())
                 sb.append("<param name=\"").append(param.getKey()).append("\">").append(param.getValue()).append("</param>\n");
         sb.append("    </plugin-info>");
         sb.append("</atlassian-plugin>");
         jarContents.put("atlassian-plugin.xml", sb.toString().getBytes());
         return this;
     }
 
     /**
      * Adds a file to the jar
      *
      * @param path The path for the entry
      * @param file The file to add
      * @return
      * @throws IOException
      */
     public PluginJarBuilder addFile(String path, File file) throws IOException
     {
         jarContents.put(path, IOUtils.toByteArray(new FileInputStream(file)));
         return this;
     }
 
     /**
      * Builds a jar file from the provided information.  The file name is not guarenteed to match the jar name, as it is
      * created as a temporary file.
      *
      * @return The created jar plugin
      * @throws IOException
      */
     public File build() throws IOException
     {
         File baseDir = new File("target");
         if (!baseDir.exists())
             baseDir = new File(System.getProperty("java.io.tmpdir"));
         else
         {
             baseDir = new File(baseDir, "tmp");
             if (!baseDir.exists())
                 baseDir.mkdir();
         }
         return build(baseDir);
     }
 
     /**
      * Builds a jar file from the provided information.  The file name is not guarenteed to match the jar name, as it is
      * created as a temporary file.
      *
      * @param baseDir The base directory for generated plugin files
      * @return The created jar plugin
      * @throws IOException
      */
     public File build(File baseDir) throws IOException
     {
 
         // Ensure there is a manifest
         if (!jarContents.containsKey("META-INF/MANIFEST.MF"))
         {
             jarContents.put("META-INF/MANIFEST.MF", "Manifest-Version: 1.0".getBytes());
         }
 
         File jarFile = File.createTempFile(name, ".jar", baseDir);
         ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(jarFile));
         for (Iterator i = jarContents.entrySet().iterator(); i.hasNext();)
         {
             Map.Entry entry = (Map.Entry) i.next();
             zout.putNextEntry(new ZipEntry((String) entry.getKey()));
             zout.write((byte[]) entry.getValue());
         }
         zout.close();
         return jarFile;
     }
 
     public ClassLoader getClassLoader()
     {
         return classLoader;
     }
 
 }
