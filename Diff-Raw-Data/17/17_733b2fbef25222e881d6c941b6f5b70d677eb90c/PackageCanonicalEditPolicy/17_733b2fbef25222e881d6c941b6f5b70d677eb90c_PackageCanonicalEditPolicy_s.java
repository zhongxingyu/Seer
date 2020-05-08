 package org.eclipse.uml2.diagram.sequence.edit.policies;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
 import org.eclipse.gmf.runtime.diagram.ui.commands.DeferredLayoutCommand;
 import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CanonicalConnectionEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
 import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.Edge;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.diagram.common.editpolicies.UpdateDescriptionRequest;
 import org.eclipse.uml2.diagram.common.genapi.IUpdaterLinkDescriptor;
 import org.eclipse.uml2.diagram.common.genapi.IUpdaterNodeDescriptor;
 import org.eclipse.uml2.diagram.sequence.edit.parts.ActionExecutionSpecificationEditPart;
 import org.eclipse.uml2.diagram.sequence.edit.parts.BehaviorExecutionSpecificationEditPart;
 import org.eclipse.uml2.diagram.sequence.edit.parts.CombinedFragmentMountingRegionEditPart;
 import org.eclipse.uml2.diagram.sequence.edit.parts.GateEditPart;
 import org.eclipse.uml2.diagram.sequence.edit.parts.InteractionEditPart;
 import org.eclipse.uml2.diagram.sequence.edit.parts.InteractionOperandMountingRegionEditPart;
 import org.eclipse.uml2.diagram.sequence.edit.parts.InteractionUseMountingRegionEditPart;
 import org.eclipse.uml2.diagram.sequence.edit.parts.LayeredCombinedFragmentEditPart;
 import org.eclipse.uml2.diagram.sequence.edit.parts.LayeredInteractionUseEditPart;
 import org.eclipse.uml2.diagram.sequence.edit.parts.LayeredOperandEditPart;
 import org.eclipse.uml2.diagram.sequence.edit.parts.LifelineEditPart;
 import org.eclipse.uml2.diagram.sequence.edit.parts.MessageEditPart;
 import org.eclipse.uml2.diagram.sequence.edit.parts.MountingLinkEditPart;
 import org.eclipse.uml2.diagram.sequence.edit.parts.PackageEditPart;
 import org.eclipse.uml2.diagram.sequence.edit.parts.StateInvariantEditPart;
 import org.eclipse.uml2.diagram.sequence.part.UMLDiagramUpdater;
 import org.eclipse.uml2.diagram.sequence.part.UMLVisualIDRegistry;
 import org.eclipse.uml2.uml.UMLPackage;
 
 /**
  * @generated
  */
 
 public class PackageCanonicalEditPolicy extends CanonicalConnectionEditPolicy {
 
 	/**
 	 * @generated
 	 */
 	Set myFeaturesToSynchronize;
 
 	/**
 	 * @generated
 	 */
 	protected List getSemanticChildrenList() {
 		View viewObject = (View) getHost().getModel();
 		List result = new LinkedList();
 		for (Iterator it = UMLDiagramUpdater.getPackage_1000SemanticChildren(viewObject).iterator(); it.hasNext();) {
 			result.add(((IUpdaterNodeDescriptor) it.next()).getModelElement());
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean shouldDeleteView(View view) {
 		return true;
 	}
 
 	/**
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	protected boolean isOrphaned(Collection semanticChildren, final View view) {
 		int visualID = UMLVisualIDRegistry.getVisualID(view);
 		switch (visualID) {
 		case GateEditPart.VISUAL_ID:
 		case LayeredInteractionUseEditPart.VISUAL_ID:
 		case LayeredCombinedFragmentEditPart.VISUAL_ID:
 		case LayeredOperandEditPart.VISUAL_ID:
 		case LifelineEditPart.VISUAL_ID:
 		case ActionExecutionSpecificationEditPart.VISUAL_ID:
 		case StateInvariantEditPart.VISUAL_ID:
 		case BehaviorExecutionSpecificationEditPart.VISUAL_ID:
 		case InteractionUseMountingRegionEditPart.VISUAL_ID:
 		case CombinedFragmentMountingRegionEditPart.VISUAL_ID:
 		case InteractionOperandMountingRegionEditPart.VISUAL_ID:
 			return true;
 		case InteractionEditPart.VISUAL_ID:
 			if (!semanticChildren.contains(view.getElement())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected String getDefaultFactoryHint() {
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected Set getFeaturesToSynchronize() {
 		if (myFeaturesToSynchronize == null) {
 			myFeaturesToSynchronize = new HashSet();
 			myFeaturesToSynchronize.add(UMLPackage.eINSTANCE.getPackage_PackagedElement());
 		}
 		return myFeaturesToSynchronize;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected List getSemanticConnectionsList() {
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected EObject getSourceElement(EObject relationship) {
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected EObject getTargetElement(EObject relationship) {
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean shouldIncludeConnection(Edge connector, Collection children) {
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void refreshSemantic() {
 		List createdViews = new LinkedList();
 		createdViews.addAll(refreshSemanticChildren());
 		List createdConnectionViews = new LinkedList();
 		createdConnectionViews.addAll(refreshSemanticConnections());
 		createdConnectionViews.addAll(refreshConnections());
 
 		if (createdViews.size() > 1) {
 			// perform a layout of the container
 			DeferredLayoutCommand layoutCmd = new DeferredLayoutCommand(host().getEditingDomain(), createdViews, host());
 			executeCommand(new ICommandProxy(layoutCmd));
 		}
 
 		createdViews.addAll(createdConnectionViews);
 		makeViewsImmutable(createdViews);
 	}
 
 	/**
 	 * @generated
 	 */
 	private Diagram getDiagram() {
 		return ((View) getHost().getModel()).getDiagram();
 	}
 
 	/**
 	 * @generated
 	 */
 	private Collection refreshConnections() {
 		Domain2Notation domain2NotationMap = new Domain2Notation();
 		Collection linkDescriptors = collectAllLinks(getDiagram(), domain2NotationMap);
 		Collection existingLinks = new LinkedList(getDiagram().getEdges());
 		for (Iterator linksIterator = existingLinks.iterator(); linksIterator.hasNext();) {
 			Edge nextDiagramLink = (Edge) linksIterator.next();
 			int diagramLinkVisualID = UMLVisualIDRegistry.getVisualID(nextDiagramLink);
 			if (diagramLinkVisualID == -1) {
 				if (nextDiagramLink.getSource() != null && nextDiagramLink.getTarget() != null) {
 					linksIterator.remove();
 				}
 				continue;
 			}
 			//don't remove notation-only links 
 			if (isNotationOnlyEdge(nextDiagramLink)) {
 				linksIterator.remove();
 				continue;
 			}
 			EObject diagramLinkObject = nextDiagramLink.getElement();
 			EObject diagramLinkSrc = nextDiagramLink.getSource().getElement();
 			EObject diagramLinkDst = nextDiagramLink.getTarget().getElement();
 			boolean existingLinkRemoved = false;
 			for (Iterator LinkDescriptorsIterator = linkDescriptors.iterator(); LinkDescriptorsIterator.hasNext();) {
 				IUpdaterLinkDescriptor nextLinkDescriptor = (IUpdaterLinkDescriptor) LinkDescriptorsIterator.next();
 				if (diagramLinkObject == nextLinkDescriptor.getModelElement() && diagramLinkSrc == nextLinkDescriptor.getSource() && diagramLinkDst == nextLinkDescriptor.getDestination()
 						&& diagramLinkVisualID == nextLinkDescriptor.getVisualID()) {
 					if (!existingLinkRemoved) {
 						linksIterator.remove();
 						existingLinkRemoved = true;
 					}
 					LinkDescriptorsIterator.remove();
 				}
 			}
 		}
 		deleteViews(existingLinks.iterator());
 		return createConnections(linkDescriptors, domain2NotationMap);
 	}
 
 	/**
 	 * @generated
 	 */
 	private Collection collectAllLinks(View view, Domain2Notation domain2NotationMap) {
 		if (UMLVisualIDRegistry.isShortcutDescendant(view)) {
 			return collectLinksOutgoingFromShortcut(view, domain2NotationMap);
 		}
 		Collection result = new LinkedList();
 		switch (UMLVisualIDRegistry.getVisualID(view)) {
 		case PackageEditPart.VISUAL_ID: {
 			if (!domain2NotationMap.containsKey(view.getElement())) {
 				result.addAll(UMLDiagramUpdater.getPackage_1000ContainedLinks(view));
 			}
 			domain2NotationMap.put(view.getElement(), view);
 			break;
 		}
 		case InteractionEditPart.VISUAL_ID: {
 			if (!domain2NotationMap.containsKey(view.getElement())) {
 				result.addAll(UMLDiagramUpdater.getInteraction_2001ContainedLinks(view));
 			}
 			domain2NotationMap.put(view.getElement(), view);
 			break;
 		}
 		case GateEditPart.VISUAL_ID: {
 			if (!domain2NotationMap.containsKey(view.getElement())) {
 				result.addAll(UMLDiagramUpdater.getGate_3005ContainedLinks(view));
 			}
 			domain2NotationMap.put(view.getElement(), view);
 			break;
 		}
 		case LayeredInteractionUseEditPart.VISUAL_ID: {
 			if (!domain2NotationMap.containsKey(view.getElement())) {
 				result.addAll(UMLDiagramUpdater.getInteractionUse_3007ContainedLinks(view));
 			}
 			domain2NotationMap.put(view.getElement(), view);
 			break;
 		}
 		case LayeredCombinedFragmentEditPart.VISUAL_ID: {
 			if (!domain2NotationMap.containsKey(view.getElement())) {
 				result.addAll(UMLDiagramUpdater.getCombinedFragment_3008ContainedLinks(view));
 			}
 			domain2NotationMap.put(view.getElement(), view);
 			break;
 		}
 		case LayeredOperandEditPart.VISUAL_ID: {
 			if (!domain2NotationMap.containsKey(view.getElement())) {
 				result.addAll(UMLDiagramUpdater.getInteractionOperand_3009ContainedLinks(view));
 			}
 			domain2NotationMap.put(view.getElement(), view);
 			break;
 		}
 		case LifelineEditPart.VISUAL_ID: {
 			if (!domain2NotationMap.containsKey(view.getElement())) {
 				result.addAll(UMLDiagramUpdater.getLifeline_3001ContainedLinks(view));
 			}
 			domain2NotationMap.put(view.getElement(), view);
 			break;
 		}
 		case ActionExecutionSpecificationEditPart.VISUAL_ID: {
 			if (!domain2NotationMap.containsKey(view.getElement())) {
 				result.addAll(UMLDiagramUpdater.getActionExecutionSpecification_3002ContainedLinks(view));
 			}
 			domain2NotationMap.put(view.getElement(), view);
 			break;
 		}
 		case StateInvariantEditPart.VISUAL_ID: {
 			if (!domain2NotationMap.containsKey(view.getElement())) {
 				result.addAll(UMLDiagramUpdater.getStateInvariant_3003ContainedLinks(view));
 			}
 			domain2NotationMap.put(view.getElement(), view);
 			break;
 		}
 		case BehaviorExecutionSpecificationEditPart.VISUAL_ID: {
 			if (!domain2NotationMap.containsKey(view.getElement())) {
 				result.addAll(UMLDiagramUpdater.getBehaviorExecutionSpecification_3004ContainedLinks(view));
 			}
 			domain2NotationMap.put(view.getElement(), view);
 			break;
 		}
 		case InteractionUseMountingRegionEditPart.VISUAL_ID: {
 			if (!domain2NotationMap.containsKey(view.getElement())) {
 				result.addAll(UMLDiagramUpdater.getInteractionUse_3006ContainedLinks(view));
 			}
 			domain2NotationMap.put(view.getElement(), view);
 			break;
 		}
 		case CombinedFragmentMountingRegionEditPart.VISUAL_ID: {
 			if (!domain2NotationMap.containsKey(view.getElement())) {
 				result.addAll(UMLDiagramUpdater.getCombinedFragment_3010ContainedLinks(view));
 			}
 			domain2NotationMap.put(view.getElement(), view);
 			break;
 		}
 		case InteractionOperandMountingRegionEditPart.VISUAL_ID: {
 			if (!domain2NotationMap.containsKey(view.getElement())) {
 				result.addAll(UMLDiagramUpdater.getInteractionOperand_3011ContainedLinks(view));
 			}
 			domain2NotationMap.put(view.getElement(), view);
 			break;
 		}
 		case MessageEditPart.VISUAL_ID: {
 			if (!domain2NotationMap.containsKey(view.getElement())) {
 				result.addAll(UMLDiagramUpdater.getMessage_4001ContainedLinks(view));
 			}
 			domain2NotationMap.put(view.getElement(), view);
 			break;
 		}
 		}
 		for (Iterator children = view.getChildren().iterator(); children.hasNext();) {
 			result.addAll(collectAllLinks((View) children.next(), domain2NotationMap));
 		}
 		for (Iterator edges = view.getSourceEdges().iterator(); edges.hasNext();) {
 			result.addAll(collectAllLinks((View) edges.next(), domain2NotationMap));
 		}
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	private Collection createConnections(Collection linkDescriptors, Domain2Notation domain2NotationMap) {
 		List adapters = new LinkedList();
 		for (Iterator linkDescriptorsIterator = linkDescriptors.iterator(); linkDescriptorsIterator.hasNext();) {
 			final IUpdaterLinkDescriptor nextLinkDescriptor = (IUpdaterLinkDescriptor) linkDescriptorsIterator.next();
 			EditPart sourceEditPart = getSourceEditPart(nextLinkDescriptor, domain2NotationMap);
 			EditPart targetEditPart = getTargetEditPart(nextLinkDescriptor, domain2NotationMap);
 			if (sourceEditPart == null || targetEditPart == null) {
 				continue;
 			}
 			CreateConnectionViewRequest.ConnectionViewDescriptor descriptor = new CreateConnectionViewRequest.ConnectionViewDescriptor(nextLinkDescriptor.getSemanticAdapter(), UMLVisualIDRegistry
 					.getType(nextLinkDescriptor.getVisualID()), ViewUtil.APPEND, false, ((IGraphicalEditPart) getHost()).getDiagramPreferencesHint());
 			CreateConnectionViewRequest ccr = new CreateConnectionViewRequest(descriptor);
 			ccr.setType(RequestConstants.REQ_CONNECTION_START);
 			ccr.setSourceEditPart(sourceEditPart);
 			sourceEditPart.getCommand(ccr);
 			ccr.setTargetEditPart(targetEditPart);
 			ccr.setType(RequestConstants.REQ_CONNECTION_END);
 			Command cmd = targetEditPart.getCommand(ccr);
 			if (cmd != null && cmd.canExecute()) {
 				executeCommand(cmd);
 				IAdaptable viewAdapter = (IAdaptable) ccr.getNewObject();
 				if (viewAdapter != null) {
 					adapters.add(viewAdapter);
 				}
 			}
 		}
 		return adapters;
 	}
 
 	/**
 	 * @generated
 	 */
 	private EditPart getEditPart(EObject domainModelElement, Domain2Notation domain2NotationMap) {
 		View view = (View) domain2NotationMap.get(domainModelElement);
 		if (view != null) {
 			return (EditPart) getHost().getViewer().getEditPartRegistry().get(view);
 		}
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	private EditPart getSourceEditPart(IUpdaterLinkDescriptor descriptor, Domain2Notation domain2NotationMap) {
 		return getEditPart(descriptor.getSource(), domain2NotationMap);
 	}
 
 	/**
 	 * @generated
 	 */
 	private EditPart getTargetEditPart(IUpdaterLinkDescriptor descriptor, Domain2Notation domain2NotationMap) {
 		return getEditPart(descriptor.getDestination(), domain2NotationMap);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected final EditPart getHintedEditPart(EObject domainModelElement, Domain2Notation domain2NotationMap, int hintVisualId) {
 		View view = (View) domain2NotationMap.getHinted(domainModelElement, UMLVisualIDRegistry.getType(hintVisualId));
 		if (view != null) {
 			return (EditPart) getHost().getViewer().getEditPartRegistry().get(view);
 		}
 		return null;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private boolean isNotationOnlyEdge(Edge edge) {
 		return UMLVisualIDRegistry.getType(MountingLinkEditPart.VISUAL_ID).equals(edge.getType());
 	}
 
 	/**
 	 * @generated
 	 */
 	private Collection<IUpdaterLinkDescriptor> collectLinksOutgoingFromShortcut(View view, Domain2Notation domain2NotationMap) {
 		EditPart ep = (EditPart) getHost().getViewer().getEditPartRegistry().get(view);
 		if (false == ep instanceof IGraphicalEditPart) {
 			return Collections.emptyList();
 		}
 		IGraphicalEditPart editPart = (IGraphicalEditPart) ep;
 		UpdateDescriptionRequest request = new UpdateDescriptionRequest();
 		//we are not using the result command -- each editpart from the tree 
 		//is required to push data into the request
 		editPart.getCommand(request);
 
 		Set<IUpdaterLinkDescriptor> linksToFromShortcuts = new HashSet<IUpdaterLinkDescriptor>();
 		for (UpdateDescriptionRequest.Descriptor next : request.getDescriptions()) {
 			linksToFromShortcuts.addAll(next.getContainedLinks());
 			linksToFromShortcuts.addAll(next.getOutgoingLinks());
 
 			if (next.getSemanticElement() != null) {
 				domain2NotationMap.put(next.getSemanticElement(), next.getProvider().getNotationView());
 			}
 
 		}
 
 		return linksToFromShortcuts;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static class Domain2Notation {
 
 		/**
 		 * @generated
 		 */
 		private final HashMap myMap = new HashMap();
 
 		/**
 		 * @generated
 		 */
 		public boolean containsDomainElement(EObject domainElement) {
 			return myMap.containsKey(domainElement);
 		}
 
 		/**
 		 * @generated
 		 */
 		public boolean containsKey(EObject domainElement) {
 			return containsDomainElement(domainElement);
 		}
 
 		/**
 		 * @generated
 		 */
 		public void put(EObject domainElement, View view) {
 			Object viewOrList = myMap.get(domainElement);
 			if (viewOrList instanceof View) {
 				myMap.remove(domainElement);
 				List<View> list = new LinkedList<View>();
 				list.add((View) viewOrList);
 				myMap.put(domainElement, list);
 				list.add(view);
 			} else if (viewOrList instanceof List) {
 				((List) viewOrList).add(view);
 			} else {
 				myMap.put(domainElement, view);
 			}
 		}
 
 		/**
 		 * @generated
 		 */
 		public View get(EObject domainEObject) {
 			Object viewOrList = myMap.get(domainEObject);
 			if (viewOrList instanceof View) {
 				return (View) viewOrList;
 			}
 			if (viewOrList instanceof List) {
 				// preferring not-shortcut to shortcut -- important for cases when links arr to/from 
 				// the element that is additionally shortcutted to the same diagram
 				for (Object next : (List) viewOrList) {
 					View nextView = (View) next;
 					if (nextView.getEAnnotation("Shortcut") == null) { //$NON-NLS-1$
 						return nextView;
 					}
 				}
 				return (View) ((List) viewOrList).get(0);
 			}
 			return null;
 		}
 
 		/**
 		 * @generated
 		 */
 		public View getHinted(EObject domainEObject, String hint) {
 			if (hint == null) {
 				return get(domainEObject);
 			}
 			Object viewOrList = myMap.get(domainEObject);
 			if (viewOrList instanceof View) {
 				//no choice, will return what we have
 				return (View) viewOrList;
 			}
			for (Object next : (List) viewOrList) {
				View nextView = (View) next;
				if (hint.equals(nextView.getType())) {
					return nextView;
 				}
 			}
			//hint not found -- return what we have
			return (View) ((List) viewOrList).get(0);
 		}
 
 	}
 
 	/**
 	 * @generated
 	 */
 	@Override
 	protected String getFactoryHint(IAdaptable elementAdapter) {
 		EObject domainModelElment = (EObject) elementAdapter.getAdapter(EObject.class);
 		View containerView = ((IGraphicalEditPart) getHost()).getNotationView();
 		int hint = UMLVisualIDRegistry.getNodeVisualID(containerView, domainModelElment);
 		return (hint != -1) ? UMLVisualIDRegistry.getType(hint) : super.getFactoryHint(elementAdapter);
 	}
 }
