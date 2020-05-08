 package com.atlassian.plugin.osgi.factory.transform;
 
 import aQute.lib.osgi.Analyzer;
 import aQute.lib.osgi.Builder;
 import aQute.lib.osgi.Jar;
 import com.atlassian.plugin.PluginInformation;
 import com.atlassian.plugin.PluginManager;
 import com.atlassian.plugin.PluginParseException;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentRegistration;
 import com.atlassian.plugin.osgi.hostcomponents.PropertyBuilder;
 import com.atlassian.plugin.osgi.hostcomponents.ContextClassLoaderStrategy;
 import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
 import com.atlassian.plugin.parsers.XmlDescriptorParser;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.commons.lang.Validate;
 import org.dom4j.*;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.SAXReader;
 import org.dom4j.io.XMLWriter;
 import org.osgi.framework.Constants;
 
 import java.io.*;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.*;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 /**
  * Default implementation of plugin transformation that uses BND to generate the manifest and manually creates the
  * spring configuration file.
  */
 public class DefaultPluginTransformer implements PluginTransformer
 {
     // The spring configuration containing exported components and imported host components
     static final String ATLASSIAN_PLUGIN_SPRING_XML = "META-INF/spring/atlassian-plugins-spring.xml";
 
     private static final Logger log = Logger.getLogger(DefaultPluginTransformer.class);
 
     private final List<SpringTransformer> springTransformers = Arrays.asList(
             new ComponentSpringTransformer(),
             new ComponentImportSpringTransformer(),
             new HostComponentSpringTransformer(),
             new ModuleTypeSpringTransformer()
     );
 
     /**
      * Transforms the file into an OSGi bundle
      * @param pluginJar The plugin jar
      * @param regs The list of registered host components
      * @return The new OSGi-enabled plugin jar
      * @throws PluginTransformationException If anything goes wrong
      */
     public File transform(File pluginJar, List<HostComponentRegistration> regs) throws PluginTransformationException
     {
         Validate.notNull(pluginJar, "The plugin jar is required");
         Validate.notNull(regs, "The host component registrations are required");
         final JarFile jar;
         try
         {
             jar = new JarFile(pluginJar);
         }
         catch (IOException e)
         {
             throw new PluginTransformationException("Plugin is not a valid jar file", e);
         }
 
         // List of all files to add/override in the new jar
         Map<String,byte[]> filesToAdd = new HashMap<String, byte[]>();
 
         // Try to generate a manifest if none available or merge with an existing one to add host component imports
         URL atlassianPluginsXmlUrl = null;
         try
         {
             final ClassLoader cl = new URLClassLoader(new URL[]{pluginJar.toURL()}, null);
             atlassianPluginsXmlUrl = cl.getResource(PluginManager.PLUGIN_DESCRIPTOR_FILENAME);
             if (atlassianPluginsXmlUrl == null)
                 throw new IllegalStateException("Cannot find atlassian-plugins.xml in jar");
 
             log.info("Generating the manifest for plugin "+pluginJar.getName());
             filesToAdd.put("META-INF/MANIFEST.MF", generateManifest(atlassianPluginsXmlUrl.openStream(), pluginJar, regs));
         }
         catch (PluginParseException e)
         {
             throw new PluginTransformationException("Unable to generate manifest", e);
         }
         catch (IOException e)
         {
             throw new PluginTransformationException("Unable to read existing plugin jar manifest", e);
         }
 
         // Try to generate the spring config that pulls in host components and exports plugin components
         if (jar.getEntry(ATLASSIAN_PLUGIN_SPRING_XML) == null) {
             try
             {
                 log.info("Generating "+ATLASSIAN_PLUGIN_SPRING_XML + " for plugin "+pluginJar.getName());
                 filesToAdd.put(ATLASSIAN_PLUGIN_SPRING_XML, generateSpringXml(atlassianPluginsXmlUrl.openStream(), regs));
             }
             catch (DocumentException e)
             {
                 throw new PluginTransformationException("Unable to generate host component spring XML", e);
             }
             catch (IOException e)
             {
                 throw new PluginTransformationException("Unable to open atlassian-plugins.xml", e);
             }
         }
 
         // Create a new jar by overriding the specified files
         try
         {
             if (log.isDebugEnabled())
             {
                 StringBuilder sb = new StringBuilder();
                 sb.append("Overriding files in ").append(pluginJar.getName()).append(":\n");
                 for (Map.Entry<String,byte[]> entry : filesToAdd.entrySet())
                 {
                     sb.append("==").append(entry.getKey()).append("==\n");
                     sb.append(new String(entry.getValue()));
                 }
                 log.debug(sb.toString());
             }
             return addFilesToExistingZip(pluginJar, filesToAdd);
         } catch (IOException e)
         {
             throw new PluginTransformationException("Unable to add files to plugin jar");
         }
 
     }
 
     /**
      * Generate the spring xml by processing the atlassian-plugins.xml file
      * @param in The stream of the atlassian-plugins.xml file
      * @param regs The list of registered host components
      * @return The new spring xml in bytes
      * @throws DocumentException If there are any errors processing the atlassian-plugins.xml document
      */
     byte[] generateSpringXml(InputStream in, List<HostComponentRegistration> regs) throws DocumentException
     {
         Document springDoc = DocumentHelper.createDocument();
         Element root = springDoc.addElement("beans");
 
         root.addNamespace("beans", "http://www.springframework.org/schema/beans");
         root.addNamespace("osgi", "http://www.springframework.org/schema/osgi");
         root.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
         root.addAttribute(new QName("schemaLocation", new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance")),
                 "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd\n" +
                 "http://www.springframework.org/schema/osgi http://www.springframework.org/schema/osgi/spring-osgi.xsd");
         root.setName("beans:beans");
         root.addAttribute("default-autowire", "autodetect");
 
         SAXReader reader = new SAXReader();
         Document pluginDoc = reader.read(in);
 
         for (SpringTransformer springTransformer : springTransformers)
         {
             springTransformer.transform(regs, pluginDoc, springDoc);
         }
 
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         OutputFormat format = OutputFormat.createPrettyPrint();
 
         try
         {
             XMLWriter writer = new XMLWriter(bout, format );
             writer.write(springDoc );
         } catch (IOException e)
         {
             throw new PluginTransformationException("Unable to print generated Spring XML", e);
         }
 
         return bout.toByteArray();
     }
 
     /**
      * Generates a new manifest file
      *
      * @param descriptorStream The existing manifest
      * @param file The jar
      * @param regs The list of host component registrations
      * @return The new manifest file in bytes
      * @throws PluginParseException If there is any problems parsing atlassian-plugin.xml
      */
     byte[] generateManifest(InputStream descriptorStream, File file, List<HostComponentRegistration> regs) throws PluginParseException
     {
 
         Builder builder = new Builder();
         try
         {
             builder.setJar(file);
             String referrers = OsgiHeaderUtil.findReferredPackages(regs);
             
             // Possibly necessary due to Spring XML creation
             referrers += "com.atlassian.plugin.osgi.external,com.atlassian.plugin,";
             if (builder.getJar().getManifest().getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME) != null)
             {
                String imports = addReferrersToImports(referrers, builder.getJar().getManifest().getMainAttributes().getValue(Constants.IMPORT_PACKAGE));
                 builder.setProperty(Constants.IMPORT_PACKAGE, imports);
                 builder.mergeManifest(builder.getJar().getManifest());
             } else
             {
                 PluginInformationDescriptorParser parser = new PluginInformationDescriptorParser(descriptorStream);
                 PluginInformation info = parser.getPluginInformation();
 
                 Properties properties = new Properties();
 
                 // Setup defaults
                 properties.put("Spring-Context", "*;timeout=60");
                 properties.put(Analyzer.BUNDLE_SYMBOLICNAME, parser.getKey());
                 properties.put(Analyzer.IMPORT_PACKAGE, "*;resolution:=optional");
                 properties.put(Analyzer.EXPORT_PACKAGE, "*");
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
                 JarFile jarfile = new JarFile(file);
                 for (Enumeration<JarEntry> e = jarfile.entries(); e.hasMoreElements(); )
                 {
                     JarEntry entry = e.nextElement();
                     if (entry.getName().startsWith("META-INF/lib/") && entry.getName().endsWith(".jar"))
                         classpath.append(",").append(entry.getName());
                 }
                 header(properties, Analyzer.BUNDLE_CLASSPATH, classpath.toString());
 
                 // Process any bundle instructions in atlassian-plugin.xml
                 properties.putAll(processBundleInstructions(parser.getDocument()));
 
                 // Add referrers to the imports list
                 properties.put(Analyzer.IMPORT_PACKAGE, addReferrersToImports(properties.getProperty(Analyzer.IMPORT_PACKAGE), referrers));
                 builder.setProperties(properties);
             }
 
             // Not sure if this is the best incantation of bnd, but as I don't have the source, it'll have to do
             builder.calcManifest();
             Jar jar = builder.build();
             ByteArrayOutputStream bout = new ByteArrayOutputStream();
             jar.writeManifest(bout);
             return bout.toByteArray();
 
         } catch (Exception t)
         {
             throw new PluginParseException("Unable to process plugin to generate OSGi manifest", t);
         }
     }
 
     private Map<String,String> processBundleInstructions(Document document)
     {
         Map<String,String> instructions = new HashMap<String,String>();
         Element pluginInfo = document.getRootElement().element("plugin-info");
         if (pluginInfo != null)
         {
             Element instructionRoot = pluginInfo.element("bundle-instructions");
             if (instructionRoot != null)
             {
                 List<Element> instructionsElement = instructionRoot.elements();
                 for (Element instructionElement : instructionsElement)
                 {
                     String name = instructionElement.getName();
                     String value = instructionElement.getTextTrim();
                     instructions.put(name, value);
                 }
             }
         }
         return instructions;
     }
 
     private String addReferrersToImports(String imports, String referrers)
     {
         if (imports != null && imports.length() > 0)
             imports = referrers + imports;
         else
             imports = referrers.substring(0, referrers.length() - 1);
         return imports;
     }
 
     /**
      * Creates a new jar by overriding the specified files in the existing one
      *
      * @param zipFile The existing zip file
      * @param files The files to override
      * @return The new zip
      * @throws IOException If there are any problems processing the streams
      */
     static File addFilesToExistingZip(File zipFile,
 			 Map<String,byte[]> files) throws IOException {
                 // get a temp file
 		File tempFile = File.createTempFile(zipFile.getName(), null);
                 // delete it, otherwise you cannot rename your existing zip to it.
 		byte[] buf = new byte[1024];
 
 		ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
 		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tempFile));
 
 		ZipEntry entry = zin.getNextEntry();
 		while (entry != null)
         {
 			String name = entry.getName();
 			if (!files.containsKey(name))
             {
 				// Add ZIP entry to output stream.
 				out.putNextEntry(new ZipEntry(name));
 				// Transfer bytes from the ZIP file to the output file
 				int len;
 				while ((len = zin.read(buf)) > 0)
 					out.write(buf, 0, len);
 			}
 			entry = zin.getNextEntry();
 		}
 		// Close the streams
 		zin.close();
 		// Compress the files
 		for (Map.Entry<String,byte[]> fentry : files.entrySet())
         {
             InputStream in = new ByteArrayInputStream(fentry.getValue());
 			// Add ZIP entry to output stream.
 			out.putNextEntry(new ZipEntry(fentry.getKey()));
 			// Transfer bytes from the file to the ZIP file
 			int len;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			// Complete the entry
 			out.closeEntry();
 			in.close();
 		}
 		// Complete the ZIP file
 		out.close();
         return tempFile;
     }
 
 
     private static void header(Properties properties, String key, Object value)
     {
         if (value == null)
             return;
 
         if (value instanceof Collection && ((Collection) value).isEmpty())
             return;
 
         properties.put(key, value.toString().replaceAll("[\r\n]", ""));
     }
 
     /**
      * Descriptor parser that exposes the PluginInformation object directly
      */
     private static class PluginInformationDescriptorParser extends XmlDescriptorParser
     {
         /**
          * @throws com.atlassian.plugin.PluginParseException
          *          if there is a problem reading the descriptor from the XML {@link java.io.InputStream}.
          */
         public PluginInformationDescriptorParser(InputStream source) throws PluginParseException
         {
             super(source);
         }
 
         public PluginInformation getPluginInformation()
         {
             return createPluginInformation(getDocument().getRootElement().element("plugin-info"));
         }
 
         @Override
         public Document getDocument()
         {
             return super.getDocument();
         }
     }
 }
