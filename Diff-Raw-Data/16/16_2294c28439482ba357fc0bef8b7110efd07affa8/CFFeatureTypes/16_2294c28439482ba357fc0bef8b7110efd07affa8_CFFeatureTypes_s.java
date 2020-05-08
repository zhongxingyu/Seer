 package com.axiomalaska.cf4j;
 
 /**
  * CF Feature Types, version 1.6.
 * @see <a href="http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.6/cf-conventions.html">http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.6/cf-conventions.html</a>.
  */
 public class CFFeatureTypes {
     public static final CFFeatureType POINT = new CFFeatureType("point");
     public static final CFFeatureType TIME_SERIES = new CFFeatureType("timeSeries");
     public static final CFFeatureType TRAJECTORY = new CFFeatureType("trajectory");
     public static final CFFeatureType PROFILE = new CFFeatureType("profile");
     public static final CFFeatureType TIME_SERIES_PROFILE = new CFFeatureType("timeSeriesProfile");
     public static final CFFeatureType TRAJECTORY_PROFILE = new CFFeatureType("trajectoryProfile");
 }
