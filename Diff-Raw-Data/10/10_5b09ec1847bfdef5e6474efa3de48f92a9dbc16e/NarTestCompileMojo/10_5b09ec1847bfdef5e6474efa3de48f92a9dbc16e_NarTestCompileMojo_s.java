 // Copyright FreeHEP, 2005-2007.
 package org.freehep.maven.nar;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 
 import net.sf.antcontrib.cpptasks.CCTask;
 import net.sf.antcontrib.cpptasks.CUtil;
 import net.sf.antcontrib.cpptasks.OutputTypeEnum;
 import net.sf.antcontrib.cpptasks.RuntimeType;
 import net.sf.antcontrib.cpptasks.types.LibrarySet;
 import net.sf.antcontrib.cpptasks.types.LibraryTypeEnum;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.codehaus.plexus.util.FileUtils;
 
 /**
  * Compiles native test source files.
  * 
  * @goal nar-testCompile
  * @phase test-compile
  * @requiresDependencyResolution test
  * @author <a href="Mark.Donszelmann@slac.stanford.edu">Mark Donszelmann</a>
 * @version $Id: plugin/src/main/java/org/freehep/maven/nar/NarTestCompileMojo.java f934ad2b8948 2007/07/13 14:17:10 duns $
  */
 public class NarTestCompileMojo extends AbstractCompileMojo {
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		if (shouldSkip())
 			return;
 
 		// make sure destination is there
 		getTargetDirectory().mkdirs();
 
 		for (Iterator i = getTests().iterator(); i.hasNext();) {
 			createTest(getAntProject(), (Test) i.next());
 		}
 	}
 
 	private void createTest(Project antProject, Test test)
 			throws MojoExecutionException, MojoFailureException {
 		String type = "test";
 
 		// configure task
 		CCTask task = new CCTask();
 		task.setProject(antProject);
 
 		// outtype
 		OutputTypeEnum outTypeEnum = new OutputTypeEnum();
 		outTypeEnum.setValue(Library.EXECUTABLE);
 		task.setOuttype(outTypeEnum);
 
 		// outDir
 		File outDir = new File(getTargetDirectory(), "bin");
 		outDir = new File(outDir, getAOL());
 		outDir.mkdirs();
 
 		// outFile
 		File outFile = new File(outDir, test.getName());
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
 		task.addConfiguredCompiler(getCpp().getCompiler(type, test.getName()));
 
 		// add C compiler
 		task.addConfiguredCompiler(getC().getCompiler(type, test.getName()));
 
 		// add Fortran compiler
 		task.addConfiguredCompiler(getFortran().getCompiler(type,
 				test.getName()));
 
 		// add java include paths
 		getJava().addIncludePaths(task, type);
 
 		// add dependency include paths
 		for (Iterator i = getNarManager().getNarDependencies("test").iterator(); i
 				.hasNext();) {
 			File include = new File(getNarManager().getNarFile(
 					(Artifact) i.next()).getParentFile(), "nar/include");
 			if (include.exists()) {
 				task.createIncludePath().setPath(include.getPath());
 			}
 		}
 
 		// add linker
 		task.addConfiguredLinker(getLinker().getLinker(this, antProject,
 				getOS(), getAOLKey() + "linker.", type));
 
 		// FIXME hardcoded values
 		String libName = getFinalName();
 		File includeDir = new File(getMavenProject().getBuild().getDirectory(),
 				"nar/include");
 		File libDir = new File(getMavenProject().getBuild().getDirectory(),
 				"nar/lib/" + getAOL() + "/" + test.getLink());
 
 		// copy shared library
 		if (test.getLink().equals(Library.SHARED)) {
 			try {
 				// defaults are Unix
 				String libPrefix = NarUtil.getDefaults().getProperty(
						getAOLKey() + "lib.prefix", "lib");
 				String libExt = NarUtil.getDefaults().getProperty(
 						getAOLKey() + "shared.extension", "so");
 				File copyDir = new File(getTargetDirectory(), (getOS().equals(
 						"Windows") ? "bin" : "lib")
 						+ "/" + getAOL() + "/" + test.getLink());
 				FileUtils.copyFileToDirectory(new File(libDir, libPrefix
 						+ libName + "." + libExt), copyDir);
				if (!getOS().equals("Windows")) {
 					libDir = copyDir;
 				}
 			} catch (IOException e) {
 				throw new MojoExecutionException(
 						"NAR: Could not copy shared library", e);
 			}
 		}
 
 		// FIXME what about copying the other shared libs?
 
 		// add include of this package
 		if (includeDir.exists()) {
 			task.createIncludePath().setLocation(includeDir);
 		}
 
 		// add library of this package
 		if (libDir.exists()) {
 			LibrarySet libSet = new LibrarySet();
 			libSet.setProject(antProject);
 			libSet.setLibs(new CUtil.StringArrayBuilder(libName));
 			LibraryTypeEnum libType = new LibraryTypeEnum();
 			libType.setValue(test.getLink());
 			libSet.setType(libType);
 			libSet.setDir(libDir);
 			task.addLibset(libSet);
 		}
 
 		// add dependency libraries
 		for (Iterator i = getNarManager().getNarDependencies("test").iterator(); i
 				.hasNext();) {
 			Artifact dependency = (Artifact) i.next();
 			File lib = new File(getNarManager().getNarFile(dependency)
 					.getParentFile(), "nar/lib/" + getAOL() + "/"
 					+ test.getLink());
 			if (lib.exists()) {
 				LibrarySet libset = new LibrarySet();
 				libset.setProject(antProject);
 				libset.setLibs(new CUtil.StringArrayBuilder(dependency
 						.getArtifactId()
 						+ "-" + dependency.getVersion()));
 				libset.setDir(lib);
 				task.addLibset(libset);
 			}
 		}
 
 		// Add JVM to linker
 		getJava().addRuntime(task, getJavaHome(), getOS(),
 				getAOLKey() + "java.");
 
 		// execute
 		try {
 			task.execute();
 		} catch (BuildException e) {
 			throw new MojoExecutionException("NAR: Test-Compile failed", e);
 		}
 	}
 
 	protected File getTargetDirectory() {
 		return new File(getMavenProject().getBuild().getDirectory(), "test-nar");
 	}
 
 }
