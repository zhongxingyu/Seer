 /* MoveAgent.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		May 4, 2012 Created by pao
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
 */
 package org.zkoss.zats.mimic.operation;
 
 /**
  * The agent for moving operation.
  * @author pao
  */
public interface MoveAgent extends OperationAgent {
 
 	/**
 	 * To move a component.
 	 * unit is pixel.
 	 * @param left distance from left.
 	 * @param top distance from top.
 	 */
 	void moveTo(int left, int top);
 }
