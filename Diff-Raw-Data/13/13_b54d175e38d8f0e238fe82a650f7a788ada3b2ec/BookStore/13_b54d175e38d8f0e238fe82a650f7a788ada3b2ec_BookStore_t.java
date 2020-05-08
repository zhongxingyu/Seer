 package org.eclipse.emf.texo.test.model.samples.emap;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.JoinTable;
 import javax.persistence.MapKeyColumn;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import org.eclipse.emf.texo.test.TexoTestObjectConverter;
 import org.eclipse.emf.texo.test.TexoTestQNameConverter;
 import org.eclipse.emf.texo.test.model.base.identifiable.Identifiable;
 import org.eclipse.persistence.annotations.Converter;
 import org.eclipse.persistence.annotations.Converters;
 
 /**
  * A representation of the model object '<em><b>BookStore</b></em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 @Entity(name = "emapsample_BookStore")
 @Table(name = "EMAP_BOOKSTORE")
 @DiscriminatorColumn(length = 255)
 @Converters({ @Converter(converterClass = TexoTestObjectConverter.class, name = "TexoTestObjectConverter"),
     @Converter(converterClass = TexoTestQNameConverter.class, name = "TexoTestQNameConverter") })
 public class BookStore extends Identifiable {
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Basic(optional = false)
   private String name = null;
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @OneToMany(cascade = { CascadeType.ALL })
   @MapKeyColumn(table = "emapsample_BookStore_booksByWriter")
   @JoinTable(name = "emapsample_BookStore_booksByWriter")
   private Map<Writer, Book> booksByWriter = new LinkedHashMap<Writer, Book>();
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @ElementCollection()
   private Map<String, String> keyWords = new LinkedHashMap<String, String>();
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @ElementCollection()
   private Map<Writer, String> namesByWriter = new LinkedHashMap<Writer, String>();
 
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
    * Sets the '{@link BookStore#getName() <em>name</em>}' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param the
    *          new value of the '{@link BookStore#getName() name}' feature.
    * @generated
    */
   public void setName(String newName) {
     name = newName;
   }
 
   /**
    * Returns the value of '<em><b>booksByWriter</b></em>' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return the value of '<em><b>booksByWriter</b></em>' feature
    * @generated
    */
   public Map<Writer, Book> getBooksByWriter() {
     return booksByWriter;
   }
 
   /**
    * Sets the '{@link BookStore#getBooksByWriter() <em>booksByWriter</em>}' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param the
    *          new value of the '{@link BookStore#getBooksByWriter() booksByWriter}' feature.
    * @generated
    */
   public void setBooksByWriter(Map<Writer, Book> newBooksByWriter) {
     booksByWriter = newBooksByWriter;
   }
 
   /**
    * Returns the value of '<em><b>keyWords</b></em>' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return the value of '<em><b>keyWords</b></em>' feature
    * @generated
    */
   public Map<String, String> getKeyWords() {
     return keyWords;
   }
 
   /**
    * Sets the '{@link BookStore#getKeyWords() <em>keyWords</em>}' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param the
    *          new value of the '{@link BookStore#getKeyWords() keyWords}' feature.
    * @generated
    */
   public void setKeyWords(Map<String, String> newKeyWords) {
     keyWords = newKeyWords;
   }
 
   /**
    * Returns the value of '<em><b>namesByWriter</b></em>' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return the value of '<em><b>namesByWriter</b></em>' feature
    * @generated
    */
   public Map<Writer, String> getNamesByWriter() {
     return namesByWriter;
   }
 
   /**
    * Sets the '{@link BookStore#getNamesByWriter() <em>namesByWriter</em>}' feature.
    * 
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param the
    *          new value of the '{@link BookStore#getNamesByWriter() namesByWriter}' feature.
    * @generated
    */
   public void setNamesByWriter(Map<Writer, String> newNamesByWriter) {
     namesByWriter = newNamesByWriter;
   }
 
   /**
    * A toString method which prints the values of all EAttributes of this instance. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @generated
    */
   @Override
   public String toString() {
     return "BookStore " + " [name: " + getName() + "]";
   }
 }
