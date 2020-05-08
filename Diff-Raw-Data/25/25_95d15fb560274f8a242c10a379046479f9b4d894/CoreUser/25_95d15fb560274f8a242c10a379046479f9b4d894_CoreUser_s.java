 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.hibernate.coreuser.domain;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.DiscriminatorType;
 
 import org.apache.commons.lang.Validate;
 
 import org.compass.annotations.SearchableId;
 import org.compass.annotations.SearchableProperty;
 
 /** Defines the minimum information needed by katari modules of a user.
  *
  * This class holds the user name, as a string that can be displayed to the
  * user (like welcome [user name]), and an application-wide user id, a long
  * that identifies the user in the scope of the application.
  *
  * This is mapped as an entity in hibernate, so that modules can have a
  * database foreign key to user objects.
  *
  * Modules implementing user management must be aware of this class. The
  * intended usage is for modules to inherit from CoreUser, and map the rest of
  * the user properties.
  */
 @Entity
 @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn( name = "user_type",
     discriminatorType = DiscriminatorType.STRING)
 @Table(name = "users")
 public abstract class CoreUser {
 
   /** The length in characters of the user name.
    */
   private static final int USER_NAME_LENGTH = 50;
 
   /** The id of the user.
    *
    * This is 0 for a newly created user.
    */
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "id", nullable = false)
   @SearchableId
   private long id = 0;
 
   /** The name of the user.
    */
   @Column(name = "name", nullable = false, length = USER_NAME_LENGTH)
   @SearchableProperty
   private String name;
 
   /** The default constructor.
    *
    * Builds an empty user.
    */
   protected CoreUser() {
   }
 
   /** A custom constructor.
    *
    * Builds a user with the most basic data it needs to have.
    *
    * @param theName The user name. It cannot be null.
   *
   * @param theMail The user email address.
    */
   public CoreUser(final String theName) {
     Validate.notNull(theName, "The user name cannot be null");
     name = theName;
   }
 
   /** Returns the id of the user.
    *
    * @return Returns the user id, 0 if the user was not persisted yet.
    */
   public long getId() {
     return id;
   }
 
   /** Returns the name of the user.
    *
    * @return the name, never null.
    */
   public String getName() {
     return name;
   }
 
   /** Sets the user name.
    *
    * @param newName The new user name. It cannot be null.
    */
   protected final void setName(final String newName) {
     Validate.notNull(newName, "The user name cannot be null");
     name = newName;
   }
 }
 
