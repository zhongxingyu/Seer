 package de.htw.berater;
 
 import java.util.List;
 
 import de.htw.berater.db.SQLConstraint;
 
 public interface Berater {
 	
 	String evaluateAndAskNewQuestion(String string);
 
 	String evaluateAndAskNewQuestion(boolean yes);
 
 	void reset();
 
 	List<SQLConstraint> getCurrentSQLConstraintsList();
 
 	boolean expectsYesNoAnswer();
 
	boolean exprectsKeywordAnswer();
 
 	String askFirstQuestionZweck(); //Szenario1
 	
 	String askFirstQuestionGeneral(); //Szenario2
 }
