 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.ooici.eoi.netcdf;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.ooici.eoi.datasetagent.AgentUtils;
 import ucar.ma2.Array;
 import ucar.ma2.MAMath;
 import ucar.ma2.Range;
 import ucar.nc2.Attribute;
 import ucar.nc2.Variable;
 import ucar.nc2.constants.AxisType;
 import ucar.nc2.constants.CF;
 import ucar.nc2.constants.FeatureType;
 import ucar.nc2.dataset.CoordinateAxis;
 import ucar.nc2.dataset.CoordinateAxis1DTime;
 import ucar.nc2.dataset.NetcdfDataset;
 
 /**
  *
  * @author cmueller
  */
 public class AttributeFactory {
 
     private AttributeFactory() {
     }
 
     public static void addTimeBoundsMetadata(NetcdfDataset ncds) {
         addTimeBoundsMetadata(ncds, null);
     }
 
     public static void addTimeBoundsMetadata(NetcdfDataset ncds, HashMap<String, Range> subRanges) {
         String startdate = "";
         String enddate = "";
         CoordinateAxis ca = ncds.findCoordinateAxis(AxisType.Time);
         if (ca != null) {
             CoordinateAxis1DTime cat = null;
             if (ca instanceof CoordinateAxis1DTime) {
                 cat = (CoordinateAxis1DTime) ca;
             } else {
                 try {
                     cat = CoordinateAxis1DTime.factory(ncds, new ucar.nc2.dataset.VariableDS(null, ncds.findVariable(ca.getName()), true), null);
                 } catch (IOException ex) {
 //                    warn = true;
 //                    thrown = ex;
                 }
             }
             if (cat != null) {
                 int sti = 0;
                 int eti = (int) cat.getSize() - 1;
                 if (subRanges != null) {
                     Range r = subRanges.get(cat.getName());
                     if (r != null) {
                         sti = r.first();
                         eti = r.last();
                     }
                 }
 
                 startdate = AgentUtils.ISO8601_DATE_FORMAT.format(cat.getTimeDate(sti));
                 enddate = AgentUtils.ISO8601_DATE_FORMAT.format(cat.getTimeDate(eti));
             } else {
 //                warn = true;
             }
         }
 
         ncds.addAttribute(null, new Attribute(IonNcConstants.ION_TIME_COVERAGE_START, startdate));
         ncds.addAttribute(null, new Attribute(IonNcConstants.ION_TIME_COVERAGE_END, enddate));
     }
 
     public static void addLatBoundsMetadata(NetcdfDataset ncds, FeatureType ftype) {
         Number[] minMax = getMetadata(ncds, ftype, AxisType.Lat, "lat", "latitude");
 
         ncds.addAttribute(null, new Attribute(IonNcConstants.ION_GEOSPATIAL_LAT_MIN, minMax[0]));
         ncds.addAttribute(null, new Attribute(IonNcConstants.ION_GEOSPATIAL_LAT_MAX, minMax[1]));
     }
 
     public static void addLonBoundsMetadata(NetcdfDataset ncds, FeatureType ftype) {
         Number[] minMax = getMetadata(ncds, ftype, AxisType.Lon, "lon", "longitude");
 
         double maxcheck = minMax[1].doubleValue();
         if (maxcheck > 180) {
             minMax[1] = maxcheck - 360;
         }
 
         ncds.addAttribute(null, new Attribute(IonNcConstants.ION_GEOSPATIAL_LON_MIN, minMax[0]));
         ncds.addAttribute(null, new Attribute(IonNcConstants.ION_GEOSPATIAL_LON_MAX, minMax[1]));
     }
 
     public static void addVertBoundsMetadata(NetcdfDataset ncds, FeatureType ftype) {
         Number[] minMax = getMetadata(ncds, ftype, AxisType.Height, "z", "depth");
 
         boolean isNan = (Double.isNaN(minMax[0].doubleValue()) | Double.isNaN(minMax[1].floatValue()));
 
         /* Determine positive direction */
         String posDir = "";//default to ''
 //        String posDir = "down";//default to 'down'
         for (Variable v : ncds.getVariables()) {
             Attribute a = v.findAttribute("positive");
             if (a != null) {
                 /* If we don't have values, use the variable (why else have "positive"??) */
                 if (isNan) {
                     try {
                         Array arr = v.read();
                         minMax[0] = MAMath.getMinimum(arr);
                         minMax[1] = MAMath.getMaximum(arr);
                     } catch (IOException ex) {
                         minMax[0] = Double.NaN;
                         minMax[1] = Double.NaN;
                         // exit quietly, return nans
                     }
                 }
                 posDir = a.getStringValue();
                 break;
             }
         }
 
 
 
 
 //        if (isNan) {
 //            /* If we can't sort out the vertical bounds, don't bother with the directionality! */
 //            posDir = "";
 //        }
 
         ncds.addAttribute(null, new Attribute(IonNcConstants.ION_GEOSPATIAL_VERTICAL_MIN, minMax[0]));
         ncds.addAttribute(null, new Attribute(IonNcConstants.ION_GEOSPATIAL_VERTICAL_MAX, minMax[1]));
         ncds.addAttribute(null, new Attribute(IonNcConstants.ION_GEOSPATIAL_VERTICAL_POSITIVE, posDir));
     }
 
     private static Number[] getMetadata(NetcdfDataset ncds, FeatureType ftype, AxisType atype, String vname, String sname) {
         if (null == ftype) {
             ftype = FeatureType.NONE;
         }
 
 
         Number min = Double.NaN;
         Number max = Double.NaN;
         CoordinateAxis ca = null;
         Variable var;
         switch (ftype) {
             case GRID:
                 ca = ncds.findCoordinateAxis(atype);
                 /* NOTE: falls through to next case if 'ca' is null by this point */
                 if (ca != null) {
                     max = ca.getMaxValue();
                     min = ca.getMinValue();
                     break;
                 }
             case ANY_POINT:
             case STATION:
             case PROFILE:
             case STATION_PROFILE:
             case TRAJECTORY:
                 /* Try to find the variable by "typical" variable name */
                 var = ncds.findVariable(vname);
                 if (var == null) {
                     /* Look for it by standard_name */
                     for (Variable v : ncds.getVariables()) {
                         Attribute a = v.findAttribute(CF.STANDARD_NAME);
                         if (a != null && a.getStringValue().equalsIgnoreCase(sname)) {
                             var = v;
                             break;
                         }
                     }
                 }
 
                 /* NOTE: falls through to 'default' if var is still null by this point */
                 if (var != null) {
                     try {
                         Array arr = var.read();
                         min = MAMath.getMinimum(arr);
                         max = MAMath.getMaximum(arr);
                         break;
                     } catch (IOException ex) {
                         /* fall through to default */
                     }
                 }
             default:
                ca = ncds.findCoordinateAxis(atype);
                /* NOTE: if 'ca' is null, "nan" will be returned */
                if (ca != null) {
                    max = ca.getMaxValue();
                    min = ca.getMinValue();
                    break;
                }
         }
         return new Number[]{min, max};
     }
 }
