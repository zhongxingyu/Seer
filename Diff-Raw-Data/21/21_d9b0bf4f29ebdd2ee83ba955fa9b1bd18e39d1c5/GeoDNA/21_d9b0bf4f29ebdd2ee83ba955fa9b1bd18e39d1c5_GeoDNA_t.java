 package geo.dna;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 //---------------------------------------------------
 //GeoDNA.java - (C) KD 2012, Richard Fox 2012
 //---------------------------------------------------
 //Ported from js to java by Richard Fox
 //---------------------------------------------------
 //Converts between lat/lon and a "geodna" code,
 //which is a single string representing a point
 //on the earth's surface.   The string is basically
 //an approximation to the lat/lon coordinate,
 //and the longer the string, the more accurate it
 //will be.   In general, coordinates that are
 //close together will share a string prefix,
 //making these codes very useful for providing
 //very fast proximity searching using only
 //text-based approaches (eg. SQL's "LIKE" operator)
 //---------------------------------------------------
 //http://www.geodna.org
 //---------------------------------------------------
 
 public class GeoDNA {
 	private static final int RADIUS_OF_EARTH = 6378100;
 	private static final String[] ALPHABET = {"g","a","t","c"};
 	private static HashMap<String,Integer> DECODE_MAP = new HashMap<String,Integer>();
 	static
 	{
 		DECODE_MAP.put("g", 0);
 		DECODE_MAP.put("a", 1);
 		DECODE_MAP.put("t", 2);
 		DECODE_MAP.put("c", 3);	
 	}
 	private static HashMap<String,String> PAIR_MAP = new HashMap<String,String>();
 	static
 	{
 		PAIR_MAP.put("g", "c");
 		PAIR_MAP.put("a", "t");
 		PAIR_MAP.put("t", "a");
 		PAIR_MAP.put("c", "c");
 		PAIR_MAP.put("w", "e");
 		PAIR_MAP.put("e", "w");
 	}
 	private static String join(String r[],String d)
 	{
 	        StringBuilder sb = new StringBuilder();
 	        int i;
 	        for(i=0;i<r.length-1;i++)
 	            sb.append(r[i]+d);
 	        return sb.toString()+r[i];
 	}
 	public static double mod( double x, double m ){
 		 return ( x % m + m ) % m;
 	}
 	public static double[] normalise ( double lat, double lon ){
 	     return new double[]{
 	         GeoDNA.mod(( lat + 90.0 ), 180.0 ) - 90.0,
 	         GeoDNA.mod(( lon + 180.0 ), 360.0 ) - 180.0,
 	     };
 	}
 	public static String encode(double latitude, double longitude){
 		return GeoDNA.encode(latitude, longitude,22,false);
 	}
 	public static String encode(double latitude, double longitude, int precision){
 		return GeoDNA.encode(latitude, longitude, precision, false);
 	}
 	public static String encode(double latitude, double longitude, int precision, boolean radians){
 		String geodna ="";
 		
 		double[] loni = new double[2];
 		double[] lati = new double[]{ -90.0, 90.0 };
 		
 		
 		double[] bits = GeoDNA.normalise( latitude, longitude );
 		latitude = bits[0];
 	    longitude = bits[1];
 	    
 	    if ( radians ) {
 	         latitude  = Math.toDegrees( latitude );
 	         longitude = Math.toDegrees( longitude );
 		}
 	    
 	    if ( longitude < 0 ) {
 	         geodna = geodna + 'w';
 	         //TODO Is there a better way to do this?
 	         //loni = new double[]{ -180.0, 0.0 };
 	         loni[0] = -180.0;
 	         loni[1] = 0.0;
 	    } else {
 	         geodna = geodna + 'e';
 	         //TODO Same as above
 	         //loni = new double[]{ 0.0, 180.0 };
 	         loni[0] = 0.0;
 	         loni[1] = 180.0;
 	    }
 	    
 	    while ( geodna.length() < precision ) {
 	         int ch = 0;
 
 	         double mid = ( loni[0] + loni[1] ) / 2.0;
 	         if ( longitude > mid ) {
 	        	 ch = ch | 2;
 	        	 loni[0] = mid;
 	         } else {
 	             loni[1] = mid;
 	         }
 
 	         mid = ( lati[0] + lati[1] ) / 2.0;
 	         if ( latitude > mid ) {
 	             ch = ch | 1;
 	             lati[0] = mid;
 	         } else {
 	             lati[1] = mid;
 	         }
 
 	         geodna = geodna + ALPHABET[ch];
 	    }
 	    return geodna;
 	}
 	//A bit of fun, Pairs the GeoDNA up with its counterpart as though it was real DNA
 	//Could be seen as inverse not sure till i test it :(
 	public static String pair(String gd, boolean swapHemispheres){
 		String[] chars = gd.split("");
 		if(swapHemispheres){
 			chars[1] = PAIR_MAP.get(chars[1]);
 		}
 		for ( int i = 1; i < chars.length; i++ ) {
 			chars[i] = PAIR_MAP.get(chars[i]);
 		}
 		return join(chars, "");
 	}
 	// locates the min/max lat/lons around the geo_dna
 	public static double[][] boundingBox( String geodna ) {
	     char[] chars = geodna.toCharArray();
 
 	     double[] loni = new double[2];
 	     double[] lati= { -90.0, 90.0 };
 
	     char first = chars[0];
 
	     if ( first == 'w' ) {
 	         loni[0] = -180.0;
 	         loni[1] = 0.0;
	     } else if ( first == 'e' ) {
 	         loni[0] = 0.0;
 	         loni[1] = 180.0;
 	     }
 
 	     for ( int i = 1; i < chars.length; i++ ) {
	         char c  = chars[i];
	         int cd = DECODE_MAP.get(Character.toString(c));
	         if ( (cd & 2) != 0 ) {
 	             loni[0] = ( loni[0] + loni[1] ) /2.0;
 	         } else {
 	             loni[1] = (loni[0]+loni[1])/2.0;
 	         }
	         if ( (cd & 1) != 0 ) {
 	             lati[0] = (lati[0] + lati[1])/2;
 	         } else {
 	        	 lati[1] = (lati[0] + lati[1])/2.0;
 	         }
 	     }
 	     double [][] r = {lati,loni};
 	     return r;
 	 }
 	public static double[] decode( String geodna) {
 		return GeoDNA.decode(geodna, false);
 	}
 	public static double[] decode( String geodna, boolean radians ) {
 	     //options = options || {};
 
 	     double[][] bits = GeoDNA.boundingBox( geodna );
 	     double[] lati = bits[0];
 	     double[] loni = bits[1];
 
 	     double lat = ( lati[0] + lati[1] ) / 2.0;
 	     double lon = ( loni[0] + loni[1] ) / 2.0;
 	     
 	     if(radians){
 	    	 lat = Math.toRadians(lat);
 	    	 lon = Math.toRadians(lon);
 	     }
 	     
 	     return new double[] { lat, lon };
 	}
 	public static double[] addVector(String geodna, double dy, double dx){
 		double[] bits = GeoDNA.decode(geodna);
 		return new double[]{
 				GeoDNA.mod( ( bits[ 0 ] + 90.0 + dy ), 180.0 ) - 90.0,
 				GeoDNA.mod( ( bits[ 1 ] + 180.0 + dx ), 360.0 ) - 180.0
 		};
 	}
 	public static String pointFromPointBearingAndDistance(String geodna, double bearing, double distance){
 		return GeoDNA.pointFromPointBearingAndDistance(geodna, bearing, distance,22);
 	}
 	public static String pointFromPointBearingAndDistance(String geodna, double bearing, double distance, int precision){
 		distance = distance * 1000;
 		double [] bits = GeoDNA.decode(geodna,true);
 		double lat1 = bits[0];
 		double lon1 = bits[1];
 		double lat2 = Math.asin( Math.sin( lat1 ) * Math.cos(distance) / RADIUS_OF_EARTH) 
 					+  Math.cos( lat1 ) * Math.sin( distance / RADIUS_OF_EARTH ) * Math.cos( bearing );
 		double lon2 = lon1 + Math.atan2( Math.sin( bearing ) * Math.sin( distance / RADIUS_OF_EARTH ) * Math.cos( lat1 ),
                 						 Math.cos( distance / RADIUS_OF_EARTH ) - Math.sin( lat1 ) * Math.sin( lat2 ));
 		return GeoDNA.encode(lat2, lon2,precision, true);
 	}
 	
 	public static double distanceInKm(String ga, String gb){
 		double[] a = GeoDNA.decode(ga);
 		double[] b = GeoDNA.decode(gb);
 		
 		if ( a[1] * b[1] < 0.0 && Math.abs( a[1] - b[1] ) > 180.0 ) {
 	         a = GeoDNA.addVector( ga, 0.0, 180.0 );
 	         b = GeoDNA.addVector( gb, 0.0, 180.0 );
 	    }
 		
 		double x = ( Math.toRadians(b[1]) - Math.toRadians(a[1]) ) * Math.cos( ( Math.toRadians(a[0]) + Math.toRadians(b[0])) / 2 );
 	    double y = ( Math.toRadians(b[0]) - Math.toRadians(a[0]) );
 	    
 	    double d = Math.sqrt( x*x + y*y ) * RADIUS_OF_EARTH;
 	    return d / 1000;
 	}
 	
 	public static String[] neighbours ( String geodna ) {
 	     double[][] bi = GeoDNA.boundingBox( geodna );
 	     double[] lati = bi[0];
 	     double[] loni = bi[1];
 	     double width  = Math.abs( loni[1] - loni[0] );
 	     double height = Math.abs( lati[1] - lati[0] );
 	     String[] neighbours = new String[16];
 
 	     for (int i = -1; i <= 1; i++ ) {
 	         for ( int j = -1; j <= 1; j++ ) {
 	             if ( i!=0 || j!=0 ) {
 	            	 double [] bits = GeoDNA.addVector ( geodna, height * i, width * j );
 	                 neighbours[neighbours.length] = GeoDNA.encode( bits[0], bits[1], geodna.length(), false );
 	             }
 	         }
 	     }
 	     return neighbours;
 	}
 	
 	// This is experimental!!
 	// Totally unoptimised - use at your peril!
 	public static ArrayList<String> neighboursWithinRadius( String geodna, double radius, int precision) {
 
 		 ArrayList<String> neighbours = new ArrayList<String>();
 	     double rh = radius * Math.sqrt(2);
 
 	     String start = GeoDNA.pointFromPointBearingAndDistance( geodna, -( Math.PI / 4 ), rh );
 	     String end = GeoDNA.pointFromPointBearingAndDistance( geodna, Math.PI / 4, rh );
 	     double[][] bbox = GeoDNA.boundingBox( start );
 	     double[] bits = GeoDNA.decode( start );
 	     double slon = bits[1];
 	     bits = GeoDNA.decode( end );
 	     double elon = bits[1];
 	     double dheight = Math.abs( bbox[0][1] - bbox[0][0] );
 	     double dwidth  = Math.abs( bbox[1][1] - bbox[1][0] );
 	     double[] n = GeoDNA.normalise( 0.0, Math.abs( elon - slon ) );
 	     double delta = Math.abs(n[1]);
 	     double tlat = 0.0;
 	     double tlon = 0.0;
 	     String current = start;
 
 	     while ( tlat <= delta ) {
 	         while ( tlon <= delta ) {
 	             double[] cbits = GeoDNA.addVector( current, 0.0, dwidth );
 	             current = GeoDNA.encode( cbits[0], cbits[1], precision );
 	             double d = GeoDNA.distanceInKm( current, geodna );
 	             if ( d <= radius ) {
 	                 neighbours.add(current);
 	             }
 	             tlon = tlon + dwidth;
 	         }
 
 	         tlat = tlat + dheight;
 	         bits = GeoDNA.addVector( start, -tlat , 0.0 );
 	         current = GeoDNA.encode( bits[0], bits[1], precision);
 	         tlon = 0.0;
 	     }
 	     return neighbours;
 	}
 	// This takes an array of GeoDNA codes and reduces it to its
 	// minimal set of codes covering the same area.
 	// Needs a more optimal impl.
 	public static ArrayList<String> reduce( List<String> geodna_codes ) {
 	     // hash all the codes
 	     HashMap<String,Integer> codes = new HashMap<String,Integer>();;
 	     for (int i = 0; i < geodna_codes.size(); i++ ) {
 	         codes.put(geodna_codes.get(i), 1);
 	     }
 
 	     ArrayList<String> reduced = new ArrayList<String>();
 	     String code;
 	     for (int i = 0; i < geodna_codes.size(); i++ ) {
 	         code = geodna_codes.get(i);
 	         if ( codes.get(code) != null ) {
 	             String parent = code.substring( 0, code.length() - 1 );
 
 	             if ( codes.get( parent + 'a' ) ==1
 	               && codes.get( parent + 't' )==1
 	               && codes.get( parent + 'g' )==1
 	               && codes.get( parent + 'c' )==1) {
 	            	 	codes.remove(parent + 'a');
 	            	 	codes.remove(parent + 't');
 	            	 	codes.remove(parent + 'g');
 	            	 	codes.remove(parent + 'c');
 	            	 	reduced.add(parent);
 	            	 	
 	             } else {
 	            	 
 	            	 reduced.add(code);
 	            	 
 	             }
 	         }
 	     }
 	     if ( geodna_codes.size() == reduced.size() ) {
 	         return reduced;
 	     }
 	     return GeoDNA.reduce( reduced );
 	 }
 
 }
 
 /*
 
  
 
 
 };*/
