 package analyzer;
 
 import backend.PreprocessorNode;
 import backend.storage.IdentifiedFeatureList;
 import backend.storage.PreprocessorOccurrence;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import net.sf.saxon.AugmentedSource;
 import net.sf.saxon.Configuration;
 import net.sf.saxon.om.DocumentInfo;
 import net.sf.saxon.om.NodeInfo;
 import net.sf.saxon.sxpath.IndependentContext;
 import net.sf.saxon.sxpath.XPathEvaluator;
 import net.sf.saxon.sxpath.XPathExpression;
 import net.sf.saxon.trans.XPathException;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import common.QueryBuilder;
 import common.XMLTools;
 
 import scanner.CheckLogics;
 import scanner.FeaturePattern;
 import scanner.PreprocessorTree;
 import scanner.RemainingOccurrencesPattern;
 import scanner.ReplaceFileException;
 import scanner.patterns.ClassIntroductionPattern;
 import scanner.patterns.CompleteFunctionPattern;
import scanner.patterns.CompleteFunctionPatternInline;
 import scanner.patterns.CompletePrivatePublicBlock;
 import scanner.patterns.DirectivesPattern;
 import scanner.patterns.InFunctionOccurrencesPattern;
 
 public class FeatureSearcher {
 	private static final String ALL_RELEVANT_DIRECTIVES_XPATH = "//(cpp:if union cpp:ifdef union cpp:ifndef union cpp:else union cpp:endif)";
 	private File outputDir = null;
 	private IdentifiedFeatureList backend = null;
 	private LinkedList<String> annotationfilter;
 
 	public void importExternalFile(File xmlDoc) throws ReplaceFileException {
 		FileOutputStream out = null;
 		try {
 			Document doc = XMLTools.parseDocument(xmlDoc);
 			if (this.outputDir != null) {
 				File baseFile = new File(this.outputDir.getAbsolutePath()
 						+ File.separatorChar + "Base" + File.separatorChar
 						+ xmlDoc.getName());
 				if (baseFile.exists())
 					throw new ReplaceFileException();
 				Transformer transformer = TransformerFactory.newInstance()
 						.newTransformer();
 				DOMSource src = new DOMSource(doc);
 				out = new FileOutputStream(baseFile);
 				StreamResult res = new StreamResult(out);
 				transformer.transform(src, res);
 				xmlDoc = baseFile;
 			}
 			checkDoc(xmlDoc, doc);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (TransformerConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerFactoryConfigurationError e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		} finally {
 			if (out != null)
 				try {
 					out.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 		}
 	}
 
 	public void checkDoc(File xmlDoc) {
 		if (!this.backend.isKnown(xmlDoc.getAbsolutePath()))
 			checkDoc(xmlDoc, null);
 	}
 
 	private void checkDoc(File xmlDoc, Document doc) {
 		PreprocessorTree tree = buildPrepTreeWithLineNumbers(xmlDoc);
 		try {
 			if (doc == null) {
 				doc = XMLTools.parseDocument(xmlDoc);
 			}
 			populateTree(doc, tree);
 			tree.calculateDepths();
 
 			FeaturePattern pattern1 = new CompletePrivatePublicBlock();
 			FeaturePattern pattern2 = new CompleteFunctionPattern();
 			FeaturePattern pattern3 = new InFunctionOccurrencesPattern();
 			FeaturePattern pattern4 = new ClassIntroductionPattern();
 			FeaturePattern pattern5 = new DirectivesPattern();
			FeaturePattern pattern6 = new CompleteFunctionPatternInline();
 
 			LinkedList<PreprocessorOccurrence> occs = new LinkedList<PreprocessorOccurrence>();
 			if (annotationfilter == null || annotationfilter.contains("CompletePrivatePublicBlockPattern"))
 				occs.addAll(pattern1.checkDoc(doc, tree));
 			if (annotationfilter == null || annotationfilter.contains("ClassIntroductionPattern"))
 				occs.addAll(pattern4.checkDoc(doc, tree));
 			if (annotationfilter == null || annotationfilter.contains("DirectivesPattern"))
 				occs.addAll(pattern5.checkDoc(doc, tree));
			if (annotationfilter == null || annotationfilter.contains("CompleteFunctionPattern")) {
 				occs.addAll(pattern2.checkDoc(doc, tree));
				occs.addAll(pattern6.checkDoc(doc, tree));
			}
 			if (annotationfilter == null || annotationfilter.contains("InFunctionOccurrencesPattern"))
 				occs.addAll(pattern3.checkDoc(doc, tree));
 
 			FeaturePattern remains = new RemainingOccurrencesPattern();
 			occs.addAll(remains.checkDoc(doc, tree));
 
 			for (PreprocessorOccurrence occ : occs) {
 				occ.setDocFileName(xmlDoc);
 				addToBackend(occ);
 			}
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (XPathExpressionException e) {
 			e.printStackTrace();
 		} catch (TransformerFactoryConfigurationError e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void populateTree(Document doc, PreprocessorTree tree)
 			throws SAXException, IOException, ParserConfigurationException,
 			XPathExpressionException {
 		javax.xml.xpath.XPathExpression expr = QueryBuilder
 				.instance()
 				.getExpression(
 						"//(cpp:if union cpp:ifdef union cpp:ifndef union cpp:else union cpp:endif)");
 		NodeList result = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
 		tree.populate(result);
 	}
 
 	private PreprocessorTree buildPrepTreeWithLineNumbers(File xmlDoc) {
 		AugmentedSource src = AugmentedSource
 				.makeAugmentedSource(new StreamSource(xmlDoc));
 		src.setLineNumbering(true);
 		Configuration conf = new Configuration();
 		conf.setLineNumbering(true);
 		XPathEvaluator evaluator = new XPathEvaluator(conf);
 		List<?> resultNodes = null;
 		try {
 			DocumentInfo doc = conf.buildDocument(src);
 
 			IndependentContext ctx = new IndependentContext(
 					doc.getConfiguration());
 			ctx.declareNamespace("cpp", "http://www.sdml.info/srcML/cpp");
 			evaluator.setStaticContext(ctx);
 
 			XPathExpression expr = evaluator.createExpression(ALL_RELEVANT_DIRECTIVES_XPATH);
 			resultNodes = expr.evaluate(src);
 		} catch (XPathException e) {
 			e.printStackTrace();
 		}
 
 		PreprocessorTree tree = new PreprocessorTree();
 
 		for (Object cont : resultNodes) {
 			if ((cont instanceof NodeInfo)) {
 				PreprocessorNode node = new PreprocessorNode();
 				NodeInfo nI = (NodeInfo) cont;
 				node.setLineNumber(nI.getLineNumber());
 				node.setType(nI.getLocalPart());
 				tree.add(node);
 			}
 		}
 		src.close();
 		return tree;
 	}
 
 	private void addToBackend(PreprocessorOccurrence occurrence) {
 		String featureName = new CheckLogics().getFeatureName(occurrence);
 
 		this.backend.add(featureName, occurrence);
 	}
 
 	public void setOutputDir(File outputDir) {
 		this.outputDir = outputDir;
 	}
 
 	public void setBackend(IdentifiedFeatureList backend) {
 		this.backend = backend;
 	}
 
 	public void setAnnotationFilter(LinkedList<String> annotationfilter) {
 		this.annotationfilter = annotationfilter;
 	}
 }
