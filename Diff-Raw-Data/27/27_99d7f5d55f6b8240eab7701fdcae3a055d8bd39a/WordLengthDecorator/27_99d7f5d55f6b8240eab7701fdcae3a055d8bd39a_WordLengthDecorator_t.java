 package edu.iastate.se339.text;
 
 public class WordLengthDecorator extends AbstractDecorator{
 
 	private int wordLength;
 	
 	public WordLengthDecorator(AbstractRepresentation component, int wordLength){
 		super(component);
 		this.wordLength = wordLength;
 	}
 	
 	public WordLengthDecorator(AbstractRepresentation component) {
 		this(component, 8);
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder b = new StringBuilder();
 		String in = component.toString();
		for(int i = 0; i < in.length(); i++){
			if(((i * component.bitsPerChar) % wordLength) == 0 && i != 0){
 				b.append(" ");
 			}
			b.append(in.charAt(i));
 		}
 		return b.toString();
 	}
 
 }
