 package org.agmip.translators.csv;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.agmip.core.types.TranslatorInput;
 
 public class DomeInput implements TranslatorInput {
 
     private static final Logger log = LoggerFactory.getLogger(DomeInput.class);
     private HashMap<String, Object> dome = new HashMap<String, Object>();
     private ArrayList<String> generatorFunctions = new ArrayList<String>();
     private ArrayList<String> ruleFunctions = new ArrayList<String>();
 
     public DomeInput() {
         generatorFunctions.add("AUTO_PDATE()");
         generatorFunctions.add("AUTO_REPLICATE_EVENTS()");
         ruleFunctions.add("AUTO_PDATE()");
     }
 
     @Override
     public Map readFile(String fileName) throws Exception {
         if (fileName.toUpperCase().endsWith("CSV")) {
             FileInputStream stream = new FileInputStream(fileName);
             readCSV(new FileInputStream(fileName));
             stream.close();
         }
         return dome;
     }
 
     public void readCSV(InputStream stream) throws Exception {
         BufferedReader br = new BufferedReader(new InputStreamReader(stream));
         HashMap<String, String> info = new HashMap<String, String>();
 //        HashMap<String, ArrayList<String>> linkOvl = new HashMap<String, ArrayList<String>>();
 //        HashMap<String, ArrayList<String>> linkStg = new HashMap<String, ArrayList<String>>();
         ArrayList<HashMap<String, String>> rules = new ArrayList<HashMap<String, String>>();
         ArrayList<HashMap<String, String>> generators = new ArrayList<HashMap<String, String>>();
         ArrayList<ArrayList<HashMap<String, String>>> genGroups = new ArrayList<ArrayList<HashMap<String, String>>>();
         CSVReader reader = new CSVReader(br);
         String[] nextLine;
         int ln = 0;
//        boolean hasGenerator = false;
 
         while ((nextLine = reader.readNext()) != null) {
             boolean isGenerator = false;
             HashMap<String, String> lineMap = new HashMap<String, String>();
             ln++;
             if (nextLine[0].startsWith("&")) {
                 // This is an official dome line.
                 log.debug("Found a DOME instruction at line {}", ln);
                 String cmd = nextLine[1].trim().toUpperCase();
                 if (cmd.equals("INFO")) {
                     info.put(nextLine[2].toLowerCase(), nextLine[3].toUpperCase());
                 } else if ((cmd.equals("FILL") || cmd.equals("REPLACE") || cmd.equals("REPLACE_FIELD_ONLY"))) {
                     StringBuilder args = new StringBuilder();
                     if (nextLine[3].endsWith("()")) {
                         log.debug("Found fun {}", nextLine[3].toUpperCase());
                         // Handle Arguments
                         if (generatorFunctions.contains(nextLine[3].toUpperCase())) {
                             if (!cmd.equals("FILL") || !ruleFunctions.contains(nextLine[3].toUpperCase())) {
                                 log.debug("Found a generator!!!");
                                 isGenerator = true;
//                                hasGenerator = true;
                             }
                         }
                         int argLen = nextLine.length - 3;
 
                         if (argLen != 0) {
                             args.append(nextLine[3]);
                             if (argLen > 1) {
                                 for (int i = 4; i < nextLine.length; i++) {
                                     args.append("|");
                                     if (!nextLine[i].startsWith("!")) {
                                         args.append(nextLine[i].toUpperCase());
                                     }
                                 }
                             }
                         }
 
                         log.debug("Current Args: {}", args.toString());
                         int chopIndex = args.indexOf("||", 0);
                         if (chopIndex == -1) {
                             chopIndex = args.length();
                         }
                         if (args.substring(0, chopIndex).endsWith("|")) {
                             chopIndex--;
                         }
                         lineMap.put("args", args.substring(0, chopIndex));
                     } else {
                         // Variable or static
                         lineMap.put("args", nextLine[3].toUpperCase());
                     }
 
                     lineMap.put("cmd", cmd);
                     lineMap.put("variable", nextLine[2]);
                     if (isGenerator) {
                         generators.add(lineMap);
                         genGroups.add(generators);
                         generators = new ArrayList<HashMap<String, String>>();
                     } else {
                         rules.add(lineMap);
                         generators.add(lineMap);
                     }
 //                } else if (cmd.equals("LINK")) {
 //                    String keyType = nextLine[2].trim().toUpperCase();
 //                    String keyValue = nextLine[3].trim();
 //                    String key = keyType + "_" + keyValue;
 //                    String domeType = nextLine[4].trim();
 //                    ArrayList<String> domeIds;
 //                    if (domeType.toUpperCase().equals("OVERLAY")) {
 //                        domeIds = getDomeIds(linkOvl, key);
 //                    } else if (domeType.toUpperCase().equals("STRATEGY")) {
 //                        domeIds = getDomeIds(linkStg, key);
 //                    } else {
 //                        log.error("Found invalid DOME type {} at line {}", domeType, ln);
 //                        continue;
 //                    }
 //                    for (int i = 5; i < nextLine.length; i++) {
 //                        if (nextLine[i].trim().startsWith("!")) {
 //                            break;
 //                        } else if (!nextLine[i].trim().equals("")) {
 //                            domeIds.add(nextLine[i].trim());
 //                        }
 //                    }
                 } else {
                     log.error("Found invalid command {} at line {}", cmd, ln);
                 }
             }
         }
         br.close();
        if (!generators.isEmpty()) {
            generators.add(new HashMap());
            genGroups.add(generators);
         }
        dome.put("generators", genGroups);
         dome.put("info", info);
         dome.put("rules", rules);
 //        dome.put("link_overlay", linkOvl);
 //        dome.put("link_stragty", linkStg);
     }
 
     public HashMap<String, Object> getDome() {
         return dome;
     }
 
 //    private ArrayList<String> getDomeIds(HashMap<String, ArrayList<String>> link, String key) {
 //        ArrayList<String> domeIds = link.get(key);
 //        if (domeIds == null) {
 //            domeIds = new ArrayList<String>();
 //            link.put(key, domeIds);
 //        }
 //        return domeIds;
 //    }
 }
