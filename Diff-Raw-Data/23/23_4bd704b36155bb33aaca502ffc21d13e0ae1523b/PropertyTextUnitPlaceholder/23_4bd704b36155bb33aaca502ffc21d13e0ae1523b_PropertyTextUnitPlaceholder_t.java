 package net.sf.okapi.common.filters;
 
 import net.sf.okapi.common.HashCodeUtil;
 
 public class PropertyTextUnitPlaceholder implements Comparable<PropertyTextUnitPlaceholder> {
 	public enum PlaceholderType {
 		TRANSLATABLE, READ_ONLY_PROPERTY, WRITABLE_PROPERTY
 	};
 
 	private PlaceholderType type;
 
 	private String name;
 	private String value;
 
 	private int mainStartPos;
 	private int mainEndPos;
 
 	public PropertyTextUnitPlaceholder(PlaceholderType type, String name, String value, int mainStartPos, int mainEndPos) {
 		this.type = type;
 		this.name = name;
		this.value = value;
 		this.mainStartPos = mainStartPos;
 		this.mainEndPos = mainEndPos;
 	}
 
 	public PlaceholderType getType() {
 		return type;
 	}
 
 	public void setType(PlaceholderType type) {
 		this.type = type;
 	}
 
 	public int getMainStartPos() {
 		return mainStartPos;
 	}
 
 	public void setMainStartPos(int mainStartPos) {
 		this.mainStartPos = mainStartPos;
 	}
 
 	public int getMainEndPos() {
 		return mainEndPos;
 	}
 
 	public void setMainEndPos(int mainEndPos) {
 		this.mainEndPos = mainEndPos;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 	public String getValue() {
 		return value;
 	}
 
 	/*
 	 * Sort is based in MainStartPos order
 	 */
 	public int compareTo(PropertyTextUnitPlaceholder aThat) {
 		final int BEFORE = -1;
 		final int EQUAL = 0;
 		final int AFTER = 1;
 
 		// this optimization is usually worthwhile, and can
 		// always be added
 		if (this.getMainStartPos() == aThat.getMainStartPos())
 			return EQUAL;
 
 		// primitive numbers follow this form
 		if (this.getMainStartPos() < aThat.getMainStartPos())
 			return BEFORE;
 		if (this.getMainStartPos() > aThat.getMainStartPos())
 			return AFTER;
 
 		return EQUAL;
 	}
 
 	/**
 	 * Define equality of state.
 	 */
 	@Override
 	public boolean equals(Object aThat) {
 		if (this == aThat)
 			return true;
 		if (!(aThat instanceof PropertyTextUnitPlaceholder))
 			return false;
 
 		PropertyTextUnitPlaceholder that = (PropertyTextUnitPlaceholder) aThat;
 		return (this.getMainStartPos() == that.getMainStartPos());
 	}
 
 	/**
 	 * A class that overrides equals must also override hashCode.
 	 */
 	@Override
 	public int hashCode() {
 		int result = HashCodeUtil.SEED;
 		result = HashCodeUtil.hash(result, this.getMainStartPos());
 		return result;
 	}
 
 }
