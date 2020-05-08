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
 
 package RdfApi;
 
 import JavaApi.RandomDataGraph.GraphBuildingBlocks.Edge;
 import JavaApi.RandomDataGraph.GraphBuildingBlocks.Node;
 import JavaApi.RandomDataGraph.Matchers.IMatcher;
 import JavaApi.RandomDataGraph.Matchers.StdMatcher;
 import JavaApi.Samplers.DictionarySamplers.*;
 import JavaApi.Samplers.NumberSamplers.NaturalNumberSamplers.ConstantNumberSampler;
 import JavaApi.Samplers.NumberSamplers.NaturalNumberSamplers.INaturalNumberSampler;
 import JavaApi.Samplers.NumberSamplers.NaturalNumberSamplers.StdNaturalNumberSampler;
 import JavaApi.Samplers.NumberSamplers.RealNumberSamplers.ConstantRealSampler;
 import JavaApi.Samplers.NumberSamplers.RealNumberSamplers.IRealNumberSampler;
 import JavaApi.Samplers.NumberSamplers.RealNumberSamplers.RangeRealNumberSampler;
 import JavaApi.Samplers.QuerySamplers.QueryWrapper;
 import JavaApi.Samplers.SamplerFunctions.SamplerFunction;
 import JavaApi.Samplers.SamplerFunctions.TypeDSamplerPair;
 import JavaApi.Samplers.SamplerFunctions.TypePropertiesFunction;
 import JavaApi.Samplers.SamplerFunctions.TypePropsPair;
 import JavaApi.Samplers.SamplingMode;
 import com.hp.hpl.jena.rdf.model.*;
 import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
 import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
 import com.hp.hpl.jena.util.FileManager;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * A class for parsing an RDF based GRR input file. The class can output all of the relevant objects in order to use the
  * GRR Java layer for generating random RDF data
  */
 public class RdfParser {
 
     /**
      * Consts - Paths
      */
     private static final String PARSING_SCHEMA_FILE = "RdfGeneratorSchema.rdf";
     private static final String PARSING_SCHEMA_FILE_LOCAL_BASE = "./src/RdfApi/";
     private static final String PARSING_SCHEMA_FILE_WEB_BASE = "http://www.cs.huji.ac.il/~danieb12/";
     private static final String PARSING_SCHEMA_FILE_LOCAL_PATH = PARSING_SCHEMA_FILE_LOCAL_BASE + PARSING_SCHEMA_FILE;
     private static final String PARSING_SCHEMA_FILE_WEB_PATH = PARSING_SCHEMA_FILE_WEB_BASE + PARSING_SCHEMA_FILE;
     private static final String RDF_GEN_NS = PARSING_SCHEMA_FILE_WEB_PATH + "#";
     
     /**
      * Class Members
      */
     private SamplerFunction _sFunction;
     private TypePropertiesFunction _typePropsMap;
     private HashMap<String, String> _aliasNsMap;
     private ArrayList<ConstructionWrapper> _constructionWrapperList;
 
     /**
      * Constructor - Only initializes the array-list of construction-wrappers
      */
     public RdfParser() {
         _constructionWrapperList = new ArrayList<ConstructionWrapper>();
     }
 
     /**
      * Public Methods
      */
 
     /**
      * Returns the ConstructionWrapper array-list that was parsed from the given RDF input file
      * @return - The ConstructionWrapper array-list
      */
     public ArrayList<ConstructionWrapper> getConstructionWrapperList() {
         return _constructionWrapperList;
     }
 
     /**
      * Returns the sampler-function that was parsed from the given RDF input file
      * @return - The sampler-function that was parsed from the given RDF input file
      */
     public SamplerFunction getSamplerFunction() {
         return _sFunction;
     }
 
     /**
      * Returns a mapping of alias names to full-namespace hash-map that was parsed from the given RDF input file
      * @return - A mapping of alias names to full-namespace hash-map that was parsed from the given RDF input file
      */
     public HashMap<String, String> getAliasNsMap() {
         return _aliasNsMap;
     }
 
     /**
      * Returns the type-property mappings that was parsed from the given RDF input file
      * @return - The type-property mappings that was parsed from the given RDF input file
      */
     public TypePropertiesFunction getTypePropsFunction() {
         return _typePropsMap;
     }
 
     /**
      * Main method that parses the given RDF input file
      * @param file - A full path name + file name to the RDF input file
      */
     public void parseRdfGeneratorFile(String file) {
 
         // Create the model and schema that will be used in order to work on the RDF input file
         Model model = FileManager.get().loadModel(file);
         Model schema = FileManager.get().loadModel(PARSING_SCHEMA_FILE_LOCAL_PATH);
         InfModel infModel = ModelFactory.createRDFSModel(schema, model);
         infModel.setNsPrefix("rdfGen", RDF_GEN_NS);
         model.setNsPrefix("rdfGen", RDF_GEN_NS);
 
         // Parse the  sampler-function
         ResIterator sampIterator = model.listResourcesWithProperty(RDF.type, new ResourceImpl(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_SAMPLER_FUNCTION));
         if (!sampIterator.hasNext())
             throw new IllegalArgumentException("File doesn't contain a Sampler-Function!");
         _sFunction = parseSamplerFunction(sampIterator.nextResource());
 
         // Parse the  type properties mappings
         ResIterator tPropIterator = model.listResourcesWithProperty(RDF.type, new ResourceImpl(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_TYPE_PROPERTIES_MAP));
         if (!sampIterator.hasNext())
         {
             _typePropsMap = new TypePropertiesFunction();
             _typePropsMap.addMap(new HashMap<String, String[]>());
         }
         _typePropsMap = parseTypePropsFunction(tPropIterator.nextResource());
 
         // Parse the alias name-sapce mappings
         ResIterator nsMapIterator = model.listResourcesWithProperty(RDF.type, new ResourceImpl(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_NS_MAPPINGS));
         if (!nsMapIterator.hasNext())
             throw new IllegalArgumentException("File doesn't contain a Name-Space mappings!");
         _aliasNsMap = parseNsMap(nsMapIterator.nextResource());
 
         // Get the base node - GraphCreator
         ResIterator graphIterator = model.listResourcesWithProperty(RDF.type, new ResourceImpl(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_GRAPH_CREATOR));
         if (!graphIterator.hasNext())
             throw new IllegalArgumentException("File doesn't contain a Graph-Creator node!");
         Resource graphCreator = graphIterator.nextResource();
 
         // For-Each construct invoke parse-construct
         Seq seq = graphCreator.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_CONSTRUCT_LIST)).getSeq();
         NodeIterator seqIter = seq.iterator();
         int index = 0;
         while (seqIter.hasNext()) {
             RDFNode node = seqIter.nextNode();
             Resource construct = node.as((new ResourceImpl(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_CONSTRUCT)).getClass());
             _constructionWrapperList.add(index, parseConstruct(construct));
             index++;
         }
     }
 
     /**
      * Private Methods
      */
 
     /**
      * A method for parsing the sampler-function from the given RDF input file
      * @param samplerResource - The resource instance that represents the sampler-function that needs to be de-serialized
      * into sampler-function object
      * @return - A SamplerFunction instance based on the given resource parsed from the RDF input file
      */
     private SamplerFunction parseSamplerFunction(Resource samplerResource) {
 
         SamplerFunction sFunction = new SamplerFunction();
 
         // Parse Mappings
         Bag bag = samplerResource.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_TYPE_DIC_MAP)).getBag();
         NodeIterator bagIter = bag.iterator();
         // For each mapping
         while (bagIter.hasNext()) {
             TypeDSamplerPair pair;
             try {
                 pair = parseSamplerMapping(bagIter.nextNode().as(ResourceImpl.class));
             } catch (IOException e) {
                 throw new IllegalArgumentException("Problems in loading the data files for the d-samplers!");
             }
             sFunction.addValue(pair.getType(), pair.getDSampler());
         }
 
         return sFunction;
     }
 
     /**
      * Method for parsing the type-property mapping function which should be represented by the given resource
      * @param resource - The resource instance that represents the type-property mapping function that needs to be
      * de-serialized into TypePropertiesFunction object   
      * @return - A TypePropertiesFunction instance based on the given resource parsed from the RDF input file
      */
     private TypePropertiesFunction parseTypePropsFunction(Resource resource) {
         TypePropertiesFunction tPropFunction = new TypePropertiesFunction();
 
         // Parse the mappings
         Bag bag = resource.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_TYPE_TYPE_PROPS_MAPPINGS)).getBag();
         NodeIterator bagIter = bag.iterator();
         // For each mapping
         while (bagIter.hasNext()) {
             TypePropsPair pair;
             pair = parseTypePropsMapping(bagIter.nextNode().as(ResourceImpl.class));
             tPropFunction.addValue(pair.getType(), pair.getProperties());
 
         }
 
         return tPropFunction;
     }
 
     /**
      * A method for parsing a specific type-properties mapping in RDF resource format while returning a TypePropsPair
      * which contains the rdf-type and an array of properties of rdf-type 
      * @param resource - The specific mapping to be parsed
      * @return - A TypePropsPair which contains the rdf-type and an array of properties of rdf-type
      */
     private TypePropsPair parseTypePropsMapping(ResourceImpl resource) {
 
         // Get the rdf-type
         Literal typeLit = (Literal) resource.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_TYPE)).getObject();
         String type = typeLit.getString();
 
         // Parse the array of properties associated with thie rdf-type
         ArrayList<String> propArray = new ArrayList<String>();
         // Parse the mappings
         Bag bag = resource.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_PROP_LIST)).getBag();
         NodeIterator bagIter = bag.iterator();
         // For each mapping
         while (bagIter.hasNext()) {
             Resource prop = bagIter.nextNode().as(ResourceImpl.class);
             Literal propLit = (Literal) prop.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_TYPE)).getObject();
             String propType = propLit.getString();
             propArray.add(propType);
         }
 
         String[] props = new String[propArray.size()];
         for (int i=0; i<props.length; i++)
             props[i] = propArray.get(i);
 
         // Return the pair 
         return new TypePropsPair(type, props);
     }
 
     /**
      * A method that parses the RDF resource representing the mapping of alias-names to full-namespace and returns a
      * hash-map with this mapping
      * @param aliasNsMappings - The RDF resource representing the mapping of alias-names to full-namespace
      * @return - A hash-map with this mapping
      */
     private HashMap<String, String> parseNsMap(Resource aliasNsMappings) {
 
         HashMap<String, String> nsMap = new HashMap<String, String>();
 
         // Parse Mappings
         Bag bag = aliasNsMappings.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_TYPE_ALIAS_NS_MAPPINGS)).getBag();
         NodeIterator bagIter = bag.iterator();
         // For each mapping
         while (bagIter.hasNext()) {
             RDFNode node = bagIter.nextNode();
             Resource aliasNsPair = node.as((new ResourceImpl(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_ALIAS_NS_PAIR)).getClass());
             Literal aliasLit = (Literal) aliasNsPair.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_ALIAS)).getObject();
             String alias = aliasLit.getString();
             Literal nsLit = (Literal) aliasNsPair.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_NAMESPACE)).getObject();
             String ns = nsLit.getString();
             nsMap.put(alias, ns);
         }
 
         return nsMap;
     }
 
     /**
      * A method for parsing an RDF resource that represent a full construction command which returns a ConstructionWrapper
      * instance representing this command
      * @param construct - The RDF resource that represent a full construction command
      * @return - A ConstructionWrapper instance representing this command
      */
     private ConstructionWrapper parseConstruct(Resource construct) {
 
         // ------------ Queries and Matcher ------------------------
         ArrayList<QueryWrapper> qWrappers = new ArrayList<QueryWrapper>();
 
         // Parse Queries
         Seq seq = construct.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_QUERIES)).getSeq();
         NodeIterator seqIter = seq.iterator();
         // For each query
         int index = 0;
         while (seqIter.hasNext()) {
             // Parse queryWrapper
             QueryWrapper qWrapper = parseQuery(seqIter.next());
             qWrappers.add(index, qWrapper);
             index++;
         }
 
         // Parse matcher
         Bag bag = construct.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_ATT_NODE_ID_MAP)).getBag();
         IMatcher matcher = parseMatcher(bag);
 
         // -----------------------------------------------------------------
 
         // ------------ Construction Pattern -----------------
 
         // Parse creation pattern
         Resource creationPattern = construct.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_CREATION_PATTERN)).getResource();
 
         Resource patternRepeatMode = creationPattern.getProperty((new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_CREATION_PATTERN_REP))).getResource();
         INaturalNumberSampler nSampler = parseNaturalNumSampler(patternRepeatMode);
 
         Bag oldNodesBag = creationPattern.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_OLD_NODES)).getBag();
         HashMap<Integer, Node> oldNodes = parseNodes(oldNodesBag);
         Bag newNodesBag = creationPattern.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_NEW_NODES)).getBag();
         HashMap<Integer, Node> newNodes = parseNodes(newNodesBag);
 
         // -----------------------------------------------------------------
 
         // ------------ Attribute Dynamic Variables Mappings ----------
 
         HashMap<String, String> attVarMap = new HashMap<String, String>();
         // Parse the query attribute dynamic-variables map
         Bag attVarBag = construct.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_ATT_DYN_VAR_MAP)).getBag();
         NodeIterator bagIter = attVarBag.iterator();
         // For each mapping
         while (bagIter.hasNext()) {
             RDFNode node = bagIter.nextNode();
             Resource attVarPair = node.as((new ResourceImpl(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_ATT_VAR_PAIR)).getClass());
             Literal attNameLit = (Literal) attVarPair.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_ATTRIBUTE_NAME)).getObject();
             String attName = attNameLit.getString();
             Literal varNameLit = (Literal) attVarPair.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_VARIABLE_NAME)).getObject();
             String varName = varNameLit.getString();
             // Add the mapping to the hash-map
             attVarMap.put(attName, varName);
         }
 
         // -----------------------------------------------------------------
 
         return new ConstructionWrapper(qWrappers, matcher, nSampler, oldNodes, newNodes, attVarMap);
     }
 
     /**
      * A method that parses an RDFNode which represents a query and returns a QueryWrapper instance which represents
      * the given query
      * @param node - An RDFNode which represents a query
      * @return - A QueryWrapper instance which represents the given query
      */
     private QueryWrapper parseQuery(RDFNode node) {
 
         // Parse the mode
         Resource query = node.as((new ResourceImpl(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_QUERY)).getClass());
         Resource mode = (Resource) query.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_MODE)).getObject();
         SamplingMode samplingMode = parseSamplingMode(mode);
 
         // Parse the selection strategy
         Resource resSelection = (Resource) query.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_QUERY_RES_SELECTION)).getObject();
         IRealNumberSampler realSampler = parseQueryResultSelection(resSelection);
 
         // Parse the query
         Literal litSparqlQuery = (Literal) query.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_SPARQL_QUERY)).getObject();
         String queryStr = parseSparqlQuery(litSparqlQuery);
 
         // Parse if the query is dynamic
         Literal isDynamicLit = (Literal) query.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_IS_DYNAMIC)).getObject();
         boolean isDynamic = isDynamicLit.getBoolean();
 
         return new QueryWrapper(queryStr, samplingMode, realSampler, isDynamic);
     }
 
     /**
      * A method that parses an RDF bag which represents a matcher and returns an instance of IMatcher based on the given
      * RDF bag
      * @param bag - The given bag which represents a matcher
      * @return - An instance of IMatcher based on the given RDF bag
      */
     private IMatcher parseMatcher(Bag bag) {
         IMatcher matcher = new StdMatcher();
         NodeIterator bagIter = bag.iterator();
         // For each query
         while (bagIter.hasNext()) {
             RDFNode node = bagIter.nextNode();
             Resource mapPair = node.as((new ResourceImpl(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_MAP_PAIR)).getClass());
             Literal idLit = (Literal) mapPair.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_ID)).getObject();
             Integer nodeId = idLit.getInt();
             Literal attNameLit = (Literal) mapPair.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_ATTRIBUTE_NAME)).getObject();
             String attributeName = attNameLit.getString();
             matcher.addMapping(nodeId, attributeName);
         }
         return matcher;
     }
 
     /**
      * A method that parses the given RDF resource which represents the sampling mode and returns the enum based format
      * of the sampling mode
      * @param mode - The sampling mode in RDF based format
      * @return - The enum based format of the sampling mode
      * @throws IllegalStateException - When the sampling mode is un-familiar
      */
     private SamplingMode parseSamplingMode(Resource mode) {
         String modeStr = mode.getProperty(RDF.type).getObject().toString();
 
         if (modeStr.equals(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_GLOBAL_DIT_MODE))
             return SamplingMode.RANDOM_GLOBAL_DISTINCT;
         else if (modeStr.equals(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_LOCAL_DIS_MODE))
             return SamplingMode.RANDOM_LOCAL_DISTINCT;
         else if (modeStr.equals(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_REPEATABLE_MODE))
             return SamplingMode.RANDOM_REPEATABLE;
         else
             throw new IllegalStateException("Failed to parse the query sampling mode!");
     }
 
     /**
      * A method for parsing the query result selection represented in the given RDF resource which returns an
      * IRealNumberSampler which represents the query-result selection mechanism
      * @param resSelection - The RDF based query result selection
      * @return - An IRealNumberSampler which represents the query-result selection mechanism
      * @throws IllegalStateException - In case that the query result selection is un-familiar 
      */
     private IRealNumberSampler parseQueryResultSelection(Resource resSelection) {
         String resSelectionStr = resSelection.getProperty(RDF.type).getObject().toString();
 
         if (resSelectionStr.equals(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_SELECT_CONST)) {
             Literal lit = (Literal) resSelection.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_CONST_VALUE)).getObject();
             int constVal = lit.getInt();
             ConstantRealSampler realSampler = new ConstantRealSampler();
             realSampler.setNumber(constVal);
             return realSampler;
         } else if (resSelectionStr.equals(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_SELECT_ALL)) {
             return null;
         } else if (resSelectionStr.equals(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_SELECT_NUMBER_RANGE)) {
             Literal minLit = (Literal) resSelection.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_MIN_VALUE)).getObject();
             double min = minLit.getDouble();
             Literal maxLit = (Literal) resSelection.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_MAX_VALUE)).getObject();
             double max = maxLit.getDouble();
             RangeRealNumberSampler realSampler = new RangeRealNumberSampler();
             realSampler.setMinValue(min);
             realSampler.setMaxValue(max);
             return realSampler;
         } else if (resSelectionStr.equals(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_SELECT_RANGE)) {
             Literal minLit = (Literal) resSelection.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_MIN_VALUE)).getObject();
             double min = minLit.getDouble();
             Literal maxLit = (Literal) resSelection.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_MAX_VALUE)).getObject();
             double max = maxLit.getDouble();
             RangeRealNumberSampler realSampler = new RangeRealNumberSampler();
             realSampler.setMinValue(min);
             realSampler.setMaxValue(max);
             return realSampler;
         } else
             throw new IllegalStateException("Failed to parse the query result selection!");
     }
 
     /**
      * A method for parsing the SPARQL query represented by the given RDF literal which returns a string representation
      * of the given query
      * @param sparqlQuery - The SPARQL query represented by the given RDF literal
      * @return - A string representation of the given query
      * @throws IllegalArgumentException - In case that we are having problems in reading the query from the given file
      * path 
      */
     private String parseSparqlQuery(Literal sparqlQuery) {
 
         String filePath = sparqlQuery.getString();
 
         String queryStr;
 
         try {
             queryStr = readFileContent(filePath);
         } catch (IOException e) {
             throw new IllegalArgumentException("Problems in reading the query file: " + filePath);
         }
 
         return queryStr;
     }
 
     /**
      * A method for reading the given file located at the given path and returning a string representation of the file's
      * content (queries are saved into files and must be read from the file for further processing in string format 
      * @param filePath - The file locating containing the data
      * @return - A string representation of the file's content 
      * @throws IOException - In case we have I/O problems with the given file and its path
      */
     private String readFileContent(String filePath) throws IOException {
         FileReader input = new FileReader(filePath);
 
         String queryStr = "";
 
         BufferedReader bufRead = new BufferedReader(input);
         String line;
         do {
             line = bufRead.readLine();
             if (line != null)
                 queryStr += line;
         }
         while (line != null);
 
         bufRead.close();
         input.close();
         return queryStr;
     }
 
     /**
      * A method for parsing an n-sampler represented by the given RDF resource which return an instance of INaturalNumberSampler
      * with the given properties as deinfed in the given resource
      * @param patternRepeatMode - The parse resource that represents the n-sampler
      * @return - An instance of INaturalNumberSampler with the given properties as defined in the given resource
      * @throws IllegalStateException - In case that the given d-sampler type isn't supported by the system  
      */
     private INaturalNumberSampler parseNaturalNumSampler(Resource patternRepeatMode) {
         String repMode = patternRepeatMode.getProperty(RDF.type).getObject().toString();
 
         if (repMode.equals(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_REPEAT_CONST)) {
             Literal lit = (Literal) patternRepeatMode.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_CONST_VALUE)).getObject();
             int constVal = lit.getInt();
             ConstantNumberSampler constSampler = new ConstantNumberSampler();
             constSampler.setNaturalNumber(constVal);
             return constSampler;
         } else if (repMode.equals(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_REPEAT_RANGE)) {
             Literal minLit = (Literal) patternRepeatMode.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_MIN_VALUE)).getObject();
             int min = minLit.getInt();
             Literal maxLit = (Literal) patternRepeatMode.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_MAX_VALUE)).getObject();
             int max = maxLit.getInt();
             StdNaturalNumberSampler nSampler = new StdNaturalNumberSampler();
             nSampler.setMinValue(min);
             nSampler.setMaxValue(max);
             return nSampler;
         } else
             throw new IllegalStateException("Failed to parse the creation pattern repetition mode and values!");
     }
 
     /**
      * A method that parses a list of nodes represented in the given RDF bag which returns a hash-map mapping node-id to
      * actual nodes
      * @param bag - An RDF bag which represents the list of nodes
      * @return - A hash-map mapping node-id to actual nodes
      */
     private HashMap<Integer, Node> parseNodes(Bag bag) {
         HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
 
         NodeIterator bagIter = bag.iterator();
         // Iterate over all bad entries (nodes)
         while (bagIter.hasNext()) {
             RDFNode rdfNode = bagIter.nextNode();
             Resource resourceNode = rdfNode.as((new ResourceImpl(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_OLD_NODE)).getClass());
             Literal typeLit = (Literal) resourceNode.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_TYPE)).getObject();
             String type = typeLit.getString();
             Literal idLit = (Literal) resourceNode.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_ID)).getObject();
             Integer nodeId = idLit.getInt();
             Bag edgeBag = resourceNode.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_EDGES)).getBag();
             ArrayList<Edge> edges = new ArrayList<Edge>();
             NodeIterator edgeBagIter = edgeBag.iterator();
             // Iterate over each node's edges
             while (edgeBagIter.hasNext()) {
                 RDFNode edgeNode = edgeBagIter.nextNode();
                 Resource resourceEdge = edgeNode.as((new ResourceImpl(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_EDGE)).getClass());
                 Literal edgeTypeLit = (Literal) resourceEdge.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_TYPE)).getObject();
                 String edgeType = edgeTypeLit.getString();
                 Literal pointsToIdLit = (Literal) resourceEdge.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_POINTS_TO_NODE_ID)).getObject();
                 Integer toNodeId = pointsToIdLit.getInt();
                 Literal edgeIdLit = (Literal) resourceEdge.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_ID)).getObject();
                 Integer edgeId = edgeIdLit.getInt();
                 Edge edge = new Edge(edgeType, edgeId, toNodeId);
                 edges.add(edge);
             }
             Node node = new Node(null, type, nodeId, edges);
             nodes.put(nodeId, node);
         }
 
         return nodes;
     }
 
     /**
      * A specific method for parsing the given sampler mapping and return a pair of rdf-type and the associated
      * dictionary-sampler 
      * @param samplerMapping - A single entry in the sampler-function mappings which contains the rdf-type and its
      * associated dictionary-sampler
      * @return - A pair of rdf-type and its associated dictionary-sampler 
      * @throws IOException - In case that one of the samplers fails to run init (which uses some external files for
      * loading the different labels
      * @throws IllegalStateException - In case of an un-familiar d-sampler
      */
     private TypeDSamplerPair parseSamplerMapping(Resource samplerMapping) throws IOException {
            String dSamplerType = samplerMapping.getProperty(RDF.type).getObject().toString();
 
         if (dSamplerType.equals(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_CUSTOM_DIC_SAMPLER)) {
             Literal typeLit = (Literal) samplerMapping.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_TYPE)).getObject();
             String type = typeLit.getString();
             Literal dataPathLit = (Literal) samplerMapping.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_SOURCE)).getObject();
             String dataPath = dataPathLit.getString();
             CustomDictionarySampler customDicSampler = new CustomDictionarySampler();
             customDicSampler.init(dataPath);
             return new TypeDSamplerPair(type, customDicSampler);
         } else if (dSamplerType.equals(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_STD_DIC_SAMPLER)) {
             Literal typeLit = (Literal) samplerMapping.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_TYPE)).getObject();
             String type = typeLit.getString();
             Literal dataPathLit = (Literal) samplerMapping.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_SOURCE)).getObject();
             String dataPath = dataPathLit.getString();
             Resource mode = (Resource) samplerMapping.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_MODE)).getObject();
             SamplingMode samplingMode = parseSamplingMode(mode);
             StdDictionarySampler dSampler = new StdDictionarySampler();
            dSampler.init(dataPath);
             dSampler.setSamplingMode(samplingMode);
             return new TypeDSamplerPair(type, dSampler);
         } else if (dSamplerType.equals(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_CTR_DIC_SAMPLER)) {
             Literal typeLit = (Literal) samplerMapping.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_TYPE)).getObject();
             String type = typeLit.getString();
             Literal labelLit = (Literal) samplerMapping.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_LABEL)).getObject();
             String label = labelLit.getString();
             Resource mode = (Resource) samplerMapping.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_MODE)).getObject();
             SamplingMode samplingMode = parseSamplingMode(mode);
             CounterDictionarySampler ctrDicSampler = new CounterDictionarySampler();
             ctrDicSampler.init(label);
             ctrDicSampler.setSamplingMode(samplingMode);
             return new TypeDSamplerPair(type, ctrDicSampler);
         } else if (dSamplerType.equals(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_EXT_DIC_SAMPLER)) {
             Literal typeLit = (Literal) samplerMapping.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_TYPE)).getObject();
             String type = typeLit.getString();
             ExternalConstDictionarySampler extDicSampler = new ExternalConstDictionarySampler();
             return new TypeDSamplerPair(type, extDicSampler);
         } else if (dSamplerType.equals(RDF_GEN_NS + RdfGenTypes.RES_RDF_TYPE_CONST_DIC_SAMPLER)) {
             Literal typeLit = (Literal) samplerMapping.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_TYPE)).getObject();
             String type = typeLit.getString();
             Literal labelLit = (Literal) samplerMapping.getProperty(new PropertyImpl(RDF_GEN_NS + RdfGenTypes.PROP_RDF_TYPE_LABEL)).getObject();
             String label = labelLit.getString();
             ConstDictionarySampler constDicSampler = new ConstDictionarySampler();
             constDicSampler.init(label);
             return new TypeDSamplerPair(type, constDicSampler);
         } else
             throw new IllegalStateException("Failed to parse the typeDictionaryMapping!");
     }
 
 }
