 package com.fathomdb.discovery;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.net.JarURLConnection;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Lists;
 
 public class ClasspathDiscovery extends Discovery {
 
 	private static final Logger log = LoggerFactory.getLogger(ClasspathDiscovery.class);
 
 	final ClassLoader classLoader;
 
 	private final List<String> packages;
 
 	private ClasspathMap map;
 
 	public ClasspathDiscovery(List<String> packages, ClassLoader classLoader) {
 		this.classLoader = classLoader;
 		this.packages = Lists.newArrayList();
 
 		for (String packageName : packages) {
 			this.packages.add(packageName);
 		}
 	}
 
 	public ClasspathDiscovery(List<String> packages) {
 		this(packages, ClasspathDiscovery.class.getClassLoader());
 	}
 
 	public ClasspathDiscovery(String... packages) {
 		this(Arrays.asList(packages));
 	}
 
 	class ClasspathMap {
 		private final List<Class> classes = Lists.newArrayList();
 
 		Collection<Class> findSubTypesOf(Class clazz) {
 			List<Class> ret = Lists.newArrayList();
 			for (Class c : classes) {
 				if (clazz.isAssignableFrom(c)) {
 					ret.add(c);
 				}
 			}
 			return ret;
 		}
 
 		void visitClasses() {
 			List<URL> urls = Lists.newArrayList();
 
 			if (classLoader instanceof URLClassLoader) {
 				URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
 				for (URL url : urlClassLoader.getURLs()) {
 					urls.add(url);
 				}
 				// String path = "";
 				// // inPackage.getName().replace('.', '/');
 				// Enumeration<URL> resource;
 				// try {
 				// resource = urlClassLoader.findResources(path);
 				// } catch (IOException e) {
 				// throw new
 				// IllegalStateException("Error doing class discovery", e);
 				// }
 				// while (resource.hasMoreElements()) {
 				// urls.add(resource.nextElement());
 				// }
 			} else {
 				throw new UnsupportedOperationException();
 			}
 
 			for (URL url : urls) {
 				log.debug("Discovery scanning url: {}", url);
 
 				String protocol = url.getProtocol();
 
 				if ("file".equals(protocol)) {
 					// TODO: Check type of URL??
 					String filePath = url.getFile();
 					if (filePath != null) {
 						File dir = new File(filePath);
 						if (!dir.exists()) {
 							log.info("Classpath entry not found: {}", dir);
 							continue;
 						}
 
 						if (dir.isDirectory()) {
 							walkDir(dir, "");
 						}
 
 						if (dir.isFile()) {
 							if (dir.getName().endsWith(".jar")) {
 								try {
 									JarFile jarFile = new JarFile(dir);
 									walkJar(jarFile);
 								} catch (IOException e) {
 									throw new IllegalStateException("Error reading entry: " + url, e);
 								}
 							} else {
 								log.warn("Unknown classpath entry: {}", dir);
 								// TODO: Ignore?
 								throw new UnsupportedOperationException();
 							}
 						}
 					} else {
 						throw new UnsupportedOperationException();
 					}
 				} else if ("jar".equals(protocol)) {
 					try {
 						JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
 
 						JarFile jar = jarConnection.getJarFile();
 						String prefix = ""; // inPackage.getName().replace('.',
 											// '/') + '/';
 
 						walkJar(jar);
 					} catch (IOException e) {
 						throw new IllegalStateException("Error reading entry: " + url, e);
 					}
 				} else {
 					throw new IllegalStateException("Unhandled protocol: " + protocol);
 				}
 			}
 		}
 
 		private void walkJar(JarFile jar) {
 			Enumeration<JarEntry> entries = jar.entries();
 			while (entries.hasMoreElements()) {
 				JarEntry jarEntry = entries.nextElement();
 
 				if (jarEntry.isDirectory()) {
 					continue;
 				}
 
 				String name = jarEntry.getName();
 				// log.debug("Discovery found entry: {}", name);
 				// System.out.println(name);
 
 				if (!name.endsWith(".class")) {
 					continue;
 				}
 
				String qualifiedName = name.replace(".class", "").replace('/', '.');
				if (isFiltered(qualifiedName)) {
 					// log.debug("Class filtered out: {}", name);
 					continue;
 				}
 				// if (!name.startsWith(prefix)) {
 				// // System.out.println("Does not start with " +
 				// // prefix);
 				// continue;
 				// }
 
 				try {
 					Class<?> clazz = Class.forName(qualifiedName);
 					visit(clazz);
 				} catch (Exception e) {
 					throw new IllegalStateException("Error loading class: " + qualifiedName, e);
 					// log.warn("Error loading class: " +
 					// qualifiedName,
 					// e);
 				}
 			}
 		}
 
 		private void visit(Class<?> clazz) {
 			// log.info("Found " + clazz.getName());
 			classes.add(clazz);
 		}
 
 		void walkDir(File dir, String packagePrefix) {
 			for (File f : dir.listFiles()) {
 				if (!f.isFile()) {
 					String subpackage = packagePrefix + f.getName() + ".";
 					walkDir(f, subpackage);
 					continue;
 				}
 				String name = f.getName();
 				if (!name.endsWith(".class")) {
 					continue;
 				}
 				String rawName = name.replace(".class", "");
 				String qualifiedName = packagePrefix + rawName;
 
 				if (isFiltered(qualifiedName)) {
 					continue;
 				}
 
 				try {
 					Class<?> clazz = Class.forName(qualifiedName);
 					visit(clazz);
 				} catch (Exception e) {
 					throw new IllegalStateException("Error loading class: " + qualifiedName, e);
 					// log.warn("Error loading class: " +
 					// qualifiedName,
 					// e);
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public <T> DiscoveredSubTypes<T> getSubTypesOf(final Class<T> clazz) {
 		final ClasspathMap map = getMap();
 		final Collection<Class> subTypes = map.findSubTypesOf(clazz);
 
 		return new DiscoveredSubTypes<T>() {
 			@Override
 			public Iterable<T> getInstances() {
 				return buildInstances(clazz, subTypes);
 			}
 		};
 	}
 
 	private synchronized ClasspathMap getMap() {
 		if (this.map == null) {
 			ClasspathMap map = new ClasspathMap();
 			map.visitClasses();
 			this.map = map;
 		}
 		return this.map;
 	}
 
 	public boolean isFiltered(String name) {
 		for (String packageName : packages) {
 			if (name.startsWith(packageName)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public <T extends Annotation> Collection<Class> findAnnotatedClasses(Class<T> class1) {
 		throw new UnsupportedOperationException();
 	}
 
	@Override
	public List<Class> findClassesInPackage(Package inPackage) {
		throw new UnsupportedOperationException();
	}

 }
