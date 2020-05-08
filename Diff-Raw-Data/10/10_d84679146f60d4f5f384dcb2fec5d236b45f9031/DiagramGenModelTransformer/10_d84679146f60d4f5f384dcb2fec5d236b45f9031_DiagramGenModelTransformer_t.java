 /*
  * Copyright (c) 2005 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.bridge.genmodel;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
 import org.eclipse.emf.codegen.ecore.genmodel.GenClassifier;
 import org.eclipse.emf.codegen.ecore.genmodel.GenDataType;
 import org.eclipse.emf.codegen.ecore.genmodel.GenFeature;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EModelElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.gmf.codegen.gmfgen.FeatureLabelModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.FeatureLinkModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenFactory;
 import org.eclipse.gmf.codegen.gmfgen.GenAuditContainer;
 import org.eclipse.gmf.codegen.gmfgen.GenAuditRule;
 import org.eclipse.gmf.codegen.gmfgen.GenAuditable;
 import org.eclipse.gmf.codegen.gmfgen.GenAuditedMetricTarget;
 import org.eclipse.gmf.codegen.gmfgen.GenChildContainer;
 import org.eclipse.gmf.codegen.gmfgen.GenChildLabelNode;
 import org.eclipse.gmf.codegen.gmfgen.GenChildNode;
 import org.eclipse.gmf.codegen.gmfgen.GenChildSideAffixedNode;
 import org.eclipse.gmf.codegen.gmfgen.GenCommonBase;
 import org.eclipse.gmf.codegen.gmfgen.GenCompartment;
 import org.eclipse.gmf.codegen.gmfgen.GenConstraint;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagram;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagramElementTarget;
 import org.eclipse.gmf.codegen.gmfgen.GenDomainAttributeTarget;
 import org.eclipse.gmf.codegen.gmfgen.GenDomainElementTarget;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
 import org.eclipse.gmf.codegen.gmfgen.GenElementInitializer;
 import org.eclipse.gmf.codegen.gmfgen.GenExpressionInterpreter;
 import org.eclipse.gmf.codegen.gmfgen.GenExpressionProviderBase;
 import org.eclipse.gmf.codegen.gmfgen.GenExpressionProviderContainer;
 import org.eclipse.gmf.codegen.gmfgen.GenFeatureInitializer;
 import org.eclipse.gmf.codegen.gmfgen.GenFeatureSeqInitializer;
 import org.eclipse.gmf.codegen.gmfgen.GenFeatureValueSpec;
 import org.eclipse.gmf.codegen.gmfgen.GenLanguage;
 import org.eclipse.gmf.codegen.gmfgen.GenLink;
 import org.eclipse.gmf.codegen.gmfgen.GenLinkConstraints;
 import org.eclipse.gmf.codegen.gmfgen.GenLinkLabel;
 import org.eclipse.gmf.codegen.gmfgen.GenMeasurable;
 import org.eclipse.gmf.codegen.gmfgen.GenMetricContainer;
 import org.eclipse.gmf.codegen.gmfgen.GenMetricRule;
 import org.eclipse.gmf.codegen.gmfgen.GenNavigator;
 import org.eclipse.gmf.codegen.gmfgen.GenNode;
 import org.eclipse.gmf.codegen.gmfgen.GenNodeLabel;
 import org.eclipse.gmf.codegen.gmfgen.GenNotationElementTarget;
 import org.eclipse.gmf.codegen.gmfgen.GenPropertySheet;
 import org.eclipse.gmf.codegen.gmfgen.GenReferenceNewElementSpec;
 import org.eclipse.gmf.codegen.gmfgen.GenRuleTarget;
 import org.eclipse.gmf.codegen.gmfgen.GenSeverity;
 import org.eclipse.gmf.codegen.gmfgen.GenTopLevelNode;
 import org.eclipse.gmf.codegen.gmfgen.LabelModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.LabelOffsetAttributes;
 import org.eclipse.gmf.codegen.gmfgen.LinkLabelAlignment;
 import org.eclipse.gmf.codegen.gmfgen.LinkModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.MetamodelType;
 import org.eclipse.gmf.codegen.gmfgen.OpenDiagramBehaviour;
 import org.eclipse.gmf.codegen.gmfgen.Palette;
 import org.eclipse.gmf.codegen.gmfgen.ProviderPriority;
 import org.eclipse.gmf.codegen.gmfgen.SpecializationType;
 import org.eclipse.gmf.codegen.gmfgen.TypeLinkModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.TypeModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.ValueExpression;
 import org.eclipse.gmf.gmfgraph.Alignment;
 import org.eclipse.gmf.gmfgraph.AlignmentFacet;
 import org.eclipse.gmf.gmfgraph.Compartment;
 import org.eclipse.gmf.gmfgraph.Direction;
 import org.eclipse.gmf.gmfgraph.LabelOffsetFacet;
 import org.eclipse.gmf.gmfgraph.Node;
 import org.eclipse.gmf.internal.bridge.History;
 import org.eclipse.gmf.internal.bridge.Knowledge;
 import org.eclipse.gmf.internal.bridge.NaiveIdentifierDispenser;
 import org.eclipse.gmf.internal.bridge.VisualIdentifierDispenser;
 import org.eclipse.gmf.internal.bridge.genmodel.navigator.NavigatorHandler;
 import org.eclipse.gmf.internal.bridge.naming.gen.GenModelNamingMediator;
 import org.eclipse.gmf.internal.bridge.tooldef.PaletteHandler;
 import org.eclipse.gmf.mappings.AuditContainer;
 import org.eclipse.gmf.mappings.AuditRule;
 import org.eclipse.gmf.mappings.AuditedMetricTarget;
 import org.eclipse.gmf.mappings.CanvasMapping;
 import org.eclipse.gmf.mappings.ChildReference;
 import org.eclipse.gmf.mappings.CompartmentMapping;
 import org.eclipse.gmf.mappings.Constraint;
 import org.eclipse.gmf.mappings.DiagramElementTarget;
 import org.eclipse.gmf.mappings.DomainAttributeTarget;
 import org.eclipse.gmf.mappings.DomainElementTarget;
 import org.eclipse.gmf.mappings.ElementInitializer;
 import org.eclipse.gmf.mappings.FeatureInitializer;
 import org.eclipse.gmf.mappings.FeatureSeqInitializer;
 import org.eclipse.gmf.mappings.FeatureValueSpec;
 import org.eclipse.gmf.mappings.GMFMapPackage;
 import org.eclipse.gmf.mappings.LabelMapping;
 import org.eclipse.gmf.mappings.Language;
 import org.eclipse.gmf.mappings.LinkConstraints;
 import org.eclipse.gmf.mappings.LinkMapping;
 import org.eclipse.gmf.mappings.Mapping;
 import org.eclipse.gmf.mappings.MappingEntry;
 import org.eclipse.gmf.mappings.MetricContainer;
 import org.eclipse.gmf.mappings.MetricRule;
 import org.eclipse.gmf.mappings.NodeMapping;
 import org.eclipse.gmf.mappings.NodeReference;
 import org.eclipse.gmf.mappings.NotationElementTarget;
 import org.eclipse.gmf.mappings.ReferenceNewElementSpec;
 import org.eclipse.gmf.mappings.Severity;
 import org.eclipse.gmf.mappings.TopNodeReference;
 
 /**
  * Creates generation model from diagram definition.
  * @author artem
  */
 @SuppressWarnings("unchecked")
 public class DiagramGenModelTransformer extends MappingTransformer {
 
 	private GenEditorGenerator myGenModel;
 	protected GenModelMatcher myGenModelMatch;
 	private final DiagramRunTimeModelHelper myDRTHelper;
 	private final ViewmapProducer myViewmaps;
 	private final VisualIdentifierDispenser myVisualIDs;
 	private final History myHistory;
 	private final Map myProcessedTypes = new IdentityHashMap(); // GenClass -> MetamodelType
 	private final Set myProcessedExpressions = new HashSet();
 
 	private final GenModelNamingMediator myNamingStrategy;
 	private final PaletteHandler myPaletteProcessor;
 	private final NavigatorHandler myNavigatorProcessor;
 	private final PropertySheetHandler myPropertySheetProcessor;
 	private final EcoreGenModelMatcher myEcoreGenModelMatch;	
 
 	public DiagramGenModelTransformer(DiagramRunTimeModelHelper drtHelper, GenModelNamingMediator namingStrategy) {
 		this(drtHelper, namingStrategy, new InnerClassViewmapProducer(), new NaiveIdentifierDispenser());
 	}
 
 	public DiagramGenModelTransformer(DiagramRunTimeModelHelper drtHelper, GenModelNamingMediator namingStrategy, ViewmapProducer viewmaps, VisualIdentifierDispenser visualIdD) {
 		assert drtHelper != null && namingStrategy != null && viewmaps != null;
 		myDRTHelper = drtHelper;
 		myNamingStrategy = namingStrategy;
 		myViewmaps = viewmaps;
 		myVisualIDs = visualIdD;
 		myHistory = new History();
 		myPaletteProcessor = new PaletteHandler();
 		myNavigatorProcessor = new NavigatorHandler();
 		myPropertySheetProcessor = new PropertySheetHandler();
 		myEcoreGenModelMatch = new EcoreGenModelMatcher();
 	}
 
 	/**
 	 * Optionally set GenModel to match ECore elements against. 
 	 * Should be invoked prior to {@link MappingTransformer#transform(Mapping)}, otherwise has no effect.
 	 * Useful for tests (and other cases) when GenModel is not known to EMF 
 	 * (and thus can't be obtained using EMF techniques).
 	 * @param emfGenModel EMF GenModel for domain model
 	 */
 	public void setEMFGenModel(GenModel emfGenModel) {
 		myGenModelMatch = new GenModelMatcher(emfGenModel);
 	}
 
 	public GenEditorGenerator getResult() {
 		return getGenEssence();
 	}
 
 	private GenEditorGenerator getGenEssence() {
 		if (myGenModel == null) {
 			myGenModel = GMFGenFactory.eINSTANCE.createGenEditorGenerator();
 		}
 		// init editor as well - transformer does not set any property to it, just make sure it's not null
 		if (myGenModel.getEditor() == null) {
 			myGenModel.setEditor(GMFGenFactory.eINSTANCE.createGenEditorView());
 		}
 		return myGenModel;
 	}
 
 	private GenDiagram getGenDiagram() {
 		if (getGenEssence().getDiagram() == null) {
 			getGenEssence().setDiagram(GMFGenFactory.eINSTANCE.createGenDiagram());
 		}
 		return getGenEssence().getDiagram();
 	}
 
 	private void initGenPlugin() {
 		if (getGenEssence().getPlugin() == null) {
 			getGenEssence().setPlugin(GMFGenFactory.eINSTANCE.createGenPlugin());
 		}
 	}
 	
 	private GenNavigator genGenNavigator() {
 		if (getGenEssence().getNavigator() == null) {
 			getGenEssence().setNavigator(GMFGenFactory.eINSTANCE.createGenNavigator());
 		}
 		return getGenEssence().getNavigator();
 	}
 
 	private Palette createGenPalette() {
 		Palette p = getGenDiagram().getPalette();
 		if (p == null) {
 			p = GMFGenFactory.eINSTANCE.createPalette();
 			getGenDiagram().setPalette(p);
 		}
 		return p;
 	}
 
 	private GenPropertySheet createPropertySheet() {
 		if (getGenEssence().getPropertySheet() == null) {
 			getGenEssence().setPropertySheet(GMFGenFactory.eINSTANCE.createGenPropertySheet());
 		}
 		return getGenEssence().getPropertySheet();
 	}
 
 	protected void process(CanvasMapping mapping) {
 		if (myGenModelMatch == null && mapping.getDomainModel() != null) {
 			myGenModelMatch = new GenModelMatcher(mapping.getDomainModel());
 		}
 		myHistory.purge();
 		if (mapping.getPalette() != null) {
 			myPaletteProcessor.initialize(createGenPalette());
 			myPaletteProcessor.process(mapping.getPalette());
 		}
 		myNavigatorProcessor.initialize(getGenDiagram(), genGenNavigator());
 		GenPackage primaryPackage = findGenPackage(mapping.getDomainModel());
 		getGenEssence().setDomainGenModel(primaryPackage == null ? null : primaryPackage.getGenModel());
 		getGenDiagram().setDomainDiagramElement(findGenClass(mapping.getDomainMetaElement()));
 		getGenDiagram().setDiagramRunTimeClass(findRunTimeClass(mapping));
 		getGenDiagram().setVisualID(myVisualIDs.get(getGenDiagram()));
 		getGenDiagram().setViewmap(myViewmaps.create(mapping.getDiagramCanvas()));
 		getGenDiagram().setIconProviderPriority(ProviderPriority.LOW_LITERAL); // override ElementTypeIconProvider
 		if (getGenDiagram().getDomainDiagramElement() != null) {
 			// since diagram is the first entity to process consider it defines metamodel type
 			getGenDiagram().setElementType(GMFGenFactory.eINSTANCE.createMetamodelType());
 			myProcessedTypes.put(getGenDiagram().getDomainDiagramElement(), getGenDiagram().getElementType());
 		} else {
 			getGenDiagram().setElementType(GMFGenFactory.eINSTANCE.createNotationType());
 		}
 		
 		initGenPlugin();
 
 		myPropertySheetProcessor.initialize(createPropertySheet());
 		myPropertySheetProcessor.process(mapping);
 
 		// set class names
 		myNamingStrategy.feed(getGenDiagram(), mapping);
 	}
 
 	protected void process(TopNodeReference topNode) {
 		final NodeMapping nme = topNode.getChild();
 		assert nme != null;
 		assertNodeMapping(nme);
 		
 		GenTopLevelNode genNode = GMFGenFactory.eINSTANCE.createGenTopLevelNode();
 		getGenDiagram().getTopLevelNodes().add(genNode);
 		genNode.setDiagramRunTimeClass(findRunTimeClass(nme));
 		genNode.setModelFacet(createModelFacet(topNode));
 		genNode.setVisualID(myVisualIDs.get(genNode));
 		genNode.setViewmap(myViewmaps.create(nme.getDiagramNode()));
 		setupElementType(genNode); 
 		myPaletteProcessor.process(nme, genNode);
 
 		// set class names
 		myNamingStrategy.feed(genNode, nme);
 		
 		processAbstractNode(nme, genNode);
 		myHistory.log(nme, genNode);
 		
 		myNavigatorProcessor.process(genNode);
 	}
 	
 	protected void process(AuditContainer audits) {
 		if(audits != null) {
 			getGenEssence().setAudits(createGenAuditContainer(audits));	
 		}
 	}	
 	
 	protected void process(MetricContainer metrics) {
 		if(metrics != null) {
 			GenMetricContainer genMetricContainer = GMFGenFactory.eINSTANCE.createGenMetricContainer();
 			for (Iterator it = metrics.getMetrics().iterator(); it.hasNext();) {
 				genMetricContainer.getMetrics().add(createGenMetric((MetricRule)it.next()));				
 			}
 			getGenEssence().setMetrics(genMetricContainer);
 		}
 	}
 
 	private void process(ChildReference childNodeRef, GenChildContainer container) {
 		final NodeMapping childNodeMapping = childNodeRef.getChild();
 		assert childNodeMapping != null;
 		assertNodeMapping(childNodeMapping);
 
 		GenChildNode childNode;
 		if (!myHistory.isKnownChildNode(childNodeMapping)) {
 			childNode = createGenChildNode(childNodeRef);
 		} else {
 			GenChildNode[] alreadyKnownChildren = myHistory.findChildNodes(childNodeMapping);
 
 			childNode = null;
 			for (int i = 0; i < alreadyKnownChildren.length; i++) {
 				if (matchChildReferenceFeatures(childNodeRef, alreadyKnownChildren[i])) {
 					childNode = alreadyKnownChildren[i];
 					break;
 				}
 			}
 			if (childNode == null) { // no match
 				childNode = createGenChildNode(childNodeRef);
 			}
 		}
 		if (container instanceof GenCompartment && childNodeMapping.getChildren().size() > 0) {
 			// TODO just layout from childNodeMapping.getDiagramNode()
 			((GenCompartment)container).setListLayout(false);
 		}
 		container.getChildNodes().add(childNode);
 		myNavigatorProcessor.process(childNode, container);
 	}
 
 	/**
 	 * Handle case when second-level ChildReference references existing nodemapping, but 
 	 * with different containment/children reference. 
 	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=129552
 	 */
 	private static boolean matchChildReferenceFeatures(ChildReference childNodeRef, GenChildNode childNode) {
 		final boolean containmentFeatureMatch;
 		final boolean childrenFeatureMatch;
 		if (childNode.getModelFacet() == null || childNode.getModelFacet().getContainmentMetaFeature() == null) {
 			containmentFeatureMatch = (null == childNodeRef.getContainmentFeature());
 		} else {
 			// seems legal to use '==' because features should came from the same model
 			containmentFeatureMatch = childNodeRef.getContainmentFeature() == childNode.getModelFacet().getContainmentMetaFeature().getEcoreFeature();
 		}
 		if (childNode.getModelFacet() == null || childNode.getModelFacet().getChildMetaFeature() == null) {
 			childrenFeatureMatch = (null == childNodeRef.getChildrenFeature());
 		} else {
 			if (childNodeRef.getChildrenFeature() == null) {
 				// likely, childMetaFeature in model facet was derived from containment feature 
 				childrenFeatureMatch = childNode.getModelFacet().getChildMetaFeature() == childNode.getModelFacet().getContainmentMetaFeature();
 			} else {
 				// honest check
 				childrenFeatureMatch = childNode.getModelFacet().getChildMetaFeature().getEcoreFeature() == childNodeRef.getChildrenFeature();
 			}
 		}
 		return containmentFeatureMatch && childrenFeatureMatch;
 	}
 
 	private GenChildNode createGenChildNode(ChildReference childNodeRef) {
 		final NodeMapping childNodeMapping = childNodeRef.getChild();
 		final GenChildNode childNode;
 		final boolean needCompartmentChildrenLabelProcessing;
 		if (isPureLabelNode(childNodeMapping)) {
 			LabelMapping soleLabel = (LabelMapping) childNodeMapping.getLabelMappings().get(0);
 			GenChildLabelNode childLabelNode = GMFGenFactory.eINSTANCE.createGenChildLabelNode();
 			childLabelNode.setViewmap(myViewmaps.create(soleLabel.getDiagramLabel()));
 			childLabelNode.setLabelModelFacet(createLabelModelFacet(soleLabel));
 			childLabelNode.setLabelReadOnly(soleLabel.isReadOnly());
 			childLabelNode.setLabelElementIcon(soleLabel.getDiagramLabel().isElementIcon());
 			childNode = childLabelNode;
 			needCompartmentChildrenLabelProcessing = false;
 		} else if (childNodeMapping.getDiagramNode().getAffixedParentSide() != Direction.NONE_LITERAL){
 			GenChildSideAffixedNode sideAffixedNode = GMFGenFactory.eINSTANCE.createGenChildSideAffixedNode(); 
 			sideAffixedNode.setViewmap(myViewmaps.create(childNodeMapping.getDiagramNode()));
 			String positionConstantName = getAffixedSideAsPositionConstantsName(childNodeMapping.getDiagramNode());
 			sideAffixedNode.setPreferredSideName(positionConstantName);
 			childNode = sideAffixedNode;
 			needCompartmentChildrenLabelProcessing = true;
 		} else {
 			childNode = GMFGenFactory.eINSTANCE.createGenChildNode();
 			childNode.setViewmap(myViewmaps.create(childNodeMapping.getDiagramNode()));
 			needCompartmentChildrenLabelProcessing = true;
 		}
 		myHistory.log(childNodeMapping, childNode);
 		getGenDiagram().getChildNodes().add(childNode);
 
 		childNode.setModelFacet(createModelFacet(childNodeRef));
 		
 		childNode.setDiagramRunTimeClass(findRunTimeClass(childNodeMapping));
 		childNode.setVisualID(myVisualIDs.get(childNode));
 		setupElementType(childNode); 
 
 		// set class names
 		myNamingStrategy.feed(childNode, childNodeMapping);
 
 		myPaletteProcessor.process(childNodeMapping, childNode);
 		if (needCompartmentChildrenLabelProcessing) {
 			processAbstractNode(childNodeMapping, childNode);
 		}
 		return childNode;
 	}
 	
 	private String getAffixedSideAsPositionConstantsName(Node diagramNode) {
 		Direction affixedSide = diagramNode.getAffixedParentSide(); 
 		final String ANY_SIDE = "NONE"; 
 		switch (affixedSide.getValue()){
 			case Direction.NONE : 
 				throw new IllegalStateException("DiagramNode: " + diagramNode + " is not side-affixed");
 			case Direction.EAST :
 			case Direction.NORTH :
 			case Direction.WEST :
 			case Direction.SOUTH :
 				return affixedSide.getName();
 			
 			case Direction.NSEW:
 				return ANY_SIDE;
 			
 			default:
 				//Runtime does not support this
 				return ANY_SIDE;
 		}
 	}
 	
 	/**
 	 * @return whether nodeMapping has single label, no children and node's diagram 
 	 * element is DiagramLabel equivalent that of it's label
 	 */
 	private boolean isPureLabelNode(NodeMapping childNodeMapping) {
 		if (childNodeMapping.getLabelMappings().size() == 1 && childNodeMapping.getChildren().isEmpty()) {
 			LabelMapping soleLabel = (LabelMapping) childNodeMapping.getLabelMappings().get(0);
 			return childNodeMapping.getDiagramNode() == soleLabel.getDiagramLabel(); 
 		}
 		return false;
 	}
 
 	private void processAbstractNode(NodeMapping mapping, GenNode genNode) {
 		Map compartments2GenCompartmentsMap = new HashMap();
 		for (Iterator it = mapping.getCompartments().iterator(); it.hasNext();) {
 			CompartmentMapping compartmentMapping = (CompartmentMapping) it.next();
 			GenCompartment compartmentGen = createGenCompartment(compartmentMapping, genNode);
 			compartments2GenCompartmentsMap.put(compartmentMapping, compartmentGen);
 		}
 
 		for (Iterator it = mapping.getChildren().iterator(); it.hasNext();) {
 			ChildReference childNodeRef = (ChildReference) it.next();
 // Currently childNodeMapping should has compartment but we plan to make this reference optional
 			CompartmentMapping compartmentMapping = childNodeRef.getCompartment();
 			GenChildContainer genChildContainer;
 			if (compartmentMapping != null && compartments2GenCompartmentsMap.containsKey(compartmentMapping)) {
 				genChildContainer = (GenChildContainer) compartments2GenCompartmentsMap.get(compartmentMapping);
 			} else {
 				genChildContainer = genNode;
 			}
 			process(childNodeRef, genChildContainer);
 		}
 		for (Iterator labels = mapping.getLabelMappings().iterator(); labels.hasNext();) {
 			LabelMapping labelMapping = (LabelMapping) labels.next();
 			GenNodeLabel label = createNodeLabel(genNode, labelMapping);
 
 			// set class names
 			myNamingStrategy.feed(label, labelMapping);
 		}
 		for (Iterator it = mapping.getRelatedDiagrams().iterator(); it.hasNext(); ) {
 			CanvasMapping nextRelatedCanvas = (CanvasMapping) it.next();
 			OpenDiagramBehaviour openDiagramPolicy = GMFGenFactory.eINSTANCE.createOpenDiagramBehaviour();
 			// ugly check that nodeMapping is related to owning canvasMapping, iow mapping.getCanvasMapping() == nextRelatedCanvas
 			if (nextRelatedCanvas.eResource() != mapping.eResource()) {
 				// unless we would like to ask user where to take appropriate .gmfgen...
 				openDiagramPolicy.setDiagramKind("put GenEditorGenerator.modelID value here");
 				openDiagramPolicy.setEditorID("put GenEditorView.id value here");
 			}
 			genNode.getBehaviour().add(openDiagramPolicy);
 		}
 	}
 
 	private GenCompartment createGenCompartment(CompartmentMapping mapping, GenNode genNode) {
 		Compartment compartment = mapping.getCompartment(); 
 		assert compartment != null;
 		GenCompartment childCompartment = GMFGenFactory.eINSTANCE.createGenCompartment();
 		getGenDiagram().getCompartments().add(childCompartment);
 		genNode.getCompartments().add(childCompartment);
 		childCompartment.setVisualID(myVisualIDs.get(childCompartment));
 		childCompartment.setDiagramRunTimeClass(getChildContainerRunTimeClass());
 		childCompartment.setViewmap(myViewmaps.create(mapping.getCompartment()));
 		childCompartment.setCanCollapse(compartment.isCollapsible());
 		childCompartment.setNeedsTitle(compartment.isNeedsTitle());
 		childCompartment.setTitle(compartment.getName());
 
 		// set class names
 		myNamingStrategy.feed(childCompartment, mapping);
 		return childCompartment;
 	}
 
 	protected void process(LinkMapping lme) {
 		assertLinkMapping(lme);
 		GenLink gl = GMFGenFactory.eINSTANCE.createGenLink();
 		getGenDiagram().getLinks().add(gl);
 		gl.setModelFacet(createModelFacet(lme));
 		gl.setVisualID(myVisualIDs.get(gl));
 		myPaletteProcessor.process(lme, gl);
 		for (Iterator labels = lme.getLabelMappings().iterator(); labels.hasNext();) {
 			LabelMapping labelMapping = (LabelMapping) labels.next();
 			GenLinkLabel label = createLinkLabel(gl, labelMapping);
 
 			// set class names
 			myNamingStrategy.feed(label, labelMapping);
 		}
 		gl.setDiagramRunTimeClass(findRunTimeClass(lme));
 
 		setupElementType(gl);
 
 		// set class names
 		myNamingStrategy.feed(gl, lme);
 
 		gl.setViewmap(myViewmaps.create(lme.getDiagramLink()));
 
 		if(lme.getCreationConstraints() != null) {
 			gl.setCreationConstraints(createLinkCreationConstraints(lme.getCreationConstraints()));
 		}
 		
 		myHistory.log(lme, gl);
 		myNavigatorProcessor.process(gl);
 	}
 
 //	private void process(AppearanceSteward appSteward) {
 //		if (appSteward.getAppearanceStyle() == null) {
 //			return;
 //		}
 //		
 //	}
 
 	private GenNodeLabel createNodeLabel(GenNode node, LabelMapping mapping) {
 		GenNodeLabel label;
 		if (Knowledge.isExternal(mapping.getDiagramLabel())) {
 			label = GMFGenFactory.eINSTANCE.createGenExternalNodeLabel();
 		} else {
 			label = GMFGenFactory.eINSTANCE.createGenNodeLabel();
 		}
 		node.getLabels().add(label);
 		label.setVisualID(myVisualIDs.get(label));
 		label.setDiagramRunTimeClass(findRunTimeClass(mapping));
 		label.setViewmap(myViewmaps.create(mapping.getDiagramLabel()));
 		label.setModelFacet(createLabelModelFacet(mapping));
 		label.setReadOnly(mapping.isReadOnly());
 		label.setElementIcon(mapping.getDiagramLabel().isElementIcon());
 		return label;
 	}
 
 	private GenLinkLabel createLinkLabel(GenLink link, LabelMapping mapping) {
 		GenLinkLabel label = GMFGenFactory.eINSTANCE.createGenLinkLabel();
 		link.getLabels().add(label);
 		label.setVisualID(myVisualIDs.get(label));
 		label.setDiagramRunTimeClass(findRunTimeClass(mapping));
 		label.setViewmap(myViewmaps.create(mapping.getDiagramLabel()));
 		label.setModelFacet(createLabelModelFacet(mapping));
 		label.setReadOnly(mapping.isReadOnly());
 		label.setElementIcon(mapping.getDiagramLabel().isElementIcon());
 		if (mapping.getDiagramLabel().find(AlignmentFacet.class) != null) {
 			AlignmentFacet af = (AlignmentFacet) mapping.getDiagramLabel().find(AlignmentFacet.class);
 			label.setAlignment(getLinkLabelAlignment(af.getAlignment()));
 		}
 		LabelOffsetAttributes loa = GMFGenFactory.eINSTANCE.createLabelOffsetAttributes();
 		LabelOffsetFacet lof = (LabelOffsetFacet) mapping.getDiagramLabel().find(LabelOffsetFacet.class);
 		if (lof != null) {
 			loa.setX(lof.getX());
 			loa.setY(lof.getY());
 		} else {
 			// stack labels under link by default
 			int weight = link.getLabels().size() + 1;
 			loa.setY(weight * 20);
 		}
 		label.getViewmap().getAttributes().add(loa);
 		return label;
 	}
 
 	private LinkLabelAlignment getLinkLabelAlignment(Alignment alignment) {
 		switch (alignment.getValue()) {
 		case Alignment.BEGINNING: return LinkLabelAlignment.SOURCE_LITERAL;
 		case Alignment.CENTER: return LinkLabelAlignment.MIDDLE_LITERAL;
 		case Alignment.END: return LinkLabelAlignment.TARGET_LITERAL;
 		default: throw new IllegalArgumentException("Link doesn't support alignment:" + alignment.getName());
 		}
 	}
 
 	private LabelModelFacet createLabelModelFacet(LabelMapping mapping) {
 		if (mapping.getFeatures().size() > 0) {
 			FeatureLabelModelFacet modelFacet = GMFGenFactory.eINSTANCE.createFeatureLabelModelFacet();
 			for (Iterator features = mapping.getFeatures().iterator(); features.hasNext();) {
 				modelFacet.getMetaFeatures().add(findGenFeature((EAttribute) features.next()));
 			}
 			modelFacet.setViewPattern(mapping.getViewPattern());
 			modelFacet.setEditPattern(mapping.getEditPattern());
 			return modelFacet;
 		}
 		if (!mapping.isReadOnly()) {
 			// use design model facet if there are no features and label is editable
 			//return GMFGenFactory.eINSTANCE.createDesignLabelModelFacet();
 		}
 		return null;
 	}
 
 	private void setupElementType(GenNode genNode) {
 		if (genNode.getModelFacet() != null) {
 			MetamodelType metamodelType = (MetamodelType) myProcessedTypes.get(genNode.getModelFacet().getMetaClass());
 			if (metamodelType == null) {
 				// this is the first metaclass encounter; consider metamodel type definition
 				genNode.setElementType(GMFGenFactory.eINSTANCE.createMetamodelType());
 				myProcessedTypes.put(genNode.getModelFacet().getMetaClass(), genNode.getElementType());
 			} else {
 				// all subsequent encounters lead to specialization definitions
 				SpecializationType specializationType = GMFGenFactory.eINSTANCE.createSpecializationType();
 				specializationType.setMetamodelType(metamodelType);
 				genNode.setElementType(specializationType);
 			}
 		} else {
 			genNode.setElementType(GMFGenFactory.eINSTANCE.createNotationType());
 		}
 	}
 
 	private void setupElementType(GenLink gl) {
 		if (gl.getModelFacet() != null) {
 			if (gl.getModelFacet() instanceof TypeModelFacet) {
 				GenClass metaClass = ((TypeModelFacet) gl.getModelFacet()).getMetaClass();
 				MetamodelType metamodelType = (MetamodelType) myProcessedTypes.get(metaClass);
 				if (metamodelType == null) {
 					// this is the first metaclass encounter; consider metamodel type definition
 					gl.setElementType(GMFGenFactory.eINSTANCE.createMetamodelType());
 					myProcessedTypes.put(metaClass, gl.getElementType());
 				} else {
 					// all subsequent encounters lead to specialization definitions
 					SpecializationType specializationType = GMFGenFactory.eINSTANCE.createSpecializationType();
 					specializationType.setMetamodelType(metamodelType);
 					gl.setElementType(specializationType);
 				}
 			} else {
 				// ref-based link; specialize null
 				SpecializationType specializationType = GMFGenFactory.eINSTANCE.createSpecializationType();
 				gl.setElementType(specializationType);
 			}
 		} else {
 			gl.setElementType(GMFGenFactory.eINSTANCE.createNotationType());
 		}
 	}
 
 	private GenClass findRunTimeClass(NodeMapping nme) {
 		return myDRTHelper.get(nme);
 	}
 
 	private GenClass findRunTimeClass(LinkMapping lme) {
 		return myDRTHelper.get(lme);
 	}
 
 	private GenClass findRunTimeClass(CanvasMapping mapping) {
 		return myDRTHelper.get(mapping);
 	}
 
 	private GenClass getChildContainerRunTimeClass() {
 		return myDRTHelper.getChildContainerDefault();
 	}
 
 	private GenClass findRunTimeClass(LabelMapping mapping) {
 		return myDRTHelper.get(mapping);
 	}
 
 
 	private void assertNodeMapping(NodeMapping mapping) {
 		assert mapping.getDiagramNode() != null;
 		assert checkLabelMappings(mapping);
 	}
 
 	private void assertLinkMapping(LinkMapping linkMapping) {
 		assert linkMapping.getDiagramLink() != null;
 		if (linkMapping.getDomainMetaElement() != null) {
 			assert linkMapping.getLinkMetaFeature() != null;
 		}
 		assert checkLabelMappings(linkMapping);
 	}
 
 	private static boolean checkLabelMappings(MappingEntry entry) {
 		boolean ok = true;
 		for (Iterator it = entry.getLabelMappings().iterator(); ok && it.hasNext();) {
 			ok = checkLabelFeatureValidity((LabelMapping) it.next());
 		}
 		return ok;
 	}
 
 	private static boolean checkLabelFeatureValidity(LabelMapping labelMapping) {
 		final EClass domainElement = labelMapping.getMapEntry().getDomainContext(); 
 		boolean isOk = true;
 		for (Iterator it = labelMapping.getFeatures().iterator(); isOk && it.hasNext(); ) {
 			EClass attrContainer = ((EAttribute) it.next()).getEContainingClass();
 			isOk = attrContainer.isSuperTypeOf(domainElement);
 		}
 		return isOk;
 	}
 
 	private GenPackage findGenPackage(EPackage ePackage) {
 		if (myGenModelMatch == null) {
 			warnNoGenModelMatcher(ePackage);
 			return null;
 		}
 		return myGenModelMatch.findGenPackage(ePackage);
 	}
 
 	private GenClass findGenClass(EClass eClass) {
 		if (myGenModelMatch == null) {
 			warnNoGenModelMatcher(eClass);
 			return null;
 		}
 		return myGenModelMatch.findGenClass(eClass);
 	}
 
 	private GenFeature findGenFeature(EStructuralFeature feature) {
 		if (myGenModelMatch == null) {
 			warnNoGenModelMatcher(feature);
 			return null;
 		}
 		return myGenModelMatch.findGenFeature(feature);
 	}
 	
 	private void warnNoGenModelMatcher(EModelElement element) {
 		// TODO : emit warning
 	}
 	
 	private TypeModelFacet createModelFacet(NodeReference anm) {
 		final NodeMapping nodeMapping = anm.getChild();
 		if (nodeMapping.getDomainContext() == null) {
 			return null;
 		}
 		TypeModelFacet typeModelFacet = setupModelFacet(nodeMapping.getDomainContext(), anm.getContainmentFeature(), anm.getChildrenFeature());
 		return setupAux(typeModelFacet, nodeMapping.getDomainSpecialization(), nodeMapping.getDomainInitializer());
 	}
 
 	private LinkModelFacet createModelFacet(LinkMapping lme) {
 		if (lme.getDomainMetaElement() != null) {
 			TypeLinkModelFacet mf = GMFGenFactory.eINSTANCE.createTypeLinkModelFacet();
 			mf.setMetaClass(findGenClass(lme.getDomainMetaElement()));
 			mf.setContainmentMetaFeature(findGenFeature(lme.getContainmentFeature()));
 			mf.setChildMetaFeature(mf.getContainmentMetaFeature());
 			mf.setSourceMetaFeature(findGenFeature(lme.getSourceMetaFeature()));
 			mf.setTargetMetaFeature(findGenFeature(lme.getLinkMetaFeature()));
 			setupAux(mf, lme.getDomainSpecialization(), lme.getDomainInitializer());
 			return mf;
 		} else if (lme.getLinkMetaFeature() != null) {
 			FeatureLinkModelFacet mf = GMFGenFactory.eINSTANCE.createFeatureLinkModelFacet();
 			mf.setMetaFeature(findGenFeature(lme.getLinkMetaFeature()));
 			return mf;
 		}
 		return null; // notation link
 	}
 
 	private GenLinkConstraints createLinkCreationConstraints(LinkConstraints constraints) {
 		LinkMapping lme = constraints.getLinkMapping();
 		if(lme == null) {
 			return null;
 		}				
 		GenLinkConstraints genConstraints = GMFGenFactory.eINSTANCE.createGenLinkConstraints();
 		Constraint sourceConstraint = constraints.getSourceEnd();
 		if(sourceConstraint != null) {
 			genConstraints.setSourceEnd(createGenConstraint(sourceConstraint));
 		}
 		Constraint targetConstraint = constraints.getTargetEnd();
 		if(targetConstraint != null) {
 			genConstraints.setTargetEnd(createGenConstraint(targetConstraint));
 		}		
 		return genConstraints; 
 	}
 
 	private TypeModelFacet setupModelFacet(EClass domainMetaElement, EStructuralFeature containmentFeature, EStructuralFeature childFeature) {
 		TypeModelFacet mf = GMFGenFactory.eINSTANCE.createTypeModelFacet();
 		mf.setMetaClass(findGenClass(domainMetaElement));
 		mf.setContainmentMetaFeature(findGenFeature(containmentFeature));
 		mf.setChildMetaFeature(childFeature == null ? mf.getContainmentMetaFeature() : findGenFeature(childFeature));
 		return mf;
 	}
 
 	/**
 	 * @return typeModelFacet argument for convenience
 	 */
 	private TypeModelFacet setupAux(TypeModelFacet typeModelFacet, Constraint spec, ElementInitializer init) {
 		// construct model element selector for domain EClass specializations if any exist
 		if(spec != null) {
 			typeModelFacet.setModelElementSelector(createGenConstraint(spec));
 		}
 		if(init != null) {
 			typeModelFacet.setModelElementInitializer(createElementInitializer(init));			
 		}
 		return typeModelFacet;
 	}
 
 	private GenElementInitializer createElementInitializer(ElementInitializer elementInitializer) {
 		if(elementInitializer instanceof FeatureSeqInitializer) {
 			FeatureSeqInitializer fsInitializer = (FeatureSeqInitializer) elementInitializer;
 			GenFeatureSeqInitializer genFsInitializer = GMFGenFactory.eINSTANCE.createGenFeatureSeqInitializer();
 			for (Iterator it = fsInitializer.getInitializers().iterator(); it.hasNext();) {
 				genFsInitializer.getInitializers().add(createGenFeatureInitializer((FeatureInitializer)it.next()));
 			}
 			if(fsInitializer.eIsSet(GMFMapPackage.eINSTANCE.getFeatureSeqInitializer_ElementClass())) {
 				genFsInitializer.setElementClass(findGenClass(fsInitializer.getElementClass()));
 			}			
 			return genFsInitializer;
 		}
 		return null;
 	}
 	
 	private GenFeatureInitializer createGenFeatureInitializer(FeatureInitializer featureInitializer) {
 		if (featureInitializer instanceof FeatureValueSpec) {
 			FeatureValueSpec featureValSpec = (FeatureValueSpec) featureInitializer;				
 			GenFeatureValueSpec genFeatureValSpec = GMFGenFactory.eINSTANCE.createGenFeatureValueSpec();				
 			genFeatureValSpec.setBody(featureValSpec.getBody());
 			genFeatureValSpec.setLanguage(createGenLanguage(featureValSpec.getLanguage()));
 			genFeatureValSpec.setFeature(findGenFeature(featureValSpec.getFeature()));
 			
 			bindToProvider(featureValSpec, genFeatureValSpec);
 			return genFeatureValSpec;
 		} else if (featureInitializer instanceof ReferenceNewElementSpec) {
 			ReferenceNewElementSpec newElementSpec = (ReferenceNewElementSpec) featureInitializer;
 			GenReferenceNewElementSpec genNewElementSpec = GMFGenFactory.eINSTANCE.createGenReferenceNewElementSpec();
 			genNewElementSpec.setFeature(findGenFeature(newElementSpec.getFeature()));
 			for (Iterator newElemInitIt = newElementSpec.getNewElementInitializers().iterator(); newElemInitIt.hasNext();) { 
 				GenFeatureSeqInitializer nextGenFeatureSeqInitializer = (GenFeatureSeqInitializer)createElementInitializer((FeatureSeqInitializer)newElemInitIt.next());
 				genNewElementSpec.getNewElementInitializers().add(nextGenFeatureSeqInitializer);
 			}
 			return genNewElementSpec;
 		}
 		assert false : "Unrecognized FeatureInitializer type"; //$NON-NLS-1$
 		return null;
 	}
 
 	private static GenLanguage createGenLanguage(Language mapLang) {
 		switch (mapLang.getValue()) {
 		case Language.OCL:
 			return GenLanguage.OCL_LITERAL;
 		case Language.JAVA:
 			return GenLanguage.JAVA_LITERAL;
 		case Language.REGEXP:
 			return GenLanguage.REGEXP_LITERAL;
 		case Language.NREGEXP:
 			return GenLanguage.NREGEXP_LITERAL;
 		default:
 			assert false : mapLang;
 		}
 		return GenLanguage.OCL_LITERAL;
 	}
 	
 	private GenConstraint createGenConstraint(Constraint constraint) {
 		if(constraint.getBody() == null) {
 			return null;
 		}
 		GenConstraint modelElementSelector = GMFGenFactory.eINSTANCE.createGenConstraint();
 		modelElementSelector.setBody(constraint.getBody());
 		modelElementSelector.setLanguage(createGenLanguage(constraint.getLanguage()));
 		bindToProvider(constraint, modelElementSelector);
 		return modelElementSelector;
 	}
 	
 	private GenAuditContainer createGenAuditContainer(AuditContainer ac) {
 		GenAuditContainer gac = GMFGenFactory.eINSTANCE.createGenAuditContainer();
 		gac.setId(ac.getId());
 		gac.setName(ac.getName());
 		gac.setDescription(ac.getDescription());
 		for(Iterator it = ac.getChildContainers().iterator(); it.hasNext();) {
 			AuditContainer nextChild = (AuditContainer) it.next();
 			gac.getChildContainers().add(createGenAuditContainer(nextChild));
 		}
 		for (Iterator it = ac.getAudits().iterator(); it.hasNext();) {
 			gac.getAudits().add(createGenAudit((AuditRule) it.next()));
 		}
 		return gac;
 	}
 	
 	private GenAuditRule createGenAudit(AuditRule audit) {
 		GenAuditRule genAudit = GMFGenFactory.eINSTANCE.createGenAuditRule();
 		genAudit.setId(audit.getId());
 		genAudit.setName(audit.getName());
 		genAudit.setMessage(audit.getMessage());
 		genAudit.setDescription(audit.getDescription());
 		genAudit.setUseInLiveMode(audit.isUseInLiveMode());
 		
 		if(audit.getTarget() != null) {
 			GenRuleTarget genTarget = createRuleTarget(audit.getTarget());
 			assert genTarget instanceof GenAuditable;
 			if(genTarget instanceof GenAuditable) {
 				genAudit.setTarget((GenAuditable)genTarget);
 			}
 		}
 		Constraint rule = audit.getRule();
 		if(rule != null) {
 			genAudit.setRule(createGenConstraint(rule));
 		}
 
 		Severity severity = audit.getSeverity();
 		GenSeverity genSeverity = null;
 		if(severity == Severity.INFO_LITERAL) {
 			genSeverity = GenSeverity.INFO_LITERAL;
 		} else if(severity == Severity.WARNING_LITERAL) {
 			genSeverity = GenSeverity.WARNING_LITERAL;
 		} else if(severity == Severity.ERROR_LITERAL) {
 			genSeverity = GenSeverity.ERROR_LITERAL;
 		}
 		if(genSeverity != null) {
 			genAudit.setSeverity(genSeverity);
 		}
 		return genAudit;
 	} 
 	
 	private GenRuleTarget createRuleTarget(EObject ruleTarget) {		
 		if (ruleTarget instanceof DomainElementTarget) {
 			DomainElementTarget domainTarget = (DomainElementTarget)ruleTarget;
 			GenDomainElementTarget genDomainTarget = GMFGenFactory.eINSTANCE.createGenDomainElementTarget();
 			genDomainTarget.setElement(domainTarget.getElement() != null ? findGenClass(domainTarget.getElement()) : null);
 			return genDomainTarget;
 		} else if (ruleTarget instanceof NotationElementTarget) {
 			NotationElementTarget notationTarget = (NotationElementTarget) ruleTarget;
 			GenNotationElementTarget genNotationTarget = GMFGenFactory.eINSTANCE.createGenNotationElementTarget();
 			genNotationTarget.setElement(notationTarget.getElement() != null ? findGenClass(notationTarget.getElement()) : null);
 			return genNotationTarget;
 
 		} else if (ruleTarget instanceof DiagramElementTarget) {
 			GenDiagramElementTarget diagramTarget = GMFGenFactory.eINSTANCE.createGenDiagramElementTarget();
 			MappingEntry mappingEntry = ((DiagramElementTarget) ruleTarget).getElement();
 			if (mappingEntry != null) {
 				LinkMapping lm = mappingEntry instanceof LinkMapping ? (LinkMapping) mappingEntry : null;
 				GenCommonBase genBase = null;				
 				if (lm != null) {
 					genBase = myHistory.find(lm);
 					assert genBase != null;
 					if(genBase != null) {
 						diagramTarget.getElement().add(genBase);
 					}
 				} else {
 					NodeMapping nm = mappingEntry instanceof NodeMapping ? (NodeMapping) mappingEntry : null;
 					// There may be few GenChildNodes corresponding to same mapping entry.					
 					// @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=136701					
 					genBase = myHistory.findTopNode(nm);
 					if(genBase != null) {
 						diagramTarget.getElement().add(genBase);
 					}
 					diagramTarget.getElement().addAll(Arrays.asList(myHistory.findChildNodes(nm)));					
 				}				
 			}
 			return diagramTarget;
 		} else if(ruleTarget instanceof AuditedMetricTarget) {			
 			GenAuditedMetricTarget genMetricTarget = GMFGenFactory.eINSTANCE.createGenAuditedMetricTarget();
 			AuditedMetricTarget metricTarget = (AuditedMetricTarget)ruleTarget;
 			if(metricTarget.getMetric() != null) {
 				genMetricTarget.setMetric(myHistory.find(metricTarget.getMetric()));
 			}
 			GenClassifier resultClassifier = myEcoreGenModelMatch.findGenClassifier(EcorePackage.eINSTANCE.getEDoubleObject());
 			assert resultClassifier instanceof GenDataType;
 			if(resultClassifier instanceof GenDataType) {
 				genMetricTarget.setMetricValueContext((GenDataType)resultClassifier);
 			}
 			return genMetricTarget;
 		} else if(ruleTarget instanceof DomainAttributeTarget) {
 			DomainAttributeTarget attrTarget = (DomainAttributeTarget) ruleTarget;
 			GenDomainAttributeTarget genAttrTarget = GMFGenFactory.eINSTANCE.createGenDomainAttributeTarget();
 			if(attrTarget.getAttribute() != null) {
 				genAttrTarget.setAttribute(findGenFeature(attrTarget.getAttribute()));
 			}
 			genAttrTarget.setNullAsError(attrTarget.isNullAsError());
 			return genAttrTarget;				
 		} else {
 			assert false : "Uknown rule target type"; //$NON-NLS-1$
 		}
 		return null;
 	}
 	
 	private GenMetricRule createGenMetric(MetricRule metric) {
 		GenMetricRule genMetric = GMFGenFactory.eINSTANCE.createGenMetricRule();
 		genMetric.setKey(metric.getKey());
 		genMetric.setName(metric.getName());
 		genMetric.setDescription(metric.getDescription());
 		genMetric.setLowLimit(metric.getLowLimit());
 		genMetric.setHighLimit(metric.getHighLimit());
 		
 		if(metric.getRule() != null) {
 			ValueExpression valueExpression = GMFGenFactory.eINSTANCE.createValueExpression();
 			valueExpression.setBody(metric.getRule().getBody());
 			valueExpression.setLanguage(createGenLanguage(metric.getRule().getLanguage()));
 			bindToProvider(metric.getRule(), valueExpression);
 			genMetric.setRule(valueExpression);
 		}
 		
		if(metric.getTarget() != null) {		
			GenRuleTarget genTarget = createRuleTarget(metric.getTarget());
			assert genTarget instanceof GenMeasurable;
			if(genTarget instanceof GenMeasurable) {
				genMetric.setTarget((GenMeasurable)genTarget);
			}
 		}
 		myHistory.log(metric, genMetric);
 		return genMetric;
 	}
 	
 	private void bindToProvider(org.eclipse.gmf.mappings.ValueExpression expression, ValueExpression genExpression) {
 		if(!myProcessedExpressions.add(expression)) {
 			// Note: may have already been bound during transformation of reused node mapping
 			return;
 		}
 		
 		GenLanguage language = genExpression.getLanguage();
 		if(language == null) {
 			return;
 		}
 		GenExpressionProviderContainer providerContainer = getGenEssence().getExpressionProviders();
 		if(providerContainer == null) {
 			providerContainer = GMFGenFactory.eINSTANCE.createGenExpressionProviderContainer();
 			getGenEssence().setExpressionProviders(providerContainer);
 		}
 		GenExpressionProviderBase provider = null;
 		for (Iterator it = providerContainer.getProviders().iterator(); it.hasNext();) {
 			GenExpressionProviderBase nextProvider = (GenExpressionProviderBase) it.next();	
 			if(language.equals(nextProvider.getLanguage())) {
 				provider = nextProvider;
 				break;
 			}
 		}
 		if(provider == null) {
 			provider = createExpressionProvider(language);
 			if(provider == null) {
 				return;
 			}
 			providerContainer.getProviders().add(provider);			
 		}
 		provider.getExpressions().add(genExpression);
 	}
 	
 	private GenExpressionProviderBase createExpressionProvider(GenLanguage language) {
 		GenExpressionProviderBase newProvider = null;
 		if(GenLanguage.JAVA_LITERAL.equals(language)) {
 			newProvider = GMFGenFactory.eINSTANCE.createGenJavaExpressionProvider();			
 		} else if(GenLanguage.OCL_LITERAL.equals(language)) {
 			GenExpressionInterpreter oclProvider = GMFGenFactory.eINSTANCE.createGenExpressionInterpreter();
 			oclProvider.setLanguage(language);
 			oclProvider.getRequiredPluginIDs().addAll(Arrays.asList(new String[] {
 				"org.eclipse.emf.ocl", //$NON-NLS-1$
 				"org.eclipse.emf.query.ocl" //$NON-NLS-1$		
 			}));
 			newProvider = oclProvider;
 		} else if(GenLanguage.REGEXP_LITERAL.equals(language) || GenLanguage.NREGEXP_LITERAL.equals(language)) {
 			GenExpressionInterpreter regexpProvider = GMFGenFactory.eINSTANCE.createGenExpressionInterpreter();
 			regexpProvider.setLanguage(language);
 			newProvider = regexpProvider;
 		}
 		return newProvider;
 	}
 }
