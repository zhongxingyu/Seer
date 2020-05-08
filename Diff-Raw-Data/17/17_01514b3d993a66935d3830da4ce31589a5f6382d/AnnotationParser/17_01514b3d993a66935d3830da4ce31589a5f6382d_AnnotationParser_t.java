 package org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser;
 
 import com.Ostermiller.util.CSVParse;
 import com.Ostermiller.util.CSVParser;
 import com.Ostermiller.util.ExcelCSVParser;
 import com.Ostermiller.util.LabeledCSVParser;
 import org.geworkbench.bison.util.RandomNumberGenerator;
 import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.ExampleFilter;
 
 import javax.swing.*;
 import java.awt.*;
 import java.io.*;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import org.geworkbench.engine.config.UILauncher;
 import java.util.Vector;
 import org.geworkbench.util.ProgressBar;
 
 /**
  * <p>Title: caWorkbench 3.0</p>
  * <p>Description:This Class is for retrieving probe annotation information from default annotation files provided by Affymetrix</p>
  *
  * @author Xuegong Wang, manjunath at genomecenter dot columbia dot edu
  * @version 1.5
 */
 
 public class AnnotationParser {
 
     public static final String version = "30";
     // when you change file format etc. this version number need to be changed so that the old file will be deleted.
     static int counter = 0;
     //field names
     public static final int DESCRIPTION = 0; //(full name)
     public static final int ABREV = 1; // title(short name)
     public static final int PATHWAY = 2; // pathway
     public static final int GOTERM = 3; // Goterms
     public static final int UNIGENE = 4; // Unigene
     public static final int LOCUSLINK = 5; // LocusLink
     public static final int SWISSPROT = 6; // swissprot
 
     public static HashMap<String, String> affyIDs = new HashMap<String, String>();
     public static HashMap<String, Vector<String>> geneNameMap = new HashMap<String,Vector<String>>();
 
     static String chipType = ""; //default;
     final static String chiptyemapfilename = "chiptypeMap.txt";
     private static String systempDir = System.getProperty(
             "temporary.files.directory");
     public final static String tmpDir;
 
     static File annoFile = null;
     static File indx = null;
     static Gotable goes = null;
 
     public static HashMap chiptypeMap = new HashMap();
     public static HashMap indexfileMap = new HashMap();
     public static String getChipType() {
         return chipType;
     }
 
     static {
         if (systempDir == null) {
             systempDir = "temp" + File.separator + "GEAW";
         }
         tmpDir = systempDir + File.separator +
                  "annotationParser/";
         File dir = new File(tmpDir);
         if (!dir.exists()) {
             dir.mkdir();
         }
         BufferedReader br = new BufferedReader(new InputStreamReader(
             AnnotationParser.class.getResourceAsStream(chiptyemapfilename)));
         try {
             String str = br.readLine();
             while (str != null) {
                 String[] data = str.split(",");
                 chiptypeMap.put(data[0].trim(), data[1].trim());
                 chiptypeMap.put(data[1].trim(), data[0].trim());
                 str = br.readLine();
             }
             br.close();
             File temp = new File(tmpDir + chiptyemapfilename);
             if (temp.exists()) {
                 BufferedReader br2 = new BufferedReader(new FileReader(temp));
                 str = br2.readLine();
                 while (str != null) {
                     String[] data = str.split(",");
                     indexfileMap.put(data[0].trim(), data[1].trim());
                     str = br2.readLine();
                 }
                 br2.close();
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         setChipType("HG_U95Av2");
     }
 
     public static void setChipType(String chiptype) {
         if (chiptypeMap.containsValue(chiptype)) {
             setType(chiptype);
         }
         else {
             JOptionPane.showMessageDialog(null,
                                           "No such format found in database");
         }
     }
 
     public static void clearAll() throws IOException {
         File temp = new File(tmpDir +
                              chiptyemapfilename);
         for (Iterator it = indexfileMap.entrySet().iterator();
                            it.hasNext(); ) {
             String file = (String) it.next();
             File idex = new File(tmpDir +
                                  file);
             if (idex.exists()) {
                 idex.delete();
             }
             if (file != null) {
                 file = file.substring(0,
                                       file.indexOf('.'));
             }
             File path = new File(tmpDir + file + ".go");
             if (path.exists()) {
                 path.delete();
             }
         }
         if (temp.exists()) {
             temp.delete();
         }
         indexfileMap.clear();
     }
 
     private static void setType(String chiptype) {
         chipType = chiptype;
         File datafile = new File(chipType +
                                  "_annot.csv");
         if (datafile.exists()) { //data file is found
             annoFile = datafile;
         }
         else { //data file is not found, search temp folder first.
             datafile = new File(tmpDir + chipType +
                                 "_annot.csv");
             if (datafile.exists()) { //data file is found
                 annoFile = datafile;
             }
             else {
                 try {
                     String ur =
                             System.getProperty("data.download.site") + chipType + "_annot.csv";
                     if (UILauncher.splash.isVisible()) {
                         UILauncher.splash.setProgressBarString(
                                 "Downloading data file...");
                     }
                     URL url = new URL(ur);
                     InputStream is = url.openStream();
                     BufferedReader br = new BufferedReader(new
                             InputStreamReader(is));
                     datafile = new File(tmpDir + chipType +
                                         "_annot.csv");
                     BufferedWriter bwr = new BufferedWriter(new
                             FileWriter(datafile));
                     String s = br.readLine();
                     while (s != null) {
                         bwr.write(s);
                         bwr.newLine();
                         s = br.readLine();
                     }
                     bwr.close();
                     br.close();
                     setType(chipType);
                 }
                 catch (Exception e) {
 
                     File d = new File(tmpDir);
                     if (!d.exists()) {
                         d.mkdir();
                     }
                     JOptionPane.showMessageDialog(null,
                                                   "<html>Can't find file " +
                                                   datafile.getName() +
                                                   "<br>Please download " +
                                                   datafile.getName() +
                                                   " to " + d.getAbsolutePath() +
                                                   "</html>");
                     return;
                 }
             }
         }
 
         if (!chiptype.equals("Other") && !chiptype.equals("Genepix")) {
             try {
                 String indexfilename = (String)
                                        indexfileMap.get(chiptypeMap.
                         get(chiptype));
                 if (indexfilename == null) { //no such file in record
                     indx = createFilewithID();
                 }
                 else {
                     indx = new File(tmpDir + indexfilename);
                 }
                 if (indx.exists() && indx.length() > 1) { //if indx file exist and valid
                     BufferedReader br = new BufferedReader(new
                             FileReader(
                                     indx));
                     if (datafile.exists()) { //datafile is found
                         annoFile = datafile;
                         String ver = br.readLine();
                         String lastModified = br.readLine();
                         if ( (ver == null) ||
                              (!ver.equalsIgnoreCase(version)) ||
                              (lastModified == null) ||
                              (datafile.lastModified() !=
                               Long.parseLong(lastModified))) {
                             br.close();
                             parse();
                         }
                         else {
                             br.close();
                             loadIndx();
                         }
                     }
                     createGoAffytable();
                 }else{
                     parse();
                     createGoAffytable();
                 }
             }
             catch (Exception ioe) {
                 ioe.printStackTrace();
             }
         }
     }
 
     private static File createFilewithID() {
 
         String tempString = "annotationParser" +
                             RandomNumberGenerator.getID() +
                             ".idx";
         return new File(tmpDir + tempString);
     }
 
     private static void loadIndx() throws HeadlessException,
             IOException {
         BufferedReader br = new BufferedReader(new
                                                FileReader(
                                                    indx));
         affyIDs.clear();
         //skip first two lines
         br.readLine();
         br.readLine();
         String oneline = br.readLine();
         while (oneline != null) {
             String rows[] = oneline.split("\t\t");
             if (rows.length == 2) {
                 affyIDs.put(rows[0], rows[1]);
             }
             else {
                 JOptionPane.showMessageDialog(null,
                                               "error in line" +
                                               oneline);
             }
             oneline = br.readLine();
         }
         br.close();
         populateGeneNameMap();
         System.gc();
     }
 
     static void populateGeneNameMap(){
         for (String affyid : affyIDs.keySet()){
             String geneName = getGeneName(affyid);
             Vector<String> ids = geneNameMap.get(geneName);
             if (ids == null)
                 ids = new Vector<String>();
             ids.add(affyid);
             geneNameMap.put(geneName, ids);
         }
     }
 
     static void parse() throws IOException {
 
         String line;
         if (!UILauncher.splash.isVisible()) {
             JOptionPane.showMessageDialog(null, "<html>Chip type "
                                           + chipType
                                           + " recognized.<br> We are going to initialize related annotation files<br> </html>");
         }
         else {
             UILauncher.splash.setProgressBarString(
                     "Creating data index file...");
         }
         BufferedWriter br = new BufferedWriter(new FileWriter(indx));
         BufferedReader xin = new BufferedReader(new FileReader(
             annoFile));
         line = xin.readLine();
         xin.close();
         System.out.println(indx.getAbsolutePath());
         FileInputStream fileIn = new FileInputStream(annoFile);
         ProgressBar pb = ProgressBar.create(ProgressBar.BOUNDED_TYPE);
         pb.setTitle("Creating index file..");
         pb.setMessage("Creating index file..");
         DefaultBoundedRangeModel model = new ProgressBar.IncrementModel(0, 10000, 0, 10000, 1);
         pb.setBounds(model);
         pb.start();
 
         if (UILauncher.splash.isVisible()) {
             pb.stop();
         }
         pb.updateTo(0f);
 
         InputStreamReader reader = new InputStreamReader(fileIn);
         CSVParse parse = null;
         if (line.startsWith("\"")) {
             parse = new CSVParser(reader);
         }
         else {
             parse = new ExcelCSVParser(reader);
         }
         LabeledCSVParser parser = new LabeledCSVParser(parse);
 
         int count = 0;
         br.write(version + '\n');
         String time = (annoFile.lastModified()) + "\n";
         br.write(time);
         affyIDs.clear();
         while (parser.getLine() != null) {
             if (UILauncher.splash.isVisible()) {
                 UILauncher.splash.setProgressBarString("probes parsed: " +
                         count++);
             }
             else {
                 pb.updateTo(count++);
                 if (count > model.getMaximum())
                     model.setMaximum(count + 1000);
             }
 
             String id = parser.getValueByLabel("Probe Set ID");
 
             String data = executeLine(parser);
 
             String dataLine = id + "\t\t" + data + '\n';
             affyIDs.put(id, data);
             br.write(dataLine);
         }
         br.close();
         BufferedWriter bw = new BufferedWriter(new FileWriter(new
                 File(tmpDir +
                      chiptyemapfilename), true));
         String pair = (String) chiptypeMap.get(chipType);
         if (pair == null) {
             pair = (String) affyIDs.keySet().iterator().next(); //use the first one as identifyer for the chiptype
         }
         indexfileMap.put(pair, indx.getName());
         pair = pair + "," + indx.getName() + '\n';
         bw.write(pair);
         bw.close();
         pb.stop();
 
         populateGeneNameMap();
         createNewGoTable();
     }
 
     public static String getGeneName(String id){
         String data = (String)affyIDs.get(id);
         if (data != null){
             String[] tokens = data.split("/////");
             if (tokens.length >= 2){
                if (tokens[1].contains("///"))
                    return tokens[1].split("///")[0];
                 return tokens[1];
             }
         }
         return null;
     }
 
     /**
      * executeLine
      *
      * @param line String
      */
     private static String executeLine(LabeledCSVParser parser) {
         String delim = "/////";
         StringBuffer data = new StringBuffer();
         String name = parser.getValueByLabel("Title");
         if (name == null) {
             name = parser.getValueByLabel("Gene Title");
         }
         data.append(name);
         String title = parser.getValueByLabel("Gene Symbol");
         if (title == null) {
             title = parser.getValueByLabel("Probe Set ID");
         }
         data.append(delim).append(title);
         String xpath = "";
 
         String pathGenMAPP = parser.getValueByLabel("Pathways GenMAPP");
         if (pathGenMAPP != null) {
             xpath = "GenMAPP : " + pathGenMAPP.trim() + '\t';
         }
 
         String pathKEGG = parser.getValueByLabel("Pathways KEGG");
         if (pathKEGG != null) {
             xpath = xpath + "KEGG : " + pathKEGG.trim();
         }
         if (xpath.length() < 1) {
             xpath = " ";
         }
         data.append(delim).append(xpath);
 
         String x = "";
         String goProc = parser.getValueByLabel("Biological Process (GO)");
         String goComp = parser.getValueByLabel("Cellular Component (GO)");
         String goFunc = parser.getValueByLabel("Molecular Function (GO)");
         if (goProc == null) {
             goProc = parser.getValueByLabel("Gene Ontology Biological Process");
             goComp = parser.getValueByLabel("Gene Ontology Cellular Component");
             goFunc = parser.getValueByLabel("Gene Ontology Molecular Function");
         }
 
         if (goProc != null) {
             x = parseGo(goProc);
         }
         if (goComp != null) {
             x = x + parseGo(goComp);
         }
         if (goFunc != null) {
             x = x + parseGo(goFunc);
         }
         if (x.length() < 1) {
             x = " ";
         }
         else {
         }
         data.append(delim).append(x);
 
         String unigene = parser.getValueByLabel("Unigene");
         if (unigene == null) {
             unigene = parser.getValueByLabel("UniGene ID");
         }
 
         unigene = unigene.split("//")[0].trim();
         if (unigene.trim().equalsIgnoreCase("---")) {
             unigene = " ";
         }
         data = data.append(delim).append(unigene);
 
         String locus = parser.getValueByLabel("LocusLink");
         if(locus == null){
             locus = parser.getValueByLabel("Entrez Gene");
         }
         if (locus.equalsIgnoreCase("---")) {
             locus = " ";
         }
         else {
             locus = locus.replaceAll(" /// ", "\t");
             locus = locus.replaceAll("---\t", "");
         }
         data = data.append(delim).append(locus);
 
         String protids = parser.getValueByLabel("SwissProt");
         if (protids.equalsIgnoreCase("---")) {
             protids = " ";
         }
         else {
             protids = protids.replaceAll(" /// ", "\t");
             protids = protids.replaceAll("---\t", "");
         }
         data = data.append(delim).append(protids);
         return data.toString();
     }
 
     static public String getInfoAsString(String affyID, int fieldID) {
         String[] result = getInfo(affyID, fieldID);
 
         String info = " ";
         if (result == null) {
             return affyID;
         }
 
         if (result.length > 0) {
             info = result[0];
             for (int i = 1; i < result.length; i++) {
                 info += "/" + result[i];
             }
         }
 
         return info;
     }
 
     /**
      *This method returns required information in different format.
      * And it can look for information both  local file.
      * @param affyid
      * affyID as string
      * @param fieldID //defined at FieldName.java
      *   0 : name(full name)
      *   1 : title(short name)
      *   2 : pathway
      *   3 : Goterms
      *   4: unigene
      *   5:LocusLink
      *   6:swissprotids
      * @return
      *   0: String[]
      *   1: String[]
      *   2: String[]  pathway or null
      *   3: string[]  Goterms//tab delimited or null
      *
      * @author Xuegong Wang
      * @version 1.0
      */
     static public String[] getInfo(String affyID, int fieldID) {
         String data = (String) affyIDs.get(affyID);
         if (data != null) {
             String[] info = null;
             String inf = data.split("/////")[fieldID];
 
             info = inf.split("\t");
             return info;
         }
         else {
             return null;
         }
     }
 
     //used to parse info from raw go data
     private static String parseGo(String godata) {
         String result = "";
         String[] gos = godata.split("///");
 
         for (int i = 0; i < gos.length; i++) {
             String onego = gos[i];
             String[] gocat = onego.split("//");
             if (gocat.length > 1) {
                 int k = Integer.parseInt(gocat[0].trim()) + 10000000;
                 gocat[0] = Integer.toString(k).substring(1);
                 result = new String(result + "GO:" + gocat[0] + "::" +
                                     gocat[1].trim() + "\t");
             }
         }
         return result;
     }
 
     /**
      * Get AffyIDs related to Goterm
      * @param Unigene
      * @return AffyIDs
      */
     public static String[] fromGoToAffy(String goName) {
         if (goes == null) {
             createGoAffytable();
         }
         if (goes != null) {
             Vector<String> ids = (Vector<String>) goes.get(goName);
             if (ids == null) {
                 return null;
             }
             else {
                 return (String[])ids.toArray();
             }
         }
         return null;
     }
 
     public static HashMap getGotable() {
         if (goes == null) {
             createGoAffytable();
         }
         return goes;
     }
 
     //create go term table that refers go term to affyid
     private static void createGoAffytable() {
         if (chipType != null && !chipType.equalsIgnoreCase("")) {
 
             String indexfilename = indx.getName();
             if (indexfilename != null) {
 
                 indexfilename = indexfilename.substring(0,
                         indexfilename.indexOf('.'));
             }
             File path = new File(tmpDir + indexfilename + ".go");
 
             if (path.exists()) {
                 try {
                     ObjectInputStream ob = new ObjectInputStream(new
                             FileInputStream(
                                     path));
                     goes = (Gotable) ob.readObject();
                     ob.close();
                 }
                 catch (ClassNotFoundException ex) {
                     ex.printStackTrace();
                 }
                 catch (IOException ex) {
                     ex.printStackTrace();
                 }
             }
             else {
                 createNewGoTable();
             }
         }
     }
 
     private static void createNewGoTable() {
         goes = new Gotable();
         ProgressBar pb = ProgressBar.create(ProgressBar.BOUNDED_TYPE);
         pb.setTitle("Processing Go Term index file...");
         pb.setMessage("Processing Go Term index file...");
         pb.setBounds(new ProgressBar.IncrementModel(0, affyIDs.keySet().size(), 0, affyIDs.keySet().size(), 1));
         pb.start();
         pb.updateTo(0f);
 
         int count = 0;
         for (Iterator ids = affyIDs.keySet().iterator(); ids.hasNext();
                 ) {
             String id = (String) ids.next();
             String[] info = getInfo(id, AnnotationParser.GOTERM);
             pb.setMessage("Processing probe");
             pb.updateTo(count++);
             if (UILauncher.splash.isVisible()) {
                 UILauncher.splash.setProgressBarString(
                         "Processing probe:" + count++);
             }
             for (int i = 0; i < info.length; i++) {
                 if (info[i] != null) {
                     String goid = info[i].split("::")[0];
                     Vector<String> hs = ((Vector<String>) goes.get(goid) == null) ? new Vector<String>() : (Vector<String>) goes.get(goid);
                     hs.add(id);
                     goes.put(goid, hs);
                 }
             }
         }
         pb.stop();
         SerilizeGoTermData();
     }
 
     public static void SerilizeGoTermData() {
         String indexfilename = indx.getName();
         if (indexfilename != null) {
 
             indexfilename = indexfilename.substring(0,
                     indexfilename.indexOf('.'));
         }
         File path = new File(tmpDir + indexfilename + ".go");
         if (path.exists()) {
             path.delete();
         }
         path = new File(tmpDir + indexfilename + ".go");
         try {
             ObjectOutputStream oj = null;
 
             oj = new ObjectOutputStream(new FileOutputStream(
                     path));
             oj.writeObject(goes);
             oj.flush();
             oj.close();
         }
         catch (IOException ex1) {
             ex1.printStackTrace();
         }
     }
 
     static class Gotable
             extends HashMap implements Serializable {
     }
 
     public static String matchChipType(String id) {
         String chip = (String) chiptypeMap.get(id);
         if ( (chip != null) && (!chip.equalsIgnoreCase(chipType))) {
             setChipType(chip);
         }
         if (indexfileMap.get(id) != null) {
             chipType = chip;
             indx = new File(tmpDir + indexfileMap.get(id));
             try {
                 loadIndx();
                 createGoAffytable();
             }
             catch (IOException ex) {
                 ex.printStackTrace();
             }
             catch (HeadlessException ex) {
                 ex.printStackTrace();
             }
         }
         return chip;
     }
 
     public static void callUserDefinedAnnotation() throws IOException {
         JFileChooser chooser = new JFileChooser();
         ExampleFilter filter = new ExampleFilter();
         filter.addExtension("csv");
         filter.setDescription("CSV files");
         chooser.setFileFilter(filter);
         chooser.setDialogTitle("Please select the annotation file");
         int returnVal = chooser.showOpenDialog(null);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             annoFile = chooser.getSelectedFile();
             chipType = "User Defined";
             indx = createFilewithID();
             parse();
             createGoAffytable();
         }
         else {
             return;
         }
     }
 }
