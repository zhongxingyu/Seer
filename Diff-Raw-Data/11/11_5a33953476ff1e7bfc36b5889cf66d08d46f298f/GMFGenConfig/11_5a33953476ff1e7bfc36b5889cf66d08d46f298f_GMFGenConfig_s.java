 /*
  * Copyright (c) 2006 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Michael Golubev (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.codegen;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
 import org.eclipse.gmf.codegen.gmfgen.GenChildContainer;
 import org.eclipse.gmf.codegen.gmfgen.GenNode;
 import org.eclipse.gmf.internal.common.reconcile.Copier;
 import org.eclipse.gmf.internal.common.reconcile.DecisionMaker;
 import org.eclipse.gmf.internal.common.reconcile.DefaultDecisionMaker;
 import org.eclipse.gmf.internal.common.reconcile.Matcher;
 import org.eclipse.gmf.internal.common.reconcile.ReconcilerConfigBase;
 import org.eclipse.gmf.internal.common.reconcile.ReflectiveMatcher;
 import org.eclipse.gmf.internal.common.reconcile.StringPatternDecisionMaker;
 
 /**
  * XXX Perhaps, org.eclipse.gmf.codegen/oeg.internal.util would be better place for this class. 
  */
 public class GMFGenConfig extends ReconcilerConfigBase {
 	private final GMFGenPackage GMFGEN = GMFGenPackage.eINSTANCE;
 	
 	public GMFGenConfig(){
 		setMatcher(GMFGEN.getGenEditorGenerator(), ALWAYS_MATCH);
 		preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_CopyrightText());
 		preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_PackageNamePrefix());
 		preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_DomainFileExtension());
 		preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_DiagramFileExtension());
 		preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_SameFileForDiagramAndModel());
 		preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_ModelID());
 		preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_DynamicTemplates());
 		preserveIfSet(GMFGEN.getGenEditorGenerator(), GMFGEN.getGenEditorGenerator_TemplateDirectory());
 		
 		setMatcher(GMFGEN.getGenPlugin(), ALWAYS_MATCH); //exactly one feature for ALWAYS_MATCH GenEditorGenerator
 		preserveIfSet(GMFGEN.getGenPlugin(), GMFGEN.getGenPlugin_ID());
 		preserveIfSet(GMFGEN.getGenPlugin(), GMFGEN.getGenPlugin_Name());
 		preserveIfSet(GMFGEN.getGenPlugin(), GMFGEN.getGenPlugin_Provider());
 		preserveIfSet(GMFGEN.getGenPlugin(), GMFGEN.getGenPlugin_Version());
 		preserveIfSet(GMFGEN.getGenPlugin(), GMFGEN.getGenPlugin_ActivatorClassName());
 		preserveIfSet(GMFGEN.getGenPlugin(), GMFGEN.getGenPlugin_PrintingEnabled());
 		
 		setMatcher(GMFGEN.getGenEditorView(), ALWAYS_MATCH); //exactly one 
 		preserveIfSet(GMFGEN.getGenEditorView(), GMFGEN.getGenEditorView_IconPath());
 		preserveIfSet(GMFGEN.getGenEditorView(), GMFGEN.getGenEditorView_ClassName());
 		preserveIfSet(GMFGEN.getGenEditorView(), GMFGEN.getGenEditorView_ID());
 		
 		setMatcher(GMFGEN.getGenDiagram(), ALWAYS_MATCH);  
 		preserveIfSet(GMFGEN.getGenDiagram(), GMFGEN.getShortcuts_ContainsShortcutsTo());
 		preserveIfSet(GMFGEN.getGenDiagram(), GMFGEN.getShortcuts_ShortcutsProvidedFor());
 		preserveIfSet(GMFGEN.getGenDiagram(), GMFGEN.getEditorCandies_CreationWizardIconPath());
 		preserveIfSet(GMFGEN.getGenDiagram(), GMFGEN.getEditorCandies_CreationWizardCategoryID());
 		preserveIfSet(GMFGEN.getGenDiagram(), GMFGEN.getGenDiagram_Synchronized());
 		preserveIfSet(GMFGEN.getGenDiagram(), GMFGEN.getBatchValidation_ValidationEnabled());
 		preserveIfSet(GMFGEN.getGenDiagram(), GMFGEN.getBatchValidation_ValidationDecorators());
 		preserveIfSet(GMFGEN.getGenDiagram(), GMFGEN.getBatchValidation_ValidationDecoratorProviderClassName());		
 		preserveIfSet(GMFGEN.getGenDiagram(), GMFGEN.getBatchValidation_ValidationDecoratorProviderPriority());		
 		preserveIfSet(GMFGEN.getGenDiagram(), GMFGEN.getBatchValidation_ValidationProviderClassName());		
 		preserveIfSet(GMFGEN.getGenDiagram(), GMFGEN.getBatchValidation_ValidationProviderPriority());
 		preserveIfSet(GMFGEN.getGenDiagram(), GMFGEN.getBatchValidation_MetricProviderPriority());		
 		preserveIfSet(GMFGEN.getGenDiagram(), GMFGEN.getBatchValidation_MetricProviderClassName());		
 		
 		setMatcher(GMFGEN.getGenTopLevelNode(), getGenNodeMatcher());
 		preserveIfNotByPattern(GMFGEN.getGenTopLevelNode(), GMFGEN.getGenChildContainer_CanonicalEditPolicyClassName(), ".*" + GenChildContainer.CANONICAL_EDIT_POLICY_SUFFIX);
 		preserveIfNotByPattern(GMFGEN.getGenTopLevelNode(), GMFGEN.getGenNode_GraphicalNodeEditPolicyClassName(), ".*" + GenNode.GRAPHICAL_NODE_EDIT_POLICY_SUFFIX);
 		
 		setMatcher(GMFGEN.getGenChildNode(), getGenNodeMatcher());
 		preserveIfNotByPattern(GMFGEN.getGenChildNode(), GMFGEN.getGenChildContainer_CanonicalEditPolicyClassName(), ".*" + GenChildContainer.CANONICAL_EDIT_POLICY_SUFFIX);
 		preserveIfNotByPattern(GMFGEN.getGenChildNode(), GMFGEN.getGenNode_GraphicalNodeEditPolicyClassName(), ".*" + GenNode.GRAPHICAL_NODE_EDIT_POLICY_SUFFIX);
 		
 		setMatcher(GMFGEN.getGenCompartment(), new ReflectiveMatcher(GMFGEN.getGenCompartment_Title()));
 		preserveIfSet(GMFGEN.getGenCompartment(), GMFGEN.getGenCompartment_ListLayout());
 		preserveIfSet(GMFGEN.getGenCompartment(), GMFGEN.getGenCompartment_CanCollapse());
 		preserveIfSet(GMFGEN.getGenCompartment(), GMFGEN.getGenCompartment_HideIfEmpty());
 		preserveIfSet(GMFGEN.getGenCompartment(), GMFGEN.getGenCompartment_NeedsTitle());
 		
 		//if parent node is matched, then viemap is matched automatically because it is [1] feature.
 		//there are nothing to reconcile for viewmaps, all their properties are derived
 		//we need this only to dig into viewmap attributes
 		setMatcherForAllSubclasses(GMFGEN.getViewmap(), ALWAYS_MATCH);
 		
 		setMatcher(GMFGEN.getDefaultSizeAttributes(), ALWAYS_MATCH);
 		setCopier(GMFGEN.getDefaultSizeAttributes(), Copier.COMPLETE_COPY);
 		preserveIfSet(GMFGEN.getDefaultSizeAttributes(), GMFGEN.getDefaultSizeAttributes_Height());
 		preserveIfSet(GMFGEN.getDefaultSizeAttributes(), GMFGEN.getDefaultSizeAttributes_Width());
 
 		addDecisionMaker(GMFGEN.getGenDiagram(), new DecisionMaker.ALWAYS_OLD(GMFGEN.getGenCommonBase_CustomBehaviour()));
 		addDecisionMaker(GMFGEN.getGenTopLevelNode(), new DecisionMaker.ALWAYS_OLD(GMFGEN.getGenCommonBase_CustomBehaviour()));
 		addDecisionMaker(GMFGEN.getGenChildNode(), new DecisionMaker.ALWAYS_OLD(GMFGEN.getGenCommonBase_CustomBehaviour()));
 		addDecisionMaker(GMFGEN.getGenChildLabelNode(), new DecisionMaker.ALWAYS_OLD(GMFGEN.getGenCommonBase_CustomBehaviour()));
 		addDecisionMaker(GMFGEN.getGenCompartment(), new DecisionMaker.ALWAYS_OLD(GMFGEN.getGenCommonBase_CustomBehaviour()));
 		addDecisionMaker(GMFGEN.getGenLink(), new DecisionMaker.ALWAYS_OLD(GMFGEN.getGenCommonBase_CustomBehaviour()));
 		addDecisionMaker(GMFGEN.getGenLinkLabel(), new DecisionMaker.ALWAYS_OLD(GMFGEN.getGenCommonBase_CustomBehaviour()));
 		addDecisionMaker(GMFGEN.getGenNodeLabel(), new DecisionMaker.ALWAYS_OLD(GMFGEN.getGenCommonBase_CustomBehaviour()));
 		addDecisionMaker(GMFGEN.getGenExternalNodeLabel(), new DecisionMaker.ALWAYS_OLD(GMFGEN.getGenCommonBase_CustomBehaviour()));
 	}
 
 	private Matcher getGenNodeMatcher(){
 		//FIXME: use new AttributeMatcher("domainMetaClass")
 		return new ReflectiveMatcher(GMFGenPackage.eINSTANCE.getGenNode(), new ReflectiveMatcher.Reflector(){
 			public Object reflect(EObject target) {
 				GenNode genNode = (GenNode)target;
 				return genNode.getDomainMetaClass();
 			}
 		});
 	}
 	
 	private void preserveIfSet(EClass eClass, EAttribute feature){
 		//FIXME: only attributes for now, allow references
 		addDecisionMaker(eClass, new DefaultDecisionMaker(feature));
 	}
 	
 	private void preserveIfNotByPattern(EClass eClass, EAttribute feature, String pattern){
 		addDecisionMaker(eClass, new StringPatternDecisionMaker(pattern, feature));
 	}
 	
 }
