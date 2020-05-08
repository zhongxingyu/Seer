 package edu.ntnu.EASY.selection.adult;
 
 import edu.ntnu.EASY.Population;
 
 public interface AdultSelector<PType> {
 
	public Population<?,PType> select(Population<?,PType> adults, Population<?,PType> children);
 
 	
 }
