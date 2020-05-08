 /*
  * Thibaut Colar Mar 2, 2010
  */
 package net.colar.netbeans.fan.scope;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import net.colar.netbeans.fan.FanParserTask;
 import net.colar.netbeans.fan.FanUtilities;
 import net.colar.netbeans.fan.indexer.model.FanSlot;
 import net.colar.netbeans.fan.indexer.model.FanType;
 import net.colar.netbeans.fan.parboiled.AstKind;
 import net.colar.netbeans.fan.parboiled.AstNode;
 import net.colar.netbeans.fan.parboiled.FanLexAstUtils;
 import net.colar.netbeans.fan.parboiled.pred.NodeKindPredicate;
 import net.colar.netbeans.fan.types.FanResolvedType;
 import org.parboiled.google.base.Predicate;
 
 /**
  *
  * @author thibautc
  */
 public class FanTypeScopeVar extends FanAstScopeVarBase
 {
 	// qualified Name
 
 	String qName = "";
 	List<FanResolvedType> inheritedItems = new ArrayList<FanResolvedType>();
 	// To make it faster to lookup vars
 	Hashtable<String, FanAstScopeVarBase> inheritedSlots = new Hashtable<String, FanAstScopeVarBase>();
 
 	public FanTypeScopeVar(AstNode typeDefNode, String name)
 	{
 		super(typeDefNode, name);
 		// not valid until parsed.
 	}
 
 	@SuppressWarnings("unchecked")
 	public void parse()
 	{
 		if (node == null)
 		{
 			return;
 		}
 		if (FanLexAstUtils.getFirstChild(node, new NodeKindPredicate(AstKind.AST_ENUM)) != null)
 		{ //TODO: Deal with enum val defs ?
 			kind = VarKind.TYPE_ENUM;
 
 		} else if (FanLexAstUtils.getFirstChild(node, new NodeKindPredicate(AstKind.AST_FACET)) != null)
 		{
 			kind = VarKind.TYPE_FACET;
 
 		} else if (FanLexAstUtils.getFirstChild(node, new NodeKindPredicate(AstKind.AST_CLASS)) != null)
 		{
 			kind = VarKind.TYPE_CLASS;
 
 		} else if (FanLexAstUtils.getFirstChild(node, new NodeKindPredicate(AstKind.AST_ENUM)) != null)
 		{ //TODO: Deal with enum val defs ?
 			kind = VarKind.TYPE_ENUM;
 
 		} else if (FanLexAstUtils.getFirstChild(node, new NodeKindPredicate(AstKind.AST_MIXIN)) != null)
 		{
 			kind = VarKind.TYPE_MIXIN;
 		}
 		AstNode nameNode = FanLexAstUtils.getFirstChild(node, new NodeKindPredicate(AstKind.AST_ID));
 
 		type = FanResolvedType.makeFromTypeSig(node, nameNode.getNodeText(true));
 
 		if (type == null || !type.isResolved())
 		{
 			FanUtilities.GENERIC_LOGGER.error(getClass().getName() + " Null type for: " + nameNode.getNodeText(true));
 			type = FanResolvedType.makeUnresolved(node);
 		}
 		node.setType(type);
 
 		AstNode inheritance = FanLexAstUtils.getFirstChild(node, new NodeKindPredicate(AstKind.AST_INHERITANCE));
 
		if (nameNode != null)
		{
			name = nameNode.getNodeText(true);
		}
 		qName = node.getRoot().getPod() + "::" + name;
 
 		List<AstNode> modifs = FanLexAstUtils.getChildren(node, new NodeKindPredicate(AstKind.AST_MODIFIER));
 		for (AstNode m : modifs)
 		{
 			String[] mStrs = m.getNodeText(true).split(" ");
 			for (String mStr : mStrs)
 			{
 				FanAstScopeVarBase.ModifEnum modif = FanAstScopeVarBase.parseModifier(mStr.trim());
 				if (modif != null)
 				{
 					modifiers.add(modif);
 				}
 			}
 		}
 
 		// Deal with ineritance
 		parseInheritance(inheritance);
 	}
 
 	public void parseSlots(FanParserTask task)
 	{
 		// Also "cache" inherited slots, for faster var lookup later
 		System.out.print(">getallslots " + name);
 		List<FanSlot> slots = FanSlot.getAllSlotsForType(qName, true, task);
 		System.out.print("<getallslots");
 		for (FanSlot slot : slots)
 		{
 			FanAstScopeVarBase newVar = new FanLocalScopeVar(node, getType(), slot, slot.getName());
 			inheritedSlots.put(slot.getName(), newVar);
 		}
 		// Deal with children - slots
 		@SuppressWarnings("unchecked")
 		AstNode blockNode = FanLexAstUtils.getFirstChild(node, new NodeKindPredicate(AstKind.AST_BLOCK));
 		if (blockNode == null)
 		{
 			return;
 		}
 		for (AstNode slot : blockNode.getChildren())
 		{
 			switch (slot.getKind())
 			{
 				case AST_SLOT_DEF:
 					@SuppressWarnings("unchecked") List<String> slotModifs = new ArrayList<String>();
 					FanFieldScopeVar slotVar = null;
 					for (AstNode child : slot.getChildren())
 					{
 						@SuppressWarnings("unchecked")
 						String slotName = FanLexAstUtils.getFirstChildText(child, new NodeKindPredicate(AstKind.AST_ID));
 						switch (child.getKind())
 						{
 							// could be multiple modifiers, but only one of either field, ctor, method
 							case AST_MODIFIER:
 								//Protection is "outside" of field_def/method_def
 								slotModifs.add(child.getNodeText(true));
 								break;
 							case AST_FIELD_DEF:
 								slotVar = new FanFieldScopeVar(child, slotName);
 								break;
 							case AST_METHOD_DEF:
 							case AST_CTOR_DEF:
 								slotVar = new FanMethodScopeVar(child, slotName, child.getKind() == AstKind.AST_CTOR_DEF);
 								break;
 						}
 					}
 					// Add the "outer" modifiers(protection) to the slotVar and save it in scope.
 					for (String modif : slotModifs)
 					{
 						if (slotVar != null)
 						{
 							slotVar.addModifiers(modif);
 						} else
 						{
 							node.getRoot().getParserTask().addError("Orphaned modifier in slot.", slot);
 						}
 					}
 					if (slotVar != null) //should be
 					{
 						node.addScopeVar(slotVar, false);
 					}
 			}
 		}
 	}
 
 	private boolean hasInheritedClass()
 	{
 		for (FanResolvedType type : inheritedItems)
 		{
 			if (!type.isResolved() && type.getDbType().isClass())
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean hasInheritedItem(String qualifiedName)
 	{
 		for (FanResolvedType type : inheritedItems)
 		{
 			if (type.getAsTypedType().equals(qualifiedName))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void parseInheritance(AstNode inheritance)
 	{
 		if (inheritance != null)
 		{
 			@SuppressWarnings("unchecked")
 			List<AstNode> children = FanLexAstUtils.getChildren(inheritance, new NodeKindPredicate(AstKind.AST_TYPE));
 			for (AstNode child : children)
 			{
 				FanResolvedType inhType = FanResolvedType.makeFromTypeSig(child, child.getNodeText(true));
 				String text = node.getNodeText(true);
 				if (!inhType.isResolved())
 				{
 					node.getRoot().getParserTask().addError("Unresolved inherited item : " + child.getNodeText(true), child);
 				} else
 				{
 					FanType fanType = inhType.getDbType();
 					if (fanType.isFinal())
 					{
 						// this covers enums too
 						node.getRoot().getParserTask().addError("Can't inherit from a final class : " + child.getNodeText(true), child);
 					} else if (fanType.isClass())
 					{
 						if (hasInheritedClass())
 						{
 							node.getRoot().getParserTask().addError("Can only inherit from one class !", inheritance);
 						}
 					}
 				}
 				if (!hasInheritedItem(text))
 				{
 					inheritedItems.add(inhType);
 				}
 			}
 		}
 	}
 
 	public List<FanResolvedType> getInheritedItems()
 	{
 		return inheritedItems;
 	}
 
 	public Hashtable<String, FanAstScopeVarBase> getInheritedSlots()
 	{
 		return inheritedSlots;
 	}
 
 	public String getQName()
 	{
 		return qName;
 	}
 }
