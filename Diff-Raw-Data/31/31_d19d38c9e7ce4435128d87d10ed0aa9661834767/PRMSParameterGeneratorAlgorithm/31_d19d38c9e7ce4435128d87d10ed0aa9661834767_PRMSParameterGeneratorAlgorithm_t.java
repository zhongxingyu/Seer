 package gov.usgs.cida.gdp.wps.algorithm;
 
 import com.google.common.base.Joiner;
 import gov.usgs.cida.gdp.coreprocessing.Delimiter;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatistics;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.Statistics1DWriter.GroupBy;
 import gov.usgs.cida.gdp.coreprocessing.analysis.grid.Statistics1DWriter.Statistic;
 import gov.usgs.cida.gdp.coreprocessing.analysis.statistics.WeightedStatistics1D;
 import gov.usgs.cida.gdp.wps.binding.CSVFileBinding;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import org.apache.commons.io.IOUtils;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.SchemaException;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.n52.wps.algorithm.annotation.Algorithm;
 import org.n52.wps.algorithm.annotation.ComplexDataInput;
 import org.n52.wps.algorithm.annotation.ComplexDataOutput;
 import org.n52.wps.algorithm.annotation.LiteralDataInput;
 import org.n52.wps.algorithm.annotation.Process;
 import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
 import org.n52.wps.server.AbstractAnnotatedAlgorithm;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.operation.TransformException;
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Range;
 import ucar.nc2.dt.GridDatatype;
 
 
 /**
  *
  * @author tkunicki
  */
 @Algorithm(
     version="1.0.0",
     title="PRMS Parameter Generator",
     abstrakt="PRMS Paramer Generator")
 public class PRMSParameterGeneratorAlgorithm extends AbstractAnnotatedAlgorithm {
 
     private FeatureCollection featureCollection;
     private String featureAttributeName;
     
     private List<URI> inputURI;
     private List<String> inputId;
     private List<String> outputId;
     private List<String> outputUnit;
    
     private Date timeStart;
     private Date timeEnd;
     
     private boolean requireFullCoverage = true;
 
     private File output;
 
     @ComplexDataInput(
             identifier=GDPAlgorithmConstants.FEATURE_COLLECTION_IDENTIFIER,
             title=GDPAlgorithmConstants.FEATURE_COLLECTION_TITLE,
             abstrakt=GDPAlgorithmConstants.FEATURE_COLLECTION_ABSTRACT,
             binding=GTVectorDataBinding.class)
     public void setFeatureCollection(FeatureCollection featureCollection) {
         this.featureCollection = featureCollection;
     }
 
     @LiteralDataInput(
             identifier=GDPAlgorithmConstants.FEATURE_ATTRIBUTE_NAME_IDENTIFIER,
             title=GDPAlgorithmConstants.FEATURE_ATTRIBUTE_NAME_TITLE,
             abstrakt=GDPAlgorithmConstants.FEATURE_ATTRIBUTE_NAME_ABSTRACT)
     public void setFeatureAttributeName(String featureAttributeName) {
         this.featureAttributeName = featureAttributeName;
     }
 
     @LiteralDataInput(
             identifier="INPUT_URI",
             maxOccurs= Integer.MAX_VALUE)
     public void setInputURI(List<URI> inputURI) {
         this.inputURI = inputURI;
     }
 
     @LiteralDataInput(
             identifier="INPUT_ID",
             maxOccurs= Integer.MAX_VALUE)
     public void setInputId(List<String> inputId) {
         this.inputId = inputId;
     }
     
     @LiteralDataInput(
             identifier="OUTPUT_ID",
             minOccurs=0,
             maxOccurs= Integer.MAX_VALUE)
     public void setOutputId(List<String> outputId) {
         this.outputId = outputId;
     }
     
     @LiteralDataInput(
             identifier="OUTPUT_UNIT",
             minOccurs=0,
             maxOccurs= Integer.MAX_VALUE)
     public void setOutputUnit(List<String> outputUnit) {
         this.outputUnit = outputUnit;
     }
     
 
     @LiteralDataInput(
             identifier=GDPAlgorithmConstants.REQUIRE_FULL_COVERAGE_IDENTIFIER,
             title=GDPAlgorithmConstants.REQUIRE_FULL_COVERAGE_TITLE,
             abstrakt=GDPAlgorithmConstants.REQUIRE_FULL_COVERAGE_ABSTRACT,
             defaultValue="true")
     public void setRequireFullCoverage(boolean requireFullCoverage) {
         this.requireFullCoverage = requireFullCoverage;
     }
 
     @LiteralDataInput(
             identifier=GDPAlgorithmConstants.TIME_START_IDENTIFIER,
             title=GDPAlgorithmConstants.TIME_START_TITLE,
             abstrakt=GDPAlgorithmConstants.TIME_START_ABSTRACT,
             minOccurs=0)
     public void setTimeStart(Date timeStart) {
         this.timeStart = timeStart;
     }
 
     @LiteralDataInput(
         identifier=GDPAlgorithmConstants.TIME_END_IDENTIFIER,
         title=GDPAlgorithmConstants.TIME_END_TITLE,
         abstrakt=GDPAlgorithmConstants.TIME_END_ABSTRACT,
         minOccurs=0)
     public void setTimeEnd(Date timeEnd) {
         this.timeEnd = timeEnd;
     }
 
     @ComplexDataOutput(
             identifier="OUTPUT",
             title="Output File",
             abstrakt="A zip file containing requested PRMS model input data.",
             binding=CSVFileBinding.class)
     public File getOutput() {
         return output;
     }
 
     @Process
     public void process() {
 
        // validate argument count preconditions
        final int inputURICount = inputURI == null ? 0 : inputURI.size();
        if (inputURICount < 1) {
            throw new IllegalStateException("INPUT_URI count must be greater than 0");
        }
        final int inputIDCount = inputId == null ? 0 : inputId.size();
        if (inputURICount != inputURICount) {
            throw new IllegalStateException("INPUT_ID argument count must be equal to INPUT_URI argument count");
        }
        final int outputIdCount = outputId == null ? 0 : outputId.size();
        if ( ! (outputIdCount == 0 || inputURICount == outputIdCount ) ) {
            throw new IllegalStateException("OUTPUT_ID argument count must either be equal to 0 or INPUT_URI and INPUT_ID argument count");
        }
        final int outputUnitCount = outputUnit == null ? 0 : outputUnit.size();
        if ( ! (outputUnitCount == 0 || inputURICount == outputUnitCount ) ) {
            throw new IllegalStateException("OUTPUT_UNIT argument count must either be equal to 0 or INPUT_URI and INPUT_ID argument count");
         }
         
         List<File> csvFileList = new ArrayList<File>();
         BufferedWriter paramWriter = null;
         try {
             if (featureCollection.getSchema().getDescriptor(featureAttributeName) == null) {
                 addError("Attribute " + featureAttributeName + " not found in feature collection");
                 return;
             }
 
             File prmsParamFile = File.createTempFile(getClass().getSimpleName(), ".param");
             File prmsDataFile = File.createTempFile(getClass().getSimpleName(), ".data");
 
             paramWriter = new BufferedWriter(new FileWriter(prmsParamFile));
 
             
             
            for (int inputURIIndex = 0; inputURIIndex < inputURICount; ++inputURIIndex) {
                 
                URI currentInputURI = inputURI.get(inputURIIndex);
                String currentInputId = inputId.get(inputURIIndex);
                 
                 GridDatatype gridDatatype = GDPAlgorithmUtil.generateGridDataType(
                         currentInputURI,
                         currentInputId,
                         featureCollection.getBounds(),
                         requireFullCoverage);
 
                 Range timeRange = GDPAlgorithmUtil.generateTimeRange(
                         gridDatatype,
                         timeStart,
                         timeEnd);
 
                 // TODO:  all I/O instances need try/finally cleanup
                 File csvTempFile = File.createTempFile(getClass().getSimpleName(), ".temp.csv");
                 BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvTempFile));
                 csvWriter.write("# " + currentInputId);
                 csvWriter.newLine();
                 FeatureCoverageWeightedGridStatistics.execute(
                         featureCollection,
                         featureAttributeName,
                         gridDatatype,
                         timeRange,
                         Arrays.asList(new Statistic[] { Statistic.MEAN } ),
                         csvWriter,
                         GroupBy.STATISTIC,
                         Delimiter.COMMA,
                         requireFullCoverage,
                         false,
                         false);
                 csvWriter.close();
                 csvFileList.add(csvTempFile);
                 
                 // PRMS .params writer
                 BufferedReader csvReader = new BufferedReader(new FileReader(csvTempFile));
                 csv2param(csvReader, paramWriter);
                 csvReader.close();
 
             }
             
             // PRMS .data writer
             BufferedWriter dataWriter = new BufferedWriter(new FileWriter(prmsDataFile));
             List<BufferedReader> csvReaderList = new ArrayList<BufferedReader>(csvFileList.size());
             for (File csvFile : csvFileList) {
                 csvReaderList.add(new BufferedReader(new FileReader(csvFile)));
             }
             csv2data(csvReaderList, dataWriter);
 
             for (BufferedReader csvReader : csvReaderList) {
                 IOUtils.closeQuietly(csvReader);
             }
             for (File csvFile : csvFileList) {
                  csvFile.delete();
             }
 
             // need to zip
             output = prmsDataFile;
 //            output = prmsParamFile;
 
 
         } catch (InvalidRangeException e) {
             addError("Error subsetting gridded data: " + e.getMessage());
         } catch (IOException e) {
             addError("IO Error :" + e.getMessage());
         } catch (FactoryException e) {
             addError("Error initializing CRS factory: " + e.getMessage());
         } catch (TransformException e) {
             addError("Error attempting CRS transform: " + e.getMessage());
         } catch (SchemaException e) {
             addError("Error subsetting gridded data : " + e.getMessage());
         } catch (Exception e) {
             addError("General Error: " + e.getMessage());
         } finally {
 //            if (featureDataset != null) try { featureDataset.close(); } catch (IOException e) { }
             IOUtils.closeQuietly(paramWriter);
         }
     }
 
 
     public static void csv2param(List<File> inList, File out) throws FileNotFoundException, IOException {
         BufferedReader r = null;
         BufferedWriter w = null;
         try {
             w = new BufferedWriter(new FileWriter(out));
             for (File in : inList) {
                 r = new BufferedReader(new FileReader(in));
                 csv2param(r, w);
                 IOUtils.closeQuietly(r);
             }
         } finally {
             IOUtils.closeQuietly(w);
         }
     }
 
     public static void csv2data(List<File> inList, File out) throws FileNotFoundException, IOException {
         List<BufferedReader> rList = new ArrayList<BufferedReader>(inList.size());
         BufferedWriter w = null;
         try {
             for (File in : inList) {
                 rList.add(new BufferedReader(new FileReader(in)));
             }
             w = new BufferedWriter(new FileWriter(out));
             csv2data(rList, w);
         } finally {
             for (BufferedReader r : rList) {
                 IOUtils.closeQuietly(r);
             }
             IOUtils.closeQuietly(w);
         }
     }
 
     private static String[] mon = {
         "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec",
     };
 
     public static void csv2param(BufferedReader csvReader, BufferedWriter paramWriter) throws IOException {
 
         String varName = csvReader.readLine().substring(2).toLowerCase(); // assumes '# '
 
         String[] split = csvReader.readLine().split(",");
         String[] hruLabel = Arrays.copyOfRange(split, 2, split.length);
         
         csvReader.readLine();
 
         int hruCount = hruLabel.length;
 
         WeightedStatistics1D[] mean = new WeightedStatistics1D[hruCount];
         WeightedStatistics1D[][] meanMonthly = new WeightedStatistics1D[12][hruCount];
 
         for (int h = 0; h < hruCount; ++h) {
             mean[h] = new WeightedStatistics1D();
             for (int m = 0; m < 12; ++m) {
                 meanMonthly[m][h] = new WeightedStatistics1D();
             }
         }
 
         DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser();
 
         String csvLine = null;
 
         while ((csvLine = csvReader.readLine()) != null && !csvLine.startsWith("ALL")) {
             split = csvLine.split(",");
             DateTime currentDateTime = formatter.parseDateTime(split[0]).toDateTime(DateTimeZone.UTC);
 
             String[] hruValues = Arrays.copyOfRange(split, 2, split.length);
 
             int m = currentDateTime.getMonthOfYear() - 1;  // returns 1 indexed, we want 0 indexed
             for (int h = 0; h < hruCount; ++h) {
                 double v = Double.parseDouble(hruValues[h]);
                 mean[h].accumulate(v, 1);
                 meanMonthly[m][h].accumulate(v, 1);
             }
         }
 
         List<HRU> hruList = new ArrayList<HRU>(hruCount);
         for (int h = 0; h < hruCount; ++h) {
             hruList.add(new HRU(hruLabel[h], h));
         }
         Collections.sort(hruList, new ComparatorHRU());
 
         paramWriter.write("####"); paramWriter.newLine();
         paramWriter.write("mean_" + varName + " 0"); paramWriter.newLine();
         paramWriter.write("1"); paramWriter.newLine();
         paramWriter.write("nhru"); paramWriter.newLine();
         paramWriter.write(Integer.toString(hruCount)); paramWriter.newLine();
         paramWriter.write("2"); paramWriter.newLine();
         for (int h = 0; h < hruCount; ++h) {
             paramWriter.write(Double.toString(mean[hruList.get(h).inputIndex].getMean())); paramWriter.newLine();
         }
         for (int m = 0; m < 12; ++m) {
             paramWriter.write("####"); paramWriter.newLine();
             paramWriter.write("mean_" + varName + "_" + mon[m] + " 0"); paramWriter.newLine();
             paramWriter.write("1"); paramWriter.newLine();
             paramWriter.write("nhru"); paramWriter.newLine();
             paramWriter.write(Integer.toString(hruCount)); paramWriter.newLine();
             paramWriter.write("2"); paramWriter.newLine();
             for (int h = 0; h < hruCount; ++h) {
                 paramWriter.write(Double.toString(meanMonthly[m][hruList.get(h).inputIndex].getMean())); paramWriter.newLine();
             }
         }
 
     }
 
     public static void csv2data(List<BufferedReader> csvReaderList, BufferedWriter dataWriter) throws IOException {
 
         Joiner joiner = Joiner.on(' ');
 
         dataWriter.write("Created by USGS GeoDataPortal, w00t!"); dataWriter.newLine();
         dataWriter.write("########################################"); dataWriter.newLine();
 
 
         int varCount = csvReaderList.size();
 
         String[] varNameA = new String[varCount];
         String[] split = null;
         String[] hruLabel = null;
         int hruCount = 0;
         for (int v = 0; v < varCount; ++v) {
             varNameA[v] = csvReaderList.get(v).readLine().substring(2).toLowerCase(); // assumes '# '
 
             if (v == 0) {
                 split = csvReaderList.get(v).readLine().split(",");
                 hruLabel = Arrays.copyOfRange(split, 2, split.length);
                 hruCount = hruLabel.length;
             } else {
                 //swallow
                 csvReaderList.get(v).readLine();
             }
             csvReaderList.get(v).readLine();
             
             dataWriter.write(varNameA[v] + " " + hruCount); dataWriter.newLine();
         }
 
         List<HRU> hruList = new ArrayList<HRU>(hruCount);
         for (int h = 0; h < hruCount; ++h) {
             hruList.add(new HRU(hruLabel[h], h));
         }
         Collections.sort(hruList, new ComparatorHRU());
 
         DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser();
 
         String csvLine;
 
         while ((csvLine = csvReaderList.get(0).readLine()) != null && !csvLine.startsWith("ALL")) {
             split = csvLine.split(",");
             DateTime currentDateTime = formatter.parseDateTime(split[0]).toDateTime(DateTimeZone.UTC);
 
             String[] hruValues = Arrays.copyOfRange(split, 2, split.length);
 
             dataWriter.write(Joiner.on(' ').join(
                     currentDateTime.getYear(),
                     currentDateTime.getMonthOfYear(),
                     currentDateTime.getDayOfMonth() - 1,
                     currentDateTime.getHourOfDay(),
                     currentDateTime.getMinuteOfHour(),
                     currentDateTime.getSecondOfMinute()));
 
             for (int h = 0; h < hruCount; ++h) {
                 dataWriter.write(" " + hruValues[hruList.get(h).inputIndex]);
             }
 
             for (int v = 1; v < varCount; ++v) {
                 csvLine = csvReaderList.get(v).readLine();
                 split = csvLine.split(",");
                 hruValues = Arrays.copyOfRange(split, 2, split.length);
                 for (int h = 0; h < hruCount; ++h) {
                     dataWriter.write(" " + hruValues[hruList.get(h).inputIndex]);
                 }
             }
 
             dataWriter.newLine();
         }
 
     }
 
     private static class HRU {
         final String name;
         final int inputIndex;
         final int outputIndex;
         public HRU(String name, int inputIndex) {
             this.name = name;
             this.outputIndex = Integer.parseInt(name);
             this.inputIndex = inputIndex;
         }
     }
 
     private static class ComparatorHRU implements Comparator<HRU> {
 
         @Override
         public int compare(HRU hl, HRU hr) {
             return Double.compare(hl.outputIndex, hr.outputIndex);
         }
     }
 }
 
