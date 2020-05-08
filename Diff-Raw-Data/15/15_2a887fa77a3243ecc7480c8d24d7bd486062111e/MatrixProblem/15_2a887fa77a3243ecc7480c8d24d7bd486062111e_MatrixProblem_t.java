 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.toxbee.sleepfighter.challenge.math;
 
 import java.util.Random;
 
 import org.apache.commons.math3.linear.RealMatrix;
 
 import se.toxbee.sleepfighter.R;
 import se.toxbee.sleepfighter.utils.debug.Debug;
 import se.toxbee.sleepfighter.utils.math.MatrixUtil;
import android.content.Context;
 
 /*
  * Challenge: compute the matrix product of two 3x3 matrices. Then compute the determinant of the product.
  */
 public class MatrixProblem implements MathProblem {
 	
 	private final Context context;
 	
 	private static final int MAX_INT = 7;
 	private static final int MIN_INT = -3;
 	
 	// we use 3x3 matrices.
 	private static final int MATRIX_SIZE = 3;
 	
 	private int solution;
 	
 	private String renderedString;
 	
 	private Random rng = new Random();
 	
 	public String render() {
 		return this.renderedString;
 	}
 	
 	public int solution() {
 		Debug.d("solution is " + solution);
 		return this.solution;
 	}
 	
 	RealMatrix createRandomMatrix() {
 		return MatrixUtil.createRandomMatrix(rng, MATRIX_SIZE, MIN_INT, MAX_INT, true);
 	}
 	
 	public void newProblem() {
 			
 		RealMatrix m1 = createRandomMatrix();
 		RealMatrix m2 = createRandomMatrix();
 		
 		computeSolution(m1, m2);
 		doRender(m1, m2);
 	}
 	
 	private void computeSolution(RealMatrix m1, RealMatrix m2) {
 		RealMatrix product = m1.multiply(m2);
 		this.solution = (int)MatrixUtil.computeDeterminant(product);
 	}
 	
 	private void doRender(RealMatrix m1, RealMatrix m2) {
 		this.renderedString = "$A =";
 		this.renderedString += renderMatrix(m1);
 		this.renderedString += renderMatrix(m2);
 		this.renderedString += "$";
 		String format = context.getResources().getString(R.string.matrix_challenge_desc);
 		this.renderedString += "<br>" + String.format(format, "$A$");
 	
 	}
 	
 	private String renderMatrix(RealMatrix m) {
 		
 		double[][] matrixData = m.getData();
 		
 		
 		// begin table
 		String str = "(\\table ";
 		
 		for(int i = 0; i < MATRIX_SIZE; ++i) {
 			for(int j = 0; j < MATRIX_SIZE; ++j) {
 				str += Integer.toString((int) matrixData[i][j]) + " ";
 				if(j != MATRIX_SIZE-1) {
 					str += " , ";
 				}
 			}
 			
 			// no ";" necessary for the last row. 
 			if(i != MATRIX_SIZE-1)
 				// start new row.  
 				str += " ; ";
 		}
 		
 		// finish table
 		str += ")";
 		
 		return str;		
 	}
 	
 	public MatrixProblem(final Context context) {
 		this.context = context;
 	}
 	
 }
