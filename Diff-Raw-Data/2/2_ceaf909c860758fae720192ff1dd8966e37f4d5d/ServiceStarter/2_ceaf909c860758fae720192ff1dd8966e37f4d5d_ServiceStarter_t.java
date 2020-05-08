 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.boot;
 
 import net.jini.config.*;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.AbstractFileFilter;
 import org.apache.commons.io.filefilter.DirectoryFileFilter;
 import org.rioproject.impl.opstring.OpStringLoader;
 import org.rioproject.opstring.OperationalString;
 import org.rioproject.opstring.ServiceElement;
 import org.rioproject.resolver.Artifact;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import sorcer.core.SorcerConstants;
 import sorcer.core.SorcerEnv;
 import sorcer.provider.boot.AbstractServiceDescriptor;
 import sorcer.resolver.Resolver;
 import sorcer.util.IOUtils;
 import sorcer.util.JavaSystemProperties;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.rmi.RMISecurityManager;
 import java.util.*;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import static sorcer.provider.boot.AbstractServiceDescriptor.Created;
 
 /**
  * @author Rafał Krupiński
  */
 public class ServiceStarter {
     final private static Logger log = LoggerFactory.getLogger(ServiceStarter.class);
 	public static final String CONFIG_RIVER = "config/services.config";
     private static final String SUFFIX_RIVER = "config";
     final public static File SORCER_DEFAULT_CONFIG = new File(SorcerEnv.getHomeDir(), "configs/sorcer-boot.config");
     private sorcer.com.sun.jini.start.ServiceStarter riverServiceStarter = new sorcer.com.sun.jini.start.ServiceStarter();
 
 	public static void main(String[] args) throws Exception {
         System.setSecurityManager(new RMISecurityManager());
 		new ServiceStarter().doMain(args);
 	}
 
 	private void doMain(String[] args) throws Exception {
 		loadDefaultProperties();
         List<String> configs = new LinkedList<String>(Arrays.asList(args));
 		if (configs.isEmpty()) {
             configs.add(SORCER_DEFAULT_CONFIG.getPath());
 		}
 		start(configs);
 	}
 
 	private void loadDefaultProperties() {
 		String sorcerHome = SorcerEnv.getHomeDir().getPath();
 		setDefaultProperty(JavaSystemProperties.PROTOCOL_HANDLER_PKGS, "net.jini.url|sorcer.util.bdb|org.rioproject.url");
 		setDefaultProperty(JavaSystemProperties.UTIL_LOGGING_CONFIG_FILE, sorcerHome + "/configs/sorcer.logging");
 		setDefaultProperty(SorcerConstants.S_KEY_SORCER_ENV, sorcerHome + "/configs/sorcer.env");
 	}
 
 	private void setDefaultProperty(String key, String value) {
 		String userValue = System.getProperty(key);
 		if (userValue == null) {
 			System.setProperty(key, value);
 		}
 	}
 
 	/**
 	 * Start services from the configs
 	 *
 	 * @param configs file path or URL of the services.config configuration
 	 */
     public void start(Collection<String> configs) throws Exception {
         List<String> riverServices = new LinkedList<String>();
         List<File> cfgJars = new LinkedList<File>();
         List<File> opstrings = new LinkedList<File>();
 
         for (String path : configs) {
             File file = null;
             if (path.startsWith(":")) {
                 file = findArtifact(path.substring(1));
             } else if (Artifact.isArtifact(path))
                 file = new File(Resolver.resolveAbsolute(path));
             if (file == null) file = new File(path);
 
             IOUtils.ensureFile(file, IOUtils.FileCheck.readable);
             path = file.getPath();
             String ext = path.substring(path.lastIndexOf('.') + 1);
 
             if (file.isDirectory())
                 riverServices.add(findConfigUrl(path).toExternalForm());
             else if (SUFFIX_RIVER.equals(ext))
                 riverServices.add(path);
             else if ("oar".equals(ext) || "jar".equals(ext))
                 cfgJars.add(file);
             else if ("opstring".equals(ext) || "groovy".equals(ext))
                 opstrings.add(file);
             else
                 throw new IllegalArgumentException("Unrecognized file " + path);
         }
         if (!riverServices.isEmpty())
            riverServiceStarter.startServicesFromPaths(riverServices.toArray(new String[riverServices.size()]));
         if (!cfgJars.isEmpty() || !opstrings.isEmpty())
             startRioStyleServices(cfgJars, opstrings);
     }
 
     private void startRioStyleServices(List<File> cfgJars, List<File> opstrings) throws Exception {
         List<OpstringServiceDescriptor> serviceDescriptors = createFromOpStrFiles(opstrings);
         serviceDescriptors.addAll(createFromOar(cfgJars));
         startServices(serviceDescriptors);
     }
 
     private File findArtifact(String artifactId) {
         Collection<File> files = FileUtils.listFiles(new File(System.getProperty("user.dir")), new ArtifactIdFileFilter(artifactId), DirectoryFileFilter.INSTANCE);
         if (files.size() == 0) {
             log.error("Artifact file {} not found", artifactId);
             return null;
         }
         if (files.size() > 1) {
             log.warn("Found {} files possibly matching artifactId, using the first", files.size());
         }
         return files.iterator().next();
     }
 
     protected List<OpstringServiceDescriptor> createFromOpStrFiles(Collection<File> files) throws Exception {
         List<OpstringServiceDescriptor> result = new LinkedList<OpstringServiceDescriptor>();
         String policyFile = System.getProperty(JavaSystemProperties.SECURITY_POLICY);
         URL policyFileUrl = new File(policyFile).toURI().toURL();
         OpStringLoader loader = new OpStringLoader();
         for (File opString : files) {
             OperationalString[] operationalStrings = loader.parseOperationalString(opString);
             result.addAll(createServiceDescriptors(operationalStrings, policyFileUrl));
         }
         return result;
     }
 
     private List<OpstringServiceDescriptor> createFromOar(Iterable<File> oarFiles) throws Exception {
         List<OpstringServiceDescriptor> result = new LinkedList<OpstringServiceDescriptor>();
         for (File oarFile : oarFiles) {
             SorcerOAR oar = new SorcerOAR(oarFile);
             OperationalString[] operationalStrings = oar.loadOperationalStrings();
             URL policyFile = oar.getPolicyFile();
             result.addAll(createServiceDescriptors(operationalStrings, policyFile));
         }
         return result;
     }
 
     private List<OpstringServiceDescriptor> createServiceDescriptors(OperationalString[] operationalStrings, URL policyFile) throws ConfigurationException {
         List<OpstringServiceDescriptor> descriptors = new LinkedList<OpstringServiceDescriptor>();
         for (OperationalString op : operationalStrings) {
             for (ServiceElement se : op.getServices()) {
                 descriptors.add(new OpstringServiceDescriptor(se, policyFile));
             }
 
             descriptors.addAll(createServiceDescriptors(op.getNestedOperationalStrings(), policyFile));
         }
         return descriptors;
     }
 
     private List<Created> startServices(List<OpstringServiceDescriptor> services) {
         List<Created> result = new ArrayList<Created>(services.size());
         for (AbstractServiceDescriptor descriptor : services) {
             try {
                 result.add(descriptor.create(EmptyConfiguration.INSTANCE));
             } catch (Exception x) {
                 log.warn("Error while creating a service from {}", descriptor, x);
             }
         }
         return result;
     }
 
     private URL findConfigUrl(String path) throws IOException {
         File configFile = new File(path);
         if (configFile.isDirectory()) {
             return new File(configFile, CONFIG_RIVER).toURI().toURL();
         } else if (path.endsWith(".jar")) {
             ZipEntry entry = new ZipFile(path).getEntry(CONFIG_RIVER);
             if (entry != null) {
                 return new URL(String.format("jar:file:%1$s!/%2$s", path, CONFIG_RIVER));
             }
         }
         return new File(path).toURI().toURL();
     }
 
     private static class ArtifactIdFileFilter extends AbstractFileFilter {
         private String artifactId;
 
         public ArtifactIdFileFilter(String artifactId) {
             this.artifactId = artifactId;
         }
 
         @Override
         public boolean accept(File dir, String name) {
             String parent = dir.getName();
             String grandParent = dir.getParentFile().getName();
             return
                     name.startsWith(artifactId + "-") && name.endsWith(".jar") && (
                             //check development structure
                             "target".equals(parent)
                                     //check repository just in case
                                     || artifactId.equals(grandParent)
                     )
                             //check distribution structure
                             || "lib".equals(grandParent) && (artifactId + ".jar").equals(name)
                     ;
         }
     }
 }
