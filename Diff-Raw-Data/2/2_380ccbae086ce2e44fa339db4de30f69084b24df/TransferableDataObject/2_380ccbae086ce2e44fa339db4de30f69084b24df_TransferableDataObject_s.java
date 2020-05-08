 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.workbench.ui.transferable;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dawb.common.services.IExpressionObject;
 import org.dawb.common.services.IExpressionObjectService;
 import org.dawb.common.services.IVariableManager;
 import org.dawb.hdf5.editor.H5Path;
 import org.dawnsci.slicing.api.data.ITransferableDataObject;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 public class TransferableDataObject implements H5Path, ITransferableDataObject{
 	
 	private static final Logger logger = LoggerFactory.getLogger(TransferableDataObject.class);
 
 	private IDataHolder       holder;
 	private IMetaData         metaData;
 
 	private boolean           checked;
 	/**
 	 * If it is a transient data set it will have been copied from 
 	 * one data view to another and the user may want to delete it.
 	 */
 	private boolean           transientData=false;
 	private String            name;
 	private String            variable;
 	private IExpressionObject expression;
 	private String            mementoKey;
 	private IExpressionObjectService service;
 	private static int expressionCount=0;
 	
 	private String filterFile;
 	
 	/**
 	 * Clones the object and sets the transientData flag to true.
 	 */
 	public ITransferableDataObject clone() {
 		TransferableDataObject ret = new TransferableDataObject();
 		ret.holder   = holder.clone();
 		ret.metaData = metaData.clone();
 		ret.checked  = checked;
 		ret.name     = name;
 		ret.variable = variable;
 		ret.expression= expression;
 		ret.mementoKey= mementoKey;
 		ret.service   = service;
 		ret.transientData=true;
 		return ret;
 	}
 
 	private TransferableDataObject() {
 		
 	}
 	
 	protected TransferableDataObject(IDataHolder holder, IMetaData meta) {
 		this.holder   = holder;
 		this.metaData = meta;
 		expressionCount++;
 		this.variable   = "expr"+expressionCount;
 		this.service  = (IExpressionObjectService)PlatformUI.getWorkbench().getService(IExpressionObjectService.class);
 	}
 
 	protected TransferableDataObject(IDataHolder holder, IMetaData meta, final String name) {
 		this.holder   = holder;
 		this.metaData = meta;
 		this.name     = name;
 		this.service  = (IExpressionObjectService)PlatformUI.getWorkbench().getService(IExpressionObjectService.class);
 		this.variable = service.getSafeName(name);
 	}
 
 	protected TransferableDataObject(IDataHolder holder, IMetaData meta, IExpressionObject expression2) {
 		this.holder     = holder;
 		this.metaData   = meta;
 		this.expression = expression2;
 		expressionCount++;
 		this.variable   = "expr"+expressionCount;
 		this.service  = (IExpressionObjectService)PlatformUI.getWorkbench().getService(IExpressionObjectService.class);
 		expression2.setExpressionName(variable);
 	}
 	
 
 	@Override
 	public IDataset getData(IMonitor monitor) {
 		
 		IDataset set = null;
 		if (!isExpression()) {
 			try {
 				set = holder.getDataset(getName());
 			} catch(IllegalArgumentException ie) {
 				try {
 					ILazyDataset lz = holder.getLazyDataset(name);
 					IDataset all = lz.getSlice();
 					if (all.getSize()<2) throw new Exception();
 					set =  all;
 				} catch (Exception e) {
 					try {
 						set =  LoaderFactory.getDataSet(holder.getFilePath(), getName(), monitor);
 					} catch (Exception e1) {
 						return null;
 					}
 				}
 			}
 		} else {
 			try {
 				set =  getExpression().getDataSet(name, monitor);
 			} catch (Exception e) {
 				return null;
 			}
 		}	
 		
 		if (set!=null) set.setName(getName());
 		return set;
 	}
 
 	@Override
 	public ILazyDataset getLazyData(IMonitor monitor) {
 		ILazyDataset set = null;
 		if (!isExpression()) {
 			set = holder.getLazyDataset(getName());
 		} else {
 			try {
 				set = getExpression().getLazyDataSet(name, monitor);
 			} catch (Exception e) {
 				return null;
 			}
 		}		
 		if (set!=null) set.setName(getName());
 		return set;
 	}
 
 	@Override
 	public int[] getShape(boolean force) {
 		
 		if (isExpression()) {
 		    try {
 		    	final IExpressionObject expr = getExpression();
 		    	if (force) {
 					final ILazyDataset lz = expr.getLazyDataSet(getVariable(), new IMonitor.Stub());
 				    return lz.getShape();
 		    	} else {
 		    		final ILazyDataset lz = expr.getCachedLazyDataSet();
 		    		return lz!=null ? lz.getShape() : null;
 		    	}
 			} catch (Exception e) {
 				logger.error("Could not get shape of "+getVariable());
 				return force ? new int[]{1} : null;
 			}
 		}
 		
 		final String name = getName();
 		if (metaData==null || metaData.getDataShapes()==null || metaData.getDataShapes().get(name)==null) {
 			final ILazyDataset set = getLazyData(null);
 			// Assuming it has been squeezed already
 			if (set!=null) return set.getShape();
 			return force ? new int[]{1} : null;
 
 		} else if (metaData.getDataShapes().containsKey(name)) {
 			final int[] shape = metaData.getDataShapes().get(name);
 			if (force) {
 				final List<Integer> ret = new ArrayList<Integer>(shape.length);
 				for (int i : shape) if (i>1) ret.add(i);
 				Integer[] ia = ret.toArray(new Integer[ret.size()]);
 				int[]     pa = new int[ia.length];
 				for (int i = 0; i < ia.length; i++) pa[i] = ia[i];
 				return pa;
  			} else {
 				return shape;
 			}
 		}
 		return new int[]{1};
 	}
 
 
 
 	public static boolean isMementoKey(final String key) {
 		if (key==null)      return false;
 		if ("".equals(key)) return false;
 		return key.matches("CheckableObject\\$(\\d)+\\$(.+)");
 	}
 
 	private String generateMementoKey() {
 		return "CheckableObject$"+System.currentTimeMillis()+"$"+getVariable();
 	}
 
 	public static String getVariable(String memento) {
 		final String[] parts = memento.split(DELIMITER);
 		return parts[0];
 	}
 
 	public static String getName(String memento) {
 		final String[] parts = memento.split(DELIMITER);
 		return parts[1];
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (checked ? 1231 : 1237);
 		result = prime * result
 				+ ((expression == null) ? 0 : expression.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result
 				+ ((variable == null) ? 0 : variable.hashCode());
 		result = prime * result
 				+ ((filterFile == null) ? 0 : filterFile.hashCode());
 		result = prime * result + yaxis;
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
 		TransferableDataObject other = (TransferableDataObject) obj;
 		if (checked != other.checked)
 			return false;
 		if (expression == null) {
 			if (other.expression != null)
 				return false;
 		} else if (!expression.equals(other.expression))
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (variable == null) {
 			if (other.variable != null)
 				return false;
 		} else if (!variable.equals(other.variable))
 			return false;
 		if (filterFile == null) {
 			if (other.filterFile != null)
 				return false;
 		} else if (!filterFile.equals(other.filterFile))
 			return false;
 		if (yaxis != other.yaxis)
 			return false;
 		return true;
 	}
 
 	@Override
 	public String getName() {
 		if (expression!=null) return expression.getExpressionString();
 		return name;
 	}
 
 	/**
 	 * Data *must* have been cloned before doing this.
 	 * @param name
 	 */
 	@Override
 	public void setName(String name) {
 		if (isExpression()) throw new IllegalArgumentException("Cannot set the name of an expression object!");
 		ILazyDataset old = holder.getLazyDataset(getName());
 		this.name = name;
 		holder.addDataset(name, old);
 	}
 
 	@Override
 	public IExpressionObject getExpression() {
 		return expression;
 	}
 
 	@Override
 	public void setExpression(IExpressionObject expression) {
 		this.expression = expression;
 	}
 
     @Override
 	public boolean isExpression() {
     	return expression!=null;
     }
 
     @Override
 	public boolean isChecked() {
 		return checked;
 	}
 
 	@Override
 	public void setChecked(boolean checked) {
 		this.checked = checked;
 	}
 
 	@Override
 	public String toString() {
 		if (expression!=null) return expression.toString();
 		return name;
 	}
 
 	@Override
 	public String getPath() {
 		return name;
 	}
 
 	@Override
 	public String getAxis(List<ITransferableDataObject> selections, boolean is2D, boolean isXFirst) {
 		
 		if (is2D) return isChecked() ? "-" : "";
 		int axis = getAxisIndex(selections, isXFirst);
 		if (axis<0) return "";
 		if (axis==0) {
 			return isXFirst ? "X" : "Y1";
 		}
 		return "Y"+axis;
 	}
 
 	@Override
 	public int getAxisIndex(List<ITransferableDataObject> selections, boolean isXFirst) {
 		if (selections!=null&&!selections.isEmpty()) {
 			if (selections.size()>1) {
 				if (selections.contains(this)) {
 					if (selections.indexOf(this)==0) {
 						return isXFirst ? 0 : 1;
 					}
 					return yaxis;
 				}
 			} if (selections.size()==1 && selections.contains(this)) {
 				return yaxis;
 			}
 		}
 		
         return -1;
     }
 	
 	private int yaxis = 1;
 
 	@Override
 	public int getYaxis() {
 		return yaxis;
 	}
 
 	@Override
 	public void setYaxis(int yaxis) {
 		this.yaxis = yaxis;
 	}
 
     @Override
 	public String getVariable() {
 		return variable;
 	}
 
 	@Override
 	public void setVariable(String variable) {
 		this.variable = variable;
 		if (expression!=null) {
 			expression.setExpressionName(variable);
 		}
 	}
 	
 	private static final String DELIMITER = "£";
 	
 	@Override
 	public void createExpression(IVariableManager psData, String mementoKey, String memento) {
 		final String[] parts = memento.split(DELIMITER);
 		this.variable   = parts[0];
 		this.expression = service.createExpressionObject(psData, variable, parts[1]);
 	}
 	
 	@Override
 	public String getMemento() {
 		return variable+DELIMITER+getName();
 	}
 	
 	@Override
 	public String getMementoKey() {
 		if (mementoKey==null) mementoKey = generateMementoKey();
 		return mementoKey;
 	}
 
 	@Override
 	public void setMementoKey(String mementoKey) {
 		this.mementoKey = mementoKey;
 	}
 
 	@Override
 	public String getDisplayName(String rootName) {
 		String setName = toString();
 		if (!isExpression() && rootName!=null && setName.startsWith(rootName)) {
 			setName = setName.substring(rootName.length());
 		}
 		return setName;
 	}
 
 	@Override
 	public String getFileName() {
 		return (new File(holder.getFilePath())).getName();
 	}
 
 	public boolean isTransientData() {
 		return transientData;
 	}
 
 	public void setTransientData(boolean transientData) {
 		this.transientData = transientData;
 	}
 	
 	/**
 	 * Nullifies a few things to help the garbage collector
 	 * be more efficient.
 	 */
 	public void dispose() {
 		if (getExpression()!=null) getExpression().clear();
 		expression = null;
 		holder     = null;
 		metaData   = null;
 		expression = null;
 	}
 
 	public String getFilterPath() {
 		return filterFile;
 	}
 
 	public void setFilterPath(String filterFile) {
 		this.filterFile = filterFile;
 	}
  }
