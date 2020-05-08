 /**
  * 
  */
 package de.xwic.cube.impl;
 
 import java.io.Serializable;
 
 import de.xwic.cube.IDataPool;
 import de.xwic.cube.IDimension;
 
 
 /**
  * 
  * @author Florian Lippisch
  */
 public class Dimension extends DimensionElement implements IDimension, Serializable {
 
 	private static final long serialVersionUID = 7146991699834998971L;
 	private final DataPool dataPool;
 	
 	/**
 	 * @param dataPool 
 	 * @param key
 	 */
 	public Dimension(DataPool dataPool, String key) {
 		super(null, null, key);
 		this.dataPool = dataPool;
 		this.dimension = this;
 	}
 
	/* (non-Javadoc)
	 * @see de.xwic.cube.impl.DimensionElement#getPath()
	 */
	@Override
	public String getPath() {
		return ""; // the path of the dimension is always empty.
	}
	
 	/**
 	 * @return the dataPool
 	 */
 	IDataPool getDataPool() {
 		return dataPool;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDimension#remove()
 	 */
 	public void remove() {
 		dataPool.removeDimension(this);
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.DimensionElement#getDimension()
 	 */
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDimension#getDimension()
 	 */
 	@Override
 	public IDimension getDimension() {
 		return this;
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.DimensionElement#getID()
 	 */
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDimension#getID()
 	 */
 	@Override
 	public String getID() {
 		return "[" + getKey() + ":*]";
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result
 				+ ((dataPool == null) ? 0 : dataPool.hashCode());
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
 		final Dimension other = (Dimension) obj;
 		if (dataPool == null) {
 			if (other.dataPool != null)
 				return false;
 		} else if (!dataPool.equals(other.dataPool))
 			return false;
 		return true;
 	}
 	
 }
