 package com.elezeta.gnlp.model.verbs;
 
 import org.modelcc.*;
 
 import com.elezeta.gnlp.model.XComplement;
 
 public class VerbalComposite extends VerbalComplement implements IModel {
 
	// A verbal composite consists of a set of verbal complements and an optional floating conjunction.

 	@Optional
 	@Position(element="complements",position=Position.BEFORELAST)
 	Conjunction conjunction;
 	
	VerbalComplement[] complements;
 
 }
