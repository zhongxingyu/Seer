 /*
  * This file is part of libror.
  * libror is distributed from http://smaa.fi/libror
  * Copyright (C) 2011-12 Tommi Tervonen.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package fi.smaa.libror.r;
 
 import org.apache.commons.math.linear.RealMatrix;
 
 import fi.smaa.libror.MaximalVectorComputation;
 
 public class MaximalVectorComputationRFacade {
 
 	private RealMatrix mat;
 
 	public MaximalVectorComputationRFacade(double[] matrix, int nRows) {
 		RealMatrix mat = RHelper.rArrayMatrixToRealMatrix(matrix, nRows);
 		this.mat = mat;
 	}
 	
 	// for testing purposes
 	MaximalVectorComputationRFacade(RealMatrix mat) {
 		this.mat = mat;
 	}
 	
 	public double[][] computeBEST() {
 		return MaximalVectorComputation.computeBEST(mat).getData();
 	}
 	
 	public int[] computeBESTindices() {
		return MaximalVectorComputation.computeBESTindices(mat);
 	}
 }
