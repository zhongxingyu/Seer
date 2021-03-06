 /*
  * Copyright 2008-2010 Microarray Informatics Team, EMBL-European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  *
  * For further details of the Gene Expression Atlas project, including source code,
  * downloads and documentation, please see:
  *
  * http://gxa.github.com/gxa
  */
 package uk.ac.ebi.gxa.netcdf.generator;
 
 import ucar.ma2.*;
 import ucar.nc2.Dimension;
 import ucar.nc2.NetcdfFileWriteable;
 import uk.ac.ebi.gxa.loader.datamatrix.DataMatrixStorage;
 import uk.ac.ebi.gxa.utils.ValueListHashMap;
 import uk.ac.ebi.microarray.atlas.model.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Efficient NetCDF writer tailored to handle chunked expression values blocks found in
  * MAGETAB expression matrixes
  *
  * @author pashky
  */
 public class NetCDFCreator {
 
     private Experiment experiment;
     private ArrayDesign arrayDesign;
 
     private List<Assay> assays;
     private Collection<Sample> samples = new LinkedHashSet<Sample>();
     private ValueListHashMap<Assay, Sample> samplesMap= new ValueListHashMap<Assay, Sample>();
     private Map<String, DataMatrixStorage.ColumnRef> assayDataMap = new HashMap<String, DataMatrixStorage.ColumnRef>();
 
     private List<DataMatrixStorage> storages = new ArrayList<DataMatrixStorage>();
     private ValueListHashMap<DataMatrixStorage,Assay> storageAssaysMap = new ValueListHashMap<DataMatrixStorage, Assay>();
 
     private Iterable<String> mergedDesignElements;
     private Map<String,Integer> mergedDesignElementsMap;
 
     // maps of properties
     private Map<String, List<String>> efvMap;
     private Map<String, List<String>> scvMap;
     private Map<String, Set<String>> uniqueEfvMap;
 
     private NetcdfFileWriteable netCdf;
 
     private int totalDesignElements;
     private int totalUniqueEfvs;
     private int maxDesignElementLength;
     private int maxEfLength;
     private int maxEfvLength;
     private int maxScLength;
     private int maxScvLength;
 
     private String version = "0.0";
 
     public void setVersion(String version) {
         this.version = version;
     }
 
     public void setExperiment(Experiment experiment) {
         this.experiment = experiment;
     }
 
     public void setArrayDesign(ArrayDesign arrayDesign) {
         this.arrayDesign = arrayDesign;
     }
 
     public void setAssays(List<Assay> assays) {
         this.assays = assays;
     }
 
     public void setSample(Assay assay, Sample sample) {
         this.samples.add(sample);
         this.samplesMap.put(assay, sample);
     }
 
     public void setAssayDataMap(Map<String, DataMatrixStorage.ColumnRef> assayDataMap) {
         this.assayDataMap = assayDataMap;
     }
 
     private Map<String, List<String>> extractProperties(Collection<? extends ObjectWithProperties> objects) {
         Map<String, List<String>> propertyMap = new HashMap<String, List<String>>();
 
         // iterate over assays, create keys for the map
         for (ObjectWithProperties assay : objects) {
             for (Property prop : assay.getProperties()) {
                 if (!propertyMap.containsKey(prop.getName())) {
                     List<String> propertyNames = new ArrayList<String>();
                     propertyMap.put(prop.getName(), propertyNames);
                 }
             }
         }
 
         for (ObjectWithProperties assay : objects) {
             for (String propName : propertyMap.keySet()) {
                 StringBuilder propValue = new StringBuilder();
                 for (Property prop : assay.getProperties())
                     if (prop.getName().equals(propName)) {
                         if(propValue.length() > 0)
                             propValue.append(",");
                         propValue.append(prop.getValue());
                     }
                 propertyMap.get(propName).add(propValue.toString());
             }
         }
 
         return propertyMap;
     }
 
     public void prepareData() {
         for(Assay a : assays) {
             DataMatrixStorage buf = assayDataMap.get(a.getAccession()).storage;
             if(!storages.contains(buf))
                 storages.add(buf);
         }
 
         // sort assay in order of buffers and reference numbers in those buffers
         Collections.sort(assays, new Comparator<Assay>() {
             public int compare(Assay o1, Assay o2) {
                 DataMatrixStorage.ColumnRef ref1 = assayDataMap.get(o1.getAccession());
                 DataMatrixStorage buf1 = ref1.storage;
                 int i1 = storages.indexOf(buf1);
 
                 DataMatrixStorage.ColumnRef ref2 = assayDataMap.get(o2.getAccession());
                 DataMatrixStorage buf2 = ref2.storage;
                 int i2 = storages.indexOf(buf2);
 
                 if(i1 != i2)
                     return i1 - i2;
 
                 return ref1.referenceIndex - ref2.referenceIndex;
             }
         });
 
         // build reverse map
         for(Assay a : assays)
             storageAssaysMap.put(assayDataMap.get(a.getAccession()).storage, a);
 
         // reshape available properties to match assays & samples
         efvMap = extractProperties(assays);
         scvMap = extractProperties(samples);
 
         // find maximum lengths for ef/efv/sc/scv strings
         maxEfLength = 0;
         maxEfvLength = 0;
         for(String ef : efvMap.keySet()) {
             maxEfLength = Math.max(maxEfLength, ef.length());
             for(String efv : efvMap.get(ef))
                 maxEfvLength = Math.max(maxEfvLength, efv.length());
         }
 
         maxScLength = 0;
         maxScvLength = 0;
         for(String sc : scvMap.keySet()) {
             maxScLength = Math.max(maxScLength, sc.length());
             for(String scv : scvMap.get(sc))
                 maxScvLength = Math.max(maxScvLength, scv.length());
         }
 
         // unqiue EFV values
         uniqueEfvMap = new HashMap<String, Set<String>>();
         totalUniqueEfvs = 0;
         for(Map.Entry<String,List<String>> ef : efvMap.entrySet()) {
             uniqueEfvMap.put(ef.getKey(), new HashSet<String>(ef.getValue()));
             totalUniqueEfvs += uniqueEfvMap.get(ef.getKey()).size();
         }
 
 
         // merge available design elements (if needed)
         if(storages.size() == 1)
             mergedDesignElements = storages.get(0).getDesignElements();
         else {
             mergedDesignElementsMap = new HashMap<String, Integer>();
             for(DataMatrixStorage buffer : storages)
                 for(String de : buffer.getDesignElements())
                     if(!mergedDesignElementsMap.containsKey(de))
                         mergedDesignElementsMap.put(de, mergedDesignElementsMap.size());
             mergedDesignElements = mergedDesignElementsMap.keySet();
         }
 
         // calculate number of available DEs, genes and maximum accesion string length
         totalDesignElements = 0;
         maxDesignElementLength = 0;
 
         for(String de : mergedDesignElements) {
             maxDesignElementLength = Math.max(maxDesignElementLength, de.length());
             ++totalDesignElements;
         }
     }
 
     private void create() throws IOException {
         // NetCDF doesn't know how to store longs, so we use DataType.DOUBLE for internal DB ids
 
         Dimension assayDimension = netCdf.addDimension("AS", assays.size());
         netCdf.addVariable("AS", DataType.DOUBLE, new Dimension[]{assayDimension});
 
         Dimension sampleDimension = netCdf.addDimension("BS", samples.size());
         netCdf.addVariable("BS", DataType.DOUBLE, new Dimension[]{sampleDimension});
 
         netCdf.addVariable("BS2AS", DataType.INT,
                            new Dimension[]{sampleDimension, assayDimension});
 
         // update the netCDF with the genes count
         Dimension designElementDimension = netCdf.addDimension("DE", totalDesignElements);
         Dimension designElementLenDimension = netCdf.addDimension("DElen", maxDesignElementLength);
 
         netCdf.addVariable("DEacc", DataType.CHAR, new Dimension[]{designElementDimension, designElementLenDimension});
         netCdf.addVariable("DE", DataType.DOUBLE, new Dimension[]{designElementDimension});
         netCdf.addVariable("GN", DataType.DOUBLE, new Dimension[]{designElementDimension});
 
         if(!scvMap.isEmpty()) {
             Dimension scDimension = netCdf.addDimension("SC", scvMap.keySet().size());
             Dimension sclenDimension = netCdf.addDimension("SClen", maxScLength);
 
             netCdf.addVariable("SC", DataType.CHAR, new Dimension[]{scDimension, sclenDimension});
 
             Dimension scvlenDimension = netCdf.addDimension("SCVlen", maxScvLength);
             netCdf.addVariable("SCV", DataType.CHAR, new Dimension[]{scDimension, sampleDimension, scvlenDimension});
         }
         
         if(!efvMap.isEmpty()) {
             Dimension efDimension = netCdf.addDimension("EF", efvMap.size());
             Dimension eflenDimension = netCdf.addDimension("EFlen", maxEfLength);
 
             netCdf.addVariable("EF", DataType.CHAR, new Dimension[]{efDimension, eflenDimension});
 
             Dimension efvlenDimension = netCdf.addDimension("EFVlen", maxEfLength + maxEfvLength + 2);
             netCdf.addVariable("EFV", DataType.CHAR, new Dimension[]{efDimension, assayDimension, efvlenDimension});
 
             Dimension uefvDimension = netCdf.addDimension("uEFV", totalUniqueEfvs);
 
             netCdf.addVariable("uEFV", DataType.CHAR, new Dimension[]{uefvDimension, efvlenDimension});
             netCdf.addVariable("uEFVnum", DataType.INT, new Dimension[]{efDimension});
 
             netCdf.addVariable("PVAL", DataType.FLOAT, new Dimension[]{designElementDimension, uefvDimension});
             netCdf.addVariable("TSTAT", DataType.FLOAT, new Dimension[]{designElementDimension, uefvDimension});
         }
 
         netCdf.addVariable("BDC", DataType.FLOAT, new Dimension[]{designElementDimension, assayDimension});
 
         // add metadata global attributes
         netCdf.addGlobalAttribute(
                 "CreateNetCDF_VERSION",
                 version);
         netCdf.addGlobalAttribute(
                 "experiment_accession",
                 experiment.getAccession());
         netCdf.addGlobalAttribute(
                 "ADaccession",
                 arrayDesign.getAccession());
         netCdf.addGlobalAttribute(
                 "ADid",
                 (double) arrayDesign.getArrayDesignID()); // netcdf doesn't know how to store longs
         netCdf.addGlobalAttribute(
                 "ADname",
                 arrayDesign.getName());
 
         netCdf.create();
     }
 
     private void write() throws IOException, InvalidRangeException {
         writeSamplesAssays();
 
         if(!efvMap.isEmpty())
             writeEfvs();
         if(!scvMap.isEmpty())
             writeScvs();
 
         writeDesignElements();
         writeExpressionValues();
     }
 
     private void writeExpressionValues() throws IOException, InvalidRangeException {
         boolean first = true;
         for(DataMatrixStorage storage : storages) {
             if(storageAssaysMap.get(storage).isEmpty()) // shouldn't happen, but let;'s be sure
                 continue;
 
             if(first) { // skip first
                 int deNum = 0;
                 for(DataMatrixStorage.Block block : storage.getBlocks()) {
                     writeAssayDataBlock(storage, block, deNum, 0, block.size() - 1);
                     deNum += block.size();
                 }
 
                 first = false;
                 continue;
             }
 
             // write other buffers finding continuous (in terms of output sequence) blocks of design elements
             for(DataMatrixStorage.Block block : storage.getBlocks()) {
                 int startSource = -1;
                 int startDestination = -1;
                 int currentSource;
                 for(currentSource = 0; currentSource < block.size(); ++currentSource) {
                     int currentDestination = mergedDesignElementsMap.get(block.designElements[currentSource]);
                     if(startSource == -1) {
                         startSource = currentSource;
                         startDestination = currentDestination;
                     } else if(currentDestination != startDestination + (currentSource - startSource)) {
                         writeAssayDataBlock(storage, block, startDestination, startSource, currentSource - 1);
                         startSource = currentSource;
                         startDestination = currentDestination;
                     }
                 }
                 writeAssayDataBlock(storage, block, startDestination, startSource, currentSource - 1);
             }
         }
     }
 
     private void writeAssayDataBlock(DataMatrixStorage storage, DataMatrixStorage.Block block, int deNum, int deBlockFrom, int deBlockTo)
         throws IOException, InvalidRangeException {
         int width = storage.getWidth();
         ArrayFloat data = (ArrayFloat)Array.factory(Float.class, new int[] { block.designElements.length, width }, block.expressionValues);
 
         int startReference = -1;
         int startDestination = -1;
         int currentDestination = -1;
         int currentReference = -1;
         for(Assay assay : storageAssaysMap.get(storage)) {
             currentReference = assayDataMap.get(assay.getAccession()).referenceIndex;
             if(currentDestination == -1)
                 currentDestination = assays.indexOf(assay);
 
             if(startReference == -1) {
                 startReference = currentReference;
                 startDestination = currentDestination;
             } else if(currentDestination != startDestination + (currentReference - startReference)) {
                 ArrayFloat adata = (ArrayFloat)data.sectionNoReduce(
                         Arrays.asList(
                                 new Range(deBlockFrom, deBlockTo),
                                 new Range(startReference, currentReference - 1)));
                 netCdf.write("BDC", new int[] { deNum, startDestination }, adata);
             }
 
             ++currentDestination;
         }
 
         ArrayFloat adata = (ArrayFloat)data.sectionNoReduce(
                 Arrays.asList(
                         new Range(deBlockFrom, deBlockTo),
                         new Range(startReference, currentReference)));
         netCdf.write("BDC", new int[] { deNum, startDestination }, adata);
 
     }
 
     private void writeDesignElements() throws IOException, InvalidRangeException {
         // store design elements one by one
         int i = 0;
         ArrayChar deName = new ArrayChar.D2(1, maxDesignElementLength);
         ArrayInt deIds = new ArrayInt.D1(totalDesignElements);
         ArrayInt gnIds = new ArrayInt.D1(totalDesignElements);
         for(String de : mergedDesignElements) {
             deName.setString(0, de);
             netCdf.write("DEacc", new int[] { i, 0 }, deName);
             Long deId = arrayDesign.getDesignElements().get(de);
             if(deId != null) {
                 deIds.setLong(i, deId);
                 List<Long> gnId = arrayDesign.getGenes().get(deId);
                 if(gnId != null && !gnId.isEmpty())
                     gnIds.setLong(i, gnId.get(0));
             }
             ++i;
         }
 
         netCdf.write("DE", deIds);
         netCdf.write("GN", gnIds);
     }
 
     private void writeSamplesAssays() throws IOException, InvalidRangeException {
         ArrayInt as = new ArrayInt.D1(assays.size());
         IndexIterator asIt = as.getIndexIterator();
         for (Assay assay : assays) {
             asIt.setLongNext(assay.getAssayID());
         }
         netCdf.write("AS", as);
 
         ArrayInt bs = new ArrayInt.D1(samples.size());
         IndexIterator bsIt = bs.getIndexIterator();
         for (Sample sample : samples) {
             bsIt.setLongNext(sample.getSampleID());
         }
         netCdf.write("BS", bs);
 
         ArrayInt bs2as = new ArrayInt.D2(samples.size(), assays.size());
         IndexIterator bs2asIt = bs2as.getIndexIterator();
 
         // iterate over assays and samples,
         for (Sample sample : samples)
             for (Assay assay : assays)
                 bs2asIt.setIntNext(samplesMap.containsKey(assay) && samplesMap.get(assay).contains(sample) ? 1 : 0);
 
         netCdf.write("BS2AS", bs2as);
     }
 
     private void writeEfvs() throws IOException, InvalidRangeException {
         // write assay property values
         ArrayChar ef = new ArrayChar.D2(efvMap.keySet().size(), maxEfLength);
         ArrayChar efv = new ArrayChar.D3(efvMap.keySet().size(), assays.size(), maxEfvLength);
         ArrayChar uefv = new ArrayChar.D2(totalUniqueEfvs, maxEfLength + maxEfvLength + 2);
         ArrayInt uefvNum = new ArrayInt.D1(efvMap.keySet().size());
 
         int ei = 0;
         int uefvi = 0;
         for(Map.Entry<String, List<String>> e : efvMap.entrySet()) {
             ef.setString(ei, e.getKey());
             int vi = 0;
             for(String v : e.getValue())
                 efv.setString(efv.getIndex().set(ei, vi++), v);
 
             for(String v : uniqueEfvMap.get(e.getKey()))
                 uefv.setString(uefvi++, e.getKey() + "||" + v);
             uefvNum.setInt(ei, uniqueEfvMap.get(e.getKey()).size());
             ++ei;
         }
 
         netCdf.write("EF", ef);
         netCdf.write("EFV", efv);
         netCdf.write("uEFV", uefv);
         netCdf.write("uEFVnum", uefvNum);
     }
 
     private void writeScvs() throws IOException, InvalidRangeException {
         int ei;// sample charactristics
         ArrayChar sc = new ArrayChar.D2(scvMap.keySet().size(), maxScLength);
         ArrayChar scv = new ArrayChar.D3(scvMap.keySet().size(), samples.size(), maxScvLength);
 
         ei = 0;
         for(Map.Entry<String, List<String>> e : scvMap.entrySet()) {
             sc.setString(ei, e.getKey());
             int vi = 0;
             for(String v : e.getValue())
                 scv.setString(scv.getIndex().set(ei, vi++), v);
            ++ei;
         }
 
         netCdf.write("SC", sc);
         netCdf.write("SCV", scv);
     }
 
     public void createNetCdf(File netCdfRepository) throws NetCDFCreatorException {
         prepareData();
 
         try {
             String netcdfName = experiment.getExperimentID() + "_" + arrayDesign.getArrayDesignID() + ".nc";
             File netcdfPath = new File(netCdfRepository, netcdfName);
             if(!netCdfRepository.exists())
                 netCdfRepository.mkdirs();
 
             netCdf = NetcdfFileWriteable.createNew(netcdfPath.getAbsolutePath(), false);
 
             try {
                 create();
                 write();
             } catch(InvalidRangeException e) {
                 throw new NetCDFCreatorException(e);
             } finally {
                 netCdf.close();
             }
         } catch(IOException e) {
             throw new NetCDFCreatorException(e);
         }
     }
 }
