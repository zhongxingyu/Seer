 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.tools.internal;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.jar.JarFile;
 import java.util.zip.ZipEntry;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.Version;
 
 import org.eclipse.virgo.kernel.artifact.bundle.BundleBridge;
 import org.eclipse.virgo.kernel.artifact.library.LibraryBridge;
 import org.eclipse.virgo.kernel.artifact.library.LibraryDefinition;
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 import org.eclipse.virgo.repository.ArtifactBridge;
 import org.eclipse.virgo.repository.ArtifactDescriptor;
 import org.eclipse.virgo.repository.Attribute;
 import org.eclipse.virgo.repository.HashGenerator;
 import org.eclipse.virgo.repository.Repository;
 import org.eclipse.virgo.repository.RepositoryCreationException;
 import org.eclipse.virgo.repository.RepositoryFactory;
 import org.eclipse.virgo.repository.builder.ArtifactDescriptorBuilder;
 import org.eclipse.virgo.repository.configuration.ExternalStorageRepositoryConfiguration;
 import org.eclipse.virgo.repository.configuration.PropertiesRepositoryConfigurationReader;
 import org.eclipse.virgo.repository.configuration.RepositoryConfiguration;
 import org.eclipse.virgo.util.math.OrderedPair;
 import org.eclipse.virgo.util.osgi.VersionRange;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
 import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
 import org.eclipse.virgo.util.osgi.manifest.ExportPackage;
 import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
 
 public final class SystemPackageFilteringRepository {
 
     private final Map<String, Version> systemPackages;
 
     private final Repository mainRepository;
 
     private final Repository systemPackageRepository;
 
     private final Set<ArtifactDescriptor> jreProvidedDependenciesDescriptors;
 
     private final ArtifactDescriptor systemBundleDescriptor;
 
     private static final String SYSTEM_BUNDLE_SYMBOLIC_NAME = "org.eclipse.osgi";
     private static final String REPOSITORY_CONFIG_PATH = File.separatorChar + "config" + File.separatorChar + "org.eclipse.virgo.repository.properties";
 
     private static final String LIB_SEARCH_PATH = File.separatorChar + "lib" + File.separatorChar + "*.jar";
     
     public SystemPackageFilteringRepository(String serverHomePath, String[] additionalSearchPaths, String indexDirectoryPath,
         @SuppressWarnings("unused") EventLogger eventLogger, BundleContext bundleContext) throws IOException {
 
         String repositoryConfigPath = null;
         String serverProfilePath = null;
 
         if (serverHomePath != null) {
             repositoryConfigPath = serverHomePath + REPOSITORY_CONFIG_PATH;
             serverProfilePath = serverHomePath + File.separator + "lib" + File.separator + "java6-server.profile";
 
             File serverProfile = new File(serverProfilePath);
             if (!serverProfile.exists()) {
                 serverProfilePath = serverHomePath + File.separator + "lib" + File.separator + "server.profile";
             }
         }
 
         Set<ArtifactBridge> artifactBridges = createArtifactBridges();
 
         PropertiesRepositoryConfigurationReader configurationReader = new PropertiesRepositoryConfigurationReader(new File(indexDirectoryPath),
             artifactBridges, new SilentEventLogger(), null, new File(serverHomePath));
         
         List<RepositoryConfiguration> repositoryConfiguration = readRepositoryConfiguration(repositoryConfigPath, configurationReader);
 
         if (additionalSearchPaths != null) {
             String nameBase = "additional-sp-";
             int id = 1;
             for (String additionalSearchPath : additionalSearchPaths) {
                 String name = nameBase + id++;
                 repositoryConfiguration.add(new ExternalStorageRepositoryConfiguration(name, new File(indexDirectoryPath, name + ".index"),
                     artifactBridges, PropertiesRepositoryConfigurationReader.convertToAntStylePath(additionalSearchPath), null));
             }
         }
 
         try {
             this.mainRepository = createRepository(repositoryConfiguration, bundleContext);
             RepositoryConfiguration systemPackageRepositoryConfiguration = new ExternalStorageRepositoryConfiguration("system-repository", new File(
                 indexDirectoryPath, "system-repository.index"), artifactBridges, serverHomePath + LIB_SEARCH_PATH, null);
             this.systemPackageRepository = createRepository(systemPackageRepositoryConfiguration, bundleContext);
         } catch (RepositoryCreationException rce) {
             IOException exc = new IOException("A failure occurred during repository creation");
             exc.initCause(rce);
             throw exc;
         }
 
         systemPackages = EquinoxOsgiProfileParser.parseProfileForExportedPackages(serverProfilePath);
         systemPackages.putAll(findExportsFromOsgiImplementationBundle(new File(serverHomePath, "lib"), SYSTEM_BUNDLE_SYMBOLIC_NAME));
 
         jreProvidedDependenciesDescriptors = new HashSet<ArtifactDescriptor>();
         systemBundleDescriptor = new SystemBundleDescriptor(createBundleManifest(this.systemPackages));
         jreProvidedDependenciesDescriptors.add(systemBundleDescriptor);
     }
 
 	private List<RepositoryConfiguration> readRepositoryConfiguration(String repositoryConfigPath, PropertiesRepositoryConfigurationReader configurationReader) throws IOException {
 		OrderedPair<Map<String, RepositoryConfiguration>, List<String>> repositoryConfigurationPair;
         
 		FileInputStream fis = null;
         try {
         	Properties properties = new Properties();
         	fis = new FileInputStream(repositoryConfigPath);        	
         	properties.load(fis);
         	 
             repositoryConfigurationPair = configurationReader.readConfiguration(properties);
         } catch (Exception e) {
         	throw new RuntimeException("Failed to read repository configuration", e);
         } finally {
             if (fis != null) {
                 fis.close();
             }
         }
         
         List<RepositoryConfiguration> repositoryConfiguration = new ArrayList<RepositoryConfiguration>();
         
         for (String name : repositoryConfigurationPair.getSecond()) {
         	repositoryConfiguration.add(repositoryConfigurationPair.getFirst().get(name));
         }
         
 		return repositoryConfiguration;
 	}
     
     private static BundleManifest createBundleManifest(Map<String, Version> exportsMap) {
     	BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest();
     	Set<Entry<String, Version>> exports = exportsMap.entrySet();
     	ExportPackage exportPackage = bundleManifest.getExportPackage();
     	for (Entry<String, Version> export : exports) {
 			exportPackage.addExportedPackage(export.getKey()).setVersion(export.getValue());
 		}
     	return bundleManifest;
     }
 
     private Map<String, Version> findExportsFromOsgiImplementationBundle(File searchDirectory, String symbolicName) {
         Map<String, Version> exports = new HashMap<String, Version>();
 
         BundleManifest bundleManifest = findOsgiImplementationBundle(searchDirectory, symbolicName);
 
         if (bundleManifest != null) {
             for (ExportedPackage exportedPackage : bundleManifest.getExportPackage().getExportedPackages()) {
                 Version version = exportedPackage.getVersion();
                 exports.put(exportedPackage.getPackageName(), version);
             }
         }
 
         return exports;
     }
 
     private BundleManifest findOsgiImplementationBundle(File searchDirectory, String symbolicName) {
         File[] filesInDir = searchDirectory.listFiles();
         if (filesInDir != null) {
             for (File fileInDir : filesInDir) {
                 if (fileInDir.getName().endsWith(".jar")) {
                     Reader reader = null;
                     try {
                         JarFile jarFile = new JarFile(fileInDir);
                         ZipEntry manifestEntry = jarFile.getEntry(JarFile.MANIFEST_NAME);
                         reader = new InputStreamReader(jarFile.getInputStream(manifestEntry));
                         BundleManifest bundleManifest = BundleManifestFactory.createBundleManifest(reader);
                         if (symbolicName.equals(bundleManifest.getBundleSymbolicName().getSymbolicName())) {
                             return bundleManifest;
                         }
                     } catch (IOException ioe) {
                         if (reader != null) {
                             try {
                                 reader.close();
                             } catch (IOException ioe2) {
                             }
                         }
                     }
                 }
             }
         }
         return null;
     }
     
     public Set<ArtifactDescriptor> findByExportedPackage(String packageName, VersionRange versionRange) {
         Version version;
         if ((version = systemPackages.get(packageName)) != null) {
         	if (versionRange.includes(version)) {
         		Set<ArtifactDescriptor> descriptorsFromLib = findByExportedPackage(this.systemPackageRepository, packageName, versionRange);
                 if (descriptorsFromLib.isEmpty()) {
                     return jreProvidedDependenciesDescriptors;
                 } else {
                     Set<ArtifactDescriptor> systemBundleDescriptors = new HashSet<ArtifactDescriptor>();
                     for (ArtifactDescriptor descriptor : descriptorsFromLib) {
                         systemBundleDescriptors.add(descriptor);
                     }
                     return systemBundleDescriptors;
                 }
 
         	}
         }
         
         return findByExportedPackage(this.mainRepository, packageName, versionRange);	    	
     }
     
 	private Set<ArtifactDescriptor> findByExportedPackage(Repository repository, String packageName, VersionRange versionRange) {
 	    Set<? extends ArtifactDescriptor> allDescriptors = repository.createQuery(Constants.EXPORT_PACKAGE, packageName).run();
 		Set<ArtifactDescriptor> withinVersionRangeDescriptors = new HashSet<ArtifactDescriptor>();
 		for (ArtifactDescriptor descriptor : allDescriptors) {
 			BundleManifest manifest = BundleManifestUtils.createBundleManifest(descriptor);
 			for (ExportedPackage exportedPackage : manifest.getExportPackage().getExportedPackages()) {
 				if (versionRange.includes(exportedPackage.getVersion())) {
 					withinVersionRangeDescriptors.add(descriptor);
 					break;
 				}
 			}
 		}
 	
 		return withinVersionRangeDescriptors;
 	}
 
     private static Repository createRepository(List<RepositoryConfiguration> repositoryConfiguration, BundleContext bundleContext) throws RepositoryCreationException {
 
         RepositoryFactory repositoryFactory = getRepositoryFactory(bundleContext);
         return repositoryFactory.createRepository(repositoryConfiguration);
     }
 
     private static Repository createRepository(RepositoryConfiguration repositoryConfiguration, BundleContext bundleContext) throws RepositoryCreationException {
 
         RepositoryFactory repositoryFactory = getRepositoryFactory(bundleContext);
         return repositoryFactory.createRepository(repositoryConfiguration);        
     }
 
     private static RepositoryFactory getRepositoryFactory(BundleContext bundleContext) {
         RepositoryFactory repositoryFactory = null;
 
        ServiceReference<RepositoryFactory> serviceReference = bundleContext.getServiceReference(RepositoryFactory.class);
         if (serviceReference != null) {
             repositoryFactory = (RepositoryFactory)bundleContext.getService(serviceReference);
         }
         
         if (repositoryFactory == null) {
             throw new IllegalStateException("RepositoryFactory service was not available. Is the repository bundle installed and started?");
         }
         
         return repositoryFactory;
     }
 
     static Set<ArtifactBridge> createArtifactBridges() {
         Set<ArtifactBridge> artefactBridges = new HashSet<ArtifactBridge>();
         artefactBridges.add(new BundleBridge(new HashGenerator() {
             public void generateHash(ArtifactDescriptorBuilder artifactDescriptorBuilder, File artifactFile) {
                 // do nothing
             }
         }));
         artefactBridges.add(new LibraryBridge(new HashGenerator() {
             public void generateHash(ArtifactDescriptorBuilder artifactDescriptorBuilder, File artifactFile) {
                 // do nothing
             }
         }));
         return artefactBridges;
     }
     
     public ArtifactDescriptor get(String type, String name, VersionRange versionRange) {
 		return this.mainRepository.get(type, name, versionRange);
 	}
 	
 	public static final class SystemBundleDescriptor implements ArtifactDescriptor {
 
 		private static final String TYPE_BUNDLE = "bundle";
 		
 		private final BundleManifest bundleManifest;
 
 		public SystemBundleDescriptor(BundleManifest bundleManifest) {
 			this.bundleManifest = bundleManifest;
 		}
 
 		public Set<Attribute> getAttribute(String name) {
 			return null;
 		}
 
 		public Set<Attribute> getAttributes() {
 			return null;
 		}
 
 		public String getFilename() {
 			return null;
 		}
 
 		public String getName() {
 			return null;
 		}
 
 		public String getType() {
 			return TYPE_BUNDLE;
 		}
 
 		public java.net.URI getUri() {
 			return null;
 		}
 
 		public Version getVersion() {
 			return null;
 		}
 
 		public BundleManifest getBundleManifest() {
 			return this.bundleManifest;
 		}		
 	}
 
 	public Set<? extends ArtifactDescriptor> getBundles() {
 		Set<? extends ArtifactDescriptor> mainBundles = this.mainRepository.createQuery(ArtifactDescriptor.TYPE, BundleBridge.BRIDGE_TYPE).run();
 		Set<ArtifactDescriptor> combined = new HashSet<ArtifactDescriptor>(mainBundles);
 		combined.add(this.systemBundleDescriptor);
 		return combined;
 	}
 
 	public Set<? extends ArtifactDescriptor> getLibraries() {
 		return this.mainRepository.createQuery(ArtifactDescriptor.TYPE, LibraryDefinition.LIBRARY_TYPE).run();
 	}
 	
 	public void shutdown() {
 		this.mainRepository.stop();
 		this.systemPackageRepository.stop();
 	}
 }
