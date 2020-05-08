 package cytoscape.util;
 
 
 public enum ScalingMethod {
	NONE("none (e.g. LOD score)"),
 	LINEAR_LOWER("linear/lower"),
 	LINEAR_UPPER("linear/upper"),
 	RANK_LOWER("rank/lower"),
 	RANK_UPPER("rank/upper");
 
	private String asString;
 
	ScalingMethod(final String asString) { this.asString = asString; }
 
	@Override public String toString() { return asString; }
 
	static public ScalingMethod getEnumValue(final String asString) {
		if (asString.equals(NONE.toString()))
			return NONE;
		if (asString.equals(LINEAR_LOWER.toString()))
			return LINEAR_LOWER;
		if (asString.equals(LINEAR_UPPER.toString()))
			return LINEAR_UPPER;
		if (asString.equals(RANK_LOWER.toString()))
			return RANK_LOWER;
		if (asString.equals(RANK_UPPER.toString()))
			return RANK_UPPER;
 
		throw new IllegalStateException("unknown string representation: \"" + asString + "\"!");
 	}
 }
