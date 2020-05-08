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
 * $Id: JVariableDefinition.java,v 1.22 2006-03-06 09:58:03 gasiunas Exp $
  */
 
 package org.caesarj.compiler.ast.phylum.variable;
 
 import org.caesarj.compiler.Log;
 import org.caesarj.compiler.ast.phylum.expression.JArrayInitializer;
 import org.caesarj.compiler.ast.phylum.expression.JExpression;
 import org.caesarj.compiler.ast.phylum.expression.JTypeNameExpression;
 import org.caesarj.compiler.constants.CaesarMessages;
 import org.caesarj.compiler.constants.KjcMessages;
 import org.caesarj.compiler.context.CBodyContext;
 import org.caesarj.compiler.context.CClassContext;
 import org.caesarj.compiler.context.CExpressionContext;
 import org.caesarj.compiler.family.ContextExpression;
 import org.caesarj.compiler.family.Path;
 import org.caesarj.compiler.types.CArrayType;
 import org.caesarj.compiler.types.CClassNameType;
 import org.caesarj.compiler.types.CDependentNameType;
 import org.caesarj.compiler.types.CReferenceType;
 import org.caesarj.compiler.types.CType;
 import org.caesarj.compiler.types.TypeFactory;
 import org.caesarj.util.PositionedError;
 import org.caesarj.util.TokenReference;
 import org.caesarj.util.UnpositionedError;
 
 /**
  * This class represents a local variable definition in the syntax tree
  */
 public class JVariableDefinition extends JLocalVariable {
 
     // ----------------------------------------------------------------------
     // CONSTRUCTORS
     // ----------------------------------------------------------------------
 
     /**
      * Construct a node in the parsing tree This method is directly called by
      * the parser
      * 
      * @param where
      *            the line of this node in the source code
      * @param modifiers
      *            the modifiers of this variable
      * @param ident
      *            the name of this variable
      * @param initializer
      *            the initializer
      */
     public JVariableDefinition(TokenReference where, int modifiers, CType type,
             String ident, JExpression initializer) {
         super(where, modifiers, DES_LOCAL_VAR, type, ident, initializer);
         verify(type != null);
     }
     
     /**
      * @param where
      * @param modifiers
      * @param kind			the kind of the variable
      * @param type
      * @param ident
      * @param initializer
      */
     public JVariableDefinition(TokenReference where, int modifiers, int kind, CType type,
             String ident, JExpression initializer) {
         super(where, modifiers, kind, type, ident, initializer);
         verify(type != null);
     }
 
     // ----------------------------------------------------------------------
     // ACCESSORS
     // ----------------------------------------------------------------------
 
     public void setType(CType type) {
         this.type = type;
     }
 
     /**
      * hasInitializer
      * 
      * @return true if there is an initializer
      */
     public boolean hasInitializer() {
         return getValue() != null;
     }
 
     /**
      * @return the initial value
      */
     public JExpression getValue() {
         return expr;
     }
 
     // ----------------------------------------------------------------------
     // INTERFACE CHECKING
     // ----------------------------------------------------------------------
 
     /**
      * Second pass (quick), check interface looks good Exceptions are not
      * allowed here, this pass is just a tuning pass in order to create
      * informations about exported elements such as Classes, Interfaces,
      * Methods, Constructors and Fields
      * 
      * @param context
      *            the current context
      * @return true iff sub tree is correct enought to check code
      */
     public void checkInterface(CClassContext context) throws PositionedError {
         try {
             type = type.checkType(context);
         } 
         catch (UnpositionedError cue) {
             // checkBody will handle it
         }
     }    
     
     // ----------------------------------------------------------------------
     // SEMANTIC ANALYSIS
     // ----------------------------------------------------------------------
 
     /**
      * Check expression and evaluate and alter context
      * 
      * @param context
      *            the actual context of analyse
      * @exception PositionedError
      *                Error catched as soon as possible
      */
     public void analyse(CBodyContext context) throws PositionedError {
         TypeFactory factory = context.getTypeFactory();
 
         try {
             type = type.checkType(context);
         } catch (UnpositionedError cue) {            
             try {
                 
                 /* 
                  * If the type check fails it may be due to a dependent-type
                  * declaration. So, if the type which failed to check was a 
                  * Classname-type, we mark it as a possible dependent type 
                  * and re-check it later.
                  */
                if (type instanceof CClassNameType) {
                     type = 
                         new CDependentNameType(((CClassNameType)type).getQualifiedName()) 
                     		.checkType(context);
                 }
                else {
                	throw cue.addPosition(getTokenReference());
                }                
             }
             catch (UnpositionedError cue2) {
 	            throw cue2.addPosition(getTokenReference());
             }            
         }
         if (!type.isPrimitive()) {
             // JLS 6.6.1
             // An array type is accessible if and only if its element type is
             // accessible.
             if (type.isArrayType()) {
                 check(context, ((CArrayType) type).getBaseType().isPrimitive()
                         || ((CArrayType) type).getBaseType().getCClass()
                                 .isAccessible(
                                         context.getClassContext().getCClass()),
                         KjcMessages.CLASS_NOACCESS, ((CArrayType) type)
                                 .getBaseType());
             } else {
 	            check(context, type.getCClass().isAccessible(
 	                    context.getClassContext().getCClass()),
 	                    KjcMessages.CLASS_NOACCESS, type);
             }
         }
 
         if (expr != null) {
             // special case for array initializers
             if (expr instanceof JArrayInitializer) {
                 check(context, type.isArrayType(),
                         KjcMessages.ARRAY_INIT_NOARRAY, type);
                 ((JArrayInitializer) expr).setType((CArrayType) type);
             }
 
             CExpressionContext expressionContext = new CExpressionContext(
                     context, context.getEnvironment());
 
             expr = expr.analyse(expressionContext);
             if (expr instanceof JTypeNameExpression) {
                 check(context, false, KjcMessages.VAR_UNKNOWN,
                         ((JTypeNameExpression) expr).getQualifiedName());
             }
 
             check(context, expr.isAssignableTo(context, type),
                     KjcMessages.VAR_INIT_BADTYPE, getIdent(), expr
                             .getType(factory));
             
             // IVICA: check family
             if(getType().isDependentType() /*&& getType().getCClass().isNested()*/) {
                 try {
 		            Path rFam = expr.getFamily();
 		            Path lFam = ((CReferenceType)getType()).getPath();
 		            Log.verbose("INITIALIZER (line "+getTokenReference().getLine()+"):");
 		            Log.verbose("\t"+lFam+" <= "+rFam);
 		            if(lFam != null && rFam != null) {
 		                
 		                // IVICA: if we are in the initialzer context, then calculate -3 to the rightExpression
 		                // Reason is that the path of the field being initialized has been resolved in the class context
 		                if(rFam != Path.NULL && context.getInitializerContext() != null) {
 		                    // adapt the k		                    
 		                    ((ContextExpression)rFam.getHead()).adaptK(-3);		                    
 		                }
 		                
 		                check(context,
 		          	      rFam.isAssignableTo( lFam ),
 		          	      KjcMessages.ASSIGNMENT_BADTYPE, 	rFam+"."+expr.getType(factory).getCClass().getIdent(),   
 		          	      lFam+"."+getType().getCClass().getIdent() );
 		            }
 		            else {
 		    	        check(
 		    	            context,
 		    	            !(lFam!=null ^ rFam!=null),
 		    	            CaesarMessages.ILLEGAL_PATH
 		    	        );	 
 		            }
                 }
                 catch (UnpositionedError e) {
                     throw e.addPosition(getTokenReference());
                 }
             }
             
             expr = expr.convertType(expressionContext, type);
         }
     }
 
 }
