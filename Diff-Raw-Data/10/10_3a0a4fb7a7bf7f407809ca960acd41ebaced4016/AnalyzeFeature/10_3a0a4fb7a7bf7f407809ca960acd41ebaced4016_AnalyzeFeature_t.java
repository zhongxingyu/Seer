 package analyzer;
 
 import backend.storage.IdentifiedFeature;
 import backend.storage.PreprocessorOccurrence;
 import common.NodeTools;
 import common.Preprocessor;
 import common.QueryBuilder;
 import common.Src2Srcml;
 import common.XMLTools;
 import common.xmlTemplates.NewFileTemplate;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.javatuples.Pair;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import refactoring.RefactoringAction;
 import refactoring.RefactoringDocument;
 import refactoring.RefactoringFactory;
 import refactoring.RefactoringStrategy;
 
 public class AnalyzeFeature {
 	private static final String IMPOSSIBLE = "impossible";
 	private static final String UNKNOWN = "unknown";
 	private IdentifiedFeature feature;
 	private AnalyzedFeature analyzedFeature = null;
 	private LinkedList<CriticalOccurrence> crits;
 	private Boolean detectclones;
 	private Boolean providehooknames;
 	private InputStreamReader istreamreader;
 	private BufferedReader bufreader;
 
 	private static String EXPR_NAMES_BELOW_IF_QUERY = "./(following::src:expr[not(contains(.,\"->\"))]/src:name union following::src:expr/src:name[1])";
 
 	private static String EXPR_NAMES_BEFORE_END_QUERY = "./(preceding::src:expr[not(contains(.,\"->\"))]/src:name union preceding::src:expr/src:name[1])";
 
 	private static String EXPR_DECLS_BELOW_IF_QUERY = "./following::src:decl_stmt/src:decl/src:name";
 
 	private static String EXPR_DECLS_BEFORE_END_QUERY = "./preceding::src:decl_stmt/src:decl/src:name";
 
 	private static String FIND_FUNCTION_CONTAINER = "./(ancestor::src:function union ancestor::src:constructor union ancestor::src:destructor)";
 
 	private static String FIND_NEXT_GOTOS = "./following::* intersect //src:goto";
 
 	private static String FIND_NEXT_DEFINE = "./following::* intersect //cpp:define";
 
 	private static String ELSE_BLOCK_BELOW = "./following::src:else";
 
 	private static String ELSE_BLOCK_BEFORE = "./preceding::src:if/src:else";
 
 	private static String CASE_BLOCK_BELOW = "./following::src:case";
 
 	private static String CASE_BLOCK_BEFORE = "./parent::src:case";
 
 	public AnalyzeFeature(IdentifiedFeature feature, Boolean detectclones, Boolean providehooknames) {
 		this.feature = feature;
 		this.detectclones = detectclones;
 		this.providehooknames = providehooknames;
 		this.istreamreader = new InputStreamReader(System.in);
 		this.bufreader = new BufferedReader(istreamreader);
 	}
 
 	public LinkedList<CriticalOccurrence> getCriticalNodes() {
 		return this.crits;
 	}
 
 
 	/**
 	 * This method removes the critical occurence from the document, which is
 	 * replaced by it's expanded versions in expandBlock
 	 * @param occ
 	 * @param n
 	 * @return next sibling of endif node
 	 */
 	private Node removeNodes(CriticalOccurrence occ, Node n) {
 		Node ifdef = occ.getPrepNodes()[0].getNode();
 		Node endif = occ.getPrepNodes()[1].getNode();
 		Node res = endif.getNextSibling();
 		LinkedList<Node> siblings = XMLTools.getSiblings(ifdef);
 
 		if (!siblings.contains(endif))
 			endif.getParentNode().removeChild(endif);
 		n.getParentNode().removeChild(n);
 
 		return res;
 	}
 
 	private void expandBlock(CriticalOccurrence occ, Node n) {
 		LinkedList<String> fnames = Src2Srcml.getConfigurationParameter(n);
 		LinkedList<LinkedList<Boolean>> confs = Preprocessor.combinations(fnames.size());
 		File unprocessedfile = Preprocessor.writeCode2File(occ.getPrepNodes());
 		LinkedList<File> generatedvariants = Preprocessor.runAll(confs, fnames, unprocessedfile);
 
 		LinkedList<File> packedvariants = Src2Srcml.prepareAllFiles(generatedvariants);
 		LinkedList<File> srcmlannovariants = Src2Srcml.runAll(packedvariants);
 		LinkedList<NodeList> disannotatednodes = Src2Srcml.extractNodesFromAll(srcmlannovariants);
 
 		Node importednode = null;
 		Document doc = n.getOwnerDocument();
 		Node pn = n.getParentNode();
 		Node lastaddition = removeNodes(occ, n);
 
 		for (NodeList nl: disannotatednodes) {
 			for (int i = 0; i < nl.getLength(); i++) {
 
 				importednode = doc.importNode(nl.item(i), true);
 
 				// fix linebreaks
 				Node cn = importednode.getLastChild();
 				cn.setTextContent(cn.getTextContent()+"\n");
 
 				if (lastaddition != null)
 					pn.insertBefore(importednode, lastaddition.getNextSibling());
 				else
 					pn.insertBefore(importednode, null);
 				lastaddition = importednode;
 			}
 		}
 	}
 
 	private void expandElseBlock(CriticalOccurrence occ) {
 		expandBlock(occ, occ.getPrepNodes()[0].getNode().getParentNode());
 	}
 
 	private void expandCaseBlock(CriticalOccurrence occ) {
 		expandBlock(occ, occ.getPrepNodes()[0].getNode().getParentNode().getParentNode());
 	}
 
 	public LinkedList<RefactoringDocument> analyze() {
 		LinkedList<RefactoringDocument> modifiedfiles = new LinkedList<RefactoringDocument>();
 		this.analyzedFeature = new AnalyzedFeature();
 		this.analyzedFeature.setName(this.feature.getName());
 
 		Iterator<PreprocessorOccurrence> it = this.feature.iterateOccurrences();
 		this.crits = new LinkedList<CriticalOccurrence>();
 
 		HashMap<String, HashMap<Node, LinkedList<CriticalOccurrence>>> affectedFiles =
 			new HashMap<String, HashMap<Node,LinkedList<CriticalOccurrence>>>();
 		HashMap<String, SimpleHookCloneFinder> cloneMap = new HashMap<String, SimpleHookCloneFinder>();
 		analyzedFeature.setAffectedFiles(affectedFiles);
 
 		while (it.hasNext()) {
 			PreprocessorOccurrence current = (PreprocessorOccurrence) it.next();
 			if ((current.getType().equals(AnalyzeFeature.UNKNOWN))
 					|| (current.getType().equals("HookRefactoring"))) {
 				CriticalOccurrence occ = new CriticalOccurrence(current);
 				boolean isImpossible = false;
 				boolean hasElseBlock = false;
 				boolean hasCaseBlock = false;
 
 				try {
 					Pair<NodeList, NodeList> belse = extractNodeList(occ, ELSE_BLOCK_BELOW, ELSE_BLOCK_BEFORE);
 					Pair<NodeList, NodeList> bcase = extractNodeList(occ, CASE_BLOCK_BELOW, CASE_BLOCK_BEFORE);
 					hasElseBlock = hasElseBlock(belse);
 					hasCaseBlock = hasCaseBlock(bcase);
 
 					if (hasCaseBlock && !current.getType().equals("HookRefactoring")) {
 						expandCaseBlock(occ);
 						modifiedfiles.add(new RefactoringDocument(occ.getDocument()));
 						continue;
 					}
 
 					if (hasElseBlock && !current.getType().equals("HookRefactoring")) {
 						expandElseBlock(occ);
 						modifiedfiles.add(new RefactoringDocument(occ.getDocument()));
 						continue;
 					}
 
 					isImpossible = hasDefines(occ);
 					if (!isImpossible) {
 						isImpossible = hasGoto(occ);
 						if (isImpossible)
 							occ.setType("impossible (local goto)");
 					} else {
 						occ.setType("impossible (local #define)");
 					}
 
 					if (!hasCaseBlock && !hasElseBlock)
 						setContainer(occ);
 				} catch (XPathExpressionException e) {
 					e.printStackTrace();
 				}
 
 				if ((!isImpossible)
 						|| (occ.getContainingFunctionNode() != null)) {
 					HashMap<Node, LinkedList<CriticalOccurrence>> affectedFunctions;
 					if ((affectedFiles.containsKey(occ.getDocFileName()))
 							&& (affectedFiles.get(occ.getDocFileName()) != null)) {
 						affectedFunctions = affectedFiles.get(occ.getDocFileName());
 					} else if ((affectedFiles.containsKey(occ.getDocFileName()))
 							&& (affectedFiles.get(occ.getDocFileName()) == null)) {
 						affectedFunctions = new HashMap<Node, LinkedList<CriticalOccurrence>>();
 						affectedFiles.remove(occ.getDocFileName());
 						affectedFiles.put(occ.getDocFileName(),
 								affectedFunctions);
 					} else {
 						affectedFunctions = new HashMap<Node, LinkedList<CriticalOccurrence>>();
 						affectedFiles.put(occ.getDocFileName(),
 								affectedFunctions);
 					}
 					LinkedList<CriticalOccurrence> occsInFunction;
 					if ((affectedFunctions != null)
 							&& (affectedFunctions.containsKey(occ
 									.getContainingFunctionNode()))) {
 						occsInFunction = affectedFunctions.get(occ
 								.getContainingFunctionNode());
 					} else {
 						if (affectedFunctions == null) {
 							affectedFunctions = new HashMap<Node, LinkedList<CriticalOccurrence>>();
 						}
 						occsInFunction = new LinkedList<CriticalOccurrence>();
 						affectedFunctions.put(occ.getContainingFunctionNode(),
 								occsInFunction);
 					}
 					occsInFunction.add(occ);
 				} else {
 					occ.setType(AnalyzeFeature.IMPOSSIBLE);
 				}
 
 				if ((!occ.getType().startsWith(AnalyzeFeature.IMPOSSIBLE))
 						&& (!occ.getType().startsWith(AnalyzeFeature.UNKNOWN))) {
 					try {
 						occ.setCritNodeType(RefactoringStrategy.SIMPLE_HOOK);
 						if (cloneMap.containsKey(occ.getDocFileName())) {
 							((SimpleHookCloneFinder) cloneMap.get(occ
 									.getDocFileName())).doDuplicateCheck(occ);
 							if (occ.getDupe() == null) {
 								checkDependencies(occ);
 							}
 							occ.setDupe(null);
 						} else {
 							SimpleHookCloneFinder scf = new SimpleHookCloneFinder();
 							cloneMap.put(occ.getDocFileName(), scf);
 							checkDependencies(occ);
 							scf.doDuplicateCheck(occ);
 						}
 
 					} catch (XPathExpressionException e) {
 						e.printStackTrace();
 					}
 				}
 				this.crits.add(occ);
 				this.analyzedFeature.addOccurrence(occ);
 			} else {
 				if (!affectedFiles.containsKey(current.getDocFileName())) {
 					affectedFiles.put(current.getDocFileName(), null);
 				}
 				this.analyzedFeature.addOccurrence(current);
 			}
 		}
 		return modifiedfiles;
 	}
 
 	private Pair<NodeList, NodeList> extractNodeList(CriticalOccurrence occ, String uperbound, String lowerbound) throws XPathExpressionException {
 		XPathExpression ub = QueryBuilder.instance()
 				.getExpression(uperbound);
 		XPathExpression lb = QueryBuilder.instance()
 				.getExpression(lowerbound);
 		NodeList ubl = (NodeList) ub.evaluate(
 				occ.getPrepNodes()[0].getNode(), XPathConstants.NODESET);
 		NodeList lbl = (NodeList) lb.evaluate(
 				occ.getPrepNodes()[(occ.getPrepNodes().length -1)].getNode(),
 				XPathConstants.NODESET);
 
 		return new Pair<NodeList, NodeList>(ubl, lbl);
 
 	}
 
 	private boolean hasDefines(CriticalOccurrence occ)
 			throws XPathExpressionException {
 		XPathExpression expr = QueryBuilder.instance().getExpression(
 				FIND_NEXT_DEFINE);
 
 		NodeList upper = (NodeList) expr.evaluate(
 				occ.getPrepNodes()[0].getNode(), XPathConstants.NODESET);
 		NodeList lower = (NodeList) expr.evaluate(
 				occ.getPrepNodes()[(occ.getPrepNodes().length - 1)].getNode(),
 				XPathConstants.NODESET);
 
 		return lower.item(0) != upper.item(0);
 	}
 
 	private boolean hasCaseBlock(Pair<NodeList, NodeList> b) {
 
 		if (b.getValue0().getLength() > 0
 				&& b.getValue1().getLength() > 0)
 			return b.getValue0().item(0) == b.getValue1().item(0);
 		else
 			return false;
 	}
 
 	private boolean hasElseBlock(Pair<NodeList, NodeList> b) {
 
 		if (b.getValue0().getLength() > 0
 				&& b.getValue1().getLength() > 0)
 			return b.getValue0().item(0) == b.getValue1().item(0);
 		else
 			return false;
 	}
 
 	private boolean hasGoto(CriticalOccurrence occ)
 			throws XPathExpressionException {
 		XPathExpression expr = QueryBuilder.instance().getExpression(
 				FIND_NEXT_GOTOS);
 
 		NodeList upper = (NodeList) expr.evaluate(
 				occ.getPrepNodes()[0].getNode(), XPathConstants.NODESET);
 		NodeList lower = (NodeList) expr.evaluate(
 				occ.getPrepNodes()[(occ.getPrepNodes().length - 1)].getNode(),
 				XPathConstants.NODESET);
 
 		return (lower.getLength() != 0) && (upper.getLength() != 0)
 				&& (lower.item(0) != upper.item(0));
 	}
 
 	private void setContainer(CriticalOccurrence occ)
 			throws XPathExpressionException {
 		String[] functionCheck = new String[occ.getPrepNodes().length];
 		Node functionNode = null;
 		for (int i = 0; i < occ.getPrepNodes().length; i++) {
 			XPathExpression expr = QueryBuilder.instance().getExpression(
 					FIND_FUNCTION_CONTAINER);
 			functionNode = (Node) expr.evaluate(
 					occ.getPrepNodes()[i].getNode(), XPathConstants.NODE);
 			if (functionNode == null) {
 				System.out.println("Null-Function? " + occ.getDocFileName()
 						+ " at line " + occ.getPrepNodes()[i].getLineNumber());
 				occ.setType("impossible");
 			} else {
 				Node functionChild = functionNode.getFirstChild();
 				StringBuilder functionString = new StringBuilder("");
 				while ((!functionChild.getNodeName().equalsIgnoreCase("block"))
 						&& (functionChild != null)) {
 					functionString.append(functionChild.getTextContent());
 					functionChild = functionChild.getNextSibling();
 				}
 				functionCheck[i] = functionString.toString();
 			}
 		}
 
 		boolean allNodesInSameFunction = functionCheck.length > 1;
 		for (int i = 1; i < functionCheck.length; i++) {
 			allNodesInSameFunction &= ((functionCheck[i] != null) && (functionCheck[0]
 					.equals(functionCheck[i])));
 		}
 
 		if (allNodesInSameFunction)
 			occ.setContainingFunctionNode(functionNode);
 	}
 
 	private void checkDependencies(CriticalOccurrence occ)
 			throws XPathExpressionException {
 		XPathExpression varUsageBelowIfExpr = QueryBuilder.instance()
 				.getExpression(EXPR_NAMES_BELOW_IF_QUERY);
 		XPathExpression varUsageBeforeEndExpr = QueryBuilder.instance()
 				.getExpression(EXPR_NAMES_BEFORE_END_QUERY);
 		NodeList belowIfList = (NodeList) varUsageBelowIfExpr.evaluate(
 				occ.getPrepNodes()[0].getNode(), XPathConstants.NODESET);
 		NodeList beforeEndList = (NodeList) varUsageBeforeEndExpr.evaluate(
 				occ.getPrepNodes()[(occ.getPrepNodes().length - 1)].getNode(),
 				XPathConstants.NODESET);
 
 		NodeList exprList = NodeTools.intersectUpperLower(belowIfList,
 				beforeEndList);
 
 		XPathExpression varDeclBelowIfExpr = QueryBuilder.instance()
 				.getExpression(EXPR_DECLS_BELOW_IF_QUERY);
 		XPathExpression varDeclBeforeEndExpr = QueryBuilder.instance()
 				.getExpression(EXPR_DECLS_BEFORE_END_QUERY);
 		NodeList declBelowIfList = (NodeList) varDeclBelowIfExpr.evaluate(
 				occ.getPrepNodes()[0].getNode(), XPathConstants.NODESET);
 		NodeList declBeforeEndList = (NodeList) varDeclBeforeEndExpr.evaluate(
 				occ.getPrepNodes()[(occ.getPrepNodes().length - 1)].getNode(),
 				XPathConstants.NODESET);
 		NodeList declList = NodeTools.intersectUpperLower(declBelowIfList,
 				declBeforeEndList);
 		if (declList.getLength() > 0) {
 			occ.setType("impossible (local declaration)");
 		}
 
 		LinkedList<String> ownLocalVariables = new LinkedList<String>();
 		HashSet<String> declNames = new HashSet<String>();
 		for (int i = 0; i < declList.getLength(); i++) {
 			declNames.add(declList.item(i).getTextContent());
 			ownLocalVariables.add(declList.item(i).getTextContent());
 		}
 
 		LinkedList<String> localVariableDependencies = new LinkedList<String>();
 		LinkedList<String> parameterDependencies = new LinkedList<String>();
 		HashSet<String> duplicateEliminator = new HashSet<String>();
 		for (int i = 0; i < exprList.getLength(); i++) {
 			String varName = exprList.item(i).getTextContent();
 
 			if (declNames.contains(varName)) {
 				occ.setType("impossible (local declaration)");
 			} else if ((!duplicateEliminator.contains(varName))
 					&& (isLocalVar(occ.getPrepNodes()[0].getNode(), varName))) {
 				localVariableDependencies.add(varName);
 				duplicateEliminator.add(varName);
 			} else {
 				if ((duplicateEliminator.contains(varName))
 						|| (!isParameter(occ.getPrepNodes()[0].getNode(),
 								varName)))
 					continue;
 				parameterDependencies.add(varName);
 				duplicateEliminator.add(varName);
 			}
 		}
 		occ.setLocalVariableDependencies(localVariableDependencies);
 		occ.setParameterDependencies(parameterDependencies);
 		occ.setOwnLocalVariables(ownLocalVariables);
 	}
 
 	private boolean isLocalVar(Node ctxNode, String varName) {
 		boolean result = false;
 		try {
 			XPathExpression expr = QueryBuilder
 					.instance()
 					.getExpression(
 							"./ancestor::src:function/src:block/descendant::src:decl_stmt/src:decl[src:name = \""
 									+ varName + "\"]");
 			result = ((NodeList) expr.evaluate(ctxNode, XPathConstants.NODESET))
 					.getLength() > 0;
 		} catch (XPathExpressionException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	private boolean isParameter(Node ctxNode, String varName) {
 		boolean result = false;
 		try {
 			XPathExpression expr = QueryBuilder.instance().getExpression(
 					"./ancestor::src:function/src:parameter_list/src:param/src:decl[src:name=\""
 							+ varName + "\"]");
 			result = ((NodeList) expr.evaluate(ctxNode, XPathConstants.NODESET))
 					.getLength() > 0;
 		} catch (XPathExpressionException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	public LinkedList<RefactoringDocument> refactor() throws SAXException,
 			IOException, ParserConfigurationException {
 		if (this.analyzedFeature == null) {
 			analyze();
 		}
 		HashMap<String, RefactoringDocument> posfiles =
 			new HashMap<String, RefactoringDocument>();
 		HashMap<String, RefactoringDocument> negfiles =
 			new HashMap<String, RefactoringDocument>();
 		Iterator<PreprocessorOccurrence> it = analyzedFeature.iterateOccurrences();
 
 		LinkedList<RefactoringDocument> modifiedFiles = new LinkedList<RefactoringDocument>();
 		setHookNames();
 		while (it.hasNext()) {
 			PreprocessorOccurrence occ = (PreprocessorOccurrence) it.next();
 			RefactoringAction action = RefactoringFactory.instance().getAction(
 					occ.getType());
 			if (action == null)
 				continue;
 			RefactoringDocument pos;
 			if (posfiles.containsKey(occ.getDocFileName())) {
 				pos = (RefactoringDocument) posfiles.get(occ.getDocFileName());
 			} else {
 				pos = NewFileTemplate.instantiate();
 				posfiles.put(occ.getDocFileName(), pos);
 				try {
 					((NewFileTemplate) pos).setClassName(occ.getDocument());
 				} catch (XPathExpressionException e) {
 					e.printStackTrace();
 				}
 			}
 			RefactoringDocument neg;
 			if (negfiles.containsKey(occ.getDocFileName())) {
 				neg = (RefactoringDocument) negfiles.get(occ.getDocFileName());
 			} else {
 				neg = NewFileTemplate.instantiate();
 				negfiles.put(occ.getDocFileName(), neg);
 				try {
 					((NewFileTemplate) neg).setClassName(occ.getDocument());
 				} catch (XPathExpressionException e) {
 					e.printStackTrace();
 				}
 			}
 
 			action.doRefactoring(occ.getDocument(), pos, neg, occ,
 					this.analyzedFeature.getName());
 
 			File docFile = new File(occ.getDocFileName());
 			String docName = docFile.getName();
 			if ((pos.getModified()) || (neg.getModified())) {
 				RefactoringDocument doc = new RefactoringDocument(
 						occ.getDocument());
 				doc.setFeatureName(docFile.getParentFile().getName());
 				modifiedFiles.add(doc);
 				doc.setFileName(docName);
 			}
 			if (pos.getModified()) {
 				modifiedFiles.add(pos);
 				pos.setFileName(docName);
 			}
 			if (neg.getModified()) {
 				modifiedFiles.add(neg);
 				neg.setFileName(docName);
 			}
 
 		}
 
 		return modifiedFiles;
 	}
 
 	private void setHookNames() {
 		HashMap<String, HashMap<Node, LinkedList<CriticalOccurrence>>> files =
 			analyzedFeature.getAffectedFiles();
 
 		for (String key : files.keySet()) {
 			SimpleHookCloneFinder cloneFinder = new SimpleHookCloneFinder();
 			int counter = 0;
 			int clonecounter = 0;
 			HashMap<Node, LinkedList<CriticalOccurrence>> nodes = files.get(key);
 			if (nodes != null)
 				for (Node node : nodes.keySet()) {
 					for (CriticalOccurrence occ : files.get(key).get(node)) {
 						if ((!occ.getType().equals("HookRefactoring"))
 								|| ((occ.getHookFunctionName() != null) && (occ
 										.getHookFunctionName().length() != 0))
 								|| ((occ.getCritNodeType() != RefactoringStrategy.SIMPLE_HOOK)
 										&& (occ.getCritNodeType() != RefactoringStrategy.BLOCK_REPLICATION_WITH_HOOK) && (occ
 										.getCritNodeType() != RefactoringStrategy.STATEMENT_REPLICATION_WITH_HOOK)))
 							continue;
 
 						if (detectclones)
 							cloneFinder.doDuplicateCheck(occ);
 
 						if (occ.getDupe() == null) {
 							String name = key.substring(key.lastIndexOf('\\') + 1);
 							name = name.substring(0, name.indexOf('.'));
 
 							if (providehooknames) {
 								try {
 									System.out.print("hookname for " + name + " -- " + occ.getDocFileName() + ": ");
 									String hookname = bufreader.readLine();
 									occ.setHookFunctionName(hookname);
 								} catch (IOException e) {
 									e.printStackTrace();
 								}
 							} else {
 								counter++;
 								occ.setHookFunctionName(name + "HookFunction" + counter);
 							}
 						} else {
 							clonecounter++;
 							System.out.println("CLONES: " + clonecounter);
 							occ.setHookFunctionName(occ.getDupe().getHookFunctionName());
 						}
 					}
 				}
 		}
 	}
 
 	public AnalyzedFeature getAnalyzedFeature() {
 		return this.analyzedFeature;
 	}
 }
