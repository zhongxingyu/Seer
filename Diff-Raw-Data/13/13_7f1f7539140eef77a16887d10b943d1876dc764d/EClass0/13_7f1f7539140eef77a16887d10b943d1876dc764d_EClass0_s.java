 package org.eclipse.emf.texo.test.model.issues.bz371509;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinColumns;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OrderColumn;
 
 /**
  * A representation of the model object '<em><b>EClass0</b></em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 @Entity(name = "EClass0")
public class EClass0 {
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, optional = true, targetEntity = EClass1.class)
   @JoinColumns({ @JoinColumn() })
   private EClass1 toClass1 = null;
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, targetEntity = EClass1.class)
   @OrderColumn()
   @JoinTable(name = "EClass0_many")
   private List<EClass1> many = new ArrayList<EClass1>();
 
   /**
    * Returns the value of '<em><b>toClass1</b></em>' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return the value of '<em><b>toClass1</b></em>' feature
    * @generated
    */
   public EClass1 getToClass1() {
     return toClass1;
   }
 
   /**
    * Sets the '{@link EClass0#getToClass1() <em>toClass1</em>}' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param the
    *          new value of the '{@link EClass0#getToClass1() toClass1}' feature.
    * @generated
    */
   public void setToClass1(EClass1 newToClass1) {
     if (toClass1 != newToClass1) {
       if (toClass1 != null) {
         toClass1.removeFromToClass0(this);
       }
       toClass1 = newToClass1;
       if (toClass1 != null) {
         toClass1.addToToClass0(this);
       }
     }
   }
 
   /**
    * Returns the value of '<em><b>many</b></em>' feature. Note: the returned collection is Unmodifiable use the
    * {#addToMany(org.eclipse.emf.texo.test.model.issues.bz371509.EClass1 value)} and
    * {@link #removeFromMany(EClass1 value)} methods to modify this feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return the value of '<em><b>many</b></em>' feature
    * @generated
    */
   public List<EClass1> getMany() {
     return Collections.unmodifiableList(many);
   }
 
   /**
    * Adds to the <em>many</em> feature.
    * 
    * @generated
    */
   public void addToMany(EClass1 manyValue) {
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
   public void removeFromMany(EClass1 manyValue) {
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
    * Sets the '{@link EClass0#getMany() <em>many</em>}' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param the
    *          new value of the '{@link EClass0#getMany() many}' feature.
    * @generated
    */
   public void setMany(List<EClass1> newMany) {
     clearMany();
     for (EClass1 value : newMany) {
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
     return "EClass0 ";
   }
 }
