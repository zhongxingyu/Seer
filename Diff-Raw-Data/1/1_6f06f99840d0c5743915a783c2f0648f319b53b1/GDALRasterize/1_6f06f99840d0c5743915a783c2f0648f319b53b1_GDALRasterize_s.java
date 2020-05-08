 /*
  *  GeoBatch - Open Source geospatial batch processing system
  *  https://github.com/nfms4redd/nfms-geobatch
  *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
  *  http://www.geo-solutions.it
  *
  *  GPLv3 + Classpath exception
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package it.geosolutions.geobatch.unredd.script.util.rasterize;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import it.geosolutions.geobatch.task.TaskExecutor;
 import it.geosolutions.geobatch.task.TaskExecutorConfiguration;
 
 //FreeMarker
 import it.geosolutions.geobatch.actions.freemarker.*;
 import it.geosolutions.geobatch.flow.event.action.ActionException;
 import it.geosolutions.geobatch.unredd.script.model.PostGisConfig;
 import it.geosolutions.geobatch.unredd.script.model.RasterizeConfig;
 import it.geosolutions.geobatch.unredd.script.util.PostGISUtils;
 import it.geosolutions.geobatch.unredd.script.util.SingleFileActionExecutor;
 import it.geosolutions.unredd.geostore.model.UNREDDLayer;
 import it.geosolutions.unredd.geostore.model.UNREDDLayerUpdate;
 import it.geosolutions.unredd.geostore.model.UNREDDLayerUpdate.Attributes;
 import org.apache.commons.io.FilenameUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class GDALRasterize {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(GDALRasterize.class);
     private RasterizeConfig rasterizeConfig;
 
     private File configDir;
     private File tempDir;
 
     public GDALRasterize(
             RasterizeConfig config,
             File configDir, File tempDir) {
 
         super();
 
         this.rasterizeConfig = config;
         this.configDir = configDir;
         this.tempDir = tempDir;
     }
 
     private File freemarker(Map<String, Object> fmRoot) throws ActionException, IllegalAccessException, IOException {
         // FREEMARKER -> GDALRASTERIZE
         // ----------------------- FreeMarker ----------------
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("-------------------- FreeMarker - rasterize ----------------");
         }
 
         // relative to the working dir
         FreeMarkerConfiguration fmc = new FreeMarkerConfiguration("unredd_freemarker", "unredd_freemarker", "unredd_freemarker");
         fmc.setConfigDir(configDir);
         fmc.setRoot(fmRoot);
         // relative to the working dir
         fmc.setInput(rasterizeConfig.getFreeMarkerTemplate());
         fmc.setOutput(tempDir.getAbsolutePath());
 
         // SIMULATE THE EventObject on the queue
 
         final File file = File.createTempFile("fm_", ".xml", tempDir);
 
         FreeMarkerAction fma = new FreeMarkerAction(fmc);
         fma.setTempDir(tempDir);
 
         File outFile = SingleFileActionExecutor.execute(fma, file);
 
         if(outFile == null)
             throw new ActionException(fma, "No output events from freemarker Action");
 
         return outFile;
     }
 
     private File taskExecutor(File inputFile, File errorFile) throws IOException, ActionException {
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("--------------------- TaskExecutor - rasterization ----------------");
         }
 
         final TaskExecutorConfiguration teConfig = new TaskExecutorConfiguration("gdal_rasterize_id", "UNREDD_rasterize", "gdal_rasterize");
 //        final EventObject ev;
 //        ev = (EventObject) queue.peek();
 //        String filename = ev.getSource().toString();
         if(LOGGER.isDebugEnabled())
             LOGGER.debug("Input file: " + inputFile);
         teConfig.setDefaultScript(inputFile.getAbsolutePath());
         teConfig.setErrorFile(errorFile.getAbsolutePath());
         teConfig.setExecutable(rasterizeConfig.getExecutable());
         if(LOGGER.isDebugEnabled())
             LOGGER.debug("gdal_rasterize executable file: " + rasterizeConfig.getExecutable());
         teConfig.setFailIgnored(false);
 //                teConfig.setOutput(getOutput());
 //                teConfig.setOutputName(getOutputName());
 //                teConfig.setServiceID(getServiceID());
         teConfig.setTimeOut(120000l);
 //                teConfig.setVariables(getVariables());
         teConfig.setConfigDir(configDir);
         teConfig.setXsl(rasterizeConfig.getTaskExecutorXslFileName());
 
         TaskExecutor tea = new TaskExecutor(teConfig);
 //        tea.setRunningContext(tempDir.getAbsolutePath());
         tea.setTempDir(tempDir);
 
         File outFile = SingleFileActionExecutor.execute(tea, inputFile);
         // taskexec just returns the input file
 
         if ( errorFile.length() != 0) {
             // error file is NOT empty
             throw new ActionException(tea, "Error in gdal_rasterize; check the error file " + errorFile.getAbsolutePath() + " for more information");
         }
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("Rasterization successfully performed");
         }
 
         return outFile;
     }
 
     public File run(UNREDDLayer layer, UNREDDLayerUpdate layerUpdate, PostGisConfig pgConfig) throws ActionException, IllegalAccessException, IOException {
 
         LOGGER.info("Rasterizing from postgis");
 
         // params to inject into the ROOT datadir for FreeMarker
         final Map<String, Object> fmRoot = buildFreemarkerMap(layer, layerUpdate);
 
         // create the where option
         String year  = layerUpdate.getAttribute(Attributes.YEAR);
         String month = layerUpdate.getAttribute(Attributes.MONTH);
 
         String where = PostGISUtils.YEARATTRIBUTENAME+"="+year;
         if (month!=null)
             where += " and " + PostGISUtils.MONTHATTRIBUTENAME+"="+month;
         fmRoot.put("WHERE", where);
         fmRoot.put("LAYERNAME", pgConfig.getSchema() + "." + layerUpdate.getAttribute(Attributes.LAYER));
         fmRoot.put("SRC", pgConfig.buildOGRString());
 
         // run it
         return run(fmRoot);
     }
 
     /**
      * Rasterize a shapefile.
      * Main rasterization params are read from the UNREDDLayer attribs.
      *
      * @return the raster file
      *
      */
     public File run(UNREDDLayer layer, UNREDDLayerUpdate layerUpdate, File shapefile) throws ActionException, IllegalAccessException, IOException {
 
         LOGGER.info("Rasterizing from shapefile");
 
         // params to inject into the ROOT datadir for FreeMarker
         final Map<String, Object> fmRoot = buildFreemarkerMap(layer, layerUpdate);
 
         // create the where option
 //        String year  = layerUpdate.getAttribute(Attributes.YEAR);
 //        String month = layerUpdate.getAttribute(Attributes.MONTH);
 //
 //        String where = PostGISUtils.YEARATTRIBUTENAME+"="+year;
 //        if (month!=null)
 //            where += " and " + PostGISUtils.MONTHATTRIBUTENAME+"="+month;
 //        fmRoot.put("WHERE", where);
         fmRoot.put("LAYERNAME", FilenameUtils.getBaseName(shapefile.getAbsolutePath()));
         fmRoot.put("SRC", shapefile.getAbsolutePath());
 
         // run it
         return run(fmRoot);
     }
 
     protected File run(Map<String, Object> fmRoot) throws ActionException, IllegalAccessException, IOException {
 
         File tifFile = File.createTempFile("rst_out_", ".tif", tempDir);
         fmRoot.put("OUTPUTFILENAME", tifFile.getAbsolutePath());
 
         LOGGER.info("Creating command line for gdal_rasterize");
         File freemarked = freemarker(fmRoot);
 
         File errorFile  = File.createTempFile("rst_err_", ".xml", tempDir);
 
         LOGGER.info("Performing rasterization into " + tifFile);
         taskExecutor(freemarked, errorFile);
 
         LOGGER.info("Rasterization completed into " + tifFile.getName());
         return tifFile;
     }
 
 
     protected Map<String, Object> buildFreemarkerMap(UNREDDLayer layer, UNREDDLayerUpdate layerUpdate)
     {
 
         Map<String, Object> ret = new HashMap<String, Object>();
 
         ret.put("LAYERNAME", layerUpdate.getAttribute(Attributes.LAYER));
 
 //        ret.put("SRC", options.getSrc());
 //        ret.put("LAYERNAME", options.getLayer());
         ret.put("ATTRIBUTENAME", layer.getAttribute(UNREDDLayer.Attributes.RASTERATTRIBNAME));
 //        ret.put("OUTPUTFILENAME", outputFile.getAbsolutePath());
         ret.put("WIDTH",  layer.getAttribute(UNREDDLayer.Attributes.RASTERPIXELWIDTH));
         ret.put("HEIGHT", layer.getAttribute(UNREDDLayer.Attributes.RASTERPIXELHEIGHT));
 
         ret.put("RX0", layer.getAttribute(UNREDDLayer.Attributes.RASTERX0));
         ret.put("RX1", layer.getAttribute(UNREDDLayer.Attributes.RASTERX1));
         ret.put("RY0", layer.getAttribute(UNREDDLayer.Attributes.RASTERY0));
         ret.put("RY1", layer.getAttribute(UNREDDLayer.Attributes.RASTERY1));
 
         ret.put("NODATA", layer.getAttribute(UNREDDLayer.Attributes.RASTERNODATA));
 
         ret.put("OF", "GTiff");
 
         ret.put("TILED", "TILED=YES");
         ret.put("TILEH", "BLOCKYSIZE="+256); // TODO
         ret.put("TILEW", "BLOCKXSIZE="+256); // TODO
 
         ret.put("ASRS", "EPSG:4326"); // TODO
 //        ret.put("WHERE", options.getWhere());
         
         String dataType = layer.getAttribute(UNREDDLayer.Attributes.RASTERDATATYPE);
         if(dataType == null) {
             dataType = "Int16";
             LOGGER.warn("Datatype not specified for layer " + layer + ". Default " + dataType + " will be used.");
         }
         ret.put("OT", dataType);
 
         return ret;
     }
 
 }
