 /**
  * 
  */
 package de.xwic.cube.impl;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import de.xwic.cube.ICube;
 import de.xwic.cube.IDataPool;
 import de.xwic.cube.IDataPoolManager;
 import de.xwic.cube.IDimension;
 import de.xwic.cube.IDimensionElement;
 import de.xwic.cube.IMeasure;
 import de.xwic.cube.StorageException;
 
 /**
  * @author Florian Lippisch
  */
 public class DataPool extends Identifyable implements IDataPool, Serializable {
 
 	private static final long serialVersionUID = -8857844492316508015L;
	private Map<String, ICube> cubeMap = new LinkedHashMap<String, ICube>();
 	private Map<String, IDimension> dimensionMap = new LinkedHashMap<String, IDimension>();
 	private Map<String, IMeasure> measureMap = new LinkedHashMap<String, IMeasure>();
 	private transient DataPoolManager dataPoolManager;
 
 	
 	/**
 	 * @param dataPoolManager 
 	 * @param key
 	 */
 	public DataPool(DataPoolManager dataPoolManager, String key) {
 		super(key);
 		this.dataPoolManager = dataPoolManager;
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDataPool#getDimensions()
 	 */
 	public Collection<IDimension> getDimensions() {
 		return Collections.unmodifiableCollection(dimensionMap.values());
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDataPool#createDimension(java.lang.String)
 	 */
 	public synchronized Dimension createDimension(String key) {
 		if (dimensionMap.containsKey(key)) {
 			throw new IllegalArgumentException("A dimension with that key already exists: " + key);
 		}
 		Dimension newDim = new Dimension(this, key);
 		dimensionMap.put(key, newDim);
 		return newDim;
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDataPool#getDimension(java.lang.String)
 	 */
 	public IDimension getDimension(String key) {
 		IDimension dim = dimensionMap.get(key);
 		if (dim == null) {
 			throw new IllegalArgumentException("A dimension with that key does not exist: " + key);
 		}
 		return dim;
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDataPool#containsDimension(java.lang.String)
 	 */
 	public boolean containsDimension(String key) {
 		return dimensionMap.containsKey(key);
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDataPool#getCubes()
 	 */
	public Collection<ICube> getCubes() {
 		return Collections.unmodifiableCollection(cubeMap.values());
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDataPool#createCube(java.lang.String, de.xwic.cube.Dimension[], de.xwic.cube.Measure[])
 	 */
 	public synchronized ICube createCube(String key, IDimension[] dimensions, IMeasure[] measures) {
 		if (cubeMap.containsKey(key)) {
 			throw new IllegalArgumentException("A cube with that key already exists: " + key);
 		}
 		Cube newCube = new Cube(this, key, dimensions, measures);
 		cubeMap.put(key, newCube);
 		return newCube;
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDataPool#getCube(java.lang.String)
 	 */
 	public ICube getCube(String key) {
 		ICube cube = cubeMap.get(key);
 		if (cube == null) {
 			throw new IllegalArgumentException("A cube with that key does not exist: " + key);
 		}
 		return cube;
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDataPool#containsCube(java.lang.String)
 	 */
 	public boolean containsCube(String key) {
 		return cubeMap.containsKey(key);
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDataPool#getMeasures()
 	 */
 	public Collection<IMeasure> getMeasures() {
 		return Collections.unmodifiableCollection(measureMap.values());
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDataPool#createMeasure(java.lang.String)
 	 */
 	public synchronized Measure createMeasure(String key) {
 		if (measureMap.containsKey(key)) {
 			throw new IllegalArgumentException("A measure with that key already exists: " + key);
 		}
 		Measure newMeasure = new Measure(this, key);
 		measureMap.put(key, newMeasure);
 		return newMeasure;
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDataPool#getMeasure(java.lang.String)
 	 */
 	public IMeasure getMeasure(String key) {
 		IMeasure measure = measureMap.get(key);
 		if (measure == null) {
 			throw new IllegalArgumentException("A measure with that key does not exist: " + key);
 		}
 		return measure;
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDataPool#containsMeasure(java.lang.String)
 	 */
 	public boolean containsMeasure(String key) {
 		return measureMap.containsKey(key);
 	}
 
 	/**
 	 * INTERNAL: remove the specified dimension.
 	 * @param dimension
 	 */
 	void removeDimension(Dimension dimension) {
 		// TODO check if cubes exist that use this dimension.
 		// TODO check that cube is empty (no cells exist).
 		if (dimensionMap.containsKey(dimension.getKey())) {
 			dimensionMap.remove(dimension.getKey());
 		}
 		
 	}
 
 	/**
 	 * @param cube
 	 */
 	void removeCube(ICube cube) {
 		if (cubeMap.containsKey(cube.getKey())) {
 			cubeMap.remove(cube.getKey());
 		}
 		
 	}
 
 	/**
 	 * @param measure
 	 */
 	void removeMeasure(Measure measure) {
 		// TODO check if its used in a cube.
 		if (measureMap.containsKey(measure.getKey())) {
 			measureMap.remove(measure.getKey());
 		}
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 //		final int prime = 31;
 		int result = super.hashCode();
 //		result = prime * result
 //				+ ((dataPoolManager == null) ? 0 : dataPoolManager.hashCode());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 //		final DataPool other = (DataPool) obj;
 //		if (dataPoolManager == null) {
 //			if (other.dataPoolManager != null)
 //				return false;
 //		} else if (!dataPoolManager.equals(other.dataPoolManager))
 //			return false;
 		return true;
 	}
 
 	/**
 	 * @return the dataPoolManager
 	 */
 	public IDataPoolManager getDataPoolManager() {
 		return dataPoolManager;
 	}
 
 	/**
 	 * @param dataPoolManager the dataPoolManager to set
 	 */
 	void setDataPoolManager(DataPoolManager dataPoolManager) {
 		this.dataPoolManager = dataPoolManager;
 	}
 	
 	/**
 	 * Store the DataPool. 
 	 * @throws StorageException 
 	 */
 	public void save() throws StorageException {
 		dataPoolManager.saveDataPool(this);
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDataPool#delete()
 	 */
 	public void delete() throws StorageException {
 		dataPoolManager.deleteDataPool(this);
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDataPool#parseDimensionElementId(java.lang.String)
 	 */
 	public IDimensionElement parseDimensionElementId(String id) {
 		/**
 		 * Copied to CubeUtil.parseDimensionElementId(String) and used there as well ! ! !
 		 */ 
 		int start = id.indexOf('[');
 		if (start == -1) {
 			throw new IllegalArgumentException("Missing starting [");
 		}
 		int end = id.indexOf(']', start);
 		if (end == -1) {
 			throw new IllegalArgumentException("Missing ending ]");
 		}
 		String part = id.substring(start + 1, end);
 		int idxDimSep = part.indexOf(':');
 		String elmKeys;
 		IDimension dimension;
 		// does the key contain a dimension key?
 		if (idxDimSep == -1) { // no key given
 			throw new IllegalArgumentException("No dimension key found");
 		}
 		
 		String dimKey = part.substring(0, idxDimSep);
 		elmKeys = part.substring(idxDimSep + 1);
 		dimension = getDimension(dimKey);
 		IDimensionElement element = dimension;
 		if (!"*".equals(elmKeys)) { // specific key given.
 			int idxPathSep;
 			int idxPathStart = 0;
 			do {
 				idxPathSep = elmKeys.indexOf('/', idxPathStart);
 				String elmKey;
 				if (idxPathSep == -1) {
 					elmKey = elmKeys.substring(idxPathStart);
 				} else {
 					elmKey = elmKeys.substring(idxPathStart, idxPathSep);
 				}
 				element = element.getDimensionElement(elmKey);
 				idxPathStart = idxPathSep + 1;
 			} while (idxPathSep != -1);
 		}
 		return element;
 	}
 	
 }
