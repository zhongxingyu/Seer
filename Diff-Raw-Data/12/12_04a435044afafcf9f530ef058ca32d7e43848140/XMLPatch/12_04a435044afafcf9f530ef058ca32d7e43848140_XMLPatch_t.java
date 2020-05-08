 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mlparch;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.io.SequenceInputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map.Entry;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author John Petska
  */
 public class XMLPatch {
 	public int verbosity = 0;
 	
 	public PrintStream stdout = System.out;
 	public void printout(int l, String s)   { if (stdout != null && l <= verbosity) stdout.print  (s); }
 	public void printlnout(int l, String s) { if (stdout != null && l <= verbosity) stdout.println(s); }
 	public PrintStream stderr = System.err;
 	public void printerr(int l, String s)   { if (stderr != null && l <= verbosity) stderr.print  (s); }
 	public void printlnerr(int l, String s) { if (stderr != null && l <= verbosity) stderr.println(s); }
 	
 	private final DocumentBuilder docBuilder;
 	private final XPathFactory xpfactory;
 	
 	public static class XMLPDoc {
 		public HashMap<String, String> options;
 		public Document doc;
 
 		public XMLPDoc(HashMap<String, String> options, Document doc) {
 			this.options = options;
 			this.doc = doc;
 		}
 	}
 	public final HashMap<String, XMLPDoc> docMap = new HashMap<String, XMLPDoc>();
 	
 	public final HashMap<String, XMLPatchOp> opList = new HashMap<String, XMLPatchOp>();
 	public void addDefaultOps() {
 		opList.put("print" , new XMLPatchOpPrint(this));
 		opList.put("="     , new XMLPatchOpSet(this));
 		opList.put("+="    , new XMLPatchOpAddSet(this));
 		opList.put("-="    , new XMLPatchOpSubSet(this));
 		opList.put("*="    , new XMLPatchOpMulSet(this));
 		opList.put("/="    , new XMLPatchOpDivSet(this));
 		opList.put("floor" , new XMLPatchOpFloor(this));
 		opList.put("ceil"  , new XMLPatchOpCeil(this));
 		opList.put("round" , new XMLPatchOpRound(this));
 		opList.put("sqrt"  , new XMLPatchOpSqrt(this));
 		opList.put("+attr" , new XMLPatchOpAddAttr(this));
 		opList.put("+elem" , new XMLPatchOpAddElem(this));
 		opList.put("remove", new XMLPatchOpRemove(this));
 	}
 	
 	public static class WhitespaceFixerInputStream extends InputStream {
 		public String fixChars = "\"";
 		public String append = " ";
 		public int quotChar  = '"';
 		
 		int appendPos = -1;
 		boolean quot = false;
 		
 		InputStream is;
 		public WhitespaceFixerInputStream(InputStream is) {
 			this.is = is;
 		}
 		
 		
 		@Override
 		public int read() throws IOException {
 			//either dump an append char, or reset to -1.
 			//if we're not appending, do nothing
 			if (appendPos >= 0)
 				if (appendPos >= append.length())
 					appendPos = -1;
 				else
 					return append.charAt(appendPos++);
 			
 			//read a char...
 			int c = is.read();
 			
 			//don't modify string literals.
 			if (c == quotChar)
 				quot = !quot;
 			
 			//if it's one of our fix characters, prepare to append
 			if (!quot && fixChars.indexOf(c) >= 0)
 				appendPos = 0;
 			//then write the char
 			return c;
 		}
 	}
 	
 	public static class DummyInputStream extends SequenceInputStream {
 		static byte[] open = "<dummy>".getBytes();
 		static byte[] close = "</dummy>".getBytes();
 		
 		public DummyInputStream(InputStream is) {
 			super(Collections.enumeration(Arrays.asList(new InputStream[] {
 				new ByteArrayInputStream(open),
 				is,
 				new ByteArrayInputStream(close),
 			})));
 		}
 	}
 	
 	public static class DummyOutputStream extends OutputStream {
 		private int state = 0;
 		//state = 0, searching for opening dummy. data is discarded
 		//state = 1, searching for closing dummy, data is written
 		//state = 2, closing dummy matched, data is discarded (never leaves this state)
 		byte[] buffer = new byte[16];
 		int bpos = 0;
 
 		OutputStream os;
 		
 		public DummyOutputStream(OutputStream os) {
 			this.os = os;
 		}
 		
 		@Override
 		public void write(int b) throws IOException {
 			byte by = (byte)(0xFF&b);
 			switch (state) {
 				case 0: //search for opening dummy, discarding all data until a match
 					if (by == DummyInputStream.open[bpos]) {
 						buffer[bpos++] = by;
 						if (bpos == DummyInputStream.open.length) {
 							state = 1;
 							bpos = 0;
 						}
 					} else {
 						bpos = 0;
 					}
 					break;
 				case 1: //search for closing dummy, writing any data that doesn't match
 					if (by == DummyInputStream.close[bpos]) {
 						buffer[bpos++] = by;
 						if (bpos == DummyInputStream.close.length) {
 							state = 2;
 							bpos = 0;
 						}
 					} else {
 						os.write(buffer, 0, bpos);
 						os.write(b);
 						bpos = 0;
 					}
 					break;
 				case 2: //done, discard all data
 					break;
 			}
 		}
 
 		@Override
 		public void flush() throws IOException {
 			os.flush();
 		}
 
 		@Override
 		public void close() throws IOException {
 			os.close();
 		}
 	}
 	public static class XMLQuery {
 		public final Document doc;
 		public final String target;
 		public final String query;
 		public final NodeList result;
 		public final boolean negative;
 		
 		public XMLQuery(Document doc, String target, String query, NodeList result, boolean negative) {
 			this.doc = doc;
 			this.target = target;
 			this.query = query;
 			this.result = result;
 			this.negative = negative;
 		}
 	}
 	public XMLPatch(int verbosity) throws Exception {
 		this.verbosity = verbosity;
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setNamespaceAware(true);
 		docBuilder = factory.newDocumentBuilder();
 		xpfactory = XPathFactory.newInstance();
 		addDefaultOps();
 	}
 	public Document getDoc(File root, String target, HashMap<String, String> options) throws FileNotFoundException, SAXException, IOException {
 		if (options == null)
 			options = new HashMap<String, String>(1);
 		XMLPDoc doc = docMap.get(target);
 		if (doc == null) {
 			File file = new File(root==null?new File("."):root, target);
 			if (!file.exists() || !file.isFile())
 				throw new RuntimeException("Couldn't locate target! (\""+file.getPath()+"\")");
 			
 			InputStream is = new FileInputStream(file);
 			if (Boolean.parseBoolean(options.get("whitespacefix")))
 				is = new WhitespaceFixerInputStream(is);
 			if (Boolean.parseBoolean(options.get("dummyroot")))
 				is = new DummyInputStream(is);
 			
 			doc = new XMLPDoc(options, docBuilder.parse(is));
 			docMap.put(target, doc);
 		}
 		return doc.doc;
 	}
 	public NodeList getNodes(Document doc, String query) throws XPathExpressionException {
 		XPath xpath = xpfactory.newXPath();
 		NodeList nodes = (NodeList) xpath.evaluate(query, doc, XPathConstants.NODESET);
 		return nodes;
 	}
 	public void applyOp(List<Node> nodes, Element config, XMLPatchOp op) throws XPathExpressionException {
 		for (int i = 0; i < nodes.size(); i++) {
 			Node node = nodes.get(i);
 			try {
 				op.apply(config, node);
 			} catch (Throwable t) {
 				printlnerr(0, "Exception applying op on "+node+": "+t.getClass().getSimpleName()+": "+t.getLocalizedMessage());
 			}
 		}
 	}
 	public void applyOp(Document doc, String query, Element config, XMLPatchOp op) throws XPathExpressionException {
 		applyOp(new NodeListList(getNodes(doc, query)), config, op);
 	}
 	public void applyPatch(File patchFile, File rootDir) throws Exception {
 		DocumentBuilderFactory patFac = DocumentBuilderFactory.newInstance();
 		DocumentBuilder patBuilder = patFac.newDocumentBuilder();
 		Document patchDoc = patBuilder.parse(new FileInputStream(patchFile));
 		Node n_xmlp = patchDoc.getFirstChild();
 		if (!n_xmlp.getNodeName().equals("xmlp"))
 			throw new RuntimeException("Root patch node must be named \"xmlp\""); 
 
 		for (Node n_xmlp_node = n_xmlp.getFirstChild(); n_xmlp_node != null; n_xmlp_node = n_xmlp_node.getNextSibling()) {
 			if (n_xmlp_node instanceof Element) {
 				Element e_xmlp_node = (Element) n_xmlp_node;
 				if (n_xmlp_node.getNodeName().equals("patch")) {
 					String patch_name = e_xmlp_node.getAttribute("name");
 					if (patch_name == null || patch_name.isEmpty())
 						printlnout(0, "Running anonymous patch...");
 					else printlnout(0, "Running patch \""+patch_name+"\"...");
 					
 					ArrayList<XMLQuery> queryList = new ArrayList<XMLQuery>();
 					patchLoop:
 					for (Node n_xmlp_patch_node = n_xmlp_node.getFirstChild(); n_xmlp_patch_node != null; n_xmlp_patch_node = n_xmlp_patch_node.getNextSibling()) {
 						if (n_xmlp_patch_node instanceof Element) {
 							Element e_xmlp_patch_node = (Element) n_xmlp_patch_node;
 							if (n_xmlp_patch_node.getNodeName().equals("addnodes")) {
 								String p_target = e_xmlp_patch_node.getAttribute("target");
 								String p_query = e_xmlp_patch_node.getAttribute("query");
 								
 								if (p_target == null || p_target.isEmpty()) { printlnerr(0, "AddQuery has no target!"); continue; }
 								if (p_query  == null || p_query.isEmpty()) { printlnerr(0, "AddQuery has no query!"); continue; }
 
 								printlnout(2, "Adding nodes with query \""+p_target+"\":\""+p_query+"\"...");
 
 								for (int i = 0; i < queryList.size(); i++) {
 									XMLQuery q = queryList.get(i);
									if (q.target.equals(p_target) && q.query.equals(p_query)) {
										if (q.negative) {
											printlnerr(0, "Warning: tried to add duplicate query!");
											continue patchLoop;
										} else {
											printlnerr(0, "Warning: cancelling out previous query!");
										}
 									}
 								}
 								
 								Document doc = getDoc(rootDir, p_target, null);
 								NodeList nodes = getNodes(doc, p_query);
 								
 								queryList.add(new XMLQuery(doc, p_target, p_query, nodes, false));
 							} else if (n_xmlp_patch_node.getNodeName().equals("remnodes")) {
 								String p_target = e_xmlp_patch_node.getAttribute("target");
 								String p_query = e_xmlp_patch_node.getAttribute("query");
 								
 								if (p_target == null || p_target.isEmpty()) { printlnerr(0, "AddQuery has no target!"); continue; }
 								if (p_query  == null || p_query.isEmpty()) { printlnerr(0, "AddQuery has no query!"); continue; }
 
 								printlnout(2, "Removing nodes with query \""+p_target+"\":\""+p_query+"\"...");
 
 								for (int i = 0; i < queryList.size(); i++) {
 									XMLQuery q = queryList.get(i);
 									if (q.target.equals(p_target) && q.query.equals(p_query)) {
 										if (q.negative) {
 											printlnerr(0, "Warning: tried to add duplicate query!");
 											continue patchLoop;
 										} else {
 											printlnerr(0, "Warning: cancelling out previous query!");
 										}
 									}
 								}
 								
 								Document doc = getDoc(rootDir, p_target, null);
 								NodeList nodes = getNodes(doc, p_query);
 								
 								queryList.add(new XMLQuery(doc, p_target, p_query, nodes, true));
 							} else if (n_xmlp_patch_node.getNodeName().equals("remquery")) {
 								String p_target = e_xmlp_patch_node.getAttribute("target");
 								String p_query = e_xmlp_patch_node.getAttribute("query");
 								
 								if (p_target == null || p_target.isEmpty()) { printlnerr(0, "RemQuery has no target!"); continue; }
 								if (p_query  == null || p_query.isEmpty()) { printlnerr(0, "RemQuery has no query!"); continue; }
 
 								printlnout(2, "Removing query \""+p_target+"\":\""+p_query+"\"...");
 								
 								for (int i = 0; i < queryList.size(); i++) {
 									XMLQuery q = queryList.get(i);
 									if (q.target.equals(p_target) && q.query.equals(p_query)) {
 										queryList.remove(i);
 										break;
 									}
 								}
 							} else if (n_xmlp_patch_node.getNodeName().equals("clearqueries")) {
 								printlnout(2, "Clearing query list...");
 								
 								queryList.clear();
 							} else if (n_xmlp_patch_node.getNodeName().equals("op")) {
 								NamedNodeMap n_xmlp_patch_op_attr = n_xmlp_patch_node.getAttributes();
 								
 								String p_op = e_xmlp_patch_node.getAttribute("id");
 								
 								if (p_op == null || p_op.isEmpty()) { printlnerr(0, "Op has no id!"); continue; }
 								
 								XMLPatchOp op = opList.get(p_op);
 								if (op == null) { printlnerr(0, "Unrecognized op! (\""+p_op+"\")"); continue; }
 								
 								ArrayList<Node> nodes = new ArrayList<Node>();
 								
 								for (int i = 0; i < queryList.size(); i++) {
 									XMLQuery q = queryList.get(i);
 									if (q.negative) {
 										printlnout(1, "Removing nodes \""+q.target+"\":\""+q.query+"\" from \""+p_op+"\" execution...");
 										nodes.removeAll(new NodeListList(q.result));
 									} else {
 										printlnout(1, "Adding nodes \""+q.target+"\":\""+q.query+"\" to \""+p_op+"\" execution...");
 										nodes.addAll(new NodeListList(q.result));
 									}
 								}
 								applyOp(nodes, (Element)n_xmlp_patch_node, op);
 							}
 						}
 					}
 				} else if (n_xmlp_node.getNodeName().equals("load")) {
 					NamedNodeMap n_xmlp_node_attr = n_xmlp_node.getAttributes();
 
 					Node n = null;
 					String p_target = null;
 
 					//get target...
 					n = n_xmlp_node_attr.getNamedItem("target");
 					if (n != null) p_target = n.getNodeValue();
 
 					if (p_target == null) { printlnerr(0, "Load has no target!"); continue; }
 
 					HashMap<String, String> options = new HashMap<String, String>();
 
 					for (int i = 0; i < n_xmlp_node_attr.getLength(); i++) {
 						Node item = n_xmlp_node_attr.item(i);
 						if (!item.getNodeName().equals("target")) {
 							options.put(item.getNodeName(), item.getNodeValue());
 						}
 					}
 
 					printlnout(0, "Loading \""+p_target+"\" with options... "+options);
 
 					getDoc(rootDir, p_target, options);
 				}
 			}
 		}
 	}
 	public void writeDocMap(File outDir) throws Exception {
 		Transformer transformer = TransformerFactory.newInstance().newTransformer();
 		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 
 		for (Iterator<Entry<String, XMLPDoc>> iter = docMap.entrySet().iterator(); iter.hasNext();) {
 			Entry i = iter.next();
 			String path = (String)i.getKey();
 			XMLPDoc doc = (XMLPDoc)i.getValue();
 			
 			//initialize StreamResult with File object to save to file
 			File outFile = new File(outDir, path);
 			printlnout(0, "Writing \""+path+"\"...");
 			
 			OutputStream os = new FileOutputStream(outFile);
 			if (Boolean.parseBoolean(doc.options.get("dummyroot")))
 				os = new DummyOutputStream(os);
 			
 			StreamResult result = new StreamResult(new OutputStreamWriter(os));
 			Source source = new DOMSource(doc.doc);
 			transformer.transform(source, result);
 		}
 	}
 	public static String getNameFromType(short type) {
 		switch (type) {
 			case Node.ELEMENT_NODE:                return "ELEMENT_NODE";
 			case Node.ATTRIBUTE_NODE:              return "ATTRIBUTE_NODE";
 			case Node.TEXT_NODE:                   return "TEXT_NODE";
 			case Node.CDATA_SECTION_NODE:          return "CDATA_SECTION_NODE";
 			case Node.ENTITY_REFERENCE_NODE:       return "ENTITY_REFERENCE_NODE";
 			case Node.ENTITY_NODE:                 return "ENTITY_NODE";
 			case Node.PROCESSING_INSTRUCTION_NODE: return "PROCESSING_INSTRUCTION_NODE";
 			case Node.COMMENT_NODE:                return "COMMENT_NODE";
 			case Node.DOCUMENT_NODE:               return "DOCUMENT_NODE";
 			case Node.DOCUMENT_TYPE_NODE:          return "DOCUMENT_TYPE_NODE";
 			case Node.DOCUMENT_FRAGMENT_NODE:      return "DOCUMENT_FRAGMENT_NODE";
 			case Node.NOTATION_NODE:               return "NOTATION_NODE";
 		}
 		return "UNKNOWN_NODE";
 	}
 	public static abstract class XMLPatchOp {
 		public final XMLPatch owner;
 		public XMLPatchOp(XMLPatch owner) {
 			this.owner = owner;
 		}
 		
 		public abstract void apply(Element config, Node target);
 	}
 	public static class XMLPatchOpPrintValue extends XMLPatchOp {
 		public XMLPatchOpPrintValue(XMLPatch owner) { super(owner); }
 		@Override public void apply(Element config, Node target) {
 			owner.printlnout(0, target.getNodeName()+"("+getNameFromType(target.getNodeType())+") = "+target.getNodeValue());
 		}
 	}
 	public static class XMLPatchOpPrint extends XMLPatchOp {
 		public XMLPatchOpPrint(XMLPatch owner) { super(owner); }
 		@Override public void apply(Element config, Node target) {
 			owner.printlnout(0, target.toString());
 		}
 	}
 	public static class XMLPatchOpSet extends XMLPatchOp {
 		public XMLPatchOpSet(XMLPatch owner) { super(owner); }
 		@Override public void apply(Element config, Node target) {
 			String value = config.getAttribute("value");
 			if (value == null || value.isEmpty()) throw new IllegalArgumentException("Expected 'value' attribute!");
 			
 			String org = target.getNodeValue();
 			target.setNodeValue(value);
 			owner.printlnout(3, "\""+org+"\" > \""+target.getNodeValue()+"\"");
 		}
 	}
 	public static class XMLPatchOpAddSet extends XMLPatchOp {
 		public XMLPatchOpAddSet(XMLPatch owner) { super(owner); }
 		@Override public void apply(Element config, Node target) {
 			String attrS = config.getAttribute("value");
 			if (attrS == null || attrS.isEmpty()) throw new IllegalArgumentException("Expected 'value' attribute!");
 			double value = Double.parseDouble(attrS);
 			
 			double org = Double.parseDouble(target.getNodeValue());
 			target.setNodeValue(Double.toString(org+value));
 			owner.printlnout(3, "\""+org+"\" > \""+target.getNodeValue()+"\"");
 		}
 	}
 	public static class XMLPatchOpSubSet extends XMLPatchOp {
 		public XMLPatchOpSubSet(XMLPatch owner) { super(owner); }
 		@Override public void apply(Element config, Node target) {
 			String attrS = config.getAttribute("value");
 			if (attrS == null || attrS.isEmpty()) throw new IllegalArgumentException("Expected 'value' attribute!");
 			double value = Double.parseDouble(attrS);
 			
 			double org = Double.parseDouble(target.getNodeValue());
 			target.setNodeValue(Double.toString(org-value));
 			owner.printlnout(3, "\""+org+"\" > \""+target.getNodeValue()+"\"");
 		}
 	}
 	public static class XMLPatchOpMulSet extends XMLPatchOp {
 		public XMLPatchOpMulSet(XMLPatch owner) { super(owner); }
 		@Override public void apply(Element config, Node target) {
 			String attrS = config.getAttribute("value");
 			if (attrS == null || attrS.isEmpty()) throw new IllegalArgumentException("Expected 'value' attribute!");
 			double value = Double.parseDouble(attrS);
 			
 			double org = Double.parseDouble(target.getNodeValue());
 			target.setNodeValue(Double.toString(org*value));
 			owner.printlnout(3, "\""+org+"\" > \""+target.getNodeValue()+"\"");
 		}
 	}
 	public static class XMLPatchOpDivSet extends XMLPatchOp {
 		public XMLPatchOpDivSet(XMLPatch owner) { super(owner); }
 		@Override public void apply(Element config, Node target) {
 			String attrS = config.getAttribute("value");
 			if (attrS == null || attrS.isEmpty()) throw new IllegalArgumentException("Expected 'value' attribute!");
 			double value = Double.parseDouble(attrS);
 			
 			double org = Double.parseDouble(target.getNodeValue());
 			target.setNodeValue(Double.toString(org/value));
 			owner.printlnout(3, "\""+org+"\" > \""+target.getNodeValue()+"\"");
 		}
 	}
 	public static class XMLPatchOpFloor extends XMLPatchOp {
 		public XMLPatchOpFloor(XMLPatch owner) { super(owner); }
 		@Override public void apply(Element config, Node target) {
 			boolean direct = false;
 			double sig = 0;
 			String attrS = config.getAttribute("direct");
 			if (attrS != null && !attrS.isEmpty()) direct = Boolean.parseBoolean(attrS);
 			attrS = config.getAttribute("sig");
 			if (attrS != null && !attrS.isEmpty()) sig = Double.parseDouble(attrS);
 			double pow = direct ? sig : Math.pow(10, sig);
 			
 			double org = Double.parseDouble(target.getNodeValue());
 			if (Math.abs(pow-1d)<0.00000001)
 				target.setNodeValue(Integer.toString((int)Math.floor(org)));
 			else
 				target.setNodeValue(Double.toString(Math.floor(org*pow)/pow));
 			owner.printlnout(3, "\""+org+"\" > \""+target.getNodeValue()+"\"");
 		}
 	}
 	public static class XMLPatchOpCeil extends XMLPatchOp {
 		public XMLPatchOpCeil(XMLPatch owner) { super(owner); }
 		@Override public void apply(Element config, Node target) {
 			boolean direct = false;
 			double sig = 0;
 			String attrS = config.getAttribute("direct");
 			if (attrS != null && !attrS.isEmpty()) direct = Boolean.parseBoolean(attrS);
 			attrS = config.getAttribute("sig");
 			if (attrS != null && !attrS.isEmpty()) sig = Double.parseDouble(attrS);
 			double pow = direct ? sig : Math.pow(10, sig);
 			
 			double org = Double.parseDouble(target.getNodeValue());
 			if (Math.abs(pow-1d)<0.00000001)
 				target.setNodeValue(Integer.toString((int)Math.ceil(org)));
 			else
 				target.setNodeValue(Double.toString(Math.ceil(org*pow)/pow));
 			owner.printlnout(3, "\""+org+"\" > \""+target.getNodeValue()+"\"");
 		}
 	}
 	public static class XMLPatchOpRound extends XMLPatchOp {
 		public XMLPatchOpRound(XMLPatch owner) { super(owner); }
 		@Override public void apply(Element config, Node target) {
 			boolean direct = false;
 			double sig = 0;
 			String attrS = config.getAttribute("direct");
 			if (attrS != null && !attrS.isEmpty()) direct = Boolean.parseBoolean(attrS);
 			attrS = config.getAttribute("sig");
 			if (attrS != null && !attrS.isEmpty()) sig = Double.parseDouble(attrS);
 			double pow = direct ? sig : Math.pow(10, sig);
 			
 			double org = Double.parseDouble(target.getNodeValue());
 			if (Math.abs(pow-1d)<0.00000001)
 				target.setNodeValue(Integer.toString((int)Math.round(org)));
 			else
 				target.setNodeValue(Double.toString(Math.round(org*pow)/pow));
 			owner.printlnout(3, "\""+org+"\" > \""+target.getNodeValue()+"\"");
 		}
 	}
 	public static class XMLPatchOpSqrt extends XMLPatchOp {
 		public XMLPatchOpSqrt(XMLPatch owner) { super(owner); }
 		@Override public void apply(Element config, Node target) {
 			double org = Double.parseDouble(target.getNodeValue());
 			target.setNodeValue(Double.toString(Math.sqrt(org)));
 			owner.printlnout(3, "\""+org+"\" > \""+target.getNodeValue()+"\"");
 		}
 	}
 	public static class XMLPatchOpAddAttr extends XMLPatchOp {
 		public XMLPatchOpAddAttr(XMLPatch owner) { super(owner); }
 		@Override public void apply(Element config, Node target) {
 			String name = config.getAttribute("name");
 			String value = config.getAttribute("value");
 			if (name == null || name.isEmpty()) throw new IllegalArgumentException("Expected 'name' attribute!");
 			if (value == null) value = "";
 			
 			if (!(target instanceof Element)) throw new IllegalArgumentException("Can only add Attributes to Elements!");
 			Element elem = (Element) target;
 			
 			elem.setAttribute(name, value);
 			owner.printlnout(3, "+attr \""+name+"\" = \""+value+"\"");
 		}
 	}
 	public static class XMLPatchOpAddElem extends XMLPatchOp {
 		public XMLPatchOpAddElem(XMLPatch owner) { super(owner); }
 		@Override public void apply(Element config, Node target) {
 			String name = config.getAttribute("name");
 			String value = config.getAttribute("value");
 			if (name == null || name.isEmpty()) throw new IllegalArgumentException("Expected 'name' attribute!");
 			if (value == null) value = "";
 			
 			Node child = target.appendChild(target.getOwnerDocument().createElement(name));
 			child.setNodeValue(value);
 			owner.printlnout(3, "+elem \""+name+"\" = \""+value+"\"");
 		}
 	}
 	public static class XMLPatchOpRemove extends XMLPatchOp {
 		public XMLPatchOpRemove(XMLPatch owner) { super(owner); }
 		@Override public void apply(Element config, Node target) {
 			target.getParentNode().removeChild(target);
 			owner.printlnout(3, "removed \""+target.getNodeName()+"\"");
 		}
 	}
 }
