 package org.eclipse.gmf.codegen.templates.editor;
 
 import org.eclipse.gmf.codegen.gmfgen.*;
 import org.eclipse.emf.codegen.ecore.genmodel.*;
 import java.util.*;
 
 public class ManifestGenerator
 {
   protected static String nl;
   public static synchronized ManifestGenerator create(String lineSeparator)
   {
     nl = lineSeparator;
     ManifestGenerator result = new ManifestGenerator();
     nl = null;
     return result;
   }
 
   protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
   protected final String TEXT_1 = "Manifest-Version: 1.0" + NL + "Bundle-ManifestVersion: 2" + NL + "Bundle-Name: %pluginName" + NL + "Bundle-SymbolicName: ";
   protected final String TEXT_2 = "; singleton:=true" + NL + "Bundle-Version: ";
   protected final String TEXT_3 = NL + "Bundle-ClassPath: ." + NL + "Bundle-Activator: ";
   protected final String TEXT_4 = NL + "Bundle-Vendor: %providerName" + NL + "Bundle-Localization: plugin" + NL + "Export-Package: ";
   protected final String TEXT_5 = NL + "Require-Bundle: org.eclipse.core.runtime," + NL + " org.eclipse.core.resources," + NL + " org.eclipse.jface," + NL + " org.eclipse.ui.ide," + NL + " org.eclipse.ui.views," + NL + " org.eclipse.ui.workbench," + NL + " org.eclipse.ui.workbench.texteditor," + NL + " org.eclipse.emf.ecore," + NL + " org.eclipse.emf.ecore.xmi," + NL + " org.eclipse.emf.edit.ui," + NL + " org.eclipse.gef;visibility:=reexport," + NL + " org.eclipse.gmf.runtime.emf.core," + NL + " org.eclipse.gmf.runtime.emf.commands.core," + NL + " org.eclipse.gmf.runtime.emf.ui.properties," + NL + " org.eclipse.gmf.runtime.diagram.ui,";
   protected final String TEXT_6 = "org.eclipse.gmf.runtime.diagram.ui.printing.render,";
   protected final String TEXT_7 = NL + " org.eclipse.gmf.runtime.diagram.ui.providers," + NL + " org.eclipse.gmf.runtime.diagram.ui.providers.ide," + NL + " org.eclipse.gmf.runtime.diagram.ui.render," + NL + " org.eclipse.gmf.runtime.diagram.ui.resources.editor," + NL + " org.eclipse.gmf.runtime.diagram.ui.resources.editor.ide," + NL + " org.eclipse.gmf.runtime.notation.providers";
   protected final String TEXT_8 = ",";
   protected final String TEXT_9 = NL + " ";
   protected final String TEXT_10 = ";visibility:=reexport";
   protected final String TEXT_11 = NL + "Eclipse-LazyStart: true";
   protected final String TEXT_12 = NL;
 
   public String generate(Object argument)
   {
     final StringBuffer stringBuffer = new StringBuffer();
     
 final GenPlugin genPlugin = (GenPlugin) argument;
 final GenModel genModel = genPlugin.getEditorGen().getDomainGenModel();
 final Set requiredPluginIDs = new LinkedHashSet();
 requiredPluginIDs.add(genModel.getModelPluginID());
 requiredPluginIDs.add(genModel.getEditPluginID());
 
 for (Iterator it = genModel.getAllUsedGenPackagesWithClassifiers().iterator(); it.hasNext();) {
 	GenModel nextGenModel = ((GenPackage) it.next()).getGenModel();
 	if (nextGenModel.hasEditSupport()) {
 		requiredPluginIDs.add(nextGenModel.getModelPluginID());
 		requiredPluginIDs.add(nextGenModel.getEditPluginID());
 	}
 }
 
 requiredPluginIDs.addAll(genPlugin.getRequiredPluginIDs());
 Iterator requiredBundleIterator = requiredPluginIDs.iterator();
 
     stringBuffer.append(TEXT_1);
     stringBuffer.append(genPlugin.getID());
     stringBuffer.append(TEXT_2);
     stringBuffer.append(genPlugin.getVersion());
     stringBuffer.append(TEXT_3);
     stringBuffer.append(genPlugin.getActivatorQualifiedClassName());
     stringBuffer.append(TEXT_4);
     stringBuffer.append(genPlugin.getEditorGen().getEditor().getPackageName());
     stringBuffer.append(TEXT_5);
     if (genPlugin.isPrintingEnabled()) {
     stringBuffer.append(TEXT_6);
     }
     stringBuffer.append(TEXT_7);
     while(requiredBundleIterator.hasNext()) {
     stringBuffer.append(TEXT_8);
     stringBuffer.append(TEXT_9);
     stringBuffer.append(requiredBundleIterator.next());
     stringBuffer.append(TEXT_10);
     }
     stringBuffer.append(TEXT_11);
     stringBuffer.append(TEXT_12);
     return stringBuffer.toString();
   }
 }
