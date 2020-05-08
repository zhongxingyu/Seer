 package com.epam.lab.intouch.dao.member;
 
 /**
  * Enumeration of all Member table attributes
  * 
  * @author Revan
  * @version 0.1
  * 
  */
 public enum Attribute {
	LOGIN, PASSWORD, NAME, SURNAME, BIRTHDAY, REGISTRATION, SEX, QLEVEL, EXPERIENCE, PHOTO_LINK, ROLE;
 
 	/**
 	 * Method returns all attributes of Member table
 	 * 
 	 * @return attributes
 	 */
 	public static String getAttributes() {
 		final StringBuilder builder = new StringBuilder();
 
 		for (Attribute temp : values()) {
 			builder.append(temp.toString().toLowerCase()).append(", ");
 		}
 
 		final int length = builder.length();
 		builder.delete(length, length - 1);
 
 		return builder.toString();
 	}
 	
 	/**
 	 * Returns attribute index in Member table
 	 * 
 	 * @return index of attribute
 	 */
 	public int index() {
		return this.ordinal() + 1;
 	}
 }
