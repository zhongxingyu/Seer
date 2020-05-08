 package io.seq;
 
 import java.io.Serializable;
 
class Sequence implements Serializable {
 
	String header;
 	
	String value;
 
 	@Override
 	public String toString() {
 		return String.format(">%s|%s", header, value);
 	}
 	 
 	
 }
