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
 
 import org.apache.commons.math.linear.Array2DRowRealMatrix;
 import org.apache.commons.math.linear.RealMatrix;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 public class MaximalVectorComputationRFacadeTest {
 
 	private RealMatrix data;
 
 	@Before
 	public void setUp() {
 		data = new Array2DRowRealMatrix(new double[][]{
 				{1.0, 2.0, 3.0},
 				{1.0, 2.0, 2.0},
 				{2.0, 1.0, 3.0}
 		});
 	}
 
 	@Test
 	public void testComputeBEST() {
 		MaximalVectorComputationRFacade fac = new MaximalVectorComputationRFacade(data);
 		double[][] res = fac.computeBEST();
 		double[][] exp = new double[][]{
 				{1.0, 2.0, 3.0},
 				{2.0, 1.0, 3.0}	
 		};
 		for (int i=0;i<res.length;i++) {
 			Assert.assertArrayEquals(exp[i], res[i], 0.0001);
 		}
 	}
 	
 	@Test
 	public void testComputeBESTIndices() {
 		MaximalVectorComputationRFacade fac = new MaximalVectorComputationRFacade(data);
		Assert.assertArrayEquals(new int[]{1, 3}, fac.computeBESTindices()); // indices starting from 1
 	}
 }
