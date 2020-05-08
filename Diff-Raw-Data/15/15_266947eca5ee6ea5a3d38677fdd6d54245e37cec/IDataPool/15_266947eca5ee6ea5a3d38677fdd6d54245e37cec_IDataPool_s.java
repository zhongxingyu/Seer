 /**
  * 
  */
 package de.xwic.cube;
 
 import java.util.Collection;
 
import de.xwic.cube.impl.Cube;

 /**
  * Container for dimensions, measures and cubes.
  * @author Florian Lippisch
  */
 public interface IDataPool extends IIdentifyable {
 
 	/**
 	 * Returns the list of dimensions.
 	 * @return the dimensions
 	 */
 	public abstract Collection<IDimension> getDimensions();
 
 	/**
 	 * Create a new dimension.
 	 * @param key
 	 * @return
 	 */
 	public abstract IDimension createDimension(String key);
 
 	/**
 	 * Returns the dimension with the specified key. If the dimension does not exist, 
 	 * an IllegalArgumentException is thrown. 
 	 * @param key
 	 * @return
 	 */
 	public abstract IDimension getDimension(String key);
 
 	/**
 	 * Returns true if the specified dimension exists.
 	 * @param key
 	 * @return
 	 */
 	public abstract boolean containsDimension(String key);
 
 	/**
 	 * @return the cubes
 	 */
	public abstract Collection<Cube> getCubes();
 
 	/**
 	 * Create a new cube.
 	 * @param key
 	 * @return
 	 */
 	public abstract ICube createCube(String key, IDimension[] dimensions,
 			IMeasure[] measures);
 
 	/**
 	 * Returns the cube with the specified key. If the cube does not exist, 
 	 * an IllegalArgumentException is thrown. 
 	 * @param key
 	 * @return
 	 */
 	public abstract ICube getCube(String key);
 
 	/**
 	 * Returns true if the specified cube exists.
 	 * @param key
 	 * @return
 	 */
 	public abstract boolean containsCube(String key);
 
 	/**
 	 * @return the measures
 	 */
 	public abstract Collection<IMeasure> getMeasures();
 
 	/**
 	 * Create a new cube.
 	 * @param key
 	 * @return
 	 */
 	public abstract IMeasure createMeasure(String key);
 
 	/**
 	 * Returns the measure with the specified key. If the measure does not exist, 
 	 * an IllegalArgumentException is thrown. 
 	 * @param key
 	 * @return
 	 */
 	public abstract IMeasure getMeasure(String key);
 
 	/**
 	 * Returns true if the specified measure exists.
 	 * @param key
 	 * @return
 	 */
 	public abstract boolean containsMeasure(String key);
 
 	/**
 	 * Persist the DataPool. 
 	 * @throws StorageException
 	 */
 	public abstract void save() throws StorageException;
 
 	/**
 	 * Delete the DataPool (both from memory and the underlying storage).
 	 * @throws StorageException
 	 */
 	public abstract void delete() throws StorageException;
 
 	/**
 	 * Returns the DimensionElement specified in the id. The id is created
 	 * from IDimensionElement.getId(). Sample:
 	 * <p>[GEO:EMEA/Germany]
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public abstract IDimensionElement parseDimensionElementId(String id);
 	
 }
