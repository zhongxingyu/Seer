 package edu.iastate.se339.text;
 
 import java.util.StringTokenizer;
 
 public class WordTokenizerDecorator extends AbstractDecorator{
 
 	private String delimiter;
 	private int wordsPerLine;
 	
 	public WordTokenizerDecorator(AbstractRepresentation component, String delimiter, int wordsPerLine){
 		super(component);
 		this.delimiter = delimiter;
 		this.wordsPerLine = wordsPerLine;
 	}
 	
 	public WordTokenizerDecorator(AbstractRepresentation component) {
 		this(component, " ", 4);
 	}
 
 	@Override
 	public String toString() {
 		String in = component.toString();
 		StringTokenizer tok = new StringTokenizer(in, delimiter);
 		StringBuilder sb = new StringBuilder();
 		int i = 0;
 		while(tok.hasMoreTokens()){
 			sb.append(tok.nextToken());
 			if(++i % wordsPerLine == 0){
 				sb.append("\n");
 			}
 		}
 		return sb.toString();
 	}
 
 }
