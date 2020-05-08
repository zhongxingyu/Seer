 package com.elezeta.gnlp.model.verbs;
 
 import org.modelcc.*;
 
 import com.elezeta.gnlp.model.XComplement;
 
 public class VerbalPhrase extends VerbalComplement implements IModel {
 
	// A verbal phrase consists of a set of floating verbs and an optional floating preposition.

	Verb[] verb;
 	
 	@Optional
	@Position(element="verb",position=Position.WITHIN)
 	Preposition preposition;
 
 }
