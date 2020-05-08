 package com.elezeta.gnlp.model.verbs;
 
 import org.modelcc.*;
 
 import com.elezeta.gnlp.model.XComplement;
 
 public class VerbalPhrase extends VerbalComplement implements IModel {
 
	Verb verb;
 	
 	@Optional
 	Preposition preposition;
 
 }
