 /*******************************************************************************
  * Copyright (c) 2009, 2011, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.obeonetwork.dsl.sysml.design.services;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.papyrus.sysml.blocks.Block;
 import org.eclipse.papyrus.sysml.blocks.Dimension;
 import org.eclipse.papyrus.sysml.blocks.Unit;
 import org.eclipse.papyrus.sysml.constraints.ConstraintBlock;
 import org.eclipse.papyrus.sysml.constraints.ConstraintProperty;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.uml2.uml.Abstraction;
 import org.eclipse.uml2.uml.Actor;
 import org.eclipse.uml2.uml.Class;
 import org.eclipse.uml2.uml.DataType;
 import org.eclipse.uml2.uml.Element;
 import org.eclipse.uml2.uml.InstanceSpecification;
 import org.eclipse.uml2.uml.Interface;
 import org.eclipse.uml2.uml.NamedElement;
 import org.eclipse.uml2.uml.Package;
 import org.eclipse.uml2.uml.Port;
 import org.eclipse.uml2.uml.Profile;
 import org.eclipse.uml2.uml.Property;
 import org.eclipse.uml2.uml.Stereotype;
 import org.eclipse.uml2.uml.Type;
 import org.obeonetwork.dsl.sysml.design.Activator;
 import org.obeonetwork.dsl.uml2.design.services.EcoreServices;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 
 import fr.obeo.dsl.viewpoint.DDiagram;
 import fr.obeo.dsl.viewpoint.DRepresentation;
 import fr.obeo.dsl.viewpoint.DSemanticDiagram;
 import fr.obeo.dsl.viewpoint.business.api.session.Session;
 import fr.obeo.dsl.viewpoint.business.api.session.SessionManager;
 import fr.obeo.dsl.viewpoint.description.Layer;
 import fr.obeo.dsl.viewpoint.ui.business.api.dialect.DialectEditor;
 import fr.obeo.dsl.viewpoint.ui.business.api.session.IEditingSession;
 import fr.obeo.dsl.viewpoint.ui.business.api.session.SessionUIManager;
 
 /**
  * Utility services for SysML.
  * 
  * @author Axel Richard <a href="mailto:axel.richard@obeo.fr">axel.richard@obeo.fr</a>
  */
 public class SysMLServices {
 
 	/**
 	 * Sysml requirement stereotype.
 	 */
 	private static final String SYSML_REQUIREMENT = "SysML::Requirements::Requirement";
 
 	/**
 	 * Check if a profile is applied on a package based on its qualified name.
 	 * 
 	 * @param currentPackage
 	 *            Package
 	 * @param profileQualifiedName
 	 *            Profile qualified name
 	 * @return True if profile is laready applied otherwised false
 	 */
 	private static Boolean isProfileApplied(Package currentPackage, String profileQualifiedName) {
 		final EList<Profile> allProfiles = currentPackage.getAllAppliedProfiles();
 		final Iterator<Profile> it = allProfiles.iterator();
 		while (it.hasNext()) {
			Profile cur = it.next();
			if (profileQualifiedName.equalsIgnoreCase(cur.getQualifiedName()))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Apply each profiles to the given Package. The first parameter is the package. The second is the profile
 	 * to apply. In case of error when the profile is applied, a message is logged in the activator logger. In
 	 * case of multiple application of the profile, a message is logged in the activator logger.
 	 * 
 	 * @param p
 	 *            : the given Package
 	 * @param profileQualifiedName
 	 *            : the profile qualified name you want to apply.
 	 */
 	private void applySysMLProfile(Package p, String profileQualifiedName) {
 		if (isProfileApplied(p, profileQualifiedName))
 			return;
 		Profile parentProfile = null;
 		if (profileQualifiedName.startsWith("SysML")) {
 			parentProfile = Activator.getSysMLProfile();
 		} else if (profileQualifiedName.startsWith("Standard")) {
 			parentProfile = Activator.getStandardProfile();
 		}
 
 		Package profilePackage = parentProfile;
 
 		final String[] profiles = profileQualifiedName.split(":{2}");
 		// search the profile in the package hierarchy
 		for (int index = 1; index < profiles.length - 1; index++) {
 			profilePackage = profilePackage.getNestedPackage(profiles[index]);
 		}
 
 		Profile profile = (Profile)profilePackage;
 
 		if (profileQualifiedName.startsWith("SysML")) {
 			profile = (Profile)profilePackage.getNestedPackage(profiles[profiles.length - 1]);
 		}
 
 		if (profile == null) {
 			final String message = "Can't apply the profile " + profileQualifiedName + " on "
 					+ p.getQualifiedName();
 			Activator.log(Status.WARNING, message, null);
 		} else {
 			p.applyProfile(profile);
 		}
 	}
 
 	/**
 	 * Create the Associated Stereotype with the given element.
 	 * 
 	 * @param e
 	 *            : the given element for which you want to apply the stereotype.
 	 * @param profileQualifiedName
 	 *            : the qualified name of the stereotype's profile you want to apply (ex. : SysML::Blocks for
 	 *            a Block).
 	 * @param stereotypeName
 	 *            : the name of the stereotype you want to apply.
 	 */
 	public void createAssociatedStereotype(Element e, String profileQualifiedName, String stereotypeName) {
 
 		applySysMLProfile(e.getModel(), profileQualifiedName);
 
 		final Element element = e;
 		final String stereotypeQualifiedName = profileQualifiedName + "::" + stereotypeName;
 
 		final Stereotype stereotype = element.getApplicableStereotype(stereotypeQualifiedName);
 		final EList<Stereotype> appliedStereotypes = element.getAppliedStereotypes();
 
 		if (stereotype == null) {
 			final String message = "Can't apply the setereotype " + stereotypeQualifiedName + " on "
 					+ element.toString();
 			Activator.log(Status.WARNING, message, null);
 		} else if (appliedStereotypes != null && appliedStereotypes.contains(stereotype)) {
 			final String message = "The stereotype " + stereotype.getQualifiedName()
 					+ " is already applied on " + element.toString();
 			Activator.log(Status.INFO, message, null);
 		} else {
 			element.applyStereotype(stereotype);
 		}
 	}
 
 	/**
 	 * Delete the Associated Stereotype with the given element.
 	 * 
 	 * @param e
 	 *            : the given element for which you want to delete the stereotype.
 	 * @param steQualified
 	 *            : the qualified name of the stereotype you want to delete (ex. : SysML::Blocks::Block).
 	 */
 	public void deleteAssociatedStereotype(Element e, String steQualified) {
 		final Element element = e;
 
 		if (element != null && steQualified != null) {
 			final Stereotype stereotype = element.getAppliedStereotype(steQualified);
 			element.unapplyStereotype(stereotype);
 		} else {
 			final String message = "Can't delete the stereotype application because the element or the stereotypeName keys are not correct";
 			Activator.log(Status.INFO, message, null);
 		}
 	}
 
 	/**
 	 * Determines if the given element is an instance of the type.
 	 * 
 	 * @param e
 	 *            : the given Element.
 	 * @param type
 	 *            : the type passed as a String.
 	 * @return true if the given element is an instance of the type, false otherwise.
 	 */
 	public boolean isInstanceOf(Element e, String type) {
 		if (e.eClass().getName().equalsIgnoreCase(type)) {
 			return true;
 		}
 		return false;
 
 	}
 
 	/**
 	 * Set the dimension feature for the given PrimitiveType stereotyped with a ValueType.
 	 * 
 	 * @param pt
 	 *            : the given Element (a PrimitiveType stereotyped with a ValueType).
 	 * @param is
 	 *            : the new Dimension (an InstanceSpecification stereotyped with a Dimension).
 	 */
 	public void setDimensionForPrimitiveType(Element pt, InstanceSpecification is) {
 		if (is != null && pt != null) {
 			final Stereotype valueType = pt.getAppliedStereotype("SysML::Blocks::ValueType");
 			final Dimension newDimension = (Dimension)is.getStereotypeApplication(is
 					.getAppliedStereotype("SysML::Blocks::Dimension"));
 			pt.setValue(valueType, "dimension", newDimension);
 		}
 	}
 
 	/**
 	 * Set the unit feature for the given PrimitiveType stereotyped with a ValueType.
 	 * 
 	 * @param pt
 	 *            : the given Element (a PrimitiveType stereotyped with a ValueType).
 	 * @param is
 	 *            : the new Unit (an InstanceSpecification stereotyped with a Unit).
 	 */
 	public void setUnitForPrimitiveType(Element pt, InstanceSpecification is) {
 		if (is != null && pt != null) {
 			final Stereotype valueType = pt.getAppliedStereotype("SysML::Blocks::ValueType");
 			final Unit newUnit = (Unit)is.getStereotypeApplication(is
 					.getAppliedStereotype("SysML::Blocks::Unit"));
 			pt.setValue(valueType, "unit", newUnit);
 		}
 	}
 
 	/**
 	 * Set the id feature for the given Class stereotyped with a Requirement.
 	 * 
 	 * @param r
 	 *            : the given Element (a Class stereotyped with a Requirement).
 	 * @param id
 	 *            : the new id (a string).
 	 */
 	public void setIdForRequirement(Element r, String id) {
 		if (r != null && id != null) {
 			final Stereotype requirement = r.getAppliedStereotype(SYSML_REQUIREMENT);
 			r.setValue(requirement, "id", id);
 		}
 	}
 
 	/**
 	 * Set the text feature for the given Class stereotyped with a Requirement.
 	 * 
 	 * @param r
 	 *            : the given Element (a Class stereotyped with a Requirement).
 	 * @param text
 	 *            : the new text (a string).
 	 */
 	public void setTextForRequirement(Element r, String text) {
 		if (r != null && text != null) {
 			final Stereotype requirement = r.getAppliedStereotype(SYSML_REQUIREMENT);
 			r.setValue(requirement, "text", text);
 		}
 	}
 
 	/**
 	 * Returns the root container; it may be this object itself.
 	 * 
 	 * @param eObject
 	 *            the object to get the root container for.
 	 * @return the root container.
 	 */
 	public EObject getRootContainer(EObject eObject) {
 		return EcoreUtil.getRootContainer(eObject);
 	}
 
 	/**
 	 * Check if the given layer is activated.
 	 * 
 	 * @param object
 	 *            the semantic diagram
 	 * @param layerID
 	 *            the given layer represented by his ID.
 	 * @return true if the given layer is activated, false otherwise.
 	 */
 	public boolean isLayerActivated(EObject object, String layerID) {
 		if (object instanceof DSemanticDiagram) {
 			final DSemanticDiagram d = (DSemanticDiagram)object;
 			for (Layer layer : d.getActivatedLayers()) {
 				if (layerID.equals(layer.getName())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Get UML String type.
 	 * 
 	 * @param object
 	 *            the object for which to find the corresponding String type
 	 * @return the found String element or null
 	 */
 	public Type getUmlStringType(EObject object) {
 		return EcoreServices.INSTANCE.findTypeByName(object, "String");
 	}
 
 	/**
 	 * Update stereotype of element if needed.
 	 * 
 	 * @param element
 	 *            the given Element.
 	 * @return th element updated.
 	 */
 	public Element updateStereotype(Element element) {
 		if (element instanceof Property) {
 			final Type type = ((Property)element).getType();
 			if (type != null) {
 				final Collection<EObject> elementStereotypes = element.getStereotypeApplications();
 				final Collection<EObject> typeStereotypes = type.getStereotypeApplications();
 				for (EObject typeStereotype : typeStereotypes) {
 					if (typeStereotype instanceof ConstraintBlock) {
 						if (elementStereotypes == null || elementStereotypes.isEmpty()) {
 							createAssociatedStereotype(element, "SysML::Constraints", "ConstraintProperty");
 							break;
 						} else {
 							for (EObject elementStereotype : elementStereotypes) {
 								if (!(elementStereotype instanceof ConstraintProperty)) {
 									createAssociatedStereotype(element, "SysML::Constraints",
 											"ConstraintProperty");
 									break;
 								}
 							}
 						}
 					} else if (typeStereotype instanceof Block) {
 						for (EObject elementStereotype : elementStereotypes) {
 							if (elementStereotype instanceof ConstraintProperty) {
 								deleteAssociatedStereotype(element, "SysML::Constraints::ConstraintProperty");
 								break;
 							}
 						}
 					}
 				}
 			}
 		}
 		return element;
 	}
 
 	/**
 	 * Delete the requirement and his stereotype.
 	 * 
 	 * @param e
 	 *            the Requirement to delete.
 	 */
 	public void deleteRequirement(NamedElement e) {
 		deleteAssociatedStereotype(e, SYSML_REQUIREMENT);
 		final EObject root = getRootContainer(e);
 		for (final Iterator<EObject> iterator = root.eAllContents(); iterator.hasNext();) {
 			final EObject object = iterator.next();
 			if (object instanceof Abstraction) {
 				final Element supplier = ((Abstraction)object).getSupplier(e.getName());
 				if (supplier != null) {
 					Stereotype s = ((Abstraction)object).getAppliedStereotype("SysML::Requirements::Satisfy");
 					if (s != null) {
 						deleteAssociatedStereotype((Abstraction)object, "SysML::Requirements::Satisfy");
 					} else {
 						s = ((Abstraction)object).getAppliedStereotype("SysML::Requirements::DeriveReqt");
 						if (s != null) {
 							deleteAssociatedStereotype((Abstraction)object, "SysML::Requirements::DeriveReqt");
 						} else {
 							s = ((Abstraction)object).getAppliedStereotype("Standard::Refine");
 							if (s != null) {
 								deleteAssociatedStereotype((Abstraction)object, "Standard::Refine");
 							} else {
 								s = ((Abstraction)object).getAppliedStereotype("SysML::Requirements::Verify");
 								if (s != null) {
 									deleteAssociatedStereotype((Abstraction)object,
 											"SysML::Requirements::Verify");
 								}
 							}
 						}
 					}
 					EcoreUtil.delete(object);
 				}
 			}
 		}
 		EcoreUtil.delete(e);
 	}
 
 	/**
 	 * Get all the valid elements for a block definition diagram.
 	 * 
 	 * @param cur
 	 *            Current semantic element
 	 * @return List of elements visible on a block definition diagram
 	 */
 	public List<EObject> getValidsForBlockDefinitionDiagram(EObject cur) {
 		Predicate<EObject> validForDiagram = new Predicate<EObject>() {
 
 			public boolean apply(EObject input) {
 
 				return "Model".equals(input.eClass().getName())
 						|| "Package".equals(input.eClass().getName())
 						|| input instanceof Interface
 						|| (input instanceof InstanceSpecification && hasStereotype(
 								(InstanceSpecification)input, "Unit"))
 						|| (input instanceof InstanceSpecification && hasStereotype(
 								(InstanceSpecification)input, "Dimension")) || input instanceof DataType
 						|| input instanceof Actor
 						|| (input instanceof Class && hasStereotype((Class)input, "Block"))
 						|| (input instanceof Class && hasStereotype((Class)input, "ConstraintBlock"));
 			}
 		};
 		return allValidSessionElements(cur, validForDiagram);
 	}
 
 	/**
 	 * Get all the valid elements for an internal block diagram.
 	 * 
 	 * @param cur
 	 *            Current semantic element
 	 * @return List of elements visible on an internal block diagram
 	 */
 	public List<EObject> getValidsForInternalBlockDiagram(Class cur) {
 		Predicate<EObject> validForDiagram = new Predicate<EObject>() {
 
 			public boolean apply(EObject input) {
 				return !(input instanceof Port) && (input instanceof Property);
 			}
 		};
 		return allValidAttributes(cur, validForDiagram);
 	}
 
 	/**
 	 * Get all the valid elements for an internal block diagram.
 	 * 
 	 * @param cur
 	 *            Current semantic element
 	 * @return List of elements visible on an internal block diagram
 	 */
 	public List<EObject> getValidsForParametricBlockDiagram(Class cur) {
 		final boolean isValueBindingLayerActive = isValueBindingLayerActive(cur);
 		Predicate<EObject> validForDiagram = new Predicate<EObject>() {
 
 			public boolean apply(EObject input) {
 				if (!isValueBindingLayerActive) {
 					return !(input instanceof Port) && (input instanceof Property)
 							&& hasStereotype((Element)input, "ConstraintProperty");
 				} else {
 					return !(input instanceof Port) && (input instanceof Property);
 				}
 			}
 		};
 		return allValidAttributes(cur, validForDiagram);
 	}
 
 	private boolean isValueBindingLayerActive(EObject cur) {
 		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
 				.getActiveEditor();
 
 		if (editor instanceof DialectEditor) {
 			DialectEditor dialectEditor = (DialectEditor)editor;
 			DRepresentation representation = dialectEditor.getRepresentation();
 			if (representation instanceof DDiagram) {
 				DDiagram diagram = (DDiagram)representation;
 				List<Layer> layers = diagram.getActivatedLayers();
 				for (Layer layer : layers) {
 					if ("PAR ValueBinding Layer".equals(layer.getName()))
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Get all the valid elements for an internal block diagram.
 	 * 
 	 * @param cur
 	 *            Current semantic element
 	 * @return List of elements visible on an internal block diagram
 	 */
 	public List<EObject> getValidsForParametricBlockDiagramValueBindingFilter(Class cur) {
 		Predicate<EObject> validForDiagram = new Predicate<EObject>() {
 
 			public boolean apply(EObject input) {
 				return !(input instanceof Port) && (input instanceof Property)
 						&& !hasStereotype((Element)input, "ConstraintProperty");
 			}
 		};
 		return allValidAttributes(cur, validForDiagram);
 	}
 
 	/**
 	 * Check if an UML element has a stereotype defined as parameter.
 	 * 
 	 * @param element
 	 *            UML element
 	 * @param stereotype
 	 *            Stereotype name to check
 	 * @return True if the UML element has the given stereotype
 	 */
 	private boolean hasStereotype(Element element, String stereotypeName) {
 		for (EObject stereotype : element.getStereotypeApplications()) {
 			if (stereotypeName.equals(stereotype.eClass().getName()))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Get all valid elements in session.
 	 * 
 	 * @param cur
 	 *            Current element
 	 * @param validForDiagram
 	 *            Predicate
 	 * @return List of valid elements
 	 */
 	private List<EObject> allValidSessionElements(EObject cur, Predicate<EObject> validForDiagram) {
 		Session found = SessionManager.INSTANCE.getSession(cur);
 		List<EObject> result = Lists.newArrayList();
 		if (found != null) {
 			for (Resource res : found.getSemanticResources()) {
 				Iterators.addAll(result, Iterators.filter(res.getAllContents(), validForDiagram));
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Get all valid attributes of a class.
 	 * 
 	 * @param cur
 	 *            Current element
 	 * @param validForDiagram
 	 *            Predicate
 	 * @return List of valid elements
 	 */
 	private List<EObject> allValidAttributes(Class cur, Predicate<EObject> validForDiagram) {
 		List<EObject> result = Lists.newArrayList();
 		Iterators.addAll(result, Iterators.filter(cur.getAttributes().iterator(), validForDiagram));
 		return result;
 	}
 }
