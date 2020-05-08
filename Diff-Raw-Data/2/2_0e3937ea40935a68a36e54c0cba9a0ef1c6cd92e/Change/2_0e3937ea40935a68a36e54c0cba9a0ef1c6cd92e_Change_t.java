 package com.oschrenk.gis.formats.osm;
 
 /**
  * A {@link Change} describes a single change of a {@link Changeset}. It is
 * describes by the {@link ChangeType} and the {@link Primitive} that will be
  * added,
  * modified or deleted.
  * 
  * @param <T>
  *            the type of the {@link Primitive}
  * @author Oliver Schrenk <oliver.schrenk@gmail.com>
  */
 public class Change<T extends Primitive> {
 
 	/** The primitive. */
 	private T primitive;
 
 	/** The change type. */
 	private ChangeType changeType;
 
 	/**
 	 * Instantiates a new change.
 	 * 
 	 * @param primitive
 	 *            the primitive
 	 * @param changeType
 	 *            the change type
 	 */
 	public Change(T primitive, ChangeType changeType) {
 		super();
 		this.primitive = primitive;
 		this.changeType = changeType;
 	}
 
 	/**
 	 * Gets the primitive.
 	 * 
 	 * @return the primitive
 	 */
 	public T getPrimitive() {
 		return primitive;
 	}
 
 	/**
 	 * Gets the change type.
 	 * 
 	 * @return the change type
 	 */
 	public ChangeType getChangeType() {
 		return changeType;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((changeType == null) ? 0 : changeType.hashCode());
 		result = prime * result + ((primitive == null) ? 0 : primitive.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@SuppressWarnings("rawtypes")
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof Change))
 			return false;
 		Change other = (Change) obj;
 		if (changeType != other.changeType)
 			return false;
 		if (primitive == null) {
 			if (other.primitive != null)
 				return false;
 		} else if (!primitive.equals(other.primitive))
 			return false;
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "Change [primitive=" + primitive + ", changeType=" + changeType + "]";
 	}
 
 }
