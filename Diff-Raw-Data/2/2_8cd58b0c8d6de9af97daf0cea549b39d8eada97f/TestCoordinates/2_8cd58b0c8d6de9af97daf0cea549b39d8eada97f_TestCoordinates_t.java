 /**
  * This file was developed for CS4233: Object-Oriented Analysis & Design.
  * The course was taken at Worcester Polytechnic Institute.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package hanto.studentndemarinis.testutil;
 
 import hanto.studentndemarinis.HantoFactory;
 import hanto.studentndemarinis.common.HexCoordinate;
 import hanto.testutil.TestHantoCoordinate;
 
 /**
  * 
  * Coordinates used for test cases
  * @author ndemarinis
  *
  */
 public class TestCoordinates {
 
 	public final static HantoFactory factory = HantoFactory.getInstance();
 	
 	public final static TestHantoCoordinate origin = new TestHantoCoordinate(0, 0);
 	
 	// The following coordinates are adjacent to (0, 0)
 	public final static TestHantoCoordinate c00 = new TestHantoCoordinate(0, 0);
 	public final static TestHantoCoordinate c01 = new TestHantoCoordinate(0, 1);
 	public final static TestHantoCoordinate c10 = new TestHantoCoordinate(1, 0);
 	public final static TestHantoCoordinate c1_1 = new TestHantoCoordinate(1, -1);
 	public final static TestHantoCoordinate c0_1 = new TestHantoCoordinate(0, -1);
	public final static TestHantoCoordinate c_10 = new TestHantoCoordinate(-1, 0);
 	public final static TestHantoCoordinate c_11 = new TestHantoCoordinate(-1, 1);
 	
 	
 	public final static TestHantoCoordinate c11 = new TestHantoCoordinate(1, 1);
 	
 	// Legacy coordinates from Gamma Hanto tests
 	public final static TestHantoCoordinate adjToOrigin10 = new TestHantoCoordinate(1, 0);
 	public final static TestHantoCoordinate adjToOrigin01 = new TestHantoCoordinate(0, 1);
 	public final static TestHantoCoordinate wayOffOrigin = new TestHantoCoordinate(3, 5);
 
 }
