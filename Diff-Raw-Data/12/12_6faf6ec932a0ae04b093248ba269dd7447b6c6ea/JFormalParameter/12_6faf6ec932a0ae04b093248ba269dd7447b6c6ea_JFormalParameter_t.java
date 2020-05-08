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
 * $Id: JFormalParameter.java,v 1.6 2005-01-27 15:20:02 aracic Exp $
  */
 
 package org.caesarj.compiler.ast.phylum.variable;
 
 import org.caesarj.compiler.context.CBodyContext;
 import org.caesarj.compiler.context.CTypeContext;
 import org.caesarj.compiler.context.CVariableInfo;
 import org.caesarj.compiler.types.CType;
 import org.caesarj.compiler.types.TypeFactory;
 import org.caesarj.util.PositionedError;
 import org.caesarj.util.TokenReference;
 import org.caesarj.util.UnpositionedError;
 
 /**
  * This class represents a parameter declaration in the syntax tree
  */
 public class JFormalParameter extends JLocalVariable {
 
   // ----------------------------------------------------------------------
   // CONSTRUCTORS
   // ----------------------------------------------------------------------
 
   /**
    * Construct a node in the parsing tree
    * This method is directly called by the parser
    * @param	where		the line of this node in the source code
    * @param	ident		the name of this variable
    * @param	initializer	the initializer
    */
   public JFormalParameter(TokenReference where,
 			  int desc,
 			  CType type,
 			  String ident,
 			  boolean isFinal) {
     super(where, isFinal ? ACC_FINAL : 0, desc, type, ident, null);
   }
   
   public Object clone() {
 	return 
 		new JFormalParameter(
 			getTokenReference(),
 			getDescription(),
 			getType(),
 			getIdent(),
 			(getModifiers() & ACC_FINAL) == 0
 		);
   }
 
   // ----------------------------------------------------------------------
   // INTERFACE CHECKING
   // ----------------------------------------------------------------------
 
   /**
    * Second pass (quick), check interface looks good
    * Exceptions are not allowed here, this pass is just a tuning
    * pass in order to create informations about exported elements
    * such as Classes, Interfaces, Methods, Constructors and Fields
    * sub classes must check modifiers and call checkInterface(super)
    * @return true iff sub tree is correct enought to check code
    */
   public CType checkInterface(CTypeContext context) {
     try {
       type = type.checkType(context);
      //return type;
     } catch (UnpositionedError cue) {
      //context.reportTrouble(cue.addPosition(getTokenReference()));
      //return context.getTypeFactory().createReferenceType(TypeFactory.RFT_OBJECT);
     }
    return type;
   }
 
   // ----------------------------------------------------------------------
   // SEMANTIC ANALYSIS
   // ----------------------------------------------------------------------
 
   /**
    * Analyses the node (semantically).
    * @param	context		the analysis context
    * @exception	PositionedError	the analysis detected an error
    */
   public void analyse(CBodyContext context) throws PositionedError {
     try {
       type = type.checkType(context);
     } catch (UnpositionedError e) {
       throw e.addPosition(getTokenReference());
     }
     try {
       context.getBlockContext().addVariable(this);
     } catch (UnpositionedError e) {
       throw e.addPosition(getTokenReference());
     }
     context.setVariableInfo(getIndex(), CVariableInfo.INITIALIZED);
   }
 
   public String toString() {
       StringBuffer buffer = new StringBuffer();
       buffer.append("JFormalParameter[");
       if (isFinal()) buffer.append("final ");
       
       buffer.append(getIdent());
       buffer.append("]");
       return buffer.toString();
   }
 
   // ----------------------------------------------------------------------
   // PUBLIC CONSTANTS
   // ----------------------------------------------------------------------
 
   public static final JFormalParameter[]	EMPTY = new JFormalParameter[0];
 }
