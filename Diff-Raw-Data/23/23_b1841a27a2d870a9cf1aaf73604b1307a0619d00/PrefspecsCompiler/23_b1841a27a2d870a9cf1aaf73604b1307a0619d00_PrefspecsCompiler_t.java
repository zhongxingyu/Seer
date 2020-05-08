 /*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 
 *******************************************************************************/
 
 package org.eclipse.imp.prefspecs.compiler;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import lpg.runtime.IAst;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.imp.builder.MarkerCreator;
 import org.eclipse.imp.core.ErrorHandler;
 import org.eclipse.imp.model.ISourceProject;
 import org.eclipse.imp.model.ModelFactory;
 import org.eclipse.imp.model.ModelFactory.ModelException;
 import org.eclipse.imp.parser.IParseController;
 import org.eclipse.imp.parser.SymbolTable;
 import org.eclipse.imp.preferences.IPreferencesService;
 import org.eclipse.imp.prefspecs.PrefspecsPlugin;
 import org.eclipse.imp.prefspecs.compiler.codegen.PreferencesFactory;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteBooleanFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteColorFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteComboFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteDirListFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteDirectoryFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteDoubleFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteFileFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteFontFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteIntFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteRadioFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.ConcreteStringFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.PreferencesPageInfo;
 import org.eclipse.imp.prefspecs.pageinfo.PreferencesTabInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualBooleanFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualColorFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualComboFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualDirListFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualDirectoryFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualDoubleFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualFileFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualFontFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualIntFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualRadioFieldInfo;
 import org.eclipse.imp.prefspecs.pageinfo.VirtualStringFieldInfo;
 import org.eclipse.imp.prefspecs.parser.PrefspecsParseController;
 import org.eclipse.imp.prefspecs.parser.Ast.*;
 import org.eclipse.imp.wizards.CodeServiceWizard;
import org.eclipse.imp.wizards.ExtensionEnabler;
 import org.eclipse.imp.wizards.ExtensionPointWizard;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.resource.StringConverter;
 import org.eclipse.pde.core.plugin.IExtensions;
 import org.eclipse.pde.core.plugin.IPluginAttribute;
 import org.eclipse.pde.core.plugin.IPluginExtension;
 import org.eclipse.pde.core.plugin.IPluginModel;
 import org.eclipse.pde.core.plugin.IPluginObject;
 import org.eclipse.pde.internal.core.bundle.BundlePluginModel;
 import org.eclipse.pde.internal.core.bundle.WorkspaceBundleModel;
 import org.eclipse.pde.internal.core.ibundle.IBundleModel;
 import org.eclipse.pde.internal.core.plugin.ImpPluginElement;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.ui.console.MessageConsoleStream;
 
 public class PrefspecsCompiler {
     private static class LabelledValueDescriptor {
         private final List<String> fValues= new ArrayList<String>();
         private final List<String> fLabels= new ArrayList<String>();
 
         public LabelledValueDescriptor(labelledStringValueList svList) {
             for(int i=0; i < svList.size(); i++) {
                 labelledStringValue lsv= svList.getlabelledStringValueAt(i);
                 stringValue sv= lsv.getoptLabel();
                 fValues.add(lsv.getidentifier().getIDENTIFIER().toString());
                 fLabels.add(sv != null ? sv.getSTRING_LITERAL().toString() : null);
             }
         }
         public int size() { return fValues.size(); }
         public String getValue(int i) { return fValues.get(i); }
         public String getLabel(int i) { return fLabels.get(i); }
         public List<String> getValues() { return fValues; }
         public List<String> getLabels() { return fLabels; }
     }
 
 	protected IProject fProject = null;
 	protected String fProjectName = null;
 	protected String fLanguageName = null;
 
 	protected List<PreferencesPageInfo> fPages = new ArrayList<PreferencesPageInfo>();
 
 	protected PreferencesPageInfo fPageInfo = null;
     protected IFile fPageInfoFile = null;
     
     // Parameters gathered from the page.genparameters file
 	protected String fPageClassNameBase = null;
 	protected String fPageId = null;
 	protected String fAlternativeMessage = null;
     protected String fPageName = null; // ignored - page names come directly from the "page xxx { }" construct
     protected String fPageMenuItem = null; // ignored - page names come directly from the "page xxx { }" construct
 
 	protected String fPagePackageName = null;
 
 	protected boolean fNoDetails = false;
 
 	private IFile fSpecFile;	
 
     private Map<String,LabelledValueDescriptor> fTypeMap= new HashMap<String, LabelledValueDescriptor>();
 
     private final MessageConsoleStream fConsoleStream;
 	
     public static String PROBLEM_MARKER_ID = PrefspecsPlugin.kPluginID + ".problem";
 
     public PrefspecsCompiler(String problem_marker_id, MessageConsoleStream messageConsoleStream) {
         PROBLEM_MARKER_ID = problem_marker_id;
         this.fConsoleStream= messageConsoleStream;
     }
 
     public List<PreferencesPageInfo> getPreferencesPageInfos(IFile specFile) {
     	computePreferencesPageInfo(specFile);
     	return fPages;
     }
 
     protected void computePreferencesPageInfo(IFile specFile) {
     	if (specFile == null) {
 		  fConsoleStream.println("PrefspecsCompiler.computePreferencesPageInfo(..):  returning null");
 		}
 		IProject project= specFile.getProject();
 		if (project == null) {
 		  fConsoleStream.println("PrefspecsCompiler.computePreferencesPageInfo(..):  Project is null; returning null");
 		}
 		ISourceProject sourceProject = null;
 		try {
 			sourceProject = ModelFactory.open(project);
 		} catch (ModelException me){
 		  fConsoleStream.println("PrefspecsCompiler.computePreferencesPageInfo(..):  Model exception:\n" + me.getMessage() + "\nReturning null");
 		}
 		IParseController parseController= new PrefspecsParseController();
 		
 		// Marker creator handles error messages from the parse controller
 		MarkerCreator markerCreator = new MarkerCreator(specFile, parseController, PROBLEM_MARKER_ID);
 		
 		// If we have a kind of parser that might be receptive, tell it
 		// what types of problem marker the builder will create
 		parseController.getAnnotationTypeInfo().addProblemMarkerType(PROBLEM_MARKER_ID);
 		
 		parseController.initialize(specFile.getProjectRelativePath(), sourceProject, markerCreator);
 		
 		parseController.parse(getFileContents(specFile), new NullProgressMonitor());
 		
 		ASTNode currentAst= (ASTNode) parseController.getCurrentAst();
 		
 		if (currentAst == null) {
 				fConsoleStream.println("PrefspecsCompiler.computePreferencesPageInfo:  current AST is null (parse errors?); unable to compute page info.");
 				return;
 		}
 		
 		TranslatorVisitor visitor = new TranslatorVisitor();
 		
 		currentAst.accept(visitor);
     }
     
     
     private final class TranslatorVisitor extends AbstractVisitor {
     	SymbolTable<IAst> innerScope;
 
         @Override
         public void unimplementedVisitor(String s) {
             // fConsoleStream.println("Don't know how to translate node type '" + s + "'.");
         }
 
         @Override
         public boolean visit(optPackageSpec ps) {
             PrefspecsCompiler.this.fPagePackageName= ps.getpackageName().toString();
             return false;
         }
 
         @Override
         public boolean visit(optDetailsSpec n) {
             PrefspecsCompiler.this.fNoDetails= n.getonOff().toString().equalsIgnoreCase("off");
             return false;
         }
 
         @Override
         public boolean visit(pageSpec p) {
         	final String pageName= p.getpageName().toString();
             fPageInfo = new PreferencesPageInfo(pageName);
             fPageInfo.setNoDetails(PrefspecsCompiler.this.fNoDetails);
         	fPages.add(fPageInfo);
 
         	if (p.getpageBody().gettabsSpec() == null) {
         	    createStandardTab(IPreferencesService.DEFAULT_LEVEL, false);
                 createStandardTab(IPreferencesService.CONFIGURATION_LEVEL, true);
                 createStandardTab(IPreferencesService.INSTANCE_LEVEL, true);
                 createStandardTab(IPreferencesService.PROJECT_LEVEL, true);
         	}
         	return true;
         }
 
         private void createStandardTab(String level, boolean editableRemovable) {
             PreferencesTabInfo tab = new PreferencesTabInfo(fPageInfo, level);
             tab.setIsEditable(editableRemovable);
             tab.setIsRemovable(editableRemovable);
             tab.setIsUsed(true);
         }
 
         @Override
         public void endVisit(pageSpec p) {
         	fPageInfo.dump(PrefspecsCompiler.this.fConsoleStream);
         }
 
         private <T> T findSpec(generalSpecList specs, Class<T> type) {
             for(int i=0; i < specs.size(); i++) {
                 IgeneralSpec spec= specs.getgeneralSpecAt(i);
                 if (type.isInstance(spec)) {
                     return (T) spec;
                 }
             }
             return null;
         }
 
         private void checkGeneralSpecs(AbstractASTNodeList specs) {
             Set<Class> specsSeen = new HashSet<Class>();
             for(int i=0; i < specs.size(); i++) {
                 Object elt = specs.getElementAt(i);
                 if (specsSeen.contains(elt.getClass())) {
                     createErrorMarker("Multiple property specifications of the same type for single entity.", specs);
                 }
                 specsSeen.add(elt.getClass());
             }
         }
 
         private void handleTabPropertySpecs(PreferencesTabInfo tabInfo, generalSpecList propSpecs) {
             checkGeneralSpecs(propSpecs);
 
             isEditableSpec editableSpec = findSpec(propSpecs, isEditableSpec.class);
             if (editableSpec != null) {
                 tabInfo.setIsEditable(editableSpec.getbooleanValue().toString().equals("true"));
             }
             isRemovableSpec removableSpec = findSpec(propSpecs, isRemovableSpec.class);
             if (removableSpec != null) {
                 tabInfo.setIsRemovable(removableSpec.getbooleanValue().toString().equals("true"));
             }
         }
 
         @Override
         public boolean visit(defaultTabSpec tabSpec) {
         	PreferencesTabInfo tabInfo = new PreferencesTabInfo(fPageInfo, IPreferencesService.DEFAULT_LEVEL);
         	generalSpecList propSpecs = tabSpec.getgeneralSpecs();
         	handleTabPropertySpecs(tabInfo, propSpecs);
 
         	isRemovableSpec removableSpec = findSpec(propSpecs, isRemovableSpec.class);
 
         	if (removableSpec != null) {
                 String removableValue= removableSpec.getbooleanValue().toString();
                 if (removableValue.equals("true")) {
                     createErrorMarker("The default level cannot be made removable", removableSpec);
                     tabInfo.setIsRemovable(false);
                 } else {
                     tabInfo.setIsRemovable(removableValue.equals("true"));
         		}
         	}
         	tabInfo.setIsUsed(tabSpec.getinout().toString().equals("in")); // Should always have an inout spec
         	return false;
         }
 
         @Override
         public boolean visit(configurationTabSpec tabSpec) {
         	PreferencesTabInfo tabInfo = new PreferencesTabInfo(fPageInfo, IPreferencesService.CONFIGURATION_LEVEL);
         	handleTabPropertySpecs(tabInfo, tabSpec.getgeneralSpecs());
             tabInfo.setIsUsed(tabSpec.getinout().toString().equals("in")); // Should always have an inout spec
         	return false;
         }
 
         @Override
         public boolean visit(instanceTabSpec tabSpec) {
         	PreferencesTabInfo tabInfo = new PreferencesTabInfo(fPageInfo, IPreferencesService.INSTANCE_LEVEL);
         	handleTabPropertySpecs(tabInfo, tabSpec.getgeneralSpecs());
         	tabInfo.setIsUsed(tabSpec.getinout().toString().equals("in")); // Should always have an inout spec
         	return false;
         }
 
         @Override
         public boolean visit(projectTabSpec tabSpec) {
         	PreferencesTabInfo tabInfo = new PreferencesTabInfo(fPageInfo, IPreferencesService.PROJECT_LEVEL);
         	handleTabPropertySpecs(tabInfo, tabSpec.getgeneralSpecs());
         	tabInfo.setIsUsed(tabSpec.getinout().toString().equals("in")); // Should always have an inout spec
         	return false;
         }
 
         private String unquoteString(ASTNodeToken stringLit) {
             String text= stringLit.toString();
             return unquoteString(text);
         }
 
         private String unquoteString(String text) {
             return text.substring(1, text.length() - 1);
         }
 
 	    private String getLabel(optLabelSpec labelSpec) {
 	        return unquoteString(labelSpec.getSTRING_LITERAL());
 	    }
 
 	    private boolean getValueOf(IbooleanValue bv) {
 	        return bv.toString().equals("true");
 	    }
 
         private String getValueOf(stringValue sv) {
             return sv.getSTRING_LITERAL().toString();
         }
 
         private int getValueOf(IsignedNumber sn) {
             if (sn instanceof signedNumber0) {
                 return Integer.parseInt(((signedNumber0) sn).getINTEGER().toString());
             } else {
                 signedNumber1 sn1= (signedNumber1) sn;
                 int absVal= Integer.parseInt(sn1.getINTEGER().toString());
                 return (sn1.getsign() instanceof sign1) ? -absVal : absVal;
             }
         }
 
         private int getValueOf(IfontStyle style) {
             if (style instanceof fontStyle0) {
                 return SWT.NORMAL;
             }
             if (style instanceof fontStyle1) {
                 return SWT.BOLD;
             }
             if (style instanceof fontStyle2) {
                 return SWT.ITALIC;
             }
             return SWT.NORMAL;
         }
 
 	    private VirtualBooleanFieldInfo findConditionalField(optConditionalSpec condSpec, String depFieldName) {
 	        String name = condSpec.getidentifier().getIDENTIFIER().toString();
             Iterator<VirtualFieldInfo> virtualFieldInfos = fPageInfo.getVirtualFieldInfos();    
             while (virtualFieldInfos.hasNext()) {
                 VirtualFieldInfo next = virtualFieldInfos.next();
                 String nextName = next.getName();
                 if (nextName.equals(name)) {
                     if (next instanceof VirtualBooleanFieldInfo) {
                         return (VirtualBooleanFieldInfo) next;
                     } else {
                         createErrorMarker("A field can only be made conditional on a boolean field", condSpec);
                     }
                 }
             }
             createErrorMarker("Field " + depFieldName + " is conditional on a non-existent field", condSpec);
             return null;
 	    }
 
 	    private void createErrorMarker(String msg, ASTNode node) {
 	        try {
 	            int startLine= node.getLeftIToken().getLine();
 	            int startOffset= node.getLeftIToken().getStartOffset();
 	            int endOffset= node.getRightIToken().getEndOffset()+1;
 
 	            IMarker m = PrefspecsCompiler.this.fSpecFile.createMarker(PROBLEM_MARKER_ID);
 	            String[] attributeNames = new String[] {IMarker.LINE_NUMBER, IMarker.CHAR_START, IMarker.CHAR_END, IMarker.MESSAGE, IMarker.PRIORITY, IMarker.SEVERITY};
 	            Object[] values = new Object[] {startLine, startOffset, endOffset, msg, IMarker.PRIORITY_HIGH, IMarker.SEVERITY_ERROR};
 
 	            m.setAttributes(attributeNames, values);
 	        } catch (CoreException e) {
                 PrefspecsPlugin.getInstance().logException("PrefspecsCompiler.createMarker: CoreException trying to create marker", e);
 	        } catch (Exception e) {
                 PrefspecsPlugin.getInstance().logException("PrefspecsCompiler.createMarker: Exception trying to create marker", e);
 	        }
         }
 
         private void setVirtualProperties(VirtualFieldInfo vField, AbstractASTNodeList generalSpecs, optConditionalSpec condSpec) {
             checkGeneralSpecs(generalSpecs);
             isEditableSpec editableSpec = findSpec(generalSpecs, isEditableSpec.class);
             if (editableSpec != null) {
                 vField.setIsEditable(getValueOf(editableSpec.getbooleanValue()));
             }
             isRemovableSpec removableSpec = findSpec(generalSpecs, isRemovableSpec.class);
             if (removableSpec != null) {
                 vField.setIsRemovable(getValueOf(removableSpec.getbooleanValue()));
             }
             optLabelSpec labelSpec = findSpec(generalSpecs, optLabelSpec.class);
             if (labelSpec != null) {
                 vField.setLabel(unquoteString(labelSpec.getSTRING_LITERAL()));
             }
             optToolTipSpec toolTipSpec = findSpec(generalSpecs, optToolTipSpec.class);
             if (toolTipSpec != null) {
                 vField.setToolTipText(unquoteString(toolTipSpec.getSTRING_LITERAL()));
             }
             if (condSpec != null) {
                 vField.setIsConditional(true);
                 vField.setConditionalWith(condSpec.getconditionType() instanceof conditionType0);
                 vField.setConditionField(findConditionalField(condSpec, vField.getName()));
             }
         }
 
         private void setConcreteProperties(VirtualFieldInfo vField, PreferencesTabInfo tab, ConcreteFieldInfo cField) {
             if (vField.hasEditableSpec()) {
                 cField.setIsEditable(vField.getIsEditable());
             } else {
                 cField.setIsEditable(tab.getIsEditable());
             }
             if (vField.hasRemovableSpec() && !(tab.getName().equals(IPreferencesService.DEFAULT_LEVEL))) {
                 cField.setIsRemovable(vField.getIsRemovable());
             } else {
                 cField.setIsRemovable(tab.getIsRemovable());
             }
             if (vField.getIsConditional()) {
                 cField.setIsConditional(true);
                 cField.setConditionalWith(vField.getConditionalWith());
                 cField.setConditionField(vField.getConditionField());
             }
         }
 
         private <T> T findSpec(AbstractASTNodeList specs, Class<T> type) {
             for(int i=0; i < specs.size(); i++) {
                 Object elt= specs.getElementAt(i);
                 if (type.isInstance(elt)) {
                     return (T) elt;
                 }
             }
             return null;
         }
 
         @Override
         public boolean visit(booleanFieldSpec boolField) {
         	VirtualBooleanFieldInfo vBool = new VirtualBooleanFieldInfo(fPageInfo, boolField.getidentifier().toString());
         	booleanFieldPropertySpecs propSpecs = boolField.getbooleanFieldPropertySpecs();
 
         	if (propSpecs != null) {
             	booleanSpecificSpecList booleanSpecificSpecs= propSpecs.getbooleanSpecificSpecs();
                 setVirtualProperties(vBool, booleanSpecificSpecs, boolField.getoptConditionalSpec());
 
                 booleanSpecialSpec specialSpec = findSpec(booleanSpecificSpecs, booleanSpecialSpec.class);
             	// hasSpecial <==> specialSpec != null (i.e., presence of spec indicates true)
             	if (specialSpec != null) {
             		vBool.setHasSpecialValue(true);
                 	vBool.setSpecialValue(getValueOf(specialSpec.getbooleanValue()));
             	} else {
             		vBool.setHasSpecialValue(false);
             	}
 
             	booleanDefValueSpec defValueSpec = findSpec(booleanSpecificSpecs, booleanDefValueSpec.class);
             	if (defValueSpec != null) {
             		vBool.setDefaultValue(getValueOf(defValueSpec.getbooleanValue()));
             	}
         	}
 
         	// Create an instance of a concrete field for each tab on the page
         	Iterator<?> tabs = fPageInfo.getTabInfos();
         	while (tabs.hasNext()) {
         		PreferencesTabInfo tab = (PreferencesTabInfo) tabs.next();
         		if (!tab.getIsUsed())
         			continue;
         		ConcreteBooleanFieldInfo cBool = new ConcreteBooleanFieldInfo(vBool, tab);
     		
         		// Set the attributes of the concrete field:
         		// if set in the virtual field, use that value;
         		// else if set for the tab, use that value;
         		// else rely on the default for the field type
             	setConcreteProperties(vBool, tab, cBool);
             	if (vBool.getHasSpecialValue()) {
             		cBool.setHasSpecialValue(true);
                 	cBool.setSpecialValue(vBool.getSpecialValue());
             	} else {
             		cBool.setHasSpecialValue(false);
             	}
         	}
         	return false;
         }
 
         @Override
         public boolean visit(dirListFieldSpec dirListField) {
         	VirtualDirListFieldInfo vDirList = new VirtualDirListFieldInfo(fPageInfo, dirListField.getidentifier().toString());
         	dirlistFieldPropertySpecs propSpecs = dirListField.getdirlistFieldPropertySpecs();
 
         	if (propSpecs != null) {
                 stringSpecificSpecList stringSpecificSpecs= propSpecs.getstringSpecificSpecs();
                 setVirtualProperties(vDirList, stringSpecificSpecs, dirListField.getoptConditionalSpec());
 
             	stringSpecialSpec specialSpec = findSpec(stringSpecificSpecs, stringSpecialSpec.class);
             	// hasSpecial <==> specialSpec != null (i.e., presence of spec indicates true)
             	if (specialSpec != null) {
             		vDirList.setHasSpecialValue(true);
                 	vDirList.setSpecialValue(getValueOf(specialSpec.getstringValue()));
             	} else {
             		vDirList.setHasSpecialValue(false);
             	}
 
             	stringDefValueSpec defValueSpec = findSpec(stringSpecificSpecs, stringDefValueSpec.class);
             	if (defValueSpec != null) {
             		vDirList.setDefaultValue(getValueOf(defValueSpec.getstringValue()));
             	}
 
             	IstringEmptySpec emptyValueSpec = findSpec(stringSpecificSpecs, IstringEmptySpec.class);
             	if (emptyValueSpec instanceof stringEmptySpec0) {
             		vDirList.setEmptyValueAllowed(false);
             		vDirList.setEmptyValue(null);
             	} else if (emptyValueSpec instanceof stringEmptySpec1) {
                		stringEmptySpec1 ses1 = (stringEmptySpec1) emptyValueSpec;
             		vDirList.setEmptyValueAllowed(true);
             		vDirList.setEmptyValue(getValueOf(ses1.getstringValue()));
             	}
         	}
         	
         	// Create an instance of a concrete field for each tab on the page
         	Iterator<PreferencesTabInfo> tabs = fPageInfo.getTabInfos();
         	while (tabs.hasNext()) {
         		PreferencesTabInfo tab = tabs.next();
         		if (!tab.getIsUsed())
         			continue;
         		ConcreteDirListFieldInfo cDirList = new ConcreteDirListFieldInfo(vDirList, tab);
         		
         		// Set the attributes of the concrete field:
         		// if set in the virtual field, use that value;
         		// else if set for the tab, use that value;
         		// else rely on the default for the field type
         		setConcreteProperties(vDirList, tab, cDirList);
 
         		if (vDirList.getHasSpecialValue()) {
             		cDirList.setHasSpecialValue(true);
                 	cDirList.setSpecialValue(vDirList.getSpecialValue());
             	} else {
             		cDirList.setHasSpecialValue(false);
             	}
             	if (vDirList.hasEmptyValueSpec()) {
 	            	if (!vDirList.getEmptyValueAllowed()) {
 	            		cDirList.setEmptyValueAllowed(false);
 	            		cDirList.setEmptyValue(null);
 	            	} else {
 	            		cDirList.setEmptyValueAllowed(true);
 	            		cDirList.setEmptyValue(vDirList.getEmptyValue());
 	            	}
             	}
         	}
         	return false;
         }
 
 
         @Override
         public boolean visit(directoryFieldSpec directoryField) {
             VirtualDirectoryFieldInfo vDir = new VirtualDirectoryFieldInfo(fPageInfo, directoryField.getidentifier().toString());
             directoryFieldPropertySpecs propSpecs = directoryField.getdirectoryFieldPropertySpecs();
             
             if (propSpecs != null) {
                 stringSpecificSpecList stringSpecificSpecs= propSpecs.getstringSpecificSpecs();
                 setVirtualProperties(vDir, stringSpecificSpecs, directoryField.getoptConditionalSpec());
             
                 stringSpecialSpec specialSpec = findSpec(stringSpecificSpecs, stringSpecialSpec.class);
                 // hasSpecial <==> specialSpec != null (i.e., presence of spec indicates true)
                 if (specialSpec != null) {
                     vDir.setHasSpecialValue(true);
                     vDir.setSpecialValue(getValueOf(specialSpec.getstringValue()));
                 } else {
                     vDir.setHasSpecialValue(false);
                 }
 
                 stringDefValueSpec defValueSpec = findSpec(stringSpecificSpecs, stringDefValueSpec.class);
                 if (defValueSpec != null) {
                     vDir.setDefaultValue(getValueOf(defValueSpec.getstringValue()));
                 }
             
                 IstringEmptySpec emptyValueSpec = findSpec(stringSpecificSpecs, IstringEmptySpec.class);
                 if (emptyValueSpec instanceof stringEmptySpec0) {
                     vDir.setEmptyValueAllowed(false);
                     vDir.setEmptyValue(null);
                 } else if (emptyValueSpec instanceof stringEmptySpec1) {
                     stringEmptySpec1 ses1 = (stringEmptySpec1) emptyValueSpec;
                     vDir.setEmptyValueAllowed(true);
                     vDir.setEmptyValue(getValueOf(ses1.getstringValue()));
                 }
             }
             
             // Create an instance of a concrete field for each tab on the page
             Iterator<PreferencesTabInfo> tabs = fPageInfo.getTabInfos();
             while (tabs.hasNext()) {
                 PreferencesTabInfo tab = tabs.next();
                 if (!tab.getIsUsed())
                     continue;
                 ConcreteDirectoryFieldInfo cFile = new ConcreteDirectoryFieldInfo(vDir, tab);
                 
                 // Set the attributes of the concrete field:
                 // if set in the virtual field, use that value;
                 // else if set for the tab, use that value;
                 // else rely on the default for the field type
                 setConcreteProperties(vDir, tab, cFile);
                 if (vDir.getHasSpecialValue()) {
                     cFile.setHasSpecialValue(true);
                     cFile.setSpecialValue(vDir.getSpecialValue());
                 } else {
                     cFile.setHasSpecialValue(false);
                 }
                 if (vDir.hasEmptyValueSpec()) {
                     if (!vDir.getEmptyValueAllowed()) {
                         cFile.setEmptyValueAllowed(false);
                         cFile.setEmptyValue(null);
                     } else {
                         cFile.setEmptyValueAllowed(true);
                         cFile.setEmptyValue(vDir.getEmptyValue());
                     }
                 }
             }
             return false;
         }
         
         @Override
         public boolean visit(fileFieldSpec fileField) {
         	VirtualFileFieldInfo vFile = new VirtualFileFieldInfo(fPageInfo, fileField.getidentifier().toString());
         	fileFieldPropertySpecs propSpecs = fileField.getfileFieldPropertySpecs();
         	
         	if (propSpecs != null) {
             	stringSpecificSpecList stringSpecificSpecs= propSpecs.getstringSpecificSpecs();
                 setVirtualProperties(vFile, stringSpecificSpecs, fileField.getoptConditionalSpec());
 
             	stringSpecialSpec specialSpec = findSpec(stringSpecificSpecs, stringSpecialSpec.class);
             	// hasSpecial <==> specialSpec != null (i.e., presence of spec indicates true)
             	if (specialSpec != null) {
             		vFile.setHasSpecialValue(true);
                 	vFile.setSpecialValue(getValueOf(specialSpec.getstringValue()));
             	} else {
             		vFile.setHasSpecialValue(false);
             	}
 
             	stringDefValueSpec defValueSpec = findSpec(stringSpecificSpecs, stringDefValueSpec.class);
             	if (defValueSpec != null) {
             		vFile.setDefaultValue(getValueOf(defValueSpec.getstringValue()));
             	}
         	
             	IstringEmptySpec emptyValueSpec = findSpec(stringSpecificSpecs, IstringEmptySpec.class);
             	if (emptyValueSpec instanceof stringEmptySpec0) {
             		vFile.setEmptyValueAllowed(false);
             		vFile.setEmptyValue(null);
             	} else if (emptyValueSpec instanceof stringEmptySpec1) {
                		stringEmptySpec1 ses1 = (stringEmptySpec1) emptyValueSpec;
             		vFile.setEmptyValueAllowed(true);
             		vFile.setEmptyValue(getValueOf(ses1.getstringValue()));
             	}
         	}
         	
         	// Create an instance of a concrete field for each tab on the page
         	Iterator<PreferencesTabInfo> tabs = fPageInfo.getTabInfos();
         	while (tabs.hasNext()) {
         		PreferencesTabInfo tab = tabs.next();
         		if (!tab.getIsUsed())
         			continue;
         		ConcreteFileFieldInfo cFile = new ConcreteFileFieldInfo(vFile, tab);
         		
         		// Set the attributes of the concrete field:
         		// if set in the virtual field, use that value;
         		// else if set for the tab, use that value;
         		// else rely on the default for the field type
         		setConcreteProperties(vFile, tab, cFile);
             	if (vFile.getHasSpecialValue()) {
             		cFile.setHasSpecialValue(true);
                 	cFile.setSpecialValue(vFile.getSpecialValue());
             	} else {
             		cFile.setHasSpecialValue(false);
             	}
             	if (vFile.hasEmptyValueSpec()) {
 	            	if (!vFile.getEmptyValueAllowed()) {
 	            		cFile.setEmptyValueAllowed(false);
 	            		cFile.setEmptyValue(null);
 	            	} else {
 	            		cFile.setEmptyValueAllowed(true);
 	            		cFile.setEmptyValue(vFile.getEmptyValue());
 	            	}
             	}
         	}
         	return false;
         }
         
         @Override
         public boolean visit(intFieldSpec intField) {
         	VirtualIntFieldInfo vInt = new VirtualIntFieldInfo(fPageInfo, intField.getidentifier().toString());
         	intFieldPropertySpecs propSpecs = intField.getintFieldPropertySpecs();
 
         	if (propSpecs != null) {
             	// Create a virtual field
                 intSpecificSpecList intSpecificSpecs = propSpecs.getintSpecificSpecs();
         	    setVirtualProperties(vInt, intSpecificSpecs, intField.getoptConditionalSpec());
 
             	intSpecialSpec specialSpec = findSpec(intSpecificSpecs, intSpecialSpec.class);
             	if (specialSpec != null) {
             		vInt.setHasSpecialValue(true);
             		vInt.setSpecialValue(getValueOf(specialSpec.getsignedNumber()));
             	} else {
             		vInt.setHasSpecialValue(false);
             	}
 
             	intDefValueSpec defValueSpec = findSpec(intSpecificSpecs, intDefValueSpec.class);
             	if (defValueSpec != null) {
             		vInt.setDefaultValue(getValueOf(defValueSpec.getsignedNumber()));
             	}
 
                 intRangeSpec rangeSpec = findSpec(intSpecificSpecs, intRangeSpec.class);
             	if (rangeSpec != null) {
             		int lowValue = getValueOf(rangeSpec.getlow());
             		int hiValue = getValueOf(rangeSpec.gethigh());
 
             		vInt.setRange(lowValue, hiValue);
             	}
         	}
         	
         	// Create an instance of a concrete field for each tab on the page
         	Iterator<PreferencesTabInfo> tabs = fPageInfo.getTabInfos();
         	while (tabs.hasNext()) {
         		PreferencesTabInfo tab = tabs.next();
         		if (!tab.getIsUsed())
         			continue;
         		ConcreteIntFieldInfo cInt = new ConcreteIntFieldInfo(vInt, tab);
 
         		// Set the attributes of the concrete field:
         		// if set in the virtual field, use that value;
         		// else if set for the tab, use that value;
         		// else rely on the default for the field type
         		setConcreteProperties(vInt, tab, cInt);
 
         		if (vInt.getHasSpecialValue()) {
             		cInt.setHasSpecialValue(true);
             		cInt.setSpecialValue(vInt.getSpecialValue());
             	} else {
             		cInt.setHasSpecialValue(false);
             	}
 
             	if (vInt.hasRangeSpec()) {
             		cInt.setRangeLow(vInt.getRangeLow());
             		cInt.setRangeHigh(vInt.getRangeHigh());
             	}
         	}
         	return false;
         }
 
 
         @Override
         public boolean visit(doubleFieldSpec doubleField) {
             VirtualDoubleFieldInfo vDouble = new VirtualDoubleFieldInfo(fPageInfo, doubleField.getidentifier().toString());
             doubleFieldPropertySpecs propSpecs = doubleField.getdoubleFieldPropertySpecs();
 
             if (propSpecs != null) {
                 // Create a virtual field
                 doubleSpecificSpecList doubleSpecificSpecs= propSpecs.getdoubleSpecificSpecs();
                 setVirtualProperties(vDouble, doubleSpecificSpecs, doubleField.getoptConditionalSpec());
 
                 doubleRangeSpec rangeSpec = findSpec(doubleSpecificSpecs, doubleRangeSpec.class);
                 doubleDefValueSpec defValueSpec = findSpec(doubleSpecificSpecs, doubleDefValueSpec.class);
 
                 if (defValueSpec != null) {
                     vDouble.setDefaultValue(Double.parseDouble(defValueSpec.getDECIMAL().toString()));
                 }
 
                 if (rangeSpec != null) {
                     double lowValue = Double.parseDouble(rangeSpec.getlow().toString());
                     double hiValue = Double.parseDouble(rangeSpec.gethigh().toString());
 
                     vDouble.setRange(lowValue, hiValue);
                 }
             }
             
             // Create an instance of a concrete field for each tab on the page
             Iterator<PreferencesTabInfo> tabs = fPageInfo.getTabInfos();
             while (tabs.hasNext()) {
                 PreferencesTabInfo tab = tabs.next();
                 if (!tab.getIsUsed())
                     continue;
                 ConcreteDoubleFieldInfo cDouble = new ConcreteDoubleFieldInfo(vDouble, tab);
 
                 // Set the attributes of the concrete field:
                 // if set in the virtual field, use that value;
                 // else if set for the tab, use that value;
                 // else rely on the default for the field type
                 setConcreteProperties(vDouble, tab, cDouble);
 
                 if (vDouble.hasRangeSpec()) {
                     cDouble.setRangeLow(vDouble.getRangeLow());
                     cDouble.setRangeHigh(vDouble.getRangeHigh());
                 }
             }
             return false;
         }
 
 
         @Override
         public boolean visit(stringFieldSpec stringField) {
         	VirtualStringFieldInfo vString = new VirtualStringFieldInfo(fPageInfo, stringField.getidentifier().toString());
         	stringFieldPropertySpecs propSpecs = stringField.getstringFieldPropertySpecs();
 
         	if (propSpecs != null) {
                 stringSpecificSpecList stringSpecificSpecs= propSpecs.getstringSpecificSpecs();
             	setVirtualProperties(vString, stringSpecificSpecs, stringField.getoptConditionalSpec());
 
                 stringSpecialSpec specialSpec = findSpec(stringSpecificSpecs, stringSpecialSpec.class);
 
             	if (specialSpec != null) {
             		vString.setHasSpecialValue(true);
                 	vString.setSpecialValue(getValueOf(specialSpec.getstringValue()));
             	} else {
             		vString.setHasSpecialValue(false);
             		//vString.setSpecialValue(null);
             	}
 
             	stringDefValueSpec defValueSpec = findSpec(stringSpecificSpecs, stringDefValueSpec.class);
             	if (defValueSpec != null) {
             		vString.setDefaultValue(getValueOf(defValueSpec.getstringValue()));
             	}
 
             	IstringEmptySpec emptyValueSpec = findSpec(stringSpecificSpecs, IstringEmptySpec.class);
             	if (emptyValueSpec instanceof stringEmptySpec0) {
             		vString.setEmptyValueAllowed(false);
             		vString.setEmptyValue(null);
             	} else if (emptyValueSpec instanceof stringEmptySpec1) {
                		stringEmptySpec1 ses1 = (stringEmptySpec1) emptyValueSpec;
             		vString.setEmptyValueAllowed(true);
             		vString.setEmptyValue(getValueOf(ses1.getstringValue()));
             	}
             	stringValidatorSpec validatorSpec= findSpec(stringSpecificSpecs, stringValidatorSpec.class);
             	if (validatorSpec != null) {
             	    String validatorQualClass= unquoteString(validatorSpec.getqualClassName().getSTRING_LITERAL().toString());
                     vString.setValidatorQualClass(validatorQualClass);
                     if (!findClass(validatorQualClass)) {
                         createErrorMarker("Validator class " + validatorQualClass + " does not exist", validatorSpec);
                     }
             	}
         	}
         	
         	// Create an instance of a concrete field for each tab on the page
         	Iterator<PreferencesTabInfo> tabs = fPageInfo.getTabInfos();
         	while (tabs.hasNext()) {
         		PreferencesTabInfo tab = tabs.next();
         		if (!tab.getIsUsed())
         			continue;
         		ConcreteStringFieldInfo cString = new ConcreteStringFieldInfo(vString, tab);
         		
         		// Set the attributes of the concrete field:
         		// if set in the virtual field, use that value;
         		// else if set for the tab, use that value;
         		// else rely on the default for the field type
         		setConcreteProperties(vString, tab, cString);
 
         		if (vString.getHasSpecialValue()) {
             		cString.setHasSpecialValue(true);
                 	cString.setSpecialValue(vString.getSpecialValue());
             	} else {
             		cString.setHasSpecialValue(false);
             	}
             	if (vString.hasEmptyValueSpec()) {
 	            	if (!vString.getEmptyValueAllowed()) {
 	            		cString.setEmptyValueAllowed(false);
 	            		cString.setEmptyValue(null);
 	            	} else {
 	            		cString.setEmptyValueAllowed(true);
 	            		cString.setEmptyValue(vString.getEmptyValue());
 	            	}
             	}
         	}
         	return false;
         }
 
 
         @Override
         public boolean visit(colorFieldSpec colorField) {
             VirtualColorFieldInfo vColor= new VirtualColorFieldInfo(fPageInfo, colorField.getidentifier().toString());
             colorFieldPropertySpecs propSpecs = colorField.getcolorFieldPropertySpecs();
 
             if (propSpecs != null) {
                 colorSpecificSpecList colorSpecificSpecs= propSpecs.getcolorSpecificSpecs();
                 setVirtualProperties(vColor, colorSpecificSpecs, colorField.getoptConditionalSpec());
 
                 colorDefValueSpec defValueSpec = findSpec(colorSpecificSpecs, colorDefValueSpec.class);
 
                 if (defValueSpec != null) {
                     int r= Integer.parseInt(defValueSpec.getred().toString());
                     int g= Integer.parseInt(defValueSpec.getgreen().toString());
                     int b= Integer.parseInt(defValueSpec.getblue().toString());
                     vColor.setDefaultColor(StringConverter.asString(new RGB(r, g, b)));
                 }
             }
             
             // Create an instance of a concrete field for each tab on the page
             Iterator<PreferencesTabInfo> tabs = fPageInfo.getTabInfos();
             while (tabs.hasNext()) {
                 PreferencesTabInfo tab = tabs.next();
                 if (!tab.getIsUsed())
                     continue;
                 ConcreteColorFieldInfo cColor= new ConcreteColorFieldInfo(vColor, tab);
                 
                 // Set the attributes of the concrete field:
                 // if set in the virtual field, use that value;
                 // else if set for the tab, use that value;
                 // else rely on the default for the field type
                 setConcreteProperties(vColor, tab, cColor);
             }
             return false;
         }
 
         @Override
         public boolean visit(fontFieldSpec fontField) {
             VirtualFontFieldInfo vFont= new VirtualFontFieldInfo(fPageInfo, fontField.getidentifier().toString());
             fontFieldPropertySpecs propSpecs = fontField.getfontFieldPropertySpecs();
 
             if (propSpecs != null) {
                 fontSpecificSpecList fontSpecificSpecs= propSpecs.getfontSpecificSpecs();
                 setVirtualProperties(vFont, fontSpecificSpecs, fontField.getoptConditionalSpec());
 
                 fontDefValueSpec defValueSpec = findSpec(fontSpecificSpecs, fontDefValueSpec.class);
 
                 if (defValueSpec != null) {
                     vFont.setDefaultName(getValueOf(defValueSpec.getname()));
                     vFont.setDefaultHeight(Integer.parseInt(defValueSpec.getheight().toString()));
                     vFont.setDefaultStyle(getValueOf(defValueSpec.getstyle()));
                 }
             }
             
             // Create an instance of a concrete field for each tab on the page
             Iterator<PreferencesTabInfo> tabs = fPageInfo.getTabInfos();
             while (tabs.hasNext()) {
                 PreferencesTabInfo tab = tabs.next();
                 if (!tab.getIsUsed())
                     continue;
                 ConcreteFontFieldInfo cFont= new ConcreteFontFieldInfo(vFont, tab);
                 
                 // Set the attributes of the concrete field:
                 // if set in the virtual field, use that value;
                 // else if set for the tab, use that value;
                 // else rely on the default for the field type
                 setConcreteProperties(vFont, tab, cFont);
             }
             return false;
         }
 
         @Override
         public boolean visit(typeSpec n) {
             String typeName= n.getidentifier().getIDENTIFIER().toString();
             LabelledValueDescriptor vd= new LabelledValueDescriptor(n.getlabelledStringValueList());
 
             fTypeMap.put(typeName, vd);
             return false;
         }
 
         @Override
         public boolean visit(comboFieldSpec comboField) {
             VirtualComboFieldInfo vCombo= new VirtualComboFieldInfo(fPageInfo, comboField.getidentifier().toString());
             comboFieldPropertySpecs propSpecs = comboField.getcomboFieldPropertySpecs();
 
             if (propSpecs != null) {
                 comboSpecificSpecList comboSpecificSpecs= propSpecs.getcomboSpecificSpecs();
                 comboDefValueSpec defValueSpec = findSpec(comboSpecificSpecs, comboDefValueSpec.class);
                 ItypeOrValuesSpec tovSpec = findSpec(comboSpecificSpecs, ItypeOrValuesSpec.class);
                 columnsSpec columnsSpec = findSpec(comboSpecificSpecs, columnsSpec.class);
                 LabelledValueDescriptor lvd;
 
                 if (tovSpec instanceof typeOrValuesSpec0) {
                     lvd= fTypeMap.get(((typeOrValuesSpec0) tovSpec).getidentifier().getIDENTIFIER().toString());
                 } else {
                     typeOrValuesSpec1 tovs1= (typeOrValuesSpec1) tovSpec;
                     lvd= new LabelledValueDescriptor(tovs1.getvaluesSpec().getlabelledStringValueList());
                 }
 
                 setVirtualProperties(vCombo, comboSpecificSpecs, comboField.getoptConditionalSpec());
 
                 if (defValueSpec != null) {
                     vCombo.setDefaultValue(defValueSpec.getidentifier().getIDENTIFIER().toString());
                 }
 
                 List<String> valuesList= lvd.getValues();
                 List<String> labelsList= lvd.getLabels();
 
                 vCombo.setValuesAndLabels(valuesList, labelsList);
 
                 if (columnsSpec != null) {
                     vCombo.setNumColumns(Integer.parseInt((columnsSpec.getINTEGER().toString())));
                 }
             }
 
             // Create an instance of a concrete field for each tab on the page
             Iterator<PreferencesTabInfo> tabs = fPageInfo.getTabInfos();
             while (tabs.hasNext()) {
                 PreferencesTabInfo tab = tabs.next();
                 if (!tab.getIsUsed())
                     continue;
                 ConcreteComboFieldInfo cCombo= new ConcreteComboFieldInfo(vCombo, tab);
                 
                 // Set the attributes of the concrete field:
                 // if set in the virtual field, use that value;
                 // else if set for the tab, use that value;
                 // else rely on the default for the field type
                 setConcreteProperties(vCombo, tab, cCombo);
             }
             return false;
         }
 
         @Override
         public boolean visit(radioFieldSpec radioField) {
             VirtualRadioFieldInfo vRadio= new VirtualRadioFieldInfo(fPageInfo, radioField.getidentifier().toString());
             radioFieldPropertySpecs propSpecs = radioField.getradioFieldPropertySpecs();
 
             if (propSpecs != null) {
                 radioSpecificSpecList radioSpecificSpecs= propSpecs.getradioSpecificSpecs();
                 radioDefValueSpec defValueSpec = findSpec(radioSpecificSpecs, radioDefValueSpec.class);
                 ItypeOrValuesSpec tovSpec = findSpec(radioSpecificSpecs, ItypeOrValuesSpec.class);
                 columnsSpec columnsSpec = findSpec(radioSpecificSpecs, columnsSpec.class);
 
                 LabelledValueDescriptor lvd;
 
                 if (tovSpec instanceof typeOrValuesSpec0) {
                     lvd= fTypeMap.get(((typeOrValuesSpec0) tovSpec).getidentifier().getIDENTIFIER().toString());
                 } else {
                     typeOrValuesSpec1 tovs1= (typeOrValuesSpec1) tovSpec;
                     lvd= new LabelledValueDescriptor(tovs1.getvaluesSpec().getlabelledStringValueList());
                 }
 
                 setVirtualProperties(vRadio, radioSpecificSpecs, radioField.getoptConditionalSpec());
 
                 if (defValueSpec != null) {
                     vRadio.setDefaultValue(defValueSpec.getidentifier().getIDENTIFIER().toString());
                 }
 
                 List<String> valuesList= lvd.getValues();
                 List<String> labelsList= lvd.getLabels();
 
                 vRadio.setValuesAndLabels(valuesList, labelsList);
 
                 if (columnsSpec != null) {
                     vRadio.setNumColumns(Integer.parseInt((columnsSpec.getINTEGER().toString())));
                 }
             }
 
             // Create an instance of a concrete field for each tab on the page
             Iterator<PreferencesTabInfo> tabs = fPageInfo.getTabInfos();
             while (tabs.hasNext()) {
                 PreferencesTabInfo tab = tabs.next();
                 if (!tab.getIsUsed())
                     continue;
                 ConcreteRadioFieldInfo cRadio= new ConcreteRadioFieldInfo(vRadio, tab);
                 
                 // Set the attributes of the concrete field:
                 // if set in the virtual field, use that value;
                 // else if set for the tab, use that value;
                 // else rely on the default for the field type
                 setConcreteProperties(vRadio, tab, cRadio);
             }
             return false;
         }
 
         /*
          * Context for processing of custom rules 
          */ 
         String customTabName = null;
         PreferencesTabInfo customTabInfo = null;
         String customFieldName = null;
         ConcreteFieldInfo customFieldInfo = null;
         
         
         @Override
         public boolean visit(customRule custRule)
         {
         	// Set context for custom spec processing
         	customTabName = custRule.gettab().toString();
         	customFieldName = custRule.getidentifier().toString();
         	
         	customTabInfo = fPageInfo.getTabInfo(customTabName);
         	Iterator<ConcreteFieldInfo> concreteFields = customTabInfo.getConcreteFields();
         	// Should always have a field with the given name
         	while (concreteFields.hasNext()) {
         		ConcreteFieldInfo next = concreteFields.next();
         		if (next.getName().equals(customFieldName)) {
         			customFieldInfo = next;
         			break;
         		}	
         	}
         	if (customFieldInfo == null) {
         		// What to do?  Seems that specification is erroneous
         		fConsoleStream.println("PrefspecsCompiler.TranslatorVisitor.visit(customRule):\n    found no field info corresponding to field name = '" +
         				customFieldName + "';\n    continuing at your own risk");
         	}
         	
         	return true;
         }
         
         
         @Override
         public void endVisit(customRule custRule)
         {
         	// Reset context of custom spec processing
         	customTabName = null;
         	customTabInfo = null;
         	customFieldName = null;
         	customFieldInfo = null;	
         }
         
 
         // Use endVisit(..) methods to get information from specs because when
         // visit(..)ed they don't yet have the info that's needed
         
         
         @Override
         public void endVisit(generalSpecList genSpecs)
         {
         	// Process general spec as part of a custom rule, if processing a custom rule
         	if (customTabName != null) {
         		isEditableSpec editableSpec= findSpec(genSpecs, isEditableSpec.class);
                 if (editableSpec != null)
 	        		customFieldInfo.setIsEditable(editableSpec.getbooleanValue().toString().equals("true"));
 
                 isRemovableSpec removableSpec= findSpec(genSpecs, isRemovableSpec.class);
                 if (removableSpec != null) {
                     String removableValue= removableSpec.getbooleanValue().toString();
                     if (removableValue.equals("true")) {
                         createErrorMarker("Illegal removable specification.", removableSpec);
                         customFieldInfo.setIsRemovable(false);
                     } else {
                         customFieldInfo.setIsRemovable(false);
                     }
                 }
         	}
         }
         
         
         @Override
         public void endVisit(booleanSpecialSpec specialSpec)
         {
         	// Process special spec as part of a boolean custom spec,
         	// if processing a custom rule
         	// If the specialSpec is not null, then the field has a special spec
         	// and the value of that spec is the special value for the field
         	if (customTabName != null) {
         		// Don't think the boolean value can be null if we have a specialSpec	
         		((ConcreteBooleanFieldInfo)customFieldInfo).setSpecialValue(specialSpec.getbooleanValue().toString().equals("true"));
         	}
         }
         
         
         @Override
         public void endVisit(stringSpecialSpec specialSpec)
         {
         	// Process special spec as part of a string custom spec,
         	// if processing a custom rule
         	// If the specialSpec is not null, then the field has a special spec
         	// and the value of that spec is the special value for the field
         	if (customTabName != null) {
         		// Don't think the boolean value can be null if we have a specialSpec	
         		((ConcreteStringFieldInfo)customFieldInfo).setSpecialValue(specialSpec.getstringValue().getSTRING_LITERAL().toString());
         	}
         }
         
 
         
         /*
          * For processing of conditional rules
          */
         
         /*
          * For "with" condition rules
          */
         @Override
         public boolean visit(conditionalSpec0 rule) {
         	
         	String conditionalFieldName = rule.getidentifier().toString();
         	String conditionFieldName = rule.getidentifier3().toString();
         	VirtualFieldInfo conditionalFieldInfo = null;
         	VirtualFieldInfo conditionFieldInfo = null;
         	
         	Iterator<VirtualFieldInfo> virtualFieldInfos = fPageInfo.getVirtualFieldInfos();	
         	while (virtualFieldInfos.hasNext()) {
         		VirtualFieldInfo next = virtualFieldInfos.next();
         		String nextName = next.getName();
         		if (nextName.equals(conditionalFieldName)) {
         			conditionalFieldInfo = next;
         		} else if (nextName.equals(conditionFieldName)) {
         			conditionFieldInfo = next;
         		}
         		if (conditionalFieldInfo != null && conditionFieldInfo != null)
         			break;
         	}
         	conditionalFieldInfo.setIsConditional(true);
         	conditionalFieldInfo.setConditionalWith(true);
         	// if we're compiling, then the AST should be correct,
         	// in which case the condition field should always be
         	// a boolean field
         	conditionalFieldInfo.setConditionField((VirtualBooleanFieldInfo)conditionFieldInfo);
         	
         	return false;
         }
         
         
         /*
          * For "against" condition rules
          */
         @Override
         public boolean visit(conditionalSpec1 rule) {
         	
         	String conditionalFieldName = rule.getidentifier().toString();
         	String conditionFieldName = rule.getidentifier3().toString();
         	VirtualFieldInfo conditionalFieldInfo = null;
         	VirtualFieldInfo conditionFieldInfo = null;
         	
         	Iterator<VirtualFieldInfo> virtualFieldInfos = fPageInfo.getVirtualFieldInfos();	
         	while (virtualFieldInfos.hasNext()) {
         		VirtualFieldInfo next = virtualFieldInfos.next();
         		String nextName = next.getName();
         		if (nextName.equals(conditionalFieldName)) {
         			conditionalFieldInfo = next;
         		} else if (nextName.equals(conditionFieldName)) {
         			conditionFieldInfo = next;
         		}
         		if (conditionalFieldInfo != null && conditionFieldInfo != null)
         			break;
         	}
         	conditionalFieldInfo.setIsConditional(true);
         	conditionalFieldInfo.setConditionalWith(false);
         	// if we're compiling, then the AST should be correct,
         	// in which case the condition field should always be
         	// a boolean field
         	conditionalFieldInfo.setConditionField((VirtualBooleanFieldInfo)conditionFieldInfo);
         	
         	return false;
         }
         
         
         
         @Override
         public void endVisit(conditionalSpecs0 spec) {
         	propagateConditionsToConcreteSpecs();
         }
         
         
         @Override
         public void endVisit(conditionalSpecs1 spec) {
         	propagateConditionsToConcreteSpecs();
         }
         
         
         protected void propagateConditionsToConcreteSpecs()
         {
         	Iterator<VirtualFieldInfo> vFieldInfos = fPageInfo.getVirtualFieldInfos();
         	while (vFieldInfos.hasNext()) {
         		VirtualFieldInfo vInfo = vFieldInfos.next();
         		if (vInfo.getIsConditional()) {
         			Iterator<ConcreteFieldInfo> cFieldInfos = vInfo.getConcreteFieldInfos();
         			while (cFieldInfos.hasNext()) {
         				ConcreteFieldInfo cInfo = cFieldInfos.next();
         				cInfo.setIsConditional(true);
         				cInfo.setConditionalWith(vInfo.getConditionalWith());
         				cInfo.setConditionField(vInfo.getConditionField());
         			}
         		}
         	}	
         }
     }
 
     
     public boolean findClass(String validatorQualClass) {
         IJavaProject javaProj= JavaCore.create(fProject);
         String validatorPathSuffix= validatorQualClass.replace('.', File.separatorChar);
         try {
             return javaProj.findElement(new Path(validatorPathSuffix).addFileExtension("java")) != null;
         } catch (JavaModelException e) {
             return false;
         }
     }
 
 
     private List<IClasspathEntry> getSourceCPEntries(IJavaProject javaProj) {
         List<IClasspathEntry> result= new ArrayList<IClasspathEntry>();
         try {
             IClasspathEntry[] cpEntries= javaProj.getResolvedClasspath(true);
             for(int i= 0; i < cpEntries.length; i++) {
                 if (cpEntries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                     result.add(cpEntries[i]);
                 }
             }
         } catch (JavaModelException e) {
             
         }
         return result;
     }
 
 
     public String getFileContents(IFile file) {
         char[] buf= null;
         try {
             File javaFile= new File(file.getLocation().toOSString());
             FileReader fileReader= new FileReader(javaFile);
             int len= (int) javaFile.length();
 
             buf= new char[len];
             fileReader.read(buf, 0, len);
             return new String(buf);
         } catch(FileNotFoundException fnf) {
             fConsoleStream.println(fnf.getMessage());
             return "";
         } catch(IOException io) {
             fConsoleStream.println(io.getMessage());
             return "";
         }
     }
 
 
     public void compile(final IFile specFile, final IProgressMonitor mon) {
 	    this.fSpecFile = specFile;
 		IWorkspaceRunnable wsop= new IWorkspaceRunnable() {
 		    public void run(IProgressMonitor monitor) throws CoreException {
 				getGenerationParameters(specFile);
 				collectCodeParms(specFile);
 				if (fPageId == null) {
 				    PrefspecsCompiler.this.fPageId= PrefspecsCompiler.this.fPagePackageName;
 				}
 				
 				// Remove previous extensions of this point, but only if
 				// they have the same extension id
 				// (extension "id" is an attribute of the "page" child
 				// of the "preferencePage" extension)
 				IProject project = specFile.getProject();
				IPluginModel pluginModel = ExtensionEnabler.getPluginModel(project);
 				
 				// SMS 30 Jul 2008
 			    if (pluginModel instanceof BundlePluginModel) {
 			    	BundlePluginModel bpm = (BundlePluginModel) pluginModel;
 			    	IBundleModel bm = bpm.getBundleModel();
 			    	if (bm instanceof WorkspaceBundleModel) {
 			    		((WorkspaceBundleModel)bm).setEditable(true);
 			    	}
 			    }
 				
 				// Load the IMP way to get the complete model
    	    	ExtensionEnabler.loadImpExtensionsModel(pluginModel, project);
 		    	IExtensions pmExtensions = pluginModel.getExtensions();
 		    	IPluginExtension[] pluginExtensions = pmExtensions.getExtensions();
 		    	for (int i = 0; i < pluginExtensions.length; i++) {
 		    		IPluginExtension pluginExtension = pluginExtensions[i];
 		    		if (pluginExtension == null) continue;
 		    		if (pluginExtension.getPoint() == null) continue;
 		    		String point = "org.eclipse.ui" + "." + "preferencePages";
 		    		if (pluginExtension.getPoint() == null) continue;
 		    		if (pluginExtension.getPoint().equals(point)) {
 		    			IPluginObject[] children = pluginExtension.getChildren();
 		    			for (int j = 0; j < children.length; j++) {
 		    	            if(children[j].getName().equals("page")) {
 		    	            	ImpPluginElement ipe = (ImpPluginElement) children[j];
 		    	            	IPluginAttribute pa = ipe.getAttribute("id");
 		    	            	if (pa != null && pa.getValue().startsWith(fPageId)) {
 				    				pmExtensions.remove(pluginExtension);
 		    	            	}
 		    	            }
 		    			}
 		    		}
 		    	}
		    	ExtensionEnabler.saveAndRefresh(pluginModel);
 
                 generateCodeStubs(specFile, mon);
 				
 				for(PreferencesPageInfo pageInfo: fPages) {
 				    String pageName= pageInfo.getPageName();
                     int lastCompIdx= (pageName.indexOf('.') > 0) ? pageName.lastIndexOf('.') + 1 : 0;
 				    String pageParent= (lastCompIdx > 0) ? (fPageId + "." + pageName.substring(0, lastCompIdx-1)) : "";
 				    String pageLabel= pageName.substring(lastCompIdx);
 
				    ExtensionEnabler.enable(
     					specFile.getProject(), "org.eclipse.ui", "preferencePages", 
     					new String[][] {
     						{ "page:id", fPageId + "." + pageName },
     						{ "page:name", pageLabel },
     						{ "page:class", fPagePackageName + "." + (pageName.replaceAll("\\.", "")) + "PreferencePage" },
     						{ "page:category", pageParent },
     					},
     					false,	// do not just replace previous extension, there may be more than one page
     					getPluginDependencies(),
     					monitor);
 				}
 		    }	
 		};
 		try {
 			wsop.run(new NullProgressMonitor());
 		} catch (CoreException e) {
 			ErrorHandler.reportError("PrefspecsCompiler.performGeneration:  CoreException:  ", e);
 		}
 	}
 	
 	
 	protected void collectCodeParms(IFile file)
 	{	
 		fProject = file.getProject();
 		fProjectName = fProject.getName();
 		
 		IPath filePath = file.getLocation();
 		IPath projectPath = file.getProject().getLocation();
 		IPath packagePath = filePath.removeFirstSegments(projectPath.segmentCount() + getProjectSourceLocationPath().segmentCount());
 		packagePath = packagePath.removeLastSegments(1);
 		fPagePackageName = packagePath.toString();
 		fPagePackageName = fPagePackageName.substring(fPagePackageName.indexOf(':')+1);
 		fPagePackageName = fPagePackageName.replace('\\', '.');
 		fPagePackageName = fPagePackageName.replace('/', '.');
 
 		// Obtain through getGenerationParameters()
 		//fPageClassNameBase = getPageInfo(file, new NullProgressMonitor()).getPageName();
 		fLanguageName = CodeServiceWizard.discoverProjectLanguage(file.getProject());	
 	}
 	
 	
 	protected void getGenerationParameters(IFile file)
 	throws CoreException
 	{
 		// Get the generation-parameters file
 		IFile genParamsFile = null;
 		IContainer parent = file.getParent();
 		IResource[] siblings = parent.members();
 		for (int i = 0; i < siblings.length; i++) {
 			if (siblings[i].getName().endsWith(".genparams")) {
 				genParamsFile = (IFile) siblings[i];
 				break;
 			}
 		}
 		if (genParamsFile == null) {
 			fConsoleStream.println("PrefspecsCompiler.getGenerationParameters:  no parameters file found located with specification file = " + file.getName());
 			return;
 		}
 
         try {
             File f= new File(genParamsFile.getLocation().toOSString());	
             BufferedReader rdr= new BufferedReader(new FileReader(f));
             String line;
 
             // In the following, the values given to "startsWith(..)" are
             // those expected to be written by NewPreferencesSpecificationWizard
             while ((line = rdr.readLine()) != null) {
             	if (line.endsWith("\n"))
             		line = line.substring(0, line.length()-1);
             	int valStart = line.indexOf("=") + 1;
             	if (line.startsWith("PageClassNameBase"))
             		fPageClassNameBase 	= line.substring(valStart);
             	else if (line.startsWith("PageName"))
             		fPageName = line.substring(valStart);
             	if (line.startsWith("PageId"))
             		fPageId	= line.substring(valStart);	
             	if (line.startsWith("PageMenuItem")) {
             		fPageMenuItem = line.substring(valStart);
             		if (fPageMenuItem.equals("TOP")) {
             			fPageMenuItem = "";
             		}
             	}
             	if (line.startsWith("AlternativeMessage"))
             		fAlternativeMessage	= line.substring(valStart);
             }
         } catch (FileNotFoundException e) {
             logError(e);
             e.printStackTrace();
         } catch (IOException e) {
             logError(e);
             e.printStackTrace();
         }
 	}
 	
 	
 	public void generateCodeStubs(IFile specFile, IProgressMonitor mon) throws CoreException
 	{
 		IProject fProject = specFile.getProject();
         Map<String,String> subs= getStandardSubstitutions(fProject);
 
         subs.put("$PREFS_CLASS_NAME$", fPageClassNameBase);
 
 //            subs.put("$PREFS_ALTERNATIVE_MESSAGE$", fAlternativeMessage);
 //            IFile pageSrc = createFileFromTemplate(fClassNamePrefix + ".java", "preferencesPageAlternative.java", fPackageFolder, subs, fProject, mon);
 //            editFile(mon, pageSrc);
 //            return;
 //        }	
         
         // Generating a full tabbed preference page
 
         List<PreferencesPageInfo> pageInfos = getPreferencesPageInfos(specFile);
 
         if (specFile.findMarkers(PROBLEM_MARKER_ID, true, 0).length > 0) {
             return;
         }
         fPageClassNameBase = pageInfos.get(0).getPageName();
         String constantsClassName = fPageClassNameBase + "Constants";
         String initializerClassName = fPageClassNameBase + "Initializer";
 
 		ISourceProject sourceProject = null;
     	try {
     		sourceProject = ModelFactory.open(fProject);
     	} catch (ModelException me) {
             fConsoleStream.println("PrefspecsCompiler.generateCodeStubs(..): Model exception:\n" + me.getMessage() + "\nReturning without parsing");
             return;
     	}
 
         subs.put("$PREFS_PACKAGE_NAME$", fPagePackageName);
 
         PreferencesFactory prefFactory= new PreferencesFactory(fConsoleStream);
 
         String projectSourceLoc= ExtensionPointWizard.getProjectSourceLocation(fProject);
         String pluginPkgName= getPluginPackageName(fProject, null);
         String pluginClassName= getPluginClassName(fProject, null);
 
         prefFactory.generatePreferencesConstants(pageInfos, sourceProject, projectSourceLoc, fPagePackageName, constantsClassName, mon);
 
         prefFactory.generatePreferencesInitializers(pageInfos,
         		pluginPkgName, pluginClassName, constantsClassName,	
         		sourceProject, projectSourceLoc, fPagePackageName, fPageClassNameBase + "Initializer",  mon);
 
         subs.put("$PREFS_INIT_CLASS_NAME$", fPageClassNameBase + "Initializer");
 
         for(PreferencesPageInfo pageInfo: pageInfos) {
             final String javaPageName= pageInfo.getPageName().replaceAll("\\.", "");
 
             subs.put("$PREFS_CLASS_NAME$", javaPageName);
 
             prefFactory.generatePreferencesPage(pageInfo, pluginPkgName, pluginClassName,
                     constantsClassName, initializerClassName, sourceProject, projectSourceLoc, fPagePackageName, javaPageName + "PreferencePage", mon);
 
             if (pageInfo.getTabInfo(IPreferencesService.DEFAULT_LEVEL).getIsUsed()) {
                 prefFactory.generateDefaultTab(
             		pageInfo,
             		pluginPkgName, pluginClassName, constantsClassName, initializerClassName,
             		sourceProject, projectSourceLoc, fPagePackageName, javaPageName + "DefaultTab",  mon);
             }
             if (pageInfo.getTabInfo(IPreferencesService.CONFIGURATION_LEVEL).getIsUsed()) {
                 prefFactory.generateConfigurationTab(
             		pageInfo,
             		pluginPkgName, pluginClassName, constantsClassName,
             		sourceProject, projectSourceLoc, fPagePackageName, javaPageName + "ConfigurationTab",  mon);
             }
             if (pageInfo.getTabInfo(IPreferencesService.INSTANCE_LEVEL).getIsUsed()) {
                 prefFactory.generateInstanceTab(
             		pageInfo,
             		pluginPkgName, pluginClassName, constantsClassName,
             		sourceProject, projectSourceLoc, fPagePackageName, javaPageName + "InstanceTab",  mon);
             }
             if (pageInfo.getTabInfo(IPreferencesService.PROJECT_LEVEL).getIsUsed()) {
                 prefFactory.generateProjectTab(
             		pageInfo,
             		pluginPkgName, pluginClassName, constantsClassName,
             		sourceProject, projectSourceLoc, fPagePackageName, javaPageName + "ProjectTab",  mon);
             }
         }
 	}
     
 
 
     /**
      * Get the name of the package in which a plugin class is defined
      * for this project, or a default value if there is no such package
      * or if the project is null.  If no default name is provided, then
      * the name of the language is used for a default.
      * 
      * The intention here is to return a the name of the plugin package,
      * if the package exists, or a name that could be used as the name
      * of the plugin package, if the package does not exist.  So this
      * method should not return null and should not be used as a test
      * of whether a given project contains a plugin package or class.
      * 
      * 
      * 
      * SMS 23 Mar 2007
      * 
      * @param project		The project for which the plugin package name is sought;
      * 						may be null
      * @param defaultName	A name to return if the given package lacks a plugin class;
      * 						may be null
      * @return				The name of the package that contains the project's plugin
      * 						class, if there is one, or a name that could be used for the
      * 						plugin package, if there is none.
      */
     public String getPluginPackageName(IProject project, String defaultName)
     {
     	String result = defaultName;
     	if (result == null)
     		result = fLanguageName;
        	if (project != null) {
             String activator = null;
            IPluginModel pm = ExtensionEnabler.getPluginModelForProject(project);
             if (pm != null) {
             	WorkspaceBundleModel wbm = new WorkspaceBundleModel(project.getFile("META-INF/MANIFEST.MF")); //$NON-NLS-1$
             	activator = wbm.getBundle().getHeader("Bundle-Activator");
             }
 
             if (activator != null && !activator.equals("")) {
             	if (activator.lastIndexOf(".") >= 0)
             		result = activator.substring(0, activator.lastIndexOf("."));
             }
     	}
        	return result;
     }
     
     /**
      * Get the name of the plugin class for this project, or a default
      * name if there is no plugin class or if the given project is null.
      * If no default name is provided, then a name based on the name of
      * the language is used for a default.
      * 
      * The intention here is to return a the name of the plugin class,
      * if it exists, or a name that could be used as the name of the
      * plugin class, if it does not exist.  So this method should not
      * return null and should not be used as a test of whether a given
      * project contains a plugin class.
      * 
      * SMS 27 Mar 2007
      * 
      * @param project		The project for which the plugin class name is sought;
      * 						may be null
      * @param defaultName	A name to return if the given package lacks a plugin class;
      * 						may be null
      * @return				The name of the project's plugin class, if there is one,
      * 						or a name that could be used for the plugin class, if there
      * 						is none.
      */
     public String getPluginClassName(IProject project, String defaultName)
     {
     	String result = defaultName;
     	if (result == null)
     		result = fPageClassNameBase + "Plugin";
        	if (project != null) {
             String activator = null;
            IPluginModel pm = ExtensionEnabler.getPluginModelForProject(project);
             if (pm != null) {
             	WorkspaceBundleModel wbm = new WorkspaceBundleModel(project.getFile("META-INF/MANIFEST.MF")); //$NON-NLS-1$	
             	activator = wbm.getBundle().getHeader("Bundle-Activator");
             }
 
             if (activator != null) {
             	result = activator.substring(activator.lastIndexOf(".")+1);
             }
     	}
        	return result;
     }
     
     /**
      * Get the plugin id defined for this project, or a default value if
      * there is no plugin id or if the given project is null.   If no default
      * id is provided, then an id based on the name of the project is used
      * for a default.
      * 
      * The intention here is to return a plugin id, if it exists, or a
      * value that could be used as the id of the plugin, if it does not
      * exist.  So this method should not return null and should not be
      * used as a test of whether a given project has a plugin id.
      * 
      * SMS 27 Mar 2007
      * 
      * @param project		The project for which the plugin id name is sought;
      * 						may be null
      * @param defaultID		A value to return if the given package lacks a plugin id;
      * 						may be null
      * @return				The plugin id of the project, if there is one, or a value
      * 						that could be used as the plugin id, if there is none.
      */
     public String getPluginID(IProject project, String defaultID)
     {
     	String result = defaultID;
     	if (result == null)
     		getPluginPackageName(project, null);
        	if (project != null) {
            result = ExtensionEnabler.getPluginIDForProject(project);
     	}
        	return result;
     }
     
     
     // SMS 23 Mar 2007
     // This version takes an IProject and provides mappings
     // related to the project's plugin aspect
     public Map<String,String> getStandardSubstitutions(IProject project)
     {
         Map<String,String> result = new HashMap<String,String>();
         //result = ExtensionPointUtils.getASTInformation((IPluginModel)pages[0].getPluginModel(), project);
         result.put("$LANG_NAME$", fLanguageName);
         result.put("$CLASS_NAME_PREFIX$", fPageClassNameBase);
         result.put("$PACKAGE_NAME$", fPagePackageName);
         // SMS 22 Mar 2007
         result.put("$PROJECT_NAME$", fProjectName);
         // SMS 23 Mar 2007
         // Not the greatest solution, but if we don't have the
         // project then we may as well assume that $PLUGIN_PACKAGE$
         // has a default value
         result.put("$PLUGIN_PACKAGE$", getPluginPackageName(null, null));
         // SMS 27 Mar 2007:  ditto
         result.put("$PLUGIN_CLASS$", getPluginClassName(null, null));
         result.put("$PLUGIN_ID$", getPluginID(null, null));
     	
     	result.remove("$PLUGIN_PACKAGE$");
         result.put("$PLUGIN_PACKAGE$", getPluginPackageName(project, null));
         // SMS 27 Mar 2007
     	result.remove("$PLUGIN_CLASS$");
         result.put("$PLUGIN_CLASS$", getPluginClassName(project, null));
         result.remove("$PLUGIN_ID$");
         result.put("$PLUGIN_ID$", getPluginID(project, null));
         return result;
     }
 
     
     public IPath getProjectSourceLocationPath() {
     	return new Path(ExtensionPointWizard.getProjectSourceLocation(fProject));
     }
     
 	protected List<String> getPluginDependencies() {
 	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
 		    "org.eclipse.imp.runtime" });
 	}
 
     
    private void logError(Exception e) {
         final Status status= new Status(IStatus.ERROR, PrefspecsPlugin.kPluginID, 0, e.getMessage(), e);
         PrefspecsPlugin.getInstance().getLog().log(status);
     }
 }	
