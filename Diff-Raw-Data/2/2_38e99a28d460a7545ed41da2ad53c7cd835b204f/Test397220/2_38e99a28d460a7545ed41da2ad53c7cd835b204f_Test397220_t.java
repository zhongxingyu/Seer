 package org.eclipse.emf.texo.test.model.issues.bz397220;
 
 import javax.persistence.Basic;
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.Entity;
 import javax.persistence.EntityListeners;
 import org.eclipse.emf.texo.test.TexoTestEntityListener;
 import org.eclipse.emf.texo.test.TexoTestObjectConverter;
 import org.eclipse.emf.texo.test.TexoTestQNameConverter;
 import org.eclipse.emf.texo.test.model.base.identifiable.Identifiable;
 import org.eclipse.persistence.annotations.Converter;
 import org.eclipse.persistence.annotations.Converters;
 
 /**
  * A representation of the model object '<em><b>Test397220</b></em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 @Entity(name = "bz397220_Test397220")
 @DiscriminatorColumn(length = 255)
 @Converters({ @Converter(converterClass = TexoTestObjectConverter.class, name = "TexoTestObjectConverter"),
     @Converter(converterClass = TexoTestQNameConverter.class, name = "TexoTestQNameConverter") })
@EntityListeners(value = { TexoTestEntityListener.class, TexoTestEntityListener.class })
 public class Test397220 extends Identifiable {
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Basic()
   private String name = null;
 
   /**
    * Returns the value of '<em><b>name</b></em>' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return the value of '<em><b>name</b></em>' feature
    * @generated
    */
   public String getName() {
     return name;
   }
 
   /**
    * Sets the '{@link Test397220#getName() <em>name</em>}' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param the
    *          new value of the '{@link Test397220#getName() name}' feature.
    * @generated
    */
   public void setName(String newName) {
     name = newName;
   }
 
   /**
    * A toString method which prints the values of all EAttributes of this instance. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @generated
    */
   @Override
   public String toString() {
     return "Test397220 " + " [name: " + getName() + "]";
   }
 }
