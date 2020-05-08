 package org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser;
 
 import com.Ostermiller.util.CSVParser;
 import com.Ostermiller.util.LabeledCSVParser;
 import org.apache.commons.collections15.map.ListOrderedMap;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.Log;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.util.RandomNumberGenerator;
 
 import javax.swing.*;
 import java.io.*;
 import java.util.*;
 import java.util.List;
 
 /**
  * <p>Title: caWorkbench 3.0</p>
  * <p>Description:This Class is for retrieving probe annotation information from default annotation files provided by Affymetrix</p>
  *
  * @author Xuegong Wang, manjunath at genomecenter dot columbia dot edu
  * @version 1.5
  */
 
 public class AnnotationParser {
 
     static Log log = LogFactory.getLog(AnnotationParser.class);
 
     public static final String version = "30";
     // when you change file format etc. this version number need to be changed so that the old file will be deleted.
     static int counter = 0;
 
     public static final String GENE_ONTOLOGY_BIOLOGICAL_PROCESS = "Gene Ontology Biological Process";
     public static final String GENE_ONTOLOGY_CELLULAR_COMPONENT = "Gene Ontology Cellular Component";
     public static final String GENE_ONTOLOGY_MOLECULAR_FUNCTION = "Gene Ontology Molecular Function";
     public static final String GENE_SYMBOL = "Gene Symbol";
     public static final String PROBE_SET_ID = "Probe Set ID";
     public static final String MAIN_DELIMITER = "///";
 
     //field names
     public static final String DESCRIPTION = "Gene Title"; //(full name)
     public static final String ABREV = GENE_SYMBOL; // title(short name)
     public static final String PATHWAY = "Pathway"; // pathway
     public static final String GOTERM = GENE_ONTOLOGY_BIOLOGICAL_PROCESS; // Goterms
     public static final String UNIGENE = "UniGene ID"; // Unigene
     // Todo figure out where locus link info comes from
     public static final String LOCUSLINK = "Entrez Gene"; // LocusLink
     public static final String SWISSPROT = "SwissProt"; // swissprot
 
     private static DSDataSet currentDataSet = null;
     private static Map<DSDataSet, String> datasetToChipTypes = new HashMap<DSDataSet, String>();
     private static Map<String, ListOrderedMap<String, Map<String, String>>> chipTypeToAnnotations = new HashMap<String, ListOrderedMap<String, Map<String, String>>>();
 
     final static String chiptyemapfilename = "chiptypeMap.txt";
     private static String systempDir = System.getProperty("temporary.files.directory");
     public final static String tmpDir;
 
     public static HashMap chiptypeMap = new HashMap();
     private static ArrayList<String> chipTypes = new ArrayList<String>();
     public static final String DEFAULT_CHIPTYPE = "HG_U95Av2";
 
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
                 chipTypes.add(data[1].trim());
                 str = br.readLine();
             }
             br.close();
             File temp = new File(tmpDir + chiptyemapfilename);
             if (temp.exists()) {
                 BufferedReader br2 = new BufferedReader(new FileReader(temp));
                 str = br2.readLine();
                 while (str != null) {
                     String[] data = str.split(",");
 //                    indexfileMap.put(data[0].trim(), data[1].trim());
                     str = br2.readLine();
                 }
                 br2.close();
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
 //        setChipType("HG_U95Av2");
     }
 
     public static DSDataSet getCurrentDataSet() {
         return currentDataSet;
     }
 
     public static void setCurrentDataSet(DSDataSet currentDataSet) {
         AnnotationParser.currentDataSet = currentDataSet;
     }
 
     public static String getCurrentChipType() {
         if (currentDataSet != null) {
             return datasetToChipTypes.get(currentDataSet);
         } else {
             return null;
         }
     }
 
     public static String getChipType(DSDataSet dataset) {
         return datasetToChipTypes.get(dataset);
     }
 
     public static boolean setChipType(DSDataSet dataset, String chiptype) {
         datasetToChipTypes.put(dataset, chiptype);
         currentDataSet = dataset;
         if (chiptypeMap.containsValue(chiptype)) {
             loadAnnotationData(chiptype);
             return true;
         }
         return false;
     }
 
     public static boolean setChipType(DSDataSet dataset, String chiptype, File annotationData) {
         datasetToChipTypes.put(dataset, chiptype);
         currentDataSet = dataset;
         return loadAnnotationData(chiptype, annotationData);
     }
 
 //    public static void clearAll() throws IOException {
 //        File temp = new File(tmpDir +
 //                chiptyemapfilename);
 //        for (Iterator it = indexfileMap.entrySet().iterator();
 //             it.hasNext();) {
 //            String file = (String) it.next();
 //            File idex = new File(tmpDir +
 //                    file);
 //            if (idex.exists()) {
 //                idex.delete();
 //            }
 //            if (file != null) {
 //                file = file.substring(0,
 //                        file.indexOf('.'));
 //            }
 //            File path = new File(tmpDir + file + ".go");
 //            if (path.exists()) {
 //                path.delete();
 //            }
 //        }
 //        if (temp.exists()) {
 //            temp.delete();
 //        }
 //        indexfileMap.clear();
 //    }
 
     private static boolean loadAnnotationData(String chipType) {
         File datafile = new File(chipType + "_annot.csv");
         return loadAnnotationData(chipType, datafile);
     }
 
     private static boolean loadAnnotationData(String chipType, File datafile) {
         if (datafile.exists()) { //data file is found
             try {
                 ListOrderedMap<String, Map<String, String>> annots = new ListOrderedMap<String, Map<String, String>>();
 //                String[][] data = CSVParser.parse(new FileReader(datafile));
                 LabeledCSVParser parser = new LabeledCSVParser(new CSVParser(new BufferedInputStream(new FileInputStream(datafile))));
                 String[] labels = parser.getLabels();
                 while (parser.getLine() != null) {
                     String affyId = parser.getValueByLabel(labels[0]);
                     Map<String, String> values = new HashMap<String, String>();
                     for (int i = 1; i < labels.length; i++) {
                         String label = labels[i];
                         values.put(label, parser.getValueByLabel(label));
                     }
                     annots.put(affyId, values);
                 }
 //                int columns = data[0].length - 1;
 //                for (int i = 0; i < columns; i++) {
 //                    int c = i + 1;
 //                    String header = data[0][c];
 //                    // Always replace map for now
 //                    Map<String, String> map = new HashMap<String, String>();
 //                    annots.put(header, map);
 //                    for (int j = 1; j < data.length; j++) {
 //                        String existing = map.get(data[j][0]);
 //                        if (existing == null) {
 //                            map.put(data[j][0], data[j][c]);
 //                        } else {
 //                            map.put(data[j][0], existing + " /// " + data[j][c]);
 //                        }
 //                    }
 //                }
                 chipTypeToAnnotations.put(chipType, annots);
                 return true;
             } catch (Exception e) {
                 log.error("", e);
                 return false;
             }
         } else {
             return false;
         }
     }
 
     private static File createFilewithID() {
 
         String tempString = "annotationParser" +
                 RandomNumberGenerator.getID() +
                 ".idx";
         return new File(tmpDir + tempString);
     }
 
     public static String getGeneName(String id) {
         try {
             ListOrderedMap<String, Map<String, String>> annots = getAllAnnotationsForDataSet(currentDataSet);
             return annots.get(id).get(GENE_SYMBOL);
         } catch (Exception e) {
             log.warn("Problem getting gene name, returning id. (AffyID: " + id+")");
             return id;
         }
     }
 
     /**
      * This method returns required information in different format.
      * And it can look for information both  local file.
      *
      * @param affyid  affyID as string
      * @param fieldID //defined at FieldName.java
      *                0 : name(full name)
      *                1 : title(short name)
      *                2 : pathway
      *                3 : Goterms
      *                4: unigene
      *                5:LocusLink
      *                6:swissprotids
      * @return 0: String[]
      *         1: String[]
      *         2: String[]  pathway or null
      *         3: string[]  Goterms//tab delimited or null
      * @author Xuegong Wang
      * @version 1.0
      */
     static public String[] getInfo(String affyID, String fieldID) {
         try {
             Map<String, String> annots = getAllAnnotationsForDataSet(currentDataSet).get(affyID);
             String field = annots.get(fieldID);
             return field.split(MAIN_DELIMITER);
         } catch (Exception e) {
             if (affyID != null) {
                 log.warn("Error getting info for affyId (" + affyID + "):" + e);
             }
             return null;
         }
 //        if (data != null) {
 //            String[] info = null;
 //            String inf = data.split("/////")[fieldID];
 //
 //            info = inf.split("\t");
 //            return info;
 //        } else {
 //            return null;
 //        }
     }
 
     //used to parse info from raw go data
     private static String parseGo(String godata) {
         String result = "";
         String[] gos = godata.split(MAIN_DELIMITER);
 
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
 
 //    /**
 //     * Get AffyIDs related to Goterm
 //     *
 //     * @param Unigene
 //     * @return AffyIDs
 //     */
 //    public static String[] fromGoToAffy(String goName) {
 //        if (goes == null) {
 //            createGoAffytable();
 //        }
 //        if (goes != null) {
 //            Vector<String> ids = (Vector<String>) goes.get(goName);
 //            if (ids == null) {
 //                return null;
 //            } else {
 //                return (String[]) ids.toArray();
 //            }
 //        }
 //        return null;
 //    }
 
     public static HashMap<String, Vector<String>> getGotable() {
         try {
             ListOrderedMap<String, Map<String, String>> annots = getAllAnnotationsForDataSet(currentDataSet);
             HashMap<String, Vector<String>> GOIDToAffy = new HashMap<String, Vector<String>>();
             HashMap<String, Vector<String>> affyToGOID = new HashMap<String, Vector<String>>();
             for (String marker : annots.keySet()) {
                 String bio = annots.get(marker).get(GENE_ONTOLOGY_BIOLOGICAL_PROCESS);
                 Vector<String> goIds = getGOIds(bio);
                 String cell = annots.get(marker).get(GENE_ONTOLOGY_CELLULAR_COMPONENT);
                 goIds.addAll(getGOIds(cell));
                 String molecular = annots.get(marker).get(GENE_ONTOLOGY_MOLECULAR_FUNCTION);
                 goIds.addAll(getGOIds(molecular));
                 affyToGOID.put(marker, goIds);
             }
             // todo: Make this return the correct map, I think we need affyToGOID here
             return GOIDToAffy;
         } catch (Exception e) {
             log.warn("Problem getting go table.", e);
             return null;
         }
     }
 
     public static ListOrderedMap<String, Map<String, String>> getAllAnnotationsForDataSet(DSDataSet dataset) {
         String chipType = datasetToChipTypes.get(dataset);
         ListOrderedMap<String, Map<String, String>> annots = chipTypeToAnnotations.get(chipType);
         return annots;
     }
 
     public static Set<String> getAffyIds() {
         try {
             return getAllAnnotationsForDataSet(currentDataSet).keySet();
         } catch (Exception e) {
             log.warn("Problem getting current affy IDs.", e);
             return null;
         }
     }
 
     private static Vector<String> getGOIds(String goString) {
         Vector<String> goIds = new Vector<String>();
         String[] blocks = goString.split(MAIN_DELIMITER);
         for (int i = 0; i < blocks.length; i++) {
             String block = blocks[i];
             String id = block.split("//")[0];
             goIds.add(id);
         }
         return goIds;
     }
 
 //    //create go term table that refers go term to affyid
 //    private static void createGoAffytable(String chipType) {
 //        if (chipType != null && !chipType.equalsIgnoreCase("")) {
 //
 //            String indexfilename = "new";
 //            if (indexfilename != null) {
 //
 //                indexfilename = indexfilename.substring(0,
 //                        indexfilename.indexOf('.'));
 //            }
 //            File path = new File(tmpDir + indexfilename + ".go");
 //
 //            if (path.exists()) {
 //                try {
 //                    ObjectInputStream ob = new ObjectInputStream(new
 //                            FileInputStream(
 //                            path));
 //                    goes = (Gotable) ob.readObject();
 //                    ob.close();
 //                }
 //                catch (ClassNotFoundException ex) {
 //                    ex.printStackTrace();
 //                }
 //                catch (IOException ex) {
 //                    ex.printStackTrace();
 //                }
 //            } else {
 //                createNewGoTable(chipType);
 //            }
 //        }
 //    }
 
 //    private static void createNewGoTable(String chipType) {
 //        goes = new Gotable();
 //        ProgressBar pb = ProgressBar.create(ProgressBar.BOUNDED_TYPE);
 //        pb.setTitle("Processing Go Term index file...");
 //        pb.setMessage("Processing Go Term index file...");
 //        pb.setBounds(new ProgressBar.IncrementModel(0, affyIDs.keySet().size(), 0, affyIDs.keySet().size(), 1));
 //        pb.start();
 //        pb.updateTo(0f);
 //
 //        int count = 0;
 //        for (Iterator ids = affyIDs.keySet().iterator(); ids.hasNext();
 //                ) {
 //            String id = (String) ids.next();
 //            String[] info = getInfo(id, AnnotationParser.GOTERM);
 //            pb.setMessage("Processing probe");
 //            pb.updateTo(count++);
 //            if (UILauncher.splash.isVisible()) {
 //                UILauncher.splash.setProgressBarString(
 //                        "Processing probe:" + count++);
 //            }
 //            for (int i = 0; i < info.length; i++) {
 //                if (info[i] != null) {
 //                    String goid = info[i].split("::")[0];
 //                    Vector<String> hs = ((Vector<String>) goes.get(goid) == null) ? new Vector<String>() : (Vector<String>) goes.get(goid);
 //                    hs.add(id);
 //                    goes.put(goid, hs);
 //                }
 //            }
 //        }
 //        pb.stop();
 //    }
 
 //    static class Gotable
 //            extends HashMap implements Serializable {
 //    }
 
     public static String matchChipType(DSDataSet dataset, String id, boolean askIfNotFound) {
         String chip = (String) chiptypeMap.get(id);
         String defaultChipChoice = chip;
         if (defaultChipChoice == null) {
             defaultChipChoice = DEFAULT_CHIPTYPE;
         }
         Object[] possibleValues = chipTypes.toArray();
         Arrays.sort(possibleValues);
         Object selectedValue = JOptionPane.showInputDialog(null, "Please confirm your chip type",
                 "Select Chip Type", JOptionPane.QUESTION_MESSAGE, null,
                 possibleValues, defaultChipChoice);
         if (selectedValue != null) {
             chip = (String) selectedValue;
             currentDataSet = dataset;
             if (chip.equals("Other")) {
                 File userFile = selectUserDefinedAnnotation(dataset);
                 if (userFile != null) {
                     chip = userFile.getName();
                     chipTypes.add(chip);
                     setChipType(dataset, chip, userFile);
                 }
             } else {
                 setChipType(dataset, chip);
             }
             return chip;
         }
         return "Not Found/Aborted";
     }
 
     public static File selectUserDefinedAnnotation(DSDataSet dataset) {
         JFileChooser chooser = new JFileChooser();
         ExampleFilter filter = new ExampleFilter();
         filter.addExtension("csv");
         filter.setDescription("CSV files");
         chooser.setFileFilter(filter);
         chooser.setDialogTitle("Please select the annotation file");
         int returnVal = chooser.showOpenDialog(null);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             File userAnnotations = chooser.getSelectedFile();
             String chipType = "User Defined";
             return userAnnotations;
 //            loadAnnotationData(chipType, userAnnotations);
 //            datasetToChipTypes.put(dataset, chipType);
         } else {
             return null;
         }
     }
 
     // Custom annotations loaded by the user (not necessarily Affy)
 
     private static class CustomAnnotations {
 
         public CustomAnnotations() {
             annotations = new ListOrderedMap<String, Map<String, String>>();
         }
 
         private ListOrderedMap<String, Map<String, String>> annotations;
     }
 
     private static HashMap<DSDataSet, CustomAnnotations> customAnnotations = new HashMap<DSDataSet, CustomAnnotations>();
 
     private static ListOrderedMap<String, Map<String, String>> getCustomAnnots(DSDataSet dataSet) {
         CustomAnnotations annots = customAnnotations.get(dataSet);
         if (annots == null) {
             annots = new CustomAnnotations();
             customAnnotations.put(dataSet, annots);
         }
         return annots.annotations;
     }
 
     /**
      * Takes a file in CSV format and parses out custom annotations. The first row has the annotation names
      * starting in column 2. The first column has the dataset item names starting in row 2. For example, for a
      * marker annotation file:
      * <table>
      * <tr><td>(blank)</td><td>Gene Name</td><td>Pathway</td></tr>
      * <tr><td>1973_s_at</td><td>MYC</td><td>Example // KEGG</td></tr>
      * <tr><td>1974_s_at</td><td>TP53</td><td>Example // KEGG</td></tr>
      * </table>
      * etc.
      *
      * @param file    the file in CSV format.
      * @param dataSet the data set to annotate.
      * @return true if sucessfully parsed, false otherwise.
      */
     public static boolean parseCustomAnnotations(File file, DSDataSet dataSet) {
         try {
             ListOrderedMap<String, Map<String, String>> customAnnots = getCustomAnnots(dataSet);
             String[][] data = CSVParser.parse(new FileReader(file));
             int columns = data[0].length - 1;
             for (int i = 0; i < columns; i++) {
                 int c = i + 1;
                 String header = data[0][c];
 //                Map<String, String> map = customAnnots.get(header);
 //                if (map == null) {
                 // Always replace map for now
                 Map<String, String> map = new HashMap<String, String>();
                 customAnnots.put(header, map);
 //                }
                 for (int j = 1; j < data.length; j++) {
                     String existing = map.get(data[j][0]);
                     if (existing == null) {
                         map.put(data[j][0], data[j][c]);
                     } else {
                         map.put(data[j][0], existing + " /// " + data[j][c]);
                     }
                 }
             }
             return true;
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
 
     /**
      * Gets a set of the names of all custom annotations.
      *
      * @param dataSet the data set for which to find annotations.
      */
     public static Set<String> getCustomAnnotations(DSDataSet dataSet) {
         ListOrderedMap<String, Map<String, String>> customAnnots = getCustomAnnots(dataSet);
         return customAnnots.keySet();
     }
 
     /**
      * Gets a custom annotation value.
      *
      * @param annotation the annotation name
      * @param item       the item name for which to find a value
      * @param dataSet    the data set for which to look up the annotation
      */
     public static String getCustomAnnotationValue(String annotation, String item, DSDataSet dataSet) {
         ListOrderedMap<String, Map<String, String>> customAnnots = getCustomAnnots(dataSet);
         Map<String, String> map = customAnnots.get(annotation);
         if (map == null) {
             return null;
         } else {
             return map.get(item);
         }
     }
 
     /**
      * Gets a grouping of the items in the dataset by annotation.
      *
      * @param annotation          the annotation by which to group.
      * @param annotationSeparator the separator sequence to use if the annotation can have compound values ('///' for Affy annotations).
      * @param dataSet             the data set for which to look up annotations.
      * @return a map of all annotation values to the list of item names that have that value.
      */
     public static Map<String, List<String>> getCustomAnnotationGroupings(String annotation, String annotationSeparator, DSDataSet dataSet) {
         ListOrderedMap<String, Map<String, String>> customAnnots = getCustomAnnots(dataSet);
         if (annotationSeparator == null) {
             annotationSeparator = MAIN_DELIMITER;
         }
         Map<String, List<String>> groups = new HashMap<String, List<String>>();
         Map<String, String> map = customAnnots.get(annotation);
         if (map == null) {
             return null;
         } else {
             Set<String> keys = map.keySet();
             for (String key : keys) {
                 String values = map.get(key);
                 String[] tokens = values.split(annotationSeparator);
                 for (int i = 0; i < tokens.length; i++) {
                     String value = tokens[i].trim();
                     List<String> group = groups.get(value);
                     if (group == null) {
                         group = new ArrayList<String>();
                         groups.put(value, group);
                     }
                     group.add(key);
                 }
             }
         }
         return groups;
     }
 }
