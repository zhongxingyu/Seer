 package gov.nih.nci.cagrid.data.cql2.validation.walker;
 
 import gov.nih.nci.cagrid.data.cql.validation.DomainConformanceException;
 import gov.nih.nci.cagrid.data.utilities.DomainModelUtils;
 import gov.nih.nci.cagrid.metadata.common.Enumeration;
 import gov.nih.nci.cagrid.metadata.common.UMLAttribute;
 import gov.nih.nci.cagrid.metadata.common.ValueDomain;
 import gov.nih.nci.cagrid.metadata.common.ValueDomainEnumerationCollection;
 import gov.nih.nci.cagrid.metadata.dataservice.DomainModel;
 import gov.nih.nci.cagrid.metadata.dataservice.UMLAssociation;
 import gov.nih.nci.cagrid.metadata.dataservice.UMLClass;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cagrid.cql2.AssociationPopulationSpecification;
 import org.cagrid.cql2.AttributeValue;
 import org.cagrid.cql2.CQLAssociatedObject;
 import org.cagrid.cql2.CQLAttribute;
 import org.cagrid.cql2.CQLGroup;
 import org.cagrid.cql2.CQLObject;
 import org.cagrid.cql2.CQLQuery;
 import org.cagrid.cql2.CQLQueryModifier;
 import org.cagrid.cql2.CQLTargetObject;
 import org.cagrid.cql2.DistinctAttribute;
 import org.cagrid.cql2.GroupLogicalOperator;
 import org.cagrid.cql2.NamedAssociation;
 import org.cagrid.cql2.NamedAssociationList;
 import org.cagrid.cql2.NamedAttribute;
 import org.cagrid.cql2.PopulationDepth;
 
 public class Cql2WalkerDomainModelValidationHandler extends Cql2WalkerHandlerAdapter {
     
     private static Log LOG = LogFactory.getLog(Cql2WalkerDomainModelValidationHandler.class);
     
     private DomainModel model = null;
     
     private CQLTargetObject target = null;
     private Stack<CQLObject> objectStack = null;
     private Stack<UMLClass> populatedAssociationStack = null;
     private UMLClass currentPopulatedAssociation = null;
     private UMLAttribute currentAttribute = null;
     
     public Cql2WalkerDomainModelValidationHandler(DomainModel model) {
         this.model = model;
         this.objectStack = new Stack<CQLObject>();
         this.populatedAssociationStack = new Stack<UMLClass>();
     }
     
 
     public void endAssociation(CQLAssociatedObject assoc) throws DomainConformanceException {
         objectStack.pop();
     }
 
 
     public void endAttribute(CQLAttribute attrib) throws DomainConformanceException {
         currentAttribute = null;
     }
 
 
     public void endTargetObject(CQLTargetObject obj) throws DomainConformanceException {
         objectStack.pop();
         // should be all out of objects now...
         if (objectStack.size() != 0) {
             throw new DomainConformanceException(objectStack.size() + " unaccounted for object(s) left on stack!");
         }
     }
 
 
     public void startAssociation(CQLAssociatedObject assoc) throws DomainConformanceException {
         // verify the associated object is valid
         UMLClass classMd = getUmlClass(assoc.getClassName());
         if (classMd == null) {
             throw new DomainConformanceException("No data type " + assoc.getClassName() 
                 + " could be found in the domain model");
         }
         // verify such an association exists
         boolean associationExists = associationExists(
             objectStack.peek().getClassName(), assoc.getClassName(), assoc.getEndName());
         if (!associationExists) {
             throw new DomainConformanceException("No association from " + objectStack.peek().getClassName() + " to " 
                 + assoc.getClassName() + " with end name " + assoc.getEndName());
         }
         // verify the instanceof attribute is valid
         if (assoc.get_instanceof() != null) {
             boolean validInstanceof = isInstanceof(assoc.getClassName(), assoc.get_instanceof());
             if (!validInstanceof) {
                 throw new DomainConformanceException(
                     assoc.get_instanceof() + " is not a subclass of " + assoc.getClassName());
             }
         }
         // push the association on the stack
         objectStack.push(assoc);
     }
 
 
     public void startAssociationPopulation(AssociationPopulationSpecification pop) throws DomainConformanceException {
         // TODO Auto-generated method stub
 
     }
 
 
     public void startAttribute(CQLAttribute attrib) throws DomainConformanceException {
         // verify the attribute exists
         CQLObject parent = objectStack.peek();
         String parentClassname = parent.get_instanceof() != null ? 
             parent.get_instanceof() : parent.getClassName();
         UMLClass classMd = getUmlClass(parentClassname);
         UMLAttribute attribMd = getUmlAttribute(attrib.getName(), parentClassname);
         if (attribMd == null) {
             throw new DomainConformanceException("Attribute '" + attrib.getName() + "' is not defined for the class "
                 + classMd.getClassName() + " or any of its parent classes");
         }
         this.currentAttribute = attribMd;
     }
 
 
     public void startAttributeValue(AttributeValue val) throws DomainConformanceException {
         // verify the data type being used is compatible
         String datatype = currentAttribute.getDataTypeName();
         String valueAsString = null;
         if (datatype == null) {
             // can't validate anything if there isn't a data type
             LOG.warn("No datatype defined for attribute " + currentAttribute.getName());
         } else {
             // verify the proper typed value is populated
             if (String.class.getName().equals(datatype)) {
                 if (val.getStringValue() == null) {
                     throw new DomainConformanceException("Attribute " + currentAttribute.getName() 
                         + " was expected to have a value of type " + String.class.getName() + ", but did not");
                 } else {
                     valueAsString = val.getStringValue();
                 }
             } else if (Integer.class.getName().equals(datatype)) {
                 if (val.getIntegerValue() == null) {
                     throw new DomainConformanceException("Attribute " + currentAttribute.getName() 
                         + " was expected to have a value of type " + Integer.class.getName() + ", but did not");
                 } else {
                     valueAsString = val.getIntegerValue().toString();
                 }
             } else if (Long.class.getName().equals(datatype)) {
                 if (val.getLongValue() == null) {
                     throw new DomainConformanceException("Attribute " + currentAttribute.getName() 
                         + " was expected to have a value of type " + Long.class.getName() + ", but did not");
                 } else {
                     valueAsString = val.getLongValue().toString();
                 }
             } else if (Date.class.getName().equals(datatype)) {
                 if (val.getDateValue() == null) {
                     throw new DomainConformanceException("Attribute " + currentAttribute.getName() 
                         + " was expected to have a value of type " + Date.class.getName() + ", but did not");
                 } else {
                     valueAsString = DateFormat.getDateInstance().format(val.getDateValue());                    
                 }
             } else if (Boolean.class.getName().equals(datatype)) {
                 if (val.getBooleanValue() == null) {
                     throw new DomainConformanceException("Attribute " + currentAttribute.getName() 
                         + " was expected to have a value of type " + Boolean.class.getName() + ", but did not");
                 } else {
                     valueAsString = val.getBooleanValue().toString();
                 }
             } else if (Double.class.getName().equals(datatype)) {
                 if (val.getDoubleValue() == null) {
                     throw new DomainConformanceException("Attribute " + currentAttribute.getName() 
                         + " was expected to have a value of type " + Double.class.getName() + ", but did not");
                 } else {
                     valueAsString = val.getDoubleValue().toString();
                 }
             }
             //check enumeration values
             ValueDomain valueDomain = currentAttribute.getValueDomain();
             if (valueDomain != null) {
                 ValueDomainEnumerationCollection enumerationCollection = valueDomain.getEnumerationCollection();
                 if (enumerationCollection != null && enumerationCollection.getEnumeration() != null
                     && enumerationCollection.getEnumeration().length > 0) {
                     Enumeration[] enumeration = enumerationCollection.getEnumeration();
                     Set<String> permValues = new HashSet<String>();
                     for (int i = 0; i < enumeration.length; i++) {
                         Enumeration e = enumeration[i];
                         permValues.add(e.getPermissibleValue());
                     }
                     if (!permValues.contains(valueAsString)) {
                         throw new DomainConformanceException("Attribute '" + currentAttribute.getName()
                             + "' defines a permissible value enumeration, and the value'" + valueAsString
                             + "' is not permissible.");
                     }
                 }
             }
         }
     }
 
 
     public void startDistinctAttribute(DistinctAttribute distinct) throws DomainConformanceException {
         String parentClassname = target.get_instanceof() != null ? 
             target.get_instanceof() : target.getClassName();
         UMLAttribute attrib = getUmlAttribute(distinct.getAttributeName(), parentClassname);
         if (attrib == null) {
             throw new DomainConformanceException("Distinct attribute " + distinct.getAttributeName() 
                 + " not found for target class " + parentClassname);
         }
     }
 
 
     public void startGroup(CQLGroup group) throws DomainConformanceException {
         // check out the logical operation
         GroupLogicalOperator op = group.getLogicalOperation();
         if (op == null) {
             throw new DomainConformanceException("Group logical operator cannot be null");
         }
         if (!GroupLogicalOperator.AND.equals(op) && !GroupLogicalOperator.OR.equals(op)) {
             throw new DomainConformanceException("Group logical operator " + op.getValue() + " is not valid");
         }
     }
 
 
     public void startNamedAttribute(NamedAttribute named) throws DomainConformanceException {
         String parentClassname = target.get_instanceof() != null ? 
             target.get_instanceof() : target.getClassName();
         UMLAttribute attrib = getUmlAttribute(named.getAttributeName(), parentClassname);
         if (attrib == null) {
             throw new DomainConformanceException("Named attribute " + named.getAttributeName() 
                 + " not found for target class " + parentClassname);
         }
     }
     
     
     public void startQuery(CQLQuery query) throws DomainConformanceException {
         // re-initialize all state variables
         this.target = null;
         this.objectStack.clear();
         this.populatedAssociationStack.clear();
         this.currentAttribute = null;
     }
 
 
     public void startQueryModifier(CQLQueryModifier mods) throws DomainConformanceException {
         // TODO Auto-generated method stub
 
     }
 
 
     public void startTargetObject(CQLTargetObject obj) throws DomainConformanceException {
         UMLClass targetClass = getUmlClass(obj.getClassName());
         if (targetClass == null) {
             throw new DomainConformanceException("Query target " + obj.getClassName()
                 + " was not found in the domain model");
         } else if (!targetClass.isAllowableAsTarget()) {
             throw new DomainConformanceException("Query target " + obj.getClassName() 
                 + " is not allowed as a target in the domain model");
         }
         // verify the instanceof attribute is valid
         if (obj.get_instanceof() != null) {
             boolean validInstanceof = isInstanceof(obj.getClassName(), obj.get_instanceof());
             if (!validInstanceof) {
                 throw new DomainConformanceException(
                     obj.get_instanceof() + " is not a subclass of " + obj.getClassName());
             }
         }
         // keep the state
         this.target = obj;
         this.objectStack.push(obj);
     }
     
     
     public void startNamedAssociationList(NamedAssociationList list) throws DomainConformanceException {
         if (populatedAssociationStack.size() == 0) {
             // push the target object on the top of the stack
             String parentClassname = target.get_instanceof() != null ? 
                 target.get_instanceof() : target.getClassName();
             populatedAssociationStack.push(getUmlClass(parentClassname));
         } else {
             populatedAssociationStack.push(currentPopulatedAssociation);
         }
     }
     
     
     public void endNamedAssociationList(NamedAssociationList list) throws DomainConformanceException {
         populatedAssociationStack.pop();
     }
     
     
     public void startNamedAssociation(NamedAssociation assoc) throws DomainConformanceException {
         UMLClass parentClass = populatedAssociationStack.peek();
         String parentClassName = DomainModelUtils.getQualifiedClassname(parentClass);
         SimplifiedUmlAssociation foundAssociation = null;
         List<SimplifiedUmlAssociation> simpleAssociations = getUmlAssociations(parentClassName);
         for (SimplifiedUmlAssociation simple : simpleAssociations) {
             if (!simple.isBidirectional() && simple.getSourceRoleName().equals(assoc.getEndName())) {
                 foundAssociation = simple;
                 break;
             }
         }
         if (foundAssociation == null) {
             // association not found
             throw new DomainConformanceException("Association from " + parentClassName 
                 + " via role name " + assoc.getEndName() + " not found");
         }
         String associatedClassname = foundAssociation.getSourceRoleName().equals(assoc.getEndName()) 
             ? foundAssociation.getSourceClass() : foundAssociation.getTargetClass();
         if (assoc.get_instanceof() != null && !(assoc.get_instanceof().equals(associatedClassname))) {
             if (!isInstanceof(associatedClassname, assoc.get_instanceof())) {
                 throw new DomainConformanceException(assoc.get_instanceof() + " is not a valid subclass of " + associatedClassname);
             }
         }
         UMLClass associatedClass = getUmlClass(associatedClassname);
         currentPopulatedAssociation = associatedClass;
     }
     
     
     public void endNamedAssociation(NamedAssociation assoc) throws DomainConformanceException {
         
     }
     
     
     public void startPopulationDepth(PopulationDepth depth) throws DomainConformanceException {
         
     }
     
     
     public void endPopulationDepth(PopulationDepth depth) throws DomainConformanceException {
         
     }
     
     
     private UMLClass getUmlClass(String className) {
         UMLClass[] allClasses = model.getExposedUMLClassCollection().getUMLClass();
 
         for (int i = 0; allClasses != null && i < allClasses.length; i++) {
             String fqn = allClasses[i].getPackageName().trim();
             if (!fqn.equals("")) {
                 fqn += "." + allClasses[i].getClassName();
             }
 
             if (fqn.equals(className)) {
                 return allClasses[i];
             }
         }
         return null;
     }
     
     
     private UMLAttribute getUmlAttribute(String attribName, String classname) {
         UMLClass classMd = getUmlClass(classname);
         List<UMLAttribute> attribs = new LinkedList<UMLAttribute>();
         // start with attributes of the queried-for class, then try the base class hierarchy
         if (classMd.getUmlAttributeCollection() != null && classMd.getUmlAttributeCollection().getUMLAttribute() != null) {
             Collections.addAll(attribs, classMd.getUmlAttributeCollection().getUMLAttribute());
         }
         for (UMLClass baseClass : DomainModelUtils.getAllSuperclasses(model, classname)) {
             if (baseClass.getUmlAttributeCollection() != null && baseClass.getUmlAttributeCollection().getUMLAttribute() != null) {
                 Collections.addAll(attribs, baseClass.getUmlAttributeCollection().getUMLAttribute());
             }
         }
         for (UMLAttribute attrib : attribs) {
             String fullAttribName = attrib.getName();
             int shortIndex = fullAttribName.indexOf(':');
             String shortAttribName = fullAttribName.substring(shortIndex + 1);
             if (shortAttribName.equals(attribName)) {
                 return attrib;
             }
         }
         return null;
     }
     
     
     private boolean associationExists(String sourceClassName, String targetClassName, String endName) {
         Set<SimplifiedUmlAssociation> associations = getAllAssociationsInvolvingClass(sourceClassName);
         boolean associationFound = false;
         for (SimplifiedUmlAssociation association : associations) {
            // verify both ends of the association are right
            // starting with source to target
            if (sourceClassName.equals(association.getSourceClass()) &&
                targetClassName.equals(association.getTargetClass())) {
                // ensure the role name matches
                associationFound = association.getTargetRoleName().equals(endName);
            }
            if (association.isBidirectional() && !associationFound) {
                // if bidirectional and we've not already found the association, try the reverse
                if (targetClassName.equals(association.getSourceClass()) &&
                    sourceClassName.equals(association.getTargetClass())) {
                    associationFound = association.getSourceRoleName().equals(endName);
                 }
             }
             if (associationFound) {
                 break;
             }
         }
         return associationFound;
     }
     
     
     private boolean isInstanceof(String baseClass, String testClass) {
         boolean validInstanceof = false;
         String[] superclasses = getSuperclassNames(testClass);
         for (String sup : superclasses) {
             if (sup.equals(baseClass)) {
                 validInstanceof = true;
                 break;
             }
         }
         return validInstanceof;
     }
     
     
     private Set<SimplifiedUmlAssociation> getAllAssociationsInvolvingClass(String involvedClass) {
         String[] searchClassNames = getSuperclassNames(involvedClass);
         Set<SimplifiedUmlAssociation> associations = new HashSet<SimplifiedUmlAssociation>();
         for (String className : searchClassNames) {
             associations.addAll(getUmlAssociations(className));
         }
         return associations;
     }
 
 
     private String[] getSuperclassNames(String className) {
         UMLClass[] superclasses = DomainModelUtils.getAllSuperclasses(model, className);
         String[] names = new String[superclasses.length + 1];
         for (int i = 0; i < superclasses.length; i++) {
             names[i] = superclasses[i].getPackageName() + "." + superclasses[i].getClassName();
         }
         names[names.length - 1] = className;
         return names;
     }
     
     
     private List<SimplifiedUmlAssociation> getUmlAssociations(String testClass) {
         List<SimplifiedUmlAssociation> associations = new ArrayList<SimplifiedUmlAssociation>();
         if (model.getExposedUMLAssociationCollection() != null
             && model.getExposedUMLAssociationCollection().getUMLAssociation() != null) {
             for (UMLAssociation assoc : model.getExposedUMLAssociationCollection().getUMLAssociation()) {
                 UMLClass sourceClassReference = DomainModelUtils.getReferencedUMLClass(model, 
                     assoc.getSourceUMLAssociationEdge().getUMLAssociationEdge().getUMLClassReference());
                 UMLClass targetClassReference = DomainModelUtils.getReferencedUMLClass(model, 
                     assoc.getTargetUMLAssociationEdge().getUMLAssociationEdge().getUMLClassReference());
                 if (sourceClassReference != null && targetClassReference != null) {
                     String sourceClassName = sourceClassReference.getPackageName() + "." + sourceClassReference.getClassName();
                     String targetClassName = targetClassReference.getPackageName() + "." + targetClassReference.getClassName();
                     if (testClass.equals(sourceClassName) || testClass.equals(targetClassName)) {
                         SimplifiedUmlAssociation simple = new SimplifiedUmlAssociation(
                             sourceClassName, targetClassName, 
                             assoc.getSourceUMLAssociationEdge().getUMLAssociationEdge().getRoleName(),
                             assoc.getTargetUMLAssociationEdge().getUMLAssociationEdge().getRoleName(),
                             assoc.isBidirectional());
                         associations.add(simple);
                     }
                 }
             }
         }
         return associations;
     }
     
     
     private static class SimplifiedUmlAssociation {
         private String sourceClass;
         private String targetClass;
         private String sourceRoleName;
         private String targetRoleName;
         private boolean bidirectional;
         
         public SimplifiedUmlAssociation(String sourceClass, String targetClass, 
             String sourceRoleName, String targetRoleName, boolean bidirectional) {
             super();
             this.sourceClass = sourceClass;
             this.targetClass = targetClass;
             this.sourceRoleName = sourceRoleName;
             this.targetRoleName = targetRoleName;
             this.bidirectional = bidirectional;
         }
 
         
         public boolean isBidirectional() {
             return bidirectional;
         }
         
 
         public String getSourceClass() {
             return sourceClass;
         }
 
         
         public String getSourceRoleName() {
             return sourceRoleName;
         }
         
 
         public String getTargetClass() {
             return targetClass;
         }
         
 
         public String getTargetRoleName() {
             return targetRoleName;
         }
         
         
         public String toString() {
             StringBuffer buff = new StringBuffer();
             buff.append("source class: ").append(sourceClass).append(" via ").append(sourceRoleName).append("\n");
             buff.append("target class: ").append(targetClass).append(" via ").append(targetRoleName).append("\n");
             buff.append("bidirectional = ").append(bidirectional);
             return buff.toString();
         }
         
         
         public int hashCode() {
             return toString().hashCode();
         }
         
         
         public boolean equals(java.lang.Object o) {
             if (o != null && o instanceof SimplifiedUmlAssociation) {
                 return toString().equals(o.toString());
             }
             return false;
         }
     }
 }
