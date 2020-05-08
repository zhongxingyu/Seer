 /**
  * 
  */
 package de.xwic.cube.impl;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.xwic.cube.IDimension;
 import de.xwic.cube.IDimensionElement;
 
 /**
  * @author Florian Lippisch
  */
 public class DimensionElement extends Identifyable implements IDimensionElement, Serializable {
 
 	private static final long serialVersionUID = -4979495250481486969L;
 	protected Map<String, IDimensionElement> elementMap = new HashMap<String, IDimensionElement>();
 	protected List<IDimensionElement> elements = new ArrayList<IDimensionElement>();
 	protected DimensionElement parent;
 	protected Dimension dimension;
 	
 	protected double weight = 1.0;
 
 	/**
 	 * @param dataPool 
 	 * @param key
 	 */
 	public DimensionElement(Dimension dimension, DimensionElement parent, String key) {
 		super(key);
 		this.dimension = dimension;
 		this.parent = parent;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDimensionElement#createDimensionElement(java.lang.String)
 	 */
 	public synchronized IDimensionElement createDimensionElement(String key) {
 		if (elementMap.containsKey(key)) {
 			throw new IllegalArgumentException("An element with that key already exists.");
 		}
 		DimensionElement element = new DimensionElement(dimension, this, key);
 		elementMap.put(key, element);
 		elements.add(element);
 		return element;
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDimensionElement#containsDimensionElement(java.lang.String)
 	 */
 	public boolean containsDimensionElement(String key) {
 		return elementMap.containsKey(key);
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDimensionElement#getDimensionElement(java.lang.String)
 	 */
 	public IDimensionElement getDimensionElement(String key) {
 		IDimensionElement element = elementMap.get(key);
 		if (element == null) {
 			throw new IllegalArgumentException("An element with the key '" + key + "' does not exist.");
 		}
 		return element;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDimensionElement#getDimensionElements()
 	 */
 	public List<IDimensionElement> getDimensionElements() {
 		return Collections.unmodifiableList(elements);
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDimensionElement#remove()
 	 */
 	public void remove() {
 		parent.removeDimensionElement(this);
 	}
 
 	/**
 	 * @param dimensionElement
 	 */
 	void removeDimensionElement(DimensionElement dimensionElement) {
 		
 		elements.remove(dimensionElement);
 		elementMap.remove(dimensionElement.getKey());
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDimensionElement#getDimension()
 	 */
 	public IDimension getDimension() {
 		return dimension;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDimensionElement#getParent()
 	 */
 	public IDimensionElement getParent() {
 		return parent;
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDimensionElement#getID()
 	 */
 	public String getID() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("[")
 		  .append(getDimension().getKey())
 		  .append(":");
 		sb.append(getPath());
 		sb.append("]");
 		return sb.toString();
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDimensionElement#getPath()
 	 */
 	public String getPath() {
 		StringBuilder sb = new StringBuilder();
 		IDimensionElement pr = parent;
 		while (pr != null && !(pr instanceof Dimension)) {
 			sb.insert(0, "/");
 			sb.insert(0, pr.getKey());
 			pr = pr.getParent();
 		}
 		sb.append(getKey());
 		return sb.toString();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result
 				+ ((dimension == null || dimension == this) ? 0 : dimension.hashCode());
 		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
 		final DimensionElement other = (DimensionElement) obj;
 		if (dimension == null) {
 			if (other.dimension != null)
 				return false;
 		} else if (dimension != this && !dimension.equals(other.dimension))
 			return false;
 		if (parent == null) {
 			if (other.parent != null)
 				return false;
 		} else if (!parent.equals(other.parent))
 			return false;
 		return true;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDimensionElement#totalSize()
 	 */
 	public int totalSize() {
 		int size = elements.size();
 		for (IDimensionElement child : elements) {
 			size += child.totalSize();
 		}
 		return size;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDimensionElement#isLeaf()
 	 */
 	public boolean isLeaf() {
 		return elements.size() == 0;
 	}
 
 	/**
 	 * @return the weight
 	 */
 	public double getWeight() {
 		return weight;
 	}
 
 	/**
 	 * @param weight the weight to set
 	 */
 	public void setWeight(double weight) {
 		//System.out.println(NumberFormat.getNumberInstance().format(weight));
 		this.weight = weight;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.xwic.cube.IDimensionElement#getElementsTotalWeight()
 	 */
 	public double getElementsTotalWeight() {
 		double total = 0;
 		for (IDimensionElement de : elements) {
 			total += de.getWeight();
 		}
 		return total;
 	}
 	
 	/**
 	 * Returns the element specified in this path. The keys in the path
 	 * are separated by a slash.
 	 * @param path
 	 * @return
 	 */
 	public IDimensionElement parsePath(String path) {
 		
		if (path.length() == 0) {
			return this;
		}
		
 		IDimensionElement element = this;
 		int idxPathSep;
 		int idxPathStart = 0;
 		do {
 			idxPathSep = path.indexOf('/', idxPathStart);
 			String elmKey;
 			if (idxPathSep == -1) {
 				elmKey = path.substring(idxPathStart);
 			} else {
 				elmKey = path.substring(idxPathStart, idxPathSep);
 			}
 			element = element.getDimensionElement(elmKey);
 			idxPathStart = idxPathSep + 1;
 		} while (idxPathSep != -1);
 		return element;
 
 	}
 	
 }
