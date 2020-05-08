 package interdroid.cuckoo.eclipse.plugin;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.eclipse.ant.core.AntRunner;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.IJavaProject;
 
 import com.android.SdkConstants;
 import com.android.ide.eclipse.adt.AdtConstants;
 import com.android.ide.eclipse.adt.AdtPlugin;
 import com.android.ide.eclipse.adt.internal.build.AidlProcessor;
 import com.android.ide.eclipse.adt.internal.build.builders.BaseBuilder;
 import com.android.ide.eclipse.adt.internal.preferences.AdtPrefs;
 import com.android.sdklib.BuildToolInfo;
 import com.android.sdklib.IAndroidTarget;
 
 @SuppressWarnings("restriction")
 public class CuckooProcessor extends AidlProcessor {
 
 	public static final String PREFIX = "CuckooProcessor: ";//$NON-NLS-1$
 
 	private IFolder myGenFolder;
 
 	public class Reporter implements ErrorReporter {
 
 		private final IProject project;
 
 		public Reporter(IProject project) {
 			this.project = project;
 		}
 
 		@Override
 		public void error(String message) {
 			AdtPlugin.printErrorToConsole(project, PREFIX + message);
 		}
 
 	}
 
 	public CuckooProcessor(IJavaProject javaProject, BuildToolInfo info,
 			IFolder genFolder) {
 		super(javaProject, info, genFolder);
 		this.myGenFolder = genFolder;
 	}
 
 	@Override
 	protected void doCompileFiles(List<IFile> sources, BaseBuilder builder,
 			IProject project, IAndroidTarget projectTarget,
 			List<IPath> sourceFolders, List<IFile> notCompiledOut,
 			List<File> libraryProjectsOut, IProgressMonitor monitor)
 			throws CoreException {
 
 		Reporter reporter = new Reporter(project);
 
 		AdtPlugin.printToConsole(project, PREFIX + "doCompileFiles()");//$NON-NLS-1$
 		super.doCompileFiles(sources, builder, project, projectTarget,
 				sourceFolders, notCompiledOut, libraryProjectsOut, monitor);
 
 		// Create list of AIDL files that need to be dealt with.
 		List<IFile> sourceList = new ArrayList<IFile>();
 		for (IFile sourceFile : sources) {
 			String aidlFile = sourceFile.getLocation().toOSString();
 			BufferedReader in = null;
 			String line = null;
 			boolean isEnabled = true;
 			boolean enabledSet = false;
 			try {
 				in = new BufferedReader(new FileReader(aidlFile));
 				while (true) {
 					line = in.readLine();
 					if (line == null)
 						break;
 					if (line.trim().startsWith("//")) {
 						if (line.contains("cuckoo:enabled=")) {
 							enabledSet = true;
 							if (line.contains("cuckoo:enabled=false")) {
 								isEnabled = false;
 								break;
 							} else if (line.contains("cuckoo:enabled=true")) {
 								isEnabled = true;
 								break;
 							}
 						}
 					}
 				}
 			} catch (IOException e) {
 				AdtPlugin.printErrorToConsole(project, PREFIX + aidlFile
 						+ ": could not read.");
 			} finally {
 				try {
 					in.close();
 				} catch (Throwable e) {
 					// ignore
 				}
 			}
 			if (!isEnabled) {//$NON-NLS-1$
 				AdtPlugin
 						.printToConsole(
 								project,
 								PREFIX
 										+ aidlFile
 										+ ": explicitly skipped (cuckoo:enabled set to false).");
 			} else {
 				if (!enabledSet) {
 					AdtPlugin
 							.printToConsole(
 									project,
 									PREFIX
 											+ aidlFile
 											+ ": implicitly enabled (cuckoo:enabled not set, insert the line \"//cuckoo:enabled=false\" between the package declaration and the interface declaration to disable offloading).");
 				} else {
 					AdtPlugin
 							.printToConsole(
 									project,
 									PREFIX
 											+ aidlFile
 											+ ": explicitly enabled (cuckoo:enabled set to true).");
 				}
 
 				sourceList.add(sourceFile);
 			}
 		}
 
 		// Loop until we've rewritten them all.
 
 		AndroidServiceRewriter rewriter = new AndroidServiceRewriter(reporter);
 		StubDeriver stubDeriver = new StubDeriver(reporter);
 
 		for (IFile sourceFile : sourceList) {
 
 			// Obtain output file (which is present, probably just created).
 			IFile aidlOutput = getAidlOutputFile(sourceFile);
 			if (aidlOutput.exists()) {
 				Properties aidlProperties = parseProperties(sourceFile,
 						reporter, project);
 				AdtPlugin.printToConsole(project, PREFIX + aidlOutput.getName()
 						+ ": rewriting.");
 				rewriter.androidServiceRewrite(aidlOutput.getLocation()
 						.toOSString(), aidlProperties);
 				stubDeriver.deriveStubFrom(aidlOutput.getLocation()
 						.toOSString(), project.getLocation().toOSString());
 			}
 		}
 
 		AdtPlugin.printToConsole(project, PREFIX + "generating build.xml.");
 		writeBuildFile(project);
 		runAnt(project);
 	}
 
 	public static Properties parseProperties(IFile sourceFile,
 			ErrorReporter reporter, IProject project) {
 		Properties properties = new Properties();
 		List<String> lines = Util.getFileAsStringList(sourceFile.getLocation()
 				.toOSString(), reporter);
 		String strategy = "speed/energy";
 		for (int i = 0; i < lines.size(); i++) {
 			String line = lines.get(i);
 			if (line.trim().startsWith("//")) {
 				// comment line
 				String keyValue = line.trim().substring(2).trim()
 						.replace(" ", "");
 				if (keyValue.startsWith("cuckoo:strategy=")
 						|| keyValue.startsWith("cuckoo.strategy=")) {
 					String value = keyValue
 							.substring(keyValue.indexOf('=') + 1).trim();
 					if (validStrategy(value)) {
 						strategy = value;
 					} else {
 						AdtPlugin.printToConsole(project, PREFIX
 								+ "Encountered invalid strategy '" + value
 								+ "' at line: " + i + " in file: "
 								+ sourceFile.getLocation().toOSString());
 						// invalid strategy
 					}
 				} else {
 					// skip the comment
 				}
 			} else if (line.trim().endsWith(";")) {
 				// is this a method declaration?
 				if (line.contains("(") && line.contains(")")) {
 					String tmp = line.substring(0, line.indexOf("("));
 					String method = tmp.substring(tmp.lastIndexOf(' ')).trim();
 					if (properties.contains(method)) {
 						AdtPlugin
 								.printErrorToConsole(
 										project,
 										"Cuckoo cannot handle duplicate method names, please make all method names in the AIDL specification unique: duplicate name '"
 												+ method + "'");
 					}
 					properties.put(method, strategy);
 					strategy = "speed/energy";
 				} else {
 					// skip the line
 				}
 			}
 		}
 		return properties;
 	}
 
 	private static boolean validStrategy(String strategy) {
 		return strategy.equalsIgnoreCase("local")
 				|| strategy.equalsIgnoreCase("remote")
 				|| strategy.equalsIgnoreCase("parallel")
 				|| strategy.equalsIgnoreCase("speed")
 				|| strategy.equalsIgnoreCase("energy")
 				|| strategy.equalsIgnoreCase("speed/energy")
 				|| strategy.equalsIgnoreCase("energy/speed");
 	}
 
 	public static void runAnt(IProject project) throws CoreException {
 		AntRunner runner = new AntRunner();
 		String dir = project.getLocation().toOSString() + File.separator
 				+ "remote";//$NON-NLS-1$
 		runner.setAntHome(dir);
 		runner.setBuildFileLocation(dir + File.separator + "build.xml");//$NON-NLS-1$
 		runner.run();
 	}
 
 	private IFile getAidlOutputFile(IFile sourceFile) throws CoreException {
 
 		IPath sourceFolderPath = getSourceFolderFor(sourceFile);
 
 		if (sourceFolderPath != null) {
 			// make a path to the source file relative to the source folder.
 			IPath relative = sourceFile.getFullPath().makeRelativeTo(
 					sourceFolderPath);
 			// remove the file name. This is now the destination folder inside
 			// the gen folder.
 			relative = relative.removeLastSegments(1);
 
 			// get an IFolder for this path.
 			IFolder destinationFolder = myGenFolder.getFolder(relative);
 
 			// Build the Java file name from the aidl name.
 			String javaName = sourceFile.getName().replaceAll(
 					AdtConstants.RE_AIDL_EXT, SdkConstants.DOT_JAVA);
 
 			// get the resource for the java file.
 			return destinationFolder.getFile(javaName);
 		}
 
 		return null;
 	}
 
 	public static void writeBuildFile(IProject project) {
 		FileWriter out = null;
 		File buildFile = new File(project.getLocation().toOSString()
 				+ File.separator + "remote" + File.separator + "build.xml");//$NON-NLS-1$//$NON-NLS-2$
 		if (buildFile.exists()) {
 			buildFile.delete();
 		}
 		try {
 			out = new FileWriter(buildFile);
 			out.write("<project name=\"Cuckoo\" default=\"build\" basedir=\".\">\n");//$NON-NLS-1$
 			out.write("<description>Cuckoo description</description>\n");//$NON-NLS-1$
 			out.write("<property environment=\"env\" />\n");//$NON-NLS-1$
 			out.write("<property name=\"src\" location=\".\"/>\n");//$NON-NLS-1$
 			out.write("<property name=\"tmp\" location=\"tmp\"/>\n");//$NON-NLS-1$
 			out.write("<property name=\"lib\" location=\".." + File.separator//$NON-NLS-1$
 					+ "assets\"/>\n");//$NON-NLS-1$
 			File[] dirs = new File(project.getLocation().toOSString()
					+ File.separator + "remote").listFiles(new FileFilter() {//$NON-NLS-1$
 
 						@Override
 						public boolean accept(File file) {
 							return file.isDirectory()
 									&& !file.getName().equals("tmp")//$NON-NLS-1$
 									&& !file.getName().equals(".svn");//$NON-NLS-1$
 						}
 					});
 
 			String depends = "clean";//$NON-NLS-1$
 			for (File dir : dirs) {
 				depends += "," + dir.getName();//$NON-NLS-1$
 			}
 			// write the main target, this depends on all the other services
 			out.write("<target name=\"build\" depends=\"" + depends//$NON-NLS-1$
 					+ "\" description=\"description\" />\n");//$NON-NLS-1$
 
 			// now write a target per service
 			for (File serviceDir : dirs) {
 				out.write("<mkdir dir=\"${lib}" + File.separator//$NON-NLS-1$
 						+ serviceDir.getName() + "\" />\n");//$NON-NLS-1$
 				out.write("<target name=\"" + serviceDir.getName()//$NON-NLS-1$
 						+ "\" depends=\"\" description=\""//$NON-NLS-1$
 						+ serviceDir.getName() + "\" >\n");//$NON-NLS-1$
 				out.write("<mkdir dir=\"${tmp}\" />\n");//$NON-NLS-1$
 				out.write("<javac source=\"1.6\" target=\"1.6\" srcdir=\"${src}"//$NON-NLS-1$
 						+ File.separator
 						+ serviceDir.getName()
 						+ "\" destdir=\"${tmp}\" >\n");//$NON-NLS-1$
 				// external jars!
 				out.write("<classpath>\n");//$NON-NLS-1$
 				out.write("<fileset dir=\"${src}" + File.separator//$NON-NLS-1$
 						+ serviceDir.getName() + File.separator
 						+ "external\">\n");//$NON-NLS-1$
 				out.write("<include name=\"*.jar\" />\n");//$NON-NLS-1$
 				out.write("</fileset>\n");//$NON-NLS-1$
 				out.write("<fileset dir=\"${src}/../libs\">\n");//$NON-NLS-1$
 				out.write("<include name=\"*.jar\" />\n");//$NON-NLS-1$
 				out.write("</fileset>\n");//$NON-NLS-1$
 				// include the android jar too!
 				String sdkDir = AdtPlugin.getDefault().getPreferenceStore()
 						.getString(AdtPrefs.PREFS_SDK_DIR);
 				String androidTarget = "android-10"; // default
 				try {
 					Properties projectProperties = new Properties();
 					projectProperties.load(new FileInputStream(project
 							.getLocation().toOSString()
 							+ File.separator
 							+ "project.properties"));
 
 					androidTarget = projectProperties.getProperty("target");
 				} catch (Throwable t) {
 				}
 
 				String androidJar = sdkDir + File.separator + "platforms"
 						+ File.separator + androidTarget + File.separator
 						+ "android.jar";
 
 				out.write("<pathelement location=\"" + androidJar + "\"/>\n");//$NON-NLS-1$
 
 				out.write("</classpath>\n");//$NON-NLS-1$
 				out.write("</javac>\n");//$NON-NLS-1$
 				// copy the external jars to the lib dir
 				out.write("<copy todir=\"${lib}" + File.separator//$NON-NLS-1$
 						+ serviceDir.getName() + "\">\n");//$NON-NLS-1$
 				out.write("<fileset dir=\"${src}" + File.separator//$NON-NLS-1$
 						+ serviceDir.getName() + File.separator
 						+ "external\">\n");//$NON-NLS-1$
 				out.write("<include name=\"*.*\" />\n");//$NON-NLS-1$
 				out.write("</fileset>\n");//$NON-NLS-1$
 				out.write("</copy>\n");//$NON-NLS-1$
 				// now jar this compiled classes
 				out.write("<jar jarfile=\"${lib}" + File.separator//$NON-NLS-1$
 						+ serviceDir.getName() + File.separator
 						+ serviceDir.getName()
 						+ ".jar\" basedir=\"${tmp}\" includes=\"**\">\n");//$NON-NLS-1$
 				out.write("</jar>\n");//$NON-NLS-1$
 				out.write("<delete dir=\"${tmp}\"/>\n");//$NON-NLS-1$
 				out.write("</target>\n");//$NON-NLS-1$
 			}
 
 			out.write("<target name=\"clean\" description=\"clean up\" >\n");//$NON-NLS-1$
 			out.write("<delete dir=\"${tmp}\"/>\n");//$NON-NLS-1$
 			out.write("<delete>\n");//$NON-NLS-1$
 			out.write("<fileset dir=\"${lib}\">\n");//$NON-NLS-1$
 			out.write("<include name=\"*.jar\" />\n");//$NON-NLS-1$
 			out.write("</fileset>\n");//$NON-NLS-1$
 			out.write("</delete>\n");//$NON-NLS-1$
 			out.write("</target>\n");//$NON-NLS-1$
 			out.write("</project>\n");//$NON-NLS-1$
 			out.flush();
 		} catch (Exception e) {
 			AdtPlugin.printErrorToConsole(project, PREFIX + buildFile.getName()
 					+ ": could not write.");
 		} finally {
 			try {
 				out.close();
 			} catch (Throwable e) {
 				// ignore
 			}
 		}
 	}
 }
