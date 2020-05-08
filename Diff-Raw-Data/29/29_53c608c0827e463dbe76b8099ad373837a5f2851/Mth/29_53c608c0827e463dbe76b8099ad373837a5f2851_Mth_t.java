 /*
  *  Copyright (C) 2012-2013 The Animo Project
  *  http://animotron.org
  *
  *  This file is part of Animi.
  *
  *  Animotron is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of
  *  the License, or (at your option) any later version.
  *
  *  Animotron is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of
  *  the GNU Affero General Public License along with Animotron.
  *  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.animotron.animi.acts;
 
 import org.animotron.matrix.Matrix;
 
 /**
  * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
  *
  */
 public class Mth {
 
 	public static void normalization(
 			final Matrix<Float> values, 
 			final float sum2) {
 		
		if (sum2 > 0f) {
			final float norm = (float) Math.sqrt(sum2);
			for (int index = 0; index < values.length(); index++) {
		    	
		    	final float value = values.getByIndex(index) / norm;
		    	
	    		values.setByIndex(value, index);
		    }
		}
 	}
 
 }
