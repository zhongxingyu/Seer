 /*-
  * Copyright 2012 Diamond Light Source Ltd.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.crystallography;
 
 import java.io.Serializable;
 import java.util.Arrays;
 
 import javax.measure.quantity.Length;
 
 import org.jscience.physics.amount.Amount;
 
 /**
  * This class is a bean to hold h,k,l and d information. 
  * 
  * It does not offer calculation between the values, just holding them.
  * 
  * Often if d is set, h,k and l are not needed but it is up to the 
  * user of the data to decide what happens with it.
  */
 public class HKL implements Serializable {
 	
 	private int[] hkl;
 	private String ringName;
 	private Amount<Length> d;
 	
 	public HKL() {
		this(new int[3]);
 	}
 	
 	public HKL(Amount<Length> d) {
 		this(-1,-1,-1,d);
 	}
 	
 	public HKL(int... hkl) {
 		this(hkl[0], hkl[1], hkl[2], null);
 	}
 	
 	@Override
 	public HKL clone() {
 		HKL ret = new HKL(getH(), getK(), getL());
 		ret.setD(d);
 		return ret;
 	}
 	
 	/**
 	 * Used for calibration standards
 	 * @param h
 	 * @param k
 	 * @param l
 	 * @param d
 	 */
 	public HKL(int h, int k, int l, Amount<Length> d) {
 		this.hkl = new int[]{h,k,l};
 		this.d   = d!=null ? d.copy() : null;
 	}
 
 	public int getH() {
 		return hkl[0];
 	}
 	
 	public int getK() {
 		return hkl[1];
 	}
 	
 	public int getL() {
 		return hkl[2];
 	}
 	
 	public void setH(int h) {
 		hkl[0]=h;
 	}
 	
 	public void setK(int k) {
 		hkl[1]=k;
 	}
 	
 	public void setL(int l) {
 		hkl[2]=l;
 	}
 	
 	public int[] getIndices() {
 		return Arrays.copyOf(hkl, hkl.length);
 	}
 	
 	public int getMaxIndex() {
 		return Math.max(Math.max(getH(), getK()), getL());
 	}
 	
 	public int getMinIndex() {
 		return Math.min(Math.min(getH(), getK()), getL());
 	}
 	
 	@Override
 	public String toString() {
 		String str = String.format("(%d, %d, %d)", getH(), getK(), getL());
 		return str;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((d == null) ? 0 : d.hashCode());
 		result = prime * result + Arrays.hashCode(hkl);
 		result = prime * result + ((ringName == null) ? 0 : ringName.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		HKL other = (HKL) obj;
 		if (d == null) {
 			if (other.d != null)
 				return false;
 		} else if (!d.equals(other.d))
 			return false;
 		if (!Arrays.equals(hkl, other.hkl))
 			return false;
 		if (ringName == null) {
 			if (other.ringName != null)
 				return false;
 		} else if (!ringName.equals(other.ringName))
 			return false;
 		return true;
 	}
 
 	public String getRingName() {
 		return ringName;
 	}
 
 	public void setRingName(String name) {
 		this.ringName = name;
 	}
 
 	public Amount<Length> getD() {
 		return d!=null ? d.copy() : null;
 	}
 
 	public void setD(Amount<Length> d) {
 		this.d = d!=null ? d.copy() : null;
 	}
 	
 	/**
 	 * d in nanometers
 	 * @return d in nanometers
 	 */
 	public double getDNano() {
 		if (d==null) return Double.NaN;
 		return d.doubleValue(CalibrationStandards.NANOMETER);
 	}
 
 	/**
 	 * @param d in nanometers
 	 */
 	public void setDNano(double d) {
 		this.d = Amount.valueOf(d, CalibrationStandards.NANOMETER);
 	}
 
 }
