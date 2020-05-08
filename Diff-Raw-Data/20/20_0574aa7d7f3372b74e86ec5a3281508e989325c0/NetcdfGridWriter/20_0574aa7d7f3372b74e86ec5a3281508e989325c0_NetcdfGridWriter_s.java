 /*
  * NetcdfGridWriter.java
  *
  * Created on September 19, 2007, 9:44 AM
  *
  * Applied Science Associates, Inc.
  * Copyright 2007.  All rights reserved.
  */
 package com.asascience.edc.nc.io;
 
 import java.beans.PropertyChangeSupport;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import ucar.nc2.units.DateRange;
 import ucar.ma2.Array;
 import ucar.ma2.DataType;
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Range;
 import ucar.nc2.Attribute;
 import ucar.nc2.Dimension;
 import ucar.nc2.FileWriter;
 import ucar.nc2.Variable;
 import ucar.nc2.dataset.CoordinateAxis;
 import ucar.nc2.dataset.CoordinateAxis1DTime;
 import ucar.nc2.dt.GridCoordSystem;
 import ucar.nc2.dt.GridDatatype;
 import ucar.nc2.dt.grid.GridDataset;
 import ucar.unidata.geoloc.LatLonRect;
 
 import com.asascience.edc.ArcType;
 import com.asascience.edc.nc.NetcdfConstraints;
 import ucar.nc2.constants.AxisType;
 
 /**
  * 
  * @author CBM
  */
 public class NetcdfGridWriter {
 
   public final static int UNDEFINED_ERROR = -1;
   public final static int SUCCESSFUL_PROCESS = 0;
   public final static int CANCELLED_PROCESS = 1;
   private List<GridDataset> gdsList;
   private PropertyChangeSupport pcs;
 
   /**
    * Creates a new instance of NetcdfGridWriter
    *
    * @param pcs
    */
   public NetcdfGridWriter(PropertyChangeSupport pcs) {
     this.pcs = pcs;
   }
 
   // TODO: Expand the reporting of errors to provide more in-depth feedback to
   // the user
   public int writeFile(String outLoc, List<GridDataset> gdsL, NetcdfConstraints cons, ArcType type) {
 
     gdsList = gdsL;
     List<String> gridList = cons.getSelVars();
     LatLonRect llbb = cons.getBoundingBox();
     boolean trimByZ = cons.isTrimByZ();
     String trimByName = cons.getTrimByDim();
     int trimByIndex = cons.getTrimByIndex();
 
     // System.err.println("S time="+cons.getStartTime());
     // System.err.println("E time="+cons.getEndTime());
 
     DateRange range = null;
     if ((cons.getStartTime() != null) && (cons.getEndTime() != null)) {
       range = new DateRange(cons.getStartTime(), cons.getEndTime());
     }
     int stride_h = 1, stride_z = 1, stride_time = 1;
 
     try {
       // System.err.println("Start writeFile");
 
       FileWriter writer = new FileWriter(outLoc, false);
 
       // Set up array lists to hold the variables, variable names, and
       // coordinate axis
       List<Variable> varList = new ArrayList<Variable>();
       List<String> varNames = new ArrayList<String>();
       List<Attribute> attsList = new ArrayList<Attribute>();
 
       pcs.firePropertyChange("note", null, "Gathering Attributes...");
       Thread.sleep(250);
 
       String gdsLoc = "";
       // gather all the global attributes from each of the GridDatasets
       for (GridDataset gds : gdsList) {
         for (Attribute a : gds.getGlobalAttributes()) {
           if (!attsList.contains(a)) {
             attsList.add(a);
           }
         }
         gdsLoc += gds.getLocationURI() + " ; ";
       }
 
       // add CF convention attribute
       attsList.add(new Attribute("History",
               "Translated by ASA (NetcdfGridWriter.java) using the Netcdf-Java CDM; " + "Original Dataset = "
               + gdsLoc + "; Translation date = " + new Date() + ";"));
 
       // write the global attributes to the file
       for (Attribute a : attsList) {
         writer.writeGlobalAttribute(a);
       }
 
       // for each of the datasets
       for (GridDataset gds : gdsList) {
         // add the desired grids
         for (String gridName : gridList) {
           if (Thread.currentThread().isInterrupted()) {
             return NetcdfGridWriter.CANCELLED_PROCESS;
           }
          pcs.firePropertyChange("note", null, "Preparing: " + gridName);
           if (varNames.contains(gridName)) {
             continue;
           }
          // System.err.println(gridName);

          // add the name to the list
           varNames.add(gridName);

          // get the grid and it's coordinate system
          GridDatatype grid = gds.findGridDatatype(gridName);

           GridCoordSystem gcsOrg = grid.getCoordinateSystem();
 
           // create a subset if needed
           Range timeRange = null;
           if (range != null & gcsOrg.hasTimeAxis1D()) {
             CoordinateAxis1DTime timeAxis = gcsOrg.getTimeAxis1D();
             int startIndex = timeAxis.findTimeIndexFromDate(range.getStart().getDate());
             int endIndex = timeAxis.findTimeIndexFromDate(range.getEnd().getDate());
 
             // System.err.println("timeUnit: " +
             // timeAxis.getTimeResolution().toString());
             // System.err.println("Time: startDate: " +
             // range.getStart().getDate() +
             // " endDate: " + range.getEnd().getDate() + " sIndex: "
             // + startIndex + " eIndex: " + endIndex);
 
             if (startIndex > -1 & endIndex > -1) {
               timeRange = new Range(startIndex, endIndex);
             }
           }
 
           Range levelRange = null;
           if (trimByZ) {// if trimming by level
             if (trimByIndex == -1) {
               levelRange = null;
             } else {
               levelRange = new Range(trimByIndex, trimByIndex);
             }
           } else {// if trimming by time
             if (trimByIndex != -1) {// false positive possible
               // use only the time indicated by the trimByIndex
               timeRange = new Range(trimByIndex, trimByIndex);
             }
             levelRange = null; // use all levels
           }
 
           if (cons.isUseAllValues()) {// if use all levels == true
             levelRange = null; // use all levels
           }
 
           // List<Dimension> ds = grid.getDimensions();
           // System.err.println("Before subset: ");
           // for(Dimension d : ds){
           // System.err.println(d.getName() + " len:" +
           // d.getLength());
           //
           // }
 
           // System.err.println("in bbLatMin=" + llbb.getLatMin());
           // System.err.println("in bbLatMax=" + llbb.getLatMax());
           // System.err.println("in bbLonMin=" + llbb.getLonMin());
           // System.err.println("in bbLonMax=" + llbb.getLonMax());
 
           if ((llbb != null) || (timeRange != null) || (stride_h >= 1)) {
             pcs.firePropertyChange("note", null, "Subsetting: " + gridName);
             grid = grid.makeSubset(timeRange, levelRange, llbb, 1, stride_h, stride_h);
           }
 
           // LatLonRect outllbb =
           // grid.getCoordinateSystem().getLatLonBoundingBox();
           // System.err.println("out bbLatMin=" +
           // outllbb.getLatMin());
           // System.err.println("out bbLatMax=" +
           // outllbb.getLatMax());
           // System.err.println("out bbLonMin=" +
           // outllbb.getLonMin());
           // System.err.println("out bbLonMax=" +
           // outllbb.getLonMax());
 
           //List<Dimension> ds2 = grid.getDimensions();
           //System.err.println("After subset: ");
           //for(Dimension d : ds2){
           //  System.err.println(d.getName() + " len:" + d.getLength());
           //}
 
           // reduce the variable to 3D then add the variable
           Variable gridV = (Variable) grid.getVariable();
 
           // if(cons.getPanelType() ==
           // com.asascience.edc.gui.SelectionPanelBase.ESRI){//ESRI
           // panel
           if (!cons.isUseAllValues()) {// only if "all levels" is not
             // selected
             pcs.firePropertyChange("note", null, "Trimming: " + gridName);
             // get the trimBy Dimension & Dimension Index
             int trimDimIndex = gridV.findDimensionIndex(trimByName);
             Dimension trimDim = gridV.getDimension(trimDimIndex);
             int len = (trimDim == null) ? -1 : trimDim.getLength();
 
             if (trimDimIndex != -1 & len == 1) {
               // //check the memory
               // System.err.println("Pre-Read:");
               // System.err.println("  Total Memory:" +
               // Utils.Memory.totalMemoryAs(Utils.Memory.MEGABYTE)
               // + " mb");
               // System.err.println("  Free Memory:" +
               // Utils.Memory.freeMemoryAs(Utils.Memory.MEGABYTE)
               // + " mb");
 
               if (Thread.currentThread().isInterrupted()) {
                 return NetcdfGridWriter.CANCELLED_PROCESS;
               }
               pcs.firePropertyChange("note", null, "Trimming: " + gridName + ": reading data");
               // get the data
               Array arrV = gridV.read();
 
               // //check the memory
               // System.err.println("Post-Read:");
               // System.err.println("  Total Memory:" +
               // Utils.Memory.totalMemoryAs(Utils.Memory.MEGABYTE)
               // + " mb");
               // System.err.println("  Free Memory:" +
               // Utils.Memory.freeMemoryAs(Utils.Memory.MEGABYTE)
               // + " mb");
 
               pcs.firePropertyChange("note", null, "Trimming: " + gridName + ": reducing data");
               // reduce the data by trimming the trimDim
               arrV = arrV.reduce(trimDimIndex);
 
               pcs.firePropertyChange("note", null, "Trimming: " + gridName + ": reconstructing data");
               // create the new variable
               Variable newGridV = new Variable(gds.getNetcdfFile(), null, null, gridV.getShortName());
               newGridV.setDataType(gridV.getDataType());
 
               List<Dimension> dims = gridV.getDimensions();
               if (trimDim != null) {
                 dims.remove(trimDim);
               }
               newGridV.setDimensions(dims);
 
               // Carry over all of the attributes, excluding those that may
               // reference an old coordinate system.  This may have changed due
               // to an altitude variable being removed.
               // Also make sure the Time variable is correct.
               for (Attribute a : (List<Attribute>) gridV.getAttributes()) {
                 if ((!a.getName().equalsIgnoreCase("coordinates")) && (!a.getName().equalsIgnoreCase("_CoordinateAxes"))) {
                   newGridV.addAttribute(a);
                 }
               }
               Attribute correctCoords = new Attribute("coordinates", newGridV.getDimensionsString());
               newGridV.addAttribute(correctCoords);
 
               newGridV.setCachedData(arrV, false);
               gridV = new Variable(newGridV);
             }
           }
           // }
 
           /**
            * Temporarily downgrade any doubles to singles for
            * compatibility with Arc
            */
           if (type == ArcType.RASTER) {
             if (gridV.getDataType() == DataType.DOUBLE) {
               gridV.setDataType(DataType.FLOAT);
             }
           }
 
           /**
            * THIS COULD BE WHERE THE FUNKYNESS IS COMING IN... IT'S
            * NOT = WITHOUT THIS THERE ARE NO VALUES FOR X, Y, Z OR
            * TIME
            */
           // add the coordinate axes for each variable if it's not
           // already in the list
           GridCoordSystem gcs = grid.getCoordinateSystem();
           List<CoordinateAxis> axes = gcs.getCoordinateAxes();
           for (int j = 0; j < axes.size(); j++) {
             CoordinateAxis axis = axes.get(j);
             // Don't carry over RunTime dimension!
             if (!axis.getAxisType().equals(AxisType.RunTime)) {
               // make sure the varname and dim name match
               // ONLY if a single dimension...prevents Lat(ny nx) vars
               // from being buggered....
               if (axis.getDimensions().size() == 1) {
                 Dimension dim = axis.getDimension(0);
                 if (dim == null) {
                   dim = axis.getDimension(0);
                 }
                 //System.err.println("Dim name: " + dim.getName());
                 //System.err.println("Axis name: " + axis.getName());
                 if (!dim.getName().equals(axis.getName())) {
                   axis.setName(dim.getName());
                   cons.setTVar(axis.getName());
                 }
               }
 
               if (!varNames.contains(axis.getName())) {
                 varNames.add(axis.getName());
                 varList.add(axis);
               }
             }
           }
 
           // Structure parentStructure = gridV.getParentStructure();
           // if(parentStructure != null){
           // String var =
           // gridV.getName().substring(gridV.getName().lastIndexOf(".")
           // + 1);
           // Variable v = parentStructure.findVariable(var);
           // if(v != null){
           // gridV = v
           // }
           // }
 
           varList.add(gridV);
         }
       }
 
       writer.writeVariables(varList);
 
       pcs.firePropertyChange("note", null, "Writing File...");
       Thread.sleep(250);
       // write the ncfile to disk
       writer.finish();
 
       return NetcdfGridWriter.SUCCESSFUL_PROCESS;
     } catch (IOException ex) {
       ex.printStackTrace();
     } catch (InvalidRangeException ex) {
       ex.printStackTrace();
     } catch (InterruptedException ex) {
       // this catches the "cancel" button
       // System.err.println("interrupted here");
       return NetcdfGridWriter.CANCELLED_PROCESS;
     } catch (Exception ex) {
       ex.printStackTrace();
     }
 
     return NetcdfGridWriter.UNDEFINED_ERROR;
   }
   // <editor-fold defaultstate="collapsed"
   // desc=" Alternate writeFile - not used at this time ">
   /**
    * public boolean writeFile(String outloc, GridDataset gds, List<String>
    * gridList, LatLonRect llbb, String trimByName, int trimByIndex, DateRange
    * range, int stride_h, int stride_z, int stride_time) { try{ //
    * System.err.println("Start writeFile"); FileWriter writer = new
    * FileWriter(outloc, false); NetcdfDataset ncd =
    * (NetcdfDataset)gds.getNetcdfFile(); //add the global attributes.
    * for(Attribute att : gds.getGlobalAttributes()){
    * writer.writeGlobalAttribute(att); } //add CF convention attribute //
    * writer.writeGlobalAttribute(new Attribute("Conventions", "CF-1.0"));
    * writer.writeGlobalAttribute(new Attribute("History",
    * "Translated by Netcdf-Java CDM (NetcdfGridWriter)\n" +
    * "Original Dataset = " + gds.getLocationURI() + "; Translation date = " +
    * new Date())); //Set up array lists to hold the variables, variable names,
    * and coordinate axis List<Variable> varList = new ArrayList<Variable>();
    * List<String> varNames = new ArrayList<String>(); List<CoordinateAxis>
    * axisList = new ArrayList<CoordinateAxis>(); // // for(String gridName :
    * gridList){ // System.err.println(gridName); // } //add the desired grids
    * for(String gridName : gridList){ if(varNames.contains(gridName)){
    * continue; } //add the name to the list varNames.add(gridName); //get the
    * grid and it's coordinate system GridDatatype grid =
    * gds.findGridDatatype(gridName); GridCoordSystem gcsOrg =
    * grid.getCoordinateSystem(); //create a subset if needed Range timeRange =
    * null; if(range != null){ CoordinateAxis1DTime timeAxis =
    * gcsOrg.getTimeAxis1D(); int startIndex =
    * timeAxis.findTimeIndexFromDate(range.getStart().getDate()); int endIndex
    * = timeAxis.findTimeIndexFromDate(range.getEnd().getDate()); timeRange =
    * new Range(startIndex, endIndex); } Range levelRange; if(trimByIndex ==
    * -1){ levelRange = null; }else{ levelRange = new Range(trimByIndex,
    * trimByIndex); } // System.err.println(llbb.getLatMin()); //
    * System.err.println(llbb.getLatMax()); //
    * System.err.println(llbb.getLonMin()); //
    * System.err.println(llbb.getLonMax()); if((llbb != null) || (timeRange !=
    * null) || (stride_h >= 1)){ grid = grid.makeSubset(timeRange, levelRange,
    * llbb, 1, stride_h, stride_h); } //reduce the variable to 3D then add the
    * variable Variable gridV = (Variable)grid.getVariable(); // Variable
    * sliceV = gridV.slice(gridV.findDimensionIndex(trimByName), trimByIndex);
    * //get and reduce the data Array arrV =
    * gridV.read().reduce(gridV.findDimensionIndex(trimByName)); //create the
    * new variable Variable newGridV = new Variable(gds.getNetcdfFile(), null,
    * null, gridV.getShortName()); newGridV.setDataType(gridV.getDataType());
    * for(Attribute a : (List<Attribute>)gridV.getAttributes()){
    * newGridV.addAttribute(a); } Dimension levelD =
    * gridV.getDimension(gridV.findDimensionIndex(trimByName)); List<Dimension>
    * dims = gridV.getDimensions(); //
    * System.err.println("Before Dims: "+dims.size()); //
    * System.err.println("Before gridDims: "+gridV.getDimensions().size());
    * if(levelD != null){ dims.remove(levelD); } newGridV.setDimensions(dims);
    * newGridV.setCachedData(arrV, false); // else{ //
    * System.err.println("levelD = null"); // } //// for(int i = dims.size() -
    * 1; i >= 0; i--){ //// System.err.println(dims.get(i).getName()); ////
    * if(dims.get(i).getName().equals(trimByName)){ //// dims.remove(i); ////
    * System.err.println("**REMOVE**"); //// } //// } //
    * System.err.println("After Dims: "+dims.size()); //
    * gridV.setDimensions(dims); //
    * System.err.println("After gridDims: "+gridV.getDimensions().size()); //
    * Group grp = new Group(ncd, null, "temp"); // grp.addVariable(gridV); //
    * grp.removeDimension(trimByName); // Variable outV =
    * grp.findVariable(gridV.getName()); //
    * System.err.println("testVarSize:"+outV.getDimensions().size()); // int i
    * = gridV.findDimensionIndex(trimByName); //
    * System.err.println("levelDimIndex:"+i); // gridV.setDimension(i, null);
    * // System.err.println("dim set = null"); // Group root =
    * writer.getNetcdf().getRootGroup(); // root.removeDimension(trimByName);
    * // Array vArr = gridV.read().reduce(); // List<Dimension> dims =
    * gridV.getDimensions(); // varList.add(gridV); varList.add(newGridV); //
    * varList.add(sliceV); THIS COULD BE WHERE THE FUNKYNESS IS COMING IN...
    * IT'S NOT = WITHOUT THIS THERE ARE NO VALUES FOR X, Y, Z OR TIME //add the
    * coordinate axes for each variable GridCoordSystem gcs =
    * grid.getCoordinateSystem(); List<CoordinateAxis> axes =
    * gcs.getCoordinateAxes(); for(int j = 0; j < axes.size(); j++){
    * CoordinateAxis axis = axes.get(j);
    * if(!varNames.contains(axis.getName())){ //
    * if(!axis.getName().equals(trimByName)){ varNames.add(axis.getName());
    * varList.add(axis); // } // axisList.add(axis); } } } // // THIS WOULD
    * WORK EXCEPT THAT THE GROUP NAME IS AUTOMATICALLY APPENDED SOMEWHERE IN
    * THE WRITE PROCESS... // // //replace any "." in the variable names with
    * "_" // for(Variable v : varList){ // v.setName(v.getName().replace(".",
    * "_")); // } // //add supplemental variables (i.e. Speed & Direction) //
    * if(hasSupplemental) // for(Variable v : suppVars) // varList.add(v);
    * writer.writeVariables(varList); // Group root =
    * writer.getNetcdf().getRootGroup(); // if(root == null)
    * System.err.println("root null"); // for(Dimension d :
    * root.getDimensions()){ // System.err.println(d.getName()); // } //
    * System.err.println("trimByName:"+trimByName); //
    * root.removeDimension(trimByName);//this errors writer.finish(); //
    * boolean fin = root.removeDimension(trimByName);//this runs but doesn't
    * get rid of the dimension! // System.err.println(fin); //
    * System.err.println("End writeFile - ok"); return true; }catch(IOException
    * ex){ ex.printStackTrace(); }catch(InvalidRangeException ex){
    * ex.printStackTrace(); }catch(Exception ex){ ex.printStackTrace(); } //
    * System.err.println("End writeFile - error"); return false; //stop here...
    * //<editor-fold defaultstate="collapsed" desc=" unused from orig "> // //
    * now add CF annotations as needed - dont change original ncd or gds //
    * NetcdfFileWriteable ncfile = writer.getNetcdf(); // Group root =
    * ncfile.getRootGroup(); // for (String gridName : gridList) { //
    * GridDatatype grid = gds.findGridDatatype(gridName); // Variable newV =
    * root.findVariable(gridName); // // // annotate Variable for CF //
    * StringBuffer sbuff = new StringBuffer(); // GridCoordSystem gcs =
    * grid.getCoordinateSystem(); // List axes = gcs.getCoordinateAxes(); //
    * for (int j = 0; j < axes.size(); j++) { // Variable axis = (Variable)
    * axes.get(j); // sbuff.append(axis.getName() + " "); // } //// if
    * (addLatLon) //// sbuff.append("lat lon"); // newV.addAttribute(new
    * Attribute("coordinates", sbuff.toString())); // // // looking for
    * coordinate transform variables // List ctList =
    * gcs.getCoordinateTransforms(); // for (int j = 0; j < ctList.size(); j++)
    * { // CoordinateTransform ct = (CoordinateTransform) ctList.get(j); //
    * Variable v = ncd.findVariable(ct.getName()); // if (ct.getTransformType()
    * == TransformType.Projection) // newV.addAttribute(new
    * Attribute("grid_mapping", v.getName())); // } // } // // for
    * (CoordinateAxis axis : axisList) { // Variable newV =
    * root.findVariable(axis.getName()); // if ((axis.getAxisType() ==
    * AxisType.Height) || (axis.getAxisType() == AxisType.Pressure) ||
    * (axis.getAxisType() == AxisType.GeoZ)) { // if (null !=
    * axis.getPositive()) // newV.addAttribute(new Attribute("positive",
    * axis.getPositive())); // } // if (axis.getAxisType() == AxisType.Lat) {
    * // newV.addAttribute(new Attribute("units", "degrees_north")); //
    * newV.addAttribute(new Attribute("standard_name", "latitude")); // } // if
    * (axis.getAxisType() == AxisType.Lon) { // newV.addAttribute(new
    * Attribute("units", "degrees_east")); // newV.addAttribute(new
    * Attribute("standard_name", "longitude")); // } // // //
    * newV.addAttribute(new Attribute(_Coordinate.AxisType,
    * axis.getAxisType().toString())); // cheating // } //</editor-fold> }
    */
   // </editor-fold>
 }
