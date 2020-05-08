 package main.jadrin.tools;
 
 import java.util.ArrayList;
 
 
 import main.jadrin.ontology.Drink;
 
 public interface PageParser {
 	
	public String getParserName();
 	public ArrayList<Drink> parsePage();
 
 }
