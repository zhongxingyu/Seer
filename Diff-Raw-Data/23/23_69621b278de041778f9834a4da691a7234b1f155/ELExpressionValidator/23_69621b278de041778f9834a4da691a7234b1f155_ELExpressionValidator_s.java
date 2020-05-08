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
 
 package org.eclipse.jst.jsf.validation.internal.el;
 
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.jst.jsf.common.internal.types.CompositeType;
 import org.eclipse.jst.jsf.common.internal.types.SignatureBasedType;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContext;
 import org.eclipse.jst.jsf.designtime.resolver.IStructuredDocumentSymbolResolverFactory;
 import org.eclipse.jst.jsf.validation.internal.IJSFViewValidator.IValidationReporter;
 import org.eclipse.jst.jsf.validation.internal.el.diagnostics.DiagnosticFactory;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ASTExpression;
 import org.eclipse.jst.jsp.core.internal.java.jspel.JSPELParser;
 import org.eclipse.jst.jsp.core.internal.java.jspel.ParseException;
 import org.eclipse.jst.jsp.core.internal.java.jspel.Token;
 import org.eclipse.jst.jsp.core.internal.java.jspel.TokenMgrError;
 
 /**
  * Validates a single expression string
  * 
  * @author cbateman
  *
  */
 public class ELExpressionValidator 
 {
     private final IStructuredDocumentContext               _context;
     private final String                                   _elText;
     private final DiagnosticFactory                        _diagnosticFactory;
 
     private ASTSemanticValidator                           _semanticValidator;
     private final IStructuredDocumentSymbolResolverFactory _symbolResolverFactory;
     private final IValidationReporter                      _reporter;
 
     /**
      * @param context
      * @param elText
      * @param symbolResolverFactory 
      * @param reporter 
      */
     public ELExpressionValidator(final IStructuredDocumentContext context,
                                  final String elText, 
                                  final IStructuredDocumentSymbolResolverFactory symbolResolverFactory,
                                  final IValidationReporter reporter)
     {
         _context = context;
         _elText = elText;
         _diagnosticFactory = new DiagnosticFactory();
         _symbolResolverFactory = symbolResolverFactory;
         _reporter = reporter;
     }
     
     /**
      * Validates a single EL expression in an XML attribute value
      * @return an ASTExpression for the node
      */
     public ASTExpression validateXMLNode() 
     {
         JSPELParser elParser = JSPELParser.createParser(_elText);
         // = 
         try {
             //final long startParsing = System.currentTimeMillis();
             final ASTExpression expr =  elParser.Expression();
             //final long endParsing = System.currentTimeMillis();
             //final long startSemantics = System.currentTimeMillis();
             validateSemantics(expr, _context);
             //final long endSemantics = System.currentTimeMillis();
 
             //System.out.println("Time to parse '"+elText+"' = "+(endParsing-startParsing));
             //System.out.println("Time to semantic checking '"+elText+"' = "+(endSemantics-startSemantics));
             
             // if the parser bailed before parsing the whole
             // expression, raise a warning that there is probably
             // some syntatical issue
             if (expr.getLastToken().endColumn < _elText.trim().length()-1)
             {
             	if (ELValidationUtil.isProjectEL22(_context) &&
             			_elText.trim().charAt(expr.getLastToken().endColumn) == '(')
             	{
             		return expr;
             	}
                 final int offset = _context.getDocumentPosition() + expr.getLastToken().endColumn;
                 final int length = _elText.trim().length() - expr.getLastToken().endColumn;
                 final Diagnostic diagnostic =
                     _diagnosticFactory.create_GENERAL_SYNTAX_ERROR();
                 
                 _reporter.report(diagnostic, offset, length);
             }
             
             return expr;
         }
         catch (ParseException e) {
             Token curTok = e.currentToken;
             int offset = _context.getDocumentPosition() + curTok.beginColumn;
             int length = curTok.endColumn - curTok.beginColumn + 1;
             final Diagnostic diagnostic = _diagnosticFactory.create_GENERAL_SYNTAX_ERROR();
             _reporter.report(diagnostic, offset, length);
             return null;
         }
         catch (TokenMgrError te) {
             final int offset = _context.getDocumentPosition();
             final int length = _elText.length();
             final Diagnostic diagnostic =
                 _diagnosticFactory.create_GENERAL_SYNTAX_ERROR();
             _reporter.report(diagnostic, offset, length);
             return null;
         }
     }
 
     /**
      * @return the type of the expression or null if 
      * the type could not be evaluated
      */
     public CompositeType getExpressionType()
     {
         if (_semanticValidator != null)
         {
             final SignatureBasedType type = 
                 _semanticValidator.getExpressionType();
             
             if (type != null)
             {
                 return type.toCompositeType();
             }
         }
         
         return null;
     }
     
 
     /**
      * @return the EL semantic validator for this expression validitor or
      * null if one has not yet been constructor.  One will only be construct
      * if validate is called and the expression does not have any syntax errors.
      */
     public IExpressionSemanticValidator getSemanticValidator()
     {
         return _semanticValidator;
     }
     
     /**
      * Validates the context-specific data for one EL expressino
      * 
      * @param expr
      * @param file
      * @param reporter
      * @param context
      */
     private void validateSemantics(ASTExpression expr,IStructuredDocumentContext context)
     {
         _semanticValidator = new ASTSemanticValidator(expr, context, _symbolResolverFactory, _reporter);
         _semanticValidator.validate();
     }
 }
