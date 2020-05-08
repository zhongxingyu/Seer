 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 package net.sourceforge.eclipsejetty.launch;
 
 import static net.sourceforge.eclipsejetty.launch.JettyLaunchClasspathMatcher.and;
 import static net.sourceforge.eclipsejetty.launch.JettyLaunchClasspathMatcher.isIncluded;
 import static net.sourceforge.eclipsejetty.launch.JettyLaunchClasspathMatcher.not;
 import static net.sourceforge.eclipsejetty.launch.JettyLaunchClasspathMatcher.notExcluded;
 import static net.sourceforge.eclipsejetty.launch.JettyLaunchClasspathMatcher.or;
 import static net.sourceforge.eclipsejetty.launch.JettyLaunchClasspathMatcher.userClasses;
 import static net.sourceforge.eclipsejetty.launch.JettyLaunchClasspathMatcher.withExtraAttribute;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 import net.sourceforge.eclipsejetty.JettyPlugin;
 import net.sourceforge.eclipsejetty.JettyPluginConstants;
 import net.sourceforge.eclipsejetty.JettyPluginUtils;
 import net.sourceforge.eclipsejetty.common.AbstractServerConfiguration;
 import net.sourceforge.eclipsejetty.common.ContainerConfig;
 import net.sourceforge.eclipsejetty.common.ContainerVersion;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
 import org.eclipse.jdt.launching.JavaLaunchDelegate;
 import org.eclipse.jdt.launching.JavaRuntime;
 
 /**
  * Launch configuration delegate for Jetty. Based on {@link JavaLaunchDelegate}.
  * 
  * @author Christian K&ouml;berl
  * @author Manfred Hantschel
  */
 public class JettyLaunchConfigurationDelegate extends JavaLaunchDelegate
 {
     public static final String CONFIGURATION_KEY = "jetty.launcher.configuration";
     public static final String HIDE_LAUNCH_INFO_KEY = "jetty.launcher.hideLaunchInfo";
 
     public JettyLaunchConfigurationDelegate()
     {
         super();
     }
 
     @Override
     public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
         throws CoreException
     {
         super.launch(configuration, mode, launch, monitor);
     }
 
     @Override
     public String verifyMainTypeName(ILaunchConfiguration configuration) throws CoreException
     {
         return JettyPluginConstants.getContainerMainTypeName(configuration);
     }
 
     @Override
     public String getVMArguments(ILaunchConfiguration configuration) throws CoreException
     {
         String[] webappClasspath =
             getLocalWebappClasspath(configuration,
                 getWebappClasspathEntries(configuration, getOriginalClasspathEntries(configuration)));
         
         // get the container type, version and config file location
         String selectedContainer = JettyPluginConstants.getContainerSelected(configuration);
         
         String vmArguments=null;
         if( selectedContainer.equals(JettyPluginConstants.ATTR_CONTAINER_JETTY) ){
         	vmArguments = getVMArgumentsForJetty(configuration, webappClasspath);
         }
         if( selectedContainer.equals(JettyPluginConstants.ATTR_CONTAINER_TOMCAT) ){
         	vmArguments = getVMArgumentsForTomcat(configuration, webappClasspath);
         }
 
         return vmArguments;
     }
     
     public String getVMArgumentsForJetty( ILaunchConfiguration configuration, 
     										String[] webappClasspath ) throws CoreException
     {
         final ContainerVersion jettyVersion = JettyPluginConstants.getVersion(configuration);
         File defaultFile = createJettyConfigurationFile(configuration, jettyVersion, webappClasspath);
         
         String vmArguments = super.getVMArguments(configuration);
         vmArguments += " -D" + CONFIGURATION_KEY + "=" + getConfigurationParameter(configuration, defaultFile);
 
         if (!JettyPluginConstants.isShowLauncherInfo(configuration))
         {
             vmArguments += " -D" + HIDE_LAUNCH_INFO_KEY;
         }
 
         return vmArguments;
     }
     
     public String getVMArgumentsForTomcat( ILaunchConfiguration configuration, 
     										String[] webappClasspath ) throws CoreException
     {
         final ContainerVersion tomcatVersion = JettyPluginConstants.getVersion(configuration);
         File defaultFile = createTomcatConfigurationFile(configuration, tomcatVersion, webappClasspath);
         
         String vmArguments = super.getVMArguments(configuration);
         //vmArguments += " -D" + CONFIGURATION_KEY + "=" + getConfigurationParameter(configuration, defaultFile);
         
         String tomcatPath = JettyPluginConstants.getTomcatPath(configuration);
         vmArguments += " -Djava.util.logging.config.file=" + tomcatPath + File.separator + "conf" + File.separator + "logging.properties";
         vmArguments += " -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager";
         vmArguments += " -Djava.endorsed.dirs="+ tomcatPath + File.separator + "endorsed";
         vmArguments += " -Dcatalina.home=" + tomcatPath;
         vmArguments += " -Dcataliba.base=" + tomcatPath;
         vmArguments += " -Djava.io.tmpdir=" + tomcatPath + File.separator + "temp";
 
         if (!JettyPluginConstants.isShowLauncherInfo(configuration))
         {
             vmArguments += " -D" + HIDE_LAUNCH_INFO_KEY;
         }
 
         return vmArguments;    	
     }
 
     private String getConfigurationParameter(ILaunchConfiguration configuration, File defaultFile) throws CoreException
     {
         StringBuilder configurationParam = new StringBuilder();
         List<ContainerConfig> configs = JettyPluginConstants.getConfigs(configuration);
 
         for (ContainerConfig config : configs)
         {
             if (config.isActive())
             {
                 if (configurationParam.length() > 0)
                 {
                     configurationParam.append(File.pathSeparator);
                 }
 
                 IFile file = config.getFile(ResourcesPlugin.getWorkspace());
 
                 if (file != null)
                 {
                     configurationParam.append(file.getLocation().toOSString());
                 }
                 else
                 {
                     configurationParam.append(defaultFile.getAbsolutePath());
                 }
             }
         }
 
         return configurationParam.toString();
     }
 
     @Override
     public String[] getClasspath(ILaunchConfiguration configuration) throws CoreException
     {
         return JettyPluginUtils.toLocationArray(getContainerClasspath(
             configuration,
             getGlobalWebappClasspathEntries(configuration,
                 getWebappClasspathEntries(configuration, getOriginalClasspathEntries(configuration)))));
     }
 
     public Collection<IRuntimeClasspathEntry> getOriginalClasspathEntries(ILaunchConfiguration configuration)
         throws CoreException
     {
         IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(configuration);
 
         entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);
         Collection<IRuntimeClasspathEntry> matchedEntries =
             userClasses().match(new LinkedHashSet<IRuntimeClasspathEntry>(Arrays.asList(entries)));
 
         //        System.out.println("Classpath Entries");
         //        System.out.println("=================");
         //        for (IRuntimeClasspathEntry entry : matchedEntries)
         //        {
         //            if (entry.getLocation().contains("antlr-runtime"))
         //            {
         //                System.out.println(entry.getLocation());
         //                System.out.println("\t Classpath Property: " + entry.getClasspathProperty());
         //                System.out.println("\t Type: " + entry.getType());
         //                System.out.println("\t Access Rules: " + Arrays.toString(entry.getClasspathEntry().getAccessRules()));
         //                System.out.println("\t Inclusion Pattern: "
         //                    + Arrays.toString(entry.getClasspathEntry().getInclusionPatterns()));
         //                System.out.println("\t Exclusion Pattern: "
         //                    + Arrays.toString(entry.getClasspathEntry().getExclusionPatterns()));
         //                System.out.println("\t Exported: " + entry.getClasspathEntry().isExported());
         //                System.out.println("\t Referencing Entry: " + entry.getClasspathEntry().getReferencingEntry());
         //                System.out.println("\t Extra Attributes"
         //                    + Arrays.toString(entry.getClasspathEntry().getExtraAttributes()));
         //            }
         //        }
         //        System.out.println("----------------------------------------------------------------------");
 
         return matchedEntries;
     }
 
     public String[] getOriginalClasspath(ILaunchConfiguration configuration) throws CoreException
     {
         return JettyPluginUtils.toLocationArray(getOriginalClasspathEntries(configuration));
     }
 
     public Collection<IRuntimeClasspathEntry> getWebappClasspathEntries(ILaunchConfiguration configuration,
         Collection<IRuntimeClasspathEntry> originalEntries) throws CoreException
     {
         return and(createWebappClasspathMatcher(configuration)).match(
             new LinkedHashSet<IRuntimeClasspathEntry>(originalEntries));
     }
 
     public String[] getWebappClasspath(ILaunchConfiguration configuration,
         Collection<IRuntimeClasspathEntry> originalEntries) throws CoreException
     {
         return JettyPluginUtils.toLocationArray(getWebappClasspathEntries(configuration, originalEntries));
     }
 
     public Collection<IRuntimeClasspathEntry> getLocalWebappClasspathEntries(ILaunchConfiguration configuration,
         Collection<IRuntimeClasspathEntry> webappEntries) throws CoreException
     {
         String globalLibs = JettyPluginConstants.getGlobalLibs(configuration);
 
         if ((globalLibs == null) || (globalLibs.trim().length() <= 0))
         {
             return new LinkedHashSet<IRuntimeClasspathEntry>(webappEntries);
         }
 
         return notExcluded(globalLibs.split("[,\\n\\r]")).match(
             new LinkedHashSet<IRuntimeClasspathEntry>(webappEntries));
     }
 
     public String[] getLocalWebappClasspath(ILaunchConfiguration configuration,
         Collection<IRuntimeClasspathEntry> webappEntries) throws CoreException
     {
         return JettyPluginUtils.toLocationArray(getLocalWebappClasspathEntries(configuration, webappEntries));
     }
 
     public Collection<IRuntimeClasspathEntry> getGlobalWebappClasspathEntries(ILaunchConfiguration configuration,
         Collection<IRuntimeClasspathEntry> webappEntries) throws CoreException
     {
         String globalLibs = JettyPluginConstants.getGlobalLibs(configuration);
 
         if ((globalLibs == null) || (globalLibs.trim().length() <= 0))
         {
             return Collections.<IRuntimeClasspathEntry> emptyList();
         }
 
         return isIncluded(globalLibs.split("[,\\n\\r]"))
             .match(new LinkedHashSet<IRuntimeClasspathEntry>(webappEntries));
     }
 
     public String[] getGlobalWebappClasspath(ILaunchConfiguration configuration,
         Collection<IRuntimeClasspathEntry> webappEntries) throws CoreException
     {
         return JettyPluginUtils.toLocationArray(getGlobalWebappClasspathEntries(configuration, webappEntries));
     }
 
     private static IRuntimeClasspathEntry[] getContainerClasspath(final ILaunchConfiguration configuration,
     																final Collection<IRuntimeClasspathEntry> collection) 
     																		throws CoreException
     {
         final List<IRuntimeClasspathEntry> entries = new ArrayList<IRuntimeClasspathEntry>();
 
         if (collection != null)
         {
             entries.addAll(collection);
         }
 
        
         // first detect the container
          String containerPath=null;
         String selectedContainer = JettyPluginConstants.getContainerSelected(configuration);
         if( selectedContainer.equals(JettyPluginConstants.ATTR_CONTAINER_JETTY) ){
         	containerPath = JettyPluginUtils.resolveVariables(JettyPluginConstants.getJettyPath(configuration));
         }
         else if( selectedContainer.equals(JettyPluginConstants.ATTR_CONTAINER_TOMCAT) ){
         	containerPath = JettyPluginUtils.resolveVariables(JettyPluginConstants.getTomcatPath(configuration));
         }
         
          
         final ContainerVersion jettyVersion = JettyPluginConstants.getVersion(configuration);
         boolean jspSupport = JettyPluginConstants.isJspSupport(configuration);
         boolean jmxSupport = JettyPluginConstants.isJmxSupport(configuration);
         boolean jndiSupport = JettyPluginConstants.isJndiSupport(configuration);
         boolean ajpSupport = JettyPluginConstants.isAjpSupport(configuration);
 
         try
         {
         	
         	URL bundleurl = FileLocator.find(
         							JettyPlugin.getDefault().getBundle(),
         							Path.fromOSString("lib/eclipse-jetty-starters-common.jar"), 
         							null);
         	
         	URL fileurl = FileLocator.toFileURL(bundleurl);
         	Path path= new Path(fileurl.getFile());
         	IRuntimeClasspathEntry classPathEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(path);
         	entries.add( classPathEntry );
                     
 //            entries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(FileLocator.toFileURL(
 //                FileLocator.find(JettyPlugin.getDefault().getBundle(),
 //                    Path.fromOSString("lib/eclipse-jetty-starters-common.jar"), null)).getFile())));
 
         	
         	URL iurl = FileLocator.find(
         				JettyPlugin.getDefault().getBundle(), 
 					  	Path.fromOSString(jettyVersion.getJar()), 
 					  	null);
         	URL furl = FileLocator.toFileURL(iurl);
         	Path path1 = new Path( furl.getFile() );
         	IRuntimeClasspathEntry cpentry = JavaRuntime.newArchiveRuntimeClasspathEntry(path1);
         	entries.add( cpentry);
         	
 //            entries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(FileLocator.toFileURL(
 //                FileLocator.find(JettyPlugin.getDefault().getBundle(), Path.fromOSString(jettyVersion.getJar()), null))
 //                .getFile())));
 
             for (final File jettyLib : jettyVersion.getLibStrategy().find(new File(containerPath), jspSupport, jmxSupport,
                 jndiSupport, ajpSupport))
             {
                 entries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(jettyLib.getCanonicalPath())));
             }
         }
         catch (final IOException e)
         {
             JettyPlugin.logError(e);
         }
 
         return entries.toArray(new IRuntimeClasspathEntry[entries.size()]);
     }
 
     private JettyLaunchClasspathMatcher createWebappClasspathMatcher(final ILaunchConfiguration configuration)
         throws CoreException
     {
         JettyLaunchClasspathMatcher vmClasspathMatcher = userClasses();
         String excludedLibs = JettyPluginConstants.getExcludedLibs(configuration);
 
         if ((excludedLibs != null) && (excludedLibs.trim().length() > 0))
         {
             vmClasspathMatcher = and(vmClasspathMatcher, notExcluded(excludedLibs.split("[,\\n\\r]")));
         }
 
         if (JettyPluginConstants.isScopeCompileExcluded(configuration))
         {
             vmClasspathMatcher = and(vmClasspathMatcher, not(withExtraAttribute("maven.scope", "compile")));
         }
 
         if (JettyPluginConstants.isScopeProvidedExcluded(configuration))
         {
             vmClasspathMatcher = and(vmClasspathMatcher, not(withExtraAttribute("maven.scope", "provided")));
         }
 
         if (JettyPluginConstants.isScopeRuntimeExcluded(configuration))
         {
             vmClasspathMatcher = and(vmClasspathMatcher, not(withExtraAttribute("maven.scope", "runtime")));
         }
 
         if (JettyPluginConstants.isScopeSystemExcluded(configuration))
         {
             vmClasspathMatcher = and(vmClasspathMatcher, not(withExtraAttribute("maven.scope", "system")));
         }
 
         if (JettyPluginConstants.isScopeTestExcluded(configuration))
         {
             vmClasspathMatcher =
                 and(vmClasspathMatcher, not(withExtraAttribute("maven.scope", "test")), notExcluded(".*/test-classes"));
         }
 
         String includedLibs = JettyPluginConstants.getIncludedLibs(configuration);
 
         if ((includedLibs != null) && (includedLibs.trim().length() > 0))
         {
             vmClasspathMatcher = or(isIncluded(includedLibs.split("[,\\n\\r]")), vmClasspathMatcher);
         }
 
         return vmClasspathMatcher;
     }
 
     private File createJettyConfigurationFile(ILaunchConfiguration configuration, 
     											ContainerVersion version,
     											String[] classpath) throws CoreException
     {
         AbstractServerConfiguration serverConfiguration = version.createServerConfiguration();
 
         serverConfiguration.setDefaultContextPath(JettyPluginConstants.getContext(configuration));
         serverConfiguration.setDefaultWar(JettyPluginConstants.getWebAppDir(configuration));
         serverConfiguration.setPort(Integer.valueOf(JettyPluginConstants.getPort(configuration)));
         serverConfiguration.setJndi(JettyPluginConstants.isJndiSupport(configuration));
         serverConfiguration.addDefaultClasspath(classpath);
 
         File file;
 
         try
         {
             file = File.createTempFile("jettyLauncherConfiguration", ".xml");
 
             serverConfiguration.write(file);
         }
         catch (IOException e)
         {
             throw new CoreException(new Status(IStatus.ERROR, JettyPlugin.PLUGIN_ID,
                 "Failed to store tmp file with Jetty launch configuration"));
         }
 
         return file;
     }
     
     /* Generates the context.xml file needed for deploying the application in tomcat.
      * <code>
      * <Context path="/mywebapp" docBase="/Users/theuser/mywebapp/src/main/webapp" >
 			<Resources className="org.apache.naming.resources.VirtualDirContext"
 				extraResourcePaths="/WEB-INF/classes=/Users/theuser/mywebapp/target/classes"/>
 			<Loader className="org.apache.catalina.loader.VirtualWebappLoader"
 				virtualClasspath="/Users/theuser/mywebapp/target/classes;/Users/theuser/mylib/target/classes;/Users/theuser/.m2/repository/log4j/log4j/1.2.15/log4j-1.2.15.jar"
 			/>
 			<JarScanner scanAllDirectories="true" />
 		</Context>
      * </code>
      */
     private File createTomcatConfigurationFile(ILaunchConfiguration configuration, 
 												ContainerVersion version,
 												String[] classpath) throws CoreException{
    
     	
     	
     	AbstractServerConfiguration serverConfiguration = version.createServerConfiguration();
     	
     	String tomcatPath = JettyPluginConstants.getTomcatPath(configuration);
     	
     	// the file name is the name of the context
     	String contextPath = JettyPluginConstants.getContext(configuration);
     	if( contextPath != null && !contextPath.trim().equals("")){
     		if( contextPath.trim().equals("/") ||
     				contextPath.trim().toLowerCase().equals("/root") ||
     				contextPath.trim().toLowerCase().equals("root") ){
     			contextPath = "ROOT";
     		}
     		
     		if( contextPath.startsWith("/")){
     			contextPath = contextPath.substring(1);
     		}
     		
     	}else{
     		contextPath = "ROOT";
     	}
     	
     	File file = new File( tomcatPath , "conf/Catalina/localhost/" + contextPath + ".xml");
     	
     	// if the file exists, rewrite it
     	if( file.exists() ){
     		file.delete();
     	}
     	// check if the parent dirs exist
     	File parentDirs = file.getParentFile();
     	if( !parentDirs.exists()){
     		parentDirs.mkdirs();
     	}
     	
     	// get absolute path for webapp folder.
//    	// TODO: fix this. this should be derived from project root, not from working dir path
//    	String path = getWorkingDirectory(configuration).getAbsolutePath();
     	IJavaProject project = getJavaProject(configuration);
     	String projectname = getResourceUri(project.getResource());
     	
     	String projectRoot =  project.getResource().getLocation().toString();
    	File webappdir = new File( projectRoot, JettyPluginConstants.getWebAppDir(configuration) );
    	
    	
     	String outputFolder =  project.getOutputLocation().makeAbsolute().toString(); 
     	String outputDir = null;
     	if( outputFolder.startsWith(projectname) ){
     		outputDir = outputFolder.substring(projectname.length());
     	}else{
     		outputDir = outputFolder;
     	}
     	
     	String absOutDir = projectRoot + outputDir;
     	
     	serverConfiguration.setDefaultContextPath(JettyPluginConstants.getContext(configuration));
     	serverConfiguration.setDefaultWar( webappdir.getAbsolutePath());
     	serverConfiguration.addDefaultClasspath(classpath);
     	serverConfiguration.setOutputFolder(absOutDir);
     	
     	// TODO: Port number, Jndi, Catalina_base and additional features
     	
     	try {
 			serverConfiguration.write(file);
 		} catch (IOException e) {
 			throw new CoreException( new Status(IStatus.ERROR, JettyPlugin.PLUGIN_ID,
 										"Failed to write tomcat context file at "+ file.getAbsolutePath() ));
 		}
     	
     	return file;
     	
     }
     
     
     
 	public String getResourceUri(IResource resource) {
 
 		String resourceName = resource.getName();
 		IPath parentPath = resource.getParent().getProjectRelativePath();
 
 		String[] segments = parentPath.segments();
 		StringBuilder sb = new StringBuilder();
 		sb.append("/");
 		if (segments.length > 1) {
 
 			for (int idx = 1; idx < segments.length; idx++) {
 				sb.append(segments[idx]);
 				sb.append("/");
 			}
 		}
 		sb.append(resourceName);
 		return sb.toString().substring(0, sb.toString().length());
 	}
 
     
 
 }
