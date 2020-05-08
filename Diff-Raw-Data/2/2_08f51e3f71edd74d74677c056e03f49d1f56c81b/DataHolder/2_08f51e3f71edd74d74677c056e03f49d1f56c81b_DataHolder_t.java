 /*
  * Copyright 2011 Diamond Light Source Ltd.
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
 
 package uk.ac.diamond.scisoft.analysis.io;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.apache.commons.collections4.map.ListHashedMap;
 import org.dawb.apache.commons.collections4.map.SynchronizedListHashedMap;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IMetadataProvider;
 
 /**
  * This class is to marshal all the data for the purpose of loading from or saving to a file
  * directly or via a ScanFileHolder.
  * <p>
  * This is designed to take in any dataset obeying the IDataset interface but output an
  * object that is a subclass of AbstractDataset - the dataset will be converted if necessary.
  * 
  * This implementation does not permit duplicated names.
  */
 public class DataHolder implements IMetadataProvider, IDataHolder, Serializable {
 	
 	protected static final Logger logger = LoggerFactory.getLogger(DataHolder.class);
 
 	/**
 	 * List containing all the name and data pairs (to be) loaded.
 	 */
 	private SynchronizedListHashedMap<String, ILazyDataset> nameDataMappings;
 
 	/**
 	 * List containing metadata
 	 */
 	private IMetaData metadata;
 
 	/**
 	 * Loader class string
 	 */
 	private String loaderClass;
 	
 	/**
 	 * The path to the original file loaded (if any)
 	 */
 	private String filePath;
 
 	/**
 	 * This must create the three objects which will be put into the ScanFileHolder
 	 */
 	public DataHolder() {
 		nameDataMappings = SynchronizedListHashedMap.synchronizedListHashedMap(new ListHashedMap<String, ILazyDataset>());
 		metadata = new Metadata();
 	}
 
	
	
 	/**
 	 * The current data as a map of lazy datasets.
 	 * @return map of lazy datasets with keys from their corresponding names
 	 */
 	@Override
 	public Map<String, ILazyDataset> toLazyMap() {
 		return nameDataMappings.clone();
 	}
 
 	/**
 	 * Does not clone the meta data.
 	 * @return shallow copy of DataHolder
 	 */
 	@Override
 	public IDataHolder clone() {
 		
 		DataHolder ret = new DataHolder();
 		ret.nameDataMappings.putAll(nameDataMappings);
 		ret.metadata    = metadata;
 		ret.filePath    = filePath;
 		ret.loaderClass = loaderClass;
 		return ret;
 	}
 
 	/**
 	 * Adds a dataset and its name into the two vectors of the Object.
 	 * 
 	 * Replaces any datasets of the same name already existing.
 	 *  
 	 * @param name
 	 *            the name of the dataset which is to be added
 	 * @param dataset
 	 *            the actual data of the dataset
 	 */
 	@Override
 	public boolean addDataset(String name, ILazyDataset dataset) {
 		boolean ret = nameDataMappings.containsKey(name);
 		nameDataMappings.put(name, dataset);
 		return ret;
 	}
 
 	/**
 	 * Adds a dataset, metadata and its name. This is for Diffraction data
 	 * 
 	 * Replaces any datasets of the same name already existing.
 	 * 
 	 * @param name
 	 *            the name of the dataset which is to be added
 	 * @param dataset
 	 *            the actual data of the dataset.
 	 * @param metadata
 	 *            the metadata that is associated with the dataset
 	 */
 	public boolean addDataset(String name, ILazyDataset dataset, IMetaData metadata) {
 		boolean ret = addDataset(name, dataset);
 		this.metadata = metadata;
 		return ret;
 	}
 	
 	/**
 	 * Add a ImetaData object
 	 * @param metadata which is an object implementing IMetaData
 	 */
 
 	public void setMetadata(IMetaData metadata) {
 		this.metadata = metadata;
 	}
 
 	/**
 	 * @return an object implementing IMetaData
 	 */
 	@Override
 	public IMetaData getMetadata() {
 		return metadata;
 	}
 
 	/**
 	 * @return List of datasets
 	 */
 	public List<ILazyDataset> getList() {
 		return new ArrayList<ILazyDataset>(nameDataMappings.values());
 	}
 
 	/**
 	 * This pulls out the dataset which could be lazy, maintaining its laziness.
 	 * @param index
 	 * @return Generic dataset with given index in holder
 	 */
 	@Override
 	public ILazyDataset getLazyDataset(int index) {
 		return nameDataMappings.getValue(index);
 	}
 
 	/**
 	 * This pulls out the dataset which could be lazy, maintaining its laziness.
 	 * @param name
 	 * @return Generic dataset with given name
 	 */
 	@Override
 	public ILazyDataset getLazyDataset(String name) {
 		return nameDataMappings.get(name);
 	}
 
 	/**
 	 * Set a generic dataset at given index. Ensure the index is in range otherwise an exception
 	 * will occur
 	 * @param index
 	 * @param dataset
 	 */
 	public void setDataset(int index, ILazyDataset dataset) {
 		nameDataMappings.setValue(index, dataset);
 	}
 
 	/**
 	 * Set a generic dataset with given name
 	 * @param name
 	 * @param dataset
 	 */
 	public void setDataset(String name, ILazyDataset dataset) {
 		nameDataMappings.put(name, dataset);
 	}
 
 	/**
 	 * This does not retrieve lazy datasets.
 	 * @param index
 	 * @return Generic dataset with given index in holder
 	 */
 	@Override
 	public AbstractDataset getDataset(int index) {
 		return DatasetUtils.convertToAbstractDataset(getLazyDataset(index));
 	}
 
 	/**
 	 * This does not retrieve lazy datasets.
 	 * @param name
 	 * @return Generic dataset with given name
 	 */
 	@Override
 	public AbstractDataset getDataset(String name) {
 		return DatasetUtils.convertToAbstractDataset(getLazyDataset(name));
 	}
 
 	/**
 	 * @param name
 	 * @return true if data holder contains name 
 	 * @see java.util.List#contains(Object)
 	 */
 	@Override
 	public boolean contains(String name) {
 		return nameDataMappings.containsKey(name);
 	}
 
 	/**
 	 * @param name
 	 * @return index of dataset with given name
 	 * @see java.util.List#indexOf(Object)
 	 */
 	public int indexOf(String name) {
 		return nameDataMappings.indexOf(name);
 	}
 
 	/**
 	 * @return Array of dataset names
 	 */
 	@Override
 	public String[] getNames() {
 		return nameDataMappings.toArray(new String[nameDataMappings.size()]);
 	}
 
 	/**
 	 * @param index
 	 * @return Dataset name at given index
 	 */
 	@Override
 	public String getName(final int index) {
 		try {
 			return nameDataMappings.get(index);
 		} catch( IndexOutOfBoundsException e ) {
 			return null;
 		}
 	}
 
 	/**
 	 * @return Number of datasets
 	 */
 	@Override
 	public int size() {
 		return nameDataMappings.size();
 	}
 
 	/**
 	 * @return Number of unique dataset names
 	 */
 	@Override
 	public int namesSize() {
 		return size();
 	}
 
 	/**
 	 * Clear list of names and datasets
 	 * @see java.util.List#clear()
 	 */
 	public void clear() {
 		nameDataMappings.clear();
 		metadata = null;
 	}
 
 	/**
 	 * Remove name and dataset at index
 	 * @param index
 	 * @see java.util.List#remove(int)
 	 */
 	public void remove(int index) {
 		nameDataMappings.remove(index);
 	}
 
 	/**
 	 * @return class
 	 */
 	@SuppressWarnings("unchecked")
 	public Class<? extends AbstractFileLoader> getLoaderClass() {
 		try {
 			return (Class<? extends AbstractFileLoader>) Class.forName(loaderClass);
 		} catch (ClassNotFoundException e) {
 			logger.error("No class found for {}", loaderClass, e);
 		}
 		return null;
 	}
 
 	public void setLoaderClass(Class<? extends AbstractFileLoader> clazz) {
 		loaderClass = clazz.getName();
 	}
 
 	@Override
 	public String getFilePath() {
 		return filePath;
 	}
 
 	public void setFilePath(String filePath) {
 		this.filePath = filePath;
 	}
 
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
 		result = prime * result + ((loaderClass == null) ? 0 : loaderClass.hashCode());
 		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
 		result = prime * result + ((nameDataMappings == null) ? 0 : nameDataMappings.hashCode());
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
 		DataHolder other = (DataHolder) obj;
 		if (filePath == null) {
 			if (other.filePath != null)
 				return false;
 		} else if (!filePath.equals(other.filePath))
 			return false;
 		if (loaderClass == null) {
 			if (other.loaderClass != null)
 				return false;
 		} else if (!loaderClass.equals(other.loaderClass))
 			return false;
 		if (metadata == null) {
 			if (other.metadata != null)
 				return false;
 		} else if (!metadata.equals(other.metadata))
 			return false;
 		if (nameDataMappings == null) {
 			if (other.nameDataMappings != null)
 				return false;
 		} else if (!nameDataMappings.equals(other.nameDataMappings))
 			return false;
 		return true;
 	}
 
 }
