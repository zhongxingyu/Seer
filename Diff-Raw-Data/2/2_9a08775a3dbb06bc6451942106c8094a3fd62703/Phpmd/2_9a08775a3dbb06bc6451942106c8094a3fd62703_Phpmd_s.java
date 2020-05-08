 /*******************************************************************************
  * Copyright (c) 2009, 2010 Dejan Spasic
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.phpsrc.eclipse.pti.tools.phpmd.core;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.php.internal.debug.core.preferences.PHPexeItem;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.phpsrc.eclipse.pti.core.AbstractPHPToolPlugin;
 import org.phpsrc.eclipse.pti.core.launching.OperatingSystem;
 import org.phpsrc.eclipse.pti.core.launching.PHPToolLauncher;
 import org.phpsrc.eclipse.pti.core.php.inifile.INIFileEntry;
 import org.phpsrc.eclipse.pti.core.php.inifile.INIFileUtil;
 import org.phpsrc.eclipse.pti.core.tools.AbstractPHPTool;
 import org.phpsrc.eclipse.pti.library.pear.PHPLibraryPEARPlugin;
 import org.phpsrc.eclipse.pti.tools.phpmd.PhpmdPlugin;
 import org.phpsrc.eclipse.pti.tools.phpmd.model.ViolationManager;
 import org.phpsrc.eclipse.pti.tools.phpmd.model.ViolationParser;
 
 @SuppressWarnings("restriction")
 public class Phpmd extends AbstractPHPTool {
 	public final static QualifiedName QUALIFIED_NAME = new QualifiedName(PhpmdPlugin.PLUGIN_ID, "phpmd");
 	public final static IPath SCRIPTPATH = PhpmdPlugin.getDefault().resolvePluginResource("/php/tools/phpmd.php");
 	private IResource resource;
 
 	public void execute(IResource selectedResource) {
 		resource = selectedResource;
 		PHPexeItem phpExec = getDefaultPhpExecutable();
 
 		if (null == phpExec) {
 			displayNoExecutalbeFoundDialog();
 			return;
 		}
 
 		String cmdLineArgs = buildCmdLineArgs();
 		INIFileEntry[] iniEntries = getPHPINIEntries();
 
 		PHPToolLauncher launcher = new PHPToolLauncher(QUALIFIED_NAME, phpExec, SCRIPTPATH, cmdLineArgs, iniEntries);
 		launcher.setPrintOuput(true);
 		String violationReport = launcher.launch(resource.getProject());
 
 		ViolationParser violationParser = new ViolationParser();
 
 		ViolationManager violationMgr = ViolationManager.getManager();
 		violationMgr.removeAllViolations();
 		violationMgr.addViolation(violationParser.parse(violationReport));
 	}
 
 	private void displayNoExecutalbeFoundDialog() {
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
 				MessageDialog.openError(shell, "PHP Mess Detector", "No executable php found");
 				System.err.println("No executable php found!");
 			}
 		});
 	}
 
 	private String buildCmdLineArgs() {
 		String location = getResourceLocationForCmdLine();
 		return String.format("%s xml %s", location, getRuleSetsForCmdLine());
 	}
 
 	private String getResourceLocationForCmdLine() {
 		return OperatingSystem.escapeShellFileArg(resource.getLocation().toOSString());
 	}
 
 	private String getRuleSetsForCmdLine() {
 		StringBuffer resultSets = new StringBuffer();
 		resultSets.append(RuleSet.CODESIZE.getFullPathname());
 		resultSets.append(",");
 		resultSets.append(RuleSet.NAMING.getFullPathname());
 		resultSets.append(",");
 		resultSets.append(RuleSet.UNUSEDCODE.getFullPathname());
 		return resultSets.toString();
 	}
 
 	private INIFileEntry[] getPHPINIEntries() {
 		INIFileEntry[] entries;
 		IPath[] pluginIncludePaths = PhpmdPlugin.getDefault().getPluginIncludePaths(resource.getProject());
 		if (pluginIncludePaths.length > 0) {
 			entries = new INIFileEntry[] { INIFileUtil.createIncludePathEntry(pluginIncludePaths) };
 		} else {
 			entries = new INIFileEntry[0];
 		}
 		return entries;
 	}
 
 	private enum RuleSet {
 		CODESIZE("codesize.xml"), UNUSEDCODE("unusedcode.xml"), NAMING("naming.xml"), DESIGN("design.xml");
 
 		private String ruleSetFilename;
 
		private String baseResoucePath = "/php/library/PEAR/data/PHP_PMD/rulesets"; //$NON-NLS-1$
 
 		private AbstractPHPToolPlugin resourceResolver = null;
 
 		RuleSet(String ruleSetFilename) {
 			this.ruleSetFilename = ruleSetFilename;
 		}
 
 		public AbstractPHPToolPlugin getResourceResolver() {
 			if (null == resourceResolver) {
 				resourceResolver = PHPLibraryPEARPlugin.getDefault();
 			}
 			return resourceResolver;
 		}
 
 		@SuppressWarnings("unused")
 		public void setResourceResolver(AbstractPHPToolPlugin resourceResolver) {
 			this.resourceResolver = resourceResolver;
 		}
 
 		@SuppressWarnings("unused")
 		public void resetResourceResolver() {
 			resourceResolver = null;
 		}
 
 		public String getFullPathname() {
 			IPath path = getResourceResolver().resolvePluginResource(getFilepath());
 			return OperatingSystem.escapeShellFileArg(path.toOSString());
 		}
 
 		private String getFilepath() {
 			return getBaseResoucePath() + "/" + getRuleSetFilename();
 		}
 
 		public String getBaseResoucePath() {
 			return baseResoucePath;
 		}
 
 		public String getRuleSetFilename() {
 			return ruleSetFilename;
 		}
 
 		@SuppressWarnings("unused")
 		public void setBaseResoucePath(final String baseResoucePath) {
 			String theBaseResourcePath = baseResoucePath;
 			if (theBaseResourcePath.lastIndexOf("/") == theBaseResourcePath.length()) {
 				theBaseResourcePath = theBaseResourcePath.substring(0, theBaseResourcePath.length());
 			}
 			this.baseResoucePath = theBaseResourcePath;
 		}
 	}
 }
