 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.structure;
 
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import net.colar.netbeans.fan.FanParserTask;
 import net.colar.netbeans.fan.parboiled.AstKind;
 import net.colar.netbeans.fan.parboiled.AstNode;
 import net.colar.netbeans.fan.parboiled.FanLexAstUtils;
 import net.colar.netbeans.fan.parboiled.FantomParserTokens.TokenName;
 import net.colar.netbeans.fan.parboiled.pred.NodeKindPredicate;
 import org.netbeans.modules.csl.api.ColoringAttributes;
 import org.netbeans.modules.csl.api.OffsetRange;
 import org.netbeans.modules.csl.api.SemanticAnalyzer;
 import org.netbeans.modules.parsing.spi.Parser.Result;
 import org.netbeans.modules.parsing.spi.Scheduler;
 import org.netbeans.modules.parsing.spi.SchedulerEvent;
 import org.parboiled.Node;
 
 /**
  * The semantic Analyzer looks at the structure(AST tree),
  * and uses this to highlight source items,
  * display semantic errors and so on.
  * @author thibautc
  */
 public class FanSemanticAnalyzer extends SemanticAnalyzer
 {
 
 	private static final Set<ColoringAttributes> ErrorSet = Collections.singleton(ColoringAttributes.UNDEFINED);
 	private volatile boolean cancelled = false;
 	private Map<OffsetRange, Set<ColoringAttributes>> highlights = null;
 	private static final Pattern INTERPOLATION = Pattern.compile("[^\\\\]\\$\\{?[a-zA-Z0-9\\.]*\\}?");
 
 	@Override
 	public Map getHighlights()
 	{
 		return highlights;
 	}
 
 	@Override
 	public void run(Result result, SchedulerEvent event)
 	{
 		FanParserTask res = (FanParserTask) result;
 		Map<OffsetRange, Set<ColoringAttributes>> newHighlights = new HashMap<OffsetRange, Set<ColoringAttributes>>();
 		if (res.getParseNodeTree() != null)
 		{
 			scanTree(res, res.getParseNodeTree(), newHighlights);
 			highlights = newHighlights.size() == 0 ? null : newHighlights;
 		}
 	}
 
 	@Override
 	public int getPriority()
 	{
 		return 0;
 	}
 
 	@Override
 	public Class<? extends Scheduler> getSchedulerClass()
 	{
 		return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
 	}
 
 	@Override
 	public void cancel()
 	{
 		this.cancelled = true;
 	}
 
 	/**
 	 * Run though AST tree and highlight relevant items
 	 * @param ast
 	 * @param newHighlights
 	 */
 	private void scanTree(FanParserTask result, Node<AstNode> node, Map<OffsetRange, Set<ColoringAttributes>> newHighlights)
 	{
		if (node.getLabel().equals(TokenName.STRS.name()))
 		{
 			addStrHighlights(result, newHighlights, node);
 		} else if (node.getValue() != null)
 		{
 			AstNode value = node.getValue();
 			switch (value.getKind())
 			{
 				case AST_TYPE_DEF:
 					addIdToHighlights(result, newHighlights, value, ColoringAttributes.CLASS_SET);
 					break;
 				case AST_CTOR_DEF:
 					addIdToHighlights(result, newHighlights, value, ColoringAttributes.CONSTRUCTOR_SET);
 					break;
 				case AST_METHOD_DEF:
 					addIdToHighlights(result, newHighlights, value, ColoringAttributes.METHOD_SET);
 					break;
 				case AST_FIELD_DEF: // global field
 					addIdToHighlights(result, newHighlights, value, ColoringAttributes.GLOBAL_SET);
 					break;
 				// static field ?
 				case AST_PARAM:
 					//case AST_CTOR_CHAIN:
 					addToHighlights(result, newHighlights, value, ColoringAttributes.PARAMETER_SET);
 					break;
 			}
 		}
 
 		// recurse into subnodes
 		for (Node<AstNode> subNode : node.getChildren())
 		{
 			scanTree(result, subNode, newHighlights);
 		}
 	}
 
 	private void addIdToHighlights(FanParserTask result, Map<OffsetRange, Set<ColoringAttributes>> newHighlights, AstNode node, EnumSet<ColoringAttributes> colorAttributes)
 	{
 		// We can't mess the enumset, so work of a copy (slower though)
 		Set<ColoringAttributes> newAttributes = EnumSet.copyOf(colorAttributes);
 
 		AstNode idNode = FanLexAstUtils.getFirstChild(node, new NodeKindPredicate(AstKind.AST_ID));
 		if (hasModifier(node, "static"))
 		{
 			newAttributes.add(ColoringAttributes.STATIC);
 		} else if (hasModifier(node, "override"))
 		{
 			newAttributes.add(ColoringAttributes.CUSTOM1);
 		}
 
 		addToHighlights(result, newHighlights, idNode, newAttributes);
 	}
 
 	private void addToHighlights(FanParserTask result, Map<OffsetRange, Set<ColoringAttributes>> newHighlights, AstNode node, Set<ColoringAttributes> colorAttributes)
 	{
 		if (node != null)
 		{
 			OffsetRange range = FanLexAstUtils.getNodeRange(node);
 			if (range != null)
 			{
 				newHighlights.put(range, colorAttributes);
 			}
 		}
 
 	}
 
 	private static boolean hasModifier(AstNode node, String modifier)
 	{
 		if (node != null)
 		{
 			for (AstNode subNode : node.getChildren())
 			{
 				if (subNode.getKind() == AstKind.AST_MODIFIER)
 				{
 					if (subNode.getNodeText(true).contains(modifier))
 					{
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Highlight interpolated variables in Strings
 	 */
 	private void addStrHighlights(FanParserTask result, Map<OffsetRange, Set<ColoringAttributes>> newHighlights, Node<AstNode> node)
 	{
 		OffsetRange strRange = FanLexAstUtils.getNodeRange(node);
 		String str = FanLexAstUtils.getNodeText(node, result.getParsingResult().inputBuffer);
 		//System.out.println("interpolation : " + str);
 		Matcher matcher = INTERPOLATION.matcher(str);
 		while (matcher.find())
 		{
 			int start = strRange.getStart() + matcher.start();
 			int end = strRange.getStart() + matcher.end();
 			OffsetRange range = new OffsetRange(start, end);
 			newHighlights.put(range, ColoringAttributes.CUSTOM2_SET);
 		}
 	}
 }
