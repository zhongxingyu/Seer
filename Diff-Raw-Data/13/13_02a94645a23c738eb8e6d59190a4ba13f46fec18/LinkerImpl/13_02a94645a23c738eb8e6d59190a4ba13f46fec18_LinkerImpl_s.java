 package cz.cuni.mff.odcleanstore.linker.impl;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import cz.cuni.mff.odcleanstore.data.RDFprefix;
 import cz.cuni.mff.odcleanstore.data.SparqlEndpoint;
 import cz.cuni.mff.odcleanstore.linker.Linker;
 import cz.cuni.mff.odcleanstore.transformer.TransformationContext;
 import cz.cuni.mff.odcleanstore.transformer.TransformedGraph;
 import de.fuberlin.wiwiss.silk.Silk;
 
 public class LinkerImpl implements Linker {
 	
 	private static final String CONFIG_FILENAME = "linkConfig.xml";
 	
 	private static final String CONFIG_DIRTY_SOURCE_ID = "dirtyDB";
 	private static final String CONFIG_CLEAN_SOURCE_ID = "dirtyDB";
 	
 	private static final String CONFIG_XML_ROOT = "Silk";
 	private static final String CONFIG_XML_PREFIXES = "Prefixes";
 	private static final String CONFIG_XML_PREFIX = "Prefix";
 	private static final String CONFIG_XML_ID = "id";
 	private static final String CONFIG_XML_PREFIX_NAMESPACE = "namespace";
 	private static final String CONFIG_XML_SOURCES = "DataSources";
 	private static final String CONFIG_XML_SOURCE = "DataSource";
 	private static final String CONFIG_XML_PARAMETER = "Param";
 	private static final String CONFIG_XML_NAME = "name";
 	private static final String CONFIG_XML_VALUE = "value";
 	private static final String CONFIG_XML_ENDPOINT_URI = "endpointURI";
 	private static final String CONFIG_XML_GRAPH = "graph";
 
 	@Override
 	public void transformNewGraph(TransformedGraph inputGraph, TransformationContext context) {
 		List<String> rawRules = new ArrayList<String>(); // TODO load rules from DB
 		List<RDFprefix> prefixes = new ArrayList<RDFprefix>(); // TODO load prefixes from DB
 		
 		Document configDoc = createConfigDoc(rawRules, prefixes, inputGraph, context);
 		
 		File configFile = storeConfigDoc(configDoc, context.getTransformerDirectory());
 		
 		Silk.executeFile(configFile, null, Silk.DefaultThreads(), true);
 	}
 
 	@Override
 	public void transformExistingGraph(TransformedGraph inputGraph, TransformationContext context) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void linkCleanDatabase(TransformationContext context) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void linkByConfigFiles(TransformationContext context) {
 		File[] files = context.getTransformerDirectory().listFiles();
 		for (File file:files) {
 			if (file.getName().endsWith(".xml")) {
 				Silk.executeFile(file, null, Silk.DefaultThreads(), true);
 			}
 		}
 	}
 	
 	private Document createConfigDoc(List<String> rawRules, List<RDFprefix> prefixes, 
 			TransformedGraph inputGraph, TransformationContext context) {
 		Document configDoc = null;
 		DocumentBuilder builder;
 		try {
 			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			configDoc = builder.newDocument();
 			Element root = configDoc.createElement(CONFIG_XML_ROOT);
 			root.appendChild(createPrefixes(configDoc, prefixes));
 			root.appendChild(createSources(configDoc, context.getDirtyDatabaseEndpoint(), 
 					context.getCleanDatabaseEndpoint(), inputGraph.getGraphName()));
 			root.appendChild(createLinkageRules(configDoc, rawRules, context));			
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 		return configDoc;
 	}
 	
 	private Element createPrefixes(Document doc, List<RDFprefix> prefixes) {
 		Element prefixesElement = doc.createElement(CONFIG_XML_PREFIXES);
 		
 		for (RDFprefix prefix:prefixes) {
 			Element prefixElement = doc.createElement(CONFIG_XML_PREFIX);;
 			prefixElement.setAttribute(CONFIG_XML_ID, prefix.getPrefixId());
 			prefixElement.setAttribute(CONFIG_XML_PREFIX_NAMESPACE, prefix.getNamespace());
 			
 			prefixesElement.appendChild(prefixElement);
 		}
 		
 		return prefixesElement;
 	}
 	
 	private Element createSources(Document doc, SparqlEndpoint dirtyEndpoint, SparqlEndpoint cleanEndpoint, String graphName) {
 		Element sourcesElement = doc.createElement(CONFIG_XML_SOURCES);
 		
 		Element sourceElement = createSource(doc, dirtyEndpoint, graphName);
 		sourceElement.setAttribute(CONFIG_XML_ID, CONFIG_DIRTY_SOURCE_ID);
 		sourcesElement.appendChild(sourceElement);
 		
 		sourceElement = createSource(doc, cleanEndpoint, null);
 		sourceElement.setAttribute(CONFIG_XML_ID, CONFIG_CLEAN_SOURCE_ID);
 		sourcesElement.appendChild(sourceElement);
 		
 		return sourcesElement;
 	}
 	
 	private Element createSource(Document doc, SparqlEndpoint endpoint, String graphName) {
 		Element sourceElement = doc.createElement(CONFIG_XML_SOURCE);
 		
 		Element parameterElement = doc.createElement(CONFIG_XML_PARAMETER);
 		parameterElement.setAttribute(CONFIG_XML_NAME, CONFIG_XML_ENDPOINT_URI);
 		parameterElement.setAttribute(CONFIG_XML_VALUE, endpoint.getUri());
 		sourceElement.appendChild(parameterElement);
 		
 		if (graphName != null) {
 			parameterElement = doc.createElement(CONFIG_XML_PARAMETER);
 			parameterElement.setAttribute(CONFIG_XML_NAME, CONFIG_XML_GRAPH);
 			parameterElement.setAttribute(CONFIG_XML_VALUE, graphName);
 			sourceElement.appendChild(parameterElement);
 		}
 		
 		return sourceElement;
 	}
 	
 	private Element createLinkageRules(Document doc, List<String> rawRules, TransformationContext context) {
 		//TODO create linkage rules
 		return null;
 	}
 
 	private File storeConfigDoc(Document configDoc, File targetDirectory) {
 		File configFile = new File(targetDirectory, CONFIG_FILENAME);
 		
 		Transformer transformer;
 		try {
 			transformer = TransformerFactory.newInstance().newTransformer();
 			DOMSource source = new DOMSource(configDoc);
 			StreamResult result = new StreamResult(configFile);
 			
 			transformer.transform(source, result);
 		} catch (TransformerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransformerFactoryConfigurationError e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 				
 		return configFile;
 	}
 }
