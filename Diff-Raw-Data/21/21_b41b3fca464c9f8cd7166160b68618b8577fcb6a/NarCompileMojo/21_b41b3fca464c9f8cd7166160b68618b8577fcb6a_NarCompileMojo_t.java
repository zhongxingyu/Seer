 // Copyright FreeHEP, 2005-2007.
 package org.freehep.maven.nar;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import net.sf.antcontrib.cpptasks.CCTask;
 import net.sf.antcontrib.cpptasks.CUtil;
 import net.sf.antcontrib.cpptasks.OutputTypeEnum;
 import net.sf.antcontrib.cpptasks.RuntimeType;
 import net.sf.antcontrib.cpptasks.types.LibrarySet;
 import net.sf.antcontrib.cpptasks.types.SystemLibrarySet;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.StringUtils;
 
 /**
  * Compiles native source files.
  * 
  * @goal nar-compile
  * @phase compile
  * @requiresDependencyResolution compile
  * @author <a href="Mark.Donszelmann@slac.stanford.edu">Mark Donszelmann</a>
 * @version $Id: plugin/src/main/java/org/freehep/maven/nar/NarCompileMojo.java 6ae92433be9e 2007/07/18 17:45:27 duns $
  */
 public class NarCompileMojo extends AbstractCompileMojo {
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		if (shouldSkip())
 			return;
 
 		// make sure destination is there
 		getTargetDirectory().mkdirs();
 
 		// check for source files
 		int noOfSources = 0;
 		noOfSources += getSourcesFor(getCpp()).size();
 		noOfSources += getSourcesFor(getC()).size();
 		noOfSources += getSourcesFor(getFortran()).size();
 		if (noOfSources > 0) {
 			for (Iterator i = getLibraries().iterator(); i.hasNext();) {
 				createLibrary(getAntProject(), (Library) i.next());
 			}
 		}
 
 		try {
 			// FIXME, should the include paths be defined at a higher level ?
 			getCpp().copyIncludeFiles(getMavenProject(),
 					new File(getTargetDirectory(), "include"));
 		} catch (IOException e) {
 			throw new MojoExecutionException(
 					"NAR: could not copy include files", e);
 		}
 	}
 
 	private List getSourcesFor(Compiler compiler) throws MojoFailureException {
 		try {
 			File srcDir = compiler.getSourceDirectory();
 			return srcDir.exists() ? FileUtils.getFiles(srcDir, StringUtils
 					.join(compiler.getIncludes().iterator(), ","), null)
 					: Collections.EMPTY_LIST;
 		} catch (IOException e) {
 			return Collections.EMPTY_LIST;
 		}
 	}
 
 	private void createLibrary(Project antProject, Library library)
 			throws MojoExecutionException, MojoFailureException {
 		// configure task
 		CCTask task = new CCTask();
 		task.setProject(antProject);
 
		// set max cores
		task.setMaxCores(maxCores);
		
 		// outtype
 		OutputTypeEnum outTypeEnum = new OutputTypeEnum();
 		String type = library.getType();
 		outTypeEnum.setValue(type);
 		task.setOuttype(outTypeEnum);
 
 		// stdc++
 		task.setLinkCPP(library.linkCPP());
 
 		// outDir
 		File outDir = new File(getTargetDirectory(), type
 				.equals(Library.EXECUTABLE) ? "bin" : "lib");
 		outDir = new File(outDir, getAOL());
 		if (!type.equals(Library.EXECUTABLE))
 			outDir = new File(outDir, type);
 		outDir.mkdirs();
 
 		// outFile
 		File outFile;
 		if (type.equals(Library.EXECUTABLE)) {
 			// executable has no version number
 			outFile = new File(outDir, getMavenProject().getArtifactId());
 		} else {
 			outFile = new File(outDir, getOutput());
 		}
 		getLog().debug("NAR - output: '" + outFile + "'");
 		task.setOutfile(outFile);
 
 		// object directory
 		File objDir = new File(getTargetDirectory(), "obj");
 		objDir = new File(objDir, getAOL());
 		objDir.mkdirs();
 		task.setObjdir(objDir);
 
 		// failOnError, libtool
 		task.setFailonerror(failOnError());
 		task.setLibtool(useLibtool());
 
 		// runtime
 		RuntimeType runtimeType = new RuntimeType();
 		runtimeType.setValue(getRuntime());
 		task.setRuntime(runtimeType);
 
 		// add C++ compiler
 		task.addConfiguredCompiler(getCpp().getCompiler(type, getOutput()));
 
 		// add C compiler
 		task.addConfiguredCompiler(getC().getCompiler(type, getOutput()));
 
 		// add Fortran compiler
 		task.addConfiguredCompiler(getFortran().getCompiler(type, getOutput()));
 
 		// add javah include path
 		File jniDirectory = getJavah().getJniDirectory();
 		if (jniDirectory.exists())
 			task.createIncludePath().setPath(jniDirectory.getPath());
 
 		// add java include paths
 		getJava().addIncludePaths(task, type);
 
 		// add dependency include paths
 		for (Iterator i = getNarManager().getNarDependencies("compile")
 				.iterator(); i.hasNext();) {
 			// FIXME, handle multiple includes from one NAR
 			NarArtifact narDependency = (NarArtifact) i.next();
 			String binding = narDependency.getNarInfo().getBinding(getAOL(),
 					Library.STATIC);
 			getLog().debug(
 					"Looking for " + narDependency + " found binding "
 							+ binding);
 			if (!binding.equals(Library.JNI)) {
 				File include = new File(getNarManager().getNarFile(
 						narDependency).getParentFile(), "nar/include");
 				getLog().debug("Looking for for directory: " + include);
 				if (include.exists()) {
 					task.createIncludePath().setPath(include.getPath());
 				}
 			}
 		}
 
 		// add linker
 		task.addConfiguredLinker(getLinker().getLinker(this, antProject,
 				getOS(), getAOLKey() + "linker.", type));
 
 		// add dependency libraries
 		// FIXME: what about PLUGIN and STATIC, depending on STATIC, should we
 		// not add all libraries, see NARPLUGIN-96
 		if (type.equals(Library.SHARED) || type.equals(Library.JNI)
 				|| type.equals(Library.EXECUTABLE)) {
 			for (Iterator i = getNarManager().getNarDependencies("compile")
 					.iterator(); i.hasNext();) {
 				NarArtifact dependency = (NarArtifact) i.next();
 
 				// FIXME no handling of "local"
 
 				// FIXME, no way to override this at this stage
 				String binding = dependency.getNarInfo().getBinding(getAOL(),
 						Library.STATIC);
 				getLog().debug("Using Binding: " + binding);
 				String aol = getAOL();
 				aol = dependency.getNarInfo().getAOL(getAOL());
 				getLog().debug("Using Library AOL: " + aol);
 
 				if (!binding.equals(Library.JNI)) {
 					File dir = new File(getNarManager().getNarFile(dependency)
 							.getParentFile(), "nar/lib/" + aol + "/" + binding);
 					getLog().debug("Looking for Library Directory: " + dir);
 					if (dir.exists()) {
 						LibrarySet libSet = new LibrarySet();
 						libSet.setProject(antProject);
 
 						// FIXME, no way to override
 						String libs = dependency.getNarInfo().getLibs(getAOL());
 						getLog().debug("Using LIBS = " + libs);
 						libSet.setLibs(new CUtil.StringArrayBuilder(libs));
 						libSet.setDir(dir);
 						task.addLibset(libSet);
 					} else {
 						getLog()
 								.debug(
 										"Library Directory " + dir
 												+ " does NOT exist.");
 					}
 
 					String sysLibs = dependency.getNarInfo().getSysLibs(
 							getAOL());
 					if (sysLibs != null) {
 						getLog().debug("Using SYSLIBS = " + sysLibs);
 						SystemLibrarySet sysLibSet = new SystemLibrarySet();
 						sysLibSet.setProject(antProject);
 
 						sysLibSet
 								.setLibs(new CUtil.StringArrayBuilder(sysLibs));
 						task.addSyslibset(sysLibSet);
 					}
 				}
 			}
 		}
 
 		// Add JVM to linker
 		getJava().addRuntime(task, getJavaHome(), getOS(),
 				getAOLKey() + "java.");
 
 		// execute
 		try {
 			task.execute();
 		} catch (BuildException e) {
 			throw new MojoExecutionException("NAR: Compile failed", e);
 		}
 	}
 }
