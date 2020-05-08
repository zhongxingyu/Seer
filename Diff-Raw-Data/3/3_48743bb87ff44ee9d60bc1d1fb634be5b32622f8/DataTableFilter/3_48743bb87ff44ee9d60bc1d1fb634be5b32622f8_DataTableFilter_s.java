 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.workbench.ui.data;
 
 import java.util.Arrays;
 
 import org.dawnsci.slicing.api.data.ITransferableDataObject;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 
 class DataTableFilter extends ViewerFilter {
 
 	private String    searchString;
 	private IMetaData meta;
 
 	public void setSearchText(String s) {
 		if (s==null) s="";
 		this.searchString = ".*" + s.toLowerCase() + ".*";
 	}
 	
 	@Override
 	public boolean select(Viewer viewer, Object parentElement, Object element) {
 		if (searchString == null || searchString.length() == 0) {
 			return true;
 		}
 
 		final String dsName = ((ITransferableDataObject)element).getName();
 		if (dsName==null || "".equals(dsName)) return true;
 
 		final String name   = dsName.toLowerCase();
 
 
 		if (name.matches(searchString)) {
 			return true;
 		}
		if (name.matches(searchString)) {
			return true;
		}
 
 		// Check size so that we can filter on image size too
 		if (meta!=null) {
 			final int[] shape = meta.getDataShapes().get(dsName);
 			if (shape!=null) {
 				final String dStr = Arrays.toString(shape);
 				if (dStr.matches(searchString)) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	public void setMetaData(IMetaData metaData) {
 		this.meta = metaData;
 	}
 
 }
