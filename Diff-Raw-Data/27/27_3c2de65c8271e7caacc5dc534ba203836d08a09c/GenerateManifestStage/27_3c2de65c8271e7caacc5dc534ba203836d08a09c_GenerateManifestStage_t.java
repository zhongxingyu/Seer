 package com.atlassian.plugin.osgi.factory.transform.stage;
 
 import aQute.lib.osgi.Analyzer;
 import aQute.lib.osgi.Builder;
 import aQute.lib.osgi.Jar;
 import com.atlassian.plugin.PluginInformation;
 import com.atlassian.plugin.PluginParseException;
 import com.atlassian.plugin.osgi.factory.transform.PluginTransformationException;
 import com.atlassian.plugin.osgi.factory.transform.TransformContext;
 import com.atlassian.plugin.osgi.factory.transform.TransformStage;
 import com.atlassian.plugin.osgi.factory.transform.model.SystemExports;
 import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
 import com.atlassian.plugin.parsers.XmlDescriptorParser;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Constants;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.*;
 import java.util.jar.JarEntry;
 import java.util.jar.Manifest;
 
 /**
  * Generates an OSGi manifest if not already defined.  Should be the last stage.
  *
  * @since 2.2.0
  */
 public class GenerateManifestStage implements TransformStage
 {
     private static final String SPRING_CONTEXT_DEFAULT = "*;timeout:=60";
     private static final Log log = LogFactory.getLog(GenerateManifestStage.class);
 
     public void execute(TransformContext context) throws PluginTransformationException
     {
         Builder builder = new Builder();
         try
         {
             builder.setJar(context.getPluginFile());
 
             // Possibly necessary due to Spring XML creation
             if (isOsgiBundle(builder.getJar().getManifest()))
             {
                 if (context.getExtraImports().isEmpty())
                 {
                     // skip any manifest manipulation
                     return;
                 }
                 else
                 {
                     assertSpringAvailableIfRequired(context);
                     String imports = addExtraImports(builder.getJar().getManifest().getMainAttributes().getValue(Constants.IMPORT_PACKAGE), context.getExtraImports());
                     builder.setProperty(Constants.IMPORT_PACKAGE, imports);
                     builder.mergeManifest(builder.getJar().getManifest());
                 }
             }
             else
             {
                 // We don't care about the modules, so we pass null to match all
                 XmlDescriptorParser parser = new XmlDescriptorParser(context.getDescriptorDocument(), null);
                 PluginInformation info = parser.getPluginInformation();
 
                 Properties properties = new Properties();
 
                 // Setup defaults
                 properties.put("Spring-Context", SPRING_CONTEXT_DEFAULT);
                 properties.put(Analyzer.BUNDLE_SYMBOLICNAME, parser.getKey());
                 properties.put(Analyzer.IMPORT_PACKAGE, "*;resolution:=optional");
 
                 // Don't export anything by default
                 //properties.put(Analyzer.EXPORT_PACKAGE, "*");
 
                 properties.put(Analyzer.BUNDLE_VERSION, info.getVersion());
 
                 // remove the verbose Include-Resource entry from generated manifest
                 properties.put(Analyzer.REMOVE_HEADERS, Analyzer.INCLUDE_RESOURCE);
 
                 header(properties, Analyzer.BUNDLE_DESCRIPTION, info.getDescription());
                 header(properties, Analyzer.BUNDLE_NAME, parser.getKey());
                 header(properties, Analyzer.BUNDLE_VENDOR, info.getVendorName());
                 header(properties, Analyzer.BUNDLE_DOCURL, info.getVendorUrl());
 
                 // Scan for embedded jars
                 StringBuilder classpath = new StringBuilder();
                 classpath.append(".");
                 for (Enumeration<JarEntry> e = context.getPluginJar().entries(); e.hasMoreElements();)
                 {
                     JarEntry entry = e.nextElement();
                     if (entry.getName().startsWith("META-INF/lib/") && entry.getName().endsWith(".jar"))
                     {
                         classpath.append(",").append(entry.getName());
                     }
                 }
                 header(properties, Analyzer.BUNDLE_CLASSPATH, classpath.toString());
 
                 // Process any bundle instructions in atlassian-plugin.xml
                 properties.putAll(context.getBndInstructions());
 
                 // Add extra imports to the imports list
                 properties.put(Analyzer.IMPORT_PACKAGE, addExtraImports(properties.getProperty(Analyzer.IMPORT_PACKAGE), context.getExtraImports()));
 
                 // Add extra exports to the exports list
                 if (!properties.containsKey(Analyzer.EXPORT_PACKAGE))
                 {
                     properties.put(Analyzer.EXPORT_PACKAGE, addExtraExports(context.getExtraExports()));
                 }
                 builder.setProperties(properties);
             }
 
             builder.calcManifest();
             Jar jar = builder.build();
             Manifest mf = jar.getManifest();
 
             enforceHostVersionsForUnknownImports(mf, context.getSystemExports());
 
             ByteArrayOutputStream bout = new ByteArrayOutputStream();
             mf.write(bout);
             context.getFileOverrides().put("META-INF/MANIFEST.MF", bout.toByteArray());
         }
         catch (Exception t)
         {
             throw new PluginParseException("Unable to process plugin to generate OSGi manifest", t);
         }
     }
 
     /**
      * Scans for any imports with no version specified and locks them into the specific version exported by the host
      * container
      * @param manifest The manifest to read and manipulate
      * @param exports The list of host exports
      */
     private void enforceHostVersionsForUnknownImports(Manifest manifest, SystemExports exports)
     {
         String origImports = manifest.getMainAttributes().getValue(Constants.IMPORT_PACKAGE);
         if (origImports != null)
         {
             StringBuilder imports = new StringBuilder();
             Map<String,Map<String,String>> header = OsgiHeaderUtil.parseHeader(origImports);
             for (Map.Entry<String,Map<String,String>> pkgImport : header.entrySet())
             {
                 String imp = null;
                 if (pkgImport.getValue().isEmpty())
                 {
                     String export = exports.getFullExport(pkgImport.getKey());
                     if (!export.equals(imp))
                     {
                         imp = export;
                     }
 
                 }
                 if (imp == null)
                 {
                     imp = OsgiHeaderUtil.buildHeader(pkgImport.getKey(), pkgImport.getValue());
                 }
                 imports.append(imp);
                 imports.append(",");
             }
             if (imports.length() > 0)
             {
                 imports.deleteCharAt(imports.length() - 1);
             }
 
             manifest.getMainAttributes().putValue(Constants.IMPORT_PACKAGE, imports.toString());
         }
     }
 
     private String addExtraExports(List<String> extraExports)
     {
         StringBuilder result = new StringBuilder();
         for (String exp : extraExports)
         {
             result.append(exp).append(",");
         }
         if (result.length() > 0)
         {
             result.deleteCharAt(result.length() - 1);
         }
 
         return result.toString();
     }
 
     private boolean isOsgiBundle(Manifest manifest) throws IOException
     {
         return manifest.getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME) != null;
     }
 
     private String addExtraImports(String imports, List<String> extraImports)
     {
         StringBuilder referrers = new StringBuilder();
         for (String imp : extraImports)
         {
             referrers.append(imp).append(",");
         }
 
         if (imports != null && imports.length() > 0)
         {
             imports = referrers + imports;
         }
         else
         {
             imports = referrers.toString();
         }
         return imports;
     }
 
     private static void header(Properties properties, String key, Object value)
     {
         if (value == null)
         {
             return;
         }
 
         if (value instanceof Collection && ((Collection) value).isEmpty())
         {
             return;
         }
 
         properties.put(key, value.toString().replaceAll("[\r\n]", ""));
     }
 
     private static void assertSpringAvailableIfRequired(TransformContext context)
     {
         if (context.shouldRequireSpring())
         {
             String header = (String) context.getManifest().getMainAttributes().get("Spring-Context");
             if (header == null)
             {
                 log.warn("The Spring Manifest header 'Spring-Context' is missing.  Please add it and " +
                         "set it to '" + SPRING_CONTEXT_DEFAULT + "'");
             }
             else if (!header.contains(";timeout:=60"))
             {
                 log.warn("The Spring Manifest header isn't set for a 60 second timeout waiting for " +
                         " dependencies.  Please add ';timeout:=60'");
             }
         }
     }
 
 }
