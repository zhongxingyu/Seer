 package com.epam.lab.intouch.dao.member;
 
 /**
  * Enumeration of all Member table attributes
  * 
  * @author Revan
  * @version 0.1
  * 
  */
 public enum Attribute {
	LOGIN(1), 
	PASSWORD(2),
	NAME(3), 
	SURNAME(4), 
	BIRTHDAY(5), 
	REGISTRATION(6), 
	SEX(7), 
	QLEVEL(8), 
	EXPERIENCE(9), 
	PHOTO_LINK(10), 
	ROLE(11);
 
	private int index;
	
	private Attribute(final int index) {
		this.index = index;
	}
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
		return this.index;
 	}
 }
