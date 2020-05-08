 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.gda.extensions.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 
 public class DatasetTitleUtils {
 
 	public static String getTitle(final AbstractDataset xIn, 
 			                      final List<AbstractDataset> ysIn, 
 			                      final boolean isFileName) {
 		return getTitle(xIn,ysIn,isFileName,null);
 	}
 
 	public static String getTitle(final AbstractDataset xIn, 
 			                      final List<AbstractDataset> ysIn, 
 			                      final boolean isFileName,
 			                      final String  rootName) {
 		
 		final AbstractDataset       x;
 		final List<AbstractDataset> ys;
 		if (ysIn==null) {
 			ys = new ArrayList<AbstractDataset>(1);
 			ys.add(xIn);
 			x = DoubleDataset.arange(ys.get(0).getSize());
 			x.setName("Index of "+xIn.getName());
 		} else {
 			x  = xIn;
 			ys = ysIn;
 		}
 		
 		final StringBuilder buf = new StringBuilder();
 		buf.append("Plot of ");
 		final Set<String> used = new HashSet<String>(7);
 		int i=0;
 		int dataSetSize=ys.size();
 		for (IDataset dataSet : ys) {
 			String name = getName(dataSet,rootName);
 			
 			if (isFileName && name!=null) {
 			    // Strip off file name
 				final Matcher matcher = Pattern.compile("(.*) \\(.*\\)").matcher(name);
 				if (matcher.matches()) name = matcher.group(1);
 			}
 			
 			if (used.contains(name)) continue;			
 			if(i==0)
 				buf.append(name);
 			if(i==1 && 1==dataSetSize-1)
 				buf.append(","+name);
 			if(i==dataSetSize-1 && dataSetSize-1!=1)
 				buf.append("..."+name);
 			i++;
 		}
 		final int index = buf.length()-1;
 		buf.delete(index, index+1);
 		buf.append(" against ");
 		buf.append(getName(x,rootName));
 		return buf.toString();
 	}
 
 	/**
 	 * 
 	 * @param x
 	 * @param rootName
 	 * @return
 	 */
 	public static String getName(IDataset x, String rootName) {
 		if (x==null) return null;
 		try {
 		    return rootName!=null
 		           ? x.getName().substring(rootName.length())
 		           : x.getName();
 		} catch (StringIndexOutOfBoundsException ne) {
 			return x.getName();
 		}
 	}
 
 	
 	private static final Pattern ROOT_PATTERN = Pattern.compile("(\\/[a-zA-Z0-9]+\\/).+");
 
 	public static String getRootName(Collection<String> names) {
 		
 		if (names==null) return null;
 		String rootName = null;
 		for (String name : names) {
 			final Matcher matcher = ROOT_PATTERN.matcher(name);
 			if (matcher.matches()) {
 				final String rName = matcher.group(1);
				if (rootName!=null && !rootName.equals(rName)) {
 					rootName = null;
 					break;
 				}
 				rootName = rName;
 			} else {
 				rootName = null;
 				break;
 			}
 		}
 		return rootName;
 	}
 
 	/**
 	 * 
 	 * @param names
 	 * @return
 	 */
 	public static Map<String,String> getChoppedNames(final Collection<String> names) {
 		
 		final String rootName = DatasetTitleUtils.getRootName(names);
 		if (rootName==null)      return null;
 		if (rootName.length()<1) return null;
 
 		final Map<String,String> chopped = new HashMap<String,String>(names.size());
 		for (String name : names) {
 			chopped.put(name, name.substring(rootName.length()));
 		}
 		return chopped;
 	}
 
 }
