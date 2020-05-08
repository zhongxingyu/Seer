 package org.eclipse.uml2.diagram.statemachine.providers;
 
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.gmf.runtime.emf.type.core.ElementTypeRegistry;
 import org.eclipse.gmf.runtime.emf.type.core.IElementType;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.uml2.diagram.statemachine.part.UMLDiagramEditorPlugin;
 import org.eclipse.uml2.uml.UMLPackage;
 
 /**
  * @generated
  */
 public class UMLElementTypes extends ElementInitializers {
 
 	/**
 	 * @generated
 	 */
 	private UMLElementTypes() {
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Map elements;
 
 	/**
 	 * @generated
 	 */
 	private static ImageRegistry imageRegistry;
 
 	/**
 	 * @generated
 	 */
 	private static ImageRegistry getImageRegistry() {
 		if (imageRegistry == null) {
 			imageRegistry = new ImageRegistry();
 		}
 		return imageRegistry;
 	}
 
 	/**
 	 * @generated
 	 */
 	private static String getImageRegistryKey(ENamedElement element) {
 		return element.getName();
 	}
 
 	/**
 	 * @generated
 	 */
 	private static ImageDescriptor getProvidedImageDescriptor(ENamedElement element) {
 		if (element instanceof EStructuralFeature) {
			element = ((EStructuralFeature) element).getEContainingClass();
 		}
 		if (element instanceof EClass) {
 			EClass eClass = (EClass) element;
 			if (!eClass.isAbstract()) {
 				return UMLDiagramEditorPlugin.getInstance().getItemImageDescriptor(eClass.getEPackage().getEFactoryInstance().create(eClass));
 			}
 		}
 		// TODO : support structural features
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static ImageDescriptor getImageDescriptor(ENamedElement element) {
 		String key = getImageRegistryKey(element);
 		ImageDescriptor imageDescriptor = getImageRegistry().getDescriptor(key);
 		if (imageDescriptor == null) {
 			imageDescriptor = getProvidedImageDescriptor(element);
 			if (imageDescriptor == null) {
 				imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
 			}
 			getImageRegistry().put(key, imageDescriptor);
 		}
 		return imageDescriptor;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static Image getImage(ENamedElement element) {
 		String key = getImageRegistryKey(element);
 		Image image = getImageRegistry().get(key);
 		if (image == null) {
 			ImageDescriptor imageDescriptor = getProvidedImageDescriptor(element);
 			if (imageDescriptor == null) {
 				imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
 			}
 			getImageRegistry().put(key, imageDescriptor);
 			image = getImageRegistry().get(key);
 		}
 		return image;
 	}
 
 	/**
 	 * @generated
 	 */
 	public static ImageDescriptor getImageDescriptor(IAdaptable hint) {
 		ENamedElement element = getElement(hint);
 		if (element == null) {
 			return null;
 		}
 		return getImageDescriptor(element);
 	}
 
 	/**
 	 * @generated
 	 */
 	public static Image getImage(IAdaptable hint) {
 		ENamedElement element = getElement(hint);
 		if (element == null) {
 			return null;
 		}
 		return getImage(element);
 	}
 
 	/**
 	 * Returns 'type' of the ecore object associated with the hint.
 	 * 
 	 * @generated
 	 */
 	public static ENamedElement getElement(IAdaptable hint) {
 		Object type = hint.getAdapter(IElementType.class);
 		if (elements == null) {
 			elements = new IdentityHashMap();
 
 			elements.put(StateMachine_1000, UMLPackage.eINSTANCE.getStateMachine());
 
 			elements.put(Region_2001, UMLPackage.eINSTANCE.getRegion());
 
 			elements.put(Pseudostate_2002, UMLPackage.eINSTANCE.getPseudostate());
 
 			elements.put(Pseudostate_2003, UMLPackage.eINSTANCE.getPseudostate());
 
 			elements.put(State_3001, UMLPackage.eINSTANCE.getState());
 
 			elements.put(State_3012, UMLPackage.eINSTANCE.getState());
 
 			elements.put(Region_3002, UMLPackage.eINSTANCE.getRegion());
 
 			elements.put(FinalState_3003, UMLPackage.eINSTANCE.getFinalState());
 
 			elements.put(Pseudostate_3004, UMLPackage.eINSTANCE.getPseudostate());
 
 			elements.put(Pseudostate_3005, UMLPackage.eINSTANCE.getPseudostate());
 
 			elements.put(Pseudostate_3006, UMLPackage.eINSTANCE.getPseudostate());
 
 			elements.put(Pseudostate_3007, UMLPackage.eINSTANCE.getPseudostate());
 
 			elements.put(Pseudostate_3008, UMLPackage.eINSTANCE.getPseudostate());
 
 			elements.put(Pseudostate_3009, UMLPackage.eINSTANCE.getPseudostate());
 
 			elements.put(Pseudostate_3010, UMLPackage.eINSTANCE.getPseudostate());
 
 			elements.put(Pseudostate_3011, UMLPackage.eINSTANCE.getPseudostate());
 
 			elements.put(Transition_4001, UMLPackage.eINSTANCE.getTransition());
 		}
 		return (ENamedElement) elements.get(type);
 	}
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType StateMachine_1000 = getElementType("org.eclipse.uml2.diagram.statemachine.StateMachine_1000"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType Region_2001 = getElementType("org.eclipse.uml2.diagram.statemachine.Region_2001"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType Pseudostate_2002 = getElementType("org.eclipse.uml2.diagram.statemachine.Pseudostate_2002"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType Pseudostate_2003 = getElementType("org.eclipse.uml2.diagram.statemachine.Pseudostate_2003"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType State_3001 = getElementType("org.eclipse.uml2.diagram.statemachine.State_3001"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType State_3012 = getElementType("org.eclipse.uml2.diagram.statemachine.State_3012"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType Region_3002 = getElementType("org.eclipse.uml2.diagram.statemachine.Region_3002"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType FinalState_3003 = getElementType("org.eclipse.uml2.diagram.statemachine.FinalState_3003"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType Pseudostate_3004 = getElementType("org.eclipse.uml2.diagram.statemachine.Pseudostate_3004"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType Pseudostate_3005 = getElementType("org.eclipse.uml2.diagram.statemachine.Pseudostate_3005"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType Pseudostate_3006 = getElementType("org.eclipse.uml2.diagram.statemachine.Pseudostate_3006"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType Pseudostate_3007 = getElementType("org.eclipse.uml2.diagram.statemachine.Pseudostate_3007"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType Pseudostate_3008 = getElementType("org.eclipse.uml2.diagram.statemachine.Pseudostate_3008"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType Pseudostate_3009 = getElementType("org.eclipse.uml2.diagram.statemachine.Pseudostate_3009"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType Pseudostate_3010 = getElementType("org.eclipse.uml2.diagram.statemachine.Pseudostate_3010"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType Pseudostate_3011 = getElementType("org.eclipse.uml2.diagram.statemachine.Pseudostate_3011"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	public static final IElementType Transition_4001 = getElementType("org.eclipse.uml2.diagram.statemachine.Transition_4001"); //$NON-NLS-1$
 
 	/**
 	 * @generated
 	 */
 	private static IElementType getElementType(String id) {
 		return ElementTypeRegistry.getInstance().getType(id);
 	}
 
 	/**
 	 * @generated
 	 */
 	private static Set KNOWN_ELEMENT_TYPES;
 
 	/**
 	 * @generated
 	 */
 	public static boolean isKnownElementType(IElementType elementType) {
 		if (KNOWN_ELEMENT_TYPES == null) {
 			KNOWN_ELEMENT_TYPES = new HashSet();
 			KNOWN_ELEMENT_TYPES.add(StateMachine_1000);
 			KNOWN_ELEMENT_TYPES.add(Region_2001);
 			KNOWN_ELEMENT_TYPES.add(Pseudostate_2002);
 			KNOWN_ELEMENT_TYPES.add(Pseudostate_2003);
 			KNOWN_ELEMENT_TYPES.add(State_3001);
 			KNOWN_ELEMENT_TYPES.add(State_3012);
 			KNOWN_ELEMENT_TYPES.add(Region_3002);
 			KNOWN_ELEMENT_TYPES.add(FinalState_3003);
 			KNOWN_ELEMENT_TYPES.add(Pseudostate_3004);
 			KNOWN_ELEMENT_TYPES.add(Pseudostate_3005);
 			KNOWN_ELEMENT_TYPES.add(Pseudostate_3006);
 			KNOWN_ELEMENT_TYPES.add(Pseudostate_3007);
 			KNOWN_ELEMENT_TYPES.add(Pseudostate_3008);
 			KNOWN_ELEMENT_TYPES.add(Pseudostate_3009);
 			KNOWN_ELEMENT_TYPES.add(Pseudostate_3010);
 			KNOWN_ELEMENT_TYPES.add(Pseudostate_3011);
 			KNOWN_ELEMENT_TYPES.add(Transition_4001);
 		}
 		return KNOWN_ELEMENT_TYPES.contains(elementType);
 	}
 }
