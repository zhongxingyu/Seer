 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawnsci.slicing.api.system;
 
 import java.io.Serializable;
 import java.lang.reflect.Array;
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 public class DimsDataList implements Serializable {
 
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5902704017223885965L;
 	
 	
 	private List<DimsData> dimsData;
 	private boolean        expression;
 		
 	public DimsDataList() {
 	}
 
 	public DimsDataList(List<DimsData> chunk) {
 		dimsData = chunk;
 	}
 	
 	public DimsDataList(int[] dataShape) throws Exception {
 		
 		try {
 						
 			// For now we just assume the first dimensions are the slow ones to make an axis out
 			// of. Later read the axis from the meta list but we do not have examples of this so
 			// far.
 			int xaxis=-1,yaxis=-1;
 			for (int i = 0; i<dataShape.length; ++i) {
 				add(new DimsData(i));
 			}
 			for (int i = dataShape.length-1; i>=0; i--) {
 				
 				if (dataShape[i]>1) {
 					if (yaxis<0) {
 						getDimsData(i).setPlotAxis(1);
 						yaxis = i;
 						continue;
 					} else  if (xaxis<0) {
 						getDimsData(i).setPlotAxis(0);
 						xaxis = i;
 						continue;
 					}
 				}
 			}
 			
 			// If we only found a y it may be a multiple-dimension set with only 1D possible.
 			// In that case change y to x.
 			if (yaxis>-1 && xaxis<0) {
 				getDimsData(yaxis).setPlotAxis(0);
 			}
 		} finally {
 			//file.close();
 		}
 	}
 
 	public List<DimsData> getDimsData() {
 		return dimsData;
 	}
 
 	public void setDimsData(List<DimsData> slices) {
 		this.dimsData = slices;
 	}
 	
 	public void add(DimsData dimension) {
 		if (dimsData==null) dimsData = new ArrayList<DimsData>(3);
 		if (dimsData.size()>dimension.getDimension() && dimension.getDimension()>-1) {
 			dimsData.set(dimension.getDimension(), dimension);
 		} else {
 			dimsData.add(dimension);
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((dimsData == null) ? 0 : dimsData.hashCode());
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
 		DimsDataList other = (DimsDataList) obj;
 		if (dimsData == null) {
 			if (other.dimsData != null)
 				return false;
 		} else if (!dimsData.equals(other.dimsData))
 			return false;
 		return true;
 	}
 
 	public static Object[] getDefault() {
 		return new DimsData[]{new DimsData(0)};
 	}
 	
 	public Object[] getElements() {
 		if (dimsData==null) return null;
 		return dimsData.toArray(new DimsData[dimsData.size()]);
 	}
 
 	public int size() {
 		if (dimsData==null) return 0;
 		return dimsData.size();
 	}
 
 	public DimsData getDimsData(int i) {
 		if (dimsData==null) return null;
 		return dimsData.get(i);
 	}
 
 	public Iterator<DimsData> iterator() {
 		if (dimsData==null) return null;
 		return dimsData.iterator();
 	}
 	
 	public void clear() {
 		if (dimsData!=null) dimsData.clear();
 	}
 	
 	public String toString() {
 		return toString(null);
 	}
 	public String toString(int[] shape) {
 		
 		final StringBuilder buf = new StringBuilder();
 		buf.append("[ ");
 		
 		int index = 0;
 		for (DimsData d : dimsData) {
 			
 			final int upper = shape!=null ? shape[index] : -1;
 			buf.append(d.getUserString(upper));
 			if (d!=dimsData.get(dimsData.size()-1)) buf.append(",  ");
 			++index;
 		}
 		buf.append(" ]");
 		return buf.toString();
 	}
 
 	public boolean isRangeDefined() {
 		for (DimsData data : getDimsData()) {
 			if (data.getSliceRange()!=null) return true;
 		}
 		return false;
 	}
 	
 	public int getAxisCount() {
 		if (dimsData==null) return -1;
 		int count = 0;
 		for (DimsData dd : dimsData) {
 			if (dd.getPlotAxis()>-1 && dd.getPlotAxis()!=DimsData.RANGE) count++;
 		}
 		return count;
 	}
 	
 	public int getRangeCount() {
 		int count = 0;
 		for (DimsData dd : dimsData) {
 			if (dd.getPlotAxis()==DimsData.RANGE) count++;
 		}
 		return count;
 	}
 
 	public boolean is2D() {
 		return getAxisCount()==2;
 	}
 	
 	public DimsDataList clone() {
 		final DimsDataList clone = new DimsDataList();
 		for (DimsData dd : getDimsData()) {
 			DimsData dnew = dd.clone();
 			clone.add(dnew);
 		}
 		clone.expression = expression;
 		return clone;
 	}
 
 	/**
 	 * Sets any axes there are to  the axis passed in
 	 */
 	public void normalise(int iaxis) {
 		for (DimsData dd : getDimsData()) {
 			if (dd.getPlotAxis()>-1) dd.setPlotAxis(iaxis);
 		}
 	}
 
 	/**
 	 * Probably not best algorithm but we are dealing with very small arrays here.
 	 * This is simply trying to ensure that only one dimension is selected as an
 	 * axis because the plot has changed.
 	 * 
 	 * @param iaxisToFind
 	 */
 	public void setSingleAxisOnly(int iaxisToFind, int iaxisValue) {
 		DimsData found = null;
 		for (DimsData dd : getDimsData()) {
 			if (dd.getPlotAxis()==iaxisToFind) {
 				dd.setPlotAxis(iaxisValue);
 				found=dd;
 			}
 		}
 		
 		if (found!=null) {
 			for (DimsData dd : getDimsData()) {
 				if (dd==found) continue;
 				dd.setPlotAxis(-1);
 			}
 			return;
 		} else { // We have to decide which of the others is x
 			
 			for (DimsData dd : getDimsData()) {
 				if (dd.getPlotAxis()>-1) {
 				    dd.setPlotAxis(iaxisValue);
 				    found=dd;
 				}
 			}
 			for (DimsData dd : getDimsData()) {
 				if (dd==found) continue;
 				dd.setPlotAxis(-1);
 			}
 		}
 	}
 
 	/**
 	 * Bit of a complex  method. It simply tries to leave the data with
 	 * two axes selected by finding the most likely two dimensions that
 	 * should be plot axes.
 	 * 
 	 * @param firstAxis
 	 * @param secondAxis
 	 */
 	public void setTwoAxesOnly(int firstAxis, int secondAxis) {
 		boolean foundFirst = false, foundSecond = false;
 		for (DimsData dd : getDimsData()) {
 			if (dd.getPlotAxis()==firstAxis)  foundFirst  = true;
 			if (dd.getPlotAxis()==secondAxis) foundSecond = true;
 		}
 		
 		if (foundFirst&&foundSecond) {
 			for (DimsData dd : getDimsData()) {
 				if (dd.getPlotAxis()==firstAxis)  continue;
 				if (dd.getPlotAxis()==secondAxis) continue;
 				dd.setPlotAxis(-1);
 			}
 			return;
 		} else { // We have to decide which of the others is first and second
 			
 			if (!foundFirst)  foundFirst  = processAxis(firstAxis, secondAxis);
 			if (!foundSecond) foundSecond = processAxis(secondAxis, firstAxis);
 			
 			for (DimsData dd : getDimsData()) {
 				if (dd.getPlotAxis()==firstAxis)  continue;
 				if (dd.getPlotAxis()==secondAxis) continue;
 				dd.setPlotAxis(-1);
 			}
 			return;
 				
 		}
 		
 	}
 	
 	/**
 	 * Bit of a complex  method. It simply tries to leave the data with
 	 * two axes selected by finding the most likely two dimensions that
 	 * should be plot axes.
 	 * 
 	 * @param firstAxis
 	 * @param secondAxis
 	 * @param thirdAxis
 	 */
 	public void setThreeAxesOnly(int firstAxis, int secondAxis, int thirdAxis) {
 
 		boolean foundFirst = false, foundSecond = false, foundThird = false;
 		for (DimsData dd : getDimsData()) {
 			if (dd.getPlotAxis()==firstAxis)  foundFirst  = true;
 			if (dd.getPlotAxis()==secondAxis) foundSecond = true;
 			if (dd.getPlotAxis()==thirdAxis)  foundThird  = true;
 		}
 		
 		if (foundFirst&&foundSecond&&foundThird) {
 			for (DimsData dd : getDimsData()) {
 				if (dd.getPlotAxis()==firstAxis)  continue;
 				if (dd.getPlotAxis()==secondAxis) continue;
 				if (dd.getPlotAxis()==thirdAxis)  continue;
 				dd.setPlotAxis(-1);
 			}
 			return;
 		} else { // We have to decide which of the others is first and second
 			
 			if (!foundFirst)  foundFirst  = processAxis(firstAxis,  secondAxis, thirdAxis);
 			if (!foundSecond) foundSecond = processAxis(secondAxis, firstAxis,  thirdAxis);
 			if (!foundThird)  foundThird  = processAxis(thirdAxis,  firstAxis,  secondAxis);
 			
 			for (DimsData dd : getDimsData()) {
 				if (dd.getPlotAxis()==firstAxis)  continue;
 				if (dd.getPlotAxis()==secondAxis) continue;
 				if (dd.getPlotAxis()==thirdAxis)  continue;
 				dd.setPlotAxis(-1);
 			}
 			return;
 				
 		}
 
 	}
 
 	
 	private final boolean processAxis(int axis, int... ignoredAxes) {
 		
 		final List ignored = asList(ignoredAxes);
 		for (DimsData dd : getDimsData()) {
 			if (dd.getPlotAxis()>-1 && !ignored.contains(dd.getPlotAxis())) {
 			    dd.setPlotAxis(axis);
 			    return true;
 			}
 		}	
 		
 		for (DimsData dd : getDimsData()) {
 			if (!ignored.contains(dd.getPlotAxis())) {
 				dd.setPlotAxis(axis);
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Convert a primitive array to a list.
 	 * @param array - an array of peimitives
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
     private static final <T> List<T> asList(final Object array) {
 		
         if (!array.getClass().isArray()) throw new IllegalArgumentException("Not an array");
         return new AbstractList<T>() {
             @Override
             public T get(int index) {
                 return (T) Array.get(array, index);
             }
 
             @Override
             public int size() {
                 return Array.getLength(array);
             }
         };
     }
 
 	public boolean isXFirst() {
 		for (DimsData dd : getDimsData()) {
 			if (dd.getPlotAxis()<0) continue;
 			return dd.getPlotAxis()==0;
 		}
 		return false;
 	}
 
 	public void reverseImage() {
 		for (DimsData dd : getDimsData()) {
 			if (dd.getPlotAxis()==0) {
 				dd.setPlotAxis(1);
 				continue;
 			}
 			
 			if (dd.getPlotAxis()==1) {
 				dd.setPlotAxis(0);
 				continue;
 			}
 		}
 	}
 
 	public boolean isExpression() {
 		return expression;
 	}
 
 	public void setExpression(boolean expression) {
 		this.expression = expression;
 	}
 
 	public boolean isEmpty() {
 		return dimsData==null || dimsData.isEmpty();
 	}
 
 
 }
