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
 * $Id: JTypeNameExpression.java,v 1.5 2005-03-01 10:55:26 aracic Exp $
  */
 
 package org.caesarj.compiler.ast.phylum.expression;
 
 import org.caesarj.compiler.codegen.CodeSequence;
 import org.caesarj.compiler.context.CBinaryTypeContext;
 import org.caesarj.compiler.context.CExpressionContext;
 import org.caesarj.compiler.context.GenerationContext;
 import org.caesarj.compiler.types.CReferenceType;
 import org.caesarj.compiler.types.CType;
 import org.caesarj.compiler.types.TypeFactory;
 import org.caesarj.util.PositionedError;
 import org.caesarj.util.TokenReference;
 import org.caesarj.util.UnpositionedError;
 
 /**
  * A 'int.class' expression
  */
 public class JTypeNameExpression extends JExpression {
 
   // ----------------------------------------------------------------------
   // CONSTRUCTORS
   // ----------------------------------------------------------------------
 
   /**
    * Construct a node in the parsing tree
    * @param where the line of this node in the source code
    */
 //   public JTypeNameExpression(TokenReference where, String qualifiedName) {
 //     super(where);
 
 //     type = CReferenceType.lookup(qualifiedName);
 //   }
 
   /**
    * Construct a node in the parsing tree
    * @param where the line of this node in the source code
    */
   public JTypeNameExpression(TokenReference where, CReferenceType type) {
     super(where);
 
     this.type = type;
   }
 
   // ----------------------------------------------------------------------
   // ACCESSORS
   // ----------------------------------------------------------------------
 
   /**
    * Compute the type of this expression (called after parsing)
    * @return the type of this expression
    */
   public CType getType(TypeFactory factory) {
     return type;
   }
 
   /**
    * Compute the type of this expression (called after parsing)
    * @return the type of this expression
    */
   public CReferenceType getClassType() {
     return type;
   }
 
   /**
    * Returns a qualified name for the type of this
    */
   public String getQualifiedName() {
     return type.getQualifiedName();
   }
 
   // ----------------------------------------------------------------------
   // SEMANTIC ANALYSIS
   // ----------------------------------------------------------------------
 
   /**
    * Analyses the expression (semantically).
    * @param	context		the analysis context
    * @return	an equivalent, analysed expression
    * @exception	PositionedError	the analysis detected an error
    */
   public JExpression analyse(CExpressionContext context) throws PositionedError {
     try {
       type = (CReferenceType)type.checkType(context);
       
       // calc family
      // CRITICAL: commented out, this has caused errors with statements like System.out.println()
      // the reason is, that binary fields do not know in which context they have been resolved
      //family = type.getPath();
      //thisAsFamily = new FieldAccess(family, type.getQualifiedName(), type);
       
     } catch (UnpositionedError e) {
       throw e.addPosition(getTokenReference());
     }
 
     return this;
   }
   /**
    * Return true iff the node is itself a Expression 
    * (not only a part like JTypeName)
    */
   public boolean isExpression() {
     return false;
   }
 
   /**
    * Used in xkjc/XUtils. DO NOT USE ANYWHERE ELSE!!
    */
   public JExpression analyse(CBinaryTypeContext context) throws PositionedError {
     try {
       type = (CReferenceType)type.checkType(context);
     } catch (UnpositionedError e) {
       throw e.addPosition(getTokenReference());
     }
 
     return this;
   }
 
   // ----------------------------------------------------------------------
   // CODE GENERATION
   // ----------------------------------------------------------------------
 
   /**
    * Generates JVM bytecode to evaluate this expression.
    *
    * @param	code		the bytecode sequence
    * @param	discardValue	discard the result of the evaluation ?
    */
   public void genCode(GenerationContext context, boolean discardValue) {
     CodeSequence code = context.getCodeSequence();
 
     if (! discardValue) {
       setLineNumber(code);
       // do nothing here
     }
   }
 
   // ----------------------------------------------------------------------
   // DATA MEMBERS
   // ----------------------------------------------------------------------
 
   private	CReferenceType		type;
 }
