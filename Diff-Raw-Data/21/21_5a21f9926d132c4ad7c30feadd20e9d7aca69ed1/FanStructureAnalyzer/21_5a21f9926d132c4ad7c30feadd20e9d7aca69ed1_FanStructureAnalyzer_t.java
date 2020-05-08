 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.structure;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import net.colar.netbeans.fan.FanParserTask;
 import net.colar.netbeans.fan.indexer.FanIndexer;
 import net.colar.netbeans.fan.indexer.model.FanType;
 import net.colar.netbeans.fan.parboiled.AstNode;
 import net.colar.netbeans.fan.parboiled.FanLexAstUtils;
 import net.colar.netbeans.fan.scope.FanAstScopeVarBase;
 import net.colar.netbeans.fan.scope.FanFieldScopeVar;
 import net.colar.netbeans.fan.scope.FanMethodScopeVar;
 import net.colar.netbeans.fan.scope.FanTypeScopeVar;
 import net.colar.netbeans.fan.types.FanResolvedType;
 import org.netbeans.modules.csl.api.ElementKind;
 import org.netbeans.modules.csl.api.Modifier;
 import org.netbeans.modules.csl.api.OffsetRange;
 import org.netbeans.modules.csl.api.StructureItem;
 import org.netbeans.modules.csl.api.StructureScanner;
 import org.netbeans.modules.csl.spi.ParserResult;
 
 /**
  * StructureScanner impl.
  * Generates a structure tree from the source (~AST), used for Navigator window (class,enum,mixin,field...)
  * Also provides code folding support.
  *
  * See: http://hg.netbeans.org/main/file/24d72d2643e1/csl.api/src/org/netbeans/modules/csl/editor/fold/GsfFoldManager.java
  * @author thibautc
  */
 public class FanStructureAnalyzer implements StructureScanner
 {
 
 	public static final String CODE_FOLDS = "codeblocks";
 	public static final String DOC_FOLDS = "comments";
 	public static final String COMMENT_FOLDS = "comments";
 	public static final String IMPORT_FOLDS = "imports";
 
 	public FanStructureAnalyzer()
 	{
 		super();
 	}
 
 	@Override
 	public List<StructureItem> scan(ParserResult result)
 	{
 		List<StructureItem> items = new ArrayList<StructureItem>();
 		try
 		{
 			FanParserTask task = (FanParserTask) result;
 			AstNode rootScope = task.getRootScope();
 			if(rootScope == null)
 				return items;
 			for (FanAstScopeVarBase var : rootScope.getLocalScopeVars().values())
 			{
 				FanStructureItem item = null;
 				if(! (var instanceof FanTypeScopeVar))
 					continue;
 				FanTypeScopeVar t = (FanTypeScopeVar) var;
 				item = new FanStructureItem(t.getNode(), ElementKind.CLASS, result);
 				List<FanResolvedType> inhs = t.getInheritedItems();
 				String inheritance = "";
 				for (FanResolvedType inh : inhs)
 				{
 					if (inheritance.length() > 0)
 					{
 						inheritance += ", ";
 					}
 					inheritance += inh.toTypeSig(false);
 				}
 				item.setName(t.getName());
 				String html = t.getName();
 				if (inheritance.length() > 0)
 				{
 					html += " : <font color='#aaaaaa'>" + inheritance + "</font>";
 				}
 				item.setHtml(html);
 				// Do slots
 				List<StructureItem> slots = new ArrayList<StructureItem>();
 				AstNode scopeNode = FanLexAstUtils.getScopeNode(t.getNode());
 				for (FanAstScopeVarBase slotVar : scopeNode.getLocalScopeVars().values())
 				{
 					if (slotVar instanceof FanMethodScopeVar)
 					{
 						FanMethodScopeVar m = (FanMethodScopeVar) slotVar;
 
 						ElementKind kind = m.isCtor() ? ElementKind.CONSTRUCTOR : ElementKind.METHOD;
 						FanStructureItem mItem = new FanStructureItem(slotVar.getNode(), kind, result);
 						String returnType = FanType.getShortName(m.getTypeString());
 						if (returnType==null || returnType.equals(FanIndexer.UNRESOLVED_TYPE) || returnType.equalsIgnoreCase("void"))
 						{
 							returnType = "";
 						}
 						// method params
 						Hashtable<String, FanResolvedType> parameters = m.getParameters();
 						String params = "";
 						for (String pname : parameters.keySet())
 						{
 							if (params.length() > 0)
 							{
 								params += ", ";
 							}
 							String pType = parameters.get(pname).toTypeSig(false);
 							if (pType.equals(FanIndexer.UNRESOLVED_TYPE))
 							{
 								pType = "";
 							}
 
 							if (pType.equals(FanIndexer.UNRESOLVED_TYPE))
 							{
 								pType = "";
 							}
 							params += "<font color='#aaaaaa'>" + pType + "</font>";
 							params += " " + pname;
 						}
 						handleModifiers(mItem, m.getModifiers());
 						mItem.setName(slotVar.getName());
 						String mHtml = slotVar.getName() + "(" + params + ")";
 						if (returnType.length() > 0)
 						{
 							mHtml += " : <font color='#aaaaaa'>" + returnType + "</font>";
 						}
 						mItem.setHtml(mHtml);
 						slots.add(mItem);
 					} else if (slotVar instanceof FanFieldScopeVar)
 					{
 						FanStructureItem slotItem = new FanStructureItem(slotVar.getNode(), ElementKind.FIELD, result);
 						String type = slotVar.getType().toTypeSig(false);
 						if (type.equals(FanIndexer.UNRESOLVED_TYPE))
 						{
 							type = "";
 						}
 						handleModifiers(slotItem, slotVar.getModifiers());
 						slotItem.setName(slotVar.getName());
 						String slotHtml = slotVar.getName();
 						if (type.length() > 0)
 						{
 							slotHtml += " : <font color='#aaaaaa'>" + type + "</font>";
 						}
 						slotItem.setHtml(slotHtml);
 						slots.add(slotItem);
 					}
 				}
 				if (slots.size() > 0)
 				{
 					item.setNestedItems(slots);
 				}
 				items.add(item);
 			}
 
 		} catch (Exception e)
 		{
 			// if there is an error here, we don't want to propagate to the user, should log though
 			e.printStackTrace();
 		}
 		return items;
 	}
 
 	@Override
 	public Map<String, List<OffsetRange>> folds(ParserResult result)
 	{  // Note: only seem to be called if no errors in the file
 		Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
 		addFolds(folds, ((FanParserTask)result).getAstTree());
 		return folds;
 	}
 
 	public Configuration getConfiguration()
 	{
 		return null;
 	}
 
 	private void handleModifiers(FanStructureItem item, List<FanAstScopeVarBase.ModifEnum> modifs)
 	{
 		for (FanAstScopeVarBase.ModifEnum modif : modifs)
 		{
 			if (modif == modif.STATIC)
 			{
 				item.addModifier(Modifier.STATIC);
 			} else if (modif == modif.PRIVATE)
 			{
 				item.addModifier(Modifier.PRIVATE);
 			} else if (modif == modif.PUBLIC)
 			{
 				item.addModifier(Modifier.PUBLIC);
 			} else if (modif == modif.PROTECTED)
 			{
 				item.addModifier(Modifier.PROTECTED);
 			}
 		}
 	}
 
 	private void addFolds(Map<String, List<OffsetRange>> folds, AstNode node)
 	{
 		if (node != null)
 		{
 			boolean foldable = false;
 			String type = CODE_FOLDS;
 			switch (node.getKind())
 			{
 				case AST_BLOCK:
 					type = CODE_FOLDS;
 					foldable = true;
 					break;
 				// TODO: allow for DOC. ML_COMMENTS folding
 				/*case AST_DOC:
 				case AST_ML_COMMENT:
 					type = DOC_FOLDS;
 					foldable = true;
 					break;*/
 			}
 
 			if (foldable)
 			{
 				int start = node.getStartLocation().getRow();
 				int end = node.getEndLocation().getRow();
 				// don't add unless at least 2 line long
 				if (start<end)
 				{
 					OffsetRange range = new OffsetRange(node.getStartLocation().getIndex(), node.getEndLocation().getIndex());
 					List<OffsetRange> fold = folds.get(type);
 					if (fold == null)
 					{
 						fold = new ArrayList<OffsetRange>();
 						folds.put(type, fold);
 					}
 					fold.add(range);
 				}
 			}
 			// recurse into children
 			for (AstNode subNode : node.getChildren())
 			{
 				addFolds(folds, subNode);
 			}
 		}
 	}
 }
