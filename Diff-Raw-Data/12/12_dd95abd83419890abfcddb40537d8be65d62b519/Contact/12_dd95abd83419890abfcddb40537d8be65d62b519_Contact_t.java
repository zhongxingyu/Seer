 /*
  * FrontlineSMS <http://www.frontlinesms.com>
  * Copyright 2007, 2008 kiwanja
  * 
  * This file is part of FrontlineSMS.
  * 
  * FrontlineSMS is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at
  * your option) any later version.
  * 
  * FrontlineSMS is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.frontlinesms.data.domain;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.*;
 
 import net.frontlinesms.data.DuplicateKeyException;
 import net.frontlinesms.data.EntityField;
 
 /**
  * Data object representing a contact.  A contact is uniquely identified by his phone number.
  * @author Alex
  */
 @Entity
 public class Contact {
 	
 //> COLUMN NAME CONSTANTS
 	/** Column name for {@link #id} */
 	static final String COLUMN_ID = "contact_id";
 	/** Column name for {@link #name} */
 	private static final String FIELD_NAME = "name";
 	/** Column name for {@link #phoneNumber} */
 	private static final String FIELD_PHONE_NUMBER = "phoneNumber";
 	/** Column name for {@link #groups} */
 	private static final String FIELD_GROUPS = "groups";
 	
 //> ENTITY FIELDS
 	/** Details of the fields that this class has. */
 	public enum Field implements EntityField<Contact> {
 		/** field mapping for {@link Contact#name} */
 		NAME(FIELD_NAME),
 		/** field mapping for {@link Contact#groups} */
 		GROUPS(FIELD_GROUPS),
 		/** field mapping for {@link Contact#phoneNumber} */
 		PHONE_NUMBER(FIELD_PHONE_NUMBER);
 		/** name of a field */
 		private final String fieldName;
 		/**
 		 * Creates a new {@link Field}
 		 * @param fieldName name of the field
 		 */
 		Field(String fieldName) { this.fieldName = fieldName; }
 		/** @see EntityField#getFieldName() */
 		public String getFieldName() { return this.fieldName; }
 	}
 	
 //> INSTANCE PROPERTIES
 	/** Unique id for this entity.  This is for hibernate usage. */
 	@SuppressWarnings("unused")
 	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
 	@Column(name=COLUMN_ID,unique=true,nullable=false,updatable=false)
 	private long id;
 	
 	/** Name of this contact */
	@Column(nullable=true, name=FIELD_NAME)
 	private String name;
 	
 	/** Phone number of this contact.  It should be unique within the system, but may be changed. */
 	@Column(unique=true, nullable=true, updatable=true, name=FIELD_PHONE_NUMBER)
 	private String phoneNumber;
 	
 	private String otherPhoneNumber;
 	private String emailAddress;
 	private String notes;
 	private boolean active;
 	
 	/** Groups that this chap is a member of. */
 	@ManyToMany(fetch=FetchType.EAGER, mappedBy=Group.COLUMN_DIRECT_MEMBERS, cascade=CascadeType.REMOVE)
 	private Set<Group> groups = new HashSet<Group>();
 	
 //> CONSTRUCTORS
 	/** Empty constructor for hibernate */
 	Contact() {}
 	
 	/**
 	 * Creates a contact with the specified attributes.
 	 * @param name The name of the new Contact
 	 * @param phoneNumber The phone number of the new contact
 	 * @param otherPhoneNumber value for {@link #otherPhoneNumber}
 	 * @param emailAddress the email address of the new contact
 	 * @param notes value for {@link #notes}
 	 * @param active value for {@link #active}
 	 */
 	public Contact(String name, String phoneNumber, String otherPhoneNumber, String emailAddress, String notes, boolean active) {
 		this.name = name;
 		this.phoneNumber = phoneNumber;
 		this.otherPhoneNumber = otherPhoneNumber;
 		this.emailAddress = emailAddress;
 		this.notes = notes;
 		this.active = active;
 	}
 	
 //> ACCESSOR METHODS
 	/**
 	 * Returns this contact's name, or if none is set, his phone number.
 	 * @return a string representing this contact.
 	 */
 	public String getDisplayName() {
 		if(this.name != null) return this.name;
 		else return this.getPhoneNumber();
 	}
 	
 	/** @return this contact's name */
 	public String getName() {
 		return this.name;
 	}
 	
 	/**
 	 * Sets this contact's name.
 	 * @param name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	/**
 	 * Gets this contact's phone number as a String.
 	 * @return {@link #phoneNumber}
 	 */
 	public String getPhoneNumber() {
 		return this.phoneNumber;
 	}
 	
 	/**
 	 * Gets this contact's other contact phone number as a String.
 	 * @return {@link #otherPhoneNumber}
 	 */
 	public String getOtherPhoneNumber() {
 		return this.otherPhoneNumber;
 	}
 	
 	/**
 	 * Sets this contact's other phone number.
 	 * @param phoneNumber
 	 */
 	public void setOtherPhoneNumber(String phoneNumber) {
 		this.otherPhoneNumber = phoneNumber;
 	}
 	
 	/**
 	 * Gets this contact's email address.
 	 * @return {@link #emailAddress}
 	 */
 	public String getEmailAddress() {
 		return this.emailAddress;
 	}
 	
 	/**
 	 * Sets this contact's email address.
 	 * @param emailAddress
 	 */
 	public void setEmailAddress(String emailAddress) {
 		this.emailAddress = emailAddress;
 	}
 	
 	/**
 	 * Gets the notes on this contact.
 	 * @return {@link #notes}
 	 */
 	public String getNotes() {
 		return notes;
 	}
 	
 	/**
 	 * Sets the notes on this contact.
 	 * @param notes
 	 */
 	public void setNotes(String notes) {
 		this.notes = notes;
 	}
 	
 	/**
 	 * Sets this contact's phone number.  This should be a phone number in international
 	 * format with leading '+'.
 	 * @param phoneNumber The contact's new phone number.
 	 * @throws NumberFormatException If the supplied number is not a valid international phone number.
 	 * @throws DuplicateKeyException If there is already a contact with the supplied number.
 	 */
 	public void setPhoneNumber(String phoneNumber) throws NumberFormatException, DuplicateKeyException {
 		this.phoneNumber = phoneNumber;
 	}
 	
 	/**
 	 * Checks if this contact is a member of the specified group.
 	 * @param group The group to check membership of.
 	 * @return TRUE if the contact is a member of the supplied group; FALSE otherwise.
 	 */
 	public boolean isMemberOf(Group group) {
 		return this.groups.contains(group);
 	}
 	
 	/**
 	 * Checks if this contact is active.
 	 * @return TRUE if the contact is active; FALSE otherwise.
 	 */
 	public boolean isActive() {
 		return this.active;
 	}
 	
 	/**
 	 * Sets this contact's active state.
 	 * @param active
 	 */
 	public void setActive(boolean active) {
 		this.active = active;
 	}
 	
 	/**
 	 * Returns the groups that this contact is a member of.
 	 * @return {@link #groups}
 	 */
 	public Collection<Group> getGroups() {
 		return this.groups;
 	}
 	
 	/** @param groups new value for {@link #groups} */
 	public void setGroups(Set<Group> groups) {
 		this.groups = groups;
 	}
 	
 	/**
 	 * Adds this contact to a group.  This should only be called from {@link Group#addContact(Contact)}
 	 * @param group Group this contact is being added to
 	 */
 	void addToGroup(Group group) {
 		this.groups.add(group);
 	}
 	
 	/**
 	 * Removes this contact from a group.  This should only be called from {@link Group#removeContact(Contact)}
 	 * @param group Group this contact is being added to
 	 */
 	void removeFromGroup(Group group) {
 		this.groups.remove(group);
 	}
 
 //> GENERATED CODE
 	/** @see Object#toString() */
 	@Override
 	public String toString() {
 		// TODO Auto-generated method stub
 		return this.getClass().getName() + "[" +
 				"name=" + this.name + ";"+
 				"phoneNumber=" + this.phoneNumber + ";"+
 				"emailAddress=" + this.emailAddress + ";"+
 				"otherPhoneNumber=" + this.otherPhoneNumber + ";"+
 				"notes=" + this.notes +
 				"]";
 	}
 	
 	/** @see java.lang.Object#hashCode() */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (active ? 1231 : 1237);
 		result = prime * result
 				+ ((emailAddress == null) ? 0 : emailAddress.hashCode());
 		result = prime * result + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((notes == null) ? 0 : notes.hashCode());
 		result = prime * result
 				+ ((otherPhoneNumber == null) ? 0 : otherPhoneNumber.hashCode());
 		return result;
 	}
 
 	/** @see java.lang.Object#equals(java.lang.Object) */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Contact other = (Contact) obj;
 		if (active != other.active)
 			return false;
 		if (emailAddress == null) {
 			if (other.emailAddress != null)
 				return false;
 		} else if (!emailAddress.equals(other.emailAddress))
 			return false;
 		if (phoneNumber == null) {
 			if (other.phoneNumber != null)
 				return false;
 		} else if (!phoneNumber.equals(other.phoneNumber))
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (notes == null) {
 			if (other.notes != null)
 				return false;
 		} else if (!notes.equals(other.notes))
 			return false;
 		if (otherPhoneNumber == null) {
 			if (other.otherPhoneNumber != null)
 				return false;
 		} else if (!otherPhoneNumber.equals(other.otherPhoneNumber))
 			return false;
 		return true;
 	}
 }
