 package org.eclipse.emf.texo.test.model.issues.bz371509;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderColumn;
 
 /**
  * A representation of the model object '<em><b>EClass1</b></em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 @Entity(name = "EClass1")
public class EClass1 {
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @OneToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, mappedBy = "toClass1", targetEntity = EClass0.class)
   private Set<EClass0> toClass0 = new HashSet<EClass0>();
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, targetEntity = EClass0.class)
   @OrderColumn()
   @JoinTable(name = "EClass1_many")
   private List<EClass0> many = new ArrayList<EClass0>();
 
   /**
    * Returns the value of '<em><b>toClass0</b></em>' feature. Note: the returned collection is Unmodifiable use the
    * {#addToToClass0(org.eclipse.emf.texo.test.model.issues.bz371509.EClass0 value)} and
    * {@link #removeFromToClass0(EClass0 value)} methods to modify this feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return the value of '<em><b>toClass0</b></em>' feature
    * @generated
    */
   public Set<EClass0> getToClass0() {
     return Collections.unmodifiableSet(toClass0);
   }
 
   /**
    * Adds to the <em>toClass0</em> feature.
    * 
    * @generated
    */
   public void addToToClass0(EClass0 toClass0Value) {
     if (!toClass0.contains(toClass0Value)) {
       toClass0.add(toClass0Value);
       toClass0Value.setToClass1(this);
     }
   }
 
   /**
    * Removes from the <em>toClass0</em> feature.
    * 
    * @generated
    */
   public void removeFromToClass0(EClass0 toClass0Value) {
     if (toClass0.contains(toClass0Value)) {
       toClass0.remove(toClass0Value);
       toClass0Value.setToClass1(null);
     }
   }
 
   /**
    * Clears the <em>toClass0</em> feature.
    * 
    * @generated
    */
   public void clearToClass0() {
     while (!toClass0.isEmpty()) {
       removeFromToClass0(toClass0.iterator().next());
     }
   }
 
   /**
    * Sets the '{@link EClass1#getToClass0() <em>toClass0</em>}' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param the
    *          new value of the '{@link EClass1#getToClass0() toClass0}' feature.
    * @generated
    */
   public void setToClass0(Set<EClass0> newToClass0) {
     clearToClass0();
     for (EClass0 value : newToClass0) {
       addToToClass0(value);
     }
   }
 
   /**
    * Returns the value of '<em><b>many</b></em>' feature. Note: the returned collection is Unmodifiable use the
    * {#addToMany(org.eclipse.emf.texo.test.model.issues.bz371509.EClass0 value)} and
    * {@link #removeFromMany(EClass0 value)} methods to modify this feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return the value of '<em><b>many</b></em>' feature
    * @generated
    */
   public List<EClass0> getMany() {
     return Collections.unmodifiableList(many);
   }
 
   /**
    * Adds to the <em>many</em> feature.
    * 
    * @generated
    */
   public void addToMany(EClass0 manyValue) {
     if (!many.contains(manyValue)) {
       many.add(manyValue);
       manyValue.addToMany(this);
     }
   }
 
   /**
    * Removes from the <em>many</em> feature.
    * 
    * @generated
    */
   public void removeFromMany(EClass0 manyValue) {
     if (many.contains(manyValue)) {
       many.remove(manyValue);
       manyValue.removeFromMany(this);
     }
   }
 
   /**
    * Clears the <em>many</em> feature.
    * 
    * @generated
    */
   public void clearMany() {
     while (!many.isEmpty()) {
       removeFromMany(many.iterator().next());
     }
   }
 
   /**
    * Sets the '{@link EClass1#getMany() <em>many</em>}' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param the
    *          new value of the '{@link EClass1#getMany() many}' feature.
    * @generated
    */
   public void setMany(List<EClass0> newMany) {
     clearMany();
     for (EClass0 value : newMany) {
       addToMany(value);
     }
   }
 
   /**
    * A toString method which prints the values of all EAttributes of this instance. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @generated
    */
   @Override
   public String toString() {
     return "EClass1 ";
   }
 }
