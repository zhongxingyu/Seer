 /* SizeAgent.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		May 4, 2012 Created by pao
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
 */
 package org.zkoss.zats.mimic.operation;
 
 /**
  * The agent of sizing operation.
  * @author pao
  */
 public interface SizeAgent extends OperationAgent {
 	/**
 	 * To maximized size of a component.
 	 * If the component isn't maximizable, it will throw exception. 
 	 * @param maximized true indicated maximization.
 	 */
 	void maximize(boolean maximized);
 
 	/**
 	 * To minimized size of a component.
 	 * If the component isn't minimizable, it will throw exception.
 	 * @param minimized true indicated minimization.
 	 */
	void minimized(boolean minimized);
 	
 	/**
 	 * To resize component.
 	 * @param width new width.
 	 * @param height specify -1, if only resize width.
 	 */
 	void resize(int width , int height);
 }
