 package net.java.dev.weblets.impl.util;
 
 import net.java.dev.weblets.util.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.Set;
 import java.util.Enumeration;
 import java.util.jar.JarFile;
 import java.util.jar.JarEntry;
 import java.io.IOException;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.net.URL;
import java.net.URLDecoder;
 
 import net.java.dev.weblets.impl.WebletContainerImpl;
 
 /**
  * Created by IntelliJ IDEA. User: werpu Date: 09.05.2008 Time: 09:19:27 To change this template use File | Settings | File Templates.
  */
 public class ConfigurationUtils {
 	/**
 	 * Gets a list of wildarded config files if a root weblets-config is present!
	 *
 	 * @return
 	 * @throws java.io.IOException
 	 */
 	private static String getPackageExtension(String incomingPath) {
 		// currently allowed package extensions for resource bundles
 		if (incomingPath.indexOf(".jar!") != -1)
 			return ".jar";
 		else if (incomingPath.indexOf(".zip!") != -1)
 			return ".zip";
 		else if (incomingPath.indexOf(".ear!") != -1)
 			return ".ear";
 		else if (incomingPath.indexOf(".par!") != -1)
 			return ".par";
 		return null;
 	}
 
 	public static Set getValidConfigFiles(String rootDir, String rootFilename, Set namesToSearchFor) throws IOException {
 		if (rootDir == null)
 			rootDir = "META-INF/";
 		Enumeration e = getConfigEnumeration(rootDir, rootFilename); /* lets find the root configs first */
 		while (e.hasMoreElements()) {
 			// we also check for subconfics
 			URL element = (URL) e.nextElement();

			String pathToOtherResources = URLDecoder.decode(element.getFile(), "UTF-8");
 			if (!StringUtils.isBlank(pathToOtherResources)) {
 				pathToOtherResources = pathToOtherResources.replaceAll("META-INF/" + rootFilename, "META-INF/");
 				String pkgExt = getPackageExtension(pathToOtherResources);
 				if (pkgExt != null) {
 					String jarPath = pathToOtherResources.substring(0, pathToOtherResources.indexOf(pkgExt + "!"));
 					jarPath += pkgExt;
 					if (jarPath.startsWith("file:"))
 						jarPath = jarPath.replaceFirst("file:", "");
 					else if (jarPath.matches("^[A-Za-z]+\\:.*")) { // only file protocols are allowed for now
 						Log log = LogFactory.getLog(ConfigurationUtils.class);
 						log.warn("Weblets initialisation Warning: " + jarPath + " Only file protocol is allowed for resource bundles for now ");
 						log.warn("continuing with the initialisation ");
 						continue;
 					}
 					JarFile file = new JarFile(jarPath);
 					Enumeration entries = file.entries();
 					while (entries.hasMoreElements()) {
 						JarEntry entry = (JarEntry) entries.nextElement();
 						String fileName = entry.getName();
 						fileName = fileName.replaceAll("\\\\", "/"); // lets normalize first
 						if (!fileName.startsWith("/"))
 							fileName = "/" + fileName;
 						if (fileName.matches("^\\/META-INF\\/.*weblets\\-config.*\\.xml$")) {
 							fileName = fileName.replaceFirst("/META-INF/", "");
 							namesToSearchFor.add(fileName);
 						}
 					}
 				} else {
 					File file = null;
 					file = new File(pathToOtherResources);
 					String[] files = file.list(new FilenameFilter() {
 						public boolean accept(File dir, String name) {
 							return name.matches("^.*weblets\\-config.*\\.xml$");
 						}
 					});
					for (int cnt = 0; files != null && cnt < files.length; cnt++) { // end for declaration
 						namesToSearchFor.add(files[cnt]);
 					}
 				}
 			}
 		}
 		return namesToSearchFor;
 	}
 
 	public static Enumeration getConfigEnumeration(String rootDir, String configFile) throws IOException {
 		ClassLoader loader = Thread.currentThread().getContextClassLoader();
 		Enumeration e = loader.getResources(rootDir + configFile);
 		if (e == null) {
 			loader = WebletContainerImpl.class.getClassLoader();
 			e = loader.getResources(rootDir + configFile);
 		}
 		return e;
 	}
 }
