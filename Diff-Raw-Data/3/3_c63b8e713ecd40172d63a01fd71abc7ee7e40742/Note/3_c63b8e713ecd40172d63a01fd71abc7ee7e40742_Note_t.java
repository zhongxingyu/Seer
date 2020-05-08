 package com.github.mnicky.bible4j.data;
 
 public final class Note implements Comparable<Note> {
 
     public enum NoteType {
 	COMMENTARY,
 	USER_NOTE;
 
 	/**
 	 * Returns char specifying the NoteType. This method is intented for use with databases that doesn't
 	 * have built-in enum type.
 	 * 
 	 * @return char specifying this NoteType
 	 */
 	public char getSpecifyingChar() {
 	    return this.name().toLowerCase().charAt(0);
 	}
 
     }
 
     private final String text;
 
     private final Position position;
 
     private final NoteType type;
 
     public Note(String text, Position position, NoteType type) {
 	this.text = text;
 	this.position = position;
 	this.type = type;
     }
 
     /**
      * Returns NoteType conforming the specified character. This method is intented for use with databases
      * that doesn't have built-in enum type.
      * 
     * @param ch character specifying the NoteType
      * @return NoteType conforming the specified character
      */
     public static NoteType getNoteTypeByChar(char ch) {
 	switch (Character.toLowerCase(ch)) {
 	    case 'c':
 		return NoteType.COMMENTARY;
 	    case 'u':
 	    default:
 		return NoteType.USER_NOTE;
 	}
 
     }
 
     public String getText() {
 	return text;
     }
 
     public Position getPosition() {
 	return position;
     }
 
     public NoteType getType() {
 	return type;
     }
 
     @Override
     public String toString() {
 	return text + " - " + position.toString() + " (" + type + ")";
     }
 
     @Override
     public boolean equals(Object obj) {
 	if (obj == this)
 	    return true;
 	if (!(obj instanceof Note))
 	    return false;
 	Note note = (Note) obj;
 	return note.text.equals(this.text) && note.position.equals(this.position)
 		&& note.type.equals(this.type);
     }
 
     @Override
     public int hashCode() {
 	int result = 17;
 	result = 31 * result + (text == null ? 0 : text.hashCode());
 	result = 31 * result + (position == null ? 0 : position.hashCode());
 	result = 31 * result + (type == null ? 0 : type.hashCode());
 	return result;
     }
 
     @Override
     public int compareTo(Note n) {
 	return this.position.compareTo(n.getPosition());
     }
 
 }
