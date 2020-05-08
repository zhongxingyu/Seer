 package org.eclipse.uml2.diagram.profile.edit.policies;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.emf.ecore.EClass;
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
 import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
 import org.eclipse.gmf.runtime.emf.type.core.IElementType;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.Edge;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.diagram.profile.edit.parts.ConstraintEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.ElementImport2EditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.ElementImportEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.EnumerationEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.EnumerationLiteralEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.ExtensionEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.GeneralizationEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.Profile2EditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.Profile3EditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.ProfileEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.PropertyEditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.Stereotype2EditPart;
 import org.eclipse.uml2.diagram.profile.edit.parts.StereotypeEditPart;
 import org.eclipse.uml2.diagram.profile.part.UMLVisualIDRegistry;
 import org.eclipse.uml2.uml.Classifier;
 import org.eclipse.uml2.uml.ElementImport;
 import org.eclipse.uml2.uml.Extension;
 import org.eclipse.uml2.uml.Generalization;
 import org.eclipse.uml2.uml.Package;
 import org.eclipse.uml2.uml.Profile;
 import org.eclipse.uml2.uml.Stereotype;
 import org.eclipse.uml2.uml.UMLPackage;
 
 /**
  * @generated
  */
 public class ProfileCanonicalEditPolicy extends CanonicalConnectionEditPolicy {
 
 	/**
 	 * We have "dummy" TopLevelNode (with vid = 2007). The only purpose 
 	 * for this node is to be a container for children (imports, etc) 
 	 * of the "main" diagram figure (that one shown as Canvas).
 	 * 
 	 * The code that replace generated SubstituteWithCanvas children associated 
 	 * with vid=2007 with the "main" instance is below.
 	 * 
 	 * Also we have modified the VisualIDRegistry#getNodeVisualID() to return
 	 * VID = 2007, for the case when top-level view is created for the same
 	 * semantic element as the canvas view. 
 	 * 
 	 * @see VisualIDRegistry 
	 * @generated
 	 */
 	protected List getSemanticChildrenList() {
 		List result = new LinkedList();
 		EObject modelObject = ((View) getHost().getModel()).getElement();
 		View viewObject = (View) getHost().getModel();
 		EObject nextValue;
 		int nodeVID;
 		for (Iterator values = ((Profile) modelObject).getOwnedStereotypes().iterator(); values.hasNext();) {
 			nextValue = (EObject) values.next();
 			nodeVID = UMLVisualIDRegistry.getNodeVisualID(viewObject, nextValue);
 			if (StereotypeEditPart.VISUAL_ID == nodeVID) {
 				result.add(nextValue);
 			}
 		}
 		for (Iterator values = ((Package) modelObject).getPackagedElements().iterator(); values.hasNext();) {
 			nextValue = (EObject) values.next();
 			nodeVID = UMLVisualIDRegistry.getNodeVisualID(viewObject, nextValue);
 			switch (nodeVID) {
 			case Profile2EditPart.VISUAL_ID: {
 				result.add(nextValue);
 				break;
 			}
 			case Profile3EditPart.VISUAL_ID: {
 				//Don't add generated SubstituteWithCanvas children
 				//result.add(nextValue);
 				break;
 			}
 			}
 		}
 		for (Iterator values = ((Package) modelObject).getOwnedTypes().iterator(); values.hasNext();) {
 			nextValue = (EObject) values.next();
 			nodeVID = UMLVisualIDRegistry.getNodeVisualID(viewObject, nextValue);
 			if (EnumerationEditPart.VISUAL_ID == nodeVID) {
 				result.add(nextValue);
 			}
 		}
 		for (Iterator values = ((Profile) modelObject).getMetaclassReferences().iterator(); values.hasNext();) {
 			nextValue = (EObject) values.next();
 			nodeVID = UMLVisualIDRegistry.getNodeVisualID(viewObject, nextValue);
 			if (ElementImportEditPart.VISUAL_ID == nodeVID) {
 				result.add(nextValue);
 			}
 		}
 
 		// add "main" figure	
 		result.add(modelObject);
 		return result;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean shouldDeleteView(View view) {
 		return view.isSetElement() && view.getElement() != null && view.getElement().eIsProxy();
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
 	private Collection myLinkDescriptors = new LinkedList();
 
 	/**
 	 * @generated
 	 */
 	private Map myEObject2ViewMap = new HashMap();
 
 	/**
 	 * @generated
 	 */
 	private Collection refreshConnections() {
 		try {
 			collectAllLinks(getDiagram());
 			Collection existingLinks = new LinkedList(getDiagram().getEdges());
 			for (Iterator diagramLinks = existingLinks.iterator(); diagramLinks.hasNext();) {
 				Edge nextDiagramLink = (Edge) diagramLinks.next();
 				EObject diagramLinkObject = nextDiagramLink.getElement();
 				EObject diagramLinkSrc = nextDiagramLink.getSource().getElement();
 				EObject diagramLinkDst = nextDiagramLink.getTarget().getElement();
 				int diagramLinkVisualID = UMLVisualIDRegistry.getVisualID(nextDiagramLink);
 				for (Iterator modelLinkDescriptors = myLinkDescriptors.iterator(); modelLinkDescriptors.hasNext();) {
 					LinkDescriptor nextLinkDescriptor = (LinkDescriptor) modelLinkDescriptors.next();
 					if (diagramLinkObject == nextLinkDescriptor.getLinkElement() && diagramLinkSrc == nextLinkDescriptor.getSource() && diagramLinkDst == nextLinkDescriptor.getDestination()
 							&& diagramLinkVisualID == nextLinkDescriptor.getVisualID()) {
 						diagramLinks.remove();
 						modelLinkDescriptors.remove();
 					}
 				}
 			}
 			deleteViews(existingLinks.iterator());
 			return createConnections(myLinkDescriptors);
 		} finally {
 			myLinkDescriptors.clear();
 			myEObject2ViewMap.clear();
 		}
 	}
 
 	/**
 	 * @generated
 	 */
 	private void collectAllLinks(View view) {
 		EObject modelElement = view.getElement();
 		int diagramElementVisualID = UMLVisualIDRegistry.getVisualID(view);
 		switch (diagramElementVisualID) {
 		case StereotypeEditPart.VISUAL_ID:
 		case Profile2EditPart.VISUAL_ID:
 		case EnumerationEditPart.VISUAL_ID:
 		case ElementImportEditPart.VISUAL_ID:
 		case Profile3EditPart.VISUAL_ID:
 		case PropertyEditPart.VISUAL_ID:
 		case ConstraintEditPart.VISUAL_ID:
 		case Stereotype2EditPart.VISUAL_ID:
 		case EnumerationLiteralEditPart.VISUAL_ID:
 		case ElementImport2EditPart.VISUAL_ID:
 		case ProfileEditPart.VISUAL_ID: {
 			myEObject2ViewMap.put(modelElement, view);
 			storeLinks(modelElement, getDiagram());
 		}
 		default: {
 		}
 			for (Iterator children = view.getChildren().iterator(); children.hasNext();) {
 				View childView = (View) children.next();
 				collectAllLinks(childView);
 			}
 		}
 	}
 
 	/**
 	 * @generated
 	 */
 	private Collection createConnections(Collection linkDescriptors) {
 		if (linkDescriptors.isEmpty()) {
 			return Collections.EMPTY_LIST;
 		}
 		List adapters = new LinkedList();
 		for (Iterator linkDescriptorsIterator = linkDescriptors.iterator(); linkDescriptorsIterator.hasNext();) {
 			final LinkDescriptor nextLinkDescriptor = (LinkDescriptor) linkDescriptorsIterator.next();
 			EditPart sourceEditPart = getEditPartFor(nextLinkDescriptor.getSource());
 			EditPart targetEditPart = getEditPartFor(nextLinkDescriptor.getDestination());
 			if (sourceEditPart == null || targetEditPart == null) {
 				continue;
 			}
 			CreateConnectionViewRequest.ConnectionViewDescriptor descriptor = new CreateConnectionViewRequest.ConnectionViewDescriptor(nextLinkDescriptor.getSemanticAdapter(), null, ViewUtil.APPEND,
 					false, ((IGraphicalEditPart) getHost()).getDiagramPreferencesHint());
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
 	private EditPart getEditPartFor(EObject modelElement) {
 		View view = (View) myEObject2ViewMap.get(modelElement);
 		if (view != null) {
 			return (EditPart) getHost().getViewer().getEditPartRegistry().get(view);
 		}
 		return null;
 	}
 
 	/**
 	 *@generated
 	 */
 	private void storeLinks(EObject container, Diagram diagram) {
 		EClass containerMetaclass = container.eClass();
 		storeFeatureModelFacetLinks(container, containerMetaclass, diagram);
 		storeTypeModelFacetLinks(container, containerMetaclass);
 	}
 
 	/**
 	 * @generated
 	 */
 	private void storeTypeModelFacetLinks(EObject container, EClass containerMetaclass) {
 		storeTypeModelFacetLinks_Generalization_4001(container, containerMetaclass);
 		storeTypeModelFacetLinks_Extension_4002(container, containerMetaclass);
 	}
 
 	/**
 	 * @generated
 	 */
 	private void storeTypeModelFacetLinks_Generalization_4001(EObject container, EClass containerMetaclass) {
 		if (UMLPackage.eINSTANCE.getClassifier().isSuperTypeOf(containerMetaclass)) {
 			for (Iterator values = ((Classifier) container).getGeneralizations().iterator(); values.hasNext();) {
 				EObject nextValue = ((EObject) values.next());
 				int linkVID = UMLVisualIDRegistry.getLinkWithClassVisualID(nextValue);
 				if (GeneralizationEditPart.VISUAL_ID == linkVID) {
 					Object structuralFeatureResult = ((Generalization) nextValue).getGeneral();
 					if (structuralFeatureResult instanceof EObject) {
 						EObject dst = (EObject) structuralFeatureResult;
 						EObject src = container;
 						myLinkDescriptors.add(new LinkDescriptor(src, dst, nextValue, linkVID));
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	private void storeTypeModelFacetLinks_Extension_4002(EObject container, EClass containerMetaclass) {
 		if (UMLPackage.eINSTANCE.getProfile().isSuperTypeOf(containerMetaclass)) {
 			Profile profile = (Profile) container;
 			for (Iterator extensions = profile.getOwnedTypes().iterator(); extensions.hasNext();) {
 				EObject next = (EObject) extensions.next();
 				if (ExtensionEditPart.VISUAL_ID == UMLVisualIDRegistry.getLinkWithClassVisualID(next)) {
 					Extension nextExtension = (Extension) next;
 					Stereotype stereotype = nextExtension.getStereotype();
 					Classifier metaclass = nextExtension.getMetaclass();
 					ElementImport metaclassImport = profile.getElementImport(metaclass, false);
 					if (stereotype != null && metaclassImport != null) {
 						myLinkDescriptors.add(new LinkDescriptor(stereotype, metaclassImport, ExtensionEditPart.VISUAL_ID));
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 *@generated
 	 */
 	private void storeFeatureModelFacetLinks(EObject container, EClass containerMetaclass, Diagram diagram) {
 
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
 	private class LinkDescriptor {
 
 		/**
 		 * @generated
 		 */
 		private EObject mySource;
 
 		/**
 		 * @generated
 		 */
 		private EObject myDestination;
 
 		/**
 		 * @generated
 		 */
 		private EObject myLinkElement;
 
 		/**
 		 * @generated
 		 */
 		private int myVisualID;
 
 		/**
 		 * @generated
 		 */
 		private IAdaptable mySemanticAdapter;
 
 		/**
 		 * @generated
 		 */
 		protected LinkDescriptor(EObject source, EObject destination, EObject linkElement, int linkVID) {
 			this(source, destination, linkVID);
 			myLinkElement = linkElement;
 			mySemanticAdapter = new EObjectAdapter(linkElement);
 		}
 
 		/**
 		 * @generated
 		 */
 		protected LinkDescriptor(EObject source, EObject destination, IElementType elementType, int linkVID) {
 			this(source, destination, linkVID);
 			myLinkElement = null;
 			final IElementType elementTypeCopy = elementType;
 			mySemanticAdapter = new IAdaptable() {
 
 				public Object getAdapter(Class adapter) {
 					if (IElementType.class.equals(adapter)) {
 						return elementTypeCopy;
 					}
 					return null;
 				}
 			};
 		}
 
 		/**
 		 * @generated
 		 */
 		private LinkDescriptor(EObject source, EObject destination, int linkVID) {
 			mySource = source;
 			myDestination = destination;
 			myVisualID = linkVID;
 		}
 
 		/**
 		 * @generated
 		 */
 		protected EObject getSource() {
 			return mySource;
 		}
 
 		/**
 		 * @generated
 		 */
 		protected EObject getDestination() {
 			return myDestination;
 		}
 
 		/**
 		 * @generated
 		 */
 		protected EObject getLinkElement() {
 			return myLinkElement;
 		}
 
 		/**
 		 * @generated
 		 */
 		protected int getVisualID() {
 			return myVisualID;
 		}
 
 		/**
 		 * @generated
 		 */
 		protected IAdaptable getSemanticAdapter() {
 			return mySemanticAdapter;
 		}
 	}
 
 }
