 /*
  * This file is part of jStar Eclipse Plug-in.
  * 
  * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
  */
 package com.jstar.eclipse.objects;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import com.jstar.eclipse.exceptions.NoJStarRootFolderException;
 import com.jstar.eclipse.services.ConsoleService;
 import com.jstar.eclipse.services.JStar.DebugMode;
 import com.jstar.eclipse.services.JStar.PrintMode;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.QualifiedName;
 
 //TODO: Rename
 public class JavaFilePersistentProperties {
 	
 	private static final QualifiedName MODE = new QualifiedName("JSTAR_VERIFICATION", "MODE");
 	private static final QualifiedName SPEC_IN_SOURCE_FILE = new QualifiedName("JSTAR_VERIFICATION", "SPEC_IN_SOURCE_FILE");
 	private static final QualifiedName JSTAR_ROOT_FOLDER = new QualifiedName("JSTAR_VERIFICATION", "JSTAR_ROOT_FOLDER");
 	public static final QualifiedName DM_SYMBOLIC = new QualifiedName("JSTAR_VERIFICATION", "DEBUG_MODE_SYMBOLIC");
 	public static final QualifiedName DM_PARSING = new QualifiedName("JSTAR_VERIFICATION", "DEBUG_MODE_PARSING");
 	public static final QualifiedName DM_CORE = new QualifiedName("JSTAR_VERIFICATION", "DEBUG_MODE_CORE");
 	public static final QualifiedName DM_SMT = new QualifiedName("JSTAR_VERIFICATION", "DEBUG_MODE_SMT");
 	
 	public static String getJStarRootFolder(final JavaProject project) {
 		final String jStarRootFolder = getProperty(project.getProject().getResource(), JSTAR_ROOT_FOLDER);	
 		
 		if (StringUtils.isBlank(jStarRootFolder)) {
 			throw new NoJStarRootFolderException();
 		}
 		
 		return jStarRootFolder;
 	}
 	
 	public static void setJStarRootFolder(final JavaProject project, String jStarFolderRoot) {
 		setProperty(project.getProject().getResource(), JSTAR_ROOT_FOLDER, jStarFolderRoot);
 	}
 	
 	public static boolean isSpecInSourceFile(final JavaFile file) {
 		return isSpecInSourceFile(file.getFile());
 	}
 	
 	public static boolean isSpecInSourceFile(final IResource resource) {
 		if ("false".equalsIgnoreCase(getProperty(resource, SPEC_IN_SOURCE_FILE))) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public static void setSpecInSourceFile(final JavaFile file, boolean specInSourceFile) {
 		setProperty(file.getFile(), SPEC_IN_SOURCE_FILE, String.valueOf(specInSourceFile));
 	}
 	
 	public static PrintMode getMode(final JavaFile file) {
 	    PrintMode printMode = null;
 	    
 	    try {
 			printMode = PrintMode.valueOf(getProperty(file.getFile(), MODE));
 		} 
 	    catch (Exception e) {
 			// Default value "-q"
 			
 			return PrintMode.QUIET;
 		}
 		
 		return printMode;
 	}
 	
 	public static void setMode(final JavaFile file, final PrintMode mode) {
 		setProperty(file.getFile(), MODE, mode.toString());
 	}
 	
 	public static void setDebugModes(JavaFile javaFile, Set<DebugMode> modes) {
		setDebugMode(javaFile, DebugMode.SYMBOLIC, modes.contains(DebugMode.SYMBOLIC));
		setDebugMode(javaFile, DebugMode.CORE, modes.contains(DebugMode.CORE));
		setDebugMode(javaFile, DebugMode.PARSING, modes.contains(DebugMode.PARSING));
		setDebugMode(javaFile, DebugMode.SMT, modes.contains(DebugMode.SMT));
	}
	
	private static void setDebugMode(JavaFile javaFile, DebugMode mode, boolean set) {
		setProperty(javaFile.getFile(), mode.getQualifiedName(), String.valueOf(set));
 	}
 	
 	public static Set<DebugMode> getDebugModes(JavaFile javaFile) {
 		final Set<DebugMode> modes = new HashSet<DebugMode>();
 		
 		final String symbolic = getProperty(javaFile.getFile(), DM_SYMBOLIC);
 		if ("true".equals(symbolic)) {
 			modes.add(DebugMode.SYMBOLIC);
 		}
 		
 		final String parsing = getProperty(javaFile.getFile(), DM_PARSING);
 		if ("true".equals(parsing)) {
 			modes.add(DebugMode.PARSING);
 		}
 		
 		final String core = getProperty(javaFile.getFile(), DM_CORE);
 		if ("true".equals(core)) {
 			modes.add(DebugMode.CORE);
 		}
 		
 		final String smt = getProperty(javaFile.getFile(), DM_SMT);
 		if ("true".equals(smt)) {
 			modes.add(DebugMode.SMT);
 		}
 	
 		return modes;
 	}
 	
 	public static String getDebugModeString(JavaFile javaFile) {		
 		final StringBuilder result = new StringBuilder();
 		
 		for (final DebugMode mode :  getDebugModes(javaFile)) {
 			result.append(mode.getCmdOption());
 		}
 		
 		return result.toString();
 	}
 	
 	private static void setProperty(final IResource resource, final QualifiedName name, final String value) {
 		try {
 			resource.setPersistentProperty(name, value);
 		} 
 		catch (CoreException ce) {
 			ConsoleService.getInstance().printErrorMessage("Could not save property " + name.getLocalName() + " with value " + value + " of file " + resource.getName() + ".");
 		}
 	}
 	
 	private static String getProperty(final IResource resource, final QualifiedName name) {
 		try {
 			return resource.getPersistentProperty(name);
 		} 
 		catch (CoreException ce) {
 			return null;
 		}
 	}
 
 }
