 package org.vamdc.portal.session.queryBuilder.unitConv;
 
 /**
  * Custom simple converters, used both in in-field unit conversions and inter-field conversions 
  *
  *
  */
public class CustomConverters {
 	
 	private final static Double C = 2.99792458e5;
 	
 	class DirectConverter implements Converter{
 		public Double convert(Double value) { return value; }
 	}
 	public static DirectConverter Direct(){ return new CustomConverters().new DirectConverter(); }
 	
 	class EVToWnConverter implements Converter{
 		public Double convert(Double value) { return 8065.54429*value; }
 	}
 	public static EVToWnConverter EVToWn(){ return new CustomConverters().new EVToWnConverter(); }
 	
 	class RydToWnConverter implements Converter{
 		public Double convert(Double value) { return 109737.31568539*value; }
 	}
 	public static RydToWnConverter RydToWn(){ return new CustomConverters().new RydToWnConverter(); }
 	
 	class FreqToWavenumberConverter implements Converter{
 		public Double convert(Double value) {
 			if (value!=null && value!=0)
 				return C*1e7/value;
 			else return Double.NaN;
 		}
 	}
 	
 	public static FreqToWavenumberConverter MHzToWn(){ return new CustomConverters().new FreqToWavenumberConverter(); }
 	
 }
