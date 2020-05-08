 /*
  *  Copyright (C) 2007 - 2012 GeoSolutions S.A.S.
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
 package it.geosolutions.unredd.stats.impl;
 
 import it.geosolutions.unredd.stats.model.config.ClassificationLayer;
 import it.geosolutions.unredd.stats.model.config.StatisticConfiguration;
 import it.geosolutions.unredd.stats.model.config.StatsType;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.commons.collections.keyvalue.MultiKey;
 import org.apache.log4j.Logger;
 import org.jaitools.imageutils.ROIGeometry;
 import org.jaitools.media.jai.classifiedstats.Result;
 import org.jaitools.numeric.Statistic;
 
 /**
  * This Class provide an entry point for handle JAI-Tools ClassifiedStatistics given a StatisticConfiguration instance
  *  
  * @author ETj (etj at geo-solutions.it)
  * @author DamianoG
  */
 public class StatsRunner {
     
     private final static Logger LOGGER = Logger.getLogger(StatsRunner.class);
     
     /**
      * Hold all informations about Statistics to calculate
      */
     private final StatisticConfiguration cfg;
     
     /**
     * Region Of Interest on wich calculate the stats
      */
     private final ROIGeometry roiGeom;
     
     /**
      * The Object responsible to hold the type and the order of the statistics
      */
     private Map<Statistic, Integer> statsIndexes = new EnumMap<Statistic, Integer>(Statistic.class);
     private Integer countIndex = null;
     
     private FileWriter outputWriter = null;
 
     /**
      * 
      * @param cfg a StatisticConfiguration object that hold all informations about Stats to calculate.
      */
     public StatsRunner(StatisticConfiguration cfg) {
         this.cfg = cfg;
         this.roiGeom = null;
     }
     
     /**
      * 
      * @param cfg a StatisticConfiguration object that hold all informations about Stats to calculate.
      * @param roiGeom the Region Of Interest on wich calculate the stats
      */
     public StatsRunner(StatisticConfiguration cfg, ROIGeometry roiGeom) {
         this.cfg = cfg;
         this.roiGeom = roiGeom;
     }
     
     /**
      * This method is the main entry point for Statistics Calculation. 
      * Its purpose is to process the provided StatisticConfiguration object and instantiate and run a RasterClassifiedStatistics object 
      * that is "StatisticConfiguration unaware". The results provided by RasterClassifiedStatistics object is stored in an output file.
      * 
      * @throws IOException
      */
     public void run() throws IOException {
 
         LOGGER.info("Preparing stats...");
         boolean deferredMode = cfg.isDeferredMode();
 
         DataFile data = new DataFile();
         File dataF = new File(cfg.getDataLayer().getFile());
         if( ! dataF.exists() )
             throw new FileNotFoundException("Data layer file not found: " + dataF.getAbsolutePath());
         else if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("Stats data file: " + dataF.getAbsolutePath());
         }
         data.setFile(dataF);
         data.setNoValue(cfg.getDataLayer().getNodata()); // may be null
 
         int cnt = -1;
         int pivotIndex = -1;
 
         List<DataFile> cls = new ArrayList<DataFile>(cfg.getClassifications().size());
         for (ClassificationLayer cl : cfg.getClassifications()) {
             cnt++;
             if(cl.isPivotDefined())
                 pivotIndex = cnt;
             DataFile df = new DataFile();
             File file = new File(cl.getFile()); // we're assuming they're all files
             if( ! file.exists() )
                 throw new FileNotFoundException("Classification layer file not found: " + file.getAbsolutePath());
             else if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Stats classification file: " + file.getAbsolutePath());
             }
             df.setFile(file);
             df.setNoValue(cl.getNodata());
             cls.add(df);
             LOGGER.info("Added classification layer " + file.getName() + " order " + cnt + " pivot:"+cl.isPivotDefined());
         }
 
         List<Statistic> stats = new ArrayList<Statistic>();
         for (int i = 0; i < cfg.getStats().size(); i++) {
             StatsType statsType = cfg.getStats().get(i);
             switch(statsType) {
                 case COUNT:
                     countIndex = i;
                     break;
                 case MIN:
                     stats.add(Statistic.MIN);
                     statsIndexes.put(Statistic.MIN, i);
                     break;
                 case MAX:
                     stats.add(Statistic.MAX);
                     statsIndexes.put(Statistic.MAX, i);
                     break;
                 case SUM:
                     stats.add(Statistic.SUM);
                     statsIndexes.put(Statistic.SUM, i);
                     break;
                 default:
                     throw new IllegalStateException("Unknown statstype " + statsType);
             }
         }
         
         LOGGER.info("Running stats...");
         RasterClassifiedStatistics rcs = new RasterClassifiedStatistics();
         rcs.setRoiGeom(roiGeom);
         Map<MultiKey,List<Result>> results = rcs.execute(deferredMode, data, cls, stats);
 
         LOGGER.info("Outputting stats results...");
 
         Map<Integer, List<Double>> pivot = new TreeMap<Integer, List<Double>>();
 
         if(pivotIndex == -1 || pivotIndex == 0) { // no pivotting
             outputStats(results);
         } else {
             LOGGER.info("Pivoting...");
             pivot(results, pivotIndex, cfg.getClassifications().get(pivotIndex).getPivot());
         }
 
     }
 
     protected void pivot(Map<MultiKey,List<Result>> results, int index, List<Double>pivotClasses) {
 
         // This map contains all results
         //   - MultiKeys here are built by all the Keys in the original MultiKey except the pivotted one
         //   - Key of the inner map is the class of the pivotted classification
         //   - Value of the inner map is the result of the single stat requested
         Map<MultiKey, Map<Double, Double>> pivotted = new HashMap<MultiKey, Map<Double, Double>>();
 
         // Regroup results by subkeys
         for (MultiKey classes : results.keySet()) {
             List<Result> result = results.get(classes);
             if(result.size() != 1)
                 throw new IllegalStateException();
             Double value = result.get(0).getValue(); // FIXME: this won't work with COUNT statistic
 
             // Create subKey (all keys except the pivot one)
             int newkeys = classes.getKeys().length-1;
             Object subkeys[] = new Object[newkeys];
             for (int i = 0; i < newkeys; i++) {
                 subkeys[i] = classes.getKeys()[i + (i>=index? 1:0)];
             }
             MultiKey subKey = new MultiKey(subkeys);
             Double pivotKey = ((Number)classes.getKeys()[index]).doubleValue();
 
             Map<Double, Double> submap;
             if(pivotted.containsKey(subKey)) {
                 submap = pivotted.get(subKey);
             } else {
                 submap = new HashMap<Double, Double>();
                 pivotted.put(subKey, submap);
             }
 
             submap.put(pivotKey, value);
         }
 
         LOGGER.info("Pivotted " + pivotted.size() + " subkeys");
         
         // build the output string
         for (MultiKey subKey : pivotted.keySet()) {
             StringBuilder sb = new StringBuilder();
 
             // append classifications' classes
             for (Object object : subKey.getKeys()) {
                 sb.append(object.toString()).append(cfg.getOutput().getSeparator());
             }
             // append pivotted data
             Map<Double, Double> pvalues = pivotted.get(subKey);
             // export data in the requested order
             for (Iterator<Double> it = pivotClasses.iterator(); it.hasNext();) {
                 Double pRequestedClass = it.next();
                 Double pval = pvalues.get(pRequestedClass);
                 if(pval != null)
                     sb.append(pval);
                 else
                     sb.append(cfg.getOutput().getMissingValue());
                 if(it.hasNext())
                     sb.append(cfg.getOutput().getSeparator());
             }
 
             output(sb);
         }
 
         closeOutputFile();
     }
 
     protected void outputStats(Map<MultiKey, List<Result>> results) {
 
         boolean rangeAlreadyLogged = false;
 
         for (MultiKey classes : results.keySet()) {
 
             StringBuilder sb = new StringBuilder();
 
             //= append all classes
             for (Object o : classes.getKeys()) {
 //                LOGGER.info("Adding class " + o);
                 sb.append(o).append(cfg.getOutput().getSeparator());
             }
 
             //= append stats in the requested order
             List<Result> resultList = results.get(classes);
             int statsnum = statsIndexes.size() + ( countIndex != null? 1 : 0);
 //            LOGGER.info(statsnum + " stats requested");
             // prefill outval
             List<Double> outval = new ArrayList<Double>(statsnum);
             for (int i = 0; i < statsnum; i++) {
                 outval.add(Double.NaN);
             }
 
             // fill count stat
             if(countIndex != null) { // COUNT is not a real stat: set it by hand
                 Long l = resultList.get(0).getNumAccepted();
                 outval.set(countIndex, l.doubleValue());
             }
 
             // fill computed stats
             for (Result result : resultList) {
 //                LOGGER.info(result);
                 Integer idx = statsIndexes.get(result.getStatistic());
                 if(idx == null) {
                     if(LOGGER.isDebugEnabled()) {
                         if(result.getStatistic() != Statistic.RANGE || ! rangeAlreadyLogged) { // display range warning only once
                             LOGGER.debug("Encountered stat not requested ("+result.getStatistic().name()+"): " + result);
                         }
                         rangeAlreadyLogged = result.getStatistic() == Statistic.RANGE;
                     } // log
                     continue;
                 }
 //                 else
 //                    LOGGER.info("Adding " + result.getStatistic());
                 outval.set(idx, result.getValue());
             }
 
             //= put stats in output line
             for (Iterator<Double> it = outval.iterator(); it.hasNext();) {
                 Double val = it.next();
                 if(! Double.isNaN(val))
                     sb.append(val);
                 else
                     sb.append(cfg.getOutput().getNanValue());
 
                 if(it.hasNext())
                     sb.append(cfg.getOutput().getSeparator());
             }
 
             output(sb);
         }
 
         closeOutputFile();
     }
 
     public void output(StringBuilder sb) {
         if(cfg.getOutput().getFile() != null) {
             if(this.outputWriter == null) {
                 createOutputFile();
             }
             try {
                 outputWriter.append(sb).append('\n');
             } catch (IOException ex) {
                     LOGGER.error("Error writing to output file : " + ex.getMessage());
                     throw new RuntimeException(ex);
             }
         } else { // no output file choosen
             System.out.println(sb);
         }
     }
 
     protected void createOutputFile() {
         if(outputWriter != null)
             throw new IllegalStateException("Output file already exists");
 
         File outfile = new File(cfg.getOutput().getFile());
         try {
             LOGGER.info("Creating output file " + outfile);
             outputWriter = new FileWriter(outfile);
         } catch (IOException ex) {
             LOGGER.error("Error creating output file: " + ex.getMessage());
             throw new RuntimeException(ex);
         }
     }
 
     protected void closeOutputFile() {
         if(outputWriter!=null) {
             try {
                 outputWriter.flush();
                 outputWriter.close();
             } catch (IOException ex) {
                 LOGGER.warn("Error in closing writer", ex);
             }
         }
     }
 
 }
