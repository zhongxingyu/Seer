 package fr.gbloquel.codestory.services;
 
 import groovy.lang.GroovyShell;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import javax.ws.rs.Encoded;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Response;
 
 @Path("/")
 public class RootResource {
 
 	private static final String ANSWER_YES = "OUI";
 	private static final String ANSWER_NO = "NON";
 
 	private static final Pattern YESNO_QUESTION_PATTERN = Pattern
 			.compile(".+\\(OUI/NON\\)");
 
 	private static final Pattern ADDITION_PATTERN = Pattern.compile("(\\d)\\+(\\d)");
 	
 	private static final Pattern MULTIPLICATION_PATTERN = Pattern.compile("(\\d)\\*(\\d)");
 	
 	@GET
 	public Response question(@Encoded @QueryParam("q") String question) {
 		if(question == null) {
 			return Response.serverError().build();
 		}
 		return Response.ok(answer(question)).build();
 	}
 	
 	
 	/**
 	 * Answer to the question
 	 * 
 	 * @param question
 	 *            the question
 	 * @return answer the answer
 	 */
 	private String answer(String question) {
 		Matcher matcher = YESNO_QUESTION_PATTERN.matcher(question);
 		if (matcher.matches()) {
 			if (question.equals("Est+ce+que+tu+reponds+toujours+oui(OUI/NON)")) {
 				return ANSWER_NO;
 			}
 
 			return ANSWER_YES;
 		}
 		Matcher matcherAddition = ADDITION_PATTERN.matcher(question);
 		if(matcherAddition.matches()) {
 			return String.valueOf(Integer.parseInt(matcherAddition.group(1)) + Integer.parseInt(matcherAddition.group(2)));
 		}
 		
 		Matcher matcherMultiplication = MULTIPLICATION_PATTERN.matcher(question);
 		if(matcherMultiplication.matches()) {
 			return String.valueOf(Integer.parseInt(matcherMultiplication.group(1)) * Integer.parseInt(matcherMultiplication.group(2)));
 		}
 		
 		if (question.equals("Quelle+est+ton+adresse+email"))
 			return "gregory.bloquel@gmail.com";
 
 		String operationResult = computeComplexeOperation(question);
 		if(operationResult.length() > 0) return operationResult;
 		
 		
 		
 		return "";
 	}
 
 	/**
 	 * Compute a complex operation like (1+2)*2
 	 * @param operation
 	 * @return result if possible else empty
 	 */
 	private String computeComplexeOperation(String operation) {
 		String resultat = "";
 	
 		
 			ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
 			ScriptEngine engine = scriptEngineManager.getEngineByName("JavaScript");
 			
 			// convert french notation into internation notation 
 			if(operation.contains(",")) {
 				operation = operation.replace(",", ".");
 			}
 			
 			resultat = String.valueOf(new GroovyShell().evaluate(operation));
 			
 			if(resultat.endsWith(".0")) { // If ended by .0 suppress
 				resultat = resultat.replace(".0", "");
 			}
 			if(resultat.endsWith(".00")) { // If ended by .0 suppress
 				resultat = resultat.replace(".00", "");
 			}
 			
			
			if(resultat.contains(".")) { // Translate result in french notation
				resultat = resultat.replace(".", ",");
			}
			
			
			
 			return resultat;
 			
 		
 	}
 	
 	
 }
