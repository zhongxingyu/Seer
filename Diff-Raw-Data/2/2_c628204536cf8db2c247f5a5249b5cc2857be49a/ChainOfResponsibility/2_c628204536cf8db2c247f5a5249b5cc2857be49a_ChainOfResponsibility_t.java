 package org.jgum.strategy;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.common.base.Function;
 
 
 /**
  * <p>
  * Class implementing the chain of responsibility pattern.
  * The class encapsulates a list of objects (the processing objects) in charge of managing a desired command.
  * </p>
  * <p>
  * A processing object delegates to the next object in the chain by means of throwing an instance of the exception class passed by in the constructor. 
  * If no exception is passed by at the constructor, a NoMyResponsibilityException exception is assumed as the indicator for delegation.
  * </p>
  * 
  *  @param <T> the type of the processing objects in the chain of responsibility.
  *  @param <V> the return type of the operation in the chain of responsibility applied to the distinct processing objects.
  * @author sergioc
  */
 public class ChainOfResponsibility<T, V> {
 
 	/**
 	 * The default exception class used for signaling delegation.
 	 */
 	public static final Class<? extends RuntimeException> DEFAULT_DELEGATION_EXCEPTION = NoMyResponsibilityException.class;
 	
 	private final List<T> responsibilityChain;
 	private final Class<? extends RuntimeException> exceptionClass;
 	private final RuntimeException chainExhaustedException;
 	
 	/**
 	 * Creates an empty chain of responsibility.
 	 */
 	public ChainOfResponsibility() {
 		this(new ArrayList());
 	}
 	
 	/**
 	 * Creates an empty chain of responsibility.
 	 * @param exceptionClass instances of this exception class denote that a processing object delegates to the next object in the responsibility chain.
 	 */
 	public ChainOfResponsibility(Class<? extends RuntimeException> exceptionClass) {
 		this(new ArrayList(), exceptionClass);
 	}
 	
 	/**
 	 * Creates a chain of responsibility initialized with the given list of processing objects.
 	 * @param responsibilityChain the processing objects.
 	 */
 	public ChainOfResponsibility(List<T> responsibilityChain) {
 		this(responsibilityChain, DEFAULT_DELEGATION_EXCEPTION);
 	}
 
 	/**
 	 * Creates a chain of responsibility initialized with the given list of processing objects.
 	 * @param responsibilityChain the processing objects.
 	 * @param exceptionClass instances of this exception class denote that a processing object delegates to the next object in the responsibility chain.
 	 */
 	public ChainOfResponsibility(List<T> responsibilityChain, Class<? extends RuntimeException> exceptionClass) {
 		this.responsibilityChain = responsibilityChain;
 		this.exceptionClass = exceptionClass;
 		try {
 			chainExhaustedException = exceptionClass.newInstance();
 		} catch (InstantiationException | IllegalAccessException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * Adds a new processing object at the beginning of the chain of responsibility.
 	 * @param processingObject a processing object.
 	 */
 	public void addFirst(T processingObject) {
 		responsibilityChain.add(0, processingObject);
 	}
 	
 	/**
 	 * Adds a new processing object at the end of the chain of responsibility.
 	 * @param processingObject a processing object.
 	 */
 	public void addLast(T processingObject) {
 		responsibilityChain.add(processingObject);
 	}
 	
 	/**
 	 * Removes the first occurrence of the specified processing object from the chain of responsibility, if it is present. 
 	 * @param processingObject a processing object.
 	 * @return true if this list contained the specified element
 	 */
 	public boolean remove(Object processingObject) {
 		return responsibilityChain.remove(processingObject);
 	}
 	
 	/**
 	 * This method executes a command on each member of the responsibility chain until it finds one that can manage it.
 	 * If a processing object throws an exception signaling delegation (the exception class is passed by in the constructor), the operation will be delegated to the next object in the responsibility chain.
 	 * Any other exception is propagated to the caller.
 	 * <p>
 	 * If no object is able to process the command after exhausting the responsibility chain, a delegation exception is thrown.
 	 * </p>
 	 * @param evaluator the evaluator of each object in the chain of responsibility.
 	 * @return the result of executing the command on the first object in the responsibility chain that does not throw a delegation exception.
 	 */
	public V apply(Function<T, V> evaluator) {
 		for(T processingObject : responsibilityChain) {
 			try {
 				return evaluator.apply(processingObject);
 			} catch (RuntimeException e) {
 				if(!exceptionClass.isInstance(e))
 					throw e;
 			}
 		}
 		throw chainExhaustedException;
 	}
 
 }
