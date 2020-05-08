 package com.agopinath.lthelogutil.streams;
 
 public final class LStreamConfig {
 	static final String LINE_SEPARATOR = System.getProperty("line.separator");
 	static final String LSTREAMID_UNASSIGNED = "UNASSIGNED";
 	
 	public static final boolean isLStreamIDUnassigned(final String lStreamID) {
 		return lStreamID.equals(LSTREAMID_UNASSIGNED);
 	}
 }
