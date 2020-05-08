 // Copyright FreeHEP, 2005-2007.
 package org.freehep.maven.nar;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 
 /**
  * Tests NAR files. Runs Native Tests and executables if produced.
  * 
  * @goal nar-test
  * @phase test
  * @requiresProject
  * @author <a href="Mark.Donszelmann@slac.stanford.edu">Mark Donszelmann</a>
 * @version $Id: plugin/src/main/java/org/freehep/maven/nar/NarTestMojo.java f025de4b73d2 2007/07/09 16:33:07 duns $
  */
 public class NarTestMojo extends AbstractCompileMojo {
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		if (shouldSkip())
 			return;
 
 		// run all tests
 		for (Iterator i = getTests().iterator(); i.hasNext();) {
 			runTest((Test) i.next());
 		}
 
 		for (Iterator i = getLibraries().iterator(); i.hasNext();) {
 			runExecutable((Library) i.next());
 		}
 	}
 
 	private void runTest(Test test) throws MojoExecutionException,
 			MojoFailureException {
 		// run if requested
 		if (test.shouldRun()) {
			String name = "target/test-nar/bin/" + getAOL() + "/" + test.getName();
 			getLog().info("Running " + name);
 			int result = runCommand(generateCommandLine(getMavenProject()
 					.getBasedir()
 					+ "/" + name, test, getLog()), generateEnvironment(test,
 					getLog()), getLog());
 			if (result != 0)
 				throw new MojoFailureException("Test " + name
 						+ " failed with exit code: " + result);
 		}
 	}
 
 	private void runExecutable(Library library) throws MojoExecutionException,
 			MojoFailureException {
 		if (library.getType().equals(Library.EXECUTABLE) && library.shouldRun()) {
 			MavenProject project = getMavenProject();
 			String name = "target/nar/bin/" + getAOL() + "/"
 					+ project.getArtifactId();
 			getLog().info("Running " + name);
 			int result = runCommand(generateCommandLine(project.getBasedir()
 					+ "/" + name, library, getLog()), generateEnvironment(
 					library, getLog()), getLog());
 			if (result != 0)
 				throw new MojoFailureException("Test " + name
 						+ " failed with exit code: " + result);
 		}
 	}
 
 	protected File getTargetDirectory() {
 		return new File(getMavenProject().getBuild().getDirectory(), "test-nar");
 	}
 
 	private String[] generateCommandLine(String name, Executable exec, Log log)
 			throws MojoExecutionException {
 
 		List cmdLine = new ArrayList();
 
 		cmdLine.add(name);
 
 		cmdLine.addAll(exec.getArgs());
 
 		log.debug("CommandLine: " + cmdLine.toString());
 
 		return (String[]) cmdLine.toArray(new String[cmdLine.size()]);
 	}
 
 	private String[] generateEnvironment(Executable exec, Log log)
 			throws MojoExecutionException, MojoFailureException {
 		List env = new ArrayList();
 
 		Set/*<File>*/ sharedPaths = new HashSet();
 		
 		// add all shared libraries of this package
 		for (Iterator i=getLibraries().iterator(); i.hasNext(); ) {
 			Library lib = (Library)i.next();
 			if (lib.getType().equals(Library.SHARED)) {
 				sharedPaths.add(new File(getMavenProject().getBasedir(), "target/nar/lib/"+getAOL()+"/"+lib.getType()));
 			}
 		}
 
 		// add dependent shared libraries
 		String classifier = getAOL()+"-shared";
 		List narArtifacts = getNarManager().getNarDependencies("compile");
 		List dependencies = getNarManager().getAttachedNarDependencies(
 				narArtifacts, classifier);
 		for (Iterator d = dependencies.iterator(); d.hasNext();) {
 			Artifact dependency = (Artifact) d.next();
 			getLog().debug("Looking for dependency " + dependency);
 
 			// FIXME reported to maven developer list, isSnapshot
 			// changes behaviour
 			// of getBaseVersion, called in pathOf.
 			if (dependency.isSnapshot())
 				;
 
 			File libDir = new File(getLocalRepository().pathOf(dependency));
 			libDir = new File(getLocalRepository().getBasedir(), libDir
 					.getParent());
 			libDir = new File(libDir, "nar/lib/"+getAOL()+"/shared");
 			sharedPaths.add(libDir);
 		}
 		
 		// set environment
 		if (sharedPaths.size() > 0) {
 			String sharedPath = "";
 			for (Iterator i=sharedPaths.iterator(); i.hasNext(); ) {
 				sharedPath += ((File)i.next()).getPath();
 				if (i.hasNext()) sharedPath += File.pathSeparator;
 			}
 			
 			String sharedPathName = getOS().equals(OS.MACOSX) ? "DYLD_LIBRARY_PATH" : getOS().startsWith("Windows") ? "PATH" : "LD_LIBRARY_PATH";
 			env.add(sharedPathName+"="+sharedPath);
 		}
 		
 		log.debug("Environment:" + env.toString());
 
 		return (String[]) env.toArray(new String[env.size()]);
 	}
 
 	// NOTE: same as in Javah.java
 	private int runCommand(String[] cmdLine, String[] env, Log log)
 			throws MojoExecutionException {
 		try {
 			Runtime runtime = Runtime.getRuntime();
 			Process process = runtime.exec(cmdLine, env);
 			StreamGobbler errorGobbler = new StreamGobbler(process
 					.getErrorStream(), true, log);
 			StreamGobbler outputGobbler = new StreamGobbler(process
 					.getInputStream(), false, log);
 
 			errorGobbler.start();
 			outputGobbler.start();
 			return process.waitFor();
 		} catch (Throwable e) {
 			throw new MojoExecutionException("Could not launch " + cmdLine[0],
 					e);
 		}
 	}
 
 	class StreamGobbler extends Thread {
 		InputStream is;
 		boolean error;
 		Log log;
 
 		StreamGobbler(InputStream is, boolean error, Log log) {
 			this.is = is;
 			this.error = error;
 			this.log = log;
 		}
 
 		public void run() {
 			try {
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(is));
 				String line = null;
 				while ((line = reader.readLine()) != null) {
 					if (error) {
 						log.error(line);
 					} else {
 						log.debug(line);
 					}
 				}
 				reader.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
