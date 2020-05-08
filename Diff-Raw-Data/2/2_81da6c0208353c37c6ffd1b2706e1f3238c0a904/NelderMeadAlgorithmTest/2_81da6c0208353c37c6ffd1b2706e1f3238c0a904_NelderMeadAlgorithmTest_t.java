 package org.eclipse.stem.tests.automaticexperiment;
 
 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.Arrays;
 
 import org.eclipse.stem.analysis.automaticexperiment.NelderMeadAlgorithm;
 import org.eclipse.stem.analysis.automaticexperiment.SimplexFunction;
 
 import junit.framework.TestCase;
 
 public class NelderMeadAlgorithmTest extends TestCase {
 
 	public void testExecute() {
 		NelderMeadAlgorithm nelder = new NelderMeadAlgorithm();
 		double[] initStart = { 1.8, 1.2 };
 		double[] step = { 0.5, 0.5 };
		nelder.execute(new SampleFunction(), initStart, step, 0.01, -1);
 		System.out.println("Results:");
 		System.out.println("Minimum Parameters - " + Arrays.toString(nelder.getMinimumParametersValues()));
 		System.out.println("Minimum Function Value - " + nelder.getMinimumFunctionValue());
 		assertTrue(Math.round(nelder.getMinimumParametersValues()[0]) == 3);
 		assertTrue(Math.round(nelder.getMinimumParametersValues()[1]) == 2);
 		assertTrue(Math.round(nelder.getMinimumFunctionValue()) == -7);
 	}
 	
 	class SampleFunction implements SimplexFunction {
 
 		public double getValue(double[] parameters) {
 			//f(x,y) = -4x + x^2 - y - xy + y^2
 			//Local minimum for this function is -7 at x=3 and y=2
 			double x = parameters[0];
 			double y = parameters[1];
 			double result = -4 * x;
 			result += Math.pow(x, 2);
 			result -= y;
 			result -= x * y;
 			result += Math.pow(y, 2);
 			System.out.println("Function value for x=" + x + ", y=" + y + " is: " + result);
 			return result;
 		}
 		
 	}
 
 }
