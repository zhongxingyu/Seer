 package org.phpsrc.eclipse.pti.tools.phpmd.core;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.php.internal.debug.core.preferences.PHPexeItem;
 import org.eclipse.php.internal.debug.core.preferences.PHPexes;
 import org.phpsrc.eclipse.pti.core.AbstractPHPToolPlugin;
 import org.phpsrc.eclipse.pti.core.launching.OperatingSystem;
 import org.phpsrc.eclipse.pti.core.launching.PHPToolLauncher;
 import org.phpsrc.eclipse.pti.core.php.inifile.INIFileEntry;
 import org.phpsrc.eclipse.pti.core.php.inifile.INIFileUtil;
 import org.phpsrc.eclipse.pti.core.tools.AbstractPHPTool;
 import org.phpsrc.eclipse.pti.library.pear.PHPLibraryPEARPlugin;
 import org.phpsrc.eclipse.pti.tools.phpmd.PhpmdPlugin;
 
 @SuppressWarnings("restriction")
 public class Phpmd extends AbstractPHPTool {
 	public final static QualifiedName QUALIFIED_NAME = new QualifiedName(PhpmdPlugin.PLUGIN_ID, "phpmd");
 
 	public enum RuleSet {
 		CODESIZE("codesize.xml"), UNUSEDCODE("unusedcode.xml"), NAMING("naming.xml");
 
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
 
 		public void setResourceResolver(AbstractPHPToolPlugin resourceResolver) {
 			this.resourceResolver = resourceResolver;
 		}
 
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
 
 		public void setBaseResoucePath(final String baseResoucePath) {
 			String theBaseResourcePath = baseResoucePath;
 			if (theBaseResourcePath.lastIndexOf("/") == theBaseResourcePath.length()) {
				theBaseResourcePath.substring(0, theBaseResourcePath.length());
 			}
 			this.baseResoucePath = theBaseResourcePath;
 		}
 	}
 
 	public void execute(IResource resource) {
 		PHPexeItem phpExec = getDefaultPhpExecutable();
 		String path = OperatingSystem.escapeShellFileArg(resource.getLocation().toOSString());
 
 		if (null == phpExec) {
 			System.err.println("No executable php found!");
 			return;
 		}
 
 		String cmdLineArgs = String.format("%s xml %s", path, determineRuleSets());
 
 		PHPToolLauncher launcher = new PHPToolLauncher(QUALIFIED_NAME, phpExec, getScriptFile(), cmdLineArgs,
 				getPHPINIEntries(resource.getProject(), resource.getLocation()));
 		launcher.setPrintOuput(true);
 		launcher.launch(resource.getProject());
 	}
 
 	private String determineRuleSets() {
 		StringBuffer resultSets = new StringBuffer();
 		resultSets.append(RuleSet.CODESIZE.getFullPathname());
 		resultSets.append(",");
 		resultSets.append(RuleSet.NAMING.getFullPathname());
 		resultSets.append(",");
 		resultSets.append(RuleSet.UNUSEDCODE.getFullPathname());
 		return resultSets.toString();
 	}
 
 	// -------------------------
 
 	private PHPexeItem getDefaultPhpExecutable() {
 		PHPexeItem defaultPhpExec = null;
 		for (PHPexeItem phpExec : PHPexes.getInstance().getAllItems()) {
 			if (phpExec.isDefault()) {
 				defaultPhpExec = phpExec;
 			}
 		}
 		return defaultPhpExec;
 	}
 
 	public static IPath getScriptFile() {
 		return PhpmdPlugin.getDefault().resolvePluginResource("/php/tools/phpmd.php");
 	}
 
 	private INIFileEntry[] getPHPINIEntries(IProject project, IPath fileIncludePath) {
 		IPath[] pluginIncludePaths = PhpmdPlugin.getDefault().getPluginIncludePaths(project);
 
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
 }
