 /**
  * Trigger Class
  * 
  * "Triggers" are used to give agents certain behaviors. They represent a boolean expression as created by the user that, when met, causes the agent to perform a certain behavior.
  * Note: Triggers should have unique priorities within an agent; problems will be had if there are multiple triggers with the same priority values within an agent. 	 
  * 
  * @author Agent Team
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.entity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.common.collect.HashBiMap;
 import com.google.common.collect.ImmutableList;
 
 import net.sourceforge.jeval.EvaluationException;
 import edu.wheaton.simulator.expression.AbstractExpressionFunction;
 import edu.wheaton.simulator.expression.Expression;
 
 public class Trigger implements Comparable<Trigger> {
 
 	/**
 	 * A name to reference a trigger by
 	 */
 	private String name;
 
 	/**
 	 * Determines when this trigger should be evaluated. Only affects the order
 	 * of triggers within its particular Agent in LinearUpdate. That is, the
 	 * order of importance in LinearUpdate for triggers is when the Owning Agent
 	 * is reached->the trigger's priority.
 	 * 
 	 * In PriorityUpdate, priority supercedes Agent ordering, that is trigger's
 	 * priority->when the Owning Agent is reached.
 	 * 
 	 */
 	private int priority;
 
 	/**
 	 * Used for AtomicUpdate In each iteration, the condition is evaluated, and
 	 * the result is stored hear. It is later checked to see whether or not the
 	 * behavior should be fired.
 	 */
 	private boolean atomicConditionResult;
 
 	/**
 	 * Represents the conditions of whether or not the trigger fires.
 	 */
 	private Expression conditionExpression;
 
 	/**
 	 * The behavior that is executed when the trigger condition is met
 	 */
 	private Expression behaviorExpression;
 
 	/**
 	 * Observers to watch this trigger
 	 */
 	private static Set<TriggerObserver> observers = new HashSet<TriggerObserver>();
 
 	/**
 	 * Constructor
 	 * 
 	 * @param priority
 	 *            Triggers are checked in order of priority, with lower numbers
 	 *            coming first
 	 * @param conditions
 	 *            boolean expression this trigger represents
 	 */
 	public Trigger(String name, int priority, Expression conditionExpression,
 			Expression behavior) {
 		this.name = name;
 		this.priority = priority;
 		this.conditionExpression = conditionExpression;
 		this.behaviorExpression = behavior;
 	}
 
 	/**
 	 * Clone Constructor. Deep copy is not necessary at this point.
 	 * 
 	 * @param parent
 	 *            The trigger from which to clone.
 	 */
 	public Trigger(Trigger parent) {
 		name = parent.getName();
 		priority = parent.getPriority();
 		conditionExpression = parent.getConditions();
 		behaviorExpression = parent.getBehavior();
 	}
 
 	/**
 	 * Evaluates the boolean expression represented by this object and fires if
 	 * all conditions evaluate to true.
 	 * 
 	 * If someone wants to evaluate an expression to something other than
 	 * boolean, they will need to change this method or fire.
 	 * 
 	 * @return Boolean
 	 * @throws EvaluationException
 	 *             if the expression was invalid
 	 */
 	public void evaluate(Agent xThis, int step) throws EvaluationException {
 		Expression condition = conditionExpression.clone();
 		Expression behavior = behaviorExpression.clone();
 
 		condition.importEntity("this", xThis);
 		behavior.importEntity("this", xThis);
 
 		boolean conditionResult = false;
 		try {
 			conditionResult = condition.evaluateBool();
 		} catch (Exception e) {
 			conditionResult = false;
 		}
 
 		if (conditionResult) {
 			fire(xThis, this, behavior, step);
 		}
 	}
 
 	/**
 	 * Just evaluates the condition and stores the result in
 	 * atomicConditionResult for later use (to see if the behavior is fired).
 	 * Vital for AtomicUpdate
 	 * 
 	 * @param xThis
 	 *            the owning Agent
 	 * @throws EvaluationException
 	 */
 	public void evaluateCond(Agent xThis) throws EvaluationException {
 		Expression condition = conditionExpression.clone();
 
 		condition.importEntity("this", xThis);
 
 		atomicConditionResult = false;
 		try {
 			atomicConditionResult = condition.evaluateBool();
 		} catch (EvaluationException e) {
 			atomicConditionResult = false;
 			throw e;
		} catch (Exception e) {
			atomicConditionResult = false;
 		}
 	}
 
 	/**
 	 * Checks atomicConditionResult to see whether or not the behavior should be
 	 * fired rather than evaluating the condition on the spot Vital for
 	 * AtomicUpdate
 	 * 
 	 * @param xThis
 	 * @throws EvaluationException
 	 */
 	public void atomicFire(Agent xThis, int step) throws EvaluationException {
 		Expression behavior = behaviorExpression.clone();
 
 		behavior.importEntity("this", xThis);
 		if (atomicConditionResult)
 			fire(xThis, this, behavior, step);
 	}
 
 
 	/**
 	 * Fires the trigger. Will depend on the Behavior object for this trigger.
 	 * 
 	 * @param a
 	 * @param t
 	 * @param behavior
 	 * @param step
 	 * @throws EvaluationException
 	 */
 	private static void fire(Agent a, Trigger t, Expression behavior, int step) throws EvaluationException {
 		try {
 			if (behavior.evaluateBool() == false) {
 			}
 			else {
 			}
 		} catch (EvaluationException e) {
 			throw new EvaluationException("Behavior");
 		}
 		notifyObservers(a.getID(), t, step);
 	}
 
 	/**
 	 * Compares this trigger to another trigger based on priority
 	 * 
 	 * @param other
 	 *            The other trigger to compare to.
 	 * @return -1 if less, 0 if same, 1 if greater.
 	 */
 	@Override
 	public int compareTo(Trigger other) {
 		if (priority == other.priority) {
 			return 0;
 		} else if (priority > other.priority) {
 			return 1;
 		} else {
 			return -1;
 		}
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public Expression getConditions() {
 		return conditionExpression;
 	}
 	
 	public Expression getBehavior() {
 		return behaviorExpression;
 	}
 
 	public int getPriority() {
 		return priority;
 	}
 
 	@Override
 	public String toString(){
 		return name;
 	}
 
 	public static class Builder {
 
 		/**
 		 * Current version of the trigger, will be returned when built
 		 */
 		private Trigger trigger;
 
 		/**
 		 * HashMap of simple values to actual JEval appropriate input
 		 * <name, Jeval equivalent>
 		 */
 		private HashBiMap<String, String> converter;
 
 		/**
 		 * Simple (user readable) values for creating conditionals
 		 */
 		private List<String> conditionalValues;
 
 		/**
 		 * Simple (user readable) values for creating behaviors
 		 */
 		private List<String> behavioralValues;
 
 		/**
 		 * Reference to prototype that is being created
 		 */
 		private Prototype prototype;
 
 		/**
 		 * HashMap of the names and the actual object of the functions.
 		 */
 		private Map<String, AbstractExpressionFunction> functions;
 
 		/**
 		 * Strings to give the gui so that they can edit it. Obtained from parsing
 		 * triggers into something that can be read by users.
 		 */
 		private String conditionString;
 		private String behaviorString;
 		
 		/**
 		 * Constructor for the trigger builder starting from scratch.
 		 * 
 		 * @param p
 		 *            A prototype with just fields
 		 */
 		public Builder(Prototype p) {
 			prototype = p;
 			trigger = new Trigger("", 0, null, null);
 			converter = HashBiMap.create();
 			conditionalValues = new ArrayList<String>();
 			behavioralValues = new ArrayList<String>();
 			functions= new HashMap<String, AbstractExpressionFunction>();
 			functions.putAll(Expression.getBehaviorFunction());
 			functions.putAll(Expression.getConditionFunction());
 			loadFieldValues(p);
 			loadOperations();
 			loadBehaviorFunctions();
 			loadConditionalFunctions();
 			conditionString = "";
 			behaviorString = "";
 		}
 		
 		/**
 		 * Constructor for the trigger builder starting with a trigger
 		 * @param t
 		 * @param p
 		 */
 		public Builder(Trigger t, Prototype p){
 			this(p);
 			parseTrigger(t);
 		}
 
 		
 		/**
 		 * Converts a JEval formed trigger to something that be read by a human and makes sense.
 		 * 
 		 * @param t
 		 */
 		private void parseTrigger(Trigger t) {
 			if (t == null){
 				return;
 			}
 			if (t.conditionExpression == null || t.behaviorExpression == null)
 				return;
 			String condition = t.getConditions().toString();
 			String behavior = t.getBehavior().toString();
 			for (String s : converter.inverse().keySet()){
 				if (condition.contains(s)){
 					condition = condition.replace(s, converter.inverse().get(s)+ " ");
 				}
 				if (behavior.contains(s)){
 					behavior = behavior.replace(s, converter.inverse().get(s)+" ");
 				}
 			}
 			if (condition.charAt(condition.length()-1)==(' ')){
 				condition= condition.substring(0, condition.length()-1);
 			}
 			conditionString = condition;
 			if (behavior.charAt(behavior.length()-1)==(' ')){
 				behavior= behavior.substring(0, behavior.length()-1);
 			}
 			behaviorString = behavior;
 		}
 		
 		/**
 		 * get the string parsed behavior
 		 * @return
 		 */
 		public String getBehaviorString(){
 			return behaviorString;
 		}
 		
 		/**
 		 * get the string parsed condition.
 		 * @return
 		 */
 		public String getConditionString(){
 			return conditionString;
 		}
 		
 		/**
 		 * Method to initialize conditionalValues and behaviorableValues
 		 */
 		private void loadFieldValues(Prototype p) {
 			conditionalValues.add("-- Values --");
 			behavioralValues.add("-- Values --");
 			for (Map.Entry<String, String> entry : p.getFieldMap().entrySet()) {
 				converter.put(entry.getKey(), "this." + entry.getKey());
 				conditionalValues.add(entry.getKey());
 				behavioralValues.add(entry.getKey());
 			}
 		}
 
 		/**
 		 * Method to store common operations and initialize them when a Builder
 		 * is initialized.
 		 */
 		private void loadOperations() {
 			conditionalValues.add("-- Operations --");
 			behavioralValues.add("-- Operations --");
 
 			converter.put("(",  "(");
 			conditionalValues.add("(");
 			behavioralValues.add("(");
 
 			converter.put(")", ")");
 			conditionalValues.add(")");
 			behavioralValues.add(")");
 
 			converter.put(",", ",");
 			conditionalValues.add(",");
 			behavioralValues.add(",");
 
 		
 			converter.put("OR", "||");
 			conditionalValues.add("OR");
 			behavioralValues.add("OR");
 
 			converter.put("AND", "&&");
 			conditionalValues.add("AND");
 			behavioralValues.add("AND");
 
 			converter.put("NOT_EQUALS", "!=");
 			conditionalValues.add("NOT_EQUALS");
 			behavioralValues.add("NOT_EQUALS");
 
 			converter.put("EQUALS", "==");
 			conditionalValues.add("EQUALS");
 			behavioralValues.add("EQUALS");
 
 			converter.put(">", ">");
 			conditionalValues.add(">");
 			behavioralValues.add(">");
 
 			converter.put("<", "<");
 			conditionalValues.add("<");
 			behavioralValues.add("<");
 
 		}
 
 		/**
 		 * Method to store and initialize all the functions that a trigger may
 		 * use.
 		 */
 		private void loadConditionalFunctions() {
 			conditionalValues.add("--Functions--");
 			conditionalValues.add("True");
 			for (String s : Expression.getConditionFunction().keySet()){
 				conditionalValues.add(convertCamelCaseToNormal(s));
 				converter.put(convertCamelCaseToNormal(s), s);
 			}
 		}
 
 		/**
 		 * Loads all of the behaviors that we are using and their JEval
 		 * equivalent.
 		 * 
 		 * @param p
 		 */
 		private void loadBehaviorFunctions() {
 			behavioralValues.add("--Functions--");
 			for (String s : Expression.getBehaviorFunction().keySet()){
 				behavioralValues.add(convertCamelCaseToNormal(s));
 				converter.put(convertCamelCaseToNormal(s), s);
 			}
 		}
 
 		/**
 		 * Sets the name of the Trigger
 		 * @param n
 		 */
 		public void addName(String n) {
 			trigger.name = n;
 		}
 
 		/**
 		 * Sets the priority of the Trigger
 		 * 
 		 * @param n
 		 */
 		public void addPriority(int p) {
 			trigger.priority = p;
 		}
 
 		/**
 		 * Returns a set of conditional values
 		 * 
 		 * @return Set of Strings
 		 */
 		public ImmutableList<String> conditionalValues() {
 			ImmutableList.Builder<String> builder = new ImmutableList.Builder<String>();
 			builder.addAll(conditionalValues);
 			return builder.build();
 		}
 
 		/**
 		 * Gets a set of possible behavioral values
 		 * 
 		 * @return Set of Strings
 		 */
 		public ImmutableList<String> behavioralValues() {
 			ImmutableList.Builder<String> builder = new ImmutableList.Builder<String>();
 			builder.addAll(behavioralValues);
 			return builder.build();
 		}
 
 		/**
 		 * Passes the builder a string of values separated with a space the
 		 * correspond to a conditional
 		 * 
 		 * @param c
 		 */
 		public void addConditional(String c) {
 			conditionString = c;
 			String condition = "";
 			String[] stringArray = c.split(" ");
 			for (String a : stringArray) {
 				condition += (findMatches(a) + " ");
 			}
 			if (condition.charAt(condition.length()-1)==(' ')){
 				condition= condition.substring(0, condition.length()-1);
 			}
 			trigger.conditionExpression = (new Expression(condition));
 		}
 
 		/**
 		 * Passes the builder a string of values separated with a space the
 		 * correspond to a behavior
 		 * 
 		 * @param b
 		 */
 		public void addBehavioral(String b) {
 			behaviorString = b;
 			String behavior = "";
 			String[] stringArray = b.split(" ");
 			for (String a : stringArray) {
 				behavior += (findMatches(a)+" ");
 			}
 			if (behavior.charAt(behavior.length()-1)==(' ')){
 				behavior= behavior.substring(0, behavior.length()-1);
 			}
 			trigger.behaviorExpression= (new Expression(behavior));
 		}
 		
 		/**
 		 * Provides the expression appropriate version of the inputed string.
 		 * If not are found, it just gives back the String so the user can enter
 		 * own input.
 		 * 
 		 * @param s
 		 * @return
 		 */
 		private String findMatches(String s) {
 			String match = converter.get(s);
 			return (match == null) ? s : match;
 		}
 
 		/**
 		 * Whether or not the last conditional and behavioral are a valid
 		 * trigger
 		 * 
 		 * @return True if it works. Otherwise throws an exception 
 		 * telling what went wrong
 		 */
 		public boolean isValid() throws Exception{
 			try{
 				if (trigger.priority < 0){
 					throw new Exception("Trigger priority cannot be negative");
 				}
 				if (trigger.conditionExpression == null)
 					throw new Exception("Trigger condition is blank");
 				if (trigger.behaviorExpression == null)
 					throw new Exception("Trigger behavior is blank");
 				new Expression(isValidHelper(trigger.conditionExpression.toString())).evaluateBool();
 				new Expression(isValidHelper(trigger.behaviorExpression.toString())).evaluateBool();
 				return true;
 			}
 			catch (Exception e){
 				throw e;
 			}
 		}
 
 		/**
 		 * Helper method for isValid method to change this.xxxx to the value of xxxx 
 		 * in the prototype.
 		 * @param s
 		 * @return simplified expression that we can evaluate
 		 */
 		private String isValidHelper(String s) throws Exception{
 			while(s.indexOf("this.") != -1){
 				int index = s.indexOf("this.");
 				String beginning = s.substring(0, index);
 				s = s.substring(index+5);
 				Map<String, String> map = prototype.getFieldMap();
 				for (String a : map.keySet()){
 					if (s.indexOf(a) == 0){
 						String value = map.get(a);
 						s = value + s.substring(a.length());
 						break;
 					}
 				}
 				s = beginning + s;
 			}
 			return isValidHelper2(s);
 		}
 		
 		/**
 		 * Helper method for isValid method to check functions for correct number of 
 		 * arguments and the 
 		 * @param s
 		 * @return simplified expression that we can evaluate
 		 */
 		private String isValidHelper2(String s) throws Exception{
 			for (String f : functions.keySet()){
 				while (s.indexOf(f)!= -1){
 					int index = s.indexOf(f);
 					String beginning = s.substring(0, index);
 					s= s.substring(index);
 					String test = isValidHelper2(s.substring(s.indexOf("(", index)));
 					System.out.println(test);
 					String[] numArgs = test.split(",");
 					if (numArgs.length != functions.get(f).numArgs()){
 						throw new Exception("The function: " + convertCamelCaseToNormal(functions.get(f).getName()) + 
 								", has the wrong number of arguments"); 
 					}
 					s = s.substring(s.indexOf(")")+1);
 					s = beginning + functionType(f)+s;
 				}
 			}
 			return s;
 		}
 		
 
 		private String functionType(String name){
 			switch (functions.get(name).getResultType()){
 			case AbstractExpressionFunction.RESULT_TYPE_BOOL: return "TRUE";
 			case AbstractExpressionFunction.RESULT_TYPE_STRING: return "'string'";
 			}
 			return null;
 		}
 
 		/**
 		 * Provides the built trigger
 		 * 
 		 * @return
 		 */
 		public Trigger build() {
 			return trigger;
 		}
 		
 		/**
 		 * converts thisIsCamelCase to this_is_camel_case.
 		 * @param s
 		 * @return
 		 */
 		private static String convertCamelCaseToNormal(String s){
 			String toReturn = "";
 			for (int i = 0; i < s.length(); i++){
 				if (Character.isUpperCase(s.charAt(i)))
 					toReturn+= "_" + Character.toLowerCase(s.charAt(i));					
 				else
 					toReturn += s.charAt(i);
 			}
 			return toReturn;
 		}
 	}
 
 	/**
 	 * Adds an observer to the trigger's list
 	 * 
 	 * @param ob
 	 */
 	public static void addObserver(TriggerObserver ob) {
 		observers.add(ob);
 	}
 
 	/**
 	 * Notifies all of the observers watching this grid
 	 * 
 	 * @param caller
 	 * @param trigger
 	 * @param step
 	 */
 	public static void notifyObservers(AgentID caller, Trigger trigger, int step) {
 		for (TriggerObserver current : observers)
 			current.update(caller, trigger, step);
 	}
 
 }
