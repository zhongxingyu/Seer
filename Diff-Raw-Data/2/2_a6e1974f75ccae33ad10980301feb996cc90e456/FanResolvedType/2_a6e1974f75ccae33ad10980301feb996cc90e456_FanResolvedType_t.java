 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.types;
 
 import java.lang.reflect.Member;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Vector;
 import net.colar.netbeans.fan.FanParserTask;
 import net.colar.netbeans.fan.FanUtilities;
 import net.colar.netbeans.fan.indexer.FanIndexer;
 import net.colar.netbeans.fan.indexer.FanIndexerFactory;
 import net.colar.netbeans.fan.indexer.model.FanSlot;
 import net.colar.netbeans.fan.indexer.model.FanType;
 import net.colar.netbeans.fan.parboiled.AstNode;
 import net.colar.netbeans.fan.scope.FanAstScopeVarBase;
 
 /**
  * Resolved type
  * Store infos of a resolved type
  * Also contains the factory methods / logic to resolve a type expr.
  * @author tcolar
  */
 public class FanResolvedType
 {
 
 	private final String asTypedType;
 	private final FanType dbType;
 	// whether it's used in a  nullable context : ex: Str? vs Str
 	private boolean nullableContext = false;
 	// whether it's used in a static context: ex Str. vs str.
 	private boolean staticContext = false;
 	private final String shortAsTypedType;
 	private final AstNode scopeNode;
 
 	protected FanResolvedType(AstNode scopeNode, String enteredType, FanType type)
 	{
 		this.scopeNode = scopeNode;
 		this.asTypedType = enteredType;
 		shortAsTypedType = asTypedType.indexOf("::") != -1 ? asTypedType.substring(asTypedType.indexOf("::") + 2) : asTypedType;
 		dbType = type;
 	}
 
 	public boolean isResolved()
 	{
 		return dbType != null;
 	}
 
 	public FanType getDbType()
 	{
 		return dbType;
 	}
 
 	public String getAsTypedType()
 	{
 		return asTypedType;
 	}
 
 	public String getShortAsTypedType()
 	{
 		return shortAsTypedType;
 	}
 
 	@Override
 	public String toString()
 	{
 		StringBuilder sb = new StringBuilder(asTypedType).append(" resolved:").append(isResolved());
 		return sb.toString();
 	}
 
 	public static FanResolvedType makeUnresolved(AstNode node)
 	{
 		return new FanResolvedType(node, FanIndexer.UNRESOLVED_TYPE, null);
 	}
 
 	public static FanResolvedType makeFromTypeSigWithWarning(AstNode node)
 	{
 		FanResolvedType result = fromTypeSig(node, node.getNodeText(true));
 		if (!result.isResolved())
 		{
 			String type = node.getNodeText(true);
 			//TODO: Propose to auto-add using statements (Hints)
 			node.getRoot().getParserTask().addError("Unresolved type: " + type, node);
 			FanUtilities.GENERIC_LOGGER.info("Could not resolve type: " + (node == null ? "null" : node.toString()));
 		}
 		return result;
 	}
 
 	/*public static FanResolvedType makeFromTypeSig(AstNode node)
 	{
 	return fromTypeSig(node.getNodeText(true));
 	}*/
 	/**
 	 * Resolve a type (type signature)
 	 * Recursive
 	 * @param scope
 	 * @param node
 	 * @return
 	 */
 	/*public static FanResolvedType makeFromTypeSig(AstNode node)
 	{
 	return makeFromTypeSig(node, FanResolvedType.makeUnresolved());
 	}
 
 	private static FanResolvedType makeFromTypeSig(AstNode node, FanResolvedType baseType)
 	{
 	if (node == null)
 	{
 	return baseType;
 	}
 	//FanRootScope root = scope.getRoot();
 
 	//System.out.println("Node Type: " + node.toStringTree());
 
 	switch (node.getType())
 	{
 	// type is just a wrapper node
 	case FanParser.AST_TYPE:
 	case FanParser.AST_CHILD:
 	for (CommonTree n : (List<CommonTree>) node.getChildren())
 	{
 	baseType = makeFromTypeSig(scope, n, baseType);
 	}
 	break;
 	case FanParser.AST_ID:
 	String typeText = FanLexAstUtils.getNodeContent(scope.getRoot().getParserResult(), node);
 	baseType = root.lookupUsing(typeText);
 	break;
 	case FanParser.SP_QMARK:
 	baseType.setNullableContext(true);
 	case FanParser.LIST_TYPE:
 	baseType = new FanResolvedListType(baseType);
 	break;
 	case FanParser.AST_MAP:
 	if (node.getChildCount() >= 2)
 	{
 	CommonTree keyNode = (CommonTree) node.getChild(0);
 	CommonTree valueNode = (CommonTree) node.getChild(1);
 	FanResolvedType keyType = makeFromTypeSig(scope, keyNode);
 	FanResolvedType valueType = makeFromTypeSig(scope, valueNode);
 	if (keyType.isResolved() && valueType.isResolved())
 	{
 	baseType = new FanResolvedMapType(keyType, valueType);
 	}
 	}
 	break;
 	case FanParser.AST_FUNC_TYPE:
 	FanResolvedType returnType = null;
 	Vector<FanResolvedType> types = new Vector<FanResolvedType>();
 	boolean passedArrow = false;
 	for (CommonTree n : (List<CommonTree>) node.getChildren())
 	{
 	switch (n.getType())
 	{
 	case FanParser.OP_ARROW:
 	passedArrow = true;
 	break;
 	case FanParser.AST_TYPE:
 	if (!passedArrow)
 	{
 	types.add(makeFromTypeSig(scope, n));
 	} else
 	{
 	returnType = makeFromTypeSig(scope, n);
 	}
 	}
 	}
 
 	baseType = new FanResolvedFuncType(types, returnType);
 	break;
 	default:
 	FanUtilities.GENERIC_LOGGER.info("Unexpected item during type parsing" + node.toStringTree());
 	baseType = makeUnresolved();
 	break;
 	}
 	return baseType;
 	}*/
 	public static FanResolvedType makeFromExpr(FanParserTask result, AstNode exprNode, int lastGoodTokenIndex)
 	{
 		FanResolvedType type = resolveExpr(null, exprNode, lastGoodTokenIndex);
 		if (type == null)
 		{
 			type = makeUnresolved(exprNode);
 		}
 		FanUtilities.GENERIC_LOGGER.debug("** resolvedType: " + type);
 		return type;
 	}
 
 	/**
 	 * recursive
 	 * @param result
 	 * @param scope
 	 * @param baseType
 	 * @param node
 	 * @param index
 	 * @return
 	 */
 	private static FanResolvedType resolveExpr(FanResolvedType baseType, AstNode node, int index)
 	{
 		//FanUtilities.GENERIC_LOGGER.info("** type: " + node.toStringTree() + " " + baseType);
 		FanParserTask result = node.getRoot().getParserTask();
 		// if unresolveable no point searching further
 		if (baseType != null && !baseType.isResolved())
 		{
 			return baseType;
 		}
 		//System.out.println("Node type: " + node.getType());
 		String t = node.getNodeText(true);
 		if (node == null || t == null)
 		{
 			FanUtilities.GENERIC_LOGGER.info("Node is empty! " + node.getParent().toString());
 			return makeUnresolved(node);
 		}
 		//System.out.println("Index: " + FanLexAstUtils.getTokenStart(node) + " VS " + index);
 		// Skip the imcomplete part past what we care about
 		if (!isValidTokenStart(node, index))
 		{
 			return baseType;
 		}
 		List<AstNode> children = node.getChildren();
 		/*switch (node.getKind())
 		{
 		//TODO: ranges (tricky)
 		case FanParser.AST_CAST:
 		CommonTree castType = (CommonTree) node.getFirstChildWithType(FanParser.AST_TYPE);
 		baseType = makeFromTypeSigWithWarning(scope, castType);
 		baseType.setStaticContext(false);
 		break;
 		case FanParser.AST_TERM_EXPR:
 		CommonTree termBase = children.get(0);
 		baseType = resolveExpr(scope, null, termBase, index);
 		CommonTree termChain = children.get(1);
 		baseType = resolveExpr(scope, baseType, termChain, index);
 		break;
 		case FanParser.AST_STATIC_CALL:
 		CommonTree type = children.get(0);
 		CommonTree idExpr = children.get(1);
 		baseType = resolveExpr(scope, null, type, index);
 		baseType = resolveExpr(scope, baseType, idExpr, index);
 		break;
 		case FanParser.AST_STR:
 		baseType = new FanResolvedType("sys::Str");
 		break;
 		case FanParser.KW_TRUE:
 		case FanParser.KW_FALSE:
 		baseType = new FanResolvedType("sys::Bool");
 		break;
 		case FanParser.CHAR:
 		baseType = new FanResolvedType("sys::Int");
 		break;
 		case FanParser.NUMBER:
 		String ftype = parseNumberType(node.getText());
 		baseType = new FanResolvedType(ftype);
 		break;
 		case FanParser.URI:
 		baseType = new FanResolvedType("sys::Uri");
 		break;
 		case FanParser.AST_NAMED_SUPER:
 		CommonTree nameNode = (CommonTree) node.getFirstChildWithType(FanParser.AST_ID);
 		baseType = resolveExpr(scope, baseType, nameNode, index);
 		baseType.setStaticContext(false);
 		break;
 		case FanParser.KW_SUPER:
 		baseType = new FanResolvedType(resolveSuper(scope));
 		baseType.setStaticContext(false);
 		break;
 		case FanParser.KW_THIS:
 		baseType = new FanResolvedType(resolveThisType(scope));
 		break;
 		case FanParser.AST_IT_BLOCK:
 		// Do nothing, keep type of left hand side.
 		baseType.setStaticContext(false);
 		break;
 		case FanParser.AST_CTOR_BLOCK:
 		CommonTree ctorNode = (CommonTree) node.getFirstChildWithType(FanParser.AST_TYPE);
 		baseType = resolveExpr(scope, baseType, ctorNode, index);
 		baseType.setStaticContext(false);
 		break;
 		case FanParser.AST_INDEX_EXPR:
 		baseType = resolveIndexExpr(scope, baseType, node, index);
 		break;
 		case FanParser.AST_LIST:
 		CommonTree firstNode = (CommonTree) children.get(0);
 		// Because of a grammar issue, some indexed expression show up as List
 		boolean isResolveExpr = false;
 		if (firstNode != null && firstNode.getType() == FanParser.AST_TYPE)
 		{
 		FanResolvedType lType = resolveExpr(scope, baseType, firstNode, index);
 		if (lType != null && !lType.isStaticContext())
 		{
 		isResolveExpr = true;
 		baseType = resolveIndexExpr(scope, lType, node, index);
 		}
 		}
 		if (!isResolveExpr)
 		{//Normal list type like Str[]
 		baseType = new FanResolvedType("sys::List");
 		CommonTree listTypeNode = (CommonTree) node.getFirstChildWithType(FanParser.AST_TERM_EXPR);
 		if (listTypeNode != null)
 		{
 		FanResolvedType listType = resolveExpr(scope, null, listTypeNode, index);
 		baseType = new FanResolvedListType(listType);
 		}
 		}
 		break;
 		case FanParser.AST_MAP:
 		baseType = new FanResolvedType("sys::Map");
 		List<CommonTree> mapElems = FanLexAstUtils.getAllChildrenWithType(node, FanParser.AST_TERM_EXPR);
 		if (mapElems.size() >= 2)
 		{
 		CommonTree keyNode = mapElems.get(0);
 		CommonTree valueNode = mapElems.get(1);
 		FanResolvedType keyType = resolveExpr(scope, null, keyNode, index);
 		FanResolvedType valueType = resolveExpr(scope, null, valueNode, index);
 		if (keyType.isResolved() && valueType.isResolved())
 		{
 		baseType = new FanResolvedMapType(keyType, valueType);
 		}
 		}
 		break;
 		case FanParser.AST_TYPE_LIT: // type litteral
 		//System.out.println("Lit: "+node.toStringTree());
 		CommonTree litNode=(CommonTree) node.getFirstChildWithType(FanParser.AST_TYPE);
 		baseType = makeFromExpr(scope, result, litNode, index);
 		baseType.setStaticContext(true);
 		break;
 		case FanParser.AST_FUNC_TYPE:
 		FanResolvedType returnType = null;
 		Vector<FanResolvedType> types = new Vector<FanResolvedType>();
 		boolean passedArrow = false;
 		for (CommonTree n : (List<CommonTree>) node.getChildren())
 		{
 		switch (n.getType())
 		{
 		case FanParser.OP_ARROW:
 		passedArrow = true;
 		break;
 		case FanParser.AST_TYPE:
 		if (!passedArrow)
 		{
 		types.add(makeFromTypeSig(scope, n));
 		} else
 		{
 		returnType = makeFromTypeSig(scope, n);
 		}
 		}
 		}
 
 		baseType = new FanResolvedFuncType(types, returnType);
 		baseType.setStaticContext(false);
 		break;
 		case FanParser.AST_SLOT_LIT:
 		baseType = new FanResolvedType("sys::Slot");
 		break;
 		case FanParser.AST_SYMBOL:
 		// TODO: is this correct - not sure
 		baseType = new FanResolvedType("sys::Symbol");
 		break;
 		case FanParser.AST_ID:
 		case FanParser.KW_IT:
 		if (baseType == null)
 		{
 		baseType = scope.resolveVar(t);
 		if (!baseType.isResolved())
 		{
 		// Try a static type (ex: Str.)
 		baseType = makeFromTypeSigWithWarning(scope, node);
 		baseType.setStaticContext(true);
 		}
 		} else
 		{
 		baseType = resolveSlotType(baseType, t);
 		}
 		break;
 		default:
 		// "Meaningless" 'wrapper' nodes (in term of expression resolving)
 		if (children != null && children.size() > 0)
 		{
 		for (CommonTree child : children)
 		{
 		baseType = resolveExpr(scope, baseType, child, index);
 		break; //TODO: to break or not ??
 		}
 		} else
 		{
 		FanUtilities.GENERIC_LOGGER.info("Don't know how to resolve: " + t + " " + node.toStringTree());
 		}
 		break;
 		}
 		 */
 
 		//System.out.println("** End type: " + baseType + "  "+baseType.isStaticContext());
 		return baseType;
 	}
 
 	public static FanResolvedType resolveSlotType(FanResolvedType baseType, String slotName)
 	{
		if(baseType==null || !baseType.isResolved())
			return FanResolvedType.makeUnresolved(null);
 		if (baseType.getDbType().isJava())
 		{
 			// java slots
 			List<Member> members = FanIndexerFactory.getJavaIndexer().findTypeSlots(baseType.getDbType().getQualifiedName());
 			boolean found = false;
 			// Returrning the first match .. because java has overloading this could be wrong
 			// However i assume overloaded methods return the same type (If it doesn't too bad, it's ugly coe anyway :) )
 			for (Member member : members)
 			{
 				if (member.getName().equalsIgnoreCase(slotName))
 				{
 					baseType = makeFromLocalType(baseType.scopeNode, FanIndexerFactory.getJavaIndexer().getReturnType(member));
 					found = true;
 					break;
 				}
 			}
 			if (!found)
 			{
 				baseType = makeUnresolved(baseType.scopeNode);
 			}
 
 		} else
 		{
 			// Fan slots
 			FanSlot slot = FanSlot.findByTypeAndName(baseType.getDbType().getQualifiedName(), slotName);
 			if (slot != null)
 			{
 				baseType = fromTypeSig(baseType.scopeNode, slot.returnedType);
 			} else
 			{
 				baseType = makeUnresolved(baseType.scopeNode);
 			}
 		}
 		return baseType;
 	}
 
 	private static FanResolvedType resolveIndexExpr(FanResolvedType baseType, AstNode node, int index)
 	{
 		//FanUtilities.GENERIC_LOGGER.info("Index expr: " + node.toStringTree());
 		if (baseType instanceof FanResolvedListType)
 		{
 			baseType = ((FanResolvedListType) baseType).getItemType();
 		} else if (baseType instanceof FanResolvedMapType)
 		{
 			baseType = ((FanResolvedMapType) baseType).getValType();
 		} else
 		{
 			baseType = resolveSlotType(baseType, "get");
 		}
 		return baseType;
 	}
 
 	private static boolean isValidTokenStart(AstNode node, int maxIndex)
 	{
 		int index = node.getStartLocation().getIndex();
 		// will be -1 for a Nill node
 		return index >= 0 && index <= maxIndex;
 	}
 
 	public boolean isStaticContext()
 	{
 		return staticContext;
 	}
 
 	public void setNullableContext(boolean nullable)
 	{
 		nullableContext = nullable;
 	}
 
 	public boolean isNullable()
 	{
 		return nullableContext;
 	}
 
 	public void setStaticContext(boolean b)
 	{
 		staticContext = b;
 	}
 
 	/**
 	 * Parse number litterals
 	 * http://fantom.org/doc/docLang/Literals.html#int
 	 * @param text
 	 * @return
 	 */
 	private static String parseNumberType(String text)
 	{
 		text = text.toLowerCase();
 		if (text.endsWith("ns") || text.endsWith("ms")
 			|| text.endsWith("sec") || text.endsWith("min")
 			|| text.endsWith("hr") || text.endsWith("day"))
 		{
 			return "sys::Duration";
 		}
 		if (text.startsWith("0x")) // hex
 		{
 			return "sys::Int"; // char
 		}
 		if (text.endsWith("f"))
 		{
 			return "sys::Float";
 		}
 		if (text.endsWith("d") || text.indexOf(".") != -1)
 		{
 			return "sys::Decimal";
 		}
 		return "sys::Int";
 	}
 
 	public static String resolveThisType(AstNode node)
 	{
 		return FanIndexer.UNRESOLVED_TYPE; // FIXME
 		/*FanAstScope tscope = scope.getTypeScope();
 		// FIXME
 		if (tscope == null)
 		{
 		return FanIndexer.UNRESOLVED_TYPE;
 		}
 		return ((FanTypeScope) tscope).getQName();*/
 	}
 
 	public static String resolveSuper(AstNode node)
 	{
 		// FIXME
 		return FanIndexer.UNRESOLVED_TYPE;
 		/*
 		FanAstScope tscope = scope;
 		while (tscope != null && !(tscope instanceof FanTypeScope))
 		{
 		tscope = tscope.getParent();
 		}
 		if (tscope != null)
 		{
 		FanResolvedType superType = ((FanTypeScope) tscope).getSuperClass();
 		return superType.getAsTypedType();
 		} else
 		{
 		return FanIndexer.UNRESOLVED_TYPE;
 		}*/
 	}
 
 	/**
 	 * "Serialize" the type into a db signature
 	 * @param fullyQualified
 	 * @return
 	 */
 	public String toTypeSig(boolean fullyQualified)
 	{
 		if (isResolved())
 		{
 			if (fullyQualified)
 			{
 				return dbType.getQualifiedName();
 			} else
 			{
 				return dbType.getSimpleName();
 			}
 		}
 		if (fullyQualified)
 		{
 			return getAsTypedType();
 		} else
 		{
 			return getShortAsTypedType();
 		}
 	}
 
 	/**
 	 * Create type from the "Serialized" dbType
 	 * @param sig
 	 * @return
 	 */
 	public static FanResolvedType fromTypeSig(AstNode scopeNode, String sig)
 	{
 		FanResolvedType type = makeUnresolved(scopeNode);
 		boolean nullable = false;
 		boolean list = false;
 		boolean nullableList = false;
 		if (sig.endsWith("?"))
 		{
 			sig = sig.substring(0, sig.length() - 1);
 			nullable = true;
 		}
 		if (sig.endsWith("[]"))
 		{
 			sig = sig.substring(0, sig.length() - 1);
 			list = true;
 			if (sig.endsWith("?"))
 			{
 				sig = sig.substring(0, sig.length() - 1);
 				nullableList = true;
 			}
 		}
 		if (sig.startsWith("[") && sig.endsWith("]"))
 		{// Full map
 			sig = sig.substring(1, sig.length() - 1);
 			int index = findMapSeparatorIndex(sig);
 			if (index != -1 && index < sig.length() - 1)
 			{
 				FanResolvedType keyType = fromTypeSig(scopeNode, sig.substring(0, index).trim());
 				FanResolvedType valType = fromTypeSig(scopeNode, sig.substring(index + 1).trim());
 				type = new FanResolvedMapType(scopeNode, keyType, valType);
 			}
 		} else if (sig.startsWith("|") && sig.endsWith("|"))
 		{
 			Vector<FanResolvedType> types = new Vector<FanResolvedType>();
 			sig = sig.substring(1, sig.length() - 1);
 			// Without -1 it will drop empty strings, causing |->| to have a zero length result array.
 			String[] parts = sig.split("->", -1);
 			String[] typeParts = parts[0].split(",");
 			for (int i = 0; i != typeParts.length; i++)
 			{
 				String formal = typeParts[i].trim();
 				String formalType = formal;
 				String formalName = "";
 				int idx = formal.indexOf(" ");
 				if (idx != -1)
 				{
 					formalType = formal.substring(0, idx).trim();
 					formalName = formal.substring(idx).trim();
 				}
 				// TODO: types can have names like Int a, Intb -> Int
 				// TODO: Make use of formalName -> will need them for scoping/completion etc...
 				types.add(fromTypeSig(scopeNode, formalType));
 			}
 			String returnType = "Void"; // Default if not specified
 			if (parts.length == 2)
 			{
 				returnType = parts[1].trim();
 			}
 			type = new FanResolvedFuncType(scopeNode, types, fromTypeSig(scopeNode, returnType));
 		} else
 		{	// check for simple map type like Sys:Int
 			int index = findMapSeparatorIndex(sig);
 			if (index != -1 && index < sig.length() - 1)
 			{
 				FanResolvedType keyType = fromTypeSig(scopeNode, sig.substring(0, index).trim());
 				FanResolvedType valType = fromTypeSig(scopeNode, sig.substring(index + 1).trim());
 				type = new FanResolvedMapType(scopeNode, keyType, valType);
 			} else
 			{
 				// true simple type like Int or Sys::Int
 				type = makeFromLocalType(scopeNode, sig);
 			}
 		}
 
 		if (nullable)
 		{
 			type.setNullableContext(true);
 		}
 		if (list)
 		{
 			type = new FanResolvedListType(scopeNode, type);
 			if (nullableList)
 			{
 				type.setNullableContext(true);
 			}
 		}
 		return type;
 	}
 
 	public static FanResolvedType makeFromLocalType(AstNode scopeNode, String enteredType)
 	{
 		FanType type = null;
 		if (enteredType.indexOf("::") != -1)
 		{	// Qualified type
 			type = FanType.findByQualifiedName(enteredType);
 		}
 		else
 		{
 			Hashtable<String, FanAstScopeVarBase> types = scopeNode.getAllScopeVars();
 			if (types.containsKey(enteredType))
 			{
 				type = types.get(enteredType).getType().getDbType();
 			}
 			// If not found in scope, try "implicit" imports
 			if (type == null)
 			{
 				// first, other types in this pod
 				type = FanType.findByQualifiedName(scopeNode.getRoot().getPod() + "::" + enteredType);
 
 			}
 			// if still not found try in "sys" pod
 			if (type == null)
 			{
 				type = FanType.findByQualifiedName("sys::" + enteredType);
 			}
 			// try Generic types
 			if (type == null && enteredType.length()==1 && enteredType.toUpperCase().equals(enteredType))
 			{
 				FanResolvedType objType=new FanResolvedType(scopeNode, enteredType, FanType.findByQualifiedName("sys::Obj"));
 				if (enteredType.equals("V") || enteredType.equals("K") || enteredType.equals("A") || enteredType.equals("B")
 					 || enteredType.equals("C") || enteredType.equals("D") || enteredType.equals("E") || enteredType.equals("F")
 					  || enteredType.equals("G") || enteredType.equals("H"))
 				{	// K, V: List/Map key and item types
 					// A-H : Function types
 					type = FanType.findByQualifiedName("sys::Obj");
 				} else if (enteredType.equals("L"))
 				{ // List
 					return new FanResolvedListType(scopeNode, objType);
 				}else if (enteredType.equals("M"))
 				{ // Map
 					return new FanResolvedMapType(scopeNode, objType, objType);
 				}else if (enteredType.equals("R"))
 				{ // Function
 					return new FanResolvedFuncType(scopeNode, new Vector<FanResolvedType>(), objType);
 				}
 			}
 			if (type == null)
 			{
 				if (type == null)
 				{
 					return makeUnresolved(scopeNode);
 				}
 			}
 		}
 		return new FanResolvedType(scopeNode, enteredType, type);
 	}
 
 	/**
 	 * Make a type from the type database (doesn't look at the local scope)
 	 * (external type, like a 'using'
 	 * @param node
 	 * @param qualifiedType
 	 * @return
 	 */
 	public static FanResolvedType makeFromDbType(AstNode node, String qualifiedType)
 	{
 		FanType type = FanType.findByQualifiedName(qualifiedType);
 		return new FanResolvedType(node, qualifiedType, type);
 	}
 
 	private static int findMapSeparatorIndex(String sig)
 	{
 		int index = 0;
 		while (index != -1 && index < sig.length())
 		{
 			index = sig.indexOf(":", index);
 			if (index == -1)
 			{
 				break; // not found
 			}				// looking for ":" but NOT "::"
 			if (index > 1 && index < sig.length() - 1 && sig.charAt(index - 1) != ':' && sig.charAt(index + 1) != ':')
 			{
 				break; // found
 			}
 			// try next one
 			index++;
 		}
 		return index;
 	}
 }
