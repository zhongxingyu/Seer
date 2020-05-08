 /**
  * Trigger Class
  * 
  * "Triggers" are used to give agents certain behaviors. They represent a boolean expression as created by the user that, when met, causes the agent to perform a certain behavior.
  * Note: Triggers should have unique priorities within an agent; problems will be had if there are multiple triggers with the same priority values within an agent. 	 
  * 
  * @author Daniel Davenport, Grant Hensel, Elliot Penson, and Simon Swenson
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.entity;
 
 import java.util.ArrayList;
 
 import net.sourceforge.jeval.EvaluationException;
import edu.wheaton.simulator.behavior.Behavior;
 import edu.wheaton.simulator.expression.ExpressionEvaluator;
 import edu.wheaton.simulator.simulation.Grid;
 
 public class Trigger implements Comparable<Trigger> {
 
 	/**
 	 * Triggers are checked in order of priority, with lower numbers coming
 	 * first
 	 */
 	private int priority;
 
 	/**
 	 * Represents the conditions of whether or not the trigger fires.
 	 */
 	private ExpressionEvaluator conditionExpression;
 
 	/**
 	 * The behavior that is executed when the trigger condition is met
 	 */
 	private ArrayList<Behavior> behaviorList;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param priority
 	 *            Triggers are checked in order of priority, with lower numbers
 	 *            coming first
 	 * @param conditions
 	 *            boolean expression this trigger represents
 	 */
 	public Trigger(int priority, ExpressionEvaluator conditionExpression,
 			Behavior behavior) {
 		this.priority = priority;
 		this.conditionExpression = conditionExpression;
 		this.behaviorList.add(behavior);
 	}
 
 	/**
 	 * Clone Constructor. Deep copy is not necessary at this point.
 	 * 
 	 * @param parent
 	 *            The trigger from which to clone.
 	 */
 	public Trigger(Trigger parent) {
 		priority = parent.priority;
 		conditionExpression = parent.conditionExpression;
 		behaviorList = parent.behaviorList;
 	}
 
 	/**
 	 * Evaluates the boolean expression represented by this object and fires if
 	 * all conditions evaluate to true.
 	 * 
 	 * If someone wants to evaluate an expression to something other than boolean,
 	 * they will need to change this method or fire.
 	 * 
 	 * @return Boolean
 	 * @throws EvaluationException
 	 *             if the expression was invalid
 	 */
 	public void evaluate(GridEntity xThis, Grid grid, GridEntity xLocal,
 			GridEntity xGlobal) throws EvaluationException {
 
 		// TODO not sure how to go about implementing this function
 		GridEntity xOther = null;
 
 		ExpressionEvaluator expr = conditionExpression.clone();
 
 		expr.importEntity("this", xThis);
 		expr.importEntity("other", xOther);
 		expr.importEntity("local", xLocal);
 		expr.importEntity("global", xGlobal);
 
 		if (expr.evaluateBool()){
 			fire(xThis, xOther, xLocal, xGlobal);
 		
 		}
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * Get this trigger's priority
 	 * 
 	 * @return the priority
 	 */
 	public int getPriority() {
 		return priority;
 	}
 
 	/**
 	 * Get the String representation of this trigger's firing condition
 	 * 
 	 * @return the firing condition
 	 */
 	public ExpressionEvaluator getConditions() {
 		return conditionExpression;
 	}
 
 	/**
 	 * Fires the trigger. Will depend on the Behavior object for this trigger.
 	 */
 	public void fire(GridEntity xThis, GridEntity xOther, GridEntity xLocal,
 			GridEntity xGlobal) {
 		for (Behavior b : behaviorList)
 			b.execute(xThis, xOther, xLocal, xGlobal);
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
 
 	/**
 	 * Adds a behavior to the end of the list of behaviors.
 	 * 
 	 * @param behavior
 	 *            Behavior to be added to list
 	 */
 	public void addBehavior(Behavior behavior) {
 		behaviorList.add(behavior);
 	}
 }
