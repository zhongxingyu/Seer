 package org.tuml.javageneration.util;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.ocl.uml.CollectionType;
 import org.eclipse.uml2.uml.Association;
 import org.eclipse.uml2.uml.BehavioredClassifier;
 import org.eclipse.uml2.uml.Class;
 import org.eclipse.uml2.uml.Classifier;
 import org.eclipse.uml2.uml.Enumeration;
 import org.eclipse.uml2.uml.Generalization;
 import org.eclipse.uml2.uml.Interface;
 import org.eclipse.uml2.uml.InterfaceRealization;
 import org.eclipse.uml2.uml.Property;
 import org.eclipse.uml2.uml.Type;
 import org.eclipse.uml2.uml.VisibilityKind;
 import org.eclipse.uml2.uml.internal.operations.ClassOperations;
 import org.opaeum.java.metamodel.OJPathName;
 import org.opaeum.java.metamodel.OJVisibilityKind;
 import org.tuml.framework.ModelLoader;
 
 public class TumlClassOperations extends ClassOperations {
 
 	public static boolean hasLookupProperty(org.eclipse.uml2.uml.Class clazz) {
 		Set<Property> properties = getAllOwnedProperties(clazz);
 		for (Property property : properties) {
 			if (new PropertyWrapper(property).hasLookup()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static Set<Property> getChildPropertiesToDelete(org.eclipse.uml2.uml.Class clazz) {
 		Set<Property> result = new HashSet<Property>();
 		Set<Property> ownedProperties = getAllOwnedProperties(clazz);
 		for (Property p : ownedProperties) {
 			PropertyWrapper pWrap = new PropertyWrapper(p);
 			if (!pWrap.isDerived() && (pWrap.isComposite() || (!pWrap.isPrimitive() && !pWrap.isEnumeration() && pWrap.getOtherEnd() == null))) {
 				result.add(p);
 			}
 		}
 		return result;
 	}
 
 	public static Set<Property> getPropertiesToClearOnDeletion(org.eclipse.uml2.uml.Class clazz) {
 		Set<Property> result = new HashSet<Property>();
 		Set<Property> ownedProperties = getAllOwnedProperties(clazz);
 		for (Property p : ownedProperties) {
 			PropertyWrapper pWrap = new PropertyWrapper(p);
 			if (!pWrap.isDerived() && !pWrap.isComposite() && !pWrap.isPrimitive() && !pWrap.isEnumeration()) {
 				result.add(p);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Returns all properties that are to be persisted on the clazz's vertex.
 	 * i.e. only simple types and enums
 	 */
 	public static Set<Property> getOnePrimitiveOrEnumProperties(org.eclipse.uml2.uml.Class clazz) {
 		Set<Property> result = new HashSet<Property>();
 		for (Property p : clazz.getAttributes()) {
 			PropertyWrapper pWrap = new PropertyWrapper(p);
 			if (!pWrap.isDerived() && pWrap.isOne() && (pWrap.isPrimitive() || pWrap.isEnumeration())) {
 				result.add(p);
 			}
 		}
 		return result;
 	}
 
 	public static Set<Property> getPrimitiveOrEnumOrComponentsProperties(org.eclipse.uml2.uml.Class clazz) {
 		Set<Property> result = new HashSet<Property>();
 		for (Property p : getAllOwnedProperties(clazz)) {
 			PropertyWrapper pWrap = new PropertyWrapper(p);
 			if ((pWrap.isOne() && !pWrap.isDerived() && !pWrap.isQualifier())
					|| (!pWrap.isDerived() && pWrap.isMany() && (pWrap.isPrimitive() || pWrap.isEnumeration())) ||
                     (pWrap.isComponent()))  {
 				result.add(p);
 			}
 		}
 		return result;
 	}
 
 	public static Set<Property> getPrimitiveOrEnumOrComponentsPropertiesExcludingCompositeParent(org.eclipse.uml2.uml.Class clazz) {
 		Set<Property> result = new HashSet<Property>();
 		for (Property p : getAllOwnedProperties(clazz)) {
 			PropertyWrapper pWrap = new PropertyWrapper(p);
			if (!pWrap.isDerived() && ((pWrap.isOne() && !pWrap.isQualifier()) || (pWrap.isMany() && (pWrap.isPrimitive() || pWrap.isEnumeration())))) {
 				// Exclude the composite parent
 				if (!(pWrap.getOtherEnd() != null && pWrap.getOtherEnd().isComposite())) {
 					result.add(p);
 				}
 			}
 		}
 		return result;
 	}
 
 	/*
 	 * These include all properties that are on the other end of an association.
 	 * It does not include inherited properties
 	 */
 	public static Set<Property> getAllOwnedProperties(org.eclipse.uml2.uml.Class clazz) {
 		Set<Property> result = new HashSet<Property>(clazz.getAttributes());
 		List<Association> associations = clazz.getAssociations();
 		for (Association association : associations) {
 			List<Property> memberEnds = association.getMemberEnds();
 			for (Property property : memberEnds) {
 				if (property.getType() != clazz) {
 					result.add(property);
 				}
 			}
 		}
 		result.addAll(getPropertiesOnRealizedInterfaces(clazz));
 		return result;
 	}
 
 	/*
 	 * These include all properties that are on the other end of an association.
 	 * It includes inherited properties
 	 */
 	public static Set<Property> getAllProperties(org.eclipse.uml2.uml.Class clazz) {
 		Set<Property> result = new HashSet<Property>(clazz.getAllAttributes());
 		Set<Association> associations = getAllAssociations(clazz);
 		for (Association association : associations) {
 			List<Property> memberEnds = association.getMemberEnds();
 			Property memberEnd1 = memberEnds.get(0);
 			Property memberEnd2 = memberEnds.get(1);
 
 			// This is for the case of hierarchies, i.e association to itself
 			if (isSpecializationOf(clazz, memberEnd1.getType()) && isSpecializationOf(clazz, memberEnd2.getType())) {
 				result.add(memberEnd1);
 				result.add(memberEnd2);
 			}
 			// This is to prevent getting the near side of an association
 			if (!isSpecializationOf(clazz, memberEnd1.getType())) {
 				result.add(memberEnd1);
 			}
 			// This is to prevent getting the near side of an association
 			if (!isSpecializationOf(clazz, memberEnd2.getType())) {
 				result.add(memberEnd2);
 			}
 
 			// for (Property property : memberEnds) {
 			// if (!isSpecializationOf(clazz, property.getType())) {
 			// result.add(property);
 			// }
 			// }
 		}
 		result.addAll(getPropertiesOnRealizedInterfaces(clazz));
 		return result;
 	}
 
 	public static Set<Property> getPropertiesOnRealizedInterfaces(org.eclipse.uml2.uml.Class clazz) {
 		Set<Property> result = new HashSet<Property>();
 		List<Interface> interfaces = clazz.getImplementedInterfaces();
 		for (Interface inf : interfaces) {
 			Set<Property> properties = TumlInterfaceOperations.getAllProperties(inf);
 			for (Property p : properties) {
 				result.add(p);
 			}
 		}
 		return result;
 	}
 
 	public static Property getOtherEndToComposite(Classifier classifier) {
 		Set<Association> associations = getAllAssociations(classifier);
 		for (Association association : associations) {
 			List<Property> memberEnds = association.getMemberEnds();
 			for (Property property : memberEnds) {
 				if (!property.isComposite() && property.getType() != classifier && property.getOtherEnd().isComposite()
 						&& isSpecializationOf(classifier, property.getOtherEnd().getType())) {
 					return property;
 				}
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * Only BehavioredClassifier can realize interfaces
 	 */
 	public static boolean isSpecializationOf(Classifier special, Type type) {
 		if (special == type) {
 			return true;
 		}
 		if ((special instanceof BehavioredClassifier) && ((BehavioredClassifier) special).getAllImplementedInterfaces().contains(type)) {
 			return true;
 		}
 		for (Classifier general : special.getGenerals()) {
 			if (isSpecializationOf(general, type)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static Set<Association> getAllAssociations(Classifier classifier) {
 		Set<Association> result = new HashSet<Association>();
 		if (classifier instanceof Class) {
 			for (Interface implementedInterface : ((Class) classifier).getAllImplementedInterfaces()) {
 				result.addAll(implementedInterface.getAssociations());
 			}
 		}
 		getAllAssociationsFromGenerals(classifier, result);
 		return result;
 	}
 
 	public static void getAllAssociationsFromGenerals(Classifier classifier, Set<Association> result) {
 		result.addAll(classifier.getAssociations());
 		for (Classifier general : classifier.getGenerals()) {
 			getAllAssociationsFromGenerals(general, result);
 		}
 	}
 
 	public static OJPathName getOtherEndToCompositePathName(Class clazz) {
 		Property endToComposite = getOtherEndToComposite(clazz);
 		if (endToComposite != null) {
 			return getPathName(endToComposite.getType());
 		} else {
 			return null;
 		}
 	}
 
 	public static OJPathName getPathName(Type type) {
 		String className = Namer.name(type);
 		String fullPackageName = Namer.name(type.getNearestPackage());
 		OJPathName ojPathName = new OJPathName(fullPackageName + "." + className);
 		return ojPathName;
 	}
 
 	public static boolean hasSupertype(Class clazz) {
 		return !getConcreteGenerals(clazz).isEmpty();
 	}
 
 	public static List<Class> getConcreteGenerals(Classifier clazz) {
 		List<Class> result = new ArrayList<Class>();
 		List<Classifier> generals = clazz.getGenerals();
 		for (Classifier classifier : generals) {
 			if (classifier instanceof Class) {
 				result.add((Class) classifier);
 			}
 		}
 		return result;
 	}
 
 	private static void getConcreteImplementations(Set<Classifier> result, Classifier clazz) {
 		if (!clazz.isAbstract()) {
 			result.add(clazz);
 		}
 		List<Generalization> generalizations = ModelLoader.getSpecifics(clazz);
 		for (Generalization generalization : generalizations) {
 			Classifier specific = generalization.getSpecific();
 			getConcreteImplementations(result, specific);
 		}
 	}
 
 	private static void getSpecializations(Set<Classifier> result, Classifier clazz) {
 		result.add(clazz);
 		List<Generalization> generalizations = ModelLoader.getSpecifics(clazz);
 		for (Generalization generalization : generalizations) {
 			Classifier specific = generalization.getSpecific();
 			getSpecializations(result, specific);
 		}
 	}
 
 	public static Set<Classifier> getRealizationWithCompositeOwner(Interface inf) {
 		Set<Classifier> result = new HashSet<Classifier>();
 		List<InterfaceRealization> interfaceRealizations = ModelLoader.getInterfaceRealization(inf);
 		for (InterfaceRealization interfaceRealization : interfaceRealizations) {
 			BehavioredClassifier c = interfaceRealization.getImplementingClassifier();
 			if (hasCompositeOwner(c)) {
 				result.add(c);
 			}
 		}
 		return result;
 	}
 
 	public static Set<Classifier> getRealizationWithoutCompositeOwner(Interface inf) {
 		Set<Classifier> result = new HashSet<Classifier>();
 		List<InterfaceRealization> interfaceRealizations = ModelLoader.getInterfaceRealization(inf);
 		for (InterfaceRealization interfaceRealization : interfaceRealizations) {
 			BehavioredClassifier c = interfaceRealization.getImplementingClassifier();
 			if (!hasCompositeOwner(c)) {
 				result.add(c);
 			}
 		}
 		return result;
 	}
 
 	public static Set<Classifier> getSpecializationWithCompositeOwner(Classifier clazz) {
 		Set<Classifier> result = new HashSet<Classifier>();
 		Set<Classifier> specializations = getSpecializations(clazz);
 		for (Classifier c : specializations) {
 			if (hasCompositeOwner(c)) {
 				result.add(c);
 			}
 		}
 		return result;
 	}
 
 	public static Set<Classifier> getConcreteSpecializationsWithoutCompositeOwner(Classifier clazz) {
 		Set<Classifier> result = new HashSet<Classifier>();
 		Set<Classifier> specializations = getSpecializations(clazz);
 		for (Classifier c : specializations) {
 			if (!c.isAbstract() && c instanceof Class && !hasCompositeOwner((Class) c)) {
 				result.add((Class) c);
 			}
 		}
 		return result;
 	}
 
 	public static Set<Classifier> getSpecializations(Classifier clazz) {
 		Set<Classifier> result = new HashSet<Classifier>();
 		getSpecializations(result, clazz);
 		return result;
 	}
 
 	public static Set<Classifier> getConcreteImplementations(Classifier clazz) {
 		Set<Classifier> result = new HashSet<Classifier>();
 		getConcreteImplementations(result, clazz);
 		return result;
 	}
 
 	public static boolean hasCompositeOwner(Classifier classifier) {
 		return getOtherEndToComposite(classifier) != null;
 	}
 
 	public static String className(Classifier clazz) {
 		if (clazz instanceof CollectionType) {
 			CollectionType collectionType = (CollectionType) clazz;
 			StringBuilder sb = new StringBuilder();
 			sb.append(TumlCollectionKindEnum.from(collectionType.getKind()).getOjPathName().getLast());
 			sb.append("<");
 			sb.append(className(collectionType.getElementType()));
 			sb.append(">");
 			return sb.toString();
 		} else {
 			return Namer.name(clazz);
 		}
 	}
 
 	public static String propertyEnumName(Type type) {
 		return Namer.name(type) + "RuntimePropertyEnum";
 	}
 
 	public static OJVisibilityKind getVisibility(VisibilityKind visibility) {
 		switch (visibility) {
 		case PRIVATE_LITERAL:
 			return OJVisibilityKind.PRIVATE;
 		case PROTECTED_LITERAL:
 			return OJVisibilityKind.PROTECTED;
 		case PUBLIC_LITERAL:
 			return OJVisibilityKind.PUBLIC;
 		case PACKAGE_LITERAL:
 			return OJVisibilityKind.DEFAULT;
 		default:
 			throw new RuntimeException("Not supported");
 		}
 	}
 
 	public static boolean isRoot(Class clazz) {
 		return false;
 	}
 
 	public static OJPathName getAuditPathName(Class c) {
 		OJPathName pathName = getPathName(c);
 		return pathName.renameLast(pathName.getLast() + "Audit");
 	}
 
 	public static Property getAttribute(Class c, String name) {
 		for (Property p : c.getAllAttributes()) {
 			if (p.getName().equals(name)) {
 				return p;
 			}
 		}
 		return null;
 	}
 
 	public static boolean isOnInterface(PropertyWrapper pWrap) {
 		return pWrap.getOwningType() instanceof Interface;
 	}
 
 	public static List<Property> getPropertiesForToJson(Class clazz) {
 		return clazz.getAttributes();
 	}
 
 	public static boolean isHierarchy(org.eclipse.uml2.uml.Class clazz) {
 		if (realizesHierarchy(clazz)) {
 			return true;
 		}
 		List<Classifier> generals = getGenerals(clazz);
 		for (Classifier general : generals) {
 			if (realizesHierarchy((Class) general)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private static boolean realizesHierarchy(org.eclipse.uml2.uml.Class clazz) {
 		List<Interface> realizedInterfaces = clazz.getImplementedInterfaces();
 		for (Interface interface1 : realizedInterfaces) {
 			if (interface1.getQualifiedName().equals("tumllib::org::tuml::hierarchy::Hierarchy")) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static boolean isEnumeration(Type owningType) {
 		return owningType instanceof Enumeration;
 	}
 
 }
