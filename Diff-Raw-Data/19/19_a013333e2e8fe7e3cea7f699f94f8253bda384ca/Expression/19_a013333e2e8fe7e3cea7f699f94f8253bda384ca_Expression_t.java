 package edu.wheaton.simulator.expression;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.regex.Pattern;
 
 import net.sourceforge.jeval.EvaluationException;
 import net.sourceforge.jeval.Evaluator;
 import net.sourceforge.jeval.VariableResolver;
 import net.sourceforge.jeval.function.FunctionException;
 import edu.wheaton.simulator.behavior.CloneBehavior;
 import edu.wheaton.simulator.behavior.DieBehavior;
 import edu.wheaton.simulator.behavior.MoveBehavior;
 import edu.wheaton.simulator.behavior.SetFieldBehavior;
 import edu.wheaton.simulator.entity.Entity;
 
 public class Expression {
 
 	/**
 	 * All variables that JEval evaluates are first passed to an associated instance of
 	 * VariableResolver. If the 'VR' returns a null then the JEval 'Evaluator' will look for the
 	 * variable name in its internal map. This 'VR' implementation is solely for the purpose of enabling
 	 * a shorter Expression syntax: "#{this.x}" rather than something like "getField('this','x')" which would require
 	 * implementing a "getField" ExpressionFunction.
 	 * 
 	 * @author bgarcia
 	 *
 	 */
 	protected class EntityFieldResolver implements VariableResolver {
 
 		private Map<String, Entity> entityMap;
 
 		/*
 		 * default constructor
 		 */
 		protected EntityFieldResolver() {
 			entityMap = new HashMap<String, Entity>();
 		}
 
 		/*
 		 * copy constructor
 		 */
 		protected EntityFieldResolver(EntityFieldResolver resolver) {
 			entityMap = new HashMap<String, Entity>();
 			entityMap.putAll(resolver.entityMap);
 		}
 
 		@Override
 		public String resolveVariable(String variableName)
 				throws FunctionException {
 
 			//splits with delimiter '.'
 			String[] args = variableName.split("\\x2e");
 
 			if (args.length != 2) {
 				return null;
 			}
 
 			String targetName = args[0];
 			String fieldName = args[1];
 
 			Entity target = getEntity(targetName);
 			if (target == null) {
 				throw new FunctionException("Target entity not found: " + targetName);
 			}
 			try {
 				String toReturn = target.getFieldValue(fieldName);
 				return toReturn;
 			} catch (NoSuchElementException e) {
 				throw new FunctionException("Target field not found: " + fieldName);
 			}
 		}
 
 		protected void setEntity(String name, Entity entity) {
 			entityMap.put(name, entity);
 		}
 
 		protected Entity getEntity(String name) {
 			return entityMap.get(name);
 		}
 	}
 
 	//boolean constants
 	public static final String TRUE = "1.0";
 	public static final String FALSE = "0.0";
 	
 	private Evaluator evaluator;
 	private EntityFieldResolver resolver;
 	private Object expr;
 
 	/**
 	 * Default constructor
 	 * 
 	 * The expression string is retrieved by calling expr.toString()
 	 */
 	public Expression(Object exprStr) {
 		setString(exprStr);
 		evaluator = new Evaluator();
 		resolver = new EntityFieldResolver();
 		evaluator.setVariableResolver(resolver);
 		
 		//make all project-defined ExpressionFunction implementations recognizable by default
 		this.importFunction(new CloneBehavior());
 		this.importFunction(new DieBehavior());
 		this.importFunction(new MoveBehavior());
 		this.importFunction(new SetFieldBehavior());
 		this.importFunction(new IsSlotOpen());
 		this.importFunction(new GetFieldOfAgentAt());
 		this.importFunction(new IsValidCoord());
 	}
 
 	/**
 	 * Copy constructor
 	 */
 	private Expression(Expression expr) {
 		evaluator = new Evaluator();
 		evaluator.setFunctions(expr.evaluator.getFunctions());
 		evaluator.setVariables(expr.evaluator.getVariables());
 		resolver = new EntityFieldResolver(expr.resolver);
 		evaluator.setVariableResolver(resolver);
 		setString(expr.expr);
 	}
 	
 	protected Expression(Evaluator eval, EntityFieldResolver res){
 		this.evaluator = eval;
 		this.resolver = res;
 	}
 	
 	/**
 	 * Returns a properly formatted variable reference.
 	 * 
 	 * fGet("x") == "#{x}"
 	 * 
 	 * fGet("this.x") == "#{this.x}"
 	 * 
 	 * @param entityName
 	 * @param fieldName
 	 * @return
 	 */
 	private static String fGet(String variableName){
 		return "#{" + variableName + "}";
 	}
 	
 	/**
 	 * Returns a properly formatted string to be passed to an Expression method.
 	 * 
 	 * "setField(" + fParams("this,x,8") + ")"
 	 *      ==
 	 * "setField('this','x',8)
 	 * 
 	 * @param params
 	 * @return
 	 */
 	public static String fParams(String params){
 		params = params.replaceAll(" ", "");
 		String[] paramList = params.split(",");
 		
 		for(int i=0; i<paramList.length; ++i){
 			paramList[i] = fParam(paramList[i]);
 		}
 		
 		String toReturn = "";
 		for(int i=0; i<paramList.length; ++i)
 			toReturn += paramList[i] + ",";
 		
 		if(toReturn.isEmpty() == false)
 			toReturn = toReturn.substring(0,toReturn.length()-1);
 		
 		return toReturn;
 	}
 	
 	/**
 	 * Returns a properly formatted string value
 	 * 
 	 * fStr("I am a banana!") == "'I am a banana!'"
 	 * 
 	 * @param value
 	 * @return
 	 */
 	public static String fStr(String value){
 		return "'" + value + "'";
 	}
 	
 	/**
 	 * Returns a properly formatted string to be passed to an Expression method.
 	 * 
 	 * "setField(" + fParam("this") + "," + fParam("x") + "," + fParam("8") + ")"
 	 *      ==
 	 * "setField('this','x',8)
 	 * 
 	 * @param name
 	 * @return
 	 */
 	private static String fParam(String param){
 		if(param.equalsIgnoreCase("true"))
 			return TRUE;
 		else if(param.equalsIgnoreCase("false"))
 			return FALSE;
 		else{
 			try{
 				return Double.valueOf(param).toString();
 			} 
 			
 			catch(Exception e){
 				return fStr(param);
 			}
 		}
 	}
 
 	/**
 	 * calls the copy constructor
 	 */
 	@Override
 	public Expression clone() {
 		return new Expression(this);
 	}
 
 	/**
 	 * sets the string that is evaluated by JEval/JEval-wrapper
 	 */
 	public void setString(Object exprStr) {
 		this.expr = exprStr;
 	}
 
 	/**
 	 * Define a variable
 	 * 
 	 * @Param name Do not format this String as you must do when creating an
 	 *        expression String. Simply pass the desired variable name.
 	 */
 	public void importVariable(String name, String value) {
 		evaluator.putVariable(name, value);
 	}
 
 	/**
 	 * Make an entity recognizable by this expression and all functions called within
 	 * 
 	 * @Param aliasName The name used to refer to the Entity in the expression
 	 *        String ("this", "other", etc.)
 	 */
 	public void importEntity(String aliasName, Entity entity) {
 		resolver.setEntity(aliasName, entity);
 	}
 
 	/**
 	 * Make an ExpressionFunction recognizable by this expression and all functions called within
 	 */
 	public void importFunction(AbstractExpressionFunction function) {
 		evaluator.putFunction(function.toJEvalFunction());
 	}
 
 	/**
 	 * get an imported Entity
 	 */
 	public Entity getEntity(String aliasName) {
 		return resolver.getEntity(aliasName);
 	}
 
 	/**
 	 * get the value of an imported variable
 	 */
 	public String getVariableValue(String variableName)
 			throws EvaluationException {
 		return evaluator.getVariableValue(variableName);
 	}
 
 	/**
 	 * clear all variables added with 'importVariable'
 	 * 
 	 */
 	public void clearVariables() {
 		evaluator.clearVariables();
 	}
 	
 	/**
 	 * clear all entities added with 'importEntity'
 	 */
 	public void clearEntities(){
 		resolver.entityMap.clear();
 	}
 
 	/**
 	 * clear all functions added with 'importFunction
 	 */
 	public void clearFunctions() {
 		evaluator.clearFunctions();
 	}
 
 	public Boolean evaluateBool() throws EvaluationException {
 		try {
 			return evaluator.getBooleanResult( formatExpr(expr) );
 		} catch (EvaluationException e) {
 			System.err.println(e.getMessage());
 			throw e;
 		}
 	}
 
 	public Double evaluateDouble() throws EvaluationException {
 		try {
 			return evaluator.getNumberResult( formatExpr(expr) );
 		} catch (EvaluationException e) {
 			System.err.println(e.getMessage());
 			throw e;
 		}
 	}
 
 	public String evaluateString() throws EvaluationException {
 		try {
 			return evaluator.evaluate( formatExpr(expr) );
 		} catch (EvaluationException e) {
 			System.err.println(e.getMessage());
 			throw e;
 		}
 	}
 	
 	@Override
 	public String toString(){
 		return expr.toString();
 	}
 
 	public static Boolean evaluateBool(Object exprStr)
 			throws EvaluationException {
 		Expression expr = new Expression(exprStr);
 		return expr.evaluateBool();
 	}
 
 	public static Double evaluateDouble(Object exprStr)
 			throws EvaluationException {
 		Expression expr = new Expression(exprStr);
 		return expr.evaluateDouble();
 	}
 
 	public static String evaluateString(Object exprStr)
 			throws EvaluationException {
 		Expression expr = new Expression(exprStr);
 		return expr.evaluateString();
 	}
 	
 	/**
 	 * Parses/formats the parameter according to expression syntax
 	 * 
 	 * This method is called before evaluating the expression.
 	 */
 	private static String formatExpr(Object expr){
 		String str = expr.toString();
 		
 		//format booleans
 		str = formatBools(str);
 		
		String regexVariableRef = "\\b[_a-zA-Z][_a-zA-Z0-9]*(\\.[_a-zA-Z][_a-zA-Z0-9]*)?\\b(?![('])(?=([^']*'[^']*')*[^']*$)";
 		
 		//string with all matches replaced with '@'
 		String temp = str.replaceAll(regexVariableRef, "@");
 		
 		//list of all non-matching segments
 		String[] nonMatchingSegs = temp.split("@");
 		
 		//construct string with all non-matches replaced with '@'
 		String temp2 = str;
 		for(String segment : nonMatchingSegs)
 			temp2 = Pattern.compile(segment,Pattern.LITERAL).matcher(temp2).replaceFirst("@");
 			//temp2 = temp2.replaceAll(segment, "@");
 		
 		//list of all matches
 		String[] matches = temp2.split("@");
 		
 		String toReturn = temp;
 		for(String match : matches){
			if(match.length()>0){
 				toReturn = toReturn.replaceFirst("@", fGet(match));
			}
 		}

 		return toReturn;
 	}
 	
 	private static String formatBools(String str){
 		str = str.replaceAll("\\b[Tt][Rr][Uu][Ee]\\b", TRUE);
 		str = str.replaceAll("\\b[Ff][Aa][Ll][Ss][Ee]\\b", FALSE);
 		return str;
 	}
 }
