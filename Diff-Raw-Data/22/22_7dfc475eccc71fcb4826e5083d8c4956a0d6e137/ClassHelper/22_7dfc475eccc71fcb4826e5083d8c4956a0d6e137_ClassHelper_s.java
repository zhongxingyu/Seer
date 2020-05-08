 package haflow.utility;
 
 import java.io.File;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import org.springframework.stereotype.Component;
 
 @Component
 public class ClassHelper {
 	private List<String> getClassNameByFile(String packageName,
 			String filePath, List<String> className,
 			boolean searchInChildPackage) throws Exception {
 		List<String> classNames = new ArrayList<String>();
 		String decodedFilePath = URLDecoder.decode(filePath, "utf-8");
 		File file = new File(decodedFilePath);
 		File[] classFiles = file.listFiles();
 		for (File classFile : classFiles) {
 			if (classFile.isDirectory()) {
 				if (searchInChildPackage) {
 					classNames.addAll(getClassNameByFile(packageName,
 							classFile.getPath(), classNames,
 							searchInChildPackage));
 				}
 			} else {
 				String classFilePath = classFile.getPath();
				String base = Thread.currentThread().getContextClassLoader()
						.getResource("").getPath();
				classFilePath = classFilePath.replace(base, "");
 				if (classFilePath.endsWith(".class")) {
 					classFilePath = classFilePath.replace("\\", ".");
 					classFilePath = classFilePath.replace("/", ".");
					classFilePath = classFilePath.substring(0,
 							classFilePath.lastIndexOf("."));
 
					if (packageName.isEmpty()) {
						while (true) {
							try {
								Class.forName(classFilePath);
								break;
							} catch (ClassNotFoundException ex) {
								classFilePath = classFilePath
										.substring(classFilePath.indexOf(".") + 1);
							}
						}
					} else {
						classFilePath = classFilePath.substring(classFilePath
								.indexOf(packageName));
					}
 					classNames.add(classFilePath);
 				}
 			}
 		}
 		return classNames;
 	}
 
 	private List<String> getClassNameByJar(String jarPath,
 			boolean searchInChildPackage) throws Exception {
 		List<String> classNames = new ArrayList<String>();
 		String[] jarInfo = jarPath.split("!");
 		String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
 		String decodedJarFilePath = URLDecoder.decode(jarFilePath, "utf-8");
 		String packagePath = jarInfo[1].substring(1);
 
 		JarFile jarFile = new JarFile(decodedJarFilePath);
 		Enumeration<JarEntry> entrys = jarFile.entries();
 		while (entrys.hasMoreElements()) {
 			JarEntry jarEntry = entrys.nextElement();
 			String entryName = jarEntry.getName();
 			if (entryName.endsWith(".class")) {
 				if (searchInChildPackage) {
 					if (entryName.startsWith(packagePath)) {
 						entryName = entryName.replace("/", ".").substring(0,
 								entryName.lastIndexOf("."));
 						classNames.add(entryName);
 					}
 				} else {
 					int index = entryName.lastIndexOf("/");
 					String entryPath;
 					if (index != -1) {
 						entryPath = entryName.substring(0, index);
 					} else {
 						entryPath = entryName;
 					}
 					if (entryPath.equals(packagePath)) {
 						entryName = entryName.replace("/", ".").substring(0,
 								entryName.lastIndexOf("."));
 						classNames.add(entryName);
 					}
 				}
 			}
 		}
 		jarFile.close();
 		return classNames;
 	}
 
 	private List<String> getClassNameByJars(URL[] jarFiles, String packagePath,
 			boolean searchInChildPackage) throws Exception {
 		List<String> classNames = new ArrayList<String>();
 		if (jarFiles != null) {
 			for (int i = 0; i < jarFiles.length; i++) {
 				URL jarFileUrl = jarFiles[i];
 				String jarFilePath = jarFileUrl.getPath();
 				if (jarFilePath.endsWith("classes/")) {
 					continue;
 				}
 				String jarPath = jarFilePath + "!/" + packagePath;
 				classNames.addAll(this.getClassNameByJar(jarPath,
 						searchInChildPackage));
 			}
 		}
 		return classNames;
 	}
 
 	public List<String> getClassNames(String packageName,
 			boolean searchInchildPackage) {
 		try {
 			List<String> classNames = null;
 			ClassLoader classLoader = Thread.currentThread()
 					.getContextClassLoader();
 			String packagePath = packageName.replace(".", "/");
 			URL packageUrl = classLoader.getResource(packagePath);
 			if (packageUrl != null) {
 				String protocol = packageUrl.getProtocol();
 				if (protocol.equals("file")) {
 					classNames = this.getClassNameByFile(packageName,
 							packageUrl.getPath(), null, searchInchildPackage);
 				} else if (protocol.equals("jar")) {
 					classNames = this.getClassNameByJar(packageUrl.getPath(),
 							searchInchildPackage);
 				} else {
 					classNames = null;
 				}
 			} else {
 				classNames = this.getClassNameByJars(
 						((URLClassLoader) classLoader).getURLs(), packagePath,
 						searchInchildPackage);
 			}
 			return classNames;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 }
