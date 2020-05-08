 package awful.objects.operators;
 
 import java.util.LinkedList;
 
 import awful.AEnvironment;
 import awful.exceptions.EvaluationException;
 import awful.objects.AEmptyList;
 import awful.objects.AListPair;
 import awful.objects.AObject;
 import awful.objects.AOperator;
 import awful.objects.ASymbol;
 
 public class AOLambda extends AOperator {
 	public AObject evaluate(AEnvironment env, AListPair parameters) throws EvaluationException {
 		LinkedList<String> params = new LinkedList<String>();
 		AObject current = parameters.getCar();
 		
		if (!(parameters.getCar() instanceof AEmptyList)) {
 			while (!(current instanceof AEmptyList)) {
 				if (!(current instanceof AListPair))
 					throw new EvaluationException("Expected AListPair");
 				if (!(((AListPair) current).getCar() instanceof ASymbol))
 					throw new EvaluationException("Expected ASymbol");
 				params.add(((ASymbol) ((AListPair) current).getCar()).getValue());
 				current = ((AListPair) current).getCdr();
 			}
 			current = parameters.getCdr();
 		}
 		else
 			current = parameters;
 		
 		if (!(current instanceof AListPair))
 			throw new EvaluationException("Expected AListPair");
 
 		AObject function = ((AListPair) current).getCar();
 		if (!(((AListPair) current).getCdr() instanceof AEmptyList))
 			throw new EvaluationException("Expected AListPair");
 		
 		return new AOLambdaFunction(params, function, env);
 	}
 }
