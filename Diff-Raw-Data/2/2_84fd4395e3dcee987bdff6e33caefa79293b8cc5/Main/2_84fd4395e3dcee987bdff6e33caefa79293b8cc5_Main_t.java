 package org.codehaus.xsite;
 
 import static java.util.Arrays.asList;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
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
 
 	private static final String DEFAULT_XSITE_FACTORY = "org.codehaus.xsite.factories.PicoXSiteFactory";
 
 	private static final String XSITE_COMPOSITION = "xsite.xml";
 
 	private static final char HELP_OPT = 'h';
 
 	private static final char VERSION_OPT = 'v';
 
 	private static final char SITEMAP_OPT = 'm';
 
 	private static final char SKIN_OPT = 's';
 
 	private static final char RESOURCES_OPT = 'R';
 
 	private static final char TEMPLATES_OPT = 'T';
 
 	private static final char FILE_OPT = 'f';
 
 	private static final char RESOURCE_OPT = 'r';
 
 	private static final char XSITE_FACTORY_OPT = 'x';
 
 	private static final char SOURCE_OPT = 'S';
 
 	private static final char OUTPUT_OPT = 'o';
 
 	private static final char L10N_OPT = 'L';
 
 	private static final char PUBLISHED_DATE_OPT = 'D';
 
 	private static final char PUBLISHED_VERSION_OPT = 'V';
 
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
 			printVersion();
 		} else {
 			if (!validateOptions(cl)) {
 				printUsage(options);
 				throw new RuntimeException("Invalid arguments "
 						+ cl.getArgList());
 			}
 			File sitemap = null;
 			File skin = null;
			File[] resources = new File[0];
 			File output = null;
 			Map<String, Object> customProperties = null;
 			try {
 				XSite xsite = instantiateXSite(cl);
 				sitemap = getSitemap(cl, null);
 				skin = getSkin(cl, null);
 				resources = getResourceDirs(cl, null);
 				output = getOutput(cl, null);
 				customProperties = getCustomProperties(cl, null);
 				xsite.build(sitemap, skin, resources, output, customProperties);
 				if (cl.hasOption(L10N_OPT)) {
 					String[] languages = cl.getOptionValue(L10N_OPT).split(",");
 					for (int l = 0; l < languages.length; l++) {
 						String language = languages[l];
 						xsite.build(getSitemap(cl, language), getSkin(cl,
 								language), getResourceDirs(cl, language),
 								getOutput(cl, language), customProperties);
 					}
 				}
 			} catch (Exception e) {
 				throw new RuntimeException(
 						"Failed to build site with sitemap '" + sitemap
 								+ "', skin '" + skin + "', resources '"
 								+ asList(resources) + "', output '" + output
 								+ "'", e);
 			}
 		}
 	}
 
 	private File getSitemap(CommandLine cl, String language) {
 		return getFile(cl.getOptionValue(SOURCE_OPT), language, cl
 				.getOptionValue(SITEMAP_OPT));
 	}
 
 	private File getSkin(CommandLine cl, String language) {
 		String sourcePath = cl.getOptionValue(SOURCE_OPT);
 		if (cl.hasOption(TEMPLATES_OPT)) {
 			sourcePath = cl.getOptionValue(TEMPLATES_OPT);
 		}
 		return getFile(sourcePath, language, cl.getOptionValue(SKIN_OPT));
 	}
 
 	private File getOutput(CommandLine cl, String language) {
 		if (language != null) {
 			return new File(cl.getOptionValue(OUTPUT_OPT) + File.separator
 					+ language);
 		}
 		return new File(cl.getOptionValue(OUTPUT_OPT));
 	}
 
 	private File[] getResourceDirs(CommandLine cl, String language) {
 		if (!cl.hasOption(RESOURCES_OPT)) {
 			return new File[] {};
 		}
 		String sourcePath = cl.getOptionValue(SOURCE_OPT);
 		String[] resourcePaths = cl.getOptionValue(RESOURCES_OPT).split(",");
 		File[] resourceDirs = new File[resourcePaths.length];
 		for (int i = 0; i < resourcePaths.length; i++) {
 			resourceDirs[i] = getFile(sourcePath, language, resourcePaths[i]);
 		}
 		return resourceDirs;
 	}
 
 	private Map<String, Object> getCustomProperties(CommandLine cl,
 			Object object) {
 		Map<String, Object> properties = new HashMap<String, Object>();
 		addProperty(cl, properties, "publishedDate", PUBLISHED_DATE_OPT,
 				new Date());
 		addProperty(cl, properties, "publishedVersion", PUBLISHED_VERSION_OPT,
 				"N/A");
 		return properties;
 	}
 
 	private void addProperty(CommandLine cl, Map<String, Object> properties,
 			String key, char opt, Object defaultValue) {
 		if (cl.hasOption(opt)) {
 			properties.put(key, cl.getOptionValue(opt));
 		} else {
 			properties.put(key, defaultValue);
 		}
 	}
 
 	private File getFile(String sourcePath, String language, String relativePath) {
 		if (language != null) {
 			String languagePath = sourcePath + File.separator + language
 					+ File.separator + relativePath;
 			if (new File(languagePath).exists()) {
 				return new File(languagePath);
 			}
 		}
 		return new File(sourcePath + File.separator + relativePath);
 	}
 
 	private XSite instantiateXSite(CommandLine cl) throws MalformedURLException {
 		XSiteFactory factory = instantiateXSiteFactory(cl);
 		Map<Class<?>, URL> config = new HashMap<Class<?>, URL>();
 		config.put(URL.class, getCompositionURL(cl));
 		return factory.createXSite(config);
 	}
 
 	private XSiteFactory instantiateXSiteFactory(CommandLine cl) {
 		String factoryClassName = DEFAULT_XSITE_FACTORY;
 		if (cl.hasOption(XSITE_FACTORY_OPT)) {
 			factoryClassName = cl.getOptionValue(XSITE_FACTORY_OPT);
 		}
 		try {
 			return (XSiteFactory) getClassLoader().loadClass(factoryClassName)
 					.newInstance();
 		} catch (Exception e) {
 			return new DefaultXSiteFactory();
 		}
 	}
 
 	private ClassLoader getClassLoader() {
 		return Thread.currentThread().getContextClassLoader();
 	}
 
 	private boolean validateOptions(CommandLine cl) {
 		if (cl.hasOption(SOURCE_OPT) && cl.hasOption(SITEMAP_OPT)
 				&& cl.hasOption(SKIN_OPT) && cl.hasOption(OUTPUT_OPT)) {
 			return true;
 		}
 		return false;
 	}
 
 	static URL getCompositionURL(CommandLine cl) throws MalformedURLException {
 		URL url = null;
 		if (cl.hasOption(FILE_OPT)) {
 			File file = new File(cl.getOptionValue(SOURCE_OPT) + File.separator
 					+ cl.getOptionValue(FILE_OPT));
 			if (file.exists()) {
 				url = file.toURI().toURL();
 			}
 		} else if (cl.hasOption(RESOURCE_OPT)) {
 			url = Thread.currentThread().getContextClassLoader().getResource(
 					cl.getOptionValue(RESOURCE_OPT));
 		} else {
 			url = Main.class.getResource(XSITE_COMPOSITION);
 		}
 		return url;
 	}
 
 	static final Options createOptions() {
 		Options options = new Options();
 		options.addOption(String.valueOf(HELP_OPT), "help", false,
 				"print this message and exit");
 		options.addOption(String.valueOf(VERSION_OPT), "xsite-version", false,
 				"print the xsite version information and exit");
 		options.addOption(String.valueOf(SOURCE_OPT), "sitemap", true,
 				"specify the source directory");
 		options
 				.addOption(String.valueOf(FILE_OPT), "file", true,
 						"specify the composition file - relative to the source directory");
 		options.addOption(String.valueOf(RESOURCE_OPT), "resource", true,
 				"specify the composition resource - relative to the classpath");
 		options
 				.addOption(String.valueOf(SITEMAP_OPT), "sitemap", true,
 						"specify the sitemap file path - relative to the source directory");
 		options
 				.addOption(String.valueOf(SKIN_OPT), "skin", true,
 						"specify the skin file path - relative to the source directory");
 		options
 				.addOption(String.valueOf(TEMPLATES_OPT), "templates", true,
 						"specify the templates directory - defaults to the source directory");
 		options
 				.addOption(String.valueOf(RESOURCES_OPT), "resources", true,
 						"specify the CSV list of resource paths - relative to the source directory");
 		options.addOption(String.valueOf(L10N_OPT), "resources", true,
 				"specify the CSV list of language codes");
 		options.addOption(String.valueOf(OUTPUT_OPT), "output", true,
 				"specify the output dir");
 		options.addOption(String.valueOf(XSITE_FACTORY_OPT), "xsite-factory",
 				true, "specify the xsite factory name");
 		options.addOption(String.valueOf(PUBLISHED_DATE_OPT), "published-date",
 				true, "specify the published date");
 		options.addOption(String.valueOf(PUBLISHED_VERSION_OPT),
 				"published-version", true, "specify the published version");
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
 		usage
 				.append(Main.class.getName()
 						+ ": -S<source-dir> -m<relative-path-to-sitemap> -s<relative-path-to-skin> -o<output-dir> "
 						+ "[-t <templates-dir>]"
 						+ "[-R <csv-of-resource-paths>]"
 						+ "[-f<relative-path-to-xsite.xml>|-r<classpath-path-to-xsite.xml>] "
 						+ "[-x<xsite-factory-classname>" + "[-h|-v]");
 		usage.append(lineSeparator);
 		usage.append("Options: " + options.getOptions());
 		System.out.println(usage.toString());
 	}
 
 	private static void printVersion() {
 		String version = "not available";
 		Package pkg = Main.class.getPackage();
 		if (pkg != null) {
 			version = pkg.getImplementationVersion();
 		}
 		System.out.println(XSite.class.getName() + " version: " + version);
 	}
 
 }
