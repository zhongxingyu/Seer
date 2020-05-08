 package gov.usgs.cida.data.grib;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
 import java.io.Closeable;
 import java.io.File;
 import java.io.Flushable;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.TransformException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ucar.ma2.*;
 import ucar.nc2.Attribute;
 import ucar.nc2.Dimension;
 import ucar.nc2.NetcdfFileWriteable;
 import ucar.nc2.Variable;
 import ucar.nc2.dataset.CoordinateAxis1DTime;
 import ucar.nc2.dataset.NetcdfDataset;
 import ucar.nc2.dt.GridCoordSystem;
 import ucar.nc2.dt.GridDatatype;
 import ucar.nc2.dt.grid.GridDataset;
 import ucar.nc2.ft.FeatureDataset;
 import ucar.nc2.time.CalendarDate;
 import ucar.nc2.time.CalendarPeriod;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class RollingNetCDFArchive implements Closeable, Flushable {
 
     public static final String DIM = "dimension";
     public static final String VAR = "variable";
     public static final String XY = "xy";
     
     private static final Logger log = LoggerFactory.getLogger(
             RollingNetCDFArchive.class);
     
     private NetcdfFileWriteable netcdf;
     private GridDataset gridDs;
     private CoordinateReferenceSystem crs;
     private GridDatatype gdt;
     private Map<String, List<String>> excludes;
     private String unlimited;
     private String unlimitedUnits;
     private String gridMapping;
     // map this variable to another
     private Map<String, String> gridVariables;
 
     // should be able to open existing file here
     public RollingNetCDFArchive(File rollingFile) throws IOException {
         String fileAsString = rollingFile.getAbsolutePath();
         if (rollingFile.exists() && NetcdfFileWriteable.canOpen(fileAsString)) {
             netcdf = NetcdfFileWriteable.openExisting(fileAsString);
             FeatureDataset fd = GribUtils.getFeatureDatasetFromFile(rollingFile);
             gridDs = GribUtils.getGridDatasetFromFeatureDataset(fd);
             gdt = GribUtils.getDatatypeFromDataset(gridDs);
             crs = GribUtils.getCRSFromDatatype(gdt);
         }
         else {
             netcdf = NetcdfFileWriteable.createNew(fileAsString);
             gridDs = null;
             crs = null;
             gdt = null;
         }
         excludes = Maps.newHashMap();
         gridVariables = null;
         unlimited = "time";
         unlimitedUnits = "hours since 2000-01-01 00:00:00";
         gridMapping = "Latitude_Longitude";
     }
     
     public void setExcludeList(String key, String... excludes) {
         this.excludes.put(key, Lists.newArrayList(excludes));
     }
     
     public void setUnlimitedDimension(String dimName, String units) {
         this.unlimited = dimName;
         this.unlimitedUnits = units;
     }
     
     public void setGridMapping(String gridMappingName) {
         this.gridMapping = gridMappingName;
     }
     
     public void setGridVariables(Map<String, String> variableMapping) {
         gridVariables = variableMapping;
     }
     
     public void define(File gribPrototype) throws IOException, FactoryException, TransformException, InvalidRangeException {
         if (!netcdf.isDefineMode()) {
             throw new IllegalStateException("Cannot call define on an already defined dataset");
         }
         FeatureDataset featureDataset = GribUtils.getFeatureDatasetFromFile(gribPrototype);
         gridDs = GribUtils.getGridDatasetFromFeatureDataset(featureDataset);
         gdt = GribUtils.getDatatypeFromDataset(gridDs);
         crs = GribUtils.getCRSFromDatatype(gdt);
         
         NetcdfDataset srcNc = gridDs.getNetcdfDataset();
         
         Dimension unlimitedDim = null;
         List<String> dimExcludes = excludes.get(DIM);
         for (Dimension dim : srcNc.getDimensions()) {
             if (unlimited.equals(dim.getName())) {
                 unlimitedDim = dim;
             }
             else if (dimExcludes != null && dimExcludes.contains(dim.getName())) {
                 // hold this one out
             }
             else {
                 netcdf.addDimension(dim.getName(), dim.getLength());
             }
         }
         if (unlimitedDim != null) {
             netcdf.addUnlimitedDimension(unlimited);
         }
         
         Variable latVar = null;
         Variable lonVar = null;
         if (excludes.containsKey(XY)) {
             Variable latLonVar = netcdf.addVariable(gridMapping, DataType.INT, "");
             // this whole section will only work if gridMapping == Latitude_Longitude
             netcdf.addVariableAttribute(latLonVar, new Attribute("grid_mapping_name", "latitude_longitude"));
             netcdf.addVariableAttribute(latLonVar, new Attribute("semi_major_axis", 6378137.0));
             netcdf.addVariableAttribute(latLonVar, new Attribute("semi_minor_axis", 6356752.314245));
             netcdf.addVariableAttribute(latLonVar, new Attribute("longitude_of_prime_meridian", 0));
             
             latVar = netcdf.addVariable("lat", DataType.DOUBLE, "y x");
             netcdf.addVariableAttribute(latVar, new Attribute("units", "degrees_north"));
             netcdf.addVariableAttribute(latVar, new Attribute("long_name", "Latitude"));
             netcdf.addVariableAttribute(latVar, new Attribute("standard_name", "latitude"));
             
             lonVar = netcdf.addVariable("lon", DataType.DOUBLE, "y x");
             netcdf.addVariableAttribute(lonVar, new Attribute("units", "degrees_east"));
             netcdf.addVariableAttribute(lonVar, new Attribute("long_name", "Longitude"));
             netcdf.addVariableAttribute(lonVar, new Attribute("standard_name", "longitude"));
         }
         
         List<String> varExcludes = Lists.newArrayList();
         varExcludes.addAll(excludes.get(VAR));
         varExcludes.addAll(excludes.get(XY));
         
         // hack to map bad variable to good one
         boolean addedDataVar = false;
         Variable mappedVariable = null;
         for (Variable var : srcNc.getVariables()) {
             if (varExcludes.contains(var.getFullName())) {
                 // hold this var out
                 if (gridVariables.containsKey(var.getFullName())) {
                     mappedVariable = var;
                 }
             }
             else {
                 Variable newVar = netcdf.addVariable(var.getFullName(), var.getDataType(), var.getDimensionsString());
 
                 for (Attribute varAttr : var.getAttributes()) {
                     netcdf.addVariableAttribute(newVar, varAttr);
                 }
                 // again this will pretty much just work for this case
                 // adding a standard name is not a bad idea // REFACTOR
                 if (unlimited.equals(var.getFullName())) {
                     netcdf.addVariableAttribute(newVar, new Attribute("units", unlimitedUnits));
                 }
                 else {
                     addedDataVar = true;
                     netcdf.addVariableAttribute(newVar, new Attribute("grid_mapping", gridMapping));
                     netcdf.addVariableAttribute(newVar, new Attribute("coordinates", "lon lat"));
                 }
             }
         }
         
         if (!addedDataVar && mappedVariable != null) {
             String target = gridVariables.get(mappedVariable.getFullName());
             Variable newVar = netcdf.addVariable(target, mappedVariable.getDataType(), mappedVariable.getDimensionsString());
             for (Attribute varAttr : mappedVariable.getAttributes()) {
                 netcdf.addVariableAttribute(newVar, varAttr);
             }
             netcdf.addVariableAttribute(newVar, new Attribute("grid_mapping", gridMapping));
             netcdf.addVariableAttribute(newVar, new Attribute("coordinates", "lon lat"));
         }
         
         for (Attribute attr : srcNc.getGlobalAttributes()) {
             netcdf.addGlobalAttribute(attr);
         }
         netcdf.addGlobalAttribute(new Attribute("Conventions", "CF-1.6"));
         
         netcdf.create();
         writeLatsAndLons(latVar, lonVar);
     }
     
     private void writeLatsAndLons(Variable latVar, Variable lonVar) throws FactoryException, TransformException, IOException, InvalidRangeException {
         checkDefined();
         double[] xCoords = GribUtils.getXCoords(gridDs);
         double[] yCoords = GribUtils.getYCoords(gridDs);
         double[][] latLonPairs = GribUtils.transformToLatLonNetCDFStyle(xCoords, yCoords, gdt);
         ArrayDouble.D2 dataLat = new ArrayDouble.D2(yCoords.length, xCoords.length);
         ArrayDouble.D2 dataLon = new ArrayDouble.D2(yCoords.length, xCoords.length);
         int yIndex = 0;
         int xIndex = 0;
         for (int i=0; i<latLonPairs[0].length; i++) {
             double lat = latLonPairs[0][i];
             double lon = latLonPairs[1][i];
             dataLat.set(yIndex, xIndex, lat);
             dataLon.set(yIndex, xIndex, lon);
             if (++xIndex % xCoords.length == 0) {
                 xIndex = 0;
                 yIndex++;
             }
         }
         netcdf.write(latVar.getFullNameEscaped(), dataLat);
         netcdf.write(lonVar.getFullNameEscaped(), dataLon);
     }
     
     
     private void checkDefined() {
         if (netcdf == null || gridDs == null || crs == null || gdt == null) {
             throw new UnsupportedOperationException("Must define prototype before continuing");
         }
     }
 
     public GridDataset getDataset() {
         checkDefined();
         return gridDs;
     }
 
     public CoordinateReferenceSystem getCRS() {
         checkDefined();
         return crs;
     }
     
     // pkg protected now for testing
     double[] getXCoords() {
         checkDefined();
         return GribUtils.getXCoords(gridDs);
     }
     double[] getYCoords() {
         checkDefined();
         return GribUtils.getYCoords(gridDs);
     }
     double[][] transform() {
         checkDefined();
         return GribUtils.transformToLatLonNetCDFStyle(getXCoords(), getYCoords(), gdt);
     }
     
     // still around in case I want to know how to do reprojection with geotools
 //    public double[] transformToLatLon(double[] xCoords, double[] yCoords) throws FactoryException, TransformException {
 //        checkDefined();
 //        double[] transformArray = new double[2 * xCoords.length * yCoords.length];
 //        int i=0;
 //        for (int y=0; y<yCoords.length; y++) {
 //            for (int x=0; x<xCoords.length; x++) {
 //                transformArray[i++] = xCoords[x];
 //                transformArray[i++] = yCoords[y];
 //            }
 //        }
 //        
 //        MathTransform toWGS84Transform = CRS.findMathTransform(
 //                crs,
 //                DefaultGeographicCRS.WGS84,
 //                true);  // must be true if missing bursa-wolf parameters (akak TOWGS84[...])
 //        
 //        toWGS84Transform.transform(transformArray, 0, transformArray, 0, transformArray.length / 2);
 //        return transformArray;
 //    }
 
     public void addFile(File gribOrSomething) throws IOException, InvalidRangeException, Exception {
         // make GridDataset out of it
         checkDefined();
         if (!netcdf.hasUnlimitedDimension()) {
             throw new UnsupportedOperationException("Cannot add to file which is already finished");
         }
         int unlimitedLength = netcdf.getUnlimitedDimension().getLength();
         
         CalendarDate originDate = CalendarDate.parseUdunits(null, "0 " + unlimitedUnits);
         CalendarPeriod periodOfMeasure = CalendarPeriod.of(1, 
             CalendarPeriod.fromUnitString(unlimitedUnits.split(" ")[0]));
         FeatureDataset fd = GribUtils.getFeatureDatasetFromFile(gribOrSomething);
         GridDataset dataset = null;
         try {
             dataset = GribUtils.getGridDatasetFromFeatureDataset(fd);
             for (String varname : gridVariables.keySet()) {
                 GridDatatype grid = dataset.findGridDatatype(varname);
                 if (grid == null) {
                     log.debug("target variable not found, skipping this file");
                     continue;
                 }
                 GridCoordSystem gcs = grid.getCoordinateSystem();
                 int xAxisLength = GridUtility.getXAxisLength(gcs);
                 int yAxisLength = GridUtility.getYAxisLength(gcs);
                 CoordinateAxis1DTime appendingTimeAxis = gcs.getTimeAxis1D();
                 double[] bound1 = appendingTimeAxis.getBound1();
                 double[] bound2 = appendingTimeAxis.getBound2();
                 int[] origins = new int[3];
                int writeIndex = 0;
                for (int readIndex = 0; readIndex<appendingTimeAxis.getSize(); readIndex++) {
                    if (bound1 != null && bound1.length > readIndex &&
                        bound2 != null && bound2.length > readIndex) {
                        double interval = bound2[readIndex] - bound1[readIndex];
                         // here I should allow non-hourly intervals in a more general way
                         if (interval != 1) {
                             continue;
                         }
                     }
                    
                    origins[0] = writeIndex + unlimitedLength;
                    CalendarDate calDate = appendingTimeAxis.getCalendarDate(readIndex);
                    int timeValue = periodOfMeasure.subtract(originDate, calDate);
 
                     ArrayInt.D1 timeArray = new ArrayInt.D1(1);
                     timeArray.set(0, timeValue);
                     
                     ArrayFloat.D3 dataArray = new ArrayFloat.D3(1, yAxisLength, xAxisLength);
                    ArrayFloat.D2 slice = (ArrayFloat.D2)grid.readDataSlice(readIndex, -1, -1, -1);
 
                     for (int y=0; y<yAxisLength; y++) {
                         for (int x=0; x<xAxisLength; x++) {
                             dataArray.set(0, y, x, slice.get(y, x));
                         }
                     }

                     netcdf.write(gridVariables.get(varname), origins, dataArray);
                    netcdf.write(unlimited, new int[]{writeIndex + unlimitedLength}, timeArray);
                    writeIndex++;
                 }
             }
         }
         finally {
             dataset.close();
         }
     }
     
     public void finish() throws IOException {
         /* Do not use this, need to think it out
          * currently changes unlimited dimension all around
         netcdf.setRedefineMode(true);
         netcdf.getUnlimitedDimension().setUnlimited(false);
         netcdf.setRedefineMode(false);
         * 
         */
         close();
     }
     
     @Override
     public void flush() throws IOException {
         netcdf.flush();
     }
     
     @Override
     public void close() throws IOException {
         netcdf.close();
     }
 }
