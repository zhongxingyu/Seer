 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 
 package org.eclipse.jst.jsf.core.internal.contentassist.el;
 
 import org.eclipse.jface.text.Region;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContext;
 import org.eclipse.jst.jsf.context.symbol.ISymbol;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ASTAddExpression;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ASTAndExpression;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ASTChoiceExpression;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ASTEqualityExpression;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ASTExpression;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ASTFunctionInvocation;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ASTLiteral;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ASTMultiplyExpression;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ASTOrExpression;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ASTRelationalExpression;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ASTUnaryExpression;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ASTValue;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ASTValuePrefix;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ASTValueSuffix;
 import org.eclipse.jst.jsp.core.internal.java.jspel.JSPELParser;
 import org.eclipse.jst.jsp.core.internal.java.jspel.JSPELParserConstants;
 import org.eclipse.jst.jsp.core.internal.java.jspel.JSPELParserVisitor;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ParseException;
 import org.eclipse.jst.jsp.core.internal.java.jspel.SimpleNode;
 import org.eclipse.jst.jsp.core.internal.java.jspel.Token;
 import org.eclipse.jst.jsp.core.internal.java.jspel.TokenMgrError;
 
 /**
  * Consumes an EL expression and converts into a completion prefix
  * 
  * @author cbateman
  *
  */
 public final class ContentAssistParser 
 {
     /**
      * @param relativePosition -- 1-based position in elText (first position is 1)
      * @param elText
      * @return a content assist strategy for the given position and el expression
      * or null if one cannot be determined
      */
     public static ContentAssistStrategy getPrefix(final int relativePosition, final String elText)
     {
         if (elText == null)
         {
             return null;
         }
         else if ("".equals(elText.trim())) //$NON-NLS-1$
         {
             return new IdCompletionStrategy("", "");  //$NON-NLS-1$//$NON-NLS-2$
         }
         
         PrefixVisitor visitor = getVisitorForPosition(relativePosition, elText);
         return visitor != null? visitor.getPrefix() : null;
     }
     
     /**
      * Get symbol and symbol region at given position in el string
      * @param context - IStructuredDocumentContext
      * @param relativePosition - position in el string
      * @param elText - el string
      * @return SymbolInfo. May be null.
      */
     public static SymbolInfo getSymbolInfo(IStructuredDocumentContext context, final int relativePosition, final String elText) {
         if (elText == null || "".equals(elText.trim())) //$NON-NLS-1$
         {
             return null;
         }
         PrefixVisitor visitor = getVisitorForPosition(relativePosition, elText);
         if (visitor != null) {
             SymbolInfo symbolInfo = visitor.getSymbolInfo(context);
             if (symbolInfo != null) {
                 Region r = symbolInfo.getRelativeRegion();
                 if (relativePosition > r.getOffset() && relativePosition <= r.getOffset() + r.getLength()) {
                     return symbolInfo;
                 }
             }
         }
         return null;
     }
 
 	private static PrefixVisitor getVisitorForPosition(final int relativePosition,
 			final String elText) {
 		final java.io.StringReader reader = new java.io.StringReader(elText);
         final JSPELParser  parser = new JSPELParser(reader);
         
         try
         {
             final ASTExpression expr = parser.Expression();
             final PrefixVisitor visitor = new PrefixVisitor(relativePosition, elText);
             expr.jjtAccept(visitor, null);
             return visitor;
         }
         catch (ParseException pe)
         {
             // TODO: handle parser by using current and expected tokens
         	return null;
         }
         catch (TokenMgrError tme)
         {
             // TODO: handle parser by using current and expected tokens
         	return null;
         }
 	}
     
     private static String substring(String s, Region r) {
        if (s == null && s.isEmpty())
         {
             return ""; //$NON-NLS-1$
         }
         return s.substring(r.getOffset(), r.getOffset() + r.getLength());
     }
     
     private static class PrefixVisitor implements JSPELParserVisitor
     {
         private final int       _relativePos;
         private final String    _fullText;
         
         private String          _symbolPrefix; // = null; initialized as tree is visited
         private int             _prefixType;
         private boolean         _prefixResolved;  // = false; set to true when the prefix is resolved
         private int             _symbolStartPos = 1; // first char has position 1
         private int             _symbolEndPos = 0;
         
         PrefixVisitor(final int relativePos, final String fullText)
         {
             _relativePos = relativePos;
             _fullText = fullText;
         }
         
         /**
          * @return the prefix if resolved or null if not resolved
          */
         public ContentAssistStrategy getPrefix()
         {
             if (_prefixResolved)
             {
                 switch(_prefixType)
                 {
                     case ContentAssistStrategy.PREFIX_TYPE_DOT_COMPLETION:
                         return new FunctionCompletionStrategy(_symbolPrefix, getProposalStart());
                     
                     case ContentAssistStrategy.PREFIX_TYPE_ID_COMPLETION:
                         return new IdCompletionStrategy(_symbolPrefix, getProposalStart());
                     
                     case ContentAssistStrategy.PREFIX_TYPE_EMPTY_EXPRESSION:
                         return new IdCompletionStrategy("", getProposalStart()); //$NON-NLS-1$
                         
                     default:
                         // do nothing; fall-through to return null
                 }
             }
 
             return null;
         }
         
         /**
          * @param context - IStructuredDocumentContext
          * @return symbol and symbol region if resolved, null otherwise
          */
         public SymbolInfo getSymbolInfo(IStructuredDocumentContext context)
         {
             if (_prefixResolved && _symbolStartPos < _symbolEndPos)
             {
                 Region region = new Region(_symbolStartPos - 1, _symbolEndPos - _symbolStartPos + 1);
                 ISymbol symbol = null;
                 switch (_prefixType)
                 {
                     case ContentAssistStrategy.PREFIX_TYPE_ID_COMPLETION:
                         symbol = SymbolResolveUtil.getSymbolForVariable(context, substring(_fullText, region));
                         break;
                     case ContentAssistStrategy.PREFIX_TYPE_DOT_COMPLETION:
                         symbol = SymbolResolveUtil.getSymbolForVariableSuffixExpr(context, _symbolPrefix
                                 + "." + substring(_fullText, region), _symbolEndPos == (_fullText != null ? _fullText.length():0)); //$NON-NLS-1$
                         break;
                 }
                 if (symbol != null) { return new SymbolInfo(symbol, region); }
             }
             return null;
         }
 
 		private String getProposalStart() {
             if (_symbolStartPos <= _relativePos && _fullText != null) {
                 return _fullText.substring(_symbolStartPos - 1, _relativePos - 1);
             }
             return ""; //$NON-NLS-1$
 		}
         
         public Object visit(ASTAddExpression node, Object data) 
         {
             return node.childrenAccept(this, data);
         }
 
 		public Object visit(ASTAndExpression node, Object data) 
         {
             return node.childrenAccept(this, data);
         }
 
         public Object visit(ASTChoiceExpression node, Object data) 
         {
             return node.childrenAccept(this, data);
         }
 
         public Object visit(ASTEqualityExpression node, Object data) 
         {
             return node.childrenAccept(this, data);
         }
 
         public Object visit(ASTExpression node, Object data) 
         {
             return node.childrenAccept(this, data);
         }
 
         public Object visit(ASTFunctionInvocation node, Object data) 
         {
             return node.childrenAccept(this, data);
         }
 
         public Object visit(ASTLiteral node, Object data) 
         {
             return node.childrenAccept(this, data);
         }
 
         public Object visit(ASTMultiplyExpression node, Object data) 
         {
             return node.childrenAccept(this, data);
         }
 
         public Object visit(ASTOrExpression node, Object data) 
         {
             return node.childrenAccept(this, data);
         }
 
         public Object visit(ASTRelationalExpression node, Object data) 
         {
             return node.childrenAccept(this, data);
         }
 
         public Object visit(ASTUnaryExpression node, Object data) 
         {
             return node.childrenAccept(this, data);
         }
 
         public Object visit(ASTValue node, Object data) 
         {
             // we're only in this value expr if it contains the cursor
             if (testContainsCursor(node))
             {
                 return node.childrenAccept(this, data);
             }
             
             return null;
         }
 
         public Object visit(ASTValuePrefix node, Object data) 
         {
             // for now, only concern ourselves with simple (identifier) prefixes
             if (!_prefixResolved
                     && node.jjtGetNumChildren() == 0
                     && node.getFirstToken().kind == JSPELParserConstants.IDENTIFIER)
             {
                 _symbolPrefix = node.getFirstToken().image;
                 
                 if (testContainsCursor(node))
                 {
                     // if the cursor is on this id, we don't need to visit
                     // further since we know both the prefix -- the id -- and
                     // the type -- it's an id completion
                     _prefixType = ContentAssistStrategy.PREFIX_TYPE_ID_COMPLETION;
                     _symbolStartPos = node.getFirstToken().beginColumn;
                     _symbolEndPos = node.getFirstToken().endColumn;
                     _prefixResolved = true;
                 }
             }
             return node.childrenAccept(this, data);
         }
 
         public Object visit(ASTValueSuffix node, Object data) 
         {
             // for now, only deal with the simple .id suffix
             Token lastToken = node.getLastToken();
 			if (node.jjtGetNumChildren() == 0)
             {
                 if (!_prefixResolved
                       && node.getFirstToken().kind == JSPELParserConstants.DOT)
                 {
                     if (lastToken.kind == JSPELParserConstants.IDENTIFIER)
                     {
                         if (testContainsCursor(node))
                         {
                             _prefixType = ContentAssistStrategy.PREFIX_TYPE_DOT_COMPLETION;
                             int proposalStartLength = _relativePos - lastToken.beginColumn;
                             if (proposalStartLength < 0) { // Cursor after firstToken start but before lastToken start?
                             	proposalStartLength = 0;
                             }
                             _symbolStartPos = lastToken.beginColumn;
                             _symbolEndPos = lastToken.endColumn;
                             _prefixResolved = true;
                         }
                         // only include this suffix on the path if the cursor is 
                         // further to the right.  Thus for x.^y we get a prefix "x"
                         // and for x.y.^z we get "x.y" since this the part we must
                         // resolve the prefix for
                         else
                         {
                             _symbolPrefix += node.getFirstToken().image + lastToken.image;
                         }
                     }
                     else if (lastToken == node.getFirstToken())
                     {
                         if (testCursorImmediatelyAfter(node))
                         {
                             _prefixType = ContentAssistStrategy.PREFIX_TYPE_DOT_COMPLETION;
                             _symbolStartPos = lastToken.endColumn + 1;
                             _symbolEndPos = lastToken.endColumn;
                             _prefixResolved = true;
                         }
                     }
                 }
 
                 return null;                
             }
             
             if (node.getFirstToken().kind == JSPELParserConstants.LBRACKET)
             {
                 // try to support ca inside the brackets
                 node.childrenAccept(this, data);
             }
 
             Object retValue =  node.childrenAccept(this, data);
                 
             if (!_prefixResolved && _fullText!=null)
             {
                 // if we haven't resolved the prefix yet, then we need
                 // to append this suffix value
                 _symbolPrefix += _fullText.substring(node.getFirstToken().beginColumn-1, node.getLastToken().endColumn);
             }
             
             return retValue;
         }
 
         public Object visit(SimpleNode node, Object data) 
         {
             return node.childrenAccept(this, data);
         }
         
         private boolean testCursorImmediatelyAfter(SimpleNode node)
         {
             return node.getLastToken().endColumn == _relativePos-1;
         }
         
         /**
          * "Containing a cursor" here is deemed to mean that current cursor
          * position as indicated by _relativePos, is either directly before, on or
          * directly after an expression.  For example, in a Value expression like
          * 
          *          x x x . y y y . z z z
          *         ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^
          *         1 2 3 4 5 6 7 8 9 0 1 2
          *         
          * Position's 1-4 are on xxx, 5-8 are on yyy and 9-12 are on zzz
          * 
          * @param node
          * @return true if the node "contains the cursor" (see above)
          */
         private boolean testContainsCursor(SimpleNode node)
         {
             return (node.getFirstToken().beginColumn <= _relativePos
                     && node.getLastToken().endColumn+1 >= _relativePos);
                 
         }
     }
     
     private ContentAssistParser()
     {
         // utility class; not instantiable
     }
 }
