 package org.eclipse.emf.texo.test.model.issues.bz399086;
 
 import javax.persistence.Basic;
import javax.persistence.DiscriminatorColumn;
 import javax.persistence.Entity;
 import org.eclipse.emf.texo.test.TexoTestObjectConverter;
 import org.eclipse.emf.texo.test.TexoTestQNameConverter;
 import org.eclipse.emf.texo.test.model.base.identifiable.Identifiable;
 import org.eclipse.persistence.annotations.Converter;
 import org.eclipse.persistence.annotations.Converters;
 
 /**
  * A representation of the model object '<em><b>MapElement</b></em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 @Entity(name = "bz399086_MapElement")
@DiscriminatorColumn(length = 255)
 @Converters({ @Converter(converterClass = TexoTestObjectConverter.class, name = "TexoTestObjectConverter"),
     @Converter(converterClass = TexoTestQNameConverter.class, name = "TexoTestQNameConverter") })
 public class MapElement extends Identifiable {
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Basic(optional = false)
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
    * Sets the '{@link MapElement#getName() <em>name</em>}' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param newName
    *          the new value of the '{@link MapElement#getName() name}' feature.
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
     return "MapElement " + " [name: " + getName() + "]";
   }
 }
