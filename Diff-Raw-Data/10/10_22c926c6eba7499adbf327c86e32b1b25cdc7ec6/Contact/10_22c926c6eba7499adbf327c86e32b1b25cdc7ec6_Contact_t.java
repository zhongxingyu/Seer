 package com.fingy.aprod;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.builder.CompareToBuilder;
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 
 public class Contact implements Comparable<Contact>{
 
 	private static final String N_A = "N/A";
 	private static final String FORBIDDEN_MESSAGE_FRAGMENT = "limitet";
 
 	private final String category;
 	private final String name;
 	private final String phoneNumber;
 
 	public Contact(String category, String name, String phoneNumber) {
 		this.category = category;
 		this.name = name;
 		this.phoneNumber = phoneNumber;
 	}
 
 	public String getCategory() {
 		return category;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getPhoneNumber() {
 		return phoneNumber;
 	}
 
 	@Override
 	public String toString() {
 		return category + "#" + name + "#" + phoneNumber;
 	}
 
 	@Override
 	public int hashCode() {
 		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
 		hashCodeBuilder.append(category);
 		hashCodeBuilder.append(name);
 		hashCodeBuilder.append(phoneNumber);
 		return hashCodeBuilder.toHashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (!(obj instanceof Contact)) {
 			return false;
 		}
 		Contact other = (Contact) obj;
 		EqualsBuilder equalsBuilder = new EqualsBuilder();
 		equalsBuilder.append(category, other.category);
 		equalsBuilder.append(name, other.name);
 		equalsBuilder.append(phoneNumber, other.phoneNumber);
 		return equalsBuilder.isEquals();
 	}
 
 	public boolean isValid() {
 		return isNameValid() && isPhoneNumberValid();
 	}
 
 	private boolean isNameValid() {
 		return StringUtils.isNotBlank(name);
 	}
 
 	private boolean isPhoneNumberValid() {
 		return StringUtils.isNotBlank(phoneNumber) && !N_A.equals(phoneNumber) && !phoneNumber.contains(FORBIDDEN_MESSAGE_FRAGMENT);
 	}
 
 	@Override
 	public int compareTo(Contact o) {
 		CompareToBuilder builder = new CompareToBuilder();
 		builder.append(category, o.category);
 		builder.append(name, o.name);
 		builder.append(phoneNumber, o.phoneNumber);
 		return builder.toComparison();
 	}
 
 	public static Contact fromString(String contactData) {
 		String[] data = contactData.split("#");
 		try {
 			return new Contact(data[0], data[1], data[2]);
 		} catch(ArrayIndexOutOfBoundsException ex) {
 			System.out.println("Contact.fromString() " + contactData);
 			throw ex;
 		}
 	}
 
 }
