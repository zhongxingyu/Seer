 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 
 package org.dawb.common.ui.slicing;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.dawnsci.doe.DOEField;
 import org.dawnsci.doe.DOEUtils;
 
 
 /**
  * Bean to hold slice data
  */
 public class DimsData implements Serializable {
 
 	public static final int RANGE = 102;
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6889488003603498855L;
 
 	@DOEField(value=1, type=java.lang.Integer.class)
 	private String    sliceRange;
 
 	private int       dimension=-1;
 	
 	/**
 	 * 0=x, 1=y, 2=z
 	 */
 	private int       axis=-1;
 	private int       slice=0;
 
 	public DimsData() {
 		
 	}
 	
 	public DimsData(final int dim) {
 		this.dimension = dim;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + axis;
 		result = prime * result + dimension;
 		result = prime * result + slice;
 		result = prime * result + ((sliceRange == null) ? 0 : sliceRange.hashCode());
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
 		DimsData other = (DimsData) obj;
 		if (axis != other.axis)
 			return false;
 		if (dimension != other.dimension)
 			return false;
 		if (slice != other.slice)
 			return false;
 		if (sliceRange == null) {
 			if (other.sliceRange != null)
 				return false;
 		} else if (!sliceRange.equals(other.sliceRange))
 			return false;
 		return true;
 	}
 
 	public String getSliceRange() {
 		if (axis>-1&&axis!=RANGE) return null;
 		return sliceRange;
 	}
 
 	public void setSliceRange(String sliceRange) {
 		this.sliceRange = sliceRange;
 	}
 
 	public int getDimension() {
 		return dimension;
 	}
 
 	public void setDimension(int dimension) {
 		this.dimension = dimension;
 	}
 
 	/**
 	 * -1=slice, 0=x, 1=y, 2=z, 102=range
 	 */	
 	public int getAxis() {
 		return axis;
 	}
 
 	public void setAxis(int axis) {
 		this.axis = axis;
 	}
 
 	public int getSlice() {
 		if (axis>-1) return -1;
 		return slice;
 	}
 
 	public void setSlice(int slice) {
 		this.slice = slice;
 	}
 
 	@Override
 	public String toString() {
 		try {
 			return BeanUtils.describe(this).toString();
 		} catch (Exception e) {
 			return e.getMessage();
 		}
 	}
 	
 	public String getUserString(final int upperRange){ 
 		if (axis>-1) {
 			if (axis==0) return "X";
 			if (axis==1) return "Y";
 			if (axis==2) return "Z";
 		}
 		if (sliceRange!=null) return sliceRange;
 		if (upperRange>0) return slice+";"+(upperRange-1)+";1";
         return String.valueOf(slice);
 	}
 
 	public List<DimsData> expand(final int size) {
 		
 		final List<DimsData> ret = new ArrayList<DimsData>(7);
 		if (axis>-1) {
 			ret.add(this);
 			return ret;
 		}
 		if (sliceRange!=null) {
 			List<? extends Number> rs = DOEUtils.expand(sliceRange);
 			for (Number number : rs) {
 				final DimsData val = new DimsData(this.dimension);
 				val.setSlice(number.intValue());
 				ret.add(val);
 			}
 			return ret;
 		}
 		
 		for (int i = slice; i < size; i++) {
 			final DimsData val = new DimsData(this.dimension);
 			val.setSlice(i);
 			ret.add(val);
 		}
 		return ret;
 	}
 
 	
 	public DimsData clone() {
 		final DimsData clone = new DimsData();
 		clone.sliceRange = this.sliceRange;
 
 		clone.dimension  = this.dimension;
 		clone.axis       = this.axis;
 		clone.slice      = this.slice;
         return clone;
 	}
 
 	public boolean isSlice() {
 		return getAxis()<0;
 	}
 	public boolean isRange() {
 		return getAxis()==RANGE;
 	}
 }
