 /*
  * Thibaut Colar Aug 18, 2009
  */
 package net.colar.netbeans.fan.completion;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Member;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 import javax.swing.text.Document;
 import javax.swing.text.JTextComponent;
 import net.colar.netbeans.fan.FanParserTask;
 import net.colar.netbeans.fan.FanUtilities;
 import net.colar.netbeans.fan.indexer.FanIndexer;
 import net.colar.netbeans.fan.indexer.FanIndexerFactory;
 import net.colar.netbeans.fan.indexer.FanJarsIndexer;
 import net.colar.netbeans.fan.types.FanResolvedType;
 import net.colar.netbeans.fan.indexer.model.FanSlot;
 import net.colar.netbeans.fan.indexer.model.FanType;
 import net.colar.netbeans.fan.parboiled.AstKind;
 import net.colar.netbeans.fan.parboiled.AstNode;
 import net.colar.netbeans.fan.parboiled.FanLexAstUtils;
 import net.colar.netbeans.fan.parboiled.pred.NodeKindPredicate;
 import net.colar.netbeans.fan.scope.FanAstScopeVarBase;
 import net.colar.netbeans.fan.structure.FanBasicElementHandle;
 import org.netbeans.modules.csl.api.CodeCompletionContext;
 import org.netbeans.modules.csl.api.CodeCompletionHandler;
 import org.netbeans.modules.csl.api.CodeCompletionResult;
 import org.netbeans.modules.csl.api.CompletionProposal;
 import org.netbeans.modules.csl.api.ElementHandle;
 import org.netbeans.modules.csl.api.ParameterInfo;
 import org.netbeans.modules.csl.spi.DefaultCompletionResult;
 import org.netbeans.modules.csl.spi.ParserResult;
 import org.openide.filesystems.FileObject;
 
 /**
  * Code Completion
  * @author thibautc
  */
 public class FanCompletionHandler implements CodeCompletionHandler
 {
 
 	private static enum DocTypes
 	{
 
 		NA,
 		POD,
 		TYPE,
 		SLOT,
 	};
 	DocTypes docType = DocTypes.NA;
 	String preamble = "";
 	public static final String[] ROOT_ITEMS =
 	{
 		"class", "mixin", "enum", "public", "internal", "abstract", "final", "const", "using"
 	};
 
 	@Override
 	public CodeCompletionResult complete(CodeCompletionContext context)
 	{
 		ArrayList<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
 
 		try
 		{
 			String prefix = context.getPrefix();
 			if (prefix == null)
 			{
 				prefix = "";
 			}
 
 			FanCompletionContext cpl = new FanCompletionContext(context);
 			int anchor = context.getCaretOffset();
 			preamble = cpl.getPreamble();
 			FanUtilities.GENERIC_LOGGER.debug("preamb: " + preamble);
 			FanParserTask result = (FanParserTask) context.getParserResult();
 
 			AstNode rootNode = result.getAstTree();
 			int offset = context.getCaretOffset();
 			AstNode curNode = FanLexAstUtils.findASTNodeAt(rootNode, offset);
 
 			System.out.println("compl context: " + cpl.getCompletionType() + " preamb:" + preamble);
 
 			switch (cpl.getCompletionType())
 			{
 				case ROOT_LEVEL:
 					proposeRootItems(proposals, anchor, prefix.toLowerCase());
 					break;
 				case IMPORT_POD:
 					proposeUsing(proposals, context, curNode);
 					break;
 				case ID:
 					proposeVars(proposals, context, prefix, curNode);
 					docType = DocTypes.TYPE;
 					break;
 				case CALL:
 					proposeCalls(proposals, context, curNode);
 					break;
 			}
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 			FanParserTask result = (FanParserTask) context.getParserResult();
 			if (result != null)
 			{
 				result.addError("Completion error", e);
 			}
 		}
 
 		DefaultCompletionResult completionResult = new DefaultCompletionResult(proposals, false);
 		return completionResult;
 	}
 
 	/*private void addtoTypeProposals(ArrayList<CompletionProposal> proposals, Collection<? extends IndexResult> findRootTypes)
 	{
 	Iterator it = findRootTypes.iterator();
 	while (it.hasNext())
 	{
 	IndexResult result = (IndexResult) it.next();
 	String txt = result.getValue(FanIndexer.INDEX_CLASS);
 	System.out.println("Prop: " + txt);
 	if (txt != null)
 	{
 	String type = txt.substring(0, txt.indexOf(';')).trim();
 	String pod = findPod(result.getFile());
 
 	proposals.add(new FanTypeProposal(type, 0, pod));
 	}
 	}
 	}*/
 	@Override
 	public String document(ParserResult result, ElementHandle handle)
 	{
 		String doc = null;
 		switch (docType)
 		{
 			case POD:
 				doc = FanIndexer.getPodDoc(handle.getName());
 				break;
 			case TYPE:
 				doc = ((FanBasicElementHandle) handle).getDoc();
 				break;
 			case SLOT:
 				doc = ((FanBasicElementHandle) handle).getDoc();
 				break;
 		}
 		return doc;
 	}
 
 	public String findPod(FileObject file)
 	{
 		return FanUtilities.getPodForPath(file.getPath());
 	}
 
 	@Override
 	public ElementHandle resolveLink(String link, ElementHandle handle)
 	{
 		//TODO: resolve links
 		/*System.out.println("Resolve link: "+link);
 		link="sys.File";
 		return new ElementHandle.UrlHandle(link);*/
 		return null;
 	}
 
 	@Override
 	public String getPrefix(ParserResult result, int arg1, boolean arg2)
 	{
 		return null;
 	}
 
 	@Override
 	public QueryType getAutoQuery(JTextComponent comp, String arg1)
 	{
 		return QueryType.NONE;
 	}
 
 	@Override
 	public String resolveTemplateVariable(String arg0, ParserResult result, int arg2, String arg3, Map arg4)
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public Set<String> getApplicableTemplates(ParserResult result, int arg1, int arg2)
 	{
 		return (Set<String>) Collections.EMPTY_SET;
 	}
 
 	@Override
 	public ParameterInfo parameters(ParserResult result, int arg1, CompletionProposal proposal)
 	{
 		return ParameterInfo.NONE;
 	}
 
 	/**
 	Root level (outside class/mixin/enum)
 	propose slot begining keywords (class,enum,mixin , public, abstract and so on)
 	also propose imports items (if within "using ...")
 	proposeImports(proposals);
 	 */
 	private void proposeRootItems(ArrayList<CompletionProposal> proposals, int anchor, String prefix)
 	{
 		for (String item : ROOT_ITEMS)
 		{
 			{
 				proposals.add(new FanKeywordProposal(item, anchor - prefix.length()));
 			}
 		}
 	}
 
 	/**
 	 * Complete imports (using)
 	 * If not in an import do nothing and return false
 	 * Forms of imports:
 	 * using id('. id)* eos    // whole pod
 	 * using id('. id)* '::' id eos   // Particular type
 	 * using ([id])? id('.' id)* '::' id eos  // FFI
 	 * using ([id])? id('.' id)* '::' (id | '$') as id eos  // with 'as'
 	 * @param proposals
 	 * @param anchor
 	 * @return
 	 */
 	private void proposePods(ArrayList<CompletionProposal> proposals, int anchor, String prefix)
 	{
 		Vector<String> names = FanType.findAllPodNames();
 		if (prefix.length() == 0)
 		{
 			proposals.add(new FanImportProposal("[java] ", anchor - prefix.length(), true));
 		}
 		for (String name : names)
 		{
 			if (name.toLowerCase().startsWith(prefix))
 			{
 				proposals.add(new FanImportProposal(name, anchor - prefix.length(), false));
 				docType = DocTypes.POD;
 			}
 		}
 	}
 
 	/**
 	 * Propose Types (class, enum, mixin)
 	 * @param podName null means all
 	 * @param proposals
 	 * @param anchor
 	 * @param prefix
 	 */
 	private void proposeTypes(String podName, ArrayList<CompletionProposal> proposals, int anchor, String prefix)
 	{
 		Vector<FanType> types;
 		if (podName == null)
 		{
 			types = FanType.findAllFantomTypes(prefix);
 		} else
 		{
 			types = FanType.findPodTypes(podName, prefix);
 		}
 		for (FanType type : types)
 		{
 			// TODO: filter out internals / private ?
 			proposals.add(new FanTypeProposal(type, anchor - prefix.length(), null));
 		}
 		docType = DocTypes.TYPE;
 	}
 
 	/**
 	 * Propose slots(functions, methods etc...) of a specific types.
 	 * @param pod
 	 * @param type
 	 * @param proposals
 	 * @param anchor
 	 * @param prefix
 	 */
 	private void proposeSlots(FanResolvedType type, ArrayList<CompletionProposal> proposals, int anchor, String prefix)
 	{
 		if (type.getDbType().isJava())
 		{
 			FanJarsIndexer indexer = FanIndexerFactory.getJavaIndexer();
 			List<Member> slots = indexer.findTypeSlots(type.getDbType().getQualifiedName());
 			for (Member slot : slots)
 			{
 				if (slot.getName().toLowerCase().startsWith(prefix))
 				{
 					boolean isStatic = Modifier.isStatic(slot.getModifiers()) || (slot instanceof Constructor);
 					if (type.isStaticContext() == isStatic)
 					{
 						proposals.add(new FanSlotProposal(slot, anchor - prefix.length()));
 						// TODO: javadoc for types/slots
 						docType = DocTypes.NA;
 					}
 				}
 			}
 		} else
 		{
 			Vector<FanSlot> slots = FanSlot.getAllSlotsForType(type.getDbType().getQualifiedName(), true);
 			for (FanSlot slot : slots)
 			{
 				if (slot.getName().toLowerCase().startsWith(prefix))
 				{
 					// constructor are not marked as static ... but fot this purpose they are
 					boolean isStatic = slot.isStatic() || slot.isCtor();
 					if (type.isStaticContext() == isStatic)
 					{
 						proposals.add(new FanSlotProposal(slot, anchor - prefix.length()));
 						docType = DocTypes.SLOT;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Java packages
 	 * @param proposals
 	 * @param anchor
 	 * @param pod
 	 */
 	private void proposeJavaPacks(ArrayList<CompletionProposal> proposals, int anchor, String basePack)
 	{
 		List<String> items = FanIndexerFactory.getJavaIndexer().listSubPackages(basePack);
 		for (String s : items)
 		{
 			FanUtilities.GENERIC_LOGGER.debug("Proposal: " + s);
 			proposals.add(new FanImportProposal(s, anchor - basePack.length(), true));
 		}
 	}
 
 	/**
 	 *
 	 * @param proposals
 	 * @param anchor
 	 * @param basePack   null means from any package
 	 * @param type  type (starts with "[java]")
 	 */
 	private void proposeJavaTypes(ArrayList<CompletionProposal> proposals, int anchor, String basePack, String type)
 	{
 		List<String> items = FanIndexerFactory.getJavaIndexer().listTypes(basePack, type);
 		for (String s : items)
 		{
 			FanUtilities.GENERIC_LOGGER.debug("Proposal: " + s);
 			proposals.add(new FanImportProposal(s, anchor - type.length(), true));
 		}
 	}
 
 	/**
 	 * Propose defined types (fan.sys) + whatever listed in using + current pod
 	 */
 	private void proposeDefinedTypes(ArrayList<CompletionProposal> proposals, int anchor, String prefix/*, FanRootScope rootScope*/)
 	{
 		/*ArrayList<CompletionProposal> props = new ArrayList<CompletionProposal>();
 		Hashtable<String, FanResolvedType> usings = rootScope.getUsing();
 		for (String key : usings.keySet())
 		{
 		if (key.startsWith(prefix))
 		{
 		FanResolvedType type = usings.get(key);
 		if (type.isResolved())
 		{
 		props.add(new FanTypeProposal(type.getDbType(), anchor - prefix.length(), key));
 		}
 		}
 		}
 		// propose all the types from the same pod (don't need a using)
 		proposeTypes(rootScope.getPod(), props, anchor, prefix);
 		// propose sys types, don't need a 'using'
 		proposeTypes("sys", props, anchor, prefix);
 		proposals.addAll(props);*/
 	}
 
 	// TODO: setup nice icons(package/class etc..) in importproposals
 	private void proposeUsing(ArrayList<CompletionProposal> proposals, CodeCompletionContext context, AstNode node)
 	{
 		if (node.getKind() == AstKind.AST_USING_AS)
 		{
 			return; // no completion after "as"
 		}
 		FanParserTask result = (FanParserTask) context.getParserResult();
 		Document doc = result.getSnapshot().getSource().getDocument(true);
 		AstNode curNode = FanLexAstUtils.findParentNode(node, AstKind.AST_USING);
 		int anchor = context.getCaretOffset();
 		if (curNode == null)
 		{
 			curNode = FanLexAstUtils.findParentNode(node, AstKind.AST_INC_USING);
 		}
 		if (curNode == null)
 		{
 			return;
 		}
 		AstNode idNode = FanLexAstUtils.getFirstChild(curNode, new NodeKindPredicate(AstKind.AST_ID));
 		AstNode ffi = FanLexAstUtils.getFirstChild(curNode, new NodeKindPredicate(AstKind.AST_USING_FFI));
 		AstNode as = FanLexAstUtils.getFirstChild(curNode, new NodeKindPredicate(AstKind.AST_USING_AS));
 		String id = "";
 		if (idNode != null)
 		{
 			id = idNode.getNodeText(true);
 		}
 
 		FanUtilities.GENERIC_LOGGER.debug("Using ID: " + id);
 		String pod = null;
 		if (id.indexOf("::") != -1)
 		{
 			pod = id.substring(0, id.indexOf("::"));
 			id = id.substring(id.indexOf("::") + 2);
 		}
 
 		if (ffi != null)
 		{
 			String ffiText = ffi.getNodeText(true);
 			if (ffiText.equals("java"))
 			{
 				if (pod == null)
 				{
 					proposeJavaPacks(proposals, anchor, id);
 				} else
 				{
 					proposeJavaTypes(proposals, anchor, pod, id);
 				}
 			}
 		} else
 		{
 			if (pod == null)
 			{
 				proposePods(proposals, anchor, id);
 			} else
 			{
 				proposeTypes(pod, proposals, anchor, id);
 			}
 		}
 
 	}
 
 	/**
 	 * Propose options for a DOT_CALL ex:
 	 * SomeClass._
 	 * SomeClass.get_
 	 * object.method()._
 	 * Window{title="a"}._
 	 * @param proposals
 	 * @param context
 	 */
 	private void proposeCalls(ArrayList<CompletionProposal> proposals, CodeCompletionContext context, AstNode node)
 	{
 		AstNode expr = FanLexAstUtils.findParentNode(node, AstKind.AST_EXPR);
 		AstNode callNode = FanLexAstUtils.findParentNodeWithin(node, AstKind.AST_CALL, expr);
 		if (callNode == null)
 		{
 			callNode = FanLexAstUtils.findParentNodeWithin(node, AstKind.AST_INC_CALL, expr);
 		}
 		// find the type of the item on the LHS of the call.
 		FanResolvedType type = FanResolvedType.makeUnresolved(node);
 		for(AstNode child : expr.getChildren())
 		{
 			// If we foud the call node, stop here (keep the previosu node type)
 			if(child.getStartLocation().getIndex() == callNode.getStartLocation().getIndex())
 				break;
 			if(child.getType()!=null && child.getType().isResolved())
 				type = child.getType();
 		}
 		String txt = callNode.getNodeText(true);
 		System.out.println("Call text: " + txt+" type: "+type);
 		int offset = context.getCaretOffset();
 		String prefix = txt;
 		int idx = prefix.indexOf(".");
 		if(idx>=0)
 		{
 			prefix = prefix.substring(idx+1);
 			//offset += idx;
 		}
 		if (type!=null && type.isResolved())
 		{
 			proposeSlots(type, proposals, offset, prefix);
 		}
 	}
 
 	private void proposeVars(ArrayList<CompletionProposal> proposals, CodeCompletionContext context, String prefix, AstNode node)
 	{
 		for (FanAstScopeVarBase var : node.getAllScopeVars().values())
 		{
 			if (var.getName().startsWith(prefix))
 			{
 				FanVarProposal prop = new FanVarProposal(var, context.getCaretOffset() - prefix.length());
 				proposals.add(prop);
 			}
 		}
 	}
 }
