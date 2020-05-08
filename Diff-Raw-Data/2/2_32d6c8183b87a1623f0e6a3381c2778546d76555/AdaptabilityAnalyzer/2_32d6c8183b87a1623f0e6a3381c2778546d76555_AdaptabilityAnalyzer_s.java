 package pete.metrics.adaptability;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import pete.executables.AnalysisException;
 import pete.executables.FileAnalyzer;
 import pete.reporting.ReportEntry;
 
 public class AdaptabilityAnalyzer implements FileAnalyzer {
 
 	private ElementCounter elementCounter;
 
 	public AdaptabilityAnalyzer() {
 		elementCounter = new ElementCounter(true);
 	}
 
 	@Override
 	public List<ReportEntry> analyzeFile(Path filePath) {
 		System.out.println("Analyzing " + filePath + " for adaptability");
 
 		ReportEntry entry = new ReportEntry(filePath.toString());
 		try {
 			populateReportEntry(entry);
 		} catch (AnalysisException e) {
 			System.err.println(e.getMessage());
 			return new ArrayList<>(0);
 		}
 		List<ReportEntry> entries = new ArrayList<>(1);
 		entries.add(entry);
 
 		elementCounter.writeToCsv(Paths.get("raw.csv"));
 
 		return entries;
 	}
 
 	private void populateReportEntry(ReportEntry entry) {
 
 		Document dom = getDom(entry.getFileName());
 
 		entry.addVariable("isCorrectNamespace", isCorrectNamespace(dom));
 		Node process = getProcess(dom);
 		entry.addVariable("isExecutable", isExecutable(process));
 		entry.addVariable("elements", getNumberOfChildren(process) + "");
 
 		elementCounter.addToCounts(process);
 	}
 
 	private Document getDom(String file) {
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		dbf.setNamespaceAware(true);
 		DocumentBuilder db;
 		Document dom = null;
 
 		try (FileInputStream fis = new FileInputStream(new File(file))) {
 			db = dbf.newDocumentBuilder();
 			db.setErrorHandler(new ErrorHandler() {
 
 				@Override
 				public void error(SAXParseException arg0) throws SAXException {
 					throw arg0;
 				}
 
 				@Override
 				public void fatalError(SAXParseException arg0)
 						throws SAXException {
 					throw arg0;
 				}
 
 				@Override
 				public void warning(SAXParseException arg0) throws SAXException {
 				}
 			});
 
 			dom = db.parse(fis);
 
 		} catch (ParserConfigurationException | SAXException | IOException e) {
 
 		}
 		return dom;
 	}
 
 	private Node getProcess(Document dom) throws AnalysisException {
 		NodeList nodes = getChildrenOfDefinitions(dom);
 		if (nodes == null) {
 			throw new AnalysisException(
					"File cannot be analyzed: definitions element is empty");
 		}
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node node = nodes.item(i);
 			if ("process".equals(node.getLocalName())) {
 				return node;
 			}
 		}
 		throw new AnalysisException(
 				"File cannot be analyzed: no process element found");
 	}
 
 	private String isCorrectNamespace(Document dom) {
 		Element root = null;
 		try {
 			root = dom.getDocumentElement();
 		} catch (NullPointerException e) {
 			return "false";
 		}
 		if ("http://www.omg.org/spec/BPMN/20100524/MODEL".equals(root
 				.getNamespaceURI())) {
 			return "true";
 		} else {
 			return "false";
 		}
 	}
 
 	private String isExecutable(Node process) {
 		NamedNodeMap attributes = process.getAttributes();
 
 		for (int j = 0; j < attributes.getLength(); j++) {
 			Node attribute = attributes.item(j);
 			if ("isExecutable".equals(attribute.getLocalName())
 					&& "true".equals(attribute.getNodeValue())) {
 				return "true";
 			}
 		}
 
 		return "false";
 	}
 
 	private NodeList getChildrenOfDefinitions(Document dom) {
 		NodeList nodes = null;
 		try {
 			nodes = dom.getChildNodes();
 		} catch (NullPointerException e) {
 			return null;
 		}
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node node = nodes.item(i);
 			System.out.println(node.getNodeName());
 			if ("definitions".equals(node.getLocalName())) {
 				return node.getChildNodes();
 			}
 		}
 		return null;
 	}
 
 	private int getNumberOfChildren(Node node) {
 		NodeList children = node.getChildNodes();
 		int result = 0;
 
 		if (children == null) {
 			// do noting, result is zero
 		} else {
 			result += children.getLength();
 			for (int i = 0; i < children.getLength(); i++) {
 				result += getNumberOfChildren(children.item(i));
 			}
 		}
 		return result;
 	}
 }
