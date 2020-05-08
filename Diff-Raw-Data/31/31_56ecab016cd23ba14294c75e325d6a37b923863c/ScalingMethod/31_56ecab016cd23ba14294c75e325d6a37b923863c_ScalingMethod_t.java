 package cytoscape.util;
 
 
 public enum ScalingMethod {
	NONE("none (prescaled)"),
 	LINEAR_LOWER("linear/lower"),
 	LINEAR_UPPER("linear/upper"),
 	RANK_LOWER("rank/lower"),
 	RANK_UPPER("rank/upper");
 
	private String displayString;
 
	ScalingMethod(final String displayString) { this.displayString = displayString; }
 
	public String getDisplayString() { return displayString; }
 
	static public ScalingMethod getEnumValue(final String displayString) {
		for (final ScalingMethod method : ScalingMethod.values()) {
			if (method.getDisplayString().equals(displayString))
				return method;
		}
 
		throw new IllegalStateException("unknown string representation: \"" + displayString + "\"!");
 	}
 }
