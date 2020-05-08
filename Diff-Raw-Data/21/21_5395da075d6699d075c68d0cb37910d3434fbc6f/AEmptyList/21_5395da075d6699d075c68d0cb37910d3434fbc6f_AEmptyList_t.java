 package awful.objects;
 
import awful.AEnvironment;
import awful.exceptions.EvaluationException;

public class AEmptyList extends AListPair {
	public AEmptyList() {
		super(null, null);
	}

 	public String toString() {
 		return "nil";
 	}
	
	public AObject evaluate(AEnvironment env, AListPair parameters) throws EvaluationException {
		return this;
	}
 }
