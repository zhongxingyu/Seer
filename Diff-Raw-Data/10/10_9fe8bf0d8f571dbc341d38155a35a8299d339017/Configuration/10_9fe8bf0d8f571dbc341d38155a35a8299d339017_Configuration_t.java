 package ru.spbau.bioinf.tagfinder;
 
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 
 public class Configuration {
 
     private File proteinDatabase;
 
     private File inputDir;
     private File resultDir;
     private File xmlDir;
     private File xmlSpectrumsDir;
     private File xmlProteinsDir;
 
     private File datasetDir;
 
     private File inputData;
     private File xmlScansDir;
 
     public Configuration(String args[]) {
         String dataset = "data/salmonella";
         if (args != null) {
             if (args.length > 0) {
                 dataset = args[0];
             }
         }
         datasetDir = new File(dataset);
         inputDir = createDir("input");
         File[] proteinDatabases = inputDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith(".fasta");
             }
         });
         if (proteinDatabases.length == 1) {
             proteinDatabase = proteinDatabases[0];
         } else {
             proteinDatabase = new File(inputDir, args[1]);
         }
 
 
         resultDir = createDir("result");
         xmlDir = createDir("xml");
         xmlSpectrumsDir = createDir(xmlDir, "spectrums");
         xmlProteinsDir = createDir(xmlDir, "proteins");
 
         xmlScansDir = new File(xmlDir, "scans");
 
         createDir("html");
     }
 
 
    public File getInputDir() {
        return inputDir;
    }

     private Map<Integer, Integer> spectrums = new HashMap<Integer, Integer>();
 
     private double ppmCoef = 5.0d / 1000000d;
 
     public double getPpmCoef() {
         return ppmCoef;
     }
 
     public double[] getEdgeLimits(Peak peak, Peak next) {
         double diff = next.diff(peak);
         double[] limits = new double[2];
         double error =  (next.getMass() + peak.getMass()) * getPpmCoef() / 2;
         limits[0] = diff - error;
         limits[1] = diff + error;
         return limits;
     }
 
 
     private File createDir(String name) {
         File dir = new File(datasetDir, name);
         dir.mkdirs();
         return dir;
     }
 
     private File createDir(File parent, String name) {
         File dir = new File(parent, name);
         dir.mkdirs();
         return dir;
     }
 
     public File getSpectrumsFile() {
         return inputData;
     }
 
     public File getProteinDatabaseFile() {
         return proteinDatabase;
     }
 
     public List<Protein> getProteins() throws Exception {
         ProteinDatabaseReader databaseReader = new ProteinDatabaseReader(getProteinDatabaseFile());
         return databaseReader.getProteins();
     }
 
     public Map<Integer, Integer> getMSAlignResults() throws IOException {
         BufferedReader input = ReaderUtil.createInputReader(new File(inputDir, "nodigestion_result_list.txt"));
         Map<Integer, Integer> ans = new HashMap<Integer, Integer>();
         String s;
         while ((s = input.readLine()) != null) {
             String[] data = ReaderUtil.getDataArray(s);
             int scanId = Integer.parseInt(data[7]);
             spectrums.put(Integer.parseInt(data[2]), scanId);
             if (Double.parseDouble(data[data.length - 1]) < 0.0015) {
                 ans.put(scanId, Integer.parseInt(data[3]));
             }
         }
         return ans;
     }
 
     public Map<Integer, List<Peak>> getMSAlignPeaks() throws IOException {
         getMSAlignResults();
         Map<Integer, Scan> scans = getScans();
         BufferedReader input = ReaderUtil.createInputReader(new File(inputDir, "no_digestion_result_detail.txt"));
         Map<Integer, List<Peak>> ans = new HashMap<Integer, List<Peak>>();
         String s;
         Properties properties;
         while ((properties = ReaderUtil.readPropertiesUntil(input, "BEGIN MATCH_PAIR")).size() > 0) {
             int scanId = spectrums.get(Integer.parseInt(properties.getProperty("SPECTRUM_ID")));
             double precursorMass = scans.get(scanId).getPrecursorMass();
             List<Peak> peaks = new ArrayList<Peak>();
             peaks.add(new Peak(0,0 ,0));
             while(!(s = input.readLine()).equals("END MATCH_PAIR")) {
                 String[] data = ReaderUtil.getDataArray(s);
                 double mass = Double.parseDouble(data[3]);
                 double peak = "B".equals(data[4]) ? mass : precursorMass - mass;
                 peaks.add(new Peak(peak, 0, 0));
             }
             peaks.add(new Peak(precursorMass, 0, 0));
             Collections.sort(peaks);
             ans.put(scanId, peaks);
             ReaderUtil.readPropertiesUntil(input, "END PRSM");
         }
         return ans;
     }
 
     public File getScanXmlFile(Scan scan) {
         return new File(xmlScansDir, "scan" + scan.getId() + ".xml");
     }
 
     public Map<Integer, Scan> getScans() throws IOException {
 
         Map<Integer, Scan> scans = new HashMap<Integer, Scan>();
 
         File[] msalignFiles = inputDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith(".msalign");
             }
         });
         if (msalignFiles.length == 1) {
             BufferedReader input = ReaderUtil.getBufferedReader(msalignFiles[0]);
             Properties properties;
             while ((properties = ReaderUtil.readPropertiesUntil(input, "PRECURSOR_MASS")).size() > 0) {
                 Scan scan = new Scan(properties, input);
                 scans.put(scan.getId(), scan);
             }
             return scans;
         }
 
        File scanDir = new File(inputDir,
            "env_multiple_mass"
            //"env2"
        );
         File[] files = scanDir.listFiles(new FileFilter() {
             public boolean accept(File pathname) {
                 return pathname.getName().endsWith(".env");
             }
         });
 
         for (File file : files) {
             BufferedReader input = ReaderUtil.getBufferedReader(file);
 
             Properties properties = ReaderUtil.readPropertiesUntil(input, "BEGIN ENVELOPE");
             String fileName = file.getName();
             int id = Integer.parseInt(fileName.substring(fileName.lastIndexOf("_") + 1, fileName.lastIndexOf(".")));
             Scan scan = new Scan(properties, input, id);
             scans.put(scan.getId(), scan);
         }
         return scans;
     }
 
 }
 
