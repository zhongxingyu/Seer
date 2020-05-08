 /**
  * <copyright>
  *
  * Copyright (c) 2010,2011 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * $Id: CS2PivotConversion.java,v 1.23 2011/05/23 05:51:18 ewillink Exp $
  */
 package org.eclipse.ocl.examples.xtext.base.cs2pivot;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EStructuralFeature.Setting;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.ocl.examples.common.utils.TracingOption;
 import org.eclipse.ocl.examples.pivot.Annotation;
 import org.eclipse.ocl.examples.pivot.AnyType;
 import org.eclipse.ocl.examples.pivot.ClassifierType;
 import org.eclipse.ocl.examples.pivot.CollectionType;
 import org.eclipse.ocl.examples.pivot.Comment;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.InvalidLiteralExp;
 import org.eclipse.ocl.examples.pivot.InvalidType;
 import org.eclipse.ocl.examples.pivot.Iteration;
 import org.eclipse.ocl.examples.pivot.LoopExp;
 import org.eclipse.ocl.examples.pivot.MultiplicityElement;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.Namespace;
 import org.eclipse.ocl.examples.pivot.OclExpression;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.OperationCallExp;
 import org.eclipse.ocl.examples.pivot.ParameterableElement;
 import org.eclipse.ocl.examples.pivot.PivotConstants;
 import org.eclipse.ocl.examples.pivot.PivotFactory;
 import org.eclipse.ocl.examples.pivot.PivotPackage;
 import org.eclipse.ocl.examples.pivot.PrimitiveType;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.TemplateBinding;
 import org.eclipse.ocl.examples.pivot.TemplateParameter;
 import org.eclipse.ocl.examples.pivot.TemplateParameterSubstitution;
 import org.eclipse.ocl.examples.pivot.TemplateSignature;
 import org.eclipse.ocl.examples.pivot.TemplateableElement;
 import org.eclipse.ocl.examples.pivot.TupleType;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.TypedElement;
 import org.eclipse.ocl.examples.pivot.TypedMultiplicityElement;
 import org.eclipse.ocl.examples.pivot.UnspecifiedType;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.util.MorePivotable;
 import org.eclipse.ocl.examples.pivot.util.Pivotable;
 import org.eclipse.ocl.examples.pivot.utilities.AbstractConversion;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.ocl.examples.xtext.base.baseCST.AnnotationElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.BaseCSTPackage;
 import org.eclipse.ocl.examples.xtext.base.baseCST.ElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.ElementRefCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.ModelElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.NamedElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.PackageCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.PivotableElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.TemplateBindingCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.TemplateParameterCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.TemplateParameterSubstitutionCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.TemplateSignatureCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.TemplateableElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.TypeRefCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.TypedElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.TypedTypeRefCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.WildcardTypeRefCS;
 import org.eclipse.ocl.examples.xtext.base.cs2pivot.BasePreOrderVisitor.OperationContinuation;
 import org.eclipse.ocl.examples.xtext.base.cs2pivot.BasePreOrderVisitor.PackageContentContinuation;
 import org.eclipse.ocl.examples.xtext.base.cs2pivot.BasePreOrderVisitor.TemplateSignatureContinuation;
 import org.eclipse.ocl.examples.xtext.base.cs2pivot.CS2Pivot.Factory;
 import org.eclipse.ocl.examples.xtext.base.util.BaseCSVisitor;
 import org.eclipse.ocl.examples.xtext.base.util.VisitableCS;
 import org.eclipse.ocl.examples.xtext.base.utilities.CS2Moniker;
 import org.eclipse.ocl.examples.xtext.base.utilities.ElementUtil;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.xtext.diagnostics.IDiagnosticConsumer;
 import org.eclipse.xtext.nodemodel.ICompositeNode;
 import org.eclipse.xtext.nodemodel.ILeafNode;
 import org.eclipse.xtext.nodemodel.INode;
 import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
 
 public class CS2PivotConversion extends AbstractConversion
 {	
 	private static final Logger logger = Logger.getLogger(CS2PivotConversion.class);
 	public static final TracingOption CONTINUATION = new TracingOption("org.eclipse.ocl.examples.xtext.base", "continuation");  //$NON-NLS-1$//$NON-NLS-2$
 
 	public static class CacheKey<T>
 	{
 		protected final String name;
 		
 		public CacheKey(String name) {
 			this.name = name;
 		}
 		
 		@Override
 		public String toString() { return name; }
 	}
 	
 	protected final CS2Pivot converter;
 	protected final MetaModelManager metaModelManager;
 	protected final Collection<? extends Resource> csResources;
 	
 	/**
 	 * The per-package Visitors
 	 */
 	private final Map<EPackage, BaseCSVisitor<Element, CS2PivotConversion>> left2RightVisitorMap = new HashMap<EPackage, BaseCSVisitor<Element, CS2PivotConversion>>();
 	private final Map<EPackage, BaseCSVisitor<Continuation<?>, CS2PivotConversion>> postOrderVisitorMap = new HashMap<EPackage, BaseCSVisitor<Continuation<?>, CS2PivotConversion>>();
 	private final Map<EPackage, BaseCSVisitor<Continuation<?>, CS2PivotConversion>> preOrderVisitorMap = new HashMap<EPackage, BaseCSVisitor<Continuation<?>, CS2PivotConversion>>();
 
 	private InterDependency<PackageContentContinuation> packagesHaveTypes = new InterDependency<PackageContentContinuation>("All unspecialized types defined", null);
 	private InterDependency<TemplateSignatureContinuation> typesHaveSignatures = new InterDependency<TemplateSignatureContinuation>("All unspecialized signatures defined", packagesHaveTypes);
 	private InterDependency<OperationContinuation<?>> operationsHaveTemplateParameters = new InterDependency<OperationContinuation<?>>("All operation template parameters defined", typesHaveSignatures);
 
 	/**
 	 * A typed cache for use by derived conversions.
 	 */
 	private final Map<CacheKey<?>, Object> intermediateCache = new HashMap<CacheKey<?>, Object>();
 
 	private Map<String, org.eclipse.ocl.examples.pivot.Package> oldPackagesByName = null;
 	private Map<String, org.eclipse.ocl.examples.pivot.Package> oldPackagesByQualifiedName = null;	// WIP lose this since using nsURIs
 
 	/**
 	 * Set of all expression nodes whose type involves an UnspecifiedType. These are
 	 * created during the left2right pass and are finally resolved to
 	 * minimize invalidity.
 	 */
 	private HashSet<TypedElement> underspecifiedTypedElements = null;
 
 	/**
 	 * The handler for any generated diagnostics. If null (which is deprecated) diagnostics are inserted
 	 * directly into the resource errors list.
 	 */
 	@SuppressWarnings("unused")
 	private final IDiagnosticConsumer diagnosticsConsumer;
 	
 	public CS2PivotConversion(CS2Pivot converter, IDiagnosticConsumer diagnosticsConsumer, Collection<? extends Resource> csResources) {
 		this.converter = converter;
 		this.diagnosticsConsumer = diagnosticsConsumer;
 		this.metaModelManager = converter.getMetaModelManager();
 		this.csResources = csResources;
 		List<Resource> mappedResources = new ArrayList<Resource>();
 		for (Resource csResource : csResources) {
 			mappedResources.add(converter.getPivotResource(csResource));
 		}
 	}
 
 	public OclExpression addBadExpressionError(ModelElementCS csElement, String boundMessage) {
 		INode node = NodeModelUtils.getNode(csElement);
 		Resource.Diagnostic resourceDiagnostic = new ValidationDiagnostic(node, boundMessage);
 		csElement.eResource().getErrors().add(resourceDiagnostic);
 		InvalidLiteralExp invalidLiteralExp = metaModelManager.createInvalidExpression();
 		csElement.setPivot(invalidLiteralExp);
 		return invalidLiteralExp;
 	}
 
 	public InvalidType addBadTypeError(ModelElementCS csElement, String message, Object... bindings) {
 		InvalidType invalidType = metaModelManager.getOclInvalidType();
 		csElement.setPivot(invalidType);
 		return invalidType;
 	}
 
 	public InvalidType addBadTypeError(ElementRefCS csElement, String message, Object... bindings) {
 		InvalidType invalidType = metaModelManager.getOclInvalidType();
 		csElement.setPivot(invalidType);
 		return invalidType;
 	}
 
 	public void addDiagnostic(ModelElementCS csElement, Diagnostic diagnostic) {
 		INode node = NodeModelUtils.getNode(csElement);
 		Resource.Diagnostic resourceDiagnostic = new ValidationDiagnostic(node, diagnostic.getMessage());
 		csElement.eResource().getErrors().add(resourceDiagnostic);
 	}
 
 	public void addDiagnostic(ElementCS csElement, String boundMessage) {
 		INode node = NodeModelUtils.getNode(csElement);
 		Resource.Diagnostic resourceDiagnostic = new ValidationDiagnostic(node, boundMessage);
 		csElement.eResource().getErrors().add(resourceDiagnostic);
 	}
 
 	protected void addUnderspecifiedTypedElement(TypedElement pivotElement) {
 		if (underspecifiedTypedElements == null) {
 			underspecifiedTypedElements  = new HashSet<TypedElement>();
 		}
 		underspecifiedTypedElements.add(pivotElement);
 	}
 
 //	public void addWarning(ModelElementCS csElement, String message) {
 //		csElement.getError().add(message);
 //	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ocl.examples.xtext.base.cs2pivot.DiagnosticHandler#addWarning(org.eclipse.ocl.examples.xtext.base.baseCST.ModelElementCS, java.lang.String, java.lang.Object)
 	 */
 	public void addWarning(ModelElementCS csElement, String message, Object... bindings) {
 		String boundMessage = NLS.bind(message, bindings);
 		INode node = NodeModelUtils.getNode(csElement);
 		Resource.Diagnostic resourceDiagnostic = new ValidationDiagnostic(node, boundMessage);
 		csElement.eResource().getErrors().add(resourceDiagnostic);
 	}
 
 	public String bind(EObject csContext, String messageTemplate, Object... bindings) {
 		return converter.bind(csContext, messageTemplate, bindings);
 	}
 
 	public boolean checkForNoErrors(Collection<? extends Resource> csResources) {
 		for (Resource csResource : csResources) {
 			if (ElementUtil.hasSyntaxError(csResource.getErrors())) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public Dependency createTypeIsReferenceableDependency(TypeRefCS csTemplateParameter) {
 		if ((csTemplateParameter == null) || (csTemplateParameter instanceof WildcardTypeRefCS)) {
 			return null;
 		}
 		else {
 			return new PivotDependency(csTemplateParameter);
 		}
 	}
 
 	protected void diagnoseContinuationFailure(List<BasicContinuation<?>> continuations) {
 		if (CONTINUATION.isActive()) {
 			for (BasicContinuation<?> continuation : continuations) {
 				CONTINUATION.println(continuation.toString());
 				for (Dependency dependency : continuation.getDependencies()) {
 					boolean canExecute = dependency.canExecute();
 					CONTINUATION.println((canExecute ? "+ " : "- ") + dependency.toString());
 				}
 			}
 		}
 		StringBuilder s = new StringBuilder();
 		int i = 0;
 		for (BasicContinuation<?> continuation : continuations) {
 			s.append("\n  ");
 			s.append(continuation);
 			for (Dependency dependency : continuation.getDependencies()) {
 				s.append("\n    ");
 				if (!dependency.canExecute()) {
 					s.append("BLOCKED ");
 					dependency.canExecute();			// FIXME debugging
 				}
 				s.append(dependency);					
 			}
 			continuation.canExecute();			// FIXME debugging
 			if (++i >= 10) {
 				s.append("\n  ...");
 				break;
 			}
 		}
 		logger.error("Failed to complete continuations" + s.toString());		// FIXME
 	}
 
 /*	private void enforceConformance(AnyType oclAny) {
 		Collection<? extends Resource> pivotResources = converter.getPivotResources();
 		Collection<Notifier> allPivotResources = new ArrayList<Notifier>(pivotResources);
 		allPivotResources.add(metaModelManager.getOrphanPackage());
 		for (TreeIterator<Object> tit = EcoreUtil.getAllContents(allPivotResources); tit.hasNext(); ) {
 			Object object = tit.next();
 			if (object instanceof org.eclipse.ocl.examples.pivot.Package) {
 				for (Type type : ((org.eclipse.ocl.examples.pivot.Package)object).getOwnedTypes()) {
 					if (type instanceof org.eclipse.ocl.examples.pivot.Class) {
 						List<org.eclipse.ocl.examples.pivot.Class> superClasses = ((org.eclipse.ocl.examples.pivot.Class)type).getSuperClasses();
 						if (superClasses.isEmpty()) {
 							if (type != oclAny) {
 								if (type instanceof org.eclipse.ocl.examples.pivot.Enumeration) {
 									superClasses.add(metaModelManager.getEnumerationType());
 								}
 								else {
 									superClasses.add(oclAny);
 								}
 							}
 						}
 					}
 				}
 			}
 			else {
 				tit.prune();
 			}
 		}
 	} */
 
 /*	protected List<String> getDocumentationStrings1(CompositeNode node) {
 		List<LeafNode> documentationNodes = CS2Pivot.getDocumentationNodes(node);
 		if (documentationNodes == null) {
 			return null;
 		}
 		List<String> documentationStrings = new ArrayList<String>();
 		for (LeafNode documentationNode : documentationNodes) {
 			String text = documentationNode.getText();
 			documentationStrings.add(text.substring(3, text.length()-3).trim());
 		}
 		return documentationStrings;
 	} */
 	
 	/**
 	 * Prune the pivots to eliminate:
 	 * - redundant orphans - e.g. obsolete specializations
 	 * - redundant elements - e.g. pivots that are not needed as a result of a CS update
 	 * - dependent caches that reference any of the above
 	 *  
 	 * Wanted pivot elements are
 	 * - all elements in all libraries
 	 * - all elements locked via MetaModelManager.addLockedElement()
 	 * - all elements transitively in/referenced from the above
 	 * - all elements referenced by incoming CS resources
 	 * - all elements in all incoming pivot resources
 	 * - except pivot resources mapped to incoming CS resources
 	 *     this is what we're cleaning up
 	 */
 	public void garbageCollect(Map<? extends Resource, ? extends Resource> cs2pivotResourceMap) {
 //		org.eclipse.ocl.examples.pivot.Class orphanClass = metaModelManager.getOrphanClass();
 //		org.eclipse.ocl.examples.pivot.Package orphanPackage = metaModelManager.getOrphanPackage();
 //		Resource orphanResource = orphanPackage.eResource();
 		final Collection<Notifier> prunableResources = new ArrayList<Notifier>(cs2pivotResourceMap.values());
 //		prunableResources.add(orphanResource);
 		Collection<Notifier> allPivotResources = new ArrayList<Notifier>(metaModelManager.getPivotResourceSet().getResources());
 //		allPivotResources.removeAll(prunableResources);					// Dead elements in orphanage or pivot of CS can be pruned
 		EObject lockingObject = metaModelManager.getLockingObject();
 		if (lockingObject != null) {
 			allPivotResources.add(lockingObject);						// Locked elements are not dead
 		}
 		allPivotResources.addAll(metaModelManager.getLibraries());			// Library elements are not dead
 		allPivotResources.addAll(cs2pivotResourceMap.keySet());			// Incoming elements are not dead
 		@SuppressWarnings("serial")
 		Map<EObject, Collection<Setting>> referencesToOrphans = new EcoreUtil.CrossReferencer(allPivotResources)
 		{
 			{ crossReference(); }
 			@Override
 			protected boolean crossReference(EObject eObject, EReference eReference, EObject crossReferencedEObject) {
 				Resource eResource = crossReferencedEObject.eResource();
 				boolean isPrunable = prunableResources.contains(eResource);
 //				if (isPrunable && prunableResources.contains(eObject.eResource())) {
 //					PivotUtil.debugObjectUsage("prunable xref ", crossReferencedEObject);
 //					PivotUtil.debugObjectUsage(" from " + eReference.getName() + " ", eObject);
 //				}
 //				else {
 //					PivotUtil.debugObjectUsage("unprunable xref ", crossReferencedEObject);
 //					PivotUtil.debugObjectUsage(" from " + eReference.getName() + " ", eObject);
 //				}
 				return isPrunable;
 			}
 
 			@Override
 			protected void handleCrossReference(EObject eObject) {
 				try {
 					super.handleCrossReference(eObject);
 				    InternalEObject internalEObject = (InternalEObject)eObject;
 					for (EObject eContent : eObject.eContents()) {
 						EReference eReference = (EReference) eContent.eContainingFeature();
 				        add(internalEObject, eReference, eContent);
 					}
 				}
 				catch (Exception e) {}
 			}
 			
 			@Override
 			protected boolean resolve() {
 				return false;		// Don't start creating specializations to resolve proxies
 			}
 		};
 		Set<EObject> wantedOrphans = new HashSet<EObject>();
 		List<Map.Entry<EObject, Collection<EStructuralFeature.Setting>>> suspects = new ArrayList<Map.Entry<EObject, Collection<EStructuralFeature.Setting>>>();
 		for (Map.Entry<EObject, Collection<EStructuralFeature.Setting>> entry : referencesToOrphans.entrySet()) {
 			EObject referencedOrphan = entry.getKey();
 			Collection<EStructuralFeature.Setting> referencesToOrphan = entry.getValue();
 			boolean wantIt = false;
 			for (EStructuralFeature.Setting setting : referencesToOrphan) {
 				EObject eObject = setting.getEObject();
 				Resource eResource = eObject.eResource();
 				if (!prunableResources.contains(eResource)) {
 //					PivotUtil.debugObjectUsage("externally refd ", referencedOrphan);
 					wantedOrphans.add(referencedOrphan);
 					wantIt = true;
 					break;
 				}
 			}
 			if (!wantIt) {
 //				if ((referencedOrphan != orphanPackage) && (referencedOrphan != orphanClass)) {
 //					PivotUtil.debugObjectUsage("externally unrefd ", referencedOrphan);
 					suspects.add(entry);
 //				}
 //				else {
 //					PivotUtil.debugObjectUsage("unkillable ", referencedOrphan);
 //				}
 			}
 		}
 		while (!suspects.isEmpty()) {
 			List<Map.Entry<EObject, Collection<EStructuralFeature.Setting>>> oldSuspects = suspects;
 			suspects = new ArrayList<Map.Entry<EObject, Collection<EStructuralFeature.Setting>>>();
 			for (Map.Entry<EObject, Collection<EStructuralFeature.Setting>> entry : oldSuspects) {
 				EObject referencedOrphan = entry.getKey();
 				Collection<EStructuralFeature.Setting> referencesToOrphan = entry.getValue();
 				boolean wantIt = false;
 				for (EStructuralFeature.Setting setting : referencesToOrphan) {
 					EObject eObject = setting.getEObject();
 					if (wantedOrphans.contains(eObject)) {
 //						PivotUtil.debugObjectUsage(pass + " internally refd ", referencedOrphan);
 //						PivotUtil.debugObjectUsage("     by ", eObject);
 						wantedOrphans.add(referencedOrphan);
 						wantIt = true;
 						break;
 					}
 				}
 				if (!wantIt) {
 //					PivotUtil.debugObjectUsage(pass + " internally unrefd ", referencedOrphan);
 					suspects.add(entry);
 				}
 			}
 			if (oldSuspects.size() <= suspects.size()) {
 				break;
 			}
 		}
 		for (Map.Entry<EObject, Collection<EStructuralFeature.Setting>> entry : suspects) {
 			EObject referencedOrphan = entry.getKey();
 			boolean wantIt = false;
 			for (EObject eChild : referencedOrphan.eContents()) {		// FIXME this avoids a loss of the Ecore part of a Complete OCL resource
 				if (wantedOrphans.contains(eChild)) {					//  surely an earlier containment want search should have found this
 					wantIt = true;
 					break;
 				}
 			}
 			if (!wantIt) {
 //				PivotUtil.debugObjectUsage("kill ", referencedOrphan);
 				Collection<EStructuralFeature.Setting> referencesToOrphan = entry.getValue();
 				if (referencesToOrphan != null) {
 					for (EStructuralFeature.Setting setting : referencesToOrphan) {
 						EObject eObject = setting.getEObject();
 						EStructuralFeature eStructuralFeature = setting.getEStructuralFeature();
 						if (!eStructuralFeature.isDerived()) {
 //							PivotUtil.debugObjectUsage(" non-derived " + eStructuralFeature.getEContainingClass().getName() + "::" + eStructuralFeature.getName() + " : ", eObject);
 							if (eStructuralFeature.isMany()) {
 								@SuppressWarnings("unchecked")
 								Collection<Object> list = (Collection<Object>) eObject.eGet(eStructuralFeature);
 								list.remove(referencedOrphan);
 							}
 							else {
 								eObject.eSet(eStructuralFeature, null);
 							}
 						}
 						else {
 //							PivotUtil.debugObjectUsage(" derived " + eStructuralFeature.getEContainingClass().getName() + "::" + eStructuralFeature.getName() + " : ", eObject);
 //							System.out.println("  derived " + eObject.getClass().getName() + "@" + eObject.hashCode() + " " + eStructuralFeature.getName());
 						}
 					}
 				}
 				EObject eContainer = referencedOrphan.eContainer();
 				if (eContainer != null) {
 					PivotUtil.debugObjectUsage("  container ", eContainer);
 					referencedOrphan.eSet(referencedOrphan.eContainingFeature(), null);
 				}
 //WIP			metaModelManager.kill(referencedOrphan);
 			}
 		}
 //		for (MonikeredElement element : oldMoniker2PivotMap.values()) {
 //			if (element.eResource() == null) {
 //				PivotUtil.debugObjectUsage("Final Old residue ", element);
 				// This is a bit late: a notification of non-containment would be sharper.
 //				metaModelManager.kill(element);
 //			}
 //		}
 //		for (MonikeredElement element : newMoniker2PivotMap.values()) {
 //			if (element.eResource() == null) {
 //				PivotUtil.debugObjectUsage("Final New residue ", element);
 //			}
 //		}
 //		for (Type orphanType : orphanPackage.getOwnedTypes()) {
 //			if (!PivotUtil.debugWellContainedness(orphanType)) {
 //				for (Setting setting : referencesToOrphans.get(orphanType)) {
 //					PivotUtil.debugObjectUsage("Dangling reference " + setting.getEStructuralFeature().getName() + " ", setting.getEObject());				
 //				}
 //			}
 //		}
 	}
 
 	protected void gatherNewPackage(Set<org.eclipse.ocl.examples.pivot.Package> newPackages, EObject pivot) {
 		if (pivot instanceof org.eclipse.ocl.examples.pivot.Package) {
 			newPackages.add((org.eclipse.ocl.examples.pivot.Package)pivot);
 		}
 		EObject eContainer = pivot.eContainer();
 		if (eContainer != null) {
 			gatherNewPackage(newPackages, eContainer);
 		}
 	}
 
 	/**
 	 * Add any packages and nested packages pivoted by csResource to newPackages. This
 	 * is invoked at the end of an update to identify redundant packages. 
 	 */
 	protected void gatherNewPackages(Set<org.eclipse.ocl.examples.pivot.Package> newPackages, Resource csResource) {
 		for (TreeIterator<EObject> tit = csResource.getAllContents(); tit.hasNext(); ) {
 			EObject eObject = tit.next();
 			if (eObject instanceof Pivotable) {
 				Element pObject = ((Pivotable)eObject).getPivot();
 				if (pObject instanceof org.eclipse.ocl.examples.pivot.Package) {
 					gatherNewPackage(newPackages, pObject);
 				}
 				else {		// CompleteOCL has package references from non-package contexts
 					if (pObject instanceof Type) {
 						gatherNewPackage(newPackages, pObject);
 					}
 					else if (pObject instanceof Operation) {
 						gatherNewPackage(newPackages, pObject);
 					}
 					else if (pObject instanceof Property) {
 						gatherNewPackage(newPackages, pObject);
 					}
 					tit.prune();
 				}
 				if (eObject instanceof MorePivotable) {  // CompleteOCL has package references from non-package contexts
 					for (Element pivot : ((MorePivotable)eObject).getMorePivots()) {
 						gatherNewPackage(newPackages, pivot);
 					}
 				}
 			}
 		}	
 	}
 
 	/**
 	 * Add any packages and nested packages in eObjects to oldPackages. This
 	 * is invoked at the start of an update to cache the packages for re-use. 
 	 */
 	protected void gatherOldPackages(List<? extends EObject> eObjects) {
 		for (EObject eObject : eObjects) {
 			if (eObject instanceof org.eclipse.ocl.examples.pivot.Package) {
 				org.eclipse.ocl.examples.pivot.Package pkg = (org.eclipse.ocl.examples.pivot.Package) eObject;
 				String name = pkg.getName();
 				if (name == null) {
 					name = PivotConstants.NULL_ROOT;
 				}
 				String qualifiedName = getQualifiedName(new StringBuilder(), pkg);
 				org.eclipse.ocl.examples.pivot.Package oldPkg = oldPackagesByQualifiedName.put(qualifiedName, pkg);
 				if (oldPkg != null) {
 					logger.warn("Duplicate qualified package name: " + qualifiedName);
 				}
 				if (name.equals(qualifiedName)) {
 					oldPkg = oldPackagesByName.put(name, pkg);
 					if ((oldPkg != null) && name.equals(getQualifiedName(new StringBuilder(), oldPkg))) {
 						logger.warn("Duplicate unqualified package name: " + qualifiedName);
 					}
 				}
 				else {
 					oldPkg = oldPackagesByName.get(name);
 					if (oldPkg == null) {
 						oldPackagesByName.put(name, pkg);
 					}
 				}
 				gatherOldPackages(pkg.getNestedPackages());
 			}
 		}	
 	}
 	
 	public final CS2Pivot getConverter() {
 		return converter;
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> T getIntermediate(CacheKey<T> key) {
 		return (T) intermediateCache.get(key);
 	}
 
 	public BaseCSVisitor<Element, CS2PivotConversion> getLeft2RightVisitor(EPackage ePackage) {
 		BaseCSVisitor<Element, CS2PivotConversion> left2RightVisitor = left2RightVisitorMap.get(ePackage);
 		if ((left2RightVisitor == null) && !left2RightVisitorMap.containsKey(ePackage)) {
 			Factory factory = converter.getFactory(ePackage);
 			if (factory != null) {
 				left2RightVisitor = factory.createLeft2RightVisitor(this);
 				if (left2RightVisitor == null) {
 					logger.error("No Left2Right Visitor created for " + ePackage.getName());
 				}
 			}
 			else {
 				logger.error("No Left2Right Visitor Factory registered for " + ePackage.getName());
 			}
 			left2RightVisitorMap.put(ePackage, left2RightVisitor);
 		}
 		return left2RightVisitor;
 	}
 
 	public final MetaModelManager getMetaModelManager() {
 		return metaModelManager;
 	}
 
 	public InterDependency<OperationContinuation<?>> getOperationsHaveTemplateParametersInterDependency() {
 		return operationsHaveTemplateParameters;
 	}
 
 	public InterDependency<PackageContentContinuation> getPackagesHaveTypesInterDependency() {
 		return packagesHaveTypes;
 	}
 
 	public BaseCSVisitor<Continuation<?>, CS2PivotConversion> getPostOrderVisitor(EPackage ePackage) {
 		BaseCSVisitor<Continuation<?>, CS2PivotConversion> postOrderVisitor = postOrderVisitorMap.get(ePackage);
 		if ((postOrderVisitor == null) && !postOrderVisitorMap.containsKey(ePackage)) {
 			Factory factory = converter.getFactory(ePackage);
 			if (factory != null) {
 				postOrderVisitor = factory.createPostOrderVisitor(this);
 				if (postOrderVisitor == null) {
 					logger.error("No PostOrder Visitor created for " + ePackage.getName());
 				}
 			}
 			else {
 				logger.error("No PostOrder Visitor Factory registered for " + ePackage.getName());
 			}
 			postOrderVisitorMap.put(ePackage, postOrderVisitor);
 		}
 		return postOrderVisitor;
 	}
 
 	public BaseCSVisitor<Continuation<?>, CS2PivotConversion> getPreOrderVisitor(EPackage ePackage) {
 		BaseCSVisitor<Continuation<?>, CS2PivotConversion> preOrderVisitor = preOrderVisitorMap.get(ePackage);
 		if ((preOrderVisitor == null) && !preOrderVisitorMap.containsKey(ePackage)) {
 			Factory factory = converter.getFactory(ePackage);
 			if (factory != null) {
 				preOrderVisitor = factory.createPreOrderVisitor(this);
 				if (preOrderVisitor == null) {
 					logger.error("No PreOrder Visitor created for " + ePackage.getName());
 				}
 			}
 			else {
 				logger.error("No PreOrder Visitor Factory registered for " + ePackage.getName());
 			}
 			preOrderVisitorMap.put(ePackage, preOrderVisitor);
 		}
 		return preOrderVisitor;
 	}
 
 	protected String getQualifiedName(StringBuilder s, org.eclipse.ocl.examples.pivot.Package pkg) {
 		org.eclipse.ocl.examples.pivot.Package nestingPackage = pkg.getNestingPackage();
 		if (nestingPackage != null) {
 			getQualifiedName(s, nestingPackage);
 			s.append("$$");
 		}
 		String name = pkg.getName();
 		if (name == null) {
 			name = PivotConstants.NULL_ROOT;
 		}
 		s.append(name);
 		return s.toString();
 	}
 	
 	protected String getQualifiedName(StringBuilder s, PackageCS csPackage) {
 		EObject eContainer = csPackage.eContainer();
 		if (eContainer instanceof PackageCS) {
 			getQualifiedName(s, (PackageCS) eContainer);
 			s.append("$$");
 		}
 		String name = csPackage.getName();
 		if (name == null) {
 			name = PivotConstants.NULL_ROOT;
 			if (eContainer == null) {
 				Resource csResource = csPackage.eResource();
 				if (csResource != null) {
 					URI csURI = csResource.getURI();
 					if (csURI != null) {
 						name = csURI.lastSegment();
 					}
 				}
 			}
 		}
 		s.append(name);
 		return s.toString();
 	}
 
 	protected List<TemplateBindingCS> getTemplateBindings(ElementCS csElement) {
 		List<TemplateBindingCS> csTemplateBindings;
 //		EObject container = csElement.eContainer();
 //		if (container instanceof ElementCS) {			
 //			csTemplateBindings = getTemplateBindings((ElementCS) container);
 //		}
 //		else {
 			csTemplateBindings = new ArrayList<TemplateBindingCS>();
 //		}
 		if (csElement instanceof TypedTypeRefCS) {
 			TypedTypeRefCS csTemplateableElement = (TypedTypeRefCS)csElement;
 			TemplateBindingCS csTemplateBinding = csTemplateableElement.getOwnedTemplateBinding();
 			if (csTemplateBinding != null) {
 				csTemplateBindings.add(csTemplateBinding);
 			}
 		}
 		return csTemplateBindings;
 	}
 	
 	protected List<TemplateSignature> getTemplateSignatures(Element pivotElement) {
 		List<TemplateSignature> pivotTemplateSignatures;
 		EObject container = pivotElement.eContainer();
 		if (container instanceof Element) {			
 			pivotTemplateSignatures = getTemplateSignatures((Element) container);
 		}
 		else {
 			pivotTemplateSignatures = new ArrayList<TemplateSignature>();
 		}
 		if (pivotElement instanceof TemplateableElement) {
 			TemplateableElement templateableElement = (TemplateableElement)pivotElement;
 			TemplateSignature templateSignature = templateableElement.getOwnedTemplateSignature();
 			if (templateSignature != null) {
 				pivotTemplateSignatures.add(templateSignature);
 			}
 		}
 		return pivotTemplateSignatures;
 	}
 
 	public InterDependency<TemplateSignatureContinuation> getTypesHaveSignaturesInterDependency() {
 		return typesHaveSignatures;
 	}
 
 	public void handleVisitNamedElement(NamedElementCS csNamedElement, NamedElement pivotElement) {
 		List<Annotation> pivotAnnotations = pivotElement.getOwnedAnnotations();
 		List<AnnotationElementCS> csAnnotations = csNamedElement.getOwnedAnnotation();
 //		if ((csAnnotations.size() <= 1) && (pivotAnnotations.size() <= 1)) {
 			refreshPivotList(Annotation.class, pivotAnnotations, csAnnotations);
 /*		}
 		else {
 			HashSet<String> names = new HashSet<String>();
 			HashMap<String, List<Annotation>> pivotMap = new HashMap<String, List<Annotation>>();
 			HashMap<String, List<AnnotationElementCS>> csMap = new HashMap<String, List<AnnotationElementCS>>();
 			for (Annotation pivotAnnotation : pivotAnnotations) {
 				String name = pivotAnnotation.getName();
 				names.add(name);
 				List<Annotation> pivotList = pivotMap.get(name);
 				if (pivotList == null) {
 					pivotList = new ArrayList<Annotation>();
 					pivotMap.put(name, pivotList);
 				}
 				pivotList.add(pivotAnnotation);
 			}
 			for (AnnotationElementCS csAnnotation : csAnnotations) {
 				String name = csAnnotation.getName();
 				names.add(name);
 				List<AnnotationElementCS> csList = csMap.get(name);
 				if (csList == null) {
 					csList = new ArrayList<AnnotationElementCS>();
 					csMap.put(name, csList);
 				}
 				csList.add(csAnnotation);
 			}
 			for (String name : names) {
 				List<Annotation> pivotList = pivotMap.get(name);
 				List<AnnotationElementCS> csList = csMap.get(name);
 //				refreshPivotList(Annotation.class, pivotAnnotations, csAnnotations);
 				List<Annotation> newPivotAnnotations = new ArrayList<Annotation>();
 				for (ModelElementCS csElement : csList) {
 					Annotation pivotAnnotation = getPivotElement(Annotation.class, csElement);
 					assert pivotAnnotation != null;
 					if (pivotElement != null) {
 						newPivotAnnotations.add(pivotAnnotation);
 					}
 				}
 				refreshList(pivotAnnotations, newPivotAnnotations);
 			}
 		} */
 	}
 
 	public void installPivotReference(ElementRefCS csElement, Element newPivotElement, EReference eReference) {
 		converter.installPivotReference(csElement, newPivotElement, eReference);
 	}
 	
 	public void installPivotUsage(ModelElementCS csElement, Element newPivotElement) {
 		converter.installPivotUsage(csElement, newPivotElement);
 	}
 
 	protected void installRootContents(Resource csResource) {
 		for (EObject eObject : csResource.getContents()) {
 			if (eObject instanceof Pivotable) {
 				Element pivotElement = ((Pivotable)eObject).getPivot();
 				if (pivotElement != null) {
 					Resource pivotResource = pivotElement.eResource();
 					if (pivotResource == null) {
 						pivotResource = converter.getPivotResource(csResource);
 						if (pivotResource != null) {
 							pivotResource.getContents().add(pivotElement);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Invoke all of the continuations that can execute, returning the list of
 	 * continuations till to perform, some of which may be ones that were
 	 * blocked by unsatisfied dependencies, others of which may be further
 	 * continuations resulting from only partial progress. Returns null if
 	 * none of the continuations could execute.
 	 * 
 	 * @param continuations
 	 * @return continuations still to perform, null if stuck.
 	 */
 	protected List<BasicContinuation<?>> progressContinuations(List<BasicContinuation<?>> continuations) {
 		List<BasicContinuation<?>> moreContinuations = new ArrayList<BasicContinuation<?>>();
 		boolean madeProgress = false;
 		boolean tracingOn = CONTINUATION.isActive();
 		if (tracingOn) {
 			CONTINUATION.println("------------------------------------------------ " + continuations.size());
 			CONTINUATION.println(packagesHaveTypes.toString());
 			CONTINUATION.println(typesHaveSignatures.toString());
 		}
 		for (BasicContinuation<?> continuation : continuations) {
 			boolean canExecute = continuation.canExecute();
 			if (tracingOn) {
 				CONTINUATION.println((canExecute ? "+ " : "- ") + continuation);
 			}
 			if (canExecute) {
 				madeProgress = true;
 				BasicContinuation<?> nextContinuation = continuation.execute();
 				if (nextContinuation != null) {
 					nextContinuation.addTo(moreContinuations);
 				}
 			}
 			else {
 				moreContinuations.add(continuation);
 			}
 		}
 		return madeProgress ? moreContinuations : null;
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> T putIntermediate(CacheKey<T> key, T object) {
 		return (T) intermediateCache.put(key, object);
 	}
 
 	public void refreshComments(Element pivotElement, ElementCS csElement) {
 		ICompositeNode node = NodeModelUtils.getNode(csElement);
 		if (node != null) {
 			List<ILeafNode> documentationNodes = CS2Pivot.getDocumentationNodes(node);
 			if (documentationNodes != null) {
 				List<String> documentationStrings = new ArrayList<String>();
 				for (ILeafNode documentationNode : documentationNodes) {
 					String text = documentationNode.getText();
 					int startIndex = text.startsWith("/**") ? 3 : 0;
 					int endIndex = text.length() - (text.endsWith("*/") ? text.endsWith("**/") ? 3 : 2 : 0);
 					if (startIndex < endIndex) {
 						documentationStrings.add(text.substring(startIndex, endIndex).trim());
 					}
 				}
 				List<Comment> ownedComments = pivotElement.getOwnedComments();
 				int iMax = Math.min(documentationStrings.size(), ownedComments.size());
 				int i = 0;
 				for (; i < iMax; i++) {
 					String string = documentationStrings.get(i);
 					String trimmedString = trimComments(string);
 					Comment comment = ownedComments.get(i);
 					if (!trimmedString.equals(comment.getBody())) {
 						comment.setBody(trimmedString);
 					}
 				}
 				for ( ; i < documentationStrings.size(); i++) {
 					String string = documentationStrings.get(i);
 					String trimmedString = trimComments(string);
 					Comment comment = PivotFactory.eINSTANCE.createComment();
 					comment.setBody(trimmedString);
 					ownedComments.add(comment);
 				}
 				while (i < ownedComments.size()) {
 					ownedComments.remove(ownedComments.size()-1);
 				}
 			}
 		}
 	}
 
 	public <T extends Element> void refreshList(Class<T> pivotClass, List<T> pivotElements, List<? extends PivotableElementCS> csElements) {
 		if (!pivotElements.isEmpty() ||!csElements.isEmpty()) {
 			List<T> newPivotElements = new ArrayList<T>();
 			for (PivotableElementCS csElement : csElements) {
 				T pivotElement = PivotUtil.getPivot(pivotClass, csElement);
 				if ((pivotElement == null) && (csElement instanceof ModelElementCS)) {
 					pivotElement = converter.getPivotElement(pivotClass, (ModelElementCS)csElement);
 				}
 				if ((pivotElement != null) && !newPivotElements.contains(pivotElement)) {		// OclInvalid can be prolific
 					newPivotElements.add(pivotElement);
 				}
 			}
 			PivotUtil.refreshList(pivotElements, newPivotElements);
 		}
 	}
 
 	/**
 	 * Return a pivotEClass instance cast to pivotClass registered for csElement.getCSI().
 	 * <p>An existing element is re-used else an new instance is created.
 	 * <p>The pivot element is installed as the original object of csElement. 
 	 * @param <T>
 	 * @param pivotClass
 	 * @param pivotEClass
 	 * @param csElement
 	 * @return
 	 */
 	public <T extends Element> T refreshModelElement(Class<T> pivotClass, EClass pivotEClass, ModelElementCS csElement) {
 		return converter.refreshModelElement(pivotClass, pivotEClass, csElement);
 	}
 
 	public void refreshName(NamedElement pivotNamedElement, String newName) {
 		String oldName = pivotNamedElement.getName();
 		if ((newName != oldName) && ((newName == null) || !newName.equals(oldName))) {
 			pivotNamedElement.setName(newName);
 		}
 	}
 
 	public <T extends NamedElement> T refreshNamedElement(Class<T> pivotClass,
 			EClass pivotEClass, NamedElementCS csElement) {
 		T pivotElement = refreshModelElement(pivotClass, pivotEClass, csElement);
 		refreshName(pivotElement, csElement.getName());
 		refreshComments(pivotElement, csElement);
 		return pivotElement;
 	}
 
 	public <T extends org.eclipse.ocl.examples.pivot.Package> T refreshPackage(Class<T> pivotClass, EClass pivotEClass, PackageCS csElement) {
 		Object pivotObject = csElement.getPivot();
 		if (pivotObject == null) {
 			String qualifiedName = getQualifiedName(new StringBuilder(), csElement);
 			pivotObject = oldPackagesByQualifiedName.get(qualifiedName);
 		}
 		String name = csElement.getName();
 		if ((name == null) && (csElement.eContainer() == null)) {
 			Resource csResource = csElement.eResource();
 			if (csResource != null) {
 				URI csURI = csResource.getURI();
 				if (csURI != null) {
 					name = csURI.lastSegment();
 				}
 			}
 		}
 		if (pivotObject == null) {
 			pivotObject = oldPackagesByName.get(name);
 		}
 		T pivotElement;
 		if (pivotObject == null) {
 			pivotElement = metaModelManager.createPackage(pivotClass, pivotEClass, name, csElement.getNsURI());
 //			logger.trace("Created " + pivotEClass.getName() + " : " + moniker); //$NON-NLS-1$ //$NON-NLS-2$
 			metaModelManager.installPackage(pivotElement);
 		}
 		else {
 			if (!pivotClass.isAssignableFrom(pivotObject.getClass())) {
 				throw new ClassCastException();
 			}
 			@SuppressWarnings("unchecked")
 			T pivotElement2 = (T) pivotObject;
 			pivotElement = pivotElement2;
 //			logger.trace("Reusing " + pivotEClass.getName() + " : " + moniker); //$NON-NLS-1$ //$NON-NLS-2$
 			refreshName(pivotElement, name);
 		}
 		converter.installPivotDefinition(csElement, pivotElement);
 		refreshComments(pivotElement, csElement);
 		return pivotElement;
 	}
 
 	public <T extends Element> void refreshPivotList(Class<T> pivotClass, List<? super T> pivotElements,
 			List<? extends ModelElementCS> csElements) {
 		if (pivotElements.isEmpty() && csElements.isEmpty()) {
 			return;
 		}
 		List<T> newPivotElements = new ArrayList<T>();
 		for (ModelElementCS csElement : csElements) {
 			T pivotElement = PivotUtil.getPivot(pivotClass, csElement);
 			if (pivotElement != null) {
 				newPivotElements.add(pivotElement);
 			}
 			else {
 				assert pivotElement != null;
 			}
 		}
 		PivotUtil.refreshList(pivotElements, newPivotElements);
 	}
 
 	public void refreshTemplateSignature(TemplateableElementCS csTemplateableElement, TemplateableElement pivotTemplateableElement) {
 		TemplateSignatureCS csTemplateSignature = csTemplateableElement.getOwnedTemplateSignature();
 		if (csTemplateSignature == null) {
 			if (pivotTemplateableElement.getOwnedTemplateSignature() != null) {
 				pivotTemplateableElement.setOwnedTemplateSignature(null);
 			}
 			return;
 		}
 		TemplateSignature pivotTemplateSignature = refreshModelElement(TemplateSignature.class, PivotPackage.Literals.TEMPLATE_SIGNATURE, csTemplateSignature);
 		if (pivotTemplateableElement.getOwnedTemplateSignature() != pivotTemplateSignature) {
 			pivotTemplateableElement.setOwnedTemplateSignature(pivotTemplateSignature);
 		}
 		List<TemplateParameter> newPivotTemplateParameters = new ArrayList<TemplateParameter>();
 		List<TemplateParameterCS> csTemplateParameters = csTemplateSignature.getOwnedTemplateParameter();
 		for (TemplateParameterCS csTemplateParameter : csTemplateParameters) {
 			org.eclipse.ocl.examples.pivot.Class pivotTemplateParameterClass = refreshNamedElement(org.eclipse.ocl.examples.pivot.Class.class,
 				PivotPackage.Literals.CLASS, csTemplateParameter);
 			TemplateParameter pivotTemplateParameter = pivotTemplateParameterClass.getOwningTemplateParameter();
 			if (pivotTemplateParameter == null) {
 				pivotTemplateParameter = PivotFactory.eINSTANCE.createTypeTemplateParameter();
 				pivotTemplateParameter.setOwnedParameteredElement(pivotTemplateParameterClass);
 			}
 			newPivotTemplateParameters.add(pivotTemplateParameter);
 		}
 		PivotUtil.refreshList(pivotTemplateSignature.getOwnedParameters(), newPivotTemplateParameters);
 	}
 	
 	public <T extends TypedMultiplicityElement> T  refreshTypedMultiplicityElement(Class<T> pivotClass,
 			EClass pivotEClass, TypedElementCS csTypedElement) {
 		T pivotElement = refreshNamedElement(pivotClass, pivotEClass, csTypedElement);
 		refreshMultiplicity(pivotElement, csTypedElement);
 		return pivotElement;
 	}
 
 	public <T> void refreshMultiplicity(MultiplicityElement pivotElement, TypedElementCS csTypedElement) {
 		pivotElement.setIsOrdered(ElementUtil.isOrdered(csTypedElement));
 		pivotElement.setIsUnique(ElementUtil.isUnique(csTypedElement));
 		int lower = ElementUtil.getLower(csTypedElement);
 		int upper = ElementUtil.getUpper(csTypedElement);
 		pivotElement.setLower(BigInteger.valueOf(lower));
 		pivotElement.setUpper(BigInteger.valueOf(upper));
 	}
 
 	protected void resetPivotMappings(Collection<? extends Resource> csResources) {
 		for (Resource csResource : csResources) {
 			for (TreeIterator<EObject> tit = csResource.getAllContents(); tit.hasNext(); ) {
 				EObject eObject = tit.next();
 				if (eObject instanceof Pivotable) {
 					Pivotable pivotable = (Pivotable)eObject;
 //					Element pivot = pivotable.getPivot();
 					pivotable.resetPivot();
 				}				
 			}
 		}
 	}
 
 	public void resolveNamespaces(List<Namespace> namespaces) {
 		for (Namespace namespace : namespaces) {
 			@SuppressWarnings("unused")
 			Namespace dummy = namespace;	// Resolves the proxies from the outside.
 		}
 	}
 
 	protected void resolveUnderspecifiedTypes() {
 		for (TypedElement underspecifiedTypedElement : underspecifiedTypedElements) {
 			Type underspecifiedType = underspecifiedTypedElement.getType();
 			Type resolvedType = resolveUnderspecifiedType(underspecifiedType);
 			underspecifiedTypedElement.setType(resolvedType);
 		}		
 	}
 	
 	protected Type resolveUnderspecifiedType(Type type) {
 		if (type instanceof UnspecifiedType) {
 			return ((UnspecifiedType)type).getLowerBound();
 		}
 		if (type instanceof CollectionType) {
 			CollectionType collectionType = (CollectionType)type;
 			Type resolvedElementType = resolveUnderspecifiedType(collectionType.getElementType());
 			return metaModelManager.getCollectionType(collectionType.getName(), resolvedElementType);
 		}
 		if (type instanceof PrimitiveType) {
 			return type;
 		}
 		if (type instanceof TupleType) {
 			TupleType tupleType = (TupleType)type;
 			List<Property> resolvedProperties = new ArrayList<Property>();
 			for (Property part : ((TupleType)type).getOwnedAttributes()) {
 				if (metaModelManager.isUnderspecified(part.getType())) {
 					Property prop = PivotFactory.eINSTANCE.createProperty();
 					prop.setName(part.getName());
 					prop.setType(resolveUnderspecifiedType(part.getType()));
 					resolvedProperties.add(part);
 				}
 				else {
 					resolvedProperties.add(part);
 				}
 			}
 			return metaModelManager.getTupleType(tupleType.getName(), resolvedProperties, null);
 		}
 		if (type instanceof ClassifierType) {
 			ClassifierType classifierType = (ClassifierType)type;
 			Type resolvedElementType = resolveUnderspecifiedType(classifierType.getInstanceType());
 			return metaModelManager.getClassifierType(resolvedElementType);
 		}
 		throw new UnsupportedOperationException();
 //		return null;
 	}
 
 	public void setReferredIteration(LoopExp expression, Iteration iteration) {
 		expression.setReferredIteration(iteration);
 	}
 
 	public void setReferredOperation(OperationCallExp expression, Operation operation) {
 		expression.setReferredOperation(operation);
 	}
 
 	/**
 	 * Set the type and so potentially satisfy some TypeOfDependency. This method ensures that
 	 * type is not set to null.
 	 * 
 	 * @param pivotExpression
 	 * @param type
 	 */
 	public void setType(TypedElement pivotElement, Type type) {
 	//	PivotUtil.debugObjectUsage("setType ", pivotElement);
 	//	PivotUtil.debugObjectUsage(" to ", type);
 //		if (type != null) {
 //			if (type.eResource() == null) {			// WIP
 	//			PivotUtil.debugObjectUsage("setType orphan ", type);
 //				assert false;
 //			}
 //		}
 //		if (type == null) {
 //			type = metaModelManager.getOclInvalidType();	// FIXME unresolved type with explanation
 //		}
 		if (type != pivotElement.getType()) {
 			pivotElement.setType(type);
 			if (metaModelManager.isUnderspecified(type)) {
 				addUnderspecifiedTypedElement(pivotElement);
 			}
 		}
 		if (type != null) {
 			PivotUtil.debugWellContainedness(type);
 		}
 	}
 
 	public void setTypeWithMultiplicity(TypedElement typedElement, TypedMultiplicityElement typedMultiplicityElement) {
 		if ((typedMultiplicityElement != null) && !typedMultiplicityElement.eIsProxy()) {
 			Type type = metaModelManager.getTypeWithMultiplicity(typedMultiplicityElement);
 			if ((type != null) && !type.eIsProxy()) {
 				setType(typedElement, type);
 				return;
 			}
 		}
 		setType(typedElement, null);
 	}
 
 	/**
 	 * Update a list of TemplateBinding to match a list of TemplateSignature
 	 * by moving/adding/removing existing entries. Once matched each TemplateBinding.signature
 	 * references the corresponding TemplateSignature, and each TemplateBinding.parameterSubstitution.formal
 	 * references the corresponding TemplateSignature.ownedParameter.
 	 * 
 	 * @param templateBindings
 	 * @param templateSignatures
 	 */
 	protected void specializeTemplateBindings(List<TemplateBinding> templateBindings, List<TemplateSignature> templateSignatures, List<TemplateBindingCS> csTemplateBindings) {
 		int csIMax = csTemplateBindings.size();
 		int pivotIMax = templateSignatures.size();
 		if (csIMax != pivotIMax) {
 			logger.warn("Inconsistent template bindings size for " + CS2Moniker.toString(csTemplateBindings.get(0).getOwningTemplateBindableElement())); //$NON-NLS-1$
 		}
 		int newMax = Math.min(csIMax, pivotIMax);
 		for (int i = 0; i < newMax; i++) {					// Invariant: lists are equal up to index i
 			TemplateBindingCS csTemplateBinding = csTemplateBindings.get(i);
 			TemplateSignature templateSignature = templateSignatures.get(i);
 			int oldMax = templateBindings.size();
 			TemplateBinding templateBinding = null;;
 			for (int j = i; j < oldMax; j++) {
 				TemplateBinding oldTemplateBinding = templateBindings.get(j);
 				if (oldTemplateBinding.getSignature() == templateSignature) {
 					if (j != i) {
 						templateBindings.add(i, templateBindings.remove(j));
 					}
 					templateBinding = oldTemplateBinding;
 //					registerPivotElement(csTemplateBinding, templateBinding);
 //					installPivotElement(csTemplateBinding, templateBinding);
 					break;
 				}
 			}
 			if (templateBinding == null) {
 //				templateBinding = refreshElement(TemplateBinding.class, PivotPackage.Literals.TEMPLATE_BINDING, csTemplateBinding);
 				templateBinding = PivotFactory.eINSTANCE.createTemplateBinding();
 				templateBinding.setSignature(templateSignature);
 				if (i < oldMax) {
 					templateBindings.add(i, templateBinding);
 				}
 				else {
 					templateBindings.add(templateBinding);					
 				}
 			}
 			installPivotReference(csTemplateBinding, templateBinding, BaseCSTPackage.Literals.PIVOTABLE_ELEMENT_CS__PIVOT);
 			specializeTemplateParameterSubstitutions(templateBinding.getParameterSubstitutions(), templateSignature.getOwnedParameters(), csTemplateBinding.getOwnedParameterSubstitution());
 			assert templateSignatures.get(i) == templateBindings.get(i).getSignature();
 		}
 		for (int k = templateBindings.size(); k > newMax; ) {
 			templateBindings.remove(--k);
 		}
 		assert templateSignatures.size() == templateBindings.size();
 	}
 
 	/**
 	 * Update a list of TemplateParameterSubstitution to match a list of TemplateParameter
 	 * by moving/adding/removing existing entries. Once matched each TemplateParameterSubstitution.formal
 	 * references the corresponding TemplateParameter.
 	 * @param eList 
 	 * 
 	 * @param templateSignature
 	 * @param templateBinding
 	 */
 	protected void specializeTemplateParameterSubstitutions(List<TemplateParameterSubstitution> templateParameterSubstitutions,
 			List<TemplateParameter> templateParameters, List<TemplateParameterSubstitutionCS> csTemplateParameterSubstitutions) {
 		int csIMax = csTemplateParameterSubstitutions.size();
 		int pivotIMax = templateParameters.size();
 		if (csIMax != pivotIMax) {
 			logger.warn("Inconsistent template parameter substitutions size"); // FIXME //$NON-NLS-1$
 		}
 		int newMax = Math.min(csIMax, pivotIMax);
 		for (int i = 0; i < newMax; i++) {					// Invariant: lists are equal up to index i
 			TemplateParameterSubstitutionCS csTemplateParameterSubstitution = csTemplateParameterSubstitutions.get(i);
 			TemplateParameter templateParameter = templateParameters.get(i);
 			int oldMax = templateParameterSubstitutions.size();
 			TemplateParameterSubstitution templateParameterSubstitution = null;
 			for (int j = i; j < oldMax; j++) {
 				TemplateParameterSubstitution oldTemplateParameterSubstitution = templateParameterSubstitutions.get(j);
 				if (oldTemplateParameterSubstitution.getFormal() == templateParameter) {
 					if (j != i) {
 						templateParameterSubstitutions.add(i, templateParameterSubstitutions.remove(j));
 					}
 					templateParameterSubstitution = oldTemplateParameterSubstitution;
 //					registerPivotElement(csTemplateParameterSubstitution, templateParameterSubstitution);
 //					installPivotElement(csTemplateParameterSubstitution, templateParameterSubstitution);
 					break;
 				}
 			}
 			if (templateParameterSubstitution == null) {
 //				templateParameterSubstitution = refreshElement(TemplateParameterSubstitution.class, PivotPackage.Literals.TEMPLATE_PARAMETER_SUBSTITUTION, csTemplateParameterSubstitution);
 				templateParameterSubstitution = PivotFactory.eINSTANCE.createTemplateParameterSubstitution();
 				templateParameterSubstitution.setFormal(templateParameter);
 				if (i < oldMax) {
 					templateParameterSubstitutions.add(i, templateParameterSubstitution);
 				}
 				else {
 					templateParameterSubstitutions.add(templateParameterSubstitution);
 				}
 			}
 			TypeRefCS csActualParameter = csTemplateParameterSubstitution.getOwnedActualParameter();
 			if (csActualParameter instanceof WildcardTypeRefCS) {
 				ParameterableElement pivotActualParameter = templateParameterSubstitution.getOwnedActual();
 				if (pivotActualParameter == null) {
 					pivotActualParameter = PivotFactory.eINSTANCE.createClass();
 					templateParameterSubstitution.setOwnedActual(pivotActualParameter);
 				}
 				String name = PivotConstants.WILDCARD_NAME;
 				if (i > 1) {
 					name += i;
 				}
 				((NamedElement)pivotActualParameter).setName(name);
 			}
 			else {
 				ParameterableElement pivotActualParameter = PivotUtil.getPivot(ParameterableElement.class, csActualParameter);
 				templateParameterSubstitution.setActual(pivotActualParameter);
 			}
 //			installPivotElement(csTemplateParameterSubstitution, templateParameterSubstitution);
 //			queueResolver(csTemplateParameterSubstitution);		// To resolve actuals
 			assert templateParameters.get(i) == templateParameterSubstitutions.get(i).getFormal();
 		}
 		for (int k = templateParameterSubstitutions.size(); k > newMax; ) {
 			templateParameterSubstitutions.remove(--k);
 		}
 		assert templateParameters.size() == templateParameterSubstitutions.size();
 	}
 
 	protected TemplateableElement specializeTemplates(TypedTypeRefCS csElement) {
 		TemplateBindingCS ownedTemplateBinding = csElement.getOwnedTemplateBinding();
 		assert ownedTemplateBinding != null;
 		Type unspecializedPivotElement = csElement.getType();
 //		logger.trace("Specializing " + moniker); //$NON-NLS-1$
 		if ((unspecializedPivotElement == null) || unspecializedPivotElement.eIsProxy()) {
 			String moniker = CS2Moniker.toString(csElement);
 			logger.error("Nothing to specialize as " + moniker); //$NON-NLS-1$
 			return null;
 		}
 /*WIP		List<Type> templateArguments = new ArrayList<Type>();
 		for (TemplateParameterSubstitutionCS tps : ownedTemplateBinding.getOwnedParameterSubstitution()) {
 			Type templateArgument = PivotUtil.getPivot(Type.class, tps.getOwnedActualParameter());
 			templateArguments.add(templateArgument);
 		}
 		TemplateSignature templateSignature = unspecializedPivotElement.getOwnedTemplateSignature();
 		List<TemplateParameter> templateParameters = templateSignature != null ? templateSignature.getParameters() : Collections.<TemplateParameter>emptyList();
 		boolean isUnspecialized = PivotUtil.isUnspecialized(templateParameters, templateArguments);	// WIP
 		if (isUnspecialized) {
 //			int iMax = templateParameters.size();
 //			assert templateArguments.size() == iMax;
 //			for (int i = 0; i < iMax; i++) {
 //				reusePivotElement(ownedTemplateBinding.getOwnedParameterSubstitution().get(i), templateParameters.get(i).getParameteredElement());
 //			}
 			reusePivotElement(csElement, unspecializedPivotElement);
 			return unspecializedPivotElement;
 		} */
 		//
 		//	Refresh the pivot specialization root
 		//
 		Type specializedPivotElement = PivotUtil.getPivot(Type.class, csElement);
 		if (specializedPivotElement == null) {
 			List<Type> templateArguments = new ArrayList<Type>();
 			for (TemplateParameterSubstitutionCS csTemplateParameterSubstitution : ownedTemplateBinding.getOwnedParameterSubstitution()) {
 				Type templateArgument = PivotUtil.getPivot(Type.class, csTemplateParameterSubstitution.getOwnedActualParameter());
 				templateArguments.add(templateArgument);
 			}
 			specializedPivotElement = metaModelManager.getLibraryType(unspecializedPivotElement, templateArguments);
 /*			EClass eClass = unspecializedPivotElement.eClass();
 			@SuppressWarnings("unchecked")
 			Class<? extends Type> pivotClazz = (Class<? extends Type>) eClass.getInstanceClass();
 			Type pivotClass = converter.refreshReferencedElement(pivotClazz, eClass, csElement, BaseCSTPackage.Literals.MODEL_ELEMENT_CS__PIVOT);
 			refreshName(pivotClass, unspecializedPivotElement.getName());
 			specializedPivotElement = pivotClass;
 			if (pivotClass instanceof CollectionType) {
 				Type elementType = PivotUtil.getPivot(Type.class, ownedTemplateBinding.getOwnedParameterSubstitution().get(0).getOwnedActualParameter());
 				((CollectionType)pivotClass).setElementType(elementType);
 			}
 			else if (pivotClass instanceof ClassifierType) {
 				Type instanceType = PivotUtil.getPivot(Type.class, ownedTemplateBinding.getOwnedParameterSubstitution().get(0).getOwnedActualParameter());
 				((ClassifierType)pivotClass).setInstanceType(instanceType);
 			}
 			specializedPivotElement.setUnspecializedElement(unspecializedPivotElement);
 //WIP			metaModelManager.addOrphanClass(specializedPivotElement); */
 		}
 		installPivotReference(csElement, specializedPivotElement, BaseCSTPackage.Literals.TYPED_TYPE_REF_CS__TYPE);
 		if (specializedPivotElement != unspecializedPivotElement) {
 			//
 			//	Refresh the pivot specialization bindings and parameter substitutions
 			//
 			List<TemplateBinding> templateBindings = specializedPivotElement.getTemplateBindings();
 			List<TemplateSignature> templateSignatures = getTemplateSignatures(unspecializedPivotElement);
 			List<TemplateBindingCS> csTemplateBindings = getTemplateBindings(csElement);
 			specializeTemplateBindings(templateBindings, templateSignatures, csTemplateBindings);
 		}
 		return specializedPivotElement;
 	}
 
 	/**
 	 * Remove the internal "* " that may result from a comment formatted in the style of this comment.
 	 * 
 	 * @param string
 	 * @return
 	 */
 	protected String trimComments(String string) {
 		String[] strings = string.trim().split("\n");
 		boolean isFormatted = true;
 		for (int i = 0; i < strings.length; i++) {
 			String line = strings[i];
 			String trimmedLine = line.trim();
 			if (trimmedLine.startsWith("*")) {
 				if (trimmedLine.length() > 1) {
 					if (!Character.isWhitespace(trimmedLine.charAt(1))) {
 						isFormatted = false;
 						break;
 					}
 				}
 			}
 			else if (i > 0) {
 				isFormatted = false;
 				break;
 			}
 		}
 		StringBuilder s = new StringBuilder();
 		for (String line : strings) {
 			String trimmedLine = line.trim();
 			if (!isFormatted) {
 			}
 			else if (trimmedLine.length() <= 1) {
 				trimmedLine = "";
 			}
 			else if (Character.isWhitespace(trimmedLine.charAt(1))) {
 				trimmedLine = trimmedLine.substring(2);			
 			}
 			if (s.length() > 0) {
 				s.append("\n");
 			}
 			s.append(trimmedLine);
 		}
 		String trimmedString = s.toString();
 		return trimmedString;
 	}
 
 	/**
 	 * Sequence the update passes to make the pivot match the CS.
 	 * @param csResources 
 	 */
 	public boolean update() {
 		resetPivotMappings(csResources);
 		oldPackagesByName = new HashMap<String, org.eclipse.ocl.examples.pivot.Package>();
 		oldPackagesByQualifiedName = new HashMap<String, org.eclipse.ocl.examples.pivot.Package>();
 		for (Resource resource : converter.cs2pivotResourceMap.values()) {
 			gatherOldPackages(resource.getContents());
 		}
 		List<BasicContinuation<?>> continuations = new ArrayList<BasicContinuation<?>>();
 		//
 		//	Perform the pre-order traversal to create packages, unspecialized classes and precedences.
 		//
 		for (Resource csResource : csResources) {
 			visitInPreOrder(csResource.getContents(), continuations);
 		}
 		//
 		//	Put all orphan root pivot elements in their resources.
 		//
 		for (Resource csResource : csResources) {
 			installRootContents(csResource);
 		}
 		//
 		//	Perform pre-order continuations to establish package, class containment and classifier template signatures.
 		//
 //		Collections.reverse(continuations);
 		while (continuations.size() > 0) {
 			List<BasicContinuation<?>> moreContinuations = progressContinuations(continuations);
 			if (moreContinuations == null) {
 				boolean hasNoErrors = checkForNoErrors(csResources);
 				if (!hasNoErrors) {
 					return false;
 				}
 				diagnoseContinuationFailure(continuations);
 				break;
 			}
 			continuations = moreContinuations;
 		}
 		//
 		//	Load the library by loading external content or exploiting the pre-ordered content. 
 		//
 		if ((metaModelManager.getLibraryResource() == null) && (metaModelManager.getDefaultStandardLibraryURI() == null))  {
 			for (Resource resource : converter.cs2pivotResourceMap.values()) {
 				metaModelManager.loadLibrary(resource);
 			}
 		}
 		AnyType oclAnyType = metaModelManager.getOclAnyType();
 		if (oclAnyType == null) {
 			return false;				// FIXME throw ??
 		}
 		//
 		//	Perform the post-order traversal to create and install the bulk of non-package/class
 		//	elements.
 		//
 		for (Resource csResource : csResources) {
 			visitInPostOrder(csResource.getContents(), continuations);
 		}
 		boolean hasNoErrors = checkForNoErrors(csResources);
 		if (!hasNoErrors) {
 			return false;
 		}
 		//
 		//	Perform post-order continuations to establish complex dependencies.
 		//
 		while (continuations.size() > 0) {
 			List<BasicContinuation<?>> moreContinuations = progressContinuations(continuations);
 			if (moreContinuations == null) {
 				diagnoseContinuationFailure(continuations);
 				break;
 			}
 			continuations = moreContinuations;
 		}
 		//
 		//	Put all orphan root pivot elements in their resources.
 		//
 		for (Resource csResource : csResources) {
 			installRootContents(csResource);		// FIXME ExpressionInOcl very late
 		}
 		//
 		//	Resolve UnspecifiedTypes
 		//
 		if (underspecifiedTypedElements != null) {
 			resolveUnderspecifiedTypes();
 		}
 		boolean hasNoMoreErrors = checkForNoErrors(csResources);
 		if (!hasNoMoreErrors) {
 			return false;
 		}
 		//
 		//	Prune obsolete packages
 		//
 		Set<org.eclipse.ocl.examples.pivot.Package> newPackages = new HashSet<org.eclipse.ocl.examples.pivot.Package>();
 		for (Resource csResource : csResources) {
 			gatherNewPackages(newPackages, csResource);
 		}
 		Set<org.eclipse.ocl.examples.pivot.Package> obsoletePackages = new HashSet<org.eclipse.ocl.examples.pivot.Package>(oldPackagesByQualifiedName.values());
 //		for (org.eclipse.ocl.examples.pivot.Package oldPackage : obsoletePackages) {
 //			System.out.println("Old package @" + Integer.toHexString(oldPackage.hashCode()) + " " + oldPackage.eResource().getURI() + " " + oldPackage.getName());
 //		}
 //		for (org.eclipse.ocl.examples.pivot.Package newPackage : newPackages) {
 //			System.out.println("New package @" + Integer.toHexString(newPackage.hashCode()) + " " + newPackage.eResource().getURI() + " " + newPackage.getName());
 //		}
 		obsoletePackages.removeAll(newPackages);
 		for (org.eclipse.ocl.examples.pivot.Package obsoletePackage : obsoletePackages) {
 			EObject eContainer = obsoletePackage.eContainer();
 			if (eContainer != null) {
 				EReference eContainmentFeature = obsoletePackage.eContainmentFeature();
 				if (eContainmentFeature.isMany()) {
 					List<?> siblings = (List<?>) eContainer.eGet(eContainmentFeature);
 //					System.out.println("Kill package @" + Integer.toHexString(obsoletePackage.hashCode()) + " " + obsoletePackage.eResource().getURI() + " " + obsoletePackage.getName());
 					siblings.remove(obsoletePackage);
 				}
 				else {
 					eContainer.eSet(eContainmentFeature, null);
 				}
 			}
 		}
 		return true;
 	}
 
 	public <T extends Element> T visitLeft2Right(Class<T> pivotClass, VisitableCS csObject) {
 		if (csObject == null) {
 			return null;
 		}
 		BaseCSVisitor<Element, CS2PivotConversion> left2RightVisitor = getLeft2RightVisitor(csObject.eClass().getEPackage());
 		if (left2RightVisitor == null) {
 			throw new IllegalArgumentException("Unsupportable " + csObject.eClass().getName() + " for CS2Pivot Left2Right pass");
 		}
 		Element monikeredElement = csObject.accept(left2RightVisitor);
		if (monikeredElement == null) {
			return null;
		}
 		if (!pivotClass.isAssignableFrom(monikeredElement.getClass())) {
 			throw new ClassCastException(monikeredElement.getClass().getName() + " is not assignable to " + pivotClass.getName());
 		}
 		@SuppressWarnings("unchecked")
 		T castElement = (T) monikeredElement;
 		return castElement;
 	}
 
 	protected void visitInPostOrder(List<? extends EObject> eObjects, List<BasicContinuation<?>> continuations) {
 		for (int i = eObjects.size(); i-- > 0; ) {
 			EObject eObject = eObjects.get(i);
 			BaseCSVisitor<Continuation<?>, CS2PivotConversion> postOrderVisitor = getPostOrderVisitor(eObject.eClass().getEPackage());
 			if ((postOrderVisitor == null) || !(eObject instanceof VisitableCS)) {
 				throw new IllegalArgumentException("Unsupportable " + eObject.eClass().getName() + " for CS2Pivot PostOrder pass");
 			}
 			List<EObject> eContents = eObject.eContents();
 			if (eContents.size() > 0) {
 				visitInPostOrder(eContents, continuations);
 			}
 			Continuation<?> continuation = ((VisitableCS)eObject).accept(postOrderVisitor);
 			if (continuation != null) {
 				continuation.addTo(continuations);
 			}
 		}
 	}
 
 	protected void visitInPreOrder(List<? extends EObject> eObjects, List<BasicContinuation<?>> continuations) {
 		for (EObject eObject : eObjects) {
 			BaseCSVisitor<Continuation<?>, CS2PivotConversion> preOrderVisitor = getPreOrderVisitor(eObject.eClass().getEPackage());
 			if ((preOrderVisitor == null) || !(eObject instanceof VisitableCS)) {
 				throw new IllegalArgumentException("Unsupportable " + eObject.eClass().getName() + " for CS2Pivot PreOrder pass");
 			}
 			Continuation<?> continuation = ((VisitableCS)eObject).accept(preOrderVisitor);
 			if (continuation != null) {
 				continuation.addTo(continuations);
 			}
 			List<EObject> eContents = eObject.eContents();
 			if (eContents.size() > 0) {
 				visitInPreOrder(eContents, continuations);
 			}
 		}
 	}
 }
