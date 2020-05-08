 /*
  * (C) Copyright IBM Corporation 2007
  * 
  * This file is part of the Eclipse IMP.
  */
 package org.eclipse.imp.lpg.wizards;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.imp.core.ErrorHandler;
 import org.eclipse.imp.lpg.LPGPlugin;
 import org.eclipse.imp.lpg.LPGRuntimePlugin;
 import org.eclipse.imp.lpg.preferences.LPGPreferencesDialogConstants;
 import org.eclipse.imp.preferences.IPreferencesService;
 import org.eclipse.imp.preferences.PreferencesService;
 import org.eclipse.imp.runtime.RuntimePlugin;
 import org.eclipse.imp.wizards.GeneratedComponentWizard;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWizard;
 import org.osgi.framework.Bundle;
 
 public class NewLanguageSupportWizard extends GeneratedComponentWizard //ExtensionPointWizard
 {
     protected GrammarOptions fGrammarOptions;
     
     protected String fGrammarFileName;
     protected String fLexerFileName;
     protected String fKwlexerFileName;
     protected String fControllerFileName;
     protected String fLocatorFileName;
     
     static final String astDirectory= "Ast";
     static final String astNode= "ASTNode";
     
     static final String sAutoGenTemplate= "%options parent_saved,automatic_ast=toplevel,visitor=preorder,ast_directory=./" + astDirectory
 	    + ",ast_type=" + astNode;
     static final String sKeywordTemplate= "%options filter=kwTemplate.gi";
  
     private final static List<String /*pluginID*/> dependencies= new ArrayList<String>();
 
     static {
 		dependencies.add(RuntimePlugin.IMP_RUNTIME);
 		dependencies.add("org.eclipse.core.runtime");
 		dependencies.add("org.eclipse.core.resources");
 		dependencies.add("org.eclipse.imp.runtime");
 		dependencies.add("lpg.runtime");
     }
 
     protected List getPluginDependencies() {
     	return dependencies;
     }
 
     @Override
     protected void generateCodeStubs(IProgressMonitor mon) throws CoreException {
 	// TODO Auto-generated method stub
     }
 	
     protected static String getTemplateBundleID() {
         return LPGPlugin.kPluginID;
     }
 
     /**
      * We will accept the selection in the workbench to see if
      * we can initialize from it.
      * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
      */
     public void init(IWorkbench workbench, IStructuredSelection selection) {
         // this.selection = selection;
     }
 
     protected Map<String,String> getStandardSubstitutions() {
         Map<String,String> result= new HashMap<String,String>();
         result.put("$LANG_NAME$", fLanguageName);
         result.put("$CLASS_NAME_PREFIX$", fClassNamePrefix);
         result.put("$PACKAGE_NAME$", fPackageName);
         return result;
     }
     
     protected String fFileNamePrefix = null;
     
     protected void setFileNamePrefix() {
     	String projectLocation = fProject.getLocation().toString();
     	fFileNamePrefix = projectLocation + '/' +   getProjectSourceLocation() + fPackageName.replace('.', '/') + '/';
     }
     
     protected String getFileNamePrefix() {
     	if (fFileNamePrefix == null) {
     		setFileNamePrefix();
     	}
     	return fFileNamePrefix;
     }
 
     protected IFile createParseController(
     	String fileName, String templateName, boolean hasKeywords, IProject project, IProgressMonitor monitor)
     	throws CoreException
     {
     	// Note:  Not all substitution parameters may be used in all templates
 		Map<String,String> subs= getStandardSubstitutions();
 		subs.put("$AST_PKG_NODE$", fPackageName + "." + astDirectory + "." + astNode);
 		subs.put("$AST_NODE$", astNode);
 		subs.put("$PARSER_TYPE$", fClassNamePrefix + "Parser");
 		subs.put("$LEXER_TYPE$", fClassNamePrefix + "Lexer");
 		
 		// SMS 9 Sep 2007
 		// Added parameter for plugin id to take advantage of an alternative
 		// form of createFileFromTemplate
 		// (Did the same for similar invocations in other methods)
 		return createFileFromTemplate(fileName, LPGPlugin.kPluginID, templateName, fPackageFolder, subs, project, monitor);
     }
 
     protected IFile createNodeLocator(
 		String fileName, String templateName, IProject project, IProgressMonitor monitor) throws CoreException
     {
     	// Note:  Not all substitution parameters may be used in all templates
     	Map<String,String> subs= getStandardSubstitutions();
     	subs.put("$AST_PKG_NODE$", fPackageName + "." + astDirectory + "." + astNode);
     	subs.put("$AST_NODE$", astNode);
     	subs.put("$PARSER_TYPE$", fClassNamePrefix + "Parser");
     	subs.put("$LEXER_TYPE$", fClassNamePrefix + "Lexer");
 
     	return createFileFromTemplate(fileName, LPGPlugin.kPluginID, templateName, fPackageFolder, subs, project, monitor);
     }
 
     protected IFile createKWLexer(String fileName, String templateName,
     	    boolean hasKeywords, IProject project, IProgressMonitor monitor) throws CoreException
     {
 		Map<String,String> subs= getStandardSubstitutions();
 		subs.put("$TEMPLATE$", templateName);
 	
 		String kwLexerTemplateName = "kwlexer.gi";
 		return createFileFromTemplate(fileName, LPGPlugin.kPluginID, kwLexerTemplateName, fPackageFolder, subs, project, monitor);
     }
 
     protected IFile createLexer(String fileName, String templateName,
     	    boolean hasKeywords, IProject project, IProgressMonitor monitor) throws CoreException
     {
 		Map<String,String> subs= getStandardSubstitutions();
 	
 		subs.put("$TEMPLATE$", templateName);
 		subs.put("$KEYWORD_FILTER$",
 			hasKeywords ? ("%options filter=" + fClassNamePrefix + "KWLexer.gi") : "");
 		subs.put("$KEYWORD_LEXER$", hasKeywords ? ("$" + fClassNamePrefix + "KWLexer") : "Object");
 		subs.put("$LEXER_MAP$", (hasKeywords ? "LexerBasicMap" : "LexerVeryBasicMap"));
 	
 		String lexerTemplateName = "lexer.gi";
 		return createFileFromTemplate(fileName, LPGPlugin.kPluginID, lexerTemplateName, fPackageFolder, subs, project, monitor);
     }
 
     protected IFile createGrammar(String fileName, String templateName,
     	    boolean autoGenerateASTs, IProject project, IProgressMonitor monitor) throws CoreException
     {
 		Map<String,String> subs= getStandardSubstitutions();
 	
 		subs.put("$AUTO_GENERATE$", autoGenerateASTs ? sAutoGenTemplate : "");
 		subs.put("$TEMPLATE$", templateName);
 	
 		String grammarTemplateFileName = "grammar.g";
 		return createFileFromTemplate(fileName, LPGPlugin.kPluginID, grammarTemplateFileName, fPackageFolder, subs, project, monitor);
     }
 
     // Adapted from GeneratedComponentWizard
     /**
      * This method is called when 'Finish' button is pressed in the wizard.
      * We will create an operation and run it using wizard as execution context.
      * 
      * This method is quite a bit simpler than the corresponding method for
      * ExtensionPointWizard since no extensions have to be created here.
      */
     public boolean performFinish()
     {
     	// Do this in the UI thread while the wizard fields are still accessible
     	try {
     		collectCodeParms();
     	} catch (IllegalArgumentException e) {
     		// Exception might be thrown if selected package is not acceptable
 		    //ErrorHandler.reportError("NewLPGGrammarWizard.performFinish:  Could not collect parameters for stubs", e);
 		    return false;
     	}
     	// Invoke after collectCodeParms() so that collectCodeParms()
     	// can collect the names of files from the wizard
     	if (!okToClobberFiles(getFilesThatCouldBeClobbered()))
     		return false;
     	// Do we need to do just this in a runnable?  Evidently not.
     	try {
     		generateCodeStubs(new NullProgressMonitor());
     	} catch (Exception e){
		    ErrorHandler.reportError("NewLanguageSupportrWizard.performFinish:  Could not generate code stubs", e);
 		    return false;
     	}
     	return true;
     }
 
     protected void setIncludeDirPreference() {
         String lpgIncDirKey= LPGPreferencesDialogConstants.P_INCLUDEPATHTOUSE;
         Bundle lpgMetaToolingBundle= Platform.getBundle(LPGPlugin.kPluginID);
         URL templateDirURL= FileLocator.find(lpgMetaToolingBundle, new Path("/templates"), null);
         try {
             String lpgTemplatesDir= FileLocator.toFileURL(templateDirURL).getPath();
             if (Platform.getOS().equals(Platform.OS_WIN32)) {
             	if (lpgTemplatesDir.startsWith("/")) {
             		lpgTemplatesDir = lpgTemplatesDir.substring(1);            	}
             }
 
             IPreferencesService ps= new PreferencesService(fProject);
             ps.setLanguageName(LPGRuntimePlugin.getLanguageID());
             ps.setStringPreference(IPreferencesService.PROJECT_LEVEL, lpgIncDirKey, lpgTemplatesDir);
             ps.setBooleanPreference(IPreferencesService.PROJECT_LEVEL, LPGPreferencesDialogConstants.P_USEDEFAULTINCLUDEPATH, false);
         } catch (IOException e) {
             LPGPlugin.getInstance().getLog().log(new Status(IStatus.ERROR, LPGPlugin.kPluginID, 0, "Unable to resolve 'templates' directory in LPG metatooling plugin", null));
         }
     }
 }
