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
 
     public static final double EVALUE_LIMIT = 0.0024;
 
     private File proteinDatabase;
 
     private File inputDir;
     private File resultDir;
     private File xmlDir;
     private File xmlPrsmDir;
     private File xmlProteinsDir;
 
     private File datasetDir;
 
     private File inputData;
     private File xmlScansDir;
 
     private String mod = null;
 
     public Configuration(String[] arg1, String... arg2) {
         int n = 0;
         if (arg1 != null) {
             n += arg1.length;
         }
         String[] a = new String[n + arg2.length];
         for (int i = 0; i < arg1.length; i++) {
             a[i] = arg1[i];
         }
         for (int i = 0; i < arg2.length; i++) {
             a[i + n] = arg2[i];
         }
         init(a);
     }
     public Configuration(String... args) {
         init(args);
     }
 
     private void init(String[] args) {
         String dataset = "data/salmonella4";
         if (args != null) {
             for (int i = 0; i < args.length; i++) {
                 String arg = args[i];
                 if (arg.startsWith("mod")) {
                     mod = arg.substring(3);
                 } else if (i == 0){
                     dataset = args[0];
                 }
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
         xmlPrsmDir = createDir(xmlDir, "prsm");
         xmlProteinsDir = createDir(xmlDir, "proteins");
 
         xmlScansDir = new File(xmlDir, "scans");
         if (mod != null) {
             xmlScansDir = new File(xmlScansDir, "mod" + mod);
         }
 
         createDir("html");
     }
 
 
     public File getInputDir() {
         return inputDir;
     }
 
     public File getXmlPrsmDir() {
         return xmlPrsmDir;
     }
 
     private Map<Integer, Integer> spectrums = new HashMap<Integer, Integer>();
 
     private double ppmCoef = 5.0d / 1000000d;
 
     public double getPpmCoef() {
         return ppmCoef;
     }
 
     public double[] getEdgeLimits(Peak peak, Peak next) {
         double diff = next.diff(peak);
         double[] limits = new double[2];
         double firstMass = peak.getMass();
         double secondMass = next.getMass();
         if (peak.getIntensity() == 0 && next.getPeakType() == PeakType.Y) {
             firstMass = 5 * secondMass;
         }
         if (next.getIntensity() == 0 && peak.getPeakType() == PeakType.B) {
             secondMass = 5 * firstMass;
         }
 
         double error =  (firstMass + secondMass) * getPpmCoef() / 2;
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
         File nodigestionFile = new File(inputDir, "nodigestion_result_list.txt");
         Map<Integer, Integer> ans = new HashMap<Integer, Integer>();
         badMSAlignResults.clear();
         String s;
 
         if (nodigestionFile.exists()) {
             BufferedReader input = ReaderUtil.createInputReader(nodigestionFile);
             while ((s = input.readLine()) != null) {
                 String[] data = ReaderUtil.getDataArray(s);
                 int scanId = Integer.parseInt(data[7]);
                 spectrums.put(Integer.parseInt(data[2]), scanId);
                 int proteinId = Integer.parseInt(data[3]);
                 double EValue = Double.parseDouble(data[data.length - 1]);
                 evalues.put(scanId, EValue);
                 if (EValue < EVALUE_LIMIT) {
                     ans.put(scanId, proteinId);
                 } else {
                     badMSAlignResults.put(scanId, proteinId);
                 }
             }
         } else {
             File resultTable = new File(inputDir, "result_table.txt");
             BufferedReader input = ReaderUtil.createInputReader(resultTable);
             //input.readLine();
             while ((s = input.readLine()) != null) {
                 String[] data = ReaderUtil.getDataArray(s);
                 int scanId = Integer.parseInt(data[5]);
                 int spectrumId = Integer.parseInt(data[2]);
                 spectrums.put(spectrumId, scanId);
                 int proteinId = Integer.parseInt(data[3]);
                 double EValue = Double.parseDouble(data[data.length - 4]);
                 evalues.put(scanId, EValue);
                 if (EValue < EVALUE_LIMIT) {
                     ans.put(scanId, proteinId);
                 } else {
                     badMSAlignResults.put(scanId, proteinId);
                 }
             }
         }
         return ans;
     }
 
     public Map<Integer, Integer> getBadMSAlignResults() throws IOException {
         getMSAlignResults();
         return badMSAlignResults;
     }
 
     private Map<Integer, Integer> badMSAlignResults = new HashMap<Integer, Integer>();
 
     private Map<Integer, Double> evalues = new HashMap<Integer, Double>();
 
     public Map<Integer, Double> getEvalues() {
         return evalues;
     }
 
     public Map<Integer, List<Peak>> getMSAlignPeaks(Map<Integer, Scan> scans) throws IOException {
         getMSAlignResults();
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
                 boolean isB = "B".equals(data[4]);
                 double value = isB ? mass : precursorMass - mass;
                 Peak peak = isB ? new Peak(value, 0, 0) : new Peak(precursorMass - mass, mass, 0, 0);
                 peaks.add(peak);
             }
             peaks.add(new Peak(precursorMass, 0, 0));
             Collections.sort(peaks);
             ans.put(scanId, peaks);
             ReaderUtil.readPropertiesUntil(input, "END PRSM");
         }
         return ans;
     }
 
     public Map<Integer, double[]> getAnnotatedSpectrums() throws IOException {
         BufferedReader input = ReaderUtil.createInputReader(new File(inputDir, "result_table.txt"));
         //input.readLine();
         Map<Integer, double[]> ans = new HashMap<Integer, double[]>();
         String s;
         while ((s = input.readLine()) != null) {
             String[] data = s.split("[\t]");
             int scanId = Integer.parseInt(data[5]);
             spectrums.put(Integer.parseInt(data[2]), scanId);
             String match = data[data.length - 8];
             List<Double> values = new ArrayList<Double>();
             int brackets = 0;
             match = match.substring(match.indexOf(".") + 1, match.lastIndexOf("."));
             match = match.replaceAll("L", "I");
             double mass = 0;
             int cur = 0;
             boolean isWall = false;
             while (cur < match.length()) {
                 if (brackets ==0) {
                     values.add(isWall ? Integer.MIN_VALUE : mass);
                 }
                 char ch = match.charAt(cur);
                 switch (ch) {
                     case '[':
                         int nextCur = match.indexOf(']', cur);
                         mass += Double.parseDouble(match.substring(cur + 1, nextCur));
                         cur = nextCur;
                         isWall = false;
                         break;
                     case '(':
                         brackets++;
                         isWall = true;
                         break;
                     case ')':
                         brackets--;
                         break;
                     default:
                         mass += Acid.getAcid(ch).getMass();
                 }
                 cur++;
             }
             values.add(mass);
             double[] spectrum = new double[values.size()];
             for (int i = 0; i < spectrum.length; i++) {
                 spectrum[i] = values.get(i);
             }
             ans.put(scanId, spectrum);
         }
         return ans;
     }
 
 
     public Map<Integer, Map<Double, String>> getMSAlignData() throws IOException {
         BufferedReader input = ReaderUtil.createInputReader(new File(inputDir, "nodigestion_result_list.txt"));
         Map<Integer, Map<Double, String>> ans = new HashMap<Integer, Map<Double, String>>();
         String s;
         while ((s = input.readLine()) != null) {
             String[] data = ReaderUtil.getDataArray(s);
             int scanId = Integer.parseInt(data[7]);
             spectrums.put(Integer.parseInt(data[2]), scanId);
             String match = data[data.length - 5];
             Map<Double, String> value = new HashMap<Double, String>();
             int brackets = 0;
             match = match.substring(match.indexOf(".") + 1, match.lastIndexOf("."));
             match = match.replaceAll("L", "I");
             double mass = 0;
             int cur = 0;
             while (cur < match.length()) {
                 if (brackets ==0) {
                     value.put(mass, match.substring(cur));
                 }
                 char ch = match.charAt(cur);
                 switch (ch) {
                     case '[':
                         int nextCur = match.indexOf(']', cur);
                         mass += Double.parseDouble(match.substring(cur + 1, nextCur));
                         cur = nextCur;
                         break;
                     case '(':
                         brackets++;
                         break;
                     case ')':
                         brackets--;
                         break;
                     default:
                         mass += Acid.getAcid(ch).getMass();
                 }
                 cur++;
             }
             ans.put(scanId, value);
         }
         return ans;
     }
 
     public File getScanXmlFile(Scan scan) {
         return new File(xmlScansDir, "scan" + scan.getId() + ".xml");
     }
 
     public File getModifiedScansDir() {
         return new File(inputDir, "env" + mod);
     }
     public Map<Integer, Scan> getScans() throws IOException {
 
         Map<Integer, Scan> scans = new HashMap<Integer, Scan>();
 
         if (mod == null) {
             File[] msalignFiles = inputDir.listFiles(new FilenameFilter() {
                 public boolean accept(File dir, String name) {
                     return name.endsWith(".msalign");
                 }
             });
             if (msalignFiles.length == 1) {
                 File msalignFile = msalignFiles[0];
                 //System.out.println("msalign file = " + msalignFile.getCanonicalPath());
                 BufferedReader input = ReaderUtil.getBufferedReader(msalignFile);
                 Properties properties;
                 while ((properties = ReaderUtil.readPropertiesUntil(input, "PRECURSOR_MASS")).size() > 0) {
                     Scan scan = new Scan(properties, input);
                     scans.put(scan.getId(), scan);
                 }
                 return scans;
             }
         }
 
         File scanDir = new File(inputDir,
             mod == null ?
                     "env_multiple_mass"
                     //"detail_new_theo_patt"
                     : "env" + mod
         );
        //System.out.println("%scanDir = " + scanDir.getCanonicalPath());
         File[] files = scanDir.listFiles(new FileFilter() {
             public boolean accept(File pathname) {
                 return pathname.getName().endsWith(".env");
             }
         });
 
         for (File file : files) {
             BufferedReader input = ReaderUtil.getBufferedReader(file);
 
             Properties properties = ReaderUtil.readPropertiesUntil(input, "BEGIN ENVELOPE");
             String fileName = file.getName();
             String idStart = fileName.substring(fileName.indexOf("_") + 1);
             int idFinish = idStart.indexOf("_");
             if (idFinish < 0) {
                 idFinish = idStart.indexOf(".");
             }
             int id = Integer.parseInt(idStart.substring(0, idFinish));
             Scan scan = new Scan(properties, input, id);
             scans.put(scan.getId(), scan);
         }
         return scans;
     }
 }
 
