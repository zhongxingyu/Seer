 package org.pa.jmeupdatesite;
 
 import static java.util.Collections.list;
 import static org.apache.commons.lang3.Validate.notNull;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import com.google.common.collect.Sets;
 
 public class JMLib {
 
 	private final File jarFile;
 	private final ZipFile jarZip;
 
 	private Set<String> packageDependencies;
 	private Set<String> providedPackages;
 	private Set<String> referencedClassNames;
 	private Set<String> referencedPackageNames;
 	private Set<String> zipDirectories;
 
 	public JMLib(File jarFile) throws IOException {
 		this.jarFile = notNull(jarFile);
 		this.jarZip = new ZipFile(jarFile);
 	}
 
 	public File getJarFile() {
 		return jarFile;
 	}
 
 	public Set<String> getProvidedPackages() {
 		if (providedPackages == null) {
 			providedPackages = new HashSet<String>();
 			for (String directory : getDirectories()) {
 				if (hasClassFile(directory)) {
 					String packageName = directory.substring(0,
 							directory.length() - 1).replace('/', '.');
 					providedPackages.add(packageName);
 				}
 			}
 		}
 		return Collections.unmodifiableSet(providedPackages);
 	}
 
 	public Set<String> getReferencedClassNames() {
 		if (referencedClassNames == null) {
 			referencedClassNames = new HashSet<String>();
 
 			for (ZipEntry entry : list(jarZip.entries())) {
 				String name = entry.getName();
 				if (name.endsWith(".class")) {
 					try {
 						referencedClassNames.addAll(ClassBytesUtil
 								.findUsedSimpleClassNames(jarZip
 										.getInputStream(entry)));
 					} catch (IllegalArgumentException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 
 		return Collections.unmodifiableSet(referencedClassNames);
 	}
 
 	public Set<String> getReferencedPackageNames() {
 		if (referencedPackageNames == null) {
 			referencedPackageNames = new HashSet<String>();
 
 			for (ZipEntry entry : list(jarZip.entries())) {
 				String name = entry.getName();
 				if (name.endsWith(".class")) {
 					try {
 						referencedPackageNames.addAll(ClassBytesUtil
 								.findPackageNamesOfUsedClasses(jarZip
 										.getInputStream(entry)));
 					} catch (IllegalArgumentException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 
 		return Collections.unmodifiableSet(referencedPackageNames);
 	}
 
 	public Set<String> getPackageDependencies() {
 		if (packageDependencies == null) {
 			packageDependencies = Sets.difference(getReferencedPackageNames(),
 					getProvidedPackages());
 		}
 		return Collections.unmodifiableSet(packageDependencies);
 	}
 
 	private Set<String> getDirectories() {
 		if (zipDirectories == null) {
 			zipDirectories = new HashSet<String>();
 			for (ZipEntry entry : list(jarZip.entries())) {
 				String name = entry.getName();
 				if (name.endsWith("/")) {
 					zipDirectories.add(name);
 				}
 			}
 		}
 		return Collections.unmodifiableSet(zipDirectories);
 	}
 
 	private boolean hasClassFile(String directory) {
 		for (ZipEntry entry : list(jarZip.entries())) {
 			String name = entry.getName();
 			if (name.startsWith(directory)) {
 				String fileName = name.substring(directory.length());
 				if (fileName.indexOf('/') == -1 && fileName.endsWith(".class")) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public int hashCode() {
 		return getJarFile().hashCode();
 	}
 	
 	
 	@Override
 	public boolean equals(Object obj) {
 		return obj != null && obj.getClass() == JMLib.class && ((JMLib)obj).getJarFile().equals(getJarFile());
 	}
 
 }
