 package de.fhb.polyencoder;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Stack;
 
 import de.fhb.polyencoder.geo.CoordinateOutOfRangeException;
 import de.fhb.polyencoder.geo.GeographicBounds;
 import de.fhb.polyencoder.geo.GeographicCoordinate;
 import de.fhb.polyencoder.geo.GeographicLocation;
 
 /**
  * Porting of Mark McClures Javascript PolylineEncoder
  * All the mathematical logic is more or less copied from McClure
  *  
  * @author Mark Rambow (markrambow[at]gmail[dot]com)
  * @author Peter Pensold
  * @version 0.5
  */
 public class PolylineEncoder {
   private boolean forceEndpoints;
   private int numLevels;
   private int zoomFactor;
   private double verySmall;
   private double[] zoomLevelBreaks;
 
   private GeographicBounds bounds;
   private ArrayList<GeographicLocation> points;
 
 
 
   /**
    * Creates a PolylinEncoder with the following default values:
    * <ul>
    * <li>numLevels = 18</li>
    * <li>zoomFactor = 2</li>
    * <li>verySmall = 0.00001</li>
    * <li>forceEndpoints = true</li>
    * </ul>
    * 
    * @see #PolylineEncoder(int, int, double, boolean)
    */
   public PolylineEncoder() {
     this(18,2,0.00001,true);
   }
 
 
 
   /**
    * 
    * @param numLevels
    *          indicates how many different levels of magnification the polyline
    *          has
    * @param zoomFactor
    *          the change in magnification between numLevels
    * @param verySmall
    *          indicates the length of a barely visible object at the highest
    *          zoom level
    * @param forceEndpoints
    *          indicates whether or not the endpoints should be visible at all
    *          zoom levels
    */
   public PolylineEncoder(int numLevels, int zoomFactor, double verySmall, boolean forceEndpoints) {
     this.numLevels = numLevels;
     this.zoomFactor = zoomFactor;
     this.verySmall = verySmall;
     this.forceEndpoints = forceEndpoints;
 
     createZoomLevelBreaks();
   }
 
 
 
   private void createZoomLevelBreaks() {
     this.zoomLevelBreaks = new double[numLevels];
 
     for (int i = 0; i < numLevels; i++) {
       this.zoomLevelBreaks[i] = verySmall*Math.pow(this.zoomFactor, numLevels - i - 1);
     }
   }
 
 
 
   /**
    * This computes the appropriate zoom level of a point in terms of it's
    * distance from the relevant segment in the DP algorithm. Could be done in
    * terms of a logarithm, but this approach makes it a bit easier to ensure
    * that the level is not too large.
    */
   public int computeLevel(double absMaxDist) {
     int lev = 0;
   
     if (absMaxDist > this.verySmall) {
       lev = 0;
   
       while (absMaxDist < this.zoomLevelBreaks[lev]) {
         lev++;
       }
     }
   
     return lev;
   }
 
 
 
   /**
    * Ramer-Douglas-Peucker algorithm, adapted for encoding. This algorithm is
    * used to reduce the number of points in a curve.
    * 
    * @return HashMap [encodedPoints; encodedPointsLiteral; encodedLevels]
    * @see <a href="http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm">Ramer-Douglas-Peucker algorithm (Wikipedia)</a>
    * 
    */
   public HashMap<String, String> dpEncode(Track track) {
     ArrayList<GeographicLocation> points = track.getPoints();
     Stack<int[]> stack = new Stack<int[]>();
     String encodedPoints, encodedLevels;
     GeographicLocation segStart, segEnd;
 
     int maxLoc = 0;
     int segStackStart, segStackEnd;
     int[] current;
     double maxDist, absMaxDist = 0.0, temp = 0.0;
     double segmentLength = 0.0;
     double[] dists = new double[points.size()];
 
     if (points.size() > 2) {
       int[] stackVal = new int[] { 0, (points.size() - 1) };
       stack.push(stackVal);
 
       while (stack.size() > 0) {
         current = stack.pop();
         segStackStart = current[0];
         segStackEnd = current[1];
         segStart = points.get(segStackStart);
         segEnd = points.get(segStackEnd);
 
         maxDist = 0;
 
         segmentLength = Math.pow(segEnd.lat() - segStart.lat(), 2) + Math.pow(segEnd.lng() - segStart.lng(), 2);
 
         for (int segLoc = segStackStart + 1; segLoc < segStackEnd; segLoc++) {
           temp = distance(points.get(segLoc), segStart, segEnd, segmentLength);
 
           if (temp > maxDist) {
             maxDist = temp;
             maxLoc = segLoc;
 
             if (maxDist > absMaxDist) {
               absMaxDist = maxDist;
             }
           }
         }
 
         if (maxDist > this.verySmall) {
           dists[maxLoc] = maxDist;
 
           int[] stackValCurMax = { segStackStart, maxLoc };
           int[] stackValMaxCur = { maxLoc, segStackEnd };
 
           stack.push(stackValCurMax);
           stack.push(stackValMaxCur);
         }
       }
     }
 
     encodedPoints = createEncodings(points, dists);
     String encodedPointsLiteral = encodeBackslash(encodedPoints);
     encodedLevels = encodeLevels(points, dists, absMaxDist);
 
     setBounds(points);
     
     HashMap<String, String> hm = new HashMap<String, String>();
     hm.put("encodedPoints", encodedPoints);
     hm.put("encodedPointsLiteral", encodedPointsLiteral);
     hm.put("encodedLevels", encodedLevels);
     return hm;
   }
 
 
 
   /**
    * Now we can use the previous function to march down the list of points and
    * encode the levels. Like createEncodings, we ignore points whose distance
    * (in dists) is undefined.
    */
   public String encodeLevels(ArrayList<GeographicLocation> points, double[] dists, double absMaxDist) {
     String edge;
     StringBuffer encoded_levels = new StringBuffer();
 
     if (this.forceEndpoints) {
       edge = encodeNumber(this.numLevels - 1);
     } else {
       edge = encodeNumber(this.numLevels - computeLevel(absMaxDist) - 1);
     }
 
     encoded_levels.append(edge);
 
     for (int i = 1; i < points.size() - 1; i++) {
       if (dists[i] != 0) {
         encoded_levels.append(encodeNumber(this.numLevels - computeLevel(dists[i]) - 1));
       }
     }
 
     encoded_levels.append(edge);
 
     return encoded_levels.toString();
   }
 
 
 
   public GeographicBounds getBounds() {
     return bounds;
   }
 
 
 
   public void setBounds(GeographicBounds bounds) {
     this.bounds = bounds;
   }
 
 
 
   public void setBounds(ArrayList<GeographicLocation> points) {
     this.bounds = new GeographicBounds(points);
   }
 
 
 
   /**
    * Sets the points and creates the bounds to this points, too.
    * 
    * @param points
    *          points used with this encoder
    */
   public void setPointsAndCreateBounds(ArrayList<GeographicLocation> points) {
     this.points = points;
 
     setBounds(points);
   }
 
 
 
   public ArrayList<GeographicLocation> getPoints() {
     return this.points;
   }
 
 
 
   public static String createEncodings(ArrayList<GeographicLocation> points, double[] dists) {
     StringBuffer encodedPoints = new StringBuffer();
   
     int pLat = 0, pLng = 0;
     int late5, lnge5, dlat, dlng;
   
     for (int i = 0; i < points.size(); i++) {
       if (dists[i] != 0 || i == 0 || i == points.size() - 1) {
         try {
           late5 = GeographicCoordinate.toInt(points.get(i).lat(), GeographicCoordinate.LATITUDE);
           lnge5 = GeographicCoordinate.toInt(points.get(i).lng(), GeographicCoordinate.LONGITUDE);
 
           dlat = late5 - pLat;
           dlng = lnge5 - pLng;
 
           encodedPoints.append(encodeSignedNumber(dlat));
           encodedPoints.append(encodeSignedNumber(dlng));
 
           pLat = late5;
           pLng = lnge5;
         } catch (CoordinateOutOfRangeException e) {
           System.out.println(e.getMessage());
         }
       }
     }
   
     return encodedPoints.toString();
   }
 
 
 
   /**
    * Computes the distance between the point pt and the segment
    * [seqStart,seqEnd]. This could probably be replaced with something that is a
    * bit more numerically stable.
    * 
    * @param pt
    * @param segStart
    * @param segEnd
    * @return
    */
   public static double distance(GeographicLocation pt, GeographicLocation segStart, GeographicLocation segEnd, double segLength) {
     double u, out = 0.0;
     double ptLat = pt.lat();
     double ptLon = pt.lng();
     double segStartLat = segStart.lat();
     double segStartLon = segStart.lng();
     double segEndLat = segEnd.lat();
     double segEndLon = segEnd.lng();
   
     if (segStart.equals(segEnd)) {
       out = Util.sqrtOfSquared((segEndLat - ptLat), (segEndLon - ptLon));
     } else {
       u = ((ptLat - segStartLat)*(segEndLat - segStartLat) + (ptLon - segStartLon)*(segEndLon - segStartLon))/segLength;
   
       if (u <= 0) {
         out = Util.sqrtOfSquared((ptLat - segStartLat), (ptLon - segStartLon));
       }
   
       if (0 < u && u < 1) {
         out = Util.sqrtOfSquared((ptLat - segStartLat - u*(segEndLat - segStartLat)), (ptLon - segStartLon - u*(segEndLon - segStartLon)));
       }
   
       if (1 <= u) {
         out = Util.sqrtOfSquared((ptLat - segEndLat), (ptLon - segEndLon));
       }
     }
   
     return out;
   }
 
 
 
   /**
    * Replaces all backslashes inside a String with two backslashes. It uses a regular
    * expression.
    * 
    * @param s
    *          String that may have double backslashes
    * 
    * @return the String with all double backslashes replaced
    * 
    * @see <a href="http://facstaff.unca.edu/mcmcclur/GoogleMaps/EncodePolyline/pitfalls.html">Potential encoding pitfalls</a>
    */
   public static String encodeBackslash(String s) {
    return s.replaceAll("\\\\", "\\\\\\\\");
   }
 
 
 
   /**
    * Algorithm is explained in <a href=
    * "http://code.google.com/intl/en/apis/maps/documentation/utilities/polylinealgorithm.html"
    * >Google's Polyline Algorithm</a> This function is very similar to Google's,
    * but Mark McClure added some stuff to deal with the double slash issue. Mark
    * McClures version can be found here: <a href=
    * "http://facstaff.unca.edu/mcmcclur/GoogleMaps/EncodePolyline/PolylineEncoder.js"
    * >Mark McClures PolylineEncoder</a>
    * 
    * @param num
    *          number to encode
    * @return the encoded number
    */
   public static String encodeNumber(int num) {
     int unitSeparator = 0x1f;
     int whitespace = 0x20;
   
     StringBuffer encodeString = new StringBuffer();
   
     while (num >= whitespace) {
       int nextValue = (whitespace | (num & unitSeparator)) + 63;
       encodeString.append((char) (nextValue));
       num >>= 5;
     }
   
     num += 63;
     encodeString.append((char) (num));
   
     return encodeString.toString();
   }
 
 
 
   /**
    * This one is Google's verbatim. Code by Mark McClure
    * 
    * @param num
    *          number to encode
    * @return the encoded signed number
    */
   public static String encodeSignedNumber(int num) {
     int signed = num << 1;
   
     if (num < 0) {
       signed = ~(signed);
     }
   
     return (encodeNumber(signed));
   }
 }
