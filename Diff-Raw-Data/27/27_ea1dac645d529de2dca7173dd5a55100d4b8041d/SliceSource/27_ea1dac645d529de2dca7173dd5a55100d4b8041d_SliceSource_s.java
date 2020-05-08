 package org.dawnsci.slicing.api.system;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.dawb.common.services.IVariableManager;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 /**
  * This class represents information which configures a slice component
  * to 
  * 
  * @author fcp94556
  *
  */
 public class SliceSource {
 
 	private IVariableManager variableManager;
 	private ILazyDataset     lazySet; 
 	private String           dataName; 
 	private String           filePath;
 	private boolean          isExpression;
 	
 	public SliceSource() {
 		
 	}
 	public SliceSource(IVariableManager man, ILazyDataset l, String name, String path, boolean isExpr) {
 		setVariableManager(man);
 		setLazySet(l);
 		setDataName(name);
 		setFilePath(path);
 		setExpression(isExpr);
 	}
 	public SliceSource(IDataHolder man, ILazyDataset l, String name, String path, boolean isExpr) {
 		setVariableManager(createDataView(man));
 		setLazySet(l);
 		setDataName(name);
 		setFilePath(path);
 		setExpression(isExpr);
 	}
 	
 	private IVariableManager createDataView(final IDataHolder holder) {
 		return new IVariableManager.Stub() {
 
 			@Override
 			public List<String> getDataNames() {
 				return Arrays.asList(holder.getNames());
 			}
 
 			@Override
 			public List<String> getVariableNames() {
 				return Arrays.asList(holder.getNames());
 			}
 
 			@Override
 			public boolean isVariableName(String name, IMonitor monitor) {
 				return holder.contains(name);
 			}
 
 			@Override
 			public IDataset getVariableValue(String name, IMonitor monitor) {
 				return holder.getDataset(name);
 			}
 
 			@Override
			public ILazyDataset getLazyValue(String name, IMonitor monitor) {
				return holder.getLazyDataset(name);
 			}		
 			
 			@Override
			public IDataset getDataValue(String name, IMonitor monitor) {
				try {
					return holder.getDataset(name);
				} catch(IllegalArgumentException ie) {
					try {
						ILazyDataset lz = holder.getLazyDataset(name);
						IDataset all = lz.getSlice();
						if (all.getSize()<2) throw new Exception();
						return all;
					} catch (Exception e) {
						return null;
					}
				}			
 			}
 
 			@Override
 			public boolean isDataName(String dataName, IMonitor monitor) {
 			    return holder.contains(dataName);
 			}			
 		};
 	}
 	public ILazyDataset getLazySet() {
 		return lazySet;
 	}
 	public void setLazySet(ILazyDataset lazySet) {
 		this.lazySet = lazySet;
 	}
 	public String getDataName() {
 		return dataName;
 	}
 	public void setDataName(String dataName) {
 		this.dataName = dataName;
 	}
 	public String getFilePath() {
 		return filePath;
 	}
 	public void setFilePath(String filePath) {
 		this.filePath = filePath;
 	}
 	public boolean isExpression() {
 		return isExpression;
 	}
 	public void setExpression(boolean isExpression) {
 		this.isExpression = isExpression;
 	}
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((dataName == null) ? 0 : dataName.hashCode());
 		result = prime * result
 				+ ((filePath == null) ? 0 : filePath.hashCode());
 		result = prime * result + (isExpression ? 1231 : 1237);
 		result = prime * result + ((lazySet == null) ? 0 : lazySet.hashCode());
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
 		SliceSource other = (SliceSource) obj;
 		if (dataName == null) {
 			if (other.dataName != null)
 				return false;
 		} else if (!dataName.equals(other.dataName))
 			return false;
 		if (filePath == null) {
 			if (other.filePath != null)
 				return false;
 		} else if (!filePath.equals(other.filePath))
 			return false;
 		if (isExpression != other.isExpression)
 			return false;
 		if (lazySet == null) {
 			if (other.lazySet != null)
 				return false;
 		} else if (!lazySet.equals(other.lazySet))
 			return false;
 		return true;
 	}
 	public IVariableManager getVariableManager() {
 		return variableManager;
 	}
 	public void setVariableManager(IVariableManager variableManager) {
 		this.variableManager = variableManager;
 	}
 	
 }
