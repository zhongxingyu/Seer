 package com.elezeta.gnlp.model.verbs;
 
 import org.modelcc.*;
 
 import com.elezeta.gnlp.model.XComplement;
 
 public class VerbalComposite extends VerbalComplement implements IModel {
 
 	@Optional
 	@Position(element="complements",position=Position.BEFORELAST)
 	Conjunction conjunction;
 	
	XComplement complements[];
	
	VerbalComplement[] items;
 
 }
