 /*
     Open Aviation Map
     Copyright (C) 2012 Ákos Maróy
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as
     published by the Free Software Foundation, either version 3 of the
     License, or (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package hu.tyrell.openaviationmap.rendering;
 
import org.geotools.geometry.GeometryBuilder;
 import org.geotools.referencing.CRS;
 import org.geotools.referencing.GeodeticCalculator;
import org.opengis.geometry.PositionFactory;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 /**
  * Enumeration of units of measurements, like meters, nautical miles,
  * millimeters and inches.
  */
 public enum UOM {
     /**
      * Meters.
      */
     M(1.0),
 
     /**
      * Feet.
      */
     FT(0.3048),
 
     /**
      * Millimeters.
      */
     MM(0.001),
 
     /**
      * Inches.
      */
     INCH(0.0254),
 
     /**
      * Nautical miles.
      */
     NM(1852);
 
     /**
      * Return an UOM value based on a string description.
      *
      * @param str the string representation of the unit of measurement.
      * @return the UOM corresponding the string representation.
      */
     public static UOM fromString(String str) {
         String s = str.trim().toLowerCase();
 
         if ("ft".equals(s) || "feet".equals(s)) {
             return FT;
         }
         if ("m".equals(s) || "meter".equals(s) || "meters".equals(s)) {
             return M;
         }
         if ("mm".equals(s)) {
             return MM;
         }
         if ("in".equals(s) || "inch".equals(s)) {
             return INCH;
         }
         if ("nm".equals(s)) {
             return NM;
         }
 
         throw new IllegalArgumentException();
     }
 
     /**
      * Return a value scaled to pixels.
      *
      * Scaling is done based on the provided scaling factor and, if needed,
      * the provided target device DPI value. For units of feet, meters and
      * nautical miles, a real-world size is expected. for units of mm and inch,
      * a target-device size is expected.
      *
      * @param value a string representation of the value to convert. this is of
      *        the form "numeric_value uom", e.g. "2mm" or "-3.5 ft"
      * @param scale the scaling factor to use
      * @param dpi the DPI value of the target device. used for scaling mm or
      *        inch units, which represent a size on the target device
      * @return the converted unit in pixels, which will show the desired size
      *         in the specified scale & DPI
      * @throws RenderException if the unit of measurement is not recognized,
      *         or the numeric value cannot be parsed
      */
     public static double
     scaleValue(String value, double scale, double dpi) throws RenderException {
         String v      = value.trim();
 
         // extract the unit of measurement
         String uomStr = uomPostfix(v);
         if (uomStr == null) {
             throw new RenderException("unrecognized unit of measurement");
         }
         UOM uom  = fromString(uomStr);
 
         // extract the numeric value
         v        = v.substring(0, v.length() - uomStr.length()).trim();
         double d = 0;
         try {
             d = Double.valueOf(v);
         } catch (NumberFormatException e) {
             throw new RenderException(e);
         }
 
         // so far so good, now scale the value
         double dotInMeters = 0.0254d / dpi;
         double result;
 
         switch (uom) {
         case FT:
         case M:
         case NM:
         default:
             if (scale == 0) {
                 throw new RenderException("no scale information provided");
             }
             result = (uom.getInMeters() * d) / (dotInMeters * scale);
             break;
 
         case MM:
         case INCH:
             result = (uom.getInMeters() * d) / dotInMeters;
             break;
         }
 
         return result;
     }
 
     /**
      * Return a value scaled to the specified CRS's scale.
      *
      * Scaling is done based on the provided scaling factor and, if needed,
      * the provided target device DPI value. For units of feet, meters and
      * nautical miles, a real-world size is expected. for units of mm and inch,
      * a target-device size is expected.
      *
      * @param value a string representation of the value to convert. this is of
      *        the form "numeric_value uom", e.g. "2mm" or "-3.5 ft"
      * @param scale the scaling factor to use
      * @param crsRef the CRS reference to use
      * @param refXY the x and y coordinates as a reference point in CRS space
      * @return the converted unit as a measure in the reference CRS
      * @throws RenderException if the unit of measurement is not recognized,
      *         or the numeric value cannot be parsed
      */
     public static double
     scaleValueCrs(String value, double scale, String crsRef, double[] refXY)
                                                     throws RenderException {
         String v      = value.trim();
 
         // extract the unit of measurement
         String uomStr = uomPostfix(v);
         if (uomStr == null) {
             throw new RenderException("unrecognized unit of measurement");
         }
         UOM uom  = fromString(uomStr);
 
         // extract the numeric value
         v        = v.substring(0, v.length() - uomStr.length()).trim();
         double d = 0;
         try {
             d = Double.valueOf(v);
         } catch (NumberFormatException e) {
             throw new RenderException(e);
         }
 
         // so far so good, now scale the value
         double result;
 
         switch (uom) {
         case FT:
         case M:
         case NM:
         default:
             result = distanceInCrs(uom.inMeters * d, refXY, crsRef);
             break;
 
         case MM:
         case INCH:
             result = distanceInCrs(uom.inMeters * d * scale, refXY, crsRef);
             break;
         }
 
         return result;
     }
 
 
     /**
      * Calculate a distance in the specified CRS notation.
      *
      * @param inMeters the distance in meters that need to be calculated in
      *        as a distance in the supplied CRS
      * @param refXY the x and y coordinate of a reference coordinate, which is
      *        used as a location to calculate the distance at
      * @param refCrs the name of the CRS to use for the calculation
      * @return a distance, measured relative to the CRS supplied, which is in
      *         fact the same distance as the supplied inMeters parameter
      */
     private static double
     distanceInCrs(double inMeters, double[] refXY, String refCrs) {
         double dist = 0;
 
         try {
             // calculate the distance in meters of 0.01 * refY in the ref CRS
             double[] sp = {refXY[0], refXY[1]};
             double[] dp = {refXY[0], refXY[1] * 1.01};
 
             CoordinateReferenceSystem crs = CRS.decode(refCrs);
             GeodeticCalculator gc = new GeodeticCalculator(crs);
            GeometryBuilder    gb = new GeometryBuilder(crs);
            PositionFactory    pf = gb.getPositionFactory();
 
            gc.setStartingPosition(pf.createDirectPosition(sp));
            gc.setDestinationPosition(pf.createDirectPosition(dp));
 
             double refY01InMeters = gc.getOrthodromicDistance();
 
             // now, calculate the CRS distance as a proportional of 0.01 * refY
             dist = inMeters * (refXY[1] * 0.01) / refY01InMeters;
 
         } catch (Exception e) {
             System.out.println(e);
         }
 
         return dist;
     }
 
     /**
      * Return the unit of measurement string, which is a postfix for a supplied
      * string value.
      *
      * @param value the value which is checked if it ends with a known unit of
      *        measurement string.
      * @return the end of value, which is a valid unit of measurement, or null.
      */
     public static String uomPostfix(String value) {
         // note: always keep this list in string legth reverse order!
         if (value.endsWith("meters")) {
             return "meters";
         }
         if (value.endsWith("meter")) {
             return "meter";
         }
         if (value.endsWith("feet")) {
             return "feet";
         }
         if (value.endsWith("inch")) {
             return "inch";
         }
         if (value.endsWith("ft")) {
             return "ft";
         }
         if (value.endsWith("mm")) {
             return "mm";
         }
         if (value.endsWith("in")) {
             return "in";
         }
         if (value.endsWith("nm")) {
             return "nm";
         }
         if (value.endsWith("m")) {
             return "m";
         }
 
         return null;
     }
 
     /**
      * The length of this unit of measurement in meters.
      */
     private double inMeters;
 
     /**
      * Constructor.
      *
      * @param inMeters the length of this unit of measurement in meters.
      */
     private UOM(double inMeters) {
         this.inMeters = inMeters;
     }
 
     /**
      * @return the inMeters
      */
     public double getInMeters() {
         return inMeters;
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         switch (this) {
         case FT:
             return "ft";
 
         case M:
             return "m";
 
         case MM:
             return "mm";
 
         case INCH:
             return "in";
 
         case NM:
             return "nm";
 
         default:
             return "";
         }
     }
 }
