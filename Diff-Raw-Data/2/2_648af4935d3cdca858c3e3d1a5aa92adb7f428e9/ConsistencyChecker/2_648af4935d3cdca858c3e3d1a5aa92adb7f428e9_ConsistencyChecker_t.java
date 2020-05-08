 /* Copyright 2009-2010 Yves Dubromelle
  *
  * This file is part of JenericAHP.
  *
  * JenericAHP is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JenericAHP is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with JenericAHP.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.taeradan.ahp;
 
 import Jama.Matrix;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Yves Dubromelle
  */
 public class ConsistencyChecker {
 
 	/**
 	 *
 	 */
 	private static final double[] randomIndex = {0.00, 0.00, 0.58, 0.90, 1.12, 1.24, 1.32, 1.41,
 												 1.45, 1.49, 1.51, 1.48, 1.56, 1.57, 1.59};
 	/**
 	 * 
 	 */
 	private static double consistenceCrit = 0;
 
 	/**
 	 *
 	 * @param prefMatrix
 	 * @param prioVector
 	 * @return
 	 */
 	public static boolean isConsistent(final PreferenceMatrix prefMatrix, final PriorityVector prioVector) {
 		boolean consistent = false;
 		Matrix matrix = prefMatrix.getMatrix();
 		Matrix vector = prioVector.getVector();
 		double[] lambdas;
 		int dimension = 0;
 		if (prefMatrix.getMatrix().getRowDimension() == prioVector.getVector().getRowDimension()) {
 			dimension = prefMatrix.getMatrix().getRowDimension();
 			if (dimension == 1) {
 				consistent = true;
 			}
 			if (dimension < 15 && dimension > 0) {
 				lambdas = new double[dimension];
 				for (int i = 0; i < dimension; i++) {
 					double sum = 0;
 					for (int j = 0; j < dimension; j++) {
 						sum = sum + matrix.get(i, j) * vector.get(j, 0);
 					}
 					lambdas[i] = sum / vector.get(i, 0);
 				}
 				double lambdaMax = Double.MIN_VALUE;
 				for (int index = 0; index < dimension; index++) {
 					if (lambdas[index] > lambdaMax) {
 						lambdaMax = lambdas[index];
 					}
 				}
 				final double CI = (lambdaMax - dimension) / (dimension - 1);
				consistenceCrit = CI / randomIndex[dimension -1];
 				if (consistenceCrit < 0.1) {
 					consistent = true;
 				}
 			} else {
 				Logger.getAnonymousLogger().severe("Preference matrix and priority vector are too wide (15 max) or empty !!"
 												   + dimension);
 			}
 		} else {
 			Logger.getAnonymousLogger().severe("The matrix and vector dimension does not match !!" + prefMatrix.
 					getMatrix().getRowDimension() + "," + prioVector.getVector().getRowDimension());
 		}
 		return consistent;
 	}
 	/**
 	 *
 	 * @return
 	 */
 	public static double getCrResult(){
 		return consistenceCrit;
 	}
 }
