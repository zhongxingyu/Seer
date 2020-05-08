 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.modelinglab.ocl.core.ast;
 
 import com.google.common.base.Preconditions;
 import java.util.*;
 import javax.annotation.Nonnull;
 import org.modelinglab.ocl.core.ast.expressions.Variable;
 import org.modelinglab.ocl.core.ast.types.Classifier;
 import org.modelinglab.ocl.core.ast.utils.ElementComparator;
 import org.modelinglab.ocl.core.ast.utils.OclUtils;
 import org.modelinglab.ocl.core.ast.utils.OclVisitor;
 import org.modelinglab.ocl.core.ast.utils.UmlVisitor;
 import org.modelinglab.ocl.core.exceptions.AmbiguosOperationCall;
 
 /**
  *
  * @author Gonzalo Ortiz Jaureguizar (gortiz at software.imdea.org)
  */
 public class Namespace extends Element {
     private static final long serialVersionUID = 1L;
     
     /**
      * A collection of Elements identifiable within the Namespace, either by being owned or by 
      * being introduced by importing or inheritance. This is a derived union.
      */
     private final Map<String, Element> ownedMembers;
     private final SortedSet<Element> implicitElements;
 
     public Namespace() {
         ownedMembers = new TreeMap<String, Element>();
         implicitElements = new TreeSet<Element>(ElementComparator.getInstance());
     }
 
     public final Set<Element> getOwnedMembers() {
         Set<Element> result = new TreeSet<Element>(ElementComparator.getInstance());
         result.addAll(ownedMembers.values());
         return result;
     }
 
     /**
      * Set owned elements. All elements will be consider not implicit
      * @param ownedMembers 
      */
     public final void setOwnedMembers(@Nonnull Set<Element> ownedMembers) {
         modifyAttempt();
         this.ownedMembers.clear();
         this.implicitElements.clear();
         for (Element element : ownedMembers) {
             this.ownedMembers.put(element.getName(), element);
             element.setNamespaceUnsecure(this);
         }
     }
 
     /**
      * adds an not implicit element
      * @param element 
      */
     public final void addMember(@Nonnull Element element) {
         modifyAttempt();
         assert element instanceof Namespace || element instanceof Classifier || element instanceof Variable
                 : "Namespaces can only contain classifiers, ocl variables or other namespaces!";
         Element other = ownedMembers.put(element.getName(), element);
         if (other != null) {
            other.setNamespaceUnsecure(null);
         }
         element.setNamespaceUnsecure(this);
     }
 
     /**
      * adds an element
      * @param element
      * @param implicit  
      */
     public final void addMember(@Nonnull Element element, boolean implicit) {
         modifyAttempt();
         addMember(element);
         setImplicit(element, implicit);
     }
 
     public final void setImplicit(@Nonnull Element element, boolean implicit) {
         modifyAttempt();
         Preconditions.checkArgument(
                 ownedMembers.containsKey(element.getName()),
                 "%s is not a member of this namespace.",
                 element.getName());
         if (implicit) {
             implicitElements.add(element);
         } else {
             implicitElements.remove(element);
         }
     }
     
     public final boolean isImplicit(@Nonnull Element element) {
         return implicitElements.contains(element);
     }
 
     public final void removeMember(@Nonnull Element element) {
         modifyAttempt();
         Element removed = ownedMembers.remove(element.getName());
         if (element.equals(removed)) {
             removed.setNamespaceUnsecure(null);
             implicitElements.remove(element);
         }
     }
 
     public final Element lookup(@Nonnull String name) {
         return ownedMembers.get(name);
     }
 
     /**
      * TODO: this method returns any valid element, it should be check that there is only one valid element!
      * @param name
      * @return an implicit TypedElement with type UmlClass. This class contains an attribute
      * with the specific name. This returned value is null if there is no element that matches this condition
      */
     public final TypedElement findImplicitSourceForAttribute(@Nonnull String name) {
         for (Element e : implicitElements) {
             if (e instanceof TypedElement) {
                 Classifier classifier = ((TypedElement) e).getType();
                 Preconditions.checkNotNull(classifier, "The type of %s is invalid", e.getName());
                 if (classifier instanceof UmlClass) {
                     UmlClass clazz = (UmlClass) classifier;
                     Set<UmlClass> classes = OclUtils.getAllSuperclasses(clazz);
                     classes.add(clazz);
 
                     for (UmlClass umlClass : classes) {
                         for (Attribute subelement : umlClass.getOwnedAttributes()) {
                             if (subelement.getName().equals(name)) {
                                 return (TypedElement) e;
                             }
                         }
                     }
                 }
             }
         }
         return null;
     }
 
     /**
      * TODO: this method returns any valid element, it should be check that there is only one valid element!
      * @param name
      * @return 
      */
     public final Attribute lookupImplicitAttribute(@Nonnull String name) {
         TypedElement te = findImplicitSourceForAttribute(name);
         if (te == null) {
             return null;
         }
         UmlClass clazz = (UmlClass) te.getType();
         Set<UmlClass> classes = OclUtils.getAllSuperclasses(clazz);
         classes.add(clazz);
 
         for (UmlClass umlClass : classes) {
             for (Attribute subelement : umlClass.getOwnedAttributes()) {
                 if (subelement.getName().equals(name)) {
                     return subelement;
                 }
             }
         }
         throw new AssertionError();
     }
 
     /**
      * TODO: this method returns any valid element, it should be check that there is only one valid element!
      * @param name
      * @return an implicit TypedElement with type UmlClass. This class contains an association end
      * with the specific name. This returned value is null if there is no element that matches this condition
      */
     public final TypedElement findImplicitSourceForAssociationEnd(@Nonnull String name) {
         for (Element e : implicitElements) {
             if (e instanceof TypedElement) {
                 Classifier classifier = ((TypedElement) e).getType();
                 Preconditions.checkNotNull(classifier, "The type of %s is invalid", e.getName());
                 if (classifier instanceof UmlClass) {
                     UmlClass clazz = (UmlClass) classifier;
 
                     Set<UmlClass> classes = OclUtils.getAllSuperclasses(clazz);
                     classes.add(clazz);
 
                     for (UmlClass umlClass : classes) {
                         for (AssociationEnd subelement : umlClass.getOwnedAssociationEnds()) {
                             if (subelement.getName().equals(name)) {
                                 return (TypedElement) e;
                             }
                         }
                     }
                 }
             }
         }
         return null;
     }
 
     /**
      * TODO: this method returns any valid element, it should be check that there is only one valid element!
      * @param name
      * @return 
      */
     public final AssociationEnd lookupImplicitAssociationEnd(@Nonnull String name) {
         TypedElement te = findImplicitSourceForAssociationEnd(name);
         if (te == null) {
             return null;
         }
         UmlClass clazz = (UmlClass) te.getType();
         Set<UmlClass> classes = OclUtils.getAllSuperclasses(clazz);
         classes.add(clazz);
 
         for (UmlClass umlClass : classes) {
             for (AssociationEnd subelement : umlClass.getOwnedAssociationEnds()) {
                 if (subelement.getName().equals(name)) {
                     return subelement;
                 }
             }
         }
         throw new AssertionError();
     }
 
     /**
      * TODO: this method returns any valid element, it should be check that there is only one valid element!
      * @param name
      * @param params 
      * @param env 
      * @return an implicit TypedElement with type UmlClass. This class contains an operation
      * with the name and arguments. This returned value is null if there is no element that matches this condition
      * @throws AmbiguosOperationCall  
      */
     public final TypedElement findImplicitSourceForOperation(@Nonnull String name, List<Classifier> params, StaticEnvironment env) throws AmbiguosOperationCall {
         for (Element e : implicitElements) {
             if (e instanceof TypedElement) {
                 Classifier classifier = ((TypedElement) e).getType();
                 Preconditions.checkNotNull(classifier, "The type of %s is invalid", e.getName());
 
                 Operation operation = env.lookupOperation(classifier, name, params);
                 if (operation != null) {
                     return (TypedElement) e;
                 }
             }
         }
         return null;
     }
 
     /**
      * TODO: this method returns any valid element, it should be check that there is only one valid element!
      * @param name
      * @param params
      * @param env
      * @return
      * @throws AmbiguosOperationCall 
      */
     public final Operation lookupImplicitOperation(@Nonnull String name, List<Classifier> params, StaticEnvironment env) throws AmbiguosOperationCall {
         TypedElement te = findImplicitSourceForOperation(name, params, env);
         if (te == null) {
             return null;
         }
         Classifier classifier = te.getType();
         Operation result = env.lookupOperation(classifier, name, params);
         assert result != null;
         return result;
     }
     
     @Override
     public final List<Element> getTreeChildren() {
         List<Element> result = super.getTreeChildren();
         result.addAll(ownedMembers.values());
         return result;
     }
     
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Namespace other = (Namespace) obj;
         if (this.ownedMembers != other.ownedMembers && (this.ownedMembers == null || !this.ownedMembers.equals(other.ownedMembers))) {
             return false;
         }
         if (this.implicitElements != other.implicitElements && (this.implicitElements == null || !this.implicitElements.equals(other.implicitElements))) {
             return false;
         }
         return elementEquals(other);
     }
     
 
     @Override
     public int hashCode() {
         return super.hashCode();
     }
     
     public final <Result, Arg> Result accept(UmlVisitor<Result, Arg> visitor, Arg arguments) {
         return visitor.visit(this, arguments);
     }
 
     @Override
     public final <Result, Arg> Result accept(OclVisitor<Result, Arg> visitor, Arg arguments) {
         return visitor.visit(this, arguments);
     }
 }
