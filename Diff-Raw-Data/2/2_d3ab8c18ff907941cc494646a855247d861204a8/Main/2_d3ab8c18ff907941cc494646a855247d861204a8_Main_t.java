 package org.codehaus.xsite;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.codehaus.xsite.factories.DefaultXSiteFactory;
 
 /**
  * Command line entry point for building XSite.
  * 
  * @author Joe Walnes
  * @author J&ouml;rg Schaible
  * @author Mauro Talevi
  */
 public class Main {
 
     private static final String DEFAULT_XSITE_FACTORY = "org.codehaus.xsite.factories.NanoXSiteFactory";
 
     private static final String XSITE_COMPOSITION = "xsite.xml";
 
     private static final String XSITE_PROPERTIES = "xsite.properties";
 
     private static final String CLASSNAME = Main.class.getName();
 
     private static final char HELP_OPT = 'h';
 
     private static final char VERSION_OPT = 'v';
 
     private static final char SITEMAP_OPT = 'm';
 
     private static final char SKIN_OPT = 's';
 
     private static final char RESOURCES_OPT = 'R';
 
     private static final char FILE_OPT = 'f';
 
     private static final char RESOURCE_OPT = 'r';
 
     private static final char XSITE_FACTORY_OPT = 'x';
 
     private static final char SOURCE_OPT = 'S';
 
     private static final char OUTPUT_OPT = 'o';
 
     public static final void main(String[] args) throws Exception {
         new Main(args);
     }
 
     public Main(String[] args) throws Exception {
         Options options = createOptions();
         CommandLine cl = null;
         try {
             cl = getCommandLine(args, options);
         } catch (ParseException e) {
             throw new RuntimeException("Failed to parse arguments: ", e);
         }
 
         if (cl.hasOption(HELP_OPT)) {
             printUsage(options);
         } else if (cl.hasOption(VERSION_OPT)) {
             Properties properties = createProperties();
             printVersion(properties);
         } else {
             if (!validateOptions(cl)) {
                 printUsage(options);
                 throw new RuntimeException("Invalid arguments "
                         + cl.getArgList());
             }
             try {
                 XSite xsite = instantiateXSite(cl);
                 String sourcePath = cl.getOptionValue(SOURCE_OPT);
                 File sitemap = new File(sourcePath+File.separator+cl.getOptionValue(SITEMAP_OPT));
                File skin = new File(sourcePath+File.separator+cl.getOptionValue(SKIN_OPT));
                 File output = new File(cl.getOptionValue(OUTPUT_OPT));
                 xsite.build(sitemap, skin, getResourceDirs(cl), output);
 
             } catch (Exception e) {
                 throw new RuntimeException("Failed to build XSite", e);
             }
         }
     }
 
     private File[] getResourceDirs(CommandLine cl) {
         if ( !cl.hasOption(RESOURCES_OPT)){
             return new File[]{};
         }
         String sourcePath = cl.getOptionValue(SOURCE_OPT);
         String[] resourcePaths = cl.getOptionValue(RESOURCES_OPT).split(",");
         File[] resourceDirs = new File[resourcePaths.length];
         for ( int i = 0; i < resourcePaths.length; i++ ){
             resourceDirs[i] = new File(sourcePath+File.separator+resourcePaths[i]);
         }
         return resourceDirs;
     }
 
     private XSite instantiateXSite(CommandLine cl) throws MalformedURLException {
         XSiteFactory factory = instantiateXSiteFactory(cl);
         Map config = new HashMap();
         config.put(URL.class, getCompositionURL(cl));
         return factory.createXSite(config);
     }
 
     private XSiteFactory instantiateXSiteFactory(CommandLine cl) {
         String factoryClassName = DEFAULT_XSITE_FACTORY;
         if (cl.hasOption(XSITE_FACTORY_OPT)) {
             factoryClassName = cl.getOptionValue(XSITE_FACTORY_OPT);
         }        
         try {
             return (XSiteFactory) getClassLoader().loadClass(factoryClassName).newInstance();
         } catch (Exception e) {
             return new DefaultXSiteFactory();
         }
     }
 
     private ClassLoader getClassLoader() {
         return Thread.currentThread().getContextClassLoader();
     }
 
     private boolean validateOptions(CommandLine cl) {
         if (cl.hasOption(SOURCE_OPT) && cl.hasOption(SITEMAP_OPT) && cl.hasOption(SKIN_OPT)
                 && cl.hasOption(OUTPUT_OPT)) {
             return true;
         }
         return false;
     }
 
     static URL getCompositionURL(CommandLine cl) throws MalformedURLException {
         URL url = null;
         Thread.currentThread().setContextClassLoader(
                 Main.class.getClassLoader());
         if (cl.hasOption(FILE_OPT)) {
             File file = new File(cl.getOptionValue(SOURCE_OPT)+File.separator+cl.getOptionValue(FILE_OPT));
             if (file.exists()) {
                 url = file.toURL();
             }
         } else if (cl.hasOption(RESOURCE_OPT)) {
             url = Thread.currentThread().getContextClassLoader().getResource(
                     cl.getOptionValue(RESOURCE_OPT));
         } else {
             url = Main.class.getResource(XSITE_COMPOSITION);
         }
         return url;
     }
 
     static final Properties createProperties() throws IOException {
         Properties properties = new Properties();
         properties.load(Main.class.getResourceAsStream(XSITE_PROPERTIES));
         return properties;
     }
 
     static final Options createOptions() {
         Options options = new Options();
         options.addOption(String.valueOf(HELP_OPT), "help", false,
                 "print this message and exit");
         options.addOption(String.valueOf(VERSION_OPT), "version", false,
                 "print the version information and exit");
         options.addOption(String.valueOf(SOURCE_OPT), "sitemap", true,
                 "specify the source directory");
         options.addOption(String.valueOf(FILE_OPT), "file", true,
                 "specify the composition file - relative to the source directory");
         options.addOption(String.valueOf(RESOURCE_OPT), "resource", true,
                 "specify the composition resource - relative to the classpath");
         options.addOption(String.valueOf(SITEMAP_OPT), "sitemap", true,
                 "specify the sitemap file path - relative to the source directory");
         options.addOption(String.valueOf(SKIN_OPT), "skin", true,
                 "specify the skin file path - relative to the source directory");
         options.addOption(String.valueOf(RESOURCES_OPT), "resources", true,
                 "specify the CSV list of resource paths - relative to the source directory");
         options.addOption(String.valueOf(OUTPUT_OPT), "output", true,
                 "specify the output dir");
         options.addOption(String.valueOf(XSITE_FACTORY_OPT), "xsite-factory", true,
                 "specify the xsite factory name");
         return options;
     }
 
     static CommandLine getCommandLine(String[] args, Options options)
             throws ParseException {
         CommandLineParser parser = new PosixParser();
         return parser.parse(options, args);
     }
 
     private static void printUsage(Options options) {
         final String lineSeparator = System.getProperty("line.separator");
         final StringBuffer usage = new StringBuffer();
         usage.append(lineSeparator);
         usage.append(CLASSNAME
                         + ": -S<source-dir> -m<relative-path-to-sitemap> -s<relative-path-to-skin> -o<output-dir> "
                         + "[-R <csv-of-resource-paths>]"
                         + "[-f<relative-path-to-xsite.xml>|-r<classpath-path-to-xsite.xml>] "
                         + "[-x<xsite-factory-classname>"
                         + "[-h|-v]");
         usage.append(lineSeparator);
         usage.append("Options: " + options.getOptions());
         System.out.println(usage.toString());
     }
 
     private static void printVersion(Properties properties) {
         System.out.println(XSite.class.getName() + " version: "
                 + properties.getProperty("version"));
     }
 
 }
