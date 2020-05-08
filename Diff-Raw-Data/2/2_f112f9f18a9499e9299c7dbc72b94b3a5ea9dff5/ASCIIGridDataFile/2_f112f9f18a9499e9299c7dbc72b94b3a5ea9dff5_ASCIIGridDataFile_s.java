 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gov.usgs.cida.data;
 
 import java.util.Map;
 import org.joda.time.Duration;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import java.util.List;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import org.apache.commons.io.IOUtils;
 import org.joda.time.Instant;
 import org.joda.time.format.DateTimeFormat;
 import static org.joda.time.DateTimeFieldType.*;
 
 /**
  *
  * @author jwalker
  */
 public class ASCIIGridDataFile {
     
     private static final Logger LOG = LoggerFactory.getLogger(ASCIIGridDataFile.class);
     
     private File underlyingFile;
     private Instant startDate = null;
     private int columns = -1;
     private String varName = null;
     private Map<Integer, List<Long>> dateIndices;
     
     public ASCIIGridDataFile(File infile) {
         this.underlyingFile = infile;
         
         // ASSUMING VAR.TIMESTEP.grid
         String filename = infile.getName();
         this.varName = filename.split("\\.")[0].toLowerCase();
         this.dateIndices = Maps.newLinkedHashMap();
     }
     
     /*
      * Sets startDate, timesteps based on assumption that they are in first two lines
      * firstline = "\ttimesteps"
      * secondline = "firstTime\tvalueGRIDid1\t...\tvalueGRIDidn"
      */
     public void inspectFile() throws FileNotFoundException, IOException {
         BufferedReader buf = new BufferedReader(new FileReader(underlyingFile));
         String line = null;
         int year = -1;
         List<Long> indices = Lists.newArrayList();
         try {
             if ((line = buf.readLine()) != null) {
                 columns = Integer.parseInt(line.trim());
             }
             while ((line = buf.readLine()) != null) {
                 String yyyymmdd = line.substring(0, 8);
                 if (startDate == null) {
                     startDate = Instant.parse(yyyymmdd, DateTimeFormat.forPattern("yyyyMMdd"));
                     year = startDate.get(year());
                     dateIndices.put(year, indices);
                     indices.add(0l);
                 }
                 else {
                     Instant thisDate = Instant.parse(yyyymmdd, DateTimeFormat.forPattern("yyyyMMdd"));
                     int thisYear = thisDate.get(year());
                     Duration daysSince = new Duration(startDate, thisDate);
                     if (thisYear == year) {
                         indices.add(daysSince.getStandardDays());
                     }
                     else {
                         year = thisYear;
                         indices = Lists.newArrayList();
                         dateIndices.put(year, indices);
                         indices.add(daysSince.getStandardDays());
                     }
                 }
             }
             
             if (columns == -1 || startDate == null) {
                 LOG.error("File doesn't look like it should, unable to pull date out of file");
                 throw ASCIIGrid2NetCDFConverter.rtex;
             }
         }
         finally {
             IOUtils.closeQuietly(buf);
         }
     }
     
     /**
      * Kind of convoluted way of converting to instant and back to date, but whatever
      * Just want to create something netcdf can do something with
      * @return formatted units for netcdf
      */
     public String getTimeUnits() {
         return "days since " + startDate.get(year()) + "-" +
                 pad(startDate.get(monthOfYear())) + "-" + 
                 pad(startDate.get(dayOfMonth()));
     }
     private String pad(int monthOrDay) {
         if (monthOrDay < 10) {
             return "0" + monthOrDay;
         }
         return "" + monthOrDay;
     }
     
     public Map<Integer, List<Long>> getTimestepIndices() {
         return dateIndices;
     }
     
     public String getVariableName() {
         return varName;
     }
     
     private BufferedReader buffer = null;
     private String[] currentLine = null;
     private int marker = 1;
     private int strideLength = -1;
     
     public void openForReading(int strideLength) throws FileNotFoundException, IOException {
         buffer = new BufferedReader(new FileReader(underlyingFile));
         // First line has number with how many lines follow it
         buffer.readLine();
         this.strideLength = strideLength;
     }
 
     public boolean hasMoreStrides() {
         // marker is at next place to read, has another stride if it ends
         // up after the last index
         return (marker + strideLength <= currentLine.length);
     }
     
     public boolean readNextLine() throws IOException {
         String line = null;
         if ((line = buffer.readLine()) != null) {
             currentLine = line.split("\\s+");
             marker = 1;
             return true;
         }
         return false;
     }
 
     /**
      * Please do not try to run this in any concurrent fashion
      * it will break badly.
      * @return 
      */
     public float[] readTimestepByStride() {
         float[] strideVals = new float[strideLength];
         for (int i=0; i<strideLength; i++) {
             strideVals[i] = Float.parseFloat(currentLine[marker]);
             marker++;
         }
         return strideVals;
     }
     
     public void closeForReading() {
         IOUtils.closeQuietly(buffer);
     }
 }
