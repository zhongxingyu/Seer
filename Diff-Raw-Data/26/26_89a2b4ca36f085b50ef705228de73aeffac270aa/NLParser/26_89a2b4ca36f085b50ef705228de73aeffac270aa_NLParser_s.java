 /****************************************************************************************
  * Copyright (C) 2010,2011 The Hebrew University of Jerusalem, 
  * Department of Computer Science and Engineering
  * Project: GRR - Generating Random RDF
  * Author:	Daniel Blum, The Hebrew University of Jerusalem, 
  * Department of Computer Science and Engineering, http://www.cs.huji.ac.il/~danieb12/
  *
  * This file is part of GRR.
  * GRR is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * Please note that no part of this code is officially supported by HUJI
  * (The Hebrew University of Jerusalem) or any of its developers.
  *
  *  GRR is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with GRR (COPYRIGHT-gpl.txt).  If not, see <http://www.gnu.org/licenses/>.
  *
  ****************************************************************************************/
 
 package NaturalLanguageApi;
 
 import NaturalLanguageApi.ParsingUtils.*;
 import NaturalLanguageApi.RdfGenObjects.AliasNsParts.RdfAliasNsPair;
 import NaturalLanguageApi.RdfGenObjects.ConstructParts.*;
 import NaturalLanguageApi.RdfGenObjects.CreationParts.*;
 import NaturalLanguageApi.RdfGenObjects.QueryParts.*;
 import NaturalLanguageApi.RdfGenObjects.RdfAliasNsMappings;
 import NaturalLanguageApi.RdfGenObjects.RdfGraphCreator;
 import NaturalLanguageApi.RdfGenObjects.RdfSamplerFunction;
 import NaturalLanguageApi.RdfGenObjects.RdfTypePropertiesMappings;
 import NaturalLanguageApi.RdfGenObjects.SamplerFunctionParts.*;
 import NaturalLanguageApi.RdfGenObjects.TypePropParts.RdfProperty;
 import NaturalLanguageApi.RdfGenObjects.TypePropParts.RdfTypePropertiesPair;
 import RdfApi.RdfGenTypes;
 import SolutionConfig.Consts;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * The main class of the Natural-Language (Textual) interface
  */
 public class NLParser {
 
     /**
      * Consts
      */
 
     // textual token that could be found in a creation command
     private static String TOKEN_NAMESPACE = "namespace";
     private static String TOKEN_FOR = "for";
     private static String TOKEN_CREATE = "create";
     private static String TOKEN_CONNECT = "connect";
     private static String TOKEN_EACH = "each";
     private static String TOKEN_WITH = "with";
     private static String TOKEN_REPEATABLE = "repeatable";
     private static String TOKEN_LOCAL_DIST = "local-distinct";
     private static String TOKEN_GLOBAL_DIST = "global-distinct";
     private static String TOKEN_REPETITIONS = "repetitions";
 
     // General purpose constants
     private static String RDF_TYPE = "rdf:type";
     private static final double MAX_P = 100;
 
     // Locations and Schema
     private static String QUERY_DIRECTORY = "SparqlQueries";
     private static String QUERY_PREFIX_RDFS = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
     private static String QUERY_PREFIX_RDF = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
 
     // RDF header schema
     private static String RDF_GEN_HEADER = "<?xml version=\"1.0\"?> \n" +
             "<rdf:RDF \n" +
             "\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
             "\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
             "\txmlns:rdfGen=\"http://www.cs.huji.ac.il/~danieb12/RdfGeneratorSchema.rdf#\"> ";
     private static String RDF_GEN_FOOTER = "</rdf:RDF>";
 
     /**
      * Class Members
      */
     private RdfSamplerFunction _sFunction;
     private RdfGraphCreator _graphCreator;
     private RdfAliasNsMappings _nsMap;
     private RdfTypePropertiesMappings _typePropsMap;
 
     private String _inputPath;
     private int _lineIndex;
     private int _tokenIndex;
     private CyclicIntCounter _idCounter;
     private CyclicIntCounter _dynamicVarCounter;
     private CyclicIntCounter _queryCounter;
 
     /**
      * Private Constructor (Creation via the static create() method)
      * @param inputPath - The input file containing the construction commands
      */
     private NLParser(String inputPath) {
         _inputPath = inputPath;
         _lineIndex = 1;
         _tokenIndex = 0;
         _idCounter = new CyclicIntCounter();
         _dynamicVarCounter = new CyclicIntCounter();
         _queryCounter = new CyclicIntCounter();
     }
 
     /**
      * Static Constructor that deals with checking that the input-file exists ans that query folder is empty
      * @param inputPath - The input file containing the construction commands
      * @return - A NLParser instance
      */
     public static NLParser create(String inputPath) {
         // First check if the path exists
         File file = new File(inputPath);
         boolean exists = file.exists();
         if (!exists) {
             throw new IllegalArgumentException("Input path doesn't exist: " + inputPath);
         } else {
             // If a query directory exists we delete it
             File dir = new File(inputPath + QUERY_DIRECTORY);
             if (dir.exists()) {
                 boolean deleted = deleteDirectory(dir);
                 if (!deleted)
                     throw new IllegalStateException("Failed to delete the Sparql-Query directory located at: " + inputPath + QUERY_DIRECTORY);
             }
             // Create the directory
             if (!dir.mkdir())
                 throw new IllegalStateException("Failed to create the Sparql-Query directory at: " + inputPath + QUERY_DIRECTORY);
         }
         
         return new NLParser(inputPath);
 
     }
 
     /**
      * Private helper method for deleting a folder
      * @param path - The folder to be deleted
      * @return - True iff the folder was successfully deleted
      */
     static public boolean deleteDirectory(File path) {
         if (path.exists()) {
             File[] files = path.listFiles();
             for (File file : files) {
                 if (file.isDirectory()) {
                     deleteDirectory(file);
                 } else {
                     if (!file.delete())
                         return false;
                 }
             }
         }
         return (path.delete());
     }
 
     /**
      * Public Methods
      */
 
     /**
      * The main method that parses the different files and stores their input into the different members of this instnace
      * @param sFunctionFile - The path to the sampler-function file
      * @param typePropsFile - The path to the type-properties mapping file
      * @param nlGeneratorFile - The path to the textual (natural-language) input file
      * @throws IOException - In case of I/O problems while trying to open and read the different files
      */
     public void parseNLGeneratorFiles(String sFunctionFile, String typePropsFile, String nlGeneratorFile) throws IOException {
 
         // Read the path to the file that defines the mappings between rdf:type and dSampler
         RdfDictionarySampler[] dictionaryMappings = parseSamplerFunctionFile(sFunctionFile);
         _sFunction = new RdfSamplerFunction(dictionaryMappings);
 
         RdfTypePropertiesPair[] typePropertiesPair = parseTypePropsFile(typePropsFile);
         _typePropsMap = new RdfTypePropertiesMappings(typePropertiesPair);
 
         ConstructsNsMapPair constructNsMap = parseConstructs(nlGeneratorFile);
         RdfConstruct[] constructs = constructNsMap.getConstructs();
         _graphCreator = new RdfGraphCreator(constructs);
 
         RdfAliasNsPair[] aliasNsPairs = constructNsMap.getAliasNsPairs();
         _nsMap = new RdfAliasNsMappings(aliasNsPairs);
 
     }
 
     /**
      * The second main method (after parseNLGeneratorFiles) which generates the output according to the parsed data
      * @param outputFile - The path to the output file (which comes in addition to the actual updates done on the DB)
      * @throws IOException - In case of I/O problems while trying to open and write to the output file
      */
     public void generateRdfGeneratorFile(String outputFile) throws IOException {
 
         // First check if the file exists
         File file = new File(outputFile);
         if (file.exists())
             file.delete();
         file.createNewFile();
 
         // Writes the header part of the generated RDF file
         BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
         out.write(RDF_GEN_HEADER);
         out.newLine();
 
         // Add the Sampler-Function section
         out.write(_sFunction.toRdfString(1));
         out.newLine();
 
         // Add the Type-Properties section
         out.write(_typePropsMap.toRdfString(1));
         out.newLine();
 
         // Add the alias name-space mappings
         out.write(_nsMap.toRdfString(1));
         out.newLine();
 
         // Add the Graph-Creator section
         out.write(_graphCreator.toRdfString(1));
         out.newLine();
 
         // Close and add footer of the RDF file
         out.newLine();
         out.write(RDF_GEN_FOOTER);
         out.close();
     }
 
     /**
      * Private Methods
      */
 
     /**
      * A method for parsing the sampler-function file and create it in RDF format (RdfDictionarySampler[])
      * @param file - The path to the sampler-function input file
      * @return - sampler-function in RDF format (RdfDictionarySampler[])
      * @throws IOException - In any case of I/O problems while opening or reading the sampler-function file
      */
     private RdfDictionarySampler[] parseSamplerFunctionFile(String file) throws IOException {
 
         ArrayList<RdfDictionarySampler> dSamplerArray = new ArrayList<RdfDictionarySampler>();
 
         File inputFile = new File(file);
         if (!inputFile.exists())
             throw new IllegalArgumentException("The given input file doesn't exist: " + file);
 
         BufferedReader br = new BufferedReader(new FileReader(file));
         String line;
 
         // Go over all of the lines in the file
         while ((line = br.readLine()) != null) {
 
             // If the line is a comment (starts with // ) or empty line we skip it
             if (emptyOrCommentLine(line))
                 continue;
 
             RdfDictionarySampler dSampler = parseSamplerLine(line);
             dSamplerArray.add(dSampler);
 
         }
         br.close();
 
         RdfDictionarySampler[] dSamplers = new RdfDictionarySampler[dSamplerArray.size()];
         for (int i = 0; i < dSamplerArray.size(); i++)
             dSamplers[i] = dSamplerArray.get(i);
 
         return dSamplers;
     }
 
     /**
      * A method for parsing the type-properties file and create it in RDF format (RdfTypePropertiesPair[])
      * @param file - The path to the type-properties input file
      * @return - type-properties mappings in RDF format (RdfTypePropertiesPair[])
      * @throws IOException - In any case of I/O problems while opening or reading the sampler-function file
      */
     private RdfTypePropertiesPair[] parseTypePropsFile(String file) throws IOException {
 
         ArrayList<RdfTypePropertiesPair> pairsArray = new ArrayList<RdfTypePropertiesPair>();
 
         File inputFile = new File(file);
         if (!inputFile.exists())
             throw new IllegalArgumentException("The given input file doesn't exist: " + file);
 
         BufferedReader br = new BufferedReader(new FileReader(file));
         String line;
 
         // Go over all of the lines in the file
         while ((line = br.readLine()) != null) {
 
             // If the line is a comment (starts with // ) or empty line we skip it
             if (emptyOrCommentLine(line))
                 continue;
 
             RdfTypePropertiesPair pair = parseTypePropsLine(line);
             pairsArray.add(pair);
 
         }
         br.close();
 
         RdfTypePropertiesPair[] pairs = new RdfTypePropertiesPair[pairsArray.size()];
         for (int i = 0; i < pairsArray.size(); i++)
             pairs[i] = pairsArray.get(i);
 
         return pairs;
 
     }
 
     /**
      * Method for parsing a line which represents a single mapping in the sampler-function mappings
      * @param line - The parsed line
      * @return - An RdfDictionarySampler instance representing the parsed line
      */
     private RdfDictionarySampler parseSamplerLine(String line) {
         // First we'll replace spaces with one space
         String unTabbedLine = line.replaceAll("\\t+", "");
         String formattedLine = unTabbedLine.replaceAll("\\s+", "");
 
         // Now we split over space
         String[] lineParts = formattedLine.split(";");
         for(int i = 0; i < lineParts.length; i++){
             lineParts[i] = lineParts[i].trim();
         }
 
         if (lineParts[0].equals(RdfGenTypes.RES_RDF_TYPE_CUSTOM_DIC_SAMPLER)) {
             // We expect to have 2 more parts - total of 3
             if (lineParts.length != 3)
                 throw new IllegalArgumentException("Sampler-Function input file has an invalid line:" + line);
             return new RdfCustomRdfDictionarySampler(lineParts[1], lineParts[2]);
         } else if (lineParts[0].equals(RdfGenTypes.RES_RDF_TYPE_STD_DIC_SAMPLER)) {
             // We expect to have 3 more parts - total of 4
             if (lineParts.length != 4)
                 throw new IllegalArgumentException("Sampler-Function input file has an invalid line:" + line);
             RdfMode mode = RdfMode.valueOf(lineParts[1]);
             return new RdfStandardDictionarySampler(lineParts[2], mode, lineParts[3]);
         } else if (lineParts[0].equals(RdfGenTypes.RES_RDF_TYPE_CTR_DIC_SAMPLER)) {
             // We expect to have 3 more parts - total of 4
             if (lineParts.length != 4)
                 throw new IllegalArgumentException("Sampler-Function input file has an invalid line:" + line);
             RdfMode mode = RdfMode.valueOf(lineParts[1]);
             return new RdfCounterDictionarySampler(lineParts[2], mode, lineParts[3]);
         }else if (lineParts[0].equals(RdfGenTypes.RES_RDF_TYPE_EXT_DIC_SAMPLER)) {
             // We expect to have 1 more parts - total of 2
             if (lineParts.length != 2)
                 throw new IllegalArgumentException("Sampler-Function input file has an invalid line:" + line);
             return new RdfExternalDictionarySampler(lineParts[1]);
         }else if (lineParts[0].equals(RdfGenTypes.RES_RDF_TYPE_CONST_DIC_SAMPLER)) {
             // We expect to have 2 more parts - total of 3
             if (lineParts.length != 3)
                 throw new IllegalArgumentException("Sampler-Function input file has an invalid line:" + line);
             return new RdfConstDictionarySampler(lineParts[1], lineParts[2]);
         }else{
             throw new IllegalArgumentException("Sampler-Function input file has an invalid line:" + line);
         }
     }
 
     /**
      * A method for parsing line which represents a single mapping in the type-properties mappings
      * @param line - The parsed line
      * @return - An RdfTypePropertiesPair instance representing the parsed line
      */
     private RdfTypePropertiesPair parseTypePropsLine(String line) {
 
          // First we'll replace spaces with one space
         String unTabbedLine = line.replaceAll("\\t+", "");
         String formattedLine = unTabbedLine.replaceAll("\\s{1,}", "");
 
         // Now we split over space
         String[] lineParts = formattedLine.split(";");
          int length = lineParts.length;
          RdfProperty[] propArray = new RdfProperty[length-1];
          for (int i=1; i<length; i++)
             propArray[i-1] = new RdfProperty(lineParts[i]);
 
          return new RdfTypePropertiesPair(lineParts[0], propArray);         
     }
 
     /**
      * Returns true iff the given file is either empty or represents a comment (starts with // )
      * @param line - The analyzed line
      * @return True iff the given file is either empty or represents a comment (starts with // )
      */
     private boolean emptyOrCommentLine(String line) {
         Pattern emptyLine = Pattern.compile("^[\\s]*$");
         Matcher emptyMatcher = emptyLine.matcher(line);
         Pattern commentLine = Pattern.compile("^[\\s]*//.*");
         Matcher commentMatcher = commentLine.matcher(line);
         return emptyMatcher.matches() || commentMatcher.matches();
     }
 
     /**
      * A method for parsing the textual input file containing all of the construction commands
      * @param file - The parsed textual input file
      * @return - A pair holding both array of constructs and the alias-namespace mappings
      * @throws IOException - In any case of I/O problems while opening or reading the input file
      */
     private ConstructsNsMapPair parseConstructs(String file) throws IOException {
 
         // Check if file exists
         File inputFile = new File(file);
         if (!inputFile.exists())
             throw new IllegalArgumentException("The given input file doesn't exist: " + file);
 
         HashMap<String, String> nsMap = new HashMap<String, String>();
         ArrayList<RdfConstruct> constructArray = new ArrayList<RdfConstruct>();
 
         BufferedReader br = new BufferedReader(new FileReader(file));
         String line;
 
         // Go over all of the lines in the file
         while ((line = br.readLine()) != null) {
 
             // If the line is a comment (starts with // ) or empty line we skip it
             if (emptyOrCommentLine(line)) {
                 _lineIndex++;
                 continue;
             }
 
             // If this is a name-sapce definition we store it an continue to the next line
             if (nameSpaceDefLine(line, nsMap)) {
                 _lineIndex++;
                 continue;
             }
 
             // Assumption - each line is a construct command
             // Parse and add each line to the array
             RdfConstruct construct = parseConstructLine(line, nsMap);
             constructArray.add(construct);
 
         }
         br.close();
         _lineIndex = 0;
         _tokenIndex = 0;
 
         RdfConstruct[] constructs = new RdfConstruct[constructArray.size()];
         for (int i = 0; i < constructArray.size(); i++)
             constructs[i] = constructArray.get(i);
 
         RdfAliasNsPair[] aliasNsPairs = new RdfAliasNsPair[nsMap.size()];
         int index = 0;
         for (String key : nsMap.keySet())
         {
             String fullNs = nsMap.get(key);
             String ns = fullNs.substring(1, fullNs.length() - 1);
             aliasNsPairs[index] = new RdfAliasNsPair(key, ns);
             index++;
         }
 
         return new ConstructsNsMapPair(constructs, aliasNsPairs);
     }
 
     /**
      * A method for parsing a line representing a mapping of alias name and full-namespace
      * @param line - The parsed line
      * @param nsMap - A hash-map that will be updated with the parsed line
      * @return true iff the given line represents a name-space mapping
      */
     private boolean nameSpaceDefLine(String line, HashMap<String, String> nsMap) {
 
         // First we'll replace all multiple tabs and spaces with one space
         String noTabbedLine = line.replaceAll("\\t+", " ");
         String formattedLine = noTabbedLine.replaceAll("\\s{2,}", " ");
 
         // Now we split over space
         String[] lineParts = formattedLine.split(" ");
 
         if (lineParts[0].toLowerCase().equals(TOKEN_NAMESPACE)) {
 
             if (lineParts.length < 3)
                 throw new IllegalArgumentException("Syntax error (line: " + _lineIndex + ") - Namespace definition line isn't in the correct format: namespace 'alias': <'URI'>");
             if (lineParts.length > 3)
                 for (int i=2; i<(lineParts.length-1); i++)
                     lineParts[2] += " " + lineParts[i+1];
             if (!lineParts[1].endsWith(":"))
                 throw new IllegalArgumentException("Syntax error (line: " + _lineIndex + ") - Alias name isn't in the correct format of: 'alias':");
 
             if (lineParts[2].startsWith("<file:"))
             {
                 try { 
                     String[] pathParts = lineParts[2].split("file:///");
                     String path = pathParts[1].substring(0, pathParts[1].length()-2); //removing the #>
                     File file = new File(path);
                     String uri = file.toURI().toString();
                     String updatedPath = uri.substring(6,uri.length());
                     lineParts[2] = "<file:///" + updatedPath + "#>"; 
                 }
                 catch(Exception e) {
                     System.out.println("Failed to convert the local path of the schema file. Only windows is currently supported");
                     e.printStackTrace();
                 }
             }
 
             if (!lineParts[2].startsWith("<") && !lineParts[2].endsWith(">"))
                 throw new IllegalArgumentException("Syntax error (line: " + _lineIndex + ") - Uri part isn't in the correct format of: <'URI'>");
 
             nsMap.put(lineParts[1], lineParts[2]);
 
             return true;
         }
 
         return false;
     }
 
     /**
      * A method for parsing a construction command represented by the given line. General command format is:
      * ( FOR ( EACH | sampling-method )
      *       [ WITH (GLOBAL-DISTINCT| LOCAL-DISTINCT| REPEATABLE) ]
      *       { list of classes }
      *       [ WHERE { list of conditions} ]  )
      * [ CREATE i-j  { list of classes } ]
      * [ CONNECT  { list of connections }]
      * 
      * @param line - The parsed line containing the construction command
      * @param nsMap - alias to full-namespace mapping which is needed for resolving the different symbols in the command
      * @return - A RdfConstruct instance which represents the given construction command in RDF format
      * @throws IOException - In case of I/O problems while parsing some inner files (which store queries)
      */
     private RdfConstruct parseConstructLine(String line, HashMap<String, String> nsMap) throws IOException {
 
         // First we'll replace all multiple tabs and spaces with one space
         String noTabbedLine = line.replaceAll("\\t+", " ");
         String formattedLine = noTabbedLine.replaceAll("\\s{2,}", " ");
 
         // Now we split over space
         String[] lineParts = formattedLine.split(" ");
 
         if (lineParts == null || lineParts.length < 5)
             throw new IllegalArgumentException("Syntax error: (line " + _lineIndex + ")");
 
         // analyze the sentence
         RdfQuery[] queries = new RdfQuery[0];
         RdfMapPair[] mapPairs = new RdfMapPair[0];
         RdfPatternRepeatMode mode = null;
         RdfNode[] newNodes = new RdfNode[0];
         RdfNode[] oldNodes = new RdfNode[0];
         RdfCreationPattern cPattern;
 
         HashMap<String, String> classVarMap = new HashMap<String, String>();
         HashMap<String, String> varClassMap = new HashMap<String, String>();
         HashMap<String, String> queryAttributeVariableMap = new HashMap<String, String>();
         HashMap<String, Integer> classIdMap = new HashMap<String, Integer>();
         HashMap<String, Integer> varIdMap = new HashMap<String, Integer>(); 
 
         boolean validCommand = false;
 
         while (_tokenIndex < lineParts.length) {
             String token = lineParts[_tokenIndex].toLowerCase();
             _tokenIndex++;
             if (token.equals(TOKEN_FOR)) {
                 QueriesMapWrapper queriesMapWrapper = parseForSection(
                         lineParts,
                         nsMap,
                         classVarMap,
                         varClassMap,
                         classIdMap,
                         varIdMap,
                         queryAttributeVariableMap);
                 queries = queriesMapWrapper.getQueries();
                 mapPairs = queriesMapWrapper.getMapPair();
             }
             else if (token.equals(TOKEN_CREATE)) {
                 NewNodesModePair pair = parseCreate(
                         lineParts,
                         classIdMap,
                         classVarMap,
                         varIdMap
                 );
                 newNodes = pair.getNewNodes();
                 mode = pair.getMode();
                 validCommand = true;
             }
             else if (token.equals(TOKEN_CONNECT)) {
                 oldNodes = parseConnect(
                         lineParts,
                         classIdMap,
                         varIdMap,
                         varClassMap,
                         newNodes                           
                 );
                 validCommand = true;
             } else {
                 throw new IllegalArgumentException("Syntax error (line: " + _lineIndex + " at word/char number: " + _tokenIndex +
                         ") - Found: " + lineParts[_tokenIndex] + " while expecting: " + TOKEN_FOR + " | " + TOKEN_CREATE + " | " + TOKEN_CONNECT);
             }
         }
         if (!validCommand)
             throw new IllegalStateException("Command doesn't contain a valid 'Create' or 'Connect' section");
 
         if (mode==null)
             mode = new RdfRepeatConst(1);
 
         cPattern = new RdfCreationPattern(mode, oldNodes, newNodes);
         RdfQueryAttVarMap attVarMap = new RdfQueryAttVarMap(queryAttributeVariableMap);
         RdfConstruct construct = new RdfConstruct(queries, mapPairs, cPattern, attVarMap);
 
         _tokenIndex = 0; // End of creation line
 
         return construct;
     }
 
     /**
      * A method for parsing the 'for' section of a construction command
      * The large amount of mappings is due to the need to update queries in run-time while dealing with dynamic
      * queries on one hand while keeping a mapping of the construction pattern to actual nodes 
      * @param lineParts - Array holding the different parts of the parsed line
      * @param nsMap - alias to full-namespace mapping
      * @param classVarMap - A mapping of class types to variable names
      * @param varClassMap - A reverse mapping of classVarMap (variable names to class type)
      * @param classIdMap - A mapping of class type to node-id
      * @param varIdMap - A mapping from variables to node-id
      * @param queryAttributeVariableMap - A mapping of attributes to variables
      * @return - Returns QueriesMapWrapper instance which will be used in later stage
      * @throws IOException - In any case of I/O problems while trying to open, read or save files (query files)
      */
     private QueriesMapWrapper parseForSection(
             String[] lineParts,
             HashMap<String, String> nsMap,
             HashMap<String, String> classVarMap,
             HashMap<String, String> varClassMap,
             HashMap<String, Integer> classIdMap,
             HashMap<String, Integer> varIdMap,
             HashMap<String, String> queryAttributeVariableMap) throws IOException {
 
         ArrayList<RdfQuery> rdfQueriesArray = new ArrayList<RdfQuery>();
         ArrayList<RdfMapPair> mapPairArray = new ArrayList<RdfMapPair>();
 
         boolean nextPartIsFor;
         do {
             // Parse the query-result selection part
             RdfQueryResultSelection qResSelection = parseQueryResultSelection(lineParts);
 
             // If exists parse the repetition mode
             RdfMode mode;
             if (lineParts[_tokenIndex].equals(TOKEN_WITH)) {
                 _tokenIndex++;
                 mode = parseRdfMode(lineParts);
             } else
                 mode = RdfMode.LocalDistinctMode; // Default in case it wasn't defined.
 
             // Query section
             // Parse the Select part  (and update the mappings)
             ArrayList<String> selectArray = parseClassSet(lineParts);
 
             for (String classType : selectArray) {
                 int id = _idCounter.getNextValue();
                 String var;
                 if (classType.startsWith("?"))
                 {
                     String[] parts = classType.split("-");
                     var = parts[0].toLowerCase();
                 }
                 else {
                     var = "?Var" + id;
                 }
                 varIdMap.put(var, id);
                 classVarMap.put(classType, var);
                 varClassMap.put(var, classType);
                 mapPairArray.add(new RdfMapPair(id, var));
                 classIdMap.put(classType, id);
             }
 
             // Parse the where part if exists
             if (_tokenIndex >= lineParts.length)
                 throw new IllegalStateException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  No more tokens! Expecting the start of the 'select' set or create/connect part");
 
             ArrayList<RdfTriple> whereTriples = null;
             ArrayList<String> filters = new ArrayList<String>();
             if ((lineParts[_tokenIndex]).toLowerCase().equals("where"))
             {
                 _tokenIndex++;
                 if (lineParts[_tokenIndex].equals("{"))
                 {
                     // Parse all the triples and filters
                     whereTriples = parseWhereSet(lineParts, filters);
                 }
             }
 
 
             // Create and Store the query in a file
             QueryDynamicPair pair = buildQueryString(
                     selectArray,
                     whereTriples,
                     filters,
                     classVarMap,
                     varClassMap,
                     queryAttributeVariableMap,
                     nsMap);
             String query = pair.getQuery();
             String queryFilePath = _inputPath + QUERY_DIRECTORY  + Consts.pathSep + "SparqlQuery" + _queryCounter.getNextValue() + ".txt";
 
             // Write the query to the file
             createQueryFile(queryFilePath, query);
 
             // Decide if this is a dynamic query
             boolean isDynamic = pair.isDynamic();
 
             RdfQuery rdfQuery = new RdfQuery(mode, qResSelection, query, queryFilePath, isDynamic);
             rdfQueriesArray.add(rdfQuery);
 
             // Check if we have another query - by looking at the next token
             String token = lineParts[_tokenIndex].toLowerCase();
             if (token.equals(TOKEN_FOR)) {
                 _tokenIndex++;
                 nextPartIsFor = true;
             } else
                 nextPartIsFor = false;
         }
         while (nextPartIsFor);
 
         RdfQuery[] queries = new RdfQuery[rdfQueriesArray.size()];
         for (int i = 0; i < rdfQueriesArray.size(); i++)
             queries[i] = rdfQueriesArray.get(i);
 
         RdfMapPair[] mapPairs = new RdfMapPair[mapPairArray.size()];
         for (int j = 0; j < mapPairArray.size(); j++)
             mapPairs[j] = mapPairArray.get(j);
 
         return new QueriesMapWrapper(queries, mapPairs);
     }
   
     /**
      * A method for parsing the 'create' part in a construction command
      * @param lineParts - Array holding the different parts of the parsed line
      * @param classIdMap - A mapping of class type to node-id
      * @param classVarMap - A mapping of class types to variable names
      * @param varIdMap - A mapping from variables to node-id
      * @return - A NewNodesModePair instance holding all data for creating new nodes
      */
     private NewNodesModePair parseCreate(
             String[] lineParts,
             HashMap<String, Integer> classIdMap,
             HashMap<String, String> classVarMap,
             HashMap<String, Integer> varIdMap
     ) {
 
         ArrayList<RdfNode> nodes = new ArrayList<RdfNode>();
 
         // Parse the number of times the set would be created
         RdfPatternRepeatMode mode = parseRepeatMode(lineParts);
 
         // Parse the new nodes part
         ArrayList<String> newNodesArray = parseClassSet(lineParts);
 
         for (String classType : newNodesArray) {
             int id = _idCounter.getNextValue();
             classIdMap.put(classType, id);
             String type;
             String var;
             if (classType.startsWith("?"))
             {
                 String[] parts = classType.split("-");
                 var = parts[0].toLowerCase();
                 type = parts[1];
             }
             else
             {
                 type = classType;
                 var =  classVarMap.get(type);
             }
 
             varIdMap.put(var, id);
 
             RdfNode newNode = new RdfNode(RdfGenTypes.RES_RDF_TYPE_NEW_NODE, type, id, null);
             nodes.add(newNode);
         }
 
         RdfNode[] newNodes = new RdfNode[nodes.size()];
         for (int i = 0; i < nodes.size(); i++)
             newNodes[i] = nodes.get(i);
 
         return new NewNodesModePair(newNodes, mode);
 
     }
 
     /**
      * A method for parsing the 'connect' part in the construction command
      * @param lineParts - Array holding the different parts of the parsed line
      * @param classIdMap - A mapping of class type to node-id
      * @param varIdMap - A mapping from variables to node-id
      * @param varClassMap - A reverse mapping of classVarMap (variable names to class type)
      * @param newNodes - Array of RDFNode representing the newly created nodes
      * @return - Array of RDFNode representing the old but updated nodes 
      */
     private RdfNode[] parseConnect(
             String[] lineParts,
             HashMap<String, Integer> classIdMap, 
             HashMap<String, Integer> varIdMap,
             HashMap<String, String> varClassMap,
             RdfNode[] newNodes) {
 
         // Create a hashMap of id and rdf-node (on the new nodes)
         HashMap<Integer, RdfNode> idNewNodeMap = new HashMap<Integer, RdfNode>();
         for (RdfNode node : newNodes)
             idNewNodeMap.put(node.getId(), node);
 
         HashMap<Integer, RdfNode> idOldNodeMap = new HashMap<Integer, RdfNode>();
         HashMap<Integer, ArrayList<RdfEdge>> idEdgeArrayMap = new HashMap<Integer, ArrayList<RdfEdge>>();
 
         ArrayList<RdfNode> oldNodes = new ArrayList<RdfNode>();
 
         // Parse the edges part
         ArrayList<RdfTriple> edgeArray = parseTripleSet(lineParts);
 
         for (RdfTriple triple : edgeArray) {
             int id = _idCounter.getNextValue();
             String edgeType = triple.getClassTypeProperty();
             String targetType = triple.getClassTypeTarget();
             int pointsToId;
             if (targetType.startsWith("?"))
             {
                 String var = targetType.toLowerCase();
                 pointsToId = varIdMap.get(var);
             }
             else
                 pointsToId = classIdMap.get(targetType);
 
             // Check if this is a new node (otherwise create it in the old nodes)
             if (!idNewNodeMap.containsKey(pointsToId)) {
                 String type;
                 if (targetType.startsWith("?"))
                 {
                     String var = targetType.toLowerCase();
                     String fullType = varClassMap.get(var);
                     String[] parts = fullType.split("-");
                     type = parts[1];
                 }
                 else
                     type = targetType;
                 RdfNode node = new RdfNode(RdfGenTypes.RES_RDF_TYPE_OLD_NODE, type, pointsToId, null);
                 oldNodes.add(node);
                 idOldNodeMap.put(pointsToId, node);
             }
 
             String sourceType = triple.getClassTypeSource();
             int sourceId;
             if (sourceType.startsWith("?"))
             {
                 String var = sourceType.toLowerCase();
                 sourceId = varIdMap.get(var);
             }
             else
                    sourceId = classIdMap.get(sourceType);
 
             if (!idNewNodeMap.containsKey(sourceId)) {
                 String type;
                 if (sourceType.startsWith("?"))
                 {
                     String var = sourceType.toLowerCase();
                     String fullType = varClassMap.get(var);
                     String[] parts = fullType.split("-");
                     type = parts[1];
                 }
                 else
                     type = targetType;
 
                 RdfNode node = new RdfNode(RdfGenTypes.RES_RDF_TYPE_OLD_NODE, type, sourceId, null);
                 oldNodes.add(node);
                 idOldNodeMap.put(sourceId, node);
             }
             RdfEdge edge = new RdfEdge(id, edgeType, pointsToId);
             if (!idEdgeArrayMap.containsKey(sourceId)) {
                 ArrayList<RdfEdge> edges = new ArrayList<RdfEdge>();
                 edges.add(edge);
                 idEdgeArrayMap.put(sourceId, edges);
             } else {
                 ArrayList<RdfEdge> edges = idEdgeArrayMap.get(sourceId);
                 edges.add(edge);
             }
 
         }
 
         for (int id : idEdgeArrayMap.keySet()) {
             if (idNewNodeMap.containsKey(id)) {
                 RdfNode node = idNewNodeMap.get(id);
                 ArrayList<RdfEdge> edges = idEdgeArrayMap.get(id);
                 RdfEdge[] edgeArr = new RdfEdge[edges.size()];
                 for (int i = 0; i < edgeArr.length; i++)
                     edgeArr[i] = edges.get(i);
                 node.setEdges(edgeArr);
             }
             else if (idOldNodeMap.containsKey(id)) {
                 RdfNode node = idOldNodeMap.get(id);
                 ArrayList<RdfEdge> edges = idEdgeArrayMap.get(id);
                 RdfEdge[] edgeArr = new RdfEdge[edges.size()];
                 for (int i = 0; i < edgeArr.length; i++)
                     edgeArr[i] = edges.get(i);
                 node.setEdges(edgeArr);
             }
             else
                 throw new IllegalStateException("Oops - Internal Error: id wasn't found - check code!");
         }
 
         RdfNode[] oldNodesArr = new RdfNode[oldNodes.size()];
         for (int i = 0; i < oldNodesArr.length; i++)
             oldNodesArr[i] = oldNodes.get(i);
 
         return oldNodesArr;
 
     }
 
     /**
      * A method for creating a query string based on the given parameters
      * @param selectArray - Array of strings representing the 'select' part of the query
      * @param whereTriples - Array of RDFTriple representing the 'where' part of the query
      * @param filters - String array representing the Filters part (if exists)
      * @param classVarMap - A mapping of class types to variable names
      * @param varClassMap - A reverse mapping of classVarMap (variable names to class type)
      * @param queryAttributeVariableMap - A mapping of attributes to variables
      * @param nsMap - alias to full-namespace mapping
      * @return - A QueryDynamicPair instance representing a pair of query in string format and a boolean indicating if
      * this is a dynamic query 
      */
     private QueryDynamicPair buildQueryString(
             ArrayList<String> selectArray,
             ArrayList<RdfTriple> whereTriples,
             ArrayList<String> filters,
             HashMap<String, String> classVarMap,
             HashMap<String, String> varClassMap,
             HashMap<String, String> queryAttributeVariableMap,
             HashMap<String, String> nsMap) {
 
         boolean isDynamic = false;
         String query = "";
         query += QUERY_PREFIX_RDF;
         query += QUERY_PREFIX_RDFS;
 
         // For each namespace that was parsed we add it to the query
         for (String ns : nsMap.keySet()) {
             query += "PREFIX " + ns + " " + nsMap.get(ns) + " \n";
         }
 
         // Add the select part
         query += "SELECT ";
         for (String classType : selectArray)
         {

             query += classVarMap.get(classType) + " ";
         }
         query += "\n";
 
         // Add the where part
         ArrayList<String> usedClasses = new ArrayList<String>();
         query += "WHERE { ";
         // For each of the classes in the select we should add the rdf:type constraint
         for (String classType : selectArray) {
             String var = classVarMap.get(classType);
             String type;
             if (classType.startsWith("?")) {
                 String[] parts = classType.split("-");
                 type = parts[1];
             } else
                 type = classType;
             if (!usedClasses.contains(type)) {
                 query += var + " " + RDF_TYPE + " " + type + " . ";
                 usedClasses.add(type);
             }
         }
 
         // Add the where part defined by the user
         if (whereTriples != null) {
             for (RdfTriple triple : whereTriples) {
 
                 // The source class
                 String sourceClass = triple.getClassTypeSource();
                 String sourceVar;
                 if (sourceClass.startsWith("?")) // We have here a user defined variable
                     sourceClass = varClassMap.get(sourceClass);
                 if (selectArray.contains(sourceClass))
                     sourceVar = classVarMap.get(sourceClass);
                 else if (classVarMap.containsKey(sourceClass)) {
                     String dynamicVal = classVarMap.get(sourceClass);
                     // dynamic variable found
                     isDynamic = true;
                     sourceVar = "@var" + _dynamicVarCounter.getNextValue();
                     queryAttributeVariableMap.put(dynamicVal, sourceVar);
                 } else {
                     throw new IllegalStateException("Failed to create query - The attribute: " + sourceClass + ", wasn't found in the select set nor in previous queries select section!");
                 }
                 if (!usedClasses.contains(sourceClass)) {
                     String type;
                     if (sourceClass.startsWith("?")) {
                         String[] parts = sourceClass.split("-");
                         type = parts[1];
                     } else
                         type = sourceClass;
                     query += sourceVar + " " + RDF_TYPE + " " + type + " . ";
                     usedClasses.add(sourceClass);
                 }
 
                 // The target class
                 String targetClass = triple.getClassTypeTarget();
                 String targetVar;
                 if (targetClass.startsWith("?")) // We have here a user defined variable
                     targetClass = varClassMap.get(targetClass);
                 if (selectArray.contains(targetClass))
                     targetVar = classVarMap.get(targetClass);
                 else if (classVarMap.containsKey(targetClass)) {
                     String dynamicVal = classVarMap.get(targetClass);
                     // dynamic variable found
                     isDynamic = true;
                     targetVar = "@var" + _dynamicVarCounter.getNextValue();
                     queryAttributeVariableMap.put(dynamicVal, targetVar);
                 } else {
                     throw new IllegalStateException("Failed to create query - The attribute: " + targetClass + ", wasn't found in the select set nor in previous queries select section!");
                 }
                 if (!usedClasses.contains(targetClass)) {
                     String type;
                     if (targetClass.startsWith("?")) {
                         String[] parts = targetClass.split("-");
                         type = parts[1];
                     } else
                         type = targetClass;
                     query += targetVar + " " + RDF_TYPE + " " + type + " . ";
                     usedClasses.add(targetClass);
                 }
 
                 // The actual triple
                 String edge = triple.getClassTypeProperty();
                 query += sourceVar + " " + edge + " " + targetVar + " . ";
             }
         }
 
         // Add the filters part
         for (String filter : filters)
         {
             // We need to check if the filter has dynamic variables or not
             String[] fParts = filter.split(" ");
             String processedFilter = "";
             for (String token : fParts)
             {
                 if (!token.startsWith("?"))
                 {
                     processedFilter += token + " ";
                     continue;
                 }
                 String classType = varClassMap.get(token);
                 String var;
                 if (selectArray.contains(classType))
                     var = classVarMap.get(classType);
                 else if (classVarMap.containsKey(classType)) {
                     String dynamicVal = classVarMap.get(classType);
                     // dynamic variable found
                     isDynamic = true;
                     var = "@var" + _dynamicVarCounter.getNextValue();
                     queryAttributeVariableMap.put(dynamicVal, var);
                 } else {
                     throw new IllegalStateException("Failed to create query - The attribute: " + classType + ", wasn't found in the select set nor in previous queries select section!");
                 }
                 processedFilter += var + " ";
             }
             query += processedFilter + " . ";
         }
 
         query += " } \n";
 
         return new QueryDynamicPair(query, isDynamic);
     }
 
     /**
      * A method for parsing the query result selection (by using
      * @param lineParts - Array holding the different parts of the parsed line
      * @return - A RdfQueryResultSelection which will be a specific instance according to the given line parts
      */
     private RdfQueryResultSelection parseQueryResultSelection(String[] lineParts) {
 
         if (_tokenIndex >= lineParts.length)
             throw new IllegalStateException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  No more tokens! Expecting (each | min-max | pmin-pmax | num)");
         String token = lineParts[_tokenIndex].toLowerCase();
         _tokenIndex++;
 
         // regex for parsing
         Pattern numRange = Pattern.compile("^[\\d]+-[\\d]+$");
         Matcher numRangeMatcher = numRange.matcher(token);
         Pattern pRange = Pattern.compile("^[\\d]+%-[\\d]+%$");
         Matcher pRangeMatcher = pRange.matcher(token);
         Pattern num = Pattern.compile("^[\\d]+");
         Matcher numMatcher = num.matcher(token);
 
         if (token.equals(TOKEN_EACH))
             return new RdfSelectAll();
         else if (numRangeMatcher.matches()) {
             String[] numRangeParts = token.split("-");
             int min = Integer.parseInt(numRangeParts[0]);
             if (min < 0)
                 throw new IllegalArgumentException("Illegal minimum value: " + min);
             int max = Integer.parseInt(numRangeParts[1]);
             if (min > max)
                 throw new IllegalArgumentException("Illegal range values min=" + min + " max=" + max);
             return new RdfSelectNumberRange(min, max);
         } else if (pRangeMatcher.matches()) {
             String[] pRangeParts = token.split("-");
             double min = (double)Integer.parseInt(pRangeParts[0].substring(0,pRangeParts[0].length()-1))/MAX_P;
             if (min < 0)
                 throw new IllegalArgumentException("Illegal minimum value: " + min);
             double max = (double)Integer.parseInt(pRangeParts[1].substring(0,pRangeParts[1].length()-1))/MAX_P;
             if (min > max)
                 throw new IllegalArgumentException("Illegal range values min=" + min + " max=" + max);
             return new RdfSelectRange(min, max);
         } else if (numMatcher.matches()) {
             int number = Integer.parseInt(numMatcher.group(0));
             if (number < 0)
                 throw new IllegalArgumentException("Illegal const value: " + number);
             return new RdfSelectConst(number);
         } else {
             throw new IllegalArgumentException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + (_tokenIndex - 1) + ") -  Found an unsupported format for query result selection mode:" + token);
         }
     }
 
     /**
      * A method for paring mode used (RDFMode)
      * @param lineParts - Array holding the different parts of the parsed line
      * @return - A RDFMode instance based on the given input
      */
     private RdfMode parseRdfMode(String[] lineParts) {
         if (_tokenIndex >= lineParts.length)
             throw new IllegalStateException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  No more tokens! Expecting  ( " + TOKEN_REPEATABLE + " | " + TOKEN_GLOBAL_DIST + " | " + TOKEN_LOCAL_DIST + " )");
         String token = lineParts[_tokenIndex].toLowerCase();
 
         RdfMode mode;
 
         if (token.equals(TOKEN_REPEATABLE))
             mode = RdfMode.RepeatableMode;
         else if (token.equals(TOKEN_GLOBAL_DIST))
             mode = RdfMode.GlobalDistinctMode;
         else if (token.equals(TOKEN_LOCAL_DIST))
             mode = RdfMode.LocalDistinctMode;
         else
             throw new IllegalArgumentException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  Invalid mode value found: " + token + " while expecting: (" + TOKEN_REPEATABLE + " | " + TOKEN_GLOBAL_DIST + " | " + TOKEN_LOCAL_DIST + ")");
 
         _tokenIndex++;
         // Parse the repetitions word
         token = lineParts[_tokenIndex].toLowerCase();
         if (!token.equals(TOKEN_REPETITIONS))
             throw new IllegalArgumentException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  expecting the work 'repetitions'");
         _tokenIndex++;
 
         return mode;
     }
 
     /**
      * A method for parsing the class set
      * @param lineParts - Array holding the different parts of the parsed line
      * @return - An array list in string format of the parsed class types found in this set
      */
     private ArrayList<String> parseClassSet(String[] lineParts) {
 
         if (_tokenIndex >= lineParts.length)
             throw new IllegalStateException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  No more tokens! Expecting the start of the set");
 
         if (!lineParts[_tokenIndex].equals("{"))
             throw new IllegalStateException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  Expecting: '{' found: " + lineParts[_tokenIndex]);
         else
             _tokenIndex++;
 
         ArrayList<String> classArray = new ArrayList<String>();
 
         // Parse the set until we reach '}'
         boolean setNotFinished = true;
         do {
             if (_tokenIndex > lineParts.length)
                 throw new IllegalArgumentException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  No more tokens! expected a class type!");
             String token = lineParts[_tokenIndex];
             _tokenIndex++;
             // Next token should either be "," or "}"
             if (_tokenIndex > lineParts.length)
                 throw new IllegalArgumentException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  Expecting: '}' or ',' or '?<var-name>' , reached the end of the line!");
 
             String nextToken = lineParts[_tokenIndex].toLowerCase();
 
             if (nextToken.startsWith("?"))
             {
                 token = nextToken + "-" + token;
                 _tokenIndex++;
                 nextToken = lineParts[_tokenIndex].toLowerCase();
             }
 
             if (nextToken.equals("}"))
             {
                 classArray.add(token);
                 setNotFinished = false;
             }
             else if (nextToken.equals(","))
             {
                 classArray.add(token);
             }
             else
                 throw new IllegalArgumentException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  Expecting: ','  found: " + token);
             
             _tokenIndex++;
         }
         while (setNotFinished);
 
         return classArray;
     }
 
     /**
      * A method for parsing triple-sets (e.g. in the 'where' part)
      * @param lineParts - Array holding the different parts of the parsed line
      * @return - An array list of RdfTriple holding the parsed triples in this set
      */
     private ArrayList<RdfTriple> parseTripleSet(String[] lineParts) {
 
         if (_tokenIndex >= lineParts.length)
             throw new IllegalStateException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  No more tokens! Expecting the start of the triple set");
 
         if (!lineParts[_tokenIndex].equals("{"))
             throw new IllegalStateException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  Expecting: '{' found: " + lineParts[_tokenIndex]);
         else
             _tokenIndex++;
 
         ArrayList<RdfTriple> tripleArrayList = new ArrayList<RdfTriple>();
 
         // Parse the set until we reach '}'
         boolean setNotFinished = true;
         do {
             if (_tokenIndex + 2 > lineParts.length)
                 throw new IllegalArgumentException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  No more tokens! expected a set of triples!");
             String token1 = lineParts[_tokenIndex];
             if (token1.startsWith("?"))
                 token1 = token1.toLowerCase();
             _tokenIndex++;
 
             String token2 = lineParts[_tokenIndex];
             _tokenIndex++;
 
             String token3 = lineParts[_tokenIndex];
             if (token3.startsWith("?"))
                 token3 = token3.toLowerCase();
             _tokenIndex++;
             
             tripleArrayList.add(new RdfTriple(token1, token2, token3));
 
             // Next token should either be "," or "}"
             if (_tokenIndex > lineParts.length)
                 throw new IllegalArgumentException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  Expecting: '}' , reached the end of the line!");
             String token = lineParts[_tokenIndex].toLowerCase();
             if (token.equals("}"))
                 setNotFinished = false;
             else if (!token.equals(","))
                 throw new IllegalArgumentException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  Expecting: ','  found: " + token);
 
             _tokenIndex++;
         }
         while (setNotFinished);
 
         return tripleArrayList;
 
     }
 
     /**
      * A method for parsing the 'where' set
      * @param lineParts - Array holding the different parts of the parsed line
      * @param filters - array list of filters
      * @return - An array list of RdfTriples representing the where set
      */
     private ArrayList<RdfTriple> parseWhereSet(String[] lineParts, ArrayList<String> filters) {
 
             if (_tokenIndex >= lineParts.length)
                 throw new IllegalStateException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  No more tokens! Expecting the start of the triple set");
 
             if (!lineParts[_tokenIndex].equals("{"))
                 throw new IllegalStateException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  Expecting: '{' found: " + lineParts[_tokenIndex]);
             else
                 _tokenIndex++;
 
             ArrayList<RdfTriple> tripleArrayList = new ArrayList<RdfTriple>();
 
             // Parse the set until we reach '}'
             boolean setNotFinished = true;
             do {
                 if (_tokenIndex + 2 > lineParts.length)
                     throw new IllegalArgumentException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  No more tokens! expected a set of triples!");
                 String token1 = lineParts[_tokenIndex];
 
                 // If this is a filter
                 if (token1.equals("FILTER("))
                 {
                     //Parse the filter part until we reach ')'
                     String filter = "" + token1;
                     String token;
                     boolean filterHasMore;
                     do {
                         _tokenIndex++;
                         token  = lineParts[_tokenIndex];
                         filter +=  " " + token;
                         filterHasMore = !token.equals(")");
                     }
                     while (filterHasMore && _tokenIndex < lineParts.length);
                     if (filterHasMore)
                         throw new IllegalArgumentException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  FILTER part wasn't closed by a ')' ");
                     _tokenIndex++;
                     filters.add(filter);
                 }
                 // Parse a regular triple
                 else {
 
                     if (token1.startsWith("?"))
                         token1 = token1.toLowerCase();
                     _tokenIndex++;
 
                     String token2 = lineParts[_tokenIndex];
                     _tokenIndex++;
 
                     String token3 = lineParts[_tokenIndex];
                     if (token3.startsWith("?"))
                         token3 = token3.toLowerCase();
                     _tokenIndex++;
 
                     tripleArrayList.add(new RdfTriple(token1, token2, token3));
                 }
 
                 // Next token should either be "," or "}"
                 if (_tokenIndex > lineParts.length)
                     throw new IllegalArgumentException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  Expecting: '}' , reached the end of the line!");
                 String token = lineParts[_tokenIndex].toLowerCase();
                 if (token.equals("}"))
                     setNotFinished = false;
                 else if (!token.equals(","))
                     throw new IllegalArgumentException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  Expecting: ','  found: " + token);
 
                 _tokenIndex++;
             }
             while (setNotFinished);
 
             return tripleArrayList;
 
         }
 
     /**
      * A method for storing the given strin gquery into a file at the given fileName location
      * @param fileName - The output file that will store the query
      * @param query - The string query that will be stored
      * @throws IOException - In any case of I/O problems while opening or saving the query into the file
      */
     private static void createQueryFile(String fileName, String query) throws IOException {
         File file = new File(fileName);
         if (file.exists())
             file.delete();
         file.createNewFile();
         BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
         out.write(query);
         out.close();
     }
 
     /**
      * A method for parsing the repeat-mode
      * @param lineParts - Array holding the different parts of the parsed line
      * @return - A RdfPatternRepeatMode instance based on the given line parts
      */
     private RdfPatternRepeatMode parseRepeatMode(String[] lineParts) {
 
         if (_tokenIndex >= lineParts.length)
             throw new IllegalStateException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  No more tokens! Expecting (each | min-max |  num)");
         String token = lineParts[_tokenIndex].toLowerCase();
         _tokenIndex++;
 
         // regex for parsing
         Pattern numRange = Pattern.compile("^[\\d]+-[\\d]+$");
         Matcher numRangeMatcher = numRange.matcher(token);
         Pattern num = Pattern.compile("^[\\d]+");
         Matcher numMatcher = num.matcher(token);
 
         if (numRangeMatcher.matches()) {
             String[] rangeParts = token.split("-");
             int min = Integer.parseInt(rangeParts[0]);
             if (min < 0)
                 throw new IllegalArgumentException("Illegal minimum value: " + min);
             int max = Integer.parseInt(rangeParts[1]);
             if (min > max)
                 throw new IllegalArgumentException("Illegal range values min=" + min + " max=" + max);
             return new RdfRepeatRange(min, max);
         } else if (numMatcher.matches()) {
             int number = Integer.parseInt(numMatcher.group(0));
             if (number < 0)
                 throw new IllegalArgumentException("Illegal const value: " + number);
             return new RdfRepeatConst(number);
         } else {
             throw new IllegalArgumentException("Syntax Error:  (line: " + _lineIndex + " at word/char number: " + _tokenIndex + ") -  Found an unsupported format of pattern repeat mode:" + token);
         }
     }
 
 }
