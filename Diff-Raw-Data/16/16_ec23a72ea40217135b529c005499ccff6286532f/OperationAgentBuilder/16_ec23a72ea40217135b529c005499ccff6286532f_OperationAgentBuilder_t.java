 /* OperationAgentBuilder.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		Mar 20, 2012 Created by pao
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
  */
 package org.zkoss.zats.mimic.impl;
 
 import org.zkoss.zats.mimic.Agent;
 import org.zkoss.zats.mimic.operation.OperationAgent;
 
 /**
  * Create an {@link OperationAgent} object for one or a group of ZK components.
  * 
  * @author pao
  * @author dennis
  */
 public interface OperationAgentBuilder<A extends Agent,O extends OperationAgent> {
 	
 	/**
 	 * This method create and return a specific implementation of {@link OperationAgent} object for specified OperationAgent class.
 	 * Similar to factory pattern.
 	 * @param agent
	 * @return the operation agent
 	 */
 	O getOperation(A agent);
 
 	/**
 	 * Provide class information for registration.
 	 * 
 	 * @return a sub-class of {@link OperationAgent} 
 	 */
 	Class<O> getOperationClass();
 }
