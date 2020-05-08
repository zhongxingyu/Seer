 /******************************************************************************* 
  * Copyright (c) 2011, 2012 Red Hat, Inc. 
  *  All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  *
  * @author Ivar Meikas
  ******************************************************************************/
 package org.eclipse.bpmn2.modeler.core.di;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.eclipse.bpmn2.Artifact;
 import org.eclipse.bpmn2.Association;
 import org.eclipse.bpmn2.BaseElement;
 import org.eclipse.bpmn2.BoundaryEvent;
 import org.eclipse.bpmn2.ChoreographyActivity;
 import org.eclipse.bpmn2.Collaboration;
 import org.eclipse.bpmn2.ConversationLink;
 import org.eclipse.bpmn2.ConversationNode;
 import org.eclipse.bpmn2.DataAssociation;
 import org.eclipse.bpmn2.DataInput;
 import org.eclipse.bpmn2.DataObject;
 import org.eclipse.bpmn2.DataObjectReference;
 import org.eclipse.bpmn2.DataOutput;
 import org.eclipse.bpmn2.DataStore;
 import org.eclipse.bpmn2.DataStoreReference;
 import org.eclipse.bpmn2.Definitions;
 import org.eclipse.bpmn2.Event;
 import org.eclipse.bpmn2.FlowElement;
 import org.eclipse.bpmn2.FlowElementsContainer;
 import org.eclipse.bpmn2.FlowNode;
 import org.eclipse.bpmn2.ItemAwareElement;
 import org.eclipse.bpmn2.Lane;
 import org.eclipse.bpmn2.LaneSet;
 import org.eclipse.bpmn2.MessageFlow;
 import org.eclipse.bpmn2.Participant;
 import org.eclipse.bpmn2.Process;
 import org.eclipse.bpmn2.RootElement;
 import org.eclipse.bpmn2.SequenceFlow;
 import org.eclipse.bpmn2.SubChoreography;
 import org.eclipse.bpmn2.SubProcess;
 import org.eclipse.bpmn2.di.BPMNDiagram;
 import org.eclipse.bpmn2.di.BPMNEdge;
 import org.eclipse.bpmn2.di.BPMNPlane;
 import org.eclipse.bpmn2.di.BPMNShape;
 import org.eclipse.bpmn2.di.BpmnDiFactory;
 import org.eclipse.bpmn2.modeler.core.Activator;
 import org.eclipse.bpmn2.modeler.core.ModelHandler;
 import org.eclipse.bpmn2.modeler.core.preferences.Bpmn2Preferences;
 import org.eclipse.bpmn2.modeler.core.preferences.ShapeStyle;
 import org.eclipse.bpmn2.modeler.core.utils.AnchorUtil;
 import org.eclipse.bpmn2.modeler.core.utils.BusinessObjectUtil;
 import org.eclipse.bpmn2.modeler.core.utils.FeatureSupport;
 import org.eclipse.bpmn2.modeler.core.utils.GraphicsUtil;
 import org.eclipse.bpmn2.modeler.core.utils.ShapeLayoutManager;
 import org.eclipse.bpmn2.modeler.core.utils.GraphicsUtil.Size;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.bpmn2.modeler.core.utils.Tuple;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dd.dc.Bounds;
 import org.eclipse.dd.dc.DcFactory;
 import org.eclipse.dd.dc.Point;
 import org.eclipse.dd.di.DiagramElement;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.graphiti.datatypes.ILocation;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.ILayoutFeature;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.features.context.impl.AreaContext;
 import org.eclipse.graphiti.features.context.impl.LayoutContext;
 import org.eclipse.graphiti.features.context.impl.UpdateContext;
 import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.FixPointAnchor;
 import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.mm.pictograms.impl.FreeFormConnectionImpl;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeService;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.PartInitException;
 
 @SuppressWarnings("restriction")
 public class DIImport {
 
 	public static final String IMPORT_PROPERTY = DIImport.class.getSimpleName().concat(".import");
 
 	private DiagramEditor editor;
 	private Diagram diagram;
 	private TransactionalEditingDomain domain;
 	private ModelHandler modelHandler;
 	private IFeatureProvider featureProvider;
 	private HashMap<BaseElement, PictogramElement> elements;
 	private Bpmn2Preferences preferences;
 	private final IPeService peService = Graphiti.getPeService();
 	private final IGaService gaService = Graphiti.getGaService();
 	
 	public static class DiagramElementTreeNode {
 		private static List<DiagramElementTreeNode> EMPTY = new ArrayList<DiagramElementTreeNode>();
 		private DiagramElementTreeNode parent;
 		private BaseElement baseElement;
 		private List<DiagramElementTreeNode> children;
 		private boolean checked = true;
 		
 		public DiagramElementTreeNode(DiagramElementTreeNode parent, BaseElement element) {
 			this.parent = parent;
 			this.baseElement = element;
 		}
 		
 		public BaseElement getBaseElement() {
 			return baseElement;
 		}
 		
 		public void setBaseElement(BaseElement baseElement) {
 			this.baseElement = baseElement;
 		}
 		
 		public DiagramElementTreeNode getParent() {
 			return parent;
 		}
 		
 		public boolean getChecked() {
 			return checked;
 		}
 		
 		private void setParentChecked(boolean checked) {
 			if (parent!=null) {
 				if (!checked) {
 					// grayed?
 					if (parent.hasChildren()) {
 						for (DiagramElementTreeNode child : parent.children) {
 							if (child.getChecked()) {
 								checked = true;
 								break;
 							}
 						}
 					}
 					parent.checked = checked;
 				}
 				else
 					parent.checked = true;
 				parent.setParentChecked(checked);
 			}
 		}
 		
 		public void setChecked(boolean checked) {
 			this.checked = checked;
 			if (hasChildren()) {
 				for (DiagramElementTreeNode child : children) {
 					child.setChecked(checked);
 				}
 			}
 			setParentChecked(checked);
 		}
 		
 		public DiagramElementTreeNode addChild(BaseElement element) {
 			assert( element.eContainer() == baseElement );
 			DiagramElementTreeNode child = getChild(element);
 			if (child!=null)
 				return child;
 			
 			if (children==null)
 				children = new ArrayList<DiagramElementTreeNode>();
 			DiagramElementTreeNode newElement = new DiagramElementTreeNode(this, element);
 			children.add(newElement);
 			return newElement;
 		}
 		
 		public DiagramElementTreeNode getChild(BaseElement element) {
 			if (hasChildren()) {
 				for (DiagramElementTreeNode child : children) {
 					if (child.getBaseElement() == element) {
 						return child;
 					}
 				}
 			}
 			return null;
 		}
 		
 		public void removeChild(BaseElement element) {
 			if (hasChildren()) {
 				for (DiagramElementTreeNode child : children) {
 					if (child.getBaseElement() == element) {
 						children.remove(child);
 						break;
 					}
 				}
 			}
 		}
 		
 		public boolean hasChildren() {
 			return children!=null && children.size()>0;
 		}
 		
 		public List<DiagramElementTreeNode> getChildren() {
 			if (hasChildren())
 				return children;
 			return EMPTY;
 		}
 	}
 	
 	public static class DiagramElementTree extends DiagramElementTreeNode implements ILabelProvider, ITreeContentProvider {
 
 		public DiagramElementTree(DiagramElementTreeNode parent, BaseElement element) {
 			super(parent, element);
 		}
 		
 		@Override
 		public void addListener(ILabelProviderListener listener) {
 		}
 
 		@Override
 		public void dispose() {
 		}
 
 		@Override
 		public boolean isLabelProperty(Object element, String property) {
 			return false;
 		}
 
 		@Override
 		public void removeListener(ILabelProviderListener listener) {
 		}
 
 		@Override
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		}
 
 		@Override
 		public Object[] getElements(Object inputElement) {
 			if (inputElement instanceof List) {
 				return ((List)inputElement).toArray();
 			}
 			return getChildren().toArray();
 		}
 
 		@Override
 		public Object[] getChildren(Object parentElement) {
 			if (parentElement instanceof DiagramElementTreeNode) {
 				return ((DiagramElementTreeNode)parentElement).getChildren().toArray();
 			}
 			return null;
 		}
 
 		@Override
 		public Object getParent(Object element) {
 			if (element instanceof DiagramElementTreeNode) {
 				return ((DiagramElementTreeNode)element).getParent();
 			}
 			return null;
 		}
 
 		@Override
 		public boolean hasChildren(Object element) {
 			if (element instanceof DiagramElementTreeNode) {
 				return ((DiagramElementTreeNode)element).hasChildren();
 			}
 			return super.hasChildren();
 		}
 
 		@Override
 		public Image getImage(Object element) {
 			return null;
 		}
 
 		@Override
 		public String getText(Object element) {
 			String text = "Unknown BPMN Element";
 			if (element instanceof DiagramElementTreeNode) {
 				BaseElement be = ((DiagramElementTreeNode)element).getBaseElement();
 				text = be.eClass().getName() + ": " + ModelUtil.getDisplayName(be);
 			}
 			return text;
 		}
 		
 	}
 	
 	public DIImport(DiagramEditor editor) {
 		this.editor = editor;
 		domain = editor.getEditingDomain();
 		featureProvider = editor.getDiagramTypeProvider().getFeatureProvider();
 	}
 	
 	/**
 	 * Look for model diagram interchange information and generate all shapes for the diagrams.
 	 * 
 	 * NB! Currently only first found diagram is generated.
 	 */
 	public void generateFromDI() {
 		final List<BPMNDiagram> bpmnDiagrams = modelHandler.getAll(BPMNDiagram.class);
 		
 		elements = new HashMap<BaseElement, PictogramElement>();
 		domain.getCommandStack().execute(new RecordingCommand(domain) {
 			@Override
 			protected void doExecute() {
 
 				diagram = editor.getDiagramTypeProvider().getDiagram();
 				
 				if (bpmnDiagrams.size() == 0) {
 					BPMNPlane plane = BpmnDiFactory.eINSTANCE.createBPMNPlane();
 					plane.setBpmnElement(modelHandler.getOrCreateProcess(modelHandler.getInternalParticipant()));
 
 					BPMNDiagram d = BpmnDiFactory.eINSTANCE.createBPMNDiagram();
 					d.setPlane(plane);
 
 					modelHandler.getDefinitions().getDiagrams().add(d);
 					
 					// don't forget to add the new Diagram to our list for processing
 					bpmnDiagrams.add(d);
 				}
 				featureProvider.link(diagram, bpmnDiagrams.get(0));
 				
 				// First: add all IDs to our ID mapping table
 				Definitions definitions = modelHandler.getDefinitions();
 				TreeIterator<EObject> iter = definitions.eAllContents();
 				while (iter.hasNext()) {
 					ModelUtil.addID( iter.next() );
 				}
 				
 				// do the import
 				for (BPMNDiagram d : bpmnDiagrams) {
 					if (preferences==null)
 						preferences = Bpmn2Preferences.getInstance(d);
 					
 					diagram = DIUtils.getOrCreateDiagram(editor,d);
 					editor.getDiagramTypeProvider().init(diagram, editor);
 
 					BPMNPlane plane = d.getPlane();
 					if (plane.getBpmnElement() == null) {
 						plane.setBpmnElement(modelHandler.getOrCreateProcess(modelHandler.getInternalParticipant()));
 					}
 					List<DiagramElement> ownedElement = plane.getPlaneElement();
 
 					importShapes(ownedElement);
 					importConnections(ownedElement);
 
 //					relayoutLanes(ownedElement);
 
 					// search for BPMN elements that do not have the DI elements
 					// needed to render them in the editor
 					DiagramElementTree missing = findMissingDIElements();
 					// Display a dialog here of the missing elements and allow user
 					// to choose which ones to create
 					if (missing.hasChildren()) {
 						MissingDIElementsDialog dlg = new MissingDIElementsDialog(missing);
 						if (dlg.open()==Window.OK) {
 							createMissingDIElements(missing);
 							
 							for (DiagramElementTreeNode node : missing.getChildren()) {
 								if (node.getChecked()) {
 									ShapeLayoutManager layoutManager = new ShapeLayoutManager(editor);
 									layoutManager.layout(node.getBaseElement());
 								}
 							}
 						}
 					}
 				}
 				
 				layoutAll();
 			}
 
 		});
 	}
 	
 	public DiagramElementTree findMissingDIElements() {
 		
 		DiagramElementTree missing = new DiagramElementTree(null,null);
 		Definitions definitions = modelHandler.getDefinitions();
 		
 		// look for any BPMN2 elements that do not have corresponding DI elements
 		for (BaseElement be : definitions.getRootElements()) {
 			findMissingDIElements(missing, be);
 		}
 		return missing;
 	}
 	
 	private int findMissingDIElements(DiagramElementTreeNode missing, LaneSet laneSet, List<FlowElement> laneElements) {
 		int added = 0;
 		if (laneSet!=null) {
 			for (Lane lane : laneSet.getLanes()) {
 				// create the missing tree node for this Lane's container
 				// this is either a FlowElementsContainer or another Lane
 				BaseElement container = (BaseElement) lane.eContainer().eContainer();
 				DiagramElementTreeNode containerNode = missing.getChild(container);
 				if (containerNode==null)
 					containerNode = missing.addChild(container);
 				DiagramElementTreeNode parentNode = containerNode.addChild(lane);
 				
 				for (FlowNode fn : lane.getFlowNodeRefs()) {
 					if (elements.get(fn)==null) {
 						parentNode.addChild(fn);
 						laneElements.add(fn);
 						++added;
 					}
 				}
 				added += findMissingDIElements(parentNode, lane.getChildLaneSet(), laneElements);
 				
 				if (added==0) {
					missing.removeChild(parentNode.getBaseElement());
					containerNode.removeChild(container);
 				}
 			}
 		}
 		return added;
 	}
 	
 	private void findMissingDIElements(DiagramElementTreeNode missing, BaseElement be) {
 		if (be instanceof FlowElementsContainer) {
 			FlowElementsContainer container = (FlowElementsContainer)be;
 			DiagramElementTreeNode parentNode = null;
 			
 			List<FlowElement> laneElements = new ArrayList<FlowElement>();
 			for (LaneSet laneSet : container.getLaneSets()) {
 				findMissingDIElements(missing, laneSet, laneElements);
 			}
 			
 			for (FlowElement fe : container.getFlowElements()) {
 				if (elements.get(fe) == null && !laneElements.contains(fe)) {
 					if (!(fe instanceof SequenceFlow)) {
 						if (parentNode==null)
 							parentNode = missing.addChild(container);
 						parentNode.addChild(fe);
 						if (fe instanceof FlowElementsContainer) {
 							findMissingDIElements(parentNode, fe);
 						}
 					}
 				}
 			}
 			List<Artifact> artifacts = getArtifacts(container);
 			if (artifacts!=null) {
 				for (Artifact a : artifacts) {
 					if (elements.get(a) == null && !(a instanceof Association)) {
 						if (parentNode==null)
 							parentNode = missing.addChild(container);
 						parentNode.addChild(a);
 					}
 				}
 			}
 		}
 		else if (be instanceof Collaboration) {
 			Collaboration container = (Collaboration)be;
 			DiagramElementTreeNode parentNode = null;
 			for (Artifact a : container.getArtifacts()) {
 				if (elements.get(a) == null && !(a instanceof Association)) {
 					if (parentNode==null)
 						parentNode = missing.addChild(container);
 					parentNode.addChild(a);
 				}
 			}
 			for (Participant p : container.getParticipants()) {
 				if (elements.get(p) == null) {
 					if (parentNode==null)
 						parentNode = missing.addChild(container);
 					parentNode.addChild(p);
 				}
 			}
 			for (ConversationNode c : container.getConversations()) {
 				if (elements.get(c) == null) {
 					if (parentNode==null)
 						parentNode = missing.addChild(container);
 					parentNode.addChild(c);
 				}
 			}
 		}
 		else if (be instanceof DataStore) {
 			if (elements.get(be) == null) {
 				missing.addChild(be);
 			}
 		}
 	}
 	
 	private List<Artifact> getArtifacts(BaseElement container) {
 		if (container instanceof Process) {
 			return ((Process)container).getArtifacts();
 		}
 		if (container instanceof SubProcess) {
 			return ((SubProcess)container).getArtifacts();
 		}
 		if (container instanceof SubChoreography) {
 			return ((SubChoreography)container).getArtifacts();
 		}
 		if (container instanceof Collaboration) {
 			return ((Collaboration)container).getArtifacts();
 		}
 		return null;
 	}
 
 	private FlowElementsContainer getRootElementContainer(EObject o) {
 		while (o!=null) {
 			if (o instanceof FlowElementsContainer && o instanceof RootElement) {
 				return (FlowElementsContainer)o;
 			}
 			o = o.eContainer();
 		}
 		return null;
 	}
 	
 	private BPMNShape createMissingDIElement(DiagramElementTreeNode node, int x, int y, List<BaseElement> created) {
 		BaseElement element = node.getBaseElement();
 		BPMNShape bpmnShape = null;
 		BPMNDiagram bpmnDiagram = createDIDiagram(element);
 		
 		if (element instanceof Lane) {
 			Lane lane = (Lane)element;
 			bpmnShape = createDIShape(bpmnDiagram, lane, x, y);
 
 			for (DiagramElementTreeNode childNode : node.getChildren()) {
 				if (childNode.getChecked()) {
 					BPMNShape shape = createMissingDIElement(childNode, x, y, created);
 					if (shape!=null) {
 						y += shape.getBounds().getHeight() + 10;
 					}
 				}
 			}
 			created.add(lane);
 		}
 		else if (element instanceof FlowElementsContainer) {
 			FlowElementsContainer container = (FlowElementsContainer)element;
 
 			for (DiagramElementTreeNode childNode : node.getChildren()) {
 				if (childNode.getChecked()) {
 					BPMNShape shape = createMissingDIElement(childNode, x, y, created);
 					if (shape!=null) {
 						y += shape.getBounds().getHeight() + 10;
 					}
 				}
 			}
 			
 			if (!(container instanceof RootElement)) {
 				// This can only be either a SubChoreography or SubProcess.
 				created.add(container);
 			}			
 		}
 		else if (element instanceof Collaboration) {
 			for (DiagramElementTreeNode childNode : node.getChildren()) {
 				if (childNode.getChecked()) {
 					BPMNShape shape = createMissingDIElement(childNode, x, y, created);
 					if (shape!=null) {
 						y += shape.getBounds().getHeight() + 10;
 					}
 				}
 			}
 		}
 		else if (element instanceof Participant) {
 			Participant participant = (Participant)element;
 			bpmnShape = createDIShape(bpmnDiagram, element, x, y);
 			created.add(element);
 		}
 		else if (element instanceof ConversationNode) {
 			bpmnShape = createDIShape(bpmnDiagram, element, x, y);
 			created.add(element);
 		}
 		else if (element instanceof FlowNode) {
 			bpmnShape = createDIShape(bpmnDiagram, element, x, y);
 			created.add(element);
 		}
 		else if (element instanceof DataObject) {
 			bpmnShape = createDIShape(bpmnDiagram, element, x, y);
 			created.add(element);
 		}
 		else if (element instanceof DataObjectReference) {
 			bpmnShape = createDIShape(bpmnDiagram, element, x, y);
 			created.add(element);
 		}
 		else if (element instanceof DataStore) {
 			bpmnShape = createDIShape(bpmnDiagram, element, x, y);
 			created.add(element);
 		}
 		else if (element instanceof DataStoreReference) {
 			bpmnShape = createDIShape(bpmnDiagram, element, x, y);
 			created.add(element);
 		}
 		return bpmnShape;
 	}
 	
 	public void createMissingDIElements(DiagramElementTree missing) {
 
 		// look for any BPMN2 elements that do not have corresponding DI elements
 		// and create DI elements for them. First, handle the BPMNShape objects:
 		int x = 102400;
 		int y = 0;
 		List<BaseElement> shapes = new ArrayList<BaseElement>();
 		for (DiagramElementTreeNode node : missing.getChildren()) {
 			if (node.getChecked()) {
 				BPMNShape shape = createMissingDIElement(node, x, y, shapes);
 				if (shape!=null) {
 					y += shape.getBounds().getHeight() + 10;
 				}
 			}
 		}
 		
 		// Next create the BPMNEdge objects. At this point, all of the source
 		// and target elements for the connections should already exist, so
 		// we don't have to worry about that.
 		List<BaseElement> connections = new ArrayList<BaseElement>();
 		for (BaseElement be : shapes) {
 			if (be instanceof FlowNode) {
 				FlowNode flowNode = (FlowNode)be;
 				// find the BPMNDiagram that contains this flow node
 				BPMNDiagram bpmnDiagram = createDIDiagram(flowNode);
 
 				for (SequenceFlow sf : flowNode.getIncoming()) {
 					if (!connections.contains(sf)) {
 						BPMNEdge bpmnEdge = createDIEdge(bpmnDiagram, sf);
 						if (bpmnEdge!=null)
 							connections.add(sf);
 					}
 				}
 
 				for (SequenceFlow sf : flowNode.getOutgoing()) {
 					if (!connections.contains(sf)) {
 						BPMNEdge bpmnEdge = createDIEdge(bpmnDiagram, sf);
 						if (bpmnEdge!=null)
 							connections.add(sf);
 					}
 				}
 			}
 			else if (be instanceof ConversationNode) {
 				ConversationNode convNode = (ConversationNode)be;
 				BPMNDiagram bpmnDiagram = createDIDiagram(convNode);
 				for (MessageFlow mf : convNode.getMessageFlowRefs()) {
 					if (!connections.contains(mf)) {
 						BPMNEdge bpmnEdge = createDIEdge(bpmnDiagram, mf);
 						if (bpmnEdge!=null)
 							connections.add(mf);
 					}
 				}
 			}
 		}
 		// Finally, Associations are RootElements and since we only include shapes
 		// in the missing elements tree, we'll have to revisit all of the RootElements
 		Definitions definitions = modelHandler.getDefinitions();
 		TreeIterator<EObject> iter = definitions.eAllContents();
 		while (iter.hasNext()) {
 			EObject o = iter.next();
 			if (o instanceof Association) {
 				Association assoc = (Association)o;
 				BPMNDiagram bpmnDiagram = createDIDiagram(assoc);
 				BPMNEdge bpmnEdge = createDIEdge(bpmnDiagram, assoc);
 				if (bpmnEdge!=null)
 					connections.add(assoc);
 			}
 		}
 	}
 	
 	protected BPMNDiagram createDIDiagram(BaseElement bpmnElement) {
 		FlowElementsContainer container = getRootElementContainer(bpmnElement);
 		Definitions definitions = modelHandler.getDefinitions();
 		BPMNDiagram bpmnDiagram = null;
 		for (BPMNDiagram d : definitions.getDiagrams()) {
 			BPMNPlane plane = d.getPlane();
 			if (plane.getBpmnElement() == container) {
 				bpmnDiagram = d;
 				break;
 			}
 		}
 		
 		// if this container does not have a BPMNDiagram, create one
 		if (bpmnDiagram==null) {
 			BPMNPlane plane = BpmnDiFactory.eINSTANCE.createBPMNPlane();
 			plane.setBpmnElement(container);
 
 			bpmnDiagram = BpmnDiFactory.eINSTANCE.createBPMNDiagram();
 			bpmnDiagram.setName(container.getId());
 			bpmnDiagram.setPlane(plane);
 
 			definitions.getDiagrams().add(bpmnDiagram);
 		}
 
 		return bpmnDiagram;
 	}
 	
 	protected BPMNShape createDIShape(BPMNDiagram bpmnDiagram, BaseElement bpmnElement, float x, float y) {
 		
 		BPMNPlane plane = bpmnDiagram.getPlane();
 		BPMNShape bpmnShape = null;
 		for (DiagramElement de : plane.getPlaneElement()) {
 			if (de instanceof BPMNShape) {
 				if (bpmnElement == ((BPMNShape)de).getBpmnElement()) {
 					bpmnShape = (BPMNShape)de;
 					break;
 				}
 			}
 		}
 		
 		if (bpmnShape==null) {
 			bpmnShape = BpmnDiFactory.eINSTANCE.createBPMNShape();
 			bpmnShape.setBpmnElement(bpmnElement);
 			Bounds bounds = DcFactory.eINSTANCE.createBounds();
 			bounds.setX(x);
 			bounds.setY(y);
 			Size size = GraphicsUtil.getShapeSize(bpmnElement, diagram);
 			bounds.setWidth(size.getWidth());
 			bounds.setHeight(size.getHeight());
 			bpmnShape.setBounds(bounds);
 			plane.getPlaneElement().add(bpmnShape);
 			Bpmn2Preferences.getInstance(bpmnDiagram.eResource()).applyBPMNDIDefaults(bpmnShape, null);
 
 			ModelUtil.setID(bpmnShape);
 			importShape(bpmnShape);
 		}
 		
 		return bpmnShape;
 	}
 	
 	protected BPMNEdge createDIEdge(BPMNDiagram bpmnDiagram, BaseElement bpmnElement) {
 		BPMNPlane plane = bpmnDiagram.getPlane();
 		BPMNEdge bpmnEdge = null;
 		for (DiagramElement de : plane.getPlaneElement()) {
 			if (de instanceof BPMNEdge) {
 				if (bpmnElement == ((BPMNEdge)de).getBpmnElement()) {
 					bpmnEdge = (BPMNEdge)de;
 					break;
 				}
 			}
 		}
 
 		if (bpmnEdge==null) {
 			bpmnEdge = BpmnDiFactory.eINSTANCE.createBPMNEdge();
 			bpmnEdge.setBpmnElement(bpmnElement);
 	
 			BaseElement sourceElement = null;
 			BaseElement targetElement = null;
 			if (bpmnElement instanceof SequenceFlow) {
 				sourceElement = ((SequenceFlow)bpmnElement).getSourceRef();
 				targetElement = ((SequenceFlow)bpmnElement).getTargetRef();
 			}
 			else if (bpmnElement instanceof MessageFlow) {
 				sourceElement = (BaseElement) ((MessageFlow)bpmnElement).getSourceRef();
 				targetElement = (BaseElement) ((MessageFlow)bpmnElement).getTargetRef();
 			}
 			else if (bpmnElement instanceof Association) {
 				sourceElement = ((Association)bpmnElement).getSourceRef();
 				targetElement = ((Association)bpmnElement).getTargetRef();
 			}
 			
 			if (sourceElement!=null && targetElement!=null) {
 				DiagramElement de;
 				de = DIUtils.findPlaneElement(plane.getPlaneElement(), sourceElement);
 				bpmnEdge.setSourceElement(de);
 				
 				de = DIUtils.findPlaneElement(plane.getPlaneElement(), targetElement);
 				bpmnEdge.setTargetElement(de);
 				
 				// the source and target elements should already have been created:
 				// we know the PictogramElements for these can be found in our elements map
 				Shape sourceShape = (Shape)elements.get(sourceElement);
 				Shape targetShape = (Shape)elements.get(targetElement);
 				if (sourceShape!=null && targetShape!=null) {
 					Tuple<FixPointAnchor,FixPointAnchor> anchors =
 							AnchorUtil.getSourceAndTargetBoundaryAnchors(sourceShape, targetShape, null);
 					org.eclipse.graphiti.mm.algorithms.styles.Point sourceLoc = GraphicsUtil.createPoint(anchors.getFirst());
 					org.eclipse.graphiti.mm.algorithms.styles.Point targetLoc = GraphicsUtil.createPoint(anchors.getSecond());
 					Point point = DcFactory.eINSTANCE.createPoint();
 					point.setX(sourceLoc.getX());
 					point.setY(sourceLoc.getY());
 					bpmnEdge.getWaypoint().add(point);
 			
 					point = DcFactory.eINSTANCE.createPoint();
 					point.setX(targetLoc.getX());
 					point.setY(targetLoc.getY());
 					bpmnEdge.getWaypoint().add(point);
 					
 					plane.getPlaneElement().add(bpmnEdge);
 					
 					ModelUtil.setID(bpmnEdge);
 					importConnection(bpmnEdge);
 				}
 			}
 		}
 		
 		return bpmnEdge;
 	}
 	
 	private void layoutAll() {
 		final List<BPMNDiagram> diagrams = modelHandler.getAll(BPMNDiagram.class);
 //		for (BPMNDiagram d : diagrams) {
 //			BPMNPlane plane = d.getPlane();
 //			for (DiagramElement de : plane.getPlaneElement()) {
 //				if (de instanceof BPMNShape) {
 //					BaseElement be = ((BPMNShape) de).getBpmnElement();
 //					PictogramElement pe = elements.get(be);
 //					if (pe instanceof Shape ) {
 //						Graphiti.getPeService().sendToFront((Shape)pe);
 //					}
 //				}
 //			}
 //		}
 
 		for (BaseElement be : elements.keySet()) {
 			PictogramElement pe = elements.get(be);
 
 			if (be instanceof SubProcess) { // we need the layout to hide children if collapsed
 				LayoutContext context = new LayoutContext(pe);
 				ILayoutFeature feature = featureProvider.getLayoutFeature(context);
 				if (feature==null) {
 					continue;
 				}
 				if (feature.canLayout(context))
 					feature.layout(context);
 			}
 			else if (be instanceof FlowNode) {
 				LayoutContext context = new LayoutContext(pe);
 				ILayoutFeature feature = featureProvider.getLayoutFeature(context);
 				if (feature!=null && feature.canLayout(context))
 					feature.layout(context);
 			}
 
 			if (pe instanceof Connection) {
 				UpdateContext context = new UpdateContext(pe);
 				IUpdateFeature feature = featureProvider.getUpdateFeature(context);
 				if (feature.updateNeeded(context).toBoolean()) {
 					feature.update(context);
 				}
 			}
 		}
 	}
 
 	public void setModelHandler(ModelHandler modelHandler) {
 		this.modelHandler = modelHandler;
 	}
 	
 	private void importShape(BPMNShape bpmnShape) {
 		if (!elements.containsKey(bpmnShape.getBpmnElement())) {
 			List<DiagramElement> newElements = new ArrayList<DiagramElement>();
 			newElements.add(bpmnShape);
 			importShapes(newElements);
 		}
 	}
 	
 	/**
 	 * Imports shapes from DI. Since we don't know the order of shapes in DI,
 	 * we may get an inner element like a boundary element before its parent.
 	 * Therefore we use a queue to postpone the import of such elements, and
 	 * prevent the layouting from crashing.
 	 * 
 	 * @param ownedElement
 	 */
 	private void importShapes(List<DiagramElement> ownedElement) {
 		Queue<BPMNShape> shapeQueue = new ConcurrentLinkedQueue<BPMNShape>();
 		
 		// Enqueue shapes
 		for (DiagramElement diagramElement : ownedElement) {
 			if (diagramElement instanceof BPMNShape) {
 				BPMNShape diShape = (BPMNShape) diagramElement;
 				if (diShape.getBpmnElement() != null) {
 					shapeQueue.offer(diShape);
 				}
 			}
 		}
 		
 		// Process Queue
 		// First pass tries to find the missing BPMNShape container
 		// Second pass synthesizes missing containers 
 		int queueLength = shapeQueue.size();
 		for (int pass=0; pass<=1; ++pass) {
 			int requeueCount = 0;
 			while (!shapeQueue.isEmpty() && requeueCount < queueLength) {
 				BPMNShape currentShape = shapeQueue.remove();
 				BaseElement bpmnElement = currentShape.getBpmnElement();
 				boolean postpone = false;
 
 				if (bpmnElement instanceof BoundaryEvent
 						&& !elements.containsKey(((BoundaryEvent) bpmnElement).getAttachedToRef())) {
 					postpone = true;
 				} else if (bpmnElement instanceof FlowNode) {
 	
 					EObject container = bpmnElement.eContainer();
 					if ((container instanceof SubProcess || container instanceof SubChoreography)
 							&& !elements.containsKey(container)) {
 						postpone = true;
 					} else if (!((FlowNode) bpmnElement).getLanes().isEmpty()) {
 						List<Lane> lanes = ((FlowNode) bpmnElement).getLanes();
 						if (pass==0) {
 							for (Lane lane : lanes) {
 								if (!elements.containsKey(lane)) {
 									postpone = true;
 									break;
 								}
 							}
 						}
 						else {
 							// synthesize missing Lane shapes
 							for (Lane lane : lanes) {
 								synthesizeLane(lane);
 							}
 						}
 					}
 				}
 	
 				if (postpone) {
 					// post-pone
 					shapeQueue.offer(currentShape);
 					++requeueCount;
 				} else {
 					createShape(currentShape);
 					requeueCount = 0;
 				}
 			}
 		}
 		
 		if (shapeQueue.size()!=0) {
 			String elementList = "";
 			for (Iterator<BPMNShape> iterator = shapeQueue.iterator(); iterator.hasNext();) {
 				BPMNShape currentShape = iterator.next();
 				BaseElement bpmnElement = currentShape.getBpmnElement();
 				if (bpmnElement!=null) {
 					String id = bpmnElement.getId();
 					if (id!=null) {
 						elementList += bpmnElement.eClass().getName() + " " + id + "\n";
 					}
 				}
 				
 			}			
 			Activator.logStatus(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Unsatisfied BPMN elements: "
 					+ elementList));
 		}
 	}
 
 	private void synthesizeLane(Lane lane) {
 		if (!elements.containsKey(lane)) {
 			List<BPMNDiagram> diagrams = modelHandler.getAll(BPMNDiagram.class);
 			// this is a new one
 			int xMin = Integer.MAX_VALUE;
 			int yMin = Integer.MAX_VALUE;
 			int width = 0;
 			int height = 0;
 			for (FlowNode flowNode : lane.getFlowNodeRefs()) {
 				BPMNShape flowNodeBPMNShape = (BPMNShape)DIUtils.findDiagramElement(diagrams,flowNode);
 				if (flowNodeBPMNShape!=null) {
 					// adjust bounds of Lane
 					Bounds bounds = flowNodeBPMNShape.getBounds();
 					int x = (int)bounds.getX();
 					int y = (int)bounds.getY();
 					int w = (int)bounds.getWidth();
 					int h = (int)bounds.getHeight();
 					if (x<xMin)
 						xMin = x;
 					if (y<yMin) 
 						yMin = y;
 					if (xMin+width < x + w)
 						width = x - xMin + w;
 					if (yMin+height < y + h)
 						height = y - yMin + h;
 				}
 			}
 			if (width>0 && height>0) {
 				// create a new BPMNShape for this Lane
 				AddContext context = new AddContext(new AreaContext(), lane);
 				context.setX(xMin-10);
 				context.setY(yMin-10);
 				context.setWidth(width+20);
 				context.setHeight(height+20);
 				context.putProperty(IMPORT_PROPERTY, true);
 				// determine the container into which to place the new Lane
 				handleLane(lane, context, null);
 				IAddFeature addFeature = featureProvider.getAddFeature(context);
 				ContainerShape newContainer = (ContainerShape)addFeature.add(context);
 				newContainer.getGraphicsAlgorithm().setTransparency(0.5);
 				Graphiti.getPeService().sendToBack(newContainer);
 				
 				elements.put(lane, newContainer);
 			}									
 		}
 	}
 	
 	private void importConnection(BPMNEdge bpmnEdge) {
 		if (!elements.containsKey(bpmnEdge.getBpmnElement())) {
 			List<DiagramElement> newElements = new ArrayList<DiagramElement>();
 			newElements.add(bpmnEdge);
 			importConnections(newElements);
 		}
 	}
 	
 	private void importConnections(List<DiagramElement> ownedElement) {
 		for (DiagramElement diagramElement : ownedElement) {
 			if (diagramElement instanceof BPMNEdge) {
 				createEdge((BPMNEdge) diagramElement);
 			}
 		}
 	}
 
 	private void relayoutLanes(List<DiagramElement> ownedElement) {
 		for (DiagramElement diagramElement : ownedElement) {
 			if (diagramElement instanceof BPMNShape && ((BPMNShape) diagramElement).getBpmnElement() instanceof Lane) {
 				BaseElement lane = ((BPMNShape) diagramElement).getBpmnElement();
 				ContainerShape shape = (ContainerShape) BusinessObjectUtil.getFirstBaseElementFromDiagram(diagram, lane);
 				FeatureSupport.redraw(shape);
 			}
 		}
 	}
 
 	/**
 	 * Find a Graphiti feature for given shape and generate necessary diagram elements.
 	 * 
 	 * @param shape
 	 */
 	private void createShape(BPMNShape shape) {
 		BaseElement bpmnElement = shape.getBpmnElement();
 		if (shape.getChoreographyActivityShape() != null) {
 			// FIXME: we currently generate participant bands automatically
 			return;
 		}
 		AddContext context = new AddContext(new AreaContext(), bpmnElement);
 		IAddFeature addFeature = featureProvider.getAddFeature(context);
 
 		if (addFeature == null) {
 			Activator.logStatus(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Element not supported: "
 					+ bpmnElement.eClass().getName()));
 			return;
 		}
 
 		context.putProperty(IMPORT_PROPERTY, true);
 		context.setNewObject(bpmnElement);
 		boolean defaultSize = false;
 		ShapeStyle ss = preferences.getShapeStyle(bpmnElement);
 		if (ss!=null)
 			defaultSize = ss.isDefaultSize();
 		
 		if (defaultSize) {
 			Size size = GraphicsUtil.getShapeSize(bpmnElement,diagram);
 			if (size!=null)
 				context.setSize(size.getWidth(),size.getHeight());
 			else
 				defaultSize = false;
 		}
 		
 		if (!defaultSize) {
 			context.setSize((int) shape.getBounds().getWidth(), (int) shape.getBounds().getHeight());
 		}
 
 		if ( (bpmnElement instanceof SubProcess) && !shape.isIsExpanded()) {
 			context.setSize(GraphicsUtil.getActivitySize(diagram).getWidth(), GraphicsUtil.getActivitySize(diagram).getHeight());
 		}
 
 		if (bpmnElement instanceof Lane) {
 			handleLane((Lane)bpmnElement, context, shape);
 		} else if (bpmnElement instanceof FlowNode ||
 				bpmnElement instanceof DataObject ||
 				bpmnElement instanceof DataObjectReference) {
 			handleFlowElement((FlowElement) bpmnElement, context, shape);
 		} else if (bpmnElement instanceof Participant) {
 			handleParticipant((Participant) bpmnElement, context, shape);
 		} else if (bpmnElement instanceof DataInput || bpmnElement instanceof DataOutput) {
 			handleItemAwareElement((ItemAwareElement)bpmnElement, context, shape);
 		} else {
 			context.setTargetContainer(diagram);
 			context.setLocation((int) shape.getBounds().getX(), (int) shape.getBounds().getY());
 		}
 
 		if (addFeature.canAdd(context)) {
 			PictogramElement newContainer = addFeature.add(context);
 			featureProvider.link(newContainer, new Object[] { bpmnElement, shape });
 			if (bpmnElement instanceof Participant) {
 				// If the Participant ("Pool") references a Process, add it to our list of elements;
 				// its ContainerShape is the same as the Participant's.
 				Process process = ((Participant) bpmnElement).getProcessRef();
 				if (process!=null)
 					elements.put(process, newContainer);
 			}
 			else if (bpmnElement instanceof ChoreographyActivity) {
 				ChoreographyActivity ca = (ChoreographyActivity)bpmnElement;
 				for (PictogramElement pe : ((ContainerShape)newContainer).getChildren()) {
 					Object o = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);
 					if (o instanceof Participant)
 						elements.put((Participant)o, pe);
 				}
 			}
 //			else if (bpmnElement instanceof Event) {
 //				GraphicsUtil.setEventSize(context.getWidth(), context.getHeight(), diagram);
 //			} else if (bpmnElement instanceof Gateway) {
 //				GraphicsUtil.setGatewaySize(context.getWidth(), context.getHeight(), diagram);
 //			} else if (bpmnElement instanceof Activity && !(bpmnElement instanceof SubProcess)) {
 //				GraphicsUtil.setActivitySize(context.getWidth(), context.getHeight(), diagram);
 //			}
 			
 			elements.put(bpmnElement, newContainer);
 			handleEvents(bpmnElement, newContainer);
 		}
 		
 		ModelUtil.addID(bpmnElement);
 	}
 
 	private void handleEvents(BaseElement bpmnElement, PictogramElement newContainer) {
 		if (bpmnElement instanceof Event) {
 			EList<EObject> contents = bpmnElement.eContents();
 			for (EObject obj : contents) {
 
 				AddContext context = new AddContext();
 				context.setTargetContainer((ContainerShape) newContainer);
 				context.setNewObject(obj);
 
 				IAddFeature aFeat = featureProvider.getAddFeature(context);
 				if (aFeat != null && aFeat.canAdd(context)) {
 					aFeat.add(context);
 				}
 			}
 		}
 	}
 
 	private void handleParticipant(Participant participant, AddContext context, BPMNShape shape) {
 		context.setTargetContainer(diagram);
 		context.setLocation((int) shape.getBounds().getX(), (int) shape.getBounds().getY());
 		FeatureSupport.setHorizontal(context, shape.isIsHorizontal());
 	}
 	
 	private void handleLane(Lane lane, AddContext context, BPMNShape shape) {
 		BaseElement parent = (BaseElement)lane.eContainer().eContainer();
 		ContainerShape cont = diagram;
 
 		// find the process this lane belongs to
 		for (BaseElement be : elements.keySet()) {
 			if (be instanceof Participant) {
 				Process processRef = ((Participant) be).getProcessRef();
 				if (processRef != null && parent.getId().equals(processRef.getId())) {
 					cont = (ContainerShape) elements.get(be);
 					break;
 				}
 			} else if (be instanceof Process) {
 				if (be.getId().equals(parent.getId())) {
 					cont = (ContainerShape) elements.get(be);
 					break;
 				}
 			} else if (be instanceof Lane) {
 				if (be.getId().equals(parent.getId())) {
 					cont = (ContainerShape) elements.get(be);
 					break;
 				}
 			}
 		}
 		context.setTargetContainer(cont);
 
 		if (shape!=null) {
 			int x = (int) shape.getBounds().getX();
 			int y = (int) shape.getBounds().getY();
 			ILocation loc = Graphiti.getPeLayoutService().getLocationRelativeToDiagram(cont);
 			x -= loc.getX();
 			y -= loc.getY();
 	
 			context.setLocation((int) x, y);
 			FeatureSupport.setHorizontal(context, shape.isIsHorizontal());
 		}
 	}
 
 	private void handleFlowElement(FlowElement element, AddContext context, BPMNShape shape) {
 		ContainerShape target = diagram;
 		int x = (int) shape.getBounds().getX();
 		int y = (int) shape.getBounds().getY();
 
 		// find a correct container element
 		List<Lane> lanes;
 		if (element instanceof FlowNode)
 			lanes = ((FlowNode)element).getLanes();
 		else {
 			lanes = new ArrayList<Lane>();
 			if (element instanceof DataObject || element instanceof DataObjectReference) {
 				int w = (int) shape.getBounds().getWidth();
 				int h = (int) shape.getBounds().getHeight();
 				// if this Data Object is contained within a Lane, make the Lane the target container
 				for (Entry<BaseElement, PictogramElement> entry : elements.entrySet()) {
 					if (entry.getKey() instanceof Lane) {
 						ContainerShape laneShape = (ContainerShape)entry.getValue();
 						if (GraphicsUtil.intersects(laneShape, x, y, w, h)) {
 							lanes.add((Lane)entry.getKey());
 							break;
 						}
 					}
 				}
 			}
 		}
 		EObject parent = element.eContainer();
 		if (	(parent instanceof SubProcess
 				|| parent instanceof Process
 				|| parent instanceof SubChoreography)
 				&& lanes.isEmpty()
 		) {
 			ContainerShape containerShape = (ContainerShape) elements.get(parent);
 			if (containerShape==null) {
 				// Maybe this is a Process that is referenced by a Pool
 				for (Entry<BaseElement, PictogramElement> entry : elements.entrySet()) {
 					if (entry.getKey() instanceof Participant) {
 						Participant p = (Participant)entry.getKey();
 						if (p.getProcessRef() == parent) {
 							containerShape = (ContainerShape)entry.getValue();
 							break;
 						}
 					}
 				}
 			}
 			if (containerShape != null) {
 				// add the FlowNode to its parent SubProcess, Process or SubChoreography
 				// but only if the node is on the same BPMNDiagram as its parent container
 				BPMNDiagram parentDiagram = DIUtils.findBPMNDiagram(editor, (BaseElement)parent, false);
 				BPMNDiagram childDiagram = DIUtils.findBPMNDiagram(editor, element, false);
 				if (parentDiagram == childDiagram) {
 					target = containerShape;
 					ILocation loc = Graphiti.getPeLayoutService().getLocationRelativeToDiagram(target);
 					x -= loc.getX();
 					y -= loc.getY();
 				}
 				else if (parentDiagram!=null) {
 					Diagram d = DIUtils.findDiagram(editor, parentDiagram);
 					target = d;
 				}
 			}
 		}
 		else if (!lanes.isEmpty()) {
 			for (Lane lane : lanes) {
 				target = (ContainerShape) elements.get(lane);
 				ILocation loc = Graphiti.getPeLayoutService().getLocationRelativeToDiagram(target);
 				x -= loc.getX();
 				y -= loc.getY();
 			}
 		}
 		context.setTargetContainer(target);
 		context.setLocation(x, y);
 	}
 
 	private void handleItemAwareElement(ItemAwareElement element, AddContext context, BPMNShape shape) {
 		ContainerShape target = diagram;
 		int x = (int) shape.getBounds().getX();
 		int y = (int) shape.getBounds().getY();
 		int w = (int) shape.getBounds().getWidth();
 		int h = (int) shape.getBounds().getHeight();
 
 		// find a correct container element
 		// if this Data Object is contained within a Lane, make the Lane the target container
 		for (Entry<BaseElement, PictogramElement> entry : elements.entrySet()) {
 			if (entry.getKey() instanceof Lane) {
 				ContainerShape laneShape = (ContainerShape)entry.getValue();
 				if (GraphicsUtil.intersects(laneShape, x, y, w, h)) {
 					target = (ContainerShape) laneShape;
 					ILocation loc = Graphiti.getPeLayoutService().getLocationRelativeToDiagram(target);
 					x -= loc.getX();
 					y -= loc.getY();
 					break;
 				}
 			}
 		}
 		context.setTargetContainer(target);
 		context.setLocation(x, y);
 	}
 	
 	/**
 	 * Find a Graphiti feature for given edge and generate necessary connections and bendpoints.
 	 * 
 	 * @param shape
 	 */
 	private void createEdge(BPMNEdge bpmnEdge) {
 		BaseElement bpmnElement = bpmnEdge.getBpmnElement();
 		EObject source = null;
 		EObject target = null;
 		PictogramElement se = null;
 		PictogramElement te = null;
 
 		// for some reason connectors don't have a common interface
 		if (bpmnElement instanceof MessageFlow) {
 			source = ((MessageFlow) bpmnElement).getSourceRef();
 			target = ((MessageFlow) bpmnElement).getTargetRef();
 			se = elements.get(source);
 			te = elements.get(target);
 		} else if (bpmnElement instanceof SequenceFlow) {
 			source = ((SequenceFlow) bpmnElement).getSourceRef();
 			target = ((SequenceFlow) bpmnElement).getTargetRef();
 			se = elements.get(source);
 			te = elements.get(target);
 		} else if (bpmnElement instanceof Association) {
 			source = ((Association) bpmnElement).getSourceRef();
 			target = ((Association) bpmnElement).getTargetRef();
 			se = elements.get(source);
 			te = elements.get(target);
 		} else if (bpmnElement instanceof ConversationLink) {
 			source = ((ConversationLink) bpmnElement).getSourceRef();
 			target = ((ConversationLink) bpmnElement).getTargetRef();
 			se = elements.get(source);
 			te = elements.get(target);
 		} else if (bpmnElement instanceof DataAssociation) {
 			// Data Association allows connections for multiple starting points, we don't support it yet
 			List<ItemAwareElement> sourceRef = ((DataAssociation) bpmnElement).getSourceRef();
 			ItemAwareElement targetRef = ((DataAssociation) bpmnElement).getTargetRef();
 			if (sourceRef != null) {
 				source = sourceRef.get(0);
 			}
 			target = targetRef;
 			do {
 				se = elements.get(source);
 				source = source.eContainer();
 			} while (se == null && source.eContainer() != null);
 			do {
 				te = elements.get(target);
 				target = target.eContainer();
 			} while (te == null && target.eContainer() != null);
 		}
 
 		ModelUtil.addID(bpmnElement);
 		
 		if (source != null && target != null) {
 			addSourceAndTargetToEdge(bpmnEdge, source, target);
 		}
 
 		if (se != null && te != null) {
 
 			Connection conn = createConnectionAndSetBendpoints(bpmnEdge, se, te);
 			elements.put(bpmnElement, conn);
 			
 		} else {
 			Activator.logStatus(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
 					"Couldn't find target element, probably not supported! Source: " + source + " Target: " + target
 							+ " Element: " + bpmnElement));
 		}
 	}
 
 	private void addSourceAndTargetToEdge(BPMNEdge bpmnEdge, EObject source, EObject target) {
 		// We get most of the information from the BpmnEdge, not from the referencing business object. Because of this
 		// we must ensure, that the edge contains necessary information.
 		DiagramElement sourceElement = null;
 		DiagramElement targetElement = null;
 		
 		try {
 			sourceElement = bpmnEdge.getSourceElement();
 			targetElement = bpmnEdge.getTargetElement();
 		}catch (ClassCastException e) {
 			// some other modelers like Yaoqiang BPMN are doing it wrong, they reference business objects instead of 
 			// DiagramElements (see BPMN 2.0 spec, p. 405, 12.2.3.5). this will cause an execption 
 			// in the BPMN 2.0 metamodel implementation
 		}
 		
 		if (sourceElement == null) {
 			bpmnEdge.setSourceElement(modelHandler.findDIElement((BaseElement) source));
 		}
 		if (targetElement == null) {
 			bpmnEdge.setTargetElement(modelHandler.findDIElement((BaseElement) target));
 		}
 	}
 
 	private Connection createConnectionAndSetBendpoints(BPMNEdge bpmnEdge, PictogramElement sourcePE,
 			PictogramElement targetPE) {
 
 		FixPointAnchor sourceAnchor = createAnchor(sourcePE, bpmnEdge, true);
 		FixPointAnchor targetAnchor = createAnchor(targetPE, bpmnEdge, false);
 
 		AddConnectionContext context = new AddConnectionContext(sourceAnchor, targetAnchor);
 		context.setNewObject(bpmnEdge.getBpmnElement());
 
 		IAddFeature addFeature = featureProvider.getAddFeature(context);
 		if (addFeature != null && addFeature.canAdd(context)) {
 			context.putProperty(IMPORT_PROPERTY, true);
 			Connection connection = (Connection) addFeature.add(context);
 			if (AnchorUtil.useAdHocAnchors(sourcePE, connection)) {
 				peService.setPropertyValue(connection, AnchorUtil.CONNECTION_SOURCE_LOCATION,
 						AnchorUtil.pointToString(sourceAnchor.getLocation()));
 			}
 			if (AnchorUtil.useAdHocAnchors(targetPE, connection)) {
 				peService.setPropertyValue(connection, AnchorUtil.CONNECTION_TARGET_LOCATION,
 						AnchorUtil.pointToString(targetAnchor.getLocation()));
 			}
 
 			if (connection instanceof FreeFormConnectionImpl) {
 				List<Point> waypoints = bpmnEdge.getWaypoint();
 				for (int i=1; i<waypoints.size()-1; ++i) {
 					DIUtils.addBendPoint((FreeFormConnection)connection, waypoints.get(i));
 				}
 			}
 			
 			featureProvider.link(connection, new Object[] { bpmnEdge.getBpmnElement(), bpmnEdge });
 			return connection;
 		} else {
 			Activator.logStatus(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Unsupported feature "
 					+ ((EObject) context.getNewObject()).eClass().getName()));
 		}
 		return null;
 	}
 
 	private FixPointAnchor createAnchor(PictogramElement pictogramElement, BPMNEdge bpmnEdge, boolean isSource) {
 		FixPointAnchor sa;
 		
 		if (pictogramElement instanceof FreeFormConnection) {
 			Shape connectionPointShape = AnchorUtil.createConnectionPoint(featureProvider,
 					(FreeFormConnection)pictogramElement,
 					Graphiti.getPeLayoutService().getConnectionMidpoint((FreeFormConnection)pictogramElement, 0.5));
 			sa = AnchorUtil.getConnectionPointAnchor(connectionPointShape);
 		}
 		else
 		{
 			BaseElement baseElement = BusinessObjectUtil.getFirstBaseElement(pictogramElement);
 			BaseElement flowElement = bpmnEdge.getBpmnElement();
 			Point waypoint = null;
 			if (isSource) {
 				waypoint = bpmnEdge.getWaypoint().get(0);
 			}
 			else {
 				waypoint = bpmnEdge.getWaypoint().get(bpmnEdge.getWaypoint().size()-1);
 				
 			}
 			
 			int x = (int)waypoint.getX();
 			int y = (int)waypoint.getY();
 			org.eclipse.graphiti.mm.algorithms.styles.Point anchorPoint = gaService.createPoint(x,y);
 			
 			if (AnchorUtil.useAdHocAnchors(baseElement, flowElement)) {
 				ILocation loc = Graphiti.getPeLayoutService().getLocationRelativeToDiagram((Shape)pictogramElement);
 				anchorPoint.setX(x - loc.getX());
 				anchorPoint.setY(y - loc.getY());
 				sa = AnchorUtil.createAdHocAnchor((AnchorContainer)pictogramElement, anchorPoint);
 				setAnchorLocation(pictogramElement, sa, waypoint);
 			}
 			else {
 				sa = AnchorUtil.findNearestAnchor((AnchorContainer)pictogramElement, anchorPoint);
 			}
 		}
 		return sa;
 	}
 
 	private void setAnchorLocation(PictogramElement elem, FixPointAnchor anchor, Point point) {
 		org.eclipse.graphiti.mm.algorithms.styles.Point p = gaService.createPoint((int) point.getX(),
 				(int) point.getY());
 
 		ILocation loc;
 		if (elem instanceof Connection)
 			loc = Graphiti.getPeLayoutService().getConnectionMidpoint((Connection)elem, 0.5);
 		else
 			loc = Graphiti.getPeLayoutService().getLocationRelativeToDiagram((Shape) elem);
 
 		int x = p.getX() - loc.getX();
 		int y = p.getY() - loc.getY();
 
 		p.setX(x);
 		p.setY(y);
 
 		anchor.setLocation(p);
 	}
 }
