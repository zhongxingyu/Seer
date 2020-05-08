 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright  2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
 * $Id: JReturnStatement.java,v 1.8 2005-02-25 13:45:41 aracic Exp $
  */
 
 package org.caesarj.compiler.ast.phylum.statement;
 
 import org.caesarj.compiler.ast.JavaStyleComment;
 import org.caesarj.compiler.ast.phylum.expression.JExpression;
 import org.caesarj.compiler.ast.phylum.variable.JLocalVariable;
 import org.caesarj.compiler.ast.visitor.IVisitor;
 import org.caesarj.compiler.codegen.CodeSequence;
import org.caesarj.compiler.constants.CaesarMessages;
 import org.caesarj.compiler.constants.KjcMessages;
 import org.caesarj.compiler.context.CBodyContext;
 import org.caesarj.compiler.context.CContext;
 import org.caesarj.compiler.context.CExpressionContext;
 import org.caesarj.compiler.context.CInitializerContext;
 import org.caesarj.compiler.context.CMethodContext;
 import org.caesarj.compiler.context.GenerationContext;
 import org.caesarj.compiler.export.CMethod;
 import org.caesarj.compiler.family.ContextExpression;
 import org.caesarj.compiler.family.Path;
 import org.caesarj.compiler.types.CReferenceType;
 import org.caesarj.compiler.types.CType;
 import org.caesarj.compiler.types.TypeFactory;
 import org.caesarj.util.PositionedError;
 import org.caesarj.util.TokenReference;
 import org.caesarj.util.UnpositionedError;
 
 /**
  * JLS 14.16: Return Statement
  *
  * A return statement returns control to the invoker of a method or constructor.
  */
 public class JReturnStatement extends JStatement {
 
   // ----------------------------------------------------------------------
   // CONSTRUCTORS
   // ----------------------------------------------------------------------
 
  /**
    * Construct a node in the parsing tree
    * @param	where		the line of this node in the source code
    * @param	expr		the expression to return.
    */
   public JReturnStatement(TokenReference where, JExpression expr, JavaStyleComment[] comments) {
     super(where, comments);
     this.expr = expr;
   }
 
   // ----------------------------------------------------------------------
   // ACCESSORS
   // ----------------------------------------------------------------------
 
 
   /**
    * Returns the type of this return statement
    */
   public CType getType(TypeFactory factory) {
     return expr != null ? expr.getType(factory) : factory.getVoidType();
   }
 
 
   // ----------------------------------------------------------------------
   // SEMANTIC ANALYSIS
   // ----------------------------------------------------------------------
 
   /**
    * Analyses the statement (semantically).
    * @param	context		the analysis context
    * @exception	PositionedError	the analysis detected an error
    */
   public void analyse(CBodyContext context) throws PositionedError {
     CType	returnType = context.getMethodContext().getCMethod().getReturnType();
     TypeFactory factory = context.getTypeFactory();
 
     check(context,
           !(context.getMethodContext() instanceof CInitializerContext),
           KjcMessages.RETURN_INITIALIZER);
     
 
     if (expr != null) {
       check(context, returnType.getTypeID() != TID_VOID, KjcMessages.RETURN_NONEMPTY_VOID);
 
       CExpressionContext	expressionContext = new CExpressionContext(context, 
                                                                            context.getEnvironment());
       expr = expr.analyse(expressionContext);
 
       check(context, expr.isExpression(), KjcMessages.NOT_AN_EXPRESSION, expr);
 
       check(context,
 	    expr.isAssignableTo(expressionContext, returnType),
 	    KjcMessages.RETURN_BADTYPE, expr.getType(factory), returnType);
       
 //    IVICA: check family, return type is a caesar type and we are not in the factory method
       try {
           
           CMethod method = context.getMethodContext().getMethodDeclaration().getMethod();
           
 	      if(
 	          // CRITICAL: better check if the method is synthetic
 	          returnType.isCaesarReference() 
 	          && !method.isCaesarFactoryMethod()
 	          && !method.isCaesarAccessorMethod()
 	          && !method.isCaesarWrapperSupportMethod()
 	      ) {
 	            Path rFam = expr.getFamily();
 	            Path lFam = ((CReferenceType)returnType).getPath();
 	            System.out.println("RETURN STATEMENT (line "+getTokenReference().getLine()+"):");
 	            System.out.println("\t"+lFam+" <= "+rFam);
 	            if(lFam != null && rFam != null) {
 	                
 	                // k ^= number of steps to the method context
 	                int k = 0;
 	                CMethodContext mctx = context.getMethodContext();
 	                CContext ctx = context;
 	                while(ctx != mctx) {
 	                    ctx = ctx.getParentContext();
 	                    k++;
 	                }
 	                
 	                // adapt the path of the return expression
 	                Path head = rFam.getHead();
 	                if(head instanceof ContextExpression)
 	                    ((ContextExpression)head).adaptK(-k);
 	                
 	                // check the equivalence
 	                check(context,
 	          	      rFam.isAssignableTo( lFam ),
 	          	      KjcMessages.ASSIGNMENT_BADTYPE, 	rFam+"."+expr.getType(factory).getCClass().getIdent(),   
 	          	      lFam+"."+returnType.getCClass().getIdent() );
 	            }
	            else {
	                check(
	                    context,
	                    !(lFam!=null ^ rFam!=null),
	                    CaesarMessages.ILLEGAL_PATH
	                );	            	                
 	            }
           }
       }
       catch (UnpositionedError e) {
           throw e.addPosition(getTokenReference());
       }      
       
       expr = expr.convertType(expressionContext, returnType);
     } else {
       check(context, returnType.getTypeID() == TID_VOID, KjcMessages.RETURN_EMPTY_NONVOID);
     }
 
     context.setReachable(false);
   }
 
   // ----------------------------------------------------------------------
   // CODE GENERATION
   // ----------------------------------------------------------------------
 
   public void recurse(IVisitor s) {
     expr.accept(s);
   }
 
   /**
    * Generates a sequence of bytescodes
    * @param	code		the code list
    */
   public void genCode(GenerationContext context) {
     CodeSequence        code = context.getCodeSequence();
     TypeFactory         factory = context.getTypeFactory();
 
     setLineNumber(code);
 
     if (expr != null) {
       expr.genCode(context, false);
 
       code.plantReturn(this, context);
       code.plantNoArgInstruction(expr.getType(factory).getReturnOpcode());
     } else {
       code.plantReturn(this, context);
       code.plantNoArgInstruction(opc_return);
     }
   }
 
   /**
    * Load the value from a local var (after finally)
    */
   public void load(GenerationContext context, JLocalVariable var) {
     CodeSequence        code = context.getCodeSequence();
     TypeFactory         factory = context.getTypeFactory();
 
     code.plantLocalVar(expr.getType(factory).getLoadOpcode(), var);
   }
 
   /**
    * Load the value from a local var (after finally)
    */
   public void store(GenerationContext context, JLocalVariable var) {
     CodeSequence        code = context.getCodeSequence();
     TypeFactory         factory = context.getTypeFactory();
 
     code.plantLocalVar(expr.getType(factory).getStoreOpcode(), var);
   }
 
   public JExpression getExpression() {return expr;}
   
   // ----------------------------------------------------------------------
   // DATA MEMBERS
   // ----------------------------------------------------------------------
 
   protected JExpression		expr;
 }
