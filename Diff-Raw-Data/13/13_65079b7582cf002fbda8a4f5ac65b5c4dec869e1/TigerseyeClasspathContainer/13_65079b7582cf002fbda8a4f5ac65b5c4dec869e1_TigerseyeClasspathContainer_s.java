 package de.tud.stg.popart.builder.eclipse;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 import org.osgi.framework.Bundle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.tud.stg.tigerseye.TigerseyeLibraryProvider;
 import de.tud.stg.tigerseye.core.DSLDefinition;
 import de.tud.stg.tigerseye.core.TigerseyeCore;
 
 //FIXME refactoring and tests
 public class TigerseyeClasspathContainer implements IClasspathContainer {
     private static final Logger logger = LoggerFactory
 	    .getLogger(TigerseyeClasspathContainer.class);
 
     private final Set<IClasspathEntry> cpEntries = new HashSet<IClasspathEntry>();
 
     private final IPath containerPath;
 
     public TigerseyeClasspathContainer(IPath containerPath) {
 	this.containerPath = containerPath;
 
 	File allRuntimeJars = TigerseyeLibraryProvider
 		.getTigerseyeRuntimeLibraryFolder();
 	File[] runtimeJars = allRuntimeJars.listFiles(new FilenameFilter() {
 	    @Override
 	    public boolean accept(File parentFile, String fileName) {
 		return fileName.endsWith(".jar");
 	    }
 	});
 	for (File jar : runtimeJars) {
 	    Path path = new Path(jar.getAbsolutePath());
 	    IClasspathEntry entry2 = JavaCore.newLibraryEntry(path, null, null);
 	    cpEntries.add(entry2);
 	}
 
 	List<DSLDefinition> dslDefinitions = TigerseyeCore
 		.getLanguageProvider().getDSLDefinitions();
 	Set<String> iteratedContributor = new HashSet<String>();
 	for (DSLDefinition dslDefinition : dslDefinitions) {
 	    if (dslDefinition.isActive()
 		    && !iteratedContributor.contains(dslDefinition
 			    .getContributorSymbolicName())) {
 		try {
 		    addProjectPathForDSL(dslDefinition);
 		} catch (IOException e) {
 		    logger.error("Failed to add dsl  {} to classpath",
 			    dslDefinition, e);
 		}
 		iteratedContributor.add(dslDefinition
 			.getContributorSymbolicName());
 	    }
 	}
 
 	logger.trace("added classpathentries: {}", this.cpEntries);
     }
 
     private void addProjectPathForDSL(DSLDefinition dslDefinition)
 	    throws IOException {
 	String contributorSymbolicName = dslDefinition
 		.getContributorSymbolicName();
 	Bundle bundle = Platform.getBundle(contributorSymbolicName);
 	File bundleFile = FileLocator.getBundleFile(bundle);
 	File buildProps = new File(bundleFile, "build.properties");
 	Properties properties = new Properties();
 	properties.load(new FileInputStream(buildProps));
 	String binFolder = properties.getProperty("output..", null);
 	if (binFolder != null) {
 	    File file = new File(bundleFile, binFolder);
 	    addFileAsCPEntryIfExistant(file);
 	}
 	String includes = properties.getProperty("bin.includes", null);
 	if (includes != null) {
 	    String[] includedFileNames = includes.split(",");
 	    for (String string : includedFileNames) {
 		File file = new File(bundleFile, string);
 		if (validCombination(
 			file, binFolder)) {
 		    addFileAsCPEntryIfExistant(file);
 		}
 	    }
 	}
     }
 
     private boolean validCombination(File file, String binFolder) {
 	if (binFolder != null) {
 	    /*
 	     * If class output folder exists it can not be nested inside a
 	     * project root folder which as well is declared as class folder.
 	     * Such a combination will cause unresolved class path problems. I
 	     * assume that the relevant class folder is the specifically as
 	     * "output.." property defined folder.
 	     */
	    file.getName().equals(".");
	    return false;
 	}
 	return true;
     }
 
     private void addFileAsCPEntryIfExistant(File cpFile) {
 	if (cpFile.exists()) {
 	    if (isValidDirectory(cpFile) || isJarFile(cpFile)) {
 		Path includePath = new Path(cpFile.getAbsolutePath());
 		IClasspathEntry newLibraryEntry = JavaCore.newLibraryEntry(
 			includePath, null, null);
 		this.cpEntries.add(newLibraryEntry);
 	    }
 	}
     }
 
     private boolean isValidDirectory(File cpFile) {
 	return cpFile.isDirectory() && !cpFile.getName().startsWith("META");
     }
 
     private boolean isJarFile(File cpFile) {
 	return cpFile.isFile() && cpFile.getName().endsWith(".jar");
     }
 
     @Override
     public IClasspathEntry[] getClasspathEntries() {
 	return this.cpEntries.toArray(new IClasspathEntry[cpEntries.size()]);
     }
 
     @Override
     public String getDescription() {
 	return "Tigerseye ClasspathContainer";
 	// alternative name "Tigerseye Runtime Library"?
     }
 
     @Override
     public int getKind() {
 	return IClasspathContainer.K_APPLICATION;
     }
 
     @Override
     public IPath getPath() {
 	return containerPath.append("TIGERSEYE_SUPPORT");
     }
 
 }
