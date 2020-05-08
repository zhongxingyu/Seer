 /*******************************************************************************
  * Copyright (c) 2009, 2010 Sven Kiera
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.phpsrc.eclipse.pti.tools.phpunit.core;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InvalidClassException;
 import java.io.InvalidObjectException;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.compiler.problem.IProblem;
 import org.eclipse.dltk.compiler.problem.ProblemSeverities;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.search.SearchMatch;
 import org.eclipse.ui.progress.UIJob;
 import org.phpsrc.eclipse.pti.core.PHPToolkitUtil;
 import org.phpsrc.eclipse.pti.core.compiler.problem.FileProblem;
 import org.phpsrc.eclipse.pti.core.launching.OperatingSystem;
 import org.phpsrc.eclipse.pti.core.launching.PHPToolLauncher;
 import org.phpsrc.eclipse.pti.core.php.inifile.INIFileEntry;
 import org.phpsrc.eclipse.pti.core.php.inifile.INIFileUtil;
 import org.phpsrc.eclipse.pti.core.php.source.PHPClassSourceModifier;
 import org.phpsrc.eclipse.pti.core.php.source.PHPSourceFile;
 import org.phpsrc.eclipse.pti.core.search.PHPSearchEngine;
 import org.phpsrc.eclipse.pti.core.tools.AbstractPHPTool;
 import org.phpsrc.eclipse.pti.tools.phpunit.PHPUnitPlugin;
 import org.phpsrc.eclipse.pti.tools.phpunit.core.model.PHPUnitModel;
 import org.phpsrc.eclipse.pti.tools.phpunit.core.model.TestRunSession;
 import org.phpsrc.eclipse.pti.tools.phpunit.core.preferences.PHPUnitPreferences;
 import org.phpsrc.eclipse.pti.tools.phpunit.core.preferences.PHPUnitPreferencesFactory;
 import org.phpsrc.eclipse.pti.ui.Logger;
 
 public class PHPUnit extends AbstractPHPTool {
 
 	private interface IMethodFilter {
 		public boolean accept(IMethod method);
 	}
 
 	public final static QualifiedName QUALIFIED_NAME = new QualifiedName(PHPUnitPlugin.PLUGIN_ID, "PHPUnit"); //$NON-NLS-1$
 	public final static String PHPUNIT_TEST_SUITE_CLASS = "PHPUnit_Framework_TestSuite"; //$NON-NLS-1$
 	public final static String PHPUNIT_TEST_CASE_CLASS = "PHPUnit_Framework_TestCase"; //$NON-NLS-1$
 
 	private final static String PHPUNIT_SKELETON_OPTION_CLASS = "skeleton-class"; //$NON-NLS-1$
 	private final static String PHPUNIT_SKELETON_OPTION_TEST = "skeleton-test"; //$NON-NLS-1$
 	private final static String PHPUNIT_SUMMARY_FILE = "phpunit.xml"; //$NON-NLS-1$
 
 	private static PHPUnit instance;
 	private IMethodFilter testMethodFilter;
 
 	protected PHPUnit() {
 		testMethodFilter = new IMethodFilter() {
 			public boolean accept(IMethod method) {
 				return method != null && method.getElementName().startsWith("test"); //$NON-NLS-1$
 			}
 		};
 	}
 
 	public static PHPUnit getInstance() {
 		if (instance == null)
 			instance = new PHPUnit();
 
 		return instance;
 	}
 
 	public void createPHPClassSkeleton(String className, IFile classFile, String testClassName, String testClassFilePath)
 			throws InvalidObjectException, CoreException, InvalidClassException, PHPUnitException {
 		createPHPClassSkeleton(className, classFile, testClassName, testClassFilePath, null);
 	}
 
 	public void createPHPClassSkeleton(String className, IFile classFile, String phpClassName, String classFilePath,
 			String superClass) throws InvalidObjectException, CoreException, InvalidClassException, PHPUnitException {
 		createSkeleton(className, classFile, phpClassName, classFilePath, superClass, PHPUNIT_SKELETON_OPTION_CLASS);
 	}
 
 	public void createTestSkeleton(String className, IFile classFile, String testClassName, String testClassFilePath)
 			throws InvalidObjectException, CoreException, InvalidClassException, PHPUnitException {
 		createTestSkeleton(className, classFile, testClassName, testClassFilePath, null);
 	}
 
 	public void createTestSkeleton(String className, IFile classFile, String testClassName, String testClassFilePath,
 			String testSuperClass) throws InvalidObjectException, CoreException, InvalidClassException,
 			PHPUnitException {
 		if (testSuperClass == null || "".equals(testSuperClass))
 			testSuperClass = PHPUNIT_TEST_CASE_CLASS;
 
 		createSkeleton(className, classFile, testClassName, testClassFilePath, testSuperClass,
 				PHPUNIT_SKELETON_OPTION_TEST, testMethodFilter);
 	}
 
 	private void createSkeleton(String className, IFile classFile, String targetClassName, String classFilePath,
 			String targetSuperClass, String skeletonOption) throws InvalidObjectException, CoreException,
 			InvalidClassException, PHPUnitException {
 		createSkeleton(className, classFile, targetClassName, classFilePath, targetSuperClass, skeletonOption, null);
 	}
 
 	private void createSkeleton(String className, IFile classFile, String targetClassName, String classFilePath,
 			String targetSuperClass, String skeletonOption, IMethodFilter methodFilter) throws InvalidObjectException,
 			CoreException, InvalidClassException, PHPUnitException {
 		Path path = new Path(classFilePath);
 		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
 		IProject project = file.getProject();
 		if (project == null)
 			throw new InvalidObjectException("no project found");
 
 		IFolder folder = (IFolder) file.getParent();
 		createFolder(folder);
 
 		String testClassLocation = file.getLocation().toOSString();
 
 		PHPClassSourceModifier modifier = null;
 		if (file.exists()) {
 			ISourceModule oldModule = PHPToolkitUtil.getSourceModule(file);
 			IType oldClass = oldModule.getAllTypes()[0];
 			modifier = new PHPClassSourceModifier(oldModule, oldClass.getElementName());
 		}
 
 		String cmdLineArgs = "--" + skeletonOption + " " + className;
 		cmdLineArgs += " " + OperatingSystem.escapeShellFileArg(classFile.getLocation().toOSString());
 		cmdLineArgs += " " + targetClassName;
 		cmdLineArgs += " " + OperatingSystem.escapeShellFileArg(testClassLocation);
 
 		PHPToolLauncher launcher = getProjectPHPToolLauncher(project, cmdLineArgs, classFile.getParent().getLocation());
 		String output = launcher.launch(project);
 
 		boolean ok = (output.indexOf("Wrote skeleton for ") >= 0 ? true : false);
 
 		if (ok) {
 			folder.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
 
 			ISourceModule newModule = PHPToolkitUtil.getSourceModule(file);
 			newModule.reconcile(false, null, new NullProgressMonitor());
 			IType newClass = newModule.getAllTypes()[0];
 
 			String newTestCaseSource = null;
 			if (modifier != null) {
 				for (IMethod method : newClass.getMethods()) {
 					if (methodFilter == null || methodFilter.accept(method)) {
 						modifier.addMethod(method);
 					}
 				}
 				newTestCaseSource = modifier.getSource();
 			} else {
 				String[] superClasses = newClass.getSuperClasses();
 				if (superClasses != null && superClasses.length > 0 && !superClasses[0].equals(targetSuperClass)) {
 					modifier = new PHPClassSourceModifier(newModule, newClass.getElementName());
 					modifier.setSuperClass(targetSuperClass);
 					newTestCaseSource = modifier.getSource();
 				}
 			}
 
 			if (newTestCaseSource != null) {
 				try {
 					FileWriter writer = new FileWriter(file.getLocation().toOSString());
 					writer.write(newTestCaseSource);
 					writer.close();
 
 					file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
 				} catch (IOException e) {
 				}
 			}
 		} else {
 			StringBuffer failures = new StringBuffer();
 
			Matcher m = Pattern.compile("[^:]+ error: .*").matcher(output);
 			while (m.find()) {
 				failures.append(m.group(0));
 			}
 
 			throw new PHPUnitException(failures.toString());
 		}
 	}
 
 	public IProblem[] runTestCase(final IFile testFile) {
 		try {
 			final File summaryFile = createTempSummaryFile(PHPUNIT_SUMMARY_FILE);
 
 			ISourceModule module = PHPToolkitUtil.getSourceModule(testFile);
 			IType[] types = module.getAllTypes();
 			for (IType type : types) {
 				String cmdLineArgs = "--log-junit " + OperatingSystem.escapeShellFileArg(summaryFile.toString());
 				cmdLineArgs += " " + type.getElementName();
 				cmdLineArgs += " " + OperatingSystem.escapeShellFileArg(testFile.getLocation().toOSString());
 
 				PHPToolLauncher launcher = getProjectPHPToolLauncher(testFile.getProject(), cmdLineArgs, testFile
 						.getParent().getLocation());
 				String output = launcher.launch(testFile.getProject());
 				IProblem[] problems = parseOutput(testFile.getProject(), output);
 
 				importTestRunSession(summaryFile);
 
 				return problems;
 			}
 		} catch (ModelException e) {
 			Logger.logException(e);
 		} catch (IOException e) {
 			Logger.logException(e);
 		}
 
 		return new IProblem[0];
 	}
 
 	private File createTempSummaryFile(String fileName) throws IOException {
 		File tempDir = createTempDir("pti_phpunit"); //$NON-NLS-1$
 		return createTempFile(tempDir, fileName);
 	}
 
 	private void importTestRunSession(final File summaryFile) {
 		UIJob job = new UIJob("Update Test Runner") {
 			public IStatus runInUIThread(IProgressMonitor monitor) {
 				try {
 					TestRunSession session = PHPUnitModel.importTestRunSession(summaryFile);
 					notifyResultListener(session);
 				} catch (CoreException e) {
 					Logger.logException(e);
 				}
 				return Status.OK_STATUS;
 			}
 		};
 		job.schedule();
 	}
 
 	public IProblem[] runAllTestsInFolder(IFolder folder) {
 		try {
 			String cmdLineArgs = OperatingSystem.escapeShellFileArg(folder.getLocation().toOSString());
 
 			final File summaryFile = createTempSummaryFile(PHPUNIT_SUMMARY_FILE);
 			cmdLineArgs = "--log-junit " + OperatingSystem.escapeShellFileArg(summaryFile.toString()) + " "
 					+ cmdLineArgs;
 
 			PHPToolLauncher launcher = getProjectPHPToolLauncher(folder.getProject(), cmdLineArgs, folder.getLocation());
 			IProblem[] problems = parseOutput(folder.getProject(), launcher.launch(folder.getProject()));
 
 			importTestRunSession(summaryFile);
 
 			return problems;
 		} catch (IOException e) {
 			Logger.logException(e);
 		}
 
 		return new IProblem[0];
 	}
 
 	public IProblem[] runTestSuite(IFile file) {
 		try {
 			String cmdLineArgs = OperatingSystem.escapeShellFileArg(file.getLocation().toOSString());
 
 			final File summaryFile = createTempSummaryFile(PHPUNIT_SUMMARY_FILE);
 			cmdLineArgs = "--log-junit " + OperatingSystem.escapeShellFileArg(summaryFile.toString()) + " "
 					+ cmdLineArgs;
 
 			PHPToolLauncher launcher = getProjectPHPToolLauncher(file.getProject(), cmdLineArgs, file.getLocation());
 			IProblem[] problems = parseOutput(file.getProject(), launcher.launch(file.getProject()));
 
 			importTestRunSession(summaryFile);
 
 			return problems;
 		} catch (IOException e) {
 			Logger.logException(e);
 		}
 
 		return new IProblem[0];
 	}
 
 	protected IProblem[] parseOutput(IProject project, String output) {
 		ArrayList<IProblem> problems = new ArrayList<IProblem>();
 
 		String projectLocation = project.getLocation().toOSString();
 
 		if (output != null && output.length() > 0) {
 			Pattern pFailed = Pattern.compile("[0-9]+\\) .*");
 			Pattern pFileAndLine = Pattern.compile(".*:[0-9]+");
 
 			String[] lines = output.split("\n");
 			Matcher m = null;
 			for (int i = 0; i < lines.length; ++i) {
 				m = pFailed.matcher(lines[i].trim());
 				if (m.matches()) {
 					++i;
 					String msg = lines[i].trim();
 					++i;
 					while (i < lines.length && !"".equals(lines[i].trim())) {
 						msg += "\n" + lines[i].trim();
 						++i;
 					}
 
 					Matcher mf = null;
 					while (i < lines.length && (mf == null || !mf.matches())) {
 						mf = pFileAndLine.matcher(lines[i].trim());
 						if (!mf.matches())
 							++i;
 					}
 
 					String lineFailureLocation = null;
 					for (int x = 1; x >= 0; --x) {
 						if (lines.length > i + x && lines[i + x].lastIndexOf(':') != -1) {
 							lineFailureLocation = lines[i + x];
 							break;
 						}
 					}
 
 					if (lineFailureLocation != null) {
 						String file = lineFailureLocation.substring(0, lineFailureLocation.lastIndexOf(":"));
 
 						IResource testFile = project.findMember(file.substring(projectLocation.length()));
 						if (testFile != null) {
 							PHPSourceFile sourceFile;
 							try {
 								sourceFile = new PHPSourceFile((IFile) testFile);
 
 								int lineNumber = Integer.parseInt(lineFailureLocation.substring(lineFailureLocation
 										.lastIndexOf(":") + 1));
 
 								problems.add(new FileProblem((IFile) testFile, msg, IProblem.Task, new String[0],
 										ProblemSeverities.Error, sourceFile.lineStart(lineNumber), sourceFile
 												.lineEnd(lineNumber), lineNumber));
 								++i;
 							} catch (CoreException e) {
 								Logger.logException(e);
 							} catch (IOException e) {
 								Logger.logException(e);
 							}
 						}
 					}
 				}
 			}
 		}
 
 		return problems.toArray(new IProblem[0]);
 	}
 
 	private void createFolder(IFolder folder) throws CoreException {
 		IContainer parent = folder.getParent();
 		if (parent instanceof IFolder) {
 			createFolder((IFolder) parent);
 		}
 		if (!folder.exists()) {
 			folder.create(true, true, new NullProgressMonitor());
 		}
 	}
 
 	private PHPToolLauncher getProjectPHPToolLauncher(IProject project, String cmdLineArgs, IPath fileIncludePath) {
 		PHPUnitPreferences prefs = PHPUnitPreferencesFactory.factory(project);
 
 		String bootstrap = prefs.getBootstrap();
 		if (bootstrap != null && bootstrap.length() > 0) {
 			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 			if (root != null) {
 				IResource resource = root.findMember(bootstrap);
 				if (resource != null && resource.exists() && resource.getLocation() != null) {
 					cmdLineArgs = "--bootstrap "
 							+ OperatingSystem.escapeShellFileArg(resource.getLocation().toOSString()) + " "
 							+ cmdLineArgs;
 				}
 			}
 		}
 
 		PHPToolLauncher launcher = new PHPToolLauncher(QUALIFIED_NAME, getPHPExecutable(prefs.getPhpExecutable()),
 				getScriptFile(), cmdLineArgs, getPHPINIEntries(project, fileIncludePath));
 
 		launcher.setPrintOuput(prefs.isPrintOutput());
 
 		return launcher;
 	}
 
 	private INIFileEntry[] getPHPINIEntries(IProject project) {
 		IPath[] includePaths = PHPUnitPlugin.getDefault().getPluginIncludePaths(project);
 		return getPHPINIEntries(includePaths);
 	}
 
 	private INIFileEntry[] getPHPINIEntries(IProject project, IPath fileIncludePath) {
 		IPath[] pluginIncludePaths = PHPUnitPlugin.getDefault().getPluginIncludePaths(project);
 
 		IPath[] includePaths = new IPath[pluginIncludePaths.length + 1];
 		System.arraycopy(pluginIncludePaths, 0, includePaths, 0, pluginIncludePaths.length);
 		includePaths[includePaths.length - 1] = fileIncludePath;
 
 		return getPHPINIEntries(includePaths);
 	}
 
 	private INIFileEntry[] getPHPINIEntries(IPath[] includePaths) {
 
 		INIFileEntry[] entries;
 		if (includePaths.length > 0) {
 			entries = new INIFileEntry[] { INIFileUtil.createIncludePathEntry(includePaths) };
 		} else {
 			entries = new INIFileEntry[0];
 		}
 
 		return entries;
 	}
 
 	public static IPath getScriptFile() {
 		return PHPUnitPlugin.getDefault().resolvePluginResource("/php/tools/phpunit.php");
 	}
 
 	static public IFile searchTestCase(IFile file) {
 		ISourceModule module = PHPToolkitUtil.getSourceModule(file);
 		try {
 			IType[] types = module.getAllTypes();
 			if (types.length > 0) {
 				if (PHPToolkitUtil.hasSuperClass(module, PHPUNIT_TEST_CASE_CLASS))
 					return file;
 
 				SearchMatch[] matches = PHPSearchEngine.findClass(types[0].getElementName() + "Test", PHPSearchEngine
 						.createProjectScope(file.getProject()));
 
 				if (matches.length > 0)
 					return (IFile) matches[0].getResource();
 			}
 		} catch (ModelException e) {
 			Logger.logException(e);
 		}
 
 		return null;
 	}
 
 	static public IFile searchTestElement(IFile testCase) {
 		ISourceModule module = PHPToolkitUtil.getSourceModule(testCase);
 		try {
 			IType[] types = module.getAllTypes();
 			if (types.length > 0) {
 				if (!PHPToolkitUtil.hasSuperClass(module, PHPUNIT_TEST_CASE_CLASS))
 					return testCase;
 				String name = types[0].getElementName();
 				name = name.substring(0, name.length() - 4);
 				SearchMatch[] matches = PHPSearchEngine.findClass(name, PHPSearchEngine.createProjectScope(testCase
 						.getProject()));
 
 				if (matches.length > 0)
 					return (IFile) matches[0].getResource();
 			}
 		} catch (ModelException e) {
 			Logger.logException(e);
 		}
 
 		return null;
 	}
 
 	static public boolean isTestCase(IFile file) {
 		return PHPToolkitUtil.hasSuperClass(file, PHPUNIT_TEST_CASE_CLASS);
 	}
 
 	static public boolean isTestSuite(IFile file) {
 		ISourceModule module = PHPToolkitUtil.getSourceModule(file);
 		if (PHPToolkitUtil.hasSuperClass(module, PHPUNIT_TEST_SUITE_CLASS))
 			return true;
 
 		try {
 			IMethod method = PHPToolkitUtil.getClassMethod(module, "suite");
 			if (method != null) {
 				if (method.getSource().contains(PHPUNIT_TEST_SUITE_CLASS)) {
 					return true;
 				}
 
 				Pattern p = Pattern.compile(".*new ([a-zA-Z0-9_]+).*");
 
 				IModelElement[] elements = method.getChildren();
 				for (IModelElement e : elements) {
 					if (e instanceof IField) {
 						IField f = (IField) e;
 
 						Matcher m = p.matcher(f.getSource());
 						if (m.matches()) {
 							SearchMatch[] classes = PHPSearchEngine.findClass(m.group(1), PHPSearchEngine
 									.createProjectScope(file.getProject()));
 							for (SearchMatch c : classes) {
 								if (PHPToolkitUtil.hasSuperClass(c.getResource(), PHPUNIT_TEST_SUITE_CLASS))
 									return true;
 							}
 						}
 					}
 				}
 			}
 		} catch (ModelException e) {
 		}
 
 		return false;
 	}
 }
