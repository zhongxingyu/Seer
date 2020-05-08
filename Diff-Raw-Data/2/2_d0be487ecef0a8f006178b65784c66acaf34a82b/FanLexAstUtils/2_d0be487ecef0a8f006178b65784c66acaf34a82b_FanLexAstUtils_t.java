 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.antlr;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import javax.swing.text.Document;
 import net.colar.netbeans.fan.FanParserResult;
 import net.colar.netbeans.fan.FanTokenID;
 import org.antlr.runtime.CommonToken;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.tree.CommonTree;
 import org.antlr.runtime.tree.Tree;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.api.lexer.TokenHierarchy;
 import org.netbeans.api.lexer.TokenSequence;
 import org.netbeans.modules.csl.api.OffsetRange;
 
 /**
  * Utilities for lexer / parser / ast trees
  * @author tcolar
  */
 public class FanLexAstUtils
 {
 
 	/**
 	 * Return the Actual token at the given caret position in the document.
 	 * @param doc
 	 * @param caretIndex
 	 * @return
 	 */
 	public static Token<? extends FanTokenID> getFanTokenAt(Document doc, int caretIndex)
 	{
 		TokenSequence ts = getFanTokenSequence(doc);
 		ts.move(caretIndex);
 		ts.moveNext();
 		Token<? extends FanTokenID> token = ts.token();
 		return token;
 	}
 
 	/**
 	 * Returns the tokensequence from adoc
 	 * @param doc
 	 * @return
 	 */
 	public static TokenSequence<? extends FanTokenID> getFanTokenSequence(Document doc, int offset)
 	{
 		//System.err.println("doc:" + doc);
 		TokenHierarchy<Document> th = TokenHierarchy.get(doc);
 		//System.err.println("hierarchy: " + th);
 		return getFanTokenSequence(th, offset);
 	}
 
 	public static TokenSequence<? extends FanTokenID> getFanTokenSequence(Document doc)
 	{
 		return getFanTokenSequence(doc, 0);
 	}
 
 	/**
 	 * Returns the tokensequence from th tokenhierarchy
 	 * @param th
 	 * @return
 	 */
 	public static TokenSequence<? extends FanTokenID> getFanTokenSequence(TokenHierarchy<?> th, int offset)
 	{
 		TokenSequence<? extends FanTokenID> ts = th.tokenSequence(FanTokenID.language());
 		if (offset != 0)
 		{
 			ts = ts.subSequence(offset);
 		}
 		return ts;
 	}
 
 	/**
 	 * Find the closing token matching the opening one (balanced) and return the range between the two
 	 * @param doc
 	 * @param ts
 	 * @param opening
 	 * @param closing
 	 * @return
 	 */
 	public static OffsetRange findRangeFromOpening(Document doc, TokenSequence<? extends FanTokenID> ts, int openingTokenID, int closingTokenID)
 	{
 		return findRange(doc, ts, openingTokenID, closingTokenID, true);
 	}
 
 	/**
 	 * Find (backward) the opening token matching the closing one (balanced) and return the range between the two
 	 * @param doc
 	 * @param ts
 	 * @param opening
 	 * @param closing
 	 * @return
 	 */
 	public static OffsetRange findRangeFromClosing(Document doc, TokenSequence<? extends FanTokenID> ts, int openingTokenID, int closingTokenID)
 	{
 		return findRange(doc, ts, openingTokenID, closingTokenID, false);
 	}
 
 	/**
 	 * Looking for matching(balanced) opening/closing token
 	 * @param doc
 	 * @param ts
 	 * @param openingID
 	 * @param closingID
 	 * @param lookForClosing (false=lookForOpening  - backward)
 	 * @return
 	 */
 	private static OffsetRange findRange(Document doc, TokenSequence<? extends FanTokenID> ts, int openingID, int closingID, boolean lookForClosing)
 	{
 		int balance = 0;
 
 		while (move(ts, lookForClosing))
 		{
 			Token<? extends FanTokenID> token = ts.token();
 			int ord = token.id().ordinal();
 
 			if (ord == openingID)
 			{
 				if ((!lookForClosing) && balance == 0)
 				{
 					return new OffsetRange(ts.offset(), ts.offset() + token.length());
 				}
 				balance++;
 			} else if (ord == closingID)
 			{
 				if (lookForClosing && balance == 0)
 				{
 					return new OffsetRange(ts.offset(), ts.offset() + token.length());
 				}
 				balance--;
 			}
 		}
 
 		return OffsetRange.NONE;
 	}
 
 	private static boolean move(TokenSequence ts, boolean forward)
 	{
 		if (forward)
 		{
 			return ts.moveNext();
 		} else
 		{
 			return ts.movePrevious();
 		}
 	}
 
 	/**
 	 * Return the range (start/end position in source file) of an AST node
 	 * for a given FanParserResult (using the ANTLR tokenStream)
 	 * @param result
 	 * @param node
 	 * @return range or null if range is invalid
 	 */
 	public static OffsetRange getNodeRange(FanParserResult result, CommonTree node)
 	{
 		if (node == null)
 		{
 			return null;
 		}
 		OffsetRange range = null;
 		try
 		{
 			CommonTokenStream tokenStream = result.getTokenStream();
 			CommonToken startToken = (CommonToken) tokenStream.get(node.getTokenStartIndex());
 			CommonToken endToken = (CommonToken) tokenStream.get(node.getTokenStopIndex());
 			if (startToken.getStartIndex() >= 0 && endToken.getStopIndex() >= startToken.getStartIndex())
 			{
 				range = new OffsetRange(startToken.getStartIndex(), endToken.getStopIndex() + 1);
 			}
 		} catch (ArrayIndexOutOfBoundsException e)
 		{
 			System.err.println("Incomplete node ? " + node.getText());
 		}
 		return range;
 	}
 
 	/**
 	 * Get the range of the whole node content (not just the node itself)
 	 * @param fanParserResult
 	 * @param idNode
 	 * @return
 	 */
 	public static OffsetRange getContentNodeRange(FanParserResult fanParserResult, CommonTree node)
 	{
 		OffsetRange range = null;
 		if (node.getChildCount() >= 0)
 		{
 			CommonTree startNode = (CommonTree) node.getChild(0);
 			CommonTree endNode = (CommonTree) node.getChild(node.getChildCount() - 1);
 			OffsetRange range1 = getNodeRange(fanParserResult, startNode);
 			OffsetRange range2 = getNodeRange(fanParserResult, endNode);
 			if (range1 != null && range2 != null)
 			{
 				range = new OffsetRange(range1.getStart(), range2.getEnd());
 			}
 		}
 		return range;
 	}
 
 	/**
 	 * Finds the closest(smallest) AST node at given lexerIndex (position in source doc.)
 	 * Returns root node if no better macth found.
 	 */
 	public static CommonTree findASTNodeAt(FanParserResult pResult, int lexerIndex)
 	{
		int offset = offsetToTokenIndex(pResult, lexerIndex);
 		CommonTree node=findASTNodeAt(pResult, pResult.getTree(), offset);
 		// If not found, return the root
 		if(node==null) node=pResult.getRootScope().getAstNode();
 		return node;
 	}
 
 	/**
 	 * Finds the closest(smallest) AST node at given lexerIndex (position in source doc.)
 	 * recursive
 	 * @param pResult
 	 * @param node
 	 * @param lexerIndex
 	 * @return Null if not found
 	 */
 	private static CommonTree findASTNodeAt(FanParserResult pResult, CommonTree node, int tokenIndex)
 	{
 		List<CommonTree> children = node.getChildren();
 		if (children != null)
 		{
 			Iterator<CommonTree> it = children.iterator();
 			while (it.hasNext())
 			{
 				CommonTree subNode = it.next();
 				int start = getTokenStart(subNode);
 				int stop = getTokenStop(subNode);
 				System.out.println("li:" + tokenIndex + " start:" + start + " stop:" + stop + " " + subNode.getType() + " " + getNodeContent(pResult, subNode));
 				if (start == -1 || stop == -1)
 				// incomplete token return the parent
 				{
 					continue;
 				}
 
 				// <= >= ??
 				if (start <= tokenIndex && stop >= tokenIndex)
 				{
 					CommonTree nextNode = findASTNodeAt(pResult, subNode, tokenIndex);
 					// tokenIndex was in Node Range but is NOT in subnode
 					// then node was the node we where looking for
 					return nextNode != null ? nextNode : node;
 				}
 			}
 		}
 		// Not found.
 		return null;
 	}
 
 	public static int getLineEndOffset(TokenSequence seq, int offset, boolean semiIsNL)
 	{
 		//System.out.println(">gleo " + offset);
 		int result = -1;
 		seq.move(offset);
 		// check if mve failed -> =~ end of stream
 		if (!seq.moveNext() || seq.offset() < offset)
 		{
 			return -1;
 		}
 		do
 		{
 			//System.out.println("seq: "+seq.offset());
 			// If this happens, we reached end of stream
 			int tokenType = seq.token().id().ordinal();
 			if (tokenType == FanLexer.LB || (semiIsNL && tokenType == FanLexer.SP_SEMI))
 			{
 				result = seq.offset();
 				break;
 			}
 		} while (seq.moveNext());
 		// put it back where it was.
 		seq.move(offset);
 		//System.out.println("<gleo "+result);
 		return result;
 	}
 
 	/**
 	 * Return the offset of the beginiing of this line (after a LineBreak or beginning of file)
 	 * @param seq
 	 * @param offset
 	 * @param semiIsNL Wether a ; is considered a beginning of line
 	 * @return
 	 */
 	public static int getLineBeginOffset(TokenSequence seq, int offset, boolean semiIsNL)
 	{
 		seq.move(offset);
 		while (seq.movePrevious())
 		{
 			//System.out.println("seq2: "+seq.offset());
 			int tokenType = seq.token().id().ordinal();
 			if (tokenType == FanLexer.LB || (semiIsNL && tokenType == FanLexer.SP_SEMI))
 			{
 				break;
 			}
 		}
 		int result = seq.offset() + 1;
 		// put it back where it was.
 		seq.move(offset);
 		return result;
 	}
 
 	/**
 	 * Move to next non whitespace token
 	 * @param seq
 	 * @param fromOfset starting offset
 	 * @param maxOffset look no further than this
 	 * @return true if non WS token found before maxOffset
 	 */
 	public static boolean moveToNextNonWSToken(TokenSequence seq, int fromOfset, int maxOffset)
 	{
 		if (fromOfset > maxOffset || fromOfset > seq.tokenCount())
 		{
 			return false;
 		}
 		seq.move(fromOfset);
 		while (seq.moveNext() && seq.offset() <= maxOffset)
 		{
 			int tokenType = seq.token().id().ordinal();
 			if (!matchType(tokenType, FanGrammarHelper.WS_TOKENS))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static boolean moveToPrevNonWSToken(TokenSequence seq, int fromOfset, int minOffset)
 	{
 		if (fromOfset < minOffset || fromOfset < 0)
 		{
 			return false;
 		}
 		seq.move(fromOfset);
 		while (seq.movePrevious() && seq.offset() >= minOffset)
 		{
 			int tokenType = seq.token().id().ordinal();
 			if (!matchType(tokenType, FanGrammarHelper.WS_TOKENS))
 			{
 				// put it back BEFORE the token
 				//seq.movePrevious();
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Move to next non significant token (ie: non whitespace / comments/ doc token)
 	 * @param seq
 	 * @param fromOfset starting offset
 	 * @param maxOffset look no further than this
 	 * @return true if non significant token found before maxOffset
 	 */
 	public static boolean moveToNextSignificantToken(TokenSequence seq, int fromOfset, int maxOffset)
 	{
 		if (fromOfset > maxOffset || fromOfset > seq.tokenCount())
 		{
 			return false;
 		}
 		while (seq.offset() <= maxOffset && seq.moveNext() && seq.offset() <= maxOffset)
 		{
 			int tokenType = seq.token().id().ordinal();
 			if (!matchType(tokenType, FanGrammarHelper.INSIGNIFICANT_TOKENS))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static boolean matchType(int type, int[] array)
 	{
 		for (int i = 0; i != array.length; i++)
 		{
 			if (array[i] == type)
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Return the offset of the start of the next line.
 	 * Max offset is the maximum, if not found by then, return -1;
 	 * @param offset
 	 * @param maxOffset
 	 * @return
 	 */
 	public static int nextLineStartOffset(TokenSequence seq, int offset, int maxOffset)
 	{
 		//System.out.println(">nlso");
 		int of = getLineEndOffset(seq, offset, false);
 		if (of > -1)
 		{
 			of += 1;
 		}
 		//System.out.println("of: "+of);
 		/*if(of > seq.)
 		return -1;*/
 		return of;
 	}
 
 	public static int getPrevLineOffset(Document document, int startOfLine)
 	{
 		int result = -1;
 		TokenSequence seq = getFanTokenSequence(document);
 		seq.move(startOfLine);
 		while (seq.movePrevious())
 		{
 			System.out.println("seq3: " + seq.offset());
 
 			if (seq.token().id().ordinal() == FanLexer.LB)
 			{
 				break;
 			}
 		}
 		result = seq.offset();
 		return result;
 	}
 
 	/**
 	 * Try to find a parent node matching the given type.
 	 * @param curNode
 	 * @return null if not found
 	 */
 	public static CommonTree findParentNode(final CommonTree theNode, int parentType)
 	{
 		// don't want to mess with the original node.
 		CommonTree node = theNode;
 		while (node != null && !node.isNil())
 		{
 			//System.out.println(""+node.getType()+" VS "+parentType+" "+node.toStringTree());
 			if (node.getType() == parentType)
 			{
 				return node;
 			}
 			node = (CommonTree) node.getParent();
 		}
 		// not found
 		return null;
 	}
 
 	public static String getNodeContent(FanParserResult result, Tree node)
 	{
 		if (node == null)
 		{
 			return "";
 		}
 		OffsetRange range = getContentNodeRange(result, (CommonTree) node);
 		String text = "";
 		try
 		{
 			if (range != null)
 			{
 				text = result.getDocument().getText(range.getStart(), range.getLength());
 			}
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return text;
 	}
 
 	public static List<CommonTree> getAllChildrenWithType(CommonTree node, int type)
 	{
 		return getAllChildrenWithType(node, type, false);
 	}
 
 	public static CommonTree getFirstChildrenWithType(CommonTree node, int type)
 	{
 		List<CommonTree> children = getAllChildrenWithType(node, type, true);
 		return children.size() == 0 ? null : children.get(0);
 	}
 
 	private static List<CommonTree> getAllChildrenWithType(CommonTree node, int type, boolean returnFirst)
 	{
 		List<CommonTree> children = new ArrayList<CommonTree>();
 		for (CommonTree child : (List<CommonTree>) node.getChildren())
 		{
 			if (child.getType() == type)
 			{
 				children.add(child);
 				if (returnFirst)
 				{
 					return children;
 				}
 			}
 		}
 		return children;
 	}
 
 	/**
 	 * Dump a CommonTree node to system.out
 	 * @param t
 	 * @param indent 0 - used in recusrion.
 	 */
 	public static void dumpTree(CommonTree t, int indent)
 	{
 		if (t != null)
 		{
 			StringBuffer sb = new StringBuffer(indent);
 			for (int i = 0; i < indent; i++)
 			{
 				sb = sb.append("  ");
 			}
 			for (int i = 0; i < t.getChildCount(); i++)
 			{
 				System.err.println(sb.toString() + t.getChild(i).toString());
 				dumpTree((CommonTree) t.getChild(i), indent + 1);
 			}
 		}
 	}
 
 	public static boolean isParentNodeOf(CommonTree parent, CommonTree node)
 	{
 		if (node == null)
 		// we went back to the root and it did not match
 		{
 			return false;
 		}
 		if (node == parent)
 		// match
 		{
 			return true;
 		} else
 		// recurse to keep looking in uper levels
 		{
 			return isParentNodeOf(parent, (CommonTree) node.getParent());
 		}
 	}
 
 	public static String getChildTextByType(FanParserResult result, CommonTree node)
 	{
 		return getChildTextByType(result, node, -1);
 	}
 
 	/**
 	 * - if itemIndex==-1 return the whole content of the node
 	 * - if itemIndex!=-1 return the content of the subchild of the node at the given index
 	 */
 	public static String getChildTextByType(FanParserResult result, CommonTree node, int itemIndex)
 	{
 		String text = "";
 		if (node != null)
 		{
 			if (itemIndex != -1 && itemIndex < node.getChildCount())
 			{
 				// return a specific child content (by index)
 				CommonTree node2 = (CommonTree) node.getChild(itemIndex);
 				if (node2 != null)
 				{
 					text = getNodeContent(result, node2);
 				}
 			} else
 			{
 				// return whole node content
 				text = getNodeContent(result, node);
 			}
 		}
 		if (text == null)
 		{
 			text = "";
 		}
 		return text;
 	}
 
 	public static String getSubChildTextByType(FanParserResult result, CommonTree node, int astType)
 	{
 		return getSubChildTextByType(result, node, astType, -1);
 	}
 
 	/**
 	 * Return the content of the first subchild of given type
 	 * - Find the suchild node of the first matching type
 	 * - if itemIndex==-1 return the whole content of the node
 	 * - if itemIndex!=-1 return the content of the subchild of the node at the given index
 	 */
 	public static String getSubChildTextByType(FanParserResult result, CommonTree node, int astType, int itemIndex)
 	{
 		String text = "";
 		if (node != null)
 		{
 			CommonTree node2 = (CommonTree) node.getFirstChildWithType(astType);
 			text = getChildTextByType(result, node2, itemIndex);
 		}
 		return text;
 	}
 
 	/**
 	 * Find the index of the last token of the given type
 	 * -1 if not found
 	 * @param result
 	 * @param node
 	 * @param type
 	 * @return
 	 */
 	public static int findLastTokenIndexByType(FanParserResult result, CommonTree node, int type)
 	{
 		int first = node.getTokenStartIndex();
 		int last = node.getTokenStopIndex();
 		CommonTokenStream ts = result.getTokenStream();
 		for (int i = last; i >= first; i--)
 		{
 			if (ts.get(i).getType() == type)
 			{
 				System.out.println("Matched token:" + ts.get(i).getText());
 				return i;
 			} else
 			{
 				System.out.println("Skipping token:" + ts.get(i).getText());
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * Return the start index of a node
 	 * If a node has no start index check the first children with a startindex
 	 * @param termBase
 	 * @return
 	 */
 	public static int getTokenStart(CommonTree node)
 	{
 		int index = node.getTokenStartIndex();
 		if (index == -1)
 		{
 			if (!node.isNil() && node.getChildCount() > 0)
 			{
 				for (CommonTree child : (List<CommonTree>) node.getChildren())
 				{
 					index = getTokenStart(child);
 					if (index != -1)
 					{
 						return index;
 					}
 				}
 			}
 		}
 		return index;
 	}
 
 	/**
 	 * Return the stop index of a node
 	 * If a node has no start index check the first children with a startindex
 	 * @param termBase
 	 * @return
 	 */
 	public static int getTokenStop(CommonTree node)
 	{
 		int index = node.getTokenStopIndex();
 		if (index == -1)
 		{
 			if (!node.isNil() && node.getChildCount() > 0)
 			{
 				List<CommonTree> children = node.getChildren();
 				for (int i = children.size() - 1; i >= 0; i--)
 				{
 					CommonTree child = children.get(i);
 					index = getTokenStop(child);
 					if (index != -1)
 					{
 						return index;
 					}
 				}
 			}
 		}
 		return index;
 	}
 
 	public static String getTokenStreamSlice(CommonTokenStream tokenStream, int start, int stop)
 	{
 		//TODO: is that good, this wil not have WS etc ...
 		List<CommonToken> tokens = tokenStream.getTokens(start, stop);
 		StringBuffer text = new StringBuffer();
 		for (CommonToken tk : tokens)
 		{
 			text.append(tk.getText());
 		}
 		return text.toString();
 	}
 
 	/**
 	 * Convert the document offset (text) to a token index
 	 * @param pResult
 	 * @return
 	 */
 	public static int offsetToTokenIndex(FanParserResult pResult, int index)
 	{
 		TokenSequence ts = getFanTokenSequence(pResult.getDocument());
 		int saved = ts.index();
 		ts.move(index);
 		ts.movePrevious();
 		int result = ts.index();
 		// restore
 		ts.move(saved);
 		return result;
 	}
 }
