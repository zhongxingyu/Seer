 package cz.cuni.mff.odcleanstore.linker.impl;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.sparql.core.Quad;
 
 import cz.cuni.mff.odcleanstore.configuration.ObjectIdentificationConfig;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.connection.exceptions.ConnectionException;
 import cz.cuni.mff.odcleanstore.connection.exceptions.DatabaseException;
 import cz.cuni.mff.odcleanstore.connection.exceptions.QueryException;
 import cz.cuni.mff.odcleanstore.data.RDFprefix;
 import cz.cuni.mff.odcleanstore.linker.Linker;
 import cz.cuni.mff.odcleanstore.linker.rules.SilkRule;
 import cz.cuni.mff.odcleanstore.shared.RDFPrefixesLoader;
 import cz.cuni.mff.odcleanstore.transformer.TransformationContext;
 import cz.cuni.mff.odcleanstore.transformer.TransformedGraph;
 import cz.cuni.mff.odcleanstore.transformer.TransformedGraphException;
 import cz.cuni.mff.odcleanstore.transformer.TransformerException;
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.impl.GraphReaderService;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 import de.fuberlin.wiwiss.silk.Silk;
 
 /**
  * Default implementation of the {link #Linker} interface.
  * 
  * @author Tomas Soukup
  */
 public class LinkerImpl implements Linker {
 	private static final Logger LOG = LoggerFactory.getLogger(LinkerImpl.class);
 	
 	private static final String DEBUG_INPUT_FILENAME = "debugInput.xml";
 	private static final String DEBUG_OUTPUT_FILENAME = "debugResult.xml";
 	
 	private static final String CONFIG_XML_CELL = "Cell";
 	private static final String CONFIG_XML_ENTITY1 = "entity1";
 	private static final String CONFIG_XML_ENTITY2 = "entity2";
 	private static final String CONFIG_XML_RESOURCE = "rdf:resource";
 	private static final String CONFIG_XML_MEASURE = "measure";
 	
 	private static final String LABEL_URI = "rdfs:label";
 	
 	private ObjectIdentificationConfig globalConfig;
 	
 	public LinkerImpl(ObjectIdentificationConfig config) {
 		globalConfig = config;
 	}
 	
 	 /**
      * {@inheritDoc}
      * 
      * Generates links between input graph in dirty database and graphs in clean database.
      * 
      * Obtains linkage rule-groups from transformer configuration.
      * When no groups are specified, uses configuration files from designated directory.
      * 
      * @param inputGraph {@inheritDoc}
      * @param context {@inheritDoc}
      */
 	@Override
 	public void transformNewGraph(TransformedGraph inputGraph, TransformationContext context) throws TransformerException {
 		LOG.info("Linking new graph: {}", inputGraph.getGraphName());
 		String config = context.getTransformerConfiguration();
 		if (config == null || config.isEmpty()) {
 			LOG.info("No configuration specified, using XML files in directory {}", context.getTransformerDirectory().getAbsolutePath());
 			linkByConfigFiles(context);
 		} else {
 			File configFile = null;
 			try {				
 				List<SilkRule> rules = loadRules(context);
 				List<RDFprefix> prefixes = RDFPrefixesLoader.loadPrefixes(context.getCleanDatabaseCredentials());
 				
 				configFile = ConfigBuilder.createLinkConfigFile(rules, prefixes, inputGraph, context, globalConfig);
 				
 				inputGraph.addAttachedGraph(getLinksGraphId(inputGraph));
 				
 				LOG.info("Calling Silk with temporary configuration file: {}", configFile.getAbsolutePath());
 				Silk.executeFile(configFile, null, Silk.DefaultThreads(), true);
 				LOG.info("Linking finished.");
 				
 				configFile.delete();
 			} catch (DatabaseException e) {
 				throw new TransformerException(e);
 			} catch (TransformedGraphException e) {
 				throw new TransformerException(e);
 			} finally {
 				if (configFile != null) {
 					configFile.delete();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @param inputGraph {@inheritDoc}
 	 * @param context {@inheritDoc}
 	 * @throws TransformerException 
 	 */
 	@Override
 	public void transformExistingGraph(TransformedGraph inputGraph, TransformationContext context) throws TransformerException {
 		LOG.info("Linking existing graph: {}", inputGraph.getGraphName());
 		LinkerDao dao;
 		try {
 			dao = LinkerDao.getInstance(context.getCleanDatabaseCredentials(), context.getDirtyDatabaseCredentials());
 			dao.clearGraph(getLinksGraphId(inputGraph));
 			transformNewGraph(inputGraph, context);
 		} catch (DatabaseException e) {
 			throw new TransformerException(e);
 		}
 	}
 	
 	@Override
     public void shutdown() {
     }
 	
 	/**
 	 * {@inheritDoc}
 	 *
 	 * @param context {@inheritDoc}
 	 */
 	@Override
 	public void linkCleanDatabase(TransformationContext context) {
 		// TODO Auto-generated method stub		
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @param context {@inheritDoc}
 	 */
 	@Override
 	public void linkByConfigFiles(TransformationContext context) {
 		File[] files = context.getTransformerDirectory().listFiles();
 		LOG.info("{} configuration files found", files.length);
 		for (File file:files) {
 			if (file.getName().endsWith(".xml")) {
 				LOG.info("Calling Silk with configuration file: {}", file.getAbsolutePath());
 				Silk.executeFile(file, null, Silk.DefaultThreads(), true);
 				LOG.info("Linking finished.");
 			}
 		}
 	}
 	
 	/**
 	 * Loads list of rules from database.
 	 * 
 	 * Parses transformer configuration to obtain rule groups.
 	 * Then loads rules from these groups from DB using {@link LinkerDao}
 	 * 
 	 * @param transformerConfiguration string containing the list of rule-groups IDs
 	 * @param dao is used to load rules from DB
 	 */
 	private List<SilkRule> loadRules(TransformationContext context) 
 			throws ConnectionException, QueryException {
 		LOG.info("Loading rule groups: {}", context.getTransformerConfiguration());
 		LinkerDao dao = LinkerDao.getInstance(context.getCleanDatabaseCredentials(), context.getDirtyDatabaseCredentials());
 		String[] ruleGroupArray = context.getTransformerConfiguration().split(",");
 
 		return dao.loadRules(ruleGroupArray);
 	}
 	
 	private String getLinksGraphId(TransformedGraph inputGraph) {
 		return globalConfig.getLinksGraphURIPrefix().toString() + inputGraph.getGraphId();
 	}
 	
 	public List<DebugResult> debugRules(InputStream source, TransformationContext context) 
 			throws TransformerException {
 		return debugRules(streamToFile(source, context.getTransformerDirectory()), context);
 	}
 	
 	public List<DebugResult> debugRules(File inputFile, TransformationContext context) 
 			throws TransformerException {
 		List<DebugResult> resultList = new ArrayList<DebugResult>();
 		File configFile = null;
 		try {
 			List<SilkRule> rules = loadRules(context);
 			List<RDFprefix> prefixes = RDFPrefixesLoader.loadPrefixes(context.getCleanDatabaseCredentials());
 			for (SilkRule rule: rules) {
 				String resultFileName = createFileName(rule, context.getTransformerDirectory(), DEBUG_OUTPUT_FILENAME);
 				configFile = ConfigBuilder.createDebugLinkConfigFile(rule, prefixes, context, globalConfig,
 						inputFile.getAbsolutePath(), resultFileName);
 				Silk.executeFile(configFile, null, Silk.DefaultThreads(), true);
 				List<LinkedPair> linkedPairs = parseLinkedPairs(resultFileName);
 				loadLabels(inputFile, linkedPairs);
 				loadLabels(context.getCleanDatabaseCredentials(), context.getDirtyDatabaseCredentials(), linkedPairs);
 				resultList.add(new DebugResult(rule, linkedPairs));
 			}
 			
 		} catch (DatabaseException e) {
 			throw new TransformerException(e);
 		} 
 		
 		return resultList;
 	}
 	
 	private String createFileName(SilkRule rule, File transformerDirectory, String fileName) {
 		return transformerDirectory.getAbsolutePath() + rule.getId() + fileName;
 	}
 	
 	private List<LinkedPair> parseLinkedPairs(String resultFileName) throws TransformerException {
 		List<LinkedPair> pairList = new ArrayList<LinkedPair>();
 		
 		DocumentBuilder builder;
 		try {
 			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			Document doc = builder.parse(resultFileName);
 			NodeList cells = doc.getElementsByTagName(CONFIG_XML_CELL);
 			int cellLength = cells.getLength();
 			
 			for (int i = 0; i < cellLength; i++) {
 				String firstUri = null;
 				String secondUri = null;
 				Double confidence = null;
 				Element cell = (Element)cells.item(i);
 				NodeList cellChildern = cell.getChildNodes();
 				int childLength = cellChildern.getLength();
 				
 				for (int j = 0; j < childLength; j++) {
 					Element child = (Element) cellChildern.item(j);
 					String childName = child.getLocalName();
 					if (CONFIG_XML_ENTITY1.equals(childName)) {
 						firstUri = child.getAttribute(CONFIG_XML_RESOURCE);
 					} else if (CONFIG_XML_ENTITY2.equals(childName)) {
 						secondUri = child.getAttribute(CONFIG_XML_RESOURCE);
 					} else if (CONFIG_XML_MEASURE.equals(childName)) {
 						String content = child.getTextContent();
 						int leftIndex = content.indexOf('(');
 						if (leftIndex != -1) {
 							int rightIndex = content.indexOf(')');
 							if (rightIndex == -1) {
 								rightIndex = content.length();
 							}
 							content = content.substring(leftIndex + 1, rightIndex);
 						}				
 						confidence = Double.valueOf(child.getTextContent());
 					}
 				}
 				
 				pairList.add(new LinkedPair(firstUri, secondUri, confidence));
 			}
 			
 		} catch (Exception e) {
 			throw new TransformerException(e);
 		} 
 		
 		return pairList;
 	}
 	
 	private File streamToFile(InputStream stream, File targetDirectory) throws TransformerException {
 		try {
 			File file = new File(targetDirectory, DEBUG_INPUT_FILENAME);
 		    OutputStream os = new FileOutputStream(file);  
 		    try {  
 		        byte[] buffer = new byte[4096];  
 		        for (int n; (n = stream.read(buffer)) != -1; )   
 		            os.write(buffer, 0, n);
 		        return file;
 		    } catch (IOException e) {
 				throw new TransformerException(e);
 			} finally { 
 		    	try {
 					os.close();
 				} catch (IOException e) { /* do nothing */ } 
 		    }
 		} catch (FileNotFoundException e) {
 			throw new TransformerException(e);
 		} finally { 
 			try {
 				stream.close();
 			} catch (IOException e) { /* do nothing */ } 
 		}
 	}
 	
 	private void loadLabels(File inputFile, List<LinkedPair> linkedPairs) {
 		NamedGraphSet graphSet = loadGraphs(inputFile);
 		for (LinkedPair pair: linkedPairs) {
 			Iterator<?> it = graphSet.findQuads(
 					Node.ANY, Node.createURI(pair.getFirstUri()), Node.createURI(LABEL_URI), Node.ANY);
 			Quad quad;
 			if (it.hasNext()) {
 				quad = (Quad)it.next();
 				pair.setFirstLabel(quad.getObject().toString());
 			}
 			
 			it = graphSet.findQuads(
 					Node.ANY, Node.createURI(pair.getSecondUri()), Node.createURI(LABEL_URI), Node.ANY);
 			if (it.hasNext()) {
 				quad = (Quad)it.next();
 				pair.setSecondLabel(quad.getObject().toString());
 			}
 		}
 	}
 	
 	private NamedGraphSet loadGraphs(File inputFile) {
 		GraphReaderService reader = new GraphReaderService();
 		reader.setLanguage("RDF/XML");
 		reader.setSourceFile(inputFile);
 		NamedGraphSet graphSet = new NamedGraphSetImpl();
 		reader.readInto(graphSet);
 		
 		return graphSet;
 	}
 	
 	private void loadLabels(JDBCConnectionCredentials cleanDBCredentials, JDBCConnectionCredentials dirtyDBCredentials,
 			List<LinkedPair> linkedPairs) throws TransformerException {
 		Map<String, String> uriLabelMap = createUriLabelMap(linkedPairs);
 		LinkerDao dao;
 		try {
 			dao = LinkerDao.getInstance(cleanDBCredentials, dirtyDBCredentials);
			dao.loadLabels(uriLabelMap);
			for (LinkedPair pair: linkedPairs) {
				String label = uriLabelMap.get(pair.getFirstUri());
				if (label != null) {
					pair.setFirstLabel(label);
				}
				label = uriLabelMap.get(pair.getSecondUri());
				if (label != null) {
					pair.setSecondLabel(label);
				}
			}
 		} catch (ConnectionException e) {
 			throw new TransformerException(e);
 		} catch (QueryException e) {
 			throw new TransformerException(e);
 		}
 	}
 	
 	private Map<String, String> createUriLabelMap(List<LinkedPair> linkedPairs) {
 		Map<String, String> uriLabelMap = new HashMap<String, String>();
 		for (LinkedPair pair: linkedPairs) {
 			uriLabelMap.put(pair.getFirstUri(), pair.getFirstLabel());
 			uriLabelMap.put(pair.getSecondUri(), pair.getSecondLabel());
 		}
 		return uriLabelMap;
 	}
 }
