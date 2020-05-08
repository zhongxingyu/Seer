 package com.shivanshusingh.pluginanalyser.analysis;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 import org.apache.ivy.osgi.core.ManifestParser;
 import org.objectweb.asm.ClassReader;
 
 import com.shivanshusingh.pluginanalyser.utils.Util;
 import com.shivanshusingh.pluginanalyser.utils.logging.Log;
 import com.shivanshusingh.pluginanalyser.utils.parsing.Constants;
 
 /**
  * Analyzes the plugin / bundles.
  * 
  * @author Shivanshu Singh
  * 
  */
 public class BundleAnalyser extends ManifestParser {
 
 	/**
 	 * @param pluginFolderPath
 	 * @param outputLocation
 	 * @param eraseOld
 	 * @throws IOException
 	 */
 	public static void analyseAndRecordAllInformationFromBasePluginFolder(String pluginFolderPath, String outputLocation,
 			boolean eraseOld) throws IOException {
 		if (!Util.checkAndCreateDirectory(outputLocation)) {
 			Log.errln("xxxx Error Accessing/Creating Output Directory for Plugin Analysis Output at: " + outputLocation
 					+ "\n Cannot continue with the analysis.");
 			return;
 		}
 
 		if (eraseOld)
 			Util.clearFolder(new File(outputLocation));
 		// reading all the files (plugin jars) in the specified plugin folder
 		long l1 = System.currentTimeMillis();
 
 		Log.outln("==== Analysing Source:" + pluginFolderPath);
 		File folder = new File(pluginFolderPath);
 		if (null == folder) {
 			Log.outln("==== nothing here.");
 			return;
 		}
 		File[] listOfFiles = folder.listFiles();
 		long pluginAnalysedCounter = 0;
 		for (int i = 0; i < listOfFiles.length; i++) {
 			if (listOfFiles[i].isFile()) {
 				String pluginJarName = listOfFiles[i].getName();
 				if (pluginJarName.toLowerCase().endsWith(Constants.JAR_FILE_EXTENSION)) {
 					// this means that this is a plugin jar (it is assumed that
 					// this would be a plugin jar if it is at this location)
 					pluginAnalysedCounter++;
 					analyseAndRecordAllInformationFromPluginJar(pluginFolderPath, pluginJarName, outputLocation);
 
 				}
 
 			} else if (listOfFiles[i].isDirectory()) {
 
 				// some plugins may be unpacked and so exist as directories
 				// instead of jars.
 				pluginAnalysedCounter++;
 				analyseAndRecordAllInformationFromPluginDir(pluginFolderPath, listOfFiles[i].getName(), outputLocation);
 				// Log.outln("Directory " + listOfFiles[i].getName());
 			}
 		}
 		long l2 = System.currentTimeMillis();
 		Log.outln(pluginAnalysedCounter + " plugin have been analyzed");
 		Log.errln(pluginAnalysedCounter + " plugin have been analyzed");
 		Log.outln("for source:" + pluginFolderPath + " time: " + Util.getFormattedTime(l2 - l1));
 		Log.errln("for source:" + pluginFolderPath + " time: " + Util.getFormattedTime(l2 - l1));
 		// String pluginJarName
 		// ="com.android.ide.eclipse.adt_21.0.1.2012-12-6-2-58.jar";
 	}
 
 	/**
 	 * @param pathPrefix
 	 * @param pluginDirName
 	 * @param outputLocation
 	 * @throws IOException
 	 */
 	public static void analyseAndRecordAllInformationFromPluginDir(String pathPrefix, String pluginDirName,
 			String outputLocation) throws IOException {
 		long l1 = System.currentTimeMillis();
 
 		try {
 			DependencyVisitor v = new DependencyVisitor();
 			BundleInformation bundleInformation = new BundleInformation();
 			String dirNameWithPathFull = pathPrefix + pluginDirName;
 
 			File folder = new File(dirNameWithPathFull);
 			if (null == folder || !folder.isDirectory()) {
 				Log.outln("==== ==== nothing here.");
 				return;
 			}
 
 			bundleInformation = getBundleManifestAndMetaInformationFromDir(folder);
 			Log.outln("now starting the plugin_from_dir dependency  extraction for  : " + folder.getPath());
 			extractDependenciesAndExportsFromDir(v, bundleInformation, folder);
 
 			writeData(v, bundleInformation, folder.getName(), outputLocation);
 			long l2 = System.currentTimeMillis();
 
 			Log.errln("==== analysed:  \n " + dirNameWithPathFull + "\n time: " + Util.getFormattedTime(l2 - l1));
 		} catch (Exception e) {
 			Log.errln("xxxx ERROR WHILE ANALYSING PLUGIN Folder : " + pathPrefix + pluginDirName);
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * @param folder
 	 * @return
 	 */
 	private static BundleInformation getBundleManifestAndMetaInformationFromDir(File folder) {
 		// recursively constructing a set of paths of all files in this plugin
 		// folder.
 		Set<String> dirFileList = Util.listFilesForFolder(folder);
 		BundleInformation bundleInformation = new BundleInformation();
 		// getting plugin meta-inf/manifest.mf manifest information
 
 		Iterator<String> en = dirFileList.iterator();
 		boolean flag_manifestFound = false, flag_pluginxmlFound = false;
 		StringBuffer pluginxmlText = new StringBuffer();
 		while (en.hasNext() && (!flag_manifestFound || !flag_pluginxmlFound)) {
 
 			File e = new File(en.next().toString());
 			String name = e.getName();
 			// Log.outln("==== handling :" + name);
 
 			if (!flag_manifestFound && name.toLowerCase().endsWith("meta-inf/manifest.mf")) {
 				// getting the manifest.
 				flag_manifestFound = true;
 				try {
 
 					Log.outln("== manifest try: file name = " + folder.getPath() + ">" + name);
 
 					bundleInformation = extractManifestInformation(new FileInputStream(e));
 
 				} catch (Exception exception) {
 				}
 			} else if (!flag_pluginxmlFound && name.toLowerCase().endsWith("plugin.xml")) {
 				flag_pluginxmlFound = true;
 				try {
 					Log.outln("== plugin.xml capture: file name = " + folder.getPath() + ">" + name);
 					BufferedReader br;
 
 					br = new BufferedReader(new FileReader(e));
 
 					String ss;
 					while ((ss = br.readLine()) != null) {
 						pluginxmlText.append(ss);
 					}
 					br.close();
 					// Log.outln(pluginxmlText);
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 			}
 		}
 		// if(null!=bundleInformation)
 		bundleInformation.setPluginXml(pluginxmlText.toString());
 		return bundleInformation;
 
 	}
 
 	/**
 	 * @param pathPrefix
 	 * @param pluginJarName
 	 * @param outputLocation
 	 * @throws IOException
 	 */
 	public static void analyseAndRecordAllInformationFromPluginJar(String pathPrefix, String pluginJarName,
 			String outputLocation) throws IOException {
 		long l1 = System.currentTimeMillis();
 
 		try {
 			DependencyVisitor v = new DependencyVisitor();
 			BundleInformation bundleInformation = new BundleInformation();
 			// ////////archive/////////////////////////////////
 			String jarFileNameWithPathFull = pathPrefix + pluginJarName;
 
 			// ZipFile f = new ZipFile(jarFileNameWithPathFull);
 			JarFile f = new JarFile(jarFileNameWithPathFull);
 
 			// Actual part of getting the dependecies and offerrings from the
 			// current jar file.////
 
 			bundleInformation = getBundleManifestAndMetaInformationFromJar(f);
 			Log.outln("== now starting the  plugin dependency  extraction");
 			extractDependenciesAndExportsFromJarFile(v, bundleInformation, f);
 
 			// ////////////////////////////////////////
 
 			// ///////// //sing le class reading try //////////////
 
 			/*
 			 * jarFileNameWithPathFull=
 			 * "./bin/com/shivanshusingh/PluginAnalyser_OLD/DUMMYFORTESTClassSignatureExtractor.class"
 			 * ; File f = new File(jarFileNameWithPathFull); InputStream in= new
 			 * FileInputStream(f); new ClassReader(in).accept(v, 0);
 			 */
 			// /////////////////////////// ////////////////////////
 
 			writeData(v, bundleInformation, pluginJarName, outputLocation);
 			long l2 = System.currentTimeMillis();
 
 			Log.errln("==== analysed:  \n " + jarFileNameWithPathFull + "\n time: " + Util.getFormattedTime(l2 - l1));
 
 		} catch (Exception e) {
 			Log.errln("xxxx ERROR WHILE ANALYSING PLUGIN Jar : " + pathPrefix + pluginJarName);
 			Log.errln(Util.getStackTrace(e));
 			e.printStackTrace();
 		}
 	}
 
 	private static long internalFileCounter = 0;
 
 	/**
 	 * @param jarfileinstance
 	 * @return
 	 * @throws IOException
 	 */
 	private static BundleInformation getBundleManifestAndMetaInformationFromJar(JarFile jarfileinstance) throws IOException {
 
 		BundleInformation bundleInformation = new BundleInformation();
 		// getting plugin meta-inf/manifest.mf manifest information
 		Enumeration<? extends JarEntry> en = jarfileinstance.entries();
 
 		boolean flag_manifestFound = false, flag_pluginxmlFound = false;
 		StringBuffer pluginxmlText = new StringBuffer();
 		while (en.hasMoreElements() && (!flag_manifestFound || !flag_pluginxmlFound)) {
 
 			JarEntry e = en.nextElement();
 			String name = e.getName();
 
 			if (!flag_manifestFound && name.toLowerCase().endsWith("meta-inf/manifest.mf")) {
 				// getting the manifest.
 				flag_manifestFound = true;
 				try {
 
 					Log.outln("== manifest try: file name = " + jarfileinstance.getName() + ">" + name);
 
 					/*
 					 * Here printing out the detected Manifest file just for
 					 * debugging purposes.
 					 */
 					/*
 					 * if (name.toLowerCase().endsWith("manifest.mf")) {
 					 * Log.outln("====== indicated manifest entry");
 					 * BufferedReader br = new BufferedReader(new
 					 * InputStreamReader( jarfileinstance.getInputStream(e)));
 					 * String ss; while ((ss = br.readLine()) != null) {
 					 * Log.outln(ss + "/////////"); }
 					 * 
 					 * extractManifestInformation(jarfileinstance.getInputStream(
 					 * e));
 					 * 
 					 * }
 					 */
 					/* /////////////////////////////////// */
 					bundleInformation = extractManifestInformation(jarfileinstance.getInputStream(e));
 					// extractManifestInformation(jarfileinstance.getManifest());
 
 				} catch (Exception exception) {
 				}
 			} else if (!flag_pluginxmlFound && name.toLowerCase().endsWith("plugin.xml")) {
 				flag_pluginxmlFound = true;
 				Log.outln("== plugin.xml capture. : file name = " + jarfileinstance.getName() + ">" + name);
 				BufferedReader br = new BufferedReader(new InputStreamReader(jarfileinstance.getInputStream(e)));
 				String ss;
 				while ((ss = br.readLine()) != null) {
 					pluginxmlText.append(ss);
 				}
 				br.close();
 				// Log.outln(pluginxmlText);
 			}
 		}
 		if (null != bundleInformation)
 			bundleInformation.setPluginXml(pluginxmlText.toString());
 		return bundleInformation;
 
 	}
 
 	/**
 	 * @param visitor
 	 *            {@link DependencyVisitor}
 	 * @param bundleInformation
 	 *            {@link BundleInformation}
 	 * @param jarfileinstance
 	 * @throws IOException
 	 */
 	private static void extractDependenciesAndExportsFromJarFile(DependencyVisitor visitor,
 			BundleInformation bundleInformation, JarFile jarfileinstance) throws IOException {
 
 		// // for zip files reading ///////////////
 		// ZipFile f = new ZipFile(jarFileNameWithPathFull.trim());
 		// Enumeration<? extends ZipEntry> en = f.entries();
 		// ///////////////////////////////////////////
 
 		Log.outln("==== Starting the Archive : " + jarfileinstance.getName() + " analysis ====");
 
 		Enumeration<? extends JarEntry> en = jarfileinstance.entries();
 		int classesAnalyzedCounter = 0;
 
 		while (en.hasMoreElements()) {
 			JarEntry e = en.nextElement();
 
 			String name = e.getName();
 			// Log.outln(name);
 
 			if (name.toLowerCase().endsWith(".class")) {
 
 				classesAnalyzedCounter++;
 				new ClassReader(jarfileinstance.getInputStream(e)).accept(visitor, 0);
 
 			} else if (name.toLowerCase().endsWith(Constants.JAR_FILE_EXTENSION)) {
 				// nested jar.
 
 				Log.outln("====> " + name + " found");
 				// Log.outln(bundleInformation.getClasspathEntries().toString());
 
 				// now check if this nested jar file is one of the Bundle
 				// classpath dependencies (lib jars)
 				if (null != bundleInformation) {
 					for (String libJarNameEnding : bundleInformation.getClasspathEntries()) {
 						// Log.outln("CHECKING:"+name+": ends with:"+libJarNameEnding);
 						if (name.toLowerCase().endsWith(libJarNameEnding.toLowerCase())) {
 							// good news, the jar is present in the bundle
 							// manifest jar file entries' list.
 							Log.outln("====> now analysing internal lib jar:" + name);
 
 							String TEMPFileName = (Util.getTEMP_DIR_PATH() + "/pa-sks-plugin-tmp-").replace("//", "/")
 									+ Math.random() + ( // jarfileinstance.getName()
 														// // + "_" +
 									name).replaceAll("/", "_").replace(" ", "_");
 
 							BufferedReader bufferedTempReader = new BufferedReader(new InputStreamReader(
 									jarfileinstance.getInputStream(e)));
 
 							BufferedWriter bufferedTempWriter = new BufferedWriter(new FileWriter(TEMPFileName));
 							int inread;
 							while ((inread = bufferedTempReader.read()) != -1) {
 								bufferedTempWriter.write(inread);
 							}
 							bufferedTempWriter.close();
 							bufferedTempReader.close();
 							Log.outln("==== created : " + TEMPFileName + "==== ");
 							internalFileCounter++;
 							extractDependenciesAndExportsFromJarFile(visitor, bundleInformation, new JarFile(TEMPFileName));
 							Log.outln("==== delete = " + new File(TEMPFileName).delete() + " : " + TEMPFileName + "====");
 							Log.outln("==== ==== ==== ==== ");
 
 							break;// get out when found and analysed.
 						}
 					}
 				}
 			}
 		}
 		Log.outln("==== ==== " + classesAnalyzedCounter + " Class Files read.");
 		Log.outln("==== Ending the Archive : " + jarfileinstance.getName() + " analysis ====");
 		jarfileinstance.close();
 	}
 
 	/**
 	 * @param visitor
 	 *            {@link DependencyVisitor}
 	 * @param bundleInformation
 	 * @param folder
 	 * @throws IOException
 	 */
 	private static void extractDependenciesAndExportsFromDir(DependencyVisitor visitor,
 			BundleInformation bundleInformation, File folder) throws IOException {
 
 		Log.outln("==== Starting the Plugin_from_Dir : " + folder.getCanonicalPath() + " analysis ====");
 
 		// getting a recursive list of all files contained in this plugin dir.
 		Set<String> dirFileList = Util.listFilesForFolder(folder);
 
 		Iterator<String> en = dirFileList.iterator();
 
 		int classesAnalyzedCounter = 0;
 
 		while (en.hasNext()) {
 			File e = new File(en.next());
 
 			String name = e.getName();
 			// Log.outln(name);
 
 			if (name.toLowerCase().endsWith(".class")) {
 
 				classesAnalyzedCounter++;
 				new ClassReader(new FileInputStream(e)).accept(visitor, 0);
 
 			} else if (name.toLowerCase().endsWith(Constants.JAR_FILE_EXTENSION)) {
 				// nested jar.
 
 				Log.outln("====> " + name + " found");
 				// Log.outln(bundleInformation.getClasspathEntries().toString());
 
 				// now check if this nested jar file is one of the Bundle
 				// classpath dependencies (lib jars)
 				if (null != bundleInformation) {
 					for (String libJarNameEnding : bundleInformation.getClasspathEntries()) {
 						// Log.outln("CHECKING:"+name+": ends with:"+libJarNameEnding);
 						if (name.toLowerCase().endsWith(libJarNameEnding.toLowerCase())) {
 							// good news, the jar is present in the bundle
 							// manifest jar file entries' list.
 							Log.outln("====> now analysing internal lib jar:" + name);
 
 							String TEMPFileName = (Util.getTEMP_DIR_PATH() + "/pa-sks-plugin-tmp-").replace("//", "/")
 									+ Math.random() + (
 									// jarfileinstance.getName()
 									// // + "_" +
 									name).replaceAll("/", "_").replace(" ", "_");
 
 							BufferedReader bufferedTempReader = new BufferedReader(new FileReader(e));
 
 							BufferedWriter bufferedTempWriter = new BufferedWriter(new FileWriter(TEMPFileName));
 							int inread;
 							while ((inread = bufferedTempReader.read()) != -1) {
 								bufferedTempWriter.write(inread);
 							}
 							bufferedTempWriter.close();
 							bufferedTempReader.close();
 							Log.outln("==== created : " + TEMPFileName + "====");
 							internalFileCounter++;
 							extractDependenciesAndExportsFromJarFile(visitor, bundleInformation, new JarFile(TEMPFileName));
 							Log.outln("==== delete = " + new File(TEMPFileName).delete() + " : " + TEMPFileName + "====");
 							Log.outln("==== ==== ==== ==== ");
 
 							break;// get out when found and analysed.
 						}
 					}
 				}
 			}
 		}
 		Log.outln(classesAnalyzedCounter + " Class Files read.");
 		Log.outln("==== Ending the Plungin_from_Dir :  " + folder.getCanonicalPath() + " analysis =====");
 	}
 
 	/**
 	 * @param manifest
 	 * @return
 	 */
 	@SuppressWarnings("unused")
 	private static BundleInformation extractManifestInformation(Manifest manifest) {
 		BundleInformation bundleinfo = null;
 		try {
 			bundleinfo = new BundleInformation(manifest);
 			// extractBundleInfo(bundleinfo);
 
 		} catch (ParseException e) {
 			Log.outln("xxxx  NO Manifest found here or cannot parse that  or maybe theres nothing to parse inthis.  ");
 			// e.printStackTrace();
 		}
 		return bundleinfo;
 	}
 
 	/**
 	 * @param manifestStream
 	 * @return
 	 */
 	private static BundleInformation extractManifestInformation(InputStream manifestStream) {
 		BundleInformation bundleInformation = null;
 
 		try {
 
 			bundleInformation = new BundleInformation(manifestStream);
 
 			// extractBundleInfo(bundleInformation);
 
 		} catch (ParseException e) {
 			Log.outln("xxxx  NO Manifest found here or cannot parse that  or maybe theres nothing to parse inthis.    ");
 
 			// e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return bundleInformation;
 	}
 
 	/**
 	 * @param v
 	 * @param bundleinfo
 	 * @param pluginFileName
 	 * @param outputLocation
 	 * @throws IOException
 	 */
 	private static void writeData(DependencyVisitor v, BundleInformation bundleinfo, String pluginFileName,
 			String outputLocation) throws IOException {
 
 		pluginFileName = pluginFileName.toLowerCase().trim();
 		if (pluginFileName.endsWith(Constants.JAR_FILE_EXTENSION))
 			pluginFileName = pluginFileName.substring(0, pluginFileName.length() - Constants.JAR_FILE_EXTENSION.length());
 
 		outputLocation = (outputLocation + "/").trim().replaceAll("//", "/");
 
 		FileWriter fwriter = new FileWriter(outputLocation + Constants.EXTRACT_FILE_PREFIX_PLUGIN
 				+ pluginFileName.replace('/', '_') + Constants.EXTRACT_FILE_EXTENSION_PLUGIN);
 		BufferedWriter writer = new BufferedWriter(fwriter);
 
 		// ////////////////////////////////////////////////
 		Set<String> allDetectedTypes = v.getAllDetectedTypes();
 		Set<String> allExternalDetectedTypes = v.getAllExternalDetectedTypes();
 		Set<String> allExternalNonJavaDetectedTypes = v.getAllExternalNonJavaDetectedTypes();
 
 		Set<String> allMyMethods = v.getAllMyMethods();
 		Set<String> allMyDeprecatedMethods = v.getAllMyDeprecatedMethods();
 		Set<String> allMyDeprecatedPublicMethods = v.getAllMyDeprecatedPublicMethods();
 		Set<String> allMyPublicMethods = v.getAllMyPublicMethods();
 
 		Set<String> allInvokations = v.getAllInvokations();
 		Set<String> allExternalInvokations = v.getAllExternalMethodInvokations();
 		Set<String> allExternalNonJavaInvokations = v.getAllExternalNonJavaMethodInvokations();
 
 		Set<String> allMyClasses = v.getAllMyClasses();
 		Set<String> allMyDeprecatedClasses = v.getAllMyDeprecatedClasses();
 		Set<String> allMyDeprecatedPublicClasses = v.getAllMyDeprecatedPublicClasses();
 		Set<String> allMyPublicClasses = v.getAllMyPublicClasses();
 
 		// ///////////////////////////////////
 
 		Map<String, TypeDependency> allTypeDependencies_SuperClassAndInterfaces = v.getAllMyTypeDependencies();
 
 		Set<String> allInheritancePairs = new HashSet<String>();
 		Set<String> allInheritanceHierarchies = new HashSet<String>();
 		Set<String> allInterfaceImplPairs = new HashSet<String>();
 		Set<String> allInterfaceImplLists = new HashSet<String>();
 		Set<String> allInheritancePairsAndInterfaceImplPairsSuperSet = new HashSet<String>();
 
 		// towards getting a set of all classes so that the various sets can be
 		// built.
 		Set<String> classesKeySet = allTypeDependencies_SuperClassAndInterfaces.keySet();
 
 		// building all inheritance Pairs set and adding to the
 		// allInheritancePairsAndInterfaceImplPairsSuperSet
 		for (String key : classesKeySet) {
 			TypeDependency typeDep = (TypeDependency) allTypeDependencies_SuperClassAndInterfaces.get(key);
 			if (null != typeDep.superClass && !"".equalsIgnoreCase(typeDep.superClass)) {
 				String entry = key + Constants.DELIM_PLUGIN_ELEMENT_SUPERCLASS_INTERFACE + typeDep.superClass;
 				allInheritancePairs.add(entry);
 				allInheritancePairsAndInterfaceImplPairsSuperSet.add(entry);
 			}
 		}
 
 		// building all inheritance Hierarchies Map
 
 		for (String key : classesKeySet) {
 			String entry = "";
 			entry += getInheritanceHeirarchy(key, Constants.DELIM_PLUGIN_ELEMENT_SUPERCLASS_INTERFACE,
 					allTypeDependencies_SuperClassAndInterfaces);
 			if (null != entry && !"".equalsIgnoreCase(entry.trim()) && !key.trim().equalsIgnoreCase(entry.trim()))
 				allInheritanceHierarchies.add(entry);
 
 		}
 
 		// building all interface implementation lists, pairs and adding to the
 		// allInheritancePairsAndInterfaceImplPairsSuperSet
 		for (String key : classesKeySet)
 
 		{
 			TypeDependency typeDep = (TypeDependency) allTypeDependencies_SuperClassAndInterfaces.get(key);
 
 			String entry = "";
 			if (null != typeDep.interfaces && 1 >= typeDep.interfaces.size()) {
 
 				// System.out.println("++++++++ interfaces implemented:"+typeDep1.interfaces.size());
 				for (String interfaceImplemented : typeDep.interfaces) {
 					if (null != interfaceImplemented && !"".equalsIgnoreCase(interfaceImplemented.trim())) {
 						entry += interfaceImplemented.trim() + ";";
 
 						String localEntry = key.trim() + Constants.DELIM_PLUGIN_ELEMENT_SUPERCLASS_INTERFACE
 								+ interfaceImplemented.trim();
 						allInterfaceImplPairs.add(localEntry);
 						allInheritancePairsAndInterfaceImplPairsSuperSet.add(localEntry);
 					}
 				}
 
 				if (!"".equalsIgnoreCase(entry))
 					allInterfaceImplLists.add(key.trim() + Constants.DELIM_PLUGIN_ELEMENT_SUPERCLASS_INTERFACE
 							+ entry.trim());
 			}
 		}
 
 		// ///////////Pruning /////////////////////////////////////////
 		Set<String> invokationCulprits = getPruneableInvokations(allMyMethods, allExternalInvokations,
 				allInheritanceHierarchies, allInterfaceImplLists);
 
 		// now finally removing the invokations. (actual pruning) at the plugin
 		// level.
 		for (String invokation : invokationCulprits) {
 			System.out.println(allExternalInvokations.remove(invokation) + "= remove  from AllExternalInvokations \t: "
 					+ invokation);
 			System.out.println(allExternalNonJavaInvokations.remove(invokation)
 					+ "= remove  from AllExternalAndNonJavaInvokations : " + invokation);
 		}
 
 		// Pruning over.
 
 		// ////////////////////////////////
 
 		List<String> allDetectedTypes_List = new ArrayList<String>(allDetectedTypes);
 		List<String> allExternalDetectedTypes_List = new ArrayList<String>(allExternalDetectedTypes);
 		List<String> allExternalNonJavaDetectedTypes_List = new ArrayList<String>(allExternalNonJavaDetectedTypes);
 
 		List<String> allMyMethods_List = new ArrayList<String>(allMyMethods);
 		List<String> allMyDeprecatedMethods_List = new ArrayList<String>(allMyDeprecatedMethods);
 		List<String> allMyDeprecatedPublicMethods_List = new ArrayList<String>(allMyDeprecatedPublicMethods);
 		List<String> allMyPublicMethods_List = new ArrayList<String>(allMyPublicMethods);
 
 		List<String> allInvokations_List = new ArrayList<String>(allInvokations);
 		List<String> allExternalInvokations_List = new ArrayList<String>(allExternalInvokations);
 		List<String> allExternalNonJavaInvokations_List = new ArrayList<String>(allExternalNonJavaInvokations);
 
 		List<String> allMyClasses_List = new ArrayList<String>(allMyClasses);
 		List<String> allMyDeprecatedClasses_List = new ArrayList<String>(allMyDeprecatedClasses);
 		List<String> allMyDeprecatedPublicClasses_List = new ArrayList<String>(allMyDeprecatedClasses);
 		List<String> allMyPublicClasses_List = new ArrayList<String>(allMyPublicClasses);
 
 		Map<String, Map<String, Integer>> globals = v.getGlobals();
 		List<String> jarPackages_List = new ArrayList<String>(globals.keySet());
 		List<String> classPackages_List = new ArrayList<String>(v.getPackages());
 
 		List<String> allInheritancePairs_List = new ArrayList<String>(allInheritancePairs);
 		List<String> allInheritanceHierarchies_List = new ArrayList<String>(allInheritanceHierarchies);
 		List<String> allInterfaceImplPairs_List = new ArrayList<String>(allInterfaceImplPairs);
 		List<String> allInterfaceImplLists_List = new ArrayList<String>(allInterfaceImplLists);
 		List<String> allInheritancePairsAndInterfaceImplPairsSuperSet_List = new ArrayList<String>(
 				allInheritancePairsAndInterfaceImplPairsSuperSet);
 
 		java.util.Collections.sort(allDetectedTypes_List);
 		java.util.Collections.sort(allMyMethods_List);
 		java.util.Collections.sort(allMyPublicMethods_List);
 		java.util.Collections.sort(allMyDeprecatedMethods_List);
 		java.util.Collections.sort(allMyDeprecatedPublicMethods_List);
 		java.util.Collections.sort(allInvokations_List);
 		java.util.Collections.sort(allMyClasses_List);
 		java.util.Collections.sort(jarPackages_List);
 		java.util.Collections.sort(classPackages_List);
 		java.util.Collections.sort(allExternalInvokations_List);
 		java.util.Collections.sort(allExternalNonJavaInvokations_List);
 		java.util.Collections.sort(allExternalDetectedTypes_List);
 		java.util.Collections.sort(allExternalNonJavaDetectedTypes_List);
 		java.util.Collections.sort(allMyPublicClasses_List);
 		java.util.Collections.sort(allMyDeprecatedClasses_List);
 		java.util.Collections.sort(allMyDeprecatedPublicClasses_List);
 		java.util.Collections.sort(allInheritancePairs_List);
 		java.util.Collections.sort(allInheritanceHierarchies_List);
 		java.util.Collections.sort(allInterfaceImplPairs_List);
 		java.util.Collections.sort(allInterfaceImplLists_List);
 		java.util.Collections.sort(allInheritancePairsAndInterfaceImplPairsSuperSet_List);
 
 		// /////////// BUNDLE MANIFEST ///////////////////
 		// java.util.Collections.sort(bundleRequirements);
 		// this is the set of other plugins that this plugin would depend on.
 		// bundleinfo.getRequires() and bundleinfo.getImports() eventually point
 		// to bundleinfo.getRequirements() without any differences.
 
 		boolean flag_bundleInfoExists = true;
 		if (null == bundleinfo || null == bundleinfo.getBundleInfo())
 			flag_bundleInfoExists = false;
 
 		writer.write(Constants.BUNDLE_REQUIREMENTS + "\n");
 		if (flag_bundleInfoExists) {
 
 			for (Object s : bundleinfo.getRequirements())
 				writer.write(s.toString() + "\n");
 			// Log.outln("Bundle Requirements = " +
 			// bundleinfo.getRequirements().toString()+"\n"+bundleinfo.getRequirements().size()
 			// +" , Bundle Requirements"); // Require-Bundle
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		writer.write(Constants.BUNDLE_EXPORTS + "\n");
 		if (flag_bundleInfoExists) {
 			for (Object s : bundleinfo.getExports())
 				writer.write(s.toString() + "\n");
 			// Log.outln("Bundle Exports = " +
 			// bundleinfo.getExports().toString()); // Export-Package
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		writer.write(Constants.BUNDLE_SYMBOLICNAME + "\n");
 		if (flag_bundleInfoExists) {
 			writer.write(null != bundleinfo.getSymbolicName() ? bundleinfo.getSymbolicName().toString() + "\n" : "");
 			// Log.outln("Symbolic Name = "+
 			// bundleinfo.getSymbolicName().toString());
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		writer.write(Constants.BUNDLE_VERSION + "\n");
 		if (flag_bundleInfoExists) {
 			writer.write(null != bundleinfo.getVersion() ? bundleinfo.getVersion().toString() + "\n" : "");
 			// Log.outln("Version = " +
 			// bundleinfo.getVersion().toString());
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		writer.write(Constants.BUNDLE_VERSION_WITHOUT_QUALIFIER + "\n");
 		if (flag_bundleInfoExists) {
 			writer.write(null != bundleinfo.getVersion() ? bundleinfo.getVersion().withoutQualifier().toString() + "\n"
 					: "");
 			// Log.outln("Version without qualifier  = " +
 			// bundleinfo.getVersion().withoutQualifier().toString());
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		writer.write(Constants.BUNDLE_IMPORTS + "\n ");
 		if (flag_bundleInfoExists) {
 			for (Object s : bundleinfo.getImports())
 				writer.write(s.toString() + "\n");
 			// Log.outln("Bundle Imports = " +
 			// bundleinfo.getImports().toString());
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		writer.write(Constants.BUNDLE_CLASSPATHENTRIES + "\n");
 		if (flag_bundleInfoExists) {
 			for (Object s : bundleinfo.getClasspathEntries())
 				writer.write(s.toString() + "\n");
 			// Log.outln("Bundle ClassPathEntries  = " +
 			// bundleinfo.getClasspathEntries().toString());
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		// Log.outln("Bundle hashcode  = " + bundleinfo.hashCode() );
 
 		// //////////////////////////////////////////////////////
 
 		writer.write(Constants.PLUGIN_ALL_INHERITANCE_HIERARCHIES + "\n");
 		/*
 		 * for( String key:classesKeySet) { String toWrite="";
 		 * toWrite+=getInheritanceHeirarchy(key,
 		 * Constants.DELIM_PLUGIN_ELEMENT_SUPERCLASS_INTERFACE,
 		 * allTypeDependencies_SuperClassAndInterfaces); if(null!=toWrite &&
 		 * !"".equalsIgnoreCase(toWrite) &&
 		 * !key.trim().equalsIgnoreCase(toWrite.trim()) )
 		 * writer.write(toWrite.trim()+"\n"); }
 		 */
 
 		for (String s : allInheritanceHierarchies_List)
 			writer.write(s + "\n");
 
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 		writer.write(Constants.PLUGIN_ALL_INHERITANCE_PAIRS + "\n");
 		/*
 		 * for( String key:classesKeySet) {
 		 * 
 		 * // for non recursive inheritence relationships. TypeDependency
 		 * typeDep=(TypeDependency)
 		 * allTypeDependencies_SuperClassAndInterfaces.get(key);
 		 * 
 		 * if(null!=typeDep.superClass
 		 * &&!"".equalsIgnoreCase(typeDep.superClass))
 		 * 
 		 * writer.write(key+Constants.DELIM_PLUGIN_ELEMENT_SUPERCLASS_INTERFACE+
 		 * typeDep.superClass+"\n");
 		 * 
 		 * 
 		 * }
 		 */
 		for (String s : allInheritancePairs_List)
 			writer.write(s + "\n");
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 		writer.write(Constants.PLUGIN_ALL_INTERFACE_IMPLEMENTATION_LISTS + "\n");
 		/*
 		 * for( String key1:classesKeySet) { TypeDependency
 		 * typeDep1=(TypeDependency)
 		 * allTypeDependencies_SuperClassAndInterfaces.get(key1);
 		 * 
 		 * 
 		 * String toWrite=""; if(null!=typeDep1.interfaces && 1>=
 		 * typeDep1.interfaces.size()) {
 		 * 
 		 * //System.out.println(
 		 * "+++++++++++++++++++++++++++++++++++++++++interfacec implemented:"
 		 * +typeDep1.interfaces.size()); for(String interfaceImplemented:
 		 * typeDep1.interfaces) { toWrite+= interfaceImplemented+";"; }
 		 * 
 		 * if(!"".equalsIgnoreCase(toWrite))
 		 * writer.write(key1+Constants.DELIM_PLUGIN_ELEMENT_SUPERCLASS_INTERFACE
 		 * +toWrite+"\n"); } }
 		 */
 		for (String s : allInterfaceImplLists_List)
 			writer.write(s + "\n");
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 		writer.write(Constants.PLUGIN_ALL_INTERFACE_IMPLEMENTATION_PAIRS + "\n");
 		for (String s : allInterfaceImplPairs_List)
 			writer.write(s + "\n");
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 		writer.write(Constants.PLUGIN_ALL_INHERITANCE_AND_INTERFACE_PAIRS + "\n");
 		for (String s : allInheritancePairsAndInterfaceImplPairsSuperSet_List)
 			writer.write(s + "\n");
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 		// ///////////////////////////////////////////////////////
 
 		writer.write(Constants.PLUGIN_ALL_MY_TYPES + "\n");
 		// "All My Classes (Types)  ========\n");
 
 		for (String s : allMyClasses_List) {
 			writer.write(s + "\n");
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		// writer.write(allMyClasses.size() + "," + " own classes (types).\n");
 		// Log.outln(allMyClasses.size() + "," +
 		// " own classes (types).");
 
 		writer.write(Constants.PLUGIN_ALL_MY_TYPES_PUBLIC + "\n");
 		// "All My Public Classes (Types) ========\n");
 
 		for (String s : allMyPublicClasses_List) {
 			writer.write(s + "\n");
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		// writer.write(allMyPublicClasses.size() + ","+
 		// " own public classes (types).\n");
 		// Log.outln(allMyPublicClasses.size() + "," +
 		// " own public classes (types).");
 
 		writer.write(Constants.PLUGIN_ALL_MY_METHODS + "\n");
 		// "All My Methods ========\n");
 
 		for (String s : allMyMethods_List) {
 			writer.write(s + "\n");
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		// writer.write(allMyMethods.size() + "," + " internal methods.\n");
 		// Log.outln(allMyMethods.size() + "," + " internal methods.");
 
 		writer.write(Constants.PLUGIN_ALL_MY_METHODS_PUBLIC + "\n");
 		// "All My Public Methods ========\n");
 
 		for (String s : allMyPublicMethods_List) {
 			writer.write(s + "\n");
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		// writer.write(allMyPublicMethods.size() + ","+
 		// " internal public methods.\n");
 		// Log.outln(allMyPublicMethods.size() + "," +
 		// " internal public methods.");
 
 		writer.write(Constants.PLUGIN_ALL_MY_METHOD_CALLS + "\n");
 		// "All Invokations ========\n");
 
 		for (String s : allInvokations_List) {
 			writer.write(s + "\n");
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		// writer.write(allInvokations.size() + "," +
 		// " method invokations (intrnal and external).\n");
 		// Log.outln(allInvokations.size() + "," +
 		// " method invokations (intrnal and external).");
 
 		writer.write(Constants.PLUGIN_ALL_MY_METHOD_CALLS_EXTERNAL + "\n");
 		// "All External Invokations ========\n");
 
 		for (String s : allExternalInvokations_List) {
 			writer.write(s + "\n");
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		// writer.write(externalInvokations.size() + ","+
 		// " method invokations (external).\n");
 		// Log.outln(externalInvokations.size() + "," +
 		// " method invokations (external).");
 
 		writer.write(Constants.PLUGIN_ALL_MY_METHOD_CALLS_EXTERNAL_AND_NON_JAVA + "\n");
 		// "All External and non Excluded Invokations ========\n");
 
 		for (String s : allExternalNonJavaInvokations_List) {
 			writer.write(s + "\n");
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		// writer.write(externalNonJavaInvokations.size() + "," +
 		// " method invokations (external and non excluded).\n");
 		// Log.outln(externalNonJavaInvokations.size() + ","
 		// + " method invokations (external and non excluded).");
 
 		writer.write(Constants.PLUGIN_ALL_TYPES_DETECTED + "\n");
 		// "All Detected Types ========\n");
 		for (String s : allDetectedTypes_List) {
 			writer.write(s + "\n");
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		// writer.write(allDetectedTypes.size() + "," +
 		// " types (internal and external).\n");
 		// Log.outln(allDetectedTypes.size() + ","
 		// + " types (internal and external).");
 
 		writer.write(Constants.PLUGIN_ALL_TYPES_DETECTED_EXTERNAL + "\n");
 		// "All External Detected Types ========\n");
 		for (String s : allExternalDetectedTypes_List) {
 			writer.write(s + "\n");
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		// writer.write(allExternalDetectedTypes.size() + "," +
 		// " types (external).\n");
 		// Log.outln(allExternalDetectedTypes.size() + ","
 		// + " types (external).");
 
 		writer.write(Constants.PLUGIN_ALL_TYPES_DETECTED_EXTERNAL_AND_NON_JAVA + "\n");
 		// "All External Non Java Detected Types ========\n");
 		for (String s : allExternalNonJavaDetectedTypes_List) {
 			writer.write(s + "\n");
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		// writer.write(allExternalNonJavaDetectedTypes.size() + "," +
 		// " types (external non excluded).\n");
 		// Log.outln(allExternalNonJavaDetectedTypes.size() + ","
 		// + " types (external Non excluded).");
 
 		writer.write(Constants.PLUGIN_ALL_JAR_PACKAGES + "\n");
 		// "All Jar Packages ========\n");
 
 		for (String s : jarPackages_List) {
 			writer.write(s + "\n");
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		// writer.write(jarPackages.size() + "," + " jar packages.\n");
 
 		writer.write(Constants.PLUGIN_ALL_CLASS_PACKAGES + "\n");
 		// "All  Class packages ========\n");
 
 		for (String s : classPackages_List) {
 			writer.write(s + "\n");
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		// writer.write(classPackages.size() + "," + " class packages.\n");
 		writer.write(Constants.PLUGIN_ALL_MY_METHODS_DEPRECATED + "\n");
 		// "All My Deprecated Methods ========\n");
 
 		for (String s : allMyDeprecatedMethods_List) {
 			writer.write(s + "\n");
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 		// writer.write(allMyDeprecatedMethods.size() + "," +
 		// " deprecated methods.\n");
 		// .println(allMyDeprecatedMethods.size() + ","
 		// + " deprecated methods.");
 
 		writer.write(Constants.PLUGIN_ALL_MY_TYPES_DEPRECATED + "\n");
 		// "All My Deprecated Classes ========\n");
 
 		for (String s : allMyDeprecatedClasses_List) {
 			writer.write(s + "\n");
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 		// writer.write(allMyDeprecatedClasses.size() + "," +
 		// " deprecated   classes. \n");
 		// Log.outln(allMyDeprecatedClasses.size() + ","
 		// + " deprecated   classes.");
 
 		writer.write(Constants.BUNDLE_PLUGIN_XML + "\n");
 		if (null != bundleinfo) {// this means that there won't be any
 									// plugin.xml available.
 			writer.write(bundleinfo.getPluginXml() + "\n");
 		}
 		writer.write(Constants.MARKER_TERMINATOR + "\n");
 
 		// writer.write("===================================================\n");
 		writer.close();
 		fwriter.close();
 	}
 
 	/**
 	 * 
 	 * gets a set of invokation signatures that need to be pruned as they might
 	 * have showed up because there was an indirect call to them through
 	 * inherited methods or interfaces.
 	 * 
 	 * @param allMyMethods
 	 * @param allExternalInvokations
 	 * @param allInheritanceHierarchies
 	 * @param allInterfaceImplLists
 	 * @return
 	 */
 	private static Set<String> getPruneableInvokations(Set<String> allMyMethods, Set<String> allExternalInvokations,
 			Set<String> allInheritanceHierarchies, Set<String> allInterfaceImplLists) {
 		// // pruning at the plugin level to remove all external method
 		// invokations such that they may appear in the external invokations
 		// list may be provided by some superclass or through an interface.
 
 		/*
 		 * all invokations ( external), see if a prefix for this one's class
 		 * exists in the inheritance and interface pairs.
 		 * 
 		 * For all those super class and interface matches, see whether the same
 		 * invokation is available in either the All Methods or All External
 		 * Methods, if so remove this method entry from Externals.
 		 * 
 		 * 
 		 * Maybe it is needed to compare the entire inheritance hierarchy .....
 		 * 
 		 * So get entries for this invokations from the class
 		 */
 
 		Set<String> invokationsToBeRemoved = new HashSet<String>();
 		for (String invokation : allExternalInvokations) {
 			// getting the class name of this invokation: first removing the
 			// return type and then the function name.
 
 			String[] invokationTokens = invokation.split(" ");
 
 			// invokationTokens[1] is the function name without the return type
 			// and without the parameter, but with the fully qualified name
 			// including the owner class.
 			String[] thisInvokationClassTokens = invokationTokens[1].split("\\.");
 			// just the function name without the owner class name with it would
 			// be thisInvokationClassTokens[last].
 			String thisInvokationFunction = thisInvokationClassTokens[thisInvokationClassTokens.length - 1];
 
 			// and so the rest would be the fully qualified class name of this
 			// function: thisInvokationClassTokens[0] to
 			// thisInvokationClassTokens[last-1].
 			String thisInvokationClass = thisInvokationClassTokens[0];
 			for (int x = 1; x < thisInvokationClassTokens.length - 1; x++) {
 				thisInvokationClass += "." + thisInvokationClassTokens[x].trim();
 
 			}
 
 			// System.out.println("checking for  invokation class token:++++++:"+thisInvokationClass);
 
 			// get the inheritance hierarchy classes and all interface impl
 			// sequences classes for all entries that start with this class in:
 			// allInheritanceHierarchies and allInterfaceImplLists
 			Set<String> superClassAndInterfaceSuspects = new HashSet<String>();
 
 			// initialize this set with java.lang.Object as everything comes
 			// from it.
 			// superClassAndInterfaceSuspects.add(Constants.JAVA_LANG_OBJECT);
 
 			for (String entry : allInheritanceHierarchies) 
 			{
 				// System.out.println(thisInvokationClass+" > (superClass) at the starting of?: "
 				// + entry);
 				if (entry.startsWith(thisInvokationClass.trim())) 
 				{
 					// probably found the entry that we were looking for.
 
 					// System.out.println(thisInvokationClass+" >  (superClass) at the starting of?: "
 					// + entry);
 
 					String tokens[] = entry.split(Constants.DELIM_PLUGIN_ELEMENT_SUPERCLASS_INTERFACE);
 					if (thisInvokationClass.trim().equals(tokens[0].trim())) 
 					{
 						// yes surely found it. and there is going to be only
 						// one inheritance hierarchy that we would be interested
 						// in.
 
 						for (int x = 1; x < tokens.length; x++) 
 						{
 							String token = tokens[x];
 							if (!"".equalsIgnoreCase(token.trim())) 
 							{
 								System.out.println("Adding ctoken:+++++++++:" + token.trim());
 								superClassAndInterfaceSuspects.add(token.trim());
 							}
 						}
 
 						break;
 					}
 
 				}
 
 			}
 			for (String entry : allInterfaceImplLists) 
 			{
 				// System.out.println(thisInvokationClass+" > (interface) at the starting of?: "
 				// + entry);
 
 				if (entry.startsWith(thisInvokationClass.trim())) 
 				{
 					// probably found the entry that we were looking for.
 
 					// System.out.println(thisInvokationClass+" > (interface) at the starting of?: "
 					// + entry);
 
 					String tokens[] = entry.split(Constants.DELIM_PLUGIN_ELEMENT_SUPERCLASS_INTERFACE);
 					if (thisInvokationClass.trim().equals(tokens[0].trim())) 
 					{
 						// yes surely found it. and there is going to be only
 						// one interfaceImplList that we would be interested
 						// in.
 						for (int x = 1; x < tokens.length; x++) 
 						{
 							// getting the list of interfaces implemented.
 							String token = tokens[x];
 							
 							
 							if (!"".equalsIgnoreCase(token.trim()))
 							{
 								String[]  interfaces=token.split(";");
 								for(String i  :  interfaces  )
 								{
 									if (!"".equalsIgnoreCase(i.trim())) 
 									{
 										System.out.println("Adding itoken:+++++++++:" + i.trim());
 		
 										superClassAndInterfaceSuspects.add(i.trim());
 									}
 								}
 							}
 						}
 
 						break;
 					}
 				}
 			}
 			/*
 			 * System.out.println("+++++++++++++++++++++");
 			 * 
 			 * for( String className:superClassAndInterfaceSuspects) {
 			 * System.out.println(className); } System.out.println(
 			 * "++++++++++++++++++++++++++++++++++++++++++++++++++++++");
 			 */
 
 			// see if the AllMyMethods has any function call with the same stuff
 			// except being offered through any of the detected super classes or
 			// interfaces found through the process above and collected in the
 			// superClassAndInterfaceSuspects set.
 
 			for (String entry : superClassAndInterfaceSuspects) {
 
 				// make the new invokation string by replacing the class name
 				// with the new class name (entry)
 				String newInvokationEntry = invokation.replace(thisInvokationClass, entry.trim());
 
 				// now check if AllMyMethods (or maybe AllMyPlublicMethods) has
 				// anything like the newInvokationEntry. And if so then remove
 				// the original invokation from AllExternalInvokations and
 				// AllExternalAndNonJavaInvokations as the original invokation
 				// wasnt external at all in the first place rather being called
 				// through a subclass or a class implementing the interface
 				// which was providing this method and thus the original
 				// invokation did not show up in AllMyMethods or AllMyPublic
 				// Methods.
 
 				if (allMyMethods.contains(newInvokationEntry)) {
 
 					// now storing the invokation to be removed to be removed in
 					// the next step. Cannot do it here as we might get a
 					// concurrent modification exception as we are iterating
 					// over the same set that we have to remove from.
 					System.out.println("Marking for removal: " + invokation + "  because of: " + newInvokationEntry);
 					invokationsToBeRemoved.add(invokation);
 
 				}
 			}
 
 		}
 		return invokationsToBeRemoved;
 	}
 
 	private static StringBuffer getInheritanceHeirarchy(String className, String delim,
 			Map<String, TypeDependency> allTypeDependencies_SuperClassAndInterfaces) {
 		StringBuffer toWrite = new StringBuffer(className);
 
 		if (allTypeDependencies_SuperClassAndInterfaces.containsKey(className)) {
 			TypeDependency typeDep = (TypeDependency) allTypeDependencies_SuperClassAndInterfaces.get(className);
 
 			if (null != typeDep.superClass && !"".equalsIgnoreCase(typeDep.superClass)) {
 				toWrite.append(delim
 						+ getInheritanceHeirarchy(typeDep.superClass, delim, allTypeDependencies_SuperClassAndInterfaces));
 			}
 		}
 		// System.out.println(toWrite);
 		return toWrite;
 	}
 
 	/**
 	 * @deprecated Do not use this.
 	 * @param bundleinfo
 	 */
 	@SuppressWarnings({ "unused", "unchecked" })
 	private static void extractBundleInfo(BundleInformation bundleinfo) {
 		Set<String> bundleExports = new LinkedHashSet<String>(bundleinfo.getExports());
 		Set<String> bundleRequires = new LinkedHashSet<String>(bundleinfo.getRequires());
 
 		// Java version so no use
 		List<String> bundleExecEnv = new ArrayList<String>(bundleinfo.getExecutionEnvironments());
 
 		// this is the set of other plugins that this plugin would depend on.
 		// bundleinfo.getRequires() and bundleinfo.getImports() eventually point
 		// to bundleinfo.getRequirements() without any differences.
 		Log.outln("Bundle Requirements = " + bundleinfo.getRequirements().toString()); // Require-Bundle
 		// //////
 		Log.outln("Bundle Exports = " + bundleExports.toString()); // Export-Package
 		Log.outln("Name Symbolic = " + bundleinfo.getSymbolicName().toString());
 		Log.outln("Version = " + bundleinfo.getVersion().toString());
 		Log.outln("Version without qualifier  = " + bundleinfo.getVersion().withoutQualifier().toString());
 		Log.outln("Bundle Imports  = " + bundleinfo.getImports().toString());
 		Log.outln("Bundle ClassPathEntries  = " + bundleinfo.getClasspathEntries().toString());
 		Log.outln("Bundle hashcode  = " + bundleinfo.hashCode());
 
 	}
 
}
