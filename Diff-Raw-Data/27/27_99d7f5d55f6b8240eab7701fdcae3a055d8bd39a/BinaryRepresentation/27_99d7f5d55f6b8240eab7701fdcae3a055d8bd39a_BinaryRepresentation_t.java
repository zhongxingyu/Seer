 package edu.iastate.se339.text;
 
 public class BinaryRepresentation extends AbstractRepresentation {
 	
 	public BinaryRepresentation(byte[] rawBytes){
 		this.rawBytes = rawBytes;
 		bitsPerChar = 1;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		for(byte b : rawBytes){
 			for(int i=7; i>=0; i--){
				sb.append(Integer.toString((b >> i - 1) & 1));
 			}
 		}
 		return sb.toString();
 	}
 
 }
